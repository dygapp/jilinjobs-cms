# Public Frontend Replaceability & Source Isolation Requirements

## Status

- Candidate source: Issue #60 / D1
- Candidate audit: Issue #60 comment `5553137311`
- Readiness: clarified and ready for specification
- Scope: Public Frontend engineering responsibility and replacement boundary only

## Intent

The public website shall remain replaceable as an implementation without coupling formal CMS content, Backend domain behavior or Admin behavior to the current Vue/Vite source layout.

The goal is to make the stable public contract explicit before E1～E3 add more formal Main Site content. This requirement does **not** require an immediate rewrite of the current Vue Public Site and does not preselect SPA, SSR, SSG, Hybrid rendering or another framework.

## Requirements

1. **Public source ownership shall be public-only.** Source owned by `frontend/public-site` shall not contain Admin CRUD, Admin upload/resource management or other `/api/admin/**` client responsibilities that are not required to render the public website.
2. Public-facing API adapters and DTOs shall be distinguishable from Admin/domain implementation models. Public Frontend code shall consume the Backend's public contracts rather than depend on Admin endpoints or Admin-only workflow state.
3. Existing canonical public URL semantics shall remain stable, including Main routes and the `/party/` namespace. A future implementation replacement must preserve the accepted route identities unless a separate product/architecture change explicitly changes them.
4. Main and Party shall preserve their existing content-scope and theme identities. D1 shall not merge their product identities merely to simplify implementation.
5. Admin remains an independent SPA and responsibility boundary. D1 shall not move public rendering responsibilities into Admin or require Admin to share the Public implementation stack.
6. Formal CMS data, migrated historical content, Article/Page HTML contracts, Resource identities and migration datasets shall not depend on Vue component names, Vue Router internals, Vite bundle structure or other Public implementation details.
7. Public delivery and verification responsibilities shall be explicit. Current build artifact shape, Gateway fallback and CI/Review integration may remain as implementation adapters, but they shall not be treated as CMS domain contracts or content-migration requirements.
8. Public Browser/Contract verification shall continue to assert stable observable behavior—direct access, route identity, content visibility, responsive behavior, metadata and runtime data contracts—rather than require Vue/Vite internals.
9. E1～E3 Main Site content work shall target CMS data / Public contracts and accepted public behavior. Those content candidates shall not require knowledge of the current Public source tree beyond a presentation implementation consuming the stable contracts.
10. The current Public architecture remains the accepted Multi-entry Modular SPA until a separate architecture decision changes it. D1 shall not silently convert the site to SSR/SSG/Hybrid or select a replacement framework.
11. The convergence shall not change current user-visible Main/Party behavior, CMS product scope, Backend business semantics or Admin workflows.
12. New generic adapters, framework layers, services or repository splits shall be introduced only when current evidence demonstrates they reduce a real replacement boundary coupling. D1 shall not add speculative abstraction solely for a hypothetical future rewrite.

## Current evidence requiring convergence

Repository audit found Public source ownership leakage that violates the intended boundary:

- `frontend/public-site/src/shared/api/articles.ts` contains Public article access together with `/api/admin/articles` CRUD and Admin Resource upload/read helpers;
- `frontend/public-site/src/shared/api/columns.ts` contains Public column reads together with `/api/admin/columns` CRUD;
- `frontend/public-site/src/shared/api/pages.ts` contains Public Page reads together with `/api/admin/pages` and `/api/admin/page-groups` writes;
- `frontend/public-site/src/shared/api/staticResources.ts` is an Admin static-resource management client located entirely inside the Public package.

Repository audit also found delivery/verification coupling that must be classified correctly rather than mistaken for domain coupling:

- Vite currently produces `index.html` and `party.html` as two entries;
- Nginx/Gateway currently maps `/party/**` to `party.html` and serves the Public build artifact separately from Admin;
- CI and Review Environment directly know the `frontend/public-site` package, Node/npm build/test commands, Playwright source location and `public-site-dist` artifact.

These are current implementation/delivery facts. The Requirement is to keep them behind an explicit Public responsibility boundary, not to preserve each fact forever.

## Non-goals

- Rewriting the Public Site now.
- Choosing a new framework or rendering model.
- Resolving SSR / SSG / Hybrid Rendering from ADR-0002.
- Changing Main/Party information architecture or navigation (Issue #57 remains separate Future Discussion).
- Browser Compatibility work from Issue #59.
- Loading/Skeleton or Mobile Human Review work from Issue #60 C1/C2.
- Performing E1/E2/E3 content migration in this requirement.
- Redesigning Backend CMS domain APIs merely to make names aesthetically cleaner when no Public coupling problem is demonstrated.
- Splitting repositories or introducing additional deployment services without current evidence.
