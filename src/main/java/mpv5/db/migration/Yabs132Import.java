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
package mpv5.db.migration;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.DatabaseUpdater;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.SchemaUpdate;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.logging.Log;

/**
 * Imports a YaBS 1.3.2 database (Apache Derby, database version 1.1984; older
 * versions are updated on the way) into the open H2 database.
 * <p>
 * <b>The old database is never opened:</b> its folder is copied to a
 * temporary folder and only the copy is read (Derby would otherwise upgrade
 * or recover it). The copy is deleted afterwards.
 * <p>
 * <b>Strategy:</b> the target must be a new database (no contacts,
 * documents, products, …). All its tables that also exist in the old database
 * are emptied and filled with the old rows <i>with their old ids</i>, so all
 * references (contacts of documents, positions, files of templates, users of
 * settings, …) stay as they were - nothing has to be renumbered. Afterwards
 * new ids continue after the highest imported id. Kept from the new database:
 * its templates (as additional templates), countries the old database does
 * not have and global settings the old database does not have.
 * <p>
 * Everything runs in one transaction: if the import fails, the target stays
 * unchanged.
 * <p>
 * <b>Old escaping:</b> YaBS 1.3.2 stored every ' as ` and, with the setting
 * "escape backslashes" (on by default), every \ as
 * "&lt;removedbackslash&gt;". Both are converted back (` only if wanted, see
 * {@link #setConvertBackticks(boolean)}; never in number formats, where ' is
 * a quote character of the pattern and would change the numbers, and the
 * numbers made from them).
 * <p>
 * Explicitly mapped columns: conversations.date is conversations.timestamp in
 * H2, revenues.ispaid (databases before 1.1982) is revenues.status (paid = 4).
 * Not imported: the record locks of the old program (tablelock) and the search
 * index (rebuilt from the imported data). Columns and tables without
 * counterpart are reported with the number of values that could not be
 * imported - nothing is dropped silently.
 */
public class Yabs132Import {

    /**
     * The database version of YaBS 1.3.2
     */
    public static final double VERSION_132 = 1.1984;
    static final String BACKSLASH = QueryHandler.BACKSLASH_REPLACEMENT_STRING;
    /**
     * Tables that must be empty in the target: the import replaces the data
     */
    static final List<String> BUSINESS_TABLES = List.of("CONTACTS", "ITEMS", "SUBITEMS", "PRODUCTS", "EXPENSES",
            "REVENUES", "PRODUCTORDERS", "ACTIVITYLISTS", "CONVERSATIONS", "SCHEDULE", "PAYMENTS", "POSTRANSACTIONS");
    /**
     * Columns renamed in the current structure: TABLE.OLDCOLUMN - NEWCOLUMN
     */
    static final Map<String, String> RENAMED = Map.of(
            "CONVERSATIONS.DATE", "TIMESTAMP",
            "REVENUES.ISPAID", "STATUS");
    /**
     * Texts in which ` stays: number formats are java.text.MessageFormat
     * patterns, a ' there is a quote character and would change the numbers.
     * YaBS 1.3.2 printed the ` as it is. The numbers (column cnumber) keep it
     * too: they were made from the formats, the next number must follow them.
     */
    static final Set<String> KEEP_BACKTICKS = Set.of("FORMATSTOUSERS.CNAME");
    /**
     * @return true if ` stays in the column: number formats and the numbers
     * made from them
     */
    static boolean keepBackticks(String table, String column) {
        return KEEP_BACKTICKS.contains(table + "." + column) || "CNUMBER".equals(column);
    }

    /**
     * Rebuilt from the imported data by the triggers of the new database
     */
    static final String SEARCHINDEX = "SEARCHINDEX";
    /**
     * Records opened in the old program, not data
     */
    static final String TABLELOCK = "TABLELOCK";

    /**
     * Progress of the import
     */
    public interface Progress {

        /**
         * @param message what is done
         * @param percent 0 - 100
         */
        void update(String message, int percent);
    }

    /**
     * Thrown if the import can not start (no YaBS database, target not
     * empty, …); nothing was changed
     */
    public static class ImportException extends Exception {

        private static final long serialVersionUID = 1L;

        public ImportException(String message) {
            super(message);
        }

        public ImportException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Rows of one table
     */
    public static class TableResult {

        public final String table;
        public long oldRows;
        public long imported;
        public final List<String> notes = new ArrayList<String>();

        TableResult(String table) {
            this.table = table;
        }
    }

    /**
     * The result: rows per table, conversions, everything not imported
     */
    public static class Result {

        public File source;
        public String schema;
        public double oldVersion;
        public final Map<String, TableResult> tables = new TreeMap<String, TableResult>();
        /**
         * Notes, e.g. what was kept of the new database
         */
        public final List<String> notes = new ArrayList<String>();
        /**
         * Data that was NOT imported (or could not be imported)
         */
        public final List<String> problems = new ArrayList<String>();
        public long backslashes;
        public long backticks;
        public long searchIndex;

