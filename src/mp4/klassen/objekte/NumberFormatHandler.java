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
package mp4.klassen.objekte;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import mp3.classes.interfaces.Countable;
import mp4.einstellungen.Programmdaten;
import mp4.utils.datum.DateConverter;

/**
 *
 * @author Andreas
 */
public class NumberFormatHandler {

    public final String MONTH = "\\{MONAT\\}";
    public final String MONTH_NAME = "\\{MONAT_NAME\\}";
    public final String YEAR = "\\{JAHR\\}";
    public final String DAY = "\\{TAG\\}";
    public final String COUNT = "00000";
    public final String SEP = "&!";
    private NumberFormat formatter;
    ;
    private String format;
    private Countable type;
    private Integer mode = 0;
    private Date date;
   
    /*
     * 
     * Beispiel:
     * {JAHR}-{MONAT_NAME}-{TAG}&!00000&!3
     * 
     * Ergebnis:
     * 
     * 2008-Januar-000231
     * 
     */

    public NumberFormatHandler(Countable type, Date date) {

        this.type = type;
        this.date = date;


    }

    public void save(String formatString) {
        if (type.getClass().isInstance(new mp4.klassen.objekte.Rechnung())) {
            Programmdaten.instanceOf().setRECHNUNG_NUMMER_FORMAT(formatString);
        } else if (type.getClass().isInstance(new mp4.klassen.objekte.Angebot())) {
            Programmdaten.instanceOf().setANGEBOT_NUMMER_FORMAT(formatString);
        }
    }


    public NumberFormat parseFormat(String format) {
        format = format.replaceAll(YEAR, DateConverter.getYear(date));
        format = format.replaceAll(MONTH, DateConverter.getMonth(date));
        format = format.replaceAll(MONTH_NAME, DateConverter.getMonthName(date));
        format = format.replaceAll(DAY, DateConverter.getDayOfMonth(date));

        String[] string = format.split(SEP);
        mode = Integer.valueOf(string[2]);

//        if (mode != 0) {
            return new DecimalFormat("'" + string[0] + "'" + string[1]);
//        } else {
//            return new DecimalFormat(string[1]);
//        }
    }

    public void format() {
       
            if (type.getClass().isInstance(new mp4.klassen.objekte.Rechnung())) {
                processRechnungType();
            } else if (type.getClass().isInstance(new mp4.klassen.objekte.Angebot())) {
                processAngebotType();
            }
        
    }

    private void processAngebotType() {
        format = Programmdaten.instanceOf().getANGEBOT_NUMMER_FORMAT();
        setFormatter(parseFormat(format));
    }

    private void processRechnungType() {
        format = Programmdaten.instanceOf().getRECHNUNG_NUMMER_FORMAT();
        setFormatter(parseFormat(format));
    }

    public NumberFormat getFormatter() {
        return formatter;
    }

    public Integer getMode() {
        return mode;
    }

    public String getNextNumber() {

       if(formatter == null) format();
        Integer count = 0;

        switch (this.getMode()) {

            case 1:
                count = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(type.getTable()).selectCountBetween(DateConverter.getDate(DateConverter.getYear(type.getDatum())),
                        DateConverter.addYear(DateConverter.getDate(DateConverter.getYear(type.getDatum()))));
                break;

            case 2:
                count = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(type.getTable()).selectCountBetween(DateConverter.getDate(DateConverter.getMonth(type.getDatum())),
                        DateConverter.addMonth(DateConverter.getDate(DateConverter.getMonth(type.getDatum()))));
                break;

            case 3:
                count = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(type.getTable()).selectCountBetween(DateConverter.getDate(DateConverter.getDay(type.getDatum())),
                        DateConverter.addDay(DateConverter.getDate(DateConverter.getDay(type.getDatum()))));
                break;

            case 0:
                count = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(type.getTable()).getNextNumber(type.getCountColumn())-1;
                break;

            default:
                String.valueOf(mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(type.getTable()).selectCount("", null) + 1);
        }

        
        return this.getFormatter().format(count + 1);
    }

    public void setFormatter(NumberFormat formatter) {
        this.formatter = formatter;
    }
}
