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
package mp4.utils.datum;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import mp3.classes.utils.Log;

/**
 *
 * @author anti
 */
public class DateConverter {

    private static Calendar cl = Calendar.getInstance();
    //   yyyy-mm-dd hh.mm.ss[.nnnnnn] - SQL DATE Timestamp
    public static DateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
    public static DateFormat DEF_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private static String NULL_DATE_STRING = "00.00.0000";
    private static Date NULL_DATE = null;

    public static String getTodayDefDate() {
        return DEF_DATE_FORMAT.format(new Date());
    }

    public static String getTodayDBDate() {
        return DB_DATE_FORMAT.format(new Date());
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
     * @return The next month
     */
    public static String addMonth(String date) {
        try {

            Date dat = DEF_DATE_FORMAT.parse(date);

            cl.setTime(dat);
            cl.add(Calendar.MONTH, 1);

            return cl.getTime().toString();
        } catch (ParseException ex) {
            Log.Debug(ex.getMessage());
            return null;
        }
    }

    /**
     * 
     * @param date
     * @return The next day
     */
    public static String addDay(String date) {

        try {

            Date dat = DEF_DATE_FORMAT.parse(date);

            cl.setTime(dat);
            cl.add(Calendar.DATE, 1);

            return cl.getTime().toString();
        } catch (ParseException ex) {
            Log.Debug(ex.getMessage());
            return null;
        }

    }

    /**
     * 
     * @param date
     * @return SQL conform date
     */
    public static Date getSQLDate(Date date) {
        try {

            return DB_DATE_FORMAT.parse(DB_DATE_FORMAT.format(date));
        } catch (ParseException ex) {
            Log.Debug(ex.getMessage());
            return null;
        }
    }

    /**
     * 
     * @param date
     * @return SQL conform date String
     */
    public static String getSQLDateString(Date date) {
        try {

            return DB_DATE_FORMAT.parse(DB_DATE_FORMAT.format(date)).toString();
        } catch (ParseException ex) {
            Log.Debug(ex.getMessage());
            return null;
        }

    }

    /**
     * 
     * @param date
     * @return Default date (dd.mm.yyyy)
     */
    public static String getDefDateString(Date date) {
        try {

            return DEF_DATE_FORMAT.parse(DEF_DATE_FORMAT.format(date)).toString();
        } catch (ParseException ex) {
            Log.Debug(ex.getMessage());
            return null;
        }
    }

    /**
     * 
     * @param date
     * @return Default date (dd.mm.yyyy)
     */
    public static Date getDefDate(Date date) {
        try {

            return DEF_DATE_FORMAT.parse(DEF_DATE_FORMAT.format(date));
        } catch (ParseException ex) {
            Log.Debug(ex.getMessage());
            return null;
        }

    }

    /**
     * Converts formated 
     * Default date (dd.mm.yyyy)
     * or
     * yyyy-mm-dd hh.mm.ss[.nnnnnn] - SQL DATE Timestamp
     * 
     * to a Date object.
     * 
     * @param date
     * @return Parsed date
     */
    public static Date getDate(String date) {
        try {
            return DEF_DATE_FORMAT.parse(date);
        } catch (ParseException ex) {
            try {
                return DB_DATE_FORMAT.parse(date);
            } catch (ParseException ex1) {
                Log.Debug(ex.getMessage());
                return null;
            }
        }
    }
}
