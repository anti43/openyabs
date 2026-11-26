package mpv5.db.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mpv5.db.common.SqlConsole.ColumnInfo;
import mpv5.db.common.SqlConsole.ExecutionResult;
import mpv5.db.common.SqlConsole.RiskLevel;
import mpv5.db.common.SqlConsole.StatementInfo;
import mpv5.db.common.SqlConsole.StatementKind;
import mpv5.handler.Activity;
import org.junit.jupiter.api.Test;

public class SqlConsoleTest {

    @Test
    public void splitStatementsRespectsQuotesAndComments() {
        String script = "-- leading comment with ; semicolon\n"
                + "SELECT 'a;b''c', \"col;name\" FROM items;\n"
                + "/* block comment ; still comment */\n"
                + "UPDATE items SET description = 'x;y' WHERE ids = 1;\n"
                + "-- comment only chunk\n"
                + "   ;   \n"
                + "SELECT `k;v` FROM contacts";
        List<String> stmts = SqlConsole.splitStatements(script);
        assertEquals(3, stmts.size());
        assertTrue(stmts.get(0).contains("SELECT 'a;b''c'"));
        assertTrue(stmts.get(1).contains("UPDATE items SET description = 'x;y'"));
        assertTrue(stmts.get(2).contains("SELECT `k;v` FROM contacts"));
    }

    @Test
    public void splitStatementsIgnoresCommentOnlyInput() {
        assertTrue(SqlConsole.splitStatements("-- only a comment\n/* block */").isEmpty());
        assertTrue(SqlConsole.splitStatements("   ; ; \n ").isEmpty());
        assertTrue(SqlConsole.splitStatements(null).isEmpty());
    }

    @Test
    public void analyzesSelectCteAndStringLiteralsSafely() {
        StatementInfo s1 = SqlConsole.analyze("SELECT * FROM items WHERE description = 'update delete alter table'");
        assertEquals(StatementKind.SELECT, s1.kind);
        assertFalse(s1.modifying);
        assertFalse(s1.alterOrDdl);
        assertFalse(s1.missingWhere);
        assertEquals(List.of("items"), s1.tables);
        assertEquals(RiskLevel.READ_ONLY, s1.riskLevel(true));

        StatementInfo cteSelect = SqlConsole.analyze(
                "WITH recent AS (SELECT ids FROM items WHERE ids > 10) SELECT r.ids, c.cname FROM recent r JOIN contacts c ON r.ids = c.ids");
        assertEquals(StatementKind.SELECT, cteSelect.kind);
        assertFalse(cteSelect.modifying);
        assertEquals(RiskLevel.READ_ONLY, cteSelect.riskLevel(true));

        StatementInfo cteUpdate = SqlConsole.analyze(
                "WITH old_items AS (SELECT ids FROM items) UPDATE items SET dateend = CURRENT_DATE");
        assertEquals(StatementKind.UPDATE, cteUpdate.kind);
        assertTrue(cteUpdate.modifying);
        assertTrue(cteUpdate.missingWhere);
        assertTrue(cteUpdate.touchesTaxTable);
        assertEquals(List.of("items"), cteUpdate.tables);
        assertEquals(RiskLevel.CONFIRM, cteUpdate.riskLevel(true));
    }

