package com.huiyi.v4.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huiyi.v4.BuildConfig
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.model.TacticalDecisionType

private enum class TabPage(val title: String) {
    Home("首页"),
    Persona("我的底色"),
    Settings("设置"),
    Developer("开发者")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuiyiRoot() {
    var tab by remember { mutableStateOf(TabPage.Home) }
    var state by remember { mutableStateOf(sampleState()) }
    var showPanel by remember { mutableStateOf(false) }
    var versionTapCount by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("会意 v4 Core") }) },
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
                TabPage.Home -> HomePage(state, onOpenPanel = { showPanel = true }, onPersona = { tab = TabPage.Persona }, onSettings = { tab = TabPage.Settings })
                TabPage.Persona -> MyPersonaPage(state, onToggle = { state = state.togglePersona() })
                TabPage.Settings -> SettingsPage(
                    versionTapCount = versionTapCount,
                    onVersionTap = {
                        versionTapCount += 1
                        if (versionTapCount >= 5) tab = TabPage.Developer
                    }
                )
                TabPage.Developer -> DeveloperSettingsPage()
            }
        }
    }

    if (showPanel) {
        ModalBottomSheet(
            onDismissRequest = { showPanel = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            FloatingTacticalPanel(
                state = state,
                onVoiceSummary = { summary -> state = state.withVoiceSummary(summary) }
            )
        }
    }
}

@Composable
private fun HomePage(
    state: HuiyiDemoState,
    onOpenPanel: () -> Unit,
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
            StatusCard("无障碍状态", "待开启")
            StatusCard("悬浮球状态", "可从这里模拟打开")
            StatusCard("我的底色", if (state.personaEnabled) "已启用" else "已关闭")
        }
        item {
            Button(onClick = onOpenPanel, modifier = Modifier.fillMaxWidth()) {
                Text("新聊天")
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
    state: HuiyiDemoState,
    onVoiceSummary: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.decision.decisionType == TacticalDecisionType.VOICE_SUMMARY_REQUIRED) {
            item { VoiceSummaryCard(state, onVoiceSummary) }
        }
        if (state.decision.decisionType == TacticalDecisionType.CONTEXT_REQUIRED) {
            item { ContextRequiredCard(state.context.contentCompleteness.reason) }
        }
        item {
            Text("会意雷达")
            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("雷达判断：${state.decision.situation}")
                    Text("当前打法：${state.decision.bestMove}")
                    Text("别做：${state.decision.avoidMoves.joinToString(" / ")}")
                    state.decision.influenceProfile.riskWarning?.let { Text("风险提示：$it") }
                    state.decision.fallbackMove?.let { Text("撤退方案：$it") }
                }
            }
        }
        items(state.routes) { route ->
            ReplyRouteCard(route)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("填入") }
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("换一组") }
            }
        }
    }
}

@Composable
private fun ReplyRouteCard(route: ReplyRoute) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(route.tag) })
                if (route.recommended) AssistChip(onClick = {}, label = { Text("推荐") })
                if (route.riskLevel != RiskLevel.LOW) AssistChip(onClick = {}, label = { Text("高风险") })
            }
            Text(route.name)
            Text(route.message)
            route.riskWarning?.let { Text("风险提示：$it") }
            route.fallbackMove?.let { Text("撤退方案：$it") }
            TextButton(onClick = {}) { Text("复制") }
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
            Text("补一屏前文，或手动写一句摘要。")
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
private fun SettingsPage(versionTapCount: Int, onVersionTap: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("设置")
            StatusCard("无障碍状态", "待开启")
            StatusCard("悬浮球开关", "手动模拟")
            StatusCard("我的气泡方向", "右侧")
            StatusCard("模型消耗", "默认使用本地模拟")
            StatusCard("更新检查", "未配置更新源")
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
private fun DeveloperSettingsPage() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("开发者设置")
            StatusCard("API 配置", "${BuildConfig.HUIYI_API_BASE_URL} / ${BuildConfig.HUIYI_API_MODEL}")
            StatusCard("导出调试包", "占位")
            StatusCard("Parser 调试", "GenericVisualBubbleParser")
            StatusCard("OCR mock 调试", "MockOcrEngine")
            StatusCard("ContextAssembler 调试", "可本地验证")
            StatusCard("本地验证", "运行 testDebugUnitTest")
            StatusCard("原始 JSON 查看", "开发者可见")
            StatusCard("updateBaseUrl", BuildConfig.HUIYI_UPDATE_BASE_URL.ifBlank { "未配置" })
            StatusCard("latest.json", "待生成")
            StatusCard("sha256", "待生成")
            StatusCard("下载日志", "暂无")
        }
    }
}
