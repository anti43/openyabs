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

/**
 *
 * @author anti43
 */
public class Produktliste implements TableData, Tabellen {

    private Query queryhandler = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(TABLE_PRODUCTS);
    private String[] where = null;
    private boolean forimport = false;
    //* <produktnummer>; <name>;<text>;<vk>;<ek>;<tax>;<warengruppenkategorie>;<warengruppenfamilie>;<warengruppe>;<url>;<ean>
    public static String[] header = new String[]{"produktnummer", "name", "text", "vk",
                    "ek", "tax", "hersteller","lieferant","warengruppenkategorie", "warengruppenfamilie",
                    "warengruppe", "url", "ean"
                };
   public static String[] header_reimport = new String[]{"produktnummer", "name", "text", "vk",
                    "ek", "tax", "herstellerid","warengruppenkategorie", "warengruppenfamilie",
                    "warengruppe", "url", "ean", "lieferantid"
                };

    /**
     * @param clazz The product type
     */
    public Produktliste(Class clazz) {
        if (clazz.isInstance(new mp4.items.Product())) {
            queryhandler = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(TABLE_PRODUCTS);
        }   
    }

    /**
     * 
     * @param clazz The contact type
     * @param produkt_id 
     */
    public Produktliste(Class clazz, Integer produkt_id) {
        if (clazz.isInstance(new mp4.items.Product())) {
            queryhandler = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(TABLE_PRODUCTS);
        } 
//        else if (clazz.isInstance(new mp4.items.Dienstleistung())) {
//            queryhandler = mp4.datenbank.verbindung.ConnectionHandler.instanceOf().clone(TABLE_SERVICES);
//        } 
        where = new String[]{"id", produkt_id.toString(), ""};
    }
//* <produktnummer>; <name>;<text>;<vk>;<ek>;<tax>;<warengruppenkategorie>;<warengruppenfamilie>;<warengruppe>;<url>;<ean>
// warengruppenkategorien, kategorienummer,name
// warengruppenfamilien, familienummer, kategorieid, name
// warengruppengruppen, gruppenummer,familienid, name

    @Override
    public Object[][] getData() {
        String[][] data = null;
        if (forimport) {
            data = queryhandler.selectFreeQuery("SELECT produkte.produktnummer AS Nummer,produkte.name,produkte.text," +
                    "produkte.vk,produkte.ek,steuersaetze.wert,produkte.herstellerid," +
                    "warengruppenkategorien.name, warengruppenfamilien.name, warengruppengruppen.name, produkte.url, produkte.ean, produkte.lieferantenid FROM produkte " +
                    "LEFT OUTER JOIN  steuersaetze ON produkte.steuersatzid = steuersaetze.id " +
                    "LEFT OUTER JOIN  warengruppengruppen ON produkte.warengruppenid = warengruppengruppen.id " +
                    "LEFT OUTER JOIN  warengruppenfamilien ON  warengruppengruppen.familienid = warengruppenfamilien.id " +
                    "LEFT OUTER JOIN  warengruppenkategorien ON warengruppenfamilien.kategorieid = warengruppenkategorien.id ", null);
        } else {
        data = queryhandler.selectFreeQuery("SELECT produkte.produktnummer AS Nummer,produkte.name,produkte.text," +
                    "produkte.vk,produkte.ek,steuersaetze.wert,hersteller.firma AS Hersteller,lieferanten.firma AS Lieferant," +
                    "warengruppenkategorien.name, warengruppenfamilien.name, warengruppengruppen.name, produkte.url,produkte.ean FROM produkte " +
                    "LEFT OUTER JOIN  lieferanten ON produkte.lieferantenid = lieferanten.id " +
                    "LEFT OUTER JOIN  steuersaetze ON produkte.steuersatzid = steuersaetze.id " +
                    "LEFT OUTER JOIN  hersteller ON produkte.herstellerid = hersteller.id " +
                    "LEFT OUTER JOIN  warengruppengruppen ON produkte.warengruppenid = warengruppengruppen.id " +
                    "LEFT OUTER JOIN  warengruppenfamilien ON  warengruppengruppen.familienid = warengruppenfamilien.id " +
                    "LEFT OUTER JOIN  warengruppenkategorien ON warengruppenfamilien.kategorieid = warengruppenkategorien.id ", null);
        }
        if (data != null) {
            return data;
        } else {
            return new Object[][]{{null},{null}};
        }
    }

    @Override
    public String getTitle() {
        return "Produkte " + DateConverter.getTodayDefDate();
    }

    public String[] getHeader() {
        return header;
    }

    public void setForimport(boolean forimport) {
        this.forimport = forimport;
    }
}
