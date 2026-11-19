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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mpv5.db.common.QueryHandler;
import mpv5.globals.GlobalSettings;
import mpv5.logging.Log;

/**
 * Per-database settings and cross-database JDBC persistence for the Car Repair
 * Shop / Kfz-Werkstatt plugin ({@code workshop_schema_version},
 * {@code ws_vehicles}, {@code ws_tire_storage}, {@code ws_job_data}).
 */
public final class WorkshopStore {

    public static final int SCHEMA_VERSION = 1;
    private static final String PREFIX = "ws.";

    private WorkshopStore() {
    }

    public static final class Settings {

        public String distanceUnit = "km";
        public int awPerHour = 12;
        public BigDecimal rateMechanical = new BigDecimal("115.00");
        public BigDecimal rateElectrical = new BigDecimal("135.00");
        public BigDecimal rateBodywork = new BigDecimal("145.00");
        public int inspectionLeadDays = 30;
        public int quoteOverrunPercent = 15;
        public boolean warnOnSave = true;

        public static Settings load() {
            Settings s = new Settings();
            try {
                if (GlobalSettings.hasProperty(PREFIX + "distanceUnit")) {
                    String u = GlobalSettings.getProperty(PREFIX + "distanceUnit");
                    s.distanceUnit = "mi".equalsIgnoreCase(u) ? "mi" : "km";
                }
                if (GlobalSettings.hasProperty(PREFIX + "awPerHour")) {
                    s.awPerHour = parseInt(GlobalSettings.getProperty(PREFIX + "awPerHour"), 12);
                }
                if (GlobalSettings.hasProperty(PREFIX + "rateMechanical")) {
                    s.rateMechanical = parseDecimal(GlobalSettings.getProperty(PREFIX + "rateMechanical"), s.rateMechanical);
                }
                if (GlobalSettings.hasProperty(PREFIX + "rateElectrical")) {
                    s.rateElectrical = parseDecimal(GlobalSettings.getProperty(PREFIX + "rateElectrical"), s.rateElectrical);
                }
                if (GlobalSettings.hasProperty(PREFIX + "rateBodywork")) {
                    s.rateBodywork = parseDecimal(GlobalSettings.getProperty(PREFIX + "rateBodywork"), s.rateBodywork);
                }
                if (GlobalSettings.hasProperty(PREFIX + "inspectionLeadDays")) {
                    s.inspectionLeadDays = parseInt(GlobalSettings.getProperty(PREFIX + "inspectionLeadDays"), 30);
                }
                if (GlobalSettings.hasProperty(PREFIX + "quoteOverrunPercent")) {
                    s.quoteOverrunPercent = parseInt(GlobalSettings.getProperty(PREFIX + "quoteOverrunPercent"), 15);
                }
                if (GlobalSettings.hasProperty(PREFIX + "warnOnSave")) {
                    s.warnOnSave = GlobalSettings.getBooleanProperty(PREFIX + "warnOnSave");
                }
            } catch (Throwable ignored) {
            }
            return s;
        }

        public void save() {
            GlobalSettings.setProperty(PREFIX + "distanceUnit", "mi".equalsIgnoreCase(distanceUnit) ? "mi" : "km");
            GlobalSettings.setProperty(PREFIX + "awPerHour", String.valueOf(Math.max(0, awPerHour)));
            GlobalSettings.setProperty(PREFIX + "rateMechanical", rateMechanical.toPlainString());
            GlobalSettings.setProperty(PREFIX + "rateElectrical", rateElectrical.toPlainString());
            GlobalSettings.setProperty(PREFIX + "rateBodywork", rateBodywork.toPlainString());
            GlobalSettings.setProperty(PREFIX + "inspectionLeadDays", String.valueOf(Math.max(1, inspectionLeadDays)));
            GlobalSettings.setProperty(PREFIX + "quoteOverrunPercent", String.valueOf(Math.max(0, quoteOverrunPercent)));
            GlobalSettings.setProperty(PREFIX + "warnOnSave", String.valueOf(warnOnSave));
            GlobalSettings.save();
        }
    }

    public static final class Vehicle {

