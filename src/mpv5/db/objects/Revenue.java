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
import mpv5.ui.panels.RevenuePanel;
import mpv5.utils.images.MPIcon;
import mpv5.utils.numberformat.FormatNumber;

/**
 *
 *  
 */
public class Revenue extends DatabaseObject implements Formattable {

//    public static int TYPE_REVENUE = 42;
    private String description = "";
    private BigDecimal netvalue = BigDecimal.ZERO;
    private BigDecimal taxpercentvalue = BigDecimal.ZERO;
    private BigDecimal brutvalue = BigDecimal.ZERO;
    private String cnumber;
    private String cname;
    private int accountsids;
    private int contactsids;
    private int refOrderids;
    private FormatHandler formatHandler;
    private Date dateadded;
    private Date dateend;
    private int status;

    public Revenue() {
        setContext(Context.getRevenue());
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
        return cname + " (" + (FormatNumber.formatLokalCurrency(this.brutvalue)) + ")";
    }
   

    @Override
    public JComponent getView() {
        return RevenuePanel.instanceOf();
    }

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        switch (status) {
            case Item.STATUS_QUEUED:
                return new MPIcon("/mpv5/resources/images/22/kontact_mail.png");
            case Item.STATUS_PAID:
                return new MPIcon("/mpv5/resources/images/22/ok.png");
            case Item.STATUS_CANCELLED:
                return new MPIcon("/mpv5/resources/images/22/file_temporary.png");
            default:
                return new MPIcon("/mpv5/resources/images/22/1uparrow.png");
        }
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

    @Override
    public FormatHandler getFormatHandler() {
        if (formatHandler == null) {
            formatHandler = new FormatHandler(this);
        }
        return formatHandler;
    }

    @Override
    public void ensureUniqueness() {
        Log.Debug(this, "In ensureUniqueness for " + this.getClass());
        setCnumber(getFormatHandler().next());
        buildCname();
        Log.Debug(this, "ensureUniqueness result: " + __getCnumber());
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
            Revenue r = (Revenue) data.get(i);
            obj[i] = r.toArray();
        }
        return obj;
    }

    /**
     * Turn this revenue into a table row
     * @return
     */
    @Override
    public Object[] toArray() {
        Object[] o = new Object[11];
        o[0] = this;
        o[1] = cname;
        o[2] = description;
        try {
            o[3] = getObject(Context.getAccounts(), accountsids);
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        o[4] = FormatNumber.formatDezimal(brutvalue);
        o[5] = FormatNumber.formatPercent(taxpercentvalue);
        o[6] = status;
        o[7] = dateadded;
        o[8] = dateend;
        try {
            o[9] = getObject(Context.getContact(), contactsids);
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        try {
            o[10] = getObject(Context.getOrder(), refOrderids);
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }

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

    @Override
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
     * returns the external Ref. (stored in cname)
     * @return the external Ref.
     */
    @Override
    public String __getCname() {
        return cname;
    }

    /**
     * sets the ext. Ref (stored in cname)
     * @param cname the external Ref.
     */
    @Override
    public void setCname(String cname) {
        this.cname = cname;
    }

    /**
     * gets the date for dataset created
     * @return the creation date
     */
    @Override
    public Date __getDateadded() {
        return dateadded;
    }

    /**
     * sets the date for dataset created
     * @param dateadded the creation date
     */
    @Override
    public void setDateadded(Date dateadded) {
        this.dateadded = dateadded;
    }

    /**
     *  gets the assigned Contact
     * @return the ID of the Contact 
     * or null if no Contact is assigned 
     * (thats why the database constraints work evertime or never
     * with this hack it does not check the id if its null)
     */
    public Integer __getContactsids() {
        if (contactsids >= 0) {
            return contactsids;
        } else {
            return (Integer) null;
        }
    }

    /**
     * set the assigned Contact
     * @param contactsids the ID of the Contact 
     * Please set -1 for none
     */
    public void setContactsids(int contactsids) {
        this.contactsids = contactsids;
    }

    /**
     *  gets the assigned Order
     * @return the ID of the Order (Item)
     * or null if no Order is assigned 
     * (thats why the database constraints work evertime or never
     * with this hack it does not check the id if its null)
     */
    public Integer __getRefOrderids() {
        if (refOrderids >= 0) {
            return refOrderids;
        } else {
            return (Integer) null;
        }
    }
    
    /**
     * set the assigned Order
     * @param refOrderids the ID of the Order 
     * Please set -1 for none
     */
    public void setRefOrderids(int refOrderids) {
        this.refOrderids = refOrderids;
    }

    /**
     * gets the status 
     * @return the status 
     */
    public int __getStatus() {
        return status;
    }

    /**
     * set the status
     * @param status the status
     */
    public void setStatus(int status) {
        this.status = status;
    }
    
    /**
     * build the cname out of the cnumber or 
     * according to the script IntRevenueLabel
     */
    private void buildCname() {
        if (cnumber == null) {
            setCname("<not set>");
        } 
        else 
        {
            if (ValueProperty.hasProperty(this.getContext(), "IntRevenueLabel")) {
                try {
                    ValueProperty script = ValueProperty.getProperty(this.getContext(), "IntRevenueLabel");
                    Object s = script.getValue();
                    if (s != null) {
                        setCname(this.evaluateAll(String.valueOf(s)));
                    }
                } catch (NodataFoundException ex) {
                    setCname(cnumber);
                }
            } else {
                setCname(cnumber);
            }
        }
    }
}
