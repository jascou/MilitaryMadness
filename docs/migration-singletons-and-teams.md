# Migration Guide: Phasing out Singletons and Boolean Team Flags

This guide describes the path to migrate legacy code to the newer architecture while avoiding big-bang changes.

Goals:
- Replace global singletons (LocationManager, UnitManager, EventBus singleton usage) with instance-scoped repositories/services.
- Replace boolean team flags (`true`=blue, `false`=red) with the `Team` enum to improve readability and future extensibility.
- Keep the codebase running at all times via interim adapters and feature toggles.

## Strategy Overview
1. Introduce adapters and facades so new code targets instance-owned services (e.g., `MapService`, repositories) while legacy code continues to use singletons.
2. Add enum-friendly APIs alongside boolean ones, deprecate old ones, and migrate call sites incrementally.
3. Provide feature toggles to opt-in to new flows in a controlled fashion (via JVM system properties).

## Interim Adapters and Helpers
- `Unit.getTeamEnum()` and `Unit.setTeam(Team)`: These provide a bridge between old callers that use booleans and new callers that rely on the `Team` enum. Prefer using the enum methods in new code. Eventually deprecate `Unit#getTeam()` and `Unit#setTeam(boolean)`.
- `DefaultMapService`: A concrete adapter over `LocationManager` that presents a small surface area for Designer and Game. Over time, move logic out of `LocationManager` into instance-owned services and replace static methods/caches.
- `EventBus`: Currently a singleton to keep wiring light. Future: inject an `EventBus` instance via constructors to eliminate global state.

## Feature Toggles (Config-Driven)
To de-risk changes, use `military.util.FeatureToggles` (system property backed) to switch between legacy and new flows.

Available toggles:
- `-Dmm.maps.versionedSave=true` — DefaultMapService.saveMap() writes versioned files (MMAPv1) instead of legacy layout. Default is `false` to preserve current tests.
- `-Dmm.sound.enabled=false` — Starts the game with sounds disabled. Default is `true`.

Guidelines:
- Toggle defaults should preserve legacy behavior and tests.
- Only use toggles on boundary seams (I/O formats, controller/loop decisions); avoid sprinkling flags deep in pure logic.

## Step-by-Step Migration (Suggested)
1. New call sites should target `Team` enum APIs (`Unit#getTeamEnum`, `Unit#setTeam(Team)`) and avoid new boolean usages.
2. For shared state, prefer instance-owned repos (e.g., `DefaultUnitRepository`) injected into `Game`/controllers; write thin wrappers to delegate to legacy singletons during transition.
3. Move static logic from `LocationManager` into a `MapRepository` instance progressively, adjusting `DefaultMapService` to delegate to the new repo. Keep the old API delegating to the new code until fully migrated.
4. Remove boolean team overloads when no longer used and simplify conversion logic.
5. Retire feature toggles once the new paths are fully validated.

## Examples
- Using Team enum today:
  ```java
  Unit u = ...;
  Team team = u.getTeamEnum();
  if (team == Team.BLUE) { /* ... */ }
  u.setTeam(Team.RED);
  ```
- Using feature toggle to write versioned map files:
  ```sh
  java -Dmm.maps.versionedSave=true -jar build/libs/MilitaryMadness-*.jar
  ```

## Cleanup Checklist
- [ ] No usages of `Unit#setTeam(boolean)` remain.
- [ ] All team checks read `Unit#getTeamEnum()`.
- [ ] No new code references global singletons directly; services injected where appropriate.
- [ ] Feature toggles removed or defaulted to new behavior; code paths consolidated.
