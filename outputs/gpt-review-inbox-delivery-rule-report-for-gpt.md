# GPT Review Inbox Delivery Rule Report

## Basic
- versionName: 4.1.9
- versionCode: 423
- taskName: gpt_review_inbox_delivery_rule
- overall_result: NOT_TESTED
- generatedAt: 2026-07-03

## Implemented
- Added `scripts/generate_gpt_review_inbox.py`.
- Added `scripts/generate-gpt-review-inbox.ps1`.
- Updated `scripts/generate-review-bundle.ps1` so every review bundle refresh also generates `outputs/gpt_review_inbox/` and `outputs/huiyi-gpt-review-inbox.zip`.

## Outputs
- outputs/gpt_review_inbox/README_FOR_GPT.md
- outputs/gpt_review_inbox/gpt-review-manifest.json
- outputs/gpt_review_inbox/changed-files-for-gpt.md
- outputs/huiyi-gpt-review-inbox.zip

## Privacy
- APK files are not copied into the inbox zip.
- `local.properties`, keystore files, and API keys are not copied.
- Current inbox uses existing GPT review reports and current failure diagnostics only.
