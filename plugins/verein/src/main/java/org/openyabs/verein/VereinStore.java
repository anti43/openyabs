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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mpv5.db.common.DatabaseConnection;
import mpv5.globals.GlobalSettings;
import mpv5.logging.Log;

/**
 * Per-database settings and cross-database JDBC persistence for the German
 * Non-Profit Association (e.V.) plugin ({@code verein_schema_version},
 * {@code ev_member_data}, {@code ev_invoice_data}).
 */
public final class VereinStore {

    public static final int SCHEMA_VERSION = 1;
    private static final String PREFIX = "ev.";

    public static final String SPHERE_IDEELL = "IDEELL";
    public static final String SPHERE_VERMOEGEN = "VERMOEGEN";
    public static final String SPHERE_ZWECKBETRIEB = "ZWECKBETRIEB";
    public static final String SPHERE_WIRTSCHAFTLICH = "WIRTSCHAFTLICH";
    public static final String[] SPHERES = {
        SPHERE_IDEELL, SPHERE_VERMOEGEN, SPHERE_ZWECKBETRIEB, SPHERE_WIRTSCHAFTLICH
    };

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_YOUTH = "YOUTH";
    public static final String STATUS_FAMILY = "FAMILY";
    public static final String STATUS_PASSIVE_FREE = "PASSIVE_FREE";
    public static final String STATUS_HONORARY = "HONORARY";
    public static final String STATUS_EXITED = "EXITED";
    public static final String[] STATUSES = {
        STATUS_ACTIVE, STATUS_YOUTH, STATUS_FAMILY, STATUS_PASSIVE_FREE, STATUS_HONORARY, STATUS_EXITED
    };

    public static final String BESCHEID_FREISTELLUNG = "FREISTELLUNG";
    public static final String BESCHEID_60A = "FESTSTELLUNG_60A";

    public static final String DONATION_SPENDE = "SPENDE";
    public static final String DONATION_MITGLIEDSBEITRAG = "MITGLIEDSBEITRAG";

    private VereinStore() {
    }

    public static final class Settings {

        public String creditorId = "";
        public String finanzamt = "";
        public String taxNumber = "";
        public String bescheidType = BESCHEID_FREISTELLUNG;
        public String freistellungDate = "";
        public String freistellungPeriod = "";
        public String purposeClause = "";
        public boolean duesTaxDeductible = false;
        public boolean kleinunternehmer = true;
        public boolean warnOnSave = true;

        public static Settings load() {
            Settings s = new Settings();
            try {
                if (GlobalSettings.hasProperty(PREFIX + "creditorId")) {
                    s.creditorId = safe(GlobalSettings.getProperty(PREFIX + "creditorId"));
                }
                if (GlobalSettings.hasProperty(PREFIX + "finanzamt")) {
                    s.finanzamt = safe(GlobalSettings.getProperty(PREFIX + "finanzamt"));
                }
                if (GlobalSettings.hasProperty(PREFIX + "taxNumber")) {
                    s.taxNumber = safe(GlobalSettings.getProperty(PREFIX + "taxNumber"));
                }
                if (GlobalSettings.hasProperty(PREFIX + "bescheidType")) {
                    String bt = safe(GlobalSettings.getProperty(PREFIX + "bescheidType"));
                    s.bescheidType = BESCHEID_60A.equalsIgnoreCase(bt) ? BESCHEID_60A : BESCHEID_FREISTELLUNG;
                }
                if (GlobalSettings.hasProperty(PREFIX + "freistellungDate")) {
                    s.freistellungDate = safe(GlobalSettings.getProperty(PREFIX + "freistellungDate"));
                }
                if (GlobalSettings.hasProperty(PREFIX + "freistellungPeriod")) {
                    s.freistellungPeriod = safe(GlobalSettings.getProperty(PREFIX + "freistellungPeriod"));
                }
                if (GlobalSettings.hasProperty(PREFIX + "purposeClause")) {
                    s.purposeClause = safe(GlobalSettings.getProperty(PREFIX + "purposeClause"));
                }
                if (GlobalSettings.hasProperty(PREFIX + "duesTaxDeductible")) {
                    s.duesTaxDeductible = GlobalSettings.getBooleanProperty(PREFIX + "duesTaxDeductible");
                }
                if (GlobalSettings.hasProperty(PREFIX + "kleinunternehmer")) {
                    s.kleinunternehmer = GlobalSettings.getBooleanProperty(PREFIX + "kleinunternehmer");
                }
                //the company setting of the core (small business exemption) wins; the plugin's
                //default (true) applies only while it has never been set
                Boolean core = mpv5.utils.export.SmallBusiness.configured();
                if (core != null) {
                    s.kleinunternehmer = core.booleanValue();
                }
                if (GlobalSettings.hasProperty(PREFIX + "warnOnSave")) {
                    s.warnOnSave = GlobalSettings.getBooleanProperty(PREFIX + "warnOnSave");
                }
            } catch (Throwable ignored) {
            }
            return s;
        }

