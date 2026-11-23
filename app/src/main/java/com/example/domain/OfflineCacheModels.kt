package com.example.domain

enum class CachedEntrySource(val label: String, val isTemplate: Boolean) {
    FONT("Font Style", false),
    TEMPLATE("Template", true),
    OCCASION("Occasion", true),
    DECORATED("Custom Decor", false),
    BIO("Bio", false),
    USERNAME("Username", false),
    NAME("Name Design", false),
    STATUS("WA Status", false);

    companion object {
        fun fromStored(value: String): CachedEntrySource {
            return entries.firstOrNull { it.name == value } ?: FONT
        }
    }
}

data class CachedStyleEntry(
    val id: String,
    val title: String,
    val inputText: String,
    val styledText: String,
    val source: CachedEntrySource,
    val templateId: String? = null,
    val fontId: String? = null,
    val wingId: String? = null,
    val borderId: String? = null,
    val separatorId: String? = null,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class StudioDraftSnapshot(
    val fontInputText: String? = null,
    val decoratorInput: String? = null,
    val decoratorFontId: String? = null,
    val decoratorWingId: String? = null,
    val decoratorBorderId: String? = null,
    val decoratorSeparatorId: String? = null,
    val bioHeadline: String? = null,
    val bioBodyText: String? = null,
    val usernameInput: String? = null,
    val nameDesignerName: String? = null,
    val nameDesignerSubtitle: String? = null,
    val waHeaderTag: String? = null,
    val waBodyText: String? = null
)