    @Test
    public void analyzesDmlMissingWhereAndTargets() {
        StatementInfo updWithWhere = SqlConsole.analyze("UPDATE items SET dateend = DATE '2026-01-01' WHERE ids = 5");
        assertEquals(StatementKind.UPDATE, updWithWhere.kind);
        assertTrue(updWithWhere.modifying);
        assertFalse(updWithWhere.alterOrDdl);
        assertFalse(updWithWhere.missingWhere);
        assertEquals(List.of("items"), updWithWhere.tables);
        assertTrue(updWithWhere.touchesTaxTable);
        assertFalse(updWithWhere.touchesGobdTable);
        assertEquals(RiskLevel.CONFIRM, updWithWhere.riskLevel(false));

        StatementInfo updNoWhere = SqlConsole.analyze("UPDATE items SET dateend = DATE '2026-01-01'");
        assertEquals(StatementKind.UPDATE, updNoWhere.kind);
        assertTrue(updNoWhere.missingWhere);

        StatementInfo delNoWhere = SqlConsole.analyze("DELETE FROM revenues");
        assertEquals(StatementKind.DELETE, delNoWhere.kind);
        assertTrue(delNoWhere.missingWhere);
        assertEquals(List.of("revenues"), delNoWhere.tables);

        StatementInfo delSubqueryOnlyWhere = SqlConsole.analyze(
                "DELETE FROM contacts /* where is only in comment */");
        assertTrue(delSubqueryOnlyWhere.missingWhere);

        StatementInfo ins = SqlConsole.analyze("INSERT INTO notes (cname) VALUES ('hi')");
        assertEquals(StatementKind.INSERT, ins.kind);
        assertTrue(ins.modifying);
        assertFalse(ins.missingWhere);
        assertFalse(ins.touchesTaxTable);
        assertEquals(List.of("notes"), ins.tables);
    }

    @Test
    public void analyzesAlterAndDdlWithRiskEscalation() {
        StatementInfo alterNonTax = SqlConsole.analyze("ALTER TABLE custom_notes ADD COLUMN tag VARCHAR(50)");
        assertEquals(StatementKind.ALTER_TABLE, alterNonTax.kind);
        assertTrue(alterNonTax.modifying);
        assertTrue(alterNonTax.alterOrDdl);
        assertFalse(alterNonTax.touchesTaxTable);
        assertEquals(List.of("custom_notes"), alterNonTax.tables);
        assertEquals(RiskLevel.WARNING_DDL, alterNonTax.riskLevel(false));
        assertEquals(RiskLevel.WARNING_DDL, alterNonTax.riskLevel(true));

        StatementInfo alterTax = SqlConsole.analyze("ALTER TABLE items ADD COLUMN legacy_ref VARCHAR(50)");
        assertEquals(StatementKind.ALTER_TABLE, alterTax.kind);
        assertTrue(alterTax.touchesTaxTable);
        assertEquals(RiskLevel.WARNING_DDL, alterTax.riskLevel(false));
        assertEquals(RiskLevel.CRITICAL_GOBD, alterTax.riskLevel(true));

        StatementInfo alterGobd = SqlConsole.analyze("ALTER TABLE gobd_audit_log DROP COLUMN row_hash");
        assertEquals(StatementKind.ALTER_TABLE, alterGobd.kind);
        assertTrue(alterGobd.touchesGobdTable);
        assertEquals(RiskLevel.CRITICAL_GOBD, alterGobd.riskLevel(false));
        assertEquals(RiskLevel.CRITICAL_GOBD, alterGobd.riskLevel(true));

        StatementInfo dropTbl = SqlConsole.analyze("DROP TABLE IF EXISTS temp_a, temp_b");
        assertEquals(StatementKind.DDL_DESTRUCTIVE, dropTbl.kind);
        assertTrue(dropTbl.alterOrDdl);
        assertEquals(List.of("temp_a", "temp_b"), dropTbl.tables);

        StatementInfo trunc = SqlConsole.analyze("TRUNCATE TABLE gobd_document_lock");
        assertEquals(StatementKind.DDL_DESTRUCTIVE, trunc.kind);
        assertTrue(trunc.touchesGobdTable);
        assertEquals(RiskLevel.CRITICAL_GOBD, trunc.riskLevel(false));

        StatementInfo createIdx = SqlConsole.analyze("CREATE INDEX idx_items_num ON items (cnumber)");
        assertEquals(StatementKind.DDL_CREATE, createIdx.kind);
        assertTrue(createIdx.alterOrDdl);
        assertEquals(List.of("items"), createIdx.tables);
        assertTrue(createIdx.touchesTaxTable);

        StatementInfo rename = SqlConsole.analyze("RENAME TABLE old_tbl TO new_tbl");
        assertEquals(StatementKind.ALTER_TABLE, rename.kind);
        assertEquals(List.of("old_tbl", "new_tbl"), rename.tables);
    }

