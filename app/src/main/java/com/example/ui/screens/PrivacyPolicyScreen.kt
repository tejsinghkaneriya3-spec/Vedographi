package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.ui.components.VedographiContentCard
import com.example.ui.components.VedographiDefaults
import com.example.ui.components.VedographiHeroCard
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.KalamFontFamily
import com.example.ui.theme.NeonOrchid
import com.example.ui.theme.RoyalViolet
import com.example.util.ShareAndClipboardHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val contactEmail = stringResource(R.string.support_contact_email)

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        RoyalViolet.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
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
                            text = stringResource(R.string.privacy_policy_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("privacy_back_btn")
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Hero Summary Banner
                VedographiHeroCard(contentPadding = PaddingValues(18.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PrivacyTip,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "VEDOGRAPHi Privacy Commitment",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "आपका डेटा, आपका नियंत्रण • Effective Nov 2026",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = KalamFontFamily),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = "VEDOGRAPHi (${BuildConfig.APPLICATION_ID}) respects your privacy. You can create Unicode fonts, Hindi calligraphy cards, bios, usernames, and WhatsApp statuses without creating an account. Cloud features are 100% optional.",
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                PolicySectionCard(
                    icon = Icons.Filled.Lock,
                    accent = EmeraldSuccess,
                    title = "1. Guest Mode & Local On-Device Processing",
                    body = "All font conversions, Hindi/Devanagari calligraphy rendering, bio templates, and custom decorations run entirely on your device. In Guest Mode, VEDOGRAPHi collects zero personal identifiers and sends zero text content to external servers."
                )

                PolicySectionCard(
                    icon = Icons.Filled.Storage,
                    accent = MaterialTheme.colorScheme.primary,
                    title = "2. Local Storage & Offline DataStore Cache",
                    body = "Your favorites, copy history (recent styles), drafts, theme preference, and generated offline cache are saved locally in Android Jetpack DataStore so you can access your work without an internet connection."
                )

                PolicySectionCard(
                    icon = Icons.Filled.CloudSync,
                    accent = NeonOrchid,
                    title = "3. Optional Google Sign-In & Cloud Firestore",
                    body = "If you choose to sign in with Google, VEDOGRAPHi uses Firebase Authentication (receiving your Firebase UID, display name, and email address) and Google Cloud Firestore solely to back up and sync your favorites, theme choice, and saved Studio projects across your devices. We never sell, rent, or share your personal data with third-party advertisers or data brokers."
                )

                PolicySectionCard(
                    icon = Icons.Filled.Security,
                    accent = RoyalViolet,
                    title = "4. Device Permissions & Sharing",
                    body = "VEDOGRAPHi requests only INTERNET and ACCESS_NETWORK_STATE permissions to detect offline/online status and perform optional Cloud Sync. We do not request access to your Camera, Microphone, Contacts, Location, SMS, or external Storage. When sharing calligraphy cards, a temporary image is shared securely via Android FileProvider."
                )

                PolicySectionCard(
                    icon = Icons.Filled.DeleteForever,
                    accent = MaterialTheme.colorScheme.error,
                    title = "5. Data Retention & Permanent Deletion",
                    body = "You have immediate, in-app control over your data:\n" +
                        "• Clear Local Data: Wipe cached styles, copy history, drafts, and local favorites anytime via Profile → Clear Local Cache & Data.\n" +
                        "• Delete Cloud Account: Signed-in users can permanently delete their Cloud Firestore profile, all saved creations, and Firebase Authentication account via Profile → Delete Account & Cloud Data."
                )

                PolicySectionCard(
                    icon = Icons.Filled.ChildCare,
                    accent = EmeraldSuccess,
                    title = "6. Children's Privacy",
                    body = "VEDOGRAPHi is a general-audience creative typography app and does not knowingly collect personal information from children under 13 (or the minimum legal age in your region). If you believe a child provided personal info via Google Sign-In, contact us to have it promptly removed."
                )

                PolicySectionCard(
                    icon = Icons.Filled.Update,
                    accent = ElectricViolet,
                    title = "7. Changes to This Privacy Policy",
                    body = "We may update this Privacy Policy as new features are introduced. Material changes will be reflected in-app with an updated effective date."
                )

                // Developer Contact Card
                VedographiContentCard(contentPadding = PaddingValues(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "8. Developer Contact",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Questions, privacy requests, or data deletion inquiries:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SelectionContainer {
                            Text(
                                text = contactEmail,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:$contactEmail")
                                        putExtra(Intent.EXTRA_SUBJECT, "VEDOGRAPHi Privacy Inquiry")
                                    }
                                    try {
                                        context.startActivity(emailIntent)
                                    } catch (_: Exception) {
                                        ShareAndClipboardHelper.copyToClipboard(context, contactEmail, "Support Email")
                                        onShowMessage("Email copied: $contactEmail")
                                    }
                                },
                                shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                                colors = VedographiDefaults.primaryButtonColors(),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(VedographiDefaults.ButtonHeight)
                                    .testTag("privacy_email_btn")
                            ) {
                                Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Email Support", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                            }
                            OutlinedButton(
                                onClick = {
                                    if (ShareAndClipboardHelper.copyToClipboard(context, contactEmail, "Support Email")) {
                                        onShowMessage("Copied $contactEmail")
                                    }
                                },
                                shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                                colors = VedographiDefaults.outlinedButtonColors(),
                                border = VedographiDefaults.outlinedButtonBorder(),
                                modifier = Modifier
                                    .height(VedographiDefaults.ButtonHeight)
                                    .testTag("privacy_copy_email_btn")
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy email", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PolicySectionCard(
    icon: ImageVector,
    accent: Color,
    title: String,
    body: String
) {
    VedographiContentCard(contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.14f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
