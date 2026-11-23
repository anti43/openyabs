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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import mpv5.db.common.DatabaseConnection;
import mpv5.globals.GlobalSettings;
import mpv5.logging.Log;
import org.openyabs.handwerk.BoqDocument.BoqPosition;
import org.openyabs.handwerk.BoqDocument.PositionType;

/**
 * Per-database settings and cross-database JDBC persistence for the Skilled
 * Trades &amp; Construction / Handwerksbetriebe plugin ({@code trades_*}
 * tables).
 */
public final class HandwerkStore {

    public static final int SCHEMA_VERSION = 1;
    private static final String PREFIX = "trades.";

    public static final String CUSTOMER_PRIVATE = "PRIVATE";
    public static final String CUSTOMER_COMMERCIAL = "COMMERCIAL";
    public static final String CUSTOMER_CONSTRUCTION_13B = "CONSTRUCTION_13B";
    public static final String CUSTOMER_PUBLIC = "PUBLIC";

    public static final String CONTRACT_BGB = "BGB";
    public static final String CONTRACT_VOB_B = "VOB_B";

    public static final String BASIS_GROSS = "GROSS";
    public static final String BASIS_NET = "NET";

    public static final String COST_LABOR = "LABOR";
    public static final String COST_TRAVEL = "TRAVEL";
    public static final String COST_MACHINE = "MACHINE";
    public static final String COST_MATERIAL = "MATERIAL";
    public static final String COST_OTHER = "OTHER";

    public static final String ROLE_MASTER = "MASTER";
    public static final String ROLE_JOURNEYMAN = "JOURNEYMAN";
    public static final String ROLE_APPRENTICE = "APPRENTICE";

    private HandwerkStore() {
    }

    public static final class Settings {

        public String chamberName = "";
        public String chamberNumber = "";
        public String ownFreistellungTaxNo = "";
        public String ownFreistellungSecNo = "";
        public String ownFreistellungValidUntil = "";
        public String ownUst1TgValidUntil = "";
        public BigDecimal rateMaster = new BigDecimal("82.00");
        public BigDecimal rateJourneyman = new BigDecimal("68.00");
        public BigDecimal rateApprentice = new BigDecimal("38.00");
        public BigDecimal rateTravel = new BigDecimal("49.00");
        public BigDecimal datanormMarkupPercent = new BigDecimal("30.00");
        public String defaultContractType = CONTRACT_BGB;
        public BigDecimal defaultRetentionPercent = new BigDecimal("5.00");
        public String defaultRetentionBasis = BASIS_GROSS;
        public int certLeadDays = 30;
        public boolean warnOnSave = true;

        public static Settings load() {
            Settings s = new Settings();
            try {
                s.chamberName = getStr("chamberName", s.chamberName);
                s.chamberNumber = getStr("chamberNumber", s.chamberNumber);
                s.ownFreistellungTaxNo = getStr("ownFreistellungTaxNo", s.ownFreistellungTaxNo);
                s.ownFreistellungSecNo = getStr("ownFreistellungSecNo", s.ownFreistellungSecNo);
                s.ownFreistellungValidUntil = getStr("ownFreistellungValidUntil", s.ownFreistellungValidUntil);
                s.ownUst1TgValidUntil = getStr("ownUst1TgValidUntil", s.ownUst1TgValidUntil);
                s.rateMaster = parseDecimal(getStr("rateMaster", null), s.rateMaster);
                s.rateJourneyman = parseDecimal(getStr("rateJourneyman", null), s.rateJourneyman);
                s.rateApprentice = parseDecimal(getStr("rateApprentice", null), s.rateApprentice);
                s.rateTravel = parseDecimal(getStr("rateTravel", null), s.rateTravel);
                s.datanormMarkupPercent = parseDecimal(getStr("datanormMarkupPercent", null), s.datanormMarkupPercent);
                String ct = getStr("defaultContractType", s.defaultContractType);
                s.defaultContractType = CONTRACT_VOB_B.equalsIgnoreCase(ct) ? CONTRACT_VOB_B : CONTRACT_BGB;
                s.defaultRetentionPercent = parseDecimal(getStr("defaultRetentionPercent", null), s.defaultRetentionPercent);
                String rb = getStr("defaultRetentionBasis", s.defaultRetentionBasis);
                s.defaultRetentionBasis = BASIS_NET.equalsIgnoreCase(rb) ? BASIS_NET : BASIS_GROSS;
                s.certLeadDays = parseInt(getStr("certLeadDays", null), s.certLeadDays);
                if (GlobalSettings.hasProperty(PREFIX + "warnOnSave")) {
                    s.warnOnSave = GlobalSettings.getBooleanProperty(PREFIX + "warnOnSave");
                }
            } catch (Throwable ignored) {
            }
            return s;
        }

        public void save() {
            GlobalSettings.setProperty(PREFIX + "chamberName", safe(chamberName));
            GlobalSettings.setProperty(PREFIX + "chamberNumber", safe(chamberNumber));
            GlobalSettings.setProperty(PREFIX + "ownFreistellungTaxNo", safe(ownFreistellungTaxNo));
            GlobalSettings.setProperty(PREFIX + "ownFreistellungSecNo", safe(ownFreistellungSecNo));
            GlobalSettings.setProperty(PREFIX + "ownFreistellungValidUntil", safe(ownFreistellungValidUntil));
            GlobalSettings.setProperty(PREFIX + "ownUst1TgValidUntil", safe(ownUst1TgValidUntil));
            GlobalSettings.setProperty(PREFIX + "rateMaster", nonNull(rateMaster, BigDecimal.ZERO).toPlainString());
            GlobalSettings.setProperty(PREFIX + "rateJourneyman", nonNull(rateJourneyman, BigDecimal.ZERO).toPlainString());
            GlobalSettings.setProperty(PREFIX + "rateApprentice", nonNull(rateApprentice, BigDecimal.ZERO).toPlainString());
            GlobalSettings.setProperty(PREFIX + "rateTravel", nonNull(rateTravel, BigDecimal.ZERO).toPlainString());
            GlobalSettings.setProperty(PREFIX + "datanormMarkupPercent", nonNull(datanormMarkupPercent, BigDecimal.ZERO).toPlainString());
            GlobalSettings.setProperty(PREFIX + "defaultContractType",
                    CONTRACT_VOB_B.equalsIgnoreCase(defaultContractType) ? CONTRACT_VOB_B : CONTRACT_BGB);
            GlobalSettings.setProperty(PREFIX + "defaultRetentionPercent", nonNull(defaultRetentionPercent, BigDecimal.ZERO).toPlainString());
            GlobalSettings.setProperty(PREFIX + "defaultRetentionBasis",
                    BASIS_NET.equalsIgnoreCase(defaultRetentionBasis) ? BASIS_NET : BASIS_GROSS);
            GlobalSettings.setProperty(PREFIX + "certLeadDays", String.valueOf(Math.max(1, certLeadDays)));
            GlobalSettings.setProperty(PREFIX + "warnOnSave", String.valueOf(warnOnSave));
            GlobalSettings.save();
        }

