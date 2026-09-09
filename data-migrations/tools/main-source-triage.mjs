// Retired EU-50 helper.
//
// Main Historical Migration was corrected to Article-only ownership on 2026-09-09:
// Page content and Main stable ListItem membership belong to the JilinJobs Site Package.
// The previous implementation destructively removed blocked Page/ListItem candidates and
// mixed Article/Page/ListItem records into one promotionReady calculation. Running that
// behavior after the ownership correction would silently cross the accepted boundary.
//
// Use `main-import-eligibility.mjs` against the frozen full-pass + final targeted-retry
// evidence. It emits Article-only migration eligibility plus an explicit Site Package
// Page/ListItem source handoff without deleting those source-evidence candidates.

throw new Error(
  'main-source-triage.mjs is retired: Main migration is Article-only; use main-import-eligibility.mjs and preserve Page/ListItem as Site Package source handoff evidence',
)
