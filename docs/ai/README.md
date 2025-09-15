AI Overview

This document summarizes the AI architecture introduced in ADR-0004 and how to tune, run, and test it.

1) Architecture
- AIPlayer: Strategy interface AIPlayer.planTurn(ImmutableGameState view, ReadOnlyServices svc, Rng rng) -> List<AiAction>.
  Implementations should be deterministic for a given input state and RNG. The reference implementation is SimpleHeuristicAI.
- ReadOnlyServices: Facade that exposes engine queries needed during planning (unit listings, movement ranges, attack previews, terrain/locations). It must not mutate engine state and is safe to call off the Event Dispatch Thread (EDT).
- AiAction: Marker interface for immutable action value types. Current concrete actions:
  - MoveAction(from, to): request to move a unit.
  - AttackAction(attackerAt, targetAt): request to attack.
  - EndTurnAction(): end the current team’s turn. Plans should include this; AIController will end the turn as a fallback if missing.
  - SelectAction(at): optional focus action, typically a no-op in headless/non-GUI paths.
  - WaitAction(millis): optional pacing/no-op wrapper.
- AIController: Orchestrates planning and execution. It builds a snapshot and calls AIPlayer to get a plan, then executes the List<AiAction}. It is designed to be invoked from a non-EDT thread (GameLoop does so) and coordinates with the GUI (e.g., AI thinking indicator) via GameLoop.
- GameAdapter: Minimal adapter so AIController can be tested headlessly without constructing Swing.

2) Execution Flow
- GameLoop detects when it’s the AI team’s turn and runs AIController.takeTurn(...) on a background thread.
- While the AI is running, the GUI shows a non-modal “AI thinking…” label and user input is disabled via GUIMiddleMan.
- AIController executes actions in order. Today, the engine guarantees an EndTurnAction is executed at the end of a plan; if none is present, it ends the turn defensively.
- Optional per-action delay (AiConfig.delayMs) lets the UI breathe between actions.

3) Tuning and CLI
- Global configuration is provided by AiConfig (static holder) and can be set through CLI flags or via SaveGame persistence.
- CLI flags (examples):
  - --ai red|blue|none: enable AI for a team (default: none)
  - --ai-seed <long>: deterministic RNG seed for AI decisions
  - --ai-delay <ms>: delay between AI actions (default 300; set 0 for tests)
  - --ai-agg <double> or --ai-aggressiveness <double>: favor attacks/closing distance
  - --ai-caution <double>: avoid ending adjacent to enemies
  - --ai-capture <double> (alias --ai-cap-priority): weight moving toward enemy bases
- Save/Load: SaveGame persists AI settings (enabled/team/seed/difficulty knobs). SaveLoadService restores them when loading.

4) Heuristic Summary (SimpleHeuristicAI)
- Attempt one strong attack if available, preferring kills, then higher expected damage. Uses ReadOnlyServices.previewCombat to compare outcomes.
- Otherwise, move at most one unit greedily toward enemies/bases with tie-breaking using RNG, applying caution to avoid ending next to enemies.
- Always ends the turn after at most one primary action sequence.

5) Headless/Testing Guidance
- Headless CI: Do not construct JFrame when GraphicsEnvironment.isHeadless() is true. MilitaryMadness.main guards against headless mode.
- Tests to run safely in headless environments (verified):
  - military.engine.EngineSmokeTest
  - military.engine.LocationManagerRoundTripTest
  - military.util.SoundAbstractionTest
  - military.engine.ai.AiControllerDeterministicTest
  - military.engine.ai.HeadlessFullRoundTest
- Avoid GUI tests that instantiate JFrame in headless mode (e.g., GuiLayoutTest) unless guarded. If you must run GUI-related logic, test controllers/models or gate code with GraphicsEnvironment.isHeadless().
- For deterministic AI tests, set AiConfig.setDelayMs(0) and use a fixed Rng seed (DefaultRng(Random(seed))). Tests should copy sample maps from test/resources into Config.mapsDir() during @Before, as demonstrated in EngineSmokeTest and HeadlessFullRoundTest.

6) Extending the AI
- Add new heuristics by implementing AIPlayer and optionally new AiAction types. Keep actions immutable.
- Expose new tuning knobs through AiConfig and wire CLI parsing in MilitaryMadness.main.
- Prefer reading data via ReadOnlyServices; if a new query is required, add a read-only method and provide a safe implementation.

7) References
- ADR-0004: docs/adr/ADR-0004-ai-player-plan.md
- Tasks: docs/tasks.md (phase 7 items)
