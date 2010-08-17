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

import java.io.Serializable;

import de.frame4j.util.TextHelper;

/** <b>Kontoverbindung</b>. <br />
 *  <br />
 *  <br />
 *  Although being part of Frame4J, this class's documentation is
 *  intentionally in German language. See
 *  <a href="./package-summary.html#pbg">this hint</a>, please.<br />
 *  <br />
 *  Objects of this class represent one customer's banking account.<br />
 *  <br />
 *  Diese Klasse reprsentiert eine einzelne Kontoverbindung eines Kunden
 *  bei seiner Bank.<br />
 *  <br />
 *  <br />
 *  Copyright 2008    Albrecht Weinert
 *  @author   Albrecht Weinert
 *  @version  $Revision$ ($Date$)
 *  @see      DTAus
 *  @see      de.a_weinert.dta
 */
// Bisher    V00.00 (19.05.2008 11:37) :  neu
//           V01.02 (21.05.2008 09:47) :  /**
//           V.13   (22.11.2008 16.32) : cvs -> SVN; Fehl. korr.
public final class Konto implements Serializable {

    /** Lnderkennzeichen des Kontos. <br />
     *  <br />
     *  default: DE (Deutschland) <br />
     */
    public String country = "DE";
    private String blz;  // nderbar aber nie null
    private int blzNum;

    /** Bankleitzahl des Kontos. <br />
     *  <br />
     *  8 Ziffern ohne Leerzeichen. <br />
     */
    public final String getBlz() {
        return this.blz;
    }

    /** Bankleitzahl des Kontos. <br />
     *  <br />
     *  8-stellig 10000000 .. 99999999 <br />
     */
    public final int getBlzNum() {
        return this.blzNum;
    }

    /** Bankleitzahl des Kontos setzen. <br />
     *  <br />
     *  8-stellig 10000000 .. 99999999 <br />
     */
    public void setBLZ(final int blz) throws IllegalArgumentException {
        if (blz < 10000000 || blz > 99999999) {
            throw new IllegalArgumentException("BLZ nicht im erlaubten Bereich");
        }
        this.blzNum = blz;
        this.blz = String.valueOf(blz);
    } /// setBLZ(int)

    /** Bankleitzahl des Kontos setzen. <br />
     *  <br />
     *  Muss eine 8-stellige Zahl 10000000 .. 99999999 ergeben. <br />
     *  Erlaubte Formate (nach Entfernung umfassenden white spaces: <br />
     *  &quot;123 456 78&quot; oder &quot;87654321&quot;<br />
     *  <br />
     */
    public void setBLZ(final CharSequence blz)
            throws IllegalArgumentException {
        String tmp = TextHelper.trimUq(blz, null);
        if (tmp == null) {
            throw new IllegalArgumentException("BLZ ist leer");
        }
        final int len = tmp.length();
        boolean legal = true;
        if (len == 10) {
            legal = tmp.charAt(3) == ' ' && tmp.charAt(7) == ' ';
        } else {
            legal = len == 8;
        }

        int blzNum = 0;
        for (int i = 0; legal && i < len; ++i) {
            char akt = tmp.charAt(i);
            if (akt == ' ') {
                continue;
            }
            if (akt < '0' || akt > '9') {
                legal = false;
            } else {
                blzNum = blzNum * 10 + (akt - '0');
            }
        } // for
        if (!legal) {
            throw new IllegalArgumentException(
                    "BLZ nicht 12345678 oder 123 456 78");
        }
        setBLZ(blzNum);
    } // setBLZ(CharSequence)
    /** Kontonummer. <br /> */
    private String number;
    /** Kontonummer als Zahl. <br />
     *  <br />
     *  max. 10-stellig.
     */
    private long numb;

    /** Kontonummer als String. <br />
     *  <br />
     *  dicht, max. 10-stellig.
     */
    public final String getNumber() {
        return this.number;
    }

    /** Kontonummer als Zahl. <br />
     *  <br />
     *  max. 10-stellig; > 9.<br /
     */
    public final long getNumb() {
        return this.numb;
    }

    /** Kontonummer als Zahl setzen. <br />
     *  <br />
     *  max. 10-stellig; > 9.<br /
     */
    public void setNumb(long numb) throws IllegalArgumentException {
        if (numb < 9 || numb > 9999999999L) {
            throw new IllegalArgumentException("illegale Kontonummer");
        }
        this.numb = numb;
        this.number = Long.toString(numb);
    } //  setNumb(long)

