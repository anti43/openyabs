package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.SubItem;
import mpv5.handler.JournalCalc;
import mpv5.handler.JournalLoader;
import mpv5.ui.toolbars.RecordActions;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * What a person does with a new installation, through the panels: a contact
 * and products, an offer of the products, the order and the invoice made from
 * it, payments, an expense and an income, the journal; no exception may be
 * logged on the way.
 */
class WorkflowTest {

    @TempDir
    Path tmp;

    private static Object[][] rows(String sql) throws Exception {
        Object[][] d = QueryHandler.getConnection().freeQuery(sql, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
        return d == null ? new Object[0][] : d;
    }

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    private static void assertMoney(String expected, BigDecimal actual, String what) {
        assertEquals(0, bd(expected).compareTo(actual.setScale(2, java.math.RoundingMode.HALF_UP)), what + ": " + actual);
    }

    private static Object call(Object o, String method, Class<?>[] types, Object... args) throws Exception {
        java.lang.reflect.Method m = o.getClass().getDeclaredMethod(method, types);
        m.setAccessible(true);
        return EInvoiceRoundTripTest.onEdt(() -> m.invoke(o, args));
    }

    private static Object field(Object o, String name) throws Exception {
        java.lang.reflect.Field f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(o);
    }

    private static int taxId(String rate) throws Exception {
        for (Object[] r : rows("SELECT ids, taxvalue FROM tax")) {
            if (new BigDecimal(String.valueOf(r[1])).compareTo(bd(rate)) == 0) {
                return Integer.parseInt(String.valueOf(r[0]));
            }
        }
        fail("no tax rate " + rate);
        return -1;
    }

    private static Item newest(int type) throws Exception {
        Object[][] r = rows("SELECT MAX(ids) FROM items WHERE inttype = " + type);
        assertNotNull(r[0][0], "a document of type " + type);
        return (Item) DatabaseObject.getObject(mpv5.handler.ItemService.contextOf(type), Integer.parseInt(String.valueOf(r[0][0])));
    }

    @Test
    void aWorkingDay() throws Exception {
        workingDay(tmp, null, false, null);
    }

    /**
     * The working day; with the GoBD plugin the exported invoice is finalized:
     * it can not be changed or deleted any more
     *
     * @param tmp
     * @param afterInstall runs after the installation (starts a plugin), may be
     * null
     * @param gobd true if the GoBD plugin runs
     * @param beforeTrash runs with the paid invoice before step 8, may be null
     * @return the invoice
     */
    static Item workingDay(Path tmp, java.util.concurrent.Callable<?> afterInstall, boolean gobd,
            java.util.function.Consumer<Item> beforeTrash) throws Exception {
        //the panels need a display (locally e.g. xvfb-run ./gradlew test --tests mpv5.db.WorkflowTest)
        org.junit.jupiter.api.Assumptions.assumeFalse(java.awt.GraphicsEnvironment.isHeadless(), "needs a display");
        //files written outside the temp folder are recorded with their checksum
        java.nio.file.Path sysTmp = tmp.resolve("systemp");
        java.nio.file.Files.createDirectories(sysTmp);
        System.setProperty("java.io.tmpdir", sysTmp.toString());
        EInvoiceRoundTripTest.newInstallation(tmp);
        mpv5.db.objects.User admin = mpv5.db.objects.User.getCurrentUser();
        for (String[] kv : new String[][]{{"business", "Wir GmbH"}, {"street", "Hauptstr. 1"}, {"zip", "50667"}, {"city", "K\u00f6ln"},
            {"country", "DE"}, {"vatid", "DE999999999"}, {"email", "info@wir.example"}, {"phone", "0221 123"}, {"iban", "DE02120300000000202051"}}) {
            admin.changeProperty("companyinfo." + kv[0], kv[1]);
        }
        admin.saveProperties();
        if (afterInstall != null) {
            afterInstall.call();
        }
        mpv5.logging.ErrorHistory.flush();
        long errorsBefore = Long.parseLong(String.valueOf(rows("SELECT COUNT(*) FROM history WHERE dbidentity = 'error'")[0][0]));

        //1. a customer in the contact panel
        mpv5.ui.panels.ModernContactPanel cp = EInvoiceRoundTripTest.onEdt(() -> new mpv5.ui.panels.ModernContactPanel(Context.getCustomer()));
        Contact c = new Contact();
        c.setCname("Muster");
        c.setPrename("Erika");
        c.setCompany("Muster GmbH");
        c.setStreet("Hauptstr. 5");
        c.setZip("50667");
        c.setCity("Köln");
        c.setCountry("DE");
        c.setMailaddress("erika@muster.example");
        c.setisCustomer(true);
        EInvoiceRoundTripTest.onEdt(() -> {
            cp.setDataOwner(c, true);
            RecordActions.save(cp);
            return null;
        });
        Object[][] cr = rows("SELECT ids, cnumber, city FROM contacts WHERE cname = 'Muster'");
        assertEquals(1, cr.length, "contact saved");
        assertEquals("Köln", cr[0][2]);
        Contact customer = (Contact) DatabaseObject.getObject(Context.getContact(), Integer.parseInt(String.valueOf(cr[0][0])));

        //2. two products in the product panel: 19 % and 7 %
        Product[] products = new Product[2];
        String[][] pd = {{"Beratung", "80.00", "19"}, {"Buch", "20.00", "7"}};
        for (int i = 0; i < 2; i++) {
            final int k = i;
            mpv5.ui.panels.ModernProductPanel pp = EInvoiceRoundTripTest.onEdt(() -> new mpv5.ui.panels.ModernProductPanel(Context.getProduct()));
            Product p = new Product();
            p.setCname(pd[k][0]);
            p.setDescription(pd[k][0]);
            p.setExternalnetvalue(bd(pd[k][1]));
            p.setInternalnetvalue(bd(pd[k][1]).divide(bd("2")));
            p.setTaxids(taxId(pd[k][2]));
            p.setMeasure("Stk");
            p.setInttype(Product.TYPE_PRODUCT);
            EInvoiceRoundTripTest.onEdt(() -> {
                pp.setDataOwner(p, true);
                RecordActions.save(pp);
                return null;
            });
            Object[][] pr = rows("SELECT ids, externalnetvalue, taxids FROM products WHERE cname = '" + pd[k][0] + "'");
            assertEquals(1, pr.length, pd[k][0] + " saved");
            assertEquals(0, bd(pd[k][1]).compareTo(new BigDecimal(String.valueOf(pr[0][1]))), "price kept");
            products[k] = (Product) DatabaseObject.getObject(Context.getProduct(), Integer.parseInt(String.valueOf(pr[0][0])));
        }

        //3. an offer: customer and products pasted, 3 x Beratung, 2 x Buch
        mpv5.ui.panels.ModernItemPanel op = EInvoiceRoundTripTest.onEdt(() -> new mpv5.ui.panels.ModernItemPanel(Context.getOffer(), Item.TYPE_OFFER));
        EInvoiceRoundTripTest.onEdt(() -> {
            op.paste(customer);
            op.paste(products[0]);
            op.paste(products[1]);
            mpv5.ui.panels.ItemPositionsTable t = (mpv5.ui.panels.ItemPositionsTable) field(op, "positions");
            t.getModel().setValueAt(bd("3"), 0, 2);
            t.getModel().setValueAt(bd("2"), 1, 2);
            t.recalculate();
            RecordActions.save(op);
            return null;
        });
        Item offer = newest(Item.TYPE_OFFER);
        assertEquals(customer.__getIDS(), offer.__getContactsids());
        SubItem[] os = offer.getSubitems();
        assertEquals(2, os.length, "two positions");
        //280 net: 240 at 19 % (45.60), 40 at 7 % (2.80)
        assertMoney("280.00", offer.__getNetvalue(), "offer net");
        assertMoney("48.40", offer.__getTaxvalue(), "offer tax");

        //4. the order from the offer, the invoice from the order (the menu items of the panel)
        EInvoiceRoundTripTest.onEdt(() -> {
            op.setDataOwner(offer, true);
            return null;
        });
        call(op, "convertToOrder", new Class<?>[0]);
        Item order = newest(Item.TYPE_ORDER);
        assertEquals(2, order.getSubitems().length, "order positions");
        assertMoney("280.00", order.__getNetvalue(), "order net");
        mpv5.ui.panels.ModernItemPanel orp = EInvoiceRoundTripTest.onEdt(() -> new mpv5.ui.panels.ModernItemPanel(Context.getOrder(), Item.TYPE_ORDER));
        EInvoiceRoundTripTest.onEdt(() -> {
            orp.setDataOwner(order, true);
            return null;
        });
        call(orp, "convertToInvoice", new Class<?>[]{int.class}, Item.TYPE_INVOICE);
        Object[][] invs = rows("SELECT COUNT(*) FROM items WHERE inttype = " + Item.TYPE_INVOICE);
        Item invoice;
        if ("0".equals(String.valueOf(invs[0][0]))) {
            //the panel asked a question (answered "no" without a display): as a person answering "yes"
            invoice = mpv5.handler.ItemService.toInvoice(order, Item.TYPE_INVOICE, SubItem.toModel(order.getSubitems(), true), q -> true);
        } else {
            invoice = newest(Item.TYPE_INVOICE);
        }
        assertNotNull(invoice);
        invoice = (Item) DatabaseObject.getObject(Context.getInvoice(), invoice.__getIDS());
        assertEquals(customer.__getIDS(), invoice.__getContactsids());
        assertEquals(2, invoice.getSubitems().length, "invoice positions");
        assertMoney("280.00", invoice.__getNetvalue(), "invoice net");
        assertMoney("48.40", invoice.__getTaxvalue(), "invoice tax");
        assertTrue(invoice.__getCnumber() != null && !invoice.__getCnumber().isEmpty(), "invoice number");

        //5. payments: 100 now, the rest later -> paid
        assertTrue(mpv5.handler.Payments.add(invoice, bd("100.00"), new Date(), "Anzahlung"));
        assertMoney("228.40", mpv5.handler.Payments.open((Item) DatabaseObject.getObject(Context.getInvoice(), invoice.__getIDS())), "open after first payment");
        assertTrue(mpv5.handler.Payments.add(invoice, bd("228.40"), new Date(), "Rest"));
        Item paid = (Item) DatabaseObject.getObject(Context.getInvoice(), invoice.__getIDS());
        assertEquals(Item.STATUS_PAID, paid.__getIntstatus(), "paid in full");

        //6. an expense and an income
        mpv5.db.objects.Expense ex = new mpv5.db.objects.Expense();
        ex.setCname("Miete");
        ex.setDescription("Miete");
        ex.setNetvalue(bd("100.00"));
        ex.setTaxpercentvalue(bd("19"));
        ex.setBrutvalue(bd("119.00"));
        ex.setAccountsids(1);
        ex.setGroupsids(1);
        ex.setIspaid(true);
        ex.setDateadded(new Date());
        ex.setDateend(new Date());
        assertTrue(ex.save(true));
        mpv5.db.objects.Revenue rv = new mpv5.db.objects.Revenue();
        rv.setCname("Zinsen");
        rv.setDescription("Zinsen");
        rv.setNetvalue(bd("10.00"));
        rv.setTaxpercentvalue(bd("0"));
        rv.setBrutvalue(bd("10.00"));
        rv.setAccountsids(1);
        rv.setGroupsids(1);
        rv.setStatus(Item.STATUS_PAID);
        rv.setDateadded(new Date());
        rv.setDateend(new Date());
        assertTrue(rv.save(true));

        //6b. a supplier's e-invoice: an expense of 50 net with its receipt
        java.io.File in = EInvoiceRoundTripTest.write(tmp.resolve("in"), "lieferant.xml", EInvoiceRoundTripTest.supplierInvoice("LIEF-7",
                new java.text.SimpleDateFormat("yyyyMMdd").format(new Date()), "Lieferant GmbH", "DE111111111", "Wir GmbH", "DE999999999", "50.00", "59.50"));
        mpv5.handler.EInvoiceImport.Result imp = EInvoiceRoundTripTest.onEdt(() -> mpv5.handler.EInvoiceImport.importFile(in, null));
        assertEquals(mpv5.handler.EInvoiceImport.Outcome.IMPORTED, imp.outcome, imp.message());
        mpv5.db.objects.Expense lief = (mpv5.db.objects.Expense) imp.record;
        lief.setIspaid(true);
        lief.setDateend(new Date());
        //with the plugin the expense is locked by its receipt: paying stays possible
        assertTrue(EInvoiceRoundTripTest.onEdt(() -> lief.save(true)), "marking an expense with receipt as paid");

        //6c. the invoice as PDF (template) and XRechnung, as "save as" does; the XRechnung imported again
        java.nio.file.Path out = tmp.resolve("out");
        java.nio.file.Files.createDirectories(out);
        Item forExport = (Item) DatabaseObject.getObject(Context.getInvoice(), invoice.__getIDS());
        mpv5.db.objects.Template tpl = mpv5.handler.TemplateHandler.loadTemplate((long) forExport.templateGroupIds(), Item.TYPE_INVOICE);
        assertNotNull(tpl, "invoice template");
        java.io.File pdf = out.resolve(forExport.__getCnumber() + ".pdf").toFile();
        mpv5.utils.export.Export ex2 = (mpv5.utils.export.Export) mpv5.utils.export.Export.createFile(tpl, forExport);
        assertNull(EInvoiceRoundTripTest.onEdt(() -> ex2.waitFor()), "PDF export");
        mpv5.utils.export.Export.wait(pdf).set(ex2, null);
        assertTrue(pdf.isFile() && pdf.length() > 1000, "PDF written");
        java.io.File xml = out.resolve(forExport.__getCnumber() + ".xml").toFile();
        mpv5.utils.export.ZugpferdFile z = new mpv5.utils.export.ZugpferdFile(forExport, xml, mpv5.utils.export.ZugpferdFile.Format.XRECHNUNG);
        assertNull(EInvoiceRoundTripTest.onEdt(() -> z.waitFor()), "XRechnung export");
        assertTrue(z.getValidation() != null && z.getValidation().isValid(), String.valueOf(z.getValidation()));
        mpv5.handler.EInvoiceImport.Result again = EInvoiceRoundTripTest.onEdt(() -> mpv5.handler.EInvoiceImport.importFile(xml, null));
        assertEquals(mpv5.handler.EInvoiceImport.Outcome.OWN_INVOICE, again.outcome, again.message());
        assertEquals(2, mpv5.handler.DocumentFiles.verify(forExport).size(), "checksums of PDF and XRechnung");

        //7. the journal of this year: invoice + income, expense; offer and order are no income
        Date now = new Date();
        JournalLoader.Loaded j = JournalLoader.loadAllResult(new vTimeframe(DateConverter.getStartOfYear(now), DateConverter.getEndOfYear(now)), false);
        JournalCalc.Totals t = j.totals;
        System.out.println("JOURNAL income " + t.incomeNet + "/" + t.incomeTax + "/" + t.incomeGross + " expenses " + t.expenseNet + "/" + t.expenseTax
                + "/" + t.expenseGross + " open offers " + t.openOffersNet + " orders " + t.openOrdersNet + " receivables " + t.openReceivables
                + " byRate " + rates(t.incomeByRate));
        assertMoney("290.00", t.incomeNet, "income net (invoice 280 + income 10)");
        assertMoney("48.40", t.incomeTax, "income tax");
        assertMoney("338.40", t.incomeGross, "income gross");
        assertMoney("150.00", t.expenseNet, "expense net (100 + e-invoice 50)");
        assertMoney("28.50", t.expenseTax, "expense tax");
        assertMoney("140.00", t.profitNet(), "profit net");
        assertMoney("0.00", t.openReceivables, "nothing open");
        assertMoney("45.60", t.incomeByRate.get(JournalCalc.rateKey(bd("19")))[1], "tax 19 %");
        assertMoney("2.80", t.incomeByRate.get(JournalCalc.rateKey(bd("7")))[1], "tax 7 %");
        //by payment date (EUeR): the same, all paid today
        JournalCalc.Totals byPay = JournalLoader.loadAllResult(new vTimeframe(DateConverter.getStartOfYear(now), DateConverter.getEndOfYear(now)), true).totals;
        assertMoney("290.00", byPay.incomeNet, "income net by payment date");
        assertMoney("150.00", byPay.expenseNet, "expense net by payment date");

        if (beforeTrash != null) {
            beforeTrash.accept((Item) DatabaseObject.getObject(Context.getInvoice(), invoice.__getIDS()));
        }

        //8. the paid invoice into the trash bin
        Item toDelete = (Item) DatabaseObject.getObject(Context.getInvoice(), invoice.__getIDS());
        boolean deleted = EInvoiceRoundTripTest.onEdt(() -> toDelete.delete());
        JournalCalc.Totals after = JournalLoader.loadAllResult(new vTimeframe(DateConverter.getStartOfYear(now), DateConverter.getEndOfYear(now)), false).totals;
        if (gobd) {
            //finalized on export: neither changed nor deleted
            assertFalse(deleted, "a finalized invoice can not be deleted");
            assertMoney("290.00", after.incomeNet, "still income");
            Item change = (Item) DatabaseObject.getObject(Context.getInvoice(), invoice.__getIDS());
            change.setNetvalue(bd("1.00"));
            assertFalse(EInvoiceRoundTripTest.onEdt(() -> change.save(true)), "a finalized invoice can not be changed");
        } else {
            assertTrue(deleted);
            assertMoney("10.00", after.incomeNet, "the trashed invoice is no income");
        }

        //9. no exception on the way
        mpv5.logging.ErrorHistory.flush();
        List<String> errors = new ArrayList<String>();
        for (Object[] r : rows("SELECT cname, reserve2 FROM history WHERE dbidentity = 'error' ORDER BY ids")) {
            errors.add(r[0] + "\n" + r[1]);
        }
        System.out.println("ERRORS (" + errors.size() + ", before " + errorsBefore + "):\n" + String.join("\n----\n", errors));
        assertEquals(errorsBefore, errors.size(), String.join("\n----\n", errors));
        return (Item) DatabaseObject.getObject(Context.getInvoice(), invoice.__getIDS(), true);
    }

    private static String rates(java.util.Map<BigDecimal, BigDecimal[]> m) {
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<BigDecimal, BigDecimal[]> e : m.entrySet()) {
            sb.append(e.getKey()).append("%=").append(e.getValue()[0]).append("/").append(e.getValue()[1]).append(" ");
        }
        return sb.toString();
    }
}
