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

import static org.openyabs.gobd.GobdUtil.blank;
import static org.openyabs.gobd.GobdUtil.str;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Item;
import mpv5.db.objects.Revenue;
import mpv5.handler.ExpenseReceipts;
import mpv5.handler.ZugpferdExpenseImport;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.panels.DataPanel;

/**
 * Marks the record views: a badge in the header, a banner with actions, and
 * the lock of the content fields of finalized records
 */
final class Views {

    private static final Color GREEN_BG = new Color(220, 252, 231);
    private static final Color GREEN_FG = new Color(22, 101, 52);
    private static final Color GREEN_LINE = new Color(74, 222, 128);
    private static final Color AMBER_BG = new Color(254, 243, 199);
    private static final Color AMBER_FG = new Color(146, 64, 14);
    private static final Color AMBER_LINE = new Color(245, 158, 11);
    private static final Color RED_BG = new Color(254, 226, 226);
    private static final Color RED_FG = new Color(153, 27, 27);
    private static final Color RED_LINE = new Color(248, 113, 113);
    private final Gobd g;

    Views(Gobd g) {
        this.g = g;
    }

    /**
     * Called for every record view that is opened or shows another record
     *
     * @param panel
     */
    void decorate(DataPanel panel) {
        if (panel == null) {
            return;
        }
        DatabaseObject owner = panel.getDataOwner();
        if (owner instanceof Expense) {
            expense(panel, (Expense) owner);
        } else if (owner instanceof Revenue) {
            revenue(panel, (Revenue) owner);
        } else if (owner instanceof Item) {
            invoice(panel, (Item) owner);
        }
    }

    private static void clear(DataPanel panel) {
        panel.setContentLocked(false);
        panel.setPluginBadge(null);
        panel.setPluginBanner(null);
    }

