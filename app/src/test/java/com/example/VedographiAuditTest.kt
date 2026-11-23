package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.data.PreferencesRepository
import com.example.domain.FontCatalog
import com.example.domain.FontCategory
import com.example.domain.SavedCreation
import com.example.domain.StudioGenerators
import com.example.domain.UnicodeFontEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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
    fun `search and category filters match expected fonts across all 10 categories`() {
        val categories = listOf(
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
            // Every Devanagari word must remain intact without corruption
            assertTrue("Corrupted 'हर शब्द' in ${spec.id}: $out", out.contains("हर शब्द"))
            assertTrue("Corrupted 'अंदाज़' in ${spec.id}: $out", out.contains("अंदाज़"))
            assertTrue("Corrupted 'नमस्ते भारत' in ${spec.id}: $out", out.contains("नमस्ते भारत"))
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
}
