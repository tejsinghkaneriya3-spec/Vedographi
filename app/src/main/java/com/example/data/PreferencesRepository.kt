package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.AppMonetizationConfig
import com.example.domain.CachedEntrySource
import com.example.domain.CachedStyleEntry
import com.example.domain.CustomFontCodec
import com.example.domain.CustomFontDefinition
import com.example.domain.SavedCreation
import com.example.domain.StudioDraftSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vedographi_prefs")

class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.dataStore)

    private object Keys {
        val FAVORITE_IDS = stringPreferencesKey("favorite_font_ids")
        val RECENT_IDS = stringPreferencesKey("recent_font_ids")
        val SAVED_CREATIONS = stringPreferencesKey("saved_creations")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val CACHED_GENERATED_STYLES = stringPreferencesKey("cached_generated_styles")
        val STUDIO_DRAFT_JSON = stringPreferencesKey("studio_draft_json")
        val LAST_CLOUD_CACHE_SYNC_MS = longPreferencesKey("last_cloud_cache_sync_ms")
        val IS_GUEST_MODE = booleanPreferencesKey("is_guest_mode")
        val LAST_OWNER_UID = stringPreferencesKey("last_owner_uid")

        // Points, Pro Fonts, Referrals, Custom Fonts, Quick Guide & Cached Config
        val POINTS_BALANCE = intPreferencesKey("points_balance")
        val UNLOCKED_PRO_FONT_IDS = stringPreferencesKey("unlocked_pro_font_ids")
        val CUSTOM_FONTS_ENCODED = stringPreferencesKey("custom_fonts_encoded")
        val MY_REFERRAL_CODE = stringPreferencesKey("my_referral_code")
        val REDEEMED_REFERRAL_CODE = stringPreferencesKey("redeemed_referral_code")
        val REFERRAL_COUNT = intPreferencesKey("referral_count")
        val ADS_WATCHED_COUNT = intPreferencesKey("ads_watched_count")
        val QUICK_GUIDE_DISMISSED = booleanPreferencesKey("quick_guide_dismissed")
        val MONETIZATION_CONFIG_CACHE = stringPreferencesKey("monetization_config_cache")
    }

    val favoriteFontIds: Flow<Set<String>> = dataStore.data.map { prefs ->
        val raw = prefs[Keys.FAVORITE_IDS].orEmpty()
        if (raw.isBlank()) emptySet() else raw.split("|").filter { it.isNotBlank() }.toSet()
    }

    val recentFontIds: Flow<List<String>> = dataStore.data.map { prefs ->
        val raw = prefs[Keys.RECENT_IDS].orEmpty()
        if (raw.isBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
    }

    val savedCreations: Flow<List<SavedCreation>> = dataStore.data.map { prefs ->
        decodeCreations(prefs[Keys.SAVED_CREATIONS].orEmpty())
    }

    val isDarkTheme: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.IS_DARK_THEME] ?: true
    }

    val cachedGeneratedStyles: Flow<List<CachedStyleEntry>> = dataStore.data.map { prefs ->
        decodeCachedStyles(prefs[Keys.CACHED_GENERATED_STYLES].orEmpty())
    }

    val studioDraftSnapshot: Flow<StudioDraftSnapshot> = dataStore.data.map { prefs ->
        decodeStudioDraft(prefs[Keys.STUDIO_DRAFT_JSON].orEmpty())
    }

    val lastCloudCacheSyncMillis: Flow<Long> = dataStore.data.map { prefs ->
        prefs[Keys.LAST_CLOUD_CACHE_SYNC_MS] ?: 0L
    }

    val guestModeEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.IS_GUEST_MODE] ?: false
    }

    val pointsBalance: Flow<Int> = dataStore.data.map { prefs ->
        (prefs[Keys.POINTS_BALANCE] ?: 0).coerceAtLeast(0)
    }

    val unlockedProFontIds: Flow<Set<String>> = dataStore.data.map { prefs ->
        val raw = prefs[Keys.UNLOCKED_PRO_FONT_IDS].orEmpty()
        if (raw.isBlank()) emptySet() else raw.split("|").filter { it.isNotBlank() }.toSet()
    }

    val customFonts: Flow<List<CustomFontDefinition>> = dataStore.data.map { prefs ->
        CustomFontCodec.decodeList(prefs[Keys.CUSTOM_FONTS_ENCODED].orEmpty())
    }

    val myReferralCode: Flow<String?> = dataStore.data.map { prefs ->
        prefs[Keys.MY_REFERRAL_CODE]?.takeIf { it.isNotBlank() }
    }

    val redeemedReferralCode: Flow<String?> = dataStore.data.map { prefs ->
        prefs[Keys.REDEEMED_REFERRAL_CODE]?.takeIf { it.isNotBlank() }
    }

    val referralCount: Flow<Int> = dataStore.data.map { prefs ->
        (prefs[Keys.REFERRAL_COUNT] ?: 0).coerceAtLeast(0)
    }

    val adsWatchedCount: Flow<Int> = dataStore.data.map { prefs ->
        (prefs[Keys.ADS_WATCHED_COUNT] ?: 0).coerceAtLeast(0)
    }

    val quickGuideDismissed: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.QUICK_GUIDE_DISMISSED] ?: false
    }

    val cachedMonetizationConfig: Flow<AppMonetizationConfig> = dataStore.data.map { prefs ->
        decodeMonetizationConfig(prefs[Keys.MONETIZATION_CONFIG_CACHE].orEmpty())
    }

    suspend fun toggleFavorite(fontId: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_IDS].orEmpty()
                .split("|")
                .filter { it.isNotBlank() }
                .toMutableSet()
            if (current.contains(fontId)) {
                current.remove(fontId)
            } else {
                current.add(fontId)
            }
            prefs[Keys.FAVORITE_IDS] = current.joinToString("|")
        }
    }

    suspend fun recordRecent(fontId: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.RECENT_IDS].orEmpty()
                .split("|")
                .filter { it.isNotBlank() && it != fontId }
                .toMutableList()
            current.add(0, fontId)
            prefs[Keys.RECENT_IDS] = current.take(25).joinToString("|")
        }
    }

    suspend fun clearRecents() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.RECENT_IDS)
        }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_DARK_THEME] = isDark
        }
    }

    suspend fun setQuickGuideDismissed(dismissed: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.QUICK_GUIDE_DISMISSED] = dismissed
        }
    }

    suspend fun addPoints(delta: Int, incrementAdsWatched: Boolean = false, incrementReferralCount: Boolean = false) {
        if (delta == 0 && !incrementAdsWatched && !incrementReferralCount) return
        dataStore.edit { prefs ->
            val current = (prefs[Keys.POINTS_BALANCE] ?: 0).coerceAtLeast(0)
            prefs[Keys.POINTS_BALANCE] = (current + delta).coerceAtLeast(0)
            if (incrementAdsWatched) {
                prefs[Keys.ADS_WATCHED_COUNT] = (prefs[Keys.ADS_WATCHED_COUNT] ?: 0) + 1
            }
            if (incrementReferralCount) {
                prefs[Keys.REFERRAL_COUNT] = (prefs[Keys.REFERRAL_COUNT] ?: 0) + 1
            }
        }
    }

    suspend fun spendPointsAndUnlockProFont(fontId: String, cost: Int): Boolean {
        var unlockedNow = false
        dataStore.edit { prefs ->
            val currentUnlocked = prefs[Keys.UNLOCKED_PRO_FONT_IDS].orEmpty()
                .split("|")
                .filter { it.isNotBlank() }
                .toMutableSet()
            if (fontId in currentUnlocked) {
                unlockedNow = true
                return@edit
            }
            val balance = (prefs[Keys.POINTS_BALANCE] ?: 0).coerceAtLeast(0)
            if (balance >= cost) {
                prefs[Keys.POINTS_BALANCE] = (balance - cost).coerceAtLeast(0)
                currentUnlocked.add(fontId)
                prefs[Keys.UNLOCKED_PRO_FONT_IDS] = currentUnlocked.joinToString("|")
                unlockedNow = true
            }
        }
        return unlockedNow
    }

    suspend fun setMyReferralCode(code: String) {
        if (code.isBlank()) return
        dataStore.edit { prefs ->
            prefs[Keys.MY_REFERRAL_CODE] = code.trim().uppercase()
        }
    }

    suspend fun recordRedeemedReferral(code: String, bonusPoints: Int) {
        dataStore.edit { prefs ->
            val alreadyRedeemed = prefs[Keys.REDEEMED_REFERRAL_CODE].orEmpty()
            if (alreadyRedeemed.isBlank()) {
                prefs[Keys.REDEEMED_REFERRAL_CODE] = code.trim().uppercase()
                val current = (prefs[Keys.POINTS_BALANCE] ?: 0).coerceAtLeast(0)
                prefs[Keys.POINTS_BALANCE] = current + bonusPoints.coerceAtLeast(0)
            }
        }
    }

    suspend fun saveCustomFont(definition: CustomFontDefinition) {
        dataStore.edit { prefs ->
            val list = CustomFontCodec.decodeList(prefs[Keys.CUSTOM_FONTS_ENCODED].orEmpty()).toMutableList()
            list.removeAll { it.id == definition.id }
            list.add(0, definition)
            prefs[Keys.CUSTOM_FONTS_ENCODED] = CustomFontCodec.encodeList(list.take(30))
        }
    }

    suspend fun deleteCustomFont(fontId: String) {
        dataStore.edit { prefs ->
            val list = CustomFontCodec.decodeList(prefs[Keys.CUSTOM_FONTS_ENCODED].orEmpty())
                .filterNot { it.id == fontId }
            prefs[Keys.CUSTOM_FONTS_ENCODED] = CustomFontCodec.encodeList(list)
        }
    }

    suspend fun cacheCustomFonts(cloudCustomFonts: List<CustomFontDefinition>) {
        dataStore.edit { prefs ->
            val cloudIds = cloudCustomFonts.map { it.id }.toSet()
            val existingLocal = CustomFontCodec.decodeList(prefs[Keys.CUSTOM_FONTS_ENCODED].orEmpty())
            val localOnly = existingLocal.filter { it.id !in cloudIds }
            prefs[Keys.CUSTOM_FONTS_ENCODED] = CustomFontCodec.encodeList((localOnly + cloudCustomFonts).take(30))
        }
    }

    suspend fun cacheMonetizationConfig(config: AppMonetizationConfig) {
        dataStore.edit { prefs ->
            prefs[Keys.MONETIZATION_CONFIG_CACHE] = encodeMonetizationConfig(config)
        }
    }

    suspend fun saveCreation(creation: SavedCreation) {
        dataStore.edit { prefs ->
            val list = decodeCreations(prefs[Keys.SAVED_CREATIONS].orEmpty()).toMutableList()
            list.removeAll { it.content == creation.content }
            list.add(0, creation)
            prefs[Keys.SAVED_CREATIONS] = encodeCreations(list.take(40))
        }
    }

    suspend fun deleteCreation(id: String) {
        dataStore.edit { prefs ->
            val list = decodeCreations(prefs[Keys.SAVED_CREATIONS].orEmpty())
                .filterNot { it.id == id }
            prefs[Keys.SAVED_CREATIONS] = encodeCreations(list)
        }
    }

    suspend fun cacheGeneratedStyle(entry: CachedStyleEntry) {
        if (entry.styledText.isBlank()) return
        dataStore.edit { prefs ->
            val list = decodeCachedStyles(prefs[Keys.CACHED_GENERATED_STYLES].orEmpty()).toMutableList()
            list.removeAll { it.styledText == entry.styledText && it.source == entry.source }
            list.add(0, entry)
            prefs[Keys.CACHED_GENERATED_STYLES] = encodeCachedStyles(list.take(MAX_CACHED_STYLES))
        }
    }

    suspend fun removeCachedStyle(id: String) {
        dataStore.edit { prefs ->
            val list = decodeCachedStyles(prefs[Keys.CACHED_GENERATED_STYLES].orEmpty())
                .filterNot { it.id == id }
            prefs[Keys.CACHED_GENERATED_STYLES] = encodeCachedStyles(list)
        }
    }

    suspend fun clearCachedStyles() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.CACHED_GENERATED_STYLES)
        }
    }

    suspend fun saveStudioDraft(transform: (StudioDraftSnapshot) -> StudioDraftSnapshot) {
        dataStore.edit { prefs ->
            val current = decodeStudioDraft(prefs[Keys.STUDIO_DRAFT_JSON].orEmpty())
            val updated = transform(current)
            prefs[Keys.STUDIO_DRAFT_JSON] = encodeStudioDraft(updated)
        }
    }

    /**
     * Read-through DataStore mirror of Firestore profile so favorites, recents, points,
     * unlocked Pro fonts, referral state, and theme remain immediately available offline.
     */
    suspend fun cacheCloudProfile(
        darkTheme: Boolean,
        favoriteIds: Set<String>,
        recentIds: List<String>,
        pointsBalance: Int? = null,
        unlockedProIds: Set<String>? = null,
        referralCode: String? = null,
        referredByCode: String? = null,
        referralCount: Int? = null,
        adsWatchedCount: Int? = null,
        syncedAtMillis: Long = System.currentTimeMillis()
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_DARK_THEME] = darkTheme
            prefs[Keys.FAVORITE_IDS] = favoriteIds.filter { it.isNotBlank() }.joinToString("|")
            prefs[Keys.RECENT_IDS] = recentIds.filter { it.isNotBlank() }.take(25).joinToString("|")
            if (pointsBalance != null) {
                prefs[Keys.POINTS_BALANCE] = pointsBalance.coerceAtLeast(0)
            }
            if (unlockedProIds != null) {
                prefs[Keys.UNLOCKED_PRO_FONT_IDS] = unlockedProIds.filter { it.isNotBlank() }.joinToString("|")
            }
            if (!referralCode.isNullOrBlank()) {
                prefs[Keys.MY_REFERRAL_CODE] = referralCode
            }
            if (!referredByCode.isNullOrBlank()) {
                prefs[Keys.REDEEMED_REFERRAL_CODE] = referredByCode
            }
            if (referralCount != null) {
                prefs[Keys.REFERRAL_COUNT] = referralCount.coerceAtLeast(0)
            }
            if (adsWatchedCount != null) {
                prefs[Keys.ADS_WATCHED_COUNT] = adsWatchedCount.coerceAtLeast(0)
            }
            prefs[Keys.LAST_CLOUD_CACHE_SYNC_MS] = syncedAtMillis
        }
    }

    suspend fun cacheCloudCreations(
        cloudCreations: List<SavedCreation>,
        syncedAtMillis: Long = System.currentTimeMillis()
    ) {
        dataStore.edit { prefs ->
            val cloudContents = cloudCreations.map { it.content }.toSet()
            val cloudIds = cloudCreations.map { it.id }.toSet()
            val existingLocal = decodeCreations(prefs[Keys.SAVED_CREATIONS].orEmpty())
            val localOnly = existingLocal.filter { it.id !in cloudIds && it.content !in cloudContents }
            val combined = (localOnly + cloudCreations).take(40)
            prefs[Keys.SAVED_CREATIONS] = encodeCreations(combined)
            prefs[Keys.LAST_CLOUD_CACHE_SYNC_MS] = syncedAtMillis
        }
    }

    suspend fun setGuestMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_GUEST_MODE] = enabled
        }
    }

    suspend fun prepareSessionForUser(userId: String) {
        if (userId.isBlank()) return
        dataStore.edit { prefs ->
            val previousOwner = prefs[Keys.LAST_OWNER_UID].orEmpty()
            if (previousOwner.isNotBlank() && previousOwner != userId) {
                prefs.remove(Keys.FAVORITE_IDS)
                prefs.remove(Keys.RECENT_IDS)
                prefs.remove(Keys.SAVED_CREATIONS)
                prefs.remove(Keys.CACHED_GENERATED_STYLES)
                prefs.remove(Keys.STUDIO_DRAFT_JSON)
                prefs.remove(Keys.LAST_CLOUD_CACHE_SYNC_MS)
                prefs.remove(Keys.POINTS_BALANCE)
                prefs.remove(Keys.UNLOCKED_PRO_FONT_IDS)
                prefs.remove(Keys.CUSTOM_FONTS_ENCODED)
                prefs.remove(Keys.MY_REFERRAL_CODE)
                prefs.remove(Keys.REDEEMED_REFERRAL_CODE)
                prefs.remove(Keys.REFERRAL_COUNT)
                prefs.remove(Keys.ADS_WATCHED_COUNT)
            }
            prefs[Keys.LAST_OWNER_UID] = userId
            prefs[Keys.IS_GUEST_MODE] = false
        }
    }

    suspend fun clearSessionOnSignOut() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.FAVORITE_IDS)
            prefs.remove(Keys.RECENT_IDS)
            prefs.remove(Keys.SAVED_CREATIONS)
            prefs.remove(Keys.CACHED_GENERATED_STYLES)
            prefs.remove(Keys.STUDIO_DRAFT_JSON)
            prefs.remove(Keys.LAST_CLOUD_CACHE_SYNC_MS)
            prefs.remove(Keys.LAST_OWNER_UID)
            prefs.remove(Keys.POINTS_BALANCE)
            prefs.remove(Keys.UNLOCKED_PRO_FONT_IDS)
            prefs.remove(Keys.CUSTOM_FONTS_ENCODED)
            prefs.remove(Keys.MY_REFERRAL_CODE)
            prefs.remove(Keys.REDEEMED_REFERRAL_CODE)
            prefs.remove(Keys.REFERRAL_COUNT)
            prefs.remove(Keys.ADS_WATCHED_COUNT)
            prefs[Keys.IS_GUEST_MODE] = false
        }
    }

    suspend fun clearAllUserData() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.FAVORITE_IDS)
            prefs.remove(Keys.RECENT_IDS)
            prefs.remove(Keys.SAVED_CREATIONS)
            prefs.remove(Keys.CACHED_GENERATED_STYLES)
            prefs.remove(Keys.STUDIO_DRAFT_JSON)
            prefs.remove(Keys.LAST_CLOUD_CACHE_SYNC_MS)
            prefs.remove(Keys.CUSTOM_FONTS_ENCODED)
        }
    }

    private fun escapeField(value: String): String {
        val sb = StringBuilder(value.length)
        for (ch in value) {
            when (ch) {
                '\\' -> sb.append("\\\\")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '|' -> sb.append("\\p")
                ';' -> sb.append("\\s")
                '=' -> sb.append("\\e")
                '\u001E' -> sb.append("\\E")
                '\u001F' -> sb.append("\\F")
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun unescapeField(encoded: String): String {
        val sb = StringBuilder(encoded.length)
        var i = 0
        while (i < encoded.length) {
            val ch = encoded[i]
            if (ch == '\\' && i + 1 < encoded.length) {
                when (encoded[i + 1]) {
                    '\\' -> sb.append('\\')
                    'n' -> sb.append('\n')
                    'r' -> sb.append('\r')
                    'p' -> sb.append('|')
                    's' -> sb.append(';')
                    'e' -> sb.append('=')
                    'E' -> sb.append('\u001E')
                    'F' -> sb.append('\u001F')
                    else -> sb.append(encoded[i + 1])
                }
                i += 2
            } else {
                sb.append(ch)
                i += 1
            }
        }
        return sb.toString()
    }

    private fun encodeCreations(list: List<SavedCreation>): String {
        return list.joinToString("\u001E") { item ->
            listOf(
                escapeField(item.id),
                escapeField(item.title),
                escapeField(item.content),
                escapeField(item.kind)
            ).joinToString("\u001F")
        }
    }

    private fun decodeCreations(raw: String): List<SavedCreation> {
        if (raw.isBlank()) return emptyList()
        return raw.split("\u001E").mapNotNull { record ->
            val parts = record.split("\u001F")
            if (parts.size >= 4) {
                SavedCreation(
                    id = unescapeField(parts[0]),
                    title = unescapeField(parts[1]),
                    content = unescapeField(parts[2]),
                    kind = unescapeField(parts[3])
                )
            } else {
                null
            }
        }
    }

    private fun encodeCachedStyles(list: List<CachedStyleEntry>): String {
        return list.joinToString("\n") { entry ->
            listOf(
                escapeField(entry.id),
                escapeField(entry.title),
                escapeField(entry.inputText),
                escapeField(entry.styledText),
                escapeField(entry.source.name),
                escapeField(entry.templateId.orEmpty()),
                escapeField(entry.fontId.orEmpty()),
                escapeField(entry.wingId.orEmpty()),
                escapeField(entry.borderId.orEmpty()),
                escapeField(entry.separatorId.orEmpty()),
                entry.timestampMillis.toString()
            ).joinToString("|")
        }
    }

    private fun decodeCachedStyles(raw: String): List<CachedStyleEntry> {
        if (raw.isBlank()) return emptyList()
        return raw.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size < 11) return@mapNotNull null
                val styledText = unescapeField(parts[3])
                if (styledText.isBlank()) return@mapNotNull null
                CachedStyleEntry(
                    id = unescapeField(parts[0]),
                    title = unescapeField(parts[1]).ifBlank { "Styled Text" },
                    inputText = unescapeField(parts[2]),
                    styledText = styledText,
                    source = CachedEntrySource.fromStored(unescapeField(parts[4])),
                    templateId = unescapeField(parts[5]).takeIf { it.isNotEmpty() },
                    fontId = unescapeField(parts[6]).takeIf { it.isNotEmpty() },
                    wingId = unescapeField(parts[7]).takeIf { it.isNotEmpty() },
                    borderId = unescapeField(parts[8]).takeIf { it.isNotEmpty() },
                    separatorId = unescapeField(parts[9]).takeIf { it.isNotEmpty() },
                    timestampMillis = parts[10].toLongOrNull() ?: 0L
                )
            }
            .toList()
    }

    private fun encodeStudioDraft(draft: StudioDraftSnapshot): String {
        val pairs = buildList {
            draft.fontInputText?.let { add("fontInputText" to it) }
            draft.decoratorInput?.let { add("decoratorInput" to it) }
            draft.decoratorFontId?.let { add("decoratorFontId" to it) }
            draft.decoratorWingId?.let { add("decoratorWingId" to it) }
            draft.decoratorBorderId?.let { add("decoratorBorderId" to it) }
            draft.decoratorSeparatorId?.let { add("decoratorSeparatorId" to it) }
            draft.bioHeadline?.let { add("bioHeadline" to it) }
            draft.bioBodyText?.let { add("bioBodyText" to it) }
            draft.usernameInput?.let { add("usernameInput" to it) }
            draft.nameDesignerName?.let { add("nameDesignerName" to it) }
            draft.nameDesignerSubtitle?.let { add("nameDesignerSubtitle" to it) }
            draft.waHeaderTag?.let { add("waHeaderTag" to it) }
            draft.waBodyText?.let { add("waBodyText" to it) }
        }
        return pairs.joinToString(";") { (k, v) -> "${escapeField(k)}=${escapeField(v)}" }
    }

    private fun decodeStudioDraft(raw: String): StudioDraftSnapshot {
        if (raw.isBlank()) return StudioDraftSnapshot()
        val map = mutableMapOf<String, String>()
        raw.split(";").forEach { token ->
            val idx = token.indexOf('=')
            if (idx > 0) {
                val key = unescapeField(token.substring(0, idx))
                val value = unescapeField(token.substring(idx + 1))
                map[key] = value
            }
        }
        return StudioDraftSnapshot(
            fontInputText = map["fontInputText"],
            decoratorInput = map["decoratorInput"],
            decoratorFontId = map["decoratorFontId"],
            decoratorWingId = map["decoratorWingId"],
            decoratorBorderId = map["decoratorBorderId"],
            decoratorSeparatorId = map["decoratorSeparatorId"],
            bioHeadline = map["bioHeadline"],
            bioBodyText = map["bioBodyText"],
            usernameInput = map["usernameInput"],
            nameDesignerName = map["nameDesignerName"],
            nameDesignerSubtitle = map["nameDesignerSubtitle"],
            waHeaderTag = map["waHeaderTag"],
            waBodyText = map["waBodyText"]
        )
    }

    private fun encodeMonetizationConfig(config: AppMonetizationConfig): String {
        return listOf(
            config.adsEnabled.toString(),
            escapeField(config.bannerAdUnitId),
            escapeField(config.rewardedAdUnitId),
            config.adRewardPoints.toString(),
            config.referralBonusPoints.toString(),
            escapeField(config.announcementBanner)
        ).joinToString("|")
    }

    private fun decodeMonetizationConfig(raw: String): AppMonetizationConfig {
        if (raw.isBlank()) return AppMonetizationConfig()
        val parts = raw.split("|")
        if (parts.size < 6) return AppMonetizationConfig()
        return AppMonetizationConfig(
            adsEnabled = parts[0].toBooleanStrictOrNull() ?: true,
            bannerAdUnitId = unescapeField(parts[1]).ifBlank { AppMonetizationConfig.DEFAULT_TEST_BANNER_UNIT_ID },
            rewardedAdUnitId = unescapeField(parts[2]).ifBlank { AppMonetizationConfig.DEFAULT_TEST_REWARDED_UNIT_ID },
            adRewardPoints = parts[3].toIntOrNull()?.coerceIn(1, 100) ?: AppMonetizationConfig.DEFAULT_AD_REWARD_POINTS,
            referralBonusPoints = parts[4].toIntOrNull()?.coerceIn(1, 50) ?: AppMonetizationConfig.DEFAULT_REFERRAL_BONUS_POINTS,
            announcementBanner = unescapeField(parts[5])
        )
    }

    private companion object {
        const val MAX_CACHED_STYLES = 60
    }
}
