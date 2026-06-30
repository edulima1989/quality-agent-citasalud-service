<!--
SYNC IMPACT REPORT
==================
Version change: [TEMPLATE] → 1.0.0 (initial ratification)

Principles added:
  - I. Clean Architecture
  - II. BDD Testing Strategy
  - III. Good Programming Practices (SOLID, YAGNI, DRY)
  - IV. API First
  - V. Quality Metrics & Coverage

Sections added:
  - Core Principles (5 principles)
  - Quality Standards
  - Development Workflow
  - Governance

Templates updated:
  ✅ .specify/memory/constitution.md (this file)
  ✅ .specify/templates/plan-template.md (Constitution Check gates updated)
  ✅ .specify/templates/spec-template.md (BDD acceptance scenarios already aligned)
  ✅ .specify/templates/tasks-template.md (test phases aligned with BDD + JaCoCo)

Deferred items:
  - TODO(RATIFICATION_DATE): Set to today 2026-06-27 (first ratification)
-->

# citasalud-service Constitution

## Core Principles

### I. Clean Architecture

All source code MUST follow Robert C. Martin's Clean Architecture layering:

- **Domain layer** (`domain/`): Entities, value objects, domain services, repository interfaces. MUST have zero dependencies on frameworks, databases, or external libraries.
- **Application layer** (`application/`): Use cases / interactors that orchestrate domain logic. MUST depend only on the domain layer. MUST NOT reference infrastructure or delivery concerns.
- **Infrastructure layer** (`infrastructure/`): Repository implementations, persistence adapters, external service clients. Implements interfaces defined in the domain layer.
- **Delivery / Interface layer** (`interfaces/`): REST controllers, CLI handlers, event listeners. MUST depend only on the application layer via use-case interfaces.

Dependency rule: source code dependencies MUST point inward only (delivery → application → domain). No layer MUST import from a layer that is further out.

**Rationale**: Enforces testability, framework independence, and long-term maintainability by keeping business logic free of infrastructure concerns.

### II. BDD Testing Strategy

All features MUST be covered by three levels of tests written using the BDD (Behaviour-Driven Development) style (Given / When / Then):

- **Unit tests**: Test individual domain entities, value objects, and use cases in isolation. MUST use mocks/stubs only for collaborators that cross layer boundaries.
- **Integration tests**: Test the interaction between the application layer and the infrastructure layer (e.g., real database, real repositories). MUST exercise actual adapters, not mocks.
- **Functional / Acceptance tests**: Test complete user scenarios through the delivery layer (REST endpoints). MUST follow the Gherkin-style Given/When/Then format aligned with spec.md acceptance scenarios.

Tests MUST be written before or alongside the implementation (BDD red-green cycle). No feature task is considered done until all three test levels pass.

**Rationale**: BDD tests serve as living documentation and ensure every feature can be validated from the user's perspective without ambiguity.

### III. Good Programming Practices (SOLID, YAGNI, DRY)

All production code MUST comply with the following practices:

- **SOLID**:
  - *Single Responsibility*: Each class MUST have one reason to change.
  - *Open/Closed*: Classes MUST be open for extension, closed for modification.
  - *Liskov Substitution*: Subtypes MUST be substitutable for their base types.
  - *Interface Segregation*: Clients MUST NOT be forced to depend on interfaces they do not use.
  - *Dependency Inversion*: High-level modules MUST depend on abstractions, not concretions.
- **YAGNI** (You Aren't Gonna Need It): Implement only what is explicitly required by the current user story. No speculative abstractions or future-proof scaffolding.
- **DRY** (Don't Repeat Yourself): Every piece of knowledge MUST have a single, authoritative representation in the codebase. Duplication MUST be extracted into a shared abstraction before merging.

**Rationale**: These practices reduce coupling, simplify maintenance, and prevent over-engineering that slows delivery.

### IV. API First

Every API endpoint MUST be defined in an OpenAPI 3.x contract **before** implementation begins:

- The OpenAPI contract MUST live at `src/main/resources/openapi/` as `*.yaml` (one file per API surface or version).
- Server-side interfaces, models, and DTOs MUST be generated via `openapi-generator` (configured in `build.gradle`). Hand-written controller interfaces are forbidden when a generated interface is available.
- The contract is the source of truth. Any change to behavior MUST be reflected in the contract first, then re-generated, then implemented.
- Breaking changes to existing contracts MUST bump the API major version.

**Rationale**: Contract-first design enables parallel front-end/back-end development, enforces a stable public interface, and eliminates drift between documentation and implementation.

### V. Quality Metrics & Coverage

All code MUST meet the following quality gates before a feature branch can merge:

- **Per-class line/branch coverage**: MUST exceed **80%** for every class that contains business logic (domain + application layers).
- **Global coverage**: MUST be **≥ 80%** across the entire codebase (line coverage).
- **Coverage tool**: JaCoCo MUST be configured in `build.gradle` with `jacocoTestReport` and `jacocoTestCoverageVerification` tasks. The CI pipeline MUST fail if thresholds are not met.
- **Report location**: `build/reports/jacoco/` — HTML and XML reports MUST be generated on every build.
- Infrastructure/generated code (openapi-generator output, configuration classes) MAY be excluded from coverage enforcement via JaCoCo exclusion rules.

**Rationale**: Measurable coverage targets prevent blind spots in testing and provide an objective quality gate independent of subjective code review.

## Quality Standards

- **Language & Build**: Java 17+ with Gradle. All dependencies MUST be declared in `build.gradle` with explicit versions.
- **Static Analysis**: Checkstyle and/or SpotBugs MUST be configured and enforced in CI.
- **Logging**: SLF4J MUST be used for all application logging. Log levels MUST match severity (DEBUG for diagnostics, INFO for business events, WARN/ERROR for actionable alerts).
- **Error Handling**: Domain exceptions MUST be defined in the domain layer. Infrastructure and delivery layers MUST translate domain exceptions into appropriate response codes/messages without leaking internal details.
- **Generated Code**: openapi-generator output MUST be placed in `build/generated/` and MUST NOT be committed to version control. The `.gitignore` MUST exclude this directory.

## Development Workflow

1. **Spec first**: A `spec.md` with BDD acceptance scenarios MUST exist before any implementation task starts.
2. **Contract before code**: OpenAPI YAML MUST be reviewed/approved before controller implementation.
3. **Tests before implementation**: Write failing BDD tests (unit + integration + functional) before writing production code.
4. **Constitution Check in plan**: Every `plan.md` MUST include a Constitution Check section validating all five principles before Phase 0 research proceeds.
5. **Coverage gate**: `./gradlew test jacocoTestCoverageVerification` MUST pass locally before opening a pull request.
6. **PR review**: All pull requests MUST be reviewed for Clean Architecture layer compliance and SOLID violations before merge.

## Governance

- This constitution supersedes all informal conventions and README-level guidance.
- **Amendments** require: (1) a written rationale, (2) a version bump following semantic versioning, (3) an updated Sync Impact Report in this file, and (4) propagation to all affected templates.
- **Versioning policy**:
  - MAJOR: Removal or redefinition of a principle that breaks existing compliant code.
  - MINOR: Addition of a new principle or materially expanded guidance.
  - PATCH: Clarifications, wording improvements, non-semantic refinements.
- **Compliance review**: Constitution compliance MUST be checked in every `plan.md` Constitution Check gate and in PR code review.
- **Runtime guidance**: See `CLAUDE.md` and `.specify/` for agent-specific execution instructions.

**Version**: 1.0.0 | **Ratified**: 2026-06-27 | **Last Amended**: 2026-06-27
