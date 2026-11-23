package com.example.domain

enum class OccasionCategory(val label: String) {
    BIRTHDAY("Birthday"),
    ANNIVERSARY("Anniversary"),
    LOVE("Love"),
    ATTITUDE("Attitude"),
    FESTIVAL("Festival")
}

enum class BioPlatform(val label: String) {
    INSTAGRAM("Instagram"),
    FACEBOOK("Facebook")
}

data class DecorationTemplate(
    val id: String,
    val name: String,
    val tag: String,
    val fontId: String,
    val wingId: String,
    val borderId: String,
    val separatorId: String = "space"
) {
    fun render(input: String): String {
        val font = FontCatalog.findById(fontId) ?: FontCatalog.allStyles.first()
        val wing = StudioGenerators.wingPresets.firstOrNull { it.id == wingId } ?: StudioGenerators.wingPresets.first()
        val border = StudioGenerators.borderPresets.firstOrNull { it.id == borderId } ?: StudioGenerators.borderPresets.first()
        val sep = StudioGenerators.separatorPresets.firstOrNull { it.id == separatorId } ?: StudioGenerators.separatorPresets.first()
        return StudioGenerators.decorateText(input, font, wing, border, sep)
    }
}

data class OccasionTemplate(
    val id: String,
    val category: OccasionCategory,
    val title: String,
    val text: String,
    val fontId: String,
    val wingId: String,
    val borderId: String
)

object TextStudioCatalog {

    val quickSymbols: List<String> = listOf(
        "👑", "✦", "꧁", "꧂", "༒", "☬", "♡", "✿", "⚡", "🔥",
        "💎", "🌙", "🦋", "☑", "ॐ", "🎉", "🎂", "💐", "❤️", "✨"
    )

