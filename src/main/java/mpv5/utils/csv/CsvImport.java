/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.csv;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Group;
import mpv5.db.objects.Product;
import mpv5.db.objects.Tax;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.logging.Log;

/**
 * Reads a CSV file and imports its rows as contacts or products: detects
 * encoding and separator, guesses the column mapping from the header (German
 * and English names), checks every row before anything is imported and says
 * what was imported and what was skipped (and why).
 */
public final class CsvImport {

    /**
     * What the rows become
     */
    public enum Kind {
        CUSTOMERS, SUPPLIERS, MANUFACTURERS, PRODUCTS;

        public boolean isContact() {
            return this != PRODUCTS;
        }

        @Override
        public String toString() {
            switch (this) {
                case CUSTOMERS:
                    return Messages.CSV_KIND_CUSTOMERS.toString();
                case SUPPLIERS:
                    return Messages.CSV_KIND_SUPPLIERS.toString();
                case MANUFACTURERS:
                    return Messages.CSV_KIND_MANUFACTURERS.toString();
                default:
                    return Messages.CSV_KIND_PRODUCTS.toString();
            }
        }
    }

    /**
     * The type of a field: how a value is checked and converted
     */
    public enum Type {
        TEXT, NUMBER, DATE, MAIL, TAX, SUPPLIER, MANUFACTURER, GROUP
    }

    /**
     * A field of a contact or product a column can be imported as
     */
    public static final class Field {

        /**
         * the setter key (see DatabaseObject#setVars), or a special key
         */
        public final String key;
        public final Type type;
        private final Messages label;
        final String[] names;

        Field(String key, Type type, Messages label, String... names) {
            this.key = key;
            this.type = type;
            this.label = label;
            this.names = names;
        }

        @Override
        public String toString() {
            return label.toString();
        }
    }

    /**
     * A problem of a row: the row is not imported
     */
    public static final class Problem {

        /**
         * the line in the file (1 = first line)
         */
        public final int line;
        public final String text;

        public Problem(int line, String text) {
            this.line = line;
            this.text = text;
        }

        @Override
        public String toString() {
            return String.format(Messages.CSV_LINE.toString(), line) + ": " + text;
        }
    }

    /**
     * What an import did
     */
    public static final class Result {

        public int imported;
        public int empty;
        public final List<Problem> skipped = new ArrayList<Problem>();
        public final List<Integer> ids = new ArrayList<Integer>();
    }

    private static final Field[] CONTACT_FIELDS = {
        new Field("cname", Type.TEXT, Messages.CSV_F_NAME, "name", "nachname", "surname", "lastname", "last name", "familienname", "cname", "kunde", "kundenname", "customer", "customer name"),
        new Field("prename", Type.TEXT, Messages.CSV_F_PRENAME, "vorname", "firstname", "first name", "prename", "given name", "rufname"),
        new Field("title", Type.TEXT, Messages.CSV_F_TITLE, "anrede", "titel", "title", "salutation"),
        new Field("company", Type.TEXT, Messages.CSV_F_COMPANY, "firma", "company", "unternehmen", "firmenname", "organisation", "organization", "company name"),
        new Field("department", Type.TEXT, Messages.CSV_F_DEPARTMENT, "abteilung", "department"),
        new Field("street", Type.TEXT, Messages.CSV_F_STREET, "strasse", "str", "street", "adresse", "anschrift", "address", "strasse und hausnummer", "address line 1"),
        new Field("zip", Type.TEXT, Messages.CSV_F_ZIP, "plz", "postleitzahl", "zip", "zipcode", "zip code", "postcode", "postal code"),
        new Field("city", Type.TEXT, Messages.CSV_F_CITY, "ort", "stadt", "city", "town", "wohnort"),
        new Field("country", Type.TEXT, Messages.CSV_F_COUNTRY, "land", "country", "staat"),
        new Field("mainphone", Type.TEXT, Messages.CSV_F_PHONE, "telefon", "tel", "phone", "telephone", "telefonnummer", "phone number", "festnetz"),
        new Field("workphone", Type.TEXT, Messages.CSV_F_WORKPHONE, "telefon geschaeftlich", "telefon dienstlich", "work phone", "business phone", "buero"),
        new Field("mobilephone", Type.TEXT, Messages.CSV_F_MOBILE, "mobil", "mobiltelefon", "handy", "mobile", "mobile phone", "cell", "cell phone"),
        new Field("fax", Type.TEXT, Messages.CSV_F_FAX, "fax", "telefax"),
        new Field("mailaddress", Type.MAIL, Messages.CSV_F_MAIL, "email", "e mail", "mail", "mailadresse", "email address", "e mail adresse", "mailaddress"),
        new Field("website", Type.TEXT, Messages.CSV_F_WEBSITE, "website", "webseite", "homepage", "internet", "web", "url"),
        new Field("cnumber", Type.TEXT, Messages.CSV_F_NUMBER, "kundennummer", "kundennr", "kdnr", "lieferantennummer", "nummer", "number", "customer number", "customer no", "nr"),
        new Field("taxnumber", Type.TEXT, Messages.CSV_F_TAXNUMBER, "ust idnr", "ustidnr", "ust id", "ustid", "steuernummer", "vat", "vat id", "vat number", "tax number", "tax id"),
        new Field("bankname", Type.TEXT, Messages.CSV_F_BANKNAME, "bank", "bankname", "kreditinstitut"),
        new Field("bankid", Type.TEXT, Messages.CSV_F_BANKID, "bic", "blz", "bankleitzahl", "swift"),
        new Field("bankaccount", Type.TEXT, Messages.CSV_F_BANKACCOUNT, "iban", "konto", "kontonummer", "account", "bank account"),
        new Field("notes", Type.TEXT, Messages.CSV_F_NOTES, "notizen", "notiz", "bemerkung", "bemerkungen", "kommentar", "notes", "note", "comment", "comments"),
        new Field("dateadded", Type.DATE, Messages.CSV_F_DATE, "angelegt", "angelegt am", "erstellt", "erstellt am", "datum", "date", "created", "date added", "kunde seit", "customer since", "seit"),
        new Field("GROUP", Type.GROUP, Messages.CSV_F_GROUP, "gruppe", "group", "kategorie", "category")
    };

