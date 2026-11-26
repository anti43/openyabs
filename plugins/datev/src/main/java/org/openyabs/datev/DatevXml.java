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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import mpv5.handler.JournalCalc;
import mpv5.utils.export.VatCategory;

/**
 * The outgoing invoices of a period as a package of the DATEV XML interface
 * online (DATEV XML-Schnittstelle online, version 6.0) for "Belege online" of
 * DATEV Unternehmen online: a ZIP with {@code document.xml} (the index), one
 * ledger file per invoice (Belegsatzdaten: {@code accountsReceivableLedger},
 * the data of the posting) and the invoice images (PDF, also ZUGFeRD: the
 * archived original if a plugin such as GoBD keeps one). Built like the
 * sample files of DATEV (accountsReceivableLedger, version 6.0): file names
 * {@code Rechnungsdaten_RA_<number>.xml} / {@code Rechnungsbild_RA_<number>.pdf},
 * credit notes as {@code Rechnungskorrektur...RAK} with negative amounts.
 * The files of a document may be PDF or XML (XRechnung), as DATEV allows.
 * All texts are limited to the characters DATEV takes ({@link #clean}).
 * <p>
 * Not certified by DATEV: check the first import with the tax adviser.
 */
public final class DatevXml {

    public static final String NS_DOCUMENT = "http://xml.datev.de/bedi/tps/document/v06.0";
    public static final String NS_LEDGER = "http://xml.datev.de/bedi/tps/ledger/v060";
    /**
     * required by DATEV in every ledger file
     */
    public static final String XML_DATA = "Kopie nur zur Verbuchung berechtigt nicht zum Vorsteuerabzug";

    /**
     * One line of the ledger file: the part of the document with one rate
     * (and, without VAT, one VAT category)
     */
    public static final class Line {

        /**
         * the rate in percent (0 for parts without VAT)
         */
        public BigDecimal rate = BigDecimal.ZERO;
        /**
         * the net amount, positive (credit notes are negative by their type)
         */
        public BigDecimal net = BigDecimal.ZERO;
        /**
         * the VAT, positive
         */
        public BigDecimal tax = BigDecimal.ZERO;
        /**
         * the revenue account (empty: assigned in DATEV)
         */
        public String account = "";
        /**
         * the BU key (empty: none)
         */
        public String bu = "";
        /**
         * the VAT category of a part without VAT, may be null
         */
        public VatCategory category;

        public Line() {
        }

        public Line(BigDecimal rate, BigDecimal net, BigDecimal tax, String account) {
            this.rate = rate;
            this.net = net;
            this.tax = tax;
            this.account = account;
        }

        /**
         * @return net + VAT
         */
        public BigDecimal gross() {
            return nz(net).add(nz(tax));
        }
    }

    /**
     * One invoice or credit note
     */
    public static final class Doc {

        /**
         * the document type (invoice, deposit, part payment, credit note) for
         * the overview
         */
        public String kind = "";
        public String number = "";
        /**
         * the document date (Rechnungsdatum)
         */
        public Date date;
        public Date dueDate;
        /**
         * the service date (Leistungsdatum), may be null
         */
        public Date deliveryDate;
        /**
         * the pay date of a paid invoice (DATEV stamps it "bezahlt"), null
         * if not paid
         */
        public Date paidAt;
        public boolean credit;
        /**
         * true for a cancelled invoice (it is exported with its
         * cancellation)
         */
        public boolean cancelled;
        public String customerName = "";
        public String customerCity = "";
        public String customerNumber = "";
        public String vatId = "";
        public String currency = "EUR";
        public String text = "";
        /**
         * the lines, amounts positive
         */
        public final List<Line> lines = new ArrayList<Line>();
        /**
         * the files of the document: file name in the ZIP - content (the
         * invoice as PDF/ZUGFeRD or the XRechnung)
         */
        public final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();

        /**
         * @return the net of the lines, negative for a credit note
         */
        public BigDecimal net() {
            BigDecimal t = BigDecimal.ZERO;
            for (Line l : lines) {
                t = t.add(nz(l.net));
            }
            return credit ? t.negate() : t;
        }

        /**
         * @return the VAT of the lines, negative for a credit note
         */
        public BigDecimal tax() {
            BigDecimal t = BigDecimal.ZERO;
            for (Line l : lines) {
                t = t.add(nz(l.tax));
            }
            return credit ? t.negate() : t;
        }

