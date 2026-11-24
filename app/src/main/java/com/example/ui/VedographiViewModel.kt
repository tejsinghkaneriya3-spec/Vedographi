package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.DeleteAuthAccountResult
import com.example.data.CloudSavedCreation
import com.example.data.CloudSyncGateway
import com.example.data.CloudUiState
import com.example.data.CloudUserProfile
import com.example.data.PreferencesRepository
import com.example.domain.BioCategory
import com.example.domain.BioPlatform
import com.example.domain.CachedEntrySource
import com.example.domain.CachedStyleEntry
import com.example.domain.DecorationTemplate
import com.example.domain.FontCatalog
import com.example.domain.FontCategory
import com.example.domain.FontStyleSpec
import com.example.domain.OccasionCategory
import com.example.domain.OccasionTemplate
import com.example.domain.SavedCreation
import com.example.domain.StudioDraftSnapshot
import com.example.domain.StudioGenerators
import com.example.domain.TextStudioCatalog
import com.example.domain.UnicodeFontEngine
import com.example.domain.UsernameVibe
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface AccountDeletionState {
    data object Idle : AccountDeletionState
    data object Deleting : AccountDeletionState
    data object ReauthRequired : AccountDeletionState
    data class Error(val message: String) : AccountDeletionState
    data object Completed : AccountDeletionState
}

data class FontPreviewItem(
    val spec: FontStyleSpec,
    val previewText: String,
    val isFavorite: Boolean,
    val isRecentlyCopied: Boolean
)

data class BatchCopyVariation(
    val id: String,
    val styleName: String,
    val styledText: String,
    val isFavorite: Boolean = false
)

data class BatchCopySheetState(
    val title: String,
    val sourceText: String,
    val variations: List<BatchCopyVariation>,
    val includeLabelsDefault: Boolean = true
)

