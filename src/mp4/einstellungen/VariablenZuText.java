/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mp4.einstellungen;

import mp4.klassen.objekte.Customer;
import mp4.klassen.objekte.Rechnung;
import mp4.utils.datum.DateConverter;

/**
 *
 * @author anti43
 */
public class VariablenZuText {

    public static final String RECHNUNG_NUMMER = "\\{RECHNUNG_NUMMER\\}";
    public static final String RECHNUNG_DATUM = "\\{RECHNUNG_DATUM\\}";
    public static final String RECHNUNG_AF_DATUM = "\\{RECHNUNG_AF_DATUM\\}";
    public static final String KUNDE_NUMMER = "\\{KUNDE_NUMMER\\}";
    public static final String KUNDE_NAME = "\\{KUNDE_NAME\\}";
    public static final String KUNDE_ANREDE = "\\{KUNDE_ANREDE\\}";
    public static final String KUNDE_VORNAME = "\\{KUNDE_VORNAME\\}";

    public static String parseText(String text, Object[] object) {

        for (int i = 0; i < object.length; i++) {
            if (object[i].getClass().isInstance(new mp4.klassen.objekte.Rechnung())) {
                if(object[i]!=null)text = parseRechnung(text, (mp4.klassen.objekte.Rechnung) object[i]);
            } else if (object[i].getClass().isInstance(new mp4.klassen.objekte.Customer())) {
                if(object[i]!=null)text = parseKunde(text, (mp4.klassen.objekte.Customer) object[i]);
            }
        }

        return text;
    }

    private static String parseKunde(String text, Customer customer) {
          return text.replaceAll(VariablenZuText.KUNDE_NAME, customer.getName()).
                replaceAll(VariablenZuText.KUNDE_NUMMER  , customer.getKundennummer()).
                replaceAll(VariablenZuText.KUNDE_ANREDE  , customer.getAnrede()).
                replaceAll(VariablenZuText.KUNDE_VORNAME , customer.getVorname());
              
    }

    private static String parseRechnung(String text, Rechnung rechnung) {
        return text.replaceAll(VariablenZuText.RECHNUNG_NUMMER, rechnung.getRechnungnummer()).
                replaceAll(VariablenZuText.RECHNUNG_DATUM, DateConverter.getDefDateString(rechnung.getDatum())).
                replaceAll(VariablenZuText.RECHNUNG_AF_DATUM, DateConverter.getDefDateString(rechnung.getAusfuehrungsDatum()));
   
    }
}
