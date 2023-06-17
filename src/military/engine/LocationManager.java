/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package military.engine;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nate
 */
public class LocationManager {

    private static LocationManager instance;
    private static ArrayList<ArrayList<Location>> entries;
    private static Location blueBase;
    private static Location redBase;

    public static Point getSize() {
        return new Point(entries.size(), entries.get(0).size());
    }

    public static Location getLoc(int x, int y) {
        return entries.get(x).get(y);
    }

    public static Location getLoc(Point p) {
        return entries.get(p.x).get(p.y);
    }

    public static void create(ArrayList<ArrayList<Location>> locs) {
        entries = locs;
        calcAjacent();
    }

    public static void generateMap(int w, int h) {
        entries = new ArrayList<>();
        for (int i = 0; i < w; i++) {
            ArrayList<Location> column = new ArrayList<>();
            for (int j = 0; j < h; j++) {
                column.add(new Location(new Point(i, j), 10));
            }
            entries.add(column);
        }
        blueBase = null;
        redBase = null;
        calcAjacent();
        int x = (w*h)/35;
        Random rand = new Random();
        
        
        for (int i = 0; i < x; i++) {
            Point p = new Point(rand.nextInt(w), rand.nextInt(h));
            for (int j = 0; j < 20; j++) {
                getLoc(p).setTerrain(30);
                p = getLoc(p).getAjacent()[rand.nextInt(getLoc(p).getAjacent().length)].getLoc();
            }
        }
        
        for (int i = 0; i < x-1; i++) {
            Point p = new Point(rand.nextInt(w), rand.nextInt(h));
            for (int j = 0; j < 15; j++) {
                getLoc(p).setTerrain(20);
                p = getLoc(p).getAjacent()[rand.nextInt(getLoc(p).getAjacent().length)].getLoc();
            }
        }
        
        for (int i = 0; i < x-2; i++) {
            Point p = new Point(rand.nextInt(w), rand.nextInt(h));
            for (int j = 0; j < 20; j++) {
                getLoc(p).setTerrain(40);
                p = getLoc(p).getAjacent()[rand.nextInt(getLoc(p).getAjacent().length)].getLoc();
            }
        }
        
        Point p = new Point(rand.nextInt(w/2), rand.nextInt(h));
        newLoc(p, 5);
        p = new Point(rand.nextInt(w/2)+(w/2)-1, rand.nextInt(h));
        newLoc(p, 6);
    }

    public static boolean isCaptured(boolean team) {
        if (team) {
            if (!blueBase.isEmpty()) {
                return !blueBase.getUnit().getTeam();
            }
        } else {
            if (!redBase.isEmpty()) {
                return redBase.getUnit().getTeam();
            }
        }
        return false;
    }

