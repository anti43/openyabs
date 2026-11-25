/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Product;
import mpv5.globals.Messages;
import mpv5.utils.csv.CsvImport;
import mpv5.utils.csv.CsvImport.Field;

/**
 * Logic of the stock management: finding products by any code (number, EAN,
 * reference, name), splitting pasted lists, stock rows of a CSV file and the
 * export of a stocktaking (Inventur)
 */
public final class StockService {

    /**
     * The fields a scanned or typed code is looked up in, in this order
     */
    public static final String[] CODE_FIELDS = {"cnumber", "ean", "reference", "cname"};

    /**
     * Separators of a pasted list (line breaks and tabs always separate)
     */
    public static final String[] SEPARATORS = {",", ";", " ", "\n"};

    private StockService() {
    }

    // ------------------------------------------------------------ pasted lists
    /**
     * @param text a pasted list
     * @param separator one of {@link #SEPARATORS}; line breaks and tabs always
     * separate
     * @return the codes, trimmed, without empty ones
     */
    public static List<String> split(String text, String separator) {
        List<String> codes = new ArrayList<String>();
        if (text == null) {
            return codes;
        }
        String sep = separator == null || separator.isEmpty() ? "\n" : separator;
        for (String line : text.split("[\\r\\n\\t]+")) {
            String[] parts = " ".equals(sep) ? line.trim().split("\\s+")
                    : "\n".equals(sep) ? new String[]{line} : line.split(java.util.regex.Pattern.quote(sep));
            for (String p : parts) {
                String c = p.trim();
                if (!c.isEmpty()) {
                    codes.add(c);
                }
            }
        }
        return codes;
    }

    /**
     * @param text a pasted list
     * @return the likely separator: ; or , if they occur, a line break if the
     * text has several lines, else a space
     */
    public static String guessSeparator(String text) {
        if (text == null) {
            return "\n";
        }
        if (text.contains(";")) {
            return ";";
        }
        if (text.contains(",")) {
            return ",";
        }
        if (text.trim().contains("\n")) {
            return "\n";
        }
        return " ";
    }

