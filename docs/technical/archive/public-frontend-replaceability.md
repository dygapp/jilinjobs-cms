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
3. move managed Article body-image Admin→Public URL translation into the Backend `PublicArticleDetail` projection so Public rendering no longer knows `/api/admin/**` Resource routes;
4. add an automated source-boundary guard that rejects `/api/admin/**` endpoint knowledge from Public production source;
5. preserve all current Public routes, observable runtime behavior, build artifact and Gateway behavior;
6. keep current CI/Review direct module invocation until a real second implementation/toolchain creates evidence that a generic build/deploy adapter is needed.

This plan deliberately treats toolchain/delivery abstraction as deferred work. Adding a framework-neutral build layer now would create speculative complexity because the repository has only one Public implementation and no selected replacement technology.

## Current coupling audit

### Backend/API

Backend already exposes explicit Public controllers/contracts, including `/api/public/columns`, `/api/public/articles`, `/api/public/resources` and `/api/public/pages` / page-group routes. The Backend does not depend on Vue source structure for these APIs.

The follow-up Readiness audit proved one concrete Public projection gap that justifies a narrow Backend change:

- Admin Article authoring inserts managed body images as `/api/admin/resources/{id}/content` and persists that HTML;
- `ArticleService.getPublic()` sanitizes the HTML but currently returns that managed URL unchanged;
- Public `publicBodyHtml()` must therefore know the Admin Resource route and translate it client-side;
- `CmsArticle.bodyImageResourceIds` already provides the accepted association set, and `PublicResourceController` already exposes `/api/public/resources/{id}/content` for published images.

Accordingly, the Backend public Article projection shall perform this translation for associated body images. This does **not** change persistence, Article identity, Resource identity or response shape. It removes an implementation leak from the Public client while preserving the observable browser request to the existing Public Resource endpoint.

No broader Backend DTO/domain refactor is justified by D1.

### Public source leakage

The current Public package includes confirmed Admin ownership leakage:

- `src/shared/api/articles.ts`
  - Admin Article list/get/create/update/publish/withdraw;
  - Admin Resource upload/get/content helpers;
  - Admin-oriented `CmsArticle`, `ArticleDraft`, `CmsResource` models mixed with Public Article models;
  - client-side `publicBodyHtml()` depends on the Admin Resource content route.
- `src/shared/api/columns.ts`
  - Admin list/create/update/delete and Admin `CmsColumn` / `ColumnDraft` mixed with Public reads.
- `src/shared/api/pages.ts`
  - Admin PageGroup/Page list/create/update/delete and Admin models mixed with Public Page reads.
- `src/shared/api/staticResources.ts`
  - pure Admin static-resource maintenance client inside Public source.
- `src/sites/main/api/staticResources.ts`
  - re-exports the pure Admin static-resource client from Main Public source.

Other audited modules such as navigation, advertisements and site config currently consume public endpoints only. `lists.ts` consumes Public list endpoints, though its type names/fields shall be changed only when required by actual Public ownership—not for cosmetic renaming.

Main Article/Column/Page views were audited and consume only their Public functions/types. Admin already owns independent Article/Column/Page/StaticResource clients under `frontend/admin/src/modules/cms/api/**`; therefore removing the Public-side Admin copies does not require moving Admin responsibility to a new module.

## Managed Article Resource projection

Implement the projection at the Backend Public Article boundary, after rich-text sanitization and before constructing the final `PublicArticleDetail.bodyHtml` value.

For every ID in `article.bodyImageResourceIds`:

- replace the exact managed Admin content route `/api/admin/resources/{id}/content` with `/api/public/resources/{id}/content` in the sanitized HTML;
- do not rewrite URLs for resource IDs outside the Article's associated body-image set;
- do not rewrite arbitrary external URLs or unrelated `/api/admin/**` text;
- keep Admin create/edit persistence unchanged;
- keep `bodyImageResourceIds` in the Public response unchanged.

Use the smallest local helper needed for this projection. Do not add a general-purpose HTML URL-rewrite framework or move rich-text persistence to a second representation.

