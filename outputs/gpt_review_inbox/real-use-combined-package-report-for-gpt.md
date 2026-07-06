# Real Use Combined Package Report

## Basic Info

- taskName: real_use_dynamic_playbook_express_self_combined_package
- versionName: 4.1.63
- versionCode: 482
- generatedAt: 2026-07-06T08:32:24+08:00
- currentOverallResult: READY_FOR_REAL_DEVICE_TEST
- apkPath: outputs/update_server/huiyi-v4.1.63-debug.apk
- apkSha256: 759B7B982F05658A12479512872AC6D1AD91B7AE58ACE53AAB65C1971CC75EF5
- updateManifest: outputs/update_server/latest.json
- lanUpdateAvailable: true
- apkCommittedToPublicGithub: false
- privateApkContainsRelayConfig: true
- userNeedsPhoneThisRound: true

## Combined Scope

- expressSelfEligibility / HOLD_BACK fix: INCLUDED
- dynamic Playbook instant cache: INCLUDED
- cloud background refresh: INCLUDED
- stale cloud refresh discard: INCLUDED
- LAST_ME local WAIT: INCLUDED
- Next Sentence / Express Self dual buttons: INCLUDED
- versionConflictResolved: true

## Accepted Evidence Carried Forward

- dynamicPlaybookFullEmulatorSmoke: PASS
- expressSelfEligibilityEmulatorSmoke: PASS
- dynamic playbook passiveNext latency: 300ms
- dynamic playbook activeExpression latency: 300ms
- cloudRefreshAttempted: true
- cloudRefreshSuccess: true
- cloudContractValidationResult: PASS
- playbookCacheUpdatedFromCloud: true
- nextClickReadsCloudEnhancedPlaybook: true
- staleCloudRefreshDiscarded: true
- staleCloudRefreshDiscardedReason: CHAT_KEY_CHANGED
- lastMeWaitPass: true

## Build And Local Tests

- version bump: PASS, 4.1.63 / 482
- app debug APK generated: PASS
- update_server APK generated: PASS
- latest.json points to current APK: PASS
- app unit tests: PASS
- assembleDebug: PASS
- mockchat assembleDebug: PASS

## User Real Device Test Scope

1. 下一句 LAST_OTHER
   - expected: 1 秒内出中文接话
   - expected: 本地 Playbook 先出，云端后台刷新

2. 下一句 LAST_ME
   - expected: 显示“你已经回过了，先等对方”
   - expected: 不出路线，不调用云端

3. 表达我，刚发完自己消息
   - expected: HOLD_BACK
   - expected: 不出 5 条主动表达路线

4. 表达我，对方谈规划 / 现实 / 稳定
   - expected: 出人物弧光 / 表达我 / 共创路线

5. 表达我，不支持 App / 桌面状态
   - expected: BLOCK
   - expected: 不出路线

## Real Device Status

- realDeviceSmoke: PENDING_USER_TEST
- userNeedsPhoneThisRound: true
- reason: 两个模拟器烟测均已通过，本轮是合并实机包，需要用户安装 4.1.63 后做一次真实聊天确认。

## Safety Notes

- rawPrivateChatUploadedToGithub: false
- autoSend: false
- apkCommittedToPublicGithub: false
- apiKeyPrintedInReports: false
