package com.huiyi.v4

import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.pipeline.EvidencePackReportGenerator
import com.huiyi.v4.domain.pipeline.SampleSource
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayDoesNotOpenMainActivityTest {
    @Test
    fun overlayFlagsShowNoMainActivity() {
        val base = evidenceResult(
            appPackage = "com.chat.real",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(
                textNode("1", Speaker.ME, "你刚刚说你离婚都10年了。", 1),
                textNode("2", Speaker.OTHER, "是啊，我离婚是10年了呀。", 2)
            )
        )
        val result = base.copy(
            overlayShownInTargetApp = true,
            foregroundPackageWhenPanelShown = "com.chat.real",
            huiyiActivityOpened = false,
            userStayedInChatApp = true,
            resultShownAsOverlay = true,
            mainActivityOpened = false
        )
        val report = EvidencePackReportGenerator().buildMarkdown(result, com.huiyi.v4.accessibility.HuiyiAccessibilityState(serviceConnected = true, rootAvailable = true))

        assertTrue(report.contains("overlayShownInTargetApp: true"))
        assertTrue(report.contains("foregroundPackageWhenPanelShown: com.chat.real"))
        assertTrue(report.contains("huiyiActivityOpened: false"))
        assertFalse(result.mainActivityOpened)
    }
}
