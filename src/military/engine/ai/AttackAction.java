package military.engine.ai;

import java.awt.Point;
import java.util.Objects;

/**
 * Attack an enemy unit from attacker position to target position.
 * Actual legality and result are evaluated by the engine.
 */
public final class AttackAction implements AiAction {
    private final Point attackerAt;
    private final Point targetAt;

    public AttackAction(Point attackerAt, Point targetAt) {
        this.attackerAt = new Point(Objects.requireNonNull(attackerAt, "attackerAt"));
        this.targetAt = new Point(Objects.requireNonNull(targetAt, "targetAt"));
    }

    public Point getAttackerAt() { return new Point(attackerAt); }
    public Point getTargetAt() { return new Point(targetAt); }

    @Override
    public String toString() {
        return "AttackAction{" +
                "attackerAt=" + attackerAt +
                ", targetAt=" + targetAt +
                '}';
    }
}
