/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package military.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.Timer;
import military.engine.CombatStats;
import military.engine.LocationManager;
import military.engine.Unit;

/**
 *
 * @author Nate
 */
public class HexGridPanel extends JPanel {

    // Viewport configuration constants
    private static final int VIEW_WIDTH = 15;
    private static final int VIEW_HEIGHT = 10;
    private static final int SCROLL_THRESHOLD_X = 13;
    private static final int SCROLL_THRESHOLD_Y = 8;
    private static final int TIMER_MS = 16;
    private static final int BULLET_STEP = 6;

    private ArrayList<Point> selectLocs = new ArrayList<>();
    private Point cursorLoc = new Point(0, 0);
    private final Viewport viewport = new Viewport(VIEW_WIDTH, VIEW_HEIGHT);

    // Combat animation state
    private boolean inCombat = false;
    private CombatStats combatStats;
    private Timer combatTimer;
    private int animX = 0;
    private int phase = 0; // 0: bullets, 1: explosions, 2: done
    private int phaseTicks = 0;
    private int bluehb = 0, redhb = 0;
    private Unit blue;
    private Unit red;
    private boolean turnForCombat;

    public void render(ArrayList<Point> select, Point cursor) {
        // Update state and request an EDT repaint instead of direct painting
        selectLocs = (select != null) ? new ArrayList<>(select) : new ArrayList<>();
        cursorLoc = (cursor != null) ? new Point(cursor) : new Point(0, 0);
        javax.swing.SwingUtilities.invokeLater(this::repaint);
    }

