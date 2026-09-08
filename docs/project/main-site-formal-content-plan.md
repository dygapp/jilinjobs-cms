# Main Site Formal Content E1～E3 Planning

## Status

- Parent Planning Authority: GitHub Issue #60
- Planning baseline: `main@f42bacf4ab7719e3291288c77f0685b428b86141`
- Phase 3 re-entry: **PASS**
- Current Ready Execution Unit: **NONE until the first Candidate passes readiness-check**
- Scope: Issue #60 E1 / E2 / E3 only

## 1. Planning intent

Issue #92 Phase 0～Phase 3 已完成前置 Repository / Migration Architecture 收敛。E1～E3 现在可以进入正常 Planning，但不能因为 re-entry PASS、Issue 编号或候选名称直接获得 Execute Authority。

本轮从当前 Repository state 重新对账 Main Site stable identities、Public behavior、Page lifecycle 与 Generic Content Migration capability，目标是形成真实依赖顺序并只创建有独立工程价值的最小 Execution Unit。

## 2. Current repository audit

### 2.1 Stable Main targets already exist

`sites/jilinjobs/structure/**` 已持有 Main 后续正式内容所需的稳定目标：

- Main Column aliases：`notice`、`employment-news`、`policy*`、`typical*`、`recruitment-announcement` 等；
- PageGroup aliases：`guide`、`jobs`；
- Page identities：standalone `about` / `budget` / `teacher-library` / `live-course` / `employment-report-contact`，以及 `guide/*`、`jobs/*`；
- List codes：`HOME_CAROUSEL`、`SITE_RELATED`、`SITE_REGIONAL_GRADUATES`、`SITE_JILIN_UNIVERSITIES`；
- Navigation stable codes与固定 Main / HOME_SHORTCUT / HOME_QUICK target；
- `HOME_RECRUITMENT_PROMO` AdvertisementSlot。

因此 E1～E3 不需要重新建立 Main stable structure foundation。

### 2.2 External-link runtime behavior already exists

当前 Public Renderer 已支持：

- `EXTERNAL_LINK` Article 在 Main 首页 / 栏目列表直接打开 `externalUrl`；
- Navigation LINK / cross-entry 使用 document navigation，并按 current `newWindow` projection执行；
- CmsList LINK / ARTICLE 已有稳定 target semantics；ARTICLE-backed external Article仍使用 Article外部 URL；
- Advertisement已有 URL / openMode；
- 固定 NCSS / 外部业务入口可继续作为明确工程集成。

E1 当前没有证据表明需要增加新的 Article open-mode字段、外链徽标、通用 Redirect对象或另一套 Link domain model。

### 2.3 Page operational-content lifecycle has a real gap

当前 `PageService` 允许预置 Page 编辑正文 / renderMode / embedUrl；`docs/specifications/preset-site-structure.md` 也把 Page 正文和呈现字段视为可运营维护内容。

但 `SitePackageProvisioner` 当前 ordinary reconcile 会把 package中的 `bodyHtml / renderMode / embedUrl` 再次 UPDATE回 Runtime。当前 `pages.json` 又包含大量占位正文。

这意味着 E2 不能通过“直接把正式正文写进 stable structure”完成：否则 Site Package 会长期接管 operator-owned Page content，且 Existing Site 的正式内容 lifecycle 与 Historical/Canonical Migration边界混同。

同时 EU-47 Generic Content Migration目前只支持 Article + ListItem，不支持稳定 Page target，因此 E2/E3 尚缺少 site-neutral Page canonical migration foundation。

## 3. Dependency closure

依赖顺序冻结为：

```text
E1  Main External-link Ownership & Behavior Boundary
    └─ freeze current semantics; no implementation EU if no gap
          ↓
E2  Main Single-page Formal Content Boundary
    ├─ first close Page stable-structure vs operational-content lifecycle gap
    ├─ establish site-neutral canonical Page content import foundation
    └─ then collect / accept actual Main formal Page content
          ↓
E3  Main Historical Content Collection & Canonical Migration
    ├─ full source discovery / accepted snapshot
    ├─ Article + external link + resource + Page + historical ListItem canonical data
    ├─ Fresh DB / idempotency / conflict / reconciliation
    └─ Human Review
```

