/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openyabs.datev;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import mpv5.db.objects.Contact;

/**
 * Formatter for DATEV-Format "Debitoren/Kreditoren" master data
 * (EXTF, Header version 700, Format category 16, Format version 5)
 * according to the official DATEV Developer Portal specification.
 * <p>
 * Rules:
 * <ul>
 * <li>Encoding: Windows-1252 ({@link DatevFormat#CHARSET})</li>
 * <li>Line separator: CRLF ({@code \r\n})</li>
 * <li>Field separator: semicolon ({@code ;})</li>
 * <li>Line 1: EXTF header with 31 fields (category 16, format version 5)</li>
 * <li>Line 2: 51 column headers covering address, EU VAT ID, communication, and Bank 1</li>
 * <li>Konto length: {@code Sachkontenlänge + 1} digits (e.g., 5 digits: 10000–69999 Debitoren, 70000–99999 Kreditoren)</li>
 * <li>Column 46 ({@code Leerfeld}) is kept strictly empty between {@code IBAN-Nr. 1} (#45) and {@code SWIFT-Code 1} (#47)</li>
 * </ul>
 */
public final class DatevMasterDataFormat {

    public static final int HEADER_VERSION = 700;
    public static final int FORMAT_CATEGORY = 16;
    public static final String FORMAT_NAME = "Debitoren/Kreditoren";
    public static final int FORMAT_VERSION = 5;
    public static final int HEADER_FIELD_COUNT = 31;
    public static final int COLUMN_COUNT = 51;

    private static final Set<String> EU_COUNTRY_PREFIXES = new HashSet<String>(Arrays.asList(
            "AT", "BE", "BG", "CY", "CZ", "DE", "DK", "EE", "EL", "ES", "FI", "FR", "HR", "HU",
            "IE", "IT", "LT", "LU", "LV", "MT", "NL", "PL", "PT", "RO", "SE", "SI", "SK", "XI"));

    /**
     * First 51 official DATEV Debitoren/Kreditoren (Formatversion 5) column headers.
     */
    public static final String[] COLUMNS = {
        "Konto",                                // 1
        "Name (Adressattyp Unternehmen)",       // 2
        "Unternehmensgegenstand",               // 3
        "Name (Adressattyp natürl. Person)",    // 4
        "Vorname (Adressattyp natürl. Person)", // 5
        "Name (Adressattyp keine Angabe)",      // 6
        "Adressattyp",                          // 7  ("0", "1", "2")
        "Kurzbezeichnung",                      // 8
        "EU-Land",                              // 9
        "EU-UStID",                             // 10
        "Anrede",                               // 11
        "Titel / Akad. Grad",                   // 12
        "Adelstitel",                           // 13
        "Namensvorsatz",                        // 14
        "Adressart",                            // 15 ("STR" / "PF")
        "Straße",                               // 16
        "Postfach",                             // 17
        "Postleitzahl",                         // 18
        "Ort",                                  // 19
        "Land",                                 // 20
        "Versandzusatz",                        // 21
        "Adresszusatz",                         // 22
        "Abweichende Anrede",                   // 23
        "Abw. Zustellbezeichnung 1",            // 24
        "Abw. Zustellbezeichnung 2",            // 25
        "Kennz. Korrespondenzadresse",          // 26
        "Adresse Gültig von",                   // 27
        "Adresse Gültig bis",                   // 28
        "Telefon",                              // 29
        "Bemerkung (Telefon)",                  // 30
        "Telefon GL",                           // 31
        "Bemerkung (Telefon GL)",               // 32
        "E-Mail",                               // 33
        "Bemerkung (E-Mail)",                   // 34
        "Internet",                             // 35
        "Bemerkung (Internet)",                 // 36
        "Fax",                                  // 37
        "Bemerkung (Fax)",                      // 38
        "Sonstige",                             // 39
        "Bemerkung (Sonstige)",                 // 40
        "Bankleitzahl 1",                       // 41
        "Bankbezeichnung 1",                    // 42
        "Bankkonto-Nummer 1",                   // 43
        "Länderkennzeichen 1",                  // 44
        "IBAN-Nr. 1",                           // 45
        "Leerfeld",                             // 46 (mandatory empty field between IBAN and SWIFT/BIC)
        "SWIFT-Code 1",                         // 47
        "Abw. Kontoinhaber 1",                  // 48
        "Kennz. Hauptbankverb. 1",              // 49
        "Bankverb. 1 Gültig von",               // 50
        "Bankverb. 1 Gültig bis"                // 51
    };

