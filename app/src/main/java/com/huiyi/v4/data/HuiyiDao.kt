package com.huiyi.v4.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface HuiyiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessageNodes(nodes: List<MessageNodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatScene(scene: ChatSceneEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplyAttempt(attempt: ReplyAttemptEntity)
}
