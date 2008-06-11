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
package mp3.classes.objects.ungrouped;

import mp3.classes.objects.eur.SKRKonto;
import java.awt.Dimension;
import java.util.Date;
import java.util.Locale;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import mp3.classes.interfaces.Constants;
import mp3.classes.layer.Popup;
import mp3.classes.layer.QueryClass;
import mp3.classes.utils.FileReaderWriter;
import mp3.classes.utils.Formater;
import mp3.classes.utils.Log;

/**
 *
 * @author anti
 */
public class MyData extends mp3.classes.layer.People implements mp3.classes.interfaces.Structure {

    private static MyData dat;
    private String backupverz = "";
    private String rechnungverz = "";
    private String angebotverz = "";
    private String mahnungverz = "";
    private String rechnungtemp = "";
    private String angebottemp = "";
    private String mahnungtemp = "";
    private String serienbrieftemp = "";
    private String globaltax = "";
    private String pdfviewer = "";
    private String browser = "";
    private String lasttab = "";
    private String ekDefaultKontoNummer = "";
    private String agDefaultKontoNummer = "";
    private SKRKonto einnahmeDefKonto;
    private SKRKonto ausgabeDefKonto;
    private Locale locale;
    private String[][] str;
    
    private String state;
//    private String[] options;
    public static MyData instanceOf() {

        if (dat == null) {
            dat = new MyData();
            return dat;
        }
        return dat;
    }
    
    public static MyData newInstanceOf() {

        dat = null;
            dat = new MyData();
        
        
        return dat;
    }
    private String[][] string;

    private MyData() {
        super(QueryClass.instanceOf().clone(TABLE_MYDATA));
        this.id = 1;

        this.str = this.select("name, wert", null, null, false);
        string = str;
        this.explode();
        this.id = 1;
    }

    public DefaultTableModel getDefaultTablemodel() {

        String[] head = new String[]{"Option", "Wert"};


        return new DefaultTableModel(str, head);


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

        string = new String[str.length][2];


        for (int k = 0; k < str.length; k++) {

            string[k][0] = String.valueOf(model.getValueAt(k, 0));
            string[k][1] = String.valueOf(model.getValueAt(k, 1));

        }

        this.explode(string);

    }

