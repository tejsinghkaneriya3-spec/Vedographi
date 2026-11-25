package com.example.domain

/**
 * User-created custom Unicode font mapping built in the Custom Style Studio.
 * Allows per-character glyph overrides across A-Z, a-z, 0-9 plus prefix, suffix, and joiner.
 */
data class CustomFontDefinition(
    val id: String,
    val name: String,
    val charMap: Map<Char, String> = emptyMap(),
    val prefix: String = "",
    val suffix: String = "",
    val letterJoiner: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun transform(input: String): String {
        if (input.isEmpty()) return ""
        val sb = StringBuilder(input.length * 2)
        var i = 0
        while (i < input.length) {
            val cp = input.codePointAt(i)
            val charCount = Character.charCount(cp)
            val token = if (cp in 0..127) {
                val ch = cp.toChar()
                charMap[ch] ?: ch.toString()
            } else {
                String(Character.toChars(cp))
            }
            sb.append(token)
            val nextIdx = i + charCount
            if (letterJoiner.isNotEmpty() && nextIdx < input.length) {
                val nextCp = input.codePointAt(nextIdx)
                if (!Character.isWhitespace(cp) && !Character.isWhitespace(nextCp) &&
                    UnicodeFontEngine.isAsciiLetterOrDigit(cp) && UnicodeFontEngine.isAsciiLetterOrDigit(nextCp)
                ) {
                    sb.append(letterJoiner)
                }
            }
            i = nextIdx
        }
        return "$prefix$sb$suffix"
    }

    fun toFontStyleSpec(): FontStyleSpec {
        return FontStyleSpec(
            id = id,
            name = name.ifBlank { "My Custom Style" },
            category = FontCategory.MY_CUSTOM,
            badge = "CUSTOM",
            isWaFbSafe = true,
            isPro = false,
            isCustom = true,
            transform = ::transform
        )
    }
}

data class CustomStarterAlphabet(
    val id: String,
    val label: String,
    val buildMap: () -> Map<Char, String>
)

/**
 * Pure Kotlin deterministic codec so unit tests run cleanly without Android org.json stubs.
 */
