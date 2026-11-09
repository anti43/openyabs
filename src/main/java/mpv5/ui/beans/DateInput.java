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
package mpv5.ui.beans;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import mpv5.utils.date.DateConverter;

/**
 * Keyboard friendly date input: numeric format of the locale (dd.MM.yyyy in
 * German), free typing with short forms (1.2., 1.2.26, 010226, 5, +7, -1,
 * h/heute/t/today), arrow keys change the part under the caret.
 */
public final class DateInput {

    private DateInput() {
    }

    /**
     * Sets up the spinner as keyboard friendly date field
     *
     * @param spinner a spinner with a SpinnerDateModel
     */
    public static void install(JSpinner spinner) {
        String pattern = DateConverter.getDefaultFormatString();
        spinner.setEditor(new JSpinner.DateEditor(spinner, pattern));
        final JFormattedTextField f = ((JSpinner.DateEditor) spinner.getEditor()).getTextField();
        Formatter fmt = new Formatter(pattern);
        f.setFormatterFactory(new DefaultFormatterFactory(fmt));
        f.setEditable(true);
        f.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        f.setColumns(Math.max(8, pattern.length()));
        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(f::selectAll);
            }
        });
        f.setToolTipText(pattern.toLowerCase().replace('y', 'j').replace('d', 't') + "  (z.B. 1.2. / 010226 / +7 / h; Alt+↓: Kalender)");
        if (!Locale.getDefault().getLanguage().equals("de")) {
            f.setToolTipText(pattern.toLowerCase() + "  (e.g. 1/2 / +7 / t; Alt+↓: calendar)");
        }
    }

    /**
     * Parses typed text to a date
     *
     * @param text
     * @param pattern the full pattern, e.g. dd.MM.yyyy
     * @return the date or null
     */
    public static Date parse(String text, String pattern) {
        if (text == null) {
            return null;
        }
        String t = text.trim().toLowerCase();
        if (t.isEmpty()) {
            return null;
        }
        Calendar now = Calendar.getInstance();
        if (t.equals("h") || t.equals("heute") || t.equals("t") || t.equals("today")) {
            return today();
        }
        if (t.matches("[+-]\\d{1,4}")) {
            Calendar c = Calendar.getInstance();
            c.setTime(today());
            c.add(Calendar.DATE, Integer.parseInt(t.replace("+", "")));
            return c.getTime();
        }
        if (t.matches("\\d{1,2}")) {
            Calendar c = Calendar.getInstance();
            c.setTime(today());
            int d = Integer.parseInt(t);
            if (d < 1 || d > c.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                return null;
            }
            c.set(Calendar.DAY_OF_MONTH, d);
            return c.getTime();
        }
        for (String p : patterns(pattern)) {
            Date d = strict(t, p);
            if (d != null) {
                if (!p.contains("y")) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(d);
                    c.set(Calendar.YEAR, now.get(Calendar.YEAR));
                    return c.getTime();
                }
                return d;
            }
        }
        return null;
    }

    private static Date today() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    static List<String> patterns(String pattern) {
        List<String> l = new ArrayList<>();
        String sep = pattern.replaceAll("[A-Za-z]", "");
        String s = sep.isEmpty() ? "." : sep.substring(0, 1);
        l.add(pattern);
        l.add(pattern.replace("yyyy", "yy"));
        //single digits
        l.add(pattern.replace("dd", "d").replace("MM", "M"));
        l.add(pattern.replace("dd", "d").replace("MM", "M").replace("yyyy", "yy"));
        //digits only
        String digits = pattern.replaceAll("[^A-Za-z]", "");
        l.add(digits);
        l.add(digits.replace("yyyy", "yy"));
        //without year (current year), e.g. "1.2." or "1/2"
        String noYear = pattern.replaceAll("[^A-Za-z]*y+[^A-Za-z]*", s).replaceAll("^\\" + s + "|\\" + s + "$", "");
        l.add(noYear.replace("dd", "d").replace("MM", "M"));
        l.add(noYear.replace("dd", "d").replace("MM", "M") + s);
        //common other separators
        for (String o : new String[]{".", "/", "-"}) {
            if (!o.equals(s)) {
                l.add(pattern.replace(s, o).replace("dd", "d").replace("MM", "M"));
                l.add(pattern.replace(s, o).replace("dd", "d").replace("MM", "M").replace("yyyy", "yy"));
            }
        }
        return l;
    }

    private static Date strict(String t, String p) {
        try {
            SimpleDateFormat f = new SimpleDateFormat(p);
            f.setLenient(false);
            ParsePosition pos = new ParsePosition(0);
            Date d = f.parse(t, pos);
            if (d != null && pos.getIndex() == t.length()) {
                if (p.contains("yyyy")) {
                    //"yyyy" accepts 2 digit years literally: 0026
                    Calendar c = Calendar.getInstance();
                    c.setTime(d);
                    if (c.get(Calendar.YEAR) < 1000) {
                        return null;
                    }
                }
                return d;
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Formatter: shows the numeric pattern, parses free input
     */
    static final class Formatter extends DateFormatter {

        private static final long serialVersionUID = 1L;
        private final String pattern;

        Formatter(String pattern) {
            super(new SimpleDateFormat(pattern));
            this.pattern = pattern;
            setAllowsInvalid(true);
            setCommitsOnValidEdit(false);
            setOverwriteMode(false);
            setValueClass(Date.class);
        }

        @Override
        public Object stringToValue(String text) throws ParseException {
            Date d = parse(text, pattern);
            if (d == null) {
                throw new ParseException(text, 0);
            }
            return d;
        }
    }
}
