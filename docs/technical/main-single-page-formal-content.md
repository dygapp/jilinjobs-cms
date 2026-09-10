# Main Single-page Formal Content Technical Plan

## Authority

- `docs/requirements/main-single-page-formal-content.md`
- `docs/specifications/main-single-page-formal-content.md`
- `docs/project/main-site-formal-content-plan.md`
- GitHub Issue #77

## Status

- Technical Plan: **READY**
- Planning baseline: `main@25e452ee3ce66c2d7da1000ad55e9570d732528a`
- Candidate scope: Main Page Formal Content Package Adoption
- Execute: **NOT STARTED / no Execute baseline**

## 1. Current implementation baseline

Current Page provisioning lives in:

`backend/modules/cms-core/src/main/kotlin/com/jilinjobs/cms/provisioning/SitePackageProvisioning.kt`.

EU-49 behavior is accepted and must remain the default:

- missing Page: CREATE uses package `bodyHtml/renderMode/embedUrl`;
- existing Page: ordinary structural reconcile does not compare or overwrite those three mutable content fields.

Current verification `backend/apps/cms-server/src/test/kotlin/com/jilinjobs/cms/provisioning/PageContentOwnershipVerification.kt` proves operator-maintained content survives ordinary reconcile.

Stable asset loading/projection remains in `SitePackageAssets.kt`; no second asset subsystem is introduced.

## 2. Generic adoption field

Extend `SitePackagePage` with one nullable field:

```kotlin
val contentAdoptionFromFingerprint: String? = null
```

Validation:

- null means no explicit existing-content adoption authority;
- non-null must match lowercase SHA-256 (`[0-9a-f]{64}`);
- the field is generic Site Package capability and must not encode Main aliases or source-system policy.

No Site Package schemaVersion bump is required if current JSON v1 evolution rules accept an additive optional field; if implementation verification proves that assumption false, stop and update the Technical Plan before changing schema semantics.

## 3. Shared Page content fingerprint

Implement or extract the smallest site-neutral helper for the accepted Page mutable-content fingerprint:

```text
SHA-256(UTF-8 exact JSON)
{"bodyHtml":<stored body>,"renderMode":<enum/string>,"embedUrl":<string-or-null>}
```

Key order is fixed exactly as above. Do not include Page id, group, alias, name, sort, enabled, preset or timestamps.

The helper must produce the same semantics as EU-49 Generic Page `expectedTargetFingerprint`; focused tests must prove the known EU-50 expected-target fingerprints for the 10 accepted Pages match the prior package baseline before package bodies are replaced.

## 4. Reconcile algorithm

Refactor `reconcilePage` only enough to keep structural and content decisions separate.

Pseudo-flow:

```text
resolve existing Page by stable identity
if missing:
  create full package Page
else:
  owned/preset check
  reconcile structural fields as today
  targetFingerprint = fingerprint(target body/render/embed)
  currentFingerprint = fingerprint(existing body/render/embed)

  if currentFingerprint == targetFingerprint:
      content = CURRENT_EQUALS_TARGET
  else if target.contentAdoptionFromFingerprint != null
       && currentFingerprint == target.contentAdoptionFromFingerprint:
      update body_html/render_mode/embed_url to target
      content = ADOPTED
  else:
      do not write content
      content = PROTECTED_DIVERGENCE
```

The SQL content update must be bounded to the three mutable fields and only execute in the exact baseline-match branch. Do not restore these fields to ordinary structural UPDATE SQL.

If structural fields also change, both changes occur in the same DB transaction used by current provisioner.

## 5. Provisioning report evolution

Keep existing `created/updated/unchanged/objects/columns` compatibility and add deterministic Page-content outcome evidence, preferably:

```kotlin
val protectedPageContent: List<String> = emptyList()
val adoptedPageContent: List<String> = emptyList()
```

Stable identity format must reuse the current `pageIdentity(groupAlias, alias)` representation or another already accepted deterministic representation.

Requirements:

- baseline-matched adoption appears in `adoptedPageContent` and contributes to `updated` when no incompatible existing counter semantics result;
- protected divergence appears in `protectedPageContent`, even if structure is otherwise unchanged;
- list ordering follows package Page order for deterministic output;
- CLI continues emitting a single `SITE_PACKAGE_PROVISION_REPORT <json>` record.

Do not fail the whole provision solely because operator content diverged; preservation + explicit report is the accepted ownership behavior. Validation/source-integrity errors still fail closed.

## 6. Main package content promotion

Execution consumes the exact accepted EU-50 Page evidence only after artifact/digest revalidation.

