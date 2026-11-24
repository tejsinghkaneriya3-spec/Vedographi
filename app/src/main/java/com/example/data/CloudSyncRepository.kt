package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
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

interface CloudSyncGateway {
    fun observeUserProfile(userId: String): Flow<CloudUserProfile?>
    fun observeCreations(userId: String): Flow<List<CloudSavedCreation>>
    suspend fun fetchCreationsOnce(userId: String): Result<List<CloudSavedCreation>>
    suspend fun ensureUserProfile(
        displayName: String?,
        darkTheme: Boolean,
        initialFavorites: Set<String>,
        initialRecents: List<String>
    ): Result<CloudUserProfile>
    suspend fun updateFavorites(favoriteIds: Set<String>): Result<Unit>
    suspend fun updateRecents(recentIds: List<String>): Result<Unit>
    suspend fun updateDarkTheme(darkTheme: Boolean): Result<Unit>
    suspend fun saveCreation(creationId: String, title: String, content: String, kind: String): Result<Unit>
    suspend fun deleteCreation(creationId: String): Result<Unit>
    suspend fun deleteAllUserCloudData(): Result<Unit>
}

class CloudSyncRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) : CloudSyncGateway {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    override fun observeUserProfile(userId: String): Flow<CloudUserProfile?> {
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

