# Public Frontend Replaceability & Source Isolation Technical Plan

## Authority

- `docs/requirements/public-frontend-replaceability.md`
- `docs/specifications/public-frontend-replaceability.md`
- `docs/architecture/decisions/ADR-0002-public-site-multi-entry-modular-spa.md`
- `docs/technical/public-site-frontend.md`
- Issue #60 / D1

## Decision

Converge the **current Public source ownership boundary first**, without introducing a generic replacement framework or changing the accepted Multi-entry SPA architecture.

The smallest justified engineering change is:

1. remove Admin-only API clients/models/mutations from `frontend/public-site/src/**`;
2. retain only Public runtime adapters/types required by Main/Party rendering;
3. add an automated source-boundary guard that rejects `/api/admin/**` dependencies from Public production source;
4. preserve all current Public routes, runtime behavior, build artifact and Gateway behavior;
5. keep current CI/Review direct module invocation until a real second implementation/toolchain creates evidence that a generic build/deploy adapter is needed.

This plan deliberately treats toolchain/delivery abstraction as deferred work. Adding a framework-neutral build layer now would create speculative complexity because the repository has only one Public implementation and no selected replacement technology.

## Current coupling audit

### Backend/API

Backend already exposes explicit Public controllers/contracts, including `/api/public/columns`, `/api/public/articles` and `/api/public/pages` / page-group routes. The Backend does not depend on Vue source structure for these APIs.

No Backend refactor is planned for the initial convergence slice unless implementation work proves that a Public response itself requires Admin-only state. Naming overlap alone is not enough reason to alter API contracts.

### Public source leakage

The current Public package includes confirmed Admin ownership leakage:

- `src/shared/api/articles.ts`
  - Admin Article list/get/create/update/publish/withdraw;
  - Admin Resource upload/get/content helpers;
  - Admin-oriented `CmsArticle`, `ArticleDraft`, `CmsResource` models mixed with Public Article models.
- `src/shared/api/columns.ts`
  - Admin list/create/update/delete and Admin `CmsColumn` / `ColumnDraft` mixed with Public reads.
- `src/shared/api/pages.ts`
  - Admin PageGroup/Page list/create/update/delete and Admin models mixed with Public Page reads.
- `src/shared/api/staticResources.ts`
  - pure Admin static-resource maintenance client inside Public source.

Other audited modules such as navigation, advertisements and site config currently consume public endpoints only. `lists.ts` consumes Public list endpoints, though its type names/fields shall be changed only when required by actual Public ownership—not for cosmetic renaming.

The implementation slice shall first prove whether the Admin-only exports above are unused by Public runtime source, then remove them surgically. If any is unexpectedly used, the usage must be classified before changing behavior; it shall not be silently retained as a Public responsibility.

## Source-boundary guard

Add one narrow automated check to the existing Public verification surface. It shall scan Public production source (`frontend/public-site/src/**`) and fail when a source file contains a dependency on `/api/admin/`.

The guard should assert the stable responsibility boundary rather than enumerate allowed Vue component paths. It shall not forbid `/api/public/`, `/static/`, Public Resource content or other accepted Public runtime contracts.

Preferred implementation is the thinnest mechanism already supported by the repository test/runtime toolchain—for example a small Playwright/Node source-boundary test—rather than introducing a lint framework solely for this rule.

## API adapter convergence

For each mixed module:

1. retain Public DTOs/functions that are actually imported by Main/Party/shared rendering;
2. remove Admin DTOs, drafts and mutation functions from the Public module;
3. remove helpers whose only purpose is Admin Resource/static-resource management;
4. ensure body/resource URL rewriting used for Public Article rendering continues to use public resource endpoints;
5. do not copy Admin helpers into another Public file merely to satisfy the guard.

Admin APIs remain owned by `frontend/admin`. Existing Admin adapters are the source for Admin UI behavior; D1 does not make Public source a shared API library for both frontends.

## Delivery / Gateway plan

No runtime delivery change is required for the initial convergence:

- keep the current Vite multi-entry build;
- keep current Main `index.html` and Party `party.html` outputs;
- keep current Nginx/Gateway route fallback;
- keep `public-site-dist` artifact and current combined runtime verification;
- keep `frontend/public-site` as the current logical module root.

These remain implementation adapters, not permanent product contracts. A future replacement candidate may change them provided it supplies equivalent canonical route/direct-access behavior and Current Evidence.

## CI / Review Environment plan

No generic wrapper is introduced in the first slice.

Current CI/Review Environment may continue to:

- set up Node for the current Public implementation;
- run package install/build/test under `frontend/public-site`;
- publish/consume the current Public build artifact;
- run existing black-box Public Browser suites.

Rationale: a wrapper that claims to be framework-neutral without a second toolchain would only relocate the current npm assumptions. The durable contract is the verified behavior and module responsibility, not a prematurely generic shell interface.

If a future replacement is actually selected, that candidate must either adapt these workflow steps or first introduce a concrete build/deploy adapter justified by the new toolchain.

## Verification strategy

The eventual implementation slice shall collect Current Evidence in this order:

1. static source-boundary guard: no `/api/admin/**` dependency under Public production source;
2. Public type-check/build;
3. Admin type-check/build to prove CRUD/resource ownership remains functional;
4. Backend tests/build to prove no API/domain regression;
5. integrated Public/Admin Browser verification;
6. targeted direct-access checks for Main/Party canonical routes already covered by current suites;
7. final diff audit confirming no Backend contract, Nginx/Gateway, content migration corpus or user-visible design change unless directly required by a discovered defect.

Because source cleanup changes frontend bytes, previous Runtime/Browser evidence becomes stale for the affected Public delivery and must be reacquired on the final implementation Head. Pure planning-doc changes do not invalidate Runtime Evidence.

## Slice recommendation

After this Technical Plan is accepted, `slice-work` should evaluate a single candidate vertical unit with the semantic goal:

> **Converge Public production source to Public-only API responsibility and enforce the boundary without changing observable Main/Party behavior.**

Expected implementation boundary:

- mixed Public API adapter cleanup;
- removal of pure Admin client residue from Public source;
- one automated responsibility-boundary guard;
- targeted Authority synchronization if implementation facts change;
- full affected frontend/integration verification.

The candidate should remain one unit unless implementation audit reveals a real Backend Public DTO redesign or delivery-adapter change that is independently valuable and cannot be verified safely in the same slice. Such a discovery must return to planning instead of expanding the unit opportunistically.

## ADR boundary

No new ADR is required for this first convergence because ADR-0002 remains accepted and D1 does not change its Multi-entry Modular SPA decision.

A new or superseding ADR is required only if later work selects a different rendering/deployment architecture (for example SSR/SSG/Hybrid, separate Main/Party runtimes, or a repository/service split).

## Rollback boundary

Before integration, the source-isolation feature branch can be abandoned with no data/schema effect. The planned implementation contains no migration or persistent-data change, so rollback after a failed pre-integration attempt is source-only.
