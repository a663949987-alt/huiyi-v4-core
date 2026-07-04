# Passive / Active Split Report

## Basic Info
- project: Huiyi v4
- taskName: split_passive_next_sentence_and_active_self_expression
- versionName: 4.1.53
- versionCode: 472
- generatedAt: 2026-07-04T18:48:22+08:00
- currentOverallResult: LOCAL_TEST_PASS_LAN_APK_READY
- userNeedsPhoneThisRound: true

## Split Result
- nextSentenceButtonExists: true
- expressSelfButtonExists: true
- floatingMenu: 下一句 / 表达我 / 隐藏
- nextSentencePanelClean: true
- nextSentencePanelHasNoLikeMeFeedback: true
- nextSentencePanelHasNoCharacterArcOverload: true
- expressSelfPanelHasCharacterArc: true
- expressSelfPanelHasPersonaFeedback: true
- passiveCloudNonBlocking: true
- localFallbackImmediate: true

## 下一句
- LAST ME: wait panel, no routes, no cloud.
- LAST OTHER: immediate local 5 routes, cloud can upgrade later.
- Soft timeout: local routes stay visible; cloud result may refresh later.
- Hidden from default panel: 像我 / 不像我 / 太油 / 太重 / 太像汇报 / 人物弧光 details.
- Visible controls: 复制 / 换一批 / 表达我 / 隐藏.

## 表达我
- Shows active self-expression routes.
- Can show ARC_REVEAL / 人物弧光.
- Shows: 本轮动作, 这句话展示了你的哪一面, 不要说过头.
- Shows feedback: 像我 / 不像我 / 太油 / 太重 / 太空 / 可发.
- Falls back to local character arc route if no cloud/persona route is available.

## Tests
- unit tests: PASS
- assembleDebug: PASS
- NextSentencePanelDoesNotShowPersonaFeedbackTest: PASS
- NextSentencePanelDoesNotShowLikeMeUnlikeMeTest: PASS
- NextSentencePanelHasExpressSelfEntryTest: PASS
- ExpressSelfPanelShowsCharacterArcTest: PASS
- ExpressSelfPanelShowsLikeMeFeedbackTest: PASS
- PassiveNextSentenceLocalFallbackShowsImmediatelyTest: PASS
- PassiveCloudTimeoutDoesNotBlockLocalRoutesTest: PASS
- LastMeNextSentenceStillWaitsTest: PASS
- ActiveExpressSelfCanUseArcRevealTest: PASS

## APK / LAN Update
- localLanApkPath: outputs/update_server/huiyi-v4.1.53-debug.apk
- lanLatestJson: http://192.168.31.243:8787/latest.json
- lanApkUrl: http://192.168.31.243:8787/huiyi-v4.1.53-debug.apk
- apkSha256: 8BC220D995A303C4A96E6F2DBFB5CDD5A0AB7DDDCCD30A1090C36E2E3674DC6B

## Phone Test Needed
Yes. User only needs to test:
1. 点“下一句”: should be clean quick replies, no persona feedback.
2. 点“表达我”: should show character arc/self-expression fields and feedback.
