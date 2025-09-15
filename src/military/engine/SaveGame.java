package military.engine;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Serializable save-game metadata capturing minimal game progress required to resume play.
 * The actual map (tiles and units) is persisted as a standard map file via MapService.saveMap().
 */
public class SaveGame implements Serializable {
    private static final long serialVersionUID = 2L; // keep for backward compatibility; added fields are optional

    private String mapName;       // map filename (without extension) where current map state was saved
    private boolean turn;         // true = Player1 (Blue), false = Player2 (Red)
    private int cursorX;          // cursor location X
    private int cursorY;          // cursor location Y

    /**
     * Optional per-unit transient state captured at save time (may be null for older saves).
     */
    private List<UnitTurnState> unitStates;

    // --- AI settings (optional; may be null/unused for older saves) ---
    private Boolean aiEnabled;               // null means unknown (older save)
    private Team aiTeam;                     // which team is controlled by AI when enabled
    private Long aiSeed;                     // deterministic RNG seed; may be null
    private Double aiAggressiveness;         // difficulty/strategy knobs
    private Double aiCaution;
    private Double aiCapturePriority;

    public SaveGame() {}

    public SaveGame(String mapName, boolean turn, Point cursor) {
        this.mapName = mapName;
        this.turn = turn;
        if (cursor == null) cursor = new Point(0,0);
        this.cursorX = cursor.x;
        this.cursorY = cursor.y;
    }

    public String getMapName() { return mapName; }
    public boolean isTurn() { return turn; }
    public Point getCursor() { return new Point(cursorX, cursorY); }

    public List<UnitTurnState> getUnitStates() {
        if (unitStates == null) return Collections.emptyList();
        return Collections.unmodifiableList(unitStates);
    }

    public void setUnitStates(List<UnitTurnState> states) {
        if (states == null) {
            this.unitStates = null;
        } else {
            this.unitStates = new ArrayList<>(states);
        }
    }

    // --- AI settings accessors ---
    public Boolean getAiEnabled() { return aiEnabled; }
    public void setAiEnabled(Boolean aiEnabled) { this.aiEnabled = aiEnabled; }

    public Team getAiTeam() { return aiTeam; }
    public void setAiTeam(Team aiTeam) { this.aiTeam = aiTeam; }

    public Long getAiSeed() { return aiSeed; }
    public void setAiSeed(Long aiSeed) { this.aiSeed = aiSeed; }

    public Double getAiAggressiveness() { return aiAggressiveness; }
    public void setAiAggressiveness(Double v) { this.aiAggressiveness = v; }

    public Double getAiCaution() { return aiCaution; }
    public void setAiCaution(Double v) { this.aiCaution = v; }

    public Double getAiCapturePriority() { return aiCapturePriority; }
    public void setAiCapturePriority(Double v) { this.aiCapturePriority = v; }

    /**
     * Serializable snapshot of a single unit's per-turn movement/attack state.
     */
    public static class UnitTurnState implements Serializable {
        private static final long serialVersionUID = 1L;
        public String name;
        public boolean team;
        public int x;
        public int y;
        public int movePointsSpent;
        public int movesUsed;
        public boolean shiftDone;
        public boolean attackDone;

        public UnitTurnState() {}

        public UnitTurnState(String name, boolean team, int x, int y,
                              int movePointsSpent, int movesUsed, boolean shiftDone, boolean attackDone) {
            this.name = name;
            this.team = team;
            this.x = x;
            this.y = y;
            this.movePointsSpent = movePointsSpent;
            this.movesUsed = movesUsed;
            this.shiftDone = shiftDone;
            this.attackDone = attackDone;
        }
    }
}
