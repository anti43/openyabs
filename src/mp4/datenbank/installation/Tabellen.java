/*
 * 
This file is part of MP by anti43 /GPL.

MP is free software: you can redistribute it and/or modify
it under the terms of the GNU General public  License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General public  License for more details.

You should have received a copy of the GNU General public  License
along with MP.  If not, see <http://www.gnu.org/licenses/>.

 * 
 */
package mp4.datenbank.installation;

/**
 *
 * @author anti43
 */
public interface Tabellen {

    /**
     * Eigene Daten
     */
    public final String TABLE_MYDATA = "daten";
    public final String TABLE_PROG_DATA = "programmdaten";
    /**
     * Kundendaten
     */
    public final String TABLE_CUSTOMERS = "kunden";
    /**
     * Kundendaten
     */
    public final String TABLE_MANUFACTURER = "hersteller";
    /**
     * Kundendaten
     */
    public final String TABLE_SUPPLIER = "lieferanten";
    /**
     * Produktdaten
     */
    public final String TABLE_PRODUCTS = "produkte";
    /**
     * Produktdaten
     */
    public final String TABLE_SERVICES = "dienstleistungen";
    /**
     * Rechnungsdaten
     */
    public final String TABLE_BILLS = "rechnungen";
    /**
     * Rechnungsposten
     */
    public final String TABLE_BILLS_DATA = "rechnungsposten";
    /**
     * Auftragsdaten
     */
    public final String TABLE_OFFERS = "angebote";
    /**
     * Auftragsposten
     */
    public final String TABLE_OFFERS_DATA = "angebotposten";
    /**
     * Files (pictures) of products
     */
    public final String TABLE_PRODUCTS_FILES = "dateien";
    /**
     * History
     */
    public final String TABLE_HISTORY = "historie";
    public final String DATABASE = "Datenbank";
    /**
     * User
     */
    public final String TABLE_USER = "benutzer";
    /**
     * Productgroups
     */
    public final String TABLE_PRODUCTS_GROUPS_CATEGORIES = "warengruppenkategorien";
    public final String TABLE_PRODUCTS_GROUPS_FAMILIES = "warengruppenfamilien";
    public final String TABLE_PRODUCTS_GROUPS_GROUPS = "warengruppengruppen";
    /**
     * Kontenrahmen
     */
    public final String TABLE_KONTEN = "konten";
    /**
     * Ausgaben
     */
    public final String TABLE_DUES = "ausgaben";
    /**
     * Einnahmen
     */
    public final String TABLE_INCOME = "einnahmen";
    /**
     * Plugins onload
     */
    public final String TABLE_PLUGINS = "plugins_to_load";
   /**
     * plugins_data
     */
    public final String TABLE_PLUGINS_DATA = "plugins_data";
    /**
     * Betreffzeilen
     */
    public final String TABLE_BILL_TEXTS = "rechnungbetreffz";
    public final String TABLE_BILL_TEXTS_TO_BILLS = "betreffz_zu_rechnung";
    public final String TABLE_TAXES = "steuersaetze";
    public final String TABLE_CUSTOMER_PRINT_FIELDS =
            "Kundennummer" + "," + "Firma" + "," + "Anrede" + "," + "Vorname" +
            "," + "Name" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" + "," + "Fax" + "," + "Mobil" + "," + "Mail" + "," + "Webseite";
    /**
     * Fields in customer table
     */
    public final String TABLE_CUSTOMER_FIELDS =
            "nummer" + "," + "Firma" + "," + "Anrede" + "," + "Vorname" +
            "," + "Name" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" + "," + "Fax" + "," + "Mobil" + "," + "Mail" + "," + 
            "Webseite" + "," + "Notizen"+ "," + "Steuernummer";
    /**
     * Fields in supplier table
     */
    public final String TABLE_SUPPLIER_FIELDS =
            "nummer" + "," + "Firma" + "," + "Anrede" + "," + "Vorname" +
            "," + "Name" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" + "," + "Fax" + "," + "Mobil" + "," + "Mail" + "," + "Webseite" + "," + "Notizen";
    /**
     * Fields in supplier table
     */
    public final String TABLE_SUPPLIER_PRINT_FIELDS =
            "nummer" + "," + "Firma" + "," + "Anrede" + "," + "Vorname" +
            "," + "Name" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" + "," + "Fax" + "," + "Mobil" + "," + "Mail" + "," + "Webseite";
    /**
     * Fields in manufacturer table
     */
    public final String TABLE_MANUFACTURER_FIELDS =
            "nummer" + "," + "Firma" + "," + "Anrede" + "," + "Vorname" +
            "," + "Name" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" + "," + "Fax" + "," + "Mobil" + "," + "Mail" + "," + "Webseite" + "," + "Notizen";
    /**
     * Fields in bills data table
     */
    public final String TABLE_BILLS_DATA_FIELDS =
            "Rechnungid" + "," + "Anzahl" + "," +
            "Posten" + "," + "Preis" + "," +
            "Steuersatz";
    /**
     * Fields in bills table
     */
    public final String TABLE_BILLS_FIELDS =
            "Rechnungnummer" + "," + "KundenId" + "," +
            "Datum" + "," + "Storno" + "," + "bezahlt" + "," + "gesamtpreis" + "," + "gesamttax" + "," +
            "AfDatum" + "," +
            "mahnungen";
    /**
     * Fields in orders data table
     */
    public final String TABLE_OFFERS_DATA_FIELDS =
            "Angebotid" + "," + "Anzahl" + "," +
            "Posten" + "," + "Preis" + "," +
            "Steuersatz";
    /**
     * Fields in orders table
     */
    public final String TABLE_OFFERS_FIELDS =
            "Angebotnummer" + "," + "KundenId" + "," +
            "Datum" + "," + "auftragdatum" + "," + "anfragevom" + "," + "validvon"  + "," +  "validbis"  + "," +  "rechnungid";
    /**
     * Fields in mydata table
     */
    public final String TABLE_MYDATA_FIELDS =
            "name" + "," + "wert";
    /**
     * Fields in products table
     */
    public final String TABLE_PRODUCTS_FIELDS =
            "Produktnummer" + "," + "Name" + "," + "Text" + "," + "VK" + "," + "EK" +
            "," + "steuersatzid" + "," + "herstellerid" + "," + "lieferantenid" + "," + 
            "warengruppenid" + "," + "Datum" + "," + "Url" + "," + "EAN" +
             "," + "bestellnr" + "," + "herstellernr" + "," + "lieferantennr" + "," + 
            "bestelldatum" + "," + "bestellmenge" + "," + "lagermenge";
     /**
     * Fields in products table
     */
    public final String TABLE_SERVICES_FIELDS =
        "produktnummer,name ,beschreibung,einheit," +
        "preis , steuersatzid ," +
        "warengruppenid," + "datum";
        
