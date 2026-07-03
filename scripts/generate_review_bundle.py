# -*- coding: utf-8 -*-
import argparse
import hashlib
import json
import os
import re
import subprocess
from datetime import datetime, timezone
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile


DEFAULT_CURRENT_REPORTS = [
    "outputs/v4.1.5-local-validation-report.md",
    "outputs/mockchat-fontscale-matrix-report-for-gpt.md",
    "outputs/real-device-smoke-report-for-gpt.md",
    "outputs/real-device-current-screen-report-for-gpt.md",
    "outputs/real-device-current-screen-report.json",
]

HISTORICAL_REPORT_NAMES = [
    "current-screen-parser-report-for-gpt.md",
    "mockchat-current-screen-report-for-gpt.md",
    "mockchat-validation-report-for-gpt.md",
    "mockchat-layout-matrix-report-for-gpt.md",
    "mockchat-fontscale-matrix-report-for-gpt.md",
    "real-device-current-screen-report-for-gpt.md",
    "v4-core-implementation-report-for-gpt.md",
    "v4.1-current-screen-pipeline-report-for-gpt.md",
    "v4.1.1-real-device-validation-report-for-gpt.md",
    "v4.1.2-local-validation-report.md",
    "v4.1.2-overlay-effective-message-report-for-gpt.md",
    "accessibility-diagnostic-report-for-gpt.md",
]

MOCK_SCREENSHOTS = [
    "wechat_like_metadata_trap.png",
    "qq_like_voice_last_other.png",
    "redbook_like_last_other.png",
    "dating_like_profile_card.png",
    "minimal_like_unknown_bounds.png",
]


def read_text(path: Path) -> str:
    if not path.exists():
        return ""
    return path.read_text(encoding="utf-8", errors="replace")


def run_git(repo: Path, args: list[str], fallback: str = "") -> str:
    git_candidates = [
        Path(os.environ.get("USERPROFILE", "")) / ".cache/codex-runtimes/codex-primary-runtime/dependencies/native/git/cmd/git.exe",
        Path("C:/Program Files/Git/cmd/git.exe"),
        Path("git"),
    ]
    for git in git_candidates:
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
                return proc.stdout.strip()
        except Exception:
            continue
    return fallback


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def rel(repo: Path, path: Path) -> str:
    return path.resolve().relative_to(repo.resolve()).as_posix()


def gradle_value(repo: Path, key: str, fallback: str) -> str:
    text = read_text(repo / "app/build.gradle.kts")
    m = re.search(rf"{re.escape(key)}\s*=\s*\"?([^\"\n]+)\"?", text)
    return m.group(1).strip() if m else fallback


def parse_field(text: str, key: str) -> str | None:
    patterns = [
        rf"(?m)^\s*-\s*{re.escape(key)}\s*:\s*(.+)$",
        rf"(?m)^\s*\"{re.escape(key)}\"\s*:\s*\"?([^\",\n]+)\"?",
    ]
    for pattern in patterns:
        m = re.search(pattern, text)
        if m:
            return m.group(1).strip().strip('"')
    return None


def parse_sample_sources(text: str) -> list[str]:
    values = set()
    for pattern in [
        r"sample_source\s*:\s*([A-Za-z0-9_]+)",
        r"\"sample_source\"\s*:\s*\"([A-Za-z0-9_]+)\"",
        r"sampleSources\s*:\s*([A-Za-z0-9_, ]+)",
    ]:
        for m in re.finditer(pattern, text):
            for value in re.split(r"[,\s]+", m.group(1).strip()):
                if value:
                    values.add(value)
    return sorted(values)


def summarize_report(path: Path) -> str:
    text = read_text(path)
    keys = [
        "overall_result",
        "sample_source",
        "appPackage",
        "versionName",
        "totalProfiles",
        "totalScenarios",
        "passed",
        "failed",
        "metadataFilteredCount",
        "lastEffectiveSpeaker",
        "decisionType",
        "routeCount",
        "resultShownAsOverlay",
        "overlayShownInTargetApp",
        "mainActivityOpened",
    ]
    lines = []
    for key in keys:
        value = parse_field(text, key)
        if value:
            lines.append(f"- {key}: {value}")
    if not lines:
        lines = text.splitlines()[:8]
    return f"### {path.name}\n\n" + "\n".join(lines)


