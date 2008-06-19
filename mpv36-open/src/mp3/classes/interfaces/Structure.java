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

package mp3.classes.interfaces;

import java.io.File;

/**
 *
 * @author anti
 */
public interface Structure {
    
    



    
    /**
     * Eigene Daten
     */
    public final String TABLE_MYDATA = "daten";
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
    public final String TABLE_ORDERS = "auftraege";
    /**
     * Auftragsposten
     */
    public final String TABLE_ORDERS_DATA = "auftragsposten";
    /**
     * Files (pictures) of products
     */
    public final String TABLE_PRODUCTS_FILES = "dateien";
    /**
     * History
     */
    public final String TABLE_HISTORY = "historie";
    
     /**
     * User
     */
    public final String TABLE_USER = "usertable";
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
   
  
    public final String TABLE_CUSTOMER_PRINT_FIELDS =
            "Kundennummer" + "," + "Firma" + "," + "Anrede" + "," + "Vorname" +
            "," + "Name" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" +"," + "Fax" + "," + "Mobil" + "," + "Mail" + "," + "Webseite";
     
     

     /**
     * Fields in customer table
     */
    public final String TABLE_CUSTOMER_FIELDS =
            "Kundennummer" + "," + "Firma" + "," + "Anrede" + "," + "Vorname" +
            "," + "Name" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" +"," + "Fax" + "," + "Mobil" + "," + "Mail" + "," + "Webseite" + "," + "Notizen";
    /**
     * Fields in supplier table
     */
    public final String TABLE_SUPPLIER_FIELDS =
            "Lieferantennummer" + "," + "Firma" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" +"," + "Fax" +  "," + "Mobil" + "," + "Mail" + "," + "Webseite" + "," + "Notizen";
      /**
     * Fields in supplier table
     */
    public final String TABLE_SUPPLIER_PRINT_FIELDS =
            "Lieferantennummer" + "," + "Firma" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" +"," + "Fax" +  "," + "Mobil" + "," + "Mail" + "," + "Webseite";
 
    
     /**
     * Fields in manufacturer table
     */
    public final String TABLE_MANUFACTURER_FIELDS =
            "Herstellernummer" + "," + "Firma" + "," + "Anrede" + "," + "Vorname" +
            "," + "Name" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" +"," + "Fax" +  "," + "Mobil" + "," + "Mail" + "," + "Webseite" + "," + "Notizen";
    
     /**
     * Fields in bills data table
     */
     public final String TABLE_BILLS_DATA_FIELDS =
             "Rechnungid" + "," + "Anzahl" + ","+
             
             "Posten" + "," +"Preis" + "," +
             "Steuersatz";
     /**
     * Fields in bills table
     */
     public final String TABLE_BILLS_FIELDS =
              "Rechnungnummer" + "," +"KundenId" + "," +
              "Datum" + "," + "Storno" + "," + "bezahlt"+ "," + "gesamtpreis"+ "," + "gesamttax";
        
          /**
     * Fields in orders data table
     */
     public final String TABLE_ORDERS_DATA_FIELDS =
             "Auftragid" + "," + "Anzahl" + ","+
             
             "Posten" + "," +"Preis" + "," +
             "Steuersatz";
     /**
     * Fields in orders table
     */
     public final String TABLE_ORDERS_FIELDS =
              "Auftragnummer" + "," +"KundenId" + "," +
              "Datum"+ "," +"auftrag"+ "," +"bis"+ "," +"rechnung";
     
      /**
     * Fields in mydata table
     */
     public final String TABLE_MYDATA_FIELDS =
              "name" + "," +"wert" ;
     /**
     * Fields in products table
     */
     public final String TABLE_PRODUCTS_FIELDS =
              "Produktnummer" + "," +"Name" + "," + "Text" + "," +"VK" + "," +"EK" +
              "," +"Tax"+ ","+ "hersteller"+ ","+"lieferantenid"+ ","+"warengruppenid"
              
              + ","+"Datum"+ ","+"Url" + "," + "EAN";
       
//     /**
//     * Fields in products groups table
//     */
//     public final String TABLE_PRODUCTS_GROUPS_FIELDS =
//               "Warengruppennummer" + "," +"Name";
     
    /**
     * Fields in products groups categories table
     */
     public final String TABLE_PRODUCTS_GROUPS_CATEGORIES_FIELDS =
               "kategorienummer"+","+"name";
         /**
     * Fields in products groups family table
     */
     public final String TABLE_PRODUCTS_GROUPS_FAMILY_FIELDS =
               "familienummer"+","+"kategorieid"+","+"name";
         /**
     * Fields in products groups group table
     */
     public final String TABLE_PRODUCTS_GROUPS_GROUP_FIELDS =
               "gruppenummer"+","+"familienid"+","+"name";
     
