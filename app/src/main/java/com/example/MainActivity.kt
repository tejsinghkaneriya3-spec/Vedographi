package com.example

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.example.auth.authStateFlowSafe
import com.example.auth.clearCredentialSession
import com.example.auth.deleteCurrentFirebaseUser
import com.example.auth.onGoogleSignInClicked
import com.example.auth.reauthenticateWithGoogle
import com.example.auth.safeCurrentFirebaseUser
import com.example.auth.signOut
import com.example.data.CloudSyncRepository
import com.example.data.PreferencesRepository
import com.example.domain.CachedEntrySource
import com.example.domain.CachedStyleEntry
import com.example.domain.FontCatalog
import com.example.domain.FontCategory
import com.example.domain.SocialPlatform
import com.example.ui.BatchCopyVariation
import com.example.ui.FontPreviewItem
import com.example.ui.UnlockProResult
import com.example.ui.VedographiViewModel
import com.example.ui.components.BatchCopyAllBottomSheet
import com.example.ui.components.SocialShareBottomSheet
import com.example.ui.components.VedographiDefaults
import com.example.ui.components.VedographiSplashScreen
import com.example.ui.components.VedographiTopHeader
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BioCaptionScreen
import com.example.ui.screens.FontsHomeScreen
import com.example.ui.screens.HowToUseScreen
import com.example.ui.screens.MyTextStudioScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.RewardsEarnScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.screens.UsernameScreen
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.FloatingNavDark
import com.example.ui.theme.FloatingNavLight
import com.example.ui.theme.LavenderWash
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonOrchid
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.VedographiTheme
import com.example.util.AdMobManager
import com.example.util.NetworkMonitor
import com.example.util.ShareAndClipboardHelper
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

