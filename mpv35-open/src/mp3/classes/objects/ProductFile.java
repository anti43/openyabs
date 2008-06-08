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
package mp3.classes.objects;

import mp3.database.util.Query;
import mp3.classes.interfaces.Structure;
import mp3.classes.layer.Popup;

/**
 *
 * @author anti43
 */
public class ProductFile extends mp3.classes.layer.Things implements mp3.classes.interfaces.Structure {

    private String Produktid = "";
    private String Url = "";
    private String Datum = "";

    public ProductFile(Query query) {
        super(query.clone(TABLE_PRODUCTS_FILES));
    }

    /**
     * 
     * @param query
     * @param id 
     */
    public ProductFile(Query query, String id) {
        super(query.clone(Structure.TABLE_PRODUCTS_FILES));
        this.id = Integer.valueOf(id);
        this.explode(this.selectLast("*", "id", id, true));
    }

    private void explode(String[] select) {

        this.setProduktid(select[1]);
        this.setUrl(select[2]);
        this.setDatum(select[3]);


    }

    private String collect() {
        String str = "";
        str = str + this.getProduktid() + "(;;,;;)";
        str = str + this.getUrl() + "(;;,;;)";
        str = str + this.getDatum();
        return str;
    }

    public void save() {

        if (id > 0) {
            this.update(TABLE_PRODUCTS_FILES_FIELDS, this.collect(), id.toString());
            isSaved = true;
        } else if (id == 0) {
            this.insert(TABLE_PRODUCTS_FILES_FIELDS, this.collect());
        } else {

            mp3.classes.layer.Popup.warn(java.util.ResourceBundle.getBundle("languages/Bundle").getString("no_data_to_save"), Popup.WARN);

        }
    }

    public String getProduktid() {
        return Produktid;
    }

    public void setProduktid(String Produktid) {
        this.Produktid = Produktid;
        this.isSaved = false;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String Url) {
        this.Url = Url;
        this.isSaved = false;
    }

    public String getDatum() {
        return Datum;
    }

    public void setDatum(String Datum) {
        this.Datum = Datum;
        this.isSaved = false;
    }
}
