/*
 * 
 * 
 */
package mp4.utils.zahlen;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import mp4.logs.*;
import mp4.einstellungen.Einstellungen;

/**
 *
 * @author Andreas
 */
public class FormatNumber {

    public static boolean checkDezimal(String text) {
        if (parseDezimal(text) == null) {
            return false;
        } else {
            return true;
        }
    }

    public static String formatLokalTax(double hauptsteuersatz) {
        return String.valueOf(((int)hauptsteuersatz));
    }

    public static NumberFormat getDefaultDecimalFormat() {
        return new java.text.DecimalFormat("#,##0.00;");
    }

    public static String formatDezimal(Double number) {
        java.text.DecimalFormat n = new java.text.DecimalFormat("#,##0.00;");
        n.setMaximumFractionDigits(2);
        return n.format(round(number));
    }

    public static Double round(double number) {
        BigDecimal b = BigDecimal.valueOf(number);
        b = b.setScale(2, BigDecimal.ROUND_HALF_UP);
        return b.doubleValue();
    }

    public static Double parseDezimal(String number) {
        java.text.DecimalFormat n = new java.text.DecimalFormat("#,##0.00;");
        n.setMaximumFractionDigits(2);
        try {
            return n.parse(number).doubleValue();
        } catch (ParseException ex) {
            Log.Debug(FormatNumber.class, ex.getMessage());
            try {
                n.applyPattern("###,##0.00;");
                return n.parse(number).doubleValue();
            } catch (ParseException parseException) {
                Log.Debug(FormatNumber.class, parseException.getMessage());
                return null;
            }
        }
    }

    static String formatDezimal(Float number) {
        java.text.DecimalFormat n = new java.text.DecimalFormat("#,##0.00;(#,##0.00)");
        n.setMaximumFractionDigits(2);
        return n.format(round(Double.valueOf(number)));
    }

    public static String formatLokalCurrency(Double betrag) {
        NumberFormat n = NumberFormat.getCurrencyInstance(Einstellungen.instanceOf().getLocale());
        return n.format(round(betrag));
    }

    public static String formatPercent(Object obj) {
        return obj.toString() + "%";
    }
}
