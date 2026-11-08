/*
 * 
 * 
 */
package mpv5.utils.numberformat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.table.TableModel;

import mpv5.logging.Log;

/**
 * This class provides useful number format methods
 */
public class FormatNumber {

    /**
     * Represents the default decimal format
     */
    public static final String FORMAT_DECIMAL = "#,###,##0.00";
    /**
     * Represents a short decimal format
     */
    public static final String FORMAT_DECIMAL_SHORT = "#0.0#";
    /**
     * @return a new short decimal format in the current default locale
     * (DecimalFormat is mutable and not thread safe, and the locale is set at
     * login - so no shared instance)
     */
    public static NumberFormat getShortDecimalFormat() {
        return new DecimalFormat(FORMAT_DECIMAL_SHORT, java.text.DecimalFormatSymbols.getInstance());
    }

    /**
     * Check whether a text can be parsed to be a decimal number
     *
     * @param text
     * @return If parsing would be successful
     */
    public static boolean checkDezimal(String text) {
        return parseDezimal(text) != null;
    }

    /**
     * The default number format
     *
     * @return
     */
    public static NumberFormat getDefaultDecimalFormat() {
        return new DecimalFormat(FORMAT_DECIMAL, java.text.DecimalFormatSymbols.getInstance());
    }

    /**
     * Formats a number to look like the users default locale decimal (+
     * rounding)
     *
     * @param number
     * @return
     */
    public static String formatDezimal(Number number) {
//        Log.Print(number);
        if (number == null) return "";

        java.text.DecimalFormat n = (DecimalFormat) getDefaultDecimalFormat();
        n.setMaximumFractionDigits(2);
        return n.format(round(number));
    }

    /**
     * Formats a number to look like an int (cut of x.digits)
     *
     * @param number
     * @return
     */
    public static String formatInteger(Number number) {
        return String.valueOf(number.intValue());
    }

    /**
     * Rounds a number up to two fraction digits
     * {BigDecimal.ROUND_HALF_UP}
     *
     * @param number
     * @return
     */
    public static BigDecimal round(Number number) {
        //toString: the decimal the user sees (0.1, not the binary 0.1000000000000000055...)
        BigDecimal b = number instanceof BigDecimal ? (BigDecimal) number : new BigDecimal(number.toString());
        return b.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Tries to parse the given text to a number, using the default Locale.
     * Removes percent & currency signs from the given string before parsing.
     *
     * @param number A string representing a number
     * @return A double number or null if string is not parseable
     */
    public static BigDecimal parseDezimal(String number) {
        if (number == null || number.trim().length() == 0) {
            return BigDecimal.ZERO;
        }
        number = number.replace("%", "");//Remove percent symbol
        java.text.DecimalFormat n = (DecimalFormat) getDefaultDecimalFormat();
        //exact: parse to BigDecimal, not via double
        n.setParseBigDecimal(true);
        try {
            return asBigDecimal(n.parse(number.trim()));
        } catch (ParseException ex) {
            Log.Debug(FormatNumber.class, ex.getMessage());
            Locale[] Locales = Locale.getAvailableLocales();
            for (int i = 0; i < Locales.length; i++) {
                Locale locale = Locales[i];
                try {
                    number = number.replace(Currency.getInstance(locale).getSymbol(), "").trim();
                } catch (Exception e) {
//                        To avoid this (why does a locale have no country??):
//                        Exception in thread "AWT-EventQueue-0" java.lang.IllegalArgumentException
//                        at java.util.Currency.getInstance(Currency.java:244)
                }
            }
            try {
                //Try to parse the String with the default locale, but with removed currency signs
                return asBigDecimal(n.parse(number.trim()));
            } catch (ParseException parseException) {
            }
            return null;
        }
    }

    /**
     * Formats a number to look like the users default locale decimal (+
     * rounding)
     *
     * @param number
     * @return
     */
    public static String formatDezimal(
            Float number) {
        java.text.DecimalFormat n = (DecimalFormat) getDefaultDecimalFormat();
        n.setMaximumFractionDigits(2);
        return n.format(round(new BigDecimal(number.toString())));
    }

    /**
     * Formats a number to look like the users default locale currency (+
     * rounding)
     *
     * @param betrag
     * @return
     */
    public static String formatLokalCurrency(Number betrag) {
        NumberFormat n = NumberFormat.getCurrencyInstance();
        String d = n.format(round(betrag));
        if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "supresscurrencysymbols")) {
            d = d.replace(n.getCurrency().getSymbol(), "");
        }
        return d;
    }

    /**
     * Formats a number to look like the users default locale percent values (+
     * rounding)
     *
     * @param number
     * @return
     */
    public static String formatPercent(Number number) {
        return formatDezimal(number) + "%";
    }

    /**
     * Checks if an object is in anyway compatible to be a number or a decimal
     * number
     *
     * @param number
     * @return
     */
    public static boolean checkNumber(Object number) {
        if (number instanceof Long || number instanceof Integer
                || number instanceof Short || number instanceof Byte
                || number instanceof AtomicInteger
                || number instanceof AtomicLong
                || (number instanceof BigInteger
                && ((BigInteger) number).bitLength() < 64)) {
            return true;
        } else if (number instanceof BigDecimal) {
            return true;
        } else if (number instanceof BigInteger) {
            return true;
        } else if (number instanceof Number) {
            return true;
        } else {
            return checkDezimal(number.toString());
        }

    }

    public static BigDecimal getBigDecimal(Object val) {
        if (val == null || String.valueOf(val).length() == 0) {
            return null;
        }
        //Log.Debug(val.getClass(), val);
        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        } else if (val instanceof Integer) {
            return BigDecimal.valueOf((Integer) val);
        } else if (val instanceof Long) {
            return BigDecimal.valueOf((Long) val);
        } else if (val instanceof Float) {
            //not valueOf(double): 0.1f would become 0.10000000149011612
            return new BigDecimal(val.toString());
        } else if (val instanceof Double) {
            return BigDecimal.valueOf((Double) val);
        } else {
            Log.Debug(FormatNumber.class, "Consider using BigDecimal as value, to prevent preciscion loss");
        }

        return new BigDecimal(val.toString());
    }

    private static BigDecimal asBigDecimal(Number n) {
        return n instanceof BigDecimal ? (BigDecimal) n : new BigDecimal(n.toString());
    }

    public static BigDecimal getBigDecimal(TableModel m, int row, int col) {
        Object val = m.getValueAt(row, col);
        return getBigDecimal(val);
    }

    public static Integer getInteger(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Integer) {
            return (Integer) val;
        }
        return Integer.valueOf(val.toString());
    }

    /**
     * Tries to parse the given Object to a BigDecimal value
     *
     * @param number
     * @return
     */
    public static BigDecimal parseNumber(Object number) {
        if (number != null) {
//            if (number instanceof Number) {
            return new BigDecimal(number.toString());
//            } else {
//                try {
//                    return Double.valueOf(number.toString());
//                } catch (NumberFormatException numberFormatException) {
//                    return parseDezimal(number.toString());
//                }
//
//            }
        } else {
            return BigDecimal.ZERO;
        }
    }
}