object CustomFontCodec {
    private fun escape(value: String): String {
        val sb = StringBuilder(value.length)
        for (ch in value) {
            when (ch) {
                '\\' -> sb.append("\\\\")
                '\u001D' -> sb.append("\\D")
                '\u001E' -> sb.append("\\E")
                '\u001F' -> sb.append("\\F")
                '\t' -> sb.append("\\t")
                ':' -> sb.append("\\c")
                ',' -> sb.append("\\m")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun unescape(encoded: String): String {
        val sb = StringBuilder(encoded.length)
        var i = 0
        while (i < encoded.length) {
            val ch = encoded[i]
            if (ch == '\\' && i + 1 < encoded.length) {
                when (encoded[i + 1]) {
                    '\\' -> sb.append('\\')
                    'D' -> sb.append('\u001D')
                    'E' -> sb.append('\u001E')
                    'F' -> sb.append('\u001F')
                    't' -> sb.append('\t')
                    'c' -> sb.append(':')
                    'm' -> sb.append(',')
                    'n' -> sb.append('\n')
                    'r' -> sb.append('\r')
                    else -> sb.append(encoded[i + 1])
                }
                i += 2
            } else {
                sb.append(ch)
                i += 1
            }
        }
        return sb.toString()
    }

    fun encodeMap(charMap: Map<Char, String>): String {
        return charMap.entries
            .sortedBy { it.key.code }
            .joinToString(",") { (k, v) -> "${escape(k.toString())}:${escape(v)}" }
    }

    fun decodeMap(raw: String): Map<Char, String> {
        if (raw.isBlank()) return emptyMap()
        val result = linkedMapOf<Char, String>()
        raw.split(",").forEach { pair ->
            val idx = pair.indexOf(':')
            if (idx > 0) {
                val keyStr = unescape(pair.substring(0, idx))
                val valStr = unescape(pair.substring(idx + 1))
                if (keyStr.isNotEmpty() && valStr.isNotEmpty()) {
                    result[keyStr[0]] = valStr
                }
            }
        }
        return result
    }

    fun encodeList(list: List<CustomFontDefinition>): String {
        return list.joinToString("\u001D") { item ->
            listOf(
                escape(item.id),
                escape(item.name),
                escape(encodeMap(item.charMap)),
                escape(item.prefix),
                escape(item.suffix),
                escape(item.letterJoiner),
                item.createdAtMillis.toString()
            ).joinToString("\u001E")
        }
    }

    fun decodeList(raw: String): List<CustomFontDefinition> {
        if (raw.isBlank()) return emptyList()
        return raw.split("\u001D").mapNotNull { record ->
            val parts = record.split("\u001E")
            if (parts.size < 7) return@mapNotNull null
            val id = unescape(parts[0])
            val name = unescape(parts[1])
            if (id.isBlank() || name.isBlank()) return@mapNotNull null
            CustomFontDefinition(
                id = id,
                name = name,
                charMap = decodeMap(unescape(parts[2])),
                prefix = unescape(parts[3]),
                suffix = unescape(parts[4]),
                letterJoiner = unescape(parts[5]),
                createdAtMillis = parts[6].toLongOrNull() ?: 0L
            )
        }
    }
}

object CustomStylePresets {
    val allEditableChars: List<Char> = (('A'..'Z') + ('a'..'z') + ('0'..'9')).toList()

    private fun mapFromTransform(transform: (String) -> String): Map<Char, String> {
        val out = linkedMapOf<Char, String>()
        allEditableChars.forEach { ch ->
            out[ch] = transform(ch.toString())
        }
        return out
    }

    val starterAlphabets: List<CustomStarterAlphabet> = listOf(
        CustomStarterAlphabet("clean", "Default Mix") { emptyMap() },
        CustomStarterAlphabet("sans_bold", "Bold Base") { mapFromTransform(UnicodeFontEngine::sansBold) },
        CustomStarterAlphabet("bold_script", "Royal Script") { mapFromTransform(UnicodeFontEngine::boldScript) },
        CustomStarterAlphabet("fraktur", "Gothic Dark") { mapFromTransform(UnicodeFontEngine::frakturGothic) },
        CustomStarterAlphabet("double_struck", "Double Struck") { mapFromTransform(UnicodeFontEngine::doubleStruck) },
        CustomStarterAlphabet("small_caps", "Small Caps") { mapFromTransform(UnicodeFontEngine::smallCaps) },
        CustomStarterAlphabet("bubble_dark", "Bubble Pop") { mapFromTransform(UnicodeFontEngine::bubbleDark) },
        CustomStarterAlphabet("greek", "Greek Fusion") { mapFromTransform(UnicodeFontEngine::greekAesthetic) },
        CustomStarterAlphabet("curvy", "Curvy Cute") { mapFromTransform(UnicodeFontEngine::curvyCute) },
        CustomStarterAlphabet("runic", "Runic Gamer") { mapFromTransform(UnicodeFontEngine::runicNordic) },
        CustomStarterAlphabet("cyber", "Cyber Leet") { mapFromTransform(UnicodeFontEngine::cyberLeet) }
    )

    private val styleTransformsForGlyphPalette: List<(String) -> String> = listOf(
        { it },
        UnicodeFontEngine::sansBold,
        UnicodeFontEngine::sansBoldItalic,
        UnicodeFontEngine::boldScript,
        UnicodeFontEngine::scriptCursive,
        UnicodeFontEngine::serifBold,
        UnicodeFontEngine::frakturGothic,
        UnicodeFontEngine::boldFraktur,
        UnicodeFontEngine::doubleStruck,
        UnicodeFontEngine::monospace,
        UnicodeFontEngine::fullwidth,
        UnicodeFontEngine::smallCaps,
        UnicodeFontEngine::bubbleLight,
        UnicodeFontEngine::bubbleDark,
        UnicodeFontEngine::squareLight,
        UnicodeFontEngine::squareDark,
        UnicodeFontEngine::greekAesthetic,
        UnicodeFontEngine::curvyCute,
        UnicodeFontEngine::runicNordic,
        UnicodeFontEngine::cyberLeet,
        UnicodeFontEngine::currencyBling,
        UnicodeFontEngine::superscriptMini,
        UnicodeFontEngine::devanagariDigits
    )

    private val extraGlyphs: Map<Char, List<String>> = mapOf(
        'A' to listOf("Λ", "∆", "Д", "ꪖ", "ᗩ", "₳", "Ⱥ", "Ǻ"),
        'a' to listOf("α", "ą", "ä", "ค", "ꪖ", "ǟ", "ɐ", "4"),
        'B' to listOf("ß", "Ɓ", "ᗺ", "฿", "Ƀ"),
        'b' to listOf("в", "ъ", "ɓ", "ɮ", "ƃ"),
        'C' to listOf("₡", "Ƈ", "ᑕ", "₵", "Ȼ"),
        'c' to listOf("¢", "ƈ", "ς", "ɔ", "ȼ"),
        'D' to listOf("Ð", "Ɗ", "ᗪ", "Đ", "ᗡ"),
        'd' to listOf("∂", "ɖ", "ɗ", "đ", "p"),
        'E' to listOf("Ξ", "Ɛ", "Ǝ", "Ɇ", "Σ"),
        'e' to listOf("є", "ε", "ɛ", "ǝ", "3"),
        'S' to listOf("§", "Ş", "₴", "⚡", "$"),
        's' to listOf("ѕ", "ֆ", "ş", "5", "₴"),
        'O' to listOf("Ø", "Ö", "Θ", "◎", "❂"),
        'o' to listOf("σ", "օ", "ø", "✿", "0")
    )

    fun glyphChoicesFor(ch: Char): List<String> {
        val base = styleTransformsForGlyphPalette.map { it(ch.toString()) }
        val extras = extraGlyphs[ch].orEmpty()
        return (listOf(ch.toString()) + extras + base)
            .filter { it.isNotBlank() }
            .distinct()
    }

    fun randomizeMix(seed: Int = (System.currentTimeMillis() % 100000).toInt()): Map<Char, String> {
        val out = linkedMapOf<Char, String>()
        allEditableChars.forEachIndexed { idx, ch ->
            val options = glyphChoicesFor(ch).drop(1) // prefer styled glyphs
            if (options.isNotEmpty()) {
                val pickIdx = kotlin.math.abs(seed + idx * 7 + ch.code) % options.size
                out[ch] = options[pickIdx]
            }
        }
        return out
    }

    val prefixChoices: List<Pair<String, String>> = listOf(
        "" to "None",
        "꧁ " to "Royal",
        "✦ " to "Star",
        "♛ " to "Crown",
        "🔥 " to "Fire",
        "『 " to "Frame",
        "༺ " to "Wing",
        "♡ " to "Heart",
        "⚡ " to "Volt",
        "ॐ " to "Om"
    )

    val suffixChoices: List<Pair<String, String>> = listOf(
        "" to "None",
        " ꧂" to "Royal",
        " ✦" to "Star",
        " ♛" to "Crown",
        " 🔥" to "Fire",
        " 』" to "Frame",
        " ༻" to "Wing",
        " ♡" to "Heart",
        " ツ" to "Smiley",
        " ☑" to "Badge"
    )

    val joinerChoices: List<Pair<String, String>> = listOf(
        "" to "Normal",
        " " to "Space",
        "•" to "Dot •",
        "✦" to "Star ✦",
        "⚡" to "Bolt ⚡",
        "♡" to "Heart ♡",
        "~ " to "Wave ~",
        "-" to "Dash -"
    )
}