Target changes:

- replace the 10 accepted Page placeholder/default bodies in `sites/jilinjobs/structure/pages.json` with accepted source `bodyHtml/renderMode/embedUrl`;
- add `contentAdoptionFromFingerprint` using each accepted handoff `expectedTargetFingerprint` after verifying it matches the prior package content;
- rewrite accepted local resource references to final `/static/pages/**` URLs;
- bump package version and structure digest(s) using current manifest contract.

The source handoff's `sourceFingerprint` remains provenance/evidence; it is not a Runtime migration mapping.

## 7. Page asset promotion

Use existing `sites/jilinjobs/assets/manifest.json` and `SitePackageAssetProjector`.

For every accepted local Page resource:

1. recover exact bytes from the verified EU-50 artifact or the explicitly authorized bounded `budget` normalization source;
2. verify path/source relationship, expected size and SHA-256 before copying;
3. choose deterministic package source path under `assets/pages/**`;
4. choose runtime target under `/static/pages/**`;
5. add source/target/SHA-256 to existing asset manifest;
6. rewrite Page body to the target URL;
7. verify manifest/projector and protected-path behavior.

Prefer meaningful stable filenames when source/product semantics are clear, especially all 13 `budget` PDFs. Where source filename is not a stable product identifier, a deterministic digest-backed filename is acceptable. Never guess extension/content type from URL when bytes/evidence disagree.

## 8. Budget bounded normalization

The 8 Human-authorized legacy absolute PDF references are the only known special acquisition case.

Execution may normalize the accepted `zhjy.jilinjobs.cn:8080/group1/cms/**` source residue to the bounded current legacy source form authorized by Issue #77 for byte acquisition. It must record for each PDF:

- original legacy URL;
- normalized acquisition URL;
- acquired size/SHA-256;
- final package source path;
- final `/static/pages/budget/**` target.

Any unresolved/mismatched PDF blocks `budget` Page acceptance; do not remove the link or replace it with another document.

## 9. Source-evidence freshness gate

At future Execute baseline recovery, re-check artifact `10086056781`:

- exists and not expired;
- digest remains `sha256:66118e4f21bf7644db1c97e2a631eee5d4902410f167606286e1280293620494`;
- run/head provenance matches accepted EU-50 evidence.

Current GitHub metadata reports expiry `2026-09-16T02:42:04Z`. If it is unavailable at Execute recovery, this Technical Plan does not authorize silent substitution. Execute must remain blocked until an explicit bounded Page reacquisition produces equivalent auditable source evidence and Current Readiness is refreshed.

## 10. Verification implementation

Extend focused Site Package verification rather than creating a second provisioning engine.

Required synthetic cases:

- fresh Page create with target formal content;
- exact old-baseline adoption;
- target-already-current no-op;
- operator divergence preserve + identity report;
- structural update + protected content coexistence;
- adoption rerun idempotency;
- operator edit after adoption survives later reconcile;
- invalid adoption fingerprint rejected.

Add Main package-content verification that runs offline after bytes are promoted and proves:

- exact set of 10 Page identities;
- accepted source/target fingerprints recorded in Work evidence agree with package result;
- no unresolved `migration-resource://` token;
- no accepted package-local resource still points at legacy `/group1/cms/**`;
- all body-owned package targets exist in asset manifest and bytes match digest;
- all 13 `budget` PDF targets exist under `/static/pages/budget/**`;
- existing Site Package Asset Projection verification remains PASS.

Repository gates remain Backend, Site Package, Admin/Public/Integrated Browser and any directly affected focused verification. Exact-head Browser verification must visit all 10 Page routes before bounded Human Review.

## 11. Side effects and rollback

Planning/Readiness changes are documentation only.

Future Execute side effects are bounded to:

- generic Site Package provisioning model/report implementation;
- JilinJobs Page package bodies/version/digests;
- JilinJobs stable Page asset bytes/manifest;
- focused verification/workflow support if required.

No Flyway, Generic Historical Migration, canonical Article data, ListItem capability or frontend technology change is required.

Rollback is the complete implementation PR. Existing Runtime operator content is never rewritten unless its current fingerprint exactly matches the explicitly declared prior package baseline.

## 12. Technical readiness

The current implementation paths, accepted source identities/fingerprints, stable Page targets, asset projector and operator guard are directly inspectable. The only product ambiguity (`budget` PDF ownership) already has Human Authority. The bounded adoption mechanism above removes the remaining technical conflict without reopening Historical Migration ownership.

Technical Plan **READY** for `slice-work → readiness-check`.
