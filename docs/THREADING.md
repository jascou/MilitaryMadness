# Threading and Concurrency Guidelines

This project targets simple, predictable threading:

- Engine/game logic runs on a single engine thread (often the main thread if not using a separate loop thread).
- Swing UI runs on the Event Dispatch Thread (EDT); all UI mutations must occur on the EDT.
- Sound playback runs on a dedicated background thread (see SoundUtility) and communicates via a thread-safe queue.

## Access Rules
- Engine state (e.g., LocationManager, Units) should be mutated only from the engine thread.
- Views should consume immutable snapshots or read-only queries. HexGridPanel currently queries grid size
  via LocationManager.getSize() during painting; core read methods (getSize, getLoc, isInBounds) are synchronized
  to reduce race risks. Avoid calling mutating methods from the EDT.

## Do/Don't
- Do use SwingUtilities.invokeLater for UI updates from non-EDT threads.
- Do not perform blocking IO on the EDT.
- Do not mutate engine state from UI event handlers; dispatch intents to the controller/loop.

## Future Work
- Migrate UI to consume immutable GameState snapshots exclusively during paint to eliminate direct engine queries.
- Add unit tests around any future cross-thread access to shared structures.
