package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CloudSyncRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<CloudUserProfile?> {
        if (userId.isBlank()) return flowOf(null)
        val path = "users/$userId"
        return db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        CloudUserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.GET, path)
                }
                throw error
            }
    }

    fun observeCreations(userId: String): Flow<List<CloudSavedCreation>> {
        if (userId.isBlank()) return flowOf(emptyList())
        val path = "users/$userId/creations"
        return db.collection("users").document(userId).collection("creations")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    CloudSavedCreation::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun ensureUserProfile(
        displayName: String?,
        darkTheme: Boolean,
        initialFavorites: Set<String>,
        initialRecents: List<String>
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val cleanDisplayName = displayName?.trim()?.takeIf { it.isNotEmpty() }?.take(120)
                @Suppress("UNCHECKED_CAST")
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to cleanDisplayName,
                    "darkTheme" to darkTheme,
                    "favoriteFontIds" to initialFavorites.take(300),
                    "recentFontIds" to initialRecents.take(30),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null } as Map<String, Any>
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateFavorites(favoriteIds: Set<String>): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "favoriteFontIds" to favoriteIds.take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateRecents(recentIds: List<String>): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "recentFontIds" to recentIds.take(30),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateDarkTheme(darkTheme: Boolean): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "darkTheme" to darkTheme,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun saveCreation(
        creationId: String,
        title: String,
        content: String,
        kind: String
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/creations/$creationId"
        return try {
            val uid = requireUserId()
            require(creationId.matches(ID_REGEX) && creationId.length <= 128) {
                "Invalid creation ID"
            }
            val safeTitle = title.trim().ifBlank { "Styled Text" }.take(120)
            val safeContent = content.trim().take(5000)
            require(safeContent.isNotEmpty()) { "Creation content cannot be empty" }
            val safeKind = if (kind in ALLOWED_KINDS) kind else "DECORATED"

            val docRef = db.collection("users").document(uid)
                .collection("creations").document(creationId)
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "title" to safeTitle,
                "content" to safeContent,
                "kind" to safeKind,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteCreation(creationId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/creations/$creationId"
        return try {
            val uid = requireUserId()
            require(creationId.isNotBlank() && creationId.matches(ID_REGEX)) {
                "Invalid creation ID for delete"
            }
            val docRef = db.collection("users").document(uid)
                .collection("creations").document(creationId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteAllUserCloudData(): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val userDocRef = db.collection("users").document(uid)
            val creationsCollection = userDocRef.collection("creations")
            docPath = creationsCollection.path
            val creationSnapshots = creationsCollection.get().await()
            if (!creationSnapshots.isEmpty) {
                creationSnapshots.documents.chunked(450).forEach { chunk ->
                    val batch = db.batch()
                    chunk.forEach { doc -> batch.delete(doc.reference) }
                    batch.commit().await()
                }
            }
            docPath = userDocRef.path
            userDocRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    private companion object {
        val ID_REGEX = Regex("^[a-zA-Z0-9_\\-]+$")
        val ALLOWED_KINDS = setOf("BIO", "USERNAME", "DECORATED", "NAME", "STATUS", "OCCASION")
    }
}
