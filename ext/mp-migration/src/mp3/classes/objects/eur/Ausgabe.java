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

package mp3.classes.objects.eur;


import mp3.classes.layer.QueryClass;
import java.util.Date;
import mp3.database.util.Query;
import mp3.classes.interfaces.Structure;
import mp3.classes.layer.*;
import mp3.classes.utils.*;

/**
 *
 * @author anti43
 */
public class Ausgabe extends mp3.classes.layer.Things implements mp3.classes.interfaces.Structure, mp3.classes.interfaces.Daemonable{
//  "kontenid INTEGER DEFAULT NULL, beschreibung varchar(500) default NULL,"+
//  "preis varchar(50) default NULL,"+"tax varchar(50) default NULL,"+"datum varchar(50) default NULL,"+
    private String Kontenid = "0";
    private String Beschreibung = "";
    private String Preis = "0";
    private String Tax = "0";
    private String Datum = "00.00.0000";

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
    public Ausgabe(String kontoid, String beschreibung, String preis, String tax, Date datum) {
        super(QueryClass.instanceOf().clone(TABLE_DUES));
        
        this.setKontenid(kontoid);
        this.setBeschreibung(beschreibung);
        this.setPreis(preis);
        this.setTax(tax);
        this.setDatum(Formater.formatDate(datum));
        
        this.save();
    }

    /**
     * 
     * @param query
     * @param id 
     */
    public Ausgabe(Query query, String id) {
        super(query.clone(Structure.TABLE_DUES));
        this.id = Integer.valueOf(id);
        this.explode(this.selectLast("*", "id", id, true));
    }


    private void explode(String[] select) {
        try {
            this.id = Integer.valueOf(select[0]);
            this.setKontenid(select[1]);
            this.setBeschreibung(select[2]);
            this.setPreis(select[3]);
            this.setTax(select[4]);
            this.setDatum(select[5]);

        } catch (NumberFormatException numberFormatException) {
        }

        
    }

        private String collect() {
        String str = "";
        str = str  + this.getKontenid() +  "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)"  + this.getBeschreibung()  + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)"  + this.getPreis()  + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)"  + this.getTax()  + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)"  + this.getDatum() + "(;;2#4#1#1#8#0#;;)" ;
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

        String[][] prods = q.select("preis, datum", null);

        return prods;
    }

    private String[][] inserType(String[][] prods) {
      String[][] pro = null;
      if(prods.length>0){
          pro =  new String[prods.length][prods[0].length +1];
          
          for (int i = 0; i < pro.length; i++) {
           int m=0;
              for (int j=0; j < pro[i].length; j++,m++) {
                  
                  
                  if(j==2) {
                        pro[i][2] = new SKRKonto(prods[i][0]).getGruppe();

                        m--;
                    }else {
                      
                                          
                        pro[i][j] = prods[i][m];
                    }

              }
          }
      } 
      return pro;
    }
    public String getKontenid() {
        return Kontenid;
    }

    public void setKontenid(String Kontenid) {
        this.Kontenid = Kontenid;
    }

    public String getBeschreibung() {
        return Beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.Beschreibung = beschreibung;
    }

    public String getPreis() {
        return Preis;
    }

    public void setPreis(String Preis) {
        this.Preis = Preis;
    }

    public String getTax() {
        return Tax;
    }

    public void setTax(String Tax) {
        this.Tax = Tax;
    }

    public String getDatum() {
        return Datum;
    }

    public void setDatum(Date Datum) {
        this.Datum = Formater.formatDate(Datum);
    }
     private void setDatum(String Datum) {
        this.Datum = Datum;
    }







}