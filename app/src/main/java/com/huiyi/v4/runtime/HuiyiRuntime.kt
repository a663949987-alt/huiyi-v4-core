package com.huiyi.v4.runtime

import android.content.Context
import com.huiyi.v4.data.DatabaseProvider
import com.huiyi.v4.data.HuiyiPersistenceRepository
import com.huiyi.v4.domain.capture.ManualContextCaptureSession
import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.ReplyAttempt
import com.huiyi.v4.domain.model.ReplyAttemptStatus
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.UserAction
import com.huiyi.v4.domain.model.UserPersonaCorpus
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureUseCase
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineResult
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineUseCase
import com.huiyi.v4.domain.pipeline.ParserReportGenerator
import com.huiyi.v4.domain.pipeline.ReplyAttemptFactory
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import com.huiyi.v4.ui.HuiyiDemoState
import com.huiyi.v4.ui.sampleState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class HuiyiRuntimeState(
    val demoState: HuiyiDemoState = sampleState(),
    val latestPipelineResult: CurrentScreenPipelineResult? = null,
    val panelVisible: Boolean = false,
    val lastError: String? = null,
    val lastDebugExportPath: String? = null
)

class HuiyiRuntime private constructor(
    private val appContext: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val persistence = HuiyiPersistenceRepository(DatabaseProvider.get(appContext).huiyiDao())
    private val pipeline = CurrentScreenPipelineUseCase(
        captureUseCase = CurrentScreenCaptureUseCase(),
        persistenceRepository = persistence
    )

    private val mutableState = MutableStateFlow(HuiyiRuntimeState())
    val state: StateFlow<HuiyiRuntimeState> = mutableState

    fun setPanelVisible(visible: Boolean) {
        mutableState.update { it.copy(panelVisible = visible) }
    }

    fun togglePersona() {
        mutableState.update { it.copy(demoState = it.demoState.togglePersona()) }
    }

    fun runNextSentence() {
        scope.launch {
            val persona = currentPersona()
            val result = pipeline.run(persona)
            result.fold(
                onSuccess = { pipelineResult ->
                    val messages = pipelineResult.context?.currentScreenMessages ?: mutableState.value.demoState.messages
                    mutableState.update {
                        it.copy(
                            demoState = it.demoState.copy(messages = messages),
                            latestPipelineResult = pipelineResult,
                            panelVisible = true,
                            lastError = pipelineResult.persistenceError,
                        )
                    }
                },
                onFailure = { error ->
                    mutableState.update { it.copy(panelVisible = true, lastError = error.message ?: "当前屏幕链路失败") }
                }
            )
        }
    }

    fun applyVoiceSummary(summary: String) {
        val old = mutableState.value
        val updatedState = old.demoState.withVoiceSummary(summary)
        val context = ContextAssembler().assemble(updatedState.messages, userPersonaCorpus = currentPersona())
        val decision = TacticalDecisionEngine().decide(context)
        val routes = if (decision.decisionType.name == "WAIT") emptyList() else ReplyRouteGenerator().generate(context, decision)
        val result = CurrentScreenPipelineResult(
            captureResult = old.latestPipelineResult?.captureResult,
            context = context,
            lastSpeakerDecision = old.latestPipelineResult?.lastSpeakerDecision
                ?: com.huiyi.v4.domain.pipeline.LastSpeakerDecisionUseCase().decide(updatedState.messages),
            tacticalDecision = decision,
            routes = routes,
            apiCalled = false
        )
        mutableState.update {
            it.copy(
                demoState = updatedState,
                latestPipelineResult = result,
                panelVisible = true,
                lastError = null
            )
        }
    }

    fun createCopiedAttempt(route: ReplyRoute) {
        val sceneId = mutableState.value.latestPipelineResult?.context?.id ?: "local-scene"
        val attempt = ReplyAttemptFactory().copied(
            route = route,
            sceneId = sceneId,
            contactId = mutableState.value.latestPipelineResult?.context?.contactId
        )
        scope.launch {
            val error = persistence.saveReplyAttempt(attempt).exceptionOrNull()?.message
            mutableState.update { it.copy(lastError = error) }
        }
    }

    fun exportParserReport(): File? {
        val capture = mutableState.value.latestPipelineResult?.captureResult ?: return null
        val file = File(appContext.filesDir, "debug/current-screen-parser-report-for-gpt.md")
        val written = ParserReportGenerator().writeTo(file, capture).getOrNull()
        mutableState.update { it.copy(lastDebugExportPath = written?.absolutePath) }
        return written
    }

    fun exportTextDebug(name: String, text: String): File {
        val file = File(appContext.filesDir, "debug/$name")
        file.parentFile?.mkdirs()
        file.writeText(text, Charsets.UTF_8)
        mutableState.update { it.copy(lastDebugExportPath = file.absolutePath) }
        return file
    }

    private fun currentPersona(): UserPersonaCorpus = DefaultPersonaCorpus.soldier(mutableState.value.demoState.personaEnabled)

    companion object {
        @Volatile
        private var instance: HuiyiRuntime? = null

        fun get(context: Context): HuiyiRuntime {
            return instance ?: synchronized(this) {
                instance ?: HuiyiRuntime(context.applicationContext).also { instance = it }
            }
        }
    }
}
