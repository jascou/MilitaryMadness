/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package military.designer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import military.engine.Location;
import military.engine.LocationManager;
import military.engine.UnitManager;
import military.gui.HexMech;
import military.gui.ModelManager;

/**
 *
 * @author Nate
 */
public class DesignGUI extends JFrame {

    private DesignPanel design;
    private JScrollPane scroll;
    private JPanel buttons;
    private JButton noTerrain;
    private JButton terrain0;
    private JButton terrain1;
    private JButton terrain2;
    private JButton terrain3;
    private JButton terrain4;
    private JButton blueBase;
    private JButton redBase;
    private JButton factory;
    private JButton teamFactory;
    private JRadioButton blueTeam;
    private JRadioButton redTeam;
    private UnitButtonsPanel unitButtons;
    private boolean team;
    private int type;
    private JButton save;
    private JButton random;

    public DesignGUI(String name){
        LocationManager.loadMap(name);
        initComponents();
        blueTeam.setSelected(true);
        team = true;
        design.grabFocus();
        design.render();
        this.setVisible(true);
    }
    
    public DesignGUI(int w, int h) {
        LocationManager.generateMap(w, h);
        initComponents();
        blueTeam.setSelected(true);
        HexMech.makeSmall();
        team = true;
        design.grabFocus();
        design.render();
        this.setVisible(true);
    }
    
    

    public void render() {
        if (!design.hasFocus()) {
            design.grabFocus();
        }
        design.render();
    }


    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        design = new DesignPanel();
        design.setPreferredSize(new Dimension(12 + LocationManager.getSize().x * 36, 18 + LocationManager.getSize().y * 35));
        design.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                Point p = HexMech.pxtoHex(evt.getX(), evt.getY());
                if (unitButtons.isSelected()) {
                    LocationManager.addUnit(p, unitButtons.getUnitName(), team);
                } else {
                    if(!LocationManager.getLoc(p).isEmpty()){
                        UnitManager.getInstance().removeUnit(LocationManager.getLoc(p).getUnit());
                    }
                    LocationManager.newLoc(p, type);
                }
                if(type == 5 || type == 6){
                    render();
                }
                else{
                    design.draw(p.x, p.y);
                }
            }
        });
        scroll = new JScrollPane(design);
        scroll.setPreferredSize(design.getPreferredSize());
        
        initButtons();
        layoutComponents();
        layoutButtons();
        this.setExtendedState(this.getExtendedState()|JFrame.MAXIMIZED_BOTH );
        pack();
    }

    private void initButtons() {
        buttons = new JPanel();
        buttons.setMinimumSize(new Dimension(70, 75 + design.getHeight()));
        buttons.setBackground(Color.black);

        noTerrain = new JButton();
        noTerrain.setText("Canyon");
        noTerrain.setMargin(new Insets(0, 0, 0, 0));
        noTerrain.setFont(new Font("Consolas", 0, 9));
        noTerrain.setForeground(Color.LIGHT_GRAY);
        noTerrain.setBackground(Color.black);
        noTerrain.setMinimumSize(new Dimension(40, 40));
        noTerrain.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                type = -1;
            }
        });

        terrain0 = new JButton();
        terrain0.setText("Road");
        terrain0.setMargin(new Insets(0, 0, 0, 0));
        terrain0.setFont(new Font("Consolas", 0, 9));
        terrain0.setForeground(Color.BLACK);
        terrain0.setBackground(Color.LIGHT_GRAY);
        terrain0.setMinimumSize(new Dimension(40, 40));
        terrain0.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                type = 0;
                unitButtons.deselect();
            }
        });

        terrain1 = new JButton();
        terrain1.setText("Grass");
        terrain1.setMargin(new Insets(0, 0, 0, 0));
        terrain1.setFont(new Font("Consolas", 0, 9));
        terrain1.setForeground(Color.BLACK);
        terrain1.setBackground(new Color(100, 150, 0));
        terrain1.setMinimumSize(new Dimension(40, 40));
        terrain1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                type = 1;
                unitButtons.deselect();
            }
        });

        terrain2 = new JButton();
        terrain2.setText("Hills");
        terrain2.setMargin(new Insets(0, 0, 0, 0));
        terrain2.setFont(new Font("Consolas", 0, 9));
        terrain2.setForeground(Color.BLACK);
        terrain2.setBackground(new Color(100, 100, 0));
        terrain2.setMinimumSize(new Dimension(40, 40));
        terrain2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                type = 2;
                unitButtons.deselect();
            }
        });

        terrain3 = new JButton();
        terrain3.setText("Forest");
        terrain3.setMargin(new Insets(0, 0, 0, 0));
        terrain3.setFont(new Font("Consolas", 0, 9));
        terrain3.setForeground(Color.BLACK);
        terrain3.setBackground(new Color(0, 125, 0));
        terrain3.setMinimumSize(new Dimension(40, 40));
        terrain3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                type = 3;
                unitButtons.deselect();
            }
        });

        terrain4 = new JButton();
        terrain4.setText("Mountain");
        terrain4.setMargin(new Insets(0, 0, 0, 0));
        terrain4.setFont(new Font("Consolas", 0, 8));
        terrain4.setForeground(Color.lightGray);
        terrain4.setBackground(new Color(0, 65, 0));
        terrain4.setMinimumSize(new Dimension(40, 40));
        terrain4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                type = 4;
                unitButtons.deselect();
            }
        });

        blueBase = new JButton();
        blueBase.setIcon(new ImageIcon(ModelManager.getModel("BlueBase").getImage().getScaledInstance( 32, 22,  java.awt.Image.SCALE_SMOOTH )));
        blueBase.setMargin(new Insets(0, 0, 0, 0));
        blueBase.setMaximumSize(new Dimension(40, 40));
        blueBase.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                type = 5;
                unitButtons.deselect();
            }
        });

        redBase = new JButton();
        redBase.setIcon(new ImageIcon(ModelManager.getModel("RedBase").getImage().getScaledInstance( 32, 22,  java.awt.Image.SCALE_SMOOTH )));
        redBase.setMargin(new Insets(0, 0, 0, 0));
        redBase.setMaximumSize(new Dimension(40, 40));
        redBase.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                type = 6;
                unitButtons.deselect();
            }
        });
        
        factory = new JButton();
        factory.setIcon(new ImageIcon(ModelManager.getModel("Factory").getImage().getScaledInstance( 32, 22,  java.awt.Image.SCALE_SMOOTH )));
        factory.setBackground(Color.LIGHT_GRAY);
        factory.setMargin(new Insets(0, 0, 0, 0));
        factory.setMaximumSize(new Dimension(40, 40));
        factory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                type = 7;
                unitButtons.deselect();
            }
        });
        
        teamFactory = new JButton();
        teamFactory.setIcon(new ImageIcon(ModelManager.getModel("Factory").getImage().getScaledInstance( 32, 22,  java.awt.Image.SCALE_SMOOTH )));
        teamFactory.setBackground(Color.BLUE);
        teamFactory.setMargin(new Insets(0, 0, 0, 0));
        teamFactory.setMaximumSize(new Dimension(40, 40));
        teamFactory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                type = team ? 8 : 9;
                unitButtons.deselect();
            }
        });

        blueTeam = new JRadioButton();
        blueTeam.setText("Blue");
        blueTeam.setFont(new Font("Consolas", 0, 9));
        blueTeam.setBackground(Color.BLACK);
        blueTeam.setForeground(Color.LIGHT_GRAY);
        blueTeam.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                team = true;
                unitButtons.changeTeam(true);
