package military.util;

/**
 * Abstraction over randomness to allow deterministic tests.
 */
public interface Rng {
    int nextInt(int bound);
    double nextDouble();
}
