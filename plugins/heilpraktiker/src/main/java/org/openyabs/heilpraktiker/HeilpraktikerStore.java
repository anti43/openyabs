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

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.SecretKey;
import mpv5.db.common.DatabaseConnection;
import mpv5.globals.GlobalSettings;
import mpv5.logging.Log;

/**
 * Persistence for the Heilpraktiker plugin: practice settings (per database via
 * {@link GlobalSettings}) and patient / medical invoice metadata stored in
 * dedicated plugin tables ({@code hp_patient_data}, {@code hp_invoice_data}).
 * <p>
 * Sensitive treatment notes ({@code soap_notes}) are encrypted at rest with
 * AES-256-GCM ({@link HeilpraktikerCrypto}) when encryption is enabled. Each
 * record tracks its § 630f Abs. 3 BGB retention deadline (10 years after
 * treatment) and supports anonymisation or deletion once expired.
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
    private static final String K_RESTRICT_SOAP = PREFIX + "restrictSoapAccess";
    private static final String K_SOAP_USERS = PREFIX + "soapAllowedUsers";
    private static final String K_ENCRYPT_SOAP = PREFIX + "encryptSoapNotes";
    private static final String K_SKIP_EINVOICE = PREFIX + "skipEInvoiceForTaxExempt";

    public static final class Settings {

        public String practitionerTitle = DEFAULT_TITLE;
        public String licenseAuthority = DEFAULT_LICENSE;
        public String ustgNotice = DEFAULT_USTG_NOTICE;
        public GebuehCatalog.PriceMode defaultPriceMode = GebuehCatalog.PriceMode.MAX;
        public boolean warnOnSave = true;
        /**
         * Restrict SOAP notes to Administrators and explicitly authorized users
         * (§ 203 StGB / Art. 9 GDPR).
         */
        public boolean restrictSoapAccess = true;
        /**
         * Comma-separated list of YaBS usernames permitted to view/edit SOAP
         * notes in addition to Administrators.
         */
        public String soapAllowedUsers = "";
        /**
         * Encrypt {@code hp_patient_data.soap_notes} at rest using AES-256-GCM.
         */
        public boolean encryptSoapNotes = true;
        /**
         * Skip e-invoice delivery (ZUGFeRD/XRechnung) for tax-exempt medical
         * treatments (§ 4 Nr. 14 UStG / § 14 Abs. 2 UStG).
         */
        public boolean skipEInvoiceForTaxExempt = true;

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
            s.warnOnSave = readBool(K_WARN_ON_SAVE, true);
            s.restrictSoapAccess = readBool(K_RESTRICT_SOAP, true);
            s.soapAllowedUsers = readStr(K_SOAP_USERS, "");
            s.encryptSoapNotes = readBool(K_ENCRYPT_SOAP, true);
            s.skipEInvoiceForTaxExempt = readBool(K_SKIP_EINVOICE, true);
            return s;
        }

        public void save() {
            GlobalSettings.setProperty(K_TITLE, safe(practitionerTitle));
            GlobalSettings.setProperty(K_LICENSE, safe(licenseAuthority));
            GlobalSettings.setProperty(K_USTG, safe(ustgNotice));
            GlobalSettings.setProperty(K_PRICE_MODE, (defaultPriceMode == null ? GebuehCatalog.PriceMode.MAX : defaultPriceMode).name());
            GlobalSettings.setProperty(K_WARN_ON_SAVE, String.valueOf(warnOnSave));
            GlobalSettings.setProperty(K_RESTRICT_SOAP, String.valueOf(restrictSoapAccess));
            GlobalSettings.setProperty(K_SOAP_USERS, safe(soapAllowedUsers));
            GlobalSettings.setProperty(K_ENCRYPT_SOAP, String.valueOf(encryptSoapNotes));
            GlobalSettings.setProperty(K_SKIP_EINVOICE, String.valueOf(skipEInvoiceForTaxExempt));
            GlobalSettings.save();
        }
    }

    public static final class PatientData {

        public int contactId;
        public String birthDate = "";
        public String insuranceType = "Selbstzahler";
        public boolean honorarvereinbarung = false;
        /**
         * Treatment / SOAP notes (§ 630f BGB, 10-year retention).
         */
        public String soapNotes = "";
        /**
         * Patient-level default for printing diagnoses on invoices ({@code null}
         * = auto from insurance type: on for PKV/Beihilfe, off for Selbstzahler).
         */
        public Boolean printDiagnosis = null;
        /**
         * Date of last treatment (ISO {@code yyyy-MM-dd}).
         */
        public String lastTreatmentDate = "";
        /**
         * Retention end date (ISO {@code yyyy-MM-dd}, 10 years after treatment
         * per § 630f Abs. 3 BGB).
         */
        public String retentionUntil = "";
    }

    public static final class InvoiceData {

        public int itemId;
        public String icd10Code = "";
        public String diagnosisText = "";
        public boolean heilbehandlung = true;
        /**
         * Treatment date(s) for single or collective invoices (Sammelrechnung).
         */
        public String treatmentDates = "";
        /**
         * Per-invoice override for printing diagnosis ({@code null} = inherit
         * from patient / insurance type).
         */
        public Boolean printDiagnosis = null;
        /**
         * Retention end date (ISO {@code yyyy-MM-dd}, 10 years after treatment
         * per § 630f Abs. 3 BGB).
         */
        public String retentionUntil = "";
    }

    public static final class PurgeResult {

        public final int patientsAffected;
        public final int invoicesAffected;

        public PurgeResult(int patientsAffected, int invoicesAffected) {
            this.patientsAffected = patientsAffected;
            this.invoicesAffected = invoicesAffected;
        }
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
     * Creates plugin tables if they do not exist yet and adds any new columns
     * to existing tables (works on H2, Derby, PostgreSQL and MySQL).
     */
    public static void initSchema(Connection c) throws SQLException {
        createTableIfMissing(c, "hp_patient_data",
                "CREATE TABLE hp_patient_data ("
                + "contact_id INTEGER PRIMARY KEY, "
                + "birth_date VARCHAR(20), "
                + "insurance_type VARCHAR(40), "
                + "honorarvereinbarung INTEGER, "
                + "soap_notes VARCHAR(8000), "
                + "print_diagnosis INTEGER, "
                + "last_treatment_date VARCHAR(20), "
                + "retention_until VARCHAR(20), "
                + "updated_at TIMESTAMP)");
        addColumnIfMissing(c, "hp_patient_data", "print_diagnosis", "INTEGER");
        addColumnIfMissing(c, "hp_patient_data", "last_treatment_date", "VARCHAR(20)");
        addColumnIfMissing(c, "hp_patient_data", "retention_until", "VARCHAR(20)");

        createTableIfMissing(c, "hp_invoice_data",
                "CREATE TABLE hp_invoice_data ("
                + "item_id INTEGER PRIMARY KEY, "
                + "icd10_code VARCHAR(80), "
                + "diagnosis_text VARCHAR(500), "
                + "is_heilbehandlung INTEGER, "
                + "treatment_dates VARCHAR(500), "
                + "print_diagnosis INTEGER, "
                + "retention_until VARCHAR(20), "
                + "updated_at TIMESTAMP)");
        addColumnIfMissing(c, "hp_invoice_data", "print_diagnosis", "INTEGER");
        addColumnIfMissing(c, "hp_invoice_data", "retention_until", "VARCHAR(20)");
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

    private static void addColumnIfMissing(Connection c, String table, String column, String columnDef) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT " + column + " FROM " + table + " WHERE 1 = 0")) {
            ps.executeQuery().close();
            return;
        } catch (SQLException missing) {
            // Column does not exist yet
        }
        try (PreparedStatement ps = c.prepareStatement("ALTER TABLE " + table + " ADD COLUMN " + column + " " + columnDef)) {
            ps.executeUpdate();
        }
    }

    public static PatientData loadPatient(Connection c, int contactId) throws SQLException {
        return loadPatient(c, contactId, HeilpraktikerCrypto.getActiveKey());
    }

    public static PatientData loadPatient(Connection c, int contactId, SecretKey key) throws SQLException {
        PatientData p = new PatientData();
        p.contactId = contactId;
        if (contactId <= 0) {
            return p;
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT birth_date, insurance_type, honorarvereinbarung, soap_notes, "
                + "print_diagnosis, last_treatment_date, retention_until "
                + "FROM hp_patient_data WHERE contact_id = ?")) {
            ps.setInt(1, contactId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p.birthDate = safe(rs.getString(1));
                    String ins = safe(rs.getString(2));
                    p.insuranceType = ins.isEmpty() ? "Selbstzahler" : ins;
                    p.honorarvereinbarung = rs.getInt(3) != 0;
                    String rawSoap = safe(rs.getString(4));
                    p.soapNotes = decryptSoapSafe(rawSoap, key);
                    p.printDiagnosis = readNullableBool(rs, 5);
                    p.lastTreatmentDate = safe(rs.getString(6));
                    p.retentionUntil = safe(rs.getString(7));
                }
            }
        }
        return p;
    }

    public static void savePatient(Connection c, PatientData p) throws SQLException {
        SecretKey key = effectiveEncryptionKey();
        savePatient(c, p, key);
    }

    public static void savePatient(Connection c, PatientData p, SecretKey key) throws SQLException {
        if (p == null || p.contactId <= 0) {
            return;
        }
        ensurePatientRetention(p);
        String storedSoap = encryptSoap(safe(p.soapNotes), key);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        int updated;
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE hp_patient_data SET birth_date = ?, insurance_type = ?, honorarvereinbarung = ?, "
                + "soap_notes = ?, print_diagnosis = ?, last_treatment_date = ?, retention_until = ?, updated_at = ? "
                + "WHERE contact_id = ?")) {
            ps.setString(1, truncate(safe(p.birthDate), 20));
            ps.setString(2, truncate(safe(p.insuranceType), 40));
            ps.setInt(3, p.honorarvereinbarung ? 1 : 0);
            ps.setString(4, truncate(storedSoap, 7900));
            setNullableBool(ps, 5, p.printDiagnosis);
            ps.setString(6, truncate(safe(p.lastTreatmentDate), 20));
            ps.setString(7, truncate(safe(p.retentionUntil), 20));
            ps.setTimestamp(8, now);
            ps.setInt(9, p.contactId);
            updated = ps.executeUpdate();
        }
        if (updated == 0) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO hp_patient_data (contact_id, birth_date, insurance_type, honorarvereinbarung, "
                    + "soap_notes, print_diagnosis, last_treatment_date, retention_until, updated_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, p.contactId);
                ps.setString(2, truncate(safe(p.birthDate), 20));
                ps.setString(3, truncate(safe(p.insuranceType), 40));
                ps.setInt(4, p.honorarvereinbarung ? 1 : 0);
                ps.setString(5, truncate(storedSoap, 7900));
                setNullableBool(ps, 6, p.printDiagnosis);
                ps.setString(7, truncate(safe(p.lastTreatmentDate), 20));
                ps.setString(8, truncate(safe(p.retentionUntil), 20));
                ps.setTimestamp(9, now);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Saves patient administrative metadata without modifying existing
     * {@code soap_notes} (used when a user without SOAP access edits billing or
     * insurance data).
     */
    public static void savePatientWithoutSoap(Connection c, PatientData p) throws SQLException {
        if (p == null || p.contactId <= 0) {
            return;
        }
        ensurePatientRetention(p);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        int updated;
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE hp_patient_data SET birth_date = ?, insurance_type = ?, honorarvereinbarung = ?, "
                + "print_diagnosis = ?, last_treatment_date = ?, retention_until = ?, updated_at = ? "
                + "WHERE contact_id = ?")) {
            ps.setString(1, truncate(safe(p.birthDate), 20));
            ps.setString(2, truncate(safe(p.insuranceType), 40));
            ps.setInt(3, p.honorarvereinbarung ? 1 : 0);
            setNullableBool(ps, 4, p.printDiagnosis);
            ps.setString(5, truncate(safe(p.lastTreatmentDate), 20));
            ps.setString(6, truncate(safe(p.retentionUntil), 20));
            ps.setTimestamp(7, now);
            ps.setInt(8, p.contactId);
            updated = ps.executeUpdate();
        }
        if (updated == 0) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO hp_patient_data (contact_id, birth_date, insurance_type, honorarvereinbarung, "
                    + "soap_notes, print_diagnosis, last_treatment_date, retention_until, updated_at) "
                    + "VALUES (?, ?, ?, ?, '', ?, ?, ?, ?)")) {
                ps.setInt(1, p.contactId);
                ps.setString(2, truncate(safe(p.birthDate), 20));
                ps.setString(3, truncate(safe(p.insuranceType), 40));
                ps.setInt(4, p.honorarvereinbarung ? 1 : 0);
                setNullableBool(ps, 5, p.printDiagnosis);
                ps.setString(6, truncate(safe(p.lastTreatmentDate), 20));
                ps.setString(7, truncate(safe(p.retentionUntil), 20));
                ps.setTimestamp(8, now);
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
                "SELECT icd10_code, diagnosis_text, is_heilbehandlung, treatment_dates, "
                + "print_diagnosis, retention_until "
                + "FROM hp_invoice_data WHERE item_id = ?")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    d.icd10Code = safe(rs.getString(1));
                    d.diagnosisText = safe(rs.getString(2));
                    d.heilbehandlung = rs.getInt(3) != 0;
                    d.treatmentDates = safe(rs.getString(4));
                    d.printDiagnosis = readNullableBool(rs, 5);
                    d.retentionUntil = safe(rs.getString(6));
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
        ensureInvoiceRetention(d);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        int updated;
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE hp_invoice_data SET icd10_code = ?, diagnosis_text = ?, is_heilbehandlung = ?, "
                + "treatment_dates = ?, print_diagnosis = ?, retention_until = ?, updated_at = ? "
                + "WHERE item_id = ?")) {
            ps.setString(1, truncate(safe(d.icd10Code), 80));
            ps.setString(2, truncate(safe(d.diagnosisText), 500));
            ps.setInt(3, d.heilbehandlung ? 1 : 0);
            ps.setString(4, truncate(safe(d.treatmentDates), 500));
            setNullableBool(ps, 5, d.printDiagnosis);
            ps.setString(6, truncate(safe(d.retentionUntil), 20));
            ps.setTimestamp(7, now);
            ps.setInt(8, d.itemId);
            updated = ps.executeUpdate();
        }
        if (updated == 0) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO hp_invoice_data (item_id, icd10_code, diagnosis_text, is_heilbehandlung, "
                    + "treatment_dates, print_diagnosis, retention_until, updated_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, d.itemId);
                ps.setString(2, truncate(safe(d.icd10Code), 80));
                ps.setString(3, truncate(safe(d.diagnosisText), 500));
                ps.setInt(4, d.heilbehandlung ? 1 : 0);
                ps.setString(5, truncate(safe(d.treatmentDates), 500));
                setNullableBool(ps, 6, d.printDiagnosis);
                ps.setString(7, truncate(safe(d.retentionUntil), 20));
                ps.setTimestamp(8, now);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Lists all patient records whose § 630f Abs. 3 BGB retention deadline has
     * passed (strictly before {@code today}) and which still hold health data.
     */
    public static List<PatientData> findExpiredPatients(Connection c, LocalDate today) throws SQLException {
        List<PatientData> expired = new ArrayList<PatientData>();
        LocalDate ref = today == null ? LocalDate.now() : today;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT contact_id, birth_date, insurance_type, honorarvereinbarung, "
                + "soap_notes, print_diagnosis, last_treatment_date, retention_until "
                + "FROM hp_patient_data WHERE retention_until IS NOT NULL AND retention_until <> ''")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String until = safe(rs.getString(8));
                    if (HeilpraktikerValidator.isRetentionExpired(until, ref)) {
                        PatientData p = new PatientData();
                        p.contactId = rs.getInt(1);
                        p.birthDate = safe(rs.getString(2));
                        p.insuranceType = safe(rs.getString(3));
                        p.honorarvereinbarung = rs.getInt(4) != 0;
                        p.soapNotes = safe(rs.getString(5));
                        p.printDiagnosis = readNullableBool(rs, 6);
                        p.lastTreatmentDate = safe(rs.getString(7));
                        p.retentionUntil = until;
                        expired.add(p);
                    }
                }
            }
        }
        return expired;
    }

    /**
     * Lists all invoice medical records whose § 630f Abs. 3 BGB retention
     * deadline has passed (strictly before {@code today}).
     */
    public static List<InvoiceData> findExpiredInvoices(Connection c, LocalDate today) throws SQLException {
        List<InvoiceData> expired = new ArrayList<InvoiceData>();
        LocalDate ref = today == null ? LocalDate.now() : today;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT item_id, icd10_code, diagnosis_text, is_heilbehandlung, "
                + "treatment_dates, print_diagnosis, retention_until "
                + "FROM hp_invoice_data WHERE retention_until IS NOT NULL AND retention_until <> ''")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String until = safe(rs.getString(7));
                    if (HeilpraktikerValidator.isRetentionExpired(until, ref)) {
                        InvoiceData d = new InvoiceData();
                        d.itemId = rs.getInt(1);
                        d.icd10Code = safe(rs.getString(2));
                        d.diagnosisText = safe(rs.getString(3));
                        d.heilbehandlung = rs.getInt(4) != 0;
                        d.treatmentDates = safe(rs.getString(5));
                        d.printDiagnosis = readNullableBool(rs, 6);
                        d.retentionUntil = until;
                        expired.add(d);
                    }
                }
            }
        }
        return expired;
    }

    /**
     * Anonymises a patient record by clearing all Art. 9 GDPR health and
     * personal fields (birth date, SOAP notes, insurance type, agreement flag).
     */
    public static int anonymizePatient(Connection c, int contactId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE hp_patient_data SET birth_date = '', insurance_type = '', "
                + "honorarvereinbarung = 0, soap_notes = '', print_diagnosis = 0, "
                + "retention_until = '', updated_at = ? WHERE contact_id = ?")) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, contactId);
            return ps.executeUpdate();
        }
    }

    /**
     * Permanently deletes a patient's Heilpraktiker metadata row.
     */
    public static int deletePatient(Connection c, int contactId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM hp_patient_data WHERE contact_id = ?")) {
            ps.setInt(1, contactId);
            return ps.executeUpdate();
        }
    }

    /**
     * Anonymises medical diagnosis fields of an invoice record while keeping
     * {@code is_heilbehandlung} for VAT auditability.
     */
    public static int anonymizeInvoice(Connection c, int itemId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE hp_invoice_data SET icd10_code = '', diagnosis_text = '', "
                + "treatment_dates = '', print_diagnosis = 0, retention_until = '', "
                + "updated_at = ? WHERE item_id = ?")) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, itemId);
            return ps.executeUpdate();
        }
    }

    /**
     * Permanently deletes an invoice's Heilpraktiker metadata row.
     */
    public static int deleteInvoice(Connection c, int itemId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM hp_invoice_data WHERE item_id = ?")) {
            ps.setInt(1, itemId);
            return ps.executeUpdate();
        }
    }

    /**
     * Anonymises (when {@code deleteRows == false}) or permanently deletes
     * (when {@code deleteRows == true}) all patient and invoice health records
     * whose § 630f Abs. 3 BGB retention period has expired.
     */
    public static PurgeResult purgeExpired(Connection c, LocalDate today, boolean deleteRows) throws SQLException {
        List<PatientData> patients = findExpiredPatients(c, today);
        List<InvoiceData> invoices = findExpiredInvoices(c, today);
        int pCount = 0;
        for (PatientData p : patients) {
            pCount += deleteRows ? deletePatient(c, p.contactId) : anonymizePatient(c, p.contactId);
        }
        int iCount = 0;
        for (InvoiceData d : invoices) {
            iCount += deleteRows ? deleteInvoice(c, d.itemId) : anonymizeInvoice(c, d.itemId);
        }
        return new PurgeResult(pCount, iCount);
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

    private static SecretKey effectiveEncryptionKey() {
        try {
            if (!Settings.load().encryptSoapNotes) {
                return null;
            }
        } catch (Throwable ignored) {
        }
        return HeilpraktikerCrypto.getActiveKey();
    }

    private static String encryptSoap(String plaintext, SecretKey key) throws SQLException {
        if (plaintext.isEmpty() || key == null) {
            return plaintext;
        }
        try {
            return HeilpraktikerCrypto.encrypt(plaintext, key);
        } catch (GeneralSecurityException e) {
            throw new SQLException("Verschl\u00fcsselung der SOAP-Notizen fehlgeschlagen: " + e.getMessage(), e);
        }
    }

    private static String decryptSoapSafe(String stored, SecretKey key) {
        if (stored == null || stored.isEmpty()) {
            return "";
        }
        if (!HeilpraktikerCrypto.isEncrypted(stored)) {
            return stored;
        }
        try {
            return HeilpraktikerCrypto.decrypt(stored, key);
        } catch (GeneralSecurityException e) {
            Log.Debug(HeilpraktikerStore.class, e);
            return "[Verschl\u00fcsselte SOAP-Notizen \u2013 Schl\u00fcssel nicht verf\u00fcgbar]";
        }
    }

    private static void ensurePatientRetention(PatientData p) {
        LocalDate treatment = HeilpraktikerValidator.parseDate(p.lastTreatmentDate);
        if (treatment != null) {
            p.lastTreatmentDate = treatment.toString();
            LocalDate computed = HeilpraktikerValidator.calculateTreatmentRetentionUntil(treatment);
            LocalDate existing = HeilpraktikerValidator.parseDate(p.retentionUntil);
            if (existing == null || computed.isAfter(existing)) {
                p.retentionUntil = computed.toString();
            }
        } else if (safe(p.retentionUntil).isEmpty() && !safe(p.soapNotes).isEmpty()) {
            p.retentionUntil = HeilpraktikerValidator.calculateTreatmentRetentionUntil(LocalDate.now()).toString();
        }
    }

    private static void ensureInvoiceRetention(InvoiceData d) {
        if (!safe(d.retentionUntil).isEmpty()) {
            return;
        }
        LocalDate treatment = HeilpraktikerValidator.parseLatestTreatmentDate(d.treatmentDates);
        if (treatment != null) {
            d.retentionUntil = HeilpraktikerValidator.calculateTreatmentRetentionUntil(treatment).toString();
        } else if (!safe(d.diagnosisText).isEmpty() || !safe(d.icd10Code).isEmpty()) {
            d.retentionUntil = HeilpraktikerValidator.calculateTreatmentRetentionUntil(LocalDate.now()).toString();
        }
    }

    private static Boolean readNullableBool(ResultSet rs, int col) throws SQLException {
        int v = rs.getInt(col);
        if (rs.wasNull()) {
            return null;
        }
        return v != 0;
    }

    private static void setNullableBool(PreparedStatement ps, int idx, Boolean value) throws SQLException {
        if (value == null) {
            ps.setNull(idx, Types.INTEGER);
        } else {
            ps.setInt(idx, value.booleanValue() ? 1 : 0);
        }
    }

    private static String readStr(String key, String fallback) {
        if (!GlobalSettings.hasProperty(key)) {
            return fallback;
        }
        String v = GlobalSettings.getProperty(key);
        return v == null || "null".equals(v) ? fallback : v;
    }

    private static boolean readBool(String key, boolean fallback) {
        return GlobalSettings.hasProperty(key)
                ? GlobalSettings.getBooleanProperty(key, fallback)
                : fallback;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }
}