    /** Kontonummer als Zeichenkette setzen. <br />
     *  <br />
     *  Format: muss max 10-stellige Zahl &gt; 9 ergeben.<br />
     *  Umfassendes white space und innen liegende Leerzeichen werden
     *  ignoriert.<br />
     *  <br />
     */
    public void setNumber(CharSequence number)
            throws IllegalArgumentException {
        String tmp = TextHelper.trimUq(number, null);
        if (tmp == null || tmp.length() < 2) {
            throw new IllegalArgumentException("leere Kontonummer");
        }

        long accNu = tmp.charAt(0) - '0';
        boolean legal = accNu >= 0 && accNu <= 9;
        for (int i = 1; legal && i < tmp.length(); ++i) {
            char akt = tmp.charAt(i);
            if (akt == ' ') {
                continue;
            }
            if (akt < '0' || akt > '9') {
                legal = false;
                break;
            }
            accNu = accNu * 10 + (akt - '0');
        }
        if (!legal) {
            throw new IllegalArgumentException(
                    "illegale Zeichen in Kontonummer: " + number);
        }
        setNumb(accNu);
    }   // setNumber(CharSequence)
    /** Unterkontomerkmal des Kontos. <br />
     *  <br />
     *  default und i.A.: null <br />
     */
    public String subnumber;
    /** Whrung des Kontos. <br />
     *  <br />
     *  default: <code>EUR</code> fr Euro. <br />
     */
    public String curr = "EUR";
    /** Kunden-ID fr HBCI. <br />
     *  <br />
     *  Dieser Wert gibt an, unter welcher Kunden-ID ein Bankkunde HBCI-Zugriff
     *  auf dieses Konto hat. Ohne HBCI-Zugriff ist und bleibt dieser Wert
     *  null.<br />
     *  <br />
     *  Wert, default: null (nur fr zuknftige Entwicklung)
     */
    public String customerid;
    /** Name des Kontoinhabers. <br />
     *  <br />
     *  Dies ist der bei der jeweiligen Bank registrierte Inhabername.<br />
     *  Sollte so stimmen, da Banken dies (prinzipiell) kontrollieren.<br />
     *  <br />
     */
    public String name;
    /** Name des Kontoinhabers (Fortsetzung) (optional). <br />
     *  <br />
     *  default: null
     */
    public String name2;
    /** Beschreibung des Kontos (optional). <br />
     *  <br />
     *  Ohne Bedeutung fr DTA.<br />
     *  default: null
     */
    public String descr;

    /** Anlegen eines neuen Konto-Objekts. <br />
     *  <br />
     *  Whrungseinstellung ist Euro.<br />
     *  <br />
     */
    public Konto(String blz, String number, CharSequence name) {
        setBLZ(blz);
        setNumber(number);
        this.name = TextHelper.trimUq(name, "Kein gueltiges Konto");
    } // Konto(String, String, CharSequence)

    /** Darstellung als Zeichenkette. <br />
     *  <br />
     */
    @Override
    public String toString() {
        return name + ":" + number
                + (subnumber != null ? " (" + subnumber + ")" : "")
                + " BLZ " + blz
                + " [" + country + "]" + curr
                + (descr != null ? " \"" + descr + '\"' : ' ');
    } //  toString()

    /** Vergleich mit anderem Konto. <br />
     *  <br />
     *  Der Vergleich erfasst alle Eigenschaften und damit mehr als die minimal
     *  erforderlichen BLZ und Nummer.<br />
     */
    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Konto)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        Konto acc = (Konto) o;
        if (this.blzNum != acc.blzNum) {
            return false;
        }
        if (this.numb != acc.numb) {
            return false;
        }
        // bis hierher msste reichen BLZ + Konto !!!

        if (this.curr == null ? acc.curr != null : !this.curr.equals(acc.curr)) {
            if (this.curr == null || !this.curr.equals(acc.curr)) {
                return false;
            }
        }
        if (this.country == null ? acc.country != null : !this.country.equals(acc.country)) {
            if (this.country == null
                    || !this.country.equals(acc.country)) {
                return false;
            }
        }
        if (this.name == null ? acc.name != null : !this.name.equals(acc.name)) {
            if (this.name == null || !this.name.equals(acc.name)) {
                return false;
            }
        }
        if (this.name2 == null ? acc.name2 != null : !this.name2.equals(acc.name2)) {
            if (this.name2 == null
                    || !this.name2.equals(acc.name2)) {
                return false;
            }
        }
        if (this.customerid == null ? acc.customerid != null : !this.customerid.equals(acc.customerid)) {
            if (this.customerid == null
                    || !this.customerid.equals(acc.customerid)) {
                return false;
            }
        }
        return true;
    } // equals(Object)

    /** Hash code. <br />
     *  <br />
     *  Funktion von Bankleitzahl und Kontonummer.<br />
     */
    @Override
    public final int hashCode() {
        return (int) numb ^ (int) (numb >> 16) ^ blzNum;
    } // hashCode()
} // class Konto (21.05.2008, 22.11.2008)
