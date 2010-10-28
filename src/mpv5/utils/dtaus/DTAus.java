/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.dtaus;

/*
 * This file is part of Frame4J.

Frame4J is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

Frame4J is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with Frame4J. If not, see http://www.gnu.org/licenses/ or
http://www.a-weinert.de/java/docs/frame4j/de/frame4j/util/doc-files/.

 *  Copyright 2009 Albrecht Weinert, Bochum, Germany (a-weinert.de)
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import static de.frame4j.util.ComVar.*;
import de.frame4j.io.DataFile;
import de.frame4j.util.ComVar;
import de.frame4j.util.ConstTime;
import de.frame4j.util.TimeHelper;
import de.frame4j.util.TextHelper;
import de.frame4j.util.TimeRO;

import de.frame4j.util.App;
import de.frame4j.xml.SAXHandler;
import de.frame4j.xml.XMLconf;

/** <b>DTAus-Datensätze für Sammelüberweisung oder -lastschrift</b>. <br />
 *  <br />
 *  Although being part of Frame4J, this class's documentation is
 *  intentionally in German language. See
 *  <a href="./package-summary.html#pbg">this hint</a>, please.<br />
 *  <br />
 *  This application creates the message accepted by all German and bigger
 *  European banks for bulk transfers from appropriate XML input. Customers
 *  having the necessary privileges may put those bulk order by online banking
 *  or by sending a disk.<br />
 *  <br />
 *  Diese Anwendung erzeugt eine DTAus-Zeichenfolge für Sammellastschriften
 *  oder -überweisungen für das Austauschformat DTA oder DTAUS deutscher
 *  Banken. Die erzeugte Datei wird per Diskette eingereicht oder per
 *  online-banking hochgeladen.<br />
 *  <br />
 *  Die DTAus-Zeichenfolge besteht aus ein oder sinnvollerweise mehreren
 *  {@link Transaction}en gleichen Typs. Sie müssen entweder Lastschriften
 *  oder Überweisungen sein; und alle Transaktionen haben dasselbe
 *  "Auftraggeberkonto".<br />
 *  <br />
 *  Diese Klasse kann von anderen als Banking-Anwendungen genutzt werden oder
 *  aber als eigenständige Anwendung. (Als Anwendung nutzt sie eine anonyme
 *  innere Ableitung von {@link App}).  &nbsp; Mit <br />
 *  <br /> &nbsp;
&nbsp; java de.a_weinert.dta.DTAus [optionen] xml-Eingabe [DTAUS0.TXT]<br />
 *  <br />
 *  transformiert man eine XML-Datei (s.u.) in das DTA-Format. Das Ergebnis
 *  kann bei einigen Banken und im Allgemeinen nur mit entsprechender
 *  zusätzlicher Vereinbarung als Diskette eingereicht bzw. mit einem
 *  online-banking Web-Dienst hochgeladen werden.<br />
 *  <br />
 *  Das XML-Format ist anhand des folgenden Beispiels (beziehungsweise
 *  von <a href="./doc-files/verein.xml" target="_top">verein.xml</a>)
 *  selbsterklärend:<code><pre>
&nbsp;  &lt;?xml version="1.0" encoding="ISO-8859-1"?&gt;
&nbsp;
&nbsp;  &lt;!-- siehe de.a_weinert.dta.DTAus
&nbsp;       <a href="http://www.a-weinert.de/java/aweinertbib.html"
target="_top">http://www.a-weinert.de/java/aweinertbib.html</a> --&gt;
&nbsp;
&nbsp;  &lt;dta type="LK" termin="11.09.2008"&gt;
&nbsp;    &lt;!-- LK: Lastschrift; GK: Überweisung; Textinhalt: Beschreibung
&nbsp;         nicht für DTA  --&gt;
&nbsp;    Verein der Meerschweinfreunde, Beitrag 2008
&nbsp;    &lt;konto nr="1234567890" blz="900 900 90"
&nbsp;     name="Freunde d. Meerschweine e.V."&gt;
&nbsp;     Das Lastschrifteinzugsbeitragskonto des Vereins
&nbsp;    &lt;/konto&gt;
&nbsp;    &lt;!--  gemeinsame(r) Zwecke(e) für alle Transaktionen --&gt;
&nbsp;    &lt;zweck&gt;Mitgliedsbeitrag 2008&lt;/zweck&gt;
&nbsp;
&nbsp;    &lt;transaction betrag="33,90"&gt;
&nbsp;      Einzug vom ersten Mitglied
&nbsp;       &lt;konto nr="2345678901" blz="990 890 07"
&nbsp;         name="Schnuppi Meerschwein"&gt;
&nbsp;         Konto des Mitglieds Nr. 1
&nbsp;       &lt;/konto&gt;
&nbsp;     &lt;zweck&gt;abzüglich Aufwand für Heu 20,31&lt;/zweck&gt;
&nbsp;    &lt;/transaction&gt;
&nbsp;
&nbsp;    &lt;transaction betrag="54,21"&gt;
&nbsp;      Einzug vom zweiten Mitglied
&nbsp;       &lt;konto nr="3456789012" blz="990 890 07"
&nbsp;         name="Muri Meerschwein"&gt;
&nbsp;         Konto des Mitglieds Nr. 2
&nbsp;       &lt;/konto&gt;
&nbsp;     &lt;zweck&gt;   gemäß Einzusgermächtig. 4.5.  &lt;/zweck&gt;
&nbsp;    &lt;/transaction&gt;
&nbsp;  &lt;/dta&gt;
 *  </pre></code>
 *
 *  Hinweise: Als Gesamtanzahl der Zwecke sind 2 bis 4 sinnvoll. Mögliche
 *  Obergrenzen liegen bei 2, 4 oder 14. Vierzehn ist wohl die bank-gegebene
 *  absolute Obergrenze, deren Überschreiten hier eine Exception
 *  auslöst.<br />
 *  Die maximale Zahl der Transaktionen scheint prinzipiell in den Tausenden
 *  zu liegen, ist für DTAus als Anwendung aber auf 200 begrenzt.<br />
 *  <br />
 *  Die Anwendung wurde für die Lastschrift von Vereinsbeiträgen (wenn auch
 *  nicht von Meerschweinen) mehrfach erfolgreich genutzt. <br />
 *  <br />
 *  <br />
 *  <a href="./package-summary.html#co">&copy;</a>
 *  Copyright 2008  &nbsp; Albrecht Weinert <br />
 *  <br />
 *  @see      Konto
 *  @see      de.a_weinert.dta
 *  @see      TextHelper
 */