    private DatevMasterDataFormat() {
    }

    /**
     * One debtor (Debitor) or creditor (Kreditor) master data record.
     */
    public static final class MasterRecord {

        /**
         * Personenkonto (length = Sachkontenlänge + 1, e.g., 10000-69999 Debitoren, 70000-99999 Kreditoren).
         */
        public String konto = "";
        /**
         * Adressattyp: "1" = natürliche Person, "2" = Unternehmen, "0" = keine Angabe.
         */
        public String adressattyp = "2";
        public String companyName = "";
        public String lastName = "";
        public String firstName = "";
        public String shortName = "";
        public String euCountry = "";
        public String euVatNumber = "";
        public String salutation = "";
        public String title = "";
        public String street = "";
        public String zip = "";
        public String city = "";
        public String country = "";
        public String phone = "";
        public String email = "";
        public String website = "";
        public String fax = "";
        public String bankName = "";
        public String bankCountry = "";
        public String iban = "";
        public String bic = "";
    }

    /**
     * Standard DATEV master data file name: {@code EXTF_Stammdaten_Debitoren_Kreditoren_<YYYYMMDD>.csv}
     */
    public static String fileName(Date createdAt) {
        Date ts = createdAt == null ? new Date() : createdAt;
        return "EXTF_Stammdaten_Debitoren_Kreditoren_" + DatevFormat.yyyymmdd(ts) + ".csv";
    }

    /**
     * Renders header line 1, column header line 2, and all master records with CRLF line endings.
     */
    public static String render(DatevConfig config, Date createdAt, List<MasterRecord> records) {
        StringBuilder sb = new StringBuilder();
        sb.append(headerLine(config, createdAt)).append(DatevFormat.CRLF);
        sb.append(columnHeaderLine()).append(DatevFormat.CRLF);
        if (records != null) {
            for (MasterRecord r : records) {
                if (r != null) {
                    sb.append(recordLine(r)).append(DatevFormat.CRLF);
                }
            }
        }
        return sb.toString();
    }

    public static byte[] encode(String csv) {
        return DatevFormat.encode(csv);
    }

    /**
     * Builds line 1 (EXTF header, 31 fields, Category 16, Formatversion 5).
     */
    public static String headerLine(DatevConfig config, Date createdAt) {
        String[] h = new String[HEADER_FIELD_COUNT];
        Date ts = createdAt == null ? new Date() : createdAt;
        String wj = config.wjBeginn == null ? "" : config.wjBeginn.trim();
        if (wj.isEmpty()) {
            wj = new SimpleDateFormat("yyyy").format(ts) + "0101";
        }
        String skr = config.kontenrahmen == null ? "" : config.kontenrahmen.trim();
        String herkunft = DatevFormat.sanitizeHerkunft(config.herkunft);
        String exportiertVon = DatevFormat.sanitizeText(config.exportiertVon, 25);
        String appInfo = DatevFormat.sanitizeText(config.anwendungsinformation, 16);

        h[0] = DatevFormat.quote("EXTF");                           // 1  Kennzeichen
        h[1] = String.valueOf(HEADER_VERSION);                      // 2  Versionsnummer (700)
        h[2] = String.valueOf(FORMAT_CATEGORY);                     // 3  Formatkategorie (16)
        h[3] = DatevFormat.quote(FORMAT_NAME);                      // 4  Formatname ("Debitoren/Kreditoren")
        h[4] = String.valueOf(FORMAT_VERSION);                      // 5  Formatversion (5)
        h[5] = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(ts);// 6  Erzeugt am
        h[6] = "";                                                  // 7  Importiert
        h[7] = DatevFormat.quote(herkunft);                         // 8  Herkunft
        h[8] = DatevFormat.quote(exportiertVon);                    // 9  Exportiert von
        h[9] = DatevFormat.quote("");                               // 10 Importiert von
        h[10] = config.beraterNummer > 0 ? String.valueOf(config.beraterNummer) : "";   // 11 Beraternummer
        h[11] = config.mandantenNummer > 0 ? String.valueOf(config.mandantenNummer) : ""; // 12 Mandantennummer
        h[12] = wj;                                                 // 13 WJ-Beginn
        h[13] = String.valueOf(Math.max(4, Math.min(8, config.sachkontenLaenge))); // 14 Sachkontenlänge
        h[14] = "";                                                 // 15 Datum von (unused for master data)
        h[15] = "";                                                 // 16 Datum bis (unused for master data)
        h[16] = DatevFormat.quote("");                              // 17 Bezeichnung
        h[17] = DatevFormat.quote("");                              // 18 Diktatkürzel
        h[18] = "";                                                 // 19 Buchungstyp
        h[19] = "";                                                 // 20 Rechnungslegungszweck
        h[20] = "";                                                 // 21 Festschreibung
        h[21] = DatevFormat.quote("");                              // 22 WKZ
        h[22] = "";                                                 // 23 Reserviert
        h[23] = DatevFormat.quote("");                              // 24 Derivatskennzeichen
        h[24] = "";                                                 // 25 Reserviert
        h[25] = "";                                                 // 26 Reserviert
        h[26] = DatevFormat.quote(skr);                             // 27 Sachkontenrahmen
        h[27] = "";                                                 // 28 ID der Branchenlösung
        h[28] = "";                                                 // 29 Reserviert
        h[29] = DatevFormat.quote("");                              // 30 Reserviert
        h[30] = DatevFormat.quote(appInfo);                         // 31 Anwendungsinformation
        return String.join(";", h);
    }

