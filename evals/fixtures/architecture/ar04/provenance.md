# AR-04 Fixture Provenance

This file is experiment metadata and is intentionally **not** part of the AR-04 runtime `context_paths`.

## Purpose

AR-04 is a retrospective blind architecture-discovery test. It asks whether a model can independently identify the sequencing flaw in an earlier candidate approach for extracting Generic Content Migration Tooling before first freezing the CMS application/core composition boundary.

The runtime model must not see this provenance file, the hidden assertions, the later Phase 0 planning corrections, AR-02 outputs, or any historical grading result.

## Repository evidence snapshot

The Backend evidence used by AR-04 is read from the experiment branch, but the relevant files were verified unchanged from the pre-Phase-0 accepted baseline:

`main@5e5ef17a60c5c59b2d6688e7545d45f8944d404b`

Verified Git blob identities:

- `backend/settings.gradle.kts` — `7d70cbe8e9a8064a41ab962ce4d9ae4e28f598a1`
- `backend/build.gradle.kts` — `0caecaa1e96315024b746cc0d6bc9935d46273a0`
- `backend/src/main/kotlin/com/jilinjobs/cms/CmsApplication.kt` — `c057df7e9f7ca7b973df148c6d8364ca99c5399f`
- `PartyCarouselMigration.kt` — `55ec6256ef3bc516beb980968514c46e087e08dd`
- `PartyCarouselMigrationV2.kt` — `fc1deaae072ffa6c6cb7313d6bc3b0255458bb44`
- `PartyHistoricalContentMigration.kt` — `b4fd697c2209c85ee35445f3c6bf8ea176cdecb1`
- `PartyHistoricalContentMigrationV2.kt` — `4a0c1d71b2d7f3d00f12697c2d73b209f932fae1`

The same hashes were rechecked on `main@b38731f776d6c44d9a5739613f51c149dffd22f9` before constructing AR-04 for `settings.gradle.kts`, `build.gradle.kts`, and the four Party migration files. No Phase 0 planning document is included in the runtime context.

## Candidate-plan origin

`candidate-plan.md` is a bounded reconstruction of the earlier design proposal discussed before the application/core boundary correction:

1. first keep one Backend Gradle project and extract migration into `tools/content-migration` plus a dedicated source set / BootJar;
2. allow migration tooling to reuse Backend `main` services/configuration;
3. defer `apps/cms-server + apps/content-migration + modules/cms-core` until later evidence demonstrates the need.

The reconstruction includes the already-known product constraint that CMS Server and Content Migration are independently runnable Spring Boot executable JARs. It does not include the later conclusion that application/core composition must be frozen before choosing the Gradle implementation.

## Evidence validity rule

A paired Sol/Astra result is comparable only when both runs have:

- the same exact experiment Head;
- the same AR-04 context digest;
- the same prompt digest;
- isolated fresh `codex exec --ephemeral` sessions;
- no runtime access to this provenance file, corpus assertions, current Phase 0 planning, or previous model outputs.
