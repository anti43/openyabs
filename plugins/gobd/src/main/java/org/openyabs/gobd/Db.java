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
package org.openyabs.gobd;

import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The database of YaBS for the GoBD tables: prepared statements, errors as
 * exceptions (never swallowed) and transactions. The query methods of YaBS
 * log and swallow errors; the plugin must not take a failed write for a lock
 * or a failed read for "not locked".
 */
final class Db {

    /**
     * Work in a transaction
     *
     * @param <T>
     */
    interface Work<T> {

        T run(Connection c) throws Exception;
    }

    private Db() {
    }

    /**
     * @return the connection of YaBS
     * @throws SQLException if there is none
     */
    static Connection connection() throws SQLException {
        mpv5.db.common.DatabaseConnection dc = mpv5.db.common.DatabaseConnection.getConnector();
        Connection c = dc == null ? null : dc.getConnection();
        if (c == null) {
            throw new SQLException("Keine Datenbankverbindung");
        }
        return c;
    }

    /**
     * @param c
     * @param sql with ? for the parameters
     * @param params
     * @return the rows (CLOBs as text)
     * @throws SQLException
     */
    static Object[][] query(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                int n = rs.getMetaData().getColumnCount();
                List<Object[]> rows = new ArrayList<Object[]>();
                while (rs.next()) {
                    Object[] row = new Object[n];
                    for (int i = 0; i < n; i++) {
                        row[i] = value(rs.getObject(i + 1));
                    }
                    rows.add(row);
                }
                return rows.toArray(new Object[0][]);
            }
        }
    }

    /**
     * @return the first row, null if none
     */
    static Object[] first(Connection c, String sql, Object... params) throws SQLException {
        Object[][] r = query(c, sql, params);
        return r.length > 0 ? r[0] : null;
    }

    /**
     * @param c
     * @param sql with ? for the parameters
     * @param params
     * @return the number of changed rows
     * @throws SQLException
     */
    static int update(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    /**
     * Runs the work in a transaction: commit if it returns, rollback if it
     * throws. Inside a running transaction it is part of that one. The
     * connection is the one of YaBS: the transaction runs on the Swing thread
     * (as the queries of YaBS), so no other query of YaBS ends up in it.
     *
     * @param <T>
     * @param work
     * @return what the work returns
     * @throws Exception what the work throws
     */
    static <T> T tx(Work<T> work) throws Exception {
        if (!java.awt.EventQueue.isDispatchThread()) {
            Object[] result = new Object[1];
            Exception[] failure = new Exception[1];
            java.awt.EventQueue.invokeAndWait(() -> {
                try {
                    result[0] = tx(work);
                } catch (Exception e) {
                    failure[0] = e;
                }
            });
            if (failure[0] != null) {
                throw failure[0];
            }
            @SuppressWarnings("unchecked")
            T t = (T) result[0];
            return t;
        }
        Connection c = connection();
        synchronized (c) {
            if (!c.getAutoCommit()) {
                return work.run(c);
            }
            c.setAutoCommit(false);
            try {
                T result = work.run(c);
                c.commit();
                return result;
            } catch (Exception | Error e) {
                try {
                    c.rollback();
                } catch (SQLException rollbackFailed) {
                    e.addSuppressed(rollbackFailed);
                }
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /**
     * @param e
     * @return true for a violated unique key (another client was faster)
     */
    static boolean isUniqueViolation(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SQLException && "23505".equals(((SQLException) t).getSQLState())) {
                return true;
            }
        }
        return false;
    }

    private static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            if (p instanceof Date && !(p instanceof java.sql.Date) && !(p instanceof Timestamp)) {
                p = new Timestamp(((Date) p).getTime());
            }
            ps.setObject(i + 1, p);
        }
    }

    private static Object value(Object o) throws SQLException {
        if (o instanceof Clob) {
            Clob clob = (Clob) o;
            try (Reader r = clob.getCharacterStream()) {
                StringBuilder b = new StringBuilder();
                char[] buf = new char[8192];
                int n;
                while ((n = r.read(buf)) > 0) {
                    b.append(buf, 0, n);
                }
                return b.toString();
            } catch (java.io.IOException e) {
                throw new SQLException(e);
            }
        }
        return o;
    }
}