    /**
     * 50 distinct text decoration templates combining Unicode fonts, wings, borders, and separators.
     */
    val decorationTemplates: List<DecorationTemplate> = listOf(
        DecorationTemplate("tpl_01", "Maharaja Gold", "Royal", "serif_bold", "maharaja", "star", "space"),
        DecorationTemplate("tpl_02", "Shahi Swag", "Desi", "sans_bold_italic", "shahi", "none", "space"),
        DecorationTemplate("tpl_03", "Moonlight Calligraphy", "Aesthetic", "bold_script", "wings", "moon", "space"),
        DecorationTemplate("tpl_04", "Anime Prisma Box", "Minimal", "double_struck", "brackets", "diamond", "space"),
        DecorationTemplate("tpl_05", "Lotus Blossom", "Floral", "script_cursive", "floral", "none", "space"),
        DecorationTemplate("tpl_06", "Esports Clan Crest", "Gamer", "sans_bold", "clan", "arrow", "dot"),
        DecorationTemplate("tpl_07", "Monarch Crown", "Royal", "serif_bold_italic", "crown", "star", "space"),
        DecorationTemplate("tpl_08", "Starburst Banner", "Fancy", "bold_script", "sparkle", "waves", "space"),
        DecorationTemplate("tpl_09", "Cupid Ribbon", "Love", "script_cursive", "heart", "none", "heart"),
        DecorationTemplate("tpl_10", "Voltage Strike", "Bold", "sans_bold", "lightning", "arrow", "spark"),
        DecorationTemplate("tpl_11", "Dual Blade Gothic", "Dark", "bold_fraktur", "swords", "star", "space"),
        DecorationTemplate("tpl_12", "Diamond Small Caps", "Bio", "small_caps", "none", "diamond", "diamond"),
        DecorationTemplate("tpl_13", "Vaporwave Horizon", "Retro", "fullwidth", "brackets", "waves", "space"),
        DecorationTemplate("tpl_14", "Terminal Code Frame", "Tech", "monospace", "none", "arrow", "dot"),
        DecorationTemplate("tpl_15", "Bubble Pop Banner", "Cute", "bubble_dark", "sparkle", "none", "space"),
        DecorationTemplate("tpl_16", "Royal Trishul Serif", "Desi", "serif_bold", "maharaja", "diamond", "star"),
        DecorationTemplate("tpl_17", "Velvet Script Halo", "Soft", "curvy_cute", "wings", "moon", "space"),
        DecorationTemplate("tpl_18", "Obsidian Fraktur", "Gothic", "fraktur_gothic", "swords", "arrow", "space"),
        DecorationTemplate("tpl_19", "Sparkle Sans Bold", "Clean", "sans_bold", "sparkle", "star", "star"),
        DecorationTemplate("tpl_20", "Greek Fusion Crest", "Stylish", "greek_aesthetic", "clan", "diamond", "space"),
        DecorationTemplate("tpl_21", "Nordic Clan Banner", "Gamer", "runic_nordic", "swords", "waves", "dot"),
        DecorationTemplate("tpl_22", "Cyber Hacker Frame", "Tech", "cyber_leet", "lightning", "arrow", "spark"),
        DecorationTemplate("tpl_23", "Imperial Double Crown", "Royal", "double_struck", "crown", "star", "space"),
        DecorationTemplate("tpl_24", "Rose Garden Cursive", "Love", "bold_script", "floral", "moon", "heart"),
        DecorationTemplate("tpl_25", "Keycap Pop Tag", "Fun", "square_dark", "none", "star", "space"),
        DecorationTemplate("tpl_26", "Classic Quoted Italic", "Poetic", "serif_italic", "wings", "waves", "space"),
        DecorationTemplate("tpl_27", "Beaded Small Caps", "Bio", "small_caps", "floral", "none", "dot"),
        DecorationTemplate("tpl_28", "Lightning Empire", "Attitude", "bold_fraktur", "lightning", "diamond", "spark"),
        DecorationTemplate("tpl_29", "Starlight Bubble", "Cute", "bubble_light", "sparkle", "moon", "space"),
        DecorationTemplate("tpl_30", "Rupee Luxe Banner", "Desi", "currency_bling", "crown", "diamond", "star"),
        DecorationTemplate("tpl_31", "Surya Shahi", "Desi", "serif_bold", "shahi", "star", "space"),
        DecorationTemplate("tpl_32", "Midnight Heartbeat", "Love", "sans_bold_italic", "heart", "moon", "heart"),
        DecorationTemplate("tpl_33", "Zen Minimal Mono", "Minimal", "monospace", "brackets", "none", "dot"),
        DecorationTemplate("tpl_34", "Royal Winged Script", "Wedding", "bold_script", "maharaja", "moon", "space"),
        DecorationTemplate("tpl_35", "Pro Squad Emblem", "Gamer", "sans_bold", "clan", "star", "spark"),
        DecorationTemplate("tpl_36", "Festival Glow", "Festive", "serif_bold_italic", "sparkle", "star", "star"),
        DecorationTemplate("tpl_37", "Crystal Double Dots", "Bio", "double_struck", "none", "diamond", "dot"),
        DecorationTemplate("tpl_38", "Slashed Neon", "Edgy", "slash_alt", "lightning", "waves", "space"),
        DecorationTemplate("tpl_39", "Tiny Mini Tag", "Minimal", "superscript_mini", "brackets", "none", "dot"),
        DecorationTemplate("tpl_40", "Parenthesized Charm", "Cute", "parenthesized", "floral", "waves", "space"),
        DecorationTemplate("tpl_41", "Crown Jewel Bold", "Royal", "sans_bold", "crown", "diamond", "diamond"),
        DecorationTemplate("tpl_42", "Gothic Moonlight", "Dark", "fraktur_gothic", "wings", "moon", "space"),
        DecorationTemplate("tpl_43", "Aesthetic Dot Waves", "Aesthetic", "small_caps", "sparkle", "waves", "dot"),
        DecorationTemplate("tpl_44", "Firecracker Bold", "Festive", "sans_bold_italic", "shahi", "arrow", "spark"),
        DecorationTemplate("tpl_45", "Heartfelt Serif", "Love", "serif_bold", "heart", "moon", "space"),
        DecorationTemplate("tpl_46", "Square Tile Arcade", "Gamer", "square_light", "clan", "diamond", "space"),
        DecorationTemplate("tpl_47", "Calligraphy Plaque", "Formal", "script_cursive", "brackets", "star", "space"),
        DecorationTemplate("tpl_48", "Diamond Sans Frame", "Modern", "sans_bold", "none", "diamond", "star"),
        DecorationTemplate("tpl_49", "Double Arrow Swag", "Attitude", "bold_fraktur", "shahi", "arrow", "space"),
        DecorationTemplate("tpl_50", "Eternal Starlight", "Luxury", "double_struck", "maharaja", "moon", "diamond")
    )

