/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.db.common;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import mpv5.db.objects.User;
import mpv5.handler.Activity;
import mpv5.logging.Log;
import mpv5.plugin.ExtensionManager;

/**
 * Pure, UI-independent engine behind the OpenYaBS SQL Console:
 * <ul>
 * <li>Quote- and comment-aware multi-statement SQL splitting</li>
 * <li>Statement classification ({@code SELECT}, {@code UPDATE}, {@code DELETE},
 * {@code INSERT}, {@code ALTER TABLE}, {@code DROP}/{@code TRUNCATE}, {@code CREATE})</li>
 * <li>Target table extraction, missing-{@code WHERE} detection, and GoBD/tax-table
 * risk assessment</li>
 * <li>Safe execution with row limits and timing</li>
 * <li>Mandatory audit logging of table-modifying statements via {@link Log} and
 * {@link Activity#sql(String, String, String, String)} (automatically recorded
 * in the GoBD SHA-256 hash-chained audit log when the GoBD plugin is active)</li>
 * <li>JDBC schema introspection (tables &amp; columns) and CSV export</li>
 * </ul>
 */
public final class SqlConsole {

    public static final String GOBD_PLUGIN_ID = "groovy.gobd-compliance";

    private static final Set<String> TAX_TABLES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "items", "subitems", "revenues", "expenses", "payments",
            "contacts", "accounts", "tax", "products",
            "files", "filestocontacts", "filestoitems"
    )));

    private static final Set<String> SYSTEM_SCHEMAS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "information_schema", "pg_catalog", "pg_toast", "sys", "mysql", "performance_schema",
            "sysibm", "syscat", "sysfun", "sysproc", "sysstat", "syscs_diag", "syscs_util", "nullid"
    )));

    private SqlConsole() {
    }

    public enum StatementKind {
        SELECT,
        INSERT,
        UPDATE,
        DELETE,
        ALTER_TABLE,
        DDL_DESTRUCTIVE,
        DDL_CREATE,
        OTHER_WRITE
    }

    public enum RiskLevel {
        READ_ONLY,
        CONFIRM,
        WARNING_DDL,
        CRITICAL_GOBD
    }

    /**
     * Parsed and classified SQL statement.
     */
    public static final class StatementInfo {

        public final String sql;
        public final StatementKind kind;
        public final List<String> tables;
        public final boolean modifying;
        public final boolean alterOrDdl;
        public final boolean missingWhere;
        public final boolean touchesGobdTable;
        public final boolean touchesTaxTable;

        StatementInfo(String sql, StatementKind kind, List<String> tables,
                boolean missingWhere, boolean touchesGobdTable, boolean touchesTaxTable) {
            this.sql = sql;
            this.kind = kind;
            this.tables = Collections.unmodifiableList(tables);
            this.modifying = kind != StatementKind.SELECT;
            this.alterOrDdl = kind == StatementKind.ALTER_TABLE
                    || kind == StatementKind.DDL_DESTRUCTIVE
                    || kind == StatementKind.DDL_CREATE;
            this.missingWhere = missingWhere;
            this.touchesGobdTable = touchesGobdTable;
            this.touchesTaxTable = touchesTaxTable;
        }

        public RiskLevel riskLevel(boolean gobdActive) {
            if (!modifying) {
                return RiskLevel.READ_ONLY;
            }
            if (touchesGobdTable || (gobdActive && alterOrDdl && touchesTaxTable)) {
                return RiskLevel.CRITICAL_GOBD;
            }
            if (alterOrDdl) {
                return RiskLevel.WARNING_DDL;
            }
            return RiskLevel.CONFIRM;
        }

        public String targetTablesSummary() {
            return tables.isEmpty() ? "" : String.join(", ", tables);
        }

        public String label() {
            switch (kind) {
                case SELECT:
                    return "SELECT";
                case INSERT:
                    return "INSERT";
                case UPDATE:
                    return "UPDATE";
                case DELETE:
                    return "DELETE";
                case ALTER_TABLE:
                    return "ALTER TABLE";
                case DDL_DESTRUCTIVE:
                    return "DROP / TRUNCATE";
                case DDL_CREATE:
                    return "CREATE";
                default:
                    return "WRITE";
            }
        }
    }

    /**
     * Execution result of a single statement.
     */
    public static final class ExecutionResult {

        public final StatementInfo info;
        public final boolean success;
        public final boolean hasResultSet;
        public final String[] columns;
        public final Object[][] rows;
        public final boolean truncated;
        public final int updateCount;
        public final String errorMessage;
        public final long durationMs;
        public final boolean timedOut;
        public final boolean cancelled;

        ExecutionResult(StatementInfo info, boolean success, boolean hasResultSet,
                String[] columns, Object[][] rows, boolean truncated,
                int updateCount, String errorMessage, long durationMs,
                boolean timedOut, boolean cancelled) {
            this.info = info;
            this.success = success;
            this.hasResultSet = hasResultSet;
            this.columns = columns == null ? new String[0] : columns;
            this.rows = rows == null ? new Object[0][0] : rows;
            this.truncated = truncated;
            this.updateCount = updateCount;
            this.errorMessage = errorMessage == null ? "" : errorMessage;
            this.durationMs = durationMs;
            this.timedOut = timedOut;
            this.cancelled = cancelled;
        }

        public String outcomeSummary() {
            if (!success) {
                if (cancelled) {
                    return "CANCELLED (" + durationMs + " ms)";
                }
                if (timedOut) {
                    return "TIMEOUT: " + errorMessage + " (" + durationMs + " ms)";
                }
                return "ERROR: " + errorMessage + " (" + durationMs + " ms)";
            }
            if (hasResultSet) {
                return rows.length + (truncated ? "+" : "") + " rows (" + durationMs + " ms)";
            }
            if (updateCount >= 0) {
                return "OK (" + updateCount + " rows affected, " + durationMs + " ms)";
            }
            return "OK (" + durationMs + " ms)";
        }
    }

    public static final class ColumnInfo {

        public final String name;
        public final String typeName;
        public final int size;
        public final boolean nullable;

        public ColumnInfo(String name, String typeName, int size, boolean nullable) {
            this.name = name;
            this.typeName = typeName;
            this.size = size;
            this.nullable = nullable;
        }

        @Override
        public String toString() {
            String sz = size > 0 && isSizedType(typeName) ? "(" + size + ")" : "";
            return name + "  —  " + typeName + sz + (nullable ? "" : " NOT NULL");
        }

        private static boolean isSizedType(String t) {
            if (t == null) {
                return false;
            }
            String u = t.toUpperCase(Locale.ROOT);
            return u.contains("CHAR") || u.contains("DEC") || u.contains("NUM") || u.contains("BIN");
        }
    }

    /**
     * @return true if the GoBD compliance plugin is currently running
     */
    public static boolean isGobdActive() {
        try {
            ExtensionManager.Installed inst = ExtensionManager.instance().find(GOBD_PLUGIN_ID);
            return inst != null && inst.getState() == ExtensionManager.State.RUNNING;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Splits a SQL script into individual non-empty statements separated by {@code ;},
     * respecting string literals, quoted identifiers, and line/block comments.
     */
    public static List<String> splitStatements(String script) {
        List<String> out = new ArrayList<String>();
        if (script == null || script.trim().isEmpty()) {
            return out;
        }
        StringBuilder current = new StringBuilder();
        int len = script.length();
        boolean inSingle = false;
        boolean inDouble = false;
        boolean inBacktick = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        for (int i = 0; i < len; i++) {
            char c = script.charAt(i);
            char next = i + 1 < len ? script.charAt(i + 1) : '\0';

            if (inLineComment) {
                current.append(c);
                if (c == '\n') {
                    inLineComment = false;
                }
                continue;
            }
            if (inBlockComment) {
                current.append(c);
                if (c == '*' && next == '/') {
                    current.append(next);
                    i++;
                    inBlockComment = false;
                }
                continue;
            }
            if (inSingle) {
                current.append(c);
                if (c == '\'' && next == '\'') {
                    current.append(next);
                    i++;
                } else if (c == '\\' && next != '\0') {
                    current.append(next);
                    i++;
                } else if (c == '\'') {
                    inSingle = false;
                }
                continue;
            }
            if (inDouble) {
                current.append(c);
                if (c == '"' && next == '"') {
                    current.append(next);
                    i++;
                } else if (c == '"') {
                    inDouble = false;
                }
                continue;
            }
            if (inBacktick) {
                current.append(c);
                if (c == '`' && next == '`') {
                    current.append(next);
                    i++;
                } else if (c == '`') {
                    inBacktick = false;
                }
                continue;
            }

            if (c == '-' && next == '-') {
                inLineComment = true;
                current.append(c).append(next);
                i++;
                continue;
            }
            if (c == '#') {
                inLineComment = true;
                current.append(c);
                continue;
            }
            if (c == '/' && next == '*') {
                inBlockComment = true;
                current.append(c).append(next);
                i++;
                continue;
            }
            if (c == '\'') {
                inSingle = true;
                current.append(c);
                continue;
            }
            if (c == '"') {
                inDouble = true;
                current.append(c);
                continue;
            }
            if (c == '`') {
                inBacktick = true;
                current.append(c);
                continue;
            }
            if (c == ';') {
                addIfExecutable(out, current.toString());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        addIfExecutable(out, current.toString());
        return out;
    }

    private static void addIfExecutable(List<String> out, String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        if (!tokenize(trimmed).isEmpty()) {
            out.add(trimmed);
        }
    }

    /**
     * Analyzes a single SQL statement: kind, target tables, missing WHERE, and
     * GoBD/tax-table sensitivity.
     */
    public static StatementInfo analyze(String rawSql) {
        String sql = rawSql == null ? "" : rawSql.trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }
        List<Token> tokens = tokenize(sql);
        if (tokens.isEmpty()) {
            return new StatementInfo(sql, StatementKind.SELECT, Collections.emptyList(), false, false, false);
        }

        int verbIdx = 0;
        String first = tokens.get(0).upper;
        if ("WITH".equals(first)) {
            verbIdx = findMainVerbAfterWith(tokens);
            first = verbIdx >= 0 && verbIdx < tokens.size() ? tokens.get(verbIdx).upper : "OTHER";
        }

        StatementKind kind = classifyVerb(first);
        // Guard against data-modifying CTEs (e.g. WITH d AS (DELETE FROM ... RETURNING *) SELECT * FROM d)
        if (kind == StatementKind.SELECT && "WITH".equals(tokens.get(0).upper)) {
            int mutIdx = findModifyingVerbInsideCte(tokens, verbIdx);
            if (mutIdx >= 0) {
                verbIdx = mutIdx;
                kind = classifyVerb(tokens.get(mutIdx).upper);
            }
        }
        // Guard against SELECT ... INTO <table/outfile>
        if (kind == StatementKind.SELECT && hasTopLevelKeyword(tokens, verbIdx + 1, "INTO")) {
            kind = StatementKind.OTHER_WRITE;
        }
        // Guard against EXPLAIN ANALYZE <DML/DDL> which executes the underlying statement
        if (kind == StatementKind.SELECT && "EXPLAIN".equals(first)) {
            for (int k = verbIdx + 1; k < tokens.size(); k++) {
                Token t = tokens.get(k);
                if (!t.quotedString && ("ANALYZE".equals(t.upper) || "ANALYSE".equals(t.upper))) {
                    kind = StatementKind.OTHER_WRITE;
                    break;
                }
            }
        }
        List<String> tables = extractTables(tokens, verbIdx, kind);

        boolean missingWhere = false;
        if (kind == StatementKind.UPDATE || kind == StatementKind.DELETE) {
            missingWhere = !hasTopLevelKeyword(tokens, verbIdx + 1, "WHERE");
        }

        boolean touchesGobd = false;
        boolean touchesTax = false;
        for (String t : tables) {
            String base = baseTableName(t);
            if (base.startsWith("gobd_")) {
                touchesGobd = true;
                touchesTax = true;
            } else if (TAX_TABLES.contains(base)) {
                touchesTax = true;
            }
        }
        // Extra safety check in case a statement references a gobd_* table in an unusual syntax
        if (!touchesGobd && kind != StatementKind.SELECT) {
            for (Token tok : tokens) {
                if (!tok.quotedString && baseTableName(tok.raw).startsWith("gobd_")) {
                    touchesGobd = true;
                    touchesTax = true;
                    break;
                }
            }
        }

        return new StatementInfo(sql, kind, tables, missingWhere, touchesGobd, touchesTax);
    }

    /**
     * Convenience helper that splits and analyzes a full SQL script.
     */
    public static List<StatementInfo> analyzeScript(String script) {
        List<StatementInfo> res = new ArrayList<StatementInfo>();
        for (String stmt : splitStatements(script)) {
            res.add(analyze(stmt));
        }
        return res;
    }

    /**
     * Executes a single analyzed statement on {@code conn}, applying {@code maxRows}
     * for result sets and auditing all modifying statements when {@code logAudit} is true.
     */
    public static ExecutionResult execute(Connection conn, StatementInfo info, int maxRows, boolean logAudit) {
        return execute(conn, info, maxRows, 0, logAudit, true, null, null);
    }

    /**
     * Executes a single analyzed statement on {@code conn} with optional query timeout
     * (in seconds, {@code <= 0} means no timeout) and cooperative cancellation support.
     */
    public static ExecutionResult execute(Connection conn, StatementInfo info, int maxRows,
            int timeoutSeconds, boolean logAudit, AtomicReference<Statement> activeStatement,
            java.util.concurrent.atomic.AtomicBoolean cancelRequested) {
        return execute(conn, info, maxRows, timeoutSeconds, logAudit, true, activeStatement, cancelRequested);
    }

    /**
     * Executes a single analyzed statement on {@code conn} with optional query timeout,
     * cooperative cancellation, and write-permission enforcement. When {@code allowModify}
     * is {@code false}, any statement other than a read-only {@code SELECT} is rejected
     * before a JDBC {@link Statement} is even created.
     */
    public static ExecutionResult execute(Connection conn, StatementInfo info, int maxRows,
            int timeoutSeconds, boolean logAudit, boolean allowModify,
            AtomicReference<Statement> activeStatement,
            java.util.concurrent.atomic.AtomicBoolean cancelRequested) {
        if (conn == null) {
            return new ExecutionResult(info, false, false, null, null, false, -1,
                    "No database connection", 0, false, false);
        }
        if ((cancelRequested != null && cancelRequested.get()) || Thread.currentThread().isInterrupted()) {
            return new ExecutionResult(info, false, false, null, null, false, -1,
                    "Cancelled by user", 0, false, true);
        }
        if (!allowModify && info.modifying) {
            String err = "Administrator rights required for non-SELECT statements";
            if (logAudit) {
                recordAudit("BLOCKED_" + info.kind.name(), info.targetTablesSummary(), info.sql, "DENIED: " + err);
            }
            return new ExecutionResult(info, false, false, null, null, false, -1, err, 0, false, false);
        }
        if (logAudit && info.modifying && info.touchesGobdTable) {
            // Log attempt before running statements targeting gobd_* tables so even
            // destructive statements against audit/lock tables leave a record first.
            recordAudit("ATTEMPT_" + info.kind.name(), info.targetTablesSummary(), info.sql, "PRE_EXECUTE");
        }

        long startNs = System.nanoTime();
        Statement stm = null;
        ResultSet rs = null;
        try {
            stm = conn.createStatement();
            if (activeStatement != null) {
                activeStatement.set(stm);
            }
            if (timeoutSeconds > 0) {
                try {
                    stm.setQueryTimeout(timeoutSeconds);
                } catch (SQLException | AbstractMethodError | UnsupportedOperationException ignored) {
                    // Driver does not support query timeouts
                }
            }
            if (!info.modifying && maxRows > 0) {
                stm.setMaxRows(maxRows + 1);
            }
            boolean hasRs = stm.execute(info.sql);
            long elapsedMs = Math.max(0L, (System.nanoTime() - startNs) / 1_000_000L);
            ExecutionResult result;
            if (hasRs) {
                rs = stm.getResultSet();
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                String[] colNames = new String[cols];
                for (int i = 1; i <= cols; i++) {
                    String label = md.getColumnLabel(i);
                    colNames[i - 1] = (label != null && !label.isEmpty()) ? label : md.getColumnName(i);
                }
                List<Object[]> rowList = new ArrayList<Object[]>();
                boolean truncated = false;
                while (rs.next()) {
                    if ((cancelRequested != null && cancelRequested.get()) || Thread.currentThread().isInterrupted()) {
                        long cancelMs = Math.max(0L, (System.nanoTime() - startNs) / 1_000_000L);
                        return new ExecutionResult(info, false, false, null, null, false, -1,
                                "Cancelled by user", cancelMs, false, true);
                    }
                    if (maxRows > 0 && rowList.size() >= maxRows) {
                        truncated = true;
                        break;
                    }
                    Object[] row = new Object[cols];
                    for (int i = 1; i <= cols; i++) {
                        row[i - 1] = formatValue(rs.getObject(i));
                    }
                    rowList.add(row);
                }
                result = new ExecutionResult(info, true, true, colNames,
                        rowList.toArray(new Object[rowList.size()][]), truncated, -1, null, elapsedMs, false, false);
            } else {
                int count = stm.getUpdateCount();
                result = new ExecutionResult(info, true, false, null, null, false, count, null, elapsedMs, false, false);
            }

            if (logAudit && info.modifying) {
                recordAudit(info.kind.name(), info.targetTablesSummary(), info.sql, result.outcomeSummary());
            } else {
                Log.Debug(SqlConsole.class, "SQL Console SELECT (" + result.outcomeSummary() + "): " + info.sql);
            }
            return result;
        } catch (SQLException ex) {
            long elapsedMs = Math.max(0L, (System.nanoTime() - startNs) / 1_000_000L);
            boolean wasCancelled = (cancelRequested != null && cancelRequested.get()) || Thread.currentThread().isInterrupted();
            boolean wasTimeout = !wasCancelled && isTimeoutException(ex, timeoutSeconds);
            String msg;
            if (wasCancelled) {
                msg = "Cancelled by user";
            } else if (wasTimeout && timeoutSeconds > 0) {
                msg = "Query timed out after " + timeoutSeconds + " s";
            } else {
                msg = ex.getMessage() == null ? ex.toString() : ex.getMessage();
            }
            ExecutionResult failed = new ExecutionResult(info, false, false, null, null, false, -1,
                    msg, elapsedMs, wasTimeout, wasCancelled);
            if (logAudit && info.modifying) {
                String suffix = wasCancelled ? "_CANCELLED" : (wasTimeout ? "_TIMEOUT" : "_FAILED");
                recordAudit(info.kind.name() + suffix, info.targetTablesSummary(), info.sql, failed.outcomeSummary());
            } else {
                Log.Debug(SqlConsole.class, "SQL Console error: " + msg + " | SQL: " + info.sql);
            }
            return failed;
        } finally {
            if (activeStatement != null && stm != null) {
                activeStatement.compareAndSet(stm, null);
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Requests cancellation of a currently executing JDBC statement without throwing.
     */
    public static void cancelQuietly(AtomicReference<Statement> activeStatement) {
        if (activeStatement == null) {
            return;
        }
        Statement stm = activeStatement.get();
        if (stm != null) {
            try {
                stm.cancel();
            } catch (SQLException | AbstractMethodError | UnsupportedOperationException ignored) {
            }
        }
    }

    private static boolean isTimeoutException(SQLException ex, int timeoutSeconds) {
        if (ex instanceof SQLTimeoutException) {
            return true;
        }
        String state = ex.getSQLState();
        if ("HYT00".equalsIgnoreCase(state) || ("57014".equals(state) && timeoutSeconds > 0)) {
            return true;
        }
        String msg = ex.getMessage();
        return timeoutSeconds > 0 && msg != null && msg.toLowerCase(Locale.ROOT).contains("timeout");
    }

    /**
     * Records an audit entry for a Groovy console script (which has direct DB access).
     */
    public static void logGroovyExecution(String script, boolean success, String outcome) {
        String action = success ? "GROOVY" : "GROOVY_FAILED";
        recordAudit(action, "groovy", script, outcome);
    }

    private static void recordAudit(String action, String tables, String sql, String outcome) {
        String user = currentUserName();
        Log.Print("SQL_CONSOLE [" + user + "] " + action
                + (tables == null || tables.isEmpty() ? "" : " [" + tables + "]")
                + " -> " + outcome + " | " + singleLine(sql));
        Activity.sql(action, tables, sql, outcome);
    }

    private static String currentUserName() {
        try {
            User u = User.getCurrentUser();
            if (u != null && u.__getCname() != null && !u.__getCname().trim().isEmpty()) {
                return u.__getCname().trim();
            }
        } catch (Throwable ignored) {
        }
        return System.getProperty("user.name", "system");
    }

    private static String singleLine(String s) {
        return s == null ? "" : s.replace('\r', ' ').replace('\n', ' ').trim();
    }

    private static Object formatValue(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            return QueryHandler.rescapeBackslashes((String) val);
        }
        if (val instanceof Clob) {
            try {
                Clob clob = (Clob) val;
                long len = Math.min(clob.length(), 4096L);
                return clob.getSubString(1, (int) len) + (clob.length() > 4096L ? "…" : "");
            } catch (SQLException e) {
                return "<CLOB>";
            }
        }
        if (val instanceof Blob) {
            try {
                return "<BLOB " + ((Blob) val).length() + " B>";
            } catch (SQLException e) {
                return "<BLOB>";
            }
        }
        if (val instanceof byte[]) {
            return "<BINARY " + ((byte[]) val).length + " B>";
        }
        return val;
    }

    /**
     * Lists user tables and views visible on the given JDBC connection.
     */
    public static List<String> listTables(Connection conn) {
        if (conn == null) {
            return Collections.emptyList();
        }
        Set<String> tables = new LinkedHashSet<String>();
        ResultSet rs = null;
        try {
            DatabaseMetaData md = conn.getMetaData();
            String catalog = null;
            String schema = null;
            try {
                catalog = conn.getCatalog();
            } catch (Throwable ignored) {
            }
            try {
                schema = conn.getSchema();
            } catch (Throwable ignored) {
            }
            rs = md.getTables(catalog, schema, "%", new String[]{"TABLE", "VIEW"});
            while (rs.next()) {
                String sch = rs.getString("TABLE_SCHEM");
                String name = rs.getString("TABLE_NAME");
                if (name == null || name.trim().isEmpty()) {
                    continue;
                }
                if (sch != null && SYSTEM_SCHEMAS.contains(sch.toLowerCase(Locale.ROOT))) {
                    continue;
                }
                tables.add(name.toLowerCase(Locale.ROOT));
            }
        } catch (SQLException e) {
            Log.Debug(SqlConsole.class, "listTables failed: " + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            }
        }
        List<String> sorted = new ArrayList<String>(tables);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Lists columns of a table on the given JDBC connection.
     */
    public static List<ColumnInfo> listColumns(Connection conn, String tableName) {
        if (conn == null || tableName == null || tableName.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<ColumnInfo> cols = new ArrayList<ColumnInfo>();
        try {
            DatabaseMetaData md = conn.getMetaData();
            String catalog = null;
            String schema = null;
            try {
                catalog = conn.getCatalog();
            } catch (Throwable ignored) {
            }
            try {
                schema = conn.getSchema();
            } catch (Throwable ignored) {
            }
            // Try as-is, uppercase (H2/Derby), and lowercase (MySQL/Postgres)
            String[] variants = new String[]{
                tableName.trim(),
                tableName.trim().toUpperCase(Locale.ROOT),
                tableName.trim().toLowerCase(Locale.ROOT)
            };
            Set<String> seen = new HashSet<String>();
            for (String v : variants) {
                if (!seen.add(v)) {
                    continue;
                }
                try (ResultSet rs = md.getColumns(catalog, schema, v, "%")) {
                    while (rs.next()) {
                        String col = rs.getString("COLUMN_NAME");
                        String type = rs.getString("TYPE_NAME");
                        int size = rs.getInt("COLUMN_SIZE");
                        boolean nullable = rs.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls;
                        if (col != null) {
                            cols.add(new ColumnInfo(col.toLowerCase(Locale.ROOT), type == null ? "" : type, size, nullable));
                        }
                    }
                }
                if (!cols.isEmpty()) {
                    break;
                }
            }
        } catch (SQLException e) {
            Log.Debug(SqlConsole.class, "listColumns failed for " + tableName + ": " + e.getMessage());
        }
        return cols;
    }

    /**
     * Renders a query result as UTF-8 CSV (semicolon-separated, Excel-friendly).
     */
    public static String toCsv(String[] columns, Object[][] rows) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        if (columns != null) {
            for (int i = 0; i < columns.length; i++) {
                if (i > 0) {
                    sb.append(';');
                }
                sb.append(csvCell(columns[i]));
            }
            sb.append("\r\n");
        }
        if (rows != null) {
            for (Object[] row : rows) {
                if (row == null) {
                    continue;
                }
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) {
                        sb.append(';');
                    }
                    sb.append(csvCell(row[i] == null ? "" : String.valueOf(row[i])));
                }
                sb.append("\r\n");
            }
        }
        return sb.toString();
    }

    private static String csvCell(String value) {
        if (value == null) {
            return "";
        }
        boolean quote = value.indexOf(';') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
        if (!quote) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // ------------------------------------------------------------------ tokenizer & parser helpers

    private static final class Token {
        final String raw;
        final String upper;
        final boolean quotedString;
        final int depth;

        Token(String raw, boolean quotedString, int depth) {
            this.raw = raw;
            this.upper = quotedString ? "" : raw.toUpperCase(Locale.ROOT);
            this.quotedString = quotedString;
            this.depth = depth;
        }
    }

    private static List<Token> tokenize(String sql) {
        List<Token> tokens = new ArrayList<Token>();
        int len = sql.length();
        int depth = 0;
        int i = 0;
        while (i < len) {
            char c = sql.charAt(i);
            char next = i + 1 < len ? sql.charAt(i + 1) : '\0';

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            // Line comment -- or #
            if ((c == '-' && next == '-') || c == '#') {
                while (i < len && sql.charAt(i) != '\n') {
                    i++;
                }
                continue;
            }
            // Block comment /* ... */
            if (c == '/' && next == '*') {
                i += 2;
                while (i + 1 < len && !(sql.charAt(i) == '*' && sql.charAt(i + 1) == '/')) {
                    i++;
                }
                i = Math.min(len, i + 2);
                continue;
            }
            // Single-quoted string literal
            if (c == '\'') {
                StringBuilder sb = new StringBuilder();
                i++;
                while (i < len) {
                    char sc = sql.charAt(i);
                    char sn = i + 1 < len ? sql.charAt(i + 1) : '\0';
                    if (sc == '\'' && sn == '\'') {
                        sb.append('\'');
                        i += 2;
                    } else if (sc == '\\' && sn != '\0') {
                        sb.append(sn);
                        i += 2;
                    } else if (sc == '\'') {
                        i++;
                        break;
                    } else {
                        sb.append(sc);
                        i++;
                    }
                }
                tokens.add(new Token(sb.toString(), true, depth));
                continue;
            }
            // Quoted identifier "..." or `...`
            if (c == '"' || c == '`') {
                char q = c;
                StringBuilder sb = new StringBuilder();
                i++;
                while (i < len) {
                    char sc = sql.charAt(i);
                    char sn = i + 1 < len ? sql.charAt(i + 1) : '\0';
                    if (sc == q && sn == q) {
                        sb.append(q);
                        i += 2;
                    } else if (sc == q) {
                        i++;
                        break;
                    } else {
                        sb.append(sc);
                        i++;
                    }
                }
                tokens.add(new Token(sb.toString(), false, depth));
                continue;
            }
            if (c == '(') {
                tokens.add(new Token("(", false, depth));
                depth++;
                i++;
                continue;
            }
            if (c == ')') {
                depth = Math.max(0, depth - 1);
                tokens.add(new Token(")", false, depth));
                i++;
                continue;
            }
            if (c == ',' || c == ';') {
                tokens.add(new Token(String.valueOf(c), false, depth));
                i++;
                continue;
            }
            if (isWordChar(c)) {
                int start = i;
                while (i < len && (isWordChar(sql.charAt(i)) || sql.charAt(i) == '.')) {
                    i++;
                }
                tokens.add(new Token(sql.substring(start, i), false, depth));
                continue;
            }
            i++;
        }
        return tokens;
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '$';
    }

    private static int findMainVerbAfterWith(List<Token> tokens) {
        // Scan top-level (depth == 0) after WITH for the first main DML/SELECT keyword
        for (int i = 1; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.depth != 0 || t.quotedString) {
                continue;
            }
            if ("SELECT".equals(t.upper) || "UPDATE".equals(t.upper) || "DELETE".equals(t.upper)
                    || "INSERT".equals(t.upper) || "MERGE".equals(t.upper)) {
                return i;
            }
        }
        return 0;
    }

    private static int findModifyingVerbInsideCte(List<Token> tokens, int mainVerbIdx) {
        int limit = mainVerbIdx > 0 ? Math.min(mainVerbIdx, tokens.size()) : tokens.size();
        for (int i = 1; i < limit; i++) {
            Token t = tokens.get(i);
            if (t.depth <= 0 || t.quotedString) {
                continue;
            }
            if ("(".equals(tokens.get(i - 1).raw)
                    && ("INSERT".equals(t.upper) || "UPDATE".equals(t.upper) || "DELETE".equals(t.upper)
                    || "MERGE".equals(t.upper) || "REPLACE".equals(t.upper))) {
                return i;
            }
        }
        return -1;
    }

    private static StatementKind classifyVerb(String verb) {
        if (verb == null) {
            return StatementKind.OTHER_WRITE;
        }
        switch (verb) {
            case "SELECT":
            case "SHOW":
            case "DESCRIBE":
            case "DESC":
            case "EXPLAIN":
            case "VALUES":
                return StatementKind.SELECT;
            case "INSERT":
            case "REPLACE":
            case "MERGE":
                return StatementKind.INSERT;
            case "UPDATE":
                return StatementKind.UPDATE;
            case "DELETE":
                return StatementKind.DELETE;
            case "ALTER":
            case "RENAME":
                return StatementKind.ALTER_TABLE;
            case "DROP":
            case "TRUNCATE":
                return StatementKind.DDL_DESTRUCTIVE;
            case "CREATE":
                return StatementKind.DDL_CREATE;
            default:
                return StatementKind.OTHER_WRITE;
        }
    }

    private static boolean hasTopLevelKeyword(List<Token> tokens, int fromIdx, String keyword) {
        for (int i = Math.max(0, fromIdx); i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.depth == 0 && !t.quotedString && keyword.equals(t.upper)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> extractTables(List<Token> tokens, int verbIdx, StatementKind kind) {
        LinkedHashSet<String> found = new LinkedHashSet<String>();
        int n = tokens.size();
        if (verbIdx < 0 || verbIdx >= n) {
            return Collections.emptyList();
        }
        int i = verbIdx + 1;

        if (kind == StatementKind.ALTER_TABLE) {
            String verb = tokens.get(verbIdx).upper;
            if ("RENAME".equals(verb)) {
                if (i < n && "TABLE".equals(tokens.get(i).upper)) {
                    i++;
                }
                addTableToken(found, tokens, i);
                for (int k = i + 1; k + 1 < n; k++) {
                    if ("TO".equals(tokens.get(k).upper) || ",".equals(tokens.get(k).raw)) {
                        addTableToken(found, tokens, k + 1);
                    }
                }
            } else {
                // ALTER [ONLINE|OFFLINE] [IGNORE] TABLE|VIEW|INDEX [IF EXISTS] <name>
                while (i < n && isModifier(tokens.get(i).upper, "ONLINE", "OFFLINE", "IGNORE", "TABLE", "VIEW", "INDEX", "IF", "EXISTS")) {
                    i++;
                }
                addTableToken(found, tokens, i);
            }
        } else if (kind == StatementKind.DDL_DESTRUCTIVE) {
            // DROP [TEMPORARY] TABLE|VIEW|INDEX [IF EXISTS] t1, t2  OR  TRUNCATE [TABLE] t1
            while (i < n && isModifier(tokens.get(i).upper, "TEMPORARY", "TEMP", "TABLE", "VIEW", "INDEX", "TRIGGER", "SCHEMA", "IF", "EXISTS")) {
                i++;
            }
            addTableToken(found, tokens, i);
            while (i + 2 < n && ",".equals(tokens.get(i + 1).raw)) {
                i += 2;
                addTableToken(found, tokens, i);
            }
        } else if (kind == StatementKind.DDL_CREATE) {
            // CREATE [OR REPLACE] [TEMPORARY] [UNIQUE] TABLE|VIEW|INDEX [IF NOT EXISTS] name [ON tbl]
            boolean isIndex = false;
            while (i < n && isModifier(tokens.get(i).upper, "OR", "REPLACE", "TEMPORARY", "TEMP", "UNIQUE", "GLOBAL", "LOCAL",
                    "TABLE", "VIEW", "INDEX", "TRIGGER", "IF", "NOT", "EXISTS")) {
                if ("INDEX".equals(tokens.get(i).upper)) {
                    isIndex = true;
                }
                i++;
            }
            if (isIndex) {
                for (int k = i; k + 1 < n; k++) {
                    if (tokens.get(k).depth == 0 && "ON".equals(tokens.get(k).upper)) {
                        addTableToken(found, tokens, k + 1);
                        break;
                    }
                }
            } else {
                addTableToken(found, tokens, i);
            }
        } else if (kind == StatementKind.UPDATE) {
            while (i < n && isModifier(tokens.get(i).upper, "LOW_PRIORITY", "IGNORE", "ONLY")) {
                i++;
            }
            addTableToken(found, tokens, i);
        } else if (kind == StatementKind.INSERT) {
            while (i < n && isModifier(tokens.get(i).upper, "LOW_PRIORITY", "DELAYED", "HIGH_PRIORITY", "IGNORE", "INTO")) {
                i++;
            }
            addTableToken(found, tokens, i);
        } else if (kind == StatementKind.DELETE) {
            while (i < n && isModifier(tokens.get(i).upper, "LOW_PRIORITY", "QUICK", "IGNORE", "FROM", "ONLY")) {
                i++;
            }
            addTableToken(found, tokens, i);
        } else if (kind == StatementKind.SELECT) {
            for (int k = verbIdx + 1; k + 1 < n; k++) {
                Token t = tokens.get(k);
                if (!t.quotedString && ("FROM".equals(t.upper) || "JOIN".equals(t.upper))) {
                    addTableToken(found, tokens, k + 1);
                }
            }
        }

        return new ArrayList<String>(found);
    }

    private static boolean isModifier(String upper, String... mods) {
        for (String m : mods) {
            if (m.equals(upper)) {
                return true;
            }
        }
        return false;
    }

    private static void addTableToken(Set<String> found, List<Token> tokens, int idx) {
        if (idx < 0 || idx >= tokens.size()) {
            return;
        }
        Token t = tokens.get(idx);
        if (t.quotedString || "(".equals(t.raw) || ")".equals(t.raw) || ",".equals(t.raw) || ";".equals(t.raw)) {
            return;
        }
        String name = t.raw.trim();
        if (!name.isEmpty()) {
            found.add(name);
        }
    }

    private static String baseTableName(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim().replace("`", "").replace("\"", "");
        int dot = s.lastIndexOf('.');
        if (dot >= 0 && dot + 1 < s.length()) {
            s = s.substring(dot + 1);
        }
        return s.toLowerCase(Locale.ROOT);
    }
}