// Bisher    V00.00 (19.05.2008 11:37) :  neu
//           V01.05 (21.05.2008 08:24) :  rec-len korr.
//           V10    (19.11.2008 16:19) :  Revisionssprung wg. cvsNT -> SVN
//           V.100+ (23.03.2009 15:52) :  Nach Frame4J
public class DTAus {

    /** Eine Transaktion (innere Klasse). <br />
     *  <br />
     *  Die Daten einer einzelnen Transaktion, die in einen Sammelauftrag
     *  übernommen werden soll.<br />
     *  <br />
     */
    public class Transaction {

        /** Konto des Zahlungsempfängers bzw. des Zahlungspflichtigen. <br />
         *  <br />
         *  Soll dieser Einzelauftrag in eine Sammelüberweisung eingestellt werden,
         *  so ist das die Kontoverbindung des Zahlungsempfängers. Bei einer
         *  Sammellastschriften ist dies die Kontoverbindung des
         *  Zahlungspflichtigen.<br />
         */
        public Konto otherAccount;
        /** Interne Kunden-ID. <br />
         *  <br />
         *  default: null; Verwendung unklar; i.A. unnötig.
         */
        public String internalCustomerId;
        /** Textschlüssel für den Auftrag. <br />
         *  <br />
         *  Bei Sammelüberweisungen ist dies 51, bei Sammellastschriften
         *  hingegen 05. Das ist DTA-Norm.<br />
         */
        public String key;
        /** Zusätzlicher Textschlüssel. <br />
         *  <br />
         *  Er wird i.d.R. nur bankintern verwendet.<br />
         *  <br />
         *  default: &quot;000&quot;; andere Belegung unklar.<br />
         */
        public String addkey = "000";
        /** Beschreibung des Kontos (optional). <br />
         *  <br />
         *  Ohne Bedeutung für DTA.<br />
         *  default: null
         */
        public String descr;


        /** Geldbetrag in cent. <br />
         *  <br />
         *  Zu überweisen (bei Sammelüberweisungen) oder einzuziehen
         *  (bei Sammellastschriften).<br />
         *  <br />
         *  Wertbereich: immer &gt; 0. <br />
         *  Hinweis: Manche Banken haben Mindestbeträge (z.B. 5,00 Euro)
         *  für Lastschriften. <br />
         *  <br />
         */
        public final int getValue() {
            return value;
        }
        private int value;

        /** Geldbetrag in cent setzen. <br />
         *  <br />
         *  @see #getValue()
         */
        public void setValue(final int value) throws IllegalArgumentException {
            if (value < 15) {
                throw new IllegalArgumentException("illegaler Betrag");
            }
            this.value = value;
        } // setValue(int)

        /** Geldbetrag in Euro setzen. <br />
         *  <br />
         *  Zu überweisen (Sammelüberweisungen) oder einzuziehen
         *  (Sammellastschriften).<br />
         *  Format: 123,45<br />
         *  <br />
         *  Wertbereich: immer &gt; 0,15.<br />
         *  <br />
         *  @see #getValue()
         */
        public void setValue(CharSequence value)
                throws IllegalArgumentException {
            String tmp = TextHelper.trimUq(value, null);
            if (tmp == null) {
                throw new IllegalArgumentException("leere Betragsangabe");
            }
            int len = tmp.length();

            int val = 0;
            int aftKom = 0;
            boolean komDa = false;
            for (int i = 0; i < len; ++i) {
                char akt = tmp.charAt(i);
                if (akt >= '0' && akt <= '9') { // Ziffer
                    val = val * 10 + (akt - '0');
                    if (komDa) {
                        ++aftKom;
                    }
                    continue;
                }
                if (akt == ',') {
                    if (komDa) {
                        throw new IllegalArgumentException(",, in Betragsangabe");
                    }
                    komDa = true;
                    continue;
                }
                // Grundsyntax 123,45 ist durch
                // nun noch "12.234,56" und "12,88 Euro" zulassen
                if (akt <= ' ' && aftKom == 2) {
                    break; // ignore ,12 Euro o.ä
                }
                if (akt == '.' && !komDa && val != 0 && i < len - 3) {
                    continue;
                }
                throw new IllegalArgumentException(
                        "illegale Betragsangabe: " + tmp);
            } // for

            if (aftKom == 0) {  // 12 für 12 Euro = 1200 hier erlauben
                val *= 100;
            } else if (aftKom == 1) { // 12,5  für 12 Euro 50 =1250 hier erlauben
                val *= 100;
            } else if (aftKom != 2) { //12,505 hier verbieten
                throw new IllegalArgumentException("zu viele Nachkommastellen: " + val);
            }
            setValue(val);
        } // setValue(CharSequence
        private String[] usage;
        private int anzUsages;

