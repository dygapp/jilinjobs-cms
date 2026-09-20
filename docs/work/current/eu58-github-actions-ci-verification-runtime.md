---
id: execution-unit:eu58-github-actions-ci-verification-runtime
type: execution-unit
status: active
readiness: PASS
controlling_issue: 182
base_sha: 90d1a2a97b3dca6844103f022632f212a7701d37
---

# EU-58 GitHub Actions CI 快速验证运行时优化

## 目标

在不降低最终完整验证强度的前提下，缩短基于 GitHub Actions / GitHub-hosted Runner 的日常开发反馈路径，重点消除纯前端迭代中重复 Backend 构建与无差别完整 CI 的等待。

## 范围

本单元包括：

- 建立 GitHub Actions CI Verification Runtime 的长期 Technical Authority；
- 区分快速验证与完整验证的证据责任；
- 使用 Backend build-input fingerprint 作为 Backend Runtime Artifact identity；
- 使用 GHCR 保存通过完整 Backend verification 的可复用 Runtime image；
- 新增 PR 快速 CI，使用 Fresh MySQL + 预构建 Backend image + 受影响前端 build + bounded browser smoke；
- Backend image 缺失时安全回退到当前 Head 的 Backend build，不把基础设施错误误判为 cache miss；
- 将现有完整 CI 调整为 main push、显式 full-ci Gate 或手动触发时执行；
- 完整 CI 继续从目标 exact Head 的源码构建 Backend、Public、Admin 并执行完整浏览器回归；
- 为 npm 安装启用 lockfile 模式与 GitHub Actions cache。

明确不包括：

- Historical Migration 数据预构建或数据库快照；
- 本地 Codex CLI / Docker Compose 开发环境；
- self-hosted Runner；
- 持久共享 Backend / Database；
- 自动推导任意 source file 到精确 E2E case 的复杂依赖图。

## 就绪检查

PASS。

理由：

- 用户已经明确目标、适用范围和非目标，当前工作不改变 Product Requirement 或用户可见业务行为；
- 当前 main、docs/work/current/README.md 与 GitHub Open PR 状态已核对，无冲突 active Unit；
- 当前 Technical Authority 已有 verification-strategy.md 可承担通用证据原则，新文档只需要拥有 GitHub Actions 场景的实现 HOW；
- 方案属于工程验证基础设施优化，使用现有 GitHub Actions、Docker、MySQL、Playwright 与 GitHub Packages 能力，不需要新的产品或高成本不可逆架构决策；
- task-level Rule Discovery 已在 base exact SHA 上完成并命中当前适用规则。

## 验收与验证映射

| 验收义务 | 实现责任 | 最低验证责任 |
| --- | --- | --- |
| 纯前端 PR 不重复 Gradle Backend build | 快速 CI + GHCR Backend image | Fast CI 命中已发布 fingerprint image |
| Backend image identity 不随无关前端 commit 漂移 | fingerprint script | 相同 backend/** tree 得到相同 fingerprint |
| Registry miss 可安全回退 | 快速 CI | miss 分支执行 Backend build；权限 / 网络异常失败关闭 |
| 快速验证不冒充完整验证 | Technical Authority + Workflow naming / summary | 快速 CI 只声明 bounded feedback evidence |
| 完整验证保持 exact Head clean build | 完整 CI | Backend + Public + Admin + Full E2E PASS |
| Backend image 只在通过完整 Backend verification 后发布 | 完整 CI publish step | GHCR image tag = verified fingerprint，记录 source SHA |
| 文档 owner 与实现一致 | Technical docs / Work artifact | 文档治理 PASS |

## 完成条件

- docs/technical/ci-verification-runtime.md、verification-strategy.md 与 Technical README 的 ownership 一致；
- Backend fingerprint、Runtime image、快速 CI 和完整 CI Workflow 均已实现；
- PR 当前 Head 的文档治理与 Workflow 实际运行证据通过；
- 至少验证一次 Backend image miss/fallback 路径，并在可行时验证一次 GHCR hit 路径；
- 完整 CI 对当前 exact Head 通过后，达到 Ready to Integrate。
