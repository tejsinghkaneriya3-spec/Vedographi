package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Translate
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.ui.components.VedographiContentCard
import com.example.ui.components.VedographiDefaults
import com.example.ui.components.VedographiHeroCard
import com.example.ui.components.VedographiPillBadge
import com.example.ui.theme.AmitaFontFamily
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.KalamFontFamily
import com.example.ui.theme.NeonOrchid
import com.example.ui.theme.ObsidianIconPlate
import com.example.ui.theme.RoyalViolet
import com.example.util.ShareAndClipboardHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
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
                        RoyalViolet.copy(alpha = 0.12f),
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
                            text = stringResource(R.string.about_app_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("about_back_btn")
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
                // Brand Identity Hero Card
                VedographiHeroCard(contentPadding = PaddingValues(22.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = ObsidianIconPlate,
                            border = BorderStroke(1.5.dp, ElectricViolet.copy(alpha = 0.7f)),
                            shadowElevation = 10.dp,
                            modifier = Modifier.size(88.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.vedographi_icon_1795394652656),
                                contentDescription = stringResource(R.string.app_name),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.6.sp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.onSurface,
                                        ElectricViolet,
                                        NeonOrchid
                                    )
                                )
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(R.string.brand_tagline),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = AmitaFontFamily,
                                fontSize = 21.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        VedographiPillBadge(
                            text = "Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})"
                        )
                    }
                }

                // Brand Mission Card
                VedographiContentCard(contentPadding = PaddingValues(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = NeonOrchid,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Our Brand Mission",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "VEDOGRAPHi empowers creators, storytellers, and everyday users across India and beyond to express their identity through expressive typography. Whether you are styling an Instagram bio, designing a royal Hindi calligraphy quote card, or sending a festive WhatsApp wish, every word deserves its own signature flair.",
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "हिंदी और इंग्लिश का अनूठा संगम — बिना किसी झंझट के स्टाइलिश शब्द और कैलीग्राफी कार्ड बनाएं।",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = KalamFontFamily,
                                lineHeight = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Key Highlights
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AboutFeatureTile(
                        icon = Icons.Filled.Brush,
                        accent = NeonOrchid,
                        title = "6 Authentic Hindi Calligraphy Fonts",
                        subtitle = "Preview & export gradient PNG cards in Kalam, Amita, Rozha One, Yatra One, Tillana, and Gotu."
                    )
                    AboutFeatureTile(
                        icon = Icons.Filled.TextFields,
                        accent = MaterialTheme.colorScheme.primary,
                        title = "225+ Unicode Styles & 50+ Templates",
                        subtitle = "Instant copy & paste styling with zero Devanagari corruption across English & Hindi."
                    )
                    AboutFeatureTile(
                        icon = Icons.Filled.Translate,
                        accent = EmeraldSuccess,
                        title = "Guest-First & Offline Ready",
                        subtitle = "Works offline out-of-the-box with Jetpack DataStore cache & copy history, plus optional Google Cloud Sync."
                    )
                }

                // Share VEDOGRAPHi App Card
                VedographiContentCard(contentPadding = PaddingValues(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Love VEDOGRAPHi?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Share the app link with friends and creators directly via WhatsApp, Telegram, or any messaging app.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                val msg = ShareAndClipboardHelper.shareAppLink(context)
                                onShowMessage(msg)
                            },
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            colors = VedographiDefaults.primaryButtonColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(VedographiDefaults.ButtonHeight)
                                .testTag("about_share_app_btn")
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.share_app_title), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                // Developer Contact & Technical Info
                VedographiContentCard(contentPadding = PaddingValues(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CloudDone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Developer & Support Info",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Package ID: ${BuildConfig.APPLICATION_ID}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SelectionContainer {
                            Text(
                                text = "Developer Contact: $contactEmail",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:$contactEmail")
                                        putExtra(Intent.EXTRA_SUBJECT, "VEDOGRAPHi Feedback")
                                    }
                                    try {
                                        context.startActivity(emailIntent)
                                    } catch (_: Exception) {
                                        ShareAndClipboardHelper.copyToClipboard(context, contactEmail, "Support Email")
                                        onShowMessage("Email copied: $contactEmail")
                                    }
                                },
                                shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                                colors = VedographiDefaults.outlinedButtonColors(),
                                border = VedographiDefaults.outlinedButtonBorder(),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(VedographiDefaults.ButtonHeight)
                                    .testTag("about_email_btn")
                            ) {
                                Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Contact Dev", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                            }
                            OutlinedButton(
                                onClick = {
                                    if (ShareAndClipboardHelper.copyToClipboard(context, contactEmail, "Developer Email")) {
                                        onShowMessage("Copied $contactEmail")
                                    }
                                },
                                shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                                colors = VedographiDefaults.outlinedButtonColors(),
                                border = VedographiDefaults.outlinedButtonBorder(),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier
                                    .height(VedographiDefaults.ButtonHeight)
                                    .testTag("about_copy_email_btn")
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy developer email", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                            }
                        }

                        OutlinedButton(
                            onClick = onOpenPrivacyPolicy,
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            colors = VedographiDefaults.outlinedButtonColors(),
                            border = VedographiDefaults.outlinedButtonBorder(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(VedographiDefaults.ButtonHeight)
                                .testTag("about_privacy_policy_btn")
                        ) {
                            Icon(Icons.Filled.PrivacyTip, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.privacy_policy_title), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AboutFeatureTile(
    icon: ImageVector,
    accent: Color,
    title: String,
    subtitle: String
) {
    VedographiContentCard(contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
