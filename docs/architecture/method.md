---
id: architecture:method
type: architecture
status: active
---

# Method 架构

## Method 是一等过程能力

Method 针对一类复杂工作定义规范生命周期。一个 Method 至少拥有：

- work kind 与进入条件；
- 阶段 / 状态和当前责任；
- 必要长期产物与 Gate；
- 转换、返回 / 回退条件；
- 整体完成条件；
- 与 Architecture、Skill、Rule 的组合边界。

Method stage 表示工作状态与责任，不要求每个阶段都创建同名 Markdown，也不要求阶段与 Skill 一一对应。

## Repository-local selection

Agent 必须能够从本地 Authority 选择 Method：

```text
current repository facts + work kind
→ Project Capability Profile 的 local selector
→ canonical Method id / locator
→ read selected Method body
```

selector 只保存稳定 `work kind → Method id / locator`，不得复制 Method stages、Gate、Skill / Rule routing、Issue / PR state。

如果没有 Method 匹配，不得为了获得流程强行套用最接近的 Method；按 Repository Authority 与 direct responsibility 工作。

## Phase identity

Rule Discovery 需要 phase token 时，只能使用当前 selected Method canonical owner 明确定义的 stable phase identity。无法无歧义确定时使用 `null`，不得从目录、自然语言阶段名、其他 Method 或 Rule metadata 猜测。

## Method 与 Repository policy

Method 不拥有 merge、release、deploy 或其他外部副作用授权。这些权限始终由 Consumer Repository / Human Authority 决定。局部、易随仓库变化的 policy 应进入 Rule 或 Repository Authority，不写进 reusable Method 正文。