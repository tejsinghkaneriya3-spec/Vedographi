package com.example.ui.theme

import androidx.annotation.FontRes
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R

val KalamFontFamily = FontFamily(
    Font(R.font.kalam, FontWeight.Normal),
    Font(R.font.kalam, FontWeight.Medium),
    Font(R.font.kalam, FontWeight.Bold)
)

val AmitaFontFamily = FontFamily(
    Font(R.font.amita, FontWeight.Normal),
    Font(R.font.amita, FontWeight.Bold)
)

val RozhaOneFontFamily = FontFamily(
    Font(R.font.rozha_one, FontWeight.Normal),
    Font(R.font.rozha_one, FontWeight.Bold)
)

val YatraOneFontFamily = FontFamily(
    Font(R.font.yatra_one, FontWeight.Normal),
    Font(R.font.yatra_one, FontWeight.Bold)
)

val TillanaFontFamily = FontFamily(
    Font(R.font.tillana, FontWeight.Normal),
    Font(R.font.tillana, FontWeight.SemiBold),
    Font(R.font.tillana, FontWeight.Bold)
)

val GotuFontFamily = FontFamily(
    Font(R.font.gotu, FontWeight.Normal),
    Font(R.font.gotu, FontWeight.Bold)
)

enum class HindiCalligraphyPreset(
    val id: String,
    val displayName: String,
    val hindiLabel: String,
    val styleNote: String,
    val fontFamily: FontFamily,
    @param:FontRes val fontResId: Int?
) {
    KALAM(
        id = "kalam",
        displayName = "Kalam Pen",
        hindiLabel = "कलाम कैलीग्राफी",
        styleNote = "Handwritten reed pen",
        fontFamily = KalamFontFamily,
        fontResId = R.font.kalam
    ),
    AMITA(
        id = "amita",
        displayName = "Amita Brush",
        hindiLabel = "अमिता ब्रश",
        styleNote = "Graceful flowing strokes",
        fontFamily = AmitaFontFamily,
        fontResId = R.font.amita
    ),
    ROZHA_ONE(
        id = "rozha_one",
        displayName = "Rozha Shahi",
        hindiLabel = "रोझा शाही",
        styleNote = "Royal bold headline",
        fontFamily = RozhaOneFontFamily,
        fontResId = R.font.rozha_one
    ),
    YATRA_ONE(
        id = "yatra_one",
        displayName = "Yatra Art",
        hindiLabel = "यात्रा कला",
        styleNote = "Indian poster lettering",
        fontFamily = YatraOneFontFamily,
        fontResId = R.font.yatra_one
    ),
    TILLANA(
        id = "tillana",
        displayName = "Tillana Swash",
        hindiLabel = "तिल्लाना स्क्रिप्ट",
        styleNote = "Playful festive curve",
        fontFamily = TillanaFontFamily,
        fontResId = R.font.tillana
    ),
    GOTU(
        id = "gotu",
        displayName = "Gotu Clean",
        hindiLabel = "गोटू मॉडर्न",
        styleNote = "Crisp modern Devanagari",
        fontFamily = GotuFontFamily,
        fontResId = R.font.gotu
    ),
    DEFAULT(
        id = "default",
        displayName = "Classic Sans",
        hindiLabel = "क्लासिक",
        styleNote = "Clean everyday UI",
        fontFamily = PlusJakartaFontFamily,
        fontResId = R.font.plus_jakarta_sans
    );

    companion object {
        fun fromId(id: String?): HindiCalligraphyPreset {
            return entries.firstOrNull { it.id == id } ?: KALAM
        }
    }
}
