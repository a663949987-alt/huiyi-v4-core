#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

if command -v pwsh >/dev/null 2>&1; then
  pwsh -NoProfile -ExecutionPolicy Bypass -File "$SCRIPT_DIR/run-relay-cloud-smoke.ps1" -ProjectRoot "$PROJECT_ROOT"
elif command -v powershell >/dev/null 2>&1; then
  powershell -NoProfile -ExecutionPolicy Bypass -File "$SCRIPT_DIR/run-relay-cloud-smoke.ps1" -ProjectRoot "$PROJECT_ROOT"
else
  echo "PowerShell is required for this smoke script." >&2
  exit 2
fi
