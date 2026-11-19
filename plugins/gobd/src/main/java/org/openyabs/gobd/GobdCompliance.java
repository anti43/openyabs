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

import static org.openyabs.gobd.GobdUtil.str;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import mpv5.db.objects.Item;
import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.SearchResult;
import mpv5.plugin.api.YabsExtension;

/**
 * GoBD compliance for YaBS (Germany): finalizing (Festschreibung) of invoices
 * on export with a SHA-256 hash chain, the allowed payment / dunning workflow,
 * protection against deleting, master data snapshots, cancellation invoices,
 * e-invoices as income, receipts of expenses, integrity checks, the Z1 auditor
 * view and the Z3 export (GDPdU / IDEA).
 * <p>
 * The Java version of the former Groovy plugin gobd_compliance.groovy. It keeps
 * the id of the script, so the settings, the archive folder and the database
 * tables stay; a copy of the script with the same id is not started twice.
 */
public final class GobdCompliance implements YabsExtension {

    /**
     * The id of the Groovy version (settings and data folder are named by it)
     */
    public static final String ID = "groovy.gobd-compliance";
    private Gobd gobd;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return "GoBD-Konformitaet";
    }

    @Override
    public String description() {
        return "Erzwingt GoBD-Unveraenderbarkeit (Festschreibung bei Export), erlaubten Offene-Posten-/Zahlungsstatus-Workflow, Loesch-/Papierkorbschutz,"
                + " Stammdaten-Snapshots, SHA-256-Hash-Verkettung, Archiv-Integritaet und GDPdU-Export";
    }

    @Override
    public String version() {
        return "2.0.0";
    }

    @Override
    public String author() {
        return "OpenYaBS Community";
    }

    @Override
    public String minYabsVersion() {
        return "2.0.10";
    }

    @Override
    public void start(ExtensionContext yabs) throws Exception {
        if (!hasDatabase()) {
            //YaBS starts the plugins after login; without a database (tests, tools) the plugin stays inactive
            mpv5.logging.Log.Debug(GobdCompliance.class, "GoBD: no database connection, not active");
            return;
        }
        final Gobd g = new Gobd(yabs);
        gobd = g;
        g.archiveRoot();
        g.initSchema();
        final Audit audit = new Audit(g);
        final Pages pages = new Pages(g);
        final Views views = new Views(g);

        yabs.addRecordModifier(g.modifier());
        yabs.onActivity(g::onActivity);

        yabs.addCommand("GoBD: 1. Beleg festschreiben", "Schreibt eine Rechnung unveraenderbar nach GoBD fest (inkl. Stammdaten-Snapshot & SHA-256 Hash-Kette)", () -> {
            Item doc = g.selectInvoice("GoBD-Festschreibung (§ 146 Abs. 4 AO)", false);
            if (doc != null) {
                g.finalizeDocument(doc, null);
            }
        });
        yabs.addCommand("GoBD: 2. Beleg stornieren (Stornorechnung)", "Erstellt eine festgeschriebene Stornorechnung fuer einen gesperrten Beleg", () -> {
            Item original = g.selectInvoice("GoBD-Stornorechnung erstellen", true);
            if (original != null) {
                g.stornoDocument(original);
            }
        });
        yabs.addCommand("GoBD: 3. Archiv-Integritaet pruefen", "Prueft alle archivierten Belege und die gesamte SHA-256-Hash-Kette",
                () -> yabs.background(() -> {
                    try {
                        return audit.checkArchive();
                    } catch (Exception e) {
                        return "FEHLER: " + e.getMessage();
                    }
                }, this::report));
        yabs.addCommand("GoBD: 4. Betriebspruefer-Export (Z3 / IDEA ZIP)",
                "Erzeugt ein IDEA-kompatibles Z3-Prueferpaket (index.xml, gdpdu-01-09-2004.dtd, CSVs & Originalbelege)", () -> {
                    String input = JOptionPane.showInputDialog(null, "Pruefungsjahr fuer den Z3-Export eingeben:", String.valueOf(LocalDate.now().getYear()));
                    if (!GobdUtil.isInteger(input)) {
                        return;
                    }
                    int year = Integer.parseInt(input.trim());
                    yabs.background(() -> {
                        try {
                            return audit.exportZ3(year);
                        } catch (Exception e) {
                            throw new IllegalStateException("Z3-Export fehlgeschlagen: " + e.getMessage(), e);
                        }
                    }, zip -> {
                        g.logAudit("EXPORT", year, "Z3_EXPORTED", null, zip.getFileName().toString(), null, "Z3 Prueferexport erstellt: " + zip.getFileName());
                        g.popup("GoBD Z3-Export erstellt:\n" + zip);
                        yabs.openFile(zip.getParent().toFile());
                    });
                });
        yabs.addCommand("GoBD: 5. Pruefer-Ansicht oeffnen (Z1 Read-Only Tab)", "Oeffnet die schreibgeschuetzte 4-Tab GoBD-Z1-Prueferansicht (§ 147 Abs. 6 AO)",
                () -> yabs.openTab("GoBD Z1-Prueferjournal", pages.auditorView()));
        yabs.addCommand("GoBD: 6. Eingangsbelege pruefen (SHA256SUMS)",
                "Prueft alle archivierten Eingangsbelege und Einnahmenbelege gegen die Datenbank und die SHA256SUMS-Dateien",
                () -> yabs.background(() -> {
                    try {
                        return audit.checkReceipts();
                    } catch (Exception e) {
                        return "FEHLER: " + e.getMessage();
                    }
                }, this::report));
        yabs.addCommand("GoBD: 7. ZUGFeRD / XRechnung als Einnahme importieren",
                "Importiert ZUGFeRD-PDF/A-3 oder XRechnung-XML als GoBD-festgeschriebene Einnahme (Revenue)", () -> {
                    JFileChooser chooser = new JFileChooser();
                    String watchDir = yabs.settings().get(Gobd.S_WATCH, "");
                    if (!GobdUtil.blank(watchDir) && Files.exists(Paths.get(watchDir))) {
                        chooser.setCurrentDirectory(new File(watchDir));
                    }
                    chooser.setDialogTitle("ZUGFeRD PDF/A-3 oder XRechnung XML als Einnahme importieren");
                    chooser.setMultiSelectionEnabled(true);
                    chooser.setFileFilter(new FileNameExtensionFilter("E-Rechnungen (ZUGFeRD PDF/A-3, XRechnung XML)", "pdf", "xml"));
                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File[] selected = chooser.getSelectedFiles();
                        if ((selected == null || selected.length == 0) && chooser.getSelectedFile() != null) {
                            selected = new File[]{chooser.getSelectedFile()};
                        }
                        if (selected != null) {
                            for (File f : selected) {
                                g.importIncomeFile(f.toPath(), null, false);
                            }
                        }
                    }
                });

        yabs.addSettingsPage("GoBD-Konformitaet", "Einstellungen fuer automatische Beleg-Festschreibung, GoBD-Archivpfad und ZUGFeRD-Einnahmen-Import",
                pages::settingsPage);

        //Search anything (Shift Shift): finalized invoices and income by number
        yabs.addSearchProvider(text -> {
            List<SearchResult> results = new ArrayList<SearchResult>();
            if (text == null || text.trim().length() < 2) {
                return results;
            }
            String q = "%" + GobdUtil.likeEscape(text.trim().toLowerCase()) + "%";
            for (Object[] r : g.q("SELECT document_id, document_number, status, finalized_at, archive_path FROM gobd_document_lock"
                    + " WHERE LOWER(document_number) LIKE ? ESCAPE '!' ORDER BY chain_seq DESC, finalized_at DESC", q)) {
                results.add(new SearchResult("GoBD-Beleg " + r[1] + " [" + r[2] + "]", "Festgeschrieben am " + r[3] + " – Klick oeffnet Originalbeleg",
                        () -> g.openArchived(str(r[4]))));
            }
            for (Object[] r : g.q("SELECT revenue_id, document_number, status, imported_at, archive_path FROM gobd_income_lock"
                    + " WHERE LOWER(document_number) LIKE ? ESCAPE '!' ORDER BY chain_seq DESC, imported_at DESC", q)) {
                results.add(new SearchResult("GoBD-Einnahme " + r[1] + " [" + r[2] + "]", "Importiert am " + r[3] + " – Klick oeffnet Originalbeleg",
                        () -> g.openArchived(str(r[4]))));
            }
            return results;
        });

        yabs.onView(views::decorate);
        g.configureIncomeWatch();
        //receipts of expenses attached without the plugin (or before): into the hash chain
        yabs.background(g::chainReceipts, n -> {
            if (n > 0) {
                g.status("GoBD: " + n + " Eingangsbeleg(e) in die Hash-Kette aufgenommen.");
            }
        });
        yabs.onStop(g::stop);
        g.status("GoBD-Konformitaets-Plugin (v" + version() + ") vollstaendig geladen.");
    }

    private static boolean hasDatabase() {
        try {
            return mpv5.db.common.DatabaseConnection.getConnector() != null
                    && mpv5.db.common.DatabaseConnection.getConnector().getConnection() != null;
        } catch (Throwable e) {
            return false;
        }
    }

        private void report(String text) {
        if (gobd == null) {
            return;
        }
        if (text.startsWith("FEHLER")) {
            gobd.error(text);
        } else {
            gobd.popup(text);
        }
    }

    @Override
    public void stop() {
        //the context runs Gobd.stop (onStop)
        gobd = null;
    }

    /**
     * @return the archive folder (for tests and other plugins)
     */
    Path archiveRoot() {
        return gobd == null ? null : gobd.archiveRoot();
    }
}
