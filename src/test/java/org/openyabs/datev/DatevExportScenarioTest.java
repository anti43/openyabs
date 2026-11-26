package org.openyabs.datev;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.User;
import mpv5.globals.LocalSettings;
import mpv5.handler.JournalCalc;
import mpv5.handler.JournalLoader;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The DATEV exports of one month with the documents of a real database:
 * invoices with several rates and a discount, an EU supply, an open invoice,
 * a credit note, a cancelled invoice with its cancellation, a cancelled
 * invoice without one, an invoice of the month before paid in the month, an
 * invoice paid in two parts and an expense. Checks that nothing is missing,
 * nothing is counted twice, every amount is the amount of the document and
 * every date is in the period - for the document package (by invoice date)
 * and the booking batch (by pay date and by document date). With
 * {@code -Ddatev.screenshots=<dir>} (and a display) the dialogs are saved as
 * PNG.
 */
public class DatevExportScenarioTest {

    @TempDir
    Path tmp;

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    private static Date date(String s) throws Exception {
        return new SimpleDateFormat("yyyy-MM-dd").parse(s);
    }

    private static void money(String expected, BigDecimal actual, String what) {
        assertEquals(0, bd(expected).compareTo(actual.setScale(2, RoundingMode.HALF_UP)), what + ": " + actual);
    }

    private void install() throws Exception {
        File dir = tmp.toFile();
        mpv5.Main.MPPATH = dir.getPath();
        mpv5.Main.SETTINGS_FILE = new File(dir, "settings.yabs").getPath();
        String location = tmp.resolve("h2").toString();
        LocalSettings.setProperty(LocalSettings.DBPATH, location);
        LocalSettings.setProperty(LocalSettings.DBDRIVER, ConnectionTypeHandler.H2_DRIVER);
        LocalSettings.setProperty(LocalSettings.DBNAME, "yabs");
        LocalSettings.setProperty(LocalSettings.DBUSER, "");
        LocalSettings.setProperty(LocalSettings.DBPASSWORD, "");
        LocalSettings.setProperty(LocalSettings.CACHE_DIR, new File(dir, "cache").getPath());
        LocalSettings.save(0);
        DatabaseConnection conn = new DatabaseConnection();
        assertTrue(conn.connect(ConnectionTypeHandler.H2_DRIVER, "", "", location, "yabs", "", true));
        assertTrue(conn.runQueries(new DatabaseInstallation().getStructure()));
        assertTrue(conn.runQueries(new DatabaseInstallation().getInitialData()));
        QueryHandler.instanceOf();
        User admin = (User) DatabaseObject.getObject(Context.getUser(), "admin");
        User._setUser(admin);
        mpv5.handler.FirstStartSetup.prepare(admin, true, null);
    }

    private static Contact customer(String company, String city, String country, String vatId) throws Exception {
        Contact c = new Contact();
        c.setCname(company);
        c.setCompany(company);
        c.setStreet("Ring 2");
        c.setZip("10115");
        c.setCity(city);
        c.setCountry(country);
        c.setMailaddress("info@" + company.replaceAll("[^a-z]", "") + ".example");
        c.settaxnumber(vatId);
        c.setisCustomer(true);
        c.setDateadded(new Date());
        assertTrue(c.save(true));
        return c;
    }

    /**
     * @param lines {quantity, price, rate, discount %}
     */
    private static Item document(Context ctx, int type, String number, Contact c, Date date, Object[]... lines) throws Exception {
        Item inv = (Item) DatabaseObject.getObject(ctx);
        inv.setContactsids(c.__getIDS());
        inv.setCname(number);
        inv.setCnumber(number);
        inv.setInttype(type);
        inv.setDateadded(date);
        inv.setDatetodo(date);
        inv.setAccountsids(1);
        inv.defineFormatHandler(new mpv5.handler.FormatHandler(inv) {
            @Override
            public synchronized String next() {
                return number;
            }
        });
        assertTrue(inv.save(true));
        //inserting sets the current time: the document date as the view keeps it
        inv.setDateadded(date);
        assertTrue(inv.save(true));
        int i = 1;
        for (Object[] l : lines) {
            SubItem s = new SubItem();
            s.setItemsids(inv.__getIDS());
            s.setCname("Position " + i);
            s.setDescription("Position " + i);
            s.setQuantityvalue(bd(String.valueOf(l[0])));
            s.setExternalvalue(bd(String.valueOf(l[1])));
            s.setInternalvalue(bd(String.valueOf(l[1])));
            s.setTaxpercentvalue(bd(String.valueOf(l[2])));
            s.setDiscount(l.length > 3 ? bd(String.valueOf(l[3])) : BigDecimal.ZERO);
            s.setOrdernr(i++);
            s.setDateadded(date);
            assertTrue(s.save(true));
        }
        mpv5.handler.ItemService.updateTotals(inv);
        Item saved = (Item) DatabaseObject.getObject(ctx, inv.__getIDS());
        assertEquals(number, saved.__getCnumber());
        return saved;
    }

