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
package mp4.datenbank.installation;

/**
 *
 * @author anti
 */
public abstract class Struktur {
//Moved to file
//    //////////////////////////////////////////////////////////////////////////77
//    /**
//     * Struktur data (tables)
//     */
//    public final static String[] SQL_COMMAND = {
//        "CREATE TABLE kunden (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
//        "nummer VARCHAR(250),Firma VARCHAR(250) default NULL," +
//        "Anrede VARCHAR(250) default NULL," +
//        "Vorname VARCHAR(250) default NULL, " +
//        "Name VARCHAR(250) default NULL, " +
//        "Str VARCHAR(250) default NULL, PLZ VARCHAR(50) default NULL," +
//        "Ort VARCHAR(300) default NULL, Tel VARCHAR(250) default NULL,Fax VARCHAR(250) default NULL," +
//        "Mobil VARCHAR(250) default NULL," +
//        "Mail VARCHAR(350) default NULL, Webseite VARCHAR(350) default NULL,Notizen VARCHAR(10000)," +
//        "Steuernummer VARCHAR(350) default NULL,"+ "Datum DATE DEFAULT CURRENT_DATE," +
//        "deleted INTEGER DEFAULT 0, " + 
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
//        "PRIMARY KEY  (id))",
//        
//        "CREATE TABLE lieferanten (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
//        "nummer VARCHAR(250),Firma VARCHAR(250) default NULL," +
//        "Anrede VARCHAR(250) default NULL," +
//        "Vorname VARCHAR(250) default NULL, " +
//        "Name VARCHAR(250) default NULL, " +
//        "Str VARCHAR(250) default NULL, PLZ VARCHAR(50) default NULL," +
//        "Ort VARCHAR(300) default NULL, Tel VARCHAR(250) default NULL,Fax VARCHAR(250) default NULL," +
//        "Mobil VARCHAR(250) default NULL," +
//        "Mail VARCHAR(350) default NULL, Webseite VARCHAR(350) default NULL,Notizen VARCHAR(10000)," +"Datum DATE DEFAULT CURRENT_DATE," +
//        "deleted INTEGER DEFAULT 0, " +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
//        "PRIMARY KEY  (id))",
//        
//        "CREATE TABLE hersteller (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
//        "nummer VARCHAR(250),Firma VARCHAR(250) default NULL,Anrede VARCHAR(250) default NULL," +
//        "Vorname VARCHAR(250) default NULL, " +
//        "Name VARCHAR(250) default NULL, Str VARCHAR(250) default NULL, PLZ VARCHAR(50) default NULL," +
//        "Ort VARCHAR(300) default NULL, Tel VARCHAR(250) default NULL,Fax VARCHAR(250) default NULL," +
//        "Mobil VARCHAR(250) default NULL," +
//        "Mail VARCHAR(350) default NULL, Webseite VARCHAR(350) default NULL,Notizen VARCHAR(10000)," +"Datum DATE DEFAULT CURRENT_DATE," +
//        "deleted INTEGER DEFAULT 0, " +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
//        "PRIMARY KEY  (id))",
//        
//        "CREATE TABLE rechnungen (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
//        "Rechnungnummer VARCHAR(250) UNIQUE NOT NULL, KundenId INTEGER REFERENCES kunden (id)," +
//        "Datum DATE NOT NULL," + "storno INTEGER DEFAULT 0," + "bezahlt INTEGER DEFAULT 0," +
//        "gesamtpreis DOUBLE DEFAULT 0," + "gesamttax DOUBLE DEFAULT 0," +
//        "AfDatum DATE NOT NULL," + "mahnungen INTEGER DEFAULT 0," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
//        "PRIMARY KEY  (id))",
//        
//        "CREATE TABLE rechnungsposten (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "rechnungid INTEGER REFERENCES rechnungen (id)," + "anzahl DOUBLE DEFAULT 0 NOT NULL," +
//        "posten VARCHAR(1000) default NULL,  preis DOUBLE DEFAULT 0 NOT NULL, " +
//        "steuersatz DOUBLE DEFAULT 0 NOT NULL," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
//        "PRIMARY KEY  (id))",
//        
//        
//        "CREATE TABLE angebote (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
//        "angebotnummer VARCHAR(250) default NULL, KundenId INTEGER REFERENCES kunden(id)," +
//        "Datum DATE NOT NULL," + "auftragdatum DATE DEFAULT NULL," + "anfragevom DATE NOT NULL," + "validvon DATE NOT NULL," + "validbis DATE NOT NULL," +
//        "rechnungid INTEGER DEFAULT 0," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
//        "PRIMARY KEY  (id))",
//        
//        "CREATE TABLE angebotposten (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "angebotid INTEGER REFERENCES angebote  (id)," + "anzahl DOUBLE DEFAULT 0 NOT NULL," +
//        "posten VARCHAR(1000) default NULL,  preis DOUBLE DEFAULT 0 NOT NULL, " +
//        "steuersatz DOUBLE DEFAULT 0 NOT NULL," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
//        "PRIMARY KEY  (id))",
//        
//        "CREATE TABLE einstellungen (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "name VARCHAR(250), wert VARCHAR(2500)," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        
//        "CREATE TABLE rechnungbetreffz (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "name VARCHAR(100), text VARCHAR(500), isvorlage INTEGER DEFAULT 0," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//
//        "CREATE TABLE programmdaten (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "name VARCHAR(250), wert VARCHAR(2500)," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        
//        "CREATE TABLE steuersaetze (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "name VARCHAR(250), wert DOUBLE DEFAULT 0," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        
//        "CREATE TABLE dienstleistungen (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "produktnummer VARCHAR(500),name VARCHAR(50),beschreibung VARCHAR(500),einheit VARCHAR(50)," +
//        "preis DOUBLE DEFAULT 0, steuersatzid INTEGER REFERENCES steuersaetze (id)," +
//        "warengruppenid INTEGER  DEFAULT 0," + "datum DATE NOT NULL," +
//        "deleted INTEGER DEFAULT 0," +       
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        
//        "CREATE TABLE produkte (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "produktnummer VARCHAR(500),name VARCHAR(50),text VARCHAR(500),vk DOUBLE DEFAULT 0,ek DOUBLE DEFAULT 0, steuersatzid INTEGER REFERENCES steuersaetze (id)," +
//        "herstellerid INTEGER REFERENCES hersteller(id), lieferantenid INTEGER REFERENCES lieferanten (id)," +
//        "warengruppenid INTEGER DEFAULT 0,datum DATE NOT NULL,url VARCHAR(250) default NULL," +
//        "ean VARCHAR(25),bestellnr VARCHAR(50),herstellernr VARCHAR(50),lieferantennr VARCHAR(50)," + 
//        "bestelldatum VARCHAR(50),bestellmenge DOUBLE DEFAULT 0, lagermenge DOUBLE DEFAULT 0," +    
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        
//        "CREATE TABLE warengruppenkategorien (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "kategorienummer VARCHAR(120),name VARCHAR(500)," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        
//        "CREATE TABLE benutzer (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "username VARCHAR(250),password VARCHAR(250)," +
//        "createdon DATE default NULL," +
//        "createdby VARCHAR(50)," + "iseditor INTEGER DEFAULT 0," + "isadmin INTEGER DEFAULT 0," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        
//        "CREATE TABLE warengruppenfamilien (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "familienummer VARCHAR(120), kategorieid  INTEGER DEFAULT 0, name VARCHAR(500)," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        
//        "CREATE TABLE warengruppengruppen (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "gruppenummer VARCHAR(120),familienid  INTEGER DEFAULT 0,name VARCHAR(500)," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        
//        "CREATE TABLE dateien (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "productid INTEGER  DEFAULT 0,url VARCHAR(500)," +
//        "datum DATE NOT NULL," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        
//        "CREATE TABLE historie (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "aktion VARCHAR(500) default NULL,text VARCHAR(250) default NULL," +
//        "datum DATE NOT NULL," +
//        "benutzer VARCHAR(500) default NULL," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        
//        "CREATE TABLE konten (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "nummer VARCHAR(500) default NULL,klasse VARCHAR(1000) default NULL," +
//        "gruppe VARCHAR(1000) default NULL," +
//        "art VARCHAR(1000) default NULL," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        
//        "CREATE TABLE ausgaben (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "kontenid INTEGER REFERENCES konten (id), beschreibung VARCHAR(500) default NULL," +
//        "preis DOUBLE DEFAULT 0," + "tax INTEGER NOT NULL," + "datum DATE NOT NULL," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        
//        "CREATE TABLE einnahmen (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "kontenid INTEGER REFERENCES konten (id), beschreibung VARCHAR(500) default NULL," +
//        "preis DOUBLE DEFAULT 0," + "tax INTEGER NOT NULL," + "datum DATE NOT NULL," +
//        "deleted INTEGER DEFAULT 0," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        
//        "CREATE TABLE  betreffz_zu_rechnung   (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "rechnungid INTEGER REFERENCES rechnungen (id)," +
//        "betreffzid INTEGER REFERENCES rechnungbetreffz (id)," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        
//        "CREATE TABLE plugins_to_load (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "name VARCHAR(250), wert VARCHAR(2500)," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//                
//        "CREATE TABLE rowlock (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//        "tablename VARCHAR(250), rowid INTEGER NOT NULL," +
//        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))"
//
//
//    };
}
