# Huiyi v4 GPT Review Inbox

- taskName: preconfigured_cloud_real_use_mvp_delivery_closure
- versionName: 4.1.25
- versionCode: 444
- currentOverallResult: DELIVERY_READY_NEEDS_USER_PHONE_SMOKE
- userNeedsPhoneThisRound: true

## APK Delivery
- githubApkCommitted: false
- reasonApkNotCommittedToGithub: preconfigured cloud APK contains a local relay credential, so it must not be committed to public GitHub.
- localInstallApkPath: outputs/huiyi-v4.1.25-cloud-preconfigured.apk
- localInstallApkSha256: ad92676e76589353beee632d30b0302b25cfe402dfd1bc71f33e93b9f6ecc945
- lanUpdateLatestJson: outputs/update_server/latest.json
- lanUpdateApkPath: outputs/update_server/huiyi-v4.1.25-debug.apk
- lanUpdateApkSha256: ad92676e76589353beee632d30b0302b25cfe402dfd1bc71f33e93b9f6ecc945
- localAndLanApkHashesMatch: true

## Current GitHub-Visible Evidence
1. APK_DELIVERY_FOR_GPT.md
2. preconfigured-cloud-mvp-report-for-gpt.md
3. preconfigured-cloud-mvp-report.json
4. outputs/update_server/latest.json
5. outputs/codex_to_gpt/result-manifest.json

## Important
Do not fail this round because APK files are not visible in GitHub. They are deliberately excluded from GitHub by .gitignore to avoid publishing the preconfigured relay credential embedded in the installable APK. Delivery to the user is by local file path and LAN update server.
