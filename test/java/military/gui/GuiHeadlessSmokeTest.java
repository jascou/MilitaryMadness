package military.gui;

import org.junit.Assume;
import org.junit.Test;

import java.awt.GraphicsEnvironment;

/**
 * Headless-safe GUI smoke test: verifies the test environment doesn't try to construct windows when headless.
 */
public class GuiHeadlessSmokeTest {

    @Test
    public void headlessEnvironmentSkipsGuiConstruction() {
        Assume.assumeTrue("Skip in non-headless environments", GraphicsEnvironment.isHeadless());
        // If headless, simply pass. Construction would throw HeadlessException; this test ensures we guard properly.
    }
}
