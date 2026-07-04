# v4.1.29 NextSentence Session Isolation Smoke Report

- taskName: next_sentence_session_isolation_cloud_callback_fix
- versionName: 4.1.29
- versionCode: 448
- generatedAt: 2026-07-04T09:10:55.5865436+08:00
- overallResult: PASS
- unitTests: PASS
- assembleDebug: PASS
- emulatorDetected: true
- emulatorSerial: emulator-5554

## Session / Cloud Binding

- activeSessionId: 0ec26efa-5463-4570-a484-60362aecdf5c
- cloudRequestSessionId: 0ec26efa-5463-4570-a484-60362aecdf5c
- cloudResponseSessionId: 0ec26efa-5463-4570-a484-60362aecdf5c
- preAnalysisSnapshotId: com.huiyi.mockchat:1783126780841:会意:-1236517109
- chatPackage: com.huiyi.mockchat
- chatWindowHash: 1523985747
- cloudResponseDiscarded: True
- cloudResponseDiscardedReason: STALE_SESSION
- activeSessionChangedDuringCloud: True
- panelRenderedSessionId: 0ec26efa-5463-4570-a484-60362aecdf5c
- oneClickOneTerminalPanel: True

## Emulator Results

- lastOtherCloudResult: PASS
- lastOtherLogEvidence: 07-04 01:00:26.866 I/HuiyiRuntime( 9783): next_sentence_success package=com.huiyi.mockchat sessionId=0ec26efa-5463-4570-a484-60362aecdf5c activeSessionId=0ec26efa-5463-4570-a484-60362aecdf5c cloudRequestSessionId=0ec26efa-5463-4570-a484-60362aecdf5c cloudResponseSessionId=0ec26efa-5463-4570-a484-60362aecdf5c preAnalysisSnapshotId=com.huiyi.mockchat:1783126780841:会意:-1236517109 chatPackage=com.huiyi.mockchat chatWindowHash=1523985747 captureSource=CURRENT_ROOT lastSpeaker=OTHER decision=EMPATHY_FIRST routes=5 cloudAttempted=true cloudSuccess=true cloudErrorCode=null cloudValidation=PASS cloudLikelyCause=NONE decisionSource=CLOUD
- lastMeWaitResult: PASS
- lastMeLogEvidence: 07-04 01:08:05.047 I/HuiyiRuntime(10384): next_sentence_success package=com.huiyi.mockchat sessionId=3ea5538f-683b-4ed4-b42e-c1596dc11833 activeSessionId=3ea5538f-683b-4ed4-b42e-c1596dc11833 cloudRequestSessionId=null cloudResponseSessionId=null preAnalysisSnapshotId=com.huiyi.mockchat:1783127284996:MockChatLab:649889453 chatPackage=com.huiyi.mockchat chatWindowHash=-294745902 captureSource=CURRENT_ROOT lastSpeaker=ME decision=WAIT routes=0 cloudAttempted=false cloudSuccess=false cloudErrorCode=null cloudValidation=NOT_RUN cloudLikelyCause=NONE decisionSource=LOCAL_WAIT
- doubleClickNextOnlyOneActiveSession: PASS
- doubleClickDiscardEvidence: 07-04 01:00:24.419 I/HuiyiRuntime( 9783): next_sentence_discarded sessionId=11cc5c68-789f-43d5-8309-dfe7675acbe4 activeSessionId=0ec26efa-5463-4570-a484-60362aecdf5c reason=STALE_SESSION
- switchingChatCancelsOldCloudRequest: PASS
- switchingChatDiscardEvidence: 07-04 01:08:48.160 I/HuiyiRuntime(10384): next_sentence_discarded sessionId=42f247f9-3976-4dc0-ab1c-6dd81c49e95f activeSessionId=3ea5538f-683b-4ed4-b42e-c1596dc11833 reason=STALE_SESSION
- staleCloudResponseDoesNotRenderPanel: PASS
- contaminatedPreAnalysisSkipsCloudAndRoutes: PASS_UNIT_TESTED
- timeoutShowsControlledFailNotSilent: PASS_UNIT_TESTED

## Evidence Paths

- screenshotsPath: outputs/gpt_review_inbox/session_race_smoke
- doubleClickScreenshot: outputs/gpt_review_inbox/session_race_smoke/double_click_one_panel_toast_loading.png
- doubleClickLogcat: outputs/gpt_review_inbox/session_race_smoke/double_click_one_panel_toast_loading_logcat.txt
- switchChatScreenshot: outputs/gpt_review_inbox/session_race_smoke/switch_chat_old_cloud_discarded_final.png
- switchChatLogcat: outputs/gpt_review_inbox/session_race_smoke/switch_chat_old_cloud_discarded_final_logcat.txt

## User Gate

- userNeedsPhoneThisRound: false
- realDeviceSmokeResult: NOT_TESTED
