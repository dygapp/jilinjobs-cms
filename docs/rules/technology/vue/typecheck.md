---
id: rule:vue-typecheck
type: rule
status: active
scope:
  phases: [execute, converge]
  activities: [verification]
  technologies: [vue3, typescript]
  artifacts: [vue-sfc, code]
  risks: []
---

# Vue Build 不等于类型检查

Vite build、bundle success 与 TypeScript / Vue template type checking 是不同证据。涉及 Vue 3 + TypeScript 变更且需要类型证据时，必须使用当前 Consumer package scripts / tsconfig 对应的 Vue-aware typecheck；当前前端 `npm run build` 已实际包含 `vue-tsc --noEmit`，不得把纯 bundler success 单独描述为类型正确证据。

不得为了匹配通用 profile 发明仓库中不存在的脚本或机械升级依赖；验证命令以当前 Consumer package 配置和 `docs/technical/verification-strategy.md` 为准。