        public void save() {
            GlobalSettings.setProperty(PREFIX + "creditorId", safe(creditorId));
            GlobalSettings.setProperty(PREFIX + "finanzamt", safe(finanzamt));
            GlobalSettings.setProperty(PREFIX + "taxNumber", safe(taxNumber));
            GlobalSettings.setProperty(PREFIX + "bescheidType",
                    BESCHEID_60A.equalsIgnoreCase(bescheidType) ? BESCHEID_60A : BESCHEID_FREISTELLUNG);
            GlobalSettings.setProperty(PREFIX + "freistellungDate", safe(freistellungDate));
            GlobalSettings.setProperty(PREFIX + "freistellungPeriod", safe(freistellungPeriod));
            GlobalSettings.setProperty(PREFIX + "purposeClause", safe(purposeClause));
            GlobalSettings.setProperty(PREFIX + "duesTaxDeductible", String.valueOf(duesTaxDeductible));
            GlobalSettings.setProperty(PREFIX + "kleinunternehmer", String.valueOf(kleinunternehmer));
            GlobalSettings.setProperty(PREFIX + "warnOnSave", String.valueOf(warnOnSave));
            GlobalSettings.save();
            //one setting for the core (invoice note, e-invoice, revenue limits) and the plugin
            try {
                Boolean core = mpv5.utils.export.SmallBusiness.configured();
                if (core == null || core.booleanValue() != kleinunternehmer) {
                    mpv5.utils.export.SmallBusiness.setEnabled(kleinunternehmer);
                }
            } catch (Throwable ignored) {
                //an older YaBS without the core setting
            }
        }
    }

    public static final class MemberData {

        public int contactId;
        public String memberId = "";
        public String status = STATUS_ACTIVE;
        public String tierSku = "";
        public String joinDate = "";
        public String exitDate = "";
        public String sepaMandateRef = "";
        public String sepaMandateDate = "";
        public String notes = "";
    }

    public static final class InvoiceData {

        public int itemId;
        public String taxSphere = SPHERE_IDEELL;
        public String fiscalYear = String.valueOf(LocalDate.now().getYear());
        public boolean isDonation;
        public String donationKind = DONATION_SPENDE;
        public boolean waiverReimbursement;
        public boolean sepaDirectDebit;
    }

    public static boolean hasDatabase() {
        try {
            DatabaseConnection dc = DatabaseConnection.getConnector();
            return dc != null && dc.getConnection() != null;
        } catch (Throwable t) {
            return false;
        }
    }

    public static Connection connection() throws SQLException {
        DatabaseConnection dc = DatabaseConnection.getConnector();
        Connection c = dc == null ? null : dc.getConnection();
        if (c == null) {
            throw new SQLException("No active database connection");
        }
        return c;
    }

