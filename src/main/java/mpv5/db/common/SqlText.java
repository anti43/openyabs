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

/**
 * Text in SQL string literals ('...').
 * <p>
 * Up to 2026 YaBS did not escape texts but changed them: every ' became `
 * ("O'Brien" was stored as "O`Brien", href='x' in a mail template as
 * href=`x`), and with the local setting dbescape (default on) every backslash
 * became "&lt;removedbackslash&gt;" in the database. Now texts are stored as they
 * are (' escaped as '', on MySQL also \ as \\). Data saved before stays as it is:
 * "&lt;removedbackslash&gt;" is always read back as \, equal-comparisons also find
 * the old forms, LIKE searches match both.
 */
public final class SqlText {

    private SqlText() {
    }

    /**
     * @param s
     * @return the text for inside '...': ' doubled; on MySQL / MariaDB also
     * backslashes doubled (there a backslash escapes, \' would end the string)
     */
    public static String literal(String s) {
        if (s == null) {
            return "";
        }
        String t = s.replace("'", "''");
        if (isMySQL()) {
            t = t.replace("\\", "\\\\");
        }
        return t;
    }

    /**
     * @param s
     * @return the text as YaBS stored it before (every ' as `)
     */
    public static String legacy(String s) {
        return s == null ? "" : s.replace("'", "`");
    }

    /**
     * @param column
     * @param value a text
     * @return column = 'value', with a ' or \\ also the old forms: (column =
     * 'O''Brien' OR column = 'O`Brien')
     */
    public static String equalsText(String column, String value) {
        String v = value == null ? "" : value;
        java.util.LinkedHashSet<String> forms = new java.util.LinkedHashSet<String>();
        forms.add(v);
        forms.add(legacy(v));
        String b = v.replace("\\", QueryHandler.BACKSLASH_REPLACEMENT_STRING);
        forms.add(b);
        forms.add(legacy(b));
        StringBuilder c = new StringBuilder();
        for (String f : forms) {
            c.append(c.length() == 0 ? "" : " OR ").append(column).append(" = '").append(literal(f)).append("'");
        }
        return forms.size() == 1 ? c.toString() : "(" + c + ")";
    }

    /**
     * @param value
     * @return the value for a LIKE pattern '%...%': a ' matches ' and the old `
     * (any single character); a \\ (the escape character of LIKE) matches any
     * single character as well
     */
    public static String likeText(String value) {
        if (value == null) {
            return "";
        }
        //'_' is the single character wildcard of LIKE
        return literal(value.replace("'", "_").replace("\\", "_"));
    }

    private static boolean isMySQL() {
        try {
            Integer t = ConnectionTypeHandler.getDriverType();
            return t != null && t == ConnectionTypeHandler.MYSQL;
        } catch (Exception e) {
            return false;
        }
    }
}
