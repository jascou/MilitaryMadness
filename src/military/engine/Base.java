package military.engine;

import java.awt.Color;
import java.awt.Point;

/**
 * This is the location that represents the base for each team. It is assigned
 * to one team and remains on the same team for the entire game.
 *
 * @author Nate
 */
public class Base extends Location {

    private boolean team;

    /**
     *
     * @param team The team assigned to the base. True is P1, False is P2
     */
    public Base(Point p, boolean team) {
        super(p, 35);
        this.team = team;
    }
    /**
     *
     * @return team The team assigned to this base. true is P1, false is P2.
     */
    public boolean getTeam() {
        return team;
    }
    
    public int getType(){
        return team ? 5 : 6;
    }
    
    public Color getColor(){
        return new Color(100, 150, 0);
    }
}
/**
 * Allons-y
 */
