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

import mp3.classes.interfaces.Strings;
import handling.db.Query;

import mp3.classes.layer.Popup;
import mp3.classes.layer.QueryClass;

/**
 *
 * @author anti
 */
public class Lieferant extends mp3.classes.layer.People implements mp4.datenbank.struktur.Tabellen {

  
    private String Lieferantennummer = "";
    private String Firma = "";
//    private String Anrede = "";
//    private String Vorname = "";
//    private String Name = "";
    private String Str = "";
    private String PLZ = "";
    private String Ort = "";
    private String Tel = "";
    private String Fax = "";
    private String Mobil = "";
    private String Mail = "";
    private String Webseite = "";
    private String Notizen = "";
    private Query query;

    public Lieferant() {
        super(QueryClass.instanceOf().clone(TABLE_SUPPLIER));
        this.id = 0;
        this.query =QueryClass.instanceOf();
        
    }
    

    public Lieferant(Query query) {
        super(query.clone(TABLE_SUPPLIER));
        this.id = 0;
        this.query =query;

    }

    public Lieferant(Query query, Integer id) {
        super(query.clone(TABLE_SUPPLIER));
        this.query =query;
        
//        if(this.id!=0) {
            this.explode(this.selectLast("*", "id", id.toString(), true, false, false));
//        }
    }


    public String[][] getProducts(){
    
        Query q = query.clone(TABLE_PRODUCTS);

        String[][] str = q.select("produkte.id, produkte.name, produkte.hersteller", null, TABLE_SUPPLIER, "lieferantenid");
   
        return str;  
    
    
    }
    
    
    public String[][] getAll(){
    
        Query q = query.clone(TABLE_SUPPLIER);

        String[][] str = q.select(Strings.ALL, null);
   
        return str;  
    }


    public String getLieferantennummer() {
        return Lieferantennummer;
    }

    public void setLieferantennummer(String Lieferantennummer) {
        this.Lieferantennummer = Lieferantennummer;
        this.isSaved = false;
    }

    public String getFirma() {
        return Firma;
    }

    public void setFirma(String Firma) {
        this.Firma = Firma;
        this.isSaved = false;
    }


    public String getStr() {
        return Str;
    }

    public void setStr(String Str) {
        this.Str = Str;
        this.isSaved = false;
    }

    public String getPLZ() {
        return PLZ;
    }

    public void setPLZ(String PLZ) {
        this.PLZ = PLZ;
        this.isSaved = false;
    }

    public String getOrt() {
        return Ort;
    }

    public void setOrt(String Ort) {
        this.Ort = Ort;
        this.isSaved = false;
    }

    public String getTel() {
        return Tel;
    }

    public void setTel(String Tel) {
        this.Tel = Tel;
        this.isSaved = false;
    }

    public String getMobil() {
        return Mobil;
    }

    public void setMobil(String Mobil) {
        this.Mobil = Mobil;
        this.isSaved = false;
    }

    public String getMail() {
        return Mail;
    }

    public void setMail(String Mail) {
        this.Mail = Mail;
        this.isSaved = false;
    }

    public String getWebseite() {
        return Webseite;
    }

    public void setWebseite(String Webseite) {
        this.Webseite = Webseite;
        this.isSaved = false;
    }

    public String getNotizen() {
        return Notizen;
    }

    public void setNotizen(String Notizen) {
        this.Notizen = Notizen;
        this.isSaved = false;
    }

    private String collect() {
        String str = "";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getLieferantennummer() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str +  "(;;2#4#1#1#8#0#;;)" + this.getFirma() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
       
        str = str +  "(;;2#4#1#1#8#0#;;)" + this.getStr() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str +  "(;;2#4#1#1#8#0#;;)" + this.getPLZ() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str +  "(;;2#4#1#1#8#0#;;)" + this.getOrt() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str +  "(;;2#4#1#1#8#0#;;)" + this.getTel() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str +  "(;;2#4#1#1#8#0#;;)" + this.getFax() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str +  "(;;2#4#1#1#8#0#;;)" + this.getMobil() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str +  "(;;2#4#1#1#8#0#;;)" + this.getMail() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str +  "(;;2#4#1#1#8#0#;;)" + this.getWebseite() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str +  "(;;2#4#1#1#8#0#;;)" + this.getNotizen() + "(;;2#4#1#1#8#0#;;)";

        return str;
    }

    private void explode(String[] str) {
            this.setLieferantennummer(str[1]);
            this.setFirma(str[2]);
            
            this.setStr(str[3]);
            this.setPLZ(str[4]);
            this.setOrt(str[5]);
            this.setTel(str[6]);
            this.setFax(str[7]);
            this.setMobil(str[8]);
            this.setMail(str[9]);
            this.setWebseite(str[10]);
            this.setNotizen(str[11]);
          
    }

//    public void destroy() {
//        this.delete(this.id.toString());
//        this.id=0;
//    }

    public void save() {

        if (id > 0) {
            this.update(TABLE_SUPPLIER_FIELDS, this.collect(), id.toString());
            isSaved = true;
        } else if (id == 0) {
            this.insert(TABLE_SUPPLIER_FIELDS, this.collect());
        }else{
        
            mp3.classes.layer.Popup.warn(java.util.ResourceBundle.getBundle("languages/Bundle").getString("no_data_to_save"),Popup.WARN);
        
        }
    }

    public String getFax() {
        return Fax;
    }

    public void setFax(String Fax) {
        this.Fax = Fax;
    }
    
     public String[][] getPrintModel(){
    
        Query q = query.clone(TABLE_SUPPLIER);

        String[][] str = q.select(TABLE_SUPPLIER_PRINT_FIELDS, null);
   
        return str;  
    }
}
