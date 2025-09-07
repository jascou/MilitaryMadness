package military.util;

import org.junit.Assert;
import org.junit.Test;

import java.awt.image.BufferedImage;

public class ResourceFallbackTest {

    @Test
    public void missingImageReturnsPlaceholderNotNull() throws Exception {
        String nonexistent = "Resources/does/not/exist.png";
        BufferedImage img = ResourceLoader.loadImage(nonexistent);
        Assert.assertNotNull(img);
        // Placeholder is 64x64 per implementation
        Assert.assertEquals(64, img.getWidth());
        Assert.assertEquals(64, img.getHeight());
    }
}
