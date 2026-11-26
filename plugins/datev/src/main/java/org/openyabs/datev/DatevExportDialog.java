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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import mpv5.ui.modern.ModernTheme;

/**
 * The dialog before a DATEV export is written: what is exported (period, the
 * date the selection and bookings use, what is included and what not), the
 * sums, every booking or document in a table, the documents left out with
 * their reason, and the notes. The user decides with the whole content in
 * front of them.
 */
final class DatevExportDialog {

    /**
     * A tab with a table
     */
    static final class Table {

        final String title;
        final String[] columns;
        final List<Object[]> rows = new ArrayList<Object[]>();
        /**
         * the last row is a sum (bold)
         */
        boolean sumRow;

        Table(String title, String... columns) {
            this.title = title;
            this.columns = columns;
        }

        Table row(Object... values) {
            rows.add(values);
            return this;
        }
    }

    /**
     * What the dialog shows
     */
    static final class Content {

        final String title;
        /**
         * label - value, shown on top
         */
        final List<String[]> facts = new ArrayList<String[]>();
        final List<Table> tables = new ArrayList<Table>();
        final List<String> errors = new ArrayList<String>();
        final List<String> notes = new ArrayList<String>();
        String proceed = "Speichern …";
        /**
         * When true and {@link #errors} is non-empty, the proceed button is disabled
         * so an invalid export (rejected by DATEV) cannot be written accidentally.
         */
        boolean blockOnErrors = false;

        Content(String title) {
            this.title = title;
        }

        Content fact(String label, String value) {
            facts.add(new String[]{label, value});
            return this;
        }
    }

    private DatevExportDialog() {
    }

    /**
     * @param v
     * @return the amount as in DATEV (1.234,56)
     */
    static String money(BigDecimal v) {
        java.text.DecimalFormat f = new java.text.DecimalFormat("#,##0.00", java.text.DecimalFormatSymbols.getInstance(java.util.Locale.GERMANY));
        return f.format(v == null ? BigDecimal.ZERO : v);
    }

