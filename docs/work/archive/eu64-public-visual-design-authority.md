---
id: execution-unit:eu64-public-visual-design-authority
type: execution-unit
status: completed
readiness: PASS
base_sha: a8f96a949973f8d6592180464f5d72c52e8174be
branch: docs/eu64-public-design-authority
started_at: 2026-09-23
completed_at: 2026-09-24
---

# EU-64 公开站视觉设计权威建立

## 目标

为公开站建立独立、长期、可由 Fresh Context 恢复的视觉设计 Authority，使产品行为规格与视觉设计规格职责分离，并使后续页面实现、视觉修复和人工评审不再依赖现有 CSS 反向推断设计意图。

## 权威输入

- `docs/requirements/information-publishing.md`：公开站产品范围、响应式和视觉验收的上层要求；
- `docs/specifications/public-site.md`：Main / Party 页面结构、用户可观察行为和视觉结构义务；
- `docs/technical/public-site-frontend.md`：公开前端长期实现契约；
- `sites/jilinjobs/**`：稳定 Site Definition、视觉资产与 provenance；
- 当前公开站实现：只作为 implementation evidence，不自动建立设计事实；
- 原吉林就业网站公开页面与 CSS：只作为 legacy visual evidence，只有经本单元显式 promotion 的值进入当前 Design Authority；
- Google `DESIGN.md` format specification：用于机器可读 token 与 Markdown 设计说明的文件格式和 lint contract。

## 规格结论

1. `frontend/public-site/DESIGN.md` 成为公开站当前视觉设计 Authority，与 `docs/specifications/public-site.md` 并行承担不同语义责任。
2. `public-site.md` 继续拥有页面、区域、交互、失败行为和具有产品意义的结构约束；`DESIGN.md` 拥有颜色、字体、尺寸、间距、形状、视觉组件与 Main / Party visual language。
3. 默认设计原则是保持原吉林就业网站已经确认的视觉层级、密度、比例、配色、字体关系和 Main / Party 品牌差异；当前 Requirement / Specification 已明确改变的响应式、可访问性、iframe 滚动等行为优先。
4. YAML token 只记录已经得到当前 Authority、原站证据与/或已接受人工复评支持的规范值；仅存在于当前 CSS 的值不得自动提升。
5. 尚未收敛的视觉差异进入 `DESIGN.md` 的“已知视觉缺口”，不作为 normative token。
6. 公开站视觉设计文件使用 Google `DESIGN.md` 当前格式，并由固定版本 CLI 在文档治理 Workflow 中执行 lint。

## 范围

- 新增 `frontend/public-site/DESIGN.md`；
- 调整 Documentation Authority Map；
- 调整 `public-site.md`，把精确视觉 token 的 ownership 转交给 Design Authority，同时保留产品结构 / 行为义务；
- 调整公开前端 Technical 与 Verification Strategy；
- 文档治理纳入 `DESIGN.md` 路径、中文主叙述规则的格式例外和官方 DESIGN lint；
- 记录原站视觉来源与 digest；
- 形成后续视觉修复可直接消费的 Known Gaps。

## 不包含

- 不修改 Vue / CSS 页面实现；
- 不在本单元解决 Known Gaps；
- 不把原站固定最小宽度、旧式交互或已经被当前 Specification supersede 的行为恢复回来；
- 不为 Admin 建立设计系统；
- 不引入运行时设计 token 库、CSS variables 重构或 Tailwind 等新实现依赖。

## 就绪状态

**PASS**

- 用户已明确授权按该方案推进；
- 当前 Requirement Baseline 与 Public Specification 足以界定 Visual Design Authority 的语义边界；
- `main@a8f96a949973f8d6592180464f5d72c52e8174be` 与最新 `origin/main` 一致，工作树干净；
- Current Work 在本单元建立前为 `NONE`；
- Google `DESIGN.md` 官方规范和 CLI 当前能力已重新核验；
- 工作可由一个文档 / 验证纵向单元闭环，不需要跨 Execution Unit Technical Plan。

## 验收义务

