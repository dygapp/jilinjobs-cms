---
id: execution-unit:eu63-review-runtime-cache
type: execution-unit
status: active
readiness: PASS
controlling_issue: 200
base_sha: 87f5dcee2dff898ad5a35b4e523d022d6f4705db
---

# EU-63 人工评审运行时缓存重构

## 目标

把完整 CI 已经验证的前端构建、人工评审数据基线与对应浏览器验证结果物化为可跨 GitHub-hosted Runner 复用的 immutable artifact，缩短人工评审环境启动时间。

## 就绪结论

`readiness-check = PASS`。

- 当前 Work locator 在本单元进入前为 `NONE`，没有并发 active Execution Unit；
- Issue #200 已明确 Goal、Scope、失败行为与验收，不改变 Product / Domain / Site Definition / Historical Migration 语义；
- `docs/technical/ci-verification-runtime.md` 已拥有 GitHub Actions / GHCR / fingerprint / Review Runtime 的长期 HOW；
- 本次选择属于 CI Runtime implementation，可在不改变产品行为的前提下独立验证和回滚。

## 技术方案

### 1. 前端运行时标识

新增 Frontend Runtime fingerprint，覆盖：

- `frontend/public-site/**`，包括受版本控制的 `package-lock.json`；
- `frontend/admin/**`，包括受版本控制的 `package-lock.json`；
- `frontend/nginx.e2e.conf`；
- Frontend Runtime Dockerfile 与 fingerprint contract 自身。

Public / Admin 的正式依赖安装使用 `npm ci`；相同 fingerprint 的 Frontend Runtime image 只校验并复用，不覆盖已有 tag。

完整 CI 继续从 exact Head 执行 Public / Admin formal build 与 Integrated Browser verification；全部通过后，把同一批 `dist` 组装成：

`ghcr.io/dygapp/jilinjobs-cms-frontend-runtime:<frontend-fingerprint>`

人工评审 Workflow 只消费该 verified image，不再执行 Node setup、npm install / ci 或 frontend build。

### 2. 评审数据基线标识

新增 Review Baseline fingerprint，至少覆盖：

- Backend Runtime fingerprint；
- MySQL Runtime contract；
- `backend/modules/cms-core/src/main/resources/db/**`；
- `sites/jilinjobs/**`；
- `data-migrations/**`；
- baseline preparation contract 自身。

Baseline 使用 logical DB dump + `runtime-static` + `runtime-uploads`，不缓存 MySQL raw datadir。

Baseline 内容只包括：

`Generic schema → Site Definition → stable assets → canonical historical migration`

Human Review fixture 不进入 baseline，每次人工评审恢复 baseline 后单独注入。

### 3. 评审验证标识

新增 Review Verification fingerprint，由 Backend / Frontend / Baseline fingerprint 与会影响 Review Browser claim 的 Playwright / verification inputs 共同决定。

完整 CI 只有在：

- Backend / Public / Admin formal verification 通过；
- Integrated Browser verification 通过；
- Review Baseline 能恢复；
- canonical Review Runtime probe 通过；

之后，才发布 verified marker。

同一 fingerprint 的 marker 不覆盖。完整 CI 仍执行 exact-Head 正式验证；若 Frontend Runtime、Review Baseline 与 matching marker 都已命中且 provenance 校验通过，缓存发布阶段可直接复用 marker，不重复执行该 marker 已证明的 Review Runtime probe。

人工评审 Workflow 命中 matching marker 时不重复执行完整 Playwright；如果 image / marker 缺失或 provenance 无法确认，则失败关闭并要求目标 Head 先完成完整 CI，不现场降级构建。

## 执行范围

- `docs/technical/ci-verification-runtime.md`；
- `.github/workflows/ci.yml`；
- `.github/workflows/review-environment.yml`；
- 必要的 fingerprint / baseline 脚本与 Frontend Runtime Dockerfile；
- 对应 Work lifecycle 与验证证据。

本单元不改 Product Requirement、Specification、CMS Domain、公开页面行为或 Production Deployment。

## 验收义务

1. 完整 CI 对 exact Head 仍执行正式 Backend、Public、Admin 与 Integrated Browser verification。
2. 完整 CI 能发布 Backend / Migration、Frontend Runtime、Review Baseline、Review Verification marker，并保留 provenance。
3. Frontend fingerprint 对真实前端 build/runtime input 变化敏感。
4. Review Baseline fingerprint 对 schema、Site Definition、canonical migration、Backend implementation 与 MySQL contract 变化敏感。
5. Review Baseline 恢复后数据库、`runtime-static`、`runtime-uploads` 与 canonical runtime 一致。
6. Human Review fixture 不进入缓存镜像。
7. 人工评审 cache-hit 路径不安装 Node、不构建前端、不执行 canonical migration、不重复完整 Playwright。
8. 人工评审仍执行必要的恢复、Runtime startup、fixture 注入、bounded smoke、FRP、外部地址与 lease 验证。
9. cache miss / Registry / provenance 异常失败关闭。
10. 文档治理、Workflow 验证、完整 CI 与真实人工评审环境启动验证均通过。
11. Public / Admin 依赖由受版本控制的 lockfile 固定，并通过 `npm ci` 复现。
12. 同一 Frontend Runtime / Review Baseline / Review Verification fingerprint 不覆盖既有 GHCR artifact；cache-hit 时保持 producer SHA 并显式复用。

## 完成条件

实现与 exact-Head 自动验证完成后，必须实际启动一次人工评审环境，证明 cache-hit 快速路径和外部 Runtime 正常；随后进入 Integration / Post-Integration closure。
