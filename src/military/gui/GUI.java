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
        // New menu actions
        void onSave(String name);
        void onLoad(String name);
        void onExit();
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
    private JLabel playerTurnLabel;
    private JLabel player1;
    private JLabel player2;
    private boolean turn;
    private MiniMapPanel miniMapPanel;

    // Create the application menu bar with Save, Load, Exit actions
    private void installMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem saveItem = new JMenuItem("Save...");
        JMenuItem loadItem = new JMenuItem("Load...");
        JMenuItem exitItem = new JMenuItem("Exit");

        saveItem.addActionListener(e -> {
            if (actions == null) return;
            String name = JOptionPane.showInputDialog(this, "Enter save name:", "Save Game", JOptionPane.QUESTION_MESSAGE);
            if (name != null) {
                name = name.trim();
                if (!name.isEmpty()) {
                    try {
                        actions.onSave(name);
                        JOptionPane.showMessageDialog(this, "Game saved as '" + name + "'", "Save", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Throwable t) {
                        JOptionPane.showMessageDialog(this, "Failed to save: " + t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        loadItem.addActionListener(e -> {
            if (actions == null) return;
            String name = military.gui.SaveSelectionDialog.showDialog(this);
            if (name != null && !name.trim().isEmpty()) {
                try {
                    actions.onLoad(name.trim());
                    JOptionPane.showMessageDialog(this, "Loaded save '" + name.trim() + "'", "Load", JOptionPane.INFORMATION_MESSAGE);
                } catch (Throwable t) {
                    JOptionPane.showMessageDialog(this, "Failed to load: " + t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        exitItem.addActionListener(e -> {
            if (actions != null) {
                actions.onExit();
            } else {
                dispose();
                System.exit(0);
            }
        });

        gameMenu.add(saveItem);
        gameMenu.add(loadItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);
        bar.add(gameMenu);
        setJMenuBar(bar);
    }

    private String promptForExistingSave() {
        try {
            java.nio.file.Path dir = military.Config.savesDir();
            java.nio.file.Files.createDirectories(dir);
            java.util.List<String> names = new java.util.ArrayList<>();
            try (java.util.stream.Stream<java.nio.file.Path> s = java.nio.file.Files.list(dir)) {
                s.filter(p -> p.getFileName().toString().endsWith(".mmsave"))
                 .forEach(p -> {
                     String fn = p.getFileName().toString();
                     names.add(fn.substring(0, fn.length() - ".mmsave".length()));
                 });
            }
            if (names.isEmpty()) return null;
            Object choice = JOptionPane.showInputDialog(this, "Choose a save:", "Load Game",
                    JOptionPane.QUESTION_MESSAGE, null, names.toArray(), names.get(0));
            return choice == null ? null : choice.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    public GUI(String map) {
        turnNumber = 1;
        initComponents();
        installMenuBar();
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
        dbg.fine(dbgMsg);
        displayPanel = hexGridPanel;
        this.turn = state.getTurn();
        // Update current player turn label
        if (playerTurnLabel != null) {
            playerTurnLabel.setText(this.turn ? "Player 1 (Blue)" : "Player 2 (Red)");
        }
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
            if (miniMapPanel != null) {
                try {
                    miniMapPanel.render(hexGridPanel.getViewportCorner(), cursor, hexGridPanel.getViewWidth(), hexGridPanel.getViewHeight());
                } catch (Throwable t) {
                    // ignore
                }
            }
        } else {
            java.util.logging.Logger logger = military.util.Logs.getLogger(GUI.class);
            logger.fine("Cursor exceeds map bounds");
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
            if (miniMapPanel != null) {
                try {
                    miniMapPanel.render(hexGridPanel.getViewportCorner(), cursor, hexGridPanel.getViewWidth(), hexGridPanel.getViewHeight());
                } catch (Throwable t) {
                    // ignore
                }
            }
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
        } catch (Exception ex) {
            java.util.logging.Logger log = military.util.Logs.getLogger(GUI.class);
            log.fine("forceGridRepaint failed: " + ex.toString());
        }
    }

    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        hexGridPanel = new HexGridPanel();
        // Add ~30px padding around the game view (total +60 in both dimensions)
        Dimension gridPref = new Dimension(18 + 15 * 53 + 60, 25 + 10 * 50 + 60);
        hexGridPanel.setPreferredSize(gridPref);
        hexGridPanel.setMinimumSize(gridPref);
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
        // Use the hex grid's preferred width (not current width which is 0 at init)
        bottomPanel.setPreferredSize(new Dimension(hexGridPanel.getPreferredSize().width, 100));
        bottomPanel.setBackground(Color.red);
        factoryPanel = new FactoryPanel();
        // Match the same padding for factory view
        factoryPanel.setPreferredSize(new Dimension(18 + 15 * 53 + 60, 25 + 10 * 50 + 60));
        factoryPanel.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                GUIMiddleMan.getInstance().putEvent(evt);
            }
        });
        initButtons();
        layoutComponents();
        layoutButtons();
        pack();
        // Ensure the initial window is large enough to show both the map and the sidebar
        enforceInitialFrameSize();
    }

    /**
     * Ensure the initial frame size accommodates both the map view and the right sidebar,
     * preventing the map from being hidden until a manual resize occurs.
     */
    private void enforceInitialFrameSize() {
        try {
            Dimension map = (hexGridPanel != null) ? hexGridPanel.getPreferredSize() : new Dimension(800, 600);
            Dimension sidebar = (buttonsPanel != null) ? buttonsPanel.getPreferredSize() : new Dimension(160, 600);
            Dimension bottom = (bottomPanel != null) ? bottomPanel.getPreferredSize() : new Dimension(map.width, 75);
            int padW = 40; // a little breathing room to account for borders/scrollbars
            int padH = 60;
            int width = Math.max(600, map.width + sidebar.width + padW);
            int height = Math.max(400, map.height + bottom.height + padH);
            Dimension target = new Dimension(width, height);
            setMinimumSize(target);
            setSize(target);
            setLocationByPlatform(true);
        } catch (Throwable ignored) {
            // Best-effort sizing; ignore failures on headless or during tests
        }
    }

    // Layout constants
    private static final int BUTTONS_PANEL_WIDTH = 160;
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
        military.gui.controls.ButtonBinder.bind(shift, () -> {
            if (actions != null) actions.onMove();
        }, 0);
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
        military.gui.controls.ButtonBinder.bind(attack, () -> {
            if (actions != null) actions.onAttack();
        }, 1);
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
        military.gui.controls.ButtonBinder.bind(info, () -> {
            if (actions != null) actions.onInfo();
        }, 2);
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
        military.gui.controls.ButtonBinder.bind(end, () -> {
            if (actions != null) actions.onEndTurn();
        }, 3);
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

        playerTurnLabel = new JLabel();
        playerTurnLabel.setBackground(new Color(0, 0, 255));
        playerTurnLabel.setForeground(Color.LIGHT_GRAY);
        playerTurnLabel.setFont(new Font("Consolas", 0, 16));
        playerTurnLabel.setText("Player 1 (Blue)");

        player1 = new JLabel();
        player1.setBackground(new Color(0, 0, 255));
        player1.setForeground(Color.LIGHT_GRAY);
        player1.setFont(new Font("Consolas", 0, 16));

        player2 = new JLabel();
        player2.setBackground(new Color(0, 0, 255));
        player2.setForeground(Color.LIGHT_GRAY);
        player2.setFont(new Font("Consolas", 0, 16));

        // Mini map panel (bottom-right outside playfield)
        miniMapPanel = new MiniMapPanel();
        miniMapPanel.setPreferredSize(new Dimension(BUTTONS_PANEL_WIDTH - 20, BUTTONS_PANEL_WIDTH - 20));
        // Clicking on the minimap repositions the main viewport
        miniMapPanel.setClickListener(mapCoord -> {
            try {
                hexGridPanel.centerViewportOn(mapCoord);
                // Refresh minimap with new viewport; keep previous cursor
                miniMapPanel.render(hexGridPanel.getViewportCorner(), null, hexGridPanel.getViewWidth(), hexGridPanel.getViewHeight());
            } catch (Throwable t) {
                // ignore
            }
        });
    }

    private void layoutComponents() {     
        Container pane = getContentPane();
        pane.setLayout(new GridBagLayout());

        // Left: main display (hex grid or factory)
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = 0;
        left.weightx = 1.0;
        left.weighty = 1.0;
        left.fill = GridBagConstraints.BOTH;
        pane.add(displayPanel, left);

        // Bottom: status area under the main display
        GridBagConstraints bottom = new GridBagConstraints();
        bottom.gridx = 0;
        bottom.gridy = 1;
        bottom.weightx = 1.0;
        bottom.weighty = 0.0;
        bottom.fill = GridBagConstraints.HORIZONTAL;
        pane.add(bottomPanel, bottom);

        // Right: buttons/sidebar occupying both rows
        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = 0;
        right.gridheight = 2;
        right.weightx = 0.0;
        right.weighty = 1.0;
        right.fill = GridBagConstraints.VERTICAL; // allow vertical growth, keep fixed width
        pane.add(buttonsPanel, right);
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
                .addComponent(playerTurnLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(player1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(player2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(miniMapPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        vGroup
                .addComponent(shift)
                .addComponent(attack)
                .addComponent(info)
                .addComponent(end)
                .addGap(30)
                .addComponent(mapName)
                .addGap(30)
                .addComponent(turnNumberLabel)
                .addGap(10)
                .addComponent(playerTurnLabel)
                .addGap(30)
                .addComponent(player1)
                .addGap(30)
                .addComponent(player2)
                .addGap(20)
                .addComponent(miniMapPanel)
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
            String bg = military.Config.resourcesDir().resolve("maps").resolve("bd01v2.gif").toString();
            BufferedImage bimg = military.util.ImageCache.get(bg);
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
            logger.info("Background image not found: " + military.Config.resourcesDir().resolve("maps").resolve("bd01v2.gif"));
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
