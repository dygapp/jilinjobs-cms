# EU-52 — Main Page Formal Content Package Adoption

## Status

- Parent Planning Authority: GitHub Issue #60 / Main Page Site Package follow-up
- Architecture Authority: GitHub Issue #77
- Requirement: `docs/requirements/main-single-page-formal-content.md`
- Specification: `docs/specifications/main-single-page-formal-content.md`
- Technical Plan: `docs/technical/main-single-page-formal-content.md`
- Planning Authority: `docs/project/main-site-formal-content-plan.md`
- Candidate formed by: **slice-work**
- Identifier: **EU-52**
- Planning baseline: `main@25e452ee3ce66c2d7da1000ad55e9570d732528a`
- Readiness: **PASS**
- Execute state: **COMPLETED**
- Execute baseline: `main@14d63d82135385e0b8de89b578836d5d995b0a4b`
- Execute Authority: **TERMINATED**

EU-52 completed its Planning → Readiness → independent Fresh Context Execute → Verification → bounded Human Review → Integration → Post-Integration lifecycle. This archived Work artifact is historical completion evidence and does not grant any new Execute Authority.

## 1. slice-work result

Current Repository evidence separates the Site Package follow-up into two different maturity paths:

1. **Main Page formal content/adoption**
   - stable Page identities already exist in `sites/jilinjobs/structure/pages.json`;
   - EU-49 already protects operator-managed Page mutable content during ordinary reconcile;
   - existing Site Package asset manifest/projector already provides stable package resource ownership;
   - EU-50 accepted source handoff contains 10 concrete Page targets with source fingerprints, expected target fingerprints and resource evidence;
   - Issue #77 Human Authority has resolved the only material Page resource ownership ambiguity for `budget`.

2. **stable Main ListItem membership**
   - current Site Package v1 owns List definitions but has no `list-items` structure type;
   - stable ListItem identity, representation, adoption, reconcile, operator mutation and upgrade semantics remain unresolved Planning work.

Combining them would couple a ready Page product slice to an unresolved ListItem architecture decision. `slice-work` therefore forms one homogeneous Candidate Execution Unit for Page only:

> **EU-52 — Main Page Formal Content Package Adoption**

Stable Main ListItem remains an independent downstream Planning Candidate and receives no EU-52 authority.

## 2. Accepted Page source handoff

The accepted Page source evidence comes from the EU-50 attempt-3 artifact and the durable repository handoff index `data-migrations/main/v1/reports/site-package-handoff.json`.

Source artifact:

- workflow run: `34303771704`;
- artifact id: `10086056781`;
- artifact name: `eu50-main-retry-attempt-3-a96cee22449508f92f3c89789f99aad477286a66`;
- source Head: `a96cee22449508f92f3c89789f99aad477286a66`;
- artifact digest: `sha256:66118e4f21bf7644db1c97e2a631eee5d4902410f167606286e1280293620494`;
- GitHub expiry: `2026-09-16T02:42:04Z`;
- current freshness at Readiness check: **PASS / expired=false**.

Accepted Page identities:

