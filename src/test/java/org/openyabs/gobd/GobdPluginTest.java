package org.openyabs.gobd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ServiceLoader;
import mpv5.plugin.api.YabsExtension;
import org.junit.jupiter.api.Test;

/**
 * The GoBD plugin (Java): helpers, e-invoice reader, resources and packaging
 */
public class GobdPluginTest {

    @Test
    public void theHashChainIsSha256OfFileAndPreviousHash() {
        byte[] file = "Rechnung".getBytes(StandardCharsets.UTF_8);
        assertEquals(64, GobdUtil.GENESIS_HASH.length());
        assertTrue(GobdUtil.GENESIS_HASH.matches("0{64}"));
        String first = GobdUtil.chained(file, GobdUtil.GENESIS_HASH);
        //SHA256(bytes || prev) is SHA256 of the concatenation
        byte[] both = ("Rechnung" + GobdUtil.GENESIS_HASH).getBytes(StandardCharsets.UTF_8);
        assertEquals(GobdUtil.sha256(both), first);
        assertNotEquals(first, GobdUtil.chained(file, first));
        //known value
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", GobdUtil.sha256("abc".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void formatsAndEscaping() {
        assertEquals("0.00", GobdUtil.fmtDec(null));
        assertEquals("0.00", GobdUtil.fmtDec("x"));
        assertEquals("12.35", GobdUtil.fmtDec(new BigDecimal("12.345")));
        assertEquals("2026-11-18", GobdUtil.fmtDate("2026-11-18 10:00:00.0"));
        assertEquals("", GobdUtil.fmtDate(null));
        assertEquals("O''Reilly", GobdUtil.esc("O'Reilly"));
        assertEquals("a b \"\"c\"\"", GobdUtil.csvCell("a\r\nb \"c\""));
        assertEquals("&lt;a&gt; &amp; &quot;b&quot; &apos;c&apos;", GobdUtil.escapeXml("<a> & \"b\" 'c'"));
        assertEquals("abcd...", GobdUtil.trunc("abcdefghij", 7));
        assertTrue(GobdUtil.isDraftNumber(""));
        assertTrue(GobdUtil.isDraftNumber("Entwurf 3"));
        assertTrue(GobdUtil.isDraftNumber("draft"));
        assertFalse(GobdUtil.isDraftNumber("RE-2026-00001"));
        assertEquals("RE_2026_1.x", GobdUtil.safeFileName("RE/2026 1.x"));
        assertTrue(GobdUtil.isTrue("1"));
        assertTrue(GobdUtil.isTrue("TRUE"));
        assertFalse(GobdUtil.isTrue(null));
    }

    @Test
    public void readsACiiInvoice() throws Exception {
        String cii = "<rsm:CrossIndustryInvoice xmlns:rsm='urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100'"
                + " xmlns:ram='urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100'"
                + " xmlns:udt='urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100'>"
                + "<rsm:ExchangedDocument><ram:ID>RE-4711</ram:ID><ram:IssueDateTime><udt:DateTimeString format='102'>20261103</udt:DateTimeString></ram:IssueDateTime></rsm:ExchangedDocument>"
                + "<rsm:SupplyChainTradeTransaction><ram:ApplicableHeaderTradeAgreement><ram:SellerTradeParty><ram:Name>Muster GmbH</ram:Name></ram:SellerTradeParty></ram:ApplicableHeaderTradeAgreement>"
                + "<ram:ApplicableHeaderTradeSettlement><ram:ApplicableTradeTax><ram:RateApplicablePercent>19</ram:RateApplicablePercent></ram:ApplicableTradeTax>"
                + "<ram:SpecifiedTradePaymentTerms><ram:DueDateDateTime><udt:DateTimeString format='102'>20261203</udt:DateTimeString></ram:DueDateDateTime></ram:SpecifiedTradePaymentTerms>"
                + "<ram:SpecifiedTradeSettlementHeaderMonetarySummation><ram:TaxBasisTotalAmount>100.00</ram:TaxBasisTotalAmount>"
                + "<ram:GrandTotalAmount>119.00</ram:GrandTotalAmount></ram:SpecifiedTradeSettlementHeaderMonetarySummation>"
                + "</ram:ApplicableHeaderTradeSettlement></rsm:SupplyChainTradeTransaction></rsm:CrossIndustryInvoice>";
        EInvoice.Parsed p = EInvoice.parse(cii.getBytes(StandardCharsets.UTF_8));
        assertTrue(p.valid(), String.valueOf(p.errors));
        assertEquals("RE-4711", p.documentNumber);
        assertEquals("Muster GmbH", p.partnerName);
        assertEquals(new BigDecimal("100.00"), p.netAmount);
        assertEquals(new BigDecimal("119.00"), p.grossAmount);
        assertEquals(new BigDecimal("19.00"), p.taxPercent);
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals("2026-11-03", f.format(p.issueDate));
        assertEquals("2026-12-03", f.format(p.dueDate));
    }

