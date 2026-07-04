# Passive / Active Split Report

## Basic Info
- project: Huiyi v4
- taskName: split_passive_next_sentence_and_active_self_expression
- versionName: 4.1.54
- versionCode: 473
- generatedAt: 2026-07-04T19:13:37+08:00
- currentOverallResult: LOCAL_TEST_PASS_LAN_APK_READY
- userNeedsPhoneThisRound: true

## Split Result
- nextSentenceButtonExists: true
- expressSelfButtonExists: true
- floatingMenu: Next Sentence / Express Self / Hide
- nextSentencePanelClean: true
- nextSentencePanelHasNoLikeMeFeedback: true
- nextSentencePanelHasNoCharacterArcOverload: true
- expressSelfPanelHasCharacterArc: true
- expressSelfPanelHasPersonaFeedback: true
- passiveCloudNonBlocking: true
- localFallbackImmediate: true

## Next Sentence
- LAST ME: wait panel, no routes, no cloud.
- LAST OTHER: immediate local 5 routes, cloud can upgrade later.
- Soft timeout: local routes stay visible; cloud result may refresh later.
- Hidden from default panel: like-me feedback, too-oily feedback, character-arc explanation.
- Visible controls: copy / refresh routes / Express Self / hide.

## Express Self
- Shows active self-expression routes.
- Can show ARC_REVEAL / character arc.
- Shows: current action, persona facet, overdo risk.
- Shows feedback: like me / unlike me / too oily / too heavy / too empty / sendable.
- Falls back to a local character arc route if no cloud/persona route is available.

## LAN Update SHA Mismatch Fix
- previousIssue: 4.1.53 APK was republished with the same filename while the phone was downloading.
- fixVersion: 4.1.54
- freshApkFilename: true
- shaVerifiedTempDownload: true
- noCacheRequestHeader: true
- updateServerNoStoreHeader: true
- serverDownloadVerified: true

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
- localLanApkPath: outputs/update_server/huiyi-v4.1.54-debug.apk
- lanLatestJson: http://192.168.31.243:8787/latest.json
- lanApkUrl: http://192.168.31.243:8787/huiyi-v4.1.54-debug.apk
- apkSha256: F8F8BF3EE31C90E863738623E518E97A1C03D9A3D8B252722771E7FE58069982

## Phone Test Needed
Yes. User only needs to:
1. Check LAN update again and install 4.1.54.
2. Tap Next Sentence: clean quick replies, no persona feedback.
3. Tap Express Self: character arc/self-expression fields and feedback.