const val ROUTE_FONTS = "fonts_home"
const val ROUTE_BIOS = "bio_captions"
const val ROUTE_USERNAMES = "usernames"
const val ROUTE_STUDIO = "studio_decorator"
const val ROUTE_PROFILE = "user_profile"
const val ROUTE_PRIVACY = "privacy_policy"
const val ROUTE_ABOUT = "about_app"
const val ROUTE_REWARDS = "rewards_earn"
const val ROUTE_HOW_TO_USE = "how_to_use"
const val ROUTE_ADMIN = "admin_panel"

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

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
        AdMobManager.initialize(applicationContext)
        setContent {
            val localPrefs = remember { PreferencesRepository(applicationContext) }
            val localDarkTheme by localPrefs.isDarkTheme.collectAsStateWithLifecycle(initialValue = true)
            val isGuestModeEnabled by localPrefs.guestModeEnabled.collectAsStateWithLifecycle(initialValue = false)
            val initialFirebaseUser = remember { safeCurrentFirebaseUser(applicationContext) }
            val currentUser by remember { authStateFlowSafe(applicationContext) }
                .collectAsStateWithLifecycle(initialValue = initialFirebaseUser)
            val rootScope = rememberCoroutineScope()

            var authLegalOverlay by rememberSaveable { mutableStateOf(AuthLegalOverlay.NONE) }
            var skipAutoSignIn by rememberSaveable { mutableStateOf(false) }
            var authStatusBanner by rememberSaveable { mutableStateOf<String?>(null) }
            var authSessionGeneration by rememberSaveable { mutableIntStateOf(0) }
            var lastObservedSessionUid by rememberSaveable { mutableStateOf(initialFirebaseUser?.uid) }
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
                                                    userEmail = user.email,
                                                    networkOnlineFlow = NetworkMonitor(app).isOnline
                                                )
                                            } else {
                                                VedographiViewModel(
                                                    repo = PreferencesRepository(app),
                                                    cloudRepo = null,
                                                    currentUserId = null,
                                                    userDisplayName = null,
                                                    userEmail = null,
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
    val isDetailRoute = currentRoute == ROUTE_PRIVACY ||
        currentRoute == ROUTE_ABOUT ||
        currentRoute == ROUTE_REWARDS ||
        currentRoute == ROUTE_HOW_TO_USE ||
        currentRoute == ROUTE_ADMIN
    var showAccountDialog by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }
    var guestSignInError by remember { mutableStateOf<String?>(null) }
    var pendingProUnlockItem by remember { mutableStateOf<FontPreviewItem?>(null) }
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
    val waFbSafeOnly by viewModel.waFbSafeOnly.collectAsStateWithLifecycle()
    val isSearchExpandedAcrossAll by viewModel.isSearchExpandedAcrossAll.collectAsStateWithLifecycle()
    val selectedCalligraphyPreset by viewModel.selectedCalligraphyPreset.collectAsStateWithLifecycle()
    val hasHindiInInput by viewModel.hasHindiInInput.collectAsStateWithLifecycle()
    val trendingItems by viewModel.trendingFontItems.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredFontItems.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteFontIds.collectAsStateWithLifecycle()
    val recentIds by viewModel.recentFontIds.collectAsStateWithLifecycle()
    val allAvailableStyles by viewModel.allAvailableStyles.collectAsStateWithLifecycle()
    val savedCreations by viewModel.savedCreations.collectAsStateWithLifecycle()
    val copiedFeedbackId by viewModel.copiedFeedbackId.collectAsStateWithLifecycle()
    val shareSheetPayload by viewModel.shareSheetPayload.collectAsStateWithLifecycle()
    val batchCopySheet by viewModel.batchCopySheet.collectAsStateWithLifecycle()

    // Wallet, Pro Fonts, Custom Style Studio, Referrals, Guide & Admin Config
    val pointsBalance by viewModel.pointsBalance.collectAsStateWithLifecycle()
    val unlockedProIds by viewModel.unlockedProFontIds.collectAsStateWithLifecycle()
    val customFonts by viewModel.customFonts.collectAsStateWithLifecycle()
    val myReferralCode by viewModel.myReferralCode.collectAsStateWithLifecycle()
    val redeemedReferralCode by viewModel.redeemedReferralCode.collectAsStateWithLifecycle()
    val referralCount by viewModel.referralCount.collectAsStateWithLifecycle()
    val adsWatchedCount by viewModel.adsWatchedCount.collectAsStateWithLifecycle()
    val quickGuideDismissed by viewModel.quickGuideDismissed.collectAsStateWithLifecycle()
    val monetizationConfig by viewModel.monetizationConfig.collectAsStateWithLifecycle()

    // Custom Style Studio Draft
    val customDraftName by viewModel.customDraftName.collectAsStateWithLifecycle()
    val customSamplePreviewInput by viewModel.customSamplePreviewInput.collectAsStateWithLifecycle()
    val customDraftCharMap by viewModel.customDraftCharMap.collectAsStateWithLifecycle()
    val customDraftPrefix by viewModel.customDraftPrefix.collectAsStateWithLifecycle()
    val customDraftSuffix by viewModel.customDraftSuffix.collectAsStateWithLifecycle()
    val customDraftJoiner by viewModel.customDraftJoiner.collectAsStateWithLifecycle()
    val customSelectedChar by viewModel.customSelectedChar.collectAsStateWithLifecycle()
    val customStudioPreviewText by viewModel.customStudioPreviewText.collectAsStateWithLifecycle()

    // AdMob Rewarded state
    val isRewardedAdReady by AdMobManager.isRewardedReady.collectAsStateWithLifecycle()
    val isRewardedAdLoading by AdMobManager.isRewardedLoading.collectAsStateWithLifecycle()
    val adStatusText by AdMobManager.lastAdStatus.collectAsStateWithLifecycle()

    LaunchedEffect(monetizationConfig.adsEnabled, monetizationConfig.rewardedAdUnitId) {
        if (monetizationConfig.adsEnabled) {
            AdMobManager.preloadRewardedAd(context, monetizationConfig.rewardedAdUnitId)
        }
    }

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
    val favoriteFontItems = remember(allAvailableStyles, favoriteIds, unlockedProIds, effectivePreviewSource, copiedFeedbackId) {
        allAvailableStyles
            .filter { it.id in favoriteIds }
            .map { spec ->
                FontPreviewItem(
                    spec = spec,
                    previewText = spec.transform(effectivePreviewSource),
                    isFavorite = true,
                    isRecentlyCopied = copiedFeedbackId == spec.id,
                    isUnlocked = !spec.isPro || spec.id in unlockedProIds
                )
            }
    }
    val recentFontItems = remember(allAvailableStyles, recentIds, favoriteIds, unlockedProIds, effectivePreviewSource, copiedFeedbackId) {
        recentIds.mapNotNull { id -> allAvailableStyles.firstOrNull { it.id == id } }.map { spec ->
            FontPreviewItem(
                spec = spec,
                previewText = spec.transform(effectivePreviewSource),
                isFavorite = spec.id in favoriteIds,
                isRecentlyCopied = copiedFeedbackId == spec.id,
                isUnlocked = !spec.isPro || spec.id in unlockedProIds
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(26.dp),
                            color = if (isDarkTheme) FloatingNavDark else FloatingNavLight,
                            border = BorderStroke(
                                width = 1.25.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        ElectricViolet.copy(alpha = 0.5f),
                                        NeonOrchid.copy(alpha = 0.35f),
                                        NeonCyan.copy(alpha = 0.3f),
                                        ElectricViolet.copy(alpha = 0.5f)
                                    )
                                )
                            ),
                            shadowElevation = 14.dp,
                            tonalElevation = 6.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            NavigationBar(
                                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                tonalElevation = 0.dp,
                                windowInsets = WindowInsets(0, 0, 0, 0),
                                modifier = Modifier.height(68.dp)
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
                                            selectedIconColor = if (isDarkTheme) CrispWhite else MaterialTheme.colorScheme.onPrimaryContainer,
                                            selectedTextColor = if (isDarkTheme) NeonOrchid else RoyalViolet,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                            indicatorColor = if (isDarkTheme) RoyalViolet.copy(alpha = 0.65f) else LavenderWash
                                        ),
                                        modifier = Modifier.testTag("nav_${dest.route}")
                                    )
                                }
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
                        containerColor = if (isDarkTheme) FloatingNavDark else FloatingNavLight,
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
                                    selectedIconColor = if (isDarkTheme) CrispWhite else MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = if (isDarkTheme) NeonOrchid else RoyalViolet,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                    indicatorColor = if (isDarkTheme) RoyalViolet.copy(alpha = 0.65f) else LavenderWash
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
                                pointsBalance = pointsBalance,
                                onPointsClick = {
                                    viewModel.ensureReferralCode()
                                    navController.navigate(ROUTE_REWARDS) { launchSingleTop = true }
                                },
                                onHowToUseClick = {
                                    navController.navigate(ROUTE_HOW_TO_USE) { launchSingleTop = true }
                                },
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
                                    isSearchExpandedAcrossAll = isSearchExpandedAcrossAll,
                                    hasHindiInInput = hasHindiInInput,
                                    trendingItems = trendingItems,
                                    filteredItems = filteredItems,
                                    selectedCalligraphyPreset = selectedCalligraphyPreset,
                                    waFbSafeOnly = waFbSafeOnly,
                                    quickGuideDismissed = quickGuideDismissed,
                                    pointsBalance = pointsBalance,
                                    announcementBanner = monetizationConfig.announcementBanner,
                                    adsEnabled = monetizationConfig.adsEnabled,
                                    bannerAdUnitId = monetizationConfig.bannerAdUnitId,
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
                                        viewModel.openBatchCopySheetForFonts()
                                    },
                                    onSearchChange = viewModel::updateSearchQuery,
                                    onSelectCategory = viewModel::selectCategory,
                                    onToggleWaFbSafe = viewModel::toggleWaFbSafeFilter,
                                    onDismissQuickGuide = viewModel::dismissQuickGuide,
                                    onOpenHowToUse = {
                                        navController.navigate(ROUTE_HOW_TO_USE) { launchSingleTop = true }
                                    },
                                    onOpenCreateCustomFont = {
                                        viewModel.selectStudioTab(6)
                                        navigateTo(ROUTE_STUDIO)
                                    },
                                    onOpenEarnPoints = {
                                        viewModel.ensureReferralCode()
                                        navController.navigate(ROUTE_REWARDS) { launchSingleTop = true }
                                    },
                                    onUnlockProItem = { item ->
                                        pendingProUnlockItem = item
                                    },
                                    onDeleteCustomFont = { id ->
                                        viewModel.deleteCustomFontDefinition(id)
                                        showMessage("Deleted custom font")
                                    },
                                    onCopyItem = { item ->
                                        if (item.isUnlocked) {
                                            copyPlain(
                                                text = item.previewText,
                                                fontId = item.spec.id,
                                                label = "Copied ${item.spec.name}!"
                                            )
                                        } else {
                                            pendingProUnlockItem = item
                                        }
                                    },
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onShareItem = { item ->
                                        if (item.isUnlocked) {
                                            viewModel.markFontUsed(item.spec.id, item.previewText)
                                            viewModel.openShareSheet(item.previewText)
                                        } else {
                                            pendingProUnlockItem = item
                                        }
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
                                    },
                                    onCopyAllUsernames = { handles ->
                                        viewModel.openBatchCopySheet(
                                            title = "Batch Copy Usernames",
                                            sourceText = usernameInput.ifBlank { "Vedo" },
                                            variations = handles.mapIndexed { idx, handle ->
                                                BatchCopyVariation(
                                                    id = "uname_${idx}_${handle.hashCode()}",
                                                    styleName = "${selectedUsernameVibe.label} #${idx + 1}",
                                                    styledText = handle
                                                )
                                            },
                                            includeLabelsDefault = false
                                        )
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
                                    },
                                    onCopyAllTemplates = { pairs ->
                                        viewModel.openBatchCopySheet(
                                            title = "Batch Copy Templates",
                                            sourceText = decoratorInput.ifBlank { "VEDOGRAPHi" },
                                            variations = pairs.map { (tpl, rendered) ->
                                                BatchCopyVariation(
                                                    id = tpl.id,
                                                    styleName = tpl.name,
                                                    styledText = rendered
                                                )
                                            },
                                            includeLabelsDefault = false
                                        )
                                    },
                                    customDraftName = customDraftName,
                                    customSampleInput = customSamplePreviewInput,
                                    customCharMap = customDraftCharMap,
                                    customPrefix = customDraftPrefix,
                                    customSuffix = customDraftSuffix,
                                    customJoiner = customDraftJoiner,
                                    customSelectedChar = customSelectedChar,
                                    customLivePreview = customStudioPreviewText,
                                    savedCustomFonts = customFonts,
                                    onCustomNameChange = viewModel::updateCustomDraftName,
                                    onCustomSampleInputChange = viewModel::updateCustomSamplePreviewInput,
                                    onCustomSelectChar = viewModel::selectCustomEditChar,
                                    onCustomSetCharGlyph = viewModel::setCustomCharGlyph,
                                    onCustomApplyStarter = viewModel::applyCustomStarterAlphabet,
                                    onCustomRandomize = viewModel::randomizeCustomGlyphMix,
                                    onCustomSelectPrefix = viewModel::updateCustomDraftPrefix,
                                    onCustomSelectSuffix = viewModel::updateCustomDraftSuffix,
                                    onCustomSelectJoiner = viewModel::updateCustomDraftJoiner,
                                    onCustomResetDraft = viewModel::resetCustomFontDraft,
                                    onCustomSaveFont = {
                                        viewModel.saveCustomFontDefinition { savedDef ->
                                            viewModel.selectCategory(FontCategory.MY_CUSTOM)
                                            showMessage("Saved '${savedDef.name}'! Check 'My Custom' in Fonts tab.")
                                        }
                                    },
                                    onCustomLoadFont = { def ->
                                        viewModel.loadCustomFontIntoEditor(def)
                                        showMessage("Loaded '${def.name}' for editing")
                                    },
                                    onCustomDeleteFont = { id ->
                                        viewModel.deleteCustomFontDefinition(id)
                                        showMessage("Deleted custom font")
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
                                    onNavigateToStudio = { navigateTo(ROUTE_STUDIO) },
                                    isAdmin = viewModel.isAdmin,
                                    pointsBalance = pointsBalance,
                                    onOpenRewards = {
                                        viewModel.ensureReferralCode()
                                        navController.navigate(ROUTE_REWARDS) { launchSingleTop = true }
                                    },
                                    onOpenHowToUse = {
                                        navController.navigate(ROUTE_HOW_TO_USE) { launchSingleTop = true }
                                    },
                                    onOpenAdminPanel = {
                                        if (viewModel.isAdmin) {
                                            navController.navigate(ROUTE_ADMIN) { launchSingleTop = true }
                                        }
                                    }
                                )
                            }

                            composable(ROUTE_REWARDS) {
                                val proStyles = remember {
                                    FontCatalog.allStyles.filter { it.isPro }
                                }
                                RewardsEarnScreen(
                                    pointsBalance = pointsBalance,
                                    adsWatchedCount = adsWatchedCount,
                                    referralCount = referralCount,
                                    myReferralCode = myReferralCode,
                                    redeemedReferralCode = redeemedReferralCode,
                                    unlockedProIds = unlockedProIds,
                                    proFonts = proStyles,
                                    sampleInput = effectivePreviewSource,
                                    monetizationConfig = monetizationConfig,
                                    isRewardedAdReady = isRewardedAdReady,
                                    isRewardedAdLoading = isRewardedAdLoading,
                                    adStatusText = adStatusText,
                                    onBack = { navController.popBackStack() },
                                    onWatchRewardedAd = {
                                        AdMobManager.showRewardedAd(
                                            activity = context.findActivity(),
                                            adUnitId = monetizationConfig.rewardedAdUnitId,
                                            expectedPoints = monetizationConfig.adRewardPoints,
                                            onRewardEarned = { earned ->
                                                viewModel.grantAdRewardPoints(earned) { newBalance ->
                                                    showMessage("+$earned pts earned! Wallet balance: $newBalance pts")
                                                }
                                            },
                                            onStatusMessage = { msg -> showMessage(msg) }
                                        )
                                    },
                                    onReloadRewardedAd = {
                                        AdMobManager.preloadRewardedAd(context, monetizationConfig.rewardedAdUnitId)
                                    },
                                    onCopyReferralCode = { code ->
                                        copyPlain(code, label = "Referral code $code copied!")
                                    },
                                    onShareReferralInvite = { code ->
                                        val link = context.getString(R.string.app_share_url)
                                        val inviteMsg = "VEDOGRAPHi use karo stylish Hindi & English fonts ke liye! Mera referral code $code lagao aur +${monetizationConfig.referralBonusPoints} bonus points pao: $link"
                                        viewModel.openShareSheet(inviteMsg)
                                    },
                                    onRedeemReferralCode = viewModel::redeemReferralCode,
                                    onUnlockProFont = { spec ->
                                        pendingProUnlockItem = FontPreviewItem(
                                            spec = spec,
                                            previewText = spec.transform(effectivePreviewSource),
                                            isFavorite = spec.id in favoriteIds,
                                            isRecentlyCopied = false,
                                            isUnlocked = false
                                        )
                                    },
                                    onCopyProFontSample = { spec, preview ->
                                        copyPlain(preview, fontId = spec.id, label = "Copied ${spec.name}!")
                                    }
                                )
                            }

                            composable(ROUTE_HOW_TO_USE) {
                                HowToUseScreen(
                                    onBack = { navController.popBackStack() },
                                    onTryFontsClick = {
                                        navController.popBackStack()
                                        navigateTo(ROUTE_FONTS)
                                    },
                                    onOpenEarnPointsClick = {
                                        viewModel.ensureReferralCode()
                                        navController.navigate(ROUTE_REWARDS) { launchSingleTop = true }
                                    }
                                )
                            }

                            composable(ROUTE_ADMIN) {
                                AdminPanelScreen(
                                    isAdmin = viewModel.isAdmin,
                                    adminEmail = currentUser?.email,
                                    currentConfig = monetizationConfig,
                                    currentPointsBalance = pointsBalance,
                                    onBack = { navController.popBackStack() },
                                    onSaveConfig = { updatedConfig ->
                                        viewModel.saveAdminMonetizationConfig(updatedConfig) { res ->
                                            res.fold(
                                                onSuccess = { showMessage("Admin config saved to Firestore!") },
                                                onFailure = { err -> showMessage(err.localizedMessage ?: "Failed to save config") }
                                            )
                                        }
                                    },
                                    onGrantSelfPoints = { pts ->
                                        viewModel.adminGrantSelfPoints(pts) { newBal ->
                                            showMessage("+$pts test points credited! Balance: $newBal pts")
                                        }
                                    }
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

    val batchState = batchCopySheet
    if (batchState != null) {
        BatchCopyAllBottomSheet(
            state = batchState,
            onDismiss = viewModel::dismissBatchCopySheet,
            onCopyBatch = { formattedText, selectedCount ->
                copyPlain(
                    text = formattedText,
                    label = "Copied $selectedCount variation(s) to clipboard!"
                )
                viewModel.dismissBatchCopySheet()
            },
            onShareBatch = { formattedText, _ ->
                viewModel.dismissBatchCopySheet()
                viewModel.openShareSheet(formattedText)
            }
        )
    }

    val proDialogItem = pendingProUnlockItem
    if (proDialogItem != null) {
        val spec = proDialogItem.spec
        val hasEnoughPoints = pointsBalance >= spec.proCost
        AlertDialog(
            onDismissRequest = { pendingProUnlockItem = null },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = if (hasEnoughPoints) "Unlock ${spec.name}?" else "Need More Points",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    Text(
                        text = proDialogItem.previewText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.padding(top = 8.dp))
                    Text(
                        text = if (hasEnoughPoints) {
                            "Unlock '${spec.name}' permanently for ${spec.proCost} points? Your balance: $pointsBalance pts."
                        } else {
                            "Cost: ${spec.proCost} pts  •  Your Balance: $pointsBalance pts.\n\nWatch a quick Rewarded Ad (+${monetizationConfig.adRewardPoints} pts) or Refer a friend (+${monetizationConfig.referralBonusPoints} pts) to unlock!"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                if (hasEnoughPoints) {
                    Button(
                        onClick = {
                            val targetSpec = spec
                            val previewText = proDialogItem.previewText
                            pendingProUnlockItem = null
                            viewModel.unlockProFont(targetSpec) { res ->
                                when (res) {
                                    is UnlockProResult.Unlocked -> {
                                        copyPlain(
                                            text = previewText,
                                            fontId = targetSpec.id,
                                            label = "Unlocked '${targetSpec.name}' & copied! Balance: ${res.newBalance} pts"
                                        )
                                    }
                                    is UnlockProResult.AlreadyUnlocked -> {
                                        copyPlain(previewText, fontId = targetSpec.id)
                                    }
                                    is UnlockProResult.InsufficientPoints -> {
                                        showMessage("Need ${res.requiredCost} pts (you have ${res.currentBalance} pts)")
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.primaryButtonColors(),
                        modifier = Modifier.testTag("confirm_unlock_pro_btn")
                    ) {
                        Text("Unlock (${spec.proCost} pts)", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    Button(
                        onClick = {
                            pendingProUnlockItem = null
                            viewModel.ensureReferralCode()
                            navController.navigate(ROUTE_REWARDS) { launchSingleTop = true }
                        },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.primaryButtonColors(),
                        modifier = Modifier.testTag("dialog_go_earn_pts_btn")
                    ) {
                        Text("Earn Points Now", style = MaterialTheme.typography.labelLarge)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingProUnlockItem = null }) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
                }
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