    @Test
    public void executesQueriesAuditsMutationsAndIntrospectsSchema() throws Exception {
        List<Activity.Entry> sqlEvents = new ArrayList<Activity.Entry>();
        Consumer<Activity.Entry> listener = e -> {
            if (e != null && e.kind == Activity.Kind.SQL) {
                sqlEvents.add(e);
            }
        };
        Activity.addListener(listener);
        try (Connection c = DriverManager.getConnection("jdbc:h2:mem:sqlconsoletest;MODE=Derby;DB_CLOSE_DELAY=-1", "sa", "")) {
            // 1. CREATE TABLE -> audited
            ExecutionResult rCreate = SqlConsole.execute(c,
                    SqlConsole.analyze("CREATE TABLE demo_items (id INT PRIMARY KEY, title VARCHAR(100))"), 200, true);
            assertTrue(rCreate.success, rCreate.errorMessage);
            assertEquals(1, sqlEvents.size());
            assertEquals("DDL_CREATE", sqlEvents.get(0).sqlAction);
            assertEquals("demo_items", sqlEvents.get(0).sqlTables);

            // 2. INSERT -> audited
            for (int i = 1; i <= 5; i++) {
                ExecutionResult rIns = SqlConsole.execute(c,
                        SqlConsole.analyze("INSERT INTO demo_items VALUES (" + i + ", 'Row " + i + "')"), 200, true);
                assertTrue(rIns.success, rIns.errorMessage);
                assertEquals(1, rIns.updateCount);
            }
            assertEquals(6, sqlEvents.size());

            // 3. SELECT with row limit truncation -> NOT audited
            ExecutionResult rSel = SqlConsole.execute(c,
                    SqlConsole.analyze("SELECT id, title FROM demo_items ORDER BY id"), 3, true);
            assertTrue(rSel.success);
            assertTrue(rSel.hasResultSet);
            assertEquals(2, rSel.columns.length);
            assertEquals(3, rSel.rows.length);
            assertTrue(rSel.truncated);
            assertEquals(6, sqlEvents.size(), "Read-only SELECT must not emit SQL audit events");

            // 4. UPDATE -> audited with affected row count
            ExecutionResult rUpd = SqlConsole.execute(c,
                    SqlConsole.analyze("UPDATE demo_items SET title = 'Updated' WHERE id <= 2"), 200, true);
            assertTrue(rUpd.success);
            assertEquals(2, rUpd.updateCount);
            assertEquals(7, sqlEvents.size());
            Activity.Entry updEntry = sqlEvents.get(6);
            assertEquals("UPDATE", updEntry.sqlAction);
            assertEquals("demo_items", updEntry.sqlTables);
            assertTrue(updEntry.sqlStatement.contains("UPDATE demo_items"));
            assertTrue(updEntry.sqlOutcome.contains("2 rows affected"));

            // 5. ALTER TABLE -> audited
            ExecutionResult rAlter = SqlConsole.execute(c,
                    SqlConsole.analyze("ALTER TABLE demo_items ADD COLUMN note VARCHAR(50)"), 200, true);
            assertTrue(rAlter.success, rAlter.errorMessage);
            assertEquals(8, sqlEvents.size());
            assertEquals("ALTER_TABLE", sqlEvents.get(7).sqlAction);
            assertEquals("demo_items", sqlEvents.get(7).sqlTables);

            // 6. Failed UPDATE on gobd_* table -> logs BOTH PRE_EXECUTE attempt and UPDATE_FAILED
            ExecutionResult rGobd = SqlConsole.execute(c,
                    SqlConsole.analyze("UPDATE gobd_audit_log SET details = 'tamper' WHERE id = 1"), 200, true);
            assertFalse(rGobd.success);
            assertEquals(10, sqlEvents.size());
            assertEquals("ATTEMPT_UPDATE", sqlEvents.get(8).sqlAction);
            assertEquals("PRE_EXECUTE", sqlEvents.get(8).sqlOutcome);
            assertEquals("UPDATE_FAILED", sqlEvents.get(9).sqlAction);
            assertTrue(sqlEvents.get(9).sqlOutcome.startsWith("ERROR:"));

            // 7. Schema introspection
            List<String> tables = SqlConsole.listTables(c);
            assertTrue(tables.contains("demo_items"), "Tables: " + tables);
            List<ColumnInfo> cols = SqlConsole.listColumns(c, "demo_items");
            assertEquals(3, cols.size());
            assertEquals("id", cols.get(0).name);
            assertEquals("title", cols.get(1).name);
            assertEquals("note", cols.get(2).name);

            // 8. CSV export with escaping & UTF-8 BOM
            String csv = SqlConsole.toCsv(
                    new String[]{"id", "title"},
                    new Object[][]{{1, "a;b"}, {2, "say \"hi\""}});
            assertTrue(csv.startsWith("\uFEFFid;title\r\n"));
            assertTrue(csv.contains("1;\"a;b\"\r\n"));
            assertTrue(csv.contains("2;\"say \"\"hi\"\"\"\r\n"));
            assertNotNull(SqlConsole.analyzeScript("SELECT 1; SELECT 2"));
        } finally {
            Activity.removeListener(listener);
        }
    }

