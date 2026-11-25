package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.domain.AdminConfig
import com.example.domain.AppMonetizationConfig
import com.example.domain.CustomFontCodec
import com.example.domain.CustomFontDefinition
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
import java.util.Locale

interface CloudSyncGateway {
    fun observeUserProfile(userId: String): Flow<CloudUserProfile?>
    fun observeCreations(userId: String): Flow<List<CloudSavedCreation>>
    fun observeCustomFonts(userId: String): Flow<List<CloudCustomFont>>
    fun observeMonetizationConfig(): Flow<AppMonetizationConfig>
    suspend fun fetchCreationsOnce(userId: String): Result<List<CloudSavedCreation>>
    suspend fun fetchCustomFontsOnce(userId: String): Result<List<CloudCustomFont>>
    suspend fun ensureUserProfile(
        displayName: String?,
        darkTheme: Boolean,
        initialFavorites: Set<String>,
        initialRecents: List<String>,
        initialPoints: Int = 0,
        initialUnlockedProIds: Set<String> = emptySet(),
        initialAdsWatched: Int = 0
    ): Result<CloudUserProfile>
    suspend fun updateFavorites(favoriteIds: Set<String>): Result<Unit>
    suspend fun updateRecents(recentIds: List<String>): Result<Unit>
    suspend fun updateDarkTheme(darkTheme: Boolean): Result<Unit>
    suspend fun updateWalletAndProUnlocks(
        pointsBalance: Int,
        unlockedProIds: Set<String>,
        adsWatchedCount: Int
    ): Result<Unit>
    suspend fun ensureReferralCode(preferredName: String?): Result<String>
    suspend fun redeemReferralCode(rawCode: String, bonusPoints: Int): Result<Int>
    suspend fun saveCreation(creationId: String, title: String, content: String, kind: String): Result<Unit>
    suspend fun deleteCreation(creationId: String): Result<Unit>
    suspend fun saveCustomFont(definition: CustomFontDefinition): Result<Unit>
    suspend fun deleteCustomFont(fontId: String): Result<Unit>
    suspend fun updateMonetizationConfig(config: AppMonetizationConfig): Result<Unit>
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

