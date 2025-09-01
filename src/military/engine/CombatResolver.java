package military.engine;

/**
 * Decouples combat calculation from UI. Creates CombatStats and can be extended
 * to apply damage and randomness.
 */
public final class CombatResolver {
    private CombatResolver() {}

    public static CombatStats resolve(Unit attacker, Unit defender, Location terrain) {
        // For now, delegate to existing CombatStats constructor
        return new CombatStats(attacker, defender);
    }
}
