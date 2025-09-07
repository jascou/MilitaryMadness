# ADR-0002: Separate Game Loop/Controller from GUI

Date: 2025-09-04

Status: Accepted

## Context
`Game` previously mixed input handling, loop timing, and UI callbacks, which hampered testability and
caused EDT violations. We needed a clearer separation of concerns aligned with MVC/MVP.

## Decision
Extract a controller/loop (`military.engine.GameLoop`) that owns the tick/step, input interpretation,
and transition logic. Keep `GUI`/view classes passive and state-driven (render snapshots), avoiding direct
mutations of engine objects from the view.

## Consequences
- Engine logic can be tested in isolation without constructing Swing components.
- Reduced risk of blocking or mutating UI off the EDT.
- Clear boundaries enable future controller substitution (e.g., bots/simulation).

## Alternatives Considered
- Keep loop in the GUI frame: rejected; too coupled and blocks UI.
- Thread pools/timers inside GUI: increases complexity and risks EDT misuse.
