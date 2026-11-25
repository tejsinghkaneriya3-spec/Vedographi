package com.example

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.PreferencesRepository
import com.example.domain.AdminConfig
import com.example.domain.CustomFontCodec
import com.example.domain.CustomFontDefinition
import com.example.domain.CustomStylePresets
import com.example.domain.FontCatalog
import com.example.domain.FontCategory
import com.example.domain.UnicodeFontEngine
import com.example.ui.UnlockProResult
import com.example.ui.VedographiViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class VedographiFeatureTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `devanagari akshara clustering preserves conjuncts and matras cleanly`() {
        val word = "शब्द"
        val interleaved = UnicodeFontEngine.hindiAksharaInterleave(word, "•")
        assertEquals("श•ब्द", interleaved)

        val namaste = UnicodeFontEngine.hindiAksharaInterleave("नमस्ते", "•")
        assertEquals("न•म•स्ते", namaste)

        val wrapped = UnicodeFontEngine.hindiWordWrap("हर शब्द", "『", "』")
        assertEquals("『हर』 『शब्द』", wrapped)
    }

    @Test
    fun `custom font definition transforms ascii per-char and round-trips through codec`() {
        val def = CustomFontDefinition(
            id = "custom_test_1",
            name = "Tej Mix: Pro, Royal",
            charMap = mapOf('A' to "Λ", 'a' to "α", '1' to "①"),
            prefix = "꧁ ",
            suffix = " ꧂",
            letterJoiner = "•",
            createdAtMillis = 1732512000000L
        )
        assertEquals("꧁ Λ•α•① नमस्ते ꧂", def.transform("Aa1 नमस्ते"))

        val encoded = CustomFontCodec.encodeList(listOf(def))
        val decoded = CustomFontCodec.decodeList(encoded)
        assertEquals(1, decoded.size)
        assertEquals(def.id, decoded[0].id)
        assertEquals(def.name, decoded[0].name)
        assertEquals(def.charMap, decoded[0].charMap)
        assertEquals(def.prefix, decoded[0].prefix)
        assertEquals(def.suffix, decoded[0].suffix)
        assertEquals(def.letterJoiner, decoded[0].letterJoiner)

        val mix = CustomStylePresets.randomizeMix(seed = 42)
        assertTrue(mix.isNotEmpty())
    }

    @Test
    fun `admin check strictly allows only tejsinghkaneriya3@gmail com when signed in`() {
        assertTrue(AdminConfig.isAdminUser("tejsinghkaneriya3@gmail.com", isGuest = false))
        assertTrue(AdminConfig.isAdminUser("  TejSinghKaneriya3@gmail.com  ", isGuest = false))
        assertFalse(AdminConfig.isAdminUser("tejsinghkaneriya3@gmail.com", isGuest = true))
        assertFalse(AdminConfig.isAdminUser("other@gmail.com", isGuest = false))
        assertFalse(AdminConfig.isAdminUser("", isGuest = false))
        assertFalse(AdminConfig.isAdminUser(null, isGuest = false))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `wallet, ad reward points, pro unlocks, referrals, wa fb filter, and custom fonts work end to end`() {
        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)
        val store = ViewModelStore()
        try {
            runTest(dispatcher) {
                val testDataStore = PreferenceDataStoreFactory.create(
                    scope = backgroundScope,
                    produceFile = { tempFolder.newFile("vedographi_feature_prefs.preferences_pb") }
                )
                val vmFactory = viewModelFactory {
                    initializer {
                        VedographiViewModel(
                            repo = PreferencesRepository(testDataStore),
                            userEmail = "user@gmail.com"
                        )
                    }
                }
                val vm = ViewModelProvider(store, vmFactory)[VedographiViewModel::class.java]
                val jobs = listOf(
                    backgroundScope.launch(dispatcher) { vm.filteredFontItems.collect { } },
                    backgroundScope.launch(dispatcher) { vm.customFonts.collect { } },
                    backgroundScope.launch(dispatcher) { vm.myReferralCode.collect { } },
                    backgroundScope.launch(dispatcher) { vm.redeemedReferralCode.collect { } },
                    backgroundScope.launch(dispatcher) { vm.adsWatchedCount.collect { } }
                )
                advanceUntilIdle()

                assertFalse(vm.isAdmin)

                // 1. WA & FB Safe filter hides combining underline styles
                vm.selectCategory(FontCategory.BOLD)
                advanceUntilIdle()
                assertTrue(vm.filteredFontItems.value.any { it.spec.id == "bold_underline" })
                vm.toggleWaFbSafeFilter()
                advanceUntilIdle()
                assertFalse(vm.filteredFontItems.value.any { it.spec.id == "bold_underline" })
                assertTrue(vm.filteredFontItems.value.all { it.spec.isWaFbSafe })
                vm.setWaFbSafeOnly(false)
                advanceUntilIdle()

                // 2. Pro font lock vs unlock with earned ad + referral points
                val proFont = FontCatalog.allStyles.first { it.isPro && it.proCost == 5 }
                var unlockResult: UnlockProResult? = null
                vm.unlockProFont(proFont) { unlockResult = it }
                advanceUntilIdle()
                assertTrue(unlockResult is UnlockProResult.InsufficientPoints)

                // Redeem friend's referral code (+2 points)
                var referralResult: Result<Int>? = null
                vm.redeemReferralCode("FRIEND22") { referralResult = it }
                advanceUntilIdle()
                assertEquals(2, referralResult?.getOrNull())
                assertEquals(2, vm.pointsBalance.value)

                // Double claim blocked
                var secondClaim: Result<Int>? = null
                vm.redeemReferralCode("OTHER999") { secondClaim = it }
                advanceUntilIdle()
                assertTrue(secondClaim?.isFailure == true)

                // Watch rewarded ad (+5 points -> 7 points total)
                vm.grantAdRewardPoints(amount = 5)
                advanceUntilIdle()
                assertEquals(7, vm.pointsBalance.value)
                assertEquals(1, vm.adsWatchedCount.value)

                // Unlock Pro font (costs 5 -> 2 points left)
                vm.unlockProFont(proFont) { unlockResult = it }
                advanceUntilIdle()
                assertTrue(unlockResult is UnlockProResult.Unlocked)
                assertEquals(2, vm.pointsBalance.value)
                assertTrue(proFont.id in vm.unlockedProFontIds.value)

                // 3. Custom Style Studio save & delete in MY_CUSTOM category
                vm.updateCustomDraftName("Kaneriya Neon")
                vm.setCustomCharGlyph('V', "Ѷ")
                vm.saveCustomFontDefinition()
                advanceUntilIdle()

                vm.selectCategory(FontCategory.MY_CUSTOM)
                advanceUntilIdle()
                val customEntry = vm.filteredFontItems.value.firstOrNull { it.spec.name == "Kaneriya Neon" }
                assertNotNull(customEntry)

                vm.deleteCustomFontDefinition(customEntry!!.spec.id)
                advanceUntilIdle()
                assertTrue(vm.filteredFontItems.value.none { it.spec.id == customEntry.spec.id })

                jobs.forEach { it.cancel() }
            }
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
