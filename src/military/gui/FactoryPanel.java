/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package military.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.swing.JPanel;
import military.engine.Factory;
import military.engine.Unit;

/**
 *
 * @author Nate
 */
public class FactoryPanel extends JPanel {

    private Point cursor = new Point(0, 0);
    private Factory fact;

    public void displayFactory(Factory factory) {
        fact = factory;
        cursor = new Point(0, 0);
        Graphics2D g2 = (Graphics2D) this.getGraphics();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                g2.drawImage(ModelManager.getModel("RoadBack").getImage(),
                        i * getWidth() / 4, j * getHeight() / 3, getWidth() / 4, getHeight() / 3, this);
                if (factory.getUnits().size() > i + (j * 4)) {
                    if (factory.getUnit(i + (j * 4)).isAttackDone() || !factory.isControlled()) {
                        g2.drawImage(ModelManager.getModel(factory.getUnit(i + (j * 4)).getModelName()).getGreyImage(factory.getTeam()),
                                50 + i * getWidth() / 4, 30 + j * getHeight() * 4, 80, 80, this);
                    } else {
                        g2.drawImage(ModelManager.getModel(factory.getUnit(i + (j * 4)).getModelName()).getImage(factory.getTeam()),
                                50 + i * getWidth() / 4, 50 + j * getHeight() * 4, 80, 80, this);
                    }
                }
            }
        }
        drawCursor(cursor);
    }

    public void drawCursor(Point p) {
        Graphics2D g2 = (Graphics2D) this.getGraphics();
        g2.setColor(Color.BLACK);
        g2.drawRect(cursor.x * getWidth() / 4, cursor.y * getHeight() / 3, getWidth() / 4, getHeight() / 3);
        g2.setColor(fact.getColor());
        cursor = p;
        g2.drawRect(cursor.x * getWidth() / 4, cursor.y * getHeight() / 3, getWidth() / 4, getHeight() / 3);
    }

    public Factory getFact() {
        return fact;
    }
    
    public Unit getUnit(){
        try {
            return fact.getUnit(cursor.x + cursor.y * 4);
        } catch (Exception e) {
            return null;
        }
    }

}