    @Test
    public void enforcesQueryTimeoutAndSupportsCancellation() throws Exception {
        String slowSql = "SELECT COUNT(*) FROM SYSTEM_RANGE(1, 50000) a CROSS JOIN SYSTEM_RANGE(1, 50000) b";
        StatementInfo slowInfo = SqlConsole.analyze(slowSql);

        try (Connection c = DriverManager.getConnection("jdbc:h2:mem:sqlconsoletimeout;MODE=Derby", "sa", "")) {
            // 1. Timeout after 1 second
            ExecutionResult timedOut = SqlConsole.execute(c, slowInfo, 100, 1, false, null, null);
            assertFalse(timedOut.success);
            assertTrue(timedOut.timedOut, "Expected timedOut=true, got: " + timedOut.outcomeSummary());
            assertFalse(timedOut.cancelled);
            assertTrue(timedOut.outcomeSummary().startsWith("TIMEOUT:"), timedOut.outcomeSummary());

            // 2. Cooperative mid-flight cancellation via cancelQuietly
            java.util.concurrent.atomic.AtomicReference<java.sql.Statement> activeStm =
                    new java.util.concurrent.atomic.AtomicReference<java.sql.Statement>();
            java.util.concurrent.atomic.AtomicBoolean cancelFlag =
                    new java.util.concurrent.atomic.AtomicBoolean(false);
            Thread canceller = new Thread(() -> {
                try {
                    for (int i = 0; i < 40 && activeStm.get() == null; i++) {
                        Thread.sleep(25);
                    }
                    Thread.sleep(100);
                    cancelFlag.set(true);
                    SqlConsole.cancelQuietly(activeStm);
                } catch (InterruptedException ignored) {
                }
            });
            canceller.start();
            ExecutionResult cancelled = SqlConsole.execute(c, slowInfo, 100, 30, false, activeStm, cancelFlag);
            canceller.join(3000);
            assertFalse(cancelled.success);
            assertTrue(cancelled.cancelled, "Expected cancelled=true, got: " + cancelled.outcomeSummary());
            assertFalse(cancelled.timedOut);
            assertTrue(cancelled.outcomeSummary().startsWith("CANCELLED"), cancelled.outcomeSummary());
        }
    }

