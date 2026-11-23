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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mpv5.db.objects.Item;
import org.junit.jupiter.api.Test;
import org.openyabs.handwerk.BoqDocument.BoqPosition;
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
 * Exhaustive unit tests for {@link HandwerkValidator} (classification, § 35a,
 * § 14b, § 13b, § 48b, VOB/B &amp; BGB § 650m retention/warranty, date
 * parsing, template placeholders) and cross-database {@link HandwerkStore}
 * persistence on both H2 and Apache Derby.
 */
class HandwerkComplianceAndStoreTest {

    // ------------------------------------------------- line classification

    @Test
    void classifyLineCoversAllHeuristicsAndExplicitOverrides() {
        LineInput textLine = new LineInput(1, "HW-MEISTER", "Meisterstunde", "Std", true, BigDecimal.TEN, BigDecimal.ZERO);
        textLine.textLine = true;
        assertEquals(HandwerkStore.COST_OTHER, HandwerkValidator.classifyLine(textLine, null));

        // Travel heuristics (SKU, German/English description, unit km)
        assertEquals(HandwerkStore.COST_TRAVEL, HandwerkValidator.classifyLine(
                new LineInput(2, "HW-ANFAHRT", "Pauschale", "Psch", false, BigDecimal.TEN, BigDecimal.ZERO), null));
        assertEquals(HandwerkStore.COST_TRAVEL, HandwerkValidator.classifyLine(
                new LineInput(3, "X", "Fahrtkosten Baustelle", "Psch", false, BigDecimal.TEN, BigDecimal.ZERO), null));
        assertEquals(HandwerkStore.COST_TRAVEL, HandwerkValidator.classifyLine(
                new LineInput(4, "X", "Kilometergeld", "km", false, BigDecimal.TEN, BigDecimal.ZERO), null));

        // Machine heuristics
        assertEquals(HandwerkStore.COST_MACHINE, HandwerkValidator.classifyLine(
                new LineInput(5, "HW-MASCHINE", "Einsatz", "Std", false, BigDecimal.TEN, BigDecimal.ZERO), null));
        assertEquals(HandwerkStore.COST_MACHINE, HandwerkValidator.classifyLine(
                new LineInput(6, "X", "Minibagger Miete", "Tag", false, BigDecimal.TEN, BigDecimal.ZERO), null));
        assertEquals(HandwerkStore.COST_MACHINE, HandwerkValidator.classifyLine(
                new LineInput(7, "X", "Fassadengerüst Stellung", "Psch", false, BigDecimal.TEN, BigDecimal.ZERO), null));

        // Labor heuristics (SKU, service flag, time units, DE/EN keywords)
        assertEquals(HandwerkStore.COST_LABOR, HandwerkValidator.classifyLine(
                new LineInput(8, "HW-AZUBI", "Helfer", "Stk", false, BigDecimal.TEN, BigDecimal.ZERO), null));
        assertEquals(HandwerkStore.COST_LABOR, HandwerkValidator.classifyLine(
                new LineInput(9, "CUSTOM", "Pauschalangebot", "Psch", true, BigDecimal.TEN, BigDecimal.ZERO), null));
        assertEquals(HandwerkStore.COST_LABOR, HandwerkValidator.classifyLine(
                new LineInput(10, "X", "Einsatz vor Ort", "h", false, BigDecimal.TEN, BigDecimal.ZERO), null));
        assertEquals(HandwerkStore.COST_LABOR, HandwerkValidator.classifyLine(
                new LineInput(11, "X", "Annual boiler maintenance installation", "pcs", false, BigDecimal.TEN, BigDecimal.ZERO), null));

        // Material fallback
        assertEquals(HandwerkStore.COST_MATERIAL, HandwerkValidator.classifyLine(
                new LineInput(12, "MAT-55", "Kupferfitting 90 Grad", "Stk", false, BigDecimal.TEN, BigDecimal.ZERO), null));

        // Explicit override always wins over heuristics
        LineCostType ov = new LineCostType();
        ov.costType = HandwerkStore.COST_MATERIAL;
        assertEquals(HandwerkStore.COST_MATERIAL, HandwerkValidator.classifyLine(
                new LineInput(13, "HW-MEISTER", "Meisterstunde", "Std", true, BigDecimal.TEN, BigDecimal.ZERO), ov));
    }

