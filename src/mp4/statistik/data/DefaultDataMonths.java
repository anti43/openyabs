/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mp4.statistik.data;

import java.util.ArrayList;
import java.util.Date;

import mp4.logs.*;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.datenbank.verbindung.Query;
import mp4.interfaces.Waitable;
import mp4.utils.datum.DateConverter;
import mp4.utils.datum.vDate;
import mp4.utils.datum.vTimeframe;
import mp4.utils.listen.ListenDataUtils;

/**
 *
 * @author anti43
 */
public class DefaultDataMonths implements Waitable {

    private vTimeframe zeitraum;
    private ArrayList columns = new ArrayList(12);
    private Query query;
    private ArrayList<Double> rechnungenVal;
    private ArrayList<Double> einnahmenVal;
    private ArrayList<Double> ausgabenVal;
    public String title = "titelPlatzHalter";
    public String vonYear;
    public String bisYear;
    private ArrayList data;
    public static int UMSATZ = 0;
    public static int GEWINN = 1;
    public static int EINNAHMEN = 2;
    public static int AUSGABEN = 3;
    private int mode = 0;

    public DefaultDataMonths(Date date, Date date0, String title, int mode) {
        this.vonYear = DateConverter.getDefDateString(date);
        this.bisYear = DateConverter.getDefDateString(date0);
        this.title = title;
        this.mode = mode;
        
        zeitraum = new vTimeframe(date, date0);

        while (date.before(date0)) {
            columns.add(date);
            date = DateConverter.addMonth(date);
        }
    }

    @SuppressWarnings("unchecked")
    public DefaultDataMonths(String start, String ende, String title, int mode) throws Exception {

        this.vonYear = start;
        this.bisYear = ende;
        this.title = title;
        this.mode = mode;


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

        } catch (NumberFormatException numberFormatException) {
            Log.Debug(numberFormatException);
        }
    }

    private ArrayList getGewinn() {
        try {
            data = ListenDataUtils.substract(ListenDataUtils.add(einnahmenVal, rechnungenVal), ausgabenVal);
        } catch (Exception ex) {
            Log.Debug(ex.getMessage());
        }
        return data;
    }

    private ArrayList getUmsatz() {
        try {
            data = ListenDataUtils.add(rechnungenVal, ListenDataUtils.add(einnahmenVal, ausgabenVal));
        } catch (Exception ex) {
            Log.Debug(ex);
        }
        return data;
    }

    private ArrayList getEinnahmen() {
        try {
            data = ListenDataUtils.add(rechnungenVal, einnahmenVal);
        } catch (Exception ex) {
            Log.Debug(ex);
        }
        return data;
    }

    private ArrayList getAusgaben() {
        try {
            data = ausgabenVal;
        } catch (Exception ex) {
            Log.Debug(ex);
        }
        return data;
    }

    public ArrayList getData() {
        return data;
    }

    public ArrayList getColumns() {
        return columns;
    }

    public void waitFor() {
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

        switch (mode) {

            case 0:
                getUmsatz();
                break;

            case 1:
                getGewinn();
                break;

            case 2:
                getEinnahmen();
                break;

            case 3:
                getAusgaben();
                break;
        }
    }
}
