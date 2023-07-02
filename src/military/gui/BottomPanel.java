/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package military.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.swing.JPanel;
import military.engine.CombatStats;
import military.engine.Location;
import military.engine.LocationManager;
import military.engine.Unit;

/**
 *
 * @author Nate
 */
public class BottomPanel extends JPanel{
    private Point cursor = new Point(0,0);
    
    
    public void render(Point p){
        cursor = p;
        paintComponent(this.getGraphics());
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setBackground(Color.black);
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(new Font("Consolas", 0, 24));
        g2.setColor(Color.LIGHT_GRAY);
        if (LocationManager.getSize().x > cursor.x && LocationManager.getSize().y > cursor.y) {
            Location loc = LocationManager.getLoc(cursor);
            if (!loc.isEmpty()) {
                g2.drawImage(ModelManager.getModel(
                                        loc.getUnit().getModelName()).
                                getImage(loc.getUnit().getTeam())
                        , 30, 15, 50, 50, this);
                g2.drawString(loc.getUnit().getName() + " x" + loc.getUnit().getHealth(), 90, 50);
                g2.drawString("Exp: " + loc.getUnit().getExp(), 250, 50);
            }
            if (loc.getTerrain() != -1) {
                g2.drawString("Terrain: " + loc.getTerrain() + "%", 500, 50);
            }
        }
    }

    public void displayCombat(CombatStats cstat){
        boolean turn = cstat.getAttacker().getTeam();
        Graphics2D g2 = (Graphics2D) this.getGraphics();
        g2.setFont(new Font("Consolas", 0, 16));
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Attack: " + (turn ? cstat.getAttackerFA():cstat.getDefenderFA()), 50, 17);
        g2.drawString("Defense: " + (turn ? cstat.getAttackerFD():cstat.getDefenderFD()), 50, 39);
        g2.drawString("Exp: " + (turn ? cstat.getAttackerEB():cstat.getDefenderEB()), 50, 61);
        g2.drawString("Attack: " + (!turn ? cstat.getAttackerFA():cstat.getDefenderFA()), getWidth() - 150, 17);
        g2.drawString("Defense: " + (!turn ? cstat.getAttackerFD():cstat.getDefenderFD()), getWidth() - 150, 39);
        g2.drawString("Exp: " + (!turn ? cstat.getAttackerEB():cstat.getDefenderEB()), getWidth() - 150, 61);
        
        g2.drawString("Terrain", getWidth()/2-30, 20);
        g2.drawString((turn ? cstat.getAttackerTerrain():cstat.getAttackerTerrain())+ "%", getWidth()/2 -35, 50);
        g2.drawString((!turn ? cstat.getAttackerTerrain():cstat.getAttackerTerrain())+ "%", getWidth()/2 +20, 50);
        
        
        
        int surrMod[] = {0, 20, 30, 40, 50, 75};
        g2.drawString("Surround Effect", 200, 20);
        g2.drawString("-" + surrMod[cstat.getSurround()] + "% to defender", 200, 50);
        g2.drawString("Support Effect", getWidth() -300, 20);
        g2.drawString("+" + cstat.getDefenderDSup() + " to defense", getWidth() -300, 50);
        
    }
    
    public void updateExp(int blue, int red){
        Graphics2D g2 = (Graphics2D) this.getGraphics();
        g2.setFont(new Font("Consolas", 0, 16));
        g2.setColor(Color.BLACK);
        g2.fillRect(41, 45, 80, 20);
        g2.fillRect(getWidth() - 155, 45, 80, 20);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Exp: " + blue, 50, 61);
        g2.drawString("Exp: " + red, getWidth() - 150, 61);
    }
    
    public void factoryUnit(Unit u){
        Graphics2D g2 = (Graphics2D) this.getGraphics();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        if(u == null)
            return;
        g2.drawImage(ModelManager.getModel(
                    u.getModelName()).
                    getImage(u.getTeam())
                    , 30, 15, 50, 50, this);
        g2.setColor(Color.LIGHT_GRAY);
        g2.setFont(new Font("Consolas", 0, 16));
        g2.drawString(u.getName(), 100, 39);
        g2.drawString("Land Attack: " + (u.getLandAttack()), 170, 25);
        g2.drawString("Air Attack: " + (u.getAirAttack()), 170, 50);
        g2.drawString("Defense: " + u.getDefense(), 350, 25);
        g2.drawString("Speed: " + u.getShift(), 350, 50);
    }
    
}
