# CMS Core / Site Package / Public Renderer 边界 Specification

## Authority

- `docs/requirements/cms-site-package-boundary.md`
- GitHub Issue #77
- GitHub Issue #92
- `docs/specifications/public-frontend-replaceability.md`
- `data-migrations/README.md`
- `docs/requirements/main-stable-listitem-site-package.md`

## Status

- Specification: **ACCEPTED / ACTIVE**
- Foundation: **EU-37～EU-42 COMPLETED**
- Cross-boundary convergence: **EU-43～EU-48 + Phase 3 PASS**
- Main Page formal-content adoption: **EU-52 COMPLETED**
- Main ListItem ownership: **JilinJobs Site Package bootstrap data / EU-53 READY**

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

The layers may remain in one repository, but source ownership and lifecycle remain distinct.

## 2. Generic CMS Core

Core includes site-neutral schema/domain/API/provisioning/migration primitives. It must not embed JilinJobs product values.

Accepted provisioning primitives include stable structure for Columns, PageGroups/Pages, Navigation, SiteConfig, CmsList definitions and AdvertisementSlots, plus the site-neutral one-time bootstrap mechanism.

Main ListItem completion requires **no new Generic Core capability**. `cms_list_item` remains ordinary Runtime data populated by Site Package bootstrap SQL.

## 3. JilinJobs Site Package

`sites/jilinjobs/` is the versioned source owner for:

```text
manifest.json
structure/**     # stable structure/content
bootstrap/**     # one-time site initialization data
assets/**        # stable site assets
```

### Main Page

EU-52 completed current Main formal Page content and stable Page assets through Site Package authority.

### Main ListItem

Main ListItem initial data belongs to `sites/jilinjobs/bootstrap/initial-data.sql`.

Current Main scope includes:

- `HOME_CAROUSEL` Main carousel initialization;
- `SITE_RELATED` 5 reviewed links;
- `SITE_REGIONAL_GRADUATES` 31 reviewed links;
- `SITE_JILIN_UNIVERSITIES` 60 reviewed links.

The three SITE_LINKS groups consume the final EU-50 reviewed content decision from repository history. They do not become Historical Migration input.

After bootstrap, these rows follow the existing ordinary operator-managed ListItem lifecycle. Site Package does not reconcile or resurrect them on later startup.

## 4. One-time bootstrap

The accepted lifecycle is:

```text
install once
→ ordinary operator-managed Runtime data
→ no replay after cms_site_bootstrap_state records the bootstrapId
```

`SitePackageBootstrapper` already implements this contract. Therefore Main ListItem completion does not require stable ListItem identity, a new package structure type, runtime reconcile, adoption fingerprints or delete protection.

## 5. Party boundary

Party ListItems remain governed by Party migration/current Party Authority. In particular, `PARTY_CAROUSEL` is not duplicated into Main Site Package bootstrap data.

This Main correction does not change Party migration ownership or Runtime behavior.

## 6. Historical Migration

Historical Migration owns historical Articles/resources and migration provenance/fingerprint/import concerns.

Main Page and Main ListItem are not Article Historical Migration content. EU-50 may retain source-discovery evidence about them, but current product delivery is through Site Package Page content/assets and bootstrap SQL respectively.

## 7. Runtime composition

Accepted composition remains:

```text
Generic Flyway schema
→ Site Package stable structure reconcile
→ Site Package one-time bootstrap data
→ stable asset projection
→ optional Historical Article migration
→ Runtime
```

Main ListItems are inserted in the one-time bootstrap step.

## 8. Replaceable Public Renderer

Public renderer continues to consume Generic public APIs and does not own ListItem source definitions. Main SITE_LINKS continue through the existing list public contract; no Main-specific API is introduced.

## 9. Verification

EU-53 verification must prove:

- bootstrap SQL / manifest digest integrity;
- Fresh Site Main carousel initialization;
- SITE_LINKS counts 5 / 31 / 60 and accepted title/URL data;
- second bootstrap evaluation does not replay or duplicate data;
- no Party ListItem changes;
- existing Generic Core/Site Package/Public behavior remains compatible.

## 10. Current Gate

The previous proposal for a stable ListItem identity/reconcile subsystem is superseded. `slice-work` has formed **EU-53 — Main ListItem Bootstrap Completion** and `readiness-check` is **PASS**. Planning/Readiness Authority integrates first; Execute then uses only the existing bootstrap SQL mechanism and does not expand Generic schema/provisioning.
