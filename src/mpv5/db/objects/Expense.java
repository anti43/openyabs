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

import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.Formattable;
import mpv5.handler.FormatHandler;

/**
 *
 *  
 */
public class Expense extends DatabaseObject implements Formattable {

    private String description = "";
    private double netvalue;
    private double taxpercentvalue;
    private double brutvalue;
    private int accountsids;
    private FormatHandler formatHandler;

    public Expense() {
        context.setDbIdentity(Context.IDENTITY_EXPENSE);
        context.setIdentityClass(this.getClass());
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

    @Override
    public String toString() {
        return __getCName();
    }

    @Override
    public JComponent getView() {
        return null;
    }

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
    }

    /**
     * @return the netvalue
     */
    public double __getNetvalue() {
        return netvalue;
    }

    /**
     * @param netvalue the netvalue to set
     */
    public void setNetvalue(double netvalue) {
        this.netvalue = netvalue;
    }

    /**
     * @return the taxpercentvalue
     */
    public double __getTaxpercentvalue() {
        return taxpercentvalue;
    }

    /**
     * @param taxpercentvalue the taxpercentvalue to set
     */
    public void setTaxpercentvalue(double taxpercentvalue) {
        this.taxpercentvalue = taxpercentvalue;
    }

    /**
     * @return the brutvalue
     */
    public double __getBrutvalue() {
        return brutvalue;
    }

    /**
     * @param brutvalue the brutvalue to set
     */
    public void setBrutvalue(double brutvalue) {
        this.brutvalue = brutvalue;
    }

    /**
     * @return the accountsids
     */
    public int __getAccountsids() {
        return accountsids;
    }

    /**
     * @param accountsids the accountsids to set
     */
    public void setAccountsids(int accountsids) {
        this.accountsids = accountsids;
    }

    public FormatHandler getFormatHandler() {
         if (formatHandler == null) {
            formatHandler = new FormatHandler(this);
        }
        return formatHandler;
    }
}
