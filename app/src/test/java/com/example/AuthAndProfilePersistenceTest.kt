package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.DeleteAuthAccountResult
import com.example.data.CloudSavedCreation
import com.example.data.CloudSyncGateway
import com.example.data.CloudUserProfile
import com.example.data.PreferencesRepository
import com.example.domain.CachedEntrySource
import com.example.domain.CachedStyleEntry
import com.example.domain.SavedCreation
import com.example.ui.AccountDeletionState
import com.example.ui.VedographiViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AuthAndProfilePersistenceTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private class FakeCloudSyncGateway(
        initialProfiles: Map<String, CloudUserProfile> = emptyMap(),
        initialCreations: Map<String, List<CloudSavedCreation>> = emptyMap()
    ) : CloudSyncGateway {
        private val profilesFlow = MutableStateFlow(initialProfiles)
        private val creationsFlow = MutableStateFlow(initialCreations)
        var ensureCalls = 0
        var savedCreationsCount = 0
        var activeUid: String? = null

        override fun observeUserProfile(userId: String): Flow<CloudUserProfile?> =
            profilesFlow.map { it[userId] }

        override fun observeCreations(userId: String): Flow<List<CloudSavedCreation>> =
            creationsFlow.map { it[userId].orEmpty() }

        override suspend fun fetchCreationsOnce(userId: String): Result<List<CloudSavedCreation>> =
            Result.success(creationsFlow.value[userId].orEmpty())

        override suspend fun ensureUserProfile(
            displayName: String?,
            darkTheme: Boolean,
            initialFavorites: Set<String>,
            initialRecents: List<String>
        ): Result<CloudUserProfile> {
            ensureCalls++
            val uid = activeUid ?: return Result.failure(IllegalStateException("Not signed in"))
            val existing = profilesFlow.value[uid]
            val cleanName = displayName?.trim()?.takeIf { it.isNotEmpty() }
            val result = if (existing == null) {
                CloudUserProfile(
                    userId = uid,
                    displayName = cleanName,
                    darkTheme = darkTheme,
                    favoriteFontIds = initialFavorites.toList(),
                    recentFontIds = initialRecents.distinct().take(25)
                )
            } else {
                val mergedFavs = (existing.favoriteFontIds + initialFavorites).distinct()
                val mergedRecents = (initialRecents + existing.recentFontIds).distinct().take(25)
                existing.copy(
                    displayName = cleanName ?: existing.displayName,
                    favoriteFontIds = mergedFavs,
                    recentFontIds = mergedRecents
                )
            }
            profilesFlow.value = profilesFlow.value + (uid to result)
            return Result.success(result)
        }

        override suspend fun updateFavorites(favoriteIds: Set<String>): Result<Unit> {
            val uid = activeUid ?: return Result.failure(IllegalStateException("Not signed in"))
            val existing = profilesFlow.value[uid] ?: CloudUserProfile(userId = uid)
            profilesFlow.value = profilesFlow.value + (uid to existing.copy(favoriteFontIds = favoriteIds.toList()))
            return Result.success(Unit)
        }

        override suspend fun updateRecents(recentIds: List<String>): Result<Unit> {
            val uid = activeUid ?: return Result.failure(IllegalStateException("Not signed in"))
            val existing = profilesFlow.value[uid] ?: CloudUserProfile(userId = uid)
            profilesFlow.value = profilesFlow.value + (uid to existing.copy(recentFontIds = recentIds.take(25)))
            return Result.success(Unit)
        }

        override suspend fun updateDarkTheme(darkTheme: Boolean): Result<Unit> {
            val uid = activeUid ?: return Result.failure(IllegalStateException("Not signed in"))
            val existing = profilesFlow.value[uid] ?: CloudUserProfile(userId = uid)
            profilesFlow.value = profilesFlow.value + (uid to existing.copy(darkTheme = darkTheme))
            return Result.success(Unit)
        }

        override suspend fun saveCreation(
            creationId: String,
            title: String,
            content: String,
            kind: String
        ): Result<Unit> {
            val uid = activeUid ?: return Result.failure(IllegalStateException("Not signed in"))
            savedCreationsCount++
            val list = creationsFlow.value[uid].orEmpty().filterNot { it.id == creationId }
            val added = CloudSavedCreation(
                id = creationId,
                userId = uid,
                title = title,
                content = content,
                kind = kind
            )
            creationsFlow.value = creationsFlow.value + (uid to (listOf(added) + list))
            return Result.success(Unit)
        }

        override suspend fun deleteCreation(creationId: String): Result<Unit> {
            val uid = activeUid ?: return Result.failure(IllegalStateException("Not signed in"))
            val list = creationsFlow.value[uid].orEmpty().filterNot { it.id == creationId }
            creationsFlow.value = creationsFlow.value + (uid to list)
            return Result.success(Unit)
        }

        override suspend fun deleteAllUserCloudData(): Result<Unit> {
            val uid = activeUid ?: return Result.failure(IllegalStateException("Not signed in"))
            profilesFlow.value = profilesFlow.value - uid
            creationsFlow.value = creationsFlow.value - uid
            return Result.success(Unit)
        }

        fun currentProfile(uid: String): CloudUserProfile? = profilesFlow.value[uid]
        fun currentCreations(uid: String): List<CloudSavedCreation> = creationsFlow.value[uid].orEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `guest local data merges cleanly into existing cloud profile on Google sign-in without duplicating creations`() {
        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)
        val store = ViewModelStore()
        try {
            runTest(dispatcher) {
                val file = tempFolder.newFile("test_guest_to_cloud_${System.nanoTime()}.preferences_pb")
                val dataStore = PreferenceDataStoreFactory.create(
                    scope = backgroundScope,
                    produceFile = { file }
                )
                val repo = PreferencesRepository(dataStore)
                // Simulate Guest Mode work prior to signing in
                repo.setGuestMode(true)
                repo.toggleFavorite("fraktur_gothic")
                repo.recordRecent("fraktur_gothic")
                repo.saveCreation(SavedCreation("guest_c1", "Guest Bio", "★ Guest Text ★", "BIO"))

                val existingCloudProfile = CloudUserProfile(
                    userId = "uid_alice",
                    displayName = "Alice Cloud",
                    darkTheme = true,
                    favoriteFontIds = listOf("sans_bold"),
                    recentFontIds = listOf("sans_bold")
                )
                val existingCloudCreation = CloudSavedCreation(
                    id = "cloud_c1",
                    userId = "uid_alice",
                    title = "Cloud Bio",
                    content = "✦ Cloud Text ✦",
                    kind = "BIO"
                )
                val fakeCloud = FakeCloudSyncGateway(
                    initialProfiles = mapOf("uid_alice" to existingCloudProfile),
                    initialCreations = mapOf("uid_alice" to listOf(existingCloudCreation))
                ).apply { activeUid = "uid_alice" }

                val factory = viewModelFactory {
                    initializer {
                        VedographiViewModel(
                            repo = repo,
                            cloudRepo = fakeCloud,
                            currentUserId = "uid_alice",
                            userDisplayName = null,
                            networkOnlineFlow = MutableStateFlow(true)
                        )
                    }
                }
                val vm = ViewModelProvider(store, factory)[VedographiViewModel::class.java]
                advanceUntilIdle()

                // Merged profile should retain both cloud and guest favorites/recents
                val cloudAfter = fakeCloud.currentProfile("uid_alice")!!
                assertEquals(setOf("sans_bold", "fraktur_gothic"), cloudAfter.favoriteFontIds.toSet())
                assertEquals(listOf("fraktur_gothic", "sans_bold"), cloudAfter.recentFontIds)
                assertEquals("Alice Cloud", cloudAfter.displayName)
                // DataStore should also mirror the merged profile & clear guest mode
                assertFalse(repo.guestModeEnabled.first())
                assertEquals(setOf("sans_bold", "fraktur_gothic"), repo.favoriteFontIds.first())
                // Only guest_c1 should be uploaded; cloud_c1 must NOT be re-uploaded
                assertEquals(1, fakeCloud.savedCreationsCount)
                assertEquals(2, fakeCloud.currentCreations("uid_alice").size)
                // Effective display name should fall back to Firestore displayName when auth displayName is null
                assertEquals("Alice Cloud", vm.effectiveDisplayName.first())

                store.clear()
            }
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `prepareSessionForUser isolates different Google accounts while preserving Guest migration`() = runTest {
        val file = tempFolder.newFile("test_multi_account_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { file }
        )
        val repo = PreferencesRepository(dataStore)

        // 1. Guest creates data -> signing into Alice preserves it
        repo.setGuestMode(true)
        repo.toggleFavorite("bold_script")
        repo.saveCreation(SavedCreation("c_guest", "Guest", "★ Guest ★", "NAME"))
        repo.prepareSessionForUser("uid_alice")
        assertEquals(setOf("bold_script"), repo.favoriteFontIds.first())
        assertEquals(1, repo.savedCreations.first().size)
        assertFalse(repo.guestModeEnabled.first())

        // 2. Switching to Bob (without explicit sign-out) wipes Alice's data cleanly
        repo.prepareSessionForUser("uid_bob")
        assertTrue(repo.favoriteFontIds.first().isEmpty())
        assertTrue(repo.savedCreations.first().isEmpty())

        // 3. Explicit sign-out wipes Bob's cached data while preserving theme
        repo.setDarkTheme(false)
        repo.toggleFavorite("double_struck")
        repo.cacheGeneratedStyle(
            CachedStyleEntry(
                id = "cs1",
                title = "Double Struck",
                inputText = "Hi",
                styledText = "ℍ𝕚",
                source = CachedEntrySource.FONT
            )
        )
        repo.clearSessionOnSignOut()
        assertTrue(repo.favoriteFontIds.first().isEmpty())
        assertTrue(repo.cachedGeneratedStyles.first().isEmpty())
        assertFalse(repo.isDarkTheme.first())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `offline-to-online transition syncs local updates and account deletion clears session`() {
        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)
        val store = ViewModelStore()
        try {
            runTest(dispatcher) {
                val file = tempFolder.newFile("test_offline_online_${System.nanoTime()}.preferences_pb")
                val dataStore = PreferenceDataStoreFactory.create(
                    scope = backgroundScope,
                    produceFile = { file }
                )
                val repo = PreferencesRepository(dataStore)
                val onlineFlow = MutableStateFlow(false)
                val fakeCloud = FakeCloudSyncGateway().apply { activeUid = "uid_carol" }

                val factory = viewModelFactory {
                    initializer {
                        VedographiViewModel(
                            repo = repo,
                            cloudRepo = fakeCloud,
                            currentUserId = "uid_carol",
                            userDisplayName = "Carol Creator",
                            networkOnlineFlow = onlineFlow
                        )
                    }
                }
                val vm = ViewModelProvider(store, factory)[VedographiViewModel::class.java]
                advanceUntilIdle()

                // Start offline: profile isn't created yet; user toggles favorite & saves creation offline
                assertEquals(0, fakeCloud.ensureCalls)
                vm.toggleFavorite("sans_bold")
                vm.saveCustomCreation("Offline Name", "♛ 𝒞𝒶𝓇𝑜𝓁 ♛", "NAME")
                advanceUntilIdle()
                assertTrue("sans_bold" in repo.favoriteFontIds.first())

                // Come back online: auto-sync uploads profile + offline creation
                onlineFlow.value = true
                advanceUntilIdle()

                assertEquals(1, fakeCloud.ensureCalls)
                val carolProfile = fakeCloud.currentProfile("uid_carol")!!
                assertEquals("Carol Creator", carolProfile.displayName)
                assertTrue("sans_bold" in carolProfile.favoriteFontIds)
                assertEquals(1, fakeCloud.currentCreations("uid_carol").size)

                // Delete cloud account & verify DataStore + cloud data wiped cleanly
                var postDeleteCalled = false
                vm.deleteCloudAccountAndData(
                    onDeleteAuthUser = { DeleteAuthAccountResult.Success },
                    onPostDeleteCleanup = { postDeleteCalled = true }
                )
                advanceUntilIdle()

                assertTrue(postDeleteCalled)
                assertEquals(AccountDeletionState.Completed, vm.accountDeletionState.value)
                assertTrue(repo.favoriteFontIds.first().isEmpty())
                assertTrue(repo.savedCreations.first().isEmpty())
                assertEquals(null, fakeCloud.currentProfile("uid_carol"))
                assertTrue(fakeCloud.currentCreations("uid_carol").isEmpty())

                store.clear()
            }
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
