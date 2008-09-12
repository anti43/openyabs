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

import java.io.File;
import java.util.Date;
import mp4.installation.Setup;
import mp4.utils.datum.DateConverter;

/**
 *
 * @author Andreas
 */
public abstract class Daten {
    
    /**
     * 
     */
    public final static String[] SQL_COMMAND = {
    
//        ******************************************************************************************************************************
        
        "INSERT INTO steuersaetze (name, wert) VALUES ('Default', 0.0)",
        "INSERT INTO steuersaetze (name, wert) VALUES ('Allgemein - DE', 19)",
        "INSERT INTO hersteller (nummer,Firma,Anrede,Vorname,Name,Str,PLZ,Ort,Tel,Fax,Mobil,Mail,Webseite,Notizen ) VALUES ('HS-00000','Default','','','Default','','','','','','','','','') ",
        "INSERT INTO lieferanten (nummer,Firma,Anrede,Vorname,Name,Str,PLZ,Ort,Tel,Fax,Mobil,Mail,Webseite,Notizen ) VALUES ('LF-00000','Default','','','Default','','','','','','','','','') ",

//        *****************************************Programmdatenb*************************************************************************************        
        
        "INSERT INTO programmdaten(name, wert) VALUES ('MAHNUNG_TEXT_DEFAULT',  'Sehr geehrter {KUNDE_ANREDE} {KUNDE_VORNAME} {KUNDE_NAME},\nin Bezug auf unsere Rechnung Nr. "+ 
        "{RECHNUNG_NUMMER} vom {RECHNUNG_DATUM} mussten wir heute feststellen, dass " +
                "Ihre Zahlung bei uns leider noch nicht eingegangen ist.\n\n" +
                "Bitte überweisen Sie umgehend den fälligen Rechnungsbetrag " +
                "zuzüglich der Mahngebühr auf unser Konto.\n\n"+
                "Sollten Sie die Zahlung bereits vorgenommen haben, dann setzen Sie sich bitte mit uns " +
                "in Verbindung. Möglicherweise konnten wir Ihre Zahlung nicht zuordnen, " +
                "weil z.B. der Verwendungszweck nicht korrekt angegeben wurde.\n\nMit freundlichen Grüßen\n\n')",
                
        "INSERT INTO programmdaten(name, wert) VALUES ('RECHNUNG_NUMMER_FORMAT',  '{JAHR}-{MONAT_NAME}-&!00000&!2')",
        "INSERT INTO programmdaten(name, wert) VALUES ('ANGEBOT_NUMMER_FORMAT',  '{JAHR}-{MONAT_NAME}-&!00000&!2')", 
        "INSERT INTO programmdaten(name, wert) VALUES ('MANUFACTURER_NUMMER_FORMAT',  'HS-&!00000&!0')",
        "INSERT INTO programmdaten(name, wert) VALUES ('SUPPLIER_NUMMER_FORMAT',  'LF-&!00000&!0')", 
        "INSERT INTO programmdaten(name, wert) VALUES ('CUSTOMER_NUMMER_FORMAT',  'KU-&!00000&!0')",
        "INSERT INTO programmdaten(name, wert) VALUES ('SERVICES_NUMMER_FORMAT',  'DL-&!00000&!0')", 
        "INSERT INTO programmdaten(name, wert) VALUES ('PRODUCT_NUMMER_FORMAT',   'PR-&!00000&!0')", 
        
        "INSERT INTO programmdaten(name, wert) VALUES ('MAINFRAME_WINDOW_STATE',  '790,1000')",    
        "INSERT INTO programmdaten(name, wert) VALUES ('WARENGRUPPEN_SEPARATOR',  '->')",   
        "INSERT INTO programmdaten(name, wert) VALUES ('IMAGE_CACHE_FOLDER', '" + Setup.instanceOf().getInstall_dirs().getCache_dir() +"')",  
        "INSERT INTO programmdaten(name, wert) VALUES ('PLUGIN_FOLDER', '" + Setup.instanceOf().getInstall_dirs().getPathplugin_dir() +"')",  
       
//        ******************************************************************************************************************************        
        
        
        "INSERT INTO benutzer(username, password, createdon, createdby, iseditor, isadmin) VALUES ('admin',  '2CB336AE1494258BCD5DFD35C698EDB1','"+DateConverter.getSQLDateString(new Date())+"', 'Niemand' , 1, 1)",       
//        ******************************************************************************************************************************        
              
        "INSERT INTO rechnungbetreffz (name, text, isvorlage) VALUES ('Unser Angebot..', 'Unser Angebot vom {Angebot_Datum}', 1)",
        "INSERT INTO rechnungbetreffz (name, text, isvorlage) VALUES ('Ihr Auftrag..', 'Ihr Auftrag vom {Auftrag_Datum}', 1)",
        "INSERT INTO rechnungbetreffz (name, text, isvorlage) VALUES ('Bearbeiter..', 'Bearbeiter: {Benutzer}', 1)",      
//        ******************************************************************************************************************************        
           
        "INSERT INTO daten (name, wert) VALUES ('Backup Verzeichnis', '" + Setup.instanceOf().getInstall_dirs().getBackup_dir() + "')",
        "INSERT INTO daten (name, wert) VALUES ('PDF Rechnungen Verzeichnis', '" +  Setup.instanceOf().getInstall_dirs().getPathpdf_bill_dir()+ "')",
        "INSERT INTO daten (name, wert) VALUES ('PDF Angebote Verzeichnis', '" + Setup.instanceOf().getInstall_dirs().getPathpdf_offer_dir() + "')",
        "INSERT INTO daten (name, wert) VALUES ('PDF Mahnung Verzeichnis', '" + Setup.instanceOf().getInstall_dirs().getPathpdf_mahnung_dir()+ "')",
        "INSERT INTO daten (name, wert) VALUES ('PDF Produkt Verzeichnis', '" + Setup.instanceOf().getInstall_dirs().getPathpdf_produkt_dir()+ "')",
       
        "INSERT INTO daten (name, wert) VALUES ('Rechnung Template', '" + Setup.instanceOf().getInstall_dirs().getPathtemplates_dir() + File.separator + "rechnung.pdf')",
        "INSERT INTO daten (name, wert) VALUES ('Angebot Template', '" + Setup.instanceOf().getInstall_dirs().getPathtemplates_dir() + File.separator + "angebot.pdf')",
        "INSERT INTO daten (name, wert) VALUES ('Mahnung Template', '" + Setup.instanceOf().getInstall_dirs().getPathtemplates_dir() + File.separator + "mahnung.pdf')",
        "INSERT INTO daten (name, wert) VALUES ('Serienbrief Template', '" + Setup.instanceOf().getInstall_dirs().getPathtemplates_dir() + File.separator + "serienbrief.pdf')",
        "INSERT INTO daten (name, wert) VALUES ('Produkt Template', '" + Setup.instanceOf().getInstall_dirs().getPathtemplates_dir() + File.separator + "produkt.pdf')",
     
        
        "INSERT INTO daten (name, wert) VALUES ('Mehrwertsteuersatz', '19')",
        "INSERT INTO daten (name, wert) VALUES ('PDF Programm', '/opt/kde3/bin/kpdf')",
        "INSERT INTO daten (name, wert) VALUES ('Internetbrowser', '/usr/bin/firefox')",
        
        "INSERT INTO daten (name, wert) VALUES ('Default Konto Einnahme (auf dieses Konto werden auch Ihre Rechnungen verbucht!)', '2112')",
        "INSERT INTO daten (name, wert) VALUES ('Default Konto Ausgabe', '1111')",
        "INSERT INTO daten (name, wert) VALUES ('Land (Waehrung) (DE,CH)', 'DE')",
     
        
//        ******************************************************************************************************************************        
       
        
        
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

        
        
        //        ******************************************************************************************************************************        
      
        
        
        "INSERT INTO warengruppenkategorien (kategorienummer,name) VALUES ('1','Produkte')",
        "INSERT INTO warengruppenfamilien (familienummer, kategorieid, name) VALUES ('1',1,'ProduktFamilie1')",
        "INSERT INTO warengruppengruppen (gruppenummer, familienid, name) VALUES ('1',1,'ProduktGruppe1')",
                
        "INSERT INTO warengruppenkategorien (kategorienummer,name) VALUES ('2','Dienstleistungen')",
        "INSERT INTO warengruppenfamilien (familienummer, kategorieid, name) VALUES ('2',2,'ProduktFamilie2')",
        "INSERT INTO warengruppengruppen (gruppenummer, familienid, name) VALUES ('2',2,'ProduktGruppe2')"
};
}

