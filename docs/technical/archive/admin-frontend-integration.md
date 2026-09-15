# 管理端前端模块集成技术计划

## 1. 目标

本文件落实 `ADR-0001：管理端前端模块集成架构`。当前 Admin 保持单一 Vue 3 SPA，同时把 Application Shell 与 CMS Module 分离，使 CMS 可以在未来完整管理平台中作为稳定模块被集成。

## 2. 当前目录边界

```text
frontend/admin/src/
├── app/
│   ├── App.vue
│   ├── admin.css
│   ├── adminModule.ts
│   ├── moduleRegistry.ts
│   └── router.ts
├── modules/
│   └── cms/
│       ├── api/
│       ├── components/
│       ├── views/
│       ├── iconCatalog.ts
│       └── module.ts
├── env.d.ts
└── main.ts
```

`app/` 只负责管理端应用级职责；`modules/cms/` 负责 CMS 页面、API 客户端和 CMS 内部组件。当前 CMS 专用组件不提升为 `shared/`，只有出现第二个真实模块复用需求后才评估共享层。

## 3. Module Contract

`AdminModule` 当前只暴露：

- `id`；
- `routes`；
- `navigationSections`。

Shell 通过 `moduleRegistry.ts` 聚合模块声明。当前不建立插件生命周期、远程加载器、模块间事件总线或全局业务 Store。

## 4. Route Boundary

Admin Vite base 继续为 `/admin/`。CMS canonical route 使用：

- `/admin/cms/articles`
- `/admin/cms/pages`
- `/admin/cms/lists`
- `/admin/cms/columns`
- `/admin/cms/navigation`
- `/admin/cms/advertisements`
- `/admin/cms/site-config`
- `/admin/cms/static-resources`

原 `/admin/articles` 等路径只作为兼容重定向，不再由菜单生成。根 `/admin/` 重定向到 `/admin/cms/articles`。

## 5. Build Boundary

CMS route component 统一通过动态 `import()` 注册，使 Vite 按路由生成异步 chunk。当前仍由 `frontend/admin` 统一执行 TypeScript 校验、Vite build 和 Playwright E2E。

未来如果模块转入独立 Repository，可增加模块自己的 TypeScript、Unit/Component Test 和 package build；Shell 在构建期消费版本化 package。只有出现不重建 Shell 即发布模块的真实要求时才评估 Module Federation。

## 6. Compatibility Boundary

本次不调整 Backend `/api/admin/**`，不改变 CMS 数据模型和 Public Site，不新增登录/账号/权限，不引入 Module Federation、qiankun 或 iframe Runtime。

## 7. Verification

至少验证：

- `/admin/` 最终进入 `/admin/cms/articles`；
- 八类 CMS 菜单均生成 `/admin/cms/**` URL；
- 原 `/admin/articles` 等旧路径能够重定向到对应 canonical URL；
- 所有 CMS 页面仍可访问且既有 CRUD / 图片 / preset / Browser E2E 回归继续通过；
- Admin build 产生路由级动态 chunk，证明页面不再由 Router 静态 import 到初始 bundle。
