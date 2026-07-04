package com.huiyi.v4.floating

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.huiyi.v4.accessibility.HuiyiAccessibilityService
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineResult
import com.huiyi.v4.runtime.HuiyiRuntime
import com.huiyi.v4.runtime.HuiyiRuntimeState

class FloatingResultPanelController(
    private val context: Context,
    private val runtime: HuiyiRuntime
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var panelView: ScrollView? = null

    fun showLoading() {
        Toast.makeText(context, "会意正在看当前聊天…", Toast.LENGTH_SHORT).show()
    }

    fun show(state: HuiyiRuntimeState) {
        hide()
        val result = state.latestPipelineResult
        if (state.lastError != null) {
            showSimplePanel(
                title = "这次没跑完",
                body = state.lastError.ifBlank { "没读到聊天页，请点一下聊天窗口后再试。" }
            )
            return
        }

        val decision = result?.tacticalDecision ?: state.demoState.decision
        val routes = result?.routes ?: state.demoState.routes
        val container = panelContainer()

        when {
            decision.decisionType == TacticalDecisionType.PRE_ANALYSIS_CONTAMINATED ||
                decision.decisionType == TacticalDecisionType.CHAT_WINDOW_NOT_FOUND -> {
                container.addView(titleText("没读到聊天页"))
                container.addView(text("没读到聊天页，请点一下聊天窗口后再试。"))
            }
            decision.decisionType == TacticalDecisionType.WAIT -> {
                container.addView(titleText("先等对方"))
                container.addView(text("你已经回过了，先等对方。"))
            }
            else -> {
                container.addView(titleText("推荐回复"))
                cloudStatusLine(result)?.let { container.addView(smallText(it)) }
                if (routes.isEmpty()) {
                    container.addView(text("这次还没拿到可用回复，请点一下聊天窗口后再试。"))
                } else {
                    addReplyChoices(container, routes)
                }
            }
        }

        addFooterButtons(container)
        attach(container, panelType = decision.decisionType.name)
        runtime.markOverlayPanelShown()
    }

    fun hide() {
        panelView?.let {
            runCatching { windowManager.removeView(it) }.onFailure { error ->
                OverlayStateStore.recordWindowManagerException(
                    error = error,
                    operation = "removeView",
                    windowType = 0,
                    overlayPermissionState = Settings.canDrawOverlays(context),
                    currentForegroundPackage = HuiyiAccessibilityService.state.value.currentPackage,
                    targetPackage = context.packageName
                )
            }
        }
        panelView = null
        OverlayStateStore.markPanelDismissed("panel_hide")
    }

    private fun showSimplePanel(title: String, body: String) {
        hide()
        val container = panelContainer()
        container.addView(titleText(title))
        container.addView(text(body))
        addFooterButtons(container)
        attach(container, panelType = "controlled_fail")
    }

    private fun addReplyChoices(container: LinearLayout, routes: List<ReplyRoute>) {
        routes.take(5).forEachIndexed { index, route ->
            val label = strategyLabel(route, index)
            container.addView(routeTitle(label))
            container.addView(text(route.message))
            container.addView(Button(context).apply {
                text = "复制"
                setOnClickListener {
                    copy(route.message)
                    runtime.createCopiedAttempt(route)
                    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun strategyLabel(route: ReplyRoute, index: Int): String {
        val prefix = if (index == 0 || route.recommended) "推荐" else "备选 ${index + 1}"
        return "$prefix - ${strategyDirection(route, index)}"
    }

    private fun strategyDirection(route: ReplyRoute, index: Int): String {
        val text = listOf(
            route.name,
            route.expectedEffect.orEmpty(),
            route.fallbackMove.orEmpty(),
            route.message
        ).joinToString(" ")
        return when {
            route.riskLevel == RiskLevel.HIGH -> "高风险推进"
            route.routeType == ReplyRouteType.ARC_REVEAL -> "人物弧光"
            text.hasAny("ARC_REVEAL", "人物弧光", "底色反差", "真实底色") -> "人物弧光"
            text.hasAny("接情绪", "情绪", "共情", "empathy") -> "接情绪"
            text.hasAny("升温", "暧昧", "拉近", "推进关系", "warm", "flirt") -> "升温"
            text.hasAny("轻松", "生活", "日常", "daily", "light") -> "轻松接话"
            text.hasAny("轻问", "问一句", "问她", "问他", "question") -> "轻问一句"
            text.hasAny("共同", "共创", "一起", "约", "推进", "co_creation") -> "共同推进"
            text.hasAny("修复", "道歉", "缓和", "repair") -> "修复关系"
            text.hasAny("撤退", "降压", "不追", "等", "withdraw", "fallback") -> "降压撤退"
            route.riskLevel == RiskLevel.MEDIUM && route.routeType == ReplyRouteType.CO_CREATION -> "升温推进"
            route.routeType == ReplyRouteType.EMPATHY -> "接情绪"
            route.routeType == ReplyRouteType.WARM_UP -> "升温"
            route.routeType == ReplyRouteType.CO_CREATION -> "共同推进"
            route.routeType == ReplyRouteType.REPAIR -> "修复关系"
            route.routeType == ReplyRouteType.COOL_DOWN -> "降压撤退"
            route.routeType == ReplyRouteType.DIRECT -> "轻问一句"
            else -> index.defaultStrategyDirection()
        }
    }

    private fun Int.defaultStrategyDirection(): String = when (this) {
        0 -> "接情绪"
        1 -> "轻松接话"
        2 -> "轻问一句"
        3 -> "升温"
        4 -> "降压撤退"
        else -> "备选思路"
    }

    private fun String.hasAny(vararg keywords: String): Boolean {
        return keywords.any { contains(it, ignoreCase = true) }
    }

    private fun addFooterButtons(container: LinearLayout) {
        container.addView(Button(context).apply {
            text = "这次不对，发给 GPT"
            setOnClickListener { runtime.exportOneTapFeedback() }
        })
        container.addView(Button(context).apply {
            text = "隐藏"
            setOnClickListener { hide() }
        })
    }

    private fun panelContainer(): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(22, 18, 22, 18)
        setBackgroundColor(0xF2FFFFFF.toInt())
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
    }

    private fun attach(container: LinearLayout, panelType: String) {
        val scroll = ScrollView(context).apply {
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            addView(container)
        }
        val params = WindowManager.LayoutParams(
            (context.resources.displayMetrics.widthPixels * 0.92f).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 36
        }
        try {
            windowManager.addView(scroll, params)
            panelView = scroll
            OverlayStateStore.markAddViewSuccess()
            OverlayStateStore.markPanelShown(panelType)
        } catch (error: Throwable) {
            OverlayStateStore.recordWindowManagerException(
                error = error,
                operation = "addView",
                windowType = params.type,
                overlayPermissionState = Settings.canDrawOverlays(context),
                currentForegroundPackage = HuiyiAccessibilityService.state.value.currentPackage,
                targetPackage = context.packageName
            )
        }
    }

    private fun cloudStatusLine(result: CurrentScreenPipelineResult?): String? {
        val cloud = result?.cloudTrace ?: return null
        if (cloud.cloudErrorCode == "NETWORK") return "云端连接不稳，先给你本地备选。"
        return when {
            cloud.decisionSource == "CLOUD" -> null
            cloud.cloudFallbackUsed -> "云端暂不可用，先给你本地备选。"
            cloud.cloudSkippedReason == "CLOUD_NOT_CONFIGURED" ||
                cloud.cloudSkippedReason == "RELAY_API_KEY_MISSING" ||
                cloud.cloudSkippedReason == "RELAY_API_KEY_INSECURE_STORAGE" -> "云端未就绪，先给你本地备选。"
            else -> null
        }
    }

    private fun titleText(value: String): TextView = text(value).apply {
        textSize = 18f
        setTextColor(0xFF0F172A.toInt())
    }

    private fun routeTitle(value: String): TextView = text(value).apply {
        textSize = 16f
        setTextColor(0xFF111827.toInt())
    }

    private fun smallText(value: String): TextView = text(value).apply {
        textSize = 13f
        setTextColor(0xFF475569.toInt())
    }

    private fun text(value: String): TextView = TextView(context).apply {
        text = value
        textSize = 15f
        setTextColor(0xFF111827.toInt())
        setPadding(0, 6, 0, 6)
    }

    private fun copy(value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("会意回复", value))
    }
}
