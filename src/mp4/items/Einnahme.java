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

/*import mp3.classes.objects.*;*/
import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.einstellungen.Einstellungen;
import java.util.Date;
import mp4.datenbank.verbindung.Query;

import java.io.Serializable;
import mp4.items.*;


import mp4.logs.*;
import mp4.interfaces.Daemonable;
import mp4.utils.datum.DateConverter;
import mp4.utils.listen.ArrayUtils;
import mp4.utils.listen.ListenDataUtils;
import mp4.utils.tabellen.DataModelUtils;
import mp4.utils.zahlen.FormatNumber;
import mp4.utils.zahlen.FormatTax;

/**
 *
 * @author anti43
 */
public class Einnahme extends mp4.items.Things implements mp4.datenbank.installation.Tabellen, Daemonable, Serializable {
    private static final long serialVersionUID = 1L;

    private Integer Kontenid = 0;
    private String Beschreibung = "";
    private Double Preis = 0.0;
    private Double Tax = 0.0;
    private Date Datum = new Date();
    public  Integer id = 0;

    /**
     * 
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Disables this object 
     */
    public void disable() {
        if (super.getQueryHandler() == null) {
            super.setQueryHandler(ConnectionHandler.instanceOf().clone(TABLE_INCOME));
        }
        this.delete(this.id);
        
    }
    /**
     * Enables this object
     */
    public void enable() {
        if (super.getQueryHandler() == null) {
            super.setQueryHandler(ConnectionHandler.instanceOf().clone(TABLE_INCOME));
        }
        this.unDelete(this.id);
        
    }
    public Einnahme() {
        super(ConnectionHandler.instanceOf().clone(TABLE_INCOME));
        this.setKontenid(Einstellungen.instanceOf().getEinnahmeDefKonto().getId());
    }

    /**
     * 
     * @param kontoid
     * @param beschreibung
     * @param preis
     * @param tax
     * @param datum
     */
    public Einnahme(int kontoid, String beschreibung, double preis, double tax, Date datum) {
        super(ConnectionHandler.instanceOf().clone(TABLE_INCOME));

        this.setKontenid(kontoid);
        this.setBeschreibung(beschreibung);
        this.setPreis(preis);
        this.setTax(tax);
        this.setDatum(datum);

        this.save();
    }

    /**
     * 
     * @param id
     */
    public Einnahme(Integer id) {
        super(ConnectionHandler.instanceOf().clone(TABLE_INCOME));
        this.id = Integer.valueOf(id);
        this.explode(this.selectLast("*", "id", id.toString(), true));
    }

    /**
     * 
     * @param query
     * @param id 
     */
    public Einnahme(Query query, String id) {
        super(query.clone(TABLE_INCOME));
        this.id = Integer.valueOf(id);
        this.explode(this.selectLast("*", "id", id, true));
    }

    /**
     * 
     * @return
     */
    public String getFPreis() {
        return FormatNumber.formatDezimal(getPreis());
    }

    public String getFTax() {
        return FormatTax.formatDezimal(getTax());
    }

    private void explode(String[] select) {
        try {
            this.id = Integer.valueOf(select[0]);
            this.setKontenid(Integer.valueOf(select[1]));
            this.setBeschreibung(select[2]);
            this.setPreis(Double.valueOf(select[3]));
            this.setTax(Double.valueOf(select[4]));
            this.setDatum(DateConverter.getDate(select[5]));

        } catch (Exception numberFormatException) {
            Log.Debug(numberFormatException);
        }
    }

    private String collect() {
        String str = "";
        str = str + this.getKontenid() + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getBeschreibung() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + this.getPreis() + "(;;,;;)";
        str = str + this.getTax() + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + DateConverter.getSQLDateString(this.getDatum()) + "(;;2#4#1#1#8#0#;;)";
        return str;
    }

    public void save() {

        if (super.getQueryHandler() == null) {
            super.setQueryHandler(ConnectionHandler.instanceOf().clone(TABLE_INCOME));
        }

        if (id > 0) {
            this.update(TABLE_INCOME_FIELDS, this.collect(), id.toString());
            isSaved = true;
        } else if (id == 0) {
           this.id = this.insert(TABLE_INCOME_FIELDS, this.collect());
      
        } 
    }

    public String getFDatum() {
        return DateConverter.getDefDateString(getDatum());
    }

    public String[][] getAll() {

        Query q = ConnectionHandler.instanceOf().clone(TABLE_INCOME);

        Object[][] prods = q.select("id, id, preis, datum", null,false);//brutto

        Object[][] bills = new Rechnung(q).getPaid();

        return ArrayUtils.ObjectToStringArray(ArrayUtils.merge(DataModelUtils.inserValue(prods,"Eingabe",2), DataModelUtils.inserValue(bills, "Rechnung", 2)));
    }

    public Integer getKontenid() {
        return Kontenid;
    }

    public void setKontenid(int Kontenid) {
        this.Kontenid = Kontenid;
    }

    public String getBeschreibung() {
        return Beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.Beschreibung = beschreibung;
    }

    public Double getPreis() {
        return Preis;
    }

    public void setPreis(double Preis) {
        this.Preis = Preis;
    }

    public Double getTax() {
        return Tax;
    }

    public void setTax(double Tax) {
        this.Tax = Tax;
    }

    public Date getDatum() {
        return Datum;
    }

    public void setDatum(Date datum) {
        this.Datum = datum;
    }

//    private String[][] inserType(String[][] prods) {
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
//                        pro[i][2] = "Eingabe";
//                        m--;
//                    } else {
//
//                        pro[i][j] = prods[i][m];
//                    }
//                }
//            }
//        }
//        return pro;
//    }
    
    
}