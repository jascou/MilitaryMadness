# MilitaryMadness

A Java-based strategy game project with a classic hex-grid combat and a simple map designer.

## Developer Onboarding (Quick Start)
- Java: JDK 8 (Gradle toolchain will target Java 8)
- Preferred build tool: Gradle Wrapper (Windows: `./gradlew.bat`, macOS/Linux: `./gradlew`)
- Main class: `military.MilitaryMadness`
- Working directory: project root (so `Maps/`, `Resources/`, and `sounds/` are discoverable when not using classpath resources)

### Build and Test
- Clean and run tests: `./gradlew.bat clean test`
- Full verification (tests, coverage, static analysis): `./gradlew.bat clean test jacocoTestReport check`
- Generate JaCoCo coverage report: `./gradlew.bat jacocoTestReport` → see `build\reports\jacoco\test\html\index.html`
- Checkstyle reports: `build\reports\checkstyle\main.html` and `test.html`
- SpotBugs report: `build\reports\spotbugs\main.html`

Run a single test class or method:
- Class: `./gradlew.bat test --tests "military.engine.EngineSmokeTest"`
- Method: `./gradlew.bat test --tests "military.engine.EngineSmokeTest.testLoadMapAndBasics"`

### Run the Game/Designer
- Run the app: `./gradlew.bat run`
- Command-line options:
  - `--play <mapName>` to launch directly into a scenario (e.g., `--play Sample_small`)
  - `--design <w> <h>` to open the designer with a new map of size w x h
  - `--design <mapName>` to open an existing map in the designer
  - AI options for testing the built-in AI from the CLI:
    - `--ai red|blue|both` choose which side(s) are automated by AI (e.g., `--ai both` for AI vs AI)
    - `--ai-seed <n>` set a deterministic seed for reproducible AI behavior
    - `--ai-delay <ms>` add a delay between AI actions for visualization (0 for fastest)
    - Strategy params (experimental): `--ai-agg <v>`, `--ai-caution <v>`, `--ai-capture <v>`

Headless note: Some GUI tests require a display and may throw `java.awt.HeadlessException`. Non-GUI engine tests are safe in headless environments.

### Project Structure
- `src/` – Java sources (engine, gui, designer, util)
- `Maps/` – Map files (text)
- `Resources/` – Images and sprites
- `sounds/` – WAV sound effects
- `docs/` – Documentation (`tasks.md`, `map-format.md`, `units.md`, `profiling.md`)
- `test/` – Unit tests and test resources

### Common Workflows
- Develop a feature:
  1. Create a branch `feature/<short-name>`
  2. Implement changes with tests (focus on `military.engine` and map logic)
  3. `./gradlew.bat clean test` locally; optionally `./gradlew.bat check`
  4. Run the app `./gradlew.bat run` to manually verify
  5. Open a PR; ensure CI is green
- Update maps/resources for tests: copy fixtures from `test\resources` into the runtime `Maps` folder in your test setup (see `LocationManagerRoundTripTest`).
- Deterministic tests: use the provided hooks (`CombatStats.setRng`, `ImageCache.setImageLoader`, `SoundPlayer` fakes) to avoid flaky tests.

### Coding Standards
- Follow `CONTRIBUTING.md` for naming, formatting, exceptions, and null-safety.
- Static analysis: Checkstyle and SpotBugs are wired into Gradle (`./gradlew.bat check`).
- Prefer `java.nio.file.Path` and `Config` for file paths; avoid hard-coded separators.
- Logging via `military.util.Logs`; avoid `System.out.println` in production code.

### Ant (optional/legacy)
- An Ant build (build.xml) is retained for legacy use: `ant`

### Troubleshooting
- "Maps folder is missing" during Play: create the `Maps` directory or use the designer to create a map.
- Headless CI: avoid constructing Swing frames in tests; see `GuiHeadlessSmokeTest`.
- Java version errors: ensure JDK 8 is installed; Gradle toolchain is configured to compile/run with Java 8.

## Contributing
Please read `CONTRIBUTING.md` for guidelines on code style, branching, PRs, and the pull request checklist.

## Next Steps
See `docs/tasks.md` for the improvement backlog and `docs/map-format.md` for the map file schema. 



## How can a player start the AI player to play?

You can start an AI-controlled game from the command line using the `--ai` flag together with `--play`:

- One side as AI (Red AI vs Blue human):
  - Windows PowerShell/cmd:
    - `./gradlew.bat run --args "--play Sample_small --ai red"`
- Blue AI vs Red human:
  - `./gradlew.bat run --args "--play Sample_small --ai blue"`
- AI vs AI (both sides automated):
  - `./gradlew.bat run --args "--play Sample_small --ai both"`

Optional flags:
- `--ai-seed <n>` make AI decisions deterministic for reproducible tests (e.g., `--ai-seed 42`).
- `--ai-delay <ms>` add a pause between AI actions for visibility (use `0` for fastest execution).
- Strategy (experimental): `--ai-agg <v>`, `--ai-caution <v>`, `--ai-capture <v>`.

Jar usage (after building `build\libs\*.jar`):
- `java -jar build\libs\MilitaryMadness.jar --play Sample_small --ai both --ai-seed 1 --ai-delay 0`

Notes:
- Make sure the map you name exists in the `Maps/` folder or packaged resources (e.g., `Sample_small`).
- There is no in-UI toggle yet; starting AI is done via the CLI options above.
