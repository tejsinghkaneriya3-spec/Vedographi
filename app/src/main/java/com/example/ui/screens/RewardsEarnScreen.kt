package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.AppMonetizationConfig
import com.example.domain.FontStyleSpec
import com.example.ui.components.VedographiContentCard
import com.example.ui.components.VedographiDefaults
import com.example.ui.components.VedographiHeroCard
import com.example.ui.components.VedographiPillBadge
import com.example.ui.components.VedographiSectionHeader
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonOrchid
import com.example.ui.theme.RoyalViolet
import com.example.util.AdMobBannerBar

@Composable
fun RewardsEarnScreen(
    pointsBalance: Int,
    adsWatchedCount: Int,
    referralCount: Int,
    myReferralCode: String?,
    redeemedReferralCode: String?,
    unlockedProIds: Set<String>,
    proFonts: List<FontStyleSpec>,
    sampleInput: String,
    monetizationConfig: AppMonetizationConfig,
    isRewardedAdReady: Boolean,
    isRewardedAdLoading: Boolean,
    adStatusText: String?,
    onBack: () -> Unit,
    onWatchRewardedAd: () -> Unit,
    onReloadRewardedAd: () -> Unit,
    onCopyReferralCode: (String) -> Unit,
    onShareReferralInvite: (String) -> Unit,
    onRedeemReferralCode: (String, (Result<Int>) -> Unit) -> Unit,
    onUnlockProFont: (FontStyleSpec) -> Unit,
    onCopyProFontSample: (FontStyleSpec, String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var friendCodeInput by remember { mutableStateOf("") }
    var referralFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isRedeeming by remember { mutableStateOf(false) }

    val unlockedCount = remember(proFonts, unlockedProIds) {
        proFonts.count { it.id in unlockedProIds }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("rewards_earn_screen")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("rewards_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.rewards_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Points Wallet Hero
            item(key = "wallet_hero") {
                VedographiHeroCard(contentPadding = PaddingValues(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Your Points Wallet",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Diamond,
                                    contentDescription = null,
                                    tint = NeonOrchid,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$pointsBalance pts",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("wallet_balance_text")
                                )
                            }
                        }
                        VedographiPillBadge(
                            text = "Unlimited Earn",
                            containerColor = EmeraldSuccess.copy(alpha = 0.16f),
                            contentColor = EmeraldSuccess,
                            borderColor = EmeraldSuccess.copy(alpha = 0.45f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WalletStatBox("Ads Watched", "$adsWatchedCount", Modifier.weight(1f))
                        WalletStatBox("Referrals", "$referralCount", Modifier.weight(1f))
                        WalletStatBox("Pro Unlocked", "$unlockedCount/${proFonts.size}", Modifier.weight(1f))
                    }
                }
            }

            // Watch Ads Earn Section (Unlimited)
            item(key = "watch_ad_card") {
                VedographiContentCard(contentPadding = PaddingValues(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.PlayCircle,
                                contentDescription = null,
                                tint = NeonOrchid,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Watch Video Ad & Earn",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        VedographiPillBadge(text = "+${monetizationConfig.adRewardPoints} pts / ad")
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Jitni baar chahein Ad dekh kar points kamayein! Every completed rewarded video gives +${monetizationConfig.adRewardPoints} points for Pro Fonts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!adStatusText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = adStatusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isRewardedAdReady) EmeraldSuccess else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onWatchRewardedAd,
                            enabled = monetizationConfig.adsEnabled,
                            shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                            colors = VedographiDefaults.primaryButtonColors(
                                containerColor = RoyalViolet,
                                contentColor = CrispWhite
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(VedographiDefaults.ButtonHeight)
                                .testTag("watch_ad_earn_btn")
                        ) {
                            if (isRewardedAdLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = CrispWhite,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Loading Ad...", style = MaterialTheme.typography.labelLarge)
                            } else {
                                Icon(Icons.Filled.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isRewardedAdReady) {
                                        "Watch Ad (+${monetizationConfig.adRewardPoints} pts)"
                                    } else {
                                        "Load & Watch Ad"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        if (!isRewardedAdReady && !isRewardedAdLoading) {
                            OutlinedButton(
                                onClick = onReloadRewardedAd,
                                shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                                colors = VedographiDefaults.outlinedButtonColors(),
                                border = VedographiDefaults.outlinedButtonBorder(),
                                modifier = Modifier.height(VedographiDefaults.ButtonHeight)
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Reload ad", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // AdMob Banner Placement
            if (monetizationConfig.adsEnabled) {
                item(key = "rewards_ad_banner") {
                    AdMobBannerBar(
                        adUnitId = monetizationConfig.bannerAdUnitId,
                        adsEnabled = monetizationConfig.adsEnabled
                    )
                }
            }

            // Refer & Earn (+2 Points)
            item(key = "refer_earn_card") {
                VedographiContentCard(contentPadding = PaddingValues(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CardGiftcard,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Refer & Earn",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        VedographiPillBadge(
                            text = "+${monetizationConfig.referralBonusPoints} pts each",
                            containerColor = EmeraldSuccess.copy(alpha = 0.16f),
                            contentColor = EmeraldSuccess,
                            borderColor = EmeraldSuccess.copy(alpha = 0.45f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Apna invite code friends ko share karein! Jab dost code claim karega to dono ko +${monetizationConfig.referralBonusPoints} points milenge.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val displayCode = myReferralCode ?: "GENERATING"
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Your Referral Code",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = displayCode,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 2.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.testTag("my_referral_code_text")
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = { onCopyReferralCode(displayCode) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy referral code")
                                }
                                IconButton(
                                    onClick = { onShareReferralInvite(displayCode) },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("share_referral_btn")
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = "Share referral invite")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (redeemedReferralCode != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldSuccess.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Referral bonus claimed using code $redeemedReferralCode!",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EmeraldSuccess
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = friendCodeInput,
                                onValueChange = { friendCodeInput = it.uppercase().take(12) },
                                label = { Text("Enter Friend's Code") },
                                singleLine = true,
                                shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                                colors = VedographiDefaults.textFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("friend_referral_input")
                            )
                            Button(
                                onClick = {
                                    isRedeeming = true
                                    onRedeemReferralCode(friendCodeInput) { res ->
                                        isRedeeming = false
                                        res.fold(
                                            onSuccess = { pts ->
                                                referralFeedback = true to "Awesome! +$pts points added to your wallet!"
                                                friendCodeInput = ""
                                            },
                                            onFailure = { err ->
                                                referralFeedback = false to (err.localizedMessage ?: "Invalid referral code")
                                            }
                                        )
                                    }
                                },
                                enabled = !isRedeeming && friendCodeInput.isNotBlank(),
                                shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                                colors = VedographiDefaults.primaryButtonColors(),
                                modifier = Modifier
                                    .height(VedographiDefaults.ButtonHeight)
                                    .testTag("redeem_referral_btn")
                            ) {
                                Text("Claim +${monetizationConfig.referralBonusPoints}", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        referralFeedback?.let { (ok, msg) ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (ok) EmeraldSuccess else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Pro Fonts Unlock Gallery
            item(key = "pro_gallery_header") {
                VedographiSectionHeader(
                    title = "Pro Fonts (${proFonts.size} Premium Styles)",
                    subtitle = "Unlock permanently with points • Tap to unlock or copy",
                    icon = Icons.Filled.Diamond,
                    iconTint = NeonOrchid
                )
            }

            items(proFonts, key = { "pro_item_${it.id}" }) { spec ->
                val unlocked = spec.id in unlockedProIds
                val preview = spec.transform(sampleInput.ifBlank { "VEDOGRAPHi" })
                VedographiContentCard(contentPadding = PaddingValues(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = spec.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                VedographiPillBadge(
                                    text = if (spec.isWaFbSafe) "WA/FB ✓" else "IG/Bio",
                                    containerColor = if (spec.isWaFbSafe) EmeraldSuccess.copy(alpha = 0.14f) else RoyalViolet.copy(alpha = 0.14f),
                                    contentColor = if (spec.isWaFbSafe) EmeraldSuccess else MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = preview,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        if (unlocked) {
                            OutlinedButton(
                                onClick = { onCopyProFontSample(spec, preview) },
                                shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                                colors = VedographiDefaults.outlinedButtonColors(),
                                border = VedographiDefaults.outlinedButtonBorder(),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", style = MaterialTheme.typography.labelLarge)
                            }
                        } else {
                            Button(
                                onClick = { onUnlockProFont(spec) },
                                shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                                colors = VedographiDefaults.primaryButtonColors(
                                    containerColor = NeonOrchid.copy(alpha = 0.9f),
                                    contentColor = CrispWhite
                                ),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("rewards_unlock_${spec.id}")
                            ) {
                                Icon(Icons.Filled.LockOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${spec.proCost} pts", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun WalletStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
