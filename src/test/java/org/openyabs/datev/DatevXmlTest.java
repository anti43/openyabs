package org.openyabs.datev;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import mpv5.utils.export.VatCategory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The DATEV document package (DATEV XML-Schnittstelle online 6.0) of the
 * DATEV plugin: built like the sample files of DATEV; the rules of the XSD
 * (lengths, patterns, order of the elements) are checked here without the
 * XSD files of DATEV
 */
public class DatevXmlTest {

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    private static Date date(String s) throws Exception {
        return new SimpleDateFormat("yyyy-MM-dd").parse(s);
    }

    private static DatevXml.Doc doc(String number, boolean credit) throws Exception {
        DatevXml.Doc d = new DatevXml.Doc();
        d.number = number;
        d.date = date("2026-03-05");
        d.credit = credit;
        d.customerName = "Kunde & Söhne <AG>";
        d.customerCity = "Musterort";
        d.customerNumber = "K999";
        d.lines.add(new DatevXml.Line(bd("19"), bd("1000.00"), bd("190.00"), "8400"));
        d.lines.add(new DatevXml.Line(bd("7"), bd("100.00"), bd("7.00"), "8300"));
        return d;
    }

    private static Document parse(byte[] xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
    }

    private static List<String> children(Element e) {
        List<String> l = new ArrayList<String>();
        for (org.w3c.dom.Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element) {
                l.add(n.getLocalName());
            }
        }
        return l;
    }

    @Test
    public void ledgerLikeTheSamplesOfDatev() throws Exception {
        Document x = parse(DatevXml.ledger(doc("R-2026-1", false), "").getBytes(StandardCharsets.UTF_8));
        Element root = x.getDocumentElement();
        assertEquals("LedgerImport", root.getLocalName());
        assertEquals(DatevXml.NS_LEDGER, root.getNamespaceURI());
        assertEquals("6.0", root.getAttribute("version"));
        assertEquals(DatevXml.XML_DATA, root.getAttribute("xml_data"));
        Element c = (Element) root.getElementsByTagNameNS(DatevXml.NS_LEDGER, "consolidate").item(0);
        assertEquals("1297.00", c.getAttribute("consolidatedAmount"));
        assertEquals("2026-03-05", c.getAttribute("consolidatedDate"));
        assertEquals("R-2026-1", c.getAttribute("consolidatedInvoiceId"));
        NodeList lines = root.getElementsByTagNameNS(DatevXml.NS_LEDGER, "accountsReceivableLedger");
        assertEquals(2, lines.getLength());
        //the lines in the order of the document; the one of 7 %
        Element l7 = (Element) ("7.00".equals(text((Element) lines.item(0), "tax")) ? lines.item(0) : lines.item(1));
        //the order of the elements of the samples (the XSD is a sequence)
        assertEquals(Arrays.asList("date", "amount", "accountNo", "tax", "currencyCode", "invoiceId", "bookingText",
                "partyId", "customerName", "customerCity"), children(l7));
        assertEquals("107.00", text(l7, "amount"));
        assertEquals("8300", text(l7, "accountNo"));
        assertEquals("7.00", text(l7, "tax"));
        assertEquals("Kunde & Söhne <AG>", text(l7, "customerName"));
    }

    private static String text(Element e, String name) {
        return e.getElementsByTagNameNS(DatevXml.NS_LEDGER, name).item(0).getTextContent();
    }

    @Test
    public void creditNoteIsNegativeAndEuSupplyHasVatIds() throws Exception {
        String own = "DE 123456789";
        DatevXml.Doc g = doc("G-1", true);
        String credit = DatevXml.ledger(g, own);
        assertTrue(credit.contains("consolidatedAmount=\"-1297.00\""));
        assertTrue(credit.contains("<amount>-1190.00</amount>"));
        assertTrue(credit.contains("<amount>-107.00</amount>"));
        assertEquals(bd("-1297.00"), g.gross(), "the overview shows the credit note negative");

        DatevXml.Doc eu = new DatevXml.Doc();
        eu.number = "R-EU";
        eu.date = date("2026-03-06");
        eu.vatId = "FR12345678901";
        eu.customerNumber = "K1";
        DatevXml.Line k = new DatevXml.Line(BigDecimal.ZERO, bd("1000.00"), BigDecimal.ZERO, "4125");
        k.category = VatCategory.K;
        eu.lines.add(k);
        Element l = (Element) parse(DatevXml.ledger(eu, own).getBytes(StandardCharsets.UTF_8)).getDocumentElement()
                .getElementsByTagNameNS(DatevXml.NS_LEDGER, "accountsReceivableLedger").item(0);
        assertEquals("4125", text(l, "accountNo"));
        assertEquals("0.00", text(l, "tax"));
        assertEquals("DE123456789", text(l, "ownVatId"));
        assertEquals("FR12345678901", text(l, "vatId"));
        List<String> order = children(l);
        assertTrue(order.indexOf("bookingText") < order.indexOf("ownVatId") && order.indexOf("ownVatId") < order.indexOf("partyId")
                && order.indexOf("partyId") < order.indexOf("vatId"), order.toString());
    }

    @Test
    public void valuesFollowTheRulesOfTheXsd() throws Exception {
        //invoiceId: [a-zA-Z0-9$%&*+-/]{0,36}
        assertEquals("RE-2026-001", DatevXml.invoiceId("RE 2026.001"));
        assertEquals("R-2026/7", DatevXml.invoiceId("R-2026/7"));
        assertEquals(36, DatevXml.invoiceId("RE 2026.001 mit einem sehr langen Zusatz dahinter").length());
        //vatId: [0-9a-zA-Z. _]{1,15}
        assertEquals("DE123456789", DatevXml.vatId("DE 123-456-789"));
        //accountNo: digits
        assertEquals("", DatevXml.accountNo("84A0"));
        assertEquals("8400", DatevXml.accountNo(" 8400 "));
        DatevXml.Doc d = doc("RE 2026.001", false);
        d.text = "März";
        String l = DatevXml.ledger(d, "");
        assertTrue(l.contains("consolidatedInvoiceId=\"RE-2026-001\""));
        assertTrue(l.contains("<invoiceId>RE-2026-001</invoiceId>"));
        assertTrue(l.contains("<information>RE 2026.001 - März</information>"), "the number as entered");
    }

    @Test
    public void paidAndDeliveryDate() throws Exception {
        DatevXml.Doc d = doc("R-9", false);
        d.paidAt = date("2026-03-20");
        d.deliveryDate = date("2026-03-01");
        Element root = parse(DatevXml.ledger(d, "").getBytes(StandardCharsets.UTF_8)).getDocumentElement();
        Element c = (Element) root.getElementsByTagNameNS(DatevXml.NS_LEDGER, "consolidate").item(0);
        assertEquals("2026-03-01", c.getAttribute("consolidatedDeliveryDate"), "required with deliveryDate in the lines");
        Element l = (Element) root.getElementsByTagNameNS(DatevXml.NS_LEDGER, "accountsReceivableLedger").item(0);
        assertEquals("2026-03-20", text(l, "paidAt"));
        assertEquals("2026-03-01", text(l, "deliveryDate"));
        List<String> order = children(l);
        assertTrue(order.indexOf("partyId") < order.indexOf("paidAt") && order.indexOf("deliveryDate") < order.indexOf("customerName"), order.toString());
        assertTrue(text(l, "bookingText").length() <= 30);
        assertEquals("", DatevXml.accountNo("123"), "4 to 8 digits");
        assertEquals("", DatevXml.accountNo("123456789"));
    }

    @Test
    public void accountsAsInTheBookingBatch() {
        DatevConfig c = new DatevConfig();
        c.kontenrahmen = "04";
        c.revenueAccounts.put(bd("19"), "4401");
        c.revenueAccounts.put(bd("7"), "4300");
        c.euDeliveryAccount = "4126";
        c.euServiceAccount = "4336";
        c.reverseChargeAccount = "4337";
        c.exportAccount = "4120";
        assertEquals("4401", DatevMapper.booking(bd("19"), false, null, null, "", c, null).account);
        assertEquals("4300", DatevMapper.booking(bd("7"), false, null, null, "", c, null).account);
        assertEquals("4126", DatevMapper.booking(BigDecimal.ZERO, false, null, VatCategory.K, "FR12345678901", c, null).account);
        assertEquals("4336", DatevMapper.booking(BigDecimal.ZERO, false, null, VatCategory.AE, "FR12345678901", c, null).account);
        assertEquals("4337", DatevMapper.booking(BigDecimal.ZERO, false, null, VatCategory.AE, "", c, null).account, "§ 13b UStG in Germany");
        assertEquals("4120", DatevMapper.booking(BigDecimal.ZERO, false, null, VatCategory.G, "", c, null).account);
        DatevMapper.Booking guess = DatevMapper.booking(BigDecimal.ZERO, false, null, null, "FR12345678901", c, null);
        assertEquals("4126", guess.account);
        assertTrue(guess.goodsAssumed, "no category: a supply of goods is assumed, and said");
        //the automatic account of 19 % needs no BU key; another account takes the BU key of the settings
        c.useAutomaticAccounts = true;
        c.revenueAccounts.put(bd("19"), "4400");
        assertEquals("", DatevMapper.booking(bd("19"), false, null, null, "", c, null).bu);
        c.useAutomaticAccounts = false;
        assertEquals("3", DatevMapper.booking(bd("19"), false, null, null, "", c, null).bu);
    }

    @Test
    public void zeroPercentWithoutCategoryIsATaxFreeDomesticSale() {
        //§ 4 Nr. 8 ff. UStG: SKR03 8100 / SKR04 4100 - not the export account 8120 / 4120 (§ 4 Nr. 1a UStG, field 43)
        for (String[] k : new String[][]{{"03", "false", "8100"}, {"04", "false", "4100"}, {"03", "true", "8195"}, {"04", "true", "4185"}}) {
            DatevConfig c = new DatevConfig();
            c.kontenrahmen = k[0];
            c.smallBusiness = Boolean.parseBoolean(k[1]);
            c.prefillFromAccounts(Collections.emptyList(), Collections.singletonList(BigDecimal.ZERO), true);
            assertEquals(k[2], c.revenueAccounts.get(mpv5.handler.JournalCalc.rateKey(BigDecimal.ZERO)), "SKR" + k[0] + ", § 19: " + k[1]);
            DatevMapper.Booking b = DatevMapper.booking(BigDecimal.ZERO, false, null, null, "", c, null);
            assertEquals(k[2], b.account);
            assertEquals(!c.smallBusiness, b.uncategorizedZero, "the dialog says how 0 % without category is booked");
            //the categories keep their own accounts
            assertEquals("03".equals(k[0]) ? "8120" : "4120", DatevMapper.booking(BigDecimal.ZERO, false, null, VatCategory.G, "", c, null).account);
            assertEquals("03".equals(k[0]) ? "8125" : "4125",
                    DatevMapper.booking(BigDecimal.ZERO, false, null, VatCategory.K, "FR12345678901", c, null).account);
        }
    }

    @Test
    public void buCodeIsWritten() throws Exception {
        DatevXml.Doc d = doc("R-BU", false);
        d.lines.get(0).bu = "3";
        Element l = (Element) parse(DatevXml.ledger(d, "").getBytes(StandardCharsets.UTF_8)).getDocumentElement()
                .getElementsByTagNameNS(DatevXml.NS_LEDGER, "accountsReceivableLedger").item(0);
        assertEquals("3", text(l, "buCode"));
        List<String> order = children(l);
        assertEquals(order.indexOf("accountNo") + 1, order.indexOf("buCode"), "buCode right after accountNo (XSD)");
        //without an account no BU key
        d.lines.get(0).account = "";
        assertFalse(DatevXml.ledger(d, "").contains("<buCode>3</buCode>"));
    }

    @Test
    public void consolidatedAmountIsTheSumOfTheLinesAsWritten() throws Exception {
        DatevXml.Doc d = new DatevXml.Doc();
        d.number = "R-RND";
        d.date = date("2026-03-01");
        //three parts with a third of a cent each: written rounded, the sum is the sum of the written lines
        d.lines.add(new DatevXml.Line(bd("19"), bd("10.004"), bd("1.901"), "8400"));
        d.lines.add(new DatevXml.Line(bd("7"), bd("10.004"), bd("0.700"), "8300"));
        String l = DatevXml.ledger(d, "");
        assertTrue(l.contains("<amount>11.91</amount>"), l);
        assertTrue(l.contains("<amount>10.70</amount>"), l);
        assertTrue(l.contains("consolidatedAmount=\"22.61\""), l);
        //a line of 0,00 is left out (DATEV: no empty lines)
        d.lines.add(new DatevXml.Line(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "8100"));
        assertEquals(2, l.split("<accountsReceivableLedger>").length - 1);
        assertEquals(2, DatevXml.ledger(d, "").split("<accountsReceivableLedger>").length - 1);
    }

    @Test
    public void packageWithDocumentXml() throws Exception {
        DatevXml.Doc d1 = doc("R-1", false);
        d1.files.put(DatevXml.imageName(d1) + ".pdf", "%PDF-1.4".getBytes(StandardCharsets.US_ASCII));
        DatevXml.Doc d2 = doc("G/1", true);
        DatevXml.Doc d3 = doc("R-3", false);
        d3.files.put(DatevXml.imageName(d3) + ".xml", "<Invoice/>".getBytes(StandardCharsets.UTF_8));
        List<DatevXml.Doc> docs = Arrays.asList(d1, d2, d3);
        byte[] zip = DatevXml.zip(docs, "", date("2026-04-01"), "Rechnungsausgang 2026");
        Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
        try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry e;
            while ((e = z.getNextEntry()) != null) {
                files.put(e.getName(), z.readAllBytes());
            }
        }
        assertEquals(Arrays.asList("document.xml", "Rechnungsdaten_RA_R-1.xml", "Rechnungsbild_RA_R-1.pdf",
                "Rechnungskorrekturdaten_RAK_G_1.xml", "Rechnungsdaten_RA_R-3.xml", "Rechnungsbild_RA_R-3.xml"),
                new ArrayList<String>(files.keySet()));
        for (String n : files.keySet()) {
            assertTrue(n.matches("[A-Za-z0-9_.\\-]+"), "ASCII file names: " + n);
        }
        Element root = parse(files.get("document.xml")).getDocumentElement();
        assertEquals("archive", root.getLocalName());
        assertEquals(DatevXml.NS_DOCUMENT, root.getNamespaceURI());
        assertEquals(Arrays.asList("header", "content"), children(root));
        NodeList docsXml = root.getElementsByTagNameNS(DatevXml.NS_DOCUMENT, "document");
        assertEquals(3, docsXml.getLength());
        String dx = new String(files.get("document.xml"), StandardCharsets.UTF_8);
        assertTrue(dx.contains("xsi:type=\"accountsReceivableLedger\" datafile=\"Rechnungsdaten_RA_R-1.xml\""));
        assertTrue(dx.contains("<property value=\"2026-03\" key=\"1\"/>"));
        assertTrue(dx.contains("<property value=\"Rechnungsausgang 2026\" key=\"3\"/>"), "the folder of the settings");
        assertTrue(dx.contains("<extension xsi:type=\"File\" name=\"Rechnungsbild_RA_R-1.pdf\"/>"));
        assertTrue(dx.contains("<extension xsi:type=\"File\" name=\"Rechnungsbild_RA_R-3.xml\"/>"), "XML is a permitted file type");
        //empty folder: DATEV's default
        String dflt = DatevXml.documentXml(Collections.singletonList(d2), Collections.singletonMap(d2, "x.xml"), date("2026-04-01"), " ");
        assertTrue(dflt.contains("<property value=\"Ausgangsrechnungen\" key=\"3\"/>"));
    }

    @Test
    public void onlyTheCharactersDatevTakes() throws Exception {
        assertEquals("\"Beratung\" - Teil 1...", DatevXml.clean("\u201eBeratung\u201c \u2013 Teil 1\u2026"));
        assertEquals("Müller's Café € §", DatevXml.clean("Mu\u0308ller\u2019s Cafe\u0301 € §"), "precomposed (NFC)");
        assertEquals("Rechnung ", DatevXml.clean("Rechnung \ud83d\ude00"), "no emoji");
        assertEquals("*a 1 000", DatevXml.clean("\u2022a 1\u202f000"));
        DatevXml.Doc d = doc("R-1", false);
        d.customerName = "O\u2019Brien \u201eTest\u201c";
        String l = DatevXml.ledger(d, "");
        assertTrue(l.contains("<customerName>O&apos;Brien &quot;Test&quot;</customerName>"), l);
        //everything in the files matches the expression of DATEV
        java.util.regex.Pattern datev = java.util.regex.Pattern.compile(
                "^[a-zA-Z0-9\u00a7@\u20ac\\t\\r\\n#|`\u00b4'^\u00b0%&*~:;.\\-/+=_ ?$!{}\\[\\],()<>\\\\\"\\x{0080}-\\x{00FF}\\x{4E00}-\\x{9FFF}\\x{AC00}-\\x{D7AF}]*$");
        assertTrue(datev.matcher(l).matches());
    }
}
