package military;

import javax.swing.JOptionPane;
import military.designer.DesignGUI;
import military.gui.SoundUtility;

/**
 *
 * @author Nate
 */
public class MilitaryMadness {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String choices[] = {"Play Game", "Create Level", "Exit"};
        int n = 0;
        while (n != 2) {
            n = JOptionPane.showOptionDialog(null, "What Would you Like to Do?", null,
                    JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices, 2);
            if (n == 0) {
                String levelName = JOptionPane.showInputDialog("Which level would you like to load?");
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

}
