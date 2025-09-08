package military.engine;

/**
 * Centralized capability tweaks for specific units to match the original game.
 * Example: Rabbit can "shoot and scoot" (move, attack, move again).
 */
public final class UnitCapabilities {
    private UnitCapabilities() {}

    /** Apply known capabilities based on unit name/type. */
    public static void apply(Unit u) {
        if (u == null) return;
        String name = u.getName();
        if ("Rabbit".equals(name)) {
            // Rabbit: can move twice per turn and can move after attacking
            u.setMaxMovesPerTurn(2);
            u.setCanMoveAfterAttack(true);
        }
        // Future: add more unit-specific adjustments here.
    }
}
