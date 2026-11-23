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
package mpv5.utils.export;

import java.util.Currency;
import java.util.Locale;

/**
 * ISO 4217 currencies supported for contact bank accounts ({@code bankcurrency})
 * and e-invoice / commercial invoice currency ({@code BT-5}).
 */
public enum CurrencyCode {

    EUR("EUR", "€", "Euro"),
    CHF("CHF", "CHF", "Swiss Franc / Schweizer Franken"),
    GBP("GBP", "£", "British Pound / Britisches Pfund"),
    USD("USD", "$", "US Dollar / US-Dollar"),
    PLN("PLN", "zł", "Polish Złoty / Polnischer Złoty"),
    CZK("CZK", "Kč", "Czech Koruna / Tschechische Krone"),
    DKK("DKK", "kr", "Danish Krone / Dänische Krone"),
    SEK("SEK", "kr", "Swedish Krona / Schwedische Krone"),
    NOK("NOK", "kr", "Norwegian Krone / Norwegische Krone"),
    HUF("HUF", "Ft", "Hungarian Forint / Ungarischer Forint"),
    RON("RON", "lei", "Romanian Leu / Rumänischer Leu"),
    BGN("BGN", "лв", "Bulgarian Lev / Bulgarischer Lew"),
    ISK("ISK", "kr", "Icelandic Króna / Isländische Krone"),
    CAD("CAD", "CA$", "Canadian Dollar / Kanadischer Dollar"),
    AUD("AUD", "A$", "Australian Dollar / Australischer Dollar"),
    JPY("JPY", "¥", "Japanese Yen / Japanischer Yen"),
    CNY("CNY", "¥", "Chinese Yuan / Chinesischer Yuan"),
    TRY("TRY", "₺", "Turkish Lira / Türkische Lira");

    private final String code;
    private final String symbol;
    private final String description;

    CurrencyCode(String code, String symbol, String description) {
        this.code = code;
        this.symbol = symbol;
        this.description = description;
    }

    /**
     * @return the 3-letter ISO 4217 code (e.g. {@code "EUR"}, {@code "CHF"})
     */
    public String code() {
        return code;
    }

    /**
     * @return the currency symbol (e.g. {@code "€"}, {@code "CHF"})
     */
    public String symbol() {
        return symbol;
    }

    /**
     * @return the human-readable description
     */
    public String description() {
        return description;
    }

    @Override
    public String toString() {
        return code + " (" + symbol + ") – " + description;
    }

    /**
     * Find a {@link CurrencyCode} by its ISO 4217 code or symbol.
     *
     * @param value code or symbol, may be null or empty
     * @return the matching {@link CurrencyCode}, or {@code null} if unknown or empty
     */
    public static CurrencyCode parse(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        // Extract leading 3-letter code if formatted like "EUR (€) – Euro"
        String upper = trimmed.toUpperCase(Locale.ROOT);
        if (upper.length() >= 3) {
            String prefix3 = upper.substring(0, 3);
            for (CurrencyCode c : values()) {
                if (c.code.equals(prefix3) && (upper.length() == 3 || !Character.isLetter(upper.charAt(3)))) {
                    return c;
                }
            }
        }
        for (CurrencyCode c : values()) {
            if (c.code.equalsIgnoreCase(trimmed) || c.symbol.equalsIgnoreCase(trimmed)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Normalize a currency string to an uppercase 3-letter ISO 4217 code.
     *
     * @param value currency code, enum label, or symbol
     * @return 3-letter ISO 4217 code, or {@code ""} if empty/invalid
     */
    public static String normalizeCode(String value) {
        CurrencyCode known = parse(value);
        if (known != null) {
            return known.code;
        }
        if (value == null) {
            return "";
        }
        String upper = value.trim().toUpperCase(Locale.ROOT);
        if (upper.matches("[A-Z]{3}")) {
            try {
                return Currency.getInstance(upper).getCurrencyCode();
            } catch (IllegalArgumentException e) {
                return upper;
            }
        }
        return "";
    }

    /**
     * @param value
     * @return true if {@code value} is a valid 3-letter ISO 4217 currency code
     * or known {@link CurrencyCode}
     */
    public static boolean isValidCode(String value) {
        if (parse(value) != null) {
            return true;
        }
        if (value == null) {
            return false;
        }
        String upper = value.trim().toUpperCase(Locale.ROOT);
        if (!upper.matches("[A-Z]{3}")) {
            return false;
        }
        try {
            Currency.getInstance(upper);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * @return the ISO 4217 currency code of the default locale, falling back to
     * {@code "EUR"}
     */
    public static String systemCurrencyCode() {
        try {
            Currency cur = Currency.getInstance(Locale.getDefault());
            if (cur != null && cur.getCurrencyCode() != null && cur.getCurrencyCode().matches("[A-Z]{3}")) {
                return cur.getCurrencyCode();
            }
        } catch (Exception e) {
            // default locale without country (e.g. Locale.ENGLISH)
        }
        return EUR.code;
    }
}
