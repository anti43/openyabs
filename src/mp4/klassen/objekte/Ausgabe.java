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
package mp4.klassen.objekte;

import java.util.Date;
import mp3.database.util.Query;

import mp3.classes.layer.*;
import mp4.klassen.objekte.SKRKonto;
import mp3.classes.utils.*;
import mp4.utils.datum.DateConverter;

/**
 *
 * @author anti43
 */
public class Ausgabe extends mp3.classes.layer.Things implements mp4.datenbank.struktur.Tabellen, mp3.classes.interfaces.Daemonable {
//  "kontenid INTEGER DEFAULT NULL, beschreibung varchar(500) default NULL,"+
//  "preis varchar(50) default NULL,"+"tax varchar(50) default NULL,"+"datum varchar(50) default NULL,"+

    private Integer Kontenid = 0;
    private String Beschreibung = "";
    private Double Preis = 0d;
    private Double Tax = 0d;
    private Date Datum = new Date();

    public Ausgabe() {
        super(QueryClass.instanceOf().clone(TABLE_DUES));
    }

    /**
     * 
     * @param kontoid
     * @param beschreibung
     * @param preis
     * @param tax
     * @param datum
     */
    public Ausgabe(int kontoid, String beschreibung, double preis, double tax, Date datum) {
        super(QueryClass.instanceOf().clone(TABLE_DUES));

        this.setKontenid(kontoid);
        this.setBeschreibung(beschreibung);
        this.setPreis(preis);
        this.setTax(tax);
        this.setDatum(datum);

        this.save();
    }

    /**
     * 
     * @param query
     * @param id 
     */
    public Ausgabe(Query query, String id) {
        super(query.clone( TABLE_DUES));
        this.id = Integer.valueOf(id);
        this.explode(this.selectLast("*", "id", id, true));
    }

    public Ausgabe(Integer id, String text, String replaceAll, String replaceAll0, Date date) {
          super(QueryClass.instanceOf().clone( TABLE_DUES));
        throw new UnsupportedOperationException("Not yet implemented");
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
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getDatum() + "(;;2#4#1#1#8#0#;;)";
        return str;
    }

    public void save() {

        if (id > 0) {
            this.update(TABLE_OUTGOINGS_FIELDS, this.collect(), id.toString());
            isSaved = true;
        } else if (id == 0) {
            this.insert(TABLE_OUTGOINGS_FIELDS, this.collect());
        } else {

            mp3.classes.layer.Popup.warn(java.util.ResourceBundle.getBundle("languages/Bundle").getString("no_data_to_save"), Popup.WARN);

        }
    }

    public String[][] getAll() {

        Query q = QueryClass.instanceOf().clone(TABLE_DUES);

        String[][] prods = q.select("kontenid,id,preis, datum", null);

        return inserType(prods);
    }

    public String getFDatum() {
        return DateConverter.getDefDateString(getDatum());
    }

    private String[][] inserType(String[][] prods) {
        String[][] pro = null;
        if (prods.length > 0) {
            pro = new String[prods.length][prods[0].length + 1];

            for (int i = 0; i < pro.length; i++) {
                int m = 0;
                for (int j = 0; j < pro[i].length; j++, m++) {


                    if (j == 2) {
                        pro[i][2] = new SKRKonto(Integer.valueOf(prods[i][0])).getGruppe();

                        m--;
                    } else {


                        pro[i][j] = prods[i][m];
                    }

                }
            }
        }
        return pro;
    }

    public Integer getKontenid() {
        return Kontenid;
    }

    public void setKontenid(Integer Kontenid) {
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

    public void setPreis(Double Preis) {
        this.Preis = Preis;
    }

    public Double getTax() {
        return Tax;
    }

    public void setTax(Double Tax) {
        this.Tax = Tax;
    }

    public Date getDatum() {
        return Datum;
    }

    public void setDatum(Date Datum) {
        this.Datum = Datum;
    }

}