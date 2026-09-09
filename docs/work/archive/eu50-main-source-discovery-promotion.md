# EU-50 — Main Source Discovery & Article Snapshot Promotion

## Status

- Parent: GitHub Issue #60 / E3
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Requirement: `docs/requirements/main-historical-content-migration.md`
- Specification: `docs/specifications/main-historical-content-migration.md`
- Technical Plan: `docs/technical/main-historical-content-migration.md`
- Identifier: **EU-50**
- Readiness: **PASS**
- Execute state: **COMPLETED**
- Verification: **PASS**
- Pull Request: **#117**
- Final implementation Head: `8c2fdd6cdbbd4d1faf865d0ba4ca0f40a0096e84`
- Integrated main: `05dfa604ccde45c8409cf6a456e4f201534dc602`
- Accepted dataset digest: `sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`
- Current migration scope: **ARTICLE ONLY**
- Current Ready Execution Unit: **NONE**
- Execute Authority: **TERMINATED**

EU-50 completed the bounded Legacy Main source-discovery / evidence / Article-only promotion stage. It did not perform Main Runtime import and did not enter EU-51.

## 1. Accepted implementation result

EU-50 closed the source/promotion boundary with the following accepted result:

1. **Bounded source discovery**
   - systematically traversed the configured Main Article surfaces/pagination;
   - classified INTERNAL / EXTERNAL_LINK Article identities;
   - collected Article body/resource evidence;
   - preserved Page/List discoveries as Site Package handoff evidence rather than migration input.

2. **Finite retry / explicit error evidence**
   - source full pass plus bounded targeted retry closed at attempt 3;
   - retryable transient targets remaining = 0;
   - only explicit HTTP 404/410 establishes `SOURCE_RESOURCE_MISSING`;
   - transport / unsupported / malformed / retry-exhausted cases remained explicit classifications;
   - no silent repair or silent discard was allowed.

3. **Article-only eligibility**

```text
3314 total Article candidates
= 3078 current import eligible
+ 6 source-defect excluded pending client confirmation
+ 230 deferred problem Articles
```

Current import-eligible subset:

- INTERNAL: **1577**；
- EXTERNAL_LINK: **1501**；
- local resource files: **2603**；
- resource bytes: **450,273,166**。

4. **Repository-owned canonical promotion**
   - accepted current subset promoted under `data-migrations/main/v1/**`；
   - promoted tree contains only current import-eligible Articles；
   - source-defect/deferred records remain durable reports/evidence outside current import index；
   - exact-head replay proved deterministic promotion with `promotion_changed=false`；
   - accepted dataset digest: `sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`。

5. **Preserved ownership boundaries**
   - Main Article historical content remains Historical Migration ownership；
   - Main Page content and stable Main ListItem membership belong to JilinJobs Site Package；
   - no Page/List migration fallback was introduced；
   - no CMS Core schema/provisioning redesign was introduced；
   - no Main-specific Runtime importer was introduced。

## 2. Human Authority / deferred Article decision

On 2026-09-09, project Human Authority explicitly decided that problematic Articles are handled later after the current task sequence:

- they remain explicit durable evidence；
- they are **not current import input**；
- they do **not block current-subset promotion/integration or current project progression**；
- they must not be guessed, repaired, rewritten or deleted merely to advance the clean subset。

Issue #60 authority comment: `5601906947`。

This decision applies to the 230 deferred problem Articles. The 6 source-defect Articles remain separately excluded pending client confirmation.

## 3. Source / provenance closure

Frozen final source/retry evidence:

- source run ID: `34303771704`；
- source artifact ID: `10086056781`；
- source artifact name: `eu50-main-retry-attempt-3-a96cee22449508f92f3c89789f99aad477286a66`；
- source artifact digest: `sha256:66118e4f21bf7644db1c97e2a631eee5d4902410f167606286e1280293620494`；
- source Head: `a96cee22449508f92f3c89789f99aad477286a66`；
- final retry attempt: `3`。

The repository-owned canonical dataset is the durable downstream input. The Actions artifact remains provenance/evidence, not the sole long-lived source.

## 4. Final exact-head verification

Final implementation Head `8c2fdd6cdbbd4d1faf865d0ba4ca0f40a0096e84` obtained required Current Evidence:

- EU-50 Main Source Discovery #64 / run `34352898345` — **PASS**；
- EU-50 Main Import Eligibility #30 / run `34352898410` — **PASS**；
- Canonical Migration Verification #256 / run `34352898301` — **PASS**；
- Generic Content Migration Verification #51 / run `34352898313` — **PASS**；
- EU-30 Migration Upgrade Verification #206 / run `34352898498` — **PASS**；
- CI #944 / run `34352898338` — **PASS**，包含 Backend / Admin / Public / Integrated Browser；
- Review Environment #830 / run `34352898415` — **PASS**，包含 AI/Browser、外部访问验证、lease 生命周期与 cleanup；
- unresolved review threads = **0**；
- base drift = **none**。

The exact-head promotion replay returned the same dataset digest and `promotion_changed=false`.

## 5. Integration / Post-Integration evidence

PR #117 was squash merged with expected exact Head `8c2fdd6cdbbd4d1faf865d0ba4ca0f40a0096e84`.

Integrated main:

`05dfa604ccde45c8409cf6a456e4f201534dc602`

Post-Integration push evidence:

- Generic Content Migration Verification #52 / run `34354290017` — **PASS**；
- CI #945 / run `34354289981` — **PASS**，包含 Backend / Admin / Public / Integrated Browser。

No Main Runtime import was performed during EU-50 integration or post-integration verification.

## 6. Acceptance closure

EU-50 acceptance obligations are closed:

- complete configured Article surface/pagination traversal established；
- Article classification and stable identity evidence preserved；
- finite retry closed；
- Article arithmetic closed；
- source-defect / deferred records remain separately discoverable；
- current import index contains Articles only；
- repository-owned canonical tree contains exactly the accepted current subset；
- local Article resources satisfy path / size / SHA-256 integrity；
- deterministic offline verification passed；
- Page/List findings remain Site Package handoff evidence；
- retired destructive mixed triage remains fail-closed；
- final exact-head and integrated-main verification passed；
- no Runtime Main import / EU-51 execution occurred。

## 7. Authority termination / downstream Gate

After EU-50 completion:

- EU-50 accepted-snapshot dependency: **SATISFIED**；
- EU-50 Execute Authority: **TERMINATED**；
- Current Ready Execution Unit: **NONE**；
- EU-51 remains **Candidate / NOT READY**；a Fresh Context must re-run downstream dependency closure / `readiness-check` against the integrated canonical dataset before any Runtime import；
- Main Page / stable ListItem follow-up remains a separate Site Package Planning/Readiness path；
- 230 deferred problem Articles and 6 source-defect Articles remain later-review/client-confirmation backlog and are not current import input；
- no downstream path inherits EU-50 Execute Authority。

This archive artifact is historical execution/current-evidence traceability. It does not itself grant new Execute Authority.
