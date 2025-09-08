/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package military;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JOptionPane;

import military.engine.Base;
import military.engine.Location;
import military.engine.LocationManager;
import military.engine.Unit;
import military.engine.CombatStats;
import military.engine.Factory;
import military.engine.UnitManager;
import military.gui.GUI;
import military.gui.GUIMiddleMan;
import military.gui.HexGridPanel;
import military.gui.HexMech;

/**
 * @author Nate
 */


public class Game implements Runnable {

    // Exposed getters to support external GameLoop controller
    public boolean getTurn() { return turn; }
    public boolean isFactory() { return factory; }
    public military.gui.GUI getGui() { return gui; }
    public military.engine.GameController getController() { return controller; }
    public java.util.ArrayList<java.awt.Point> getSelectLocs() { return new java.util.ArrayList<>(selectLocs); }
    public java.awt.Point getRenderCursor() { return (buttonCursor.y == -1) ? cursor : buttonCursor; }

    final int KEY_SHIFT = 16;
    final int KEY_CTRL = 17;
    final int KEY_SPACE_BAR = 32;
    final int KEY_ARROW_LEFT = 37;
    final int KEY_ARROW_UP = 38;
    final int KEY_ARROW_RIGHT = 39;
    final int KEY_ARROW_DOWN = 40;
    final int KEY_LETTER_A = 65;
    final int KEY_LETTER_D = 68;
    final int KEY_LETTER_S = 83;
    final int KEY_LETTER_W = 87;

    final int MOUSE_LEFT_BTN = 1;
    final int MOUSE_MIDDLE_BTN = 3;
    final int MOUSE_RIGHT_BTN = 2;


    private final GUI gui;
    private boolean turn;
    private Point cursor;
    private Point buttonCursor;
    private Point factoryLoc;
    private int factoryUnit;
    private Factory mFactory;
    private Point unitLoc;
    private ArrayList<Point> selectLocs;
    private boolean shifting;
    private boolean attacking;
    private boolean factory;

    // Initial step toward decoupling UI and game state
    private final military.engine.GameState state;
    private final military.engine.GameController controller;

    public Game(String levelName) {
        new military.engine.DefaultMapService().loadMap(levelName);
        gui = new GUI(levelName);
        // Provide GUI with controller actions (dependency inversion)
        gui.setActions(new military.gui.GUI.GameActions() {
            @Override public void onMove() { shift(); }
            @Override public void onAttack() { attack(); }
            @Override public void onInfo() { info(); }
            @Override public void onEndTurn() { end(); }
        });
        turn = true;
        cursor = new Point(1, 0);
        buttonCursor = new Point(-1, -1);
        selectLocs = new ArrayList<>();
        factoryUnit = -1;
        // Initialize controller and shared state
        state = new military.engine.GameState();
        state.setTurn(turn);
        state.setCursor(new Point(cursor));
        controller = new military.engine.GameController(state);
        // Announce the initial active player at game start
        announceTurn(turn);
    }

