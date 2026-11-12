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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.h2.api.Trigger;

/**
 * H2 triggers are Java classes: this one runs the SQL of a Derby trigger
 * (stored in the table yabstriggers by {@link H2Dialect}). References to
 * the row (newdata.cname) become parameters.
 */
public class H2Trigger implements Trigger {

    private static final Pattern ROW = Pattern.compile("(?is)^\\s*REFERENCING\\s+(NEW|OLD)(?:\\s+ROW)?\\s+AS\\s+(\\w+)\\s+FOR\\s+EACH\\s+ROW\\s+(.*)$");
    private static final Pattern STATEMENT = Pattern.compile("(?is)^\\s*FOR\\s+EACH\\s+(?:ROW|STATEMENT)\\s+(.*)$");
    private String sql;
    private boolean old;
    private final List<Integer> params = new ArrayList<Integer>();

    @Override
    public void init(Connection conn, String schema, String trigger, String table, boolean before, int type) throws SQLException {
        String body;
        try (PreparedStatement ps = conn.prepareStatement("SELECT body FROM yabstriggers WHERE cname = ?")) {
            ps.setString(1, trigger.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No statement for trigger " + trigger);
                }
                body = rs.getString(1);
            }
        }
        Matcher m = ROW.matcher(body);
        if (!m.matches()) {
            Matcher s = STATEMENT.matcher(body);
            if (!s.matches()) {
                throw new SQLException("Unknown trigger " + trigger + ": " + body);
            }
            sql = s.group(1);
            return;
        }
        old = m.group(1).equalsIgnoreCase("OLD");
        List<String> columns = new ArrayList<String>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS"
                + " WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION")) {
            ps.setString(1, schema);
            ps.setString(2, table);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    columns.add(rs.getString(1).toUpperCase());
                }
            }
        }
        Matcher ref = Pattern.compile("(?i)\\b" + Pattern.quote(m.group(2)) + "\\.(\\w+)\\b").matcher(m.group(3));
        StringBuilder b = new StringBuilder();
        while (ref.find()) {
            int i = columns.indexOf(ref.group(1).toUpperCase());
            if (i < 0) {
                throw new SQLException("Trigger " + trigger + ": no column " + ref.group(1) + " in " + table);
            }
            params.add(i);
            ref.appendReplacement(b, "?");
        }
        ref.appendTail(b);
        sql = b.toString();
    }

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        Object[] row = old ? oldRow : newRow;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, row == null ? null : row[params.get(i)]);
            }
            ps.execute();
        }
    }
}
