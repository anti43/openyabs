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

import java.lang.reflect.Method;
import java.util.ArrayList;
import mp4.items.SKRKonto;
import mp4.benutzerverwaltung.User;

import java.util.Date;
import java.util.Locale;
import mp4.globals.Constants;

import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.globals.Strings;
import mp4.items.visual.Popup;
import mp4.utils.files.FileReaderWriter;
import mp4.logs.*;
import mp4.main.Main;
import mp4.utils.datum.DateConverter;
import mp4.utils.listen.ListenDataUtils;

/**
 *
 * @author anti
 */
public class Einstellungen implements mp4.datenbank.installation.Tabellen {

    private static Einstellungen dat;
    private DataHandler datahandler;
    private User user = new User();

    private Einstellungen() {
        datahandler = new DataHandler(TABLE_CONFIG_DATA);
        Locale.setDefault(getLocale());
    }

    public static Einstellungen instanceOf() {
        if (dat == null) {
            dat = new Einstellungen();
            return dat;
        }
        return dat;
    }
////////////////////////////////////////////////////////////////////////////////
    public Object[][] getExtProgs() {
        return getDaten("Programm");
    }

    public Object[][] getVorlagen() {
        return getDaten("Template");
    }

    public Object[][] getVerzeichnisse() {
        return getDaten("Verzeichnis");
    }

//    private Object[][] getDaten(String string) {
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
    @SuppressWarnings("unchecked")
    public Object[][] getDaten(String typ) {
        ArrayList values = null;
        try {
            values = new ArrayList();
            Method[] methods = this.getClass().getMethods();

            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().startsWith("get") && methods[i].getName().endsWith(typ) &&!methods[i].isVarArgs()) {

                    values.add(new Object[]{methods[i].getName().substring(3, methods[i].getName().length()).replaceAll("_", " "), 
                    methods[i].invoke(this, (Object[]) null)});
                    
                    
                }
            }
        } catch (Exception ex) {
            return ListenDataUtils.listToTableArray(values);
        }
        return ListenDataUtils.listToTableArray(values);
    }
////////////////////////////////////////////////////////////////////////////////
    public User getUser() {
        return user;
    }

    public void setUser(User usern) {
        this.user = usern;
    }

    public String getDate() {
        return DateConverter.getDefDateString(new Date());
    }

    public String getVersion() {
        return Constants.VERSION;
    }
////////////////////////////////////////////////////////////////////////////////  
    public String getDatenbank_Verzeichnis() {
        try {
            FileReaderWriter f = new FileReaderWriter(Main.SETTINGS_FILE);
            String[] dats = f.read().split(";");
            return dats[0];
        } catch (Exception exception) {
            Log.Debug(exception);
            return "N/A";
        }
    }

    public void setDatenbank_Verzeichnis(String pfad) {
        FileReaderWriter f = new FileReaderWriter(Main.SETTINGS_FILE);
        f.write(pfad + Strings.COLON + Constants.VERSION);

    }

    public String getBackup_Verzeichnis() {
        return datahandler.getString("Backup Verzeichnis");
    }

    public void setBackup_Verzeichnis(String Backupverzeichnis) {
        datahandler.setString("Backup Verzeichnis", Backupverzeichnis);
    }

    public String getRechnung_Verzeichnis() {
        return datahandler.getString("Rechnung Verzeichnis");
    }

    public void setRechnung_Verzeichnis(String Rechnungverzeichnis) {
        datahandler.setString("Rechnung Verzeichnis", Rechnungverzeichnis);
    }

    public String getAngebot_Verzeichnis() {
        return datahandler.getString("Angebot Verzeichnis");
    }

    public void setAngebot_Verzeichnis(String Angebotverzeichnis) {
        datahandler.setString("Angebot Verzeichnis", Angebotverzeichnis);
    }

    public String getMahnung_Verzeichnis() {
        return datahandler.getString("Mahnung Verzeichnis");
    }

    public void setMahnung_Verzeichnis(String Mahnungverzeichnis) {
        datahandler.setString("Mahnung Verzeichnis", Mahnungverzeichnis);
    }

    public String getProdukt_Verzeichnis() {
        return datahandler.getString("Produkt Verzeichnis");
    }

    public void setProdukt_Verzeichnis(String Produktverzeichnis) {
        datahandler.setString("Produkt Verzeichnis", Produktverzeichnis);
    }
