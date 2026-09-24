---
id: technical:public-frontend
type: technical-contract
status: active
relations:
  specifications:
    - docs/specifications/public-site.md
    - docs/specifications/page-content.md
  requirements:
    - docs/requirements/hui-employment-integration.md
  architecture:
    - docs/architecture/cms-architecture.md
    - docs/architecture/decisions/ADR-0002-public-site-multi-entry-modular-spa.md
    - docs/architecture/decisions/ADR-0003-public-shared-shell-components.md
    - docs/architecture/decisions/ADR-0004-public-shared-column-page.md
  design:
    - docs/design/public-site/DESIGN.md
  interface:
    - docs/technical/http-interface-contract.md
  verification:
    - docs/technical/verification-strategy.md
updated_at: 2026-09-23
---

# Public Frontend 跨 Feature 技术契约

## 1. 文档责任

本文只维护当前 Public Renderer implementation 在多个 Feature 之间持续需要一致的 HOW。Main / Party 的用户可观察行为、canonical URL、Carousel lifecycle、失败状态和 验收 由 Product Specifications 持有；颜色、字体、尺寸、间距、形状、组件 presentation 与原站视觉复刻规则由 `docs/design/public-site/DESIGN.md` 持有；Site / Theme / replaceability boundary 由 CMS Architecture 与 ADR 持有；Backend ↔ Public 的稳定 HTTP compatibility 由 `docs/technical/http-interface-contract.md` 唯一持有。

本文不复制 CMS Domain rules、HTTP DTO inventory、历史迁移 dataset、精确 asset hash、当前 component inventory、package version 或 E2E case inventory。

## 2. 当前实现适配

Public 当前由一个前端 package 提供 Main / Party 两个真实 Site Entry，并共享同一构建生命周期。两个 Entry 各自拥有 App / Router / theme / Site-specific page composition；共享部分只包含已经由产品事实证明稳定一致的 capability。

当前 implementation 使用 Vue / TypeScript / Vite；精确版本、HTML entry、source path 与 build script 由 package / Vite configuration 自己持有，不提升为 Product contract。

如果未来替换 Public Renderer，只要满足当前 Requirement / Specification / Architecture / Interface Contract，可以改变 framework、bundle、entry implementation 与 delivery adapter。

## 3. 共享 / 站点特定源码所有权

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

## 4. 仅公开端源码边界

Public production source只消费 Public contracts，不拥有 Admin CRUD / mutation / resource-management endpoint knowledge。

稳定 Public endpoint、wire projection、resource URL 与 error envelope 由 `http-interface-contract.md` 持有；Public TypeScript adapter只实现消费侧 mapping，不重新定义接口事实。

当前 Repository 通过 source-boundary check 对 `/api/admin/` endpoint knowledge执行 fail-fast guard。具体 guard脚本和扫描实现属于 Repository tooling；长期约束是：

- Public source不依赖 Admin-only endpoint；
- managed content在 Backend Public projection边界转换成 Public 可消费 representation；
- Public frontend不复制 Admin client来修补 Backend projection gap；
- by-code / by-group 等已建立的 scoped Public query由 Backend contract提供，不退回“全量取回再过滤”作为替代实现；
- source-boundary guard不能替代 Browser / interface-contract verification。

## 5. 路由 / 交付

Main 与 Party 的 canonical route由 `docs/specifications/public-site.md` 持有。Technical implementation需要保证：

- Main / Party direct access与refresh可到达正确 Entry；
- Entry file name / bundler chunk不进入 canonical URL；
- Main / Party Router不互相吞并 route namespace；
- Gateway fallback、static/resource/API route与当前 deployment adapter一致；
- compatibility URL若存在，只服务迁移，不成为新 canonical route。

Nginx规则、Vite input和具体 artifact名由 current Repository implementation持有。

## 6. 公开数据装配

页面按稳定业务 scope消费 Public API，不使用“全局固定窗口 + 前端过滤”代替 Backend已有的 scope查询。

Frontend adapter可以把 transport DTO转成 view-facing shape，但不能成为第二份 Interface / Domain Authority。Article、Page、CmsList、Resource 的 identity / lifecycle / effective content interpretation来自当前 Domain / Specification，HTTP representation来自 Interface Contract。

Navigation、CmsListItem 与 Advertisement 的 `openMode` 属于 Backend 已投影的显式数据。Public renderer 对空值、`_self`、`_blank` 只做直接 HTML target 映射；Advertisement 的 `NO_LINK` 只控制是否生成可点击链接。不得保留“`DEFAULT` + HTTP(S) URL => 新窗口”一类二次推导 helper。

异步装配必须绑定当前 route / scope；旧请求返回不得覆盖已经变化的 route state。

## 7. 页面渲染器集成

Page renderer使用明确稳定的 renderer identity进行选择，不通过 alias、URL、DOM shape或正文 heuristic猜测类型。

当前 implementation可以使用 renderer registry把 accepted renderer key映射到具体 Vue renderer；unknown / malformed renderer遵循 Specification 的可诊断 失败关闭 behavior。

Renderer registry是 implementation mechanism，不是 Page业务对象的新身份层；新增 renderer必须先有相应 Product / Domain / Architecture / Specification Authority。

