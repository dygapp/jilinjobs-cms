# Main Single-page Formal Content Specification

## Authority

- GitHub Issue #60 / Main Page Site Package follow-up
- GitHub Issue #77
- `docs/requirements/main-single-page-formal-content.md`
- `docs/project/main-site-formal-content-plan.md`
- `docs/specifications/cms-site-package-boundary.md`

## Status

- Specification: **READY**
- Technical Planning: **REQUIRED / READY in companion Technical Plan**
- Planning baseline: `main@25e452ee3ce66c2d7da1000ad55e9570d732528a`
- Implementation lifecycle: **COMPLETED via EU-52 / Execute Authority TERMINATED**

## 1. Accepted package projection

The Main Page follow-up consumes only the 10 accepted EU-50 Page handoff records and projects them into the JilinJobs Site Package:

| Source identity | Stable target |
|---|---|
| `main-page:about` | `(null, about)` |
| `main-page:budget` | `(null, budget)` |
| `main-page:teacher-library` | `(null, teacher-library)` |
| `main-page:employment-report-contact` | `(null, employment-report-contact)` |
| `main-page:guide-contact` | `(guide, contact)` |
| `main-page:guide-dagl` | `(guide, dagl)` |
| `main-page:guide-faq` | `(guide, faq)` |
| `main-page:guide-dygl` | `(guide, dygl)` |
| `main-page:guide-jypq` | `(guide, jypq)` |
| `main-page:guide-xlrz` | `(guide, xlrz)` |

Every target already exists as a stable preset Page in current `sites/jilinjobs/structure/pages.json`. This Unit does not create new product Page identities.

## 2. Package content representation

For each accepted Page, `sites/jilinjobs/structure/pages.json` becomes the long-term Site Package authority for the accepted formal `bodyHtml / renderMode / embedUrl` create-time default.

The Page package definition may add one optional, generic adoption field whose semantics are frozen as:

```text
contentAdoptionFromFingerprint: <sha256 or null>
```

The fingerprint is the SHA-256 of the same canonical Page-content serialization already used by the accepted Generic Page target-precondition contract:

```json
{"bodyHtml":<stored body>,"renderMode":<enum name>,"embedUrl":<string-or-null>}
```

with fixed key order and exact UTF-8 bytes.

This field is not a permanent overwrite flag. It identifies exactly one prior package-owned content baseline that may be upgraded to the new package content.

## 3. Provisioning classification

For a missing Page, provisioner behavior remains CREATE with full target package content.

For an existing Page, the provisioner resolves stable identity and independently classifies structural and mutable-content changes.

Content classification:

- `CURRENT_EQUALS_TARGET`: Runtime mutable content already equals target package content; no content write.
- `CURRENT_EQUALS_ADOPTION_BASELINE`: `contentAdoptionFromFingerprint` is declared and equals current Runtime content fingerprint; update only `body_html / render_mode / embed_url` to target package content.
- `PROTECTED_DIVERGENCE`: target differs, adoption baseline is absent or does not match; preserve Runtime content and report the Page identity.

Structural `group/name/sortOrder/enabled/preset` reconciliation continues under the existing Site Package contract regardless of content classification.

A Page may therefore have structural UPDATE while its operator content is protected. Report output must preserve this distinction.

## 4. Provisioning report

The machine-readable `SITE_PACKAGE_PROVISION_REPORT` must make protected content observable. At minimum it adds a stable collection such as:

```text
protectedPageContent: [<stable page identity>...]
```

Equivalent naming is allowed, but it must be deterministic, machine-readable and identity-specific. A divergence must not be represented only as generic `unchanged`.

Successful baseline adoption may continue to count as `updated`; no global result-status redesign is required.

## 5. Adoption lifecycle

For each Page included in this formal-content release:

1. source handoff's `expectedTargetFingerprint` is verified against the prior package definition/current accepted baseline before it is used as `contentAdoptionFromFingerprint`;
2. new formal content is written into the package definition;
3. Fresh Site gets the new content on CREATE;
4. Existing Site still at the old package baseline is upgraded once;
5. Existing Site with operator divergence is preserved and reported;
6. rerun after successful adoption sees target content and is idempotent;
7. operator edits after adoption survive later ordinary reconciles.

Future formal-content changes require a new explicit adoption baseline or a separate Requirement. The provisioner must not infer one automatically.