        public int id;
        public int contactId;
        public String licensePlate = "";
        public String vin = "";
        public String makeModel = "";
        public String hsnTsn = "";
        public String firstRegistration = "";
        public long mileage;
        public String mileageUnit = "km";
        public String inspectionDue = "";
        public String tireSize = "";
        public boolean archived;
        public String notes = "";

        public String displayLabel() {
            String p = safe(licensePlate);
            String m = safe(makeModel);
            if (!p.isEmpty() && !m.isEmpty()) {
                return p + " (" + m + ")";
            }
            return !p.isEmpty() ? p : (!m.isEmpty() ? m : "#" + id);
        }

        @Override
        public String toString() {
            return displayLabel();
        }
    }

    public static final class TireStorage {

        public int id;
        public int vehicleId;
        public String season = "WINTER";
        public String storageLocation = "";
        public String treadFl = "";
        public String treadFr = "";
        public String treadRl = "";
        public String treadRr = "";
        public String dotCodes = "";
        public String rimType = "ALLOY";
        public boolean wheelBoltsIncluded;
        public String conditionNotes = "";
        public String storedAt = "";

        public String treadSummary() {
            if (safe(treadFl).isEmpty() && safe(treadFr).isEmpty()
                    && safe(treadRl).isEmpty() && safe(treadRr).isEmpty()) {
                return "";
            }
            return nonEmpty(treadFl) + " / " + nonEmpty(treadFr) + " / "
                    + nonEmpty(treadRl) + " / " + nonEmpty(treadRr) + " mm";
        }

        private static String nonEmpty(String s) {
            return safe(s).isEmpty() ? "-" : safe(s);
        }
    }

    public static final class JobData {

        public int itemId;
        public int vehicleId;
        public String plateSnapshot = "";
        public long mileage;
        public String mileageUnit = "km";
        public String dropoffDate = "";
        public String pickupDate = "";
        public String complaint = "";
        public String workSummary = "";
        public String internalNotes = "";
        public String oldPartsHandling = "DISPOSE";
        public int linkedQuoteId;
        public String quoteNumberSnapshot = "";
        public String quoteTotalSnapshot = "";
        public boolean overrunApproved;
        public String overrunApprovedAt = "";
    }

    public static boolean hasDatabase() {
        try {
            return QueryHandler.getConnection() != null
                    && QueryHandler.getConnection().getConnection() != null;
        } catch (Throwable t) {
            return false;
        }
    }

    public static Connection connection() throws SQLException {
        return QueryHandler.getConnection().getConnection();
    }

