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
 *
 * @author Nate
 */


public class Game implements Runnable {

    final int KEY_SHIFT         = 16;
    final int KEY_CTRL          = 17;
    final int KEY_SPACE_BAR     = 32;
    final int KEY_ARROW_LEFT    = 37;
    final int KEY_ARROW_UP      = 38;
    final int KEY_ARROW_RIGHT   = 39;
    final int KEY_ARROW_DOWN    = 40;
    final int KEY_LETTER_A      = 65;
    final int KEY_LETTER_D      = 68;
    final int KEY_LETTER_S      = 83;
    final int KEY_LETTER_W      = 87;

    final int MOUSE_LEFT_BTN    = 1;
    final int MOUSE_MIDDLE_BTN  = 3;
    final int MOUSE_RIGHT_BTN   = 2;


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

    public Game(String levelName) {
        LocationManager.loadMap(levelName);
        gui = new GUI(levelName);
        turn = true;
        cursor = new Point(0, 0);
        buttonCursor = new Point(-1, -1);
        selectLocs = new ArrayList<>();
        factoryUnit = -1;
    }

    @Override
    public void run() {
        while (!LocationManager.isCaptured(!turn)) {
            if (!factory) {
                gui.render(turn, selectLocs, (buttonCursor.y == -1) ? cursor : buttonCursor);
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
                    if (kevt.getKeyCode() == KEY_CTRL) {      // Changed from 10 (June 16, '23)
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
                while (kevt.getKeyCode() >= KEY_ARROW_LEFT && kevt.getKeyCode() <= KEY_ARROW_DOWN || kevt.getKeyCode() >= KEY_LETTER_A && kevt.getKeyCode() <= KEY_LETTER_W) {
                    if ((kevt.getKeyCode() == KEY_ARROW_UP || kevt.getKeyCode() == KEY_LETTER_W) && cursor.y != 0) {
                        cursor.y--;
                    }
                    if ((kevt.getKeyCode() == KEY_ARROW_DOWN || kevt.getKeyCode() == KEY_LETTER_S)
                            && (cursor.y < 2 || (!factory && cursor.y < LocationManager.getSize().y - 1))) {
                        cursor.y++;
                    }
                    if ((kevt.getKeyCode() == KEY_ARROW_LEFT || kevt.getKeyCode() == KEY_LETTER_A) && cursor.x != 0) {
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
                if (kevt.getKeyCode() == 10) {
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
                        if (UnitManager.getInstance().getUnits(false).isEmpty()) {
                            JOptionPane.showMessageDialog(gui, "Player 1 Wins!");
                            gui.dispose();
                            return;
                        }
                        if (UnitManager.getInstance().getUnits(true).isEmpty()) {
                            JOptionPane.showMessageDialog(gui, "Player 2 Wins!");
                            gui.dispose();
                            return;
                        }
                        while ((GUIMiddleMan.getInstance().getEvent() instanceof KeyEvent)
                                && ((KeyEvent) GUIMiddleMan.getInstance().getEvent()).getKeyCode() != 10) {
                        }
                    } else if (factory) {
                        if (mFactory.isControlled() && mFactory.getTeam() == turn) {
                            if (mFactory.getUnits().size() > cursor.x + (4 * cursor.y)) {
                                if (!mFactory.getUnit(cursor.x + (4 * cursor.y)).isAttackDone()) {
                                    shifting = true;
                                    selectLocs.clear();
                                    for (Location loc : mFactory.getAjacent()) {
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
                if (kevt.getKeyCode() == 16) {
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
                    if (LocationManager.getLoc(cursor) instanceof Factory) {
                        mFactory = (Factory) LocationManager.getLoc(cursor);
                        factory = true;
                        factoryLoc = cursor;
                        cursor = new Point(0, 0);
                        gui.displayFactory((Factory) LocationManager.getLoc(factoryLoc));
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
                    if (UnitManager.getInstance().getUnits(false).isEmpty()) {
                        JOptionPane.showMessageDialog(gui, "Player 1 Wins!");
                        gui.dispose();
                        return;
                    }
                    if (UnitManager.getInstance().getUnits(true).isEmpty()) {
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
                cursor = HexMech.pxtoHex(mevt.getX(), mevt.getY());
                gui.moveCursor(cursor);
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
            JOptionPane.showMessageDialog(gui, "Unit alread moved this turn");
            return;
        }
        shifting = true;
        buttonCursor.y = -1;
        unitLoc = new Point(cursor.x, cursor.y);

        int movesLeftAtPoint[][] = new int[LocationManager.getSize().x][LocationManager.getSize().y];
        for (int i = 0; i < movesLeftAtPoint.length; i++) {
            for (int j = 0; j < movesLeftAtPoint[0].length; j++) {
                movesLeftAtPoint[i][j] = -1;
            }
        }
        Unit unit = LocationManager.getLoc(cursor).getUnit();
        movesLeftAtPoint[cursor.x][cursor.y] = LocationManager.getLoc(cursor).getUnit().getShift();
        movesRecur(cursor, movesLeftAtPoint, unit);
        selectLocs.clear();
        for (int i = 0; i < movesLeftAtPoint.length; i++) {
            for (int j = 0; j < movesLeftAtPoint[0].length; j++) {
                if (movesLeftAtPoint[i][j] > -1) {
                    selectLocs.add(new Point(i, j));
                }
            }
        }
    }

    private void movesRecur(Point p, int[][] movesLeftAtPoint, Unit unit) {
        Location locs[] = LocationManager.getLoc(p).getAjacent();
        for (Location loc : locs) {
            if (loc.getTerrain() == -1 || (!loc.isEmpty() && loc.getUnit().getTeam() != turn)) {
                continue;
            }
            int terrain = loc.getTerrain() / 10;
            if (terrain == 0 || unit.isAir()) {
                terrain = 1;
            }
            int newMovesLeft = movesLeftAtPoint[p.x][p.y] - terrain;
            boolean flanked = false;
            for (Location flank : loc.getAjacent()) {
                if (!flank.isEmpty() && flank.getUnit().getTeam() != turn) {
                    flanked = true;
                }
            }
            if (newMovesLeft > movesLeftAtPoint[loc.getLoc().x][loc.getLoc().y]
                    && (unit.getType().equals("Infantry") || (!(loc instanceof Base) && terrain != 4))) {
                if (!((loc instanceof Factory) && ((Factory) loc).getTeam() != unit.getTeam() && !unit.getType().equals("Infantry"))) {
                    movesLeftAtPoint[loc.getLoc().x][loc.getLoc().y] = newMovesLeft;
                    if (!flanked) {
                        movesRecur(loc.getLoc(), movesLeftAtPoint, unit);
                    }
                }

            }
        }
        if (!LocationManager.getLoc(p).isEmpty()) {
            movesLeftAtPoint[p.x][p.y] = -1;
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
            for (Location loc : LocationManager.getLoc(cursor).getAjacent()) {
                rangedRecur(loc, attacker.getRange() - 1);
            }
        } else {
            for (Location loc : LocationManager.getLoc(cursor).getAjacent()) {
                if (!loc.isEmpty()) {
                    Unit defender = loc.getUnit();
                    if (defender.getTeam() != turn) {
                        selectLocs.add(new Point(loc.getLoc()));
                    }

                }
            }
        }
        if (selectLocs.isEmpty()) {
            JOptionPane.showMessageDialog(gui, "No attacks available");
            return;
        }
        attacking = true;
        unitLoc = new Point(cursor.x, cursor.y);
    }

    private void rangedRecur(Location center, int attackLeft) {
        for (Location loc : center.getAjacent()) {
            if (!loc.isEmpty()) {
                Unit defender = loc.getUnit();
                if (defender.getTeam() != turn) {
                    selectLocs.add(new Point(loc.getLoc()));
                }

            }
            if (attackLeft > 1) {
                rangedRecur(loc, attackLeft - 1);
            }
        }
    }

    private void info() {
        System.out.println("Info");
    }

    private void end() {
        turn = !turn;
        UnitManager.getInstance().resetUnits();
        buttonCursor.y = -1;
        if (turn) {
            gui.incrementTurn();
        }
        gui.render(turn, selectLocs, cursor);
    }
}
