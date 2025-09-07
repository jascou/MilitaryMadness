package military.gui;

import java.awt.Point;

/**
 * Viewport model to manage a visible window over a larger grid.
 * Holds the top-left corner (in tile coordinates) and enforces bounds.
 */
public final class Viewport {
    private final int viewWidth;
    private final int viewHeight;
    private final Point corner = new Point(0, 0);

    public Viewport(int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    public Point getCorner() {
        return new Point(corner);
    }

    public void setCorner(Point p, int mapWidth, int mapHeight) {
        corner.x = clamp(p.x, 0, Math.max(0, mapWidth - viewWidth));
        corner.y = clamp(p.y, 0, Math.max(0, mapHeight - viewHeight));
    }

    public void adjustToCursor(Point cursor, int mapWidth, int mapHeight, int thresholdX, int thresholdY) {
        if (cursor == null) return;
        if (cursor.x == corner.x && corner.x > 0) {
            corner.x = Math.max(0, corner.x - 2);
        } else if (cursor.y == corner.y && corner.y > 0) {
            corner.y = Math.max(0, corner.y - 2);
        } else if (cursor.x == corner.x + thresholdX && cursor.x < mapWidth - 2) {
            corner.x = Math.min(mapWidth - viewWidth, corner.x + 2);
        } else if (cursor.y == corner.y + thresholdY && cursor.y < mapHeight - 2) {
            corner.y = Math.min(mapHeight - viewHeight, corner.y + 2);
        }
    }

    public int getViewWidth() { return viewWidth; }
    public int getViewHeight() { return viewHeight; }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
