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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Account;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Item;
import mpv5.db.objects.Revenue;
import mpv5.globals.Constants;
import mpv5.handler.JournalCalc;
import mpv5.handler.JournalCalc.Entry;
import mpv5.logging.Log;
import org.openyabs.datev.DatevFormat.BookingLine;

/**
 * Maps counted YaBS {@link JournalCalc.Entry} rows into DATEV
 * {@link BookingLine}s (one per document and tax rate) and collects validation
 * issues before writing the file.
 */
public final class DatevMapper {

    /**
     * EU member state VAT ID prefixes (excluding DE, for intra-EU deliveries).
     */
    private static final Set<String> OTHER_EU_PREFIXES = new HashSet<String>(Arrays.asList(
            "AT", "BE", "BG", "CY", "CZ", "DK", "EE", "EL", "ES", "FI", "FR", "HR", "HU",
            "IE", "IT", "LT", "LU", "LV", "MT", "NL", "PL", "PT", "RO", "SE", "SI", "SK", "XI"));

    /**
     * Standard SKR03/SKR04 expense accounts for intra-community acquisitions (§ 1a UStG)
     * and EU B2B reverse-charge services (§ 13b UStG) where DATEV expects supplier EU VAT ID in col 40.
     */
    private static final Set<String> SKR03_EU_EXPENSE_ACCOUNTS = new HashSet<String>(Arrays.asList("3420", "3425", "3123"));
    private static final Set<String> SKR04_EU_EXPENSE_ACCOUNTS = new HashSet<String>(Arrays.asList("5420", "5425", "5923"));

    /**
     * Legal form tokens indicating a business entity rather than a walk-in POS cash customer.
     */
    private static final Set<String> LEGAL_FORM_TOKENS = new HashSet<String>(Arrays.asList(
            "gmbh", "ug", "ag", "kg", "ohg", "gbr", "e.k.", "ek", "e.v.", "ev",
            "ltd", "inc", "llc", "sarl", "s.a.", "bv", "nv", "co.", "partg", "stiftung", "se", "kgaa"));

    /**
     * Resolves an {@link Account} for a journal entry or by account number.
     */
    public interface AccountLookup {

        Account accountOf(Entry entry);

        Account byNumber(String accountNumber);
    }

    /**
     * Resolves the VAT ID (USt-IdNr.) of the contact of an entry.
     */
    public interface ContactVatLookup {

        String vatIdOf(Entry entry);
    }

    /**
     * Resolves the VAT category (UNCL5305, see
     * {@link mpv5.utils.export.VatCategory}) of the part of a document with a
     * rate, from the tax rates of its positions (#214)
     */
    public interface TaxCategoryLookup {

        /**
         * @param entry
         * @param rate the rate of the part
         * @return the category, null if unknown
         */
        mpv5.utils.export.VatCategory categoryOf(Entry entry, BigDecimal rate);
    }

    /**
     * Resolves net amounts per 0%-rate VAT category for multi-category 0% invoices
     * (e.g., an invoice mixing category K goods and category AE services).
     * A {@code null} key represents an uncategorized 0% position.
     */
    public interface ZeroCategoryLookup {

        Map<mpv5.utils.export.VatCategory, BigDecimal> zeroNetByCategory(Entry entry);
    }

    /**
     * Foreign currency details for an entry (when currency differs from batch currency).
     */
    public static final class CurrencyInfo {

        public String currency = "EUR";
        public BigDecimal kurs;
        public BigDecimal basisUmsatz;
        public String wkzBasisUmsatz = "EUR";

        public CurrencyInfo() {
        }

        public CurrencyInfo(String currency) {
            this.currency = currency;
        }

        public CurrencyInfo(String currency, BigDecimal kurs, BigDecimal basisUmsatz, String wkzBasisUmsatz) {
            this.currency = currency;
            this.kurs = kurs;
            this.basisUmsatz = basisUmsatz;
            this.wkzBasisUmsatz = wkzBasisUmsatz;
        }
    }

    /**
     * Resolves document currency and optional foreign-currency conversion data.
     */
    public interface CurrencyLookup {

        CurrencyInfo currencyOf(Entry entry);
    }

    /**
     * Resolves the delivery/service date (Leistungsdatum, § 14 Abs. 4 Nr. 6 UStG) of an entry.
     */
    public interface ServiceDateLookup {

        Date serviceDateOf(Entry entry);
    }

    /**
     * Optional resolver for a receipt file name or DATEV Beleglink GUID.
     */
    public interface ReceiptLinkLookup {

        String receiptLinkOf(Entry entry);
    }

    public static final class Issue {

        public enum Severity {
            ERROR,
            WARNING
        }

        public final Severity severity;
        public final String message;

        public Issue(Severity severity, String message) {
            this.severity = severity;
            this.message = message;
        }

        @Override
        public String toString() {
            return (severity == Severity.ERROR ? "[Fehler] " : "[Hinweis] ") + message;
        }
    }

    public static final class Result {

        public final List<BookingLine> lines = new ArrayList<BookingLine>();
        public final List<Issue> issues = new ArrayList<Issue>();

        public boolean hasErrors() {
            for (Issue i : issues) {
                if (i.severity == Issue.Severity.ERROR) {
                    return true;
                }
            }
            return false;
        }

        /**
         * @return the sum of all lines (amounts without sign, income and
         * expenses together) - for the sides see {@link #sumDebit()} and
         * {@link #sumCredit()}
         */
        public BigDecimal totalGross() {
            BigDecimal sum = BigDecimal.ZERO;
            for (BookingLine l : lines) {
                if (l.umsatz != null) {
                    sum = sum.add(l.umsatz.abs());
                }
            }
            return sum;
        }

        /**
         * @return the lines into the money account (S: income)
         */
        public BigDecimal sumDebit() {
            return sum("S");
        }

        /**
         * @return the lines out of the money account (H: expenses, credit
         * notes)
         */
        public BigDecimal sumCredit() {
            return sum("H");
        }

        private BigDecimal sum(String side) {
            BigDecimal sum = BigDecimal.ZERO;
            for (BookingLine l : lines) {
                if (l.umsatz != null && side.equals(l.sollHaben)) {
                    sum = sum.add(l.umsatz.abs());
                }
            }
            return sum;
        }
    }

    private DatevMapper() {
    }

