package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.AdminConfig
import com.example.domain.AppMonetizationConfig
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.VedographiContentCard
import com.example.ui.components.VedographiDefaults
import com.example.ui.components.VedographiHeroCard
import com.example.ui.components.VedographiPillBadge
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoyalViolet

@Composable
fun AdminPanelScreen(
    isAdmin: Boolean,
    adminEmail: String?,
    currentConfig: AppMonetizationConfig,
    currentPointsBalance: Int,
    onBack: () -> Unit,
    onSaveConfig: (AppMonetizationConfig) -> Unit,
    onGrantSelfPoints: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("admin_panel_screen")
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
                    .testTag("admin_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.admin_panel_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (!isAdmin) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                EmptyStateCard(
                    title = "Admin Access Restricted",
                    subtitle = "This section is restricted to verified app administrators only.",
                    icon = Icons.Filled.Lock,
                    actionLabel = "Go Back",
                    onActionClick = onBack
                )
            }
            return
        }

        var adsEnabled by remember(currentConfig) { mutableStateOf(currentConfig.adsEnabled) }
        var bannerUnitId by remember(currentConfig) { mutableStateOf(currentConfig.bannerAdUnitId) }
        var rewardedUnitId by remember(currentConfig) { mutableStateOf(currentConfig.rewardedAdUnitId) }
        var adRewardPointsText by remember(currentConfig) { mutableStateOf(currentConfig.adRewardPoints.toString()) }
        var referralBonusText by remember(currentConfig) { mutableStateOf(currentConfig.referralBonusPoints.toString()) }
        var announcement by remember(currentConfig) { mutableStateOf(currentConfig.announcementBanner) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "admin_identity") {
                VedographiHeroCard(contentPadding = PaddingValues(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Verified Administrator",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    VedographiPillBadge(
                        text = adminEmail ?: AdminConfig.ADMIN_EMAIL,
                        containerColor = EmeraldSuccess.copy(alpha = 0.16f),
                        contentColor = EmeraldSuccess,
                        borderColor = EmeraldSuccess.copy(alpha = 0.45f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Changes saved here update live Firestore /app_config/monetization for all app users.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item(key = "admin_admob_config") {
                VedographiContentCard(contentPadding = PaddingValues(16.dp)) {
                    Text(
                        text = "AdMob & Rewards Controls",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable AdMob Ads",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Toggle Banner & Rewarded video ads globally",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = adsEnabled,
                            onCheckedChange = { adsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CrispWhite,
                                checkedTrackColor = RoyalViolet
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bannerUnitId,
                        onValueChange = { bannerUnitId = it },
                        label = { Text("Banner Ad Unit ID") },
                        singleLine = true,
                        shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                        colors = VedographiDefaults.textFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = rewardedUnitId,
                        onValueChange = { rewardedUnitId = it },
                        label = { Text("Rewarded Ad Unit ID") },
                        singleLine = true,
                        shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                        colors = VedographiDefaults.textFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextButton(
                        onClick = {
                            bannerUnitId = AppMonetizationConfig.DEFAULT_TEST_BANNER_UNIT_ID
                            rewardedUnitId = AppMonetizationConfig.DEFAULT_TEST_REWARDED_UNIT_ID
                        }
                    ) {
                        Text("Reset to Official Google Test IDs", style = MaterialTheme.typography.labelMedium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = adRewardPointsText,
                            onValueChange = { adRewardPointsText = it.filter(Char::isDigit).take(3) },
                            label = { Text("Ad Reward Pts") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                            colors = VedographiDefaults.textFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = referralBonusText,
                            onValueChange = { referralBonusText = it.filter(Char::isDigit).take(2) },
                            label = { Text("Referral Bonus") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                            colors = VedographiDefaults.textFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = announcement,
                        onValueChange = { announcement = it.take(240) },
                        label = { Text("Home Notice Banner (Optional)") },
                        maxLines = 2,
                        shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                        colors = VedographiDefaults.textFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val updated = AppMonetizationConfig(
                                adsEnabled = adsEnabled,
                                bannerAdUnitId = bannerUnitId.trim().ifBlank { AppMonetizationConfig.DEFAULT_TEST_BANNER_UNIT_ID },
                                rewardedAdUnitId = rewardedUnitId.trim().ifBlank { AppMonetizationConfig.DEFAULT_TEST_REWARDED_UNIT_ID },
                                adRewardPoints = (adRewardPointsText.toIntOrNull() ?: AppMonetizationConfig.DEFAULT_AD_REWARD_POINTS).coerceIn(1, 100),
                                referralBonusPoints = (referralBonusText.toIntOrNull() ?: AppMonetizationConfig.DEFAULT_REFERRAL_BONUS_POINTS).coerceIn(1, 50),
                                announcementBanner = announcement.trim()
                            )
                            onSaveConfig(updated)
                        },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.primaryButtonColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(VedographiDefaults.ButtonHeight)
                            .testTag("admin_save_config_btn")
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Cloud AdMob Config", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            item(key = "admin_wallet_test") {
                VedographiContentCard(contentPadding = PaddingValues(16.dp)) {
                    Text(
                        text = "Admin Wallet & Pro Unlock Test",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Current balance: $currentPointsBalance pts. Credit test points to verify Pro Font unlocking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { onGrantSelfPoints(20) },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.outlinedButtonColors(),
                        border = VedographiDefaults.outlinedButtonBorder(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(VedographiDefaults.ButtonHeight)
                            .testTag("admin_grant_points_btn")
                    ) {
                        Icon(Icons.Filled.Diamond, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grant +20 Points to My Wallet", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