| Source identity | Stable target | Source fingerprint | Expected old target fingerprint | Downloaded resource evidence |
|---|---|---|---|---:|
| `main-page:guide-contact` | `(guide, contact)` | `c9f507774906aec36413cfac1394ab2dd3049ef0cfa69670a4ec1178ce4e76ee` | `edb04aab9395a8b669c4c4c6a465807ae185e1c203eb659bc5984590a15a8e61` | 0 |
| `main-page:guide-dagl` | `(guide, dagl)` | `15b72c1f6f3ffa0dc8b75a8f87036f96d05b35c049e29a4a49b548da247f2d18` | `25d85acf95b5a4180b819a33088ddab39b9d40ea820c3a5dbd9082873d572bf2` | 0 |
| `main-page:guide-faq` | `(guide, faq)` | `69650b45db23d617a5c20169f7a03b64e74da6835a2f4544a56dfedda8d004aa` | `1113157546fb3e9dcce421db0c3ea2029888e8ff88a94312d4010677689b512d` | 0 |
| `main-page:guide-dygl` | `(guide, dygl)` | `8af1c593f94d7a5912606122076860436c9dbef0737fa6b875d704af6646ea51` | `3ab71f716f07415cd43233ff16185505e3812d3292bb519663e9b8fd545e02f8` | 0 |
| `main-page:guide-jypq` | `(guide, jypq)` | `2b867bc8859386271419797a59a77a15811be35d8791c1c1dd750f172bc42867` | `d4c59afd36a228116cdac13cb7e441a649f66501c7617c4fb667db2785b8f662` | 4 |
| `main-page:about` | `(null, about)` | `50b661b20bba70dd32c3932b558611748540a768954c796f5966d84ffbc92ebf` | `7b0edda0a0731b89e578d442cdf9e243dccf7e97eaa8d867aeb068742733904e` | 0 |
| `main-page:guide-xlrz` | `(guide, xlrz)` | `cb6c9facbbf4754295a6c31e321fcf36e0d03c79dd21cd1dbe367850c648ec2d` | `ce05285439b5bbbcaae59d1f6e1bb113a1ac098563fdfdcb7783ea5085b9f95b` | 1 |
| `main-page:budget` | `(null, budget)` | `59c85eb560ad8e8c889a75a8f800140840a54f687ee68b82463a7f4ebf3f273c` | `cd3bb10e785b86be1fe36c1b66dcc0e2c6764c077311a989e2a6785990903562` | 5 + 8 Human-authorized bounded PDF acquisitions |
| `main-page:employment-report-contact` | `(null, employment-report-contact)` | `7272454f00e4eb0992527e2ed0e192ce425292ab5ba2f7adac3ebb81cee8940d` | `6dbbec7d03e73fe0cab2309f9433325a43447d1bf5518a1251bc7353c10ddebb` | 0 |
| `main-page:teacher-library` | `(null, teacher-library)` | `76e7d771af7e01a6b6218e1d0ca80f4b117e133ea223e7570de3ec788e2e9b95` | `a156eec61c9598c897aa3d33afe19c2f49fc6ccd2fb3992cb84042db62105c41` | 138 |

All 10 accepted Page records are `RICH_TEXT`.

The expected old target fingerprints are adoption preconditions, not source fingerprints. They must be revalidated against the actual prior package baseline during Execute before any package content bytes are promoted.

## 3. Dependency closure

### 3.1 Repository architecture — PASS

Issue #77 and current four-layer Authority establish:

- stable Page identity/content defaults and stable Page assets belong to JilinJobs Site Package;
- Historical Migration is not the Main Page fallback;
- Public Renderer remains a replaceable consumer of stable CMS/API/URL contracts.

### 3.2 EU-49 foundation — PASS

EU-49 is completed and its Execute Authority terminated. It already provides:

- ordinary Site Package reconcile preserving existing Page `bodyHtml/renderMode/embedUrl`;
- deterministic Fresh Page create defaults;
- Page domain validation/edit foundation;
- Generic Page migration capability retained only as generic compatibility capability.

EU-52 does not inherit EU-49 Execute Authority and does not reopen it.

### 3.3 EU-50 source evidence — PASS

EU-50 is completed. Its accepted attempt-3 source evidence provides the 10 Page handoff records and durable repository handoff index. The artifact is currently present and non-expired with the accepted digest.

### 3.4 EU-51 — CLOSED / no dependency authority

EU-51 is completed and its Execute Authority terminated. EU-52 does not depend on or inherit EU-51 Runtime import authority; EU-51 matters only as current repository lifecycle evidence proving there is no active execution Unit.

### 3.5 `budget` resource decision — PASS

