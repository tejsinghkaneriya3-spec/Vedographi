package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CustomFontDefinition
import com.example.domain.CustomStarterAlphabet
import com.example.domain.CustomStylePresets
import com.example.ui.components.CopyFeedbackPreviewBox
import com.example.ui.components.VedographiContentCard
import com.example.ui.components.VedographiDefaults
import com.example.ui.components.VedographiHeroCard
import com.example.ui.components.VedographiPillBadge
import com.example.ui.components.rememberCopyPulseState
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonOrchid
import com.example.ui.theme.RoyalViolet

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomStyleStudioTab(
    draftName: String,
    sampleInput: String,
    charMap: Map<Char, String>,
    prefix: String,
    suffix: String,
    joiner: String,
    selectedChar: Char,
    livePreview: String,
    savedCustomFonts: List<CustomFontDefinition>,
    onNameChange: (String) -> Unit,
    onSampleInputChange: (String) -> Unit,
    onSelectChar: (Char) -> Unit,
    onSetCharGlyph: (Char, String) -> Unit,
    onApplyStarterAlphabet: (CustomStarterAlphabet) -> Unit,
    onRandomizeMix: () -> Unit,
    onSelectPrefix: (String) -> Unit,
    onSelectSuffix: (String) -> Unit,
    onSelectJoiner: (String) -> Unit,
    onResetDraft: () -> Unit,
    onSaveCustomFont: () -> Unit,
    onLoadCustomFont: (CustomFontDefinition) -> Unit,
    onDeleteCustomFont: (String) -> Unit,
    onCopyText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var charsetGroup by remember { mutableIntStateOf(1) } // 0 = A-Z, 1 = a-z, 2 = 0-9
    var manualGlyphInput by remember(selectedChar) { mutableStateOf("") }

    val charsInGroup = remember(charsetGroup) {
        when (charsetGroup) {
            0 -> ('A'..'Z').toList()
            1 -> ('a'..'z').toList()
            else -> ('0'..'9').toList()
        }
    }
    val glyphChoices = remember(selectedChar) {
        CustomStylePresets.glyphChoicesFor(selectedChar)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("custom_style_studio_tab"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Live Preview Hero Card
        item(key = "custom_studio_preview") {
            val copyPulse = rememberCopyPulseState()
            VedographiHeroCard(contentPadding = PaddingValues(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = NeonOrchid,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Custom Style Studio",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(
                        onClick = onResetDraft,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Text(
                    text = "Har letter ko apna Unicode look do • Save & use anywhere in Fonts!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                text = livePreview,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 20.sp,
                                    lineHeight = 29.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            copyPulse.trigger()
                            onCopyText(livePreview)
                        },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.outlinedButtonColors(),
                        border = VedographiDefaults.outlinedButtonBorder(),
                        modifier = Modifier
                            .weight(1f)
                            .height(VedographiDefaults.ButtonHeight)
                            .testTag("custom_studio_copy_btn")
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Preview", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                    }

                    Button(
                        onClick = onSaveCustomFont,
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.primaryButtonColors(
                            containerColor = RoyalViolet,
                            contentColor = CrispWhite
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(VedographiDefaults.ButtonHeight)
                            .testTag("save_custom_font_btn")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Font", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                    }
                }
            }
        }

        // Font Name & Test Phrase
        item(key = "custom_studio_inputs") {
            VedographiContentCard(contentPadding = PaddingValues(14.dp)) {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = onNameChange,
                    label = { Text("Font Name (e.g. Royal Mix)") },
                    singleLine = true,
                    shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                    colors = VedographiDefaults.textFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_font_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = sampleInput,
                    onValueChange = onSampleInputChange,
                    label = { Text("Preview Input Text") },
                    singleLine = true,
                    shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                    colors = VedographiDefaults.textFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_font_sample_input")
                )
            }
        }

        // Starter Alphabets + Randomize Mix
        item(key = "custom_studio_starters") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. Start From Base Style",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    OutlinedButton(
                        onClick = onRandomizeMix,
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.outlinedButtonColors(),
                        border = VedographiDefaults.outlinedButtonBorder(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("randomize_custom_font_btn")
                    ) {
                        Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Random Mix", style = MaterialTheme.typography.labelLarge)
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CustomStylePresets.starterAlphabets, key = { it.id }) { starter ->
                        FilterChip(
                            selected = false,
                            onClick = { onApplyStarterAlphabet(starter) },
                            label = { Text(starter.label, style = MaterialTheme.typography.labelMedium) },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(false),
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }
            }
        }

        // Per-Character Mapping Mixer
        item(key = "custom_studio_char_mixer") {
            VedographiContentCard(contentPadding = PaddingValues(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. Customize Any Letter",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    VedographiPillBadge(
                        text = "Editing '$selectedChar' → ${charMap[selectedChar] ?: selectedChar}"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("A - Z", "a - z", "0 - 9").forEachIndexed { idx, label ->
                        SegmentedButton(
                            selected = charsetGroup == idx,
                            onClick = {
                                charsetGroup = idx
                                val firstChar = when (idx) {
                                    0 -> 'A'
                                    1 -> 'a'
                                    else -> '0'
                                }
                                onSelectChar(firstChar)
                            },
                            shape = SegmentedButtonDefaults.itemShape(idx, 3),
                            colors = VedographiDefaults.segmentedButtonColors()
                        ) {
                            Text(label, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    charsInGroup.forEach { ch ->
                        val isSelected = ch == selectedChar
                        val mapped = charMap[ch] ?: ch.toString()
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) {
                                RoyalViolet
                            } else if (mapped != ch.toString()) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) NeonOrchid else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectChar(ch) }
                                .minimumInteractiveComponentSize()
                                .testTag("custom_char_key_$ch")
                        ) {
                            Text(
                                text = "$ch:$mapped",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) CrispWhite else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Pick Unicode glyph for '$selectedChar':",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(glyphChoices, key = { "${selectedChar}_$it" }) { glyph ->
                        val active = (charMap[selectedChar] ?: selectedChar.toString()) == glyph
                        FilterChip(
                            selected = active,
                            onClick = { onSetCharGlyph(selectedChar, glyph) },
                            label = {
                                Text(
                                    text = glyph,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(active),
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("custom_glyph_option_${glyph.hashCode()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = manualGlyphInput,
                        onValueChange = { manualGlyphInput = it.take(6) },
                        label = { Text("Or paste any symbol/emoji") },
                        singleLine = true,
                        shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                        colors = VedographiDefaults.textFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            val trimmed = manualGlyphInput.trim()
                            if (trimmed.isNotEmpty()) {
                                onSetCharGlyph(selectedChar, trimmed)
                                manualGlyphInput = ""
                            }
                        },
                        enabled = manualGlyphInput.isNotBlank(),
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.primaryButtonColors(),
                        modifier = Modifier.height(VedographiDefaults.ButtonHeight)
                    ) {
                        Icon(Icons.Filled.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Apply", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // Prefix / Suffix / Joiner
        item(key = "custom_studio_decor") {
            VedographiContentCard(contentPadding = PaddingValues(14.dp)) {
                Text(
                    text = "3. Wings, Frames & Letter Spacing",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("Prefix", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CustomStylePresets.prefixChoices, key = { "pre_${it.second}" }) { (value, label) ->
                        val selected = prefix == value
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectPrefix(value) },
                            label = {
                                Text(
                                    text = if (value.isBlank()) label else "${value.trim()} $label",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(selected),
                            modifier = Modifier.height(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Suffix", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CustomStylePresets.suffixChoices, key = { "suf_${it.second}" }) { (value, label) ->
                        val selected = suffix == value
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectSuffix(value) },
                            label = {
                                Text(
                                    text = if (value.isBlank()) label else "$label ${value.trim()}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(selected),
                            modifier = Modifier.height(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Letter Joiner", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CustomStylePresets.joinerChoices, key = { "join_${it.second}" }) { (value, label) ->
                        val selected = joiner == value
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectJoiner(value) },
                            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(selected),
                            modifier = Modifier.height(38.dp)
                        )
                    }
                }
            }
        }

        // Saved Custom Fonts
        item(key = "saved_custom_fonts_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Saved Custom Fonts (${savedCustomFonts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                VedographiPillBadge(
                    text = "In 'My Custom' tab",
                    containerColor = EmeraldSuccess.copy(alpha = 0.14f),
                    contentColor = EmeraldSuccess,
                    borderColor = EmeraldSuccess.copy(alpha = 0.4f)
                )
            }
        }

        if (savedCustomFonts.isEmpty()) {
            item(key = "no_custom_fonts") {
                VedographiContentCard(contentPadding = PaddingValues(16.dp)) {
                    Text(
                        text = "No custom fonts saved yet. Mix letters above and tap 'Save Font' — your creation will appear in the Fonts list and sync to your profile!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(savedCustomFonts, key = { "saved_cf_${it.id}" }) { def ->
                val renderedSample = def.transform(sampleInput.ifBlank { "VEDOGRAPHi" })
                VedographiContentCard(contentPadding = PaddingValues(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = def.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = renderedSample,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row {
                            IconButton(
                                onClick = { onCopyText(renderedSample) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy custom font sample")
                            }
                            IconButton(
                                onClick = { onLoadCustomFont(def) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit custom font")
                            }
                            IconButton(
                                onClick = { onDeleteCustomFont(def.id) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete custom font")
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}
