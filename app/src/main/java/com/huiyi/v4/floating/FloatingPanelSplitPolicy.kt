package com.huiyi.v4.floating

import com.huiyi.v4.domain.cloud.CloudAnalysisTrace
import com.huiyi.v4.runtime.FloatingPanelMode
import com.huiyi.v4.runtime.NextSentencePendingCloudSessionPolicy

object FloatingPanelSplitPolicy {
    const val NEXT_SENTENCE_LABEL = "下一句"
    const val EXPRESS_SELF_LABEL = "表达我"
    const val FEEDBACK_LABEL = "这次不对，发给 GPT"
    const val HIDE_LABEL = "隐藏"

    val mainMenuLabels = listOf(NEXT_SENTENCE_LABEL, EXPRESS_SELF_LABEL, FEEDBACK_LABEL, HIDE_LABEL)
    val personaFeedbackLabels = listOf("像我", "不像我", "太油", "太重", "太空", "太像汇报", "可发")
    val characterArcDetailLabels = listOf("人物弧光", "本轮动作", "这句话展示了你的哪一面", "不要说过头")

    fun showsPersonaFeedback(mode: FloatingPanelMode): Boolean = false

    fun showsCharacterArcDetails(mode: FloatingPanelMode): Boolean = false

    fun showsExpressSelfEntry(mode: FloatingPanelMode): Boolean = mode == FloatingPanelMode.NEXT_SENTENCE

    fun blocksLocalRoutesWhileCloudPending(mode: FloatingPanelMode): Boolean = mode == FloatingPanelMode.NEXT_SENTENCE

    fun titleForNextSentence(cloudTrace: CloudAnalysisTrace?): String {
        val waitingForCloud = cloudTrace?.cloudErrorCode == NextSentencePendingCloudSessionPolicy.SOFT_TIMEOUT_PENDING
        return when {
            cloudTrace?.decisionSource == "CLOUD" -> "会意云端分析"
            cloudTrace?.decisionSource == "CLOUD_ENHANCED_PLAYBOOK" -> "会意云端分析"
            waitingForCloud -> "先等一下"
            else -> "先等一下"
        }
    }
}