Issue #77 Human Authority confirms all 13 `budget` PDFs are package-owned stable assets. The 8 legacy absolute `zhjy.jilinjobs.cn:8080/group1/cms/**` references are accepted legacy source residue and may be reacquired only through the authorized bounded normalization. No remaining Page product-ownership ambiguity blocks the slice.

### 3.6 Page package baseline drift — PASS

Current GitHub history shows `sites/jilinjobs/structure/pages.json` was last changed by EU-38 on 2026-09-06 (`4846d6a5d4766da708e06e86289fcffa586a404f`), before EU-50 source evidence was produced. No later Page package baseline change is present at planning baseline `25e452...`.

## 4. In scope

EU-52 future Execute scope is bounded to:

1. add generic exact-baseline Page content adoption semantics to Site Package provisioning while preserving EU-49 operator guard;
2. add identity-specific adopted/protected Page content outcomes to the existing machine-readable provisioning report;
3. promote the accepted 10 Main Page formal bodies into `sites/jilinjobs/structure/pages.json`;
4. promote accepted Page-local stable resources into `sites/jilinjobs/assets/pages/**` and the existing asset manifest/projector;
5. perform the Issue #77-authorized bounded acquisition/normalization for the 8 unresolved `budget` PDF source residues;
6. rewrite accepted local references to `/static/pages/**`;
7. update package version/structure/asset digests using existing manifest contracts;
8. add focused offline verification, exact-head Browser verification and bounded Human Review.

## 5. Out of scope

Explicitly excluded:

- stable Main ListItem capability/content/adoption/reconcile;
- all 230 deferred problem Articles;
- all 6 source-defect Articles awaiting customer confirmation;
- new Article migration/import work;
- Historical Migration Page mapping or Main-specific Generic schema;
- Generic Flyway change;
- placeholder/fixed-integration Page redesign;
- Public frontend technology or URL-contract change;
- Party canonical data/migration changes;
- `dygapp/agentic-dev` baseline upgrade or repository modification.

## 6. Readiness check

`readiness-check` against planning baseline `main@25e452ee3ce66c2d7da1000ad55e9570d732528a`:

| Dimension | Result | Evidence |
|---|---|---|
| Authority / Intent | **PASS** | Issue #60/#77 + current Site Package ownership correction define Page as independent product path |
| Requirement | **PASS** | current Requirement READY; no unresolved product intent |
| Specification | **PASS** | accepted target set, adoption semantics, resource boundary, error behavior and verification contract are frozen |
| Technical Planning | **PASS** | current provisioner, Page guard, asset projector and bounded implementation topology are directly inspectable |
| Slice Integrity | **PASS** | one Page capability+content delivery; stable ListItem explicitly split out |
| Dependency Closure | **PASS** | EU-49/EU-50 completed; `budget` Human Authority accepted; EU-51 closed/no inherited authority |
| Source Evidence Freshness | **PASS** | artifact `10086056781` currently `expired=false`; exact digest/provenance recorded |
| Baseline Drift | **PASS** | Page package baseline unchanged after EU-38; current main is verified planning baseline |
| Verification Feasibility | **PASS** | existing Site Package, Backend, Admin/Public/Integrated Browser and asset verification surfaces available |
| Scope / Rollback | **PASS** | bounded package/provisioning/content change; no schema/Article/ListItem/frontend-tech expansion |
| Human/Product Ambiguity | **PASS** | all 13 budget PDFs resolved by Issue #77 Human Authority; visual acceptance deferred to bounded Human Review after automated Browser |

Overall Readiness: **PASS**.

## 7. Verification contract for Execute

Before an implementation PR can integrate, exact-head evidence must prove:

