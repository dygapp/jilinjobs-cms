# AGENTS.md

## Project

`jilinjobs-cms` is an independent Consumer project for the information-publishing and public website capabilities of the Jilin Smart Employment Cloud Platform.

The current iteration focuses on the center main site. Do not infer additional product scope from other projects, conversations, personal memory, or implementation conventions.

## Repository Authority

When project facts or rules conflict, use this order:

1. `AGENTS.md` — repository governance, authority boundaries, and working rules.
2. `README.md` — current project goal and explicitly confirmed iteration scope.
3. `docs/requirements/information-publishing.md` — authoritative detailed business requirements for behavior that is inside the current iteration scope.
4. Later project Specifications / Architecture / Decisions that are explicitly created from the authorities above.
5. Code and tests as evidence of current implementation state, not as authority for inventing product requirements.
6. Conversation history, temporary plans, and agent reasoning are non-authoritative.

The current iteration scope in `README.md` may intentionally select only part of the broader requirement document. A capability that exists in the source requirement but is explicitly out of scope in `README.md` must not be implemented in this iteration unless the project owner changes the scope.

## Knowledge Boundary

Project facts may come only from:

- this Consumer Repository;
- the explicitly identified `agentic-dev` baseline as development-method guidance;
- authoritative requirement inputs that have been intentionally incorporated into this repository;
- current runtime/repository state that can be directly observed.

Do not use other chats, other repositories, personal memory, or unstated domain assumptions as Consumer project facts.

`agentic-dev` determines how to work; it does not determine this project's business facts.

## Development Method

Method source:

- Repository: `dygapp/agentic-dev`
- Baseline branch: `master`
- Baseline commit at project bootstrap: `d8b90b1880a13c061b3e854abcfe4e5e93c31229`

Follow its current Operating Guide and Method with progressive disclosure:

- use only the Skills required by the current stage;
- do not create artifacts merely to represent a stage;
- keep Specification focused on WHAT / WHY;
- create durable Technical Planning only when cross-unit HOW decisions genuinely need long-term coordination;
- prefer vertical, independently verifiable, context-fit Execution Units;
- use Fresh Context for execution where practical;
- require current evidence before completion claims.

Do not copy the complete `agentic-dev` repository structure, method documents, or Skills into this repository unless a real project need later justifies a Consumer-local artifact.

## Human Escalation

The Agent should resolve ordinary, low-impact, reversible implementation choices autonomously.

Escalate only when a decision materially changes one or more of:

- product goal;
- scope;
- user-visible behavior;
- business boundary;
- acceptance result;
- significant non-functional obligation;
- major architecture direction;
- security/privacy-sensitive or destructive external state.

Do not ask the Human to decide routine naming, file organization, library-level choices, or other reversible implementation details unless they become materially consequential.

## Verification and Integration

No success or completion claim may be made without current evidence appropriate to the claim.

Local implementation and verification may proceed when authorized by the active runtime. A coherent local commit may record a verified project state.

Push, merge, release, deploy, destructive cleanup, and other shared/external integration actions require explicit Human authority or a later repository policy that clearly delegates them.

## Experiment Boundary

This project is currently an `agentic-dev` Consumer Experiment.

Experiment feedback is tracked externally in `dygapp/agentic-dev` GitHub Issue #18. That Issue is an evidence transport and tracking channel, not Consumer product authority.

Only meaningful evidence and Classification Candidates should be sent back. Do not record full conversations, private reasoning, routine Skill invocation logs, or copies of Consumer business documents in the experiment Issue.

The Consumer Agent may propose a Classification Candidate but must not promote an observation into an `agentic-dev` Method or Contract conclusion.
