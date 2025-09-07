package military.gui;

import org.junit.Assert;
import org.junit.Test;

public class GuiLayoutTest {

    @Test
    public void testGuiConstructsAndHasContent() {
        System.setProperty("java.awt.headless", "true");
        GUI gui = new GUI("Sample_small");
        // Should have some components laid out
        int count = gui.getContentPane().getComponentCount();
        Assert.assertTrue("GUI should have some components", count > 0);
        gui.dispose();
    }
}