    @Override
    public void run() {
        while (!LocationManager.isCaptured(!turn)) {
            // Optionally, preload frequently used images (background)
            String bg = military.Config.resourcesDir().resolve("maps").resolve("bd01v2.gif").toString();
            military.util.ResourceLoader.preloadImages(bg);
            if (!factory) {
                controller.render(gui, turn, selectLocs, (buttonCursor.y == -1) ? cursor : buttonCursor);
            }
            InputEvent evt = GUIMiddleMan.getInstance().getEvent();
            KeyEvent kevt;
            MouseEvent mevt;
            if (evt instanceof KeyEvent) {
                kevt = (KeyEvent) evt;
                if (buttonCursor.y != -1) {
                    if (kevt.getKeyCode() == KEY_ARROW_UP || kevt.getKeyCode() == KEY_LETTER_W) {
                        buttonCursor.y--;
                        if (buttonCursor.y == -1) {
                            buttonCursor.y = 3;
                        }
                    }
                    if (kevt.getKeyCode() == KEY_ARROW_DOWN || kevt.getKeyCode() == KEY_LETTER_S) {
                        buttonCursor.y++;
                        if (buttonCursor.y == 4) {
                            buttonCursor.y = 0;
                        }
                    }
                    if (kevt.getKeyCode() == military.util.InputMappings.CTRL) {      // centralized
                        if (buttonCursor.y == 0) {
                            shift();
                        } else if (buttonCursor.y == 1) {
                            attack();
                        } else if (buttonCursor.y == 2) {
                            info();
                        } else if (buttonCursor.y == 3) {
                            end();
                        }
                    }
                    if (kevt.getKeyCode() == KEY_SHIFT) {
                        buttonCursor.y = -1;
                    }
                    continue;
                }
                while (kevt.getKeyCode() >= KEY_ARROW_LEFT && kevt.getKeyCode() <= KEY_ARROW_DOWN
                        || kevt.getKeyCode() >= KEY_LETTER_A && kevt.getKeyCode() <= KEY_LETTER_W) {
                    if ((kevt.getKeyCode() == KEY_ARROW_UP || kevt.getKeyCode() == KEY_LETTER_W) && cursor.y != 0) {
                        cursor.y--;
                    }
                    if ((kevt.getKeyCode() == KEY_ARROW_DOWN || kevt.getKeyCode() == KEY_LETTER_S)
                            && (cursor.y < 2 || (!factory && cursor.y < LocationManager.getSize().y - 1))) {
                        cursor.y++;
                    }
                    if ((kevt.getKeyCode() == KEY_ARROW_LEFT || kevt.getKeyCode() == KEY_LETTER_A) && cursor.x != 1) {
                        cursor.x--;
                    }
                    if ((kevt.getKeyCode() == KEY_ARROW_RIGHT || kevt.getKeyCode() == KEY_LETTER_D)
                            && (cursor.x < 3 || (!factory && cursor.x < LocationManager.getSize().x - 1))) {
                        cursor.x++;
                    }
                    gui.moveCursor(cursor);
                    evt = GUIMiddleMan.getInstance().getEvent();
                    if (evt instanceof MouseEvent) {
                        break;
                    }
                    kevt = (KeyEvent) evt;
                }
                // Allow keyboard Shift to initiate movement highlighting directly
                if (kevt.getKeyCode() == military.util.InputMappings.SHIFT) {
                    if (!shifting && !attacking && !factory) {
                        shift();
                        continue;
                    }
                }
                if (kevt.getKeyCode() == military.util.InputMappings.ENTER) {
                    if (!shifting && !attacking && !factory) {
                        if (LocationManager.getLoc(cursor) instanceof Factory) {
                            mFactory = (Factory) LocationManager.getLoc(cursor);
                            factory = true;
                            factoryLoc = cursor;
                            cursor = new Point(0, 0);
                            gui.displayFactory((Factory) LocationManager.getLoc(factoryLoc));
                        } else {
                            buttonCursor.setLocation(-1, 0);
                        }
                        continue;
                    }
                    if (shifting) {
                        boolean validMove = false;
                        for (Point p : selectLocs) {
                            if (cursor.x == p.x && cursor.y == p.y) {
                                validMove = true;
                            }
                        }
                        if (!validMove) {
                            JOptionPane.showMessageDialog(gui, "Cannot Move Here");
                            continue;
                        }
                        Location newLoc = LocationManager.getLoc(cursor);
                        if (factoryUnit == -1) {
                            Unit u = LocationManager.getLoc(unitLoc).getUnit();
                            LocationManager.getLoc(unitLoc).getUnit().move(newLoc);
                            shifting = false;
                            selectLocs.clear();
                            if (!(newLoc instanceof Factory)) {
                                attack();
                            }
                            u.attack();
                        } else {
                            ((Factory) LocationManager.getLoc(unitLoc)).getUnit(factoryUnit).move(newLoc);
                            factoryUnit = -1;
                            shifting = false;
                            selectLocs.clear();
                            Unit u = newLoc.getUnit();
                            mFactory.removeUnit(u);
                            u.attack();
                        }
                        // Immediately update the UI to clear or update highlights after movement
                        controller.render(gui, turn, selectLocs, (buttonCursor.y == -1) ? cursor : buttonCursor);
                        try { gui.forceGridRepaint(); } catch (Exception ex) {
                            java.util.logging.Logger log = military.util.Logs.getLogger(Game.class);
                            log.fine("forceGridRepaint threw: " + ex.toString());
                        }
                        if (LocationManager.getLoc(cursor) instanceof Base && ((Base) newLoc).getTeam() != turn) {
                            JOptionPane.showMessageDialog(gui, "Player " + (turn ? "1" : "2") + " Wins!");
                            gui.dispose();
                            return;
                        }

                    } else if (attacking) {
                        boolean validAttack = false;
                        for (Point p : selectLocs) {
                            if (cursor.x == p.x && cursor.y == p.y) {
                                validAttack = true;
                            }
                        }
                        if (!validAttack) {
                            JOptionPane.showMessageDialog(gui, "Cannot Attack Here");
                            continue;
                        }
                        displayCombatAndPost(military.engine.CombatResolver.resolve(
                                LocationManager.getLoc(unitLoc).getUnit(),
                                LocationManager.getLoc(cursor).getUnit(),
                                LocationManager.getLoc(cursor)));
                        if (!military.engine.TurnRules.ALLOW_MOVE_AFTER_ATTACK) {
                            // Enforcement is already via attack() marking flags; this documents the rule usage
                        }
                        LocationManager.getLoc(unitLoc).getUnit().attack();
                        attacking = false;
                        selectLocs.clear();
                        if (LocationManager.getLoc(cursor).getUnit().getHealth() <= 0) {
                            UnitManager.getInstance().removeUnit(LocationManager.getLoc(cursor).getUnit());
                            LocationManager.getLoc(cursor).removeUnit();
                        }
                        if (LocationManager.getLoc(unitLoc).getUnit().getHealth() <= 0) {
                            UnitManager.getInstance().removeUnit(LocationManager.getLoc(unitLoc).getUnit());
                            LocationManager.getLoc(unitLoc).removeUnit();
                        }
                        if (unitRepo.getUnits(military.engine.Team.RED).isEmpty()) {
                            JOptionPane.showMessageDialog(gui, "Player 1 Wins!");
                            gui.dispose();
                            return;
                        }
                        if (unitRepo.getUnits(military.engine.Team.BLUE).isEmpty()) {
                            JOptionPane.showMessageDialog(gui, "Player 2 Wins!");
                            gui.dispose();
                            return;
                        }
                        try {
                            while ((GUIMiddleMan.getInstance().getEvent() instanceof KeyEvent)
                                    && ((KeyEvent) GUIMiddleMan.getInstance().getEvent()).getKeyCode() != 10) {
                            }
                        } catch (ClassCastException e) {
                            // Added exception June 18, 2023 to catch casting MouseEvent to KeyEvent
                            e.printStackTrace();
                        }
                    } else if (factory) {
                        if (mFactory.isControlled() && mFactory.getTeam() == turn) {
                            if (mFactory.getUnits().size() > cursor.x + (4 * cursor.y)) {
                                if (!mFactory.getUnit(cursor.x + (4 * cursor.y)).isAttackDone()) {
                                    shifting = true;
                                    selectLocs.clear();
                                    for (Location loc : mFactory.getAdjacent()) {
                                        if (loc.getTerrain() < 40 && loc.getTerrain() != -1) {
                                            selectLocs.add(loc.getLoc());
                                        }
                                    }
                                    factoryUnit = cursor.x + (4 * cursor.y);
                                    unitLoc = factoryLoc;
                                    cursor = new Point(factoryLoc);
                                    factory = false;
                                }
                            }
                        }
                    }
                }
                if (kevt.getKeyCode() == military.util.InputMappings.ESCAPE) {
                    if (shifting) {
                        shifting = false;
                        selectLocs.clear();
                    }
                    if (attacking) {
                        attacking = false;
                        selectLocs.clear();
                    }
                    if (factory) {
                        factory = false;
                        factoryUnit = -1;
                        cursor = new Point(factoryLoc);
                    }
                }
            }
            if (evt instanceof MouseEvent && !factory) {
                interpretMouseEvent((MouseEvent) evt);
            }

        }
    }

