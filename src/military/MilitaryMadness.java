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
    static List<String> levels = new ArrayList();
    static JComboBox scenarioComboBox;
    static String levelName;
    InputStream levelInputStream = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        loadMapList();
        //findByFileExtensions("Maps", "txt");
        String choices[] = {"Play Game", "Create Level", "Exit"};

        int n = 0;
        while (n != 2) {
            n = JOptionPane.showOptionDialog(null, "What Would you Like to Do?", null,
                    JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices, 2);
            if (n == 0) {
                JOptionPane.showMessageDialog(
                        null,
                        scenarioComboBox,
                        "Choose a scenario to load:",
                        JOptionPane.QUESTION_MESSAGE
                );
                // levelName = JOptionPane.showInputDialog("Which level would you like to load?");
                new Thread(SoundUtility.getInstance()).start();
                Game game = null;
                try {
                    game = new Game(levelName);
                } catch (NullPointerException e) {
                    JOptionPane.showMessageDialog(null, "Invalid File Name");
                    continue;
                }
                game.run();
            } else if (n == 1) {
                String choices2[] = {"New Level", "Old Level"};
                int m = JOptionPane.showOptionDialog(null, "What Would you Like to Do?", null,
                        JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices2, "Play Game");
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
                    while (dgui.isVisible()) {
                    }

                } else {
                    String levelName = JOptionPane.showInputDialog("What level would you like to load?");
                    DesignGUI dgui = new DesignGUI(levelName);
                    while (dgui.isVisible()) {
                    }
                }
            }
        }
        System.exit(0);
    }

    static void loadMapList() throws IOException {
        scenarioComboBox = new JComboBox();
        List<Path> fileList = listFiles(Path.of("Maps"));
        fileList.forEach(System.out::println);
        for (Path path : fileList) {
            String levelName = path.toFile().getName();
            levelName = levelName.replace(".txt", "");
            scenarioComboBox.addItem(levelName);
        }
        scenarioComboBox.setSelectedIndex(0);

        scenarioComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(e);
                levelName = (String) scenarioComboBox.getSelectedItem();
            }
        });
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
