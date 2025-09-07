package military.engine;

/**
 * Encodes allowed turn rules to avoid scattering conditionals.
 */
public final class TurnRules {
    private TurnRules() {}

    // Core toggles
    public static final boolean ALLOW_MOVE_AFTER_ATTACK = false;
    public static final boolean ALLOW_ATTACK_AFTER_MOVE = true;

    // Additional rule toggles for clarity and future use
    public static final boolean ALLOW_FACTORY_DEPLOY_AND_MOVE_SAME_TURN = true;
    public static final boolean CAPTURE_ON_ENTRY = true; // capture an enemy base upon entering
    public static final boolean ENABLE_FLANKING_PENALTY = true; // pathfinding considers enemy adjacency as flanking
}
