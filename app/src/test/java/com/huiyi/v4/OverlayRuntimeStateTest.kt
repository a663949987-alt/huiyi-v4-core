package com.huiyi.v4

import com.huiyi.v4.floating.OverlayStateStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayRuntimeStateTest {
    @Test
    fun nextClickExceptionKeepsBubbleVisibleAndShowsErrorPanelState() {
        OverlayStateStore.resetForTest()
        OverlayStateStore.markBubbleVisible(true)
        OverlayStateStore.recordPipelineException(IllegalStateException("boom"))
        OverlayStateStore.markPanelShown("error")

        val state = OverlayStateStore.state.value
        assertTrue(state.bubbleVisible)
        assertTrue(state.errorPanelVisible)
        assertTrue(state.lastPipelineException?.contains("boom") == true)
    }

    @Test
    fun windowManagerExceptionIsLogged() {
        OverlayStateStore.resetForTest()
        OverlayStateStore.recordWindowManagerException(
            error = IllegalArgumentException("bad token"),
            operation = "addView",
            windowType = 2038,
            overlayPermissionState = true,
            currentForegroundPackage = "com.chat.real",
            targetPackage = "com.huiyi.v4"
        )

        assertNotNull(OverlayStateStore.state.value.lastWindowManagerException)
        assertTrue(OverlayStateStore.state.value.lastWindowManagerException!!.contains("bad token"))
    }

    @Test
    fun overlayHiddenOnlyByUserMarksUserHideReason() {
        OverlayStateStore.resetForTest()
        OverlayStateStore.markBubbleVisible(true)
        OverlayStateStore.markPanelDismissed("analysis_error")

        assertTrue(OverlayStateStore.state.value.bubbleVisible)
        assertEquals("analysis_error", OverlayStateStore.state.value.removeViewReason)

        OverlayStateStore.markUserHide()
        assertEquals("user_hide", OverlayStateStore.state.value.removeViewReason)
    }
}