    @Test
    public void detectsDisguisedModifyingStatements() {
        StatementInfo selectInto = SqlConsole.analyze("SELECT * INTO backup_items FROM items");
        assertTrue(selectInto.modifying);
        assertEquals(StatementKind.OTHER_WRITE, selectInto.kind);

        StatementInfo modifyingCte = SqlConsole.analyze(
                "WITH deleted AS (DELETE FROM items RETURNING *) SELECT * FROM deleted");
        assertTrue(modifyingCte.modifying);
        assertEquals(StatementKind.DELETE, modifyingCte.kind);
        assertEquals(List.of("items"), modifyingCte.tables);

        StatementInfo explainAnalyze = SqlConsole.analyze("EXPLAIN ANALYZE DELETE FROM items WHERE ids = 1");
        assertTrue(explainAnalyze.modifying);
        assertEquals(StatementKind.OTHER_WRITE, explainAnalyze.kind);

        StatementInfo plainExplain = SqlConsole.analyze("EXPLAIN SELECT * FROM items");
        assertFalse(plainExplain.modifying);
        assertEquals(StatementKind.SELECT, plainExplain.kind);
    }

    @Test
    public void blocksNonSelectStatementsWhenAllowModifyIsFalse() throws Exception {
        List<Activity.Entry> sqlEvents = new ArrayList<Activity.Entry>();
        Consumer<Activity.Entry> listener = e -> {
            if (e != null && e.kind == Activity.Kind.SQL) {
                sqlEvents.add(e);
            }
        };
        Activity.addListener(listener);
        try (Connection c = DriverManager.getConnection("jdbc:h2:mem:sqlconsolereadonly;MODE=Derby;DB_CLOSE_DELAY=-1", "sa", "")) {
            // Seed table as admin (allowModify = true)
            assertTrue(SqlConsole.execute(c,
                    SqlConsole.analyze("CREATE TABLE ro_items (id INT PRIMARY KEY, title VARCHAR(100))"),
                    200, false).success);
            assertTrue(SqlConsole.execute(c,
                    SqlConsole.analyze("INSERT INTO ro_items VALUES (1, 'Original')"),
                    200, false).success);

            // Non-admin SELECT succeeds and does not emit an audit event
            ExecutionResult sel = SqlConsole.execute(c,
                    SqlConsole.analyze("SELECT id, title FROM ro_items"),
                    200, 30, true, false, null, null);
            assertTrue(sel.success, sel.errorMessage);
            assertEquals(1, sel.rows.length);
            assertEquals("Original", sel.rows[0][1]);
            assertTrue(sqlEvents.isEmpty());

            // Non-admin modifying statements are all blocked and audited as BLOCKED_*
            String[] blockedStatements = new String[]{
                "UPDATE ro_items SET title = 'Hacked' WHERE id = 1",
                "DELETE FROM ro_items WHERE id = 1",
                "INSERT INTO ro_items VALUES (2, 'Injected')",
                "ALTER TABLE ro_items ADD COLUMN extra VARCHAR(20)",
                "DROP TABLE ro_items",
                "CREATE TABLE ro_extra (id INT)",
                "SELECT * INTO ro_copy FROM ro_items"
            };
            for (String sql : blockedStatements) {
                StatementInfo info = SqlConsole.analyze(sql);
                ExecutionResult res = SqlConsole.execute(c, info, 200, 30, true, false, null, null);
                assertFalse(res.success, "Expected non-admin statement to be blocked: " + sql);
                assertTrue(res.errorMessage != null && res.errorMessage.contains("Administrator"),
                        "Unexpected error message for " + sql + ": " + res.errorMessage);
            }
            assertEquals(blockedStatements.length, sqlEvents.size());
            for (Activity.Entry entry : sqlEvents) {
                assertTrue(entry.sqlAction.startsWith("BLOCKED_"), "Expected BLOCKED_* action, got " + entry.sqlAction);
                assertTrue(entry.sqlOutcome.startsWith("DENIED:"), "Expected DENIED outcome, got " + entry.sqlOutcome);
            }

            // Verify table and row remain completely untouched
            ExecutionResult verify = SqlConsole.execute(c,
                    SqlConsole.analyze("SELECT id, title FROM ro_items"),
                    200, 30, false, false, null, null);
            assertTrue(verify.success, verify.errorMessage);
            assertEquals(1, verify.rows.length);
            assertEquals("Original", verify.rows[0][1]);
        } finally {
            Activity.removeListener(listener);
        }
    }
}
