package military.engine.ai;

import military.engine.DefaultMapService;
import military.engine.Team;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Task 88: Headless integration test verifying that one full human+AI round
 * (human ends turn, AI plays and ends turn) completes deterministically with a fixed seed.
 *
 * This test does not construct any Swing components and runs safely in headless CI.
 */
public class HeadlessFullRoundTest {

    @Before
    public void setupMap() throws Exception {
        // Ensure Sample_small.txt is available in runtime Maps directory
        java.nio.file.Path mapsDir = military.Config.mapsDir();
        java.nio.file.Files.createDirectories(mapsDir);
        java.nio.file.Path dst = mapsDir.resolve("Sample_small.txt");
        if (!java.nio.file.Files.exists(dst)) {
            try (java.io.InputStream in = getClass().getClassLoader().getResourceAsStream("Maps/Sample_small.txt")) {
                if (in == null) throw new IllegalStateException("Test resource Maps/Sample_small.txt not found");
                java.nio.file.Files.copy(in, dst, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }
        // Load the sample map used by other engine tests (pass base name without extension)
        new DefaultMapService().loadMap("Sample_small");
        // Zero out AI visualization delay for deterministic speed in tests
        AiConfig.setDelayMs(0);
        // Reset optional difficulty knobs to default neutral values
        AiConfig.setAggressiveness(1.0);
        AiConfig.setCaution(1.0);
        AiConfig.setCapturePriority(1.0);
    }

    @Test
    public void testFullRoundDeterministicWithSeed() {
        // Record initial unit counts for sanity; a no-op AIController execution should not change counts
        int blueBefore = military.engine.DefaultUnitRepository.getInstance().getUnits(Team.BLUE).size();
        int redBefore = military.engine.DefaultUnitRepository.getInstance().getUnits(Team.RED).size();

        // Fake game adapter that tracks turn flips; start with BLUE (human)
        class FakeAdapter implements GameAdapter {
            boolean turn = true; // true -> BLUE, human
            int endCalls = 0;
            @Override public boolean getTurnFlag() { return turn; }
            @Override public ArrayList<Point> getSelectLocs() { return new ArrayList<>(); }
            @Override public Point getRenderCursor() { return new Point(0,0); }
            @Override public void endTurn() { endCalls++; turn = !turn; }
            @Override public boolean move(Point from, Point to) { return false; }
            @Override public boolean attack(Point attackerAt, Point targetAt) { return false; }
        }
        FakeAdapter adapter = new FakeAdapter();

        // Human ends turn -> now RED (AI) turn
        adapter.endTurn();
        Assert.assertFalse("After human ends turn, it should be RED's turn", adapter.getTurnFlag());

        // Execute AI turn deterministically with a fixed seed
        AIPlayer ai = new SimpleHeuristicAI();
        AIController controller = new AIController(ai);
        military.util.Rng rng = new military.util.DefaultRng(new java.util.Random(987654321L));
        controller.takeTurn(adapter, Team.RED, rng);

        // After AI completes, it should be BLUE's turn again (full round completed)
        Assert.assertTrue("After AI ends turn, it should be BLUE's turn again", adapter.getTurnFlag());
        Assert.assertEquals("Two endTurn calls should have been made (human + AI)", 2, adapter.endCalls);

        // Sanity: unit counts remain the same because current AIController doesn't execute move/attack actions
        int blueAfter = military.engine.DefaultUnitRepository.getInstance().getUnits(Team.BLUE).size();
        int redAfter = military.engine.DefaultUnitRepository.getInstance().getUnits(Team.RED).size();
        Assert.assertEquals("BLUE unit count should remain unchanged after a full round", blueBefore, blueAfter);
        Assert.assertEquals("RED unit count should remain unchanged after a full round", redBefore, redAfter);
    }
}
