/*
 *  This file is part of MP by anti43 /GPL.
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author anti
 */
public class DateConverter {

    public static String[] months = {"Jan", "Feb",
            "Mar", "Aprl", "May", "Jun", "Jul",
            "Aug", "Sep", "Oct", "Nov",
            "Dec"};

    private static Calendar cl = Calendar.getInstance();
    //   yyyy-mm-dd hh.mm.ss[.nnnnnn] - SQL DATE Timestamp
    public static final DateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DateFormat DE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
    public static final DateFormat DE_DATE_FORMAT_DAY = new SimpleDateFormat("dd", Locale.GERMAN);
    public static final DateFormat DE_DATE_FORMAT_SHORTYEAR = new SimpleDateFormat("dd.MM.yy", Locale.GERMAN);
    public static final DateFormat DE_DATE_FORMAT_SHORTMONTH = new SimpleDateFormat("dd.M.yyyy", Locale.GERMAN);
    public static final DateFormat DE_DATE_FORMAT_NODAY_SHORTMONTH_SHORTYEAR = new SimpleDateFormat("M.yy", Locale.GERMAN);
    public static final DateFormat DE_DATE_FORMAT_NODAY_SHORTMONTH_YEAR = new SimpleDateFormat("M.yyyy", Locale.GERMAN);
    public static final DateFormat DE_DATE_FORMAT_SHORTMONTH_SHORTYEAR = new SimpleDateFormat("dd.M.yy", Locale.GERMAN);
    public static final DateFormat DE_DATE_FORMAT_NODAY_MONTH_YEAR = new SimpleDateFormat("MM.yyyy", Locale.GERMAN);
    public static final DateFormat DE_DATE_FORMAT_YEAR = new SimpleDateFormat("yyyy", Locale.GERMAN);
    public static final DateFormat DE_DATE_FORMAT_MONTH = new SimpleDateFormat("MMMM", Locale.GERMAN);
    public static final DateFormat DE_DATE_FORMAT_NODAY_LONGMONTH_YEAR = new SimpleDateFormat("MMMM.yyyy", Locale.GERMAN);
    public static final DateFormat ENG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final DateFormat[] DE_DATES = new DateFormat[]{DB_DATE_FORMAT, ENG_DATE_FORMAT, DE_DATE_FORMAT_NODAY_LONGMONTH_YEAR,
        DE_DATE_FORMAT, DE_DATE_FORMAT_SHORTYEAR, DE_DATE_FORMAT_SHORTMONTH, DE_DATE_FORMAT_NODAY_SHORTMONTH_SHORTYEAR,
        DE_DATE_FORMAT_SHORTMONTH_SHORTYEAR, DE_DATE_FORMAT_NODAY_MONTH_YEAR, DE_DATE_FORMAT_YEAR, DE_DATE_FORMAT_NODAY_SHORTMONTH_YEAR
    };
    public static final DateFormat DE_FULL_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyy - HH:mm:ss");

    public static Date addDays(Date date, Integer add) {
        cl.setTime(date);
        cl.add(Calendar.DATE, add);

        return cl.getTime();
    }

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

    public static String getFullDefDateString(Date date) {
        return DE_FULL_DATE_FORMAT.format(new Date());
    }

    public static Date getSylvesterOf(Date date) {
        return DateConverter.getDate(DE_DATE_FORMAT_YEAR.format(date));
    }

    public static String getTodayDefDate() {
        return DE_DATE_FORMAT.format(new Date());
    }

    public static String getTodayDBDate() {
        return DB_DATE_FORMAT.format(new Date());
    }

    public static Date addYear(Date date) {

        cl.setTime(date);
        cl.add(Calendar.YEAR, 1);

        return cl.getTime();
    }

    /**
     * 
     * @param date
     * @return The next month
     */
    public static Date addMonth(Date date) {

        cl.setTime(date);
        cl.add(Calendar.MONTH, 1);

        return cl.getTime();
    }

    /**
     * 
     * @param date
     * @return The next day
     */
    public static Date addDay(Date date) {

        cl.setTime(date);
        cl.add(Calendar.DATE, 1);

        return cl.getTime();
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
     * @return Default date (dd.mm.yyyy)
     */
    public static String getDefDateString(Date date) {
        return DE_DATE_FORMAT.format(date);
    }

    public static String getYear(Date datum) {
        return DE_DATE_FORMAT_YEAR.format(datum);
    }

    public static String getMonth(Date datum) {
        return DE_DATE_FORMAT_NODAY_MONTH_YEAR.format(datum);
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
        for (int i = 0; i < DE_DATES.length; i++) {
            try {
                DATE = DE_DATES[i].parse(date);
                return DATE;
            } catch (ParseException ex) {
            }
        }
        return DATE;
    }

    public static String getTodayDefMonth() {
        return DE_DATE_FORMAT_NODAY_MONTH_YEAR.format(new Date());
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

    public static Date getMonthAndYear() {
        try {
            return DateConverter.DE_DATE_FORMAT_NODAY_MONTH_YEAR.parse(DateConverter.getMonth() + "." + DateConverter.getYear());
        } catch (ParseException ex) {
            return null;
        }
    }
}
