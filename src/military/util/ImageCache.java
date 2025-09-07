package military.util;

import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple global image cache to avoid repeated disk IO and per-frame ImageIO.read calls.
 * Delegates actual loading via an ImageLoader (classpath-first, filesystem fallback by default).
 */
public final class ImageCache {
    private ImageCache() {}

    private static final ConcurrentMap<String, BufferedImage> CACHE = new ConcurrentHashMap<>();
    private static volatile ImageLoader LOADER = new DefaultImageLoader();

    /**
     * Override the image loader (test hook). Pass null to reset to default.
     */
    public static void setImageLoader(ImageLoader loader) {
        LOADER = (loader != null) ? loader : new DefaultImageLoader();
    }

    public static BufferedImage get(String path) {
        return CACHE.computeIfAbsent(path, p -> {
            try {
                return LOADER.load(p);
            } catch (Exception ex) {
                return placeholder();
            }
        });
    }

    public static void put(String key, BufferedImage img) {
        if (key != null && img != null) {
            CACHE.put(key, img);
        }
    }

    public static void clear() {
        CACHE.clear();
    }

    private static java.awt.image.BufferedImage placeholder() {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        return img;
    }
}
