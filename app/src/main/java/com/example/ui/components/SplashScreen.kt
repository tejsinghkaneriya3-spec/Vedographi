package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonOrchid
import com.example.ui.theme.ObsidianNight
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.SoftLilac
import kotlinx.coroutines.delay

@Composable
fun VedographiSplashScreen(
    onFinished: () -> Unit
) {
    val logoScale = remember { Animatable(0.75f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
        )
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 450)
        )
        delay(850)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF28134E),
                        Color(0xFF120924),
                        ObsidianNight
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinished
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFF0E091F),
                shadowElevation = 24.dp,
                modifier = Modifier
                    .size(124.dp)
                    .scale(logoScale.value)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.vedographi_icon_1795394652656),
                    contentDescription = stringResource(R.string.app_name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(32.dp))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "VEDOGRAPHi",
                style = MaterialTheme.typography.displayLarge.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White, SoftLilac, ElectricViolet, NeonOrchid)
                    ),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                modifier = Modifier.alpha(contentAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.brand_tagline),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = SoftLilac,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.alpha(contentAlpha.value)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .alpha(contentAlpha.value)
                    .clip(RoundedCornerShape(50))
                    .background(RoyalViolet.copy(alpha = 0.25f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "120+ Unicode Fonts • हिंदी Safe",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
