---
id: execution-unit:eu57-hui-employment-review-feedback
type: execution-unit
status: completed
readiness: PASS
controlling_issue: 179
base_sha: eab13abf7fe9f9c49ea1bc1bed45dda3cfaac48f
verified_head_sha: 41d6b31844f68edc91533e7e37806a01aa49edc0
integrated_sha: 5092dd44566c611b110acf5ebc7707540c2d32b9
completed_at: 2026-09-20
---

# EU-57 慧就业集成人工评审反馈收敛

## 目标

收敛慧就业集成首轮人工评审确认的导航交互、二级 iframe 滚动和首页视觉问题，不改变慧就业业务映射，不扩大 CMS 产品边界。

## 范围

本单元包括：

- 页面切换后隐藏当前导航下拉菜单，移动端同时收起主导航；
- 招聘信息五个导航入口与“直播课程”导航入口使用安全的新窗口行为；
- 招聘信息二级页面使用 `1300px`、直播课程二级页面使用 `1250px` 的无内部滚动承载基线；
- 首页直播课程移除重复宿主标题，同时保持“更多”进入 `/page/live-course`；
- 招聘公告与其他首页资讯区统一标题、列表正文、日期和交互色彩；
- 同步公开站 Specification、Technical contract、Site Definition 与当前验证。

不包括：

- 修改慧就业页面内部 DOM、样式或业务行为；
- 新增 iframe 动态跨源测高协议；
- 新增 CMS 配置、数据库 Schema 或后台运营能力；
- 部署生产环境。

## 验收与验证映射

| 验收义务 | 实现责任 | 最低验证责任 |
| --- | --- | --- |
| 导航切换后下拉隐藏 | `PublicNavigation.vue`、共享导航样式 | 浏览器验证桌面下拉与移动导航收起 |
| 慧就业导航入口新窗口 | Site Definition `openMode`、RouterLink 投影 | Site Package 校验与浏览器属性 / popup 行为验证 |
| 二级 iframe 无内部滚动 | 慧就业目标高度、统一 iframe 组件 | 浏览器断言 `scrolling="no"` 与承载高度 |
| 首页直播课程无双标题且“更多”目标不变 | `PublicHomeView.vue`、首页样式 | 浏览器 DOM、路由与视觉观察 |
| 首页资讯文字体系一致 | 首页共享资讯样式 | 计算样式断言与视觉观察 |
| 当前权威、实现与验证一致 | Specification、Technical、测试 | 文档治理、正式构建、定向与整体相关回归 |

## 就绪检查

`PASS`。

理由：

- 用户反馈已经明确改变的可观察行为，不存在需要再次升级的人工作业；
- `docs/requirements/hui-employment-integration.md` 继续完整拥有业务映射，当前修订只进入 `docs/specifications/public-site.md` 与 `docs/technical/public-site-frontend.md` 的既有责任；
- 当前 `main`、`docs/work/current/README.md`、Open PR / Branch 状态一致，没有冲突的 active Unit；
- 所有验收义务均能映射到现有共享 Navigation、慧就业 iframe 组件、Site Definition 与现有 Playwright 验证；
- 技术实现属于现有 seam 内的低影响可逆修正，不需要独立跨单元 Technical Plan。

## 完成条件

- 上述验收义务全部具备当前代码与验证证据；
- `git diff --check`、Public 正式 build、相关浏览器回归与必要 Site Package 校验通过；
- 视觉观察确认首页直播课程只保留单一标题层，招聘公告与其形成一致的并排模块；
- PR 当前 Head 的自动化证据满足 Repository 集成要求；
- 集成后完成 Post-Integration 验证与 Work lifecycle closure。

## 完成结果

- 共享导航在目标选择后立即隐藏当前下拉菜单，移动端同时关闭主导航；桌面 hover / focus 能在指针离开后恢复正常行为；
- 招聘信息五个入口与直播课程入口通过 Site Definition `NEW_WINDOW` 和 RouterLink 属性投影使用安全新窗口；
- 招聘信息二级页以 `1300px`、直播课程二级页以 `1250px` 承载 iframe，并统一声明 `scrolling="no"`，由宿主页面承担纵向滚动；
- 首页移除直播课程外层重复标题，保留本站“更多”路由覆盖；右侧招聘公告与首页其他资讯区统一标题、正文、日期和 hover 色彩；
- Specification、Technical contract、Site Definition、清单摘要与 Playwright 回归同步完成，没有改变慧就业业务 URL 映射、CMS Schema 或后台运营边界。

## 验收与验证证据

完成态 PR Head：`41d6b31844f68edc91533e7e37806a01aa49edc0`。

| 验收范围 | 当前证据 | 结果 |
| --- | --- | --- |
| Backend、Public、Admin、完整集成浏览器回归 | [CI Run 1211](https://github.com/dygapp/jilinjobs-cms/actions/runs/35483493042) | PASS |
| Site Package Definition 与摘要 | [Site Package Verification Run 153](https://github.com/dygapp/jilinjobs-cms/actions/runs/35483493041) | PASS |
| 当前文档治理 | [文档治理 Run 165](https://github.com/dygapp/jilinjobs-cms/actions/runs/35483493046) | PASS |
| 首页完整截图与 AI 视觉观察 | CI Run 1211 的 `playwright-evidence` artifact `10596787252` | PASS |
| Review Runtime 构建、浏览器回归、外部 HTTP 与归属校验 | [人工评审环境 Run 881](https://github.com/dygapp/jilinjobs-cms/actions/runs/35482992894)，产品实现 Head `8dc0f5257d837f3e1fbae09d7ddd90b5c97c172a` | PASS |
| 集成后 `main` 文档治理、Site Package、Backend、Public、Admin 与浏览器回归 | [CI Run 1212 attempt 2](https://github.com/dygapp/jilinjobs-cms/actions/runs/35483900154)、[Site Package Verification Run 154](https://github.com/dygapp/jilinjobs-cms/actions/runs/35483900109)、[文档治理 Run 167](https://github.com/dygapp/jilinjobs-cms/actions/runs/35483900182) | PASS |

首次集成后浏览器 Job 在拉取 Playwright 镜像时因 MCR 连接超时失败，产品验证步骤尚未启动；仅重跑该失败 Job 后，Run 1212 attempt 2 的镜像拉取与完整浏览器回归成功。

首页截图确认直播课程只保留嵌入内容自身标题，左右模块顶部和高度对齐，招聘公告使用一致的青色标题、边框与协调的列表文字 / 日期样式。导航收起、新窗口属性与 popup、移动菜单关闭、iframe 无内部滚动及承载高度均由当前浏览器断言覆盖。

## 集成与闭环

- Controlling Issue：[Issue #179](https://github.com/dygapp/jilinjobs-cms/issues/179)，已由合并提交关闭；
- 实现 PR：[PR #180](https://github.com/dygapp/jilinjobs-cms/pull/180)，已 squash 合并；
- 实际集成提交：[`5092dd44566c611b110acf5ebc7707540c2d32b9`](https://github.com/dygapp/jilinjobs-cms/commit/5092dd44566c611b110acf5ebc7707540c2d32b9)；
- 完成态 PR Head 与实际集成提交的 tree 均为 `4c40c1c121a66ea4197b64a723b1c395f7841e95`；
- 集成后的 `main` 已完成 Post-Integration verification，本文件退出 Current lifecycle 并进入 archive。

本次未执行生产部署。外部慧就业页面未来内容高度或响应策略变化仍需通过新的反馈批次调整宿主承载基线。
