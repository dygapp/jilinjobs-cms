#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

mode="${1:-status}"
state_root="$repo_root/.local-ci/human-review"
state_file="$state_root/state.env"
evidence_root="$repo_root/.local-ci/evidence"
mysql_container="jilinjobs-human-review-mysql"
backend_container="cms-backend"
frontend_container="cms-frontend"
mysql_image="${HUMAN_REVIEW_MYSQL_IMAGE:-mysql:8.4}"
alpine_image="${HUMAN_REVIEW_ALPINE_IMAGE:-alpine:3.23}"
apply_fixture="${HUMAN_REVIEW_APPLY_FIXTURE:-true}"

usage() {
  cat <<'USAGE'
Usage:
  scripts/human-review.sh start
  scripts/human-review.sh status
  scripts/human-review.sh reset
  scripts/human-review.sh stop

Environment:
  HUMAN_REVIEW_APPLY_FIXTURE=true|false
  HUMAN_REVIEW_EVIDENCE_DIR=.local-ci/evidence/<run-id>
  HUMAN_REVIEW_MYSQL_IMAGE=mysql:8.4
  HUMAN_REVIEW_ALPINE_IMAGE=alpine:3.23

start/reset require a clean worktree and a matching exact-HEAD Local Docker CI PASS.
They reuse verified project images and never delete project/base images or BuildKit caches.
USAGE
}

say() { printf '\n==> %s\n' "$*"; }
die() { printf 'human-review: %s\n' "$*" >&2; exit 1; }

kv_value() {
  local file="$1" key="$2"
  awk -F= -v key="$key" '$1 == key {sub(/^[^=]*=/, ""); print; exit}' "$file"
}

container_running() {
  [[ "$(docker inspect --format '{{.State.Running}}' "$1" 2>/dev/null || true)" == "true" ]]
}

http_ready() {
  curl --fail --silent "$1" >/dev/null 2>&1
}

assert_local_ci_idle() {
  local lock_pid=""
  lock_pid="$(cat "$repo_root/.local-ci/lock/pid" 2>/dev/null || true)"
  if [[ "$lock_pid" =~ ^[0-9]+$ ]] && kill -0 "$lock_pid" 2>/dev/null; then
    die "Local Docker CI 正在运行（pid $lock_pid）；人工评审与 Full CI 共用 3306/8080/5173，请等待 CI 结束后重试"
  fi

  local name
  for name in jilinjobs-local-ci-mysql jilinjobs-local-ci-backend jilinjobs-local-ci-frontend; do
    if container_running "$name"; then
      die "检测到 Local Docker CI 容器 $name 正在运行；请先结束 CI"
    fi
  done
}

