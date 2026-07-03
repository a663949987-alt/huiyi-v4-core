import argparse
import cgi
import json
import os
import shutil
import subprocess
import sys
import tempfile
import time
import zipfile
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path


ALLOWED_PREFIXES = (
    "README_FOR_GPT.md",
    "one-tap-feedback-manifest.json",
    "latest-session/",
    "current-screen/",
    "metadata/",
    "unsupported-app-adaptation-report.json",
    "unsupported-app-adaptation-report-for-gpt.md",
)


def run(cmd, cwd):
    completed = subprocess.run(cmd, cwd=cwd, text=True, capture_output=True, shell=False)
    if completed.returncode != 0:
        raise RuntimeError((completed.stderr or completed.stdout or str(cmd)).strip())
    return completed.stdout.strip()


def safe_session_id(value):
    cleaned = "".join(ch if ch.isalnum() or ch in ("-", "_") else "-" for ch in (value or "no-session"))
    return cleaned[:80] or "no-session"


def read_zip_text(zip_path, name):
    with zipfile.ZipFile(zip_path) as zf:
        with zf.open(name) as stream:
            return stream.read().decode("utf-8", errors="replace")


def privacy_is_safe(zip_path):
    raw = read_zip_text(zip_path, "metadata/privacy-scan.json")
    data = json.loads(raw)
    return (
        data.get("safeForPublicGitHub") is True
        and data.get("containsRawPrivateChat") is False
        and data.get("containsRawScreenshot") is False
        and data.get("containsApiKey") is False
        and data.get("containsToken") is False
        and data.get("containsKeystore") is False
    )


def manifest_summary(zip_path):
    raw = read_zip_text(zip_path, "one-tap-feedback-manifest.json")
    data = json.loads(raw)
    latest = data.get("latestSession", {})
    feedback = data.get("feedback", {})
    return {
        "appVersionName": data.get("appVersionName") or "",
        "appVersionCode": int(data.get("appVersionCode") or 0),
        "bundleType": data.get("bundleType") or "",
        "sessionId": latest.get("sessionId") or "no-session",
        "terminalState": latest.get("terminalState") or "UNKNOWN",
        "appPackage": latest.get("appPackage") or "unknown",
        "actualLastSpeaker": latest.get("actualLastSpeaker") or "UNKNOWN",
        "decisionType": latest.get("decisionType") or "UNKNOWN",
        "routeCount": latest.get("routeCount") or 0,
        "waitPanelShown": latest.get("waitPanelShown") or False,
        "routePanelShown": latest.get("routePanelShown") or False,
        "errorCode": latest.get("errorCode") or "",
        "userMarkedWrong": latest.get("userMarkedWrong") or False,
        "userCorrectionLastSpeaker": latest.get("userCorrectionLastSpeaker") or "NONE",
        "feedbackTargetSessionId": feedback.get("feedbackTargetSessionId") or "",
        "feedbackTargetSessionFound": feedback.get("feedbackTargetSessionFound") is True,
        "feedbackTriggeredNewAnalysis": feedback.get("feedbackTriggeredNewAnalysis") is True,
        "feedbackReCapturedCurrentRoot": feedback.get("feedbackReCapturedCurrentRoot") is True,
        "feedbackUsedOverlayStateAsPreAnalysis": feedback.get("feedbackUsedOverlayStateAsPreAnalysis") is True,
        "cloudEnabled": latest.get("cloudEnabled") is True,
        "cloudAttempted": latest.get("cloudAttempted") is True,
        "cloudContractImplemented": latest.get("cloudContractImplemented") is True,
    }


def current_required_version(repo):
    latest_json = repo / "outputs" / "update_server" / "latest.json"
    if not latest_json.exists():
        return 0, ""
    data = json.loads(latest_json.read_text(encoding="utf-8"))
    return int(data.get("versionCode") or 0), data.get("versionName") or ""


def validate_upload(summary, repo):
    required_code, required_name = current_required_version(repo)
    if required_code and summary["appVersionCode"] < required_code:
        raise PermissionError(
            f"STALE_PHONE_BUNDLE_VERSION: uploaded={summary['appVersionName']}({summary['appVersionCode']}) "
            f"required={required_name}({required_code})"
        )
    if summary["feedbackTriggeredNewAnalysis"] or summary["feedbackReCapturedCurrentRoot"]:
        raise PermissionError("FEEDBACK_EXPORT_RECAPTURED_OR_RERAN_ANALYSIS")
    if not summary["feedbackTargetSessionFound"] or not summary["feedbackTargetSessionId"]:
        raise PermissionError("FEEDBACK_TARGET_SESSION_NOT_BOUND")
    if summary["cloudEnabled"] or summary["cloudAttempted"] or summary["cloudContractImplemented"]:
        raise PermissionError("CLOUD_ANALYSIS_MUST_REMAIN_TODO")


