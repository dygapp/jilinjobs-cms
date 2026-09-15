# Project Knowledge

`docs/project/` 只保存当前 `jilinjobs-cms` Repository 自身的长期 Project Knowledge，不作为“重要文档”通用收容区。

## Current Project owners

- Project Charter equivalent：根 `README.md`；
- Project Capability Profile：`project-capability-profile.md`；
- Project Roadmap：`project-roadmap.md`；
- Project Evolution：`project-evolution.md`。

它们分别拥有项目使命 / 范围摘要、当前 capability instance、持久路线与稳定历史摘要，不复制 Method、Architecture、Rule、Skill、Feature Specification、Technical、Current Work 或 GitHub native state 的规范正文。

## Semantic ownership

- reusable / Consumer-local complex workflow → `docs/methods/`；
- long-lived structure / ownership / runtime invariant → `docs/architecture/`；
- conditional policy / constraint / completion requirement → `docs/rules/`；
- bounded executable procedure → `skills/`；
- Product / Domain facts → `docs/requirements/`；
- Feature observable contract → `docs/specifications/`；
- cross-Feature implementation HOW / Verification Strategy → `docs/technical/`；
- active execution lifecycle → `docs/work/`；
- process evidence / upgrade history / completed planning → GitHub Evidence 或相应 `archive/`。

不能因为内容重要、曾经放在 Project、包含“治理 / 规划 / 方法”等词，就继续留在 Project root。

## Archive

`archive/` 保存已完成或被取代、仍有 provenance / audit 价值的 Project-level planning、governance、baseline upgrade 与 validation history，默认不参与 ordinary Fresh Context。

目录边界的 canonical contract 由 `docs/architecture/project-knowledge.md` 持有；本 README 只提供 Human navigation。
