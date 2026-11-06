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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import mpv5.logging.Log;

/**
 * Rebuilds an embedded Derby database whose catalog is damaged (e.g. triggers
 * whose stored statements are missing - Derby then fails on every ALTER TABLE
 * and closes the connection on DROP TRIGGER).
 * <p>
 * A new database is created with the current YaBS structure, all data is
 * copied into it and the directories are swapped. The old database is kept
 * as backup next to it ({@code <name>-backup-<date>}); nothing is changed in
 * the old database. If anything fails, the new database is deleted and the
 * old one stays in place.
 */
public class DerbyRebuild {

    /**
     * filled by the triggers on insert - copied last
     */
    private static final String SEARCHINDEX = "SEARCHINDEX";
    private static final int COMMIT_EVERY = 500;
    private static final String VERSION_ROW = "INSERT INTO globalsettings (cname, groupsids, value) VALUES ('yabs_dbversion', 1, '"
            + mpv5.globals.Constants.DATABASE_ORIGINAL_VERSION + "')";
    private final File database;
    private final String user;
    private final String password;
    private final Map<Double, String[]> updates;
    private final String[] structure;
    private final String[] initialData;
    private final java.util.function.UnaryOperator<String> prefix;
    private File backup;

    /**
     * @param database the database directory (Derby URL path + db name)
     * @param user may be null
     * @param password may be null
     * @param structure the initial structure ({@link DatabaseInstallation})
     * @param initialData the data of a new installation, the updates need it
     * ({@link DatabaseInstallation#INITIAL_DATA}); replaced by the copied data
     * @param updates the updates ({@link DatabaseUpdater})
     * @param prefix applies the table prefix to a statement, may be null
     */
    public DerbyRebuild(File database, String user, String password, String[] structure, String[] initialData,
            Map<Double, String[]> updates,
            java.util.function.UnaryOperator<String> prefix) {
        this.database = database.getAbsoluteFile();
        this.user = user;
        this.password = password;
        this.structure = structure;
        this.initialData = initialData;
        this.updates = updates;
        this.prefix = prefix;
    }

    /**
     * @return the directory the old database was moved to (after
     * {@link #run()})
     */
    public File getBackup() {
        return backup;
    }

    /**
     * Rebuild the database. The database must not be in use (it is shut
     * down).
     *
     * @return the number of copied rows
     * @throws Exception if the rebuild failed - the old database is unchanged
     */
    public long run() throws Exception {
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        File target = new File(database.getParentFile(), database.getName() + "-rebuild-" + stamp);
        backup = new File(database.getParentFile(), database.getName() + "-backup-" + stamp);
        shutdown(database);
        long rows;
        try {
            rows = build(target);
        } catch (Exception e) {
            shutdown(target);
            delete(target);
            shutdown(database);
            throw e;
        }
        shutdown(target);
        shutdown(database);
        Files.move(database.toPath(), backup.toPath());
        try {
            Files.move(target.toPath(), database.toPath());
        } catch (IOException e) {
            Files.move(backup.toPath(), database.toPath());
            throw e;
        }
        Log.Debug(DerbyRebuild.class, "Database rebuilt, " + rows + " rows copied, old database kept in " + backup);
        return rows;
    }

    private long build(File target) throws Exception {
        try (Connection from = connect(database, false); Connection to = connect(target, true)) {
            from.setReadOnly(true);
            to.setAutoCommit(true);
            //structure of a new installation, then all updates
            SchemaUpdate schema = new SchemaUpdate(r -> to, true, prefix);
            schema.runAll(structure);
            schema.runAll(initialData);
            try (Statement s = to.createStatement()) {
                String sql = "UPDATE globalsettings SET value ='" + mpv5.globals.Constants.DATABASE_ORIGINAL_VERSION
                        + "' WHERE cname = 'yabs_dbversion'";
                if (s.executeUpdate(prefix == null ? sql : prefix.apply(sql)) == 0) {
                    s.execute(prefix == null ? VERSION_ROW : prefix.apply(VERSION_ROW));
                }
            }
            double version = schema.run(mpv5.globals.Constants.DATABASE_ORIGINAL_VERSION, updates);

            List<String> tables = tables(to);
            Set<String> old = new LinkedHashSet<String>(tables(from));
            clear(to, tables);

            to.setAutoCommit(false);
            List<Row> deferred = new ArrayList<Row>();
            long rows = 0;
            for (String t : tables) {
                if (!isSearchIndex(t) && old.contains(t)) {
                    rows += copy(from, to, t, deferred);
                }
            }
            rows += retry(to, deferred);
            //rows created by the indexer triggers are replaced by the original index
            for (String t : tables) {
                if (isSearchIndex(t)) {
                    try (Statement s = to.createStatement()) {
                        s.executeUpdate("DELETE FROM " + t);
                    }
                    if (old.contains(t)) {
                        rows += copy(from, to, t, deferred);
                        rows += retry(to, deferred);
                    }
                }
            }
            to.commit();
            to.setAutoCommit(true);
            for (String t : tables) {
                restartIdentity(to, t);
            }
            try (Statement s = to.createStatement()) {
                String sql = "UPDATE globalsettings SET value ='" + version + "' WHERE cname = 'yabs_dbversion'";
                s.executeUpdate(prefix == null ? sql : prefix.apply(sql));
            }
            return rows;
        }
    }

