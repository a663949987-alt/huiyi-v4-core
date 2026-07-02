package com.huiyi.v4.domain.modelprovider

import com.huiyi.v4.domain.model.ChatSceneContext
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.TacticalDecision

interface TacticalModelProvider {
    suspend fun generateTacticalReply(input: TacticalPromptInput): TacticalReplyResult
}

data class TacticalPromptInput(
    val context: ChatSceneContext,
    val decision: TacticalDecision
)

data class TacticalReplyResult(
    val decision: TacticalDecision,
    val routes: List<ReplyRoute>,
    val providerName: String,
    val apiCalled: Boolean
)