        /** Hinzufügen einer Verwendungszweckzeile zu diesem Auftrag. <br />
         *  <br />
         *  Hiermit hinzufügte Verwendungszwecke reihen sich hinter bereits definierte
         *  gemeinsame Verwendungszwecke (so vorhanden) ein.<br />
         *  <br />
         *  Die oberste (von DTA gesetzte) Grenze sind 14 Verwendungszwecke. Formular-
         *  oder anwendungsbedingt sind oft 2 oder 4 sinnvolle (maximale)
         *  Anzahlen.<br />
         *  <br />
         *  Die Länge wird auf 27 Zeichen beschränkt, und es wird auf den
         *  eingeschränkten DTA-Zeichensatz gewandelt.<br />
         *  <br />
         *  @see DTAus#addUsage(CharSequence)
         *  @see DTAus#DTAtranslate
         *  @see DTAus#check(CharSequence)
         */
        public void addUsage(CharSequence usage) {
            String tmp = TextHelper.trimUq(usage, null);
            if (null == tmp) {
                return;
            }
            this.usage[anzUsages] = tmp;
            ++anzUsages;
        } // addUsage(CharSequence

        /** Erzeugen einer neuen  Transaktion ohne Betrag. <br />
         */
        public Transaction(Konto anderKonto) {
            key = (type == TYPE_CREDIT ? "51" : "05");
            usage = DTAus.this.usage.clone();       // Übernahme gemeinsamer
            this.anzUsages = DTAus.this.anzUsages; // Zweckzeilen
            this.otherAccount = anderKonto;
            if (anderKonto == null || anderKonto.equals(DTAus.this.myAccount)) {
                throw new IllegalArgumentException("kein oder eigenes Konto");
            }
        } // Transaction(Konto

        /** Erzeugen einer neuen  Transaktion. <br />  */
        public Transaction(Konto anderKonto, int value) {
            this(anderKonto);
            setValue(value);
        } // Transaction(Konto, int)

        /** Erzeugen einer neuen  Transaktion. <br /> */
        public Transaction(Konto anderKonto, CharSequence value) {
            this(anderKonto);
            setValue(value);
        } // Transaction(Konto, CharSequence)

        @Override
        public String toString() {
            return "Transaktion " + internalCustomerId + ":" + descr
                    + " [" + value + "]" + curr;
        } // toString()

        /** Zeichenkettendarstellung (DTA). <br />
         *  <br />
         *  Dies ist die DTA-Darstellung, nicht die Textdarstellung für

         *  Menschen.<br />
         */
        public String toDTAstring() {
            StringBuilder ret = new StringBuilder(156);

            ret.append("0000C");
            ret.append(expand(myAccount.getBlz(), 8, ' ', ALIGN_RIGHT));
            ret.append(expand(otherAccount.getBlz(), 8, ' ', ALIGN_RIGHT));
            ret.append(expand(otherAccount.getNumber(), 10, '0', ALIGN_RIGHT));


            //// to do:  oh boy! toString macht die Prüfsummen
            sumBLZ += otherAccount.getBlzNum();
            sumNumber += otherAccount.getNumb();

            if (otherAccount.curr.equals("DEM")) {
                sumDM += value;
            } else {
                sumEUR += value;
            }

            ///  sumAmount += value.getValue();

            if (internalCustomerId == null) {
                ///    1234567890123
                ret.append("0000000000000"); // 13 Nullen
            } else {
                ret.append('0');
                ret.append(expand(check(internalCustomerId), 11, '0',
                        ALIGN_LEFT));
                ret.append('0');
            }

            ret.append(expand(key, 2, '0', ALIGN_RIGHT));
            ret.append(expand(addkey, 3, '0', ALIGN_RIGHT));
            ret.append(' ');
            if ("DEM".equals(otherAccount.curr)) {
                ret.append(expand(Integer.toString(value), 11, '0',
                        ALIGN_RIGHT));
            } else {    // 12345678901
                ret.append("00000000000");
            }
            ret.append(expand(myAccount.getBlz(), 8, ' ', ALIGN_RIGHT));
            ret.append(expand(myAccount.getNumber(), 10, '0', ALIGN_RIGHT));
            if (!"DEM".equals(otherAccount.curr)) {
                ret.append(expand(Integer.toString(value), 11, '0',
                        ALIGN_RIGHT));
            } else {    // 12345678901
                ret.append("00000000000");
            }
            ret.append("   "); // 3 Leerzeichen
            ret.append(expand(check(otherAccount.name), 27, ' ', ALIGN_LEFT));
            ret.append("        "); // 8 Leerzeichen

            ret.append(expand(check(myAccount.name), 27, ' ', ALIGN_LEFT));

            String st = "";
            if (anzUsages != 0) {
                st = check(usage[0]);
            }
            ret.append(expand(st, 27, ' ', ALIGN_LEFT));

            ret.append(curr);
            ret.append("  "); // 2 LZ

            int posForNumOfExt = ret.length();
            ret.append("00");

            int basicLenOfCSet = 128 + 27 + 27 + 5;
            int realLenOfCSet = basicLenOfCSet;
            int numOfExt = 0;

            // Erweiterungsteile
            // *** name2 für myAccount und otherAccount vorerst weggelassen

            for (int i = 1; i < anzUsages; ++i) {
                st = check(usage[i]);

                if (((realLenOfCSet % 128) + 29) > 128) {
                    int diff = 128 - (realLenOfCSet % 128);
                    ret.append(expand("", diff, ' ', ALIGN_LEFT));
                    realLenOfCSet += diff;
                }
                ret.append("02");
                ret.append(expand(st, 27, ' ', ALIGN_LEFT));
                realLenOfCSet += 29;
                ++numOfExt;
            } // for

            if ((realLenOfCSet % 128) != 0) {
                int diff = 128 - (realLenOfCSet % 128);
                ret.append(expand("", diff, ' ', ALIGN_LEFT));
                realLenOfCSet += diff;
            }

            ret.replace(posForNumOfExt, posForNumOfExt + 2,
                    TextHelper.twoDigit(numOfExt));
            ret.replace(0, 4,
                    expand(Integer.toString(basicLenOfCSet + 29 * numOfExt), 4,
                    '0', ALIGN_RIGHT));
            return ret.toString();
        } // toDTAstring()   DTA !Mensch -toString
    } // class Transaction =================================================
    private static final byte CURR_DM = 0x20;  // obsolete
    private static final byte CURR_EUR = 0x31;
    private static final boolean ALIGN_LEFT = true;
    private static final boolean ALIGN_RIGHT = false;
    private Konto myAccount;
    String dtaDescr;     // Kommentar für dtaus (future use, ignEclWrn)
    /** Typ des Sammelauftrags: Sammelüberweisung. <br />
     *  <br />
     *  Wert: &quot;GK&quot; (Überweisung Kunde)<br />
     */
    public static final String TYPE_CREDIT = "GK";
    /** Typ des Sammelauftrags: Lastschrift. <br />
     *  <br />
     *  Wert: &quot;GK&quot; (Lastschrift Kunde)<br />
     */
    public static final String TYPE_DEBIT = "LK";   // ;
    // Typ des Sammelauftrags
    private String type = TYPE_CREDIT; // Überweisung Kunde;
    private ConstTime execdate;
    private char curr = CURR_EUR;
    private Transaction[] entries;
    private int anzTrans;
    private long sumDM;
    private long sumEUR;
    private long sumBLZ;
    private long sumNumber;
    private String[] usage;
    private int anzUsages;

