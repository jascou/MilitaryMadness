/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package military.engine;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author Nate
 */
public class Location {
    // TODO: Define location constants

    private final Point loc;
    private Location adjacent[]; // like a clock
    private int terrain;

    public void setTerrain(int terrain) {
        this.terrain = terrain;
    }
    private Unit unit;
    

    public Location(Point loc, int terrain) {
        this.loc = loc;
        this.terrain = terrain;
        unit = null;
    }
    
    public void setAdjacent(Location adjacent[]){
        this.adjacent = adjacent;
    }
    
    public ArrayList<Unit> getAjacentUnits(){
        ArrayList<Unit> ajunits = new ArrayList<>();
        for(Location l: adjacent){
            if(l.getUnit()!=null){
                ajunits.add(l.getUnit());
            }
        }
        return ajunits;
    }

    public boolean isEmpty(){
        return unit == null;
    }
    
    public Location[] getAdjacent() {
        return adjacent;
    }

    public int getTerrain() {
        return terrain;
    }
    
    public int getType(){
        if(terrain == -1){
            return -1;
        }
        return terrain/10;
    }

    public Color getColor() {
        if(terrain == 0)
            return Color.LIGHT_GRAY;
        if(terrain == 10)
            return new Color(100, 150, 0);
        if(terrain == 20)
            return new Color(100, 100, 0);
        if(terrain == 30)
            return new Color(0, 125, 0);
        if(terrain == 40)
            return new Color(0, 65, 0);
        return Color.BLACK;
    }
    
    public Point getLoc() {
        return loc;
    }
    
    public Unit getUnit(){
        return unit;
    }
    
    public boolean addUnit(Unit u){
        if(unit == null){
            unit = u;
            unit.setLoc(this);
            return true;
        }
        return false;
    }
    
    public Unit removeUnit(){
        Unit u = unit;
        unit = null;
        return u;
    }
    
    
}
