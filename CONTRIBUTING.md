# Contributing to MilitaryMadness

Thank you for your interest in improving this project! This document describes how to contribute code and documentation changes safely and consistently.

## Coding Standards
- Language level: Java 8+.
- Naming: classes PascalCase, methods/fields camelCase, constants UPPER_SNAKE_CASE.
- Formatting: 4-space indentation, no tabs; keep lines reasonably short.
- Null-safety: validate inputs early; prefer Optional for optional returns; never return null Collections.
- Exceptions: prefer informative messages; catch only what you can handle; avoid empty catch blocks.
- Collections: use generics (e.g., ArrayList<String>) — no raw types.
- IO: use try-with-resources; close streams deterministically; avoid hardcoded separators.
- Logging: prefer java.util.logging (or SLF4J if introduced later) over System.out for non-trivial messages.

## Commits & Branching
- Keep commits small and focused with descriptive messages.
- Use feature branches: `feature/<short-name>` (e.g., `feature/maps-safety`).
- Rebase/squash before merge to keep history tidy.

## Tests
- Add or update unit tests when changing engine or map logic.
- Keep Swing UI out of unit tests; focus on pure logic.

## Build & Run
- Ant build (build.xml) or run via IDE.
- Main class: `military.MilitaryMadness`.
- Ensure working directory is project root until resource loading is centralized.

## Pull Request Checklist
- Code compiles, runs basic flows (game or designer).
- No raw types; streams closed; paths via `java.nio.file.Path` when possible.
- No busy-wait loops; Swing actions on EDT.
- Updated docs/tasks.md (checked off relevant items) and any affected docs.

Thanks for contributing!