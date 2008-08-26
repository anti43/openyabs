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
package mp4.items;

import mp4.items.handler.NumberFormatHandler;
import mp4.einstellungen.Einstellungen;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import mp3.classes.interfaces.Countable;
import mp4.datenbank.verbindung.Query;

import mp3.classes.interfaces.Strings;
import mp3.classes.utils.Log;
import mp4.utils.tabellen.models.PostenTableModel;

import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.datenbank.verbindung.PrepareData;
import mp4.utils.datum.DateConverter;
import mp4.utils.tabellen.DataModelUtils;
import mp4.utils.zahlen.FormatNumber;

/**
 *
 * @author anti43
 */
public class Rechnung extends mp3.classes.layer.Things implements mp4.datenbank.installation.Tabellen, Countable {

    private String Rechnungnummer = "";
    private String UnserZeichen = "";
    private String IhrZeichen = "";
    private Integer KundenId = 0;
    private Integer Mahnungen = 0;
    private Date Datum = new Date();
    private Date AusfuehrungsDatum = new Date();
    private Double Gesamtpreis = 0.0;
    private Double Gesamttax = 0.0;
    private boolean storno = false;
    private boolean bezahlt = false;
    private boolean verzug = false;
    private Query query;
    public Integer id = 0;
    private PostenTableModel postendata = null;
    private RechnungBetreffZZR zeilenHandler;
    private Angebot Angebot = null;

    public Rechnung(String text) {
        super(ConnectionHandler.instanceOf().clone(TABLE_BILLS));
        this.query = ConnectionHandler.instanceOf();
        this.explode(this.selectLast(Strings.ALL, "rechnungnummer", text, true));
        zeilenHandler = new RechnungBetreffZZR(id);
        nfh = new NumberFormatHandler(this, getDatum());
        
    }

    public void add(PostenTableModel m) {
        this.postendata = m;
    }

    public void add(RechnungPosten rechnungPosten) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getCountColumn() {
        return "rechnungnummer";
    }

    public Integer getId() {
        return id;
    }

    public void destroy() {
        this.delete(this.id);
        this.id = 0;
    }

    public Rechnung() {
        super(ConnectionHandler.instanceOf().clone(TABLE_BILLS));
        this.query = ConnectionHandler.instanceOf();
        zeilenHandler = new RechnungBetreffZZR();
        nfh = new NumberFormatHandler(this, getDatum());
        
    }

    public Rechnung(Query query) {
        super(query.clone(TABLE_BILLS));
        this.query = query;
        zeilenHandler = new RechnungBetreffZZR();
        nfh = new NumberFormatHandler(this, getDatum());
        
    }

    /**
     * 
     * @param id
     */
    public Rechnung(Integer id) {
        super(ConnectionHandler.instanceOf().clone(TABLE_BILLS));
        this.id = Integer.valueOf(id);
        this.explode(this.selectLast(Strings.ALL, Strings.ID, id.toString(), true));
        this.query = ConnectionHandler.instanceOf().clone(TABLE_BILLS);
        zeilenHandler = new RechnungBetreffZZR(id);

        int aid = new Angebot().search(this.getId());
        if (aid != 0) {
            this.Angebot = new Angebot(aid);
        }
        nfh = new NumberFormatHandler(this, getDatum());
        
    }

    public String getFDatum() {
        return DateConverter.getDefDateString(getDatum());
    }

    public String getFGesamtpreis() {
        return FormatNumber.formatDezimal(getGesamtpreis());
    }

    public Rechnung expose() {
        return this;
    }

    public String[][] getUnpaid() {
        Query q = query.clone(TABLE_BILLS);
        String[][] prods = q.select("id, rechnungnummer, gesamtpreis, datum", new String[]{"bezahlt", "0", "", "storno", "0", ""}, "datum", false, true, false);
        return prods;
    }

    public String[][] getPaid() {
        Query q = query.clone(TABLE_BILLS);
        String[][] prods = q.select("id, rechnungnummer, gesamtpreis, datum", new String[]{"bezahlt", "1", "", "storno", "0", ""}, "datum", false, true, false);
        return prods;
    }

