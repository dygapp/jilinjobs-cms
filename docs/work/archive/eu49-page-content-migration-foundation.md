# EU-49 — Page Operational Content Ownership & Migration Foundation

## Status

- Parent: GitHub Issue #60 / E2
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Requirement: `docs/requirements/main-single-page-formal-content.md`
- Specification: `docs/specifications/main-single-page-formal-content.md`
- Technical Plan: `docs/technical/main-single-page-formal-content.md`
- Candidate formed by: `slice-work`
- Identifier: **EU-49**
- Readiness: **PASS**
- Planning baseline: `main@f42bacf4ab7719e3291288c77f0685b428b86141`
- Execute baseline: `main@ad742086c340bb3735814ddfdc72c9b8d1709fa1`
- Execute state: **COMPLETED**
- Verification: **PASS**
- Pull Request: **#112**
- Final implementation Head: `a59fef41ab5061175be276df66048d3dbcdac320`
- Final implementation tree: `8b34416e20936f141ccbebaed5fa6df6439f5e19`
- Integrated main: `18da735c654c1a5d1310fe6db7e8e98f6f7b0026`
- Integrated tree: `8b34416e20936f141ccbebaed5fa6df6439f5e19`
- Execute Authority: **TERMINATED**

EU-49 由 Issue #60 / E2 在完成 Requirement、Ready Specification、Technical Planning、`slice-work` 与 `readiness-check` 后形成。Planning/Readiness PR #111 集成后，通过 Fresh Context 重新核验 integrated `main`、Issue #60 / #77、Authority、Open PR / Actions 与 base drift，并以 `main@ad742086c340bb3735814ddfdc72c9b8d1709fa1` 建立本 Unit 独立 Execute baseline；没有继承 EU-48、Phase 3 或 E1 的 Execute Authority。

## 1. Accepted implementation result

EU-49 完成以下 bounded foundation：

1. **Page operational content ownership**
   - Fresh Page creation继续允许 Site Package 提供 `bodyHtml / renderMode / embedUrl` 初始默认值；
   - ordinary Site Package reconcile 对 existing Page 不再持续覆盖这三个 operator-managed content fields；
   - stable Page identity / structure ownership 仍属于 Site Package。

2. **Core Page content capability**
   - 新增窄的 Page content-only update boundary；
   - 复用既有 sanitizer、render-mode 与 embed validation；
   - 不改变 Page identity/group/name/order/enabled/preset；
   - 不新增 HTTP API。

3. **Generic Page canonical migration capability**
   - Generic Content Migration 增加 site-neutral Page canonical load/preflight/apply/mapping/report；
   - stable target 使用 `(groupAlias? + pageAlias)`；
   - first apply 使用 exact target-content fingerprint guard；
   - same-source rerun = SKIP，并保留 import 后 operator edits；
   - source fingerprint / target precondition / mapping target drift = CONFLICT；
   - Page body resource 使用安全路径、digest 校验与 deterministic historical static projection。

4. **Generic schema evolution**
   - 新增 append-only `V3__page_content_migration_mapping.sql`；
   - V3 保持 site-neutral；V1/V2 未修改；
   - Flyway history verification 同步接受已批准的 Generic V1/V2/V3 lineage。

5. **Preserved boundaries**
   - 没有 Main canonical Page bytes / source collection；
   - 没有 E3 execution；
   - 没有 Party dataset change；
   - 没有 Public/Admin route 或视觉变化；
   - 没有 Page Resource association；
   - 没有 `agentic-dev` baseline update。

## 2. Verification closure

Final implementation Head `a59fef41ab5061175be276df66048d3dbcdac320` 取得 required exact-head evidence：

- CI #867 / run `34199804572` — **PASS**，包含 Backend / Admin / Public / Integrated Browser；
- Backend Application Boundary Verification #25 — **PASS**；
- Generic Content Migration Verification #22 — **PASS**；
- Site Package Verification #67 — **PASS**；
- Party Migration De-specialization Verification #8 — **PASS**；
- Canonical Migration Verification #193 — **PASS**；
- EU-30 Migration Upgrade Verification #143 — **PASS**；
- unresolved review threads = **0**。

首轮 verification 暴露并关闭了两个 Stale / Compatibility Verification 问题：

1. 新 `PageMapper.updatePageContent` capability 使两个 existing test doubles 需要同步实现新方法；主实现本身已编译通过；
2. EU-41 baseline-separation verifier 仍硬编码 Generic Flyway 仅 V1/V2，在合法 V3 加入后被同步为 accepted V1/V2/V3 lineage；没有放宽为任意 migration。

上述修复后，所有 final-head required gates 重新执行并 PASS。

## 3. Integration / Post-Integration evidence

PR #112 以 expected exact Head `a59fef41ab5061175be276df66048d3dbcdac320` squash merged。

Integrated main：

`18da735c654c1a5d1310fe6db7e8e98f6f7b0026`

Integrated tree：

`8b34416e20936f141ccbebaed5fa6df6439f5e19`

该 tree 与 final PR Head tree 完全一致。

Integrated push evidence：

- CI #868 / run `34200526862` — **PASS**，包含 Backend / Admin / Public / Integrated Browser；
- Generic Content Migration Verification #23 / run `34200526865` — **PASS**；
- Site Package Verification #68 / run `34200526881` — **PASS**；
- Backend Application Boundary Verification #26 / run `34200526853` — **PASS**；
- Party Migration De-specialization Verification #9 / run `34200526884` — **PASS**。

Final PR tree 与 squash-integrated tree 等价，因此 final-head Canonical Migration / EU-30 Upgrade exact-tree evidence仍可作为对应 regression claim 的 Current Evidence；integrated push workflows另外重新证明了本 Unit直接影响的 Generic / Site Package / Boundary / Party compatibility 与完整 CI chain。

## 4. Acceptance closure

EU-49 acceptance obligations全部闭环：

- existing preset Page content edits survive ordinary Site Package reconcile；
- Fresh Page provisioning保持 deterministic；
- Generic Page first apply只在 exact target precondition匹配时执行；
- first apply记录 mapping并得到 expected sanitized/re-written Runtime content；
- same input rerun = SKIP，且不覆盖后续 operator edit；
- source / target / mapping drift拒绝且不修改 Page；
- resource path/digest/token safety得到验证；
- V3 site-neutral / append-only；
- Article/List 与 Party consumers regression PASS；
- content-migration仍只依赖 cms-core并保持 non-web；
- diff没有 Main真实内容或可见页面变化；
- final required Actions PASS，unresolved review threads = 0。

## 5. Authority termination / next gate

EU-49 completion 后：

- E2 Page Operational Content Ownership & Migration Foundation：**COMPLETED**；
- Current Ready Execution Unit：**NONE**；
- EU-49 Execute Authority：**TERMINATED**；
- E3 的 EU-49 foundation prerequisite：**SATISFIED**；
- E3 仍只保持 Requirement / Specification Planning Authority，不因 EU-49 completion 自动获得 Identifier、Ready 状态或 Execute Authority；
- 下一自然 Gate 是 E3 Fresh Context Planning / source-evidence recovery，然后重新执行必要 dependency closure、`slice-work` 与 `readiness-check`；
- 在新的 Ready Execution Unit形成前，不开始 Main source collection / canonical promotion / Execute；
- Issue #77 继续承担长期四层 Architecture Authority；
- 本 archive artifact只承担 historical evidence / traceability，不授予后续 Execute Authority。
