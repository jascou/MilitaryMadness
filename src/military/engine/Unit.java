/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package military.engine;

import java.awt.Point;

/**
 *
 * @author Nate
 */
public class Unit{

    
    private final String type;
    private final String name;
    private final boolean ranged;
    private final boolean air;
    private final int range;
    private boolean team;
    private final int landAttack;
    private final int airAttack;
    private final int defense;
    private final int shift;
    private boolean shiftDone;
    private boolean attackDone;
    private int exp;
    private int health;
    private Location loc;

    public Unit(String name, String type, boolean isRange, boolean isAir, boolean team, int landAttack, int airAttack, int range, int defense, int shift) {
        this.name = name;
        this.type = type;
        this.ranged = isRange;
        this.air = isAir;
        this.team = team;
        this.landAttack = landAttack;
        this.airAttack = airAttack;
        this.range = range;
        this.defense = defense;
        this.shift = shift;
        shiftDone = false;
        attackDone = false;
        health = 8;
    }

    public void setTeam(boolean team) {
        this.team = team;
    }
    
    public void move(Location newLoc){
        this.loc.removeUnit();
        newLoc.addUnit(this);
        this.loc = newLoc;
        shiftDone = true;
    }
    
    public void attack(){
        attackDone = true;
        shiftDone = true;
    }
    
    public void reset(){
        shiftDone = false;
        attackDone = false;
    }

    public boolean isShiftDone() {
        return shiftDone;
    }

    public boolean isAttackDone() {
        return attackDone;
    }
    
    public int getShift() {
        return shift;
    }

    public String getType() {
        return type;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public int getRange() {
        return range;
    }

    public void addExp(int e) {
        this.exp += e;
        if (exp > 8) {
            exp = 8;
        }
    }

    public void setHealth(int he) {
        this.health = he;
        if (health < 0) {
            health = 0;
        }
    }

    public String getName() {
        return name;
    }

    public boolean isRanged() {
        return ranged;
    }

    public boolean isAir() {
        return air;
    }

    public int getExp() {
        return exp;
    }

    public int getHealth() {
        return health;
    }

    public boolean getTeam() {
        return team;
    }

    public Location getLoc() {
        return loc;
    }

    public int getLandAttack() {
        return landAttack;
    }

    public int getAirAttack() {
        return airAttack;
    }

    public int getDefense() {
        return defense;
    }

    public String getModelName() {
        return name;
    }

    public Point getRenderLoc() {
        return loc.getLoc();
    }

    @Override
    public String toString() {
        return "\n+=========================+" +
                "\ntype=" + type +
                "\nname=" + name +
                "\nteam=" + team +
                "\nlandAttack=" + landAttack +
                "\nairAttack=" + airAttack +
                "\ndefense=" + defense +
                "\nexp=" + exp +
                "\nhealth=" + health + 
                "\n+========================+";
    }
}