        /**
         * @return all rows imported
         */
        public long importedRows() {
            long n = 0;
            for (TableResult t : tables.values()) {
                n += t.imported;
            }
            return n;
        }

        /**
         * @return the report as text (rows per table, conversions, notes,
         * problems)
         */
        public String getSummary() {
            StringBuilder b = new StringBuilder();
            b.append(String.format(Messages.Y132_REPORT_SOURCE.toString(), source, Double.toString(oldVersion))).append('\n');
            b.append(String.format(Messages.Y132_REPORT_ROWS.toString(), importedRows(), tables.size())).append("\n\n");
            int w = 8;
            for (String t : tables.keySet()) {
                w = Math.max(w, t.length());
            }
            b.append(String.format("%-" + w + "s %10s %10s%n", Messages.Y132_REPORT_TABLE, Messages.Y132_REPORT_OLD, Messages.Y132_REPORT_IMPORTED));
            for (TableResult t : tables.values()) {
                b.append(String.format("%-" + w + "s %10d %10d", t.table.toLowerCase(Locale.ROOT), t.oldRows, t.imported));
                if (!t.notes.isEmpty()) {
                    b.append("   ").append(String.join("; ", t.notes));
                }
                b.append('\n');
            }
            b.append('\n');
            b.append(String.format(Messages.Y132_REPORT_CONVERTED.toString(), backslashes, backticks)).append('\n');
            for (String n : notes) {
                b.append("- ").append(n).append('\n');
            }
            b.append('\n');
            if (problems.isEmpty()) {
                b.append(Messages.Y132_REPORT_NOTHING_LOST);
            } else {
                b.append(Messages.Y132_REPORT_PROBLEMS).append('\n');
                for (String p : problems) {
                    b.append("- ").append(p).append('\n');
                }
            }
            return b.toString();
        }
    }

    private boolean convertBackticks = true;
    private String user;
    private String password;
    private Progress progress = (m, p) -> {
    };

    /**
     * @param convertBackticks true (default): every ` of the old texts
     * becomes ' again (YaBS 1.3.2 stored ' as `, a typed ` can not be told
     * apart)
     */
    public void setConvertBackticks(boolean convertBackticks) {
        this.convertBackticks = convertBackticks;
    }

    /**
     * Only needed if the old Derby database requires a user (authentication
     * on)
     *
     * @param user may be null
     * @param password may be null
     */
    public void setCredentials(String user, String password) {
        this.user = user == null || user.isBlank() ? null : user;
        this.password = password;
    }

    /**
     * @param progress may be null
     */
    public void setProgress(Progress progress) {
        this.progress = progress == null ? (m, p) -> {
        } : progress;
    }

    /**
     * @param dir
     * @return true if the folder is an Apache Derby database
     */
    public static boolean isDerbyDatabase(File dir) {
        return dir != null && new File(dir, "service.properties").isFile() && new File(dir, "seg0").isDirectory();
    }

    /**
     * @param dir a Derby database or a folder containing some (up to two
     * levels deep, e.g. the YaBS folder ~/.yabs)
     * @return the Derby databases
     */
    public static List<File> findDatabases(File dir) {
        List<File> l = new ArrayList<File>();
        find(dir, 2, l);
        return l;
    }

    private static void find(File dir, int depth, List<File> l) {
        if (dir == null || !dir.isDirectory()) {
            return;
        }
        if (isDerbyDatabase(dir)) {
            l.add(dir);
            return;
        }
        if (depth == 0) {
            return;
        }
        File[] files = dir.listFiles(File::isDirectory);
        if (files == null) {
            return;
        }
        Arrays.sort(files);
        for (File f : files) {
            find(f, depth - 1, l);
        }
    }

