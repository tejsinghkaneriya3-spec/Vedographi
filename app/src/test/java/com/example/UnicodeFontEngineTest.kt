package com.example

import com.example.domain.FontCatalog
import com.example.domain.FontCategory
import com.example.domain.StudioGenerators
import com.example.domain.UnicodeFontEngine
import com.example.domain.UsernameVibe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnicodeFontEngineTest {

    @Test
    fun `catalog has at least 200 unique font styles`() {
        val styles = FontCatalog.allStyles
        assertTrue("Expected >= 200 fonts, got ${styles.size}", styles.size >= 200)
        val uniqueIds = styles.map { it.id }.toSet()
        assertEquals(styles.size, uniqueIds.size)
    }

    @Test
    fun `all 11 requested font categories are populated`() {
        val requiredCategories = listOf(
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
        requiredCategories.forEach { category ->
            val count = FontCatalog.allStyles.count { it.category == category }
            assertTrue("Category ${category.label} has no styles", count >= 15)
        }
    }

    @Test
    fun `every font style dynamically transforms user entered English text`() {
        val sampleA = "Hello World"
        val sampleB = "Vedographi"
        FontCatalog.allStyles.forEach { spec ->
            val outA = spec.transform(sampleA)
            val outB = spec.transform(sampleB)
            assertNotEquals("Style ${spec.id} ignored user text", outA, outB)
            assertTrue("Style ${spec.id} produced blank output", outA.isNotBlank())
        }
    }

    @Test
    fun `pure Hindi text is preserved intact without corruption across all styles`() {
        val hindi = "हर शब्द का अपना अंदाज़"
        assertTrue(UnicodeFontEngine.containsHindi(hindi))

        FontCatalog.allStyles.forEach { spec ->
            val transformed = spec.transform(hindi)
            assertTrue(
                "Style ${spec.id} corrupted Hindi text: $transformed",
                transformed.contains(hindi)
            )
        }
    }

    @Test
    fun `mixed Hinglish transforms English while preserving Hindi`() {
        val mixed = "नमस्ते VEDOGRAPHi 2026"
        val bold = UnicodeFontEngine.sansBold(mixed)
        assertTrue(bold.startsWith("नमस्ते "))
        assertFalse(bold.contains("VEDOGRAPHi"))
        assertTrue(UnicodeFontEngine.containsHindi(bold))

        val strikethrough = UnicodeFontEngine.combineAscii(mixed, 0x0336)
        assertTrue(strikethrough.startsWith("नमस्ते "))
    }

    @Test
    fun `upside down flips English safely while keeping Hindi readable`() {
        val mixed = "नमस्ते abc"
        val flipped = UnicodeFontEngine.upsideDownSafe(mixed)
        assertTrue(flipped.startsWith("नमस्ते "))
        assertTrue(flipped.contains("ɔqɐ"))
    }

    @Test
    fun `username generator creates varied non-empty handles`() {
        val names = StudioGenerators.generateUsernames("Vedo", UsernameVibe.ROYAL, seed = 1)
        assertTrue(names.size >= 24)
        assertTrue(names.all { it.isNotBlank() })
    }

    @Test
    fun `hindi calligraphy category includes 20+ Unicode styles with Devanagari numerals and paired presets`() {
        val hindiStyles = FontCatalog.allStyles.filter { it.category == FontCategory.HINDI_CALLIGRAPHY }
        assertTrue("Expected >= 20 Hindi Calligraphy styles, got ${hindiStyles.size}", hindiStyles.size >= 20)
        hindiStyles.forEach { spec ->
            assertTrue("Missing calligraphyPresetId for ${spec.id}", !spec.calligraphyPresetId.isNullOrBlank())
            val out = spec.transform("नमस्ते 2026 Vedo")
            assertTrue("Expected Hindi text preserved in ${spec.id}: $out", out.contains("नमस्ते"))
            assertTrue("Expected Devanagari numerals in ${spec.id}: $out", out.contains("२०२६"))
        }
    }
}
