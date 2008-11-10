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
package mp4.globals;

import mp4.utils.files.FileReaderWriter;

/**
 *
 * @author anti43
 */
public interface Strings {
    
    public static String WELCOME_MESSAGE = "Anmerkungen, Bugs und Feedback zu MP bitte an mp-rechnungs-und-kundenverwaltung@googlegroups.com senden. Vielen Dank!";
    

    public String NO = "Nein";
    public String YES = "Ja";
    public String NO_PDF_PROG = "Bitte geben Sie unter \nBearbeiten-> Einstellungen ein PDF-Programm an.\n";
    public String NO_PDF = "Bitte geben Sie unter \nBearbeiten-> Einstellungen ein PDF-Template an.\n";
    public String NO_BROWSER = "Kein Browser angegeben. Wählen Sie Ihren Internetbrowser unter 'Programmeinstellungen'.";
    public String TEST_CONF = "Teste Konfiguration";
    public String PERMISSION_DENIED = "Nicht möglich, Dateien anzulegen. Programm wird beendet.";
    public String SHUTDOWN = "Programm wird beendet";
    public String COLON = ";";
    public String COMMENT_SIGN = "#";
    public String SETTINGS_NOT_FOUND = "'settings.mp' nicht gefunden. Programm beendet.\n";
    public String DB_INIT = "Initialisiere Datenbank...";
    public String ONE_INSTANCE = "Beenden Sie zuerst alle anderen Instanzen von MP!";
    public String SETTINGS_EXAMPLE = "#Beispiel:<version>;<dbpath>;<dbuser>;<dbpassword>;<dbdriver> [(DERBY)(MYSQL)(custom driver)]";
    
    public String TABLE_Kunde_PRINT_HEADER =
            "K-Nr" + "," + "Firma" + "," + "Anrede" + "," + "Vorname" +
            "," + "Name" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" + "," + "Fax" + "," + "Mobil" + "," + "Mail" + "," + "Webseite";
    public String TABLE_SUPPLIER_PRINT_HEADER =
            "L-Nr" + "," + "Firma" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" + "," + "Fax" + "," + "Mobil" + "," + "Mail" + "," + "Webseite";
    
    public String TABLE_MANUFACTURER_PRINT_HEADER =
            "H-Nr" + "," + "Firma" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" + "," + "Fax" + "," + "Mobil" + "," + "Mail" + "," + "Webseite";
    
    public String TABLE_PRODUCTS_LIST_PRINT_HEADER =
            "P-Nr" + "," + "Name" + "," + "Text" + "," + "VK" + "," + "EK" +
            "," + "Tax" + "," + "Hersteller" + "," + "Lieferant" + "," + "Warengruppennr" + "," +
            "Datum" + "," + "EAN";
   
    public String[] MONTHS= new String[]{"Jan","Feb","Mar","Apr","Mai","Jun","Jul","Aug","Sep","Okt","Nov","Dez"};
    public String[] EUR_TEMPLATE = new String[]{
            "1110 Waren, Rohstoffe und Hilfsstoffe einschl. der Nebenkosten",
            "1120 Bezogene Leistungen (z.B. Fremdleistungen) ",
            "1130 Ausgaben für eigenes Personal  ",
            "1140 Aufwendungen für geringwertige Wirtschaftsgüter ",
            "1150 Miete / Pacht für Geschäftsräume und betrieblich genutzte Grundstücke ",
            "1160 Sonstige Aufwendungen für betrieblich genutzte Grundstücke ",
            "1170 Abziehbare Aufwendungen für ein häusliches Arbeitszimmer ",
            "1180 Reisekosten,Aufwendungen für doppelte Haushaltsführung ",
            "1190 Geschenke – abziehbar ",
            "1200 Geschenke – nicht abziehbar ",
            "1210 Bewirtung – abziehbar ",
            "1220 Bewirtung – nicht abziehbar ",
            "1230 Übrige Betriebsausgaben ",
            "1240 Fortbildung und Fachliteratur ",
            "1250 Rechts- und Steuerberatung, Buchführung ",
            "1260 Porto, Telefon, Büromaterial ",
            "1270 An das Finanzamt gezahlte und ggf. verrechnete Umsatzsteuer ",
            
            "<html>&nbsp;",
            
            "<html><p align=right><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "Gezahlte Vorsteuerbeträge: &nbsp;",
            
            "<html>&nbsp;",
            
            "<html><p align=right><b><font color=red>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;Summe Betriebsausgaben: &nbsp;",
            "<html>&nbsp;",
            
            "2100 Betriebseinnahmen als umsatzsteuerlicher Kleinunternehmer ",
            "2110 Umsatzsteuerpflichtige Betriebseinnahmen ",
            "2120 Sonstige Sach-, Nutzungs- und Leistungsentnahmen ",
            "2130 Private Kfz-Nutzung ",
            "2140 Vom Finanzamt erstattete und ggf. verrechnete Umsatzsteuer",
            "<html>&nbsp;&nbsp;",
            
            "<html><p align=right><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;" +
                    "Vereinnahmte Vorsteuerbeträge: &nbsp;",
            
            "<html>&nbsp;",
            "<html><p align=right><b><font color=green>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "Summe Betriebseinnahmen: &nbsp;",
            
            "<html>&nbsp;",
            
            "<html><p align=right>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "<b>Gesamtübersicht:&nbsp;",
            
            "<html><p align=right><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+&nbsp;Einnahmen: &nbsp;" +
                    "</b>",
            "<html><p align=right><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;" +
                    "Ausgaben: &nbsp;</b>",
            "<html><p align=right><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "=&nbsp;&nbsp;&nbsp;&nbsp;Summe: &nbsp;</b>"
                    
        };
   
    
    /**
     * 
     * String utilitis
     */
    public final String ALL = "*";
    public final String NULL = "";
    public final String ID = "id";
    public final String NOTNULL = "NOT null";
    public final String Kunde = "Kunde";
    public final String SUPPLIER = "Lieferant";
    public final String MANUFACTURER = "Hersteller";
    public final String PRODUCT = "Produkt";
    public final String OFFER = "Angebot";
    public final String SERVICE = "Dienstleistung";
    public final String BILL = "Rechnung";
    public final String BACKUP = "Datensicherung";
    public final String KONTEN = "Konten";
    public final String EINNAHME = "Einnahme";
    public final String CONFIG = "Konfiguration";
    
    
    //GPL
    
    public final String START_MESSAGE = 
        "\n"+
        "\n  MP " + Constants.VERSION + " Copyright (C) 2006-2008 Andreas Weber\n"+
        "\n  This program comes with ABSOLUTELY NO WARRANTY."+
        "\n  MP is free software, and you are welcome to redistribute it " +
        "\n  under certain conditions;" +
        "\n  Start with -license for details.\n" +
        "\n  Start with -help for command line options.\n" +
        "*****************************************************************\n\n";

    public final String GPL = new FileReaderWriter("license.txt").read();

    
}
