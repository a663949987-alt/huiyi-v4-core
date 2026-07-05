package com.huiyi.v4.floating

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.huiyi.v4.runtime.HuiyiRuntime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FloatingBubbleService : Service() {
    private var controller: FloatingBubbleController? = null
    private var resultPanelController: FloatingResultPanelController? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        OverlayStateStore.markFloatingServiceRunning(true)
        val runtime = HuiyiRuntime.get(this)
        resultPanelController = FloatingResultPanelController(this, runtime)
        controller = FloatingBubbleController(
            context = this,
            onNextSentence = { clickAck ->
                runCatching {
                    scope.launch {
                        try {
                            Log.i(LOG_TAG, "next_sentence_click_ack latencyMs=${clickAck.clickAckLatencyMs} ackVisible=${clickAck.clickAckVisible}")
                            resultPanelController?.hide()
                            delay(if (clickAck.panelVisibleBeforeClick) 300L else 40L)
                            val sessionId = runtime.runNextSentence(clickAck)
                            delay(700L)
                            val state = runtime.state.value
                            if (state.lastNextSentenceTrace?.sessionId == sessionId &&
                                state.latestPipelineResult == null &&
                                state.lastError == null
                            ) {
                                resultPanelController?.showLoading()
                            }
                        } catch (error: Throwable) {
                            Log.e(LOG_TAG, "next_sentence_click_failed", error)
                            OverlayStateStore.recordPipelineException(error)
                            runtime.showOverlayError(error)
                            controller?.markIdle()
                        }
                    }
                }.onFailure { error ->
                    OverlayStateStore.recordPipelineException(error)
                    runtime.showOverlayError(error)
                }
            },
            onExpressSelf = { clickAck ->
                runCatching {
                    scope.launch {
                        try {
                            Log.i(LOG_TAG, "express_self_click_ack latencyMs=${clickAck.clickAckLatencyMs} ackVisible=${clickAck.clickAckVisible}")
                            resultPanelController?.hide()
                            delay(if (clickAck.panelVisibleBeforeClick) 240L else 40L)
                            val sessionId = runtime.runExpressSelf(clickAck)
                            delay(700L)
                            val state = runtime.state.value
                            if (state.lastNextSentenceTrace?.sessionId == sessionId &&
                                state.latestPipelineResult == null &&
                                state.lastError == null
                            ) {
                                resultPanelController?.showLoading()
                            }
                        } catch (error: Throwable) {
                            Log.e(LOG_TAG, "express_self_click_failed", error)
                            OverlayStateStore.recordPipelineException(error)
                            runtime.showOverlayError(error)
                            controller?.markIdle()
                        }
                    }
                }.onFailure { error ->
                    OverlayStateStore.recordPipelineException(error)
                    runtime.showOverlayError(error)
                }
            },
            onFeedback = {
                runCatching {
                    scope.launch {
                        try {
                            Log.i(LOG_TAG, "one_tap_feedback_from_floating_menu")
                            runtime.exportOneTapFeedback()
                            Toast.makeText(this@FloatingBubbleService, "正在生成反馈包", Toast.LENGTH_SHORT).show()
                        } catch (error: Throwable) {
                            Log.e(LOG_TAG, "one_tap_feedback_click_failed", error)
                            OverlayStateStore.recordPipelineException(error)
                            runtime.showOverlayError(error)
                        }
                    }
                }.onFailure { error ->
                    OverlayStateStore.recordPipelineException(error)
                    runtime.showOverlayError(error)
                }
            }
        )
        controller?.show()
        scope.launch {
            runtime.state.collectLatest { state ->
                if (state.panelVisible && (state.latestPipelineResult != null || state.lastError != null)) {
                    resultPanelController?.show(state)
                    controller?.markIdle()
                    runtime.setPanelVisible(false)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        OverlayStateStore.markFloatingServiceRunning(true)
        if (intent?.getBooleanExtra(EXTRA_RESET_PANEL, false) == true) {
            resultPanelController?.hide()
        }
        controller?.show()
        return START_STICKY
    }

    override fun onDestroy() {
        OverlayStateStore.markFloatingServiceRunning(false)
        controller?.hide("service_destroy")
        resultPanelController?.hide()
        controller = null
        resultPanelController = null
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private companion object {
        const val LOG_TAG = "FloatingBubbleService"
        const val EXTRA_RESET_PANEL = "resetPanel"
    }
}
