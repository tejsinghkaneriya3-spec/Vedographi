package com.example.domain

enum class FontCategory(val label: String) {
    ALL("All"),
    TRENDING("Trending"),
    FAVORITES("Favorites"),
    RECENT("Recent"),
    STYLISH("Stylish"),
    BOLD("Bold"),
    CURSIVE("Cursive"),
    GOTHIC("Gothic"),
    SERIF("Serif"),
    BUBBLE("Bubble"),
    SMALL_CAPS("Small Caps"),
    DOUBLE_STRUCK("Double Struck"),
    DECORATIVE("Decorative"),
    FANCY("Fancy")
}

data class FontStyleSpec(
    val id: String,
    val name: String,
    val category: FontCategory,
    val isTrending: Boolean = false,
    val badge: String? = null,
    val transform: (String) -> String
)

data class SavedCreation(
    val id: String,
    val title: String,
    val content: String,
    val kind: String // "BIO", "USERNAME", "DECORATED"
)

enum class SocialPlatform(val displayName: String, val packages: List<String>) {
    WHATSAPP("WhatsApp", listOf("com.whatsapp", "com.whatsapp.w4b")),
    INSTAGRAM("Instagram", listOf("com.instagram.android")),
    FACEBOOK("Facebook", listOf("com.facebook.katana", "com.facebook.orca")),
    TELEGRAM("Telegram", listOf("org.telegram.messenger", "org.telegram.plus")),
    MORE("More Apps", emptyList())
}