    /**
     * Ready-made Birthday, Anniversary, Love, Attitude, and Festival templates (English & Hindi).
     */
    val occasionTemplates: List<OccasionTemplate> = listOf(
        // Birthday (7)
        OccasionTemplate(
            "bday_1", OccasionCategory.BIRTHDAY, "Royal Birthday Wish",
            "Happy Birthday! May every dream shine bright", "bold_script", "sparkle", "star"
        ),
        OccasionTemplate(
            "bday_2", OccasionCategory.BIRTHDAY, "Hindi Birthday",
            "जन्मदिन की हार्दिक शुभकामनाएँ! सदा खुश रहो", "serif_bold", "floral", "star"
        ),
        OccasionTemplate(
            "bday_3", OccasionCategory.BIRTHDAY, "Bestie Birthday Bash",
            "Happy Bday Legend! Party Hard Today", "sans_bold", "shahi", "diamond"
        ),
        OccasionTemplate(
            "bday_4", OccasionCategory.BIRTHDAY, "Aesthetic Birthday",
            "Another year wiser, brighter & stronger", "small_caps", "wings", "moon"
        ),
        OccasionTemplate(
            "bday_5", OccasionCategory.BIRTHDAY, "Cake & Candles",
            "Cheers to your best chapter yet!", "double_struck", "crown", "waves"
        ),
        OccasionTemplate(
            "bday_6", OccasionCategory.BIRTHDAY, "Bhai Birthday Swag",
            "Happy Birthday Bhai • जीते रहो मुस्कुराते रहो", "sans_bold_italic", "maharaja", "star"
        ),
        OccasionTemplate(
            "bday_7", OccasionCategory.BIRTHDAY, "Sweet Minimal Wish",
            "Wishing you joy, health & endless smiles", "script_cursive", "heart", "none"
        ),

        // Anniversary (6)
        OccasionTemplate(
            "anniv_1", OccasionCategory.ANNIVERSARY, "Golden Anniversary",
            "Happy Anniversary! Forever & Always", "bold_script", "heart", "moon"
        ),
        OccasionTemplate(
            "anniv_2", OccasionCategory.ANNIVERSARY, "Hindi Salgirah",
            "शादी की सालगिरह की ढेरों बधाईयाँ!", "serif_bold", "floral", "star"
        ),
        OccasionTemplate(
            "anniv_3", OccasionCategory.ANNIVERSARY, "Couple Goals",
            "Two souls, one beautiful journey together", "serif_bold_italic", "wings", "diamond"
        ),
        OccasionTemplate(
            "anniv_4", OccasionCategory.ANNIVERSARY, "Milestone Love",
            "Another year of love, laughter & memories", "double_struck", "sparkle", "moon"
        ),
        OccasionTemplate(
            "anniv_5", OccasionCategory.ANNIVERSARY, "Parents Anniversary",
            "Maa & Papa • Happy Anniversary with Love", "sans_bold", "crown", "star"
        ),
        OccasionTemplate(
            "anniv_6", OccasionCategory.ANNIVERSARY, "Romantic Forever",
            "साथ आपका यूँ ही बना रहे हर जनम", "script_cursive", "heart", "waves"
        ),

        // Love (7)
        OccasionTemplate(
            "love_1", OccasionCategory.LOVE, "Pure Heartbeat",
            "You are my today and all of my tomorrows", "bold_script", "heart", "moon"
        ),
        OccasionTemplate(
            "love_2", OccasionCategory.LOVE, "Hindi Ishq",
            "तुम मेरी हर दुआ का सबसे खूबसूरत हिस्सा हो", "serif_bold", "floral", "moon"
        ),
        OccasionTemplate(
            "love_3", OccasionCategory.LOVE, "Soulmate Note",
            "Every love story is beautiful, ours is my favorite", "script_cursive", "wings", "none"
        ),
        OccasionTemplate(
            "love_4", OccasionCategory.LOVE, "Soft Aesthetic Love",
            "In a sea of people, my eyes always search for you", "small_caps", "heart", "waves"
        ),
        OccasionTemplate(
            "love_5", OccasionCategory.LOVE, "Dil Ki Baat",
            "तेरी मुस्कान ही मेरी सबसे बड़ी खुशी है", "sans_bold_italic", "sparkle", "moon"
        ),
        OccasionTemplate(
            "love_6", OccasionCategory.LOVE, "Moon & Stars",
            "Love you to the moon and back", "double_struck", "wings", "moon"
        ),
        OccasionTemplate(
            "love_7", OccasionCategory.LOVE, "Cute Couple Tag",
            "Mine Forever • दिल से दिल तक", "curvy_cute", "heart", "diamond"
        ),

        // Attitude (7)
        OccasionTemplate(
            "att_1", OccasionCategory.ATTITUDE, "Royal Maharaja",
            "Born to Express, Not to Impress", "bold_fraktur", "maharaja", "arrow"
        ),
        OccasionTemplate(
            "att_2", OccasionCategory.ATTITUDE, "Desi Swag",
            "अपना अंदाज़ ही अपनी पहचान है!", "sans_bold", "shahi", "star"
        ),
        OccasionTemplate(
            "att_3", OccasionCategory.ATTITUDE, "Silent Hustler",
            "Work in silence, let success make the noise", "sans_bold_italic", "lightning", "diamond"
        ),
        OccasionTemplate(
            "att_4", OccasionCategory.ATTITUDE, "King Mindset",
            "No competition when you are original", "serif_bold", "crown", "arrow"
        ),
        OccasionTemplate(
            "att_5", OccasionCategory.ATTITUDE, "Hindi Attitude",
            "हम वो नहीं जो भीड़ में खो जाएं!", "sans_bold", "swords", "star"
        ),
        OccasionTemplate(
            "att_6", OccasionCategory.ATTITUDE, "Fearless Vibe",
            "My life, my rules, my attitude", "double_struck", "shahi", "diamond"
        ),
        OccasionTemplate(
            "att_7", OccasionCategory.ATTITUDE, "Unstoppable",
            "Fall seven times, stand up eight", "monospace", "brackets", "arrow"
        ),

        // Festival (8)
        OccasionTemplate(
            "fest_1", OccasionCategory.FESTIVAL, "Happy Diwali",
            "Shubh Deepawali • दीपावली की हार्दिक शुभकामनाएँ!", "serif_bold", "sparkle", "star"
        ),
        OccasionTemplate(
            "fest_2", OccasionCategory.FESTIVAL, "Happy Holi",
            "Happy Holi! रंगों भरी होली की शुभकामनाएँ", "bold_script", "floral", "waves"
        ),
        OccasionTemplate(
            "fest_3", OccasionCategory.FESTIVAL, "Eid Mubarak",
            "Eid Mubarak • चाँद मुबारक हो सबको", "bold_script", "wings", "moon"
        ),
        OccasionTemplate(
            "fest_4", OccasionCategory.FESTIVAL, "Raksha Bandhan",
            "Happy Raksha Bandhan • भाई-बहन का अटूट प्यार", "serif_bold_italic", "heart", "star"
        ),
        OccasionTemplate(
            "fest_5", OccasionCategory.FESTIVAL, "Navratri & Dussehra",
            "Shubh Navratri • जय माता दी!", "sans_bold", "maharaja", "star"
        ),
        OccasionTemplate(
            "fest_6", OccasionCategory.FESTIVAL, "Ganesh Chaturthi",
            "Ganpati Bappa Morya • गणपति बप्पा मोरया!", "serif_bold", "shahi", "diamond"
        ),
        OccasionTemplate(
            "fest_7", OccasionCategory.FESTIVAL, "Happy New Year",
            "Happy New Year! New Dreams, New Beginnings", "double_struck", "sparkle", "arrow"
        ),
        OccasionTemplate(
            "fest_8", OccasionCategory.FESTIVAL, "Independence Day",
            "Happy Independence Day • जय हिंद वन्दे मातरम्!", "sans_bold", "crown", "star"
        )
    )