After this Backend projection is in place, Public `publicBodyHtml()` shall no longer need Admin-route translation and can be removed or reduced to Public-only behavior.

## Source-boundary guard

Add one narrow automated check to the existing Public verification surface. It shall scan Public production source (`frontend/public-site/src/**`) and fail when a source file contains `/api/admin/` endpoint knowledge.

The guard should assert the stable responsibility boundary rather than enumerate allowed Vue component paths. It shall not forbid `/api/public/`, `/static/`, Public Resource content or other accepted Public runtime contracts.

Because managed Article image projection is moved to Backend, no compatibility exception for Admin Resource URL literals is required in Public source. This keeps the guard simple and durable.

Preferred implementation is the thinnest mechanism already supported by the repository test/runtime toolchain—for example a small Playwright/Node source-boundary test—rather than introducing a lint framework solely for this rule.

## API adapter convergence

For each mixed module:

1. retain Public DTOs/functions that are actually imported by Main/Party/shared rendering;
2. remove Admin DTOs, drafts and mutation functions from the Public module;
3. remove helpers whose only purpose is Admin Resource/static-resource management;
4. remove the Main `staticResources.ts` re-export whose only target is the pure Admin client;
5. consume `PublicArticleDetail.bodyHtml` as the already-public projection instead of translating Admin Resource URLs in the browser;
6. do not copy Admin helpers into another Public file merely to satisfy the guard.

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

1. targeted Backend tests for Public Article managed-image projection:
   - associated managed image URL becomes `/api/public/resources/{id}/content`;
   - unassociated/arbitrary URLs are not rewritten;
   - persisted/Admin Article HTML remains unchanged by Public reads;
2. static source-boundary guard: no `/api/admin/**` endpoint knowledge under Public production source;
3. Public type-check/build;
4. Admin type-check/build to prove CRUD/resource ownership remains functional;
5. Backend full tests/build;
6. integrated Public/Admin Browser verification, including existing managed Article body-image rendering;
7. targeted direct-access checks for Main/Party canonical routes already covered by current suites;
8. final diff audit confirming no Nginx/Gateway, content migration corpus, persistence representation or user-visible design change beyond the required Public projection responsibility correction.

Because source cleanup and Public response projection change delivery bytes/response content representation, previous Runtime/Browser evidence becomes stale for the affected Public delivery and must be reacquired on the final implementation Head. Pure planning-doc changes do not invalidate Runtime Evidence.

## Slice recommendation

After this corrected Technical Plan is accepted, `slice-work` should evaluate a single candidate vertical unit with the semantic goal:

> **Converge Public production source to Public-only API responsibility, project managed Article resources through the Backend Public contract, and enforce the boundary without changing observable Main/Party behavior.**

Expected implementation boundary:

- mixed Public API adapter cleanup;
- removal of pure Admin client residue/re-export from Public source;
- narrow Backend Public Article managed-resource projection;
- one automated responsibility-boundary guard;
- targeted Backend projection tests;
- targeted Authority synchronization if implementation facts change;
- full affected Backend/frontend/integration verification.

The candidate should remain one unit unless implementation audit reveals a broader Backend Public DTO redesign or delivery-adapter change that is independently valuable and cannot be verified safely in the same slice. Such a discovery must return to planning instead of expanding the unit opportunistically.

## ADR boundary

No new ADR is required for this convergence because ADR-0002 remains accepted and D1 does not change its Multi-entry Modular SPA decision. The managed-image Public projection is a responsibility correction inside the existing Backend/Public contract boundary, not a new rendering/deployment architecture.

A new or superseding ADR is required only if later work selects a different rendering/deployment architecture (for example SSR/SSG/Hybrid, separate Main/Party runtimes, or a repository/service split).

## Rollback boundary

Before integration, the source-isolation feature branch can be abandoned with no data/schema effect. The planned implementation contains no migration or persistent-data change. The Backend projection affects only Public response construction, so rollback after a failed pre-integration attempt is source-only and does not require data repair.
