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

import java.util.ArrayList;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.Formattable;
import mpv5.db.common.NodataFoundException;
import mpv5.handler.FormatHandler;
import mpv5.logging.Log;
import mpv5.ui.panels.ExpensePanel;
import mpv5.ui.panels.RevenuePanel;
import mpv5.utils.images.MPIcon;
import mpv5.utils.numberformat.FormatNumber;

/**
 *
 *  
 */
public class Revenue extends DatabaseObject implements Formattable {

    public static int TYPE_REVENUE = 42;
    private String description = "";
    private double netvalue;
    private double taxpercentvalue;
    private double brutvalue;
    private String cnumber;
    private int accountsids;
    private FormatHandler formatHandler;

    public Revenue() {
        context = Context.getRevenue();
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
        return RevenuePanel.instanceOf();
    }

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return new MPIcon("/mpv5/resources/images/22/1uparrow.png");
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

    @Override
    public void ensureUniqueness() {
        setCnumber(getFormatHandler().toString(getFormatHandler().getNextNumber()));
        setCName(__getCnumber());
    }

    /**
     * Create a table model's data from all revenues
     * @return
     * @throws NodataFoundException
     */
    public static Object[][] getRevenues() throws NodataFoundException {
        ArrayList<DatabaseObject> data = getObjects(Context.getRevenue());
        Object[][] obj = new Object[data.size()][];
        for (int i = 0; i < data.size(); i++) {
            Revenue e = (Revenue) data.get(i);
            obj[i] = e.toArray();
        }
        return obj;
    }

    /**
     * Turn this revenue into a table row
     * @return
     */
    public Object[] toArray() {
        Object[] o = new Object[5];
        o[0] = this;
        o[1] = description;
        try {
            o[2] = getObject(Context.getAccounts(), accountsids);
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        o[3] = FormatNumber.formatDezimal(brutvalue);
        o[4] = FormatNumber.formatPercent(taxpercentvalue);
        return o;
    }

    /**
     * @return the cnumber
     */
    public String __getCnumber() {
        return cnumber;
    }

    /**
     * @param cnumber the cnumber to set
     */
    public void setCnumber(String cnumber) {
        this.cnumber = cnumber;
    }

    public void defineFormatHandler(FormatHandler handler) {
        formatHandler = handler;
    }
}
