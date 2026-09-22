---
id: execution-unit:eu63-review-runtime-cache
type: execution-unit
status: completed
readiness: PASS
controlling_issue: 200
base_sha: 87f5dcee2dff898ad5a35b4e523d022d6f4705db
integrated_sha: c6a23a87960ddbd73030fac8a0b8b43f639ae1e2
---

# EU-63 人工评审运行时缓存重构

状态：`COMPLETED`。

## 目标与范围

把完整 CI 已验证的前端构建、人工评审数据基线与对应浏览器验证结果物化为可跨 GitHub-hosted Runner 复用的 immutable artifact，缩短人工评审环境启动时间，并消除人工评审固定槽位旧租约导致的长时间排队。

本执行单元只调整 GitHub Actions CI / Review Runtime 的 verified artifact、fingerprint、数据基线恢复与证据复用，不改变 Product Requirement、Specification、CMS Domain、Site Definition、Historical Migration Authority、公开页面行为或 Production Deployment。

## 最终实现

- Frontend Runtime fingerprint 覆盖 Public / Admin 受控源码与 lockfile、`frontend/nginx.e2e.conf`、Runtime Dockerfile 和 fingerprint contract；Public / Admin 使用 `npm ci`。
- 完整 CI 发布并复用 `ghcr.io/dygapp/jilinjobs-cms-frontend-runtime:<frontend-fingerprint>`。
- Review Baseline fingerprint 覆盖 Backend Runtime fingerprint、MySQL contract、schema、`sites/jilinjobs/**`、`data-migrations/**` 与 baseline preparation contract。
- Review Baseline 使用 logical DB dump + `runtime-static` + `runtime-uploads`，不缓存 MySQL raw datadir；Human Review fixture 不进入 baseline。
- 主站评审数据从已接受的 `data-migrations/main/v1` 派生，每个 article surface 按 `publishDate DESC, sourceOrder ASC, legacyKey ASC` 选取最新 30 条，再通过现有 Generic Content Migration 导入；完整 canonical dataset 不被裁剪或改写。
- Review Verification fingerprint 覆盖 Backend / Frontend / Baseline identity 与会改变浏览器验证 claim 的测试和运行契约；matching marker 命中后不重复完整 Playwright。
- 相同 Frontend Runtime / Review Baseline / Review Verification fingerprint 不覆盖已有 GHCR artifact，保留真实 producer SHA。
- 人工评审 cache-hit 路径不安装 Node、不重新构建前端、不重新执行 canonical migration、不重复完整 Playwright；只恢复 baseline、启动 Runtime、注入 Human Review fixture、执行 bounded smoke / FRP / 外部地址与 owner / lease 验证。
- 人工评审固定共享槽位改为 latest-wins，新有效 `human-review` / `workflow_dispatch` Run 不等待旧 45 分钟租约自然结束。
- 快速 CI 对 CI 编排 / Review Runtime-only 变更不再无条件安装 Java / Gradle、构建 Backend 或拉取无关 Playwright Runtime；Backend Runtime container contract 变化仍保留必要的快速集成覆盖。

## 实现态验证证据

PR #201 完成态 Head：`f3b1678252b90e1d344267336900e570eb195539`。

- 文档治理 #254：`PASS`。
- 快速 CI #64：`PASS`；约 1 分 49 秒，Java / Gradle / Backend build 均按影响范围跳过。
- 完整 CI producer：`986ab1a25d0fa8af10cf243148f095b5558e1946`，完整 CI #1249：`PASS`。
- `986ab1a… -> f3b1678…` 精确差异只涉及 `.github/workflows/fast-ci.yml`、`docs/technical/ci-verification-runtime.md`、`docs/work/current/eu63-review-runtime-cache.md`；Backend / Frontend / Review Baseline / Review Verification 的 build / runtime / verification inputs 未改变。
- 当前 Review Runtime verification fingerprint：`5baeb8b2f5960958db6919519e3adf720230b7aa72b7f3cda2828d0da9e6ee7d`；producer SHA 保留为 `986ab1a25d0fa8af10cf243148f095b5558e1946`。
- 人工评审环境 #913 已完成 Runtime 解析、Baseline 恢复、Runtime 启动、canonical 验证、Human Review fixture、FRP、外部地址、owner / lease 与证据上传；其 `review-external-runtime-evidence` 绑定 exact Head `f3b1678…`。
- 主站派生评审数据已在 exact-head Runtime 中验证：各 article surface 运行时数量与派生 manifest 一致，`notice` / `employment-news` / `recruitment-announcement` 等栏目各恢复最新 30 条后再独立注入 Human Review fixture。
- 收敛结论：Blocking `0`、Medium `0`、Unverified completion obligation `0`，PR #201 `READY TO INTEGRATE`。

## 集成与闭环证据

- 实现 PR：#201，已按人工明确授权 squash 合并；
- 实际集成提交：`c6a23a87960ddbd73030fac8a0b8b43f639ae1e2`；
- 集成后 `main` 文档治理：Run `35689034862` / #255，`PASS`；
- 集成后 `main` 完整 CI：Run `35689034684` / #1251，`PASS`；
- 集成后 Backend、Public、Admin、Integrated browser verification：全部 `PASS`；
- 集成后 `Publish verified review runtime`：`PASS`，Frontend Runtime、Review Baseline 与 Review Verification 均命中 verified cache，未重复构建 Frontend、生成 Baseline 或执行 Review Runtime probe；
- 集成后 task-level Rule Discovery：Run `35689316479`，requested SHA = actual SHA = `c6a23a87960ddbd73030fac8a0b8b43f639ae1e2`，`status=ok`。

本归档只关闭 Work lifecycle，不改变已经集成的产品、运行时或验证语义。本次未执行 Production Deployment / Release。