    private static boolean isSearchIndex(String table) {
        return table.endsWith(SEARCHINDEX);
    }

    private Connection connect(File db, boolean create) throws SQLException {
        String url = "jdbc:derby:" + db.getPath() + (create ? ";create=true" : "");
        return user == null ? DriverManager.getConnection(url) : DriverManager.getConnection(url, user, password);
    }

    /**
     * Shut down one embedded database; nothing happens if it is not booted
     */
    static void shutdown(File db) {
        try {
            DriverManager.getConnection("jdbc:derby:" + db.getAbsolutePath() + ";shutdown=true");
        } catch (SQLException e) {
            //08006 = shut down, XJ004 = not found/not booted
            Log.Debug(DerbyRebuild.class, "Shutdown " + db.getName() + ": " + e.getMessage());
        }
    }

    private static List<String> tables(Connection c) throws SQLException {
        List<String> list = new ArrayList<String>();
        try (ResultSet rs = c.getMetaData().getTables(null, c.getSchema(), "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                list.add(rs.getString("TABLE_NAME").toUpperCase(Locale.ROOT));
            }
        }
        return orderByReferences(c, list);
    }

    /**
     * @return the tables, referenced tables first
     */
    private static List<String> orderByReferences(Connection c, List<String> tables) throws SQLException {
        Map<String, Set<String>> refs = new LinkedHashMap<String, Set<String>>();
        DatabaseMetaData meta = c.getMetaData();
        for (String t : tables) {
            Set<String> r = new LinkedHashSet<String>();
            try (ResultSet rs = meta.getImportedKeys(null, c.getSchema(), t)) {
                while (rs.next()) {
                    String p = rs.getString("PKTABLE_NAME").toUpperCase(Locale.ROOT);
                    if (!p.equals(t)) {
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
                //cycle: the remaining rows are retried until their references exist
                for (String t : tables) {
                    if (!ordered.contains(t)) {
                        ordered.add(t);
                    }
                }
            }
        }
        return ordered;
    }

    /**
     * Delete the rows the updates inserted into the new database
     */
    private static void clear(Connection c, List<String> tables) throws SQLException {
        List<String> left = new ArrayList<String>(tables);
        for (int pass = 0; pass <= tables.size() && !left.isEmpty(); pass++) {
            for (String t : new ArrayList<String>(left)) {
                try (Statement s = c.createStatement()) {
                    s.executeUpdate("DELETE FROM " + t);
                    left.remove(t);
                } catch (SQLException e) {
                    //still referenced, next pass
                }
            }
        }
        if (!left.isEmpty()) {
            throw new SQLException("Could not empty the new tables " + left);
        }
    }

    private static class Row {

        final String table;
        final String insert;
        final int[] types;
        final Object[] values;

        Row(String table, String insert, int[] types, Object[] values) {
            this.table = table;
            this.insert = insert;
            this.types = types;
            this.values = values;
        }
    }

    private long copy(Connection from, Connection to, String table, List<Row> deferred) throws SQLException {
        Map<String, Integer> target = columns(to, table);
        Map<String, Integer> source = columns(from, table);
        List<String> cols = new ArrayList<String>();
        for (String col : target.keySet()) {
            if (source.containsKey(col)) {
                cols.add(col);
            }
        }
        if (cols.isEmpty()) {
            return 0;
        }
        String identity = identityColumn(to, table);
        if (identity != null && cols.contains(identity)) {
            try (Statement s = to.createStatement()) {
                //GENERATED ALWAYS does not accept the old ids
                s.execute("ALTER TABLE " + table + " ALTER COLUMN " + identity + " SET GENERATED BY DEFAULT");
            } catch (SQLException e) {
                Log.Debug(DerbyRebuild.class, table + ": " + e.getMessage());
            }
        }
        String list = String.join(", ", cols);
        StringBuilder marks = new StringBuilder();
        for (int i = 0; i < cols.size(); i++) {
            marks.append(i == 0 ? "?" : ", ?");
        }
        String insert = "INSERT INTO " + table + " (" + list + ") VALUES (" + marks + ")";
        int[] types = new int[cols.size()];
        for (int i = 0; i < cols.size(); i++) {
            types[i] = target.get(cols.get(i));
        }
        String order = identity != null && cols.contains(identity) ? " ORDER BY " + identity : "";
        long count = 0;
        try (Statement s = from.createStatement();
                ResultSet rs = s.executeQuery("SELECT " + list + " FROM " + table + order);
                PreparedStatement ps = to.prepareStatement(insert)) {
            while (rs.next()) {
                Object[] values = new Object[cols.size()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = read(rs, i + 1, types[i]);
                }
                if (insert(ps, types, values, table)) {
                    count++;
                } else {
                    deferred.add(new Row(table, insert, types, values));
                }
                if (count % COMMIT_EVERY == 0) {
                    to.commit();
                }
            }
        }
        to.commit();
        Log.Debug(DerbyRebuild.class, "Copied " + table + ": " + count + " rows");
        return count;
    }

    /**
     * Insert rows whose referenced rows were not there yet, until nothing
     * changes
     */
    private static long retry(Connection to, List<Row> deferred) throws SQLException {
        long count = 0;
        while (!deferred.isEmpty()) {
            int before = deferred.size();
            for (Row r : new ArrayList<Row>(deferred)) {
                try (PreparedStatement ps = to.prepareStatement(r.insert)) {
                    if (insert(ps, r.types, r.values, r.table)) {
                        deferred.remove(r);
                        count++;
                    }
                }
            }
            to.commit();
            if (deferred.size() == before) {
                Row r = deferred.get(0);
                throw new SQLException(deferred.size() + " rows reference missing data, e.g. in table " + r.table
                        + ": " + java.util.Arrays.toString(r.values));
            }
        }
        return count;
    }

    /**
     * @return true if inserted, false if a referenced row is missing (yet)
     */
    private static boolean insert(PreparedStatement ps, int[] types, Object[] values, String table) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                ps.setNull(i + 1, types[i]);
            } else if (values[i] instanceof byte[]) {
                ps.setBytes(i + 1, (byte[]) values[i]);
            } else {
                ps.setObject(i + 1, values[i]);
            }
        }
        try {
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                return false;
            }
            throw new SQLException("Copying a row of " + table + " failed: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    private static Object read(ResultSet rs, int i, int type) throws SQLException {
        switch (type) {
            case Types.BLOB:
            case Types.LONGVARBINARY:
            case Types.VARBINARY:
            case Types.BINARY:
                return rs.getBytes(i);
            case Types.CLOB:
            case Types.LONGVARCHAR:
                return rs.getString(i);
            default:
                return rs.getObject(i);
        }
    }

