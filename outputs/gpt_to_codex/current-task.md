# GPT -> Codex Current Task

taskStatus: COMPLETED
project: Huiyi v4
taskName: late_cloud_result_return_after_soft_timeout_fix
versionName: 4.1.56
versionCode: 475
createdBy: User
userNeedsPhoneThisRound: true
realDeviceRequiredThisRound: true

## Problem
- User observed relay token consumption but no cloud result returned to the phone.
- Latest phone feedback was version 4.1.52.
- The trace showed cloudAttempted=true, cloudErrorCode=SOFT_TIMEOUT_PENDING, and decisionSource=LOCAL_FALLBACK.
- Emulator reproduced a late cloud result being discarded with reason FOREGROUND_PACKAGE_CHANGED.

## Goals
- Keep late cloud success alive after soft timeout.
- Surface late cloud failure or contract failure back to runtime/report.
- Do not discard valid late cloud result just because the panel was hidden.
- Do not discard valid late cloud result on foreground package drift when session, snapshot, package, and window hash still match.
- Preserve stale-session, snapshot, package, window hash, and contamination safety guards.

## Result
- currentOverallResult: EMULATOR_LATE_CLOUD_PASS_LAN_APK_READY
- unitTests: PASS
- assembleDebug: PASS
- emulatorLateCloudSmokeResult: PASS
- LAN update: outputs/update_server/huiyi-v4.1.56-debug.apk
- apkSha256: A48E30DEF6FB77F80CFAF14C2871B81C84494A5B4497B9BCF07211FED458B4B3
