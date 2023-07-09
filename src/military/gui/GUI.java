/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package military.gui;

import military.engine.CombatStats;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;

import military.engine.Factory;
import military.engine.LocationManager;
import military.engine.UnitManager;

/**
 *
 * @author Nate
 */
public class GUI extends JFrame {

    private HexGridPanel hexGridPanel;
    private FactoryPanel factoryPanel;
    private JPanel displayPanel;
    private BottomPanel bottomPanel;
    private JPanel buttonsPanel;
    private JPanel mapPanel;
    private JButton shift;
    private JButton attack;
    private JButton info;
    private JButton end;
    private JLabel mapName;
    private int turnNumber;
    private JLabel turnNumberLabel;
    private JLabel player1;
    private JLabel player2;
    private boolean turn;

    public GUI(String map) {
        turnNumber = 1;
        initComponents();
        mapName.setText("<html>Map:<br>" + map + "</html>");
        turnNumberLabel.setText("Turn " + turnNumber);
        hexGridPanel.grabFocus();
        buttonsPanel.setVisible(true);
        this.setVisible(true);
    }

    public void render(boolean turn, ArrayList<Point> select, Point cursor) {
        displayPanel = hexGridPanel;
        this.turn = turn;
        player1.setText("<html>Player 1<br>Units: " + UnitManager.getInstance().getUnits(true).size() + "</html>");
        player2.setText("<html>Player 2<br>Units: " + UnitManager.getInstance().getUnits(false).size() + "</html>");
        if (cursor.x == -1) {
            if (cursor.y == 0) {
                shift.grabFocus();
            } else if (cursor.y == 1) {
                attack.grabFocus();
            } else if (cursor.y == 2) {
                info.grabFocus();
            } else if (cursor.y == 3) {
                end.grabFocus();
            }
            return;
        }
        if (!hexGridPanel.hasFocus()) {
            hexGridPanel.grabFocus();
        }
        if (LocationManager.getSize().x > cursor.x && LocationManager.getSize().y > cursor.y) {
            hexGridPanel.render(select, new Point(cursor.x, cursor.y));
            bottomPanel.render(cursor);
        } else {
            System.out.println("Cursor exceeds map bounds");
        }
    }

