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
package mp4.einstellungen;


import mp4.benutzerverwaltung.User;
import mp4.klassen.objekte.*;
import java.util.Date;
import java.util.Locale;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import mp3.classes.interfaces.Constants;

import mp3.classes.layer.Popup;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp3.classes.utils.FileReaderWriter;
import mp3.classes.utils.Formater;
import mp3.classes.utils.Log;

/**
 *
 * @author anti
 */
public class Einstellungen extends mp3.classes.layer.People implements mp4.datenbank.struktur.Tabellen {

    private static Einstellungen dat;
    private String backupverz = "";
    private String rechnungverz = "";
    private String angebotverz = "";
    private String mahnungverz = "";
    private String rechnungtemp = "";
    private String angebottemp = "";
    private String mahnungtemp = "";
    private String serienbrieftemp = "";
    private Double globaltax = 0d;
    private String pdfviewer = "";
    private String browser = "";
    
    private String ekDefaultKontoNummer = "";
    private String agDefaultKontoNummer = "";
    private SKRKonto einnahmeDefKonto;
    private SKRKonto ausgabeDefKonto;
    private Locale locale;
    private String[][] valurarray;
    
    private String[][] orig_valuearray;

    private User user = new User();
    
    public static Einstellungen instanceOf() {
        if (dat == null) {
            dat = new Einstellungen();
            return dat;
        }
        return dat;
    }

    public static Einstellungen newInstanceOf() {
        dat = null;
        dat = new Einstellungen();
        return dat;
    }
    

    private Einstellungen() {
        super(ConnectionHandler.instanceOf().clone(TABLE_MYDATA));
        this.id = 1;
        this.valurarray = this.select("name, wert", null, null, false);
        orig_valuearray = valurarray;
        this.explode(valurarray);
        this.id = 1;
        Locale.setDefault(getLocale());
    }

    public User getUser() {
        return user;
    }

    private int getAppVersion() {
        return Integer.valueOf(Constants.VERSION);
    }


    public DefaultTableModel getDefaultTablemodel() {

        String[] head = new String[]{"Option", "Wert"};
        return new DefaultTableModel(valurarray, head);
    }

    public String getDate() {
        return Formater.formatDate(new Date());
    }

    public String getVersion() {

        try {
            FileReaderWriter f = new FileReaderWriter(Constants.SETTINGS);
            String[] dats = f.read().split(";");

            return dats[1];

        } catch (Exception exception) {
            Log.Debug(exception);

            return "N/A";
        }

    }

    public String getDbPath() {

        try {
            FileReaderWriter f = new FileReaderWriter(Constants.SETTINGS);
            String[] dats = f.read().split(";");

            return dats[0];

        } catch (Exception exception) {
            Log.Debug(exception);

            return "N/A";
        }

    }

    public void setModel(TableModel model) {

        orig_valuearray = new String[valurarray.length][2];


        for (int k = 0; k < valurarray.length; k++) {

            orig_valuearray[k][0] = String.valueOf(model.getValueAt(k, 0));
            orig_valuearray[k][1] = String.valueOf(model.getValueAt(k, 1));

        }

        this.explode(orig_valuearray);

    }



    private void explode(String[][] str) {
        if (str != null) {
            this.setBackupverz(str[0][1]);
            this.setRechnungverz(str[1][1]);
            this.setAngebotverz(str[2][1]);
            this.setMahnungverz(str[3][1]);
            this.setRechnungtemp(str[4][1]);
            this.setAngebottemp(str[5][1]);
            this.setMahnungtemp(str[6][1]);
            this.setSerienbrieftemp(str[7][1]);
            this.setGlobaltax(Double.valueOf(str[8][1]));
            this.setPdfviewer(str[9][1]);
            this.setBrowser(str[10][1]);
            this.setEkDefaultKonto(str[11][1]);
            this.setAgDefaultKonto(str[12][1]);
            this.setLocale(new Locale("de", str[12+1][1]));
            
            try {
                this.setEinnahmeDefKonto(new SKRKonto(ConnectionHandler.instanceOf(), getEkDefaultKonto(), true));
            } catch (Exception exception) {
                Popup.notice("Einnahmenkonto nicht vorhanden.\nBeachten Sie die genaue Schreibweise (z.B. '3 000' anstatt '3000')");
                this.setEkDefaultKonto("2100");
                this.setEinnahmeDefKonto(new SKRKonto(ConnectionHandler.instanceOf(), getEkDefaultKonto(), true));
            }

            try {
                this.setAusgabeDefKonto(new SKRKonto(ConnectionHandler.instanceOf(), getAgDefaultKonto(), true));
            } catch (Exception exception) {
                Popup.notice("Ausgabenkonto nicht vorhanden.\nBeachten Sie die genaue Schreibweise (z.B. '3 000' anstatt '3000')");
                this.setAgDefaultKonto("1111");
                this.setAusgabeDefKonto(new SKRKonto(ConnectionHandler.instanceOf(), getAgDefaultKonto(), true));
            }
        }
    }

