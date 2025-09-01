# MilitaryMadness

A Java-based strategy game project with a classic hex-grid combat and a simple map designer.

## Build & Run

This repository uses an Ant build file (build.xml). You can also run directly from your IDE.

- Main class: `military.MilitaryMadness`
- Working directory: project root (so `Maps/`, `Resources/`, and `sounds/` are discoverable by current code)
- Java: JDK 8+

### Ant (optional)
- Ensure JAVA_HOME is set and Ant is installed.
- From a terminal (PowerShell on Windows): `ant`

## Project Structure
- `src/` – Java sources (engine, GUI, designer)
- `Maps/` – Map files (text)
- `Resources/` – Images and sprites
- `sounds/` – WAV sound effects
- `docs/` – Documentation (`tasks.md`, `execution.md`)

## Getting Started
1. Ensure the `Maps` folder exists and contains at least one map (e.g., `Maps\Revolt.txt`).
2. Run `military.MilitaryMadness` from your IDE.
3. Choose Play Game to select a scenario, or Create Level to open the map designer.

If the `Maps` folder is missing or empty, the Play option will be disabled, and you can still create a new level.

## Contributing
Please read `CONTRIBUTING.md` for guidelines on code style, branching, and PRs.

## Next Steps
See `docs/tasks.md` for the improvement backlog and `docs/execution.md` for how we execute tasks incrementally.
