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
package mp3.classes.objects.bill;

import mp3.database.util.Query;
import mp3.classes.layer.Popup;

/**
 *
 * @author anti43
 */
public class OfferProduct extends mp3.classes.layer.Things implements mp3.classes.interfaces.Structure {

    private String auftragid= "";
    private String anzahl = "";
    private String posten = "";
    private String preis = "";
    private String steuersatz = "";

    public OfferProduct(Query query) {
        super(query.clone(TABLE_ORDERS_DATA));

    }

    /**
     * 
     * @param query
     * @param id
     */
    public OfferProduct(Query query, String id) {
        super(query.clone(TABLE_ORDERS_DATA));
        this.id = Integer.valueOf(id);
        this.explode(this.selectLast("*", "id", id, true));
    }

    private void explode(String[] select) {
        try {

            this.setauftragid(select[1]);
            this.setAnzahl(select[2]);
            this.setPosten(select[3]);
            this.setPreis(select[4]);
            this.setSteuersatz(select[5]);

        } catch (Exception exception) {
        }

    }


    private String collect() {
        String str = "";
        str = str +  this.getauftragid() + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getAnzahl() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getPosten() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getPreis() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getSteuersatz() + "(;;2#4#1#1#8#0#;;)";
        
//        str = str + "(;;2#4#1#1#8#0#;;)" + "null" + "(;;2#4#1#1#8#0#;;)" + ",";
//        str = str + "(;;2#4#1#1#8#0#;;)" + "null" + "(;;2#4#1#1#8#0#;;)" + ",";
//        str = str + "(;;2#4#1#1#8#0#;;)" + "null" + "(;;2#4#1#1#8#0#;;)";
        
        return str;
    }

    public void save() {

        if (id > 0) {
            this.update(TABLE_ORDERS_DATA_FIELDS, this.collect(), id.toString());
            isSaved = true;
        } else if (id == 0) {
            this.insert(TABLE_ORDERS_DATA_FIELDS, this.collect());
        } else {

            mp3.classes.layer.Popup.warn(java.util.ResourceBundle.getBundle("languages/Bundle").getString("no_data_to_save"), Popup.WARN);

        }
    }

    public String getauftragid() {
        return auftragid;
    }

    public void setauftragid(String auftragid) {
        this.auftragid = auftragid;
    }

    public String getPosten() {
        return posten;
    }

    public void setPosten(String posten) {
        this.posten = posten;
    }

    public String getPreis() {
        return preis;
    }

    public void setPreis(String preis) {
        this.preis = preis;
    }

    public String getSteuersatz() {
        return steuersatz;
    }

    public void setSteuersatz(String steuersatz) {
        this.steuersatz = steuersatz;
    }

    public String getAnzahl() {
        return anzahl;
    }

    public void setAnzahl(String anzahl) {
        this.anzahl = anzahl;
    }

}
