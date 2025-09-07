# ADR-0001: Introduce a Domain Event System

Date: 2025-09-04

Status: Accepted

## Context
Historically, core engine logic (movement, turn changes) triggered UI and sound side-effects directly.
This tight coupling made it hard to test engine logic, and UI concerns leaked into the game rules.

## Decision
Introduce a minimal domain EventBus and a set of events (e.g., TurnStarted, UnitMoved). The engine posts
these events, and adapters in the UI/Sound layer subscribe to them to trigger visuals and sounds.

- Event producers: engine logic.
- Event consumers: GUI/Sound adapters on the EDT or background thread as appropriate.
- Keep the bus simple (synchronous callbacks) to avoid hidden threading.

## Consequences
- Decoupled engine from UI/sound; easier unit testing for engine code.
- Clearer boundaries per package responsibilities (engine vs gui).
- Potential ordering concerns are explicit and testable.

## Alternatives Considered
- Direct callbacks into GUI from engine: rejected due to coupling.
- Full reactive framework (Rx): overkill for current scope; increases dependencies.
