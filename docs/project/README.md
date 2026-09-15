# Project Knowledge

`docs/project/` 只保存 **当前 `jilinjobs-cms` Repository 自身的长期 Project Knowledge**，不作为“重要文档”通用收容区。

## Current Project owners

- Project Charter equivalent：根 `README.md`；
- Project Capability Profile：`project-capability-profile.md`；
- Project Roadmap：`project-roadmap.md`；
- Project Evolution：`project-evolution.md`。

这些文件分别拥有项目使命 / 范围摘要、当前 capability instance、未来路线与稳定历史摘要。它们不得复制 Method、Architecture、Rule、Skill、Feature Specification、Technical Plan、Current Work 或 GitHub native state 的规范正文。

## 不属于 Project 的内容

以下内容应进入各自真实 owner：

- reusable / Consumer-local complex workflow → `docs/methods/`；
- long-lived structure / ownership / runtime invariant → `docs/architecture/`；
- conditional policy / constraint / completion requirement → `docs/rules/`；
- bounded executable procedure → `skills/`；
- Product / Domain facts → `docs/requirements/` 或后续明确的 Domain owner；
- Feature WHAT / WHY → `docs/specifications/`；
- Feature coordinated HOW / Verification Strategy → `docs/technical/`；
- active execution lifecycle → `docs/work/`；
- process evidence / upgrade history / completed planning → GitHub Evidence 或相应 `archive/`。

不能因为内容重要、曾经放在 Project、包含“治理 / 规划 / 方法”等词，就继续留在 Project root。

## Archive

`archive/` 只保留已完成或被取代、仍有 provenance / audit 价值的 Project-level planning / governance / baseline upgrade / validation history。它默认不参与 ordinary Fresh Context。

## Issue #153 过渡路径

Foundation Rebuild 期间，若干旧 `docs/project/*.md` 已经降级为 transitional locator，只用于兼容尚未迁移的引用。它们不属于最终 Project Knowledge，也不得重新取得规范正文；在 locator migration 完成后必须删除。

目录边界的 canonical contract 由 `docs/architecture/project-knowledge.md` 持有，本 README 只提供 Human navigation。