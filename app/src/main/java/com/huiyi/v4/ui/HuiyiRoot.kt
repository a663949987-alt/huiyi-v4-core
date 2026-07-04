package com.huiyi.v4.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.huiyi.v4.BuildConfig
import com.huiyi.v4.accessibility.HuiyiAccessibilityService
import com.huiyi.v4.accessibility.AccessibilityRuntimeReader
import com.huiyi.v4.accessibility.accessibilityRuntimeMessage
import com.huiyi.v4.domain.cloud.CloudProviderType
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.panel.RoutePanelDisplayText
import com.huiyi.v4.domain.pipeline.RealDeviceScenario
import com.huiyi.v4.domain.persona.CharacterArcCandidate
import com.huiyi.v4.domain.persona.CharacterArcPreferenceProfile
import com.huiyi.v4.domain.persona.CharacterArcReviewItem
import com.huiyi.v4.domain.persona.CharacterArcUserFeedback
import com.huiyi.v4.floating.FloatingBubbleService
import com.huiyi.v4.runtime.FloatingPanelMode
import com.huiyi.v4.runtime.HuiyiRuntime
import com.huiyi.v4.runtime.HuiyiRuntimeState
import kotlinx.coroutines.delay

private enum class TabPage(val title: String) {
    Home("首页"),
    Review("聊天复盘"),
    Persona("我的底色"),
    Settings("设置"),
    Developer("开发者")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuiyiRoot() {
    val context = LocalContext.current
    val runtime = remember { HuiyiRuntime.get(context) }
    val runtimeState by runtime.state.collectAsState()
    val accessibilityState by HuiyiAccessibilityService.state.collectAsState()
    var accessibilityRefreshTick by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            accessibilityRefreshTick = System.currentTimeMillis()
            delay(1000L)
        }
    }
    val accessibilityRuntime = remember(accessibilityRefreshTick, accessibilityState) {
        AccessibilityRuntimeReader.read(context)
    }
    val accessibilityLabel = accessibilityRuntimeMessage(accessibilityRuntime)
    var accessGranted by remember { mutableStateOf(isAccessPasswordValid(context)) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableStateOf(TabPage.Home) }
    var versionTapCount by remember { mutableIntStateOf(0) }

    if (!accessGranted) {
        PasswordGate(
            value = passwordInput,
            error = passwordError,
            onValueChange = {
                passwordInput = it
                passwordError = null
            },
            onSubmit = {
                if (passwordInput == HUIYI_ACCESS_PASSWORD) {
                    saveAccessPasswordVerified(context)
                    accessGranted = true
                    passwordInput = ""
                    passwordError = null
                } else {
                    passwordError = "密码不对"
                }
            }
        )
        return
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("会意 v4.1") }) },
        bottomBar = {
            NavigationBar {
                listOf(TabPage.Home, TabPage.Review, TabPage.Persona, TabPage.Settings).forEach { page ->
                    NavigationBarItem(
                        selected = tab == page,
                        onClick = { tab = page },
                        label = { Text(page.title) },
                        icon = {}
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (tab) {
                TabPage.Home -> HomePage(
                    state = runtimeState,
                    accessibilityLabel = accessibilityLabel,
                    onOpenPanel = { runtime.setPanelVisible(true) },
                    onRunPipeline = { runtime.runNextSentence() },
                    onPersona = { tab = TabPage.Persona },
                    onSettings = { tab = TabPage.Settings }
                )
                TabPage.Review -> ChatReviewPage()
                TabPage.Persona -> MyPersonaPage(
                    runtime = runtime,
                    state = runtimeState.demoState,
                    preferenceProfile = runtimeState.characterArcPreferenceProfile,
                    onToggle = runtime::togglePersona
                )
                TabPage.Settings -> SettingsPage(
                    accessibilityLabel = accessibilityLabel,
                    overlayLabel = if (Settings.canDrawOverlays(context)) "已授权" else "未授权",
                    lanUpdateUrl = runtimeState.lanUpdateState.updateUrl,
                    lanUpdateStatus = runtimeState.lanUpdateState.status,
                    lanUpdateError = runtimeState.lanUpdateState.error,
                    lanLatestVersion = runtimeState.lanUpdateState.latestManifest?.versionName,
                    versionTapCount = versionTapCount,
                    onOpenAccessibility = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    onOpenOverlay = { context.openOverlaySettings() },
                    onStartBubble = { context.startService(Intent(context, FloatingBubbleService::class.java)) },
                    onStopBubble = { context.stopService(Intent(context, FloatingBubbleService::class.java)) },
                    onLanUpdateUrlChange = runtime::setLanUpdateUrl,
                    onCheckLanUpdate = runtime::checkLanUpdate,
                    onDownloadLanUpdate = runtime::downloadLanUpdate,
                    onOpenUpdateInstaller = runtime::openDownloadedUpdateInstaller,
                    onVersionTap = {
                        versionTapCount += 1
                        if (versionTapCount >= 5) tab = TabPage.Developer
                    }
                )
                TabPage.Developer -> DeveloperSettingsPage(runtime, runtimeState, accessibilityState.toString())
            }
        }
    }

    if (runtimeState.panelVisible) {
        ModalBottomSheet(
            onDismissRequest = { runtime.setPanelVisible(false) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            FloatingTacticalPanel(
                runtime = runtime,
                state = runtimeState,
                onVoiceSummary = runtime::applyVoiceSummary
            )
        }
    }
}

private const val HUIYI_ACCESS_PASSWORD = "6639"
private const val ACCESS_PASSWORD_PREFS = "huiyi-access-password"
private const val ACCESS_PASSWORD_VERIFIED_AT = "verified_at"
private const val ACCESS_PASSWORD_VALID_MS = 5L * 60L * 60L * 1000L

private fun isAccessPasswordValid(context: Context): Boolean {
    val verifiedAt = context.getSharedPreferences(ACCESS_PASSWORD_PREFS, Context.MODE_PRIVATE)
        .getLong(ACCESS_PASSWORD_VERIFIED_AT, 0L)
    if (verifiedAt <= 0L) return false
    return System.currentTimeMillis() - verifiedAt < ACCESS_PASSWORD_VALID_MS
}

private fun saveAccessPasswordVerified(context: Context) {
    context.getSharedPreferences(ACCESS_PASSWORD_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putLong(ACCESS_PASSWORD_VERIFIED_AT, System.currentTimeMillis())
        .apply()
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PasswordGate(
    value: String,
    error: String?,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text("会意 v4") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("输入使用密码")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("密码") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = error != null
            )
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it)
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
                Text("进入")
            }
        }
    }
}

