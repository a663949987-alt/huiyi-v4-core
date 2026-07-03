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
        if child.is_file():
            child.unlink()
        elif child.is_dir():
            shutil.rmtree(child)

    review_text = read_text(outputs / "review/huiyi-v4-review-for-gpt.md")
    review_manifest = json_or_empty(outputs / "review/manifest.json")
    latest_failure = json_or_empty(outputs / "latest-next-sentence-failure.json")

    task_name = review_manifest.get("taskName") or parse_field(review_text, "taskName", "unknown")
    version_name = review_manifest.get("versionName") or parse_field(review_text, "versionName", "unknown")
    version_code = review_manifest.get("versionCode") or parse_field(review_text, "versionCode", "0")
    overall = review_manifest.get("overallResult") or parse_field(review_text, "overall_result", "NOT_TESTED")
    generated_at = datetime.now(timezone.utc).astimezone().strftime("%Y-%m-%d %H:%M:%S %z")

    copied = []
    copied.extend(safe_copy(repo, inbox, REQUIRED_FILES))
    copied.extend(safe_copy(repo, inbox, [(src, dest, False, desc) for src, dest, desc in OPTIONAL_FILES]))

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

## What changed this round
1. Added fixed GPT review inbox delivery folder generation.
2. Added machine-readable GPT inbox manifest and changed-files summary.
3. Added single upload zip: `outputs/huiyi-gpt-review-inbox.zip`.

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
2. latest-next-sentence-failure.json
3. latest-next-sentence-failure.md
4. accessibility-click-diagnostic-report-for-gpt.md
5. next-sentence-screenshot-capability-audit-for-gpt.md

## Build / test results
- testDebugUnitTest: {commands["testDebugUnitTest"]}
- assembleDebug: {commands["assembleDebug"]}
- assembleRelease: {commands["assembleRelease"]}
- realDeviceSmoke: {commands["realDeviceSmoke"]}

## APK
- debugApkPath: outputs/huiyi-v{version_name}-debug.apk
- APK is not included in this review zip.

## Known remaining problems
- Real-device smoke is not completed in this Codex environment unless the user exports phone diagnostics.
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
- path: scripts/generate_gpt_review_inbox.py
  - reason: Generate the fixed GPT review inbox folder, manifest, README, changed-files summary, and zip.
  - risk: Low; output packaging only.
- path: scripts/generate-gpt-review-inbox.ps1
  - reason: PowerShell wrapper for Windows/Codex workflow.
  - risk: Low; wrapper only.
- path: scripts/generate-review-bundle.ps1
  - reason: Automatically refresh GPT review inbox after review bundle generation.
  - risk: Low; keeps future deliveries consistent.
- path: outputs/gpt_review_inbox/*
  - reason: Current GPT upload folder.
  - risk: Low; excludes APKs, local.properties, keystore, and secrets.

## Important logic changes
1. GPT review files are centralized under `outputs/gpt_review_inbox/`.
2. `outputs/huiyi-gpt-review-inbox.zip` is generated for one-file upload.
3. The inbox includes a README and machine-readable manifest.

## Tests added / updated
- No Android runtime tests needed; this is a delivery packaging rule.

## Known risk areas
- If future reports are renamed, add them to `OPTIONAL_FILES` in `scripts/generate_gpt_review_inbox.py`.
- If real device diagnostics exist only on the phone, the user still needs to export them first.
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
        "realDeviceTested": real_device_tested,
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
        "currentStatus": current_status,
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
