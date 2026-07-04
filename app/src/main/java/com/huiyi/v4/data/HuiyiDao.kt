package com.huiyi.v4.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HuiyiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessageNodes(nodes: List<MessageNodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatScene(scene: ChatSceneEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplyAttempt(attempt: ReplyAttemptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSetting(setting: AppSettingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLightListenMessages(messages: List<LightListenMessageEntity>)

    @Query("SELECT * FROM light_listen_messages WHERE contactKey = :contactKey ORDER BY observedAt ASC, localSequence ASC LIMIT :limit")
    suspend fun getLightListenMessagesForContact(contactKey: String, limit: Int): List<LightListenMessageEntity>

    @Query("DELETE FROM light_listen_messages WHERE observedAt < :cutoff")
    suspend fun deleteLightListenMessagesOlderThan(cutoff: Long)
}