        private static String getStr(String key, String fallback) {
            return GlobalSettings.hasProperty(PREFIX + key)
                    ? safe(GlobalSettings.getProperty(PREFIX + key))
                    : fallback;
        }
    }

    public static final class ContactMeta {

        public int contactId;
        public String customerKind = CUSTOMER_PRIVATE;
        public String reverseChargeCertNo = "";
        public String reverseChargeCertUntil = "";
        public String withholdingExemptionTaxNo = "";
        public String withholdingExemptionSecNo = "";
        public String withholdingExemptionUntil = "";
        public String notes = "";
    }

    public static final class Site {

        public int id;
        public int contactId;
        public String projectNo = "";
        public String siteName = "";
        public String street = "";
        public String zip = "";
        public String city = "";
        public String country = "DE";
        public String contractType = CONTRACT_BGB;
        public BigDecimal retentionPercent = BigDecimal.ZERO;
        public String retentionBasis = BASIS_GROSS;
        public String acceptanceDate = "";
        public int warrantyYears = 5;
        public String warrantyUntil = "";
        public String maintenanceDue = "";
        public boolean archived;
        public String notes = "";

        public String formattedAddress() {
            StringBuilder sb = new StringBuilder();
            if (!safe(street).isEmpty()) {
                sb.append(safe(street));
            }
            String cityLine = (safe(zip) + " " + safe(city)).trim();
            if (!cityLine.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(cityLine);
            }
            if (!safe(country).isEmpty() && !"DE".equalsIgnoreCase(safe(country))) {
                if (sb.length() > 0) {
                    sb.append(" (").append(safe(country)).append(")");
                } else {
                    sb.append(safe(country));
                }
            }
            return sb.toString();
        }

