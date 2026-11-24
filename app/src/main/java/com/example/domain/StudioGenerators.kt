package com.example.domain

enum class BioCategory(val label: String) {
    ATTITUDE("Attitude"),
    HINDI_SHAYARI("हिंदी Shayari"),
    AESTHETIC("Aesthetic"),
    CREATOR("Creator"),
    LOVE("Love"),
    MOTIVATIONAL("Motivational"),
    GAMING("Gaming")
}

data class BioTemplate(
    val id: String,
    val category: BioCategory,
    val headline: String,
    val lines: List<String>
)

enum class UsernameVibe(val label: String) {
    GAMER_PRO("Gamer Pro"),
    ROYAL("Royal"),
    AESTHETIC("Aesthetic"),
    DESI_SWAG("Desi Swag"),
    MINIMAL("Minimal"),
    CYBER("Cyber")
}

data class WingPreset(val id: String, val label: String, val left: String, val right: String)
data class BorderPreset(val id: String, val label: String, val top: String, val bottom: String)
data class SeparatorPreset(val id: String, val label: String, val token: String)

object StudioGenerators {

    val bioTemplates: List<BioTemplate> = listOf(
        // Attitude
        BioTemplate(
            "att_1", BioCategory.ATTITUDE, "Official Legend",
            listOf("Simple by heart, Royal by mindset", "Zero ego • 100% Real", "Apna Time Khud Banate Hain")
        ),
        BioTemplate(
            "att_2", BioCategory.ATTITUDE, "Unstoppable Vibe",
            listOf("Silent moves, Loud results", "Haters watch • Legends build", "Born to Stand Out")
        ),
        BioTemplate(
            "att_3", BioCategory.ATTITUDE, "No Limits",
            listOf("Khud ki pehchan sabse badi hai", "Dream Big • Hustle Hard", "Classy & Fearless")
        ),
        // Hindi Shayari
        BioTemplate(
            "hin_1", BioCategory.HINDI_SHAYARI, "अंदाज़-ए-बयां",
            listOf("हर शब्द का अपना अंदाज़!", "खामोशी भी एक जवाब होती है", "दिल से लिखते हैं, सच लिखते हैं")
        ),
        BioTemplate(
            "hin_2", BioCategory.HINDI_SHAYARI, "रॉयल शायरी",
            listOf("नाम छोटा है पर सोच आसमान जितनी", "अपनी राह खुद चुनते हैं ✨", "मेहनत की स्याही से लिखते हैं तक़दीर")
        ),
        BioTemplate(
            "hin_3", BioCategory.HINDI_SHAYARI, "देसी स्वैग",
            listOf("सादा जीवन, शाही मिज़ाज", "दोस्ती दिल से, पहचान काम से", "जय हिंद • Proud Indian")
        ),
        // Aesthetic
        BioTemplate(
            "aes_1", BioCategory.AESTHETIC, "Moonchild",
            listOf("chasing sunsets & soft melodies", "coffee • poetry • midnight skies", "creating my own calm")
        ),
        BioTemplate(
            "aes_2", BioCategory.AESTHETIC, "Velvet Soul",
            listOf("minimal mind • golden heart", "slow days & lavender nights", "art in every little detail")
        ),
        BioTemplate(
            "aes_3", BioCategory.AESTHETIC, "Starlight Diary",
            listOf("breathing magic into words", "lost in books & lo-fi beats", "peace over perfection")
        ),
        // Creator
        BioTemplate(
            "cre_1", BioCategory.CREATOR, "Digital Creator",
            listOf("Visuals • Reels • Storytelling", "Turning ideas into digital art", "Collab / DM for work")
        ),
        BioTemplate(
            "cre_2", BioCategory.CREATOR, "Content Studio",
            listOf("Design • Typography • Lifestyle", "New posts every week ✦", "Creating daily magic")
        ),
        // Love
        BioTemplate(
            "lov_1", BioCategory.LOVE, "Pure Vibes",
            listOf("Dil se juda ek pyaara rishta", "You are my favorite notification ♡", "Always grateful, always smiling")
        ),
        BioTemplate(
            "lov_2", BioCategory.LOVE, "Forever Yours",
            listOf("तेरी मुस्कान ही मेरी दुनिया है", "Made of stardust & good memories", "Love • Peace • Kindness")
        ),
        // Motivational
        BioTemplate(
            "mot_1", BioCategory.MOTIVATIONAL, "Daily Rise",
            listOf("Discipline beats motivation every day", "1% better than yesterday", "Focus on the vision")
        ),
        BioTemplate(
            "mot_2", BioCategory.MOTIVATIONAL, "संकल्प",
            listOf("रुकना नहीं है, झुकना नहीं है", "Success is built in silence", "Keep grinding, stay humble")
        ),
        // Gaming
        BioTemplate(
            "gam_1", BioCategory.GAMING, "MVP Squad",
            listOf("Clutch King • Headshot Machine", "GGs only | Live & Ranked", "Fear the Rush")
        ),
        BioTemplate(
            "gam_2", BioCategory.GAMING, "Esports Grind",
            listOf("Eat • Sleep • Victory • Repeat", "Squad Leader | No Mercy", "Drop Hot, Win Clean")
        )
    )