    /** Hinzufügen einer gemeinsamen Verwendungszweckzeile zum
     *                                                   Sammelauftrag. <br />
     *  <br />
     *  Gemeinsame Verwendungszwecke werden (so vorhanden) bei jeder (hernach)
     *  erzeugten {@link Transaction TraTransaktion} als erste
     *  Verwendungszeilen übernommen.<br />
     *  <br />
     *  Dies entspricht weitgehender Verwendungspraxis: Häufig sind ja alle
     *  erste Zweckzeilen aller (Sammel-) Lastschriften gleich (z.B.
     *  &quot;Mitgliedsbeitrag 2009&quot;).<br />
     *  <br />
     *  @see Transaction#addUsage(CharSequence)
     */
    public void addUsage(CharSequence usage) {
        String tmp = TextHelper.trimUq(usage, null);
        if (null == tmp) {
            return;
        }
        this.usage[anzUsages] = tmp;
        ++anzUsages;
    } // addUsage(CharSequence)

    /** Erzeugen eines neuen Sammelauftrags. <br />
     *  <br />
     *  Entspricht
     *   {@link #DTAus(Konto, String, ConstTime) DTAus(myAccount, type, null)}, also
     *  sofortige Ausführung.<br />
     *  <br />
     */
    public DTAus(Konto myAccount, String type) {
        this(myAccount, type, null);
    } // DTAus(Konto, String)

    /** Erzeugen eines neuen Sammelauftrags. <br />
     *  <br />
     *  <code>myAccount</code> ist dabei das &quot;eigene&quot; Konto, welches bei
     *  Sammelüberweisungen als Belastungskonto und bei Sammellastschriften als
     *  Gutschriftkonto verwendet wird.<br/>
     *  <br />
     *  <code>type</code> ist die Art des Sammelauftrags:<ul>
     *  <li> LK, lk, Einzug oder Lastschrift <br />
     *       für Sammellastschrift bzw.</li>
     *  <li> GK, gk oder Überweisung <br />
     *       für Sammelüberweisung.</li></ul>
     *
     *  <code>execdate</code> gibt das Datum an, an dem dieser Sammelauftrag
     *  ausgeführt werden soll. Gegebenenfalls muss es zwischen aktuellen Datum
     *  und 15 Tage später liegen.<br />
     *  <br />
     *  @param myAccount Gegenkonto für die enthaltenen Aufträge
     *  @param type GK für Sammelüberweisungen und LK für Sammellastschriften
     *  @param execdate Ausführungsdatum für diesen
     *               Sammelauftrag; <code>null</code> für sofortige Ausführung.
     *  @throws IllegalArgumentException bei falschem type
     *  @throws NullPointerException ohne Konto
     */
    public DTAus(final Konto myAccount, final String type,
            final ConstTime execdate)
            throws IllegalArgumentException, NullPointerException {
        this.myAccount = myAccount;
        if (myAccount.curr.equals("DEM")) {
            this.curr = CURR_DM;
        }

        if (TYPE_DEBIT.equals(type) || "lk".equals(type)
                || "Einzug".equals(type) || "Lastschrift".equals(type)) {
            this.type = TYPE_DEBIT;
        } else if (TYPE_CREDIT.equals(type) || "gk".equals(type)
                || "Überweisung".equals(type)) {
            this.type = TYPE_CREDIT;
        } else {
            throw new IllegalArgumentException("falscher Typ: " + type);
        }

        this.execdate = execdate;
        entries = new Transaction[200];
        usage = new String[14];
    } // DTAus(Konto, ...

