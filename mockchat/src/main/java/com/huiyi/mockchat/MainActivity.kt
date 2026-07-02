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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    private var currentScenario by mutableStateOf(MockScenario.LAST_OTHER)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentScenario = scenarioFrom(intent)
        setContent {
            MaterialTheme {
                MockChatScreen(
                    scenario = currentScenario,
                    onScenarioChange = { currentScenario = it },
                    onExportScreenshot = { view -> exportScreenshot(view, currentScenario) }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        currentScenario = scenarioFrom(intent)
    }

    private fun scenarioFrom(intent: Intent): MockScenario {
        val raw = intent.getStringExtra("scenario")
            ?: intent.data?.getQueryParameter("scenario")
            ?: MockScenario.LAST_OTHER.id
        return MockScenario.entries.firstOrNull { it.id == raw } ?: MockScenario.LAST_OTHER
    }

    private fun exportScreenshot(view: View, scenario: MockScenario) {
        val bitmap = Bitmap.createBitmap(view.width.coerceAtLeast(1), view.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "mockchat_${scenario.id}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MockChatLab")
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return
        val out: OutputStream = contentResolver.openOutputStream(uri) ?: return
        out.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}

enum class MockScenario(val id: String, val label: String) {
    LAST_ME("last_me", "A last_me"),
    LAST_OTHER("last_other", "B last_other"),
    METADATA_TRAP("metadata_trap", "C metadata_trap"),
    VOICE_LAST_OTHER("voice_last_other", "D voice_last_other"),
    UNKNOWN_BOUNDS("unknown_bounds", "E unknown_bounds"),
    LOW_EXPRESSION("low_expression", "F low_expression")
}

private enum class BubbleKind {
    TIME,
    OTHER,
    ME,
    VOICE_OTHER,
    IMAGE_OTHER,
    CENTER_UNKNOWN
}

private data class ChatItem(
    val kind: BubbleKind,
    val text: String
)

@Composable
private fun MockChatScreen(
    scenario: MockScenario,
    onScenarioChange: (MockScenario) -> Unit,
    onExportScreenshot: (View) -> Unit
) {
    val view = LocalView.current
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF4F1EC)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(scenario, onScenarioChange, onExportScreenshot = { onExportScreenshot(view) })
            ChatList(
                items = scenario.items(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            InputBar()
        }
    }
}

@Composable
private fun TopBar(
    scenario: MockScenario,
    onScenarioChange: (MockScenario) -> Unit,
    onExportScreenshot: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F4EE))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("返回", fontSize = 14.sp, color = Color(0xFF6E6A63))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("白云蓝天", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text("上次在线时间07-02 18:06", fontSize = 12.sp, color = Color(0xFF7D776D), maxLines = 1)
            }
            Box {
                Text(
                    text = "场景",
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    fontSize = 13.sp,
                    color = Color(0xFF4C463F)
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    MockScenario.entries.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.label) },
                            onClick = {
                                expanded = false
                                onScenarioChange(item)
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
                text = scenario.label,
                fontSize = 11.sp,
                color = Color(0xFF918A7E),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            OutlinedButton(onClick = onExportScreenshot) {
                Text("导出截图", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ChatList(items: List<ChatItem>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items) { item ->
            when (item.kind) {
                BubbleKind.TIME -> TimeStamp(item.text)
                BubbleKind.OTHER -> OtherBubble(item.text)
                BubbleKind.ME -> MeBubble(item.text)
                BubbleKind.VOICE_OTHER -> OtherBubble(item.text, voice = true)
                BubbleKind.IMAGE_OTHER -> ImageBubble(item.text)
                BubbleKind.CENTER_UNKNOWN -> CenterBubble(item.text)
            }
        }
    }
}

@Composable
private fun TimeStamp(text: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFE3DDD4))
                .padding(horizontal = 10.dp, vertical = 3.dp),
            color = Color(0xFF756E64),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun OtherBubble(text: String, voice: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Avatar(Color(0xFF86A8A8))
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            modifier = Modifier
                .width(260.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = if (voice) 13.dp else 10.dp),
            fontSize = 16.sp,
            lineHeight = 22.sp,
            color = Color(0xFF23211E)
        )
    }
}

