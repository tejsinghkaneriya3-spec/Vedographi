package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CachedEntrySource
import com.example.domain.CachedStyleEntry
import com.example.domain.DecorationTemplate
import com.example.domain.FontCatalog
import com.example.domain.OccasionCategory
import com.example.domain.OccasionTemplate
import com.example.domain.SavedCreation
import com.example.domain.StudioGenerators
import com.example.domain.TextStudioCatalog
import com.example.ui.FontPreviewItem
import com.example.ui.components.VedographiContentCard
import com.example.ui.components.VedographiDefaults
import com.example.ui.components.VedographiHeroCard
import com.example.ui.components.VedographiPillBadge

private val StudioTabLabels = listOf(
    "50 Templates",
    "Customize",
    "Name Designer",
    "WA Status",
    "Occasions",
    "Saved"
)

@Composable
fun MyTextStudioScreen(
    selectedTab: Int,
    templateTagFilter: String,
    decoratorInput: String,
    selectedDecoratorFontId: String,
    selectedWingId: String,
    selectedBorderId: String,
    selectedSeparatorId: String,
    decoratedOutput: String,
    selectedOccasionCategory: OccasionCategory,
    nameDesignerName: String,
    nameDesignerSubtitle: String,
    nameDesignerFontId: String,
    nameDesignerWingId: String,
    nameDesignerBadge: String,
    designedNameOutput: String,
    waHeaderTag: String,
    waBodyText: String,
    waFontId: String,
    waDividerIdx: Int,
    waShowFooter: Boolean,
    designedWaStatusOutput: String,
    favoriteFonts: List<FontPreviewItem>,
    recentFonts: List<FontPreviewItem>,
    savedCreations: List<SavedCreation>,
    cachedStyles: List<CachedStyleEntry> = emptyList(),
    isGuest: Boolean = false,
    isOnline: Boolean = true,
    isCloudSynced: Boolean = false,
    isBackingUp: Boolean = false,
    userEmail: String? = null,
    onBackupNow: () -> Unit = {},
    onSignInWithGoogle: (() -> Unit)? = null,
    onSelectStudioTab: (Int) -> Unit,
    onSelectTemplateTagFilter: (String) -> Unit,
    onApplyTemplate: (DecorationTemplate) -> Unit,
    onCopyTemplate: ((DecorationTemplate, String) -> Unit)? = null,
    onShareTemplate: ((DecorationTemplate, String) -> Unit)? = null,
    onDecoratorInputChange: (String) -> Unit,
    onInsertSymbol: (String) -> Unit,
    onCustomFinalOverrideChange: (String) -> Unit,
    onResetCustomFinalOverride: () -> Unit,
    onSelectDecoratorFont: (String) -> Unit,
    onSelectWing: (String) -> Unit,
    onSelectBorder: (String) -> Unit,
    onSelectSeparator: (String) -> Unit,
    onSelectOccasionCategory: (OccasionCategory) -> Unit,
    onApplyOccasion: (OccasionTemplate) -> Unit,
    onNameDesignerNameChange: (String) -> Unit,
    onNameDesignerSubtitleChange: (String) -> Unit,
    onSelectNameDesignerFont: (String) -> Unit,
    onSelectNameDesignerWing: (String) -> Unit,
    onSelectNameDesignerBadge: (String) -> Unit,
    onApplyWaMoodPreset: (String, String) -> Unit,
    onWaHeaderTagChange: (String) -> Unit,
    onWaBodyTextChange: (String) -> Unit,
    onSelectWaFont: (String) -> Unit,
    onSelectWaDivider: (Int) -> Unit,
    onToggleWaFooter: () -> Unit,
    onCopyText: (String) -> Unit,
    onCacheStyle: (title: String, inputText: String, styledText: String, source: CachedEntrySource, fontId: String?, wingId: String?, borderId: String?, separatorId: String?) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onSaveCreation: (String, String, String) -> Unit,
    onShareText: (String) -> Unit,
    onShareWhatsAppDirect: (String) -> Unit,
    onDeleteCreation: (String) -> Unit,
    onCopyFontItem: (FontPreviewItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onShareFontItem: (FontPreviewItem) -> Unit,
    onClearRecents: () -> Unit,
    onRestoreCachedEntry: (CachedStyleEntry) -> Unit = {},
    onRemoveCachedStyle: (String) -> Unit = {},
    onClearCachedStyles: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // My Text Studio Title & Sub-navigation Pills
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AutoFixHigh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "My Text Studio",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "50+ templates, borders, name & status designers",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(StudioTabLabels.indices.toList()) { idx ->
                val label = if (idx == 5) {
                    "Saved (${savedCreations.size + favoriteFonts.size + cachedStyles.size})"
                } else {
                    StudioTabLabels[idx]
                }
                val selected = selectedTab == idx
                FilterChip(
                    selected = selected,
                    onClick = { onSelectStudioTab(idx) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    colors = VedographiDefaults.filterChipColors(),
                    border = VedographiDefaults.filterChipBorder(selected),
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("studio_pill_$idx")
                )
            }
        }

        when (selectedTab) {
            0 -> FiftyTemplatesTab(
                decoratorInput = decoratorInput,
                tagFilter = templateTagFilter,
                onInputChange = onDecoratorInputChange,
                onSelectTagFilter = onSelectTemplateTagFilter,
                onApplyTemplate = onApplyTemplate,
                onCopyTemplate = { tpl, text ->
                    onCopyTemplate?.invoke(tpl, text) ?: onCopyText(text)
                },
                onShareTemplate = { tpl, text ->
                    onShareTemplate?.invoke(tpl, text) ?: onShareText(text)
                }
            )
            1 -> CustomDesignerTab(
                decoratorInput = decoratorInput,
                selectedDecoratorFontId = selectedDecoratorFontId,
                selectedWingId = selectedWingId,
                selectedBorderId = selectedBorderId,
                selectedSeparatorId = selectedSeparatorId,
                decoratedOutput = decoratedOutput,
                onDecoratorInputChange = onDecoratorInputChange,
                onInsertSymbol = onInsertSymbol,
                onCustomFinalOverrideChange = onCustomFinalOverrideChange,
                onResetCustomFinalOverride = onResetCustomFinalOverride,
                onSelectDecoratorFont = onSelectDecoratorFont,
                onSelectWing = onSelectWing,
                onSelectBorder = onSelectBorder,
                onSelectSeparator = onSelectSeparator,
                onCopyText = { text ->
                    onCacheStyle(
                        "Decorated",
                        decoratorInput,
                        text,
                        CachedEntrySource.DECORATED,
                        selectedDecoratorFontId,
                        selectedWingId,
                        selectedBorderId,
                        selectedSeparatorId
                    )
                    onCopyText(text)
                },
                onSaveCreation = { text -> onSaveCreation("Decorated", text, "DECORATED") },
                onShareText = { text ->
                    onCacheStyle(
                        "Decorated",
                        decoratorInput,
                        text,
                        CachedEntrySource.DECORATED,
                        selectedDecoratorFontId,
                        selectedWingId,
                        selectedBorderId,
                        selectedSeparatorId
                    )
                    onShareText(text)
                }
            )
            2 -> NameDesignerTab(
                name = nameDesignerName,
                subtitle = nameDesignerSubtitle,
                selectedFontId = nameDesignerFontId,
                selectedWingId = nameDesignerWingId,
                selectedBadge = nameDesignerBadge,
                designedOutput = designedNameOutput,
                onNameChange = onNameDesignerNameChange,
                onSubtitleChange = onNameDesignerSubtitleChange,
                onSelectFont = onSelectNameDesignerFont,
                onSelectWing = onSelectNameDesignerWing,
                onSelectBadge = onSelectNameDesignerBadge,
                onCopyText = { text ->
                    onCacheStyle(
                        nameDesignerName.ifBlank { "Stylish Name" },
                        nameDesignerName,
                        text,
                        CachedEntrySource.NAME,
                        nameDesignerFontId,
                        nameDesignerWingId,
                        null,
                        null
                    )
                    onCopyText(text)
                },
                onSaveCreation = { text -> onSaveCreation(nameDesignerName.ifBlank { "Name" }, text, "NAME") },
                onShareText = { text ->
                    onCacheStyle(
                        nameDesignerName.ifBlank { "Stylish Name" },
                        nameDesignerName,
                        text,
                        CachedEntrySource.NAME,
                        nameDesignerFontId,
                        nameDesignerWingId,
                        null,
                        null
                    )
                    onShareText(text)
                }
            )
            3 -> WhatsAppStatusDesignerTab(
                headerTag = waHeaderTag,
                bodyText = waBodyText,
                selectedFontId = waFontId,
                selectedDividerIdx = waDividerIdx,
                showFooter = waShowFooter,
                designedStatus = designedWaStatusOutput,
                onApplyMoodPreset = onApplyWaMoodPreset,
                onHeaderTagChange = onWaHeaderTagChange,
                onBodyTextChange = onWaBodyTextChange,
                onSelectFont = onSelectWaFont,
                onSelectDivider = onSelectWaDivider,
                onToggleFooter = onToggleWaFooter,
                onCopyText = { text ->
                    onCacheStyle(
                        waHeaderTag.ifBlank { "WA Status" },
                        waBodyText,
                        text,
                        CachedEntrySource.STATUS,
                        waFontId,
                        null,
                        null,
                        null
                    )
                    onCopyText(text)
                },
                onSaveCreation = { text -> onSaveCreation("WA Status", text, "STATUS") },
                onShareWhatsApp = { text ->
                    onCacheStyle(
                        waHeaderTag.ifBlank { "WA Status" },
                        waBodyText,
                        text,
                        CachedEntrySource.STATUS,
                        waFontId,
                        null,
                        null,
                        null
                    )
                    onShareWhatsAppDirect(text)
                },
                onShareText = { text ->
                    onCacheStyle(
                        waHeaderTag.ifBlank { "WA Status" },
                        waBodyText,
                        text,
                        CachedEntrySource.STATUS,
                        waFontId,
                        null,
                        null,
                        null
                    )
                    onShareText(text)
                }
            )
            4 -> OccasionsTab(
                selectedCategory = selectedOccasionCategory,
                onSelectCategory = onSelectOccasionCategory,
                onCustomizeOccasion = onApplyOccasion,
                onCopyText = { text ->
                    onCacheStyle(
                        selectedOccasionCategory.label,
                        text,
                        text,
                        CachedEntrySource.OCCASION,
                        null,
                        null,
                        null,
                        null
                    )
                    onCopyText(text)
                },
                onSaveCreation = { title, text -> onSaveCreation(title, text, "OCCASION") },
                onShareText = { text ->
                    onCacheStyle(
                        selectedOccasionCategory.label,
                        text,
                        text,
                        CachedEntrySource.OCCASION,
                        null,
                        null,
                        null,
                        null
                    )
                    onShareText(text)
                }
            )
            else -> SavedAndRecentTab(
                savedCreations = savedCreations,
                favoriteFonts = favoriteFonts,
                recentFonts = recentFonts,
                cachedStyles = cachedStyles,
                isGuest = isGuest,
                isOnline = isOnline,
                isCloudSynced = isCloudSynced,
                isBackingUp = isBackingUp,
                userEmail = userEmail,
                onBackupNow = onBackupNow,
                onSignInWithGoogle = onSignInWithGoogle,
                onCopyText = onCopyText,
                onShareText = onShareText,
                onDeleteCreation = onDeleteCreation,
                onCopyFontItem = onCopyFontItem,
                onToggleFavorite = onToggleFavorite,
                onShareFontItem = onShareFontItem,
                onClearRecents = onClearRecents,
                onRestoreCachedEntry = onRestoreCachedEntry,
                onRemoveCachedStyle = onRemoveCachedStyle,
                onClearCachedStyles = onClearCachedStyles
            )
        }
    }
}

