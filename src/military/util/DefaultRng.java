package military.util;

import java.util.Random;

/**
 * Default RNG backed by java.util.Random.
 */
public class DefaultRng implements Rng {
    private final Random rand;

    public DefaultRng() {
        this(new Random());
    }

    public DefaultRng(Random rand) {
        this.rand = rand;
    }

    @Override
    public int nextInt(int bound) {
        return rand.nextInt(bound);
    }

    @Override
    public double nextDouble() {
        return rand.nextDouble();
    }
}
