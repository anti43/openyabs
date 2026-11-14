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
package mpv5.logging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Errors of the program in the history (category "Errors"): every logged
 * exception, once per error message, with how often it happened. Stored in
 * the history table: dbidentity {@value #DBIDENTITY}, the message as name,
 * the count in reserve1, where it happened in reserve2, the date of the last
 * time as date.
 * <p>
 * Never on the calling thread: collected and written every few seconds by a
 * background thread, many of the same error at once are one update. Errors
 * while writing are not recorded again, and nothing is written while there is
 * no database connection (kept until there is one).
 */
public final class ErrorHistory {

    /**
     * The dbidentity of error entries in the history table
     */
    public static final String DBIDENTITY = "error";
    /**
     * At most this many different errors wait for the database
     */
    static final int MAX_PENDING = 200;
    private static final long DELAY_MS = 2000;
    private static final long RETRY_MS = 15000;

    private ErrorHistory() {
    }

    /**
     * An error waiting to be written
     */
    static final class Pending {

        int count;
        String where;
    }

    private static final Map<String, Pending> PENDING = new LinkedHashMap<String, Pending>();
    private static final ThreadLocal<Boolean> WRITING = ThreadLocal.withInitial(() -> false);
    private static ScheduledExecutorService writer;
    private static boolean scheduled;
    private static volatile boolean enabled = true;

    /**
     * @param on false: nothing is recorded (e.g. tests)
     */
    public static void setEnabled(boolean on) {
        enabled = on;
    }

    /**
     * Records an exception (called for every logged exception)
     *
     * @param source where it was logged (object or class), may be null
     * @param t
     */
    public static void record(Object source, Throwable t) {
        if (!enabled || t == null || WRITING.get() || ignored(t)) {
            return;
        }
        String key = message(t);
        String where = where(source, t);
        synchronized (PENDING) {
            Pending p = PENDING.get(key);
            if (p == null) {
                if (PENDING.size() >= MAX_PENDING) {
                    return;
                }
                p = new Pending();
                PENDING.put(key, p);
            }
            p.count++;
            p.where = where;
            if (!scheduled) {
                scheduled = true;
                if (writer == null) {
                    writer = Executors.newSingleThreadScheduledExecutor(r -> {
                        Thread th = new Thread(r, "error history");
                        th.setDaemon(true);
                        return th;
                    });
                }
                writer.schedule(ErrorHistory::flush, DELAY_MS, TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * Not errors of the program: "nothing found" and a change the user did
     * not approve are part of the normal flow
     */
    static boolean ignored(Throwable t) {
        String n = t.getClass().getName();
        return n.equals("mpv5.db.common.NodataFoundException")
                || n.equals("mpv5.ui.panels.ChangeNotApprovedException");
    }

    /**
     * @param t
     * @return the type and message of the error, e.g.
     * "NullPointerException: Cannot invoke ..." (at most 250 characters)
     */
    static String message(Throwable t) {
        String m = t.getMessage();
        String s = t.getClass().getSimpleName() + (m == null || m.trim().isEmpty() ? "" : ": " + m.trim().replaceAll("\\s+", " "));
        return cut(s, 250);
    }

    /**
     * @return the class that logged it and the first place in YaBS code
     * (at most 500 characters)
     */
    static String where(Object source, Throwable t) {
        //a String: the name of the thread (uncaught exceptions)
        String src = source == null ? "" : source instanceof Class ? ((Class<?>) source).getName()
                : source instanceof String ? "Thread " + source : source.getClass().getName();
        String at = "";
        for (StackTraceElement e : t.getStackTrace()) {
            if (e.getClassName().startsWith("mpv5.")) {
                at = e.toString();
                break;
            }
        }
        if (at.isEmpty() && t.getStackTrace().length > 0) {
            at = t.getStackTrace()[0].toString();
        }
        String s = src.equals(Log.class.getName()) || src.isEmpty() ? at : at.isEmpty() ? src : src + " · " + at;
        return cut(s, 500);
    }

    private static String cut(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    /**
     * Writes the collected errors now, if there is a database connection
     * (on the calling thread)
     */
    public static void flush() {
        Map<String, Pending> todo;
        synchronized (PENDING) {
            scheduled = false;
            if (PENDING.isEmpty()) {
                return;
            }
            todo = new LinkedHashMap<String, Pending>(PENDING);
            PENDING.clear();
        }
        Connection c = connection();
        if (c == null) {
            //no database (yet, e.g. at start): back to the list, tried again later
            requeue(todo);
            synchronized (PENDING) {
                if (!scheduled && writer != null) {
                    scheduled = true;
                    writer.schedule(ErrorHistory::flush, RETRY_MS, TimeUnit.MILLISECONDS);
                }
            }
            return;
        }
        WRITING.set(true);
        try {
            String user = "";
            int group = 1;
            try {
                mpv5.db.objects.User u = mpv5.db.objects.User.getCurrentUser();
                user = u.__getCname() == null ? "" : u.__getCname();
                group = u.__getGroupsids();
            } catch (Throwable e) {
                //no user yet
            }
            for (Map.Entry<String, Pending> e : todo.entrySet()) {
                try {
                    write(c, e.getKey(), e.getValue().count, e.getValue().where, user, group);
                } catch (Throwable ex) {
                    //not recorded again (it is an error of this writer); in the log only
                    Log.Print("Error history not written: " + ex);
                }
            }
        } finally {
            WRITING.set(false);
        }
    }

    private static void requeue(Map<String, Pending> todo) {
        synchronized (PENDING) {
            for (Map.Entry<String, Pending> e : todo.entrySet()) {
                Pending p = PENDING.get(e.getKey());
                if (p == null) {
                    if (PENDING.size() >= MAX_PENDING) {
                        continue;
                    }
                    PENDING.put(e.getKey(), e.getValue());
                } else {
                    p.count += e.getValue().count;
                }
            }
        }
    }

    private static Connection connection() {
        try {
            mpv5.db.common.DatabaseConnection dc = mpv5.db.common.DatabaseConnection.getConnector();
            Connection c = dc == null ? null : dc.getConnection();
            return c == null || c.isClosed() ? null : c;
        } catch (Throwable e) {
            return null;
        }
    }

    private static String table() {
        return mpv5.db.common.Context.getHistory().getDbIdentity();
    }

    /**
     * Adds count to the entry of the error, a new entry the first time
     */
    static void write(Connection c, String message, int count, String where, String user, int group) throws Exception {
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        Integer id = null;
        long before = 0;
        try (PreparedStatement s = c.prepareStatement("SELECT ids, reserve1 FROM " + table() + " WHERE dbidentity = ? AND cname = ?")) {
            s.setString(1, DBIDENTITY);
            s.setString(2, message);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    id = r.getInt(1);
                    try {
                        before = Long.parseLong(String.valueOf(r.getString(2)).trim());
                    } catch (NumberFormatException e) {
                        before = 1;
                    }
                }
            }
        }
        if (id != null) {
            try (PreparedStatement u = c.prepareStatement("UPDATE " + table() + " SET reserve1 = ?, reserve2 = ?, dateadded = ?, username = ? WHERE ids = ?")) {
                u.setString(1, String.valueOf(before + count));
                u.setString(2, where);
                u.setTimestamp(3, now);
                u.setString(4, user);
                u.setInt(5, id);
                u.executeUpdate();
            }
        } else {
            try (PreparedStatement i = c.prepareStatement("INSERT INTO " + table()
                    + " (cname, username, dbidentity, intitem, groupsids, dateadded, reserve1, reserve2) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                i.setString(1, message);
                i.setString(2, user);
                i.setString(3, DBIDENTITY);
                i.setInt(4, 0);
                i.setInt(5, group);
                i.setTimestamp(6, now);
                i.setString(7, String.valueOf(count));
                i.setString(8, where);
                i.executeUpdate();
            }
        }
    }
}
