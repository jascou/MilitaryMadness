/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package military.engine;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nate
 */
public class UnitManager {
    private static final UnitManager INSTANCE = new UnitManager();
    private ArrayList<Unit> blueUnits;
    private ArrayList<Unit> redUnits;
    
    private UnitManager() {
        blueUnits = new ArrayList<>();
        redUnits = new ArrayList<>();
    }
    
    public void addUnit(Unit u){
        (u.getTeam() ? blueUnits : redUnits).add(u);
    }
    
    public List<Unit> getUnits(boolean team){
        return team ? blueUnits : redUnits;
    }
    
    public void removeUnit(Unit u){
        (u.getTeam() ? blueUnits : redUnits).remove(u);
    }
    
    public void resetUnits(){
        for(Unit u : blueUnits){
            u.reset();
        }
        for(Unit u : redUnits){
            u.reset();
        }
    }
    
    public static UnitManager getInstance() {
        return INSTANCE;
    }
    
    
}
