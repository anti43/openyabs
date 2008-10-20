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

import mp4.datenbank.verbindung.DataLock;
import mp4.datenbank.installation.Tabellen;
import mp4.datenbank.verbindung.EasyQuery;
import mp4.datenbank.verbindung.PrepareData;

import mp4.interfaces.Queries;
import mp4.datenbank.verbindung.Query;
import mp4.interfaces.Lockable;
import mp4.logs.Log;

/**
 *
 * @author anti
 */
public abstract class People extends EasyQuery implements Queries, Tabellen, Lockable {

    public Integer id = 0;
    public boolean isSaved = false;
    public boolean readonly = false;
    private String Nummer = "";
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
    private DataLock datalock;


    public People(Query query) {
        super(query);
        this.query = query;
    }

    @Override
    public boolean lock() {
        datalock = new DataLock(query, id);
        return getDatalock().lockRow();
    }

    @Override
    public void unlock() {
        try {
            getDatalock().unLockRow();
        } catch (Exception e) {
        }
    }

    @Override
    public void finalize(){
        unlock();
    }

    public void setNummer(String Kundennummer) {
        this.Nummer = Kundennummer;
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

    public String getNummer() {
        return Nummer;
    }

    public String getFax() {
        return Fax;
    }

    public void setFax(String Fax) {
        this.Fax = Fax;
        this.isSaved = false;
    }

    /**
     * Collect the data, MUST be overridden with PrepareData.finalize(collect())
     * @return The database insert string
     */
    public String collect() {
        String str = PrepareData.prepareString(this.getNummer());
        str = str + PrepareData.prepareString(this.getFirma());
        str = str + PrepareData.prepareString(this.getAnrede());
        str = str + PrepareData.prepareString(this.getVorname());
        str = str + PrepareData.prepareString(this.getName());
        str = str + PrepareData.prepareString(this.getStr());
        str = str + PrepareData.prepareString(this.getPLZ());
        str = str + PrepareData.prepareString(this.getOrt());
        str = str + PrepareData.prepareString(this.getTel());
        str = str + PrepareData.prepareString(this.getFax());
        str = str + PrepareData.prepareString(this.getMobil());
        str = str + PrepareData.prepareString(this.getMail());
        str = str + PrepareData.prepareString(this.getWebseite());
        str = str + PrepareData.prepareString(this.getNotizen());
        return str;
    }

    public String expose() {
        return (collect().replaceAll("(;;2#4#1#1#8#0#;;)", " ").replaceAll("(;;,;;)", "\n"));
    }

    public void explode(String[] str) {
        try {
            this.id = Integer.valueOf(str[0]);
            this.setNummer(str[1]);
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
            this.setWebseite(str[12 + 1]);
            this.setNotizen(str[12 + 2]);
        } catch (Exception exception) {
            Log.Debug(this,exception);
        }
    }

    public Integer getId() {
        return id;
    }

    public void destroy() {
        this.delete(this.id);
        this.id = 0;
    }

    public DataLock getDatalock() {
        return datalock;
    }

}