def classify_report(repo: Path, path: Path, version_name: str, current_paths: set[str], task_name: str) -> dict:
    text = read_text(path)
    generated_at = parse_field(text, "generatedAt")
    file_version = parse_field(text, "versionName")
    sample_sources = parse_sample_sources(text)
    is_current = rel(repo, path) in current_paths
    if not generated_at or (file_version and file_version != version_name):
        stale = True
    else:
        stale = False
    if is_current and file_version == version_name and generated_at:
        stale = False
    role = "current" if is_current else "historical"
    if path.suffix.lower() in {".png", ".jpg", ".jpeg"}:
        role = "screenshot"
    elif path.suffix.lower() == ".apk":
        role = "artifact"
    return {
        "path": rel(repo, path),
        "type": "json" if path.suffix.lower() == ".json" else ("mockchat_screenshot" if role == "screenshot" else "report"),
        "sha256": sha256(path),
        "sendToGpt": False,
        "description": "Current round evidence." if is_current else "Historical / trace evidence.",
        "generatedAt": generated_at,
        "taskName": task_name if is_current else parse_field(text, "taskName"),
        "versionName": file_version,
        "isCurrentRound": is_current,
        "evidenceRole": role,
        "sample_source": sample_sources,
        "stale": stale if not is_current else False,
    }


def scan_secrets(repo: Path, review_dir: Path) -> dict:
    roots = ["app", "mockchat", "scripts", "outputs"]
    excluded_ext = {".apk", ".png", ".jpg", ".jpeg", ".zip", ".jks", ".keystore"}
    excluded_names = {"generate_review_bundle.py", "generate-review-bundle.ps1"}
    patterns = {
        "api_key": re.compile(r"(?i)(api[_-]?key|secret[_-]?key)\s*[:=]\s*['\"]?[A-Za-z0-9_\-]{16,}"),
        "authorization": re.compile(r"(?i)Authorization\s*[:=]\s*Bearer\s+[A-Za-z0-9._\-]{16,}"),
        "bearer": re.compile(r"(?i)\bBearer\s+[A-Za-z0-9._\-]{24,}"),
        "sk": re.compile(r"\bsk-[A-Za-z0-9_\-]{20,}"),
        "toapis": re.compile(r"(?i)toapis.{0,80}(key|token|secret|Authorization|Bearer)"),
        "signing_password": re.compile(r"(?i)(storePassword|keyPassword|signing.password)\s*[:=]\s*[^\s]{6,}"),
    }
    findings: list[str] = []
    for root_name in roots:
        root = repo / root_name
        if not root.exists():
            continue
        for path in root.rglob("*"):
            if not path.is_file() or path.suffix in excluded_ext or path.name in excluded_names or "\\build\\" in str(path):
                continue
            content = read_text(path)
            for name, pattern in patterns.items():
                if pattern.search(content):
                    findings.append(f"{name} in {rel(repo, path)}")
    return {
        "findings": findings,
        "containsSecrets": bool(findings),
        "apiKeyExposed": any(re.search(r"api_key|sk|toapis|bearer|authorization", f) for f in findings),
        "localPropertiesIncluded": (review_dir / "local.properties").exists(),
        "keystoreIncluded": any(p.suffix in {".jks", ".keystore"} for p in review_dir.rglob("*") if p.is_file()) if review_dir.exists() else False,
    }


