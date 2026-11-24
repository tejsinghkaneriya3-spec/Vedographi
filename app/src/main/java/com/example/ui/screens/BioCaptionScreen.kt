package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.BioCategory
import com.example.domain.BioPlatform
import com.example.domain.StudioGenerators
import com.example.ui.components.VedographiContentCard
import com.example.ui.components.VedographiDefaults
import com.example.ui.components.VedographiHeroCard
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.NeonOrchid
import com.example.ui.theme.RoyalViolet

@Composable
fun BioCaptionScreen(
    selectedPlatform: BioPlatform,
    selectedCategory: BioCategory,
    headline: String,
    bodyText: String,
    selectedFontId: String,
    selectedDividerIdx: Int,
    includeBullets: Boolean,
    generatedPreview: String,
    onSelectPlatform: (BioPlatform) -> Unit,
    onSelectCategory: (BioCategory) -> Unit,
    onShuffleTemplate: () -> Unit,
    onHeadlineChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onSelectFont: (String) -> Unit,
    onSelectDivider: (Int) -> Unit,
    onToggleBullets: () -> Unit,
    onCopyBio: (String) -> Unit,
    onSaveBio: (String) -> Unit,
    onShareBio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "bio_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bio & Caption Studio",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Instagram & Facebook ready stylish bios",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(
                    onClick = onShuffleTemplate,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("bio_shuffle_btn")
                ) {
                    Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Shuffle", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // Instagram vs Facebook selector
        item(key = "bio_platform_selector") {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                BioPlatform.entries.forEachIndexed { idx, platform ->
                    SegmentedButton(
                        selected = platform == selectedPlatform,
                        onClick = { onSelectPlatform(platform) },
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = BioPlatform.entries.size),
                        colors = VedographiDefaults.segmentedButtonColors(),
                        modifier = Modifier.testTag("bio_platform_${platform.name}")
                    ) {
                        Text("${platform.label} Bio", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // Category Chips
        item(key = "bio_categories") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BioCategory.entries) { cat ->
                    val selected = cat == selectedCategory
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectCategory(cat) },
                        label = { Text(cat.label, style = MaterialTheme.typography.labelLarge) },
                        colors = VedographiDefaults.filterChipColors(),
                        border = VedographiDefaults.filterChipBorder(selected),
                        modifier = Modifier.height(42.dp)
                    )
                }
            }
        }

        // Live Profile Mockup Preview
        item(key = "bio_preview_card") {
            VedographiHeroCard(contentPadding = PaddingValues(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(RoyalViolet, NeonOrchid))
                            )
                    ) {
                        Text(
                            text = headline.take(1).uppercase().ifEmpty { "V" },
                            style = MaterialTheme.typography.titleLarge,
                            color = CrispWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${selectedPlatform.label} Profile Preview",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (selectedPlatform == BioPlatform.INSTAGRAM) {
                                "@vedographi.official • 150 char limit friendly"
                            } else {
                                "Facebook Intro • Clean multi-line layout"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SelectionContainer {
                        Text(
                            text = generatedPreview,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onCopyBio(generatedPreview) },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.primaryButtonColors(),
                        modifier = Modifier
                            .weight(1f)
                            .height(VedographiDefaults.ButtonHeight)
                            .testTag("copy_bio_btn")
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Bio", style = MaterialTheme.typography.labelLarge)
                    }
                    OutlinedButton(
                        onClick = { onSaveBio(generatedPreview) },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.outlinedButtonColors(),
                        border = VedographiDefaults.outlinedButtonBorder(),
                        modifier = Modifier
                            .height(VedographiDefaults.ButtonHeight)
                            .testTag("save_bio_btn")
                    ) {
                        Icon(Icons.Filled.BookmarkAdd, contentDescription = "Save bio", modifier = Modifier.size(18.dp))
                    }
                    OutlinedButton(
                        onClick = { onShareBio(generatedPreview) },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.outlinedButtonColors(),
                        border = VedographiDefaults.outlinedButtonBorder(),
                        modifier = Modifier
                            .height(VedographiDefaults.ButtonHeight)
                            .testTag("share_bio_btn")
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share bio", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Inputs
        item(key = "bio_inputs") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = headline,
                    onValueChange = onHeadlineChange,
                    label = { Text("Headline / Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                    colors = VedographiDefaults.textFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bodyText,
                    onValueChange = onBodyChange,
                    label = { Text("Bio Lines / Caption (English or हिंदी)") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                    colors = VedographiDefaults.textFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Font Style Selector
        item(key = "bio_fonts") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Typography Style",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StudioGenerators.bioFonts) { spec ->
                        val selected = spec.id == selectedFontId
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectFont(spec.id) },
                            label = {
                                Text(
                                    spec.transform(spec.name.split(" ").first()),
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

        // Divider Selector & Bullet Switch
        item(key = "bio_dividers") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Aesthetic Divider",
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
                            text = "Add ✦ sparkle bullets to lines",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = includeBullets,
                            onCheckedChange = { onToggleBullets() },
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
