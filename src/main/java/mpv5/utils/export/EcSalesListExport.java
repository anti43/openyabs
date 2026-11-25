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
package mpv5.utils.export;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Address;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Tax;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.handler.JournalCalc;
import mpv5.handler.JournalCalc.Entry;
import mpv5.logging.Log;
import mpv5.utils.numberformat.InvoiceMath;

/**
 * EC Sales List (Zusammenfassende Meldung, ZM, § 18a UStG / Art. 262 EU VAT
 * Directive 2006/112/EC) exported from the journal.
 * <p>
 * Aggregates net sales of outgoing invoices (invoices, deposits, part payments,
 * and credit notes which reduce the assessment base) for intra-EU B2B supplies:
 * <ul>
 * <li>{@link SupplyType#GOODS} ({@link VatCategory#K}, intra-community supply
 * of goods, § 4 Nr. 1b / § 6a UStG): BZSt / ELSTER indicator {@code L}
 * (Lieferung, code {@code 0}),</li>
 * <li>{@link SupplyType#SERVICES} ({@link VatCategory#AE}, EU B2B reverse-charge
 * services, § 13b Abs. 1 / § 3a Abs. 2 UStG): BZSt / ELSTER indicator {@code S}
 * (Sonstige Leistung, code {@code 1}).</li>
 * </ul>
 * Rows are grouped by normalized customer EU VAT ID ({@code USt-IdNr.}) and
 * {@link SupplyType}. Issues (missing or non-EU VAT IDs on intra-EU / reverse
 * charge invoices) are collected so the user or tax adviser can fix them.
 */
public final class EcSalesListExport {

    /**
     * EU member state VAT ID prefixes eligible for the EC Sales List
     * (excluding domestic {@code DE}; including {@code EL} for Greece and
     * {@code XI} for Northern Ireland).
     */
    public static final Set<String> OTHER_EU_VAT_PREFIXES = new HashSet<String>(Arrays.asList(
            "AT", "BE", "BG", "CY", "CZ", "DK", "EE", "EL", "ES", "FI", "FR", "HR", "HU",
            "IE", "IT", "LT", "LU", "LV", "MT", "NL", "PL", "PT", "RO", "SE", "SI", "SK", "XI"));

    /**
     * Type of intra-community supply reported in the EC Sales List (§ 18a UStG).
     */
    public enum SupplyType {

        /**
         * Intra-community supply of goods (§ 4 Nr. 1b / § 6a UStG, EN 16931
         * category {@code K}).
         */
        GOODS("L", "0", VatCategory.K),
        /**
         * Intra-EU B2B service subject to reverse charge (§ 3a Abs. 2 / § 13b
         * UStG, EN 16931 category {@code AE}).
         */
        SERVICES("S", "1", VatCategory.AE);

        private final String code;
        private final String elsterCode;
        private final VatCategory category;

        SupplyType(String code, String elsterCode, VatCategory category) {
            this.code = code;
            this.elsterCode = elsterCode;
            this.category = category;
        }

        /**
         * @return the human-readable ZM indicator ({@code "L"} = Lieferung,
         * {@code "S"} = Sonstige Leistung)
         */
        public String code() {
            return code;
        }

        /**
         * @return the numeric BZSt/ELSTER CSV indicator ({@code "0"} = Lieferung,
         * {@code "1"} = Sonstige Leistung)
         */
        public String elsterCode() {
            return elsterCode;
        }

        /**
         * @return the corresponding EN 16931 {@link VatCategory}
         */
        public VatCategory category() {
            return category;
        }

        public String label() {
            return this == GOODS
                    ? Messages.JOURNAL_ZM_TYPE_GOODS.toString()
                    : Messages.JOURNAL_ZM_TYPE_SERVICES.toString();
        }

        public static SupplyType forCategory(VatCategory cat) {
            if (cat == VatCategory.K) {
                return GOODS;
            }
            if (cat == VatCategory.AE) {
                return SERVICES;
            }
            return null;
        }
    }

    /**
     * One aggregated row of the EC Sales List (per customer VAT ID and
     * {@link SupplyType}).
     */
    public static final class Line {

        public final String countryCode;
        public final String vatNumber;
        public final String vatId;
        public final SupplyType type;
        public final String customerName;
        public BigDecimal netAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        public int invoiceCount;
        public final List<String> invoiceNumbers = new ArrayList<String>();

        public Line(String countryCode, String vatNumber, String vatId, SupplyType type, String customerName) {
            this.countryCode = countryCode;
            this.vatNumber = vatNumber;
            this.vatId = vatId;
            this.type = type;
            this.customerName = customerName == null ? "" : customerName;
        }