@Composable
private fun HomePage(
    state: HuiyiRuntimeState,
    accessibilityLabel: String,
    onOpenPanel: () -> Unit,
    onRunPipeline: () -> Unit,
    onPersona: () -> Unit,
    onSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("今日状态")
            Spacer(Modifier.height(8.dp))
            StatusCard("当前模式", "手动开挂")
            StatusCard("云端状态", if (state.cloudSettings.cloudEnabled && state.cloudSettings.relayBaseUrlConfigured && state.cloudSettings.relayApiKeyConfigured) "已就绪" else "未配置")
            StatusCard("无障碍状态", accessibilityLabel)
            StatusCard("悬浮球状态", "可在设置中开启")
            StatusCard("我的底色", if (state.demoState.personaEnabled) "已启用" else "已关闭")
            state.lastError?.let { StatusCard("最近提示", it) }
        }
        item {
            Button(onClick = onRunPipeline, modifier = Modifier.fillMaxWidth()) {
                Text("下一句")
            }
        }
        item {
            OutlinedButton(onClick = onOpenPanel, modifier = Modifier.fillMaxWidth()) {
                Text("查看最近判断")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onPersona, modifier = Modifier.weight(1f)) { Text("我的底色") }
                OutlinedButton(onClick = onSettings, modifier = Modifier.weight(1f)) { Text("设置") }
            }
        }
    }
}

@Composable
private fun StatusCard(title: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title)
            Text(value)
        }
    }
}