     /**
     * Fields in products files table
     */
     public final String TABLE_PRODUCTS_FILES_FIELDS =
               "productid" + "," +"url"+ ","+ "datum";
     
    /**
     * Fields in history table
     */
     public final String TABLE_HISTORY_FIELDS =
               "aktion" + "," +"text"+ ","+ "datum";
     
     /**
     * User table
     */
     public final String TABLE_USER_FIELDS=
               "username" + "," +"password";
     
     /**
     * Columns for productslist table
     */
     public final String TABLE_PRODUCTS_LIST_COLUMNS =
              "Produktnummer" + "," +"Name" + "," + "Text" + "," +"VK" + "," +"EK" +
              "," +"Tax"+ ","+ "Herstellernummer"+ ","+"Lieferantennummer"+ ","+"Warengruppennummer"
              
              + ","+"Datum"+ ","+"Url" + "," + "EAN";
     
  /**
     * Kontenrahmen
     */
    public final String TABLE_KONTEN_FIELDS = "nummer" + "," +"klasse" + "," +"gruppe" + "," +"art";
    
    
    
     /**
     * Ausgaben
     */
    public final String TABLE_OUTGOINGS_FIELDS = "kontenid" + "," +"beschreibung" + "," +
            "preis" + "," +"tax" + "," +"datum";
    
    /**
     * Einnahmen
     */
    public final String TABLE_INCOME_FIELDS  = "kontenid" + "," +"beschreibung" + "," +
            "preis" + "," +"tax" + "," +"datum";
     
     /**
      * 
      * String utilitis
      */
     public final String ALL = "*";
     public final String NULL = "";
     public final String ID = "id";
     public final String NOTNULL = "NOT null";
     
     public final String CUSTOMER = "Kunde";

     
     public final String SUPPLIER = "Lieferant";

     
     public final String PRODUCT = "Produkt";
  
     
     public final String ORDER = "Angebot";

     
     public final String BILL = "Rechnung";
     
     public final String BACKUP = "Datensicherung";
     
     public final String KONTEN = "Konten";

    //////////////////////////////////////////////////////////////////////////77
    /**
     * Installation data (tables)
     */
    public final String[] tables = {
        
        "CREATE TABLE kunden (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "Kundennummer varchar(250),Firma varchar(250) default NULL,Anrede varchar(250) default NULL," +
                "Vorname varchar(250) default NULL, " +
                "Name varchar(250) default NULL, Str varchar(250) default NULL, PLZ varchar(50) default NULL," +
                "Ort varchar(300) default NULL, Tel varchar(250) default NULL,Fax varchar(250) default NULL," +
                "Mobil varchar(250) default NULL," +
                "Mail varchar(350) default NULL, Webseite varchar(350) default NULL,Notizen varchar(10000)," +
                "deleted INTEGER DEFAULT 0, "+
               
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL," +
                "PRIMARY KEY  (id))",
                
        "CREATE TABLE lieferanten (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "lieferantennummer varchar(250),Firma varchar(250) default NULL," +
                "Str varchar(250) default NULL, PLZ varchar(50) default NULL," +
                "Ort varchar(300) default NULL, Tel varchar(250) default NULL,Fax varchar(250) default NULL," +
                "Mobil varchar(250) default NULL," +
                "Mail varchar(350) default NULL, Webseite varchar(350) default NULL,Notizen varchar(10000)," +
                "deleted INTEGER DEFAULT 0, "+
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL," +
                "PRIMARY KEY  (id))",
                
         "CREATE TABLE hersteller (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "herstellernummer varchar(250),Firma varchar(250) default NULL,Anrede varchar(250) default NULL," +
                "Vorname varchar(250) default NULL, " +
                "Name varchar(250) default NULL, Str varchar(250) default NULL, PLZ varchar(50) default NULL," +
                "Ort varchar(300) default NULL, Tel varchar(250) default NULL,Fax varchar(250) default NULL," +
                "Mobil varchar(250) default NULL," +
                "Mail varchar(350) default NULL, Webseite varchar(350) default NULL,Notizen varchar(10000)," +
                "deleted INTEGER DEFAULT 0, "+
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL," +
                "PRIMARY KEY  (id))",
        
        "CREATE TABLE rechnungsposten (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "rechnungid INTEGER NOT NULL," + "anzahl varchar(100) default NULL," +
                "posten varchar(1000) default NULL,  preis varchar(250) default NULL, " +
                "steuersatz varchar(100) default NULL," +
                "deleted INTEGER DEFAULT 0,"+
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL," +
                "PRIMARY KEY  (id))",
                
        "CREATE TABLE rechnungen (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "Rechnungnummer varchar(250) default NULL,KundenId INTEGER NOT NULL," +
                "Datum varchar(200) default NULL," + "storno INTEGER DEFAULT 0," + "bezahlt INTEGER DEFAULT 0," +
                "gesamtpreis varchar(20),"+ "gesamttax varchar(20),"+ 
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL," +
                "PRIMARY KEY  (id))",
                
       "CREATE TABLE auftragsposten (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "auftragid INTEGER NOT NULL," + "anzahl varchar(100) default NULL," +
                "posten varchar(1000) default NULL,  preis varchar(250) default NULL, " +
                "steuersatz varchar(100) default NULL," +
                "deleted INTEGER DEFAULT 0,"+
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL," +
                "PRIMARY KEY  (id))",
                
        "CREATE TABLE auftraege (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "Auftragnummer varchar(250) default NULL,KundenId INTEGER NOT NULL," +
                "Datum varchar(200) default NULL," +"auftrag INTEGER DEFAULT 0,"+"bis varchar(200) default NULL," +
                "rechnung  INTEGER DEFAULT 0," +"deleted INTEGER DEFAULT 0,"+
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL," +
                "PRIMARY KEY  (id))",
        
        "CREATE TABLE daten (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "name varchar(250), wert varchar(250)," +
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL,PRIMARY KEY  (id))",
            
