---
id: technical:ci-verification-runtime
type: technical-strategy
status: active
relations:
  verification:
    - docs/technical/verification-strategy.md
updated_at: 2026-09-20
---

# GitHub Actions CI 验证运行时

## 1. 文档责任与适用范围

本文只定义 jilinjobs-cms 在 GitHub Actions，尤其是 GitHub-hosted Runner 场景中的 CI 验证运行时实现方式。它解决临时 Runner、跨 Workflow Run 状态不持久、前端真实页面验证依赖 Backend / Database，以及重复 Backend build 带来的反馈成本。

本文不是通用 CI Architecture，也不规定其他 Runtime 必须采用相同实现。以下场景不由本文直接约束：

- 本地 Codex CLI 的快速开发与自动化验证；
- 开发人员本地长期运行的 MySQL、Redis、MinIO、Backend 或 Frontend；
- self-hosted Runner 上可持续复用的 Runtime；
- Persistent Integration Environment；
- 本地 Docker Compose / Dev Container；
- 未来 jilinjobs 主项目的 Local Development Runtime；
- Historical Migration 数据预构建、数据库快照或迁移 Runtime 优化。

当未来验证运行于具有持久环境和低成本本地反馈能力的 Runtime 时，应依据该 Runtime 的真实约束重新设计快速反馈路径，不机械复用本文的 GHCR / prebuilt Backend 模型。

## 2. 与通用验证策略的关系

docs/technical/verification-strategy.md 持有跨 Runtime 的验证分层、证据 claim 与最终完成语义；本文只负责这些原则在 GitHub Actions 中如何实现。

稳定关系：

~~~text
verification-strategy.md
        ↓ 通用验证语义
ci-verification-runtime.md
        ↓ GitHub Actions 实现 HOW
