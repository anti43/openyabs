package org.openyabs.heilpraktiker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import mpv5.plugin.api.YabsExtension;
import org.junit.jupiter.api.Test;
import org.openyabs.heilpraktiker.GebuehCatalog.Entry;
import org.openyabs.heilpraktiker.GebuehCatalog.ParsedCode;
import org.openyabs.heilpraktiker.GebuehCatalog.PriceMode;
import org.openyabs.heilpraktiker.HeilpraktikerStore.InvoiceData;
import org.openyabs.heilpraktiker.HeilpraktikerStore.PatientData;
import org.openyabs.heilpraktiker.HeilpraktikerStore.Settings;
import org.openyabs.heilpraktiker.HeilpraktikerValidator.LineItem;

/**
 * Tests for the Heilpraktiker & GebüH 85/2002 plugin.
 */
public class HeilpraktikerPluginTest {

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    @Test
    public void pluginIsDiscoveredByServiceLoader() {
        boolean found = false;
        for (YabsExtension ext : ServiceLoader.load(YabsExtension.class)) {
            if (HeilpraktikerPlugin.ID.equals(ext.id())) {
                found = true;
                assertEquals("Heilpraktiker & Geb\u00fcH", ext.name());
            }
        }
        assertTrue(found, "HeilpraktikerPlugin must be registered in META-INF/services/mpv5.plugin.api.YabsExtension");
    }

    @Test
    public void officialGebuehCatalogHasAll144EntriesAndCorrectFees() throws Exception {
        GebuehCatalog cat = GebuehCatalog.loadDefault();
        assertEquals(144, cat.size(), "Official GebüH 85/2002 has 144 entries");
        assertEquals(27, cat.groups().size(), "Official GebüH 85/2002 has 27 groups");

        // Unassigned / non-existent codes must not be present
        assertNull(cat.get("12.3"));
        assertNull(cat.get("12.6"));
        assertNull(cat.get("39.3"));
        assertNull(cat.get("40"));
        assertNull(cat.get("45"));

        // Spot-check official GebüH 85/2002 entries & fee ranges
        Entry z1 = cat.get("1");
        assertNotNull(z1);
        assertEquals(bd("12.30"), z1.minFee);
        assertEquals(bd("20.54"), z1.maxFee);

        Entry z2 = cat.get("2");
        assertNotNull(z2);
        assertEquals(bd("15.40"), z2.minFee);
        assertEquals(bd("40.96"), z2.maxFee);

        Entry z3 = cat.get("3");
        assertNotNull(z3);
        assertTrue(z3.aloneOnly);
        assertEquals(bd("4.50"), z3.maxFee);

        Entry z4 = cat.get("4");
        assertNotNull(z4);
        assertEquals(Arrays.asList("1", "17.1"), z4.onlyAllowedWith);

        Entry z17 = cat.get("17.1");
        assertNotNull(z17);
        assertTrue(z17.title.contains("Neurologische"));

        Entry z19_1 = cat.get("19.1");
        Entry z19_2 = cat.get("19.2");
        assertEquals(bd("26.04"), z19_1.maxFee);
        assertEquals(bd("46.02"), z19_2.maxFee);

        Entry z25_6 = cat.get("25.6");
        assertTrue(z25_6.title.contains("Huneke"));

        Entry z26_1 = cat.get("26.1");
        assertTrue(z26_1.title.contains("Blutentnahme"));
        Entry z27_1 = cat.get("27.1");
        assertTrue(z27_1.title.contains("Blutegel"));

        Entry z34_2 = cat.get("34.2");
        assertTrue(z34_2.title.contains("chiropraktischer"));
        assertEquals(1, z34_2.maxPerSession);

        Entry z35_1 = cat.get("35.1");
        assertTrue(z35_1.title.contains("Osteopathische"));

        Entry z39_13 = cat.get("39.13");
        assertNotNull(z39_13);
        assertTrue(z39_13.title.contains("Ultraschall"));

        // Fee mode calculation
        assertEquals(bd("12.30"), z1.feeFor(PriceMode.MIN));
        assertEquals(bd("16.42"), z1.feeFor(PriceMode.MID));
        assertEquals(bd("20.54"), z1.feeFor(PriceMode.MAX));
    }