1. generic adoption fingerprint validation;
2. Fresh Page creation with new formal package content;
3. exact old-baseline Existing Page adoption;
4. operator-diverged Existing Page preservation + identity-specific report;
5. structural reconcile remains compatible with protected content;
6. successful adoption is idempotent and later operator edits survive;
7. all 10 target identities/source fingerprints/expected old target fingerprints reconcile with accepted source evidence;
8. all accepted Page-local resources match source size/SHA-256 and package manifest entries;
9. `budget` all 13 PDF targets exist under `/static/pages/budget/**` and no accepted local reference retains legacy `/group1/cms/**` dependency;
10. no `migration-resource://` token remains in final Page package body;
11. no Main Page Historical Migration mapping/input is created;
12. focused Site Package/asset verification, Backend, Admin, Public and Integrated Browser regressions PASS;
13. automated exact-head Browser verification covers all 10 Page routes before Human Review;
14. bounded Human Review accepts the same exact implementation Head;
15. unresolved review threads = 0 and final base drift/mergeability checks PASS.

## 8. Source freshness Execute gate

Readiness PASS is not perpetual source-access authority. At Fresh Context Execute recovery, the agent must re-query GitHub artifact `10086056781` and confirm:

- `expired=false` and artifact remains retrievable;
- digest remains `sha256:66118e4f21bf7644db1c97e2a631eee5d4902410f167606286e1280293620494`;
- run/head provenance remains exactly accepted;
- integrated Requirement/Specification/Technical/Work Authority has not drifted;
- `main` has not changed Page package baseline or affected provisioning semantics.

If the artifact is unavailable/expired, **do not establish Execute Authority** from this Work record. First perform a new explicit bounded Page source-acquisition step with equivalent auditable provenance, then refresh Readiness. No silent fallback or source substitution is authorized.

## 9. Planning branch boundary

This Planning/Readiness lifecycle may modify only Authority/Work recovery documentation. It must not:

- download/promote Page source bytes into the repository;
- change `sites/jilinjobs/**` product bytes;
- change provisioning application code;
- acquire the 8 budget PDFs;
- run Runtime Page mutation;
- create a Main Page migration mapping;
- enter stable ListItem planning/implementation inside EU-52.

## 10. Next Gate

After this Planning/Readiness state is integrated and Post-Integration Current Evidence confirms the repository recovery locators are consistent:

> **Current Ready Execution Unit = EU-52; Execute = NOT STARTED.**

The next natural Gate is a **new Fresh Context EU-52 Execute-baseline recovery**. That Fresh Context must independently revalidate integrated `main`, Issue #60/#77, this Work artifact, source artifact freshness, Open PR/Issue/Actions and base drift before establishing Execute Authority.

EU-52 does not authorize or nominate the stable Main ListItem follow-up after its own future completion.

## 11. Execute Current Evidence

Fresh Context Execute recovery established the independent baseline `main@14d63d82135385e0b8de89b578836d5d995b0a4b` after revalidating integrated Authority, no Page/provisioning drift, source artifact availability/digest/provenance, Open PR/Actions, and the ten prior-package Page fingerprints.

EU-50 source artifact remains `10086056781` from run `34303771704`, Head `a96cee22449508f92f3c89789f99aad477286a66`, digest `sha256:66118e4f21bf7644db1c97e2a631eee5d4902410f167606286e1280293620494`.

The eight Issue #77-authorized legacy `zhjy.jilinjobs.cn:8080/group1/cms/**` budget residues were reacquired by bounded host normalization through EU-52 acquisition run `34433750232` / source commit `10e443132aeb06e01518400641d658bb7f95d36d`. Run result: **PASS**, 8/8 actual PDF responses, acquisition artifact `10135439308`, artifact digest `sha256:01104be3bccc5bafec4ca75e52fdc22ebbf2a408a8ca8d55cf7090474a8463a8`.