    // ------------------------------------------------------------ lookup
    /**
     * Finds a product by its number, EAN (also written with spaces or dashes),
     * reference or name
     *
     * @param code
     * @return the product, null if none
     */
    public static Product find(String code) {
        String c = code == null ? "" : code.trim();
        if (c.isEmpty()) {
            return null;
        }
        String digits = c.replaceAll("[\\s\\-]", "");
        for (String field : CODE_FIELDS) {
            Product p = find(field, c);
            if (p == null && "ean".equals(field) && !digits.equals(c)) {
                p = find(field, digits);
            }
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    private static Product find(String field, String value) {
        try {
            return (Product) DatabaseObject.getObject(Context.getProduct(), field, value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * A new product for a code that was not found: the code is its name, and
     * its EAN if it is a valid EAN
     *
     * @param code
     * @return the product, not saved
     */
    public static Product newProduct(String code) {
        Product p = new Product();
        p.setInttype(Product.TYPE_PRODUCT);
        p.setDateadded(new Date());
        p.setCname(code == null ? "" : code.trim());
        String digits = code == null ? "" : code.replaceAll("[\\s\\-]", "");
        if (ProductService.isValidEan(digits)) {
            p.setEan(digits);
        }
        p.setStockvalue(BigDecimal.ZERO);
        p.setIntinventorytype(1);
        return p;
    }

    // ------------------------------------------------------------ CSV rows
    /**
     * @param row
     * @param mapping
     * @return the code of the row: number, EAN, reference or name (the first
     * mapped and not empty in this order), "" if none
     */
    public static String code(String[] row, Field[] mapping) {
        for (String key : CODE_FIELDS) {
            for (int col = 0; col < mapping.length; col++) {
                if (mapping[col] != null && key.equals(mapping[col].key)) {
                    String v = col < row.length && row[col] != null ? row[col].trim() : "";
                    if (!v.isEmpty()) {
                        return v;
                    }
                }
            }
        }
        return "";
    }

    /**
     * @param row
     * @param mapping
     * @return the quantity of the stock column, 1 if there is none or it is
     * empty
     * @throws NumberFormatException if it is not a number
     */
    public static BigDecimal quantity(String[] row, Field[] mapping) {
        for (int col = 0; col < mapping.length; col++) {
            if (mapping[col] != null && "stockvalue".equals(mapping[col].key)) {
                String v = col < row.length && row[col] != null ? row[col].trim() : "";
                return v.isEmpty() ? BigDecimal.ONE : CsvImport.number(v);
            }
        }
        return BigDecimal.ONE;
    }

    /**
     * @param row
     * @param mapping
     * @return the problems of a stock row (no code, quantity not a number),
     * empty if it can be taken
     */
    public static List<String> check(String[] row, Field[] mapping) {
        List<String> p = new ArrayList<String>();
        if (code(row, mapping).isEmpty()) {
            p.add(Messages.STOCK_NO_CODE.toString());
        }
        try {
            quantity(row, mapping);
        } catch (Exception e) {
            p.add(String.format(Messages.CSV_PROBLEM_NUMBER.toString(), Messages.STOCK_COL_QUANTITY, e.getMessage()));
        }
        return p;
    }

    /**
     * @param mapping
     * @return a problem of the mapping (no column to find the product by),
     * null if fine
     */
    public static String checkMapping(Field[] mapping) {
        for (Field f : mapping) {
            if (f != null) {
                for (String k : CODE_FIELDS) {
                    if (k.equals(f.key)) {
                        return null;
                    }
                }
            }
        }
        return Messages.STOCK_MAPPING_NO_CODE.toString();
    }

    /**
     * A new product from a CSV row (all mapped columns), stock 0: the
     * quantity is booked afterwards
     *
     * @param row
     * @param mapping
     * @return the product, not saved
     * @throws Exception
     */
    public static Product newProduct(String[] row, Field[] mapping) throws Exception {
        Product p = CsvImport.createProduct(row, mapping);
        if (p.__getCname() == null || p.__getCname().trim().isEmpty()) {
            p.setCname(code(row, mapping));
        }
        p.setStockvalue(BigDecimal.ZERO);
        p.setIntinventorytype(1);
        return p;
    }

    // ------------------------------------------------------------ stocktaking
    /**
     * A counted product of a stocktaking
     */
    public static final class Count {

        public final String number, name, ean, unit;
        public final BigDecimal expected, counted;

        public Count(String number, String name, String ean, String unit, BigDecimal expected, BigDecimal counted) {
            this.number = nvl(number);
            this.name = nvl(name);
            this.ean = nvl(ean);
            this.unit = nvl(unit);
            this.expected = expected == null ? BigDecimal.ZERO : expected;
            this.counted = counted;
        }

        /**
         * @return counted - expected, null if not counted
         */
        public BigDecimal difference() {
            return counted == null ? null : counted.subtract(expected);
        }
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String num(BigDecimal v) {
        return v == null ? "" : v.stripTrailingZeros().toPlainString().replace('.', ',');
    }

    private static String csv(String v, char sep) {
        String s = nvl(v);
        if (s.indexOf(sep) >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }

    /**
     * @param counts
     * @param sep the separator (;)
     * @return the stocktaking as CSV: number, name, EAN, unit, expected,
     * counted, difference (numbers with decimal comma)
     */
    public static String inventoryCsv(List<Count> counts, char sep) {
        StringBuilder b = new StringBuilder();
        String[] head = {Messages.CSV_F_PRODUCT_NUMBER.toString(), Messages.CSV_F_PRODUCT_NAME.toString(), Messages.CSV_F_EAN.toString(),
            Messages.CSV_F_MEASURE.toString(), Messages.STOCK_COL_EXPECTED.toString(), Messages.STOCK_COL_COUNTED.toString(),
            Messages.STOCK_COL_DIFF.toString()};
        for (int i = 0; i < head.length; i++) {
            b.append(i > 0 ? String.valueOf(sep) : "").append(csv(head[i], sep));
        }
        b.append("\r\n");
        for (Count c : counts) {
            String[] v = {c.number, c.name, c.ean, c.unit, num(c.expected), num(c.counted), num(c.difference())};
            for (int i = 0; i < v.length; i++) {
                b.append(i > 0 ? String.valueOf(sep) : "").append(csv(v[i], sep));
            }
            b.append("\r\n");
        }
        return b.toString();
    }

    private static String html(String s) {
        return nvl(s).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /**
     * @param counts
     * @param title e.g. "Inventur 25.11.2026"
     * @return the stocktaking as printable HTML page; differences marked
     */
    public static String inventoryHtml(List<Count> counts, String title) {
        StringBuilder b = new StringBuilder();
        b.append("<!DOCTYPE html>\n<html><head><meta charset=\"utf-8\"><title>").append(html(title)).append("</title>\n")
                .append("<style>body{font-family:sans-serif;margin:24px}table{border-collapse:collapse;width:100%}")
                .append("th,td{border:1px solid #bbb;padding:4px 8px;text-align:left}td.n{text-align:right}")
                .append("tr.d td{background:#fde7e7}tr.u td{color:#888}</style></head><body>\n<h1>")
                .append(html(title)).append("</h1>\n<table>\n<tr>");
        for (String h : new String[]{Messages.CSV_F_PRODUCT_NUMBER.toString(), Messages.CSV_F_PRODUCT_NAME.toString(),
            Messages.CSV_F_EAN.toString(), Messages.CSV_F_MEASURE.toString(), Messages.STOCK_COL_EXPECTED.toString(),
            Messages.STOCK_COL_COUNTED.toString(), Messages.STOCK_COL_DIFF.toString()}) {
            b.append("<th>").append(html(h)).append("</th>");
        }
        b.append("</tr>\n");
        int diffs = 0;
        int uncounted = 0;
        for (Count c : counts) {
            BigDecimal d = c.difference();
            boolean diff = d != null && d.signum() != 0;
            diffs += diff ? 1 : 0;
            uncounted += c.counted == null ? 1 : 0;
            b.append("<tr").append(diff ? " class=\"d\"" : c.counted == null ? " class=\"u\"" : "").append("><td>")
                    .append(html(c.number)).append("</td><td>").append(html(c.name)).append("</td><td>").append(html(c.ean))
                    .append("</td><td>").append(html(c.unit)).append("</td><td class=\"n\">").append(num(c.expected))
                    .append("</td><td class=\"n\">").append(num(c.counted)).append("</td><td class=\"n\">").append(num(d))
                    .append("</td></tr>\n");
        }
        b.append("</table>\n<p>").append(html(String.format(Messages.STOCK_INVENTORY_SUMMARY.toString(), counts.size(), diffs, uncounted)))
                .append("</p>\n</body></html>\n");
        return b.toString();
    }
}
