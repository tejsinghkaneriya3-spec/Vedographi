package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.auth.authStateFlow
import com.example.auth.clearCredentialSession
import com.example.auth.deleteCurrentFirebaseUser
import com.example.auth.onGoogleSignInClicked
import com.example.auth.reauthenticateWithGoogle
import com.example.auth.signOut
import com.example.data.CloudSyncRepository
import com.example.data.PreferencesRepository
import com.example.domain.CachedEntrySource
import com.example.domain.CachedStyleEntry
import com.example.domain.FontCatalog
import com.example.domain.SocialPlatform
import com.example.ui.FontPreviewItem
import com.example.ui.VedographiViewModel
import com.example.ui.components.SocialShareBottomSheet
import com.example.ui.components.VedographiDefaults
import com.example.ui.components.VedographiSplashScreen
import com.example.ui.components.VedographiTopHeader
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BioCaptionScreen
import com.example.ui.screens.FontsHomeScreen
import com.example.ui.screens.MyTextStudioScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.screens.UsernameScreen
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.VedographiTheme
import com.example.util.NetworkMonitor
import com.example.util.ShareAndClipboardHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

const val ROUTE_FONTS = "fonts_home"
const val ROUTE_BIOS = "bio_captions"
const val ROUTE_USERNAMES = "usernames"
const val ROUTE_STUDIO = "studio_decorator"
const val ROUTE_PROFILE = "user_profile"
const val ROUTE_PRIVACY = "privacy_policy"
const val ROUTE_ABOUT = "about_app"

private enum class AuthLegalOverlay {
    NONE,
    PRIVACY,
    ABOUT
}

