/*
 * 
 *  *  This file is part of MP by anti43 /GPL.
 *  *  
 *  *      MP is free software: you can redistribute it and/or modify
 *  *      it under the terms of the GNU General Public License as published by
 *  *      the Free Software Foundation, either version 3 of the License, or
 *  *      (at your option) any later version.
 *  *  
 *  *      MP is distributed in the hope that it will be useful,
 *  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 * *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *      GNU General Public License for more details.
 *  *  
 *  *      You should have received a copy of the GNU General Public License
 *  *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package mp4.utils.tabellen.models;

import java.util.Date;
import java.util.Locale;
import mp4.items.Popup;
import mp4.datenbank.verbindung.ConnectionHandler;
import javax.swing.table.DefaultTableModel;
import mp4.globals.Constants;
import mp4.globals.Strings;

import mp4.logs.*;
import mp4.datenbank.verbindung.Query;
import mp4.einstellungen.Einstellungen;
import mp4.utils.datum.DateConverter;
import mp4.utils.datum.vTimeframe;
import mp4.utils.zahlen.FormatNumber;

/**
 *
 * @author anti43
 */
public class EURTableModel implements Constants, Strings, mp4.datenbank.installation.Tabellen {

    private Object[][] data;
    String[] head = new String[]{"Beschreibung", "Wert"};
    public static int MONAT = 0;
    public static int JAHR = 1;
    Double vorsteuerausgabe = 0d;
    Double gesamtausgabe = 0d;
    Double vorsteuereinnahme = 0d;
    Double gesamteinnahme = 0d;
    Double konto1110 = 0d; // Waren, Rohstoffe und Hilfsstoffe einschl. der Nebenkosten",
    Double konto1120 = 0d; // Bezogene Leistungen (z.B. Fremdleistungen)Double konto,
    Double konto1130 = 0d; // Ausgaben für eigenes Personal Double konto,
    Double konto1140 = 0d; // Aufwendungen für geringwertige WirtschaftsgüterDouble konto,
    Double konto1150 = 0d; // Miete / Pacht für Geschäftsräume und betrieblich genutzte GrundstückeDouble konto,
    Double konto1160 = 0d; // Sonstige Aufwendungen für betrieblich genutzte GrundstückeDouble konto,
    Double konto1170 = 0d; // Abziehbare Aufwendungen für ein häusliches ArbeitszimmerDouble konto,
    Double konto1180 = 0d; // Reisekosten,Aufwendungen für doppelte HaushaltsführungDouble konto,
    Double konto1190 = 0d; // Geschenke – abziehbarDouble konto,
    Double konto1200 = 0d; // Geschenke – nicht abziehbarDouble konto,
    Double konto1210 = 0d; // Bewirtung – abziehbarDouble konto,
    Double konto1220 = 0d; // Bewirtung – nicht abziehbarDouble konto,
    Double konto1230 = 0d; // Übrige BetriebsausgabenDouble konto,
    Double konto1240 = 0d; // Fortbildung und FachliteraturDouble konto,
    Double konto1250 = 0d; // Rechts- und Steuerberatung, BuchführungDouble konto,
    Double konto1260 = 0d; // Porto, Telefon, BüromaterialDouble konto,
    Double konto1270 = 0d; // An das Finanzamt gezahlte und ggf. verrechnete UmsatzsteuerDouble konto,
    Double konto2100 = 0d; // Betriebseinnahmen als umsatzsteuerlicher KleinunternehmerDouble konto,
    Double konto2110 = 0d; // Umsatzsteuerpflichtige BetriebseinnahmenDouble konto,
    Double konto2120 = 0d; // Sonstige Sach-, Nutzungs- und LeistungsentnahmenDouble konto,
    Double konto2130 = 0d; // Private Kfz-NutzungDouble konto,
    Double konto2140 = 0d; // Vom Finanzamt erstattete und ggf. verrechnete Umsatzsteuer";

    public EURTableModel() {
        new EURTableModel(DateConverter.getDate(DateConverter.getYear()), EURTableModel.JAHR);
    }

