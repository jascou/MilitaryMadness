package military.engine;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service to save and load game state. Persists the current map (tiles and units)
 * using the existing MapService/LocationManager infrastructure, and writes a small
 * serialized SaveGame metadata file containing the turn, cursor, and per-unit movement state.
 */
public final class SaveLoadService {
    private SaveLoadService() {}

    /**
     * Saves the current game to the Saves directory using the given save name.
     * The map is saved to Maps/{mapName}.txt; the save metadata is stored at Saves/{saveName}.mmsave
     * @param saveName arbitrary save slot name (filename without extension)
     * @param mapName the map filename (without extension) to write current map state into
     * @param state the current GameState providing turn and cursor
     */
    public static void save(String saveName, String mapName, GameState state) {
        if (saveName == null || saveName.trim().isEmpty()) {
            throw new IllegalArgumentException("saveName cannot be empty");
        }
        if (mapName == null || mapName.trim().isEmpty()) {
            throw new IllegalArgumentException("mapName cannot be empty");
        }
        if (state == null) {
            throw new IllegalArgumentException("state cannot be null");
        }
        // 1) Persist the current map (tiles + units)
        new DefaultMapService().saveMap(mapName);
        // 2) Persist tiny metadata file
        Path dir = military.Config.savesDir();
        try {
            Files.createDirectories(dir);
        } catch (Exception ignored) {}
        Path file = dir.resolve(saveName + ".mmsave");
        SaveGame data = new SaveGame(mapName, state.isTurn(), state.getCursor());
        // Capture per-unit movement state
        java.util.List<SaveGame.UnitTurnState> unitStates = new java.util.ArrayList<>();
        for (Unit u : UnitManager.getInstance().getUnits(true)) {
            if (u.getLoc() == null) continue;
            SaveGame.UnitTurnState uts = new SaveGame.UnitTurnState(
                    u.getName(), true,
                    u.getLoc().getLoc().x, u.getLoc().getLoc().y,
                    u.getMovePointsSpentThisTurn(), u.getMovesUsedThisTurn(),
                    u.isShiftDone(), u.isAttackDone());
            unitStates.add(uts);
        }
        for (Unit u : UnitManager.getInstance().getUnits(false)) {
            if (u.getLoc() == null) continue;
            SaveGame.UnitTurnState uts = new SaveGame.UnitTurnState(
                    u.getName(), false,
                    u.getLoc().getLoc().x, u.getLoc().getLoc().y,
                    u.getMovePointsSpentThisTurn(), u.getMovesUsedThisTurn(),
                    u.isShiftDone(), u.isAttackDone());
            unitStates.add(uts);
        }
        data.setUnitStates(unitStates);
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
            oos.writeObject(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save game: " + file + ": " + e.getMessage(), e);
        }
    }

    /**
     * Loads a previously saved game. This will load the underlying map using MapService, and
     * return the SaveGame metadata so the caller can restore transient fields like turn and cursor.
     * @param saveName save slot name (filename without extension)
     * @return SaveGame loaded metadata
     */
    public static SaveGame load(String saveName) {
        if (saveName == null || saveName.trim().isEmpty()) {
            throw new IllegalArgumentException("saveName cannot be empty");
        }
        Path file = military.Config.savesDir().resolve(saveName + ".mmsave");
        SaveGame data;
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
            data = (SaveGame) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load save: " + file + ": " + e.getMessage(), e);
        }
        // Delegate map load to the adapter
        new DefaultMapService().loadMap(data.getMapName());
        // Apply per-unit movement state if available
        try {
            java.util.List<SaveGame.UnitTurnState> list = data.getUnitStates();
            if (list != null && !list.isEmpty()) {
                // Build a quick index by key
                java.util.Map<String, SaveGame.UnitTurnState> idx = new java.util.HashMap<>();
                for (SaveGame.UnitTurnState uts : list) {
                    String key = uts.name + "|" + uts.team + "|" + uts.x + "," + uts.y;
                    idx.put(key, uts);
                }
                java.util.function.Consumer<Unit> apply = (Unit u) -> {
                    if (u.getLoc() == null) return;
                    String k = u.getName() + "|" + u.getTeam() + "|" + u.getLoc().getLoc().x + "," + u.getLoc().getLoc().y;
                    SaveGame.UnitTurnState s = idx.get(k);
                    if (s != null) {
                        u.setTurnMoveState(s.movePointsSpent, s.movesUsed, s.shiftDone, s.attackDone);
                    }
                };
                for (Unit u : UnitManager.getInstance().getUnits(true)) apply.accept(u);
                for (Unit u : UnitManager.getInstance().getUnits(false)) apply.accept(u);
            }
        } catch (Throwable ignored) {
            // Be robust to older saves or partial data
        }
        return data;
    }
}
