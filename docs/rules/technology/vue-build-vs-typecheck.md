---
id: rule:vue-build-vs-typecheck
type: rule
status: active
scope:
  phases: [execute, converge]
  activities: [implementation, verification]
  technologies: [vue3, typescript]
  artifacts: [code]
  risks: []
---

# Vue 构建不能替代类型检查

Vite build、bundle success 与 TypeScript / Vue template type checking 是不同证据。涉及 Vue 3 + TypeScript 变更时，应使用本 Consumer 当前 package scripts 与 tsconfig 对应的真实 typecheck 命令；build 通过不能单独证明类型正确。

不得为了匹配通用 profile 发明仓库中不存在的脚本或机械升级依赖；验证命令以当前 Consumer package 配置为准。
