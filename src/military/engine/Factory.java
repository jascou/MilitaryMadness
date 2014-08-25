/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package military.engine;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nate
 */
public class Factory extends Location{
    private boolean team;
    private boolean controlled;
    private ArrayList<Unit> units;
    
    public Factory(Point p){
        super(p, 0);
        controlled = false;
        units = new ArrayList<>();
    }
    
    public Factory(Point p, boolean team){
        super(p, 0);
        controlled = true;
        this.team = team;
        units = new ArrayList<>();
    }
    
    public List<Unit> getUnits(){
        return units;
    }
    
    public boolean addUnit(Unit u){
        if(units.size()==12){
            return false;
        }
        units.add(u);
        u.setLoc(this);
        if(u.getTeam() != team){
            control(!team);
        }
        return true;
    }
    
    public Unit removeUnit(Unit u){
        units.remove(u);
        return u;
    }
    
    public void control(boolean team){
        controlled = true;
        this.team = team;
        for(Unit u: units){
            u.setTeam(team);
        }
    }
    
    public boolean isEmpty(){
        return true;
    }
    
    public Color getColor(){
        return controlled ? (team ? Color.BLUE : Color.RED) : new Color(100, 150, 0);
    }
    
    public Unit getUnit(int n){
        return units.get(n);
    }

    public boolean getTeam() {
        return team;
    }

    public boolean isControlled() {
        return controlled;
    }
 
    public int getType(){
        return controlled ? (team ? 8 : 9) : 7;
    }
}