## 6. Source promotion boundary

The durable source-handoff locator is `data-migrations/main/v1/reports/site-package-handoff.json`; exact Page bodies/resources are recovered from the accepted EU-50 attempt-3 artifact only after verifying:

- run `34303771704`;
- artifact `10086056781`;
- artifact digest `sha256:66118e4f21bf7644db1c97e2a631eee5d4902410f167606286e1280293620494`;
- source Head `a96cee22449508f92f3c89789f99aad477286a66`;
- each Page source fingerprint, target identity and expected-target fingerprint agree with the durable handoff/evidence.

Artifact expiration or digest mismatch invalidates the execution input. No implicit fallback source is allowed.

Once accepted Page bodies/resources are integrated into `sites/jilinjobs/**`, the repository package bytes and manifests become long-term product authority; the workflow artifact remains provenance only.

## 7. Resource projection

Accepted Page resources are copied into page-scoped package paths:

```text
sites/jilinjobs/assets/pages/<page-scope>/<stable-name-or-digest-backed-name>
          ↓ existing asset manifest/projector
/static/pages/<page-scope>/<stable-name-or-digest-backed-name>
```

Rules:

- source bytes must match accepted size/SHA-256 evidence;
- `bodyHtml` resource references are rewritten deterministically to package `/static/pages/**` URLs;
- no `migration-resource://` token remains in final package body;
- no accepted local resource continues to depend on legacy host URLs;
- asset manifest covers every package-owned Page resource and current protected-path semantics apply;
- no target uses `/static/uploads/**` or `/static/migrated/content/**`.

For `budget`, all 13 PDFs are included. The 8 legacy absolute `zhjy.jilinjobs.cn:8080/group1/cms/**` references may be reacquired only through the bounded normalization authorized on Issue #77. Their final names must be stable and meaningful, and final body links must use `/static/pages/budget/**`.

## 8. Source anomaly contract

Execution is fail-closed for Page scope when any accepted Page has:

- missing/ambiguous stable target;
- source/artifact fingerprint mismatch;
- missing resource bytes;
- size/SHA-256 mismatch;
- unclassified external resource that is required for the accepted Page;
- a new source-defect category not already covered by Authority.

Such findings are reported and do not permit silent resource deletion, HTML repair, inferred replacement, or partial acceptance of that affected Page.

This Page anomaly handling does not change the separate 230 + 6 Article boundary.

## 9. Verification contract

Automated verification must cover:

- loader validation for adoption fingerprint format;
- fresh create formal content;
- baseline-matched content adoption;
- protected divergence identity reporting;
- structural reconcile with protected content;
- second-run idempotency;
- post-adoption operator edit preservation;
- all 10 Page identities and accepted source fingerprints;
- package body contains no unresolved `migration-resource://` or accepted legacy local-resource URL;
- package asset manifest source/target uniqueness, digest integrity and safe projection;
- `budget` 13 PDF package projection;
- no Main Page mapping under Historical Migration;
- existing Site Package / Backend / Admin / Public / Integrated Browser regressions.

Browser verification on the exact candidate Head must cover all 10 public Page routes and representative local assets, including `budget` PDF links and image-heavy `teacher-library`. Bounded Human Review follows successful automated Browser verification.

## 10. Scope boundary

Included:

- generic Site Package Page adoption precondition/report capability;
- accepted 10 Main Page formal bodies;
- accepted package-owned Page assets;
- deterministic reference rewrite;
- focused automated + Browser + Human verification.

Excluded:

- stable ListItem capability/content;
- 230 deferred + 6 source-defect Articles;
- placeholder/fixed integration product redesign;
- Generic Historical Migration changes;
- Public URL/frontend technology changes;
- agentic-dev baseline upgrade.

## 11. Slice readiness

The capability and content are one atomic delivery boundary: integrating formal package bytes without guarded Existing-Site adoption would leave current sites stale, while adding adoption semantics without an accepted target package would have no product outcome. They therefore form one homogeneous Candidate Execution Unit under `slice-work`.

This Specification remains **READY / CURRENT** as the accepted product contract and was implemented by EU-52. PR #125 integrated at `main@ccbd9fd8c6048f5b4a96d965b8578f7b7a1d2838`; exact-head automated verification, bounded Human Review and Post-Integration CI #973 passed. EU-52 Execute Authority is terminated.
