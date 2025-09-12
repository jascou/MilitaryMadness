package military;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import military.designer.DesignGUI;
import military.gui.SoundUtility;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Nate
 */
public class MilitaryMadness {
    static List<String> levels = new ArrayList<String>();
    static JComboBox<String> scenarioComboBox;
    static String levelName;
    InputStream levelInputStream = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // Headless guard: avoid constructing Swing dialogs/frames when running in headless CI
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            java.util.logging.Logger logger = military.util.Logs.getLogger(MilitaryMadness.class);
            logger.info("Headless environment detected; skipping UI startup.");
            return;
        }
        // Initialize optional telemetry/log-to-file if opted-in
        try { military.util.Telemetry.initIfEnabled(); } catch (Exception ignore) {}
        // Command-line options:
        // --play <mapName>
        // --design <width> <height>
        // --design <mapName>
        if (args != null && args.length > 0) {
            try {
                if ("--play".equalsIgnoreCase(args[0]) && args.length >= 2) {
                    String map = args[1];
                    if (!military.util.Validator.isValidMapName(map)) {
                        showMessageEDT("Invalid map name: " + map);
                        return;
                    }
                    new Thread(SoundUtility.getInstance()).start();
                    // Persist last played map
                    try { military.util.PreferencesManager.setLastMapName(map); } catch (Exception ex) { /* best-effort */ }
                    final Game[] holder = new Game[1];
                    javax.swing.SwingUtilities.invokeAndWait(() -> holder[0] = new Game(map));
                    Thread gameThread = new Thread(new military.engine.GameLoop(holder[0]), "GameLoop");
                    gameThread.start();
                    // Wait for game to end (loop exits when base captured)
                    try { gameThread.join(); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    SoundUtility.getInstance().shutdown();
                    return;
                } else if ("--design".equalsIgnoreCase(args[0]) && args.length >= 2) {
                    if (args.length == 2) {
                        String map = args[1];
                        if (!military.util.Validator.isValidMapName(map)) {
                            showMessageEDT("Invalid map name: " + map);
                            return;
                        }
                        new DesignGUI(map);
                        return;
                    } else if (args.length >= 3) {
                        java.util.OptionalInt w = military.util.Validator.parsePositiveIntWithin(args[1], 1, 1000);
                        java.util.OptionalInt h = military.util.Validator.parsePositiveIntWithin(args[2], 1, 1000);
                        if (!w.isPresent() || !h.isPresent()) {
                            showMessageEDT("Invalid width/height for --design. Use integers between 1 and 1000.");
                            return;
                        }
                        new DesignGUI(w.getAsInt(), h.getAsInt());
                        return;
                    }
                }
            } catch (Exception ex) {
                java.util.logging.Logger logger = military.util.Logs.getLogger(MilitaryMadness.class);
                logger.severe("Command-line option failed: " + ex.getMessage());
                // fall through to UI
            }
        }

        boolean hasMaps = loadMapList();
        boolean hasSaves = hasSaveFiles();
        // Build choices dynamically based on availability
        java.util.List<String> choiceList = new java.util.ArrayList<>();
        if (hasMaps) choiceList.add("Play Game");
        if (hasSaves) choiceList.add("Load Game");
        choiceList.add("Create Level");
        choiceList.add("Exit");
        String[] choices = choiceList.toArray(new String[0]);

        int n = -1;
        do {
            n = showOptionDialogEDT("What Would you Like to Do?", choices, "Exit");
            if (n < 0) break;
            String selection = choices[n];
            if ("Play Game".equals(selection)) {
                // Use improved map selection dialog with metadata
                String chosen = military.gui.MapSelectionDialog.showDialog(null);
                if (chosen == null || chosen.trim().isEmpty()) {
                    showMessageEDT("Please select a valid map to play.");
                    continue;
                }
                levelName = chosen;
                try { military.util.PreferencesManager.setLastMapName(levelName); } catch (Exception ex) { /* best-effort */ }
                new Thread(SoundUtility.getInstance()).start();
                try {
                    final Game[] holder = new Game[1];
                    javax.swing.SwingUtilities.invokeAndWait(() -> holder[0] = new Game(levelName));
                    Thread gameThread = new Thread(new military.engine.GameLoop(holder[0]), "GameLoop");
                    gameThread.start();
                    gameThread.join();
                } catch (Exception e) {
                    showMessageEDT("Failed to start the game: " + e.getMessage());
                }
            } else if ("Load Game".equals(selection)) {
                String saveName = military.gui.SaveSelectionDialog.showDialog(null);
                if (saveName == null || saveName.trim().isEmpty()) {
                    continue;
                }
                saveName = saveName.trim();
                new Thread(SoundUtility.getInstance()).start();
                try {
                    // Load save (also loads the map)
                    military.engine.SaveGame data = military.engine.SaveLoadService.load(saveName);
                    final Game[] holder = new Game[1];
                    javax.swing.SwingUtilities.invokeAndWait(() -> holder[0] = new Game(data.getMapName()));
                    // Restore transient fields (turn, cursor)
                    holder[0].loadGame(saveName);
                    Thread gameThread = new Thread(new military.engine.GameLoop(holder[0]), "GameLoop");
                    gameThread.start();
                    gameThread.join();
                } catch (Exception e) {
                    showMessageEDT("Failed to load save: " + e.getMessage());
                }
            } else if ("Create Level".equals(selection)) {
                String[] choices2 = {"New Level", "Old Level"};
                int m = showOptionDialogEDT("What Would you Like to Do?", choices2, choices2[0]);
                if (m == 0) {
                    int width;
                    int height;
                    String w = showInputDialogEDT("Width? (1-100)");
                    String h = showInputDialogEDT("Height? (1-100)");
                    java.util.OptionalInt wv = military.util.Validator.parsePositiveIntWithin(w, 1, 100);
                    java.util.OptionalInt hv = military.util.Validator.parsePositiveIntWithin(h, 1, 100);
                    if (!wv.isPresent() || !hv.isPresent()) {
                        showMessageEDT("Invalid width or height. Please enter numbers between 1 and 100.");
                        continue;
                    }
                    width = wv.getAsInt();
                    height = hv.getAsInt();
                    DesignGUI dgui = new DesignGUI(width, height);
                } else if (m == 1) {
                    String levelToLoad = showInputDialogEDT("What level would you like to load?");
                    if (military.util.Validator.isValidMapName(levelToLoad)) {
                        DesignGUI dgui = new DesignGUI(levelToLoad);
                    } else if (levelToLoad != null) {
                        showMessageEDT("Invalid map name. Use letters, numbers, dash or underscore.");
                    }
                }
            }
        } while (!"Exit".equals(choices[n]));

        // Graceful shutdown without System.exit
        SoundUtility.getInstance().shutdown();
    }

    static boolean loadMapList() {
        scenarioComboBox = new JComboBox<>();
        List<Path> fileList;
        try {
            Path mapsPath = military.Config.mapsDir();
            if (!Files.isDirectory(mapsPath)) {
                showMessageEDT("Maps folder is missing. You can still create a new level.");
                return false;
            }
            fileList = listFiles(mapsPath);
        } catch (IOException io) {
            showMessageEDT("Unable to read Maps folder: " + io.getMessage());
            return false;
        }
        if (fileList == null || fileList.isEmpty()) {
            showMessageEDT("No maps found in Maps folder. You can create a new level.");
            return false;
        }
        for (Path path : fileList) {
            String ln = path.toFile().getName();
            ln = ln.replace(".txt", "");
            scenarioComboBox.addItem(ln);
        }
        if (scenarioComboBox.getItemCount() > 0) {
            // If a last map preference exists and is present in the list, select it
            String last = military.util.PreferencesManager.getLastMapName();
            boolean set = false;
            if (last != null) {
                for (int i = 0; i < scenarioComboBox.getItemCount(); i++) {
                    if (last.equals(scenarioComboBox.getItemAt(i))) {
                        scenarioComboBox.setSelectedIndex(i);
                        levelName = (String) scenarioComboBox.getItemAt(i);
                        set = true;
                        break;
                    }
                }
            }
            if (!set) {
                scenarioComboBox.setSelectedIndex(0);
                levelName = (String) scenarioComboBox.getItemAt(0);
            }
        }
        scenarioComboBox.addActionListener(e -> {
            levelName = (String) scenarioComboBox.getSelectedItem();
        });
        return true;
    }

    public static void mapFileComboBox(List<Path> fileList) {
        JComboBox comboBox = new JComboBox(fileList.toArray());
        comboBox.addActionListener(e -> { });
        BasicComboBoxRenderer renderer = new BasicComboBoxRenderer();
        renderer.setPreferredSize(new Dimension(200, 130));
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setVerticalAlignment(SwingConstants.CENTER);
        comboBox.setRenderer(renderer);
        comboBox.setMaximumRowCount(12);
    }

    public static List<Path> findByFileExtensions(Path path, String fileExtension) throws IOException {

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }

        List<Path> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(Files::isRegularFile) // is a file
                    .filter(p -> p.getFileName().toString().endsWith(fileExtension))
                    .collect(Collectors.toList());
        }
        return result;
    }

    public static List<Path> listFiles(Path path) throws IOException {

        List<Path> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk.filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }
        return result;
    }

    static void printFileNames(File[] a, int i, int lvl) {
        // base case of the recursion
        // i == a.length means the directory has
        // no more files. Hence, the recursion has to stop
        if (i == a.length) {
            return;
        }
        // checking if the encountered object is a file or not
        if (a[i].isFile()) {
            java.util.logging.Logger logger = military.util.Logs.getLogger(MilitaryMadness.class);
            logger.fine(a[i].getName());
        }
        // recursively printing files from the directory
        // i + 1 means look for the next file
        printFileNames(a, i + 1, lvl);
    }

    private static int showOptionDialogEDT(String message, Object[] options, Object initial) {
        final int[] result = new int[]{-1};
        try {
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                result[0] = javax.swing.JOptionPane.showOptionDialog(null, message, null,
                        javax.swing.JOptionPane.OK_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE, null, options, initial);
            });
        } catch (Exception e) {
            java.util.logging.Logger logger = military.util.Logs.getLogger(MilitaryMadness.class);
            logger.severe("Failed to show option dialog: " + e.getMessage());
        }
        return result[0];
    }

    private static void showMessageEDT(String message) {
        try {
            javax.swing.SwingUtilities.invokeAndWait(() -> javax.swing.JOptionPane.showMessageDialog(null, message));
        } catch (Exception e) {
            java.util.logging.Logger logger = military.util.Logs.getLogger(MilitaryMadness.class);
            logger.severe("Failed to show message dialog: " + e.getMessage());
        }
    }

    private static String showInputDialogEDT(String prompt) {
        final String[] result = new String[]{null};
        try {
            javax.swing.SwingUtilities.invokeAndWait(() -> result[0] = javax.swing.JOptionPane.showInputDialog(prompt));
        } catch (Exception e) {
            java.util.logging.Logger logger = military.util.Logs.getLogger(MilitaryMadness.class);
            logger.severe("Failed to show input dialog: " + e.getMessage());
        }
        return result[0];
    }

    private static void showComponentDialogEDT(String title, java.awt.Component component) {
        try {
            javax.swing.SwingUtilities.invokeAndWait(() -> javax.swing.JOptionPane.showMessageDialog(null, component, title, javax.swing.JOptionPane.QUESTION_MESSAGE));
        } catch (Exception e) {
            java.util.logging.Logger logger = military.util.Logs.getLogger(MilitaryMadness.class);
            logger.severe("Failed to show component dialog: " + e.getMessage());
        }
    }

    static boolean hasSaveFiles() {
        try {
            java.nio.file.Path dir = military.Config.savesDir();
            if (!java.nio.file.Files.isDirectory(dir)) return false;
            try (java.util.stream.Stream<java.nio.file.Path> s = java.nio.file.Files.list(dir)) {
                return s.anyMatch(p -> p.getFileName().toString().endsWith(".mmsave"));
            }
        } catch (Exception e) {
            return false;
        }
    }
}
