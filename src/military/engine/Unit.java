/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package military.engine;

import java.awt.Point;

/**
 * Unit domain model.
 * Immutable core stats (type/name/attacks/defense/range/shift, flags for air/ranged) are final.
 * Mutable state is explicit and limited to team, experience, health, shiftDone/attackDone, and location.
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

    // New per-turn movement capability: allow certain units (e.g., Rabbit) to move twice and/or move after attack
    private int maxMovesPerTurn = 1; // default: one move per turn
    private int movesUsedThisTurn = 0;
    private boolean canMoveAfterAttack = false;
    // Tracks movement points (terrain cost) spent this turn to enforce "remaining shift points" rule
    private int movePointsSpentThisTurn = 0;

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
        // compute movement cost for entering newLoc (align with PathfindingService)
        int terrain = newLoc.getTerrain() / 10;
        if (terrain == 0 || this.isAir()) {
            terrain = 1;
        }
        // apply movement
        this.loc.removeUnit();
        newLoc.addUnit(this);
        this.loc = newLoc;
        // increment move counts and points; mark shiftDone based on caps
        movesUsedThisTurn++;
        movePointsSpentThisTurn += Math.max(0, terrain);
        // shiftDone if we've exhausted move points or hit the per-turn move action cap
        if (movePointsSpentThisTurn >= this.shift || movesUsedThisTurn >= maxMovesPerTurn) {
            shiftDone = true;
        }
    }
    
    public void attack(){
        attackDone = true;
        // If this unit can move after attacking, don't force shiftDone unless already out of moves
        if (!canMoveAfterAttack) {
            shiftDone = true;
        } else {
            shiftDone = (movesUsedThisTurn >= maxMovesPerTurn);
        }
    }
    
    public void reset(){
        shiftDone = false;
        attackDone = false;
        movesUsedThisTurn = 0;
        movePointsSpentThisTurn = 0;
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

    // Capability configuration and queries
    public void setMaxMovesPerTurn(int maxMovesPerTurn) { if (maxMovesPerTurn < 1) maxMovesPerTurn = 1; this.maxMovesPerTurn = maxMovesPerTurn; }
    public int getMaxMovesPerTurn() { return maxMovesPerTurn; }
    public int getMovesUsedThisTurn() { return movesUsedThisTurn; }
    public void setCanMoveAfterAttack(boolean value) { this.canMoveAfterAttack = value; }
    public boolean canMoveAfterAttack() { return canMoveAfterAttack; }
    public int getMovePointsSpentThisTurn() { return movePointsSpentThisTurn; }

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

    /**
     * Adapter to migrate from boolean team to Team enum.
     */
    public Team getTeamEnum() {
        return team ? Team.BLUE : Team.RED;
    }

    public void setTeam(Team t) {
        this.team = (t == Team.BLUE);
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
        StringBuilder sb = new StringBuilder(192);
        sb.append("\n+=========================+");
        sb.append("\ntype=").append(type);
        sb.append("\nname=").append(name);
        sb.append("\nteam=").append(team);
        sb.append("\nlandAttack=").append(landAttack);
        sb.append("\nairAttack=").append(airAttack);
        sb.append("\ndefense=").append(defense);
        sb.append("\nexp=").append(exp);
        sb.append("\nhealth=").append(health);
        sb.append("\n+========================+");
        return sb.toString();
    }
}
