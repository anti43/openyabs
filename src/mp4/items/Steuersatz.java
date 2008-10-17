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

import mp4.logs.*;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.datenbank.verbindung.PrepareData;

/**
 *
 * @author anti43
 */
public class Steuersatz extends mp4.items.Things implements mp4.datenbank.installation.Tabellen {

    private String Name = null;
    private Double Wert = 0d;


    public void destroy() {
        if (!readonly) {
        this.delete(this.getId());
        this.id = 0;
        }
    }

    public Steuersatz() {
        super(ConnectionHandler.instanceOf().clone(TABLE_TAXES));
    }

    /**
     * 
     * @param id 
     */
    public Steuersatz(Integer id) {
        super(ConnectionHandler.instanceOf().clone(TABLE_TAXES));
        this.id = id;
        readonly = !lock(); 
        try {
            this.explode(this.selectLast("name, wert", "id", id.toString(), true));
        } catch (Exception ex) {
             Log.Debug(this,ex);
        }
    }

    public String[][] getAll() {
    
        return this.select("id, name, wert", null, null, false);
    
    }

    private void explode(String[] select) {
        try {
            this.setName(select[0]);
            this.setWert(Double.valueOf(select[1]));

        } catch (Exception ex) {
            Log.Debug(this,ex);
        }
    }

    private String collect() {
        String str = PrepareData.prepareString(this.getName());
        str = str + PrepareData.prepareNumber(this.getWert());
        return PrepareData.finalize(str);
    }

    public boolean save() {

        if (!readonly && getId() > 0) {
            this.update(TABLE_TAXES_FIELDS, this.collect(), getId());
            isSaved = true;
            return true;
        } else if (getId() == 0) {
            this.id = this.insert(TABLE_TAXES_FIELDS, this.collect(),null);
            lock();
            return true;
        }
        return false;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public Double getWert() {
        return Wert;
    }

    public void setWert(Double Wert) {
        this.Wert = Wert;
    }
}
