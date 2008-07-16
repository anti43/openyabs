/*
 * 
 *  *  This file is part of MP by anti43 /GPL.
 *  *  
 *  *      MP is free software: you can redistribute it and/or modify
 *  *      it under the terms of the GNU General Public License as published by
 *  *      the Free Software Foundation, either version 3 of the License, or
 *  *      (at your option) any later version.
 *  *  
 *  *      MP is distributed in the hope that it will be useful,
 *  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 * *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *      GNU General Public License for more details.
 *  *  
 *  *      You should have received a copy of the GNU General Public License
 *  *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package mp3.classes.interfaces;

/**
 *
 * @author anti43
 */
public interface Strings {
    public String NO = "Nein";
    public String YES = "Ja";
    public String NO_PDF = "Bitte geben Sie unter \nBearbeiten-> Einstellungen ein PDF-Template an.\n";
   
    public String TEST_CONF = "Teste Konfiguration";
   
    public String SHUTDOWN = "Programm wird beendet";
    public String COLON = ";";
    public String SETTINGS_NOT_FOUND = "'settings.mp' nicht gefunden. Programm beendet.\n";
    public String DB_INIT = "Initialisiere Datenbank...";
    public String ONE_INSTANCE = "Beenden Sie zuerst alle anderen Instanzen von MP Version 3 !";
    public String TABLE_CUSTOMER_PRINT_HEADER =
            "K-Nr" + "," + "Firma" + "," + "Anrede" + "," + "Vorname" +
            "," + "Name" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" + "," + "Fax" + "," + "Mobil" + "," + "Mail" + "," + "Webseite";
    public String TABLE_SUPPLIER_PRINT_HEADER =
            "L-Nr" + "," + "Firma" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" + "," + "Fax" + "," + "Mobil" + "," + "Mail" + "," + "Webseite";
    public String TABLE_PRODUCTS_LIST_PRINT_HEADER =
            "P-Nr" + "," + "Name" + "," + "Text" + "," + "VK" + "," + "EK" +
            "," + "Tax" + "," + "Hersteller" + "," + "Lieferant" + "," + "Warengruppennr" + "," + 
            "Datum" + "," + "EAN";
}
