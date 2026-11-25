package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.domain.AppMonetizationConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

object AdMobManager {
    private const val TAG = "VedographiAdMob"
    private val initialized = AtomicBoolean(false)

    private val _isRewardedReady = MutableStateFlow(false)
    val isRewardedReady: StateFlow<Boolean> = _isRewardedReady.asStateFlow()

    private val _isRewardedLoading = MutableStateFlow(false)
    val isRewardedLoading: StateFlow<Boolean> = _isRewardedLoading.asStateFlow()

    private val _lastAdStatus = MutableStateFlow<String?>(null)
    val lastAdStatus: StateFlow<String?> = _lastAdStatus.asStateFlow()

    private var cachedRewardedAd: RewardedAd? = null
    private var loadedUnitId: String? = null

    fun initialize(context: Context) {
        if (initialized.compareAndSet(false, true)) {
            runCatching {
                MobileAds.initialize(context.applicationContext) {
                    Log.d(TAG, "AdMob MobileAds initialized")
                }
            }.onFailure { err ->
                Log.w(TAG, "MobileAds init skipped/failed: ${err.message}")
            }
        }
    }

    fun preloadRewardedAd(
        context: Context,
        adUnitId: String = AppMonetizationConfig.DEFAULT_TEST_REWARDED_UNIT_ID
    ) {
        val cleanId = adUnitId.trim().ifBlank { AppMonetizationConfig.DEFAULT_TEST_REWARDED_UNIT_ID }
        initialize(context)

        if (cachedRewardedAd != null && loadedUnitId == cleanId) {
            _isRewardedReady.value = true
            return
        }
        if (_isRewardedLoading.value) return

        _isRewardedLoading.value = true
        _lastAdStatus.value = "Loading rewarded ad…"
        runCatching {
            val request = AdRequest.Builder().build()
            RewardedAd.load(
                context.applicationContext,
                cleanId,
                request,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        cachedRewardedAd = ad
                        loadedUnitId = cleanId
                        _isRewardedLoading.value = false
                        _isRewardedReady.value = true
                        _lastAdStatus.value = "Rewarded ad ready!"
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        cachedRewardedAd = null
                        _isRewardedLoading.value = false
                        _isRewardedReady.value = false
                        _lastAdStatus.value = "Ad unavailable right now (${error.code}). Check internet & retry."
                        Log.w(TAG, "Rewarded ad load failed: ${error.message}")
                    }
                }
            )
        }.onFailure { err ->
            _isRewardedLoading.value = false
            _isRewardedReady.value = false
            _lastAdStatus.value = "Ad service unavailable on this device."
            Log.w(TAG, "Rewarded ad load exception: ${err.message}")
        }
    }

    fun showRewardedAd(
        activity: Activity?,
        adUnitId: String,
        expectedPoints: Int,
        onRewardEarned: (Int) -> Unit,
        onStatusMessage: (String) -> Unit
    ) {
        if (activity == null) {
            onStatusMessage("Unable to open ad from current screen.")
            return
        }
        val cleanId = adUnitId.trim().ifBlank { AppMonetizationConfig.DEFAULT_TEST_REWARDED_UNIT_ID }
        val ad = cachedRewardedAd
        if (ad == null || loadedUnitId != cleanId) {
            preloadRewardedAd(activity, cleanId)
            onStatusMessage("Loading rewarded video… please tap Watch Ad again in a few seconds.")
            return
        }

        cachedRewardedAd = null
        _isRewardedReady.value = false

        var rewardGranted = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preloadRewardedAd(activity, cleanId)
                if (!rewardGranted) {
                    onStatusMessage("Watch full ad to earn +$expectedPoints points.")
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                preloadRewardedAd(activity, cleanId)
                onStatusMessage("Ad could not be shown (${adError.message}). Retrying load…")
            }
        }

        ad.show(activity) { _ ->
            rewardGranted = true
            onRewardEarned(expectedPoints)
        }
    }
}

@Composable
fun AdMobBannerBar(
    adUnitId: String,
    adsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    if (!adsEnabled) return
    val cleanId = remember(adUnitId) {
        adUnitId.trim().ifBlank { AppMonetizationConfig.DEFAULT_TEST_BANNER_UNIT_ID }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_banner_container")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 6.dp)
        ) {
            Text(
                text = "Ad • Supports free fonts",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            ) {
                val adViewRef = remember { arrayOfNulls<AdView>(1) }
                DisposableEffect(cleanId) {
                    onDispose {
                        runCatching { adViewRef[0]?.destroy() }
                        adViewRef[0] = null
                    }
                }
                AndroidView(
                    factory = { ctx ->
                        AdMobManager.initialize(ctx)
                        runCatching {
                            AdView(ctx).apply {
                                setAdSize(AdSize.BANNER)
                                setAdUnitId(cleanId)
                                adListener = object : AdListener() {
                                    override fun onAdFailedToLoad(error: LoadAdError) {
                                        Log.d("VedographiAdMob", "Banner failed: ${error.message}")
                                    }
                                }
                                loadAd(AdRequest.Builder().build())
                            }.also { adViewRef[0] = it }
                        }.getOrElse {
                            FrameLayout(ctx)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
