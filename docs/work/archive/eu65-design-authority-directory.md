---
id: execution-unit:eu65-design-authority-directory
type: execution-unit
status: completed
readiness: PASS
base_sha: 4006131eb1c90399f9f8494b6264a5793550f03e
branch: docs/eu65-design-authority-directory
started_at: 2026-09-24
completed_at: 2026-09-24
---

# EU-65 视觉设计权威目录重构

## 目标

将公开站视觉设计 Authority 从原实现目录位置迁移到文档 Authority 区域 `docs/design/public-site/DESIGN.md`，使 Authority 的物理位置与其语义责任一致，同时保持标准文件名 `DESIGN.md`。

## 范围

- 新建 `docs/design/` 文档区域及 Human Navigation；
- 将现有 Public Visual Design Authority 原样迁移到 `docs/design/public-site/DESIGN.md`；
- 更新 Current Documentation IA、Specification、Technical、Verification、文档治理脚本与 Workflow 中的 locator；
- 不修改 DESIGN 内容语义，不修改 Public runtime source；
- 不修改当前 Method / Skills；相关发现链问题仅向 `dygapp/agentic-dev` 提交 Consumer feedback。

## 就绪状态

**PASS**

用户已经明确限定本轮只完成目录重构，并授权把 Method / Skills 适配问题作为 upstream feedback 交由 `agentic-dev` 后续统一处理。当前 `main@4006131eb1c90399f9f8494b6264a5793550f03e` 与 `origin/main` 一致，工作树干净，Current Work 在本单元建立前为 `NONE`。

## 验收

1. Public Visual Design Authority 只在 `docs/design/public-site/DESIGN.md` 作为 Current locator 存在；
2. Current 非归档文档不存在旧实现目录位置的 Design Authority locator；
3. `docs/README.md` 能从 Fresh Context 定位 `docs/design/` 与 Public Design owner；
4. 文档治理脚本纳入 `docs/design`，Google DESIGN lint 使用新路径；
5. `docs/**` path filter 自然覆盖 Design Authority，不再为源码目录维护额外 workflow path；
6. 不修改 `frontend/public-site/src/**`；
7. Method / Skills 不在本单元修改；
8. 对 Method / Skills 的 Design Authority integration 缺口形成 upstream feedback evidence。

## 实施结果

- Public Visual Design Authority 已从原实现目录位置迁移为 `docs/design/public-site/DESIGN.md`，文件内容语义未改变；
- 新增 `docs/design/README.md`，定义 Design Authority 区域的人类导航和与 Requirement / Specification / Architecture / Technical / implementation 的职责边界；
- `docs/README.md` 已把 `docs/design/` 注册为一级 Documentation Authority 区域，并把 Public Visual Design locator 指向新路径；
- `public-site.md`、Public Technical、Verification Strategy 已同步新 locator；
- 文档治理脚本已从单个源码目录文件切换为扫描 `docs/design`；
- 文档治理 Workflow 的 Google DESIGN lint 已使用新路径；由于 `docs/**` 已覆盖该文件，不再保留额外的源码目录 path filter；
- 未修改 `frontend/public-site/src/**`，未修改 Method / Skills。

## 上游反馈

已复用 `dygapp/agentic-dev` 的长期 Consumer feedback Issue #58，没有创建重复 Issue。

反馈 comment：

- comment id：`5805952124`
- 目标：`dygapp/agentic-dev#58`
- 内容：Design Authority 成为独立规范层后，现有 Method / Skills 的 Authority vocabulary / conditional discovery / owner-return contract 需要在当前发布模型重构中统一评估；
- 明确不要求 Consumer 本轮修改 Method / Skills，也不预设新增 Design Skill / Rule；
- 关联当前 upstream Issue #172 的 Skills 发布模型重构作为后续评估上下文。

评论写入后已通过 GitHub API 按 comment id 重新读取确认。

## 当前验证

- Current 非归档范围旧 Design locator：0；
- `npx --yes @google/design.md@0.4.0 lint docs/design/public-site/DESIGN.md`：0 errors / 1 已知 contrast warning；
- `node scripts/verify-docs-governance.mjs`：PASS；
- Workflow YAML 解析：PASS；
- `git diff --check`：PASS；
- Public runtime source guard：PASS。


## 集成与 Post-Integration 证据

- PR #205 — `docs(design): 迁移视觉设计权威到文档目录` 最终候选 Head：`9a3bd59b8d320e3b6cf1811ee0315b82bbbdafe7`；
- PR exact-head 快速 CI Run `35944293680`：`completed/success`；
- PR exact-head 文档治理检查 Run `35944293682`：`completed/success`；
- PR #205 已合并，Merge Commit：`22adc20bbe0447558a0bd92a15c1f1b1e42bae11`；
- GitHub 重新读取确认 PR #205 为 `closed + merged=true`，且 `origin/main` 指向上述 Merge Commit；
- Merge Commit 对应 main push 文档治理检查 Run `35944489604`：`completed/success`；
- Merge Commit 对应 main push 完整 CI Run `35944489596`：run 级 `completed successfully`；Backend verify、Public site frontend verify、Admin frontend verify、Integrated browser verification、Publish verified review runtime 均 success；
- GitHub REST 公共匿名 rate limit 在 Post-Integration 后段被耗尽，因此完整 CI 最终状态通过同一 GitHub Actions 公共 Run 页面重新读取，而不是把 API rate-limit 错误当作 CI 失败。
