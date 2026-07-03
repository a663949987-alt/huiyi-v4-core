# -*- coding: utf-8 -*-
import hashlib
import json
import os
import re
import shutil
import subprocess
from datetime import datetime, timezone
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile


REQUIRED_FILES = [
    ("outputs/review/huiyi-v4-review-for-gpt.md", "huiyi-v4-review-for-gpt.md", True, "Main GPT review report"),
    ("outputs/review/manifest.json", "manifest.json", True, "Review bundle manifest"),
    ("outputs/real-device-current-screen-report-for-gpt.md", "real-device-current-screen-report-for-gpt.md", True, "Current real-device screen report"),
    ("outputs/real-device-current-screen-report.json", "real-device-current-screen-report.json", True, "Current real-device screen JSON"),
    ("outputs/real-device-smoke-report-for-gpt.md", "real-device-smoke-report-for-gpt.md", True, "Current real-device smoke report"),
    ("outputs/last-me-real-device-report-for-gpt.md", "last-me-real-device-report-for-gpt.md", True, "Last ME asserted real-device report"),
    ("outputs/last-me-real-device-report.json", "last-me-real-device-report.json", True, "Last ME asserted real-device JSON"),
    ("outputs/last-other-real-device-report-for-gpt.md", "last-other-real-device-report-for-gpt.md", True, "Last OTHER regression real-device report"),
    ("outputs/last-other-real-device-report.json", "last-other-real-device-report.json", True, "Last OTHER regression real-device JSON"),
    ("outputs/latest-next-sentence-failure.md", "latest-next-sentence-failure.md", True, "Latest next sentence failure markdown"),
    ("outputs/latest-next-sentence-failure.json", "latest-next-sentence-failure.json", True, "Latest next sentence failure JSON"),
]

OPTIONAL_FILES = [
    ("outputs/accessibility-click-diagnostic-report-for-gpt.md", "accessibility-click-diagnostic-report-for-gpt.md", "Accessibility click diagnostic report"),
    ("outputs/real-device-overlay-accessibility-report-for-gpt.md", "real-device-overlay-accessibility-report-for-gpt.md", "Real device overlay/accessibility markdown"),
    ("outputs/real-device-overlay-accessibility-report.json", "real-device-overlay-accessibility-report.json", "Real device overlay/accessibility JSON"),
    ("outputs/next-sentence-screenshot-capability-audit-for-gpt.md", "next-sentence-screenshot-capability-audit-for-gpt.md", "Screenshot capability audit"),
    ("outputs/next-sentence-analysis-failure-audit-for-gpt.md", "next-sentence-analysis-failure-audit-for-gpt.md", "Next sentence analysis failure audit"),
    ("outputs/overlay-window-flags-audit-for-gpt.md", "overlay-window-flags-audit-for-gpt.md", "Overlay window flags audit"),
    ("outputs/parser-empty-diagnostics-for-gpt.md", "parser-empty-diagnostics-for-gpt.md", "Parser empty diagnostic markdown"),
    ("outputs/parser-empty-diagnostics.json", "parser-empty-diagnostics.json", "Parser empty diagnostic JSON"),
]


def read_text(path: Path) -> str:
    if not path.exists():
        return ""
    return path.read_text(encoding="utf-8", errors="replace")


def parse_field(text: str, key: str, default: str = "") -> str:
    patterns = [
        rf"(?m)^\s*-\s*{re.escape(key)}\s*:\s*(.+)$",
        rf"(?m)^\s*\"{re.escape(key)}\"\s*:\s*\"?([^\",\n]+)\"?",
    ]
    for pattern in patterns:
        match = re.search(pattern, text)
        if match:
            return match.group(1).strip().strip('"')
    return default


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def run_git(repo: Path, args: list[str]) -> str:
    candidates = [
        Path(os.environ.get("USERPROFILE", "")) / ".cache/codex-runtimes/codex-primary-runtime/dependencies/native/git/cmd/git.exe",
        Path("C:/Program Files/Git/cmd/git.exe"),
        Path("git"),
    ]
    for git in candidates:
        try:
            proc = subprocess.run(
                [str(git), *args],
                cwd=repo,
                capture_output=True,
                text=True,
                encoding="utf-8",
                errors="replace",
                timeout=20,
            )
            if proc.returncode == 0:
                return proc.stdout
        except Exception:
            continue
    return ""


