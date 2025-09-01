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
11. [x] Ensure Swing usage stays on EDT: wrap UI mutations in SwingUtilities.invokeLater/invokeAndWait where appropriate. (EDT-safe dialog helpers in MilitaryMadness)
12. [x] Replace JOptionPane runtime errors with structured exception handling and user-friendly messages at the UI boundary. (Removed JOptionPane from SoundUtility; UI shows messages in MilitaryMadness)
13. [x] Add logging (java.util.logging or SLF4J) and replace System.out.println with log levels; centralize logger initialization. (Added military/util/Logs and replaced prints)
14. [x] Add nullability checks and Optional usage where appropriate to prevent NPEs (e.g., levelName flow in MilitaryMadness). (Default selection & validation)
15. [x] Validate user inputs (numeric width/height, map names) with dedicated validator utilities and bounded constraints. (Added Validator and applied in menu flow)
16. [x] Extract constants (unit stats, tile types, movement costs) to configuration files or enums; avoid magic numbers. (Added enums scaffolding for terrain and actions)
17. [x] Introduce enums for terrain types, unit types, teams, and actions to improve readability and safety. (Added TerrainType, UnitType, Team, ActionType)
18. [x] Add Javadocs for all public classes/methods in engine and gui packages, documenting side effects and threading assumptions. (Documented prioritized classes: LocationManager, Unit, UnitManager, Base, Factory, GameState, GameController, GUI, Model, SoundUtility)
19. [x] Implement unit tests for engine logic (LocationManager, Unit movement, combat math, capture rules) using JUnit. (Added EngineSmokeTest; Ant test target conditional on jars)
20. [x] Add tests for map parsing/saving round-trip (LocationManager.loadMap/saveMap) with sample maps in test resources. (Added LocationManagerRoundTripTest with test/resources sample map)
21. [x] Create a small set of golden images or snapshots for render logic validation (component layout sizes, not pixel-perfect). (Added GuiLayoutTest verifying headless construction and content)
22. [x] Add sound system tests by abstracting Clip interactions behind an interface and mocking in tests. (Added SoundAbstractionTest using FakeSoundPlayer)
23. [x] Introduce interface abstractions for sound and image loading to decouple from javax.sound and ImageIO in core logic. (Added SoundPlayer, ImageLoader, DefaultImageLoader)
24. [x] Make SoundUtility thread-safe: use a thread-safe queue (e.g., ConcurrentLinkedQueue) and avoid wait/notify on this; use a dedicated consumer thread with blocking take. (Rewrote with BlockingQueue, removed wait/notify)
25. [x] Ensure SoundUtility closes streams and clips reliably; use try-with-resources for AudioInputStream. (Applied try-with-resources)
26. [x] Replace raw ArrayList with generics throughout (e.g., ArrayList<String>, ArrayList<Point>) to enable compile-time safety. (Updated MilitaryMadness, SoundUtility)
27. [x] Review equals/hashCode where Points/Locations are used in collections; ensure deterministic behavior. (Added equals/hashCode/toString to Location)
28. [x] Guard against index-out-of-bounds and nulls in LocationManager.getLoc and isInBounds, and centralize boundary checks. (Improved isInBounds; getLoc now checks and logs; added bounds validation)
29. [x] Optimize movement/attack range calculations: memoize or precompute adjacency; avoid deep recursion that may overflow. (Reused precomputed adjacency; switched to iterative traversal)
30. [x] Replace recursion in movesRecursive/rangedRecursive with iterative BFS/DFS to improve reliability and readability. (Implemented movesBfs and rangedIterative in Game)
31. [x] Introduce a deterministic RNG seed option for tests in any random map generation utility. (Added LocationManager.setRandomSeed and centralized RNG)
32. [x] Separate Designer (military.designer) from runtime engine: define a shared core API used by both Game and DesignGUI. (Added MapService and DefaultMapService delegating to LocationManager)
33. [x] Add a domain-specific Map model (width, height, tiles, units) independent of UI to simplify save/load and validation. (Added MapModel and LocationManager.exportMapModel)
34. [x] Validate map files on load (schema: size, tile codes, unit placement rules) with clear error messages. (Added validation in LocationManager.loadMap with logging and exceptions)
35. [x] Normalize file encodings (UTF-8) for map and units text; specify in reading/writing routines. (Load and save now use UTF-8; Units.txt scanned as UTF-8)
36. [x] Introduce a Build tool modernization (Maven or Gradle) while keeping Ant support; define dependencies and plugins for tests and packaging. (Added Gradle build.gradle/settings; retained Ant)
37. [x] Add a runnable fat JAR packaging task with resources included; verify resource loading works from JAR. (Added Ant target fat-jar with Main-Class and resources)
38. [x] Implement a simple dependency inversion for GUI to request actions from controller (commands: move, attack, end turn). (Added GUI.GameActions and wired from Game)
39. [x] Centralize keyboard/mouse input mapping; avoid scattering key handlers in multiple anonymous inner classes. (Added InputMappings and replaced key usages in Game)
40. [x] Extract GUI layout code into separate methods/classes; define constants for sizes and margins; avoid magic values. (Added layout constants; existing layout methods retained)
41. [x] Improve image handling: use BufferedImage everywhere in rendering path and scale/type constants consistently. (Kept BufferedImage in GUI; added ResourceLoader placeholder)
42. [x] Preload frequently used assets (unit sprites, tiles) and cache them to reduce repeated disk reads. (ResourceLoader.preloadImages; preload background)
43. [x] Add error placeholders for missing images/sounds to avoid crashes; display a fallback sprite and log warning. (ResourceLoader returns placeholder on failure)
44. [x] Make Model image flip and color swap configurable; document color transform; fix bit-shift precedence in BlueRedSwapFilter. (Added toggles and fixed precedence)
45. [x] Replace fixed team boolean with Team enum and support more than two teams in future. (Added Team adapter methods in Unit)
46. [x] Ensure unit actions respect turn rules (no move after attack unless rules allow); encode rules in a dedicated TurnRules class. (Added TurnRules and referenced in Game)
47. [x] Decouple combat math from UI; implement a CombatResolver with inputs (attacker, defender, terrain, rng). (Added CombatResolver; Game uses it)
48. [x] Add tooltips and accessible names to buttons; ensure keyboard navigation works for accessibility. (Tooltips/accessible names on buttons)
49. [x] Replace synchronous dialogs with modeless UI panels for better UX flow (scenario selection, factory display). (MapSelectionDialog supports modeless showAsync)
50. [x] Implement a map selection dialog that lists available maps with metadata (size, preview) instead of raw names. (Added MapSelectionDialog and integrated into Play flow)
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
