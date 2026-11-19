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
package org.openyabs.workshop;

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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import mpv5.plugin.ExtensionManager;
import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.YabsExtension;
import org.junit.jupiter.api.Test;
import org.openyabs.workshop.WorkshopStore.JobData;
import org.openyabs.workshop.WorkshopStore.Settings;
import org.openyabs.workshop.WorkshopStore.TireStorage;
import org.openyabs.workshop.WorkshopStore.Vehicle;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the generic core plugin i18n API and the Car Repair Shop /
 * Kfz-Werkstatt plugin ({@link WorkshopPlugin}).
 */
class WorkshopPluginTest {

    @Test
    void serviceLoaderFindsWorkshopPluginAndAllResources() throws Exception {
        boolean found = false;
        for (YabsExtension ext : ServiceLoader.load(YabsExtension.class)) {
            if (WorkshopPlugin.ID.equals(ext.id())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "WorkshopPlugin must be discoverable via ServiceLoader");

        for (String res : WorkshopPlugin.TEMPLATE_RESOURCES) {
            try (InputStream in = WorkshopPlugin.class.getResourceAsStream(res)) {
                assertNotNull(in, "Missing bundled template resource: " + res);
                String xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(xml.contains("<office:document"), "Valid FODT root in " + res);
            }
        }

        Properties en = loadUtf8Props(WorkshopPlugin.I18N_EN);
        Properties de = loadUtf8Props(WorkshopPlugin.I18N_DE);
        assertEquals(en.stringPropertyNames(), de.stringPropertyNames(),
                "English and German workshop translation bundles must have identical keys");
        assertTrue(de.getProperty("plugin.description").contains("Arbeitswerte"));
        assertTrue(de.getProperty("tire.dialog.title").contains("\u00a7 688 BGB"));
    }

    @Test
    void corePluginI18nScopingFallbackFormattingAndCleanup() throws Exception {
        File tmpDir = Files.createTempDirectory("yabs-i18n-test").toFile();
        ExtensionManager mgr = new ExtensionManager(tmpDir, null);

        YabsExtension pluginA = new YabsExtension() {
            @Override public String id() { return "test.plugin.a"; }
            @Override public String name() { return "Plugin A"; }
            @Override public String description() { return ""; }
            @Override public String version() { return "1.0"; }
            @Override public void start(ExtensionContext yabs) {
                yabs.addTranslations("en", Map.of("title", "English A", "greet", "Hello {0} ({1})"));
                yabs.addTranslations("de", Map.of("title", "Deutsch A", "greet", "Hallo {0} ({1})"));
                yabs.addTranslations("de_AT", Map.of("title", "\u00d6sterreich A"));
            }
        };
        YabsExtension pluginB = new YabsExtension() {
            @Override public String id() { return "test.plugin.b"; }
            @Override public String name() { return "Plugin B"; }
            @Override public String description() { return ""; }
            @Override public String version() { return "1.0"; }
            @Override public void start(ExtensionContext yabs) {
                yabs.addTranslations("en", Collections.singletonMap("title", "English B"));
            }
        };

        ExtensionManager.Installed instA = mgr.register(pluginA);
        ExtensionManager.Installed instB = mgr.register(pluginB);
        assertTrue(mgr.start(instA));
        assertTrue(mgr.start(instB));

        // Plugin-ID scoping: identical short key does not collide
        assertEquals("English A", mgr.translateForLocale("test.plugin.a", "en", "title", null));
        assertEquals("English B", mgr.translateForLocale("test.plugin.b", "en", "title", null));

        // Fallback chain: exact locale -> base language -> en -> fallback -> key
        assertEquals("\u00d6sterreich A", mgr.translateForLocale("test.plugin.a", "de_AT", "title", null));
        assertEquals("Deutsch A", mgr.translateForLocale("test.plugin.a", "de_DE", "title", null));
        assertEquals("Deutsch A", mgr.translateForLocale("test.plugin.a", "DE-Yabs-2.0", "title", null));
        assertEquals("English A", mgr.translateForLocale("test.plugin.a", "fr_FR", "title", null));
        assertEquals("English A", mgr.translateForLocale("test.plugin.a", "buildin_en", "title", null));
        assertEquals("Fallback!", mgr.translateForLocale("test.plugin.a", "de", "missing.key", "Fallback!"));
        assertEquals("missing.key", mgr.translateForLocale("test.plugin.a", "de", "missing.key", null));

        // MessageFormat parameter substitution
        assertEquals("Hallo Max (42)", mgr.translateForLocale("test.plugin.a", "de", "greet", null, "Max", 42));

        // Cleanup on stop
        assertTrue(mgr.contributions("test.plugin.a") >= 3);
        mgr.stop(instA);
        assertEquals(0, mgr.contributions("test.plugin.a"));
        assertEquals("title", mgr.translateForLocale("test.plugin.a", "de", "title", null));
    }

    @Test
    void vinValidationAcceptsEuVinsAndRejectsInvalidOnes() {
        // Standard EU VW VIN (no North American 9th-digit check digit)
        assertTrue(WorkshopValidator.isValidVin("WVWZZZ1JZXW000001"));
        assertTrue(WorkshopValidator.isValidVin("wba3a5c50cf256789"));
        // Invalid length or forbidden letters I, O, Q
        assertFalse(WorkshopValidator.isValidVin("WVWZZZ1JZ"));
        assertFalse(WorkshopValidator.isValidVin("WVWZZZ1JZXW00000I"));
        assertFalse(WorkshopValidator.isValidVin("WVWZZZ1JZXW00000O"));
        assertFalse(WorkshopValidator.isValidVin("WVWZZZ1JZXW00000Q"));
        assertFalse(WorkshopValidator.isValidVin("WVWZZZ1JZ-W000001"));
    }

    @Test
    void unitAwareMileageValidationAndQuoteOverrunChecks() throws Exception {
        File tmpDir = Files.createTempDirectory("yabs-ws-val").toFile();
        ExtensionManager mgr = new ExtensionManager(tmpDir, null);
        WorkshopPlugin plugin = new WorkshopPlugin();
        ExtensionManager.Installed inst = mgr.register(plugin);
        assertTrue(mgr.start(inst));

        ExtensionContext stubCtx = new ExtensionContext() {
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
                return mgr.translateForLocale(WorkshopPlugin.ID, "en", key, key, args);
            }
        };

        Vehicle v = new Vehicle();
        v.id = 1;
        v.licensePlate = "B-AB 1234";
        v.vin = "INVALID-VIN";

        JobData job = new JobData();
        job.vehicleId = 1;
        job.plateSnapshot = "B-AB 1234";
        job.mileage = 90000;
        job.mileageUnit = "mi"; // ~144,841 km < 150,000 km previous
        job.quoteNumberSnapshot = "KV-2026-01";
        job.quoteTotalSnapshot = "1000.00";
        job.overrunApproved = false;

        Settings s = new Settings();
        s.quoteOverrunPercent = 15;

        List<String> warnings = WorkshopValidator.validate(job, v, new BigDecimal("1250.00"), 150000L, s, stubCtx);
        assertEquals(3, warnings.size(), "Expected mileage drop, invalid VIN, and quote overrun warnings: " + warnings);

        // Fix VIN, raise mileage above previous km equivalent, mark overrun approved
        v.vin = "WVWZZZ1JZXW000001";
        job.mileage = 100000; // ~160,934 km > 150,000 km
        job.overrunApproved = true;
        List<String> ok = WorkshopValidator.validate(job, v, new BigDecimal("1250.00"), 150000L, s, stubCtx);
        assertTrue(ok.isEmpty(), "Expected no warnings after fixes: " + ok);
    }