    /** Hinzufügen eines einzelnen Auftrags zu diesem Sammelauftrag.<br />
     *  <br />
     *  Das {@link DTAus.Transaction}-Objekt, welches hier als Argument benötigt
     *  wird, muss mit '<code>dtaus.new&nbsp;Transaction()</code>' erzeugt werden
     *  ('<code>dtaus</code>' ist dabei das aktuelle
     *  <code>DTAUS</code>-Objekt).<br />
     *  <br />
     *  @param entry Hinzuzufügender Einzelauftrag
     */
    public void addEntry(final Transaction entry) {
        entries[anzTrans] = entry;
        ++anzTrans;
    } // addEntry(Transaction)

    @Override
    public String toString() {
        return "DTAus (" + dtaDescr + ") " + anzTrans + " Transaktionen";
    }

    /** Der Sammelauftrag im DTA-Format. <br />
     *  <br />
     *  Der Rückgabewert kann direkt als Sammelauftrag (Datei für Diskette
     *  oder für online banking) verwendet werden. <br />
     *  <br />
     *  Implementierungshinweis: Dies ist (aufgrund des Vorgängervorbilds noch)
     *  die DTA-Darstellung und verletzt damit die toString()-Semantik. (ToDo:
     *  Dies in eine andere Methode tun  und fehlendes echtes toString()
     *  ergänzen).<br />
     *  <br />
     *  @return DTAUS-Datenstrom
     */
    public String toDTAstring() {
        StringBuilder ret = new StringBuilder(890);
        sumBLZ = 0;
        sumNumber = 0;
        sumDM = 0;
        sumEUR = 0;

        // DTA Datensatz A  -----------------------------------------
        ret.append("0128A").append(type);

        ret.append(expand(myAccount.getBlz(), 8, ' ', ALIGN_RIGHT));
        //    12345678
        ret.append("00000000"); // 8 Nullen
        ret.append(expand(check(myAccount.name), 27, ' ', ALIGN_LEFT));

        ConstTime zeit = ComVar.Impl.setActTime(); //  new Time(null);
        ret.append(zeit.toString("dmy")); // 290208 für 29. Februar 2008


        ret.append("    "); // 4 Leerzeichen
        ret.append(expand(myAccount.getNumber(), 10, '0', ALIGN_RIGHT));
        //    1234567890
        ret.append("0000000000"); // 10 Nullen

        //    123456789012345
        ret.append("               "); // 15 Leerzeichen

        if (execdate == null) {
            //    12345678
            ret.append("        "); // 8 Leerzeichen
        } else {
            //// form=new SimpleDateFormat("ddMMyyyy");
            ret.append(execdate.toString("dmY")); // 29072008 f. 29. Juli 2008
        }
        //    123456789.123456789.1234
        ret.append("                        "); // 24 Leerzeichen
        ret.append(curr);

        // C-sets
        for (Transaction entry : entries) {
            if (entry == null) {
                break; // end of list
            }
            ret.append(entry.toDTAstring());
        } //

        // E-set
        ret.append("0128E     "); // Kennung + 5 Leerzeichen
        ret.append(expand(Integer.toString(anzTrans), 7, '0', ALIGN_RIGHT));

        if ("DEM".equals(myAccount.curr)) {
            ret.append(expand(Long.toString(sumDM), 13, '0', ALIGN_RIGHT));
        } else {    // 1234567890123
            ret.append("0000000000000"); // 13 Nullen oder DM-Summe
        }

        ret.append(expand(Long.toString(sumNumber), 17, '0', ALIGN_RIGHT));
        ret.append(expand(Long.toString(sumBLZ), 17, '0', ALIGN_RIGHT));

        if (!"DEM".equals(myAccount.curr)) {
            ret.append(expand(Long.toString(sumEUR), 13, '0', ALIGN_RIGHT));
        } else {    // 1234567890123
            ret.append("0000000000000"); // 13 Nullen oder DM-Summe
        }

        for (int i = 0; i != 51; ++i) {
            ret.append(' '); // 51 Leerzeichen
        }
        return ret.toString();
    } // toDTAstring()
    /** Lookup-Tabelle für den (perversen) DTA-Zeichensatz. <br />
     *  <br />
     *  DTA kennt offiziell nur Großbuchstaben und wenige Sonderzeichen.<br />
     *  ÄÖÜß werden perverserweise als [\]~ dargestellt.<br />
     *  Alles Andere wird in Leerzeichen verwandelt. Alternativ dürfte es als
     *  Fehler zurückgewiesen werden (was hier nicht geschieht).<br />
     *  <br />
     *  {@link #DTAtranslate} vermittelt (im ISO8859-1-Bereich von 0..255) diese
     *  Textverhunzung als Lookup-Tabelle.<br />
     *  charZiel =  {@link #DTAtranslate}.charAt(charQuell); <br />
     *  <br />
     *  So wird dann aus &quot;gemäß Einzusgermächtig. 4.5.&quot; folgende
     *  Scheußlichkeit: &nbsp;&quot;GEM[~ EINZUSGERM[CHTIG. 4.5&quot;. &nbsp;
     *  (Gruß von Cobol und EBCDIC.)<br />
     *  <br />
     *  @see #check(CharSequence)
     */
    public static final String DTAtranslate =
            /// 0123456789x123456789z123456789d1
            /// O123456789ABCDEFO123456789ABCDEF
            "                                " // control -> ' '
            + "    $%&   *+,-./0123456789      " // sonder
            + " ABCDEFGHIJKLMNOPQRSTUVWXYZ     " // groß
            + " ABCDEFGHIJKLMNOPQRSTUVWXYZ     " // klein