    public static String columnHeaderLine() {
        return String.join(";", COLUMNS);
    }

    /**
     * Builds one data line with 51 semicolon-separated fields.
     */
    public static String recordLine(MasterRecord r) {
        String[] f = new String[COLUMN_COUNT];
        for (int i = 0; i < f.length; i++) {
            f[i] = "";
        }
        String type = "1".equals(r.adressattyp) ? "1" : "2".equals(r.adressattyp) ? "2" : "0";
        f[0] = DatevFormat.digitsOnly(r.konto);                     // 1  Konto (unquoted)
        if ("2".equals(type)) {
            f[1] = DatevFormat.quote(DatevFormat.sanitizeText(r.companyName, 50)); // 2 Name Unternehmen
        } else if ("1".equals(type)) {
            f[3] = DatevFormat.quote(DatevFormat.sanitizeText(r.lastName, 30));    // 4 Name nat. Person
            if (!DatevConfig.isBlank(r.firstName)) {
                f[4] = DatevFormat.quote(DatevFormat.sanitizeText(r.firstName, 30)); // 5 Vorname nat. Person
            }
        } else {
            String fallback = !DatevConfig.isBlank(r.companyName) ? r.companyName : r.lastName;
            f[5] = DatevFormat.quote(DatevFormat.sanitizeText(fallback, 50));      // 6 Name keine Angabe
        }
        f[6] = DatevFormat.quote(type);                             // 7  Adressattyp ("1"/"2"/"0")
        if (!DatevConfig.isBlank(r.shortName)) {
            f[7] = DatevFormat.quote(DatevFormat.sanitizeText(r.shortName, 15));   // 8 Kurzbezeichnung
        }
        if (!DatevConfig.isBlank(r.euCountry) && !DatevConfig.isBlank(r.euVatNumber)) {
            f[8] = DatevFormat.quote(r.euCountry.trim().toUpperCase(Locale.ROOT)); // 9 EU-Land (2 chars)
            f[9] = DatevFormat.quote(DatevFormat.sanitizeText(r.euVatNumber, 13)); // 10 EU-UStID (without prefix)
        }
        if (!DatevConfig.isBlank(r.salutation)) {
            f[10] = DatevFormat.quote(DatevFormat.sanitizeText(r.salutation, 30)); // 11 Anrede
        }
        if (!DatevConfig.isBlank(r.title)) {
            f[11] = DatevFormat.quote(DatevFormat.sanitizeText(r.title, 25));      // 12 Titel
        }
        boolean hasAddress = !DatevConfig.isBlank(r.street) || !DatevConfig.isBlank(r.zip) || !DatevConfig.isBlank(r.city);
        if (hasAddress) {
            f[14] = DatevFormat.quote("STR");                       // 15 Adressart
        }
        if (!DatevConfig.isBlank(r.street)) {
            f[15] = DatevFormat.quote(DatevFormat.sanitizeText(r.street, 36));     // 16 Straße
        }
        if (!DatevConfig.isBlank(r.zip)) {
            f[17] = DatevFormat.quote(DatevFormat.sanitizeText(r.zip, 10));        // 18 Postleitzahl
        }
        if (!DatevConfig.isBlank(r.city)) {
            f[18] = DatevFormat.quote(DatevFormat.sanitizeText(r.city, 30));       // 19 Ort
        }
        if (!DatevConfig.isBlank(r.country)) {
            String iso = r.country.trim().toUpperCase(Locale.ROOT);
            if (iso.length() == 2) {
                f[19] = DatevFormat.quote(iso);                     // 20 Land (ISO-2)
            }
        }
        if (hasAddress) {
            f[25] = "1";                                            // 26 Kennz. Korrespondenzadresse
        }
        if (!DatevConfig.isBlank(r.phone)) {
            f[28] = DatevFormat.quote(DatevFormat.sanitizeText(r.phone, 60));      // 29 Telefon
        }
        if (!DatevConfig.isBlank(r.email)) {
            f[32] = DatevFormat.quote(DatevFormat.sanitizeText(r.email, 60));      // 33 E-Mail
        }
        if (!DatevConfig.isBlank(r.website)) {
            f[34] = DatevFormat.quote(DatevFormat.sanitizeText(r.website, 60));    // 35 Internet
        }
        if (!DatevConfig.isBlank(r.fax)) {
            f[36] = DatevFormat.quote(DatevFormat.sanitizeText(r.fax, 60));        // 37 Fax
        }
        String cleanIban = r.iban == null ? "" : r.iban.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        String cleanBic = r.bic == null ? "" : r.bic.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (!DatevConfig.isBlank(r.bankName)) {
            f[41] = DatevFormat.quote(DatevFormat.sanitizeText(r.bankName, 30));   // 42 Bankbezeichnung 1
        }
        if (!cleanIban.isEmpty()) {
            String bankCountry = !DatevConfig.isBlank(r.bankCountry) && r.bankCountry.trim().length() == 2
                    ? r.bankCountry.trim().toUpperCase(Locale.ROOT)
                    : (cleanIban.length() >= 2 && Character.isLetter(cleanIban.charAt(0)) ? cleanIban.substring(0, 2) : "");
            if (!bankCountry.isEmpty()) {
                f[43] = DatevFormat.quote(bankCountry);             // 44 Länderkennzeichen 1
            }
            f[44] = DatevFormat.quote(cleanIban.length() > 34 ? cleanIban.substring(0, 34) : cleanIban); // 45 IBAN-Nr. 1
            // f[45] = "";                                          // 46 Leerfeld (MUST remain empty!)
            if (!cleanBic.isEmpty()) {
                f[46] = DatevFormat.quote(cleanBic.length() > 11 ? cleanBic.substring(0, 11) : cleanBic); // 47 SWIFT-Code 1
            }
            f[48] = "1";                                            // 49 Kennz. Hauptbankverb. 1
        } else if (!cleanBic.isEmpty()) {
            f[46] = DatevFormat.quote(cleanBic.length() > 11 ? cleanBic.substring(0, 11) : cleanBic); // 47 SWIFT-Code 1
        }
        return String.join(";", f);
    }

