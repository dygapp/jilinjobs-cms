# Frontend 局部约束

本文件只约束 frontend/**。Repository-wide Authority、权限和通用约束继续继承根 AGENTS.md 与 docs/governance/constraints.md。

## 当前技术事实

frontend/admin 与 frontend/public-site 是独立 Vue / Vite 应用。实际依赖版本和命令以各自 package.json 为准；当前两者使用 Vue 3.5.x + TypeScript。

## Vue / TypeScript 约束

- 新建 Vue SFC 在没有更具体兼容约束时优先使用 script setup；不因此批量迁移稳定 Options API / 既有组件。
- Props 保持父到子的单向输入；需要编辑语义时使用本地 state、emit、标准 component v-model 或已经存在的共享状态机制。
- defineProps / defineEmits 的 runtime declaration 与 type declaration 不在同一声明中混用；存在 runtime validation 责任时不得为了简化类型而删除。
- defineModel() 只用于确实属于标准 component v-model 的新契约，不为使用新 API 改写稳定自定义 prop / emit contract。
- 涉及 Vue + TypeScript 类型 claim 时使用 Vue-aware typecheck。当前两个应用的 npm run build 都包含 vue-tsc --noEmit，不能把纯 bundler success 单独描述为类型正确证据。
- 不为了匹配通用 Guide / 示例机械升级依赖；技术选择以本 Consumer 当前 Authority、package scripts 与 docs/technical/verification-strategy.md 为准。