@Composable
private fun FiftyTemplatesTab(
    decoratorInput: String,
    tagFilter: String,
    onInputChange: (String) -> Unit,
    onSelectTagFilter: (String) -> Unit,
    onApplyTemplate: (DecorationTemplate) -> Unit,
    onCopyTemplate: (DecorationTemplate, String) -> Unit,
    onShareTemplate: (DecorationTemplate, String) -> Unit
) {
    val allTags = remember {
        listOf("All") + TextStudioCatalog.decorationTemplates.map { it.tag }.distinct()
    }
    val templates = remember(tagFilter) {
        if (tagFilter == "All") {
            TextStudioCatalog.decorationTemplates
        } else {
            TextStudioCatalog.decorationTemplates.filter { it.tag == tagFilter }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "tpl_input") {
            OutlinedTextField(
                value = decoratorInput,
                onValueChange = onInputChange,
                label = { Text("Your Text for All 50 Templates (English / हिंदी)") },
                singleLine = true,
                shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                colors = VedographiDefaults.textFieldColors(),
                trailingIcon = {
                    if (decoratorInput.isNotEmpty()) {
                        IconButton(
                            onClick = { onInputChange("") },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("studio_templates_input")
            )
        }

        item(key = "tpl_tags") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allTags) { tag ->
                    val selected = tag == tagFilter
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectTagFilter(tag) },
                        label = { Text(tag, style = MaterialTheme.typography.labelLarge) },
                        colors = VedographiDefaults.filterChipColors(),
                        border = VedographiDefaults.filterChipBorder(selected),
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        }

        item(key = "tpl_count") {
            Text(
                text = "${templates.size} decoration templates • Tap Customize to edit borders & symbols",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(templates, key = { it.id }) { template ->
            val rendered = template.render(decoratorInput)
            VedographiContentCard(
                onClick = { onCopyTemplate(template, rendered) },
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.testTag("decoration_tpl_${template.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    VedographiPillBadge(text = template.tag)
                }

                Spacer(modifier = Modifier.height(8.dp))

                SelectionContainer {
                    Text(
                        text = rendered,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            lineHeight = 25.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onCopyTemplate(template, rendered) },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.primaryButtonColors(),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(VedographiDefaults.ButtonHeight)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                    }
                    OutlinedButton(
                        onClick = { onApplyTemplate(template) },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.outlinedButtonColors(),
                        border = VedographiDefaults.outlinedButtonBorder(),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(VedographiDefaults.ButtonHeight)
                    ) {
                        Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Customize", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                    }
                    OutlinedButton(
                        onClick = { onShareTemplate(template, rendered) },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.outlinedButtonColors(),
                        border = VedographiDefaults.outlinedButtonBorder(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(VedographiDefaults.ButtonHeight)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share template", modifier = Modifier.size(17.dp))
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun CustomDesignerTab(
    decoratorInput: String,
    selectedDecoratorFontId: String,
    selectedWingId: String,
    selectedBorderId: String,
    selectedSeparatorId: String,
    decoratedOutput: String,
    onDecoratorInputChange: (String) -> Unit,
    onInsertSymbol: (String) -> Unit,
    onCustomFinalOverrideChange: (String) -> Unit,
    onResetCustomFinalOverride: () -> Unit,
    onSelectDecoratorFont: (String) -> Unit,
    onSelectWing: (String) -> Unit,
    onSelectBorder: (String) -> Unit,
    onSelectSeparator: (String) -> Unit,
    onCopyText: (String) -> Unit,
    onSaveCreation: (String) -> Unit,
    onShareText: (String) -> Unit
) {
    var editFinalDirectly by remember { mutableStateOf(false) }

    val decoratorFonts = remember {
        listOf(
            "bold_script", "sans_bold", "serif_bold_italic", "fraktur_gothic",
            "double_struck", "small_caps", "monospace", "fullwidth", "bubble_dark", "greek_aesthetic"
        ).mapNotNull { FontCatalog.findById(it) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "custom_preview") {
            VedographiHeroCard(contentPadding = PaddingValues(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Decorated Text Preview",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = { editFinalDirectly = !editFinalDirectly },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (editFinalDirectly) "Done" else "Fine-Tune", style = MaterialTheme.typography.labelLarge)
                        }
                        if (editFinalDirectly) {
                            IconButton(
                                onClick = {
                                    onResetCustomFinalOverride()
                                    editFinalDirectly = false
                                },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Reset edits")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (editFinalDirectly) {
                    OutlinedTextField(
                        value = decoratedOutput,
                        onValueChange = onCustomFinalOverrideChange,
                        label = { Text("Customize final text before copying") },
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                        colors = VedographiDefaults.textFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_final_editor")
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SelectionContainer {
                            Text(
                                text = decoratedOutput,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 18.sp,
                                    lineHeight = 27.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onCopyText(decoratedOutput) },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.primaryButtonColors(),
                        modifier = Modifier
                            .weight(1f)
                            .height(VedographiDefaults.ButtonHeight)
                            .testTag("copy_decorated_btn")
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Real Text", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                    }
                    OutlinedButton(
                        onClick = { onSaveCreation(decoratedOutput) },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.outlinedButtonColors(),
                        border = VedographiDefaults.outlinedButtonBorder(),
                        modifier = Modifier.height(VedographiDefaults.ButtonHeight)
                    ) {
                        Icon(Icons.Filled.BookmarkAdd, contentDescription = "Save creation", modifier = Modifier.size(18.dp))
                    }
                    OutlinedButton(
                        onClick = { onShareText(decoratedOutput) },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.outlinedButtonColors(),
                        border = VedographiDefaults.outlinedButtonBorder(),
                        modifier = Modifier.height(VedographiDefaults.ButtonHeight)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share creation", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        item(key = "custom_input") {
            OutlinedTextField(
                value = decoratorInput,
                onValueChange = onDecoratorInputChange,
                label = { Text("Text to Decorate (English or हिंदी)") },
                singleLine = true,
                shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                colors = VedographiDefaults.textFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item(key = "quick_symbols") {
            Column {
                Text(
                    "Quick Insert Symbols",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TextStudioCatalog.quickSymbols) { sym ->
                        AssistChip(
                            onClick = { onInsertSymbol(sym) },
                            label = { Text(sym, fontSize = 18.sp) },
                            colors = VedographiDefaults.assistChipColors(),
                            border = VedographiDefaults.assistChipBorder()
                        )
                    }
                }
            }
        }

        item(key = "custom_fonts") {
            Column {
                Text(
                    "Base Font",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(decoratorFonts, key = { it.id }) { spec ->
                        val selected = spec.id == selectedDecoratorFontId
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectDecoratorFont(spec.id) },
                            label = { Text(spec.transform("Abc"), style = MaterialTheme.typography.labelLarge) },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(selected),
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }
            }
        }

        item(key = "custom_wings") {
            Column {
                Text(
                    "Frames & Wings",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StudioGenerators.wingPresets, key = { it.id }) { wing ->
                        val selected = wing.id == selectedWingId
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectWing(wing.id) },
                            label = {
                                val label = if (wing.left.isEmpty()) wing.label else "${wing.left.trim()} ${wing.label}"
                                Text(label, style = MaterialTheme.typography.labelLarge)
                            },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(selected),
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }
            }
        }

        item(key = "custom_borders") {
            Column {
                Text(
                    "Stylish Borders",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StudioGenerators.borderPresets, key = { it.id }) { border ->
                        val selected = border.id == selectedBorderId
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectBorder(border.id) },
                            label = { Text(border.label, style = MaterialTheme.typography.labelLarge) },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(selected),
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }
            }
        }

        item(key = "custom_seps") {
            Column {
                Text(
                    "Decorative Separators",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StudioGenerators.separatorPresets, key = { it.id }) { sep ->
                        val selected = sep.id == selectedSeparatorId
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectSeparator(sep.id) },
                            label = { Text(sep.label, style = MaterialTheme.typography.labelLarge) },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(selected),
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
