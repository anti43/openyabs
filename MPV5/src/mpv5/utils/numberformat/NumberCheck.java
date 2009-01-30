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
package mpv5.utils.numberformat;

import java.text.NumberFormat;

/**
 *
 * @author anti
 */
public class NumberCheck {

    public static Float checkFloat(Object number) {
        try {
            return Float.valueOf(number.toString());
        } catch (NumberFormatException numberFormatException) {
            try {
                return Float.valueOf(number.toString().replace(",", "").replace(".", ""));
            } catch (NumberFormatException numberFormatException1) {
                try {
                    return Float.valueOf(removeCurrencySymbols(number));
                } catch (NumberFormatException numberFormatException12) {
                    return null;
                }
            }
        }
    }

    public static Integer checkInteger(Object number) {
        try {
            return Integer.valueOf(number.toString());
        } catch (NumberFormatException numberFormatException) {
            return null;
        }
    }

    public static Double checkDouble(Object number) {
        try {
            return Double.valueOf(number.toString());
        } catch (NumberFormatException numberFormatException) {
            try {
                return Double.valueOf(number.toString().replace(",", "").replace(".", ""));
            } catch (NumberFormatException numberFormatException1) {
                try {
                    return Double.valueOf(removeCurrencySymbols(number));
                } catch (NumberFormatException numberFormatException12) {
                    return null;
                }
            }
        }
    }
   public static Float checkFloatNN(Object number) {
        try {
            return Float.valueOf(number.toString());
        } catch (NumberFormatException numberFormatException) {
            try {
                return Float.valueOf(number.toString().replace(",", "").replace(".", ""));
            } catch (NumberFormatException numberFormatException1) {
                try {
                    return Float.valueOf(removeCurrencySymbols(number));
                } catch (NumberFormatException numberFormatException12) {
                    return 0f;
                }
            }
        }
    }

    public static Integer checkIntegerNN(Object number) {
        try {
            return Integer.valueOf(number.toString());
        } catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    public static Double checkDoubleNN(Object number) {
        try {
            return Double.valueOf(number.toString());
        } catch (NumberFormatException numberFormatException) {
            try {
                return Double.valueOf(number.toString().replace(",", "").replace(".", ""));
            } catch (NumberFormatException numberFormatException1) {
                try {
                    return Double.valueOf(removeCurrencySymbols(number));
                } catch (NumberFormatException numberFormatException12) {
                    return 0d;
                }
            }
        }
    }
  public static String removeCurrencySymbols(Object number) {
        return number.toString().replaceAll(NumberFormat.getCurrencyInstance().getCurrency().getSymbol(), "").trim();
    }
}
