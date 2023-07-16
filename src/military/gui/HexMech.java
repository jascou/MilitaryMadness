package military.gui;

import java.awt.*;
import java.util.logging.Logger;

import military.engine.Base;
import military.engine.Factory;
import military.engine.LocationManager;
import military.engine.Unit;

/**
 * Code taken and editied from http://www.quarkphysics.ca/scripsi/hexgrid/
 */
public class HexMech {

    private static int s = 35;	// length of one side
    private static int t = 18;	// short side of 30o triangle outside of each hex
    private static int r = 25;	// radius of inscribed circle (centre to middle of each side). r= h/2
    private static int h = 50;	// height. Distance between centres of two adjacent hexes. Distance between two opposite sides in a hex.

    private static final int h0 = 46;
    private static final int r0 = 22;
    private static final int t0 = 16;
    private static final int s0 = 31;

    private static Point corner = new Point (0,0);

    public static void setCorner(Point p) {
        corner = p;
    }
    
    public static void makeSmall(){
        s = 24;
        t = 12;
        r = 18;
        h = 35;
    }
    
    /**
     * *******************************************************
     * Name: hex() Parameters: (x0,y0) This point is normally the top left
     * corner of the rectangle enclosing the hexagon. However, if XYVertex is
     * true then (x0,y0) is the vertex of the top left corner of the hexagon.
     * Returns: a polygon containing the six points. Called from: drawHex(),
     * fillhex() Purpose: This function takes two points that describe a hexagon
     * and calculates all six of the points in the hexagon.
     * *******************************************************
     */
    public static Polygon hex(int i, int j) {
        int x = (i-corner.x) * (s + t);
        int y = (j-corner.y) * h + (i % 2) * h / 2;
        int[] cx, cy;
        cx = new int[]{x + t, x + t + s, x + t + s + t, x + t + s, x + t, x};
        cy = new int[]{y, y, y + r, y + r + r, y + r + r, y + r};
        return new Polygon(cx, cy, 6);
    }

    public static Polygon smallHex(int i, int j) {
        int x = 4+(i-corner.x) * (s + t);
        int y = 3+(j-corner.y) * h + (i % 2) * h / 2;
        int[] cx, cy;
        cx = new int[]{x + t0, x + t0 + s0, x + t0 + s0 + t0, x + t0 + s0, x + t0, x};
        cy = new int[]{y, y, y + r0, y + r0 + r0, y + r0 + r0, y + r0};
        return new Polygon(cx, cy, 6);
    }

    /**
     * ******************************************************************
     * Name: drawHex() Parameters: (i,j) : the x,y coordinates of the inital
     * point of the hexagon g2: the Graphics2D object to draw on. Returns: void
     * Calls: hex() Purpose: This function draws a hexagon based on the initial
     * point (x,y). The hexagon is drawn in the colour specified in
     * hexgame.COLOURELL.
     * *******************************************************************
     */
    public static void drawHex(int i, int j, Graphics2D g2) {
        try {
            g2.setStroke(new BasicStroke(2));
            int x = (i - corner.x) * (s + t);
            int y = (j - corner.y) * h + (i % 2) * h / 2;
            //System.out.println("drawHex(), i:" + i + ", j:" + j);
            Polygon poly = hex((i), (j));

            if (x > 0 && y > 0) {
                if (LocationManager.getSize().x > i && LocationManager.getSize().y > j) {
//            if (LocationManager.isInBounds(x, y)) {
                    g2.setColor(LocationManager.getLoc(i, j).getColor());
                    g2.fillPolygon(poly);
                    g2.setColor(Color.black);
                    g2.drawPolygon(poly);
                }
            }

            if (LocationManager.getSize().x > i && LocationManager.getSize().y > j) {
                if (LocationManager.getLoc(i, j) instanceof Factory) {
                    g2.drawImage(ModelManager.getModel("Factory").getImage(),
                            x + (s + t + t - 32) / 2 - 5, y + (h - 32) / 2, null);
                }
            }

            if (LocationManager.getSize().x > i && LocationManager.getSize().y > j) {
                if (LocationManager.getLoc(i, j) instanceof Base) {
                    boolean team = ((Base) LocationManager.getLoc(i, j)).getTeam();
                    g2.drawImage(ModelManager.getModel((team ? "BlueBase" : "RedBase")).getImage(),
                            x + (s + t + t - 32) / 2 - 5, y + (h - 32) / 2, null);
                }
            }
            if (LocationManager.getSize().x > i && LocationManager.getSize().y > j) {
                if (!LocationManager.getLoc(i, j).isEmpty()) {
                    Unit u = LocationManager.getLoc(i, j).getUnit();
                    if (u.isAttackDone() && u.isShiftDone()) {
                        g2.drawImage(ModelManager.getModel(u.getName()).getGreyImage(u.getTeam()),
                                x + (s + t + t - 32) / 2, y + (h - 32) / 2, null);
                    } else {
                        g2.drawImage(ModelManager.getModel(u.getName()).getImage(u.getTeam()),
                                x + (s + t + t - 32) / 2, y + (h - 32) / 2, null);
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public static void selectHex(int i, int j, Graphics2D g2) {
        Polygon poly = smallHex(i, j);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(4));
        g2.drawPolygon(poly);
        g2.setStroke(new BasicStroke(2));
    }

    public static void cursor(int i, int j, Graphics2D g2) {
        cursor(i, j, true, g2);
    }

    public static void cursor(int i, int j, boolean turn, Graphics2D g2) {
        Polygon poly = smallHex(i, j);
        g2.setColor(turn ? Color.BLUE : Color.RED);
        g2.setStroke(new BasicStroke(4));
        g2.drawPolygon(poly);
        g2.setStroke(new BasicStroke(2));
    }

    public static Point pxtoHex(int mx, int my) {
        Point p = new Point(-1, -1);
        int x = (int) (mx / (s + t)); //this gives a quick value for x. It works only on odd cols and doesn't handle the triangle sections. It assumes that the hexagon is a rectangle with width s+t (=1.5*s).
        int y = (int) ((my - (x % 2) * r) / h); //this gives the row easily. It needs to be offset by h/2 (=r)if it is in an even column

        /**
         * ****FIX for clicking in the triangle spaces (on the left side only)******
         */
        //dx,dy are the number of pixels from the hex boundary. (ie. relative to the hex clicked in)
        int dx = mx - x * (s + t);
        int dy = my - y * h;

        if (my - (x % 2) * r < 0) {
            return p; // prevent clicking in the open halfhexes at the top of the screen
        }
		//System.out.println("dx=" + dx + " dy=" + dy + "  > " + dx*r/t + " <");

        //even columns
        if (x % 2 == 0) {
            if (dy > r) {	//bottom half of hexes
                if (dx * r / t < dy - r) {
                    x--;
                }
            }
            if (dy < r) {	//top half of hexes
                if ((t - dx) * r / t > dy) {
                    x--;
                    y--;
                }
            }
        } else {  // odd columns
            if (dy > h) {	//bottom half of hexes
                if (dx * r / t < dy - h) {
                    x--;
                    y++;
                }
            }
            if (dy < h) {	//top half of hexes
                //System.out.println("" + (t- dx)*r/t +  " " + (dy - r));
                if ((t - dx) * r / t > dy - r) {
                    x--;
                }
            }
        }
        p.x = x+corner.x;
        p.y = y+corner.y;
        return p;
    }

}
