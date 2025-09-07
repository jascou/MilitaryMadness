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

/**
 * Manages the map grid (locations), base/factory placements, and unit placement.
 *
 * Thread-safety: The engine is designed for single-threaded access for mutation (the game/engine thread).
 * Read access from the EDT occurs for rendering needs (e.g., size queries). To reduce race risks, common
 * read methods like getSize(), getLoc(), and isInBounds() are synchronized. All other mutation methods
 * should be called only from the engine thread. Avoid calling mutating methods from the EDT.
 * IO: loadMap/saveMap perform filesystem access via Config paths; callers should ensure valid filenames.
 *
 * @author Nate
 */
public class LocationManager {

    private static LocationManager instance;
    private static ArrayList<ArrayList<Location>> entries;
    private static Location blueBase;
    private static Location redBase;
    private static final java.util.logging.Logger LOGGER = military.util.Logs.getLogger(LocationManager.class);
    private static final Random RNG = new Random();

    public static synchronized Point getSize() {
        return new Point(entries.size(), entries.get(0).size());
    }

    public static synchronized Location getLoc(int x, int y) {
//        System.out.println("getLoc(" + x + ", " + y +")");
        if (!isInBounds(x, y)) {
            java.util.logging.Logger logger = military.util.Logs.getLogger(LocationManager.class);
            logger.warning("getLoc out of bounds: (" + x + "," + y + ") size=" + (entries == null ? "null" : getSize()));
            throw new IllegalArgumentException("Location out of bounds: (" + x + "," + y + ")");
        }
        return entries.get(x).get(y);
    }

    public static synchronized boolean isInBounds(int x, int y) {
        if (entries == null || entries.isEmpty()) return false;
        if (x < 0 || y < 0) return false;
        int width = entries.size();
        int height = entries.get(0).size();
        return x < width && y < height;
    }

    public static Location getLoc(Point p) {
        return getLoc(p.x, p.y);
    }

    public static void create(ArrayList<ArrayList<Location>> locs) {
        entries = locs;
        calcAdjacent();
    }

    public static void setRandomSeed(long seed) {
        RNG.setSeed(seed);
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
        calcAdjacent();
        int x = (w*h)/35;
        Random rand = RNG;

        for (int i = 0; i < x; i++) {
            Point p = new Point(rand.nextInt(w), rand.nextInt(h));
            for (int j = 0; j < 20; j++) {
                getLoc(p).setTerrain(30);
                p = getLoc(p).getAdjacent()[rand.nextInt(getLoc(p).getAdjacent().length)].getLoc();
            }
        }
        
        for (int i = 0; i < x-1; i++) {
            Point p = new Point(rand.nextInt(w), rand.nextInt(h));
            for (int j = 0; j < 15; j++) {
                getLoc(p).setTerrain(20);
                p = getLoc(p).getAdjacent()[rand.nextInt(getLoc(p).getAdjacent().length)].getLoc();
            }
        }
        
        for (int i = 0; i < x-2; i++) {
            Point p = new Point(rand.nextInt(w), rand.nextInt(h));
            for (int j = 0; j < 20; j++) {
                getLoc(p).setTerrain(40);
                p = getLoc(p).getAdjacent()[rand.nextInt(getLoc(p).getAdjacent().length)].getLoc();
            }
        }
        
        Point p = new Point(rand.nextInt(w/2), rand.nextInt(h));
        newLoc(p, 5);
        p = new Point(rand.nextInt(w/2)+(w/2)-1, rand.nextInt(h));
        newLoc(p, 6);
    }

    /**
         * Returns true if the specified team's base has been captured.
         * @param team true for blue team, false for red team
         */
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