def clear_dir(path):
    if path.exists():
        shutil.rmtree(path)
    path.mkdir(parents=True, exist_ok=True)


def extract_allowed(zip_path, destination):
    clear_dir(destination)
    with zipfile.ZipFile(zip_path) as zf:
        for info in zf.infolist():
            name = info.filename.replace("\\", "/")
            if name.endswith("/"):
                continue
            if ".." in Path(name).parts:
                continue
            if not any(name == prefix or name.startswith(prefix) for prefix in ALLOWED_PREFIXES):
                continue
            target = destination / name
            target.parent.mkdir(parents=True, exist_ok=True)
            with zf.open(info) as source, target.open("wb") as out:
                shutil.copyfileobj(source, out)


def write_inbox_readme(repo, summary, uploaded_at, commit_hash="PENDING"):
    readme = repo / "outputs" / "gpt_review_inbox" / "README_FOR_GPT.md"
    readme.parent.mkdir(parents=True, exist_ok=True)
    readme.write_text(
        f"""# Huiyi v4 GPT Review Inbox

## Source
- phoneBundleIncluded: true
- oneTapFeedbackIncluded: true
- uploadedFromPhone: true
- latestPhoneUploadAt: {uploaded_at}
- latestPhoneAppVersionName: {summary['appVersionName']}
- latestPhoneAppVersionCode: {summary['appVersionCode']}
- latestPhoneSessionId: {summary['sessionId']}
- githubCommitHash: {commit_hash}
- githubReviewPath: outputs/gpt_review_inbox/phone/latest/
- realDeviceTested: true
- acceptedSmokeSet: Liaoqi LAST_ME / Liaoqi LAST_OTHER / Unsupported App
- phoneLatestIsCurrentVersion: true
- cloudStillTodo: true

## Latest Phone Conclusion
- terminalState: {summary['terminalState']}
- appPackage: {summary['appPackage']}
- actualLastSpeaker: {summary['actualLastSpeaker']}
- decisionType: {summary['decisionType']}
- routeCount: {summary['routeCount']}
- waitPanelShown: {summary['waitPanelShown']}
- routePanelShown: {summary['routePanelShown']}
- errorCode: {summary['errorCode']}
- userMarkedWrong: {summary['userMarkedWrong']}
- userCorrectionLastSpeaker: {summary['userCorrectionLastSpeaker']}
- feedbackTargetSessionId: {summary['feedbackTargetSessionId']}
- feedbackTargetSessionFound: {summary['feedbackTargetSessionFound']}
- feedbackTriggeredNewAnalysis: {summary['feedbackTriggeredNewAnalysis']}
- feedbackReCapturedCurrentRoot: {summary['feedbackReCapturedCurrentRoot']}
- feedbackUsedOverlayStateAsPreAnalysis: {summary['feedbackUsedOverlayStateAsPreAnalysis']}

## GPT Should Inspect
1. outputs/gpt_review_inbox/phone/latest/README_FOR_GPT.md
2. outputs/gpt_review_inbox/phone/latest/one-tap-feedback-manifest.json
3. outputs/gpt_review_inbox/phone/latest/latest-session/next-sentence-flight-record.json
""",
        encoding="utf-8",
    )


def write_inbox_manifest(repo, summary, uploaded_at, commit_hash="PENDING"):
    manifest = repo / "outputs" / "gpt_review_inbox" / "gpt-review-manifest.json"
    manifest.parent.mkdir(parents=True, exist_ok=True)
    data = {
        "project": "huiyi-v4",
        "phoneBundleIncluded": True,
        "oneTapFeedbackIncluded": True,
        "uploadedFromPhone": True,
        "latestPhoneUploadAt": uploaded_at,
        "latestPhoneAppVersionName": summary["appVersionName"],
        "latestPhoneAppVersionCode": summary["appVersionCode"],
        "latestPhoneSessionId": summary["sessionId"],
        "githubCommitHash": commit_hash,
        "githubReviewPath": "outputs/gpt_review_inbox/phone/latest/",
        "realDeviceTested": True,
        "acceptedSmokeSet": [
            "Liaoqi LAST_ME: ME -> WAIT",
            "Liaoqi LAST_OTHER: OTHER -> routes",
            "Unsupported App: show unsupported prompt and export adapter bundle",
        ],
        "phoneLatestIsCurrentVersion": True,
        "cloudStillTodo": True,
        "latestPhoneConclusion": summary,
    }
    manifest.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")


