# Xiaoenai Generic Trial Feedback Fix Report

## Basic

- project: huiyi-v4
- taskName: xiaoenai_generic_trial_feedback_fix
- versionName: 4.1.72
- versionCode: 491
- generatedAt: 2026-07-06T18:22:00+08:00
- currentOverallResult: READY_FOR_PHONE_UPDATE_TEST
- userNeedsPhoneThisRound: true

## User Feedback Addressed

- Latest one-tap package showed windowTitle=小恩爱 and LAST_ME -> WAIT_PANEL correctly.
- The package still showed targetAppSupported=false.
- The package did not include Express Self validation because expressSelfClicked=false.

## Fixes

- Runtime DynamicPlaybookRequest no longer uses the old hardcoded support list.
- Xiaoenai normal chat pages now go through ChatAppProfileDetector.
- Xiaoenai normal chat can become GENERIC_TRIAL when effective messages and parser confidence pass.
- Huawei desktop / launcher still remains BLOCK.
- One-tap feedback record generation now uses ChatAppProfileDetector for targetAppSupported and adapterName.
- Express Self result records keep expressSelfClicked=true when the panel/session is EXPRESS_SELF.

## Expected Phone Report After Update

- appPackage: com.xiaoenai.app
- windowTitlePreAnalysisRedacted: 小恩爱
- targetAppSupported: true
- adapterName: GenericChatTrial
- source: GENERIC_TRIAL through expressSelfEligibility when Express Self is clicked
- latest session LAST_ME -> WAIT_PANEL remains PASS
- Express Self feedback package after clicking 表达我 should show expressSelfClicked=true

## Tests

- OneTapFeedbackExportTest.XiaoenaiNormalChatFeedbackRecordUsesGenericTrialProfileTest: PASS
- OneTapFeedbackExportTest.ExpressSelfFeedbackRecordMarksExpressSelfClickedTest: PASS
- ExpressSelfEligibilityTest: PASS
- RealUseV4171CombinedPackageSmokeTest: PASS
- MultiAppProfileMatrixTest: PASS
- app:testDebugUnitTest: PASS
- app:assembleDebug: PASS

## Update Package

- apkPath: outputs/update_server/huiyi-v4.1.72-debug.apk
- latestJsonPath: outputs/update_server/latest.json
- latestJsonVersionName: 4.1.72
- latestJsonVersionCode: 491
- latestJsonApkUrl: huiyi-v4.1.72-debug.apk
- apkSha256: 9C570B53E9FABC7EED5FA1E1B2865D913919CA327836CF686F1AA659B48CFE23
- sha256MatchesLatestJson: true

## User Test Scope

1. Update to v4.1.72 through LAN update.
2. Open Xiaoenai normal chat page.
3. LAST_ME -> 点下一句: should still show WAIT.
4. 点表达我, then use one-tap feedback if needed.
5. GPT package should show expressSelfClicked=true for Express Self feedback.
