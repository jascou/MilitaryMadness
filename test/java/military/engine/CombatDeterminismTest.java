package military.engine;

import military.util.Rng;
import org.junit.Assert;
import org.junit.Test;

public class CombatDeterminismTest {

    private static class FixedRng implements Rng {
        private final double value;
        public FixedRng(double v) { this.value = v; }
        @Override public int nextInt(int bound) { return (int)Math.floor(value * Math.max(1, bound)); }
        @Override public double nextDouble() { return value; }
    }

    @Test
    public void combatWithFixedRngIsDeterministic() {
        // Build 2 adjacent tiles and 2 simple units
        Location[] grid = TestBuilders.twoAdjacent();
        Unit a1 = TestBuilders.unit("A", false, false, true, 10, 0, 1, 5, 3);
        Unit d1 = TestBuilders.unit("D", false, false, false, 8, 0, 1, 4, 3);
        grid[0].addUnit(a1);
        grid[1].addUnit(d1);

        // Set RNG to fixed value to eliminate randomness
        CombatStats.setRng(new FixedRng(0.5));

        int aStart = a1.getHealth();
        int dStart = d1.getHealth();
        CombatResolver.resolve(a1, d1, grid[1]);
        int aAfter = a1.getHealth();
        int dAfter = d1.getHealth();

        // Repeat with identical setup and RNG; expect identical results
        Location[] grid2 = TestBuilders.twoAdjacent();
        Unit a2 = TestBuilders.unit("A", false, false, true, 10, 0, 1, 5, 3);
        Unit d2 = TestBuilders.unit("D", false, false, false, 8, 0, 1, 4, 3);
        grid2[0].addUnit(a2);
        grid2[1].addUnit(d2);
        CombatStats.setRng(new FixedRng(0.5));
        CombatResolver.resolve(a2, d2, grid2[1]);

        Assert.assertTrue("Attacker should not gain health", aAfter <= aStart);
        Assert.assertTrue("Defender should not gain health", dAfter <= dStart);
        Assert.assertEquals("Deterministic attacker health across runs", a2.getHealth(), aAfter);
        Assert.assertEquals("Deterministic defender health across runs", d2.getHealth(), dAfter);
    }
}
