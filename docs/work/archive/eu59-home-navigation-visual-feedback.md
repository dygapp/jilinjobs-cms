---
id: execution-unit:eu59-home-navigation-visual-feedback
type: execution-unit
status: completed
readiness: PASS
controlling_issue: 185
base_sha: dd08954b9ece718dbe7afdd3454e55fd3066524c
verified_head_sha: 46ed902212f4712f8d655ad6957fe015a65f795c
integrated_sha: 0cf177fc186125aef532f485f1d028e279521ff0
completed_at: 2026-09-20
---

# EU-59 首页栏目标签与导航字号反馈修复

## 目标

在不改变慧就业业务映射、CMS Domain 或导航信息架构的前提下，收敛 Main 首页本地栏目标题与“直播课程”相邻区域的视觉差异，并提高首页“网站导航”模块内容的可读性。

## 范围

- 首页本地栏目标题改为上沿强调色条的 Tab-like 基线，取消文字下沿强调线；
- 通知公告、就业动态、快速导航、招聘公告、最新招聘、网站导航等本站标题使用一致的标题视觉；
- 首页“网站导航”模块 Tab 字号调整为 16px，链接正文调整为 15px；
- 移动端继续使用模块自身横向 Tab 滚动与单列链接布局，避免页面级横向溢出；
- 同步 Public Specification 与 Playwright 视觉回归断言；
- 回归确认 Issue #179 的 1～4 项既有修复不退化。

## 不包含

- 不修改慧就业 iframe 内部 DOM / CSS；
- 不修改慧就业 URL、业务编号、renderer identity 或 Page lifecycle；
- 不修改顶部公共主导航的信息架构、栏目名称、字号或 open-mode；
- 不修改 Backend、Admin、数据库或 Site Definition。

## 就绪状态

**PASS**

- 用户反馈明确，Goal / Scope / Observable Behavior 无关键歧义；
- 当前 Requirement / Specification / Technical Authority 足以支撑本次局部、可逆的 Public UI 变更；
- `main@dd08954b9ece718dbe7afdd3454e55fd3066524c` 的 Current Work 为 `NONE`，开始本单元前无 Open PR；
- 当前变更可以在单一 Public execution unit 内完成并以 build + browser regression 收敛。

## 验收结果

1. 首页本站栏目标题统一为上沿 3px 强调色条，不再使用文字下沿强调线；
2. 通知公告、就业动态、快速导航、招聘公告、最新招聘、网站导航等本站标题使用统一的 Tab-like 视觉基线；
3. 首页“网站导航”模块 Tab 为 16px、链接正文为 15px，顶部公共主导航维持既有字号；
4. 移动端“网站导航”继续采用模块内横向 Tab 滚动与单列链接布局，页面无横向溢出；
5. Issue #179 对应的菜单收起、新窗口、iframe 无内部滚动与直播课程单标题行为在最终完整浏览器回归中继续通过；
6. Public / Admin / Backend 构建、Site Package、完整浏览器回归与文档治理均取得 exact-head 与集成后 main 当前证据。

## 实施中的修正

首次完整 CI Run `35489924518` 暴露两项问题：

- 将第 6 条误解释为顶部公共主导航字号，导致 Party 共享导航视觉基线回归；
- 新增“所有根导航文字不得裁切”断言，但测试运行时会临时增加导航数据，该断言不成立。

随后将第 6 条校准为首页底部“网站导航”模块内容字号，恢复顶部公共主导航既有字号，并删除不成立的裁切断言。修正后的 exact Head 为 `46ed902212f4712f8d655ad6957fe015a65f795c`。

## 验证证据

完成态 PR Head：`46ed902212f4712f8d655ad6957fe015a65f795c`。

| 验证范围 | 当前证据 | 结果 |
| --- | --- | --- |
| PR 文档治理 | Run `35490321561` | PASS |
| PR 快速 CI | Run `35490321541` | PASS |
| PR 完整 CI | Run `35490344568` | PASS |
| PR Public / Admin / Backend | Run `35490344568` 对应 jobs | PASS |
| PR Integrated browser verification | Run `35490344568` | PASS |
| 集成后 main 文档治理 | Run `35491016301` | PASS |
| 集成后 main 完整 CI | Run `35491016305` | PASS |
| 集成后 main Integrated browser verification | Run `35491016305` | PASS |

## 集成与闭环

- Controlling Issue：Issue #185，已 completed；
- 实现 PR：PR #186，已 squash 合并；
- 完成态 PR Head：`46ed902212f4712f8d655ad6957fe015a65f795c`；
- 实际集成提交：`0cf177fc186125aef532f485f1d028e279521ff0`；
- 集成后 `main` 文档治理与完整 CI 均已通过；
- 本归档只关闭 Work lifecycle，不再改变产品行为、Specification、Runtime 或测试。

本次未执行 Production Deployment / Release。