    public static void loadMap(String filename) {
        if (instance == null) {
            instance = new LocationManager();
        }
        entries = new ArrayList<>();
        InputStream inStream = null;
        try {
            inStream = new FileInputStream("Maps//" + filename + ".txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Scanner reader = new Scanner(inStream);
        int numColumns = reader.nextInt();
        int numRows = reader.nextInt();

        for (int x = 0; x < numColumns; x++) {
            ArrayList<Location> column = new ArrayList<>();
            for (int y = 0; y < numRows; y++) {
                int terrain = reader.nextInt();
                if (terrain == -1) {
                    column.add(new Location(new Point(x, y), -1));
                } else if (terrain >= 0 && terrain <= 4) {
                    column.add(new Location(new Point(x, y), terrain * 10));
                } else if (terrain == 5) {
                    column.add(new Base(new Point(x, y), true));
                    blueBase = column.get(y);
                } else if (terrain == 6) {
                    column.add(new Base(new Point(x, y), false));
                    redBase = column.get(y);
                } else if (terrain == 7) {
                    column.add(new Factory(new Point(x, y)));
                } else if (terrain == 8) {
                    column.add(new Factory(new Point(x, y), true));
                } else if (terrain == 9) {
                    column.add(new Factory(new Point(x, y), false));
                }
            }
            entries.add(column);
        }

        while (reader.hasNext()) {
            int x = reader.nextInt();
            int y = reader.nextInt();
            String name = reader.next();
            boolean team = reader.nextBoolean();
//            InputStream unitStream = instance.getClass().getClassLoader().getResourceAsStream("Units.txt");
            InputStream unitStream = null;
            try{
                unitStream = new FileInputStream("Resources//Units.txt");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            Scanner unitReader = new Scanner(unitStream);
            while (!unitReader.next().equals(name)) {
            }
            Unit u = new Unit(name, unitReader.next(), unitReader.nextBoolean(),
                    unitReader.nextBoolean(), team, unitReader.nextInt(),
                    unitReader.nextInt(), unitReader.nextInt(), unitReader.nextInt(), unitReader.nextInt());
            entries.get(x).get(y).addUnit(u);
            UnitManager.getInstance().addUnit(u);
            unitReader.close();
        }

        calcAjacent();
    }

    public static void addUnit(Point p, String name, boolean team) {
        if (instance == null) {
            instance = new LocationManager();
        }
        int x = p.x;
        int y = p.y;
        InputStream unitStream = instance.getClass().getClassLoader().getResourceAsStream("Units.txt");
        assert unitStream != null;          // Added June 16, 2023
        Scanner unitReader = new Scanner(unitStream);
        while (!unitReader.next().equals(name)) {
        }
        Unit u = new Unit(name, unitReader.next(), unitReader.nextBoolean(),
                unitReader.nextBoolean(), team, unitReader.nextInt(),
                unitReader.nextInt(), unitReader.nextInt(), unitReader.nextInt(), unitReader.nextInt());
        entries.get(x).get(y).addUnit(u);
        UnitManager.getInstance().addUnit(u);
        unitReader.close();
    }

    public static void newLoc(Point p, int type) {
        if (blueBase != null && p.equals(blueBase.getLoc())) {
            blueBase = null;
        }
        if (redBase != null && p.equals(redBase.getLoc())) {
            redBase = null;
        }
        if (type == -1) {
            entries.get(p.x).set(p.y, new Location(p, -1));
        } else if (type >= 0 && type <= 4) {
            entries.get(p.x).set(p.y, new Location(p, type * 10));
        } else if (type == 5) {
            entries.get(p.x).set(p.y, new Base(p, true));
            if (blueBase != null) {
                entries.get(blueBase.getLoc().x).set(blueBase.getLoc().y, new Location(p, -1));
            }
            blueBase = entries.get(p.x).get(p.y);
        } else if (type == 6) {
            entries.get(p.x).set(p.y, new Base(p, false));
            if (redBase != null) {
                entries.get(redBase.getLoc().x).set(redBase.getLoc().y, new Location(p, -1));
            }
            redBase = entries.get(p.x).get(p.y);
        } else if (type == 7) {
            entries.get(p.x).set(p.y, new Factory(p));
        } else if (type == 8) {
            entries.get(p.x).set(p.y, new Factory(p, true));
        } else if (type == 9) {
            entries.get(p.x).set(p.y, new Factory(p, false));
        }
    }

    public static void saveMap(String filename) {
        if (instance == null) {
            instance = new LocationManager();
        }
        BufferedWriter writer = null;
        try {
            File mapFile = new File("Maps\\" + filename + ".txt");

            writer = new BufferedWriter(new FileWriter(mapFile));
            writer.write(entries.size() + "");
            writer.newLine();
            writer.write(entries.get(0).size() + "");
            writer.newLine();
            for (int i = 0; i < entries.size(); i++) {
                for (int j = 0; j < entries.get(0).size(); j++) {
                    writer.write(entries.get(i).get(j).getType() + " ");
                }
                writer.newLine();
            }

            for (Unit u : UnitManager.getInstance().getUnits(true)) {
                writer.write(u.getLoc().getLoc().x + " " + u.getLoc().getLoc().y
                        + " " + u.getName() + " true");
                writer.newLine();
            }
            for (Unit u : UnitManager.getInstance().getUnits(false)) {
                writer.write(u.getLoc().getLoc().x + " " + u.getLoc().getLoc().y
                        + " " + u.getName() + " false");
                writer.newLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    public static Location getBase(boolean team) {
        return team ? blueBase : redBase;
    }

    private static void calcAjacent() {
        int numColumns = entries.size();
        int numRows = entries.get(0).size();
        for (int x = 0; x < numColumns; x++) {
            for (int y = 0; y < numRows; y++) {
                if (x == 0) {
                    if (y == 0) {
                        Location[] ajacent = {entries.get(x).get(y + 1), entries.get(x + 1).get(y)};
                        entries.get(x).get(y).setAjacent(ajacent);
                    } else if (y == numRows - 1) {
                        Location[] ajacent = {entries.get(x).get(y - 1), entries.get(x + 1).get(y)};
                        entries.get(x).get(y).setAjacent(ajacent);
                    } else {
                        Location[] ajacent = {entries.get(x).get(y + 1), entries.get(x + 1).get(y + 1),
                            entries.get(x + 1).get(y), entries.get(x).get(y - 1)};
                        entries.get(x).get(y).setAjacent(ajacent);
                    }
                } else if (x == numColumns - 1) {
                    if (y == 0) {
                        Location[] ajacent = {entries.get(x).get(y + 1), entries.get(x - 1).get(y)};
                        entries.get(x).get(y).setAjacent(ajacent);
                    } else if (y == numRows - 1) {
                        Location[] ajacent = {entries.get(x).get(y - 1), entries.get(x - 1).get(y)};
                        entries.get(x).get(y).setAjacent(ajacent);
                    } else {
                        Location[] ajacent = {entries.get(x).get(y + 1), entries.get(x - 1).get(y + 1),
                            entries.get(x - 1).get(y), entries.get(x).get(y - 1)};
                        entries.get(x).get(y).setAjacent(ajacent);
                    }
                } else if (y == 0) {
                    if (x % 2 == 0) {
                        Location[] ajacent = {entries.get(x - 1).get(y), entries.get(x).get(y),
                            entries.get(x + 1).get(y)};
                        entries.get(x).get(y).setAjacent(ajacent);
                    } else {
                        Location[] ajacent = {entries.get(x - 1).get(y + 1), entries.get(x - 1).get(y), entries.get(x).get(y + 1),
                            entries.get(x + 1).get(y), entries.get(x + 1).get(y + 1)};
                        entries.get(x).get(y).setAjacent(ajacent);
                    }
                } else if (y == numRows - 1) {
                    if (x % 2 == 0) {
                        Location[] ajacent = {entries.get(x - 1).get(y - 1), entries.get(x - 1).get(y), entries.get(x).get(y - 1),
                            entries.get(x + 1).get(y), entries.get(x + 1).get(y - 1)};
                        entries.get(x).get(y).setAjacent(ajacent);
                    } else {
                        Location[] ajacent = {entries.get(x - 1).get(y), entries.get(x).get(y),
                            entries.get(x + 1).get(y)};
                        entries.get(x).get(y).setAjacent(ajacent);
                    }
                } else {
                    if (x % 2 == 0) {
                        Location[] ajacent = {entries.get(x - 1).get(y - 1), entries.get(x - 1).get(y), entries.get(x).get(y - 1),
                            entries.get(x).get(y + 1), entries.get(x + 1).get(y - 1), entries.get(x + 1).get(y)};
                        entries.get(x).get(y).setAjacent(ajacent);
                    } else {
                        Location[] ajacent = {entries.get(x - 1).get(y + 1), entries.get(x - 1).get(y), entries.get(x).get(y - 1),
                            entries.get(x).get(y + 1), entries.get(x + 1).get(y + 1), entries.get(x + 1).get(y)};
                        entries.get(x).get(y).setAjacent(ajacent);
                    }
                }
            }
        }
    }
}
