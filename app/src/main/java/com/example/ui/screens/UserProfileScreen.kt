package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.CachedStyleEntry
import com.example.domain.SavedCreation
import com.example.ui.AccountDeletionState
import com.example.ui.FontPreviewItem
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FontPreviewCard
import com.example.ui.components.VedographiContentCard
import com.example.ui.components.VedographiDefaults
import com.example.ui.components.VedographiHeroCard
import com.example.ui.components.VedographiPillBadge
import com.example.ui.components.VedographiSectionHeader
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonOrchid
import com.example.ui.theme.RoyalViolet

private val ProfileSections = listOf("All", "Favs", "Projects", "Cached")

@Composable
fun UserProfileScreen(
    displayName: String?,
    email: String?,
    isGuest: Boolean = false,
    isSigningIn: Boolean = false,
    signInErrorMessage: String? = null,
    isOnline: Boolean = true,
    isCloudSynced: Boolean,
    isBackingUp: Boolean,
    accountDeletionState: AccountDeletionState = AccountDeletionState.Idle,
    favoriteFonts: List<FontPreviewItem>,
    savedCreations: List<SavedCreation>,
    cachedStyles: List<CachedStyleEntry> = emptyList(),
    recentFontsCount: Int,
    onBackupNow: () -> Unit,
    onSignInWithGoogle: () -> Unit = {},
    onDismissSignInError: () -> Unit = {},
    onSignOut: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onShareApp: () -> Unit = {},
    onClearLocalData: () -> Unit = {},
    onDeleteCloudAccount: () -> Unit = {},
    onConfirmReauthAndDelete: () -> Unit = {},
    onDismissDeletionFeedback: () -> Unit = {},
    onCopyFontItem: (FontPreviewItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onShareFontItem: (FontPreviewItem) -> Unit,
    onCopyProject: (String) -> Unit,
    onShareProject: (String) -> Unit,
    onOpenProjectInStudio: (SavedCreation) -> Unit,
    onDeleteProject: (String) -> Unit,
    onRestoreCachedEntry: (CachedStyleEntry) -> Unit = {},
    onRemoveCachedStyle: (String) -> Unit = {},
    onClearCachedStyles: () -> Unit = {},
    onNavigateToFonts: () -> Unit,
    onNavigateToStudio: () -> Unit,
    isAdmin: Boolean = false,
    pointsBalance: Int = 0,
    onOpenRewards: () -> Unit = {},
    onOpenHowToUse: () -> Unit = {},
    onOpenAdminPanel: () -> Unit = {},
    onOpenStoreHub: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSection by rememberSaveable { mutableIntStateOf(0) }
    var selectedProjectKind by rememberSaveable { mutableStateOf("ALL") }
    var showClearLocalConfirm by remember { mutableStateOf(false) }
    var showDeleteAccountConfirm by remember { mutableStateOf(false) }

    val availableKinds = remember(savedCreations) {
        listOf("ALL") + savedCreations.map { it.kind }.distinct()
    }
    val filteredProjects = remember(savedCreations, selectedProjectKind) {
        if (selectedProjectKind == "ALL") {
            savedCreations
        } else {
            savedCreations.filter { it.kind == selectedProjectKind }
        }
    }

    val safeName = if (isGuest) {
        stringResource(R.string.guest_display_name)
    } else {
        displayName?.takeIf { it.isNotBlank() } ?: "VEDOGRAPHi Creator"
    }
    val initialChar = if (isGuest) {
        "G"
    } else {
        (displayName?.firstOrNull() ?: email?.firstOrNull() ?: 'V')
            .uppercaseChar()
            .toString()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Profile Card
        item(key = "profile_hero") {
            VedographiHeroCard(contentPadding = PaddingValues(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(RoyalViolet, ElectricViolet, NeonOrchid))
                            )
                    ) {
                        Text(
                            text = initialChar,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = CrispWhite
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = safeName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isGuest) {
                            Text(
                                text = stringResource(R.string.guest_mode_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (!email.isNullOrBlank()) {
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        val statusTint = when {
                            isGuest -> MaterialTheme.colorScheme.primary
                            !isOnline -> NeonOrchid
                            isCloudSynced -> EmeraldSuccess
                            else -> MaterialTheme.colorScheme.primary
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = statusTint.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, statusTint.copy(alpha = 0.45f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        isGuest -> Icons.Outlined.PersonOutline
                                        !isOnline -> Icons.Filled.CloudOff
                                        isCloudSynced -> Icons.Filled.CloudDone
                                        else -> Icons.Filled.CloudSync
                                    },
                                    contentDescription = null,
                                    tint = statusTint,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when {
                                        isGuest -> stringResource(R.string.guest_mode_badge)
                                        !isOnline -> "Offline • DataStore Cache Active"
                                        isCloudSynced -> "Cloud + DataStore Cache Active"
                                        else -> "Syncing with Cloud"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = statusTint
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileStatTile(
                        count = favoriteFonts.size,
                        label = "Favorites",
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatTile(
                        count = savedCreations.size,
                        label = "Projects",
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatTile(
                        count = cachedStyles.size,
                        label = "Cached",
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatTile(
                        count = recentFontsCount,
                        label = "Recent",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Backup / Sign-In / Sign-Out Actions
                if (isGuest) {
                    Button(
                        onClick = onSignInWithGoogle,
                        enabled = !isSigningIn,
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.primaryButtonColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(VedographiDefaults.ButtonHeight)
                            .testTag("profile_google_sign_in_button")
                    ) {
                        if (isSigningIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = CrispWhite,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Signing in...", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Login,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.sign_in_with_google),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    if (!signInErrorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onDismissSignInError)
                        ) {
                            Text(
                                text = signInErrorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onBackupNow,
                            enabled = !isBackingUp,
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            colors = VedographiDefaults.primaryButtonColors(),
                            modifier = Modifier
                                .weight(1f)
                                .height(VedographiDefaults.ButtonHeight)
                                .testTag("profile_backup_button")
                        ) {
                            if (isBackingUp) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = CrispWhite,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Backing up...", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.CloudSync,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Backup Now", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                            }
                        }

                        OutlinedButton(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            colors = VedographiDefaults.outlinedButtonColors(),
                            border = VedographiDefaults.outlinedButtonBorder(),
                            modifier = Modifier
                                .height(VedographiDefaults.ButtonHeight)
                                .testTag("profile_sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign Out", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }

        // Section Filter Switcher
        item(key = "profile_section_tabs") {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ProfileSections.forEachIndexed { idx, label ->
                    val displayLabel = when (idx) {
                        1 -> "Favs (${favoriteFonts.size})"
                        2 -> "Saved (${savedCreations.size})"
                        3 -> "Cache (${cachedStyles.size})"
                        else -> label
                    }
                    SegmentedButton(
                        selected = selectedSection == idx,
                        onClick = { selectedSection = idx },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = idx,
                            count = ProfileSections.size
                        ),
                        colors = VedographiDefaults.segmentedButtonColors(),
                        modifier = Modifier.testTag("profile_tab_$idx")
                    ) {
                        Text(
                            displayLabel,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Favorite Fonts Section
        if (selectedSection == 0 || selectedSection == 1) {
            item(key = "fav_section_header") {
                VedographiSectionHeader(
                    title = "Saved Favorite Fonts (${favoriteFonts.size})",
                    icon = Icons.Filled.Favorite,
                    iconTint = NeonOrchid
                )
            }

            if (favoriteFonts.isEmpty()) {
                item(key = "fav_empty") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        EmptyStateCard(
                            title = "No favorite fonts saved yet",
                            subtitle = "Heart any of the 220+ Unicode fonts to back them up to your profile.",
                            icon = Icons.Outlined.FavoriteBorder
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = onNavigateToFonts,
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            colors = VedographiDefaults.outlinedButtonColors(),
                            border = VedographiDefaults.outlinedButtonBorder(),
                            modifier = Modifier.height(VedographiDefaults.ButtonHeight)
                        ) {
                            Icon(Icons.Filled.TextFields, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Explore 220+ Fonts", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            } else {
                items(favoriteFonts, key = { "profile_fav_${it.spec.id}" }) { item ->
                    FontPreviewCard(
                        item = item,
                        onCopy = { onCopyFontItem(item) },
                        onToggleFavorite = { onToggleFavorite(item.spec.id) },
                        onShare = { onShareFontItem(item) }
                    )
                }
            }
        }

        // Custom Studio Projects Section
        if (selectedSection == 0 || selectedSection == 2) {
            item(key = "projects_section_header") {
                Spacer(modifier = Modifier.height(4.dp))
                VedographiSectionHeader(
                    title = "Custom Studio Projects (${savedCreations.size})",
                    icon = Icons.Filled.AutoFixHigh,
                    iconTint = MaterialTheme.colorScheme.primary
                )
            }

            if (availableKinds.size > 2) {
                item(key = "project_kind_filters") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableKinds) { kind ->
                            val selected = kind == selectedProjectKind
                            FilterChip(
                                selected = selected,
                                onClick = { selectedProjectKind = kind },
                                label = { Text(kind, style = MaterialTheme.typography.labelLarge) },
                                colors = VedographiDefaults.filterChipColors(),
                                border = VedographiDefaults.filterChipBorder(selected),
                                modifier = Modifier.height(40.dp)
                            )
                        }
                    }
                }
            }

            if (filteredProjects.isEmpty()) {
                item(key = "projects_empty") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        EmptyStateCard(
                            title = "No custom projects saved yet",
                            subtitle = "Save decorated texts, bios, usernames, stylish names, or WhatsApp statuses from My Text Studio.",
                            icon = Icons.Outlined.BookmarkBorder
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = onNavigateToStudio,
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            colors = VedographiDefaults.outlinedButtonColors(),
                            border = VedographiDefaults.outlinedButtonBorder(),
                            modifier = Modifier.height(VedographiDefaults.ButtonHeight)
                        ) {
                            Icon(Icons.Filled.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open My Text Studio", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            } else {
                items(filteredProjects, key = { "profile_proj_${it.id}" }) { project ->
                    SavedProjectCard(
                        project = project,
                        onCopy = { onCopyProject(project.content) },
                        onShare = { onShareProject(project.content) },
                        onOpenInStudio = { onOpenProjectInStudio(project) },
                        onDelete = { onDeleteProject(project.id) }
                    )
                }
            }
        }

        // Offline DataStore Cache Section (Previously Generated Styles & Templates)
        if (selectedSection == 0 || selectedSection == 3) {
            item(key = "profile_cache_header") {
                Spacer(modifier = Modifier.height(4.dp))
                VedographiSectionHeader(
                    title = "Offline Cached Styles (${cachedStyles.size})",
                    icon = Icons.Filled.History,
                    iconTint = NeonOrchid,
                    action = if (cachedStyles.isNotEmpty()) {
                        {
                            TextButton(
                                onClick = onClearCachedStyles,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text("Clear", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    } else {
                        null
                    }
                )
            }

            if (cachedStyles.isEmpty()) {
                item(key = "profile_cache_empty") {
                    EmptyStateCard(
                        title = "No cached styles or templates yet",
                        subtitle = "Copy, share, or customize any font or template and it will stay accessible offline via DataStore.",
                        icon = Icons.Filled.History
                    )
                }
            } else {
                items(cachedStyles, key = { "profile_cache_${it.id}" }) { entry ->
                    CachedStyleCard(
                        entry = entry,
                        onCopy = { onCopyProject(entry.styledText) },
                        onReuse = { onRestoreCachedEntry(entry) },
                        onShare = { onShareProject(entry.styledText) },
                        onDelete = { onRemoveCachedStyle(entry.id) }
                    )
                }
            }
        }

        // Settings, Legal, Share & Data Controls Section
        item(key = "account_and_privacy_card") {
            Spacer(modifier = Modifier.height(6.dp))
            VedographiContentCard(
                contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.account_and_privacy_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGuest) {
                        "Privacy details, app info, native share, and local device data controls."
                    } else {
                        "Privacy details, app info, native share, local cache wipe, or permanent account deletion."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileSettingsActionRow(
                    icon = Icons.Filled.Diamond,
                    iconTint = NeonOrchid,
                    title = "Earn Points & Refer (+2 pts)",
                    subtitle = "Wallet: $pointsBalance pts • Watch Ads & Unlock Pro Fonts",
                    testTag = "profile_rewards_btn",
                    onClick = onOpenRewards
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                ProfileSettingsActionRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    iconTint = EmeraldSuccess,
                    title = stringResource(R.string.how_to_use_title),
                    subtitle = "Step-by-step guide for WhatsApp, Hindi & Custom Studio",
                    testTag = "profile_how_to_use_btn",
                    onClick = onOpenHowToUse
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (isAdmin) {
                    ProfileSettingsActionRow(
                        icon = Icons.Filled.AdminPanelSettings,
                        iconTint = EmeraldSuccess,
                        title = stringResource(R.string.admin_panel_title),
                        subtitle = "AdMob IDs, rewards & admin test tools",
                        testTag = "profile_admin_panel_btn",
                        onClick = onOpenAdminPanel
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                ProfileSettingsActionRow(
                    icon = Icons.Filled.RocketLaunch,
                    iconTint = ElectricViolet,
                    title = "Play Store & App Icon Hub",
                    subtitle = "Adaptive launcher icon & publishing readiness checklist",
                    testTag = "profile_store_hub_btn",
                    onClick = onOpenStoreHub
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                ProfileSettingsActionRow(
                    icon = Icons.Filled.PrivacyTip,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.privacy_policy_title),
                    subtitle = "Permissions, data safety & deletion rights",
                    testTag = "profile_privacy_policy_btn",
                    onClick = onOpenPrivacyPolicy
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                ProfileSettingsActionRow(
                    icon = Icons.Filled.Info,
                    iconTint = NeonOrchid,
                    title = stringResource(R.string.about_app_title),
                    subtitle = "Version, brand mission & developer contact",
                    testTag = "profile_about_btn",
                    onClick = onOpenAbout
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                ProfileSettingsActionRow(
                    icon = Icons.Filled.Share,
                    iconTint = EmeraldSuccess,
                    title = stringResource(R.string.share_app_title),
                    subtitle = "Send VEDOGRAPHi link to friends",
                    testTag = "profile_share_app_btn",
                    onClick = onShareApp
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                ProfileSettingsActionRow(
                    icon = Icons.Filled.DeleteSweep,
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    title = stringResource(R.string.clear_local_data),
                    subtitle = "Remove cached styles, drafts & local recents",
                    testTag = "profile_clear_local_data_btn",
                    onClick = { showClearLocalConfirm = true }
                )

                if (!isGuest) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileSettingsActionRow(
                        icon = Icons.Filled.DeleteForever,
                        iconTint = MaterialTheme.colorScheme.error,
                        title = stringResource(R.string.delete_cloud_account),
                        subtitle = "Permanently delete Firestore profile & account",
                        titleColor = MaterialTheme.colorScheme.error,
                        enabled = accountDeletionState !is AccountDeletionState.Deleting,
                        testTag = "profile_delete_account_btn",
                        onClick = { showDeleteAccountConfirm = true }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }

    if (showClearLocalConfirm) {
        AlertDialog(
            onDismissRequest = { showClearLocalConfirm = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Clear Local Cache & Data?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    if (isGuest) {
                        "This removes all locally saved favorites, recent fonts, Studio creations, drafts, and offline cached styles from this device."
                    } else {
                        "This removes locally cached styles, drafts, and offline DataStore entries on this device. Your Google Cloud backup stays safe unless you delete your account."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearLocalConfirm = false
                        onClearLocalData()
                    },
                    shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Clear Local Data", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearLocalConfirm = false }) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }

    if (showDeleteAccountConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirm = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Delete Account & Cloud Data?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "This action is permanent and cannot be undone. It will permanently delete:\n" +
                        "• Your Cloud Firestore profile & saved favorites\n" +
                        "• All cloud-synced Studio projects\n" +
                        "• Your Firebase Authentication account link\n" +
                        "• All local offline DataStore cache & drafts",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountConfirm = false
                        onDeleteCloudAccount()
                    },
                    shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete Permanently", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirm = false }) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }

    when (accountDeletionState) {
        AccountDeletionState.Deleting -> {
            AlertDialog(
                onDismissRequest = {},
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                title = { Text("Deleting Account", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            "Removing your cloud creations, profile, and account...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                confirmButton = {}
            )
        }
        AccountDeletionState.ReauthRequired -> {
            AlertDialog(
                onDismissRequest = onDismissDeletionFeedback,
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                title = { Text("Google Verification Required", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Text(
                        "For your security, please verify your Google sign-in once more to permanently delete your account.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmReauthAndDelete,
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Verify & Delete", style = MaterialTheme.typography.labelLarge)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDeletionFeedback) {
                        Text("Cancel", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        }
        is AccountDeletionState.Error -> {
            AlertDialog(
                onDismissRequest = onDismissDeletionFeedback,
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                title = { Text("Account Deletion Failed", style = MaterialTheme.typography.titleLarge) },
                text = { Text(accountDeletionState.message, style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    TextButton(onClick = onDismissDeletionFeedback) {
                        Text("OK", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        }
        AccountDeletionState.Idle,
        AccountDeletionState.Completed -> Unit
    }
}

@Composable
private fun ProfileSettingsActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    testTag: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.14f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = titleColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileStatTile(
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SavedProjectCard(
    project: SavedCreation,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onOpenInStudio: () -> Unit,
    onDelete: () -> Unit
) {
    VedographiContentCard(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.testTag("profile_project_${project.id}")
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
                VedographiPillBadge(text = project.kind)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = project.title,
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
                    contentDescription = "Delete project",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        SelectionContainer {
            Text(
                text = project.content,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 25.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCopy,
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
                onClick = onOpenInStudio,
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
                Text("Studio", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
            }
            OutlinedButton(
                onClick = onShare,
                shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                colors = VedographiDefaults.outlinedButtonColors(),
                border = VedographiDefaults.outlinedButtonBorder(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(VedographiDefaults.ButtonHeight)
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Share project", modifier = Modifier.size(17.dp))
            }
        }
    }
}
