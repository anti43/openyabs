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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.function.UnaryOperator;
import mpv5.logging.Log;

/**
 * Runs the schema updates of {@link DatabaseUpdater}, one statement at a time.
 * <ul>
 * <li>An update that was applied before (partly) can run again: errors like
 * "column already exists" or "is not a column" are logged and skipped.</li>
 * <li>The database version is saved after every update step, so a failed run
 * continues where it stopped.</li>
 * <li>Any other error stops the update and is reported - the version is not
 * raised past the failed step.</li>
 * <li>A damaged Derby catalog (missing stored statements of triggers, internal
 * Derby errors) is reported as {@link DamagedDatabaseException}.</li>
 * </ul>
 */
public class SchemaUpdate {

    /**
     * Provides the connection; reconnect after Derby closed it
     */
    public interface ConnectionSource {

        Connection get(boolean reconnect) throws SQLException;
    }

    /**
     * An update statement failed
     */
    public static class UpdateException extends SQLException {

        private static final long serialVersionUID = 1L;
        private final double version;
        private final String statement;

        public UpdateException(double version, String statement, SQLException cause) {
            super(message(version, statement, cause), cause.getSQLState(), cause.getErrorCode(), cause);
            this.version = version;
            this.statement = statement;
        }

        private static String message(double version, String statement, SQLException cause) {
            return "Database update " + version + " failed: " + cause.getMessage()
                    + " [" + cause.getSQLState() + "]\n" + statement;
        }

        public double getVersion() {
            return version;
        }

        public String getStatement() {
            return statement;
        }
    }

    /**
     * The Derby catalog is damaged (e.g. triggers whose stored statements are
     * missing); the database can not be updated in place
     */
    public static class DamagedDatabaseException extends UpdateException {

        private static final long serialVersionUID = 1L;

        public DamagedDatabaseException(double version, String statement, SQLException cause) {
            super(version, statement, cause);
        }
    }

    enum Outcome {
        /**
         * the change is already in the database
         */
        ALREADY_DONE,
        /**
         * the connection was closed, retry with a new one
         */
        CONNECTION_LOST,
        /**
         * the Derby catalog is damaged
         */
        DAMAGED,
        /**
         * a real error
         */
        FAILED
    }

    private final ConnectionSource connections;
    private final boolean derby;
    private final UnaryOperator<String> prefix;

    /**
     * @param connections
     * @param derby true for Derby, false for MySQL
     * @param prefix applies the table prefix to a statement
     */
    public SchemaUpdate(ConnectionSource connections, boolean derby, UnaryOperator<String> prefix) {
        this.connections = connections;
        this.derby = derby;
        this.prefix = prefix == null ? UnaryOperator.identity() : prefix;
    }

    /**
     * Run all updates newer than the given version
     *
     * @param from the current database version
     * @param updates version - statements
     * @return the new version
     * @throws SQLException {@link UpdateException} or
     * {@link DamagedDatabaseException} if an update failed
     */
    public double run(double from, Map<Double, String[]> updates) throws SQLException {
        double version = from;
        for (Map.Entry<Double, String[]> e : updates.entrySet()) {
            if (e.getKey() <= from) {
                continue;
            }
            for (String sql : e.getValue()) {
                execute(e.getKey(), prefix.apply(sql));
            }
            version = e.getKey();
            saveVersion(version);
        }
        return version;
    }

    /**
     * Run the statements, e.g. the structure of a new database; nothing may
     * fail
     */
    public void runAll(String[] statements) throws SQLException {
        for (String sql : statements) {
            try (Statement s = connections.get(false).createStatement()) {
                s.execute(prefix.apply(sql));
            }
        }
    }

    private void execute(double version, String sql) throws SQLException {
        Log.Debug(SchemaUpdate.class, "Update " + version + ": " + sql);
        for (int attempt = 0;; attempt++) {
            try {
                Connection c = connections.get(attempt > 0);
                try (Statement s = c.createStatement()) {
                    s.execute(sql);
                }
                return;
            } catch (SQLException ex) {
                Outcome o = classify(ex, derby);
                switch (o) {
                    case ALREADY_DONE:
                        Log.Debug(SchemaUpdate.class, "Already done: " + ex.getMessage());
                        return;
                    case CONNECTION_LOST:
                        if (attempt == 0) {
                            Log.Debug(SchemaUpdate.class, "Connection lost, reconnecting: " + ex.getMessage());
                            continue;
                        }
                        throw new UpdateException(version, sql, ex);
                    case DAMAGED:
                        throw new DamagedDatabaseException(version, sql, ex);
                    default:
                        throw new UpdateException(version, sql, ex);
                }
            }
        }
    }

    private void saveVersion(double version) throws SQLException {
        String sql = prefix.apply("UPDATE globalsettings SET value ='" + version + "' WHERE cname = 'yabs_dbversion'");
        try (Statement s = connections.get(false).createStatement()) {
            s.execute(sql);
        } catch (SQLException ex) {
            throw new UpdateException(version, sql, ex);
        }
    }

    /**
     * @param ex
     * @param derby
     * @return what the error means for the update
     */
    static Outcome classify(SQLException ex, boolean derby) {
        Outcome result = null;
        for (SQLException e = ex; e != null; e = e.getNextException()) {
            Outcome o = classifyOne(e, derby);
            if (o == Outcome.DAMAGED) {
                return o;
            }
            if (result == null || (result == Outcome.ALREADY_DONE && o != Outcome.ALREADY_DONE)) {
                result = o;
            }
        }
        return result;
    }

    private static Outcome classifyOne(SQLException e, boolean derby) {
        String state = e.getSQLState() == null ? "" : e.getSQLState();
        String msg = e.getMessage() == null ? "" : e.getMessage();
        if (derby) {
            if (state.startsWith("XJ001") || msg.contains("StoredPreparedStatement")) {
                //internal Java exception (e.g. NullPointerException) or missing stored
                //statement of a trigger: Derby closes the connection, the catalog is damaged
                return Outcome.DAMAGED;
            }
            if (state.startsWith("08")) {
                return Outcome.CONNECTION_LOST;
            }
            switch (state.length() >= 5 ? state.substring(0, 5) : state) {
                case "X0Y32"://already exists
                case "42X14"://is not a column in table
                case "42X04"://column not found (renamed or dropped before)
                case "42Y55"://does not exist (drop table)
                case "42X94"://does not exist (trigger, index)
                case "X0X81"://does not exist
                //H2
                case "42S21"://duplicate column
                case "42S01"://table exists
                case "42S11"://index exists
                case "42S22"://column not found
                case "42S02"://table not found
                case "42S12"://index not found
                case "90057"://constraint not found
                case "90042"://trigger not found
                case "90041"://trigger exists
                case "90045"://constraint exists
                case "42X86"://there is no constraint
                    return Outcome.ALREADY_DONE;
                default:
                    return Outcome.FAILED;
            }
        }
        if (state.startsWith("08")) {
            return Outcome.CONNECTION_LOST;
        }
        switch (e.getErrorCode()) {
            case 1050://table already exists
            case 1060://duplicate column
            case 1061://duplicate key name
            case 1091://can't drop, check that it exists
            case 1054://unknown column
            case 1146://table doesn't exist
            case 1360://trigger does not exist
            case 1359://trigger already exists
                return Outcome.ALREADY_DONE;
            default:
                return Outcome.FAILED;
        }
    }
}
