# Performance Profiling Plan (Java Flight Recorder)

This plan outlines how to profile MilitaryMadness to identify rendering hot spots (e.g., large maps) and expensive operations (image IO, painting, movement calculations).

Tools:
- Java Flight Recorder (JFR) and Java Mission Control (JMC)

Scenarios:
1. Load a medium-to-large map and exercise movement/attack ranges repeatedly.
2. Scroll/paint the grid extensively to capture rendering costs.
3. Trigger combat popups and factory UI.

How to record (JDK 8u40+ with JFR, or newer JDK):
- From command line:
```
java -XX:StartFlightRecording=filename=mm.jfr,duration=120s,settings=profile -jar build/libs/MilitaryMadness-*.jar
```
- Or start/stop programmatically using `jdk.jfr.Recording` on newer JDKs (if app is upgraded).

What to analyze:
- Hot methods in painting (HexGridPanel.paintComponent) and image loading paths.
- Excessive allocations or GC pressure during render loops.
- Synchronization/contention on event queues or sound system (should be minimal after queue refactor).
- IO on map or resource loading during gameplay (should be cached/preloaded where feasible).

Follow-ups:
- Cache frequently accessed images earlier (ResourceLoader.preloadImages).
- Reduce layout churn by reusing Swing components.
- Consider batching repaints.
