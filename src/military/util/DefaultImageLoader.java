package military.util;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Default ImageLoader implementation that delegates to ResourceLoader.
 */
public class DefaultImageLoader implements ImageLoader {
    @Override
    public BufferedImage load(String relativePath) throws IOException {
        return ResourceLoader.loadImage(relativePath);
    }
}
