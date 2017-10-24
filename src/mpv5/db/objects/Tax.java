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
package mpv5.db.objects;

import java.math.BigDecimal;
import javax.swing.JComponent;

import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryHandler;
import mpv5.globals.Constants;
import mpv5.logging.Log;
import mpv5.utils.images.MPIcon;

/**
 *
 *
 */
public class Tax extends DatabaseObject {

    private BigDecimal taxvalue = BigDecimal.ZERO;
    private String identifier = "";
    private String country = "";

    /**
     * Tries to fetch the value for the given tax id
     *
     * @param taxid
     * @return A value or 0d if not found
     */
    public static BigDecimal getTaxValue(Integer taxid) {
        try {
            return ((Tax) getObject(Context.getTaxes(), taxid)).__getTaxvalue();
        } catch (NodataFoundException ex) {
            //Log.Debug(ex);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Return the value for calculation, eg 1.19 for 19%
     * @param taxid
     * @return 
     */
    public static BigDecimal getCalculationValue(Integer taxid) {
        return getTaxValue(taxid).divide(Constants.BD100, 9, BigDecimal.ROUND_HALF_UP).add(BigDecimal.ONE);
    }

    /**
     * Tries to fetch the id for the given tax value
     *
     * @param value
     * @return A value or 0d if not found
     */
    public static int getTaxId(BigDecimal value) {
        try {
            int v = Integer.valueOf(QueryHandler.instanceOf().clone(Context.getTaxes()).select("ids", new String[]{"taxvalue", value.toString(), ""})[0][0].toString());
            Log.Debug(Item.class, "Found tax id: " + v);
            return v;
        } catch (NumberFormatException numberFormatException) {
            Log.Debug(Item.class, "Found NO tax id: " + numberFormatException);
            return 1;
        }
    }

    public Tax() {
        setContext(Context.getTaxes());
    }

    @Override
    public JComponent getView() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MPIcon getIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the taxvalue
     */
    public BigDecimal __getTaxvalue() {
        return taxvalue;
    }

    /**
     * @param taxvalue the taxvalue to set
     */
    public void setTaxvalue(BigDecimal taxvalue) {
        this.taxvalue = taxvalue;
    }

    /**
     * @return the identifier
     */
    public String __getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the country
     */
    public String __getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }
}