@Composable
fun FloatingTacticalPanel(
    runtime: HuiyiRuntime,
    state: HuiyiRuntimeState,
    onVoiceSummary: (String) -> Unit
) {
    val decision = state.latestPipelineResult?.tacticalDecision ?: state.demoState.decision
    val routes = state.latestPipelineResult?.routes ?: state.demoState.routes
    val expressSelfMode = state.floatingPanelMode == FloatingPanelMode.EXPRESS_SELF
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (decision.decisionType == TacticalDecisionType.VOICE_SUMMARY_REQUIRED) {
            item { VoiceSummaryCard(state.demoState, onVoiceSummary) }
        }
        if (decision.decisionType == TacticalDecisionType.CONTEXT_REQUIRED) {
            item { ContextRequiredCard(decision.coreInsight) }
        }
        item {
            Text("会意雷达")
            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("雷达判断：${decision.situation}")
                    Text("当前打法：${decision.bestMove}")
                    Text("别做：${decision.avoidMoves.joinToString(" / ")}")
                    decision.influenceProfile.riskWarning?.let { Text("风险提示：$it") }
                    decision.fallbackMove?.let { Text("撤退方案：$it") }
                    state.latestPipelineResult?.lastSpeakerDecision?.reason?.let { Text(it) }
                }
            }
        }
        state.latestPipelineResult?.cloudTrace?.let { cloud ->
            if (decision.decisionType != TacticalDecisionType.WAIT) {
                val label = when {
                    cloud.cloudErrorCode == "NETWORK" -> "云端连接失败，已使用本地建议"
                    cloud.decisionSource == "CLOUD" -> "会意云端分析"
                    cloud.cloudFallbackUsed -> "云端暂不可用，已使用本地建议"
                    cloud.cloudSkippedReason == "CLOUD_NOT_CONFIGURED" -> "本地建议：云端暂未配置。"
                    else -> null
                }
                if (label != null) item { Text(label) }
            }
        }
        if (expressSelfMode) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RoutePanelDisplayText.expressSelfSummaryLines(
                            arcProgressState = state.latestPipelineResult?.expressSelfArcProgressState,
                            routes = routes
                        ).forEach { line ->
                            Text(line)
                        }
                    }
                }
            }
        }
        if (decision.decisionType == TacticalDecisionType.WAIT) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("最后一句是你发的，先等她回，不要继续补话。")
                    }
                }
            }
        } else {
            items(routes) { route ->
                ReplyRouteCard(
                    runtime = runtime,
                    route = route,
                    showCharacterArcDetails = expressSelfMode,
                    showPersonaFeedback = expressSelfMode
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("填入") }
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("补读上一屏") }
            }
        }
    }
}

@Composable
private fun ReplyRouteCard(
    runtime: HuiyiRuntime,
    route: ReplyRoute,
    showCharacterArcDetails: Boolean,
    showPersonaFeedback: Boolean
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(route.tag) })
                AssistChip(onClick = {}, label = { Text(route.intensity.name) })
                if (route.recommended) AssistChip(onClick = {}, label = { Text("推荐") })
                if (route.riskLevel != RiskLevel.LOW) AssistChip(onClick = {}, label = { Text("高风险") })
            }
            val displayName = if (!showCharacterArcDetails &&
                route.routeType == com.huiyi.v4.domain.model.ReplyRouteType.ARC_REVEAL
            ) {
                "稳住节奏"
            } else {
                route.name
            }
            Text(displayName)
            if (showCharacterArcDetails) {
            Text("本轮动作：${route.panelNextAction}")
            route.panelPersonaFacet?.let { Text("这句话展示了你的哪一面：$it") }
            if (route.routeType == com.huiyi.v4.domain.model.ReplyRouteType.ARC_REVEAL) {
                Text("路线标签：${route.panelRouteLabel}")
                Text("不要说过头：${route.riskWarning ?: "不要把轻表达讲成长篇自证。"}")
            }
            }
            Text(route.message)
            route.riskWarning?.let { Text("风险提示：$it") }
            route.fallbackMove?.let { Text("撤退方案：$it") }
            TextButton(
                onClick = {
                    clipboard.setText(AnnotatedString(route.message))
                    runtime.createCopiedAttempt(route)
                    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("复制")
            }
            if (showPersonaFeedback) {
                RouteFeedbackButtons(runtime, route)
            }
        }
    }
}