    @Test
    public void readsAUblInvoiceAndComputesTheTaxRate() {
        String ubl = "<Invoice xmlns='urn:oasis:names:specification:ubl:schema:xsd:Invoice-2'"
                + " xmlns:cbc='urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2'"
                + " xmlns:cac='urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2'>"
                + "<cbc:ID>INV-1</cbc:ID><cbc:IssueDate>2026-01-31</cbc:IssueDate>"
                + "<cac:AccountingSupplierParty><cac:Party><cac:PartyName><cbc:Name>Lieferant AG</cbc:Name></cac:PartyName></cac:Party></cac:AccountingSupplierParty>"
                + "<cac:LegalMonetaryTotal><cbc:TaxExclusiveAmount currencyID='EUR'>200</cbc:TaxExclusiveAmount>"
                + "<cbc:TaxInclusiveAmount currencyID='EUR'>214</cbc:TaxInclusiveAmount></cac:LegalMonetaryTotal></Invoice>";
        EInvoice.Parsed p = EInvoice.parse(ubl.getBytes(StandardCharsets.UTF_8));
        assertTrue(p.valid(), String.valueOf(p.errors));
        assertEquals("INV-1", p.documentNumber);
        assertEquals("Lieferant AG", p.partnerName);
        assertEquals(new BigDecimal("7.00"), p.taxPercent);
        //no due date: the issue date
        assertEquals(p.issueDate, p.dueDate);
    }

    @Test
    public void missingFieldsAndBrokenXmlAreErrors() {
        EInvoice.Parsed p = EInvoice.parse("<Invoice/>".getBytes(StandardCharsets.UTF_8));
        assertFalse(p.valid());
        assertEquals(4, p.errors.size());
        assertFalse(EInvoice.parse("not xml".getBytes(StandardCharsets.UTF_8)).valid());
        //no doctype (XXE)
        assertFalse(EInvoice.parse("<!DOCTYPE x [<!ENTITY e SYSTEM 'file:///etc/passwd'>]><Invoice><ID>&e;</ID></Invoice>"
                .getBytes(StandardCharsets.UTF_8)).valid());
        assertNull(EInvoice.parseDecimal(" "));
        assertEquals(new BigDecimal("1.50"), EInvoice.parseDecimal("1,5"));
        assertNull(EInvoice.parseDate("31.02.2026"));
        assertNotNull(EInvoice.parseDate("31.01.2026"));
    }

    @Test
    public void theGdpduFilesAreThere() {
        String index = Audit.indexXml(2026);
        assertTrue(index.startsWith("<?xml"));
        assertTrue(index.contains("<Name>GoBD_Z3_2026</Name>"));
        assertFalse(index.contains("@YEAR@"));
        assertTrue(index.contains("<URL>einnahmen.csv</URL>"));
        assertTrue(Audit.dtd().contains("<!ELEMENT DataSet"));
        assertFalse(Audit.dtd().contains("\"\"\""));
    }

    @Test
    public void thePluginIsFoundAsService() throws Exception {
        //YaBS itself has a services file too (its built-in plugins): look at all of them
        boolean listed = false;
        java.util.Enumeration<java.net.URL> files = GobdCompliance.class.getClassLoader().getResources("META-INF/services/mpv5.plugin.api.YabsExtension");
        while (files.hasMoreElements()) {
            try (InputStream in = files.nextElement().openStream()) {
                listed |= new String(in.readAllBytes(), StandardCharsets.UTF_8).trim().equals(GobdCompliance.class.getName());
            }
        }
        assertTrue(listed, "plugins/gobd/src/main/resources/META-INF/services lists only " + GobdCompliance.class.getName());
        boolean found = false;
        for (YabsExtension e : ServiceLoader.load(YabsExtension.class)) {
            found |= e instanceof GobdCompliance;
        }
        assertTrue(found);
        GobdCompliance g = new GobdCompliance();
        //the id of the Groovy version: settings and archive folder stay
        assertEquals("groovy.gobd-compliance", g.id());
        assertEquals("2.0.10", g.minYabsVersion());
    }

    @Test
    public void onlyInvoicesAreUnderGobd() {
        assertTrue(Gobd.isGobdItemType(mpv5.db.objects.Item.TYPE_INVOICE));
        assertTrue(Gobd.isGobdItemType(mpv5.db.objects.Item.TYPE_CREDIT));
        assertTrue(Gobd.isGobdItemType(mpv5.db.objects.Item.TYPE_DEPOSIT));
        assertTrue(Gobd.isGobdItemType(mpv5.db.objects.Item.TYPE_PART_PAYMENT));
        assertFalse(Gobd.isGobdItemType(mpv5.db.objects.Item.TYPE_OFFER));
        assertFalse(Gobd.isGobdItemType(mpv5.db.objects.Item.TYPE_ORDER));
    }
}
