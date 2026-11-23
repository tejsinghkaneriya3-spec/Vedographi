package com.example.domain

/**
 * Curated catalog of 220 distinct Unicode English font transformations across 10 core categories.
 * Every transformation passes Devanagari (Hindi) characters cleanly without corruption.
 */
object FontCatalog {

    private fun wrapped(
        id: String,
        name: String,
        category: FontCategory,
        prefix: String,
        suffix: String,
        trending: Boolean = false,
        badge: String? = null,
        base: (String) -> String
    ) = FontStyleSpec(
        id = id,
        name = name,
        category = category,
        isTrending = trending,
        badge = badge,
        transform = { input -> "$prefix${base(input)}$suffix" }
    )

    val allStyles: List<FontStyleSpec> = listOf(
        // =========================================================
        // 1. STYLISH (22 styles)
        // =========================================================
        FontStyleSpec("monospace", "Typewriter Mono", FontCategory.STYLISH, isTrending = true) {
            UnicodeFontEngine.monospace(it)
        },
        FontStyleSpec("fullwidth", "Vaporwave Wide", FontCategory.STYLISH, isTrending = true, badge = "AESTHETIC") {
            UnicodeFontEngine.fullwidth(it)
        },
        FontStyleSpec("greek_aesthetic", "Greek Fusion", FontCategory.STYLISH, isTrending = true) {
            UnicodeFontEngine.greekAesthetic(it)
        },
        FontStyleSpec("runic_nordic", "Nordic Runes", FontCategory.STYLISH) {
            UnicodeFontEngine.runicNordic(it)
        },
        FontStyleSpec("cyber_leet", "Cyber Hacker", FontCategory.STYLISH) {
            UnicodeFontEngine.cyberLeet(it)
        },
        FontStyleSpec("currency_bling", "Rupee & Bling", FontCategory.STYLISH, badge = "₹") {
            UnicodeFontEngine.currencyBling(it)
        },
        FontStyleSpec("upside_down", "Upside Down Flip", FontCategory.STYLISH, isTrending = true) {
            UnicodeFontEngine.upsideDownSafe(it)
        },
        FontStyleSpec("stylish_alt_case", "Alternating Case", FontCategory.STYLISH) {
            UnicodeFontEngine.alternatingCase(it, startUpper = true)
        },
        FontStyleSpec("stylish_vapor_spaced", "Spaced Vaporwave", FontCategory.STYLISH) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.fullwidth(it), " ")
        },
        FontStyleSpec("stylish_mono_spaced", "Spaced Typewriter", FontCategory.STYLISH) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.monospace(it), " ")
        },
        wrapped("mono_terminal", "Terminal Prompt", FontCategory.STYLISH, ">_ ", " //") {
            UnicodeFontEngine.monospace(it)
        },
        wrapped("mono_brackets", "Code Block Tag", FontCategory.STYLISH, "< ", " />") {
            UnicodeFontEngine.monospace(it)
        },
        wrapped("cyber_glitch", "Matrix Glitch", FontCategory.STYLISH, "【⚡", "⚡】") {
            UnicodeFontEngine.cyberLeet(it)
        },
        wrapped("runic_clan", "Esports Clan Tag", FontCategory.STYLISH, "么 ", " ツ") {
            UnicodeFontEngine.runicNordic(it)
        },
        wrapped("greek_aura", "Neon Greek", FontCategory.STYLISH, "◈ ", " ◈") {
            UnicodeFontEngine.greekAesthetic(it)
        },
        wrapped("mono_wave", "Lo-Fi Console", FontCategory.STYLISH, "▌ ", " ▐") {
            UnicodeFontEngine.monospace(it)
        },
        wrapped("stylish_slash_alt", "Cyber Slashed", FontCategory.STYLISH, "// ", " //") {
            UnicodeFontEngine.slashAlt(it)
        },
        wrapped("stylish_hex_mono", "Hexagon Mono", FontCategory.STYLISH, "⬡ ", " ⬡") {
            UnicodeFontEngine.monospace(it)
        },
        wrapped("stylish_wide_bar", "Synthwave Grid", FontCategory.STYLISH, "░▒ ", " ▒░") {
            UnicodeFontEngine.fullwidth(it)
        },
        wrapped("stylish_bling_crown", "Richie Rich", FontCategory.STYLISH, "💸 ", " 💸") {
            UnicodeFontEngine.currencyBling(it)
        },
        wrapped("stylish_greek_star", "Olympus Star", FontCategory.STYLISH, "✦ ", " ✦") {
            UnicodeFontEngine.greekAesthetic(it)
        },
        wrapped("stylish_mono_underline", "Underlined Mono", FontCategory.STYLISH, "", "") {
            UnicodeFontEngine.combineAscii(UnicodeFontEngine.monospace(it), 0x0332)
        },

        // =========================================================
        // 2. BOLD (20 styles)
        // =========================================================
        FontStyleSpec("sans_bold", "Modern Sans Bold", FontCategory.BOLD, isTrending = true, badge = "HOT") {
            UnicodeFontEngine.sansBold(it)
        },
        FontStyleSpec("sans_bold_italic", "Dynamic Bold Italic", FontCategory.BOLD, isTrending = true) {
            UnicodeFontEngine.sansBoldItalic(it)
        },
        FontStyleSpec("bold_shout_caps", "All Caps Heavy", FontCategory.BOLD) {
            UnicodeFontEngine.sansBold(it.uppercase())
        },
        wrapped("bold_crown", "King Sans Bold", FontCategory.BOLD, "♛ ", " ♛", trending = true) {
            UnicodeFontEngine.sansBold(it)
        },
        wrapped("bold_diamonds", "Diamond Sans Bold", FontCategory.BOLD, "◆ ", " ◆") {
            UnicodeFontEngine.sansBold(it)
        },
        wrapped("bold_brackets", "Heavy Bracket Bold", FontCategory.BOLD, "【 ", " 】") {
            UnicodeFontEngine.sansBold(it)
        },
        wrapped("bold_slash_wing", "Slash Wing Bold", FontCategory.BOLD, "彡 ", " 彡") {
            UnicodeFontEngine.sansBold(it)
        },
        wrapped("bold_upper", "Thunder Shout Caps", FontCategory.BOLD, "⚡ ", " ⚡") {
            UnicodeFontEngine.sansBold(it.uppercase())
        },
        FontStyleSpec("bold_underline", "Bold Underline", FontCategory.BOLD) {
            UnicodeFontEngine.combineAscii(UnicodeFontEngine.sansBold(it), 0x0332)
        },
        FontStyleSpec("bold_double_underline", "Bold Double Line", FontCategory.BOLD) {
            UnicodeFontEngine.combineAscii(UnicodeFontEngine.sansBold(it), 0x0333)
        },
        FontStyleSpec("bold_strike", "Bold Strikethrough", FontCategory.BOLD) {
            UnicodeFontEngine.combineAscii(UnicodeFontEngine.sansBold(it), 0x0336)
        },
        FontStyleSpec("bold_spaced", "Wide Impact Bold", FontCategory.BOLD) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.sansBold(it), " ")
        },
        wrapped("bold_fire", "Blaze Sans Bold", FontCategory.BOLD, "🔥 ", " 🔥") {
            UnicodeFontEngine.sansBold(it)
        },
        wrapped("bold_star_pair", "Star Power Bold", FontCategory.BOLD, "★ ", " ★") {
            UnicodeFontEngine.sansBold(it)
        },
        wrapped("bold_arrow_italic", "Turbo Bold Italic", FontCategory.BOLD, "➤ ", " ◄") {
            UnicodeFontEngine.sansBoldItalic(it)
        },
        wrapped("bold_check", "Verified Heavy", FontCategory.BOLD, "✦ ", " ☑") {
            UnicodeFontEngine.sansBold(it)
        },
        wrapped("bold_box_bar", "Steel Bar Bold", FontCategory.BOLD, "▐ ", " ▌") {
            UnicodeFontEngine.sansBold(it)
        },
        wrapped("bold_corner_frame", "Corner Bold", FontCategory.BOLD, "『 ", " 』") {
            UnicodeFontEngine.sansBold(it)
        },
        wrapped("bold_pulse", "Pulse Italic Bold", FontCategory.BOLD, "〰 ", " 〰") {
            UnicodeFontEngine.sansBoldItalic(it)
        },
        wrapped("bold_shield", "Titan Shield", FontCategory.BOLD, "❖ ", " ❖") {
            UnicodeFontEngine.sansBold(it.uppercase())
        },

        // =========================================================
        // 3. CURSIVE (20 styles)
        // =========================================================
        FontStyleSpec("script_cursive", "Signature Cursive", FontCategory.CURSIVE, isTrending = true, badge = "POPULAR") {
            UnicodeFontEngine.scriptCursive(it)
        },
        FontStyleSpec("bold_script", "Royal Calligraphy", FontCategory.CURSIVE, isTrending = true, badge = "HOT") {
            UnicodeFontEngine.boldScript(it)
        },
        FontStyleSpec("curvy_cute", "Velvet Curvy", FontCategory.CURSIVE) {
            UnicodeFontEngine.curvyCute(it)
        },
        wrapped("script_heart", "Love Calligraphy", FontCategory.CURSIVE, "♡ ", " ♡", trending = true) {
            UnicodeFontEngine.boldScript(it)
        },
        wrapped("script_wings", "Angel Wing Script", FontCategory.CURSIVE, "꧁ ", " ꧂", trending = true) {
            UnicodeFontEngine.boldScript(it)
        },
        wrapped("script_butterfly", "Butterfly Script", FontCategory.CURSIVE, "ʚïɞ ", " ʚïɞ") {
            UnicodeFontEngine.scriptCursive(it)
        },
        wrapped("script_rose", "Rose Petal Script", FontCategory.CURSIVE, "✿ ", " ✿") {
            UnicodeFontEngine.boldScript(it)
        },
        wrapped("script_moon", "Crescent Script", FontCategory.CURSIVE, "☾ ", " ☽") {
            UnicodeFontEngine.scriptCursive(it)
        },
        wrapped("script_ribbon", "Ribbon Swirl", FontCategory.CURSIVE, "୨୧ ", " ୨୧") {
            UnicodeFontEngine.scriptCursive(it)
        },
        wrapped("script_star", "Starlight Script", FontCategory.CURSIVE, "✧･ﾟ: * ", " *:･ﾟ✧") {
            UnicodeFontEngine.boldScript(it)
        },
        wrapped("script_feather", "Quill Feather", FontCategory.CURSIVE, "❦ ", " ❦") {
            UnicodeFontEngine.scriptCursive(it)
        },
        wrapped("script_crown", "Queen Calligraphy", FontCategory.CURSIVE, "♕ ", " ♕") {
            UnicodeFontEngine.boldScript(it)
        },
        wrapped("curvy_sparkle", "Glitter Curvy", FontCategory.CURSIVE, "✨ ", " ✨") {
            UnicodeFontEngine.curvyCute(it)
        },
        wrapped("script_vine", "Floral Vine Script", FontCategory.CURSIVE, "༺❀༻ ", " ༺❀༻") {
            UnicodeFontEngine.scriptCursive(it)
        },
        wrapped("script_pearl", "Pearl Loop Script", FontCategory.CURSIVE, "∘˚˳° ", " °˳˚∘") {
            UnicodeFontEngine.boldScript(it)
        },
        wrapped("script_wave", "Ocean Wave Script", FontCategory.CURSIVE, "〰 ", " 〰") {
            UnicodeFontEngine.scriptCursive(it)
        },
        wrapped("script_arrow_love", "Cupid Calligraphy", FontCategory.CURSIVE, "»——❥ ", " ❥——«") {
            UnicodeFontEngine.boldScript(it)
        },
        wrapped("script_tulip", "Lotus Calligraphy", FontCategory.CURSIVE, "🌷 ", " 🌷") {
            UnicodeFontEngine.boldScript(it)
        },
        wrapped("script_underline", "Underlined Script", FontCategory.CURSIVE, "", "") {
            UnicodeFontEngine.combineAscii(UnicodeFontEngine.boldScript(it), 0x0332)
        },
        wrapped("script_gold_swash", "Royal Swash", FontCategory.CURSIVE, "༺ ", " ༻") {
            UnicodeFontEngine.scriptCursive(it)
        },

        // =========================================================
        // 4. GOTHIC (20 styles)
        // =========================================================
        FontStyleSpec("fraktur_gothic", "Old English Gothic", FontCategory.GOTHIC, isTrending = true, badge = "CLASSIC") {
            UnicodeFontEngine.frakturGothic(it)
        },
        FontStyleSpec("bold_fraktur", "Dark Empire Bold", FontCategory.GOTHIC, isTrending = true, badge = "HOT") {
            UnicodeFontEngine.boldFraktur(it)
        },
        wrapped("gothic_lord", "Royal Trishul Gothic", FontCategory.GOTHIC, "꧁༒☬ ", " ☬༒꧂", trending = true) {
            UnicodeFontEngine.boldFraktur(it)
        },
        wrapped("gothic_sword", "Blade Knight", FontCategory.GOTHIC, "⚔ ", " ⚔") {
            UnicodeFontEngine.boldFraktur(it)
        },
        wrapped("gothic_skull", "Shadow Skull", FontCategory.GOTHIC, "☠ ", " ☠") {
            UnicodeFontEngine.frakturGothic(it)
        },
        wrapped("gothic_bat", "Midnight Fang", FontCategory.GOTHIC, "༺༒ ", " ༒༻") {
            UnicodeFontEngine.boldFraktur(it)
        },
        wrapped("gothic_cross", "Vampire Crest", FontCategory.GOTHIC, "† ", " †") {
            UnicodeFontEngine.frakturGothic(it)
        },
        wrapped("gothic_crown", "Dark Monarch", FontCategory.GOTHIC, "♚ ", " ♚") {
            UnicodeFontEngine.boldFraktur(it)
        },
        wrapped("gothic_spade", "Ace of Spades", FontCategory.GOTHIC, "♠ ", " ♠") {
            UnicodeFontEngine.frakturGothic(it)
        },
        wrapped("gothic_dragon", "Dragon Clan Gothic", FontCategory.GOTHIC, "乡 ", " 乡") {
            UnicodeFontEngine.boldFraktur(it)
        },
        wrapped("gothic_slash", "Bloodline Gothic", FontCategory.GOTHIC, "◤ ", " ◢") {
            UnicodeFontEngine.boldFraktur(it)
        },
        wrapped("gothic_pent", "Arcane Seal", FontCategory.GOTHIC, "⍟ ", " ⍟") {
            UnicodeFontEngine.frakturGothic(it)
        },
        wrapped("gothic_flame", "Inferno Gothic", FontCategory.GOTHIC, "🔥 ", " 🔥") {
            UnicodeFontEngine.boldFraktur(it)
        },
        wrapped("gothic_thunder", "Storm Fraktur", FontCategory.GOTHIC, "⚡ ", " ⚡") {
            UnicodeFontEngine.frakturGothic(it)
        },
        wrapped("gothic_obsidian", "Obsidian Frame", FontCategory.GOTHIC, "▓▒░ ", " ░▒▓") {
            UnicodeFontEngine.boldFraktur(it)
        },
        wrapped("gothic_phantom", "Phantom Crest", FontCategory.GOTHIC, "◥ ", " ◤") {
            UnicodeFontEngine.boldFraktur(it)
        },
        wrapped("gothic_wolf", "Lone Wolf Gothic", FontCategory.GOTHIC, "⧼ ", " ⧽") {
            UnicodeFontEngine.frakturGothic(it)
        },
        wrapped("gothic_eclipse", "Dark Eclipse", FontCategory.GOTHIC, "🌑 ", " 🌑") {
            UnicodeFontEngine.boldFraktur(it)
        },
        wrapped("gothic_club", "Royal Club", FontCategory.GOTHIC, "♣ ", " ♣") {
            UnicodeFontEngine.frakturGothic(it)
        },
        FontStyleSpec("gothic_spaced", "Wide Fraktur", FontCategory.GOTHIC) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.boldFraktur(it), " ")
        },

        // =========================================================
        // 5. SERIF (20 styles)
        // =========================================================
        FontStyleSpec("serif_bold", "Royal Serif Bold", FontCategory.SERIF, isTrending = true, badge = "HOT") {
            UnicodeFontEngine.serifBold(it)
        },
        FontStyleSpec("serif_italic", "Classic Serif Italic", FontCategory.SERIF) {
            UnicodeFontEngine.serifItalic(it)
        },
        FontStyleSpec("serif_bold_italic", "Imperial Bold Italic", FontCategory.SERIF, isTrending = true) {
            UnicodeFontEngine.serifBoldItalic(it)
        },
        FontStyleSpec("sans_italic", "Editorial Italic", FontCategory.SERIF) {
            UnicodeFontEngine.sansItalic(it)
        },
        FontStyleSpec("serif_underline", "Serif Underline", FontCategory.SERIF) {
            UnicodeFontEngine.combineAscii(UnicodeFontEngine.serifBold(it), 0x0332)
        },
        FontStyleSpec("serif_double_underline", "Serif Double Rule", FontCategory.SERIF) {
            UnicodeFontEngine.combineAscii(UnicodeFontEngine.serifBold(it), 0x0333)
        },
        FontStyleSpec("serif_italic_underline", "Poetic Underline", FontCategory.SERIF) {
            UnicodeFontEngine.combineAscii(UnicodeFontEngine.serifItalic(it), 0x0332)
        },
        FontStyleSpec("serif_spaced", "Vogue Serif Spaced", FontCategory.SERIF) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.serifBold(it), " ")
        },
        wrapped("serif_crown", "Monarch Serif", FontCategory.SERIF, "♛ ", " ♛") {
            UnicodeFontEngine.serifBold(it)
        },
        wrapped("serif_spark", "Sparkle Serif Italic", FontCategory.SERIF, "✦ ", " ✦") {
            UnicodeFontEngine.serifBoldItalic(it)
        },
        wrapped("serif_quotes", "Classic Quoted", FontCategory.SERIF, "❝ ", " ❞") {
            UnicodeFontEngine.serifItalic(it)
        },
        wrapped("serif_pillar", "Roman Pillar", FontCategory.SERIF, "║ ", " ║") {
            UnicodeFontEngine.serifBold(it)
        },
        wrapped("serif_diamond", "Diamond Editorial", FontCategory.SERIF, "◇ ", " ◇") {
            UnicodeFontEngine.serifBoldItalic(it)
        },
        wrapped("serif_floral", "Botanical Serif", FontCategory.SERIF, "✾━━ ", " ━━✾") {
            UnicodeFontEngine.serifItalic(it)
        },
        wrapped("serif_brackets", "Bookplate Serif", FontCategory.SERIF, "〔 ", " 〕") {
            UnicodeFontEngine.serifBold(it)
        },
        wrapped("serif_surya", "Surya Serif", FontCategory.SERIF, "☼ ", " ☼") {
            UnicodeFontEngine.serifBold(it)
        },
        wrapped("serif_ribbon", "Vintage Ribbon", FontCategory.SERIF, "—✦ ", " ✦—") {
            UnicodeFontEngine.serifBoldItalic(it)
        },
        wrapped("serif_italic_moon", "Lunar Editorial", FontCategory.SERIF, "☾ ", " ☽") {
            UnicodeFontEngine.serifItalic(it)
        },
        wrapped("serif_upper_bold", "Monumental Caps", FontCategory.SERIF, "", "") {
            UnicodeFontEngine.serifBold(it.uppercase())
        },
        FontStyleSpec("serif_strike", "Redacted Serif", FontCategory.SERIF) {
            UnicodeFontEngine.combineAscii(UnicodeFontEngine.serifBold(it), 0x0336)
        },

        // =========================================================
        // 6. BUBBLE (22 styles)
        // =========================================================
        FontStyleSpec("bubble_light", "Bubble Outline", FontCategory.BUBBLE, isTrending = true) {
            UnicodeFontEngine.bubbleLight(it)
        },
        FontStyleSpec("bubble_dark", "Bubble Filled", FontCategory.BUBBLE, isTrending = true, badge = "POP") {
            UnicodeFontEngine.bubbleDark(it)
        },
        FontStyleSpec("square_light", "Square Tile", FontCategory.BUBBLE) {
            UnicodeFontEngine.squareLight(it)
        },
        FontStyleSpec("square_dark", "Negative Block", FontCategory.BUBBLE, isTrending = true) {
            UnicodeFontEngine.squareDark(it)
        },
        FontStyleSpec("parenthesized", "Parenthesized", FontCategory.BUBBLE) {
            UnicodeFontEngine.parenthesized(it)
        },
        FontStyleSpec("regional_blocks", "Blue Keycaps", FontCategory.BUBBLE) {
            UnicodeFontEngine.regionalBlocks(it)
        },
        wrapped("bubble_cloud", "Cloud Bubbles", FontCategory.BUBBLE, "☁ ", " ☁") {
            UnicodeFontEngine.bubbleLight(it)
        },
        wrapped("bubble_dark_star", "Night Bubble", FontCategory.BUBBLE, "★ ", " ★") {
            UnicodeFontEngine.bubbleDark(it)
        },
        wrapped("square_bracket", "Pixel Boxed", FontCategory.BUBBLE, "『 ", " 』") {
            UnicodeFontEngine.squareLight(it)
        },
        wrapped("square_dark_fire", "Bold Tile Fire", FontCategory.BUBBLE, "◈ ", " ◈") {
            UnicodeFontEngine.squareDark(it)
        },
        wrapped("paren_pill", "Pill Tag", FontCategory.BUBBLE, "〔 ", " 〕") {
            UnicodeFontEngine.parenthesized(it)
        },
        wrapped("bubble_heart", "Candy Bubble", FontCategory.BUBBLE, "❥ ", " ❥") {
            UnicodeFontEngine.bubbleLight(it)
        },
        wrapped("square_arrow", "Arcade Frame", FontCategory.BUBBLE, "▶ ", " ◀") {
            UnicodeFontEngine.squareDark(it)
        },
        wrapped("bubble_spark", "Fizzy Pop", FontCategory.BUBBLE, "°• ", " •°") {
            UnicodeFontEngine.bubbleDark(it)
        },
        wrapped("square_corner", "Corner Grid", FontCategory.BUBBLE, "「 ", " 」") {
            UnicodeFontEngine.squareLight(it)
        },
        FontStyleSpec("bubble_spaced", "Spaced Bubbles", FontCategory.BUBBLE) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.bubbleLight(it), " ")
        },
        FontStyleSpec("bubble_dark_spaced", "Spaced Dark Bubbles", FontCategory.BUBBLE) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.bubbleDark(it), " ")
        },
        wrapped("bubble_crown", "Bubble Monarch", FontCategory.BUBBLE, "👑 ", " 👑") {
            UnicodeFontEngine.bubbleDark(it)
        },
        wrapped("square_verified", "Verified Tiles", FontCategory.BUBBLE, "✦ ", " ☑") {
            UnicodeFontEngine.squareDark(it)
        },
        wrapped("bubble_music", "Pop Beat Bubbles", FontCategory.BUBBLE, "🎵 ", " 🎵") {
            UnicodeFontEngine.bubbleLight(it)
        },
        wrapped("paren_flower", "Soft Blossom Tag", FontCategory.BUBBLE, "✿ ", " ✿") {
            UnicodeFontEngine.parenthesized(it)
        },
        wrapped("bubble_halo", "Halo Circle", FontCategory.BUBBLE, "○• ", " •○") {
            UnicodeFontEngine.bubbleLight(it)
        },

        // =========================================================
        // 7. SMALL CAPS (20 styles)
        // =========================================================
        FontStyleSpec("small_caps", "Small Caps Studio", FontCategory.SMALL_CAPS, isTrending = true, badge = "BIO") {
            UnicodeFontEngine.smallCaps(it)
        },
        FontStyleSpec("small_caps_spaced", "Spaced Small Caps", FontCategory.SMALL_CAPS) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.smallCaps(it), " ")
        },
        FontStyleSpec("superscript_mini", "Tiny Superscript", FontCategory.SMALL_CAPS) {
            UnicodeFontEngine.superscriptMini(it)
        },
        FontStyleSpec("subscript_tiny", "Micro Subscript", FontCategory.SMALL_CAPS) {
            UnicodeFontEngine.subscriptTiny(it)
        },
        wrapped("small_caps_dot", "Dotted Small Caps", FontCategory.SMALL_CAPS, "• ", " •") {
            UnicodeFontEngine.smallCaps(it)
        },
        wrapped("small_caps_crown", "Royal Small Caps", FontCategory.SMALL_CAPS, "♛ ", " ♛") {
            UnicodeFontEngine.smallCaps(it)
        },
        wrapped("small_caps_brackets", "Clean Bio Brackets", FontCategory.SMALL_CAPS, "『 ", " 』") {
            UnicodeFontEngine.smallCaps(it)
        },
        wrapped("small_caps_spark", "Sparkle Small Caps", FontCategory.SMALL_CAPS, "✦ ", " ✦") {
            UnicodeFontEngine.smallCaps(it)
        },
        wrapped("small_caps_heart", "Sweet Small Caps", FontCategory.SMALL_CAPS, "♡ ", " ♡") {
            UnicodeFontEngine.smallCaps(it)
        },
        wrapped("small_caps_ribbon", "Aesthetic Small Caps", FontCategory.SMALL_CAPS, "°•. ✿ ", " ✿ .•°") {
            UnicodeFontEngine.smallCaps(it)
        },
        FontStyleSpec("small_caps_underline", "Underlined Small Caps", FontCategory.SMALL_CAPS) {
            UnicodeFontEngine.combineAscii(UnicodeFontEngine.smallCaps(it), 0x0332)
        },
        wrapped("small_caps_verified", "Creator Handle", FontCategory.SMALL_CAPS, "@", " ☑") {
            UnicodeFontEngine.smallCaps(it)
        },
        wrapped("small_caps_moon", "Moonlit Small Caps", FontCategory.SMALL_CAPS, "☾ ", " ☽") {
            UnicodeFontEngine.smallCaps(it)
        },
        wrapped("small_caps_diamond", "Diamond Small Caps", FontCategory.SMALL_CAPS, "◈ ", " ◈") {
            UnicodeFontEngine.smallCaps(it)
        },
        wrapped("small_caps_arrow", "Arrow Small Caps", FontCategory.SMALL_CAPS, "➳ ", " ➳") {
            UnicodeFontEngine.smallCaps(it)
        },
        wrapped("small_caps_wing", "Winged Small Caps", FontCategory.SMALL_CAPS, "༺ ", " ༻") {
            UnicodeFontEngine.smallCaps(it)
        },
        wrapped("small_caps_pill", "Tag Pill Caps", FontCategory.SMALL_CAPS, "〔 ", " 〕") {
            UnicodeFontEngine.smallCaps(it)
        },
        wrapped("superscript_star", "Floating Star Mini", FontCategory.SMALL_CAPS, "✧ ", " ✧") {
            UnicodeFontEngine.superscriptMini(it)
        },
        wrapped("subscript_tag", "Low-Key Subscript", FontCategory.SMALL_CAPS, "— ", " —") {
            UnicodeFontEngine.subscriptTiny(it)
        },
        FontStyleSpec("small_caps_dot_join", "Beaded Small Caps", FontCategory.SMALL_CAPS) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.smallCaps(it), "•")
        },

        // =========================================================
        // 8. DOUBLE STRUCK (20 styles)
        // =========================================================
        FontStyleSpec("double_struck", "Double-Struck Classic", FontCategory.DOUBLE_STRUCK, isTrending = true, badge = "PRO") {
            UnicodeFontEngine.doubleStruck(it)
        },
        wrapped("double_star", "Starlight Double", FontCategory.DOUBLE_STRUCK, "✦ ", " ✦") {
            UnicodeFontEngine.doubleStruck(it)
        },
        wrapped("double_crown", "Imperial Double", FontCategory.DOUBLE_STRUCK, "♛ ", " ♛") {
            UnicodeFontEngine.doubleStruck(it)
        },
        wrapped("double_brackets", "Prisma Double", FontCategory.DOUBLE_STRUCK, "『 ", " 』") {
            UnicodeFontEngine.doubleStruck(it)
        },
        FontStyleSpec("double_spaced", "Airy Double-Struck", FontCategory.DOUBLE_STRUCK) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.doubleStruck(it), " ")
        },
        FontStyleSpec("double_underline", "Underlined Double", FontCategory.DOUBLE_STRUCK) {
            UnicodeFontEngine.combineAscii(UnicodeFontEngine.doubleStruck(it), 0x0332)
        },
        wrapped("double_wings", "Angel Double", FontCategory.DOUBLE_STRUCK, "༺ ", " ༻") {
            UnicodeFontEngine.doubleStruck(it)
        },
        wrapped("double_spark", "Cosmic Double", FontCategory.DOUBLE_STRUCK, "✴ ", " ✴") {
            UnicodeFontEngine.doubleStruck(it)
        },
        wrapped("double_arrow", "Target Double", FontCategory.DOUBLE_STRUCK, "➳ ", " ➳") {
            UnicodeFontEngine.doubleStruck(it)
        },
        wrapped("double_heart", "Pure Heart Double", FontCategory.DOUBLE_STRUCK, "♡ ", " ♡") {
            UnicodeFontEngine.doubleStruck(it)
        },
        wrapped("double_box", "Crystal Frame", FontCategory.DOUBLE_STRUCK, "❖ ", " ❖") {
            UnicodeFontEngine.doubleStruck(it)
        },
        wrapped("double_hex", "Tech Hex Double", FontCategory.DOUBLE_STRUCK, "⬡ ", " ⬡") {
            UnicodeFontEngine.doubleStruck(it)
        },
        wrapped("double_fire", "Blaze Double", FontCategory.DOUBLE_STRUCK, "🔥 ", " 🔥") {
            UnicodeFontEngine.doubleStruck(it)
        },
        wrapped("double_verified", "Verified Double", FontCategory.DOUBLE_STRUCK, "✦ ", " ☑") {
            UnicodeFontEngine.doubleStruck(it)
        },
        wrapped("double_diamond", "Diamond Double", FontCategory.DOUBLE_STRUCK, "💎 ", " 💎") {
            UnicodeFontEngine.doubleStruck(it)
        },
        wrapped("double_lightning", "Volt Double", FontCategory.DOUBLE_STRUCK, "⚡ ", " ⚡") {
            UnicodeFontEngine.doubleStruck(it)
        },
        wrapped("double_pill", "Bracketed Double", FontCategory.DOUBLE_STRUCK, "【 ", " 】") {
            UnicodeFontEngine.doubleStruck(it)
        },
        wrapped("double_moon", "Lunar Double", FontCategory.DOUBLE_STRUCK, "☾ ", " ☽") {
            UnicodeFontEngine.doubleStruck(it)
        },
        FontStyleSpec("double_upper", "Monument Double Caps", FontCategory.DOUBLE_STRUCK) {
            UnicodeFontEngine.doubleStruck(it.uppercase())
        },
        FontStyleSpec("double_strike", "Crossed Double", FontCategory.DOUBLE_STRUCK) {
            UnicodeFontEngine.combineAscii(UnicodeFontEngine.doubleStruck(it), 0x0336)
        },

        // =========================================================
        // 9. DECORATIVE (28 styles)
        // =========================================================
        FontStyleSpec("strikethrough", "Strikethrough", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x0336)
        },
        FontStyleSpec("slash_through", "Diagonal Slash", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x0338)
        },
        FontStyleSpec("underline_single", "Clean Underline", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x0332)
        },
        FontStyleSpec("underline_double", "Double Underline", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x0333)
        },
        FontStyleSpec("overline", "Overhead Bar", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x0305)
        },
        FontStyleSpec("dotted_aura", "Dotted Halo", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x0307, 0x0323)
        },
        FontStyleSpec("wavy_tilde", "Wavy Strike", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x0334)
        },
        FontStyleSpec("double_dot_top", "Umlaut Chic", FontCategory.DECORATIVE) {
            UnicodeFontEngine.doubleTopDot(it)
        },
        FontStyleSpec("slash_alt", "Slashed Letters", FontCategory.DECORATIVE) {
            UnicodeFontEngine.slashAlt(it)
        },
        FontStyleSpec("deco_ring_above", "Halo Ring Top", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x030A)
        },
        FontStyleSpec("deco_tilde_top", "Tilde Wave Top", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x0303)
        },
        FontStyleSpec("deco_short_stroke", "Short Crossbar", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x0335)
        },
        FontStyleSpec("deco_diaeresis_below", "Twin Dots Below", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x0324)
        },
        FontStyleSpec("deco_circumflex_below", "Chevron Below", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x032D)
        },
        FontStyleSpec("deco_square_below", "Pixel Base", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x033B)
        },
        FontStyleSpec("deco_seagull_below", "Winged Underscore", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x033C)
        },
        FontStyleSpec("deco_arrow_below", "Upward Arrow Base", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x034E)
        },
        FontStyleSpec("deco_arch_above", "Crown Arch Top", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x0311)
        },
        FontStyleSpec("deco_sandwich_bar", "Top & Bottom Bars", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x0305, 0x0332)
        },
        FontStyleSpec("deco_crosshatch", "Crossed Grid", FontCategory.DECORATIVE) {
            UnicodeFontEngine.combineAscii(it, 0x0336, 0x0338)
        },
        FontStyleSpec("lightning_join", "Lightning Joiner", FontCategory.DECORATIVE) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.sansBold(it), "⚡")
        },
        FontStyleSpec("dot_join", "Beaded Dot Joiner", FontCategory.DECORATIVE) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.smallCaps(it), "•")
        },
        FontStyleSpec("star_join", "Starlight Joiner", FontCategory.DECORATIVE) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.sansBold(it), "✦")
        },
        FontStyleSpec("heart_join", "Heartlink Letters", FontCategory.DECORATIVE) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.sansItalic(it), "♡")
        },
        FontStyleSpec("spaced_vapor", "Spaced Luxury", FontCategory.DECORATIVE) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.sansBold(it), "  ")
        },
        FontStyleSpec("deco_diamond_join", "Gemstone Chain", FontCategory.DECORATIVE) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.sansBold(it), "◆")
        },
        FontStyleSpec("deco_wave_join", "Tilde Ribbon Chain", FontCategory.DECORATIVE) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.sansBoldItalic(it), "~")
        },
        FontStyleSpec("deco_plus_join", "Cross Plus Chain", FontCategory.DECORATIVE) {
            UnicodeFontEngine.interleaveAscii(UnicodeFontEngine.monospace(it), "+")
        },

        // =========================================================
        // 10. FANCY (28 styles)
        // =========================================================
        wrapped("deco_maharaja", "Maharaja Royal", FontCategory.FANCY, "꧁༒☬ ", " ☬༒꧂", trending = true, badge = "DESI") {
            UnicodeFontEngine.serifBold(it)
        },
        wrapped("deco_shahi", "Shahi Swag", FontCategory.FANCY, "★彡 ", " 彡★", trending = true) {
            UnicodeFontEngine.sansBoldItalic(it)
        },
        wrapped("deco_insta_pro", "Insta Aesthetic", FontCategory.FANCY, "°•. ✿ ", " ✿ .•°") {
            UnicodeFontEngine.smallCaps(it)
        },
        wrapped("deco_bgmi_clan", "Pro Gamer Tag", FontCategory.FANCY, "乡『 ", " 』ツ") {
            UnicodeFontEngine.sansBold(it)
        },
        wrapped("deco_music", "Vibe Melody", FontCategory.FANCY, "♫♪.ılılıll ", " llılılı.♪♫") {
            UnicodeFontEngine.sansItalic(it)
        },
        wrapped("deco_arrow_love", "Cupid Arrow", FontCategory.FANCY, "»——❥ ", " ❥——«") {
            UnicodeFontEngine.boldScript(it)
        },
        wrapped("deco_floral", "Botanical Frame", FontCategory.FANCY, "✾━━ ", " ━━✾") {
            UnicodeFontEngine.serifItalic(it)
        },
        wrapped("deco_sparkle_bar", "Starburst Banner", FontCategory.FANCY, "✧･ﾟ: *✧ ", " ✧*:･ﾟ✧") {
            UnicodeFontEngine.sansBold(it)
        },
        wrapped("deco_sun", "Surya Radiant", FontCategory.FANCY, "☼ ", " ☼") {
            UnicodeFontEngine.serifBold(it)
        },
        wrapped("deco_lotus", "Padma Elegance", FontCategory.FANCY, "❀•° ", " °•❀") {
            UnicodeFontEngine.scriptCursive(it)
        },
        wrapped("deco_crown_king", "Attitude King", FontCategory.FANCY, "👑 ", " 👑") {
            UnicodeFontEngine.sansBold(it)
        },
        wrapped("deco_fire_squad", "Blaze Streak", FontCategory.FANCY, "🔥⚡ ", " ⚡🔥") {
            UnicodeFontEngine.sansBoldItalic(it)
        },
        wrapped("deco_diamond_lux", "Diamond Luxury", FontCategory.FANCY, "💎 ", " 💎") {
            UnicodeFontEngine.doubleStruck(it)
        },
        wrapped("deco_check_verified", "Verified Creator", FontCategory.FANCY, "✦ ", " ☑") {
            UnicodeFontEngine.sansBold(it)
        },
        wrapped("deco_zen", "Zen Minimal", FontCategory.FANCY, "〔 ✧ ", " ✧ 〕") {
            UnicodeFontEngine.monospace(it)
        },
        wrapped("deco_infinity", "Infinity Loop", FontCategory.FANCY, "∞ ", " ∞") {
            UnicodeFontEngine.boldScript(it)
        },
        wrapped("fancy_phoenix", "Phoenix Wings", FontCategory.FANCY, "༺🔥 ", " 🔥༻") {
            UnicodeFontEngine.boldFraktur(it)
        },
        wrapped("fancy_galaxy", "Galaxy Stardust", FontCategory.FANCY, "⋆｡°✩ ", " ✩°｡⋆") {
            UnicodeFontEngine.boldScript(it)
        },
        wrapped("fancy_samurai", "Samurai Katana", FontCategory.FANCY, "『⚔ ", " ⚔』") {
            UnicodeFontEngine.sansBold(it)
        },
        wrapped("fancy_cherry", "Sakura Blossom", FontCategory.FANCY, "🌸 ", " 🌸") {
            UnicodeFontEngine.scriptCursive(it)
        },
        wrapped("fancy_angel_halo", "Angelic Aura", FontCategory.FANCY, "°• ✞ ", " ✞ •°") {
            UnicodeFontEngine.serifBoldItalic(it)
        },
        wrapped("fancy_butterfly_dream", "Monarch Wing", FontCategory.FANCY, "🦋 ", " 🦋") {
            UnicodeFontEngine.curvyCute(it)
        },
        wrapped("fancy_thunder", "Thunder Strike", FontCategory.FANCY, "⚡彡 ", " 彡⚡") {
            UnicodeFontEngine.sansBoldItalic(it)
        },
        wrapped("fancy_royal_scroll", "Royal Decree", FontCategory.FANCY, "📜 ", " 📜") {
            UnicodeFontEngine.serifBold(it)
        },
        wrapped("fancy_midnight", "Midnight Crescent", FontCategory.FANCY, "🌙✨ ", " ✨🌙") {
            UnicodeFontEngine.boldScript(it)
        },
        wrapped("fancy_heart_queen", "Queen of Hearts", FontCategory.FANCY, "♥♕ ", " ♕♥") {
            UnicodeFontEngine.serifBold(it)
        },
        wrapped("fancy_cyber_pulse", "Neon Pulse", FontCategory.FANCY, "◈⚡ ", " ⚡◈") {
            UnicodeFontEngine.fullwidth(it)
        },
        wrapped("fancy_desi_swastik", "Shubh Swag", FontCategory.FANCY, "ॐ✦ ", " ✦ॐ") {
            UnicodeFontEngine.serifBold(it)
        }
    )

    fun findById(id: String): FontStyleSpec? = allStyles.firstOrNull { it.id == id }
}