    /**
     * The user to sign in as after an import: "admin" if there, else the
     * enabled user with the most rights
     *
     * @param c the database
     * @return ids, cname, password (MD5) - null if there is no user
     * @throws SQLException
     */
    public static String[] signInUser(Connection c) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(
                "SELECT ids, cname, password FROM users WHERE invisible = 0 "
                + "ORDER BY CASE WHEN cname = 'admin' THEN 0 ELSE 1 END, CASE WHEN isenabled = 1 THEN 0 ELSE 1 END, inthighestright, ids")) {
            return rs.next() ? new String[]{rs.getString(1), rs.getString(2), rs.getString(3)} : null;
        }
    }

    /**
     * Save the report as text file
     *
     * @param r the result
     * @param dir the folder (the YaBS folder)
     * @return the file, null if it could not be written
     */
    public static File saveReport(Result r, File dir) {
        try {
            File f = new File(dir, "import-yabs-1.3.2-" + new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date()) + ".txt");
            Files.write(f.toPath(), r.getSummary().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return f;
        } catch (Exception e) {
            Log.Debug(Yabs132Import.class, e);
            return null;
        }
    }

    /**
     * Import the old database into the target. The target must be an H2
     * database without contacts, documents, products …
     *
     * @param oldDatabase the folder of the old Derby database (e.g.
     * ~/.yabs/yabs)
     * @param target the H2 database, auto commit on; a connection of its own
     * (the import runs in one transaction)
     * @return the result
     * @throws ImportException if the import can not start - nothing changed
     * @throws Exception if the import failed - the target is unchanged
     */
    public Result importInto(File oldDatabase, Connection target) throws Exception {
        if (oldDatabase == null || !isDerbyDatabase(oldDatabase)) {
            throw new ImportException(String.format(Messages.Y132_NOT_A_DATABASE.toString(), oldDatabase));
        }
        String product = target.getMetaData().getDatabaseProductName();
        if (product == null || !product.toUpperCase(Locale.ROOT).contains("H2")) {
            throw new ImportException(String.format(Messages.Y132_TARGET_NOT_H2.toString(), product));
        }
        Result r = new Result();
        r.source = oldDatabase.getAbsoluteFile();
        String targetSchema = target.getSchema();
        Map<String, String> targetTables = tables(target, targetSchema, "");
        checkEmpty(target, targetTables);

        Path tmp = Files.createTempDirectory("yabs132-import");
        File copy = new File(tmp.toFile(), "db");
        try {
            progress.update(Messages.Y132_COPYING.toString(), 1);
            if (new File(oldDatabase, "db.lck").exists()) {
                r.notes.add(Messages.Y132_LOCKED.toString());
            }
            copyDatabase(oldDatabase.toPath(), copy.toPath());
            try (Connection source = connect(copy)) {
                importFrom(source, copy, target, targetSchema, targetTables, r);
            } finally {
                shutdown(copy);
            }
        } finally {
            delete(tmp);
        }
        progress.update(Messages.Y132_DONE.toString(), 100);
        return r;
    }

    /**
     * The target must not hold data of its own: the import replaces it
     */
    private static void checkEmpty(Connection target, Map<String, String> targetTables) throws SQLException, ImportException {
        List<String> found = new ArrayList<String>();
        for (String t : BUSINESS_TABLES) {
            if (targetTables.containsKey(t)) {
                long n = count(target, targetTables.get(t));
                if (n > 0) {
                    found.add(t.toLowerCase(Locale.ROOT) + ": " + n);
                }
            }
        }
        if (!found.isEmpty()) {
            throw new ImportException(String.format(Messages.Y132_TARGET_NOT_EMPTY.toString(), String.join(", ", found)));
        }
    }

    private Connection connect(File copy) throws ImportException {
        String url = "jdbc:derby:" + copy.getAbsolutePath();
        try {
            return user == null ? DriverManager.getConnection(url) : DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new ImportException(String.format(Messages.Y132_CANNOT_OPEN.toString(), e.getMessage()), e);
        }
    }

    private void importFrom(Connection source, File copy, Connection target, String targetSchema,
            Map<String, String> targetTables, Result r) throws Exception {
        //the schema of the old tables is the user name YaBS connected with (APP without)
        String[] found = findSchema(source);
        if (found == null) {
            throw new ImportException(String.format(Messages.Y132_NOT_A_DATABASE.toString(), r.source));
        }
        r.schema = found[0];
        String prefix = found[1];
        try (Statement s = source.createStatement()) {
            s.execute("SET SCHEMA \"" + r.schema.replace("\"", "\"\"") + "\"");
        }
        r.oldVersion = version(source, prefix);
        if (r.oldVersion > Constants.DATABASE_VERSION) {
            r.notes.add(String.format(Messages.Y132_NEWER.toString(), Double.toString(r.oldVersion)));
        }
        if (r.oldVersion < VERSION_132) {
            //an older YaBS: the updates up to 1.3.2 on the copy, as YaBS 1.3.2 would have done
            progress.update(Messages.Y132_UPDATING.toString(), 4);
            updateCopy(source, copy, r, prefix);
        }

        Map<String, String> sourceTables = tables(source, r.schema, prefix);
        List<String> common = new ArrayList<String>();
        for (String t : orderByReferences(target, targetSchema, new ArrayList<String>(targetTables.keySet()))) {
            if (sourceTables.containsKey(t)) {
                common.add(t);
            }
        }
        for (String t : sourceTables.keySet()) {
            if (!targetTables.containsKey(t)) {
                long n = count(source, sourceTables.get(t));
                if (n > 0) {
                    r.problems.add(String.format(Messages.Y132_TABLE_MISSING.toString(), t.toLowerCase(Locale.ROOT), n));
                }
            }
        }
        long total = 0;
        Map<String, Long> counts = new HashMap<String, Long>();
        for (String t : common) {
            long n = count(source, sourceTables.get(t));
            counts.put(t, n);
            total += n;
        }

        //kept from the new database
        Kept kept = new Kept(target, targetTables);

        boolean auto = target.getAutoCommit();
        try (Statement s = target.createStatement()) {
            //the rows are copied as they are, in any order (also rows whose reference was deleted in the old database)
            s.execute("SET REFERENTIAL_INTEGRITY FALSE");
        }
        try {
            target.setAutoCommit(false);
            for (String t : common) {
                try (Statement s = target.createStatement()) {
                    s.executeUpdate("DELETE FROM " + targetTables.get(t));
                }
            }
            long done = 0;
            for (String t : common) {
                TableResult tr = new TableResult(t);
                tr.oldRows = counts.get(t);
                r.tables.put(t, tr);
                if (t.equals(SEARCHINDEX)) {
                    continue;
                }
                if (t.equals(TABLELOCK)) {
                    if (tr.oldRows > 0) {
                        tr.notes.add(Messages.Y132_LOCKS_SKIPPED.toString());
                    }
                    continue;
                }
                final long before = done;
                final long all = Math.max(1, total);
                copyTable(source, sourceTables.get(t), target, targetTables.get(t), tr, r,
                        n -> progress.update(String.format(Messages.Y132_IMPORTING.toString(), t.toLowerCase(Locale.ROOT), n, tr.oldRows),
                                (int) (5 + 90 * (before + n) / all)));
                done += tr.oldRows;
            }
            progress.update(Messages.Y132_FINISHING.toString(), 96);
            kept.restore(target, targetTables, r);
            //the version of this YaBS stays
            try (PreparedStatement ps = target.prepareStatement("UPDATE " + targetTables.get("GLOBALSETTINGS")
                    + " SET value = ? WHERE cname = 'yabs_dbversion'")) {
                ps.setString(1, kept.version);
                ps.executeUpdate();
            }
            if (r.tables.containsKey(SEARCHINDEX)) {
                TableResult si = r.tables.get(SEARCHINDEX);
                r.searchIndex = count(target, targetTables.get(SEARCHINDEX));
                si.imported = 0;
                si.notes.add(String.format(Messages.Y132_INDEX_REBUILT.toString(), r.searchIndex));
            }
            target.commit();
        } catch (Exception e) {
            try {
                target.rollback();
            } catch (SQLException ex) {
                Log.Debug(Yabs132Import.class, ex);
            }
            throw e;
        } finally {
            try (Statement s = target.createStatement()) {
                s.execute("SET REFERENTIAL_INTEGRITY TRUE");
            }
            target.setAutoCommit(auto);
        }
        //new rows get ids after the imported ones
        for (String t : common) {
            restartIdentity(target, targetSchema, targetTables.get(t));
        }
    }

    /**
     * @return schema and table prefix of the YaBS tables, null if none
     */
    private static String[] findSchema(Connection c) throws SQLException {
        String[] best = null;
        int bestTables = -1;
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(
                "SELECT s.SCHEMANAME, t.TABLENAME FROM SYS.SYSTABLES t, SYS.SYSSCHEMAS s "
                + "WHERE t.SCHEMAID = s.SCHEMAID AND t.TABLETYPE = 'T' AND t.TABLENAME LIKE '%GLOBALSETTINGS'")) {
            List<String[]> l = new ArrayList<String[]>();
            while (rs.next()) {
                String name = rs.getString(2);
                l.add(new String[]{rs.getString(1), name.substring(0, name.length() - "GLOBALSETTINGS".length())});
            }
            for (String[] x : l) {
                int n = tables(c, x[0], x[1]).size();
                if (n > bestTables) {
                    best = x;
                    bestTables = n;
                }
            }
        }
        return best;
    }

    private static double version(Connection c, String prefix) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(
                "SELECT value FROM " + prefix + "globalsettings WHERE cname = 'yabs_dbversion'")) {
            if (rs.next()) {
                try {
                    return Double.parseDouble(rs.getString(1).trim());
                } catch (Exception e) {
                    Log.Debug(Yabs132Import.class, "Version " + rs.getString(1) + ": " + e.getMessage());
                }
            }
        }
        return Constants.DATABASE_ORIGINAL_VERSION;
    }

    /**
     * The Derby updates up to 1.3.2 on the copy; if one fails, the columns
     * are still matched by name (and reported if they can not)
     */
    private void updateCopy(Connection source, File copy, Result r, String prefix) {
        if (!prefix.isEmpty()) {
            r.notes.add(String.format(Messages.Y132_UPDATE_FAILED.toString(), Double.toString(r.oldVersion), "table prefix " + prefix));
            return;
        }
        Map<Double, String[]> updates = new TreeMap<Double, String[]>(
                new DatabaseUpdater().getStructureForMigration(ConnectionTypeHandler.DERBY)).headMap(VERSION_132, true);
        final Connection[] con = {source};
        try {
            double v = new SchemaUpdate(reconnect -> {
                if (reconnect) {
                    Connection c = DriverManager.getConnection("jdbc:derby:" + copy.getAbsolutePath());
                    try (Statement s = c.createStatement()) {
                        s.execute("SET SCHEMA \"" + r.schema.replace("\"", "\"\"") + "\"");
                    }
                    con[0] = c;
                }
                return con[0];
            }, true, null).run(r.oldVersion, updates);
            r.notes.add(String.format(Messages.Y132_UPDATED.toString(), Double.toString(r.oldVersion), Double.toString(v)));
        } catch (SQLException e) {
            Log.Debug(Yabs132Import.class, e);
            r.notes.add(String.format(Messages.Y132_UPDATE_FAILED.toString(), Double.toString(r.oldVersion), e.getMessage()));
        }
        if (con[0] != source) {
            try {
                con[0].close();
            } catch (SQLException e) {
                Log.Debug(Yabs132Import.class, e);
            }
        }
    }

    /**
     * Column of a table
     */
    private static class Column {

        final String name;
        final int type;
        final boolean nullable;
        final boolean hasDefault;

        Column(String name, int type, boolean nullable, boolean hasDefault) {
            this.name = name;
            this.type = type;
            this.nullable = nullable;
            this.hasDefault = hasDefault;
        }

        boolean isText() {
            switch (type) {
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                case Types.CLOB:
                case Types.NCHAR:
                case Types.NVARCHAR:
                case Types.LONGNVARCHAR:
                case Types.NCLOB:
                    return true;
                default:
                    return false;
            }
        }

        boolean isBinary() {
            switch (type) {
                case Types.BLOB:
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                    return true;
                default:
                    return false;
            }
        }
    }

    private interface RowCount {

        void rows(long n);
    }

    private void copyTable(Connection source, String sourceTable, Connection target, String targetTable,
            TableResult tr, Result r, RowCount count) throws SQLException {
        String t = tr.table;
        Map<String, Column> from = columns(source, sourceTable);
        Map<String, Column> to = columns(target, targetTable);
        //old column - new column
        Map<String, String> map = new LinkedHashMap<String, String>();
        for (String c : from.keySet()) {
            if (to.containsKey(c)) {
                map.put(c, c);
            }
        }
        for (String c : from.keySet()) {
            String renamed = RENAMED.get(t + "." + c);
            if (!map.containsKey(c) && renamed != null && to.containsKey(renamed) && !map.containsValue(renamed)) {
                map.put(c, renamed);
                tr.notes.add(String.format(Messages.Y132_COLUMN_RENAMED.toString(), c.toLowerCase(Locale.ROOT), renamed.toLowerCase(Locale.ROOT)));
            }
        }
        for (String c : from.keySet()) {
            if (!map.containsKey(c)) {
                long n = countNotNull(source, sourceTable, c);
                if (n > 0) {
                    r.problems.add(String.format(Messages.Y132_COLUMN_MISSING.toString(), t.toLowerCase(Locale.ROOT), c.toLowerCase(Locale.ROOT), n));
                } else {
                    tr.notes.add(String.format(Messages.Y132_COLUMN_EMPTY.toString(), c.toLowerCase(Locale.ROOT)));
                }
            }
        }
        if (map.isEmpty()) {
            return;
        }
        List<String> oldCols = new ArrayList<String>(map.keySet());
        List<String> newCols = new ArrayList<String>(map.values());
        //a NOT NULL column without default the old table does not have: the rows can not be inserted
        for (Column c : to.values()) {
            if (!newCols.contains(c.name) && !c.nullable && !c.hasDefault && tr.oldRows > 0) {
                r.problems.add(String.format(Messages.Y132_COLUMN_REQUIRED.toString(), t.toLowerCase(Locale.ROOT), c.name.toLowerCase(Locale.ROOT)));
            }
        }
        StringBuilder marks = new StringBuilder();
        for (int i = 0; i < newCols.size(); i++) {
            marks.append(i == 0 ? "?" : ", ?");
        }
        String insert = "INSERT INTO " + targetTable + " (" + quoted(newCols) + ") VALUES (" + marks + ")";
        String order = from.containsKey("IDS") ? " ORDER BY IDS" : "";
        boolean ispaid = "STATUS".equals(map.get("ISPAID")) && "REVENUES".equals(t);
        long rows = 0;
        long failed = 0;
        try (Statement s = source.createStatement();
                ResultSet rs = s.executeQuery("SELECT " + quoted(oldCols) + " FROM " + sourceTable + order);
                PreparedStatement ps = target.prepareStatement(insert)) {
            while (rs.next()) {
                Object id = null;
                for (int i = 0; i < oldCols.size(); i++) {
                    Column src = from.get(oldCols.get(i));
                    Column dst = to.get(newCols.get(i));
                    Object v;
                    if (src.isText()) {
                        String text = rs.getString(i + 1);
                        if (text != null) {
                            if (text.indexOf('<') >= 0 && text.contains(BACKSLASH)) {
                                text = text.replace(BACKSLASH, "\\");
                                r.backslashes++;
                            }
                            if (convertBackticks && text.indexOf('`') >= 0 && !keepBackticks(t, src.name)) {
                                text = text.replace('`', '\'');
                                r.backticks++;
                            }
                        }
                        v = text;
                    } else if (src.isBinary()) {
                        v = rs.getBytes(i + 1);
                    } else {
                        v = rs.getObject(i + 1);
                    }
                    if (ispaid && "ISPAID".equals(src.name) && v != null && ((Number) v).intValue() == 1) {
                        //1.1982: paid is status 4
                        v = 4;
                    }
                    if ("IDS".equals(src.name)) {
                        id = v;
                    }
                    if (v == null) {
                        ps.setNull(i + 1, dst.type);
                    } else if (v instanceof byte[]) {
                        ps.setBytes(i + 1, (byte[]) v);
                    } else {
                        ps.setObject(i + 1, v);
                    }
                }
                try {
                    ps.executeUpdate();
                    tr.imported++;
                } catch (SQLException e) {
                    failed++;
                    if (failed <= 20) {
                        r.problems.add(String.format(Messages.Y132_ROW_FAILED.toString(), t.toLowerCase(Locale.ROOT),
                                id == null ? "?" : String.valueOf(id), e.getMessage()));
                    }
                    Log.Debug(Yabs132Import.class, t + " " + id + ": " + e.getMessage());
                }
                rows++;
                if (rows % 200 == 0) {
                    count.rows(rows);
                }
            }
        }
        if (failed > 20) {
            r.problems.add(String.format(Messages.Y132_ROWS_FAILED.toString(), t.toLowerCase(Locale.ROOT), failed));
        }
        count.rows(rows);
    }

    /**
     * Data of the new database that stays: its templates (with their files),
     * the countries and global settings the old database does not have
     */
    private static class Kept {

        String version = String.valueOf(Constants.DATABASE_VERSION);
        final List<Map<String, Object>> settings = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> countries = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> templates = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> files = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> assignments = new ArrayList<Map<String, Object>>();

        Kept(Connection c, Map<String, String> tables) throws SQLException {
            if (tables.containsKey("GLOBALSETTINGS")) {
                settings.addAll(rows(c, tables.get("GLOBALSETTINGS"), null));
                for (Map<String, Object> m : settings) {
                    if ("yabs_dbversion".equals(m.get("CNAME"))) {
                        version = String.valueOf(m.get("VALUE"));
                    }
                }
            }
            if (tables.containsKey("COUNTRIES")) {
                countries.addAll(rows(c, tables.get("COUNTRIES"), null));
            }
            if (tables.containsKey("TEMPLATES") && tables.containsKey("FILES") && tables.containsKey("TEMPLATESTOUSERS")
                    && tables.containsKey("USERS")) {
                templates.addAll(rows(c, tables.get("TEMPLATES"), null));
                files.addAll(rows(c, tables.get("FILES"), "cname IN (SELECT filename FROM " + tables.get("TEMPLATES") + ")"));
                assignments.addAll(rows(c, tables.get("TEMPLATESTOUSERS"), null));
            }
        }

        void restore(Connection c, Map<String, String> tables, Result r) throws SQLException {
            //settings the old database does not have (e.g. of newer functions)
            int n = 0;
            if (!settings.isEmpty()) {
                Set<String> have = keys(c, tables.get("GLOBALSETTINGS"), "cname", "groupsids");
                for (Map<String, Object> m : settings) {
                    if (!have.contains(m.get("CNAME") + "|" + m.get("GROUPSIDS"))) {
                        insert(c, tables.get("GLOBALSETTINGS"), m);
                        n++;
                    }
                }
            }
            if (n > 0) {
                r.notes.add(String.format(Messages.Y132_KEPT_SETTINGS.toString(), n));
            }
            //countries the old database does not have
            n = 0;
            if (!countries.isEmpty()) {
                Set<String> have = keys(c, tables.get("COUNTRIES"), "iso");
                long id = max(c, tables.get("COUNTRIES"));
                for (Map<String, Object> m : countries) {
                    if (!have.contains(String.valueOf(m.get("ISO")))) {
                        m.put("IDS", ++id);
                        insert(c, tables.get("COUNTRIES"), m);
                        n++;
                    }
                }
            }
            if (n > 0) {
                r.notes.add(String.format(Messages.Y132_KEPT_COUNTRIES.toString(), n));
            }
            //the templates of the new database stay as additional templates
            if (templates.isEmpty()) {
                return;
            }
            long oldAssignments = count(c, tables.get("TEMPLATESTOUSERS"));
            Set<String> fileNames = keys(c, tables.get("FILES"), "cname");
            long fileId = max(c, tables.get("FILES"));
            Set<String> written = new HashSet<String>();
            for (Map<String, Object> f : files) {
                String name = String.valueOf(f.get("CNAME"));
                if (fileNames.contains(name)) {
                    //same (random) file name in the old database: the template keeps the old file
                    continue;
                }
                f.put("IDS", ++fileId);
                insert(c, tables.get("FILES"), f);
                written.add(name);
            }
            long templateId = max(c, tables.get("TEMPLATES"));
            Map<Object, Long> ids = new HashMap<Object, Long>();
            for (Map<String, Object> t : templates) {
                if (!written.contains(String.valueOf(t.get("FILENAME")))) {
                    continue;
                }
                ids.put(t.get("IDS"), ++templateId);
                t.put("IDS", templateId);
                insert(c, tables.get("TEMPLATES"), t);
            }
            //assigned only if the old database has no assigned templates at all (else they are just available)
            int assigned = 0;
            if (oldAssignments == 0 && !assignments.isEmpty()) {
                Set<String> users = keys(c, tables.get("USERS"), "ids");
                long id = max(c, tables.get("TEMPLATESTOUSERS"));
                for (Map<String, Object> a : assignments) {
                    Long tid = ids.get(a.get("TEMPLATESIDS"));
                    if (tid == null || !users.contains(String.valueOf(a.get("USERSIDS")))) {
                        continue;
                    }
                    a.put("IDS", ++id);
                    a.put("TEMPLATESIDS", tid);
                    a.put("CNAME", tid + "@" + a.get("USERSIDS") + "@" + a.get("GROUPSIDS"));
                    insert(c, tables.get("TEMPLATESTOUSERS"), a);
                    assigned++;
                }
            }
            if (!ids.isEmpty()) {
                r.notes.add(String.format(assigned > 0 ? Messages.Y132_KEPT_TEMPLATES_ASSIGNED.toString()
                        : Messages.Y132_KEPT_TEMPLATES.toString(), ids.size()));
            }
        }
    }

    private static List<Map<String, Object>> rows(Connection c, String table, String where) throws SQLException {
        List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM " + table + (where == null ? "" : " WHERE " + where))) {
            int n = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<String, Object>();
                for (int i = 1; i <= n; i++) {
                    int type = rs.getMetaData().getColumnType(i);
                    m.put(rs.getMetaData().getColumnName(i).toUpperCase(Locale.ROOT),
                            type == Types.BLOB || type == Types.VARBINARY || type == Types.LONGVARBINARY || type == Types.BINARY
                            ? rs.getBytes(i) : rs.getObject(i));
                }
                l.add(m);
            }
        }
        return l;
    }

    private static void insert(Connection c, String table, Map<String, Object> row) throws SQLException {
        List<String> cols = new ArrayList<String>(row.keySet());
        StringBuilder marks = new StringBuilder();
        for (int i = 0; i < cols.size(); i++) {
            marks.append(i == 0 ? "?" : ", ?");
        }
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO " + table + " (" + quoted(cols) + ") VALUES (" + marks + ")")) {
            for (int i = 0; i < cols.size(); i++) {
                Object v = row.get(cols.get(i));
                if (v instanceof byte[]) {
                    ps.setBytes(i + 1, (byte[]) v);
                } else {
                    ps.setObject(i + 1, v);
                }
            }
            ps.executeUpdate();
        }
    }

    private static Set<String> keys(Connection c, String table, String... cols) throws SQLException {
        Set<String> s = new HashSet<String>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT " + String.join(", ", cols) + " FROM " + table)) {
            while (rs.next()) {
                StringBuilder b = new StringBuilder();
                for (int i = 1; i <= cols.length; i++) {
                    b.append(i > 1 ? "|" : "").append(rs.getString(i));
                }
                s.add(b.toString());
            }
        }
        return s;
    }

    private static long max(Connection c, String table) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT MAX(ids) FROM " + table)) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    static long count(Connection c, String table) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    private static long countNotNull(Connection c, String table, String column) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(
                "SELECT COUNT(*) FROM " + table + " WHERE \"" + column + "\" IS NOT NULL")) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    private static String quoted(List<String> cols) {
        StringBuilder b = new StringBuilder();
        for (String c : cols) {
            b.append(b.length() == 0 ? "" : ", ").append('"').append(c).append('"');
        }
        return b.toString();
    }

    /**
     * @return TABLE (upper case, without prefix) - name to use in SQL
     */
    private static Map<String, String> tables(Connection c, String schema, String prefix) throws SQLException {
        Map<String, String> m = new TreeMap<String, String>();
        try (ResultSet rs = c.getMetaData().getTables(null, schema, "%", new String[]{"TABLE", "BASE TABLE"})) {
            while (rs.next()) {
                String name = rs.getString("TABLE_NAME");
                String upper = name.toUpperCase(Locale.ROOT);
                String p = prefix.toUpperCase(Locale.ROOT);
                if (!upper.startsWith(p)) {
                    continue;
                }
                m.put(upper.substring(p.length()), "\"" + name.replace("\"", "\"\"") + "\"");
            }
        }
        return m;
    }

    /**
     * @return column name (upper case) - column
     */
    private static Map<String, Column> columns(Connection c, String quotedTable) throws SQLException {
        String table = quotedTable.substring(1, quotedTable.length() - 1).replace("\"\"", "\"");
        Map<String, Column> m = new LinkedHashMap<String, Column>();
        try (ResultSet rs = c.getMetaData().getColumns(null, c.getSchema(), table, "%")) {
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                String def = rs.getString("COLUMN_DEF");
                boolean auto = "YES".equals(rs.getString("IS_AUTOINCREMENT"));
                m.put(name.toUpperCase(Locale.ROOT), new Column(name.toUpperCase(Locale.ROOT),
                        rs.getInt("DATA_TYPE"), rs.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls, auto || def != null));
            }
        }
        return m;
    }

    /**
     * @return the tables, referenced tables first
     */
    private static List<String> orderByReferences(Connection c, String schema, List<String> tables) throws SQLException {
        Map<String, Set<String>> refs = new LinkedHashMap<String, Set<String>>();
        DatabaseMetaData meta = c.getMetaData();
        for (String t : tables) {
            Set<String> r = new LinkedHashSet<String>();
            try (ResultSet rs = meta.getImportedKeys(null, schema, t)) {
                while (rs.next()) {
                    String p = rs.getString("PKTABLE_NAME").toUpperCase(Locale.ROOT);
                    if (!p.equals(t) && tables.contains(p)) {
                        r.add(p);
                    }
                }
            }
            refs.put(t, r);
        }
        List<String> ordered = new ArrayList<String>();
        while (ordered.size() < tables.size()) {
            boolean progress = false;
            for (String t : tables) {
                if (!ordered.contains(t) && ordered.containsAll(refs.get(t))) {
                    ordered.add(t);
                    progress = true;
                }
            }
            if (!progress) {
                for (String t : tables) {
                    if (!ordered.contains(t)) {
                        ordered.add(t);
                    }
                }
            }
        }
        return ordered;
    }

    private static void restartIdentity(Connection c, String schema, String quotedTable) throws SQLException {
        String table = quotedTable.substring(1, quotedTable.length() - 1).replace("\"\"", "\"");
        String identity = null;
        try (ResultSet rs = c.getMetaData().getColumns(null, schema, table, "%")) {
            while (rs.next()) {
                if ("YES".equals(rs.getString("IS_AUTOINCREMENT"))) {
                    identity = rs.getString("COLUMN_NAME");
                }
            }
        }
        if (identity == null) {
            return;
        }
        long max;
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT MAX(\"" + identity + "\") FROM " + quotedTable)) {
            max = rs.next() ? rs.getLong(1) : 0;
        }
        try (Statement s = c.createStatement()) {
            s.execute("ALTER TABLE " + quotedTable + " ALTER COLUMN \"" + identity + "\" RESTART WITH " + (max + 1));
        }
    }

    /**
     * Copy the database folder; the lock files of a running Derby are left
     * out (the copy is recovered like after a crash)
     */
    static void copyDatabase(Path from, Path to) throws IOException {
        Files.walkFileTree(from, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(to.resolve(from.relativize(dir).toString()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String name = file.getFileName().toString();
                if (!name.equals("db.lck") && !name.equals("dbex.lck")) {
                    Files.copy(file, to.resolve(from.relativize(file).toString()), StandardCopyOption.COPY_ATTRIBUTES);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void shutdown(File db) {
        try {
            DriverManager.getConnection("jdbc:derby:" + db.getAbsolutePath() + ";shutdown=true");
        } catch (SQLException e) {
            //08006 = shut down
            Log.Debug(Yabs132Import.class, "Shutdown copy: " + e.getMessage());
        }
    }

    private static void delete(Path dir) {
        try (Stream<Path> files = Files.walk(dir)) {
            files.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException e) {
            Log.Debug(Yabs132Import.class, "Could not delete " + dir + ": " + e.getMessage());
        }
    }
}
