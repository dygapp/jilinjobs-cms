---
id: execution-unit:eu60-link-open-mode-convergence
type: execution-unit
status: completed
readiness: PASS
controlling_issue: 188
base_sha: c40e384ce9cd4647e4a1cc5f539e1bccfadddaee
verified_head_sha: c7b2db7a265aa92f7b976204f78f1c1ce4567cc6
integrated_sha: c9928b04832f7ef0c452d61ce3f6c7f95a218a0e
completed_at: 2026-09-20
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
- 文档先行提交：`c40e384ce9cd4647e4a1cc5f539e1bccfadddaee`。

## 完成范围

- 新增 V5 追加式 Flyway migration，兼容迁移既有 `open_mode`；
- Navigation / CmsListItem / Advertisement Backend model、validation、HTTP projection 收敛为 nullable HTML target 语义；
- Site Package navigation schema、provisioning、JilinJobs Site Definition 与 bootstrap 默认数据同步；
- Admin Navigation / List / Advertisement 表单与 API adapter 同步；
- Public Navigation、Main / Party CmsList 与 Advertisement renderer 删除 URL 类型推断；
- 历史 canonical migration 数据保持来源词汇，在 migration write boundary 转换到当前 Runtime contract；
- 相关 Backend、Site Package、Admin / Public Browser tests 与真实 MySQL V4 → V5 迁移验证补齐。

## 最终语义

- 空值：不输出 HTML `target`；
- `_self`：当前浏览上下文；
- `_blank`：新浏览上下文，并配套安全 `rel`；
- Advertisement 的 `NO_LINK`：保留 URL 但不生成可点击链接；
- Runtime 不再保留“HTTP(S) 外链默认新窗口”的隐式推断；
- CmsList ARTICLE placement 的打开方式由 placement 自身持有，不从关联 Article URL 类型推断；
- 无独立 open-mode 的 Article `EXTERNAL_LINK` 入口继续遵循其既有独立新窗口规则。

## 兼容迁移结果

V5 migration 保持迁移前可观察行为：

- `NEW_WINDOW -> _blank`；
- `SAME_WINDOW -> _self`；
- 旧 `DEFAULT` 若旧 Runtime 会形成 HTTP(S) 外部 target，则迁移为 `_blank`；
- 其余旧 `DEFAULT` 迁移为 `NULL`；
- CmsList ARTICLE placement 结合关联 Article 的当前外链 target 判定历史 `DEFAULT`；
- Advertisement `NO_LINK` 保持不变；
- 三张 current table 的 `open_mode` 最终允许 `NULL` 且默认值为 `NULL`；
- 迁移后 current tables 不再保留 `DEFAULT / SAME_WINDOW / NEW_WINDOW`。

## 验收结果

1. 新写入只接受空值、`_self`、`_blank`；Advertisement 额外接受 `NO_LINK`。
2. PublicNavigation wire 不再暴露 `newWindow`，而直接暴露 nullable `openMode`。
3. Main / Party 不再存在 `DEFAULT + HTTP(S) => _blank` 推导。
4. 招聘信息五个入口与“直播课程”在 Site Definition 中显式使用 `_blank`。
5. 既有外部 bootstrap 链接迁移后保持新窗口行为。
6. Advertisement `NO_LINK` 保留 URL 且不生成可点击链接。
7. Article `EXTERNAL_LINK` 既有独立新窗口行为未退化。
8. 数据库 fresh schema、V4 → V5 migration、Backend、Admin、Public、Site Package 与完整 CI 均通过 PR Head 与实际 integrated-main 验证。

## 验证证据

完成态 PR Head：`c7b2db7a265aa92f7b976204f78f1c1ce4567cc6`。

实际集成提交：`c9928b04832f7ef0c452d61ce3f6c7f95a218a0e`。

| 验证范围 | 当前证据 | 结果 |
| --- | --- | --- |
| PR 文档治理 | Run `35494257967` | PASS |
| PR 快速 CI | Run `35494257970` | PASS |
| PR 完整 CI | Run `35495248281` | PASS |
| PR V4 → V5 真实 MySQL migration | Run `35495248281` / Backend verify | PASS |
| PR Integrated browser verification | Run `35495248281` | PASS |
| 集成后 main 文档治理 | Run `35495900416` | PASS |
| 集成后 main 完整 CI | Run `35495900461` | PASS |
| 集成后 main V4 → V5 migration | Run `35495900461` / Backend verify | PASS |
| 集成后 main Integrated browser verification | Run `35495900461` | PASS |
| 集成后 Site Package | Run `35495900425` | PASS |
| 集成后 Generic Content Migration | Run `35495900511` | PASS |
| 集成后 Party Migration | Run `35495900424` | PASS |
| 集成后 Backend Boundary | Run `35495900418` | PASS |
| integrated-main exact-SHA Rule Discovery | Run `35495915893` | PASS |

## 集成与闭环

- Controlling Issue：Issue #188，已关闭；
- 实现 PR：PR #189，已 squash 合并；
- 完成态 PR Head：`c7b2db7a265aa92f7b976204f78f1c1ce4567cc6`；
- 实际集成提交：`c9928b04832f7ef0c452d61ce3f6c7f95a218a0e`；
- 集成后 main 的完整 CI、真实数据库迁移验证与浏览器回归均已通过；
- 本归档只关闭 Work lifecycle，不再改变产品行为、Requirement、Specification、Runtime 或测试。

本次未执行 Production Deployment / Release。
