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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The neutral database structure of the current version
 * ({@code /db/schema.sql}, written by {@link SchemaExport}): new databases are
 * created from it in one step instead of the old structure plus all updates.
 */
public final class Schema {

    public static final String RESOURCE = "/db/schema.sql";
    private static Schema instance;
    private final double version;
    private final List<String> structure = new ArrayList<String>();
    private final List<String> triggers = new ArrayList<String>();
    private final List<String> data = new ArrayList<String>();

    /**
     * @param text the file
     */
    Schema(String text) {
        Matcher m = Pattern.compile("version ([0-9.]+)").matcher(text);
        version = m.find() ? Double.parseDouble(m.group(1)) : 0;
        for (String s : split(text)) {
            String u = s.toUpperCase();
            if (u.startsWith("CREATE TRIGGER")) {
                triggers.add(s);
            } else if (u.startsWith("INSERT")) {
                data.add(s);
            } else {
                structure.add(s);
            }
        }
    }

    /**
     * @return the schema of this YaBS version
     */
    public static synchronized Schema get() {
        if (instance == null) {
            try (InputStream in = Schema.class.getResourceAsStream(RESOURCE)) {
                instance = new Schema(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            } catch (IOException | NullPointerException e) {
                throw new IllegalStateException("Missing " + RESOURCE, e);
            }
        }
        return instance;
    }

    /**
     * Statements end with ";" at the end of a line; "--" lines are comments
     */
    static List<String> split(String text) {
        List<String> l = new ArrayList<String>();
        StringBuilder cur = new StringBuilder();
        for (String line : text.split("\n")) {
            String t = line.trim();
            if (t.startsWith("--") || (t.isEmpty() && cur.length() == 0)) {
                continue;
            }
            cur.append(line).append('\n');
            if (t.endsWith(";")) {
                String s = cur.toString().trim();
                l.add(s.substring(0, s.length() - 1).trim());
                cur.setLength(0);
            }
        }
        if (cur.toString().trim().length() > 0) {
            l.add(cur.toString().trim());
        }
        return l;
    }

    /**
     * @return the database version of the file
     */
    public double getVersion() {
        return version;
    }

    /**
     * @return tables, indexes, foreign keys (neutral)
     */
    public List<String> getStructure() {
        return Collections.unmodifiableList(structure);
    }

    /**
     * @return the triggers (Derby syntax)
     */
    public List<String> getTriggers() {
        return Collections.unmodifiableList(triggers);
    }

    /**
     * @return the rows of the updates, inserted after the initial data
     */
    public List<String> getData() {
        return Collections.unmodifiableList(data);
    }

    /**
     * @param d the database
     * @return structure and triggers for the database
     */
    public String[] structureFor(SqlDialect d) {
        List<String> l = new ArrayList<String>();
        for (String s : structure) {
            l.addAll(d.convert(s));
        }
        for (String s : triggers) {
            l.addAll(d.trigger(s));
        }
        return l.toArray(new String[0]);
    }

    /**
     * @param d the database
     * @return the rows of the updates and the version of this file
     */
    public String[] dataFor(SqlDialect d) {
        List<String> l = new ArrayList<String>(data);
        l.add("UPDATE globalsettings SET value = '" + version + "' WHERE cname = 'yabs_dbversion'");
        return l.toArray(new String[0]);
    }
}
