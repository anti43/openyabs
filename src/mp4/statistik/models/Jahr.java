/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mp4.statistik.models;

import java.util.ArrayList;
import java.util.Date;
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
public class Jahr {

    private vTimeframe zeitraum;
    private ArrayList columns = new ArrayList(1);
    private Query query;
    private ArrayList<Double> rechnungenVal;
    private ArrayList<Double> einnahmenVal;
    private ArrayList<Double> ausgabenVal;

    public Jahr(String jahr) throws Exception {
        Integer year = 0;

        try {
            if (jahr.length() == 4) {
                year = Integer.valueOf(jahr);
                year++;
            } else {
                throw new Exception("Unsupported Timeframe!");
            }
            vDate von = new vDate(jahr);
            vDate bis = new vDate(year.toString());
            this.zeitraum = new vTimeframe(von, bis);

            Date date = zeitraum.getStart();  
            
            while (date.before(zeitraum.getEnd())){
                columns.add(date);
                date = DateConverter.addMonth(date);
            } 


//        columns = new ArrayList();
//        
//        columns.add(new Date());
//        columns.add(DateConverter.addYear(new Date()));
//        columns.add(DateConverter.addYear(DateConverter.addYear(new Date())));
//        columns.add(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(new Date()))));
//        columns.add(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(new Date())))));
//        columns.add(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(new Date()))))));
//        columns.add(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(new Date())))))));
//        columns.add(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(new Date()))))))));
//        columns.add(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(DateConverter.addYear(new Date())))))))));
//     

            query = ConnectionHandler.instanceOf().clone(null);

            query.setTable("Rechnungen");
            rechnungenVal = query.selectMonthlySums("gesamtpreis", new String[]{"storno", "0", ""}, zeitraum);

            query.setTable("Einnahmen");
            einnahmenVal = query.selectMonthlySums("Preis", null, zeitraum);

            query.setTable("Ausgaben");
            ausgabenVal = query.selectMonthlySums("Preis", null, zeitraum);

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
//
//    public ArrayList getGewinn() {
//
//
//        return Formater.ArrayToColumnList(Formater.merge(val1, val2), 0, Double.class);
//    }
    public ArrayList getUmsatz() {

//        return rechnungenVal;
        return Formater.merge(rechnungenVal, Formater.merge(einnahmenVal, ausgabenVal));
    }

    public ArrayList getColumns() {
        return columns;
    }
}