    @Test
    public void parsesStandardAndAnalogGebuehCodes() throws Exception {
        GebuehCatalog cat = GebuehCatalog.loadDefault();

        ParsedCode p1 = cat.parse("GEBUEH-19.2");
        assertNotNull(p1);
        assertEquals("19.2", p1.code);
        assertFalse(p1.analog);

        ParsedCode p2 = cat.parse("A 35.1");
        assertNotNull(p2);
        assertEquals("35.1", p2.code);
        assertTrue(p2.analog);

        ParsedCode p3 = cat.parse("GEBUEH-A35.1");
        assertNotNull(p3);
        assertEquals("35.1", p3.code);
        assertTrue(p3.analog);

        ParsedCode p4 = cat.parse("Geb\u00fcH 4: Eingehende Beratung");
        assertNotNull(p4);
        assertEquals("4", p4.code);
        assertFalse(p4.analog);

        assertNull(cat.parse("Wellness-Massage 60 Min."));
    }

    @Test
    public void validatesMandatoryFieldsVatBoundsAndCombinationRules() throws Exception {
        GebuehCatalog cat = GebuehCatalog.loadDefault();

        // 1. Clean invoice (Ziffer 1 + Ziffer 4 is explicitly allowed!)
        PatientData patient = new PatientData();
        patient.birthDate = "14.05.1980";
        patient.insuranceType = "PKV";
        patient.honorarvereinbarung = false;

        InvoiceData inv = new InvoiceData();
        inv.heilbehandlung = true;
        inv.icd10Code = "M54.5";
        inv.diagnosisText = "Kreuzschmerz";
        inv.treatmentDates = "10.03.2026";

        List<LineItem> cleanLines = Arrays.asList(
                new LineItem("GEBUEH-1", "Geb\u00fcH 1: Eingehende Untersuchung", bd("20.54"), BigDecimal.ZERO),
                new LineItem("GEBUEH-4", "Geb\u00fcH 4: Eingehende Beratung", bd("21.98"), BigDecimal.ZERO)
        );
        assertTrue(HeilpraktikerValidator.validate(patient, inv, cleanLines, cat).isEmpty(),
                "Ziffer 1 + Ziffer 4 within bounds and with mandatory fields must pass cleanly");

        // 2. Missing diagnosis, DOB, treatment dates, VAT error, fee over max without Honorarvereinbarung
        PatientData emptyPatient = new PatientData();
        InvoiceData emptyInv = new InvoiceData();
        emptyInv.heilbehandlung = true;
        List<LineItem> badLines = Arrays.asList(
                new LineItem("A 35.1", "Osteopathie analog", bd("85.00"), bd("19"))
        );
        List<String> w = HeilpraktikerValidator.validate(emptyPatient, emptyInv, badLines, cat);
        assertTrue(w.stream().anyMatch(s -> s.contains("Diagnose")));
        assertTrue(w.stream().anyMatch(s -> s.contains("Geburtsdatum")));
        assertTrue(w.stream().anyMatch(s -> s.contains("Behandlungsdatum")));
        assertTrue(w.stream().anyMatch(s -> s.contains("4 Nr. 14")));
        assertTrue(w.stream().anyMatch(s -> s.contains("Honorarvereinbarung")));

        // With signed Honorarvereinbarung, exceeding maxFee does not warn
        emptyPatient.honorarvereinbarung = true;
        List<String> wWithAgreement = HeilpraktikerValidator.validate(emptyPatient, emptyInv, badLines, cat);
        assertFalse(wWithAgreement.stream().anyMatch(s -> s.contains("Honorarvereinbarung")));

        // 3. Combination rules: Ziffer 4 + 21.1, Ziffer 3 + 21.1, Ziffer 14.1 + 14.2, Ziffer 12.7 + 12.9, Ziffer 34.2 x2
        List<LineItem> comboLines = Arrays.asList(
                new LineItem("GEBUEH-3", "Kurze Info", bd("4.50"), BigDecimal.ZERO),
                new LineItem("GEBUEH-4", "Eingehende Beratung", bd("20.00"), BigDecimal.ZERO),
                new LineItem("GEBUEH-21.1", "Akupunktur", bd("25.00"), BigDecimal.ZERO),
                new LineItem("GEBUEH-14.1", "Augenvordergrund", bd("10.00"), BigDecimal.ZERO),
                new LineItem("GEBUEH-14.2", "Augenhintergrund", bd("10.00"), BigDecimal.ZERO),
                new LineItem("GEBUEH-12.7", "Blutstatus", bd("18.00"), BigDecimal.ZERO),
                new LineItem("GEBUEH-12.9", "H\u00e4moglobin", bd("5.50"), BigDecimal.ZERO),
                new LineItem("GEBUEH-34.2", "Chiropraktik WS", bd("18.00"), BigDecimal.ZERO),
                new LineItem("GEBUEH-34.2", "Chiropraktik WS", bd("18.00"), BigDecimal.ZERO)
        );
        List<String> cw = HeilpraktikerValidator.validate(patient, inv, comboLines, cat);
        assertTrue(cw.stream().anyMatch(s -> s.contains("Geb\u00fcH 3") && s.contains("alleinige")));
        assertTrue(cw.stream().anyMatch(s -> s.contains("Geb\u00fcH 4") && s.contains("1 / 17.1")));
        assertTrue(cw.stream().anyMatch(s -> s.contains("14.1") && s.contains("14.2")));
        assertTrue(cw.stream().anyMatch(s -> s.contains("12.7") && s.contains("12.9")));
        assertTrue(cw.stream().anyMatch(s -> s.contains("34.2") && s.contains("Sitzung")));
    }

