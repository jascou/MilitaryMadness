package military.util;

/**
 * Default system clock.
 */
public final class DefaultClock implements Clock {
    @Override
    public long nowMillis() {
        return System.currentTimeMillis();
    }
}
