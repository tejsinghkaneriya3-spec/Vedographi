package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.PreferencesRepository
import com.example.domain.FontCatalog
import com.example.domain.FontCategory
import com.example.domain.SavedCreation
import com.example.domain.StudioGenerators
import com.example.domain.UnicodeFontEngine
import com.example.ui.BatchCopyVariation
import com.example.ui.VedographiViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class VedographiAuditTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `all 220 styles transform custom user input dynamically without leaving raw English untouched`() {
        val customInput = "Vedographi Audit 2026"
        val styles = FontCatalog.allStyles
        assertTrue("Expected >= 200 styles, got ${styles.size}", styles.size >= 200)

        var transformedCount = 0
        styles.forEach { spec ->
            val output = spec.transform(customInput)
            assertTrue("Empty output for ${spec.id}", output.isNotBlank())
            if (output != customInput) {
                transformedCount++
            }
            // Verify changing user text changes output dynamically
            val otherOutput = spec.transform("Other Text 99")
            assertNotEquals("Style ${spec.id} did not react dynamically to input", output, otherOutput)
        }
        assertEquals(styles.size, transformedCount)
    }

    @Test
    fun `search and category filters match expected fonts across all 11 categories`() {
        val categories = listOf(
            FontCategory.HINDI_CALLIGRAPHY,
            FontCategory.STYLISH,
            FontCategory.BOLD,
            FontCategory.CURSIVE,
            FontCategory.GOTHIC,
            FontCategory.SERIF,
            FontCategory.BUBBLE,
            FontCategory.SMALL_CAPS,
            FontCategory.DOUBLE_STRUCK,
            FontCategory.DECORATIVE,
            FontCategory.FANCY
        )
        categories.forEach { cat ->
            val matching = FontCatalog.allStyles.filter { it.category == cat }
            assertTrue("Category ${cat.label} has too few fonts", matching.size >= 20)
        }

        val gothicSearch = FontCatalog.allStyles.filter {
            it.name.lowercase().contains("gothic") ||
                it.category.label.lowercase().contains("gothic") ||
                it.id.lowercase().contains("gothic")
        }
        assertTrue(gothicSearch.size >= 20)

        val frakturIdSearch = FontCatalog.allStyles.filter {
            it.id.lowercase().contains("fraktur")
        }
        assertTrue(frakturIdSearch.isNotEmpty())
    }

    @Test
    fun `hindi devanagari text including matras and conjuncts remains intact across all 220 styles`() {
        val hindiSentence = "हर शब्द का अपना अंदाज़! नमस्ते भारत"
        assertTrue(UnicodeFontEngine.containsHindi(hindiSentence))

        FontCatalog.allStyles.forEach { spec ->
            val out = spec.transform(hindiSentence)
            if (spec.category == FontCategory.HINDI_CALLIGRAPHY) {
                assertTrue("Broken conjunct 'ब्द' in ${spec.id}: $out", out.contains("ब्द"))
                assertTrue("Broken akshara 'अं'/'दा'/'ज़' in ${spec.id}: $out", out.contains("अं") && out.contains("दा") && out.contains("ज़"))
                assertTrue("Broken conjunct 'स्ते' in ${spec.id}: $out", out.contains("स्ते"))
            } else {
                // Every Devanagari word must remain intact without corruption
                assertTrue("Corrupted 'हर शब्द' in ${spec.id}: $out", out.contains("हर शब्द"))
                assertTrue("Corrupted 'अंदाज़' in ${spec.id}: $out", out.contains("अंदाज़"))
                assertTrue("Corrupted 'नमस्ते भारत' in ${spec.id}: $out", out.contains("नमस्ते भारत"))
            }
        }
    }

    @Test
    fun `copy all output contains every filtered style and transformed preview`() {
        val input = "Hello भारत"
        val items = FontCatalog.allStyles.take(10)
        val copyAll = items.joinToString("\n") { "${it.name}: ${it.transform(input)}" }
        val lines = copyAll.lines()
        assertEquals(10, lines.size)
        lines.forEach { line ->
            assertTrue(line.contains("भारत"))
        }
    }

    @Test
    fun `favorites, recents, saved creations, and theme persist locally in DataStore`() = runTest {
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tempFolder.newFile("vedographi_audit_prefs.preferences_pb") }
        )
        val repo = PreferencesRepository(testDataStore)

        // Favorites toggle & persistence
        assertTrue(repo.favoriteFontIds.first().isEmpty())
        repo.toggleFavorite("sans_bold")
        repo.toggleFavorite("bold_script")
        assertEquals(setOf("sans_bold", "bold_script"), repo.favoriteFontIds.first())
        repo.toggleFavorite("sans_bold")
        assertEquals(setOf("bold_script"), repo.favoriteFontIds.first())

        // Recents ordering, deduplication & clear
        repo.recordRecent("fraktur_gothic")
        repo.recordRecent("double_struck")
        repo.recordRecent("fraktur_gothic")
        assertEquals(listOf("fraktur_gothic", "double_struck"), repo.recentFontIds.first())
        repo.clearRecents()
        assertTrue(repo.recentFontIds.first().isEmpty())

        // Saved creations save & delete
        val creation = SavedCreation("id1", "Bio", "✦ 𝐇𝐞𝐥𝐥𝐨 ✦", "BIO")
        repo.saveCreation(creation)
        assertEquals(1, repo.savedCreations.first().size)
        repo.deleteCreation("id1")
        assertTrue(repo.savedCreations.first().isEmpty())

        // Theme persistence
        assertTrue(repo.isDarkTheme.first())
        repo.setDarkTheme(false)
        assertFalse(repo.isDarkTheme.first())
    }

    @Test
    fun `bio and decorator generators preserve Hindi while styling English`() {
        val font = FontCatalog.findById("sans_bold")!!
        val bio = StudioGenerators.formatBio(
            headline = "VEDOGRAPHi",
            bodyLines = listOf("हर शब्द का अपना अंदाज़!"),
            font = font,
            divider = "━━━━━━✦✗✦━━━━━━",
            includeBullets = true
        )
        assertTrue(bio.contains("हर शब्द का अपना अंदाज़!"))
        assertTrue(bio.contains(UnicodeFontEngine.sansBold("VEDOGRAPHi")))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `search query filters fonts by name, style category, and falls back across categories`() {
        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)
        val store = ViewModelStore()
        try {
            runTest(dispatcher) {
                val testDataStore = PreferenceDataStoreFactory.create(
                    scope = backgroundScope,
                    produceFile = { tempFolder.newFile("vedographi_search_prefs.preferences_pb") }
                )
                val factory = viewModelFactory {
                    initializer {
                        VedographiViewModel(repo = PreferencesRepository(testDataStore))
                    }
                }
                val vm = ViewModelProvider(store, factory)[VedographiViewModel::class.java]
                val collectJob = backgroundScope.launch(dispatcher) {
                    vm.filteredFontItems.collect { }
                }
                val fallbackJob = backgroundScope.launch(dispatcher) {
                    vm.isSearchExpandedAcrossAll.collect { }
                }
                advanceUntilIdle()

                // 1. Filter by style category name
                vm.updateSearchQuery("Hindi Calligraphy")
                val hindiResults = vm.filteredFontItems.value
                assertTrue(hindiResults.size >= 20)
                assertTrue(hindiResults.all { it.spec.category == FontCategory.HINDI_CALLIGRAPHY })

                // 2. Filter by multi-word font name
                vm.updateSearchQuery("Kalash Royal")
                val kalashResults = vm.filteredFontItems.value
                assertTrue(kalashResults.any { it.spec.id == "hindi_kalash_royal" })

                // 3. Cross-category fallback when another category chip is active
                vm.selectCategory(FontCategory.BOLD)
                vm.updateSearchQuery("Gothic")
                val gothicFallbackResults = vm.filteredFontItems.value
                assertTrue(gothicFallbackResults.isNotEmpty())
                assertTrue(gothicFallbackResults.all { it.spec.category == FontCategory.GOTHIC })
                assertTrue(vm.isSearchExpandedAcrossAll.value)

                // 4. Clearing search restores selected category list
                vm.updateSearchQuery("")
                assertFalse(vm.isSearchExpandedAcrossAll.value)
                assertTrue(vm.filteredFontItems.value.all { it.spec.category == FontCategory.BOLD })

                collectJob.cancel()
                fallbackJob.cancel()
            }
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `batch copy all action builds multi-variation clipboard output with selection and label options`() {
        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)
        val store = ViewModelStore()
        try {
            runTest(dispatcher) {
                val testDataStore = PreferenceDataStoreFactory.create(
                    scope = backgroundScope,
                    produceFile = { tempFolder.newFile("vedographi_batch_copy_prefs.preferences_pb") }
                )
                val factory = viewModelFactory {
                    initializer {
                        VedographiViewModel(repo = PreferencesRepository(testDataStore))
                    }
                }
                val vm = ViewModelProvider(store, factory)[VedographiViewModel::class.java]
                val collectJob = backgroundScope.launch(dispatcher) {
                    vm.filteredFontItems.collect { }
                }
                advanceUntilIdle()

                vm.updateInputText("Vedographi Batch")
                vm.selectCategory(FontCategory.BOLD)
                advanceUntilIdle()

                // Open batch copy sheet for active category
                vm.openBatchCopySheetForFonts()
                val sheet = vm.batchCopySheet.value
                assertTrue(sheet != null && sheet.variations.size >= 20)
                assertEquals("Vedographi Batch", sheet!!.sourceText)

                // Subset selection with style labels ON
                val firstTwoIds = sheet.variations.take(2).map { it.id }.toSet()
                val withLabels = vm.buildCopyAllText(selectedIds = firstTwoIds, includeStyleNames = true)
                val labeledLines = withLabels.lines()
                assertEquals(2, labeledLines.size)
                assertTrue(labeledLines[0].startsWith("${sheet.variations[0].styleName}: "))

                // Subset selection with style labels OFF (pure variations)
                val cleanOnly = vm.buildCopyAllText(selectedIds = firstTwoIds, includeStyleNames = false)
                val cleanLines = cleanOnly.lines()
                assertEquals(2, cleanLines.size)
                assertEquals(sheet.variations[0].styledText, cleanLines[0])
                assertEquals(sheet.variations[1].styledText, cleanLines[1])

                // Generic variations (e.g., usernames/templates) formatting & sheet dismiss
                val customBatch = listOf(
                    BatchCopyVariation("u1", "Royal #1", "♛ Vedo ♛"),
                    BatchCopyVariation("u2", "Royal #2", "★彡 Vedo 彡★")
                )
                assertEquals(
                    "♛ Vedo ♛\n★彡 Vedo 彡★",
                    vm.formatBatchCopyText(customBatch, selectedIds = setOf("u1", "u2"), includeStyleNames = false)
                )
                assertEquals("", vm.formatBatchCopyText(customBatch, selectedIds = emptySet(), includeStyleNames = true))

                vm.dismissBatchCopySheet()
                assertEquals(null, vm.batchCopySheet.value)

                collectJob.cancel()
            }
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
