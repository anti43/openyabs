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

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import mpv5.globals.GlobalSettings;
import org.openyabs.datev.DatevFormat.BookingLine;

/**
 * GoBD export audit log for DATEV Buchungsstapel exports.
 * <p>
 * Tracks previously exported booking keys ({@code Buchungs GUID} / {@code Belegfeld 1 + Belegdatum + Umsatz})
 * and batch metadata (timestamp, file name, SHA-256 hash) via {@link GlobalSettings}
 * so accidental duplicate re-exports into DATEV Kanzlei-Rechnungswesen are flagged
 * before saving.
 */
public final class DatevExportLog {

    private static final String K_STAPEL_PREFIX = DatevConfig.PREFIX + "exportprotokoll.stapel.";
    private static final String K_SCHLUESSEL = DatevConfig.PREFIX + "exportprotokoll.schluessel";
    private static final int MAX_STORED_KEYS = 2000;

    private DatevExportLog() {
    }

    /**
     * Builds the canonical deduplication key of a booking line.
     */
    public static String lineKey(BookingLine l) {
        if (l == null) {
            return "";
        }
        if (l.buchungsGuid != null && !l.buchungsGuid.trim().isEmpty()) {
            return l.buchungsGuid.trim();
        }
        return (l.belegfeld1 == null ? "" : l.belegfeld1.trim()) + "|"
                + (l.belegdatum == null ? "" : l.belegdatum.trim()) + "|"
                + DatevFormat.formatAmount(l.umsatz) + "|"
                + (l.gegenkonto == null ? "" : l.gegenkonto.trim());
    }

    /**
     * Computes a lowercase hex SHA-256 digest of the exported file bytes.
     */
    public static String sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data == null ? new byte[0] : data);
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format(Locale.ROOT, "%02x", b & 0xFF));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns all booking lines in {@code lines} whose deduplication key was
     * already recorded in a previous DATEV export.
     */
    public static List<BookingLine> findDuplicates(Collection<BookingLine> lines) {
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> known = loadExportedKeys();
        if (known.isEmpty()) {
            return Collections.emptyList();
        }
        List<BookingLine> dups = new ArrayList<BookingLine>();
        for (BookingLine l : lines) {
            String k = lineKey(l);
            if (!k.isEmpty() && known.contains(k)) {
                dups.add(l);
            }
        }
        return dups;
    }

    /**
     * Records a completed DATEV export (batch SHA-256, file name, period, and exported line keys).
     */
    public static void recordExport(String fileName, Date from, Date to, Collection<BookingLine> lines, byte[] content) {
        Set<String> known = loadExportedKeys();
        if (lines != null) {
            for (BookingLine l : lines) {
                String k = lineKey(l);
                if (!k.isEmpty()) {
                    known.add(k);
                }
            }
        }
        // Keep bounded history to prevent unbounded GlobalSettings growth
        List<String> list = new ArrayList<String>(known);
        if (list.size() > MAX_STORED_KEYS) {
            list = list.subList(list.size() - MAX_STORED_KEYS, list.size());
        }
        GlobalSettings.setProperty(K_SCHLUESSEL, String.join(",", list));

        String ts = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String summary = (fileName == null ? "" : fileName) + "|"
                + DatevFormat.yyyymmdd(from) + "-" + DatevFormat.yyyymmdd(to) + "|"
                + (lines == null ? 0 : lines.size()) + "|"
                + sha256(content);
        GlobalSettings.setProperty(K_STAPEL_PREFIX + ts, summary);
        try {
            GlobalSettings.save();
        } catch (Exception ignored) {
            // In unit tests without a running DB, GlobalSettings still holds properties in memory
        }
    }

    /**
     * Returns all recorded export batches (timestamp -> summary string).
     */
    public static Map<String, String> batches() {
        Map<String, String> out = new LinkedHashMap<String, String>();
        for (String[] pair : GlobalSettings.getProperties(K_STAPEL_PREFIX).getList()) {
            if (pair != null && pair.length >= 2 && pair[0] != null) {
                out.put(pair[0], pair[1]);
            }
        }
        return out;
    }

    /**
     * Clears the export log (primarily for tests).
     */
    public static void clear() {
        GlobalSettings.removeProperty(K_SCHLUESSEL);
        for (String[] pair : GlobalSettings.getProperties(K_STAPEL_PREFIX).getList()) {
            if (pair != null && pair.length > 0 && pair[0] != null) {
                GlobalSettings.removeProperty(K_STAPEL_PREFIX + pair[0]);
            }
        }
    }

    private static Set<String> loadExportedKeys() {
        Set<String> set = new LinkedHashSet<String>();
        if (!GlobalSettings.hasProperty(K_SCHLUESSEL)) {
            return set;
        }
        String raw = GlobalSettings.getProperty(K_SCHLUESSEL);
        if (raw == null || raw.trim().isEmpty() || "null".equals(raw)) {
            return set;
        }
        for (String part : raw.split(",")) {
            String p = part.trim();
            if (!p.isEmpty()) {
                set.add(p);
            }
        }
        return set;
    }
}
