package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.domain.model.Speaker
import java.io.File

class ParserReportGenerator {
    fun build(result: CurrentScreenCaptureResult): String {
        val messages = result.messages
        val reasonCounts = messages.groupingBy { it.speakerReason ?: "unknown_visual_bounds" }.eachCount()
        return buildString {
            appendLine("# Current Screen Parser Report")
            appendLine()
            result.warning?.let {
                appendLine("WARNING: $it")
                appendLine()
            }
            appendLine("- appPackage: ${result.snapshot.appPackage ?: "unknown"}")
            appendLine("- windowTitle: ${result.snapshot.windowTitle ?: "unknown"}")
            appendLine("- screenWidth: ${result.snapshot.screenWidth}")
            appendLine("- screenHeight: ${result.snapshot.screenHeight}")
            appendLine("- parserName: GenericVisualBubbleParser")
            appendLine("- capturedNodeCount: ${result.snapshot.nodes.size}")
            appendLine("- parsedMessageCount: ${messages.size}")
            appendLine("- meCount: ${messages.count { it.speaker == Speaker.ME }}")
            appendLine("- otherCount: ${messages.count { it.speaker == Speaker.OTHER }}")
            appendLine("- unknownCount: ${messages.count { it.speaker == Speaker.UNKNOWN }}")
            appendLine("- systemCount: ${messages.count { it.speaker == Speaker.SYSTEM }}")
            appendLine()
            appendLine("## speakerReason")
            reasonCounts.forEach { (reason, count) -> appendLine("- $reason: $count") }
            appendLine()
            appendLine("## Recent Messages")
            messages.takeLast(20).forEachIndexed { index, message ->
                val side = when (message.speaker) {
                    Speaker.ME -> "right"
                    Speaker.OTHER -> "left"
                    Speaker.SYSTEM -> "system"
                    Speaker.UNKNOWN -> "unknown"
                }
                val speaker = message.speaker.name.lowercase()
                val confidence = message.speakerConfidence
                val reason = message.speakerReason ?: "unknown_visual_bounds"
                val text = message.normalizedText ?: "[voice]"
                appendLine("[m${(index + 1).toString().padStart(3, '0')}][$side][$speaker $confidence% $reason] $text")
            }
        }
    }

    fun writeTo(file: File, result: CurrentScreenCaptureResult): Result<File> = runCatching {
        file.parentFile?.mkdirs()
        file.writeText(build(result), Charsets.UTF_8)
        file
    }
}