//                charlie.setIcon(new ImageIcon(ModelManager.getModel("Charlie").getImage(true)));
//                bison.setIcon(new ImageIcon(ModelManager.getModel("Bison").getImage(true)));
//                kilroy.setIcon(new ImageIcon(ModelManager.getModel("Kilroy").getImage(true)));
//                hadrian.setIcon(new ImageIcon(ModelManager.getModel("Hadrian").getImage(true)));
                teamFactory.setBackground(Color.BLUE);
                redTeam.setSelected(false);
            }
        });
        redTeam = new JRadioButton();
        redTeam.setText("Red");
        redTeam.setFont(new Font("Consolas", 0, 9));
        redTeam.setBackground(Color.BLACK);
        redTeam.setForeground(Color.LIGHT_GRAY);
        redTeam.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                team = false;
                unitButtons.changeTeam(false);
//                charlie.setIcon(new ImageIcon(ModelManager.getModel("Charlie").getImage(false)));
//                bison.setIcon(new ImageIcon(ModelManager.getModel("Bison").getImage(false)));
//                kilroy.setIcon(new ImageIcon(ModelManager.getModel("Kilroy").getImage(false)));
//                hadrian.setIcon(new ImageIcon(ModelManager.getModel("Hadrian").getImage(false)));
                teamFactory.setBackground(Color.red);
                blueTeam.setSelected(false);
            }
        });

        unitButtons = new UnitButtonsPanel();
        unitButtons.setMaximumSize(new Dimension(100, 0));
