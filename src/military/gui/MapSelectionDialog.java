package military.gui;

import military.Config;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A simple dialog listing available maps with name and dimensions.
 * Provides both modal selection and an asynchronous (modeless) show option.
 */
public class MapSelectionDialog extends JDialog {
    private JTable table;
    private String selected;

    public MapSelectionDialog(Window owner) {
        super(owner, "Select Map", ModalityType.APPLICATION_MODAL);
        buildUI();
        setSize(400, 300);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Name", "Width", "Height"}, 0) {
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

        // Load data
        try {
            List<Path> maps = Files.list(Config.mapsDir())
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".txt"))
                    .collect(Collectors.toList());
            for (Path p : maps) {
                int[] dims = readDims(p);
                model.addRow(new Object[]{p.getFileName().toString().replaceFirst("\\.txt$", ""), dims[0], dims[1]});
            }
        } catch (Exception ex) {
            // ignore; empty table
        }
    }

    private int[] readDims(Path p) {
        try {
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            int w = Integer.parseInt(lines.get(0).trim());
            int h = Integer.parseInt(lines.get(1).trim());
            return new int[]{w, h};
        } catch (Exception ex) {
            return new int[]{-1, -1};
        }
    }

    public static String showDialog(Window owner) {
        MapSelectionDialog dlg = new MapSelectionDialog(owner);
        dlg.setVisible(true);
        return dlg.selected;
    }

    public static void showAsync(Window owner, java.util.function.Consumer<String> onSelected) {
        MapSelectionDialog dlg = new MapSelectionDialog(owner);
        dlg.setModalityType(ModalityType.MODELESS);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setVisible(true);
        // When disposed, deliver selection (may be null)
        dlg.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                if (onSelected != null) onSelected.accept(dlg.selected);
            }
        });
    }
}
