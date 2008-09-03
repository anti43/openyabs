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
package mp4.items.handler;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import mp3.classes.utils.Log;
import mp4.einstellungen.Programmdaten;
import mp4.interfaces.Countable;
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
    private String format;
    private Countable type;
    private Integer mode = 0;
    private Date date;
    private static String startwert = null;

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

    public String[] getCurrentFormat() {
        if (type.getClass().isInstance(new mp4.items.Rechnung())) {
             return Programmdaten.instanceOf().getRECHNUNG_NUMMER_FORMAT().split("&!");
        } else if (type.getClass().isInstance(new mp4.items.Angebot())) {
             return Programmdaten.instanceOf().getANGEBOT_NUMMER_FORMAT().split("&!");
        } else if (type.getClass().isInstance(new mp4.items.Dienstleistung())) {
             return Programmdaten.instanceOf().getSERVICES_NUMMER_FORMAT().split("&!");
        } else if (type.getClass().isInstance(new mp4.items.Customer())) {
             return Programmdaten.instanceOf().getCUSTOMER_NUMMER_FORMAT().split("&!");
        } else if (type.getClass().isInstance(new mp4.items.Lieferant())) {
             return Programmdaten.instanceOf().getSUPPLIER_NUMMER_FORMAT().split("&!");
        } else if (type.getClass().isInstance(new mp4.items.Hersteller())) {
             return Programmdaten.instanceOf().getMANUFACTURER_NUMMER_FORMAT().split("&!");
        } else if (type.getClass().isInstance(new mp4.items.Product())) {
             return Programmdaten.instanceOf().getPRODUCT_NUMMER_FORMAT().split("&!");
        }
       return null;
    }

    public void save(String formatString) {
        if (type.getClass().isInstance(new mp4.items.Rechnung())) {
            Programmdaten.instanceOf().setRECHNUNG_NUMMER_FORMAT(formatString);
        } else if (type.getClass().isInstance(new mp4.items.Angebot())) {
            Programmdaten.instanceOf().setANGEBOT_NUMMER_FORMAT(formatString);
        } else if (type.getClass().isInstance(new mp4.items.Dienstleistung())) {
            Programmdaten.instanceOf().setSERVICES_NUMMER_FORMAT(formatString);
        } else if (type.getClass().isInstance(new mp4.items.Customer())) {
            Programmdaten.instanceOf().setCUSTOMER_NUMMER_FORMAT(formatString);
        } else if (type.getClass().isInstance(new mp4.items.Lieferant())) {
            Programmdaten.instanceOf().setSUPPLIER_NUMMER_FORMAT(formatString);
        } else if (type.getClass().isInstance(new mp4.items.Hersteller())) {
            Programmdaten.instanceOf().setMANUFACTURER_NUMMER_FORMAT(formatString);
        } else if (type.getClass().isInstance(new mp4.items.Product())) {
            Programmdaten.instanceOf().setPRODUCT_NUMMER_FORMAT(formatString);
        }
    }

    public NumberFormat parseFormat(String format) {
        format = format.replaceAll(YEAR, DateConverter.getYear(date));
        format = format.replaceAll(MONTH, DateConverter.getMonth(date));
        format = format.replaceAll(MONTH_NAME, DateConverter.getMonthName(date));
        format = format.replaceAll(DAY, DateConverter.getDayOfMonth(date));

        String[] string = format.split(SEP);

        if (string.length == 1) {
            mode = 0;
            return new DecimalFormat(string[1]);
        } else if (string.length == 2) {
            try {
                mode = Integer.valueOf(string[1]);
                return new DecimalFormat(string[0]);
            } catch (Exception ex) {
                mode = 0;
                return new DecimalFormat("'" + string[0] + "'" + string[1]);
            }
        } else {
            try {
                mode = Integer.valueOf(string[2]);
                return new DecimalFormat("'" + string[0] + "'" + string[1]);
            } catch (NumberFormatException numberFormatException) {
                mode = 0;
                return new DecimalFormat("'" + string[0] + "'" + string[1]);
            }
        }
    }

    public void format() {

        if (type.getClass().isInstance(new mp4.items.Rechnung())) {
            processRechnungType();
        } else if (type.getClass().isInstance(new mp4.items.Angebot())) {
            processAngebotType();
        } else if (type.getClass().isInstance(new mp4.items.Dienstleistung())) {
            processServicesType();
        } else if (type.getClass().isInstance(new mp4.items.Customer())) {
            processCustomerType();
        } else if (type.getClass().isInstance(new mp4.items.Lieferant())) {
            processSupplierType();
        } else if (type.getClass().isInstance(new mp4.items.Hersteller())) {
            processManufacturerType();
        } else if (type.getClass().isInstance(new mp4.items.Product())) {
            processProductType();
        }

    }

    public void setStartWert(String wert) {
        NumberFormatHandler.startwert = wert;
    }

    private void processAngebotType() {
        format = Programmdaten.instanceOf().getANGEBOT_NUMMER_FORMAT();
        setFormatter(parseFormat(format));
    }

    private void processCustomerType() {
        format = Programmdaten.instanceOf().getCUSTOMER_NUMMER_FORMAT();
        setFormatter(parseFormat(format));
    }

    private void processProductType() {
        format = Programmdaten.instanceOf().getPRODUCT_NUMMER_FORMAT();
        setFormatter(parseFormat(format));
    }

    private void processSupplierType() {
        format = Programmdaten.instanceOf().getSUPPLIER_NUMMER_FORMAT();
        setFormatter(parseFormat(format));
    }

    private void processManufacturerType() {
        format = Programmdaten.instanceOf().getMANUFACTURER_NUMMER_FORMAT();
        setFormatter(parseFormat(format));
    }

    private void processRechnungType() {
        format = Programmdaten.instanceOf().getRECHNUNG_NUMMER_FORMAT();
        setFormatter(parseFormat(format));
    }

    private void processServicesType() {
        format = Programmdaten.instanceOf().getSERVICES_NUMMER_FORMAT();
        setFormatter(parseFormat(format));
    }

    public NumberFormat getFormatter() {
        return formatter;
    }

    public Integer getMode() {
        return mode;
    }

    public String getNextNumber() {

        if (NumberFormatHandler.startwert == null) {
            if (formatter == null) {
                format();
            }
            Integer count = 0;

            Log.Debug("Number Parser mode: " + this.getMode());
            Log.Debug("Number Parser format: " + this.getFormatter().format(1d));

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

                case 4:
                    count = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(type.getTable()).getNextIndexOfStringCol(type.getCountColumn()) - 1;
                    break;

                case 0:
                    count = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(type.getTable()).getCount();
                    break;

                default:
                    return String.valueOf(mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(type.getTable()).selectCount("", null) + 1);
            }

            return this.getFormatter().format(count + 1);
        } else {
            Log.Debug("Number Parser uses: " + startwert);
            startwert = checkValue(startwert);
            String tmp = startwert;
            startwert = null;
            return tmp;
        }
    }

    public void setFormatter(NumberFormat formatter) {
        this.formatter = formatter;
    }

    public boolean exists(String value) {

        String[][] i = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(type.getTable()).select("id", new String[]{type.getCountColumn(), value, "'"});

        if (i != null && i.length > 0) {
            return true;
        } else {
            return false;
        }
    }

    public String checkValue(String value) {
        if (exists(value)) {
            return checkValue(value + 1);
        } else {
            return value;
        }
    }
}
