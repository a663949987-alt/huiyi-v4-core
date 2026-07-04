package com.huiyi.v4

import com.huiyi.v4.domain.cloud.CloudAnalysisTrace
import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.review.ChatReviewMemoryBuilder
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatReviewMemoryBuilderTest {
    @Test
    fun ChatReviewDraftGroupsByContactAndKeepsAnalysisOffForegroundTest() {
        val context = ContextAssembler().assemble(
            currentScreenMessages = listOf(
                textNode("m1", Speaker.ME, "怎么了", 1),
                textNode("m2", Speaker.OTHER, "今天有点累", 2),
                textNode("m3", Speaker.ME, "慢慢说", 3),
                textNode("m4", Speaker.OTHER, "不想聊太复杂", 4)
            )
        )
        val decision = TacticalDecisionEngine().decide(context)
        val routes = ReplyRouteGenerator().generate(context, decision)

        val draft = ChatReviewMemoryBuilder.build(
            context = context,
            appPackage = "com.example.chat",
            windowTitle = "白云蓝天",
            decision = decision,
            routes = routes,
            cloudTrace = CloudAnalysisTrace.success(
                com.huiyi.v4.domain.cloud.CloudAnalysisConfig(cloudEnabled = true, endpoint = "https://relay.example"),
                com.huiyi.v4.domain.cloud.CloudAnalysisOutput(
                    sessionId = "s",
                    preAnalysisSnapshotId = "snap",
                    chatPackage = "com.example.chat",
                    chatWindowHash = "hash",
                    cloudRequestId = "req",
                    decision = decision,
                    routes = routes,
                    latencyMs = 10,
                    modelUsed = "gpt-5.4",
                    primaryModel = "gpt-5.4"
                )
            ),
            now = 100L
        )

        assertEquals("test-contact", draft.contactKey)
        assertEquals("白云蓝天", draft.contactDisplayName)
        assertEquals("gpt-5.4", draft.modelSource)
        assertEquals(routes.size, draft.routeCount)
        assertTrue(draft.profileHints.isNotEmpty())
    }
}
