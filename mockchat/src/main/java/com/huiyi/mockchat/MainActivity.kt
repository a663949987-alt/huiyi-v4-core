package com.huiyi.mockchat

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.OutputStream

class MainActivity : ComponentActivity() {
    private var currentProfile by mutableStateOf(MockChatLayoutProfile.WECHAT_LIKE)
    private var currentScenario by mutableStateOf(MockScenario.LAST_OTHER)
    private var currentFontScale by mutableStateOf(MockFontScaleProfile.NORMAL)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyIntent(intent)
        setContent {
            MaterialTheme {
                MockChatScreen(
                    profile = currentProfile,
                    scenario = currentScenario,
                    fontScaleProfile = currentFontScale,
                    onProfileChange = { currentProfile = it },
                    onScenarioChange = { currentScenario = it },
                    onFontScaleChange = { currentFontScale = it },
                    onExportScreenshot = { view -> exportScreenshot(view, currentProfile, currentScenario) }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        applyIntent(intent)
    }

    private fun applyIntent(intent: Intent) {
        val profileRaw = intent.getStringExtra("profile")
            ?: intent.data?.getQueryParameter("profile")
            ?: currentProfile.id
        val scenarioRaw = intent.getStringExtra("scenario")
            ?: intent.data?.getQueryParameter("scenario")
            ?: currentScenario.id
        val fontScaleRaw = intent.getStringExtra("fontScale")
            ?: intent.data?.getQueryParameter("fontScale")
            ?: currentFontScale.id
        currentProfile = MockChatLayoutProfile.entries.firstOrNull { it.id == profileRaw } ?: currentProfile
        currentScenario = MockScenario.entries.firstOrNull { it.id == scenarioRaw } ?: currentScenario
        currentFontScale = MockFontScaleProfile.entries.firstOrNull { it.id == fontScaleRaw } ?: currentFontScale
    }

    private fun exportScreenshot(view: View, profile: MockChatLayoutProfile, scenario: MockScenario) {
        val bitmap = Bitmap.createBitmap(view.width.coerceAtLeast(1), view.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "${profile.id}_${scenario.id}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MockChatLab")
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return
        val out: OutputStream = contentResolver.openOutputStream(uri) ?: return
        out.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}

enum class MockChatLayoutProfile(
    val id: String,
    val label: String,
    val title: String,
    val status: String,
    val background: Color,
    val incoming: Color,
    val outgoing: Color,
    val round: Int,
    val showAvatar: Boolean,
    val inputRich: Boolean
) {
    LIAOQI_LIKE("liaoqi_like", "LIAOQI_LIKE", "白云蓝天", "上次在线时间07-02 18:06", Color(0xFFF4F1EC), Color.White, Color(0xFFBEE6A8), 12, true, true),
    XIAOENAI_LIKE("xiaoenai_like", "XIAOENAI_LIKE", "小恩爱", "情侣空间", Color(0xFFFFF4F8), Color.White, Color(0xFFFFD6E7), 18, true, true),
    WECHAT_LIKE("wechat_like", "WECHAT_LIKE", "白云蓝天", "上次在线时间07-02 18:06", Color(0xFFF4F1EC), Color.White, Color(0xFFBEE6A8), 8, true, false),
    QQ_LIKE("qq_like", "QQ_LIKE", "蓝桥", "手机在线", Color(0xFFEFF6FF), Color(0xFFFFFFFF), Color(0xFFB8E5FF), 18, true, true),
    REDBOOK_DM_LIKE("redbook_like", "REDBOOK_DM_LIKE", "小鹿同学", "刚刚在线", Color(0xFFFFF7F7), Color.White, Color(0xFFFFE1E7), 14, true, true),
    DATING_APP_LIKE("dating_like", "DATING_APP_LIKE", "林夏", "资料完整度 82%", Color(0xFFFFFAF2), Color.White, Color(0xFFE8D8FF), 16, true, true),
    MINIMAL_CHAT_LIKE("minimal_like", "MINIMAL_CHAT_LIKE", "对话", "在线", Color(0xFFF7F7F5), Color(0xFFFDFDFB), Color(0xFFEDEDE8), 6, false, false),
    WEBVIEW_LIKE_LOW_ACCESSIBILITY("webview_like_low_accessibility", "WEBVIEW_LIKE_LOW_ACCESSIBILITY", "WebView Chat", "custom view", Color(0xFFF1F1F1), Color(0xFFEAEAEA), Color(0xFFDCDCDC), 4, false, false),
    LAUNCHER_DESKTOP("launcher_desktop", "LAUNCHER_DESKTOP", "华为桌面", "launcher", Color(0xFFF6F6F6), Color.White, Color(0xFFE0E0E0), 6, false, false),
    HUIYI_OVERLAY_CONTAMINATED("huiyi_overlay_contaminated", "HUIYI_OVERLAY_CONTAMINATED", "会意雷达", "这次不对，发给 GPT", Color(0xFFF6F6F6), Color.White, Color(0xFFE0E0E0), 6, false, false),
    LIAOQI_HUAWEI_LARGE_TEXT("liaoqi_huawei_large_text", "LIAOQI_HUAWEI_LARGE_TEXT", "白云蓝天", "上次在线时间07-02 18:06", Color(0xFFF4F1EC), Color.White, Color(0xFFBEE6A8), 12, true, true)
}

enum class MockFontScaleProfile(val id: String, val label: String, val scale: Float) {
    SMALL("font_small", "font_small 0.85", 0.85f),
    NORMAL("font_normal", "font_normal 1.0", 1.0f),
    LARGE("font_large", "font_large 1.15", 1.15f),
    EXTRA_LARGE("font_extra_large", "font_extra_large 1.3", 1.3f),
    HUGE("font_huge", "font_huge 1.5", 1.5f)
}

enum class MockScenario(val id: String, val label: String) {
    LAST_ME("last_me", "A last_me"),
    LAST_OTHER("last_other", "B last_other"),
    READ_RECEIPT_STATUS("read_receipt_status", "C1 read_receipt_status"),
    SEND_FAILED("send_failed", "C2 send_failed"),
    METADATA_TRAP("metadata_trap", "C metadata_trap"),
    VOICE_LAST_OTHER("voice_last_other", "D voice_last_other"),
    IMAGE_OR_STICKER("image_or_sticker", "E image_or_sticker"),
    LOW_EXPRESSION("low_expression", "F low_expression"),
    LONG_MULTILINE("long_multiline", "G long_multiline"),
    QUOTED_REPLY("quoted_reply", "H quoted_reply"),
    UNKNOWN_BOUNDS("unknown_bounds", "I unknown_bounds"),
    TIME_AT_BOTTOM("time_at_bottom", "J time_at_bottom"),
    HUIYI_OVERLAY_CONTAMINATION("huiyi_overlay_contamination", "K huiyi_overlay_contamination")
}

private enum class BubbleKind {
    TIME,
    DATE,
    SYSTEM,
    OTHER,
    ME,
    VOICE_OTHER,
    IMAGE_OTHER,
    STICKER_OTHER,
    SHARE_OTHER,
    PROFILE_CARD,
    LONG_OTHER,
    CENTER_UNKNOWN,
    STATUS,
    QUOTED_OTHER
}

private data class ChatItem(
    val kind: BubbleKind,
    val text: String
)

@Composable
private fun MockChatScreen(
    profile: MockChatLayoutProfile,
    scenario: MockScenario,
    fontScaleProfile: MockFontScaleProfile,
    onProfileChange: (MockChatLayoutProfile) -> Unit,
    onScenarioChange: (MockScenario) -> Unit,
    onFontScaleChange: (MockFontScaleProfile) -> Unit,
    onExportScreenshot: (View) -> Unit
) {
    val view = LocalView.current
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = profile.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                profile = profile,
                scenario = scenario,
                fontScaleProfile = fontScaleProfile,
                onProfileChange = onProfileChange,
                onScenarioChange = onScenarioChange,
                onFontScaleChange = onFontScaleChange,
                onExportScreenshot = { onExportScreenshot(view) }
            )
            ChatList(
                profile = profile,
                fontScale = fontScaleProfile.scale,
                items = scenario.items(profile),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            InputBar(profile, fontScaleProfile.scale)
        }
    }
}

@Composable
private fun TopBar(
    profile: MockChatLayoutProfile,
    scenario: MockScenario,
    fontScaleProfile: MockFontScaleProfile,
    onProfileChange: (MockChatLayoutProfile) -> Unit,
    onScenarioChange: (MockScenario) -> Unit,
    onFontScaleChange: (MockFontScaleProfile) -> Unit,
    onExportScreenshot: () -> Unit
) {
    var profileExpanded by remember { mutableStateOf(false) }
    var scenarioExpanded by remember { mutableStateOf(false) }
    var fontExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.72f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("返回", fontSize = (14 * fontScaleProfile.scale).sp, color = Color(0xFF6E6A63))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(profile.title, fontSize = (18 * fontScaleProfile.scale).sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(profile.status, fontSize = (12 * fontScaleProfile.scale).sp, color = Color(0xFF7D776D), maxLines = 1)
            }
            Box {
                Text(
                    text = "模板",
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { profileExpanded = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    fontSize = (13 * fontScaleProfile.scale).sp,
                    color = Color(0xFF4C463F)
                )
                DropdownMenu(expanded = profileExpanded, onDismissRequest = { profileExpanded = false }) {
                    MockChatLayoutProfile.entries.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.label) },
                            onClick = {
                                profileExpanded = false
                                onProfileChange(item)
                            }
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${profile.label} / ${scenario.label} / ${fontScaleProfile.label}",
                fontSize = (11 * fontScaleProfile.scale).sp,
                color = Color(0xFF918A7E),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    Text(
                        text = "场景",
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { scenarioExpanded = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        fontSize = (12 * fontScaleProfile.scale).sp,
                        color = Color(0xFF4C463F)
                    )
                    DropdownMenu(expanded = scenarioExpanded, onDismissRequest = { scenarioExpanded = false }) {
                        MockScenario.entries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.label) },
                                onClick = {
                                    scenarioExpanded = false
                                    onScenarioChange(item)
                                }
                            )
                        }
                    }
                }
                OutlinedButton(onClick = onExportScreenshot) {
                    Text("导出截图", fontSize = (12 * fontScaleProfile.scale).sp)
                }
                Box {
                    Text(
                        text = "字体",
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { fontExpanded = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        fontSize = (12 * fontScaleProfile.scale).sp,
                        color = Color(0xFF4C463F)
                    )
                    DropdownMenu(expanded = fontExpanded, onDismissRequest = { fontExpanded = false }) {
                        MockFontScaleProfile.entries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.label) },
                                onClick = {
                                    fontExpanded = false
                                    onFontScaleChange(item)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatList(profile: MockChatLayoutProfile, fontScale: Float, items: List<ChatItem>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { item ->
            SimpleChatNode(profile, item, fontScale)
        }
    }
}

@Composable
private fun SimpleChatNode(profile: MockChatLayoutProfile, item: ChatItem, fontScale: Float) {
    val alignment = when (item.kind) {
        BubbleKind.TIME, BubbleKind.DATE, BubbleKind.SYSTEM, BubbleKind.CENTER_UNKNOWN -> Alignment.Center
        BubbleKind.ME -> Alignment.CenterEnd
        BubbleKind.STATUS -> Alignment.CenterEnd
        else -> Alignment.CenterStart
    }
    val background = when (item.kind) {
        BubbleKind.TIME, BubbleKind.DATE -> Color(0xFFE3DDD4)
        BubbleKind.SYSTEM -> Color(0xFFFFF0C7)
        BubbleKind.ME -> profile.outgoing
        BubbleKind.CENTER_UNKNOWN -> Color(0xFFFFFBF1)
        BubbleKind.STATUS -> Color.Transparent
        BubbleKind.IMAGE_OTHER, BubbleKind.STICKER_OTHER, BubbleKind.SHARE_OTHER, BubbleKind.PROFILE_CARD -> Color(0xFFE9EEF1)
        else -> profile.incoming
    }
    val width = when (item.kind) {
        BubbleKind.TIME -> 92.dp
        BubbleKind.DATE -> 160.dp
        BubbleKind.SYSTEM -> 230.dp
        BubbleKind.VOICE_OTHER -> 150.dp
        BubbleKind.IMAGE_OTHER, BubbleKind.STICKER_OTHER -> 164.dp
        BubbleKind.SHARE_OTHER, BubbleKind.PROFILE_CARD -> 220.dp
        BubbleKind.CENTER_UNKNOWN -> 250.dp
        BubbleKind.STATUS -> 120.dp
        else -> if (profile == MockChatLayoutProfile.LIAOQI_HUAWEI_LARGE_TEXT) 318.dp else 268.dp
    }
    val height = when (item.kind) {
        BubbleKind.TIME, BubbleKind.DATE -> (26 * fontScale).dp
        BubbleKind.SYSTEM -> (32 * fontScale).dp
        BubbleKind.STATUS -> (22 * fontScale).dp
        BubbleKind.IMAGE_OTHER, BubbleKind.STICKER_OTHER -> 58.dp
        BubbleKind.SHARE_OTHER, BubbleKind.PROFILE_CARD -> 62.dp
        BubbleKind.LONG_OTHER -> (72 * fontScale).dp
        else -> (50 * fontScale).dp
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Row(
            modifier = Modifier.align(alignment),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (profile.showAvatar && item.kind.isIncoming()) {
                Avatar(Color(0xFF86A8A8))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = item.text,
                modifier = Modifier
                    .width(width)
                    .height(height)
                    .clip(RoundedCornerShape(profile.round.dp))
                    .background(background)
                    .then(if (item.kind == BubbleKind.CENTER_UNKNOWN) Modifier.border(1.dp, Color(0xFFD0C6B8), RoundedCornerShape(profile.round.dp)) else Modifier)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                fontSize = ((if (item.kind == BubbleKind.TIME || item.kind == BubbleKind.DATE) 12 else 14) * fontScale).sp,
                lineHeight = (16 * fontScale).sp,
                color = Color(0xFF23211E),
                maxLines = when (item.kind) {
                    BubbleKind.TIME, BubbleKind.DATE, BubbleKind.VOICE_OTHER -> 1
                    BubbleKind.STATUS -> 1
                    BubbleKind.SHARE_OTHER, BubbleKind.PROFILE_CARD, BubbleKind.LONG_OTHER -> 3
                    else -> 2
                },
                overflow = TextOverflow.Ellipsis
            )
            if (profile.showAvatar && item.kind == BubbleKind.ME) {
                Spacer(Modifier.width(8.dp))
                Avatar(Color(0xFF7C9563))
            }
        }
    }
}

private fun BubbleKind.isIncoming(): Boolean = this in setOf(
    BubbleKind.OTHER,
    BubbleKind.LONG_OTHER,
    BubbleKind.VOICE_OTHER,
    BubbleKind.IMAGE_OTHER,
    BubbleKind.STICKER_OTHER,
    BubbleKind.SHARE_OTHER,
    BubbleKind.PROFILE_CARD,
    BubbleKind.QUOTED_OTHER
)

@Composable
private fun Avatar(color: Color) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun InputBar(profile: MockChatLayoutProfile, fontScale: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(Color.White.copy(alpha = 0.72f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("语音", fontSize = (14 * fontScale).sp, color = Color(0xFF514C45))
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text("输入框", color = Color(0xFF8A8378), fontSize = (15 * fontScale).sp)
        }
        Spacer(Modifier.width(8.dp))
        if (profile.inputRich) {
            Text("图片", fontSize = (14 * fontScale).sp, color = Color(0xFF514C45))
            Spacer(Modifier.width(8.dp))
        }
        Text("表情", fontSize = (14 * fontScale).sp, color = Color(0xFF514C45))
        Spacer(Modifier.width(8.dp))
        Button(onClick = {}) { Text("发送") }
    }
}

private fun MockScenario.items(profile: MockChatLayoutProfile): List<ChatItem> = when (this) {
    MockScenario.LAST_ME -> listOf(
        ChatItem(BubbleKind.TIME, "10:56"),
        ChatItem(BubbleKind.OTHER, "${profile.title}：孩子的事情我还是会有点想法。"),
        ChatItem(BubbleKind.ME, "你们是离了婚才生的小孩吗？你刚刚说你离婚都10年了。")
    )
    MockScenario.LAST_OTHER -> baseOtherThread(profile)
    MockScenario.READ_RECEIPT_STATUS -> listOf(
        ChatItem(BubbleKind.TIME, "11:08"),
        ChatItem(BubbleKind.OTHER, "I may be offline for a while."),
        ChatItem(BubbleKind.ME, "Okay, I will not keep pushing."),
        ChatItem(BubbleKind.STATUS, "\u5df2\u8bfb"),
        ChatItem(BubbleKind.STATUS, "\u672a\u8bfb"),
        ChatItem(BubbleKind.STATUS, "\u2713\u2713")
    )
    MockScenario.SEND_FAILED -> listOf(
        ChatItem(BubbleKind.TIME, "11:09"),
        ChatItem(BubbleKind.OTHER, "I need a little space."),
        ChatItem(BubbleKind.ME, "Understood, I will pause."),
        ChatItem(BubbleKind.STATUS, "\u53d1\u9001\u5931\u8d25"),
        ChatItem(BubbleKind.STATUS, "\u91cd\u53d1")
    )
    MockScenario.METADATA_TRAP -> metadataTrapThread(profile)
    MockScenario.VOICE_LAST_OTHER -> listOf(
        ChatItem(BubbleKind.TIME, "10:56"),
        ChatItem(BubbleKind.OTHER, "我先发个语音，你听一下。"),
        ChatItem(BubbleKind.ME, "好，我听。"),
        ChatItem(BubbleKind.VOICE_OTHER, "语音 18秒")
    )
    MockScenario.IMAGE_OR_STICKER -> listOf(
        ChatItem(BubbleKind.TIME, "10:56"),
        ChatItem(BubbleKind.ME, "你刚才说的地方我大概明白。"),
        if (profile == MockChatLayoutProfile.QQ_LIKE) {
            ChatItem(BubbleKind.STICKER_OTHER, "[sticker] 表情包 未描述")
        } else {
            ChatItem(BubbleKind.IMAGE_OTHER, "[image] 图片 未描述")
        }
    )
    MockScenario.LOW_EXPRESSION -> listOf(
        ChatItem(BubbleKind.TIME, "10:56"),
        ChatItem(BubbleKind.OTHER, "嗯"),
        ChatItem(BubbleKind.OTHER, "好"),
        ChatItem(BubbleKind.OTHER, "没事"),
        ChatItem(BubbleKind.OTHER, "忙"),
        ChatItem(BubbleKind.OTHER, "晚点说")
    )
    MockScenario.LONG_MULTILINE -> listOf(
        ChatItem(BubbleKind.DATE, "2026-07-03"),
        ChatItem(BubbleKind.ME, "你慢慢说，我在。"),
        ChatItem(BubbleKind.LONG_OTHER, "我其实不是不想回你\n就是今天事情堆在一起\n有点不知道先说哪一件")
    )
    MockScenario.QUOTED_REPLY -> listOf(
        ChatItem(BubbleKind.TIME, "10:56"),
        ChatItem(BubbleKind.ME, "那你先别急着解释。"),
        ChatItem(BubbleKind.QUOTED_OTHER, "引用：那你先别急着解释。\n实际回复：我不是急，我是怕你误会。")
    )
    MockScenario.UNKNOWN_BOUNDS -> listOf(
        ChatItem(BubbleKind.TIME, "10:56"),
        ChatItem(BubbleKind.OTHER, "这个事情我有点不知道怎么说。"),
        ChatItem(BubbleKind.OTHER, "你先别急着回。"),
        ChatItem(BubbleKind.CENTER_UNKNOWN, "这句故意居中，边界不明显。")
    )
    MockScenario.TIME_AT_BOTTOM -> baseOtherThread(profile) + ChatItem(BubbleKind.TIME, "11:08")
    MockScenario.HUIYI_OVERLAY_CONTAMINATION -> listOf(
        ChatItem(BubbleKind.TIME, "11:10"),
        ChatItem(BubbleKind.OTHER, "I was not ignoring you, I just got overwhelmed."),
        ChatItem(BubbleKind.ME, "I hear you. I will slow down."),
        ChatItem(BubbleKind.SYSTEM, "\u4f1a\u610f\u96f7\u8fbe \u6700\u540e\u4e00\u53e5\u662f\u6211 \u8fd9\u6b21\u4e0d\u5bf9\uff0c\u53d1\u7ed9 GPT")
    )
}

private fun metadataTrapThread(profile: MockChatLayoutProfile): List<ChatItem> = listOf(
    ChatItem(BubbleKind.DATE, "2026-07-03"),
    ChatItem(BubbleKind.SYSTEM, "系统提示：你们已成为好友"),
    ChatItem(BubbleKind.TIME, "10:56"),
    ChatItem(BubbleKind.OTHER, "${profile.title}：今天事情有点多。"),
    ChatItem(BubbleKind.ME, "我在，你挑你想说的讲。"),
    ChatItem(BubbleKind.TIME, "11:00"),
    ChatItem(BubbleKind.SYSTEM, "系统推荐：保持礼貌沟通"),
    ChatItem(BubbleKind.OTHER, "我就是怕一说又变成抱怨。"),
    ChatItem(BubbleKind.TIME, "11:02")
)

private fun baseOtherThread(profile: MockChatLayoutProfile): List<ChatItem> {
    if (profile == MockChatLayoutProfile.LIAOQI_HUAWEI_LARGE_TEXT) {
        return listOf(
            ChatItem(BubbleKind.TIME, "10:56"),
            ChatItem(BubbleKind.OTHER, "你爱吃什么"),
            ChatItem(BubbleKind.ME, "好啊，乖乖，我不跟你聊了，拜拜。"),
            ChatItem(BubbleKind.OTHER, "😊好"),
            ChatItem(BubbleKind.ME, "😆吃饭前唱军歌，不如你唱的好听"),
            ChatItem(BubbleKind.TIME, "10:58"),
            ChatItem(BubbleKind.OTHER, "为什么给我发这个😂五分钟的视频才看完"),
            ChatItem(BubbleKind.OTHER, "今天我们要开个会，月底最后一天……"),
            ChatItem(BubbleKind.TIME, "10:59"),
            ChatItem(BubbleKind.OTHER, "看你挺忙的，忙完注意休息哈")
        )
    }
    val middle = when (profile) {
        MockChatLayoutProfile.QQ_LIKE -> ChatItem(BubbleKind.STICKER_OTHER, "[sticker] 表情包 未描述")
        MockChatLayoutProfile.REDBOOK_DM_LIKE -> ChatItem(BubbleKind.SHARE_OTHER, "分享卡片：周末散步路线")
        MockChatLayoutProfile.DATING_APP_LIKE -> ChatItem(BubbleKind.PROFILE_CARD, "资料卡：喜欢夜跑 / 电影")
        else -> ChatItem(BubbleKind.IMAGE_OTHER, "[image] 图片 未描述")
    }
    return listOf(
        ChatItem(BubbleKind.TIME, "10:56"),
        ChatItem(BubbleKind.OTHER, "孩子跟着男方的。但是男方也提一个跟我离婚的之前要孩子的抚养费……"),
        ChatItem(BubbleKind.ME, "你们是离了婚才生的小孩吗？你刚刚说你离婚都10年了。"),
        ChatItem(BubbleKind.TIME, "11:00"),
        ChatItem(BubbleKind.OTHER, "是啊，我离婚是10年了呀。"),
        ChatItem(BubbleKind.TIME, "10:58"),
        ChatItem(BubbleKind.OTHER, "小孩之前跟着我在一起，在广州。在深圳。很多地方都去打工过……"),
        middle,
        ChatItem(BubbleKind.TIME, "10:59"),
        ChatItem(BubbleKind.OTHER, "本来我的过去我不想再提离婚，都10年了，孩子也舍不得……")
    )
}
