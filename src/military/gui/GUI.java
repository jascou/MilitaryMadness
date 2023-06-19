/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package military.gui;

import military.engine.CombatStats;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JLabel;
import javax.swing.JPanel;
import military.engine.Factory;
import military.engine.UnitManager;

/**
 *
 * @author Nate
 */
public class GUI extends JFrame {

    private HexGridPanel hex;
    private FactoryPanel fact;
    private JPanel display;
    private BottomPanel bottom;
    private JPanel buttons;
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
        hex.grabFocus();
        buttons.setVisible(true);
        this.setVisible(true);
    }

    public void render(boolean turn, ArrayList<Point> select, Point cursor) {
        display = hex;
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
        if (!hex.hasFocus()) {
            hex.grabFocus();
        }
        hex.render(select, new Point(cursor.x, cursor.y));
        bottom.render(cursor);
    }

    public void moveCursor(Point cursor) {
        if(display == hex){
            hex.grabFocus();
            try {
                hex.drawCursor(new Point(cursor.x, cursor.y), turn);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();  // TODO: Limit cursor movement to within game map bounds
            }
            bottom.render(cursor);
        }
        if(display == fact){
            fact.drawCursor(new Point(cursor.x, cursor.y));
            bottom.factoryUnit(fact.getUnit());
        }
        
    }

    public void displayCombat(CombatStats cstat) {
        bottom.displayCombat(cstat);
        hex.displayCombat(cstat);
        boolean turn = cstat.getAttacker().getTeam();
        bottom.updateExp((turn ? cstat.getAttacker().getExp() : cstat.getDefender().getExp()),
                (!turn ? cstat.getAttacker().getExp() : cstat.getDefender().getExp()));
    }

    public void incrementTurn() {
        turnNumber++;
        turnNumberLabel.setText("Turn " + turnNumber);
    }

    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        hex = new HexGridPanel();
        hex.setPreferredSize(new Dimension(18 + 15* 53, 25 + 10 * 50));
        hex.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                GUIMiddleMan.getInstance().putEvent(evt);
            }
        });
        hex.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                GUIMiddleMan.getInstance().putEvent(evt);
            }
        });
        display = hex;
        bottom = new BottomPanel();
        bottom.setPreferredSize(new Dimension(hex.getWidth(), 75));
        bottom.setBackground(Color.red);
        fact = new FactoryPanel();
        fact.setPreferredSize(new Dimension(18 + 15 * 53, 25 + 10 * 50));
        fact.addKeyListener(new KeyAdapter() {
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
        buttons = new JPanel();
        buttons.setPreferredSize(new Dimension(110, 600));
        buttons.setBackground(Color.black);
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
        pane.add(display, c);
        
        c.gridx = 0;
        c.gridy = 1;
        pane.add(bottom, c);
        
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 2;
        pane.add(buttons, c);
    }

    private void layoutButtons() {
        GroupLayout layout = new GroupLayout(buttons);
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
        buttons.setLayout(layout);

    }

    public void displayFactory(Factory factory) {
        display = fact;
        layoutComponents();
        setVisible(true);
        bottom.factoryUnit(factory.getUnit(0));
        fact.displayFactory(factory);
    }
}
