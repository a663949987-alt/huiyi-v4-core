# Phone Upload Waiting For Retry

- phoneBundleIncluded: false
- uploadedFromPhone: false
- gatewayReady: true
- gatewayHealth: PASS
- gatewayUrl: http://192.168.31.243:8791/api/huiyi/review-upload
- expectedUserAction: install v4.1.23, then run only the 3 phone smoke checks when safe.
- expectedPhoneResult: phone/latest is overwritten by current-version phone evidence, GPT can review.
- lastMeIfNoSafeScenario: NOT_TESTED_USER_DID_NOT_HAVE_SAFE_SCENARIO
- requiredSmokeSet: Liaoqi LAST_ME / Liaoqi LAST_OTHER / Unsupported App
- latestPathAfterSuccess: outputs/gpt_review_inbox/phone/latest/