## 8. 资源集成

Public 只消费当前公开资源 contract：stable Site assets、允许公开的 managed resources与受控 historical resources。

managed resource content / attachment 与 `/static/**` 的 HTTP namespace / binary compatibility 由 Interface Contract 持有。稳定模板资源不直接依赖 Legacy Source URL；mutable uploads不提升为 Site Package stable assets。具体 Runtime storage、manifest和asset projection由 Site Definition / Backend implementation持有。

## 9. 慧就业固定 iframe 集成

慧就业属于无需运营维护的固定第三方 seam，完整地址和业务编号映射由 Requirement 持有，并以 Public production source 中的只读工程常量实现；不得转存为 `SiteProperty`、普通 Page `embedUrl` 编辑项或新的 CMS 配置对象。

首页直接消费明确命名的就业日历、最新招聘与直播课程目标。招聘信息五个 PageGroup 成员和直播课程独立 Page 使用不同的稳定 renderer identity 表达各自业务映射；Public renderer registry 以该 identity 解析目标，不从 Page alias、URL、标题或 DOM 推断业务编号。

这些 Page 使用 `NONE + EXTERNAL` content profile，Site Definition 持有稳定 Page identity、分组关系、renderer identity 与 lifecycle，主要业务内容仍由慧就业持有。Generic CMS Core 只验证“显式外部 renderer + external ownership”组合，不硬编码 JilinJobs renderer 名称或慧就业地址。Admin 对这类非运营内容 profile 提供只读诊断，不开放目标地址与正文编辑。

统一 iframe 组件至少承担：

- 明确、可访问的 iframe title；
- 加载中、加载完成、浏览器 error 与超时状态；
- 超时或失败后的原位重试；
- 每次重试隔离旧加载状态；
- 不让单个外部 frame 的状态影响 Main Shell 或同页其他区域；
- 按首页紧凑区域与二级页面主体区域提供响应式高度，窄屏不产生本站页面横向溢出；
- 二级页面关闭 iframe 自身滚动，并按原网站已确认基线为招聘信息提供 `1300px`、为直播课程提供 `1250px` 的宿主承载高度，只保留宿主页面滚动。

当前 timeout 属于低风险工程参数，由组件内部持有，不进入 CMS 配置。由于跨源 iframe 无法读取内部 DOM，Public 只以浏览器可观察的 `load` / `error` 与 timeout 作为外层状态证据，不声称检查慧就业页面内部业务成功。

共享 Navigation 在目标被选择时收起移动端菜单，并抑制当前指针仍停留在父项上造成的桌面下拉残留；指针离开后恢复正常 hover / focus 行为。Site Definition 继续通过 Navigation `openMode` 显式表达浏览上下文：空值不输出 `target`，`_self` / `_blank` 直接投影同名 HTML `target`。Internal Router target 与 external URL 使用同一投影规则，Public 不再通过 URL 类型推断是否新窗口。

首页直播课程不得再包裹第二层同名标题；本站“更多”链接以覆盖嵌入页原入口的方式保持 `/page/live-course` 规范目标。招聘公告与其他首页资讯列表消费 `docs/design/public-site/DESIGN.md` 的同一组字体、颜色与交互 token，不尝试跨源修改慧就业 iframe 内部样式。

## 10. 视觉设计 Authority 投影

Public Renderer 必须把 `docs/design/public-site/DESIGN.md` 当作视觉设计输入，而不是从现有 selector 或组件样式反向推导长期设计规则。

实现约束：

- YAML 中的 normative token 应通过 CSS variables、theme variables、组件 props / class 或等价的薄映射进入实现；本契约不强制一次性重构现有 CSS；
- 同一语义颜色、typography 或 spacing 不应在多个组件各自复制一组含义相同但命名不同的 magic value；
- 当前实现值与 DESIGN token 不一致时，先判断 implementation defect、stale Design Authority 或 `DESIGN.md` 已明确记录的 Known Gap；
- `DESIGN.md` 的 Known Gap 不授予 Agent 自行选择最终视觉值的权限；后续修复应先取得当前 Authority / visual evidence，再 promotion；
- renderer replacement 必须能够从 Requirement + Specification + Design + Architecture / Technical 重建公开站，而不是依赖读取被替换 CSS 作为设计输入。

## 11. 构建 / 验证适配

当前 Public package自己持有 Node engine、Vue / TypeScript / Vite与 scripts 的精确版本。正式 build包含 source-boundary guard、Vue-aware type-check与bundler build。

影响 API adapter 时验证 `http-interface-contract.md` compatibility；影响 route、DOM、async data、resource或用户交互时追加 浏览器验证；存在视觉 验收时再取得对应 AI / 人工视觉证据。证据规则以 `docs/technical/verification-strategy.md` 与 live-discovered verification Rules为准。

## 12. 不由本文拥有

- Main / Party 产品身份与业务信息架构；
- Carousel用户可观察行为；
- CMS Domain data semantics；
- HTTP endpoint / DTO field 的第二份 Authority；
- 历史迁移 counts / fingerprint；
- Site asset具体 hash / resource inventory；
- package / component / test case inventory；
- 已完成 Public replaceability / source-isolation计划的过程历史。