    private void interpretMouseEvent(MouseEvent mevt) {
        while (mevt.getComponent() instanceof HexGridPanel) {
            if (HexMech.pxtoHex(mevt.getX(), mevt.getY()).equals(cursor) || mevt.getClickCount() == 2) {
                if (!shifting && !attacking) {
                    cursor.x = HexMech.pxtoHex(mevt.getX(), mevt.getY()).x;
                    cursor.y = HexMech.pxtoHex(mevt.getX(), mevt.getY()).y;
                        if (cursor.x > 0 && cursor.y > 0) {
                            if (LocationManager.getSize().x > cursor.x && LocationManager.getSize().y > cursor.y) {
                                if (LocationManager.getLoc(cursor) instanceof Factory) {
                                    mFactory = (Factory) LocationManager.getLoc(cursor);
                                    factory = true;
                                    factoryLoc = cursor;
                                    cursor = new Point(0, 0);
                                    gui.displayFactory((Factory) LocationManager.getLoc(factoryLoc));
                                }
                            }
                        }
                    return;
                }
                if (shifting) {
                    boolean validMove = false;
                    for (Point p : selectLocs) {
                        if (cursor.x == p.x && cursor.y == p.y) {
                            validMove = true;
                        }
                    }
                    if (!validMove) {
                        JOptionPane.showMessageDialog(gui, "Cannot Move Here");
                        return;
                    }
                    Location newLoc = LocationManager.getLoc(cursor);

                    if (factoryUnit == -1) {
                        Unit u = LocationManager.getLoc(unitLoc).getUnit();
                        LocationManager.getLoc(unitLoc).getUnit().move(newLoc);
                        shifting = false;
                        selectLocs.clear();
                        if (!(newLoc instanceof Factory)) {
                            attack();
                        }
                        u.attack();
                    } else {
                        ((Factory) LocationManager.getLoc(unitLoc)).getUnit(factoryUnit).move(newLoc);
                        factoryUnit = -1;
                        shifting = false;
                        selectLocs.clear();
                        Unit u = newLoc.getUnit();
                        mFactory.removeUnit(u);
                        u.attack();
                        military.engine.events.EventBus.getInstance().post(new military.engine.events.UnitMoved(u, new java.awt.Point(newLoc.getLoc())));
                    }
                    // Immediately update the UI to clear or update highlights after movement
                    controller.render(gui, turn, selectLocs, (buttonCursor.y == -1) ? cursor : buttonCursor);
                    try { gui.forceGridRepaint(); } catch (Exception ex) {
                        java.util.logging.Logger log = military.util.Logs.getLogger(Game.class);
                        log.fine("forceGridRepaint threw: " + ex.toString());
                    }
                    if (LocationManager.getLoc(cursor) instanceof Base && ((Base) newLoc).getTeam() != turn) {
                        JOptionPane.showMessageDialog(gui, "Player " + (turn ? "1" : "2") + " Wins!");
                        gui.dispose();
                        return;
                    }
                } else if (attacking) {
                    boolean validAttack = false;
                    for (Point p : selectLocs) {
                        if (cursor.x == p.x && cursor.y == p.y) {
                            validAttack = true;
                        }
                    }
                    if (!validAttack) {
                        JOptionPane.showMessageDialog(gui, "Cannot Attack Here");
                        return;
                    }
                    gui.displayCombat(new CombatStats(LocationManager.getLoc(unitLoc).getUnit(),
                            LocationManager.getLoc(cursor).getUnit()));
                    LocationManager.getLoc(unitLoc).getUnit().attack();
                    attacking = false;
                    selectLocs.clear();
                    if (LocationManager.getLoc(cursor).getUnit().getHealth() <= 0) {
                        UnitManager.getInstance().removeUnit(LocationManager.getLoc(cursor).getUnit());
                        LocationManager.getLoc(cursor).removeUnit();
                    }
                    if (LocationManager.getLoc(unitLoc).getUnit().getHealth() <= 0) {
                        UnitManager.getInstance().removeUnit(LocationManager.getLoc(unitLoc).getUnit());
                        LocationManager.getLoc(unitLoc).removeUnit();
                    }
                    if (unitRepo.getUnits(military.engine.Team.RED).isEmpty()) {
                        JOptionPane.showMessageDialog(gui, "Player 1 Wins!");
                        gui.dispose();
                        return;
                    }
                    if (unitRepo.getUnits(military.engine.Team.BLUE).isEmpty()) {
                        JOptionPane.showMessageDialog(gui, "Player 2 Wins!");
                        gui.dispose();
                        return;
                    }
                    InputEvent evt = GUIMiddleMan.getInstance().getEvent();
                    while ((evt instanceof KeyEvent) && ((KeyEvent) evt).getKeyCode() != 10) {
                        evt = GUIMiddleMan.getInstance().getEvent();
                    }
                }
                return;
            } else {
                Point newHex = HexMech.pxtoHex(mevt.getX(), mevt.getY());
                int x = newHex.x;
                int y = newHex.y;
                if (x < 1 || ((x % 2 == 0) && (y == 0))) { // Ensure click is within map bounds
                    return;
                }
                cursor = newHex;
                try {
                    gui.moveCursor(cursor);
                } catch (IndexOutOfBoundsException e) {
                    // Added June 18, 2023 to catch Index-1 out of bounds for length <num>
                    e.printStackTrace();
                }
                InputEvent evt = GUIMiddleMan.getInstance().getEvent();
                if (evt instanceof KeyEvent) {
                    break;
                }
                mevt = (MouseEvent) evt;
            }
        }
        if (mevt.getX() == -1) {
            buttonCursor = mevt.getPoint();
            if (mevt.getY() == 0) {
                shift();
            } else if (mevt.getY() == 1) {
                attack();
            } else if (mevt.getY() == 2) {
                info();
            } else if (mevt.getY() == 3) {
                end();
            }
        }

    }