    @Test
    public void buildsTemplateFieldsAndShipsValidFodtTemplate() throws Exception {
        PatientData p = new PatientData();
        p.birthDate = "01.02.1985";
        p.insuranceType = "Beihilfe";
        p.honorarvereinbarung = true;

        InvoiceData inv = new InvoiceData();
        inv.icd10Code = "R51";
        inv.diagnosisText = "Kopfschmerz";
        inv.treatmentDates = "03.04.2026, 10.04.2026";
        inv.heilbehandlung = true;

        Settings s = new Settings();
        s.practitionerTitle = "Heilpraktikerin";
        s.licenseAuthority = "Erlaubnis nach \u00a7 1 HeilprG, Gesundheitsamt M\u00fcnchen";

        Map<String, Object> fields = HeilpraktikerValidator.templateFields(p, inv, s);
        assertEquals("01.02.1985", fields.get("hp.patientDob"));
        assertEquals("Beihilfe", fields.get("hp.insuranceType"));
        assertEquals("R51", fields.get("hp.icd10"));
        assertEquals("Kopfschmerz", fields.get("hp.diagnosis"));
        assertEquals("03.04.2026, 10.04.2026", fields.get("hp.treatmentDates"));
        assertTrue(String.valueOf(fields.get("hp.ustgNotice")).contains("\u00a7 4 Nr. 14"));

        // Non-medical wellness invoice must omit § 4 Nr. 14 notice
        inv.heilbehandlung = false;
        assertEquals("", HeilpraktikerValidator.templateFields(p, inv, s).get("hp.ustgNotice"));

        // Bundled .fodt template exists and references the hp.* placeholders
        try (InputStream in = HeilpraktikerPlugin.class.getResourceAsStream(HeilpraktikerPlugin.TEMPLATE_RESOURCE)) {
            assertNotNull(in, "Bundled FODT template must exist");
            String xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(xml.contains("[hp.patientDob]"));
            assertTrue(xml.contains("[hp.icd10]"));
            assertTrue(xml.contains("[hp.diagnosis]"));
            assertTrue(xml.contains("[hp.ustgNotice]"));
            assertTrue(xml.contains("[hp.licenseAuthority]"));
            assertTrue(xml.contains("630a BGB"));
        }
    }