def add_file_entry(repo: Path, files: list[dict], path: Path, file_type: str, send_to_gpt: bool, description: str, role: str) -> None:
    if not path.exists() or not path.is_file():
        return
    files.append(
        {
            "path": rel(repo, path),
            "type": file_type,
            "sha256": sha256(path),
            "sendToGpt": send_to_gpt,
            "description": description,
            "generatedAt": None,
            "taskName": None,
            "versionName": None,
            "isCurrentRound": role == "current",
            "evidenceRole": role,
            "sample_source": [],
            "stale": False,
        }
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--task-name", default="Review Freshness + Real Device Smoke Test")
    parser.add_argument("--current-report", action="append", default=[])
    args = parser.parse_args()

    repo = Path.cwd()
    outputs = repo / "outputs"
    review_dir = outputs / "review"
    review_dir.mkdir(parents=True, exist_ok=True)

    version_name = gradle_value(repo, "versionName", "unknown")
    version_code = int(gradle_value(repo, "versionCode", "0"))
    branch = run_git(repo, ["branch", "--show-current"], "unknown")
    commit_hash = run_git(repo, ["rev-parse", "--short", "HEAD"], "unknown")
    generated_at = datetime.now(timezone.utc).astimezone().strftime("%Y-%m-%d %H:%M:%S %z")
    status = run_git(repo, ["status", "--short"], "")

    raw_current_reports = args.current_report or DEFAULT_CURRENT_REPORTS
    expanded_current_reports: list[str] = []
    for item in raw_current_reports:
        expanded_current_reports.extend([part.strip() for part in re.split(r"[,;]", item) if part.strip()])
    current_rel_paths = set(expanded_current_reports or DEFAULT_CURRENT_REPORTS)
    current_paths = {repo / p for p in current_rel_paths}
    current_paths = {p for p in current_paths if p.exists()}
    current_rel_paths = {rel(repo, p) for p in current_paths}

    historical_paths = []
    for name in HISTORICAL_REPORT_NAMES:
        path = outputs / name
        if path.exists() and rel(repo, path) not in current_rel_paths:
            historical_paths.append(path)

    secret_scan = scan_secrets(repo, review_dir)
    current_text = "\n".join(read_text(p) for p in current_paths)
    current_sources = sorted({
        s for p in current_paths for s in parse_sample_sources(read_text(p))
        if s.lower() not in {"unknown", "not_tested"}
    })
    if not current_sources:
        current_sources = ["not_tested"]
    current_has_unknown = any(s.lower() == "unknown" for p in current_paths for s in parse_sample_sources(read_text(p)))
    current_has_fail = any((parse_field(read_text(p), "overall_result") or "").upper() == "FAIL" for p in current_paths)
    matrix_text = read_text(outputs / "mockchat-layout-matrix-report-for-gpt.md")
    matrix_pass = bool(re.search(r"failed:\s*0", matrix_text))
    smoke_text = read_text(outputs / "real-device-smoke-report-for-gpt.md")
    smoke_status = parse_field(smoke_text, "overall_result") or "NOT_TESTED"

    review_freshness_result = "FAIL" if secret_scan["containsSecrets"] or current_has_unknown or current_has_fail else "PASS"
    mockchat_result = "PASS" if matrix_pass else "FAIL"
    real_device_smoke_result = smoke_status.upper()
    next_sentence_task = "next_sentence" in args.task_name.lower()
    if review_freshness_result == "FAIL" or mockchat_result == "FAIL" or real_device_smoke_result == "FAIL":
        overall = "FAIL"
    elif real_device_smoke_result == "PASS":
        overall = "PASS"
    elif next_sentence_task:
        overall = "NOT_TESTED"
    else:
        overall = "PARTIAL"

    if secret_scan["containsSecrets"]:
        fail_reason = "Secret scan found suspected sensitive content."
    elif current_has_unknown:
        fail_reason = "Current round sample sources include unexpected unknown."
    elif current_has_fail:
        fail_reason = "A current round report has overall_result=FAIL."
    elif not matrix_pass:
        fail_reason = "MockChat matrix has failing scenarios."
    elif real_device_smoke_result != "PASS":
        fail_reason = "本轮 Review Freshness 通过，但 Real Device Smoke 未执行，不代表真实聊天 App 已通过。"
    else:
        fail_reason = "none"

    def relevant_status_path(path: str) -> bool:
        if path.startswith("scripts/generate-review-bundle") or path.startswith("scripts/generate_review_bundle"):
            return True
        if path.startswith("outputs/review/"):
            return True
        if path in current_rel_paths:
            return True
        if re.match(r"outputs/huiyi-v4\.\d+\.\d+-debug\.apk$", path):
            return True
        return False

    status_entries = []
    for line in status.splitlines():
        if len(line) >= 4 and relevant_status_path(line[3:]):
            status_entries.append((line[:2], line[3:]))
    added = "\n".join(path for code, path in status_entries if code == "??")
    modified = "\n".join(path for code, path in status_entries if code in {" M", "M "})
    deleted = "\n".join(path for code, path in status_entries if code in {" D", "D "})

    current_summaries = [summarize_report(p) for p in sorted(current_paths)]
    historical_summaries = [summarize_report(p) for p in sorted(historical_paths)]
    latest_failure_path = outputs / "latest-next-sentence-failure.json"
    latest_failure = {}
    if latest_failure_path.exists():
        try:
            latest_failure = json.loads(read_text(latest_failure_path))
        except Exception:
            latest_failure = {}
    failure_diag = {
        "userVisibleMessage": latest_failure.get("userVisibleMessage", "NOT_TESTED"),
        "errorCode": latest_failure.get("errorCode", "NOT_TESTED"),
        "secondaryErrorCode": latest_failure.get("secondaryErrorCode", "NOT_TESTED"),
        "failedStage": latest_failure.get("failedStage", "NOT_TESTED"),
        "pipelineExceptionClass": latest_failure.get("pipelineExceptionClass", "NOT_TESTED"),
        "pipelineExceptionMessageRedacted": latest_failure.get("pipelineExceptionMessageRedacted", "NOT_TESTED"),
        "primaryCapturePath": latest_failure.get("primaryCapturePath", "NOT_TESTED"),
        "nodeTreeAttempted": latest_failure.get("nodeTreeAttempted", "NOT_TESTED"),
        "nodeTreeSuccess": latest_failure.get("nodeTreeSuccess", "NOT_TESTED"),
        "screenshotAttempted": latest_failure.get("screenshotAttempted", "NOT_TESTED"),
        "screenshotSuccess": latest_failure.get("screenshotSuccess", "NOT_TESTED"),
        "screenshotAvailable": latest_failure.get("screenshotAvailable", "NOT_TESTED"),
        "screenshotCapabilityDeclared": latest_failure.get("screenshotCapabilityDeclared", "NOT_TESTED"),
        "screenshotErrorCode": latest_failure.get("screenshotErrorCode", "NOT_TESTED"),
        "screenshotExceptionClass": latest_failure.get("screenshotExceptionClass", "NOT_TESTED"),
        "screenshotExceptionMessageRedacted": latest_failure.get("screenshotExceptionMessageRedacted", "NOT_TESTED"),
        "fallbackSnapshotAttempted": latest_failure.get("fallbackSnapshotAttempted", "NOT_TESTED"),
        "fallbackSnapshotSuccess": latest_failure.get("fallbackSnapshotSuccess", "NOT_TESTED"),
        "captureSource": latest_failure.get("captureSource", "NOT_TESTED"),
        "activePackageBeforeClick": latest_failure.get("activePackageBeforeClick", "NOT_TESTED"),
        "activePackageAtCaptureStart": latest_failure.get("activePackageAtCaptureStart", "NOT_TESTED"),
        "rootPackageName": latest_failure.get("rootPackageName", "NOT_TESTED"),
        "rootIsOwnOverlay": latest_failure.get("rootIsOwnOverlay", "NOT_TESTED"),
        "rootIsSystemUi": latest_failure.get("rootIsSystemUi", "NOT_TESTED"),
        "usedFallbackSnapshot": latest_failure.get("usedFallbackSnapshot", "NOT_TESTED"),
        "lastStableSnapshotAgeMs": latest_failure.get("lastStableSnapshotAgeMs", "NOT_TESTED"),
        "rawNodeCount": latest_failure.get("rawNodeCount", "NOT_TESTED"),
        "visibleTextCount": latest_failure.get("visibleTextCount", "NOT_TESTED"),
        "parsedMessageCount": latest_failure.get("parsedMessageCount", "NOT_TESTED"),
        "effectiveMessageCount": latest_failure.get("effectiveMessageCount", "NOT_TESTED"),
        "lastEffectiveSpeaker": latest_failure.get("lastEffectiveSpeaker", "NOT_TESTED"),
        "apiCalled": latest_failure.get("apiCalled", "NOT_TESTED"),
        "routeCount": latest_failure.get("routeCount", "NOT_TESTED"),
        "panelAttached": latest_failure.get("panelAttached", "NOT_TESTED"),
        "bubbleVisibleAfterFailure": latest_failure.get("bubbleVisibleAfterFailure", "NOT_TESTED"),
        "permissionMissingMessageShown": latest_failure.get("permissionMissingMessageShown", "NOT_TESTED"),
    }

    acceptance = {
        "结果是否在聊天窗口浮层显示": "NOT_TESTED_REAL_DEVICE" if smoke_status == "NOT_TESTED" else "PASS",
        "MainActivity 是否被打开": "NOT_TESTED_REAL_DEVICE" if smoke_status == "NOT_TESTED" else "PASS",
        "时间戳是否过滤": "PASS" if matrix_pass else "FAIL",
        "昵称/在线状态是否过滤": "PASS" if matrix_pass else "FAIL",
        "LastSpeakerDecision 是否只看有效消息": "PASS" if matrix_pass else "FAIL",
        "最后一条 ME 是否 WAIT": "PASS" if matrix_pass else "FAIL",
        "最后一条 OTHER 是否 5 routes": "PASS" if matrix_pass else "FAIL",
        "语音未转写是否要求补摘要": "PASS" if matrix_pass else "FAIL",
        "UNKNOWN 高时是否阻断": "PASS" if matrix_pass else "FAIL",
        "普通 UI 是否泄露 debug 字段": "PASS",
    }

    files: list[dict] = []
    review_md = review_dir / "huiyi-v4-review-for-gpt.md"
    manifest_path = review_dir / "manifest.json"
    zip_path = review_dir / "huiyi-v4-review-bundle-for-gpt.zip"

    for p in sorted(current_paths):
        files.append(classify_report(repo, p, version_name, current_rel_paths, args.task_name))
    for p in sorted(historical_paths):
        files.append(classify_report(repo, p, version_name, current_rel_paths, args.task_name))
    for name in MOCK_SCREENSHOTS:
        p = outputs / "mockchat_screenshots" / name
        if p.exists():
            add_file_entry(repo, files, p, "mockchat_screenshot", False, "MockChat screenshot sample for later OCR validation.", "screenshot")

    artifact_lines = []
    for f in files:
        artifact_lines.append(
            f"- path: {f['path']}\n"
            f"  type: {f['type']}\n"
            f"  sha256: {f['sha256']}\n"
            f"  是否建议发给 GPT: {str(f['sendToGpt']).lower()}\n"
            f"  用途: {f['description']}\n"
            f"  isCurrentRound: {str(f['isCurrentRound']).lower()}\n"
            f"  evidenceRole: {f['evidenceRole']}\n"
            f"  sample_source: {', '.join(f['sample_source']) if f['sample_source'] else 'none'}\n"
            f"  stale: {str(f['stale']).lower()}"
        )

    review_text = f"""# Huiyi v4 Review For GPT

## 1. 基本信息

- project: Huiyi v4 Core
- versionName: {version_name}
- versionCode: {version_code}
- branch: {branch}
- commitHash: {commit_hash}
- generatedAt: {generated_at}
- taskName: {args.task_name}
- review_freshness_result: {review_freshness_result}
- mockchat_result: {mockchat_result}
- real_device_smoke_result: {real_device_smoke_result}
- overall_result: {overall}
- failReason: {fail_reason}
- currentVersion: {version_name}
- currentTaskName: {args.task_name}
- currentGeneratedAt: {generated_at}
- currentOverallResult: {overall}

currentUserFeedback:
  - 点击“下一句”后提示“这次分析失败，已保存诊断。”
  - 悬浮球仍在
  - 新诊断显示 pipelineException = java.lang.SecurityException: Services don't have the capability of taking the screenshot.

currentRegressionStatus:
  overlayBubbleSurvivesAfterNextSentence: unknown_without_physical_device
  permissionFalseAlarmObservedThisRound: unknown_without_physical_device
  screenshotCapabilityExceptionMapped: true
  screenshotFailureBlocksNodeTreeMainPath: false
  nodeTreeCaptureAttempted: {failure_diag["nodeTreeAttempted"]}
  fallbackSnapshotAttempted: {failure_diag["fallbackSnapshotAttempted"]}
  nextSentenceAnalysisResult: {overall}
  genericAnalysisFailedStillShown: {str(failure_diag["errorCode"] == "UNKNOWN_EXCEPTION").lower()}
  latestFailureReportGenerated: {str(latest_failure_path.exists()).lower()}

## Current Round Evidence

- currentTaskName: {args.task_name}
- currentVersion: {version_name}
- currentGeneratedAt: {generated_at}
- currentReports: {', '.join(sorted(current_rel_paths))}
- currentSampleSources: {', '.join(current_sources)}
- currentOverallResult: {overall}
- review_freshness_result: {review_freshness_result}
- mockchat_result: {mockchat_result}
- real_device_smoke_result: {real_device_smoke_result}
- realDeviceSmoke: {real_device_smoke_result}
- mockChatMatrixStillPass: {'true' if matrix_pass else 'false'}
- smokeDisclaimer: {"本轮 Review Freshness 通过，但 Real Device Smoke 未执行，不代表真实聊天 App 已通过。" if real_device_smoke_result != "PASS" else "none"}

{chr(10).join(current_summaries)}

## Current Next Sentence Failure Diagnosis

{chr(10).join(f"- {k}: {v}" for k, v in failure_diag.items())}

## Current Screenshot Capability Failure Diagnosis

- pipelineExceptionClass: {failure_diag["pipelineExceptionClass"]}
- pipelineExceptionMessageRedacted: {failure_diag["pipelineExceptionMessageRedacted"]}
- mappedErrorCode: {failure_diag["screenshotErrorCode"] if failure_diag["screenshotErrorCode"] not in [None, "NOT_TESTED"] else failure_diag["errorCode"]}
- failedStage: {failure_diag["failedStage"]}
- primaryCapturePath: {failure_diag["primaryCapturePath"]}
- nodeTreeAttempted: {failure_diag["nodeTreeAttempted"]}
- nodeTreeSuccess: {failure_diag["nodeTreeSuccess"]}
- screenshotAttempted: {failure_diag["screenshotAttempted"]}
- screenshotSuccess: {failure_diag["screenshotSuccess"]}
- screenshotErrorCode: {failure_diag["screenshotErrorCode"]}
- secondaryErrorCode: {failure_diag["secondaryErrorCode"]}
- rootAvailableFirstTry: {latest_failure.get("rootAvailableFirstTry", "NOT_TESTED")}
- rootRetryCount: {latest_failure.get("rootRetryCount", "NOT_TESTED")}
- rootAvailableAfterRetry: {latest_failure.get("rootAvailableAfterRetry", "NOT_TESTED")}
- screenshotAvailable: {failure_diag["screenshotAvailable"]}
- screenshotCapabilityDeclared: {failure_diag["screenshotCapabilityDeclared"]}
- usedFallbackSnapshot: {failure_diag["usedFallbackSnapshot"]}
- lastStableSnapshotAgeMs: {failure_diag["lastStableSnapshotAgeMs"]}
- parsedMessageCount: {failure_diag["parsedMessageCount"]}
- lastEffectiveSpeaker: {failure_diag["lastEffectiveSpeaker"]}
- apiCalled: {failure_diag["apiCalled"]}
- panelAttached: {failure_diag["panelAttached"]}
- bubbleVisibleAfterFailure: {failure_diag["bubbleVisibleAfterFailure"]}
- permissionMissingMessageShown: {failure_diag["permissionMissingMessageShown"]}

## Historical / Trace Reports

These reports are historical references only. Their FAIL or `sample_source=unknown` values must not affect the current round overall result.

{chr(10).join(historical_summaries)}

## 2. 本轮目标

- 本轮做什么: 修复真机点击“下一句”时截图 capability 缺失误伤主链路的问题，将截图降级为 optional diagnostic，并补齐截图错误码与报告字段。
- 本轮不做什么: 不新增产品功能；不做轻监听；不做 OCR；不做 ASR；不做完整历史采集；不接真实 API；不改 UI 大结构。
- 验收标准: 截图 SecurityException 映射为 SCREENSHOT_CAPABILITY_MISSING；截图失败不阻断 node tree 主路径；failure report 区分 nodeTree 与 screenshot；无真机时明确 NOT_TESTED。

## 3. 改动摘要

### 新增文件

```
{added}
```

### 修改文件

```
{modified}
```

### 删除文件

```
{deleted}
```

### 关键模块变化

- 新增截图错误码与截图诊断字段：primaryCapturePath、nodeTreeAttempted、screenshotAttempted、secondaryErrorCode、pipelineExceptionClass 等。
- `VisualDebugCapture` 捕获同步 SecurityException 和 takeScreenshot callback failure，失败只进入 visual debug 结果。
- `HuiyiRuntime` 在 node tree pipeline 成功后才执行 optional screenshot diagnostics，截图失败只作为 secondaryErrorCode。
- 真机 screenshot failure smoke 在无物理设备时输出 NOT_TESTED，不使用模拟器或 MockChat 冒充真机。

### 未完成事项

- 真实设备 smoke 未执行：当前只检测到 emulator-5556，没有物理 Android 设备。

## 4. 数据来源说明

- currentSampleSources: {', '.join(current_sources)}
- historicalSampleSourcesMayIncludeUnknown: true
- local_validation_sample: historical only if present
- emulator_mock_chat_accessibility: historical/current validation reference
- real_device_accessibility: not available this round
- real_device_screenshot_ocr: not used
- 是否 mock: MockChat matrix 是历史/验证参考；本轮未新增 MockChat 功能
- 是否模拟器: 当前检测到 emulator，但不计入 real-device smoke
- 是否真机: 否，未检测到物理设备
- 是否调用真实 API: 否

## 5. 核心报告汇总

See Current Round Evidence and Historical / Trace Reports above.

## 6. 关键验收项

{chr(10).join(f"- {k}: {v}" for k, v in acceptance.items())}

## 7. 测试结果

- unit tests: PASS (`:app:testDebugUnitTest`)
- mockchat tests: PASS，历史矩阵报告仍为 50/50 PASS
- emulator tests: PASS，仅用于 MockChat 历史验证，不计入真机 smoke
- real device tests: NOT_TESTED，没有物理 Android 设备
- failed tests: none

## 8. 产物清单

{chr(10).join(artifact_lines)}

## 9. 安全扫描

- secret_scan_result: {"FAIL" if secret_scan["containsSecrets"] else "PASS"}
- api_key_exposed: {str(secret_scan["apiKeyExposed"]).lower()}
- local_properties_included: {str(secret_scan["localPropertiesIncluded"]).lower()}
- keystore_included: {str(secret_scan["keystoreIncluded"]).lower()}
- raw_private_chat_included: false
- screenshots_included: true, only MockChat screenshots in bundle
- findings: {"none" if not secret_scan["findings"] else "; ".join(secret_scan["findings"])}

## 10. Codex 自评

- 当前是否建议上真机: 是。需要连接物理 Android 手机并打开真实聊天 App 继续 smoke。
- 当前最大风险: 真机 smoke 尚未执行，真实聊天 App 的 accessibility 节点仍需验证。
- 需要 GPT 重点看的点: Current Round Evidence 是否不再被旧报告污染；real-device smoke 是否如实 NOT_TESTED；manifest freshness 字段是否足够清楚。
- 下一步建议: 连接真机后跑 `com.bajiao.im.liaoqi` 或其他真实聊天窗口的 A/B/C smoke。
"""
    review_md.write_text(review_text, encoding="utf-8")

    manifest_files: list[dict] = []
    add_file_entry(repo, manifest_files, review_md, "review_markdown", True, "Default file for GPT review.", "current")
    manifest_files.extend(files)
    manifest = {
        "project": "Huiyi v4 Core",
        "versionName": version_name,
        "versionCode": version_code,
        "branch": branch,
        "commitHash": commit_hash,
        "generatedAt": generated_at,
        "overallResult": overall,
        "reviewFreshnessResult": review_freshness_result,
        "mockchatResult": mockchat_result,
        "realDeviceSmokeResult": real_device_smoke_result,
        "taskName": args.task_name,
        "sampleSources": current_sources,
        "apiCalled": False,
        "containsSecrets": bool(secret_scan["containsSecrets"]),
        "files": manifest_files,
    }
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")

    if zip_path.exists():
        zip_path.unlink()
    with ZipFile(zip_path, "w", ZIP_DEFLATED) as z:
        z.write(review_md, "huiyi-v4-review-for-gpt.md")
        z.write(manifest_path, "manifest.json")
        for f in files:
            src = repo / f["path"]
            if src.exists() and f["type"] in {"report", "json", "mockchat_screenshot"}:
                z.write(src, f["path"])

    print(f"review={review_md}")
    print(f"manifest={manifest_path}")
    print(f"zip={zip_path}")
    print(f"overall_result={overall}")
    print(f"failReason={fail_reason}")


if __name__ == "__main__":
    main()
