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
package org.openyabs.handwerk;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.openyabs.handwerk.BoqDocument.BoqPosition;
import org.openyabs.handwerk.BoqDocument.PositionType;

/**
 * Generic pan-European CSV Bill-of-Quantities codec (for spreadsheet-based
 * tenders such as France DPGF/BPU, UK NRM 2, Italy Computo Metrico, or custom
 * Excel exports).
 * <p>
 * Expected columns (auto-detected header in EN or DE, semicolon or comma
 * delimiter): {@code OZ/Ordinal}, {@code Category}, {@code Type},
 * {@code Quantity}, {@code Unit}, {@code ShortText}, {@code LongText},
 * {@code UnitPrice}.
 */
public final class CsvBoqCodec implements BoqCodec {

    @Override
    public String id() {
        return "CSV";
    }

    @Override
    public boolean canRead(String fileName, String content) {
        String lower = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".csv") || lower.endsWith(".tsv");
    }

    @Override
    public boolean canExportBid(String formatCode) {
        return "CSV".equalsIgnoreCase(formatCode);
    }

    @Override
    public String bidFileExtension() {
        return "csv";
    }

    @Override
    public BoqDocument read(String fileName, String content) {
        BoqDocument doc = new BoqDocument();
        doc.formatCode = "CSV";
        doc.exchangePhase = "GENERIC";
        doc.rawSource = content == null ? "" : content;
        doc.importedAt = LocalDate.now().toString();
        doc.projectName = stripExt(fileName);
        doc.boqLabel = doc.projectName;

        String cleaned = content == null ? "" : (content.startsWith("\uFEFF") ? content.substring(1) : content);
        String[] lines = cleaned.replace("\r\n", "\n").split("\n");
        if (lines.length == 0) {
            return doc;
        }
        char sep = lines[0].indexOf(';') >= 0 ? ';' : ',';

        int colOz = 0, colCat = 1, colType = 2, colQty = 3, colUnit = 4, colShort = 5, colLong = 6, colUp = 7, colBidder = -1;
        int startRow = 0;
        List<String> first = parseCsvLine(lines[0], sep);
        if (looksLikeHeader(first)) {
            startRow = 1;
            colOz = findCol(first, "oz", "ordinal", "pos", "item", "nr");
            colCat = findCol(first, "category", "titel", "section", "lot", "kategorie");
            colType = findCol(first, "type", "typ", "art");
            colQty = findCol(first, "qty", "quantity", "menge");
            colUnit = findCol(first, "unit", "einheit", "qu", "me");
            colShort = findCol(first, "shorttext", "short", "kurztext", "description", "beschreibung");
            colLong = findCol(first, "longtext", "long", "langtext", "detail");
            colUp = findCol(first, "unitprice", "unit_price", "ep", "preis", "price", "up");
            colBidder = findCol(first, "biddertext", "bidder", "bietertext", "bieter");
        }

        int seq = 1;
        for (int i = startRow; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            List<String> cols = parseCsvLine(lines[i], sep);
            BoqPosition p = new BoqPosition();
            String oz = cell(cols, colOz);
            p.ordinalNo = oz.isEmpty() ? String.format("%04d", seq * 10) : oz;
            p.categoryPath = cell(cols, colCat);
            p.positionType = parseType(cell(cols, colType));
            p.quantity = parseDecimal(cell(cols, colQty), BigDecimal.ZERO);
            p.unit = cell(cols, colUnit);
            p.shortText = cell(cols, colShort);
            p.longText = cell(cols, colLong);
            p.unitPrice = parseDecimal(cell(cols, colUp), BigDecimal.ZERO);
            p.bidderText = cell(cols, colBidder);
            if (!p.shortText.isEmpty() || p.quantity.signum() > 0) {
                doc.positions.add(p);
                seq++;
            }
        }
        return doc;
    }

    @Override
    public byte[] exportBid(
            BoqDocument document,
            Map<String, BigDecimal> unitPricesByOrdinal,
            Map<String, String> bidderTextByOrdinal,
            String producerVersion) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ordinal;Category;Type;Quantity;Unit;ShortText;UnitPrice;LineTotal;BidderText\n");
        for (BoqPosition p : document.positions) {
            BigDecimal up = unitPricesByOrdinal != null && unitPricesByOrdinal.containsKey(p.ordinalNo)
                    ? unitPricesByOrdinal.get(p.ordinalNo)
                    : p.unitPrice;
            if (up == null) {
                up = BigDecimal.ZERO;
            }
            up = up.setScale(2, RoundingMode.HALF_UP);
            BigDecimal qty = p.quantity == null ? BigDecimal.ZERO : p.quantity;
            BigDecimal total = p.positionType == PositionType.TEXT
                    ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                    : qty.multiply(up).setScale(2, RoundingMode.HALF_UP);
            String bt = bidderTextByOrdinal != null && bidderTextByOrdinal.containsKey(p.ordinalNo)
                    ? bidderTextByOrdinal.get(p.ordinalNo)
                    : p.bidderText;

            sb.append(csv(p.ordinalNo)).append(';')
                    .append(csv(p.categoryPath)).append(';')
                    .append(p.positionType.name()).append(';')
                    .append(qty.toPlainString()).append(';')
                    .append(csv(p.unit)).append(';')
                    .append(csv(p.shortText)).append(';')
                    .append(up.toPlainString()).append(';')
                    .append(total.toPlainString()).append(';')
                    .append(csv(bt)).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static boolean looksLikeHeader(List<String> row) {
        String joined = String.join(" ", row).toLowerCase(Locale.ROOT);
        return joined.contains("oz") || joined.contains("ordinal") || joined.contains("menge")
                || joined.contains("quantity") || joined.contains("kurztext") || joined.contains("short");
    }

    private static int findCol(List<String> headers, String... keywords) {
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "");
            for (String kw : keywords) {
                if (h.equals(kw)) {
                    return i;
                }
            }
        }
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "");
            for (String kw : keywords) {
                if (kw.length() >= 3 && h.contains(kw)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static String cell(List<String> row, int idx) {
        return idx >= 0 && idx < row.size() ? row.get(idx).trim() : "";
    }

    private static PositionType parseType(String raw) {
        String s = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        if (s.startsWith("OPT") || s.startsWith("EVENT") || s.startsWith("BEDARF") || s.equals("EP")) {
            return PositionType.OPTIONAL;
        }
        if (s.startsWith("ALT") || s.startsWith("WAHL") || s.equals("AP")) {
            return PositionType.ALTERNATIVE;
        }
        if (s.startsWith("BASE") || s.startsWith("GRUND")) {
            return PositionType.BASE;
        }
        if (s.startsWith("TEXT") || s.startsWith("TITLE") || s.startsWith("TITEL")) {
            return PositionType.TEXT;
        }
        return PositionType.NORMAL;
    }

    private static BigDecimal parseDecimal(String raw, BigDecimal fallback) {
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        String cleaned = raw.trim().replaceAll("[^0-9.,-]", "");
        if (cleaned.contains(",") && cleaned.contains(".")) {
            cleaned = cleaned.lastIndexOf(',') > cleaned.lastIndexOf('.')
                    ? cleaned.replace(".", "").replace(',', '.')
                    : cleaned.replace(",", "");
        } else if (cleaned.contains(",")) {
            cleaned = cleaned.replace(',', '.');
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    static List<String> parseCsvLine(String line, char sep) {
        List<String> result = new ArrayList<String>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(ch);
                }
            } else {
                if (ch == '"') {
                    inQuotes = true;
                } else if (ch == sep) {
                    result.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(ch);
                }
            }
        }
        result.add(cur.toString());
        return result;
    }

    private static String csv(String s) {
        String v = s == null ? "" : s;
        if (v.contains(";") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    private static String stripExt(String fileName) {
        if (fileName == null) {
            return "BoQ";
        }
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}
