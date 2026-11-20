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
package org.openyabs.verein;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import mpv5.plugin.ExtensionManager;
import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.YabsExtension;
import org.junit.jupiter.api.Test;
import org.openyabs.verein.VereinStore.InvoiceData;
import org.openyabs.verein.VereinStore.MemberData;
import org.openyabs.verein.VereinStore.Settings;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the German Non-Profit Association (e.V.) plugin ({@link VereinPlugin}).
 */
class VereinPluginTest {

    @Test
    void serviceLoaderFindsVereinPluginAndResourcesMatch() throws Exception {
        boolean found = false;
        for (YabsExtension ext : ServiceLoader.load(YabsExtension.class)) {
            if (VereinPlugin.ID.equals(ext.id())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "VereinPlugin must be discoverable via ServiceLoader");

        for (String res : VereinPlugin.TEMPLATE_RESOURCES) {
            try (InputStream in = VereinPlugin.class.getResourceAsStream(res)) {
                assertNotNull(in, "Missing template resource: " + res);
                String xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(xml.contains("<office:document"), "Valid FODT root in " + res);
                assertTrue(xml.contains("[ev."), "OpenYaBS [ev.*] placeholders in " + res);
            }
        }

        Properties en = loadUtf8Props(VereinPlugin.I18N_EN);
        Properties de = loadUtf8Props(VereinPlugin.I18N_DE);
        assertEquals(en.stringPropertyNames(), de.stringPropertyNames(),
                "DE and EN translation bundles must have identical keys");
    }

    @Test
    void amountToGermanWordsProducesRealGermanWordsForBmfReceipts() {
        assertEquals("null Euro", VereinValidator.amountToGermanWords(BigDecimal.ZERO));
        assertEquals("ein Euro", VereinValidator.amountToGermanWords(new BigDecimal("1.00")));
        assertEquals("einundzwanzig Euro f\u00fcnf Cent", VereinValidator.amountToGermanWords(new BigDecimal("21.05")));
        assertEquals("einhundertf\u00fcnfundzwanzig Euro", VereinValidator.amountToGermanWords(new BigDecimal("125.00")));
        assertEquals("eintausend Euro", VereinValidator.amountToGermanWords(new BigDecimal("1000.00")));
        assertEquals("zweitausendf\u00fcnfhundert Euro f\u00fcnfzig Cent", VereinValidator.amountToGermanWords(new BigDecimal("2500.50")));
        assertEquals("eine Million Euro", VereinValidator.amountToGermanWords(new BigDecimal("1000000.00")));
    }

    @Test
    void billabilitySkipsExemptAndExitedMembers() {
        MemberData active = new MemberData();
        active.status = VereinStore.STATUS_ACTIVE;
        active.tierSku = "BEITRAG-AKTIV";
        assertTrue(VereinValidator.isBillableForYear(active, 2026));

        MemberData passive = new MemberData();
        passive.status = VereinStore.STATUS_PASSIVE_FREE;
        passive.tierSku = "BEITRAG-AKTIV";
        assertFalse(VereinValidator.isBillableForYear(passive, 2026));

        MemberData honorary = new MemberData();
        honorary.status = VereinStore.STATUS_HONORARY;
        honorary.tierSku = "BEITRAG-AKTIV";
        assertFalse(VereinValidator.isBillableForYear(honorary, 2026));

        MemberData exitedBefore = new MemberData();
        exitedBefore.status = VereinStore.STATUS_ACTIVE;
        exitedBefore.tierSku = "BEITRAG-AKTIV";
        exitedBefore.exitDate = "2025-12-31";
        assertFalse(VereinValidator.isBillableForYear(exitedBefore, 2026));

        MemberData exitedDuring = new MemberData();
        exitedDuring.status = VereinStore.STATUS_ACTIVE;
        exitedDuring.tierSku = "BEITRAG-AKTIV";
        exitedDuring.exitDate = "2026-06-30";
        assertTrue(VereinValidator.isBillableForYear(exitedDuring, 2026));
    }

    @Test
    void validates2026TaxRulesSepaAndParagraph63Abs5Ao() throws Exception {
        File tmpDir = Files.createTempDirectory("yabs-verein-val").toFile();
        ExtensionManager mgr = new ExtensionManager(tmpDir, null);
        assertTrue(mgr.start(mgr.register(new VereinPlugin())));
        ExtensionContext deCtx = stubCtx(tmpDir, mgr, "de");

        Settings s = new Settings();
        s.bescheidType = VereinStore.BESCHEID_FREISTELLUNG;
        s.freistellungDate = "10.01.2020"; // > 5 years old on 2026-11-19
        s.creditorId = "";
        s.duesTaxDeductible = false;
        s.kleinunternehmer = false;

        MemberData m = new MemberData();
        m.sepaMandateRef = "";

        InvoiceData d = new InvoiceData();
        d.taxSphere = VereinStore.SPHERE_IDEELL;
        d.sepaDirectDebit = true;
        d.isDonation = true;
        d.donationKind = VereinStore.DONATION_MITGLIEDSBEITRAG;

        LocalDate today = LocalDate.of(2026, 11, 19);
        List<String> warnings = VereinValidator.validate(
                m, d, Collections.singletonList(new BigDecimal("19")), new BigDecimal("150.00"), s, today, deCtx);
        // Expect: Ideell VAT > 0, SEPA mandate missing, Creditor ID missing, Freistellung expired, dues not deductible, <= 300 EUR hint
        assertEquals(6, warnings.size(), "Expected 6 compliance warnings: " + warnings);

        // Verify § 60a AO 3-year expiry check
        s.bescheidType = VereinStore.BESCHEID_60A;
        s.freistellungDate = "01.10.2023";
        assertTrue(VereinValidator.isBescheidExpired(s, today));
        s.freistellungDate = "01.12.2024";
        assertFalse(VereinValidator.isBescheidExpired(s, today));

        // Clean valid donation > 300 EUR with 0% VAT and valid SEPA
        s.creditorId = "DE98ZZZ09999999999";
        m.sepaMandateRef = "M-2026-001";
        m.sepaMandateDate = "2026-01-15";
        d.donationKind = VereinStore.DONATION_SPENDE;
        List<String> ok = VereinValidator.validate(
                m, d, Collections.singletonList(BigDecimal.ZERO), new BigDecimal("500.00"), s, today, deCtx);
        assertTrue(ok.isEmpty(), "Expected no warnings for valid donation: " + ok);
    }

    @Test
    void templateFieldsAndSphereCsvReportWork() throws Exception {
        File tmpDir = Files.createTempDirectory("yabs-verein-tpl").toFile();
        ExtensionManager mgr = new ExtensionManager(tmpDir, null);
        assertTrue(mgr.start(mgr.register(new VereinPlugin())));
        ExtensionContext deCtx = stubCtx(tmpDir, mgr, "de");

        Settings s = new Settings();
        s.creditorId = "DE98ZZZ09999999999";
        s.finanzamt = "Finanzamt Berlin";
        s.taxNumber = "27/640/12345";
        s.freistellungDate = "15.03.2024";
        s.freistellungPeriod = "2021 bis 2023";
        s.purposeClause = "F\u00f6rderung des Sports";

        MemberData m = new MemberData();
        m.memberId = "M-042";
        m.status = VereinStore.STATUS_ACTIVE;
        m.sepaMandateRef = "SEPA-042";
        m.sepaMandateDate = "2024-02-01";

        InvoiceData d = new InvoiceData();
        d.taxSphere = VereinStore.SPHERE_IDEELL;
        d.fiscalYear = "2026";
        d.sepaDirectDebit = true;
        d.waiverReimbursement = true;

        Map<String, Object> fields = VereinValidator.templateFields(m, d, new BigDecimal("250.00"), s, deCtx);
        assertEquals("M-042", fields.get("ev.memberId"));
        assertEquals("zweihundertf\u00fcnfzig Euro", fields.get("ev.amountInWords"));
        assertTrue(String.valueOf(fields.get("ev.sepaPreNotification")).contains("DE98ZZZ09999999999"));
        assertTrue(String.valueOf(fields.get("ev.waiverNotice")).contains("Ja"));
        assertEquals("Ja \u2611   Nein \u2610", fields.get("ev.waiverCheckboxes"));
        String exClause = String.valueOf(fields.get("ev.exemptionClause"));
        assertTrue(exClause.contains("\u00a7 5 Abs. 1 Nr. 9 des K\u00f6rperschaftsteuergesetzes"));
        assertTrue(exClause.contains("\u00a7 3 Nr. 6 des Gewerbesteuergesetzes"));
        assertTrue(String.valueOf(fields.get("ev.membershipDuesClause")).contains("\u00a7 10b Abs. 1 Satz 8"));

        s.bescheidType = VereinStore.BESCHEID_60A;
        assertTrue(VereinValidator.buildBmfExemptionClause(s).contains("\u00a7 60a AO gesondert festgestellt"));

        try (InputStream in = VereinPlugin.class.getResourceAsStream(VereinPlugin.TEMPLATE_RESOURCES[0])) {
            String xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(xml.contains("\u00a7 9 Abs. 3 KStG, \u00a7 9 Nr. 5 GewStG"));
            assertTrue(xml.contains("\u00a7 63 Abs. 5 AO"));
            assertTrue(xml.contains("[ev.exemptionClause]"));
        }

        Map<String, BigDecimal> totals = new LinkedHashMap<String, BigDecimal>();
        totals.put(VereinStore.SPHERE_IDEELL, new BigDecimal("12000.00"));
        totals.put(VereinStore.SPHERE_WIRTSCHAFTLICH, new BigDecimal("52000.00"));
        String csv = VereinValidator.buildSphereReportCsv(totals, deCtx);
        assertTrue(csv.contains("Ideeller Bereich;12000,00"));
        assertTrue(csv.contains("Wirtschaftlicher Gesch\u00e4ftsbetrieb;52000,00"));
        assertTrue(totals.get(VereinStore.SPHERE_WIRTSCHAFTLICH).compareTo(VereinValidator.COMMERCIAL_THRESHOLD_2026) > 0);
    }

    @Test
    void h2SchemaAndPersistenceRoundTrip() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:verein_test;DB_CLOSE_DELAY=-1", "sa", "")) {
            VereinStore.initSchema(conn);
            VereinStore.initSchema(conn); // idempotent
            assertEquals(VereinStore.SCHEMA_VERSION, VereinStore.getSchemaVersion(conn));

            MemberData m = new MemberData();
            m.contactId = 10;
            m.memberId = "M-100";
            m.status = VereinStore.STATUS_FAMILY;
            m.tierSku = "BEITRAG-FAMILIE";
            m.joinDate = "2024-01-01";
            m.sepaMandateRef = "REF-100";
            m.sepaMandateDate = "2024-01-02";
            VereinStore.saveMember(conn, m);

            MemberData loadedM = VereinStore.loadMember(conn, 10);
            assertNotNull(loadedM);
            assertEquals("M-100", loadedM.memberId);
            assertEquals(VereinStore.STATUS_FAMILY, loadedM.status);
            assertEquals("REF-100", loadedM.sepaMandateRef);

            InvoiceData d = new InvoiceData();
            d.itemId = 500;
            d.taxSphere = VereinStore.SPHERE_ZWECKBETRIEB;
            d.fiscalYear = "2026";
            d.isDonation = true;
            d.donationKind = VereinStore.DONATION_SPENDE;
            d.waiverReimbursement = true;
            d.sepaDirectDebit = false;
            VereinStore.saveInvoice(conn, d);

            assertTrue(VereinStore.hasInvoice(conn, 500));
            InvoiceData loadedD = VereinStore.loadInvoice(conn, 500);
            assertEquals(VereinStore.SPHERE_ZWECKBETRIEB, loadedD.taxSphere);
            assertTrue(loadedD.isDonation);
            assertTrue(loadedD.waiverReimbursement);
        }
    }

    @Test
    void sepaBatchUsesMemberMandatesAndPassesPain00800108Xsd() {
        mpv5.utils.export.SepaDirectDebit.Creditor creditor = new mpv5.utils.export.SepaDirectDebit.Creditor(
                "Turnverein Musterstadt e.V.",
                "DE89370400440532013000",
                "COBADEFFXXX",
                "DE98ZZZ09999999999");

        MemberData m1 = new MemberData();
        m1.memberId = "M-001";
        m1.sepaMandateRef = "SEPA-M-001";
        m1.sepaMandateDate = "2024-02-15";

        MemberData m2 = new MemberData();
        m2.memberId = "M-002";
        m2.sepaMandateRef = "SEPA-M-002";
        m2.sepaMandateDate = "10.03.2025";

        MemberData missingMandate = new MemberData();
        missingMandate.memberId = "M-003";

        mpv5.utils.export.SepaDirectDebit.Debit d1 = VereinValidator.buildMemberDebit(
                "RE-2026-01", "Beitrag Aktiv", new BigDecimal("120.00"),
                "Max Mustermann", "DE44500105175407324931", "INGDDEFFXXX", m1, "2026");
        mpv5.utils.export.SepaDirectDebit.Debit d2 = VereinValidator.buildMemberDebit(
                "RE-2026-02", "Beitrag Jugend", new BigDecimal("60.00"),
                "Anna Schmidt", "DE89370400440532013000", "", m2, "2026");
        mpv5.utils.export.SepaDirectDebit.Debit skipped = VereinValidator.buildMemberDebit(
                "RE-2026-03", "Beitrag Aktiv", new BigDecimal("120.00"),
                "Ohne Mandat", "DE44500105175407324931", "", missingMandate, "2026");

        assertNotNull(d1);
        assertNotNull(d2);
        assertNull(skipped, "Member without SEPA mandate must be skipped");

        String xml = VereinValidator.buildSepaBatchXml(
                creditor,
                java.util.Arrays.asList(d1, d2),
                LocalDate.of(2026, 12, 4),
                java.time.LocalDateTime.of(2026, 11, 19, 14, 0, 0));

        assertTrue(xml.contains("<BtchBookg>true</BtchBookg>"));
        assertTrue(xml.contains("<NbOfTxs>2</NbOfTxs>"));
        assertTrue(xml.contains("<CtrlSum>180.00</CtrlSum>"));
        assertTrue(xml.contains("<MndtId>SEPA-M-001</MndtId>"));
        assertTrue(xml.contains("<DtOfSgntr>2024-02-15</DtOfSgntr>"));
        assertTrue(xml.contains("<MndtId>SEPA-M-002</MndtId>"));
        assertTrue(xml.contains("<DtOfSgntr>2025-03-10</DtOfSgntr>"));
    }

    private static ExtensionContext stubCtx(File tmpDir, ExtensionManager mgr, String lang) {
        return new ExtensionContext() {
            @Override public void addCommand(String t, String d, Runnable a) {}
            @Override public void addSettingsPage(String t, String d, java.util.function.Supplier<javax.swing.JComponent> p) {}
            @Override public void addSearchProvider(java.util.function.Function<String, List<mpv5.plugin.api.SearchResult>> p) {}
            @Override public void onActivity(java.util.function.Consumer<mpv5.handler.Activity.Entry> l) {}
            @Override public void addRecordModifier(mpv5.db.common.DatabaseObjectModifier m) {}
            @Override public void openTab(String t, javax.swing.JComponent c) {}
            @Override public void notify(String m) {}
            @Override public mpv5.plugin.api.PluginSettings settings() { return null; }
            @Override public File dataFolder() { return tmpDir; }
            @Override public <T> void background(java.util.function.Supplier<T> w, java.util.function.Consumer<T> t) {}
            @Override public String tr(String key, Object... args) {
                return mgr.translateForLocale(VereinPlugin.ID, lang, key, key, args);
            }
        };
    }

    private static Properties loadUtf8Props(String resourcePath) throws Exception {
        Properties p = new Properties();
        try (InputStream in = VereinPlugin.class.getResourceAsStream(resourcePath)) {
            assertNotNull(in, "Missing resource: " + resourcePath);
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return p;
    }
}
