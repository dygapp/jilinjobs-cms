# 前端后续收敛执行单元

本文记录中心党建 EU-29 完成后的下一阶段前端工作规划。当前仍以 EU-29 Party Historical Content Migration & Final Review 为主动执行单元；本文只固化后续顺序、边界与已确认决策，不提前改变当前阶段状态。

计划顺序：

```text
EU-29 Party Historical Content Migration & Final Review
→ EU-30 Carousel Architecture & Behavior Convergence
→ EU-31 Browser Compatibility & Runtime Guard Convergence
→ 后续公开站点剩余内容与集成收敛
```

---

## EU-30 — Carousel Architecture & Behavior Convergence

### 状态

计划中。EU-29 关闭后再进入专项讨论与实施。

### Goal

对当前公开站轮播图实现进行专项复核，明确 Main Site 与 Party 的轮播能力边界，判断现有实现是否需要在功能、交互、响应式、数据模型或技术方案上重构，并在形成明确方案后完成必要实现与验证。

EU-30 当前只做占位规划，不预设必须重构，也不预设必须引入第三方轮播组件。

### Discussion Scope

EU-30 启动后至少重新读取并检查：

- Main Site 与 Party 当前轮播实现、重复能力和共享边界；
- 当前 `CmsList`、Main 轮播数据与 `PARTY_CAROUSEL` 是否继续满足产品需求；
- 自动播放、手动切换、循环、暂停、hover/focus、触摸与手势行为；
- 单图、无图、图片加载失败、数据延迟等边界状态；
- Desktop / Mobile 的尺寸、比例、裁切、容器高度与响应式策略；
- 动画实现、定时器生命周期、页面隐藏/恢复和性能；
- 键盘操作、可访问性以及 `prefers-reduced-motion`；
- 是否继续轻量自实现，还是采用经验证的第三方 Carousel 方案；
- Admin 中轮播成员维护能力是否需要随 Public 实现调整；
- 最终实现使用的 JavaScript、Web API 和 CSS 特性是否会影响 EU-31 的浏览器兼容基线。

### Non-goals

- 不在 EU-29 关闭前提前重构轮播实现；
- 不因为规划 EU-30 而修改 EU-29 当前迁移模型；只要现有 `PARTY_CAROUSEL` 足以完成历史迁移，就先按 EU-29 收口；
- 不预设必须引入第三方依赖；
- 不为了未来可能需求提前扩展通用 CMS 数据模型；
- 不在 EU-30 中提前完成完整浏览器最低版本收敛；浏览器兼容性由 EU-31 负责，但 EU-30 应避免无必要使用明显过新的浏览器特性。

### Exit Condition

EU-30 只有在轮播方案、实现边界和必要验证完成后关闭。若 EU-30 改变 Public Site DOM、CSS、交互、数据模型或依赖，EU-31 必须针对最终实现取得新的兼容性证据，不复用受影响的旧兼容证据。

---

## EU-31 — Browser Compatibility & Runtime Guard Convergence

### 状态

计划中。EU-30 完成后进入。

### Goal

为 Public Site 与 Admin 建立显式、可维护且不随 Vite / 依赖默认值静默漂移的浏览器兼容契约；在当前 Vue 3 技术路线可合理支持的范围内降低浏览器最低版本，并建立运行时兼容检测、低版本提示、构建目标和独立专项验证流程。

本单元采用“双端统一兼容技术路线、两套最低版本标准”：Public Site 面向公众，优先向更低版本下探；Admin 面向管理人员，也适当降低版本，但不承担与 Public 相同的超低版本兼容成本。

### 1. Compatibility Scope

#### Public Site

Primary Scope：`frontend/public-site`，包括：

- Main Site；
- Party Site；
- Main / Party Shared Shell；
- `/column/**`、`/article/**`、`/page/**`、Page Group / Tab、业务指南等公开页面；
- Desktop / Tablet / Mobile 响应式与公开交互。

