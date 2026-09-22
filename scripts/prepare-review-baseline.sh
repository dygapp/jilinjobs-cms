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

sudo rm -rf runtime-static runtime-uploads review-baseline-build review-main-subset
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

party_root="$repo_root/data-migrations/party/v1"
test -f "$party_root/manifest.json"
test -f "$party_root/index.ndjson"
test -f "$party_root/lists/PARTY_CAROUSEL/index.json"
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
' "$party_root/manifest.json" >/dev/null
test "$(find runtime-static -maxdepth 1 -type d -name 'verification-*' | wc -l)" -eq 0

docker run --rm --network host \
  -e 'DB_URL=jdbc:mysql://127.0.0.1:3306/jilinjobs_cms?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true' \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=root \
  -e CMS_STORAGE_ROOT=/uploads \
  -e CMS_STATIC_ROOT=/runtime-static \
  -v "$repo_root/runtime-uploads:/uploads" \
  -v "$repo_root/runtime-static:/runtime-static" \
  -v "$party_root:/snapshot:ro" \
  "$MIGRATION_IMAGE" /snapshot \
  | tee review-baseline-build/party-import.log

main_source_root="$repo_root/data-migrations/main/v1"
main_subset_root="$repo_root/review-main-subset"
python3 scripts/build-main-review-subset.py \
  --source-root "$main_source_root" \
  --source-surfaces "$repo_root/data-migrations/main/source-surfaces.json" \
  --output-root "$main_subset_root" \
  --per-column 30 \
  | tee review-baseline-build/main-subset-selection.json

docker run --rm --network host \
  -e 'DB_URL=jdbc:mysql://127.0.0.1:3306/jilinjobs_cms?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true' \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=root \
  -e CMS_STORAGE_ROOT=/uploads \
  -e CMS_STATIC_ROOT=/runtime-static \
  -v "$repo_root/runtime-uploads:/uploads" \
  -v "$repo_root/runtime-static:/runtime-static" \
  -v "$main_subset_root:/snapshot:ro" \
  "$MIGRATION_IMAGE" /snapshot \
  | tee review-baseline-build/main-import.log

python3 - <<'PY'
import json, re
from pathlib import Path

def report(path):
    log=Path(path).read_text(encoding='utf-8')
    matches=re.findall(r'CONTENT_MIGRATION_REPORT (\{.*\})', log)
    assert matches, f'missing CONTENT_MIGRATION_REPORT: {path}'
    return json.loads(matches[-1])

party_manifest=json.loads(Path('data-migrations/party/v1/manifest.json').read_text(encoding='utf-8'))
party_expected=party_manifest['acceptedSnapshot']
party_runtime_articles=party_manifest.get('candidateExtension', {}).get('runtimeDatasetArticles', party_expected['articles'])
party_report=report('review-baseline-build/party-import.log')
party_total=party_runtime_articles + party_expected['carouselItems']
assert party_report['total']==party_total and party_report['created']==party_total, party_report
assert party_report['updated']==0 and party_report['skipped']==0 and party_report['conflicts']==0 and party_report['invalid']==0, party_report

subset=json.loads(Path('review-main-subset/review-subset-manifest.json').read_text(encoding='utf-8'))
assert subset['policy']['perColumn']==30, subset
assert subset['selectedArticles']==sum(column['selected'] for column in subset['columns']), subset
for column in subset['columns']:
    assert 0 < column['selected'] <= 30, column
    assert column['selected'] == min(column['available'], 30), column

main_report=report('review-baseline-build/main-import.log')
expected_main=subset['selectedArticles']
assert main_report['total']==expected_main and main_report['created']==expected_main, main_report
assert main_report['updated']==0 and main_report['skipped']==0 and main_report['conflicts']==0 and main_report['invalid']==0, main_report
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

main_subset=json.loads(Path('review-main-subset/review-subset-manifest.json').read_text(encoding='utf-8'))
manifest={
    'schemaVersion':1,
    'sourceSha':os.environ['SOURCE_SHA'],
    'backendFingerprint':os.environ['BACKEND_FINGERPRINT'],
    'baselineFingerprint':os.environ['BASELINE_FINGERPRINT'],
    'mysql':'8.4',
    'mainReviewSubset':main_subset,
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
