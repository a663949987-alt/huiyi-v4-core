package com.huiyi.v4.floating

import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class OverlayRuntimeState(
    val bubbleVisible: Boolean = false,
    val resultPanelVisible: Boolean = false,
    val errorPanelVisible: Boolean = false,
    val lastPanelType: String? = null,
    val lastBubbleClickAt: Long? = null,
    val lastPanelShownAt: Long? = null,
    val lastPanelDismissedAt: Long? = null,
    val lastOverlayError: String? = null,
    val lastWindowManagerException: String? = null,
    val lastWindowManagerStackTrace: String? = null,
    val addViewSuccess: Boolean = false,
    val removeViewReason: String? = null,
    val floatingServiceRunning: Boolean = false,
    val lastPipelineException: String? = null,
    val serviceStoppedByUser: Boolean = false
)

object OverlayStateStore {
    private val mutableState = MutableStateFlow(OverlayRuntimeState())
    val state: StateFlow<OverlayRuntimeState> = mutableState

    fun markFloatingServiceRunning(running: Boolean) {
        mutableState.value = mutableState.value.copy(floatingServiceRunning = running)
    }

    fun markBubbleVisible(visible: Boolean) {
        mutableState.value = mutableState.value.copy(bubbleVisible = visible)
    }

    fun markBubbleClick() {
        mutableState.value = mutableState.value.copy(lastBubbleClickAt = System.currentTimeMillis())
    }

    fun markPanelShown(type: String) {
        mutableState.value = mutableState.value.copy(
            resultPanelVisible = type != "error",
            errorPanelVisible = type == "error",
            lastPanelType = type,
            lastPanelShownAt = System.currentTimeMillis()
        )
    }

    fun markPanelDismissed(reason: String) {
        mutableState.value = mutableState.value.copy(
            resultPanelVisible = false,
            errorPanelVisible = false,
            lastPanelDismissedAt = System.currentTimeMillis(),
            removeViewReason = reason
        )
    }

    fun markUserHide() {
        mutableState.value = mutableState.value.copy(
            bubbleVisible = false,
            serviceStoppedByUser = true,
            removeViewReason = "user_hide"
        )
    }

    fun recordOverlayError(message: String) {
        mutableState.value = mutableState.value.copy(lastOverlayError = message)
    }

    fun recordPipelineException(error: Throwable) {
        mutableState.value = mutableState.value.copy(
            lastPipelineException = "${error::class.java.name}: ${error.message}",
            lastOverlayError = error.message
        )
    }

    fun recordWindowManagerException(
        error: Throwable,
        operation: String,
        windowType: Int,
        overlayPermissionState: Boolean,
        currentForegroundPackage: String?,
        targetPackage: String?
    ) {
        mutableState.value = mutableState.value.copy(
            addViewSuccess = if (operation == "addView") false else mutableState.value.addViewSuccess,
            lastWindowManagerException = buildString {
                append("${error::class.java.name}: ${error.message}")
                append("; operation=$operation")
                append("; windowType=$windowType")
                append("; overlayPermissionState=$overlayPermissionState")
                append("; currentForegroundPackage=${currentForegroundPackage ?: "unknown"}")
                append("; targetPackage=${targetPackage ?: "unknown"}")
            },
            lastWindowManagerStackTrace = error.stackTraceToString(),
            lastOverlayError = error.message
        )
    }

    fun markAddViewSuccess() {
        mutableState.value = mutableState.value.copy(addViewSuccess = true, lastWindowManagerException = null)
    }

    fun resetForTest() {
        mutableState.value = OverlayRuntimeState()
    }
}
