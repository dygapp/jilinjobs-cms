# 主站外链归属与行为规格说明

## Authority

- GitHub Issue #60；
- `docs/requirements/main-external-link-boundary.md`；
- `docs/specifications/public-site.md`。

## 状态

- 规格：**CURRENT / ACCEPTED**；
- Technical Planning：当前无额外计划需求；
- Current Execution Gate 不由本文维护，统一读取 `docs/work/current/README.md`。

## 1. Surface 矩阵

| Surface | 业务 owner | Public target | 打开行为 owner | 迁移 owner |
|---|---|---|---|---|
| EXTERNAL_LINK Article | Article | `externalUrl` | 当前 Article 公开行为 | Historical Article canonical unit |
| Navigation LINK | NavigationItem | `targetUrl` / projected `href` | Navigation `openMode` | stable structure 时属于 Site Package |
| CmsList LINK | CmsListItem | item URL | ListItem `openMode` | 历史成员属于 Historical Migration |
| CmsList ARTICLE | CmsListItem placement + Article target | Article canonical target | 当前 Article/List projection | 历史 placement + referenced Article |
| Advertisement | Advertisement | advertisement URL | Advertisement `openMode` | 有 source evidence 时才进入后续迁移 |
| fixed integration | engineering Site/Public code | fixed target | engineering contract | 除非重新分类，否则不属于 Historical Migration |

同一 URL 可以因业务 placement 不同而合法出现在多个 surface；URL 相等不是 identity 规则。

## 2. 主站公开行为

### 2.1 Article

- Main Column 与首页 Article aggregation 将 `articleType=EXTERNAL_LINK` 且 `externalUrl` 有效的记录视为外部目标；
- External Article 直接打开来源地址，保持当前新窗口行为；
- INTERNAL Article 使用 `/article/{id}`；
- 不为 External Article 创建站内正文副本。

### 2.2 Navigation

- same-entry internal path 继续使用 Router navigation；
- external URL 与 cross-entry target 使用 document navigation；
- 当前 `newWindow` projection 控制 `_blank`，新窗口 anchor 保持 `noopener noreferrer`；
- `/party/**` 是 cross-entry route，不属于外部业务 URL。

### 2.3 CmsList

- LINK item 使用 item-owned URL / title / openMode；
- ARTICLE item 解析当前 Article target；
- ARTICLE + INTERNAL 使用消费 Site 的 canonical article route；
- ARTICLE + EXTERNAL_LINK 使用 Article external source URL；
- list placement 不转移 Article ownership。

### 2.4 Advertisement / fixed integration

Advertisement 使用既有 URL / openMode projection；固定 NCSS 等已接受 engineering seam 保持固定，除非未来运营维护需求要求重新建立 CMS owner。

## 3. 迁移分类

历史 source discovery 必须按 source business role 分类，而不是按 URL 字符串分类：

1. Column 内容记录 → `EXTERNAL_LINK` Article canonical record；
2. stable list 的历史运营成员 → ListItem canonical record；
3. stable navigation structure → Site Package Navigation；
4. Fresh Site ordinary default → Site Package bootstrap；
5. fixed integration → engineering asset；
6. role 无法确认 → 保留 unresolved evidence，不静默提升。

## 4. Current contract audit

当前 Repository 已具备本规格所需 runtime primitives：

- Public Column / Main home 能直接使用 EXTERNAL_LINK Article target；
- Public Navigation 区分 internal Router navigation 与 external/cross-entry document navigation；
- Main CmsList carousel 与 Advertisement 保持各自 target/open-mode 语义；
- Site Package stable definitions 与 Historical Article/List migration 保持不同 ownership。

因此，仅为了本规格不需要新增 schema、API、Admin form、frontend component 或 migration engine capability。

## 5. 后续变化边界

若未来提出 external badge、leave-site confirmation、health checking、global link object 或 per-Article opening control，应建立新的 Requirement，不得把它们隐式追加到本已接受规格中。
