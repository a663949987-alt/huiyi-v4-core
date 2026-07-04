# Emulator Cloud Smoke Report

## Basic

- taskName: emulator_mockchat_cloud_smoke
- versionName: 4.1.28
- versionCode: 447
- generatedAt: 2026-07-04T08:20:33.9800839+08:00
- overallResult: PASS
- emulatorDetected: True
- emulatorSerial: emulator-5554
- huiyiInstalled: True
- mockchatInstalled: True
- accessibilityEnabled: True
- overlayPermissionGranted: True
- relaySmokeAlreadyPassed: True
- realDeviceSmokeResult: NOT_TESTED
- userNeedsPhoneThisRound: False

## LAST_OTHER MockChat Cloud

- result: PASS
- preAnalysisSnapshotTrusted: True
- actualLastSpeaker: OTHER
- cloudAttempted: True
- cloudSuccess: True
- cloudErrorCode: null
- cloudContractValidationResult: PASS
- decisionSource: CLOUD
- routeCount: 5
- title: 会意云端分析
- screenshotPath: outputs/gpt_review_inbox/emulator_cloud_smoke/last_other_after_contract_prompt.png
- logcatPath: outputs/gpt_review_inbox/emulator_cloud_smoke/last_other_after_contract_prompt_logcat.txt
- logEvidence: 07-04 00:18:16.462 I/HuiyiRuntime( 8236): next_sentence_success package=com.huiyi.mockchat captureSource=CURRENT_ROOT lastSpeaker=OTHER decision=EMPATHY_FIRST routes=5 cloudAttempted=true cloudSuccess=true cloudErrorCode=null cloudValidation=PASS cloudLikelyCause=NONE decisionSource=CLOUD

## LAST_ME MockChat Wait

- result: PASS
- actualLastSpeaker: ME
- cloudAttempted: False
- decisionSource: LOCAL_WAIT
- terminalState: WAIT_PANEL
- routeCount: 0
- showsCloudAnalyzing: False
- screenshotPath: outputs/gpt_review_inbox/emulator_cloud_smoke/last_me_wait_after.png
- logcatPath: outputs/gpt_review_inbox/emulator_cloud_smoke/last_me_wait_logcat.txt
- logEvidence: 07-04 00:19:36.840 I/HuiyiRuntime( 8378): next_sentence_success package=com.huiyi.mockchat captureSource=CURRENT_ROOT lastSpeaker=ME decision=WAIT routes=0 cloudAttempted=false cloudSuccess=false cloudErrorCode=null cloudValidation=NOT_RUN cloudLikelyCause=NONE decisionSource=LOCAL_WAIT

## Evidence Paths

- screenshotsPath: outputs/gpt_review_inbox/emulator_cloud_smoke
- lastOtherLogcat: outputs/gpt_review_inbox/emulator_cloud_smoke/last_other_after_contract_prompt_logcat.txt
- lastMeLogcat: outputs/gpt_review_inbox/emulator_cloud_smoke/last_me_wait_logcat.txt

## Conclusion

- emulatorUiSmokeResult: PASS
- sendToGpt: True
- phoneNeededThisRound: false
