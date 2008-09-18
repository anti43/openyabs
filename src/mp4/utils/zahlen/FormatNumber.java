/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

    public static NumberFormat getDefaultDecimalFormat() {

        return new java.text.DecimalFormat("#,##0.00;(#,##0.00)");
    }

    public static String formatDezimal(Double number) {
        java.text.DecimalFormat n = new java.text.DecimalFormat("#,##0.00;(#,##0.00)");
        n.setMaximumFractionDigits(2);
        return n.format(round(number));
    }

    public static Double round(double number) {
//        System.out.print(number);
        BigDecimal b = BigDecimal.valueOf(number);
        b = b.setScale(2, BigDecimal.ROUND_HALF_UP);
        return b.doubleValue();
    }

    public static Double parseDezimal(String number) {
        java.text.DecimalFormat n = new java.text.DecimalFormat("#,##0.00;(#,##0.00)");
        n.setMaximumFractionDigits(2);
        try {
            return n.parse(number).doubleValue();
        } catch (ParseException ex) {
//            Popup.notice("Ungültiger Wert: " + number);
            Log.Debug(ex.getMessage());
            return null;
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
