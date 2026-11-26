package org.openyabs.datev;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mrtyldr.datev.core.DatevRowValidator;
import io.github.mrtyldr.datev.core.DatevSchema;
import io.github.mrtyldr.datev.core.DatevValidationError;
import io.github.mrtyldr.datev.core.DatevValidationMode;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import mpv5.db.objects.Account;
import mpv5.db.objects.Item;
import mpv5.globals.Constants;
import mpv5.handler.JournalCalc;
import mpv5.handler.JournalCalc.Entry;
import mpv5.utils.export.VatCategory;
import org.junit.jupiter.api.Test;
import org.openyabs.datev.DatevFormat.BookingLine;

/**
 * Compliance & regression test suite covering the DATEV EXTF 700 v13 /
 * Category 16 / GoBD / UStG / HGB audit findings.
 */
public class DatevComplianceAuditTest {

    private static Date date(String ymd) throws Exception {
        return new SimpleDateFormat("yyyy-MM-dd").parse(ymd);
    }

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    private static Account account(int num, String name, int type, double tax, String frame) {
        Account a = new Account();
        a.setIntaccountclass(num);
        a.setCname(name);
        a.setIntaccounttype(type);
        a.setTaxvalue(tax);
        a.setFrame(frame);
        return a;
    }

    private static DatevConfig skr03Config() {
        DatevConfig c = new DatevConfig();
        c.beraterNummer = 29098;
        c.mandantenNummer = 55003;
        c.wjBeginn = "20260101";
        c.sachkontenLaenge = 4;
        c.kontenrahmen = "03";
        c.bezeichnung = "Buchungsstapel 03/2026";
        c.diktatKuerzel = "YB";
        c.festschreibung = false;
        c.waehrung = "EUR";
        c.moneyAccountIncome = "1200";
        c.moneyAccountExpense = "1200";
        c.cashAccount = "1000";
        c.euDeliveryAccount = "8125";
        c.euServiceAccount = "8336";
        c.reverseChargeAccount = "8337";
        c.exportAccount = "8120";
        c.useAutomaticAccounts = true;
        c.revenueAccounts.put(bd("19"), "8400");
        c.revenueAccounts.put(bd("7"), "8300");
        c.revenueAccounts.put(bd("0"), "8100");
        c.expenseAccounts.put(bd("19"), "3400");
        c.expenseAccounts.put(bd("7"), "3300");
        c.expenseAccounts.put(bd("0"), "4900");
        c.applyDefaultBuKeys();
        return c;
    }

    private static DatevMapper.AccountLookup lookup(Map<Entry, Account> byEntry, Account... chart) {
        final Map<String, Account> byNum = new HashMap<String, Account>();
        for (Account a : chart) {
            byNum.put(String.valueOf(a.__getIntaccountclass()), a);
            if (a.__getIntaccountclass() > 0 && a.__getIntaccountclass() < 1000) {
                byNum.put(String.format("%04d", a.__getIntaccountclass()), a);
            }
        }
        return new DatevMapper.AccountLookup() {
            @Override
            public Account accountOf(Entry entry) {
                return byEntry.get(entry);
            }

            @Override
            public Account byNumber(String accountNumber) {
                return accountNumber == null ? null : byNum.get(accountNumber.trim());
            }
        };
    }

    static List<String> splitCsv(String line) {
        List<String> l = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ';' && !quoted) {
                l.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        l.add(sb.toString());
        return l;
    }

    static void assertStrictDatevValid(String csv) {
        assertTrue(csv.endsWith("\r\n"), "EXTF CSV must end with CRLF");
        assertFalse(csv.replace("\r\n", "").contains("\n"), "No bare LF permitted");
        String[] rows = csv.split("\r\n");
        assertTrue(rows.length >= 2, "At least header + column line");
        assertEquals(31, rows[0].split(";", -1).length, "EXTF 700 header must have 31 fields");
        List<String> heads = splitCsv(rows[1]);
        assertEquals(DatevSchema.CURRENT_V13.columnCount(), heads.size(), "v13 has 125 columns");
        for (int r = 2; r < rows.length; r++) {
            List<String> cols = splitCsv(rows[r]);
            assertEquals(125, cols.size(), "Row " + r + " must have 125 columns");
            List<DatevValidationError> errs = DatevRowValidator.validate(heads, cols, DatevValidationMode.STRICT);
            assertEquals(List.of(), errs, "Row " + r + " failed STRICT validation: " + rows[r]);
        }
    }

