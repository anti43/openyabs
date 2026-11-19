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
package mpv5.plugin.api;

import java.util.Collections;
import java.util.Map;

/**
 * Extension point for plugins to contribute an export entry to the Journal
 * export menu and optionally include files in the tax-adviser ZIP package.
 */
public interface JournalExport {

    /**
     * @return the label shown in the Journal Export menu
     */
    String title();

    /**
     * @return optional tooltip text, or null
     */
    default String tooltip() {
        return null;
    }

    /**
     * Runs the export when the user picks it from the Journal Export menu
     * (called on the Swing event dispatch thread).
     *
     * @param context the currently loaded journal entries, period and filters
     */
    void export(JournalExportContext context);

    /**
     * Optional files to include in the tax-adviser ZIP package
     * ({@link mpv5.utils.export.JournalAdviserPackage}), e.g.
     * {@code "datev/EXTF_Buchungsstapel_20260101_20261231.csv" -> bytes}.
     * Called on a background thread during ZIP creation.
     *
     * @param context the journal data being packaged
     * @return map of ZIP entry path to file bytes (empty if nothing to add)
     * @throws Exception if generating the extra files fails
     */
    default Map<String, byte[]> adviserPackageEntries(JournalExportContext context) throws Exception {
        return Collections.emptyMap();
    }
}
