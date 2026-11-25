package com.example.domain

/**
 * Pure Kotlin Unicode text transformation engine.
 * Supports Devanagari (Hindi) akshara-cluster-safe transformations alongside English/Latin Unicode
 * alphabets so Hindi matras and conjunct consonants (virama) are never corrupted.
 */
object UnicodeFontEngine {

    fun containsHindi(text: String): Boolean {
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            if (cp in 0x0900..0x097F || cp in 0xA8E0..0xA8FF) {
                return true
            }
            i += Character.charCount(cp)
        }
        return false
    }

    fun isAsciiLetterOrDigit(cp: Int): Boolean {
        return (cp in 'A'.code..'Z'.code) ||
            (cp in 'a'.code..'z'.code) ||
            (cp in '0'.code..'9'.code)
    }

    private fun isDevanagariBase(cp: Int): Boolean {
        return cp in 0x0904..0x0939 ||
            cp in 0x0958..0x0961 ||
            cp in 0x0972..0x097F
    }

    private fun isDevanagariConsonant(cp: Int): Boolean {
        return cp in 0x0915..0x0939 ||
            cp in 0x0958..0x095F ||
            cp in 0x0978..0x097F
    }

    private fun isDevanagariCombiningOrSign(cp: Int): Boolean {
        return cp in 0x0900..0x0903 || // chandrabindu, anusvara, visarga
            cp == 0x093C || // nukta
            cp in 0x093A..0x093B ||
            cp in 0x093E..0x094C || // dependent vowel matras
            cp in 0x094E..0x0957 || // matras, Vedic tone marks
            cp in 0x0962..0x0963 || // vocalic vowel signs
            cp == 0xFE0F || cp == 0xFE0E
    }

    /**
     * Splits text into grapheme/akshara tokens while keeping every Devanagari conjunct (consonant + virama + consonant)
     * and trailing matras/anusvara/nukta attached inside a single indivisible cluster.
     */
    fun clusterDevanagariWords(input: String): List<Pair<String, Boolean>> {
        if (input.isEmpty()) return emptyList()
        val result = mutableListOf<Pair<String, Boolean>>()
        var i = 0
        while (i < input.length) {
            val cp = input.codePointAt(i)
            val charCount = Character.charCount(cp)
            if (isDevanagariBase(cp)) {
                val cluster = StringBuilder()
                cluster.appendCodePoint(cp)
                var j = i + charCount
                var expectConsonantAfterVirama = false
                while (j < input.length) {
                    val nextCp = input.codePointAt(j)
                    val nextCount = Character.charCount(nextCp)
                    when {
                        nextCp == 0x094D -> {
                            cluster.appendCodePoint(nextCp)
                            j += nextCount
                            expectConsonantAfterVirama = true
                        }
                        expectConsonantAfterVirama && isDevanagariConsonant(nextCp) -> {
                            cluster.appendCodePoint(nextCp)
                            j += nextCount
                            expectConsonantAfterVirama = false
                        }
                        isDevanagariCombiningOrSign(nextCp) -> {
                            cluster.appendCodePoint(nextCp)
                            j += nextCount
                            expectConsonantAfterVirama = false
                        }
                        else -> break
                    }
                }
                result.add(cluster.toString() to true)
                i = j
            } else {
                result.add(String(Character.toChars(cp)) to false)
                i += charCount
            }
        }
        return result
    }

    /**
     * Inserts a clean Unicode separator between adjacent Devanagari aksharas inside each Hindi word
     * while never splitting matras or virama conjuncts. Works cleanly in WhatsApp, Facebook, and Instagram.
     */
    fun hindiAksharaInterleave(input: String, separator: String): String {
        if (input.isEmpty() || separator.isEmpty()) return input
        val tokens = clusterDevanagariWords(input)
        val sb = StringBuilder(input.length * 2)
        for (idx in tokens.indices) {
            val (token, isDev) = tokens[idx]
            sb.append(token)
            if (isDev && idx + 1 < tokens.size && tokens[idx + 1].second) {
                sb.append(separator)
            }
        }
        return sb.toString()
    }

    /**
     * Wraps every Devanagari akshara cluster in brackets/ornaments (e.g. 『ह』『र』 『श』『ब्द』).
     */
    fun hindiAksharaWrap(input: String, left: String, right: String): String {
        if (input.isEmpty()) return input
        val tokens = clusterDevanagariWords(input)
        val sb = StringBuilder(input.length * 3)
        for ((token, isDev) in tokens) {
            if (isDev) {
                sb.append(left).append(token).append(right)
            } else {
                sb.append(token)
            }
        }
        return sb.toString()
    }

    /**
     * Wraps each Devanagari word individually with clean ornaments so Hindi phrases stand out visually.
     */
    fun hindiWordWrap(input: String, left: String, right: String): String {
        if (input.isEmpty()) return input
        val tokens = clusterDevanagariWords(input)
        val sb = StringBuilder(input.length * 2)
        val currentDevWord = StringBuilder()

        fun flushWord() {
            if (currentDevWord.isNotEmpty()) {
                sb.append(left).append(currentDevWord).append(right)
                currentDevWord.clear()
            }
        }

        for ((token, isDev) in tokens) {
            if (isDev) {
                currentDevWord.append(token)
            } else {
                flushWord()
                sb.append(token)
            }
        }
        flushWord()
        return sb.toString()
    }

    // ---- Native WhatsApp Formatting Helpers (100% support for Hindi + English on WhatsApp) ----

    fun whatsappFormat(input: String, markerLeft: String, markerRight: String = markerLeft): String {
        if (input.isBlank()) return input
        return input.lines().joinToString("\n") { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) line else "$markerLeft$trimmed$markerRight"
        }
    }

    fun whatsappBold(input: String): String = whatsappFormat(input, "*")
    fun whatsappItalic(input: String): String = whatsappFormat(input, "_")
    fun whatsappStrike(input: String): String = whatsappFormat(input, "~")
    fun whatsappBoldItalic(input: String): String = whatsappFormat(input, "*_", "_*")
    fun whatsappMono(input: String): String = whatsappFormat(input, "```")

    /**
     * Maps ASCII A-Z, a-z, and optional 0-9 to mathematical / enclosed Unicode blocks
     * while passing every other code point (including Hindi/Devanagari) untouched.
     */
    fun mapRange(
        input: String,
        upperStart: Int,
        lowerStart: Int,
        digitStart: Int? = null,
        exceptions: Map<Char, Int> = emptyMap(),
        uppercaseBeforeMap: Boolean = false
    ): String {
        if (input.isEmpty()) return input
        val sb = StringBuilder(input.length * 2)
        var i = 0
        while (i < input.length) {
            val cp = input.codePointAt(i)
            val charCount = Character.charCount(cp)
            val effectiveCp = if (uppercaseBeforeMap && cp in 'a'.code..'z'.code) {
                cp - 'a'.code + 'A'.code
            } else {
                cp
            }

            when {
                effectiveCp in 'A'.code..'Z'.code -> {
                    val ch = effectiveCp.toChar()
                    val mapped = exceptions[ch] ?: (upperStart + (effectiveCp - 'A'.code))
                    sb.appendCodePoint(mapped)
                }
                effectiveCp in 'a'.code..'z'.code -> {
                    val ch = effectiveCp.toChar()
                    val mapped = exceptions[ch] ?: (lowerStart + (effectiveCp - 'a'.code))
                    sb.appendCodePoint(mapped)
                }
                digitStart != null && effectiveCp in '0'.code..'9'.code -> {
                    sb.appendCodePoint(digitStart + (effectiveCp - '0'.code))
                }
                else -> {
                    sb.appendCodePoint(cp)
                }
            }
            i += charCount
        }
        return sb.toString()
    }

    /**
     * Maps ASCII A-Z, a-z, 0-9 via custom 26/26/10 glyph lists.
     * Supports multi-codepoint glyphs and surrogate pairs cleanly.
     */
    fun mapTable(
        input: String,
        upper: List<String>,
        lower: List<String>,
        digits: List<String>? = null
    ): String {
        if (input.isEmpty()) return input
        val sb = StringBuilder(input.length * 2)
        var i = 0
        while (i < input.length) {
            val cp = input.codePointAt(i)
            when {
                cp in 'A'.code..'Z'.code -> sb.append(upper[cp - 'A'.code])
                cp in 'a'.code..'z'.code -> sb.append(lower[cp - 'a'.code])
                digits != null && cp in '0'.code..'9'.code -> sb.append(digits[cp - '0'.code])
                else -> sb.appendCodePoint(cp)
            }
            i += Character.charCount(cp)
        }
        return sb.toString()
    }

    /**
     * Applies combining diacritical marks ONLY to ASCII A-Z, a-z, 0-9 so Devanagari matras
     * and conjunct consonants are never corrupted.
     */
    fun combineAscii(input: String, vararg combiningMarks: Int): String {
        if (input.isEmpty()) return input
        val sb = StringBuilder(input.length * 3)
        var i = 0
        while (i < input.length) {
            val cp = input.codePointAt(i)
            sb.appendCodePoint(cp)
            if (isAsciiLetterOrDigit(cp)) {
                for (mark in combiningMarks) {
                    sb.appendCodePoint(mark)
                }
            }
            i += Character.charCount(cp)
        }
        return sb.toString()
    }

    /**
     * Inserts a separator string between adjacent ASCII letters/digits without splitting
     * Devanagari syllables or corrupting matras.
     */
    fun interleaveAscii(input: String, separator: String): String {
        if (input.isEmpty()) return input
        val sb = StringBuilder(input.length * 2)
        var i = 0
        while (i < input.length) {
            val cp = input.codePointAt(i)
            val charCount = Character.charCount(cp)
            sb.appendCodePoint(cp)
            val nextIndex = i + charCount
            if (isAsciiLetterOrDigit(cp) && nextIndex < input.length) {
                val nextCp = input.codePointAt(nextIndex)
                if (isAsciiLetterOrDigit(nextCp)) {
                    sb.append(separator)
                }
            }
            i = nextIndex
        }
        return sb.toString()
    }

    /**
     * Flips ASCII runs upside-down and reverses ONLY ASCII runs while leaving
     * Devanagari / non-ASCII runs in natural reading order.
     */
    fun upsideDownSafe(input: String): String {
        if (input.isEmpty()) return input
        val out = StringBuilder(input.length)
        val asciiRun = StringBuilder()

        fun flushAscii() {
            if (asciiRun.isNotEmpty()) {
                for (j in asciiRun.length - 1 downTo 0) {
                    val ch = asciiRun[j]
                    out.append(UPSIDE_DOWN_MAP[ch] ?: ch)
                }
                asciiRun.clear()
            }
        }

        var i = 0
        while (i < input.length) {
            val cp = input.codePointAt(i)
            if (cp in 0x21..0x7E) {
                asciiRun.append(cp.toChar())
            } else {
                flushAscii()
                out.appendCodePoint(cp)
            }
            i += Character.charCount(cp)
        }
        flushAscii()
        return out.toString()
    }

    /**
     * Alternates uppercase/lowercase strictly on ASCII letters while leaving Devanagari untouched.
     */
    fun alternatingCase(input: String, startUpper: Boolean = true): String {
        if (input.isEmpty()) return input
        val sb = StringBuilder(input.length)
        var upper = startUpper
        var i = 0
        while (i < input.length) {
            val cp = input.codePointAt(i)
            when {
                cp in 'A'.code..'Z'.code || cp in 'a'.code..'z'.code -> {
                    val ch = cp.toChar()
                    sb.append(if (upper) ch.uppercaseChar() else ch.lowercaseChar())
                    upper = !upper
                }
                else -> sb.appendCodePoint(cp)
            }
            i += Character.charCount(cp)
        }
        return sb.toString()
    }

    /**
     * Converts ASCII numerals 0-9 into Devanagari numerals ०-९ while preserving
     * all Devanagari letters, matras, conjuncts, and Latin characters intact.
     */
    fun devanagariDigits(input: String): String {
        if (input.isEmpty()) return input
        val sb = StringBuilder(input.length)
        var i = 0
        while (i < input.length) {
            val cp = input.codePointAt(i)
            if (cp in '0'.code..'9'.code) {
                sb.appendCodePoint(0x0966 + (cp - '0'.code))
            } else {
                sb.appendCodePoint(cp)
            }
            i += Character.charCount(cp)
        }
        return sb.toString()
    }

    /**
     * Formats Hindi, Hinglish, or English text in authentic Unicode Hindi Calligraphy style:
     * transforms Devanagari words safely at akshara-cluster boundaries (optional inter-akshara
     * ornament, word framing, or WhatsApp bold/italic markers), converts numerals to ०-९,
     * and transforms Latin letters into matching calligraphy Unicode alphabets.
     */
    fun hindiCalligraphyStyle(
        input: String,
        englishTransform: (String) -> String = ::boldScript,
        hindiTransform: (String) -> String = { it },
        useDevanagariDigits: Boolean = true,
        prefix: String = "",
        suffix: String = ""
    ): String {
        val digitNormalized = if (useDevanagariDigits) devanagariDigits(input) else input
        val hindiStyled = hindiTransform(digitNormalized)
        val styled = englishTransform(hindiStyled)
        return "$prefix$styled$suffix"
    }

    // ---- Standard Unicode Alphabets ----

    fun serifBold(input: String): String =
        mapRange(input, upperStart = 0x1D400, lowerStart = 0x1D41A, digitStart = 0x1D7CE)

    fun serifItalic(input: String): String =
        mapRange(
            input,
            upperStart = 0x1D434,
            lowerStart = 0x1D44E,
            exceptions = mapOf('h' to 0x210E)
        )

    fun serifBoldItalic(input: String): String =
        mapRange(input, upperStart = 0x1D468, lowerStart = 0x1D482, digitStart = 0x1D7CE)

    fun sansBold(input: String): String =
        mapRange(input, upperStart = 0x1D5D4, lowerStart = 0x1D5EE, digitStart = 0x1D7EC)

    fun sansItalic(input: String): String =
        mapRange(input, upperStart = 0x1D608, lowerStart = 0x1D622)

    fun sansBoldItalic(input: String): String =
        mapRange(input, upperStart = 0x1D63C, lowerStart = 0x1D656, digitStart = 0x1D7EC)

    fun scriptCursive(input: String): String =
        mapRange(
            input,
            upperStart = 0x1D49C,
            lowerStart = 0x1D4B6,
            exceptions = mapOf(
                'B' to 0x212C,
                'E' to 0x2130,
                'F' to 0x2131,
                'H' to 0x210B,
                'I' to 0x2110,
                'L' to 0x2112,
                'M' to 0x2133,
                'R' to 0x211B,
                'e' to 0x212F,
                'g' to 0x210A,
                'o' to 0x2134
            )
        )

    fun boldScript(input: String): String =
        mapRange(input, upperStart = 0x1D4D0, lowerStart = 0x1D4EA, digitStart = 0x1D7CE)

    fun frakturGothic(input: String): String =
        mapRange(
            input,
            upperStart = 0x1D504,
            lowerStart = 0x1D51E,
            exceptions = mapOf(
                'C' to 0x212D,
                'H' to 0x210C,
                'I' to 0x2111,
                'R' to 0x211C,
                'Z' to 0x2128
            )
        )

    fun boldFraktur(input: String): String =
        mapRange(input, upperStart = 0x1D56C, lowerStart = 0x1D586)

    fun doubleStruck(input: String): String =
        mapRange(
            input,
            upperStart = 0x1D538,
            lowerStart = 0x1D552,
            digitStart = 0x1D7D8,
            exceptions = mapOf(
                'C' to 0x2102,
                'H' to 0x210D,
                'N' to 0x2115,
                'P' to 0x2119,
                'Q' to 0x211A,
                'R' to 0x211D,
                'Z' to 0x2124
            )
        )

    fun monospace(input: String): String =
        mapRange(input, upperStart = 0x1D670, lowerStart = 0x1D68A, digitStart = 0x1D7F6)

    fun fullwidth(input: String): String =
        mapRange(input, upperStart = 0xFF21, lowerStart = 0xFF41, digitStart = 0xFF10)

    fun bubbleLight(input: String): String =
        mapRange(
            input,
            upperStart = 0x24B6,
            lowerStart = 0x24D0,
            exceptions = mapOf('0' to 0x24EA)
        ).let { mapped ->
            val sb = StringBuilder(mapped.length)
            var i = 0
            while (i < mapped.length) {
                val cp = mapped.codePointAt(i)
                when (cp) {
                    '0'.code -> sb.appendCodePoint(0x24EA)
                    in '1'.code..'9'.code -> sb.appendCodePoint(0x2460 + (cp - '1'.code))
                    else -> sb.appendCodePoint(cp)
                }
                i += Character.charCount(cp)
            }
            sb.toString()
        }

    fun bubbleDark(input: String): String =
        mapRange(
            input,
            upperStart = 0x1F150,
            lowerStart = 0x1F150,
            uppercaseBeforeMap = true
        ).let { mapped ->
            val sb = StringBuilder(mapped.length)
            var i = 0
            while (i < mapped.length) {
                val cp = mapped.codePointAt(i)
                when (cp) {
                    '0'.code -> sb.appendCodePoint(0x24FF)
                    in '1'.code..'9'.code -> sb.appendCodePoint(0x2776 + (cp - '1'.code))
                    else -> sb.appendCodePoint(cp)
                }
                i += Character.charCount(cp)
            }
            sb.toString()
        }

    fun squareLight(input: String): String =
        mapRange(input, upperStart = 0x1F130, lowerStart = 0x1F130, uppercaseBeforeMap = true)

    fun squareDark(input: String): String =
        mapRange(input, upperStart = 0x1F170, lowerStart = 0x1F170, uppercaseBeforeMap = true)

    fun parenthesized(input: String): String =
        mapRange(input, upperStart = 0x1F110, lowerStart = 0x249C)

    fun regionalBlocks(input: String): String {
        if (input.isEmpty()) return input
        val sb = StringBuilder(input.length * 3)
        var i = 0
        while (i < input.length) {
            val cp = input.codePointAt(i)
            val upperCp = if (cp in 'a'.code..'z'.code) cp - 'a'.code + 'A'.code else cp
            if (upperCp in 'A'.code..'Z'.code) {
                sb.appendCodePoint(0x1F1E6 + (upperCp - 'A'.code))
                sb.append('\u2009')
            } else {
                sb.appendCodePoint(cp)
            }
            i += Character.charCount(cp)
        }
        return sb.toString().trimEnd('\u2009')
    }

    fun smallCaps(input: String): String =
        mapTable(input, UPPER_STANDARD, SMALL_CAPS_LOWER, SUB_DIGITS)

    fun superscriptMini(input: String): String =
        mapTable(input, SUPERSCRIPT_UPPER, SUPERSCRIPT_LOWER, SUPERSCRIPT_DIGITS)

    fun subscriptTiny(input: String): String =
        mapTable(input, SUBSCRIPT_LETTERS, SUBSCRIPT_LETTERS, SUB_DIGITS)

    fun greekAesthetic(input: String): String =
        mapTable(input, GREEK_UPPER, GREEK_LOWER)

    fun curvyCute(input: String): String =
        mapTable(input, CURVY_UPPER, CURVY_LOWER)

    fun runicNordic(input: String): String =
        mapTable(input, RUNIC_LETTERS, RUNIC_LETTERS)

    fun cyberLeet(input: String): String =
        mapTable(input, CYBER_UPPER, CYBER_LOWER, CYBER_DIGITS)

    fun doubleTopDot(input: String): String =
        mapTable(input, DOT_UPPER, DOT_LOWER)

    fun slashAlt(input: String): String =
        mapTable(input, SLASH_UPPER, SLASH_LOWER)

    fun currencyBling(input: String): String =
        mapTable(input, BLING_UPPER, BLING_LOWER)

    // ---- Table Definitions ----

    private val UPPER_STANDARD = ('A'..'Z').map { it.toString() }

    private val SMALL_CAPS_LOWER = listOf(
        "ᴀ", "ʙ", "ᴄ", "ᴅ", "ᴇ", "ꜰ", "ɢ", "ʜ", "ɪ", "ᴊ", "ᴋ", "ʟ", "ᴍ",
        "ɴ", "ᴏ", "ᴘ", "ǫ", "ʀ", "ꜱ", "ᴛ", "ᴜ", "ᴠ", "ᴡ", "x", "ʏ", "ᴢ"
    )

    private val SUPERSCRIPT_UPPER = listOf(
        "ᴬ", "ᴮ", "ᶜ", "ᴰ", "ᴱ", "ᶠ", "ᴳ", "ᴴ", "ᴵ", "ᴶ", "ᴷ", "ᴸ", "ᴹ",
        "ᴺ", "ᴼ", "ᴾ", "Q", "ᴿ", "ˢ", "ᵀ", "ᵁ", "ⱽ", "ᵂ", "ˣ", "ʸ", "ᶻ"
    )

    private val SUPERSCRIPT_LOWER = listOf(
        "ᵃ", "ᵇ", "ᶜ", "ᵈ", "ᵉ", "ᶠ", "ᵍ", "ʰ", "ⁱ", "ʲ", "ᵏ", "ˡ", "ᵐ",
        "ⁿ", "ᵒ", "ᵖ", "q", "ʳ", "ˢ", "ᵗ", "ᵘ", "ᵛ", "ʷ", "ˣ", "ʸ", "ᶻ"
    )

    private val SUPERSCRIPT_DIGITS = listOf("⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹")

    private val SUBSCRIPT_LETTERS = listOf(
        "ₐ", "b", "c", "d", "ₑ", "f", "g", "ₕ", "ᵢ", "ⱼ", "ₖ", "ₗ", "ₘ",
        "ₙ", "ₒ", "ₚ", "q", "ᵣ", "ₛ", "ₜ", "ᵤ", "ᵥ", "w", "ₓ", "y", "z"
    )

    private val SUB_DIGITS = listOf("₀", "₁", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉")

    private val GREEK_UPPER = listOf(
        "Α", "Β", "¢", "Đ", "Ξ", "₣", "Ǥ", "Ħ", "ł", "J", "Ҝ", "Ł", "Μ",
        "Й", "Ø", "₱", "Q", "Я", "Ş", "Ŧ", "Ц", "V", "Ш", "Ж", "¥", "Ƶ"
    )

    private val GREEK_LOWER = listOf(
        "α", "в", "¢", "∂", "є", "ƒ", "g", "н", "ι", "נ", "к", "ℓ", "м",
        "η", "σ", "ρ", "q", "я", "ѕ", "т", "υ", "ν", "ω", "χ", "у", "z"
    )

    private val CURVY_UPPER = listOf(
        "Ǟ", "ɮ", "Ƈ", "Ɗ", "Ɛ", "Ƒ", "Ɠ", "Ƕ", "Ȉ", "Ɉ", "Ӄ", "Ŀ", "Ϻ",
        "Ƞ", "Ö", "Ƥ", "Ǫ", "Ʀ", "Ş", "Ʈ", "Ȕ", "Ʋ", "Ɯ", "Ӿ", "Ÿ", "Ȥ"
    )

    private val CURVY_LOWER = listOf(
        "ǟ", "ɮ", "ƈ", "ɖ", "ɛ", "ʄ", "ɢ", "ɦ", "ɨ", "ʝ", "ӄ", "ʟ", "ʍ",
        "ռ", "օ", "ք", "զ", "ʀ", "ֆ", "ȶ", "ʊ", "ʋ", "ա", "Ӽ", "ʏ", "ʐ"
    )

    private val RUNIC_LETTERS = listOf(
        "ᗩ", "ᗷ", "ᑕ", "ᗪ", "E", "ᖴ", "G", "ᕼ", "I", "ᒍ", "K", "ᒪ", "ᗰ",
        "ᑎ", "O", "ᑭ", "ᑫ", "ᖇ", "S", "T", "ᑌ", "ᐯ", "ᗯ", "᙭", "Y", "ᘔ"
    )

    private val CYBER_UPPER = listOf(
        "Λ", "ß", "₡", "Ð", "Ξ", "Ŧ", "₲", "Ħ", "Ī", "J", "Ҝ", "Ł", "M",
        "И", "Ø", "₱", "Q", "Я", "§", "₮", "Ʉ", "V", "₩", "Ж", "¥", "Ƶ"
    )

    private val CYBER_LOWER = listOf(
        "4", "8", "c", "d", "3", "f", "6", "h", "1", "j", "k", "l", "m",
        "n", "0", "p", "q", "r", "5", "7", "u", "v", "w", "x", "y", "2"
    )

    private val CYBER_DIGITS = listOf("⓪", "①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨")

    private val DOT_UPPER = listOf(
        "Ä", "Ḃ", "Ċ", "Ḋ", "Ë", "Ḟ", "Ġ", "Ḧ", "Ï", "J", "Ḳ", "Ḷ", "Ṁ",
        "Ṅ", "Ö", "Ṗ", "Q", "Ṙ", "Ṡ", "Ṫ", "Ü", "Ṿ", "Ẅ", "Ẍ", "Ÿ", "Ż"
    )

    private val DOT_LOWER = listOf(
        "ä", "ḃ", "ċ", "ḋ", "ë", "ḟ", "ġ", "ḧ", "ï", "j", "ḳ", "ḷ", "ṁ",
        "ṅ", "ö", "ṗ", "q", "ṙ", "ṡ", "ṫ", "ü", "ṿ", "ẅ", "ẍ", "ÿ", "ż"
    )

    private val SLASH_UPPER = listOf(
        "Ⱥ", "Ƀ", "Ȼ", "Đ", "Ɇ", "₣", "Ǥ", "Ħ", "Ɨ", "Ɉ", "Ꝁ", "Ł", "Ɱ",
        "₦", "Ø", "Ᵽ", "Ꝗ", "Ɍ", "Ş", "Ŧ", "Ʉ", "Ṽ", "₩", "Ӿ", "Ɏ", "Ƶ"
    )

    private val SLASH_LOWER = listOf(
        "ⱥ", "ƀ", "ȼ", "đ", "ɇ", "ƒ", "ǥ", "ħ", "ɨ", "ɉ", "ꝁ", "ł", "ɱ",
        "ɲ", "ø", "ᵽ", "ꝗ", "ɍ", "ş", "ŧ", "ʉ", "ṽ", "₩", "ӿ", "ɏ", "ƶ"
    )

    private val BLING_UPPER = listOf(
        "₳", "฿", "₵", "Đ", "Ɇ", "₣", "₲", "Ħ", "ł", "J", "₭", "Ł", "₥",
        "₦", "Ø", "₱", "Q", "₹", "₴", "₮", "Ʉ", "V", "₩", "Ӿ", "¥", "Ƶ"
    )

    private val BLING_LOWER = listOf(
        "₳", "฿", "₵", "đ", "ɇ", "ƒ", "₲", "ħ", "ɨ", "ʝ", "₭", "ł", "₥",
        "₦", "ø", "₱", "q", "₹", "₴", "ŧ", "ʉ", "v", "₩", "ӿ", "¥", "ƶ"
    )

    private val UPSIDE_DOWN_MAP: Map<Char, String> = mapOf(
        'a' to "ɐ", 'b' to "q", 'c' to "ɔ", 'd' to "p", 'e' to "ǝ",
        'f' to "ɟ", 'g' to "ƃ", 'h' to "ɥ", 'i' to "ᴉ", 'j' to "ɾ",
        'k' to "ʞ", 'l' to "l", 'm' to "ɯ", 'n' to "u", 'o' to "o",
        'p' to "d", 'q' to "b", 'r' to "ɹ", 's' to "s", 't' to "ʇ",
        'u' to "n", 'v' to "ʌ", 'w' to "ʍ", 'x' to "x", 'y' to "ʎ",
        'z' to "z",
        'A' to "∀", 'B' to "𐐒", 'C' to "Ɔ", 'D' to "ᗡ", 'E' to "Ǝ",
        'F' to "Ⅎ", 'G' to "פ", 'H' to "H", 'I' to "I", 'J' to "ſ",
        'K' to "ʞ", 'L' to "˥", 'M' to "W", 'N' to "N", 'O' to "O",
        'P' to "Ԁ", 'Q' to "Q", 'R' to "ᴚ", 'S' to "S", 'T' to "⊥",
        'U' to "∩", 'V' to "Λ", 'W' to "M", 'X' to "X", 'Y' to "⅄",
        'Z' to "Z",
        '1' to "Ɩ", '2' to "ᄅ", '3' to "Ɛ", '4' to "ㄣ", '5' to "ϛ",
        '6' to "9", '7' to "ㄥ", '8' to "8", '9' to "6", '0' to "0",
        '!' to "¡", '?' to "¿", '.' to "˙", ',' to "'"
    )
}