| Final file | Original legacy URL | Normalized acquisition URL | Bytes | SHA-256 | Runtime target |
|---|---|---|---:|---|---|
| `budget-2023.pdf` | `https://zhjy.jilinjobs.cn:8080/group1/cms/t_biz_attachment/content2/2023-02-27/8331fd04-91d2-4365-8576-44a7adb40d44.pdf` | `https://24365.jl.smartedu.cn/group1/cms/t_biz_attachment/content2/2023-02-27/8331fd04-91d2-4365-8576-44a7adb40d44.pdf` | 1020114 | `120edfa8ff20294845d8a96dc30c1cf3e3efd42ba7c561a85c554ad33d412a68` | `/static/pages/budget/budget-2023.pdf` |
| `budget-2024.pdf` | `https://zhjy.jilinjobs.cn:8080/group1/cms/t_biz_attachment/content2/2024-03-12/d87173cd-8a3f-4667-a042-ece836a0f0ea.pdf` | `https://24365.jl.smartedu.cn/group1/cms/t_biz_attachment/content2/2024-03-12/d87173cd-8a3f-4667-a042-ece836a0f0ea.pdf` | 1072453 | `22191f03b5eb7a8591a6ab94a76353bdf49efceb7f52dba75c985579d704e3f0` | `/static/pages/budget/budget-2024.pdf` |
| `budget-2025.pdf` | `https://zhjy.jilinjobs.cn:8080/group1/cms/t_biz_attachment/content2/2025-03-07/ceb5b088-6158-4b73-b446-3cf650194866.pdf` | `https://24365.jl.smartedu.cn/group1/cms/t_biz_attachment/content2/2025-03-07/ceb5b088-6158-4b73-b446-3cf650194866.pdf` | 395609 | `2a76bddefbc00baa4648526dbb4f8a902dbbea54294ae9246878648255c6d585` | `/static/pages/budget/budget-2025.pdf` |
| `budget-2026.pdf` | `https://zhjy.jilinjobs.cn:8080/group1/cms/t_biz_attachment/content2/2026-03-17/84773049-6580-4c9d-87ce-ab29f9576157.pdf` | `https://24365.jl.smartedu.cn/group1/cms/t_biz_attachment/content2/2026-03-17/84773049-6580-4c9d-87ce-ab29f9576157.pdf` | 558958 | `9c198388a55e90c930d7c994972749fb05aaff63f016eafd272814ca30f3aefe` | `/static/pages/budget/budget-2026.pdf` |
| `final-accounts-2022.pdf` | `https://zhjy.jilinjobs.cn:8080/group1/cms/t_biz_attachment/content2/2023-09-08/e74779f2-e551-43ad-a5f8-0a91020e8d00.pdf` | `https://24365.jl.smartedu.cn/group1/cms/t_biz_attachment/content2/2023-09-08/e74779f2-e551-43ad-a5f8-0a91020e8d00.pdf` | 703607 | `4dae555b1986e58953794d5326aed259b9742740cee1a7f527c183ed51c556ec` | `/static/pages/budget/final-accounts-2022.pdf` |
| `final-accounts-2023.pdf` | `https://zhjy.jilinjobs.cn:8080/group1/cms/t_biz_attachment/content2/2024-09-12/f7b003d5-9074-4d26-ac58-827131c02f22.pdf` | `https://24365.jl.smartedu.cn/group1/cms/t_biz_attachment/content2/2024-09-12/f7b003d5-9074-4d26-ac58-827131c02f22.pdf` | 2722567 | `b8da79b1ab19d1eda5dbbb22f99cc16ed9b5ccc4ccb2595b9a7117f43e9b1f24` | `/static/pages/budget/final-accounts-2023.pdf` |
| `final-accounts-2024.pdf` | `https://zhjy.jilinjobs.cn:8080/group1/cms/t_biz_attachment/content2/2025-09-05/4851fa77-f787-4d7f-be10-34490a98df0f.pdf` | `https://24365.jl.smartedu.cn/group1/cms/t_biz_attachment/content2/2025-09-05/4851fa77-f787-4d7f-be10-34490a98df0f.pdf` | 1946548 | `14066db387bfb7a003df6ce8f1d55180ad561c76b8440dee4d3274f22efcd84b` | `/static/pages/budget/final-accounts-2024.pdf` |
| `final-accounts-2025.pdf` | `https://zhjy.jilinjobs.cn:8080/group1/cms/t_biz_attachment/content2/2026-09-04/75b071b2-c354-4690-bb8e-2bdc25e600bb.pdf` | `https://24365.jl.smartedu.cn/group1/cms/t_biz_attachment/content2/2026-09-04/75b071b2-c354-4690-bb8e-2bdc25e600bb.pdf` | 2325289 | `6ea0e382e585ceae389a717744b2ec76b3c9fd6ec85a5e263a2c15fcd4d544aa` | `/static/pages/budget/final-accounts-2025.pdf` |

