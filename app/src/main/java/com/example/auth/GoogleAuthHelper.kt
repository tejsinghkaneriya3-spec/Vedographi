package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface DeleteAuthAccountResult {
    data object Success : DeleteAuthAccountResult
    data object ReauthRequired : DeleteAuthAccountResult
    data class Error(val message: String) : DeleteAuthAccountResult
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun isFirebaseConfigured(context: Context): Boolean {
    val appContext = context.applicationContext
    if (FirebaseApp.getApps(appContext).isNotEmpty()) return true
    return runCatching { FirebaseApp.initializeApp(appContext) != null }.getOrDefault(false)
}

fun safeCurrentFirebaseUser(context: Context): FirebaseUser? {
    if (!isFirebaseConfigured(context)) return null
    return runCatching { Firebase.auth.currentUser }.getOrNull()
}

fun authStateFlowSafe(context: Context): Flow<FirebaseUser?> {
    if (!isFirebaseConfigured(context)) return flowOf(null)
    return runCatching { Firebase.auth.authStateFlow() }.getOrElse { flowOf(null) }
}

private fun resolveDefaultWebClientId(context: Context): String? {
    val resId = context.resources.getIdentifier(
        "default_web_client_id",
        "string",
        context.packageName
    )
    if (resId == 0) return null
    return runCatching { context.getString(resId).trim() }
        .getOrNull()
        ?.takeIf { it.isNotEmpty() }
}

fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
): Job? {
    if (!isFirebaseConfigured(context)) {
        onUnauthenticated()
        return null
    }
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return null
    }
    val clientId = resolveDefaultWebClientId(context)
    if (clientId == null) {
        onUnauthenticated()
        return null
    }
    val activity = context.findActivity()
    if (activity == null) {
        Log.w("Auth", "Skipping silent auto sign-in: Activity context unavailable")
        onUnauthenticated()
        return null
    }
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    return scope.launch {
        try {
            val cred = credentialManager.getCredential(activity, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                withContext(NonCancellable) {
                    Firebase.auth.signInWithCredential(
                        GoogleAuthProvider.getCredential(token, null)
                    ).await()
                    withContext(Dispatchers.Main.immediate) {
                        onAuthSuccess()
                    }
                }
            } else {
                onUnauthenticated()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Silent auto sign-in cancelled or dismissed: ${e.message}", e)
            onUnauthenticated()
        } catch (e: NoCredentialException) {
            Log.d("Auth", "No previously authorized Google credential for silent sign-in")
            onUnauthenticated()
        } catch (e: GetCredentialException) {
            Log.w("Auth", "Silent auto sign-in credential error (${e.type}): ${e.errorMessage}", e)
            onUnauthenticated()
        } catch (e: Exception) {
            Log.w("Auth", "Silent auto sign-in failed: ${e.localizedMessage}", e)
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    if (!isFirebaseConfigured(context)) {
        onAuthError("Firebase Sign-In is not configured in this build. Please continue as Guest.")
        return
    }
    val clientId = resolveDefaultWebClientId(context)
    if (clientId == null) {
        onAuthError(
            "Google Sign-In configuration missing: google-services.json / default_web_client_id not found. You can continue as Guest."
        )
        return
    }
    val activity = context.findActivity()
    if (activity == null) {
        onAuthError("Unable to launch Google Sign-In from current window context")
        return
    }

    scope.launch {
        try {
            val cred = fetchGoogleCredential(activity, credentialManager, clientId)
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                withContext(NonCancellable) {
                    Firebase.auth.signInWithCredential(
                        GoogleAuthProvider.getCredential(token, null)
                    ).await()
                    withContext(Dispatchers.Main.immediate) {
                        onAuthSuccess()
                    }
                }
            } else {
                onAuthError("Unexpected credential type returned by Google Sign-In")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            withContext(Dispatchers.Main.immediate) {
                onAuthCancelled()
            }
        } catch (e: NoCredentialException) {
            Log.w("Auth", "No Google credentials available on device: ${e.message}", e)
            withContext(Dispatchers.Main.immediate) {
                onAuthError(
                    "No Google account is signed in on this device. Add a Google account in device Settings or tap Continue as Guest."
                )
            }
        } catch (e: GetCredentialProviderConfigurationException) {
            Log.e("Auth", "Credential provider misconfigured or missing Play Services", e)
            withContext(Dispatchers.Main.immediate) {
                onAuthError(
                    "Google Play Services Sign-In is unavailable on this device. Update Google Play Services or continue as Guest."
                )
            }
        } catch (e: GetCredentialException) {
            Log.e("Auth", "Credential Manager error (${e.type}): ${e.errorMessage}", e)
            withContext(Dispatchers.Main.immediate) {
                onAuthError(
                    e.errorMessage?.toString()?.takeIf { it.isNotBlank() }
                        ?: "Google Sign-In failed (${e.type}). You can retry or continue as Guest."
                )
            }
        } catch (e: FirebaseNetworkException) {
            Log.e("Auth", "Firebase network error during Google Sign-In", e)
            withContext(Dispatchers.Main.immediate) {
                onAuthError("Network connection required for Google Sign-In. Check your internet or continue as Guest.")
            }
        } catch (e: FirebaseAuthException) {
            Log.e("Auth", "Firebase Auth error (${e.errorCode}): ${e.localizedMessage}", e)
            withContext(Dispatchers.Main.immediate) {
                onAuthError(
                    e.localizedMessage?.takeIf { it.isNotBlank() }
                        ?: "Firebase authentication failed (${e.errorCode}). Please try again."
                )
            }
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            withContext(Dispatchers.Main.immediate) {
                onAuthError(e.localizedMessage ?: "Sign-in failed. Please try again or continue as Guest.")
            }
        }
    }
}

private suspend fun fetchGoogleCredential(
    activity: Activity,
    credentialManager: CredentialManager,
    clientId: String
) = try {
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val primaryRequest = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()
    credentialManager.getCredential(activity, primaryRequest).credential
} catch (primaryError: Exception) {
    if (primaryError is NoCredentialException || primaryError is GetCredentialUnsupportedException) {
        Log.w(
            "Auth",
            "Primary GetSignInWithGoogleOption unavailable (${primaryError.javaClass.simpleName}), trying GetGoogleIdOption fallback"
        )
        val fallbackOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(false)
            .build()
        val fallbackRequest = GetCredentialRequest.Builder()
            .addCredentialOption(fallbackOption)
            .build()
        credentialManager.getCredential(activity, fallbackRequest).credential
    } else {
        throw primaryError
    }
}

fun signOut(
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit = {}
) {
    scope.launch {
        withContext(NonCancellable) {
            clearCredentialSession(credentialManager)
            runCatching { Firebase.auth.signOut() }
            withContext(Dispatchers.Main.immediate) {
                onSignOutComplete()
            }
        }
    }
}

suspend fun clearCredentialSession(credentialManager: CredentialManager) {
    withContext(NonCancellable) {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w("Auth", "Failed to clear credential state: ${e.message}")
        }
    }
}

suspend fun deleteCurrentFirebaseUser(): DeleteAuthAccountResult {
    val user = runCatching { Firebase.auth.currentUser }.getOrNull()
        ?: return DeleteAuthAccountResult.Success
    return try {
        user.delete().await()
        DeleteAuthAccountResult.Success
    } catch (e: CancellationException) {
        throw e
    } catch (_: FirebaseAuthRecentLoginRequiredException) {
        DeleteAuthAccountResult.ReauthRequired
    } catch (e: Exception) {
        Log.e("Auth", "Failed to delete Firebase Auth user", e)
        DeleteAuthAccountResult.Error(e.localizedMessage ?: "Failed to delete account")
    }
}

suspend fun reauthenticateWithGoogle(
    context: Context,
    credentialManager: CredentialManager
): Result<Unit> {
    if (!isFirebaseConfigured(context)) {
        return Result.failure(IllegalStateException("Firebase Sign-In is not configured in this build"))
    }
    val user = Firebase.auth.currentUser
        ?: return Result.failure(IllegalStateException("No active signed-in user"))
    val clientId = resolveDefaultWebClientId(context)
        ?: return Result.failure(IllegalStateException("Google Sign-In configuration missing"))
    val activity = context.findActivity()
        ?: return Result.failure(IllegalStateException("Unable to launch Google Sign-In from current context"))

    return try {
        val cred = fetchGoogleCredential(activity, credentialManager, clientId)
        if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
            user.reauthenticate(GoogleAuthProvider.getCredential(token, null)).await()
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("Unexpected credential type during re-authentication"))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: GetCredentialCancellationException) {
        Log.w("Auth", "Google re-authentication cancelled: ${e.message}", e)
        Result.failure(IllegalStateException("Google verification was cancelled."))
    } catch (e: NoCredentialException) {
        Log.w("Auth", "No Google credentials available during re-auth", e)
        Result.failure(IllegalStateException("No Google account available on this device."))
    } catch (e: GetCredentialException) {
        Log.e("Auth", "Credential error during re-auth (${e.type}): ${e.errorMessage}", e)
        Result.failure(
            IllegalStateException(
                e.errorMessage?.toString()?.takeIf { it.isNotBlank() }
                    ?: "Google verification failed (${e.type})."
            )
        )
    } catch (e: Exception) {
        Log.e("Auth", "Google re-authentication failed", e)
        Result.failure(e)
    }
}
