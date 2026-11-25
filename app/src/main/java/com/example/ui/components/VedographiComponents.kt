package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButtonColors
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.SocialPlatform
import com.example.ui.BatchCopySheetState
import com.example.ui.FontPreviewItem
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.HindiCalligraphyPreset
import com.example.ui.theme.InstagramMagenta
import com.example.ui.theme.KalamFontFamily
import com.example.ui.theme.NeonOrchid
import com.example.ui.theme.ObsidianIconPlate
import com.example.ui.theme.RoseHeart
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.WhatsAppGreen
import kotlinx.coroutines.launch
import kotlin.math.hypot

@Stable
class CopyPulseState {
    var pulseTick by mutableIntStateOf(0)
        private set

    fun trigger() {
        pulseTick += 1
    }
}

@Composable
fun rememberCopyPulseState(): CopyPulseState = remember { CopyPulseState() }

@Composable
fun CopyFeedbackPreviewBox(
    pulseState: CopyPulseState,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    highlightColor: Color = EmeraldSuccess,
    content: @Composable BoxScope.() -> Unit
) {
    val scale = remember { Animatable(1f) }
    val rippleProgress = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }

    LaunchedEffect(pulseState.pulseTick) {
        if (pulseState.pulseTick > 0) {
            launch {
                scale.snapTo(1f)
                scale.animateTo(
                    targetValue = 1.032f,
                    animationSpec = tween(durationMillis = 85)
                )
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            launch {
                rippleProgress.snapTo(0f)
                rippleAlpha.snapTo(0.28f)
                launch {
                    rippleProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 360)
                    )
                }
                rippleAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 380)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(shape)
            .drawWithContent {
                drawContent()
                val alpha = rippleAlpha.value
                if (alpha > 0.005f) {
                    val maxRadius = hypot(size.width.toDouble(), size.height.toDouble()).toFloat() * 0.68f
                    val currentRadius = (maxRadius * rippleProgress.value).coerceAtLeast(1f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                highlightColor.copy(alpha = alpha),
                                highlightColor.copy(alpha = alpha * 0.35f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = currentRadius
                        ),
                        radius = currentRadius,
                        center = center
                    )
                }
            },
        content = content
    )
}

object VedographiDefaults {
    val ButtonCornerRadius = 14.dp
    val InputCornerRadius = 16.dp
    val CardCornerRadius = 20.dp
    val HeroCornerRadius = 24.dp
    val ButtonHeight = 48.dp

    @Composable
    fun primaryButtonColors(
        containerColor: Color = RoyalViolet,
        contentColor: Color = CrispWhite
    ): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    )

    @Composable
    fun outlinedButtonColors(): ButtonColors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.onSurface,
        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    )

    @Composable
    fun outlinedButtonBorder(enabled: Boolean = true): BorderStroke = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = if (enabled) 0.9f else 0.3f)
    )

    @Composable
    fun textFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.75f),
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    @Composable
    fun filterChipColors(): SelectableChipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = RoyalViolet,
        selectedLabelColor = CrispWhite,
        selectedLeadingIconColor = CrispWhite,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        iconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    @Composable
    fun filterChipBorder(selected: Boolean): BorderStroke = FilterChipDefaults.filterChipBorder(
        enabled = true,
        selected = selected,
        borderColor = MaterialTheme.colorScheme.outlineVariant,
        selectedBorderColor = ElectricViolet.copy(alpha = 0.9f),
        borderWidth = 1.dp,
        selectedBorderWidth = 1.25.dp
    )

    @Composable
    fun assistChipColors(): ChipColors = AssistChipDefaults.assistChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        labelColor = MaterialTheme.colorScheme.onSurface,
        leadingIconContentColor = MaterialTheme.colorScheme.primary
    )

    @Composable
    fun assistChipBorder(): BorderStroke = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )

    @Composable
    fun segmentedButtonColors(): SegmentedButtonColors = SegmentedButtonDefaults.colors(
        activeContainerColor = RoyalViolet,
        activeContentColor = CrispWhite,
        activeBorderColor = ElectricViolet.copy(alpha = 0.85f),
        inactiveContainerColor = MaterialTheme.colorScheme.surface,
        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun VedographiPillBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun VedographiHeroCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(VedographiDefaults.HeroCornerRadius),
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    ElectricViolet,
                    NeonOrchid.copy(alpha = 0.9f),
                    RoyalViolet
                )
            )
        ),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            RoyalViolet.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    )
                )
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun VedographiContentCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(VedographiDefaults.CardCornerRadius),
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.72f),
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val baseModifier = modifier
        .fillMaxWidth()
        .clip(shape)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, borderColor),
        modifier = baseModifier
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun VedographiSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (action != null) {
            Spacer(modifier = Modifier.width(8.dp))
            action()
        }
    }
}

