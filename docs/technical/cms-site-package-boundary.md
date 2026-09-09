# CMS Core / JilinJobs Site Package Boundary Technical Plan

## Authority

- `docs/requirements/cms-site-package-boundary.md`
- `docs/specifications/cms-site-package-boundary.md`
- GitHub Issue #77
- GitHub Issue #92
- `docs/technical/cms-architecture.md`
- `data-migrations/README.md`

## Status

- Technical Planning: **ACTIVE / ACCEPTED**
- Foundation completed: **EU-37～EU-42**
- Cross-boundary convergence: **EU-43～EU-48 + Phase 3 PASS**
- Current Main ownership correction: **Page + stable Main ListItem → Site Package**
- Current unimplemented gap: **stable ListItem package/reconcile capability**

## 1. Accepted runtime foundation

The accepted composition remains:

```text
Generic CMS Flyway schema
→ JilinJobs stable Site Package reconcile
→ optional one-time bootstrap
→ stable Site asset projection
→ optional Historical Article migration
→ Runtime / public contracts
```

Flyway remains Generic Schema-only and does not own JilinJobs instance values.

## 2. Current stable Site Package implementation

`backend/modules/cms-core/.../SitePackageProvisioning.kt` currently supports these structure types:

- columns;
- page-groups;
- pages;
- navigation-locations;
- navigation-items;
- site-config;
- lists;
- advertisement-slots.

Current `SitePackageDefinition` has corresponding collections and no `listItems` collection.

Stable package source is `sites/jilinjobs/structure/**`; stable assets remain `sites/jilinjobs/assets/**`.

## 3. Page implementation boundary

`SitePackagePage` includes stable identity/content fields:

- `groupAlias`;
- `alias`;
- `name`;
- `bodyHtml`;
- `renderMode`;
- `embedUrl`;
- sort/enabled/preset metadata.

The loader validates Pages and the provisioner reconciles them as package-owned structure. EU-49 later added operator-content guard/generic Page migration capability; that capability remains valid, but Main E3 no longer uses Page migration as ownership.

A later Site Package Page-content Unit may update `sites/jilinjobs/structure/pages.json` from accepted source handoff evidence, subject to the existing operator-content/precondition semantics.

## 4. Current ListItem implementation gap

Generic CmsList runtime currently supports `CmsListItem` with:

- parent list id;
- source type LINK / ARTICLE;
- optional article id;
- title/subtitle/url;
- image path/resource;
- open mode/sort/enabled/extra JSON.

However it has no package stable code/preset ownership contract.

Current Site Package only reconciles **CmsList definitions**. It cannot express/reconcile stable membership.

Therefore a stable Main ListItem implementation cannot be safely achieved by merely moving JSON files. It needs a designed Generic capability plus JilinJobs package data.

## 5. Bootstrap historical compatibility

Current `sites/jilinjobs/bootstrap/initial-data.sql` creates:

- one `HOME_CAROUSEL` ListItem;
- five `SITE_RELATED` ListItems;
- one `HOME_RECRUITMENT_PROMO` Advertisement.

`SitePackageBootstrapper` applies the artifact once and records completion in `cms_site_bootstrap_state`. After apply, these rows are ordinary operator-managed data and are intentionally not reconciled/resurrected.

EU-41 evidence remains historically valid. The latest Main ownership decision means the six stable Main ListItems now need a future transition plan out of this one-time lifecycle. Do not modify the bootstrap rows ad hoc in EU-50.

## 6. Required stable ListItem capability plan

A separately authorized Unit must define before implementation:

### Schema / identity

- a site-neutral stable identity strategy for package-owned ListItems;
- whether the identity needs a new nullable database field/index similar to provisioning-only Navigation code;
- uniqueness scope (global vs parent List code);
- ARTICLE target stable reference strategy without relying on runtime numeric ids in package authority.

### Package representation

- new structure type/schema (for example `list-items`, exact name to be decided by that Unit);
- parent `listCode` reference;
- LINK/ARTICLE fields;
- image/resource path policy;
- deterministic ordering/integrity metadata.

### Reconcile / operator ownership

- package-owned create/update semantics;
- what fields operators may edit and whether package upgrades overwrite them;
- delete protection/resurrection policy;
- conflict behavior when an ordinary Runtime row resembles a package row.

### Existing-site adoption

- map current bootstrap ListItems to future stable identities without duplicate creation;
- preserve accepted operator edits/deletes according to the chosen product policy;
- define whether adoption is automatic, explicit or requires a bounded compatibility migration.

### Verification

- Generic Core-only baseline;
- first package apply;
- second apply idempotency;
- Existing Site adoption;
- operator mutation/deletion cases;
- package version update;
- conflict/rollback;
- Public list output reconciliation.

## 7. Main E3 technical boundary

Current Main E3 tooling may collect Page/List source bytes but only Articles enter migration eligibility and future canonical import.

The Article promotion workflow must produce a separate Site Package handoff containing Page/List candidates and all problems. It must not delete Page/List evidence merely because those records are not migration eligible.

The old mixed destructive triage path is retired.

## 8. Historical Migration compatibility

Generic Content Migration can continue supporting Article/Page/List at the capability layer because Party/other accepted consumers may need them.

Current Main ownership is narrower: **Article only**. Generic capability does not override Product Authority.

Party canonical data and accepted compatibility must remain unchanged.

## 9. Stable asset boundary

`sites/jilinjobs/assets/**` remains the stable Site asset source owner. Main Article migration resources remain with Article units. Future Page/List source resources must be classified under Site Package content/asset authority by their dedicated Unit rather than mechanically copied from migration artifacts.

## 10. Current Gate

EU-50 may complete Article-only migration evidence/promotion. It must not implement the ListItem gap.

After the current ownership correction is integrated, Issue #77/current Repository Authority should decide when to form the bounded Site Package Page/List Unit. No historical EU Execute Authority is revived.