    @Test
    public void storeCreatesTablesAndPersistsPatientAndInvoiceData() throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:h2:mem:hpstore;MODE=Derby", "sa", "")) {
            HeilpraktikerStore.initSchema(c);
            // Calling initSchema twice must be idempotent
            HeilpraktikerStore.initSchema(c);

            PatientData p = new PatientData();
            p.contactId = 42;
            p.birthDate = "12.08.1977";
            p.insuranceType = "PKV";
            p.honorarvereinbarung = true;
            p.soapNotes = "S: Rueckenschmerzen\nO: Verspannung LWS\nA: M54.5\nP: Akupunktur";
            HeilpraktikerStore.savePatient(c, p);

            PatientData loadedP = HeilpraktikerStore.loadPatient(c, 42);
            assertEquals("12.08.1977", loadedP.birthDate);
            assertEquals("PKV", loadedP.insuranceType);
            assertTrue(loadedP.honorarvereinbarung);
            assertTrue(loadedP.soapNotes.contains("M54.5"));

            // Update existing row
            loadedP.insuranceType = "PKV + Beihilfe";
            HeilpraktikerStore.savePatient(c, loadedP);
            assertEquals("PKV + Beihilfe", HeilpraktikerStore.loadPatient(c, 42).insuranceType);

            InvoiceData d = new InvoiceData();
            d.itemId = 101;
            d.icd10Code = "M54.5";
            d.diagnosisText = "Kreuzschmerz";
            d.heilbehandlung = true;
            d.treatmentDates = "10.03.2026";
            HeilpraktikerStore.saveInvoice(c, d);

