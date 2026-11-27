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
package org.openyabs.handwerk;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import javax.xml.parsers.DocumentBuilderFactory;
import mpv5.db.objects.Item;
import mpv5.plugin.ExtensionManager;
import mpv5.plugin.api.YabsExtension;
import org.junit.jupiter.api.Test;
import org.openyabs.handwerk.BoqDocument.BoqPosition;
import org.openyabs.handwerk.BoqDocument.PositionType;
import org.openyabs.handwerk.DatanormImporter.Article;
import org.openyabs.handwerk.DatanormImporter.ParseResult;
import org.openyabs.handwerk.HandwerkStore.ContactMeta;
import org.openyabs.handwerk.HandwerkStore.ItemMeta;
import org.openyabs.handwerk.HandwerkStore.LineCostType;
import org.openyabs.handwerk.HandwerkStore.Measurement;
import org.openyabs.handwerk.HandwerkStore.Settings;
import org.openyabs.handwerk.HandwerkStore.Site;
import org.openyabs.handwerk.HandwerkStore.TimeSheet;
import org.openyabs.handwerk.HandwerkValidator.LaborSplitSummary;
import org.openyabs.handwerk.HandwerkValidator.LineInput;
import org.openyabs.handwerk.HandwerkValidator.RetentionResult;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit and integration tests for the Skilled Trades &amp;
 * Construction / Handwerksbetriebe plugin ({@link HandwerkPlugin}).
 */
class HandwerkPluginTest {