Promotion generator verified all 10 accepted Page prior-package fingerprints before replacement and all 156 Page asset bytes before writing package files. Stable Main ListItem and the 230 + 6 Article backlog remain untouched.


## 12. Implementation, Human Review, Integration & Post-Integration closure

Final implementation evidence:

- Execute baseline: `main@14d63d82135385e0b8de89b578836d5d995b0a4b`;
- implementation PR: #125;
- final exact Head: `ff4acdc08e9b902d6aa273ff33fae33fb5bfc520`;
- exact-head CI #972 / run `34439729656`: **PASS** including Backend / Public / Admin / Integrated Browser;
- exact-head Site Package #103, Backend Boundary #40, Generic #63, Canonical #273, Party De-specialization #18, EU-30 #223, EU-51 Runtime #17 and EU-51 Imported Browser #13: **PASS**;
- unresolved review threads/comments/reviews: 0; final pre-integration base drift: none.

Bounded Human Review on the same exact Head: **PASS**. Review Environment #854 / run `34440230078` completed AI/Browser prechecks, clean review baseline, Runtime verification, FRP tunnel and external URL checks before manual review. The only manual finding was `guide/faq` top-level question hierarchy; all 10 top-level questions were promoted to semantic `h2`, answer numbering stayed paragraph content, and the final Browser regression verified heading font size > answer text and `font-weight: 700`. Manual re-review returned PASS.

PR #125 was squash merged with expected-head protection. Implementation integrated main:

`main@ccbd9fd8c6048f5b4a96d965b8578f7b7a1d2838`

Post-Integration evidence on that exact main:

- CI #973 / run `34442174532`: **PASS** including Backend / Public / Admin / Integrated Browser;
- Site Package #104 / run `34442174578`: **PASS**;
- Backend Application Boundary #41 / run `34442174526`: **PASS**;
- Generic Content Migration #64 / run `34442174535`: **PASS**;
- Party Migration De-specialization #19 / run `34442174559`: **PASS**.

Accepted product result:

- 10 Main formal Pages are JilinJobs Site Package-owned defaults;
- 156 new Page-owned assets were integrated; package assets total = 187;
- `budget` all 13 PDFs are package-owned under `/static/pages/budget/**`;
- Existing-Site content adoption is exact prior-package fingerprint guarded; operator-diverged content is preserved/reported; successful adoption is idempotent and later operator edits remain protected;
- no Main Page Historical Migration mapping/input, Generic Flyway/schema change, stable ListItem implementation or Article backlog import was introduced.

Boundary after closure:

- stable Main ListItem remains an independent Planning Candidate with no Identifier / Readiness / Execute Authority;
- 230 deferred problem Articles and 6 source-defect Articles remain independent later-review / customer-confirmation evidence;
- the proposed “latest 20 records per column” Article limit was **not adopted** into EU-52 Authority and would require a separate future Planning decision if used;
- no downstream candidate inherits EU-52 Execute Authority.

EU-52 is therefore **COMPLETED** and its Execute Authority is **TERMINATED**. Current Ready Execution Unit returns to **NONE**.
