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

import mp4.items.handler.ProductGroupHandler;
import java.util.Date;

import mp4.interfaces.Countable;
import mp4.datenbank.verbindung.ConnectionHandler;

import mp4.datenbank.verbindung.PrepareData;
import mp4.datenbank.verbindung.Query;
import mp4.items.handler.NumberFormatHandler;
import mp4.utils.datum.DateConverter;

/**
 *
 * @author anti43
 */
public class Dienstleistung extends mp3.classes.layer.Things implements mp4.datenbank.installation.Tabellen, Countable {

    
    private String Nummer = "";
    private String Name = "";
    private String Beschreibung = "";
    private String Einheit = "";
    private Double Preis = 0d;
    private Integer SteuersatzId = 1;
    private Integer WarengruppenId = 0;
    private Date Datum = new Date();
    
    private Query query;
    public boolean isvalid = false;
    public Integer id = 0;


    public Integer getId() {
        return id;
    }

    public void destroy() {
        this.delete(this.id);
        this.id = 0;
    }

    public Dienstleistung() {
        super(ConnectionHandler.instanceOf().clone(TABLE_SERVICES));
        this.query = ConnectionHandler.instanceOf();
        nfh = new NumberFormatHandler(this, new Date());
    }

    public Dienstleistung(String text, Double parseDezimal) {
        super(ConnectionHandler.instanceOf().clone(TABLE_SERVICES));
        this.setName(text);
        this.setPreis(parseDezimal);
        nfh = new NumberFormatHandler(this, new Date());
        this.save();
    }


    /**
     * 
     * @param query
     * @param id
     */
    public Dienstleistung(Integer id) {
        super(ConnectionHandler.instanceOf().clone(TABLE_SERVICES));
        this.id = Integer.valueOf(id);
        this.explode(this.selectLast("*", "id", id.toString(), true));
        this.isvalid = true;
        this.query = ConnectionHandler.instanceOf();
        nfh = new NumberFormatHandler(this, new Date());
    }

    public void setTaxID(int taxID) {
        this.setSteuersatzId(taxID);
    }


    public boolean isValid() {
        if (this.id > 0) {
            return true;
        } else {
            return false;
        }
    }


    private void explode(String[] select) {

        this.setNummer(select[1]);
        this.setName(select[2]);

        this.setBeschreibung(select[3]);
        this.setEinheit(select[4]);
        this.setPreis(Double.valueOf(select[5]));

        this.setSteuersatzId(Integer.valueOf(select[6]));
        this.setWarengruppenId(Integer.valueOf(select[7]));

        this.setDatum(DateConverter.getDate(select[8]));

    }

    public String[][] getAll() {

        Query q = query.clone(TABLE_SERVICES);

        String[][] str = q.selectFreeQuery("SELECT dienstleistungen.id, dienstleistungen.Produktnummer AS Nummer,dienstleistungen.Name,dienstleistungen.beschreibung," +
                "dienstleistungen.preis," + "dienstleistungen.einheit," +
                "Warengruppenid,dienstleistungen.Datum FROM dienstleistungen " +
                "LEFT OUTER JOIN  steuersaetze ON dienstleistungen.steuersatzid = steuersaetze.id " +
                "LEFT OUTER JOIN  warengruppengruppen ON dienstleistungen.warengruppenid = warengruppengruppen.id " +
                "WHERE dienstleistungen.deleted = 0", null);
        return str;
    }

    private String collect() {
        String str = "";
        str = str + PrepareData.prepareString(this.getNummer());
        str = str + PrepareData.prepareString(this.getName());
        str = str + PrepareData.prepareString(this.getBeschreibung());
        str = str + PrepareData.prepareString(this.getEinheit());
        str = str + PrepareData.prepareNumber(this.getPreis());
        str = str + PrepareData.prepareNumber(this.getSteuersatzId());

        str = str + PrepareData.prepareNumber(this.getWarengruppenId());
        str = str + PrepareData.prepareString(DateConverter.getSQLDateString(this.getDatum()));
        return PrepareData.finalize(str);
    }

    public void save() {

        if (id > 0) {
            this.update(TABLE_SERVICES_FIELDS, this.collect(), id.toString());
            isSaved = true;
           
        } else if (id == 0) {
            this.id = this.insert(TABLE_SERVICES_FIELDS, this.collect());
           
        }
    }

    public Integer getWarengruppenId() {
        return WarengruppenId;
    }

    public String getProductgroupPath() {
        return ProductGroupHandler.instanceOf().getHierarchyPath(this.getWarengruppenId());
    }

    public void setWarengruppenId(Integer Warengruppenid) {
        this.WarengruppenId = Warengruppenid;
        this.isSaved = false;
    }


    public String getProduktNummer() {
        return getNummer();
    }

    public void setNummer(String Nummer) {
        this.Nummer = Nummer;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public Date getDatum() {
        return Datum;
    }

    public void setDatum(Date Datum) {
        this.Datum = Datum;
    }

    public Double getTaxValue() {
        return Double.valueOf(ConnectionHandler.instanceOf().clone(TABLE_TAXES).select("wert", new String[]{"id", this.getSteuersatzId().toString(), ""})[0][0]);
    }


    public int delete(String id) {
        return delete(Integer.valueOf(id));
    }

    public Integer getSteuersatzId() {
        return SteuersatzId;
    }

    public void setSteuersatzId(Integer SteuersatzId) {
        this.SteuersatzId = SteuersatzId;
    }

    public String getNummer() {
        return Nummer;
    }

    public String getBeschreibung() {
        return Beschreibung;
    }

    public void setBeschreibung(String Beschreibung) {
        this.Beschreibung = Beschreibung;
    }

    public String getEinheit() {
        return Einheit;
    }

    public void setEinheit(String Einheit) {
        this.Einheit = Einheit;
    }

    public Double getPreis() {
        return Preis;
    }

    public void setPreis(Double Preis) {
        this.Preis = Preis;
    }


    public String getTable() {
        return TABLE_SERVICES;
    }

    public String getCountColumn() {
       return "produktnummer";
    }

}