@Composable
private fun MeBubble(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Text(
            text = text,
            modifier = Modifier
                .width(260.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFBEE6A8))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            fontSize = 16.sp,
            lineHeight = 22.sp,
            color = Color(0xFF1F2A1F)
        )
        Spacer(Modifier.width(8.dp))
        Avatar(Color(0xFF7C9563))
    }
}

@Composable
private fun CenterBubble(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(
            text = text,
            modifier = Modifier
                .width(250.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFFFBF1))
                .border(1.dp, Color(0xFFD0C6B8), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            fontSize = 16.sp,
            lineHeight = 22.sp,
            color = Color(0xFF2C2824)
        )
    }
}

@Composable
private fun ImageBubble(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Avatar(Color(0xFF86A8A8))
        Spacer(Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .width(168.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE9EEF1))
                .border(1.dp, Color(0xFFC8D2D7), RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("图片", fontSize = 15.sp, color = Color(0xFF61717A))
            Text(text, fontSize = 12.sp, color = Color(0xFF61717A), maxLines = 2)
        }
    }
}

@Composable
private fun Avatar(color: Color) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun InputBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .background(Color(0xFFEEEAE4))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("语音", fontSize = 14.sp, color = Color(0xFF514C45))
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
            Text("输入框", color = Color(0xFF8A8378), fontSize = 15.sp)
        }
        Spacer(Modifier.width(8.dp))
        Text("表情", fontSize = 14.sp, color = Color(0xFF514C45))
        Spacer(Modifier.width(8.dp))
        Button(onClick = {}) { Text("发送") }
    }
}

private fun MockScenario.items(): List<ChatItem> = when (this) {
    MockScenario.LAST_ME -> listOf(
        ChatItem(BubbleKind.TIME, "10:56"),
        ChatItem(BubbleKind.OTHER, "孩子跟着男方的。但是男方也提一个跟我离婚的之前要孩子的抚养费……"),
        ChatItem(BubbleKind.ME, "你们是离了婚才生的小孩吗？你刚刚说你离婚都10年了。")
    )
    MockScenario.LAST_OTHER -> baseOtherThread()
    MockScenario.METADATA_TRAP -> baseOtherThread() + ChatItem(BubbleKind.TIME, "11:02")
    MockScenario.VOICE_LAST_OTHER -> listOf(
        ChatItem(BubbleKind.TIME, "10:56"),
        ChatItem(BubbleKind.OTHER, "我先发个语音，你听一下。"),
        ChatItem(BubbleKind.ME, "好，我听。"),
        ChatItem(BubbleKind.VOICE_OTHER, "语音 18秒")
    )
    MockScenario.UNKNOWN_BOUNDS -> listOf(
        ChatItem(BubbleKind.TIME, "10:56"),
        ChatItem(BubbleKind.OTHER, "这个事情我有点不知道怎么说。"),
        ChatItem(BubbleKind.OTHER, "你先别急着回。"),
        ChatItem(BubbleKind.CENTER_UNKNOWN, "我也不知道这句应该算谁在说。")
    )
    MockScenario.LOW_EXPRESSION -> listOf(
        ChatItem(BubbleKind.TIME, "10:56"),
        ChatItem(BubbleKind.OTHER, "嗯"),
        ChatItem(BubbleKind.OTHER, "好"),
        ChatItem(BubbleKind.OTHER, "没事"),
        ChatItem(BubbleKind.OTHER, "忙"),
        ChatItem(BubbleKind.OTHER, "晚点说")
    )
}

private fun baseOtherThread(): List<ChatItem> = listOf(
    ChatItem(BubbleKind.TIME, "10:56"),
    ChatItem(BubbleKind.OTHER, "孩子跟着男方的。但是男方也提一个跟我离婚的之前要孩子的抚养费……"),
    ChatItem(BubbleKind.ME, "你们是离了婚才生的小孩吗？你刚刚说你离婚都10年了。"),
    ChatItem(BubbleKind.TIME, "11:00"),
    ChatItem(BubbleKind.OTHER, "是啊，我离婚是10年了呀。"),
    ChatItem(BubbleKind.TIME, "10:58"),
    ChatItem(BubbleKind.OTHER, "小孩之前跟着我在一起，在广州。在深圳。很多地方都去打工过……"),
    ChatItem(BubbleKind.IMAGE_OTHER, "图片占位"),
    ChatItem(BubbleKind.TIME, "10:59"),
    ChatItem(BubbleKind.OTHER, "本来我的过去我不想再提离婚，都10年了，孩子也舍不得……")
)
