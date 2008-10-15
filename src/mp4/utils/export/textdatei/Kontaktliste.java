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
package mp4.utils.export.textdatei;

import mp4.datenbank.installation.Tabellen;
import mp4.datenbank.verbindung.Query;
import mp4.interfaces.TableData;
import mp4.utils.datum.DateConverter;
import mp4.utils.listen.ArrayUtils;


/**
 *
 * @author anti43
 */
public class Kontaktliste implements TableData, Tabellen{
    private Query queryhandler;
    private String[] where = null;

    /**
     * 
     * @param clazz The contact type
     */
    public Kontaktliste(Class clazz) {
    
        if (clazz.isInstance(new mp4.items.Customer())) {
           queryhandler = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(TABLE_CUSTOMERS);
        } else if (clazz.isInstance(new mp4.items.Lieferant())) {
           queryhandler = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(TABLE_SUPPLIER);
        } else if (clazz.isInstance(new mp4.items.Hersteller())) {
           queryhandler = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(TABLE_MANUFACTURER); 
        } 
    }

    /**
     * 
     * @param clazz The contact type
     * @param kontakt_id The id of the contact
     */
    public Kontaktliste(Class clazz, Integer kontakt_id) {
        if (clazz.isInstance(new mp4.items.Customer())) {
           queryhandler = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(TABLE_CUSTOMERS);
        } else if (clazz.isInstance(new mp4.items.Lieferant())) {
           queryhandler = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(TABLE_SUPPLIER);
        } else if (clazz.isInstance(new mp4.items.Hersteller())) {
           queryhandler = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(TABLE_MANUFACTURER); 
        } 
        
        where = new String[]{"id", kontakt_id.toString(), ""};
    }
																																																																																																																																																																																																																																											
    @Override
    public Object[][] getData() {
        return ArrayUtils.merge(new String[][]{{"Name","E-mail Address","Notes","Mobile Phone",
                "Company","Business Phone", "Business Fax","Business Address"}
        }, queryhandler.select("vorname||' '||name,mail,notizen,mobil,firma,tel,fax,str||' '||plz||' '||ort", where));
    }

    @Override
    public String getTitle() {
        return "Kontakte " + DateConverter.getTodayDefDate();
    }
}