Public Site 的 Browser Compatibility 属于正式 Acceptance Requirement。

#### Admin

Scope：`frontend/admin`。

Admin 同样建立显式最低版本和兼容检测，但采用更高的最低版本；不追求 IE11，不为了与 Public 对齐而机械降低到 Public 的最低版本。Admin 兼容治理的长期原因是 CMS 后续可能并入更大的业务平台，面向学校管理员等更广用户群，浏览器环境不应完全依赖 Vite 当前默认值。

### 2. Candidate Minimum Browser Matrix

以下版本是 EU-31 的调查下限，不是现在直接宣布的最终支持契约。正式版本必须由依赖边界、构建验证、Web API / CSS 审计和真实浏览器测试共同确定。

#### Public Site Candidate Minimum

| 浏览器 | 初始调查下限 |
|---|---:|
| Chrome / Chromium | 约 80 |
| Edge Chromium | 约 80 |
| Firefox | 约 78 |
| Safari | 13～14 |
| iOS Safari | 13～14 |
| Android Chrome / WebView | 按实际能力与运行证据确定 |
| IE11 | 当前不支持 |

Public Site 不机械继承 Element Plus 的最低版本。EU-31 开始时应先确认 Public Runtime 是否实际消费 Element Plus；若没有实际 Runtime 依赖，则不让 Element Plus 自动抬高 Public 最低版本。

#### Admin Candidate Minimum

| 浏览器 | 初始调查下限 |
|---|---:|
| Chrome | 85 |
| Edge | 85 |
| Firefox | 79 |
| Safari | 14.1 |
| IE11 | 不支持 |

Admin 候选线优先参考当前 Element Plus 官方支持边界和本项目真实运行结果。最终版本可以在证据不足或真实兼容缺陷存在时适度提高，但不能因为 Vite 默认 target 更高就直接放弃向下兼容调查。

### 3. Build Compatibility Contract

EU-31 应显式治理 Public 与 Admin 的构建目标，避免未来 Vite 升级静默改变 Browser Contract。

至少评估并固定：

- `build.target`；
- `build.cssTarget`；
- 是否存在必要的局部 polyfill；
- 是否需要 browserslist 或等价的单一兼容版本来源；
- 构建目标与 Runtime Compatibility Policy 的一致性。

默认不引入 `@vitejs/plugin-legacy` 或大范围 global polyfill。优先顺序：

```text
Baseline / widely supported native feature
→ HTML/CSS fallback
→ Progressive Enhancement
→ 小范围兼容写法
→ 局部 Polyfill
→ 有明确必要时才评估 Legacy Bundle
```

Vite 能把可转换的现代 JavaScript 语法降级到较低 target，但 transpilation 不能自动补齐所有 Web API，也不能突破 Vue 3 自身不可补齐的 Runtime 边界，因此“build 成功”不能单独证明浏览器兼容。

### 4. JavaScript / Web API / CSS Audit

EU-31 必须对 Public 做完整审计，对 Admin 做与其 Candidate Minimum 匹配的审计。

重点包括：

- JavaScript syntax 与打包结果；
- `Promise`、`Proxy`、`Symbol`、`URL`、`fetch`、Observer、Clipboard、File/Blob、Storage、Intl 等实际使用的 Web API；
- CSS Grid / Flex、`gap`、`aspect-ratio`、`position: sticky`、`object-fit`、`clamp()`、`:has()`、container query、dynamic viewport units、logical properties、filter/backdrop、颜色函数、scrollbar、form controls 等实际使用的 CSS 能力；
- Main / Party / Admin 依赖组件自身的兼容边界；
- CSS vendor prefix / fallback 是否必要；
- DOM / Router / async interaction 在较低版本浏览器中的行为。

只检查项目实际依赖的关键能力，不为了“兼容性”机械加入大范围 polyfill 或无实际消费者的抽象层。

