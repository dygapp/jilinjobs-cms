---
id: architecture:engineering-capability
type: architecture
status: active
---

# Consumer-local 工程能力架构

## 目标

本 Architecture 定义 `jilinjobs-cms` 对可复用工程能力的长期分类、语义所有权与组合关系。核心原则是：

> **单一语义所有权，多视窗表达。**

规范语义只由真实 规范语义所有者 持有；README、Guide、Issue、Roadmap 与导航可以解释或定位，但不得成为第二套规范正文。

## 能力类型

- **Method**：拥有一类复杂工作的进入条件、阶段 / 状态、责任转换、Gate、返回与完成语义。
- **Architecture**：拥有长期结构、capability boundary、语义所有者ship、组合关系与 runtime invariant。
- **Skill**：在责任已明确后提供稳定、有界、可独立调用的执行 Procedure。
- **Rule**：按当前 task facts 条件性施加 policy、constraint、default、invariant 或 completion requirement。
- **Guide**：Human-facing 解释、示例与导航，不拥有 Agent runtime 的规范语义。
- **Research / Evidence**：保留非规范性证据、比较和实验；只有结论晋升到真实 owner 后才成为长期 Authority。

Project Knowledge 不是 reusable capability；它只描述当前 Consumer 自身使命、capability instance、路线与稳定演进摘要。

## Agent 运行视图

```text
Repository Authority / Bootstrap
→ Project Capability Profile + current repository facts
→ Method selection（若当前 work kind 命中）
→ current Method stage / direct responsibility
    ├─→ relevant Architecture
    ├─→ Skill discovery / invocation（如需要）
    └─→ Rule Discovery → applicable Rule bodies
→ execute / verify / return
```

Method、Architecture、Skill、Rule 彼此按责任组合，不形成固定的 `Method → Skill → Rule` 串行流水线。

## Consumer 本地特化

`agentic-dev` reusable capability 只有经过显式 adopt / adapt / replace / reject 并落到本仓库 规范语义所有者 后，才属于 Consumer current capability。Consumer-local Rule、版本、路径、工具与 Repository policy 可以与 upstream 不同；不得为了目录或数量一致机械镜像 upstream。

## 长期事实分类

新增或修改长期内容时，先判断真实 owner：

- 复杂工作生命周期变化 → Method；
- 长期结构 / ownership / runtime invariant → Architecture；
- 独立稳定执行 Procedure → Skill；
- 条件性规范约束 → Rule；
- 当前 Repository 的使命 / capability instance / Roadmap / 演进摘要 → Project Knowledge；
- 仅面向人的解释 → Guide；
- 仅过程或探索证据 → Git / Issue / PR / Actions / Research。

不得因为内容“重要”、已有历史文件或目录习惯而保留重复 owner。