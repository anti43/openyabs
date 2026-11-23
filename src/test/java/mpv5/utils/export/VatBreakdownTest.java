package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.odt.ODTConstants;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import mpv5.db.objects.SubItem;
import mpv5.utils.numberformat.InvoiceMath;
import org.junit.jupiter.api.Test;
import org.mustangproject.Invoice;
import org.mustangproject.ZUGFeRD.TransactionCalculator;
import org.mustangproject.ZUGFeRD.VATAmount;

/**
 * The VAT breakdown per rate (issue #168, § 14 Abs. 4 Nr. 8 UStG): the
 * calculation, the template fields and the bundled templates
 */
class VatBreakdownTest {

    private static InvoiceMath.Line l(String qty, String price, String discount, String tax) {
        return new InvoiceMath.Line(new BigDecimal(qty), new BigDecimal(price), new BigDecimal(discount), new BigDecimal(tax));
    }

    private static Map<String, Object> fields(List<InvoiceMath.Line> lines) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        java.lang.reflect.Method m = mpv5.db.objects.Item.class.getDeclaredMethod("putVatGroups", Map.class, List.class);
        m.setAccessible(true);
        m.invoke(null, map, lines);
        return map;
    }

    @Test
    void oneRateIsOneGroup() throws Exception {
        List<InvoiceMath.RateTotal> g = InvoiceMath.byRateDescending(Arrays.asList(l("2", "50", "0", "19"), l("1", "10", "0", "19.00")));
        assertEquals(1, g.size());
        assertEquals(0, new BigDecimal("110.00").compareTo(g.get(0).net));
        assertEquals(0, new BigDecimal("20.90").compareTo(g.get(0).tax));
        assertEquals(0, new BigDecimal("130.90").compareTo(g.get(0).gross));
        Map<String, Object> f = fields(Arrays.asList(l("2", "50", "0", "19")));
        assertEquals(1, f.get("vatgroupcount"));
        assertEquals("false", f.get("hasmultipletaxrates"));
        assertEquals("19", f.get("vatgroup1.ratevalue"));
        assertEquals(1, ((List<?>) f.get("vatgroups")).size());
    }

    @Test
    void mixedRatesAreSortedHighestFirst() throws Exception {
        List<InvoiceMath.Line> lines = Arrays.asList(l("1", "200", "0", "7"), l("1", "50", "0", "0"), l("2", "500", "0", "19"));
        List<InvoiceMath.RateTotal> g = InvoiceMath.byRateDescending(lines);
        assertEquals(3, g.size());
        assertEquals(0, new BigDecimal("19").compareTo(g.get(0).rate));
        assertEquals(0, new BigDecimal("1000.00").compareTo(g.get(0).net));
        assertEquals(0, new BigDecimal("190.00").compareTo(g.get(0).tax));
        assertEquals(0, new BigDecimal("7").compareTo(g.get(1).rate));
        assertEquals(0, new BigDecimal("14.00").compareTo(g.get(1).tax));
        assertEquals(0, new BigDecimal("214.00").compareTo(g.get(1).gross));
        assertEquals(0, BigDecimal.ZERO.compareTo(g.get(2).rate));
        assertEquals(0, BigDecimal.ZERO.compareTo(g.get(2).tax));
        //the groups add up to the totals of the document
        InvoiceMath.Totals t = InvoiceMath.totals(lines);
        BigDecimal net = BigDecimal.ZERO, tax = BigDecimal.ZERO;
        for (InvoiceMath.RateTotal r : g) {
            net = net.add(r.net);
            tax = tax.add(r.tax);
        }
        assertEquals(0, t.net.compareTo(net));
        assertEquals(0, t.tax.compareTo(tax));
        Map<String, Object> f = fields(lines);
        assertEquals(3, f.get("vatgroupcount"));
        assertEquals("true", f.get("hasmultipletaxrates"));
        assertEquals("19", f.get("vatgroup1.ratevalue"));
        assertEquals("7", f.get("vatgroup2.ratevalue"));
        assertEquals("0", f.get("vatgroup3.ratevalue"));
        for (String k : mpv5.db.objects.Item.VATGROUP_FIELDS) {
            assertTrue(f.containsKey("vatgroup1." + k), k);
        }
    }

    @Test
    void taxIsCalculatedOnTheNetOfTheRate() {
        //3 x 10.01 at 19 %: 30.03 x 0.19 = 5.7057 -> 5.71, not 3 x 1.90 = 5.70
        List<InvoiceMath.RateTotal> g = InvoiceMath.byRateDescending(Arrays.asList(l("1", "10.01", "0", "19"), l("1", "10.01", "0", "19"), l("1", "10.01", "0", "19")));
        assertEquals(0, new BigDecimal("30.03").compareTo(g.get(0).net));
        assertEquals(0, new BigDecimal("5.71").compareTo(g.get(0).tax));
    }

    @Test
    void lineDiscountsReduceTheNetOfTheirRate() {
        //10 % discount on the 19 % line, none on the 7 % line
        List<InvoiceMath.RateTotal> g = InvoiceMath.byRateDescending(Arrays.asList(l("1", "100", "10", "19"), l("1", "100", "0", "7")));
        assertEquals(0, new BigDecimal("90.00").compareTo(g.get(0).net));
        assertEquals(0, new BigDecimal("17.10").compareTo(g.get(0).tax));
        assertEquals(0, new BigDecimal("100.00").compareTo(g.get(1).net));
        assertEquals(0, new BigDecimal("7.00").compareTo(g.get(1).tax));
    }

    @Test
    void groupsMatchTheEInvoice() throws Exception {
        //the same values as ApplicableTradeTax of the ZUGFeRD / XRechnung XML, rate by rate
        String[][] data = {{"3", "10.01", "0", "19"}, {"2", "4.99", "5", "7"}, {"1.5", "33.33", "0", "19"}, {"1", "12", "0", "0"}, {"7", "0.333", "12.5", "7"}};
        List<InvoiceMath.Line> lines = new ArrayList<InvoiceMath.Line>();
        List<SubItem> subs = new ArrayList<SubItem>();
        for (int i = 0; i < data.length; i++) {
            String[] d = data[i];
            lines.add(l(d[0], d[1], d[2], d[3]));
            subs.add(ZugpferdFileTest.line("L" + i, "", d[0], "Stk", d[1], d[3], d[2]));
        }
        Map<String, String> c = ZugpferdFileTest.company();
        Invoice inv = ZugpferdFile.createInvoice(ZugpferdFileTest.invoice(), subs.toArray(new SubItem[0]),
                ZugpferdFile.createSender(c::get), ZugpferdFileTest.buyer(), "ref", 0, null);
        Map<BigDecimal, VATAmount> xml = new HashMap<BigDecimal, VATAmount>();
        for (VATAmount v : new TransactionCalculator(inv).getTaxDetails()) {
            xml.put(v.getApplicablePercent().stripTrailingZeros(), v);
        }
        List<InvoiceMath.RateTotal> g = InvoiceMath.byRateDescending(lines);
        assertEquals(xml.size(), g.size());
        for (InvoiceMath.RateTotal r : g) {
            VATAmount v = xml.get(r.rate.stripTrailingZeros());
            assertTrue(v != null, "rate " + r.rate);
            assertEquals(0, InvoiceMath.round2(v.getBasis()).compareTo(r.net), "net " + r.rate);
            assertEquals(0, InvoiceMath.round2(v.getCalculated()).compareTo(r.tax), "tax " + r.rate);
        }
    }

    @Test
    void theDocumentsListIsTakenFromTheTemplateData() {
        Map<String, Object> data = new HashMap<String, Object>();
        List<String> own = Arrays.asList("a");
        data.put("item.vatgroups", own);
        data.put("item.reference.vatgroups", Arrays.asList("b"));
        data.put("item.cname", "x");
        assertEquals(own, ODTFile2.vatGroups(data));
        assertFalse(data.containsKey("item.vatgroups"));
        assertFalse(data.containsKey("item.reference.vatgroups"));
        assertTrue(data.containsKey("item.cname"));
        assertTrue(ODTFile2.vatGroups(new HashMap<String, Object>()).isEmpty());
    }

    /**
     * Fills a bundled template as ODTFile2 does (tables and VAT groups as lists)
     *
     * @return content.xml of the result
     */
    private static String render(String resource, Map<String, Object> vat, boolean withVatList) throws Exception {
        IXDocReport report;
        try (InputStream in = VatBreakdownTest.class.getResourceAsStream(resource)) {
            report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Velocity);
        }
        report.removePreprocessor(ODTConstants.CONTENT_XML_ENTRY);
        report.addPreprocessor(ODTConstants.CONTENT_XML_ENTRY, mpv5.utils.xdocreport.YabsODTPreprocessor.INSTANCE);
        FieldsMetadata md = new FieldsMetadata();
        for (int t = 1; t <= 2; t++) {
            for (int j = 0; j < 10; j++) {
                md.addFieldAsList(ODTFile2.KEY_TABLE + t + ".C" + j);
            }
        }
        for (String f : mpv5.db.objects.Item.VATGROUP_FIELDS) {
            md.addFieldAsList(ODTFile2.KEY_VATGROUPS + "." + f);
        }
        report.setFieldsMetadata(md);
        IContext ctx = report.createContext();
        List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        Map<String, String> row = new HashMap<String, String>();
        String[] cells = {"1", "3", "Beratung", "10,01", "19,00%", "30,03"};
        for (int j = 0; j < cells.length; j++) {
            row.put("C" + j, cells[j]);
        }
        rows.add(row);
        ctx.put("xtable1", rows);
        ctx.put("xtable2", new ArrayList<Map<String, String>>());
        if (withVatList) {
            ctx.put(ODTFile2.KEY_VATGROUPS, ODTFile2.vatGroups(new HashMap<String, Object>(Map.of("item.vatgroups", vat.get("vatgroups")))));
        }
        ctx.put("item.netvaluef", "NET-TOTAL");
        ctx.put("item.taxvaluef", "TAX-TOTAL");
        ctx.put("item.grosvaluef", "GROSS-TOTAL");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        report.process(ctx, out);
        try (ZipInputStream z = new ZipInputStream(new java.io.ByteArrayInputStream(out.toByteArray()))) {
            for (ZipEntry e; (e = z.getNextEntry()) != null;) {
                if (e.getName().equals("content.xml")) {
                    String x = new String(z.readAllBytes(), StandardCharsets.UTF_8).replaceAll("<[^>]+>", "|").replaceAll("\\|+", "|");
                    //characters written as references (the euro sign as &#8364;)
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("&#(\\d+);").matcher(x);
                    StringBuffer b = new StringBuffer();
                    while (m.find()) {
                        m.appendReplacement(b, java.util.regex.Matcher.quoteReplacement(new String(Character.toChars(Integer.parseInt(m.group(1))))));
                    }
                    m.appendTail(b);
                    return b.toString().replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'");
                }
            }
        }
        throw new AssertionError("no content.xml");
    }

    @Test
    void theBundledTemplatesShowTheRateOfEachPositionAndOneRowPerRate() throws Exception {
        Map<String, Object> vat = fields(Arrays.asList(l("3", "10.01", "0", "19"), l("2", "5", "0", "7")));
        for (String t : new String[]{"/extra/templates/invoice.odt", "/extra/templates/demo-00.odt", "/extra/templates/demo-20.odt"}) {
            String c = render(t, vat, true);
            //the position table: the rate before the total
            assertTrue(c.contains("USt."), t);
            assertTrue(c.contains("10,01|19,00%|30,03"), t + ": " + c.substring(Math.max(0, c.indexOf("Beratung") - 50), Math.min(c.length(), c.indexOf("Beratung") + 200)));
            //the VAT table: one row per rate, highest first, then the sum
            String r1 = vat.get("vatgroup1.name") + "|" + vat.get("vatgroup1.netf") + "|" + vat.get("vatgroup1.vatf") + "|" + vat.get("vatgroup1.grossf");
            String r2 = vat.get("vatgroup2.name") + "|" + vat.get("vatgroup2.netf") + "|" + vat.get("vatgroup2.vatf") + "|" + vat.get("vatgroup2.grossf");
            String plain = c;
            assertTrue(plain.contains("Steuersatz|Netto|MwSt.|Brutto|" + r1 + "|" + r2 + "|Gesamt|NET-TOTAL|TAX-TOTAL|GROSS-TOTAL"), t + ": " + plain);
        }
    }

    @Test
    void invoicesShowTheServiceDate() throws Exception {
        //§ 14 Abs. 4 Nr. 6 UStG: the date of the service on every invoice (#208)
        for (String t : new String[]{"/extra/templates/invoice.odt", "/extra/templates/demo-00.odt", "/extra/templates/demo-00-ku.odt",
            "/extra/templates/demo-20.odt", "/extra/templates/demo-21.odt", "/extra/templates/demo-22.odt"}) {
            String c = content(t);
            assertTrue(c.contains(">Leistungsdatum<"), t);
            assertTrue(c.contains("&lt;item.servicedate&gt;"), t);
        }
        //not on an offer
        assertFalse(content("/extra/templates/demo-02.odt").contains(">Leistungsdatum<"));
    }

    @Test
    void invoiceAndDemoTemplatesContainEoriFields() throws Exception {
        for (String t : new String[]{"/extra/templates/invoice.odt", "/extra/templates/demo-00.odt", "/extra/templates/demo-00-ku.odt",
            "/extra/templates/demo-20.odt", "/extra/templates/demo-21.odt", "/extra/templates/demo-22.odt"}) {
            String c = content(t);
            assertTrue(c.contains("&lt;companyinfo.eori&gt;"), t + " missing companyinfo.eori");
            assertTrue(c.contains("&lt;item.contact.eori&gt;"), t + " missing item.contact.eori");
        }
        String contactSheet = content("/extra/templates/demo-08.odt");
        assertTrue(contactSheet.contains("&lt;contact.eori&gt;"), "demo-08.odt missing contact.eori");
        assertTrue(contactSheet.contains("&lt;contact.peppolid&gt;"), "demo-08.odt missing contact.peppolid");
    }

    private static String content(String resource) throws Exception {
        try (ZipInputStream z = new ZipInputStream(VatBreakdownTest.class.getResourceAsStream(resource))) {
            for (ZipEntry e; (e = z.getNextEntry()) != null;) {
                if (e.getName().equals("content.xml")) {
                    return new String(z.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        throw new AssertionError("no content.xml in " + resource);
    }

    @Test
    void theSmallBusinessInvoiceShowsNoVat() throws Exception {
        //Kleinunternehmer (§ 19 UStG): no VAT column, no VAT table, no VAT total - but the note and the QR code
        String c = render(mpv5.handler.FirstStartSetup.SMALL_BUSINESS_INVOICE_TEMPLATE,
                fields(Arrays.asList(l("1", "100", "0", "0"))), true);
        //the invoice page only: the page listing all fields at the end has the VAT fields as well
        assertTrue(c.contains("Verf\u00fcgbare Felder"), c);
        c = c.substring(0, c.indexOf("Verf\u00fcgbare Felder"));
        assertFalse(c.contains("USt."), c);
        assertFalse(c.contains("Steuersatz"), c);
        assertFalse(c.contains("MwSt"), c);
        assertFalse(c.contains("TAX-TOTAL"), c);
        assertTrue(c.contains("Summe|NET-TOTAL"), c);
        assertTrue(c.contains("Rechnungsbetrag|GROSS-TOTAL"), c);
        String raw;
        try (ZipInputStream z = new ZipInputStream(getClass().getResourceAsStream(mpv5.handler.FirstStartSetup.SMALL_BUSINESS_INVOICE_TEMPLATE))) {
            StringBuilder b = new StringBuilder();
            for (ZipEntry e; (e = z.getNextEntry()) != null;) {
                b.append(e.getName()).append('\n');
                if (e.getName().equals("content.xml")) {
                    b.append(new String(z.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
            raw = b.toString();
        }
        assertTrue(raw.contains("&lt;companyinfo.smallbusinessnote&gt;"), "the note of the exemption");
        assertTrue(raw.contains("Pictures/QRCode.png"), "the EPC QR code");
    }

    @Test
    void aTemplateWithoutTheVatTableIsUnchanged() throws Exception {
        //the purchase order keeps its 5 columns and has no VAT table
        String c = render("/extra/templates/demo-10.odt", fields(Arrays.asList(l("1", "1", "0", "19"))), true);
        assertFalse(c.contains("Steuersatz"));
        assertFalse(c.contains("USt."));
    }
}
