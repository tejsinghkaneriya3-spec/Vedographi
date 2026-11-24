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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CachedStyleEntry
import com.example.domain.FontCatalog
import com.example.domain.OccasionCategory
import com.example.domain.OccasionTemplate
import com.example.domain.SavedCreation
import com.example.domain.StudioGenerators
import com.example.domain.TextStudioCatalog
import com.example.ui.FontPreviewItem
import com.example.ui.components.CopyFeedbackPreviewBox
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FontPreviewCard
import com.example.ui.components.VedographiContentCard
import com.example.ui.components.VedographiDefaults
import com.example.ui.components.VedographiHeroCard
import com.example.ui.components.VedographiPillBadge
import com.example.ui.components.rememberCopyPulseState
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.HindiCalligraphyPreset
import com.example.ui.theme.NeonOrchid
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.WhatsAppDarkBg
import com.example.ui.theme.WhatsAppDarkSurface
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppOnGreen

@Composable
fun NameDesignerTab(
    name: String,
    subtitle: String,
    selectedFontId: String,
    selectedWingId: String,
    selectedBadge: String,
    designedOutput: String,
    onNameChange: (String) -> Unit,
    onSubtitleChange: (String) -> Unit,
    onSelectFont: (String) -> Unit,
    onSelectWing: (String) -> Unit,
    onSelectBadge: (String) -> Unit,
    onCopyText: (String) -> Unit,
    onSaveCreation: (String) -> Unit,
    onShareText: (String) -> Unit
) {
    val nameFonts = remember {
        listOf(
            "bold_script", "serif_bold", "sans_bold", "bold_fraktur",
            "double_struck", "small_caps", "greek_aesthetic", "curvy_cute",
            "hindi_kalash_royal", "hindi_morpankh_script", "hindi_rajwada_crown", "hindi_shayari_quote"
        ).mapNotNull { FontCatalog.findById(it) }
    }
    val badges = listOf("", "👑", "⚡", "🔥", "💎", "✿", "☑", "★", "♡")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "name_preview") {
            val copyPulse = rememberCopyPulseState()
            VedographiHeroCard(contentPadding = PaddingValues(16.dp)) {
                Text(
                    text = "Stylish Name Designer",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                CopyFeedbackPreviewBox(
                    pulseState = copyPulse,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SelectionContainer {
                            Text(
                                text = designedOutput,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 21.sp,
                                    lineHeight = 30.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(16.dp)
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
                        onClick = {
                            copyPulse.trigger()
                            onCopyText(designedOutput)
                        },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.primaryButtonColors(),
                        modifier = Modifier
                            .weight(1f)
                            .height(VedographiDefaults.ButtonHeight)
                            .testTag("copy_designed_name_btn")
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Name", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                    }
                    OutlinedButton(
                        onClick = { onSaveCreation(designedOutput) },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.outlinedButtonColors(),
                        border = VedographiDefaults.outlinedButtonBorder(),
                        modifier = Modifier.height(VedographiDefaults.ButtonHeight)
                    ) {
                        Icon(Icons.Filled.BookmarkAdd, contentDescription = "Save name", modifier = Modifier.size(18.dp))
                    }
                    OutlinedButton(
                        onClick = { onShareText(designedOutput) },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.outlinedButtonColors(),
                        border = VedographiDefaults.outlinedButtonBorder(),
                        modifier = Modifier.height(VedographiDefaults.ButtonHeight)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share name", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        item(key = "name_inputs") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Your Name (English or हिंदी)") },
                    singleLine = true,
                    shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                    colors = VedographiDefaults.textFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = onSubtitleChange,
                    label = { Text("Optional Tagline / Title (e.g. Official, King)") },
                    singleLine = true,
                    shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                    colors = VedographiDefaults.textFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item(key = "name_fonts") {
            Column {
                Text(
                    "Calligraphy & Font",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(nameFonts, key = { it.id }) { spec ->
                        val selected = spec.id == selectedFontId
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectFont(spec.id) },
                            label = {
                                val chipFont = spec.calligraphyPresetId
                                    ?.let { HindiCalligraphyPreset.fromId(it).fontFamily }
                                    ?: MaterialTheme.typography.labelLarge.fontFamily
                                Text(
                                    spec.transform(name.ifBlank { "Name" }),
                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = chipFont),
                                    maxLines = 1
                                )
                            },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(selected),
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }
            }
        }

        item(key = "name_wings") {
            Column {
                Text(
                    "Crown & Wing Frame",
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
                                Text(
                                    if (wing.left.isEmpty()) wing.label else "${wing.left.trim()} ${wing.label}",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(selected),
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }
            }
        }

        item(key = "name_badges") {
            Column {
                Text(
                    "Accent Badge",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(badges) { badge ->
                        val selected = badge == selectedBadge
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectBadge(badge) },
                            label = { Text(badge.ifEmpty { "None" }, style = MaterialTheme.typography.labelLarge) },
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

@Composable
fun WhatsAppStatusDesignerTab(
    headerTag: String,
    bodyText: String,
    selectedFontId: String,
    selectedDividerIdx: Int,
    showFooter: Boolean,
    designedStatus: String,
    onApplyMoodPreset: (String, String) -> Unit,
    onHeaderTagChange: (String) -> Unit,
    onBodyTextChange: (String) -> Unit,
    onSelectFont: (String) -> Unit,
    onSelectDivider: (Int) -> Unit,
    onToggleFooter: () -> Unit,
    onCopyText: (String) -> Unit,
    onSaveCreation: (String) -> Unit,
    onShareWhatsApp: (String) -> Unit,
    onShareText: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "wa_moods") {
            Column {
                Text(
                    "Quick Status Moods",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TextStudioCatalog.waStatusMoods) { (mood, text) ->
                        AssistChip(
                            onClick = { onApplyMoodPreset(mood, text) },
                            label = { Text(mood, style = MaterialTheme.typography.labelLarge) },
                            colors = VedographiDefaults.assistChipColors(),
                            border = VedographiDefaults.assistChipBorder()
                        )
                    }
                }
            }
        }

        item(key = "wa_preview") {
            val copyPulse = rememberCopyPulseState()
            Surface(
                shape = RoundedCornerShape(VedographiDefaults.HeroCornerRadius),
                color = WhatsAppDarkBg,
                border = BorderStroke(1.5.dp, WhatsAppGreen.copy(alpha = 0.65f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WhatsApp Status Preview",
                            style = MaterialTheme.typography.labelLarge,
                            color = WhatsAppGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    CopyFeedbackPreviewBox(
                        pulseState = copyPulse,
                        shape = RoundedCornerShape(16.dp),
                        highlightColor = WhatsAppGreen,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = WhatsAppDarkSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SelectionContainer {
                                Text(
                                    text = designedStatus,
                                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 25.sp),
                                    color = CrispWhite,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                copyPulse.trigger()
                                onCopyText(designedStatus)
                            },
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            colors = VedographiDefaults.primaryButtonColors(),
                            modifier = Modifier
                                .weight(1f)
                                .height(VedographiDefaults.ButtonHeight)
                                .testTag("copy_wa_status_btn")
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                        }
                        Button(
                            onClick = { onShareWhatsApp(designedStatus) },
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            colors = VedographiDefaults.primaryButtonColors(
                                containerColor = WhatsAppGreen,
                                contentColor = WhatsAppOnGreen
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(VedographiDefaults.ButtonHeight)
                                .testTag("share_wa_direct_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                        }
                        OutlinedButton(
                            onClick = { onSaveCreation(designedStatus) },
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            border = BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.45f)),
                            modifier = Modifier.height(VedographiDefaults.ButtonHeight)
                        ) {
                            Icon(
                                Icons.Filled.BookmarkAdd,
                                contentDescription = "Save status",
                                tint = CrispWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        OutlinedButton(
                            onClick = { onShareText(designedStatus) },
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            border = BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.45f)),
                            modifier = Modifier.height(VedographiDefaults.ButtonHeight)
                        ) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = "Share status",
                                tint = CrispWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        item(key = "wa_inputs") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = headerTag,
                    onValueChange = onHeaderTagChange,
                    label = { Text("Status Header Badge") },
                    singleLine = true,
                    shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                    colors = VedographiDefaults.textFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bodyText,
                    onValueChange = onBodyTextChange,
                    label = { Text("Status Text (English or हिंदी)") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                    colors = VedographiDefaults.textFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item(key = "wa_fonts") {
            Column {
                Text(
                    "Status Font",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StudioGenerators.bioFonts) { spec ->
                        val selected = spec.id == selectedFontId
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectFont(spec.id) },
                            label = {
                                val chipFont = spec.calligraphyPresetId
                                    ?.let { HindiCalligraphyPreset.fromId(it).fontFamily }
                                    ?: MaterialTheme.typography.labelLarge.fontFamily
                                Text(
                                    spec.transform(spec.name.split(" ").first()),
                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = chipFont),
                                    maxLines = 1
                                )
                            },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(selected),
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }
            }
        }

        item(key = "wa_dividers") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Status Border Divider",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(StudioGenerators.bioDividers) { index, pair ->
                        val selected = index == selectedDividerIdx
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectDivider(index) },
                            label = { Text(pair.first, style = MaterialTheme.typography.labelLarge) },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(selected),
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }
                VedographiContentCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Include VEDOGRAPHi signature footer",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = showFooter,
                            onCheckedChange = { onToggleFooter() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CrispWhite,
                                checkedTrackColor = RoyalViolet
                            )
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun OccasionsTab(
    selectedCategory: OccasionCategory,
    onSelectCategory: (OccasionCategory) -> Unit,
    onCustomizeOccasion: (OccasionTemplate) -> Unit,
    onCopyText: (String) -> Unit,
    onSaveCreation: (String, String) -> Unit,
    onShareText: (String) -> Unit
) {
    val templates = remember(selectedCategory) {
        TextStudioCatalog.occasionTemplates.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "occ_categories") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(OccasionCategory.entries) { category ->
                    val selected = category == selectedCategory
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectCategory(category) },
                        label = { Text(category.label, style = MaterialTheme.typography.labelLarge) },
                        colors = VedographiDefaults.filterChipColors(),
                        border = VedographiDefaults.filterChipBorder(selected),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("occasion_chip_${category.name}")
                    )
                }
            }
        }

        items(templates, key = { it.id }) { item ->
            val font = FontCatalog.findById(item.fontId) ?: FontCatalog.allStyles.first()
            val wing = StudioGenerators.wingPresets.firstOrNull { it.id == item.wingId } ?: StudioGenerators.wingPresets.first()
            val border = StudioGenerators.borderPresets.firstOrNull { it.id == item.borderId } ?: StudioGenerators.borderPresets.first()
            val sep = StudioGenerators.separatorPresets.first()
            val rendered = StudioGenerators.decorateText(item.text, font, wing, border, sep)
            val copyPulse = rememberCopyPulseState()

            VedographiContentCard(contentPadding = PaddingValues(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = { onSaveCreation(item.title, rendered) },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Filled.BookmarkAdd, contentDescription = "Save template")
                    }
                }

                CopyFeedbackPreviewBox(
                    pulseState = copyPulse,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SelectionContainer {
                        Text(
                            text = rendered,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 25.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            copyPulse.trigger()
                            onCopyText(rendered)
                        },
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
                        onClick = { onCustomizeOccasion(item) },
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
                        onClick = { onShareText(rendered) },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.outlinedButtonColors(),
                        border = VedographiDefaults.outlinedButtonBorder(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(VedographiDefaults.ButtonHeight)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(17.dp))
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun SavedAndRecentTab(
    savedCreations: List<SavedCreation>,
    favoriteFonts: List<FontPreviewItem>,
    recentFonts: List<FontPreviewItem>,
    cachedStyles: List<CachedStyleEntry> = emptyList(),
    isGuest: Boolean = false,
    isOnline: Boolean = true,
    isCloudSynced: Boolean = false,
    isBackingUp: Boolean = false,
    userEmail: String? = null,
    onBackupNow: () -> Unit = {},
    onSignInWithGoogle: (() -> Unit)? = null,
    onCopyText: (String) -> Unit,
    onShareText: (String) -> Unit,
    onDeleteCreation: (String) -> Unit,
    onCopyFontItem: (FontPreviewItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onShareFontItem: (FontPreviewItem) -> Unit,
    onClearRecents: () -> Unit,
    onRestoreCachedEntry: (CachedStyleEntry) -> Unit = {},
    onRemoveCachedStyle: (String) -> Unit = {},
    onClearCachedStyles: () -> Unit = {}
) {
    var cacheFilterIdx by rememberSaveable { mutableIntStateOf(0) }
    val filteredCachedStyles = remember(cachedStyles, cacheFilterIdx) {
        when (cacheFilterIdx) {
            1 -> cachedStyles.filter { it.source.isTemplate }
            2 -> cachedStyles.filter { !it.source.isTemplate }
            else -> cachedStyles
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "cloud_backup_card") {
            Surface(
                shape = RoundedCornerShape(VedographiDefaults.CardCornerRadius),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.72f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = when {
                                isGuest || !isOnline -> Icons.Filled.CloudOff
                                isCloudSynced -> Icons.Filled.CloudDone
                                else -> Icons.Filled.CloudSync
                            },
                            contentDescription = null,
                            tint = when {
                                isGuest || !isOnline -> NeonOrchid
                                isCloudSynced -> EmeraldSuccess
                                else -> ElectricViolet
                            },
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when {
                                    isGuest -> "Guest Mode • Offline DataStore Active"
                                    !isOnline -> "Offline Mode • DataStore Cache Active"
                                    isCloudSynced -> "Google Cloud + Offline Cache Active"
                                    else -> "Syncing with Cloud"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when {
                                    isGuest -> "${cachedStyles.size} cached • ${savedCreations.size} saved locally (Sign in to sync)"
                                    !isOnline -> "${cachedStyles.size} cached styles/templates • ${savedCreations.size} projects available offline"
                                    else -> "${cachedStyles.size} cached offline • " + (
                                        userEmail?.takeIf { it.isNotBlank() }
                                            ?: "${savedCreations.size} projects • ${favoriteFonts.size} favorites"
                                        )
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            if (isGuest && onSignInWithGoogle != null) {
                                onSignInWithGoogle()
                            } else {
                                onBackupNow()
                            }
                        },
                        enabled = !isBackingUp,
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.outlinedButtonColors(),
                        border = VedographiDefaults.outlinedButtonBorder(enabled = !isBackingUp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(VedographiDefaults.ButtonHeight)
                            .testTag("studio_backup_now_btn")
                    ) {
                        if (isBackingUp) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = when {
                                    isGuest -> "Sign In"
                                    isOnline -> "Backup"
                                    else -> "Cached"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        // Offline Cache Section: Previously Generated Styles & Templates
        item(key = "cached_styles_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Offline Cache (${cachedStyles.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (cachedStyles.isNotEmpty()) {
                    TextButton(
                        onClick = onClearCachedStyles,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text("Clear", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        if (cachedStyles.isNotEmpty()) {
            item(key = "cached_filter_chips") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val labels = listOf("All", "Templates", "Styles & Fonts")
                    itemsIndexed(labels) { idx, label ->
                        val selected = cacheFilterIdx == idx
                        FilterChip(
                            selected = selected,
                            onClick = { cacheFilterIdx = idx },
                            label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(selected),
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }
            }

            items(filteredCachedStyles, key = { "cached_${it.id}" }) { entry ->
                CachedStyleCard(
                    entry = entry,
                    onCopy = { onCopyText(entry.styledText) },
                    onReuse = { onRestoreCachedEntry(entry) },
                    onShare = { onShareText(entry.styledText) },
                    onDelete = { onRemoveCachedStyle(entry.id) }
                )
            }
        } else {
            item(key = "cached_empty") {
                Text(
                    text = "Styles, templates, bios, and names you copy or customize are automatically cached in DataStore for offline access.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Text(
                "Saved Creations",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (savedCreations.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No saved creations yet",
                    subtitle = "Save styled bios, names, statuses, or decorated phrases from any studio.",
                    icon = Icons.Outlined.BookmarkBorder
                )
            }
        } else {
            items(savedCreations, key = { it.id }) { item ->
                val copyPulse = rememberCopyPulseState()
                VedographiContentCard(contentPadding = PaddingValues(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${item.kind} • ${item.title}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { onDeleteCreation(item.id) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete")
                        }
                    }
                    CopyFeedbackPreviewBox(
                        pulseState = copyPulse,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SelectionContainer {
                            Text(
                                text = item.content,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                copyPulse.trigger()
                                onCopyText(item.content)
                            },
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            colors = VedographiDefaults.outlinedButtonColors(),
                            border = VedographiDefaults.outlinedButtonBorder(),
                            modifier = Modifier.height(VedographiDefaults.ButtonHeight)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy", style = MaterialTheme.typography.labelLarge)
                        }
                        OutlinedButton(
                            onClick = { onShareText(item.content) },
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            colors = VedographiDefaults.outlinedButtonColors(),
                            border = VedographiDefaults.outlinedButtonBorder(),
                            modifier = Modifier.height(VedographiDefaults.ButtonHeight)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Favorite Fonts (${favoriteFonts.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (favoriteFonts.isEmpty()) {
            item {
                Text(
                    text = "Tap the heart on any font in the Fonts tab to pin it here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(favoriteFonts, key = { "saved_fav_${it.spec.id}" }) { item ->
                FontPreviewCard(
                    item = item,
                    onCopy = { onCopyFontItem(item) },
                    onToggleFavorite = { onToggleFavorite(item.spec.id) },
                    onShare = { onShareFontItem(item) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recently Used (${recentFonts.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (recentFonts.isNotEmpty()) {
                    TextButton(
                        onClick = onClearRecents,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text("Clear", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        if (recentFonts.isEmpty()) {
            item {
                Text(
                    text = "Copy or share any font style to build your quick history.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(recentFonts, key = { "saved_rec_${it.spec.id}" }) { item ->
                FontPreviewCard(
                    item = item,
                    onCopy = { onCopyFontItem(item) },
                    onToggleFavorite = { onToggleFavorite(item.spec.id) },
                    onShare = { onShareFontItem(item) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun CachedStyleCard(
    entry: CachedStyleEntry,
    onCopy: () -> Unit,
    onReuse: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val copyPulse = rememberCopyPulseState()

    VedographiContentCard(
        contentPadding = PaddingValues(16.dp),
        modifier = modifier.testTag("cached_style_card_${entry.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                VedographiPillBadge(text = entry.source.label)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Remove cached style",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (entry.inputText.isNotBlank() && entry.inputText != entry.styledText) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Input: ${entry.inputText}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        CopyFeedbackPreviewBox(
            pulseState = copyPulse,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SelectionContainer {
                Text(
                    text = entry.styledText,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 25.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    copyPulse.trigger()
                    onCopy()
                },
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
                onClick = onReuse,
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
                Text("Reuse", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
            }
            OutlinedButton(
                onClick = onShare,
                shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                colors = VedographiDefaults.outlinedButtonColors(),
                border = VedographiDefaults.outlinedButtonBorder(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(VedographiDefaults.ButtonHeight)
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Share cached style", modifier = Modifier.size(17.dp))
            }
        }
    }
}
