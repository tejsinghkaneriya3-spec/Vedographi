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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FontPreviewCard
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonOrchid
import com.example.ui.theme.WhatsAppGreen

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
            "double_struck", "small_caps", "greek_aesthetic", "curvy_cute"
        ).mapNotNull { FontCatalog.findById(it) }
    }
    val badges = listOf("", "👑", "⚡", "🔥", "💎", "✿", "☑", "★", "♡")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "name_preview") {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(listOf(ElectricViolet, NeonOrchid))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Stylish Name Designer",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
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
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onCopyText(designedOutput) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("copy_designed_name_btn")
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Name", maxLines = 1, softWrap = false)
                        }
                        OutlinedButton(
                            onClick = { onSaveCreation(designedOutput) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Filled.BookmarkAdd, contentDescription = "Save name", modifier = Modifier.size(18.dp))
                        }
                        OutlinedButton(
                            onClick = { onShareText(designedOutput) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Share name", modifier = Modifier.size(18.dp))
                        }
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
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = onSubtitleChange,
                    label = { Text("Optional Tagline / Title (e.g. Official, King)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item(key = "name_fonts") {
            Column {
                Text("Calligraphy & Font", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(nameFonts, key = { it.id }) { spec ->
                        FilterChip(
                            selected = spec.id == selectedFontId,
                            onClick = { onSelectFont(spec.id) },
                            label = { Text(spec.transform(name.ifBlank { "Name" })) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        item(key = "name_wings") {
            Column {
                Text("Crown & Wing Frame", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StudioGenerators.wingPresets, key = { it.id }) { wing ->
                        FilterChip(
                            selected = wing.id == selectedWingId,
                            onClick = { onSelectWing(wing.id) },
                            label = {
                                Text(if (wing.left.isEmpty()) wing.label else "${wing.left.trim()} ${wing.label}")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        item(key = "name_badges") {
            Column {
                Text("Accent Badge", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(badges) { badge ->
                        FilterChip(
                            selected = badge == selectedBadge,
                            onClick = { onSelectBadge(badge) },
                            label = { Text(badge.ifEmpty { "None" }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
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
                Text("Quick Status Moods", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TextStudioCatalog.waStatusMoods) { (mood, text) ->
                        AssistChip(
                            onClick = { onApplyMoodPreset(mood, text) },
                            label = { Text(mood) }
                        )
                    }
                }
            }
        }

        item(key = "wa_preview") {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF0B1F1A),
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

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF132D26),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SelectionContainer {
                            Text(
                                text = designedStatus,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 25.sp
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onCopyText(designedStatus) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("copy_wa_status_btn")
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy", maxLines = 1, softWrap = false)
                        }
                        Button(
                            onClick = { onShareWhatsApp(designedStatus) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WhatsAppGreen,
                                contentColor = Color(0xFF042214)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("share_wa_direct_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", maxLines = 1, softWrap = false)
                        }
                        OutlinedButton(
                            onClick = { onSaveCreation(designedStatus) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                Icons.Filled.BookmarkAdd,
                                contentDescription = "Save status",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        OutlinedButton(
                            onClick = { onShareText(designedStatus) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = "Share status",
                                tint = Color.White,
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
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bodyText,
                    onValueChange = onBodyTextChange,
                    label = { Text("Status Text (English or हिंदी)") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item(key = "wa_fonts") {
            Column {
                Text("Status Font", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StudioGenerators.bioFonts) { spec ->
                        FilterChip(
                            selected = spec.id == selectedFontId,
                            onClick = { onSelectFont(spec.id) },
                            label = { Text(spec.transform(spec.name.split(" ").first())) }
                        )
                    }
                }
            }
        }

        item(key = "wa_dividers") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Status Border Divider", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(StudioGenerators.bioDividers) { index, pair ->
                        FilterChip(
                            selected = index == selectedDividerIdx,
                            onClick = { onSelectDivider(index) },
                            label = { Text(pair.first) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Include VEDOGRAPHi signature footer", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = showFooter, onCheckedChange = { onToggleFooter() })
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
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { onSelectCategory(category) },
                        label = { Text(category.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("occasion_chip_${category.name}")
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

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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

                    SelectionContainer {
                        Text(
                            text = rendered,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 25.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onCopyText(rendered) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", maxLines = 1, softWrap = false)
                        }
                        OutlinedButton(
                            onClick = { onCustomizeOccasion(item) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Customize", maxLines = 1, softWrap = false)
                        }
                        OutlinedButton(
                            onClick = { onShareText(rendered) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(17.dp))
                        }
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
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
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
                                isCloudSynced -> WhatsAppGreen
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
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(40.dp)
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
                        tint = ElectricViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Offline Cache (${cachedStyles.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                if (cachedStyles.isNotEmpty()) {
                    TextButton(onClick = onClearCachedStyles) {
                        Text("Clear")
                    }
                }
            }
        }

        if (cachedStyles.isNotEmpty()) {
            item(key = "cached_filter_chips") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val labels = listOf("All", "Templates", "Styles & Fonts")
                    itemsIndexed(labels) { idx, label ->
                        FilterChip(
                            selected = cacheFilterIdx == idx,
                            onClick = { cacheFilterIdx = idx },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
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
            Text("Saved Creations", style = MaterialTheme.typography.titleLarge)
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
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.kind} • ${item.title}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { onDeleteCreation(item.id) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete")
                            }
                        }
                        SelectionContainer {
                            Text(
                                text = item.content,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onCopyText(item.content) },
                                modifier = Modifier.height(40.dp)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy")
                            }
                            OutlinedButton(
                                onClick = { onShareText(item.content) },
                                modifier = Modifier.height(40.dp)
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Favorite Fonts (${favoriteFonts.size})", style = MaterialTheme.typography.titleLarge)
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
                Text("Recently Used (${recentFonts.size})", style = MaterialTheme.typography.titleLarge)
                if (recentFonts.isNotEmpty()) {
                    TextButton(onClick = onClearRecents) {
                        Text("Clear")
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
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("cached_style_card_${entry.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
                    ) {
                        Text(
                            text = entry.source.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
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

            SelectionContainer {
                Text(
                    text = entry.styledText,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 25.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCopy,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", maxLines = 1, softWrap = false)
                }
                OutlinedButton(
                    onClick = onReuse,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reuse", maxLines = 1, softWrap = false)
                }
                OutlinedButton(
                    onClick = onShare,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = "Share cached style", modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}
