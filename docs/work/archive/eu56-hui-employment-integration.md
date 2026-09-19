---
id: execution-unit:eu56-hui-employment-integration
type: execution-unit
status: completed
readiness: PASS
controlling_issue: 176
base_sha: 5442cf9a4910e54e29f565147c624c3aedb4fe9a
verified_head_sha: f43c5340f01efa5ce81a042475cb85bf3be25e27
integrated_sha: 678e1e7d402140ea5762cb58c856f38d2eab8c60
completed_at: 2026-09-19
---

# EU-56 慧就业公共网站固定 iframe 集成

## 目标与结果

依据 `docs/requirements/hui-employment-integration.md`，已在吉林就业公开站完成九个当前慧就业目标的精确嵌入，替换前期占位实现，并保持 CMS 产品边界不扩大。

完成结果：

- 首页就业日历、最新招聘、直播课程分别加载 Requirement 指定的完整慧就业页面；
- 五个招聘信息二级页面保持 Main Shell、breadcrumb、PageGroup Tab 与对应慧就业业务一致；
- 首页直播课程“更多”进入本站 `/page/live-course`，再嵌入业务分类 `5`、业务编号 `27` 的完整页面；
- 单个外部区域具有加载、20 秒超时、失败提示与原位重试，不影响本站其他内容；
- CMS 未新增地址配置、API、数据库 Schema 或运营内容能力；固定外部 Page 在 Admin 中只读可诊断。

## 关键技术实现方案

### 固定目标与 renderer identity

慧就业完整地址集中在 Public production source 的只读映射中。首页直接消费三个具名目标；六个二级 Page 通过 `HUI_EMPLOYMENT_*` 稳定 renderer identity 选择目标，不从 alias、标题、正文或路由参数推导业务编号。

Requirement 继续是完整 URL 与业务编号的唯一长期事实 owner；Specification 持有用户可观察行为，Technical Design 持有 renderer、iframe 状态和 CMS 边界，代码与测试只做有界投影。

### 统一 iframe 状态机

`HuiEmploymentFrame.vue` 统一承担：

- `IntersectionObserver` 延迟激活与浏览器原生 lazy loading；
- 明确的可访问 title、loading / loaded / failed 状态；
- 20 秒加载超时、浏览器 error 处理与按 attempt remount 的原位重试；
- 首页日历、首页宽区、首页紧凑区和二级页面四类响应式高度；
- 外部失败只替换当前 iframe 区域，Main Header、Navigation、breadcrumb、Tab、Footer 与同页其他内容继续可用。

跨源 iframe 无法读取内部 DOM，因此外层只声明浏览器可观察的 `load` / `error` / timeout 状态，不把 iframe 内部业务成功误报为本站验证结果。

### Page contract 与受控升级

六个既有 Page 使用 `NONE + EXTERNAL + 显式 rendererKey`。Generic CMS Core 允许该组合，但拒绝正文、structured payload 或 `embedUrl` 并行持有固定目标；已知不兼容的 Rich、Internal Static 与 Structured renderer 继续失败关闭。

Site Definition 以六个旧占位状态的精确 `contentAdoptionFromFingerprint` 完成一次受控升级。Fresh provision、旧占位 adoption 与重复 apply 幂等性由 Backend verification 覆盖，未增加数据库迁移。

### Admin 边界

Admin 能识别固定外部 Page 并展示 content owner 与 renderer identity，但不显示地址、占位正文或保存操作，也不允许通过普通 Page 编辑器改写固定集成责任。

## 验收与验证证据

完成态 PR Head：`f43c5340f01efa5ce81a042475cb85bf3be25e27`。

| 验收范围 | 当前证据 | 结果 |
| --- | --- | --- |
| Backend test、bootJar、Site Package foundation / composition / asset projection | [CI Run 1207](https://github.com/dygapp/jilinjobs-cms/actions/runs/35434738958) | PASS |
| Public / Admin 正式构建 | [CI Run 1207](https://github.com/dygapp/jilinjobs-cms/actions/runs/35434738958) | PASS |
| Public 65 个浏览器用例与 Admin 47 个浏览器用例 | [CI Run 1207](https://github.com/dygapp/jilinjobs-cms/actions/runs/35434738958) | PASS |
| Site Package 占位升级与幂等性 | [Site Package Verification Run 149](https://github.com/dygapp/jilinjobs-cms/actions/runs/35434738938) | PASS |
| Generic Content Migration 兼容性 | [Generic Content Migration Run 137](https://github.com/dygapp/jilinjobs-cms/actions/runs/35434738931) | PASS |
| Backend application boundary | [Backend Application Boundary Run 114](https://github.com/dygapp/jilinjobs-cms/actions/runs/35434738979) | PASS |
| Canonical / EU-30 / Rich Text 回归 | [Canonical Run 343](https://github.com/dygapp/jilinjobs-cms/actions/runs/35434738952)、[EU-30 Run 293](https://github.com/dygapp/jilinjobs-cms/actions/runs/35434738934)、[EU-54 Run 60](https://github.com/dygapp/jilinjobs-cms/actions/runs/35434738941) | PASS |
| Current 文档治理 | [文档治理 Run 160](https://github.com/dygapp/jilinjobs-cms/actions/runs/35434738949) | PASS |

本地还通过 Public / Admin `npm run build`、两端 Playwright 用例发现、Site Definition JSON / SHA-256 校验与 `git diff --check`。

独立变更复核发现 1 个中等问题：Public Specification 首页职责清单保留旧称“招聘日历”。该问题已在完成态 Head 修正为“就业日历”，复核后无剩余 blocking / medium finding。

## 集成与闭环

- Controlling Issue：[Issue #176](https://github.com/dygapp/jilinjobs-cms/issues/176)，状态 `completed`；
- 实现 PR：[PR #177](https://github.com/dygapp/jilinjobs-cms/pull/177)，已合并；
- 实际集成提交：[`678e1e7d402140ea5762cb58c856f38d2eab8c60`](https://github.com/dygapp/jilinjobs-cms/commit/678e1e7d402140ea5762cb58c856f38d2eab8c60)；
- 集成提交与完成态 PR Head 的 tree 均为 `592c7bb41590b804bc1362ddbafc3b12169512df`，因此完成态自动化证据覆盖的源码树与 `main` 实际集成树完全一致；
- `main` 已回读指向实际集成提交，Current Work locator 已恢复为 `NONE`，本文件退出 Current lifecycle 并进入 archive。

外部慧就业服务的持续可用性与未来 iframe 响应头变化不由本仓库控制；当前实现以区域级失败状态和重试限制影响面。后续人工可依据本报告和实际运行效果独立复核，但该人工复核不属于 EU-56 完成 Gate。
