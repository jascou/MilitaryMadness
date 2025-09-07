Project-specific Developer Guidelines

Audience: Advanced contributors to MilitaryMadness. This document captures practical, repo-specific details for building, testing, and developing reliably in this codebase.

1) Build and Configuration
- JDK: Java 8 (Gradle toolchain is configured to use JavaLanguageVersion.of(8)). If multiple JDKs are installed, Gradle will select an appropriate one.
- Build tool: Gradle (preferred). Ant build.xml is retained for legacy use.
- Main class: military.MilitaryMadness
- Source layout:
  - Main sources: src
  - Main resources: Resources, Maps, sounds (packaged into the jar)
  - Test sources: test\java
  - Test resources: test\resources
- Gradle sourceSets (from build.gradle):
  - main.java -> src
  - main.resources -> Resources, Maps, sounds
  - test.java -> test\java
  - test.resources -> test\resources
- Run commands (Windows PowerShell or cmd):
  - Build and run tests: .\gradlew.bat clean test
  - Run app: .\gradlew.bat run
  - Create jar (with resources): .\gradlew.bat jar (output under build\libs)
  - Full verification (tests, coverage verification, checkstyle): .\gradlew.bat clean test jacocoTestReport check
- Artifacts/Reports:
  - JaCoCo coverage report: build\reports\jacoco\test\html\index.html
  - Checkstyle reports: build\reports\checkstyle\main.html and test.html
- Checkstyle configuration: config\checkstyle\checkstyle.xml (Gradle uses toolVersion 10.12.5; ignoreFailures=true in build.gradle, CI may still surface issues).
- Resource loading & paths:
  - The game expects Maps, Resources, and sounds available on the classpath or project root. Gradle’s jar task packages these directories inside the jar.
  - Code uses java.nio.file.Path in many places to normalize Windows/Linux paths. Avoid hard-coded separators; prefer Paths.get(...) and Config for resolved locations.
- Headless/GUI: For CI or headless machines, avoid constructing Swing frames. Engine tests are safe in headless mode; some GUI tests require a display and may throw HeadlessException.

2) Testing
- Framework: JUnit 4.13.2 (assertions via JUnit/Hamcrest). Tests live under test\java; fixtures under test\resources.
- Quick start (Windows):
  - Run all tests: .\gradlew.bat test
  - Run a single test class: .\gradlew.bat test --tests "military.engine.EngineSmokeTest"
  - Run a single test method: .\gradlew.bat test --tests "military.engine.EngineSmokeTest.testLoadMapAndBasics"
  - Generate coverage report: .\gradlew.bat jacocoTestReport
- What passes in headless environments (verified now):
  - military.engine.EngineSmokeTest (passes)
  - military.engine.LocationManagerRoundTripTest (passes)
  - military.util.SoundAbstractionTest (passes)
  - military.gui.GuiLayoutTest currently fails in headless mode with java.awt.HeadlessException; do not include in headless runs.
- Maps and fixtures used by tests:
  - Tests copy test\resources\Maps\Sample_small.txt into the runtime Maps directory via Config.mapsDir() before execution (see EngineSmokeTest and LocationManagerRoundTripTest). You do not need to pre-populate Maps manually for these test classes.
- Adding a new test (guidelines):
  - Place new test code in test\java under the appropriate package (e.g., military.engine).
  - Prefer deterministic code paths: if randomness is involved, use provided hooks (e.g., LocationManager.setRandomSeed if available) or inject fakes.
  - For GUI-related logic, test controllers or models instead of constructing JFrame in headless CI. If you must test Swing, guard with GraphicsEnvironment.isHeadless() and skip appropriately.
  - Use test resources under test\resources instead of relying on working directory. If your code expects files in Maps/Resources, copy from test resources into Config.mapsDir() within a @Before method and clean up in @After.
