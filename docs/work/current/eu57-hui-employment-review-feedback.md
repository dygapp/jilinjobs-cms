---
id: execution-unit:eu57-hui-employment-review-feedback
type: execution-unit
status: active
readiness: PASS
controlling_issue: 179
base_sha: eab13abf7fe9f9c49ea1bc1bed45dda3cfaac48f
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
