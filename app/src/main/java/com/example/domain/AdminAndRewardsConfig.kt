package com.example.domain

/**
 * Admin authorization & AdMob/Rewards configuration.
 * Admin tools are strictly restricted to ADMIN_EMAIL both in client UI and Firestore security rules.
 */
object AdminConfig {
    const val ADMIN_EMAIL = "tejsinghkaneriya3@gmail.com"

    fun isAdminUser(email: String?, isGuest: Boolean): Boolean {
        if (isGuest || email.isNullOrBlank()) return false
        return email.trim().equals(ADMIN_EMAIL, ignoreCase = true)
    }
}

data class AppMonetizationConfig(
    val adsEnabled: Boolean = true,
    val bannerAdUnitId: String = DEFAULT_TEST_BANNER_UNIT_ID,
    val rewardedAdUnitId: String = DEFAULT_TEST_REWARDED_UNIT_ID,
    val adRewardPoints: Int = DEFAULT_AD_REWARD_POINTS,
    val referralBonusPoints: Int = DEFAULT_REFERRAL_BONUS_POINTS,
    val announcementBanner: String = ""
) {
    companion object {
        // Official Google AdMob Android Test Ad Unit IDs (safe out of the box; replaceable via Admin Panel)
        const val DEFAULT_TEST_BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val DEFAULT_TEST_REWARDED_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        const val DEFAULT_AD_REWARD_POINTS = 5
        const val DEFAULT_REFERRAL_BONUS_POINTS = 2
    }
}