    private void collect() {
        orig_valuearray[0][1] = getBackupverz();
        orig_valuearray[1][1] = getRechnungverz();
        orig_valuearray[2][1] = getAngebotverz();
        orig_valuearray[3][1] = getMahnungverz();
        orig_valuearray[4][1] = getRechnungtemp();
        orig_valuearray[5][1] = getAngebottemp();
        orig_valuearray[6][1] = getMahnungtemp();
        orig_valuearray[7][1] = getSerienbrieftemp();
        orig_valuearray[8][1] = getGlobaltax().toString();
        orig_valuearray[9][1] = getPdfviewer();
        orig_valuearray[10][1] = getBrowser();
       
        orig_valuearray[11][1] = getEkDefaultKonto();
        orig_valuearray[12][1] = getAgDefaultKonto();
        orig_valuearray[12+1][1] = getLocale().getCountry();
       
    }

    public void save() {

        collect();
        for (int i = 0; i < orig_valuearray.length; i++) {

            this.update("wert", "(;;2#4#1#1#8#0#;;)" + orig_valuearray[i][1] + "(;;2#4#1#1#8#0#;;)", String.valueOf(i + 1));

        }

        dat = new Einstellungen();

    }

    public String getBackupverz() {
        return backupverz;
    }

    public void setBackupverz(String backupverz) {
        this.backupverz = backupverz;
    }

    public String getRechnungverz() {
        return rechnungverz;
    }

    public void setRechnungverz(String rechnungverz) {
        this.rechnungverz = rechnungverz;
    }

    public String getAngebotverz() {
        return angebotverz;
    }

    public void setAngebotverz(String angebotverz) {
        this.angebotverz = angebotverz;
    }

    public String getMahnungverz() {
        return mahnungverz;
    }

    public void setMahnungverz(String mahnungverz) {
        this.mahnungverz = mahnungverz;
    }

    public String getRechnungtemp() {
        return rechnungtemp;
    }

    public void setRechnungtemp(String rechnungtemp) {
        this.rechnungtemp = rechnungtemp;
    }

    public String getAngebottemp() {
        return angebottemp;
    }

    public void setAngebottemp(String angebottemp) {
        this.angebottemp = angebottemp;
    }

    public String getMahnungtemp() {
        return mahnungtemp;
    }

    public void setMahnungtemp(String mahnungtemp) {
        this.mahnungtemp = mahnungtemp;
    }

    public String getSerienbrieftemp() {
        return serienbrieftemp;
    }

    public void setSerienbrieftemp(String serienbrieftemp) {
        this.serienbrieftemp = serienbrieftemp;
    }

    public Double getGlobaltax() {

        try {
            Double.valueOf(globaltax);

        } catch (NumberFormatException numberFormatException) {

            return 0d;
        }


        return globaltax;
    }

    public void setGlobaltax(Double globaltax) {
        this.globaltax = globaltax;
    }

    public String getPdfviewer() {
        return pdfviewer;
    }

    public void setPdfviewer(String pdfviewer) {
        this.pdfviewer = pdfviewer;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }


    private String getEkDefaultKonto() {
        return ekDefaultKontoNummer;
    }

    private void setEkDefaultKonto(String ekDefaultKonto) {
        this.ekDefaultKontoNummer = ekDefaultKonto;
    }

    private String getAgDefaultKonto() {
        return agDefaultKontoNummer;
    }

    private void setAgDefaultKonto(String agDefaultKonto) {
        this.agDefaultKontoNummer = agDefaultKonto;
    }

    public SKRKonto getEinnahmeDefKonto() {
        return einnahmeDefKonto;
    }

    public void setEinnahmeDefKonto(SKRKonto einnahmeDefKonto) {
        this.einnahmeDefKonto = einnahmeDefKonto;
        this.setEkDefaultKonto(einnahmeDefKonto.getNummer());
    }

    public SKRKonto getAusgabeDefKonto() {
        return ausgabeDefKonto;
    }

    public void setAusgabeDefKonto(SKRKonto ausgabeDefKonto) {
        this.ausgabeDefKonto = ausgabeDefKonto;
        this.setAgDefaultKonto(ausgabeDefKonto.getNummer());
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }



   
}
