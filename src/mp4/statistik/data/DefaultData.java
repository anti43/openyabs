/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mp4.statistik.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import mp3.classes.utils.Formater;
import mp3.classes.utils.Log;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.datenbank.verbindung.Query;
import mp4.utils.datum.DateConverter;
import mp4.utils.datum.vDate;
import mp4.utils.datum.vTimeframe;

/**
 *
 * @author anti43
 */
public class DefaultData {

    private vTimeframe zeitraum;
    private ArrayList columns = new ArrayList(1);
    private Query query;
    private ArrayList<Double> rechnungenVal;
    private ArrayList<Double> einnahmenVal;
    private ArrayList<Double> ausgabenVal;

    @SuppressWarnings("unchecked")
    public DefaultData(String start, String ende) throws Exception {

        try {
            vDate von = new vDate(start);
            vDate bis = new vDate(ende);
            if (von.isVerified && bis.isVerified) {
                this.zeitraum = new vTimeframe(von, bis);
            } else {
                throw new Exception("Unsupported Timeframe!");
            }
            Date date = zeitraum.getStart();

            while (date.before(zeitraum.getEnd())) {
                columns.add(date);
                date = DateConverter.addMonth(date);
            }

            query = ConnectionHandler.instanceOf().clone(null);

            query.setTable("Rechnungen");
            rechnungenVal = query.selectMonthlySums("gesamtpreis", new String[]{"storno", "0", ""}, zeitraum, " AND bezahlt = 1 ");

            query.setTable("Einnahmen");
            einnahmenVal = query.selectMonthlySums("Preis", null, zeitraum, "");

            query.setTable("Ausgaben");
            ausgabenVal = query.selectMonthlySums("Preis", null, zeitraum, "");

            if (rechnungenVal.isEmpty()) {
                rechnungenVal.add(0d);
            }
            if (einnahmenVal.isEmpty()) {
                einnahmenVal.add(0d);
            }
            if (ausgabenVal.isEmpty()) {
                ausgabenVal.add(0d);
            }
        } catch (NumberFormatException numberFormatException) {
            Log.Debug(numberFormatException);
        }
    }

    public ArrayList getGewinn() {
        try {
            return Formater.substract(Formater.add(einnahmenVal, rechnungenVal), ausgabenVal);
        } catch (Exception ex) {
            Log.Debug(ex.getMessage());
        }
        return null;
    }

    public ArrayList getUmsatz() {

        return Formater.merge(rechnungenVal, Formater.merge(einnahmenVal, ausgabenVal));
    }

    public ArrayList getColumns() {
        return columns;
    }
}