    public void setState(int height, int width) {
       this.setState(height +","+width);
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
            this.setGlobaltax(str[8][1]);
            this.setPdfviewer(str[9][1]);
            this.setBrowser(str[10][1]);
            this.setLasttab(str[11][1]);
            this.setEkDefaultKonto(str[12][1]);
            this.setAgDefaultKonto(str[12 + 1][1]);
            this.setLocale(new Locale("de",str[14][1]));


            try {
               
                this.setEinnahmeDefKonto(new SKRKonto(QueryClass.instanceOf(), getEkDefaultKonto(), true));


            } catch (Exception exception) {
                
                Popup.notice("Einnahmenkonto nicht vorhanden.\nBeachten Sie die genaue Schreibweise (z.B. '3 000' anstatt '3000')");
                this.setEkDefaultKonto("0");
                this.setEinnahmeDefKonto(new SKRKonto(QueryClass.instanceOf(), getEkDefaultKonto(), true));

            }

            try {
                this.setAusgabeDefKonto(new SKRKonto(QueryClass.instanceOf(), getAgDefaultKonto(), true));

            } catch (Exception exception) {
                Popup.notice("Ausgabenkonto nicht vorhanden.\nBeachten Sie die genaue Schreibweise (z.B. '3 000' anstatt '3000')");
                this.setAgDefaultKonto("1");
                this.setAusgabeDefKonto(new SKRKonto(QueryClass.instanceOf(), getAgDefaultKonto(), true));
            }

            

        }
    }

    private void explode() {
        boolean ok=false;

        if (str != null) {
            this.setBackupverz(str[0][1]);
            this.setRechnungverz(str[1][1]);
            this.setAngebotverz(str[2][1]);
            this.setMahnungverz(str[3][1]);
            this.setRechnungtemp(str[4][1]);
            this.setAngebottemp(str[5][1]);
            this.setMahnungtemp(str[6][1]);
            this.setSerienbrieftemp(str[7][1]);
            this.setGlobaltax(str[8][1]);
            this.setPdfviewer(str[9][1]);
            this.setBrowser(str[10][1]);
            this.setLasttab(str[11][1]);
            this.setEkDefaultKonto(str[12][1]);
            this.setAgDefaultKonto(str[12 + 1][1]);
            
            try {
                  this.setLocale(new Locale("de",str[14][1]));

                  ok=true;
            } catch (ArrayIndexOutOfBoundsException exception) {
                ok=false;
                Log.Debug(exception.getMessage());
            
                this.insert("name,wert", "(;;2#4#1#1#8#0#;;)Land (Waehrung) (DE,CH)(;;2#4#1#1#8#0#;;),(;;2#4#1#1#8#0#;;)DE(;;2#4#1#1#8#0#;;)");
               
                MyData.newInstanceOf();
            }


               try {
               
                this.setEinnahmeDefKonto(new SKRKonto(QueryClass.instanceOf(), getEkDefaultKonto(), true));


            } catch (Exception exception) {
                this.setEkDefaultKonto("2100");
                this.setEinnahmeDefKonto(new SKRKonto(QueryClass.instanceOf(), getEkDefaultKonto(), true));

            }

            try {
                this.setAusgabeDefKonto(new SKRKonto(QueryClass.instanceOf(), getAgDefaultKonto(), true));

            } catch (Exception exception) {
                this.setAgDefaultKonto("1111");
                this.setAusgabeDefKonto(new SKRKonto(QueryClass.instanceOf(), getAgDefaultKonto(), true));
            }
            
            if(ok){
            try {
                this.setState(str[15][1]);

            } catch (IndexOutOfBoundsException exception) {
                
                this.insert("name,wert", "(;;2#4#1#1#8#0#;;)*Hauptfenster(;;2#4#1#1#8#0#;;),(;;2#4#1#1#8#0#;;) (;;2#4#1#1#8#0#;;)");
                
                MyData.newInstanceOf();
            }
            }
        }
    }

    private void collect() {
        string[0][1] = getBackupverz();
        string[1][1] = getRechnungverz();
        string[2][1] = getAngebotverz();
        string[3][1] = getMahnungverz();
        string[4][1] = getRechnungtemp();
        string[5][1] = getAngebottemp();
        string[6][1] = getMahnungtemp();
        string[7][1] = getSerienbrieftemp();
        string[8][1] = getGlobaltax();
        string[9][1] = getPdfviewer();
        string[10][1] = getBrowser();
        string[11][1] = getLasttab().toString();
        string[12][1] = getEkDefaultKonto();
        string[12 + 1][1] = getAgDefaultKonto();
        try {
            string[14][1] = getLocale().getCountry();

        } catch (Exception exception) {
        }

        try {
            string[15][1] = getState();

        } catch (Exception exception) {
        }




    }

    public void save() {

        collect();
        for (int i = 0; i < string.length; i++) {

            this.update("wert", "(;;2#4#1#1#8#0#;;)" + string[i][1] + "(;;2#4#1#1#8#0#;;)", String.valueOf(i + 1));

        }

        dat = new MyData();

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

    public String getGlobaltax() {

        try {
            Double.valueOf(globaltax);

        } catch (NumberFormatException numberFormatException) {

            return "0";
        }


        return globaltax;
    }

    public void setGlobaltax(String globaltax) {
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

    public Integer getLasttab() {
        Integer i = 0;

        try {
            i = Integer.valueOf(lasttab);

        } catch (NumberFormatException numberFormatException) {
            return 0;
        }


        return i;
    }

    public void setLasttab(String lasttab) {
        this.lasttab = lasttab;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    
    public Dimension getMainframeSize(){
    
        return new Dimension(Integer.valueOf(getState().split(",")[1]),Integer.valueOf(getState().split(",")[0]));
              
    
    }
}