E1 与 E2 的 accepted contract 是 E3 source classification / canonical ownership 的输入。E3 不应在 E1/E2 boundary未冻结时开始构建长期 canonical dataset。

## 4. E1 planning result

E1 形成独立 Requirement / Specification，但当前 repository audit未发现需要代码实现的 contract gap。

E1 的目标是把现有多种外链载体的 ownership统一解释清楚，而不是把它们合并成一个模型：

- Article EXTERNAL_LINK：Article owns title + source URL；
- Navigation LINK：Navigation owns navigation label + target + open mode；
- CmsList LINK：ListItem owns presentation title/URL/open mode；
- CmsList ARTICLE：Article owns canonical target；ListItem只承担 placement；
- Advertisement：Advertisement owns campaign target/open mode；
- fixed integration：工程 owns genuinely fixed third-party seam；
- Historical external content/list members：E3 Canonical Migration owns provenance/fingerprint/import。

若该 Requirement / Specification integration后无新的 diff evidence推翻 audit，E1 以 **Planning / Authority closure** 完成，不创建 Candidate Execution Unit。

## 5. E2 first implementation slice

E2 的第一个真实实现 slice 应只解决 foundation，不采集 Main 真实正文：

**Candidate identifier is assigned only after slice-work + collision check.**

目标：

1. ordinary Site Package Page reconcile不再覆盖 existing Page的 operator-owned `bodyHtml / renderMode / embedUrl`；Fresh create仍可使用 package defaults作为初始占位；
2. Generic Content Migration增加稳定 Page target的 canonical content能力；
3. Page canonical identity使用 Site stable identity（`groupAlias? + alias`），不依赖 Runtime id / Vue Router实现；
4. first apply必须具有明确 preflight / target guard，不能静默覆盖未知 operator content；
5. accepted apply后记录 site-neutral migration mapping/fingerprint，使 repeated same input = SKIP、unexpected source/target drift = CONFLICT；
6. 保持 Article/ListItem current Generic behavior、Party compatibility、Site Package structure、Public API与页面视觉不变。

该 slice完成后，E2 才具备安全接收真实 Main formal Page canonical content的长期路径。

## 6. E3 planning boundary

E3 将复用 `data-migrations/README.md` 与 EU-47/EU-48 accepted canonical organization，不复制 Party assumptions。

E3 的 Specification不能在 source discovery之前冻结 accepted item count / exact source set。Planning可以先冻结 discovery completeness、identity、provenance、canonical shape与verification contract；实际 accepted snapshot必须由后续 repository-owned evidence晋升。

E3 至少覆盖：

- Main Column historical Article / EXTERNAL_LINK Article；
- Article resources / attachments / body references；
- E2确定需要迁移的 Page formal content；
- historical `HOME_CAROUSEL` / `SITE_LINKS`等运营 ListItem（仅当 source evidence证明属于迁移范围）；
- source discovery completeness / unresolved classification；
- Fresh import、second import idempotency、fingerprint conflict、Runtime reconciliation与Human Review。

## 7. Non-goals

- 不修改 Party accepted content / compatibility；
- 不修改 Public frontend technology；
- 不进入 C1 / C2 / Issue #57 / #59；
- 不执行 Repository split；
- 不把 Main historical data塞回 Flyway、Site stable structure或one-time bootstrap；
- 不为“统一外链”新增通用 Link实体；
- 不在 foundation slice中采集外网内容或冻结未经 repository evidence接受的Main item count；
- 不更新 `agentic-dev` baseline。

## 8. Next gate

1. integrate E1 Requirement / Specification closure；
2. integrate E2 Requirement / Specification / Technical Plan；
3. 对 E2 first foundation slice执行 `slice-work → readiness-check`；
4. 只有 Readiness PASS 后形成新的 Ready Execution Unit；
5. Execute必须从 integrated planning baseline重新 Fresh Context核验，不继承 EU-48 或 Phase 3 Authority。
