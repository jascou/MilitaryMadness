package military.engine;

/**
 * Encodes allowed turn rules to avoid scattering conditionals.
 */
public final class TurnRules {
    private TurnRules() {}

    public static final boolean ALLOW_MOVE_AFTER_ATTACK = false;
    public static final boolean ALLOW_ATTACK_AFTER_MOVE = true;
}