            + "                                " // control Hi -> ' '
            + "                                " // sonder Hi -> ' '
            + "    [                 \\     ]  ~" // groß Hi    \\=\
            + "    [                 \\     ]   "; // klein Hi '

    /** Prüfen und Aufbereiten einer Zeichenkette für DTA. <br />
     *  <br />
     *  Unzulässige (s.u.) Zeichen werden zu Leerzeichen.<br />
     *  Führendes und nachlaufendes white space wird (unter Berücksichtigung
     *  dieser Umkodierung) entfernt.<br />
     *  <br />
     *  Kleinbuchstaben werden in Großbuchstaben gewandelt.<br />
     *  Und  &quot;ÄÖÜß&quot; durch &quot;[\]~&quot; ersetzt. ([sic!], DTA,
     *  Cobol-Erbe der Banken). Dies Alles ist DTA-Norm.<br />
     *  <br />
     *  Zulässige Zeichen sind: 0 bis 9, A bis Z, Leerzeichen, Punkt ".",
     *  Komma  ",", Und &amp;, -/+*$% sowie ÄÖÜß.<br />
     *  <br />
     *  @see #DTAtranslate
     *  @throws IllegalArgumentException für Unicode  > ü

     *                       (und damit auch für  > 8 Bit)
     */
    public static String check(CharSequence val)
            throws IllegalArgumentException {
        if (val == null) {
            return EMPTY_STRING;
        }
        int len = val.length();
        if (len == 0) {
            return EMPTY_STRING;
        }

        int start = 0;
        char c = ' ';
        for (; start < len; ++start) { // over leading white space
            c = val.charAt(start);
            if (c > 'ü') {
                throw new IllegalArgumentException(
                        "falscher Zeichenbereich");
            }
            c = DTAtranslate.charAt(c); // translate k->g
            if (c != ' ') {
                break;
            }
            
        }  // over leading white space

        if (c == ' ') {
            return EMPTY_STRING; // only white space
        }
        char lastC = ' ';
        int startInd = start + 1;
        for (; startInd < len; --len) { // over leading white space
            lastC = val.charAt(len - 1);
            if (lastC > 'ü') {
                throw new IllegalArgumentException(
                        "falscher Zeichenbereich");
            }
            lastC = DTAtranslate.charAt(lastC); // translate k->g
            if (lastC != ' ') {
                break;
            }
        }  // over leading white space

        int zielLen = len - start;
        StringBuilder bastel = new StringBuilder(zielLen);
        bastel.append(c);
        --len; // last is translated already
        for (; startInd < len; ++startInd) { // over inner chars
            c = val.charAt(startInd);
            if (c > 'ü') {
                throw new IllegalArgumentException(
                        "falscher Zeichenbereich");
            }
            c = DTAtranslate.charAt(c); // translate k->g, etc.
            bastel.append(c);
        }

        if (lastC != ' ') {
            bastel.append(lastC);
        }
        return bastel.toString();
    } // check(CharSequence)

    /** Begrenzen und Auffüllen einer Zeichenkette auf Länge. <br />
     *  <br />
     *  Die Zeichenkette st wird links- oder rechtsbündig mit dem Zeichen
     *  filler auf die Ziellänge len aufgefüllt (falls sie kürzer ist).<br />
     *  <br />
     *  Ist die Ausgangslänge größer als die Ziellänge, wird oberhalb Ziellänge 15
     *  gekürzt und darunter eine Exception ausgelöst. (DTA-) Hintergrund dieses
     *  Verhaltens ist, dass das Kürzen von Kontonummern, Beträgen oder
     *  Bankleitzahlen (auf die Ziellängen 10 bzw. 8) einen vorher gemachten
     *  Fehler kaschieren würde, während das Kürzen von zu langen Namen und
     *  Verwendungszwecken auf die DTA-Ziellänge 27 i.A. sinnvoll bzw. gewollt
     *  ist.<br />
     *  <br />
     *  @param st    Ausgangszeichenkette
     *  @param len   Ziellänge
     *  @param filler  Füllzeichen, i.A. Leerzeichen (Text) oder 0 (Zahlen)
     *  @param alignLeft true: linksbündig
     *  @return  Die ggf. aufgefüllte oder ggf. gekürzte Zeichenkette.
    @throws  IllegalArgumentException wenn unter Länge 15 gekürzt werden müsste
     */
    public static String expand(final String st, final int len,
            final char filler, final boolean alignLeft)
            throws IllegalArgumentException {
        if (len <= 0) {
            return EMPTY_STRING;
        }
        final int stLen = st == null ? 0 : st.length();
        if (len == stLen) {
            return len == 0 ? EMPTY_STRING : st;
        } // Länge passt
        if (stLen > len) {
            if (len > 15 && st != null) {
                return st.substring(0, len);
            }
            // kurz sind Betrag Kontonummer, BLZ (müssen stimmen!)
            // lang (27 Zeichen) sind Namen, Zweck etc.; werden gekürzt

            throw new IllegalArgumentException("string too long: " + stLen);
        }
        StringBuilder bastel = new StringBuilder(len);
        if (alignLeft && stLen != 0) {
            bastel.append(st);
        }
        for (int i = stLen; i < len; ++i) {
            bastel.append(filler);
        }
        if (!alignLeft && stLen != 0) {
            bastel.append(st);
        }
        return bastel.toString();
    } // expand(String,,,)