        public String displayLabel() {
            String p = safe(projectNo);
            String n = safe(siteName);
            String addr = formattedAddress();
            StringBuilder sb = new StringBuilder();
            if (!p.isEmpty()) {
                sb.append(p);
            }
            if (!n.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" \u2013 ");
                }
                sb.append(n);
            }
            if (!addr.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" (").append(addr).append(")");
                } else {
                    sb.append(addr);
                }
            }
            return sb.length() > 0 ? sb.toString() : "#" + id;
        }

        @Override
        public String toString() {
            return displayLabel();
        }
    }

    public static final class ItemMeta {

        public int itemId;
        public int siteId;
        public String siteSnapshot = "";
        public boolean laborTaxCreditEnabled;
        public boolean cashPayment;
        public boolean privateRetentionNotice;
        public boolean reverseChargeEnabled;
        public BigDecimal retentionPercent = BigDecimal.ZERO;
        public String retentionBasis = BASIS_GROSS;
        public BigDecimal retentionAmount = BigDecimal.ZERO;
        public String retentionReleaseDate = "";
        public String retentionBondRef = "";
        public int boqId;
        public String notes = "";
    }

    public static final class LineCostType {

        public int itemId;
        public int subItemId;
        public String costType = COST_LABOR;
        public BigDecimal laborSharePercent = new BigDecimal("100.00");
    }

    public static final class Measurement {

        public int id;
        public int siteId;
        public int itemId;
        public int subItemId;
        public String ordinalNo = "";
        public String workDate = "";
        public String description = "";
        public String formula = "";
        public BigDecimal computedQty = BigDecimal.ZERO;
        public String unit = "m2";
        public String notes = "";
    }

    public static final class TimeSheet {

        public int id;
        public int siteId;
        public int itemId;
        public String workDate = "";
        public String workerName = "";
        public String workerRole = ROLE_JOURNEYMAN;
        public BigDecimal hours = BigDecimal.ZERO;
        public BigDecimal travelKm = BigDecimal.ZERO;
        public String description = "";
        public String signedBy = "";
        public boolean billed;
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

    private static String largeTextType(Connection conn) {
        try {
            DatabaseMetaData md = conn.getMetaData();
            String product = md == null || md.getDatabaseProductName() == null
                    ? "" : md.getDatabaseProductName().toLowerCase(Locale.ROOT);
            if (product.contains("postgres")) {
                return "TEXT";
            }
            if (product.contains("mysql") || product.contains("mariadb")) {
                return "LONGTEXT";
            }
        } catch (SQLException ignored) {
        }
        return "CLOB";
    }

    /**
     * Creates all {@code trades_*} tables using cross-database SQL (H2, Derby,
     * MySQL/MariaDB, PostgreSQL). Safe to invoke repeatedly.
     */
    public static void initSchema(Connection conn) throws SQLException {
        if (conn == null) {
            return;
        }
        String bigText = largeTextType(conn);

        createTable(conn, "CREATE TABLE trades_schema_version ("
                + "version INTEGER NOT NULL PRIMARY KEY, "
                + "applied_at VARCHAR(32) NOT NULL"
                + ")");

        createTable(conn, "CREATE TABLE trades_contact_meta ("
                + "contact_id INTEGER NOT NULL PRIMARY KEY, "
                + "customer_kind VARCHAR(32) NOT NULL DEFAULT 'PRIVATE', "
                + "reverse_charge_cert_no VARCHAR(64), "
                + "reverse_charge_cert_until VARCHAR(32), "
                + "withholding_exemption_tax_no VARCHAR(64), "
                + "withholding_exemption_sec_no VARCHAR(64), "
                + "withholding_exemption_until VARCHAR(32), "
                + "notes VARCHAR(2000)"
                + ")");

        createTable(conn, "CREATE TABLE trades_sites ("
                + "id INTEGER NOT NULL PRIMARY KEY, "
                + "contact_id INTEGER NOT NULL, "
                + "project_no VARCHAR(64), "
                + "site_name VARCHAR(255), "
                + "street VARCHAR(255), "
                + "zip VARCHAR(32), "
                + "city VARCHAR(128), "
                + "country VARCHAR(16), "
                + "contract_type VARCHAR(16) NOT NULL DEFAULT 'BGB', "
                + "retention_percent DECIMAL(6,2) NOT NULL DEFAULT 0, "
                + "retention_basis VARCHAR(16) NOT NULL DEFAULT 'GROSS', "
                + "acceptance_date VARCHAR(32), "
                + "warranty_years INTEGER NOT NULL DEFAULT 5, "
                + "warranty_until VARCHAR(32), "
                + "maintenance_due VARCHAR(32), "
                + "archived INTEGER NOT NULL DEFAULT 0, "
                + "notes VARCHAR(2000)"
                + ")");

        createTable(conn, "CREATE TABLE trades_item_meta ("
                + "item_id INTEGER NOT NULL PRIMARY KEY, "
                + "site_id INTEGER NOT NULL DEFAULT 0, "
                + "site_snapshot VARCHAR(512), "
                + "labor_tax_credit_enabled INTEGER NOT NULL DEFAULT 0, "
                + "cash_payment INTEGER NOT NULL DEFAULT 0, "
                + "private_retention_notice INTEGER NOT NULL DEFAULT 0, "
                + "reverse_charge_enabled INTEGER NOT NULL DEFAULT 0, "
                + "retention_percent DECIMAL(6,2) NOT NULL DEFAULT 0, "
                + "retention_basis VARCHAR(16) NOT NULL DEFAULT 'GROSS', "
                + "retention_amount DECIMAL(14,2) NOT NULL DEFAULT 0, "
                + "retention_release_date VARCHAR(32), "
                + "retention_bond_ref VARCHAR(128), "
                + "boq_id INTEGER NOT NULL DEFAULT 0, "
                + "notes VARCHAR(2000)"
                + ")");

        createTable(conn, "CREATE TABLE trades_line_cost_type ("
                + "item_id INTEGER NOT NULL, "
                + "subitem_id INTEGER NOT NULL, "
                + "cost_type VARCHAR(32) NOT NULL DEFAULT 'LABOR', "
                + "labor_share_percent DECIMAL(6,2) NOT NULL DEFAULT 100, "
                + "PRIMARY KEY (item_id, subitem_id)"
                + ")");

        createTable(conn, "CREATE TABLE trades_boq_documents ("
                + "id INTEGER NOT NULL PRIMARY KEY, "
                + "item_id INTEGER NOT NULL DEFAULT 0, "
                + "format_code VARCHAR(32), "
                + "exchange_phase VARCHAR(16), "
                + "project_name VARCHAR(255), "
                + "boq_label VARCHAR(255), "
                + "currency VARCHAR(8), "
                + "raw_source " + bigText + ", "
                + "imported_at VARCHAR(32)"
                + ")");

        createTable(conn, "CREATE TABLE trades_boq_positions ("
                + "id INTEGER NOT NULL PRIMARY KEY, "
                + "boq_id INTEGER NOT NULL, "
                + "subitem_id INTEGER NOT NULL DEFAULT 0, "
                + "ordinal_no VARCHAR(64), "
                + "source_item_id VARCHAR(64), "
                + "category_path VARCHAR(512), "
                + "position_type VARCHAR(24), "
                + "qty DECIMAL(14,4) NOT NULL DEFAULT 0, "
                + "unit VARCHAR(32), "
                + "unit_price DECIMAL(14,4) NOT NULL DEFAULT 0, "
                + "short_text VARCHAR(1000), "
                + "long_text " + bigText + ", "
                + "bidder_text VARCHAR(2000)"
                + ")");

        createTable(conn, "CREATE TABLE trades_measurements ("
                + "id INTEGER NOT NULL PRIMARY KEY, "
                + "site_id INTEGER NOT NULL DEFAULT 0, "
                + "item_id INTEGER NOT NULL DEFAULT 0, "
                + "subitem_id INTEGER NOT NULL DEFAULT 0, "
                + "ordinal_no VARCHAR(64), "
                + "work_date VARCHAR(32), "
                + "description VARCHAR(512), "
                + "formula VARCHAR(512), "
                + "computed_qty DECIMAL(14,4) NOT NULL DEFAULT 0, "
                + "unit VARCHAR(32), "
                + "notes VARCHAR(1000)"
                + ")");

        createTable(conn, "CREATE TABLE trades_time_sheets ("
                + "id INTEGER NOT NULL PRIMARY KEY, "
                + "site_id INTEGER NOT NULL DEFAULT 0, "
                + "item_id INTEGER NOT NULL DEFAULT 0, "
                + "work_date VARCHAR(32), "
                + "worker_name VARCHAR(128), "
                + "worker_role VARCHAR(32), "
                + "hours DECIMAL(8,2) NOT NULL DEFAULT 0, "
                + "travel_km DECIMAL(8,2) NOT NULL DEFAULT 0, "
                + "description VARCHAR(2000), "
                + "signed_by VARCHAR(128), "
                + "billed INTEGER NOT NULL DEFAULT 0"
                + ")");

        if (getSchemaVersion(conn) < SCHEMA_VERSION) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO trades_schema_version (version, applied_at) VALUES (?, ?)")) {
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
                ResultSet rs = st.executeQuery("SELECT MAX(version) FROM trades_schema_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static void createTable(Connection conn, String ddl) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(ddl);
        } catch (SQLException e) {
            String state = e.getSQLState() == null ? "" : e.getSQLState();
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase(Locale.ROOT);
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

    // --------------------------------------------------------- contact meta

    public static ContactMeta loadContactMeta(Connection conn, int contactId) throws SQLException {
        ContactMeta m = new ContactMeta();
        m.contactId = contactId;
        if (conn == null || contactId <= 0) {
            return m;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT contact_id, customer_kind, reverse_charge_cert_no, reverse_charge_cert_until, "
                + "withholding_exemption_tax_no, withholding_exemption_sec_no, withholding_exemption_until, notes "
                + "FROM trades_contact_meta WHERE contact_id = ?")) {
            ps.setInt(1, contactId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.customerKind = safeDefault(rs.getString(2), CUSTOMER_PRIVATE);
                    m.reverseChargeCertNo = safe(rs.getString(3));
                    m.reverseChargeCertUntil = safe(rs.getString(4));
                    m.withholdingExemptionTaxNo = safe(rs.getString(5));
                    m.withholdingExemptionSecNo = safe(rs.getString(6));
                    m.withholdingExemptionUntil = safe(rs.getString(7));
                    m.notes = safe(rs.getString(8));
                }
            }
        }
        return m;
    }

    public static ContactMeta loadContactMetaSafe(int contactId) {
        if (contactId <= 0 || !hasDatabase()) {
            ContactMeta m = new ContactMeta();
            m.contactId = contactId;
            return m;
        }
        try {
            return loadContactMeta(connection(), contactId);
        } catch (Exception e) {
            Log.Debug(HandwerkStore.class, e);
            ContactMeta m = new ContactMeta();
            m.contactId = contactId;
            return m;
        }
    }

    public static synchronized void saveContactMeta(Connection conn, ContactMeta m) throws SQLException {
        if (conn == null || m == null || m.contactId <= 0) {
            return;
        }
        boolean exists;
        try (PreparedStatement chk = conn.prepareStatement(
                "SELECT 1 FROM trades_contact_meta WHERE contact_id = ?")) {
            chk.setInt(1, m.contactId);
            try (ResultSet rs = chk.executeQuery()) {
                exists = rs.next();
            }
        }
        if (exists) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE trades_contact_meta SET customer_kind = ?, reverse_charge_cert_no = ?, "
                    + "reverse_charge_cert_until = ?, withholding_exemption_tax_no = ?, "
                    + "withholding_exemption_sec_no = ?, withholding_exemption_until = ?, notes = ? "
                    + "WHERE contact_id = ?")) {
                ps.setString(1, safeDefault(m.customerKind, CUSTOMER_PRIVATE));
                ps.setString(2, safe(m.reverseChargeCertNo));
                ps.setString(3, safe(m.reverseChargeCertUntil));
                ps.setString(4, safe(m.withholdingExemptionTaxNo));
                ps.setString(5, safe(m.withholdingExemptionSecNo));
                ps.setString(6, safe(m.withholdingExemptionUntil));
                ps.setString(7, safe(m.notes));
                ps.setInt(8, m.contactId);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO trades_contact_meta (contact_id, customer_kind, reverse_charge_cert_no, "
                    + "reverse_charge_cert_until, withholding_exemption_tax_no, withholding_exemption_sec_no, "
                    + "withholding_exemption_until, notes) VALUES (?,?,?,?,?,?,?,?)")) {
                ps.setInt(1, m.contactId);
                ps.setString(2, safeDefault(m.customerKind, CUSTOMER_PRIVATE));
                ps.setString(3, safe(m.reverseChargeCertNo));
                ps.setString(4, safe(m.reverseChargeCertUntil));
                ps.setString(5, safe(m.withholdingExemptionTaxNo));
                ps.setString(6, safe(m.withholdingExemptionSecNo));
                ps.setString(7, safe(m.withholdingExemptionUntil));
                ps.setString(8, safe(m.notes));
                ps.executeUpdate();
            }
        }
    }

    public static List<ContactMeta> loadAllContactMetas(Connection conn) throws SQLException {
        List<ContactMeta> list = new ArrayList<ContactMeta>();
        if (conn == null) {
            return list;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT contact_id, customer_kind, reverse_charge_cert_no, reverse_charge_cert_until, "
                + "withholding_exemption_tax_no, withholding_exemption_sec_no, withholding_exemption_until, notes "
                + "FROM trades_contact_meta ORDER BY contact_id");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ContactMeta m = new ContactMeta();
                m.contactId = rs.getInt(1);
                m.customerKind = safeDefault(rs.getString(2), CUSTOMER_PRIVATE);
                m.reverseChargeCertNo = safe(rs.getString(3));
                m.reverseChargeCertUntil = safe(rs.getString(4));
                m.withholdingExemptionTaxNo = safe(rs.getString(5));
                m.withholdingExemptionSecNo = safe(rs.getString(6));
                m.withholdingExemptionUntil = safe(rs.getString(7));
                m.notes = safe(rs.getString(8));
                list.add(m);
            }
        }
        return list;
    }

    // ----------------------------------------------------------------- sites

    public static synchronized Site saveSite(Connection conn, Site s) throws SQLException {
        if (s.id <= 0) {
            s.id = nextId(conn, "trades_sites");
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO trades_sites (id, contact_id, project_no, site_name, street, zip, city, country, "
                    + "contract_type, retention_percent, retention_basis, acceptance_date, warranty_years, "
                    + "warranty_until, maintenance_due, archived, notes) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
                bindSite(ps, s, true);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE trades_sites SET contact_id = ?, project_no = ?, site_name = ?, street = ?, zip = ?, "
                    + "city = ?, country = ?, contract_type = ?, retention_percent = ?, retention_basis = ?, "
                    + "acceptance_date = ?, warranty_years = ?, warranty_until = ?, maintenance_due = ?, "
                    + "archived = ?, notes = ? WHERE id = ?")) {
                bindSite(ps, s, false);
                ps.executeUpdate();
            }
        }
        return s;
    }

    private static void bindSite(PreparedStatement ps, Site s, boolean insert) throws SQLException {
        int i = 1;
        if (insert) {
            ps.setInt(i++, s.id);
        }
        ps.setInt(i++, s.contactId);
        ps.setString(i++, safe(s.projectNo));
        ps.setString(i++, safe(s.siteName));
        ps.setString(i++, safe(s.street));
        ps.setString(i++, safe(s.zip));
        ps.setString(i++, safe(s.city));
        ps.setString(i++, safeDefault(s.country, "DE"));
        ps.setString(i++, CONTRACT_VOB_B.equalsIgnoreCase(s.contractType) ? CONTRACT_VOB_B : CONTRACT_BGB);
        ps.setBigDecimal(i++, nonNull(s.retentionPercent, BigDecimal.ZERO));
        ps.setString(i++, BASIS_NET.equalsIgnoreCase(s.retentionBasis) ? BASIS_NET : BASIS_GROSS);
        ps.setString(i++, safe(s.acceptanceDate));
        ps.setInt(i++, s.warrantyYears > 0 ? s.warrantyYears : (CONTRACT_VOB_B.equalsIgnoreCase(s.contractType) ? 4 : 5));
        ps.setString(i++, safe(s.warrantyUntil));
        ps.setString(i++, safe(s.maintenanceDue));
        ps.setInt(i++, s.archived ? 1 : 0);
        ps.setString(i++, safe(s.notes));
        if (!insert) {
            ps.setInt(i, s.id);
        }
    }

    public static Site loadSite(Connection conn, int id) throws SQLException {
        if (conn == null || id <= 0) {
            return null;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, contact_id, project_no, site_name, street, zip, city, country, contract_type, "
                + "retention_percent, retention_basis, acceptance_date, warranty_years, warranty_until, "
                + "maintenance_due, archived, notes FROM trades_sites WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? readSite(rs) : null;
            }
        }
    }

    public static Site loadSiteSafe(int id) {
        if (id <= 0 || !hasDatabase()) {
            return null;
        }
        try {
            return loadSite(connection(), id);
        } catch (Exception e) {
            Log.Debug(HandwerkStore.class, e);
            return null;
        }
    }

    public static List<Site> loadSitesForContact(Connection conn, int contactId, boolean includeArchived) throws SQLException {
        List<Site> out = new ArrayList<Site>();
        if (conn == null || contactId <= 0) {
            return out;
        }
        String sql = "SELECT id, contact_id, project_no, site_name, street, zip, city, country, contract_type, "
                + "retention_percent, retention_basis, acceptance_date, warranty_years, warranty_until, "
                + "maintenance_due, archived, notes FROM trades_sites WHERE contact_id = ?"
                + (includeArchived ? "" : " AND archived = 0")
                + " ORDER BY archived ASC, id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contactId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(readSite(rs));
                }
            }
        }
        return out;
    }

    public static List<Site> loadSitesForContactSafe(int contactId, boolean includeArchived) {
        if (contactId <= 0 || !hasDatabase()) {
            return Collections.emptyList();
        }
        try {
            return loadSitesForContact(connection(), contactId, includeArchived);
        } catch (Exception e) {
            Log.Debug(HandwerkStore.class, e);
            return Collections.emptyList();
        }
    }

    public static List<Site> loadAllSites(Connection conn, boolean includeArchived) throws SQLException {
        List<Site> out = new ArrayList<Site>();
        if (conn == null) {
            return out;
        }
        String sql = "SELECT id, contact_id, project_no, site_name, street, zip, city, country, contract_type, "
                + "retention_percent, retention_basis, acceptance_date, warranty_years, warranty_until, "
                + "maintenance_due, archived, notes FROM trades_sites"
                + (includeArchived ? "" : " WHERE archived = 0")
                + " ORDER BY archived ASC, id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(readSite(rs));
            }
        }
        return out;
    }

    public static List<Site> searchSites(Connection conn, String query) throws SQLException {
        List<Site> out = new ArrayList<Site>();
        if (conn == null || query == null || query.trim().isEmpty()) {
            return out;
        }
        String like = "%" + escapeLike(query.trim().toLowerCase(Locale.ROOT)) + "%";
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, contact_id, project_no, site_name, street, zip, city, country, contract_type, "
                + "retention_percent, retention_basis, acceptance_date, warranty_years, warranty_until, "
                + "maintenance_due, archived, notes FROM trades_sites "
                + "WHERE LOWER(COALESCE(project_no, '')) LIKE ? ESCAPE '!' "
                + "OR LOWER(COALESCE(site_name, '')) LIKE ? ESCAPE '!' "
                + "OR LOWER(COALESCE(street, '')) LIKE ? ESCAPE '!' "
                + "OR LOWER(COALESCE(city, '')) LIKE ? ESCAPE '!' "
                + "ORDER BY id DESC")) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(readSite(rs));
                }
            }
        }
        return out;
    }

    public static void deleteSite(Connection conn, int siteId) throws SQLException {
        if (conn == null || siteId <= 0) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM trades_measurements WHERE site_id = ?")) {
            ps.setInt(1, siteId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM trades_time_sheets WHERE site_id = ?")) {
            ps.setInt(1, siteId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM trades_sites WHERE id = ?")) {
            ps.setInt(1, siteId);
            ps.executeUpdate();
        }
    }

    private static Site readSite(ResultSet rs) throws SQLException {
        Site s = new Site();
        s.id = rs.getInt(1);
        s.contactId = rs.getInt(2);
        s.projectNo = safe(rs.getString(3));
        s.siteName = safe(rs.getString(4));
        s.street = safe(rs.getString(5));
        s.zip = safe(rs.getString(6));
        s.city = safe(rs.getString(7));
        s.country = safeDefault(rs.getString(8), "DE");
        s.contractType = safeDefault(rs.getString(9), CONTRACT_BGB);
        s.retentionPercent = nonNull(rs.getBigDecimal(10), BigDecimal.ZERO);
        s.retentionBasis = safeDefault(rs.getString(11), BASIS_GROSS);
        s.acceptanceDate = safe(rs.getString(12));
        s.warrantyYears = rs.getInt(13);
        s.warrantyUntil = safe(rs.getString(14));
        s.maintenanceDue = safe(rs.getString(15));
        s.archived = rs.getInt(16) != 0;
        s.notes = safe(rs.getString(17));
        return s;
    }

    // ------------------------------------------------------------- item meta

    public static boolean hasItemMeta(Connection conn, int itemId) throws SQLException {
        if (conn == null || itemId <= 0) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM trades_item_meta WHERE item_id = ?")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static ItemMeta loadItemMeta(Connection conn, int itemId) throws SQLException {
        ItemMeta m = new ItemMeta();
        m.itemId = itemId;
        if (conn == null || itemId <= 0) {
            return m;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT item_id, site_id, site_snapshot, labor_tax_credit_enabled, cash_payment, "
                + "private_retention_notice, reverse_charge_enabled, retention_percent, retention_basis, "
                + "retention_amount, retention_release_date, retention_bond_ref, boq_id, notes "
                + "FROM trades_item_meta WHERE item_id = ?")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.siteId = rs.getInt(2);
                    m.siteSnapshot = safe(rs.getString(3));
                    m.laborTaxCreditEnabled = rs.getInt(4) != 0;
                    m.cashPayment = rs.getInt(5) != 0;
                    m.privateRetentionNotice = rs.getInt(6) != 0;
                    m.reverseChargeEnabled = rs.getInt(7) != 0;
                    m.retentionPercent = nonNull(rs.getBigDecimal(8), BigDecimal.ZERO);
                    m.retentionBasis = safeDefault(rs.getString(9), BASIS_GROSS);
                    m.retentionAmount = nonNull(rs.getBigDecimal(10), BigDecimal.ZERO);
                    m.retentionReleaseDate = safe(rs.getString(11));
                    m.retentionBondRef = safe(rs.getString(12));
                    m.boqId = rs.getInt(13);
                    m.notes = safe(rs.getString(14));
                }
            }
        }
        return m;
    }

    public static ItemMeta loadItemMetaSafe(int itemId) {
        if (itemId <= 0 || !hasDatabase()) {
            ItemMeta m = new ItemMeta();
            m.itemId = itemId;
            return m;
        }
        try {
            return loadItemMeta(connection(), itemId);
        } catch (Exception e) {
            Log.Debug(HandwerkStore.class, e);
            ItemMeta m = new ItemMeta();
            m.itemId = itemId;
            return m;
        }
    }

    public static synchronized void saveItemMeta(Connection conn, ItemMeta m) throws SQLException {
        if (conn == null || m == null || m.itemId <= 0) {
            return;
        }
        boolean exists = hasItemMeta(conn, m.itemId);
        if (exists) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE trades_item_meta SET site_id = ?, site_snapshot = ?, labor_tax_credit_enabled = ?, "
                    + "cash_payment = ?, private_retention_notice = ?, reverse_charge_enabled = ?, "
                    + "retention_percent = ?, retention_basis = ?, retention_amount = ?, "
                    + "retention_release_date = ?, retention_bond_ref = ?, boq_id = ?, notes = ? "
                    + "WHERE item_id = ?")) {
                bindItemMeta(ps, m, false);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO trades_item_meta (item_id, site_id, site_snapshot, labor_tax_credit_enabled, "
                    + "cash_payment, private_retention_notice, reverse_charge_enabled, retention_percent, "
                    + "retention_basis, retention_amount, retention_release_date, retention_bond_ref, boq_id, notes) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
                bindItemMeta(ps, m, true);
                ps.executeUpdate();
            }
        }
    }

    private static void bindItemMeta(PreparedStatement ps, ItemMeta m, boolean insert) throws SQLException {
        int i = 1;
        if (insert) {
            ps.setInt(i++, m.itemId);
        }
        ps.setInt(i++, m.siteId);
        ps.setString(i++, safe(m.siteSnapshot));
        ps.setInt(i++, m.laborTaxCreditEnabled ? 1 : 0);
        ps.setInt(i++, m.cashPayment ? 1 : 0);
        ps.setInt(i++, m.privateRetentionNotice ? 1 : 0);
        ps.setInt(i++, m.reverseChargeEnabled ? 1 : 0);
        ps.setBigDecimal(i++, nonNull(m.retentionPercent, BigDecimal.ZERO));
        ps.setString(i++, BASIS_NET.equalsIgnoreCase(m.retentionBasis) ? BASIS_NET : BASIS_GROSS);
        ps.setBigDecimal(i++, nonNull(m.retentionAmount, BigDecimal.ZERO));
        ps.setString(i++, safe(m.retentionReleaseDate));
        ps.setString(i++, safe(m.retentionBondRef));
        ps.setInt(i++, m.boqId);
        ps.setString(i++, safe(m.notes));
        if (!insert) {
            ps.setInt(i, m.itemId);
        }
    }

    public static List<ItemMeta> loadAllItemMetasWithRetention(Connection conn) throws SQLException {
        List<ItemMeta> out = new ArrayList<ItemMeta>();
        if (conn == null) {
            return out;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT item_id, site_id, site_snapshot, labor_tax_credit_enabled, cash_payment, "
                + "private_retention_notice, reverse_charge_enabled, retention_percent, retention_basis, "
                + "retention_amount, retention_release_date, retention_bond_ref, boq_id, notes "
                + "FROM trades_item_meta WHERE retention_amount > 0 OR retention_percent > 0 "
                + "ORDER BY retention_release_date ASC, item_id DESC");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ItemMeta m = new ItemMeta();
                m.itemId = rs.getInt(1);
                m.siteId = rs.getInt(2);
                m.siteSnapshot = safe(rs.getString(3));
                m.laborTaxCreditEnabled = rs.getInt(4) != 0;
                m.cashPayment = rs.getInt(5) != 0;
                m.privateRetentionNotice = rs.getInt(6) != 0;
                m.reverseChargeEnabled = rs.getInt(7) != 0;
                m.retentionPercent = nonNull(rs.getBigDecimal(8), BigDecimal.ZERO);
                m.retentionBasis = safeDefault(rs.getString(9), BASIS_GROSS);
                m.retentionAmount = nonNull(rs.getBigDecimal(10), BigDecimal.ZERO);
                m.retentionReleaseDate = safe(rs.getString(11));
                m.retentionBondRef = safe(rs.getString(12));
                m.boqId = rs.getInt(13);
                m.notes = safe(rs.getString(14));
                out.add(m);
            }
        }
        return out;
    }

    // -------------------------------------------------------- line cost type

    public static Map<Integer, LineCostType> loadLineCostTypes(Connection conn, int itemId) throws SQLException {
        Map<Integer, LineCostType> out = new LinkedHashMap<Integer, LineCostType>();
        if (conn == null || itemId <= 0) {
            return out;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT item_id, subitem_id, cost_type, labor_share_percent "
                + "FROM trades_line_cost_type WHERE item_id = ?")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LineCostType ct = new LineCostType();
                    ct.itemId = rs.getInt(1);
                    ct.subItemId = rs.getInt(2);
                    ct.costType = safeDefault(rs.getString(3), COST_LABOR);
                    ct.laborSharePercent = nonNull(rs.getBigDecimal(4), new BigDecimal("100.00"));
                    out.put(ct.subItemId, ct);
                }
            }
        }
        return out;
    }

    public static Map<Integer, LineCostType> loadLineCostTypesSafe(int itemId) {
        if (itemId <= 0 || !hasDatabase()) {
            return Collections.emptyMap();
        }
        try {
            return loadLineCostTypes(connection(), itemId);
        } catch (Exception e) {
            Log.Debug(HandwerkStore.class, e);
            return Collections.emptyMap();
        }
    }

    public static synchronized void saveLineCostType(Connection conn, LineCostType ct) throws SQLException {
        if (conn == null || ct == null || ct.itemId <= 0 || ct.subItemId <= 0) {
            return;
        }
        boolean exists;
        try (PreparedStatement chk = conn.prepareStatement(
                "SELECT 1 FROM trades_line_cost_type WHERE item_id = ? AND subitem_id = ?")) {
            chk.setInt(1, ct.itemId);
            chk.setInt(2, ct.subItemId);
            try (ResultSet rs = chk.executeQuery()) {
                exists = rs.next();
            }
        }
        if (exists) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE trades_line_cost_type SET cost_type = ?, labor_share_percent = ? "
                    + "WHERE item_id = ? AND subitem_id = ?")) {
                ps.setString(1, safeDefault(ct.costType, COST_LABOR));
                ps.setBigDecimal(2, nonNull(ct.laborSharePercent, new BigDecimal("100.00")));
                ps.setInt(3, ct.itemId);
                ps.setInt(4, ct.subItemId);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO trades_line_cost_type (item_id, subitem_id, cost_type, labor_share_percent) "
                    + "VALUES (?,?,?,?)")) {
                ps.setInt(1, ct.itemId);
                ps.setInt(2, ct.subItemId);
                ps.setString(3, safeDefault(ct.costType, COST_LABOR));
                ps.setBigDecimal(4, nonNull(ct.laborSharePercent, new BigDecimal("100.00")));
                ps.executeUpdate();
            }
        }
    }

    // -------------------------------------------------------- BoQ documents

    public static synchronized BoqDocument saveBoqDocument(Connection conn, BoqDocument doc) throws SQLException {
        if (doc.id <= 0) {
            doc.id = nextId(conn, "trades_boq_documents");
            if (safe(doc.importedAt).isEmpty()) {
                doc.importedAt = LocalDate.now().toString();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO trades_boq_documents (id, item_id, format_code, exchange_phase, "
                    + "project_name, boq_label, currency, raw_source, imported_at) VALUES (?,?,?,?,?,?,?,?,?)")) {
                ps.setInt(1, doc.id);
                ps.setInt(2, doc.itemId);
                ps.setString(3, safe(doc.formatCode));
                ps.setString(4, safe(doc.exchangePhase));
                ps.setString(5, safe(doc.projectName));
                ps.setString(6, safe(doc.boqLabel));
                ps.setString(7, safeDefault(doc.currency, "EUR"));
                ps.setString(8, safe(doc.rawSource));
                ps.setString(9, safe(doc.importedAt));
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE trades_boq_documents SET item_id = ?, format_code = ?, exchange_phase = ?, "
                    + "project_name = ?, boq_label = ?, currency = ?, raw_source = ?, imported_at = ? WHERE id = ?")) {
                ps.setInt(1, doc.itemId);
                ps.setString(2, safe(doc.formatCode));
                ps.setString(3, safe(doc.exchangePhase));
                ps.setString(4, safe(doc.projectName));
                ps.setString(5, safe(doc.boqLabel));
                ps.setString(6, safeDefault(doc.currency, "EUR"));
                ps.setString(7, safe(doc.rawSource));
                ps.setString(8, safe(doc.importedAt));
                ps.setInt(9, doc.id);
                ps.executeUpdate();
            }
        }

        // Replace positions for this BoQ
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM trades_boq_positions WHERE boq_id = ?")) {
            del.setInt(1, doc.id);
            del.executeUpdate();
        }
        int nextPosId = nextId(conn, "trades_boq_positions");
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO trades_boq_positions (id, boq_id, subitem_id, ordinal_no, source_item_id, "
                + "category_path, position_type, qty, unit, unit_price, short_text, long_text, bidder_text) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
            for (BoqPosition p : doc.positions) {
                p.id = nextPosId++;
                p.boqId = doc.id;
                ins.setInt(1, p.id);
                ins.setInt(2, p.boqId);
                ins.setInt(3, p.subItemId);
                ins.setString(4, safe(p.ordinalNo));
                ins.setString(5, safe(p.sourceItemId));
                ins.setString(6, safe(p.categoryPath));
                ins.setString(7, p.positionType == null ? PositionType.NORMAL.name() : p.positionType.name());
                ins.setBigDecimal(8, nonNull(p.quantity, BigDecimal.ZERO));
                ins.setString(9, safe(p.unit));
                ins.setBigDecimal(10, nonNull(p.unitPrice, BigDecimal.ZERO));
                ins.setString(11, truncate(safe(p.shortText), 1000));
                ins.setString(12, safe(p.longText));
                ins.setString(13, truncate(safe(p.bidderText), 2000));
                ins.executeUpdate();
            }
        }
        return doc;
    }

    public static BoqDocument loadBoqDocument(Connection conn, int boqId) throws SQLException {
        if (conn == null || boqId <= 0) {
            return null;
        }
        BoqDocument doc = null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, item_id, format_code, exchange_phase, project_name, boq_label, currency, "
                + "raw_source, imported_at FROM trades_boq_documents WHERE id = ?")) {
            ps.setInt(1, boqId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    doc = readBoqHeader(rs);
                }
            }
        }
        if (doc != null) {
            loadBoqPositions(conn, doc);
        }
        return doc;
    }

    public static BoqDocument loadBoqForItem(Connection conn, int itemId) throws SQLException {
        if (conn == null || itemId <= 0) {
            return null;
        }
        BoqDocument doc = null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, item_id, format_code, exchange_phase, project_name, boq_label, currency, "
                + "raw_source, imported_at FROM trades_boq_documents WHERE item_id = ? ORDER BY id DESC")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    doc = readBoqHeader(rs);
                }
            }
        }
        if (doc != null) {
            loadBoqPositions(conn, doc);
        }
        return doc;
    }

    public static BoqDocument loadBoqForItemSafe(int itemId) {
        if (itemId <= 0 || !hasDatabase()) {
            return null;
        }
        try {
            return loadBoqForItem(connection(), itemId);
        } catch (Exception e) {
            Log.Debug(HandwerkStore.class, e);
            return null;
        }
    }

    public static List<BoqPosition> searchBoqPositions(Connection conn, String query) throws SQLException {
        List<BoqPosition> out = new ArrayList<BoqPosition>();
        if (conn == null || query == null || query.trim().isEmpty()) {
            return out;
        }
        String like = "%" + escapeLike(query.trim().toLowerCase(Locale.ROOT)) + "%";
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, boq_id, subitem_id, ordinal_no, source_item_id, category_path, position_type, "
                + "qty, unit, unit_price, short_text, long_text, bidder_text FROM trades_boq_positions "
                + "WHERE LOWER(COALESCE(ordinal_no, '')) LIKE ? ESCAPE '!' "
                + "OR LOWER(COALESCE(short_text, '')) LIKE ? ESCAPE '!' "
                + "ORDER BY boq_id DESC, id ASC")) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                int limit = 20;
                while (rs.next() && out.size() < limit) {
                    out.add(readBoqPosition(rs));
                }
            }
        }
        return out;
    }

    private static BoqDocument readBoqHeader(ResultSet rs) throws SQLException {
        BoqDocument doc = new BoqDocument();
        doc.id = rs.getInt(1);
        doc.itemId = rs.getInt(2);
        doc.formatCode = safe(rs.getString(3));
        doc.exchangePhase = safe(rs.getString(4));
        doc.projectName = safe(rs.getString(5));
        doc.boqLabel = safe(rs.getString(6));
        doc.currency = safeDefault(rs.getString(7), "EUR");
        doc.rawSource = safe(rs.getString(8));
        doc.importedAt = safe(rs.getString(9));
        return doc;
    }

    private static void loadBoqPositions(Connection conn, BoqDocument doc) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, boq_id, subitem_id, ordinal_no, source_item_id, category_path, position_type, "
                + "qty, unit, unit_price, short_text, long_text, bidder_text "
                + "FROM trades_boq_positions WHERE boq_id = ? ORDER BY id ASC")) {
            ps.setInt(1, doc.id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    doc.positions.add(readBoqPosition(rs));
                }
            }
        }
    }

    private static BoqPosition readBoqPosition(ResultSet rs) throws SQLException {
        BoqPosition p = new BoqPosition();
        p.id = rs.getInt(1);
        p.boqId = rs.getInt(2);
        p.subItemId = rs.getInt(3);
        p.ordinalNo = safe(rs.getString(4));
        p.sourceItemId = safe(rs.getString(5));
        p.categoryPath = safe(rs.getString(6));
        try {
            p.positionType = PositionType.valueOf(safeDefault(rs.getString(7), PositionType.NORMAL.name()));
        } catch (IllegalArgumentException e) {
            p.positionType = PositionType.NORMAL;
        }
        p.quantity = nonNull(rs.getBigDecimal(8), BigDecimal.ZERO);
        p.unit = safe(rs.getString(9));
        p.unitPrice = nonNull(rs.getBigDecimal(10), BigDecimal.ZERO);
        p.shortText = safe(rs.getString(11));
        p.longText = safe(rs.getString(12));
        p.bidderText = safe(rs.getString(13));
        return p;
    }

    // ---------------------------------------------------------- measurements

    public static synchronized Measurement saveMeasurement(Connection conn, Measurement m) throws SQLException {
        if (m.id <= 0) {
            m.id = nextId(conn, "trades_measurements");
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO trades_measurements (id, site_id, item_id, subitem_id, ordinal_no, "
                    + "work_date, description, formula, computed_qty, unit, notes) VALUES (?,?,?,?,?,?,?,?,?,?,?)")) {
                ps.setInt(1, m.id);
                ps.setInt(2, m.siteId);
                ps.setInt(3, m.itemId);
                ps.setInt(4, m.subItemId);
                ps.setString(5, safe(m.ordinalNo));
                ps.setString(6, safe(m.workDate));
                ps.setString(7, safe(m.description));
                ps.setString(8, safe(m.formula));
                ps.setBigDecimal(9, nonNull(m.computedQty, BigDecimal.ZERO));
                ps.setString(10, safe(m.unit));
                ps.setString(11, safe(m.notes));
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE trades_measurements SET site_id = ?, item_id = ?, subitem_id = ?, ordinal_no = ?, "
                    + "work_date = ?, description = ?, formula = ?, computed_qty = ?, unit = ?, notes = ? "
                    + "WHERE id = ?")) {
                ps.setInt(1, m.siteId);
                ps.setInt(2, m.itemId);
                ps.setInt(3, m.subItemId);
                ps.setString(4, safe(m.ordinalNo));
                ps.setString(5, safe(m.workDate));
                ps.setString(6, safe(m.description));
                ps.setString(7, safe(m.formula));
                ps.setBigDecimal(8, nonNull(m.computedQty, BigDecimal.ZERO));
                ps.setString(9, safe(m.unit));
                ps.setString(10, safe(m.notes));
                ps.setInt(11, m.id);
                ps.executeUpdate();
            }
        }
        return m;
    }

    public static List<Measurement> loadMeasurementsForItem(Connection conn, int itemId) throws SQLException {
        List<Measurement> out = new ArrayList<Measurement>();
        if (conn == null || itemId <= 0) {
            return out;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, site_id, item_id, subitem_id, ordinal_no, work_date, description, formula, "
                + "computed_qty, unit, notes FROM trades_measurements WHERE item_id = ? ORDER BY id ASC")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(readMeasurement(rs));
                }
            }
        }
        return out;
    }

    public static List<Measurement> loadMeasurementsForItemSafe(int itemId) {
        if (itemId <= 0 || !hasDatabase()) {
            return Collections.emptyList();
        }
        try {
            return loadMeasurementsForItem(connection(), itemId);
        } catch (Exception e) {
            Log.Debug(HandwerkStore.class, e);
            return Collections.emptyList();
        }
    }

    public static void deleteMeasurement(Connection conn, int id) throws SQLException {
        if (conn == null || id <= 0) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM trades_measurements WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static Measurement readMeasurement(ResultSet rs) throws SQLException {
        Measurement m = new Measurement();
        m.id = rs.getInt(1);
        m.siteId = rs.getInt(2);
        m.itemId = rs.getInt(3);
        m.subItemId = rs.getInt(4);
        m.ordinalNo = safe(rs.getString(5));
        m.workDate = safe(rs.getString(6));
        m.description = safe(rs.getString(7));
        m.formula = safe(rs.getString(8));
        m.computedQty = nonNull(rs.getBigDecimal(9), BigDecimal.ZERO);
        m.unit = safe(rs.getString(10));
        m.notes = safe(rs.getString(11));
        return m;
    }

    // ----------------------------------------------------------- time sheets

    public static synchronized TimeSheet saveTimeSheet(Connection conn, TimeSheet t) throws SQLException {
        if (t.id <= 0) {
            t.id = nextId(conn, "trades_time_sheets");
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO trades_time_sheets (id, site_id, item_id, work_date, worker_name, "
                    + "worker_role, hours, travel_km, description, signed_by, billed) VALUES (?,?,?,?,?,?,?,?,?,?,?)")) {
                ps.setInt(1, t.id);
                ps.setInt(2, t.siteId);
                ps.setInt(3, t.itemId);
                ps.setString(4, safe(t.workDate));
                ps.setString(5, safe(t.workerName));
                ps.setString(6, safeDefault(t.workerRole, ROLE_JOURNEYMAN));
                ps.setBigDecimal(7, nonNull(t.hours, BigDecimal.ZERO));
                ps.setBigDecimal(8, nonNull(t.travelKm, BigDecimal.ZERO));
                ps.setString(9, safe(t.description));
                ps.setString(10, safe(t.signedBy));
                ps.setInt(11, t.billed ? 1 : 0);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE trades_time_sheets SET site_id = ?, item_id = ?, work_date = ?, worker_name = ?, "
                    + "worker_role = ?, hours = ?, travel_km = ?, description = ?, signed_by = ?, billed = ? "
                    + "WHERE id = ?")) {
                ps.setInt(1, t.siteId);
                ps.setInt(2, t.itemId);
                ps.setString(3, safe(t.workDate));
                ps.setString(4, safe(t.workerName));
                ps.setString(5, safeDefault(t.workerRole, ROLE_JOURNEYMAN));
                ps.setBigDecimal(6, nonNull(t.hours, BigDecimal.ZERO));
                ps.setBigDecimal(7, nonNull(t.travelKm, BigDecimal.ZERO));
                ps.setString(8, safe(t.description));
                ps.setString(9, safe(t.signedBy));
                ps.setInt(10, t.billed ? 1 : 0);
                ps.setInt(11, t.id);
                ps.executeUpdate();
            }
        }
        return t;
    }

    public static List<TimeSheet> loadTimeSheetsForItem(Connection conn, int itemId) throws SQLException {
        List<TimeSheet> out = new ArrayList<TimeSheet>();
        if (conn == null || itemId <= 0) {
            return out;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, site_id, item_id, work_date, worker_name, worker_role, hours, travel_km, "
                + "description, signed_by, billed FROM trades_time_sheets WHERE item_id = ? ORDER BY work_date ASC, id ASC")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(readTimeSheet(rs));
                }
            }
        }
        return out;
    }

    public static List<TimeSheet> loadTimeSheetsForSite(Connection conn, int siteId) throws SQLException {
        List<TimeSheet> out = new ArrayList<TimeSheet>();
        if (conn == null || siteId <= 0) {
            return out;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, site_id, item_id, work_date, worker_name, worker_role, hours, travel_km, "
                + "description, signed_by, billed FROM trades_time_sheets WHERE site_id = ? ORDER BY work_date ASC, id ASC")) {
            ps.setInt(1, siteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(readTimeSheet(rs));
                }
            }
        }
        return out;
    }

    public static List<TimeSheet> loadTimeSheetsForItemSafe(int itemId) {
        if (itemId <= 0 || !hasDatabase()) {
            return Collections.emptyList();
        }
        try {
            return loadTimeSheetsForItem(connection(), itemId);
        } catch (Exception e) {
            Log.Debug(HandwerkStore.class, e);
            return Collections.emptyList();
        }
    }

    public static void deleteTimeSheet(Connection conn, int id) throws SQLException {
        if (conn == null || id <= 0) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM trades_time_sheets WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static TimeSheet readTimeSheet(ResultSet rs) throws SQLException {
        TimeSheet t = new TimeSheet();
        t.id = rs.getInt(1);
        t.siteId = rs.getInt(2);
        t.itemId = rs.getInt(3);
        t.workDate = safe(rs.getString(4));
        t.workerName = safe(rs.getString(5));
        t.workerRole = safeDefault(rs.getString(6), ROLE_JOURNEYMAN);
        t.hours = nonNull(rs.getBigDecimal(7), BigDecimal.ZERO);
        t.travelKm = nonNull(rs.getBigDecimal(8), BigDecimal.ZERO);
        t.description = safe(rs.getString(9));
        t.signedBy = safe(rs.getString(10));
        t.billed = rs.getInt(11) != 0;
        return t;
    }

    // ---------------------------------------------------------- GDPR delete

    public static void deleteContactData(Connection conn, int contactId) throws SQLException {
        if (conn == null || contactId <= 0) {
            return;
        }
        List<Site> sites = loadSitesForContact(conn, contactId, true);
        for (Site s : sites) {
            deleteSite(conn, s.id);
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM trades_contact_meta WHERE contact_id = ?")) {
            ps.setInt(1, contactId);
            ps.executeUpdate();
        }
    }

    // --------------------------------------------------------------- helpers

    static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    static String safeDefault(String s, String def) {
        String t = safe(s);
        return t.isEmpty() ? def : t;
    }

    static BigDecimal nonNull(BigDecimal b, BigDecimal fallback) {
        return b == null ? fallback : b;
    }

    static String truncate(String s, int maxLen) {
        if (s == null || s.length() <= maxLen) {
            return s == null ? "" : s;
        }
        return s.substring(0, maxLen);
    }

    static String escapeLike(String s) {
        return s.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    public static int parseInt(String raw, int fallback) {
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static BigDecimal parseDecimal(String raw, BigDecimal fallback) {
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        try {
            String normalized = raw.trim().replace(" ", "");
            if (normalized.contains(",") && normalized.contains(".")) {
                normalized = normalized.replace(".", "").replace(',', '.');
            } else {
                normalized = normalized.replace(',', '.');
            }
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