### 5. Pre-bootstrap Runtime Compatibility Guard

Public 与 Admin 都必须提供浏览器兼容检测和低版本提示，但使用不同 Compatibility Policy。

Guard 必须在 Vue App bootstrap 之前运行，不能依赖 Vue 或 Element Plus 才显示，否则真正不兼容的浏览器可能在提示代码执行前就白屏。

设计原则：

```text
Pre-bootstrap Guard
→ Critical Feature Detection
→ Browser / Version Advisory Detection
→ Compatibility Result
→ Supported / Advisory / Unsupported
```

判断以 Feature Detection 为主、浏览器版本判断为辅：

- Critical Feature PASS + 已知版本满足最低标准：正常启动；
- Critical Feature PASS，但浏览器未知或版本略低：允许继续并按策略给出轻量提示；
- Critical Feature FAIL：显示强兼容提示，必要时不启动 Vue App。

传统 User-Agent / Client Hints 只用于辅助识别、提示文案和诊断，不作为唯一能力判定依据。Guard 自身必须使用保守语法和简单 DOM/CSS，不使用可能导致旧浏览器解析失败的新语法。

### 6. Warning UX

至少提供两级提示：

#### Soft Warning

浏览器版本低于推荐线但关键能力仍可运行时：提示“当前浏览器版本较低，部分页面效果或功能可能存在差异，建议升级浏览器”，允许继续访问。

#### Hard Warning

关键运行能力缺失时：

- Public：优先保留仍可安全提供的基础内容访问；无法保证运行时给出明确升级提示；
- Admin：可以更严格，不满足最低运行能力时阻止进入管理功能并提示升级。

### 7. IE11 Boundary & Future Contingency

当前明确：

- EU-31 不要求实现 IE11；
- IE11 当前对 Public 和 Admin 都属于 Unsupported；
- 如果未来系统上线后真实用户环境或客户明确要求 IE11，只针对 Public Site 重新启动新的兼容性 Architecture / Requirement / Technical Plan；Admin 即使届时也不要求 IE11。

未来 Public IE11 的候选路径可以包括但不限于：

- 降低 Public 前端框架版本，例如评估 Vue 2 及匹配依赖；
- 建立独立 Legacy Public Frontend，与现代 Vue 3 Public Site 共用 Backend / CMS API；
- 为公开内容提供 SSR / server-rendered / static HTML fallback，以 Progressive Enhancement 保证基本内容访问；
- 仅在事实证明可行时采用 legacy bundle + targeted polyfill；不得预设该方案能突破 Vue 3 本身的 Runtime 边界；
- 其他届时经验证可满足 IE11 的有效技术方案。

该备用路径当前只作为已识别的 Contingency，不属于 EU-31 未完成项，也不提前引入 Legacy 依赖。

### 8. Verification Runtime Strategy

完整浏览器兼容矩阵不进入每次 PR / push 的默认 CI。EU-31 应把验证拆成两层。

#### Default CI — 日常轻量守护

默认 CI 保留：

- Public / Admin `vue-tsc + Vite build`；
- 显式兼容 target 下的生产构建；
- Compatibility Guard 的低成本自动测试；
- Chromium 功能 E2E；
- Backend / Integration 回归。

Default CI 要证明“代码仍然符合兼容构建 Contract 和正常功能回归”，但不每次重复执行完整跨浏览器与最低版本矩阵。

#### Manual Browser Compatibility Workflow — 专项验证

新增独立 Workflow，建议命名：

```text
Browser Compatibility Verification
```

初始只配置 `workflow_dispatch`，不配置 `push`、`pull_request` 或定时 `schedule`。完整兼容验证只在明确需要时人工执行。

建议提供：

```text
scope = public | admin | all
profile = standard | minimum | full
```

