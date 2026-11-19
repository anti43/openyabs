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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
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
 * <li>A command in the Plugins menu to export the current year's journal.</li>
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
        return "Exportiert das Journal als DATEV-Buchungsstapel (EXTF 700, Formatversion 13, CSV) fuer DATEV Kanzlei-Rechnungswesen / Unternehmen online.";
    }

    @Override
    public String version() {
        return "1.0.0";
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
    public void start(final ExtensionContext yabs) throws Exception {
        yabs.addSettingsPage(
                "DATEV-Export",
                "Berater-/Mandantennummer, Kontenrahmen (SKR03/SKR04), Geldkonten, Steuersatz-Konten und BU-Schluessel",
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
                DatevConfig config = DatevConfig.load();
                DatevMapper.Result res = DatevMapper.map(ctx.entries, config, ctx.from, ctx.to);
                String csv = DatevFormat.render(config, ctx.from, ctx.to, new Date(), res.lines);
                String path = "datev/" + DatevFormat.fileName(ctx.from, ctx.to);
                return Collections.singletonMap(path, DatevFormat.encode(csv));
            }
        });

        yabs.addCommand(
                "DATEV: Buchungsstapel exportieren (aktuelles Jahr)",
                "Laedt das Journal des laufenden Jahres (EÜR / Zahlungsdatum) und exportiert den DATEV-Buchungsstapel",
                () -> {
                    LocalDate now = LocalDate.now();
                    Date start = Date.from(LocalDate.of(now.getYear(), 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                    Date end = DateConverter.getEndOfDay(Date.from(LocalDate.of(now.getYear(), 12, 31).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    yabs.background(() -> {
                        try {
                            return JournalLoader.loadAllResult(new vTimeframe(start, end), true);
                        } catch (Exception e) {
                            throw new IllegalStateException("Journal konnte nicht geladen werden: " + e.getMessage(), e);
                        }
                    }, loaded -> exportInteractive(yabs, new JournalExportContext(
                            loaded.entries, loaded.totals, start, end, Collections.emptyList(), loaded.labels, null, null)));
                });
    }

    private void exportInteractive(ExtensionContext yabs, JournalExportContext ctx) {
        try {
            DatevConfig config = DatevConfig.load();
            DatevMapper.Result result = DatevMapper.map(ctx.entries, config, ctx.from, ctx.to);
            String defaultName = DatevFormat.fileName(ctx.from, ctx.to);
            if (!DatevCheckDialog.confirm(ctx.parent, result, defaultName)) {
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
            Files.write(target.toPath(), DatevFormat.encode(csv));
            ctx.notifyExported(target);
            yabs.notify("DATEV-Buchungsstapel geschrieben (" + result.lines.size() + " Zeilen): " + target.getName());
        } catch (Exception e) {
            Log.Debug(DatevPlugin.class, e);
            Popup.error(e);
        }
    }

    @Override
    public void stop() {
        // ExtensionManager removes all registered pages, exports and commands automatically
    }
}
