package military.engine.ai;

import military.engine.DefaultMapService;
import military.engine.Team;
import military.util.DefaultRng;
import military.util.Rng;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.Point;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Task 80: Deterministic headless unit test ensuring AIController with stub/simple AI
 * ends its turn without errors on Sample_small.txt.
 */
public class AiControllerDeterministicTest {

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
    }

    @Test
    public void testStubAiEndsTurnImmediately() {
        // Use deterministic RNG with a fixed seed
        Rng rng = new DefaultRng(new java.util.Random(12345L));
        AIPlayer ai = new SimpleHeuristicAI(); // may propose a move, but controller will at least end turn
        AIController controller = new AIController(ai);

        // Fake game adapter that records endTurn calls; start with RED's turn for variety
        class FakeAdapter implements GameAdapter {
            boolean turn = false; // false -> RED
            int endCalls = 0;
            @Override public boolean getTurnFlag() { return turn; }
            @Override public ArrayList<Point> getSelectLocs() { return new ArrayList<>(); }
            @Override public Point getRenderCursor() { return new Point(0,0); }
            @Override public void endTurn() { endCalls++; turn = !turn; }
            @Override public boolean move(Point from, Point to) { return false; }
            @Override public boolean attack(Point attackerAt, Point targetAt) { return false; }
        }
        FakeAdapter adapter = new FakeAdapter();

        controller.takeTurn(adapter, Team.RED, rng);

        Assert.assertEquals("AI should have ended its turn exactly once", 1, adapter.endCalls);
        Assert.assertTrue("Turn should have flipped after AI ends turn", adapter.getTurnFlag());
    }
}
