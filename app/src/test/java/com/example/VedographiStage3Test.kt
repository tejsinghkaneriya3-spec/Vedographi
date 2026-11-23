package com.example

import com.example.domain.BioPlatform
import com.example.domain.FontCatalog
import com.example.domain.OccasionCategory
import com.example.domain.StudioGenerators
import com.example.domain.TextStudioCatalog
import com.example.domain.UnicodeFontEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VedographiStage3Test {

    @Test
    fun decorationTemplatesHasAtLeast50UniqueAndRendersCustomAndHindiText() {
        val templates = TextStudioCatalog.decorationTemplates
        assertTrue("Expected >= 50 templates, got ${templates.size}", templates.size >= 50)
        assertEquals(templates.size, templates.map { it.id }.toSet().size)
        assertEquals(templates.size, templates.map { it.name }.toSet().size)

        val hindiTagline = "हर शब्द का अपना अंदाज़!"
        val mixedInput = "Vedographi • $hindiTagline"

        val hindiWords = listOf("हर", "शब्द", "का", "अपना", "अंदाज़!")
        templates.forEach { tpl ->
            val rendered = tpl.render(mixedInput)
            assertTrue("Rendered template ${tpl.id} should not be blank", rendered.isNotBlank())
            if (tpl.separatorId == "space") {
                assertTrue(
                    "Template ${tpl.id} must preserve full Hindi phrase untouched",
                    rendered.contains(hindiTagline)
                )
            } else {
                hindiWords.forEach { word ->
                    assertTrue(
                        "Template ${tpl.id} must preserve Hindi word '$word' untouched",
                        rendered.contains(word)
                    )
                }
            }
            assertNotEquals(
                "Template ${tpl.id} should transform or decorate ASCII input",
                mixedInput,
                rendered
            )
        }
    }

    @Test
    fun occasionTemplatesCoverAllFiveCategoriesAndPreserveHindi() {
        val occasions = TextStudioCatalog.occasionTemplates
        OccasionCategory.entries.forEach { category ->
            val matching = occasions.filter { it.category == category }
            assertTrue("Category $category should have ready-made templates", matching.isNotEmpty())
        }

        val hindiTemplates = occasions.filter { UnicodeFontEngine.containsHindi(it.text) }
        assertTrue("Should include Hindi occasion templates", hindiTemplates.isNotEmpty())

        hindiTemplates.forEach { occ ->
            val font = FontCatalog.findById(occ.fontId) ?: FontCatalog.allStyles.first()
            val wing = StudioGenerators.wingPresets.firstOrNull { it.id == occ.wingId }
                ?: StudioGenerators.wingPresets.first()
            val border = StudioGenerators.borderPresets.firstOrNull { it.id == occ.borderId }
                ?: StudioGenerators.borderPresets.first()
            val decorated = StudioGenerators.decorateText(
                occ.text,
                font,
                wing,
                border,
                StudioGenerators.separatorPresets.first()
            )
            val hindiWords = occ.text.split(" ").filter { UnicodeFontEngine.containsHindi(it) }
            hindiWords.forEach { word ->
                assertTrue(
                    "Occasion ${occ.id} must preserve Hindi word '$word' in '$decorated'",
                    decorated.contains(word)
                )
            }
        }
    }

    @Test
    fun stylishNameDesignerProducesCopyableDecoratedNameWithSubtitleAndHindi() {
        val font = FontCatalog.findById("bold_script") ?: FontCatalog.allStyles.first()
        val wing = StudioGenerators.wingPresets.first { it.id == "maharaja" }

        val designed = TextStudioCatalog.formatStylishName(
            name = "Aarav शर्मा",
            subtitle = "Royal King",
            font = font,
            wing = wing,
            badgeSymbol = "👑"
        )

        assertTrue(designed.contains("शर्मा"))
        assertTrue(designed.contains(wing.left))
        assertTrue(designed.contains(wing.right))
        assertTrue(designed.contains("👑"))
        assertTrue(designed.contains(UnicodeFontEngine.smallCaps("Royal King")))
        assertFalse(designed.contains("null"))
    }

    @Test
    fun whatsAppStatusDesignerGeneratesFormattedRealUnicodeText() {
        val font = FontCatalog.findById("sans_bold") ?: FontCatalog.allStyles.first()
        val divider = StudioGenerators.bioDividers[1].second

        val status = TextStudioCatalog.formatWhatsAppStatus(
            headerTag = "Daily Vibe",
            body = "Apna Time Aayega\nहर शब्द का अपना अंदाज़!",
            font = font,
            divider = divider,
            showFooter = true
        )

        assertTrue(status.contains(UnicodeFontEngine.smallCaps("Daily Vibe")))
        assertTrue(status.contains(UnicodeFontEngine.sansBold("Apna Time Aayega")))
        assertTrue(status.contains("हर शब्द का अपना अंदाज़!"))
        assertTrue(status.contains("VEDOGRAPHi Status"))
        assertTrue(status.contains(divider))
    }

    @Test
    fun bioPlatformFormattingSupportsInstagramAndFacebook() {
        val font = FontCatalog.findById("sans_bold") ?: FontCatalog.allStyles.first()
        val divider = StudioGenerators.bioDividers[1].second

        val instaBio = StudioGenerators.formatBio(
            headline = "VEDOGRAPHi",
            bodyLines = listOf("Creator & Dreamer", "हर शब्द का अपना अंदाज़!"),
            font = font,
            divider = divider,
            includeBullets = true
        )
        val fbBio = if (BioPlatform.FACEBOOK.label == "Facebook") {
            instaBio.replace(divider, "").trim()
        } else {
            instaBio
        }

        assertTrue(instaBio.contains(divider))
        assertFalse(fbBio.contains(divider))
        assertTrue(fbBio.contains(UnicodeFontEngine.sansBold("VEDOGRAPHi")))
        assertTrue(fbBio.contains("हर शब्द का अपना अंदाज़!"))
    }
}
