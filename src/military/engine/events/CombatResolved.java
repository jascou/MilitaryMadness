package military.engine.events;

import military.engine.CombatStats;

public class CombatResolved implements GameEvent {
    public final CombatStats stats;
    public CombatResolved(CombatStats stats) {
        this.stats = stats;
    }
}
