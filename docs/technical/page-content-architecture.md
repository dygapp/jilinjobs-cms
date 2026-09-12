# Page Content Architecture Technical Plan

## Authority

- `docs/requirements/page-content-architecture.md`
- `docs/specifications/page-content-architecture.md`
- `docs/project/development-method.md`
- `docs/technical/main-single-page-formal-content.md`
- `docs/technical/verification-strategy.md`
- GitHub Issue #77 — Generic CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer boundary
- GitHub Issue #137 — durable Page-content acquisition / legacy structure evidence only; current product decisions are owned by the Requirement / Specification above

## Status

- Technical Plan: **READY**
- Planning baseline: `main@7fd5d4b45506f9eac570038c606db17feaba1ae2`
- Primary implementation target: `guide/jypq` Structured Page
- Stage Return: **NOT TRIGGERED**
- Candidate / Ready Execution Unit: **NONE at Technical Plan creation**
- Execute Authority: **NONE**

This plan coordinates a change that crosses Generic CMS persistence/domain/API, Site Package adoption, Admin authoring and the replaceable Public Renderer. That cross-unit HOW value is durable enough to justify a Technical Plan rather than leaving the decisions implicit in one implementation diff.

## 1. Current implementation evidence

Current Page persistence and runtime still encode three different concerns in one shape:

```text
bodyHtml
+ renderMode = RICH_TEXT | EMBED_PLACEHOLDER | INTERNAL_STATIC
+ embedUrl
```

`PageRenderMode` is persisted in `cms_page.render_mode`, exposed directly through Admin/Public DTOs and used by the current Public renderer. `PublicPageView.vue` special-cases only `EMBED_PLACEHOLDER`; all other values fall through to one `v-html` path. This is the mixed contract the accepted Specification requires us to evolve away from.

Current Site Package provisioning already provides the ownership safety that must be preserved:

- missing Page: create package content;
- ordinary reconcile of an existing Page: do not overwrite operator-managed content;
- explicit adoption: update only when current content exactly matches the declared prior-package fingerprint;
- operator divergence: preserve and report;
- rerun: idempotent.

The current `guide/jypq` package body is a flattened Rich Page containing **3 ordered legacy sections/cards**. The second section contains the **4 accepted package images** under `/static/pages/guide/jypq/**`. No current repository evidence proves interaction/state beyond this durable card structure and presentation.

## 2. Selected architecture

Choose **Option A — Page-owned, versioned Structured payload**.

The first Structured contract is a site-neutral ordered card collection. Page remains the aggregate and the single mutable content boundary; cards do not become independently addressable CMS entities.

Conceptual Page contract after the change:

```text
stable Page identity / grouping / canonical URL / lifecycle
+
contentModel
+
rendererKey
+
contentOwner
+
exactly one model-owned content representation
  - Rich: bodyHtml
  - Structured: structuredPayload
  - no CMS primary body: NONE / integration metadata
+
existing integration metadata when applicable (for example embedUrl)
```

The three accepted semantic dimensions therefore remain independent:

- `contentModel` answers what data shape the Page body has;
- `rendererKey` answers which registered renderer consumes that contract;
- `contentOwner` answers which authority owns the primary content after the current lifecycle transition.

They are intentionally not collapsed into a new profile enum or another mixed `renderMode`.

## 3. Alternatives considered

| Option | Advantages | Costs / risks | Verdict |
|---|---|---|---|
| **A. Page-owned versioned Structured payload** | Atomic Page ownership; minimal schema; preserves ordered structure; simple package default/adoption; no item lifecycle; schema/version fail-closed; Admin can edit one Page transactionally | No SQL-level card querying/indexing; item identity is not independently stable | **SELECTED** — current Requirement has no cross-page reuse, independent item identity or card query need |
| **B. Normalized `page_section` / `page_item` entities** | Stable row identity; direct item querying/indexing; natural item CRUD | More domain/repository/Flyway/API/Admin complexity; ordering and transactional adoption span rows; package bootstrap/adoption needs item identity and diff semantics; creates lifecycle not required by current target | **REJECTED for current scope** — over-designed for one Page-owned ordered collection |
| **C. One opaque `content_contract_json` containing model + renderer + owner + payload** | Small physical schema; flexible envelope | Hides three accepted orthogonal dimensions in an opaque blob; weakens audit/query/validation; makes renderer and ownership drift harder to detect; encourages an untyped page-builder contract | **REJECTED** |
| **D. Keep `renderMode` and add `STRUCTURED` / alias-specific dispatch** | Smallest immediate diff | Recreates the exact mixed semantic problem; encourages path/alias fallback and silent Rich rendering | **REJECTED** |

