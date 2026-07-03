# Huiyi v4.1.18 LAN Update Check Failure Hotfix Report

- project: huiyi-v4
- taskName: lan_update_check_failure_default_url_fallback
- versionName: 4.1.18
- versionCode: 436
- overall_result: PASS_FOR_LOCAL_BUILD_AND_LAN_SERVER_CHECK
- real_device_result: NEEDS_USER_RETRY
- generatedAt: 2026-07-03 16:46:00 +08:00

## Problem

User reported phone LAN update check failure.

PC-side checks showed:

- update server process listening on 0.0.0.0:8787
- local latest.json reachable
- LAN latest.json reachable at http://192.168.31.243:8787/latest.json
- APK reachable at http://192.168.31.243:8787/huiyi-v4.1.18-debug.apk
- Windows firewall rule creation failed because administrator permission is required

## Fix

- version bumped to 4.1.18 / 436
- development default update URL is now built in:
  - http://192.168.31.243:8787/latest.json
- HuiyiRuntime initializes LAN update URL from saved preference, falling back to BuildConfig.HUIYI_UPDATE_BASE_URL.
- checkLanUpdate now retries BuildConfig.HUIYI_UPDATE_BASE_URL if the saved/custom URL fails.

## Verification

- unit tests: PASS
- assembleDebug: PASS
- latest.json LAN check: PASS
- APK HEAD request over LAN URL: PASS

## Published Artifacts

- outputs/huiyi-v4.1.18-debug.apk
- outputs/update_server/huiyi-v4.1.18-debug.apk
- outputs/update_server/latest.json

## Notes

If the phone currently cannot check updates because the already-installed app has a stale bad URL, user may need to install v4.1.18 once from the direct LAN APK URL. After that, future LAN checks should use the built-in fallback URL.
