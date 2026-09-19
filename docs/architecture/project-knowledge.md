---
id: architecture:project-knowledge
type: architecture
status: active
---

# Project Knowledge 架构

## 目标

Project Knowledge 只保存当前 `jilinjobs-cms` 自身值得跨 Fresh Context 长期恢复的项目事实与稳定摘要，不承载 reusable capability 正文。

## 当前 Project owners

本 Consumer 使用以下职责分离：

- 根 `README.md`：Project Charter equivalent + stable human entry，回答项目是谁、当前长期范围与非扩张边界；
- `docs/project/project-capability-profile.md`：当前 Repository 如何实例化已采用 capability；
- `docs/project/project-roadmap.md`：当前正式 baseline / evolution gate、下一候选与持续观察事项；
- `docs/project/project-evolution.md`：理解今天设计仍有价值的稳定里程碑和主要转折。

文件名不是通用强制标准，但以上 semantic responsibilities 不得重新混合。

## 不属于 Project Knowledge 的内容

- Method stage / Gate / completion 正文；
- Rule policy / activation metadata；
- Skill Procedure；
- reusable Architecture contract；
- Feature Requirement / Specification / Technical Plan；
- 一次性 planning / audit / validation / closure 报告；
- 可从 GitHub 直接恢复的 Open PR / Issue / Actions 流水账；
- 临时 source inventory、comparison matrix、review scratchpad。

这些内容必须回到真实 语义所有者，或只保留在 Git / Issue / PR / Actions 中。

## Capability Profile 边界

Project Capability Profile 只保存少量 Repository-local instance pointers，例如 工作类型 → Method locator、Rule root / Discovery Tool、Skill root、Human / Agent entry 与 evaluated upstream baseline。它不得复制完整 Rule / Skill inventory、Method stages、当前 candidate set 或 Issue / PR 状态。

## Roadmap 与 Evolution

Roadmap 只回答当前正式基线、当前演进 Gate、下一候选；Evolution 只保存对理解今天设计仍有价值的主要里程碑。每个 EU、commit、workflow run、实验流水账不进入长期 Project Knowledge。

当可变 Repository state 与长期 Project summary 冲突时，应读取真实 当前证据 并修正 stale summary；Product / Method / Architecture 语义仍回到各自 规范语义所有者。