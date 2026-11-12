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

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * H2 (file database) uses the Derby structure and updates, converted: H2
 * runs in its Derby compatibility mode, and SMALLINT columns become BIGINT
 * (no overflows of small numbers).
 */
public final class H2Dialect {

    private static final Pattern SMALLINT = Pattern.compile("(?i)\\bSMALLINT\\b");
    private static final Pattern REF_DEFAULT = Pattern.compile(
            "(?i)(REFERENCES\\s+\\w+\\s*\\(\\s*\\w+\\s*\\)(?:\\s+ON\\s+DELETE\\s+(?:CASCADE|RESTRICT|SET\\s+NULL|NO\\s+ACTION))?)\\s+(DEFAULT\\s+[^,)\\s]+)");
    private static final Pattern LONG_VARCHAR = Pattern.compile("(?i)\\bLONG\\s+VARCHAR\\b");
    private static final Pattern RENAME = Pattern.compile("(?is)^\\s*RENAME\\s+COLUMN\\s+(\\w+)\\.(\\w+)\\s+TO\\s+(\\w+)\\s*$");
    private static final Pattern TRIGGER = Pattern.compile(
            "(?is)^\\s*CREATE\\s+TRIGGER\\s+(\\w+)\\s+((?:NO\\s+CASCADE\\s+)?BEFORE|AFTER)\\s+(INSERT|UPDATE|DELETE)(?:\\s+OF\\s+[\\w\\s,]+?)?\\s+ON\\s+(\\w+)\\s+(.*)$");
    private static final Pattern IDENTITY = Pattern.compile("(?i)(START WITH \\d+)\\s*,\\s*(INCREMENT BY)");

    /**
     * column names YaBS uses which are keywords in H2
     */
    static final String NON_KEYWORDS = "VALUE,KEY,YEAR,MONTH,DAY,HOUR,MINUTE,SECOND,USER,ROW,TOP,LIMIT,MINUS";

    private H2Dialect() {
    }

    /**
     * @param sql a Derby statement
     * @return the statement for H2
     */
    public static String convert(String sql) {
        if (sql == null) {
            return null;
        }
        Matcher t = TRIGGER.matcher(sql);
        if (t.matches()) {
            //H2 triggers are Java classes: H2Trigger runs the stored statement
            String rest = t.group(5);
            boolean row = !rest.trim().toUpperCase().matches("(?s)FOR\\s+EACH\\s+STATEMENT.*");
            return "CREATE TABLE IF NOT EXISTS yabstriggers (cname VARCHAR(250) PRIMARY KEY, body VARCHAR(10000)); "
                    + "MERGE INTO yabstriggers (cname, body) KEY (cname) VALUES ('" + t.group(1).toUpperCase() + "', '"
                    + convert(rest).replace("'", "''") + "'); "
                    + "CREATE TRIGGER " + t.group(1) + " " + (t.group(2).toUpperCase().endsWith("BEFORE") ? "BEFORE" : "AFTER")
                    + " " + t.group(3) + " ON " + t.group(4) + (row ? " FOR EACH ROW" : "")
                    + " CALL \"" + H2Trigger.class.getName() + "\"";
        }
        if (sql.matches("(?is)\\s*ALTER\\s+TABLE\\s+activitylist(s|items)\\s+DROP\\s+CONSTRAINT\\s+SQL\\d+\\s*")) {
            //Derby names of the references dropped in 1.1979: H2 creates the tables without them
            return "SELECT 1";
        }
        if (sql.matches("(?is)\\s*CREATE\\s+TABLE\\s+activitylist(s|items)\\s*\\(.*")) {
            sql = sql.replaceAll("(?i)(orderids\\s+BIGINT)\\s+REFERENCES\\s+items\\(ids\\)", "$1")
                    .replaceAll("(?i)(contactsids\\s+BIGINT)\\s+REFERENCES\\s+contacts\\(ids\\)(\\s+DEFAULT\\s+0)", "$1$2")
                    .replaceAll("(?i)(productsids\\s+BIGINT)\\s+REFERENCES\\s+products\\(ids\\)\\s+ON\\s+DELETE\\s+CASCADE", "$1");
        }
        Matcher rename = RENAME.matcher(sql);
        if (rename.matches()) {
            return "ALTER TABLE " + rename.group(1) + " ALTER COLUMN " + rename.group(2) + " RENAME TO " + rename.group(3);
        }
        String r = SMALLINT.matcher(sql).replaceAll("BIGINT");
        r = LONG_VARCHAR.matcher(r).replaceAll("VARCHAR(32700)");
        r = REF_DEFAULT.matcher(r).replaceAll("$2 $1");
        return IDENTITY.matcher(r).replaceAll("$1 $2");
    }

    public static String[] convert(String[] sql) {
        String[] r = new String[sql.length];
        for (int i = 0; i < sql.length; i++) {
            r[i] = convert(sql[i]);
        }
        return r;
    }

    public static Map<Double, String[]> convert(Map<Double, String[]> updates) {
        Map<Double, String[]> r = new TreeMap<Double, String[]>();
        for (Map.Entry<Double, String[]> e : updates.entrySet()) {
            r.put(e.getKey(), convert(e.getValue()));
        }
        return r;
    }

    /**
     * @param path the folder of the database
     * @param name the name of the database (the file is name.mv.db)
     * @param create create it if missing
     * @return the JDBC url
     */
    public static String url(String path, String name, boolean create) {
        return "jdbc:h2:file:" + path + java.io.File.separator + name + ";MODE=Derby;DEFAULT_NULL_ORDERING=HIGH;NON_KEYWORDS=" + NON_KEYWORDS
                + (create ? "" : ";IFEXISTS=TRUE");
    }
}