    public EURTableModel(Date date, int mode) {

        Locale.setDefault(Einstellungen.instanceOf().getLocale());
        data = new Object[EUR_TEMPLATE.length][2];

        for (int i = 0; i < EUR_TEMPLATE.length; i++) {

            data[i][0] = EUR_TEMPLATE[i];

        }
        vTimeframe timeframe = null;

        switch (mode) {
            case 0:
                timeframe = new vTimeframe(date, DateConverter.addMonth(date));
                break;
            case 1:
                timeframe = new vTimeframe(date, DateConverter.addYear(date));
                break;
        }

        Log.Debug(timeframe, true);


        Query q = ConnectionHandler.instanceOf().clone(TABLE_DUES);
        String[][] ausgaben = q.selectBetween("kontenid, preis, tax, datum", null, timeframe);

        Integer kontoID = 0;
        double nettobetrag = 0d;

        for (int h = 0; h < ausgaben.length; h++) {
            kontoID = 0;
            nettobetrag = 0d;

            try {
                kontoID = Integer.valueOf(ausgaben[h][0]);
                if (ausgaben[h][1] != null && Double.valueOf(ausgaben[h][1]) > 0) {
                    nettobetrag = (Double.valueOf(ausgaben[h][1]) / ((Double.valueOf(ausgaben[h][2]) / 100) + 1));
                    vorsteuerausgabe = vorsteuerausgabe + (Double.valueOf(ausgaben[h][1]) - nettobetrag);
                    gesamtausgabe = gesamtausgabe + Double.valueOf(ausgaben[h][1]);
                }

            } catch (NumberFormatException ex) {

                kontoID = 0;
                nettobetrag = 0d;
                Log.Debug(ex);
                Popup.error(ex.getMessage(), "Überprüfen Sie die angebenen Beträge.");
            }


            switch (kontoID) {


                case 1:
                case 2:
                case 3:
                    konto1110 = konto1110 + nettobetrag;
                    break;

                case 4:
                case 5:
                case 6:
                    konto1120 = konto1120 + nettobetrag;
                    break;

                case 7:
                    konto1130 = konto1130 + nettobetrag;
                    break;
                case 8:
                    konto1140 = konto1140 + nettobetrag;
                    break;
                case 9:
                    konto1150 = konto1150 + nettobetrag;
                    break;
                case 10:
                    konto1160 = konto1160 + nettobetrag;
                    break;
                case 11:
                    konto1170 = konto1170 + nettobetrag;
                    break;

                case 12:
                case 13:
                case 14:
                case 15:
                    konto1180 = konto1180 + nettobetrag;
                    break;

                case 16:
                    konto1190 = konto1190 + nettobetrag;
                    break;
                case 17:
                    konto1200 = konto1200 + nettobetrag;
                    break;
                case 18:
                    konto1210 = konto1210 + nettobetrag;
                    break;

                case 19:
                    konto1220 = konto1220 + nettobetrag;
                    break;

                case 20:
                case 21:
                case 22:
                    konto1230 = konto1230 + nettobetrag;
                    break;
                case 23:
                    konto1240 = konto1240 + nettobetrag;
                    break;
                case 24:
                    konto1250 = konto1250 + nettobetrag;
                    break;
                case 25:
                    konto1260 = konto1260 + nettobetrag;
                    break;
                case 26:
                    konto1270 = konto1270 + nettobetrag;
                    break;
                default:
                    Popup.error("Konto konnte nicht ermittelt werden: " + kontoID, "Es ist ein Fehler aufgetreten.");
            }

        }

        data[0][1] = FormatNumber.formatLokalCurrency(konto1110);
        data[1][1] = FormatNumber.formatLokalCurrency(konto1120);
        data[2][1] = FormatNumber.formatLokalCurrency(konto1130);
        data[3][1] = FormatNumber.formatLokalCurrency(konto1140);
        data[4][1] = FormatNumber.formatLokalCurrency(konto1150);
        data[5][1] = FormatNumber.formatLokalCurrency(konto1160);
        data[6][1] = FormatNumber.formatLokalCurrency(konto1170);
        data[7][1] = FormatNumber.formatLokalCurrency(konto1180);
        data[8][1] = FormatNumber.formatLokalCurrency(konto1190);
        data[9][1] = FormatNumber.formatLokalCurrency(konto1200);
        data[10][1] = FormatNumber.formatLokalCurrency(konto1210);
        data[11][1] = FormatNumber.formatLokalCurrency(konto1220);
        data[12][1] = FormatNumber.formatLokalCurrency(konto1230);
        data[13][1] = FormatNumber.formatLokalCurrency(konto1240);
        data[14][1] = FormatNumber.formatLokalCurrency(konto1250);
        data[15][1] = FormatNumber.formatLokalCurrency(konto1260);
        data[16][1] = FormatNumber.formatLokalCurrency(konto1270);

        data[18][1] = FormatNumber.formatLokalCurrency(vorsteuerausgabe);

        data[20][1] = FormatNumber.formatLokalCurrency(gesamtausgabe);


        q = ConnectionHandler.instanceOf().clone(TABLE_INCOME);
        String[][] einnahmen = q.selectBetween("kontenid, preis, tax, datum", null, timeframe);

        for (int h = 0; h < einnahmen.length; h++) {
            kontoID = 0;
            nettobetrag = 0d;

            try {

                kontoID = Integer.valueOf(einnahmen[h][0]);
                if (einnahmen[h][1] != null && Double.valueOf(einnahmen[h][1]) > 0) {
                    nettobetrag = (Double.valueOf(einnahmen[h][1]) / ((Double.valueOf(einnahmen[h][2]) / 100) + 1));
                    vorsteuereinnahme = vorsteuereinnahme + (Double.valueOf(einnahmen[h][1]) - nettobetrag);

                    gesamteinnahme = gesamteinnahme + Double.valueOf(einnahmen[h][1]);
                }

            } catch (NumberFormatException ex) {

                kontoID = 0;
                nettobetrag = 0d;
                Log.Debug(ex);
                Popup.error(ex.getMessage(), "Überprüfen Sie die angebenen Beträge. ");
            }


            switch (kontoID) {


                case 27:
                    konto2100 = konto2100 + nettobetrag;
                    break;
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                    konto2110 = konto2110 + nettobetrag;
                    break;

                case 33:
                case 34:
                case 35:
                    konto2120 = konto2120 + nettobetrag;
                    break;
                case 36:
                    konto2130 = konto2130 + nettobetrag;
                    break;
                case 37:
                    konto2140 = konto2140 + nettobetrag;
                    break;
                default:
                    Popup.error("Konto konnte nicht ermittelt werden: " + kontoID, "Es ist ein Fehler aufgetreten.");
            }



        }



//        kontenid, preis, tax, datumq.selectBetween("kontenid, preis, tax, datum", null, timeframe);
        q = ConnectionHandler.instanceOf().clone(TABLE_BILLS);
        String[][] rechnungen = q.selectBetween("id, gesamtpreis, gesamttax, datum", new String[]{"bezahlt", "1", "", "storno", "0", ""}, timeframe);
        kontoID = Einstellungen.instanceOf().getEinnahmeDefKonto().getId();

        for (int h = 0; h < rechnungen.length; h++) {
            nettobetrag = 0d;

            try {
                if (rechnungen[h][1] != null && Double.valueOf(rechnungen[h][1]) > 0) {
                    nettobetrag = (Double.valueOf(rechnungen[h][1]) / ((Double.valueOf(rechnungen[h][2]))));
                    vorsteuereinnahme = vorsteuereinnahme + (Double.valueOf(rechnungen[h][1]) - nettobetrag);
                    gesamteinnahme = gesamteinnahme + Double.valueOf(rechnungen[h][1]);
                }

            } catch (NumberFormatException ex) {

                kontoID = 0;
                nettobetrag = 0d;
                Log.Debug(ex);
                Popup.error(ex.getMessage(), "Überprüfen Sie die angebenen Beträge.");
            }


            switch (kontoID) {


                case 27:
                    konto2100 = konto2100 + nettobetrag;
                    break;
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                    konto2110 = konto2110 + nettobetrag;
                    break;

                case 33:
                case 34:
                case 35:
                    konto2120 = konto2120 + nettobetrag;
                    break;
                case 36:
                    konto2130 = konto2130 + nettobetrag;
                    break;
                case 37:
                    konto2140 = konto2140 + nettobetrag;
                    break;
                default:
                    Popup.error("Konto konnte nicht ermittelt werden: " + kontoID, "Es ist ein Fehler aufgetreten.");
            }



        }

        data[22][1] = FormatNumber.formatLokalCurrency(konto2100);
        data[23][1] = FormatNumber.formatLokalCurrency(konto2110);
        data[24][1] = FormatNumber.formatLokalCurrency(konto2120);
        data[25][1] = FormatNumber.formatLokalCurrency(konto2130);
        data[26][1] = FormatNumber.formatLokalCurrency(konto2140);

        data[28][1] = FormatNumber.formatLokalCurrency(vorsteuereinnahme);

        data[30][1] = FormatNumber.formatLokalCurrency(gesamteinnahme);

        data[33][1] = FormatNumber.formatLokalCurrency(gesamteinnahme);
        data[34][1] = FormatNumber.formatLokalCurrency(gesamtausgabe);
        data[35][1] = FormatNumber.formatLokalCurrency(gesamteinnahme - gesamtausgabe);

    }

    public DefaultTableModel getModel() {
        return new DefaultTableModel(data, head);
    }
}