Normalized entities become a future option only if real Requirements require independently addressable items, cross-page reuse, item-level query/indexing, or independent item workflow. That evidence does not exist today.

## 4. Generic persistence and domain contract

### 4.1 Flyway evolution

Use the next append-only Generic Flyway migration. The migration must support both:

```text
Fresh Database -> V1 -> V2 -> V3 -> new migration -> current schema
```

and upgrade of an existing current database without rewriting operator content.

Evolve `cms_page` as follows:

```text
render_mode        -> renderer_key      VARCHAR(100) NOT NULL
ADD content_model                     VARCHAR(32)  NOT NULL
ADD content_owner                     VARCHAR(32)  NOT NULL
ADD structured_payload                LONGTEXT     NULL
```

The existing physical `render_mode` column is renamed rather than retained beside a second renderer column. Existing values remain stable renderer identities during the migration, which avoids creating two persisted renderer authorities:

```text
RICH_TEXT          -> rendererKey RICH_TEXT
EMBED_PLACEHOLDER  -> rendererKey EMBED_PLACEHOLDER
INTERNAL_STATIC    -> rendererKey INTERNAL_STATIC
```

Backfill the other dimensions deterministically:

```text
RICH_TEXT          -> contentModel=RICH_TEXT, contentOwner=OPERATOR
EMBED_PLACEHOLDER  -> contentModel=NONE,      contentOwner=EXTERNAL
INTERNAL_STATIC    -> contentModel=NONE,      contentOwner=ENGINEERING
```

`LONGTEXT` is preferred over database-native JSON for `structured_payload`: current behavior does not require SQL queries inside the payload, while application-level canonical parsing/validation keeps the contract portable and explicit.

### 4.2 Generic enums / values

Generic Core owns the finite semantic dimensions:

```text
PageContentModel = RICH_TEXT | STRUCTURED | NONE
PageContentOwner = OPERATOR | SITE_PACKAGE | ENGINEERING | EXTERNAL
```

`rendererKey` remains a validated string, not a Generic Core enum, because renderer registration belongs to the replaceable Public Renderer and may be site-specific. Generic validation owns only syntax/length, not membership in a concrete frontend registry.

The first implementation uses `OPERATOR` for both normal Rich Pages and adopted `guide/jypq`. `SITE_PACKAGE` remains a valid ownership concept but is not used to claim ongoing ownership of `guide/jypq`: the package is the accepted default/adoption source; after create/adoption, Runtime operator data is the primary authority.

### 4.3 One primary content representation

Validation must make ownership of the Page body unambiguous:

- `RICH_TEXT`: `bodyHtml` is the body authority; `structuredPayload` must be null;
- `STRUCTURED`: `structuredPayload` is the body authority; top-level `bodyHtml` must be empty; `embedUrl` must be null for the current Structured profile;
- `NONE`: no CMS field may be interpreted as a primary whole-page body; existing placeholder/internal metadata remains behavior-preserving compatibility data until those profiles receive separate product planning.

A Structured Page must never simultaneously render `structuredPayload` plus an old Rich `bodyHtml` fallback.

## 5. Structured Card Collection V1

The first site-neutral Structured schema is deliberately narrow:

```json
{
  "schemaVersion": 1,
  "kind": "CARD_COLLECTION",
  "items": [
    {
      "title": "plain text title",
      "bodyHtml": "<p>sanitized item-level rich content</p>"
    }
  ]
}
```

Contract:

- array order is card presentation/order authority;
- `title` is plain text and required;
- `bodyHtml` is item-level Rich content and is sanitized through the existing Rich HTML policy;
- no persisted item ID is introduced because current cards have no independent URL, workflow, references or query identity;
- no separate resource array is introduced in V1. Images/files remain references inside the item-level Rich body and use the existing managed/static resource path policy;
- unknown `kind`, unknown `schemaVersion`, malformed JSON or invalid item structure fails closed; it is never coerced to Rich HTML;
- schema evolution that cannot be backward-read requires a new supported schema version and explicit adoption/migration decision.

For `guide/jypq`, V1 contains exactly the 3 accepted ordered card titles/bodies recovered in current package evidence. The existing 4 `/static/pages/guide/jypq/**` images remain inside the appropriate item body and keep their current package asset authority.

This is not a general page-builder schema. New block kinds/layout composition are out of scope until a real Requirement proves them necessary.

## 6. Renderer identity and resolution

### 6.1 Stable renderer keys

Existing values continue as renderer identities for behavior compatibility:

