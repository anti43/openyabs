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
package mpv5.items.main;

import java.util.Date;
import mpv5.db.common.DatabaseObject;

/**
 *
 * @author anti
 */
public class SubItem extends DatabaseObject {

    private int itemsids;
    private int originalproductsids;
    private double countvalue;
    private double quantityvalue;
    private String measure;
    private String description;
    private double value;
    private double taxpercentvalue;
    private Date datedelivery;

    @Override
    public String __getCName() {
        return cname;
    }

    @Override
    public void setCName(String name) {
        this.cname = name;
    }

    /**
     * @return the itemsids
     */
    public int __getItemsids() {
        return itemsids;
    }

    /**
     * @param itemsids the itemsids to set
     */
    public void setItemsids(int itemsids) {
        this.itemsids = itemsids;
    }

    /**
     * @return the originalproductsids
     */
    public int __getOriginalproductsids() {
        return originalproductsids;
    }

    /**
     * @param originalproductsids the originalproductsids to set
     */
    public void setOriginalproductsids(int originalproductsids) {
        this.originalproductsids = originalproductsids;
    }

    /**
     * @return the count
     */
    public double __getCountvalue() {
        return countvalue;
    }

    /**
     * @param count the count to set
     */
    public void setCountvalue(double count) {
        this.countvalue = count;
    }

    /**
     * @return the quantity
     */
    public double __getQuantityvalue() {
        return quantityvalue;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantityvalue(double quantity) {
        this.quantityvalue = quantity;
    }

    /**
     * @return the measure
     */
    public String __getMeasure() {
        return measure;
    }

    /**
     * @param measure the measure to set
     */
    public void setMeasure(String measure) {
        this.measure = measure;
    }

    /**
     * @return the description
     */
    public String __getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the value
     */
    public double __getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * @return the taxpercent
     */
    public double __getTaxpercentvalue() {
        return taxpercentvalue;
    }

    /**
     * @param taxpercent the taxpercent to set
     */
    public void setTaxpercentvalue(double taxpercent) {
        this.taxpercentvalue = taxpercent;
    }

    /**
     * @return the datedelivery
     */
    public Date __getDatedelivery() {
        return datedelivery;
    }

    /**
     * @param datedelivery the datedelivery to set
     */
    public void setDatedelivery(Date datedelivery) {
        this.datedelivery = datedelivery;
    }


}