    /**
     * Maps a list of YaBS {@link Contact}s into DATEV debtor/creditor {@link MasterRecord}s,
     * deriving valid {@code Sachkontenlänge + 1}-digit Personenkonto numbers (10000–69999 for
     * Debitoren, 70000–99999 for Kreditoren when SKL = 4).
     */
    public static List<MasterRecord> fromContacts(List<Contact> contacts, DatevConfig config) {
        List<MasterRecord> result = new ArrayList<MasterRecord>();
        if (contacts == null) {
            return result;
        }
        int skl = config == null ? 4 : Math.max(4, Math.min(8, config.sachkontenLaenge));
        long mult = 1L;
        for (int i = 0; i < skl; i++) {
            mult *= 10L;
        }
        long debitorBase = mult;       // e.g. 10000 for SKL=4
        long kreditorBase = 7L * mult; // e.g. 70000 for SKL=4
        Set<String> usedAccounts = new HashSet<String>();
        long nextDebitor = debitorBase + 1;
        long nextKreditor = kreditorBase + 1;

        for (Contact c : contacts) {
            if (c == null) {
                continue;
            }
            boolean isCreditor = c.__getIssupplier() && !c.__getIscustomer();
            String konto = derivePersonenkonto(c, isCreditor, skl, debitorBase, kreditorBase, usedAccounts);
            if (konto == null) {
                if (isCreditor) {
                    while (usedAccounts.contains(String.valueOf(nextKreditor))) {
                        nextKreditor++;
                    }
                    konto = String.valueOf(nextKreditor++);
                } else {
                    while (usedAccounts.contains(String.valueOf(nextDebitor))) {
                        nextDebitor++;
                    }
                    konto = String.valueOf(nextDebitor++);
                }
            }
            usedAccounts.add(konto);
            result.add(fromContact(c, konto));
        }
        return result;
    }

