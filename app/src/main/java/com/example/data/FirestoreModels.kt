package com.example.data

import android.util.Log
import androidx.annotation.Keep
import com.example.domain.AdminConfig
import com.example.domain.AppMonetizationConfig
import com.example.domain.CustomFontCodec
import com.example.domain.CustomFontDefinition
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.CancellationException
import org.json.JSONArray
import org.json.JSONObject

@Keep
@IgnoreExtraProperties
data class CloudUserProfile(
    val userId: String = "",
    val displayName: String? = null,
    val darkTheme: Boolean = true,
    val favoriteFontIds: List<String> = emptyList(),
    val recentFontIds: List<String> = emptyList(),
    val pointsBalance: Long = 0L,
    val unlockedProFontIds: List<String> = emptyList(),
    val referralCode: String? = null,
    val referredByCode: String? = null,
    val referralCount: Long = 0L,
    val adsWatchedCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudSavedCreation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val kind: String = "DECORATED",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudCustomFont(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val charMapEncoded: String = "",
    val prefix: String = "",
    val suffix: String = "",
    val letterJoiner: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomain(): CustomFontDefinition = CustomFontDefinition(
        id = id,
        name = name,
        charMap = CustomFontCodec.decodeMap(charMapEncoded),
        prefix = prefix,
        suffix = suffix,
        letterJoiner = letterJoiner,
        createdAtMillis = createdAt?.toDate()?.time ?: System.currentTimeMillis()
    )
}

@Keep
@IgnoreExtraProperties
data class CloudMonetizationConfig(
    val adsEnabled: Boolean = true,
    val bannerAdUnitId: String = AppMonetizationConfig.DEFAULT_TEST_BANNER_UNIT_ID,
    val rewardedAdUnitId: String = AppMonetizationConfig.DEFAULT_TEST_REWARDED_UNIT_ID,
    val adRewardPoints: Long = AppMonetizationConfig.DEFAULT_AD_REWARD_POINTS.toLong(),
    val referralBonusPoints: Long = AppMonetizationConfig.DEFAULT_REFERRAL_BONUS_POINTS.toLong(),
    val announcementBanner: String = "",
    val updatedBy: String = AdminConfig.ADMIN_EMAIL,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomain(): AppMonetizationConfig = AppMonetizationConfig(
        adsEnabled = adsEnabled,
        bannerAdUnitId = bannerAdUnitId.ifBlank { AppMonetizationConfig.DEFAULT_TEST_BANNER_UNIT_ID },
        rewardedAdUnitId = rewardedAdUnitId.ifBlank { AppMonetizationConfig.DEFAULT_TEST_REWARDED_UNIT_ID },
        adRewardPoints = adRewardPoints.toInt().coerceIn(1, 100),
        referralBonusPoints = referralBonusPoints.toInt().coerceIn(1, 50),
        announcementBanner = announcementBanner
    )
}

sealed interface CloudUiState<out T> {
    data object Loading : CloudUiState<Nothing>
    data class Success<T>(val data: T) : CloudUiState<T>
    data class Error(val message: String) : CloudUiState<Nothing>
}

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(
    exception: Throwable,
    operationType: OperationType,
    path: String?
): String {
    val causes = generateSequence(exception) { it.cause }
    val rootError = causes.firstOrNull { it is FirebaseFirestoreException }
        ?: causes.firstOrNull { it !is CancellationException }
        ?: exception

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", rootError.message ?: rootError.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}
