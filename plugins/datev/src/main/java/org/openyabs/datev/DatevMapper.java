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
     * Optional resolver for a receipt file name (Beleglink).
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

        public BigDecimal totalGross() {
            BigDecimal sum = BigDecimal.ZERO;
            for (BookingLine l : lines) {
                if (l.umsatz != null) {
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
            if (entry == null || !(entry.obj instanceof Item)) {
                return "";
            }
            Item item = (Item) entry.obj;
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
     * Maps journal entries using the database lookups.
     */
    public static Result map(List<Entry> entries, DatevConfig config, Date from, Date to) {
        return map(entries, config, from, to, dbAccountLookup(), dbContactVatLookup(), null);
    }

    /**
     * Maps journal entries into DATEV booking lines and validates settings and
     * entries.
     */
    public static Result map(List<Entry> entries, DatevConfig config, Date from, Date to,
            AccountLookup accountLookup, ContactVatLookup vatLookup, ReceiptLinkLookup receiptLookup) {
        Result result = new Result();
        validateConfig(config, from, to, result.issues);

        Set<String> warnedMissingRateAccounts = new HashSet<String>();
        int countedDocs = 0;

        if (entries != null) {
            for (Entry e : entries) {
                if (e == null || !e.counted || JournalCalc.isCancelled(e)) {
                    continue;
                }
                countedDocs++;

                Account docAccount = accountLookup == null ? null : accountLookup.accountOf(e);
                if (docAccount == null) {
                    result.issues.add(new Issue(Issue.Severity.WARNING,
                            "Beleg " + labelOf(e) + ": kein Konto zugeordnet (Fallback-Konto wird verwendet)."));
                } else if (docAccount.__getIntaccountclass() <= 0) {
                    result.issues.add(new Issue(Issue.Severity.WARNING,
                            "Beleg " + labelOf(e) + ": Konto '" + docAccount.__getCname() + "' hat keine Kontonummer (intaccountclass)."));
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
                Date bookingDate = resolveBookingDate(e, config.belegdatumMode);
                String buchungstext = resolveBookingText(e);
                String beleglink = receiptLookup == null ? "" : nullToEmpty(receiptLookup.receiptLinkOf(e));
                String contactVat = vatLookup == null ? "" : DatevFormat.sanitizeEuVatId(vatLookup.vatIdOf(e));

                for (Map.Entry<BigDecimal, BigDecimal[]> re : rates.entrySet()) {
                    BigDecimal rate = re.getKey();
                    BigDecimal net = re.getValue()[0] == null ? BigDecimal.ZERO : re.getValue()[0];
                    BigDecimal tax = re.getValue()[1] == null ? BigDecimal.ZERO : re.getValue()[1];
                    BigDecimal gross = net.add(tax);
                    if (gross.signum() == 0) {
                        continue;
                    }

                    // Determine Soll/Haben relative to Konto (money account)
                    // Income: S (money account debited); Expense: H; Credit note: reversed (H)
                    boolean debitMoney = !isExpense && !isCredit;
                    if (gross.signum() < 0) {
                        debitMoney = !debitMoney;
                    }

                    // Determine Gegenkonto
                    String gegenkonto = "";
                    Account effectiveGegenkontoObj = null;
                    boolean docAccountMatches = docAccount != null
                            && docAccount.__getIntaccountclass() > 0
                            && (rate == null || DatevConfig.rateMatches(docAccount.__getTaxvalue(), rate));

                    if (docAccountMatches) {
                        gegenkonto = String.valueOf(docAccount.__getIntaccountclass());
                        effectiveGegenkontoObj = docAccount;
                    } else {
                        // Fallback from settings per tax rate (and EU delivery for 0% with EU VAT ID)
                        if (!isExpense && rate != null && rate.signum() == 0
                                && isOtherEuVatId(contactVat)
                                && !DatevConfig.isBlank(config.euDeliveryAccount)) {
                            gegenkonto = config.euDeliveryAccount.trim();
                        } else if (rate != null) {
                            BigDecimal key = JournalCalc.rateKey(rate);
                            gegenkonto = nullToEmpty(isExpense
                                    ? config.expenseAccounts.get(key)
                                    : config.revenueAccounts.get(key));
                        }
                        if (DatevConfig.isBlank(gegenkonto) && docAccount != null && docAccount.__getIntaccountclass() > 0) {
                            // Last resort: use document account even if taxvalue differs
                            gegenkonto = String.valueOf(docAccount.__getIntaccountclass());
                            effectiveGegenkontoObj = docAccount;
                        } else if (!DatevConfig.isBlank(gegenkonto) && accountLookup != null) {
                            effectiveGegenkontoObj = accountLookup.byNumber(gegenkonto);
                        }
                    }

                    if (DatevConfig.isBlank(gegenkonto)) {
                        String rk = (isExpense ? "Ausgabe:" : "Einnahme:") + (rate == null ? "unbekannt" : rate.toPlainString() + "%");
                        if (warnedMissingRateAccounts.add(rk)) {
                            result.issues.add(new Issue(Issue.Severity.ERROR,
                                    "Kein " + (isExpense ? "Aufwandskonto" : "Erlöskonto") + " für Steuersatz "
                                    + (rate == null ? "unbekannt" : rate.toPlainString() + " %") + " konfiguriert."));
                        }
                    }

                    // BU-Schlüssel
                    String bu = resolveBuKey(rate, isExpense, config, effectiveGegenkontoObj, gegenkonto);

                    // EU-Land u. UStID for 0% EU deliveries (e.g. SKR03 8125 / SKR04 4125)
                    String euVatField = "";
                    boolean isEuAccount = !DatevConfig.isBlank(config.euDeliveryAccount)
                            && config.euDeliveryAccount.trim().equals(gegenkonto);
                    if (!isExpense && rate != null && rate.signum() == 0 && (isEuAccount || isOtherEuVatId(contactVat))) {
                        euVatField = contactVat;
                        if (isEuAccount && DatevConfig.isBlank(euVatField)) {
                            result.issues.add(new Issue(Issue.Severity.WARNING,
                                    "Beleg " + labelOf(e) + ": EU-Lieferung (Konto " + gegenkonto + ") ohne USt-IdNr. beim Kontakt."));
                        }
                    }

                    BookingLine line = new BookingLine();
                    line.umsatz = gross.abs();
                    line.sollHaben = debitMoney ? "S" : "H";
                    line.wkz = config.waehrung;
                    line.konto = konto;
                    line.gegenkonto = gegenkonto;
                    line.buSchluessel = bu;
                    line.belegdatum = DatevFormat.ddmm(bookingDate);
                    line.belegfeld1 = rawNum;
                    line.buchungstext = buchungstext;
                    line.beleglink = beleglink;
                    line.euLandUstId = euVatField;
                    line.festschreibung = config.festschreibung;
                    result.lines.add(line);
                }
            }
        }

        if (countedDocs == 0) {
            result.issues.add(new Issue(Issue.Severity.WARNING,
                    "Keine buchungsrelevanten (bezahlten) Belege im ausgewählten Zeitraum gefunden."));
        }
        return result;
    }

    private static void validateConfig(DatevConfig config, Date from, Date to, List<Issue> issues) {
        if (config.beraterNummer < 1001 || config.beraterNummer > 9999999) {
            issues.add(new Issue(Issue.Severity.ERROR,
                    "Beraternummer fehlt oder ungültig (erlaubt: 1001–9999999)."));
        }
        if (config.mandantenNummer < 1 || config.mandantenNummer > 99999) {
            issues.add(new Issue(Issue.Severity.ERROR,
                    "Mandantennummer fehlt oder ungültig (erlaubt: 1–99999)."));
        }
        if (config.wjBeginn == null || !config.wjBeginn.trim().matches("\\d{8}")) {
            issues.add(new Issue(Issue.Severity.ERROR,
                    "Wirtschaftsjahresbeginn fehlt oder hat nicht das Format JJJJMMTT."));
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
        if (from != null && to != null) {
            SimpleDateFormat yf = new SimpleDateFormat("yyyy");
            if (!yf.format(from).equals(yf.format(to))) {
                issues.add(new Issue(Issue.Severity.WARNING,
                        "Zeitraum erstreckt sich über mehrere Jahre (" + yf.format(from) + "–" + yf.format(to)
                        + "). DATEV erwartet pro Wirtschaftsjahr eine eigene Datei (Belegdatum ist TTMM)."));
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
     * Detects POS / cash register sales ("Barverkauf" contact or "POS" description).
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
        return contact.equals("barverkauf") || contact.startsWith("barverkauf ");
    }

    private static String resolveBuKey(BigDecimal rate, boolean isExpense, DatevConfig config, Account effectiveAccount, String gegenkonto) {
        if (rate == null || rate.signum() == 0) {
            return "";
        }
        if (config.useAutomaticAccounts && effectiveAccount != null
                && DatevConfig.rateMatches(effectiveAccount.__getTaxvalue(), rate)) {
            return "";
        }
        //the automatic account of the rate in SKR03 / SKR04 (8400, 8300, 3400, 3300 / 4400, 4300, 5400, 5300)
        //books the tax itself; DATEV rejects a BU key on it, also when the chart of YaBS does not have it
        if (config.useAutomaticAccounts && gegenkonto != null && gegenkonto.trim().equals(
                DatevConfig.canonicalAccount(isExpense ? Account.EXPENSE : Account.INCOME, JournalCalc.rateKey(rate), "04".equals(config.kontenrahmen)))) {
            return "";
        }
        BigDecimal key = JournalCalc.rateKey(rate);
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
}
