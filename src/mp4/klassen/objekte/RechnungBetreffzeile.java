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


import mp3.classes.layer.QueryClass;

/**
 *
 * @author anti43
 */
public class RechnungBetreffzeile extends mp3.classes.layer.Things implements mp4.datenbank.struktur.Tabellen {

    private Integer id = 0;
    private String[] value;
    private String name;
    private String text;
    private boolean isVorlage = false;
    private int rechnungID = 0;

    public Integer getId() {
        return id;
    }

    public void destroy() {
        this.delete(this.getId());
        this.id = 0;
    }

    public RechnungBetreffzeile() {
        super(QueryClass.instanceOf().clone(TABLE_BILL_TEXTS));
    }

    /**
     * 
     * @param query
     * @param id 
     */
    public RechnungBetreffzeile(Integer id) {
        super(QueryClass.instanceOf().clone(TABLE_BILL_TEXTS));
        this.id = id;
        value = this.selectLast("*", "id", id.toString(), true);
        explode(value);
    }

    public String[][] getAllData() {
        return this.select("id, name, text", null, null, false);
    }
    
    public String[][] getDataOf(Integer rechnungId) {
        return this.select("*", "rechnungid", rechnungId.toString(), true);
    }

    private String collect() {
        String str = "";

        str = str + this.getRechnungID() + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getName() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getText() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";

        return str;
    }
    private void explode(String[] select) {

        this.setRechnungID(Integer.valueOf(select[1]));
        this.setName(select[2]);
        this.setText(select[3]);
    }
    public void save() {

        if (getId() > 0) {
            this.update(TABLE_BILL_TEXTS_FIELDS, this.collect(), getId().toString());
            isSaved = true;
        } else if (getId() == 0) {
            this.insert(TABLE_BILL_TEXTS_FIELDS, this.collect());
        } 
    }

    public String[] getValue() {
        return value;
    }

    public void setValue(String[] value) {
        this.value = value;
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

    public int getRechnungID() {
        return rechnungID;
    }

    public void setRechnungID(int rechnungID) {
        this.rechnungID = rechnungID;
    }
}
