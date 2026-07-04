package com.huiyi.v4.domain.context

import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.Speaker
import kotlin.math.absoluteValue

data class LightListenWindowStats(
    val chatKey: String,
    val storedMessageCount: Int,
    val lastUpdatedAt: Long
)

class LightListenMemory(
    private val maxMessagesPerChat: Int = 80,
    private val maxAgeMs: Long = 30 * 60 * 1000L
) {
    private data class Entry(
        val fingerprint: String,
        val fuzzyFingerprint: String,
        val message: MessageNode,
        val firstSeenAt: Long,
        val lastSeenAt: Long
    )

    private val windows = linkedMapOf<String, MutableList<Entry>>()

    @Synchronized
    fun ingest(
        appPackage: String?,
        windowTitle: String?,
        messages: List<MessageNode>,
        capturedAt: Long = System.currentTimeMillis()
    ): LightListenWindowStats? {
        val key = chatKey(appPackage, windowTitle) ?: return null
        prune(capturedAt)
        val entries = windows.getOrPut(key) { mutableListOf() }
        messages.asSequence()
            .filter { it.isEffectiveChatMessage }
            .filter { it.speaker == Speaker.ME || it.speaker == Speaker.OTHER }
            .filter { it.normalizedText?.isNotBlank() == true || it.content !is MessageContent.Text }
            .forEach { message ->
                val fingerprint = fingerprint(message)
                val existingIndex = entries.indexOfLast { it.fingerprint == fingerprint }
                val lightMessage = message.asLightListenMessage(key, fingerprint, capturedAt)
                if (existingIndex >= 0) {
                    val existing = entries[existingIndex]
                    entries[existingIndex] = existing.copy(
                        message = lightMessage.copy(id = existing.message.id),
                        lastSeenAt = capturedAt
                    )
                } else {
                    entries += Entry(
                        fingerprint = fingerprint,
                        fuzzyFingerprint = fuzzyFingerprint(message),
                        message = lightMessage,
                        firstSeenAt = capturedAt,
                        lastSeenAt = capturedAt
                    )
                }
            }
        while (entries.size > maxMessagesPerChat) entries.removeAt(0)
        return LightListenWindowStats(key, entries.size, capturedAt)
    }

    @Synchronized
    fun backfillFor(
        appPackage: String?,
        windowTitle: String?,
        currentMessages: List<MessageNode>,
        maxCount: Int = 24,
        now: Long = System.currentTimeMillis()
    ): List<MessageNode> {
        val key = chatKey(appPackage, windowTitle) ?: return emptyList()
        prune(now)
        val currentFingerprints = currentMessages.mapTo(mutableSetOf()) { fuzzyFingerprint(it) }
        val entries = windows[key].orEmpty()
            .filter { now - it.lastSeenAt <= maxAgeMs }
            .filterNot { it.fuzzyFingerprint in currentFingerprints }
            .takeLast(maxCount)
        return entries.mapIndexed { index, entry ->
            entry.message.copy(
                source = MessageSource.ACCESSIBILITY_LIGHT_LISTEN,
                localSequence = -10_000L + index,
                createdAt = entry.firstSeenAt
            )
        }
    }

    @Synchronized
    fun statsFor(appPackage: String?, windowTitle: String?, now: Long = System.currentTimeMillis()): LightListenWindowStats? {
        val key = chatKey(appPackage, windowTitle) ?: return null
        prune(now)
        val entries = windows[key] ?: return null
        return LightListenWindowStats(key, entries.size, entries.maxOfOrNull { it.lastSeenAt } ?: 0L)
    }

    @Synchronized
    fun clear() {
        windows.clear()
    }

    private fun prune(now: Long) {
        val iterator = windows.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value.removeAll { now - it.lastSeenAt > maxAgeMs }
            if (entry.value.isEmpty()) iterator.remove()
        }
    }

    private fun MessageNode.asLightListenMessage(chatKey: String, fingerprint: String, capturedAt: Long): MessageNode {
        val idHash = "${chatKey}|$fingerprint".hashCode().absoluteValue.toString(16)
        return copy(
            id = "light-$idHash",
            source = MessageSource.ACCESSIBILITY_LIGHT_LISTEN,
            createdAt = capturedAt
        )
    }

    private fun chatKey(appPackage: String?, windowTitle: String?): String? {
        val pkg = appPackage?.trim().orEmpty()
        if (pkg.isBlank()) return null
        val title = windowTitle?.trim()
            ?.replace(Regex("\\s+"), " ")
            ?.take(80)
            .orEmpty()
            .ifBlank { "default" }
        return "$pkg|$title"
    }

    private fun fingerprint(message: MessageNode): String {
        val boundsPart = listOfNotNull(
            message.inferredSide,
            message.bubbleBounds?.top?.div(12)?.toString(),
            message.bubbleBounds?.bottom?.div(12)?.toString(),
            message.textBounds?.top?.div(12)?.toString()
        ).joinToString(":")
        return "${fuzzyFingerprint(message)}|$boundsPart"
    }

    private fun fuzzyFingerprint(message: MessageNode): String =
        "${message.speaker.name}|${contentKey(message)}"

    private fun contentKey(message: MessageNode): String = when (val content = message.content) {
        is MessageContent.Text -> content.text.trim().replace(Regex("\\s+"), " ").take(160)
        is MessageContent.Voice -> "voice:${content.durationSeconds ?: -1}:${content.transcriptText.orEmpty()}:${content.userSummary.orEmpty()}"
        is MessageContent.Image -> "image:${content.descriptionText.orEmpty()}"
        is MessageContent.Video -> "video:${content.durationSeconds ?: -1}:${content.descriptionText.orEmpty()}"
        is MessageContent.Sticker -> "sticker:${content.meaningText.orEmpty()}"
    }
}
