package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.VedographiContentCard
import com.example.ui.components.VedographiDefaults
import com.example.ui.components.VedographiHeroCard
import com.example.ui.components.VedographiPillBadge
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonOrchid
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.WhatsAppGreen

@Composable
fun HowToUseScreen(
    onBack: () -> Unit,
    onTryFontsClick: () -> Unit,
    onOpenEarnPointsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("how_to_use_screen")
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
                    .testTag("how_to_use_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.how_to_use_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "guide_intro") {
                VedographiHeroCard(contentPadding = PaddingValues(18.dp)) {
                    VedographiPillBadge(text = "Beginner Friendly • आसान गाइड")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "VEDOGRAPHi 10-Second Guide",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Stylish English, Hindi Calligraphy, aur custom Unicode fonts banaye aur seedha WhatsApp, Facebook, Instagram par copy-paste karein.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item(key = "step_1") {
                GuideStepCard(
                    stepNumber = "1",
                    icon = Icons.Filled.TextFields,
                    accent = RoyalViolet,
                    title = "Type Text & Tap Copy (लिखें और Copy करें)",
                    englishTip = "Type anything in English or Hindi in the top box on the Fonts tab. Every card transforms live — tap any card or 'Copy' button, or use 'Copy All' for multiple styles.",
                    hinglishTip = "Upar box me apna naam ya shayari likhein. Neeche saare fonts turant badal jayenge — bas kisi bhi card par tap karein aur WhatsApp/FB me paste kar dein!",
                    exampleText = "Sample: 𝐕𝐄𝐃𝐎𝐆𝐑𝐀𝐏𝐇𝐢 • ꧁ हर शब्द ꧂"
                )
            }

            item(key = "step_2") {
                GuideStepCard(
                    stepNumber = "2",
                    icon = Icons.Filled.CheckCircle,
                    accent = WhatsAppGreen,
                    title = "WhatsApp & Facebook Safe Filter",
                    englishTip = "Turn ON the 'WA & FB Safe' filter chip on Fonts to show only styles guaranteed to display cleanly in WhatsApp chats, Status, and Facebook posts without boxes.",
                    hinglishTip = "Agar kisi phone me boxes dikhen to 'WA & FB Safe' filter on rakhein. Isme WhatsApp Bold (*text*), Italic (_text_), aur saaf Unicode fonts hi dikhte hain.",
                    exampleText = "WA Native: *हर शब्द का अपना अंदाज़*  |  _Royal Look_"
                )
            }

            item(key = "step_3") {
                GuideStepCard(
                    stepNumber = "3",
                    icon = Icons.Filled.Brush,
                    accent = NeonOrchid,
                    title = "Hindi Calligraphy Fonts (हिंदी स्टाइल)",
                    englishTip = "Choose 'Hindi Calligraphy' chip for Devanagari akshara-safe fonts (preserving all matras like अंदाज़ & शब्द) plus Share Card image export.",
                    hinglishTip = "Hindi fonts ab matra aur aadhe aksharon ko bina tode style karte hain (जैसे: ह•र श•ब्द, 『नमस्ते』, ✦ शुभ प्रभात ✦) aur Calligraphy Card photo bhi share kar sakte hain.",
                    exampleText = "『नमस्ते भारत』 • ह•र श•ब्द"
                )
            }

            item(key = "step_4") {
                GuideStepCard(
                    stepNumber = "4",
                    icon = Icons.Filled.Tune,
                    accent = EmeraldSuccess,
                    title = "Custom Style Studio (खुद का फॉन्ट बनाएं)",
                    englishTip = "Open Studio -> 'Custom Font' to assign custom Unicode glyphs to any letter A-Z, a-z, 0-9, add Royal wings or joiners, and save directly into 'My Custom'.",
                    hinglishTip = "Studio me 'Custom Font' khol kar har letter ke liye apni pasand ka symbol chunein, Save Font dabaye, aur woh hamesha aapke Fonts tab me rahega.",
                    exampleText = "꧁ Ѵᴇᴅᴏɢʀᴀᴘʜɪ ꧂"
                )
            }

            item(key = "step_5") {
                GuideStepCard(
                    stepNumber = "5",
                    icon = Icons.Filled.Diamond,
                    accent = NeonOrchid,
                    title = "Earn Points, Refer (+2) & Pro Fonts",
                    englishTip = "Tap the 💎 Points pill in the top bar anytime. Watch Rewarded Ads as many times as you like to earn points, or share your Referral Code to get +2 points, then unlock Pro Fonts permanently!",
                    hinglishTip = "Top bar me 💎 Points par tap karein. Jitni baar chahein Ad dekh kar points kamayein ya dost ko refer karke +2 points paayein, aur Pro Fonts unlock karein.",
                    exampleText = "Watch Ad (+5 pts) + Refer Friend (+2 pts) -> Unlock PRO!"
                )
            }

            item(key = "guide_actions") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onTryFontsClick,
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.outlinedButtonColors(),
                        border = VedographiDefaults.outlinedButtonBorder(),
                        modifier = Modifier
                            .weight(1f)
                            .height(VedographiDefaults.ButtonHeight)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Try Fonts Now", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = onOpenEarnPointsClick,
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.primaryButtonColors(),
                        modifier = Modifier
                            .weight(1f)
                            .height(VedographiDefaults.ButtonHeight)
                    ) {
                        Icon(Icons.Filled.Diamond, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Earn Points", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun GuideStepCard(
    stepNumber: String,
    icon: ImageVector,
    accent: Color,
    title: String,
    englishTip: String,
    hinglishTip: String,
    exampleText: String
) {
    VedographiContentCard(contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Step $stepNumber",
                    style = MaterialTheme.typography.labelSmall,
                    color = accent
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = englishTip,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = hinglishTip,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = exampleText,
                style = MaterialTheme.typography.labelMedium,
                color = CrispWhite.takeIf { false } ?: MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
    }
}
