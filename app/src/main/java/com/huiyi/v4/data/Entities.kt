package com.huiyi.v4.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "message_nodes")
data class MessageNodeEntity(
    @PrimaryKey val id: String,
    val contactId: String?,
    val speaker: String,
    val contentJson: String,
    val normalizedText: String?,
    val source: String,
    val localSequence: Long,
    val confidence: Int,
    val createdAt: Long,
    val sceneId: String?
)

@Entity(tableName = "chat_scenes")
data class ChatSceneEntity(
    @PrimaryKey val id: String,
    val contactId: String?,
    val payloadJson: String,
    val completenessScore: Int,
    val createdAt: Long
)

@Entity(tableName = "reply_attempts")
data class ReplyAttemptEntity(
    @PrimaryKey val id: String,
    val contactId: String?,
    val sceneId: String,
    val routeId: String,
    val routeType: String,
    val suggestedText: String,
    val userAction: String,
    val status: String,
    val selectedAt: Long,
    val confirmedSentAt: Long?,
    val finalSentText: String?
)

@Entity(tableName = "reply_outcomes")
data class ReplyOutcomeEntity(
    @PrimaryKey val id: String,
    val attemptId: String,
    val contactId: String?,
    val replied: Boolean,
    val replyDelaySeconds: Long?,
    val outcomeScore: Int,
    val outcomeLabel: String
)

@Entity(tableName = "user_persona_corpus")
data class UserPersonaCorpusEntity(
    @PrimaryKey val id: String,
    val name: String,
    val enabled: Boolean,
    val payloadJson: String,
    val updatedAt: Long
)

@Entity(tableName = "update_cache")
data class UpdateCacheEntity(
    @PrimaryKey val id: String = "latest",
    val manifestJson: String?,
    val updateBaseUrl: String?,
    val downloadedApkPath: String?,
    val downloadStatus: String,
    val errorLog: String?,
    val updatedAt: Long
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long
)

@Entity(
    tableName = "light_listen_messages",
    indices = [Index(value = ["contactKey", "observedAt"])]
)
data class LightListenMessageEntity(
    @PrimaryKey val id: String,
    val contactKey: String,
    val appPackage: String,
    val windowTitle: String?,
    val speaker: String,
    val contentType: String,
    val text: String?,
    val source: String,
    val observedAt: Long,
    val localSequence: Long,
    val confidence: Int,
    val speakerConfidence: Int,
    val contentConfidence: Int,
    val cloudHistoryFormatJson: String?,
    val createdAt: Long
)
