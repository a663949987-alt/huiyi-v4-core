# Changed Files For GPT

## Cloud Timeout Escalation

- app/src/main/java/com/huiyi/v4/domain/cloud/CloudAnalysis.kt
- app/src/test/java/com/huiyi/v4/PreconfiguredCloudRealUseMvpTest.kt

## Accessibility Reconnect Guard From v4.1.43

- app/src/main/java/com/huiyi/v4/accessibility/AccessibilityModels.kt
- app/src/main/java/com/huiyi/v4/domain/pipeline/NextSentenceDiagnostics.kt
- app/src/main/java/com/huiyi/v4/domain/pipeline/NextSentenceFailureReportGenerator.kt
- app/src/main/java/com/huiyi/v4/floating/FloatingResultPanelController.kt
- app/src/main/java/com/huiyi/v4/runtime/HuiyiRuntime.kt
- app/src/main/java/com/huiyi/v4/runtime/OneTapFeedbackExporter.kt
- app/src/test/java/com/huiyi/v4/AccessibilityRuntimeTextTest.kt
- app/src/test/java/com/huiyi/v4/NextSentenceNoReactionUxTest.kt

## Version And Delivery

- app/build.gradle.kts
- outputs/update_server/latest.json
- outputs/gpt_review_inbox/README_FOR_GPT.md
- outputs/codex_to_gpt/README_FOR_GPT.md
- outputs/codex_to_gpt/result-manifest.json
- outputs/codex_to_gpt/changed-files-for-gpt.md

## Not Included In Public GitHub

- outputs/update_server/huiyi-v4.1.44-debug.apk
- outputs/huiyi-v4.1.44-debug.apk

Reason: private APK may contain relay configuration and is delivered through LAN update only.
