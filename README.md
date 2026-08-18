# jilinjobs-cms

`jilinjobs-cms` is a Greenfield Consumer project for the information-publishing and public website capabilities of the Jilin Smart Employment Cloud Platform.

## Current Goal

Build the core information-publishing capability for the **center main site**.

This repository starts deliberately small. Project structure and durable artifacts should grow only when real development work creates a lasting need.

## Current Iteration Scope

### In Scope

- column / category management;
- menu and navigation organization;
- core information publishing;
- public main-site frontend pages:
  - home page;
  - second-level / section page;
  - content detail page.

### Out of Scope for This Iteration

- external content embedding;
- user and permission management;
- the `中心党建` secondary site/theme;
- comments and other interaction features;
- complex statistics;
- multi-site expansion.

The detailed source requirement is broader than this iteration. Only requirements that fall inside the scope above are active implementation requirements for the current iteration.

## Authoritative Requirement

Initial authoritative business requirement:

- `docs/requirements/information-publishing.md`

The source document is confirmed V3.2 and is preserved as the detailed business requirement input. It must not be silently expanded, corrected, or replaced by assumptions.

## Development Method

Method and reusable Skills source:

- `dygapp/agentic-dev`
- bootstrap baseline: `master@d8b90b1880a13c061b3e854abcfe4e5e93c31229`

Start from the `agentic-dev` Operating Guide and load only the Method / Project Rules / Skills needed by the current stage.

Do not pre-create a large architecture, decisions, tasks, plans, templates, or governance hierarchy.

## Current Stage

**Project Initialization → Specification**

The immediate next durable project result is a minimum-sufficient WHAT / WHY Specification for the current iteration.

No production code has been authorized or started by this bootstrap package.

## Repository Skeleton

```text
.
├── AGENTS.md
├── README.md
└── docs/
    └── requirements/
        └── information-publishing.md
```

Additional artifacts should be added only when they have real authority, coordination, traceability, or long-term knowledge value.
