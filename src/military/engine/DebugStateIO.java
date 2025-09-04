package military.engine;

import military.util.ResourceLoader;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Lightweight save/load of the current game state for debugging/regression tests.
 * Format (GSTATEv1):
 *  Line 1: GSTATEv1
 *  Line 2: <width>
 *  Line 3: <height>
 *  Next: width*height tile type integers (column-major like LocationManager.saveMap)
 *  Then unit lines: x y name team health exp
 */
public final class DebugStateIO {
    private DebugStateIO() {}

    private static final String HEADER = "GSTATEv1";

    public static void saveState(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(HEADER); writer.newLine();
            java.awt.Point size = LocationManager.getSize();
            writer.write(Integer.toString(size.x)); writer.newLine();
            writer.write(Integer.toString(size.y)); writer.newLine();
            for (int x = 0; x < size.x; x++) {
                for (int y = 0; y < size.y; y++) {
                    writer.write(LocationManager.getLoc(x, y).getType() + " ");
                }
                writer.newLine();
            }
            // Units: write both teams with health/exp
            for (Unit u : UnitManager.getInstance().getUnits(true)) {
                Point p = u.getLoc().getLoc();
                writer.write(p.x + " " + p.y + " " + u.getName() + " true " + u.getHealth() + " " + u.getExp());
                writer.newLine();
            }
            for (Unit u : UnitManager.getInstance().getUnits(false)) {
                Point p = u.getLoc().getLoc();
                writer.write(p.x + " " + p.y + " " + u.getName() + " false " + u.getHealth() + " " + u.getExp());
                writer.newLine();
            }
        }
    }

    public static void loadState(Path path) throws IOException {
        try (Scanner reader = new Scanner(Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
            String first = reader.hasNext() ? reader.next() : null;
            if (first == null || !HEADER.equals(first)) {
                throw new IllegalArgumentException("Unknown game state header: " + first);
            }
            int w = reader.nextInt();
            int h = reader.nextInt();
            // Build entries grid
            java.util.ArrayList<java.util.ArrayList<Location>> entries = new java.util.ArrayList<>();
            for (int x = 0; x < w; x++) {
                java.util.ArrayList<Location> col = new java.util.ArrayList<>();
                for (int y = 0; y < h; y++) {
                    if (!reader.hasNextInt()) {
                        throw new IllegalArgumentException("Unexpected end of tiles at (" + x + "," + y + ")");
                    }
                    int type = reader.nextInt();
                    Point p = new Point(x, y);
                    if (type == -1) {
                        col.add(new Location(p, -1));
                    } else if (type >= 0 && type <= 4) {
                        col.add(new Location(p, type * 10));
                    } else if (type == 5) {
                        col.add(new Base(p, true));
                    } else if (type == 6) {
                        col.add(new Base(p, false));
                    } else if (type == 7) {
                        col.add(new Factory(p));
                    } else if (type == 8) {
                        col.add(new Factory(p, true));
                    } else if (type == 9) {
                        col.add(new Factory(p, false));
                    } else {
                        throw new IllegalArgumentException("Invalid terrain code in state: " + type);
                    }
                }
                entries.add(col);
            }
            // Create the grid and adjacency
            LocationManager.create(entries);
            // Clear UnitManager lists
            UnitManager.getInstance().getUnits(true).clear();
            UnitManager.getInstance().getUnits(false).clear();

            // Read units until EOF
            while (reader.hasNext()) {
                int x = reader.nextInt();
                int y = reader.nextInt();
                String name = reader.next();
                boolean team = reader.nextBoolean();
                int health = reader.nextInt();
                int exp = reader.nextInt();
                // Create unit from Units.txt like LocationManager.addUnit
                Unit u = null;
                try (InputStream unitStream = ResourceLoader.openTextFromResources("Units.txt");
                     Scanner unitReader = new Scanner(new InputStreamReader(unitStream, StandardCharsets.UTF_8))) {
                    while (unitReader.hasNext()) {
                        String token = unitReader.next();
                        if (name.equals(token)) {
                            String type = unitReader.next();
                            boolean isRange = unitReader.nextBoolean();
                            boolean isAir = unitReader.nextBoolean();
                            int landAttack = unitReader.nextInt();
                            int airAttack = unitReader.nextInt();
                            int range = unitReader.nextInt();
                            int defense = unitReader.nextInt();
                            int shift = unitReader.nextInt();
                            u = new Unit(name, type, isRange, isAir, team, landAttack, airAttack, range, defense, shift);
                            break;
                        }
                    }
                } catch (Exception ex) {
                    java.util.logging.Logger log = military.util.Logs.getLogger(DebugStateIO.class);
                    log.fine("Units.txt lookup failed for '" + name + "', using fallback: " + ex.toString());
                    // fall through to fallback
                }
                if (u == null) {
                    // Fallback with minimal stats if Units.txt format/name is unknown
                    u = new Unit(name, name, false, false, team, 0, 0, 1, 0, 0);
                }
                LocationManager.getLoc(x, y).addUnit(u);
                // Apply saved mutable state
                u.setHealth(health);
                u.addExp(exp); // addExp caps at 8 and accumulates
                UnitManager.getInstance().addUnit(u);
            }
        }
    }
}
