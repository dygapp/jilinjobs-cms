---
id: architecture:consumer
type: architecture
status: active
---

# Consumer 架构

## Consumer 所有权

`jilinjobs-cms` 始终拥有自己的项目事实、Project Knowledge、Requirement / Domain、Method、Architecture、Skill、Rule、代码、验证与集成策略。`agentic-dev` 只提供可显式采用的 reusable capability。

核心边界：

> **Project 不传播，Capability 传播。**

upstream Project Charter、Capability Profile、Roadmap、Evolution、Issue / PR、Research / Eval 只能作为 provenance / comparison context，不自动成为 Consumer Authority。

## 普通运行

显式 adoption / upgrade 完成后：

```text
Consumer Repository facts
→ Consumer-local Project Knowledge / capability instance
→ local Method selection / direct responsibility
→ local Architecture / Skill / Rule Discovery
→ execute / verify / return
```

普通运行时 默认：

```text
upstream access = 0
```

本地 owner、Method selector、Skill entry、Rule Discovery 或 metadata 缺失 / 冲突时先 Consumer-local 失败关闭，不在线读取 upstream 补流程或规则。

## 显式 upstream re-entry

只有当前 Repository / 人工权威 明确启动 adoption、upgrade、research / comparison、Consumer validation，或显式授权的 Foundation governance work，才允许只读进入固定 upstream ref。允许读取不等于自动采用；所有接受结果必须重新落到 Consumer-local 规范语义所有者。

## 本地 规范语义所有者

Consumer 可以采用不同路径、工具、Method adaptation 与 Rule specialization，只要：

- 项目事实仍由本地 Authority 持有；
- capability 语义所有者 单一且可发现；
- Rule metadata 与正文同源；
- 不建立需要与 Rule 正文人工同步的中心路由表；
- upstream provenance 与 local Authority 不混为一谈。

## 能力投影

显式 adoption / upgrade 可传播的对象包括 Method、Architecture、Skill、Rule 与必要 Tool / runtime contract。每项能力必须有明确 disposition；upstream Project Knowledge 不属于传播对象。

治理或升级结束后必须验证普通 Fresh Context 能仅依赖 Consumer-local assets 工作。