    private static JLabel badge(String text, Color bg, Color fg, Color line, String tip) {
        JLabel b = new JLabel(text);
        b.setOpaque(true);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(ModernTheme.font(0, Font.BOLD));
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(line, 1, true),
                BorderFactory.createEmptyBorder(ModernTheme.px(2), ModernTheme.px(8), ModernTheme.px(2), ModernTheme.px(8))));
        b.setToolTipText(tip);
        return b;
    }

    private static JPanel banner(String text, boolean bold, Color bg, Color fg, Color line, int padding, JComponent... actions) {
        JPanel banner = new JPanel(new BorderLayout(ModernTheme.px(12), 0));
        banner.setBackground(bg);
        banner.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, line), ModernTheme.padding(padding, 12)));
        JLabel info = new JLabel(text);
        info.setFont(ModernTheme.font(1, bold ? Font.BOLD : Font.PLAIN));
        info.setForeground(fg);
        banner.add(info, BorderLayout.CENTER);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0));
        p.setOpaque(false);
        for (JComponent a : actions) {
            p.add(a);
        }
        banner.add(p, BorderLayout.EAST);
        return banner;
    }

    private static JButton button(String text, Runnable action) {
        JButton b = ModernTheme.actionButton(new JButton(text), null);
        b.addActionListener(e -> action.run());
        return b;
    }

    private JButton openButton(String archivePath) {
        JButton b = button("Originalbeleg oeffnen", () -> g.openArchived(archivePath));
        b.setToolTipText(archivePath);
        return b;
    }

    private static Component parent(DataPanel panel) {
        return panel instanceof Component ? (Component) panel : null;
    }

    private static String shortHash(String sha) {
        return sha.length() >= 12 ? sha.substring(0, 12) : sha;
    }

    // ---------------------------------------------------------------- expenses
    /**
     * GoBD Rz. 61-64: no booking without receipt
     */
    private void expense(DataPanel panel, Expense exp) {
        if (exp.__getIDS() <= 0) {
            clear(panel);
            return;
        }
        Object[][] receipts = g.rows("SELECT ids, originalname, archivepath, sha256, filesize FROM expense_receipts WHERE expensesids = " + exp.__getIDS());
        if (receipts.length > 0) {
            String path = str(receipts[0][2]);
            String sha = str(receipts[0][3]);
            //amounts, tax, account and removing the receipt are locked
            panel.setContentLocked(true);
            panel.setPluginBadge(badge("GoBD BELEGABLAGE (" + receipts.length + ")", GREEN_BG, GREEN_FG, GREEN_LINE,
                    "GoBD Rz. 61–64 & § 146 Abs. 4 AO | " + receipts.length + " Beleg(e) revisionssicher archiviert | SHA-256: " + sha));
            panel.setPluginBanner(banner("GoBD-Festschreibung: Ausgabe besitzt " + receipts.length + " verknuepfte(n) Originalbeleg(e) im Jahresarchiv"
                    + " (SHA-256: " + shortHash(sha) + "…). Betraege, Steuer, Konto & Belegentfernung sind gesperrt; Zahlungsstatus und Zahlungsdatum bleiben buchbar.",
                    true, GREEN_BG, GREEN_FG, GREEN_LINE, 5, openButton(path)));
            return;
        }
        panel.setContentLocked(false);
        panel.setPluginBadge(badge("GoBD: OHNE BELEG", AMBER_BG, AMBER_FG, AMBER_LINE,
                "GoBD Rz. 61–64: Keine Buchung ohne Beleg! Bitte scannen oder fuegen Sie den Eingangsbeleg an."));
        JButton attach = button("Beleg verknuepfen & festschreiben", () -> attachExpenseReceipt(panel, exp));
        panel.setPluginBanner(banner("GoBD-Hinweis: Zu dieser Ausgabe ist noch kein Eingangsbeleg hinterlegt"
                + " (Grundsatz: „Keine Buchung ohne Beleg“, GoBD Rz. 61–64).", false, AMBER_BG, AMBER_FG, AMBER_LINE, 5, attach));
    }

    private void attachExpenseReceipt(DataPanel panel, Expense exp) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Eingangsbeleg / Scan fuer Ausgabe auswaehlen (PDF, XML, Bild)");
        chooser.setFileFilter(new FileNameExtensionFilter("Belegdateien (PDF, XML, PNG, JPG)", "pdf", "xml", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(parent(panel)) != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
            return;
        }
        try {
            File f = chooser.getSelectedFile();
            ExpenseReceipts.Receipt rec = ExpenseReceipts.attach(exp, f);
            if (rec == null) {
                return;
            }
            boolean plainPdf = f.getName().toLowerCase().endsWith(".pdf") && ZugpferdExpenseImport.parse(f) == null;
            g.logAudit("EXPENSE", exp.__getIDS(), "EXPENSE_RECEIPT_ATTACHED", null, Gobd.expenseSnapshot(exp), rec.sha256, plainPdf
                    ? "Eingangsbeleg " + rec.originalName + " (einfaches PDF - rechnerische Richtigkeit der Belegwerte nicht automatisiert geprueft) verknuepft und Ausgabe festgeschrieben."
                    : "Eingangsbeleg " + rec.originalName + " verknuepft und Ausgabe festgeschrieben.");
            g.chainReceipts();
            panel.setDataOwner(exp, true);
            g.notifyQuiet("GoBD-Eingangsbeleg archiviert & Ausgabe festgeschrieben:\nDatei: " + rec.originalName + "\nSHA-256: " + rec.sha256.substring(0, 16) + "..."
                    + (plainPdf ? "\n(Hinweis: Einfacher PDF-Beleg - Werte koennen nicht automatisch auf rechnerische Richtigkeit geprueft werden.)" : ""), exp);
        } catch (Exception ex) {
            g.error("Fehler beim Archivieren des Eingangsbelegs:\n" + ex.getMessage());
        }
    }

    // ---------------------------------------------------------------- income
    private void revenue(DataPanel panel, Revenue rev) {
        if (rev.__getIDS() <= 0) {
            clear(panel);
            return;
        }
        Object[] lock = g.incomeLockRecord(rev.__getIDS());
        Object[][] receipts = g.rows("SELECT ids, originalname, archivepath, sha256, filesize FROM revenue_receipts WHERE revenuesids = " + rev.__getIDS());
        if (receipts.length > 0) {
            //receipts linked by YaBS: into the hash chain of the plugin (only those not chained yet)
            g.chainReceipts(Gobd.Receipts.REVENUE);
        }
        if (lock != null || receipts.length > 0) {
            String docNum = lock != null ? (blank(str(lock[2])) ? str(rev.__getCnumber()) : str(lock[2]))
                    : (blank(rev.__getCnumber()) ? "#" + rev.__getIDS() : rev.__getCnumber());
            String path = lock != null ? str(lock[7]) : str(receipts[0][2]);
            String format = lock != null ? (blank(str(lock[9])) ? "ZUGFERD_PDFA3" : str(lock[9])) : "RECEIPT_FILE";
            String sha = lock != null ? str(lock[10]) : str(receipts[0][3]);
            panel.setContentLocked(true);
            panel.setPluginBadge(badge(lock != null ? "GoBD E-RECHNUNG" : "GoBD BELEG-ARCHIV", GREEN_BG, GREEN_FG, GREEN_LINE,
                    "GoBD § 146 Abs. 4 AO | Einnahme " + docNum + " (" + format + ") | SHA-256: " + sha));
            String text = lock != null
                    ? "GoBD-Festschreibung: Einnahme " + docNum + " (" + format + ", SHA-256: " + shortHash(sha) + "…) ist mit einem Originalbeleg im Jahresarchiv"
                    + " verknuepft. Betraege, Steuer, Konto & Datum sind gesperrt; Zahlungsstatus und Zahlungsdatum bleiben buchbar."
                    : "GoBD-Festschreibung: Einnahme " + docNum + " ist mit " + receipts.length + " archivierten Beleg(en) verknuepft."
                    + " Betraege, Steuer, Konto & Datum sind gesperrt; Zahlungsstatus und Zahlungsdatum bleiben buchbar.";
            panel.setPluginBanner(banner(text, true, GREEN_BG, GREEN_FG, GREEN_LINE, 6, openButton(path)));
            return;
        }
        panel.setContentLocked(false);
        panel.setPluginBadge(badge("GoBD: OHNE BELEG", AMBER_BG, AMBER_FG, AMBER_LINE,
                "GoBD Rz. 61–64: Keine Buchung ohne Beleg! Bitte verknuepfen Sie eine ZUGFeRD-/XRechnung-Datei oder einen Beleg."));
        JButton link = button("ZUGFeRD / Beleg verknuepfen", () -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("ZUGFeRD / XRechnung / Beleg fuer Einnahme auswaehlen");
            chooser.setFileFilter(new FileNameExtensionFilter("Belegdateien (PDF, XML, PNG, JPG)", "pdf", "xml", "png", "jpg", "jpeg"));
            if (chooser.showOpenDialog(parent(panel)) == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
                g.importIncomeFile(chooser.getSelectedFile().toPath(), rev, false);
            }
        });
        panel.setPluginBanner(banner("GoBD-Hinweis: Zu dieser Einnahme ist noch kein Originalbeleg hinterlegt"
                + " (Grundsatz: „Keine Buchung ohne Beleg“, GoBD Rz. 61–64).", false, AMBER_BG, AMBER_FG, AMBER_LINE, 6, link));
    }

    // ---------------------------------------------------------------- invoices
    /**
     * Only invoices, credit notes, deposit and part payment invoices; never
     * orders or offers
     */
    private void invoice(DataPanel panel, Item doc) {
        if (!Gobd.isGobdItemType(doc.__getInttype()) || doc.__getIDS() <= 0) {
            clear(panel);
            return;
        }
        Object[] lock = g.lockRecord(doc.__getIDS());
        if (lock == null) {
            //a saved draft
            panel.setContentLocked(false);
            JLabel draft = new JLabel("GoBD: Entwurf (offen)");
            draft.setOpaque(true);
            draft.setBackground(ModernTheme.accentSoft());
            draft.setForeground(ModernTheme.accent());
            draft.setFont(ModernTheme.font(0, Font.BOLD));
            draft.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(2), ModernTheme.px(8), ModernTheme.px(2), ModernTheme.px(8)));
            draft.setToolTipText("Dieser Beleg ist noch nicht nach GoBD festgeschrieben und wird beim Export automatisch gesichert.");
            panel.setPluginBadge(draft);
            JPanel b = banner("GoBD-Hinweis: Dieser Beleg ist noch ein bearbeitbarer Entwurf und wird beim PDF-/E-Rechnungs-Export automatisch festgeschrieben.",
                    false, ModernTheme.accentSoft(), ModernTheme.foreground(), ModernTheme.border(), 5,
                    button("Jetzt GoBD-festschreiben", () -> {
                        if (!mpv5.ui.toolbars.RecordActions.saveBeforeExport(panel)) {
                            return;
                        }
                        DatabaseObject current = panel.getDataOwner();
                        if (current instanceof Item) {
                            g.finalizeDocument((Item) current, null);
                        }
                    }));
            panel.setPluginBanner(b);
            return;
        }
        String docNum = blank(str(lock[1])) ? str(doc.__getCnumber()) : str(lock[1]);
        String state = blank(str(lock[2])) ? "FINALIZED" : str(lock[2]);
        String path = str(lock[7]);
        String format = blank(str(lock[8])) ? "ARCHIVE" : str(lock[8]);
        String sha = str(lock[9]);
        boolean cancelled = state.equals("CANCELLED");

        //1. content fields and positions locked (payment status and reminders stay free)
        panel.setContentLocked(true);
        //print, PDF, preview and e-invoice open the archived file instead of creating a new one
        if (!blank(path)) {
            panel.setLockedDocument(new File(path));
        }
        Color bg = cancelled ? RED_BG : AMBER_BG;
        Color fg = cancelled ? RED_FG : AMBER_FG;
        Color line = cancelled ? RED_LINE : AMBER_LINE;
        panel.setPluginBadge(badge(cancelled ? "GoBD STORNIERT" : "GoBD FESTGESCHRIEBEN", bg, fg, line,
                "GoBD § 146 Abs. 4 AO | Festgeschrieben am " + str(lock[3]) + " von " + str(lock[4]) + " | Format: " + format + " | SHA-256: " + sha));
        JButton open = openButton(path);
        //short; the details (format, checksum, what stays free) in the tooltip and the badge
        String text = "Beleg " + docNum + " wurde von " + str(lock[4]) + " am " + germanTime(lock[3]) + " festgeschrieben"
                + (cancelled ? " (storniert)." : ".");
        String details = "GoBD § 146 Abs. 4 AO: unveraenderbar archiviert (" + format + ", SHA-256: " + shortHash(sha)
                + "…). Steuerrelevante Felder und Positionen sind gesperrt; Zahlungsstatus, Teilzahlungen und Mahnungen bleiben buchbar.";
        if (!cancelled && doc.__getInttype() != Item.TYPE_CREDIT) {
            JButton storno = button("Stornorechnung erstellen", () -> {
                int ok = javax.swing.JOptionPane.showConfirmDialog(parent(panel),
                        "Soll fuer den festgeschriebenen Beleg " + docNum + " jetzt eine GoBD-konforme Stornorechnung erstellt werden?",
                        "GoBD-Stornorechnung (§ 146 Abs. 4 AO)", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE);
                if (ok == javax.swing.JOptionPane.YES_OPTION) {
                    g.stornoDocument(doc);
                }
            });
            panel.setPluginBanner(withTooltip(banner(text, true, bg, fg, line, 6, open, storno), details));
        } else {
            panel.setPluginBanner(withTooltip(banner(text, true, bg, fg, line, 6, open), details));
        }
    }

    /**
     * @return e.g. 21.11.2026 14:03
     */
    private static String germanTime(Object t) {
        if (t instanceof java.util.Date) {
            return new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format((java.util.Date) t);
        }
        return GobdUtil.fmtTimestamp(t);
    }

    /**
     * @return the banner, its text with the tooltip
     */
    private static JPanel withTooltip(JPanel banner, String tooltip) {
        banner.setToolTipText(tooltip);
        for (java.awt.Component c : banner.getComponents()) {
            if (c instanceof javax.swing.JComponent && !(c instanceof JButton)) {
                ((javax.swing.JComponent) c).setToolTipText(tooltip);
            }
        }
        return banner;
    }
}