    private final military.engine.UnitRepository unitRepo = military.engine.DefaultUnitRepository.getInstance();

    private void shift() {
        if (LocationManager.getLoc(cursor).isEmpty()) {
            JOptionPane.showMessageDialog(gui, "No Unit Present");
            return;
        }
        if (LocationManager.getLoc(cursor).getUnit().getTeam() != turn) {
            JOptionPane.showMessageDialog(gui, "Cannot Control this unit");
            return;
        }
        if (LocationManager.getLoc(cursor).getUnit().isShiftDone()) {
            JOptionPane.showMessageDialog(gui, "Unit already moved this turn");
            return;
        }
        shifting = true;
        buttonCursor.y = -1;
        unitLoc = new Point(cursor.x, cursor.y);

        Unit unit = LocationManager.getLoc(cursor).getUnit();
        java.util.List<Point> moves = military.engine.PathfindingService.computeMovesBfs(cursor, unit, turn);
        selectLocs.clear();
        selectLocs.addAll(moves);
        java.util.logging.Logger dbg = military.util.Logs.getLogger(Game.class);
        String debugMsg = "[DEBUG_LOG] shift(): selectLocs size=" + selectLocs.size() + ", cursor=" + cursor + ", thread=" + Thread.currentThread().getName();
        dbg.fine(debugMsg);
        // Force an immediate render so valid move hexes highlight without waiting for the next loop
        controller.render(gui, turn, selectLocs, (buttonCursor.y == -1) ? cursor : buttonCursor);
        // Attempt to force synchronous grid repaint for immediate visual feedback
        try { gui.forceGridRepaint(); } catch (Exception ex) {
            java.util.logging.Logger log = military.util.Logs.getLogger(Game.class);
            log.fine("forceGridRepaint threw: " + ex.toString());
        }
    }


