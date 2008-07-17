/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mp4.utils.zahlen;

import java.text.ParseException;
import mp3.classes.layer.Popup;
import mp3.classes.utils.Log;


/**
 *
 * @author Andreas
 */
public class FormatNumber {

    public static String formatDezimal(Double number) {
        java.text.DecimalFormat n = new java.text.DecimalFormat("#,##0.00;(#,##0.00)");
        n.setMaximumFractionDigits(2);
        return n.format(number);
    }

    public static Double parseDezimal(String number) {
        java.text.DecimalFormat n = new java.text.DecimalFormat("#,##0.00;(#,##0.00)");
        n.setMaximumFractionDigits(2);
        try {
            return n.parse(number).doubleValue();
        } catch (ParseException ex) {
            Popup.notice("Ungültiger Wert: " + number);
            Log.Debug(ex.getMessage());
            return null;
        }
    }

    static String formatDezimal(Float number) {
        java.text.DecimalFormat n = new java.text.DecimalFormat("#,##0.00;(#,##0.00)");
        n.setMaximumFractionDigits(2);
        return n.format(number);
    }
}