            assertTrue(HeilpraktikerStore.hasInvoiceData(c, 101));
            InvoiceData loadedD = HeilpraktikerStore.loadInvoice(c, 101);
            assertEquals("M54.5", loadedD.icd10Code);
            assertEquals("Kreuzschmerz", loadedD.diagnosisText);
            assertTrue(loadedD.heilbehandlung);
            assertEquals("10.03.2026", loadedD.treatmentDates);
            assertEquals("2036-03-10", loadedD.retentionUntil);
        }
    }

    @Test
    public void diagnosisPrintingDefaultsAndOverridesRespectGdprArt9() {
        Settings s = new Settings();

        // PKV / Beihilfe default to printing diagnosis
        PatientData pkv = new PatientData();
        pkv.insuranceType = "PKV";
        InvoiceData inv = new InvoiceData();
        inv.icd10Code = "M54.5";
        inv.diagnosisText = "Kreuzschmerz";

        assertTrue(HeilpraktikerValidator.shouldPrintDiagnosis(pkv, inv));
        assertFalse(HeilpraktikerValidator.needsPrintDiagnosisPrompt(pkv, inv));
        Map<String, Object> pkvFields = HeilpraktikerValidator.templateFields(pkv, inv, s);
        assertEquals("M54.5", pkvFields.get("hp.icd10"));
        assertEquals("Kreuzschmerz", pkvFields.get("hp.diagnosis"));
        assertEquals("ja", pkvFields.get("hp.printDiagnosis"));

        // Explicit opt-out on invoice hides diagnosis even for PKV
        inv.printDiagnosis = Boolean.FALSE;
        assertFalse(HeilpraktikerValidator.shouldPrintDiagnosis(pkv, inv));
        Map<String, Object> hiddenFields = HeilpraktikerValidator.templateFields(pkv, inv, s);
        assertEquals("", hiddenFields.get("hp.icd10"));
        assertEquals("", hiddenFields.get("hp.diagnosis"));
        assertEquals("nein", hiddenFields.get("hp.printDiagnosis"));

        // Selbstzahler defaults to NOT printing diagnosis and triggers prompt when undecided
        PatientData selfPay = new PatientData();
        selfPay.insuranceType = "Selbstzahler";
        InvoiceData selfInv = new InvoiceData();
        selfInv.icd10Code = "F43.0";
        selfInv.diagnosisText = "Akute Belastungsreaktion";

        assertFalse(HeilpraktikerValidator.shouldPrintDiagnosis(selfPay, selfInv));
        assertTrue(HeilpraktikerValidator.needsPrintDiagnosisPrompt(selfPay, selfInv));
        Map<String, Object> selfFields = HeilpraktikerValidator.templateFields(selfPay, selfInv, s);
        assertEquals("", selfFields.get("hp.icd10"));
        assertEquals("", selfFields.get("hp.diagnosis"));

        // Explicit opt-in on invoice prints diagnosis for Selbstzahler and stops prompting
        selfInv.printDiagnosis = Boolean.TRUE;
        assertTrue(HeilpraktikerValidator.shouldPrintDiagnosis(selfPay, selfInv));
        assertFalse(HeilpraktikerValidator.needsPrintDiagnosisPrompt(selfPay, selfInv));
        assertEquals("F43.0", HeilpraktikerValidator.templateFields(selfPay, selfInv, s).get("hp.icd10"));
    }

    @Test
    public void warnsBeforeUnencryptedEmailOnlyWhenDiagnosisIsPrinted() {
        PatientData pkv = new PatientData();
        pkv.insuranceType = "PKV";
        InvoiceData inv = new InvoiceData();
        inv.icd10Code = "M54.5";
        inv.diagnosisText = "Kreuzschmerz";

        String warn = HeilpraktikerValidator.mailWarning(pkv, inv);
        assertNotNull(warn);
        assertTrue(warn.contains("Art. 9 DSGVO"));
        assertTrue(warn.contains("203 StGB"));

        // Turning off diagnosis printing suppresses the unencrypted e-mail warning
        inv.printDiagnosis = Boolean.FALSE;
        assertNull(HeilpraktikerValidator.mailWarning(pkv, inv));

        // Selbstzahler without explicit opt-in does not print diagnosis -> no warning
        PatientData selfPay = new PatientData();
        selfPay.insuranceType = "Selbstzahler";
        InvoiceData selfInv = new InvoiceData();
        selfInv.diagnosisText = "Kopfschmerz";
        assertNull(HeilpraktikerValidator.mailWarning(selfPay, selfInv));
    }

    @Test
    public void restrictsSoapNotesAccessByRoleAndUsername() {
        Settings restricted = new Settings();
        restricted.restrictSoapAccess = true;
        restricted.soapAllowedUsers = "heilpraktiker1, dr_mueller";

        // Admin always allowed
        assertTrue(HeilpraktikerSecurity.canAccessSoapNotes("admin", mpv5.usermanagement.MPSecurityManager.RIGHT_TO_ADMINISTRATE, false, restricted));
        // Regular user without role or allowlist entry denied
        assertFalse(HeilpraktikerSecurity.canAccessSoapNotes("buchhaltung", mpv5.usermanagement.MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE, false, restricted));
        // User with explicit per-user role property allowed
        assertTrue(HeilpraktikerSecurity.canAccessSoapNotes("praxis_assistenz", mpv5.usermanagement.MPSecurityManager.RIGHT_TO_EDIT, true, restricted));
        // User listed in soapAllowedUsers (case-insensitive) allowed
        assertTrue(HeilpraktikerSecurity.canAccessSoapNotes("DR_MUELLER", mpv5.usermanagement.MPSecurityManager.RIGHT_TO_VIEW, false, restricted));

        // When restriction is disabled, everyone can access
        restricted.restrictSoapAccess = false;
        assertTrue(HeilpraktikerSecurity.canAccessSoapNotes("buchhaltung", mpv5.usermanagement.MPSecurityManager.RIGHT_TO_VIEW, false, restricted));
    }

    @Test
    public void encryptsSoapNotesAtRestAndPreservesOnNonSoapSave() throws Exception {
        javax.crypto.SecretKey key = HeilpraktikerCrypto.generateKey();
        String secret = "S: Chronische Migr\u00e4ne (§ 630f BGB)\nO: Verspannung HWS";
        String enc1 = HeilpraktikerCrypto.encrypt(secret, key);
        String enc2 = HeilpraktikerCrypto.encrypt(secret, key);
        assertTrue(HeilpraktikerCrypto.isEncrypted(enc1));
        assertFalse(enc1.contains("Migr\u00e4ne"));
        assertFalse(enc1.equals(enc2), "Random IV must produce distinct ciphertexts");
        assertEquals(secret, HeilpraktikerCrypto.decrypt(enc1, key));
        assertEquals("Legacy plain text", HeilpraktikerCrypto.decrypt("Legacy plain text", key));

        // Key file persistence round-trip
        java.nio.file.Path tmpKey = java.nio.file.Files.createTempFile("hp-soap-key-", ".key");
        java.nio.file.Files.delete(tmpKey);
        try {
            javax.crypto.SecretKey created = HeilpraktikerCrypto.loadOrCreateKey(tmpKey.toFile());
            javax.crypto.SecretKey reloaded = HeilpraktikerCrypto.loadOrCreateKey(tmpKey.toFile());
            assertEquals(secret, HeilpraktikerCrypto.decrypt(HeilpraktikerCrypto.encrypt(secret, created), reloaded));
        } finally {
            java.nio.file.Files.deleteIfExists(tmpKey);
        }

        // Database persistence with encryption + savePatientWithoutSoap preserving ciphertext
        try (Connection c = DriverManager.getConnection("jdbc:h2:mem:hpenc;MODE=Derby", "sa", "")) {
            HeilpraktikerStore.initSchema(c);
            PatientData p = new PatientData();
            p.contactId = 7;
            p.birthDate = "01.01.1980";
            p.insuranceType = "PKV";
            p.soapNotes = secret;
            HeilpraktikerStore.savePatient(c, p, key);

            // Raw column in DB must be encrypted, not plaintext
            try (java.sql.PreparedStatement ps = c.prepareStatement("SELECT soap_notes FROM hp_patient_data WHERE contact_id = 7");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                String raw = rs.getString(1);
                assertTrue(HeilpraktikerCrypto.isEncrypted(raw));
                assertFalse(raw.contains("Migr\u00e4ne"));
            }

            // Loading with key decrypts back to original text
            PatientData loaded = HeilpraktikerStore.loadPatient(c, 7, key);
            assertEquals(secret, loaded.soapNotes);

            // Non-SOAP user editing insurance type must NOT erase encrypted SOAP notes
            loaded.insuranceType = "Beihilfe";
            loaded.soapNotes = "SHOULD_NOT_OVERWRITE";
            HeilpraktikerStore.savePatientWithoutSoap(c, loaded);
            PatientData afterAdminEdit = HeilpraktikerStore.loadPatient(c, 7, key);
            assertEquals("Beihilfe", afterAdminEdit.insuranceType);
            assertEquals(secret, afterAdminEdit.soapNotes);
        }
    }

    @Test
    public void tracksTenYearRetentionAndPurgesExpiredHealthData() throws Exception {
        assertEquals(java.time.LocalDate.of(2026, 4, 10),
                HeilpraktikerValidator.parseLatestTreatmentDate("03.04.2026, 10.04.2026"));
        assertEquals(java.time.LocalDate.of(2036, 4, 10),
                HeilpraktikerValidator.calculateTreatmentRetentionUntil(java.time.LocalDate.of(2026, 4, 10)));

        try (Connection c = DriverManager.getConnection("jdbc:h2:mem:hpretention;MODE=Derby", "sa", "")) {
            // Also verify schema migration from legacy tables without new columns
            try (java.sql.Statement st = c.createStatement()) {
                st.executeUpdate("CREATE TABLE hp_patient_data (contact_id INTEGER PRIMARY KEY, birth_date VARCHAR(20), insurance_type VARCHAR(40), honorarvereinbarung INTEGER, soap_notes VARCHAR(4000), updated_at TIMESTAMP)");
                st.executeUpdate("CREATE TABLE hp_invoice_data (item_id INTEGER PRIMARY KEY, icd10_code VARCHAR(80), diagnosis_text VARCHAR(500), is_heilbehandlung INTEGER, treatment_dates VARCHAR(500), updated_at TIMESTAMP)");
            }
            HeilpraktikerStore.initSchema(c);

            // Expired patient (treatment 2014 -> retention until 2024-05-01)
            PatientData oldP = new PatientData();
            oldP.contactId = 1;
            oldP.birthDate = "10.10.1970";
            oldP.lastTreatmentDate = "2014-05-01";
            oldP.soapNotes = "Alte Behandlungsnotiz";
            HeilpraktikerStore.savePatient(c, oldP, null);

            // Active patient (treatment 2025 -> retention until 2035-06-01)
            PatientData freshP = new PatientData();
            freshP.contactId = 2;
            freshP.birthDate = "11.11.1990";
            freshP.lastTreatmentDate = "2025-06-01";
            freshP.soapNotes = "Aktuelle Notiz";
            HeilpraktikerStore.savePatient(c, freshP, null);

            // Expired invoice (treatment 01.03.2015 -> retention until 2025-03-01)
            InvoiceData oldInv = new InvoiceData();
            oldInv.itemId = 10;
            oldInv.icd10Code = "M54.5";
            oldInv.diagnosisText = "Kreuzschmerz";
            oldInv.treatmentDates = "01.03.2015";
            oldInv.heilbehandlung = true;
            HeilpraktikerStore.saveInvoice(c, oldInv);

            java.time.LocalDate today = java.time.LocalDate.of(2026, 11, 20);
            assertEquals(1, HeilpraktikerStore.findExpiredPatients(c, today).size());
            assertEquals(1, HeilpraktikerStore.findExpiredInvoices(c, today).size());

            // Anonymise expired records
            HeilpraktikerStore.PurgeResult res = HeilpraktikerStore.purgeExpired(c, today, false);
            assertEquals(1, res.patientsAffected);
            assertEquals(1, res.invoicesAffected);

            // Expired patient & invoice are anonymised; fresh patient untouched
            PatientData afterOldP = HeilpraktikerStore.loadPatient(c, 1, null);
            assertEquals("", afterOldP.soapNotes);
            assertEquals("", afterOldP.birthDate);
            assertEquals("Aktuelle Notiz", HeilpraktikerStore.loadPatient(c, 2, null).soapNotes);

            InvoiceData afterOldInv = HeilpraktikerStore.loadInvoice(c, 10);
            assertEquals("", afterOldInv.icd10Code);
            assertEquals("", afterOldInv.diagnosisText);
            assertTrue(afterOldInv.heilbehandlung, "VAT exemption flag kept for auditability on anonymisation");
        }
    }

    @Test
    public void skipsEInvoiceForTaxExemptMedicalTreatments() {
        Settings s = new Settings();
        s.skipEInvoiceForTaxExempt = true;

        InvoiceData heil = new InvoiceData();
        heil.heilbehandlung = true;
        assertTrue(HeilpraktikerValidator.shouldSkipEInvoice(heil, true, s));
        assertFalse(HeilpraktikerValidator.shouldSkipEInvoice(heil, false, s));

        InvoiceData wellness = new InvoiceData();
        wellness.heilbehandlung = false;
        assertFalse(HeilpraktikerValidator.shouldSkipEInvoice(wellness, true, s));

        s.skipEInvoiceForTaxExempt = false;
        assertFalse(HeilpraktikerValidator.shouldSkipEInvoice(heil, true, s));
    }

    @Test
    public void registersDedicatedSetupCommand() throws Exception {
        java.io.File tmpDir = java.nio.file.Files.createTempDirectory("yabs-hp-test").toFile();
        mpv5.plugin.ExtensionManager mgr = new mpv5.plugin.ExtensionManager(tmpDir, null);
        mpv5.plugin.ExtensionManager.Installed inst = mgr.register(new HeilpraktikerPlugin());
        assertTrue(mgr.start(inst));
        assertEquals(3, mgr.commands().size(), "Expected Setup + Check + GDPR purge commands");
        assertEquals("Heilpraktiker: Setup", mgr.commands().get(0).title);
        assertEquals(1, mgr.pages().size());
        mgr.stop(inst);
    }
}
