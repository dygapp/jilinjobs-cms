# EU-55 — Structured Page Card Adoption

## Status

- Parent Planning Authority: GitHub Issue #137 + accepted Page Content Architecture Requirement / Specification
- Requirement: `docs/requirements/page-content-architecture.md`
- Specification: `docs/specifications/page-content-architecture.md`
- Technical Plan: `docs/technical/page-content-architecture.md`
- Planning integration: `main@17288b40af98084905c9f7b6ed24d5e3a7c7fb71`
- Execute baseline: `main@17288b40af98084905c9f7b6ed24d5e3a7c7fb71`
- Candidate Identifier: **EU-55**
- Readiness: **PASS**
- Execute state: **READY / ACTIVE**
- Execute Authority: **GRANTED for this Unit only**
- Implementation branch: `feature/eu-55-structured-page-card-adoption`
- Implementation PR: pending

## 1. Vertical outcome

An existing or Fresh JilinJobs instance can safely represent, author and render `guide/jypq` as an operator-owned Structured Card Page while existing Rich Pages remain compatible and Site Package adoption preserves operator divergence.

This is one atomic vertical Unit because Generic Page schema/API, Site Package data/adoption, Admin authoring and Public renderer must become compatible in the same integrated release. Splitting a generic foundation from the first real Structured Page would create an intermediate state that is not independently useful and risks mixed-version contracts.

## 2. In scope

- Generic CMS Page domain/persistence/API support for orthogonal `contentModel`, `rendererKey`, `contentOwner` and Page-owned versioned Structured payload;
- append-only Flyway upgrade from current Page schema while preserving existing Page content;
- `CARD_COLLECTION` V1 with ordered cards, required plain title and sanitized item-level Rich body;
- small explicit Public renderer registry/resolution with unknown renderer fail-closed;
- existing Rich/placeholder/internal behavior compatibility required by current contracts;
- Admin model-specific Structured card authoring, including item-level current Rich editor and ordering;
- JilinJobs Site Package schema/data update and `guide/jypq` Structured default;
- exact accepted Rich-baseline adoption, Fresh create, operator divergence preserve/report and idempotency;
- current `guide/jypq` 3-card content and 4 accepted package image resources;
- API compatibility projection required by Technical Plan;
- automated Backend/Admin/Public/Site Package/Browser verification and bounded Human Review after automated verification.

## 3. Out of scope

- generic Page Builder / arbitrary block framework;
- normalized section/item persistence without new Product evidence;
- FAQ migration;
- Hui Employment or other new External integration;
- Engineering Page implementation sample;
- arbitrary operator switching among content model / renderer / ownership profiles;
- Structured-to-Structured future package adoption protocol;
- Page Resource relation;
- Main historical migration reactivation;
- public frontend technology replacement;
- production release/deploy beyond current Repository integration lifecycle.

## 4. Readiness Check

### Requirement / Specification

**PASS.** Page Content Architecture Requirement and Specification are Human Review accepted. `guide/jypq` Structured is the accepted target; no new source/Product evidence contradicts it.

### Technical Plan

**PASS.** `docs/technical/page-content-architecture.md` selects Page-owned versioned Structured payload, defines rejected alternatives, persistence/API/Admin/Site Package/renderer boundaries, adoption safety, fail-closed behavior, verification and Stage Return conditions.

### Dependencies / base drift

**PASS.** EU-54 is completed and Execute Authority terminated. Planning PR #143 is integrated at exact `main@17288b40af98084905c9f7b6ed24d5e3a7c7fb71`. No competing Open PR or active Ready Unit exists at readiness time; `EU-55` was not previously allocated in current repository evidence.

### Migration / adoption safety

**PASS.** The Technical Plan preserves EU-49/EU-52 semantics: Fresh create; exact prior-package Rich fingerprint adoption; operator divergence preserve + report; idempotency; structural reconcile without silent content overwrite; no Main historical migration fallback. `guide/jypq` adoption additionally requires a lossless Rich predecessor state before atomic transition.

### Verification

**PASS.** Verification is bounded and executable: full active Flyway fresh/upgrade coverage; Generic Page/API tests; Site Package Fresh/adoption/divergence/idempotency tests; Admin/Public type-check + build; renderer fail-closed tests; Integrated Browser/Review Environment; `about` Rich regression; `/page/guide/jypq` 3 cards + 4 images; Admin edit/reorder/reload; bounded Human Review after machine evidence.

### Human Review expectation

**PASS for Execute start.** Human Review is a later verification gate, not a blocker to implementation. Implementation must stop at that natural gate if human observation is still required after automated evidence.

### Execution scope / context fit

**PASS.** One representative Structured model and one real product Page keep the capability narrow. Cross-layer changes are necessary to deliver one atomic vertical outcome; no speculative generic plugin/page-builder work is included. Detailed file edits remain JIT implementation work.

## 5. Acceptance

EU-55 is acceptable only when current exact-head evidence proves all of the following:

1. existing Rich Page behavior such as `about` remains compatible;
2. Generic Page persistence/API can represent the three orthogonal dimensions without a second renderer authority;
3. Structured `CARD_COLLECTION` V1 validates and fails closed for malformed/unknown model/schema/version;
4. `guide/jypq` has exactly 3 cards in accepted order with accepted titles/body and all 4 current package images;
5. Public dispatch is based only on explicit `rendererKey`; unknown keys do not fall back to Rich HTML;
6. Admin can maintain/reorder Structured cards using item-level Rich content without exposing a parallel whole-page arbitrary HTML authority;
7. Fresh create, exact prior Rich adoption, divergence protection and rerun idempotency are proven;
8. canonical `/page/guide/jypq` and existing Page route/group shell remain stable;
9. Main historical migration is not reactivated or used as fallback;
10. required Backend/Frontend/Browser/Review evidence is current for the exact implementation Head;
11. bounded Human Review is completed before final Integration if required by current verification authority.

## 6. Stage Return

Immediately stop Execute and return to Specification / Human Review if implementation evidence proves any Technical Plan Stage Return condition, including real `guide/jypq` behavior beyond the Structured card contract, need for independent item workflow/identity/query, inability to keep one content authority, unsafe adoption of operator divergence, or any need for alias/path/DOM renderer dispatch.

## 7. Execution notes

- Current primary responsibility: **Execute — Structured Page Card Adoption**.
- Supporting capabilities enter JIT: Generic CMS persistence/domain/API, Site Package, Admin Vue/TypeScript, Public Renderer, Verification, Review Environment.
- Do not access `dygapp/agentic-dev` upstream during ordinary execution.
- Do not create Reviewed Discovery Map / Runtime View unless real runtime complexity proves current native discovery insufficient.
- Candidate/Ready identity and Current State are owned by this artifact + `docs/work/current/README.md`, not copied into Bootstrap/Roadmap surfaces.
