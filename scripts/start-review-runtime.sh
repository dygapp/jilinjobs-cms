#!/usr/bin/env bash
set -euo pipefail

: "${BACKEND_IMAGE:?BACKEND_IMAGE is required}"
: "${FRONTEND_IMAGE:?FRONTEND_IMAGE is required}"

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

docker rm -f cms-backend cms-frontend >/dev/null 2>&1 || true

docker run -d --name cms-backend --network host \
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
    docker logs cms-backend
    exit 1
  fi
  sleep 2
done

mkdir -p runtime-review-meta
printf '{}\n' > runtime-review-meta/review-environment.json

docker run -d --name cms-frontend --network host \
  -v "$repo_root/runtime-review-meta/review-environment.json:/usr/share/nginx/html/public/review-environment.json:ro" \
  "$FRONTEND_IMAGE"

for i in $(seq 1 30); do
  if curl --fail --silent http://127.0.0.1:5173/ | grep -qi '<html' \
    && curl --fail --silent http://127.0.0.1:5173/admin/ | grep -qi '<html'; then
    exit 0
  fi
  if [ "$i" -eq 30 ]; then
    docker logs cms-frontend
    exit 1
  fi
  sleep 1
done
