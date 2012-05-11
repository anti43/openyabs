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
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.Formattable;
import mpv5.db.common.NodataFoundException;
import mpv5.handler.FormatHandler;
import mpv5.logging.Log;
import mpv5.ui.panels.ExpensePanel;
import mpv5.utils.images.MPIcon;
import mpv5.utils.numberformat.FormatNumber;

/**
 *
 *  
 */
public class Expense extends DatabaseObject implements Formattable {

    public static int TYPE_EXPENSE = 43;
    private String description = "";
    private BigDecimal netvalue;
    private BigDecimal taxpercentvalue;
    private BigDecimal brutvalue;
    private String cnumber;
    private int accountsids;
    private FormatHandler formatHandler;
    private Date dateend = new Date();
    private boolean ispaid;

    public Expense() {
        setContext(Context.getExpense());
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
        return __getCname();
    }

    @Override
    public JComponent getView() {
        return ExpensePanel.instanceOf();
    }

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return new MPIcon("/mpv5/resources/images/22/1downarrow.png");
    }

    /**
     * @return the netvalue
     */
    public BigDecimal __getNetvalue() {
        return netvalue;
    }

    /**
     * @param netvalue the netvalue to set
     */
    public void setNetvalue(BigDecimal netvalue) {
        this.netvalue = netvalue;
    }

    /**
     * @return the taxpercentvalue
     */
    public BigDecimal __getTaxpercentvalue() {
        return taxpercentvalue;
    }

    /**
     * @param taxpercentvalue the taxpercentvalue to set
     */
    public void setTaxpercentvalue(BigDecimal taxpercentvalue) {
        this.taxpercentvalue = taxpercentvalue;
    }

    /**
     * @return the brutvalue
     */
    public BigDecimal __getBrutvalue() {
        return brutvalue;
    }

    /**
     * @param brutvalue the brutvalue to set
     */
    public void setBrutvalue(BigDecimal brutvalue) {
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
        setCnumber(getFormatHandler().next());
        setCname(__getCnumber());
    }

    /**
     * Create a table model's data from all expenses
     * @return
     * @throws NodataFoundException
     */
    public static Object[][] getExpenses() throws NodataFoundException {
        ArrayList<DatabaseObject> data = getObjects(Context.getExpense());
        Object[][] obj = new Object[data.size()][];
        for (int i = 0; i < data.size(); i++) {
            Expense e = (Expense) data.get(i);
            obj[i] = e.toArray();
        }
        return obj;
    }

    /**
     * Turn this expense into a table row
     * @return
     */
    @Override
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

    /**
     * @return the dateend
     */
    public Date __getDateend() {
        return dateend;
    }

    /**
     * @param dateend the dateend to set
     */
    public void setDateend(Date dateend) {
        this.dateend = dateend;
    }

    /**
     * @return the paid
     */
    public boolean __getIspaid() {
        return ispaid;
    }

    /**
     * @param paid the paid to set
     */
    public void setIspaid(boolean paid) {
        this.ispaid = paid;
    }
}
