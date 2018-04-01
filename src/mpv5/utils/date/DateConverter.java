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
package mpv5.utils.date;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import mpv5.logging.Log;
import mpv5.utils.text.TypeConversion;

/**
 *
 * This class provides date formatting methods and default
 * date formats
 */
public class DateConverter {

    /**
     * Gets month strings. For example: "January",
     * "February", etc.
     */
    public static String[] months = DateFormatSymbols.getInstance().getMonths();
    private static Calendar cl = Calendar.getInstance();
    /**
     * The date formatter with the short formatting style
     * for the default locale.
     */
    public static DateFormat DEF_DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);
    /**
     * "EEE, dd MMM yyyy HH:mm:ss z"
     */
    public static final DateFormat NATIVE_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    /**
     * "dd.MM.yyy - HH:mm:ss"
     */
    public static final DateFormat DE_FULL_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyy - HH:mm:ss");
    /**
     * "yyyy-MM-dd HH:mm:ss"
     */
    public static final DateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * DE format
     */
    public static final DateFormat DE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
    /**
     * "yyyy-MM-dd"
     */
    public static final DateFormat ENG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    /**
     * YYYY
     */
    public static final DateFormat YEAR_DATE_FORMAT = new SimpleDateFormat("yyyy");
    /**
     * Contains all available default date formats
     */
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
    public static synchronized Date addDays(Date date, Integer amount) {
        cl.setTime(date);
        cl.add(Calendar.DATE, amount);

        return cl.getTime();
    }

    /**
     *
     * @param date
     * @param amount
     * @param Typ
     * @return
     */
    public static synchronized Date addDynamic(Date date, Integer amount, Integer Typ) {
        cl.setTime(date);
        cl.add(Typ, amount);

        return cl.getTime();
    }

    /**
     *
     * @param date
     * @param amount
     * @return
     */
    public static synchronized Date addYears(Date date, int amount) {
        cl.setTime(date);
        cl.add(Calendar.YEAR, amount);

        return cl.getTime();
    }

    /**
     * Get DAYS difference
     *
     * @param date1
     * @param date2
     * @return
     */
    public static synchronized Integer getDifferenceBetween(Date date1, Date date2) {

        if (date1.after(date2)) {
            Date swap = date1;
            date1 = date2;
            date2 = swap;
        }

        Calendar d1 = Calendar.getInstance();
        d1.setTime(date1);

        Calendar d2 = Calendar.getInstance();
        d2.setTime(date2);

        int days = d2.get(java.util.Calendar.DAY_OF_YEAR)
                - d1.get(java.util.Calendar.DAY_OF_YEAR);
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
     * Returns the same date , one second before the next
     * day
     *
     * @param date
     * @return
     */
    public static synchronized Date getEndOfDay(Date date) {
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
     * Returns the same date, first millisecond
     *
     * @param date
     * @return
     */
    public static synchronized Date getStartOfDay(Date date) {
        Calendar calendar = cl;
        synchronized (calendar) {
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 1);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            return calendar.getTime();
        }
    }

    /**
     * Returns the same date, first millisecond of the year
     *
     * @param date
     * @return
     */
    public static synchronized Date getStartOfYear(Date date) {
        Calendar calendar = cl;
        synchronized (calendar) {
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 1);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.MONTH, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            return calendar.getTime();
        }
    }

    /**
     * Returns the same date, last millisecond of the year
     *
     * @param date
     * @return
     */
    public static synchronized Date getEndOfYear(Date date) {
        Calendar calendar = cl;
        synchronized (calendar) {
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MILLISECOND, 999);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            return calendar.getTime();
        }
    }

    /**
     * Returns the same date, first Day of Week
     *
     * @param date
     * @return
     */
    public static synchronized Date getStartOfWeek(Date date) {
        Calendar calendar = cl;
        synchronized (calendar) {
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 1);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            return calendar.getTime();
        }
    }

    /**
     * Returns the same date, last Day of Week
     *
     * @param date
     * @return
     */
    public static synchronized Date getEndOfWeek(Date date) {
        Calendar calendar = cl;
        synchronized (calendar) {
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 1);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);       
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            calendar.add(Calendar.DAY_OF_MONTH, 6);
            return calendar.getTime();
        }
    }

     /**
     * Returns the same date, first Day of Week
     *
     * @param date
     * @return
     */
    public static synchronized Date getStartOfQuarter(Date date) {
        Calendar calendar = cl;
        synchronized (calendar) {
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 1);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)/3 +1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            return calendar.getTime();
        }
    }

    /**
     * Returns the same date, last Day of Week
     *
     * @param date
     * @return
     */
    public static synchronized Date getEndOfQuarter(Date date) {
        Calendar calendar = cl;
        synchronized (calendar) {
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 1);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)/3 +4);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            return calendar.getTime();
        }
    }
    
    /**
     * Returns the same date, first millisecond
     *
     * @param date
     * @return
     */
    public static synchronized Date getStartOfMonth(Date date) {
        Calendar calendar = cl;
        synchronized (calendar) {
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 1);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            return calendar.getTime();
        }
    }

    /**
     * Returns the same date, last millisecond of the month
     *
     * @param date
     * @return
     */
    public static synchronized Date getEndOfMonth(Date date) {
        Calendar calendar = cl;
        synchronized (calendar) {
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MILLISECOND, 999);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            return calendar.getTime();
        }
    }

    /**
     * Quarter as 1,2,3,4
     *
     * @return
     */
    public static synchronized int getQuarter() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int month = cal.get(Calendar.MONTH);
        /* 0 through 11 */
        int quarter = (month / 3) + 1;
        return quarter;
    }

    /**
     * Quarter of a given date as 1, 2, 3, 4
     *
     * @param date
     * @return Quarter as 1, 2, 3, 4
     */
    public static synchronized int getQuarter(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);
        /* 0 through 11 */
        int quarter = (month / 3) + 1;
        return quarter;
    }

    /**
     * The current date in the default, localized format
     *
     * @return
     */
    public static synchronized String getTodayDefDate() {
        return DE_DATE_FORMAT.format(new Date());
    }

    /**
     * The current date in SQL format
     *
     * @return
     */
    public static synchronized String getTodayDBDate() {
        return DB_DATE_FORMAT.format(new Date());
    }

    /**
     *
     * @param date
     * @return
     */
    public static synchronized Date addYear(Date date) {
        return addYears(date, 1);
    }

    /**
     *
     * @param date
     * @return The next month
     */
    public static synchronized Date addMonth(Date date) {
        return addMonths(date, 1);
    }

    /**
     *
     * @param date
     * @param amount
     * @return The next month
     */
    public static synchronized Date addMonths(Date date, int amount) {
        Calendar cal = DateConverter.cl;

        synchronized (cal) {
            cal.setTime(date);
            cal.add(Calendar.MONTH, amount);

            return cal.getTime();
        }
    }

    /**
     *
     * @param date
     * @return End of the quarter
     */
    public static synchronized Date addQuarter(Date date) {
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
    public static synchronized Date addDay(Date date) {
        return addDays(date, 1);
    }

    /**
     *
     * @param date
     * @return SQL conform date String
     */
    public static synchronized String getSQLDateString(Date date) {
        return DB_DATE_FORMAT.format(date);
    }

    /**
     *
     * @param date
     * @return Default date
     */
    public static synchronized String getDefDateString(Date date) {
        if (date == null) {
            return "";
        } else {
            return DEF_DATE_FORMAT.format(date);
        }
    }

    /**
     *
     * @param datum
     * @return
     */
    public static synchronized String getDay(Date datum) {
        return DE_DATE_FORMAT.format(datum);
    }

    /**
     *
     * @param date
     * @return
     */
    public static synchronized String getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return String.valueOf(cal.get(Calendar.MONTH) + 1);
    }

    /**
     *
     * @param date
     * @return
     */
    public static synchronized String getMonthName(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return months[cal.get(Calendar.MONTH)];
    }

    /**
     *
     * @param date
     * @return
     */
    public static synchronized String getYearName(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return String.valueOf(cal.get(Calendar.YEAR));
    }

    /**
     *
     * @param date
     * @return
     */
    public static synchronized String getDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Converts formated Default date (dd.mm.yyyy) or
     * variants or yyyy-mm-dd hh.mm.ss[.nnnnnn] - SQL DATE
     * Timestamp
     *
     * to a Date object.
     *
     * @param date
     * @return Parsed date
     */
    public static synchronized Date getDate(String date) {
        Date DATE = null;
        for (DateFormat d : DATE_FORMATS) {
            try {
                DATE = d.parse(date);
                return DATE;
            } catch (ParseException ex) {
            }
        }
        if (additionalFormats.isEmpty()) {
            buildFormats();
        }

        if (DATE == null) {
            for (DateFormat d : additionalFormats) {
                try {
                    DATE = d.parse(date);
                    return DATE;
                } catch (ParseException ex) {
                }
            }
        }

        if (DATE == null) {
            Log.Debug(DateConverter.class, "String not parseable to a date: " + date);
        }
        return DATE;
    }
    static final List<DateFormat> additionalFormats = new Vector<DateFormat>();

    /**
     * Try to parse the given object to a date
     *
     * @param object
     * @return
     */
    public static synchronized Date getDate(Object object) {
        if (object instanceof Date) {
            return (Date) object;
        } else if (object instanceof java.sql.Date) {
            return new Date(((java.sql.Date) object).getTime());
        } else {
            return getDate(object.toString());
        }
    }

    /**
     *
     * @return
     */
    public static synchronized String getYear() {
        return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    }

    /**
     *
     * @return
     */
    public static synchronized String getDayOfMonth() {
        String m = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        if (m.length() == 1) {
            m = "0" + m;
        }
        return m;
    }

    /**
     *
     * @return
     */
    public static synchronized String getDayMonthAndYear() {
        return DateConverter.getDefDateString(new Date());
    }

    /**
     *
     * @return
     */
    public static synchronized String getMonth() {
        String m = String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1);
        if (m.length() == 1) {
            m = "0" + m;
        }
        return m;
    }

    /**
     *
     * @return
     */
    public static synchronized String getMonthName() {
        Calendar cal = Calendar.getInstance();
        return months[cal.get(Calendar.MONTH)];
    }

    /**
     *
     * @param df
     */
    public static synchronized void setDefaultFormat(DateFormat df) {
        DEF_DATE_FORMAT = df;
    }

    /**
     *
     * @return
     */
    public static synchronized String getDefaultFormatString() {
        if (DEF_DATE_FORMAT instanceof SimpleDateFormat) {
            return ((SimpleDateFormat) DEF_DATE_FORMAT).toPattern();
        } else {
            return "dd.MM.yyyy";
        }
    }

    private static synchronized void buildFormats() {
        Locale[] locales = DateFormat.getAvailableLocales();
        for (int i = 0; i < locales.length; i++) {
            Locale locale = locales[i];
            additionalFormats.addAll(Arrays.asList(new DateFormat[]{
                DateFormat.getDateInstance(DateFormat.SHORT, locale),
                DateFormat.getDateInstance(DateFormat.MEDIUM, locale),
                DateFormat.getDateInstance(DateFormat.LONG, locale),
                DateFormat.getDateInstance(DateFormat.FULL, locale),
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, locale),
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale),
                DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, locale),
                DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM, locale),
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale),
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale),
                DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale),
                DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT, locale),
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL, locale),
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL, locale),
                DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.FULL, locale),
                DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, locale),
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL, locale),
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL, locale),
                DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.FULL, locale),
                DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, locale)
            }));
        }
    }

    /**
     * Generates a random date, which is not in the future
     *
     * @return
     */
    public static Date getRandomDate() {
        return new RandomDate(new vTimeframe(new Date(0), new Date()));
    }

    /**
     *
     * @return 01092010
     */
    public static String getDateNumeric() {
        String year = getYear();
        String month = getMonth();
        String day = getDayOfMonth();
        String dn = year + month + day;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("item.date.locale") == null) {
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("item.date.locale", Locale.getDefault().toString());
        }
        try {
            Locale l = TypeConversion.stringToLocale(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("item.date.locale"));
            if (l.equals(Locale.GERMAN) || l.equals(Locale.GERMANY)) {
                dn = day + month + year;
            }
        } catch (Exception e) {
            Log.Debug(e);
        } finally {
            return dn;
        }
    }
}