private data class NavDestinationItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val NavDestinations = listOf(
    NavDestinationItem(
        route = ROUTE_FONTS,
        labelRes = R.string.nav_fonts,
        selectedIcon = Icons.Filled.TextFields,
        unselectedIcon = Icons.Outlined.TextFields
    ),
    NavDestinationItem(
        route = ROUTE_BIOS,
        labelRes = R.string.nav_bios,
        selectedIcon = Icons.Filled.FormatQuote,
        unselectedIcon = Icons.Outlined.FormatQuote
    ),
    NavDestinationItem(
        route = ROUTE_USERNAMES,
        labelRes = R.string.nav_usernames,
        selectedIcon = Icons.Filled.AlternateEmail,
        unselectedIcon = Icons.Outlined.AlternateEmail
    ),
    NavDestinationItem(
        route = ROUTE_STUDIO,
        labelRes = R.string.nav_studio,
        selectedIcon = Icons.Filled.AutoFixHigh,
        unselectedIcon = Icons.Outlined.AutoFixHigh
    ),
    NavDestinationItem(
        route = ROUTE_PROFILE,
        labelRes = R.string.nav_profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val localPrefs = remember { PreferencesRepository(applicationContext) }
            val localDarkTheme by localPrefs.isDarkTheme.collectAsStateWithLifecycle(initialValue = true)
            val isGuestModeEnabled by localPrefs.guestModeEnabled.collectAsStateWithLifecycle(initialValue = false)
            val currentUser by Firebase.auth.authStateFlow()
                .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)
            val rootScope = rememberCoroutineScope()

            var authLegalOverlay by rememberSaveable { mutableStateOf(AuthLegalOverlay.NONE) }
            var skipAutoSignIn by rememberSaveable { mutableStateOf(false) }
            var authStatusBanner by rememberSaveable { mutableStateOf<String?>(null) }
            var authSessionGeneration by rememberSaveable { mutableIntStateOf(0) }
            var lastObservedSessionUid by rememberSaveable { mutableStateOf(Firebase.auth.currentUser?.uid) }
            var pendingPostSignInMessage by rememberSaveable { mutableStateOf<String?>(null) }

            var activeDarkTheme by remember { mutableStateOf(localDarkTheme) }
            LaunchedEffect(currentUser?.uid) {
                val newUid = currentUser?.uid
                if (newUid != lastObservedSessionUid) {
                    if (lastObservedSessionUid == null && newUid != null) {
                        val who = currentUser?.displayName?.takeIf { it.isNotBlank() }
                            ?: currentUser?.email?.takeIf { it.isNotBlank() }
                            ?: "Google Account"
                        pendingPostSignInMessage = "Signed in as $who! Syncing profile & studio..."
                    }
                    lastObservedSessionUid = newUid
                    authSessionGeneration += 1
                }
            }
            LaunchedEffect(localDarkTheme, currentUser) {
                if (currentUser == null) {
                    activeDarkTheme = localDarkTheme
                } else {
                    localPrefs.setGuestMode(false)
                }
            }

            LaunchedEffect(activeDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (activeDarkTheme) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    },
                    navigationBarStyle = if (activeDarkTheme) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    }
                )
            }

            VedographiTheme(darkTheme = activeDarkTheme) {
                var showSplash by rememberSaveable { mutableStateOf(true) }

                Crossfade(targetState = showSplash, label = "splash_crossfade") { isSplash ->
                    if (isSplash) {
                        VedographiSplashScreen(onFinished = { showSplash = false })
                    } else {
                        val user = currentUser
                        if (user == null && !isGuestModeEnabled) {
                            when (authLegalOverlay) {
                                AuthLegalOverlay.PRIVACY -> PrivacyPolicyScreen(
                                    onBack = { authLegalOverlay = AuthLegalOverlay.NONE }
                                )
                                AuthLegalOverlay.ABOUT -> AboutScreen(
                                    onBack = { authLegalOverlay = AuthLegalOverlay.NONE },
                                    onOpenPrivacyPolicy = { authLegalOverlay = AuthLegalOverlay.PRIVACY }
                                )
                                AuthLegalOverlay.NONE -> AuthScreen(
                                    onAuthSuccess = {
                                        authStatusBanner = null
                                        skipAutoSignIn = false
                                    },
                                    onContinueAsGuest = {
                                        authStatusBanner = null
                                        rootScope.launch {
                                            localPrefs.setGuestMode(true)
                                        }
                                    },
                                    onOpenPrivacyPolicy = { authLegalOverlay = AuthLegalOverlay.PRIVACY },
                                    onOpenAbout = { authLegalOverlay = AuthLegalOverlay.ABOUT },
                                    skipAutoSignIn = skipAutoSignIn,
                                    statusBannerMessage = authStatusBanner
                                )
                            }
                        } else {
                            val sessionKey = "${user?.uid ?: "guest"}_$authSessionGeneration"
                            key(sessionKey) {
                                val viewModel: VedographiViewModel = viewModel(
                                    key = sessionKey,
                                    factory = viewModelFactory {
                                        initializer {
                                            val app = checkNotNull(this[APPLICATION_KEY]) {
                                                "APPLICATION_KEY missing from CreationExtras"
                                            }
                                            if (user != null) {
                                                val databaseId = app.getString(R.string.firestore_database_id)
                                                val db = FirebaseFirestore.getInstance(databaseId)
                                                VedographiViewModel(
                                                    repo = PreferencesRepository(app),
                                                    cloudRepo = CloudSyncRepository(db),
                                                    currentUserId = user.uid,
                                                    userDisplayName = user.displayName,
                                                    networkOnlineFlow = NetworkMonitor(app).isOnline
                                                )
                                            } else {
                                                VedographiViewModel(
                                                    repo = PreferencesRepository(app),
                                                    cloudRepo = null,
                                                    currentUserId = null,
                                                    userDisplayName = null,
                                                    networkOnlineFlow = NetworkMonitor(app).isOnline
                                                )
                                            }
                                        }
                                    }
                                )
                                val syncedDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
                                LaunchedEffect(syncedDarkTheme) {
                                    activeDarkTheme = syncedDarkTheme
                                }
                                VedographiAppShell(
                                    viewModel = viewModel,
                                    isDarkTheme = syncedDarkTheme,
                                    currentUser = user,
                                    pendingPostSignInMessage = pendingPostSignInMessage,
                                    onPostSignInMessageConsumed = { pendingPostSignInMessage = null },
                                    onBeforeExplicitSignOut = {
                                        skipAutoSignIn = true
                                    },
                                    onExplicitSignOut = {
                                        skipAutoSignIn = true
                                        rootScope.launch {
                                            localPrefs.clearSessionOnSignOut()
                                        }
                                    },
                                    onBeforeAccountDelete = {
                                        skipAutoSignIn = true
                                    },
                                    onAccountDeleted = { deletedMsg ->
                                        skipAutoSignIn = true
                                        authStatusBanner = deletedMsg
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VedographiAppShell(
    viewModel: VedographiViewModel,
    isDarkTheme: Boolean,
    currentUser: FirebaseUser?,
    pendingPostSignInMessage: String?,
    onPostSignInMessageConsumed: () -> Unit,
    onBeforeExplicitSignOut: () -> Unit,
    onExplicitSignOut: () -> Unit,
    onBeforeAccountDelete: () -> Unit,
    onAccountDeleted: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_FONTS
    val isDetailRoute = currentRoute == ROUTE_PRIVACY || currentRoute == ROUTE_ABOUT
    var showAccountDialog by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }
    var guestSignInError by remember { mutableStateOf<String?>(null) }
    val isGuest = currentUser == null

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            isSigningIn = false
            guestSignInError = null
        }
    }

    LaunchedEffect(pendingPostSignInMessage) {
        val msg = pendingPostSignInMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            onPostSignInMessageConsumed()
        }
    }

    // Collected States
    val effectiveDisplayName by viewModel.effectiveDisplayName.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isCloudSynced by viewModel.isCloudSynced.collectAsStateWithLifecycle()
    val isBackingUp by viewModel.isBackingUp.collectAsStateWithLifecycle()
    val accountDeletionState by viewModel.accountDeletionState.collectAsStateWithLifecycle()
    val cloudSyncError by viewModel.cloudSyncError.collectAsStateWithLifecycle()
    val cachedStyles by viewModel.cachedGeneratedStyles.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isInputEmpty by viewModel.isInputEmpty.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedCalligraphyPreset by viewModel.selectedCalligraphyPreset.collectAsStateWithLifecycle()
    val hasHindiInInput by viewModel.hasHindiInInput.collectAsStateWithLifecycle()
    val trendingItems by viewModel.trendingFontItems.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredFontItems.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteFontIds.collectAsStateWithLifecycle()
    val recentIds by viewModel.recentFontIds.collectAsStateWithLifecycle()
    val savedCreations by viewModel.savedCreations.collectAsStateWithLifecycle()
    val copiedFeedbackId by viewModel.copiedFeedbackId.collectAsStateWithLifecycle()
    val shareSheetPayload by viewModel.shareSheetPayload.collectAsStateWithLifecycle()

    LaunchedEffect(cloudSyncError) {
        val err = cloudSyncError
        if (err != null) {
            snackbarHostState.showSnackbar(err)
            viewModel.clearCloudSyncError()
        }
    }

    // Bio states
    val selectedBioPlatform by viewModel.selectedBioPlatform.collectAsStateWithLifecycle()
    val selectedBioCategory by viewModel.selectedBioCategory.collectAsStateWithLifecycle()
    val bioHeadline by viewModel.bioHeadline.collectAsStateWithLifecycle()
    val bioBodyText by viewModel.bioBodyText.collectAsStateWithLifecycle()
    val selectedBioFontId by viewModel.selectedBioFontId.collectAsStateWithLifecycle()
    val selectedBioDividerIdx by viewModel.selectedBioDividerIdx.collectAsStateWithLifecycle()
    val bioIncludeBullets by viewModel.bioIncludeBullets.collectAsStateWithLifecycle()
    val generatedBioPreview by viewModel.generatedBioPreview.collectAsStateWithLifecycle()

    // Username states
    val usernameInput by viewModel.usernameInput.collectAsStateWithLifecycle()
    val selectedUsernameVibe by viewModel.selectedUsernameVibe.collectAsStateWithLifecycle()
    val generatedUsernames by viewModel.generatedUsernames.collectAsStateWithLifecycle()

    // My Text Studio states
    val studioSelectedTab by viewModel.studioSelectedTab.collectAsStateWithLifecycle()
    val templateTagFilter by viewModel.templateTagFilter.collectAsStateWithLifecycle()
    val decoratorInput by viewModel.decoratorInput.collectAsStateWithLifecycle()
    val selectedDecoratorFontId by viewModel.selectedDecoratorFontId.collectAsStateWithLifecycle()
    val selectedWingId by viewModel.selectedWingId.collectAsStateWithLifecycle()
    val selectedBorderId by viewModel.selectedBorderId.collectAsStateWithLifecycle()
    val selectedSeparatorId by viewModel.selectedSeparatorId.collectAsStateWithLifecycle()
    val decoratedOutput by viewModel.decoratedOutput.collectAsStateWithLifecycle()
    val selectedOccasionCategory by viewModel.selectedOccasionCategory.collectAsStateWithLifecycle()
    val nameDesignerName by viewModel.nameDesignerName.collectAsStateWithLifecycle()
    val nameDesignerSubtitle by viewModel.nameDesignerSubtitle.collectAsStateWithLifecycle()
    val nameDesignerFontId by viewModel.nameDesignerFontId.collectAsStateWithLifecycle()
    val nameDesignerWingId by viewModel.nameDesignerWingId.collectAsStateWithLifecycle()
    val nameDesignerBadge by viewModel.nameDesignerBadge.collectAsStateWithLifecycle()
    val designedNameOutput by viewModel.designedNameOutput.collectAsStateWithLifecycle()
    val waHeaderTag by viewModel.waHeaderTag.collectAsStateWithLifecycle()
    val waBodyText by viewModel.waBodyText.collectAsStateWithLifecycle()
    val waFontId by viewModel.waFontId.collectAsStateWithLifecycle()
    val waDividerIdx by viewModel.waDividerIdx.collectAsStateWithLifecycle()
    val waShowFooter by viewModel.waShowFooter.collectAsStateWithLifecycle()
    val designedWaStatusOutput by viewModel.designedWaStatusOutput.collectAsStateWithLifecycle()

    fun showMessage(msg: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
        }
    }

    fun copyPlain(text: String, fontId: String? = null, label: String = "Copied to clipboard!") {
        if (ShareAndClipboardHelper.copyToClipboard(context, text)) {
            if (fontId != null) viewModel.markFontUsed(fontId, text)
            showMessage(label)
        }
    }

    val triggerGoogleSignInFromGuest: () -> Unit = {
        guestSignInError = null
        isSigningIn = true
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isSigningIn = false
                guestSignInError = null
                showAccountDialog = false
            },
            onAuthError = { msg ->
                isSigningIn = false
                guestSignInError = msg
                showMessage(msg)
            },
            scope = scope,
            onAuthCancelled = {
                isSigningIn = false
                guestSignInError = "Sign-in was cancelled. You can retry anytime."
            }
        )
    }

    val navigateTo: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val restoreCachedAndNavigate: (CachedStyleEntry) -> Unit = { entry ->
        val dest = viewModel.restoreCachedEntry(entry)
        when (dest) {
            "FONTS" -> navigateTo(ROUTE_FONTS)
            "BIOS" -> navigateTo(ROUTE_BIOS)
            "USERNAMES" -> navigateTo(ROUTE_USERNAMES)
            else -> navigateTo(ROUTE_STUDIO)
        }
        showMessage("Restored '${entry.title}' from offline cache")
    }

    val effectivePreviewSource = inputText.ifEmpty { "VEDOGRAPHi" }
    val favoriteFontItems = remember(favoriteIds, effectivePreviewSource, copiedFeedbackId) {
        FontCatalog.allStyles
            .filter { it.id in favoriteIds }
            .map { spec ->
                FontPreviewItem(
                    spec = spec,
                    previewText = spec.transform(effectivePreviewSource),
                    isFavorite = true,
                    isRecentlyCopied = copiedFeedbackId == spec.id
                )
            }
    }
    val recentFontItems = remember(recentIds, favoriteIds, effectivePreviewSource, copiedFeedbackId) {
        recentIds.mapNotNull { FontCatalog.findById(it) }.map { spec ->
            FontPreviewItem(
                spec = spec,
                previewText = spec.transform(effectivePreviewSource),
                isFavorite = spec.id in favoriteIds,
                isRecentlyCopied = copiedFeedbackId == spec.id
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        if (isDarkTheme) {
                            RoyalViolet.copy(alpha = 0.08f)
                        } else {
                            RoyalViolet.copy(alpha = 0.03f)
                        },
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.navigationBarsPadding()
                ) { data ->
                    Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        actionColor = MaterialTheme.colorScheme.primary
                    )
                }
            },
            bottomBar = {
                if (!useRail && !isDetailRoute) {
                    Column {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            NavDestinations.forEach { dest ->
                                val selected = currentRoute == dest.route
                                val label = stringResource(dest.labelRes)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTo(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag("nav_${dest.route}")
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail && !isDetailRoute) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxHeight()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        Spacer(modifier = Modifier.weight(0.2f))
                        NavDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            val label = stringResource(dest.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .then(if (isDetailRoute) Modifier else Modifier.statusBarsPadding())
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 840.dp)
                            .fillMaxSize()
                    ) {
                        if (!isDetailRoute) {
                            VedographiTopHeader(
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { viewModel.toggleDarkTheme() },
                                isCloudSynced = isCloudSynced,
                                isOnline = isOnline,
                                onAccountClick = {
                                    if (currentRoute == ROUTE_PROFILE) {
                                        showAccountDialog = true
                                    } else {
                                        navigateTo(ROUTE_PROFILE)
                                    }
                                }
                            )
                        }

                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_FONTS,
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(innerPadding)
                                .imePadding()
                        ) {
                            composable(ROUTE_FONTS) {
                                FontsHomeScreen(
                                    inputText = inputText,
                                    isInputEmpty = isInputEmpty,
                                    searchQuery = searchQuery,
                                    selectedCategory = selectedCategory,
                                    hasHindiInInput = hasHindiInInput,
                                    trendingItems = trendingItems,
                                    filteredItems = filteredItems,
                                    selectedCalligraphyPreset = selectedCalligraphyPreset,
                                    onSelectCalligraphyPreset = viewModel::selectCalligraphyPreset,
                                    onCopyCalligraphyText = { text ->
                                        copyPlain(text, label = "Copied Hindi calligraphy text!")
                                    },
                                    onShareCalligraphyCard = {
                                        val msg = ShareAndClipboardHelper.shareCalligraphyCard(
                                            context = context,
                                            text = inputText,
                                            preset = selectedCalligraphyPreset
                                        )
                                        showMessage(msg)
                                    },
                                    onInputChange = viewModel::updateInputText,
                                    onClearText = {
                                        viewModel.clearInputText()
                                        showMessage("Text cleared")
                                    },
                                    onPasteClick = {
                                        val pasted = ShareAndClipboardHelper.pasteFromClipboard(context)
                                        if (!pasted.isNullOrEmpty()) {
                                            viewModel.updateInputText(pasted)
                                            showMessage("Pasted from clipboard")
                                        } else {
                                            showMessage("Clipboard is empty")
                                        }
                                    },
                                    onCopyAllClick = {
                                        val allText = viewModel.buildCopyAllText()
                                        if (allText.isNotEmpty()) {
                                            copyPlain(
                                                text = allText,
                                                label = "Copied all ${filteredItems.size} styles!"
                                            )
                                        }
                                    },
                                    onSearchChange = viewModel::updateSearchQuery,
                                    onSelectCategory = viewModel::selectCategory,
                                    onCopyItem = { item ->
                                        copyPlain(
                                            text = item.previewText,
                                            fontId = item.spec.id,
                                            label = "Copied ${item.spec.name}!"
                                        )
                                    },
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onShareItem = { item ->
                                        viewModel.markFontUsed(item.spec.id, item.previewText)
                                        viewModel.openShareSheet(item.previewText)
                                    }
                                )
                            }

                            composable(ROUTE_BIOS) {
                                BioCaptionScreen(
                                    selectedPlatform = selectedBioPlatform,
                                    selectedCategory = selectedBioCategory,
                                    headline = bioHeadline,
                                    bodyText = bioBodyText,
                                    selectedFontId = selectedBioFontId,
                                    selectedDividerIdx = selectedBioDividerIdx,
                                    includeBullets = bioIncludeBullets,
                                    generatedPreview = generatedBioPreview,
                                    onSelectPlatform = viewModel::selectBioPlatform,
                                    onSelectCategory = viewModel::selectBioCategory,
                                    onShuffleTemplate = viewModel::shuffleBioTemplate,
                                    onHeadlineChange = viewModel::updateBioHeadline,
                                    onBodyChange = viewModel::updateBioBodyText,
                                    onSelectFont = viewModel::selectBioFont,
                                    onSelectDivider = viewModel::selectBioDivider,
                                    onToggleBullets = viewModel::toggleBioBullets,
                                    onCopyBio = { text ->
                                        viewModel.cacheStyleGeneration(
                                            title = bioHeadline.ifBlank { "${selectedBioPlatform.label} Bio" },
                                            inputText = bioBodyText,
                                            styledText = text,
                                            source = CachedEntrySource.BIO,
                                            fontId = selectedBioFontId
                                        )
                                        copyPlain(text, label = "Bio copied!")
                                    },
                                    onSaveBio = { text ->
                                        viewModel.saveCustomCreation(
                                            title = bioHeadline.ifBlank { "${selectedBioPlatform.label} Bio" },
                                            content = text,
                                            kind = "BIO"
                                        )
                                        showMessage("Saved bio to Studio!")
                                    },
                                    onShareBio = { text ->
                                        viewModel.cacheStyleGeneration(
                                            title = bioHeadline.ifBlank { "${selectedBioPlatform.label} Bio" },
                                            inputText = bioBodyText,
                                            styledText = text,
                                            source = CachedEntrySource.BIO,
                                            fontId = selectedBioFontId
                                        )
                                        viewModel.openShareSheet(text)
                                    }
                                )
                            }

                            composable(ROUTE_USERNAMES) {
                                UsernameScreen(
                                    usernameInput = usernameInput,
                                    selectedVibe = selectedUsernameVibe,
                                    generatedUsernames = generatedUsernames,
                                    onInputChange = viewModel::updateUsernameInput,
                                    onSelectVibe = viewModel::selectUsernameVibe,
                                    onShuffle = viewModel::shuffleUsernames,
                                    onCopyUsername = { handle ->
                                        viewModel.cacheStyleGeneration(
                                            title = "${selectedUsernameVibe.label} Handle",
                                            inputText = usernameInput,
                                            styledText = handle,
                                            source = CachedEntrySource.USERNAME
                                        )
                                        copyPlain(handle, label = "Username copied!")
                                    },
                                    onSaveUsername = { handle ->
                                        viewModel.saveCustomCreation(
                                            title = selectedUsernameVibe.label,
                                            content = handle,
                                            kind = "USERNAME"
                                        )
                                        showMessage("Saved username to Studio!")
                                    },
                                    onShareUsername = { handle ->
                                        viewModel.cacheStyleGeneration(
                                            title = "${selectedUsernameVibe.label} Handle",
                                            inputText = usernameInput,
                                            styledText = handle,
                                            source = CachedEntrySource.USERNAME
                                        )
                                        viewModel.openShareSheet(handle)
                                    }
                                )
                            }

                            composable(ROUTE_STUDIO) {
                                MyTextStudioScreen(
                                    selectedTab = studioSelectedTab,
                                    templateTagFilter = templateTagFilter,
                                    decoratorInput = decoratorInput,
                                    selectedDecoratorFontId = selectedDecoratorFontId,
                                    selectedWingId = selectedWingId,
                                    selectedBorderId = selectedBorderId,
                                    selectedSeparatorId = selectedSeparatorId,
                                    decoratedOutput = decoratedOutput,
                                    selectedOccasionCategory = selectedOccasionCategory,
                                    nameDesignerName = nameDesignerName,
                                    nameDesignerSubtitle = nameDesignerSubtitle,
                                    nameDesignerFontId = nameDesignerFontId,
                                    nameDesignerWingId = nameDesignerWingId,
                                    nameDesignerBadge = nameDesignerBadge,
                                    designedNameOutput = designedNameOutput,
                                    waHeaderTag = waHeaderTag,
                                    waBodyText = waBodyText,
                                    waFontId = waFontId,
                                    waDividerIdx = waDividerIdx,
                                    waShowFooter = waShowFooter,
                                    designedWaStatusOutput = designedWaStatusOutput,
                                    favoriteFonts = favoriteFontItems,
                                    recentFonts = recentFontItems,
                                    savedCreations = savedCreations,
                                    cachedStyles = cachedStyles,
                                    isGuest = isGuest,
                                    isOnline = isOnline,
                                    isCloudSynced = isCloudSynced,
                                    isBackingUp = isBackingUp,
                                    userEmail = currentUser?.email,
                                    onBackupNow = {
                                        viewModel.backupNow { msg -> showMessage(msg) }
                                    },
                                    onSignInWithGoogle = if (isGuest) triggerGoogleSignInFromGuest else null,
                                    onSelectStudioTab = viewModel::selectStudioTab,
                                    onSelectTemplateTagFilter = viewModel::selectTemplateTagFilter,
                                    onApplyTemplate = { template ->
                                        viewModel.applyTemplateToCustomizer(template)
                                        showMessage("Loaded '${template.name}' in Customizer")
                                    },
                                    onCopyTemplate = { template, rendered ->
                                        viewModel.recordTemplateUsed(template, rendered)
                                        copyPlain(rendered, label = "Copied '${template.name}'!")
                                    },
                                    onShareTemplate = { template, rendered ->
                                        viewModel.recordTemplateUsed(template, rendered)
                                        viewModel.openShareSheet(rendered)
                                    },
                                    onDecoratorInputChange = viewModel::updateDecoratorInput,
                                    onInsertSymbol = viewModel::insertSymbolIntoDecorator,
                                    onCustomFinalOverrideChange = viewModel::updateCustomFinalOverride,
                                    onResetCustomFinalOverride = {
                                        viewModel.resetCustomFinalOverride()
                                        showMessage("Reset custom text edits")
                                    },
                                    onSelectDecoratorFont = viewModel::selectDecoratorFont,
                                    onSelectWing = viewModel::selectWing,
                                    onSelectBorder = viewModel::selectBorder,
                                    onSelectSeparator = viewModel::selectSeparator,
                                    onSelectOccasionCategory = viewModel::selectOccasionCategory,
                                    onApplyOccasion = { occasion ->
                                        viewModel.applyOccasionToCustomizer(occasion)
                                        showMessage("Loaded '${occasion.title}' in Customizer")
                                    },
                                    onNameDesignerNameChange = viewModel::updateNameDesignerName,
                                    onNameDesignerSubtitleChange = viewModel::updateNameDesignerSubtitle,
                                    onSelectNameDesignerFont = viewModel::selectNameDesignerFont,
                                    onSelectNameDesignerWing = viewModel::selectNameDesignerWing,
                                    onSelectNameDesignerBadge = viewModel::selectNameDesignerBadge,
                                    onApplyWaMoodPreset = viewModel::applyWaMoodPreset,
                                    onWaHeaderTagChange = viewModel::updateWaHeaderTag,
                                    onWaBodyTextChange = viewModel::updateWaBodyText,
                                    onSelectWaFont = viewModel::selectWaFont,
                                    onSelectWaDivider = viewModel::selectWaDivider,
                                    onToggleWaFooter = viewModel::toggleWaFooter,
                                    onCopyText = { text -> copyPlain(text, label = "Copied styled text!") },
                                    onCacheStyle = { title, input, styled, source, fontId, wingId, borderId, sepId ->
                                        viewModel.cacheStyleGeneration(
                                            title = title,
                                            inputText = input,
                                            styledText = styled,
                                            source = source,
                                            fontId = fontId,
                                            wingId = wingId,
                                            borderId = borderId,
                                            separatorId = sepId
                                        )
                                    },
                                    onSaveCreation = { title, content, kind ->
                                        viewModel.saveCustomCreation(title, content, kind)
                                        showMessage("Saved '$title' to Studio!")
                                    },
                                    onShareText = viewModel::openShareSheet,
                                    onShareWhatsAppDirect = { text ->
                                        val resultMsg = ShareAndClipboardHelper.shareToPlatform(
                                            context = context,
                                            text = text,
                                            platform = SocialPlatform.WHATSAPP
                                        )
                                        showMessage(resultMsg)
                                    },
                                    onDeleteCreation = { id ->
                                        viewModel.deleteSavedCreation(id)
                                        showMessage("Deleted saved item")
                                    },
                                    onCopyFontItem = { item ->
                                        copyPlain(
                                            text = item.previewText,
                                            fontId = item.spec.id,
                                            label = "Copied ${item.spec.name}!"
                                        )
                                    },
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onShareFontItem = { item ->
                                        viewModel.markFontUsed(item.spec.id, item.previewText)
                                        viewModel.openShareSheet(item.previewText)
                                    },
                                    onClearRecents = {
                                        viewModel.clearRecents()
                                        showMessage("Cleared recently used fonts")
                                    },
                                    onRestoreCachedEntry = restoreCachedAndNavigate,
                                    onRemoveCachedStyle = { id ->
                                        viewModel.removeCachedStyle(id)
                                        showMessage("Removed from offline cache")
                                    },
                                    onClearCachedStyles = {
                                        viewModel.clearCachedStyles()
                                        showMessage("Cleared offline cache")
                                    }
                                )
                            }

                            composable(ROUTE_PROFILE) {
                                UserProfileScreen(
                                    displayName = effectiveDisplayName,
                                    email = currentUser?.email,
                                    isGuest = isGuest,
                                    isSigningIn = isSigningIn,
                                    signInErrorMessage = guestSignInError,
                                    isOnline = isOnline,
                                    isCloudSynced = isCloudSynced,
                                    isBackingUp = isBackingUp,
                                    accountDeletionState = accountDeletionState,
                                    favoriteFonts = favoriteFontItems,
                                    savedCreations = savedCreations,
                                    cachedStyles = cachedStyles,
                                    recentFontsCount = recentFontItems.size,
                                    onBackupNow = {
                                        viewModel.backupNow { msg -> showMessage(msg) }
                                    },
                                    onSignInWithGoogle = triggerGoogleSignInFromGuest,
                                    onDismissSignInError = { guestSignInError = null },
                                    onSignOut = {
                                        onBeforeExplicitSignOut()
                                        signOut(credentialManager, scope) {
                                            onExplicitSignOut()
                                        }
                                    },
                                    onOpenPrivacyPolicy = {
                                        navController.navigate(ROUTE_PRIVACY) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onOpenAbout = {
                                        navController.navigate(ROUTE_ABOUT) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onShareApp = {
                                        showMessage(ShareAndClipboardHelper.shareAppLink(context))
                                    },
                                    onClearLocalData = {
                                        viewModel.clearLocalData { msg -> showMessage(msg) }
                                    },
                                    onDeleteCloudAccount = {
                                        onBeforeAccountDelete()
                                        val deletedMsg = context.getString(R.string.account_deleted_confirmation)
                                        viewModel.deleteCloudAccountAndData(
                                            onDeleteAuthUser = { deleteCurrentFirebaseUser() },
                                            onPostDeleteCleanup = { clearCredentialSession(credentialManager) },
                                            onSuccess = { onAccountDeleted(deletedMsg) }
                                        )
                                    },
                                    onConfirmReauthAndDelete = {
                                        onBeforeAccountDelete()
                                        val deletedMsg = context.getString(R.string.account_deleted_confirmation)
                                        scope.launch {
                                            val reauthResult = reauthenticateWithGoogle(context, credentialManager)
                                            if (reauthResult.isSuccess) {
                                                viewModel.deleteCloudAccountAndData(
                                                    onDeleteAuthUser = { deleteCurrentFirebaseUser() },
                                                    onPostDeleteCleanup = { clearCredentialSession(credentialManager) },
                                                    onSuccess = { onAccountDeleted(deletedMsg) }
                                                )
                                            } else {
                                                showMessage(
                                                    reauthResult.exceptionOrNull()?.localizedMessage
                                                        ?: "Google verification failed. Please try again."
                                                )
                                            }
                                        }
                                    },
                                    onDismissDeletionFeedback = viewModel::resetAccountDeletionState,
                                    onCopyFontItem = { item ->
                                        copyPlain(
                                            text = item.previewText,
                                            fontId = item.spec.id,
                                            label = "Copied ${item.spec.name}!"
                                        )
                                    },
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onShareFontItem = { item ->
                                        viewModel.markFontUsed(item.spec.id, item.previewText)
                                        viewModel.openShareSheet(item.previewText)
                                    },
                                    onCopyProject = { text -> copyPlain(text, label = "Copied project text!") },
                                    onShareProject = viewModel::openShareSheet,
                                    onOpenProjectInStudio = { project ->
                                        viewModel.updateDecoratorInput(project.content)
                                        viewModel.selectStudioTab(1)
                                        navigateTo(ROUTE_STUDIO)
                                        showMessage("Opened '${project.title}' in Studio")
                                    },
                                    onDeleteProject = { id ->
                                        viewModel.deleteSavedCreation(id)
                                        showMessage("Deleted saved project")
                                    },
                                    onRestoreCachedEntry = restoreCachedAndNavigate,
                                    onRemoveCachedStyle = { id ->
                                        viewModel.removeCachedStyle(id)
                                        showMessage("Removed from offline cache")
                                    },
                                    onClearCachedStyles = {
                                        viewModel.clearCachedStyles()
                                        showMessage("Cleared offline cache")
                                    },
                                    onNavigateToFonts = { navigateTo(ROUTE_FONTS) },
                                    onNavigateToStudio = { navigateTo(ROUTE_STUDIO) }
                                )
                            }

                            composable(ROUTE_PRIVACY) {
                                PrivacyPolicyScreen(
                                    onBack = { navController.popBackStack() },
                                    onShowMessage = { msg -> showMessage(msg) }
                                )
                            }

                            composable(ROUTE_ABOUT) {
                                AboutScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenPrivacyPolicy = {
                                        navController.navigate(ROUTE_PRIVACY) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onShowMessage = { msg -> showMessage(msg) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val payload = shareSheetPayload
    if (payload != null) {
        SocialShareBottomSheet(
            text = payload,
            onDismiss = viewModel::dismissShareSheet,
            onCopy = {
                copyPlain(payload, label = "Copied styled text!")
                viewModel.dismissShareSheet()
            },
            onSharePlatform = { platform ->
                val resultMsg = ShareAndClipboardHelper.shareToPlatform(context, payload, platform)
                viewModel.dismissShareSheet()
                showMessage(resultMsg)
            }
        )
    }

    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = {
                guestSignInError = null
                showAccountDialog = false
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    if (currentUser == null) "VEDOGRAPHi Guest Mode" else "VEDOGRAPHi Cloud Sync",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    if (currentUser == null) {
                        Text(
                            text = stringResource(R.string.guest_display_name),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.padding(top = 6.dp))
                        Text(
                            text = "Offline DataStore active (${favoriteIds.size} favorites, ${savedCreations.size} projects, ${cachedStyles.size} cached styles). Sign in with Google anytime to back up to the cloud.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!guestSignInError.isNullOrBlank()) {
                            Spacer(modifier = Modifier.padding(top = 10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = guestSignInError.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = effectiveDisplayName?.takeIf { it.isNotBlank() } ?: "Google Account",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!currentUser.email.isNullOrBlank()) {
                            Text(
                                text = currentUser.email.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.padding(top = 8.dp))
                        Text(
                            text = when {
                                !isOnline -> "Offline Mode • DataStore cache active (${favoriteIds.size} favorites, ${savedCreations.size} projects, ${cachedStyles.size} cached styles/templates)"
                                isCloudSynced -> "Synced with Firebase Firestore & DataStore (${favoriteIds.size} favorites, ${savedCreations.size} projects, ${cachedStyles.size} cached styles)"
                                else -> "Syncing preferences & studio creations..."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                if (currentUser == null) {
                    Button(
                        onClick = triggerGoogleSignInFromGuest,
                        enabled = !isSigningIn,
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.primaryButtonColors()
                    ) {
                        Text(
                            if (isSigningIn) "Signing in..." else stringResource(R.string.sign_in_with_google),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            showAccountDialog = false
                            onBeforeExplicitSignOut()
                            signOut(credentialManager, scope) {
                                onExplicitSignOut()
                            }
                        },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.primaryButtonColors()
                    ) {
                        Text("Sign out", style = MaterialTheme.typography.labelLarge)
                    }
                }
            },
            dismissButton = {
                Row {
                    if (currentUser == null) {
                        TextButton(
                            onClick = {
                                showAccountDialog = false
                                navController.navigate(ROUTE_PRIVACY) { launchSingleTop = true }
                            }
                        ) {
                            Text("Privacy Policy", style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        TextButton(
                            enabled = !isBackingUp,
                            onClick = {
                                viewModel.backupNow { msg -> showMessage(msg) }
                                showAccountDialog = false
                            }
                        ) {
                            Text(if (isBackingUp) "Backing up..." else "Backup Now", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    TextButton(onClick = { showAccountDialog = false }) {
                        Text("Close", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        )
    }
}
