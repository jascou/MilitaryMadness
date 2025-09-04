# Performance Profiling Plan (Java Flight Recorder)

This plan outlines how to profile MilitaryMadness to identify rendering hot spots (e.g., large maps) and expensive operations (image IO, painting, movement calculations).

Targets and Budgets:
- Frame target: 60 FPS -> 16.7 ms/frame budget on average.
- Painting budget (HexGridPanel.paintComponent): <= 8 ms/frame (P95).
- Image loading during gameplay: 0 ms in steady-state (all renders should use cached images).
- GC time: < 5% of wall time during active scrolling/combat.

Tools:
- Java Flight Recorder (JFR) and Java Mission Control (JMC)
- VisualVM (heap/CPU sampler) as an alternative on JDK 8

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

Tracking Checklist:
- [ ] P95 paintComponent() <= 8 ms (profiled on representative map).
- [ ] 0 image loads during steady-state scroll (verify via JFR IO events or custom logging).
- [ ] No EDT blocking calls (sleep, IO) within render paths.
- [ ] GC pressure acceptable (<5% wall time) during scroll/combat.

Follow-ups:
- Cache frequently accessed images earlier (ResourceLoader.preloadImages).
- Reduce layout churn by reusing Swing components.
- Consider batching repaints.
