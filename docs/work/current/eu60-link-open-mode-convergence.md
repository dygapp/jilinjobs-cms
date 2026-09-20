---
id: execution-unit:eu60-link-open-mode-convergence
type: execution-unit
status: active
readiness: PASS
controlling_issue: 188
base_sha: c40e384ce9cd4647e4a1cc5f539e1bccfadddaee
---

# EU-60 链接打开方式语义收敛

## 目标

将 NavigationItem、CmsListItem 与 Advertisement 的 `openMode` 从 `DEFAULT / SAME_WINDOW / NEW_WINDOW` 私有字典收敛为直接表达浏览上下文的空值、`_self`、`_blank`；Advertisement 继续使用 `NO_LINK` 表达保留 URL 但禁止点击。Public Runtime 不再依据 URL 类型推断打开方式。

## 权威输入

- Issue #188；
- `docs/requirements/cms-domain.md` V2.1；
- `docs/specifications/admin-site.md` V3.2；
- `docs/specifications/public-site.md` V3.4；
- `docs/technical/http-interface-contract.md`；
- `docs/technical/public-site-frontend.md`；
- 文档先行 commit：`c40e384ce9cd4647e4a1cc5f539e1bccfadddaee`。

## 范围

- 新增追加式 Flyway migration，兼容迁移既有 `open_mode`；
- Navigation / CmsListItem / Advertisement Backend model、validation、HTTP projection；
- Site Package navigation schema、provisioning 与 JilinJobs Site Definition；
- JilinJobs bootstrap 默认数据；
- Admin Navigation / List / Advertisement 表单与 API adapter；
- Public Navigation、Main / Party CmsList 与 Advertisement renderer；
- 相关 Backend、Site Package、Admin / Public Browser tests。

## 迁移不变量

- `NEW_WINDOW -> _blank`；
- `SAME_WINDOW -> _self`；
- 旧 `DEFAULT` 若按旧 Runtime 会形成 HTTP(S) 外部 target，则迁移为 `_blank`；
- 其余旧 `DEFAULT` 迁移为 `NULL`；
- CmsList ARTICLE placement 必须结合关联 Article 的外链 target 保持旧行为；
- Advertisement `NO_LINK` 保持不变；
- 迁移完成后 Runtime 不再保留“外链默认新窗口”的兼容推导。

## 不包含

- 不改变 Article `EXTERNAL_LINK` 固定新窗口规则；
- 不新增 `clickable` 字段；
- 不把固定工程外链改造成 CMS 数据；
- 不改变 Navigation targetType、CmsList source identity、Advertisement URL 生命周期；
- 不执行 Production Deployment / Release。

## 验收义务

1. 新写入只接受空值、`_self`、`_blank`；Advertisement 额外接受 `NO_LINK`。
2. PublicNavigation wire 不再暴露 `newWindow`，而直接暴露 nullable `openMode`。
3. Main / Party 不再存在 `DEFAULT + HTTP(S) => _blank` 推导。
4. Site Definition 中需要新窗口的招聘信息五个入口与“直播课程”仍显式使用 `_blank`。
5. 现有外部 bootstrap 链接迁移后仍保持新窗口行为。
6. Advertisement `NO_LINK` 继续保留 URL 且不生成可点击链接。
7. Article `EXTERNAL_LINK` 既有新窗口行为不退化。
8. 数据库 fresh schema + V5 migration、Backend、Admin、Public、Site Package 与完整 CI 均通过当前 Head 验证。

## 就绪状态

**PASS**

当前 Authority、实现边界、迁移策略与验证责任已经明确；该变更必须作为单一原子 Execution Unit 完成，以避免 Backend wire 与 Frontend consumer 产生中间不兼容状态。
