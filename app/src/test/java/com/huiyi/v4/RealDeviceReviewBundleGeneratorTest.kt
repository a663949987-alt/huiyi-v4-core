package com.huiyi.v4

import com.huiyi.v4.accessibility.HuiyiAccessibilityState
import com.huiyi.v4.domain.capture.VisualDebugResult
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.pipeline.RealDeviceReviewBundleGenerator
import com.huiyi.v4.domain.pipeline.RealDeviceScenario
import com.huiyi.v4.domain.pipeline.RealDeviceScenarioValidator
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
            ownAppPackage = "com.huiyi.v4",
            scenario = RealDeviceScenario.LAST_OTHER
        )

        assertEquals("NOT_TESTED", bundle.realDeviceSmokeResult)
        assertEquals("NOT_TESTED", bundle.overallResult)
        assertTrue(bundle.reviewMarkdown.contains("real_device_smoke_result: NOT_TESTED"))
        assertTrue(bundle.reviewMarkdown.contains("overall_result: NOT_TESTED"))
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
            ownAppPackage = "com.huiyi.v4",
            scenario = RealDeviceScenario.LAST_OTHER
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
            ownAppPackage = "com.huiyi.v4",
            scenario = RealDeviceScenario.LAST_OTHER
        )

        assertEquals("FAIL", bundle.realDeviceSmokeResult)
        assertEquals("FAIL", bundle.overallResult)
        assertTrue(bundle.failReason.contains("route_count_mismatch"))
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

    @Test
    fun scenarioLastMeButActualOtherIsScenarioDefinitionMismatchNotProductFailure() {
        val result = evidenceResult(
            appPackage = "com.bajiao.im.liaoqi",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(
                textNode("1", Speaker.ME, "我先处理一下", 1),
                textNode("2", Speaker.OTHER, "这个事情也需要考虑好规划好才行。", 2)
            )
        ).copy(
            overlayShownInTargetApp = true,
            foregroundPackageWhenPanelShown = "com.bajiao.im.liaoqi",
            huiyiActivityOpened = false,
            userStayedInChatApp = true,
            resultShownAsOverlay = true,
            mainActivityOpened = false
        )

        val validation = RealDeviceScenarioValidator.validate(result, RealDeviceScenario.LAST_ME)
        val bundle = generator.build(
            latestResult = result,
            accessibilityState = HuiyiAccessibilityState(serviceConnected = true, rootAvailable = true),
            generatedAt = 1,
            versionName = "4.1.10",
            versionCode = 426,
            ownAppPackage = "com.huiyi.v4",
            scenario = RealDeviceScenario.LAST_ME
        )

        assertEquals("PASS", validation.realDeviceFunctionalSmoke)
        assertEquals("MISMATCH", validation.scenarioAssertionResult)
        assertEquals(false, validation.scenarioDefinitionTrusted)
        assertEquals("SCENARIO_DEFINITION_MISMATCH", validation.scenarioFailureCategory)
        assertEquals("CONTROLLED_PASS_WITH_SCENARIO_MISMATCH", validation.currentOverallResult)
        assertEquals("PASS", bundle.realDeviceSmokeResult)
        assertEquals("CONTROLLED_PASS_WITH_SCENARIO_MISMATCH", bundle.overallResult)
        assertTrue(bundle.reviewMarkdown.contains("scenarioAssertionResult: MISMATCH"))
        assertTrue(bundle.reviewMarkdown.contains("currentOverallResult: CONTROLLED_PASS_WITH_SCENARIO_MISMATCH"))
        assertTrue(bundle.reviewMarkdown.contains("failureCategory: scenario_definition_mismatch"))
    }

    @Test
    fun screenshotUnavailableDoesNotFailFunctionalSmoke() {
        val result = evidenceResult(
            appPackage = "com.bajiao.im.liaoqi",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(
                textNode("1", Speaker.ME, "收到", 1),
                textNode("2", Speaker.OTHER, "那你看看怎么安排。", 2)
            ),
            visualDebugResult = VisualDebugResult(
                screenshotCaptured = false,
                screenshotUnavailable = true,
                reason = "Services don't have the capability of taking the screenshot.",
                screenshotPath = null,
                overlayImagePath = "outputs/visual-debug.png",
                screenshotWidth = 1080,
                screenshotHeight = 2400,
                accessibilityBoundsProjected = true,
                ocrUsed = false,
                visualTruthAvailable = false,
                screenshotErrorCode = "SCREENSHOT_CAPABILITY_MISSING"
            )
        ).copy(
            overlayShownInTargetApp = true,
            foregroundPackageWhenPanelShown = "com.bajiao.im.liaoqi",
            huiyiActivityOpened = false,
            userStayedInChatApp = true,
            resultShownAsOverlay = true,
            mainActivityOpened = false
        )

        val validation = RealDeviceScenarioValidator.validate(result, RealDeviceScenario.AUTO_FROM_SCREEN)

        assertEquals("PASS", validation.realDeviceFunctionalSmoke)
        assertEquals("OPTIONAL_FAILED", validation.screenshotDiagnosticStatus)
        assertEquals(false, validation.screenshotFailureBlocksMainPath)
        assertEquals(false, validation.visualTruthAvailable)
        assertEquals("NONE", validation.visualTruthSource)
        assertEquals("SCREENSHOT_CAPABILITY_MISSING", validation.secondaryDiagnosticErrorCode)
    }

    @Test
    fun postPanelWindowTitleDoesNotDefineScenarioExpectation() {
        val result = evidenceResult(
            appPackage = "com.bajiao.im.liaoqi",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(
                textNode("1", Speaker.ME, "好的", 1),
                textNode("2", Speaker.OTHER, "这个事情也需要考虑好规划好才行。", 2)
            ),
            windowTitle = "会意雷达 判断：普通聊天推进。 打法：接住她当前内容。"
        ).copy(
            overlayShownInTargetApp = true,
            foregroundPackageWhenPanelShown = "com.bajiao.im.liaoqi",
            huiyiActivityOpened = false,
            userStayedInChatApp = true,
            resultShownAsOverlay = true,
            mainActivityOpened = false
        )

        val validation = RealDeviceScenarioValidator.validate(result, RealDeviceScenario.LAST_ME)

        assertEquals(true, validation.reportWindowTitleContaminatedByPanel)
        assertEquals(false, validation.postPanelStateUsedForScenarioExpectation)
        assertEquals("UNKNOWN_CONTAMINATED_BY_POST_PANEL", validation.preAnalysisWindowTitle)
        assertEquals("MISMATCH", validation.scenarioAssertionResult)
        assertEquals("scenario_definition_mismatch", validation.failureReason)
    }
}
