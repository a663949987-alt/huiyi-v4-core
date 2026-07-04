package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.accessibility.CurrentScreenSnapshot

data class NextSentenceSessionBinding(
    val activeSessionId: String,
    val activeSnapshotId: String,
    val activeChatPackage: String,
    val activeChatWindowHash: String,
    val panelSessionId: String? = null
)

data class CloudResponseBinding(
    val sessionId: String,
    val preAnalysisSnapshotId: String,
    val chatPackage: String,
    val chatWindowHash: String
)

data class CloudResponseGateDecision(
    val allowed: Boolean,
    val discardedReason: String? = null
)

object NextSentenceSessionGate {
    const val STALE_SESSION = "STALE_SESSION"
    const val SNAPSHOT_CHANGED = "SNAPSHOT_CHANGED"
    const val CHAT_CONTEXT_CHANGED = "CHAT_CONTEXT_CHANGED"
    const val PANEL_SESSION_MISMATCH = "PANEL_SESSION_MISMATCH"

    fun evaluate(
        active: NextSentenceSessionBinding,
        response: CloudResponseBinding
    ): CloudResponseGateDecision {
        if (response.sessionId != active.activeSessionId) {
            return CloudResponseGateDecision(false, STALE_SESSION)
        }
        if (response.preAnalysisSnapshotId != active.activeSnapshotId) {
            return CloudResponseGateDecision(false, SNAPSHOT_CHANGED)
        }
        if (response.chatPackage != active.activeChatPackage ||
            response.chatWindowHash != active.activeChatWindowHash
        ) {
            return CloudResponseGateDecision(false, CHAT_CONTEXT_CHANGED)
        }
        if (active.panelSessionId != null && active.panelSessionId != response.sessionId) {
            return CloudResponseGateDecision(false, PANEL_SESSION_MISMATCH)
        }
        return CloudResponseGateDecision(true)
    }
}

object NextSentenceSnapshotIdentity {
    fun snapshotId(snapshot: CurrentScreenSnapshot): String {
        val packageName = snapshot.appPackage.orEmpty()
        val title = snapshot.windowTitle.orEmpty()
        val textHash = snapshot.nodes
            .asSequence()
            .mapNotNull { it.readableText }
            .take(40)
            .joinToString("|")
            .hashCode()
        return "$packageName:${snapshot.capturedAt}:$title:$textHash"
    }

    fun chatWindowHash(snapshot: CurrentScreenSnapshot): String {
        val packageName = snapshot.appPackage.orEmpty()
        val title = snapshot.windowTitle.orEmpty()
        val boundsAndText = snapshot.nodes
            .asSequence()
            .filter { it.visibleToUser }
            .take(80)
            .joinToString("|") { node ->
                val bounds = node.bounds
                "${node.className}:${bounds.left},${bounds.top},${bounds.right},${bounds.bottom}:${node.readableText.orEmpty().take(40)}"
            }
        return "$packageName:$title:$boundsAndText".hashCode().toString()
    }
}
