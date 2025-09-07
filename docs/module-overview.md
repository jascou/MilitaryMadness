# Package & Module Overview

This document summarizes the current responsibilities and boundaries among the main packages. It provides a reference for contributors and supports future refactors.

- military (entrypoint & configuration)
  - MilitaryMadness: Application entrypoint, top-level menu and mode selection.
  - Config: Centralized configuration for filesystem locations (Maps, Resources, sounds).

- military.engine (core domain / game logic)
  - LocationManager, Location, Unit, Base, Factory, UnitManager: Core game data structures and rules.
  - CombatStats: Computation of combat numbers.
  - Game: Game loop and input interpretation (to be further decoupled).
  - GameState: Minimal state model (turn, cursor) separated from rendering, to support testability.
  - GameController: Thin controller owning GameState and serving as a façade between UI and core; Game now routes rendering via the controller.

- military.gui (presentation layer)
  - GUI, BottomPanel, HexGridPanel, Model, ModelManager, SoundUtility, etc.: Rendering, input capture, and audio playback.
  - Near-term goal: keep Swing interactions on EDT and rely on controller/core for decisions.

- military.designer (map editor)
  - DesignGUI, DesignPanel, UnitButtonsPanel: Map designer UI and actions.

Notes:
- Resources are loaded with a classpath-first, filesystem-fallback strategy via military.util.ResourceLoader.
- Paths are normalized via java.nio.file.Path in Config to avoid hardcoded separators.
- Future work: continue migrating Game input handling and state mutations into GameController and extend GameState (selection, pending actions) for fuller decoupling.
