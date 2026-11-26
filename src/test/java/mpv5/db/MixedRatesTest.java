package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.User;
import mpv5.handler.EInvoiceImport;
import mpv5.handler.JournalCalc;
import mpv5.handler.JournalLoader;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.export.ZugpferdFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Three VAT rates (19 %, 7 %, 0 %) in one document, incoming and outgoing:
 * import, export (XRechnung, ZUGFeRD), payments, journal by rate, DATEV.
 */
class MixedRatesTest {

    @TempDir
    Path tmp;

    static Object[][] rows(String sql) throws Exception {
        Object[][] d = QueryHandler.getConnection().freeQuery(sql, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
        return d == null ? new Object[0][] : d;
    }

    static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    static void money(String expected, BigDecimal actual, String what) {
        assertNotNull(actual, what);
        assertEquals(0, bd(expected).compareTo(actual.setScale(2, java.math.RoundingMode.HALF_UP)), what + ": " + actual);
    }

    /**
     * A supplier invoice with 19 %, 7 % and 0 % (CII): 100 + 19, 50 + 3.50,
     * 20 + 0 = 192.50
     */
    static String mixedSupplierInvoice(String number, String date) {
        return mixedInvoice(number, date, "Lieferant GmbH", "DE111111111", "Wir GmbH", "DE999999999");
    }

    static String mixedInvoice(String number, String date, String seller, String sellerVat, String buyer, String buyerVat) {
        String tax = "<ram:ApplicableTradeTax><ram:CalculatedAmount>%s</ram:CalculatedAmount><ram:TypeCode>VAT</ram:TypeCode>"
                + "%s<ram:BasisAmount>%s</ram:BasisAmount><ram:CategoryCode>%s</ram:CategoryCode>"
                + "<ram:RateApplicablePercent>%s</ram:RateApplicablePercent></ram:ApplicableTradeTax>";
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<rsm:CrossIndustryInvoice xmlns:rsm=\"urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100\""
                + " xmlns:ram=\"urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100\""
                + " xmlns:udt=\"urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100\">\n"
                + "<rsm:ExchangedDocument><ram:ID>" + number + "</ram:ID><ram:TypeCode>380</ram:TypeCode>"
                + "<ram:IssueDateTime><udt:DateTimeString format=\"102\">" + date + "</udt:DateTimeString></ram:IssueDateTime></rsm:ExchangedDocument>\n"
                + "<rsm:SupplyChainTradeTransaction>"
                + "<ram:IncludedSupplyChainTradeLineItem><ram:SpecifiedTradeProduct><ram:Name>Werkzeug</ram:Name></ram:SpecifiedTradeProduct></ram:IncludedSupplyChainTradeLineItem>"
                + "<ram:IncludedSupplyChainTradeLineItem><ram:SpecifiedTradeProduct><ram:Name>Fachbuch</ram:Name></ram:SpecifiedTradeProduct></ram:IncludedSupplyChainTradeLineItem>"
                + "<ram:IncludedSupplyChainTradeLineItem><ram:SpecifiedTradeProduct><ram:Name>Porto</ram:Name></ram:SpecifiedTradeProduct></ram:IncludedSupplyChainTradeLineItem>"
                + "<ram:ApplicableHeaderTradeAgreement>"
                + "<ram:SellerTradeParty><ram:Name>" + seller + "</ram:Name><ram:SpecifiedTaxRegistration><ram:ID schemeID=\"VA\">" + sellerVat + "</ram:ID></ram:SpecifiedTaxRegistration></ram:SellerTradeParty>"
                + "<ram:BuyerTradeParty><ram:Name>" + buyer + "</ram:Name><ram:SpecifiedTaxRegistration><ram:ID schemeID=\"VA\">" + buyerVat + "</ram:ID></ram:SpecifiedTaxRegistration></ram:BuyerTradeParty>"
                + "</ram:ApplicableHeaderTradeAgreement>"
                + "<ram:ApplicableHeaderTradeSettlement><ram:InvoiceCurrencyCode>EUR</ram:InvoiceCurrencyCode>"
                + String.format(tax, "19.00", "", "100.00", "S", "19.00")
                + String.format(tax, "3.50", "", "50.00", "S", "7.00")
                + String.format(tax, "0.00", "<ram:ExemptionReason>Porto (durchlaufender Posten)</ram:ExemptionReason>", "20.00", "E", "0.00")
                + "<ram:SpecifiedTradeSettlementHeaderMonetarySummation><ram:LineTotalAmount>170.00</ram:LineTotalAmount>"
                + "<ram:TaxBasisTotalAmount>170.00</ram:TaxBasisTotalAmount><ram:TaxTotalAmount currencyID=\"EUR\">22.50</ram:TaxTotalAmount>"
                + "<ram:GrandTotalAmount>192.50</ram:GrandTotalAmount><ram:DuePayableAmount>192.50</ram:DuePayableAmount>"
                + "</ram:SpecifiedTradeSettlementHeaderMonetarySummation>"
                + "</ram:ApplicableHeaderTradeSettlement></rsm:SupplyChainTradeTransaction>\n"
                + "</rsm:CrossIndustryInvoice>\n";
    }

    @Test
    void threeRates() throws Exception {
        scenario(tmp, null);
    }

    /**
     * @param afterInstall starts a plugin, may be null
     * @return the own invoice
     */
    static Item scenario(Path tmp, java.util.concurrent.Callable<?> afterInstall) throws Exception {
        Path sysTmp = tmp.resolve("systemp");
        Files.createDirectories(sysTmp);
        System.setProperty("java.io.tmpdir", sysTmp.toString());
        EInvoiceRoundTripTest.newInstallation(tmp);
        User admin = User.getCurrentUser();
        for (String[] kv : new String[][]{{"business", "Wir GmbH"}, {"street", "Hauptstr. 1"}, {"zip", "50667"}, {"city", "Köln"},
            {"country", "DE"}, {"vatid", "DE999999999"}, {"email", "info@wir.example"}, {"phone", "0221 123"}, {"iban", "DE02120300000000202051"}}) {
            admin.changeProperty("companyinfo." + kv[0], kv[1]);
        }
        admin.saveProperties();
        if (afterInstall != null) {
            afterInstall.call();
        }
        String today = new java.text.SimpleDateFormat("yyyyMMdd").format(new Date());

        //--- 1. the supplier invoice with three rates
        File in = EInvoiceRoundTripTest.write(tmp.resolve("in"), "mixed.xml", mixedSupplierInvoice("MIX-1", today));
        mpv5.handler.ZugpferdExpenseImport.ParsedInvoice parsed = mpv5.handler.ZugpferdExpenseImport.parse(in);
        assertNotNull(parsed);
        money("170.00", parsed.netAmount, "net");
        money("192.50", parsed.grossAmount, "gross");
        EInvoiceImport.Result r = EInvoiceRoundTripTest.onEdt(() -> EInvoiceImport.importFile(in, null));
        assertEquals(EInvoiceImport.Outcome.IMPORTED, r.outcome, r.message());
        System.out.println("IMPORTED: " + java.util.Arrays.deepToString(rows("SELECT cnumber, netvalue, taxpercentvalue, brutvalue, description FROM expenses ORDER BY ids")));
        //one booking per rate (an expense has one rate): 100 at 19 %, 50 at 7 %, 20 at 0 %
        Object[][] ex = rows("SELECT netvalue, taxpercentvalue, brutvalue FROM expenses ORDER BY taxpercentvalue DESC");
        assertEquals(3, ex.length, "one expense per VAT rate");
        String[][] expected = {{"100.00", "19", "119.00"}, {"50.00", "7", "53.50"}, {"20.00", "0", "20.00"}};
        for (int i = 0; i < 3; i++) {
            money(expected[i][0], new BigDecimal(String.valueOf(ex[i][0])), "net " + expected[i][1] + " %");
            assertEquals(0, bd(expected[i][1]).compareTo(new BigDecimal(String.valueOf(ex[i][1]))), "rate");
            money(expected[i][2], new BigDecimal(String.valueOf(ex[i][2])), "gross " + expected[i][1] + " %");
        }
        assertEquals(3, Integer.parseInt(String.valueOf(rows("SELECT COUNT(*) FROM expense_receipts")[0][0])), "each with the receipt");
        //the same file again: nothing new
        assertEquals(EInvoiceImport.Outcome.DUPLICATE_FILE, EInvoiceImport.importFile(in, null).outcome);
        for (DatabaseObject o : DatabaseObject.getObjects(Context.getExpense())) {
            mpv5.db.objects.Expense e = (mpv5.db.objects.Expense) o;
            e.setIspaid(true);
            e.setDateend(new Date());
            assertTrue(EInvoiceRoundTripTest.onEdt(() -> e.save(true)));
        }

        //--- 1b. an own invoice of another program with three rates: three income bookings (not paid yet)
        File own = EInvoiceRoundTripTest.write(tmp.resolve("in"), "alt-mix.xml", mixedInvoice("ALT-MIX", today, "Wir GmbH", "DE999999999", "Kunde AG", "DE222222222"));
        EInvoiceImport.Result ri = EInvoiceRoundTripTest.onEdt(() -> EInvoiceImport.importFile(own, null));
        assertEquals(EInvoiceImport.Outcome.IMPORTED, ri.outcome, ri.message());
        assertEquals(EInvoiceImport.Kind.INCOME, ri.kind);
        assertEquals(3, Integer.parseInt(String.valueOf(rows("SELECT COUNT(*) FROM revenues")[0][0])), "one income per VAT rate");
        assertEquals(3, Integer.parseInt(String.valueOf(rows("SELECT COUNT(*) FROM revenue_receipts")[0][0])));
        money("192.50", new BigDecimal(String.valueOf(rows("SELECT SUM(brutvalue) FROM revenues")[0][0])), "income parts add up");
        //the same invoice in another file: found by the sum of its parts
        File own2 = EInvoiceRoundTripTest.write(tmp.resolve("in"), "alt-mix-kopie.xml", mixedInvoice("ALT-MIX", today, "Wir GmbH", "DE999999999", "Kunde AG", "DE222222222") + " ");
        assertNotEquals(EInvoiceImport.Outcome.IMPORTED, EInvoiceImport.importFile(own2, null).outcome);
        File sup2 = EInvoiceRoundTripTest.write(tmp.resolve("in"), "mixed-kopie.xml", mixedSupplierInvoice("MIX-1", today) + " ");
        assertEquals(EInvoiceImport.Outcome.DUPLICATE_NUMBER, EInvoiceImport.importFile(sup2, null).outcome, "expense parts by their sum");

        //--- 2. an own invoice with three rates
        Contact c = new Contact();
        c.setCname("Kunde");
        c.setCompany("Kunde AG");
        c.setStreet("Ring 2");
        c.setZip("10115");
        c.setCity("Berlin");
        c.setCountry("DE");
        c.setMailaddress("k@kunde.example");
        c.setisCustomer(true);
        c.setDateadded(new Date());
        assertTrue(c.save(true));
        Item inv = (Item) DatabaseObject.getObject(Context.getInvoice());
        inv.setContactsids(c.__getIDS());
        inv.setCname("Gemischt");
        inv.setInttype(Item.TYPE_INVOICE);
        inv.setDatetodo(new Date());
        inv.setDateend(new Date());
        inv.setDateadded(new Date());
        inv.setAccountsids(1);
        assertTrue(inv.save(true));
        String[][] pos = {{"Beratung", "100.00", "19"}, {"Buch", "50.00", "7"}, {"Porto", "20.00", "0"}};
        for (int i = 0; i < pos.length; i++) {
            SubItem s = new SubItem();
            s.setItemsids(inv.__getIDS());
            s.setCname(pos[i][0]);
            s.setDescription(pos[i][0]);
            s.setQuantityvalue(BigDecimal.ONE);
            s.setExternalvalue(bd(pos[i][1]));
            s.setInternalvalue(bd(pos[i][1]));
            s.setTaxpercentvalue(bd(pos[i][2]));
            s.setOrdernr(i + 1);
            s.setDateadded(new Date());
            assertTrue(s.save(true));
        }
        mpv5.handler.ItemService.updateTotals(inv);
        Item invoice = (Item) DatabaseObject.getObject(Context.getInvoice(), inv.__getIDS());
        money("170.00", invoice.__getNetvalue(), "invoice net");
        money("22.50", invoice.__getTaxvalue(), "invoice tax");

        //exported as XRechnung and ZUGFeRD: valid, read again with the same amounts
        Path out = tmp.resolve("out");
        Files.createDirectories(out);
        for (ZugpferdFile.Format f : ZugpferdFile.Format.values()) {
            File target = out.resolve(invoice.__getCnumber() + "." + f.getExtension()).toFile();
            ZugpferdFile z = new ZugpferdFile(invoice, target, f);
            Exception e = EInvoiceRoundTripTest.onEdt(() -> z.waitFor());
            assertNull(e, f + ": " + e);
            assertTrue(z.getValidation() != null && z.getValidation().isValid(), f + ": " + z.getValidation());
            mpv5.handler.ZugpferdExpenseImport.ParsedInvoice p = mpv5.handler.ZugpferdExpenseImport.parse(target);
            money("170.00", p.netAmount, f + " net");
            money("192.50", p.grossAmount, f + " gross");
            assertEquals(EInvoiceImport.Outcome.OWN_INVOICE, EInvoiceRoundTripTest.onEdt(() -> EInvoiceImport.importFile(target, null)).outcome, f + " again");
        }

        //paid in two parts
        assertTrue(mpv5.handler.Payments.add(invoice, bd("100.00"), new Date(), "Teil 1"));
        assertTrue(mpv5.handler.Payments.add(invoice, bd("92.50"), new Date(), "Teil 2"));
        assertEquals(Item.STATUS_PAID, ((Item) DatabaseObject.getObject(Context.getInvoice(), invoice.__getIDS())).__getIntstatus());

        //--- 3. the journal by payment date: per rate, income and expenses
        Date now = new Date();
        JournalLoader.Loaded j = JournalLoader.loadAllResult(new vTimeframe(DateConverter.getStartOfYear(now), DateConverter.getEndOfYear(now)), true);
        JournalCalc.Totals t = j.totals;
        System.out.println("INCOME BY RATE " + rates(t.incomeByRate) + " EXPENSES BY RATE " + rates(t.expensesByRate));
        for (Map<BigDecimal, BigDecimal[]> m : List.of(t.incomeByRate, t.expensesByRate)) {
            String what = m == t.incomeByRate ? "income" : "expenses";
            money("100.00", m.get(JournalCalc.rateKey(bd("19")))[0], what + " net 19 %");
            money("19.00", m.get(JournalCalc.rateKey(bd("19")))[1], what + " VAT 19 %");
            money("50.00", m.get(JournalCalc.rateKey(bd("7")))[0], what + " net 7 %");
            money("3.50", m.get(JournalCalc.rateKey(bd("7")))[1], what + " VAT 7 %");
            money("20.00", m.get(JournalCalc.rateKey(BigDecimal.ZERO))[0], what + " net 0 %");
            money("0.00", m.get(JournalCalc.rateKey(BigDecimal.ZERO))[1], what + " VAT 0 %");
        }
        money("170.00", t.incomeNet, "income net");
        money("22.50", t.incomeTax, "income VAT");
        money("170.00", t.expenseNet, "expense net");
        money("22.50", t.expenseTax, "expense VAT");

        //--- 4. DATEV: a line per payment and rate, sums as the journal, the tax by account / BU key
        org.openyabs.datev.DatevConfig config = new org.openyabs.datev.DatevConfig();
        config.beraterNummer = 1001;
        config.mandantenNummer = 10;
        config.prefillFromAccounts(new ArrayList<mpv5.db.objects.Account>(), List.of(bd("19"), bd("7"), BigDecimal.ZERO), true);
        org.openyabs.datev.DatevMapper.Result res = org.openyabs.datev.DatevMapper.map(j.entries, config, DateConverter.getStartOfYear(now), DateConverter.getEndOfYear(now));
        BigDecimal soll = BigDecimal.ZERO;
        BigDecimal haben = BigDecimal.ZERO;
        java.util.Map<String, BigDecimal> byAccount = new java.util.TreeMap<String, BigDecimal>();
        for (org.openyabs.datev.DatevFormat.BookingLine l : res.lines) {
            System.out.println("DATEV " + l.sollHaben + " " + l.umsatz + " " + l.konto + "/" + l.gegenkonto + " BU '" + l.buSchluessel + "' " + l.belegfeld1);
            if ("S".equals(l.sollHaben)) {
                soll = soll.add(l.umsatz);
            } else {
                haben = haben.add(l.umsatz);
            }
            byAccount.merge(l.gegenkonto, l.umsatz, BigDecimal::add);
        }
        money("192.50", soll, "DATEV income");
        money("192.50", haben, "DATEV expenses");
        //gross per account: 19 % 119.00, 7 % 53.50, 0 % 20.00 on each side
        money("119.00", byAccount.get("8400"), "8400 (19 %)");
        money("53.50", byAccount.get("8300"), "8300 (7 %)");
        //0 % without VAT category: a tax-free domestic sale (§ 4 Nr. 8 ff. UStG, SKR03 8100) - not an export
        //(8120, § 4 Nr. 1a UStG: DATEV reports it in field 43 of the VAT return); exports, EU supplies and
        //reverse charge have their own accounts by the category of the rate (G, K, AE)
        money("20.00", byAccount.get("8100"), "8100 (0 %)");
        money("119.00", byAccount.get("3400"), "3400 (19 %)");
        money("53.50", byAccount.get("3300"), "3300 (7 %)");
        money("20.00", byAccount.get("4900"), "4900 (0 %)");
        String csv = org.openyabs.datev.DatevFormat.render(config, DateConverter.getStartOfYear(now), DateConverter.getEndOfYear(now), new Date(), res.lines);
        String[] lines = csv.split("\r\n");
        List<String> heads = DatevWorkflowTest.splitCsv(lines[1]);
        for (int i = 2; i < lines.length; i++) {
            assertEquals(List.of(), io.github.mrtyldr.datev.core.DatevRowValidator.validate(heads, DatevWorkflowTest.splitCsv(lines[i]),
                    io.github.mrtyldr.datev.core.DatevValidationMode.STRICT), lines[i]);
        }
        return invoice;
    }

    private static String rates(Map<BigDecimal, BigDecimal[]> m) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<BigDecimal, BigDecimal[]> e : m.entrySet()) {
            sb.append(e.getKey()).append("%=").append(e.getValue()[0]).append("/").append(e.getValue()[1]).append(" ");
        }
        return sb.toString();
    }
}
