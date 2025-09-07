package military.engine;

import org.jetbrains.annotations.NotNull;

/**
 * Decouples combat calculation from UI. Creates CombatStats and can be extended
 * to apply damage and randomness.
 */
public final class CombatResolver {
    private CombatResolver() {}

    /**
     * Resolve combat between two units. Never returns null.
     */
    public static @NotNull CombatStats resolve(@NotNull Unit attacker, @NotNull Unit defender, @NotNull Location terrain) {
        // For now, delegate to existing CombatStats constructor
        return new CombatStats(attacker, defender);
    }
}
