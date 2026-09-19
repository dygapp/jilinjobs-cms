---
id: rule:execution-continuity
type: rule
status: active
scope:
  phases: [execute, converge]
  activities: [implementation, verification, review, external-operation]
  technologies: []
  artifacts: []
  risks: []
---

# 执行连续性

当当前工作已经获得足够 Authority 且仍处于同一授权 Scope 时，内部 Slice、单次 Commit、可自行修复的失败、普通检查点或一次成功验证都不是默认人工停顿边界。

## 默认持续推进

允许为了控制复杂度、上下文和验证范围使用 Slice、JIT Plan、检查点和局部修复步骤，但这些内部单元默认不是新的人工任务。

只要同时满足：

- 当前 Authority 足够；
- 工作仍在已授权 Scope；
- 剩余选择属于低影响、可逆的常规工程判断；
- 所需权限和输入存在；
- 当前 Method / 验收 仍要求继续；

就应连续分析、实施、验证、修复和收敛，而不是因为一个内部步骤完成就请求人工发送“继续”。

## 真正停止 / 汇报边界

完整收口或人工决策默认只在以下情况发生：

1. 当前授权工作达到 Method / Review Batch 的自然完成或 人工评审 / Integration Gate；
2. 需要改变 Product Goal、Scope、User-visible Behavior、Business Boundary、重大 Architecture、Security / Privacy 等高影响事实；
3. 缺失必须由人工提供的权限、凭据、外部事实或不可替代输入；
4. 出现超出当前 Scope、难逆或共享状态高影响操作；
5. 用户明确要求阶段总结或停在当前检查点。

普通测试失败、可自行诊断的 Workflow 失败、局部修复、单个 Review Finding、一次重试或一个内部 Slice 完成本身不构成停止条件。

## 沟通负担

长任务可以提供必要的简短进度更新，但不把文件读取、每个 Commit、每次测试或每个内部 Slice重述为完整阶段报告。最终汇报聚焦结果、验收 / 证据、剩余风险、人工 Gate 和下一主要动作。

## 不覆盖的边界

连续执行不降低验证要求，不扩大 Scope，也不授予 merge / release / deploy 权限。当前 Repository / 人工权威、适用 Method、Rules 与 验收 始终优先。