@Composable
fun VedographiTopHeader(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    isCloudSynced: Boolean = false,
    isOnline: Boolean = true,
    pointsBalance: Int = 0,
    onPointsClick: (() -> Unit)? = null,
    onHowToUseClick: (() -> Unit)? = null,
    onAccountClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(13.dp),
                color = ObsidianIconPlate,
                border = BorderStroke(1.25.dp, ElectricViolet.copy(alpha = 0.65f)),
                shadowElevation = 6.dp,
                modifier = Modifier.size(42.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.vedographi_icon_1795394652656),
                    contentDescription = stringResource(R.string.app_name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.onBackground,
                                MaterialTheme.colorScheme.primary,
                                NeonOrchid
                            )
                        )
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.brand_tagline),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = KalamFontFamily,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onPointsClick != null) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = RoyalViolet.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onPointsClick)
                        .minimumInteractiveComponentSize()
                        .testTag("header_points_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Diamond,
                            contentDescription = "Points balance",
                            tint = NeonOrchid,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$pointsBalance",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            if (onHowToUseClick != null) {
                FilledTonalIconButton(
                    onClick = onHowToUseClick,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("header_how_to_use_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = stringResource(R.string.how_to_use_title),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (onAccountClick != null) {
                FilledTonalIconButton(
                    onClick = onAccountClick,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = when {
                            !isOnline -> NeonOrchid
                            isCloudSynced -> EmeraldSuccess
                            else -> MaterialTheme.colorScheme.primary
                        }
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("account_sync_button")
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Filled.Public else Icons.Filled.CloudOff,
                        contentDescription = if (isOnline) "Account and cloud sync" else "Offline DataStore cache active",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            FilledTonalIconButton(
                onClick = onToggleTheme,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .size(40.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = stringResource(R.string.toggle_theme),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun FontPreviewCard(
    item: FontPreviewItem,
    onCopy: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    previewFontFamily: FontFamily? = null,
    onUnlockPro: (() -> Unit)? = null,
    onDeleteCustom: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val heartScale by animateFloatAsState(
        targetValue = if (item.isFavorite) 1.18f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "heart_scale"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            item.isRecentlyCopied -> EmeraldSuccess
            !item.isUnlocked -> NeonOrchid.copy(alpha = 0.72f)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.72f)
        },
        label = "card_border"
    )

    val copyPulse = rememberCopyPulseState()

    val triggerPrimaryAction: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (item.isUnlocked) {
            copyPulse.trigger()
            onCopy()
        } else {
            onUnlockPro?.invoke()
        }
    }

    VedographiContentCard(
        borderColor = borderColor,
        onClick = triggerPrimaryAction,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        modifier = modifier.testTag("font_card_${item.spec.id}")
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
                Text(
                    text = item.spec.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                VedographiPillBadge(text = item.spec.category.label)
                if (item.spec.isPro) {
                    Spacer(modifier = Modifier.width(6.dp))
                    VedographiPillBadge(
                        text = if (item.isUnlocked) "PRO ✓" else "PRO • ${item.spec.proCost} pts",
                        containerColor = if (item.isUnlocked) {
                            EmeraldSuccess.copy(alpha = 0.16f)
                        } else {
                            NeonOrchid.copy(alpha = 0.18f)
                        },
                        contentColor = if (item.isUnlocked) EmeraldSuccess else NeonOrchid,
                        borderColor = if (item.isUnlocked) {
                            EmeraldSuccess.copy(alpha = 0.45f)
                        } else {
                            NeonOrchid.copy(alpha = 0.55f)
                        }
                    )
                } else if (item.spec.badge != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    VedographiPillBadge(
                        text = item.spec.badge,
                        containerColor = NeonOrchid.copy(alpha = 0.16f),
                        contentColor = NeonOrchid,
                        borderColor = NeonOrchid.copy(alpha = 0.45f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.spec.isCustom && onDeleteCustom != null) {
                    IconButton(
                        onClick = onDeleteCustom,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("delete_custom_font_${item.spec.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Delete custom font",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleFavorite()
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("fav_button_${item.spec.id}")
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(R.string.action_favorite),
                        tint = if (item.isFavorite) RoseHeart else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.scale(heartScale)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Platform Compatibility Indicator (WA/FB Safe vs IG/Bio)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VedographiPillBadge(
                text = if (item.spec.isWaFbSafe) "WA & FB ✓" else "IG / Bio",
                containerColor = if (item.spec.isWaFbSafe) {
                    EmeraldSuccess.copy(alpha = 0.14f)
                } else {
                    RoyalViolet.copy(alpha = 0.14f)
                },
                contentColor = if (item.spec.isWaFbSafe) {
                    EmeraldSuccess
                } else {
                    MaterialTheme.colorScheme.primary
                },
                borderColor = if (item.spec.isWaFbSafe) {
                    EmeraldSuccess.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                }
            )
            if (!item.isUnlocked) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked Pro font",
                        tint = NeonOrchid,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Unlock once with points • Yours forever",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonOrchid
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        val resolvedFontFamily = item.spec.calligraphyPresetId
            ?.let { HindiCalligraphyPreset.fromId(it).fontFamily }
            ?: previewFontFamily
            ?: MaterialTheme.typography.titleLarge.fontFamily

        CopyFeedbackPreviewBox(
            pulseState = copyPulse,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = item.previewText,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = resolvedFontFamily,
                    fontSize = 21.sp,
                    lineHeight = 30.sp
                ),
                color = if (item.isUnlocked) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (item.isUnlocked) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = triggerPrimaryAction,
                    shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                    colors = VedographiDefaults.primaryButtonColors(
                        containerColor = if (item.isRecentlyCopied) EmeraldSuccess else RoyalViolet,
                        contentColor = CrispWhite
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(VedographiDefaults.ButtonHeight)
                        .testTag("copy_button_${item.spec.id}")
                ) {
                    AnimatedContent(
                        targetState = item.isRecentlyCopied,
                        label = "copy_state"
                    ) { copied ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (copied) stringResource(R.string.action_copied) else stringResource(R.string.action_copy),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onShare,
                    shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                    colors = VedographiDefaults.outlinedButtonColors(),
                    border = VedographiDefaults.outlinedButtonBorder(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(VedographiDefaults.ButtonHeight)
                        .testTag("share_button_${item.spec.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.action_share),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        } else {
            Button(
                onClick = triggerPrimaryAction,
                shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                colors = VedographiDefaults.primaryButtonColors(
                    containerColor = NeonOrchid.copy(alpha = 0.9f),
                    contentColor = CrispWhite
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(VedographiDefaults.ButtonHeight)
                    .testTag("unlock_pro_button_${item.spec.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.LockOpen,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Unlock Pro Font (${item.spec.proCost} pts)",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialShareBottomSheet(
    text: String,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onSharePlatform: (SocialPlatform) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.share_sheet_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            val copyPulse = rememberCopyPulseState()

            CopyFeedbackPreviewBox(
                pulseState = copyPulse,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SocialPlatformButton(
                    label = stringResource(R.string.share_whatsapp),
                    icon = Icons.AutoMirrored.Filled.Chat,
                    tint = WhatsAppGreen,
                    testTag = "share_whatsapp_btn",
                    onClick = { onSharePlatform(SocialPlatform.WHATSAPP) }
                )
                SocialPlatformButton(
                    label = stringResource(R.string.share_instagram),
                    icon = Icons.Filled.CameraAlt,
                    tint = InstagramMagenta,
                    testTag = "share_instagram_btn",
                    onClick = { onSharePlatform(SocialPlatform.INSTAGRAM) }
                )
                SocialPlatformButton(
                    label = stringResource(R.string.share_facebook),
                    icon = Icons.Filled.Public,
                    tint = FacebookBlue,
                    testTag = "share_facebook_btn",
                    onClick = { onSharePlatform(SocialPlatform.FACEBOOK) }
                )
                SocialPlatformButton(
                    label = stringResource(R.string.share_telegram),
                    icon = Icons.AutoMirrored.Filled.Send,
                    tint = TelegramBlue,
                    testTag = "share_telegram_btn",
                    onClick = { onSharePlatform(SocialPlatform.TELEGRAM) }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        copyPulse.trigger()
                        onCopy()
                    },
                    shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                    colors = VedographiDefaults.outlinedButtonColors(),
                    border = VedographiDefaults.outlinedButtonBorder(),
                    modifier = Modifier
                        .weight(1f)
                        .height(VedographiDefaults.ButtonHeight)
                        .testTag("sheet_copy_btn")
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_copy), style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = { onSharePlatform(SocialPlatform.MORE) },
                    shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                    colors = VedographiDefaults.primaryButtonColors(),
                    modifier = Modifier
                        .weight(1f)
                        .height(VedographiDefaults.ButtonHeight)
                        .testTag("sheet_more_btn")
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.share_more), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun SocialPlatformButton(
    label: String,
    icon: ImageVector,
    tint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.16f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    icon: ImageVector = Icons.Outlined.AutoAwesome,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(28.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (!actionLabel.isNullOrBlank() && onActionClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                    colors = VedographiDefaults.outlinedButtonColors(),
                    border = VedographiDefaults.outlinedButtonBorder(),
                    modifier = Modifier.height(VedographiDefaults.ButtonHeight)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchCopyAllBottomSheet(
    state: BatchCopySheetState,
    onDismiss: () -> Unit,
    onCopyBatch: (formattedText: String, selectedCount: Int) -> Unit,
    onShareBatch: (formattedText: String, selectedCount: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current
    val copyPulse = rememberCopyPulseState()

    var selectedIds by remember(state) {
        mutableStateOf(state.variations.map { it.id }.toSet())
    }
    var includeStyleLabels by remember(state) {
        mutableStateOf(state.includeLabelsDefault)
    }

    val favoriteCount = remember(state.variations) {
        state.variations.count { it.isFavorite }
    }

    val selectedVariations = remember(state.variations, selectedIds) {
        state.variations.filter { it.id in selectedIds }
    }

    val formattedBatchText = remember(selectedVariations, includeStyleLabels) {
        selectedVariations.joinToString("\n") { item ->
            if (includeStyleLabels) "${item.styleName}: ${item.styledText}" else item.styledText
        }
    }

    val allSelected = selectedIds.size == state.variations.size && state.variations.isNotEmpty()
    val top5Ids = remember(state.variations) { state.variations.take(5).map { it.id }.toSet() }
    val top10Ids = remember(state.variations) { state.variations.take(10).map { it.id }.toSet() }
    val favIds = remember(state.variations) { state.variations.filter { it.isFavorite }.map { it.id }.toSet() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (state.sourceText.isNotBlank()) {
                        Text(
                            text = "Input: ${state.sourceText}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                VedographiPillBadge(
                    text = "${selectedVariations.size} / ${state.variations.size} selected"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick batch selection presets
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item(key = "preset_all") {
                    FilterChip(
                        selected = allSelected,
                        onClick = { selectedIds = state.variations.map { it.id }.toSet() },
                        label = { Text("All (${state.variations.size})", style = MaterialTheme.typography.labelMedium) },
                        colors = VedographiDefaults.filterChipColors(),
                        border = VedographiDefaults.filterChipBorder(allSelected),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("batch_preset_all")
                    )
                }
                if (state.variations.size > 5) {
                    val isTop5 = selectedIds == top5Ids
                    item(key = "preset_top5") {
                        FilterChip(
                            selected = isTop5,
                            onClick = { selectedIds = top5Ids },
                            label = { Text("Top 5", style = MaterialTheme.typography.labelMedium) },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(isTop5),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("batch_preset_top5")
                        )
                    }
                }
                if (state.variations.size > 10) {
                    val isTop10 = selectedIds == top10Ids
                    item(key = "preset_top10") {
                        FilterChip(
                            selected = isTop10,
                            onClick = { selectedIds = top10Ids },
                            label = { Text("Top 10", style = MaterialTheme.typography.labelMedium) },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(isTop10),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("batch_preset_top10")
                        )
                    }
                }
                if (favoriteCount > 0) {
                    val isFavs = selectedIds == favIds
                    item(key = "preset_favs") {
                        FilterChip(
                            selected = isFavs,
                            onClick = { selectedIds = favIds },
                            label = { Text("Favorites ($favoriteCount)", style = MaterialTheme.typography.labelMedium) },
                            colors = VedographiDefaults.filterChipColors(),
                            border = VedographiDefaults.filterChipBorder(isFavs),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("batch_preset_favorites")
                        )
                    }
                }
                item(key = "preset_clear") {
                    val isEmpty = selectedIds.isEmpty()
                    FilterChip(
                        selected = isEmpty,
                        onClick = { selectedIds = emptySet() },
                        label = { Text("Clear", style = MaterialTheme.typography.labelMedium) },
                        colors = VedographiDefaults.filterChipColors(),
                        border = VedographiDefaults.filterChipBorder(isEmpty),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("batch_preset_clear")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Format toggle: Style Name + Text vs Clean Lines
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Include style labels",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (includeStyleLabels) {
                            "Copies as \"Style Name: text\""
                        } else {
                            "Copies pure styled variations line by line"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = includeStyleLabels,
                    onCheckedChange = { includeStyleLabels = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CrispWhite,
                        checkedTrackColor = RoyalViolet
                    ),
                    modifier = Modifier.testTag("batch_include_labels_switch")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Combined Live Clipboard Preview
            CopyFeedbackPreviewBox(
                pulseState = copyPulse,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formattedBatchText.ifBlank { "Select one or more variations below to copy." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (formattedBatchText.isBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Checkable variations list
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 240.dp)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(state.variations, key = { it.id }) { variation ->
                        val checked = variation.id in selectedIds
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedIds = if (checked) {
                                        selectedIds - variation.id
                                    } else {
                                        selectedIds + variation.id
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .minimumInteractiveComponentSize()
                                .testTag("batch_item_${variation.id}")
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    selectedIds = if (isChecked) {
                                        selectedIds + variation.id
                                    } else {
                                        selectedIds - variation.id
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = RoyalViolet,
                                    checkmarkColor = CrispWhite
                                )
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = variation.styleName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = variation.styledText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (formattedBatchText.isNotEmpty()) {
                            onShareBatch(formattedBatchText, selectedVariations.size)
                        }
                    },
                    enabled = selectedVariations.isNotEmpty(),
                    shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                    colors = VedographiDefaults.outlinedButtonColors(),
                    border = VedographiDefaults.outlinedButtonBorder(enabled = selectedVariations.isNotEmpty()),
                    modifier = Modifier
                        .height(VedographiDefaults.ButtonHeight)
                        .testTag("batch_share_btn")
                ) {
                    Icon(Icons.Filled.Share, contentDescription = "Share selected", modifier = Modifier.size(18.dp))
                }

                Button(
                    onClick = {
                        if (formattedBatchText.isNotEmpty()) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            copyPulse.trigger()
                            onCopyBatch(formattedBatchText, selectedVariations.size)
                        }
                    },
                    enabled = selectedVariations.isNotEmpty(),
                    shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                    colors = VedographiDefaults.primaryButtonColors(),
                    modifier = Modifier
                        .weight(1f)
                        .height(VedographiDefaults.ButtonHeight)
                        .testTag("batch_copy_confirm_btn")
                ) {
                    Icon(Icons.Filled.CopyAll, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            selectedVariations.isEmpty() -> "Select Variations"
                            allSelected -> "Copy All (${selectedVariations.size})"
                            else -> "Copy ${selectedVariations.size} Selected"
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