    public void stepOnce(java.awt.event.InputEvent evtInitial) {
        java.awt.event.KeyEvent kevt;
        java.awt.event.MouseEvent mevt;
        java.awt.event.InputEvent evt = evtInitial;
        if (evt instanceof java.awt.event.KeyEvent) {
            kevt = (java.awt.event.KeyEvent) evt;
            if (buttonCursor.y != -1) {
                if (kevt.getKeyCode() == KEY_ARROW_UP || kevt.getKeyCode() == KEY_LETTER_W) {
                    buttonCursor.y--;
                    if (buttonCursor.y == -1) {
                        buttonCursor.y = 3;
                    }
                }
                if (kevt.getKeyCode() == KEY_ARROW_DOWN || kevt.getKeyCode() == KEY_LETTER_S) {
                    buttonCursor.y++;
                    if (buttonCursor.y == 4) {
                        buttonCursor.y = 0;
                    }
                }
                if (kevt.getKeyCode() == military.util.InputMappings.CTRL) {
                    if (buttonCursor.y == 0) {
                        shift();
                    } else if (buttonCursor.y == 1) {
                        attack();
                    } else if (buttonCursor.y == 2) {
                        info();
                    } else if (buttonCursor.y == 3) {
                        end();
                    }
                }
                if (kevt.getKeyCode() == KEY_SHIFT) {
                    buttonCursor.y = -1;
                }
                return;
            }
            while (kevt.getKeyCode() >= KEY_ARROW_LEFT && kevt.getKeyCode() <= KEY_ARROW_DOWN
                    || kevt.getKeyCode() >= KEY_LETTER_A && kevt.getKeyCode() <= KEY_LETTER_W) {
                if ((kevt.getKeyCode() == KEY_ARROW_UP || kevt.getKeyCode() == KEY_LETTER_W) && cursor.y != 0) {
                    cursor.y--;
                }
                if ((kevt.getKeyCode() == KEY_ARROW_DOWN || kevt.getKeyCode() == KEY_LETTER_S)
                        && (cursor.y < 2 || (!factory && cursor.y < LocationManager.getSize().y - 1))) {
                    cursor.y++;
                }
                if ((kevt.getKeyCode() == KEY_ARROW_LEFT || kevt.getKeyCode() == KEY_LETTER_A) && cursor.x != 1) {
                    cursor.x--;
                }
                if ((kevt.getKeyCode() == KEY_ARROW_RIGHT || kevt.getKeyCode() == KEY_LETTER_D)
                        && (cursor.x < 3 || (!factory && cursor.x < LocationManager.getSize().x - 1))) {
                    cursor.x++;
                }
                gui.moveCursor(cursor);
                evt = GUIMiddleMan.getInstance().getEvent();
                if (evt instanceof java.awt.event.MouseEvent) {
                    break;
                }
                kevt = (java.awt.event.KeyEvent) evt;
            }
            // Allow keyboard Shift to initiate movement highlighting directly
            if (kevt.getKeyCode() == military.util.InputMappings.SHIFT) {
                if (!shifting && !attacking && !factory) {
                    shift();
                    return;
                }
            }
            if (kevt.getKeyCode() == military.util.InputMappings.ENTER) {
                if (!shifting && !attacking && !factory) {
                    if (LocationManager.getLoc(cursor) instanceof Factory) {
                        mFactory = (Factory) LocationManager.getLoc(cursor);
                        factory = true;
                        factoryLoc = cursor;
                        cursor = new Point(0, 0);
                        gui.displayFactory((Factory) LocationManager.getLoc(factoryLoc));
                    } else {
                        buttonCursor.setLocation(-1, 0);
                    }
                    return;
                }
                if (shifting) {
                    boolean validMove = false;
                    for (Point p : selectLocs) {
                        if (cursor.x == p.x && cursor.y == p.y) {
                            validMove = true;
                        }
                    }
                    if (!validMove) {
                        javax.swing.JOptionPane.showMessageDialog(gui, "Cannot Move Here");
                        return;
                    }
                    Location newLoc = LocationManager.getLoc(cursor);
                    if (factoryUnit == -1) {
                        Unit u = LocationManager.getLoc(unitLoc).getUnit();
                        LocationManager.getLoc(unitLoc).getUnit().move(newLoc);
                        shifting = false;
                        selectLocs.clear();
                        if (!(newLoc instanceof Factory)) {
                            attack();
                        }
                        u.attack();
                    } else {
                        ((Factory) LocationManager.getLoc(unitLoc)).getUnit(factoryUnit).move(newLoc);
                        factoryUnit = -1;
                        shifting = false;
                        selectLocs.clear();
                        Unit u = newLoc.getUnit();
                        mFactory.removeUnit(u);
                        u.attack();
                        military.engine.events.EventBus.getInstance().post(new military.engine.events.UnitMoved(u, new java.awt.Point(newLoc.getLoc())));
                    }
                    if (LocationManager.getLoc(cursor) instanceof Base && ((Base) newLoc).getTeam() != turn) {
                        javax.swing.JOptionPane.showMessageDialog(gui, "Player " + (turn ? "1" : "2") + " Wins!");
                        gui.dispose();
                        return;
                    }

                } else if (attacking) {
                    boolean validAttack = false;
                    for (Point p : selectLocs) {
                        if (cursor.x == p.x && cursor.y == p.y) {
                            validAttack = true;
                        }
                    }
                    if (!validAttack) {
                        javax.swing.JOptionPane.showMessageDialog(gui, "Cannot Attack Here");
                        return;
                    }
                    gui.displayCombat(military.engine.CombatResolver.resolve(
                            LocationManager.getLoc(unitLoc).getUnit(),
                            LocationManager.getLoc(cursor).getUnit(),
                            LocationManager.getLoc(cursor)));
                    if (!military.engine.TurnRules.ALLOW_MOVE_AFTER_ATTACK) {
                        // Enforcement is already via attack() marking flags; this documents the rule usage
                    }
                    LocationManager.getLoc(unitLoc).getUnit().attack();
                    attacking = false;
                    selectLocs.clear();
                    if (LocationManager.getLoc(cursor).getUnit().getHealth() <= 0) {
                        UnitManager.getInstance().removeUnit(LocationManager.getLoc(cursor).getUnit());
                        LocationManager.getLoc(cursor).removeUnit();
                    }
                    if (LocationManager.getLoc(unitLoc).getUnit().getHealth() <= 0) {
                        UnitManager.getInstance().removeUnit(LocationManager.getLoc(unitLoc).getUnit());
                        LocationManager.getLoc(unitLoc).removeUnit();
                    }
                    if (UnitManager.getInstance().getUnits(false).isEmpty()) {
                        javax.swing.JOptionPane.showMessageDialog(gui, "Player 1 Wins!");
                        gui.dispose();
                        return;
                    }
                    if (UnitManager.getInstance().getUnits(true).isEmpty()) {
                        javax.swing.JOptionPane.showMessageDialog(gui, "Player 2 Wins!");
                        gui.dispose();
                        return;
                    }
                    try {
                        while ((GUIMiddleMan.getInstance().getEvent() instanceof java.awt.event.KeyEvent)
                                && ((java.awt.event.KeyEvent) GUIMiddleMan.getInstance().getEvent()).getKeyCode() != 10) {
                        }
                    } catch (ClassCastException e) {
                        // Added exception June 18, 2023 to catch casting MouseEvent to KeyEvent
                        e.printStackTrace();
                    }
                } else if (factory) {
                    if (mFactory.isControlled() && mFactory.getTeam() == turn) {
                        if (mFactory.getUnits().size() > cursor.x + (4 * cursor.y)) {
                            if (!mFactory.getUnit(cursor.x + (4 * cursor.y)).isAttackDone()) {
                                shifting = true;
                                selectLocs.clear();
                                for (Location loc : mFactory.getAdjacent()) {
                                    if (loc.getTerrain() < 40 && loc.getTerrain() != -1) {
                                        selectLocs.add(loc.getLoc());
                                    }
                                }
                                factoryUnit = cursor.x + (4 * cursor.y);
                                unitLoc = factoryLoc;
                                cursor = new Point(factoryLoc);
                                factory = false;
                            }
                        }
                    }
                }
            }
            if (kevt.getKeyCode() == military.util.InputMappings.ESCAPE) {
                if (shifting) {
                    shifting = false;
                    selectLocs.clear();
                }
                if (attacking) {
                    attacking = false;
                    selectLocs.clear();
                }
                if (factory) {
                    factory = false;
                    factoryUnit = -1;
                    cursor = new Point(factoryLoc);
                }
            }
        }
        if (!(evt instanceof java.awt.event.MouseEvent)) {
            return;
        }
        mevt = (java.awt.event.MouseEvent) evt;
        while (mevt.getComponent() instanceof military.gui.HexGridPanel) {
            if (military.gui.HexMech.pxtoHex(mevt.getX(), mevt.getY()).equals(cursor) || mevt.getClickCount() == 2) {
                if (!shifting && !attacking) {
                    cursor.x = military.gui.HexMech.pxtoHex(mevt.getX(), mevt.getY()).x;
                    cursor.y = military.gui.HexMech.pxtoHex(mevt.getX(), mevt.getY()).y;
                        if (cursor.x > 0 && cursor.y > 0) {
                            if (LocationManager.getSize().x > cursor.x && LocationManager.getSize().y > cursor.y) {
                                gui.moveCursor(cursor);
                                if (mevt.getClickCount() == 2) {
                                    if (!LocationManager.getLoc(cursor).isEmpty()) {
                                        buttonCursor.setLocation(-1, 0);
                                        break;
                                    }
                                }
                            }
                        }
                } else if (shifting) {
                    if (mevt.getClickCount() == 2) {
                        boolean validMove = false;
                        for (Point p : selectLocs) {
                            if (cursor.x == p.x && cursor.y == p.y) {
                                validMove = true;
                            }
                        }
                        if (!validMove) {
                            javax.swing.JOptionPane.showMessageDialog(gui, "Cannot Move Here");
                            return;
                        }
                        military.engine.Location newLoc = LocationManager.getLoc(cursor);
                        if (factoryUnit == -1) {
                            Unit u = LocationManager.getLoc(unitLoc).getUnit();
                            LocationManager.getLoc(unitLoc).getUnit().move(newLoc);
                            shifting = false;
                            selectLocs.clear();
                            if (!(newLoc instanceof Factory)) {
                                attack();
                            }
                            u.attack();
                        } else {
                            ((Factory) LocationManager.getLoc(unitLoc)).getUnit(factoryUnit).move(newLoc);
                            factoryUnit = -1;
                            shifting = false;
                            selectLocs.clear();
                            Unit u = newLoc.getUnit();
                            mFactory.removeUnit(u);
                            u.attack();
                        }
                        if (LocationManager.getLoc(cursor) instanceof Base && ((Base) newLoc).getTeam() != turn) {
                            javax.swing.JOptionPane.showMessageDialog(gui, "Player " + (turn ? "1" : "2") + " Wins!");
                            gui.dispose();
                            return;
                        }
                    }
                } else if (attacking && mevt.getClickCount() == 2) {
                    boolean validAttack = false;
                    for (Point p : selectLocs) {
                        if (cursor.x == p.x && cursor.y == p.y) {
                            validAttack = true;
                        }
                    }
                    if (!validAttack) {
                        javax.swing.JOptionPane.showMessageDialog(gui, "Cannot Attack Here");
                        return;
                    }
                    gui.displayCombat(military.engine.CombatResolver.resolve(
                            LocationManager.getLoc(unitLoc).getUnit(),
                            LocationManager.getLoc(cursor).getUnit(),
                            LocationManager.getLoc(cursor)));
                    if (!military.engine.TurnRules.ALLOW_MOVE_AFTER_ATTACK) {
                        // Enforcement is already via attack() marking flags; this documents the rule usage
                    }
                    LocationManager.getLoc(unitLoc).getUnit().attack();
                    attacking = false;
                    selectLocs.clear();
                    if (LocationManager.getLoc(cursor).getUnit().getHealth() <= 0) {
                        UnitManager.getInstance().removeUnit(LocationManager.getLoc(cursor).getUnit());
                        LocationManager.getLoc(cursor).removeUnit();
                    }
                    if (LocationManager.getLoc(unitLoc).getUnit().getHealth() <= 0) {
                        UnitManager.getInstance().removeUnit(LocationManager.getLoc(unitLoc).getUnit());
                        LocationManager.getLoc(unitLoc).removeUnit();
                    }
                    if (UnitManager.getInstance().getUnits(false).isEmpty()) {
                        javax.swing.JOptionPane.showMessageDialog(gui, "Player 1 Wins!");
                        gui.dispose();
                        return;
                    }
                    if (UnitManager.getInstance().getUnits(true).isEmpty()) {
                        javax.swing.JOptionPane.showMessageDialog(gui, "Player 2 Wins!");
                        gui.dispose();
                        return;
                    }
                    try {
                        while ((GUIMiddleMan.getInstance().getEvent() instanceof java.awt.event.KeyEvent)
                                && ((java.awt.event.KeyEvent) GUIMiddleMan.getInstance().getEvent()).getKeyCode() != 10) {
                        }
                    } catch (ClassCastException e) {
                        // Added exception June 18, 2023 to catch casting MouseEvent to KeyEvent
                        e.printStackTrace();
                    }
                }
            }
            java.awt.Point newHex = military.gui.HexMech.pxtoHex(mevt.getX(), mevt.getY());
            int x = newHex.x;
            int y = newHex.y;
            if (x < 1 || ((x % 2 == 0) && (y == 0))) { // Ensure click is within map bounds
                return;
            }
            cursor = newHex;
            try {
                gui.moveCursor(cursor);
            } catch (IndexOutOfBoundsException e) {
                // Added June 18, 2023 to catch Index-1 out of bounds for length <num>
                e.printStackTrace();
            }
            evt = GUIMiddleMan.getInstance().getEvent();
            if (evt instanceof java.awt.event.KeyEvent) {
                break;
            }
            mevt = (java.awt.event.MouseEvent) evt;
        }
        if (mevt.getX() == -1) {
            buttonCursor = mevt.getPoint();
            if (mevt.getY() == 0) {
                shift();
            } else if (mevt.getY() == 1) {
                attack();
            } else if (mevt.getY() == 2) {
                info();
            } else if (mevt.getY() == 3) {
                end();
            }
        }
    }

