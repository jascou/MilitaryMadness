package military.gui;

import military.engine.LocationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Mini map widget rendered outside of the playfield.
 * It shows terrain colors, the current viewport rectangle, and the cursor.
 *
 * This panel is headless-safe (it only draws when realized) and does not perform any IO.
 */
public class MiniMapPanel extends JPanel {
    public interface ClickListener {
        void onMiniMapClick(Point mapCoord);
    }

    private Point viewCorner = new Point(0, 0);
    private Point cursor = new Point(0, 0);
    private int viewWidth = 15;
    private int viewHeight = 10;
    private ClickListener clickListener;

    public MiniMapPanel() {
        setBackground(Color.BLACK);
        // Reasonable default size that fits the right-side buttons panel
        setPreferredSize(new Dimension(140, 140));
        setMinimumSize(new Dimension(100, 100));

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                navigate(e.getPoint());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                navigate(e.getPoint());
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setClickListener(ClickListener listener) {
        this.clickListener = listener;
    }

    public void render(Point viewCorner, Point cursor, int viewWidth, int viewHeight) {
        if (viewCorner != null) this.viewCorner = new Point(viewCorner);
        if (cursor != null) this.cursor = new Point(cursor);
        if (viewWidth > 0) this.viewWidth = viewWidth;
        if (viewHeight > 0) this.viewHeight = viewHeight;
        SwingUtilities.invokeLater(this::repaint);
    }

    private void navigate(Point panelPt) {
        if (clickListener == null || panelPt == null) return;
        try {
            Point mapSize = LocationManager.getSize();
            int mapW = Math.max(1, mapSize.x);
            int mapH = Math.max(1, mapSize.y);
            int pad = 6;
            int availW = Math.max(1, getWidth() - pad * 2);
            int availH = Math.max(1, getHeight() - pad * 2);
            int scale = Math.max(1, Math.min(availW / mapW, availH / mapH));
            int mmW = mapW * scale;
            int mmH = mapH * scale;
            int x0 = (getWidth() - mmW) / 2;
            int y0 = (getHeight() - mmH) / 2;
            int relX = panelPt.x - x0;
            int relY = panelPt.y - y0;
            int mx = clamp(relX / scale, 0, mapW - 1);
            int my = clamp(relY / scale, 0, mapH - 1);
            clickListener.onMiniMapClick(new Point(mx, my));
        } catch (Throwable t) {
            // ignore conversion errors
        }
    }

    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        try {
            Point mapSize = LocationManager.getSize();
            int mapW = Math.max(1, mapSize.x);
            int mapH = Math.max(1, mapSize.y);
            int pad = 6;
            int availW = Math.max(1, getWidth() - pad * 2);
            int availH = Math.max(1, getHeight() - pad * 2);
            int scale = Math.max(1, Math.min(availW / mapW, availH / mapH));
            int mmW = mapW * scale;
            int mmH = mapH * scale;
            int x0 = (getWidth() - mmW) / 2;   // center inside panel
            int y0 = (getHeight() - mmH) / 2;

            // Frame
            g2.setColor(new Color(30, 30, 30));
            g2.fillRect(x0 - 2, y0 - 2, mmW + 4, mmH + 4);

            // Terrain
            for (int x = 0; x < mapW; x++) {
                for (int y = 0; y < mapH; y++) {
                    Color c = LocationManager.getLoc(x, y).getColor();
                    g2.setColor(c);
                    g2.fillRect(x0 + x * scale, y0 + y * scale, scale, scale);
                }
            }

            // Viewport rectangle
            g2.setColor(Color.WHITE);
            int vx = x0 + viewCorner.x * scale;
            int vy = y0 + viewCorner.y * scale;
            g2.drawRect(vx, vy, viewWidth * scale, viewHeight * scale);

            // Cursor
            g2.setColor(Color.RED);
            int cx = x0 + cursor.x * scale;
            int cy = y0 + cursor.y * scale;
            g2.fillRect(cx, cy, Math.max(1, scale), Math.max(1, scale));
        } catch (Throwable t) {
            // Swallow to avoid breaking UI if LocationManager not ready
        }
    }
}
