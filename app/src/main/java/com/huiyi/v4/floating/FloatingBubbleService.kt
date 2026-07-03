package com.huiyi.v4.floating

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.huiyi.v4.runtime.HuiyiRuntime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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
            onNextSentence = {
                runCatching {
                    resultPanelController?.showLoading()
                    runtime.runNextSentence()
                }.onFailure { error ->
                    OverlayStateStore.recordPipelineException(error)
                    runtime.showOverlayError(error)
                }
            },
            onOneTapFeedback = {
                runtime.exportOneTapFeedback()
            }
        )
        controller?.show()
        scope.launch {
            runtime.state.collectLatest { state ->
                if (state.panelVisible && (state.latestPipelineResult != null || state.lastError != null)) {
                    resultPanelController?.show(state)
                    runtime.setPanelVisible(false)
                }
            }
        }
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
}
