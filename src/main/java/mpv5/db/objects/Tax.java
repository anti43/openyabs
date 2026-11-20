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
     * the VAT category (UNCL5305, see {@link mpv5.utils.export.VatCategory}),
     * empty: S for a rate above 0 %, E for 0 %
     */
    private String categorycode = "";
    /**
     * the exemption reason code (VATEX), empty: the default of the category
     */
    private String exemptioncode = "";
    /**
     * the note on the invoice, empty: the default of the category
     */
    private String exemptionnote = "";

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

    /**
     * @return the stored VAT category code, "" if not set
     */
    public String __getCategorycode() {
        return categorycode;
    }

    /**
     * @param categorycode the VAT category code (UNCL5305), "" for the default
     */
    public void setCategorycode(String categorycode) {
        this.categorycode = categorycode == null ? "" : categorycode.trim();
    }

    /**
     * @return the stored exemption reason code (VATEX), "" if not set
     */
    public String __getExemptioncode() {
        return exemptioncode;
    }

    /**
     * @param exemptioncode the exemption reason code (VATEX), "" for the
     * default of the category
     */
    public void setExemptioncode(String exemptioncode) {
        this.exemptioncode = exemptioncode == null ? "" : exemptioncode.trim();
    }

    /**
     * @return the stored note on the invoice, "" if not set
     */
    public String __getExemptionnote() {
        return exemptionnote;
    }

    /**
     * @param exemptionnote the note on the invoice, "" for the default of the
     * category
     */
    public void setExemptionnote(String exemptionnote) {
        this.exemptionnote = exemptionnote == null ? "" : exemptionnote.trim();
    }

    /**
     * @return the VAT category of this rate (the stored one if it fits the
     * rate)
     */
    public mpv5.utils.export.VatCategory category() {
        return mpv5.utils.export.VatCategory.of(categorycode, taxvalue);
    }

    /**
     * @return the exemption reason code (VATEX): the stored one, else the
     * default of the category; null for none
     */
    public String reasonCode() {
        mpv5.utils.export.VatCategory c = category();
        if (c == mpv5.utils.export.VatCategory.S) {
            return null;
        }
        return exemptioncode != null && !exemptioncode.isEmpty() ? exemptioncode : c.defaultReasonCode();
    }

    /**
     * @return the note on the invoice: the stored one, else the default of
     * the category ("" for none)
     */
    public String note() {
        mpv5.utils.export.VatCategory c = category();
        if (c == mpv5.utils.export.VatCategory.S) {
            return "";
        }
        return exemptionnote != null && !exemptionnote.isEmpty() ? exemptionnote : c.defaultNote();
    }

    /**
     * The tax rate of a position: the first of the preferred rates (e.g. the
     * customer's rate, then the product's rate) with the value of the
     * position, else a rate with this value and a category, else any rate with
     * this value
     *
     * @param rate the rate of the position in percent
     * @param taxes all tax rates
     * @param preferred ids of preferred rates, in order (null or &lt;= 0 are
     * skipped)
     * @return the rate, null if no rate has this value
     */
    public static Tax forRate(BigDecimal rate, java.util.List<Tax> taxes, Integer... preferred) {
        BigDecimal r = rate == null ? BigDecimal.ZERO : rate;
        if (taxes == null) {
            return null;
        }
        if (preferred != null) {
            for (Integer id : preferred) {
                if (id == null || id <= 0) {
                    continue;
                }
                for (Tax t : taxes) {
                    if (id.equals(t.__getIDS()) && sameRate(t, r)) {
                        return t;
                    }
                }
            }
        }
        Tax any = null;
        for (Tax t : taxes) {
            if (sameRate(t, r)) {
                if (t.__getCategorycode() != null && !t.__getCategorycode().isEmpty()) {
                    return t;
                }
                if (any == null) {
                    any = t;
                }
            }
        }
        return any;
    }

    private static boolean sameRate(Tax t, BigDecimal rate) {
        return t.__getTaxvalue() != null && t.__getTaxvalue().compareTo(rate) == 0;
    }

    /**
     * @return all tax rates, an empty list if there are none or no database
     */
    public static java.util.List<Tax> all() {
        java.util.List<Tax> l = new java.util.ArrayList<Tax>();
        try {
            if (mpv5.db.common.DatabaseConnection.getConnector() == null
                    || mpv5.db.common.DatabaseConnection.getConnector().getConnection() == null) {
                return l;
            }
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getTaxes())) {
                l.add((Tax) o);
            }
        } catch (Exception e) {
            Log.Debug(Tax.class, e.getMessage());
        }
        return l;
    }
}
