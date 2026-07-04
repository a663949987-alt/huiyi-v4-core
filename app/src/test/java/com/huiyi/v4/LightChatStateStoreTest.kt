package com.huiyi.v4

import com.huiyi.v4.domain.context.LightChatStateStore
import com.huiyi.v4.domain.context.NextMoveType
import com.huiyi.v4.domain.model.MessageDeliveryStatus
import com.huiyi.v4.domain.model.MessageStatusArtifact
import com.huiyi.v4.domain.model.MetadataType
import com.huiyi.v4.domain.model.Speaker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LightChatStateStoreTest {
    @Test
    fun StableSnapshotKeepsRecentSummarySessionBindingAndSafetyFlagsTest() {
        val messages = (1..14).map { index ->
            textNode(
                id = "m$index",
                speaker = if (index % 2 == 0) Speaker.ME else Speaker.OTHER,
                text = "message $index",
                sequence = index.toLong()
            )
        }
        val readStatus = textNode("status", Speaker.SYSTEM, "read", 15).copy(
            isEffectiveChatMessage = false,
            metadataType = MetadataType.READ_RECEIPT,
            attachedDeliveryStatus = MessageDeliveryStatus.READ,
            attachedReadStatus = MessageDeliveryStatus.READ,
            statusArtifact = MessageStatusArtifact(
                id = "status-read",
                status = MessageDeliveryStatus.READ,
                rawTextRedacted = "read",
                contentDescriptionRedacted = null,
                stateDescriptionRedacted = null,
                bounds = null,
                source = "test",
                attachedToMessageId = "m14",
                confidence = 90,
                reason = "read_receipt_metadata"
            )
        )

        val snapshot = LightChatStateStore().buildStableSnapshot(
            appPackage = "chat.app",
            windowTitle = "Alice",
            messages = messages + readStatus,
            capturedAt = 2_000L,
            nextSentenceSessionId = "session-1",
            preAnalysisSnapshotId = "snapshot-1",
            panelSessionId = "session-1",
            chatWindowHash = "hash-1"
        )

        assertEquals("chat.app|Alice", snapshot.chatKey)
        assertEquals(12, snapshot.recentEffectiveMessages.size)
        assertEquals("message 14", snapshot.lastUserMessage?.text)
        assertEquals("message 13", snapshot.lastOtherMessage?.text)
        assertEquals(1, snapshot.messageStatusMetadata.size)
        assertEquals("session-1", snapshot.sessionBinding.nextSentenceSessionId)
        assertEquals("snapshot-1", snapshot.sessionBinding.preAnalysisSnapshotId)
        assertFalse(snapshot.safetyFlags.longTermRawChatStorage)
        assertFalse(snapshot.safetyFlags.autoSend)
        assertFalse(snapshot.safetyFlags.rawPrivateChatUploadedToGithub)
    }

    @Test
    fun LastOtherFutureTopicExposesSelfExpressionHookWithoutChangingPipelineTest() {
        val snapshot = LightChatStateStore().buildStableSnapshot(
            appPackage = "chat.app",
            windowTitle = "Alice",
            messages = listOf(
                textNode("me", Speaker.ME, "I hear you", 1),
                textNode("other", Speaker.OTHER, "I care about our future and a stable plan.", 2)
            ),
            matchedPersonaCardIds = listOf("persona-stable-relationship")
        )

        assertTrue(snapshot.selfExpressionOpportunity.exists)
        assertTrue(
            snapshot.selfExpressionOpportunity.type == NextMoveType.EXPRESS_SELF ||
                snapshot.selfExpressionOpportunity.type == NextMoveType.CO_CREATE_MEANING
        )
        assertEquals(listOf("persona-stable-relationship"), snapshot.selfExpressionOpportunity.matchedPersonaCardIds)
    }
}
