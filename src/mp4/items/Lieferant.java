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
import mp4.globals.Strings;
import mp4.datenbank.verbindung.Query;
import mp4.items.handler.NumberFormatHandler;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.datenbank.verbindung.PrepareData;
import mp4.logs.Log;

/**
 *
 * @author anti
 */
public class Lieferant extends mp4.items.People implements mp4.datenbank.installation.Tabellen, Countable {


    private Query query;
    private NumberFormatHandler nfh;

    public Lieferant() {
        super(ConnectionHandler.instanceOf().clone(TABLE_SUPPLIER));
        this.id = 0;
        this.query =ConnectionHandler.instanceOf();
        nfh = new NumberFormatHandler(this, new Date());
    }



    public Lieferant(Query query) {
        super(query.clone(TABLE_SUPPLIER));
        this.id = 0;
        this.query =query;
        nfh = new NumberFormatHandler(this, new Date());
    }

    public Lieferant(Integer id) {
        super(ConnectionHandler.instanceOf().clone(TABLE_SUPPLIER));
        this.query =ConnectionHandler.instanceOf();
        this.id = id;
        readonly = !lock();
        try {
            this.explode(this.selectLast("*", "id", id.toString(), true));
        } catch (Exception ex) {
            Log.Debug(this,ex);
        }
        nfh = new NumberFormatHandler(this, new Date());
    }


    public String[][] getProducts(){

        Query q = query.clone(TABLE_PRODUCTS);
        String[][] str = q.select("produkte.id, produkte.name, produkte.hersteller", null, TABLE_SUPPLIER, "lieferantenid");
        return str;  
    }
    
    public String[][] getAll(){
        Query q = query.clone(TABLE_SUPPLIER);
        String[][] str = q.select(Strings.ALL, null);
        return str;  
    }

    @Override
    public String collect() {
         return  PrepareData.finalize(super.collect());
    }

    @Override
    public void explode(String[] str) {
          super.explode(str);
    }

     public boolean save() throws Exception {
        
            if (id > 0) {
                if (!readonly) {
                this.update(TABLE_SUPPLIER_FIELDS, this.collect(), id);
                isSaved = true;
                return true;
                } 
            } else if (id == 0) {
                this.id = this.insert(TABLE_SUPPLIER_FIELDS , this.collect());
                lock();
                return true;
            }
        return false;
    }
    
     public String[][] getPrintModel(){
    
        Query q = query.clone(TABLE_SUPPLIER);

        String[][] str = q.select(TABLE_SUPPLIER_PRINT_FIELDS, null);
   
        return str;  
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
        return TABLE_SUPPLIER;
    }

    @Override
    public String getCountColumn() {
       return "nummer";
    }

    public NumberFormatHandler getNfh() {
        return nfh;
    }

}