    /**
     * Fields in products groups categories table
     */
    public final String TABLE_PRODUCTS_GROUPS_CATEGORIES_FIELDS =
            "kategorienummer" + "," + "name";
    /**
     * Fields in products groups family table
     */
    public final String TABLE_PRODUCTS_GROUPS_FAMILY_FIELDS =
            "familienummer" + "," + "kategorieid" + "," + "name";
    /**
     * Fields in products groups group table
     */
    public final String TABLE_PRODUCTS_GROUPS_GROUP_FIELDS =
            "gruppenummer" + "," + "familienid" + "," + "name";
    /**
     * Fields in products files table
     */
    public final String TABLE_PRODUCTS_FILES_FIELDS =
            "productid" + "," + "url" + "," + "datum";
    /**
     * Fields in history table
     */
    public final String TABLE_HISTORY_FIELDS =
            "aktion" + "," + "text" + "," + "datum" + "," + "benutzer";
    /**
     * User table
     */
    public final String TABLE_USER_FIELDS =
                 "username" + "," + "password" + "," + "createdon" + "," + 
                 "createdby" + "," + "iseditor" + "," + "isadmin";
    /**
     * Columns for productslist table
     */
    public final String TABLE_PRODUCTS_LIST_COLUMNS =
            "Produktnummer" + "," + "Name" + "," + "Text" + "," + "VK" + "," + "EK" +
            "," + "Tax" + "," + "Hersteller" + "," + "Lieferant" + "," + "Warengruppennummer" + "," + "Datum" + "," + "Url" + "," + "EAN";
    /**
     * Kontenrahmen
     */
    public final String TABLE_KONTEN_FIELDS = "nummer" + "," + "klasse" + "," + "gruppe" + "," + "art";
    /**
     * Ausgaben
     */
    public final String TABLE_OUTGOINGS_FIELDS = "kontenid" + "," + "beschreibung" + "," +
            "preis" + "," + "tax" + "," + "datum";
    /**
     * Einnahmen
     */
    public final String TABLE_INCOME_FIELDS = "kontenid" + "," + "beschreibung" + "," +
            "preis" + "," + "tax" + "," + "datum";
    /**
     * Vorlagen
     */
    public final String TABLE_BILL_TEXTS_FIELDS = "name" + "," +
            "text" + "," +
            "isvorlage";
    /**
     * Vorlagen
     */
    public final String TABLE_BILL_TEXTS_TO_BILLS_FIELDS = "rechnungid" + "," +
            "betreffzid";
    /**
     * Steuersaetze
     */
    public final String TABLE_TAXES_FIELDS = "name" + "," +
            "wert";
}
