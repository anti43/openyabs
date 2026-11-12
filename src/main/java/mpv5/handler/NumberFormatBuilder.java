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
package mpv5.handler;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A number format (e.g. RE-2026-00042) from building blocks: prefix, date
 * part, separator, digits of the counter, suffix - and back. Writes the
 * pattern {@link FormatHandler} uses ({@code RE-[YEAR]-{0,number,00000}}) and
 * the parse position: 0 lets the counter start again when the date part
 * changes (new year / month), the length of the text before the counter
 * keeps counting.
 */
public final class NumberFormatBuilder {

    /**
     * The date part of a number
     */
    public enum DatePart {
        NONE(""),
        YEAR("[YEAR]"),
        YEAR_SHORT("[YEAR_SHORT]"),
        YEAR_MONTH("[YEAR]{sep}[MONTH]");

        final String tokens;

        DatePart(String tokens) {
            this.tokens = tokens;
        }
    }

    private String prefix = "";
    private DatePart date = DatePart.NONE;
    private String separator = "-";
    private int digits = 5;
    private String suffix = "";
    private boolean restart = true;

    public String getPrefix() {
        return prefix;
    }

    /**
     * @param prefix text before the number; brackets and braces are removed
     * (they have a meaning in the pattern)
     */
    public NumberFormatBuilder setPrefix(String prefix) {
        this.prefix = clean(prefix);
        return this;
    }

    public DatePart getDate() {
        return date;
    }

    public NumberFormatBuilder setDate(DatePart date) {
        this.date = date == null ? DatePart.NONE : date;
        return this;
    }

    public String getSeparator() {
        return separator;
    }

    public NumberFormatBuilder setSeparator(String separator) {
        this.separator = clean(separator);
        return this;
    }

    public int getDigits() {
        return digits;
    }

    /**
     * @param digits at least, 1 to 10 (1: no leading zeros)
     */
    public NumberFormatBuilder setDigits(int digits) {
        this.digits = Math.max(1, Math.min(10, digits));
        return this;
    }

    public String getSuffix() {
        return suffix;
    }

    public NumberFormatBuilder setSuffix(String suffix) {
        this.suffix = clean(suffix);
        return this;
    }

    /**
     * @return true: the counter starts again at 1 with a new year (month)
     */
    public boolean isRestart() {
        return restart;
    }

    public NumberFormatBuilder setRestart(boolean restart) {
        this.restart = restart;
        return this;
    }

    private static String clean(String s) {
        return s == null ? "" : s.replaceAll("[\\[\\]{}]", "");
    }

    private static String quote(String s) {
        return s.replace("'", "''");
    }

    private String datePattern() {
        return date.tokens.replace("{sep}", quote(separator));
    }

    /**
     * @return the pattern for {@link FormatHandler}, e.g.
     * RE-[YEAR]-{0,number,00000}
     */
    public String pattern() {
        StringBuilder zeros = new StringBuilder();
        for (int i = 0; i < digits; i++) {
            zeros.append('0');
        }
        String d = datePattern();
        return quote(prefix) + d + (d.isEmpty() ? "" : quote(separator)) + "{0,number," + zeros + "}" + quote(suffix);
    }

    /**
     * @param when the date for the date part
     * @return the date part as text, e.g. 2026-11
     */
    String dateText(Date when) {
        return resolve(date.tokens.replace("{sep}", separator), when);
    }

    /**
     * @return 0 (start again with a new date part) or the length of the text
     * before the counter (keep counting)
     */
    public int parsePosition() {
        if (restart || date == DatePart.NONE) {
            return 0;
        }
        String d = dateText(new Date());
        return prefix.length() + d.length() + separator.length();
    }

    /**
     * @param n the counter
     * @param when the date
     * @return the number, e.g. RE-2026-00042
     */
    public String sample(int n, Date when) {
        return format(pattern(), n, when);
    }

    /**
     * @param pattern any pattern of {@link FormatHandler}
     * @param n the counter
     * @param when the date for the date variables
     * @return the number
     */
    public static String format(String pattern, int n, Date when) {
        return new MessageFormat(resolve(pattern, when)).format(new Object[]{n});
    }

    private static String resolve(String text, Date when) {
        Calendar c = Calendar.getInstance();
        c.setTime(when);
        String year = String.valueOf(c.get(Calendar.YEAR));
        return text.replace("[YEAR_SHORT]", year.substring(2))
                .replace("[YEAR]", year)
                .replace("[MONTH]", String.format("%02d", c.get(Calendar.MONTH) + 1))
                .replace("[QUARTER]", String.valueOf(c.get(Calendar.MONTH) / 3 + 1));
    }

    private static final Pattern PARSE = Pattern.compile(
            "^((?:[^\\[\\]{}']|'')*)(\\[YEAR\\]((?:[^\\[\\]{}']|'')*)\\[MONTH\\]|\\[YEAR\\]|\\[YEAR_SHORT\\])?((?:[^\\[\\]{}']|'')*)\\{0,number,(0+)\\}((?:[^\\[\\]{}']|'')*)$");

    /**
     * @param pattern a stored pattern
     * @param position its parse position
     * @return the building blocks, null if the pattern can not be built with
     * them (expert pattern)
     */
    public static NumberFormatBuilder fromPattern(String pattern, int position) {
        if (pattern == null) {
            return null;
        }
        Matcher m = PARSE.matcher(pattern.trim());
        if (!m.matches()) {
            return null;
        }
        NumberFormatBuilder b = new NumberFormatBuilder();
        String unq1 = m.group(1).replace("''", "'");
        String dateTokens = m.group(2);
        String sep = m.group(4).replace("''", "'");
        if (dateTokens == null) {
            //no date: everything before the number is the prefix
            b.setPrefix(unq1 + sep).setDate(DatePart.NONE).setSeparator("-");
        } else {
            DatePart d = dateTokens.startsWith("[YEAR_SHORT]") ? DatePart.YEAR_SHORT
                    : dateTokens.contains("[MONTH]") ? DatePart.YEAR_MONTH : DatePart.YEAR;
            if (d == DatePart.YEAR_MONTH && !m.group(3).replace("''", "'").equals(sep)) {
                return null;//different separators: not a building block format
            }
            b.setPrefix(unq1).setDate(d).setSeparator(sep);
        }
        b.setDigits(m.group(5).length()).setSuffix(m.group(6).replace("''", "'"));
        b.setRestart(position == 0);
        return b;
    }
}
