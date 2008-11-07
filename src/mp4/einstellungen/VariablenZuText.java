/*
 * 
 * 
 */
package mp4.einstellungen;

import java.util.Date;
import mp4.items.People;
import mp4.items.Rechnung;
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
    public static final String MONTH = "\\{MONAT\\}";
    public static final String MONTH_NAME = "\\{MONAT_NAME\\}";
    public static final String YEAR = "\\{JAHR\\}";
    public static final String DAY = "\\{TAG\\}";

    public static String parseText(String text, Object[] object) {

        for (int i = 0; i < object.length; i++) {
            if (object[i].getClass().isInstance(new mp4.items.Rechnung())) {
                if (object[i] != null) {
                    text = parseRechnung(text, (mp4.items.Rechnung) object[i]);
                }
            } else if (object[i].getClass().isInstance(new mp4.items.Kunde()) ||
                    object[i].getClass().isInstance(new mp4.items.Lieferant()) ||
                    object[i].getClass().isInstance(new mp4.items.Hersteller())) {
                if (object[i] != null) {
                    text = parseKunde(text, (mp4.items.People) object[i]);
                }
            } else if(object[i].getClass().isInstance(new Date()) ){
                text =  parseDatum(text,(Date) object[i] );
            }
        }

        return text;
    }

    private static String parseKunde(String text, People kontakt) {
        return text.replaceAll(VariablenZuText.KUNDE_NAME, kontakt.getName()).
                replaceAll(VariablenZuText.KUNDE_NUMMER, kontakt.getNummer()).
                replaceAll(VariablenZuText.KUNDE_ANREDE, kontakt.getAnrede()).
                replaceAll(VariablenZuText.KUNDE_VORNAME, kontakt.getVorname());

    }

    private static String parseRechnung(String text, Rechnung rechnung) {
        return text.replaceAll(VariablenZuText.RECHNUNG_NUMMER, rechnung.getRechnungnummer()).
                replaceAll(VariablenZuText.RECHNUNG_DATUM, DateConverter.getDefDateString(rechnung.getDatum())).
                replaceAll(VariablenZuText.RECHNUNG_AF_DATUM, DateConverter.getDefDateString(rechnung.getAusfuehrungsDatum()));

    }
    
    private static String parseDatum(String text, Date datum){
    
        text = text.replaceAll(YEAR, DateConverter.getYear(datum));
        text = text.replaceAll(MONTH, DateConverter.getMonth(datum));
        text = text.replaceAll(MONTH_NAME, DateConverter.getMonthName(datum));
        text = text.replaceAll(DAY, DateConverter.getDayOfMonth(datum));
    
        return text;
    }
}