        "CREATE TABLE produkte (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "produktnummer varchar(500),name varchar(50),text varchar(500),vk varchar(250),ek varchar(250),tax varchar(250)," +
                "hersteller varchar(250),lieferantenid INTEGER NOT NULL,"+
                "warengruppenid INTEGER NOT NULL,"+"datum varchar(250) default NULL,"+"url varchar(250) default NULL,"+
                "ean BIGINT NOT NULL,"+
                "deleted INTEGER DEFAULT 0,"+
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL,PRIMARY KEY  (id))",  
 
         "CREATE TABLE warengruppenkategorien (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "kategorienummer varchar(120),name varchar(500),"+ 
                 "deleted INTEGER DEFAULT 0,"+
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL,PRIMARY KEY  (id))",
                
          "CREATE TABLE usertable (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "username varchar(250),password varchar(250),"+ 
                "deleted INTEGER DEFAULT 0,"+
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL,PRIMARY KEY  (id))",
                
         "CREATE TABLE warengruppenfamilien (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "familienummer varchar(120), kategorieid  INTEGER DEFAULT 0, name varchar(500),"+ 
                 "deleted INTEGER DEFAULT 0,"+
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL,PRIMARY KEY  (id))",
          
         "CREATE TABLE warengruppengruppen (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "gruppenummer varchar(120),familienid  INTEGER DEFAULT 0,name varchar(500),"+ 
                 "deleted INTEGER DEFAULT 0,"+
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL,PRIMARY KEY  (id))",

                
         "CREATE TABLE dateien (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "productid INTEGER NOT NULL,url varchar(500),"+
                "datum varchar(200) default NULL,"+
                "deleted INTEGER DEFAULT 0,"+
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL,PRIMARY KEY  (id))",
               
         "CREATE TABLE historie (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "aktion varchar(500) default NULL,text varchar(250) default NULL,"+
                "datum varchar(500) default NULL,"+
                "deleted INTEGER DEFAULT 0,"+
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL,PRIMARY KEY  (id))",

         "CREATE TABLE konten (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "nummer varchar(500) default NULL,klasse varchar(1000) default NULL,"+
                "gruppe varchar(1000) default NULL,"+
                "art varchar(1000) default NULL,"+
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL,PRIMARY KEY  (id))",
                   
         "CREATE TABLE ausgaben (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "kontenid INTEGER DEFAULT NULL, beschreibung varchar(500) default NULL,"+
                "preis varchar(500) default NULL,"+"tax varchar(500) default NULL,"+"datum varchar(500) default NULL,"+
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL,PRIMARY KEY  (id))",
                   
         "CREATE TABLE einnahmen (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "kontenid INTEGER DEFAULT NULL, beschreibung varchar(500) default NULL,"+
                "preis varchar(500) default NULL,"+"tax varchar(500) default NULL,"+"datum varchar(500) default NULL,"+
                "reserve1 varchar(500) default NULL,reserve2 varchar(500) default NULL,PRIMARY KEY  (id))",

    
            
            
          "INSERT INTO daten (name, wert) VALUES ('Backup Verzeichnis', '" + System.getProperty("user.home") + File.separator + ProtectedStrings.PROG_NAME + File.separator + "backups"+"')",
          
                  "INSERT INTO daten (name, wert) VALUES ('PDF Rechnungen Verzeichnis', '" + System.getProperty("user.home") + File.separator + ProtectedStrings.PROG_NAME  + File.separator + "PDF" + File.separator+ "Rechnungen"+"')",
          
                  "INSERT INTO daten (name, wert) VALUES ('PDF Angebote Verzeichnis', '" + System.getProperty("user.home") + File.separator + ProtectedStrings.PROG_NAME  + File.separator + "PDF" + File.separator+"Angebote"+"')",
          
                  "INSERT INTO daten (name, wert) VALUES ('PDF Mahnung Verzeichnis', '" + System.getProperty("user.home") + File.separator + ProtectedStrings.PROG_NAME  + File.separator + "PDF" + File.separator+"Mahnungen"+"')",
          
                  "INSERT INTO daten (name, wert) VALUES ('Rechnung Template', '" + System.getProperty("user.home") + File.separator + ProtectedStrings.PROG_NAME + File.separator + "templates"+ File.separator + "rechnung-mwst.pdf')",
          
                  "INSERT INTO daten (name, wert) VALUES ('Angebot Template', '" + System.getProperty("user.home") + File.separator + ProtectedStrings.PROG_NAME  + File.separator + "templates"+ File.separator + "angebot.pdf')",
          
                  "INSERT INTO daten (name, wert) VALUES ('Mahnung Template', '" + System.getProperty("user.home") + File.separator + ProtectedStrings.PROG_NAME  + File.separator + "templates"+ File.separator + "mahnung.pdf')",
          
                  "INSERT INTO daten (name, wert) VALUES ('Serienbrief Template', '" + System.getProperty("user.home") + File.separator + ProtectedStrings.PROG_NAME  + File.separator + "templates"+ File.separator + "serienbrief.pdf')",
          
                  "INSERT INTO daten (name, wert) VALUES ('Mehrwertsteuersatz', '19')",
                  
                  "INSERT INTO daten (name, wert) VALUES ('PDF Programm', '/opt/kde3/bin/kpdf')",
                  
                  "INSERT INTO daten (name, wert) VALUES ('Internetbrowser', '/usr/bin/firefox')",
                  
                  "INSERT INTO daten (name, wert) VALUES ('*Starttab', '8')",
                  
                  "INSERT INTO daten (name, wert) VALUES ('Default Konto Einnahme (auf dieses Konto werden auch Ihre Rechnungen verbucht!)', '2112')",
                   
                  "INSERT INTO daten (name, wert) VALUES ('Default Konto Ausgabe', '1111')",
                  
                  "INSERT INTO daten (name, wert) VALUES ('Land (Waehrung) (DE,CH)', 'DE')",
                  
                  "INSERT INTO daten (name, wert) VALUES ('*Hauptfenster', ' ')",


