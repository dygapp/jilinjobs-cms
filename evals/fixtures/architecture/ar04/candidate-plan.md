# Content Migration Extraction Candidate Plan

## Goal

Remove historical Party migration implementation from the normal CMS production runtime while preserving reuse of existing CMS domain and persistence capabilities.

The target operational requirement is that the normal CMS server and the generic content-migration tool are independently runnable Spring Boot executable JARs with different runtime lifecycles.

The migration tool should remain site-neutral. Party-specific aliases, accepted fingerprints and historical transition rules belong to migration datasets / profiles, not to generic CMS runtime code.

## Current constraints

- The Backend is currently one Gradle project and one `src/main` source set.
- `CmsApplication` is the normal Spring Boot server entry point.
- Historical Party migration classes currently live under `backend/src/main/kotlin/com/jilinjobs/cms/migration`.
- The existing migration code reuses CMS services / mappers, datasource configuration, Flyway-managed schema and resource handling rather than directly writing an independent database model.
- Normal CMS production packaging must stop carrying Party-specific migration implementation.
- The change should avoid a broad Clean Architecture rewrite or one-Gradle-module-per-domain decomposition.

## Proposed sequence

### Stage 1 — isolate migration tooling without restructuring the Backend application

Keep `backend/` as a single Gradle project.

Move generic migration implementation out of `src/main` into a dedicated tooling source boundary:

```text
backend/
├── src/
│   ├── main/
│   └── test/
└── tools/
    └── content-migration/
        ├── kotlin/
        └── test/
```

Define dedicated Gradle source sets such as:

```text
main
test
contentMigration
contentMigrationTest
```

Allow the dependency direction:

```text
contentMigration -> main
```

and prohibit:

```text
main -> contentMigration
```

The normal `bootJar` should contain only the CMS server production runtime. A separate BootJar / executable task should package the `contentMigration` source set with its own migration application main class so the two executable Spring Boot JARs can be run independently.

The migration application may reuse the existing CMS application configuration / service graph as needed, provided migration implementation itself is outside the normal production source set and normal CMS startup does not activate migration commands.

This stage should preserve existing CMS behavior and accepted Party migration behavior while removing Party-specific migration classes from the production runtime artifact.

### Stage 2 — introduce `apps` / `modules` only if stronger evidence appears

Do not restructure the whole Backend solely to complete Stage 1.

If later repository evolution demonstrates sustained need for multiple independently composed applications or reusable CMS capability boundaries, evolve to a multi-project shape such as:

```text
backend/
├── apps/
│   ├── cms-server/
│   └── content-migration/
└── modules/
    └── cms-core/
```

with dependency direction:

```text
cms-server -> cms-core
content-migration -> cms-core
```

CMS domains such as content / listing / resource / navigation can continue using package-level modularity inside `cms-core`; they do not need separate Gradle modules by default.

## Intended benefits

- remove historical migration code from the normal production source/artifact quickly;
- avoid an early broad Backend restructure;
- retain direct reuse of existing CMS services and persistence behavior;
- produce two independently runnable Spring Boot JARs;
- leave a path to stronger application/core module boundaries later if real reuse pressure justifies it.

## Review question

Assess whether this sequencing can safely deliver the stated runtime, packaging and ownership boundaries without creating hidden coupling or forcing avoidable rework later.