/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package military.gui;

import military.engine.CombatStats;
import military.engine.ImmutableGameState;

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
/**
 * Main Swing window for the game. All UI updates should occur on the EDT.
 * Rendering is triggered by the game loop but Swing components are managed safely.
 */
public class GUI extends JFrame {

    public interface GameActions {
        void onMove();
        void onAttack();
        void onInfo();
        void onEndTurn();
    }

    private GameActions actions; 
    public void setActions(GameActions actions) { this.actions = actions; }

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
        // Backward-compatible path: construct a temporary snapshot and delegate
        ImmutableGameState snap = new ImmutableGameState(turn, cursor, select,
                military.engine.DefaultUnitRepository.getInstance().getUnits(military.engine.Team.BLUE).size(),
                military.engine.DefaultUnitRepository.getInstance().getUnits(military.engine.Team.RED).size());
        render(snap);
    }

    // New snapshot-based rendering API
    public void render(ImmutableGameState state) {
        java.util.logging.Logger dbg = military.util.Logs.getLogger(GUI.class);
        String dbgMsg = "[DEBUG_LOG] GUI.render(state): select size=" + (state.getSelect()==null?"null":state.getSelect().size()) + ", cursor=" + state.getCursor() + ", EDT=" + javax.swing.SwingUtilities.isEventDispatchThread();
        dbg.info(dbgMsg);
        System.out.println(dbgMsg);
        displayPanel = hexGridPanel;
        this.turn = state.getTurn();
        player1.setText("<html>Player 1<br>Units: " + state.getBlueCount() + "</html>");
        player2.setText("<html>Player 2<br>Units: " + state.getRedCount() + "</html>");
        Point cursor = state.getCursor();
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
            hexGridPanel.render(new ArrayList<>(state.getSelect()), new Point(cursor.x, cursor.y));
            bottomPanel.render(cursor);
        } else {
            System.out.println("Cursor exceeds map bounds");
        }
    }

    public void moveCursor(Point cursor) {
        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            javax.swing.SwingUtilities.invokeLater(() -> moveCursor(new Point(cursor)));
            return;
        }
        int x = cursor.x;
        int y = cursor.y;
        if (x < 0 || ((x % 2 == 0) && (y == 0))) { // Ensure click is within map bounds
            return;
        }
        if (displayPanel == hexGridPanel) {
            hexGridPanel.grabFocus();
            try {
                hexGridPanel.drawCursor(new Point(cursor.x, cursor.y), turn);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            bottomPanel.render(cursor);
        }
        if (displayPanel == factoryPanel) {
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

    // Force an immediate repaint of the hex grid (used after critical state changes)
    public void forceGridRepaint() {
        try {
            if (hexGridPanel != null) {
                hexGridPanel.paintImmediately(hexGridPanel.getVisibleRect());
            }
        } catch (Exception ignore) {
            // best-effort
        }
    }

    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        hexGridPanel = new HexGridPanel();
        hexGridPanel.setPreferredSize(new Dimension(18 + 15 * 53, 25 + 10 * 50));
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
        //loadMap(null);
        displayPanel = hexGridPanel;

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

    // Layout constants
    private static final int BUTTONS_PANEL_WIDTH = 110;
    private static final int FONT_SIZE = 16;

    private void initButtons() {
        buttonsPanel = new JPanel();
        buttonsPanel.setPreferredSize(new Dimension(BUTTONS_PANEL_WIDTH, 600));
        buttonsPanel.setBackground(Color.black);
        shift = new JButton();
        shift.setBackground(new Color(0, 0, 255));
        shift.setForeground(Color.LIGHT_GRAY);
        shift.setText("Shift");
        shift.setFont(new Font("Consolas", 0, FONT_SIZE));
        shift.setToolTipText("Move a selected unit");
        shift.getAccessibleContext().setAccessibleName("Shift Button");
        shift.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (actions != null) {
                    actions.onMove();
                }
                // Always enqueue the legacy event to drive the game loop consistently
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
        attack.setFont(new Font("Consolas", 0, FONT_SIZE));
        attack.setToolTipText("Attack an adjacent enemy");
        attack.getAccessibleContext().setAccessibleName("Attack Button");
        attack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (actions != null) {
                    actions.onAttack();
                }
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
        info.setFont(new Font("Consolas", 0, FONT_SIZE));
        info.setToolTipText("Show information");
        info.getAccessibleContext().setAccessibleName("Info Button");
        info.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (actions != null) {
                    actions.onInfo();
                }
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
        end.setFont(new Font("Consolas", 0, FONT_SIZE));
        end.setToolTipText("End current player's turn");
        end.getAccessibleContext().setAccessibleName("End Turn Button");
        end.setText("End");
        end.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (actions != null) {
                    actions.onEndTurn();
                }
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

    /**
         * Switches the display to the factory panel for the given factory.
         */
        public void displayFactory(Factory factory) {
        displayPanel = factoryPanel;
        layoutComponents();
        setVisible(true);
        bottomPanel.factoryUnit(factory.getUnit(0));
        factoryPanel.displayFactory(factory);
    }

    /**
         * Loads and displays a background map image; failure is logged and non-fatal.
         */
        public void loadMap(String mapImageName) {
        //mapPanel = new JPanel();
       // InputStream inStream = null;
        try {
            BufferedImage bimg = military.util.ImageCache.get("Resources/maps/bd01v2.gif");
            int width = bimg.getWidth();
            int height = bimg.getHeight();
//            Image image = ImageIO.read(inStream);
//            final Dimension jpanelDimensions = new Dimension(new ImageIcon(image).getIconWidth(), new ImageIcon(image).getIconHeight());
            final Dimension jpanelDimensions = new Dimension(width, height);
//            final Dimension jpanelDimensions = new Dimension(18 + 15* 53, 25 + 10 * 50);
           // inStream.close();
            hexGridPanel.add(new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(bimg, 45, 25, null);
                }

                @Override
                public Dimension getPreferredSize() {
                    //return super.getPreferredSize();
                    return jpanelDimensions;
                }
            });
        } catch (Exception ex) {
            java.util.logging.Logger logger = military.util.Logs.getLogger(GUI.class);
            logger.info("Background image not found: Resources/maps/bd01v2.gif");
        }
    }

    public static BufferedImage resize(final Image image, final int width, final int height){
        assert image != null;
        final BufferedImage bi = new BufferedImage(width, height, image instanceof BufferedImage ? ((BufferedImage)image).getType() : BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = bi.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return bi;
    }
}
