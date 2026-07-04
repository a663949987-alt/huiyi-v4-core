package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.accessibility.CurrentScreenSnapshot
import com.huiyi.v4.accessibility.HuiyiAccessibilityService
import com.huiyi.v4.accessibility.ScreenNodeSnapshot
import com.huiyi.v4.domain.capture.GenericVisualBubbleParser
import com.huiyi.v4.domain.capture.LiaoqiRealParser
import com.huiyi.v4.domain.capture.VisualTruthAligner
import com.huiyi.v4.domain.capture.VisualBubble
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.Speaker

data class CurrentScreenCaptureResult(
    val snapshot: CurrentScreenSnapshot,
    val messages: List<MessageNode>,
    val sampleSource: SampleSource,
    val warning: String? = null,
    val parserName: String = "GenericVisualBubbleParser",
    val parserFallbackUsed: Boolean = false,
    val accessibilityBoundsProjected: Boolean = false,
    val ocrUsed: Boolean = false,
    val visualTruthAvailable: Boolean = false,
    val visualConflictCount: Int = 0,
    val visualSpeakerFallbackCount: Int = 0,
    val captureSource: NextSentenceCaptureSource = NextSentenceCaptureSource.CURRENT_ROOT,
    val usedFallbackSnapshot: Boolean = false,
    val lastStableSnapshotAgeMs: Long? = null,
    val lastStableSnapshotPackage: String? = null,
    val currentRootPackageAtCapture: String? = null,
    val rootIsOwnOverlay: Boolean = false,
    val rootIsSystemUi: Boolean = false,
    val rootRetryCount: Int = 0,
    val rootAvailableFirstTry: Boolean = true,
    val rootAvailableAfterRetry: Boolean = true,
    val rawNodeCount: Int = snapshot.nodes.size,
    val visibleTextCount: Int = snapshot.nodes.count { it.readableText?.isNotBlank() == true }
)