        /**
         * @return the gross of the lines as in consolidatedAmount (negative
         * for a credit note)
         */
        public BigDecimal gross() {
            return net().add(tax());
        }
    }

    private DatevXml() {
    }

    /**
     * the characters DATEV takes in the XML files besides letters and digits
     * (DATEV XML-Schnittstelle online, Zeichensatz): also Latin-1 (umlauts),
     * CJK and Hangul
     */
    private static final String ALLOWED = "\u00a7@\u20ac\t\r\n#|`\u00b4'^\u00b0%&*~:;.-/+=_ ?$!{}[],()<>\\\"";

    /**
     * @param c
     * @return true if DATEV takes the character
     */
    static boolean allowed(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                || (c >= 0x80 && c <= 0xff) || (c >= 0x4e00 && c <= 0x9fff) || (c >= 0xac00 && c <= 0xd7af)
                || ALLOWED.indexOf(c) >= 0;
    }

    /**
     * @param s
     * @return the text with the characters DATEV takes only: precomposed
     * (NFC), typographic quotes, dashes, ellipsis, bullets and special spaces
     * replaced, other characters left out
     */
    public static String clean(String s) {
        if (s == null) {
            return "";
        }
        String n = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFC);
        StringBuilder b = new StringBuilder(n.length());
        for (char c : n.toCharArray()) {
            switch (c) {
                case '\u2018': case '\u2019': case '\u201a': case '\u201b': case '\u2032':
                    b.append('\'');
                    break;
                case '\u201c': case '\u201d': case '\u201e': case '\u201f': case '\u2033':
                    b.append('"');
                    break;
                case '\u2010': case '\u2011': case '\u2012': case '\u2013': case '\u2014': case '\u2015': case '\u2212':
                    b.append('-');
                    break;
                case '\u2026':
                    b.append("...");
                    break;
                case '\u2022': case '\u2023': case '\u2043':
                    b.append('*');
                    break;
                case '\u2009': case '\u200a': case '\u202f': case '\u2007': case '\u2002': case '\u2003':
                    b.append(' ');
                    break;
                case '\u2122':
                    b.append("(TM)");
                    break;
                default:
                    if (allowed(c)) {
                        b.append(c);
                    }
            }
        }
        return b.toString();
    }

    /**
     * @param s
     * @return the text for XML: {@link #clean} and escaped (also ' as
     * &amp;apos; as DATEV asks)
     */
    static String x(String s) {
        String c = clean(s);
        StringBuilder b = new StringBuilder(c.length());
        for (char ch : c.toCharArray()) {
            switch (ch) {
                case '&':
                    b.append("&amp;");
                    break;
                case '<':
                    b.append("&lt;");
                    break;
                case '>':
                    b.append("&gt;");
                    break;
                case '"':
                    b.append("&quot;");
                    break;
                case '\'':
                    b.append("&apos;");
                    break;
                default:
                    b.append(ch);
            }
        }
        return b.toString();
    }

    static String cut(String s, int max) {
        String v = clean(s).trim();
        return v.length() > max ? v.substring(0, max) : v;
    }

    /**
     * the largest file DATEV takes in a package
     */
    public static final long MAX_FILE = 20L * 1024 * 1024;
    /**
     * the most documents of one package (document.xml)
     */
    public static final int MAX_DOCUMENTS = 4999;

    /**
     * @param number the invoice number
     * @return the number as DATEV takes it as invoiceId (letters, digits and
     * {@code $%&*+-/}, at most 36): other characters become "-"
     */
    public static String invoiceId(String number) {
        String s = DatevFormat.transliterate(number == null ? "" : number.trim()).replaceAll("[^a-zA-Z0-9$%&*+\\-/]+", "-");
        return cut(s, 36);
    }

    /**
     * @param vatId
     * @return the VAT ID as DATEV takes it (letters, digits, "." and "_",
     * at most 15), empty if none
     */
    public static String vatId(String vatId) {
        return cut((vatId == null ? "" : vatId).replaceAll("[^0-9a-zA-Z._]", ""), 15);
    }

    /**
     * @param account
     * @return the account if it is a number of 4 to 8 digits (as DATEV
     * describes the field), empty else
     */
    static String accountNo(String account) {
        String a = account == null ? "" : account.trim();
        return a.matches("[1-9][0-9]{3,7}") ? a : "";
    }

    static String day(Date d) {
        return new SimpleDateFormat("yyyy-MM-dd").format(d == null ? new Date() : d);
    }

    static String amount(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * @param number the invoice number
     * @return the part of a file name (letters, digits, - and _)
     */
    static String safe(String number) {
        String s = DatevFormat.transliterate(number == null ? "" : number).replaceAll("[^A-Za-z0-9_\\-]+", "_");
        return s.isEmpty() ? "Beleg" : s;
    }

    /**
     * @param d
     * @param ownVatId the VAT ID of the company (for parts without VAT),
     * may be empty
     * @return the ledger file (Belegsatzdaten) of the document: one
     * {@code accountsReceivableLedger} per line in one {@code consolidate};
     * consolidatedAmount is the sum of the lines as written
     */
    public static String ledger(Doc d, String ownVatId) {
        BigDecimal sign = d.credit ? BigDecimal.ONE.negate() : BigDecimal.ONE;
        List<Line> lines = new ArrayList<Line>();
        BigDecimal total = BigDecimal.ZERO;
        for (Line l : d.lines) {
            if (l.gross().setScale(2, RoundingMode.HALF_UP).signum() != 0) {
                lines.add(l);
                total = total.add(l.gross().setScale(2, RoundingMode.HALF_UP).multiply(sign));
            }
        }
        StringBuilder b = new StringBuilder();
        b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        b.append("<LedgerImport xmlns=\"").append(NS_LEDGER).append("\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
                .append(" xsi:schemaLocation=\"").append(NS_LEDGER).append(" Belegverwaltung_online_ledger_import_v060.xsd\"")
                .append(" version=\"6.0\" generator_info=\"YaBS DATEV-Plugin\" generating_system=\"YaBS\"")
                .append(" xml_data=\"").append(XML_DATA).append("\">\n");
        b.append("  <consolidate consolidatedAmount=\"").append(amount(total))
                .append("\" consolidatedDate=\"").append(day(d.date))
                .append("\" consolidatedInvoiceId=\"").append(x(invoiceId(d.number)))
                .append("\" consolidatedCurrencyCode=\"").append(x(d.currency)).append('"');
        if (d.deliveryDate != null) {
            //required when deliveryDate is given in the lines
            b.append(" consolidatedDeliveryDate=\"").append(day(d.deliveryDate)).append('"');
        }
        b.append(">\n");
        String info = invoiceId(d.number).equals(d.number) ? d.text : join(d.number, d.text);
        for (Line l : lines) {
            BigDecimal rate = l.rate == null ? BigDecimal.ZERO : l.rate;
            b.append("    <accountsReceivableLedger>\n");
            el(b, "date", day(d.date));
            el(b, "amount", amount(l.gross().multiply(sign)));
            el(b, "accountNo", accountNo(l.account));
            if (!accountNo(l.account).isEmpty() && l.bu != null && l.bu.trim().matches("[0-9]{1,4}")) {
                el(b, "buCode", l.bu.trim());
            }
            el(b, "tax", amount(rate));
            el(b, "information", cut(info, 120));
            el(b, "currencyCode", d.currency);
            el(b, "invoiceId", invoiceId(d.number));
            //30 characters as DATEV describes the field (the XSD takes 60)
            el(b, "bookingText", cut(d.number + " " + d.customerName, 30));
            if (rate.signum() == 0 && ownVatId != null && !ownVatId.trim().isEmpty()) {
                el(b, "ownVatId", vatId(ownVatId));
            }
            el(b, "partyId", cut(d.customerNumber, 15));
            if (d.paidAt != null) {
                el(b, "paidAt", day(d.paidAt));
            }
            if (d.vatId != null && !d.vatId.trim().isEmpty()) {
                el(b, "vatId", vatId(d.vatId));
            }
            if (d.dueDate != null) {
                el(b, "dueDate", day(d.dueDate));
            }
            if (d.deliveryDate != null) {
                el(b, "deliveryDate", day(d.deliveryDate));
            }
            el(b, "customerName", cut(d.customerName, 50));
            el(b, "customerCity", cut(d.customerCity, 30));
            b.append("    </accountsReceivableLedger>\n");
        }
        b.append("  </consolidate>\n");
        b.append("</LedgerImport>\n");
        return b.toString();
    }

    private static String join(String a, String b) {
        String x = a == null ? "" : a.trim();
        String y = b == null ? "" : b.trim();
        return x.isEmpty() ? y : y.isEmpty() ? x : x + " - " + y;
    }

    private static void el(StringBuilder b, String name, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        b.append("      <").append(name).append('>').append(x(value.trim())).append("</").append(name).append(">\n");
    }

    /**
     * @param d
     * @return the file name of the ledger file, as in the samples of DATEV
     */
    static String ledgerName(Doc d) {
        return (d.credit ? "Rechnungskorrekturdaten_RAK_" : "Rechnungsdaten_RA_") + safe(d.number) + ".xml";
    }

    /**
     * @param d
     * @return the base name of the invoice images
     */
    static String imageName(Doc d) {
        return (d.credit ? "Rechnungskorrekturbild_RAK_" : "Rechnungsbild_RA_") + safe(d.number);
    }

    /**
     * @param docs the documents
     * @param ledgerNames document - name of its ledger file
     * @param created the time of the export
     * @return document.xml
     */
    public static String documentXml(List<Doc> docs, Map<Doc, String> ledgerNames, Date created, String folder) {
        StringBuilder b = new StringBuilder();
        b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        b.append("<archive xmlns=\"").append(NS_DOCUMENT).append("\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
                .append(" xsi:schemaLocation=\"").append(NS_DOCUMENT).append(" Document_v060.xsd\"")
                .append(" version=\"6.0\" generatingSystem=\"YaBS\">\n");
        b.append("  <header>\n");
        b.append("    <date>").append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(created)).append("</date>\n");
        b.append("  </header>\n");
        b.append("  <content>\n");
        for (Doc d : docs) {
            b.append("    <document>\n");
            b.append("      <extension xsi:type=\"accountsReceivableLedger\" datafile=\"").append(x(ledgerNames.get(d))).append("\">\n");
            b.append("        <property value=\"").append(new SimpleDateFormat("yyyy-MM").format(d.date == null ? created : d.date)).append("\" key=\"1\"/>\n");
            String folderName = cut(folder, 64);
            b.append("        <property value=\"").append(x(folderName.isEmpty() ? DatevConfig.DEFAULT_INVOICE_FOLDER : folderName)).append("\" key=\"3\"/>\n");
            b.append("      </extension>\n");
            for (String f : d.files.keySet()) {
                b.append("      <extension xsi:type=\"File\" name=\"").append(x(f)).append("\"/>\n");
            }
            b.append("    </document>\n");
        }
        b.append("  </content>\n");
        b.append("</archive>\n");
        return b.toString();
    }

    /**
     * @param docs
     * @param ownVatId the VAT ID of the company
     * @param created
     * @param folder the folder of the outgoing invoices in Belege online
     * @return the ZIP for DATEV: document.xml, the ledger files and the
     * files of the invoices
     * @throws IOException
     */
    public static byte[] zip(List<Doc> docs, String ownVatId, Date created, String folder) throws IOException {
        Map<Doc, String> ledgerNames = new LinkedHashMap<Doc, String>();
        Set<String> names = new HashSet<String>();
        names.add("document.xml");
        for (Doc d : docs) {
            ledgerNames.put(d, unique(names, ledgerName(d)));
            //unique file names over all documents
            Map<String, byte[]> renamed = new LinkedHashMap<String, byte[]>();
            for (Map.Entry<String, byte[]> f : d.files.entrySet()) {
                renamed.put(unique(names, f.getKey()), f.getValue());
            }
            d.files.clear();
            d.files.putAll(renamed);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            put(zip, "document.xml", documentXml(docs, ledgerNames, created, folder).getBytes(StandardCharsets.UTF_8));
            for (Doc d : docs) {
                put(zip, ledgerNames.get(d), ledger(d, ownVatId).getBytes(StandardCharsets.UTF_8));
                for (Map.Entry<String, byte[]> f : d.files.entrySet()) {
                    put(zip, f.getKey(), f.getValue());
                }
            }
        }
        return out.toByteArray();
    }

    private static String unique(Set<String> names, String name) {
        String n = name;
        int i = 2;
        while (!names.add(n)) {
            int dot = name.lastIndexOf('.');
            n = dot > 0 ? name.substring(0, dot) + "_" + i + name.substring(dot) : name + "_" + i;
            i++;
        }
        return n;
    }

    private static void put(ZipOutputStream zip, String name, byte[] data) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(data);
        zip.closeEntry();
    }

    static BigDecimal nz(BigDecimal b) {
        return b == null ? BigDecimal.ZERO : b;
    }
}
