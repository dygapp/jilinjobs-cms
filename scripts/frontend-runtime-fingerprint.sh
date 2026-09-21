#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

tracked_tree_hash() {
  local root="$1"
  git ls-tree -r HEAD -- "$root" \
    | awk '$4 !~ /\/tests\// && $4 !~ /playwright\.config\.ts$/' \
    | sha256sum \
    | awk '{print $1}'
}

{
  echo "node=24"
  echo "nginx=1.29.8-alpine"
  echo "public=$(tracked_tree_hash frontend/public-site)"
  echo "admin=$(tracked_tree_hash frontend/admin)"
  echo "gateway=$(git rev-parse HEAD:frontend/nginx.e2e.conf)"
  echo "dockerfile=$(git rev-parse HEAD:frontend/Dockerfile.runtime)"
  echo "contract=$(git rev-parse HEAD:scripts/frontend-runtime-fingerprint.sh)"
} | sha256sum | awk '{print $1}'