open class CurrentScreenCaptureUseCase(
    private val serviceProvider: () -> HuiyiAccessibilityService? = { HuiyiAccessibilityService.instance }
) {
    open fun capture(): Result<CurrentScreenCaptureResult> {
        val sessionId = java.util.UUID.randomUUID().toString()
        val startedAt = System.currentTimeMillis()
        val service = serviceProvider() ?: return Result.failure(
            failure(
                trace = NextSentenceSessionTrace(
                    sessionId = sessionId,
                    startedAt = startedAt,
                    stage = NextSentenceStage.ACCESSIBILITY_STATE_CHECKED,
                    serviceConnected = false,
                    primaryCapturePath = "NONE",
                    nodeTreeAttempted = false
                ),
                code = NextSentenceErrorCode.ACCESSIBILITY_SERVICE_NOT_CONNECTED,
                stage = NextSentenceStage.ACCESSIBILITY_STATE_CHECKED
            )
        )
        val ownPackage = service.packageName
        val firstRootPackage = service.currentRootPackageName()
        val firstTryAvailable = firstRootPackage != null
        var retryCount = 0
        var snapshotResult = service.captureCurrentScreen()
        while (snapshotResult.isFailure && retryCount < 2) {
            retryCount += 1
            Thread.sleep(if (retryCount == 1) 250L else 500L)
            snapshotResult = service.captureCurrentScreen()
        }
        val afterRetryPackage = service.currentRootPackageName()
        val fallback = service.lastStableChatSnapshot()
        val fallbackAgeMs = fallback?.let { System.currentTimeMillis() - it.capturedAt }
        val baseTrace = NextSentenceSessionTrace(
            sessionId = sessionId,
            startedAt = startedAt,
            stage = NextSentenceStage.NODE_TREE_CAPTURE_STARTED,
            activePackageAtCaptureStart = firstRootPackage,
            activePackageAfterRootRetry = afterRetryPackage,
            rootPackageAtCaptureStart = firstRootPackage,
            rootAvailableFirstTry = firstTryAvailable,
            rootRetryCount = retryCount,
            rootAvailableAfterRetry = snapshotResult.isSuccess,
            lastStableSnapshotAgeMs = fallbackAgeMs,
            lastStableSnapshotPackage = fallback?.packageName,
            primaryCapturePath = "NODE_TREE",
            nodeTreeAttempted = true,
            nodeTreeSuccess = snapshotResult.isSuccess,
            fallbackSnapshotAttempted = true,
            fallbackSnapshotSuccess = false
        )
        var snapshot = snapshotResult.getOrNull()
        var appPackage = snapshot?.appPackage.orEmpty()
        while (snapshot != null && (appPackage == ownPackage || appPackage == "com.android.systemui") && retryCount < 4) {
            retryCount += 1
            Thread.sleep(if (appPackage == ownPackage) 700L else 300L)
            val settledSnapshot = service.captureCurrentScreen().getOrNull()
            if (settledSnapshot != null) {
                snapshot = settledSnapshot
                appPackage = settledSnapshot.appPackage.orEmpty()
            }
        }
        if (snapshot == null) {
            val fallbackCapture = fallback?.takeIf {
                fallbackAgeMs != null &&
                    fallbackAgeMs <= STABLE_CHAT_SNAPSHOT_MAX_AGE_MS &&
                    it.normalizedMessages.isNotEmpty()
            }
            if (fallbackCapture != null) {
                return Result.success(fromSnapshot(
                    snapshot = fallbackCapture.snapshot,
                    captureSource = NextSentenceCaptureSource.LAST_STABLE_CHAT_SNAPSHOT,
                    usedFallbackSnapshot = true,
                    lastStableSnapshotAgeMs = fallbackAgeMs,
                    lastStableSnapshotPackage = fallbackCapture.packageName,
                    currentRootPackageAtCapture = afterRetryPackage,
                    rootRetryCount = retryCount,
                    rootAvailableFirstTry = firstTryAvailable,
                    rootAvailableAfterRetry = false
                ))
            }
            return Result.failure(
                failure(
                    trace = baseTrace.copy(captureSource = NextSentenceCaptureSource.NONE, primaryCapturePath = "NONE"),
                    code = NextSentenceErrorCode.ROOT_UNAVAILABLE,
                    stage = NextSentenceStage.NODE_TREE_CAPTURE_RETRYING,
                    cause = snapshotResult.exceptionOrNull()
                )
            )
        }
        val rootIsOwnOverlay = appPackage == ownPackage
        val rootIsSystemUi = appPackage == "com.android.systemui"
        val fallbackCapture = fallback?.takeIf {
            fallbackAgeMs != null &&
                fallbackAgeMs <= STABLE_CHAT_SNAPSHOT_MAX_AGE_MS &&
                it.normalizedMessages.isNotEmpty()
        }
        if ((rootIsOwnOverlay || rootIsSystemUi) && fallbackCapture != null) {
            return Result.success(fromSnapshot(
                snapshot = fallbackCapture.snapshot,
                captureSource = NextSentenceCaptureSource.LAST_STABLE_CHAT_SNAPSHOT,
                usedFallbackSnapshot = true,
                lastStableSnapshotAgeMs = fallbackAgeMs,
                lastStableSnapshotPackage = fallbackCapture.packageName,
                currentRootPackageAtCapture = appPackage,
                rootIsOwnOverlay = rootIsOwnOverlay,
                    rootIsSystemUi = rootIsSystemUi,
                    rootRetryCount = retryCount,
                    rootAvailableFirstTry = firstTryAvailable,
                    rootAvailableAfterRetry = true
            ))
        }
        if (rootIsOwnOverlay || rootIsSystemUi) {
            return Result.failure(
                failure(
                    trace = baseTrace.copy(
                        rootPackageName = appPackage,
                        rootClassName = service.currentRootClassName(),
                        rootWindowTitle = snapshot.windowTitle,
                        rootIsOwnOverlay = rootIsOwnOverlay,
                        rootIsSystemUi = rootIsSystemUi,
                        captureSource = NextSentenceCaptureSource.NONE,
                        primaryCapturePath = "NONE",
                        nodeTreeAttempted = true,
                        nodeTreeSuccess = false,
                        fallbackSnapshotAttempted = true,
                        fallbackSnapshotSuccess = false
                    ),
                    code = if (rootIsOwnOverlay) NextSentenceErrorCode.ROOT_IS_OWN_OVERLAY else NextSentenceErrorCode.ROOT_IS_SYSTEM_UI,
                    stage = NextSentenceStage.NODE_TREE_CAPTURED
                )
            )
        }
        return runCatching {
            fromSnapshot(
                snapshot = snapshot,
                captureSource = NextSentenceCaptureSource.CURRENT_ROOT,
                usedFallbackSnapshot = false,
                lastStableSnapshotAgeMs = fallbackAgeMs,
                lastStableSnapshotPackage = fallback?.packageName,
                currentRootPackageAtCapture = appPackage,
                rootRetryCount = retryCount,
                rootAvailableFirstTry = firstTryAvailable,
                rootAvailableAfterRetry = true
            )
        }.recoverCatching { error ->
            if (error is NextSentenceException) throw error
            throw failure(
                trace = baseTrace.copy(rootPackageName = appPackage, rootWindowTitle = snapshot.windowTitle),
                code = NextSentenceErrorCode.UNKNOWN_EXCEPTION,
                stage = NextSentenceStage.CHAT_MESSAGES_PARSED,
                cause = error
            )
        }
    }

    private fun fromSnapshot(
        snapshot: CurrentScreenSnapshot,
        captureSource: NextSentenceCaptureSource,
        usedFallbackSnapshot: Boolean,
        lastStableSnapshotAgeMs: Long?,
        lastStableSnapshotPackage: String?,
        currentRootPackageAtCapture: String?,
        rootIsOwnOverlay: Boolean = false,
        rootIsSystemUi: Boolean = false,
        rootRetryCount: Int,
        rootAvailableFirstTry: Boolean,
        rootAvailableAfterRetry: Boolean
    ): CurrentScreenCaptureResult {
        if (snapshot.nodes.isEmpty()) throw failure(
            trace = traceFor(snapshot, captureSource, usedFallbackSnapshot, lastStableSnapshotAgeMs, lastStableSnapshotPackage),
            code = NextSentenceErrorCode.NODE_TREE_EMPTY,
            stage = NextSentenceStage.NODE_TREE_EXTRACTED
        )
        val readableCount = snapshot.nodes.count { it.readableText?.isNotBlank() == true }
        if (readableCount == 0) throw failure(
            trace = traceFor(snapshot, captureSource, usedFallbackSnapshot, lastStableSnapshotAgeMs, lastStableSnapshotPackage),
            code = NextSentenceErrorCode.RAW_TEXT_EMPTY,
            stage = NextSentenceStage.NODE_TREE_EXTRACTED
        )
        val bubbles = snapshot.nodes.toVisualBubbles()
        val parsed = parseForApp(snapshot.appPackage, snapshot.screenWidth, bubbles)
        val visualAlignment = VisualTruthAligner(snapshot.screenWidth).align(parsed.messages)
        val messages = visualAlignment.messages
            .filter { it.normalizedText?.isNotBlank() == true || it.content is MessageContent.Voice }
        if (messages.isEmpty()) throw failure(
            trace = traceFor(snapshot, captureSource, usedFallbackSnapshot, lastStableSnapshotAgeMs, lastStableSnapshotPackage).copy(
                rawNodeCount = snapshot.nodes.size,
                visibleTextCount = readableCount
            ),
            code = NextSentenceErrorCode.CHAT_MESSAGE_PARSE_EMPTY,
            stage = NextSentenceStage.CHAT_MESSAGES_PARSED
        )
        val effectiveCount = messages.count { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM }
        val source = if (snapshot.appPackage == "com.huiyi.mockchat") {
            SampleSource.EMULATOR_MOCK_CHAT_ACCESSIBILITY
        } else {
            SampleSource.REAL_DEVICE_ACCESSIBILITY
        }
        return CurrentScreenCaptureResult(
            snapshot = snapshot,
            messages = messages,
            sampleSource = source,
            warning = if (bubbles.size < snapshot.nodes.count { it.readableText != null }) "WARNING: fallback parser filtered low quality nodes." else null,
            parserName = parsed.parserName,
            parserFallbackUsed = parsed.fallbackUsed,
            accessibilityBoundsProjected = visualAlignment.accessibilityBoundsProjected,
            ocrUsed = visualAlignment.ocrUsed,
            visualTruthAvailable = visualAlignment.visualTruthAvailable,
            visualConflictCount = visualAlignment.conflictCount,
            visualSpeakerFallbackCount = visualAlignment.visualSpeakerFallbackCount,
            captureSource = captureSource,
            usedFallbackSnapshot = usedFallbackSnapshot,
            lastStableSnapshotAgeMs = lastStableSnapshotAgeMs,
            lastStableSnapshotPackage = lastStableSnapshotPackage,
            currentRootPackageAtCapture = currentRootPackageAtCapture,
            rootIsOwnOverlay = rootIsOwnOverlay,
            rootIsSystemUi = rootIsSystemUi,
            rootRetryCount = rootRetryCount,
            rootAvailableFirstTry = rootAvailableFirstTry,
            rootAvailableAfterRetry = rootAvailableAfterRetry,
            rawNodeCount = snapshot.nodes.size,
            visibleTextCount = readableCount
        )
    }

    private fun traceFor(
        snapshot: CurrentScreenSnapshot,
        captureSource: NextSentenceCaptureSource,
        usedFallbackSnapshot: Boolean,
        lastStableSnapshotAgeMs: Long?,
        lastStableSnapshotPackage: String?
    ) = NextSentenceSessionTrace(
        sessionId = java.util.UUID.randomUUID().toString(),
        startedAt = System.currentTimeMillis(),
        rootPackageName = snapshot.appPackage,
        rootWindowTitle = snapshot.windowTitle,
        captureSource = captureSource,
        primaryCapturePath = if (captureSource == NextSentenceCaptureSource.LAST_STABLE_CHAT_SNAPSHOT) "LAST_STABLE_CHAT_SNAPSHOT" else "NODE_TREE",
        nodeTreeAttempted = true,
        nodeTreeSuccess = captureSource == NextSentenceCaptureSource.CURRENT_ROOT,
        fallbackSnapshotAttempted = true,
        fallbackSnapshotSuccess = captureSource == NextSentenceCaptureSource.LAST_STABLE_CHAT_SNAPSHOT,
        usedFallbackSnapshot = usedFallbackSnapshot,
        lastStableSnapshotAgeMs = lastStableSnapshotAgeMs,
        lastStableSnapshotPackage = lastStableSnapshotPackage
    )

    private fun failure(
        trace: NextSentenceSessionTrace,
        code: NextSentenceErrorCode,
        stage: NextSentenceStage,
        cause: Throwable? = null
    ): NextSentenceException {
        return NextSentenceException(
            code = code,
            failedStage = stage,
            trace = trace.failed(code, stage, cause),
            cause = cause
        )
    }

    private fun parseForApp(appPackage: String?, screenWidth: Int, bubbles: List<VisualBubble>): ParserSelection {
        if (appPackage == "com.bajiao.im.liaoqi") {
            val liaoqiMessages = LiaoqiRealParser(screenWidth = screenWidth).parse(bubbles, MessageSource.ACCESSIBILITY_CURRENT_SCREEN)
            if (liaoqiMessages.any { it.isEffectiveChatMessage }) {
                return ParserSelection(liaoqiMessages, "LiaoqiRealParser", fallbackUsed = false)
            }
            val genericMessages = GenericVisualBubbleParser(screenWidth = screenWidth).parse(bubbles, MessageSource.ACCESSIBILITY_CURRENT_SCREEN)
            return ParserSelection(genericMessages, "GenericVisualBubbleParser", fallbackUsed = true)
        }
        return ParserSelection(
            messages = GenericVisualBubbleParser(screenWidth = screenWidth).parse(bubbles, MessageSource.ACCESSIBILITY_CURRENT_SCREEN),
            parserName = "GenericVisualBubbleParser",
            fallbackUsed = false
        )
    }

    private fun List<ScreenNodeSnapshot>.toVisualBubbles(): List<VisualBubble> {
        return asSequence()
            .filter { it.visibleToUser }
            .filter { it.bounds.right > it.bounds.left && it.bounds.bottom > it.bounds.top }
            .filter { it.readableText?.isNotBlank() == true }
            .filterNot { it.bounds.bottom - it.bounds.top < 4 }
            .map { node ->
                VisualBubble(
                    id = node.id,
                    text = node.readableText,
                    rowBounds = node.parentBounds ?: node.bounds,
                    bubbleBounds = node.ancestorBoundsChain.asReversed()
                        .firstOrNull { bounds -> bounds != node.bounds && bounds.right > bounds.left && bounds.bottom > bounds.top }
                        ?: node.parentBounds
                        ?: node.bounds,
                    textBounds = node.bounds,
                    parentBounds = node.parentBounds,
                    ancestorBoundsChain = node.ancestorBoundsChain,
                    confidence = if (node.className?.contains("TextView") == true) 88 else 72
                )
            }
            .toList()
    }

    private data class ParserSelection(
        val messages: List<MessageNode>,
        val parserName: String,
        val fallbackUsed: Boolean
    )

    private companion object {
        const val STABLE_CHAT_SNAPSHOT_MAX_AGE_MS = 60_000L
    }
}
