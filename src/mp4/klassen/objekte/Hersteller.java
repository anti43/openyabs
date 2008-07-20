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

import mp4.datenbank.verbindung.Query;
import mp3.classes.layer.Popup;

/**
 *
 * @author anti
 */
public class Hersteller extends mp3.classes.layer.People implements mp4.datenbank.struktur.Tabellen {

   
    private String herstellernummer = "";
    private String Firma = "";
    private String Anrede = "";
    private String Vorname = "";
    private String Name = "";
    private String Str = "";
    private String PLZ = "";
    private String Ort = "";
    private String Tel = "";
    private String Mobil = "";
    private String Mail = "";
    private String Webseite = "";
    private String Notizen = "";


    public Hersteller(Query query) {
        super(query.clone(TABLE_MANUFACTURER));
    }

    public Hersteller(Query query, String id) {
        super(query.clone(TABLE_MANUFACTURER));
        this.id=Integer.valueOf(id);
        this.explode(this.selectLast("*", "id", id, true,false,false));
    }
    public String getid() {
        return id.toString();
    }
    public String getHerstellernummer() {
        return herstellernummer;
    }

    public void setHerstellernummer(String Herstellernummer) {
        this.herstellernummer = Herstellernummer;
        this.isSaved = false;
    }

    public String getFirma() {
        return Firma;
    }

    public void setFirma(String Firma) {
        this.Firma = Firma;
        this.isSaved = false;
    }

    public String getAnrede() {
        return Anrede;
    }

    public void setAnrede(String Anrede) {
        this.Anrede = Anrede;
        this.isSaved = false;
    }

    public String getVorname() {
        return Vorname;
    }

    public void setVorname(String Vorname) {
        this.Vorname = Vorname;
        this.isSaved = false;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
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
        str = str + this.getHerstellernummer() + "(;;,;;)";
        str = str + this.getFirma() + "(;;,;;)";
        str = str + this.getAnrede() + "(;;,;;)";
        str = str + this.getVorname() + "(;;,;;)";
        str = str + this.getName() + "(;;,;;)";
        str = str + this.getStr() + "(;;,;;)";
        str = str + this.getPLZ() + "(;;,;;)";
        str = str + this.getOrt() + "(;;,;;)";
        str = str + this.getTel() + "(;;,;;)";
        str = str + this.getMobil() + "(;;,;;)";
        str = str + this.getMail() + "(;;,;;)";
        str = str + this.getWebseite() + "(;;,;;)";
        str = str + this.getNotizen();

        return str;
    }

    private void explode(String[] str) {

        this.setHerstellernummer(str[1]);
        this.setFirma(str[2]);
        this.setAnrede(str[3]);
        this.setVorname(str[4]);
        this.setName(str[5]);
        this.setStr(str[6]);
        this.setPLZ(str[7]);
        this.setOrt(str[8]);
        this.setTel(str[9]);
        this.setMobil(str[10]);
        this.setMail(str[11]);
        this.setWebseite(str[12]);
        this.setNotizen(str[12+1]);
    }


    public void save() {

        
        if (id > 0) {
            this.update(TABLE_MANUFACTURER_FIELDS, this.collect(), id.toString());
            isSaved = true;
        } else if (id == 0) {
            this.insert(TABLE_MANUFACTURER_FIELDS, this.collect());
        }else{
        
            mp3.classes.layer.Popup.warn(java.util.ResourceBundle.getBundle("languages/Bundle").getString("no_data_to_save"),Popup.WARN);
        
        }
    }
}