1. DESIGN 文件符合官方格式，YAML token 可被官方 linter 解析且无 error。
2. 设计 Authority 与 Requirement / Specification / Technical / Code / legacy evidence 的职责边界明确，不形成双重 owner。
3. 当前已确认首页小板块 32px / 17px / 3px、网站导航 16px / 15px、Main / Party 主题与轮播比例等视觉事实被迁移到正确视觉 owner。
4. 原站复刻原则明确包含“视觉忠实但不复制已被当前 Authority 否定的旧实现”。
5. 当前无法由 Authority 唯一确定的样式不进入 normative token，并形成可追踪 Known Gaps。
6. `docs/README.md` 可以从 Fresh Context 定位 Public Visual Design Authority。
7. 文档治理对 `frontend/public-site/DESIGN.md` 的变更自动触发，并执行官方 DESIGN lint。
8. 本单元不产生 Public UI runtime 行为变化。


## 实现结果

- 已创建 `frontend/public-site/DESIGN.md`，采用 Google `DESIGN.md` 的 YAML token + Markdown 设计说明结构；
- 已建立 Main / Party 颜色、已确认 typography、关键尺寸、形状和组件视觉 token；
- 已固化“视觉忠实原站，但当前 Requirement / Specification 明确修订优先”的复刻原则；
- 已记录 2026-09-23 重新核验的原站 CSS 来源与 SHA-256；
- 已把仍存在歧义的 Main 大板块标题、普通资讯密度、Shell 细节、栏目 / 文章页、Party 局部细节、首页节奏与青绿色标题对比度列入 Known Gaps；
- 已将 `docs/specifications/public-site.md` 中精确视觉值的 ownership 收敛到 Design Authority，同时保留页面结构、交互和用户可观察验收责任；
- 已同步 Documentation IA、Public Technical、Verification Strategy 与文档治理 Workflow；
- 本单元没有修改 `frontend/public-site/src/**`，未改变当前页面 Runtime 行为。

## 当前验证证据

| 验证 | 结果 |
| --- | --- |
| `npx --yes @google/design.md@0.4.0 lint frontend/public-site/DESIGN.md` | PASS：0 errors；1 warning 已进入 Known Gap |
| `node scripts/verify-docs-governance.mjs` | PASS：Current 文档语言与本地引用完整性基线通过 |
| `git diff --check` | PASS |
| `npx --yes js-yaml@4.1.0 .github/workflows/docs-governance.yml` | PASS：Workflow YAML 可解析 |
| Public production source guard | PASS：无 `frontend/public-site/src/**` 变更 |
| Current ownership 搜索 | PASS：当前非归档文档不再平行持有 32/17/3、16/15、8:5、585:329 等精确视觉值 |

官方 DESIGN lint 的唯一 warning 是 `#00AEBD` 青绿色文字在白底上的 2.70:1 对比度提示。该组合同时有原站证据和已接受首页视觉基线，本单元不以静态 warning 为由自行改变视觉要求；已作为 Known Gap 保存，等待后续视觉 / 可访问性定向复核。

## 收敛状态

最终 Blocking：**0**，Medium：**0**。本单元只建立 Design Authority，因此不以本轮证据声明现有网站全部视觉缺口已修复；这些视觉缺口继续由 `frontend/public-site/DESIGN.md` 的 Known Gaps 持有。

PR exact-head、Integration 与 Post-Integration verification 已完成，本单元满足 closure 条件。


## 远端集成与 Post-Integration 证据

2026-09-24 最终 GitHub-native 证据：

- PR #203 — `docs(public): 建立公开站视觉设计权威` 最终候选 Head：`068472e666ddab821c823492e1d30034e8d886f6`；
- PR exact-head 文档治理检查 Run `35940738760`：`completed/success`；
- PR exact-head 快速 CI Run `35940738735`：`completed/success`；
- PR #203 已合并，Merge Commit：`cd7e0f694f749107ab4c2e2cee273ee0d7806dbc`；
- Merge Commit 对应 main push 文档治理检查 Run `35940935346`：`completed/success`；
- Merge Commit 对应 main push 完整 CI Run `35940935351`：`completed/success`，其中 Backend verify、Admin frontend verify、Public site frontend verify、Integrated browser verification 与 verified review runtime 发布均成功；
- `origin/main` 已重新读取确认指向上述 Merge Commit；
- 执行期出现过 GitHub TLS / SSH 传输 incident，但恢复后所有写操作均重新读取并验证真实远端状态，因此该 incident 不构成最终未验证项。

## 完成条件

已完成 scoped lint、文档治理检查、设计文件官方 lint、最终 diff / ownership 复核、PR exact-head 验证、Integration 与 Post-Integration verification。本单元进入 completed / historical lifecycle；未执行 Production Deployment。
