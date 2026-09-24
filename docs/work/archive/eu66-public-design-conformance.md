---
id: execution-unit:eu66-public-design-conformance
type: execution-unit
status: completed
readiness: PASS
base_sha: b4f776452780c5af4efeb0e04d401cf27ce1dd78
branch: fix/eu66-public-design-conformance
started_at: 2026-09-24
completed_at: 2026-09-24
---

# EU-66 公开站视觉设计一致性修复

## 目标

依据 `docs/design/public-site/DESIGN.md` 当前已经形成的 normative token / component rules，对 Public Site 当前实现执行一次 Design Conformance Review，并只修复无需新增设计决策即可判定的实现偏差。

## 范围

本单元只处理以下已确认偏差：

1. Main page canvas 默认正文色恢复为 `text-primary = #323B47`；
2. Main 轮播 caption 遮罩恢复为 `overlay-dark = rgba(0, 18, 41, 0.44)`；
3. Main 首页日期 / “更多”等已明确属于 metadata 的文本恢复 `13px / 400 / 1.4`；
4. 网站导航分类 Tab / 链接恢复 DESIGN 中 `1.4 / 1.6` 行高；
5. Party 首页 “更多” / 日期 metadata 恢复统一 metadata token；
6. Party 移动端不得覆盖已确定的 24px 大板块标题与 `14px / 20px / 40px` 轮播 caption 规范值。

同时补充与上述规范值直接对应的 Browser CSS assertions。

## 明确不在范围

以下事项仍由 `DESIGN.md` Known Gaps 持有，本单元不裁决、不修改：

- Main 整行大板块标题的 20px vs 24px 与 marker 高度；
- Main 普通资讯列表 14px / 30px 与原站密度差异；
- Main Shell 仍未 token 化的 Header / Banner / dropdown 细节；
- 栏目列表 / 文章详情完整 typography / spacing；
- Party 内部卡片其它 typography / spacing；
- 首页 grid gap、section gap、card padding；
- `#00AEBD` 白底对比度 warning；
- 其他尚未分类的 implementation-only 色值。

## 就绪状态

**PASS**

当前 `main@b4f776452780c5af4efeb0e04d401cf27ce1dd78` 与 `origin/main` 一致，工作树干净，没有 Open PR。Design Authority、Technical 与 Verification 已明确规定：实现与 normative token 冲突时先分类；本单元中的偏差均可在不改变 Product Goal、Scope、用户行为或新增长期设计事实的前提下作为 implementation defect 修复。

## 实施结果

- Main page canvas 默认文字已由 `#212529` 恢复到 `text-primary = #323B47`；
- Main 轮播 caption 遮罩已由 `rgba(0,18,41,.8)` 恢复到 `overlay-dark = rgba(0,18,41,.44)`；
- Main 首页 panel “更多”、日期以及直播课程“更多”已统一到 metadata `13px / 1.4`，并移除 `<=760px` 下把直播课程“更多”降为 12px 的响应式覆盖；
- 网站导航分类 Tab / 链接已分别补齐 `1.4 / 1.6` 行高；
- Party 首页“更多”和 article-list 日期已统一到 `text-muted = #8491A1`、`13px / 1.4`；
- Party `<=600px` 不再把大板块标题降到 21px，也不再把轮播 caption 降到 13px / 36px；移动端继续消费既有 24px 标题与 `14px / 20px / 40px` caption token；
- Main / Party 视觉 E2E 已新增与上述规范值对应的 computed-style assertions；
- `DESIGN.md`、Specification、Technical Authority 本身没有语义变更；Known Gaps 保持原样。

## 当前本地证据

- Public source-boundary：PASS；
- `vue-tsc --noEmit` + Vite production build：PASS；
- Google `DESIGN.md` lint：0 errors / 1 原有 contrast warning；
- Current 文档治理：PASS；
- `git diff --check`：PASS；
- Browser assertions 已写入现有 visual-fidelity E2E；项目同版本 Playwright Chromium 的 targeted computed-style 验证已覆盖本单元规范值并通过。
- 使用项目同版本 Playwright Chromium 直接加载当前 Main / Party CSS 的 desktop(1440×1000) / mobile(390×844) computed-style 验证：PASS；已实际验证 Main page canvas、Main caption overlay、Main metadata、网站导航 line-height、Party metadata、Party 大标题和 Party caption 全部命中当前 normative token；
- 该 computed-style 证据证明 CSS cascade / media-query 投影正确，但不替代需要 Backend / Site bootstrap 的真实 Public Runtime E2E。

## 集成与收敛结果

- 项目负责人明确取消本单元的 PR 路径，并授权直接在本地仓库完成集成后尽快收敛；该决定只改变 Repository Integration 路径，不改变本单元范围或视觉规范验收值；
- `main` 从 `b4f776452780c5af4efeb0e04d401cf27ce1dd78` 以 `--ff-only` 方式直接前进到实现 Head `a52f35bb15270881e84970061a1ebc30ebd0c9c6`，未产生额外 merge commit；
- 集成后的同一实现 Head 重新执行 Public `npm run build`，其中 source-boundary、`vue-tsc --noEmit` 与 Vite production build 均 PASS；Current 文档治理与 `git diff --check origin/main..HEAD` 均 PASS；
- 由于集成路径被人工权威改为本地直接合并，本单元不再创建 PR，也不声称取得 PR `full-ci` 或完整 Public Runtime E2E 证据；完成声明仅覆盖下方验收项以及已记录的当前本地 Browser / build / governance 证据；
- 既有 DESIGN Known Gaps 保持未裁决状态，不因本单元完成而改变。

## 验收

1. 上述 6 组偏差全部与当前 Design Authority 一致；
2. Existing Known Gaps 不因本单元被修改或自动提升为 token；
3. Public frontend source-boundary / type-check / build 通过；
4. targeted visual Browser assertions 能验证实际 computed style；
5. Google DESIGN lint 继续为 0 error；现有 contrast warning 不因本单元变化；
6. 文档治理和 `git diff --check` 通过；
7. 无 Backend / Admin / Domain / Requirement / Specification 行为变更。
