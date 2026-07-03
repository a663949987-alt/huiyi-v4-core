# Changed Files For GPT

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
