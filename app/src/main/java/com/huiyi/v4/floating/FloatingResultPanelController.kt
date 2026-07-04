package com.huiyi.v4.floating

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.panel.RoutePanelDisplayText
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineResult
import com.huiyi.v4.domain.persona.CharacterArcUserFeedback
import com.huiyi.v4.runtime.FloatingPanelMode
import com.huiyi.v4.runtime.HuiyiRuntime
import com.huiyi.v4.runtime.HuiyiRuntimeState
import com.huiyi.v4.runtime.NextSentencePendingCloudSessionPolicy

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
        if (state.lastError != null) {
            showSimplePanel(
                title = "没读到聊天页",
                body = state.lastError.ifBlank { "没读到聊天页，请点一下聊天窗口后再试。" }
            )
            return
        }

        val result = state.latestPipelineResult
        val decision = result?.tacticalDecision ?: state.demoState.decision
        val routes = result?.routes ?: state.demoState.routes
        val container = panelContainer()

        if (state.floatingPanelMode == FloatingPanelMode.EXPRESS_SELF) {
            showExpressSelfPanelV2(container, result, routes)
        } else {
            showNextSentencePanel(container, result, decision.decisionType, routes)
        }

        attach(container, panelType = "${state.floatingPanelMode.name}:${decision.decisionType.name}")
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

    private fun showNextSentencePanel(
        container: LinearLayout,
        result: CurrentScreenPipelineResult?,
        decisionType: TacticalDecisionType,
        routes: List<ReplyRoute>
    ) {
        when (decisionType) {
            TacticalDecisionType.PRE_ANALYSIS_CONTAMINATED,
            TacticalDecisionType.CHAT_WINDOW_NOT_FOUND -> {
                container.addView(titleText("没读到聊天页"))
                container.addView(text("没读到聊天页，请点一下聊天窗口后再试。"))
                addHideFooter(container)
            }
            TacticalDecisionType.WAIT -> {
                container.addView(titleText("先等对方"))
                container.addView(text("你已经回过了，先等对方。"))
                addNextSentenceFooter(container)
            }
            else -> {
                val waitingForCloud = result?.cloudTrace?.cloudErrorCode == NextSentencePendingCloudSessionPolicy.SOFT_TIMEOUT_PENDING
                container.addView(titleText(FloatingPanelSplitPolicy.titleForNextSentence(result?.cloudTrace)))
                cloudStatusLine(result)?.let { container.addView(smallText(it)) }
                if (routes.isEmpty()) {
                    if (waitingForCloud) {
                        container.addView(text("本地建议正在准备，云端回来会自动刷新。"))
                    } else {
                        container.addView(text("这次还没拿到可用回复，请点一下聊天窗口后再试。"))
                    }
                } else {
                    addReplyChoices(
                        container = container,
                        routes = routes,
                        mode = FloatingPanelMode.NEXT_SENTENCE
                    )
                    container.addView(smallText("这轮想表达自己？点表达我。"))
                }
                addNextSentenceFooter(container)
            }
        }
    }

    private fun showExpressSelfPanelV2(
        container: LinearLayout,
        result: CurrentScreenPipelineResult?,
        routes: List<ReplyRoute>
    ) {
        container.addView(titleText("表达我"))
        RoutePanelDisplayText.expressSelfSummaryLines(
            arcProgressState = result?.expressSelfArcProgressState,
            routes = routes
        ).forEach { line ->
            container.addView(smallText(line))
        }
        cloudStatusLine(result)?.let { container.addView(smallText(it)) }
        if (routes.isEmpty()) {
            container.addView(text("这轮还没找到适合露出的底色，先低压力接住对方。"))
        } else {
            addReplyChoices(
                container = container,
                routes = routes,
                mode = FloatingPanelMode.EXPRESS_SELF
            )
        }
        addExpressSelfFooter(container)
    }

    private fun showExpressSelfPanel(
        container: LinearLayout,
        result: CurrentScreenPipelineResult?,
        routes: List<ReplyRoute>
    ) {
        val summaryLines = RoutePanelDisplayText.expressSelfSummaryLines(
            arcProgressState = result?.expressSelfArcProgressState,
            routes = routes
        )
        summaryLines.forEach { line -> container.addView(smallText(line)) }
        container.addView(titleText("表达我"))
        container.addView(smallText("本轮动作：表达我 / 共创 / 让她看见你"))
        RoutePanelDisplayText.topActionLine(routes)?.let { container.addView(smallText(it)) }
        cloudStatusLine(result)?.let { container.addView(smallText(it)) }
        if (routes.isEmpty()) {
            container.addView(text("这轮还没找到适合露出的底色，先低压力接住对方。"))
        } else {
            addReplyChoices(
                container = container,
                routes = routes,
                mode = FloatingPanelMode.EXPRESS_SELF
            )
        }
        addExpressSelfFooter(container)
    }

    private fun showSimplePanel(title: String, body: String) {
        hide()
        val container = panelContainer()
        container.addView(titleText(title))
        container.addView(text(body))
        addAccessibilitySettingsButtonIfNeeded(container, body)
        addHideFooter(container)
        attach(container, panelType = "controlled_fail")
    }

    private fun addAccessibilitySettingsButtonIfNeeded(container: LinearLayout, body: String) {
        if (!body.contains("无障碍")) return
        container.addView(Button(context).apply {
            text = "打开无障碍设置"
            setOnClickListener {
                runCatching {
                    context.startActivity(
                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
        })
    }

    private fun addReplyChoices(
        container: LinearLayout,
        routes: List<ReplyRoute>,
        mode: FloatingPanelMode
    ) {
        val showCharacterArcDetails = FloatingPanelSplitPolicy.showsCharacterArcDetails(mode)
        val showPersonaFeedback = FloatingPanelSplitPolicy.showsPersonaFeedback(mode)
        routes.take(5).forEachIndexed { index, route ->
            val label = if (mode == FloatingPanelMode.NEXT_SENTENCE) {
                passiveRouteHeader(route, index)
            } else {
                RoutePanelDisplayText.routeHeader(route, index)
            }
            container.addView(routeTitle(label))
            if (showCharacterArcDetails) {
                RoutePanelDisplayText.detailLines(route).forEach { line ->
                    container.addView(smallText(line))
                }
            }
            container.addView(text(route.message))
            container.addView(Button(context).apply {
                text = "复制"
                setOnClickListener {
                    copy(route.message)
                    runtime.createCopiedAttempt(route)
                    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                }
            })
            if (showPersonaFeedback) {
                addRouteFeedbackButtons(container, route)
            }
        }
    }

    private fun passiveRouteHeader(route: ReplyRoute, index: Int): String {
        val prefix = if (index == 0 || route.recommended) "推荐" else "备选 ${index + 1}"
        val direction = when (route.routeType) {
            ReplyRouteType.EMPATHY -> "接情绪"
            ReplyRouteType.WARM_UP -> "升温"
            ReplyRouteType.CO_CREATION -> "轻问一句"
            ReplyRouteType.COOL_DOWN,
            ReplyRouteType.WAIT -> "降压"
            ReplyRouteType.REPAIR -> "修复"
            ReplyRouteType.DIRECT -> "直接确认"
            ReplyRouteType.ARC_REVEAL,
            ReplyRouteType.SELF_STORY,
            ReplyRouteType.STABLE -> "稳住节奏"
        }
        return "$prefix - $direction"
    }

    private fun addRouteFeedbackButtons(container: LinearLayout, route: ReplyRoute) {
        val feedbacks = listOf(
            "像我" to CharacterArcUserFeedback.LIKE_ME,
            "不像我" to CharacterArcUserFeedback.NOT_LIKE_ME,
            "太油" to CharacterArcUserFeedback.TOO_OILY,
            "太重" to CharacterArcUserFeedback.TOO_HEAVY,
            "太空" to CharacterArcUserFeedback.TOO_EMPTY,
            "可发" to CharacterArcUserFeedback.SENDABLE
        )
        feedbacks.chunked(3).forEach { group ->
            val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            group.forEach { (label, feedback) ->
                row.addView(Button(context).apply {
                    text = label
                    setOnClickListener {
                        runtime.recordCharacterArcRouteFeedback(route, feedback)
                        Toast.makeText(context, "已记住：$label", Toast.LENGTH_SHORT).show()
                    }
                }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            }
            container.addView(row)
        }
    }

    private fun addNextSentenceFooter(container: LinearLayout) {
        val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(Button(context).apply {
            text = "换一批"
            setOnClickListener {
                hide()
                runtime.runNextSentence()
            }
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(Button(context).apply {
            text = "表达我"
            setOnClickListener {
                hide()
                runtime.runExpressSelf()
            }
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(Button(context).apply {
            text = "隐藏"
            setOnClickListener { hide() }
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        container.addView(row)
    }

    private fun addExpressSelfFooter(container: LinearLayout) {
        container.addView(Button(context).apply {
            text = "这次不对，发给 GPT"
            setOnClickListener { runtime.exportOneTapFeedback() }
        })
        addHideFooter(container)
    }

    private fun addHideFooter(container: LinearLayout) {
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
        if (cloud.cloudErrorCode == NextSentencePendingCloudSessionPolicy.SOFT_TIMEOUT_PENDING) {
            return "云端还在后台等，回来会自动刷新。"
        }
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
