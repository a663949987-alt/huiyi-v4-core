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
import com.huiyi.v4.domain.pipeline.EvidencePackReportGenerator
import com.huiyi.v4.domain.pipeline.ParserReportGenerator
import com.huiyi.v4.domain.pipeline.ReplyAttemptFactory
import com.huiyi.v4.accessibility.HuiyiAccessibilityService
import com.huiyi.v4.domain.pipeline.EvidencePackFiles
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import com.huiyi.v4.ui.HuiyiDemoState
import com.huiyi.v4.ui.sampleState
import com.huiyi.v4.update.LanUpdateManager
import com.huiyi.v4.update.LanUpdateState
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
    val lastDebugExportPath: String? = null,
    val lastEvidenceJsonPath: String? = null,
    val lastPublicExportPath: String? = null,
    val lanUpdateState: LanUpdateState = LanUpdateState()
)

class HuiyiRuntime private constructor(
    private val appContext: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val persistence = HuiyiPersistenceRepository(DatabaseProvider.get(appContext).huiyiDao())
    private val updateManager = LanUpdateManager(appContext)
    private val prefs = appContext.getSharedPreferences("huiyi-runtime", Context.MODE_PRIVATE)
    private val pipeline = CurrentScreenPipelineUseCase(
        captureUseCase = CurrentScreenCaptureUseCase(),
        persistenceRepository = persistence
    )

    private val mutableState = MutableStateFlow(HuiyiRuntimeState())
    val state: StateFlow<HuiyiRuntimeState> = mutableState

    init {
        val savedUrl = prefs.getString("lan_update_url", "").orEmpty()
        mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(updateUrl = savedUrl)) }
    }

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
        val text = ParserReportGenerator().build(capture)
        val exported = PublicDownloadExporter(appContext)
            .exportText("current-screen-parser-report-for-gpt.md", text)
            .getOrElse { PublicDownloadExporter(appContext).fallbackToPrivate("current-screen-parser-report-for-gpt.md", text) }
        mutableState.update {
            it.copy(
                lastDebugExportPath = exported.privateFallbackFile?.absolutePath,
                lastPublicExportPath = exported.displayPath
            )
        }
        return exported.privateFallbackFile
    }

    fun exportRealDeviceEvidencePack(): Pair<File, File>? {
        val result = mutableState.value.latestPipelineResult ?: return null
        val generator = EvidencePackReportGenerator()
        val now = System.currentTimeMillis()
        val markdown = generator.buildMarkdown(result, HuiyiAccessibilityService.state.value, now)
        val json = generator.buildJson(result, HuiyiAccessibilityService.state.value, now)
        val exporter = PublicDownloadExporter(appContext)
        val markdownExport = exporter.exportText("real-device-current-screen-report-for-gpt.md", markdown)
            .getOrElse { exporter.fallbackToPrivate("real-device-current-screen-report-for-gpt.md", markdown) }
        val jsonExport = exporter.exportText("real-device-current-screen-report.json", json, "application/json")
            .getOrElse { exporter.fallbackToPrivate("real-device-current-screen-report.json", json) }
        val files = EvidencePackReportGenerator()
            .writeTo(File(appContext.filesDir, "debug"), result, HuiyiAccessibilityService.state.value)
            .getOrNull()
            ?: EvidencePackFiles(
                markdown = markdownExport.privateFallbackFile ?: File(appContext.filesDir, "debug/real-device-current-screen-report-for-gpt.md"),
                json = jsonExport.privateFallbackFile ?: File(appContext.filesDir, "debug/real-device-current-screen-report.json")
            )
        mutableState.update {
            it.copy(
                lastDebugExportPath = files.markdown.absolutePath,
                lastEvidenceJsonPath = files.json.absolutePath,
                lastPublicExportPath = markdownExport.displayPath + " / " + jsonExport.displayPath
            )
        }
        return files.markdown to files.json
    }

    fun exportTextDebug(name: String, text: String): File {
        val file = File(appContext.filesDir, "debug/$name")
        file.parentFile?.mkdirs()
        file.writeText(text, Charsets.UTF_8)
        mutableState.update { it.copy(lastDebugExportPath = file.absolutePath) }
        return file
    }

    fun setLanUpdateUrl(url: String) {
        prefs.edit().putString("lan_update_url", url).apply()
        mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(updateUrl = url, error = null)) }
    }

    fun checkLanUpdate() {
        scope.launch {
            val url = mutableState.value.lanUpdateState.updateUrl
            mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(status = "正在检查", error = null)) }
            updateManager.check(url).fold(
                onSuccess = { (manifest, raw) ->
                    mutableState.update {
                        it.copy(
                            lanUpdateState = it.lanUpdateState.copy(
                                latestManifest = manifest,
                                latestJsonRaw = raw,
                                status = "发现版本 ${manifest.versionName} (${manifest.versionCode})",
                                error = null
                            )
                        )
                    }
                },
                onFailure = { error ->
                    mutableState.update {
                        it.copy(lanUpdateState = it.lanUpdateState.copy(status = "检查失败", error = error.message))
                    }
                }
            )
        }
    }

    fun downloadLanUpdate() {
        scope.launch {
            val updateState = mutableState.value.lanUpdateState
            val manifest = updateState.latestManifest
            if (manifest == null) {
                mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(status = "请先检查更新", error = null)) }
                return@launch
            }
            mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(status = "正在下载", error = null)) }
            updateManager.download(updateState.updateUrl, manifest).fold(
                onSuccess = { file ->
                    mutableState.update {
                        it.copy(
                            lanUpdateState = it.lanUpdateState.copy(
                                downloadedApkPath = file.absolutePath,
                                status = "下载完成，等待安装确认",
                                error = null
                            )
                        )
                    }
                },
                onFailure = { error ->
                    mutableState.update {
                        it.copy(lanUpdateState = it.lanUpdateState.copy(status = "下载失败", error = error.message))
                    }
                }
            )
        }
    }

    fun openDownloadedUpdateInstaller() {
        val path = mutableState.value.lanUpdateState.downloadedApkPath
        if (path.isNullOrBlank()) {
            mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(status = "还没有下载 APK")) }
            return
        }
        updateManager.openInstaller(File(path)).fold(
            onSuccess = {
                mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(status = "已打开系统安装确认")) }
            },
            onFailure = { error ->
                mutableState.update { it.copy(lanUpdateState = it.lanUpdateState.copy(status = "打开安装失败", error = error.message)) }
            }
        )
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
