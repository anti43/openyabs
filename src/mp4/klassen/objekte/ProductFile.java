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
import mp3.classes.interfaces.Structure;
import mp3.classes.layer.Popup;
import mp4.utils.datum.DateConverter;

/**
 *
 * @author anti43
 */
public class ProductFile extends mp3.classes.layer.Things implements mp3.classes.interfaces.Structure {

    private Integer Produktid = null;
    private String Url = "";
    private Date Datum = new Date();

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

        this.setProduktid(Integer.valueOf(select[1]));
        this.setUrl(select[2]);
        this.setDatum(DateConverter.getDate(select[3]));
    }

    private String collect() {
        String str = "";
        str = str + this.getProduktid() + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getUrl() + "(;;2#4#1#1#8#0#;;)" +"(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getDatum()+ "(;;2#4#1#1#8#0#;;)" ;
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

    public Integer getProduktid() {
        return Produktid;
    }

    public void setProduktid(Integer Produktid) {
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

    public Date getDatum() {
        return Datum;
    }

    public void setDatum(Date Datum) {
        this.Datum = Datum;
        this.isSaved = false;
    }
}
