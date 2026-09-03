# ADR-0002：公开站采用按 Site Boundary 划分的 Multi-entry Modular SPA

- Status: Accepted
- Date: 2026-09-01
- Scope: `frontend/public-site`

## 1. Context

当前公开站已经是 Vue 3 + Vue Router 应用，但 Vite 同时构建 `index.html` 与 `page.html`。`main.ts` 和 `page-main.ts` 实际内容相同，两个 HTML Entry 都挂载同一个 `App` 和同一个 Router；Nginx 只是让 `/page/**` fallback 到 `page.html`。因此当前结构承担了 Multi-entry 的配置成本，却没有形成真正不同的 Runtime / Product Boundary。

与此同时，项目负责人已经确认“中心党建”将在下一阶段立即实现，不再长期保持主导航占位。中心党建具有明显独立的红色视觉主题，适合与中心主站形成独立 Site / Theme Boundary；但当前没有独立 Repository、独立部署、不同技术栈、不同团队或独立发布生命周期的真实需求。

需要同时解决：

1. 消除 `/page/**` 对重复 HTML/Vue Entry 的无业务价值依赖；
2. 让中心主站源码从平铺 `views/components/api/css` 演进为可扩展模块边界；
3. 在本次重构中为中心党建建立足够强的 App / Router / Shell / Theme 隔离；
4. 不因为视觉主题不同而提前承担第二套 package、依赖、CI、构建、部署和 API transport 的工程成本；
5. 为未来真正独立发布 / 部署时保留低成本拆分路径。

## 2. Decision

公开站采用 **Multi-entry Modular SPA**，Entry 按真实 Site / Theme Boundary 划分，而不是按普通页面类型划分。

当前两个公开 Site：

```text
frontend/public-site
├── Main Site
│   └── /, /column/**, /article/**, /page/**
└── Party Building Site
    └── /party/**
```

两者当前：

- 位于同一 `frontend/public-site` Vue / Vite 工程；
- 使用同一套 package / dependency baseline；
- 一次 Vite build 生成多个真实 Site Entry；
- 同一部署单元通过 Nginx 按 URL namespace 选择 HTML fallback；
- 共享同一个 Spring Boot CMS Backend；
- 不使用 Module Federation、iframe 或其他 Runtime Microfrontend 机制。

### 2.1 Main Site Boundary

Main Site 持有：

- `sites/main/app/`：App、Router、bootstrap；
- `sites/main/shell/`：Header、Footer、Navigation Layout；
- `sites/main/modules/`：`home`、`content`、`page`、`integration` 等页面职责模块；
- 主站蓝白视觉体系与页面样式。

Main Site 的页面路由使用 Vue Router 动态 `import()` 进行 route-level lazy loading。

原 `page.html / page-main.ts` 删除。`/page/**` 与 `/column/**`、`/article/**` 一样由 Main Site Entry 直接承载。

### 2.2 Party Building Site Boundary

Party Building Site 持有：

- `sites/party/app/`：独立 App、Router、bootstrap；
- `sites/party/shell/`：党建 Header / Footer / Navigation 基础 Shell；
- `sites/party/modules/`：党建页面模块；
- 独立红色主题、页面 Frame 和 Site-level CSS。

当前 canonical namespace 为 `/party/**`，基础入口为 `/party/`。

本次架构阶段只建立可独立访问、可直接刷新、主题隔离且可扩展的基础页面框架。党建真实栏目、内容、视觉细节和专属后台能力由后续 Requirement / Specification 单独确认。

### 2.3 Shared Boundary

`src/shared/` 只承担已经证明跨 Site 稳定复用且不携带主题所有权的技术能力，例如：

- API transport / CMS DTO；
- 静态资源 URL 处理；
- SEO / metadata utility；
- 明确无主题的通用工具。

以下内容默认不得为“去重复”强行进入 Shared：

- Header / Footer；
- Navigation Layout；
- Page Frame；
- Theme tokens / colors；
- Typography；
- 首页或专题区块布局。

### 2.4 Build / Runtime Boundary

Vite Entry：

```text
index.html -> Main Site
party.html -> Party Building Site
```

Gateway：

```text
/admin/** -> Admin Site
/party/** -> Party Building Site -> party.html fallback
其他公开路径 -> Main Site -> index.html fallback
/api/** -> Backend
/static/** -> Backend/static resources
```

公开 canonical URL 与 HTML 文件名解耦，用户不直接访问 `party.html`。

## 3. Why not a separate Party Building frontend project now

独立前端工程能够提供更强的 package/build/deploy 隔离，但当前只有视觉和主题边界得到确认，尚未出现工程生命周期边界。

如果现在建立 `frontend/party`，会立即增加：

- 第二套 `package.json` / Vite / TypeScript 配置；
- 第二次 npm install、build artifact 和 CI job；
- 独立 Browser Verification 入口与更多 Gateway/Review 配置；
- API types / transport / SEO / resource helpers 的复制或额外 shared package；
- 当前同团队、同 Backend、同部署场景下不必要的集成成本。

因此独立视觉主题由独立 Site Entry / Shell / Theme 解决，不把 UI Boundary 机械提升为 Project Boundary。

## 4. Future extraction trigger

只有出现以下真实需求之一，再评估把 `sites/party` 提取为独立前端工程或独立应用：

- 独立发布或独立部署；
- 独立域名及明显不同的运行环境；
- 不同团队长期独立维护；
- 不同技术栈或依赖版本；
- 党建规模发展为完整独立网站，且主站修改不应触发其构建/验证；
- 性能、故障、安全或发布隔离成为明确非功能要求。

当前不以“红色主题”本身作为拆分触发条件。

## 5. Consequences

正向影响：

- Multi-entry 获得真实业务语义，删除当前重复 `page.html` Entry；
- 主站与党建视觉 Shell 强隔离；
- 保持单工程、单依赖基线、单构建部署，避免过早分布式前端复杂度；
- Site 内部继续模块化并支持 route-level lazy loading；
- 未来如需独立工程，可从清晰 `sites/party` 边界直接提取。

代价与约束：

- 同一次 Public Site build 仍会覆盖两个 Site；
- Vite / Nginx / Browser Verification 必须显式验证两个 Entry；
- Shared 边界需要克制，防止主站主题样式反向污染党建；
- 当未来出现真正独立生命周期时，需要新的 ADR 重新评估工程拆分。

## 6. Non-goals

本 ADR 不决定：

- 中心党建完整栏目和信息架构；
- 党建最终视觉复刻方案；
- 党建是否需要专属 CMS / Admin Module；
- SSR / SSG / Hybrid Rendering；
- 独立 Repository / Deployment；
- Module Federation。

上述事项只有在对应需求和证据出现后再单独决策。