    /**
     * @return column name - JDBC type, in table order
     */
    private static Map<String, Integer> columns(Connection c, String table) throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        try (ResultSet rs = c.getMetaData().getColumns(null, c.getSchema(), table, "%")) {
            while (rs.next()) {
                map.put(rs.getString("COLUMN_NAME").toUpperCase(Locale.ROOT), rs.getInt("DATA_TYPE"));
            }
        }
        return map;
    }

    private static String identityColumn(Connection c, String table) throws SQLException {
        try (ResultSet rs = c.getMetaData().getColumns(null, c.getSchema(), table, "%")) {
            while (rs.next()) {
                if ("YES".equals(rs.getString("IS_AUTOINCREMENT"))) {
                    return rs.getString("COLUMN_NAME").toUpperCase(Locale.ROOT);
                }
            }
        }
        return null;
    }

    /**
     * New rows get ids after the copied ones
     */
    private static void restartIdentity(Connection c, String table) throws SQLException {
        String identity = identityColumn(c, table);
        if (identity == null) {
            return;
        }
        long max = 0;
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT MAX(" + identity + ") FROM " + table)) {
            if (rs.next()) {
                max = rs.getLong(1);
            }
        }
        try (Statement s = c.createStatement()) {
            s.execute("ALTER TABLE " + table + " ALTER COLUMN " + identity + " RESTART WITH " + (max + 1));
        }
    }

    private static void delete(File dir) {
        if (!dir.exists()) {
            return;
        }
        try (Stream<Path> files = Files.walk(dir.toPath())) {
            files.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException e) {
            Log.Debug(DerbyRebuild.class, "Could not delete " + dir + ": " + e.getMessage());
        }
    }
}