    public String[][] getPaidEUR() {
//        kontenid, preis, tax, datum
        Query q = query.clone(TABLE_BILLS);
        String[][] prods = q.select("gesamtpreis, gesamttax, datum", new String[]{"bezahlt", "1", "", "storno", "0", ""}, "datum", false, true, false);
        String[][] tzh = new String[prods.length][4];

        for (int g = 0; g < prods.length; g++) {
            tzh[g][0] = Einstellungen.instanceOf().getEinnahmeDefKonto().getId().toString();
            for (int l = 0; l < prods[g].length; l++) {
                tzh[g][l + 1] = prods[g][l];
            }
        }
        return tzh;
    }

    public boolean hasId() {
        if (id > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void setAngebot(Angebot angebot) {
        if (angebot.hasId()) {
            angebot.setRechnungId(this.getId());
            angebot.save();
            this.Angebot = angebot;
        } else {
            angebot.setRechnungId(this.getId());
            angebot.setKundenId(this.getKundenId());
            angebot.setAuftragdatum(this.getDatum());
            angebot.setDatum(this.getDatum());
            angebot.setValidVon(this.getDatum());
            angebot.setBisDatum(DateConverter.addMonth(this.getDatum()));
            angebot.setAngebotnummer(angebot.getNextNumber());
            angebot.save();
            this.Angebot = angebot;
        }
    }

    public void setAusfuehrungsDatum(Date date) {

        this.AusfuehrungsDatum = date;
    }

    public void setRechnungnummer(Integer nummer) {

        setRechnungnummer(nummer.toString());
    }

    private void clearPostenData() {
        new RechnungPosten().deleteExistingOf(this);
    }

    private void explode(String[] select) {
        try {

            this.id = Integer.valueOf(select[0]);
            this.setRechnungnummer(select[1]);
            this.setKundenId(Integer.valueOf(select[2]));
            this.setDatum(DateConverter.getDate(select[3]));
            this.setStorno(select[4]);
            this.setBezahlt(select[5]);
            this.setGesamtpreis(Double.valueOf(select[6]));
            this.setGesamttax(Double.valueOf(select[7]));
            this.setAusfuehrungsDatum(DateConverter.getDate(select[8]));
            this.setMahnungen(Integer.valueOf(select[9]));
        } catch (Exception exception) {
            Log.Debug(exception.getMessage());
        }
        if (!isBezahlt()) {
            try {
                Date date = DateConverter.addMonth(getDatum());
                if (new Date().after(date)) {
                    this.setVerzug(true);
                }
            } catch (Exception ex) {
                Log.Debug(ex.getMessage());
            }
        }
    }

    private String collect() {
        String str = PrepareData.prepareString(this.getRechnungnummer());
        str = str + PrepareData.prepareNumber(this.getKundenId());
        str = str + PrepareData.prepareString(DateConverter.getSQLDateString(this.getDatum()));
        str =
                str + PrepareData.prepareBoolean(isStorno());
        str = str + PrepareData.prepareBoolean(isBezahlt());
        str = str +
                PrepareData.prepareNumber(
                this.getGesamtpreis());
        str = str + PrepareData.prepareNumber(this.getGesamttax());
        str = str + PrepareData.prepareString(DateConverter.getSQLDateString(
                this.getAusfuehrungsDatum()));
        str = str + PrepareData.prepareNumber(this.getMahnungen());
        return PrepareData.finalize(str);


    }

    public boolean save() {
        int result = -1;
        if (id > 0 && !isSaved) {
            result = this.update(TABLE_BILLS_FIELDS, this.collect(), id.toString());
            if (postendata != null) {
                clearPostenData();
                explode(postendata);
            }
            zeilenHandler.save();
            isSaved = true;
        } else if (id == 0 && !isSaved) {
            result = this.insert(TABLE_BILLS_FIELDS, this.collect());
            this.id = this.getMyId();
            if (postendata !=
                    null) {
                explode(postendata);
            }
            zeilenHandler.setRechnungId(id);
            zeilenHandler.save();
            isSaved = true;
        }
        if (result > 0) {
            return true;
        } else {
            return false;
        }
    }

    public int getid() {
        return id;
    }

    public String getRechnungnummer() {
        return Rechnungnummer;
    }

    public void setRechnungnummer(String Rechnungnummer) {
        this.Rechnungnummer = Rechnungnummer;
        this.isSaved = false;

    }

    public Integer getKundenId() {
        return KundenId;
    }

    public void setKundenId(Integer KundenId) {
        this.KundenId = KundenId;
        this.isSaved = false;
    }

    public Date getDatum() {


        return Datum;
    }

    public void setDatum(
            Date Datum) {
        this.Datum = Datum;
        this.isSaved = false;
    }

    /**
     * 
     * @return id,Anzahl,Posten,Mehrwertsteuer,Nettopreis,Bruttopreis
     */
    public Object[][] getProductlistAsArray() {

        DecimalFormat n = new DecimalFormat("0.00");
        Query q = query.clone(TABLE_BILLS_DATA);
        String[] wher = {"rechnungid", this.getId().toString(), ""};
        String[][] prods = q.select(Strings.ALL, wher);
        Object[][] nstr = new Object[prods.length][6];

        try {

            for (int i = 0; i < nstr.length; i++) {

                nstr[i][0] = Integer.valueOf(prods[i][0]);

                nstr[i][1] = Double.valueOf(prods[i][2]);



                nstr[i][2] = String.valueOf(prods[i][3]);
                nstr[i][3] = Double.valueOf(prods[i][5]);
                try {
                    nstr[i][4] = Double.valueOf(prods[i][4]);
                    nstr[i][5] = Double.valueOf(
                            (Double.valueOf(prods[i][4]) *
                            (((Double.valueOf(prods[i][5])) / 100) + 1)));
                } catch (NumberFormatException numberFormatException) {

                    nstr[i][    4] = Double.valueOf("0");
                    nstr[i][5] = Double.valueOf("0");


                }
            }
        } catch (Exception exception) {
            Log.Debug(exception);
        }
        return nstr;

    }

    
 

    public PostenTableModel getProductlistAsTableModel() {

        DecimalFormat n = new DecimalFormat("0.00");
  
        Query q = query.clone(TABLE_BILLS_DATA);
        String[] wher = {"rechnungid", this.getId().toString(), ""};
        String[][] prods = q.select(Strings.ALL, wher);
        Object[][] nstr = new Object[prods.length][6];

        try {
            for (int i = 0; i < nstr.length; i++) {
                nstr[i][0] = Integer.valueOf(prods[i][0]);

                nstr[i][1] = Double.valueOf(prods[i][2]);



                nstr[i][2] = String.valueOf(prods[i][3]);
                nstr[i][3] = Double.valueOf(prods[i][5]);
                try {
                    nstr[i][4] = Double.valueOf(prods[i][4]);
                    nstr[i][5] = Double.valueOf(
                            (Double.valueOf(prods[i][4]) *
                            (((Double.valueOf(prods[i][5])) / 100) + 1)));
                } catch (NumberFormatException numberFormatException) {
                    nstr[i][4] = Double.valueOf("0");
                    nstr[i][5] = Double.valueOf("0");
                }

            }
        } catch (Exception exception) {

            Log.Debug(exception);
        }
        return new PostenTableModel(nstr);
    }

    private void explode(PostenTableModel m) {
        for (int i = 0; i < m.getRowCount(); i++) {
            if (m.getValueAt(i, 4) != null || m.getValueAt(i, 2) != null) {
                RechnungPosten b = new RechnungPosten();
                b.setRechnungid(this.getId());
                b.setPosten((String) m.getValueAt(i,2));
              
                try {
                    
                    b.setAnzahl(Double.valueOf((Double) m.getValueAt(i, 1)));
                } catch (Exception e) {
                     b.setAnzahl(0d);
                }

                try {
                    
                    b.setSteuersatz(Double.valueOf((Double) m.getValueAt(i, 3)));
                } catch (Exception e) {
                    b.setSteuersatz(0d);
                }

                try {
                    
                    b.setPreis(Double.valueOf((Double) m.getValueAt(i, 4)));
                } catch (Exception e) {
                    b.setPreis(0d);
                }
                b.save();
            }
        }
    }

    private Integer getMyId() {
        String[] str = this.selectLast("id", "rechnungnummer", this.getRechnungnummer(), false);
        return Integer.valueOf(str[0]);
    }

    private RechnungPosten[] getProducts(Query query) {
        Query q = query.clone(TABLE_BILLS_DATA);
        String[] wher = {"rechnungid", this.getId().toString(), ""};
        String[][] str = q.select(Strings.ALL, wher);
        RechnungPosten[] prof = new RechnungPosten[str.length];

        for (int t = 0; t < str.length; t++) {
            prof[t] = new RechnungPosten(query, str[0][t]);
        }
        return prof;
    }
//    public RechnungPosten[] getBp() {
//        return bp;
//    }
    /**
     * 
     * @param table1fields
     * @return
     */
    public Object[][] getWithDependencies() {
        Query q = query.clone(TABLE_BILLS);
        String[][] prods = q.select("rechnungen.id,rechnungen.rechnungnummer,rechnungen.datum,kundennummer,firma, bezahlt, storno", null, TABLE_CUSTOMERS, "kundenid", "rechnungen.datum");
        if (prods == null || prods.length < 1) {
            prods = new String[][]{{null, null, null, null, null, "0", "0"}};
        }
        return DataModelUtils.changeToClassValue(prods, Boolean.class, new int[]{5, 6});
    }

    public String[][] getAll() {
        Query q = query.clone(TABLE_BILLS);
        String[][] prods = q.select(Strings.ALL, null);
        return prods;
    }

    public boolean isStorno() {
        return storno;
    }

    public void setStorno(boolean storno) {
        this.storno = storno;
    }

    public boolean isBezahlt() {
        return bezahlt;
    }

    public void setBezahlt(boolean bezahlt) {
        this.bezahlt = bezahlt;
    }

    public boolean isVerzug() {
        return verzug;
    }

    public void setVerzug(boolean verzug) {
        this.verzug = verzug;
    }

    public Double getGesamtpreis() {
        return Gesamtpreis;
    }

    public void setGesamtpreis(Double Gesamtpreis) {
        this.Gesamtpreis = Gesamtpreis;
    }
//    public String[][] inserType(String[][] prods) {
//        String[][] pro = null;
//        if (prods.length > 0) {
//            pro = new String[prods.length][prods[0].length + 1];
//            
//            for (int i = 0; i < pro.length; i++) {
//                int m = 0;
//                for (int j = 0; j < pro[i].length; j++, m++) {
//                    
//                    
//                    if (j == 2) {
//                        pro[i][2] = "Rechnung";
//                        m--;
//                    } else {
//                        
//                        pro[i][j] = prods[i][m];
//                    }
//
//                }
//            }
//        }        
//        return pro;
//    }
    public Double getGesamttax() {
        return Gesamttax;
    }

    public void setGesamttax(Double Gesamttax) {
        this.Gesamttax = Gesamttax;
    }

    public int delete(String id) {
        return delete(Integer.valueOf(id));
    }

    public Date getAusfuehrungsDatum() {
        return AusfuehrungsDatum;
    }

    public String getUnserZeichen() {
        return UnserZeichen;
    }

    public void setUnserZeichen(String UnserZeichen) {
        this.UnserZeichen = UnserZeichen;
    }

    public String getIhrZeichen() {
        return IhrZeichen;
    }

    public void setIhrZeichen(String IhrZeichen) {
        this.IhrZeichen = IhrZeichen;
    }

    public RechnungBetreffZZR getZeilenHandler() {
        return zeilenHandler;
    }

    public Integer getMahnungen() {
        return Mahnungen;
    }

    public void setMahnungen(Integer Mahnungen) {
        this.Mahnungen = Mahnungen;
    }

    private void setBezahlt(String string) {
        if (string.equals("1")) {
            setBezahlt(true);
        } else {
            setBezahlt(false);
        }
    }

    private void setStorno(String string) {
        if (string.equals("1")) {
            setStorno(true);
        } else {
            setStorno(false);
        }
    }

    public Angebot getAngebot() {
        return Angebot;
    }


    public void setNumberFormatHandler(NumberFormatHandler nfh) {
        this.nfh = nfh;
    }

    public String getTable() {
        return TABLE_BILLS;
    }

}
