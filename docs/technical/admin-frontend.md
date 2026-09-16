---
id: technical:admin-frontend
type: technical-contract
status: active
relations:
  specifications:
    - docs/specifications/admin-site.md
  architecture:
    - docs/architecture/cms-architecture.md
    - docs/architecture/decisions/ADR-0001-admin-frontend-module-integration.md
  interface:
    - docs/technical/http-interface-contract.md
  verification:
    - docs/technical/verification-strategy.md
updated_at: 2026-09-16
---

# Admin Frontend 跨 Feature 技术契约

## 1. 文档责任

本文只维护 Admin 在多个 Feature 之间持续需要共享的前端 implementation contract。用户可观察行为、管理能力与 Acceptance 由 `docs/specifications/admin-site.md` 持有；模块化 SPA 的长期边界与 ADR 由 Architecture Authority 持有；Backend ↔ Admin 的 endpoint / request / response / error compatibility 由 `docs/technical/http-interface-contract.md` 唯一持有。

本文不复制每个 CMS 对象的业务规则、HTTP DTO inventory、当前组件清单、精确依赖版本、测试数量或某个 Execution Unit 的设计计划。

## 2. Application / module model

Admin 当前是单一 Vue SPA，由 Application Shell 聚合业务模块，不使用 runtime microfrontend。

当前 module contract 至少表达：

- module identity；
- landing route；
- routes；
- optional compatibility routes；
- navigation sections。

Shell 只消费模块公开 contract，不读取模块内部页面 / API / component implementation。CMS 是当前实际业务模块；没有真实第二模块需求时，不预先建立插件生命周期、跨模块 event bus 或远程加载框架。

具体 `AdminModule` TypeScript shape、registry、Router 与源码目录以 current implementation 为准。

## 3. Routing

Router 将业务模块置于 Admin application namespace，canonical URL 与 compatibility behavior 必须满足 Admin Specification。

Technical 层只要求：

- Shell 不硬编码模块内部页面逻辑；
- compatibility route 不成为导航与新代码的 primary target；
- direct access / refresh 与 application base 协同；
- route-level loading / chunking 可以由当前 bundler 实现，但 chunk 文件名不是长期 contract。

## 4. API responsibility

Admin source 只消费 Admin authoring / management contract；它可以拥有 mutation、validation feedback、Resource authoring 与管理态 DTO adapter。

稳定 HTTP namespace、method、query、wire field、status、multipart 与 error envelope 统一来自 `http-interface-contract.md`。Frontend API Type / Draft model 是 consumer-side adapter，不是第二份 Interface / Domain Authority；adapter 应投影 canonical wire contract，而不是通过局部 helper 发明不存在的 Backend endpoint。

Backend 返回的 stable identity、preset、status、policy 等字段必须按当前 Domain / Specification 解释，不由 UI 自行发明可编辑语义。发现 adapter 与 Backend / Interface Contract 不一致时必须 fail closed 判断 stale client、stale provider 或 contract change，不以“TypeScript 已声明”为理由自动扩张接口。

## 5. Shared authoring primitives

跨 CMS 管理页面已经证明稳定一致的 authoring / presentation responsibility 应使用共享 primitive，而不是每个 Feature 重建一套。

当前长期类别包括：

- managed image/resource selection 与 current-value preview；
- adaptive image preview / reusable viewer integration；
- compact accessible row actions；
- shared container/member navigation patterns；
- Rich Text authoring adapter；
- consistent loading / empty / error / validation feedback。

具体 component 名、Element Plus API、DOM class 与样式参数由 implementation 持有；只有当多个 Feature 真实共享同一职责时才进入 shared，不因代码相似机械抽象。

## 6. Rich Text integration

Admin Rich Text editor 通过薄 adapter 接入当前 authoring contract：

- editor state 不成为业务正文之外的第二份持久化 Authority；
- managed images / attachments 走 CMS Resource contract；
- paste / HTML policy 必须尊重当前 compatibility / security boundary；
- editor brand/version 与 toolbar layout 由当前 implementation/package 持有；
- 替换 editor 不应要求改变 Article/Page 的 Domain semantics。

更具体的跨 Consumer Rich Text HOW 由 `docs/technical/rich-text-authoring.md` 持有。

## 7. UI state

纯界面局部状态默认留在客户端，例如折叠、当前局部 selection、dialog visibility。只有当前 Product / Domain Authority 明确要求跨会话、跨设备或用户级持久化时，才建立新的 Runtime persistence；不能因为 UI 有 state 就自动创建 SiteProperty 或用户 Profile。

## 8. Build / verification adapter

当前 Admin package 以 Repository `package.json` 持有 Node engine、Vue / TypeScript / Vite / Element Plus / editor 的精确版本与 scripts。

正式 build 必须包含 Vue-aware type-check 与 bundler build；有 Router / DOM / async / user interaction 风险时追加 Browser Evidence。触达 API adapter 时，应验证 consumer 与 `http-interface-contract.md` 的 endpoint / wire compatibility，而不是让 client type 成为唯一接口测试 oracle。具体命令和版本直接从 package / Workflow 恢复，本文不复制 inventory。

## 9. 不由本文拥有

- CMS Domain object / lifecycle / validation rule；
- Admin 信息架构的产品文案与 Acceptance；
- HTTP endpoint / DTO 的第二份长期 Authority；
- 每个页面的 current component tree；
- package version inventory；
- Browser test case inventory；
- 已完成模块拆分或 UI convergence 的历史计划。