@Composable
private fun RouteFeedbackButtons(runtime: HuiyiRuntime, route: ReplyRoute) {
    val context = LocalContext.current
    val feedbacks = listOf(
        "像我" to CharacterArcUserFeedback.LIKE_ME,
        "不像我" to CharacterArcUserFeedback.NOT_LIKE_ME,
        "太油" to CharacterArcUserFeedback.TOO_OILY,
        "太重" to CharacterArcUserFeedback.TOO_HEAVY,
        "太空" to CharacterArcUserFeedback.TOO_EMPTY,
        "可发" to CharacterArcUserFeedback.SENDABLE
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        feedbacks.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { (label, feedback) ->
                    AssistChip(
                        onClick = {
                            runtime.recordCharacterArcRouteFeedback(route, feedback)
                            Toast.makeText(context, "已记住：$label", Toast.LENGTH_SHORT).show()
                        },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceSummaryCard(
    state: HuiyiDemoState,
    onVoiceSummary: (String) -> Unit
) {
    var custom by remember { mutableStateOf("") }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("上一条是语音，我还不知道内容。")
            Text("听完后补一句摘要，我再帮你判断怎么回。")
            state.voiceMessages.forEach { message ->
                val voice = message.content as MessageContent.Voice
                Text("${if (message.speaker.name == "OTHER") "对方" else "我的"}语音 ${voice.durationSeconds ?: 0} 秒")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("她在抱怨", "她在解释", "她在撒娇/开玩笑", "她在说正事").forEach { label ->
                        AssistChip(onClick = { custom = label }, label = { Text(label) })
                    }
                }
            }
            OutlinedTextField(
                value = custom,
                onValueChange = { custom = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("手动输入") },
                placeholder = { Text("她说今天店里很忙，有点累。") }
            )
            Button(onClick = { if (custom.isNotBlank()) onVoiceSummary(custom) }, modifier = Modifier.fillMaxWidth()) {
                Text("保存摘要并重新判断")
            }
        }
    }
}

@Composable
private fun ContextRequiredCard(reason: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("上下文还不够")
            Text(reason)
            Text("本轮先不自动滚动，可点击“补读上一屏”占位。")
        }
    }
}

@Composable
private fun ChatReviewPage() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("聊天复盘")
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("下一句的共创点、易错点、雷区和撤退方案会在后台按聊天对象归档。")
                    Text("这个模块后续会按对象展示：特点、爱好、雷区、互动节奏和历史复盘。")
                    Text("当前版本先启用后台记忆种子，不在聊天悬浮窗里打扰用户。")
                }
            }
        }
    }
}

@Composable
private fun MyPersonaPage(
    runtime: HuiyiRuntime,
    state: HuiyiDemoState,
    preferenceProfile: CharacterArcPreferenceProfile,
    onToggle: () -> Unit
) {
    val corpus = state.persona
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("我的底色")
            StatusCard("模板", corpus.name)
            StatusCard("状态", if (corpus.enabled) "已启用" else "已关闭")
            Button(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
                Text(if (corpus.enabled) "关闭" else "启用")
            }
        }
        item {
            SoloReviewLab(runtime, preferenceProfile)
        }
        items(corpus.identityCards) { card ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("身份：${card.title}")
                    Text(card.summary)
                    Text("给人的感觉：${card.values.joinToString(" / ")}")
                    Text("适合表达：${card.bestFor.joinToString(" / ")}")
                    Text("表达风险：${card.risk}")
                }
            }
        }
        items(corpus.storyCards) { story ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(story.title)
                    Text(story.expression)
                    Text("风险：${story.risk}")
                }
            }
        }
    }
}

@Composable
private fun SoloReviewLab(
    runtime: HuiyiRuntime,
    preferenceProfile: CharacterArcPreferenceProfile
) {
    val reviewItems = remember { runtime.soloCharacterArcReviewItems(20) }
    var index by remember { mutableIntStateOf(0) }
    var note by remember { mutableStateOf("") }
    val item = reviewItems.getOrNull(index)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Text("单人弧光盲选")
        Text("第一轮最多 20 题；候选类型已隐藏，只按你的真实感觉点。")
        Text("已记录反馈：${preferenceProfile.feedbackCount}")
        Text("舒适强度：${preferenceProfile.preferredIntensity}")
        if (item == null) {
            Text("本轮盲选已完成。之后真实使用时，顺手点路线旁边的“像我 / 太油 / 太重”就行。")
        } else {
            Text("第 ${index + 1} / ${reviewItems.size} 题")
            Text("场景：${item.scenario.scenario}")
            Text("对方说：${item.scenario.otherSays}")
            item.blindCandidates.take(3).forEachIndexed { candidateIndex, candidate ->
                SoloReviewCandidateCard(
                    runtime = runtime,
                    item = item,
                    candidate = candidate,
                    label = listOf("A", "B", "C").getOrElse(candidateIndex) { "选项" },
                    note = note,
                    onAnswered = {
                        note = ""
                        index = (index + 1).coerceAtMost(reviewItems.size)
                    }
                )
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("可选备注") },
                placeholder = { Text("比如：这句像我，但有点重") }
            )
            OutlinedButton(
                onClick = {
                    runtime.recordSoloCharacterArcFeedback(
                        item = item,
                        candidate = null,
                        feedback = CharacterArcUserFeedback.ALL_BAD,
                        note = note
                    )
                    note = ""
                    index = (index + 1).coerceAtMost(reviewItems.size)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("都不行")
            }
        }
    }
}

