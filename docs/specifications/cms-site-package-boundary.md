# CMS Core / Site Package / Public Renderer 边界 Specification

## Authority

- `docs/requirements/cms-site-package-boundary.md`
- GitHub Issue #77
- GitHub Issue #92
- `docs/specifications/public-frontend-replaceability.md`
- `data-migrations/README.md`
- `docs/requirements/main-stable-listitem-site-package.md`
- `docs/specifications/main-stable-listitem-site-package.md`

## Status

- Specification: **ACCEPTED / ACTIVE**
- Foundation: **EU-37～EU-42 COMPLETED**
- Cross-boundary convergence: **EU-43～EU-48 + Phase 3 PASS**
- Main Page formal-content adoption: **EU-52 COMPLETED**
- Main stable ListItem ownership: **Site Package**
- Main stable ListItem design: **Requirement / Specification / Technical Plan READY; implementation not yet authorized**

## 1. Four-layer contract

```text
Generic CMS Core
        ↓
JilinJobs Site Package
        ↓
Historical Content Migration
        ↓
Runtime CMS Data
        ↓
Replaceable Public Renderer
```

The layers may remain in one repository, but source ownership and lifecycle are distinct.

## 2. Generic CMS Core

Core includes site-neutral schema/domain/API/provisioning/migration primitives. It must not embed JilinJobs product values.

Accepted provisioning primitives include stable identity/reconcile for Columns, PageGroups/Pages, Navigation, SiteConfig, CmsList definitions and AdvertisementSlots plus site-neutral bootstrap-state and stable-asset capabilities.

The current Main ListItem Planning Authority extends the design with a site-neutral stable ListItem primitive:

- nullable ListItem stable code;
- stable logical identity `(list_id, code)`;
- optional Site Package `list-items` representation;
- guarded adoption/reconcile/report mechanics;
- stable-item deletion protection while ordinary null-code rows remain operator data.

The Generic primitive must not contain the concrete JilinJobs 96-member dataset.

## 3. JilinJobs Site Package

### 3.1 Versioned owner

`sites/jilinjobs/` is the versioned source owner for:

```text
manifest.json
structure/**     # stable structure/content/defaults
bootstrap/**     # truly one-time ordinary defaults only
assets/**        # stable site assets
```

### 3.2 Stable Page

Page stable identity remains `(groupAlias? + alias)`. EU-52 completed Main formal Page body/resource adoption with explicit prior-baseline guards; ordinary operator edits continue to survive reconcile.

Main Page formal content is Site Package content, not Historical Migration input.

### 3.3 Stable CmsList / ListItem

CmsList definition stable identity remains `code`.

For Main stable ListItems, current accepted design is:

```text
package identity = (listCode, itemCode)
runtime identity = (list_id, code)
```

`code = NULL` continues to mean ordinary operator-created ListItem. Non-null code is package stable membership identity. Stable identity is independent from numeric id, title, URL and sort order.

Current Main package scope is exactly the reviewed SITE_LINKS membership:

- `SITE_RELATED`: 5;
- `SITE_REGIONAL_GRADUATES`: 31;
- `SITE_JILIN_UNIVERSITIES`: 60;
- total: 96.

Concrete content remains JilinJobs Site Package data. Current Generic package capability is intentionally limited to stable `LINK` ListItems because no current Main requirement needs ARTICLE/image ListItem package projection.

## 4. Operator mutation boundary

Stable ListItem membership and identity are package-owned; ordinary mutable presentation/target payload remains operator-maintainable after create/adoption.

Therefore:

- package creates a missing stable identity from package defaults;
- package may adopt a known prior package/bootstrap row only through exact declared fingerprint;
- ordinary reconcile does not overwrite operator-mutated title/URL/sort/enabled/etc.;
- stable-coded rows cannot be deleted through ordinary Admin;
- operator-created null-code rows remain normal CRUD data and do not participate in stable reconcile;
- package omission is not a deletion instruction.

