package com.huiyi.v4

import com.huiyi.v4.accessibility.HuiyiAccessibilityState
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.pipeline.RealDeviceReviewBundleGenerator
import com.huiyi.v4.domain.pipeline.SampleSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RealDeviceReviewBundleGeneratorTest {
    private val generator = RealDeviceReviewBundleGenerator()

    @Test
    fun noRealDeviceSmokeExportsPartialNotTested() {
        val mockResult = evidenceResult(
            appPackage = "com.huiyi.mockchat",
            source = SampleSource.EMULATOR_MOCK_CHAT_ACCESSIBILITY,
            messages = listOf(textNode("1", Speaker.OTHER, "hello", 1))
        )

        val bundle = generator.build(
            latestResult = mockResult,
            accessibilityState = HuiyiAccessibilityState(serviceConnected = true, rootAvailable = true),
            generatedAt = 1,
            versionName = "4.1.4",
            versionCode = 414,
            ownAppPackage = "com.huiyi.v4"
        )

        assertEquals("NOT_TESTED", bundle.realDeviceSmokeResult)
        assertEquals("PARTIAL", bundle.overallResult)
        assertTrue(bundle.reviewMarkdown.contains("real_device_smoke_result: NOT_TESTED"))
        assertTrue(bundle.reviewMarkdown.contains("overall_result: PARTIAL"))
        assertTrue(bundle.reviewMarkdown.contains("不代表真实聊天 App 已通过"))
        assertTrue(bundle.currentScreenMarkdown.contains("realDeviceSmoke: NOT_TESTED"))
    }

    @Test
    fun realDeviceBundleIncludesMessagesAndRoutes() {
        val result = evidenceResult(
            appPackage = "com.chat.real",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(
                textNode("1", Speaker.ME, "hello", 1),
                textNode("2", Speaker.OTHER, "are you there", 2)
            )
        )

        val bundle = generator.build(
            latestResult = result.copy(
                overlayShownInTargetApp = true,
                foregroundPackageWhenPanelShown = "com.chat.real",
                huiyiActivityOpened = false,
                userStayedInChatApp = true,
                resultShownAsOverlay = true,
                mainActivityOpened = false
            ),
            accessibilityState = HuiyiAccessibilityState(serviceConnected = true, rootAvailable = true),
            generatedAt = 1,
            versionName = "4.1.4",
            versionCode = 414,
            ownAppPackage = "com.huiyi.v4"
        )

        assertEquals("PASS", bundle.realDeviceSmokeResult)
        assertEquals("PASS", bundle.overallResult)
        assertTrue(bundle.currentScreenJson.contains("\"parsedMessages\""))
        assertTrue(bundle.currentScreenJson.contains("\"ReplyRoutes\""))
        assertTrue(bundle.currentScreenMarkdown.contains("sample_source: real_device_accessibility"))
        assertTrue(bundle.smokeMarkdown.contains("overlayShownInTargetApp: true"))
    }

    @Test
    fun realDeviceOtherLastWithoutRoutesFailsSmokeValidation() {
        val result = evidenceResult(
            appPackage = "com.chat.real",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(
                textNode("1", Speaker.ME, "hello", 1),
                textNode("2", Speaker.OTHER, "are you there", 2)
            ),
            includeRoutes = false
        ).copy(
            overlayShownInTargetApp = true,
            foregroundPackageWhenPanelShown = "com.chat.real",
            huiyiActivityOpened = false,
            userStayedInChatApp = true,
            resultShownAsOverlay = true,
            mainActivityOpened = false
        )

        val bundle = generator.build(
            latestResult = result,
            accessibilityState = HuiyiAccessibilityState(serviceConnected = true, rootAvailable = true),
            generatedAt = 1,
            versionName = "4.1.7",
            versionCode = 417,
            ownAppPackage = "com.huiyi.v4"
        )

        assertEquals("FAIL", bundle.realDeviceSmokeResult)
        assertEquals("FAIL", bundle.overallResult)
        assertTrue(bundle.failReason.contains("last speaker OTHER must generate 5 routes"))
        assertTrue(bundle.reviewMarkdown.contains("real_device_smoke_result: FAIL"))
        assertTrue(bundle.smokeMarkdown.contains("validationResult: FAIL"))
    }

    @Test
    fun realDeviceMeLastWaitPassesSmokeValidation() {
        val result = evidenceResult(
            appPackage = "com.chat.real",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(
                textNode("1", Speaker.OTHER, "are you there", 1),
                textNode("2", Speaker.ME, "yes", 2)
            ),
            includeRoutes = false
        ).copy(
            overlayShownInTargetApp = true,
            foregroundPackageWhenPanelShown = "com.chat.real",
            huiyiActivityOpened = false,
            userStayedInChatApp = true,
            resultShownAsOverlay = true,
            mainActivityOpened = false
        )

        val bundle = generator.build(
            latestResult = result,
            accessibilityState = HuiyiAccessibilityState(serviceConnected = true, rootAvailable = true),
            generatedAt = 1,
            versionName = "4.1.7",
            versionCode = 417,
            ownAppPackage = "com.huiyi.v4"
        )

        assertEquals("PASS", bundle.realDeviceSmokeResult)
        assertEquals("PASS", bundle.overallResult)
    }
}