class VedographiViewModel(
    private val repo: PreferencesRepository,
    private val cloudRepo: CloudSyncGateway? = null,
    private val currentUserId: String? = null,
    private val userDisplayName: String? = null,
    networkOnlineFlow: Flow<Boolean> = flowOf(true)
) : ViewModel() {

    val isGuest: Boolean = currentUserId.isNullOrBlank() || cloudRepo == null

    private val _cloudSyncError = MutableStateFlow<String?>(null)
    val cloudSyncError: StateFlow<String?> = _cloudSyncError.asStateFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    val isOnline: StateFlow<Boolean> = networkOnlineFlow
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val cachedGeneratedStyles: StateFlow<List<CachedStyleEntry>> = repo.cachedGeneratedStyles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    val lastCacheSyncMillis: StateFlow<Long> = repo.lastCloudCacheSyncMillis
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), 0L)

    private val _accountDeletionState = MutableStateFlow<AccountDeletionState>(AccountDeletionState.Idle)
    val accountDeletionState: StateFlow<AccountDeletionState> = _accountDeletionState.asStateFlow()

    private val initialSyncCompleted = MutableStateFlow(isGuest)
    private var draftSaveJob: Job? = null

    private val cloudProfileState: StateFlow<CloudUiState<CloudUserProfile?>> =
        if (!isGuest && cloudRepo != null && !currentUserId.isNullOrBlank()) {
            cloudRepo.observeUserProfile(currentUserId)
                .map<CloudUserProfile?, CloudUiState<CloudUserProfile?>> { CloudUiState.Success(it) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val rootError = firestoreError ?: error
                    Log.w(TAG, "Error observing cloud profile", rootError)
                    emit(CloudUiState.Error(rootError.localizedMessage ?: "Cloud profile sync unavailable"))
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), CloudUiState.Loading)
        } else {
            MutableStateFlow<CloudUiState<CloudUserProfile?>>(CloudUiState.Loading).asStateFlow()
        }

    private val cloudCreationsState: StateFlow<CloudUiState<List<CloudSavedCreation>>> =
        if (!isGuest && cloudRepo != null && !currentUserId.isNullOrBlank()) {
            cloudRepo.observeCreations(currentUserId)
                .map<List<CloudSavedCreation>, CloudUiState<List<CloudSavedCreation>>> { CloudUiState.Success(it) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val rootError = firestoreError ?: error
                    Log.w(TAG, "Error observing cloud creations", rootError)
                    emit(CloudUiState.Error(rootError.localizedMessage ?: "Cloud creations sync unavailable"))
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), CloudUiState.Loading)
        } else {
            MutableStateFlow<CloudUiState<List<CloudSavedCreation>>>(CloudUiState.Loading).asStateFlow()
        }

    val isCloudSynced: StateFlow<Boolean> = combine(isOnline, cloudProfileState, cloudCreationsState) { online, p, c ->
        !isGuest && online && p is CloudUiState.Success && c is CloudUiState.Success
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), false)

    val effectiveDisplayName: StateFlow<String?> = cloudProfileState
        .map { state ->
            val cloudName = (state as? CloudUiState.Success)?.data?.displayName
            userDisplayName?.takeIf { it.isNotBlank() } ?: cloudName?.takeIf { it.isNotBlank() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), userDisplayName)

    val isDarkTheme: StateFlow<Boolean> = combine(isOnline, repo.isDarkTheme, cloudProfileState) { online, localDark, cloudState ->
        when {
            !isGuest && online && cloudState is CloudUiState.Success -> cloudState.data?.darkTheme ?: localDark
            else -> localDark
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), true)

    val favoriteFontIds: StateFlow<Set<String>> = combine(isOnline, repo.favoriteFontIds, cloudProfileState) { online, localFavs, cloudState ->
        when {
            !isGuest && online && cloudState is CloudUiState.Success -> {
                cloudState.data?.favoriteFontIds?.toSet()?.let { it + localFavs } ?: localFavs
            }
            else -> localFavs
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptySet())

    val recentFontIds: StateFlow<List<String>> = combine(isOnline, repo.recentFontIds, cloudProfileState) { online, localRecents, cloudState ->
        when {
            !isGuest && online && cloudState is CloudUiState.Success -> {
                val cloudList = cloudState.data?.recentFontIds.orEmpty()
                if (cloudList.isEmpty()) localRecents else (localRecents + cloudList).distinct().take(25)
            }
            else -> localRecents
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    val savedCreations: StateFlow<List<SavedCreation>> = combine(isOnline, repo.savedCreations, cloudCreationsState) { online, localList, cloudState ->
        when {
            !isGuest && online && cloudState is CloudUiState.Success -> {
                val cloudMapped = cloudState.data.map {
                    SavedCreation(
                        id = it.id,
                        title = it.title,
                        content = it.content,
                        kind = it.kind
                    )
                }
                val cloudIds = cloudMapped.map { it.id }.toSet()
                val cloudContents = cloudMapped.map { it.content }.toSet()
                val localOnly = localList.filter { it.id !in cloudIds && it.content !in cloudContents }
                localOnly + cloudMapped
            }
            else -> localList
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    init {
        viewModelScope.launch {
            if (!isGuest && !currentUserId.isNullOrBlank()) {
                repo.prepareSessionForUser(currentUserId)
            }
            hydrateDraftsFromDataStore()
            if (!isGuest && isOnline.value) {
                syncLocalBackupToCloud(showErrorOnFailure = false)
            }
            initialSyncCompleted.value = true
        }

        if (!isGuest) {
            // Read-through cache: mirror cloud profile updates into DataStore once initial merge finishes
            viewModelScope.launch {
                combine(initialSyncCompleted, _isBackingUp, cloudProfileState) { synced, backingUp, state ->
                    if (synced && !backingUp) (state as? CloudUiState.Success)?.data else null
                }.collect { profile ->
                    if (profile != null) {
                        repo.cacheCloudProfile(
                            darkTheme = profile.darkTheme,
                            favoriteIds = profile.favoriteFontIds.toSet(),
                            recentIds = profile.recentFontIds
                        )
                    }
                }
            }

            // Read-through cache: mirror cloud creations into DataStore once initial sync finishes
            viewModelScope.launch {
                combine(initialSyncCompleted, _isBackingUp, cloudCreationsState) { synced, backingUp, state ->
                    if (synced && !backingUp) (state as? CloudUiState.Success)?.data else null
                }.collect { list ->
                    if (list != null) {
                        repo.cacheCloudCreations(
                            list.map {
                                SavedCreation(
                                    id = it.id,
                                    title = it.title,
                                    content = it.content,
                                    kind = it.kind
                                )
                            }
                        )
                    }
                }
            }

            // Auto-sync offline changes when internet connection comes back
            viewModelScope.launch {
                var previouslyOnline = isOnline.value
                isOnline.drop(1).collect { nowOnline ->
                    if (!previouslyOnline && nowOnline) {
                        syncLocalBackupToCloud(showErrorOnFailure = false)
                    }
                    previouslyOnline = nowOnline
                }
            }
        }
    }

    private suspend fun hydrateDraftsFromDataStore() {
        val draft = repo.studioDraftSnapshot.first()
        draft.fontInputText?.let { _inputText.value = it }
        draft.decoratorInput?.let { _decoratorInput.value = it }
        draft.decoratorFontId?.let { _selectedDecoratorFontId.value = it }
        draft.decoratorWingId?.let { _selectedWingId.value = it }
        draft.decoratorBorderId?.let { _selectedBorderId.value = it }
        draft.decoratorSeparatorId?.let { _selectedSeparatorId.value = it }
        draft.bioHeadline?.let { _bioHeadline.value = it }
        draft.bioBodyText?.let { _bioBodyText.value = it }
        draft.usernameInput?.let { _usernameInput.value = it }
        draft.nameDesignerName?.let { _nameDesignerName.value = it }
        draft.nameDesignerSubtitle?.let { _nameDesignerSubtitle.value = it }
        draft.waHeaderTag?.let { _waHeaderTag.value = it }
        draft.waBodyText?.let { _waBodyText.value = it }
    }

    private fun scheduleDraftPersistence() {
        draftSaveJob?.cancel()
        draftSaveJob = viewModelScope.launch {
            delay(300)
            repo.saveStudioDraft {
                StudioDraftSnapshot(
                    fontInputText = _inputText.value,
                    decoratorInput = _decoratorInput.value,
                    decoratorFontId = _selectedDecoratorFontId.value,
                    decoratorWingId = _selectedWingId.value,
                    decoratorBorderId = _selectedBorderId.value,
                    decoratorSeparatorId = _selectedSeparatorId.value,
                    bioHeadline = _bioHeadline.value,
                    bioBodyText = _bioBodyText.value,
                    usernameInput = _usernameInput.value,
                    nameDesignerName = _nameDesignerName.value,
                    nameDesignerSubtitle = _nameDesignerSubtitle.value,
                    waHeaderTag = _waHeaderTag.value,
                    waBodyText = _waBodyText.value
                )
            }
        }
    }

    fun backupNow(onComplete: (String) -> Unit = {}) {
        viewModelScope.launch {
            if (isGuest || cloudRepo == null) {
                onComplete("Guest Mode: styles are saved on this device. Sign in with Google to back up to the cloud.")
                return@launch
            }
            if (!isOnline.value) {
                onComplete("Offline: your styles & templates are safely cached in DataStore.")
                return@launch
            }
            val resultMessage = syncLocalBackupToCloud(showErrorOnFailure = true)
            onComplete(resultMessage)
        }
    }

    fun resetAccountDeletionState() {
        _accountDeletionState.value = AccountDeletionState.Idle
    }

    fun clearLocalData(onComplete: (String) -> Unit = {}) {
        viewModelScope.launch {
            repo.clearAllUserData()
            onComplete(
                if (isGuest) {
                    "All local guest data, cache, and drafts cleared."
                } else {
                    "Local DataStore cache & drafts cleared."
                }
            )
        }
    }

    fun deleteCloudAccountAndData(
        onDeleteAuthUser: suspend () -> DeleteAuthAccountResult,
        onPostDeleteCleanup: suspend () -> Unit = {},
        onSuccess: () -> Unit = {}
    ) {
        if (_accountDeletionState.value == AccountDeletionState.Deleting) return
        val cloud = cloudRepo
        if (isGuest || cloud == null || currentUserId.isNullOrBlank()) {
            _accountDeletionState.value = AccountDeletionState.Error("No signed-in cloud account to delete.")
            return
        }
        if (!isOnline.value) {
            _accountDeletionState.value = AccountDeletionState.Error(
                "Internet connection is required to delete your cloud account."
            )
            return
        }
        viewModelScope.launch {
            _accountDeletionState.value = AccountDeletionState.Deleting
            val cloudResult = cloud.deleteAllUserCloudData()
            if (cloudResult.isFailure) {
                _accountDeletionState.value = AccountDeletionState.Error(
                    cloudResult.exceptionOrNull()?.localizedMessage ?: "Failed to delete cloud data"
                )
                return@launch
            }
            when (val authResult = onDeleteAuthUser()) {
                is DeleteAuthAccountResult.Success -> {
                    repo.clearSessionOnSignOut()
                    onPostDeleteCleanup()
                    _accountDeletionState.value = AccountDeletionState.Completed
                    onSuccess()
                }
                is DeleteAuthAccountResult.ReauthRequired -> {
                    _accountDeletionState.value = AccountDeletionState.ReauthRequired
                }
                is DeleteAuthAccountResult.Error -> {
                    _accountDeletionState.value = AccountDeletionState.Error(authResult.message)
                }
            }
        }
    }

    private suspend fun syncLocalBackupToCloud(showErrorOnFailure: Boolean): String {
        val cloud = cloudRepo ?: return "Guest Mode active"
        val uid = currentUserId?.takeIf { it.isNotBlank() } ?: return "Guest Mode active"
        _isBackingUp.value = true
        return try {
            val localDark = repo.isDarkTheme.first()
            val localFavs = repo.favoriteFontIds.first()
            val localRecents = repo.recentFontIds.first()
            val localCreations = repo.savedCreations.first()

            val ensureResult = cloud.ensureUserProfile(
                displayName = userDisplayName,
                darkTheme = localDark,
                initialFavorites = localFavs,
                initialRecents = localRecents
            )
            if (ensureResult.isFailure) {
                val msg = ensureResult.exceptionOrNull()?.localizedMessage ?: "Cloud backup failed"
                if (showErrorOnFailure) _cloudSyncError.value = msg
                return msg
            }

            val mergedProfile = ensureResult.getOrNull()
            if (mergedProfile != null) {
                repo.cacheCloudProfile(
                    darkTheme = mergedProfile.darkTheme,
                    favoriteIds = mergedProfile.favoriteFontIds.toSet(),
                    recentIds = mergedProfile.recentFontIds
                )
            }

            // Fetch existing cloud creations directly rather than reading uncollected Loading state
            val cloudCreationsResult = cloud.fetchCreationsOnce(uid)
            val existingCloudCreations = cloudCreationsResult.getOrNull()
                ?: (cloudCreationsState.value as? CloudUiState.Success)?.data.orEmpty()
            val cloudIds = existingCloudCreations.map { it.id }.toSet()
            val cloudContents = existingCloudCreations.map { it.content }.toSet()
            var uploadedCount = 0
            localCreations.forEach { localItem ->
                val safeId = localItem.id
                    .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                    .ifBlank { UUID.randomUUID().toString() }
                    .take(128)
                if (safeId !in cloudIds && localItem.content !in cloudContents) {
                    val res = cloud.saveCreation(
                        creationId = safeId,
                        title = localItem.title,
                        content = localItem.content,
                        kind = localItem.kind
                    )
                    if (res.isSuccess) uploadedCount++
                }
            }
            if (uploadedCount > 0) {
                "Backed up $uploadedCount local creation(s) & profile to cloud!"
            } else {
                "All favorites, recents & studio projects backed up to cloud!"
            }
        } finally {
            _isBackingUp.value = false
        }
    }

    // === Home / Fonts Screen State ===
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    val isInputEmpty: StateFlow<Boolean> = _inputText
        .map { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), true)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(FontCategory.ALL)
    val selectedCategory: StateFlow<FontCategory> = _selectedCategory.asStateFlow()

    private val _selectedCalligraphyPreset = MutableStateFlow(com.example.ui.theme.HindiCalligraphyPreset.KALAM)
    val selectedCalligraphyPreset: StateFlow<com.example.ui.theme.HindiCalligraphyPreset> =
        _selectedCalligraphyPreset.asStateFlow()

    private val _copiedFeedbackId = MutableStateFlow<String?>(null)
    val copiedFeedbackId: StateFlow<String?> = _copiedFeedbackId.asStateFlow()

    private val _shareSheetPayload = MutableStateFlow<String?>(null)
    val shareSheetPayload: StateFlow<String?> = _shareSheetPayload.asStateFlow()

    private val _batchCopySheet = MutableStateFlow<BatchCopySheetState?>(null)
    val batchCopySheet: StateFlow<BatchCopySheetState?> = _batchCopySheet.asStateFlow()

    val hasHindiInInput: StateFlow<Boolean> = _inputText
        .map { UnicodeFontEngine.containsHindi(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), false)

    fun selectCalligraphyPreset(preset: com.example.ui.theme.HindiCalligraphyPreset) {
        _selectedCalligraphyPreset.value = preset
    }

    val trendingFontItems: StateFlow<List<FontPreviewItem>> = combine(
        _inputText,
        favoriteFontIds,
        _copiedFeedbackId
    ) { rawText, favs, copiedId ->
        FontCatalog.allStyles
            .filter { it.isTrending }
            .map { spec ->
                val effective = if (rawText.isEmpty() && spec.category == FontCategory.HINDI_CALLIGRAPHY) {
                    "हर शब्द • VEDOGRAPHi"
                } else {
                    rawText.ifEmpty { "VEDOGRAPHi" }
                }
                FontPreviewItem(
                    spec = spec,
                    previewText = spec.transform(effective),
                    isFavorite = spec.id in favs,
                    isRecentlyCopied = copiedId == spec.id
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    private fun matchesFontQuery(spec: com.example.domain.FontStyleSpec, query: String): Boolean {
        val tokens = query.split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return true
        val searchableFields = buildString {
            append(spec.name.lowercase())
            append(' ')
            append(spec.category.label.lowercase())
            append(' ')
            append(spec.id.lowercase().replace('_', ' '))
            spec.badge?.let {
                append(' ')
                append(it.lowercase())
            }
            spec.calligraphyPresetId?.let {
                append(' ')
                append(it.lowercase().replace('_', ' '))
            }
        }
        return tokens.all { token -> searchableFields.contains(token) }
    }

    val isSearchExpandedAcrossAll: StateFlow<Boolean> = combine(
        _searchQuery,
        _selectedCategory
    ) { rawQuery, category ->
        val query = rawQuery.trim().lowercase()
        if (query.isEmpty() ||
            category == FontCategory.ALL ||
            category == FontCategory.FAVORITES ||
            category == FontCategory.RECENT
        ) {
            false
        } else {
            val categoryBase = when (category) {
                FontCategory.TRENDING -> FontCatalog.allStyles.filter { it.isTrending }
                else -> FontCatalog.allStyles.filter { it.category == category }
            }
            categoryBase.none { matchesFontQuery(it, query) } &&
                FontCatalog.allStyles.any { matchesFontQuery(it, query) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), false)

    val filteredFontItems: StateFlow<List<FontPreviewItem>> = combine(
        _inputText,
        _searchQuery,
        _selectedCategory,
        favoriteFontIds,
        recentFontIds,
        _copiedFeedbackId
    ) { args ->
        val rawText = args[0] as String
        val query = (args[1] as String).trim().lowercase()
        val category = args[2] as FontCategory
        @Suppress("UNCHECKED_CAST")
        val favs = args[3] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val recents = args[4] as List<String>
        val copiedId = args[5] as String?

        val baseList = when (category) {
            FontCategory.ALL -> FontCatalog.allStyles
            FontCategory.TRENDING -> FontCatalog.allStyles.filter { it.isTrending }
            FontCategory.FAVORITES -> FontCatalog.allStyles.filter { it.id in favs }
            FontCategory.RECENT -> recents.mapNotNull { FontCatalog.findById(it) }
            else -> FontCatalog.allStyles.filter { it.category == category }
        }

        val searched = if (query.isEmpty()) {
            baseList
        } else {
            val directMatches = baseList.filter { matchesFontQuery(it, query) }
            if (directMatches.isNotEmpty() ||
                category == FontCategory.ALL ||
                category == FontCategory.FAVORITES ||
                category == FontCategory.RECENT
            ) {
                directMatches
            } else {
                // Allow searching by another category/style name without getting stuck on an active chip
                FontCatalog.allStyles.filter { matchesFontQuery(it, query) }
            }
        }

        searched.map { spec ->
            val effective = if (rawText.isEmpty() && spec.category == FontCategory.HINDI_CALLIGRAPHY) {
                "हर शब्द • VEDOGRAPHi"
            } else {
                rawText.ifEmpty { "VEDOGRAPHi" }
            }
            FontPreviewItem(
                spec = spec,
                previewText = spec.transform(effective),
                isFavorite = spec.id in favs,
                isRecentlyCopied = copiedId == spec.id
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    // === Bio & Caption Generator State ===
    private val _selectedBioPlatform = MutableStateFlow(BioPlatform.INSTAGRAM)
    val selectedBioPlatform: StateFlow<BioPlatform> = _selectedBioPlatform.asStateFlow()

    private val _selectedBioCategory = MutableStateFlow(BioCategory.ATTITUDE)
    val selectedBioCategory: StateFlow<BioCategory> = _selectedBioCategory.asStateFlow()

    private val _bioHeadline = MutableStateFlow("Official Legend")
    val bioHeadline: StateFlow<String> = _bioHeadline.asStateFlow()

    private val _bioBodyText = MutableStateFlow(
        "Simple by heart, Royal by mindset\nZero ego • 100% Real\nApna Time Khud Banate Hain"
    )
    val bioBodyText: StateFlow<String> = _bioBodyText.asStateFlow()

    private val _selectedBioFontId = MutableStateFlow("sans_bold")
    val selectedBioFontId: StateFlow<String> = _selectedBioFontId.asStateFlow()

    private val _selectedBioDividerIdx = MutableStateFlow(1)
    val selectedBioDividerIdx: StateFlow<Int> = _selectedBioDividerIdx.asStateFlow()

    private val _bioIncludeBullets = MutableStateFlow(true)
    val bioIncludeBullets: StateFlow<Boolean> = _bioIncludeBullets.asStateFlow()

    private var bioCycleIdx = 0

    val generatedBioPreview: StateFlow<String> = combine<Any, String>(
        _bioHeadline,
        _bioBodyText,
        _selectedBioFontId,
        _selectedBioDividerIdx,
        _bioIncludeBullets,
        _selectedBioPlatform
    ) { args ->
        val headline = args[0] as String
        val body = args[1] as String
        val fontId = args[2] as String
        val divIdx = args[3] as Int
        val bullets = args[4] as Boolean
        val platform = args[5] as BioPlatform

        val font = FontCatalog.findById(fontId) ?: StudioGenerators.bioFonts.first()
        val divider = StudioGenerators.bioDividers.getOrElse(divIdx) { StudioGenerators.bioDividers.first() }.second
        val base = StudioGenerators.formatBio(
            headline = headline,
            bodyLines = body.lines(),
            font = font,
            divider = divider,
            includeBullets = bullets
        )
        if (platform == BioPlatform.FACEBOOK && divider.isNotEmpty()) {
            base.replace(divider, "").trim()
        } else {
            base
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), "")

    // === Username Generator State ===
    private val _usernameInput = MutableStateFlow("Vedo")
    val usernameInput: StateFlow<String> = _usernameInput.asStateFlow()

    private val _selectedUsernameVibe = MutableStateFlow(UsernameVibe.ROYAL)
    val selectedUsernameVibe: StateFlow<UsernameVibe> = _selectedUsernameVibe.asStateFlow()

    private val _usernameSeed = MutableStateFlow(0)
    val usernameSeed: StateFlow<Int> = _usernameSeed.asStateFlow()

    val generatedUsernames: StateFlow<List<String>> = combine(
        _usernameInput,
        _selectedUsernameVibe,
        _usernameSeed
    ) { name, vibe, seed ->
        StudioGenerators.generateUsernames(name, vibe, seed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    // === My Text Studio State ===
    private val _studioSelectedTab = MutableStateFlow(0)
    val studioSelectedTab: StateFlow<Int> = _studioSelectedTab.asStateFlow()

    private val _templateTagFilter = MutableStateFlow("All")
    val templateTagFilter: StateFlow<String> = _templateTagFilter.asStateFlow()

    private val _decoratorInput = MutableStateFlow("VEDOGRAPHi • हर शब्द")
    val decoratorInput: StateFlow<String> = _decoratorInput.asStateFlow()

    private val _selectedDecoratorFontId = MutableStateFlow("bold_script")
    val selectedDecoratorFontId: StateFlow<String> = _selectedDecoratorFontId.asStateFlow()

    private val _selectedWingId = MutableStateFlow("maharaja")
    val selectedWingId: StateFlow<String> = _selectedWingId.asStateFlow()

    private val _selectedBorderId = MutableStateFlow("star")
    val selectedBorderId: StateFlow<String> = _selectedBorderId.asStateFlow()

    private val _selectedSeparatorId = MutableStateFlow("space")
    val selectedSeparatorId: StateFlow<String> = _selectedSeparatorId.asStateFlow()

    private val _customFinalOverride = MutableStateFlow<String?>(null)

    val generatedDecoratedOutput: StateFlow<String> = combine(
        _decoratorInput,
        _selectedDecoratorFontId,
        _selectedWingId,
        _selectedBorderId,
        _selectedSeparatorId
    ) { text, fontId, wingId, borderId, sepId ->
        val font = FontCatalog.findById(fontId) ?: FontCatalog.allStyles.first()
        val wing = StudioGenerators.wingPresets.firstOrNull { it.id == wingId } ?: StudioGenerators.wingPresets.first()
        val border = StudioGenerators.borderPresets.firstOrNull { it.id == borderId } ?: StudioGenerators.borderPresets.first()
        val sep = StudioGenerators.separatorPresets.firstOrNull { it.id == sepId } ?: StudioGenerators.separatorPresets.first()
        StudioGenerators.decorateText(text, font, wing, border, sep)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), "")

    val decoratedOutput: StateFlow<String> = combine(
        generatedDecoratedOutput,
        _customFinalOverride
    ) { generated, overrideText ->
        overrideText ?: generated
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), "")

    // Occasion Templates State
    private val _selectedOccasionCategory = MutableStateFlow(OccasionCategory.BIRTHDAY)
    val selectedOccasionCategory: StateFlow<OccasionCategory> = _selectedOccasionCategory.asStateFlow()

    // Stylish Name Designer State
    private val _nameDesignerName = MutableStateFlow("Aarav")
    val nameDesignerName: StateFlow<String> = _nameDesignerName.asStateFlow()

    private val _nameDesignerSubtitle = MutableStateFlow("Official")
    val nameDesignerSubtitle: StateFlow<String> = _nameDesignerSubtitle.asStateFlow()

    private val _nameDesignerFontId = MutableStateFlow("bold_script")
    val nameDesignerFontId: StateFlow<String> = _nameDesignerFontId.asStateFlow()

    private val _nameDesignerWingId = MutableStateFlow("maharaja")
    val nameDesignerWingId: StateFlow<String> = _nameDesignerWingId.asStateFlow()

    private val _nameDesignerBadge = MutableStateFlow("👑")
    val nameDesignerBadge: StateFlow<String> = _nameDesignerBadge.asStateFlow()

    val designedNameOutput: StateFlow<String> = combine(
        _nameDesignerName,
        _nameDesignerSubtitle,
        _nameDesignerFontId,
        _nameDesignerWingId,
        _nameDesignerBadge
    ) { name, sub, fontId, wingId, badge ->
        val font = FontCatalog.findById(fontId) ?: FontCatalog.allStyles.first()
        val wing = StudioGenerators.wingPresets.firstOrNull { it.id == wingId } ?: StudioGenerators.wingPresets.first()
        TextStudioCatalog.formatStylishName(name, sub, font, wing, badge)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), "")

    // WhatsApp Status Designer State
    private val _waHeaderTag = MutableStateFlow("Daily Status")
    val waHeaderTag: StateFlow<String> = _waHeaderTag.asStateFlow()

    private val _waBodyText = MutableStateFlow("अपनी कहानी खुद लिखो,\nदुनिया तो सिर्फ पढ़ेगी!")
    val waBodyText: StateFlow<String> = _waBodyText.asStateFlow()

    private val _waFontId = MutableStateFlow("sans_bold")
    val waFontId: StateFlow<String> = _waFontId.asStateFlow()

    private val _waDividerIdx = MutableStateFlow(1)
    val waDividerIdx: StateFlow<Int> = _waDividerIdx.asStateFlow()

    private val _waShowFooter = MutableStateFlow(true)
    val waShowFooter: StateFlow<Boolean> = _waShowFooter.asStateFlow()

    val designedWaStatusOutput: StateFlow<String> = combine<Any, String>(
        _waHeaderTag,
        _waBodyText,
        _waFontId,
        _waDividerIdx,
        _waShowFooter
    ) { args ->
        val header = args[0] as String
        val body = args[1] as String
        val fontId = args[2] as String
        val divIdx = args[3] as Int
        val footer = args[4] as Boolean
        val font = FontCatalog.findById(fontId) ?: FontCatalog.allStyles.first()
        val divider = StudioGenerators.bioDividers.getOrElse(divIdx) { StudioGenerators.bioDividers.first() }.second
        TextStudioCatalog.formatWhatsAppStatus(header, body, font, divider, footer)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), "")

    // === Actions ===

    fun clearCloudSyncError() {
        _cloudSyncError.value = null
    }

    fun updateInputText(value: String) {
        _inputText.value = value
        scheduleDraftPersistence()
    }

    fun clearInputText() {
        _inputText.value = ""
        scheduleDraftPersistence()
    }

    fun buildCopyAllText(): String {
        return buildCopyAllText(selectedIds = null, includeStyleNames = true)
    }

    fun buildCopyAllText(
        selectedIds: Set<String>?,
        includeStyleNames: Boolean
    ): String {
        val variations = filteredFontItems.value.map {
            BatchCopyVariation(
                id = it.spec.id,
                styleName = it.spec.name,
                styledText = it.previewText,
                isFavorite = it.isFavorite
            )
        }
        return formatBatchCopyText(
            variations = variations,
            selectedIds = selectedIds,
            includeStyleNames = includeStyleNames
        )
    }

    fun formatBatchCopyText(
        variations: List<BatchCopyVariation>,
        selectedIds: Set<String>?,
        includeStyleNames: Boolean
    ): String {
        val target = if (selectedIds == null) {
            variations
        } else {
            variations.filter { it.id in selectedIds }
        }
        if (target.isEmpty()) return ""
        return target.joinToString("\n") { item ->
            if (includeStyleNames) "${item.styleName}: ${item.styledText}" else item.styledText
        }
    }

    fun openBatchCopySheetForFonts() {
        val items = filteredFontItems.value
        if (items.isEmpty()) return
        val source = _inputText.value.ifBlank { "VEDOGRAPHi" }
        _batchCopySheet.value = BatchCopySheetState(
            title = "Batch Copy Font Variations",
            sourceText = source,
            variations = items.map {
                BatchCopyVariation(
                    id = it.spec.id,
                    styleName = it.spec.name,
                    styledText = it.previewText,
                    isFavorite = it.isFavorite
                )
            },
            includeLabelsDefault = true
        )
    }

    fun openBatchCopySheet(
        title: String,
        sourceText: String,
        variations: List<BatchCopyVariation>,
        includeLabelsDefault: Boolean = true
    ) {
        if (variations.isEmpty()) return
        _batchCopySheet.value = BatchCopySheetState(
            title = title,
            sourceText = sourceText,
            variations = variations,
            includeLabelsDefault = includeLabelsDefault
        )
    }

    fun dismissBatchCopySheet() {
        _batchCopySheet.value = null
    }

    fun updateSearchQuery(value: String) {
        _searchQuery.value = value
    }

    fun selectCategory(category: FontCategory) {
        _selectedCategory.value = category
    }

    fun toggleDarkTheme() {
        val next = !isDarkTheme.value
        viewModelScope.launch {
            repo.setDarkTheme(next)
            if (!isGuest && isOnline.value) {
                cloudRepo?.updateDarkTheme(next)?.onFailure { err ->
                    _cloudSyncError.value = err.localizedMessage ?: "Failed to sync theme preference"
                }
            }
        }
    }

    fun toggleFavorite(fontId: String) {
        viewModelScope.launch {
            repo.toggleFavorite(fontId)
            val updatedFavs = repo.favoriteFontIds.first()
            if (!isGuest && isOnline.value) {
                cloudRepo?.updateFavorites(updatedFavs)?.onFailure { err ->
                    _cloudSyncError.value = err.localizedMessage ?: "Failed to sync favorite"
                }
            }
        }
    }

    fun markFontUsed(fontId: String, styledText: String? = null) {
        val spec = FontCatalog.findById(fontId)
        val rawInput = _inputText.value.ifEmpty {
            if (spec?.category == FontCategory.HINDI_CALLIGRAPHY) "हर शब्द • VEDOGRAPHi" else "VEDOGRAPHi"
        }
        val output = styledText ?: spec?.transform(rawInput)
        viewModelScope.launch {
            repo.recordRecent(fontId)
            val updatedLocalRecents = repo.recentFontIds.first()
            val cloudRecents = (cloudProfileState.value as? CloudUiState.Success)?.data?.recentFontIds.orEmpty()
            val mergedRecents = (updatedLocalRecents + cloudRecents).distinct().take(25)
            if (spec != null && !output.isNullOrBlank()) {
                repo.cacheGeneratedStyle(
                    CachedStyleEntry(
                        id = UUID.randomUUID().toString(),
                        title = spec.name,
                        inputText = rawInput,
                        styledText = output,
                        source = CachedEntrySource.FONT,
                        fontId = spec.id
                    )
                )
            }
            if (!isGuest && isOnline.value) {
                cloudRepo?.updateRecents(mergedRecents)
            }
            _copiedFeedbackId.value = fontId
            delay(1500)
            if (_copiedFeedbackId.value == fontId) {
                _copiedFeedbackId.value = null
            }
        }
    }

    fun cacheStyleGeneration(
        title: String,
        inputText: String,
        styledText: String,
        source: CachedEntrySource,
        templateId: String? = null,
        fontId: String? = null,
        wingId: String? = null,
        borderId: String? = null,
        separatorId: String? = null
    ) {
        if (styledText.isBlank()) return
        viewModelScope.launch {
            repo.cacheGeneratedStyle(
                CachedStyleEntry(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    inputText = inputText,
                    styledText = styledText,
                    source = source,
                    templateId = templateId,
                    fontId = fontId,
                    wingId = wingId,
                    borderId = borderId,
                    separatorId = separatorId
                )
            )
        }
    }

    fun recordTemplateUsed(template: DecorationTemplate, renderedOutput: String) {
        cacheStyleGeneration(
            title = template.name,
            inputText = _decoratorInput.value,
            styledText = renderedOutput,
            source = CachedEntrySource.TEMPLATE,
            templateId = template.id,
            fontId = template.fontId,
            wingId = template.wingId,
            borderId = template.borderId,
            separatorId = template.separatorId
        )
    }

    fun recordOccasionUsed(occasion: OccasionTemplate, renderedOutput: String) {
        cacheStyleGeneration(
            title = occasion.title,
            inputText = occasion.text,
            styledText = renderedOutput,
            source = CachedEntrySource.OCCASION,
            templateId = occasion.id,
            fontId = occasion.fontId,
            wingId = occasion.wingId,
            borderId = occasion.borderId,
            separatorId = "space"
        )
    }

    fun removeCachedStyle(id: String) {
        viewModelScope.launch {
            repo.removeCachedStyle(id)
        }
    }

    fun clearCachedStyles() {
        viewModelScope.launch {
            repo.clearCachedStyles()
        }
    }

    fun restoreCachedEntry(entry: CachedStyleEntry): String {
        return when (entry.source) {
            CachedEntrySource.FONT -> {
                if (entry.inputText.isNotBlank() && entry.inputText != "VEDOGRAPHi") {
                    _inputText.value = entry.inputText
                }
                _searchQuery.value = ""
                _selectedCategory.value = FontCategory.ALL
                scheduleDraftPersistence()
                "FONTS"
            }
            CachedEntrySource.TEMPLATE,
            CachedEntrySource.OCCASION,
            CachedEntrySource.DECORATED -> {
                if (entry.inputText.isNotBlank()) {
                    _decoratorInput.value = entry.inputText
                }
                entry.fontId?.let { _selectedDecoratorFontId.value = it }
                entry.wingId?.let { _selectedWingId.value = it }
                entry.borderId?.let { _selectedBorderId.value = it }
                entry.separatorId?.let { _selectedSeparatorId.value = it }
                _customFinalOverride.value = if (entry.source == CachedEntrySource.DECORATED && entry.fontId == null) {
                    entry.styledText
                } else {
                    null
                }
                _studioSelectedTab.value = 1
                scheduleDraftPersistence()
                "STUDIO"
            }
            CachedEntrySource.NAME -> {
                if (entry.inputText.isNotBlank()) _nameDesignerName.value = entry.inputText
                entry.fontId?.let { _nameDesignerFontId.value = it }
                entry.wingId?.let { _nameDesignerWingId.value = it }
                _studioSelectedTab.value = 2
                scheduleDraftPersistence()
                "STUDIO"
            }
            CachedEntrySource.STATUS -> {
                if (entry.inputText.isNotBlank()) _waBodyText.value = entry.inputText
                entry.fontId?.let { _waFontId.value = it }
                _studioSelectedTab.value = 3
                scheduleDraftPersistence()
                "STUDIO"
            }
            CachedEntrySource.BIO -> {
                if (entry.inputText.isNotBlank()) _bioBodyText.value = entry.inputText
                entry.fontId?.let { _selectedBioFontId.value = it }
                scheduleDraftPersistence()
                "BIOS"
            }
            CachedEntrySource.USERNAME -> {
                if (entry.inputText.isNotBlank()) _usernameInput.value = entry.inputText
                scheduleDraftPersistence()
                "USERNAMES"
            }
        }
    }

    fun clearRecents() {
        viewModelScope.launch {
            repo.clearRecents()
            if (!isGuest && isOnline.value) {
                cloudRepo?.updateRecents(emptyList())?.onFailure { err ->
                    _cloudSyncError.value = err.localizedMessage ?: "Failed to clear cloud recents"
                }
            }
        }
    }

    fun openShareSheet(text: String) {
        _shareSheetPayload.value = text
    }

    fun dismissShareSheet() {
        _shareSheetPayload.value = null
    }

    // Bio Actions
    fun selectBioPlatform(platform: BioPlatform) {
        _selectedBioPlatform.value = platform
    }

    fun selectBioCategory(category: BioCategory) {
        _selectedBioCategory.value = category
        bioCycleIdx = 0
        applyBioTemplate(category, 0)
    }

    fun shuffleBioTemplate() {
        bioCycleIdx += 1
        applyBioTemplate(_selectedBioCategory.value, bioCycleIdx)
    }

    private fun applyBioTemplate(category: BioCategory, index: Int) {
        val matching = StudioGenerators.bioTemplates.filter { it.category == category }
        if (matching.isNotEmpty()) {
            val picked = matching[index % matching.size]
            _bioHeadline.value = picked.headline
            _bioBodyText.value = picked.lines.joinToString("\n")
        }
    }

    fun updateBioHeadline(value: String) {
        _bioHeadline.value = value
        scheduleDraftPersistence()
    }

    fun updateBioBodyText(value: String) {
        _bioBodyText.value = value
        scheduleDraftPersistence()
    }

    fun selectBioFont(fontId: String) {
        _selectedBioFontId.value = fontId
    }

    fun selectBioDivider(index: Int) {
        _selectedBioDividerIdx.value = index
    }

    fun toggleBioBullets() {
        _bioIncludeBullets.update { !it }
    }

    // Username Actions
    fun updateUsernameInput(value: String) {
        _usernameInput.value = value
        scheduleDraftPersistence()
    }

    fun selectUsernameVibe(vibe: UsernameVibe) {
        _selectedUsernameVibe.value = vibe
    }

    fun shuffleUsernames() {
        _usernameSeed.update { it + 1 }
    }

    // Studio & Decorator Actions
    fun selectStudioTab(index: Int) {
        _studioSelectedTab.value = index
    }

    fun selectTemplateTagFilter(tag: String) {
        _templateTagFilter.value = tag
    }

    fun updateDecoratorInput(value: String) {
        _decoratorInput.value = value
        _customFinalOverride.value = null
        scheduleDraftPersistence()
    }

    fun insertSymbolIntoDecorator(symbol: String) {
        _decoratorInput.update { current ->
            if (current.isBlank()) symbol else "$current $symbol"
        }
        _customFinalOverride.value = null
        scheduleDraftPersistence()
    }

    fun updateCustomFinalOverride(value: String) {
        _customFinalOverride.value = value
    }

    fun resetCustomFinalOverride() {
        _customFinalOverride.value = null
    }

    fun selectDecoratorFont(fontId: String) {
        _selectedDecoratorFontId.value = fontId
        _customFinalOverride.value = null
        scheduleDraftPersistence()
    }

    fun selectWing(wingId: String) {
        _selectedWingId.value = wingId
        _customFinalOverride.value = null
        scheduleDraftPersistence()
    }

    fun selectBorder(borderId: String) {
        _selectedBorderId.value = borderId
        _customFinalOverride.value = null
        scheduleDraftPersistence()
    }

    fun selectSeparator(sepId: String) {
        _selectedSeparatorId.value = sepId
        _customFinalOverride.value = null
        scheduleDraftPersistence()
    }

    fun applyTemplateToCustomizer(template: DecorationTemplate) {
        _selectedDecoratorFontId.value = template.fontId
        _selectedWingId.value = template.wingId
        _selectedBorderId.value = template.borderId
        _selectedSeparatorId.value = template.separatorId
        _customFinalOverride.value = null
        _studioSelectedTab.value = 1
        recordTemplateUsed(template, template.render(_decoratorInput.value))
        scheduleDraftPersistence()
    }

    fun selectOccasionCategory(category: OccasionCategory) {
        _selectedOccasionCategory.value = category
    }

    fun applyOccasionToCustomizer(occasion: OccasionTemplate) {
        _decoratorInput.value = occasion.text
        _selectedDecoratorFontId.value = occasion.fontId
        _selectedWingId.value = occasion.wingId
        _selectedBorderId.value = occasion.borderId
        _selectedSeparatorId.value = "space"
        _customFinalOverride.value = null
        _studioSelectedTab.value = 1
        val font = FontCatalog.findById(occasion.fontId) ?: FontCatalog.allStyles.first()
        val wing = StudioGenerators.wingPresets.firstOrNull { it.id == occasion.wingId } ?: StudioGenerators.wingPresets.first()
        val border = StudioGenerators.borderPresets.firstOrNull { it.id == occasion.borderId } ?: StudioGenerators.borderPresets.first()
        val sep = StudioGenerators.separatorPresets.first()
        recordOccasionUsed(occasion, StudioGenerators.decorateText(occasion.text, font, wing, border, sep))
        scheduleDraftPersistence()
    }

    // Name Designer Actions
    fun updateNameDesignerName(value: String) {
        _nameDesignerName.value = value
        scheduleDraftPersistence()
    }

    fun updateNameDesignerSubtitle(value: String) {
        _nameDesignerSubtitle.value = value
        scheduleDraftPersistence()
    }

    fun selectNameDesignerFont(fontId: String) {
        _nameDesignerFontId.value = fontId
    }

    fun selectNameDesignerWing(wingId: String) {
        _nameDesignerWingId.value = wingId
    }

    fun selectNameDesignerBadge(badge: String) {
        _nameDesignerBadge.value = badge
    }

    // WhatsApp Status Designer Actions
    fun applyWaMoodPreset(header: String, body: String) {
        _waHeaderTag.value = header
        _waBodyText.value = body
        scheduleDraftPersistence()
    }

    fun updateWaHeaderTag(value: String) {
        _waHeaderTag.value = value
        scheduleDraftPersistence()
    }

    fun updateWaBodyText(value: String) {
        _waBodyText.value = value
        scheduleDraftPersistence()
    }

    fun selectWaFont(fontId: String) {
        _waFontId.value = fontId
    }

    fun selectWaDivider(index: Int) {
        _waDividerIdx.value = index
    }

    fun toggleWaFooter() {
        _waShowFooter.update { !it }
    }

    fun saveCustomCreation(title: String, content: String, kind: String) {
        if (content.isBlank()) return
        val newId = UUID.randomUUID().toString()
        val source = when (kind) {
            "BIO" -> CachedEntrySource.BIO
            "USERNAME" -> CachedEntrySource.USERNAME
            "NAME" -> CachedEntrySource.NAME
            "STATUS" -> CachedEntrySource.STATUS
            "OCCASION" -> CachedEntrySource.OCCASION
            else -> CachedEntrySource.DECORATED
        }
        viewModelScope.launch {
            repo.saveCreation(
                SavedCreation(
                    id = newId,
                    title = title,
                    content = content,
                    kind = kind
                )
            )
            repo.cacheGeneratedStyle(
                CachedStyleEntry(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    inputText = content,
                    styledText = content,
                    source = source
                )
            )
            if (!isGuest && isOnline.value) {
                cloudRepo?.saveCreation(
                    creationId = newId,
                    title = title,
                    content = content,
                    kind = kind
                )?.onFailure { err ->
                    _cloudSyncError.value = err.localizedMessage ?: "Saved locally; cloud sync failed"
                }
            }
        }
    }

    fun deleteSavedCreation(id: String) {
        viewModelScope.launch {
            repo.deleteCreation(id)
            if (!isGuest && isOnline.value) {
                cloudRepo?.deleteCreation(id)?.onFailure { err ->
                    _cloudSyncError.value = err.localizedMessage ?: "Failed to delete from cloud"
                }
            }
        }
    }

    private companion object {
        const val TAG = "VedographiVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