    private void attack() {
        if (LocationManager.getLoc(cursor).isEmpty()) {
            JOptionPane.showMessageDialog(gui, "No Unit Present");
            return;
        }
        if (LocationManager.getLoc(cursor).getUnit().getTeam() != turn) {
            JOptionPane.showMessageDialog(gui, "Cannot Control this unit");
            return;
        }
        if (LocationManager.getLoc(cursor).getUnit().isAttackDone()) {
            JOptionPane.showMessageDialog(gui, "Unit already attacked this turn");
            return;
        }
        buttonCursor.y = -1;

        Unit attacker = LocationManager.getLoc(cursor).getUnit();
        if (attacker.isRanged()) {
            for (Location loc : LocationManager.getLoc(cursor).getAdjacent()) {
                rangedIterative(loc, attacker.getRange() - 1);
            }
        } else {
            for (Location loc : LocationManager.getLoc(cursor).getAdjacent()) {
                if (!loc.isEmpty()) {
                    Unit defender = loc.getUnit();
                    if (defender.getTeam() != turn) {
                        selectLocs.add(new Point(loc.getLoc()));
                    }
                }
            }
        }
        if (selectLocs.isEmpty()) {
            // TODO: Surface via GUI bottom panel message
            java.util.logging.Logger logger = military.util.Logs.getLogger(Game.class);
            logger.info("No attacks available");
            return;
        }
        attacking = true;
        unitLoc = new Point(cursor.x, cursor.y);
    }

