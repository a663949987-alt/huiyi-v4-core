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
import androidx.compose.ui.unit.dp
import com.huiyi.v4.BuildConfig
import com.huiyi.v4.accessibility.HuiyiAccessibilityService
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.floating.FloatingBubbleService
import com.huiyi.v4.runtime.HuiyiRuntime
import com.huiyi.v4.runtime.HuiyiRuntimeState

private enum class TabPage(val title: String) {
    Home("首页"),
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
    var tab by remember { mutableStateOf(TabPage.Home) }
    var versionTapCount by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("会意 v4.1") }) },
        bottomBar = {
            NavigationBar {
                listOf(TabPage.Home, TabPage.Persona, TabPage.Settings).forEach { page ->
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
                    onOpenPanel = { runtime.setPanelVisible(true) },
                    onRunPipeline = { runtime.runNextSentence() },
                    onPersona = { tab = TabPage.Persona },
                    onSettings = { tab = TabPage.Settings }
                )
                TabPage.Persona -> MyPersonaPage(runtimeState.demoState, onToggle = runtime::togglePersona)
                TabPage.Settings -> SettingsPage(
                    accessibilityLabel = when {
                        accessibilityState.serviceConnected && accessibilityState.rootAvailable -> "已连接"
                        accessibilityState.serviceConnected -> "已开启，等待窗口"
                        else -> "未开启"
                    },
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

@Composable
private fun HomePage(
    state: HuiyiRuntimeState,
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
            StatusCard("无障碍状态", if (HuiyiAccessibilityService.state.value.serviceConnected) "已连接" else "未连接")
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
                ReplyRouteCard(runtime, route)
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
private fun ReplyRouteCard(runtime: HuiyiRuntime, route: ReplyRoute) {
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
            Text(route.name)
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
private fun MyPersonaPage(state: HuiyiDemoState, onToggle: () -> Unit) {
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
                        placeholder = { Text("http://电脑IP:8787/latest.json") }
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("开发者设置")
            StatusCard("API 配置", "${BuildConfig.HUIYI_API_BASE_URL} / ${BuildConfig.HUIYI_API_MODEL}")
            Button(onClick = { runtime.exportParserReport() }, modifier = Modifier.fillMaxWidth()) { Text("导出当前屏幕解析报告") }
            Button(onClick = { runtime.exportRealDeviceEvidencePack() }, modifier = Modifier.fillMaxWidth()) { Text("导出真机当前屏幕证据包") }
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
            StatusCard("最近导出", state.lastDebugExportPath ?: "暂无")
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

private fun Context.openOverlaySettings() {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:$packageName")
    )
    startActivity(intent)
}
