#!/usr/bin/env bash
set -euo pipefail

: "${BASELINE_IMAGE:?BASELINE_IMAGE is required}"
: "${EXPECTED_BASELINE_FINGERPRINT:?EXPECTED_BASELINE_FINGERPRINT is required}"

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

docker rm -f review-baseline-copy >/dev/null 2>&1 || true
rm -rf review-baseline-restore
mkdir -p review-baseline-restore

container_id="$(docker create "$BASELINE_IMAGE")"
trap 'docker rm -f "$container_id" >/dev/null 2>&1 || true' EXIT

for name in database.sql.gz runtime-static.tar.gz runtime-uploads.tar.gz manifest.json; do
  docker cp "$container_id:/baseline/$name" "review-baseline-restore/$name"
done

docker rm -f "$container_id" >/dev/null
trap - EXIT

jq --exit-status --arg expected "$EXPECTED_BASELINE_FINGERPRINT" \
  '.baselineFingerprint == $expected and .mysql == "8.4"' \
  review-baseline-restore/manifest.json >/dev/null

python3 - <<'PY'
import hashlib, json
from pathlib import Path
root=Path('review-baseline-restore')
manifest=json.loads((root/'manifest.json').read_text(encoding='utf-8'))
for name, meta in manifest['files'].items():
    data=(root/name).read_bytes()
    assert len(data)==meta['sizeBytes'], (name, len(data), meta['sizeBytes'])
    assert hashlib.sha256(data).hexdigest()==meta['sha256'], name
PY

docker run --rm --network host mysql:8.4 \
  mysql --default-character-set=utf8mb4 -h127.0.0.1 -uroot -proot -e \
  'DROP DATABASE IF EXISTS jilinjobs_cms; CREATE DATABASE jilinjobs_cms CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;'

gzip -dc review-baseline-restore/database.sql.gz \
  | docker run --rm -i --network host mysql:8.4 \
      mysql --default-character-set=utf8mb4 -h127.0.0.1 -uroot -proot jilinjobs_cms

sudo rm -rf runtime-static runtime-uploads
mkdir -p runtime-static runtime-uploads
tar -C runtime-static -xzf review-baseline-restore/runtime-static.tar.gz
tar -C runtime-uploads -xzf review-baseline-restore/runtime-uploads.tar.gz
