# MilitaryMadness Improvement Checklist

Below is an ordered, actionable checklist of improvements spanning architecture, code quality, testing, tooling, and documentation. Each task is intentionally small enough to be executed and tracked. Check off items as you complete them.

1. [x] Establish a CONTRIBUTING.md with coding standards (naming, formatting, null-safety, exceptions) and commit message conventions. (Added CONTRIBUTING.md)
2. [x] Add a top-level README.md with build/run instructions (JDK version, how to launch game/designer, assets path requirements). (Added README.md)
3. [x] Define a consistent package and module overview (diagram or text) describing engine, gui, designer, entrypoint responsibilities. (Added docs/module-overview.md)
4. [x] Introduce a central configuration class for paths (Maps, Resources, sounds) and OS-agnostic separators (use java.nio.file.Path) instead of hardcoded strings. (Added src/military/Config.java)
5. [x] Replace all backslash/forward-slash string paths with Path-based resolution (Model, SoundUtility, GUI map loader, MilitaryMadness, LocationManager). (Updated Model, SoundUtility, GUI, MilitaryMadness, LocationManager)
6. [x] Remove reliance on working directory; load assets via classpath resources with a fallback to file system where necessary. (Added ResourceLoader with classpath-first, filesystem fallback; updated call sites)
7. [x] Create a ResourceLoader utility (images, sounds, text) with clear error reporting and optional caching. (Added src/military/util/ResourceLoader.java)
8. [x] Decouple UI from game loop: extract Game state/logic from Swing event handling to a separate service/controller. (Introduced GameController and routed rendering through it)
9. [x] Introduce a GameState model (turn, selected unit, cursor, pending actions) separate from rendering to support testability. (Added GameState; synchronized turn/cursor)
10. [x] Replace busy-wait loops (e.g., while(dgui.isVisible()) {}) with proper Swing event-driven callbacks or modal dialogs. (Removed loops in MilitaryMadness)
11. [ ] Ensure Swing usage stays on EDT: wrap UI mutations in SwingUtilities.invokeLater/invokeAndWait where appropriate.
12. [ ] Replace JOptionPane runtime errors with structured exception handling and user-friendly messages at the UI boundary.
13. [ ] Add logging (java.util.logging or SLF4J) and replace System.out.println with log levels; centralize logger initialization.
14. [x] Add nullability checks and Optional usage where appropriate to prevent NPEs (e.g., levelName flow in MilitaryMadness). (Default selection & validation)
15. [ ] Validate user inputs (numeric width/height, map names) with dedicated validator utilities and bounded constraints.
16. [ ] Extract constants (unit stats, tile types, movement costs) to configuration files or enums; avoid magic numbers.
17. [ ] Introduce enums for terrain types, unit types, teams, and actions to improve readability and safety.
18. [ ] Add Javadocs for all public classes/methods in engine and gui packages, documenting side effects and threading assumptions.
19. [ ] Implement unit tests for engine logic (LocationManager, Unit movement, combat math, capture rules) using JUnit.
20. [ ] Add tests for map parsing/saving round-trip (LocationManager.loadMap/saveMap) with sample maps in test resources.
21. [ ] Create a small set of golden images or snapshots for render logic validation (component layout sizes, not pixel-perfect).
22. [ ] Add sound system tests by abstracting Clip interactions behind an interface and mocking in tests.
23. [ ] Introduce interface abstractions for sound and image loading to decouple from javax.sound and ImageIO in core logic.
24. [ ] Make SoundUtility thread-safe: use a thread-safe queue (e.g., ConcurrentLinkedQueue) and avoid wait/notify on this; use a dedicated consumer thread with blocking take.
25. [x] Ensure SoundUtility closes streams and clips reliably; use try-with-resources for AudioInputStream. (Applied try-with-resources)
26. [x] Replace raw ArrayList with generics throughout (e.g., ArrayList<String>, ArrayList<Point>) to enable compile-time safety. (Updated MilitaryMadness, SoundUtility)
27. [ ] Review equals/hashCode where Points/Locations are used in collections; ensure deterministic behavior.
28. [ ] Guard against index-out-of-bounds and nulls in LocationManager.getLoc and isInBounds, and centralize boundary checks.
29. [ ] Optimize movement/attack range calculations: memoize or precompute adjacency; avoid deep recursion that may overflow.
30. [ ] Replace recursion in movesRecursive/rangedRecursive with iterative BFS/DFS to improve reliability and readability.
31. [ ] Introduce a deterministic RNG seed option for tests in any random map generation utility.
32. [ ] Separate Designer (military.designer) from runtime engine: define a shared core API used by both Game and DesignGUI.
33. [ ] Add a domain-specific Map model (width, height, tiles, units) independent of UI to simplify save/load and validation.
34. [ ] Validate map files on load (schema: size, tile codes, unit placement rules) with clear error messages.
35. [ ] Normalize file encodings (UTF-8) for map and units text; specify in reading/writing routines.
36. [ ] Introduce a Build tool modernization (Maven or Gradle) while keeping Ant support; define dependencies and plugins for tests and packaging.
37. [ ] Add a runnable fat JAR packaging task with resources included; verify resource loading works from JAR.
38. [ ] Implement a simple dependency inversion for GUI to request actions from controller (commands: move, attack, end turn).
39. [ ] Centralize keyboard/mouse input mapping; avoid scattering key handlers in multiple anonymous inner classes.
40. [ ] Extract GUI layout code into separate methods/classes; define constants for sizes and margins; avoid magic values.
41. [ ] Improve image handling: use BufferedImage everywhere in rendering path and scale/type constants consistently.
42. [ ] Preload frequently used assets (unit sprites, tiles) and cache them to reduce repeated disk reads.
43. [ ] Add error placeholders for missing images/sounds to avoid crashes; display a fallback sprite and log warning.
44. [ ] Make Model image flip and color swap configurable; document color transform; fix bit-shift precedence in BlueRedSwapFilter.
45. [ ] Replace fixed team boolean with Team enum and support more than two teams in future.
46. [ ] Ensure unit actions respect turn rules (no move after attack unless rules allow); encode rules in a dedicated TurnRules class.
47. [ ] Decouple combat math from UI; implement a CombatResolver with inputs (attacker, defender, terrain, rng).
48. [ ] Add tooltips and accessible names to buttons; ensure keyboard navigation works for accessibility.
49. [ ] Replace synchronous dialogs with modeless UI panels for better UX flow (scenario selection, factory display).
50. [ ] Implement a map selection dialog that lists available maps with metadata (size, preview) instead of raw names.
51. [x] Add a defensive check when Maps folder is missing or empty; show a helpful message and disable Play option. (Dynamic menu, validation)
52. [x] Replace System.exit(0) in main flow with proper window close handling and lifecycle management. (Removed System.exit; graceful shutdown)
53. [ ] Remove deprecated API usages and annotate suppressions where necessary; set source/target compatibility.
54. [ ] Introduce Checkstyle/SpotBugs or Error Prone for static analysis; fix high/medium severity issues.
55. [ ] Add GitHub Actions (or similar) CI workflow: build, test, static analysis, and package artifacts on push/PR.
56. [ ] Add code coverage tooling (JaCoCo) with a minimum coverage threshold for engine and map logic.
57. [ ] Document the map file format in docs/map-format.md with examples and constraints.
58. [ ] Document unit types and stats in docs/units.md and consider externalizing to Resources/Units.txt JSON/YAML.
59. [ ] Create a performance profiling plan (Java Flight Recorder) to identify rendering hot spots on large maps.
60. [ ] Introduce a versioning scheme for save files/maps; implement backward compatibility checks.
61. [ ] Ensure cross-platform audio/image support; test on Windows/macOS/Linux with headless mode for CI.
62. [ ] Add command-line options to run the game designer or play mode directly (e.g., --play map, --design width height).
63. [x] Implement graceful shutdown: stop sound thread, save any pending preferences, and dispose frames. (Added SoundUtility.shutdown())
64. [ ] Extract preferences (sound on/off, last map) to a small persistent settings file using java.util.prefs or JSON.
65. [ ] Audit exception handling: wrap external IO and present recoverable flows; avoid empty catch blocks.
66. [ ] Refactor long classes (Game 500+ lines, GUI ~400 lines) into cohesive components with single responsibilities.
67. [ ] Replace anonymous inner classes with lambdas where possible (Java 8+) to improve clarity.
68. [ ] Add unit tests for GUI controller bindings using robot or headless event queue with fakes.
69. [ ] Harden file writing (saveMap): use temp file + atomic replace to avoid corruption on crash.
70. [ ] Validate designer input: prohibit zero/negative sizes; enforce sensible max grid sizes.
71. [ ] Ensure Location and Unit are immutable where feasible, or clearly document mutability and synchronization.
72. [ ] Remove duplicate code paths for loading the same asset twice (Model.loadImage temp vs. image); consolidate.
73. [ ] Audit toString/debug printing for sensitive or excessive output; standardize formatting with StringBuilder.
74. [ ] Add a lightweight plugin point to register new unit types without changing core engine code.
75. [ ] Provide a basic telemetry/log file (opt-in) for bug reports (Java Preferences + logs directory).
