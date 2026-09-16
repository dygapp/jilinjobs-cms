---
id: technical:public-frontend
type: technical-contract
status: active
relations:
  specifications:
    - docs/specifications/public-site.md
    - docs/specifications/page-content.md
  architecture:
    - docs/architecture/cms-architecture.md
    - docs/architecture/decisions/ADR-0002-public-site-multi-entry-modular-spa.md
    - docs/architecture/decisions/ADR-0003-public-shared-shell-components.md
    - docs/architecture/decisions/ADR-0004-public-shared-column-page.md
  interface:
    - docs/technical/http-interface-contract.md
  verification:
    - docs/technical/verification-strategy.md
updated_at: 2026-09-16
---

# Public Frontend 跨 Feature 技术契约

## 1. 文档责任

本文只维护当前 Public Renderer implementation 在多个 Feature 之间持续需要一致的 HOW。Main / Party 的用户可观察行为、canonical URL、Carousel lifecycle、失败状态和 Acceptance 由 Product Specifications 持有；Site / Theme / replaceability boundary 由 CMS Architecture 与 ADR 持有；Backend ↔ Public 的稳定 HTTP compatibility 由 `docs/technical/http-interface-contract.md` 唯一持有。

本文不复制 CMS Domain rules、HTTP DTO inventory、历史迁移 dataset、精确 asset hash、当前 component inventory、package version 或 E2E case inventory。

## 2. Current implementation adapter

Public 当前由一个前端 package 提供 Main / Party 两个真实 Site Entry，并共享同一构建生命周期。两个 Entry 各自拥有 App / Router / theme / Site-specific page composition；共享部分只包含已经由产品事实证明稳定一致的 capability。

当前 implementation 使用 Vue / TypeScript / Vite；精确版本、HTML entry、source path 与 build script 由 package / Vite configuration 自己持有，不提升为 Product contract。

如果未来替换 Public Renderer，只要满足当前 Requirement / Specification / Architecture / Interface Contract，可以改变 framework、bundle、entry implementation 与 delivery adapter。

## 3. Shared vs Site-specific source ownership

可以进入 shared 的长期 implementation responsibility 包括：

- Public API transport / public DTO adapters；
- resource URL / metadata / SEO utility；
- 已接受的 Navigation / Footer shared shell；
- 已接受的二级栏目页面 presentation primitive；
- 无主题 Carousel lifecycle / state primitive；
- 多 Site 都需要且行为真正一致的通用 utility。

保持 Site-specific：

- Main / Party App 与 Router composition；
- theme / Banner / homepage or topic layout；
- Site-specific route scope；
- 尚未由 Requirement / Architecture 证明可共享的模板与交互。

“代码相似”不是 shared owner 的充分条件；如果产品语义已经不同，不能用 shared abstraction 隐藏分叉。

## 4. Public-only source boundary

Public production source只消费 Public contracts，不拥有 Admin CRUD / mutation / resource-management endpoint knowledge。

稳定 Public endpoint、wire projection、resource URL 与 error envelope 由 `http-interface-contract.md` 持有；Public TypeScript adapter只实现消费侧 mapping，不重新定义接口事实。

当前 Repository 通过 source-boundary check 对 `/api/admin/` endpoint knowledge执行 fail-fast guard。具体 guard脚本和扫描实现属于 Repository tooling；长期约束是：

- Public source不依赖 Admin-only endpoint；
- managed content在 Backend Public projection边界转换成 Public 可消费 representation；
- Public frontend不复制 Admin client来修补 Backend projection gap；
- by-code / by-group 等已建立的 scoped Public query由 Backend contract提供，不退回“全量取回再过滤”作为替代实现；
- source-boundary guard不能替代 Browser / interface-contract verification。

## 5. Routing / delivery

Main 与 Party 的 canonical route由 `docs/specifications/public-site.md` 持有。Technical implementation需要保证：

- Main / Party direct access与refresh可到达正确 Entry；
- Entry file name / bundler chunk不进入 canonical URL；
- Main / Party Router不互相吞并 route namespace；
- Gateway fallback、static/resource/API route与当前 deployment adapter一致；
- compatibility URL若存在，只服务迁移，不成为新 canonical route。

Nginx规则、Vite input和具体 artifact名由 current Repository implementation持有。

## 6. Public data assembly

页面按稳定业务 scope消费 Public API，不使用“全局固定窗口 + 前端过滤”代替 Backend已有的 scope查询。

Frontend adapter可以把 transport DTO转成 view-facing shape，但不能成为第二份 Interface / Domain Authority。Article、Page、CmsList、Resource 的 identity / lifecycle / effective content interpretation来自当前 Domain / Specification，HTTP representation来自 Interface Contract。

异步装配必须绑定当前 route / scope；旧请求返回不得覆盖已经变化的 route state。

## 7. Page renderer integration

Page renderer使用明确稳定的 renderer identity进行选择，不通过 alias、URL、DOM shape或正文 heuristic猜测类型。

当前 implementation可以使用 renderer registry把 accepted renderer key映射到具体 Vue renderer；unknown / malformed renderer遵循 Specification 的可诊断 fail-closed behavior。

Renderer registry是 implementation mechanism，不是 Page业务对象的新身份层；新增 renderer必须先有相应 Product / Domain / Architecture / Specification Authority。

## 8. Resource integration

Public 只消费当前公开资源 contract：stable Site assets、允许公开的 managed resources与受控 historical resources。

managed resource content / attachment 与 `/static/**` 的 HTTP namespace / binary compatibility 由 Interface Contract 持有。稳定模板资源不直接依赖 Legacy Source URL；mutable uploads不提升为 Site Package stable assets。具体 Runtime storage、manifest和asset projection由 Site Definition / Backend implementation持有。

## 9. Build / verification adapter

当前 Public package自己持有 Node engine、Vue / TypeScript / Vite与 scripts 的精确版本。正式 build包含 source-boundary guard、Vue-aware type-check与bundler build。

影响 API adapter 时验证 `http-interface-contract.md` compatibility；影响 route、DOM、async data、resource或用户交互时追加 Browser Verification；存在视觉 Acceptance时再取得对应 AI / Human Visual Evidence。证据规则以 `docs/technical/verification-strategy.md` 与 live-discovered verification Rules为准。

## 10. 不由本文拥有

- Main / Party 产品身份与业务信息架构；
- Carousel用户可观察行为；
- CMS Domain data semantics；
- HTTP endpoint / DTO field 的第二份 Authority；
- Historical Migration counts / fingerprint；
- Site asset具体 hash / resource inventory；
- package / component / test case inventory；
- 已完成 Public replaceability / source-isolation计划的过程历史。