latest_evidence_dir() {
  local explicit="${HUMAN_REVIEW_EVIDENCE_DIR:-}"
  if [[ -n "$explicit" ]]; then
    if [[ "$explicit" = /* ]]; then
      printf '%s\n' "$explicit"
    else
      printf '%s\n' "$repo_root/$explicit"
    fi
    return
  fi

  local latest_file="$evidence_root/latest.txt"
  [[ -f "$latest_file" ]] || die "缺少 $latest_file；请先执行 bash scripts/local-ci.sh full"
  local run_id
  run_id="$(cat "$latest_file")"
  [[ -n "$run_id" ]] || die "$latest_file 为空"
  printf '%s\n' "$evidence_root/$run_id"
}

load_verified_evidence() {
  command -v docker >/dev/null || die "docker is required"
  command -v git >/dev/null || die "git is required"
  command -v curl >/dev/null || die "curl is required"
  command -v jq >/dev/null || die "jq is required"
  docker info >/dev/null || die "Docker daemon is not available"

  local dirty
  dirty="$(git status --porcelain=v1)"
  [[ -z "$dirty" ]] || die "工作树不干净；start/reset 只启动 exact-HEAD 已验证环境。请先完成变更并重新执行完整 Local Docker CI"

  current_head="$(git rev-parse HEAD)"
  evidence_dir="$(latest_evidence_dir)"
  result_file="$evidence_dir/result.txt"
  subject_file="$evidence_dir/subject.txt"
  [[ -f "$result_file" ]] || die "缺少验证结果：$result_file"
  [[ -f "$subject_file" ]] || die "缺少验证 subject：$subject_file"

  [[ "$(kv_value "$result_file" result)" == "PASS" ]] || die "指定 evidence 不是 PASS：$evidence_dir"
  evidence_subject="$(kv_value "$result_file" subject)"
  [[ "$evidence_subject" == "$current_head" ]] || die "最新完整 CI subject=$evidence_subject，与当前 HEAD=$current_head 不一致；请先重新执行 bash scripts/local-ci.sh full"

  backend_image="$(kv_value "$result_file" backend_image)"
  frontend_image="$(kv_value "$result_file" frontend_image)"
  baseline_image="$(kv_value "$result_file" baseline_image)"
  verification_image="$(kv_value "$result_file" verification_image)"
  backend_fingerprint="$(kv_value "$subject_file" backend_fingerprint)"
  baseline_fingerprint="$(kv_value "$subject_file" baseline_fingerprint)"

  local image
  for image in "$backend_image" "$frontend_image" "$baseline_image" "$verification_image"; do
    [[ -n "$image" ]] || die "evidence 缺少必需 image binding：$result_file"
    docker image inspect "$image" >/dev/null 2>&1 || die "缺少 verified image：$image；请重新执行完整 Local Docker CI"
  done

  docker run --rm "$verification_image" cat /verification.json \
    | jq --exit-status --arg head "$current_head" '.sourceSubject == $head' >/dev/null \
    || die "Review verification marker 未绑定当前 HEAD=$current_head"
}

ensure_mysql() {
  if container_running "$mysql_container"; then
    if docker run --rm --network host "$mysql_image" \
      mysqladmin ping -h127.0.0.1 -uroot -proot --silent >/dev/null 2>&1; then
      return
    fi
    docker rm -f "$mysql_container" >/dev/null 2>&1 || true
  else
    docker rm -f "$mysql_container" >/dev/null 2>&1 || true
  fi

  say "启动 Human Review MySQL 8.4"
  docker run -d --name "$mysql_container" --network host \
    -e MYSQL_ROOT_PASSWORD=root -e MYSQL_ROOT_HOST=% \
    "$mysql_image" --character-set-server=utf8mb4 --collation-server=utf8mb4_0900_ai_ci >/dev/null

  local i
  for i in $(seq 1 60); do
    if docker run --rm --network host "$mysql_image" \
      mysqladmin ping -h127.0.0.1 -uroot -proot --silent >/dev/null 2>&1; then
      return
    fi
    if [[ "$i" -eq 60 ]]; then
      docker logs "$mysql_container" >&2 || true
      die "Human Review MySQL 未在预期时间内 ready；请检查 host 3306 是否被其他服务占用"
    fi
    sleep 2
  done
}

stop_runtime_containers() {
  docker rm -f "$backend_container" "$frontend_container" >/dev/null 2>&1 || true
}

cleanup_runtime_dirs() {
  if ! rm -rf \
    "$repo_root/runtime-static" \
    "$repo_root/runtime-uploads" \
    "$repo_root/review-baseline-restore" \
    "$repo_root/runtime-review-meta" 2>/dev/null; then
    docker run --rm -v "$repo_root:/repo" "$alpine_image" sh -c \
      'rm -rf /repo/runtime-static /repo/runtime-uploads /repo/review-baseline-restore /repo/runtime-review-meta' >/dev/null
  fi
}

write_state() {
  mkdir -p "$state_root"
  cat > "$state_file" <<STATE
source_head=$current_head
evidence_dir=$evidence_dir
backend_image=$backend_image
frontend_image=$frontend_image
baseline_image=$baseline_image
verification_image=$verification_image
fixture=$apply_fixture
started_at=$(date -u +%Y-%m-%dT%H:%M:%SZ)
STATE
}

runtime_healthy() {
  container_running "$mysql_container" \
    && container_running "$backend_container" \
    && container_running "$frontend_container" \
    && http_ready http://127.0.0.1:8080/api/admin/columns \
    && http_ready http://127.0.0.1:5173/ \
    && http_ready http://127.0.0.1:5173/admin/
}

print_access() {
  cat <<'ACCESS'

Human Review Runtime 已就绪：
  Public:  http://127.0.0.1:5173/
  Admin:   http://127.0.0.1:5173/admin/
  Backend: http://127.0.0.1:8080/

Windows + WSL2 通常可直接使用：
  http://localhost:5173/
  http://localhost:5173/admin/
ACCESS
}

restore_and_start() {
  stop_runtime_containers

  say "恢复 verified Review Baseline"
  BASELINE_IMAGE="$baseline_image" \
  EXPECTED_BASELINE_FINGERPRINT="$baseline_fingerprint" \
  EXPECTED_BACKEND_FINGERPRINT="$backend_fingerprint" \
    bash scripts/restore-review-baseline.sh

  say "启动 Backend + Public/Admin Runtime"
  BACKEND_IMAGE="$backend_image" FRONTEND_IMAGE="$frontend_image" \
    bash scripts/start-review-runtime.sh

  if [[ "$apply_fixture" == "true" ]]; then
    say "注入人工评审 fixture"
    REVIEW_BASE_URL=http://127.0.0.1:5173 bash scripts/apply-human-review-fixture.sh
  elif [[ "$apply_fixture" != "false" ]]; then
    die "HUMAN_REVIEW_APPLY_FIXTURE 只能为 true 或 false"
  fi

  runtime_healthy || die "Human Review Runtime 启动后健康检查失败"
  write_state
  print_access
}

start_runtime() {
  assert_local_ci_idle
  load_verified_evidence

  if runtime_healthy && [[ -f "$state_file" ]] \
    && [[ "$(kv_value "$state_file" source_head)" == "$current_head" ]]; then
    say "Human Review Runtime 已在当前 verified HEAD 上运行"
    print_access
    return
  fi

  ensure_mysql
  restore_and_start
}

reset_runtime() {
  assert_local_ci_idle
  load_verified_evidence
  ensure_mysql
  restore_and_start
}

status_runtime() {
  command -v docker >/dev/null || die "docker is required"
  docker info >/dev/null || die "Docker daemon is not available"

  local head
  head="$(git rev-parse HEAD 2>/dev/null || true)"
  printf 'head=%s\n' "$head"

  if [[ -f "$state_file" ]]; then
    printf 'source_head=%s\n' "$(kv_value "$state_file" source_head)"
    printf 'evidence_dir=%s\n' "$(kv_value "$state_file" evidence_dir)"
    printf 'started_at=%s\n' "$(kv_value "$state_file" started_at)"
    printf 'fixture=%s\n' "$(kv_value "$state_file" fixture)"
  else
    printf 'state=absent\n'
  fi

  local name
  for name in "$mysql_container" "$backend_container" "$frontend_container"; do
    if container_running "$name"; then
      printf 'container.%s=running\n' "$name"
    else
      printf 'container.%s=stopped\n' "$name"
    fi
  done

  if runtime_healthy; then
    printf 'status=running\n'
    print_access
  elif container_running "$mysql_container" || container_running "$backend_container" || container_running "$frontend_container"; then
    printf 'status=degraded\n'
  else
    printf 'status=stopped\n'
  fi
}

stop_runtime() {
  say "停止 Human Review Runtime"
  stop_runtime_containers
  docker rm -f "$mysql_container" >/dev/null 2>&1 || true
  cleanup_runtime_dirs
  rm -f "$state_file"
  printf 'status=stopped\n'
  printf '保留 verified project images、基础镜像、依赖缓存与 BuildKit cache。\n'
}

case "$mode" in
  start) start_runtime ;;
  status) status_runtime ;;
  reset) reset_runtime ;;
  stop) stop_runtime ;;
  -h|--help|help) usage ;;
  *) usage >&2; exit 2 ;;
esac
