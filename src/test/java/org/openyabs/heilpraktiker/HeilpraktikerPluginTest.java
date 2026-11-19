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
        }
    }
}