    fun formatStylishName(
        name: String,
        subtitle: String,
        font: FontStyleSpec,
        wing: WingPreset,
        badgeSymbol: String
    ): String {
        val cleanName = name.trim().ifEmpty { "VEDOGRAPHi" }
        val styledName = font.transform(cleanName)
        val mainLine = "${wing.left}$styledName${wing.right}"
        val cleanSub = subtitle.trim()
        return if (cleanSub.isEmpty()) {
            if (badgeSymbol.isNotEmpty()) "$mainLine $badgeSymbol" else mainLine
        } else {
            val styledSub = UnicodeFontEngine.smallCaps(cleanSub)
            buildString {
                appendLine(mainLine)
                if (badgeSymbol.isNotEmpty()) {
                    append("$badgeSymbol $styledSub $badgeSymbol")
                } else {
                    append("✦ $styledSub ✦")
                }
            }
        }
    }

    val waStatusMoods: List<Pair<String, String>> = listOf(
        "Attitude" to "अपनी कहानी खुद लिखो,\nदुनिया तो सिर्फ पढ़ेगी!",
        "Morning" to "Good Morning!\nNayi subah, naye irade, nayi umeed.",
        "Love" to "दिल की बात लफ्ज़ों में नहीं,\nआँखों में बयां होती है।",
        "Hustle" to "Dream Big • Stay Humble\nMake Every Second Count",
        "Festive" to "खुशियों के दीप जलें,\nहर दिन उत्सव सा लगे!"
    )

    fun formatWhatsAppStatus(
        headerTag: String,
        body: String,
        font: FontStyleSpec,
        divider: String,
        showFooter: Boolean
    ): String {
        val styledBody = body.lines().joinToString("\n") { line ->
            if (line.isBlank()) "" else font.transform(line)
        }.trim()
        val cleanHeader = headerTag.trim()
        return buildString {
            if (divider.isNotEmpty()) appendLine(divider)
            if (cleanHeader.isNotEmpty()) {
                appendLine("✦ ${UnicodeFontEngine.smallCaps(cleanHeader)} ✦")
            }
            append(styledBody)
            if (showFooter) {
                appendLine()
                append("— ✧ VEDOGRAPHi Status ✧ —")
            }
            if (divider.isNotEmpty()) {
                appendLine()
                append(divider)
            }
        }.trim()
    }
}