```text
RICH_TEXT
EMBED_PLACEHOLDER
INTERNAL_STATIC
```

The JilinJobs Site Package binds `guide/jypq` to one new site-specific stable renderer identity, for example:

```text
JILINJOBS_GUIDE_CARDS
```

The final spelling may change during implementation only if all package/API/registry/tests use one stable value; it must not encode the Page alias as a dispatch condition.

### 6.2 Registry boundary

The Main Public Renderer owns a small static registry/resolver, conceptually:

```text
rendererKey -> renderer component
```

It is not a dynamic plugin framework and does not add runtime discovery or generic plugin loading.

- `RICH_TEXT` resolves to the generic Rich renderer;
- existing placeholder/internal identities resolve to their behavior-preserving renderers;
- `JILINJOBS_GUIDE_CARDS` resolves to the JilinJobs Structured card renderer;
- unknown key renders an explicit unsupported/error state and records diagnosable evidence; it must not fall through to `v-html`.

Alias, path, group, DOM shape or body heuristics are forbidden as renderer selectors.

Generic Core validates renderer-key syntax. The replaceable Public Renderer validates registration at consumption time. Site Package verification proves every renderer key used by the JilinJobs package is registered by the current Main renderer.

## 7. Content ownership and Site Package adoption

### 7.1 `guide/jypq` lifecycle

The valid lifecycle is:

```text
Site Package accepted Structured default
-> Fresh create OR explicit exact-baseline adoption
-> Runtime row contentOwner=OPERATOR
-> operator-managed Structured payload
-> Public structured renderer
```

There is no long-lived runtime state in which the following three surfaces all claim the Page body:

```text
structuredPayload + old top-level bodyHtml + Vue hardcode
```

The renderer contains presentation logic only. Card titles/body/resources live in Runtime Structured data after create/adoption; package data is the accepted baseline/default and does not remain a second runtime body authority.

### 7.2 Existing Rich baseline before adoption

The current Rich `guide/jypq` package baseline remains the only accepted adoption predecessor.

The existing EU-52 `contentAdoptionFromFingerprint` contract is preserved for current Rich predecessors. Because existing renderer string values are retained when `render_mode` becomes `renderer_key`, the accepted legacy fingerprint remains computable exactly as:

```text
SHA-256(UTF-8 exact JSON)
{"bodyHtml":<stored body>,"renderMode":<legacy renderer string>,"embedUrl":<string-or-null>}
```

For `guide/jypq`, adoption is allowed only when the existing row is also a lossless legacy-Rich state:

```text
contentModel = RICH_TEXT
rendererKey = RICH_TEXT
contentOwner = OPERATOR
structuredPayload = null
legacy content fingerprint = declared prior-package fingerprint
```

If any precondition fails, preserve the complete current content contract and report protected divergence.

### 7.3 Atomic target adoption

When the precondition matches, one transaction replaces the complete mutable content contract:

```text
contentModel
rendererKey
contentOwner
bodyHtml          -> empty for Structured
structuredPayload -> accepted CARD_COLLECTION V1
embedUrl          -> null
```

Structural Page fields continue to follow the existing structural reconcile policy.

After adoption:

- an unchanged rerun is a no-op;
- operator edits to the Structured payload survive ordinary reconcile;
- a later package mismatch is protected/reported unless a future explicit adoption contract is separately authorized;
- Main Historical Migration is not consulted as fallback.

A future Structured->Structured package adoption fingerprint/version is intentionally not invented now. If such a product need appears, it receives its own versioned adoption contract instead of silently reinterpreting EU-52's legacy Rich fingerprint.

## 8. Site Package boundary

The Page structure contract changes enough that JilinJobs Site Package should move to the next package schema version rather than keep a misleading mixed `renderMode` field.

Page entries become conceptually:

```json
{
  "groupAlias": "guide",
  "alias": "jypq",
  "name": "就业派遣",
  "contentModel": "STRUCTURED",
  "rendererKey": "JILINJOBS_GUIDE_CARDS",
  "contentOwner": "OPERATOR",
  "bodyHtml": "",
  "structuredPayload": { "schemaVersion": 1, "kind": "CARD_COLLECTION", "items": [] },
  "embedUrl": null,
  "contentAdoptionFromFingerprint": "<exact accepted prior Rich baseline>",
  "sortOrder": 10,
  "enabled": true,
  "preset": true
}
```

Site-neutral package loader/provisioner owns these generic fields and their validation. JilinJobs-specific content and `JILINJOBS_GUIDE_CARDS` binding live only in `sites/jilinjobs/**` and Main Public renderer code.