"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1110', 'Ausgabenkonten', 'Waren, Rohstoffe und Hilfsstoffe einschl. der Nebenkosten', 'Waren, Rohstoffe und Hilfsstoffe')",
"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1111', 'Ausgabenkonten', 'Waren, Rohstoffe und Hilfsstoffe einschl. der Nebenkosten', 'Allgemeines Ausgabenkonto')",
"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1112', 'Ausgabenkonten', 'Waren, Rohstoffe und Hilfsstoffe einschl. der Nebenkosten', 'Verbrauchsmaterial')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1120', 'Ausgabenkonten', 'Bezogene Leistungen (z.B. Fremdleistungen)', 'Bezogene Leistungen für unmittelbaren Betriebszweck')",
"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1121', 'Ausgabenkonten', 'Bezogene Leistungen (z.B. Fremdleistungen)', 'Telekommunikation')",
"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1122', 'Ausgabenkonten', 'Bezogene Leistungen (z.B. Fremdleistungen)', 'Energie')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1130', 'Ausgabenkonten', 'Ausgaben für eigenes Personal ', 'Gehälter, Löhne, Versicherungsbeiträge für Arbeitnehmer')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1140', 'Ausgabenkonten', 'Aufwendungen für geringwertige Wirtschaftsgüter', 'Aufwendungen für geringwertige Wirtschaftsgüter (GWG)')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1150', 'Ausgabenkonten', 'Miete / Pacht für Geschäftsräume und betrieblich genutzte Grundstücke', 'Miete / Pacht für Geschäftsräume')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1160', 'Ausgabenkonten', 'Sonstige Aufwendungen für betrieblich genutzte Grundstücke', 'Aufwendungen für Grundstücke')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1170', 'Ausgabenkonten', 'Abziehbare Aufwendungen für ein häusliches Arbeitszimmer', 'Aufwendungen für ein häusl. Arbeitszimmer')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1180', 'Ausgabenkonten', 'Reisekosten,Aufwendungen für doppelte Haushaltsführung', 'Reisekosten')",
"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1181', 'Ausgabenkonten', 'Reisekosten,Aufwendungen für doppelte Haushaltsführung', 'Fahrtkosten öffentlich (Bahn, ÖPNV, Taxi)')",
"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1182', 'Ausgabenkonten', 'Reisekosten,Aufwendungen für doppelte Haushaltsführung', 'Hotelkosten')",
"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1183', 'Ausgabenkonten', 'Reisekosten,Aufwendungen für doppelte Haushaltsführung', 'Privat-PKW für betriebliche Fahrten')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1190', 'Ausgabenkonten', 'Geschenke – abziehbar', 'Geschenke – abziehbar')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1200', 'Ausgabenkonten', 'Geschenke – nicht abziehbar', 'Geschenke – nicht abziehbar')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1210', 'Ausgabenkonten', 'Bewirtung – abziehbar', 'Bewirtung – abziehbar')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1220', 'Ausgabenkonten', 'Bewirtung – nicht abziehbar', 'Bewirtung – nicht abziehbar')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1230', 'Ausgabenkonten', 'Übrige Betriebsausgaben', 'Sonstige abziehbare Betriebsausgaben')",
"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1231', 'Ausgabenkonten', 'Übrige Betriebsausgaben', 'Sonstige nicht abziehbare Betriebsausgaben')",
"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1232', 'Ausgabenkonten', 'Übrige Betriebsausgaben', 'Instandhaltung und Reparaturen')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1240', 'Ausgabenkonten', 'Fortbildung und Fachliteratur', 'Fortbildung, Fachliteratur')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1250', 'Ausgabenkonten', 'Rechts- und Steuerberatung, Buchführung', 'Rechts- und Steuerberatung, Buchführung')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1260', 'Ausgabenkonten', 'Porto, Telefon, Büromaterial', 'Bürobedarf, Porto')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('1270', 'Ausgabenkonten', 'An das Finanzamt gezahlte und ggf. verrechnete Umsatzsteuer', 'An das FA gezahlte Umsatzsteuer')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('2100', 'Einnahmenkonten', 'Betriebseinnahmen als umsatzsteuerlicher Kleinunternehmer', 'Allgemeines Einnahmenkonto')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('2110', 'Einnahmenkonten', 'Umsatzsteuerpflichtige Betriebseinnahmen', 'Erlöse 0% Umsatzsteuer')",
"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('2111', 'Einnahmenkonten', 'Umsatzsteuerpflichtige Betriebseinnahmen', 'Erlöse 7% Umsatzsteuer')",
"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('2112', 'Einnahmenkonten', 'Umsatzsteuerpflichtige Betriebseinnahmen', 'Erlöse 19% Umsatzsteuer')",
"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('2113', 'Einnahmenkonten', 'Umsatzsteuerpflichtige Betriebseinnahmen', 'Sonstige Erträge')",
"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('2114', 'Einnahmenkonten', 'Umsatzsteuerpflichtige Betriebseinnahmen', 'Sachbezüge (Waren)')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('2120', 'Einnahmenkonten', 'Sonstige Sach-, Nutzungs- und Leistungsentnahmen', 'Sachentnahmen')",
"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('2121', 'Einnahmenkonten', 'Sonstige Sach-, Nutzungs- und Leistungsentnahmen', 'Nutzungs- und Leistungsentnahmen')",
"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('2122', 'Einnahmenkonten', 'Sonstige Sach-, Nutzungs- und Leistungsentnahmen', 'Private Telefonnutzung')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('2130', 'Einnahmenkonten', 'Private Kfz-Nutzung', 'Private KFZ-Nutzung')",

"INSERT INTO konten (nummer, klasse,gruppe,art) VALUES ('2140', 'Einnahmenkonten', 'Vom Finanzamt erstattete und ggf. verrechnete Umsatzsteuer', 'Vom FA erstattete Umsatzsteuer')",

                  
          "INSERT INTO warengruppenkategorien (kategorienummer,name) VALUES ('1','Default')",
          
          "INSERT INTO warengruppenfamilien (familienummer, kategorieid, name) VALUES ('1',1,'Default')",
          
          "INSERT INTO warengruppengruppen (gruppenummer, familienid, name) VALUES ('1',1,'Default')"
            
 };
}
