package military.util;

/**
 * Abstraction for time retrieval to enable deterministic tests.
 */
public interface Clock {
    long nowMillis();
}
