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

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import javax.swing.JOptionPane;
import mpv5.handler.JournalLoader;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.JournalExport;
import mpv5.plugin.api.JournalExportContext;
import mpv5.plugin.api.YabsExtension;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;

/**
 * DATEV export plugin ("DATEV-Format Buchungsstapel", EXTF 700, category 21,
 * format version 13).
 * <p>
 * Adds:
 * <ul>
 * <li>A settings page under Settings › Plugins › DATEV-Export,</li>
 * <li>"DATEV-Buchungsstapel (CSV)" in the Journal Export menu with pre-export
 * validation,</li>
 * <li>Automatic inclusion of {@code datev/EXTF_Buchungsstapel_...csv} in the
 * tax-adviser ZIP package while the plugin is active,</li>
 * <li>A command in the Plugins menu to export the current year's journal,</li>
 * <li>"DATEV-Belegpaket" in the Journal Export menu: the outgoing invoices as
 * package of the DATEV XML-Schnittstelle online (DATEV Unternehmen online)
 * with the archived originals or XRechnung files ({@link DatevXml}).</li>
 * </ul>
 */
public final class DatevPlugin implements YabsExtension {

    public static final String ID = "org.openyabs.datev";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return "DATEV-Export";
    }

    @Override
    public String description() {
        return "Exportiert DATEV-Buchungsstapel (EXTF 700, Formatversion 13, CSV), Stammdaten Debitoren/Kreditoren (EXTF 700, Kategorie 16, Version 5) sowie Belegpakete (XML-Schnittstelle online) für DATEV Kanzlei-Rechnungswesen und Unternehmen online.";
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
        return "2.0.23";
    }

    @Override
    public void start(final ExtensionContext yabs) throws Exception {
        yabs.addSettingsPage(
                "DATEV-Export",
                "Berater-/Mandantennummer, Kontenrahmen (SKR03/SKR04), Geldkonten, Steuersatz-Konten und BU-Schlüssel",
                () -> DatevSettingsPage.create(yabs));

        yabs.addJournalExport(new JournalExport() {
            @Override
            public String title() {
                return "DATEV-Buchungsstapel (CSV)";
            }

            @Override
            public String tooltip() {
                return "Schreibt einen DATEV-Buchungsstapel (EXTF 700, Formatversion 13, Windows-1252) mit einer Zeile je bezahltem Beleg und Steuersatz.";
            }

            @Override
            public void export(JournalExportContext ctx) {
                exportInteractive(yabs, ctx);
            }

            @Override
            public Map<String, byte[]> adviserPackageEntries(JournalExportContext ctx) {
                DatevConfig base = DatevConfig.load();
                java.util.List<mpv5.handler.JournalCalc.Entry> entries = ctx.entries;
                if (ctx.from != null && ctx.to != null) {
                    try {
                        vTimeframe t = new vTimeframe(ctx.from, ctx.to);
                        JournalLoader.Loaded loaded = JournalLoader.loadAllResult(t, DatevExportContent.byPayDate(base));
                        if (loaded != null && loaded.entries != null) {
                            entries = loaded.entries;
                        }
                    } catch (Exception ex) {
                        Log.Debug(DatevPlugin.class, "Adviser package DATEV reload fallback: " + ex.getMessage());
                    }
                }
                DatevConfig config = DatevConfig.loadWithDefaults(entries);
                return buildAdviserPackageEntries(entries, config, ctx.from, ctx.to);
            }
        });

        yabs.addJournalExport(new JournalExport() {
            @Override
            public String title() {
                return "DATEV-Stammdaten Debitoren/Kreditoren (CSV)";
            }

            @Override
            public String tooltip() {
                return "Exportiert Kunden (Debitoren) und Lieferanten (Kreditoren) als DATEV-Format EXTF 700 (Kategorie 16, Version 5, Windows-1252).";
            }

            @Override
            public void export(JournalExportContext ctx) {
                exportMasterData(yabs, ctx);
            }
        });

        yabs.addJournalExport(new JournalExport() {
            @Override
            public String title() {
                return "DATEV-Belegpaket (XML-Schnittstelle online, mit E-Rechnungen)";
            }

            @Override
            public String tooltip() {
                return "Ausgangsrechnungen und Gutschriften als ZIP für DATEV Unternehmen online (DATEV Belegtransfer): "
                        + "Rechnungsdaten je Beleg mit dem Original (PDF/ZUGFeRD) bzw. der XRechnung";
            }

            @Override
            public void export(JournalExportContext ctx) {
                exportPackage(yabs, ctx);
            }
        });

        yabs.addCommand(
                "DATEV: Buchungsstapel exportieren (aktuelles Jahr)",
                "Exportiert den DATEV-Buchungsstapel des laufenden Jahres (Datum nach den DATEV-Einstellungen)",
                () -> {
                    LocalDate now = LocalDate.now();
                    Date start = Date.from(LocalDate.of(now.getYear(), 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                    Date end = DateConverter.getEndOfDay(Date.from(LocalDate.of(now.getYear(), 12, 31).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    exportInteractive(yabs, new JournalExportContext(Collections.emptyList(), null, start, end,
                            Collections.emptyList(), null, null, null));
                });
    }

    /**
     * Builds the DATEV EXTF CSV entry for the tax-adviser ZIP package and throws
     * {@link IllegalStateException} when validation reports blocking errors so
     * {@link mpv5.utils.export.JournalAdviserPackage} records the problem instead
     * of bundling an invalid DATEV file.
     */
    static Map<String, byte[]> buildAdviserPackageEntries(java.util.List<mpv5.handler.JournalCalc.Entry> entries,
            DatevConfig config, Date from, Date to) {
        return buildAdviserPackageEntries(entries, config, from, to,
                DatevMapper.dbAccountLookup(), DatevMapper.dbContactVatLookup(),
                DatevMapper.dbReceiptLinkLookup(), DatevMapper.dbTaxCategoryLookup(),
                DatevMapper.dbZeroCategoryLookup(), DatevMapper.dbCurrencyLookup(),
                DatevMapper.dbServiceDateLookup());
    }

    static Map<String, byte[]> buildAdviserPackageEntries(java.util.List<mpv5.handler.JournalCalc.Entry> entries,
            DatevConfig config, Date from, Date to,
            DatevMapper.AccountLookup accountLookup, DatevMapper.ContactVatLookup vatLookup,
            DatevMapper.ReceiptLinkLookup receiptLookup, DatevMapper.TaxCategoryLookup categoryLookup,
            DatevMapper.ZeroCategoryLookup zeroCategoryLookup, DatevMapper.CurrencyLookup currencyLookup,
            DatevMapper.ServiceDateLookup serviceDateLookup) {
        DatevMapper.Result res = DatevMapper.map(entries, config, from, to,
                accountLookup, vatLookup, receiptLookup, categoryLookup, zeroCategoryLookup,
                currencyLookup, serviceDateLookup);
        if (res.hasErrors()) {
            java.util.List<String> errs = new java.util.ArrayList<String>();
            for (DatevMapper.Issue i : res.issues) {
                if (i.severity == DatevMapper.Issue.Severity.ERROR) {
                    errs.add(i.message);
                }
            }
            throw new IllegalStateException("DATEV-Buchungsstapel enthält Fehler und wurde nicht ins Steuerberater-Paket aufgenommen: "
                    + String.join(" | ", errs));
        }
        String csv = DatevFormat.render(config, from, to, new Date(), res.lines);
        String path = "datev/" + DatevFormat.fileName(from, to);
        return Collections.singletonMap(path, DatevFormat.encode(csv));
    }

    /**
     * Exports customer/supplier master data as DATEV EXTF 700 Category 16 (Debitoren/Kreditoren, v5).
     */
    private void exportMasterData(final ExtensionContext yabs, final JournalExportContext ctx) {
        yabs.background(() -> {
            java.util.List<mpv5.db.objects.Contact> contacts = new java.util.ArrayList<mpv5.db.objects.Contact>();
            try {
                for (mpv5.db.common.DatabaseObject o : mpv5.db.common.DatabaseObject.getObjects(mpv5.db.common.Context.getContact())) {
                    contacts.add((mpv5.db.objects.Contact) o);
                }
            } catch (Exception e) {
                throw new IllegalStateException("Kontakte konnten nicht geladen werden: " + e.getMessage(), e);
            }
            return contacts;
        }, contacts -> {
            try {
                DatevConfig config = DatevConfig.loadWithDefaults(null);
                java.util.List<DatevMasterDataFormat.MasterRecord> records = DatevMasterDataFormat.fromContacts(contacts, config);
                if (records.isEmpty()) {
                    JOptionPane.showMessageDialog(ctx.parent, "Keine Kontakte (Debitoren/Kreditoren) zum Export vorhanden.",
                            "DATEV-Stammdaten", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                Date now = new Date();
                String defaultName = DatevMasterDataFormat.fileName(now);
                DialogForFile chooser = new DialogForFile(DialogForFile.FILES_ONLY, defaultName);
                if (!chooser.saveFile()) {
                    return;
                }
                File target = chooser.getFile();
                if (!target.getName().toLowerCase().endsWith(".csv")) {
                    target = new File(target.getParentFile(), target.getName() + ".csv");
                }
                String csv = DatevMasterDataFormat.render(config, now, records);
                Files.write(target.toPath(), DatevMasterDataFormat.encode(csv));
                ctx.notifyExported(target);
                yabs.notify("DATEV-Stammdaten geschrieben (" + records.size() + " Kontakte): " + target.getName());
            } catch (Exception e) {
                Log.Debug(DatevPlugin.class, e);
                Popup.error(e);
            }
        });
    }

    /**
     * The booking batch: the documents of the period are loaded again,
     * without the filters of the journal, by pay date or document date as set
     * in the settings; the dialog shows period, dates, sums, every booking
     * and what is left out
     */
    private void exportInteractive(final ExtensionContext yabs, final JournalExportContext ctx) {
        final DatevConfig base = DatevConfig.load();
        final boolean byPay = DatevExportContent.byPayDate(base);
        yabs.background(() -> {
            try {
                vTimeframe t = new vTimeframe(ctx.from, ctx.to);
                JournalLoader.Loaded l = JournalLoader.loadAllResult(t, byPay);
                //the documents of the period by document date: what is not booked, and why
                return new Object[]{l, byPay ? JournalLoader.loadAll(t, false) : l.entries};
            } catch (Exception e) {
                throw new IllegalStateException("Journal konnte nicht geladen werden: " + e.getMessage(), e);
            }
        }, both -> {
            JournalLoader.Loaded loaded = (JournalLoader.Loaded) both[0];
            @SuppressWarnings("unchecked")
            java.util.List<mpv5.handler.JournalCalc.Entry> periodDocs = (java.util.List<mpv5.handler.JournalCalc.Entry>) both[1];
            try {
                DatevConfig config = DatevConfig.loadWithDefaults(loaded.entries);
                DatevMapper.Result result = DatevMapper.map(loaded.entries, config, ctx.from, ctx.to);
                java.util.List<DatevFormat.BookingLine> dups = DatevExportLog.findDuplicates(result.lines);
                if (!dups.isEmpty()) {
                    result.issues.add(0, new DatevMapper.Issue(DatevMapper.Issue.Severity.WARNING,
                            dups.size() + " Buchungszeile(n) wurden bereits in einem früheren DATEV-Export ausgegeben"
                            + " (GoBD-Hinweis: Doppelimport in DATEV Kanzlei-Rechnungswesen vermeiden)."));
                }
                if (!DatevConfig.isSaved()) {
                    result.issues.add(0, new DatevMapper.Issue(DatevMapper.Issue.Severity.WARNING,
                            "Die DATEV-Einstellungen wurden noch nicht gespeichert: verwendet werden die Standardkonten des SKR"
                            + config.kontenrahmen + ". Berater- und Mandantennummer unter " + DatevConfig.SETTINGS_PATH
                            + " eintragen, die Konten prüfen und speichern."));
                }
                String defaultName = DatevFormat.fileName(ctx.from, ctx.to);
                if (!DatevExportDialog.confirm(ctx.parent, DatevExportContent.batch(config, ctx.from, ctx.to, loaded.entries, periodDocs, result, defaultName))) {
                    return;
                }
                DialogForFile chooser = new DialogForFile(DialogForFile.FILES_ONLY, defaultName);
                if (!chooser.saveFile()) {
                    return;
                }
                File target = chooser.getFile();
                if (!target.getName().toLowerCase().endsWith(".csv")) {
                    target = new File(target.getParentFile(), target.getName() + ".csv");
                }
                String csv = DatevFormat.render(config, ctx.from, ctx.to, new Date(), result.lines);
                byte[] bytes = DatevFormat.encode(csv);
                Files.write(target.toPath(), bytes);
                DatevExportLog.recordExport(target.getName(), ctx.from, ctx.to, result.lines, bytes);
                ctx.notifyExported(target);
                yabs.notify("DATEV-Buchungsstapel " + DatevExportContent.period(ctx.from, ctx.to) + " geschrieben ("
                        + result.lines.size() + " Zeilen): " + target.getName());
            } catch (Exception e) {
                Log.Debug(DatevPlugin.class, e);
                Popup.error(e);
            }
        });
    }

    /**
     * The DATEV document package (XML-Schnittstelle online) of the outgoing
     * invoices of the period: loaded again by document date, without the
     * filters of the journal
     */
    private void exportPackage(final ExtensionContext yabs, final JournalExportContext c) {
        yabs.background(() -> {
            try {
                DatevConfig config = DatevConfig.loadWithDefaults(null);
                return new Object[]{config, DatevXmlExport.load(c.from, c.to, config, (n, r) -> n + ": keine XRechnung (" + r + ")")};
            } catch (Exception e) {
                throw new IllegalStateException("Belege konnten nicht geladen werden: " + e.getMessage(), e);
            }
        }, loaded -> {
            DatevConfig config = (DatevConfig) loaded[0];
            DatevXmlExport.Collected col = (DatevXmlExport.Collected) loaded[1];
            SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
            String name = "DATEV_Belege_" + (c.from == null ? "" : f.format(c.from)) + "_" + (c.to == null ? "" : f.format(c.to)) + ".zip";
            if (col.docs.isEmpty()) {
                JOptionPane.showMessageDialog(c.parent, "Keine Rechnungen oder Gutschriften mit Rechnungsdatum "
                        + DatevExportContent.period(c.from, c.to) + " (stornierte ohne Stornorechnung werden nicht exportiert).",
                        "DATEV-Belegpaket", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            DatevExportDialog.Content content = DatevExportContent.documentPackage(col, c.from, c.to, config.invoiceFolder, name,
                    DatevConfig.isSaved(), config.kontenrahmen);
            if (!DatevExportDialog.confirm(c.parent, content) || col.docs.size() > DatevXml.MAX_DOCUMENTS) {
                return;
            }
            DialogForFile chooser = new DialogForFile(DialogForFile.FILES_ONLY, name);
            if (!chooser.saveFile()) {
                return;
            }
            File target = chooser.getFile();
            if (!target.getName().toLowerCase().endsWith(".zip")) {
                target = new File(target.getParentFile(), target.getName() + ".zip");
            }
            try {
                byte[] pkg = DatevXml.zip(col.docs, mpv5.globals.CompanyInfo.getProperty("account1"), new Date(), config.invoiceFolder);
                Files.write(target.toPath(), pkg);
                String done = "DATEV-Belegpaket " + DatevExportContent.period(c.from, c.to) + " mit " + col.docs.size()
                        + " Belegen geschrieben: " + target.getName() + ".";
                if (pkg.length > 100L * 1024 * 1024) {
                    done += " Das Paket ist größer als 100 MB – DATEV empfiehlt kleinere Pakete (kürzerer Zeitraum).";
                }
                c.notifyExported(target);
                yabs.notify(done);
            } catch (Exception e) {
                Log.Debug(DatevPlugin.class, e);
                Popup.error(e);
            }
        });
    }

    @Override
    public void stop() {
        // ExtensionManager removes all registered pages, exports and commands automatically
    }
}
