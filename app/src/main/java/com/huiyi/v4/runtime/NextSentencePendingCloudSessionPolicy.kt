package com.huiyi.v4.runtime

import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineResult

object NextSentencePendingCloudSessionPolicy {
    const val SOFT_TIMEOUT_PENDING = "SOFT_TIMEOUT_PENDING"

    fun shouldResumePendingSession(
        result: CurrentScreenPipelineResult?,
        activeSessionId: String?
    ): Boolean {
        val sessionId = result?.sessionId ?: return false
        if (activeSessionId.isNullOrBlank()) return false
        return sessionId == activeSessionId &&
            result.cloudTrace.cloudErrorCode == SOFT_TIMEOUT_PENDING &&
            result.cloudTrace.cloudRequestActuallySent &&
            !result.cloudTrace.cloudSuccess
    }
}
