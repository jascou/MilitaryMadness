package military.gui;

import military.Config;
import military.engine.SaveGame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog listing available saved games with basic metadata so the player can pick one.
 */
public class SaveSelectionDialog extends JDialog {
    private JTable table;
    private String selected;

    public SaveSelectionDialog(Window owner) {
        super(owner, "Select Saved Game", ModalityType.APPLICATION_MODAL);
        buildUI();
        setSize(500, 320);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Save Name", "Map", "Turn"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                selected = (String) table.getValueAt(row, 0);
                dispose();
            }
        });
        cancel.addActionListener(e -> { selected = null; dispose(); });
        buttons.add(ok); buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);

        // Load save files and metadata
        try {
            Path dir = Config.savesDir();
            Files.createDirectories(dir);
            List<Path> saves = Files.list(dir)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".mmsave"))
                    .sorted((a,b) -> a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString()))
                    .collect(Collectors.toList());
            for (Path p : saves) {
                String baseName = p.getFileName().toString().replaceFirst("\\.mmsave$", "");
                String map = "?";
                String turn = "?";
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(p))) {
                    SaveGame sg = (SaveGame) ois.readObject();
                    if (sg != null) {
                        map = sg.getMapName();
                        turn = sg.isTurn() ? "Blue (P1)" : "Red (P2)";
                    }
                } catch (Throwable ignored) {
                    // leave defaults for unreadable saves
                }
                model.addRow(new Object[]{baseName, map, turn});
            }
        } catch (Exception ex) {
            // ignore; empty table
        }
    }

    public static String showDialog(Window owner) {
        SaveSelectionDialog dlg = new SaveSelectionDialog(owner);
        dlg.setVisible(true);
        return dlg.selected;
    }
}