    override fun observeCustomFonts(userId: String): Flow<List<CloudCustomFont>> {
        if (userId.isBlank()) return flowOf(emptyList())
        val path = "users/$userId/custom_fonts"
        return db.collection("users").document(userId).collection("custom_fonts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    CloudCustomFont::class.java,
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

    override fun observeMonetizationConfig(): Flow<AppMonetizationConfig> {
        val path = "app_config/monetization"
        return db.collection("app_config").document("monetization")
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(CloudMonetizationConfig::class.java)?.toDomain()
                        ?: AppMonetizationConfig()
                } else {
                    AppMonetizationConfig()
                }
            }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("CloudSyncRepo", "Unable to read monetization config at $path; using defaults", error)
                emit(AppMonetizationConfig())
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

    override suspend fun fetchCustomFontsOnce(userId: String): Result<List<CloudCustomFont>> {
        var path = "users/$userId/custom_fonts"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch when reading custom fonts." }
            val colRef = db.collection("users").document(uid).collection("custom_fonts")
            path = colRef.path
            val snapshot = colRef.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
            val list = snapshot.toObjects(
                CloudCustomFont::class.java,
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
        initialRecents: List<String>,
        initialPoints: Int,
        initialUnlockedProIds: Set<String>,
        initialAdsWatched: Int
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
            val cleanInitPro = sanitizeIds(initialUnlockedProIds, MAX_UNLOCKED_PRO)
            val safePoints = initialPoints.coerceIn(0, MAX_POINTS).toLong()
            val safeAds = initialAdsWatched.coerceIn(0, MAX_POINTS).toLong()

            if (!snapshot.exists()) {
                val payload = buildCreateProfilePayload(
                    uid = uid,
                    displayName = cleanDisplayName,
                    darkTheme = darkTheme,
                    favoriteFontIds = cleanInitFavs,
                    recentFontIds = cleanInitRecents,
                    pointsBalance = safePoints,
                    unlockedProFontIds = cleanInitPro,
                    adsWatchedCount = safeAds
                )
                docRef.set(payload).await()
                Result.success(
                    CloudUserProfile(
                        userId = uid,
                        displayName = cleanDisplayName,
                        darkTheme = darkTheme,
                        favoriteFontIds = cleanInitFavs,
                        recentFontIds = cleanInitRecents,
                        pointsBalance = safePoints,
                        unlockedProFontIds = cleanInitPro,
                        adsWatchedCount = safeAds
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
                val mergedPro = (existing.unlockedProFontIds + cleanInitPro)
                    .filter { it.isNotBlank() }
                    .map { it.take(MAX_ID_LENGTH) }
                    .distinct()
                    .take(MAX_UNLOCKED_PRO)
                val mergedPoints = maxOf(existing.pointsBalance.coerceAtLeast(0L), safePoints)
                val mergedAds = maxOf(existing.adsWatchedCount.coerceAtLeast(0L), safeAds)
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
                if (mergedPro != existing.unlockedProFontIds) {
                    updates["unlockedProFontIds"] = mergedPro
                }
                if (mergedPoints != existing.pointsBalance) {
                    updates["pointsBalance"] = mergedPoints
                }
                if (mergedAds != existing.adsWatchedCount) {
                    updates["adsWatchedCount"] = mergedAds
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
                        recentFontIds = mergedRecents,
                        pointsBalance = mergedPoints,
                        unlockedProFontIds = mergedPro,
                        adsWatchedCount = mergedAds
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

    override suspend fun updateWalletAndProUnlocks(
        pointsBalance: Int,
        unlockedProIds: Set<String>,
        adsWatchedCount: Int
    ): Result<Unit> {
        val safePoints = pointsBalance.coerceIn(0, MAX_POINTS).toLong()
        val safeUnlocked = sanitizeIds(unlockedProIds, MAX_UNLOCKED_PRO)
        val safeAds = adsWatchedCount.coerceIn(0, MAX_POINTS).toLong()
        return updateProfileFieldsOrCreate(
            updateFields = mapOf(
                "pointsBalance" to safePoints,
                "unlockedProFontIds" to safeUnlocked,
                "adsWatchedCount" to safeAds,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            fallbackPoints = safePoints,
            fallbackUnlockedPro = safeUnlocked,
            fallbackAdsWatched = safeAds
        )
    }

    override suspend fun ensureReferralCode(preferredName: String?): Result<String> {
        var path = "referral_codes"
        return try {
            val uid = requireUserId()
            val userDocRef = db.collection("users").document(uid)
            val userSnap = userDocRef.get().await()
            val existingCode = userSnap.getString("referralCode")?.trim()?.uppercase(Locale.US)
            if (!existingCode.isNullOrBlank() && REFERRAL_CODE_REGEX.matches(existingCode)) {
                return Result.success(existingCode)
            }

            val prefix = (preferredName ?: "VEDO")
                .uppercase(Locale.US)
                .replace(Regex("[^A-Z0-9]"), "")
                .take(4)
                .padEnd(4, 'V')
            val uidPart = uid
                .uppercase(Locale.US)
                .replace(Regex("[^A-Z0-9]"), "")
                .takeLast(4)
                .padStart(4, '9')
            val candidate = (prefix + uidPart).take(8)
            val codeRef = db.collection("referral_codes").document(candidate)
            path = codeRef.path
            val codeSnap = codeRef.get().await()
            if (!codeSnap.exists()) {
                val payload = mapOf(
                    "code" to candidate,
                    "ownerUid" to uid,
                    "ownerName" to (sanitizeDisplayName(preferredName) ?: "Creator"),
                    "createdAt" to FieldValue.serverTimestamp()
                )
                codeRef.set(payload).await()
            }
            if (userSnap.exists()) {
                userDocRef.update(
                    mapOf(
                        "referralCode" to candidate,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val createPayload = buildCreateProfilePayload(
                    uid = uid,
                    displayName = sanitizeDisplayName(preferredName ?: auth.currentUser?.displayName),
                    darkTheme = true,
                    favoriteFontIds = emptyList(),
                    recentFontIds = emptyList(),
                    referralCode = candidate
                )
                userDocRef.set(createPayload).await()
            }
            Result.success(candidate)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    override suspend fun redeemReferralCode(rawCode: String, bonusPoints: Int): Result<Int> {
        var path = "referral_claims"
        return try {
            val uid = requireUserId()
            val cleanCode = rawCode.trim().uppercase(Locale.US)
            require(REFERRAL_CODE_REGEX.matches(cleanCode)) {
                "Enter a valid 6–12 character referral code (A-Z, 0-9)."
            }
            val codeRef = db.collection("referral_codes").document(cleanCode)
            path = codeRef.path
            val codeSnap = codeRef.get().await()
            require(codeSnap.exists()) { "Referral code '$cleanCode' was not found." }
            val ownerUid = codeSnap.getString("ownerUid").orEmpty()
            require(ownerUid.isNotBlank() && ownerUid != uid) {
                "You cannot redeem your own referral code."
            }

            val claimRef = db.collection("referral_claims").document(uid)
            path = claimRef.path
            val existingClaim = claimRef.get().await()
            require(!existingClaim.exists()) { "You have already redeemed a referral code." }

            val safeBonus = bonusPoints.coerceIn(1, 50)
            val claimPayload = mapOf(
                "claimerUid" to uid,
                "code" to cleanCode,
                "referrerUid" to ownerUid,
                "bonusPoints" to safeBonus.toLong(),
                "createdAt" to FieldValue.serverTimestamp()
            )
            claimRef.set(claimPayload).await()

            val userRef = db.collection("users").document(uid)
            path = userRef.path
            val userSnap = userRef.get().await()
            val currentPoints = userSnap.getLong("pointsBalance")?.coerceAtLeast(0L) ?: 0L
            val nextPoints = (currentPoints + safeBonus).coerceAtMost(MAX_POINTS.toLong())
            if (userSnap.exists()) {
                userRef.update(
                    mapOf(
                        "pointsBalance" to nextPoints,
                        "referredByCode" to cleanCode,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val createPayload = buildCreateProfilePayload(
                    uid = uid,
                    displayName = sanitizeDisplayName(auth.currentUser?.displayName),
                    darkTheme = true,
                    favoriteFontIds = emptyList(),
                    recentFontIds = emptyList(),
                    pointsBalance = nextPoints,
                    referredByCode = cleanCode
                )
                userRef.set(createPayload).await()
            }
            Result.success(safeBonus)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    private suspend fun updateProfileFieldsOrCreate(
        updateFields: Map<String, Any>,
        fallbackDarkTheme: Boolean = true,
        fallbackFavorites: List<String> = emptyList(),
        fallbackRecents: List<String> = emptyList(),
        fallbackPoints: Long = 0L,
        fallbackUnlockedPro: List<String> = emptyList(),
        fallbackAdsWatched: Long = 0L
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
                        recentFontIds = fallbackRecents,
                        pointsBalance = fallbackPoints,
                        unlockedProFontIds = fallbackUnlockedPro,
                        adsWatchedCount = fallbackAdsWatched
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
        recentFontIds: List<String>,
        pointsBalance: Long = 0L,
        unlockedProFontIds: List<String> = emptyList(),
        referralCode: String? = null,
        referredByCode: String? = null,
        referralCount: Long = 0L,
        adsWatchedCount: Long = 0L
    ): Map<String, Any> {
        @Suppress("UNCHECKED_CAST")
        return mapOf(
            "userId" to uid,
            "displayName" to displayName,
            "darkTheme" to darkTheme,
            "favoriteFontIds" to favoriteFontIds,
            "recentFontIds" to recentFontIds,
            "pointsBalance" to pointsBalance,
            "unlockedProFontIds" to unlockedProFontIds,
            "referralCode" to referralCode,
            "referredByCode" to referredByCode,
            "referralCount" to referralCount,
            "adsWatchedCount" to adsWatchedCount,
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

    override suspend fun saveCustomFont(definition: CustomFontDefinition): Result<Unit> {
        val safeId = definition.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(MAX_ID_LENGTH)
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/custom_fonts/$safeId"
        return try {
            val uid = requireUserId()
            require(safeId.isNotBlank() && safeId.matches(ID_REGEX)) { "Invalid custom font ID" }
            val safeName = definition.name.trim().ifBlank { "My Custom Font" }.take(64)
            val encodedMap = CustomFontCodec.encodeMap(definition.charMap).take(4000)

            val docRef = db.collection("users").document(uid)
                .collection("custom_fonts").document(safeId)
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "name" to safeName,
                "charMapEncoded" to encodedMap,
                "prefix" to definition.prefix.take(48),
                "suffix" to definition.suffix.take(48),
                "letterJoiner" to definition.letterJoiner.take(12),
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

    override suspend fun deleteCustomFont(fontId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/custom_fonts/$fontId"
        return try {
            val uid = requireUserId()
            require(fontId.isNotBlank() && fontId.matches(ID_REGEX)) { "Invalid custom font ID" }
            val docRef = db.collection("users").document(uid)
                .collection("custom_fonts").document(fontId)
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

    override suspend fun updateMonetizationConfig(config: AppMonetizationConfig): Result<Unit> {
        val docPath = "app_config/monetization"
        return try {
            requireUserId()
            val email = auth.currentUser?.email
            check(AdminConfig.isAdminUser(email, isGuest = false)) {
                "Only the verified app administrator can modify monetization settings."
            }
            val payload = mapOf(
                "adsEnabled" to config.adsEnabled,
                "bannerAdUnitId" to config.bannerAdUnitId.trim().take(100),
                "rewardedAdUnitId" to config.rewardedAdUnitId.trim().take(100),
                "adRewardPoints" to config.adRewardPoints.coerceIn(1, 100).toLong(),
                "referralBonusPoints" to config.referralBonusPoints.coerceIn(1, 50).toLong(),
                "announcementBanner" to config.announcementBanner.trim().take(240),
                "updatedBy" to AdminConfig.ADMIN_EMAIL,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("app_config").document("monetization").set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
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
            val customFontsCollection = userDocRef.collection("custom_fonts")
            docPath = customFontsCollection.path
            val customFontSnapshots = customFontsCollection.get().await()
            if (!customFontSnapshots.isEmpty) {
                customFontSnapshots.documents.chunked(450).forEach { chunk ->
                    val batch = db.batch()
                    chunk.forEach { doc -> batch.delete(doc.reference) }
                    batch.commit().await()
                }
            }
            runCatching {
                db.collection("referral_claims").document(uid).delete().await()
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
        const val MAX_UNLOCKED_PRO = 150
        const val MAX_POINTS = 1000000
        const val MAX_ID_LENGTH = 128
        val ID_REGEX = Regex("^[a-zA-Z0-9_\\-]+$")
        val REFERRAL_CODE_REGEX = Regex("^[A-Z0-9]{6,12}$")
        val ALLOWED_KINDS = setOf("BIO", "USERNAME", "DECORATED", "NAME", "STATUS", "OCCASION")
    }
}
