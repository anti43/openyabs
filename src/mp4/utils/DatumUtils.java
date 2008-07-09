/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mp4.utils;

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
public class DatumUtils {

    private static Calendar cl = Calendar.getInstance();
    //   yyyy-mm-dd hh.mm.ss[.nnnnnn] - SQL DATE Timestamp
    public static DateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
    public static DateFormat DEF_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private static String NULL_DATE_STRING = "00.00.0000";
    private static Date NULL_DATE = null;

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
        }
        return NULL_DATE_STRING;
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
        }
        return NULL_DATE_STRING;
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
        }

        return NULL_DATE;
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
        }

        return NULL_DATE;
    }

    /**
     * 
     * @param date
     * @return Default date (dd.mm.yyyy)
     */
    public static Date getDate(String date) {
        try {

            return DEF_DATE_FORMAT.parse(date);
        } catch (ParseException ex) {
            Log.Debug(ex.getMessage());
        }

        return NULL_DATE;
    }
}
