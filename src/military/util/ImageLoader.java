package military.util;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Abstraction for loading images.
 */
public interface ImageLoader {
    BufferedImage load(String relativePath) throws IOException;
}
