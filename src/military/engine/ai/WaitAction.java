package military.engine.ai;

/**
 * Optional: explicit wait/no-op action. Can be useful to pace execution
 * or to reserve unit actions without moving or attacking.
 */
public final class WaitAction implements AiAction {
    private final long millis;

    public WaitAction(long millis) {
        if (millis < 0) throw new IllegalArgumentException("millis must be >= 0");
        this.millis = millis;
    }

    public long getMillis() { return millis; }

    @Override
    public String toString() {
        return "WaitAction{" +
                "millis=" + millis +
                '}';
    }
}