    private void rangedIterative(Location start, int attackLeft) {
        class Node { Location loc; int depth; Node(Location l,int d){loc=l;depth=d;} }
        java.util.ArrayDeque<Node> stack = new java.util.ArrayDeque<>();
        stack.push(new Node(start, attackLeft));
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            for (Location loc : node.loc.getAdjacent()) {
                if (!loc.isEmpty()) {
                    Unit defender = loc.getUnit();
                    if (defender.getTeam() != turn) {
                        selectLocs.add(new Point(loc.getLoc()));
                    }
                }
                if (node.depth > 1) {
                    stack.push(new Node(loc, node.depth - 1));
                }
            }
        }
    }

    private void info() {
        java.util.logging.Logger logger = military.util.Logs.getLogger(Game.class);
        logger.info("Info requested");
    }

    private void displayCombatAndPost(CombatStats stats) {
        gui.displayCombat(stats);
        military.engine.events.EventBus.getInstance().post(new military.engine.events.CombatResolved(stats));
    }

    private void announceTurn(boolean whoseTurn) {
        try {
            if (java.awt.GraphicsEnvironment.isHeadless()) {
                return; // do nothing in headless environments (tests/CI)
            }
            String playerLabel = whoseTurn ? "Player 1" : "Player 2";
            String teamLabel = whoseTurn ? "Blue" : "Red";
            javax.swing.JOptionPane.showMessageDialog(
                    gui,
                    playerLabel + " (" + teamLabel + ") turn",
                    "Turn Started",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            // Swallow any UI exceptions to avoid blocking gameplay
        }
    }

    private boolean confirmEndTurn() {
        try {
            if (java.awt.GraphicsEnvironment.isHeadless()) {
                return true; // auto-confirm in headless environments (e.g., tests/CI)
            }
            Object[] options = {"Yes", "Cancel"};
            int choice = javax.swing.JOptionPane.showOptionDialog(
                    gui,
                    "Are you sure you want to end your turn?",
                    "End Turn",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]
            );
            return choice == 0; // 0 -> "Yes"
        } catch (Exception ex) {
            // Fail-open to avoid blocking gameplay if dialog can't be shown
            return true;
        }
    }

    private void end() {
        if (!confirmEndTurn()) {
            return;
        }
        turn = !turn;
        unitRepo.resetUnits();
        buttonCursor.y = -1;
        if (turn) {
            gui.incrementTurn();
        }
        // Post domain event for turn switch
        military.engine.events.EventBus.getInstance().post(new military.engine.events.TurnStarted(turn ? military.engine.Team.BLUE : military.engine.Team.RED));
        // Announce the new active player
        announceTurn(turn);
        controller.render(gui, turn, selectLocs, cursor);
    }
}