def handle_upload(repo, zip_path):
    if not privacy_is_safe(zip_path):
        raise PermissionError("GITHUB_UPLOAD_PRIVACY_BLOCKED")
    summary = manifest_summary(zip_path)
    validate_upload(summary, repo)
    session = safe_session_id(summary["sessionId"])
    stamp = datetime.now(timezone.utc).strftime("%Y%m%d-%H%M%S")
    uploaded_at = datetime.now(timezone.utc).isoformat()
    latest = repo / "outputs" / "gpt_review_inbox" / "phone" / "latest"
    archive = repo / "outputs" / "gpt_review_inbox" / "phone" / "archive" / f"{stamp}-{session}"
    extract_allowed(zip_path, latest)
    extract_allowed(zip_path, archive)
    write_inbox_readme(repo, summary, uploaded_at)
    write_inbox_manifest(repo, summary, uploaded_at)
    run(["git", "add", "outputs/gpt_review_inbox"], repo)
    commit_message = f"Add phone one tap feedback {session}"
    status = run(["git", "status", "--short", "outputs/gpt_review_inbox"], repo)
    if status:
        run(["git", "commit", "-m", commit_message], repo)
        run(["git", "push", "origin", "main"], repo)
    commit_hash = run(["git", "rev-parse", "HEAD"], repo)
    write_inbox_readme(repo, summary, uploaded_at, commit_hash)
    write_inbox_manifest(repo, summary, uploaded_at, commit_hash)
    run(["git", "add", "outputs/gpt_review_inbox/README_FOR_GPT.md", "outputs/gpt_review_inbox/gpt-review-manifest.json"], repo)
    if run(["git", "status", "--short", "outputs/gpt_review_inbox"], repo):
        run(["git", "commit", "-m", f"Update phone feedback commit hash {session}"], repo)
        run(["git", "push", "origin", "main"], repo)
        commit_hash = run(["git", "rev-parse", "HEAD"], repo)
    return {
        "githubCommitHash": commit_hash,
        "githubBranch": "main",
        "githubReviewPath": "outputs/gpt_review_inbox/phone/latest/",
        "githubReviewUrl": "https://github.com/a663949987-alt/huiyi-v4-core/tree/main/outputs/gpt_review_inbox/phone/latest",
        "uploadedAt": uploaded_at,
    }


class Handler(BaseHTTPRequestHandler):
    repo = None

    def do_GET(self):
        if self.path == "/health":
            self.send_json(200, {"ok": True})
        else:
            self.send_json(404, {"error": "not_found"})

    def do_POST(self):
        if self.path != "/api/huiyi/review-upload":
            self.send_json(404, {"error": "not_found"})
            return
        ctype, pdict = cgi.parse_header(self.headers.get("content-type"))
        if ctype != "multipart/form-data":
            self.send_json(400, {"error": "multipart_required"})
            return
        pdict["boundary"] = bytes(pdict["boundary"], "utf-8")
        pdict["CONTENT-LENGTH"] = int(self.headers.get("content-length", 0))
        form = cgi.FieldStorage(fp=self.rfile, headers=self.headers, environ={"REQUEST_METHOD": "POST"})
        item = form["oneTapFeedbackZip"] if "oneTapFeedbackZip" in form else None
        if item is None or not getattr(item, "file", None):
            self.send_json(400, {"error": "zip_missing"})
            return
        with tempfile.NamedTemporaryFile(delete=False, suffix=".zip") as tmp:
            shutil.copyfileobj(item.file, tmp)
            tmp_path = Path(tmp.name)
        try:
            result = handle_upload(self.repo, tmp_path)
            self.send_json(200, result)
        except PermissionError as error:
            self.send_json(403, {"error": str(error)})
        except Exception as error:
            self.send_json(500, {"error": type(error).__name__, "message": str(error)})
        finally:
            tmp_path.unlink(missing_ok=True)

    def log_message(self, fmt, *args):
        sys.stdout.write("[%s] %s\n" % (self.log_date_time_string(), fmt % args))
        sys.stdout.flush()

    def send_json(self, code, data):
        raw = json.dumps(data, ensure_ascii=False).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(raw)))
        self.end_headers()
        self.wfile.write(raw)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--repo", required=True)
    parser.add_argument("--host", default="0.0.0.0")
    parser.add_argument("--port", type=int, default=8791)
    args = parser.parse_args()
    Handler.repo = Path(args.repo).resolve()
    server = ThreadingHTTPServer((args.host, args.port), Handler)
    print(f"Huiyi review upload gateway listening on http://{args.host}:{args.port}", flush=True)
    server.serve_forever()


if __name__ == "__main__":
    main()
