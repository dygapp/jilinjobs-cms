---
id: execution-unit:eu59-home-navigation-visual-feedback
type: execution-unit
status: active
readiness: PASS
controlling_issue: 185
base_sha: dd08954b9ece718dbe7afdd3454e55fd3066524c
---

# EU-59 首页栏目标签与导航字号反馈修复

## 目标

在不改变慧就业业务映射、CMS Domain 或导航信息架构的前提下，收敛 Main 首页本地栏目标题与“直播课程”相邻区域的视觉差异，并提高桌面主导航的可读性。

## 范围

- 首页本地栏目标题改为上沿强调色条的 Tab-like 基线，取消文字下沿强调线；
- 通知公告、就业动态、快速导航、招聘公告、最新招聘、网站导航等本站标题使用一致的标题视觉；
- 桌面主导航根项字号调整为 18px，下拉项调整为 17px；
- 移动端维持紧凑字号，避免导航拥挤和横向溢出；
- 同步 Public Specification 与 Playwright 视觉回归断言；
- 回归确认 Issue #179 的 1～4 项既有修复不退化。

## 不包含

- 不修改慧就业 iframe 内部 DOM / CSS；
- 不修改慧就业 URL、业务编号、renderer identity 或 Page lifecycle；
- 不修改导航栏目、路由或 open-mode；
- 不修改 Backend、Admin、数据库或 Site Definition。

## 就绪状态

**PASS**

- 用户已明确指出两项新的可观察视觉问题，且期望方向清晰；
- 当前 Requirement / Specification / Technical Authority 足以支撑本次局部、可逆的 Public UI 变更；
- `main@dd08954b9ece718dbe7afdd3454e55fd3066524c` 的 Current Work 为 `NONE`，开始本单元前无 Open PR；
- 当前变更可以在单一 Public execution unit 内完成并以 build + browser regression 收敛。

## 验收条件

1. 首页本站栏目标题使用上沿强调色条，不再使用文字下沿强调线；
2. 上述首页标题的字体、字号、颜色与基本 Tab-like 结构一致；
3. 桌面主导航根项为 18px，下拉项为 17px，并保持既有 1200px 页面基线无明显横向溢出；
4. 移动端导航维持可用、菜单切换正确且页面无横向溢出；
5. Issue #179 对应的菜单收起、新窗口、iframe 无内部滚动与直播课程单标题行为继续通过；
6. Public build、相关 Playwright 与文档治理取得当前 Head 证据。

## 当前执行状态

实现与验证进行中。