    override fun observeCreations(userId: String): Flow<List<CloudSavedCreation>> {
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

    override suspend fun fetchCreationsOnce(userId: String): Result<List<CloudSavedCreation>> {
        var path = "users/$userId/creations"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch when reading creations." }
            val colRef = db.collection("users").document(uid).collection("creations")
            path = colRef.path
            val snapshot = colRef.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
            val list = snapshot.toObjects(
                CloudSavedCreation::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    override suspend fun ensureUserProfile(
        displayName: String?,
        darkTheme: Boolean,
        initialFavorites: Set<String>,
        initialRecents: List<String>
    ): Result<CloudUserProfile> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            val snapshot = docRef.get().await()
            val cleanDisplayName = sanitizeDisplayName(displayName ?: auth.currentUser?.displayName)
            val cleanInitFavs = sanitizeIds(initialFavorites, MAX_FAVORITES)
            val cleanInitRecents = sanitizeIds(initialRecents, MAX_RECENTS)

            if (!snapshot.exists()) {
                val payload = buildCreateProfilePayload(
                    uid = uid,
                    displayName = cleanDisplayName,
                    darkTheme = darkTheme,
                    favoriteFontIds = cleanInitFavs,
                    recentFontIds = cleanInitRecents
                )
                docRef.set(payload).await()
                Result.success(
                    CloudUserProfile(
                        userId = uid,
                        displayName = cleanDisplayName,
                        darkTheme = darkTheme,
                        favoriteFontIds = cleanInitFavs,
                        recentFontIds = cleanInitRecents
                    )
                )
            } else {
                val existing = snapshot.toObject(
                    CloudUserProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: CloudUserProfile(userId = uid, darkTheme = darkTheme)

                val mergedFavs = (existing.favoriteFontIds + cleanInitFavs)
                    .filter { it.isNotBlank() }
                    .map { it.take(MAX_ID_LENGTH) }
                    .distinct()
                    .take(MAX_FAVORITES)
                val mergedRecents = (cleanInitRecents + existing.recentFontIds)
                    .filter { it.isNotBlank() }
                    .map { it.take(MAX_ID_LENGTH) }
                    .distinct()
                    .take(MAX_RECENTS)
                val resolvedDisplayName = cleanDisplayName ?: sanitizeDisplayName(existing.displayName)

                val updates = mutableMapOf<String, Any>()
                if (resolvedDisplayName != null && resolvedDisplayName != existing.displayName) {
                    updates["displayName"] = resolvedDisplayName
                }
                if (mergedFavs != existing.favoriteFontIds) {
                    updates["favoriteFontIds"] = mergedFavs
                }
                if (mergedRecents != existing.recentFontIds) {
                    updates["recentFontIds"] = mergedRecents
                }
                if (updates.isNotEmpty()) {
                    updates["updatedAt"] = FieldValue.serverTimestamp()
                    docRef.update(updates).await()
                }
                Result.success(
                    existing.copy(
                        userId = uid,
                        displayName = resolvedDisplayName,
                        favoriteFontIds = mergedFavs,
                        recentFontIds = mergedRecents
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun updateFavorites(favoriteIds: Set<String>): Result<Unit> {
        val sanitized = sanitizeIds(favoriteIds, MAX_FAVORITES)
        return updateProfileFieldsOrCreate(
            updateFields = mapOf(
                "favoriteFontIds" to sanitized,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            fallbackFavorites = sanitized
        )
    }

    override suspend fun updateRecents(recentIds: List<String>): Result<Unit> {
        val sanitized = sanitizeIds(recentIds, MAX_RECENTS)
        return updateProfileFieldsOrCreate(
            updateFields = mapOf(
                "recentFontIds" to sanitized,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            fallbackRecents = sanitized
        )
    }

    override suspend fun updateDarkTheme(darkTheme: Boolean): Result<Unit> {
        return updateProfileFieldsOrCreate(
            updateFields = mapOf(
                "darkTheme" to darkTheme,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            fallbackDarkTheme = darkTheme
        )
    }

    private suspend fun updateProfileFieldsOrCreate(
        updateFields: Map<String, Any>,
        fallbackDarkTheme: Boolean = true,
        fallbackFavorites: List<String> = emptyList(),
        fallbackRecents: List<String> = emptyList()
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            try {
                docRef.update(updateFields).await()
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                    val createPayload = buildCreateProfilePayload(
                        uid = uid,
                        displayName = sanitizeDisplayName(auth.currentUser?.displayName),
                        darkTheme = fallbackDarkTheme,
                        favoriteFontIds = fallbackFavorites,
                        recentFontIds = fallbackRecents
                    )
                    createProfileSafely(docRef, createPayload)
                } else {
                    throw e
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    private suspend fun createProfileSafely(docRef: DocumentReference, payload: Map<String, Any>) {
        docRef.set(payload).await()
    }

    private fun buildCreateProfilePayload(
        uid: String,
        displayName: String?,
        darkTheme: Boolean,
        favoriteFontIds: List<String>,
        recentFontIds: List<String>
    ): Map<String, Any> {
        @Suppress("UNCHECKED_CAST")
        return mapOf(
            "userId" to uid,
            "displayName" to displayName,
            "darkTheme" to darkTheme,
            "favoriteFontIds" to favoriteFontIds,
            "recentFontIds" to recentFontIds,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null } as Map<String, Any>
    }

    private fun sanitizeDisplayName(raw: String?): String? =
        raw?.trim()?.takeIf { it.isNotEmpty() }?.take(120)

    private fun sanitizeIds(ids: Iterable<String>, limit: Int): List<String> =
        ids.filter { it.isNotBlank() }
            .map { it.take(MAX_ID_LENGTH) }
            .distinct()
            .take(limit)

    override suspend fun saveCreation(
        creationId: String,
        title: String,
        content: String,
        kind: String
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/creations/$creationId"
        return try {
            val uid = requireUserId()
            require(creationId.matches(ID_REGEX) && creationId.length <= MAX_ID_LENGTH) {
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

    override suspend fun deleteCreation(creationId: String): Result<Unit> {
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

    override suspend fun deleteAllUserCloudData(): Result<Unit> {
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
        const val MAX_FAVORITES = 300
        const val MAX_RECENTS = 25
        const val MAX_ID_LENGTH = 128
        val ID_REGEX = Regex("^[a-zA-Z0-9_\\-]+$")
        val ALLOWED_KINDS = setOf("BIO", "USERNAME", "DECORATED", "NAME", "STATUS", "OCCASION")
    }
}