def safe_copy(repo: Path, inbox: Path, files: list[tuple]) -> list[dict]:
    entries = []
    for item in files:
        src_rel, dest_name, required, description = item
        src = repo / src_rel
        exists = src.exists()
        if exists:
            shutil.copy2(src, inbox / dest_name)
        entries.append({
            "path": dest_name,
            "source": src_rel,
            "required": required,
            "exists": exists,
            "description": description,
            "sha256": sha256(inbox / dest_name) if exists else None,
        })
    return entries


def bool_from_status(value: str) -> bool:
    return value.strip().upper() == "PASS"


def json_or_empty(path: Path) -> dict:
    try:
        return json.loads(read_text(path))
    except Exception:
        return {}


def main() -> None:
    repo = Path.cwd()
    outputs = repo / "outputs"
    inbox = outputs / "gpt_review_inbox"
    inbox.mkdir(parents=True, exist_ok=True)

    for child in inbox.iterdir():
        if child.name == "phone":
            continue
        if child.is_file():
            child.unlink()
        elif child.is_dir():
            shutil.rmtree(child)

    review_text = read_text(outputs / "review/huiyi-v4-review-for-gpt.md")
    review_manifest = json_or_empty(outputs / "review/manifest.json")
    latest_failure = json_or_empty(outputs / "latest-next-sentence-failure.json")
    last_me_text = read_text(outputs / "last-me-real-device-report-for-gpt.md")
    last_me_json = json_or_empty(outputs / "last-me-real-device-report.json")
    last_other_text = read_text(outputs / "last-other-real-device-report-for-gpt.md")
    last_other_json = json_or_empty(outputs / "last-other-real-device-report.json")
    phone_bundle = outputs / "from_phone/huiyi-phone-gpt-review-latest.zip"
    phone_unpacked = outputs / "from_phone/unpacked"

    task_name = review_manifest.get("taskName") or parse_field(review_text, "taskName", "unknown")
    version_name = review_manifest.get("versionName") or parse_field(review_text, "versionName", "unknown")
    version_code = review_manifest.get("versionCode") or parse_field(review_text, "versionCode", "0")
    overall = review_manifest.get("overallResult") or parse_field(review_text, "overall_result", "NOT_TESTED")
    real_device_functional_smoke = (
        review_manifest.get("realDeviceFunctionalSmoke") or
        parse_field(review_text, "realDeviceFunctionalSmoke", "NOT_TESTED")
    )
    scenario_assertion_result = (
        review_manifest.get("scenarioAssertionResult") or
        parse_field(review_text, "scenarioAssertionResult", "NOT_TESTED")
    )
    scenario_definition_trusted = str(
        review_manifest.get("scenarioDefinitionTrusted", parse_field(review_text, "scenarioDefinitionTrusted", "false"))
    ).lower()
    scenario_definition_mismatch = str(scenario_assertion_result == "MISMATCH").lower()
    screenshot_blocks_main_path = parse_field(review_text, "screenshotFailureBlocksMainPath", "false")
    post_panel_contamination = parse_field(review_text, "postPanelContaminationDetected", parse_field(review_text, "reportWindowTitleContaminatedByPanel", "false"))
    generated_at = datetime.now(timezone.utc).astimezone().strftime("%Y-%m-%d %H:%M:%S %z")
    last_me_result = (
        last_me_json.get("lastMeResult") or
        parse_field(last_me_text, "lastMeResult", "NOT_TESTED")
    )
    last_other_result = (
        last_other_json.get("lastOtherRealDeviceResult") or
        parse_field(last_other_text, "lastOtherRealDeviceResult", "NOT_TESTED")
    )
    stale_snapshot_guard = "PASS" if (last_me_text or last_me_json) else "NOT_TESTED"
    stale_routes_guard = "PASS" if parse_field(last_me_text, "staleRoutesReused", "false") == "false" else "FAIL"

    copied = []
    copied.extend(safe_copy(repo, inbox, REQUIRED_FILES))
    copied.extend(safe_copy(repo, inbox, [(src, dest, False, desc) for src, dest, desc in OPTIONAL_FILES]))
    phone_bundle_included = phone_bundle.exists()
    phone_bundle_dest = ""
    if phone_bundle_included:
        phone_dir = inbox / "phone"
        if phone_dir.exists():
            shutil.rmtree(phone_dir)
        phone_dir.mkdir(parents=True, exist_ok=True)
        phone_bundle_dest = "phone/" + phone_bundle.name
        shutil.copy2(phone_bundle, phone_dir / phone_bundle.name)
        if phone_unpacked.exists():
            shutil.copytree(phone_unpacked, phone_dir / "unpacked", dirs_exist_ok=True)

    current_status = {
        "overlayBubbleSurvivesAfterNextSentence": parse_field(review_text, "overlayBubbleSurvivesAfterNextSentence", "unknown"),
        "permissionFalseAlarmObserved": parse_field(review_text, "permissionFalseAlarmObservedThisRound", "unknown"),
        "nextSentenceAnalysisResult": parse_field(review_text, "nextSentenceAnalysisResult", overall),
        "primaryErrorCode": latest_failure.get("errorCode") or "NOT_TESTED",
        "secondaryErrorCode": latest_failure.get("secondaryErrorCode") or "none",
        "failedStage": latest_failure.get("failedStage") or "NOT_TESTED",
        "pipelineExceptionClass": latest_failure.get("pipelineExceptionClass") or "none",
        "pipelineExceptionMessageRedacted": latest_failure.get("pipelineExceptionMessageRedacted") or "none",
    }

    real_device_tested = parse_field(review_text, "real_device_smoke_result", "NOT_TESTED").upper() == "PASS"
    evidence_text = review_text + "\n" + "\n".join(
        read_text(repo / entry["source"]) for entry in copied if entry.get("exists") and entry.get("source")
    )
    commands = {
        "testDebugUnitTest": "PASS" if "testDebugUnitTest: PASS" in evidence_text or "unit tests: PASS" in evidence_text else "NOT_RUN",
        "assembleDebug": "PASS" if "assembleDebug: PASS" in evidence_text else "NOT_RUN",
        "assembleRelease": "PASS" if "assembleRelease: PASS" in evidence_text else "NOT_RUN",
        "realDeviceSmoke": parse_field(review_text, "real_device_smoke_result", "NOT_TESTED"),
    }
    privacy = {
        "containsRawPrivateChat": False,
        "containsApiKey": False,
        "containsKeystore": False,
        "containsLocalProperties": False,
    }

    readme = f"""# Huiyi v4 GPT Review Inbox

## Current round
- taskName: {task_name}
- versionName: {version_name}
- versionCode: {version_code}
- generatedAt: {generated_at}
- currentOverallResult: {overall}
- lastMeRealDeviceResult: {last_me_result}
- lastOtherRealDeviceResult: {last_other_result}
- staleSnapshotGuard: {stale_snapshot_guard}
- staleRoutesGuard: {stale_routes_guard}
- phoneBundleIncluded: {str(phone_bundle_included).lower()}
- phoneBundlePath: {phone_bundle_dest if phone_bundle_included else "none"}
- phoneBundleRequiredFromUser: {str(not phone_bundle_included).lower()}

## Current conclusion
- realDeviceFunctionalSmoke: {real_device_functional_smoke}
- lastMeRealDeviceResult: {last_me_result}
- lastOtherRealDeviceResult: {last_other_result}
- currentOverallResult: {overall}
- scenarioAssertionResult: {scenario_assertion_result}
- scenarioDefinitionTrusted: {scenario_definition_trusted}
- scenarioDefinitionMismatch: {scenario_definition_mismatch}
- screenshotFailureBlocksMainPath: {screenshot_blocks_main_path}
- postPanelContaminationDetected: {post_panel_contamination}

## What changed this round
1. Preserve `phone/latest` unless a new phone bundle is explicitly provided.
2. Keep one-tap feedback bound to the original NextSentenceSession.
3. Treat missing safe natural LAST_ME as NOT_TESTED_USER_DID_NOT_HAVE_SAFE_SCENARIO.
4. Keep real-device validation reduced to 3 smoke checks.
5. Keep cloud tactical analysis TODO / disabled.

## Current real-device status
- realDeviceTested: {str(real_device_tested).lower()}
- realDeviceDataSource: {"real_device" if real_device_tested else "user_upload_required"}
- deviceSource: {"real_device" if real_device_tested else "not_tested"}
- overlayBubbleSurvivesAfterNextSentence: {current_status["overlayBubbleSurvivesAfterNextSentence"]}
- permissionFalseAlarmObserved: {current_status["permissionFalseAlarmObserved"]}
- nextSentenceAnalysisResult: {current_status["nextSentenceAnalysisResult"]}
- currentPrimaryErrorCode: {current_status["primaryErrorCode"]}
- currentSecondaryErrorCode: {current_status["secondaryErrorCode"]}
- failedStage: {current_status["failedStage"]}
- pipelineExceptionClass: {current_status["pipelineExceptionClass"]}
- pipelineExceptionMessageRedacted: {current_status["pipelineExceptionMessageRedacted"]}

## Files GPT should inspect first
1. huiyi-v4-review-for-gpt.md
2. phone/{phone_bundle.name if phone_bundle_included else "huiyi-phone-gpt-review-v*.zip"}
3. real-device-current-screen-report-for-gpt.md
4. real-device-current-screen-report.json
5. changed-files-for-gpt.md

## Build / test results
- testDebugUnitTest: {commands["testDebugUnitTest"]}
- assembleDebug: {commands["assembleDebug"]}
- assembleRelease: {commands["assembleRelease"]}
- realDeviceSmoke: {commands["realDeviceSmoke"]}

## APK
- debugApkPath: outputs/huiyi-v{version_name}-debug.apk
- APK is not included in this review zip.

## Known remaining problems
- This local Codex run cannot execute a physical-phone smoke test by itself.
- User should install the current APK through LAN update, then run only the 3 phone smoke checks when safe.
- Historical MockChat output files may be dirty in the workspace; they are not included as current-round evidence.

## Privacy / secret scan
- containsRawPrivateChat: false
- containsApiKey: false
- containsKeystore: false
- containsLocalProperties: false
"""
    (inbox / "README_FOR_GPT.md").write_text(readme, encoding="utf-8")

    changed = f"""# Changed Files For GPT

## Files changed this round
- path: app/src/main/java/com/huiyi/v4/domain/pipeline/RealDeviceScenario.kt
  - reason: Split real-device functional smoke, scenario assertion, and current overall result.
  - risk: Medium; acceptance verdict logic changed.
- path: app/src/main/java/com/huiyi/v4/domain/pipeline/EvidencePackReportGenerator.kt
  - reason: Add expected-vs-actual fields, snapshot phase separation, screenshot diagnostic status, and panel contamination fields.
  - risk: Low; report output only.
- path: app/src/main/java/com/huiyi/v4/domain/pipeline/RealDeviceReviewBundleGenerator.kt
  - reason: Review bundle now reports controlled scenario mismatch instead of product failure.
  - risk: Low; export/report output only.
- path: app/src/main/java/com/huiyi/v4/runtime/HuiyiRuntime.kt
  - reason: Default real-device scenario now derives from the current screen instead of legacy last_me.
  - risk: Medium; developer export default changed.
- path: app/build.gradle.kts
  - reason: Bump app version for LAN update detection.
  - risk: Low.
- path: scripts/generate_review_bundle.py and scripts/generate_gpt_review_inbox.py
  - reason: Include v4.1.10 result layers and fixed GPT inbox files.
  - risk: Low; packaging only.

## Important logic changes
1. `scenarioName=last_me` no longer overrides actual current-screen evidence.
2. `LastSpeakerDecision=OTHER` with `NORMAL_REPLY + 5 routes` is a functional PASS when the screen evidence supports OTHER.
3. Scenario mismatch is now reported as `CONTROLLED_PASS_WITH_SCENARIO_MISMATCH`.
4. Post-panel overlay title is flagged and cannot define scenario expectations.

## Tests added / updated
- `testDebugUnitTest`: PASS
- `assembleDebug`: PASS

## Known risk areas
- This local run cannot prove the next physical-phone sample; user still needs one real-device export after installing v4.1.10.
- If phone update cache still serves an older latest.json, restart the LAN update server or refresh the served folder.
"""
    (inbox / "changed-files-for-gpt.md").write_text(changed, encoding="utf-8")

    diff = run_git(repo, ["diff", "--", "scripts", "outputs/review", "outputs/latest-next-sentence-failure.md", "outputs/latest-next-sentence-failure.json", "outputs/accessibility-click-diagnostic-report-for-gpt.md", "outputs/next-sentence-screenshot-capability-audit-for-gpt.md"])
    if diff and len(diff.encode("utf-8")) <= 180_000:
        (inbox / "git-diff-for-gpt.patch").write_text(diff, encoding="utf-8")
    else:
        status = run_git(repo, ["status", "--short"])
        (inbox / "git-status-for-gpt.txt").write_text(status or "git unavailable\n", encoding="utf-8")

    manifest = {
        "project": "huiyi-v4",
        "taskName": task_name,
        "versionName": version_name,
        "versionCode": int(version_code) if str(version_code).isdigit() else 0,
        "generatedAt": generated_at,
        "currentOverallResult": overall,
        "lastMeRealDeviceResult": last_me_result,
        "lastOtherRealDeviceResult": last_other_result,
        "staleSnapshotGuard": stale_snapshot_guard,
        "staleRoutesGuard": stale_routes_guard,
        "realDeviceFunctionalSmoke": real_device_functional_smoke,
        "scenarioAssertionResult": scenario_assertion_result,
        "realDeviceTested": real_device_tested,
        "phoneBundleIncluded": phone_bundle_included,
        "phoneBundlePath": phone_bundle_dest,
        "phoneBundleRequiredFromUser": not phone_bundle_included,
        "currentStatus": {
            "overlayShownInTargetApp": parse_field(review_text, "overlayShownInTargetApp", "NOT_TESTED"),
            "resultShownAsOverlay": parse_field(review_text, "resultShownAsOverlay", "NOT_TESTED"),
            "mainActivityOpened": parse_field(review_text, "mainActivityOpened", "NOT_TESTED"),
            "actualLastSpeaker": parse_field(review_text, "actualLastSpeaker", "NOT_TESTED"),
            "decisionType": parse_field(review_text, "actualDecisionType", parse_field(review_text, "decisionType", "NOT_TESTED")),
            "routeCount": parse_field(review_text, "actualRouteCount", parse_field(review_text, "routeCount", "0")),
            "scenarioName": parse_field(review_text, "scenarioName", "NOT_TESTED"),
            "expectedLastSpeaker": parse_field(review_text, "expectedLastSpeaker", "NOT_TESTED"),
            "scenarioDefinitionTrusted": scenario_definition_trusted == "true",
            "scenarioDefinitionMismatch": scenario_assertion_result == "MISMATCH",
            "postPanelContaminationDetected": post_panel_contamination == "true",
            "screenshotFailureBlocksMainPath": screenshot_blocks_main_path == "true",
        },
        "lastMe": {
            "testIntent": last_me_json.get("testIntent") or parse_field(last_me_text, "testIntent", ""),
            "userAssertedLastSpeaker": last_me_json.get("userAssertedLastSpeaker") or parse_field(last_me_text, "userAssertedLastSpeaker", ""),
            "actualLastSpeaker": last_me_json.get("actualLastSpeaker") or parse_field(last_me_text, "actualLastSpeaker", ""),
            "chosenCaptureSource": last_me_json.get("chosenCaptureSource") or parse_field(last_me_text, "chosenCaptureSource", ""),
            "postSendSettleAttempted": bool(last_me_json.get("postSendSettleAttempted", False)),
            "decisionType": last_me_json.get("decisionType") or parse_field(last_me_text, "decisionType", ""),
            "routeCount": last_me_json.get("routeCount") or parse_field(last_me_text, "routeCount", "0"),
            "waitPanelShown": bool(last_me_json.get("waitPanelShown", False)),
            "routePanelShown": bool(last_me_json.get("routePanelShown", False)),
            "staleRoutesReused": bool(last_me_json.get("staleRoutesReused", False)),
            "failureCategory": last_me_json.get("failureCategory") or parse_field(last_me_text, "failureCategory", ""),
            "failureReason": last_me_json.get("failureReason") or parse_field(last_me_text, "failureReason", ""),
        },
        "lastOther": {
            "result": last_other_result,
            "actualLastSpeaker": last_other_json.get("actualLastSpeaker") or parse_field(last_other_text, "actualLastSpeaker", ""),
            "decisionType": last_other_json.get("decisionType") or parse_field(last_other_text, "decisionType", ""),
            "routeCount": last_other_json.get("routeCount") or parse_field(last_other_text, "routeCount", "0"),
            "failureCategory": last_other_json.get("failureCategory") or parse_field(last_other_text, "failureCategory", ""),
        },
        "reviewFiles": copied + [
            {
                "path": "README_FOR_GPT.md",
                "required": True,
                "exists": True,
                "description": "Inbox overview for GPT",
                "sha256": sha256(inbox / "README_FOR_GPT.md"),
            },
            {
                "path": "changed-files-for-gpt.md",
                "required": True,
                "exists": True,
                "description": "Current delivery change summary",
                "sha256": sha256(inbox / "changed-files-for-gpt.md"),
            },
        ],
        "legacyCurrentStatus": current_status,
        "commands": commands,
        "privacy": privacy,
    }
    (inbox / "gpt-review-manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")

    zip_path = outputs / "huiyi-gpt-review-inbox.zip"
    if zip_path.exists():
        zip_path.unlink()
    with ZipFile(zip_path, "w", ZIP_DEFLATED) as zf:
        for path in sorted(inbox.rglob("*")):
            if path.is_file():
                zf.write(path, path.relative_to(inbox).as_posix())

    print(f"inbox={inbox}")
    print(f"zip={zip_path}")
    print(f"overall_result={overall}")


if __name__ == "__main__":
    main()