    private static final Field[] PRODUCT_FIELDS = {
        new Field("cname", Type.TEXT, Messages.CSV_F_PRODUCT_NAME, "name", "bezeichnung", "artikel", "artikelname", "artikelbezeichnung", "produkt", "produktname", "product", "product name", "title", "titel", "kurztext"),
        new Field("cnumber", Type.TEXT, Messages.CSV_F_PRODUCT_NUMBER, "artikelnummer", "artikelnr", "art nr", "artnr", "nummer", "number", "sku", "item number", "product number", "nr"),
        new Field("description", Type.TEXT, Messages.CSV_F_DESCRIPTION, "beschreibung", "description", "langtext", "text", "details"),
        new Field("externalnetvalue", Type.NUMBER, Messages.CSV_F_SALEPRICE, "verkaufspreis", "vk", "vk netto", "verkaufspreis netto", "preis", "preis netto", "nettopreis", "price", "net price", "sale price", "selling price", "unit price"),
        new Field("internalnetvalue", Type.NUMBER, Messages.CSV_F_BUYPRICE, "einkaufspreis", "ek", "ek netto", "einkaufspreis netto", "purchase price", "buy price", "cost", "cost price"),
        new Field("TAX", Type.TAX, Messages.CSV_F_TAX, "mwst", "mwst satz", "ust", "ust satz", "steuer", "steuersatz", "tax", "tax rate", "vat", "vat rate"),
        new Field("measure", Type.TEXT, Messages.CSV_F_MEASURE, "einheit", "mengeneinheit", "me", "unit", "measure", "uom"),
        new Field("ean", Type.TEXT, Messages.CSV_F_EAN, "ean", "gtin", "barcode", "ean code"),
        new Field("reference", Type.TEXT, Messages.CSV_F_REFERENCE, "referenz", "reference", "herstellernummer", "herstellerartikelnummer", "mpn"),
        new Field("url", Type.TEXT, Messages.CSV_F_URL, "url", "link", "webseite", "website"),
        new Field("stockvalue", Type.NUMBER, Messages.CSV_F_STOCK, "bestand", "lagerbestand", "stock", "menge", "quantity", "qty", "lager"),
        new Field("thresholdvalue", Type.NUMBER, Messages.CSV_F_THRESHOLD, "mindestbestand", "meldebestand", "min stock", "minimum stock", "threshold"),
        new Field("SUPPLIER", Type.SUPPLIER, Messages.CSV_F_SUPPLIER, "lieferant", "supplier", "vendor"),
        new Field("MANUFACTURER", Type.MANUFACTURER, Messages.CSV_F_MANUFACTURER, "hersteller", "manufacturer", "marke", "brand"),
        new Field("dateadded", Type.DATE, Messages.CSV_F_DATE, "angelegt", "angelegt am", "erstellt", "erstellt am", "datum", "date", "created", "date added"),
        new Field("GROUP", Type.GROUP, Messages.CSV_F_GROUP, "gruppe", "group", "warengruppe", "kategorie", "category")
    };

