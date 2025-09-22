package military.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import military.engine.Location;

/**
 * Procedural terrain painter for hex tiles. Produces lightweight textures so terrain
 * is more visually distinct than flat colors, without introducing external assets.
 *
 * Keeps a tiny cache of TexturePaints keyed by terrain type to avoid per-frame allocations.
 */
public final class TilePainter {
    private static final int TEX_SIZE = 16;
    private static final Map<Integer, Paint> CACHE = new HashMap<>();

    private TilePainter() {}

    /**
     * Fill the given polygon representing a terrain hex with a procedural texture.
     * Falls back to Location.getColor() solid fill when terrain is unknown.
     */
    public static void fillTerrainHex(Graphics2D g2, Polygon poly, Location loc) {
        if (loc == null || poly == null) return;
        int terr = loc.getTerrain();
        Paint p = CACHE.get(terr);
        if (p == null) {
            p = createPaint(loc);
            CACHE.put(terr, p);
        }
        Paint old = g2.getPaint();
        g2.setPaint(p);
        g2.fillPolygon(poly);
        g2.setPaint(old);
    }

    private static Paint createPaint(Location loc) {
        int terr = loc.getTerrain();
        Color base = loc.getColor();
        // Derive a darker stroke color for pattern
        Color stroke = darker(base, 0.55f);
        BufferedImage img = new BufferedImage(TEX_SIZE, TEX_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Background
            g.setColor(base);
            g.fillRect(0, 0, TEX_SIZE, TEX_SIZE);
            g.setColor(stroke);
            // Choose a simple pattern based on coarse terrain category (by decades: 0,10,20,30,40)
            int type = loc.getType();
            switch (type) {
                case 0: // plains/neutral
                    // gentle diagonal hatching
                    for (int x = -TEX_SIZE; x < TEX_SIZE * 2; x += 6) {
                        g.drawLine(x, 0, x + 6, TEX_SIZE);
                    }
                    break;
                case 1: // fields/roads
                    // alternating horizontal bands
                    for (int y = 0; y < TEX_SIZE; y += 4) {
                        g.drawLine(0, y, TEX_SIZE, y);
                    }
                    break;
                case 2: // hills
                    // small chevrons
                    for (int y = 2; y < TEX_SIZE; y += 6) {
                        for (int x = 0; x < TEX_SIZE; x += 8) {
                            g.drawLine(x, y, x + 4, y - 2);
                            g.drawLine(x + 4, y - 2, x + 8, y);
                        }
                    }
                    break;
                case 3: // forest
                    // stipple dots
                    for (int y = 1; y < TEX_SIZE; y += 4) {
                        for (int x = (y % 8 == 1 ? 1 : 3); x < TEX_SIZE; x += 6) {
                            g.fillRect(x, y, 2, 2);
                        }
                    }
                    break;
                case 4: // mountains
                    // rocky triangles
                    for (int y = 0; y < TEX_SIZE; y += 8) {
                        for (int x = 0; x < TEX_SIZE; x += 8) {
                            g.drawLine(x + 1, y + 7, x + 4, y + 2);
                            g.drawLine(x + 4, y + 2, x + 7, y + 7);
                            g.drawLine(x + 1, y + 7, x + 7, y + 7);
                        }
                    }
                    break;
                default:
                    // fallback to subtle cross-hatch for unknown
                    for (int x = -TEX_SIZE; x < TEX_SIZE * 2; x += 6) {
                        g.drawLine(x, 0, x + 6, TEX_SIZE);
                        g.drawLine(x, TEX_SIZE, x + 6, 0);
                    }
            }
        } finally {
            g.dispose();
        }
        // Anchor pattern at (0,0); Graphics2D will transform with polygon fill
        return new TexturePaint(img, new Rectangle(0, 0, TEX_SIZE, TEX_SIZE));
    }

    private static Color darker(Color c, float factor) {
        int r = Math.max((int)(c.getRed() * factor), 0);
        int g = Math.max((int)(c.getGreen() * factor), 0);
        int b = Math.max((int)(c.getBlue() * factor), 0);
        return new Color(r, g, b, c.getAlpha());
    }
}
