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

import java.awt.Component;
import java.io.File;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import mpv5.handler.JournalCalc.Entry;
import mpv5.handler.JournalCalc.Totals;

/**
 * Data passed to a plugin's {@link JournalExport} from the journal (or the
 * tax-adviser ZIP export).
 */
public final class JournalExportContext {

    public final List<Entry> entries;
    public final Totals totals;
    public final Date from;
    public final Date to;
    public final List<String> filters;
    public final Map<BigDecimal, String> rateLabels;
    /**
     * parent component for dialogs, may be null in headless / background runs
     */
    public final Component parent;
    /**
     * optional callback to display the exported file link in the journal
     */
    public final Consumer<File> onExported;

    public JournalExportContext(List<Entry> entries, Totals totals, Date from, Date to,
            List<String> filters, Map<BigDecimal, String> rateLabels,
            Component parent, Consumer<File> onExported) {
        this.entries = entries == null ? Collections.emptyList() : entries;
        this.totals = totals == null ? new Totals() : totals;
        this.from = from;
        this.to = to;
        this.filters = filters == null ? Collections.emptyList() : filters;
        this.rateLabels = rateLabels == null ? Collections.emptyMap() : rateLabels;
        this.parent = parent;
        this.onExported = onExported;
    }

    /**
     * Notifies the journal that a file was exported, if a callback was provided.
     *
     * @param file
     */
    public void notifyExported(File file) {
        if (onExported != null && file != null) {
            onExported.accept(file);
        }
    }
}