    // ----------------------------------------- § 35a discount, VAT & bounds

    @Test
    void laborSplitHandlesDocumentDiscountMixedVatAndClampedOverrides() {
        LineInput labor19 = new LineInput(1, "HW-GESELLE", "Montage", "Std", true,
                new BigDecimal("1000.00"), new BigDecimal("19.00"));
        LineInput labor7 = new LineInput(2, "HW-MEISTER", "Sonderleistung 7%", "Std", true,
                new BigDecimal("200.00"), new BigDecimal("7.00"));
        LineInput mat19 = new LineInput(3, "M-1", "Kabel", "m", false,
                new BigDecimal("500.00"), new BigDecimal("19.00"));
        LineInput clampedHigh = new LineInput(4, "P-1", "Paket", "Psch", false,
                new BigDecimal("100.00"), new BigDecimal("19.00"));
        LineInput clampedNeg = new LineInput(5, "P-2", "Paket 2", "Psch", false,
                new BigDecimal("100.00"), new BigDecimal("19.00"));

        Map<Integer, LineCostType> overrides = new HashMap<Integer, LineCostType>();
        LineCostType ovHigh = new LineCostType();
        ovHigh.subItemId = 4;
        ovHigh.costType = HandwerkStore.COST_LABOR;
        ovHigh.laborSharePercent = new BigDecimal("150.00"); // clamped to 100%
        overrides.put(4, ovHigh);

        LineCostType ovNeg = new LineCostType();
        ovNeg.subItemId = 5;
        ovNeg.costType = HandwerkStore.COST_LABOR;
        ovNeg.laborSharePercent = new BigDecimal("-25.00"); // clamped to 0%
        overrides.put(5, ovNeg);

        // Apply 10% document-level discount -> factor 0.90
        LaborSplitSummary s = HandwerkValidator.computeLaborSplit(
                Arrays.asList(labor19, labor7, mat19, clampedHigh, clampedNeg),
                overrides, new BigDecimal("10.00"));

        // Labor net: (1000 + 200 + 100) * 0.9 = 1170.00
        // Labor VAT: (1000*0.9*0.19) + (200*0.9*0.07) + (100*0.9*0.19) = 171.00 + 12.60 + 17.10 = 200.70
        assertEquals(new BigDecimal("1170.00"), s.laborNet);
        assertEquals(new BigDecimal("200.70"), s.laborVat);
        assertEquals(new BigDecimal("1370.70"), s.laborGross);

        // Material net: (500 + 100) * 0.9 = 540.00; VAT = 540 * 0.19 = 102.60; Gross = 642.60
        assertEquals(new BigDecimal("540.00"), s.materialNet);
        assertEquals(new BigDecimal("102.60"), s.materialVat);
        assertEquals(new BigDecimal("642.60"), s.materialGross);

        // Credit = 20% of 1370.70 = 274.14
        assertEquals(new BigDecimal("274.14"), s.estimatedCredit35a);
    }

    // ----------------------------------------- retention & date boundaries

    @Test
    void retentionFixedAmountGrossNetAndClamping() {
        ItemMeta grossMeta = new ItemMeta();
        grossMeta.retentionPercent = new BigDecimal("5.00");
        grossMeta.retentionBasis = HandwerkStore.BASIS_GROSS;
        RetentionResult rGross = HandwerkValidator.computeRetention(
                new BigDecimal("10000.00"), new BigDecimal("11900.00"), grossMeta);
        assertEquals(HandwerkStore.BASIS_GROSS, rGross.effectiveBasis);
        assertEquals(new BigDecimal("595.00"), rGross.retentionAmount);
        assertEquals(new BigDecimal("11305.00"), rGross.payableAfterRetention);

        ItemMeta fixedMeta = new ItemMeta();
        fixedMeta.retentionPercent = BigDecimal.ZERO;
        fixedMeta.retentionAmount = new BigDecimal("250.00");
        RetentionResult rFixed = HandwerkValidator.computeRetention(
                new BigDecimal("1000.00"), new BigDecimal("1190.00"), fixedMeta);
        assertEquals(new BigDecimal("250.00"), rFixed.retentionAmount);
        assertEquals(new BigDecimal("940.00"), rFixed.payableAfterRetention);

        // Excessive retention never produces negative payable amount
        ItemMeta overMeta = new ItemMeta();
        overMeta.retentionAmount = new BigDecimal("9999.00");
        RetentionResult rClamped = HandwerkValidator.computeRetention(
                new BigDecimal("500.00"), new BigDecimal("595.00"), overMeta);
        assertEquals(new BigDecimal("0.00"), rClamped.payableAfterRetention);
    }