- `standard`：当前主流浏览器跨浏览器回归；
- `minimum`：重点验证正式最低版本 Browser Contract；
- `full`：标准浏览器 + 最低版本 + Mobile + Layout / Screenshot Evidence。

专项流程至少承担：

- Public cross-browser matrix；
- Public minimum-version matrix；
- Public Mobile / layout verification；
- Admin cross-browser matrix；
- Admin minimum-version matrix；
- Compatibility Guard runtime behavior；
- screenshot / trace / report artifacts。

### 9. When Compatibility Workflow Is Required

不采用“每个 PR 都运行”或机械固定周期。以下场景应明确执行专项 Workflow：

- EU-31 首次建立 Browser Contract；
- Vue、Vite、Vue Router、Element Plus 等关键前端依赖升级；
- `build.target`、`cssTarget`、polyfill、Compatibility Guard 修改；
- 大范围 CSS / Layout / Responsive 重构；
- 引入新的 Web API 或较新的 CSS 特性；
- 浏览器最低支持版本调整；
- EU-30 轮播重构后，作为首次最终兼容基线的一部分；
- 正式发布前需要完整兼容复核时；
- Human Review 或生产反馈暴露疑似浏览器特有问题时。

普通文案、Backend、CMS 数据或不影响相关 Compatibility Claim 的小范围修改，不要求自动执行完整兼容矩阵。

### 10. Cross-browser Test Focus

Public 的专项验证强度高于 Admin。

Public 至少覆盖：Main 首页、栏目、文章、固定页、Page Group / Tab、业务指南、Party 首页、Party 栏目、Party 文章、外链行为、共享 Navigation/Footer，以及 EU-30 最终轮播实现。

Admin 重点覆盖：Admin Shell、导航、表格、表单、Dialog、Select、上传/图片预览、Tabs、滚动/fixed/sticky 等主要交互和布局。

机器验证重点检查：

- 无 unexpected page error / console error；
- CSS / JS / 图片 / 字体资源加载正常；
- 页面关键区域有非零尺寸；
- 关键内容没有异常 overlap；
- 375 / 390px 等移动宽度无横向溢出；
- Dropdown / Modal / fixed / sticky 不跑出 viewport；
- 图片 natural size 和容器比例合理；
- route change 后不出现空白页。

跨浏览器截图用于发现结构与布局差异，但不做不同浏览器之间的严格 pixel-perfect 比较；字体渲染和抗锯齿差异由 AI/Human Visual Review 判断是否构成真实视觉缺陷。

### 11. Acceptance

EU-31 关闭前至少满足：

- Public 与 Admin 最终 Browser Compatibility Contract 已由实际证据确定并固化；
- 两端构建 target / CSS target 不再依赖可能漂移的隐式默认值；
- Public 与 Admin 关键 JS / Web API / CSS 兼容审计完成；
- Pre-bootstrap Compatibility Guard 与低版本提示实现并验证；
- Default CI 只承担低成本兼容守护，不被完整 Browser Matrix 拖慢；
- 独立 `workflow_dispatch` Browser Compatibility Workflow 可按 scope/profile 执行；
- Public 取得主流浏览器、移动浏览器和最低版本所需证据；
- Admin 取得其较高最低版本对应的主要浏览器证据；
- 代表性 Layout / Screenshot Evidence 完整；
- 当前不支持 IE11 的边界及未来仅 Public 的 Contingency 已明确；
- 所有 Blocking / High Compatibility Finding 关闭后再结束本单元。

### Non-goals

- 当前不实现 IE11；
- 不为了兼容而把 Admin 降到 Public 的最低版本；
- 不把完整兼容矩阵塞入每次 PR / push 默认 CI；
- 不为了“理论兼容”引入没有当前证据支持的 legacy bundle、大型 polyfill 或浏览器抽象框架；
- 不把 WebKit 最新版本证据伪称为某个特定历史 Safari 版本证据；无法取得真实最低版本 Runtime 时必须明确 Evidence Limit。
