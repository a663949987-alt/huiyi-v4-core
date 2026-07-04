package com.huiyi.v4.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MessageNodeEntity::class,
        ChatSceneEntity::class,
        ReplyAttemptEntity::class,
        ReplyOutcomeEntity::class,
        UserPersonaCorpusEntity::class,
        UpdateCacheEntity::class,
        AppSettingEntity::class,
        LightListenMessageEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class HuiyiDatabase : RoomDatabase() {
    abstract fun huiyiDao(): HuiyiDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `light_listen_messages` (
                        `id` TEXT NOT NULL,
                        `contactKey` TEXT NOT NULL,
                        `appPackage` TEXT NOT NULL,
                        `windowTitle` TEXT,
                        `speaker` TEXT NOT NULL,
                        `contentType` TEXT NOT NULL,
                        `text` TEXT,
                        `source` TEXT NOT NULL,
                        `observedAt` INTEGER NOT NULL,
                        `localSequence` INTEGER NOT NULL,
                        `confidence` INTEGER NOT NULL,
                        `speakerConfidence` INTEGER NOT NULL,
                        `contentConfidence` INTEGER NOT NULL,
                        `cloudHistoryFormatJson` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_light_listen_messages_contactKey_observedAt` " +
                        "ON `light_listen_messages` (`contactKey`, `observedAt`)"
                )
            }
        }
    }
}