//        charlie = new JButton();
//        charlie.setIcon(new ImageIcon(ModelManager.getModel("Charlie").getImage(true)));
//        charlie.setMinimumSize(new Dimension(40, 40));
//        charlie.setMargin(new Insets(0, 0, 0, 0));
//        charlie.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                type = 10;
//            }
//        });
//
//        bison = new JButton();
//        bison.setIcon(new ImageIcon(ModelManager.getModel("Bison").getImage(true)));
//        bison.setMinimumSize(new Dimension(40, 40));
//        bison.setMargin(new Insets(0, 0, 0, 0));
//        bison.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                type = 11;
//            }
//        });
//        
//        kilroy = new JButton();
//        kilroy.setIcon(new ImageIcon(ModelManager.getModel("Kilroy").getImage(true)));
//        kilroy.setMinimumSize(new Dimension(40, 40));
//        kilroy.setMargin(new Insets(0, 0, 0, 0));
//        kilroy.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                type = 12;
//            }
//        });
//        
//        hadrian = new JButton();
//        hadrian.setIcon(new ImageIcon(ModelManager.getModel("Hadrian").getImage(true)));
//        hadrian.setMinimumSize(new Dimension(40, 40));
//        hadrian.setMargin(new Insets(0, 0, 0, 0));
//        hadrian.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                type = 13;
//            }
//        });

        save = new JButton();
        save.setText("Save");
        save.setBackground(Color.BLACK);
        save.setForeground(Color.LIGHT_GRAY);
        save.setFont(new Font("Consolas", 0, 16));
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (LocationManager.getBase(true) == null || LocationManager.getBase(false) == null) {
                    JOptionPane.showMessageDialog(design, "Base Missing");
                    return;
                }
                String mapName = JOptionPane.showInputDialog("Map Name?");
                if (mapName == null || mapName.equals("")) {
                    JOptionPane.showMessageDialog(design, "Invalid Name");
                    return;
                }
                try {
                    LocationManager.saveMap(mapName);
                    JOptionPane.showMessageDialog(design, "Map "+ mapName + " saved.");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(design, "Error occurred: "+ e.toString());
                }
            }
        });
        
        random = new JButton();
        random = new JButton();
        random.setText("Random");
        random.setBackground(Color.BLACK);
        random.setForeground(Color.LIGHT_GRAY);
        random.setFont(new Font("Consolas", 0, 16));
        random.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                LocationManager.generateMap(LocationManager.getSize().x, LocationManager.getSize().y);
                render();
            }
        });
    }

    private void layoutComponents() {
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        GroupLayout.ParallelGroup vGroup = layout.createParallelGroup();
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
        GroupLayout.ParallelGroup hSubParGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup vSubSeqGroup = layout.createSequentialGroup();
        hSubParGroup
                .addComponent(scroll);

        hGroup
                .addGroup(hSubParGroup)
                .addComponent(buttons);
        vSubSeqGroup
                .addComponent(scroll);
        vGroup
                .addGroup(vSubSeqGroup)
                .addComponent(buttons);
        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);
    }

    private void layoutButtons() {
        GroupLayout layout = new GroupLayout(buttons);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        GroupLayout.ParallelGroup hGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING, false);
        GroupLayout.SequentialGroup hg1 = layout.createSequentialGroup();
        GroupLayout.SequentialGroup hg2 = layout.createSequentialGroup();
        GroupLayout.SequentialGroup hg3 = layout.createSequentialGroup();
        GroupLayout.SequentialGroup hg4 = layout.createSequentialGroup();
        GroupLayout.SequentialGroup hg5 = layout.createSequentialGroup();
        GroupLayout.SequentialGroup hg6 = layout.createSequentialGroup();
        GroupLayout.ParallelGroup vg1 = layout.createParallelGroup();
        GroupLayout.ParallelGroup vg2 = layout.createParallelGroup();
        GroupLayout.ParallelGroup vg3 = layout.createParallelGroup();
        GroupLayout.ParallelGroup vg4 = layout.createParallelGroup();
        GroupLayout.ParallelGroup vg5 = layout.createParallelGroup();
        GroupLayout.ParallelGroup vg6 = layout.createParallelGroup();
        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

        hg1
                .addComponent(noTerrain)
                .addComponent(terrain0);

        hg2
                .addComponent(terrain1)
                .addComponent(terrain2);

        hg3
                .addComponent(terrain3)
                .addComponent(terrain4);

        hg4
                .addComponent(blueBase)
                .addComponent(redBase);

        hg5
                .addComponent(factory)
                .addComponent(teamFactory);
        
        hg6
                .addComponent(blueTeam)
                .addComponent(redTeam);

        vg1
                .addComponent(noTerrain)
                .addComponent(terrain0);

        vg2
                .addComponent(terrain1)
                .addComponent(terrain2);

        vg3
                .addComponent(terrain3)
                .addComponent(terrain4);

        vg4
                .addComponent(blueBase)
                .addComponent(redBase);
        
        vg5
                .addComponent(factory)
                .addComponent(teamFactory);


        vg6
                .addComponent(blueTeam)
                .addComponent(redTeam);

        hGroup
                .addGroup(hg1)
                .addGroup(hg2)
                .addGroup(hg3)
                .addGroup(hg4)
                .addGroup(hg5)
                .addGroup(hg6)
                .addComponent(unitButtons)
                .addComponent(save)
                .addComponent(random);
        vGroup
                .addGroup(vg1)
                .addGroup(vg2)
                .addGroup(vg3)
                .addGroup(vg4)
                .addGroup(vg5)
                .addGroup(vg6)
                .addComponent(unitButtons)
                .addComponent(save)
                .addComponent(random)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);
        buttons.setLayout(layout);

    }
}
