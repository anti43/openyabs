/*
 *  This file is part of MP.
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
package mpv5.utils.numbers;

import mpv5.utils.numberformat.FormatNumber;


/**
 *
 *  
 */
public class vFloat {
    private Float value = null;
    private String svalue = null;
    private boolean isVerified = false;
    private boolean isPositive = false;
    private String ovalue = "";
    private String decValue;

    public vFloat(Object number) {
        if (number != null) {
            this.ovalue = number.toString();
             if (FormatNumber.checkNumber(number)) {
                this.value = ((Number)FormatNumber.parseNumber(ovalue)).floatValue();
                this.decValue = FormatNumber.formatDezimal(value);
                this.svalue = this.value.toString();
                this.isVerified = true;
                if (this.value >= 0) {
                    this.isPositive = true;
                }
            }
        }
    }

    /**
     * @return the value
     */
    public Float getValue() {
        return value;
    }

    /**
     * @return the svalue
     */
    public String getSvalue() {
        return svalue;
    }

    /**
     * @return the isVerified
     */
    public boolean isIsVerified() {
        return isVerified;
    }

    /**
     * @return the isPositive
     */
    public boolean isIsPositive() {
        return isPositive;
    }

    /**
     * @return the ovalue
     */
    public String getOvalue() {
        return ovalue;
    }

    /**
     * @return the decValue
     */
    public String getDecValue() {
        return decValue;
    }
}
