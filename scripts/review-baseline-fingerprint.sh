#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

backend_fingerprint="$(bash scripts/backend-runtime-fingerprint.sh)"

{
  echo "mysql=8.4"
  echo "baseline-base=alpine:3.23"
  echo "backend=$backend_fingerprint"
  echo "schema=$(git rev-parse HEAD:backend/modules/cms-core/src/main/resources/db)"
  echo "site=$(git rev-parse HEAD:sites/jilinjobs)"
  echo "canonical=$(git rev-parse HEAD:data-migrations)"
  echo "main-review-subset=$(git rev-parse HEAD:scripts/build-main-review-subset.py)"
  echo "prepare=$(git rev-parse HEAD:scripts/prepare-review-baseline.sh)"
  echo "contract=$(git rev-parse HEAD:scripts/review-baseline-fingerprint.sh)"
} | sha256sum | awk '{print $1}'
