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
package mpv5.utils.text;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Validation of IBAN (ISO 13616, checksum ISO 7064 mod 97-10) and BIC (ISO
 * 9362)
 */
public final class Iban {

    private static final Map<String, Integer> LENGTHS = new HashMap<String, Integer>();
    private static final Set<String> COUNTRIES = new HashSet<String>(Arrays.asList(Locale.getISOCountries()));

    static {
        //IBAN registry (SWIFT), country code -> length
        String l = "AD24 AE23 AL28 AT20 AZ28 BA20 BE16 BG22 BH22 BI27 BR29 BY28 CH21 CR22 CY28 CZ24 DE22 DJ27 "
                + "DK18 DO28 EE20 EG29 ES24 FI18 FK18 FO18 FR27 GB22 GE22 GI23 GL18 GR27 GT28 HN28 HR21 HU28 "
                + "IE22 IL23 IQ23 IS26 IT27 JO30 KW30 KZ20 LB28 LC32 LI21 LT20 LU20 LV21 LY25 MC27 MD24 ME22 "
                + "MK19 MN20 MR27 MT31 MU30 NI28 NL18 NO15 OM23 PK24 PL28 PS29 PT25 QA29 RO24 RS22 RU33 SA24 "
                + "SC31 SD18 SE24 SI19 SK24 SM27 SO23 ST25 SV28 TL23 TN24 TR26 UA29 VA22 VG24 XK20 YE30";
        for (String e : l.split(" ")) {
            LENGTHS.put(e.substring(0, 2), Integer.valueOf(e.substring(2)));
        }
    }

    private Iban() {
    }

    /**
     * Remove spaces and dashes, upper case
     *
     * @param value
     * @return the normalized value, never null
     */
    public static String normalize(String value) {
        return value == null ? "" : value.replaceAll("[\\s\\-]", "").toUpperCase(Locale.ROOT);
    }

    /**
     * Format an IBAN in groups of four characters (DE02 1203 0000 ...)
     *
     * @param iban
     * @return
     */
    public static String format(String iban) {
        return normalize(iban).replaceAll("(.{4})(?!$)", "$1 ");
    }

    /**
     * @param iban the IBAN, may contain spaces
     * @return true if country, length and check digits are correct
     */
    public static boolean isValid(String iban) {
        String s = normalize(iban);
        if (!s.matches("[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}")) {
            return false;
        }
        Integer length = LENGTHS.get(s.substring(0, 2));
        if (length == null || length.intValue() != s.length()) {
            return false;
        }
        String rearranged = s.substring(4) + s.substring(0, 4);
        StringBuilder digits = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            digits.append(Character.getNumericValue(c));
        }
        return new BigInteger(digits.toString()).mod(BigInteger.valueOf(97)).intValue() == 1;
    }

    /**
     * @param bic the BIC, may contain spaces
     * @return true if the BIC is well formed (8 or 11 characters, valid
     * country)
     */
    public static boolean isValidBic(String bic) {
        String s = normalize(bic);
        return s.matches("[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?") && COUNTRIES.contains(s.substring(4, 6));
    }
}