@Composable
private fun SoloReviewCandidateCard(
    runtime: HuiyiRuntime,
    item: CharacterArcReviewItem,
    candidate: CharacterArcCandidate,
    label: String,
    note: String,
    onAnswered: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("候选 $label")
            Text(candidate.text)
            listOf(
                listOf(
                    "最像我" to CharacterArcUserFeedback.LIKE_ME,
                    "最自然" to CharacterArcUserFeedback.MOST_NATURAL,
                    "最想继续聊" to CharacterArcUserFeedback.WANT_CONTINUE
                ),
                listOf(
                    "太油" to CharacterArcUserFeedback.TOO_OILY,
                    "太重" to CharacterArcUserFeedback.TOO_HEAVY,
                    "太像汇报" to CharacterArcUserFeedback.TOO_REPORT
                )
            ).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { (buttonLabel, feedback) ->
                        AssistChip(
                            onClick = {
                                runtime.recordSoloCharacterArcFeedback(item, candidate, feedback, note)
                                onAnswered()
                            },
                            label = { Text(buttonLabel) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsPage(
    accessibilityLabel: String,
    overlayLabel: String,
    lanUpdateUrl: String,
    lanUpdateStatus: String,
    lanUpdateError: String?,
    lanLatestVersion: String?,
    versionTapCount: Int,
    onOpenAccessibility: () -> Unit,
    onOpenOverlay: () -> Unit,
    onStartBubble: () -> Unit,
    onStopBubble: () -> Unit,
    onLanUpdateUrlChange: (String) -> Unit,
    onCheckLanUpdate: () -> Unit,
    onDownloadLanUpdate: () -> Unit,
    onOpenUpdateInstaller: () -> Unit,
    onVersionTap: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("设置")
            StatusCard("无障碍", accessibilityLabel)
            Button(onClick = onOpenAccessibility, modifier = Modifier.fillMaxWidth()) { Text("打开无障碍设置") }
            StatusCard("悬浮窗权限", overlayLabel)
            Button(onClick = onOpenOverlay, modifier = Modifier.fillMaxWidth()) { Text("打开悬浮窗授权") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onStartBubble, modifier = Modifier.weight(1f)) { Text("显示悬浮球") }
                OutlinedButton(onClick = onStopBubble, modifier = Modifier.weight(1f)) { Text("隐藏") }
            }
            StatusCard("我的气泡方向", "右侧")
            StatusCard("模型消耗", "默认本地规则")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("局域网更新")
                    OutlinedTextField(
                        value = lanUpdateUrl,
                        onValueChange = onLanUpdateUrlChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("更新地址") },
                        placeholder = { Text("可留空，检查时自动发现") }
                    )
                    Text("状态：$lanUpdateStatus")
                    lanLatestVersion?.let { Text("最新版本：$it") }
                    lanUpdateError?.let { Text("错误：$it") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = onCheckLanUpdate, modifier = Modifier.weight(1f)) { Text("检查") }
                        OutlinedButton(onClick = onDownloadLanUpdate, modifier = Modifier.weight(1f)) { Text("下载") }
                    }
                    Button(onClick = onOpenUpdateInstaller, modifier = Modifier.fillMaxWidth()) {
                        Text("打开安装")
                    }
                }
            }
            StatusCard("隐私说明", "默认不保存原始截图")
            TextButton(onClick = onVersionTap) {
                Text("版本号 ${BuildConfig.VERSION_NAME}")
            }
            if (versionTapCount in 1..4) {
                Text("再点 ${5 - versionTapCount} 次进入开发者模式")
            }
        }
    }
}

