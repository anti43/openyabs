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

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import mp3.classes.utils.Log;
import mp4.datenbank.verbindung.ConnectionHandler;

import mp4.datenbank.verbindung.Query;
import mp4.utils.datum.DateConverter;

/**
 *
 * @author anti43
 */
public class Product extends mp3.classes.layer.Things implements mp4.datenbank.struktur.Tabellen {

    private String Ean = "00000000";
    private String Nummer = "";
    private String Name = "";
    private Date Datum = new Date();
    private String url = "";
    private String Text = "";
    private Double VK = 0d;
    private Double EK = 0d;
    private Integer SteuersatzId = 1;
    private Integer HerstellerId = 1;
    private Integer LieferantenId = 1;
    private Integer WarengruppenId = 0;
    private Query query;
    public boolean isvalid = false;
    public Integer id = 0;
    private Hersteller hersteller;
    private Lieferant lieferant;
    private ProductImage image = new ProductImage();
    private URL ProductImageURL = null;

    public Integer getId() {
        return id;
    }

    public void destroy() {
        this.delete(this.id);
        this.id = 0;
    }

    public Product() {
        super(ConnectionHandler.instanceOf().clone(TABLE_PRODUCTS));
        lieferant = new Lieferant();
        this.query = ConnectionHandler.instanceOf();


    }

    public Product(String text, Double parseDezimal) {
        super(ConnectionHandler.instanceOf().clone(TABLE_PRODUCTS));
        this.setName(text);
        this.setVK(parseDezimal);

        this.save();

    }

    public Product(Query query) {
        super(query.clone(TABLE_PRODUCTS));
        lieferant = new Lieferant(query);
        this.query = query;
    }

    /**
     * 
     * @param query
     * @param id
     */
    public Product(Integer id) {
        super(ConnectionHandler.instanceOf().clone(TABLE_PRODUCTS));
        this.id = Integer.valueOf(id);
        this.explode(this.selectLast("*", "id", id.toString(), true));

        if (!this.getLieferantenId().equals(0)) {
            this.lieferant = new Lieferant(this.getLieferantenId());
        }

        if (!this.getHerstellerId().equals(0)) {
            this.hersteller = new Hersteller(this.getHerstellerId());
        }

        this.isvalid = true;
        this.query = ConnectionHandler.instanceOf();

        fetchImage();

    }

    public void setTaxID(int taxID) {
        this.setSteuersatzId(taxID);
    }

    private ProductImage fetchImage() {
        this.image = new ProductImage().searchImage(this.getId());
        return image;
    }

    public ProductImage getImage() {
        if (image != null) {
            return image;
        } else {
            return null;
        }
    }

    public URI getImagePath() {
        if (image != null) {
            return this.image.getURI();
        }
        return null;
    }