////////////////////////////////////////////////////////////////////////////////  
    public String getLieferschein_Template() {
        return datahandler.getString("Lieferschein Template");
    }

    public void setLieferschein_Template(String Lieferscheintemplate) {
        datahandler.setString("Lieferschein Template", Lieferscheintemplate);
    }

    public String getRechnung_Template() {
        return datahandler.getString("Rechnung Template");
    }

    public void setRechnung_Template(String Rechnungtemplate) {
        datahandler.setString("Rechnung Template", Rechnungtemplate);
    }

    public String getAngebot_Template() {
        return datahandler.getString("Angebot Template");
    }

    public void setAngebot_Template(String Angebottemplate) {
        datahandler.setString("Angebot Template", Angebottemplate);
    }

    public String getMahnung_Template() {
        return datahandler.getString("Mahnung Template");
    }

    public void setMahnung_Template(String Mahnungtemplate) {
        datahandler.setString("Mahnung Template", Mahnungtemplate);
    }

    public String getSerienbrief_Template() {
        return datahandler.getString("Serienbrief Template");
    }

    public void setSerienbrief_Template(String Serienbrieftemplate) {
        datahandler.setString("Serienbrief Template", Serienbrieftemplate);
    }

    public String getProdukt_Template() {
        return datahandler.getString("Produkt Template");
    }

    public void setProdukt_Template(String Produkttemplate) {
        datahandler.setString("Produkt Template", Produkttemplate);
    }
////////////////////////////////////////////////////////////////////////////////  
    public String getPdf_Programm() {
        return datahandler.getString("Pdf Programm");
    }

    public void setPdf_Programm(String Pdfprogramm) {
        datahandler.setString("Pdf Programm", Pdfprogramm);
    }

    public String getBrowser_Programm() {
        return datahandler.getString("Browser Programm");
    }

    public void setBrowser_Programm(String Browser) {
        datahandler.setString("Browser Programm", Browser);
    }
////////////////////////////////////////////////////////////////////////////////
    public Double getHauptsteuersatz() {
        return datahandler.getDouble("Hauptsteuersatz");
    }

    public void setHauptsteuersatz(Double Hauptsteuersatz) {
        datahandler.setDouble("Hauptsteuersatz", Hauptsteuersatz);
    }

    public Locale getLocale() {
        try {
            return new Locale(datahandler.getString("Locale").split("_")[0], datahandler.getString("Locale").split("_")[1]);
        } catch (Exception e) {
            Popup.notice("Es ist ein Fehler aufgetreten, Locale de_DE wird benutzt.\n" + e.getMessage());
            return new Locale("de", "DE");
        }
    }

    public void setLocale(Locale locale) {
        datahandler.setString("Locale", locale.toString());
    }

////////////////////////////////////////////////////////////////////////////////
    private String getEkDefaultKonto() {
        return datahandler.getString("Konto Einnahme");
    }

    private void setEkDefaultKonto(String EkDefaultKonto) {
        datahandler.setString("Konto Einnahme", EkDefaultKonto);
    }

    private String getAgDefaultKonto() {
        return datahandler.getString("Konto Ausgabe");
    }

    private void setAgDefaultKonto(String AgDefaultKonto) {
        datahandler.setString("Konto Ausgabe", AgDefaultKonto);
    }

    public SKRKonto getEinnahmen_Standard_Konto() {
        try {
            return new SKRKonto(ConnectionHandler.instanceOf(), getEkDefaultKonto(), true);
        } catch (Exception e) {
            Popup.notice("Einnahmenkonto nicht vorhanden.\nBeachten Sie die genaue Schreibweise (z.B. '3000' anstatt '3 000')");
            this.setEkDefaultKonto("2100");
            return new SKRKonto(ConnectionHandler.instanceOf(), getEkDefaultKonto(), true);
        }
    }

    public void setEinnahme_Standard_Konto(SKRKonto einnahmeDefKonto) {
        this.setEkDefaultKonto(einnahmeDefKonto.getNummer());
    }

    public SKRKonto getAusgabeDefKonto() {
        try {
            return new SKRKonto(ConnectionHandler.instanceOf(), getAgDefaultKonto(), true);
        } catch (Exception e) {
            Popup.notice("Ausgabenkonto nicht vorhanden.\nBeachten Sie die genaue Schreibweise (z.B. '3 000' anstatt '3000')");
            this.setAgDefaultKonto("1111");
            return new SKRKonto(ConnectionHandler.instanceOf(), getAgDefaultKonto(), true);
        }
    }

    public void setAusgabeDefKonto(SKRKonto ausgabeDefKonto) {
        this.setAgDefaultKonto(ausgabeDefKonto.getNummer());
    }
}
