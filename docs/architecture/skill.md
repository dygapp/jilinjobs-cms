---
id: architecture:skill
type: architecture
status: active
---

# Skill 架构

## Skill 定义

Skill 是当前责任已经明确后，可独立调用的稳定、有界执行能力：

```text
Trigger / Purpose
→ Inputs
→ Procedure
→ Outputs
→ Exit Conditions
→ Escalation
```

Skill 不拥有跨多个 Method stage 的项目生命周期，也不因自身存在取得 Repository 写入、merge、release、deploy 或其他外部副作用授权。

## 与 Method / Rule 的边界

Method 拥有复杂工作的长期生命周期；Skill 拥有其中某个明确责任下可重复执行的 Procedure。Method stage 不要求一一对应 Skill，一个 Skill 也可以在多个 Method 中复用。

Rule 与 Skill 正交。容易因 Consumer / Repository Authority 改变的 policy、格式、审批、授权和完成声明要求优先由 Rule / Repository Authority 持有，不写死进 Skill。

## 清单所有权

Architecture 不持有当前 Consumer 的完整 Skill inventory 或数量。实际 `skills/*/SKILL.md` corpus 是 Skill 资源；当前 Skill root / discovery entry 由 Project Capability Profile 声明。

新增 Skill 至少要证明独立 trigger、稳定 inputs、可重复 procedure、稳定 outputs、明确 exit / escalation、单一 semantic owner 与真实重复使用价值。

## Agent Skills 互操作

Consumer-local `SKILL.md` 使用 Agent Skills 原生 `name` / `description`，并通过 metadata string map 保存本仓库逻辑身份：

```yaml
metadata:
  jilinjobs-cms-id: skill:example
  jilinjobs-cms-type: skill
  jilinjobs-cms-status: active
```

不得为了同步 upstream inventory 机械新增 Skill；显式 adoption / upgrade / governance 必须逐项裁决。