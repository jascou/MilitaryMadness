package military.util;

import military.Config;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility for loading resources (images, sounds, text) with classpath-first strategy
 * and filesystem fallback. Includes a very small optional cache for images.
 */
public final class ResourceLoader {
    private static final Map<String, BufferedImage> imageCache = new ConcurrentHashMap<>();

    private ResourceLoader() {}

    /**
     * Attempts to open a resource as InputStream using the classpath first, then the filesystem.
     * The path is interpreted relative to the project root (e.g., "Resources/Units.txt").
     */
    public static InputStream openResource(String relativePath) throws IOException {
        String cp = relativePath.replace('\\', '/');
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ResourceLoader.class.getClassLoader();
        }
        InputStream in = cl.getResourceAsStream(cp);
        if (in != null) {
            return in;
        }
        // Fallback to filesystem
        Path fsPath = Path.of(relativePath);
        if (!fsPath.isAbsolute()) {
            fsPath = Path.of(relativePath);
        }
        return Files.newInputStream(fsPath);
    }

    /**
     * Loads an image using classpath-first, then filesystem fallback.
     * The path should be given relative to project root (e.g., "Resources/maps/bd01v2.gif").
     */
    public static BufferedImage loadImage(String relativePath) throws IOException {
        BufferedImage cached = imageCache.get(relativePath);
        if (cached != null) {
            return cached;
        }
        String cp = relativePath.replace('\\', '/');
        // Try classpath as URL
        URL url = ResourceLoader.class.getClassLoader().getResource(cp);
        if (url != null) {
            BufferedImage img = ImageIO.read(url);
            if (img != null) {
                imageCache.put(relativePath, img);
                return img;
            }
        }
        // Fallback to filesystem
        Path fsPath = Path.of(relativePath);
        BufferedImage img = ImageIO.read(fsPath.toFile());
        if (img != null) {
            imageCache.put(relativePath, img);
        }
        return img;
    }

    /**
     * Opens a text resource under Resources/ directory by file name.
     */
    public static InputStream openTextFromResources(String fileName) throws IOException {
        Path candidate = Config.resourcesDir().resolve(fileName);
        // Try classpath first
        String cp = ("Resources/" + fileName).replace('\\', '/');
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ResourceLoader.class.getClassLoader();
        }
        InputStream in = cl.getResourceAsStream(cp);
        if (in != null) {
            return in;
        }
        return Files.newInputStream(candidate);
    }

    /**
     * Resolves a sound file location on the filesystem using Config.
     */
    public static Path soundPath(String fileName) {
        return Config.soundsDir().resolve(fileName);
    }
}
