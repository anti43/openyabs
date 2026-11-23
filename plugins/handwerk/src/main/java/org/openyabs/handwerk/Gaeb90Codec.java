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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import org.openyabs.handwerk.BoqDocument.BoqPosition;
import org.openyabs.handwerk.BoqDocument.PositionType;

/**
 * Legacy GAEB 90 fixed-width text codec supporting {@code .D81}, {@code .D83},
 * {@code .D84}, and {@code .D86} import as well as {@code .D84} priced bid
 * export.
 * <p>
 * Handles core GAEB 90 record types: {@code 00} (header), {@code 01} (project),
 * {@code 11}/{@code 12} (category/group headers), {@code 21} (item header with
 * OZ, item type, quantity &amp; unit), {@code 25} (short text), {@code 26}
 * (long text), {@code 27}/{@code 28} (unit/total prices), and {@code 99}
 * (trailer).
 */
public final class Gaeb90Codec implements BoqCodec {

    private static final Charset GAEB90_CHARSET = StandardCharsets.ISO_8859_1;

    @Override
    public String id() {
        return "GAEB_90";
    }

    @Override
    public boolean canRead(String fileName, String content) {
        String lower = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (lower.matches(".*\\.d8[0-9]$")) {
            return true;
        }
        if (content != null) {
            String trimmed = content.trim();
            return trimmed.startsWith("00") && (trimmed.contains("GAEB") || trimmed.contains("83") || trimmed.contains("81"));
        }
        return false;
    }

    @Override
    public boolean canExportBid(String formatCode) {
        return "GAEB_90".equalsIgnoreCase(formatCode);
    }

    @Override
    public String bidFileExtension() {
        return "D84";
    }

