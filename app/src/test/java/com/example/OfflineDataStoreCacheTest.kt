package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.data.PreferencesRepository
import com.example.domain.CachedEntrySource
import com.example.domain.CachedStyleEntry
import com.example.domain.SavedCreation
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.domain.StudioDraftSnapshot
import com.example.domain.TextStudioCatalog
import com.example.ui.VedographiViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class OfflineDataStoreCacheTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createTestRepository(): Pair<PreferencesRepository, CoroutineScope> {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = tempFolder.newFile("test_vedographi_cache_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file }
        )
        return PreferencesRepository(dataStore) to scope
    }

    @Test
    fun `cacheGeneratedStyle stores and deduplicates Unicode Hindi and special characters losslessly`() = runTest {
        val (repo, scope) = createTestRepository()
        try {
            val tpl = TextStudioCatalog.decorationTemplates.first()
            val hindiInput = "हर शब्द | का अपना = अंदाज़;\n2026"
            val renderedTpl = tpl.render(hindiInput)

            repo.cacheGeneratedStyle(
                CachedStyleEntry(
                    id = "c1",
                    title = tpl.name,
                    inputText = hindiInput,
                    styledText = renderedTpl,
                    source = CachedEntrySource.TEMPLATE,
                    templateId = tpl.id,
                    fontId = tpl.fontId,
                    wingId = tpl.wingId,
                    borderId = tpl.borderId,
                    separatorId = tpl.separatorId,
                    timestampMillis = 1000L
                )
            )
            repo.cacheGeneratedStyle(
                CachedStyleEntry(
                    id = "c2",
                    title = "Bold Script",
                    inputText = "Vedographi",
                    styledText = "𝓥𝓮𝓭𝓸𝓰𝓻𝓪𝓹𝓱𝓲",
                    source = CachedEntrySource.FONT,
                    fontId = "bold_script",
                    timestampMillis = 2000L
                )
            )
            // Re-cache same template output; should deduplicate and move to top
            repo.cacheGeneratedStyle(
                CachedStyleEntry(
                    id = "c3",
                    title = tpl.name,
                    inputText = hindiInput,
                    styledText = renderedTpl,
                    source = CachedEntrySource.TEMPLATE,
                    templateId = tpl.id,
                    fontId = tpl.fontId,
                    wingId = tpl.wingId,
                    borderId = tpl.borderId,
                    separatorId = tpl.separatorId,
                    timestampMillis = 3000L
                )
            )

            val cached = repo.cachedGeneratedStyles.first()
            assertEquals(2, cached.size)
            assertEquals("c3", cached.first().id)
            assertEquals(CachedEntrySource.TEMPLATE, cached.first().source)
            assertEquals(tpl.id, cached.first().templateId)
            assertEquals(hindiInput, cached.first().inputText)
            assertEquals(renderedTpl, cached.first().styledText)
            assertEquals("𝓥𝓮𝓭𝓸𝓰𝓻𝓪𝓹𝓱𝓲", cached[1].styledText)

            repo.removeCachedStyle("c2")
            assertEquals(1, repo.cachedGeneratedStyles.first().size)

            repo.clearCachedStyles()
            assertTrue(repo.cachedGeneratedStyles.first().isEmpty())
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `studioDraftSnapshot persists and restores offline workspace inputs with newlines and symbols`() = runTest {
        val (repo, scope) = createTestRepository()
        try {
            repo.saveStudioDraft {
                StudioDraftSnapshot(
                    fontInputText = "नमस्ते Studio | Royal",
                    decoratorInput = "Royal = King; 100%",
                    decoratorFontId = "serif_bold",
                    decoratorWingId = "maharaja",
                    decoratorBorderId = "star",
                    decoratorSeparatorId = "space",
                    bioHeadline = "Creator",
                    bioBodyText = "Line 1\nLine 2 | Bio",
                    usernameInput = "Aarav",
                    nameDesignerName = "Aarav",
                    nameDesignerSubtitle = "Official",
                    waHeaderTag = "Mood",
                    waBodyText = "Apna time aayega\n🔥"
                )
            }

            val restored = repo.studioDraftSnapshot.first()
            assertEquals("नमस्ते Studio | Royal", restored.fontInputText)
            assertEquals("Royal = King; 100%", restored.decoratorInput)
            assertEquals("maharaja", restored.decoratorWingId)
            assertEquals("Line 1\nLine 2 | Bio", restored.bioBodyText)
            assertEquals("Apna time aayega\n🔥", restored.waBodyText)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `read-through cloud mirroring caches profile and merges offline creations`() = runTest {
        val (repo, scope) = createTestRepository()
        try {
            val offlineItem = SavedCreation("offline_1", "Offline Bio", "★彡 Offline\nLine 2 彡★", "BIO")
            repo.saveCreation(offlineItem)

            val cloudItem = SavedCreation("cloud_1", "Cloud Name", "꧁👑 𝓡𝓸𝔂𝓪𝓵 👑꧂", "NAME")
            repo.cacheCloudProfile(
                darkTheme = false,
                favoriteIds = setOf("bold_script", "sans_bold"),
                recentIds = listOf("bold_script"),
                syncedAtMillis = 123456L
            )
            repo.cacheCloudCreations(listOf(cloudItem), syncedAtMillis = 123456L)

            assertFalse(repo.isDarkTheme.first())
            assertEquals(setOf("bold_script", "sans_bold"), repo.favoriteFontIds.first())
            assertEquals(listOf("bold_script"), repo.recentFontIds.first())
            assertEquals(123456L, repo.lastCloudCacheSyncMillis.first())

            val creations = repo.savedCreations.first()
            assertEquals(2, creations.size)
            assertTrue(creations.any { it.id == "offline_1" && it.content == "★彡 Offline\nLine 2 彡★" })
            assertTrue(creations.any { it.id == "cloud_1" })
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `guestModeEnabled persists and clearAllUserData wipes all user records without clearing theme or guest flag`() = runTest {
        val (repo, scope) = createTestRepository()
        try {
            assertFalse(repo.guestModeEnabled.first())
            repo.setGuestMode(true)
            assertTrue(repo.guestModeEnabled.first())

            repo.setDarkTheme(false)
            repo.toggleFavorite("bold_script")
            repo.recordRecent("bold_script")
            repo.saveCreation(SavedCreation("g1", "Guest Bio", "★ Guest ★", "BIO"))
            repo.cacheGeneratedStyle(
                CachedStyleEntry(
                    id = "gs1",
                    title = "Bold Script",
                    inputText = "Hi",
                    styledText = "𝓗𝓲",
                    source = CachedEntrySource.FONT,
                    fontId = "bold_script"
                )
            )
            repo.saveStudioDraft { it.copy(fontInputText = "Draft Text") }

            repo.clearAllUserData()

            assertTrue(repo.favoriteFontIds.first().isEmpty())
            assertTrue(repo.recentFontIds.first().isEmpty())
            assertTrue(repo.savedCreations.first().isEmpty())
            assertTrue(repo.cachedGeneratedStyles.first().isEmpty())
            assertNull(repo.studioDraftSnapshot.first().fontInputText)
            assertEquals(0L, repo.lastCloudCacheSyncMillis.first())
            // Theme and guest session choice remain preserved
            assertFalse(repo.isDarkTheme.first())
            assertTrue(repo.guestModeEnabled.first())
        } finally {
            scope.cancel()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `VedographiViewModel operates cleanly in Guest Mode with null cloudRepo and supports clearLocalData`() {
        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)
        val store = ViewModelStore()
        try {
            runTest(dispatcher) {
                val file = tempFolder.newFile("test_vm_guest_${System.nanoTime()}.preferences_pb")
                val dataStore = PreferenceDataStoreFactory.create(
                    scope = backgroundScope,
                    produceFile = { file }
                )
                val repo = PreferencesRepository(dataStore)
                val factory = viewModelFactory {
                    initializer {
                        VedographiViewModel(
                            repo = repo,
                            cloudRepo = null,
                            currentUserId = null,
                            userDisplayName = null,
                            networkOnlineFlow = flowOf(false)
                        )
                    }
                }
                val vm = ViewModelProvider(store, factory)[VedographiViewModel::class.java]
                advanceUntilIdle()

                assertTrue(vm.isGuest)
                assertFalse(vm.isCloudSynced.value)

                vm.toggleFavorite("sans_bold")
                vm.saveCustomCreation("Guest Name", "꧁ 𝓐𝓪𝓻𝓪𝓿 ꧂", "NAME")
                advanceUntilIdle()

                assertTrue("sans_bold" in repo.favoriteFontIds.first())
                assertEquals(1, repo.savedCreations.first().size)
                assertEquals(1, repo.cachedGeneratedStyles.first().size)

                var backupMessage = ""
                vm.backupNow { backupMessage = it }
                advanceUntilIdle()
                assertTrue(backupMessage.contains("Guest Mode"))

                var clearedMessage = ""
                vm.clearLocalData { clearedMessage = it }
                advanceUntilIdle()
                assertTrue(clearedMessage.contains("cleared"))
                assertTrue(repo.favoriteFontIds.first().isEmpty())
                assertTrue(repo.savedCreations.first().isEmpty())
                assertTrue(repo.cachedGeneratedStyles.first().isEmpty())
                store.clear()
            }
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