    public static void initSchema(Connection conn) throws SQLException {
        if (conn == null) {
            return;
        }
        createTable(conn, "CREATE TABLE verein_schema_version ("
                + "version INTEGER NOT NULL PRIMARY KEY, "
                + "applied_at VARCHAR(32) NOT NULL"
                + ")");
        createTable(conn, "CREATE TABLE ev_member_data ("
                + "contact_id INTEGER NOT NULL PRIMARY KEY, "
                + "member_id VARCHAR(64), "
                + "status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE', "
                + "tier_sku VARCHAR(64), "
                + "join_date VARCHAR(32), "
                + "exit_date VARCHAR(32), "
                + "sepa_mandate_ref VARCHAR(64), "
                + "sepa_mandate_date VARCHAR(32), "
                + "notes VARCHAR(2000)"
                + ")");
        createTable(conn, "CREATE TABLE ev_invoice_data ("
                + "item_id INTEGER NOT NULL PRIMARY KEY, "
                + "tax_sphere VARCHAR(32) NOT NULL DEFAULT 'IDEELL', "
                + "fiscal_year VARCHAR(16), "
                + "is_donation INTEGER NOT NULL DEFAULT 0, "
                + "donation_kind VARCHAR(32) NOT NULL DEFAULT 'SPENDE', "
                + "waiver_reimbursement INTEGER NOT NULL DEFAULT 0, "
                + "sepa_direct_debit INTEGER NOT NULL DEFAULT 0"
                + ")");
        if (getSchemaVersion(conn) < SCHEMA_VERSION) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO verein_schema_version (version, applied_at) VALUES (?, ?)")) {
                ps.setInt(1, SCHEMA_VERSION);
                ps.setString(2, LocalDate.now().toString());
                ps.executeUpdate();
            } catch (SQLException ignored) {
            }
        }
    }

    public static int getSchemaVersion(Connection conn) throws SQLException {
        if (conn == null) {
            return 0;
        }
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT MAX(version) FROM verein_schema_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static void createTable(Connection conn, String ddl) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(ddl);
        } catch (SQLException e) {
            String state = e.getSQLState() == null ? "" : e.getSQLState();
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (state.equals("42S01") || state.equals("X0Y32") || state.equals("42P07")
                    || msg.contains("already exists")) {
                return;
            }
            throw e;
        }
    }

    // ------------------------------------------------------------- member CRUD

    public static void saveMember(Connection conn, MemberData m) throws SQLException {
        if (m.contactId <= 0) {
            return;
        }
        try (PreparedStatement upd = conn.prepareStatement(
                "UPDATE ev_member_data SET member_id=?, status=?, tier_sku=?, join_date=?, exit_date=?, "
                + "sepa_mandate_ref=?, sepa_mandate_date=?, notes=? WHERE contact_id=?")) {
            upd.setString(1, safe(m.memberId));
            upd.setString(2, normStatus(m.status));
            upd.setString(3, safe(m.tierSku));
            upd.setString(4, safe(m.joinDate));
            upd.setString(5, safe(m.exitDate));
            upd.setString(6, safe(m.sepaMandateRef));
            upd.setString(7, safe(m.sepaMandateDate));
            upd.setString(8, safe(m.notes));
            upd.setInt(9, m.contactId);
            if (upd.executeUpdate() == 0) {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO ev_member_data (contact_id, member_id, status, tier_sku, join_date, exit_date, "
                        + "sepa_mandate_ref, sepa_mandate_date, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    ins.setInt(1, m.contactId);
                    ins.setString(2, safe(m.memberId));
                    ins.setString(3, normStatus(m.status));
                    ins.setString(4, safe(m.tierSku));
                    ins.setString(5, safe(m.joinDate));
                    ins.setString(6, safe(m.exitDate));
                    ins.setString(7, safe(m.sepaMandateRef));
                    ins.setString(8, safe(m.sepaMandateDate));
                    ins.setString(9, safe(m.notes));
                    ins.executeUpdate();
                }
            }
        }
    }

    public static MemberData loadMember(Connection conn, int contactId) throws SQLException {
        if (conn == null || contactId <= 0) {
            return null;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT contact_id, member_id, status, tier_sku, join_date, exit_date, "
                + "sepa_mandate_ref, sepa_mandate_date, notes FROM ev_member_data WHERE contact_id=?")) {
            ps.setInt(1, contactId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? readMember(rs) : null;
            }
        }
    }

    public static MemberData loadMemberSafe(int contactId) {
        if (contactId <= 0 || !hasDatabase()) {
            return null;
        }
        try {
            return loadMember(connection(), contactId);
        } catch (Exception e) {
            Log.Debug(VereinStore.class, e);
            return null;
        }
    }

    public static List<MemberData> loadAllMembers(Connection conn) throws SQLException {
        List<MemberData> list = new ArrayList<MemberData>();
        if (conn == null) {
            return list;
        }
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT contact_id, member_id, status, tier_sku, join_date, exit_date, "
                        + "sepa_mandate_ref, sepa_mandate_date, notes FROM ev_member_data ORDER BY member_id ASC, contact_id ASC")) {
            while (rs.next()) {
                list.add(readMember(rs));
            }
        }
        return list;
    }

    private static MemberData readMember(ResultSet rs) throws SQLException {
        MemberData m = new MemberData();
        m.contactId = rs.getInt(1);
        m.memberId = safe(rs.getString(2));
        m.status = normStatus(rs.getString(3));
        m.tierSku = safe(rs.getString(4));
        m.joinDate = safe(rs.getString(5));
        m.exitDate = safe(rs.getString(6));
        m.sepaMandateRef = safe(rs.getString(7));
        m.sepaMandateDate = safe(rs.getString(8));
        m.notes = safe(rs.getString(9));
        return m;
    }

    // ------------------------------------------------------------- invoice CRUD

    public static void saveInvoice(Connection conn, InvoiceData d) throws SQLException {
        if (d.itemId <= 0) {
            return;
        }
        try (PreparedStatement upd = conn.prepareStatement(
                "UPDATE ev_invoice_data SET tax_sphere=?, fiscal_year=?, is_donation=?, donation_kind=?, "
                + "waiver_reimbursement=?, sepa_direct_debit=? WHERE item_id=?")) {
            upd.setString(1, normSphere(d.taxSphere));
            upd.setString(2, safe(d.fiscalYear));
            upd.setInt(3, d.isDonation ? 1 : 0);
            upd.setString(4, DONATION_MITGLIEDSBEITRAG.equalsIgnoreCase(d.donationKind) ? DONATION_MITGLIEDSBEITRAG : DONATION_SPENDE);
            upd.setInt(5, d.waiverReimbursement ? 1 : 0);
            upd.setInt(6, d.sepaDirectDebit ? 1 : 0);
            upd.setInt(7, d.itemId);
            if (upd.executeUpdate() == 0) {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO ev_invoice_data (item_id, tax_sphere, fiscal_year, is_donation, donation_kind, "
                        + "waiver_reimbursement, sepa_direct_debit) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    ins.setInt(1, d.itemId);
                    ins.setString(2, normSphere(d.taxSphere));
                    ins.setString(3, safe(d.fiscalYear));
                    ins.setInt(4, d.isDonation ? 1 : 0);
                    ins.setString(5, DONATION_MITGLIEDSBEITRAG.equalsIgnoreCase(d.donationKind) ? DONATION_MITGLIEDSBEITRAG : DONATION_SPENDE);
                    ins.setInt(6, d.waiverReimbursement ? 1 : 0);
                    ins.setInt(7, d.sepaDirectDebit ? 1 : 0);
                    ins.executeUpdate();
                }
            }
        }
    }

    public static InvoiceData loadInvoice(Connection conn, int itemId) throws SQLException {
        InvoiceData d = new InvoiceData();
        d.itemId = itemId;
        if (conn == null || itemId <= 0) {
            return d;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT tax_sphere, fiscal_year, is_donation, donation_kind, waiver_reimbursement, sepa_direct_debit "
                + "FROM ev_invoice_data WHERE item_id=?")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    d.taxSphere = normSphere(rs.getString(1));
                    d.fiscalYear = safe(rs.getString(2));
                    d.isDonation = rs.getInt(3) != 0;
                    d.donationKind = safe(rs.getString(4));
                    d.waiverReimbursement = rs.getInt(5) != 0;
                    d.sepaDirectDebit = rs.getInt(6) != 0;
                }
            }
        }
        return d;
    }

    public static InvoiceData loadInvoiceSafe(int itemId) {
        InvoiceData d = new InvoiceData();
        d.itemId = itemId;
        if (itemId <= 0 || !hasDatabase()) {
            return d;
        }
        try {
            return loadInvoice(connection(), itemId);
        } catch (Exception e) {
            Log.Debug(VereinStore.class, e);
            return d;
        }
    }

    public static boolean hasInvoice(Connection conn, int itemId) throws SQLException {
        if (conn == null || itemId <= 0) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM ev_invoice_data WHERE item_id=?")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static List<InvoiceData> loadAllInvoices(Connection conn) throws SQLException {
        List<InvoiceData> list = new ArrayList<InvoiceData>();
        if (conn == null) {
            return list;
        }
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT item_id, tax_sphere, fiscal_year, is_donation, donation_kind, waiver_reimbursement, sepa_direct_debit "
                        + "FROM ev_invoice_data")) {
            while (rs.next()) {
                InvoiceData d = new InvoiceData();
                d.itemId = rs.getInt(1);
                d.taxSphere = normSphere(rs.getString(2));
                d.fiscalYear = safe(rs.getString(3));
                d.isDonation = rs.getInt(4) != 0;
                d.donationKind = safe(rs.getString(5));
                d.waiverReimbursement = rs.getInt(6) != 0;
                d.sepaDirectDebit = rs.getInt(7) != 0;
                list.add(d);
            }
        }
        return list;
    }

    public static List<MemberData> loadAllMembersSafe() {
        if (!hasDatabase()) {
            return Collections.emptyList();
        }
        try {
            return loadAllMembers(connection());
        } catch (Exception e) {
            Log.Debug(VereinStore.class, e);
            return Collections.emptyList();
        }
    }

    static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    static String normSphere(String s) {
        String v = safe(s).toUpperCase();
        for (String sp : SPHERES) {
            if (sp.equals(v)) {
                return sp;
            }
        }
        return SPHERE_IDEELL;
    }

    static String normStatus(String s) {
        String v = safe(s).toUpperCase();
        for (String st : STATUSES) {
            if (st.equals(v)) {
                return st;
            }
        }
        return STATUS_ACTIVE;
    }
}