    @Override
    public BoqDocument read(String fileName, String content) {
        BoqDocument doc = new BoqDocument();
        doc.formatCode = "GAEB_90";
        doc.rawSource = content == null ? "" : content;
        doc.importedAt = LocalDate.now().toString();
        doc.currency = "EUR";
        if (fileName != null && fileName.toLowerCase(Locale.ROOT).matches(".*\\.d\\d{2}$")) {
            doc.exchangePhase = fileName.substring(fileName.length() - 2);
        } else {
            doc.exchangePhase = "83";
        }

        String[] lines = (content == null ? "" : content).replace("\r\n", "\n").split("\n");
        String currentCategory = "";
        BoqPosition current = null;
        StringBuilder longBuf = new StringBuilder();

        for (String raw : lines) {
            if (raw.length() < 2) {
                continue;
            }
            String rec = raw.substring(0, 2);
            if ("00".equals(rec)) {
                String dp = slice(raw, 2, 4).trim();
                if (dp.matches("8[0-9]")) {
                    doc.exchangePhase = dp;
                }
            } else if ("01".equals(rec)) {
                String title = slice(raw, 2, 62).trim();
                if (!title.isEmpty() && doc.projectName.isEmpty()) {
                    doc.projectName = title;
                    doc.boqLabel = title;
                }
            } else if ("11".equals(rec) || "12".equals(rec)) {
                flushLongText(current, longBuf);
                current = null;
                String oz = slice(raw, 2, 11).trim();
                String title = slice(raw, 11, 72).trim();
                currentCategory = (oz + (title.isEmpty() ? "" : " " + title)).trim();
                BoqPosition header = new BoqPosition();
                header.ordinalNo = oz;
                header.positionType = PositionType.TEXT;
                header.shortText = title.isEmpty() ? oz : title;
                doc.positions.add(header);
            } else if ("21".equals(rec)) {
                flushLongText(current, longBuf);
                current = new BoqPosition();
                current.ordinalNo = slice(raw, 2, 11).trim();
                current.categoryPath = currentCategory;
                String kindFlag = slice(raw, 11, 13).trim().toUpperCase(Locale.ROOT);
                if (kindFlag.contains("E") || kindFlag.contains("B")) {
                    current.positionType = PositionType.OPTIONAL;
                } else if (kindFlag.contains("A")) {
                    current.positionType = PositionType.ALTERNATIVE;
                } else {
                    current.positionType = PositionType.NORMAL;
                }
                // Standard GAEB 90: quantity in cols 23..34 (implied 3 decimals if pure digits) and unit in cols 34..38
                String qtyRaw = slice(raw, 23, 34).trim();
                String unitRaw = slice(raw, 34, 40).trim();
                current.quantity = parseGaeb90Qty(qtyRaw);
                current.unit = unitRaw;
                doc.positions.add(current);
            } else if ("25".equals(rec) && current != null) {
                String txt = slice(raw, 2, 72).trim();
                if (!txt.isEmpty()) {
                    current.shortText = current.shortText.isEmpty() ? txt : current.shortText + " " + txt;
                }
            } else if ("26".equals(rec) && current != null) {
                String txt = slice(raw, 2, 72).trim();
                if (!txt.isEmpty()) {
                    if (longBuf.length() > 0) {
                        longBuf.append("\n");
                    }
                    longBuf.append(txt);
                }
            } else if ("27".equals(rec) && current != null) {
                String upRaw = slice(raw, 2, 15).trim();
                current.unitPrice = parseGaeb90Money(upRaw);
            }
        }
        flushLongText(current, longBuf);

        if (doc.projectName.isEmpty()) {
            doc.projectName = fileName == null ? "GAEB 90" : fileName;
            doc.boqLabel = doc.projectName;
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
        int lineNo = 1;
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyy"));
        sb.append(formatRecord("00", "84GAEB90" + date + "OpenYaBS " + safe(producerVersion), lineNo++)).append("\r\n");
        sb.append(formatRecord("01", safe(document.projectName), lineNo++)).append("\r\n");

        BigDecimal grandTotal = BigDecimal.ZERO;
        for (BoqPosition p : document.positions) {
            if (p.positionType == PositionType.TEXT) {
                continue;
            }
            BigDecimal up = unitPricesByOrdinal != null && unitPricesByOrdinal.containsKey(p.ordinalNo)
                    ? unitPricesByOrdinal.get(p.ordinalNo)
                    : p.unitPrice;
            if (up == null) {
                up = BigDecimal.ZERO;
            }
            up = up.setScale(2, RoundingMode.HALF_UP);
            BigDecimal qty = p.quantity == null ? BigDecimal.ZERO : p.quantity;
            BigDecimal it = qty.multiply(up).setScale(2, RoundingMode.HALF_UP);
            if (p.isCountedInTotal()) {
                grandTotal = grandTotal.add(it);
            }

            // Record 21: OZ (9 chars) + padding + price in cents (11 digits)
            long upCents = up.movePointRight(2).longValue();
            long itCents = it.movePointRight(2).longValue();
            String rec21Body = padRight(p.ordinalNo, 9) + "  " + padLeft(String.valueOf(upCents), 11)
                    + padLeft(String.valueOf(itCents), 12);
            sb.append(formatRecord("21", rec21Body, lineNo++)).append("\r\n");

            if (bidderTextByOrdinal != null) {
                String bt = bidderTextByOrdinal.get(p.ordinalNo);
                if (bt != null && !bt.trim().isEmpty()) {
                    sb.append(formatRecord("26", bt.trim(), lineNo++)).append("\r\n");
                }
            }
        }
        long grandCents = grandTotal.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValue();
        sb.append(formatRecord("99", padLeft(String.valueOf(grandCents), 14), lineNo)).append("\r\n");
        return sb.toString().getBytes(GAEB90_CHARSET);
    }

    private static void flushLongText(BoqPosition current, StringBuilder buf) {
        if (current != null && buf.length() > 0) {
            current.longText = buf.toString();
            if (current.shortText.isEmpty()) {
                current.shortText = current.longText.length() > 80 ? current.longText.substring(0, 80) + "…" : current.longText;
            }
        }
        buf.setLength(0);
    }

    private static BigDecimal parseGaeb90Qty(String raw) {
        if (raw == null || raw.isEmpty()) {
            return BigDecimal.ZERO;
        }
        if (raw.contains(".") || raw.contains(",")) {
            try {
                return new BigDecimal(raw.replace(',', '.'));
            } catch (NumberFormatException ignored) {
                return BigDecimal.ZERO;
            }
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(digits).movePointLeft(3);
    }

    private static BigDecimal parseGaeb90Money(String raw) {
        if (raw == null || raw.isEmpty()) {
            return BigDecimal.ZERO;
        }
        if (raw.contains(".") || raw.contains(",")) {
            try {
                return new BigDecimal(raw.replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException ignored) {
                return BigDecimal.ZERO;
            }
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(digits).movePointLeft(2);
    }

    private static String slice(String line, int start, int end) {
        if (line == null || start >= line.length()) {
            return "";
        }
        return line.substring(start, Math.min(end, line.length()));
    }

    private static String formatRecord(String type, String body, int lineNo) {
        String content = type + padRight(body == null ? "" : body, 70);
        if (content.length() > 74) {
            content = content.substring(0, 74);
        }
        return padRight(content, 74) + String.format("%06d", lineNo % 1000000);
    }

    private static String padRight(String s, int width) {
        String v = s == null ? "" : s;
        if (v.length() >= width) {
            return v.substring(0, width);
        }
        StringBuilder sb = new StringBuilder(width);
        sb.append(v);
        while (sb.length() < width) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private static String padLeft(String s, int width) {
        String v = s == null ? "" : s;
        if (v.length() >= width) {
            return v.substring(v.length() - width);
        }
        StringBuilder sb = new StringBuilder(width);
        while (sb.length() + v.length() < width) {
            sb.append('0');
        }
        sb.append(v);
        return sb.toString();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
