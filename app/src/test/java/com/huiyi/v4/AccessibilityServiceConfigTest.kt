package com.huiyi.v4

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AccessibilityServiceConfigTest {
    @Test
    fun accessibilityServiceDoesNotDeclareScreenshotCapability() {
        val xml = File("src/main/res/xml/huiyi_accessibility_service.xml").readText()

        assertTrue(xml.contains("android:canRetrieveWindowContent=\"true\""))
        assertFalse(xml.contains("android:canTakeScreenshot=\"true\""))
    }
}
