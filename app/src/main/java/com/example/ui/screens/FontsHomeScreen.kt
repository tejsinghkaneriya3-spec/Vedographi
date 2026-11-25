package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.AppMonetizationConfig
import com.example.domain.FontCategory
import com.example.ui.FontPreviewItem
import com.example.ui.components.CopyFeedbackPreviewBox
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FontPreviewCard
import com.example.ui.components.VedographiContentCard
import com.example.ui.components.VedographiDefaults
import com.example.ui.components.VedographiHeroCard
import com.example.ui.components.VedographiPillBadge
import com.example.ui.components.VedographiSectionHeader
import com.example.ui.components.rememberCopyPulseState
import com.example.ui.theme.CalligraphyCanvasEnd
import com.example.ui.theme.CalligraphyCanvasMid
import com.example.ui.theme.CalligraphyCanvasStart
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.HindiCalligraphyPreset
import com.example.ui.theme.NeonOrchid
import com.example.ui.theme.RoyalViolet
import com.example.util.AdMobBannerBar
import kotlinx.coroutines.launch

private val QuickSamplePhrases = listOf(
    "हर शब्द का अपना अंदाज़!",
    "ॐ नमः शिवाय ✨",
    "दिल की बात सुनो",
    "अपना टाइम आएगा 🔥",
    "शुभ प्रभात • नमस्ते",
    "Royal Attitude",
    "VEDOGRAPHi"
)

