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
package mpv5.db.objects;

import java.util.ArrayList;
import javax.swing.JComponent;

import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;

/**
 *
 * @author anti
 */
public class Account extends DatabaseObject {

    // Overridden and used here to organize accounts and sub-accounts
    private int groupsids;
    private int intaccountclass ;
    /**
     * Expenses go here
     */
    public static int EXPENSE = 0;
    /**
     * Revenues go here
     */
    public static int REVENUE = 1;


    public Account() {
        context.setDbIdentity(Context.IDENTITY_ACCOUNTS);
        context.setIdentityClass(this.getClass());
    }
    private String description;
    private int taxids;
    private int intaccounttype;

    @Override
    public JComponent getView() {
        throw new UnsupportedOperationException("Not supported yet.");
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
     * @return the taxids
     */
    public int __getTaxids() {
        return taxids;
    }

    /**
     * @param taxids the taxids to set
     */
    public void setTaxids(int taxids) {
        this.taxids = taxids;
    }

    /**
     * Get all Items which are assigned to this account
     * @return
     * @throws mpv5.db.common.NodataFoundException
     */
    public ArrayList<Item> getItemsInAccount() throws NodataFoundException {
        ArrayList<Item> tmp = DatabaseObject.getReferencedObjects((Item) DatabaseObject.getObject(Context.getItems()), Context.getItemsToAccounts());

        QueryCriteria c = new QueryCriteria();
        c.add("defaultaccountsids", this.__getIDS());
        ArrayList<Item> tmp2 = DatabaseObject.getObjects(new Item(), c);

        tmp.addAll(tmp2);
        
        return tmp;
    }

    /**
     * Get all additional accounts where the given item is currently assigned to
     * @param item
     * @return
     * @throws mpv5.db.common.NodataFoundException
     */
    public static ArrayList<Account> getAccountsOfItem(Item item) throws NodataFoundException {
        Object[][] tmp = QueryHandler.instanceOf().clone(Context.getItemsToAccounts()).select("accountsids", new String[]{"itemsids", item.__getIDS().toString(), ""});
        ArrayList<Account> l = new ArrayList<Account>();

        l.add((Account) DatabaseObject.getObject(Context.getAccounts(), item.__getDefaultaccountsids()));

        for (int i = 0; i < tmp.length; i++) {
            int id = Integer.valueOf(tmp[i][0].toString());
            l.add((Account) DatabaseObject.getObject(Context.getAccounts(), id));
        }

        return l;
    }

    /**
     * Represents the parent account
     * @return the groupsids
     */
    @Override
    public int __getGroupsids() {
        return groupsids;
    }

    /**
     * Set the parent account
     * @param groupsids the groupsids to set
     */
    @Override
    public void setGroupsids(int groupsids) {
        this.groupsids = groupsids;
    }

    /**
     * @return the accounttype
     */
    public int __getIntaccounttype() {
        return intaccounttype;
    }

    /**
     * @param accounttype the accounttype to set
     */
    public void setIntaccounttype(int accounttype) {
        this.intaccounttype = accounttype;
    }


    /**
     * @return the intaccountclass
     */
    public int __getIntaccountclass() {
        return intaccountclass;
    }

    /**
     * @param intaccountclass the intaccountclass to set
     */
    public void setIntaccountclass(int intaccountclass) {
        this.intaccountclass = intaccountclass;
    }
}