    /**
         * Loads a map from Maps/{filename}.txt and initializes locations and units.
         * @param filename map name without extension
         */
        public static void loadMap(String filename) {
        if (instance == null) {
            instance = new LocationManager();
        }
        entries = new ArrayList<>();
        java.nio.file.Path mapPath = military.Config.mapsDir().resolve(filename + ".txt");
        try (java.io.InputStream inStream = java.nio.file.Files.newInputStream(mapPath);
             java.util.Scanner reader = new java.util.Scanner(new java.io.InputStreamReader(inStream, java.nio.charset.StandardCharsets.UTF_8))) {
            // Support optional version header: MMAPv1
            int numColumns;
            int numRows;
            String firstToken = reader.hasNext() ? reader.next() : null;
            if (firstToken == null) {
                throw new IllegalArgumentException("Empty map file");
            }
            try {
                // If first token is an integer, it's the legacy format (dims first)
                numColumns = Integer.parseInt(firstToken);
                numRows = reader.nextInt();
            } catch (NumberFormatException nfe) {
                // Otherwise expect a version header token then dims
                if (!firstToken.startsWith("MMAPv")) {
                    LOGGER.severe("Unknown map header: " + firstToken);
                    throw new IllegalArgumentException("Unknown map header: " + firstToken);
                }
                numColumns = reader.nextInt();
                numRows = reader.nextInt();
            }

            if (numColumns <= 0 || numRows <= 0 || numColumns > 500 || numRows > 500) {
                LOGGER.severe("Invalid map dimensions: " + numColumns + "x" + numRows);
                throw new IllegalArgumentException("Invalid map dimensions");
            }

            for (int x = 0; x < numColumns; x++) {
                ArrayList<Location> column = new ArrayList<>();
                for (int y = 0; y < numRows; y++) {
                    if (!reader.hasNextInt()) {
                        LOGGER.severe("Unexpected end of tiles at (" + x + "," + y + ")");
                        throw new IllegalArgumentException("Unexpected end of tiles");
                    }
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
                    } else {
                        LOGGER.severe("Invalid terrain code " + terrain + " at (" + x + "," + y + ")");
                        throw new IllegalArgumentException("Invalid terrain code: " + terrain);
                    }
                }
                entries.add(column);
            }

            // Units section (optional)
            while (reader.hasNext()) {
                int x = reader.nextInt();
                int y = reader.nextInt();
                String name = reader.next();
                boolean team = reader.nextBoolean();
                if (!isInBounds(x, y)) {
                    LOGGER.severe("Unit position out of bounds: (" + x + "," + y + ")");
                    throw new IllegalArgumentException("Unit position out of bounds");
                }
                try (java.io.InputStream unitStream = military.util.ResourceLoader.openTextFromResources("Units.txt");
                     java.util.Scanner unitReader = new java.util.Scanner(new java.io.InputStreamReader(unitStream, java.nio.charset.StandardCharsets.UTF_8))) {
                    while (unitReader.hasNext() && !unitReader.next().equals(name)) {}
                    Unit u = new Unit(name, unitReader.next(), unitReader.nextBoolean(),
                            unitReader.nextBoolean(), team, unitReader.nextInt(),
                            unitReader.nextInt(), unitReader.nextInt(), unitReader.nextInt(), unitReader.nextInt());
                    entries.get(x).get(y).addUnit(u);
                    UnitManager.getInstance().addUnit(u);
                }
            }
        } catch (java.io.IOException ex) {
            LOGGER.severe("Failed to load map: " + mapPath + " - " + ex.getMessage());
            throw new RuntimeException(ex);
        }
        calcAdjacent();
    }

    /**
         * Adds a unit with the given name and team at the specified coordinate.
         */
        public static void addUnit(Point p, String name, boolean team) {
        if (instance == null) {
            instance = new LocationManager();
        }
        int x = p.x;
        int y = p.y;
//        InputStream unitStream = instance.getClass().getClassLoader().getResourceAsStream("Units.txt");
        // Use plugin registry (with fallback to Units.txt) to create the unit by name
        Unit u = UnitPluginRegistry.create(name, team);
        entries.get(x).get(y).addUnit(u);
        UnitManager.getInstance().addUnit(u);
    }

    /**
         * Changes the terrain or structure at the given coordinate to the specified type code.
         */
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

    /**
         * Saves the current map layout and units to Maps/{filename}.txt.
         * Writing is best-effort; caller is responsible for error handling.
         * Legacy format without a header to preserve round-trip equality with older maps.
         */
        public static void saveMap(String filename) {
        if (instance == null) {
            instance = new LocationManager();
        }
        java.nio.file.Path mapPath = military.Config.mapsDir().resolve(filename + ".txt");
        java.nio.file.Path tmpPath = mapPath.resolveSibling(filename + ".txt.tmp");
        try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(tmpPath, java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write(Integer.toString(entries.size()));
            writer.newLine();
            writer.write(Integer.toString(entries.get(0).size()));
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
            LOGGER.severe("Failed to write temp map file: " + tmpPath + " - " + e.getMessage());
            return;
        }
        try {
            java.nio.file.Files.move(tmpPath, mapPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception moveEx) {
            try {
                // Fallback without ATOMIC_MOVE if not supported
                java.nio.file.Files.move(tmpPath, mapPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ex2) {
                LOGGER.severe("Failed to replace map file: " + mapPath + " - " + ex2.getMessage());
            }
        }
    }

    /**
         * Saves using the versioned format with a header (e.g., MMAPv1). Useful for future evolution.
         */
        public static void saveMapV1(String filename) {
        if (instance == null) {
            instance = new LocationManager();
        }
        java.nio.file.Path mapPath = military.Config.mapsDir().resolve(filename + ".txt");
        java.nio.file.Path tmpPath = mapPath.resolveSibling(filename + ".txt.tmp");
        try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(tmpPath, java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("MMAPv1");
            writer.newLine();
            writer.write(Integer.toString(entries.size()));
            writer.newLine();
            writer.write(Integer.toString(entries.get(0).size()));
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
            LOGGER.severe("Failed to write temp map file (v1): " + tmpPath + " - " + e.getMessage());
            return;
        }
        try {
            java.nio.file.Files.move(tmpPath, mapPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception moveEx) {
            try {
                java.nio.file.Files.move(tmpPath, mapPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ex2) {
                LOGGER.severe("Failed to replace map file (v1): " + mapPath + " - " + ex2.getMessage());
            }
        }
    }

    /**
         * Returns the base location for the given team.
         * @param team true for blue, false for red
         */
        public static Location getBase(boolean team) {
        return team ? blueBase : redBase;
    }

    public static MapModel exportMapModel() {
        int w = entries.size();
        int h = entries.get(0).size();
        int[][] tiles = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                tiles[x][y] = entries.get(x).get(y).getType();
            }
        }
        java.util.List<MapModel.UnitEntry> units = new java.util.ArrayList<>();
        for (Unit u : UnitManager.getInstance().getUnits(true)) {
            units.add(new MapModel.UnitEntry(u.getLoc().getLoc().x, u.getLoc().getLoc().y, u.getName(), true));
        }
        for (Unit u : UnitManager.getInstance().getUnits(false)) {
            units.add(new MapModel.UnitEntry(u.getLoc().getLoc().x, u.getLoc().getLoc().y, u.getName(), false));
        }
        MapModel model = new MapModel(w, h, tiles, units);
        return model;
    }

    private static void calcAdjacent() {
        int numColumns = entries.size();
        int numRows = entries.get(0).size();
        for (int x = 0; x < numColumns; x++) {
            for (int y = 0; y < numRows; y++) {
                if (x == 0) {
                    if (y == 0) {
                        Location[] ajacent = {entries.get(x).get(y + 1), entries.get(x + 1).get(y)};
                        entries.get(x).get(y).setAdjacent(ajacent);
                    } else if (y == numRows - 1) {
                        Location[] ajacent = {entries.get(x).get(y - 1), entries.get(x + 1).get(y)};
                        entries.get(x).get(y).setAdjacent(ajacent);
                    } else {
                        Location[] ajacent = {entries.get(x).get(y + 1), entries.get(x + 1).get(y + 1),
                            entries.get(x + 1).get(y), entries.get(x).get(y - 1)};
                        entries.get(x).get(y).setAdjacent(ajacent);
                    }
                } else if (x == numColumns - 1) {
                    if (y == 0) {
                        Location[] ajacent = {entries.get(x).get(y + 1), entries.get(x - 1).get(y)};
                        entries.get(x).get(y).setAdjacent(ajacent);
                    } else if (y == numRows - 1) {
                        Location[] ajacent = {entries.get(x).get(y - 1), entries.get(x - 1).get(y)};
                        entries.get(x).get(y).setAdjacent(ajacent);
                    } else {
                        Location[] ajacent = {entries.get(x).get(y + 1), entries.get(x - 1).get(y + 1),
                            entries.get(x - 1).get(y), entries.get(x).get(y - 1)};
                        entries.get(x).get(y).setAdjacent(ajacent);
                    }
                } else if (y == 0) {
                    if (x % 2 == 0) {
                        Location[] ajacent = {entries.get(x - 1).get(y), entries.get(x).get(y),
                            entries.get(x + 1).get(y)};
                        entries.get(x).get(y).setAdjacent(ajacent);
                    } else {
                        Location[] ajacent = {entries.get(x - 1).get(y + 1), entries.get(x - 1).get(y), entries.get(x).get(y + 1),
                            entries.get(x + 1).get(y), entries.get(x + 1).get(y + 1)};
                        entries.get(x).get(y).setAdjacent(ajacent);
                    }
                } else if (y == numRows - 1) {
                    if (x % 2 == 0) {
                        Location[] ajacent = {entries.get(x - 1).get(y - 1), entries.get(x - 1).get(y), entries.get(x).get(y - 1),
                            entries.get(x + 1).get(y), entries.get(x + 1).get(y - 1)};
                        entries.get(x).get(y).setAdjacent(ajacent);
                    } else {
                        Location[] ajacent = {entries.get(x - 1).get(y), entries.get(x).get(y),
                            entries.get(x + 1).get(y)};
                        entries.get(x).get(y).setAdjacent(ajacent);
                    }
                } else {
                    if (x % 2 == 0) {
                        Location[] ajacent = {entries.get(x - 1).get(y - 1), entries.get(x - 1).get(y), entries.get(x).get(y - 1),
                            entries.get(x).get(y + 1), entries.get(x + 1).get(y - 1), entries.get(x + 1).get(y)};
                        entries.get(x).get(y).setAdjacent(ajacent);
                    } else {
                        Location[] ajacent = {entries.get(x - 1).get(y + 1), entries.get(x - 1).get(y), entries.get(x).get(y - 1),
                            entries.get(x).get(y + 1), entries.get(x + 1).get(y + 1), entries.get(x + 1).get(y)};
                        entries.get(x).get(y).setAdjacent(ajacent);
                    }
                }
            }
        }
    }
}
