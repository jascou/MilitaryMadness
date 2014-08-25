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
import military.engine.CombatStats;
import military.engine.LocationManager;
import military.engine.Unit;

/**
 *
 * @author Nate
 */
public class HexGridPanel extends JPanel {

    private ArrayList<Point> selectLocs;
    private Point cursorLoc = new Point(0, 0);
    private Point corner = new Point(0, 0);

    public void render(ArrayList<Point> select, Point cursor) {
        selectLocs = select;
        cursorLoc = cursor;
        paintComponent(this.getGraphics());
    }

    public void render() {
        selectLocs = new ArrayList<>();
        cursorLoc = new Point(100, 100);
        paintComponent(this.getGraphics());
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(6));
        BufferedImage bimg = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2img = (Graphics2D)bimg.getGraphics();
        g2img.setColor(Color.black);
        g2img.fillRect(0,0,bimg.getWidth(), bimg.getHeight());
        HexMech.setCorner(corner);
        for (int i = corner.x; i < corner.x + 15; i++) {
            for (int j = corner.y; j < corner.y + 10; j++) {
                HexMech.drawHex(i, j, g2img);
            }
        }
        
        if(corner.x > 0){
            for (int j = corner.y; j < corner.y + 10; j++) {
                HexMech.drawHex(corner.x-1, j, g2img);
            }
        }
        if(corner.y > 0){
            for (int i = corner.x; i < corner.x + 15; i++) {
                HexMech.drawHex(i, corner.y-1, g2img);
            }
        }
        if(corner.x < LocationManager.getSize().x-15){
            for (int j = corner.y; j < corner.y + 10; j++) {
                HexMech.drawHex(corner.x+15, j, g2img);
            }
        }
        if(corner.y < LocationManager.getSize().y -10){
            for (int i = corner.x; i < corner.x + 15; i++) {
                HexMech.drawHex(i, corner.y+10, g2img);
            }
        }
        
        for (Point p : selectLocs) {
            HexMech.selectHex(p.x, p.y, g2img);
        }
        HexMech.cursor(cursorLoc.x, cursorLoc.y, g2img);
        g2.drawImage(bimg, 0, 0, null);
    }

    public void drawCursor(Point cursor, boolean turn) {

        Graphics2D g2 = (Graphics2D) this.getGraphics();
        HexMech.drawHex(cursorLoc.x, cursorLoc.y, g2);
        for (Point p : selectLocs) {
            if (p.x == cursorLoc.x && p.y == cursorLoc.y) {
                HexMech.selectHex(p.x, p.y, g2);
            }
        }
        cursorLoc = cursor;
        if (cursorLoc.x == corner.x && cursorLoc.x != 0) {
            corner.x -= 2;
            paintComponent(this.getGraphics());
            return;
        }
        if (cursorLoc.y == corner.y && cursorLoc.y != 0) {
            corner.y -= 2;
            paintComponent(this.getGraphics());
            return;
        }
        if (cursorLoc.x == corner.x + 13 && cursorLoc.x < LocationManager.getSize().x - 2) {
            corner.x += 2;
            paintComponent(this.getGraphics());
            return;
        }
        if (cursorLoc.y == corner.y + 8 && cursorLoc.y < LocationManager.getSize().y - 2) {
            corner.y += 2;
            paintComponent(this.getGraphics());
            return;
        }
        HexMech.cursor(cursorLoc.x, cursorLoc.y, turn, g2);
    }

    public void displayCombat(CombatStats cstat) {
        boolean turn = cstat.getAttacker().getTeam();
        Unit blue = (turn ? cstat.getAttacker() : cstat.getDefender());
        Unit red = (turn ? cstat.getDefender() : cstat.getAttacker());
        Graphics2D g2 = (Graphics2D) this.getGraphics();
        g2.setColor(blue.getLoc().getColor());
        g2.fillRect(0, 0, getWidth() / 2, getHeight());
        g2.setColor(red.getLoc().getColor());
        g2.fillRect(getWidth() / 2, 0, getWidth() / 2, getHeight());
        g2.setColor(Color.black);
        int bluehb = (turn ? cstat.getAttackerHB() : cstat.getDefenderHB());
        int redhb = (!turn ? cstat.getAttackerHB() : cstat.getDefenderHB());
        SoundUtility.getInstance().playSound("Launch.wav");
        for (int i = 0; i < bluehb; i++) {
            g2.drawImage(ModelManager.getModel(blue.getModelName()).getImage(true), 50, i * getHeight() / bluehb, 40, 40, null);
        }
        for (int i = 0; i < redhb; i++) {
            g2.drawImage(ModelManager.getModel(red.getModelName()).getImage(false), getWidth() - 90, i * getHeight() / redhb, 40, 40, null);
        }

        for (int x = 90; x < getWidth() - 85; x++) {
            for (int i = 0; i < bluehb; i++) {
                if (x < getWidth() / 2) {
                    g2.setColor(blue.getLoc().getColor());
                } else {
                    g2.setColor(red.getLoc().getColor());
                }
                g2.fillRect(x - 1, 18 + i * getHeight() / bluehb, 3, 3);
                g2.setColor(Color.black);
                g2.fillRect(x, 18 + i * getHeight() / bluehb, 3, 3);
            }
            for (int i = 0; i < redhb; i++) {
                if (x < getWidth() / 2) {
                    g2.setColor(red.getLoc().getColor());
                } else {
                    g2.setColor(blue.getLoc().getColor());
                }
                g2.fillRect(getWidth() - x + 1, 18 + i * getHeight() / redhb, 3, 3);
                g2.setColor(Color.black);
                g2.fillRect(getWidth() - x, 18 + i * getHeight() / redhb, 3, 3);
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
            }
        }
        SoundUtility.getInstance().playSound("Explosion.wav");
        for (int i = 0; i < bluehb - blue.getHealth(); i++) {
            g2.drawImage(ModelManager.getModel("Explosion").getImage(true), 50, i * getHeight() / bluehb, 40, 40, null);
        }
        for (int i = 0; i < redhb - red.getHealth(); i++) {
            g2.drawImage(ModelManager.getModel("Explosion").getImage(false), getWidth() - 90, i * getHeight() / redhb, 40, 40, null);
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {

        }
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