        /**
         * @return the assessment base rounded to whole euros as required by
         * § 18a Abs. 7 UStG / BZSt ELSTER
         */
        public BigDecimal roundedEuroAmount() {
            return netAmount.setScale(0, RoundingMode.DOWN);
        }
    }

    /**
     * Resolves the contact and line-level VAT category breakdown of a journal
     * entry (allows unit testing without a database).
     */
    public interface EntryInspector {

        /**
         * @param entry
         * @return the customer VAT ID (e.g. {@code "ATU12345678"}), or {@code ""}
         */
        String vatIdOf(Entry entry);

        /**
         * @param entry
         * @return the customer name
         */
        String customerNameOf(Entry entry);

        /**
         * @param entry
         * @return the signed net amounts of the entry by {@link VatCategory}
         * (credit notes negative, invoices positive)
         */
        Map<VatCategory, BigDecimal> netByCategory(Entry entry);
    }

    /**
     * Result of building the EC Sales List for a journal period.
     */
    public static final class Result {

        public final List<Line> lines = new ArrayList<Line>();
        public final List<String> problems = new ArrayList<String>();

        public BigDecimal totalNet() {
            BigDecimal sum = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            for (Line l : lines) {
                sum = sum.add(l.netAmount);
            }
            return sum;
        }
    }

    private EcSalesListExport() {
    }

    /**
     * @param vatId
     * @return true if {@code vatId} is a well-formed VAT ID of another EU
     * member state (or Northern Ireland {@code XI})
     */
    public static boolean isOtherEuVatId(String vatId) {
        if (!ZugpferdFile.isValidVatId(vatId)) {
            return false;
        }
        String norm = ZugpferdFile.normalizeVatId(vatId);
        return norm.length() > 2 && OTHER_EU_VAT_PREFIXES.contains(norm.substring(0, 2));
    }

    /**
     * Build the EC Sales List from the given journal entries using the database
     * inspector.
     *
     * @param entries
     * @return
     */
    public static Result build(List<Entry> entries) {
        return build(entries, dbInspector());
    }

    /**
     * Build the EC Sales List from the given journal entries.
     * <p>
     * Outgoing invoices ({@link Constants#TYPE_INVOICE},
     * {@link Constants#TYPE_DEPOSIT}, {@link Constants#TYPE_PART_PAYMENT}) and
     * credit notes ({@link Constants#TYPE_CREDIT}, which reduce the reported
     * net amount) are included unless cancelled. If the journal is loaded on a
     * cash basis with payment rows ({@link Constants#TYPE_PAYMENT}) and without
     * the underlying invoice rows, the invoice of each payment is inspected
     * once; if the invoice rows are present in {@code entries}, they are
     * inspected directly so intra-community supplies are reported in the period
     * of supply/invoicing (§ 18a Abs. 1, 6 UStG).
     *
     * @param entries
     * @param inspector
     * @return
     */
    public static Result build(List<Entry> entries, EntryInspector inspector) {
        Result res = new Result();
        if (entries == null || entries.isEmpty()) {
            return res;
        }
        EntryInspector insp = inspector != null ? inspector : dbInspector();
        Map<String, Line> byKey = new LinkedHashMap<String, Line>();
        Set<Integer> seenItemIds = new HashSet<Integer>();

        for (Entry e : entries) {
            if (e == null || JournalCalc.isCancelled(e)) {
                continue;
            }
            boolean isInvoiceDoc = e.type == Constants.TYPE_INVOICE
                    || e.type == Constants.TYPE_CREDIT
                    || e.type == Constants.TYPE_DEPOSIT
                    || e.type == Constants.TYPE_PART_PAYMENT;
            if (!isInvoiceDoc) {
                continue;
            }
            if (e.itemId > 0 && !seenItemIds.add(e.itemId)) {
                continue;
            }
            Map<VatCategory, BigDecimal> byCat = insp.netByCategory(e);
            if (byCat == null || byCat.isEmpty()) {
                continue;
            }
            String rawVat = insp.vatIdOf(e);
            String normVat = ZugpferdFile.normalizeVatId(rawVat);
            String customer = insp.customerNameOf(e);

            for (Map.Entry<VatCategory, BigDecimal> catEntry : byCat.entrySet()) {
                SupplyType supplyType = SupplyType.forCategory(catEntry.getKey());
                BigDecimal amount = catEntry.getValue();
                if (supplyType == null || amount == null || amount.signum() == 0) {
                    continue;
                }
                if (normVat.isEmpty()) {
                    res.problems.add(String.format(Messages.JOURNAL_ZM_MISSING_VATID.toString(),
                            e.number, customer, supplyType.category().code()));
                    continue;
                }
                if (!ZugpferdFile.isValidVatId(normVat)) {
                    res.problems.add(String.format(Messages.JOURNAL_ZM_INVALID_VATID.toString(),
                            e.number, customer, rawVat));
                    continue;
                }
                String prefix = normVat.substring(0, 2);
                if (!OTHER_EU_VAT_PREFIXES.contains(prefix)) {
                    // e.g. domestic DE reverse charge (§ 13b Abs. 2 UStG) or third-country reverse charge:
                    // only intra-community supplies (K) to non-EU VAT IDs are flagged as problem
                    if (supplyType == SupplyType.GOODS) {
                        res.problems.add(String.format(Messages.JOURNAL_ZM_NON_EU_VATID.toString(),
                                e.number, customer, normVat));
                    }
                    continue;
                }
                String numberPart = normVat.substring(2);
                String key = normVat + "|" + supplyType.name();
                Line line = byKey.get(key);
                if (line == null) {
                    line = new Line(prefix, numberPart, normVat, supplyType, customer);
                    byKey.put(key, line);
                }
                line.netAmount = line.netAmount.add(amount).setScale(2, RoundingMode.HALF_UP);
                line.invoiceCount++;
                if (e.number != null && !e.number.isEmpty() && !line.invoiceNumbers.contains(e.number)) {
                    line.invoiceNumbers.add(e.number);
                }
            }
        }
        for (Line l : byKey.values()) {
            if (l.netAmount.signum() != 0) {
                res.lines.add(l);
            }
        }
        return res;
    }