All existing JilinJobs Page entries move from `renderMode` to `rendererKey` under the new package schema without changing their accepted product content. Package version, manifest structure digest and affected verification fixtures must be updated together.

## 9. Admin authoring

Admin identifies the authoring surface from `contentModel` and supported Structured schema, never from alias/path.

Minimum behavior:

- `RICH_TEXT`: keep the current mature `RichTextEditor` whole-body surface;
- `STRUCTURED + CARD_COLLECTION V1`: show an ordered card editor;
- each card exposes required title and item-level `RichTextEditor` body;
- operator can add/remove/reorder cards and maintain body resources through the existing Rich editor/resource path behavior;
- Structured editing never exposes a parallel arbitrary whole-page HTML editor;
- preset `guide/jypq` does not allow an ordinary content edit to switch content model, renderer identity or ownership;
- unsupported content model/schema/version shows a blocking error/read-only diagnostic rather than a fallback editor;
- invalid Structured payload is rejected before persistence with actionable validation errors.

The current Page create/edit UI may keep behavior-preserving profile choices for existing Page types through a thin mapping to the orthogonal contract. It must not introduce a new persisted profile enum.

No new Page Resource relation is introduced. `RichTextEditor` remains the item-body editor adapter; the Structured Page architecture does not fork a second rich-text engine.

## 10. Admin/Public API compatibility

### 10.1 DTO evolution

Add explicit fields to Admin/Public Page contracts:

```text
contentModel
rendererKey
contentOwner
structuredContent (typed object or null at the HTTP boundary)
```

Existing Page identity, name, group, breadcrumbs and `canonicalUrl` remain unchanged.

For existing Rich / placeholder / internal Pages, current wire-visible `bodyHtml`, `renderMode` and `embedUrl` fields remain available during a compatibility window. `renderMode` becomes a deprecated projection derived from `rendererKey` for the three existing legacy identities; it is not accepted as the new domain dispatch authority.

Structured clients consume the new fields. `structuredContent` is emitted as validated JSON data, not an opaque JSON string.

### 10.2 Compatibility scope

- Existing Rich Page responses remain behavior- and field-compatible for current consumers.
- Current Admin and Public applications are upgraded in the same vertical implementation before `guide/jypq` Structured adoption is made live.
- A historical client that does not understand Structured Pages is not allowed to define a Rich fallback contract. Deployment/integration verification must prevent a mixed-version rollout in which Structured data becomes live before the current renderer can consume it.
- Updated Public code explicitly fails closed for unknown `contentModel`, Structured kind/version or `rendererKey`.

The deprecated `renderMode` DTO projection can be removed only under a later explicit compatibility decision; its persistence column is not retained as a second truth.

## 11. Public rendering and canonical routing

Routes remain exactly:

```text
/page/{alias}
/page/{groupAlias}/{alias}
```

`/page/guide/jypq` remains canonical.

`PublicPageView.vue` remains the route/shell owner and delegates the body to renderer resolution. Group tabs, breadcrumbs and Page shell remain generic Page behavior. The Structured renderer receives only the validated Page contract and contains no hard-coded card titles, source facts or duplicate content.

SEO/summary logic for Structured Pages derives text from the validated Structured contract rather than reading an empty top-level `bodyHtml`.

## 12. Historical Migration boundary

No Generic or Main Historical Migration fallback is added.

- current Main historical migration remains frozen;
- `cms_page_legacy_mapping` continues as historical provenance/mapping only;
- Page content shape evolution is implemented through Generic Flyway + Site Package default/adoption;
- missing/unsafe adoption is preserved and reported, not repaired through migration.

If future historical evidence is needed only to validate provenance, it remains evidence and does not become a second runtime content source.

## 13. Verification strategy

Implementation must extend current repository verification rather than relying on the docs-only Planning PR behavior.

### 13.1 Generic persistence/domain/API

Automated cases must prove:

- fresh database applies the complete active Flyway chain including the new Page contract migration;
- upgrade fixture maps each existing legacy renderer value deterministically without changing body/operator data;
- Rich Page create/read/update sanitization remains correct;
- Structured Card Collection V1 parse/validation/canonical serialization;
- unknown model / owner / payload kind / payload version fails closed;
- Structured validation forbids simultaneous top-level Rich body authority;
- Admin/Public DTOs preserve existing Rich compatibility and expose typed Structured data;
- canonical Page URL/group/breadcrumb behavior is unchanged.

### 13.2 Renderer dispatch

Automated Public verification must prove:

- `RICH_TEXT` resolves to Rich rendering;
- `JILINJOBS_GUIDE_CARDS` resolves to the Structured card renderer;
- unknown renderer identity produces the explicit unsupported state;
- there is no alias/path/DOM fallback route to any renderer;
- unsupported Structured schema/version cannot fall through to Rich rendering.

### 13.3 Admin authoring

Automated Admin/browser verification must prove:

- `about` still opens/saves through the mature Rich editor;
- Structured `guide/jypq` opens only the card authoring surface;
- operator can change card title/body and reorder cards;
- item-level Rich content/resource insertion follows the existing editor policy;
- no top-level arbitrary HTML authoring surface is concurrently available;
- invalid/unsupported payload is blocked with a visible diagnostic.

### 13.4 Site Package and adoption

Focused provisioning verification must cover:

- Fresh create directly creates the Structured target;
- exact current prior-package Rich baseline adopts to Structured;
- operator-diverged Rich baseline is preserved and reported;
- target-already-current is a no-op;
- adoption rerun is idempotent;
- operator Structured edit after adoption survives ordinary reconcile and is reported as protected divergence when package differs;
- structural reconcile can coexist with protected content;
- current EU-52 legacy fingerprints for unaffected Rich Pages remain valid under the renderer-column rename;
- Main Historical Migration is never called as fallback.

### 13.5 `guide/jypq` product contract

Current exact content verification must prove:

- `guide/jypq` Structured payload has **3 cards**, in accepted order;
- all 3 accepted titles and corresponding body content are preserved;
- the existing 4 accepted package image URLs remain present in the correct card body and bytes remain package-manifest backed;
- package manifest/version/digests are internally consistent;
- canonical route is exactly `/page/guide/jypq`;
- the public page renders the 3 cards through renderer dispatch, not a Rich fallback.

### 13.6 Repository gates / runtime review

Because implementation changes backend schema/domain/API plus both frontends and Site Package, minimum Current Evidence is:

```text
Backend Verify
+ fresh Flyway / upgrade verification
+ Site Package focused verification
+ Admin build (vue-tsc + Vite)
+ Public build (vue-tsc + Vite)
+ relevant backend/frontend behavior tests
+ Integrated Browser / Review Environment
+ bounded Human Review after automated evidence
```

Browser verification must include at least:

- Rich regression: `about`;
- Structured route: `/page/guide/jypq`;
- card count/order/titles/body visibility;
- all 4 existing package images;
- Admin Structured edit/reorder/save/reload;
- renderer unknown/fail-closed test path using controlled test data;
- canonical navigation/group behavior.

Automated evidence is collected before Human Review. Human Review checks the real card presentation and Rich regression; it does not replace machine-checkable content/adoption assertions.

## 14. Stage Return conditions

Stop Technical Planning / implementation continuation and return to Specification + bounded Human Review if new current evidence proves any of the following:

- `guide/jypq` requires repository-owned interaction/state/behavior that Card Collection V1 cannot naturally express;
- a Structured Page needs independent item workflow/identity/query semantics that invalidate Page-owned atomic payload;
- accepted Product semantics conflict with operator-owned Structured Runtime data;
- a valid runtime state would require both Structured payload and top-level Rich/renderer hardcode to claim the body;
- exact prior-package adoption cannot preserve operator divergence safely;
- the proposed Public renderer must dispatch by alias/path/DOM shape to satisfy the real behavior.

Do not solve any of these in implementation with hidden special cases.

## 15. Explicit non-decisions

This Technical Plan does **not** decide or authorize:

- a generic Page builder/block framework;
- normalized card/section entities without new evidence;
- FAQ automatic migration to Structured;
- a new External/iframe production integration contract;
- an Engineering Page sample or repository-owned workflow;
- arbitrary operator switching among content model / renderer / owner combinations;
- a future Structured->Structured package adoption fingerprint protocol;
- a new Page Resource relation;
- public frontend technology replacement;
- Main historical migration reactivation;
- production deployment/release.

## 16. Slice-work assessment

The Technical Plan is sufficiently concrete for slice-work: Requirement and Specification are accepted; persistence/domain/API ownership, renderer resolution, Admin authoring, Site Package adoption, fail-closed behavior and verification obligations are all bounded.

The natural vertical outcome is one end-to-end capability:

> An existing/fresh JilinJobs instance can safely represent, author and render `guide/jypq` as an operator-owned Structured Card Page, while Rich Pages remain compatible and Site Package adoption preserves operator divergence.

Whether this becomes one or multiple Candidate Execution Units is decided by post-integration `slice-work` against the then-current `main` and Current Evidence. This document itself creates no Candidate/Ready Unit and no Execute Authority.