    private static final Pattern MAIL = Pattern.compile("^[^@\\s;,]+@[^@\\s;,]+\\.[^@\\s;,]+$");
    private static final char[] SEPARATORS = {';', ',', '\t', '|'};

    private CsvImport() {
    }

    /**
     * @param kind
     * @return the fields a column can be imported as
     */
    public static List<Field> fields(Kind kind) {
        return Arrays.asList(kind.isContact() ? CONTACT_FIELDS : PRODUCT_FIELDS);
    }

    // ------------------------------------------------------------ reading
    /**
     * @param bytes the file
     * @return UTF-8 (also with BOM) if the bytes are valid UTF-8, else
     * Windows-1252 (Excel on Windows)
     */
    public static Charset detectCharset(byte[] bytes) {
        if (bytes.length >= 2 && ((bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE) || (bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF))) {
            return StandardCharsets.UTF_16;
        }
        try {
            StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes));
            return StandardCharsets.UTF_8;
        } catch (CharacterCodingException e) {
            return Charset.forName("windows-1252");
        }
    }

    /**
     * @param bytes
     * @param cs
     * @return the text, without byte order mark
     */
    public static String decode(byte[] bytes, Charset cs) {
        String s = new String(bytes, cs);
        if (!s.isEmpty() && s.charAt(0) == '﻿') {
            s = s.substring(1);
        }
        return s;
    }

    /**
     * @param text
     * @return the separator which splits the first lines into the same (and
     * most) columns: ; , tab or |
     */
    public static char detectSeparator(String text) {
        char best = ';';
        double bestScore = -1;
        for (char sep : SEPARATORS) {
            List<String[]> rows = parse(text, sep, 20);
            if (rows.isEmpty()) {
                continue;
            }
            int cols = rows.get(0).length;
            if (cols < 2) {
                continue;
            }
            int same = 0;
            for (String[] r : rows) {
                if (r.length == cols) {
                    same++;
                }
            }
            double score = (double) same / rows.size() * 100 + cols;
            if (score > bestScore) {
                bestScore = score;
                best = sep;
            }
        }
        return best;
    }

    /**
     * Parses CSV: fields in "quotes" may contain the separator, line breaks
     * and "" for a quote
     *
     * @param text
     * @param sep
     * @param maxRows at most this many rows, &lt;= 0: all
     * @return the rows (empty lines are kept as one empty field)
     */
    public static List<String[]> parse(String text, char sep, int maxRows) {
        List<String[]> rows = new ArrayList<String[]>();
        List<String> row = new ArrayList<String>();
        StringBuilder f = new StringBuilder();
        boolean quoted = false;
        boolean any = false;
        int n = text.length();
        for (int i = 0; i < n; i++) {
            char c = text.charAt(i);
            any = true;
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < n && text.charAt(i + 1) == '"') {
                        f.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    f.append(c);
                }
            } else if (c == '"' && f.toString().trim().isEmpty()) {
                f.setLength(0);
                quoted = true;
            } else if (c == sep) {
                row.add(f.toString());
                f.setLength(0);
            } else if (c == '\r' || c == '\n') {
                if (c == '\r' && i + 1 < n && text.charAt(i + 1) == '\n') {
                    i++;
                }
                row.add(f.toString());
                f.setLength(0);
                rows.add(row.toArray(new String[0]));
                row = new ArrayList<String>();
                any = false;
                if (maxRows > 0 && rows.size() >= maxRows) {
                    return rows;
                }
            } else {
                f.append(c);
            }
        }
        if (any || f.length() > 0 || !row.isEmpty()) {
            row.add(f.toString());
            rows.add(row.toArray(new String[0]));
        }
        return rows;
    }

    /**
     * A CSV file, read
     */
    public static final class Data {

        public Charset charset;
        public char separator;
        public boolean header = true;
        public List<String[]> rows = new ArrayList<String[]>();
        byte[] bytes;

        /**
         * @return the number of columns (the longest row)
         */
        public int columns() {
            int c = 0;
            for (String[] r : rows) {
                c = Math.max(c, r.length);
            }
            return c;
        }

        /**
         * @param col
         * @return the header of the column, or "Column n"
         */
        public String columnName(int col) {
            if (header && !rows.isEmpty() && col < rows.get(0).length && !rows.get(0)[col].trim().isEmpty()) {
                return rows.get(0)[col].trim();
            }
            return String.format(Messages.CSV_COLUMN.toString(), col + 1);
        }

        /**
         * @return the index of the first data row
         */
        public int firstData() {
            return header ? 1 : 0;
        }

        /**
         * Reads the file again with other settings
         *
         * @param cs
         * @param sep
         */
        public void reparse(Charset cs, char sep) {
            charset = cs;
            separator = sep;
            rows = parse(decode(bytes, cs), sep, 0);
            //a last empty line is no row
            while (!rows.isEmpty() && isEmpty(rows.get(rows.size() - 1))) {
                rows.remove(rows.size() - 1);
            }
        }
    }

    /**
     * Reads a file: encoding and separator detected
     *
     * @param f
     * @return the data
     * @throws IOException
     */
    public static Data read(File f) throws IOException {
        return read(Files.readAllBytes(f.toPath()));
    }

    /**
     * @param bytes
     * @return the data, encoding and separator detected
     */
    public static Data read(byte[] bytes) {
        Data d = new Data();
        d.bytes = bytes;
        Charset cs = detectCharset(bytes);
        d.reparse(cs, detectSeparator(decode(bytes, cs)));
        return d;
    }

    // ------------------------------------------------------------ mapping
    /**
     * @param s
     * @return lower case, umlauts written out, only letters, digits and single
     * spaces
     */
    static String normalize(String s) {
        String t = s == null ? "" : s.toLowerCase(Locale.ROOT).trim()
                .replace("\u00e4", "ae").replace("\u00f6", "oe").replace("\u00fc", "ue").replace("\u00df", "ss");
        t = Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return t.replaceAll("[^a-z0-9]+", " ").trim();
    }

    /**
     * Guesses the field of every column from its header
     *
     * @param headers the column names
     * @param kind
     * @return per column the field, or null (not imported)
     */
    public static Field[] guess(String[] headers, Kind kind) {
        List<Field> fields = fields(kind);
        Field[] result = new Field[headers.length];
        int[] scores = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            String h = normalize(headers[i]);
            if (h.isEmpty()) {
                continue;
            }
            for (Field f : fields) {
                int s = score(h, f);
                if (s > scores[i]) {
                    scores[i] = s;
                    result[i] = f;
                }
            }
        }
        //every field once: the best column wins
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result.length; j++) {
                if (i != j && result[i] != null && result[i] == result[j]) {
                    if (scores[j] > scores[i] || (scores[j] == scores[i] && j < i)) {
                        result[i] = null;
                    }
                }
            }
        }
        return result;
    }

    private static int score(String header, Field f) {
        int best = 0;
        List<String> names = new ArrayList<String>(Arrays.asList(f.names));
        names.add(normalize(f.toString()));
        names.add(f.key.toLowerCase(Locale.ROOT));
        for (String n : names) {
            String nn = normalize(n);
            if (nn.isEmpty()) {
                continue;
            }
            if (header.equals(nn)) {
                best = Math.max(best, 100);
            } else if (nn.length() >= 3 && (" " + header + " ").contains(" " + nn + " ")) {
                best = Math.max(best, 50 + nn.length());
            } else if (nn.length() >= 4 && header.replace(" ", "").contains(nn.replace(" ", ""))) {
                best = Math.max(best, 20 + nn.length());
            }
        }
        return best;
    }

    // ------------------------------------------------------------ values
    /**
     * Reads a number as written in Germany or in English: 1.234,56 1,234.56
     * 1234,5 12% 9,90 €
     *
     * @param s
     * @return the number
     * @throws NumberFormatException
     */
    public static BigDecimal number(String s) {
        String t = s == null ? "" : s.trim().replace(" ", "").replace(" ", "")
                .replace("\u20ac", "").replace("EUR", "").replace("eur", "").replace("$", "").replace("%", "").replace("'", "");
        if (t.isEmpty()) {
            throw new NumberFormatException("empty");
        }
        int comma = t.lastIndexOf(',');
        int dot = t.lastIndexOf('.');
        if (comma >= 0 && dot >= 0) {
            if (comma > dot) {
                t = t.replace(".", "").replace(',', '.');
            } else {
                t = t.replace(",", "");
            }
        } else if (comma >= 0) {
            t = t.indexOf(',') != comma ? t.replace(",", "") : t.replace(',', '.');
        } else if (dot >= 0 && t.indexOf('.') != dot) {
            //1.234.567
            t = t.replace(".", "");
        }
        return new BigDecimal(t);
    }

    private static final String[] DATE_FORMATS = {"dd.MM.yyyy", "d.M.yyyy", "dd.MM.yy", "yyyy-MM-dd", "dd/MM/yyyy", "d/M/yyyy", "dd-MM-yyyy", "yyyy-MM-dd HH:mm:ss", "dd.MM.yyyy HH:mm"};

    /**
     * @param s
     * @return the date, e.g. 31.12.2025, 2025-12-31, 31/12/2025
     * @throws ParseException
     */
    public static Date date(String s) throws ParseException {
        String t = s == null ? "" : s.trim();
        for (String f : DATE_FORMATS) {
            SimpleDateFormat df = new SimpleDateFormat(f);
            df.setLenient(false);
            java.text.ParsePosition p = new java.text.ParsePosition(0);
            Date d = df.parse(t, p);
            if (d != null && p.getIndex() == t.length()) {
                return d;
            }
        }
        throw new ParseException(t, 0);
    }

    /**
     * @param row a row of the file
     * @return true if all fields are empty
     */
    public static boolean isEmpty(String[] row) {
        for (String s : row) {
            if (s != null && !s.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static String value(String[] row, int col) {
        return col < row.length && row[col] != null ? row[col].trim() : "";
    }

    /**
     * Checks a row
     *
     * @param row
     * @param mapping per column the field or null
     * @param kind
     * @return the problems, empty if the row can be imported
     */
    public static List<String> check(String[] row, Field[] mapping, Kind kind) {
        return check(row, mapping, kind, null);
    }

    /**
     * Checks a row; a number already used (in the database or in an earlier
     * row) is a problem: the record exists already
     *
     * @param row
     * @param mapping per column the field or null
     * @param kind
     * @param taken the numbers in use (lower case), may be null; the number of
     * a row without problems is added
     * @return the problems, empty if the row can be imported
     */
    public static List<String> check(String[] row, Field[] mapping, Kind kind, java.util.Set<String> taken) {
        List<String> problems = new ArrayList<String>();
        String number = "";
        String name = "";
        String company = "";
        for (int c = 0; c < mapping.length; c++) {
            Field f = mapping[c];
            if (f == null) {
                continue;
            }
            String v = value(row, c);
            if (f.key.equals("cname")) {
                name = v;
            } else if (f.key.equals("cnumber")) {
                number = v;
            } else if (f.key.equals("company")) {
                company = v;
            }
            if (v.isEmpty()) {
                continue;
            }
            switch (f.type) {
                case NUMBER:
                case TAX:
                    try {
                        BigDecimal b = number(v);
                        if (f.type == Type.TAX && (b.signum() < 0 || b.compareTo(new BigDecimal(100)) > 0)) {
                            problems.add(String.format(Messages.CSV_PROBLEM_NUMBER.toString(), f, v));
                        }
                    } catch (Exception e) {
                        problems.add(String.format(Messages.CSV_PROBLEM_NUMBER.toString(), f, v));
                    }
                    break;
                case DATE:
                    try {
                        date(v);
                    } catch (Exception e) {
                        problems.add(String.format(Messages.CSV_PROBLEM_DATE.toString(), f, v));
                    }
                    break;
                case MAIL:
                    if (!MAIL.matcher(v).matches()) {
                        problems.add(String.format(Messages.CSV_PROBLEM_MAIL.toString(), v));
                    }
                    break;
                default:
                    if (v.length() > 250) {
                        problems.add(String.format(Messages.CSV_PROBLEM_LONG.toString(), f));
                    }
            }
        }
        if (kind.isContact() ? (name.isEmpty() && company.isEmpty()) : name.isEmpty()) {
            problems.add(0, kind.isContact() ? Messages.CSV_PROBLEM_NO_NAME.toString() : Messages.CSV_PROBLEM_NO_PRODUCT_NAME.toString());
        }
        if (taken != null && !number.isEmpty()) {
            String key = number.toLowerCase(Locale.ROOT);
            if (taken.contains(key)) {
                problems.add(String.format(Messages.CSV_PROBLEM_EXISTS.toString(), number));
            } else if (problems.isEmpty()) {
                taken.add(key);
            }
        }
        return problems;
    }

    /**
     * @param kind
     * @return the numbers of the contacts (or products) in the database, lower
     * case
     */
    public static java.util.Set<String> existingNumbers(Kind kind) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        try {
            Object[][] rows = mpv5.db.common.QueryHandler.instanceOf().clone(kind.isContact() ? Context.getContact() : Context.getProduct())
                    .select("cnumber", (String[]) null);
            for (Object[] r : rows) {
                if (r[0] != null && !String.valueOf(r[0]).trim().isEmpty()) {
                    s.add(String.valueOf(r[0]).trim().toLowerCase(Locale.ROOT));
                }
            }
        } catch (Exception e) {
            Log.Debug(CsvImport.class, e);
        }
        return s;
    }

    /**
     * @param mapping
     * @param kind
     * @return what is missing in the mapping, null if it is fine
     */
    public static String checkMapping(Field[] mapping, Kind kind) {
        boolean name = false;
        boolean company = false;
        for (Field f : mapping) {
            if (f != null) {
                name |= f.key.equals("cname");
                company |= f.key.equals("company");
            }
        }
        if (kind.isContact() ? !(name || company) : !name) {
            return kind.isContact() ? Messages.CSV_MAPPING_NO_NAME.toString() : Messages.CSV_MAPPING_NO_PRODUCT_NAME.toString();
        }
        return null;
    }

    /**
     * @param v
     * @param f
     * @return the value as it will be imported (for the preview)
     */
    public static String display(String v, Field f) {
        if (v == null || v.trim().isEmpty() || f == null) {
            return v == null ? "" : v.trim();
        }
        try {
            switch (f.type) {
                case NUMBER:
                    return mpv5.utils.numberformat.FormatNumber.formatDezimal(number(v));
                case TAX:
                    return number(v).stripTrailingZeros().toPlainString() + " %";
                case DATE:
                    return new SimpleDateFormat("dd.MM.yyyy").format(date(v));
                default:
                    return v.trim();
            }
        } catch (Exception e) {
            return v.trim();
        }
    }

    // ------------------------------------------------------------ import
    /**
     * Imports the rows without problems; rows with problems and empty rows are
     * skipped
     *
     * @param data
     * @param mapping
     * @param kind
     * @param progress called with the number of rows done, may be null
     * @return what was imported and skipped
     */
    public static Result importRows(Data data, Field[] mapping, Kind kind, java.util.function.IntConsumer progress) {
        Result r = new Result();
        java.util.Map<String, Integer> taxes = new java.util.HashMap<String, Integer>();
        java.util.Set<String> taken = existingNumbers(kind);
        for (int i = data.firstData(); i < data.rows.size(); i++) {
            String[] row = data.rows.get(i);
            int line = i + 1;
            if (isEmpty(row)) {
                r.empty++;
                continue;
            }
            List<String> problems = check(row, mapping, kind, taken);
            if (!problems.isEmpty()) {
                r.skipped.add(new Problem(line, String.join("; ", problems)));
                continue;
            }
            try {
                DatabaseObject o = create(row, mapping, kind, taxes);
                if (o.saveImport() && o.isExisting()) {
                    afterInsert(o, row, mapping);
                    r.imported++;
                    r.ids.add(o.__getIDS());
                } else {
                    r.skipped.add(new Problem(line, Messages.CSV_PROBLEM_SAVE.toString()));
                }
            } catch (Exception e) {
                Log.Debug(CsvImport.class, e);
                r.skipped.add(new Problem(line, Messages.CSV_PROBLEM_SAVE.toString() + " " + e.getMessage()));
            }
            if (progress != null) {
                progress.accept(i - data.firstData() + 1);
            }
        }
        return r;
    }

    private static DatabaseObject create(String[] row, Field[] mapping, Kind kind, java.util.Map<String, Integer> taxes) throws Exception {
        DatabaseObject o;
        if (kind.isContact()) {
            Contact c = new Contact();
            c.setisCustomer(kind == Kind.CUSTOMERS);
            c.setisSupplier(kind == Kind.SUPPLIERS);
            c.setisManufacturer(kind == Kind.MANUFACTURERS);
            c.setCountry(User.getCurrentUser().__getDefcountry());
            c.setDateadded(new Date());
            o = c;
        } else {
            Product p = new Product();
            p.setInttype(Product.TYPE_PRODUCT);
            p.setDateadded(new Date());
            o = p;
        }
        java.util.Map<String, Method> setters = o.setVars();
        for (int col = 0; col < mapping.length; col++) {
            Field f = mapping[col];
            String v = value(row, col);
            if (f == null || v.isEmpty()) {
                continue;
            }
            switch (f.type) {
                case NUMBER:
                    setters.get(f.key).invoke(o, number(v));
                    break;
                case DATE:
                    setters.get(f.key).invoke(o, date(v));
                    break;
                case TAX:
                    ((Product) o).setTaxids(tax(number(v), taxes));
                    break;
                case SUPPLIER:
                    ((Product) o).setSuppliersids(contact(v, true));
                    break;
                case MANUFACTURER:
                    ((Product) o).setManufacturersids(contact(v, false));
                    break;
                case GROUP:
                    o.setGroupsids(group(v));
                    break;
                default:
                    setters.get(f.key).invoke(o, v);
            }
        }
        if (o instanceof Contact) {
            Contact c = (Contact) o;
            if ((c.__getCname() == null || c.__getCname().isEmpty()) && c.__getCompany() != null && !c.__getCompany().isEmpty()) {
                c.setCname(c.__getCompany());
                c.setisCompany(true);
            }
        }
        return o;
    }

    /**
     * A new record gets a new number and today as date when it is inserted:
     * the number and date of the file are set afterwards
     */
    private static void afterInsert(DatabaseObject o, String[] row, Field[] mapping) throws Exception {
        boolean again = false;
        for (int col = 0; col < mapping.length; col++) {
            Field f = mapping[col];
            String v = value(row, col);
            if (f == null || v.isEmpty()) {
                continue;
            }
            if (f.key.equals("cnumber") && o instanceof Contact && !v.equals(((Contact) o).__getCNumber())) {
                ((Contact) o).setCNumber(v);
                again = true;
            } else if (f.type == Type.DATE) {
                o.setVars().get(f.key).invoke(o, date(v));
                again = true;
            }
        }
        if (again) {
            o.save(true);
        }
    }

    private static int tax(BigDecimal value, java.util.Map<String, Integer> cache) throws NodataFoundException {
        String key = value.stripTrailingZeros().toPlainString();
        Integer id = cache.get(key);
        if (id != null) {
            return id;
        }
        try {
            for (DatabaseObject d : DatabaseObject.getObjects(Context.getTaxes())) {
                Tax t = (Tax) d;
                if (t.__getTaxvalue() != null && t.__getTaxvalue().compareTo(value) == 0) {
                    cache.put(key, t.__getIDS());
                    return t.__getIDS();
                }
            }
        } catch (NodataFoundException e) {
            //no taxes yet
        }
        Tax t = new Tax();
        t.setTaxvalue(value);
        t.setCname(key + " %");
        t.setIdentifier(key + " %");
        t.setCountry(User.getCurrentUser().__getDefcountry());
        t.saveImport();
        cache.put(key, t.__getIDS());
        return t.__getIDS();
    }

    private static int contact(String name, boolean supplier) {
        try {
            Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), name);
            if (c != null) {
                return c.__getIDS();
            }
        } catch (NodataFoundException e) {
            //new
        }
        Contact c = new Contact();
        c.setCname(name);
        c.setisSupplier(supplier);
        c.setisManufacturer(!supplier);
        c.setisCompany(true);
        c.setCompany(name);
        c.setDateadded(new Date());
        c.saveImport();
        return c.__getIDS();
    }

    private static int group(String name) {
        try {
            Group g = (Group) DatabaseObject.getObject(Context.getGroup(), name);
            if (g != null) {
                return g.__getIDS();
            }
        } catch (NodataFoundException e) {
            //new
        }
        Group g = new Group();
        g.setCname(name);
        g.setGroupsids(1);
        g.saveImport();
        return g.__getIDS();
    }
}
