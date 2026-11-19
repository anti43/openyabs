/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openyabs.datev;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import mpv5.ui.modern.ModernTheme;
import org.openyabs.datev.DatevMapper.Issue;
import org.openyabs.datev.DatevMapper.Result;

/**
 * Pre-export validation dialog for DATEV Buchungsstapel: lists all detected
 * errors and warnings and lets the user choose "Trotzdem schreiben" (or
 * "Exportieren") or "Abbrechen".
 */
final class DatevCheckDialog {

    private DatevCheckDialog() {
    }

    /**
     * Shows the check dialog and returns {@code true} if the user wants to
     * proceed writing the file.
     */
    static boolean confirm(Component parent, Result result, String fileName) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog dialog = new JDialog(owner, "DATEV-Export prüfen", JDialog.ModalityType.APPLICATION_MODAL);
        final boolean[] accepted = {false};

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        String summary = String.format("Datei: %s  ·  %d Buchungszeilen  ·  Summe brutto: %s EUR",
                fileName, result.lines.size(), DatevFormat.formatAmount(result.totalGross()));
        JLabel header = new JLabel(summary);
        header.setFont(ModernTheme.font(1, Font.BOLD));
        root.add(header, BorderLayout.NORTH);

        if (result.issues.isEmpty()) {
            JLabel ok = new JLabel("Keine Probleme gefunden. Die Datei kann geschrieben werden.");
            ok.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            root.add(ok, BorderLayout.CENTER);
        } else {
            DefaultListModel<String> model = new DefaultListModel<String>();
            for (Issue issue : result.issues) {
                model.addElement(issue.toString());
            }
            JList<String> list = new JList<String>(model);
            JScrollPane scroll = new JScrollPane(list);
            scroll.setPreferredSize(new Dimension(620, 260));
            JPanel center = new JPanel(new BorderLayout(0, 6));
            JLabel hint = new JLabel("Gefundene Hinweise / Probleme (" + result.issues.size() + "):");
            center.add(hint, BorderLayout.NORTH);
            center.add(scroll, BorderLayout.CENTER);
            root.add(center, BorderLayout.CENTER);
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton cancel = new JButton("Abbrechen");
        cancel.addActionListener(e -> dialog.dispose());
        JButton proceed = new JButton(result.issues.isEmpty() ? "Schreiben" : "Trotzdem schreiben");
        proceed.addActionListener(e -> {
            accepted[0] = true;
            dialog.dispose();
        });
        buttons.add(cancel);
        buttons.add(proceed);
        root.add(buttons, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        return accepted[0];
    }
}
