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
package org.openyabs.heilpraktiker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import mpv5.db.common.DatabaseConnection;
import mpv5.globals.GlobalSettings;
import mpv5.logging.Log;

/**
 * Persistence for the Heilpraktiker plugin: practice settings (per database via
 * {@link GlobalSettings}) and patient / medical invoice metadata stored in
 * dedicated plugin tables ({@code hp_patient_data}, {@code hp_invoice_data}).
 */
public final class HeilpraktikerStore {

    public static final String DEFAULT_TITLE = "Heilpraktiker/in";
    public static final String DEFAULT_LICENSE = "Erlaubnis zur Aus\u00fcbung der Heilkunde nach \u00a7 1 HeilprG";
    public static final String DEFAULT_USTG_NOTICE =
            "Heilbehandlungen im Bereich der Humanmedizin sind gem\u00e4\u00df \u00a7 4 Nr. 14 Buchst. a UStG von der Umsatzsteuer befreit.";

    private static final String PREFIX = "hp.";
    private static final String K_TITLE = PREFIX + "practitionerTitle";
    private static final String K_LICENSE = PREFIX + "licenseAuthority";
    private static final String K_USTG = PREFIX + "ustgNotice";
    private static final String K_PRICE_MODE = PREFIX + "defaultPriceMode";
    private static final String K_WARN_ON_SAVE = PREFIX + "warnOnSave";

    public static final class Settings {

        public String practitionerTitle = DEFAULT_TITLE;
        public String licenseAuthority = DEFAULT_LICENSE;
        public String ustgNotice = DEFAULT_USTG_NOTICE;
        public GebuehCatalog.PriceMode defaultPriceMode = GebuehCatalog.PriceMode.MAX;
        public boolean warnOnSave = true;

        public static Settings load() {
            Settings s = new Settings();
            s.practitionerTitle = readStr(K_TITLE, DEFAULT_TITLE);
            s.licenseAuthority = readStr(K_LICENSE, DEFAULT_LICENSE);
            s.ustgNotice = readStr(K_USTG, DEFAULT_USTG_NOTICE);
            String pm = readStr(K_PRICE_MODE, GebuehCatalog.PriceMode.MAX.name());
            try {
                s.defaultPriceMode = GebuehCatalog.PriceMode.valueOf(pm);
            } catch (Exception e) {
                s.defaultPriceMode = GebuehCatalog.PriceMode.MAX;
            }
            s.warnOnSave = GlobalSettings.hasProperty(K_WARN_ON_SAVE)
                    ? GlobalSettings.getBooleanProperty(K_WARN_ON_SAVE, true)
                    : true;
            return s;
        }

        public void save() {
            GlobalSettings.setProperty(K_TITLE, safe(practitionerTitle));
            GlobalSettings.setProperty(K_LICENSE, safe(licenseAuthority));
            GlobalSettings.setProperty(K_USTG, safe(ustgNotice));
            GlobalSettings.setProperty(K_PRICE_MODE, (defaultPriceMode == null ? GebuehCatalog.PriceMode.MAX : defaultPriceMode).name());
            GlobalSettings.setProperty(K_WARN_ON_SAVE, String.valueOf(warnOnSave));
            GlobalSettings.save();
        }
    }

    public static final class PatientData {

        public int contactId;
        public String birthDate = "";
        public String insuranceType = "Selbstzahler";
        public boolean honorarvereinbarung = false;
        /**
         * Treatment / SOAP notes (§ 630f BGB, 10-year retention)
         */
        public String soapNotes = "";
    }

    public static final class InvoiceData {

        public int itemId;
        public String icd10Code = "";
        public String diagnosisText = "";
        public boolean heilbehandlung = true;
        /**
         * Treatment date(s) for single or collective invoices (Sammelrechnung)
         */
        public String treatmentDates = "";
    }

    private HeilpraktikerStore() {
    }

    static Connection connection() throws SQLException {
        DatabaseConnection dc = DatabaseConnection.getConnector();
        Connection c = dc == null ? null : dc.getConnection();
        if (c == null) {
            throw new SQLException("Keine Datenbankverbindung");
        }
        return c;
    }

    static boolean hasDatabase() {
        try {
            return DatabaseConnection.getConnector() != null
                    && DatabaseConnection.getConnector().getConnection() != null;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Creates plugin tables if they do not exist yet (works on H2, Derby,
     * PostgreSQL and MySQL).
     */
    public static void initSchema(Connection c) throws SQLException {
        createTableIfMissing(c, "hp_patient_data",
                "CREATE TABLE hp_patient_data ("
                + "contact_id INTEGER PRIMARY KEY, "
                + "birth_date VARCHAR(20), "
                + "insurance_type VARCHAR(40), "
                + "honorarvereinbarung INTEGER, "
                + "soap_notes VARCHAR(4000), "
                + "updated_at TIMESTAMP)");
        createTableIfMissing(c, "hp_invoice_data",
                "CREATE TABLE hp_invoice_data ("
                + "item_id INTEGER PRIMARY KEY, "
                + "icd10_code VARCHAR(80), "
                + "diagnosis_text VARCHAR(500), "
                + "is_heilbehandlung INTEGER, "
                + "treatment_dates VARCHAR(500), "
                + "updated_at TIMESTAMP)");
    }

    private static void createTableIfMissing(Connection c, String table, String ddl) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM " + table + " WHERE 1 = 0")) {
            ps.executeQuery().close();
            return;
        } catch (SQLException missing) {
            // Table does not exist yet
        }
        try (PreparedStatement ps = c.prepareStatement(ddl)) {
            ps.executeUpdate();
        }
    }

