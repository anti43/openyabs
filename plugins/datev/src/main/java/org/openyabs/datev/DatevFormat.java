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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Formatter for DATEV-Format "Buchungsstapel" (EXTF, Header version 700,
 * Format category 21, Format version 13) according to the DATEV Developer
 * Portal specification (developer.datev.de).
 * <p>
 * Rules:
 * <ul>
 * <li>Encoding: Windows-1252 ({@link #CHARSET})</li>
 * <li>Line separator: CRLF ({@code \r\n})</li>
 * <li>Field separator: semicolon ({@code ;})</li>
 * <li>Line 1: EXTF header with 31 fields</li>
 * <li>Line 2: 125 column headers of Buchungsstapel format version 13</li>
 * <li>Lines 3..n: booking lines with 125 fields each</li>
 * <li>Text fields enclosed in double quotes; internal quotes doubled; no control characters</li>
 * <li>Amounts always positive with decimal comma and no thousands separator</li>
 * </ul>
 */
public final class DatevFormat {

    public static final Charset CHARSET = Charset.forName("windows-1252");
    public static final String CRLF = "\r\n";
    public static final int HEADER_VERSION = 700;
    public static final int FORMAT_CATEGORY = 21;
    public static final String FORMAT_NAME = "Buchungsstapel";
    public static final int FORMAT_VERSION = 13;
    public static final int HEADER_FIELD_COUNT = 31;
    public static final int COLUMN_COUNT = 125;
    public static final int MAX_BELEGFELD1_LEN = 36;
    public static final int MAX_BUCHUNGSTEXT_LEN = 60;
    public static final int MAX_BEZEICHNUNG_LEN = 30;

    /**
     * Exact 125 column headers of DATEV Buchungsstapel format version 13
     * (from developer.datev.de).
     */
    public static final String[] COLUMNS = {
        "Umsatz (ohne Soll/Haben-Kz)",          // 1
        "Soll/Haben-Kennzeichen",               // 2
        "WKZ Umsatz",                           // 3
        "Kurs",                                 // 4
        "Basis-Umsatz",                         // 5
        "WKZ Basis-Umsatz",                     // 6
        "Konto",                                // 7
        "Gegenkonto (ohne BU-Schlüssel)",       // 8
        "BU-Schlüssel",                         // 9
        "Belegdatum",                           // 10
        "Belegfeld 1",                          // 11
        "Belegfeld 2",                          // 12
        "Skonto",                               // 13
        "Buchungstext",                         // 14
        "Postensperre",                         // 15
        "Diverse Adressnummer",                 // 16
        "Geschäftspartnerbank",                 // 17
        "Sachverhalt",                          // 18
        "Zinssperre",                           // 19
        "Beleglink",                            // 20
        "Beleginfo - Art 1",                    // 21
        "Beleginfo - Inhalt 1",                 // 22
        "Beleginfo - Art 2",                    // 23
        "Beleginfo - Inhalt 2",                 // 24
        "Beleginfo - Art 3",                    // 25
        "Beleginfo - Inhalt 3",                 // 26
        "Beleginfo - Art 4",                    // 27
        "Beleginfo - Inhalt 4",                 // 28
        "Beleginfo - Art 5",                    // 29
        "Beleginfo - Inhalt 5",                 // 30
        "Beleginfo - Art 6",                    // 31
        "Beleginfo - Inhalt 6",                 // 32
        "Beleginfo - Art 7",                    // 33
        "Beleginfo - Inhalt 7",                 // 34
        "Beleginfo - Art 8",                    // 35
        "Beleginfo - Inhalt 8",                 // 36
        "KOST1 - Kostenstelle",                 // 37
        "KOST2 - Kostenstelle",                 // 38
        "Kost-Menge",                           // 39
        "EU-Land u. UStID (Bestimmung)",        // 40
        "EU-Steuersatz (Bestimmung)",           // 41
        "Abw. Versteuerungsart",                // 42
        "Sachverhalt L+L",                      // 43
        "Funktionsergänzung L+L",               // 44
        "BU 49 Hauptfunktionstyp",              // 45
        "BU 49 Hauptfunktionsnummer",           // 46
        "BU 49 Funktionsergänzung",             // 47
        "Zusatzinformation - Art 1",            // 48
        "Zusatzinformation- Inhalt 1",          // 49
        "Zusatzinformation - Art 2",            // 50
        "Zusatzinformation- Inhalt 2",          // 51
        "Zusatzinformation - Art 3",            // 52
        "Zusatzinformation- Inhalt 3",          // 53
        "Zusatzinformation - Art 4",            // 54
        "Zusatzinformation- Inhalt 4",          // 55
        "Zusatzinformation - Art 5",            // 56
        "Zusatzinformation- Inhalt 5",          // 57
        "Zusatzinformation - Art 6",            // 58
        "Zusatzinformation- Inhalt 6",          // 59
        "Zusatzinformation - Art 7",            // 60
        "Zusatzinformation- Inhalt 7",          // 61
        "Zusatzinformation - Art 8",            // 62
        "Zusatzinformation- Inhalt 8",          // 63
        "Zusatzinformation - Art 9",            // 64
        "Zusatzinformation- Inhalt 9",          // 65
        "Zusatzinformation - Art 10",           // 66
        "Zusatzinformation- Inhalt 10",         // 67
        "Zusatzinformation - Art 11",           // 68
        "Zusatzinformation- Inhalt 11",         // 69
        "Zusatzinformation - Art 12",           // 70
        "Zusatzinformation- Inhalt 12",         // 71
        "Zusatzinformation - Art 13",           // 72
        "Zusatzinformation- Inhalt 13",         // 73
        "Zusatzinformation - Art 14",           // 74
        "Zusatzinformation- Inhalt 14",         // 75
        "Zusatzinformation - Art 15",           // 76
        "Zusatzinformation- Inhalt 15",         // 77
        "Zusatzinformation - Art 16",           // 78
        "Zusatzinformation- Inhalt 16",         // 79
        "Zusatzinformation - Art 17",           // 80
        "Zusatzinformation- Inhalt 17",         // 81
        "Zusatzinformation - Art 18",           // 82
        "Zusatzinformation- Inhalt 18",         // 83
        "Zusatzinformation - Art 19",           // 84
        "Zusatzinformation- Inhalt 19",         // 85
        "Zusatzinformation - Art 20",           // 86
        "Zusatzinformation- Inhalt 20",         // 87
        "Stück",                                // 88
        "Gewicht",                              // 89
        "Zahlweise",                            // 90
        "Forderungsart",                        // 91
        "Veranlagungsjahr",                     // 92
        "Zugeordnete Fälligkeit",               // 93
        "Skontotyp",                            // 94
        "Auftragsnummer",                       // 95
        "Buchungstyp",                          // 96
        "USt-Schlüssel (Anzahlungen)",          // 97
        "EU-Land (Anzahlungen)",                // 98
        "Sachverhalt L+L (Anzahlungen)",        // 99
        "EU-Steuersatz (Anzahlungen)",          // 100
        "Erlöskonto (Anzahlungen)",             // 101
        "Herkunft-Kz",                          // 102
        "Buchungs GUID",                        // 103
        "KOST-Datum",                           // 104
        "SEPA-Mandatsreferenz",                 // 105
        "Skontosperre",                         // 106
        "Gesellschaftername",                   // 107
        "Beteiligtennummer",                    // 108
        "Identifikationsnummer",                // 109
        "Zeichnernummer",                       // 110
        "Postensperre bis",                     // 111
        "Bezeichnung SoBil-Sachverhalt",        // 112
        "Kennzeichen SoBil-Buchung",            // 113
        "Festschreibung",                       // 114
        "Leistungsdatum",                       // 115
        "Datum Zuord. Steuerperiode",           // 116
        "Fälligkeit",                           // 117
        "Generalumkehr (GU)",                   // 118
        "Steuersatz",                           // 119
        "Land",                                 // 120
        "Abrechnungsreferenz",                  // 121
        "BVV-Position",                         // 122
        "EU-Land u. UStID (Ursprung)",          // 123
        "EU-Steuersatz (Ursprung)",             // 124
        "Abw. Skontokonto"                      // 125
    };

    private DatevFormat() {
    }

    /**
     * One booking line of a DATEV Buchungsstapel.
     */
    public static final class BookingLine {

        public BigDecimal umsatz = BigDecimal.ZERO;
        /**
         * "S" or "H"
         */
        public String sollHaben = "S";
        public String wkz = "EUR";
        /**
         * Col 4 (Kurs): exchange rate for foreign-currency bookings (up to 6 decimals), optional.
         */
        public BigDecimal kurs;
        /**
         * Col 5 (Basis-Umsatz): EUR base amount for foreign-currency bookings, optional.
         */
        public BigDecimal basisUmsatz;
        /**
         * Col 6 (WKZ Basis-Umsatz): base currency ISO code (e.g., "EUR"), optional.
         */
        public String wkzBasisUmsatz = "";
        public String konto = "";
        public String gegenkonto = "";
        public String buSchluessel = "";
        /**
         * DDMM (4 digits)
         */
        public String belegdatum = "";
        public String belegfeld1 = "";
        /**
         * Col 12 (Belegfeld 2): optional reference / due-date code (max 12 chars).
         */
        public String belegfeld2 = "";
        /**
         * Col 13 (Skonto): optional cash discount amount (always positive).
         */
        public BigDecimal skonto;
        public String buchungstext = "";
        /**
         * Col 20 (Beleglink): DATEV Belegbilderservice link ({@code BEDI "<UUID>"} / {@code DDMS} / {@code DORG}),
         * or a plain UUID which is formatted as {@code BEDI "<UUID>"}.
         */
        public String beleglink = "";
        /**
         * Col 21 (Beleginfo - Art 1): max 20 chars (e.g., "Belegdatei").
         */
        public String beleginfoArt1 = "";
        /**
         * Col 22 (Beleginfo - Inhalt 1): max 210 chars (e.g., archived PDF/XML filename).
         */
        public String beleginfoInhalt1 = "";
        public String euLandUstId = "";
        /**
         * Col 103 (Buchungs GUID): deterministic UUID (36 chars) for GoBD traceability.
         */
        public String buchungsGuid = "";
        public boolean festschreibung = false;
        /**
         * Col 115 (Leistungsdatum): DDMMYYYY (8 digits), emitted when delivery/service date differs from booking date.
         */
        public String leistungsdatum = "";
        /**
         * Col 116 (Datum Zuord. Steuerperiode): DDMMYYYY (8 digits), emitted when service period differs from booking period.
         */
        public String steuerperiodeDatum = "";
        /**
         * Col 117 (Fälligkeit): DDMMYYYY (8 digits), optional due date.
         */
        public String faelligkeit = "";
        /**
         * Col 118 (Generalumkehr (GU)): true to mark a reversal booking ("1").
         */
        public boolean generalumkehr = false;
    }

    /**
     * Standard DATEV file name: {@code EXTF_Buchungsstapel_<from>_<to>.csv}
     */
    public static String fileName(Date from, Date to) {
        return "EXTF_Buchungsstapel_" + yyyymmdd(from) + "_" + yyyymmdd(to) + ".csv";
    }

    /**
     * Builds the complete EXTF CSV content (header line 1, column headers line 2,
     * and all booking lines) with CRLF line endings.
     */
    public static String render(DatevConfig config, Date from, Date to, Date createdAt, List<BookingLine> lines) {
        StringBuilder sb = new StringBuilder();
        sb.append(headerLine(config, from, to, createdAt)).append(CRLF);
        sb.append(columnHeaderLine()).append(CRLF);
        if (lines != null) {
            for (BookingLine line : lines) {
                sb.append(bookingLine(line)).append(CRLF);
            }
        }
        return sb.toString();
    }

    /**
     * Encodes the rendered EXTF CSV into Windows-1252 bytes.
     */
    public static byte[] encode(String csv) {
        return csv.getBytes(CHARSET);
    }

    /**
     * Builds line 1 (EXTF header, 31 fields in exact DATEV Developer Portal order).
     */
    public static String headerLine(DatevConfig config, Date from, Date to, Date createdAt) {
        String[] h = new String[HEADER_FIELD_COUNT];
        Date ts = createdAt == null ? new Date() : createdAt;
        String wj = config.wjBeginn == null ? "" : config.wjBeginn.trim();
        if (wj.isEmpty() && from != null) {
            wj = new SimpleDateFormat("yyyy").format(from) + "0101";
        }
        String bez = sanitizeBezeichnung(config.bezeichnung);
        String diktat = sanitizeDiktat(config.diktatKuerzel);
        String wkz = config.waehrung == null || config.waehrung.trim().isEmpty() ? "EUR" : config.waehrung.trim().toUpperCase(Locale.ROOT);
        String skr = config.kontenrahmen == null ? "" : config.kontenrahmen.trim();
        String herkunft = sanitizeHerkunft(config.herkunft);
        String exportiertVon = sanitizeText(config.exportiertVon, 25);
        int buchungstyp = config.buchungstyp == 2 ? 2 : 1;
        int rlZweck = (config.rechnungslegungszweck == 30 || config.rechnungslegungszweck == 50
                || config.rechnungslegungszweck == 40 || config.rechnungslegungszweck == 64)
                ? config.rechnungslegungszweck : 0;
        String appInfo = sanitizeText(config.anwendungsinformation, 16);

        h[0] = quote("EXTF");                                       // 1  Kennzeichen
        h[1] = String.valueOf(HEADER_VERSION);                      // 2  Versionsnummer (700)
        h[2] = String.valueOf(FORMAT_CATEGORY);                     // 3  Formatkategorie (21)
        h[3] = quote(FORMAT_NAME);                                  // 4  Formatname ("Buchungsstapel")
        h[4] = String.valueOf(FORMAT_VERSION);                      // 5  Formatversion (13)
        h[5] = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(ts);// 6  Erzeugt am
        h[6] = "";                                                  // 7  Importiert (Leerfeld)
        h[7] = quote(herkunft);                                     // 8  Herkunft
        h[8] = quote(exportiertVon);                                // 9  Exportiert von
        h[9] = quote("");                                           // 10 Importiert von
        h[10] = config.beraterNummer > 0 ? String.valueOf(config.beraterNummer) : "";   // 11 Beraternummer
        h[11] = config.mandantenNummer > 0 ? String.valueOf(config.mandantenNummer) : ""; // 12 Mandantennummer
        h[12] = wj;                                                 // 13 WJ-Beginn (YYYYMMDD)
        h[13] = String.valueOf(Math.max(4, Math.min(8, config.sachkontenLaenge))); // 14 Sachkontenlänge (4-8)
        h[14] = yyyymmdd(from);                                     // 15 Datum von (YYYYMMDD)
        h[15] = yyyymmdd(to);                                       // 16 Datum bis (YYYYMMDD)
        h[16] = quote(bez);                                         // 17 Bezeichnung (max 30)
        h[17] = quote(diktat);                                      // 18 Diktatkürzel
        h[18] = String.valueOf(buchungstyp);                        // 19 Buchungstyp (1 = FiBu, 2 = JA)
        h[19] = String.valueOf(rlZweck);                            // 20 Rechnungslegungszweck (0/30/50)
        h[20] = config.festschreibung ? "1" : "0";                  // 21 Festschreibung (0/1)
        h[21] = quote(wkz);                                         // 22 WKZ ("EUR")
        h[22] = "";                                                 // 23 Reserviert
        h[23] = quote("");                                          // 24 Derivatskennzeichen
        h[24] = "";                                                 // 25 Reserviert
        h[25] = "";                                                 // 26 Reserviert
        h[26] = quote(skr);                                         // 27 Sachkontenrahmen ("03"/"04")
        h[27] = "";                                                 // 28 ID der Branchenlösung
        h[28] = "";                                                 // 29 Reserviert
        h[29] = quote("");                                          // 30 Reserviert
        h[30] = quote(appInfo);                                     // 31 Anwendungsinformation
        return String.join(";", h);
    }

    /**
     * Builds line 2 (125 column headers separated by semicolons).
     */
    public static String columnHeaderLine() {
        return String.join(";", COLUMNS);
    }

    /**
     * Builds one data line with 125 fields separated by semicolons.
     */
    public static String bookingLine(BookingLine l) {
        String[] f = new String[COLUMN_COUNT];
        for (int i = 0; i < f.length; i++) {
            f[i] = "";
        }
        BigDecimal amount = l.umsatz == null ? BigDecimal.ZERO : l.umsatz.abs().setScale(2, RoundingMode.HALF_UP);
        f[0] = formatAmount(amount);                                // 1  Umsatz (ohne Soll/Haben-Kz)
        f[1] = quote("H".equalsIgnoreCase(l.sollHaben) ? "H" : "S");// 2  Soll/Haben-Kennzeichen
        f[2] = quote(l.wkz == null || l.wkz.trim().isEmpty() ? "EUR" : l.wkz.trim().toUpperCase(Locale.ROOT)); // 3 WKZ Umsatz
        if (l.kurs != null && l.kurs.signum() > 0) {
            f[3] = formatRate(l.kurs);                              // 4  Kurs
        }
        if (l.basisUmsatz != null && l.basisUmsatz.signum() != 0) {
            f[4] = formatAmount(l.basisUmsatz.abs().setScale(2, RoundingMode.HALF_UP)); // 5  Basis-Umsatz
            String baseWkz = l.wkzBasisUmsatz == null || l.wkzBasisUmsatz.trim().isEmpty()
                    ? "EUR" : l.wkzBasisUmsatz.trim().toUpperCase(Locale.ROOT);
            f[5] = quote(baseWkz);                                  // 6  WKZ Basis-Umsatz
        } else if (l.wkzBasisUmsatz != null && !l.wkzBasisUmsatz.trim().isEmpty()) {
            f[5] = quote(l.wkzBasisUmsatz.trim().toUpperCase(Locale.ROOT));
        }
        f[6] = digitsOnly(l.konto);                                 // 7  Konto
        f[7] = digitsOnly(l.gegenkonto);                            // 8  Gegenkonto (ohne BU-Schlüssel)
        f[8] = quote(digitsOnly(l.buSchluessel));                   // 9  BU-Schlüssel
        f[9] = l.belegdatum == null ? "" : digitsOnly(l.belegdatum);// 10 Belegdatum (DDMM)
        f[10] = quote(sanitizeBelegfeld1(l.belegfeld1));            // 11 Belegfeld 1 (max 36, allowed chars)
        if (l.belegfeld2 != null && !l.belegfeld2.trim().isEmpty()) {
            f[11] = quote(sanitizeBelegfeld2(l.belegfeld2));        // 12 Belegfeld 2 (max 12)
        }
        if (l.skonto != null && l.skonto.signum() > 0) {
            f[12] = formatAmount(l.skonto.abs().setScale(2, RoundingMode.HALF_UP)); // 13 Skonto
        }
        f[13] = quote(sanitizeText(l.buchungstext, MAX_BUCHUNGSTEXT_LEN)); // 14 Buchungstext (max 60)

        String infoArt1 = l.beleginfoArt1 == null ? "" : l.beleginfoArt1.trim();
        String infoInhalt1 = l.beleginfoInhalt1 == null ? "" : l.beleginfoInhalt1.trim();
        if (l.beleglink != null && !l.beleglink.trim().isEmpty()) {
            String formattedLink = formatBeleglink(l.beleglink);
            if (!formattedLink.isEmpty()) {
                f[19] = quote(formattedLink);                       // 20 Beleglink (BEDI/DDMS/DORG "<GUID>")
            } else if (infoArt1.isEmpty() && infoInhalt1.isEmpty()) {
                // Non-GUID receipt reference routed to Beleginfo 1 so col 20 stays strictly DATEV-valid
                infoArt1 = "Belegdatei";
                infoInhalt1 = l.beleglink.trim();
            }
        }
        if (!infoArt1.isEmpty() || !infoInhalt1.isEmpty()) {
            f[20] = quote(sanitizeText(infoArt1.isEmpty() ? "Belegdatei" : infoArt1, 20)); // 21 Beleginfo - Art 1
            f[21] = quote(sanitizeText(infoInhalt1, 210));          // 22 Beleginfo - Inhalt 1
        }
        if (l.euLandUstId != null && !l.euLandUstId.trim().isEmpty()) {
            f[39] = quote(sanitizeEuVatId(l.euLandUstId));          // 40 EU-Land u. UStID (Bestimmung)
        }
        if (l.buchungsGuid != null && !l.buchungsGuid.trim().isEmpty()) {
            f[102] = quote(sanitizeText(l.buchungsGuid, 36));       // 103 Buchungs GUID
        }
        f[113] = l.festschreibung ? "1" : "0";                      // 114 Festschreibung
        if (l.leistungsdatum != null && digitsOnly(l.leistungsdatum).length() == 8) {
            f[114] = digitsOnly(l.leistungsdatum);                  // 115 Leistungsdatum (DDMMYYYY)
        }
        if (l.steuerperiodeDatum != null && digitsOnly(l.steuerperiodeDatum).length() == 8) {
            f[115] = digitsOnly(l.steuerperiodeDatum);              // 116 Datum Zuord. Steuerperiode (DDMMYYYY)
        }
        if (l.faelligkeit != null && digitsOnly(l.faelligkeit).length() == 8) {
            f[116] = digitsOnly(l.faelligkeit);                     // 117 Fälligkeit (DDMMYYYY)
        }
        if (l.generalumkehr) {
            f[117] = "1";                                           // 118 Generalumkehr (GU)
        }
        return String.join(";", f);
    }

    public static String formatAmount(BigDecimal v) {
        DecimalFormat df = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.GERMANY));
        df.setGroupingUsed(false);
        return df.format(v == null ? BigDecimal.ZERO : v.abs());
    }

    /**
     * Formats a foreign-currency exchange rate (col 4 Kurs): up to 6 decimals,
     * comma decimal separator, no thousands separator.
     */
    public static String formatRate(BigDecimal rate) {
        if (rate == null || rate.signum() <= 0) {
            return "";
        }
        DecimalFormat df = new DecimalFormat("0.00####", DecimalFormatSymbols.getInstance(Locale.GERMANY));
        df.setGroupingUsed(false);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(rate.abs());
    }

    public static String yyyymmdd(Date d) {
        return d == null ? "" : new SimpleDateFormat("yyyyMMdd").format(d);
    }

    public static String ddmm(Date d) {
        return d == null ? "" : new SimpleDateFormat("ddMM").format(d);
    }

    public static String ddmmyyyy(Date d) {
        return d == null ? "" : new SimpleDateFormat("ddMMyyyy").format(d);
    }

    /**
     * Formats a DATEV Beleglink value for column 20:
     * accepts an already-formatted {@code BEDI/DDMS/DORG "<GUID>"} string or a raw
     * UUID (formatted as {@code BEDI "<UUID>"}). Returns empty string for non-GUID
     * strings so they do not violate DATEV's strict column 20 format.
     */
    public static String formatBeleglink(String raw) {
        if (raw == null) {
            return "";
        }
        String s = stripControl(raw);
        if (s.matches("^(BEDI|DDMS|DORG) \".+\"$")) {
            return s.length() > 210 ? s.substring(0, 210) : s;
        }
        if (s.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            return "BEDI \"" + s.toUpperCase(Locale.ROOT) + "\"";
        }
        return "";
    }

    /**
     * Quotes a text field for DATEV CSV: normalizes to lossless Windows-1252,
     * strips control characters, and doubles internal quotes.
     */
    public static String quote(String s) {
        String clean = stripControl(s == null ? "" : s);
        return "\"" + clean.replace("\"", "\"\"") + "\"";
    }

    /**
     * Normalizes Unicode text to lossless Windows-1252:
     * <ul>
     * <li>Applies NFC so decomposed sequences like {@code a\u0308} become {@code ä}</li>
     * <li>Preserves all natively representable CP1252 characters ({@code äöüÄÖÜß€éŠž…})</li>
     * <li>Transliterates non-CP1252 Latin letters ({@code Ł->L, ł->l, Đ->D, đ->d, ẞ->SS})
     *     and strips combining marks for other Latin diacritics ({@code č->c, ř->r, ń->n, ś->s, ź->z})</li>
     * <li>Drops non-encodable symbols/emojis and control characters so {@link #encode(String)}
     *     never emits replacement {@code ?} (0x3F) bytes or splits surrogate pairs</li>
     * </ul>
     */
    public static String toCp1252Safe(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        String nfc = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFC);
        java.nio.charset.CharsetEncoder enc = CHARSET.newEncoder();
        StringBuilder b = new StringBuilder(nfc.length());
        boolean dropped = false;
        for (int i = 0; i < nfc.length(); ) {
            int cp = nfc.codePointAt(i);
            i += Character.charCount(cp);
            if (cp == '\r' || cp == '\n' || cp == '\t' || cp == 0x2007 || cp == 0x2009 || cp == 0x202F) {
                b.append(' ');
                continue;
            }
            if (cp < 0x20 || cp == 0x7F || (cp >= 0x80 && cp <= 0x9F)) {
                dropped = true;
                continue;
            }
            if (cp <= 0xFFFF && enc.canEncode((char) cp)) {
                b.append((char) cp);
                continue;
            }
            String mapped = transliterateNonCp1252(cp);
            if (mapped != null) {
                b.append(mapped);
                continue;
            }
            // Decompose accented Latin letters (e.g., č, ř, ń, ś, ź, ż, ę, ć, ů, ğ, ş, ă, ő, ű)
            String nfd = java.text.Normalizer.normalize(new String(Character.toChars(cp)), java.text.Normalizer.Form.NFD);
            int before = b.length();
            for (int j = 0; j < nfd.length(); j++) {
                char c = nfd.charAt(j);
                if (Character.getType(c) != Character.NON_SPACING_MARK && c >= 0x20 && c != 0x7F
                        && !(c >= 0x80 && c <= 0x9F) && enc.canEncode(c)) {
                    b.append(c);
                }
            }
            if (b.length() == before) {
                dropped = true;
            }
        }
        String res = b.toString();
        if (dropped) {
            res = res.replaceAll(" {2,}", " ");
        }
        return res.trim();
    }

    private static String transliterateNonCp1252(int cp) {
        switch (cp) {
            case 0x0141: return "L";   // Ł
            case 0x0142: return "l";   // ł
            case 0x0110: return "D";   // Đ
            case 0x0111: return "d";   // đ
            case 0x0126: return "H";   // Ħ
            case 0x0127: return "h";   // ħ
            case 0x0130: return "I";   // İ
            case 0x0131: return "i";   // ı
            case 0x014A: return "N";   // Ŋ
            case 0x014B: return "n";   // ŋ
            case 0x0166: return "T";   // Ŧ
            case 0x0167: return "t";   // ŧ
            case 0x1E9E: return "SS";  // ẞ
            case 0x2010:
            case 0x2011:
            case 0x2012:
            case 0x2212: return "-";   // Unicode hyphens / minus
            case 0x2192: return "->";  // →
            case 0x2190: return "<-";  // ←
            default: return null;
        }
    }

    public static String stripControl(String s) {
        return toCp1252Safe(s);
    }

    public static String sanitizeText(String s, int maxLen) {
        String clean = toCp1252Safe(s);
        return clean.length() > maxLen ? clean.substring(0, maxLen).trim() : clean;
    }

    /**
     * DATEV Belegfeld 1: max 36 chars, only digits, ASCII letters, and
     * {@code $ & % * + - /}.
     */
    public static String sanitizeBelegfeld1(String s) {
        return sanitizeBelegfeld(s, MAX_BELEGFELD1_LEN);
    }

    /**
     * DATEV Belegfeld 2: max 12 chars, same allowed charset as Belegfeld 1.
     */
    public static String sanitizeBelegfeld2(String s) {
        return sanitizeBelegfeld(s, 12);
    }

    private static String sanitizeBelegfeld(String s, int maxLen) {
        if (s == null) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        s = transliterate(s);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (isAllowedBelegfeld1Char(c)) {
                b.append(c);
            }
        }
        return b.length() > maxLen ? b.substring(0, maxLen) : b.toString();
    }

    /**
     * @param s
     * @return German umlauts and ß written out (Müller - Mueller), so a
     * document number keeps its letters where only ASCII is allowed
     */
    public static String transliterate(String s) {
        if (s == null) {
            return "";
        }
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFC)
                .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss")
                .replace("Ä", "Ae").replace("Ö", "Oe").replace("Ü", "Ue")
                .replace("ẞ", "SS");
    }

    public static boolean isAllowedBelegfeld1Char(char c) {
        return (c >= '0' && c <= '9')
                || (c >= 'A' && c <= 'Z')
                || (c >= 'a' && c <= 'z')
                || c == '$' || c == '&' || c == '%' || c == '*' || c == '+' || c == '-' || c == '/';
    }

    public static String sanitizeBezeichnung(String s) {
        String clean = toCp1252Safe(s);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            if (Character.isLetterOrDigit(c) || c == ' ' || c == '.' || c == '-' || c == '/' || c == '_') {
                b.append(c);
            }
        }
        String r = b.toString().trim();
        return r.length() > MAX_BEZEICHNUNG_LEN ? r.substring(0, MAX_BEZEICHNUNG_LEN).trim() : r;
    }

    public static String sanitizeDiktat(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = Character.toUpperCase(s.charAt(i));
            if (c >= 'A' && c <= 'Z') {
                b.append(c);
            }
        }
        if (b.length() >= 2) {
            return b.substring(0, 2);
        }
        return b.length() == 1 ? b.toString() : "";
    }

    public static String sanitizeHerkunft(String s) {
        if (s == null || s.trim().isEmpty()) {
            return "RE";
        }
        String clean = toCp1252Safe(s).replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (clean.isEmpty()) {
            return "RE";
        }
        return clean.length() > 2 ? clean.substring(0, 2) : clean;
    }

    public static String sanitizeEuVatId(String s) {
        if (s == null) {
            return "";
        }
        String clean = s.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        return clean.length() > 15 ? clean.substring(0, 15) : clean;
    }

    public static String digitsOnly(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                b.append(c);
            }
        }
        return b.toString();
    }
}
