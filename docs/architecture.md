# Architecture Overview

This document describes the current architecture of MilitaryMadness, focusing on modules, data flow, input/event handling, threading, and the rendering pipeline. It reflects the existing codebase with recent incremental refactors toward better separation of concerns.

## Modules and Packages

- military.engine (core logic/state)
  - Game: main game loop, input interpretation, turn flow, movement and combat orchestration (being gradually decomposed)
  - GameController: thin facade to render via GUI using a shared GameState (EDT‑aware)
  - GameState: small state holder (turn, cursor, etc.) used by rendering
  - LocationManager, Location: map tiles, adjacency, terrain, bounds
  - Unit, UnitManager: units and their lifecycle (singleton, slated for de‑globalization)
  - Factory, Base: special tiles/structures
  - CombatResolver, CombatStats: deterministic combat calculations and results
  - TurnRules: central toggles for rule variants
  - MapService (+ default adapter implemented via LocationManager)

- military.gui (views/controllers)
  - GUI: main JFrame and UI composition (grid, buttons, bottom panel, factory view)
  - HexGridPanel: hex grid component handling painting of map, selection highlights, cursor
  - GUIMiddleMan: simple synchronized event bridge between GUI and Game loop
  - BottomPanel, FactoryPanel, MapSelectionDialog, SoundUtility
  - HexMech: hex math/drawing helpers

- military.util (infrastructure/helpers)
  - ResourceLoader: image/audio/resource loading with caching/fallbacks
  - Logs: centralized logger acquisition
  - InputMappings: key codes and mouse button constants
  - Validator, image/audio abstractions, etc.

- Entrypoint
  - MilitaryMadness: CLI and menu flow; starts Game (play) or DesignGUI (designer)

## High-Level Data Flow

1. Startup
   - MilitaryMadness parses CLI args or shows a menu, loads a map, starts SoundUtility thread, and creates Game.
   - Game loads the map via LocationManager and constructs GUI. Game also initializes GameState and a GameController.

2. Event Loop
   - GUI components (HexGridPanel, buttons) push AWT events to GUIMiddleMan.
   - Game.run() blocks on GUIMiddleMan.getEvent() to receive input events.
   - Game interprets events to update selection/cursor/modes and to trigger actions (move, attack, end turn).

3. Rendering
   - Game asks GameController to render, passing: current turn, selection highlights, and cursor.
   - GameController updates GameState and calls GUI.render() on the EDT (invokeAndWait/invokeLater as needed).
   - GUI delegates to HexGridPanel.render(), which updates its internal state and requests repaint().

4. Model Updates
   - Movement/attack mutate engine objects: Unit, LocationManager, UnitManager.
   - CombatResolver computes CombatStats; GUI displays a combat sequence; engine updates health/exp and removes dead units.

## Threading Model

- Game loop runs on its own thread (synchronously reading from GUIMiddleMan).
- GUI updates and painting must occur on the Swing Event Dispatch Thread (EDT).
- GameController ensures GUI.render() is invoked on the EDT. HexGridPanel.render() posts repaint to the EDT.
- SoundUtility uses a background thread for audio playback.

Caveats and ongoing improvements:
- Some legacy code uses getGraphics() and Thread.sleep() in painting paths; ongoing work is replacing these with repaint() and Swing timers.
- UnitManager is a singleton (global mutable state); slated for replacement with an injected repository.

## Rendering Pipeline

1. Game determines view state: turn, cursor, selectable/movable hexes, optional button focus.
2. GameController.render(gui, turn, select, cursor) updates GameState and defers to GUI on the EDT.
3. GUI.render()
   - Updates side panels (player unit counts, turn label)
   - Calls HexGridPanel.render(select, cursor)
   - Updates BottomPanel with cursor info
4. HexGridPanel.paintComponent(Graphics)
   - Draws background (placeholder), hex grid via HexMech.drawHex
   - Iterates selectLocs to highlight valid hexes
   - Draws cursor marker
   - Double buffering: relies on Swing’s buffer; current code also creates a BufferedImage per frame (to be optimized)

## Input and Commands

- Keyboard/mouse events are centralized in InputMappings and interpreted by Game:
  - Shift initiates movement highlighting; Escape cancels modes; Enter confirms actions; arrows/WASD move cursor.
  - Mouse clicks on the grid move the cursor; clicks on side buttons enqueue synthetic events.
- GUI buttons also call back via GUI.GameActions (Move/Attack/Info/End), allowing a controller‑style flow while retaining legacy event bridging.

## Known Technical Debt and Direction

- Break down Game into focused services (pathfinding, combat, turn management) and an event bus.
- Replace direct getGraphics() calls in HexGridPanel/GUI with pure repaint‑driven rendering.
- Remove Thread.sleep() from UI code; use javax.swing.Timer for animations.
- Replace boolean team with Team enum end‑to‑end; remove singleton UnitManager; introduce GameState snapshots for rendering.
- Consolidate resource loading via ResourceLoader and ensure classpath packaging works for JARs and CI.

## Diagram (simplified)

MilitaryMadness (main)
  -> Game (loop) ---uses---> LocationManager/Unit/Factory (engine state)
  -> GameController ---updates---> GameState
  -> GUI (EDT)
       |-> HexGridPanel (paint)
       |-> Buttons/BottomPanel
       ^            |
       |            v
       +---- GUIMiddleMan <---- input events (mouse/keyboard)

This overview should help onboard contributors and provide context for the refactors outlined in docs/tasks.md.