Future stable-member retirement requires explicit versioned Authority rather than absence-based deletion.

## 5. One-time bootstrap

Bootstrap remains for data whose desired lifecycle is:

```text
install once
→ ordinary operator-managed Runtime data
→ never reconciled/resurrected by package
```

Historical current bootstrap contains:

- `HOME_CAROUSEL`: 1 ListItem;
- `SITE_RELATED`: 5 ListItems;
- `HOME_RECRUITMENT_PROMO`: 1 Advertisement.

The Main stable ListItem plan now defines the transition:

- the five SITE_RELATED defaults leave bootstrap when stable `list-items` becomes active;
- Existing Sites adopt those five only from exact known prior-bootstrap fingerprints;
- HOME_CAROUSEL stays bootstrap/operator-managed in this scope;
- Advertisement bootstrap stays unchanged.

This preserves EU-41 bootstrap semantics for truly bootstrap-owned rows while moving only the separately authorized stable membership into Site Package structure.

## 6. Historical Migration

Historical Migration owns provenance/fingerprint/import-oriented historical content.

Current Main Article contract remains INTERNAL / EXTERNAL_LINK Article plus accepted resources and migration evidence. Main Page and stable Main ListItem are not Historical Migration fallback content.

Party ListItem migration remains governed by Party's accepted authority and is not changed by Main ListItem package ownership.

## 7. Site Package stable identities

Current accepted identities include:

- Column → alias;
- PageGroup → alias;
- Page → groupAlias + alias;
- NavigationLocation → code;
- NavigationItem → provisioning code;
- CmsList → code;
- **ListItem → parent list code + stable item code**;
- AdvertisementSlot → code;
- SiteConfig → key.

For the current 96 Main entries, explicit package item codes are assigned once to the frozen reviewed occurrences. They are never dynamically regenerated from legacy order, Runtime sort order, title or URL.

## 8. Runtime composition

Accepted target composition becomes:

```text
Generic Flyway schema
→ Site Package stable reconcile
     ├─ existing stable structure
     └─ optional stable list-items
→ optional one-time bootstrap for remaining ordinary defaults
→ stable asset projection
→ optional Historical Article migration
→ Runtime
```

Stable ListItems join stable reconcile once the separately planned implementation passes Readiness and is integrated. They never enter Historical Migration solely because EU-50 originally discovered them.

## 9. Replaceable Public Renderer

Public renderer continues to consume stable public APIs, routes, `/static/**`, SiteConfig, Navigation, Lists, Pages and Articles. It does not own source definitions or stable provisioning identity.

The current Main ListItem plan keeps `/api/public/lists/by-group/SITE_LINKS` as the public contract and introduces no Main-specific endpoint.

## 10. Verification

Current stable ListItem planning requires evidence for:

- append-only nullable ListItem stable identity;
- Generic package code containing no JilinJobs-specific member data;
- Site Package with/without optional `list-items`;
- exact Fresh Site 5 + 31 + 60 Main SITE_LINKS membership;
- guarded adoption of the historical five SITE_RELATED bootstrap rows;
- no heuristic ownership claim, no duplicate bootstrap creation;
- operator-edit preservation and stable-item delete protection;
- no absence-based deletion;
- Public SITE_LINKS rendering;
- HOME_CAROUSEL, Party, Page, Article/migration and full repository regression compatibility.

## 11. Current Gate

The stable Main ListItem architecture questions previously recorded here are now resolved by:

- `docs/requirements/main-stable-listitem-site-package.md`;
- `docs/specifications/main-stable-listitem-site-package.md`;
- `docs/technical/main-stable-listitem-site-package.md`.

They are READY Planning Authority but do not themselves grant Execute Authority. Current Ready Execution Unit remains **NONE** until this Planning Authority is integrated and `slice-work → readiness-check` completes against current `main`.
