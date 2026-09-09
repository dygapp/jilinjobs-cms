# CMS Core / Site Package / Public Renderer 边界需求

## Status

- Candidate source: GitHub Issue #77
- Stage: **Requirement Authority — ACTIVE / ACCEPTED BOUNDARY**
- Foundation completed: EU-37～EU-42
- Repository / migration convergence completed: EU-43～EU-48 + Issue #92 Phase 3 PASS
- Current Main ownership correction: **Page + stable Main ListItem belong to JilinJobs Site Package**
- Current Site Package capability gap: **stable ListItem identity / provisioning / reconcile**

## 1. Four-layer authority

The long-term boundary is:

```text
Generic CMS Core
        ↓ generic schema/domain/API/provisioning capability
JilinJobs Site Package
        ↓ stable site structure + stable site content + install/bootstrap + stable assets
Historical Content Migration
        ↓ historical Article/canonical provenance content
Runtime CMS Data
        ↓ public contracts
Replaceable Public Renderer
```

Logical/lifecycle ownership takes precedence over the fact that all layers currently live in one Git repository.

## 2. Generic CMS Core requirements

Generic CMS Core owns:

- site-neutral database schema and Flyway evolution;
- Article/Page/Navigation/List/Advertisement/SiteConfig/Resource domain capabilities;
- Admin/Public APIs and Generic migration/provisioning primitives;
- stable identity/reconcile mechanisms that are genuinely reusable;
- site-neutral bootstrap completion-state capability.

Generic Core must not hardcode JilinJobs aliases, list codes, navigation labels, Page text, URLs, contacts or site asset values.

If stable ListItem support is added, its schema/identity/reconcile mechanics must be generic. JilinJobs-specific membership values remain Site Package data.

## 3. JilinJobs Site Package requirements

`sites/jilinjobs/**` is the versioned authority for the JilinJobs site product definition.

### 3.1 Stable structure and content

Site Package owns at least:

- Columns;
- PageGroups and Pages, including accepted stable Page content/body defaults;
- NavigationLocations / NavigationItems;
- CmsList definitions;
- **stable Main ListItem membership**;
- AdvertisementSlots and any stable site placement content explicitly accepted as product definition;
- SiteConfig values;
- stable site assets;
- package identity/version/integrity metadata.

Stable objects/content must use repository-owned identities rather than Runtime numeric IDs and must have explicit reconcile/ownership semantics.

### 3.2 Page

Current Site Package v1 already supports stable `Page` identity and `bodyHtml`/render configuration through `sites/jilinjobs/structure/pages.json`. Main Legacy Page source content must therefore converge into Site Package authority rather than Historical Migration.

Operator-edit/reconcile semantics accepted by EU-49 remain relevant implementation constraints; a later Page-content update must respect the current ownership guard rather than blindly overwrite operator content.

### 3.3 Main stable ListItem

Current product authority now classifies Main homepage/site-link ListItem membership as stable Site Package content.

Current implementation does **not** yet satisfy this requirement:

- Site Package v1 supports stable `lists` definitions but no `list-items` structure type;
- `cms_list_item` has no package stable identity/preset ownership contract;
- current Main bootstrap ListItems are one-time ordinary Runtime defaults and are not reconciled.

A separately authorized Site Package Unit must define stable ListItem identity, representation, adoption/reconcile, operator mutation and upgrade/idempotency behavior before Main stable ListItem content can be considered fully provisioned.

## 4. Bootstrap requirements

One-time bootstrap remains valid for **truly initial ordinary operator data** that is not stable Site Package content.

Historical EU-41 evidence showed six Main ListItems + one Advertisement as one-time defaults. That was correct for the then-current boundary. The latest Main ownership decision supersedes that classification **for stable Main ListItem membership**.

The existing bootstrap implementation must not be silently reinterpreted as stable reconcile. Any transition of current ListItems out of bootstrap requires explicit Site Package planning and compatibility evidence.

## 5. Historical Content Migration requirements

`data-migrations/**` owns historical content that requires source provenance/fingerprint/import lifecycle.

For **Main E3**, current migration ownership is Article-only:

- INTERNAL Articles;
- EXTERNAL_LINK Articles;
- Article body images/attachments and migration evidence.

Main Page content and stable Main ListItem membership are not Historical Migration input. Legacy Source discovery may preserve them as Site Package handoff evidence.

Party or another scope may retain historical ListItem canonical data where its accepted authority explicitly requires that lifecycle; the Main correction does not retroactively rewrite accepted Party history.

## 6. Stable assets

Stable Site assets remain owned by `sites/jilinjobs/assets/**` with package identity, target path and digest integrity. Runtime uploads remain separate mutable storage.

Historical Article resources remain with the Article migration unit unless a separate product decision promotes an asset into stable Site ownership.

## 7. Replaceable Public Renderer

The current Vue public site is an implementation, not Site Authority.

Renderer replacement must be possible against stable public API/resource/site-data contracts without redefining Site Package or replaying Historical Migration merely because frontend technology changes.

## 8. Verification requirements

The repository must be able to prove:

1. Generic schema contains no JilinJobs instance rows before Site Package composition;
2. stable Site Package structure can be loaded/reconciled idempotently;
3. Page content ownership follows current Site Package + operator-guard semantics;
4. any future stable ListItem capability has deterministic identity/reconcile/adoption verification;
5. one-time bootstrap does not overwrite/resurrect ordinary operator data;
6. stable Site assets pass source/target/digest verification;
7. Historical Main migration imports Articles only under current E3 authority;
8. Page/List source evidence is handed off without silent loss;
9. Public Renderer remains replaceable through stable contracts.

## 9. Current planning finding

The current gap is not a reason to keep ListItems in migration. It is a real **Site Package capability gap** that must be planned from Issue #77/current Repository Authority before implementation.

Required future decisions include:

- stable ListItem identity;
- package schema/manifest type;
- list-code relationship;
- operator ownership/conflict policy;
- bootstrap-to-stable adoption/compatibility;
- reconcile ordering/idempotency;
- Fresh/Existing Site verification.

No existing EU Execute Authority is implicitly extended by this finding.

## 10. Non-goals

- no speculative multi-site framework;
- no forced repository split;
- no new Public frontend technology choice;
- no Page/List fallback into Historical Migration merely because Generic migration supports those Runtime mutations;
- no stable ListItem implementation without a separate ready Unit.
