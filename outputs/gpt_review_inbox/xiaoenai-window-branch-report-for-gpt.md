# Xiaoenai Window Branch Fix Report

## Basic Info
- taskName: xiaoenai_window_title_branch_fix_before_phone
- versionName: 4.1.67
- versionCode: 486
- generatedAt: 2026-07-06T12:10:24+08:00
- currentOverallResult: XIAOENAI_WINDOW_BRANCH_FIX_LOCAL_PASS
- userNeedsPhoneThisRound: false

## Problem
v4.1.66 phone evidence showed Express Self blocked with windowTitle=华为桌面 / BLOCK_UNTRUSTED_SNAPSHOT. If the user was actually in Xiaoenai chat, this means a stale launcher/window title polluted the front-window trust decision.

## Fix
- Xiaoenai package/root now has priority over stale desktop title text.
- `windowTitle=华为桌面` is no longer enough to block when the active package/root is `com.xiaoenai.app`.
- Real launcher/desktop still blocks when the active package is launcher/desktop or the package is blank with desktop title markers.
- Huiyi panel contamination markers still block.

## Branch Validation

### Xiaoenai normal chat page
- fixture: `appPackage=com.xiaoenai.app`
- currentAppPackage: `com.xiaoenai.app`
- currentWindowTitleRedacted: `华为桌面`
- expected: allow `GENERIC_TRIAL` or `XIAOENAI_PROFILE`
- actual: `ALLOW_GENERIC_TRIAL`
- result: PASS

### Huawei desktop / launcher
- fixture: `currentAppPackage=com.huawei.android.launcher`
- currentWindowTitleRedacted: `华为桌面`
- expected: BLOCK
- actual: `BLOCK_UNTRUSTED_SNAPSHOT`
- blockReason: `WINDOW_IS_DESKTOP_OR_LAUNCHER`
- result: PASS

## Test Result
- targeted test: `ExpressSelfEligibilityTest`: PASS
- full unit tests: PASS
- debug assemble: PASS
- emulator smoke: NOT_RUN_FIXTURE_BRANCH_COVERAGE_USED

## User Testing
- userNeedsPhoneThisRound: false
- reason: this round only closes the branch bug locally before another phone package is delivered.
