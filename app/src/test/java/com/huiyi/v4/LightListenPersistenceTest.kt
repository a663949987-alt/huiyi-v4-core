package com.huiyi.v4

import com.huiyi.v4.data.AppSettingEntity
import com.huiyi.v4.data.ChatSceneEntity
import com.huiyi.v4.data.HuiyiDao
import com.huiyi.v4.data.HuiyiPersistenceRepository
import com.huiyi.v4.data.LightListenMessageEntity
import com.huiyi.v4.data.MessageNodeEntity
import com.huiyi.v4.data.ReplyAttemptEntity
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.Speaker
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LightListenPersistenceTest {
    @Test
    fun LightListenMessagesPersistAsTimelineWithCloudHistoryFormatTest() = runTest {
        val dao = FakeDao()
        val repository = HuiyiPersistenceRepository(dao)
        val later = textNode("later", Speaker.OTHER, "later text", 20)
            .copy(source = MessageSource.ACCESSIBILITY_LIGHT_LISTEN, createdAt = 2_000L)
        val earlier = textNode("earlier", Speaker.ME, "earlier text", 10)
            .copy(source = MessageSource.ACCESSIBILITY_LIGHT_LISTEN, createdAt = 1_000L)

        repository.saveLightListenMessages(
            appPackage = "chat.app",
            windowTitle = "Alice",
            messages = listOf(later, earlier),
            observedAt = 3_000L
        ).getOrThrow()

        assertEquals(listOf("earlier", "later"), dao.lightMessages.map { it.id })
        assertEquals("chat.app|Alice", dao.lightMessages.first().contactKey)
        assertEquals(3_000L, dao.lightMessages.first().observedAt)
        assertTrue(dao.lightMessages.first().cloudHistoryFormatJson.orEmpty().contains("huiyi-history-message-v1"))
        assertTrue(dao.lightMessages.first().cloudHistoryFormatJson.orEmpty().contains("cannotOverrideCurrentScreenshot"))
        assertTrue(dao.deletedCutoff != 0L)
    }

    private class FakeDao : HuiyiDao {
        val lightMessages = mutableListOf<LightListenMessageEntity>()
        var deletedCutoff: Long = 0L

        override suspend fun insertMessageNodes(nodes: List<MessageNodeEntity>) = Unit
        override suspend fun insertChatScene(scene: ChatSceneEntity) = Unit
        override suspend fun insertReplyAttempt(attempt: ReplyAttemptEntity) = Unit
        override suspend fun insertAppSetting(setting: AppSettingEntity) = Unit

        override suspend fun insertLightListenMessages(messages: List<LightListenMessageEntity>) {
            lightMessages.clear()
            lightMessages += messages
        }

        override suspend fun getLightListenMessagesForContact(contactKey: String, limit: Int): List<LightListenMessageEntity> =
            lightMessages.filter { it.contactKey == contactKey }.take(limit)

        override suspend fun deleteLightListenMessagesOlderThan(cutoff: Long) {
            deletedCutoff = cutoff
        }
    }
}