@Composable
private fun DeveloperSettingsPage(
    runtime: HuiyiRuntime,
    state: HuiyiRuntimeState,
    accessibilityStatus: String
) {
    var cloudEnabled by remember(state.cloudSettings) { mutableStateOf(state.cloudSettings.cloudEnabled) }
    var relayBaseUrl by remember(state.cloudSettings) { mutableStateOf(state.cloudSettings.baseUrl) }
    var relayModel by remember(state.cloudSettings) { mutableStateOf(state.cloudSettings.model) }
    var relayTimeoutMs by remember(state.cloudSettings) { mutableStateOf(state.cloudSettings.timeoutMs.toString()) }
    var relayApiKeyInput by remember(state.cloudSettings) { mutableStateOf("") }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("开发者设置")
            StatusCard("API 配置", "${BuildConfig.HUIYI_API_BASE_URL} / ${BuildConfig.HUIYI_API_MODEL}")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("高级设置 / 云端设置")
                    StatusCard("providerType", CloudProviderType.OPENAI_COMPATIBLE_RELAY)
                    StatusCard("hasRelayApiKey", state.cloudSettings.relayApiKeyConfigured.toString())
                    StatusCard("keyStorage", state.cloudSettings.relayApiKeyStorageMode)
                    if (!state.cloudSettings.relaySecureStorageAvailable) {
                        Text("当前版本未启用安全密钥存储，不能保存中转站 Key。")
                    }
                    StatusCard("testStatus", state.cloudSettingsTestStatus)
                    OutlinedButton(
                        onClick = { cloudEnabled = !cloudEnabled },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (cloudEnabled) "cloudEnabled: true" else "cloudEnabled: false") }
                    OutlinedTextField(
                        value = relayBaseUrl,
                        onValueChange = { relayBaseUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("baseUrl") },
                        placeholder = { Text("https://relay.example/v1/huiyi") }
                    )
                    OutlinedTextField(
                        value = relayModel,
                        onValueChange = { relayModel = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("model") }
                    )
                    OutlinedTextField(
                        value = relayTimeoutMs,
                        onValueChange = { relayTimeoutMs = it.filter(Char::isDigit) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("timeoutMs") }
                    )
                    OutlinedTextField(
                        value = relayApiKeyInput,
                        onValueChange = { relayApiKeyInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("apiKey") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                runtime.saveCloudSettings(
                                    cloudEnabled,
                                    CloudProviderType.OPENAI_COMPATIBLE_RELAY,
                                    relayBaseUrl,
                                    relayModel,
                                    relayTimeoutMs.toLongOrNull() ?: 20000L,
                                    relayApiKeyInput.ifBlank { null }
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("保存") }
                        OutlinedButton(
                            onClick = runtime::testCloudSettings,
                            modifier = Modifier.weight(1f)
                        ) { Text("测试连接") }
                    }
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("真机验收场景")
                    StatusCard("当前选择", state.selectedRealDeviceScenario.displayName)
                    RealDeviceScenario.entries.forEach { scenario ->
                        OutlinedButton(
                            onClick = { runtime.setRealDeviceScenario(scenario) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (scenario == state.selectedRealDeviceScenario) "已选择 ${scenario.displayName}" else scenario.displayName)
                        }
                    }
                }
            }
            Button(onClick = { runtime.toggleParserDiagnostics() }, modifier = Modifier.fillMaxWidth()) { Text("查看最近解析消息") }
            if (state.showParserDiagnostics) {
                ParserDiagnosticsCard(runtime, state)
            }
            StatusCard("视觉调试图", state.lastVisualDebugOverlayPath ?: "点击下一句后生成")
            Text("导出真机验收包会包含当前聊天截图叠框图，仅用于开发者验收。")
            Button(onClick = { runtime.exportRealDeviceEvidencePack() }, modifier = Modifier.fillMaxWidth()) { Text("导出当前屏幕报告") }
            Button(onClick = { runtime.runLastMeAcceptanceTestAndExport() }, modifier = Modifier.fillMaxWidth()) { Text("导出 last ME 验收包") }
            Button(onClick = { runtime.runLastOtherAcceptanceTestAndExport() }, modifier = Modifier.fillMaxWidth()) { Text("导出 last OTHER 验收包") }
            Button(onClick = { runtime.exportPhoneGptReviewBundle() }, modifier = Modifier.fillMaxWidth()) { Text("导出 GPT 验收总包") }
            OutlinedButton(onClick = { runtime.exportParserReport() }, modifier = Modifier.fillMaxWidth()) { Text("导出解析调试报告") }
            Button(
                onClick = { runtime.exportTextDebug("latest-context.txt", state.latestPipelineResult?.context.toString()) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("导出最近 ChatSceneContext") }
            Button(
                onClick = { runtime.exportTextDebug("latest-decision.txt", state.latestPipelineResult?.tacticalDecision.toString()) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("导出最近 TacticalDecision") }
            Button(
                onClick = { runtime.exportTextDebug("latest-routes.txt", state.latestPipelineResult?.routes.orEmpty().joinToString("\n")) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("导出最近 ReplyRoutes") }
            Button(
                onClick = { runtime.exportTextDebug("accessibility-status.txt", accessibilityStatus) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("导出无障碍状态报告") }
            Button(
                onClick = { runtime.exportClickDiagnosticReports() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("导出点击诊断报告") }
            StatusCard("最近导出", state.lastDebugExportPath ?: "暂无")
            StatusCard("GPT 验收总包", state.latestPhoneGptReviewBundlePath ?: "暂无")
            StatusCard("一键反馈包", state.latestOneTapFeedbackBundlePath ?: "暂无")
            StatusCard("一键 GitHub 上传", state.oneTapGithubUploadState.userVisibleMessage.ifBlank { state.oneTapGithubUploadState.stage.name })
            StatusCard("一键 GitHub 链接", state.oneTapGithubUploadState.githubReviewUrl ?: state.oneTapGithubUploadState.errorCode ?: "暂无")
            StatusCard("下载目录", state.lastPublicExportPath ?: "导出后显示")
            StatusCard("证据包 JSON", state.lastEvidenceJsonPath ?: "暂无")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("真机证据包步骤")
                    Text("1. 打开真实聊天 App。")
                    Text("2. 进入某个聊天窗口。")
                    Text("3. 点击会意悬浮球。")
                    Text("4. 点击“下一句”。")
                    Text("5. 回到会意 App 开发者页。")
                    Text("6. 点击“导出真机当前屏幕证据包”。")
                }
            }
            StatusCard("updateBaseUrl", BuildConfig.HUIYI_UPDATE_BASE_URL.ifBlank { "未配置" })
        }
    }
}

