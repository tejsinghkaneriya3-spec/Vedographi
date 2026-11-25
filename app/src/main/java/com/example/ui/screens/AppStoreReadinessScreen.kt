package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.VedographiContentCard
import com.example.ui.components.VedographiDefaults
import com.example.ui.components.VedographiHeroCard
import com.example.ui.components.VedographiPillBadge
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonOrchid
import com.example.ui.theme.RoyalViolet
import com.example.util.ShareAndClipboardHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppStoreReadinessScreen(
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val packageName = "com.aistudio.vedographi.xkwmzv"

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        RoyalViolet.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .testTag("app_store_readiness_screen")
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Play Store & App Icon Hub",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("store_hub_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { innerPadding ->
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Hero App Icon Spotlight Card
                VedographiHeroCard(contentPadding = PaddingValues(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(26.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(
                                2.dp,
                                Brush.linearGradient(listOf(ElectricViolet, NeonOrchid, EmeraldSuccess))
                            ),
                            shadowElevation = 12.dp,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.vedographi_icon_1795394652656),
                                    contentDescription = "App Launcher Icon",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(24.dp))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = "Verified Ready",
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Adaptive Launcher Icon & Store Assets",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            VedographiPillBadge(
                                text = "Play Store Public Ready ✓",
                                containerColor = EmeraldSuccess.copy(alpha = 0.16f),
                                contentColor = EmeraldSuccess,
                                borderColor = EmeraldSuccess.copy(alpha = 0.45f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                ShareAndClipboardHelper.copyToClipboard(context, packageName, "Package Name")
                                onShowMessage("Copied package name: $packageName")
                            },
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            colors = VedographiDefaults.primaryButtonColors(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(VedographiDefaults.ButtonHeight)
                                .testTag("copy_package_btn")
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Package ID", style = MaterialTheme.typography.labelLarge, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                val msg = ShareAndClipboardHelper.shareAppLink(context)
                                onShowMessage(msg)
                            },
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            colors = VedographiDefaults.outlinedButtonColors(),
                            border = VedographiDefaults.outlinedButtonBorder(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(VedographiDefaults.ButtonHeight)
                                .testTag("share_store_details_btn")
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Hub", style = MaterialTheme.typography.labelLarge, maxLines = 1)
                        }
                    }
                }

                // Section Header: Play Store Readiness Checklist
                Text(
                    text = "Google Play Store Publishing Checklist",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                // Checklist Cards
                StoreChecklistItem(
                    title = "Adaptive App Icon (mipmap-anydpi)",
                    description = "Configured with custom foreground layer and Obsidian-Night background layer conforming to Material You launcher specs.",
                    isReady = true
                )

                StoreChecklistItem(
                    title = "Application ID & Namespace",
                    description = "Unique applicationId ($packageName) is set securely for Play Store AAB/APK distribution.",
                    isReady = true
                )

                StoreChecklistItem(
                    title = "Google Play Policy Compliance",
                    description = "Zero broad storage permissions required; utilizes Android Photo Picker and standard INTERNET connectivity.",
                    isReady = true
                )

                StoreChecklistItem(
                    title = "Firebase Cloud Firestore & Authentication",
                    description = "Robust cloud backup, Google Sign-In via Credential Manager, and real-time security rules provisioned.",
                    isReady = true
                )

                StoreChecklistItem(
                    title = "Privacy Policy & Support Email",
                    description = "In-app Privacy Policy screen and active developer contact email configured for review compliance.",
                    isReady = true
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StoreChecklistItem(
    title: String,
    description: String,
    isReady: Boolean
) {
    VedographiContentCard(
        borderColor = if (isReady) EmeraldSuccess.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = CircleShape,
                color = if (isReady) EmeraldSuccess.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isReady) Icons.Filled.CheckCircle else Icons.Filled.NewReleases,
                        contentDescription = null,
                        tint = if (isReady) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