    private static Item paid(Item i, Date when) throws Exception {
        i.setIntstatus(Item.STATUS_PAID);
        i.setDateend(when);
        assertTrue(i.save(true));
        return (Item) DatabaseObject.getObject(mpv5.handler.ItemService.contextOf(i.__getInttype()), i.__getIDS());
    }

    private static Item status(Item i, int status) throws Exception {
        i.setIntstatus(status);
        assertTrue(i.save(true));
        return (Item) DatabaseObject.getObject(mpv5.handler.ItemService.contextOf(i.__getInttype()), i.__getIDS());
    }

    /**
     * @return gross of the document as stored: net - discount + tax
     */
    private static BigDecimal gross(Item i) {
        return i.__getNetvalue().subtract(i.__getDiscountvalue() == null ? BigDecimal.ZERO : i.__getDiscountvalue()).add(i.__getTaxvalue());
    }

    /**
     * A new invoice written in the invoice view (customer and product pasted)
     * and dated back: it keeps the document date of the view
     */
    private static Item viewInvoice(Contact c, Date back) throws Exception {
        mpv5.db.objects.Product p = new mpv5.db.objects.Product();
        p.setCname("Beratung");
        p.setDescription("Beratung");
        p.setExternalnetvalue(bd("100.00"));
        p.setInttype(mpv5.db.objects.Product.TYPE_SERVICE);
        assertTrue(p.save(true));
        final mpv5.ui.panels.ModernItemPanel[] panel = new mpv5.ui.panels.ModernItemPanel[1];
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            try {
                panel[0] = new mpv5.ui.panels.ModernItemPanel(Context.getInvoice(), Item.TYPE_INVOICE);
                panel[0].paste(c);
                panel[0].paste(p);
                java.lang.reflect.Field f = mpv5.ui.panels.ModernItemPanel.class.getDeclaredField("date1");
                f.setAccessible(true);
                ((mpv5.ui.beans.LabeledDateChooser) f.get(panel[0])).setDate(back);
                mpv5.ui.dialogs.Popup.silently(() -> {
                    mpv5.ui.toolbars.RecordActions.save(panel[0]);
                    return null;
                });
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        Object[][] r = QueryHandler.getConnection().freeQuery("SELECT ids, dateadded FROM items WHERE cnumber LIKE 'RE-%'",
                mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
        assertEquals(1, r.length, "saved in the view");
        assertTrue(String.valueOf(r[0][1]).startsWith(new SimpleDateFormat("yyyy-MM-dd").format(back)),
                "the document date of the view, not the day it was written: " + r[0][1]);
        return (Item) DatabaseObject.getObject(Context.getInvoice(), Integer.parseInt(String.valueOf(r[0][0])));
    }

    @Test
    public void aMonthForDatev() throws Exception {
        install();
        Date from = date("2026-03-01");
        Date to = DateConverter.getEndOfDay(date("2026-03-31"));
        Contact de = customer("Kunde AG", "Berlin", "DE", "");
        Contact fr = customer("Client SARL", "Paris", "FR", "FR12345678901");

        //R1: two rates, 10 % discount on the first line, paid in the month
        Item r1 = paid(document(Context.getInvoice(), Item.TYPE_INVOICE, "R-2026-001", de, date("2026-03-02"),
                new Object[]{2, "100.00", 19, 10}, new Object[]{1, "50.00", 7}), date("2026-03-10"));
        money("230.00", r1.__getNetvalue().subtract(r1.__getDiscountvalue()), "R1 net after discount (200 - 10 % + 50)");
        //R2: EU supply without VAT, paid
        Item r2 = paid(document(Context.getInvoice(), Item.TYPE_INVOICE, "R-2026-002", fr, date("2026-03-05"),
                new Object[]{1, "1000.00", 0}), date("2026-03-06"));
        //R3: open
        Item r3 = document(Context.getInvoice(), Item.TYPE_INVOICE, "R-2026-003", de, date("2026-03-08"), new Object[]{3, "100.00", 19});
        //G1: a credit note, refunded in the month
        Item g1 = paid(document(Context.getCredit(), Item.TYPE_CREDIT, "G-2026-001", de, date("2026-03-12"), new Object[]{1, "100.00", 19}),
                date("2026-03-12"));
        //R4 cancelled by ST1
        Item r4 = document(Context.getInvoice(), Item.TYPE_INVOICE, "R-2026-004", de, date("2026-03-15"), new Object[]{1, "200.00", 19});
        Item st1 = document(Context.getCredit(), Item.TYPE_CREDIT, "ST-2026-001", de, date("2026-03-16"), new Object[]{1, "200.00", 19});
        st1.setRefinvoiceids(r4.__getIDS());
        assertTrue(st1.save(true));
        r4 = status(r4, Item.STATUS_CANCELLED);
        //R5 cancelled without a cancellation (never issued)
        Item r5 = status(document(Context.getInvoice(), Item.TYPE_INVOICE, "R-2026-005", de, date("2026-03-18"), new Object[]{1, "999.00", 19}),
                Item.STATUS_CANCELLED);
        //R6 of February, paid in March
        Item r6 = paid(document(Context.getInvoice(), Item.TYPE_INVOICE, "R-2026-006", de, date("2026-02-27"), new Object[]{1, "400.00", 19}),
                date("2026-03-03"));
        //R7 paid in two parts: March and April
        Item r7 = document(Context.getInvoice(), Item.TYPE_INVOICE, "R-2026-007", de, date("2026-03-22"), new Object[]{1, "500.00", 19});
        assertTrue(mpv5.handler.Payments.add(r7, bd("300.00"), date("2026-03-25"), "Teil 1"));
        assertTrue(mpv5.handler.Payments.add(r7, bd("295.00"), date("2026-04-05"), "Teil 2"));
        //RV: written in the invoice view today, dated back to March 2 (needs a display)
        boolean view = !java.awt.GraphicsEnvironment.isHeadless();
        Item rv = view ? viewInvoice(de, date("2026-03-02")) : null;
        //E1: an expense, paid
        Expense e1 = new Expense();
        e1.setCname("E-2026-001");
        e1.setCnumber("E-2026-001");
        e1.setDescription("Büromaterial");
        e1.setNetvalue(bd("100.00"));
        e1.setTaxpercentvalue(bd("19"));
        e1.setBrutvalue(bd("119.00"));
        e1.setAccountsids(1);
        e1.setGroupsids(1);
        e1.setDateadded(date("2026-03-20"));
        e1.setIspaid(true);
        e1.setDateend(date("2026-03-20"));
        assertTrue(e1.save(true));
        e1.setDateadded(date("2026-03-20"));
        assertTrue(e1.save(true));

        DatevConfig config = DatevConfig.loadWithDefaults(null);
        config.beraterNummer = 1001;
        config.mandantenNummer = 10;
        config.wjBeginn = "20260101";

        // ------------------------------------------------ the document package (by invoice date)
        DatevXmlExport.Collected col = DatevXmlExport.load(from, to, config, (n, r) -> n + ": " + r);
        Map<String, DatevXml.Doc> docs = new HashMap<String, DatevXml.Doc>();
        for (DatevXml.Doc d : col.docs) {
            assertFalse(docs.containsKey(d.number), "once: " + d.number);
            docs.put(d.number, d);
        }
        System.out.println("PACKAGE " + docs.keySet() + " left out " + col.excluded.size() + " notes " + col.problems);
        java.util.TreeSet<String> expectedDocs = new java.util.TreeSet<String>(List.of("R-2026-001", "R-2026-002", "R-2026-003", "G-2026-001",
                "R-2026-004", "ST-2026-001", "R-2026-007"));
        if (rv != null) {
            expectedDocs.add(rv.__getCnumber());
        }
        assertEquals(expectedDocs, new java.util.TreeSet<String>(docs.keySet()), "every outgoing document of March by its invoice date, open ones too");
        assertEquals(1, col.excluded.size());
        assertEquals("R-2026-005", col.excluded.get(0).number, "cancelled without a cancellation: never issued");
        assertTrue(docs.get("R-2026-004").cancelled, "the cancelled invoice goes with its cancellation");
        List<Item> all = new ArrayList<Item>(List.of(r1, r2, r3, g1, r4, st1, r7));
        if (rv != null) {
            all.add(rv);
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (Item i : all) {
            DatevXml.Doc d = docs.get(i.__getCnumber());
            BigDecimal expected = gross(i);
            if (i.__getInttype() == Item.TYPE_CREDIT) {
                expected = expected.negate();
            }
            money(expected.toPlainString(), d.gross(), i.__getCnumber() + " gross as the document");
            sum = sum.add(expected);
            assertEquals(new SimpleDateFormat("yyyyMMdd").format(i.__getDateadded()), new SimpleDateFormat("yyyyMMdd").format(d.date),
                    i.__getCnumber() + ": the invoice date");
            String ledger = DatevXml.ledger(d, "DE999999999");
            assertTrue(ledger.contains("consolidatedAmount=\"" + expected.setScale(2, RoundingMode.HALF_UP).toPlainString() + "\""),
                    i.__getCnumber() + ": " + ledger);
        }
        money(sum.toPlainString(), col.totals()[2], "the sum of the package");
        //R1: 19 % 180,00 + 34,20, 7 % 50,00 + 3,50
        DatevXml.Doc d1 = docs.get("R-2026-001");
        assertEquals(2, d1.lines.size());
        Map<BigDecimal, DatevXml.Line> byRate = new HashMap<BigDecimal, DatevXml.Line>();
        for (DatevXml.Line l : d1.lines) {
            byRate.put(JournalCalc.rateKey(l.rate), l);
        }
        money("214.20", byRate.get(JournalCalc.rateKey(bd("19"))).gross(), "R1 19 %");
        money("53.50", byRate.get(JournalCalc.rateKey(bd("7"))).gross(), "R1 7 %");
        assertEquals("8400", byRate.get(JournalCalc.rateKey(bd("19"))).account);
        assertEquals("8300", byRate.get(JournalCalc.rateKey(bd("7"))).account);
        assertEquals("8125", docs.get("R-2026-002").lines.get(0).account, "EU supply");
        assertEquals("FR12345678901", docs.get("R-2026-002").vatId);
        if (rv != null) {
            //0 % without category: a tax-free domestic sale, not an export
            assertEquals("8100", docs.get(rv.__getCnumber()).lines.get(0).account);
            assertTrue(col.problems.stream().anyMatch(p -> p.contains("steuerfreie Inlandsumsätze") && p.contains(rv.__getCnumber())), String.valueOf(col.problems));
        }
        assertNotNull(docs.get("R-2026-001").paidAt, "paid: the pay date for DATEV");
        assertEquals(null, docs.get("R-2026-003").paidAt, "open: no pay date");
        for (String p : col.problems) {
            assertFalse(p.contains("weicht vom Beleg ab"), p);
        }
        //the package itself
        byte[] zip = DatevXml.zip(col.docs, "DE999999999", new Date(), config.invoiceFolder);
        assertTrue(zip.length > 0);

        // ------------------------------------------------ the booking batch by pay date
        JournalLoader.Loaded byPay = JournalLoader.loadAllResult(new vTimeframe(from, to), true);
        DatevMapper.Result batch = DatevMapper.map(byPay.entries, config, from, to);
        List<String> numbers = new ArrayList<String>();
        for (DatevFormat.BookingLine l : batch.lines) {
            numbers.add(l.belegfeld1 + "/" + l.sollHaben + "/" + l.umsatz.setScale(2, RoundingMode.HALF_UP).toPlainString());
            String day = l.belegdatum;
            assertEquals("03", day.substring(2), "booked in March: " + l.belegfeld1 + " " + day);
        }
        System.out.println("BATCH BY PAY DATE " + numbers + " issues " + batch.issues);
        assertTrue(batch.issues.stream().noneMatch(i -> i.severity == DatevMapper.Issue.Severity.ERROR), String.valueOf(batch.issues));
        //into the bank: R1 267,70, R2 1000, R6 476 (of February, paid in March), R7 part 300
        money("2043.70", batch.sumDebit(), "income paid in March");
        //out: E1 119, G1 119
        money("238.00", batch.sumCredit(), "expense and refund in March");
        assertTrue(numbers.stream().noneMatch(n -> n.startsWith("R-2026-003/")), "open: not booked");
        assertTrue(numbers.stream().noneMatch(n -> n.startsWith("R-2026-004/") || n.startsWith("R-2026-005/")), "cancelled: not booked");
        //the dialog lists what is left out
        List<JournalCalc.Entry> periodDocs = JournalLoader.loadAll(new vTimeframe(from, to), false);
        DatevExportDialog.Content batchContent = DatevExportContent.batch(config, from, to, byPay.entries, periodDocs, batch,
                DatevFormat.fileName(from, to));
        DatevExportDialog.Table left = batchContent.tables.get(1);
        Map<String, String> leftReasons = new HashMap<String, String>();
        for (Object[] r : left.rows) {
            leftReasons.put(String.valueOf(r[1]), String.valueOf(r[5]));
        }
        System.out.println("BATCH NOT BOOKED " + leftReasons);
        //every document of March that is not (fully) in the batch is named with its reason
        assertTrue(leftReasons.get("R-2026-003").startsWith("offen"), "open");
        assertEquals("storniert", leftReasons.get("R-2026-004"));
        assertEquals("storniert", leftReasons.get("R-2026-005"));
        assertTrue(leftReasons.get("ST-2026-001").startsWith("offen"), "the cancellation is not refunded");
        assertTrue(leftReasons.get("R-2026-007").startsWith("teilweise"), "paid in parts: March booked, April later");
        assertFalse(leftReasons.containsKey("R-2026-001"), "booked");
        assertFalse(leftReasons.containsKey("E-2026-001"), "booked");
        if (rv != null) {
            assertTrue(leftReasons.get(rv.__getCnumber()).startsWith("offen"));
        }

        // ------------------------------------------------ the booking batch by document date
        config.belegdatumMode = DatevConfig.DateMode.DOCUMENT_DATE;
        //the old way: the journal by pay date with document dates: R6 (February) outside the batch - now an error
        DatevMapper.Result wrong = DatevMapper.map(byPay.entries, config, from, to);
        assertTrue(wrong.issues.stream().anyMatch(i -> i.severity == DatevMapper.Issue.Severity.ERROR && i.message.contains("R-2026-006")),
                "a booking outside the period is an error: " + wrong.issues);
        //the export loads by document date: every date in March
        JournalLoader.Loaded byDoc = JournalLoader.loadAllResult(new vTimeframe(from, to), false);
        DatevMapper.Result batchDoc = DatevMapper.map(byDoc.entries, config, from, to);
        System.out.println("BATCH BY DOCUMENT DATE " + batchDoc.lines.size() + " issues " + batchDoc.issues);
        assertTrue(batchDoc.issues.stream().noneMatch(i -> i.severity == DatevMapper.Issue.Severity.ERROR), String.valueOf(batchDoc.issues));
        for (DatevFormat.BookingLine l : batchDoc.lines) {
            assertEquals("03", l.belegdatum.substring(2), l.belegfeld1);
            assertFalse(l.belegfeld1.equals("R-2026-006"), "February invoice: not in March by document date");
        }
        assertTrue(DatevExportContent.batch(config, from, to, byDoc.entries, byDoc.entries, batchDoc, "x.csv").tables.get(1).rows.stream()
                .anyMatch(r -> "R-2026-003".equals(r[1])), "by document date: the open invoice is named too");

        // ------------------------------------------------ screenshots of the dialogs
        String shots = System.getProperty("datev.screenshots", System.getenv("DATEV_SCREENSHOTS"));
        if (shots != null && !java.awt.GraphicsEnvironment.isHeadless()) {
            config.belegdatumMode = DatevConfig.DateMode.PAYMENT_DATE;
            //the plugin as installed: its exports in the menu of the journal
            mpv5.plugin.ExtensionManager m = mpv5.plugin.ExtensionManager.instance();
            mpv5.plugin.ExtensionManager.Installed datev = m.register(new DatevPlugin());
            assertTrue(mpv5.ui.dialogs.Popup.silently(() -> m.start(datev)), String.valueOf(datev.getError()));
            DatevExportDialog.Content pkg = DatevExportContent.documentPackage(col, from, to, config.invoiceFolder,
                    "DATEV_Belege_20260301_20260331.zip", true, config.kontenrahmen);
            DatevExportScreenshots.save(new File(shots), "datev-batch.png", batchContent, 0);
            DatevExportScreenshots.save(new File(shots), "datev-batch-left.png", batchContent, 1);
            DatevExportScreenshots.save(new File(shots), "datev-batch-notes.png", batchContent, 2);
            DatevExportScreenshots.save(new File(shots), "datev-package.png", pkg, 0);
            DatevExportScreenshots.save(new File(shots), "datev-package-left.png", pkg, 1);
            DatevExportScreenshots.save(new File(shots), "datev-package-notes.png", pkg, 2);
            DatevExportScreenshots.journal(new File(shots), "journal-export-menu.png", from, to);
            java.nio.file.Files.write(new File(shots, "DATEV_Belege_20260301_20260331.zip").toPath(), zip);
            java.nio.file.Files.write(new File(shots, DatevFormat.fileName(from, to)).toPath(),
                    DatevFormat.encode(DatevFormat.render(config, from, to, new Date(), batch.lines)));
        }
    }
}
