/*
 *  This file is part of MP.
 *  
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *  
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *  
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.date;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import mpv5.logging.Log;
import mpv5.ui.frames.MPV5View;

/**
 *
 *  anti
 */
public class DateConverter {

    public static String[] months = DateFormatSymbols.getInstance().getMonths();
    private static Calendar cl = Calendar.getInstance();
    public static DateFormat DEF_DATE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT);
    public static final DateFormat NATIVE_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    public static final DateFormat DE_FULL_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyy - HH:mm:ss");
    public static final DateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //DE format
    public static final DateFormat DE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
    //US format
    public static final DateFormat ENG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final DateFormat YEAR_DATE_FORMAT = new SimpleDateFormat("yyyy");
    public static final ArrayList<DateFormat> DATE_FORMATS = new ArrayList<DateFormat>(Arrays.asList(new DateFormat[]{
                DB_DATE_FORMAT,
                ENG_DATE_FORMAT,
                DE_DATE_FORMAT,
                DE_FULL_DATE_FORMAT,
                NATIVE_DATE_FORMAT,
                DEF_DATE_FORMAT,
                YEAR_DATE_FORMAT
            }));

    /**
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addDays(Date date, Integer amount) {
        cl.setTime(date);
        cl.add(Calendar.DATE, amount);

        return cl.getTime();
    }

    /**
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addYears(Date date, int amount) {

        cl.setTime(date);
        cl.add(Calendar.YEAR, amount);

        return cl.getTime();
    }

    /**
     * Get days difference
     * @param date1
     * @param date2
     * @return
     */
    public static Integer getDifferenceBetween(Date date1, Date date2) {

        if (date1.after(date2)) {
            Date swap = date1;
            date1 = date2;
            date2 = swap;
        }

        Calendar d1 = Calendar.getInstance();
        d1.setTime(date1);

        Calendar d2 = Calendar.getInstance();
        d2.setTime(date2);

        int days = d2.get(java.util.Calendar.DAY_OF_YEAR) -
                d1.get(java.util.Calendar.DAY_OF_YEAR);
        int y2 = d2.get(java.util.Calendar.YEAR);
        if (d1.get(java.util.Calendar.YEAR) != y2) {
            d1 = (java.util.Calendar) d1.clone();
            do {
                days += d1.getActualMaximum(java.util.Calendar.DAY_OF_YEAR);
                d1.add(java.util.Calendar.YEAR, 1);
            } while (d1.get(java.util.Calendar.YEAR) != y2);
        }
        return days;

    }

    /**
     * Returns the same date , one second before the next day
     * @param date
     * @return
     */
    public static Date getEndOfDay(Date date) {
        Calendar calendar = cl;
        synchronized (calendar) {
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MILLISECOND, 999);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MINUTE, 59);
            return calendar.getTime();
        }
    }

    /**
     * Quarter as 1,2,3,4
     * @return
     */
    public static int getQuarter() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int month = cal.get(Calendar.MONTH); /* 0 through 11 */
        int quarter = (month / 3) + 1;
        return quarter;
    }

    public static String getTodayDefDate() {
        return DE_DATE_FORMAT.format(new Date());
    }

    public static String getTodayDBDate() {
        return DB_DATE_FORMAT.format(new Date());
    }

    public static Date addYear(Date date) {
        Calendar cal = DateConverter.cl;
        synchronized (cal) {
            cal.setTime(date);
            cal.add(Calendar.YEAR, 1);

            return cal.getTime();
        }
    }

    /**
     *
     * @param date
     * @return The next month
     */
    public static Date addMonth(Date date) {
        Calendar cal = DateConverter.cl;

        synchronized (cal) {
            cal.setTime(date);
            cal.add(Calendar.MONTH, 1);

            return cal.getTime();
        }
    }

    /**
     *
     * @param date
     * @return End of the quarter
     */
    public static Date addQuarter(Date date) {
        Calendar cal = DateConverter.cl;

        synchronized (cal) {
            cal.setTime(date);
            cal.add(Calendar.MONTH, 3);

            return cal.getTime();
        }
    }

    /**
     *
     * @param date
     * @return The next day
     */
    public static Date addDay(Date date) {
        Calendar cal = DateConverter.cl;
        synchronized (cal) {
            cal.setTime(date);
            cal.add(Calendar.DATE, 1);

            return cal.getTime();
        }
    }

    /**
     *
     * @param date
     * @return SQL conform date String
     */
    public static String getSQLDateString(Date date) {
        return DB_DATE_FORMAT.format(date);
    }

    /**
     *
     * @param date
     * @return Default date
     */
    public static String getDefDateString(Date date) {
        return DEF_DATE_FORMAT.format(date);
    }

    public static String getDay(Date datum) {
        return DE_DATE_FORMAT.format(datum);
    }

    public static String getMonthName(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return months[cal.get(Calendar.MONTH)];
    }

    public static String getDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Converts formated
     * Default date (dd.mm.yyyy) or variants
     * or
     * yyyy-mm-dd hh.mm.ss[.nnnnnn] - SQL DATE Timestamp
     *
     * to a Date object.
     *
     * @param date
     * @return Parsed date
     */
    public static Date getDate(String date) {
        Date DATE = null;
        for (int i = 0; i < DATE_FORMATS.toArray().length; i++) {
            try {
                DATE = ((DateFormat) DATE_FORMATS.toArray()[i]).parse(date);
                return DATE;
            } catch (ParseException ex) {
            }
        }
        if (DATE == null) {
            Log.Debug(DateConverter.class, "String not parseable to a date: " + date);
        }
        return DATE;
    }

    /**
     * Triy to parse the given object to a date
     * @param object
     * @return
     */
    public static Date getDate(Object object) {
        if (object instanceof Date) {
            return (Date) object;
        }else if (object instanceof java.sql.Date) {
            return new Date(((java.sql.Date) object).getTime());
        } else {
            return getDate(object.toString());
        }
    }

    public static String getYear() {
        return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    }

    public static String getDayOfMonth() {
        return String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    }

    public static String getDayMonthAndYear() {
        return DateConverter.getDefDateString(new Date());
    }

    public static String getMonth() {
        return String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1);
    }

    public static String getMonthName() {
        Calendar cal = Calendar.getInstance();
        return months[cal.get(Calendar.MONTH)];
    }

    public static void setDefaultFormat(DateFormat df) {
        DEF_DATE_FORMAT = df;
    }
}
