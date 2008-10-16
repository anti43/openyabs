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
import java.util.logging.Level;
import java.util.logging.Logger;
import mp4.items.Things;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.logs.Log;

/**
 *
 * @author anti43
 */
public class RechnungBetreffzeile extends mp4.items.Things implements mp4.datenbank.installation.Tabellen {

    private Integer id = 0;
    private String[] value;
    private String name;
    private String text;
    private boolean isVorlage = false;
    private RechnungBetreffZZR handler;

    public RechnungBetreffzeile(String name, String text) {
        super(ConnectionHandler.instanceOf().clone(TABLE_BILL_TEXTS));
        this.name = name;
        this.text = text;
        this.save();
    }

    public Integer getId() {
        return id;
    }

    public void destroy() {
        ConnectionHandler.instanceOf().clone(TABLE_BILL_TEXTS_TO_BILLS).delete(new String[]{"id", this.getId().toString()});
        this.id = 0;
    }

    public RechnungBetreffzeile() {
        super(ConnectionHandler.instanceOf().clone(TABLE_BILL_TEXTS));
    }

    /**
     * 
     * @param id 
     */
    public RechnungBetreffzeile(Integer id) {
        super(ConnectionHandler.instanceOf().clone(TABLE_BILL_TEXTS));
        this.id = id;
        try {
            value = this.selectLast("*", "id", id.toString(), true);
        } catch (Exception ex) {
             Log.Debug(ex);
        }
        explode(value);

        
    }

    public String[][] getAllData() {
        return this.select("id, name, text", null, null, false);
    }

    public String[][] getDataOf(Integer rechnungId) {
        return this.select("*", "rechnungid", rechnungId.toString(), true);
    }

    public String[][] getVorlagen() {
        return this.select("id, name, text", "isvorlage", "1", true);
    }

    public void isVorlage(boolean vorlage) {
        isVorlage = vorlage;
    }

    public String[][] getVorlagenIds() {
       return this.select("id", "isvorlage", "1", true);
    }

    void setHandler(RechnungBetreffZZR handler) {
        this.handler = handler;
    }

    private String collect() {
        String str = "";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getName() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getText() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";

        if (isVorlage) {
            str = str + "1";
        } else {
            str = str + "0";
        }
        return str;
    }

    private void explode(String[] select) {

        this.setName(select[1]);
        this.setText(select[2]);

        if (select[3].matches("1")) {
            isVorlage(true);
        }
    }

    public void save() {

        if (getId() > 0) {
            this.update(TABLE_BILL_TEXTS_FIELDS, this.collect(), getId());
            isSaved = true;
        } else if (getId() == 0) {
            id = this.insert(TABLE_BILL_TEXTS_FIELDS, this.collect(),null);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

   
}