    /**
     * Maps a single YaBS {@link Contact} with a caller-supplied DATEV Personenkonto.
     */
    public static MasterRecord fromContact(Contact c, String personenkonto) {
        MasterRecord r = new MasterRecord();
        r.konto = personenkonto == null ? "" : DatevFormat.digitsOnly(personenkonto);
        boolean isCompany = c.__getisCompany() || !DatevConfig.isBlank(c.__getCompany());
        if (isCompany) {
            r.adressattyp = "2";
            r.companyName = !DatevConfig.isBlank(c.__getCompany()) ? c.__getCompany().trim() : nvl(c.__getCname());
            r.shortName = r.companyName;
        } else {
            r.adressattyp = "1";
            r.lastName = nvl(c.__getCname());
            r.firstName = nvl(c.__getPrename());
            r.shortName = (r.lastName + (r.firstName.isEmpty() ? "" : ", " + r.firstName)).trim();
            r.salutation = c.__getisMale() ? "Herr" : "Frau";
            r.title = nvl(c.__getTitle());
        }
        String vat = DatevFormat.sanitizeEuVatId(c.__getTaxnumber());
        if (vat.length() >= 4) {
            String prefix = vat.substring(0, 2);
            if (EU_COUNTRY_PREFIXES.contains(prefix)) {
                r.euCountry = prefix;
                r.euVatNumber = vat.substring(2);
            }
        }
        r.street = nvl(c.__getStreet());
        r.zip = nvl(c.__getZip());
        r.city = nvl(c.__getCity());
        r.country = normalizeCountry(c.__getCountry());
        r.phone = !DatevConfig.isBlank(c.__getMainphone()) ? c.__getMainphone().trim() : nvl(c.__getWorkphone());
        r.email = nvl(c.__getMailaddress());
        r.website = nvl(c.__getWebsite());
        r.fax = nvl(c.__getFax());
        r.bankName = nvl(c.__getBankname());
        r.bankCountry = normalizeCountry(c.__getBankcountry());
        r.iban = nvl(c.__getBankaccount());
        r.bic = nvl(c.__getBankid());
        return r;
    }

    private static String derivePersonenkonto(Contact c, boolean isCreditor, int skl,
            long debitorBase, long kreditorBase, Set<String> used) {
        long min = isCreditor ? kreditorBase : debitorBase;
        long max = isCreditor ? (10L * debitorBase - 1L) : (kreditorBase - 1L);
        String digits = DatevFormat.digitsOnly(c.__getCNumber());
        if (digits.length() == skl + 1) {
            try {
                long v = Long.parseLong(digits);
                if (v >= min && v <= max && !used.contains(digits)) {
                    return digits;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        int id = c.__getIDS();
        if (id > 0) {
            long candidate = min + id;
            if (candidate <= max && !used.contains(String.valueOf(candidate))) {
                return String.valueOf(candidate);
            }
        }
        return null;
    }

    private static String normalizeCountry(String country) {
        if (DatevConfig.isBlank(country)) {
            return "";
        }
        String s = country.trim().toUpperCase(Locale.ROOT);
        if (s.length() == 2 && Character.isLetter(s.charAt(0)) && Character.isLetter(s.charAt(1))) {
            return s;
        }
        if (s.startsWith("DEUTSCHLAND") || s.equals("GERMANY")) {
            return "DE";
        }
        if (s.startsWith("ÖSTERREICH") || s.startsWith("OESTERREICH") || s.equals("AUSTRIA")) {
            return "AT";
        }
        if (s.startsWith("SCHWEIZ") || s.equals("SWITZERLAND")) {
            return "CH";
        }
        if (s.startsWith("FRANKREICH") || s.equals("FRANCE")) {
            return "FR";
        }
        return "";
    }

    private static String nvl(String s) {
        return s == null || "null".equals(s) ? "" : s.trim();
    }
}
