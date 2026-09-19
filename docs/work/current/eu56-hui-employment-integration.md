---
id: execution-unit:eu56-hui-employment-integration
type: execution-unit
status: active
readiness: PASS
controlling_issue: 176
base_sha: 5442cf9a4910e54e29f565147c624c3aedb4fe9a
---

# EU-56 慧就业公共网站固定 iframe 集成

## 目标

依据 `docs/requirements/hui-employment-integration.md`，在吉林就业公开站完成九个当前慧就业目标的精确嵌入，替换前期占位实现，并保持 CMS 产品边界不扩大。

## 就绪检查

状态：`PASS`

- Requirement 已明确范围、业务编号、完整 URL、外部系统边界与验收不变量；
- `docs/specifications/public-site.md` 与 `docs/specifications/page-content.md` 已形成可观察行为、失败语义和后台只读边界；
- `docs/technical/public-site-frontend.md` 已明确固定工程常量、renderer identity、CMS 边界与统一 iframe 责任；
- 当前基线为 `5442cf9a4910e54e29f565147c624c3aedb4fe9a`，`main` 无其他 active / Ready Execution Unit；
- GitHub Issue #176 是本单元 controlling Issue；
- 当前变更可以在一个纵向执行单元内完成并独立验证。

## 验收义务与责任映射

| 验收义务 | 实现责任 | 验证责任 |
|---|---|---|
| 首页三个区域加载精确目标 | 慧就业只读常量、首页组合、统一 iframe 组件 | Public build、Browser DOM / URL 断言 |
| 招聘信息五个页面映射一致 | Site Definition renderer identity、Public registry / renderer | Backend Page contract test、Browser route / Tab / iframe 断言 |
| 直播课程“更多”进入本站二级页 | 独立 Page identity、首页内部路由 | Browser 导航与规范 URL 断言 |
| 外部失败不破坏本站框架 | iframe loading / timeout / error / retry 状态隔离 | Browser 失败与重试场景 |
| 不新增 CMS 运营配置 | 固定工程常量、Page `NONE + EXTERNAL`、Admin 只读诊断 | Backend contract test、Admin build / Browser 断言 |
| Site Definition 可重建 | pages definition、manifest integrity、Generic Core external renderer validation | Site Package verification、Backend test |

## 执行方案

1. 建立慧就业固定 URL 与稳定 renderer identity 映射；
2. 实现统一 iframe 组件及状态机；
3. 替换首页就业日历、最新招聘与直播课程占位，并接入直播课程二级页；
4. 将五个招聘信息 Page 和直播课程 Page 投射为显式外部 renderer；
5. 让 Generic CMS 接受显式 `NONE + EXTERNAL` renderer，同时保持非法组合失败关闭；
6. 让 Admin 对非运营外部 profile 提供只读诊断；
7. 补齐 Backend / Frontend / Browser 验证；
8. 收敛 Authority、Evidence 与实施报告，完成集成后归档本文件。

## 完成条件

- 所有当前 Requirement 映射均有实现与自动化证据；
- Backend、Public、Admin 正式 build 与相关测试通过；
- Browser 验证覆盖成功、映射、导航、失败隔离与重试；
- 文档治理校验通过；
- 无已知 Blocking / Medium 缺口；
- 最终报告记录关键技术方案、验证证据与人工后续复核入口，但不把人工复核列为本单元 Gate。

## 实施结果

执行中。