    @Test
    void inspectionDateParsingHandlesDeMonthYearAndUkDayPrecision() {
        assertEquals(LocalDate.of(2026, 11, 30), WorkshopValidator.parseInspectionDue("2026-11"));
        assertEquals(LocalDate.of(2026, 11, 30), WorkshopValidator.parseInspectionDue("11/2026"));
        assertEquals(LocalDate.of(2026, 11, 15), WorkshopValidator.parseInspectionDue("2026-11-15"));
        assertEquals(LocalDate.of(2026, 11, 15), WorkshopValidator.parseInspectionDue("15.11.2026"));
        assertTrue(WorkshopValidator.isInspectionDueSoon("2026-11-15", LocalDate.of(2026, 11, 1), 30));
        assertFalse(WorkshopValidator.isInspectionDueSoon("2027-06-01", LocalDate.of(2026, 11, 1), 30));
    }

    @Test
    void templateFieldsUseSnapshotAndExcludeInternalNotes() throws Exception {
        File tmpDir = Files.createTempDirectory("yabs-ws-tpl").toFile();
        ExtensionManager mgr = new ExtensionManager(tmpDir, null);
        WorkshopPlugin plugin = new WorkshopPlugin();
        assertTrue(mgr.start(mgr.register(plugin)));

        ExtensionContext deCtx = new ExtensionContext() {
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
                return mgr.translateForLocale(WorkshopPlugin.ID, "de", key, key, args);
            }
        };

        Vehicle v = new Vehicle();
        v.licensePlate = "M-NEW 999";
        v.vin = "WVWZZZ1JZXW000001";
        v.makeModel = "VW Golf VII";
        v.hsnTsn = "0603 / BKP";
        v.firstRegistration = "2018-04-12";
        v.mileage = 95000;
        v.mileageUnit = "km";
        v.inspectionDue = "2027-04";
        v.tireSize = "205/55 R16";

        JobData j = new JobData();
        j.plateSnapshot = "B-OLD 123";
        j.mileage = 80000;
        j.mileageUnit = "km";
        j.dropoffDate = "2026-11-10";
        j.pickupDate = "2026-11-11";
        j.complaint = "Bremsen quietschen";
        j.workSummary = "Bremsbel\u00e4ge vorne erneuert";
        j.internalNotes = "SECRET INTERNAL NOTE";
        j.oldPartsHandling = "DISPOSE";
        j.quoteNumberSnapshot = "KV-100";
        j.quoteTotalSnapshot = "450.00";