    /** DTAus-Objekt aus XML-Beschreibung erzeugen. <br />
     *  <br />
     *  Diese Methode stellt die &quot;factory&quot; für ein
     *  {@link DTAus}-Objekt aus einer XML-Beschreibung dar. <br />
     *  Siehe dazu {@link DTAus Klassenbeschreibung}. <br />
     *  <br />
     *  Diese factory-Methode wird (natürlich) auch von dieser Klasse
     *  {@link DTAus de.a_weinert.dta.DTAus} verwendet, wenn sie als Anwendung
     *  (zum Zweck DTA-Datei aus XML) gestartet / verwendet wird. Diese Methode
     *  kann natürlich auch in anderen Anwendungen eingesetzt werden.<br />
     *  <br />
     *  @param xmlInp  Dateiname XML Beschreibung
     *  @param log     Log-Datei
     *  @param verbose Log ausführlich
     *  @return DTaus-Objekt bei Erfolg
     *  @throws SAXException
     *  @throws ParserConfigurationException
     *  @throws SAXNotSupportedException
     *  @throws SAXNotRecognizedException
     *  @throws IOException
     */
    public static DTAus makeDTAusXML(final String xmlInp,
            final PrintWriter log, final boolean verbose)
            throws SAXNotRecognizedException, SAXNotSupportedException,
            ParserConfigurationException, SAXException, IOException {
        //=== SAXHandler (ano, inner) ======================================
        SAXHandler handler = new SAXHandler("DTAus-Parser", log, true) {

            /// Sax-Parser state machine
            boolean inDTA;
            DTAus dtaus; // gleichzeitig das (End-) Ergebnis der Anstrengungen

            @Override
            public Object getProduct() {
                return dtaus;
            }
            String type;         // DTAus.type
            TimeRO termin;       // DTAus.termin
            Konto myAcount;      // DTaus.myAccount
            boolean inKonto;
            Konto konto;
            boolean inTransact;
            Transaction tr;
            String betrag;   // Transaction.betrag
            /// String transDescr;   // Kommentar für Transaktion (future use,..)
            boolean inZweck;
            String zweck;  // für Alles, Kommentar, nicht für DTA

            @Override
            public void startElement(final String namespaceURI,
                    final String localName, final String qName,
                    final Attributes atts) throws SAXException {

                String tagName = TextHelper.trimUq(localName, qName);

                if ("dta".equals(tagName)) {
                    if (inDTA) {
                        throw new SAXException("multiple start element <dta>");
                    }
                    inDTA = true;
                    type = atts.getValue("type");
                    String time = atts.getValue("termin");
                    if (time != null) {
                        termin = new TimeRO(TimeHelper.parse(time), null);
                    }
                    return;
                } // dta

                if ("konto".equals(tagName)) {
                    if (!inDTA || inKonto || inZweck) {
                        throw new SAXException("misplaced element <konto>");
                    }
                    inKonto = true;
                    String nr = atts.getValue("nr");
                    String blz = atts.getValue("blz");
                    String name = atts.getValue("name");
                    konto = new Konto(blz, nr, name);
                    if (!inTransact) { // nicht in Transaktion ==>  Gegenkonto
                        if (myAcount != null) {
                            throw new SAXException(
                                    "multiple elements <dta><konto>");
                        }
                        myAcount = konto;
                        dtaus = new DTAus(myAcount, type, termin);
                        return;
                    } // nicht in Transaktion ==> dies ist das eine Gegenkonto
                    // in Transaktion ==> dies ist das Zielkonto
                    if (tr != null) {
                        throw new SAXException(
                                "multiple elements <transaction><konto>");
                    }
                    if (dtaus == null) {
                        throw new SAXException(
                                "missing / misplaced <dta><konto>");
                    }
                    tr = dtaus.new Transaction(konto, betrag);
                    return;
                } // dta

                if ("transaction".equals(tagName)) {
                    if (!inDTA || inKonto || inZweck || inTransact) {
                        throw new SAXException("misplaced element <transaction>");
                    }
                    inTransact = true;
                    /// transDescr = null;
                    tr = null;
                    betrag = atts.getValue("betrag");
                    return;
                }

                if ("zweck".equals(tagName)) {
                    if (!inDTA || inKonto || inZweck) {
                        throw new SAXException("misplaced element <zweck>");
                    }
                    inZweck = true;
                    zweck = null;
                    return;
                }


            } // startElement(String,

            @Override
            public void endElement(final String namespaceURI,
                    final String localName, final String qName)
                    throws SAXException {
                String tagName = TextHelper.trimUq(localName, qName);
                if ("zweck".equals(tagName)) {
                    inZweck = false;
                    if (zweck != null) {
                        if (inTransact) {
                            if (tr == null) {
                                throw new SAXException(
                                        "misplaced element <transaction><zweck>");
                            }
                            tr.addUsage(zweck);
                            zweck = null;
                            return;
                        }
                        if (dtaus == null) {
                            throw new SAXException(
                                    "misplaced element <dta><zweck>");
                        }
                        dtaus.addUsage(zweck);
                        zweck = null;
                    }
                    return;
                } // zweck

                if ("transaction".equals(tagName)) {
                    inTransact = false;
                    if (tr == null) {
                        throw new SAXException(
                                "incomplete <transaction> .. </transaction>");
                    }
                    if (tr.anzUsages == 0) {
                        throw new SAXException(
                                "Transaktion ohne jeden Zweck / Betreff");
                    }
                    if (dtaus == null) {
                        throw new SAXException(
                                "misplaced element <dta><transaction>");
                    }
                    dtaus.addEntry(tr);
                    return;
                } //   transaction

                if ("konto".equals(tagName)) {
                    inKonto = false;
                    return;
                }

                if ("dta".equals(tagName)) {
                    if (dtaus == null || dtaus.anzTrans == 0) {
                        throw new SAXException(
                                "incomplete / empty definition of <dta> ... </dta>");
                    }
                    return;
                }

            } // endElement(String,,,

            @Override
            public void characters(final char[] ch, final int start,
                    final int length) throws SAXException {
                String tmp =
                        TextHelper.trimUq(new String(ch, start, length), null);
                if (tmp == null) {
                    return;
                }

                if (inZweck) {
                    if (zweck == null) {
                        zweck = tmp;
                    } else {
                        zweck = zweck + ' ' + tmp;
                    }
                    return;
                }
                if (inKonto) {
                    if (konto != null) {
                        konto.descr = tmp;
                    }
                    return;
                }
                if (inTransact) {
                    if (tr != null) {
                        tr.descr = tmp;
                    }
                    return;
                }
                if (inDTA) {
                    if (dtaus != null) {
                        dtaus.dtaDescr = tmp;
                    }
                    return;
                }
            } // characters(char[],
        }; // new SAXHandler ==== (ano, inner) ===============================

        String fileNam = TextHelper.trimUq(xmlInp, null);
        if (fileNam == null) {
            return null;
        }
        InputStream xin = new FileInputStream(fileNam);


        XMLconf xmlConf = new XMLconf(); /// XML-Parser- etc. Konfiguration
        xmlConf.simplifyInput();

        XMLReader xmlReader = xmlConf.makeXMLReader(handler);
        InputSource ips = new InputSource(xin);
        xmlReader.parse(ips);
        handler.reportErrors();

        return (DTAus) handler.getProduct();

    } // makeDTAusXML(String,,,)
// ----     de.a_weinert.dta.DTAus als Anwendung      ----------------------
    /** Der Standard-Dateiname für DTA-Austausch-Dateien. <br />
     *  <br />
     *  Dies ist DTA-Norm. Im Falle einer Diskette beispielsweise muss auf dieser
     *  genau eine Datei genau dieses Namens sein. Online-banking-Anwendungen
     *  akzeptieren hingegen i.A. den upload beliebig benannter
     *  DTAus-Dateien.<br />
     *  <br />
     *  Wert: DTAUS0.TXT<br />
     */
    public static final String DTA_OUT = "DTAUS0.TXT";