    @Test
    void dateParsingAndCertificateExpiryBoundaryDates() {
        assertEquals(LocalDate.of(2026, 11, 22), HandwerkValidator.parseDate("2026-11-22"));
        assertEquals(LocalDate.of(2026, 11, 22), HandwerkValidator.parseDate("22.11.2026"));
        assertEquals(LocalDate.of(2028, 2, 29), HandwerkValidator.parseDate("2028-02")); // leap year
        assertEquals(LocalDate.of(2027, 2, 28), HandwerkValidator.parseDate("2027-02")); // non-leap year
        assertNull(HandwerkValidator.parseDate("not-a-date"));
        assertNull(HandwerkValidator.parseDate(""));

        LocalDate today = LocalDate.of(2026, 11, 22);
        // Yesterday: expired, not expiring soon
        assertTrue(HandwerkValidator.isCertificateExpired("2026-11-21", today));
        assertFalse(HandwerkValidator.isCertificateExpiringSoon("2026-11-21", 30, today));
        // Today: NOT expired yet, IS expiring soon
        assertFalse(HandwerkValidator.isCertificateExpired("2026-11-22", today));
        assertTrue(HandwerkValidator.isCertificateExpiringSoon("2026-11-22", 30, today));
        // Exactly +30 days: IS expiring soon
        assertTrue(HandwerkValidator.isCertificateExpiringSoon("2026-12-22", 30, today));
        // +31 days: valid & NOT expiring soon
        assertFalse(HandwerkValidator.isCertificateExpiringSoon("2026-12-23", 30, today));
    }

    // ----------------------------------------- full compliance rule matrix

    @Test
    void complianceValidationCleanPassAndEdgeCases() {
        LocalDate today = LocalDate.of(2026, 11, 22);
        Settings cfg = new Settings();
        cfg.ownFreistellungTaxNo = "27/123/45678";
        cfg.ownFreistellungSecNo = "SEC-1234";
        cfg.ownFreistellungValidUntil = "2028-12-31";

        LineInput labor19 = new LineInput(1, "HW-GESELLE", "Montage", "Std", true,
                new BigDecimal("1000.00"), new BigDecimal("19.00"));
        LineInput labor0 = new LineInput(2, "HW-GESELLE", "Bauleistung § 13b", "Std", true,
                new BigDecimal("10000.00"), BigDecimal.ZERO);

        // 1. Fully compliant private invoice -> ZERO warnings
        ContactMeta priv = new ContactMeta();
        priv.customerKind = HandwerkStore.CUSTOMER_PRIVATE;
        ItemMeta privIm = new ItemMeta();
        privIm.laborTaxCreditEnabled = true;
        privIm.cashPayment = false;
        privIm.privateRetentionNotice = true;
        privIm.retentionPercent = new BigDecimal("5.00"); // exactly at BGB § 650m cap -> OK
        assertTrue(HandwerkValidator.validate(
                Item.TYPE_INVOICE, priv, null, privIm,
                Collections.singletonList(labor19), Collections.emptyMap(),
                null, new BigDecimal("1190.00"), new BigDecimal("1190.00"), cfg, today, null).isEmpty());

        // 2. Fully compliant § 13b Reverse Charge commercial invoice over EUR 5,000 -> ZERO warnings
        ContactMeta b2b13b = new ContactMeta();
        b2b13b.customerKind = HandwerkStore.CUSTOMER_CONSTRUCTION_13B;
        b2b13b.reverseChargeCertNo = "UST1TG-2026-88";
        b2b13b.reverseChargeCertUntil = "2028-12-31";
        ItemMeta rcIm = new ItemMeta();
        rcIm.reverseChargeEnabled = true;
        rcIm.retentionPercent = new BigDecimal("10.00"); // exactly at VOB/B § 17 cap -> OK
        assertTrue(HandwerkValidator.validate(
                Item.TYPE_PART_PAYMENT, b2b13b, null, rcIm,
                Collections.singletonList(labor0), Collections.emptyMap(),
                null, new BigDecimal("10000.00"), new BigDecimal("10000.00"), cfg, today, null).isEmpty());

        // 3. Construction 13b customer with valid USt 1 TG billed with 19% VAT -> triggers val.13b.expected!
        ItemMeta nonRcIm = new ItemMeta();
        nonRcIm.reverseChargeEnabled = false;
        List<String> expectedRcWarnings = HandwerkValidator.validate(
                Item.TYPE_INVOICE, b2b13b, null, nonRcIm,
                Collections.singletonList(labor19), Collections.emptyMap(),
                null, new BigDecimal("1190.00"), new BigDecimal("1190.00"), cfg, today, null);
        assertEquals(1, expectedRcWarnings.size());
        assertTrue(expectedRcWarnings.get(0).contains("13b"));

        // 4. Offers do NOT trigger § 14b or § 48b billing warnings even if missing notices/certificates
        Settings emptyCfg = new Settings();
        assertTrue(HandwerkValidator.validate(
                Item.TYPE_OFFER, priv, null, new ItemMeta(),
                Collections.singletonList(labor19), Collections.emptyMap(),
                null, new BigDecimal("9000.00"), new BigDecimal("9000.00"), emptyCfg, today, null).isEmpty());

        // 5. Commercial invoice at exact EUR 5,000.00 threshold -> NO § 48b warning; EUR 5,000.01 -> warning!
        ContactMeta comm = new ContactMeta();
        comm.customerKind = HandwerkStore.CUSTOMER_COMMERCIAL;
        assertTrue(HandwerkValidator.validate(
                Item.TYPE_INVOICE, comm, null, new ItemMeta(),
                Collections.singletonList(labor19), Collections.emptyMap(),
                null, new BigDecimal("5000.00"), new BigDecimal("5000.00"), emptyCfg, today, null).isEmpty());
        List<String> overThreshold = HandwerkValidator.validate(
                Item.TYPE_INVOICE, comm, null, new ItemMeta(),
                Collections.singletonList(labor19), Collections.emptyMap(),
                null, new BigDecimal("5000.01"), new BigDecimal("5000.01"), emptyCfg, today, null);
        assertTrue(overThreshold.stream().anyMatch(w -> w.contains("48b")));
    }

