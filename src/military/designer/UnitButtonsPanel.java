package military.designer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import military.gui.ModelManager;

/**
 *
 * @author Nate
 */
public class UnitButtonsPanel extends JPanel {

    private ArrayList<JButton> buttons;
    private String unitName;
    private boolean selected;

    public UnitButtonsPanel() {
        this.setBackground(Color.BLACK);
        buttons = new ArrayList<>();
        GridLayout grid = new GridLayout(0,2);
        grid.setHgap(5);
        grid.setVgap(5);
        this.setLayout(grid);
        InputStream unitStream = null;
        try {
           unitStream = new FileInputStream("Resources//Units.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
//        InputStream unitStream = getClass().getClassLoader().getResourceAsStream("Units.txt");
        Scanner unitReader = new Scanner(unitStream);
        
        while (!unitReader.next().equals("Shift")) {
        }
        while (unitReader.hasNext()) {
            final JButton butt = new JButton();
            butt.setName(unitReader.next());
            butt.setIcon(new ImageIcon(ModelManager.getModel(butt.getName()).getImage(true).getScaledInstance( 20, 20,  java.awt.Image.SCALE_SMOOTH )));
            butt.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    unitName = butt.getName();
                    selected = true;
                    
                }
            });
            butt.setMargin(new Insets(0, 0, 0, 0));
            butt.setMinimumSize(new Dimension(32, 32));
            butt.setBackground(Color.darkGray);
            buttons.add(butt);
            this.add(butt);
            unitReader.nextLine();
        }
        unitReader.close();
    }

    public void changeTeam(boolean team){
        for(JButton butt: buttons){
            butt.setIcon(new ImageIcon(ModelManager.getModel(butt.getName()).getImage(team).getScaledInstance( 32, 32,  java.awt.Image.SCALE_SMOOTH )));
        }
    }
    
    public String getUnitName() {
        return unitName;
    }
    
    public void deselect(){
        selected = false;
    }

    public boolean isSelected() {
        return selected;
    }
}
