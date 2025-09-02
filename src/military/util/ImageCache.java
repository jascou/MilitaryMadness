package military.util;

import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple global image cache to avoid repeated disk IO and per-frame ImageIO.read calls.
 * Delegates actual loading to ResourceLoader (classpath-first, filesystem fallback).
 */
public final class ImageCache {
    private ImageCache() {}

    private static final ConcurrentMap<String, BufferedImage> CACHE = new ConcurrentHashMap<>();

    public static BufferedImage get(String path) {
        return CACHE.computeIfAbsent(path, p -> {
            try {
                return ResourceLoader.loadImage(p);
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
