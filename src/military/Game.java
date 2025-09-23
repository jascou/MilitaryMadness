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
    private String currentMapName;

    // Initial step toward decoupling UI and game state
    private final military.engine.GameState state;
    private final military.engine.GameController controller;

    public Game(String levelName) {
        new military.engine.DefaultMapService().loadMap(levelName);
        this.currentMapName = levelName;
        gui = new GUI(levelName);
        // Provide GUI with controller actions (dependency inversion)
        gui.setActions(new military.gui.GUI.GameActions() {
            @Override public void onMove() { shift(); }
            @Override public void onAttack() { attack(); }
            @Override public void onInfo() { info(); }
            @Override public void onEndTurn() { end(); }
            @Override public void onSave(String name) { saveGame(name); }
            @Override public void onLoad(String name) { loadGame(name); }
            @Override public void onExit() {
                try { if (gui != null) gui.dispose(); } catch (Throwable ignored) {}
                System.exit(0);
            }
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

    // Convenience API: save the current game state. Persists map and metadata.
    public void saveGame(String saveName) {
        military.engine.SaveLoadService.save(saveName, this.currentMapName, this.state);
    }

    // Convenience API: load a game state and update in-memory fields.
    public void loadGame(String saveName) {
        military.engine.SaveGame data = military.engine.SaveLoadService.load(saveName);
        this.currentMapName = data.getMapName();
        this.turn = data.isTurn();
        this.cursor = data.getCursor();
        this.buttonCursor = new java.awt.Point(-1, -1);
        this.selectLocs.clear();
        // propagate to shared state for rendering
        this.state.setTurn(this.turn);
        this.state.setCursor(new java.awt.Point(this.cursor));
        // Optional: announce current turn after load
        announceTurn(this.turn);
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
                        // Clicking/confirming on own hex: treat as move completion, clear highlights
                        if (cursor.equals(unitLoc)) {
                            attacking = false;
                            selectLocs.clear();
                            controller.render(gui, turn, selectLocs, (buttonCursor.y == -1) ? cursor : buttonCursor);
                            try { gui.forceGridRepaint(); } catch (Exception ex) {
                                java.util.logging.Logger log = military.util.Logs.getLogger(Game.class);
                                log.fine("forceGridRepaint threw: " + ex.toString());
                            }
                            continue;
                        }
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
                    // New behavior: clicking the unit's current location acts as "complete move" and clears highlights
                    if (cursor.equals(unitLoc)) {
                        attacking = false;
                        selectLocs.clear();
                        controller.render(gui, turn, selectLocs, (buttonCursor.y == -1) ? cursor : buttonCursor);
                        try { gui.forceGridRepaint(); } catch (Exception ex) {
                            java.util.logging.Logger log = military.util.Logs.getLogger(Game.class);
                            log.fine("forceGridRepaint threw: " + ex.toString());
                        }
                        return;
                    }
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
                    Unit attacker = LocationManager.getLoc(unitLoc).getUnit();
                    Unit defender = LocationManager.getLoc(cursor).getUnit();
                    gui.displayCombat(new CombatStats(attacker, defender));
                    if (attacker != null) attacker.attack();
                    attacking = false;
                    selectLocs.clear();
                    if (defender != null && defender.getHealth() <= 0) {
                        UnitManager.getInstance().removeUnit(defender);
                        LocationManager.getLoc(cursor).removeUnit();
                    }
                    if (attacker != null && attacker.getHealth() <= 0) {
                        UnitManager.getInstance().removeUnit(attacker);
                        LocationManager.getLoc(unitLoc).removeUnit();
                        attacker = null;
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
                    // If attacker is eligible to move after attacking, re-enter shifting mode
                    if (attacker != null && attacker.canMoveAfterAttack() && !attacker.isShiftDone()) {
                        shifting = true;
                        unitLoc = new Point(attacker.getLoc().getLoc());
                        cursor = new Point(unitLoc);
                        java.util.List<Point> moves = military.engine.PathfindingService.computeMovesBfs(unitLoc, attacker, turn);
                        selectLocs.clear();
                        selectLocs.addAll(moves);
                        controller.render(gui, turn, selectLocs, (buttonCursor.y == -1) ? cursor : buttonCursor);
                        try { gui.forceGridRepaint(); } catch (Exception ex) { }
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
        // Special rule: units that can move after attack (e.g., Rabbit) cannot take the second move until they have attacked
        Unit unit = LocationManager.getLoc(cursor).getUnit();
        if (unit.getMaxMovesPerTurn() > 1 && unit.canMoveAfterAttack() && unit.getMovesUsedThisTurn() >= 1 && !unit.isAttackDone()) {
            // Block second move until an attack has been performed
            JOptionPane.showMessageDialog(gui, "Must attack before moving again");
            return;
        }
        shifting = true;
        buttonCursor.y = -1;
        unitLoc = new Point(cursor.x, cursor.y);

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
                    // Clicking on own hex: treat as move completion, clear highlights
                    if (cursor.equals(unitLoc)) {
                        attacking = false;
                        selectLocs.clear();
                        controller.render(gui, turn, selectLocs, (buttonCursor.y == -1) ? cursor : buttonCursor);
                        try { gui.forceGridRepaint(); } catch (Exception ex) {
                            java.util.logging.Logger log = military.util.Logs.getLogger(Game.class);
                            log.fine("forceGridRepaint threw: " + ex.toString());
                        }
                        return;
                    }
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
                    military.engine.CombatStats stats = military.engine.CombatResolver.resolve(
                            LocationManager.getLoc(unitLoc).getUnit(),
                            LocationManager.getLoc(cursor).getUnit(),
                            LocationManager.getLoc(cursor));
                    gui.displayCombat(stats);
                    Unit attacker = LocationManager.getLoc(unitLoc).getUnit();
                    Unit defender = LocationManager.getLoc(cursor).getUnit();
                    if (attacker != null) attacker.attack();
                    attacking = false;
                    selectLocs.clear();
                    if (defender != null && defender.getHealth() <= 0) {
                        UnitManager.getInstance().removeUnit(defender);
                        LocationManager.getLoc(cursor).removeUnit();
                    }
                    if (attacker != null && attacker.getHealth() <= 0) {
                        UnitManager.getInstance().removeUnit(attacker);
                        LocationManager.getLoc(unitLoc).removeUnit();
                        attacker = null;
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
                    // Rabbit-like capability: re-enter shifting after attack if allowed
                    if (attacker != null && attacker.canMoveAfterAttack() && !attacker.isShiftDone()) {
                        shifting = true;
                        unitLoc = new Point(attacker.getLoc().getLoc());
                        cursor = new Point(unitLoc);
                        java.util.List<Point> moves = military.engine.PathfindingService.computeMovesBfs(unitLoc, attacker, turn);
                        selectLocs.clear();
                        selectLocs.addAll(moves);
                        controller.render(gui, turn, selectLocs, (buttonCursor.y == -1) ? cursor : buttonCursor);
                        try { gui.forceGridRepaint(); } catch (Exception ex) { }
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
            // Already attacked this turn: do not enter attack mode (silently ignore)
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
        try {
            if (java.awt.GraphicsEnvironment.isHeadless()) {
                return; // avoid dialogs in headless mode
            }
            if (LocationManager.getLoc(cursor).isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(gui, "No Unit Present");
                return;
            }
            Unit u = LocationManager.getLoc(cursor).getUnit();
            // Build a simple info panel with image and stats
            javax.swing.JPanel panel = new javax.swing.JPanel();
            panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.X_AXIS));

            // Image on the left
            try {
                military.gui.Model model = new military.gui.Model(u.getModelName());
                java.awt.Image img = model.getImage(u.getTeam());
                if (img != null) {
                    // scale to a reasonable size
                    int w = img.getWidth(null);
                    int h = img.getHeight(null);
                    if (w > 0 && h > 0) {
                        int maxW = 160, maxH = 160;
                        double scale = Math.min((double) maxW / w, (double) maxH / h);
                        if (scale < 1.0) {
                            int nw = (int) Math.max(1, Math.round(w * scale));
                            int nh = (int) Math.max(1, Math.round(h * scale));
                            java.awt.Image scaled = img.getScaledInstance(nw, nh, java.awt.Image.SCALE_SMOOTH);
                            panel.add(new javax.swing.JLabel(new javax.swing.ImageIcon(scaled)));
                        } else {
                            panel.add(new javax.swing.JLabel(new javax.swing.ImageIcon(img)));
                        }
                    }
                }
            } catch (Exception ex) {
                // ignore image issues; text will still show
            }

            // Text on the right
            javax.swing.JPanel text = new javax.swing.JPanel();
            text.setLayout(new javax.swing.BoxLayout(text, javax.swing.BoxLayout.Y_AXIS));
            String teamStr = u.getTeam() ? "Blue" : "Red";
            text.add(new javax.swing.JLabel("Name: " + u.getName()));
            text.add(new javax.swing.JLabel("Type: " + u.getType()));
            text.add(new javax.swing.JLabel("Team: " + teamStr));
            text.add(new javax.swing.JLabel("Health: " + u.getHealth()));
            text.add(new javax.swing.JLabel("EXP: " + u.getExp()));
            text.add(new javax.swing.JLabel("Land Attack: " + u.getLandAttack()));
            text.add(new javax.swing.JLabel("Air Attack: " + u.getAirAttack()));
            text.add(new javax.swing.JLabel("Defense: " + u.getDefense()));
            text.add(new javax.swing.JLabel("Range: " + u.getRange() + (u.isRanged() ? " (Ranged)" : "")));
            text.add(new javax.swing.JLabel("Move (Shift): " + u.getShift()));
            // Capabilities
            String caps = "";
            if (u.canMoveAfterAttack()) caps += "Move after attack; ";
            if (u.getMaxMovesPerTurn() > 1) caps += "Moves per turn: " + u.getMaxMovesPerTurn() + "; ";
            if (!caps.isEmpty()) {
                text.add(new javax.swing.JLabel("Special: " + caps));
            }
            panel.add(javax.swing.Box.createHorizontalStrut(12));
            panel.add(text);

            javax.swing.JOptionPane.showMessageDialog(gui, panel, "Unit Info", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            // Swallow to avoid breaking gameplay if UI fails
        }
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

    /**
     * Programmatic end-turn entry point for automation/AI.
     * Safe to call from non-EDT threads; rendering will be scheduled appropriately by controller.
     */
    public void endTurnForAutomation() {
        end();
    }

    /**
     * Programmatic movement for AI/automation. Validates legality similarly to user input flow
     * but without dialogs. Returns true if the move was applied.
     */
    public boolean moveUnitForAutomation(java.awt.Point from, java.awt.Point to) {
        try {
            if (from == null || to == null) return false;
            if (!LocationManager.isInBounds(from.x, from.y) || !LocationManager.isInBounds(to.x, to.y)) return false;
            if (LocationManager.getLoc(from).isEmpty()) return false;
            Unit u = LocationManager.getLoc(from).getUnit();
            if (u.getTeam() != turn) return false; // not this unit's turn
            if (u.isShiftDone()) return false; // already moved
            // Validate reachable
            java.util.List<java.awt.Point> moves = military.engine.PathfindingService.computeMovesBfs(from, u, turn);
            boolean ok = false;
            for (java.awt.Point p : moves) { if (p.equals(to)) { ok = true; break; } }
            if (!ok) return false;
            // Destination must be unoccupied (PathfindingService should ensure, but double-check)
            if (!LocationManager.getLoc(to).isEmpty()) return false;
            // Apply move
            LocationManager.getLoc(from).getUnit().move(LocationManager.getLoc(to));
            // Clear selections and re-render
            selectLocs.clear();
            cursor = new java.awt.Point(to);
            controller.render(gui, turn, selectLocs, (buttonCursor.y == -1) ? cursor : buttonCursor);
            try { gui.forceGridRepaint(); } catch (Exception ex) { }
            // If moved onto enemy base, announce win and dispose
            if (LocationManager.getLoc(to) instanceof Base && ((Base) LocationManager.getLoc(to)).getTeam() != turn) {
                try { javax.swing.JOptionPane.showMessageDialog(gui, "Player " + (turn ? "1" : "2") + " Wins!"); } catch (Exception ignore) {}
                try { gui.dispose(); } catch (Exception ignore) {}
            }
            // Mirror user flow: after moving, unit cannot attack (legacy rule)
            u.attack();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Programmatic attack for AI/automation. Validates basic legality and resolves combat
     * without showing blocking dialogs. Returns true if an attack was executed.
     */
    public boolean attackForAutomation(java.awt.Point attackerAt, java.awt.Point targetAt) {
        try {
            if (attackerAt == null || targetAt == null) return false;
            if (!LocationManager.isInBounds(attackerAt.x, attackerAt.y) || !LocationManager.isInBounds(targetAt.x, targetAt.y)) return false;
            if (LocationManager.getLoc(attackerAt).isEmpty()) return false;
            Unit attacker = LocationManager.getLoc(attackerAt).getUnit();
            if (attacker.getTeam() != turn) return false;
            if (attacker.isAttackDone()) return false;
            // Build list of valid targets as in attack()
            java.util.ArrayList<java.awt.Point> valid = new java.util.ArrayList<>();
            if (attacker.isRanged()) {
                for (Location loc : LocationManager.getLoc(attackerAt).getAdjacent()) {
                    rangedIterative(loc, attacker.getRange() - 1);
                }
                valid.addAll(selectLocs);
            } else {
                for (Location loc : LocationManager.getLoc(attackerAt).getAdjacent()) {
                    if (!loc.isEmpty() && loc.getUnit().getTeam() != turn) {
                        valid.add(new java.awt.Point(loc.getLoc()));
                    }
                }
            }
            boolean ok = false;
            for (java.awt.Point p : valid) { if (p.equals(targetAt)) { ok = true; break; } }
            if (!ok) return false;
            // Resolve combat
            military.engine.CombatStats stats = military.engine.CombatResolver.resolve(
                    attacker, LocationManager.getLoc(targetAt).getUnit(), LocationManager.getLoc(targetAt));
            displayCombatAndPost(stats);
            attacker.attack();
            // Remove dead units
            if (LocationManager.getLoc(targetAt).getUnit().getHealth() <= 0) {
                UnitManager.getInstance().removeUnit(LocationManager.getLoc(targetAt).getUnit());
                LocationManager.getLoc(targetAt).removeUnit();
            }
            if (LocationManager.getLoc(attackerAt).isEmpty() || LocationManager.getLoc(attackerAt).getUnit().getHealth() <= 0) {
                if (!LocationManager.getLoc(attackerAt).isEmpty()) {
                    UnitManager.getInstance().removeUnit(LocationManager.getLoc(attackerAt).getUnit());
                    LocationManager.getLoc(attackerAt).removeUnit();
                }
            }
            // Clear selections and re-render
            selectLocs.clear();
            controller.render(gui, turn, selectLocs, (buttonCursor.y == -1) ? cursor : buttonCursor);
            try { gui.forceGridRepaint(); } catch (Exception ex) { }
            // Victory checks
            if (unitRepo.getUnits(military.engine.Team.RED).isEmpty()) {
                try { javax.swing.JOptionPane.showMessageDialog(gui, "Player 1 Wins!"); } catch (Exception ignore) {}
                try { gui.dispose(); } catch (Exception ignore) {}
            }
            if (unitRepo.getUnits(military.engine.Team.BLUE).isEmpty()) {
                try { javax.swing.JOptionPane.showMessageDialog(gui, "Player 2 Wins!"); } catch (Exception ignore) {}
                try { gui.dispose(); } catch (Exception ignore) {}
            }
            return true;
        } catch (Throwable t) {
            return false;
        } finally {
            // Reset any temporary selectLocs built by rangedIterative
            selectLocs.clear();
        }
    }
}
