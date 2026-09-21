#!/usr/bin/env bash
set -euo pipefail

: "${BACKEND_IMAGE:?BACKEND_IMAGE is required}"
: "${MIGRATION_IMAGE:?MIGRATION_IMAGE is required}"
: "${BASELINE_IMAGE:?BASELINE_IMAGE is required}"
: "${BACKEND_FINGERPRINT:?BACKEND_FINGERPRINT is required}"
: "${BASELINE_FINGERPRINT:?BASELINE_FINGERPRINT is required}"
: "${SOURCE_SHA:?SOURCE_SHA is required}"

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

cleanup() {
  docker rm -f review-baseline-backend >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker rm -f review-baseline-backend >/dev/null 2>&1 || true

docker run --rm --network host mysql:8.4 \
  mysql --default-character-set=utf8mb4 -h127.0.0.1 -uroot -proot -e \
  'DROP DATABASE IF EXISTS jilinjobs_cms; CREATE DATABASE jilinjobs_cms CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;'

sudo rm -rf runtime-static runtime-uploads review-baseline-build
mkdir -p runtime-static runtime-uploads review-baseline-build

docker run -d --name review-baseline-backend --network host \
  -e 'DB_URL=jdbc:mysql://127.0.0.1:3306/jilinjobs_cms?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true' \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=root \
  -e CMS_STATIC_ROOT=/runtime-static \
  -e CMS_STORAGE_ROOT=/uploads \
  -e CMS_SITE_PACKAGE_ROOT=/site-package \
  -e CMS_SITE_PACKAGE_BOOTSTRAP_ON_START=true \
  -v "$repo_root/runtime-static:/runtime-static" \
  -v "$repo_root/runtime-uploads:/uploads" \
  -v "$repo_root/sites/jilinjobs:/site-package:ro" \
  "$BACKEND_IMAGE"

for i in $(seq 1 60); do
  if curl --fail --silent http://127.0.0.1:8080/api/admin/columns >/dev/null; then
    break
  fi
  if [ "$i" -eq 60 ]; then
    docker logs review-baseline-backend
    exit 1
  fi
  sleep 2
done

canonical_root="$repo_root/data-migrations/party/v1"
test -f "$canonical_root/manifest.json"
test -f "$canonical_root/index.ndjson"
test -f "$canonical_root/lists/PARTY_CAROUSEL/index.json"
jq --exit-status '
  (.status == "accepted-canonical" or .status == "candidate-extension")
  and (if .status == "candidate-extension" then
    .candidateExtension.status == "pending-human-review"
    and .candidateExtension.acceptedEu29Articles == .acceptedSnapshot.articles
    and .candidateExtension.runtimeDatasetArticles == (.acceptedSnapshot.articles + .candidateExtension.addedArticles)
    and .candidateExtension.reportedTotal == .candidateExtension.addedArticles
    and .candidateExtension.unresolved == 0
  else true end)
  and .acceptedSnapshot.articles == 181
  and .acceptedSnapshot.internalArticles == 120
  and .acceptedSnapshot.externalArticles == 61
  and .acceptedSnapshot.carouselItems == 4
  and .acceptedSnapshot.unresolved == 0
  and .acceptedSnapshot.artifactDigest == "sha256:230ac0df997b3dc913ed38503a8289eae30d8bb0a455fd858e388ddc27066148"
' "$canonical_root/manifest.json" >/dev/null
test "$(find runtime-static -maxdepth 1 -type d -name 'verification-*' | wc -l)" -eq 0

docker run --rm --network host \
  -e 'DB_URL=jdbc:mysql://127.0.0.1:3306/jilinjobs_cms?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true' \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=root \
  -e CMS_STORAGE_ROOT=/uploads \
  -e CMS_STATIC_ROOT=/runtime-static \
  -v "$repo_root/runtime-uploads:/uploads" \
  -v "$repo_root/runtime-static:/runtime-static" \
  -v "$canonical_root:/snapshot:ro" \
  "$MIGRATION_IMAGE" /snapshot \
  | tee review-baseline-build/canonical-import.log

python3 - <<'PY'
import json, re
from pathlib import Path
manifest=json.loads(Path('data-migrations/party/v1/manifest.json').read_text(encoding='utf-8'))
expected=manifest['acceptedSnapshot']
runtime_articles=manifest.get('candidateExtension', {}).get('runtimeDatasetArticles', expected['articles'])
log=Path('review-baseline-build/canonical-import.log').read_text(encoding='utf-8')
matches=re.findall(r'CONTENT_MIGRATION_REPORT (\{.*\})', log)
assert matches, 'missing CONTENT_MIGRATION_REPORT'
report=json.loads(matches[-1])
expected_total=runtime_articles + expected['carouselItems']
assert report['total']==expected_total and report['created']==expected_total, report
assert report['updated']==0 and report['skipped']==0 and report['conflicts']==0 and report['invalid']==0, report
PY

docker rm -f review-baseline-backend >/dev/null
trap - EXIT

docker run --rm --network host mysql:8.4 \
  mysqldump -h127.0.0.1 -uroot -proot --single-transaction --skip-comments --no-tablespaces jilinjobs_cms \
  | gzip -9n > review-baseline-build/database.sql.gz

tar --sort=name --mtime='UTC 1970-01-01' --owner=0 --group=0 --numeric-owner -C runtime-static -cf - . \
  | gzip -9n > review-baseline-build/runtime-static.tar.gz
tar --sort=name --mtime='UTC 1970-01-01' --owner=0 --group=0 --numeric-owner -C runtime-uploads -cf - . \
  | gzip -9n > review-baseline-build/runtime-uploads.tar.gz

python3 - <<'PY'
import hashlib, json, os
from pathlib import Path

root=Path('review-baseline-build')
files={}
for name in ('database.sql.gz','runtime-static.tar.gz','runtime-uploads.tar.gz'):
    data=(root/name).read_bytes()
    files[name]={'sizeBytes':len(data),'sha256':hashlib.sha256(data).hexdigest()}

manifest={
    'schemaVersion':1,
    'sourceSha':os.environ['SOURCE_SHA'],
    'backendFingerprint':os.environ['BACKEND_FINGERPRINT'],
    'baselineFingerprint':os.environ['BASELINE_FINGERPRINT'],
    'mysql':'8.4',
    'files':files,
}
(root/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,sort_keys=True,indent=2)+'\n',encoding='utf-8')
PY

cat > review-baseline-build/Dockerfile <<'EOF'
FROM alpine:3.23
COPY database.sql.gz /baseline/database.sql.gz
COPY runtime-static.tar.gz /baseline/runtime-static.tar.gz
COPY runtime-uploads.tar.gz /baseline/runtime-uploads.tar.gz
COPY manifest.json /baseline/manifest.json
EOF

docker build \
  --label "org.opencontainers.image.source=https://github.com/${GITHUB_REPOSITORY:-dygapp/jilinjobs-cms}" \
  --label "org.opencontainers.image.revision=$SOURCE_SHA" \
  --label "io.jilinjobs.backend-fingerprint=$BACKEND_FINGERPRINT" \
  --label "io.jilinjobs.review-baseline-fingerprint=$BASELINE_FINGERPRINT" \
  -t "$BASELINE_IMAGE" review-baseline-build