        TireStorage t = new TireStorage();
        t.season = "WINTER";
        t.storageLocation = "Regal B-12";
        t.treadFl = "6.5";
        t.treadFr = "6.5";
        t.treadRl = "7.0";
        t.treadRr = "7.0";
        t.dotCodes = "3223";
        t.rimType = "ALLOY";
        t.conditionNotes = "Felge VL leichter Kratzer";

        Settings s = new Settings();
        s.awPerHour = 12;

        Map<String, Object> fields = WorkshopValidator.templateFields(v, j, t, s, deCtx);
        assertEquals("B-OLD 123", fields.get("ws.plate"), "Snapshot plate must win over current vehicle plate");
        assertEquals("80000", fields.get("ws.mileage"), "Snapshot mileage must win");
        assertEquals("Bremsbel\u00e4ge vorne erneuert", fields.get("ws.workSummary"));
        assertEquals("12 AW/h (5 Min./AW)", fields.get("ws.awDefinition"));
        assertEquals("Regal B-12", fields.get("ws.tireStorageLocation"));
        assertEquals("6.5 / 6.5 / 7.0 / 7.0 mm", fields.get("ws.tireTreadDepth"));
        assertEquals("Felge VL leichter Kratzer", fields.get("ws.tireCondition"));
        assertFalse(fields.values().toString().contains("SECRET INTERNAL NOTE"),
                "Internal technician notes must never be exported to template placeholders");
    }

    @Test
    void h2SchemaAndPersistenceRoundTrip() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:workshop_test;DB_CLOSE_DELAY=-1", "sa", "")) {
            WorkshopStore.initSchema(conn);
            WorkshopStore.initSchema(conn); // idempotent
            assertEquals(WorkshopStore.SCHEMA_VERSION, WorkshopStore.getSchemaVersion(conn));

            Vehicle v1 = new Vehicle();
            v1.contactId = 42;
            v1.licensePlate = "HH-XY 100";
            v1.vin = "WVWZZZ1JZXW000001";
            v1.makeModel = "Audi A4";
            v1.mileage = 50000;
            v1.mileageUnit = "km";
            v1.inspectionDue = "2026-12";
            WorkshopStore.saveVehicle(conn, v1);
            assertTrue(v1.id > 0);

            Vehicle v2 = new Vehicle();
            v2.contactId = 42;
            v2.licensePlate = "HH-OLD 99";
            v2.archived = true;
            WorkshopStore.saveVehicle(conn, v2);

            assertEquals(1, WorkshopStore.loadVehiclesForContact(conn, 42, false).size());
            assertEquals(2, WorkshopStore.loadVehiclesForContact(conn, 42, true).size());

            TireStorage tire = new TireStorage();
            tire.vehicleId = v1.id;
            tire.season = "WINTER";
            tire.storageLocation = "R-04";
            tire.treadFl = "6.0";
            tire.treadFr = "6.0";
            tire.treadRl = "6.5";
            tire.treadRr = "6.5";
            tire.rimType = "STEEL";
            tire.wheelBoltsIncluded = true;
            WorkshopStore.saveTireStorage(conn, tire);

            TireStorage loadedTire = WorkshopStore.loadLatestTireStorage(conn, v1.id);
            assertNotNull(loadedTire);
            assertEquals("R-04", loadedTire.storageLocation);
            assertTrue(loadedTire.wheelBoltsIncluded);

            JobData job = new JobData();
            job.itemId = 700;
            job.vehicleId = v1.id;
            job.plateSnapshot = "HH-XY 100";
            job.mileage = 62000;
            job.mileageUnit = "km";
            job.dropoffDate = "2026-11-18";
            job.workSummary = "Oil change";
            job.overrunApproved = true;
            job.overrunApprovedAt = "2026-11-18 phone";
            WorkshopStore.saveJob(conn, job);

            // Vehicle plate later changes, job snapshot stays intact
            v1.licensePlate = "B-NEW 200";
            WorkshopStore.saveVehicle(conn, v1);

            JobData loadedJob = WorkshopStore.loadJob(conn, 700);
            assertEquals("HH-XY 100", loadedJob.plateSnapshot);
            assertTrue(loadedJob.overrunApproved);
            assertEquals(62000L, WorkshopStore.maxRecordedMileageKm(conn, v1.id, 701));
            assertEquals(1, WorkshopStore.loadHistoryForVehicle(conn, v1.id).size());
        }
    }

    private static Properties loadUtf8Props(String resourcePath) throws Exception {
        Properties p = new Properties();
        try (InputStream in = WorkshopPlugin.class.getResourceAsStream(resourcePath)) {
            assertNotNull(in, "Missing resource: " + resourcePath);
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return p;
    }
}