- Example template (engine test):
  - File: test\java\military\engine\ExampleBasicTest.java
    package military.engine;
    import org.junit.*;
    public class ExampleBasicTest {
      @Test
      public void example() {
        Assert.assertTrue(1 + 1 == 2);
      }
    }
  - Run: .\gradlew.bat test --tests "military.engine.ExampleBasicTest.example"
  - After local experimentation, remove any temporary example tests before committing unless they provide real value.

3) Running and Debugging
- Running from Gradle: .\gradlew.bat run
- Running from IDE:
  - Main class: military.MilitaryMadness
  - Working directory: project root (so Maps, Resources, sounds are discovered if not using classpath resources).
  - On Windows, ensure file encodings and line endings are handled as UTF-8 where the loader expects it (map load/save routines use UTF-8).
- CLI options: MilitaryMadness supports command-line flags such as --play <map> and --design <w> <h> (see MilitaryMadness for details). Useful for bypassing interactive flows.
- Logging: Prefer the centralized logging utility (military.util.Logs or as referenced in docs/tasks.md) over System.out.println. Set appropriate levels and avoid logging in hot render loops.

4) Development Practices Specific to This Repo
- Package boundaries (see docs/architecture.md and docs/module-overview.md):
  - military.engine: core game state, movement, combat, map management
  - military.gui: Swing views and minimal controllers; keep UI passive, state-driven
  - military.util: resource loading, logging, sound abstractions, helpers
- Threading and Swing:
  - All UI mutations must occur on the EDT; use SwingUtilities.invokeLater from non-EDT paths.
  - Avoid blocking the EDT. Use javax.swing.Timer for periodic UI updates/animations.
- Rendering:
  - Do not call getGraphics() directly; rely on repaint() and paintComponent.
  - Avoid per-frame image IO; use the centralized ResourceLoader/image cache.
- Determinism for tests:
  - Use provided RNG hooks and inject fakes for time/sound/image loading where possible, so tests are deterministic and CI-stable.
- Map and resources I/O:
  - Use Config for resolving Maps/Resources paths; avoid hard-coded strings.
  - Load resources via classpath when packaged; fall back to filesystem in development.
- Code style and static analysis:
  - Follow Checkstyle rules in config/checkstyle/checkstyle.xml.
  - Keep public APIs documented with Javadoc (see tasks.md for prioritized classes).
  - Prefer enums for domain concepts (Team, TerrainType, UnitType) over booleans.
- CI expectations:
  - GitHub Actions workflow builds with a JDK matrix, runs tests and static analysis, and publishes reports. GUI tests that require a display should be disabled or guarded.

5) Troubleshooting
- HeadlessException during GUI tests: Set or check System.getProperty("java.awt.headless"). Even with headless=true, constructing a JFrame will fail; prefer testing non-UI logic or use headless-capable strategies.
- Missing maps/resources during tests: Ensure your test copies required files from test\resources into Config.mapsDir() in a @Before method.
- Gradle cannot find Java 8: Install/enable a JDK 8; Gradle toolchains will attempt to provision one, but local toolchain support depends on Gradle/JDK setup.
- Classpath vs working-directory issues: The Gradle jar task bundles Resources, Maps, sounds; at runtime use classpath-aware loaders (see ResourceLoader) to avoid reliance on CWD.

6) Verified Commands (executed now)
- .\gradlew.bat test --tests "military.engine.EngineSmokeTest" => passed
- .\gradlew.bat test --tests "military.engine.LocationManagerRoundTripTest" => passed
- .\gradlew.bat test --tests "military.util.SoundAbstractionTest" => passed
- Note: military.gui.GuiLayoutTest fails in headless environments; exclude from CI or guard test code.

7) When adding tests or utilities during exploration
- Do not commit throwaway files. Remove any temporary test files before submitting a PR unless they provide clear value.
- Keep this guideline file (.junie/guidelines.md) up-to-date when the build, testing, or packaging story changes (e.g., bumping Java version, adding new static analysis tools).