    // ----------------------------------- cross-DB persistence: H2 & Derby

    @Test
    void crossDatabaseSchemaAndCrudWorkIdenticallyOnH2AndApacheDerby() throws Exception {
        String[] urls = {
            "jdbc:h2:mem:handwerk_crossdb_h2;DB_CLOSE_DELAY=-1",
            "jdbc:derby:memory:handwerk_crossdb_derby;create=true"
        };
        for (String url : urls) {
            try (Connection conn = DriverManager.getConnection(url)) {
                verifyAllNineTablesRoundTrip(conn, url);
            }
        }
    }

    private static void verifyAllNineTablesRoundTrip(Connection conn, String label) throws Exception {
        HandwerkStore.initSchema(conn);
        HandwerkStore.initSchema(conn); // idempotent on repeated startup
        assertEquals(HandwerkStore.SCHEMA_VERSION, HandwerkStore.getSchemaVersion(conn), label);

        // ContactMeta insert + update
        ContactMeta cm = new ContactMeta();
        cm.contactId = 77;
        cm.customerKind = HandwerkStore.CUSTOMER_COMMERCIAL;
        cm.reverseChargeCertNo = "RC-1";
        HandwerkStore.saveContactMeta(conn, cm);
        cm.reverseChargeCertNo = "RC-2";
        cm.reverseChargeCertUntil = "2029-12-31";
        HandwerkStore.saveContactMeta(conn, cm);
        assertEquals("RC-2", HandwerkStore.loadContactMeta(conn, 77).reverseChargeCertNo, label);
        assertEquals(1, HandwerkStore.loadAllContactMetas(conn).size(), label);

        // Site insert + update + search
        Site site = new Site();
        site.contactId = 77;
        site.projectNo = "PRJ-77";
        site.siteName = "Werkhalle Ost";
        site.street = "Industriestr. 5";
        site.zip = "20095";
        site.city = "Hamburg";
        HandwerkStore.saveSite(conn, site);
        site.notes = "Updated note";
        HandwerkStore.saveSite(conn, site);
        assertEquals("Updated note", HandwerkStore.loadSite(conn, site.id).notes, label);
        assertEquals(1, HandwerkStore.searchSites(conn, "hamburg").size(), label);

        // ItemMeta insert + update
        ItemMeta im = new ItemMeta();
        im.itemId = 801;
        im.siteId = site.id;
        im.retentionPercent = new BigDecimal("5.00");
        im.retentionAmount = new BigDecimal("250.00");
        HandwerkStore.saveItemMeta(conn, im);
        im.retentionBondRef = "BOND-2026";
        HandwerkStore.saveItemMeta(conn, im);
        assertEquals("BOND-2026", HandwerkStore.loadItemMeta(conn, 801).retentionBondRef, label);

        // LineCostType insert + update
        LineCostType lct = new LineCostType();
        lct.itemId = 801;
        lct.subItemId = 901;
        lct.costType = HandwerkStore.COST_LABOR;
        lct.laborSharePercent = new BigDecimal("100.00");
        HandwerkStore.saveLineCostType(conn, lct);
        lct.laborSharePercent = new BigDecimal("45.00");
        HandwerkStore.saveLineCostType(conn, lct);
        assertEquals(0, new BigDecimal("45.00").compareTo(
                HandwerkStore.loadLineCostTypes(conn, 801).get(901).laborSharePercent), label);

        // BoQ insert + replace positions with large CLOB payload
        BoqDocument boq = new BoqDocument();
        boq.itemId = 801;
        boq.formatCode = "GAEB_XML_33";
        boq.projectName = "Werkhalle";
        StringBuilder bigRaw = new StringBuilder("<GAEB>");
        for (int i = 0; i < 400; i++) {
            bigRaw.append("0123456789");
        }
        bigRaw.append("</GAEB>");
        boq.rawSource = bigRaw.toString();
        BoqPosition pos = new BoqPosition();
        pos.ordinalNo = "01.0010";
        pos.shortText = "Hallenbeleuchtung LED";
        pos.longText = bigRaw.toString();
        pos.quantity = new BigDecimal("20");
        pos.unit = "Stk";
        pos.unitPrice = new BigDecimal("150.00");
        boq.positions.add(pos);
        HandwerkStore.saveBoqDocument(conn, boq);
        // Update BoQ (replaces positions cleanly)
        pos.unitPrice = new BigDecimal("160.00");
        HandwerkStore.saveBoqDocument(conn, boq);
        BoqDocument loadedBoq = HandwerkStore.loadBoqForItem(conn, 801);
        assertNotNull(loadedBoq, label);
        assertEquals(bigRaw.length(), loadedBoq.rawSource.length(), label);
        assertEquals(0, new BigDecimal("160.00").compareTo(loadedBoq.positions.get(0).unitPrice), label);

        // Measurement + TimeSheet insert, update, delete
        Measurement m = new Measurement();
        m.siteId = site.id;
        m.itemId = 801;
        m.description = "Decke";
        m.formula = "10 * 5";
        m.computedQty = new BigDecimal("50.000");
        HandwerkStore.saveMeasurement(conn, m);
        m.description = "Decke neu";
        HandwerkStore.saveMeasurement(conn, m);
        assertEquals("Decke neu", HandwerkStore.loadMeasurementsForItem(conn, 801).get(0).description, label);

        TimeSheet ts = new TimeSheet();
        ts.siteId = site.id;
        ts.itemId = 801;
        ts.workerName = "Erika";
        ts.hours = new BigDecimal("4.00");
        HandwerkStore.saveTimeSheet(conn, ts);
        ts.billed = true;
        HandwerkStore.saveTimeSheet(conn, ts);
        assertTrue(HandwerkStore.loadTimeSheetsForItem(conn, 801).get(0).billed, label);

        HandwerkStore.deleteMeasurement(conn, m.id);
        HandwerkStore.deleteTimeSheet(conn, ts.id);
        assertTrue(HandwerkStore.loadMeasurementsForItem(conn, 801).isEmpty(), label);
        assertTrue(HandwerkStore.loadTimeSheetsForItem(conn, 801).isEmpty(), label);

        HandwerkStore.deleteContactData(conn, 77);
        assertTrue(HandwerkStore.loadAllSites(conn, true).isEmpty(), label);
    }
}
