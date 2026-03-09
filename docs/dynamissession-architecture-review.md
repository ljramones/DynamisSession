# DynamisSession Architecture Boundary Ratification Review

Date: 2026-03-09

## Intent and Scope

This is a boundary-ratification review for DynamisSession based on current repository code and structure.

This pass does not refactor code. It defines strict ownership and dependency boundaries between Session, WorldEngine, Content, and adjacent policy layers.

## 1) Repo Overview (Grounded)

Repository shape:

- Multi-module Maven project:
  - `session-api`
  - `session-core`
  - `session-runtime`

Implemented contracts and internals:

- `session-api`
  - `SessionManager` (`newGame`, `save`, `load`)
  - `WorldSnapshotter`
  - codec interfaces (`CodecRegistry`, `ComponentCodec`)
  - persistence model DTOs (`SaveGame`, `SaveMetadata`, `EcsSnapshot`, `EntityRecord`, `ComponentEntry`)
- `session-core`
  - `DefaultWorldSnapshotter`
  - `DefaultCodecRegistry`
  - binary IO (`SaveGameWriter`, `SaveGameReader`)
- `session-runtime`
  - `DefaultSessionManager`
  - `DefaultWorldImporter`

Dependency shape:

- `session-api` depends on `dynamis-core` and `ecs-api`.
- `session-core` depends on `session-api`, `dynamis-core`, `ecs-api`.
- `session-runtime` depends on `session-api`, `session-core`, `ecs-api`, `ecs-core`.
- no dependencies on WorldEngine, SceneGraph, LightEngine, Scripting, Content, AssetPipeline, Localization, or Event.

## 2) Strict Ownership Statement

### 2.1 What DynamisSession should exclusively own

DynamisSession should own **session authority and persistence boundaries**, including:

- save/load session lifecycle at slot/session scope
- save payload schema/versioning and serialization boundaries
- runtime session-state restoration from persisted data
- component codec registry boundary for persisted ECS component payloads

### 2.2 What is appropriate for Session

Appropriate concerns:

- session identity metadata (`SaveMetadata`) and format versioning
- snapshot export/import orchestration and on-disk encoding/decoding
- strict compatibility/error behavior for persistence formats

### 2.3 What DynamisSession must never own

DynamisSession must not own:

- world bootstrap/tick orchestration authority (WorldEngine)
- ECS substrate ownership (storage/query mechanics)
- scene hierarchy ownership (SceneGraph)
- render planning or GPU execution authority (LightEngine/DynamisGPU)
- content/asset shaping or asset-pipeline ownership (Content/AssetPipeline)
- scripting runtime ownership

## 3) Dependency Rules

### 3.1 Allowed dependencies for DynamisSession

- `DynamisCore` shared identity/error primitives
- ECS API contracts for world snapshot interface surface
- ECS runtime/core only inside `session-runtime` where reconstruction requires concrete world creation (transitional)

### 3.2 Forbidden dependencies for DynamisSession

- WorldEngine orchestration layers
- SceneGraph substrate
- LightEngine/GPU render execution layers
- Content runtime ownership and asset shaping
- scripting/policy orchestration layers

### 3.3 Who may depend on DynamisSession

- WorldEngine and host runtime layers that need save/load authority
- gameplay/application layers that need session-slot operations
- testing/integration harnesses

Dependency direction intent:

- Session provides persistence authority to WorldEngine; WorldEngine consumes Session, not vice versa.

## 4) Public vs Internal Boundary Assessment

### 4.1 Canonical public boundary

Public boundary should primarily be:

- `session-api` (`SessionManager`, model DTOs, codec/snapshot interfaces)

### 4.2 Internal/implementation areas

Internal by intent:

- `session-core` default snapshot + binary IO + codec registry implementation
- `session-runtime` default manager/importer wiring

### 4.3 Current boundary pressure points

1. `DefaultSessionManager.newGame()` and `load()` currently construct/return `DefaultWorld` directly (concrete ECS implementation coupling in runtime path).

2. `DefaultWorldSnapshotter.exportSnapshot()` requires `DefaultCodecRegistry` specifically for codec enumeration; this narrows the abstraction promised by `CodecRegistry`.

3. `DefaultWorldImporter` currently depends on `DefaultWorld` concrete APIs to preserve entity IDs deterministically.

These are practical implementation choices, but they tighten Session-to-ECS concrete coupling.

## 5) Policy Leakage / Overlap Findings

### 5.1 Major clean boundaries confirmed

- No world tick/lifecycle orchestration exists in Session modules.
- No SceneGraph, render, scripting, or event orchestration code is present.
- No Content/AssetPipeline ownership code is present.
- Save/load responsibility is focused and concrete (format, IO, snapshot conversion).

### 5.2 Notable overlap/drift risks

1. **Session <-> WorldEngine lifecycle ambiguity (low to moderate)**  
`SessionManager.newGame()` creates a world. This is useful, but can blur authority with WorldEngine bootstrap if it expands.

2. **Session <-> ECS substrate coupling (moderate)**  
runtime/import paths rely on `DefaultWorld`, and snapshot export requires `DefaultCodecRegistry` for enumeration. This can constrain substitution and blend persistence with ECS implementation details.

3. **Session API abstraction gap (moderate)**  
`CodecRegistry` exposes only `register/find`, but export needs full codec iteration; requiring concrete registry type indicates an incomplete abstraction seam.

4. **Content/profile/config authority overlap (currently clean)**  
No direct ownership present now; keep it that way so session metadata does not absorb broader content authority.

## 6) Relationship Clarification

### 6.1 Session vs WorldEngine

- Session owns save/load persistence authority.
- WorldEngine owns world lifecycle/tick orchestration.
- WorldEngine should call Session for persistence operations, not delegate orchestration back into Session.

### 6.2 Session vs Content

- Session stores references and runtime state snapshots; Content owns asset identity/resolution/pipeline concerns.
- Session should not absorb asset-shaping logic or content-manifest authority.

### 6.3 Session vs ECS

- ECS owns state substrate implementation.
- Session owns persistence conversion boundaries around ECS state.
- Session should avoid hardening deeper dependency on a single ECS implementation where avoidable.

## 7) Ratification Result

**Ratified with constraints.**

Why:

- Current repo strongly centers on persistence/save-load/session boundaries.
- Clear absence of world/render/scene/scripting orchestration leakage.
- Constraints are required because runtime paths currently couple to concrete ECS and concrete codec-registry implementations (`DefaultWorld`, `DefaultCodecRegistry`).

## 8) Strict Boundary Rules to Carry Forward

1. Keep `session-api` as the stable public persistence boundary.
2. Keep Session focused on persistence and runtime state restoration only.
3. Keep world lifecycle/tick orchestration in WorldEngine.
4. Avoid expanding Session into content authority, asset shaping, or scripting/event orchestration.
5. Reduce future reliance on concrete ECS/registry implementations at boundaries when evolving APIs.

## 9) Recommended Next Step

Next deep review should be **DynamisContent**.

Reason:

- Content is the nearest likely overlap with Session on ownership of loadable state, asset references, and runtime data authority.
- Ratifying Content next will clarify Session-vs-Content boundaries before broader integration planning.