    // -------------------------------------------------------------------------
    // Test Case 1: Standard mixed 19% + 7% invoice with cent-exact rounding
    // -------------------------------------------------------------------------
    @Test
    public void standardMixed19And7InvoicePassesStrictDatevValidation() throws Exception {
        DatevConfig cfg = skr03Config();
        Account acc8400 = account(8400, "Erlöse 19% USt", Account.INCOME, 19.0, "de_skr03");
        Account acc8300 = account(8300, "Erlöse 7% USt", Account.INCOME, 7.0, "de_skr03");

        Entry inv = new Entry();
        inv.type = Constants.TYPE_INVOICE;
        inv.status = Item.STATUS_PAID;
        inv.date = date("2026-03-10");
        inv.number = "RE-2026-0100";
        inv.contact = "Musterfirma GmbH";
        inv.net = bd("180.00");
        inv.tax = bd("23.40");
        inv.gross = bd("203.40");
        Map<BigDecimal, BigDecimal[]> pos = new TreeMap<BigDecimal, BigDecimal[]>(JournalCalc.RATE_ORDER);
        pos.put(bd("19"), new BigDecimal[]{bd("100.00"), bd("19.00")});
        pos.put(bd("7"), new BigDecimal[]{bd("100.00"), bd("7.00")});
        inv.rates = JournalCalc.split(inv.net, inv.tax, pos);
        JournalCalc.compute(Collections.singletonList(inv), Collections.emptySet());

        Map<Entry, Account> accMap = Collections.singletonMap(inv, acc8400);
        DatevMapper.Result res = DatevMapper.map(Collections.singletonList(inv), cfg,
                date("2026-03-01"), date("2026-03-31"), lookup(accMap, acc8400, acc8300), null, null, null);

        assertFalse(res.hasErrors(), "No errors expected: " + res.issues);
        assertEquals(2, res.lines.size());
        BookingLine l7 = res.lines.get(0);
        BookingLine l19 = res.lines.get(1);
        assertEquals(bd("96.30"), l7.umsatz);
        assertEquals("S", l7.sollHaben);
        assertEquals("8300", l7.gegenkonto);
        assertEquals("", l7.buSchluessel);
        assertEquals("1003", l7.belegdatum);
        assertEquals(bd("107.10"), l19.umsatz);
        assertEquals("8400", l19.gegenkonto);
        assertEquals("", l19.buSchluessel);
        assertEquals(bd("203.40"), l7.umsatz.add(l19.umsatz));

        String csv = DatevFormat.render(cfg, date("2026-03-01"), date("2026-03-31"),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2026-04-01 10:00:00.000"), res.lines);
        assertStrictDatevValid(csv);
    }

    // -------------------------------------------------------------------------
    // CRITICAL-6: Credit notes (positive AND negative stored) must never double-invert
    // -------------------------------------------------------------------------
    @Test
    public void creditNotesPositiveAndNegativeBothBookAsHabenWithoutDoubleInversion() throws Exception {
        DatevConfig cfg = skr03Config();
        Account acc8400 = account(8400, "Erlöse 19% USt", Account.INCOME, 19.0, "de_skr03");

        Entry posCredit = new Entry();
        posCredit.type = Constants.TYPE_CREDIT;
        posCredit.status = Item.STATUS_PAID;
        posCredit.date = date("2026-03-15");
        posCredit.number = "GS-2026-0001";
        posCredit.contact = "Kunde Eins GmbH";
        posCredit.net = bd("50.00");
        posCredit.tax = bd("9.50");
        posCredit.gross = bd("59.50");
        posCredit.rates.put(bd("19"), new BigDecimal[]{bd("50.00"), bd("9.50")});

        // Credit note stored with negative amounts (common in YaBS / imported DBs)
        Entry negCredit = new Entry();
        negCredit.type = Constants.TYPE_CREDIT;
        negCredit.status = Item.STATUS_PAID;
        negCredit.date = date("2026-03-16");
        negCredit.number = "ST-2026-0002";
        negCredit.contact = "Kunde Eins GmbH";
        negCredit.net = bd("-100.00");
        negCredit.tax = bd("-19.00");
        negCredit.gross = bd("-119.00");
        negCredit.rates.put(bd("19"), new BigDecimal[]{bd("-100.00"), bd("-19.00")});

        List<Entry> list = Arrays.asList(posCredit, negCredit);
        JournalCalc.Totals totals = JournalCalc.compute(list, Collections.emptySet());
        // Both credit notes must REDUCE income, never add to it
        assertEquals(0, bd("-150.00").compareTo(totals.incomeNet), "Both credits reduce incomeNet: " + totals.incomeNet);
        assertEquals(0, bd("-28.50").compareTo(totals.incomeTax), "Both credits reduce incomeTax: " + totals.incomeTax);
        assertEquals(0, bd("-178.50").compareTo(totals.incomeGross), "Both credits reduce incomeGross: " + totals.incomeGross);

        Map<Entry, Account> accMap = new HashMap<Entry, Account>();
        accMap.put(posCredit, acc8400);
        accMap.put(negCredit, acc8400);

        DatevMapper.Result res = DatevMapper.map(list, cfg, date("2026-03-01"), date("2026-03-31"),
                lookup(accMap, acc8400), null, null, null);
        assertFalse(res.hasErrors(), res.issues.toString());
        assertEquals(2, res.lines.size());

        BookingLine lPos = res.lines.get(0);
        BookingLine lNeg = res.lines.get(1);
        assertEquals(bd("59.50"), lPos.umsatz);
        assertEquals("H", lPos.sollHaben, "Positive-stored credit note must book Haben on money account");
        assertEquals(bd("119.00"), lNeg.umsatz);
        assertEquals("H", lNeg.sollHaben, "Negative-stored credit note must ALSO book Haben (no double-inversion!)");
        assertEquals(0, bd("178.50").compareTo(res.sumCredit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(res.sumDebit()));

        String csv = DatevFormat.render(cfg, date("2026-03-01"), date("2026-03-31"), date("2026-04-01"), res.lines);
        assertStrictDatevValid(csv);
    }

    // -------------------------------------------------------------------------
    // CRITICAL-5: Zero-rate EU / RC / Export routing, Greece EL, explicit E, col 40
    // -------------------------------------------------------------------------
    @Test
    public void euReverseChargeEuGoodsGreeceAndExplicitExemptDoNotBypassOrLeakCol40() throws Exception {
        DatevConfig cfg = skr03Config();
        // Assign a 0% document account (8100) to all invoices to verify CRITICAL-5:
        // a 0% docAccount must NEVER override explicit K / AE / G zero-rate regimes!
        Account acc8100 = account(8100, "Steuerfreie Umsätze §4Nr.8ff UstG", Account.INCOME, 0.0, "de_skr03");

        Entry euService = zeroInvoice("RE-EU-SRV", "Athen Consulting SA", "1000.00");
        Entry euGoods = zeroInvoice("RE-EU-GDS", "Client France SAS", "2000.00");
        Entry domExempt = zeroInvoice("RE-DOM-EX", "Wien GmbH", "500.00");
        Entry exportSale = zeroInvoice("RE-EXP-US", "Zurich AG", "800.00");

        List<Entry> all = Arrays.asList(euService, euGoods, domExempt, exportSale);
        JournalCalc.compute(all, Collections.emptySet());

        Map<Entry, Account> accMap = new HashMap<Entry, Account>();
        for (Entry e : all) {
            accMap.put(e, acc8100);
        }
        Map<Entry, VatCategory> cats = new HashMap<Entry, VatCategory>();
        cats.put(euService, VatCategory.AE);
        cats.put(euGoods, VatCategory.K);
        cats.put(domExempt, VatCategory.E); // explicit domestic tax-free (§ 4 Nr. 22 UStG)
        cats.put(exportSale, VatCategory.G);

        DatevMapper.ContactVatLookup vats = e -> {
            if (e == euService) return "EL 094014201";     // Greece uses EL!
            if (e == euGoods) return "FR 40 303265045";
            if (e == domExempt) return "ATU12345678";      // EU customer buying domestic exempt seminar
            if (e == exportSale) return "FR99999999999";   // customer happens to have EU VAT ID on file
            return "";
        };

        DatevMapper.Result res = DatevMapper.map(all, cfg, date("2026-03-01"), date("2026-03-31"),
                lookup(accMap, acc8100), vats, null, (e, r) -> cats.get(e));

        assertFalse(res.hasErrors(), res.issues.toString());
        Map<String, BookingLine> byNum = new HashMap<String, BookingLine>();
        for (BookingLine l : res.lines) {
            byNum.put(l.belegfeld1, l);
        }

        // 1. EU B2B Reverse Charge service -> 8336 + EL VAT ID
        assertEquals("8336", byNum.get("RE-EU-SRV").gegenkonto, "0% docAccount must not override AE EU service");
        assertEquals("EL094014201", byNum.get("RE-EU-SRV").euLandUstId);

        // 2. EU goods supply -> 8125 + FR VAT ID
        assertEquals("8125", byNum.get("RE-EU-GDS").gegenkonto, "0% docAccount must not override K EU goods");
        assertEquals("FR40303265045", byNum.get("RE-EU-GDS").euLandUstId);

        // 3. Explicit VatCategory.E domestic exempt to Austrian business -> stays 8100, NO col 40 leakage!
        assertEquals("8100", byNum.get("RE-DOM-EX").gegenkonto, "Explicit VatCategory.E must stay domestic 8100");
        assertEquals("", byNum.get("RE-DOM-EX").euLandUstId, "Col 40 must NOT be populated on domestic exempt 8100");

        // 4. Export G -> 8120, NO col 40 leakage even if customer has EU VAT ID on file!
        assertEquals("8120", byNum.get("RE-EXP-US").gegenkonto, "0% docAccount must not override G export");
        assertEquals("", byNum.get("RE-EXP-US").euLandUstId, "Col 40 must NOT be populated on export 8120");

        String csv = DatevFormat.render(cfg, date("2026-03-01"), date("2026-03-31"), date("2026-04-01"), res.lines);
        assertStrictDatevValid(csv);
    }

    private static Entry zeroInvoice(String num, String contact, String amount) throws Exception {
        Entry e = new Entry();
        e.type = Constants.TYPE_INVOICE;
        e.status = Item.STATUS_PAID;
        e.date = date("2026-03-20");
        e.number = num;
        e.contact = contact;
        e.net = bd(amount);
        e.tax = bd("0.00");
        e.gross = bd(amount);
        e.rates.put(BigDecimal.ZERO, new BigDecimal[]{bd(amount), bd("0.00")});
        return e;
    }

    // -------------------------------------------------------------------------
    // CRITICAL-1 & HIGH-4: Non-automatic SKR03/SKR04 accounts MUST emit BU keys
    // and class-0 accounts (480, 670, 27) MUST left-pad to 4 digits (0480, 0670, 0027)
    // -------------------------------------------------------------------------
    @Test
    public void nonAutomaticAccountsEmitBuKeyAndClass0AccountsPadToFourDigits() throws Exception {
        DatevConfig cfg = skr03Config();
        Account acc8400 = account(8400, "Erlöse 19% USt", Account.INCOME, 19.0, "de_skr03");
        Account acc4930 = account(4930, "Bürobedarf", Account.EXPENSE, 19.0, "de_skr03");
        Account acc4980 = account(4980, "Betriebsbedarf", Account.EXPENSE, 19.0, "de_skr03");
        Account acc4600 = account(4600, "Werbekosten", Account.EXPENSE, 19.0, "de_skr03");
        Account acc480 = account(480, "Geringwertige Wirtschaftsgüter", Account.OTHER, 19.0, "de_skr03");
        Account acc3300 = account(3300, "Wareneingang 7% Vorsteuer", Account.EXPENSE, 7.0, "de_skr03");

        Entry invAuto = paidEntry(Constants.TYPE_INVOICE, Item.STATUS_PAID, "2026-03-10", "RE-AUTO", "Kunde GmbH", "100.00", "19.00", "19");
        Entry expBuero = paidEntry(Constants.TYPE_EXPENSE, JournalCalc.EXPENSE_PAID, "2026-03-11", "AUS-BUERO", "Büromarkt AG", "100.00", "19.00", "19");
        Entry expGwg = paidEntry(Constants.TYPE_EXPENSE, JournalCalc.EXPENSE_PAID, "2026-03-12", "AUS-GWG", "Monitor GWG", "500.00", "95.00", "19");
        Entry expWare7 = paidEntry(Constants.TYPE_EXPENSE, JournalCalc.EXPENSE_PAID, "2026-03-13", "AUS-WARE", "Großhandel KG", "100.00", "7.00", "7");
        Entry expWerbung = paidEntry(Constants.TYPE_EXPENSE, JournalCalc.EXPENSE_PAID, "2026-03-14", "AUS-WERB", "Ads GmbH", "200.00", "38.00", "19");

        List<Entry> all = Arrays.asList(invAuto, expBuero, expGwg, expWare7, expWerbung);
        JournalCalc.compute(all, Collections.emptySet());

        Map<Entry, Account> accMap = new HashMap<Entry, Account>();
        accMap.put(invAuto, acc8400);
        accMap.put(expBuero, acc4930);
        accMap.put(expGwg, acc480);
        accMap.put(expWare7, acc3300);
        accMap.put(expWerbung, acc4600);

        DatevMapper.Result res = DatevMapper.map(all, cfg, date("2026-03-01"), date("2026-03-31"),
                lookup(accMap, acc8400, acc4930, acc4980, acc4600, acc480, acc3300), null, null, null);

        assertFalse(res.hasErrors(), "No errors expected: " + res.issues);
        Map<String, BookingLine> byNum = new HashMap<String, BookingLine>();
        for (BookingLine l : res.lines) {
            byNum.put(l.belegfeld1, l);
        }

        // DATEV AM/AV automatic accounts -> NO BU key
        assertEquals("8400", byNum.get("RE-AUTO").gegenkonto);
        assertEquals("", byNum.get("RE-AUTO").buSchluessel, "8400 is an Automatikkonto");
        assertEquals("3300", byNum.get("AUS-WARE").gegenkonto);
        assertEquals("", byNum.get("AUS-WARE").buSchluessel, "3300 is an Automatikkonto");

        // Non-automatic accounts with taxvalue=19.0 in YaBS XML -> MUST emit BU key 9!
        assertEquals("4930", byNum.get("AUS-BUERO").gegenkonto);
        assertEquals("9", byNum.get("AUS-BUERO").buSchluessel, "4930 Bürobedarf is NOT an Automatikkonto -> BU 9 required!");
        assertEquals("4600", byNum.get("AUS-WERB").gegenkonto);
        assertEquals("9", byNum.get("AUS-WERB").buSchluessel, "4600 Werbekosten is NOT an Automatikkonto -> BU 9 required!");

        // Class-0 GWG account 480 -> left-padded to 0480 + BU key 9!
        assertEquals("0480", byNum.get("AUS-GWG").gegenkonto, "Class-0 account 480 must be padded to 0480 for SKL=4");
        assertEquals("9", byNum.get("AUS-GWG").buSchluessel, "0480 GWG is NOT an Automatikkonto -> BU 9 required!");

        // Also check SKR04 non-automatic accounts (6815 Bürobedarf, 0670 GWG) vs automatic (4400, 5400)
        DatevConfig skr04 = skr03Config();
        skr04.kontenrahmen = "04";
        skr04.moneyAccountIncome = "1800";
        skr04.moneyAccountExpense = "1800";
        Account acc6815 = account(6815, "Bürobedarf", Account.EXPENSE, 19.0, "de_skr04");
        Account acc670 = account(670, "GWG", Account.OTHER, 19.0, "de_skr04");
        Account acc4400 = account(4400, "Erlöse 19%", Account.INCOME, 19.0, "de_skr04");
        DatevMapper.Booking b6815 = DatevMapper.booking(bd("19"), true, acc6815, null, "", skr04, null);
        assertEquals("6815", b6815.account);
        assertEquals("9", b6815.bu, "SKR04 6815 Bürobedarf requires BU 9");
        DatevMapper.Booking b670 = DatevMapper.booking(bd("19"), true, acc670, null, "", skr04, null);
        assertEquals("0670", b670.account, "SKR04 class-0 account 670 must pad to 0670");
        assertEquals("9", b670.bu, "SKR04 0670 GWG requires BU 9");
        DatevMapper.Booking b4400 = DatevMapper.booking(bd("19"), false, acc4400, null, "", skr04, null);
        assertEquals("4400", b4400.account);
        assertEquals("", b4400.bu, "SKR04 4400 is an Automatikkonto");

        String csv = DatevFormat.render(cfg, date("2026-03-01"), date("2026-03-31"), date("2026-04-01"), res.lines);
        assertStrictDatevValid(csv);
    }

    private static Entry paidEntry(int type, int status, String ymd, String num, String contactOrDesc,
            String net, String tax, String rate) throws Exception {
        Entry e = new Entry();
        e.type = type;
        e.status = status;
        e.date = date(ymd);
        e.number = num;
        if (type == Constants.TYPE_EXPENSE) {
            e.description = contactOrDesc;
        } else {
            e.contact = contactOrDesc;
        }
        e.net = bd(net);
        e.tax = bd(tax);
        e.gross = bd(net).add(bd(tax));
        e.rates.put(bd(rate), new BigDecimal[]{bd(net), bd(tax)});
        return e;
    }

    // -------------------------------------------------------------------------
    // HIGH-5: Lossless CP1252 sanitization (äöüß€, macOS NFD, Polish/Czech, emojis)
    // -------------------------------------------------------------------------
    @Test
    public void cp1252SanitizationPreservesUmlautsEuroAndAvoidsQuestionMarkCorruption() throws Exception {
        DatevConfig cfg = skr03Config();
        cfg.bezeichnung = "März — Łódź € 2026 \uD83D\uDE80";

        BookingLine line = new BookingLine();
        line.umsatz = bd("119.00");
        line.sollHaben = "S";
        line.konto = "1200";
        line.gegenkonto = "8400";
        line.belegdatum = "2503";
        line.belegfeld1 = "RE-2026-ÄÖÜ";
        // NFD ü (u + combining diaeresis), em-dash, Polish Ł/ó/ź, Czech Č, Euro, emoji, quotes
        line.buchungstext = "Mu\u0308ller & Söhne \u2014 \u0141\u00f3d\u017a \u010cS \u20ac 100 \uD83D\uDE80 \"Top\"";

        String csv = DatevFormat.render(cfg, date("2026-03-01"), date("2026-03-31"), date("2026-04-01"),
                Collections.singletonList(line));
        byte[] bytes = DatevFormat.encode(csv);

        // Must contain ZERO '?' (0x3F) replacement characters!
        for (byte b : bytes) {
            assertFalse(b == (byte) 0x3F, "CP1252 output must not contain '?' replacement bytes: "
                    + new String(bytes, DatevFormat.CHARSET));
        }
        // Verify exact CP1252 encoding of ü (0xFC), ö (0xF6), € (0x80)
        boolean hasEuro = false;
        boolean hasUmlautU = false;
        for (byte b : bytes) {
            if (b == (byte) 0x80) hasEuro = true;
            if (b == (byte) 0xFC) hasUmlautU = true;
        }
        assertTrue(hasEuro, "Euro sign must be encoded as CP1252 0x80");
        assertTrue(hasUmlautU, "NFD ü must normalize to NFC and encode as CP1252 0xFC");

        String decoded = new String(bytes, DatevFormat.CHARSET);
        String[] rows = decoded.split("\r\n");
        List<String> cols = splitCsv(rows[2]);
        assertEquals("RE-2026-AeOeUe", cols.get(10));
        assertEquals("Müller & Söhne \u2014 Lódz CS € 100 \"Top\"", cols.get(13));

        // Surrogate-pair truncation test right at index 59-60
        String withTrailingEmoji = "A".repeat(59) + "\uD83D\uDE80" + "B";
        String truncated = DatevFormat.sanitizeText(withTrailingEmoji, 60);
        assertTrue(truncated.length() <= 60);
        for (int i = 0; i < truncated.length(); i++) {
            assertFalse(Character.isSurrogate(truncated.charAt(i)), "Must never leave a lone surrogate");
        }
        assertStrictDatevValid(csv);
    }

    // -------------------------------------------------------------------------
    // CRITICAL-3: WJ-Beginn validation & deviating fiscal year support
    // -------------------------------------------------------------------------
    @Test
    public void wjBeginnValidationRejectsMismatchAndInvalidDateAndAllowsDeviatingFiscalYear() throws Exception {
        Account acc8400 = account(8400, "Erlöse 19% USt", Account.INCOME, 19.0, "de_skr03");

        // Subtest A: Prior-year export (Dec 2026) with stale default WJ-Beginn 20270101 -> MUST be ERROR
        DatevConfig stale = skr03Config();
        stale.wjBeginn = "20270101";
        Entry dec2026 = paidEntry(Constants.TYPE_INVOICE, Item.STATUS_PAID, "2026-12-15", "RE-2026-12", "Kunde", "100.00", "19.00", "19");
        JournalCalc.compute(Collections.singletonList(dec2026), Collections.emptySet());
        DatevMapper.Result resStale = DatevMapper.map(Collections.singletonList(dec2026), stale,
                date("2026-12-01"), date("2026-12-31"), lookup(Collections.singletonMap(dec2026, acc8400), acc8400), null, null, null);
        assertTrue(resStale.hasErrors(), "Export period outside WJ-Beginn fiscal year must be ERROR: " + resStale.issues);
        assertTrue(resStale.issues.stream().anyMatch(i -> i.severity == DatevMapper.Issue.Severity.ERROR
                && i.message.contains("Wirtschaftsjahr")), resStale.issues.toString());

        // Subtest B: Impossible calendar date 20269999 -> MUST be ERROR
        DatevConfig invalidWj = skr03Config();
        invalidWj.wjBeginn = "20269999";
        DatevMapper.Result resInvalid = DatevMapper.map(Collections.singletonList(dec2026), invalidWj,
                date("2026-12-01"), date("2026-12-31"), lookup(Collections.singletonMap(dec2026, acc8400), acc8400), null, null, null);
        assertTrue(resInvalid.hasErrors(), "Invalid calendar date 20269999 must be ERROR: " + resInvalid.issues);

        // Subtest C: Deviating fiscal year (WJ-Beginn = 20250701 -> FY 2025-07-01..2026-06-30),
        // period 2025-12-01..2026-01-31 spans two calendar years WITHIN one FY -> ZERO errors & ZERO multi-year warnings!
        DatevConfig deviating = skr03Config();
        deviating.wjBeginn = "20250701";
        Entry dec2025 = paidEntry(Constants.TYPE_INVOICE, Item.STATUS_PAID, "2025-12-20", "RE-2025-12", "Kunde", "100.00", "19.00", "19");
        Entry jan2026 = paidEntry(Constants.TYPE_INVOICE, Item.STATUS_PAID, "2026-01-10", "RE-2026-01", "Kunde", "100.00", "19.00", "19");
        List<Entry> devEntries = Arrays.asList(dec2025, jan2026);
        JournalCalc.compute(devEntries, Collections.emptySet());
        Map<Entry, Account> devAcc = new HashMap<Entry, Account>();
        devAcc.put(dec2025, acc8400);
        devAcc.put(jan2026, acc8400);
        DatevMapper.Result resDev = DatevMapper.map(devEntries, deviating,
                date("2025-12-01"), date("2026-01-31"), lookup(devAcc, acc8400), null, null, null);
        assertFalse(resDev.hasErrors(), "Valid deviating fiscal year must produce zero errors: " + resDev.issues);
        assertTrue(resDev.issues.stream().noneMatch(i -> i.message.contains("mehrere Jahre")),
                "Valid deviating fiscal year must NOT emit false-positive multi-year warning: " + resDev.issues);
    }

    // -------------------------------------------------------------------------
    // HIGH-4: Sachkontenlänge validation
    // -------------------------------------------------------------------------
    @Test
    public void sachkontenLaengeRejectsMismatchedAccountLengthsAndAllowsPersonenkonto() throws Exception {
        DatevConfig cfg = skr03Config();
        cfg.sachkontenLaenge = 4;
        cfg.revenueAccounts.put(bd("19"), "840000"); // 6 digits invalid for SKL=4!

        Entry inv = paidEntry(Constants.TYPE_INVOICE, Item.STATUS_PAID, "2026-03-10", "RE-1", "Kunde", "100.00", "19.00", "19");
        JournalCalc.compute(Collections.singletonList(inv), Collections.emptySet());
        DatevMapper.Result bad = DatevMapper.map(Collections.singletonList(inv), cfg,
                date("2026-03-01"), date("2026-03-31"), null, null, null, null);
        assertTrue(bad.hasErrors(), "6-digit account in SKL=4 batch must produce ERROR: " + bad.issues);

        // 5-digit Personenkonto (10001) on SKL=4 MUST be accepted
        cfg.revenueAccounts.put(bd("19"), "8400");
        cfg.moneyAccountIncome = "10001";
        DatevMapper.Result ok = DatevMapper.map(Collections.singletonList(inv), cfg,
                date("2026-03-01"), date("2026-03-31"), null, null, null, null);
        assertFalse(ok.hasErrors(), "5-digit Personenkonto on SKL=4 must be valid: " + ok.issues);
    }

    // -------------------------------------------------------------------------
    // MEDIUM-3, MEDIUM-4, HIGH-3, LOW-2, MEDIUM-6
    // -------------------------------------------------------------------------
    @Test
    public void belegfeld1CollisionAndDiktatAndDocumentDateWarningAndCashSaleHeuristic() throws Exception {
        // 1-letter Diktatkürzel preserved
        assertEquals("A", DatevFormat.sanitizeDiktat("a"));
        assertEquals("YB", DatevFormat.sanitizeDiktat("yb"));

        // Cash sale heuristic must NOT misroute "Barverkauf GmbH" to cash register 1000
        Entry b2b = paidEntry(Constants.TYPE_INVOICE, Item.STATUS_PAID, "2026-03-10", "RE-1", "Barverkauf GmbH", "100.00", "19.00", "19");
        assertFalse(DatevMapper.isCashSale(b2b), "Company 'Barverkauf GmbH' must not be treated as cash POS sale");
        Entry pos = paidEntry(Constants.TYPE_INVOICE, Item.STATUS_PAID, "2026-03-10", "BON-1", "Barverkauf", "10.00", "1.90", "19");
        assertTrue(DatevMapper.isCashSale(pos), "Contact 'Barverkauf' is a cash sale");

        // Belegfeld 1 collision across two distinct documents produces warning
        DatevConfig cfg = skr03Config();
        cfg.belegdatumMode = DatevConfig.DateMode.DOCUMENT_DATE;
        Account acc8400 = account(8400, "Erlöse 19% USt", Account.INCOME, 19.0, "de_skr03");
        Entry d1 = paidEntry(Constants.TYPE_INVOICE, Item.STATUS_PAID, "2026-03-10", "RE_2026_1", "Kunde A", "100.00", "19.00", "19");
        Entry d2 = paidEntry(Constants.TYPE_INVOICE, Item.STATUS_PAID, "2026-03-11", "RE.2026.1", "Kunde B", "200.00", "38.00", "19");
        List<Entry> list = Arrays.asList(d1, d2);
        JournalCalc.compute(list, Collections.emptySet());
        Map<Entry, Account> am = new HashMap<Entry, Account>();
        am.put(d1, acc8400);
        am.put(d2, acc8400);
        DatevMapper.Result res = DatevMapper.map(list, cfg, date("2026-03-01"), date("2026-03-31"),
                lookup(am, acc8400), null, null, null);
        assertTrue(res.issues.stream().anyMatch(i -> i.message.contains("Belegfeld 1") && i.message.contains("kollidiert")),
                "Duplicate sanitized Belegfeld 1 must warn: " + res.issues);
        assertTrue(res.issues.stream().anyMatch(i -> i.message.contains("Soll-Versteuerung") || i.message.contains("Geldkonto")),
                "DOCUMENT_DATE with Geldkonto must warn about Soll-Versteuerung/accrual limitation: " + res.issues);
    }

    @Test
    public void skr03XmlHasCleanTaxUidAndUpToDateGwgLabel() throws Exception {
        String raw = Files.readString(new File("ext/accounts-german-skr03.xml").toPath(), StandardCharsets.UTF_8);
        assertFalse(raw.contains("<inttaxuid>16</inttaxuid>"), "Obsolete 16% tax UID must not remain in SKR03 XML");
        assertFalse(raw.contains("410EUR"), "Outdated 410EUR GWG label must be updated");
    }

    // -------------------------------------------------------------------------
    // MEDIUM-5: DatevXmlExport handles negative bucket & class-0 account
    // -------------------------------------------------------------------------
    @Test
    public void datevXmlExportPreservesNegativeBucketInRegularInvoice() throws Exception {
        DatevConfig cfg = skr03Config();
        Entry inv = new Entry();
        inv.type = Constants.TYPE_INVOICE;
        inv.status = Item.STATUS_PAID;
        inv.date = date("2026-03-10");
        inv.number = "RE-RET-1";
        inv.contact = "Kunde AG";
        // 19% goods 200 net (+38 tax = 238 gross) minus 7% return -50 net (-3.50 tax = -53.50 gross) => total 150 net, 34.50 tax, 184.50 gross
        inv.net = bd("150.00");
        inv.tax = bd("34.50");
        inv.gross = bd("184.50");
        inv.rates.put(bd("19"), new BigDecimal[]{bd("200.00"), bd("38.00")});
        inv.rates.put(bd("7"), new BigDecimal[]{bd("-50.00"), bd("-3.50")});

        DatevXmlExport.Collected col = DatevXmlExport.collect(Collections.singletonList(inv), cfg,
                Collections.emptySet(), null, null, Collections.emptySet(), false, (n, r) -> n + ": " + r);
        assertEquals(1, col.docs.size());
        assertEquals(0, bd("184.50").compareTo(col.docs.get(0).gross()), "Gross must reconcile with negative line: " + col.docs.get(0).gross());
        assertTrue(col.problems.stream().noneMatch(p -> p.contains("weicht vom Beleg ab")),
                "Must not mismatch document total: " + col.problems);
    }

    // -------------------------------------------------------------------------
    // HIGH-2: adviserPackageEntries must not silently bundle an invalid EXTF CSV
    // -------------------------------------------------------------------------
    @Test
    public void adviserPackageEntriesRejectsInvalidConfigInsteadOfBundlingBrokenCsv() throws Exception {
        DatevConfig bad = new DatevConfig(); // beraterNummer=0, mandantenNummer=0 -> ERROR
        Entry inv = paidEntry(Constants.TYPE_INVOICE, Item.STATUS_PAID, "2026-03-10", "RE-1", "Kunde", "100.00", "19.00", "19");
        JournalCalc.compute(Collections.singletonList(inv), Collections.emptySet());
        Account acc8400 = account(8400, "Erlöse 19% USt", Account.INCOME, 19.0, "de_skr03");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                DatevPlugin.buildAdviserPackageEntries(Collections.singletonList(inv), bad,
                        date("2026-03-01"), date("2026-03-31"),
                        lookup(Collections.singletonMap(inv, acc8400), acc8400),
                        null, null, null, null, null, null));
        assertTrue(ex.getMessage().contains("Fehler"), ex.getMessage());

        // Valid config succeeds and emits a valid EXTF CSV entry under datev/EXTF_Buchungsstapel_...csv
        DatevConfig good = skr03Config();
        Map<String, byte[]> files = DatevPlugin.buildAdviserPackageEntries(Collections.singletonList(inv), good,
                date("2026-03-01"), date("2026-03-31"),
                lookup(Collections.singletonMap(inv, acc8400), acc8400),
                null, null, null, null, null, null);
        assertEquals(1, files.size());
        String key = files.keySet().iterator().next();
        assertTrue(key.startsWith("datev/EXTF_Buchungsstapel_"), key);
        assertStrictDatevValid(new String(files.get(key), DatevFormat.CHARSET));
    }

    // -------------------------------------------------------------------------
    // CRITICAL-2: Foreign currency guard & Kurs / Basis-Umsatz / WKZ Basis-Umsatz
    // -------------------------------------------------------------------------
    @Test
    public void foreignCurrencyRequiresConversionDataAndRendersCols3To6() throws Exception {
        DatevConfig cfg = skr03Config();
        Account acc8400 = account(8400, "Erlöse 19% USt", Account.INCOME, 19.0, "de_skr03");
        Entry usdInv = paidEntry(Constants.TYPE_INVOICE, Item.STATUS_PAID, "2026-03-10", "RE-USD-1", "US Client Inc.", "1000.00", "0.00", "0");
        JournalCalc.compute(Collections.singletonList(usdInv), Collections.emptySet());

        // Subtest A: USD invoice WITHOUT Kurs / Basis-Umsatz -> MUST produce ERROR
        DatevMapper.Result missingFx = DatevMapper.map(Collections.singletonList(usdInv), cfg,
                date("2026-03-01"), date("2026-03-31"),
                lookup(Collections.singletonMap(usdInv, acc8400), acc8400),
                null, null, (e, r) -> VatCategory.G, null,
                e -> new DatevMapper.CurrencyInfo("USD"), null);
        assertTrue(missingFx.hasErrors(), "Foreign currency without Kurs/Basis-Umsatz must be ERROR: " + missingFx.issues);
        assertTrue(missingFx.issues.stream().anyMatch(i -> i.severity == DatevMapper.Issue.Severity.ERROR
                && i.message.contains("USD")), missingFx.issues.toString());

        // Subtest B: USD invoice WITH Kurs & Basis-Umsatz -> valid EXTF row with cols 3..6 populated
        DatevMapper.Result withFx = DatevMapper.map(Collections.singletonList(usdInv), cfg,
                date("2026-03-01"), date("2026-03-31"),
                lookup(Collections.singletonMap(usdInv, acc8400), acc8400),
                null, null, (e, r) -> VatCategory.G, null,
                e -> new DatevMapper.CurrencyInfo("USD", bd("1.0850"), bd("921.66"), "EUR"), null);
        assertFalse(withFx.hasErrors(), "Complete foreign currency conversion must not error: " + withFx.issues);
        assertEquals(1, withFx.lines.size());
        BookingLine line = withFx.lines.get(0);
        assertEquals("USD", line.wkz);
        assertEquals(0, bd("1.085").compareTo(line.kurs));
        assertEquals(0, bd("921.66").compareTo(line.basisUmsatz));
        assertEquals("EUR", line.wkzBasisUmsatz);

        String csv = DatevFormat.render(cfg, date("2026-03-01"), date("2026-03-31"), date("2026-04-01"), withFx.lines);
        List<String> cols = splitCsv(csv.split("\r\n")[2]);
        assertEquals("1000,00", cols.get(0));
        assertEquals("USD", cols.get(2));
        assertEquals("1,085", cols.get(3));
        assertEquals("921,66", cols.get(4));
        assertEquals("EUR", cols.get(5));
        assertStrictDatevValid(csv);
    }

    // -------------------------------------------------------------------------
    // CRITICAL-4: Leistungsdatum (col 115) & Datum Zuord. Steuerperiode (col 116)
    // -------------------------------------------------------------------------
    @Test
    public void leistungsdatumYearEndCutoffPopulatesCols115And116() throws Exception {
        DatevConfig cfg = skr03Config();
        cfg.wjBeginn = "20270101";
        Account acc8400 = account(8400, "Erlöse 19% USt", Account.INCOME, 19.0, "de_skr03");

        Entry yearEnd = paidEntry(Constants.TYPE_INVOICE, Item.STATUS_PAID, "2027-01-05", "RE-2027-01", "Kunde A", "100.00", "19.00", "19");
        Entry sameDay = paidEntry(Constants.TYPE_INVOICE, Item.STATUS_PAID, "2027-01-05", "RE-2027-02", "Kunde B", "100.00", "19.00", "19");
        Entry sameMonth = paidEntry(Constants.TYPE_INVOICE, Item.STATUS_PAID, "2027-01-10", "RE-2027-03", "Kunde C", "100.00", "19.00", "19");
        List<Entry> entries = Arrays.asList(yearEnd, sameDay, sameMonth);
        JournalCalc.compute(entries, Collections.emptySet());

        Map<Entry, Account> am = new HashMap<Entry, Account>();
        for (Entry e : entries) {
            am.put(e, acc8400);
        }
        DatevMapper.ServiceDateLookup svcDates = e -> {
            try {
                if (e == yearEnd) return date("2026-12-28");
                if (e == sameDay) return date("2027-01-05");
                if (e == sameMonth) return date("2027-01-02");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return null;
        };

        DatevMapper.Result res = DatevMapper.map(entries, cfg, date("2027-01-01"), date("2027-01-31"),
                lookup(am, acc8400), null, null, null, null, null, svcDates);
        assertFalse(res.hasErrors(), res.issues.toString());
        assertEquals(3, res.lines.size());

        BookingLine lYearEnd = res.lines.get(0);
        assertEquals("0501", lYearEnd.belegdatum);
        assertEquals("28122026", lYearEnd.leistungsdatum);
        assertEquals("28122026", lYearEnd.steuerperiodeDatum);

        BookingLine lSameDay = res.lines.get(1);
        assertEquals("", lSameDay.leistungsdatum);
        assertEquals("", lSameDay.steuerperiodeDatum);

        BookingLine lSameMonth = res.lines.get(2);
        assertEquals("02012027", lSameMonth.leistungsdatum);
        assertEquals("", lSameMonth.steuerperiodeDatum);

        String csv = DatevFormat.render(cfg, date("2027-01-01"), date("2027-01-31"), date("2027-02-01"), res.lines);
        String[] rows = csv.split("\r\n");
        List<String> colsYearEnd = splitCsv(rows[2]);
        assertEquals("28122026", colsYearEnd.get(114)); // col 115 (1-based)
        assertEquals("28122026", colsYearEnd.get(115)); // col 116 (1-based)
        assertStrictDatevValid(csv);
    }

    // -------------------------------------------------------------------------
    // CRITICAL-5 & MEDIUM-6: Multi-category 0% invoice split + EU expense col 40
    // -------------------------------------------------------------------------
    @Test
    public void multiCategoryZeroInvoiceSplitsCorrectlyAndEuExpensesEmitCol40() throws Exception {
        DatevConfig cfg = skr03Config();
        Account acc8100 = account(8100, "Steuerfreie Umsätze", Account.INCOME, 0.0, "de_skr03");
        Account acc3425 = account(3425, "Innergem. Erwerb 19% VSt/19% USt", Account.EXPENSE, 0.0, "de_skr03");
        Account acc3123 = account(3123, "Sonstige Leistungen EU §13b UStG", Account.EXPENSE, 0.0, "de_skr03");

        // Multi-category 0% EU invoice: 600 EUR goods (K -> 8125) + 400 EUR service (AE -> 8336)
        Entry mixedEu = zeroInvoice("RE-EU-MIX", "Client France SAS", "1000.00");
        Entry euAcq = paidEntry(Constants.TYPE_EXPENSE, JournalCalc.EXPENSE_PAID, "2026-03-18", "ER-EU-ACQ", "Lieferant AT GmbH", "500.00", "0.00", "0");
        Entry euSrv = paidEntry(Constants.TYPE_EXPENSE, JournalCalc.EXPENSE_PAID, "2026-03-19", "ER-EU-SRV", "Cloud IE Ltd", "300.00", "0.00", "0");
        List<Entry> all = Arrays.asList(mixedEu, euAcq, euSrv);
        JournalCalc.compute(all, Collections.emptySet());

        Map<Entry, Account> am = new HashMap<Entry, Account>();
        am.put(mixedEu, acc8100);
        am.put(euAcq, acc3425);
        am.put(euSrv, acc3123);

        DatevMapper.ZeroCategoryLookup zeroCats = e -> {
            if (e != mixedEu) return Collections.emptyMap();
            Map<VatCategory, BigDecimal> m = new java.util.LinkedHashMap<VatCategory, BigDecimal>();
            m.put(VatCategory.K, bd("600.00"));
            m.put(VatCategory.AE, bd("400.00"));
            return m;
        };
        DatevMapper.ContactVatLookup vats = e -> {
            if (e == mixedEu) return "FR12345678901";
            if (e == euAcq) return "ATU12345678";
            if (e == euSrv) return "IE9876543A";
            return "";
        };

        DatevMapper.Result res = DatevMapper.map(all, cfg, date("2026-03-01"), date("2026-03-31"),
                lookup(am, acc8100, acc3425, acc3123), vats, null, null, zeroCats, null, null);
        assertFalse(res.hasErrors(), res.issues.toString());
        assertEquals(4, res.lines.size(), "Mixed 0% invoice produces 2 lines + 2 EU expense lines");

        BookingLine lGoods = res.lines.get(0);
        BookingLine lSvc = res.lines.get(1);
        assertEquals("8125", lGoods.gegenkonto);
        assertEquals(bd("600.00"), lGoods.umsatz);
        assertEquals("FR12345678901", lGoods.euLandUstId);
        assertEquals("8336", lSvc.gegenkonto);
        assertEquals(bd("400.00"), lSvc.umsatz);
        assertEquals("FR12345678901", lSvc.euLandUstId);
        assertEquals(bd("1000.00"), lGoods.umsatz.add(lSvc.umsatz));

        BookingLine lAcq = res.lines.get(2);
        assertEquals("3425", lAcq.gegenkonto);
        assertEquals("", lAcq.buSchluessel, "3425 is an Automatikkonto");
        assertEquals("ATU12345678", lAcq.euLandUstId, "EU acquisition expense 3425 must emit supplier EU VAT ID in col 40");

        BookingLine lEuSrv = res.lines.get(3);
        assertEquals("3123", lEuSrv.gegenkonto);
        assertEquals("", lEuSrv.buSchluessel, "3123 is an Automatikkonto");
        assertEquals("IE9876543A", lEuSrv.euLandUstId, "EU §13b service expense 3123 must emit supplier EU VAT ID in col 40");

        String csv = DatevFormat.render(cfg, date("2026-03-01"), date("2026-03-31"), date("2026-04-01"), res.lines);
        assertStrictDatevValid(csv);
    }

    // -------------------------------------------------------------------------
    // HIGH-1 & HIGH-2: Beleglink BEDI, Beleginfo, Buchungs GUID, ExportLog, blockOnErrors
    // -------------------------------------------------------------------------
    @Test
    public void gobdBeleglinkBeleginfoGuidExportLogAndErrorBlockingWorkEndToEnd() throws Exception {
        DatevConfig cfg = skr03Config();
        Account acc8400 = account(8400, "Erlöse 19% USt", Account.INCOME, 19.0, "de_skr03");
        Entry eUuid = paidEntry(Constants.TYPE_INVOICE, Item.STATUS_PAID, "2026-03-10", "RE-UUID", "Kunde A", "100.00", "19.00", "19");
        Entry eFile = paidEntry(Constants.TYPE_INVOICE, Item.STATUS_PAID, "2026-03-11", "RE-FILE", "Kunde B", "200.00", "38.00", "19");
        List<Entry> entries = Arrays.asList(eUuid, eFile);
        JournalCalc.compute(entries, Collections.emptySet());
        Map<Entry, Account> am = new HashMap<Entry, Account>();
        am.put(eUuid, acc8400);
        am.put(eFile, acc8400);

        String sampleUuid = "550e8400-e29b-41d4-a716-446655440000";
        DatevMapper.ReceiptLinkLookup receipts = e -> e == eUuid ? sampleUuid : "RE-FILE_2026.pdf";

        DatevMapper.Result res = DatevMapper.map(entries, cfg, date("2026-03-01"), date("2026-03-31"),
                lookup(am, acc8400), null, receipts, null);
        assertFalse(res.hasErrors(), res.issues.toString());
        assertEquals(2, res.lines.size());

        // Deterministic Buchungs GUID (36-char UUID) on every line
        DatevMapper.Result res2 = DatevMapper.map(entries, cfg, date("2026-03-01"), date("2026-03-31"),
                lookup(am, acc8400), null, receipts, null);
        assertEquals(36, res.lines.get(0).buchungsGuid.length());
        assertEquals(res.lines.get(0).buchungsGuid, res2.lines.get(0).buchungsGuid, "Buchungs GUID must be deterministic");

        String csv = DatevFormat.render(cfg, date("2026-03-01"), date("2026-03-31"), date("2026-04-01"), res.lines);
        String[] rows = csv.split("\r\n");
        List<String> colsUuid = splitCsv(rows[2]);
        List<String> colsFile = splitCsv(rows[3]);

        // Col 20 (index 19) = BEDI "<UUID>" (uppercase hex per DATEV Belegbilderservice)
        assertEquals("BEDI \"" + sampleUuid.toUpperCase(java.util.Locale.ROOT) + "\"", colsUuid.get(19));
        // When filename given, Col 20 empty & Beleginfo Art 1 / Inhalt 1 (cols 21-22, indices 20-21) populated
        assertEquals("", colsFile.get(19));
        assertEquals("Belegdatei", colsFile.get(20));
        assertEquals("RE-FILE_2026.pdf", colsFile.get(21));
        // Col 103 (index 102) = Buchungs GUID
        assertEquals(res.lines.get(0).buchungsGuid, colsUuid.get(102));
        assertStrictDatevValid(csv);

        // DatevExportLog: duplicate detection + SHA-256 batch record
        DatevExportLog.clear();
        try {
            assertTrue(DatevExportLog.findDuplicates(res.lines).isEmpty());
            byte[] encoded = DatevFormat.encode(csv);
            DatevExportLog.recordExport("EXTF_Buchungsstapel_20260301_20260331.csv",
                    date("2026-03-01"), date("2026-03-31"), res.lines, encoded);
            assertEquals(2, DatevExportLog.findDuplicates(res.lines).size(), "Re-export must detect both duplicate lines");
            Map<String, String> batches = DatevExportLog.batches();
            assertEquals(1, batches.size());
            assertTrue(batches.values().iterator().next().contains(DatevExportLog.sha256(encoded)));
        } finally {
            DatevExportLog.clear();
        }

        // DatevExportContent sets blockOnErrors = true when result has errors
        DatevConfig badCfg = new DatevConfig();
        DatevMapper.Result badRes = DatevMapper.map(entries, badCfg, date("2026-03-01"), date("2026-03-31"),
                lookup(am, acc8400), null, null, null);
        DatevExportDialog.Content content = DatevExportContent.batch(badCfg, date("2026-03-01"), date("2026-03-31"),
                entries, entries, badRes, "EXTF_test.csv");
        assertTrue(content.blockOnErrors, "Export dialog must block saving when ERROR issues exist");
        assertFalse(content.errors.isEmpty());
    }

    // -------------------------------------------------------------------------
    // HIGH-3: DATEV EXTF 700 Category 16 Debitoren/Kreditoren v5 Master Data
    // -------------------------------------------------------------------------
    @Test
    public void masterDataCategory16V5RendersAdressattypEuVatAndBank1WithEmptyLeerfeld46() throws Exception {
        DatevConfig cfg = skr03Config();
        cfg.sachkontenLaenge = 4;

        mpv5.db.objects.Contact company = new mpv5.db.objects.Contact();
        company.setIDS(42);
        company.setisCompany(true);
        company.setisCustomer(true);
        company.setCompany("Müller & Söhne GmbH");
        company.settaxnumber("EL 123456789");
        company.setStreet("Hauptstraße 10");
        company.setZip("10115");
        company.setCity("Berlin");
        company.setCountry("Deutschland");
        company.setMailaddress("info@mueller.de");
        company.setBankname("Deutsche Bank");
        company.setBankaccount("DE89 3704 0044 0532 0130 00");
        company.setBankid("COBADEFFXXX");

        mpv5.db.objects.Contact personSupplier = new mpv5.db.objects.Contact();
        personSupplier.setIDS(7);
        personSupplier.setisCompany(false);
        personSupplier.setisCustomer(false);
        personSupplier.setisSupplier(true);
        personSupplier.setCname("Schröder");
        personSupplier.setPrename("Anna");
        personSupplier.setisMale(false);
        personSupplier.setStreet("Parkweg 3");
        personSupplier.setZip("80331");
        personSupplier.setCity("München");
        personSupplier.setCountry("DE");

        List<DatevMasterDataFormat.MasterRecord> recs = DatevMasterDataFormat.fromContacts(
                Arrays.asList(company, personSupplier), cfg);
        assertEquals(2, recs.size());

        String csv = DatevMasterDataFormat.render(cfg, date("2026-04-01"), recs);
        assertTrue(csv.endsWith("\r\n"));
        assertFalse(csv.replace("\r\n", "").contains("\n"));

        byte[] encoded = DatevMasterDataFormat.encode(csv);
        for (byte b : encoded) {
            assertFalse(b == (byte) 0x3F, "Master data CP1252 output must not contain '?' replacement bytes");
        }

        String[] rows = csv.split("\r\n");
        assertEquals(4, rows.length);
        List<String> hdr = splitCsv(rows[0]);
        assertEquals(31, hdr.size());
        assertEquals("EXTF", hdr.get(0));
        assertEquals("700", hdr.get(1));
        assertEquals("16", hdr.get(2));
        assertEquals("Debitoren/Kreditoren", hdr.get(3));
        assertEquals("5", hdr.get(4));
        assertEquals("4", hdr.get(13));
        assertEquals("03", hdr.get(26));

        List<String> colHeads = splitCsv(rows[1]);
        assertEquals(51, colHeads.size());
        assertEquals("IBAN-Nr. 1", colHeads.get(44));
        assertEquals("Leerfeld", colHeads.get(45));
        assertEquals("SWIFT-Code 1", colHeads.get(46));

        // Row 1: Company Debitor
        List<String> c1 = splitCsv(rows[2]);
        assertEquals(51, c1.size());
        assertEquals("10042", c1.get(0), "Debitor account must be SKL+1 digits (10000..69999)");
        assertEquals("Müller & Söhne GmbH", c1.get(1));
        assertEquals("", c1.get(3));
        assertEquals("", c1.get(4));
        assertEquals("2", c1.get(6), "Company must have Adressattyp = 2");
        assertEquals("EL", c1.get(8), "EU-Land must be split to 2-char prefix");
        assertEquals("123456789", c1.get(9), "EU-UStID must exclude 2-char prefix");
        assertEquals("STR", c1.get(14));
        assertEquals("Hauptstraße 10", c1.get(15));
        assertEquals("DE", c1.get(19));
        assertEquals("DE", c1.get(43));
        assertEquals("DE89370400440532013000", c1.get(44));
        assertEquals("", c1.get(45), "Column 46 (Leerfeld) MUST be strictly empty between IBAN and SWIFT!");
        assertEquals("COBADEFFXXX", c1.get(46));
        assertEquals("1", c1.get(48));

        // Row 2: Natural Person Kreditor
        List<String> c2 = splitCsv(rows[3]);
        assertEquals(51, c2.size());
        assertEquals("70007", c2.get(0), "Kreditor account must be SKL+1 digits (70000..99999)");
        assertEquals("", c2.get(1));
        assertEquals("Schröder", c2.get(3));
        assertEquals("Anna", c2.get(4));
        assertEquals("1", c2.get(6), "Natural person must have Adressattyp = 1");
        assertEquals("Frau", c2.get(10));
    }
}