.github/workflows/** / scripts/**
        ↓ 可执行实现
GitHub Actions / GHCR / Artifact Evidence
~~~

Workflow 不反向拥有长期验证语义。Workflow 与本文冲突时，应先判断实现是否 stale，再由真实 Technical owner 修正。

## 3. 核心目标

当前优化不以“把 Full CI 本身压缩到极短”为主要目标，而是让昂贵的完整验证退出每次代码修改的关键反馈路径。

GitHub Actions 下采用两层验证：

~~~text
PR 开发迭代
→ 快速 CI
→ 快速反馈 / 修复循环
→ Ready to Integrate
→ 完整 CI
→ exact Head 完整自动化证据
~~~

快速 CI 与完整 CI 的证据责任不同，前者不能替代后者。

## 4. Backend Artifact identity 与 Repository provenance

Monorepo 的 Repository commit SHA 表示整个仓库状态，前端变化也会改变 SHA，因此不得把 github.sha 直接作为 Backend 是否需要重建的唯一判断。

Backend Runtime Artifact 使用独立的 build-input fingerprint：

~~~text
Repository SHA
→ 本次 CI / PR 的仓库 provenance

Backend fingerprint
→ Backend Runtime Artifact identity
~~~

当前 Backend executable 与 Runtime image 的构建输入位于 backend/**，因此 fingerprint 由当前 commit 的 Backend Git tree identity 计算。实现入口统一由 scripts/backend-runtime-fingerprint.sh 持有，不在多个 Workflow 中复制算法。

如果未来 Backend 构建开始依赖 backend/** 之外的输入，必须先更新该脚本和本文的 build-input contract，再依赖新的 fingerprint。

Site Package、Frontend 和 Historical Migration 不因为与 Backend 同仓库就自动进入 Backend image fingerprint；它们保持各自 ownership 与 Runtime composition。

## 5. GHCR 后端运行时产物

Backend Runtime image 使用 GitHub Container Registry 保存，不把 JAR、image tar 或其他生成二进制提交到 Git。

逻辑 identity：

~~~text
ghcr.io/dygapp/jilinjobs-cms-backend:<backend-fingerprint>
~~~

镜像必须保留至少以下 provenance：

- Backend fingerprint；
- 构建时实际 source commit SHA；
- OCI source repository label；
- Registry image digest（由 Registry / Workflow evidence 提供）。

只有完成当前完整 Backend verification 的构建才允许发布为可跨 Run 复用的 Backend image。快速 CI 在 image miss 时可以为当前 Run 临时构建 Backend 以继续验证，但不得把未完成完整 Backend verification 的临时产物晋升为共享 verified image。

## 6. 快速 CI

快速 CI 面向 PR 开发反馈，目标是证明当前受影响前端与一个明确 provenance 的 Backend Runtime 在 Fresh Database 上保持基本集成可用，并尽快发现 build / route / runtime 级回归。

典型路径：

~~~text
检出 PR 精确 Head
→ 识别受影响前端范围
→ 计算 Backend fingerprint
→ 登录 GHCR
→ 检查镜像
   ├─ HIT  → 拉取已验证镜像
   ├─ MISS → 为本次 Run 构建当前 Head Backend
   └─ AUTH / NETWORK / UNKNOWN → FAIL
→ 创建 Fresh MySQL
→ 启动 Backend
→ 构建受影响前端
→ 启动有界前端 Gateway
→ 执行受影响应用的固定快速 smoke
→ 上传浏览器证据 / 失败诊断
~~~

快速 CI 的约束：

- MySQL 每个 Job 独立创建，不共享历史测试数据；
- GHCR HIT 只避免 Backend build，不省略 Backend Runtime；
- Registry 权限、网络或未知错误不得降级为“镜像不存在”；
- Public / Admin 只构建实际受影响应用；未受影响应用可以使用空静态目录，只要本轮 smoke 不依赖它；
- 默认 browser smoke 是稳定、低成本的集成探针，不宣称覆盖 Feature 全部 Acceptance；
- 快速 CI 不尝试从任意 source diff 自动推导精确 E2E case；
- Feature-specific targeted E2E 与其他 Acceptance 验证仍由当前 Execution Unit 的 verification obligations 选择，不能因为 Fast CI PASS 自动省略；
- Fast CI PASS 只能形成 bounded development-feedback evidence。

## 7. 完整 CI

完整 CI 是最终自动化收敛证据，必须从目标 exact Head 的 Repository source 重新建立验证链，不以预构建 Backend image 替代 Backend build。

完整路径继续覆盖：

- Backend clean test / bootJar；
- Site Package foundation / composition / bootstrap / asset verification；
- Public formal build；
- Admin formal build；
- Fresh MySQL；
- 当前 Head Backend Runtime；
- 双前端 gateway；
- Public 完整 Playwright；
- Admin 完整 Playwright；
- 必要 Artifact / diagnostics。

完整 CI 通过后，可以把本次已经完成完整 Backend verification 的 Backend Runtime image 发布到 GHCR，供后续快速 CI 使用。

完整 CI 的 GitHub Actions 触发边界为：

- main 的非文档 push：集成后完整验证并刷新可复用 Backend image；
- PR 显式进入 full-ci Gate：对当前 PR exact Head 执行完整验证；
- workflow_dispatch：用于明确指定 ref 的人工 / Agent 验证。

普通 PR synchronize 不再自动重复完整 CI。

## 8. 变化范围与快速验证

快速 CI 只在 Public / Admin 相关变更需要开发反馈时承担 Runtime browser path。

当前主要范围：

- frontend/public-site/** → Public build + Public fast browser；
- frontend/admin/** → Admin build + Admin fast browser；
- frontend/nginx.e2e.conf → 双前端 fast browser；
- Backend 变化导致 fingerprint MISS 时，本轮临时构建 Backend；最终仍由完整 CI 完成正式 Backend verification。

复杂跨层变化、Site Package、Migration、Workflow 核心组合变化不因为 Fast CI 存在就降低其完整验证义务。

## 9. Registry lookup 与失败关闭

Registry lookup 必须明确区分：

~~~text
FOUND
NOT_FOUND
AUTH_ERROR
NETWORK_ERROR
UNKNOWN_ERROR
~~~

只有明确的 NOT_FOUND 允许进入 build fallback。登录失败、权限失败、网络错误或无法分类的 Registry 错误必须使当前快速验证失败，避免把基础设施问题伪装成正常 cache miss。

## 10. Workflow 编排

同一验证链内部优先使用 GitHub Actions Job dependency 或 reusable workflow，而不是依赖不同 Runner 之间的隐式网络状态。

不同 GitHub-hosted Job / Run：

- 不共享 localhost；
- 不共享普通文件系统；
- 不假定容器或进程持续存在；
- 需要跨 Run 复用时通过 GHCR、Actions Artifact 等持久介质传递。

当前方案不通过长期共享 Backend service 连接不同 Runner。

## 11. 构建缓存与生成物

存储职责保持分离：

- Git Repository：源码、Dockerfile、Workflow、脚本与 Authority；
- GHCR：可运行 Backend Runtime image；
- Actions Artifact：测试报告、诊断和短期构建证据；
- Actions Cache：npm / Gradle 等可丢失的依赖缓存；
- Release Asset：正式发布物（需要时）。

Cache 不承担 artifact authority，Git 不存储构建二进制。

## 12. 可复用原则

本文的实现仅限 GitHub Actions，但以下工程原则可在其他 Runtime 中重新投影：

1. Repository SHA 与 Component Artifact Version 分责；
2. Artifact identity 基于真正 build inputs，Repository SHA 保留 provenance；
3. 快速反馈与最终完成验证分层；
4. 可复用 immutable artifact，但测试状态应隔离；
5. Fast Verification 可以复用已验证 artifact，Final Verification 仍验证目标 exact Head；
6. CI dependency 使用显式编排，不依赖隐式 Runner 状态；
7. 昂贵验证不应位于每次代码修改的关键反馈路径。

未来本地持久 Runtime 可以采用不同实现，不应把 GHCR lookup、Fresh MySQL 或临时 Backend container 当成这些原则的必然形式。
