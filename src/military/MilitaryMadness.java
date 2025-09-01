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
        boolean hasMaps = loadMapList();
        // Build choices dynamically based on map availability
        String[] choices = hasMaps ? new String[]{"Play Game", "Create Level", "Exit"}
                                   : new String[]{"Create Level", "Exit"};

        int n = -1;
        do {
            n = JOptionPane.showOptionDialog(null, "What Would you Like to Do?", null,
                    JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices, choices[choices.length - 1]);
            if (hasMaps && n == 0) {
                JOptionPane.showMessageDialog(
                        null,
                        scenarioComboBox,
                        "Choose a scenario to load:",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (levelName == null || levelName.isBlank()) {
                    JOptionPane.showMessageDialog(null, "Please select a valid map to play.");
                    continue;
                }
                new Thread(SoundUtility.getInstance()).start();
                try {
                    Game game = new Game(levelName);
                    game.run();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Failed to start the game: " + e.getMessage());
                }
            } else if ((hasMaps && n == 1) || (!hasMaps && n == 0)) {
                String[] choices2 = {"New Level", "Old Level"};
                int m = JOptionPane.showOptionDialog(null, "What Would you Like to Do?", null,
                        JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices2, choices2[0]);
                if (m == 0) {
                    int width = 0;
                    int height = 0;
                    try {
                        String w = JOptionPane.showInputDialog("Width?");
                        width = Integer.parseInt(w);
                        String h = JOptionPane.showInputDialog("Height?");
                        height = Integer.parseInt(h);
                    } catch (NumberFormatException numberFormatException) {
                        JOptionPane.showMessageDialog(null, "Invalid Number");
                        continue;
                    }
                    DesignGUI dgui = new DesignGUI(width, height);
                    // Removed busy-wait; the designer window manages its own lifecycle
                } else if (m == 1) {
                    String levelToLoad = JOptionPane.showInputDialog("What level would you like to load?");
                    if (levelToLoad != null && !levelToLoad.isBlank()) {
                        DesignGUI dgui = new DesignGUI(levelToLoad);
                        // Removed busy-wait
                    }
                }
            }
        } while (!((hasMaps && n == 2) || (!hasMaps && n == 1)));

        // Graceful shutdown without System.exit
        SoundUtility.getInstance().shutdown();
    }

    static boolean loadMapList() {
        scenarioComboBox = new JComboBox<>();
        List<Path> fileList;
        try {
            Path mapsPath = Path.of("Maps");
            if (!Files.isDirectory(mapsPath)) {
                JOptionPane.showMessageDialog(null, "Maps folder is missing. You can still create a new level.");
                return false;
            }
            fileList = listFiles(mapsPath);
        } catch (IOException io) {
            JOptionPane.showMessageDialog(null, "Unable to read Maps folder: " + io.getMessage());
            return false;
        }
        if (fileList == null || fileList.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No maps found in Maps folder. You can create a new level.");
            return false;
        }
        for (Path path : fileList) {
            String ln = path.toFile().getName();
            ln = ln.replace(".txt", "");
            scenarioComboBox.addItem(ln);
        }
        if (scenarioComboBox.getItemCount() > 0) {
            scenarioComboBox.setSelectedIndex(0);
            levelName = (String) scenarioComboBox.getItemAt(0);
        }
        scenarioComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                levelName = (String) scenarioComboBox.getSelectedItem();
            }
        });
        return true;
    }

    public static void mapFileComboBox(List<Path> fileList) {
        JComboBox comboBox = new JComboBox(fileList.toArray());
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
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
            System.out.println(a[i].getName());
        }
        // recursively printing files from the directory
        // i + 1 means look for the next file
        printFileNames(a, i + 1, lvl);
    }

}
