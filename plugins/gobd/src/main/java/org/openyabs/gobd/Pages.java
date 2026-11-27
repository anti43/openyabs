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
package org.openyabs.gobd;

import static org.openyabs.gobd.GobdUtil.fmtDate;
import static org.openyabs.gobd.GobdUtil.fmtDec;
import static org.openyabs.gobd.GobdUtil.str;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import mpv5.ui.modern.ModernTheme;

/**
 * The Z1 auditor view (read only, § 147 Abs. 6 AO) and the settings page
 */
final class Pages {

    private static final String ALL_YEARS = "Alle Jahre";
    private final Gobd g;

    Pages(Gobd g) {
        this.g = g;
    }

    private static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable table(DefaultTableModel model, final int archiveCol) {
        JTable t = new JTable(model) {
            private static final long serialVersionUID = 1L;

            @Override
            public String getToolTipText(MouseEvent e) {
                int v = rowAtPoint(e.getPoint());
                if (v >= 0 && archiveCol >= 0) {
                    Object p = getModel().getValueAt(convertRowIndexToModel(v), archiveCol);
                    if (p != null && !p.toString().trim().isEmpty()) {
                        return "Doppelklick oeffnet Originalbeleg: " + p;
                    }
                }
                return super.getToolTipText(e);
            }
        };
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(true);
        if (archiveCol >= 0 && archiveCol < t.getColumnModel().getColumnCount()) {
            //the path stays in the model, hidden
            TableColumn c = t.getColumnModel().getColumn(archiveCol);
            c.setMinWidth(0);
            c.setMaxWidth(0);
            c.setPreferredWidth(0);
            t.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && t.getSelectedRow() >= 0) {
                        openSelected(t, archiveCol);
                    }
                }
            });
        }
        return t;
    }

    private void openSelected(JTable t, int archiveCol) {
        int v = t.getSelectedRow();
        if (v < 0) {
            g.popup("Bitte markieren Sie zuerst eine Zeile mit verknuepftem Originalbeleg.");
            return;
        }
        g.openArchived(str(t.getModel().getValueAt(t.convertRowIndexToModel(v), archiveCol)));
    }

    /**
     * Writes the complete audit log as a text file in the background, logs
     * it and opens it (the program of the system for .txt files)
     */
    void exportAuditLog() {
        Audit audit = new Audit(g);
        g.yabs.background(() -> {
            try {
                return audit.exportAuditLogText();
            } catch (Exception e) {
                throw new IllegalStateException("Audit-Log-Export fehlgeschlagen: " + e.getMessage(), e);
            }
        }, file -> {
            try {
                g.logAudit("EXPORT", 0L, "AUDIT_LOG_EXPORTED", null, file.getFileName().toString(), null,
                        "Audit-Log vollstaendig als Textdatei exportiert: " + file);
            } catch (RuntimeException e) {
                g.error("Audit-Log exportiert, aber der Export konnte nicht protokolliert werden:\n" + e.getMessage());
            }
            g.status("GoBD: Audit-Log als Textdatei geschrieben: " + file);
            g.yabs.openFile(file.toFile());
        });
    }

    /**
     * @return the Z1 auditor view: invoices, income, expenses with receipts,
     * audit log, per year
     */
    JComponent auditorView() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel summary = new JLabel("GoBD Z1-Prueferansicht – unmittelbarer schreibgeschuetzter Datenzugriff");
        summary.setFont(ModernTheme.font(1, Font.BOLD));
        JComboBox<String> yearCombo = new JComboBox<String>(new String[]{ALL_YEARS});
        JButton refresh = ModernTheme.actionButton(new JButton("Aktualisieren"), null);
        JButton openSel = ModernTheme.actionButton(new JButton("Markierten Originalbeleg oeffnen"), null);
        JButton auditText = ModernTheme.actionButton(new JButton("Audit-Log als Textdatei"), null);
        auditText.setToolTipText("Vollstaendiges Audit-Log (alle Jahre, mit Hash-Kette und Pruefergebnis) als UTF-8-Textdatei oeffnen");
        auditText.addActionListener(e -> exportAuditLog());
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.add(summary, BorderLayout.WEST);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.add(new JLabel("Pruefungszeitraum:"));
        controls.add(yearCombo);
        controls.add(openSel);
        controls.add(auditText);
        controls.add(refresh);
        top.add(controls, BorderLayout.EAST);

        DefaultTableModel inv = model("ID", "Belegnummer", "Status", "Festgeschrieben am", "Benutzer", "Format", "SHA-256 Hash", "Storno-Bezug", "Archivpfad");
        DefaultTableModel inc = model("Einnahme-ID", "Belegnummer", "Status", "Importiert am", "Benutzer", "Originaldatei", "Format", "Datei-SHA-256", "Ketten-Hash", "Archivpfad");
        DefaultTableModel exp = model("Ausgabe-ID", "Belegnummer", "Beschreibung", "Datum", "Brutto (EUR)", "Belegstatus", "Originaldatei", "SHA-256", "Archivpfad");
        DefaultTableModel aud = model("ID", "Zeitstempel", "Benutzer", "Objekttyp", "Objekt-ID", "Aktion", "Alter Wert", "Neuer Wert", "Details");
        JTable invT = table(inv, 8);
        JTable incT = table(inc, 9);
        JTable expT = table(exp, 8);
        JTable audT = table(aud, -1);
        //missing receipts (GoBD Rz. 61-64, no booking without receipt) stand out
        expT.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    boolean missing = str(tbl.getModel().getValueAt(tbl.convertRowIndexToModel(row), 5)).contains("Fehlt");
                    c.setBackground(missing ? new Color(254, 243, 199) : new Color(220, 252, 231));
                    c.setForeground(missing ? new Color(146, 64, 14) : new Color(22, 101, 52));
                }
                return c;
            }
        });
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1. Ausgangsrechnungen & Stornos", new JScrollPane(invT));
        tabs.addTab("2. Importierte Einnahmen (ZUGFeRD)", new JScrollPane(incT));
        tabs.addTab("3. Ausgaben & Eingangsbelege", new JScrollPane(expT));
        tabs.addTab("4. GoBD Audit-Protokoll", new JScrollPane(audT));
        tabs.addChangeListener(e -> openSel.setEnabled(tabs.getSelectedIndex() <= 2));
        openSel.addActionListener(e -> {
            int i = tabs.getSelectedIndex();
            if (i == 0) {
                openSelected(invT, 8);
            } else if (i == 1) {
                openSelected(incT, 9);
            } else if (i == 2) {
                openSelected(expT, 8);
            }
        });

        final boolean[] updatingYears = {false};
        Runnable reload = () -> {
            String selectedYear = str(yearCombo.getSelectedItem()).trim();
            boolean byYear = !selectedYear.isEmpty() && !selectedYear.equals(ALL_YEARS);
            Object[][] invRows = g.rows("SELECT document_id, document_number, status, finalized_at, finalized_by, artifact_format, sha256_hash,"
                    + " cancels_document_id, archive_path FROM gobd_document_lock ORDER BY COALESCE(chain_seq, document_id) DESC, finalized_at DESC");
            Object[][] incRows = g.rows("SELECT revenue_id, document_number, status, imported_at, imported_by, original_name, artifact_format,"
                    + " file_sha256, chain_hash, archive_path FROM gobd_income_lock ORDER BY chain_seq DESC, imported_at DESC");
            Object[][] expRows = g.rows("SELECT e.ids, e.cnumber, e.description, e.dateadded, e.brutvalue, r.originalname, r.sha256, r.archivepath"
                    + " FROM expenses e LEFT JOIN expense_receipts r ON e.ids = r.expensesids WHERE COALESCE(e.invisible, 0) = 0 ORDER BY e.ids DESC");
            Object[][] audRows = g.rows("SELECT id, created_at, user_id, entity_type, entity_id, action, old_value, new_value, details"
                    + " FROM gobd_audit_log ORDER BY id DESC");
            Set<String> years = new TreeSet<String>(Collections.reverseOrder());
            Consumer<Object> year = o -> {
                String s = fmtDate(o);
                if (s.length() >= 4 && GobdUtil.isInteger(s.substring(0, 4))) {
                    years.add(s.substring(0, 4));
                }
            };
            for (Object[] r : invRows) {
                year.accept(r[3]);
            }
            for (Object[] r : incRows) {
                year.accept(r[3]);
            }
            for (Object[] r : expRows) {
                year.accept(r[3]);
            }
            for (Object[] r : audRows) {
                year.accept(r[1]);
            }
            updatingYears[0] = true;
            try {
                yearCombo.removeAllItems();
                yearCombo.addItem(ALL_YEARS);
                for (String y : years) {
                    yearCombo.addItem(y);
                }
                if (byYear && years.contains(selectedYear)) {
                    yearCombo.setSelectedItem(selectedYear);
                } else {
                    yearCombo.setSelectedItem(ALL_YEARS);
                    byYear = false;
                }
            } finally {
                updatingYears[0] = false;
            }
            final boolean f = byYear;
            inv.setRowCount(0);
            for (Object[] r : invRows) {
                if (!f || fmtDate(r[3]).startsWith(selectedYear)) {
                    inv.addRow(new Object[]{r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7] == null ? "" : r[7], r[8] == null ? "" : r[8]});
                }
            }
            inc.setRowCount(0);
            for (Object[] r : incRows) {
                if (!f || fmtDate(r[3]).startsWith(selectedYear)) {
                    inc.addRow(new Object[]{r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8], r[9] == null ? "" : r[9]});
                }
            }
            exp.setRowCount(0);
            int missing = 0;
            for (Object[] r : expRows) {
                if (!f || fmtDate(r[3]).startsWith(selectedYear)) {
                    String orig = str(r[5]).trim();
                    String arch = str(r[7]).trim();
                    boolean has = !orig.isEmpty() && !arch.isEmpty();
                    if (!has) {
                        missing++;
                    }
                    exp.addRow(new Object[]{r[0], str(r[1]), str(r[2]), fmtDate(r[3]), fmtDec(r[4]), has ? "Archiviert" : "Fehlt",
                        orig, str(r[6]).trim(), arch});
                }
            }
            aud.setRowCount(0);
            for (Object[] r : audRows) {
                if (!f || fmtDate(r[1]).startsWith(selectedYear)) {
                    aud.addRow(new Object[]{r[0], r[1], r[2], r[3], r[4], r[5], str(r[6]), str(r[7]), str(r[8])});
                }
            }
            tabs.setTitleAt(0, "1. Ausgangsrechnungen & Stornos (" + inv.getRowCount() + ")");
            tabs.setTitleAt(1, "2. Importierte Einnahmen / ZUGFeRD (" + inc.getRowCount() + ")");
            tabs.setTitleAt(2, "3. Ausgaben & Eingangsbelege (" + exp.getRowCount() + (missing > 0 ? " | " + missing + " ohne Beleg" : "") + ")");
            tabs.setTitleAt(3, "4. GoBD Audit-Protokoll (" + aud.getRowCount() + ")");
            summary.setText("GoBD Z1-Prueferansicht – " + inv.getRowCount() + " Ausgangsbelege | " + inc.getRowCount()
                    + " ZUGFeRD-Einnahmen | " + exp.getRowCount() + " Ausgaben | " + aud.getRowCount() + " Audit-Eintraege");
        };
        yearCombo.addActionListener(e -> {
            if (!updatingYears[0]) {
                reload.run();
            }
        });
        refresh.addActionListener(e -> reload.run());
        reload.run();
        root.add(top, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        return root;
    }

    // ---------------------------------------------------------------- settings
    /**
     * A folder field with "..." and "Speichern"
     */
    private static JPanel folderRow(JTextField field, String chooserTitle, Consumer<String> save, Component parent) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(field, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JButton browse = new JButton("...");
        browse.setToolTipText(chooserTitle);
        browse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(field.getText());
            chooser.setDialogTitle(chooserTitle);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
                save.accept(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        JButton saveB = new JButton("Speichern");
        saveB.addActionListener(e -> save.accept(field.getText()));
        buttons.add(browse);
        buttons.add(saveB);
        row.add(buttons, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) row.getPreferredSize().getHeight()));
        return row;
    }

    private static JLabel label(String text, int top) {
        JLabel l = new JLabel(text);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(top, 0, 4, 0));
        return l;
    }

    /**
     * @return the settings: automatic finalizing, archive folder, watch folder
     * for the income import
     */
    JComponent settingsPage() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JCheckBox autoLock = new JCheckBox("Rechnungen beim Export (PDF / ZUGFeRD / XRechnung) automatisch festschreiben (GoBD-Standard)",
                g.yabs.settings().getBoolean(Gobd.S_AUTOLOCK, true));
        autoLock.setAlignmentX(Component.LEFT_ALIGNMENT);
        autoLock.addActionListener(e -> {
            g.yabs.settings().setBoolean(Gobd.S_AUTOLOCK, autoLock.isSelected());
            g.status("GoBD-Einstellung gespeichert.");
        });

        JTextField archive = new JTextField(g.archiveRoot().toAbsolutePath().toString(), 30);
        JPanel archiveRow = folderRow(archive, "GoBD-Archivordner auswaehlen", s -> {
            try {
                Path p = Paths.get(s.trim()).toAbsolutePath();
                Files.createDirectories(p);
                g.yabs.settings().set(Gobd.S_ARCHIVE, p.toString());
                archive.setText(p.toString());
                g.logAudit("SETTINGS", 0L, "ARCHIVE_PATH_CHANGED", null, p.toString(), null, "GoBD-Archivordner geaendert");
                g.popup("GoBD-Archivordner gespeichert:\n" + p);
            } catch (Exception ex) {
                g.error("Ungueltiger oder nicht beschreibbarer Ordnerpfad:\n" + ex.getMessage());
            }
        }, panel);


        JButton sealBtn = new JButton("Bestand jetzt versiegeln");
        sealBtn.setToolTipText("Versiegelt alle steuerrelevanten Datensaetze (Hash je Datensatz, Hash-Kette der Versiegelungen)");
        sealBtn.addActionListener(e -> g.yabs.background(() -> {
            try {
                return g.inventory.seal("MANUAL");
            } catch (Exception ex) {
                throw new IllegalStateException("GoBD-Bestandsversiegelung fehlgeschlagen: " + ex.getMessage(), ex);
            }
        }, r -> {
            if (r.suspicious()) {
                g.error(r.message());
            } else {
                g.popup(r.message());
            }
        }));
        JPanel sealRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sealRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        sealRow.add(sealBtn);
        sealRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        panel.add(autoLock);
        panel.add(label("Revisionssicherer GoBD-Archivordner:", 10));
        panel.add(archiveRow);
        panel.add(sealRow);
        //e-invoices (expenses and income) are imported by YaBS: import buttons, drag and drop and
        //the e-invoice folder (settings > folders); the plugin locks the records with their receipts
        panel.add(label("E-Rechnungen (Ausgaben und Einnahmen) importiert YaBS: Import-Schaltflaechen, Drag & Drop und der"
                + " E-Rechnungs-Ordner (Einstellungen > Ordner). Das Plugin schreibt die Buchungen mit ihrem Beleg fest.", 14));
        JPanel root = new JPanel(new BorderLayout());
        root.add(panel, BorderLayout.NORTH);
        return new JScrollPane(root);
    }
}