    public boolean isValid() {
        if (this.id > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void setHersteller(Hersteller hersteller) {
        this.hersteller = hersteller;
    }

    private void explode(String[] select) {

        this.setNummer(select[1]);
        this.setName(select[2]);

        this.setText(select[3]);
        this.setVK(Double.valueOf(select[4]));
        this.setEK(Double.valueOf(select[5]));

        this.setSteuersatzId(Integer.valueOf(select[6]));
        this.setHerstellerId(Integer.valueOf(select[7]));
        this.setLieferantenId(Integer.valueOf(select[8]));
        this.setWarengruppenId(Integer.valueOf(select[9]));

        this.setDatum(DateConverter.getDate(select[10]));
        this.setUrl(select[11]);
        this.setEan(select[12]);
    }

    public String[][] getAll() {

        Query q = query.clone(TABLE_PRODUCTS);

        String[][] str = q.selectFreeQuery("SELECT produkte.id, produkte.Produktnummer AS Nummer,produkte.Name,produkte.text," +
                "produkte.VK,produkte.EK,steuersaetze.wert,hersteller.firma AS Hersteller,Lieferanten.firma AS Lieferant," +
                "Warengruppenid,produkte.Datum,produkte.Url,produkte.EAN FROM produkte " +
                "LEFT OUTER JOIN  steuersaetze ON produkte.steuersatzid = steuersaetze.id " +
                "LEFT OUTER JOIN  lieferanten ON produkte.lieferantenid = lieferanten.id " +
                "LEFT OUTER JOIN  hersteller ON produkte.herstellerid = hersteller.id " +
                "LEFT OUTER JOIN  warengruppengruppen ON produkte.warengruppenid = warengruppengruppen.id " +
                "WHERE produkte.deleted = 0");

        return str;
    }

    private String collect() {
        String str = "";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getProduktNummer() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getName() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getText() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + this.getVK() + "(;;,;;)";
        str = str + this.getEK() + "(;;,;;)";
        str = str + this.getSteuersatzId() + "(;;,;;)";
        str = str + this.getHerstellerId() + "(;;,;;)";
        str = str + this.getLieferantenId() + "(;;,;;)";
        str = str + this.getWarengruppenId() + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + DateConverter.getSQLDateString(this.getDatum()) + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getUrl() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getEan() + "(;;2#4#1#1#8#0#;;)";

        return str;
    }

    public void save() {

        if (id > 0) {
            this.update(TABLE_PRODUCTS_FIELDS, this.collect(), id.toString());
            isSaved = true;
            fetchImage();
        } else if (id == 0) {
            this.id = this.insert(TABLE_PRODUCTS_FIELDS, this.collect());
            fetchImage();
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

//    private ProductFile[] getFiles(Query query) {
//        Query q = query.clone(TABLE_PRODUCTS_FILES);
//
//        String[] wher = {"productid", this.getId(), ""};
//
//        String[][] str = q.select("id", wher);
//        ProductFile[] prof = null;
//
//        for (int t = 0; t < str.length; t++) {
//
//            prof[t] = new ProductFile(query, str[0][t]);
//        }
//        return prof;
//    }
    public Lieferant getSupplier() {
        return lieferant;
    }

    public void setSupplier(mp4.klassen.objekte.Lieferant supplier) {


        if (supplier != null) {
            this.setLieferantenId(supplier.getId());
        } else {
            this.setLieferantenId(0);
        }
        this.lieferant = supplier;


        this.isSaved = false;
    }

//    public ProductFile[] getFiles() {
//        return files;
//    }
    public Integer getHerstellerId() {
        return HerstellerId;
    }

    public Integer getLieferantenId() {
        return LieferantenId;
    }

    public void setLieferantenId(Integer LieferantenId) {
        this.LieferantenId = LieferantenId;
    }

    public String getProduktNummer() {
        return Nummer;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return Text;
    }

    public void setText(String Text) {
        this.Text = Text;
    }

    public Double getVK() {
        return VK;
    }

    public void setVK(Double VK) {
        this.VK = VK;
    }

    public Double getEK() {
        return EK;
    }

    public void setEK(Double EK) {
        this.EK = EK;
    }

    public Double getTaxValue() {
        return Double.valueOf(ConnectionHandler.instanceOf().clone(TABLE_TAXES).select("wert", new String[]{"id", this.getSteuersatzId().toString(), ""})[0][0]);
    }

    public String getEan() {
        try {
            if (Long.valueOf(Ean) > 0) {
                return Ean;
            } else {
                return "";
            }
        } catch (NumberFormatException numberFormatException) {
            return Ean;
        }
    }

    public void setEan(String Ean) {
        this.Ean = Ean;
    }

    public String[][] getPrintModel() {

        Query q = query.clone(TABLE_PRODUCTS);

        String[][] str = q.selectFreeQuery("SELECT produkte.Produktnummer AS Nummer,produkte.Name,produkte.Text," +
                "produkte.VK,produkte.EK,produkte.Tax,Hersteller,Lieferanten.firma AS Lieferant," +
                "Warengruppenid,produkte.Datum,produkte.EAN FROM produkte " +
                "LEFT OUTER JOIN  lieferanten ON produkte.lieferantenid = lieferanten.id " +
                "LEFT OUTER JOIN  warengruppengruppen ON produkte.warengruppenid = warengruppengruppen.id");

        return str;
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

    private void setHerstellerId(Integer valueOf) {
        this.HerstellerId = valueOf;
    }

    public Hersteller getHersteller() {
        return hersteller;
    }
}