@Composable
private fun ParserDiagnosticsCard(runtime: HuiyiRuntime, state: HuiyiRuntimeState) {
    val result = state.latestPipelineResult
    val messages = result?.captureResult?.messages.orEmpty()
    val effective = messages.filter { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM }
    val last = result?.lastSpeakerDecision?.lastEffectiveMessage
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("最近解析消息")
            Text("我判断最后一句是：")
            Text("「${last?.normalizedText ?: "[无文本]"}」")
            Text("来自：${last?.speaker ?: "未识别"}")
            Text("解析器：${result?.captureResult?.parserName ?: "暂无"}")
            Text("fallback：${result?.captureResult?.parserFallbackUsed ?: false}")
            Text("visualTruth：${result?.captureResult?.visualTruthAvailable ?: false}")
            Text("conflictCount：${result?.captureResult?.visualConflictCount ?: 0}")
            Text("overlay：${result?.visualDebugResult?.overlayImagePath ?: "暂无"}")
            state.lastDebugCorrection?.let { Text("最近调试纠正：$it") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { runtime.applyLastMessageCorrection(Speaker.ME) }, modifier = Modifier.weight(1f)) { Text("实际是我") }
                OutlinedButton(onClick = { runtime.applyLastMessageCorrection(Speaker.OTHER) }, modifier = Modifier.weight(1f)) { Text("实际是对方") }
            }
            OutlinedButton(onClick = { runtime.applyLastMessageCorrection(null) }, modifier = Modifier.fillMaxWidth()) {
                Text("这不是聊天消息")
            }
            effective.takeLast(20).forEachIndexed { index, message ->
                val side = message.inferredSide ?: when (message.speaker) {
                    Speaker.ME -> "right"
                    Speaker.OTHER -> "left"
                    Speaker.SYSTEM -> "system"
                    Speaker.UNKNOWN -> "unknown"
                }
                Text(
                    "[${(index + 1).toString().padStart(2, '0')}][$side][${message.speaker} ${message.speakerConfidence}%] ${message.normalizedText ?: "[non-text]"}"
                )
                Text("reason ${message.speakerReason ?: "unknown"}")
                Text("bounds row=${message.rowBounds?.toShortText() ?: "none"} text=${message.textBounds?.toShortText() ?: "none"} bubble=${message.bubbleBounds?.toShortText() ?: "none"}")
            }
        }
    }
}

private fun com.huiyi.v4.domain.model.VisualBounds.toShortText(): String {
    return "$left,$top,$right,$bottom"
}

private fun Context.openOverlaySettings() {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:$packageName")
    )
    startActivity(intent)
}