@Composable
fun FontsHomeScreen(
    inputText: String,
    isInputEmpty: Boolean,
    searchQuery: String,
    selectedCategory: FontCategory,
    hasHindiInInput: Boolean,
    trendingItems: List<FontPreviewItem>,
    filteredItems: List<FontPreviewItem>,
    onInputChange: (String) -> Unit,
    onClearText: () -> Unit,
    onPasteClick: () -> Unit,
    onCopyAllClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (FontCategory) -> Unit,
    onCopyItem: (FontPreviewItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onShareItem: (FontPreviewItem) -> Unit,
    modifier: Modifier = Modifier,
    selectedCalligraphyPreset: HindiCalligraphyPreset = HindiCalligraphyPreset.KALAM,
    onSelectCalligraphyPreset: (HindiCalligraphyPreset) -> Unit = {},
    onCopyCalligraphyText: (String) -> Unit = {},
    onShareCalligraphyCard: () -> Unit = {},
    isSearchExpandedAcrossAll: Boolean = false,
    waFbSafeOnly: Boolean = false,
    quickGuideDismissed: Boolean = false,
    pointsBalance: Int = 0,
    announcementBanner: String = "",
    adsEnabled: Boolean = true,
    bannerAdUnitId: String = AppMonetizationConfig.DEFAULT_TEST_BANNER_UNIT_ID,
    onToggleWaFbSafe: () -> Unit = {},
    onDismissQuickGuide: () -> Unit = {},
    onOpenHowToUse: () -> Unit = {},
    onOpenCreateCustomFont: () -> Unit = {},
    onOpenEarnPoints: () -> Unit = {},
    onUnlockProItem: (FontPreviewItem) -> Unit = {},
    onDeleteCustomFont: (String) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current

    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 3 }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Pinned Font Search Bar at the top of the Font Selection screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 4.dp, bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_fonts_placeholder),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search fonts",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            VedographiPillBadge(text = "${filteredItems.size}")
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSearchChange("")
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("clear_search_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = stringResource(R.string.action_clear)
                                )
                            }
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                ),
                shape = RoundedCornerShape(50),
                colors = VedographiDefaults.textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_fonts_field")
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 10.dp, bottom = 84.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            // Optional Admin Announcement Banner
            if (announcementBanner.isNotBlank()) {
                item(key = "admin_announcement") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = RoyalViolet.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Campaign,
                                contentDescription = null,
                                tint = NeonOrchid,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = announcementBanner,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Beginner 3-Step Quick Guide (dismissible)
            if (!quickGuideDismissed && searchQuery.isBlank()) {
                item(key = "beginner_quick_guide") {
                    VedographiContentCard(
                        borderColor = EmeraldSuccess.copy(alpha = 0.55f),
                        contentPadding = PaddingValues(14.dp),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .testTag("quick_guide_card")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Quick Start • आसान 3 स्टेप्स",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            TextButton(
                                onClick = onDismissQuickGuide,
                                modifier = Modifier.testTag("dismiss_quick_guide_btn")
                            ) {
                                Text("Got it", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Type text below  •  2. Filter 'WA & FB Safe' or 'Hindi Calligraphy'  •  3. Tap any card to Copy & Paste!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = onOpenHowToUse,
                                shape = RoundedCornerShape(12.dp),
                                colors = VedographiDefaults.outlinedButtonColors(),
                                border = VedographiDefaults.outlinedButtonBorder(),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("quick_guide_how_to_use_btn")
                            ) {
                                Text("How to Use", style = MaterialTheme.typography.labelMedium)
                            }
                            Button(
                                onClick = onOpenEarnPoints,
                                shape = RoundedCornerShape(12.dp),
                                colors = VedographiDefaults.primaryButtonColors(),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("quick_guide_earn_btn")
                            ) {
                                Icon(Icons.Filled.Diamond, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Earn Pro Pts", style = MaterialTheme.typography.labelMedium, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // 1. Large Prominent Text Input Box at the top
            item(key = "input_hero") {
                VedographiHeroCard(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live Style Studio",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        AnimatedVisibility(visible = hasHindiInInput) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = EmeraldSuccess.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Translate,
                                        contentDescription = null,
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.hindi_safe_badge),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onInputChange,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.input_placeholder),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 17.sp,
                            lineHeight = 25.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                        colors = VedographiDefaults.textFieldColors(),
                        trailingIcon = {
                            if (inputText.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onClearText()
                                    },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("clear_input_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = stringResource(R.string.action_clear)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("main_text_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Character counter & responsive 3-button action bar (Paste / Clear Text / Copy All)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isInputEmpty) {
                                "235+ styles ready"
                            } else {
                                "${inputText.length} chars • Live transform"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (inputText.isNotEmpty()) {
                            Text(
                                text = "Tap any card to copy",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onPasteClick()
                            },
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            colors = VedographiDefaults.outlinedButtonColors(),
                            border = VedographiDefaults.outlinedButtonBorder(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(VedographiDefaults.ButtonHeight)
                                .testTag("paste_input_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentPaste,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.action_paste),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onClearText()
                            },
                            enabled = inputText.isNotEmpty(),
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            colors = VedographiDefaults.outlinedButtonColors(),
                            border = VedographiDefaults.outlinedButtonBorder(enabled = inputText.isNotEmpty()),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(VedographiDefaults.ButtonHeight)
                                .testTag("clear_text_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.action_clear_text),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onCopyAllClick()
                            },
                            enabled = filteredItems.isNotEmpty(),
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            colors = VedographiDefaults.primaryButtonColors(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(VedographiDefaults.ButtonHeight)
                                .testTag("copy_all_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CopyAll,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.action_copy_all),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Sample Chips (English + Hindi)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(QuickSamplePhrases) { phrase ->
                            AssistChip(
                                onClick = {
                                    focusManager.clearFocus()
                                    onInputChange(phrase)
                                },
                                shape = RoundedCornerShape(50),
                                label = {
                                    Text(
                                        text = phrase,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = selectedCalligraphyPreset.fontFamily
                                        ),
                                        maxLines = 1
                                    )
                                },
                                colors = VedographiDefaults.assistChipColors(),
                                border = VedographiDefaults.assistChipBorder()
                            )
                        }
                    }
                }
            }

            // 1B. Hindi Calligraphy Studio Card (hidden during active text search so results appear immediately)
            if (searchQuery.isBlank() &&
                (selectedCategory == FontCategory.ALL || selectedCategory == FontCategory.HINDI_CALLIGRAPHY)
            ) {
                item(key = "hindi_calligraphy_studio") {
                    val calligraphyDisplay = inputText.ifBlank { stringResource(R.string.brand_tagline) }
                    val calligraphyCopyPulse = rememberCopyPulseState()
                    Surface(
                        shape = RoundedCornerShape(VedographiDefaults.HeroCornerRadius),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.25.dp,
                            Brush.linearGradient(
                                listOf(
                                    NeonOrchid.copy(alpha = 0.75f),
                                    ElectricViolet.copy(alpha = 0.75f)
                                )
                            )
                        ),
                        tonalElevation = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("hindi_calligraphy_card")
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(16.dp)
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
                                    Icon(
                                        imageVector = Icons.Filled.Brush,
                                        contentDescription = null,
                                        tint = NeonOrchid,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.hindi_calligraphy_title),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                VedographiPillBadge(text = selectedCalligraphyPreset.styleNote)
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(HindiCalligraphyPreset.entries, key = { it.id }) { preset ->
                                    val selected = preset == selectedCalligraphyPreset
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onSelectCalligraphyPreset(preset)
                                        },
                                        label = {
                                            Text(
                                                text = "${preset.hindiLabel} • ${preset.displayName}",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontFamily = preset.fontFamily
                                                ),
                                                maxLines = 1
                                            )
                                        },
                                        colors = VedographiDefaults.filterChipColors(),
                                        border = VedographiDefaults.filterChipBorder(selected),
                                        modifier = Modifier
                                            .height(40.dp)
                                            .testTag("calligraphy_chip_${preset.id}")
                                    )
                                }
                            }

                            // Live Calligraphy Canvas Preview
                            CopyFeedbackPreviewBox(
                                pulseState = calligraphyCopyPulse,
                                shape = RoundedCornerShape(18.dp),
                                highlightColor = NeonOrchid,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    CalligraphyCanvasStart,
                                                    CalligraphyCanvasMid,
                                                    CalligraphyCanvasEnd
                                                )
                                            )
                                        )
                                        .padding(horizontal = 16.dp, vertical = 20.dp)
                                ) {
                                    Text(
                                        text = calligraphyDisplay,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontFamily = selectedCalligraphyPreset.fontFamily,
                                            fontSize = 24.sp,
                                            lineHeight = 34.sp
                                        ),
                                        color = CrispWhite,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        calligraphyCopyPulse.trigger()
                                        onCopyCalligraphyText(calligraphyDisplay)
                                    },
                                    shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                                    colors = VedographiDefaults.outlinedButtonColors(),
                                    border = VedographiDefaults.outlinedButtonBorder(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(VedographiDefaults.ButtonHeight)
                                        .testTag("copy_calligraphy_text_btn")
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Text", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                                }
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onShareCalligraphyCard()
                                    },
                                    shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                                    colors = VedographiDefaults.primaryButtonColors(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(VedographiDefaults.ButtonHeight)
                                        .testTag("share_calligraphy_card_btn")
                                ) {
                                    Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        stringResource(R.string.share_calligraphy_card),
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1C. Quick Filter & Action Strip (WA & FB Safe, Custom Font Studio, Earn Points)
            item(key = "quick_action_strip") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(key = "wa_fb_filter") {
                        FilterChip(
                            selected = waFbSafeOnly,
                            onClick = onToggleWaFbSafe,
                            shape = RoundedCornerShape(50),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(R.string.wa_fb_safe_filter),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(waFbSafeOnly),
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("wa_fb_safe_filter_chip")
                        )
                    }
                    item(key = "create_custom_shortcut") {
                        AssistChip(
                            onClick = onOpenCreateCustomFont,
                            shape = RoundedCornerShape(50),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Custom Font Studio",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = VedographiDefaults.assistChipColors(),
                            border = VedographiDefaults.assistChipBorder(),
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("shortcut_custom_font_chip")
                        )
                    }
                    item(key = "earn_points_shortcut") {
                        AssistChip(
                            onClick = onOpenEarnPoints,
                            shape = RoundedCornerShape(50),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Diamond,
                                    contentDescription = null,
                                    tint = NeonOrchid,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Earn Pts ($pointsBalance)",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = VedographiDefaults.assistChipColors(),
                            border = VedographiDefaults.assistChipBorder(),
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("shortcut_earn_points_chip")
                        )
                    }
                }
            }

            // 2. Category Chips
            item(key = "category_chips") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FontCategory.entries, key = { it.name }) { category ->
                        val selected = category == selectedCategory
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectCategory(category) },
                            shape = RoundedCornerShape(50),
                            leadingIcon = when (category) {
                                FontCategory.HINDI_CALLIGRAPHY -> {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Brush,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                FontCategory.PRO_FONTS -> {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Diamond,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                FontCategory.MY_CUSTOM -> {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Tune,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                else -> null
                            },
                            label = {
                                Text(
                                    text = category.label,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(selected),
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("category_chip_${category.name}")
                        )
                    }
                }
            }

            // 4. Trending Carousel (visible on All when not searching)
            if (selectedCategory == FontCategory.ALL && searchQuery.isBlank() && trendingItems.isNotEmpty()) {
                item(key = "trending_header") {
                    VedographiSectionHeader(
                        title = stringResource(R.string.trending_section_title),
                        action = {
                            Text(
                                text = "Tap to copy",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                }

                item(key = "trending_carousel") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(trendingItems, key = { "trend_${it.spec.id}" }) { item ->
                            TrendingSpotlightCard(
                                item = item,
                                previewFontFamily = selectedCalligraphyPreset.fontFamily,
                                onCopy = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (item.isUnlocked) {
                                        onCopyItem(item)
                                    } else {
                                        onUnlockProItem(item)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 5. Section Header + Font Count Pill
            item(key = "list_header") {
                VedographiSectionHeader(
                    title = when {
                        isSearchExpandedAcrossAll -> "All Matching Fonts"
                        searchQuery.isNotBlank() && selectedCategory == FontCategory.ALL -> "Search Results"
                        selectedCategory == FontCategory.ALL -> stringResource(R.string.all_fonts_section_title)
                        else -> selectedCategory.label
                    },
                    subtitle = when {
                        isSearchExpandedAcrossAll -> "Showing matches across all categories for \"$searchQuery\""
                        waFbSafeOnly -> "Filtered to WhatsApp & Facebook 100% compatible styles"
                        isInputEmpty && selectedCategory == FontCategory.HINDI_CALLIGRAPHY ->
                            "Previewing \"हर शब्द • VEDOGRAPHi\" — type Hindi or English above"
                        isInputEmpty -> "Previewing \"VEDOGRAPHi\" — type above to transform live"
                        else -> null
                    },
                    action = {
                        VedographiPillBadge(text = "${filteredItems.size} fonts")
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }

            // 6. Empty State or Font List
            if (filteredItems.isEmpty()) {
                item(key = "empty_state") {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        when {
                            selectedCategory == FontCategory.MY_CUSTOM && searchQuery.isBlank() -> EmptyStateCard(
                                title = "No custom fonts created yet",
                                subtitle = "Create your own unique Unicode alphabet in Custom Style Studio and it will appear right here.",
                                icon = Icons.Filled.Tune,
                                actionLabel = "Open Custom Style Studio",
                                onActionClick = onOpenCreateCustomFont
                            )
                            selectedCategory == FontCategory.FAVORITES && searchQuery.isBlank() -> EmptyStateCard(
                                title = stringResource(R.string.empty_favorites_title),
                                subtitle = stringResource(R.string.empty_favorites_subtitle),
                                icon = Icons.Outlined.FavoriteBorder
                            )
                            selectedCategory == FontCategory.RECENT && searchQuery.isBlank() -> EmptyStateCard(
                                title = stringResource(R.string.empty_recent_title),
                                subtitle = stringResource(R.string.empty_recent_subtitle),
                                icon = Icons.Outlined.History
                            )
                            else -> EmptyStateCard(
                                title = stringResource(R.string.empty_search_title),
                                subtitle = stringResource(R.string.empty_search_subtitle),
                                icon = Icons.Outlined.SearchOff,
                                actionLabel = if (searchQuery.isNotBlank() || selectedCategory != FontCategory.ALL || waFbSafeOnly) {
                                    "Reset Search & Filters"
                                } else {
                                    null
                                },
                                onActionClick = {
                                    onSearchChange("")
                                    onSelectCategory(FontCategory.ALL)
                                    if (waFbSafeOnly) onToggleWaFbSafe()
                                }
                            )
                        }
                    }
                }
            } else {
                items(filteredItems, key = { it.spec.id }) { item ->
                    FontPreviewCard(
                        item = item,
                        onCopy = { onCopyItem(item) },
                        onToggleFavorite = { onToggleFavorite(item.spec.id) },
                        onShare = { onShareItem(item) },
                        previewFontFamily = selectedCalligraphyPreset.fontFamily,
                        onUnlockPro = { onUnlockProItem(item) },
                        onDeleteCustom = if (item.spec.isCustom) {
                            { onDeleteCustomFont(item.spec.id) }
                        } else {
                            null
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        // Scroll-to-Top Floating Button for mobile comfort across 235+ fonts
        androidx.compose.animation.AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 18.dp)
        ) {
            SmallFloatingActionButton(
                onClick = {
                    scope.launch { listState.animateScrollToItem(0) }
                },
                shape = CircleShape,
                containerColor = RoyalViolet,
                contentColor = CrispWhite,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("scroll_to_top_fab")
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.scroll_to_top)
                )
            }
        }
        }

        if (adsEnabled) {
            AdMobBannerBar(
                adUnitId = bannerAdUnitId,
                adsEnabled = adsEnabled,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun TrendingSpotlightCard(
    item: FontPreviewItem,
    previewFontFamily: FontFamily,
    onCopy: () -> Unit
) {
    val copyPulse = rememberCopyPulseState()

    Surface(
        shape = RoundedCornerShape(VedographiDefaults.CardCornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(
            1.dp,
            if (item.isRecentlyCopied) EmeraldSuccess else MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)
        ),
        modifier = Modifier
            .width(214.dp)
            .clip(RoundedCornerShape(VedographiDefaults.CardCornerRadius))
            .clickable {
                copyPulse.trigger()
                onCopy()
            }
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            RoyalViolet.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.spec.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = stringResource(R.string.action_copy),
                    tint = if (item.isRecentlyCopied) EmeraldSuccess else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val resolvedFontFamily = item.spec.calligraphyPresetId
                ?.let { HindiCalligraphyPreset.fromId(it).fontFamily }
                ?: previewFontFamily
            CopyFeedbackPreviewBox(
                pulseState = copyPulse,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.previewText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = resolvedFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when {
                    !item.isUnlocked -> "PRO • ${item.spec.proCost} pts to unlock"
                    item.isRecentlyCopied -> "Copied!"
                    else -> "Tap to copy"
                },
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    !item.isUnlocked -> NeonOrchid
                    item.isRecentlyCopied -> EmeraldSuccess
                    else -> MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}