    /**
     * Render the EC Sales List as semicolon-separated CSV (without BOM).
     * Includes BZSt / ELSTER compatible columns (country prefix, VAT number
     * without prefix, full USt-IdNr., assessment base in EUR and rounded whole
     * EUR, ZM indicator L/S, ELSTER indicator 0/1, customer name, invoice count
     * and invoice numbers).
     *
     * @param result
     * @return
     */
    public static String toCsv(Result result) {
        StringBuilder b = new StringBuilder();
        String[] head = {
            Messages.JOURNAL_ZM_COL_COUNTRY.toString(),
            Messages.JOURNAL_ZM_COL_VATNO.toString(),
            Messages.JOURNAL_ZM_COL_VATID.toString(),
            Messages.JOURNAL_ZM_COL_AMOUNT_EUR.toString(),
            Messages.JOURNAL_ZM_COL_AMOUNT_EXACT.toString(),
            Messages.JOURNAL_ZM_COL_TYPE_CODE.toString(),
            Messages.JOURNAL_ZM_COL_ELSTER_CODE.toString(),
            Messages.JOURNAL_ZM_COL_TYPE.toString(),
            Messages.JOURNAL_ZM_COL_CUSTOMER.toString(),
            Messages.JOURNAL_ZM_COL_INVOICES.toString()
        };
        for (int i = 0; i < head.length; i++) {
            b.append(i > 0 ? ";" : "").append(JournalAdviserPackage.cell(head[i]));
        }
        b.append("\r\n");
        if (result != null) {
            for (Line l : result.lines) {
                Object[] row = {
                    l.countryCode,
                    l.vatNumber,
                    l.vatId,
                    l.roundedEuroAmount().toPlainString(),
                    JournalAdviserPackage.amount(l.netAmount),
                    l.type.code(),
                    l.type.elsterCode(),
                    l.type.label(),
                    l.customerName,
                    String.join(", ", l.invoiceNumbers)
                };
                for (int i = 0; i < row.length; i++) {
                    b.append(i > 0 ? ";" : "").append(JournalAdviserPackage.cell(row[i]));
                }
                b.append("\r\n");
            }
        }
        return b.toString();
    }

    /**
     * Write the EC Sales List CSV (UTF-8 with BOM) to a file.
     *
     * @param result
     * @param target
     * @throws IOException
     */
    public static void writeCsv(Result result, File target) throws IOException {
        try (OutputStream out = Files.newOutputStream(target.toPath())) {
            JournalAdviserPackage.writeCsv(out, toCsv(result));
        }
    }

