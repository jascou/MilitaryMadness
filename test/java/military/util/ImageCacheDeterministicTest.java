package military.util;

import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageCacheDeterministicTest {

    private static class FakeImageLoader implements ImageLoader {
        @Override
        public BufferedImage load(String relativePath) throws IOException {
            BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.GREEN);
            g.fillRect(0,0,2,2);
            g.dispose();
            return img;
        }
    }

    @Test
    public void testDeterministicLoaderReturnsKnownImage() {
        ImageCache.clear();
        ImageCache.setImageLoader(new FakeImageLoader());
        BufferedImage a = ImageCache.get("any/path.png");
        BufferedImage b = ImageCache.get("any/path.png");
        Assert.assertNotNull(a);
        Assert.assertEquals(2, a.getWidth());
        Assert.assertEquals(2, a.getHeight());
        // cache returns same instance for same key
        Assert.assertTrue(a == b);
        // reset to default to avoid side effects on other tests
        ImageCache.setImageLoader(null);
        ImageCache.clear();
    }
}