    /** Start der Anwendung (App-Erbe). <br />
     *  <br />
     *  Aufruf: java de.a_weinert.dta.DTAus [optionen] xml-Datei [dta-Datei]<br />
     *  <br />
     *  @see #makeDTAusXML(String, PrintWriter, boolean)
     *  @see App
     */
    public static void main(final String[] argv) { // simple Test
        new App(argv) { // ano inner of de.frame4j.util.App

            @Override
            public int doIt() {
                log.println();
                log.println(twoLineStartMsg().append('\n'));
                String dtaOut = null;
                String xmlInp = null;

                for (int i = 0; i < args.length; ++i) { // search 2 unused params
                    final String akt = args[i];
                    if (akt == null) {
                        continue;
                    }
                    if (xmlInp == null) {
                        xmlInp = akt;
                        continue;
                    }
                    dtaOut = akt;
                    break;
                } // search 1st two unused parameters

                xmlInp = TextHelper.trimUq(xmlInp, null);
                dtaOut = TextHelper.trimUq(dtaOut, DTA_OUT);

                if (xmlInp == null) {
                    return errMeld(15, "Keine Angabe zur Eingabe (XML-Datei)");
                }
                log.println("Eingabe: " + xmlInp + "; Ausgabe : " + dtaOut);

                DTAus dtaus = null;
                try {
                    dtaus = makeDTAusXML(xmlInp, log, verbose);
                } catch (Exception ex) {
                    return errorExit(19, ex, null);
                }
                if (dtaus == null) {
                    return errMeld(19, "Keine DTAus erzeugt");
                }

                if (dtaOut == null || verbose) {
                    log.println("\n \n ----- DTAus-Datenstrom:\n");
                    log.println(dtaus);
                    log.flush();
                    if (dtaOut == null) {
                        return 0;
                    }
                }

                DataFile outDat = new DataFile(dtaOut);
                try {
                    DataFile.OS os = outDat.new OS(false, null);
                    os.pw.print(dtaus.toDTAstring());
                    os.close();
                } catch (IOException ex) {
                    return errorExit(27, ex, null);
                }
                return 0;
            }
        };

    } // main()
} // class DTAus (19.05.2008, 20.08.2008)
