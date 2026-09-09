# CMS Core / Site Package / Public Renderer 边界 Specification

## Authority

- `docs/requirements/cms-site-package-boundary.md`
- GitHub Issue #77
- GitHub Issue #92
- `docs/specifications/public-frontend-replaceability.md`
- `data-migrations/README.md`

## Status

- Specification: **ACCEPTED / ACTIVE**
- Foundation: **EU-37～EU-42 COMPLETED**
- Cross-boundary convergence: **EU-43～EU-48 + Phase 3 PASS**
- Current correction: **Main Page + stable Main ListItem → Site Package**
- Current capability gap: **stable ListItem package/reconcile**

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

Accepted provisioning primitives currently include stable identity/reconcile for Columns, PageGroups/Pages, Navigation, SiteConfig, CmsList definitions and AdvertisementSlots plus site-neutral bootstrap-state capability.

A future stable ListItem primitive, if introduced, must remain generic: the Core may define identity/reconcile mechanics, but actual Main membership belongs to the JilinJobs package.

## 3. JilinJobs Site Package

### 3.1 Versioned owner

`sites/jilinjobs/` is the versioned source owner for:

```text
manifest.json
structure/**     # stable structure/content
bootstrap/**     # truly one-time ordinary defaults only
assets/**        # stable site assets
```

### 3.2 Stable Page

Current stable identity is `(groupAlias? + alias)`.

`SitePackagePage` contains:

- `bodyHtml`;
- `renderMode`;
- `embedUrl`;
- sort/enabled/preset metadata.

Therefore Main Page content is a Site Package concern. Legacy Page discovery from EU-50 is a source handoff, not a Page migration unit.

The EU-49 operator-content guard remains an implementation constraint: reconcile/update semantics must not silently destroy operator-owned Page content.

### 3.3 Stable CmsList / ListItem

CmsList definition stable identity remains `code`.

For Main, the accepted product boundary now also requires stable ListItem membership to be package-owned. Current Site Package v1 cannot yet express that requirement because:

- `SUPPORTED_STRUCTURE_TYPES` has `lists` but no `list-items`;
- `SitePackageDefinition` contains list definitions but no ListItems;
- current runtime `CmsListItem` has no package stable code/preset ownership;
- current bootstrap ListItems are intentionally ordinary operator-managed rows after first install.

This is an explicit **capability gap**. It must be resolved by a separately planned Site Package Unit; Historical Migration is not a fallback.

## 4. One-time bootstrap

Bootstrap is for data whose desired lifecycle is:

```text
install once
→ ordinary operator-managed Runtime data
→ never reconciled/resurrected by package
```

Current historical bootstrap content:

- `HOME_CAROUSEL`: 1 ListItem;
- `SITE_RELATED`: 5 ListItems;
- `HOME_RECRUITMENT_PROMO`: 1 Advertisement.

EU-41 correctly established this behavior under the then-current boundary. The latest Main decision supersedes the *ownership classification* of stable Main ListItems, not the recorded historical implementation/evidence.

Until a new Unit defines transition semantics, the current implementation remains compatibility state; it must not be silently rewritten or treated as final stable ListItem support.

## 5. Historical Migration

Historical Migration owns provenance/fingerprint/import-oriented content.

Current Main E3 contract:

- INTERNAL Article;
- EXTERNAL_LINK Article;
- Article resources/attachments;
- collection/retry/error evidence.

Main Page and stable Main ListItem source findings are emitted as Site Package handoff evidence. They are excluded from Main import eligibility.

Party ListItem migration remains governed by Party's accepted authority and is not invalidated by the Main ownership correction.

## 6. Site Package stable identities

Current accepted identities include:

- Column → alias;
- PageGroup → alias;
- Page → groupAlias + alias;
- NavigationLocation → code;
- NavigationItem → provisioning code;
- CmsList → code;
- AdvertisementSlot → code;
- SiteConfig → key.

**ListItem stable identity is not yet defined.** A later Unit must define it before adding stable package representation/reconcile.

## 7. Required future ListItem design questions

A ready Site Package Unit must resolve at least:

1. stable identity field/derivation;
2. package JSON/manifest schema;
3. parent CmsList code reference;
4. LINK vs ARTICLE target representation;
5. source image/resource ownership;
6. package-owned vs operator-owned mutation semantics;
7. adoption of existing bootstrap rows without duplication/loss;
8. deletion/resurrection rules;
9. first apply / second apply / upgrade / conflict verification.

Do not infer these from Runtime numeric IDs or legacy source order alone.

## 8. Runtime composition

Accepted baseline remains:

```text
Generic Flyway schema
→ Site Package stable reconcile
→ optional one-time bootstrap where still applicable
→ stable asset projection
→ optional Historical Article migration
→ Runtime
```

A future stable ListItem capability would join stable reconcile, not Historical Migration, once separately authorized and implemented.

## 9. Replaceable Public Renderer

Public renderer consumes stable public APIs, routes, `/static/**`, SiteConfig, Navigation, Lists, Pages and Articles. It does not own their source definition.

Renderer replacement must not require replaying historical data or redefining Site Package ownership.

## 10. Verification

Current verification expectations:

- Generic Core-only DB contains no JilinJobs instance values;
- Site Package stable structure first/second apply is deterministic;
- Page identity/content semantics are package-owned with operator guards respected;
- bootstrap remains one-time/no-overwrite/no-resurrection for truly bootstrap-owned rows;
- Main migration eligibility is Article-only;
- Page/List handoff is complete and explicit;
- stable ListItem gap remains visible until separately closed;
- Party/Generic compatibility remains unaffected.

## 11. Current Gate

The next Site Package architecture work is **planning**, not implicit implementation under EU-50. Issue #77/current Repository Authority must form a bounded Unit before modifying schema/provisioning/list ownership.
