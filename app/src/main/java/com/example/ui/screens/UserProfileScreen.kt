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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PrivacyTip
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
import androidx.compose.material3.FilterChipDefaults
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
    onSignOut: () -> Unit,
    onOpenPrivacyAndAbout: () -> Unit = {},
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
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                border = BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(listOf(ElectricViolet, NeonOrchid))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                color = Color.White
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
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = when {
                                    isGuest -> ElectricViolet.copy(alpha = 0.16f)
                                    !isOnline -> NeonOrchid.copy(alpha = 0.16f)
                                    isCloudSynced -> EmeraldSuccess.copy(alpha = 0.14f)
                                    else -> ElectricViolet.copy(alpha = 0.14f)
                                }
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
                                        tint = when {
                                            isGuest -> ElectricViolet
                                            !isOnline -> NeonOrchid
                                            isCloudSynced -> EmeraldSuccess
                                            else -> ElectricViolet
                                        },
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
                                        color = when {
                                            isGuest -> ElectricViolet
                                            !isOnline -> NeonOrchid
                                            isCloudSynced -> EmeraldSuccess
                                            else -> ElectricViolet
                                        }
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
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalViolet,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("profile_google_sign_in_button")
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Signing in...", maxLines = 1, softWrap = false)
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Login,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.sign_in_with_google),
                                    maxLines = 1,
                                    softWrap = false
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
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RoyalViolet,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("profile_backup_button")
                            ) {
                                if (isBackingUp) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Backing up...", maxLines = 1, softWrap = false)
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.CloudSync,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Backup Now", maxLines = 1, softWrap = false)
                                }
                            }

                            OutlinedButton(
                                onClick = onSignOut,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("profile_sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sign Out", maxLines = 1, softWrap = false)
                            }
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
                        modifier = Modifier.testTag("profile_tab_$idx")
                    ) {
                        Text(displayLabel, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        // Favorite Fonts Section
        if (selectedSection == 0 || selectedSection == 1) {
            item(key = "fav_section_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = NeonOrchid,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Saved Favorite Fonts (${favoriteFonts.size})",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            if (favoriteFonts.isEmpty()) {
                item(key = "fav_empty") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        EmptyStateCard(
                            title = "No favorite fonts saved yet",
                            subtitle = "Heart any of the 220+ Unicode fonts to back them up to your profile.",
                            icon = Icons.Outlined.FavoriteBorder
                        )
                        OutlinedButton(
                            onClick = onNavigateToFonts,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Filled.TextFields, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Explore 220+ Fonts")
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoFixHigh,
                            contentDescription = null,
                            tint = ElectricViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Custom Studio Projects (${savedCreations.size})",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            if (availableKinds.size > 2) {
                item(key = "project_kind_filters") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableKinds) { kind ->
                            FilterChip(
                                selected = kind == selectedProjectKind,
                                onClick = { selectedProjectKind = kind },
                                label = { Text(kind) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
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
                        OutlinedButton(
                            onClick = onNavigateToStudio,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Filled.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open My Text Studio")
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            tint = NeonOrchid,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Offline Cached Styles (${cachedStyles.size})",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    if (cachedStyles.isNotEmpty()) {
                        TextButton(onClick = onClearCachedStyles) {
                            Text("Clear")
                        }
                    }
                }
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

        // Account, Privacy Policy & Data Deletion Section
        item(key = "account_and_privacy_card") {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PrivacyTip,
                            contentDescription = null,
                            tint = ElectricViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.account_and_privacy_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = if (isGuest) {
                            "Review data practices or clear locally stored cache, drafts, and favorites from this device."
                        } else {
                            "Review data practices, wipe local device cache, or permanently delete your Google-linked cloud account and data."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = onOpenPrivacyAndAbout,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_privacy_about_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PrivacyTip,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.privacy_and_about), maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = { showClearLocalConfirm = true },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_clear_local_data_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.clear_local_data), maxLines = 1)
                    }

                    if (!isGuest) {
                        OutlinedButton(
                            onClick = { showDeleteAccountConfirm = true },
                            enabled = accountDeletionState !is AccountDeletionState.Deleting,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("profile_delete_account_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteForever,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.delete_cloud_account), maxLines = 1)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }

    if (showClearLocalConfirm) {
        AlertDialog(
            onDismissRequest = { showClearLocalConfirm = false },
            title = { Text("Clear Local Cache & Data?") },
            text = {
                Text(
                    if (isGuest) {
                        "This removes all locally saved favorites, recent fonts, Studio creations, drafts, and offline cached styles from this device."
                    } else {
                        "This removes locally cached styles, drafts, and offline DataStore entries on this device. Your Google Cloud backup stays safe unless you delete your account."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearLocalConfirm = false
                        onClearLocalData()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Clear Local Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearLocalConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteAccountConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirm = false },
            title = { Text("Delete Account & Cloud Data?") },
            text = {
                Text(
                    "This action is permanent and cannot be undone. It will permanently delete:\n" +
                        "• Your Cloud Firestore profile & saved favorites\n" +
                        "• All cloud-synced Studio projects\n" +
                        "• Your Firebase Authentication account link\n" +
                        "• All local offline DataStore cache & drafts"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountConfirm = false
                        onDeleteCloudAccount()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    when (accountDeletionState) {
        AccountDeletionState.Deleting -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Deleting Account") },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Removing your cloud creations, profile, and account...")
                    }
                },
                confirmButton = {}
            )
        }
        AccountDeletionState.ReauthRequired -> {
            AlertDialog(
                onDismissRequest = onDismissDeletionFeedback,
                title = { Text("Google Verification Required") },
                text = {
                    Text("For your security, please verify your Google sign-in once more to permanently delete your account.")
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmReauthAndDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Verify & Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDeletionFeedback) {
                        Text("Cancel")
                    }
                }
            )
        }
        is AccountDeletionState.Error -> {
            AlertDialog(
                onDismissRequest = onDismissDeletionFeedback,
                title = { Text("Account Deletion Failed") },
                text = { Text(accountDeletionState.message) },
                confirmButton = {
                    TextButton(onClick = onDismissDeletionFeedback) {
                        Text("OK")
                    }
                }
            )
        }
        AccountDeletionState.Idle,
        AccountDeletionState.Completed -> Unit
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
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
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_project_${project.id}")
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
                            text = project.kind,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
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
                    onClick = onOpenInStudio,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Studio", maxLines = 1, softWrap = false)
                }
                OutlinedButton(
                    onClick = onShare,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = "Share project", modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}
