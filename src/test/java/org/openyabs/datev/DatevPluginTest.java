package org.openyabs.datev;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Account;
import mpv5.db.objects.Item;
import mpv5.globals.Constants;
import mpv5.handler.JournalCalc;
import mpv5.handler.JournalCalc.Entry;
import mpv5.handler.JournalCalc.Totals;
import mpv5.plugin.api.JournalExport;
import mpv5.plugin.api.JournalExportContext;
import mpv5.plugin.api.YabsExtension;
import mpv5.utils.export.JournalAdviserPackage;
import mpv5.utils.xml.XMLReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openyabs.datev.DatevFormat.BookingLine;

/**
 * Tests for the DATEV export plugin (EXTF 700, category 21, format version 13)
 * and the updated SKR03 / SKR04 charts of accounts.
 */
public class DatevPluginTest {

    private static Date date(String ymd) throws Exception {
        return new SimpleDateFormat("yyyy-MM-dd").parse(ymd);
    }

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    private static Account account(int num, String name, int type, double tax) {
        Account a = new Account();
        a.setIntaccountclass(num);
        a.setCname(name);
        a.setIntaccounttype(type);
        a.setTaxvalue(tax);
        a.setFrame("de_skr03");
        return a;
    }

    private static DatevConfig sampleConfig() {
        DatevConfig c = new DatevConfig();
        c.beraterNummer = 29098;
        c.mandantenNummer = 55003;
        c.wjBeginn = "20260101";
        c.sachkontenLaenge = 4;
        c.kontenrahmen = "03";
        c.bezeichnung = "Buchungsstapel 01/2026";
        c.diktatKuerzel = "YB";
        c.festschreibung = false;
        c.waehrung = "EUR";
        c.moneyAccountIncome = "1200";
        c.moneyAccountExpense = "1200";
        c.cashAccount = "1000";
        c.euDeliveryAccount = "8125";
        c.useAutomaticAccounts = true;
        c.revenueAccounts.put(bd("19"), "8400");
        c.revenueAccounts.put(bd("7"), "8300");
        c.revenueAccounts.put(bd("0"), "8120");
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
        }
        return new DatevMapper.AccountLookup() {
            @Override
            public Account accountOf(Entry entry) {
                return byEntry.get(entry);
            }

            @Override
            public Account byNumber(String accountNumber) {
                return byNum.get(accountNumber);
            }
        };
    }

    @Test
    public void pluginIsDiscoveredByServiceLoader() {
        boolean found = false;
        for (YabsExtension ext : ServiceLoader.load(YabsExtension.class)) {
            if (DatevPlugin.ID.equals(ext.id())) {
                found = true;
                assertEquals("DATEV-Export", ext.name());
                assertEquals("2.0.0", ext.version());
                assertEquals("2.0.23", ext.minYabsVersion());
            }
        }
        assertTrue(found, "DatevPlugin must be registered in META-INF/services/mpv5.plugin.api.YabsExtension");
    }

    @Test
    public void headerLineMatchesDatevExtf700Spec() throws Exception {
        DatevConfig cfg = sampleConfig();
        Date from = date("2026-01-01");
        Date to = date("2026-01-31");
        Date created = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2026-02-01 14:04:40.439");

        String header = DatevFormat.headerLine(cfg, from, to, created);
        String[] f = header.split(";", -1);
        assertEquals(31, f.length, "EXTF 700 header must have exactly 31 fields");

        // Verified against developer.datev.de/de/file-format/details/datev-format/format-description/header
        assertEquals("\"EXTF\"", f[0]);             // 1  Kennzeichen
        assertEquals("700", f[1]);                  // 2  Versionsnummer
        assertEquals("21", f[2]);                   // 3  Formatkategorie (Buchungsstapel)
        assertEquals("\"Buchungsstapel\"", f[3]);   // 4  Formatname
        assertEquals("13", f[4]);                   // 5  Formatversion
        assertEquals("20260201140440439", f[5]);    // 6  Erzeugt am (YYYYMMDDHHMMSSFFF)
        assertEquals("", f[6]);                     // 7  Importiert (Leerfeld)
        assertEquals("\"RE\"", f[7]);               // 8  Herkunft
        assertEquals("\"\"", f[8]);                 // 9  Exportiert von
        assertEquals("\"\"", f[9]);                 // 10 Importiert von
        assertEquals("29098", f[10]);               // 11 Beraternummer
        assertEquals("55003", f[11]);               // 12 Mandantennummer
        assertEquals("20260101", f[12]);            // 13 WJ-Beginn
        assertEquals("4", f[13]);                   // 14 Sachkontenlänge
        assertEquals("20260101", f[14]);            // 15 Datum von
        assertEquals("20260131", f[15]);            // 16 Datum bis
        assertEquals("\"Buchungsstapel 01/2026\"", f[16]); // 17 Bezeichnung
        assertEquals("\"YB\"", f[17]);              // 18 Diktatkürzel
        assertEquals("1", f[18]);                   // 19 Buchungstyp (Finanzbuchführung)
        assertEquals("0", f[19]);                   // 20 Rechnungslegungszweck
        assertEquals("0", f[20]);                   // 21 Festschreibung (default off)
        assertEquals("\"EUR\"", f[21]);             // 22 WKZ
        assertEquals("", f[22]);                    // 23 Reserviert
        assertEquals("\"\"", f[23]);                // 24 Derivatskennzeichen
        assertEquals("", f[24]);                    // 25 Reserviert
        assertEquals("", f[25]);                    // 26 Reserviert
        assertEquals("\"03\"", f[26]);              // 27 Sachkontenrahmen
        assertEquals("", f[27]);                    // 28 ID der Branchenlösung
        assertEquals("", f[28]);                    // 29 Reserviert
        assertEquals("\"\"", f[29]);                // 30 Reserviert
        assertEquals("\"\"", f[30]);                // 31 Anwendungsinformation

        assertEquals(125, DatevFormat.COLUMNS.length, "Buchungsstapel v13 must have 125 columns");
        assertEquals("Umsatz (ohne Soll/Haben-Kz)", DatevFormat.COLUMNS[0]);
        assertEquals("Gegenkonto (ohne BU-Schlüssel)", DatevFormat.COLUMNS[7]);
        assertEquals("EU-Land u. UStID (Bestimmung)", DatevFormat.COLUMNS[39]);
        assertEquals("Festschreibung", DatevFormat.COLUMNS[113]);
        assertEquals("Abw. Skontokonto", DatevFormat.COLUMNS[124]);
        assertEquals("EXTF_Buchungsstapel_20260101_20260131.csv", DatevFormat.fileName(from, to));
    }

    @Test
    public void encodingCrlfQuotingAndTextLimits() throws Exception {
        DatevConfig cfg = sampleConfig();
        BookingLine line = new BookingLine();
        line.umsatz = bd("119.50");
        line.sollHaben = "S";
        line.konto = "1200";
        line.gegenkonto = "8400";
        line.belegdatum = "1501";
        line.belegfeld1 = "RE-2026/001#invalid_chars_123456789012345678901234567890";
        line.buchungstext = "Müller \"GmbH\" & Co.\r\nBürobedarf € 1234567890123456789012345678901234567890";

        String csv = DatevFormat.render(cfg, date("2026-01-01"), date("2026-01-31"), date("2026-02-01"), Collections.singletonList(line));
        // Every line ends with CRLF
        assertTrue(csv.endsWith("\r\n"));
        String[] rows = csv.split("\r\n");
        assertEquals(3, rows.length);
        // No bare LF
        assertFalse(csv.replace("\r\n", "").contains("\n"));
        assertFalse(csv.replace("\r\n", "").contains("\r"));

        // Windows-1252 round-trip preserves umlauts and €
        byte[] bytes = DatevFormat.encode(csv);
        String decoded = new String(bytes, DatevFormat.CHARSET);
        assertTrue(decoded.contains("Müller"));
        assertTrue(decoded.contains("Bürobedarf"));
        assertTrue(decoded.contains("€"));

        // Check booking line fields
        String[] cols = rows[2].split(";", -1);
        assertEquals(125, cols.length);
        assertEquals("119,50", cols[0]);
        assertEquals("\"S\"", cols[1]);
        assertEquals("\"EUR\"", cols[2]);
        assertEquals("1200", cols[6]);
        assertEquals("8400", cols[7]);
        assertEquals("1501", cols[9]);

        // Belegfeld 1: max 36 chars, only allowed characters (no '#' or '_')
        String bf1 = cols[10].substring(1, cols[10].length() - 1);
        assertEquals(36, bf1.length());
        assertFalse(bf1.contains("#"));
        assertFalse(bf1.contains("_"));

        // Buchungstext: quotes doubled, control chars stripped, max 60 chars before quoting
        String bt = cols[13];
        assertTrue(bt.startsWith("\"") && bt.endsWith("\""));
        assertTrue(bt.contains("\"\"GmbH\"\""));
        String btUnescaped = bt.substring(1, bt.length() - 1).replace("\"\"", "\"");
        assertTrue(btUnescaped.length() <= 60);
    }

    private static Entry zeroRateInvoice(String number) throws Exception {
        Entry e = new Entry();
        e.type = Constants.TYPE_INVOICE;
        e.status = Item.STATUS_PAID;
        e.date = date("2026-01-22");
        e.number = number;
        e.contact = "Kunde";
        e.net = bd("500.00");
        e.tax = bd("0.00");
        e.gross = bd("500.00");
        e.rates.put(BigDecimal.ZERO, new BigDecimal[]{bd("500.00"), bd("0.00")});
        return e;
    }

    /**
     * 0 % sales by the VAT category of their tax rate (#214): EU services on
     * 8336, domestic § 13b on 8337, intra-community supplies on 8125, exports
     * on 8120; without a category an EU 0 % sale is a supply of goods, with a
     * warning
     */
    @Test
    public void zeroRateSalesByVatCategory() throws Exception {
        DatevConfig cfg = sampleConfig();
        cfg.euServiceAccount = "8336";
        cfg.reverseChargeAccount = "8337";
        cfg.exportAccount = "8120";
        Account acc8400 = account(8400, "Erlöse 19% USt", Account.INCOME, 19.0);
        Entry service = zeroRateInvoice("RE-EU-SERVICE");
        Entry domestic = zeroRateInvoice("RE-13B");
        Entry goods = zeroRateInvoice("RE-EU-GOODS");
        Entry export = zeroRateInvoice("RE-EXPORT");
        Entry unknown = zeroRateInvoice("RE-EU-UNKNOWN");
        List<Entry> all = Arrays.asList(service, domestic, goods, export, unknown);
        JournalCalc.compute(all, Collections.<Integer>emptySet());
        Map<Entry, Account> accMap = new HashMap<Entry, Account>();
        for (Entry e : all) {
            accMap.put(e, acc8400);//the document account (19 %) does not fit 0 %
        }
        Map<Entry, mpv5.utils.export.VatCategory> cat = new HashMap<Entry, mpv5.utils.export.VatCategory>();
        cat.put(service, mpv5.utils.export.VatCategory.AE);
        cat.put(domestic, mpv5.utils.export.VatCategory.AE);
        cat.put(goods, mpv5.utils.export.VatCategory.K);
        cat.put(export, mpv5.utils.export.VatCategory.G);
        DatevMapper.ContactVatLookup vat = e -> e == domestic || e == export ? "" : "FR40303265045";
        DatevMapper.Result res = DatevMapper.map(all, cfg, date("2026-01-01"), date("2026-01-31"),
                lookup(accMap, acc8400), vat, null, (e, r) -> cat.get(e));
        Map<String, BookingLine> byNumber = new HashMap<String, BookingLine>();
        for (BookingLine l : res.lines) {
            byNumber.put(l.belegfeld1, l);
        }
        assertEquals("8336", byNumber.get("RE-EU-SERVICE").gegenkonto);
        assertEquals("FR40303265045", byNumber.get("RE-EU-SERVICE").euLandUstId, "EU services: VAT ID for the ZM");
        assertEquals("8337", byNumber.get("RE-13B").gegenkonto);
        assertEquals("", byNumber.get("RE-13B").euLandUstId);
        assertEquals("8125", byNumber.get("RE-EU-GOODS").gegenkonto);
        assertEquals("FR40303265045", byNumber.get("RE-EU-GOODS").euLandUstId);
        assertEquals("8120", byNumber.get("RE-EXPORT").gegenkonto);
        assertEquals("8125", byNumber.get("RE-EU-UNKNOWN").gegenkonto, "no category: as before");
        assertTrue(res.issues.stream().anyMatch(i -> i.message.contains("RE-EU-UNKNOWN") && i.message.contains("Kategorie AE")),
                res.issues.toString());
        assertTrue(res.issues.stream().noneMatch(i -> i.message.contains("RE-EU-SERVICE") && i.message.contains("ohne USt-Kategorie")));

        //SKR04 defaults
        DatevConfig skr04 = new DatevConfig();
        skr04.kontenrahmen = "04";
        skr04.euServiceAccount = "";
        skr04.reverseChargeAccount = "";
        skr04.exportAccount = "";
        skr04.prefillFromAccounts(Collections.<Account>emptyList(), Collections.<BigDecimal>emptyList(), false);
        assertEquals("4336", skr04.euServiceAccount);
        assertEquals("4337", skr04.reverseChargeAccount);
        assertEquals("4120", skr04.exportAccount);
    }

    @Test
    public void mapsInvoiceMixedDiscountCreditExpensePartialPaymentAndEuDelivery() throws Exception {
        DatevConfig cfg = sampleConfig();
        Account acc8400 = account(8400, "Erlöse 19% USt", Account.INCOME, 19.0);
        Account acc8300 = account(8300, "Erlöse 7% USt", Account.INCOME, 7.0);
        Account acc8125 = account(8125, "Steuerfreie innergem. Lieferungen", Account.INCOME, 0.0);
        Account acc3300 = account(3300, "Wareneingang 7% Vorsteuer", Account.EXPENSE, 7.0);
        Account acc4930 = account(4930, "Bürobedarf", Account.EXPENSE, 19.0);

        // 1. Standard 19% invoice (paid)
        Entry inv19 = new Entry();
        inv19.type = Constants.TYPE_INVOICE;
        inv19.status = Item.STATUS_PAID;
        inv19.date = date("2026-01-10");
        inv19.number = "RE-2026-001";
        inv19.contact = "Kunde Eins GmbH";
        inv19.net = bd("100.00");
        inv19.tax = bd("19.00");
        inv19.gross = bd("119.00");
        inv19.rates.put(bd("19"), new BigDecimal[]{bd("100.00"), bd("19.00")});

        // 2. Mixed 19%/7% invoice with 10% document discount (split via JournalCalc.split)
        Entry mixed = new Entry();
        mixed.type = Constants.TYPE_INVOICE;
        mixed.status = Item.STATUS_PAID;
        mixed.date = date("2026-01-12");
        mixed.number = "RE-2026-002";
        mixed.contact = "Mischkunde AG";
        mixed.net = bd("180.00");
        mixed.tax = bd("23.40");
        mixed.gross = bd("203.40");
        Map<BigDecimal, BigDecimal[]> posLines = new TreeMap<BigDecimal, BigDecimal[]>(JournalCalc.RATE_ORDER);
        posLines.put(bd("19"), new BigDecimal[]{bd("100.00"), bd("19.00")});
        posLines.put(bd("7"), new BigDecimal[]{bd("100.00"), bd("7.00")});
        mixed.rates = JournalCalc.split(mixed.net, mixed.tax, posLines);

        // 3. Credit note 19% (paid)
        Entry credit = new Entry();
        credit.type = Constants.TYPE_CREDIT;
        credit.status = Item.STATUS_PAID;
        credit.date = date("2026-01-15");
        credit.number = "GS-2026-001";
        credit.contact = "Kunde Eins GmbH";
        credit.net = bd("50.00");
        credit.tax = bd("9.50");
        credit.gross = bd("59.50");
        credit.rates.put(bd("19"), new BigDecimal[]{bd("50.00"), bd("9.50")});

        // 4. Expense 7% (paid)
        Entry exp7 = new Entry();
        exp7.type = Constants.TYPE_EXPENSE;
        exp7.status = JournalCalc.EXPENSE_PAID;
        exp7.date = date("2026-01-18");
        exp7.number = "AUS-2026-01";
        exp7.description = "Fachbuch";
        exp7.net = bd("20.00");
        exp7.tax = bd("1.40");
        exp7.gross = bd("21.40");
        exp7.rates.put(bd("7"), new BigDecimal[]{bd("20.00"), bd("1.40")});

        // 5. Partial payment on a mixed invoice (50% paid)
        Entry payment = new Entry();
        payment.type = Constants.TYPE_PAYMENT;
        payment.status = Item.STATUS_PAID;
        payment.date = date("2026-01-20");
        payment.number = "RE-2026-003";
        payment.contact = "Teilzahler KG";
        payment.net = bd("90.00");
        payment.tax = bd("11.70");
        payment.gross = bd("101.70");
        payment.rates = JournalCalc.split(payment.net, payment.tax, posLines);

        // 6. 0% EU delivery (8125) with French VAT ID
        Entry eu = new Entry();
        eu.type = Constants.TYPE_INVOICE;
        eu.status = Item.STATUS_PAID;
        eu.date = date("2026-01-22");
        eu.number = "RE-2026-EU1";
        eu.contact = "Client France SAS";
        eu.net = bd("500.00");
        eu.tax = bd("0.00");
        eu.gross = bd("500.00");
        eu.rates.put(BigDecimal.ZERO, new BigDecimal[]{bd("500.00"), bd("0.00")});

        // 7. Cancelled invoice & unpaid expense (must NOT appear in DATEV output)
        Entry cancelled = new Entry();
        cancelled.type = Constants.TYPE_INVOICE;
        cancelled.status = Item.STATUS_CANCELLED;
        cancelled.date = date("2026-01-25");
        cancelled.number = "RE-STORNO";
        cancelled.net = bd("999.00");
        cancelled.tax = bd("189.81");
        cancelled.gross = bd("1188.81");
        cancelled.rates.put(bd("19"), new BigDecimal[]{bd("999.00"), bd("189.81")});

        Entry unpaidExp = new Entry();
        unpaidExp.type = Constants.TYPE_EXPENSE;
        unpaidExp.status = JournalCalc.EXPENSE_UNPAID;
        unpaidExp.date = date("2026-01-26");
        unpaidExp.number = "AUS-OFFEN";
        unpaidExp.net = bd("50.00");
        unpaidExp.tax = bd("9.50");
        unpaidExp.gross = bd("59.50");
        unpaidExp.rates.put(bd("19"), new BigDecimal[]{bd("50.00"), bd("9.50")});

        List<Entry> all = Arrays.asList(inv19, mixed, credit, exp7, payment, eu, cancelled, unpaidExp);
        JournalCalc.compute(all, Collections.<Integer>emptySet());

        Map<Entry, Account> accMap = new HashMap<Entry, Account>();
        accMap.put(inv19, acc8400);
        accMap.put(mixed, acc8400); // 19% matches doc account; 7% must fall back to 8300
        accMap.put(credit, acc8400);
        accMap.put(exp7, acc3300);
        accMap.put(payment, acc8400);
        accMap.put(eu, acc8125);

        DatevMapper.AccountLookup accLookup = lookup(accMap, acc8400, acc8300, acc8125, acc3300, acc4930);
        DatevMapper.ContactVatLookup vatLookup = e -> e == eu ? "FR 12 345678901" : "";

        DatevMapper.Result res = DatevMapper.map(all, cfg, date("2026-01-01"), date("2026-01-31"), accLookup, vatLookup, null);

        // 1 (inv19) + 2 (mixed) + 1 (credit) + 1 (exp7) + 2 (payment) + 1 (eu) = 8 lines
        assertEquals(8, res.lines.size(), "Cancelled and unpaid entries must be excluded");

        // Verify inv19 (automatic account -> empty BU key)
        BookingLine l0 = res.lines.get(0);
        assertEquals(bd("119.00"), l0.umsatz);
        assertEquals("S", l0.sollHaben);
        assertEquals("1200", l0.konto);
        assertEquals("8400", l0.gegenkonto);
        assertEquals("", l0.buSchluessel);
        assertEquals("1001", l0.belegdatum);
        assertEquals("RE-2026-001", l0.belegfeld1);

        // Verify mixed invoice: 7% falls back to 8300, 19% uses 8400, sum == stored gross 203.40
        BookingLine m7 = res.lines.get(1);
        BookingLine m19 = res.lines.get(2);
        assertEquals("8300", m7.gegenkonto);
        assertEquals("8400", m19.gegenkonto);
        assertEquals(bd("203.40"), m7.umsatz.add(m19.umsatz));

        // Verify credit note: reversed S/H -> "H"
        BookingLine lc = res.lines.get(3);
        assertEquals(bd("59.50"), lc.umsatz);
        assertEquals("H", lc.sollHaben);
        assertEquals("1200", lc.konto);
        assertEquals("8400", lc.gegenkonto);

        // Verify expense 7%
        BookingLine le = res.lines.get(4);
        assertEquals(bd("21.40"), le.umsatz);
        assertEquals("H", le.sollHaben);
        assertEquals("1200", le.konto);
        assertEquals("3300", le.gegenkonto);
        assertEquals("Fachbuch", le.buchungstext);

        // Verify partial payment: sum of split lines == payment gross 101.70
        BookingLine p7 = res.lines.get(5);
        BookingLine p19 = res.lines.get(6);
        assertEquals(bd("101.70"), p7.umsatz.add(p19.umsatz));
        assertEquals("S", p7.sollHaben);
        assertEquals("S", p19.sollHaben);

        // Verify 0% EU delivery: account 8125 and sanitized VAT ID
        BookingLine leu = res.lines.get(7);
        assertEquals(bd("500.00"), leu.umsatz);
        assertEquals("8125", leu.gegenkonto);
        assertEquals("FR12345678901", leu.euLandUstId);
        assertEquals("", leu.buSchluessel);

        // Total gross sum across all emitted lines equals sum of counted documents' gross amounts
        BigDecimal expectedGross = inv19.gross.add(mixed.gross).add(credit.gross).add(exp7.gross).add(payment.gross).add(eu.gross);
        assertEquals(expectedGross, res.totalGross());

        // When automatic accounts are disabled, BU keys must be emitted (3 for 19% income, 8 for 7% expense)
        cfg.useAutomaticAccounts = false;
        DatevMapper.Result nonAuto = DatevMapper.map(all, cfg, date("2026-01-01"), date("2026-01-31"), accLookup, vatLookup, null);
        assertEquals("3", nonAuto.lines.get(0).buSchluessel);
        assertEquals("2", nonAuto.lines.get(1).buSchluessel);
        assertEquals("3", nonAuto.lines.get(3).buSchluessel);
        assertEquals("8", nonAuto.lines.get(4).buSchluessel);
        assertEquals("", nonAuto.lines.get(7).buSchluessel);
    }

    @Test
    public void cashSaleUsesCashAccountAndValidationReportsProblems() throws Exception {
        DatevConfig cfg = new DatevConfig(); // missing Berater & Mandant
        cfg.moneyAccountIncome = "1200";
        cfg.moneyAccountExpense = "1200";
        cfg.cashAccount = "1000";
        cfg.revenueAccounts.put(bd("19"), "8400");

        Entry pos = new Entry();
        pos.type = Constants.TYPE_INVOICE;
        pos.status = Item.STATUS_PAID;
        pos.date = date("2026-03-05");
        pos.number = "BON-2026-0001";
        pos.contact = "Barverkauf";
        pos.description = "POS · Kassierer";
        pos.net = bd("10.00");
        pos.tax = bd("1.90");
        pos.gross = bd("11.90");
        pos.rates.put(bd("19"), new BigDecimal[]{bd("10.00"), bd("1.90")});
        JournalCalc.compute(Collections.singletonList(pos), Collections.<Integer>emptySet());

        DatevMapper.Result res = DatevMapper.map(Collections.singletonList(pos), cfg,
                date("2025-12-01"), date("2026-03-31"), null, null, null);
        assertEquals(1, res.lines.size());
        assertEquals("1000", res.lines.get(0).konto, "POS/Barverkauf should book to cash account");
        assertEquals("8400", res.lines.get(0).gegenkonto, "Missing document account should fall back to rate setting");
        assertTrue(res.hasErrors(), "Missing Berater/Mandant must produce validation errors");
        assertTrue(res.issues.stream().anyMatch(i -> i.message.contains("Beraternummer")));
        assertTrue(res.issues.stream().anyMatch(i -> i.message.contains("Mandantennummer")));
        assertTrue(res.issues.stream().anyMatch(i -> i.message.contains("mehrere Jahre")));
    }

    @Test
    public void skr03AndSkr04AccountXmlFilesParseCleanlyOffline() throws Exception {
        File skr03 = new File("ext/accounts-german-skr03.xml");
        File skr04 = new File("ext/accounts-german-skr04.xml");
        assertTrue(skr03.isFile(), "ext/accounts-german-skr03.xml must exist");
        assertTrue(skr04.isFile(), "ext/accounts-german-skr04.xml must exist");

        // Verify no UTF-8 mojibake or old Kennzahl 51 in SKR03
        String raw03 = java.nio.file.Files.readString(skr03.toPath(), StandardCharsets.UTF_8);
        assertFalse(raw03.contains("Â§"), "SKR03 XML must not contain UTF-8 mojibake");
        assertFalse(raw03.contains("<inttaxfid>51</inttaxfid>"), "SKR03 XML must use UStVA Kz 81 instead of obsolete 51");
        assertFalse(raw03.contains("16%/19%"), "SKR03 XML must not contain outdated 16%/19% labels");
        assertTrue(raw03.contains("<inttaxfid>81</inttaxfid>"));

        XMLReader r3 = new XMLReader();
        r3.newDoc(skr03, false);
        ArrayList<ArrayList<DatabaseObject>> objs3 = r3.getObjects();
        List<Account> accs3 = flattenAccounts(objs3);
        assertTrue(accs3.size() > 300, "SKR03 should contain >300 accounts, got " + accs3.size());
        assertEquals("03", DatevConfig.detectKontenrahmen(accs3));
        assertTrue(accs3.stream().anyMatch(a -> a.__getIntaccountclass() == 8400 && a.__getInttaxfid() == 81 && a.__getTaxvalue() == 19.0));
        assertTrue(accs3.stream().anyMatch(a -> a.__getIntaccountclass() == 1200));

        XMLReader r4 = new XMLReader();
        r4.newDoc(skr04, false);
        ArrayList<ArrayList<DatabaseObject>> objs4 = r4.getObjects();
        List<Account> accs4 = flattenAccounts(objs4);
        assertTrue(accs4.size() >= 100, "SKR04 should contain >=100 accounts, got " + accs4.size());
        assertEquals("04", DatevConfig.detectKontenrahmen(accs4));
        assertTrue(accs4.stream().anyMatch(a -> a.__getIntaccountclass() == 4400 && a.__getInttaxfid() == 81 && a.__getTaxvalue() == 19.0));
        assertTrue(accs4.stream().anyMatch(a -> a.__getIntaccountclass() == 1800));
        assertTrue(accs4.stream().anyMatch(a -> a.__getIntaccountclass() == 4125));
    }

    @Test
    public void adviserZipIncludesDatevCsvWhenPluginExportIsActive(@TempDir Path dir) throws Exception {
        java.util.Locale prevLocale = java.util.Locale.getDefault();
        String prevLang = System.getProperty("user.language");
        java.util.Locale.setDefault(java.util.Locale.US);
        System.setProperty("user.language", "en");
        try {
            Date from = date("2026-01-01");
            Date to = date("2026-01-31");
            Entry inv = new Entry();
            inv.type = Constants.TYPE_INVOICE;
            inv.status = Item.STATUS_PAID;
            inv.date = date("2026-01-10");
            inv.number = "RE-1";
            inv.contact = "Testkunde";
            inv.net = bd("100.00");
            inv.tax = bd("19.00");
            inv.gross = bd("119.00");
            inv.rates.put(bd("19"), new BigDecimal[]{bd("100.00"), bd("19.00")});
            List<Entry> entries = Collections.singletonList(inv);
            Totals totals = JournalCalc.compute(entries, Collections.<Integer>emptySet());

            final DatevConfig cfg = sampleConfig();
            JournalExport datevExport = new JournalExport() {
                @Override
                public String title() {
                    return "DATEV-Buchungsstapel (CSV)";
                }

                @Override
                public void export(JournalExportContext context) {
                }

                @Override
                public Map<String, byte[]> adviserPackageEntries(JournalExportContext ctx) {
                    DatevMapper.Result res = DatevMapper.map(ctx.entries, cfg, ctx.from, ctx.to, null, null, null);
                    String csv = DatevFormat.render(cfg, ctx.from, ctx.to, ctx.to, res.lines);
                    return Collections.singletonMap("datev/" + DatevFormat.fileName(ctx.from, ctx.to), DatevFormat.encode(csv));
                }
            };

            File zipFile = dir.resolve("journal-package.zip").toFile();
            new JournalAdviserPackage(entries, totals, from, to, Collections.emptyList(), Collections.emptyMap())
                    .setPluginExports(Collections.singletonList(datevExport))
                    .write(zipFile);

            try (ZipFile zf = new ZipFile(zipFile)) {
                assertNotNull(zf.getEntry("journal.csv"));
                assertNotNull(zf.getEntry("journal_tax_rates.csv"));
                ZipEntry datevEntry = zf.getEntry("datev/EXTF_Buchungsstapel_20260101_20260131.csv");
                assertNotNull(datevEntry, "Tax-adviser ZIP must include datev/EXTF_Buchungsstapel_...csv");
                String content = new String(zf.getInputStream(datevEntry).readAllBytes(), DatevFormat.CHARSET);
                assertTrue(content.startsWith("\"EXTF\";700;21;\"Buchungsstapel\";13;"));
                assertTrue(content.contains("119,00;\"S\";\"EUR\";;;;1200;8400;"));

                ZipEntry summary = zf.getEntry("summary.txt");
                assertNotNull(summary, "Summary file must use international name summary.txt");
                String sumText = new String(zf.getInputStream(summary).readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(sumText.contains("datev/EXTF_Buchungsstapel_20260101_20260131.csv"));
                assertFalse(sumText.contains("Steuerberater"), "Default English summary must not contain hardcoded German");
                assertFalse(sumText.contains("Einnahmen-\u00dcberschuss-Rechnung"), "Default English summary must not contain hardcoded German");
            }
        } finally {
            java.util.Locale.setDefault(prevLocale);
            if (prevLang == null) {
                System.clearProperty("user.language");
            } else {
                System.setProperty("user.language", prevLang);
            }
        }
    }

    @Test
    public void prefillRespectsExplicitKontenrahmenAndAvoidsCrossChartLeakage() {
        Account skr03Bank = new Account();
        skr03Bank.setIntaccountclass(1200);
        skr03Bank.setFrame("SKR03");
        Account skr03Rev19 = new Account();
        skr03Rev19.setIntaccountclass(8400);
        skr03Rev19.setIntaccounttype(Account.INCOME);
        skr03Rev19.setTaxvalue(19.0);
        skr03Rev19.setFrame("SKR03");
        List<Account> skr03DbAccounts = Arrays.asList(skr03Bank, skr03Rev19);

        DatevConfig cfg = new DatevConfig();
        cfg.kontenrahmen = "04";
        cfg.cashAccount = "1000";
        cfg.prefillFromAccounts(skr03DbAccounts, Arrays.asList(bd("19"), bd("7"), bd("0")), true);

        assertEquals("04", cfg.kontenrahmen, "Explicit SKR04 choice must not be reverted to DB SKR03");
        assertEquals("1800", cfg.moneyAccountIncome);
        assertEquals("1800", cfg.moneyAccountExpense);
        assertEquals("1600", cfg.cashAccount, "Existing SKR03 cash account must switch to SKR04 1600");
        assertEquals("4400", cfg.revenueAccounts.get(bd("19")));
        assertEquals("5400", cfg.expenseAccounts.get(bd("19")));
        assertEquals("4125", cfg.euDeliveryAccount);
        assertTrue(cfg.chartConsistencyWarnings().isEmpty(), "Clean SKR04 config must have no consistency warnings");
        assertFalse(cfg.hasCustomAccountOverrides(Arrays.asList(bd("19"), bd("7"), bd("0"))),
                "Standard SKR04 accounts must be recognized as DATEV standard");
        cfg.moneyAccountIncome = "1810";
        assertTrue(cfg.hasCustomAccountOverrides(Arrays.asList(bd("19"), bd("7"), bd("0"))),
                "Custom bank account 1810 must be detected as custom override");
        assertNotNull(cfg.detectChartMismatchWithAccounts(skr03DbAccounts),
                "Should inform user when YaBS DB accounts are SKR03 while config is SKR04");
    }

    @Test
    public void mixedSkr03AndSkr04AccountsTriggerWarningsInConfigAndMapper() throws Exception {
        DatevConfig mixed = sampleConfig();
        mixed.kontenrahmen = "03";
        mixed.revenueAccounts.put(bd("19"), "4400"); // SKR04 revenue mixed into SKR03 config

        List<String> warnings = mixed.chartConsistencyWarnings(false);
        assertFalse(warnings.isEmpty(), "Mixed SKR03/SKR04 accounts must emit warning");
        assertTrue(warnings.get(0).contains("gemischt"), "Warning should clearly state accounts are mixed: " + warnings.get(0));
        assertTrue(warnings.get(0).contains("4400"), "Warning should name offending SKR04 account");
        assertTrue(warnings.get(0).contains("1200"), "Warning should name SKR03 account");

        // Mapper must also surface the warning during Buchungsstapel validation
        DatevMapper.Result res = DatevMapper.map(Collections.emptyList(), mixed, date("2026-01-01"), date("2026-01-31"),
                null, null, null);
        assertTrue(res.issues.stream().anyMatch(i -> i.severity == DatevMapper.Issue.Severity.WARNING
                && i.message.contains("gemischt")),
                "DatevMapper must report mixed SKR03/SKR04 warning");

        // All SKR03 accounts while SKR04 is selected must also warn
        DatevConfig mismatch = sampleConfig();
        mismatch.kontenrahmen = "04";
        List<String> mismatchWarnings = mismatch.chartConsistencyWarnings(false);
        assertFalse(mismatchWarnings.isEmpty());
        assertTrue(mismatchWarnings.get(0).contains("SKR04") && mismatchWarnings.get(0).contains("SKR03"));

        // Clean SKR03 config has zero warnings
        assertTrue(sampleConfig().chartConsistencyWarnings(false).isEmpty());
    }

    @Test
    public void pluginRegistersJournalExportsAndCommandsCleanly() throws Exception {
        final List<String> commands = new ArrayList<String>();
        final List<String> settingsPages = new ArrayList<String>();
        final List<JournalExport> journalExports = new ArrayList<JournalExport>();
        mpv5.plugin.api.ExtensionContext stub = new mpv5.plugin.api.ExtensionContext() {
            @Override
            public void addCommand(String title, String description, Runnable action) {
                commands.add(title);
            }

            @Override
            public void addSettingsPage(String title, String description, java.util.function.Supplier<javax.swing.JComponent> page) {
                settingsPages.add(title);
            }

            @Override
            public void addJournalExport(JournalExport export) {
                journalExports.add(export);
            }

            @Override
            public void addSearchProvider(java.util.function.Function<String, List<mpv5.plugin.api.SearchResult>> provider) {
            }

            @Override
            public void onActivity(java.util.function.Consumer<mpv5.handler.Activity.Entry> listener) {
            }

            @Override
            public void addRecordModifier(mpv5.db.common.DatabaseObjectModifier modifier) {
            }

            @Override
            public void openTab(String title, javax.swing.JComponent content) {
            }

            @Override
            public void notify(String message) {
            }

            @Override
            public mpv5.plugin.api.PluginSettings settings() {
                return null;
            }

            @Override
            public File dataFolder() {
                return null;
            }

            @Override
            public <T> void background(java.util.function.Supplier<T> work, java.util.function.Consumer<T> then) {
            }
        };

        new DatevPlugin().start(stub);

        assertTrue(settingsPages.contains("DATEV-Export"));

        assertEquals(3, journalExports.size(), "DATEV plugin must register 3 Journal Export entries");
        for (JournalExport je : journalExports) {
            assertNotNull(je.title());
            assertFalse(je.title().trim().isEmpty());
            assertNotNull(je.tooltip());
            assertFalse(je.tooltip().trim().isEmpty(), "Export must have a helpful tooltip: " + je.title());
        }
        assertTrue(journalExports.get(0).title().startsWith("DATEV-Belegpaket")
                && journalExports.get(0).title().contains("ZIP")
                && journalExports.get(0).title().contains("Unternehmen online"),
                "Unexpected Belegpaket label: " + journalExports.get(0).title());
        assertTrue(journalExports.get(1).title().startsWith("DATEV-Buchungsstapel")
                && journalExports.get(1).title().contains("EXTF-CSV")
                && journalExports.get(1).title().contains("Kanzlei-Rechnungswesen"),
                "Unexpected Buchungsstapel label: " + journalExports.get(1).title());
        assertTrue(journalExports.get(2).title().startsWith("DATEV-Stammdaten")
                && journalExports.get(2).title().contains("Debitoren/Kreditoren")
                && journalExports.get(2).title().contains("EXTF-CSV"),
                "Unexpected Stammdaten label: " + journalExports.get(2).title());

        assertEquals("DATEV: Setup", commands.get(0),
                "DATEV plugin must register 'DATEV: Setup' as first command in Plugins menu: " + commands);
        assertTrue(commands.stream().anyMatch(c -> c.contains("Belegpaket")),
                "DATEV plugin must register Belegpaket command in Plugins menu: " + commands);
        assertTrue(commands.stream().anyMatch(c -> c.contains("Buchungsstapel")),
                "DATEV plugin must register Buchungsstapel command in Plugins menu: " + commands);
        assertTrue(commands.stream().anyMatch(c -> c.contains("Stammdaten")),
                "DATEV plugin must register Stammdaten command in Plugins menu: " + commands);
    }

    @Test
    public void masterDataContentValidatesHeaderAndBuildsPreview() {
        DatevMasterDataFormat.MasterRecord debtor = new DatevMasterDataFormat.MasterRecord();
        debtor.konto = "10001";
        debtor.adressattyp = "2";
        debtor.companyName = "Musterkunde GmbH";
        debtor.euCountry = "DE";
        debtor.euVatNumber = "123456789";
        debtor.zip = "10115";
        debtor.city = "Berlin";
        debtor.iban = "DE89370400440532013000";

        DatevMasterDataFormat.MasterRecord creditor = new DatevMasterDataFormat.MasterRecord();
        creditor.konto = "70001";
        creditor.adressattyp = "1";
        creditor.firstName = "Erika";
        creditor.lastName = "Muster";
        creditor.zip = "80331";
        creditor.city = "München";

        List<DatevMasterDataFormat.MasterRecord> records = Arrays.asList(debtor, creditor);

        // Missing Berater- & Mandantennummer must block export
        DatevConfig invalid = new DatevConfig();
        DatevExportDialog.Content bad = DatevExportContent.masterData(invalid, records, "EXTF_Stammdaten_20260101.csv");
        assertTrue(bad.blockOnErrors, "Missing Berater-/Mandantennummer must block Stammdaten export");
        assertTrue(bad.errors.stream().anyMatch(e -> e.contains("Beraternummer")));
        assertTrue(bad.errors.stream().anyMatch(e -> e.contains("Mandantennummer")));

        // Valid config builds clean preview with Debitor/Kreditor counts & Kontakte table
        DatevConfig valid = sampleConfig();
        DatevExportDialog.Content ok = DatevExportContent.masterData(valid, records, "EXTF_Stammdaten_20260101.csv");
        assertTrue(ok.errors.isEmpty(), "Valid config must produce no Stammdaten errors: " + ok.errors);
        assertFalse(ok.blockOnErrors);
        assertTrue(ok.facts.stream().anyMatch(f -> f[1].contains("1 Debitoren") && f[1].contains("1 Kreditoren")),
                "Facts must report Debitor and Kreditor counts");
        assertEquals(1, ok.tables.size());
        assertEquals("Kontakte", ok.tables.get(0).title);
        assertEquals(2, ok.tables.get(0).rows.size());
        assertEquals("10001", ok.tables.get(0).rows.get(0)[0]);
        assertEquals("Musterkunde GmbH", ok.tables.get(0).rows.get(0)[2]);
        assertEquals("70001", ok.tables.get(0).rows.get(1)[0]);
        assertEquals("Erika Muster", ok.tables.get(0).rows.get(1)[2]);
    }

    private static List<Account> flattenAccounts(ArrayList<ArrayList<DatabaseObject>> objs) {
        List<Account> res = new ArrayList<Account>();
        if (objs != null) {
            for (ArrayList<DatabaseObject> group : objs) {
                for (DatabaseObject o : group) {
                    if (o instanceof Account) {
                        res.add((Account) o);
                    }
                }
            }
        }
        return res;
    }
}