    /**
     * Creates plugin-owned tables using cross-database SQL (H2, Derby,
     * MySQL/MariaDB, PostgreSQL). Safe to call repeatedly.
     */
    public static void initSchema(Connection conn) throws SQLException {
        if (conn == null) {
            return;
        }
        createTable(conn, "CREATE TABLE workshop_schema_version ("
                + "version INTEGER NOT NULL PRIMARY KEY, "
                + "applied_at VARCHAR(32) NOT NULL"
                + ")");
        createTable(conn, "CREATE TABLE ws_vehicles ("
                + "id INTEGER NOT NULL PRIMARY KEY, "
                + "contact_id INTEGER NOT NULL, "
                + "license_plate VARCHAR(64), "
                + "vin VARCHAR(64), "
                + "make_model VARCHAR(255), "
                + "hsn_tsn VARCHAR(64), "
                + "first_registration VARCHAR(32), "
                + "mileage BIGINT NOT NULL DEFAULT 0, "
                + "mileage_unit VARCHAR(8) NOT NULL DEFAULT 'km', "
                + "inspection_due VARCHAR(32), "
                + "tire_size VARCHAR(64), "
                + "archived INTEGER NOT NULL DEFAULT 0, "
                + "notes VARCHAR(2000)"
                + ")");
        createTable(conn, "CREATE TABLE ws_tire_storage ("
                + "id INTEGER NOT NULL PRIMARY KEY, "
                + "vehicle_id INTEGER NOT NULL, "
                + "season VARCHAR(32), "
                + "storage_location VARCHAR(128), "
                + "tread_fl VARCHAR(16), "
                + "tread_fr VARCHAR(16), "
                + "tread_rl VARCHAR(16), "
                + "tread_rr VARCHAR(16), "
                + "dot_codes VARCHAR(128), "
                + "rim_type VARCHAR(32), "
                + "wheel_bolts INTEGER NOT NULL DEFAULT 0, "
                + "condition_notes VARCHAR(1000), "
                + "stored_at VARCHAR(32)"
                + ")");
        createTable(conn, "CREATE TABLE ws_job_data ("
                + "item_id INTEGER NOT NULL PRIMARY KEY, "
                + "vehicle_id INTEGER NOT NULL DEFAULT 0, "
                + "plate_snapshot VARCHAR(64), "
                + "mileage BIGINT NOT NULL DEFAULT 0, "
                + "mileage_unit VARCHAR(8) NOT NULL DEFAULT 'km', "
                + "dropoff_date VARCHAR(32), "
                + "pickup_date VARCHAR(32), "
                + "complaint VARCHAR(2000), "
                + "work_summary VARCHAR(2000), "
                + "internal_notes VARCHAR(2000), "
                + "old_parts_handling VARCHAR(32), "
                + "linked_quote_id INTEGER NOT NULL DEFAULT 0, "
                + "quote_number_snapshot VARCHAR(64), "
                + "quote_total_snapshot VARCHAR(64), "
                + "overrun_approved INTEGER NOT NULL DEFAULT 0, "
                + "overrun_approved_at VARCHAR(64)"
                + ")");

        if (getSchemaVersion(conn) < SCHEMA_VERSION) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO workshop_schema_version (version, applied_at) VALUES (?, ?)")) {
                ps.setInt(1, SCHEMA_VERSION);
                ps.setString(2, LocalDate.now().toString());
                ps.executeUpdate();
            } catch (SQLException ignored) {
                // concurrent init
            }
        }
    }

    public static int getSchemaVersion(Connection conn) throws SQLException {
        if (conn == null) {
            return 0;
        }
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT MAX(version) FROM workshop_schema_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static void createTable(Connection conn, String ddl) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(ddl);
        } catch (SQLException e) {
            String state = e.getSQLState() == null ? "" : e.getSQLState();
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            // H2: 42S01, Derby: X0Y32, PostgreSQL: 42P07, MySQL: 42S01
            if (state.equals("42S01") || state.equals("X0Y32") || state.equals("42P07")
                    || msg.contains("already exists")) {
                return;
            }
            throw e;
        }
    }

    private static synchronized int nextId(Connection conn, String table) throws SQLException {
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT MAX(id) FROM " + table)) {
            int max = rs.next() ? rs.getInt(1) : 0;
            return max + 1;
        }
    }

    // ------------------------------------------------------------- vehicles

    public static synchronized Vehicle saveVehicle(Connection conn, Vehicle v) throws SQLException {
        if (v.id <= 0) {
            v.id = nextId(conn, "ws_vehicles");
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO ws_vehicles (id, contact_id, license_plate, vin, make_model, hsn_tsn, "
                    + "first_registration, mileage, mileage_unit, inspection_due, tire_size, archived, notes) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                ins.setInt(1, v.id);
                ins.setInt(2, v.contactId);
                ins.setString(3, safe(v.licensePlate));
                ins.setString(4, safe(v.vin).toUpperCase());
                ins.setString(5, safe(v.makeModel));
                ins.setString(6, safe(v.hsnTsn));
                ins.setString(7, safe(v.firstRegistration));
                ins.setLong(8, Math.max(0L, v.mileage));
                ins.setString(9, normUnit(v.mileageUnit));
                ins.setString(10, safe(v.inspectionDue));
                ins.setString(11, safe(v.tireSize));
                ins.setInt(12, v.archived ? 1 : 0);
                ins.setString(13, safe(v.notes));
                ins.executeUpdate();
            }
        } else {
            try (PreparedStatement upd = conn.prepareStatement(
                    "UPDATE ws_vehicles SET contact_id=?, license_plate=?, vin=?, make_model=?, hsn_tsn=?, "
                    + "first_registration=?, mileage=?, mileage_unit=?, inspection_due=?, tire_size=?, archived=?, notes=? "
                    + "WHERE id=?")) {
                upd.setInt(1, v.contactId);
                upd.setString(2, safe(v.licensePlate));
                upd.setString(3, safe(v.vin).toUpperCase());
                upd.setString(4, safe(v.makeModel));
                upd.setString(5, safe(v.hsnTsn));
                upd.setString(6, safe(v.firstRegistration));
                upd.setLong(7, Math.max(0L, v.mileage));
                upd.setString(8, normUnit(v.mileageUnit));
                upd.setString(9, safe(v.inspectionDue));
                upd.setString(10, safe(v.tireSize));
                upd.setInt(11, v.archived ? 1 : 0);
                upd.setString(12, safe(v.notes));
                upd.setInt(13, v.id);
                upd.executeUpdate();
            }
        }
        return v;
    }

    public static Vehicle loadVehicle(Connection conn, int vehicleId) throws SQLException {
        if (conn == null || vehicleId <= 0) {
            return null;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, contact_id, license_plate, vin, make_model, hsn_tsn, first_registration, "
                + "mileage, mileage_unit, inspection_due, tire_size, archived, notes "
                + "FROM ws_vehicles WHERE id=?")) {
            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? readVehicle(rs) : null;
            }
        }
    }

    public static Vehicle loadVehicleSafe(int vehicleId) {
        if (vehicleId <= 0 || !hasDatabase()) {
            return null;
        }
        try {
            return loadVehicle(connection(), vehicleId);
        } catch (Exception e) {
            Log.Debug(WorkshopStore.class, e);
            return null;
        }
    }

    public static List<Vehicle> loadVehiclesForContact(Connection conn, int contactId, boolean includeArchived) throws SQLException {
        List<Vehicle> list = new ArrayList<Vehicle>();
        if (conn == null || contactId <= 0) {
            return list;
        }
        String sql = "SELECT id, contact_id, license_plate, vin, make_model, hsn_tsn, first_registration, "
                + "mileage, mileage_unit, inspection_due, tire_size, archived, notes "
                + "FROM ws_vehicles WHERE contact_id=?"
                + (includeArchived ? "" : " AND archived=0")
                + " ORDER BY archived ASC, license_plate ASC, id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contactId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(readVehicle(rs));
                }
            }
        }
        return list;
    }

    public static List<Vehicle> loadVehiclesForContactSafe(int contactId, boolean includeArchived) {
        if (contactId <= 0 || !hasDatabase()) {
            return Collections.emptyList();
        }
        try {
            return loadVehiclesForContact(connection(), contactId, includeArchived);
        } catch (Exception e) {
            Log.Debug(WorkshopStore.class, e);
            return Collections.emptyList();
        }
    }

    public static List<Vehicle> loadAllActiveVehicles(Connection conn) throws SQLException {
        List<Vehicle> list = new ArrayList<Vehicle>();
        if (conn == null) {
            return list;
        }
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT id, contact_id, license_plate, vin, make_model, hsn_tsn, first_registration, "
                        + "mileage, mileage_unit, inspection_due, tire_size, archived, notes "
                        + "FROM ws_vehicles WHERE archived=0 ORDER BY inspection_due ASC, license_plate ASC")) {
            while (rs.next()) {
                list.add(readVehicle(rs));
            }
        }
        return list;
    }

    public static void deleteVehicle(Connection conn, int vehicleId) throws SQLException {
        if (conn == null || vehicleId <= 0) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM ws_tire_storage WHERE vehicle_id=?")) {
            ps.setInt(1, vehicleId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM ws_vehicles WHERE id=?")) {
            ps.setInt(1, vehicleId);
            ps.executeUpdate();
        }
    }

    private static Vehicle readVehicle(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.id = rs.getInt(1);
        v.contactId = rs.getInt(2);
        v.licensePlate = safe(rs.getString(3));
        v.vin = safe(rs.getString(4));
        v.makeModel = safe(rs.getString(5));
        v.hsnTsn = safe(rs.getString(6));
        v.firstRegistration = safe(rs.getString(7));
        v.mileage = rs.getLong(8);
        v.mileageUnit = normUnit(rs.getString(9));
        v.inspectionDue = safe(rs.getString(10));
        v.tireSize = safe(rs.getString(11));
        v.archived = rs.getInt(12) != 0;
        v.notes = safe(rs.getString(13));
        return v;
    }

    // ------------------------------------------------------------- tire storage

    public static synchronized TireStorage saveTireStorage(Connection conn, TireStorage t) throws SQLException {
        if (t.id <= 0) {
            t.id = nextId(conn, "ws_tire_storage");
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO ws_tire_storage (id, vehicle_id, season, storage_location, tread_fl, tread_fr, "
                    + "tread_rl, tread_rr, dot_codes, rim_type, wheel_bolts, condition_notes, stored_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                ins.setInt(1, t.id);
                ins.setInt(2, t.vehicleId);
                ins.setString(3, safe(t.season));
                ins.setString(4, safe(t.storageLocation));
                ins.setString(5, safe(t.treadFl));
                ins.setString(6, safe(t.treadFr));
                ins.setString(7, safe(t.treadRl));
                ins.setString(8, safe(t.treadRr));
                ins.setString(9, safe(t.dotCodes));
                ins.setString(10, safe(t.rimType));
                ins.setInt(11, t.wheelBoltsIncluded ? 1 : 0);
                ins.setString(12, safe(t.conditionNotes));
                ins.setString(13, safe(t.storedAt));
                ins.executeUpdate();
            }
        } else {
            try (PreparedStatement upd = conn.prepareStatement(
                    "UPDATE ws_tire_storage SET vehicle_id=?, season=?, storage_location=?, tread_fl=?, tread_fr=?, "
                    + "tread_rl=?, tread_rr=?, dot_codes=?, rim_type=?, wheel_bolts=?, condition_notes=?, stored_at=? "
                    + "WHERE id=?")) {
                upd.setInt(1, t.vehicleId);
                upd.setString(2, safe(t.season));
                upd.setString(3, safe(t.storageLocation));
                upd.setString(4, safe(t.treadFl));
                upd.setString(5, safe(t.treadFr));
                upd.setString(6, safe(t.treadRl));
                upd.setString(7, safe(t.treadRr));
                upd.setString(8, safe(t.dotCodes));
                upd.setString(9, safe(t.rimType));
                upd.setInt(10, t.wheelBoltsIncluded ? 1 : 0);
                upd.setString(11, safe(t.conditionNotes));
                upd.setString(12, safe(t.storedAt));
                upd.setInt(13, t.id);
                upd.executeUpdate();
            }
        }
        return t;
    }

    public static TireStorage loadLatestTireStorage(Connection conn, int vehicleId) throws SQLException {
        if (conn == null || vehicleId <= 0) {
            return null;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, vehicle_id, season, storage_location, tread_fl, tread_fr, tread_rl, tread_rr, "
                + "dot_codes, rim_type, wheel_bolts, condition_notes, stored_at "
                + "FROM ws_tire_storage WHERE vehicle_id=? ORDER BY id DESC")) {
            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? readTire(rs) : null;
            }
        }
    }

    public static TireStorage loadLatestTireStorageSafe(int vehicleId) {
        if (vehicleId <= 0 || !hasDatabase()) {
            return null;
        }
        try {
            return loadLatestTireStorage(connection(), vehicleId);
        } catch (Exception e) {
            Log.Debug(WorkshopStore.class, e);
            return null;
        }
    }

    public static List<TireStorage> loadAllTireStorage(Connection conn) throws SQLException {
        List<TireStorage> list = new ArrayList<TireStorage>();
        if (conn == null) {
            return list;
        }
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT id, vehicle_id, season, storage_location, tread_fl, tread_fr, tread_rl, tread_rr, "
                        + "dot_codes, rim_type, wheel_bolts, condition_notes, stored_at "
                        + "FROM ws_tire_storage ORDER BY storage_location ASC, id DESC")) {
            while (rs.next()) {
                list.add(readTire(rs));
            }
        }
        return list;
    }

    private static TireStorage readTire(ResultSet rs) throws SQLException {
        TireStorage t = new TireStorage();
        t.id = rs.getInt(1);
        t.vehicleId = rs.getInt(2);
        t.season = safe(rs.getString(3));
        t.storageLocation = safe(rs.getString(4));
        t.treadFl = safe(rs.getString(5));
        t.treadFr = safe(rs.getString(6));
        t.treadRl = safe(rs.getString(7));
        t.treadRr = safe(rs.getString(8));
        t.dotCodes = safe(rs.getString(9));
        t.rimType = safe(rs.getString(10));
        t.wheelBoltsIncluded = rs.getInt(11) != 0;
        t.conditionNotes = safe(rs.getString(12));
        t.storedAt = safe(rs.getString(13));
        return t;
    }

    // ------------------------------------------------------------- job data

    public static void saveJob(Connection conn, JobData j) throws SQLException {
        if (j.itemId <= 0) {
            return;
        }
        try (PreparedStatement upd = conn.prepareStatement(
                "UPDATE ws_job_data SET vehicle_id=?, plate_snapshot=?, mileage=?, mileage_unit=?, "
                + "dropoff_date=?, pickup_date=?, complaint=?, work_summary=?, internal_notes=?, "
                + "old_parts_handling=?, linked_quote_id=?, quote_number_snapshot=?, quote_total_snapshot=?, "
                + "overrun_approved=?, overrun_approved_at=? WHERE item_id=?")) {
            upd.setInt(1, j.vehicleId);
            upd.setString(2, safe(j.plateSnapshot));
            upd.setLong(3, Math.max(0L, j.mileage));
            upd.setString(4, normUnit(j.mileageUnit));
            upd.setString(5, safe(j.dropoffDate));
            upd.setString(6, safe(j.pickupDate));
            upd.setString(7, safe(j.complaint));
            upd.setString(8, safe(j.workSummary));
            upd.setString(9, safe(j.internalNotes));
            upd.setString(10, safe(j.oldPartsHandling));
            upd.setInt(11, j.linkedQuoteId);
            upd.setString(12, safe(j.quoteNumberSnapshot));
            upd.setString(13, safe(j.quoteTotalSnapshot));
            upd.setInt(14, j.overrunApproved ? 1 : 0);
            upd.setString(15, safe(j.overrunApprovedAt));
            upd.setInt(16, j.itemId);
            if (upd.executeUpdate() == 0) {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO ws_job_data (item_id, vehicle_id, plate_snapshot, mileage, mileage_unit, "
                        + "dropoff_date, pickup_date, complaint, work_summary, internal_notes, "
                        + "old_parts_handling, linked_quote_id, quote_number_snapshot, quote_total_snapshot, "
                        + "overrun_approved, overrun_approved_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    ins.setInt(1, j.itemId);
                    ins.setInt(2, j.vehicleId);
                    ins.setString(3, safe(j.plateSnapshot));
                    ins.setLong(4, Math.max(0L, j.mileage));
                    ins.setString(5, normUnit(j.mileageUnit));
                    ins.setString(6, safe(j.dropoffDate));
                    ins.setString(7, safe(j.pickupDate));
                    ins.setString(8, safe(j.complaint));
                    ins.setString(9, safe(j.workSummary));
                    ins.setString(10, safe(j.internalNotes));
                    ins.setString(11, safe(j.oldPartsHandling));
                    ins.setInt(12, j.linkedQuoteId);
                    ins.setString(13, safe(j.quoteNumberSnapshot));
                    ins.setString(14, safe(j.quoteTotalSnapshot));
                    ins.setInt(15, j.overrunApproved ? 1 : 0);
                    ins.setString(16, safe(j.overrunApprovedAt));
                    ins.executeUpdate();
                }
            }
        }
    }

    public static JobData loadJob(Connection conn, int itemId) throws SQLException {
        JobData j = new JobData();
        j.itemId = itemId;
        if (conn == null || itemId <= 0) {
            return j;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT vehicle_id, plate_snapshot, mileage, mileage_unit, dropoff_date, pickup_date, "
                + "complaint, work_summary, internal_notes, old_parts_handling, linked_quote_id, "
                + "quote_number_snapshot, quote_total_snapshot, overrun_approved, overrun_approved_at "
                + "FROM ws_job_data WHERE item_id=?")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    fillJob(j, rs);
                }
            }
        }
        return j;
    }

    public static JobData loadJobSafe(int itemId) {
        JobData j = new JobData();
        j.itemId = itemId;
        if (itemId <= 0 || !hasDatabase()) {
            return j;
        }
        try {
            return loadJob(connection(), itemId);
        } catch (Exception e) {
            Log.Debug(WorkshopStore.class, e);
            return j;
        }
    }

    public static boolean hasJob(Connection conn, int itemId) throws SQLException {
        if (conn == null || itemId <= 0) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM ws_job_data WHERE item_id=?")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static List<JobData> loadHistoryForVehicle(Connection conn, int vehicleId) throws SQLException {
        List<JobData> list = new ArrayList<JobData>();
        if (conn == null || vehicleId <= 0) {
            return list;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT item_id, vehicle_id, plate_snapshot, mileage, mileage_unit, dropoff_date, pickup_date, "
                + "complaint, work_summary, internal_notes, old_parts_handling, linked_quote_id, "
                + "quote_number_snapshot, quote_total_snapshot, overrun_approved, overrun_approved_at "
                + "FROM ws_job_data WHERE vehicle_id=? ORDER BY dropoff_date DESC, item_id DESC")) {
            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JobData j = new JobData();
                    j.itemId = rs.getInt(1);
                    j.vehicleId = rs.getInt(2);
                    j.plateSnapshot = safe(rs.getString(3));
                    j.mileage = rs.getLong(4);
                    j.mileageUnit = normUnit(rs.getString(5));
                    j.dropoffDate = safe(rs.getString(6));
                    j.pickupDate = safe(rs.getString(7));
                    j.complaint = safe(rs.getString(8));
                    j.workSummary = safe(rs.getString(9));
                    j.internalNotes = safe(rs.getString(10));
                    j.oldPartsHandling = safe(rs.getString(11));
                    j.linkedQuoteId = rs.getInt(12);
                    j.quoteNumberSnapshot = safe(rs.getString(13));
                    j.quoteTotalSnapshot = safe(rs.getString(14));
                    j.overrunApproved = rs.getInt(15) != 0;
                    j.overrunApprovedAt = safe(rs.getString(16));
                    list.add(j);
                }
            }
        }
        return list;
    }

    /**
     * Returns the highest previously recorded mileage (converted to km) for a
     * vehicle across its vehicle master record and all other job records
     * (excluding {@code excludeItemId}), or {@code null} if none exists.
     */
    public static Long maxRecordedMileageKm(Connection conn, int vehicleId, int excludeItemId) throws SQLException {
        if (conn == null || vehicleId <= 0) {
            return null;
        }
        Long maxKm = null;
        Vehicle v = loadVehicle(conn, vehicleId);
        if (v != null && v.mileage > 0) {
            maxKm = WorkshopValidator.toKilometers(v.mileage, v.mileageUnit);
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT mileage, mileage_unit FROM ws_job_data WHERE vehicle_id=? AND item_id<>?")) {
            ps.setInt(1, vehicleId);
            ps.setInt(2, excludeItemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long m = rs.getLong(1);
                    String u = normUnit(rs.getString(2));
                    if (m > 0) {
                        long km = WorkshopValidator.toKilometers(m, u);
                        if (maxKm == null || km > maxKm) {
                            maxKm = km;
                        }
                    }
                }
            }
        }
        return maxKm;
    }

    private static void fillJob(JobData j, ResultSet rs) throws SQLException {
        j.vehicleId = rs.getInt(1);
        j.plateSnapshot = safe(rs.getString(2));
        j.mileage = rs.getLong(3);
        j.mileageUnit = normUnit(rs.getString(4));
        j.dropoffDate = safe(rs.getString(5));
        j.pickupDate = safe(rs.getString(6));
        j.complaint = safe(rs.getString(7));
        j.workSummary = safe(rs.getString(8));
        j.internalNotes = safe(rs.getString(9));
        j.oldPartsHandling = safe(rs.getString(10));
        j.linkedQuoteId = rs.getInt(11);
        j.quoteNumberSnapshot = safe(rs.getString(12));
        j.quoteTotalSnapshot = safe(rs.getString(13));
        j.overrunApproved = rs.getInt(14) != 0;
        j.overrunApprovedAt = safe(rs.getString(15));
    }

    static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    static String normUnit(String u) {
        return "mi".equalsIgnoreCase(safe(u)) ? "mi" : "km";
    }

    static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    static BigDecimal parseDecimal(String s, BigDecimal fallback) {
        try {
            return new BigDecimal(s.trim().replace(',', '.'));
        } catch (Exception e) {
            return fallback;
        }
    }
}
