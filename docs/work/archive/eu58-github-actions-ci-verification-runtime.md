---
id: execution-unit:eu58-github-actions-ci-verification-runtime
type: execution-unit
status: completed
readiness: PASS
controlling_issue: 182
base_sha: 90d1a2a97b3dca6844103f022632f212a7701d37
verified_head_sha: 22396173cca368731824d2a63d0e630392585874
integrated_sha: bbdf3069883a85850a112487dd4e8e22864b1210
completed_at: 2026-09-20
---

# EU-58 GitHub Actions CI 快速验证运行时优化

## 目标

在不降低最终完整验证强度的前提下，缩短基于 GitHub Actions / GitHub-hosted Runner 的日常开发反馈路径，重点消除纯前端迭代中重复 Backend 构建与无差别完整 CI 的等待。

## 范围

本单元完成：

- 建立 GitHub Actions CI 验证运行时的长期 Technical Authority；
- 区分快速验证与完整验证的证据责任；
- 使用 Backend build-input fingerprint 作为 Backend Runtime Artifact identity；
- 使用 GHCR 保存通过完整 Backend verification 的可复用 Runtime image；
- 新增 PR 快速 CI，使用 Fresh MySQL + 预构建 Backend image + 受影响前端 build + bounded browser smoke；
- Backend image 缺失时安全回退到当前 Head 的 Backend build，并保持权限 / 网络 / 未知 Registry 错误失败关闭；
- 将原 CI 收敛为完整验证 Gate，继续从 exact Head 源码构建 Backend、Public、Admin 并执行完整浏览器回归；
- 按现有依赖治理边界优化前端安装：Admin 使用既有 lockfile 的 `npm ci` 与 npm cache；Public 因当前没有 `package-lock.json` 继续使用 `npm install`，未为本次 CI 优化额外引入 lockfile。

明确未包含：

- Historical Migration 数据预构建或数据库快照；
- 本地 Codex CLI / Docker Compose 开发环境；
- self-hosted Runner；
- 持久共享 Backend / Database；
- 自动推导任意 source file 到精确 E2E case 的复杂依赖图。

## 关键实现结果

### Backend Artifact identity

Repository commit SHA 继续承担仓库 provenance；Backend Runtime Artifact identity 由 `scripts/backend-runtime-fingerprint.sh` 基于当前 commit 的 `backend/**` Git tree 计算。

当前共享 Runtime image 使用：

`ghcr.io/dygapp/jilinjobs-cms-backend:<backend-fingerprint>`

只有完整 Backend verification 通过后才发布共享 image；Fast CI 的 miss fallback 只构建本次 Run 的临时 image，不晋升为共享 verified artifact。

### 快速验证

`.github/workflows/fast-ci.yml` 面向 PR 高频反馈：

- 检出 PR exact Head；
- 识别 Public / Admin / Backend 受影响范围；
- 计算 Backend fingerprint；
- 检查 GHCR image；
- image miss 时执行当前 Head Backend test + bootJar 并构建临时 image；
- image hit 时跳过 Java、Gradle、Backend build 和临时 image build；
- 创建 Fresh MySQL；
- 构建受影响前端；
- 启动 Backend 与前端 Gateway；
- 执行 Public / Admin bounded browser smoke；
- 上传 browser evidence 或失败诊断。

Fast CI 的 PASS 只形成开发反馈证据，不替代完整 CI。

### 完整验证

`.github/workflows/ci.yml` 作为最终完整验证 Gate：

- `main` 非文档 push 自动执行；
- PR 通过 `full-ci` label 显式执行；
- 支持 `workflow_dispatch`；
- Backend 从 exact Head 源码执行完整 build / verification；
- Public / Admin 分别正式 build；
- Fresh MySQL + 当前 Head Backend + 双前端执行完整 Playwright；
- Backend verification 成功后发布 fingerprint image 到 GHCR。

## 验收与验证证据

完成态 PR Head：`22396173cca368731824d2a63d0e630392585874`。

| 验收范围 | 当前证据 | 结果 |
| --- | --- | --- |
| 文档治理 | [文档治理 Run 35488293193](https://github.com/dygapp/jilinjobs-cms/actions/runs/35488293193) | PASS |
| GHCR miss / fallback、临时 Backend image、双前端 build 与快速浏览器验证 | [快速 CI Run 35488293173 attempt 1](https://github.com/dygapp/jilinjobs-cms/actions/runs/35488293173) | PASS |
| PR exact Head 完整 Backend / Public / Admin / 集成浏览器验证与 GHCR 发布 | [完整 CI Run 35488439131](https://github.com/dygapp/jilinjobs-cms/actions/runs/35488439131) | PASS |
| GHCR hit 后跳过 Java / Gradle / Backend fallback build / 临时 image build | [快速 CI Run 35488293173 attempt 2](https://github.com/dygapp/jilinjobs-cms/actions/runs/35488293173) | PASS |
| 集成后 main 文档治理 | [文档治理 Run 35489040371](https://github.com/dygapp/jilinjobs-cms/actions/runs/35489040371) | PASS |
| 集成后 main Backend / Public / Admin / 完整集成浏览器验证 | [完整 CI Run 35489040392](https://github.com/dygapp/jilinjobs-cms/actions/runs/35489040392) | PASS |

首次 Fast CI 验证还暴露了 Public 当前没有 `package-lock.json`，因此 `npm ci` 不适用。该失败被分类为 CI implementation 与真实 Repository dependency contract 不一致；修复为继续使用 `npm install` 后，后续 Fast CI 与完整 CI 均通过。

GHCR hit 的直接证据中，以下 Fast CI steps 均为 `skipped`：

- `配置 Java 21`
- `配置 Gradle`
- `Backend 快速构建或 image miss fallback`
- `构建本次 Run 的临时 Backend image`

因此预构建 Backend Runtime 复用已经真实进入快速反馈路径，而不是仅存在于配置中。

## 集成与闭环

- Controlling Issue：[Issue #182](https://github.com/dygapp/jilinjobs-cms/issues/182)；
- 实现 PR：[PR #183](https://github.com/dygapp/jilinjobs-cms/pull/183)，已 squash 合并；
- 完成态 PR Head：`22396173cca368731824d2a63d0e630392585874`；
- 实际集成提交：`bbdf3069883a85850a112487dd4e8e22864b1210`；
- 集成后完整 CI 与文档治理均已通过；
- 本 closure 仅归档 Work lifecycle，不再改变 CI 产品实现或 Technical Authority。

本次未执行 Production Deployment / Release，也未扩展到本地持久开发 Runtime、self-hosted Runner 或 Historical Migration 数据优化。