    /**
     * @param c
     * @return the content of the dialog (also for screenshots and tests)
     */
    static JComponent panel(Content c) {
        JPanel root = new JPanel(new BorderLayout(0, ModernTheme.px(10)));
        root.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(14), ModernTheme.px(14), ModernTheme.px(8), ModernTheme.px(14)));

        JPanel facts = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(ModernTheme.px(2), 0, ModernTheme.px(2), ModernTheme.px(12));
        int row = 0;
        for (String[] f : c.facts) {
            g.gridx = 0;
            g.gridy = row;
            g.weightx = 0;
            g.fill = GridBagConstraints.NONE;
            JLabel l = new JLabel(f[0]);
            l.setFont(l.getFont().deriveFont(Font.BOLD));
            facts.add(l, g);
            g.gridx = 1;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            //long texts wrap (the whole sentence matters: what is in, what not)
            facts.add(new JLabel("<html><div style='width:" + ModernTheme.px(640) + "px'>" + esc(f[1]).replace("\n", "<br>") + "</div></html>"), g);
            row++;
        }
        root.add(facts, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        for (Table t : c.tables) {
            tabs.addTab(t.title + " (" + (t.sumRow ? Math.max(0, t.rows.size() - 1) : t.rows.size()) + ")", table(t));
        }
        if (!c.errors.isEmpty() || !c.notes.isEmpty()) {
            //the whole message, wrapped
            StringBuilder b = new StringBuilder();
            for (String e : c.errors) {
                b.append("FEHLER: ").append(e).append("\n\n");
            }
            for (String e : c.notes) {
                b.append("\u2022 ").append(e).append("\n\n");
            }
            javax.swing.JTextArea area = new javax.swing.JTextArea(b.toString().trim());
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(6), ModernTheme.px(8), ModernTheme.px(6), ModernTheme.px(8)));
            area.setCaretPosition(0);
            JScrollPane sp = new JScrollPane(area);
            tabs.addTab((c.errors.isEmpty() ? "Hinweise" : "Fehler und Hinweise") + " (" + (c.errors.size() + c.notes.size()) + ")", sp);
            if (!c.errors.isEmpty()) {
                tabs.setForegroundAt(tabs.getTabCount() - 1, new Color(0xB0, 0x20, 0x20));
                tabs.setSelectedIndex(tabs.getTabCount() - 1);
            }
        }
        root.add(tabs, BorderLayout.CENTER);
        root.setPreferredSize(new Dimension(ModernTheme.px(980), ModernTheme.px(560)));
        return root;
    }

    private static JScrollPane table(final Table t) {
        DefaultTableModel m = new DefaultTableModel(t.columns, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int r, int col) {
                return false;
            }
        };
        for (Object[] r : t.rows) {
            Object[] shown = new Object[r.length];
            for (int i = 0; i < r.length; i++) {
                shown[i] = r[i] instanceof BigDecimal ? money((BigDecimal) r[i]) : r[i];
            }
            m.addRow(shown);
        }
        JTable table = new JTable(m);
        table.setRowHeight(Math.max(table.getRowHeight(), ModernTheme.px(22)));
        table.setAutoCreateRowSorter(!t.sumRow);
        final boolean[] numeric = new boolean[t.columns.length];
        for (Object[] r : t.rows) {
            for (int i = 0; i < r.length && i < numeric.length; i++) {
                numeric[i] |= r[i] instanceof BigDecimal;
            }
        }
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable tb, Object value, boolean sel, boolean focus, int r, int col) {
                Component comp = super.getTableCellRendererComponent(tb, value, sel, focus, r, col);
                int mc = tb.convertColumnIndexToModel(col);
                setHorizontalAlignment(mc < numeric.length && numeric[mc] ? SwingConstants.RIGHT : SwingConstants.LEFT);
                boolean sum = t.sumRow && tb.convertRowIndexToModel(r) == tb.getModel().getRowCount() - 1;
                comp.setFont(comp.getFont().deriveFont(sum ? Font.BOLD : Font.PLAIN));
                if (value != null) {
                    setToolTipText(String.valueOf(value));
                }
                return comp;
            }
        });
        //the texts get the room: reason, message, customer, accounts
        for (int i = 0; i < t.columns.length; i++) {
            String col = t.columns[i];
            int w = col.equals("Grund") || col.equals("Meldung") ? 380
                    : col.startsWith("Konten") || col.startsWith("Kunde") || col.equals("Buchungstext") || col.equals("Datei") ? 170
                    : col.equals("Art") ? 90 : col.equals("S/H") || col.equals("BU") ? 40 : 80;
            table.getColumnModel().getColumn(i).setPreferredWidth(ModernTheme.px(w));
        }
        if ("Meldung".equals(t.columns[t.columns.length - 1])) {
            table.getColumnModel().getColumn(0).setMaxWidth(ModernTheme.px(80));
        }
        return new JScrollPane(table);
    }

    /**
     * Shows the dialog
     *
     * @param parent
     * @param c
     * @return true if the user wants to save the file
     */
    static boolean confirm(Component parent, Content c) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog dialog = new JDialog(owner, c.title, JDialog.ModalityType.APPLICATION_MODAL);
        final boolean[] accepted = {false};
        JPanel all = new JPanel(new BorderLayout());
        all.add(panel(c), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), ModernTheme.px(8)));
        JButton cancel = new JButton("Abbrechen");
        cancel.addActionListener(e -> dialog.dispose());
        boolean blocked = c.blockOnErrors && !c.errors.isEmpty();
        JButton proceed = new JButton(c.errors.isEmpty() ? c.proceed
                : (blocked ? "Fehler beheben vor Export" : "Trotz Fehlern " + c.proceed));
        proceed.setEnabled(!blocked);
        proceed.addActionListener(e -> {
            accepted[0] = true;
            dialog.dispose();
        });
        buttons.add(cancel);
        buttons.add(proceed);
        all.add(buttons, BorderLayout.SOUTH);
        dialog.setContentPane(all);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.getRootPane().setDefaultButton(c.errors.isEmpty() ? proceed : cancel);
        dialog.setVisible(true);
        return accepted[0];
    }

    static String esc(String s) {
        return (s == null ? "" : s).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
