# Repository Guidelines

## Purpose and Boundaries
DynamisSession provides save/load infrastructure for ECS worlds.

- Entity identity is sourced from DynamisCore (`EntityId` comes from DynamisCore).
- Session does not own ECS storage, lifecycle, or SceneGraph responsibilities.
- Session owns persistence workflows, schema evolution, and format versioning.
- Component codecs are explicitly registered; reflection-based scanning is not used.

## Project Layout
- `session-api`: persistence contracts, DTOs, and codec interfaces
- `session-core`: snapshot assembly and binary IO implementation
- `session-runtime`: runtime wiring for world creation and slot operations

## Build and Test
- `mvn validate` checks module wiring and build configuration.
- `mvn test` runs module test suites with preview enabled.

## Contribution Rules
- Keep module boundaries strict: APIs in `session-api`, implementations in `session-core`, orchestration in `session-runtime`.
- Add tests with every behavioral change, especially serialization and version-handling paths.
- Use Conventional Commits (for example, `feat(session-core): add binary save reader`).
