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

import java.util.Date;
import mp4.interfaces.Countable;
import mp4.globals.Strings;
import mp4.datenbank.verbindung.Query;
import mp4.items.handler.NumberFormatHandler;
import mp4.items.Popup;
import mp4.datenbank.verbindung.ConnectionHandler;

/**
 *
 * @author anti
 */
public class Lieferant extends mp4.items.People implements mp4.datenbank.installation.Tabellen, Countable {

  
    private String Lieferantennummer = "";
    private String Firma = "";
    private String Anrede = "";
    private String Vorname = "";
    private String Name = "";
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
    private NumberFormatHandler nfh;

    public Lieferant() {
        super(ConnectionHandler.instanceOf().clone(TABLE_SUPPLIER));
        this.id = 0;
        this.query =ConnectionHandler.instanceOf();
        nfh = new NumberFormatHandler(this, new Date());
    }



    public Lieferant(Query query) {
        super(query.clone(TABLE_SUPPLIER));
        this.id = 0;
        this.query =query;
        nfh = new NumberFormatHandler(this, new Date());
    }

    public Lieferant(Integer id) {
        super(ConnectionHandler.instanceOf().clone(TABLE_SUPPLIER));
        this.query =ConnectionHandler.instanceOf();
        this.id = id;
        this.explode(this.selectLast("*", "id", id.toString(), true));
        nfh = new NumberFormatHandler(this, new Date());
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

    public String getid() {
        return this.id.toString();
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
        str = str +  "(;;2#4#1#1#8#0#;;)" + this.getAnrede() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str +  "(;;2#4#1#1#8#0#;;)" + this.getVorname() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str +  "(;;2#4#1#1#8#0#;;)" + this.getName() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
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
            this.setAnrede(str[3]);
            this.setVorname(str[4]);
            this.setName(str[5]);
            this.setStr(str[6]);
            this.setPLZ(str[7]);
            this.setOrt(str[8]);
            this.setTel(str[9]);
            this.setFax(str[10]);
            this.setMobil(str[11]);
            this.setMail(str[12]);
            this.setWebseite(str[12+1]);
            this.setNotizen(str[12+2]);
          
    }

    public void save() {

        if (id > 0) {
            this.update(TABLE_SUPPLIER_FIELDS, this.collect(), id.toString());
            isSaved = true;
        } else if (id == 0) {
            this.id = this.insert(TABLE_SUPPLIER_FIELDS, this.collect(),new int[]{0});
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

    public String getAnrede() {
        return Anrede;
    }

    public void setAnrede(String Anrede) {
        this.Anrede = Anrede;
    }

    public String getVorname() {
        return Vorname;
    }

    public void setVorname(String Vorname) {
        this.Vorname = Vorname;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }
    
   public boolean isValid() {
        if(this.id > 0) {
            return true;
        }
        else {
            return false;
        }
    }

      public Date getDatum() {
        return new Date();
    }

    public String getTable() {
        return TABLE_SUPPLIER;
    }

    public String getCountColumn() {
       return "nummer";
    }

    public NumberFormatHandler getNfh() {
        return nfh;
    }
}
