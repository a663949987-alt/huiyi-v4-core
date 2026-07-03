# Huiyi v4 Phone Latest

## Current State

- phoneLatestStatus: WAITING_FOR_CURRENT_VERSION_PHONE_SMOKE
- appVersionName: 4.1.23
- appVersionCode: 441
- replacedOldPollutedBundle: true
- previousPollutedVersionRemovedFromLatest: 4.1.20
- source: LOCAL_CURRENT_VERSION_PLACEHOLDER
- realDeviceTested: false
- oneTapFeedbackIncluded: false
- cloudEnabled: false
- cloudContractImplemented: false
- cloudAttempted: false

## Required Phone Smoke Set

1. Liaoqi LAST_ME: ME -> WAIT
2. Liaoqi LAST_OTHER: OTHER -> routes
3. Unsupported App: show unsupported prompt and export adapter bundle

## Last ME

- lastMeRealDeviceResult: NOT_TESTED_USER_DID_NOT_HAVE_SAFE_SCENARIO
- reason: User has not produced a safe natural LAST_ME phone scenario on current version.

## Important Contract

- phone/latest must not contain old v4.1.20 polluted feedback as latest evidence.
- One-tap feedback must bind the original NextSentenceSession.
- One-tap feedback must not rerun parser, recapture Huiyi overlay, or use panel text as pre-analysis.
- Cloud tactical analysis remains TODO and disabled.

## GPT Should Inspect

1. outputs/gpt_review_inbox/phone/latest/one-tap-feedback-manifest.json
2. outputs/gpt_review_inbox/phone/latest/latest-session/next-sentence-flight-record.json
3. outputs/gpt_review_inbox/phone/latest/current-screen/real-device-current-screen-report.json