    public static PatientData loadPatient(Connection c, int contactId) throws SQLException {
        PatientData p = new PatientData();
        p.contactId = contactId;
        if (contactId <= 0) {
            return p;
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT birth_date, insurance_type, honorarvereinbarung, soap_notes FROM hp_patient_data WHERE contact_id = ?")) {
            ps.setInt(1, contactId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p.birthDate = safe(rs.getString(1));
                    String ins = safe(rs.getString(2));
                    p.insuranceType = ins.isEmpty() ? "Selbstzahler" : ins;
                    p.honorarvereinbarung = rs.getInt(3) != 0;
                    p.soapNotes = safe(rs.getString(4));
                }
            }
        }
        return p;
    }

    public static void savePatient(Connection c, PatientData p) throws SQLException {
        if (p == null || p.contactId <= 0) {
            return;
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        int updated;
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE hp_patient_data SET birth_date = ?, insurance_type = ?, honorarvereinbarung = ?, soap_notes = ?, updated_at = ? WHERE contact_id = ?")) {
            ps.setString(1, truncate(safe(p.birthDate), 20));
            ps.setString(2, truncate(safe(p.insuranceType), 40));
            ps.setInt(3, p.honorarvereinbarung ? 1 : 0);
            ps.setString(4, truncate(safe(p.soapNotes), 4000));
            ps.setTimestamp(5, now);
            ps.setInt(6, p.contactId);
            updated = ps.executeUpdate();
        }
        if (updated == 0) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO hp_patient_data (contact_id, birth_date, insurance_type, honorarvereinbarung, soap_notes, updated_at) VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, p.contactId);
                ps.setString(2, truncate(safe(p.birthDate), 20));
                ps.setString(3, truncate(safe(p.insuranceType), 40));
                ps.setInt(4, p.honorarvereinbarung ? 1 : 0);
                ps.setString(5, truncate(safe(p.soapNotes), 4000));
                ps.setTimestamp(6, now);
                ps.executeUpdate();
            }
        }
    }

    public static InvoiceData loadInvoice(Connection c, int itemId) throws SQLException {
        InvoiceData d = new InvoiceData();
        d.itemId = itemId;
        if (itemId <= 0) {
            return d;
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT icd10_code, diagnosis_text, is_heilbehandlung, treatment_dates FROM hp_invoice_data WHERE item_id = ?")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    d.icd10Code = safe(rs.getString(1));
                    d.diagnosisText = safe(rs.getString(2));
                    d.heilbehandlung = rs.getInt(3) != 0;
                    d.treatmentDates = safe(rs.getString(4));
                }
            }
        }
        return d;
    }

    public static boolean hasInvoiceData(Connection c, int itemId) throws SQLException {
        if (itemId <= 0) {
            return false;
        }
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM hp_invoice_data WHERE item_id = ?")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void saveInvoice(Connection c, InvoiceData d) throws SQLException {
        if (d == null || d.itemId <= 0) {
            return;
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        int updated;
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE hp_invoice_data SET icd10_code = ?, diagnosis_text = ?, is_heilbehandlung = ?, treatment_dates = ?, updated_at = ? WHERE item_id = ?")) {
            ps.setString(1, truncate(safe(d.icd10Code), 80));
            ps.setString(2, truncate(safe(d.diagnosisText), 500));
            ps.setInt(3, d.heilbehandlung ? 1 : 0);
            ps.setString(4, truncate(safe(d.treatmentDates), 500));
            ps.setTimestamp(5, now);
            ps.setInt(6, d.itemId);
            updated = ps.executeUpdate();
        }
        if (updated == 0) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO hp_invoice_data (item_id, icd10_code, diagnosis_text, is_heilbehandlung, treatment_dates, updated_at) VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, d.itemId);
                ps.setString(2, truncate(safe(d.icd10Code), 80));
                ps.setString(3, truncate(safe(d.diagnosisText), 500));
                ps.setInt(4, d.heilbehandlung ? 1 : 0);
                ps.setString(5, truncate(safe(d.treatmentDates), 500));
                ps.setTimestamp(6, now);
                ps.executeUpdate();
            }
        }
    }

    static PatientData loadPatientSafe(int contactId) {
        try {
            return loadPatient(connection(), contactId);
        } catch (Exception e) {
            Log.Debug(HeilpraktikerStore.class, e.getMessage());
            PatientData p = new PatientData();
            p.contactId = contactId;
            return p;
        }
    }

    static InvoiceData loadInvoiceSafe(int itemId) {
        try {
            return loadInvoice(connection(), itemId);
        } catch (Exception e) {
            Log.Debug(HeilpraktikerStore.class, e.getMessage());
            InvoiceData d = new InvoiceData();
            d.itemId = itemId;
            return d;
        }
    }

    private static String readStr(String key, String fallback) {
        if (!GlobalSettings.hasProperty(key)) {
            return fallback;
        }
        String v = GlobalSettings.getProperty(key);
        return v == null || "null".equals(v) ? fallback : v;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }
}
