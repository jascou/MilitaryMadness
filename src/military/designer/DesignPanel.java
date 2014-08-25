/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package military.designer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.swing.JPanel;
import military.engine.LocationManager;
import military.gui.HexMech;

/**
 *
 * @author Nate
 */
public class DesignPanel extends JPanel{
    
    public void render(){
        paintComponent(this.getGraphics());
    }
    
    public void draw(int x, int y){
        HexMech.drawHex(x, y, (Graphics2D)this.getGraphics());
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setBackground(Color.black);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(6));
        HexMech.setCorner(new Point(0,0));
        for (int i = 0; i < LocationManager.getSize().x; i++) {
            for (int j = 0; j < LocationManager.getSize().y; j++) {
                HexMech.drawHex(i, j, g2);
            }
        }
    }
}
