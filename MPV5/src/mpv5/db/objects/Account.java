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
package mpv5.db.objects;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JComponent;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
import mpv5.utils.images.MPIcon;

/**
 *
 *  
 */
public class Account extends DatabaseObject {

    private static ArrayList<DatabaseObject> accounts;

    /**
     * Cache accounts
     * @return
     */
    public synchronized static ArrayList<DatabaseObject> cacheAccounts() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    accounts = DatabaseObject.getObjects(Context.getAccounts(), false);
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            }
        };
        new Thread(runnable).start();

        return accounts;
    }
    private int intparentaccount;
    private int intaccountclass;
    public static int ASSET = 0;
    public static int COST = 1;
    public static int EXPENSE = 2;
    public static int INCOME = 3;
    public static int LIABILITY = 4;
    public static int EQUITY = 5;

    public Account() {
        context.setDbIdentity(Context.IDENTITY_ACCOUNTS);
        context.setIdentityClass(this.getClass());
    }
    private String description = "";
    private double taxvalue;
    private int intaccounttype;
    private int profitfid;
    private int taxfid;
    private int taxuid;
    private String frame = "buildin";

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
     * A (preferably cached) view to the accounts
     * @return
     * @throws DataNotCachedException
     */
    public synchronized static ArrayList<DatabaseObject> getAccounts() throws DataNotCachedException {
        if (accounts != null) {
            return accounts;
        } else {
            throw new DataNotCachedException(Context.getAccounts());
        }
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
     *
     *   <li> 1: Assets: Aktiva,
     *   <li>2: Cost: direkte Kosten (Wareneinkauf),
     *   <li>3: Expenses: sonstige Kosten, Aufwandskonto,
     *   <li>4: Income: Ertragskonto,
     *   <li>5: Liablities: Verbindlichkeiten (Mittelherkunft),
     *   <li>6: Equity: Passiva
     *
     * @return the accounttype
     */
    public int __getIntaccounttype() {
        return intaccounttype;
    }

    /**
     *
     *   <li> 1: Assets: Aktiva,
     *   <li>2: Cost: direkte Kosten (Wareneinkauf),
     *   <li>3: Expenses: sonstige Kosten, Aufwandskonto,
     *   <li>4: Income: Ertragskonto,
     *   <li>5: Liablities: Verbindlichkeiten (Mittelherkunft),
     *   <li>6: Equity: Passiva
     *
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

    /**
     * @return the intparentaccount
     */
    public int __getIntparentaccount() {
        return intparentaccount;
    }

    /**
     * @param intparentaccount the intparentaccount to set
     */
    public void setIntparentaccount(int intparentaccount) {
        this.intparentaccount = intparentaccount;
    }

    public static DefaultTreeModel toTreeModel(ArrayList<Account> data, Account rootNode) {

        DefaultMutableTreeNode node1 = null;
        if (data.size() > 0) {
            node1 = new DefaultMutableTreeNode(rootNode);
            data.remove(rootNode);//remove root if in list
            try {
                MPView.setWaiting(true);
                node1 = addToParents(node1, data);

            } catch (Exception e) {
                Log.Debug(e);
            } finally {
                MPView.setWaiting(false);
            }
        }
        DefaultTreeModel model = new DefaultTreeModel(node1);
        return model;
    }

    @SuppressWarnings("unchecked")
    private static DefaultMutableTreeNode addToParents(DefaultMutableTreeNode firstnode, ArrayList<Account> dobjlist) {

        for (int i = 0; i < dobjlist.size(); i++) {
            Account dobj = dobjlist.get(i);

            if (dobj.__getIntparentaccount() <= 0 && firstnode.isRoot()) {
//                Log.Debug(ArrayUtilities.class, "Node is root child, adding it to root and removing it from the list.");
                firstnode.add(new DefaultMutableTreeNode(dobj));
                dobjlist.remove(dobj);//First level groups
                i--;
            } else {
                int parentid = dobj.__getIntparentaccount();
                if (((Account) firstnode.getUserObject()).__getIDS().intValue() == parentid) {
//                    Log.Debug(ArrayUtilities.class, "Node is child of parentnode, adding and removing it from the list.");
                    firstnode.add(new DefaultMutableTreeNode(dobj));
                    dobjlist.remove(dobj);
                    i--;
                } else {
//                    Log.Debug(ArrayUtilities.class, "Node is no child of parentnode, iterating over the parent node..");
                    @SuppressWarnings("unchecked")
                    Enumeration<DefaultMutableTreeNode> nodes = firstnode.children();
                    while (nodes.hasMoreElements()) {
                        addToParents(nodes.nextElement(), dobjlist);
                    }
                }
            }
        }
        return firstnode;
    }

    /**
     * @return the taxvalue
     */
    public double __getTaxvalue() {
        return taxvalue;
    }

    /**
     * @param taxvalue the taxvalue to set
     */
    public void setTaxvalue(double taxvalue) {
        this.taxvalue = taxvalue;
    }

    @Override
    public boolean delete() {
        try {
            ArrayList<Account> childs = DatabaseObject.getObjects(context, new QueryCriteria("intparentaccount", ids));
            for (int i = 0; i < childs.size(); i++) {
                DatabaseObject databaseObject = childs.get(i);
                if (!databaseObject.delete()) {
                    return false;
                }
            }
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }
        return super.delete();
    }

    @Override
    public MPIcon getIcon() {
        return null;
    }

    /**
     * @return the profitfid
     */
    public int __getProfitfid() {
        return profitfid;
    }

    /**
     * @param profitfid the profitfid to set
     */
    public void setProfitfid(int profitfid) {
        this.profitfid = profitfid;
    }

    /**
     * @return the taxfid
     */
    public int __getTaxfid() {
        return taxfid;
    }

    /**
     * @param taxfid the taxfid to set
     */
    public void setTaxfid(int taxfid) {
        this.taxfid = taxfid;
    }

    /**
     * @return the taxuid
     */
    public int __getTaxuid() {
        return taxuid;
    }

    /**
     * @param taxuid the taxuid to set
     */
    public void setTaxuid(int taxuid) {
        this.taxuid = taxuid;
    }

    /**
     * @return the frame
     */
    public String __getFrame() {
        return frame;
    }

    /**
     * @param frame the frame to set
     */
    public void setFrame(String frame) {
        this.frame = frame;
    }
}
