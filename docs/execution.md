# Executing the Improvement Tasks

This document explains how to work through the checklist in `docs/tasks.md` efficiently and safely. It defines prioritization, the task lifecycle, branching strategy, review/testing expectations, and minimal build/run instructions for this repository.

## 1) Prioritization
Use these priority levels to select what to do next:
- P0 – Broken build, data loss, or security risk. Fix immediately.
- P1 – High impact on stability/architecture or developer productivity (e.g., resource loading, path handling, threading fixes).
- P2 – Medium impact refactors and cleanups (e.g., enums, constants extraction, logging).
- P3 – Nice-to-haves (docs polish, previews, telemetry).

Start with quick, low-risk wins that unlock others:
- Path normalization and resource loading utilities
- Removing busy-waits and ensuring Swing on EDT
- Generics and basic null/IO handling

Then move to larger refactors in small, incremental PRs.

## 2) Definition of Done (DoD)
A task is done when:
- The code compiles and the game/designer still launches.
- Relevant tests (if present) pass; for new logic add unit tests when feasible.
- No new warnings/errors are introduced in CI (if configured).
- Docs updated (README/CONTRIBUTING or inline Javadocs) as needed.
- `docs/tasks.md` is updated: the corresponding [ ] is checked [x] and notes/links to PRs are added.

## 3) Branching & Commit Strategy
- Main branch: always green (buildable and runnable).
- Feature branches: `feature/<task-short-name>` (e.g., `feature/resource-loader`).
- Commits: small, descriptive messages ("SoundUtility: replace wait/notify with queue and try-with-resources").
- Rebase or squash before merge if history is noisy.

## 4) Task Lifecycle
1. Pick a task from `docs/tasks.md` (prefer P1s), create an issue/PR stub if using a tracker.
2. Create a feature branch.
3. Implement incrementally; prefer small, reviewable commits.
4. Build locally and run smoke tests (see Build & Run below).
5. If applicable, add/adjust unit tests (start with engine logic; GUI via headless where possible).
6. Update docs (Javadocs/comments and any related docs file).
7. Update `docs/tasks.md`: mark the item as in-progress in your PR description and check it ([x]) after merge.
8. Open a PR; request review. Address feedback promptly.
9. Merge when green; delete the branch.

## 5) Testing Guidance (Practical Minimum)
- Short term: add tests around engine and map parsing/saving (LocationManager, Unit movement/attack calculations). Keep Swing UI out of unit tests initially.
- Determinism: inject RNG seeds for any random logic.
- IO: prefer classpath resources in tests; add small sample maps to test resources.

## 6) Build & Run (Local)
This repo includes an Ant `build.xml` and compiled classes under `out/`. Use Ant or your IDE (IntelliJ/NetBeans) to run.

- Build with Ant (if Ant is configured on your machine):
  - In IDE: run the default Ant target.
  - From terminal (PowerShell): `ant` (requires Apache Ant installed and JAVA_HOME set).

- Run from IDE:
  - Main class: `military.MilitaryMadness`
  - Ensure working directory is the project root so `Maps` and `Resources` folders are discoverable by current code.

- Assets:
  - Maps are under `Maps/` (e.g., `Maps\Revolt.txt`).
  - Images under `Resources/` and `Resources/sprites/`.
  - Sounds under `sounds/`.

Note: Several tasks will migrate file loading to classpath resources with proper fallbacks; for now, keep the working directory at project root when launching.

## 7) Code Review Checklist (Quick)
- No busy-wait loops; Swing interactions on EDT.
- Paths use `java.nio.file.Path` or a config utility; avoid hardcoded separators.
- Streams are closed; use try-with-resources.
- No raw types (use generics).
- Logging instead of System.out for non-trivial flows.
- Null/IO errors handled with helpful messages.

## 8) Recommended Execution Order (First Milestone)
1. Paths & resource centralization (Tasks #4–7, #51). 
2. UI threading and busy-wait removal (Tasks #10–11, #52–53).
3. Safety & hygiene: generics, try-with-resources, logging, input validation (Tasks #12–15, #25–27, #65).
4. Map load/save validation (Tasks #34–35, #69), plus a couple of unit tests (#19–20).

Ship these as separate small PRs (4–8 PRs total).

## 9) Tracking Progress
- Update `docs/tasks.md` for each merged PR: change `[ ]` to `[x]` and add a brief note or PR link.
- Optionally mirror items as issues in your tracker for assignment and discussion.

## 10) Communication & Ownership
- Assign a single owner per task/PR.
- Use PR descriptions to capture scope, rationale, and testing notes.
- For risky refactors, propose a short design in the PR or a `docs/` note first.

## 11) Rollback/Recovery
- Keep changes small; if regressions occur, revert the PR quickly and iterate on a follow-up branch.

This process is intentionally lightweight to keep momentum while improving code quality. As CI/tests mature, tighten the DoD accordingly.
