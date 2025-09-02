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
import java.awt.image.BufferedImage;
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

    private ArrayList<Point> selectLocs = new ArrayList<>();
    private Point cursorLoc = new Point(0, 0);
    private Point corner = new Point(0, 0);

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
        HexMech.setCorner(corner);
        for (int i = corner.x; i < corner.x + 15; i++) {
            for (int j = corner.y; j < corner.y + 10; j++) {
                HexMech.drawHex(i, j, g2);
            }
        }
        if (corner.x > 0) {
            for (int j = corner.y; j < corner.y + 10; j++) {
                HexMech.drawHex(corner.x - 1, j, g2);
            }
        }
        if (corner.y > 0) {
            for (int i = corner.x; i < corner.x + 15; i++) {
                HexMech.drawHex(i, corner.y - 1, g2);
            }
        }
        if (corner.x < LocationManager.getSize().x - 15) {
            for (int j = corner.y; j < corner.y + 10; j++) {
                HexMech.drawHex(corner.x + 15, j, g2);
            }
        }
        if (corner.y < LocationManager.getSize().y - 10) {
            for (int i = corner.x; i < corner.x + 15; i++) {
                HexMech.drawHex(i, corner.y + 10, g2);
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

    public void updateCursor(Point cursor, boolean turn) {
        cursorLoc = (cursor != null) ? new Point(cursor) : new Point(0, 0);
        // Adjust corner to keep cursor near center region
        if (cursorLoc.x == corner.x && cursorLoc.x != 0) {
            corner.x -= 2;
        } else if (cursorLoc.y == corner.y && cursorLoc.y != 0) {
            corner.y -= 2;
        } else if (cursorLoc.x == corner.x + 13 && cursorLoc.x < LocationManager.getSize().x - 2) {
            corner.x += 2;
        } else if (cursorLoc.y == corner.y + 8 && cursorLoc.y < LocationManager.getSize().y - 2) {
            corner.y += 2;
        }
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
        try { SoundUtility.getInstance().playSound("Launch.wav"); } catch (Exception ignore) {}
        if (combatTimer != null) {
            combatTimer.stop();
        }
        combatTimer = new Timer(16, e -> {
            if (!inCombat) { ((Timer)e.getSource()).stop(); return; }
            if (phase == 0) {
                animX += 6;
                if (animX >= getWidth() - 85) {
                    phase = 1;
                    phaseTicks = 0;
                    try { SoundUtility.getInstance().playSound("Explosion.wav"); } catch (Exception ignore) {}
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
