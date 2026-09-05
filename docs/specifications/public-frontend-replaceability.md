# Public Frontend Replaceability & Source Isolation Specification

## Authority

- `docs/requirements/public-frontend-replaceability.md`
- Issue #60 / D1 — Public Frontend Replaceability / Isolation
- Issue #60 D1 Candidate Audit comment `5553137311`
- `docs/architecture/decisions/ADR-0002-public-site-multi-entry-modular-spa.md`
- `docs/technical/public-site-frontend.md`

## Status

- Specification: Ready
- Technical Planning: required before slicing
- Execution Unit: not yet created

## Stable Contract Boundary

### Backend / data contracts

A Public implementation may be replaced without changing the following contracts merely because the rendering technology changes:

- `/api/public/**` public data endpoints;
- public Resource content/attachment access and `/static/**` resources;
- CMS Article/Page/List/Navigation/Site configuration identities needed by accepted public behavior;
- `bodyHtml` as the persisted Article/Page rich-text contract;
- canonical migration datasets and stable legacy identities/fingerprints;
- Main / Party content scopes.

D1 does not declare every field currently returned by a Public endpoint permanently immutable. Field-level cleanup remains allowed when separately justified, but replacement implementations must depend on public response models, never Admin API clients.

### Public URL contracts

Unless a separate Requirement changes routing, a replacement must preserve the accepted public route semantics:

- `/`
- `/column/:alias`
- `/articles/:id`
- `/pages/:alias`
- existing Page Group / Business Guide public routes defined by current Public routing authority
- `/party/`
- `/party/column/:alias`
- `/party/articles/:id`

Direct access/refresh shall continue to work for accepted routes. Main and Party continue to use their own content scope/theme identity.

### Admin contract

`frontend/admin` remains the owner of Admin CRUD/upload/maintenance interactions. Public source shall not own or export Admin clients merely because historical code once shared API helpers.

## Public Source Ownership

`frontend/public-site` remains the current logical Public module root. Keeping this logical root is not equivalent to permanently requiring Vue or Vite.

Within that module:

1. production source under `src/**` may call public runtime endpoints and load public/static assets;
2. production source shall not call `/api/admin/**`;
3. Admin CRUD models, drafts, mutation functions and static-resource maintenance clients shall not live in Public production source unless a separately accepted public feature demonstrably requires them;
4. shared Public API modules shall expose names/types reflecting public consumption rather than bundling unused Admin interfaces beside them;
5. Main/Party/shared ownership may continue inside the Public module while both entries are implemented together; source organization is implementation detail, whereas Main/Party external identities remain stable.

A static boundary check shall make accidental `/api/admin/**` reintroduction into Public production source fail verification.

## Delivery Boundary

Current delivery is a Multi-entry Vite build producing Main and Party entry documents consumed by the shared Gateway. This remains the accepted implementation during D1 convergence.

D1 distinguishes two layers:

### Stable delivery behavior

- one deployable Public frontend responsibility is served separately from Admin;
- Main and Party canonical public paths resolve successfully through the Gateway;
- `/api/**` and public static/resource traffic continue to reach Backend according to current runtime contracts;
- a Public build can be independently verified before it is combined with Backend/Admin in integration verification.

### Replaceable delivery implementation

The following are current adapter details, not permanent product/domain contracts:

- Node/npm as the Public toolchain;
- Vite;
- `index.html` / `party.html` file names;
- `dist/` internal bundle/chunk layout;
- current package-internal Playwright location;
- the exact Nginx fallback mechanism.

D1 does not add a generic build/deploy framework now. CI/Review may continue to invoke the current module directly until a real replacement requires a second implementation/toolchain. A future replacement is allowed to change these adapters while preserving Stable delivery behavior and Current Evidence obligations.

## Verification Boundary

### Replacement-stable evidence

The following tests/evidence express contracts a future implementation must continue satisfying:

- public API / runtime data behavior;
- canonical public URL direct access;
- Main/Party content-scope isolation;
- observable public interaction/visual requirements already accepted by current Authority;
- responsive behavior and page metadata/SEO obligations;
- historical/canonical content runtime reconciliation;
- external Review Environment access when that environment is required for the candidate.

These tests may physically remain under the current Public package. Their filesystem location does not define the contract.

### Implementation-specific verification

Vue component structure, Vue Router internals, Vite configuration and generated chunk names may be tested only when necessary to validate the current implementation. Such assertions shall not be promoted to replacement requirements.

## Content Migration Boundary

E1/E2/E3 shall consume or produce CMS-owned content/migration assets, not Public source-code artifacts.

In particular:

- source discovery/canonical datasets/importers/provenance remain independent of Vue components;
- migrated `bodyHtml`, resource relations, aliases, external links and route identities belong to CMS/public contracts;
- visual/runtime review can use the current Public implementation as Current Evidence without making that implementation a data authority;
- replacing Public rendering later must not require re-migrating accepted content solely because the framework changed.

## Acceptance Obligations

Before this Specification can produce a Ready Execution Unit, Technical Planning shall define the smallest concrete convergence slice. That slice must be able to prove at least:

1. no `/api/admin/**` client call remains in `frontend/public-site/src/**`;
2. Public build succeeds after Admin-only API/model cleanup;
3. existing Public Browser verification remains green with no user-visible behavior change;
4. Admin build/verification remains green and continues owning its CRUD/resource clients;
5. Backend/Public API contracts and Main/Party canonical routes are unchanged by source-ownership cleanup;
6. a repository-level/static guard detects a future Public production-source dependency on `/api/admin/**`;
7. no speculative framework/deployment abstraction or SSR/SSG decision is introduced;
8. final diff remains attributable to Public responsibility isolation, its verification and Authority synchronization.

## Deferred Decisions

The following remain explicitly deferred until real evidence requires them:

- SPA vs SSR vs SSG vs Hybrid;
- a replacement framework/toolchain;
- moving Public Browser tests outside `frontend/public-site`;
- introducing a generic build/deploy adapter shared by multiple implementations;
- changing `index.html` / `party.html` or Nginx fallback shape;
- repository/service splitting.
