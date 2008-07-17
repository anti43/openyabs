/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mp4.utils.zahlen;



/**
 *
 * @author Andreas
 */
public class FormatNumber {

  public static String formatLokal(Double number) {
        java.text.DecimalFormat n = new java.text.DecimalFormat();
        n.setMaximumFractionDigits(2);   //Nachkommastellen einstellen
        return n.format(number);
    }
}
