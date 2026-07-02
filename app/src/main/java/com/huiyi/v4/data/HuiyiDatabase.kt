package com.huiyi.v4.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MessageNodeEntity::class,
        ChatSceneEntity::class,
        ReplyAttemptEntity::class,
        ReplyOutcomeEntity::class,
        UserPersonaCorpusEntity::class,
        UpdateCacheEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class HuiyiDatabase : RoomDatabase()
