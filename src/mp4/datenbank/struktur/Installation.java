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
package mp4.datenbank.struktur;

import mp3.classes.interfaces.*;
import java.io.File;

/**
 *
 * @author anti
 */
public interface Installation {

    //////////////////////////////////////////////////////////////////////////77
    /**
     * Installation data (tables)
     */
    public final String[] SQL_COMMAND = {
        "CREATE TABLE kunden (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "Kundennummer VARCHAR(250),Firma VARCHAR(250) default NULL,Anrede VARCHAR(250) default NULL," +
        "Vorname VARCHAR(250) default NULL, " +
        "Name VARCHAR(250) default NULL, Str VARCHAR(250) default NULL, PLZ VARCHAR(50) default NULL," +
        "Ort VARCHAR(300) default NULL, Tel VARCHAR(250) default NULL,Fax VARCHAR(250) default NULL," +
        "Mobil VARCHAR(250) default NULL," +
        "Mail VARCHAR(350) default NULL, Webseite VARCHAR(350) default NULL,Notizen VARCHAR(10000)," +
        "deleted INTEGER DEFAULT 0, " +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
        "PRIMARY KEY  (id))",
        
        "CREATE TABLE lieferanten (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "lieferantennummer VARCHAR(250),Firma VARCHAR(250) default NULL," +
        "Str VARCHAR(250) default NULL, PLZ VARCHAR(50) default NULL," +
        "Ort VARCHAR(300) default NULL, Tel VARCHAR(250) default NULL,Fax VARCHAR(250) default NULL," +
        "Mobil VARCHAR(250) default NULL," +
        "Mail VARCHAR(350) default NULL, Webseite VARCHAR(350) default NULL,Notizen VARCHAR(10000)," +
        "deleted INTEGER DEFAULT 0, " +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
        "PRIMARY KEY  (id))",
        
        "CREATE TABLE hersteller (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "herstellernummer VARCHAR(250),Firma VARCHAR(250) default NULL,Anrede VARCHAR(250) default NULL," +
        "Vorname VARCHAR(250) default NULL, " +
        "Name VARCHAR(250) default NULL, Str VARCHAR(250) default NULL, PLZ VARCHAR(50) default NULL," +
        "Ort VARCHAR(300) default NULL, Tel VARCHAR(250) default NULL,Fax VARCHAR(250) default NULL," +
        "Mobil VARCHAR(250) default NULL," +
        "Mail VARCHAR(350) default NULL, Webseite VARCHAR(350) default NULL,Notizen VARCHAR(10000)," +
        "deleted INTEGER DEFAULT 0, " +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
        "PRIMARY KEY  (id))",
        
        "CREATE TABLE rechnungen (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "Rechnungnummer VARCHAR(250) UNIQUE NOT NULL, KundenId INTEGER REFERENCES kunden (id)," +
        "Datum DATE NOT NULL," + "storno INTEGER DEFAULT 0," + "bezahlt INTEGER DEFAULT 0," +
        "gesamtpreis DOUBLE DEFAULT 0," + "gesamttax INTEGER NOT NULL," +
        "AfDatum DATE NOT NULL," + "mahnungen INTEGER DEFAULT 0," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
        "PRIMARY KEY  (id))",
        
        "CREATE TABLE rechnungsposten (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "rechnungid INTEGER REFERENCES rechnungen (id)," + "anzahl DOUBLE DEFAULT 0," +
        "posten VARCHAR(1000) default NULL,  preis DOUBLE DEFAULT 0, " +
        "steuersatz DOUBLE DEFAULT 0," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
        "PRIMARY KEY  (id))",
        
        
        "CREATE TABLE angebote (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "angebotnummer VARCHAR(250) default NULL, KundenId INTEGER REFERENCES kunden(id)," +
        "Datum DATE NOT NULL," + "auftragdatum DATE DEFAULT NULL," + "anfragevom DATE NOT NULL," + "validvon DATE NOT NULL," + "validbis DATE NOT NULL," +
        "rechnungid INTEGER DEFAULT 0," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
        "PRIMARY KEY  (id))",
        
        "CREATE TABLE angebotposten (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "angebotid INTEGER REFERENCES angebote  (id)," + "anzahl DOUBLE DEFAULT 0," +
        "posten VARCHAR(1000) default NULL,  preis DOUBLE DEFAULT 0, " +
        "steuersatz DOUBLE DEFAULT 0," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
        "PRIMARY KEY  (id))",
        
        "CREATE TABLE daten (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "name VARCHAR(250), wert VARCHAR(250)," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
        
        "CREATE TABLE rechnungbetreffz (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "name VARCHAR(100), text VARCHAR(500), isvorlage INTEGER DEFAULT 0," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",

        "CREATE TABLE programmdaten (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "name VARCHAR(250), wert VARCHAR(2500)," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
        
        "CREATE TABLE steuersaetze (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "name VARCHAR(250), wert DOUBLE DEFAULT 0," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
        
        "CREATE TABLE dienstleistungen (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "produktnummer VARCHAR(500),name VARCHAR(50),beschreibung VARCHAR(500),einheit VARCHAR(50)," +
        "preis DOUBLE DEFAULT 0, steuersatzid INTEGER REFERENCES steuersaetze (id)," +
        "warengruppenid INTEGER  DEFAULT 0," + "datum DATE NOT NULL," +
        "deleted INTEGER DEFAULT 0," +       
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
        
        "CREATE TABLE produkte (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "produktnummer VARCHAR(500),name VARCHAR(50),text VARCHAR(500),vk DOUBLE DEFAULT 0,ek DOUBLE DEFAULT 0, steuersatzid INTEGER REFERENCES steuersaetze (id)," +
        "hersteller VARCHAR(250),lieferantenid INTEGER DEFAULT 0," +
        "warengruppenid INTEGER DEFAULT 0," + "datum DATE NOT NULL," + "url VARCHAR(250) default NULL," +
        "ean VARCHAR(25)," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
        
        "CREATE TABLE warengruppenkategorien (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "kategorienummer VARCHAR(120),name VARCHAR(500)," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
        
        "CREATE TABLE benutzer (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "username VARCHAR(250),password VARCHAR(250)," +
        "createdon DATE NOT NULL," +
        "createdby VARCHAR(50)," + "iseditor INTEGER DEFAULT 0," + "isadmin INTEGER DEFAULT 0," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
        
        "CREATE TABLE warengruppenfamilien (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "familienummer VARCHAR(120), kategorieid  INTEGER DEFAULT 0, name VARCHAR(500)," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
        
        "CREATE TABLE warengruppengruppen (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "gruppenummer VARCHAR(120),familienid  INTEGER DEFAULT 0,name VARCHAR(500)," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
        
        "CREATE TABLE dateien (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "productid INTEGER  DEFAULT 0,url VARCHAR(500)," +
        "datum DATE NOT NULL," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
        
        "CREATE TABLE historie (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "aktion VARCHAR(500) default NULL,text VARCHAR(250) default NULL," +
        "datum DATE NOT NULL," +
        "benutzer VARCHAR(500) default NULL," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
        
        "CREATE TABLE konten (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "nummer VARCHAR(500) default NULL,klasse VARCHAR(1000) default NULL," +
        "gruppe VARCHAR(1000) default NULL," +
        "art VARCHAR(1000) default NULL," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
        
        "CREATE TABLE ausgaben (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "kontenid INTEGER REFERENCES konten (id), beschreibung VARCHAR(500) default NULL," +
        "preis DOUBLE DEFAULT 0," + "tax INTEGER NOT NULL," + "datum DATE NOT NULL," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
        
        "CREATE TABLE einnahmen (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "kontenid INTEGER REFERENCES konten (id), beschreibung VARCHAR(500) default NULL," +
        "preis DOUBLE DEFAULT 0," + "tax INTEGER NOT NULL," + "datum DATE NOT NULL," +
        "deleted INTEGER DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
        
        "CREATE TABLE  betreffz_zu_rechnung   (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "rechnungid INTEGER REFERENCES rechnungen (id)," +
        "betreffzid INTEGER REFERENCES rechnungbetreffz (id)," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
        
        
        
        "INSERT INTO steuersaetze (name, wert) VALUES ('Default', 0.0)",
        
        
        
        "INSERT INTO programmdaten(name, wert) VALUES ('MAHNUNG_TEXT_DEFAULT',  'Sehr geehrter {KUNDE_ANREDE} {KUNDE_VORNAME} {KUNDE_NAME},\nin Bezug auf unsere Rechnung Nr. "+ 
        "{RECHNUNG_NUMMER} vom {RECHNUNG_DATUM} mussten wir heute feststellen, dass " +
                "Ihre Zahlung bei uns leider noch nicht eingegangen ist.\n\n" +
                "Bitte überweisen Sie umgehend den fälligen Rechnungsbetrag " +
                "zuzüglich der Mahngebühr auf unser Konto.\n\n"+
                "Sollten Sie die Zahlung bereits vorgenommen haben, dann setzen Sie sich bitte mit uns " +
                "in Verbindung. Möglicherweise konnten wir Ihre Zahlung nicht zuordnen, " +
                "weil z.B. der Verwendungszweck nicht korrekt angegeben wurde.\n\nMit freundlichen Grüßen\n\n')",
        
                
                
        "INSERT INTO rechnungbetreffz (name, text, isvorlage) VALUES ('Unser Angebot..', 'Unser Angebot vom {Angebot_Datum}', 1)",
        "INSERT INTO rechnungbetreffz (name, text, isvorlage) VALUES ('Ihr Auftrag..', 'Ihr Auftrag vom {Auftrag_Datum}', 1)",
        "INSERT INTO rechnungbetreffz (name, text, isvorlage) VALUES ('Bearbeiter..', 'Bearbeiter: {Benutzer}', 1)",
         
         
         
        "INSERT INTO daten (name, wert) VALUES ('Backup Verzeichnis', '" + System.getProperty("user.home") + File.separator + ProtectedStrings.PROG_NAME + File.separator + "backups" + "')",
        "INSERT INTO daten (name, wert) VALUES ('PDF Rechnungen Verzeichnis', '" + System.getProperty("user.home") + File.separator + ProtectedStrings.PROG_NAME + File.separator + "PDF" + File.separator + "Rechnungen" + "')",
        "INSERT INTO daten (name, wert) VALUES ('PDF Angebote Verzeichnis', '" + System.getProperty("user.home") + File.separator + ProtectedStrings.PROG_NAME + File.separator + "PDF" + File.separator + "Angebote" + "')",
        "INSERT INTO daten (name, wert) VALUES ('PDF Mahnung Verzeichnis', '" + System.getProperty("user.home") + File.separator + ProtectedStrings.PROG_NAME + File.separator + "PDF" + File.separator + "Mahnungen" + "')",
        "INSERT INTO daten (name, wert) VALUES ('Rechnung Template', '" + System.getProperty("user.home") + File.separator + ProtectedStrings.PROG_NAME + File.separator + "templates" + File.separator + "rechnung-mwst.pdf')",
        "INSERT INTO daten (name, wert) VALUES ('Angebot Template', '" + System.getProperty("user.home") + File.separator + ProtectedStrings.PROG_NAME + File.separator + "templates" + File.separator + "angebot.pdf')",
        "INSERT INTO daten (name, wert) VALUES ('Mahnung Template', '" + System.getProperty("user.home") + File.separator + ProtectedStrings.PROG_NAME + File.separator + "templates" + File.separator + "mahnung.pdf')",
        "INSERT INTO daten (name, wert) VALUES ('Serienbrief Template', '" + System.getProperty("user.home") + File.separator + ProtectedStrings.PROG_NAME + File.separator + "templates" + File.separator + "serienbrief.pdf')",
        "INSERT INTO daten (name, wert) VALUES ('Mehrwertsteuersatz', '19')",
        "INSERT INTO daten (name, wert) VALUES ('PDF Programm', '/opt/kde3/bin/kpdf')",
        "INSERT INTO daten (name, wert) VALUES ('Internetbrowser', '/usr/bin/firefox')",
        
        "INSERT INTO daten (name, wert) VALUES ('Default Konto Einnahme (auf dieses Konto werden auch Ihre Rechnungen verbucht!)', '2112')",
        "INSERT INTO daten (name, wert) VALUES ('Default Konto Ausgabe', '1111')",
        "INSERT INTO daten (name, wert) VALUES ('Land (Waehrung) (DE,CH)', 'DE')",
       
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
        "INSERT INTO warengruppenkategorien (kategorienummer,name) VALUES ('1','Kategorie')",
        "INSERT INTO warengruppenfamilien (familienummer, kategorieid, name) VALUES ('1',1,'Familie')",
        "INSERT INTO warengruppengruppen (gruppenummer, familienid, name) VALUES ('1',1,'Gruppe')"
    };
}