    /**
     * Default database-backed {@link EntryInspector}.
     *
     * @return
     */
    public static EntryInspector dbInspector() {
        final List<Tax> taxes = Tax.all();
        final Map<Integer, Contact> contactCache = new HashMap<Integer, Contact>();
        return new EntryInspector() {
            private Item itemOf(Entry e) {
                if (e.obj instanceof Item) {
                    return (Item) e.obj;
                }
                if (e.itemId > 0) {
                    for (int t : new int[]{Item.TYPE_INVOICE, Item.TYPE_CREDIT, Item.TYPE_DEPOSIT, Item.TYPE_PART_PAYMENT}) {
                        try {
                            return (Item) DatabaseObject.getObject(mpv5.handler.ItemService.contextOf(t), e.itemId);
                        } catch (Exception ex) {
                            // try next type
                        }
                    }
                }
                return null;
            }

            private Contact contactOf(Item item) {
                if (item == null || item.__getContactsids() <= 0) {
                    return null;
                }
                int cid = item.__getContactsids();
                if (!contactCache.containsKey(cid)) {
                    Contact c = null;
                    try {
                        c = (Contact) DatabaseObject.getObject(Context.getContact(), cid);
                    } catch (Exception ex) {
                        Log.Debug(EcSalesListExport.class, ex.getMessage());
                    }
                    contactCache.put(cid, c);
                }
                return contactCache.get(cid);
            }

            @Override
            public String vatIdOf(Entry entry) {
                Item item = itemOf(entry);
                Contact c = contactOf(item);
                if (c == null) {
                    return "";
                }
                Address inv = null;
                try {
                    inv = c.getInvoiceaddress();
                } catch (Exception ex) {
                    // ignore
                }
                return ZugpferdFile.firstNonEmpty(inv == null ? null : inv.__getTaxnumber(), c.__getTaxnumber());
            }

            @Override
            public String customerNameOf(Entry entry) {
                String fromEntry = (ZugpferdFile.nvl(entry.contact) + " " + ZugpferdFile.nvl(entry.prename)).trim();
                if (!fromEntry.isEmpty()) {
                    return fromEntry;
                }
                Contact c = contactOf(itemOf(entry));
                if (c == null) {
                    return "";
                }
                return ZugpferdFile.firstNonEmpty(c.__getCompany(),
                        ZugpferdFile.join(" ", c.__getPrename(), c.__getCname()));
            }

            @Override
            public Map<VatCategory, BigDecimal> netByCategory(Entry entry) {
                Map<VatCategory, BigDecimal> result = new LinkedHashMap<VatCategory, BigDecimal>();
                Item item = itemOf(entry);
                if (item == null) {
                    return result;
                }
                Contact c = contactOf(item);
                java.util.function.Function<SubItem, Tax> rateFn = ZugpferdFile.taxRates(c, taxes);
                BigDecimal sign = entry.type == Constants.TYPE_CREDIT ? BigDecimal.ONE.negate() : BigDecimal.ONE;
                SubItem[] subitems = item.getSubitems();
                boolean anyLine = false;
                for (SubItem s : subitems) {
                    if (s == null || s.getInttype() == SubItem.TYPE_TEXT) {
                        continue;
                    }
                    BigDecimal q = s.effectiveQuantity();
                    if (q.signum() == 0) {
                        continue;
                    }
                    BigDecimal lineNet = InvoiceMath.lineNet(q, s.__getExternalvalue(), s.__getDiscount());
                    if (lineNet.signum() == 0) {
                        continue;
                    }
                    anyLine = true;
                    BigDecimal r = InvoiceMath.rate(s.__getTaxpercentvalue());
                    Tax tax = rateFn.apply(s);
                    VatCategory cat = tax != null && tax.__getTaxvalue() != null && tax.__getTaxvalue().compareTo(r) == 0
                            ? tax.category() : VatCategory.of(null, r);
                    BigDecimal signed = lineNet.multiply(sign).setScale(2, RoundingMode.HALF_UP);
                    result.put(cat, result.getOrDefault(cat, BigDecimal.ZERO).add(signed));
                }
                if (!anyLine && entry.net != null && entry.net.signum() != 0 && c != null) {
                    // Fallback when an invoice has a net amount without loaded subitems: check customer tax rate
                    String taxId = mpv5.handler.ContactService.contactTax(c);
                    if (!taxId.isEmpty()) {
                        try {
                            Tax t = (Tax) DatabaseObject.getObject(Context.getTaxes(), Integer.parseInt(taxId));
                            if (t != null) {
                                BigDecimal signed = entry.net.abs().multiply(sign).setScale(2, RoundingMode.HALF_UP);
                                result.put(t.category(), signed);
                            }
                        } catch (Exception ex) {
                            // ignore
                        }
                    }
                }
                return result;
            }
        };
    }
}