    @Test
    void serviceLoaderFindsHandwerkPluginAndAllResources() throws Exception {
        boolean found = false;
        for (YabsExtension ext : ServiceLoader.load(YabsExtension.class)) {
            if (HandwerkPlugin.ID.equals(ext.id())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "HandwerkPlugin must be discoverable via ServiceLoader");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        for (String res : HandwerkPlugin.TEMPLATE_RESOURCES) {
            try (InputStream in = HandwerkPlugin.class.getResourceAsStream(res)) {
                assertNotNull(in, "Missing bundled template resource: " + res);
                byte[] bytes = in.readAllBytes();
                String xml = new String(bytes, StandardCharsets.UTF_8);
                assertTrue(xml.contains("<office:document"), "Valid FODT root in " + res);
                assertNotNull(dbf.newDocumentBuilder().parse(new ByteArrayInputStream(bytes)),
                        "FODT must parse cleanly as XML: " + res);
            }
        }

        Properties en = loadUtf8Props(HandwerkPlugin.I18N_EN);
        Properties de = loadUtf8Props(HandwerkPlugin.I18N_DE);
        assertEquals(en.stringPropertyNames(), de.stringPropertyNames(),
                "English and German translation bundles must have identical keys");
        for (String key : en.stringPropertyNames()) {
            String ev = en.getProperty(key);
            String dv = de.getProperty(key);
            assertFalse(ev.trim().isEmpty(), "Empty EN translation for " + key);
            assertFalse(dv.trim().isEmpty(), "Empty DE translation for " + key);
            java.text.MessageFormat ef = new java.text.MessageFormat(ev);
            java.text.MessageFormat df = new java.text.MessageFormat(dv);
            assertEquals(ef.getFormatsByArgumentIndex().length, df.getFormatsByArgumentIndex().length,
                    "Mismatched MessageFormat argument count for key: " + key);
        }
        assertTrue(de.getProperty("plugin.name").contains("Handwerk"));
        assertTrue(en.getProperty("plugin.name").contains("Skilled Trades"));
    }

    @Test
    void headlessExtensionManagerLifecycle() throws Exception {
        File tmpDir = Files.createTempDirectory("yabs-handwerk-test").toFile();
        ExtensionManager mgr = new ExtensionManager(tmpDir, null);
        HandwerkPlugin plugin = new HandwerkPlugin();
        ExtensionManager.Installed inst = mgr.register(plugin);

        assertTrue(mgr.start(inst), "HandwerkPlugin must start cleanly without a database");
        assertEquals(5, mgr.commands().size(), "Expected 5 registered commands (Setup + 4 operational)");
        assertTrue(mgr.commands().get(0).title.endsWith(": Setup"),
                "First command must be Setup: " + mgr.commands().get(0).title);
        assertEquals(1, mgr.pages().size(), "Expected 1 settings page");
        assertEquals(1, mgr.privacyContributors().size(), "Expected 1 privacy contributor");
        assertEquals("Handwerksbetriebe & Bau",
                mgr.translateForLocale(HandwerkPlugin.ID, "de_DE", "plugin.name", null));
        assertEquals("Skilled Trades & Construction",
                mgr.translateForLocale(HandwerkPlugin.ID, "en", "plugin.name", null));

        mgr.stop(inst);
        assertTrue(mgr.commands().isEmpty(), "Commands must be cleaned up on stop");
        assertTrue(mgr.pages().isEmpty(), "Settings pages must be cleaned up on stop");
        assertTrue(mgr.privacyContributors().isEmpty(), "Privacy contributors must be cleaned up on stop");
    }

    @Test
    void h2SchemaAndFullPersistenceRoundTrip() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:handwerk_test;DB_CLOSE_DELAY=-1", "sa", "")) {
            HandwerkStore.initSchema(conn);
            HandwerkStore.initSchema(conn); // idempotent
            assertEquals(HandwerkStore.SCHEMA_VERSION, HandwerkStore.getSchemaVersion(conn));

            // ContactMeta
            ContactMeta cm = new ContactMeta();
            cm.contactId = 101;
            cm.customerKind = HandwerkStore.CUSTOMER_CONSTRUCTION_13B;
            cm.reverseChargeCertNo = "UST1TG-2026-001";
            cm.reverseChargeCertUntil = "2028-12-31";
            cm.withholdingExemptionTaxNo = "123/456/78901";
            cm.withholdingExemptionSecNo = "SEC-9988";
            cm.withholdingExemptionUntil = "2027-06-30";
            HandwerkStore.saveContactMeta(conn, cm);

            ContactMeta loadedCm = HandwerkStore.loadContactMeta(conn, 101);
            assertEquals(HandwerkStore.CUSTOMER_CONSTRUCTION_13B, loadedCm.customerKind);
            assertEquals("UST1TG-2026-001", loadedCm.reverseChargeCertNo);
            assertEquals("2028-12-31", loadedCm.reverseChargeCertUntil);

            // Sites (1-to-many)
            Site s1 = new Site();
            s1.contactId = 101;
            s1.projectNo = "BV-2026-01";
            s1.siteName = "Neubau Kita Nord";
            s1.street = "Hauptstr. 12";
            s1.zip = "10115";
            s1.city = "Berlin";
            s1.contractType = HandwerkStore.CONTRACT_VOB_B;
            s1.retentionPercent = new BigDecimal("5.00");
            s1.acceptanceDate = "2026-10-01";
            s1.warrantyYears = 4;
            s1.warrantyUntil = HandwerkValidator.computeWarrantyUntil(s1.acceptanceDate, s1.contractType, s1.warrantyYears);
            s1.maintenanceDue = "2027-10-01";
            HandwerkStore.saveSite(conn, s1);
            assertTrue(s1.id > 0);
            assertEquals("2030-10-01", s1.warrantyUntil);

            Site s2 = new Site();
            s2.contactId = 101;
            s2.projectNo = "BV-2024-99";
            s2.siteName = "Altbau West";
            s2.archived = true;
            HandwerkStore.saveSite(conn, s2);

            assertEquals(1, HandwerkStore.loadSitesForContact(conn, 101, false).size());
            assertEquals(2, HandwerkStore.loadSitesForContact(conn, 101, true).size());
            assertEquals(1, HandwerkStore.searchSites(conn, "Kita").size());

            // ItemMeta + LineCostType
            ItemMeta im = new ItemMeta();
            im.itemId = 500;
            im.siteId = s1.id;
            im.siteSnapshot = s1.displayLabel();
            im.reverseChargeEnabled = true;
            im.retentionPercent = new BigDecimal("5.00");
            im.retentionBasis = HandwerkStore.BASIS_NET;
            im.retentionAmount = new BigDecimal("500.00");
            im.retentionReleaseDate = "2030-10-01";
            HandwerkStore.saveItemMeta(conn, im);
            assertTrue(HandwerkStore.hasItemMeta(conn, 500));
            assertEquals(1, HandwerkStore.loadAllItemMetasWithRetention(conn).size());

            LineCostType lct = new LineCostType();
            lct.itemId = 500;
            lct.subItemId = 9001;
            lct.costType = HandwerkStore.COST_LABOR;
            lct.laborSharePercent = new BigDecimal("70.00");
            HandwerkStore.saveLineCostType(conn, lct);
            Map<Integer, LineCostType> lctMap = HandwerkStore.loadLineCostTypes(conn, 500);
            assertEquals(1, lctMap.size());
            assertEquals(0, new BigDecimal("70.00").compareTo(lctMap.get(9001).laborSharePercent));

            // BoQ Document + Positions
            BoqDocument boq = new BoqDocument();
            boq.itemId = 500;
            boq.formatCode = "GAEB_XML_33";
            boq.exchangePhase = "83";
            boq.projectName = "Kita Nord";
            boq.boqLabel = "Elektro";
            BoqPosition pos = new BoqPosition();
            pos.ordinalNo = "01.01.0010";
            pos.shortText = "NYM-J 3x1,5 verlegen";
            pos.longText = "Liefern und verlegen von Mantelleitung NYM-J 3x1,5 mm2.";
            pos.quantity = new BigDecimal("120");
            pos.unit = "m";
            pos.unitPrice = new BigDecimal("4.50");
            boq.positions.add(pos);
            HandwerkStore.saveBoqDocument(conn, boq);

            BoqDocument loadedBoq = HandwerkStore.loadBoqForItem(conn, 500);
            assertNotNull(loadedBoq);
            assertEquals(1, loadedBoq.positions.size());
            assertEquals("01.01.0010", loadedBoq.positions.get(0).ordinalNo);
            assertEquals(0, new BigDecimal("540.00").compareTo(loadedBoq.totalNet()));
            assertEquals(1, HandwerkStore.searchBoqPositions(conn, "NYM-J").size());

            // Measurement + TimeSheet
            Measurement meas = new Measurement();
            meas.siteId = s1.id;
            meas.itemId = 500;
            meas.ordinalNo = "01.01.0010";
            meas.workDate = "2026-11-10";
            meas.description = "Wand Nord";
            meas.formula = "4,50 * 2,60";
            meas.computedQty = MeasurementEvaluator.evaluate(meas.formula);
            meas.unit = "m2";
            HandwerkStore.saveMeasurement(conn, meas);
            assertEquals(1, HandwerkStore.loadMeasurementsForItem(conn, 500).size());

            TimeSheet ts = new TimeSheet();
            ts.siteId = s1.id;
            ts.itemId = 500;
            ts.workDate = "2026-11-10";
            ts.workerName = "Max Mustermann";
            ts.workerRole = HandwerkStore.ROLE_MASTER;
            ts.hours = new BigDecimal("3.50");
            ts.travelKm = new BigDecimal("24.00");
            ts.description = "Verteilerkasten verdrahtet";
            HandwerkStore.saveTimeSheet(conn, ts);
            assertEquals(1, HandwerkStore.loadTimeSheetsForItem(conn, 500).size());

            // GDPR cascade deletion
            HandwerkStore.deleteContactData(conn, 101);
            assertTrue(HandwerkStore.loadSitesForContact(conn, 101, true).isEmpty());
            assertTrue(HandwerkStore.loadTimeSheetsForSite(conn, s1.id).isEmpty());
            assertEquals(HandwerkStore.CUSTOMER_PRIVATE, HandwerkStore.loadContactMeta(conn, 101).customerKind);
        }
    }

    @Test
    void laborSplitAnd35aCalculationsAndWarnings() {
        LineInput labor = new LineInput(1, "HW-GESELLE", "Gesellenstunde Montage", "Std", true,
                new BigDecimal("400.00"), new BigDecimal("19.00"));
        LineInput travel = new LineInput(2, "HW-ANFAHRT", "Anfahrtspauschale", "Psch", true,
                new BigDecimal("50.00"), new BigDecimal("19.00"));
        LineInput material = new LineInput(3, "MAT-100", "Heizungsventil 1/2 Zoll", "Stk", false,
                new BigDecimal("200.00"), new BigDecimal("19.00"));
        LineInput mixed = new LineInput(4, "PKT-1", "Komplettpaket Pumpe inkl. Einbau", "Psch", false,
                new BigDecimal("1000.00"), new BigDecimal("19.00"));

        assertEquals(HandwerkStore.COST_LABOR, HandwerkValidator.classifyLine(labor, null));
        assertEquals(HandwerkStore.COST_TRAVEL, HandwerkValidator.classifyLine(travel, null));
        assertEquals(HandwerkStore.COST_MATERIAL, HandwerkValidator.classifyLine(material, null));

        LineCostType mixedOverride = new LineCostType();
        mixedOverride.subItemId = 4;
        mixedOverride.costType = HandwerkStore.COST_LABOR;
        mixedOverride.laborSharePercent = new BigDecimal("30.00");

        LaborSplitSummary split = HandwerkValidator.computeLaborSplit(
                Arrays.asList(labor, travel, material, mixed),
                Collections.singletonMap(4, mixedOverride),
                BigDecimal.ZERO);

        // Labor net = 400 + 50 + 300 = 750.00; VAT 19% = 142.50; Gross = 892.50
        assertEquals(new BigDecimal("750.00"), split.laborNet);
        assertEquals(new BigDecimal("142.50"), split.laborVat);
        assertEquals(new BigDecimal("892.50"), split.laborGross);
        // Material net = 200 + 700 = 900.00; VAT 19% = 171.00; Gross = 1071.00
        assertEquals(new BigDecimal("900.00"), split.materialNet);
        assertEquals(new BigDecimal("171.00"), split.materialVat);
        assertEquals(new BigDecimal("1071.00"), split.materialGross);
        // Estimated 20% credit = 178.50
        assertEquals(new BigDecimal("178.50"), split.estimatedCredit35a);

        // Cap at EUR 1,200.00 when labor gross exceeds EUR 6,000.00
        LineInput hugeLabor = new LineInput(5, "HW-MEISTER", "Meisterstunde", "Std", true,
                new BigDecimal("10000.00"), new BigDecimal("19.00"));
        LaborSplitSummary capped = HandwerkValidator.computeLaborSplit(
                Collections.singletonList(hugeLabor), Collections.emptyMap(), BigDecimal.ZERO);
        assertEquals(new BigDecimal("1200.00"), capped.estimatedCredit35a);
    }

    @Test
    void complianceRules14b13b48bVobAndTemplateFields() {
        LocalDate today = LocalDate.of(2026, 11, 22);
        Settings cfg = new Settings();
        cfg.chamberName = "HWK Berlin";
        cfg.chamberNumber = "HWK-99001";
        cfg.ownFreistellungTaxNo = "27/123/45678";
        cfg.ownFreistellungSecNo = "FS-7766";
        cfg.ownFreistellungValidUntil = "2025-12-31"; // expired!

        // Case 1: Private customer with cash payment, missing § 14b notice, and consumer retention > 5%
        ContactMeta priv = new ContactMeta();
        priv.customerKind = HandwerkStore.CUSTOMER_PRIVATE;
        ItemMeta privItem = new ItemMeta();
        privItem.laborTaxCreditEnabled = true;
        privItem.cashPayment = true;
        privItem.privateRetentionNotice = false;
        privItem.retentionPercent = new BigDecimal("8.00");
        LineInput matOnly = new LineInput(1, "M1", "Rohr", "Stk", false, new BigDecimal("100.00"), new BigDecimal("19.00"));

        List<String> w1 = HandwerkValidator.validate(
                Item.TYPE_INVOICE, priv, null, privItem,
                Collections.singletonList(matOnly), Collections.emptyMap(),
                null, new BigDecimal("119.00"), new BigDecimal("119.00"), cfg, today, null);
        assertTrue(w1.stream().anyMatch(s -> s.contains("35a") && s.contains("Cash")));
        assertTrue(w1.stream().anyMatch(s -> s.contains("35a") && s.contains("0.00")));
        assertTrue(w1.stream().anyMatch(s -> s.contains("14b")));
        assertTrue(w1.stream().anyMatch(s -> s.contains("650m")));

        // Case 2: § 13b Reverse Charge with expired USt 1 TG, non-zero VAT, expired own § 48b over EUR 5,000, unpriced BoQ
        ContactMeta b2b = new ContactMeta();
        b2b.customerKind = HandwerkStore.CUSTOMER_CONSTRUCTION_13B;
        b2b.reverseChargeCertNo = "UST1TG-OLD";
        b2b.reverseChargeCertUntil = "2026-01-01"; // expired
        ItemMeta rcItem = new ItemMeta();
        rcItem.reverseChargeEnabled = true;
        rcItem.retentionPercent = new BigDecimal("12.00"); // > 10% on part payment
        BoqDocument boq = new BoqDocument();
        BoqPosition unpriced = new BoqPosition();
        unpriced.ordinalNo = "01.01";
        unpriced.quantity = BigDecimal.TEN;
        unpriced.unitPrice = BigDecimal.ZERO;
        boq.positions.add(unpriced);

        List<String> w2 = HandwerkValidator.validate(
                Item.TYPE_PART_PAYMENT, b2b, null, rcItem,
                Collections.singletonList(matOnly), Collections.emptyMap(),
                boq, new BigDecimal("6000.00"), new BigDecimal("6000.00"), cfg, today, null);
        assertTrue(w2.stream().anyMatch(s -> s.contains("13b") && s.contains("expired")));
        assertTrue(w2.stream().anyMatch(s -> s.contains("13b") && s.contains("VAT > 0%")));
        assertTrue(w2.stream().anyMatch(s -> s.contains("48b") && s.contains("expired")));
        assertTrue(w2.stream().anyMatch(s -> s.contains("VOB/B")));
        assertTrue(w2.stream().anyMatch(s -> s.contains("BoQ")));

        // Retention under Reverse Charge forces NET basis
        ItemMeta rcRet = new ItemMeta();
        rcRet.reverseChargeEnabled = true;
        rcRet.retentionPercent = new BigDecimal("5.00");
        rcRet.retentionBasis = HandwerkStore.BASIS_GROSS;
        RetentionResult rr = HandwerkValidator.computeRetention(
                new BigDecimal("10000.00"), new BigDecimal("11900.00"), rcRet);
        assertEquals(HandwerkStore.BASIS_NET, rr.effectiveBasis);
        assertEquals(new BigDecimal("500.00"), rr.retentionAmount);
        assertEquals(new BigDecimal("9500.00"), rr.payableAfterRetention);

        // Warranty calculation: 4 years VOB/B vs 5 years BGB
        assertEquals("2030-05-15", HandwerkValidator.computeWarrantyUntil("2026-05-15", HandwerkStore.CONTRACT_VOB_B, 0));
        assertEquals("2031-05-15", HandwerkValidator.computeWarrantyUntil("2026-05-15", HandwerkStore.CONTRACT_BGB, 0));

        // Template fields must never contain null values
        Site site = new Site();
        site.projectNo = "P-10";
        site.siteName = "Haus A";
        site.street = "Weg 1";
        site.zip = "80331";
        site.city = "München";
        site.contractType = HandwerkStore.CONTRACT_VOB_B;
        site.acceptanceDate = "2026-05-15";
        site.warrantyYears = 4;
        site.warrantyUntil = "2030-05-15";

        Map<String, Object> fields = HandwerkValidator.templateFields(
                b2b, site, rcRet, new LaborSplitSummary(),
                new BigDecimal("10000.00"), new BigDecimal("10000.00"),
                boq, Collections.emptyList(), Collections.emptyList(), cfg, null);
        assertFalse(fields.isEmpty());
        for (Map.Entry<String, Object> e : fields.entrySet()) {
            assertNotNull(e.getValue(), "Placeholder must never be null: " + e.getKey());
        }
        assertEquals("HWK Berlin", fields.get("hw.chamberName"));
        assertEquals("VOB/B", fields.get("hw.contractType"));
        assertEquals("500.00", fields.get("hw.retentionAmount"));
        assertEquals("9500.00", fields.get("hw.payableAfterRetention"));
        assertTrue(String.valueOf(fields.get("hw.notice13b")).contains("13b"));
    }

    @Test
    void gaebXmlRoundTripX83ToX84() throws Exception {
        String x83 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<GAEB xmlns=\"http://www.gaeb.de/GAEB_DA_XML/DA83/3.3\">\n"
                + "  <GAEBInfo>\n"
                + "    <Version>3.3</Version>\n"
                + "    <VersDate>2021-05</VersDate>\n"
                + "    <Date>2026-10-01</Date>\n"
                + "    <ProgSystem>ArchitectAVA</ProgSystem>\n"
                + "  </GAEBInfo>\n"
                + "  <PrjInfo><NamePrj>Schule Nord</NamePrj></PrjInfo>\n"
                + "  <Award>\n"
                + "    <AwardInfo><Cur>EUR</Cur></AwardInfo>\n"
                + "    <BoQ ID=\"B1\">\n"
                + "      <BoQInfo><Name>Sanitär</Name></BoQInfo>\n"
                + "      <BoQBody>\n"
                + "        <BoQCtgy ID=\"C1\" RNoPart=\"01\">\n"
                + "          <LblTx><p>Rohinstallation</p></LblTx>\n"
                + "          <BoQBody>\n"
                + "            <Itemlist>\n"
                + "              <Item ID=\"I1\" RNoPart=\"0010\">\n"
                + "                <Qty>25.000</Qty>\n"
                + "                <QU>m</QU>\n"
                + "                <Description>\n"
                + "                  <CompleteText>\n"
                + "                    <OutlineText><OutlTxt><TextOutlTxt>Kupferrohr DN 22 verlegen</TextOutlTxt></OutlTxt></OutlineText>\n"
                + "                    <DetailTxt><Text><p>Liefern und verlegen von Kupferrohr DN 22 inklusive Schellen.</p></Text></DetailTxt>\n"
                + "                  </CompleteText>\n"
                + "                </Description>\n"
                + "              </Item>\n"
                + "              <Item ID=\"I2\" RNoPart=\"0020\">\n"
                + "                <Provis>WithoutTotal</Provis>\n"
                + "                <Qty>2.000</Qty>\n"
                + "                <QU>Stk</QU>\n"
                + "                <Description>\n"
                + "                  <CompleteText>\n"
                + "                    <OutlineText><OutlTxt><TextOutlTxt>Eventualposition Absperrventil</TextOutlTxt></OutlTxt></OutlineText>\n"
                + "                  </CompleteText>\n"
                + "                </Description>\n"
                + "              </Item>\n"
                + "            </Itemlist>\n"
                + "          </BoQBody>\n"
                + "        </BoQCtgy>\n"
                + "      </BoQBody>\n"
                + "    </BoQ>\n"
                + "  </Award>\n"
                + "</GAEB>";

        BoqCodec codec = BoqCodec.findReader("ausschreibung.X83", x83);
        assertNotNull(codec);
        assertEquals("GAEB_XML", codec.id());

        BoqDocument doc = codec.read("ausschreibung.X83", x83);
        assertEquals("GAEB_XML_33", doc.formatCode);
        assertEquals("83", doc.exchangePhase);
        assertEquals("Schule Nord", doc.projectName);
        assertEquals("Sanitär", doc.boqLabel);
        assertEquals(3, doc.positions.size());

        BoqPosition header = doc.positions.get(0);
        assertEquals("01", header.ordinalNo);
        assertEquals(PositionType.TEXT, header.positionType);
        assertEquals("Rohinstallation", header.shortText);

        BoqPosition p1 = doc.positions.get(1);
        assertEquals("01.0010", p1.ordinalNo);
        assertEquals(PositionType.NORMAL, p1.positionType);
        assertEquals(0, new BigDecimal("25").compareTo(p1.quantity));
        assertEquals("m", p1.unit);
        assertTrue(p1.shortText.contains("Kupferrohr DN 22"));
        assertTrue(p1.longText.contains("Schellen"));

        BoqPosition p2 = doc.positions.get(2);
        assertEquals("01.0020", p2.ordinalNo);
        assertEquals(PositionType.OPTIONAL, p2.positionType);
        assertFalse(p2.isCountedInTotal());

        Map<String, BigDecimal> prices = new LinkedHashMap<String, BigDecimal>();
        prices.put("01.0010", new BigDecimal("42.50"));
        prices.put("01.0020", new BigDecimal("85.00"));
        Map<String, String> bidderTexts = Collections.singletonMap("01.0010", "Fabrikat Viega Profipress");

        byte[] x84Bytes = codec.exportBid(doc, prices, bidderTexts, "2.1.0");
        String x84 = new String(x84Bytes, StandardCharsets.UTF_8);

        assertTrue(x84.contains("GAEB_DA_XML/DA84/3.3"), "Must switch namespace to DA84");
        assertTrue(x84.contains("OpenYaBS"), "Must regenerate GAEBInfo with OpenYaBS producer");
        assertTrue(x84.contains("<UP>42.50</UP>"), "Must write unit price for 01.0010");
        assertTrue(x84.contains("<IT>1062.50</IT>"), "Must compute line total 25 * 42.50 = 1062.50");
        assertTrue(x84.contains("Viega Profipress"), "Must preserve bidder text");

        // Re-parse exported X84 to confirm valid GAEB DA XML
        BoqDocument reparsed = codec.read("angebot.X84", x84);
        assertEquals("84", reparsed.exchangePhase);
        assertEquals(0, new BigDecimal("42.50").compareTo(reparsed.positions.get(1).unitPrice));
        assertEquals(0, new BigDecimal("1062.50").compareTo(reparsed.totalNet()),
                "Optional position must not be included in BoQ net total");
    }

    @Test
    void gaeb90RoundTripD83ToD84() throws Exception {
        String d83 = ""
                + "00830000000000000000000000000000000000000000000000000000000000000000000000000001\n"
                + "2101010010             00000010000m2                                          02\n"
                + "25Malerarbeiten Innenwand streichen                                           03\n"
                + "26Zweifacher Dispersionsanstrich weiss deckend.                               04\n";

        BoqCodec codec = BoqCodec.findReader("tender.D83", d83);
        assertNotNull(codec);
        assertEquals("GAEB_90", codec.id());

        BoqDocument doc = codec.read("tender.D83", d83);
        assertEquals("GAEB_90", doc.formatCode);
        assertEquals(1, doc.positions.size());
        BoqPosition p = doc.positions.get(0);
        assertEquals("01010010", p.ordinalNo);
        assertEquals(0, new BigDecimal("10.000").compareTo(p.quantity));
        assertEquals("m2", p.unit);
        assertTrue(p.shortText.contains("Malerarbeiten"));
        assertTrue(p.longText.contains("Dispersionsanstrich"));

        byte[] d84Bytes = codec.exportBid(doc,
                Collections.singletonMap("01010010", new BigDecimal("18.50")),
                Collections.emptyMap(), "2.1.0");
        String d84 = new String(d84Bytes, StandardCharsets.ISO_8859_1);
        assertTrue(d84.startsWith("0084"), "Header phase must switch to 84");
        assertTrue(d84.contains("2101010010"), "Must emit type-21 price record for OZ");
        assertTrue(d84.contains("00000001850"), "Must encode 18.50 EUR as 00000001850 cents");
    }

    @Test
    void csvBoqCodecRoundTrip() throws Exception {
        String csv = "OZ;Type;Qty;Unit;UnitPrice;ShortText;LongText\n"
                + "01.01;NORMAL;12,5;m2;0,00;\"Fliesen verlegen\";\"Feinsteinzeug 60x60 cm\"\n"
                + "01.02;OPTIONAL;1;Psch;;\"Sockelleisten optional\";\"Optional\"\n";

        BoqCodec codec = BoqCodec.findReader("boq.csv", csv);
        assertNotNull(codec);
        assertEquals("CSV", codec.id());

        BoqDocument doc = codec.read("boq.csv", csv);
        assertEquals(2, doc.positions.size());
        assertEquals("01.01", doc.positions.get(0).ordinalNo);
        assertEquals(0, new BigDecimal("12.5").compareTo(doc.positions.get(0).quantity));
        assertEquals(PositionType.OPTIONAL, doc.positions.get(1).positionType);

        byte[] out = codec.exportBid(doc,
                Collections.singletonMap("01.01", new BigDecimal("64.00")),
                Collections.singletonMap("01.01", "Villeroy & Boch"), "2.1.0");
        BoqDocument reparsed = codec.read("bid.csv", new String(out, StandardCharsets.UTF_8));
        assertEquals(0, new BigDecimal("64.00").compareTo(reparsed.positions.get(0).unitPrice));
        assertEquals("Villeroy & Boch", reparsed.positions.get(0).bidderText);
    }

    @Test
    void datanorm4And5Parsing() {
        String rab = "V 04010126Grosshandel GmbH                                                  EUR\n"
                + "R;N;RG01;2500;Rohrmaterial 25% Rabatt;\n";
        String dn4 = "A;N;ART-100;00;Kupferrohr 22x1mm;Stange 5m;1;2;m;125000;RG01;WG01;\n"
                + "B;N;ART-100;KUPFER22;;;4012345678901;\n"
                + "T;N;ART-100;1;1;Nahtlos gezogenes Kupferrohr nach EN 1057;2;Halbhart R250;\n"
                + "P;N;ART-100;1;100000;\n";

        ParseResult res = DatanormImporter.parse(rab, dn4);
        assertEquals("4", res.version);
        Article a = res.articles.get("ART-100");
        assertNotNull(a);
        assertEquals("Kupferrohr 22x1mm", a.name);
        assertEquals("4012345678901", a.ean);
        assertEquals("m", a.unit);
        assertTrue(a.longText.contains("EN 1057"));
        // Price updated by P record: 100000 cents / 100 (priceUnit=2) = 10.00 list price; minus 25% RG01 discount = 7.50 net
        assertEquals(new BigDecimal("10.00"), a.rawUnitPrice);
        assertEquals(new BigDecimal("7.50"), a.netPurchasePrice);
        assertEquals(1, res.priceUpdates);

        String dn5 = "V;050;S;20261101;EUR;Elektro Grosshandel;\n"
                + "A;N;EL-500;00;Schalter UP;Einsatz alpinweiss;2;0;Stk;1490;;;\n";
        ParseResult res5 = DatanormImporter.parse(dn5);
        assertEquals("5", res5.version);
        Article a5 = res5.articles.get("EL-500");
        assertNotNull(a5);
        assertEquals(new BigDecimal("14.90"), a5.netPurchasePrice);
    }

    @Test
    void measurementEvaluatorArithmeticAndSafety() {
        assertEquals(0, new BigDecimal("37.52").compareTo(
                MeasurementEvaluator.evaluate("2 * (4,50 + 3,20) * 2,60 - 1,20 * 2,10")));
        assertEquals(0, new BigDecimal("-5").compareTo(MeasurementEvaluator.evaluate("-2 - 3")));
        assertEquals(0, new BigDecimal("12.5").compareTo(MeasurementEvaluator.evaluate("25 / 2")));

        assertThrows(IllegalArgumentException.class, () -> MeasurementEvaluator.evaluate(""));
        assertThrows(IllegalArgumentException.class, () -> MeasurementEvaluator.evaluate("10 / 0"));
        assertThrows(IllegalArgumentException.class, () -> MeasurementEvaluator.evaluate("2 * (3 + 4"));
        assertThrows(IllegalArgumentException.class, () -> MeasurementEvaluator.evaluate("Runtime.getRuntime()"));
    }

    private static Properties loadUtf8Props(String resourcePath) throws Exception {
        Properties p = new Properties();
        try (InputStream in = HandwerkPlugin.class.getResourceAsStream(resourcePath)) {
            assertNotNull(in, "Missing resource: " + resourcePath);
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return p;
    }
}
