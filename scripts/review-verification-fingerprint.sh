#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

backend_fingerprint="$(bash scripts/backend-runtime-fingerprint.sh)"
frontend_fingerprint="$(bash scripts/frontend-runtime-fingerprint.sh)"
baseline_fingerprint="$(bash scripts/review-baseline-fingerprint.sh)"

{
  echo "playwright=mcr.microsoft.com/playwright:v1.61.0-noble"
  echo "backend=$backend_fingerprint"
  echo "frontend=$frontend_fingerprint"
  echo "baseline=$baseline_fingerprint"
  echo "public-tests=$(git rev-parse HEAD:frontend/public-site/tests)"
  echo "public-playwright=$(git rev-parse HEAD:frontend/public-site/playwright.config.ts)"
  echo "admin-tests=$(git rev-parse HEAD:frontend/admin/tests)"
  echo "admin-playwright=$(git rev-parse HEAD:frontend/admin/playwright.config.ts)"
  echo "restore=$(git rev-parse HEAD:scripts/restore-review-baseline.sh)"
  echo "start=$(git rev-parse HEAD:scripts/start-review-runtime.sh)"
  echo "fixture=$(git rev-parse HEAD:scripts/apply-human-review-fixture.sh)"
  echo "verify=$(git rev-parse HEAD:scripts/verify-review-runtime.py)"
  echo "ci=$(git rev-parse HEAD:.github/workflows/ci.yml)"
  echo "review=$(git rev-parse HEAD:.github/workflows/review-environment.yml)"
  echo "contract=$(git rev-parse HEAD:scripts/review-verification-fingerprint.sh)"
} | sha256sum | awk '{print $1}'