    val bioFonts: List<FontStyleSpec> = listOf(
        FontCatalog.findById("sans_bold")!!,
        FontCatalog.findById("small_caps")!!,
        FontCatalog.findById("bold_script")!!,
        FontCatalog.findById("serif_bold_italic")!!,
        FontCatalog.findById("double_struck")!!,
        FontCatalog.findById("fraktur_gothic")!!,
        FontCatalog.findById("monospace")!!,
        FontCatalog.findById("fullwidth")!!,
        FontCatalog.findById("hindi_kalash_royal")!!,
        FontCatalog.findById("hindi_morpankh_script")!!,
        FontCatalog.findById("hindi_shlok_gold")!!,
        FontCatalog.findById("hindi_shayari_quote")!!
    )

    val bioDividers: List<Pair<String, String>> = listOf(
        "Clean" to "",
        "Star Line" to "━━━━━━✦✗✦━━━━━━",
        "Moon" to "•─────⋅☾☽⋅─────•",
        "Royal" to "꧁༒☬━━━━━━━━━☬༒꧂",
        "Floral" to "✿━━━━━━━━━━━━━━━✿",
        "Spark" to "✧･ﾟ: *✧･ﾟ:* 　　 *:･ﾟ✧*:･ﾟ✧"
    )

    fun formatBio(
        headline: String,
        bodyLines: List<String>,
        font: FontStyleSpec,
        divider: String,
        includeBullets: Boolean
    ): String {
        val styledHead = font.transform(headline.trim())
        val styledLines = bodyLines
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { line ->
                val transformed = font.transform(line)
                if (includeBullets) "✦ $transformed" else transformed
            }

        return buildString {
            if (divider.isNotEmpty()) {
                appendLine(divider)
            }
            if (styledHead.isNotEmpty()) {
                appendLine(styledHead)
            }
            styledLines.forEachIndexed { idx, line ->
                if (idx == styledLines.lastIndex && divider.isEmpty()) {
                    append(line)
                } else {
                    appendLine(line)
                }
            }
            if (divider.isNotEmpty()) {
                append(divider)
            }
        }.trim()
    }

    fun generateUsernames(rawName: String, vibe: UsernameVibe, seed: Int): List<String> {
        val clean = rawName.trim().ifEmpty { "VEDO" }
        val lower = clean.lowercase()
        val upper = clean.uppercase()

        val prefixes: List<String>
        val suffixes: List<String>
        val joins: List<String>

        when (vibe) {
            UsernameVibe.GAMER_PRO -> {
                prefixes = listOf("乡", "么", "⚡", "〆", "꧁༒", "『", "ツ", "PRO_")
                suffixes = listOf("乡", "ツ", "_OP", "_YT", "༒꧂", "』", "_GG", "々")
                joins = listOf(".", "_", "x", "々")
            }
            UsernameVibe.ROYAL -> {
                prefixes = listOf("♛ ", "꧁", "👑 ", "✦ ", "༺", "ROYAL_")
                suffixes = listOf(" ♛", "꧂", "_OFFICIAL", " 👑", "༻", "_KING")
                joins = listOf(".", "_", "✦")
            }
            UsernameVibe.AESTHETIC -> {
                prefixes = listOf("✿ ", "♡ ", "°• ", "☾ ", "✧ ", "soft.")
                suffixes = listOf(" ✿", " ♡", ".vibes", " ☽", ".aura", " ✧")
                joins = listOf(".", "·", "♡")
            }
            UsernameVibe.DESI_SWAG -> {
                prefixes = listOf("★彡 ", "꧁༒☬", "MR_", "DESI_", "NAWAB_", "⚡")
                suffixes = listOf(" 彡★", "☬༒꧂", "_JI", "_SWAG", "_RAJ", "⚡")
                joins = listOf("_", ".", "★")
            }
            UsernameVibe.MINIMAL -> {
                prefixes = listOf("", "i.", "the.", "its.", "@", "•")
                suffixes = listOf("", ".hq", ".x", ".io", "_", "•")
                joins = listOf(".", "_", "")
            }
            UsernameVibe.CYBER -> {
                prefixes = listOf("0x", ">_", "【", "◈ ", "NEO_", "SYN_")
                suffixes = listOf("_404", "//", "】", " ◈", "_EXE", "_AI")
                joins = listOf("_", "::", "-")
            }
        }

        val transforms: List<(String) -> String> = listOf(
            { UnicodeFontEngine.sansBold(it) },
            { UnicodeFontEngine.serifBoldItalic(it) },
            { UnicodeFontEngine.smallCaps(it) },
            { UnicodeFontEngine.boldScript(it) },
            { UnicodeFontEngine.boldFraktur(it) },
            { UnicodeFontEngine.doubleStruck(it) },
            { UnicodeFontEngine.monospace(it) },
            { UnicodeFontEngine.greekAesthetic(it) },
            { UnicodeFontEngine.runicNordic(it) },
            { UnicodeFontEngine.cyberLeet(it) },
            { UnicodeFontEngine.fullwidth(it) },
            { UnicodeFontEngine.curvyCute(it) }
        )

        val results = mutableListOf<String>()
        var i = 0
        while (results.size < 24 && i < 72) {
            val idx = (i + seed * 3) % transforms.size
            val p = prefixes[(i * 5 + seed) % prefixes.size]
            val s = suffixes[(i * 7 + seed * 2) % suffixes.size]
            val j = joins[(i + seed) % joins.size]
            val base = when (i % 7) {
                0 -> clean
                1 -> upper
                2 -> "$lower${j}99"
                3 -> "${clean}${j}x"
                4 -> "x$clean"
                5 -> "${lower}${j}07"
                else -> "${upper}${j}1"
            }
            val styledBase = transforms[idx](base)
            val candidate = "$p$styledBase$s".trim()
            if (candidate !in results) {
                results.add(candidate)
            }
            i++
        }
        return results
    }