    public void moveCursor(Point cursor) {
        int x, y;
        x = cursor.x;
        y = cursor.y;
        if (x < 0 || ((x % 2 == 0) && (y == 0))) { // Ensure click is within map bounds
            return;
        }
        if(displayPanel == hexGridPanel){
            hexGridPanel.grabFocus();
            try {
                hexGridPanel.drawCursor(new Point(cursor.x, cursor.y), turn);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            bottomPanel.render(cursor);
        }
        if(displayPanel == factoryPanel){
            factoryPanel.drawCursor(new Point(cursor.x, cursor.y));
            bottomPanel.factoryUnit(factoryPanel.getUnit());
        }
        
    }

    public void displayCombat(CombatStats cstat) {
        bottomPanel.displayCombat(cstat);
        hexGridPanel.displayCombat(cstat);
        boolean turn = cstat.getAttacker().getTeam();
        bottomPanel.updateExp((turn ? cstat.getAttacker().getExp() : cstat.getDefender().getExp()),
                (!turn ? cstat.getAttacker().getExp() : cstat.getDefender().getExp()));
    }

    public void incrementTurn() {
        turnNumber++;
        turnNumberLabel.setText("Turn " + turnNumber);
    }

    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        hexGridPanel = new HexGridPanel();
        hexGridPanel.setPreferredSize(new Dimension(18 + 15* 53, 25 + 10 * 50));
        hexGridPanel.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                GUIMiddleMan.getInstance().putEvent(evt);
            }
        });
        hexGridPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                GUIMiddleMan.getInstance().putEvent(evt);
            }
        });
        displayPanel = hexGridPanel;

        loadMap(null);
        bottomPanel = new BottomPanel();
        bottomPanel.setPreferredSize(new Dimension(hexGridPanel.getWidth(), 75));
        bottomPanel.setBackground(Color.red);
        factoryPanel = new FactoryPanel();
        factoryPanel.setPreferredSize(new Dimension(18 + 15 * 53, 25 + 10 * 50));
        factoryPanel.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                GUIMiddleMan.getInstance().putEvent(evt);
            }
        });
        initButtons();
        layoutComponents();
        layoutButtons();
        pack();
    }

    private void initButtons() {
        buttonsPanel = new JPanel();
        buttonsPanel.setPreferredSize(new Dimension(110, 600));
        buttonsPanel.setBackground(Color.black);
        shift = new JButton();
        shift.setBackground(new Color(0, 0, 255));
        shift.setForeground(Color.LIGHT_GRAY);
        shift.setText("Shift");
        shift.setFont(new Font("Consolas", 0, 16));
        shift.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                GUIMiddleMan.getInstance().putEvent(new MouseEvent(shift, 0, 0, 0, -1, 0, 1, false));
            }
        });
        shift.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                shift.setBackground(Color.red);
            }

            public void focusLost(FocusEvent evt) {
                shift.setBackground(Color.blue);
            }
        });
        shift.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                GUIMiddleMan.getInstance().putEvent(evt);
            }
        });
        attack = new JButton();
        attack.setBackground(new Color(0, 0, 255));
        attack.setForeground(Color.LIGHT_GRAY);
        attack.setText("Attack");
        attack.setFont(new Font("Consolas", 0, 16));
        attack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                GUIMiddleMan.getInstance().putEvent(new MouseEvent(shift, 0, 0, 0, -1, 1, 1, false));
            }
        });
        attack.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                attack.setBackground(Color.red);
            }

            public void focusLost(FocusEvent evt) {
                attack.setBackground(Color.blue);
            }
        });
        attack.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                GUIMiddleMan.getInstance().putEvent(evt);
            }
        });
        info = new JButton();
        info.setBackground(new Color(0, 0, 255));
        info.setForeground(Color.LIGHT_GRAY);
        info.setText("Info");
        info.setFont(new Font("Consolas", 0, 16));
        info.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                GUIMiddleMan.getInstance().putEvent(new MouseEvent(shift, 0, 0, 0, -1, 2, 1, false));
            }
        });
        info.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                info.setBackground(Color.red);
            }

            public void focusLost(FocusEvent evt) {
                info.setBackground(Color.blue);
            }
        });
        info.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                GUIMiddleMan.getInstance().putEvent(evt);
            }
        });
        end = new JButton();
        end.setBackground(new Color(0, 0, 255));
        end.setForeground(Color.LIGHT_GRAY);
        end.setFont(new Font("Consolas", 0, 16));
        end.setText("End");
        end.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                GUIMiddleMan.getInstance().putEvent(new MouseEvent(shift, 0, 0, 0, -1, 3, 1, false));
            }
        });
        end.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                end.setBackground(Color.red);
            }

            public void focusLost(FocusEvent evt) {
                end.setBackground(Color.blue);
            }
        });
        end.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                GUIMiddleMan.getInstance().putEvent(evt);
            }
        });

        mapName = new JLabel();
        mapName.setBackground(new Color(0, 0, 255));
        mapName.setForeground(Color.LIGHT_GRAY);
        mapName.setFont(new Font("Consolas", 0, 16));

        turnNumberLabel = new JLabel();
        turnNumberLabel.setBackground(new Color(0, 0, 255));
        turnNumberLabel.setForeground(Color.LIGHT_GRAY);
        turnNumberLabel.setFont(new Font("Consolas", 0, 16));

        player1 = new JLabel();
        player1.setBackground(new Color(0, 0, 255));
        player1.setForeground(Color.LIGHT_GRAY);
        player1.setFont(new Font("Consolas", 0, 16));

        player2 = new JLabel();
        player2.setBackground(new Color(0, 0, 255));
        player2.setForeground(Color.LIGHT_GRAY);
        player2.setFont(new Font("Consolas", 0, 16));
    }

    private void layoutComponents() {     
        Container pane = getContentPane();
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        
        c.gridx = 0;
        c.gridy = 0;
        pane.add(displayPanel, c);
        
        c.gridx = 0;
        c.gridy = 1;
        pane.add(bottomPanel, c);
        
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 2;
        pane.add(buttonsPanel, c);
    }

    private void layoutButtons() {
        GroupLayout layout = new GroupLayout(buttonsPanel);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        GroupLayout.ParallelGroup hGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING, false);
        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        hGroup
                .addComponent(shift, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(attack, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(info, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(end, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(mapName, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(turnNumberLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(player1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(player2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        vGroup
                .addComponent(shift)
                .addComponent(attack)
                .addComponent(info)
                .addComponent(end)
                .addGap(30)
                .addComponent(mapName)
                .addGap(30)
                .addComponent(turnNumberLabel)
                .addGap(30)
                .addComponent(player1)
                .addGap(30)
                .addComponent(player2)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);
        buttonsPanel.setLayout(layout);

    }

    public void displayFactory(Factory factory) {
        displayPanel = factoryPanel;
        layoutComponents();
        setVisible(true);
        bottomPanel.factoryUnit(factory.getUnit(0));
        factoryPanel.displayFactory(factory);
    }

    public void loadMap(String mapImageName) {
        //mapPanel = new JPanel();
        InputStream inStream = null;
        try {
            inStream = new FileInputStream("Resources//maps/bd01.gif");
            BufferedImage bimg = ImageIO.read(new File("Resources//maps/bd01.gif"));
            int width = bimg.getWidth();
            int height = bimg.getHeight();
//            Image image = ImageIO.read(inStream);
//            final Dimension jpanelDimensions = new Dimension(new ImageIcon(image).getIconWidth(), new ImageIcon(image).getIconHeight());
            final Dimension jpanelDimensions = new Dimension(width, height);
            inStream.close();
            displayPanel.add(new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(bimg, 0, 0, null);
                }

                @Override
                public Dimension getPreferredSize() {
                    //return super.getPreferredSize();
                    return jpanelDimensions;
                }
            });
        } catch (Exception ex) {
            System.out.println("no Image");
        }
    }
}
