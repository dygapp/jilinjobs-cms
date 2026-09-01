# ADR-0001：管理端前端模块集成架构

- 状态：Accepted
- 日期：2026-09-01

## Context

管理系统会随着业务范围扩展形成多个相对独立的前端业务模块，同时需要统一的管理端 Shell、导航、路由和用户体验。可选方案包括 iframe 集成、无明确边界的大型 SPA、模块化 SPA，以及以 Module Federation 等技术实现的运行时微前端。

当前 `jilinjobs-cms` 已使用 Vue 3、Vue Router、Vite 和 Element Plus，并且 CMS Admin 当前仍由单一团队、单一构建和单一部署链路维护。仓库没有证据表明当前需要独立部署的前端模块或跨技术栈运行时组合。

## Decision

管理端当前采用“模块化 SPA + 可演进微前端”架构。

1. 当前运行时仍是单一 Vue SPA、单一 Vue Router、单一构建和单一部署。
2. Admin Shell 与业务模块建立明确源码边界。Shell 只消费模块声明，不直接维护模块内部页面实现。
3. 每个模块声明自己的路由与导航，并使用 Vue Router 动态 `import()` 实现路由级懒加载和构建期代码分割。
4. 模块可以位于同一 Repository，也允许未来以独立 Repository 和版本化 package 的形式在构建期被 Shell 消费；独立 Repository 不自动意味着独立部署。
5. Module Federation 属于未来演进机制，不作为当前基础设施依赖。只有出现真实的独立发布、独立部署、跨团队或跨技术栈需求时，才评估把特定 Local Module 升级为 Remote Module。
6. iframe 不作为正常业务模块的默认集成机制，只保留给遗留系统、第三方页面或需要强隔离的特殊场景。
7. 管理端模块使用 `/admin/<module>/<feature>` 路由命名空间。当前 CMS canonical URL 为 `/admin/cms/**`；原 `/admin/<cms-feature>` 路径仅作为兼容重定向保留。
8. 本次前端路由命名空间调整不自动改变 Backend API。当前 `/api/admin/**` 是否未来增加服务/模块 namespace，需结合 Backend Service Boundary 单独决策。
9. 公开站可以采用模块化源码组织和路由懒加载，但不把可演进微前端作为当前公开站核心架构目标。
10. 模块声明同时拥有自身 landing route 与 compatibility routes；Shell Router 不枚举任何模块 feature path。Module Registry 必须校验默认入口唯一、模块 id 唯一、canonical route/navigation 均位于自身 namespace，并拒绝重复路由路径，使模块边界成为可执行约束而非仅文档约定。
11. 样式所有权遵循相同边界：`app/` 只持有全局基础与 Shell 自身样式；跨模块可复用的管理页布局/交互基础样式进入 `shared/`；模块专属页面、领域组件和 feature selector 由模块目录自己持有。Shell 不通过全局 CSS 反向维护业务模块内部 class，模块组件优先自持 scoped 样式。
12. 源码穿越边界只允许两类显式依赖：`moduleRegistry.ts` 作为 composition root 引用各业务模块声明；业务模块的 `module.ts` 引用 `app/adminModule.ts` 公开 Module Contract。除这两类外，Shell 不直接 import 模块内部实现，模块页面/组件也不 import Shell 内部实现。

## Consequences

### Positive

- CMS 作为独立业务模块可以更容易集成到未来完整管理平台。
- 当前不承担 Module Federation 的运行时、依赖共享、远程版本和故障治理成本。
- 模块边界、路由 namespace、模块声明契约与样式所有权可直接成为未来微前端演进的稳定切分点。
- 可以保持现有 Vue/Vite/Element Plus 技术栈和统一 E2E 验证链路。
- Shell Router 不再持有 CMS feature 路由知识，新增本地模块时只需要增加模块声明与 Registry composition，不需要修改 Shell 的模块内部路由表。
- Shell 样式不再隐式依赖 CMS 页面结构；共享管理页视觉 primitives 与业务模块私有样式具有明确所有者。

### Trade-offs

- 当前模块仍随 Admin SPA 一起发布，不能做到 Remote Module 的独立部署。
- 需要维护 Shell、Shared Layer 与 Module Contract，禁止通过任意跨目录 import 或全局 CSS 重新形成隐式耦合。
- 旧 CMS URL 需要在兼容期维护重定向，但兼容映射由 CMS Module 自己维护。

## Current Consumer Mapping

当前 `jilinjobs-cms` 只实现 CMS Module，不在本仓库引入尚未成为项目事实的其他平台业务模块名称。源码边界为：

```text
frontend/admin/src/
├── app/                         # Admin Shell、Router、Module Registry；仅 Shell/全局基础样式
│   └── admin.css
├── shared/                      # Shell 与业务模块都可消费的管理端基础 primitives
│   └── admin-content.css
└── modules/
    └── cms/                     # CMS Module
        ├── api/
        ├── components/          # CMS-local 组件；私有样式由组件自身或模块持有
        ├── views/
        ├── admin.css            # CMS 页面/feature 私有样式
        ├── iconCatalog.ts
        └── module.ts
```

`app/router.ts` 仅消费 Registry 聚合后的 `adminDefaultRoute` 与 `adminModuleRoutes`。当前 CMS 的 `/cms/**` canonical routes、旧顶级路径 compatibility redirects、默认 landing route 与 CMS 私有样式全部由 `modules/cms` 声明或持有；`moduleRegistry.ts` 在应用启动时验证模块路由契约。

`main.ts` 负责加载全局 Shell 样式与 shared 管理页基础样式；`modules/cms/module.ts` 负责加载 CMS 私有样式。`AdminIconAction.vue`、`AdminPanelToggle.vue` 等 CMS-local 组件不再由 `app/admin.css` 维护内部 selector。

本 ADR 不扩大当前产品范围，不引入登录、账号、角色或权限实现。