    /**
     * Default database-backed account lookup.
     */
    public static AccountLookup dbAccountLookup() {
        final Map<Integer, Account> byId = new HashMap<Integer, Account>();
        final Map<String, Account> byNum = new HashMap<String, Account>();
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getAccounts())) {
                Account a = (Account) o;
                byId.put(a.__getIDS(), a);
                if (a.__getIntaccountclass() > 0) {
                    String formatted = DatevConfig.formatAccount(a.__getIntaccountclass());
                    byNum.putIfAbsent(formatted, a);
                    byNum.putIfAbsent(String.valueOf(a.__getIntaccountclass()), a);
                }
            }
        } catch (Exception e) {
            Log.Debug(DatevMapper.class, e.getMessage());
        }
        return new AccountLookup() {
            @Override
            public Account accountOf(Entry entry) {
                if (entry == null || entry.obj == null) {
                    return null;
                }
                int id = 0;
                if (entry.obj instanceof Item) {
                    id = ((Item) entry.obj).__getAccountsids();
                } else if (entry.obj instanceof Expense) {
                    id = ((Expense) entry.obj).__getAccountsids();
                } else if (entry.obj instanceof Revenue) {
                    id = ((Revenue) entry.obj).__getAccountsids();
                }
                return id > 0 ? byId.get(id) : null;
            }

            @Override
            public Account byNumber(String accountNumber) {
                return accountNumber == null ? null : byNum.get(accountNumber.trim());
            }
        };
    }

    /**
     * Default database-backed contact VAT ID lookup.
     */
    public static ContactVatLookup dbContactVatLookup() {
        final Map<Integer, String> cache = new HashMap<Integer, String>();
        return entry -> {
            if (entry == null) {
                return "";
            }
            Item item = resolveItem(entry);
            if (item == null) {
                return "";
            }
            int cid = item.__getContactsids();
            if (cid <= 0) {
                return "";
            }
            if (cache.containsKey(cid)) {
                return cache.get(cid);
            }
            String vat = "";
            try {
                Contact c = item.getContact();
                if (c != null && c.__getTaxnumber() != null) {
                    vat = c.__getTaxnumber().trim();
                }
            } catch (Exception e) {
                Log.Debug(DatevMapper.class, e.getMessage());
            }
            cache.put(cid, vat);
            return vat;
        };
    }

    /**
     * Default database-backed receipt link lookup: returns the locked/archived
     * document filename (from GoBD plugin or similar) if present for the item.
     */
    public static ReceiptLinkLookup dbReceiptLinkLookup() {
        final Map<Integer, String> cache = new HashMap<Integer, String>();
        return entry -> {
            Item item = resolveItem(entry);
            if (item == null || item.__getIDS() <= 0) {
                return "";
            }
            if (cache.containsKey(item.__getIDS())) {
                return cache.get(item.__getIDS());
            }
            String link = "";
            try {
                mpv5.plugin.ExtensionManager.LockedDocument ld = mpv5.plugin.ExtensionManager.instance().lockedDocument(item);
                if (ld != null && ld.file != null) {
                    link = ld.file.getName();
                }
            } catch (Throwable t) {
                Log.Debug(DatevMapper.class, t.getMessage());
            }
            cache.put(item.__getIDS(), link);
            return link;
        };
    }

    /**
     * Default database-backed currency lookup using {@link mpv5.utils.export.ZugpferdFile#findCurrency(Item, Contact)}.
     */
    public static CurrencyLookup dbCurrencyLookup() {
        final Map<Integer, CurrencyInfo> cache = new HashMap<Integer, CurrencyInfo>();
        return entry -> {
            Item item = resolveItem(entry);
            if (item == null) {
                return null;
            }
            int id = item.__getIDS();
            if (id > 0 && cache.containsKey(id)) {
                return cache.get(id);
            }
            CurrencyInfo info = null;
            try {
                Contact c = item.__getContactsids() > 0 ? item.getContact() : null;
                String cur = mpv5.utils.export.ZugpferdFile.findCurrency(item, c);
                if (!DatevConfig.isBlank(cur)) {
                    info = new CurrencyInfo(cur.trim().toUpperCase(Locale.ROOT));
                }
            } catch (Exception ex) {
                Log.Debug(DatevMapper.class, ex.getMessage());
            }
            if (id > 0) {
                cache.put(id, info);
            }
            return info;
        };
    }

    /**
     * Default database-backed delivery/service date (Leistungsdatum) lookup from {@link Item#__getDatetodo()}.
     */
    public static ServiceDateLookup dbServiceDateLookup() {
        return entry -> {
            Item item = resolveItem(entry);
            return item != null ? item.__getDatetodo() : null;
        };
    }

    private static Item resolveItem(Entry entry) {
        if (entry == null) {
            return null;
        }
        if (entry.obj instanceof Item) {
            return (Item) entry.obj;
        }
        if (entry.itemId > 0) {
            try {
                return (Item) DatabaseObject.getObject(Context.getInvoice(), entry.itemId);
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * Resolves explicit VAT category of a {@link mpv5.db.objects.Tax} without coercing an
     * unconfigured empty category code ({@code ""}) or the generic default 0 % tax rate
     * (seeded as {@code E} with empty exemption code and note by
     * {@code FirstStartSetup.completeTaxRates()}) to an explicit domestic exemption.
     */
    static mpv5.utils.export.VatCategory explicitCategoryOf(mpv5.db.objects.Tax t, BigDecimal rate) {
        if (t == null || t.__getTaxvalue() == null || t.__getTaxvalue().compareTo(rate) != 0) {
            return null;
        }
        String code = t.__getCategorycode();
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        mpv5.utils.export.VatCategory cat = mpv5.utils.export.VatCategory.of(code, rate);
        if (cat == mpv5.utils.export.VatCategory.E
                && DatevConfig.isBlank(t.__getExemptioncode())
                && DatevConfig.isBlank(t.__getExemptionnote())) {
            return null;
        }
        return cat;
    }

    /**
     * Maps journal entries using the database lookups.
     */
    public static Result map(List<Entry> entries, DatevConfig config, Date from, Date to) {
        return map(entries, config, from, to, dbAccountLookup(), dbContactVatLookup(),
                dbReceiptLinkLookup(), dbTaxCategoryLookup(), dbZeroCategoryLookup(),
                dbCurrencyLookup(), dbServiceDateLookup());
    }

    /**
     * The VAT category of the positions of a document with a rate: the tax
     * rate of each position as in the e-invoice (the customer's rate, the
     * product's rate, any rate with the value), skipping text-only lines
     * ({@link mpv5.db.objects.SubItem#TYPE_TEXT}) and zero-quantity lines.
     * Returns {@code null} when the tax rate has no explicitly configured category code.
     *
     * @return the lookup (reads the documents)
     */
    public static TaxCategoryLookup dbTaxCategoryLookup() {
        final List<mpv5.db.objects.Tax> taxes = mpv5.db.objects.Tax.all();
        final Map<String, mpv5.utils.export.VatCategory> cache = new HashMap<String, mpv5.utils.export.VatCategory>();
        return (entry, rate) -> {
            if (entry == null || rate == null) {
                return null;
            }
            Item item = resolveItem(entry);
            if (item == null) {
                return null;
            }
            BigDecimal r = mpv5.utils.numberformat.InvoiceMath.rate(rate);
            String key = item.__getIDS() + "@" + r.stripTrailingZeros().toPlainString();
            if (cache.containsKey(key)) {
                return cache.get(key);
            }
            mpv5.utils.export.VatCategory found = null;
            try {
                Contact contact = item.__getContactsids() > 0 ? item.getContact() : null;
                java.util.function.Function<mpv5.db.objects.SubItem, mpv5.db.objects.Tax> rates
                        = mpv5.utils.export.ZugpferdFile.taxRates(contact, taxes);
                for (mpv5.db.objects.SubItem s : item.getSubitems()) {
                    if (s.getInttype() == mpv5.db.objects.SubItem.TYPE_TEXT) {
                        continue;
                    }
                    if (s.effectiveQuantity() == null || s.effectiveQuantity().signum() == 0) {
                        continue;
                    }
                    BigDecimal sr = mpv5.utils.numberformat.InvoiceMath.rate(s.__getTaxpercentvalue());
                    if (sr.compareTo(r) != 0 || s.__getDescription() == null || s.__getDescription().trim().isEmpty()) {
                        continue;
                    }
                    mpv5.db.objects.Tax t = rates.apply(s);
                    found = explicitCategoryOf(t, r);
                    break;
                }
            } catch (Exception ex) {
                Log.Debug(DatevMapper.class, ex.getMessage());
            }
            cache.put(key, found);
            return found;
        };
    }

    /**
     * Database-backed lookup returning 0%-rate net amounts grouped by explicit VAT category
     * (preserving {@code null} for uncategorized 0% positions), skipping text-only positions.
     */
    public static ZeroCategoryLookup dbZeroCategoryLookup() {
        final List<mpv5.db.objects.Tax> taxes = mpv5.db.objects.Tax.all();
        final Map<Integer, Map<mpv5.utils.export.VatCategory, BigDecimal>> cache
                = new HashMap<Integer, Map<mpv5.utils.export.VatCategory, BigDecimal>>();
        return entry -> {
            Item item = resolveItem(entry);
            if (item == null) {
                return null;
            }
            int id = item.__getIDS();
            if (id > 0 && cache.containsKey(id)) {
                return cache.get(id);
            }
            Map<mpv5.utils.export.VatCategory, BigDecimal> map = new java.util.LinkedHashMap<mpv5.utils.export.VatCategory, BigDecimal>();
            try {
                Contact contact = item.__getContactsids() > 0 ? item.getContact() : null;
                java.util.function.Function<mpv5.db.objects.SubItem, mpv5.db.objects.Tax> rates
                        = mpv5.utils.export.ZugpferdFile.taxRates(contact, taxes);
                for (mpv5.db.objects.SubItem s : item.getSubitems()) {
                    if (s.getInttype() == mpv5.db.objects.SubItem.TYPE_TEXT) {
                        continue;
                    }
                    BigDecimal qty = s.effectiveQuantity();
                    if (qty == null || qty.signum() == 0) {
                        continue;
                    }
                    BigDecimal sr = mpv5.utils.numberformat.InvoiceMath.rate(s.__getTaxpercentvalue());
                    if (sr.signum() != 0 || s.__getDescription() == null || s.__getDescription().trim().isEmpty()) {
                        continue;
                    }
                    BigDecimal price = s.__getExternalvalue() == null ? BigDecimal.ZERO : s.__getExternalvalue();
                    BigDecimal disc = s.__getDiscount() == null ? BigDecimal.ZERO : s.__getDiscount();
                    BigDecimal lineNet = mpv5.utils.numberformat.InvoiceMath.lineNet(qty, price, disc);
                    if (lineNet.signum() == 0) {
                        continue;
                    }
                    mpv5.db.objects.Tax t = rates.apply(s);
                    mpv5.utils.export.VatCategory cat = explicitCategoryOf(t, BigDecimal.ZERO);
                    BigDecimal prev = map.get(cat);
                    map.put(cat, prev == null ? lineNet : prev.add(lineNet));
                }
            } catch (Exception ex) {
                Log.Debug(DatevMapper.class, ex.getMessage());
            }
            if (id > 0) {
                cache.put(id, map);
            }
            return map;
        };
    }

    /**
     * Maps journal entries into DATEV booking lines and validates settings and
     * entries.
     */
    public static Result map(List<Entry> entries, DatevConfig config, Date from, Date to,
            AccountLookup accountLookup, ContactVatLookup vatLookup, ReceiptLinkLookup receiptLookup) {
        return map(entries, config, from, to, accountLookup, vatLookup, receiptLookup, null, null, null, null);
    }

    /**
     * Maps journal entries into DATEV booking lines and validates settings and
     * entries.
     *
     * @param categoryLookup the VAT category of the 0 % part of a document
     * (reverse charge, intra-community supply, export: the account), null:
     * every 0 % sale to an EU business is an intra-community supply of goods
     */
    public static Result map(List<Entry> entries, DatevConfig config, Date from, Date to,
            AccountLookup accountLookup, ContactVatLookup vatLookup, ReceiptLinkLookup receiptLookup,
            TaxCategoryLookup categoryLookup) {
        return map(entries, config, from, to, accountLookup, vatLookup, receiptLookup, categoryLookup, null, null, null);
    }

    /**
     * Full overload supporting multi-category 0% splitting, foreign currency, and service date (Leistungsdatum).
     */
    public static Result map(List<Entry> entries, DatevConfig config, Date from, Date to,
            AccountLookup accountLookup, ContactVatLookup vatLookup, ReceiptLinkLookup receiptLookup,
            TaxCategoryLookup categoryLookup, ZeroCategoryLookup zeroCategoryLookup,
            CurrencyLookup currencyLookup, ServiceDateLookup serviceDateLookup) {
        Result result = new Result();
        validateConfig(config, from, to, result.issues);

        Set<String> warnedMissingRateAccounts = new HashSet<String>();
        Set<String> warnedGoodsAssumed = new HashSet<String>();
        Set<String> warnedInvalidAccounts = new HashSet<String>();
        Map<String, String> belegfeld1ToDocKey = new HashMap<String, String>();
        Map<String, String> belegfeld1ToLabel = new HashMap<String, String>();
        List<String> zeroDomestic = new ArrayList<String>();
        Set<String> zeroAccounts = new java.util.TreeSet<String>();
        int countedDocs = 0;

        if (entries != null) {
            for (Entry e : entries) {
                if (e == null || !e.counted || JournalCalc.isCancelled(e)) {
                    continue;
                }
                countedDocs++;
                String docKey = docIdentityKey(e);

                Account docAccount = accountLookup == null ? null : accountLookup.accountOf(e);
                if (docAccount == null) {
                    result.issues.add(new Issue(Issue.Severity.WARNING,
                            "Beleg " + labelOf(e) + ": kein Konto zugeordnet (Fallback-Konto wird verwendet)."));
                } else if (docAccount.__getIntaccountclass() <= 0) {
                    result.issues.add(new Issue(Issue.Severity.WARNING,
                            "Beleg " + labelOf(e) + ": Konto '" + docAccount.__getCname() + "' hat keine Kontonummer, gebucht wird auf das "
                            + (e.type == Constants.TYPE_EXPENSE ? "Aufwandskonto" : "Erlöskonto") + " des Steuersatzes aus den DATEV-Einstellungen."
                            + " Für ein eigenes Konto dem Beleg ein Konto mit Kontonummer zuordnen."));
                }

                String rawNum = e.number == null ? "" : e.number.trim();
                String sanitizedNum = DatevFormat.sanitizeBelegfeld1(rawNum);
                if (rawNum.length() > DatevFormat.MAX_BELEGFELD1_LEN) {
                    result.issues.add(new Issue(Issue.Severity.WARNING,
                            "Beleg " + labelOf(e) + ": Belegnummer länger als 36 Zeichen (wird auf '" + sanitizedNum + "' gekürzt)."));
                } else if (!rawNum.equals(sanitizedNum)) {
                    result.issues.add(new Issue(Issue.Severity.WARNING,
                            "Beleg " + labelOf(e) + ": Belegnummer enthält für DATEV unzulässige Zeichen (exportiert als '" + sanitizedNum + "')."));
                }
                if (!sanitizedNum.isEmpty()) {
                    String prevKey = belegfeld1ToDocKey.putIfAbsent(sanitizedNum, docKey);
                    if (prevKey == null) {
                        belegfeld1ToLabel.put(sanitizedNum, labelOf(e));
                    } else if (!prevKey.equals(docKey)) {
                        result.issues.add(new Issue(Issue.Severity.WARNING,
                                "Beleg " + labelOf(e) + ": Belegfeld 1 '" + sanitizedNum + "' kollidiert nach Kürzung/Bereinigung mit Beleg '"
                                + belegfeld1ToLabel.get(sanitizedNum) + "' (GoBD-Eindeutigkeit prüfen)."));
                    }
                }

                Map<BigDecimal, BigDecimal[]> rates = e.rates;
                if (rates == null || rates.isEmpty()) {
                    rates = new TreeMap<BigDecimal, BigDecimal[]>(JournalCalc.RATE_ORDER);
                    rates.put(null, new BigDecimal[]{
                        e.net == null ? BigDecimal.ZERO : e.net,
                        e.tax == null ? BigDecimal.ZERO : e.tax
                    });
                }

                boolean isExpense = e.type == Constants.TYPE_EXPENSE;
                boolean isCredit = e.type == Constants.TYPE_CREDIT;
                String konto = resolveMoneyAccount(e, config);
                validateAccountLength(konto, "Geldkonto", labelOf(e), config.sachkontenLaenge, warnedInvalidAccounts, result.issues);
                Date bookingDate = resolveBookingDate(e, config.belegdatumMode);
                if (outside(bookingDate, from, to)) {
                    //DATEV takes only bookings within the dates of the batch (header): the import fails
                    result.issues.add(new Issue(Issue.Severity.ERROR,
                            "Beleg " + labelOf(e) + ": Buchungsdatum " + day(bookingDate) + " liegt außerhalb des Zeitraums "
                            + day(from) + " – " + day(to) + " (DATEV lehnt den Stapel ab). Belegdatum-Einstellung und Zeitraum prüfen."));
                }
                String cancelledOriginal = cancelledOriginal(e);
                if (cancelledOriginal != null) {
                    result.issues.add(new Issue(Issue.Severity.WARNING,
                            "Gutschrift " + labelOf(e) + " storniert Rechnung " + cancelledOriginal + ": stornierte Rechnungen exportiert der"
                            + " Buchungsstapel nicht. Wurde die Rechnung bezahlt und in einem früheren Stapel gebucht, ist das richtig;"
                            + " sonst steht hier nur die Erstattung ohne den Umsatz."));
                }
                String buchungstext = resolveBookingText(e);
                String beleglink = receiptLookup == null ? "" : nullToEmpty(receiptLookup.receiptLinkOf(e));
                String contactVat = vatLookup == null ? "" : DatevFormat.sanitizeEuVatId(vatLookup.vatIdOf(e));
                CurrencyInfo curInfo = currencyLookup == null ? null : currencyLookup.currencyOf(e);
                String batchWkz = DatevConfig.isBlank(config.waehrung) ? "EUR" : config.waehrung.trim().toUpperCase(Locale.ROOT);
                String docWkz = (curInfo == null || DatevConfig.isBlank(curInfo.currency))
                        ? batchWkz : curInfo.currency.trim().toUpperCase(Locale.ROOT);
                boolean isForeignCurrency = !docWkz.equalsIgnoreCase(batchWkz);
                if (isForeignCurrency && (curInfo == null || (curInfo.kurs == null && curInfo.basisUmsatz == null))) {
                    result.issues.add(new Issue(Issue.Severity.ERROR,
                            "Beleg " + labelOf(e) + ": Fremdwährung " + docWkz + " weicht von der Stapelwährung " + batchWkz
                            + " ab, aber Wechselkurs (Kurs) und Basis-Umsatz in " + batchWkz
                            + " fehlen (DATEV darf Fremdwährungsbeträge nicht als " + batchWkz + " verbuchen)."));
                }

                Date serviceDate = (config.leistungsdatumExportieren && serviceDateLookup != null)
                        ? serviceDateLookup.serviceDateOf(e) : null;
                String leistungsdatum = "";
                String steuerperiodeDatum = "";
                if (serviceDate != null && bookingDate != null) {
                    String sDay = DatevFormat.yyyymmdd(serviceDate);
                    String bDay = DatevFormat.yyyymmdd(bookingDate);
                    if (!sDay.equals(bDay)) {
                        leistungsdatum = DatevFormat.ddmmyyyy(serviceDate);
                        if (!sDay.substring(0, 6).equals(bDay.substring(0, 6))) {
                            steuerperiodeDatum = leistungsdatum;
                        }
                    }
                }

                int lineSeq = 0;
                for (Map.Entry<BigDecimal, BigDecimal[]> re : rates.entrySet()) {
                    BigDecimal rate = re.getKey();
                    BigDecimal net = re.getValue()[0] == null ? BigDecimal.ZERO : re.getValue()[0];
                    BigDecimal tax = re.getValue()[1] == null ? BigDecimal.ZERO : re.getValue()[1];
                    BigDecimal gross = net.add(tax);
                    if (gross.signum() == 0) {
                        continue;
                    }

                    // Split 0% revenue across multiple VAT categories (e.g., K + AE on one invoice) when present
                    List<mpv5.utils.export.VatCategory> partCats = new ArrayList<mpv5.utils.export.VatCategory>();
                    List<BigDecimal> partGross = new ArrayList<BigDecimal>();
                    if (!isExpense && rate != null && rate.signum() == 0 && zeroCategoryLookup != null) {
                        Map<mpv5.utils.export.VatCategory, BigDecimal> zeroMap = zeroCategoryLookup.zeroNetByCategory(e);
                        if (zeroMap != null && zeroMap.size() > 1) {
                            List<BigDecimal> weights = new ArrayList<BigDecimal>();
                            for (Map.Entry<mpv5.utils.export.VatCategory, BigDecimal> ze : zeroMap.entrySet()) {
                                if (ze.getValue() != null && ze.getValue().signum() != 0) {
                                    partCats.add(ze.getKey());
                                    weights.add(ze.getValue().abs());
                                }
                            }
                            if (partCats.size() > 1) {
                                List<BigDecimal> split = JournalCalc.distribute(gross.abs(), weights);
                                for (BigDecimal sVal : split) {
                                    partGross.add(gross.signum() < 0 ? sVal.negate() : sVal);
                                }
                            } else {
                                partCats.clear();
                            }
                        }
                    }
                    if (partGross.isEmpty()) {
                        mpv5.utils.export.VatCategory singleCat = null;
                        if (!isExpense && rate != null && rate.signum() == 0) {
                            if (categoryLookup != null) {
                                singleCat = categoryLookup.categoryOf(e, rate);
                            } else if (zeroCategoryLookup != null) {
                                Map<mpv5.utils.export.VatCategory, BigDecimal> zm = zeroCategoryLookup.zeroNetByCategory(e);
                                if (zm != null && !zm.isEmpty()) {
                                    singleCat = zm.keySet().iterator().next();
                                }
                            }
                        }
                        partCats.add(singleCat);
                        partGross.add(gross);
                    }

                    for (int p = 0; p < partGross.size(); p++) {
                        BigDecimal partAmount = partGross.get(p);
                        if (partAmount.signum() == 0) {
                            continue;
                        }
                        mpv5.utils.export.VatCategory category = partCats.get(p);

                        // Determine Soll/Haben relative to Konto (money account)
                        // Income: S (money account debited); Expense: H; Credit note: always H regardless of sign convention
                        boolean debitMoney = !isExpense && !isCredit;
                        if (!isCredit && partAmount.signum() < 0) {
                            debitMoney = !debitMoney;
                        }

                        // Gegenkonto and BU key: the same rules as the document package (DATEV XML)
                        Booking booking = booking(rate, isExpense, docAccount, category, contactVat, config, accountLookup);
                        String gegenkonto = booking.account;
                        if (booking.goodsAssumed && warnedGoodsAssumed.add(labelOf(e))) {
                            result.issues.add(new Issue(Issue.Severity.WARNING,
                                    "Beleg " + labelOf(e) + ": 0 %-Umsatz an ein Unternehmen in der EU ohne USt-Kategorie, als"
                                    + " innergemeinschaftliche Lieferung (Konto " + gegenkonto + ") gebucht. Leistungen (§ 3a Abs. 2 UStG)"
                                    + " gehören auf das Konto für EU-Leistungen: dem Kunden einen Steuersatz mit Kategorie AE"
                                    + " (Reverse Charge) zuordnen, Lieferungen Kategorie K."));
                        }
                        if (DatevConfig.isBlank(gegenkonto)) {
                            String rk = (isExpense ? "Ausgabe:" : "Einnahme:") + (rate == null ? "unbekannt" : rate.toPlainString() + "%");
                            if (warnedMissingRateAccounts.add(rk)) {
                                result.issues.add(new Issue(Issue.Severity.ERROR,
                                        "Kein " + (isExpense ? "Aufwandskonto" : "Erlöskonto") + " für Steuersatz "
                                        + (rate == null ? "unbekannt" : rate.toPlainString() + " %") + " konfiguriert (Tabelle der Steuersätze unter "
                                        + DatevConfig.SETTINGS_PATH + ")."));
                            }
                        } else {
                            validateAccountLength(gegenkonto, "Gegenkonto", labelOf(e), config.sachkontenLaenge, warnedInvalidAccounts, result.issues);
                        }
                        String bu = booking.bu;
                        if (booking.uncategorizedZero && !zeroDomestic.contains(labelOf(e))) {
                            zeroDomestic.add(labelOf(e));
                            zeroAccounts.add(gegenkonto);
                        }
                        if (booking.exportAccountForNonExport && warnedGoodsAssumed.add("export:" + labelOf(e))) {
                            result.issues.add(new Issue(Issue.Severity.WARNING, exportAccountNote(labelOf(e), gegenkonto, config)));
                        }

                        // Col 40 (EU-Land u. UStID): emit ONLY on EU accounts (ZM / § 1a / § 13b EU)
                        String euVatField = "";
                        boolean isEuIncomeAccount = !isExpense && rate != null && rate.signum() == 0
                                && ((!DatevConfig.isBlank(config.euDeliveryAccount) && config.euDeliveryAccount.trim().equals(gegenkonto))
                                || (!DatevConfig.isBlank(config.euServiceAccount) && config.euServiceAccount.trim().equals(gegenkonto)));
                        boolean isEuExpenseAccount = isExpense && isEuExpenseAccount(gegenkonto, "04".equals(config.kontenrahmen));
                        if (isEuIncomeAccount || isEuExpenseAccount) {
                            euVatField = contactVat;
                            if (DatevConfig.isBlank(euVatField)) {
                                result.issues.add(new Issue(Issue.Severity.WARNING,
                                        "Beleg " + labelOf(e) + ": EU-Sachverhalt (Konto " + gegenkonto + ") ohne USt-IdNr. beim Kontakt."));
                            }
                        }

                        BookingLine line = new BookingLine();
                        line.umsatz = partAmount.abs();
                        line.sollHaben = debitMoney ? "S" : "H";
                        line.wkz = docWkz;
                        if (isForeignCurrency && curInfo != null) {
                            line.kurs = curInfo.kurs;
                            if (curInfo.basisUmsatz != null && curInfo.basisUmsatz.signum() != 0) {
                                // Scale basisUmsatz proportionally if split across rates/categories
                                BigDecimal docTotalGross = e.gross != null && e.gross.signum() != 0 ? e.gross.abs() : partAmount.abs();
                                if (partAmount.abs().compareTo(docTotalGross) == 0) {
                                    line.basisUmsatz = curInfo.basisUmsatz.abs();
                                } else {
                                    line.basisUmsatz = curInfo.basisUmsatz.abs()
                                            .multiply(partAmount.abs())
                                            .divide(docTotalGross, 2, java.math.RoundingMode.HALF_UP);
                                }
                                line.wkzBasisUmsatz = DatevConfig.isBlank(curInfo.wkzBasisUmsatz) ? batchWkz : curInfo.wkzBasisUmsatz;
                            }
                        }
                        line.konto = konto;
                        line.gegenkonto = gegenkonto;
                        line.buSchluessel = bu;
                        line.belegdatum = DatevFormat.ddmm(bookingDate);
                        line.belegfeld1 = rawNum;
                        line.buchungstext = buchungstext;
                        line.beleglink = beleglink;
                        line.euLandUstId = euVatField;
                        line.buchungsGuid = deterministicGuid(docKey, rawNum, bookingDate, rate, category, lineSeq++);
                        line.festschreibung = config.festschreibung;
                        line.leistungsdatum = leistungsdatum;
                        line.steuerperiodeDatum = steuerperiodeDatum;
                        result.lines.add(line);
                    }
                }
            }
        }

        if (!zeroDomestic.isEmpty()) {
            result.issues.add(new Issue(Issue.Severity.WARNING, zeroDomesticNote(zeroDomestic, zeroAccounts, config)));
        }
        if (countedDocs == 0) {
            result.issues.add(new Issue(Issue.Severity.WARNING,
                    "Keine buchungsrelevanten (bezahlten) Belege im ausgewählten Zeitraum gefunden."));
        }
        return result;
    }

    static boolean isEuExpenseAccount(String accountNo, boolean skr04) {
        if (DatevConfig.isBlank(accountNo)) {
            return false;
        }
        String digits = DatevFormat.digitsOnly(accountNo);
        if (digits.length() < 4) {
            return false;
        }
        String base4 = digits.substring(0, 4);
        return skr04 ? SKR04_EU_EXPENSE_ACCOUNTS.contains(base4) : SKR03_EU_EXPENSE_ACCOUNTS.contains(base4);
    }

    static boolean isValidAccountLength(String accountNo, int sachkontenLaenge) {
        String digits = DatevFormat.digitsOnly(accountNo);
        if (digits.isEmpty()) {
            return false;
        }
        int skl = Math.max(4, Math.min(8, sachkontenLaenge));
        return digits.length() == skl || digits.length() == skl + 1;
    }

    private static void validateAccountLength(String accountNo, String role, String docLabel, int sachkontenLaenge,
            Set<String> warned, List<Issue> issues) {
        if (DatevConfig.isBlank(accountNo) || sachkontenLaenge < 4 || sachkontenLaenge > 8) {
            return;
        }
        if (!isValidAccountLength(accountNo, sachkontenLaenge)) {
            String key = role + ":" + accountNo.trim();
            if (warned.add(key)) {
                issues.add(new Issue(Issue.Severity.ERROR,
                        "Beleg " + docLabel + ": " + role + " '" + accountNo.trim()
                        + "' passt nicht zur konfigurierten Sachkontenlänge " + sachkontenLaenge
                        + " (erwartet: " + sachkontenLaenge + " Stellen für Sachkonten oder "
                        + (sachkontenLaenge + 1) + " Stellen für Personenkonten)."));
            }
        }
    }

    private static String docIdentityKey(Entry e) {
        if (e.itemId > 0) {
            return "item:" + e.itemId;
        }
        if (e.obj instanceof DatabaseObject && ((DatabaseObject) e.obj).__getIDS() > 0) {
            return e.obj.getClass().getSimpleName() + ":" + ((DatabaseObject) e.obj).__getIDS();
        }
        return "entry:" + System.identityHashCode(e);
    }

    private static String deterministicGuid(String docKey, String rawNum, Date bookingDate,
            BigDecimal rate, mpv5.utils.export.VatCategory category, int seq) {
        String seed = docKey + "|" + rawNum + "|" + DatevFormat.yyyymmdd(bookingDate) + "|"
                + (rate == null ? "null" : rate.stripTrailingZeros().toPlainString()) + "|"
                + (category == null ? "none" : category.code()) + "|" + seq;
        return java.util.UUID.nameUUIDFromBytes(seed.getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
    }

    /**
     * The account (Gegenkonto) and BU key of the part of a document with one
     * rate
     */
    public static final class Booking {

        /**
         * the account, empty if none is configured
         */
        public String account = "";
        /**
         * the BU key, empty if none (automatic account, 0 %)
         */
        public String bu = "";
        /**
         * the account object (for the tax of the account), may be null
         */
        public Account effective;
        /**
         * true if a 0 % sale to an EU business without VAT category was
         * booked as intra-community supply of goods
         */
        public boolean goodsAssumed;
        /**
         * true if a 0 % sale that is not an export (category E, O, Z or none)
         * is booked on the export account (SKR03 8120 / SKR04 4120): DATEV
         * reports it as export
         */
        public boolean exportAccountForNonExport;
        /**
         * true for a 0 % sale without VAT category booked as
         * tax-free domestic sale on the account of 0 % (8100 / 4100)
         */
        public boolean uncategorizedZero;
    }

    /**
     * The account and BU key of the part of a document with one rate: explicit
     * 0 % cross-border/reverse-charge VAT categories (AE, K, G) take precedence
     * over a generic 0 % document account; otherwise the document account is used
     * if its tax fits the rate, else 0 % sales by category/EU fallback, else the
     * rate account of the settings. The BU key is omitted only when the account
     * is an official DATEV AM/AV automatic account (or configured as custom automatic
     * account). Both the DATEV booking batch and the DATEV document package book with it.
     *
     * @param rate the rate of the part (null: unknown)
     * @param isExpense
     * @param docAccount the account of the document, may be null
     * @param category the VAT category of a 0 % part, may be null (unconfigured)
     * @param contactVat the VAT ID of the customer (sanitized), may be empty
     * @param config
     * @param accountLookup may be null
     * @return the booking
     */
    public static Booking booking(BigDecimal rate, boolean isExpense, Account docAccount, mpv5.utils.export.VatCategory category,
            String contactVat, DatevConfig config, AccountLookup accountLookup) {
        Booking b = new Booking();
        boolean isZeroIncome = !isExpense && rate != null && rate.signum() == 0;
        boolean explicitSpecialZero = isZeroIncome
                && (category == mpv5.utils.export.VatCategory.AE
                || category == mpv5.utils.export.VatCategory.K
                || category == mpv5.utils.export.VatCategory.G);

        boolean docAccountMatches = !explicitSpecialZero
                && docAccount != null
                && docAccount.__getIntaccountclass() > 0
                && (rate == null || DatevConfig.rateMatches(docAccount.__getTaxvalue(), rate));

        if (docAccountMatches) {
            b.account = DatevConfig.formatAccount(docAccount.__getIntaccountclass());
            b.effective = docAccount;
        } else {
            // Fallback from settings per tax rate; 0 % sales by their VAT category (#214):
            // reverse charge (EU service / § 13b), intra-community supply, export
            String byCategory = isZeroIncome
                    ? zeroRateAccount(config, category, isOtherEuVatId(contactVat)) : null;
            if (!DatevConfig.isBlank(byCategory)) {
                b.account = byCategory.trim();
                //no category: EU 0 % sales as before as supplies of goods - services are booked wrong
                b.goodsAssumed = category == null;
            } else if (rate != null) {
                BigDecimal key = JournalCalc.rateKey(rate);
                b.account = nullToEmpty(isExpense
                        ? config.expenseAccounts.get(key)
                        : config.revenueAccounts.get(key));
            }
            if (DatevConfig.isBlank(b.account) && docAccount != null && docAccount.__getIntaccountclass() > 0) {
                // Last resort: use document account even if taxvalue differs
                b.account = DatevConfig.formatAccount(docAccount.__getIntaccountclass());
                b.effective = docAccount;
            } else if (!DatevConfig.isBlank(b.account) && accountLookup != null) {
                b.effective = accountLookup.byNumber(b.account);
            }
        }
        b.bu = resolveBuKey(rate, isExpense, config, b.effective, b.account);
        b.exportAccountForNonExport = isZeroIncome && !b.goodsAssumed
                && category != mpv5.utils.export.VatCategory.G && category != mpv5.utils.export.VatCategory.K
                && category != mpv5.utils.export.VatCategory.AE
                && !DatevConfig.isBlank(b.account) && b.account.equals(nullToEmpty(config.exportAccount));
        b.uncategorizedZero = isZeroIncome && !b.goodsAssumed && !b.exportAccountForNonExport
                && !config.smallBusiness && category == null
                && !DatevConfig.isBlank(b.account);
        return b;
    }

    /**
     * @param numbers the document numbers booked as uncategorized 0 % domestic sales
     * @param accounts the accounts used
     * @param config the DATEV configuration
     * @return the note explaining that uncategorized 0 % sales were booked as
     * tax-free domestic sales (§ 4 Nr. 8 ff. UStG)
     */
    static String zeroDomesticNote(List<String> numbers, Set<String> accounts, DatevConfig config) {
        boolean skr04 = "04".equals(config.kontenrahmen);
        return numbers.size() + " Beleg(e) mit 0 %-Umsatz ohne Steuerkategorie als steuerfreie Inlandsumsätze (§ 4 Nr. 8 ff. UStG) auf "
                + String.join(", ", accounts) + " gebucht: " + String.join(", ", numbers) + ". Für Ausfuhren ("
                + (skr04 ? "4120" : "8120") + "), innergemeinschaftliche Lieferungen (" + (skr04 ? "4125" : "8125")
                + "), Reverse Charge (" + (skr04 ? "4336/4337" : "8336/8337") + ") oder § 19 UStG (" + (skr04 ? "4185" : "8195")
                + ") dem Steuersatz unter Einstellungen › Steuersätze die passende Kategorie geben (G, K, AE) bzw. die Kleinunternehmerregelung einschalten.";
    }

    static String exportAccountNote(String number, String account, DatevConfig config) {
        boolean skr04 = "04".equals(config.kontenrahmen);
        return "Beleg " + number + ": 0 %-Umsatz ohne Kategorie Ausfuhr auf Konto " + account + " gebucht – das ist im SKR"
                + config.kontenrahmen + " das Konto für Ausfuhren (DATEV meldet den Umsatz als Ausfuhr). Steuerfreie Inlandsumsätze gehören"
                + " z. B. auf " + (skr04 ? "4100" : "8100") + ": das Konto für 0 % in der Tabelle der Steuersätze unter "
                + DatevConfig.SETTINGS_PATH + " ändern oder dem Steuersatz die passende Kategorie geben.";
    }

    /**
     * @param d
     * @param from
     * @param to
     * @return true if the day of d is before the day of from or after the day
     * of to (null: no check)
     */
    static boolean outside(Date d, Date from, Date to) {
        if (d == null || from == null || to == null) {
            return false;
        }
        String x = new SimpleDateFormat("yyyyMMdd").format(d);
        return x.compareTo(new SimpleDateFormat("yyyyMMdd").format(from)) < 0
                || x.compareTo(new SimpleDateFormat("yyyyMMdd").format(to)) > 0;
    }

    static String day(Date d) {
        return d == null ? "?" : new SimpleDateFormat("dd.MM.yyyy").format(d);
    }

    /**
     * @param e
     * @return the number of the cancelled invoice a credit note cancels (a
     * cancellation, GoBD Stornorechnung), null if none
     */
    private static String cancelledOriginal(Entry e) {
        if (e.type != Constants.TYPE_CREDIT || !(e.obj instanceof Item)) {
            return null;
        }
        try {
            Item original = ((Item) e.obj).getCorrectedInvoice();
            return original != null && original.__getIntstatus() == Item.STATUS_CANCELLED ? original.__getCnumber() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private static java.time.LocalDate parseStrictYyyyMmDd(String s) {
        if (s == null || !s.trim().matches("\\d{8}")) {
            return null;
        }
        try {
            java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("uuuuMMdd")
                    .withResolverStyle(java.time.format.ResolverStyle.STRICT);
            return java.time.LocalDate.parse(s.trim(), f);
        } catch (Exception ex) {
            return null;
        }
    }

    private static java.time.LocalDate toLocalDate(Date d) {
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private static void validateConfig(DatevConfig config, Date from, Date to, List<Issue> issues) {
        if (config.beraterNummer < 1001 || config.beraterNummer > 9999999) {
            issues.add(new Issue(Issue.Severity.ERROR,
                    "Beraternummer fehlt oder ungültig (erlaubt: 1001–9999999, vom Steuerberater; einzutragen unter "
                    + DatevConfig.SETTINGS_PATH + ")."));
        }
        if (config.mandantenNummer < 1 || config.mandantenNummer > 99999) {
            issues.add(new Issue(Issue.Severity.ERROR,
                    "Mandantennummer fehlt oder ungültig (erlaubt: 1–99999, vom Steuerberater; einzutragen unter "
                    + DatevConfig.SETTINGS_PATH + ")."));
        }
        java.time.LocalDate wjStart = parseStrictYyyyMmDd(config.wjBeginn);
        if (wjStart == null) {
            issues.add(new Issue(Issue.Severity.ERROR,
                    "Wirtschaftsjahresbeginn fehlt oder ist kein gültiges Kalenderdatum im Format JJJJMMTT."));
        }
        if (config.sachkontenLaenge < 4 || config.sachkontenLaenge > 8) {
            issues.add(new Issue(Issue.Severity.ERROR,
                    "Sachkontenlänge muss zwischen 4 und 8 liegen."));
        }
        if (DatevConfig.isBlank(config.moneyAccountIncome)) {
            issues.add(new Issue(Issue.Severity.ERROR,
                    "Geldkonto für Einnahmen ist nicht konfiguriert."));
        }
        if (DatevConfig.isBlank(config.moneyAccountExpense)) {
            issues.add(new Issue(Issue.Severity.ERROR,
                    "Geldkonto für Ausgaben ist nicht konfiguriert."));
        }
        if (config.belegdatumMode == DatevConfig.DateMode.DOCUMENT_DATE) {
            issues.add(new Issue(Issue.Severity.WARNING,
                    "Belegdatum-Modus 'Belegdatum (Rechnungsdatum)' bucht gegen das Geldkonto (" + config.moneyAccountIncome
                    + "): für echte Soll-Versteuerung / OPOS-Buchhaltung sind Debitoren-/Kreditorenkonten erforderlich."));
        }
        if (from != null && to != null && wjStart != null) {
            java.time.LocalDate fromLd = toLocalDate(from);
            java.time.LocalDate toLd = toLocalDate(to);
            java.time.LocalDate wjEnd = wjStart.plusYears(1).minusDays(1);
            if (fromLd.isBefore(wjStart) || toLd.isAfter(wjEnd)) {
                String multiYear = fromLd.getYear() != toLd.getYear()
                        ? " (Zeitraum erstreckt sich über mehrere Jahre: " + fromLd.getYear() + "–" + toLd.getYear() + ")"
                        : "";
                issues.add(new Issue(Issue.Severity.ERROR,
                        "Zeitraum " + day(from) + " – " + day(to) + multiYear
                        + " liegt außerhalb des konfigurierten Wirtschaftsjahres ("
                        + wjStart + " – " + wjEnd + ", WJ-Beginn " + config.wjBeginn.trim()
                        + "). DATEV lehnt den Stapel ab (Belegdatum ist TTMM); WJ-Beginn oder Zeitraum anpassen."));
            }
        }
    }

    private static String resolveMoneyAccount(Entry e, DatevConfig config) {
        if (!DatevConfig.isBlank(config.cashAccount) && isCashSale(e)) {
            return config.cashAccount.trim();
        }
        return e.type == Constants.TYPE_EXPENSE
                ? nullToEmpty(config.moneyAccountExpense)
                : nullToEmpty(config.moneyAccountIncome);
    }

    /**
     * Detects POS / cash register sales ("Barverkauf" contact or "POS" description),
     * excluding business entities whose name happens to start with "Barverkauf" (e.g., "Barverkauf GmbH").
     */
    static boolean isCashSale(Entry e) {
        if (e == null || e.type == Constants.TYPE_EXPENSE) {
            return false;
        }
        String desc = e.description == null ? "" : e.description.trim();
        if (desc.startsWith("POS ") || desc.startsWith("POS\u00b7") || desc.startsWith("POS ·")) {
            return true;
        }
        String contact = e.contact == null ? "" : e.contact.trim().toLowerCase(Locale.ROOT);
        if (contact.equals("barverkauf")) {
            return true;
        }
        if (contact.startsWith("barverkauf ")) {
            String[] tokens = contact.split("[\\s,&/()]+");
            for (int i = 1; i < tokens.length; i++) {
                if (LEGAL_FORM_TOKENS.contains(tokens[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static String resolveBuKey(BigDecimal rate, boolean isExpense, DatevConfig config, Account effectiveAccount, String gegenkonto) {
        if (rate == null || rate.signum() == 0) {
            return "";
        }
        BigDecimal key = JournalCalc.rateKey(rate);
        // Only omit BU-Schlüssel on true DATEV AM/AV automatic accounts (or explicitly configured custom automatic accounts).
        // Non-automatic accounts (e.g., SKR03 4930, 4600, 0480; SKR04 6815, 0670) MUST emit their BU key even when
        // YaBS stores a default tax rate on the account record.
        if (config.isAutomaticAccount(gegenkonto, isExpense, key)) {
            return "";
        }
        return nullToEmpty(isExpense ? config.buKeysExpense.get(key) : config.buKeysIncome.get(key));
    }

    private static Date resolveBookingDate(Entry e, DatevConfig.DateMode mode) {
        if (mode == DatevConfig.DateMode.DOCUMENT_DATE && e.type != Constants.TYPE_PAYMENT) {
            Date docDate = null;
            if (e.obj instanceof Item) {
                docDate = ((Item) e.obj).__getDateadded();
            } else if (e.obj instanceof Expense) {
                docDate = ((Expense) e.obj).__getDateadded();
            } else if (e.obj instanceof Revenue) {
                docDate = ((Revenue) e.obj).__getDateadded();
            }
            if (docDate != null) {
                return docDate;
            }
        } else if (e.type != Constants.TYPE_PAYMENT) {
            Date payDate = null;
            if (e.obj instanceof Item) {
                payDate = ((Item) e.obj).__getDateend();
            } else if (e.obj instanceof Expense) {
                payDate = ((Expense) e.obj).__getDateend();
            } else if (e.obj instanceof Revenue) {
                payDate = ((Revenue) e.obj).__getDateend();
            }
            if (payDate != null) {
                return payDate;
            }
        }
        return e.date;
    }

    private static String resolveBookingText(Entry e) {
        String contact = ((e.contact == null ? "" : e.contact) + " " + (e.prename == null ? "" : e.prename)).trim();
        String desc = e.description == null ? "" : e.description.trim();
        if (e.type == Constants.TYPE_EXPENSE || e.type == Constants.TYPE_REVENUE) {
            return !desc.isEmpty() ? desc : contact;
        }
        return !contact.isEmpty() ? contact : desc;
    }

    static boolean isOtherEuVatId(String sanitizedVatId) {
        if (sanitizedVatId == null || sanitizedVatId.length() < 4) {
            return false;
        }
        return OTHER_EU_PREFIXES.contains(sanitizedVatId.substring(0, 2).toUpperCase(Locale.ROOT));
    }

    private static String labelOf(Entry e) {
        if (e.number != null && !e.number.trim().isEmpty()) {
            return e.number.trim();
        }
        return e.description != null && !e.description.trim().isEmpty() ? e.description.trim() : "(ohne Nummer)";
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * The revenue account of a 0 % sale by its VAT category (#214).
     * Explicit {@link mpv5.utils.export.VatCategory#E} (exempt under § 4 Nr. 8 ff. UStG)
     * returns {@code null} so it stays on the 0 % exempt account (8100 / 4100) and is
     * never coerced into an EU intra-community supply of goods (8125 / 4125).
     *
     * @param config
     * @param category the category of the part, null if unknown/unconfigured
     * @param euBusiness the customer has a VAT ID of another EU country
     * @return the account, null: the account of the rate (0 %)
     */
    static String zeroRateAccount(DatevConfig config, mpv5.utils.export.VatCategory category, boolean euBusiness) {
        if (category == mpv5.utils.export.VatCategory.AE) {
            //reverse charge: a service to an EU business (§ 3a Abs. 2 UStG), else domestic § 13b UStG
            return euBusiness ? config.euServiceAccount : config.reverseChargeAccount;
        }
        if (category == mpv5.utils.export.VatCategory.K) {
            return config.euDeliveryAccount;
        }
        if (category == mpv5.utils.export.VatCategory.G) {
            return config.exportAccount;
        }
        if (category == null && euBusiness) {
            //unconfigured 0 % tax on EU business: fallback to intra-community supply of goods (with warning)
            return config.euDeliveryAccount;
        }
        return null;
    }
}