    val wingPresets = listOf(
        WingPreset("none", "None", "", ""),
        WingPreset("maharaja", "Royal Wings", "꧁༒☬ ", " ☬༒꧂"),
        WingPreset("shahi", "Shahi Swag", "★彡 ", " 彡★"),
        WingPreset("wings", "Angel Wings", "༺ ", " ༻"),
        WingPreset("brackets", "Anime Box", "『 ", " 』"),
        WingPreset("sparkle", "Starburst", "✧･ﾟ: * ", " *:･ﾟ✧"),
        WingPreset("floral", "Lotus Bloom", "✿•° ", " °•✿"),
        WingPreset("clan", "Pro Clan", "乡 ", " ツ"),
        WingPreset("crown", "Monarch", "♛ ", " ♛"),
        WingPreset("heart", "Heartfelt", "»——❥ ", " ❥——«"),
        WingPreset("lightning", "Voltage", "⚡ ", " ⚡"),
        WingPreset("swords", "Dual Blades", "⚔ ", " ⚔")
    )

    val borderPresets = listOf(
        BorderPreset("none", "No Border", "", ""),
        BorderPreset("star", "Star Line", "━━━━━━✦✗✦━━━━━━", "━━━━━━✦✗✦━━━━━━"),
        BorderPreset("moon", "Moonlit", "•─────⋅☾☽⋅─────•", "•─────⋅☾☽⋅─────•"),
        BorderPreset("diamond", "Diamond Bar", "❖━━━━━━━━━━━━━❖", "❖━━━━━━━━━━━━━❖"),
        BorderPreset("waves", "Wave Ribbon", "〰〰〰〰✦〰〰〰〰", "〰〰〰〰✦〰〰〰〰"),
        BorderPreset("arrow", "Double Arrow", "»═══════════════«", "»═══════════════«")
    )

    val separatorPresets = listOf(
        SeparatorPreset("space", "Normal", " "),
        SeparatorPreset("dot", "• Dot", " • "),
        SeparatorPreset("star", "✦ Star", " ✦ "),
        SeparatorPreset("spark", "⚡ Bolt", " ⚡ "),
        SeparatorPreset("heart", "♡ Heart", " ♡ "),
        SeparatorPreset("diamond", "◆ Gem", " ◆ ")
    )

    fun decorateText(
        input: String,
        font: FontStyleSpec,
        wing: WingPreset,
        border: BorderPreset,
        separator: SeparatorPreset
    ): String {
        val trimmed = input.trim().ifEmpty { "VEDOGRAPHi" }
        val wordJoined = if (separator.token != " ") {
            trimmed.split(Regex("\\s+")).joinToString(separator.token)
        } else {
            trimmed
        }
        val transformed = font.transform(wordJoined)
        val winged = "${wing.left}$transformed${wing.right}"
        return buildString {
            if (border.top.isNotEmpty()) appendLine(border.top)
            append(winged)
            if (border.bottom.isNotEmpty()) {
                appendLine()
                append(border.bottom)
            }
        }
    }
}
