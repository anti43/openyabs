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
package org.openyabs.heilpraktiker;

import groovy.json.JsonSlurper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Official GebüH 85/2002 catalog (Gebührenverzeichnis für Heilpraktiker,
 * Fachverband Deutscher Heilpraktiker e.V., 144 entries across 27 groups,
 * Ziffern 1–39.13).
 */
public final class GebuehCatalog {

    public static final String RESOURCE_PATH = "/org/openyabs/heilpraktiker/gebueh_catalog.json";
    public static final String SKU_PREFIX = "GEBUEH-";
    private static final Pattern CODE_PATTERN = Pattern.compile(
            "(?:GEBUEH-|Geb\\u00fcH\\s*)?(A\\s*)?(\\d{1,2}(?:\\.\\d{1,2})?)\\b",
            Pattern.CASE_INSENSITIVE);

    public enum PriceMode {
        MIN,
        MID,
        MAX
    }

    public static final class Entry {

        public final String code;
        public final String group;
        public final String title;
        public final BigDecimal minFee;
        public final BigDecimal maxFee;
        public final List<String> excludesWith;
        public final List<String> onlyAllowedWith;
        public final boolean aloneOnly;
        public final int maxPerCase;
        public final int maxPerSession;
        public final String note;

        public Entry(String code, String group, String title, BigDecimal minFee, BigDecimal maxFee,
                List<String> excludesWith, List<String> onlyAllowedWith, boolean aloneOnly,
                int maxPerCase, int maxPerSession, String note) {
            this.code = code;
            this.group = group;
            this.title = title;
            this.minFee = minFee;
            this.maxFee = maxFee;
            this.excludesWith = Collections.unmodifiableList(excludesWith);
            this.onlyAllowedWith = Collections.unmodifiableList(onlyAllowedWith);
            this.aloneOnly = aloneOnly;
            this.maxPerCase = maxPerCase;
            this.maxPerSession = maxPerSession;
            this.note = note == null ? "" : note;
        }

        public String sku() {
            return SKU_PREFIX + code;
        }

        public BigDecimal feeFor(PriceMode mode) {
            if (mode == PriceMode.MIN && minFee.signum() > 0) {
                return minFee;
            }
            if (mode == PriceMode.MID && minFee.signum() > 0) {
                return minFee.add(maxFee).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            }
            return maxFee;
        }
    }

    public static final class ParsedCode {

        public final String code;
        public final boolean analog;

        public ParsedCode(String code, boolean analog) {
            this.code = code;
            this.analog = analog;
        }
    }

    private final String version;
    private final String source;
    private final Map<String, Entry> byCode = new LinkedHashMap<String, Entry>();

    private GebuehCatalog(String version, String source, List<Entry> entries) {
        this.version = version;
        this.source = source;
        for (Entry e : entries) {
            byCode.put(e.code, e);
        }
    }

    /**
     * Loads the bundled official GebüH 85/2002 catalog from the plugin JAR.
     */
    @SuppressWarnings("unchecked")
    public static GebuehCatalog loadDefault() throws IOException {
        try (InputStream in = GebuehCatalog.class.getResourceAsStream(RESOURCE_PATH)) {
            if (in == null) {
                throw new IOException("GebüH catalog resource not found: " + RESOURCE_PATH);
            }
            Map<String, Object> root = (Map<String, Object>) new JsonSlurper()
                    .parse(new InputStreamReader(in, StandardCharsets.UTF_8));
            String version = str(root.get("catalogVersion"));
            String source = str(root.get("source"));
            List<Map<String, Object>> rawItems = (List<Map<String, Object>>) root.get("items");
            List<Entry> list = new ArrayList<Entry>();
            if (rawItems != null) {
                for (Map<String, Object> m : rawItems) {
                    list.add(new Entry(
                            str(m.get("code")),
                            str(m.get("group")),
                            str(m.get("title")),
                            bd(m.get("minFee")),
                            bd(m.get("maxFee")),
                            strList(m.get("excludesWith")),
                            strList(m.get("onlyAllowedWith")),
                            Boolean.TRUE.equals(m.get("aloneOnly")),
                            intVal(m.get("maxPerCase")),
                            intVal(m.get("maxPerSession")),
                            str(m.get("note"))));
                }
            }
            return new GebuehCatalog(version, source, list);
        }
    }

    public String version() {
        return version;
    }

    public String source() {
        return source;
    }

    public int size() {
        return byCode.size();
    }

    public List<Entry> all() {
        return Collections.unmodifiableList(new ArrayList<Entry>(byCode.values()));
    }

    public Set<String> groups() {
        Set<String> g = new LinkedHashSet<String>();
        for (Entry e : byCode.values()) {
            g.add(e.group);
        }
        return Collections.unmodifiableSet(g);
    }

    public Entry get(String code) {
        if (code == null) {
            return null;
        }
        return byCode.get(code.trim());
    }

    /**
     * Extracts a GebüH code (and whether it is marked as analog billing with
     * prefix {@code A}) from a product SKU or invoice line description such as
     * {@code "GEBUEH-19.2"}, {@code "A 35.1"}, {@code "GebüH 4: Eingehende Beratung"}.
     *
     * @param text SKU or description text
     * @return parsed code, or {@code null} if no GebüH code is recognized
     */
    public ParsedCode parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String trimmed = text.trim();
        // Direct match (e.g. "19.2", "A 35.1", "A35.1", "GEBUEH-19.2", "GEBUEH-A 35.1")
        String cleaned = trimmed;
        if (cleaned.toUpperCase().startsWith(SKU_PREFIX)) {
            cleaned = cleaned.substring(SKU_PREFIX.length()).trim();
        }
        if (cleaned.startsWith("A ") || cleaned.startsWith("a ")) {
            String rest = cleaned.substring(2).trim();
            if (byCode.containsKey(rest)) {
                return new ParsedCode(rest, true);
            }
        } else if ((cleaned.startsWith("A") || cleaned.startsWith("a")) && cleaned.length() > 1 && Character.isDigit(cleaned.charAt(1))) {
            String rest = cleaned.substring(1).trim();
            if (byCode.containsKey(rest)) {
                return new ParsedCode(rest, true);
            }
        } else if (byCode.containsKey(cleaned)) {
            return new ParsedCode(cleaned, false);
        }

        // Pattern match inside description (only if prefixed with GEBUEH- or GebüH or leading A <code>)
        if (trimmed.toUpperCase().contains("GEBUEH") || trimmed.toLowerCase().contains("gebüh")
                || trimmed.matches("^[Aa]\\s*\\d{1,2}(\\.\\d{1,2})?\\b.*")) {
            Matcher m = CODE_PATTERN.matcher(trimmed);
            if (m.find()) {
                boolean analog = m.group(1) != null && !m.group(1).trim().isEmpty();
                String code = m.group(2);
                if (byCode.containsKey(code)) {
                    return new ParsedCode(code, analog);
                }
            }
        }
        return null;
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private static BigDecimal bd(Object o) {
        if (o == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(String.valueOf(o).trim()).setScale(2, RoundingMode.HALF_UP);
    }

    private static int intVal(Object o) {
        if (o == null) {
            return 0;
        }
        return ((Number) o).intValue();
    }

    @SuppressWarnings("unchecked")
    private static List<String> strList(Object o) {
        List<String> l = new ArrayList<String>();
        if (o instanceof List) {
            for (Object x : (List<Object>) o) {
                if (x != null) {
                    l.add(String.valueOf(x).trim());
                }
            }
        }
        return l;
    }
}