    public void render() {
        selectLocs = new ArrayList<>();
        cursorLoc = new Point(100, 100);
        javax.swing.SwingUtilities.invokeLater(this::repaint);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (selectLocs == null) { selectLocs = new java.util.ArrayList<>(); }
        if (cursorLoc == null) { cursorLoc = new Point(0, 0); }
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(6));
        // Background
        g2.setColor(Color.black);
        g2.fillRect(0, 0, getWidth(), getHeight());
        // Draw grid and selections
        Point corner = viewport.getCorner();
        HexMech.setCorner(corner);
        for (int i = corner.x; i < corner.x + VIEW_WIDTH; i++) {
            for (int j = corner.y; j < corner.y + VIEW_HEIGHT; j++) {
                HexMech.drawHex(i, j, g2);
            }
        }
        if (corner.x > 0) {
            for (int j = corner.y; j < corner.y + VIEW_HEIGHT; j++) {
                HexMech.drawHex(corner.x - 1, j, g2);
            }
        }
        if (corner.y > 0) {
            for (int i = corner.x; i < corner.x + VIEW_WIDTH; i++) {
                HexMech.drawHex(i, corner.y - 1, g2);
            }
        }
        if (corner.x < LocationManager.getSize().x - VIEW_WIDTH) {
            for (int j = corner.y; j < corner.y + VIEW_HEIGHT; j++) {
                HexMech.drawHex(corner.x + VIEW_WIDTH, j, g2);
            }
        }
        if (corner.y < LocationManager.getSize().y - VIEW_HEIGHT) {
            for (int i = corner.x; i < corner.x + VIEW_WIDTH; i++) {
                HexMech.drawHex(i, corner.y + VIEW_HEIGHT, g2);
            }
        }
        for (Point p : selectLocs) {
            HexMech.selectHex(p.x, p.y, g2);
        }
        HexMech.cursor(cursorLoc.x, cursorLoc.y, g2);
        // If a combat animation is active, draw it on top
        if (inCombat) {
            paintCombat(g2);
        }
    }

    public void drawCursor(Point cursor, boolean turn) {
        updateCursor(cursor, turn);
    }

    public Point getViewportCorner() {
        return viewport.getCorner();
    }

    public int getViewWidth() { return VIEW_WIDTH; }
    public int getViewHeight() { return VIEW_HEIGHT; }

    /**
     * Centers the viewport on the given map coordinate (clamped to bounds).
     */
    public void centerViewportOn(Point mapCoord) {
        if (mapCoord == null) return;
        Point desiredTopLeft = new Point(mapCoord.x - VIEW_WIDTH / 2, mapCoord.y - VIEW_HEIGHT / 2);
        viewport.setCorner(desiredTopLeft, military.engine.LocationManager.getSize().x, military.engine.LocationManager.getSize().y);
        repaint();
    }

    /**
     * Sets the viewport top-left corner directly (clamped to bounds).
     */
    public void setViewportCorner(Point topLeft) {
        if (topLeft == null) return;
        viewport.setCorner(new Point(topLeft), military.engine.LocationManager.getSize().x, military.engine.LocationManager.getSize().y);
        repaint();
    }

    public void updateCursor(Point cursor, boolean turn) {
        cursorLoc = (cursor != null) ? new Point(cursor) : new Point(0, 0);
        // Adjust viewport to keep cursor near edges according to thresholds
        viewport.adjustToCursor(cursorLoc, LocationManager.getSize().x, LocationManager.getSize().y,
                SCROLL_THRESHOLD_X, SCROLL_THRESHOLD_Y);
        repaint();
    }

    public void displayCombat(CombatStats cstat) {
        // Initialize animation state and start timer; avoid blocking UI thread
        this.combatStats = cstat;
        this.turnForCombat = cstat.getAttacker().getTeam();
        this.blue = (turnForCombat ? cstat.getAttacker() : cstat.getDefender());
        this.red = (turnForCombat ? cstat.getDefender() : cstat.getAttacker());
        this.bluehb = (turnForCombat ? cstat.getAttackerHB() : cstat.getDefenderHB());
        this.redhb = (!turnForCombat ? cstat.getAttackerHB() : cstat.getDefenderHB());
        this.animX = 90;
        this.phase = 0;
        this.phaseTicks = 0;
        this.inCombat = true;
        try { SoundUtility.getInstance().playSound("Launch.wav"); } catch (Exception ex) {
            java.util.logging.Logger log = military.util.Logs.getLogger(HexGridPanel.class);
            log.fine("Sound play failed (Launch.wav): " + ex.toString());
        }
        if (combatTimer != null) {
            combatTimer.stop();
        }
        combatTimer = new Timer(TIMER_MS, e -> {
            if (!inCombat) { ((Timer)e.getSource()).stop(); return; }
            if (phase == 0) {
                animX += BULLET_STEP;
                if (animX >= getWidth() - 85) {
                    phase = 1;
                    phaseTicks = 0;
                    try { SoundUtility.getInstance().playSound("Explosion.wav"); } catch (Exception ex) {
                        java.util.logging.Logger log = military.util.Logs.getLogger(HexGridPanel.class);
                        log.fine("Sound play failed (Explosion.wav): " + ex.toString());
                    }
                }
            } else if (phase == 1) {
                phaseTicks++;
                if (phaseTicks > 12) {
                    phase = 2;
                }
            } else {
                inCombat = false;
                ((Timer)e.getSource()).stop();
            }
            repaint();
        });
        combatTimer.start();
    }

    private void paintCombat(Graphics2D g2) {
        // Background halves
        g2.setColor(blue.getLoc().getColor());
        g2.fillRect(0, 0, getWidth() / 2, getHeight());
        g2.setColor(red.getLoc().getColor());
        g2.fillRect(getWidth() / 2, 0, getWidth() / 2, getHeight());
        g2.setColor(Color.black);
        // Draw unit images at sides
        for (int i = 0; i < bluehb; i++) {
            g2.drawImage(ModelManager.getModel(blue.getModelName()).getImage(true), 50, i * getHeight() / bluehb, 40, 40, null);
        }
        for (int i = 0; i < redhb; i++) {
            g2.drawImage(ModelManager.getModel(red.getModelName()).getImage(false), getWidth() - 90, i * getHeight() / redhb, 40, 40, null);
        }
        // Phase 0: bullet traces
        if (phase == 0) {
            int x = animX;
            for (int i = 0; i < bluehb; i++) {
                g2.setColor((x < getWidth() / 2) ? blue.getLoc().getColor() : red.getLoc().getColor());
                g2.fillRect(x - 1, 18 + i * getHeight() / bluehb, 3, 3);
                g2.setColor(Color.black);
                g2.fillRect(x, 18 + i * getHeight() / bluehb, 3, 3);
            }
            for (int i = 0; i < redhb; i++) {
                g2.setColor((x < getWidth() / 2) ? red.getLoc().getColor() : blue.getLoc().getColor());
                g2.fillRect(getWidth() - x + 1, 18 + i * getHeight() / redhb, 3, 3);
                g2.setColor(Color.black);
                g2.fillRect(getWidth() - x, 18 + i * getHeight() / redhb, 3, 3);
            }
        }
        // Phase 1: explosions overlays
        if (phase >= 1) {
            for (int i = 0; i < bluehb - blue.getHealth(); i++) {
                g2.drawImage(ModelManager.getModel("Explosion").getImage(true), 50, i * getHeight() / bluehb, 40, 40, null);
            }
            for (int i = 0; i < redhb - red.getHealth(); i++) {
                g2.drawImage(ModelManager.getModel("Explosion").getImage(false), getWidth() - 90, i * getHeight() / redhb, 40, 40, null);
            }
        }
        // Phase 2: fill destroyed blocks
        if (phase >= 2) {
            g2.setColor(blue.getLoc().getColor());
            for (int i = 0; i < bluehb - blue.getHealth(); i++) {
                g2.fillRect(50, i * getHeight() / bluehb, 40, 40);
            }
            g2.setColor(red.getLoc().getColor());
            for (int i = 0; i < redhb - red.getHealth(); i++) {
                g2.fillRect(getWidth() - 90, i * getHeight() / redhb, 40, 40);
            }
        }
    }

    // August 28, 2025 - commented out the contents because the board image is loading on top of the units and hex map.
    public void loadMapImage(String mapImageName) {

        try {
//            BufferedImage bimg = ImageIO.read(new File("Resources//maps/bd01v2.gif"));
//            int width = bimg.getWidth();
//            int height = bimg.getHeight();
//            int transparency = bimg.getTransparency();
//            Graphics2D g2 = (Graphics2D) this.getGraphics();
//            g2.drawImage(bimg, 60, 20, null);
        } catch (Exception ex) {
            java.util.logging.Logger logger = military.util.Logs.getLogger(HexGridPanel.class);
            logger.warning("Error loading map image: " + ex);
        }
    }

}
