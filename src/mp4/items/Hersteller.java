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
import mp4.interfaces.Countable;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.datenbank.verbindung.PrepareData;
import mp4.items.handler.NumberFormatHandler;
import mp4.logs.Log;
/**
 *
 * @author anti
 */
public class Hersteller extends mp4.items.People implements mp4.datenbank.installation.Tabellen, Countable {
    private NumberFormatHandler nfh;

    public Hersteller() {
        super(ConnectionHandler.instanceOf().clone(TABLE_MANUFACTURER));
        nfh = new NumberFormatHandler(this, new Date());
    }

    public Hersteller(Integer id) {
        super(ConnectionHandler.instanceOf().clone(TABLE_MANUFACTURER));
        this.id = id;
        readonly = !lock();
        try {
            this.explode(this.selectLast("*", "id", id.toString(), true));
        } catch (Exception ex) {
            Log.Debug(this,ex);
        }
        nfh = new NumberFormatHandler(this, new Date());
    }
    public String getid() {
        return id.toString();
    }
    public String getHerstellernummer() {
        return super.getNummer();
    }

    public void setHerstellernummer(String Herstellernummer) {
        super.setNummer(Herstellernummer);
    }

    @Override
    public String collect() {
         return  PrepareData.finalize(super.collect());
    }

    @Override
    public void explode(String[] str) {
          super.explode(str);
    }


    @Override
    public boolean save() throws Exception {
        
            if (id > 0) {
                if (!readonly) {
                this.update(TABLE_MANUFACTURER_FIELDS, this.collect(), id);
                isSaved = true;
                return true;
                } 
            } else if (id == 0) {
                this.id = this.insert(TABLE_MANUFACTURER_FIELDS, this.collect());
                lock();
                return true;
            }
        return false;
    }
    
    
    public boolean isValid() {
        if(this.id > 0) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public Date getDatum() {
        return new Date();
    }

    @Override
    public String getTable() {
        return TABLE_MANUFACTURER;
    }

    @Override
    public String getCountColumn() {
       return "nummer";
    }

    public NumberFormatHandler getNfh() {
        return nfh;
    }


}
