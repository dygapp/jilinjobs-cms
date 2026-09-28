#!/usr/bin/env bash

# Shared Local CI / Human Review source-identity helpers.
# This file is sourced by repository-owned scripts; it intentionally performs
# no work at load time.

backend_input_paths=(
  backend
)

frontend_input_paths=(
  frontend/public-site
  frontend/admin
  frontend/nginx.e2e.conf
  frontend/Dockerfile.runtime
)

baseline_input_paths=(
  backend/modules/cms-core/src/main/resources/db
  sites/jilinjobs
  data-migrations
  scripts/build-main-review-subset.py
  scripts/prepare-review-baseline.sh
  scripts/restore-review-baseline.sh
)

review_control_input_paths=(
  scripts/start-review-runtime.sh
  scripts/apply-human-review-fixture.sh
  scripts/verify-review-runtime.py
)

review_runtime_input_paths=(
  "${backend_input_paths[@]}"
  "${frontend_input_paths[@]}"
  "${baseline_input_paths[@]}"
  "${review_control_input_paths[@]}"
)

fingerprint_paths() {
  local label="$1"
  shift
  {
    printf 'label=%s\n' "$label"
    git ls-files -co --exclude-standard -z -- "$@" \
      | sort -z \
      | while IFS= read -r -d '' path; do
          printf 'path=%s\n' "$path"
          if [[ -L "$path" ]]; then
            printf 'link=%s\n' "$(readlink "$path")"
          elif [[ -f "$path" ]]; then
            sha256sum "$path"
          else
            printf 'missing\n'
          fi
        done
  } | sha256sum | awk '{print $1}'
}

compute_local_ci_component_fingerprints() {
  backend_fingerprint="$(fingerprint_paths "backend|gradle=9.6.1|jdk=21" "${backend_input_paths[@]}")"
  frontend_fingerprint="$(fingerprint_paths "frontend|node=24|nginx=1.29.8" "${frontend_input_paths[@]}")"
  baseline_input_fingerprint="$(fingerprint_paths "review-baseline|mysql=8.4|alpine=3.23" "${baseline_input_paths[@]}")"
  baseline_fingerprint="$(
    printf 'backend=%s\ninputs=%s\n' "$backend_fingerprint" "$baseline_input_fingerprint" \
      | sha256sum | awk '{print $1}'
  )"
  review_control_fingerprint="$(fingerprint_paths "human-review-control|schema=1" "${review_control_input_paths[@]}")"
  review_runtime_fingerprint="$(
    printf 'schema=1\nbackend=%s\nfrontend=%s\nbaseline=%s\ncontrol=%s\n' \
      "$backend_fingerprint" "$frontend_fingerprint" "$baseline_fingerprint" "$review_control_fingerprint" \
      | sha256sum | awk '{print $1}'
  )"
}

review_runtime_equivalent_to_commit() {
  local commit="$1"
  git cat-file -e "$commit^{commit}" 2>/dev/null || return 2
  git diff --quiet "$commit" -- "${review_runtime_input_paths[@]}" || return 1
  if [[ -n "$(git ls-files --others --exclude-standard -- "${review_runtime_input_paths[@]}")" ]]; then
    return 1
  fi
  return 0
}
