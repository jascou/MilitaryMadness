# ADR-0004: Plan to Implement an AI Player (Human vs AI)

Status: Proposed
Date: 2025-09-12
Authors: Team MilitaryMadness

Context
- The current architecture separates the GUI (Swing) from the core engine using GameController and a dedicated GameLoop (see ADR-0002). Input events are funneled via GUIMiddleMan to Game.stepOnce, which mutates state and triggers renders through the controller.
- The engine holds core rules for movement, combat, pathing, turn flow, and map/unit repositories. Rendering reads from ImmutableGameState snapshots.
- There is no non-human input source today. To support “Human vs AI,” turns owned by the AI must be executed without mouse/keyboard events while keeping GUI responsive. The solution must be deterministic for tests and configurable in headless environments.

Goals
1) Add an AI opponent a human can play against on existing maps.
2) Keep UI passive: the AI drives the same Game API that human input uses; no special-casing in GUI.
3) Deterministic and testable: seedable RNG and headless-friendly execution.
4) Minimal coupling: clear extension points for multiple difficulty levels/strategies.

Non-goals (for initial delivery)
- Multiplayer networking.
- Perfect strategic play; we target a competent baseline.
- Deep refactors of existing Game logic.

High-Level Design
- Introduce an AIController that can take a turn for a given Team. It will:
  - Observe the immutable/snapshot state and read-only engine services (LocationManager, UnitManager, Factory, etc.).
  - Produce a sequence of atomic actions already understood by Game.stepOnce (move, attack, end-turn). Where needed, expose a small internal API that bypasses GUI-only events but preserves rules/validation.
  - Execute actions synchronously during the AI’s turn on a background thread (not EDT), scheduling GUI renders via GameController.
- Define an AIPlayer interface and one concrete SimpleHeuristicAI implementation. Later AIs (Minimax, Monte Carlo) can be plugged in.
- Turn Integration: when it’s Team.RED (configurable) and that side is AI-controlled, Game triggers AIController.takeTurn(). Human turns are unchanged.

Key Extension Points
- AIPlayer (new): planTurn(ImmutableGameState view, ReadOnlyServices svc) -> List<Action>
  - Action is a sealed/value type describing domain operations: MoveAction, AttackAction, EndTurnAction.
- AIController (new):
  - Builds a read-only facade over engine services to avoid accidental mutation.
  - Calls AIPlayer.planTurn and executes actions against Game through a minimal adapter.
- Game adapter (new thin layer): Game.apply(Action a) which maps Action to existing methods (shift/move selection, attack, end), avoiding GUI events.

Data and Services Needed by AI
- Map graph/topology: LocationManager API for neighbors, terrain, occupancy.
- Units and stats: UnitManager and/or repository to query units per team, movement/attack ranges, health.
- Combat prediction: reuse CombatStats.calculate(..) helper or add a light-weight evaluator that mirrors existing post-combat effects.
- Pathfinding: reuse/prefer existing movement range logic; if insufficient, add a deterministic A* utility (engine-scoped) with terrain cost modifiers.

Phased Plan and Milestones
1) Scaffolding and Contracts
   - Add ai package: military.engine.ai
   - Add Action types: MoveAction, AttackAction, EndTurnAction, SelectAction (optional), WaitAction (optional).
   - Add AIPlayer interface and SimpleHeuristicAI stub returning EndTurnAction only (for wiring).
   - Add AIController with takeTurn(Game game, Team aiTeam) method; for now it immediately ends the turn. Provide seed injection.
   - Add ReadOnlyServices facade exposing read-only queries (units by team, locations, movement/attack ranges, combat preview).
   - CLI/config hook: add flag --ai red|blue|none and seed option; default none to keep behavior unchanged.
   - Tests: unit test that AIController on an empty or simple map ends turn deterministically in headless mode.

2) Movement and Targeting Basics
   - Implement SimpleHeuristicAI that:
     - Iterates AI units with actions remaining.
     - For each unit, computes reachable tiles (breadth-limited by movement) and evaluates greedy score: prefer tiles that place unit in attack range of a weak enemy; otherwise advance toward nearest enemy/base.
     - Executes at most one move per unit per turn.
   - Add pathfinding helper (if needed), deterministic and tested with fixtures.
   - Tests: given Sample_small.txt, verify planned actions are legal and applied, and that at least one unit moves toward the nearest opposing unit.

3) Attacking
   - Extend heuristic to select best available attack after moving or from current position.
   - Use CombatStats to estimate expected damage; prefer kills, then highest expected net damage.
   - Execute AttackAction when legal; ensure Game state updates and rendering occurs between unit actions.
   - Tests: scenarios where AI chooses to attack when lethal is possible; verify state (unit counts/HP) after the AI turn.

4) Turn Management and UX
   - Integrate AI turns with GameLoop: when it becomes AI team’s turn, block human input and show a lightweight “AI thinking…” indicator (non-modal, optional in headless).
   - Ensure all UI updates happen on EDT; all AI computation on a background thread.
   - Add configurable delay between actions for watchability; zero in tests.
   - Tests: headless run of one full human+AI turn completes without throwing and is deterministic given a seed.

5) Difficulty and Strategy Options
   - Add parameters to SimpleHeuristicAI (aggressiveness, caution re: terrain/defense, capture priority). Expose via CLI or a settings dialog.
   - Optionally add a MinimaxAI with shallow depth for endgame tactics on small maps.

6) Persistence and Save/Load
   - Include AI configuration (controlled team, seed, difficulty) in SaveGame and SaveLoadService. Backward-compatible: default to human vs human when missing.
   - Tests: round-trip save/load retains AI settings.

7) Polishing and Documentation
   - Javadoc for AI interfaces and actions.
   - docs/ai/README.md with architecture notes, tuning guidance, and examples.
   - Update guidelines with headless caveats and test recipes for AI.

Execution Details
- Threading: AIController runs on a single background thread per AI team; it interacts with Game via a small, synchronized adapter or by queuing synthetic InputEvents into GUIMiddleMan if we keep that façade. Prefer direct method calls to avoid GUI-only event codes.
- Determinism: Inject java.util.Random with a known seed into AIPlayer; do not use ThreadLocalRandom. Avoid time-based decisions.
- Performance: Keep heuristics O(U * R) where U is units and R is reachable tiles; avoid per-frame heavy allocations. Cache movement ranges if reused across evaluations.
- Error handling: If an action becomes illegal due to concurrent state change (rare in single-threaded flow), skip and continue; never hang. Ensure AI must always eventually emit EndTurnAction.

Acceptance Criteria (Initial Release)
- When launched with --ai red, the RED team is controlled by AI and plays its turns automatically on all shipped maps.
- The AI moves units in a way that approaches or attacks the opponent when in range; it ends its turn automatically.
- The game remains playable by a human on the opposite team with no GUI freezes; tests pass in headless mode.

Open Questions
- Is there an existing movement range API comprehensive enough for AI planning, or do we need a dedicated pathfinder?
- Do factories/spawns have special action semantics we should teach the AI in phase 2/3?

Alternatives Considered
- Simulating key presses/mouse events through GUIMiddleMan: higher coupling with GUI event codes; discarded in favor of a thin, typed Action API.
- Running AI on the EDT: rejected to avoid UI stalls.

Dependencies
- Relies on ADR-0002 (controller separation) and current GameLoop scheduling. No external libs.

Risk Mitigation
- Keep feature-flagged via CLI and default off; keep save/load backward compatible; build thorough unit tests around deterministic scenarios.
