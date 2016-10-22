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

import mpv5.db.common.DataNotCachedException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import javax.swing.JComponent;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.utils.images.MPIcon;

/**
 *
 *  
 */
public class Account extends DatabaseObject {

    private static ArrayList<DatabaseObject> accounts;

    /**
     * ANLAGEGUT KOSTEN Eigenkapital UNKOSTEN EINKOMMEN HAFTUNG
     * @param type
     * @return
     */
    public static String getTypeString(int type) {
        switch (type) {
            case Account.OTHER:
                return Messages.MISC.toString();
            case Account.COST:
                return Messages.COST.toString();
            case Account.EXPENSE:
                return Messages.EXPENSE.toString();
            case Account.INCOME:
                return Messages.INCOME.toString();
            case Account.TAXES:
                return Messages.TAXES.toString();
            case Account.RESERVE:
                return Messages.RESERVE.toString();
            default:
                return "N/A";
        }
    }

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

    public Account() {
        setContext(Context.getAccounts());
    }
    private int intparentaccount;
    private int intaccountclass;
    public final static int OTHER = 0;
    public final static int COST = 1;//Direkte Kosten (Wareneinkauf),
    public final static int EXPENSE = 2;//Sonstige Kosten, Aufwandskonto
    public final static int INCOME = 3;//Ertragskonto
    public final static int TAXES = 4;//Steuern
    public final static int RESERVE = 5;

    private String description = "";
    private double taxvalue;
    private int intaccounttype;
    private int intprofitfid;
    private int inttaxfid;
    private int inttaxuid;
    private String frame = "buildin";
    private String hierarchypath = "";

    @Override
    public JComponent getView() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return null;
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

//    /**
//     * Get all Items which are assigned to this account
//     * @return
//     * @throws mpv5.db.common.NodataFoundException
//     */
//    public List<Item> getItemsInAccount() throws NodataFoundException {
//        List<Item> tmp = DatabaseObject.getReferencedObjects(
//                (Item) DatabaseObject.getObject(Context.getItem()), Context.getItemsToAccounts());
//
//        QueryCriteria c = new QueryCriteria("defaultaccountsids", this.__getIDS());
//        ArrayList<Item> tmp2 = DatabaseObject.getObjects(new Item(), c);
//
//        tmp.addAll(tmp2);
//
//        return tmp;
//    }

    /**
     * Get all additional accounts where the given item is currently assigned to
     * @param item
     * @return
     * @throws mpv5.db.common.NodataFoundException
     */
    public static ArrayList<Account> getAccountsOfItem(Item item) throws NodataFoundException {
        Object[][] tmp = QueryHandler.instanceOf().clone(Context.getItemsToAccounts()).select("accountsids", new String[]{"itemsids", item.__getIDS().toString(), ""});
        ArrayList<Account> l = new ArrayList<Account>();

        l.add((Account) DatabaseObject.getObject(Context.getAccounts(), item.__getAccountsids()));

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

    /**
     * Create a tree model
     * @param data
     * @param rootNode
     * @return
     */
    public static DefaultTreeModel toTreeModel(ArrayList<Account> data, Account rootNode) {

        DefaultMutableTreeNode node1 = null;
        if (data.size() > 0) {
            node1 = new DefaultMutableTreeNode(rootNode);
            data.remove(rootNode);//remove root if in list
            try {
                mpv5.YabsViewProxy.instance().setWaiting(true);
                node1 = addToParents(node1, data);
            } catch (Exception e) {
                Log.Debug(e);
            } finally {
                mpv5.YabsViewProxy.instance().setWaiting(false);
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
//                Log.Debug(Account.class, "Node is root child, adding it to root and removing it from the list.");
                firstnode.add(new DefaultMutableTreeNode(dobj));
//                Log.Debug(Account.class, "Added 1st " + dobj);
                dobjlist.remove(dobj);//First level groups
                i--;
            } else {
//                Log.Debug(Account.class, "Check " + dobj);
                int parentid = dobj.__getIntparentaccount();
                if (((Account) firstnode.getUserObject()).__getIDS().intValue() == parentid) {
                    firstnode.add(new DefaultMutableTreeNode(dobj));
//                    Log.Debug(Account.class, "       Parent " + firstnode);
//                    Log.Debug(Account.class, "             Added " + dobj);
                    dobjlist.remove(dobj);
                    i--;
                } else {
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
     * Create a tree model
     * @param data
     * @param rootaccount
     * @return
     */
    public static DefaultTreeModel toTreeModel2(ArrayList<Account> data, Account rootaccount) {

        HashMap<Account, DefaultMutableTreeNode> map = new HashMap<Account, DefaultMutableTreeNode>();
        for (Account c : data) {
            map.put(c, new DefaultMutableTreeNode(c));
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootaccount);
        getChildrenOf(root, data, map);
        DefaultTreeModel model = new DefaultTreeModel(root);
        return model;
    }

    private static synchronized void getChildrenOf(DefaultMutableTreeNode node, ArrayList<Account> data, HashMap<Account, DefaultMutableTreeNode> map) {
        for (int i = 0; i < data.size(); i++) {
            Account account = data.get(i);
            DefaultMutableTreeNode anode = map.get(account);
            if(account.__getIntparentaccount() == ((Account)node.getUserObject()).__getIDS()){
                node.add(anode);
                data.remove(account);
                i--;
                getChildrenOf(anode, data, map);
            }
        }
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
            ArrayList<Account> childs = DatabaseObject.getObjects(getContext(), new QueryCriteria("intparentaccount", ids));
            for (int i = 0; i < childs.size(); i++) {
                DatabaseObject databaseObject = childs.get(i);
                if (!databaseObject.delete()) {
                    return false;
                }
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        try {
            return super.delete();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public MPIcon getIcon() {
        return null;
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

    /**
     * @return the intprofitfid
     */
    public int __getIntprofitfid() {
        return intprofitfid;
    }

    /**
     * @param intprofitfid the intprofitfid to set
     */
    public void setIntprofitfid(int intprofitfid) {
        this.intprofitfid = intprofitfid;
    }

    /**
     * @return the inttaxfid
     */
    public int __getInttaxfid() {
        return inttaxfid;
    }

    /**
     * @param inttaxfid the inttaxfid to set
     */
    public void setInttaxfid(int inttaxfid) {
        this.inttaxfid = inttaxfid;
    }

    /**
     * @return the inttaxuid
     */
    public int __getInttaxuid() {
        return inttaxuid;
    }

    /**
     * @param inttaxuid the inttaxuid to set
     */
    public void setInttaxuid(int inttaxuid) {
        this.inttaxuid = inttaxuid;
    }

    /**
     * @return the hierarchypath
     */
    public String __getHierarchypath() {
         if (hierarchypath == null || hierarchypath.equals("")) {
            int intp = __getIDS();
            do {
                try {
                    Account p = (Account) getObject(Context.getAccounts(), intp);
                    hierarchypath = Group.GROUPSEPARATOR + p + hierarchypath;
                    intp = p.__getIntparentaccount();
                } catch (NodataFoundException ex) {
                    break;
                }
            } while (intp >= 1);
        }
        return hierarchypath.replaceFirst(Group.GROUPSEPARATOR, "");
    }

    /**
     * @param hierarchypath the hierarchypath to set
     */
    public void setHierarchypath(String hierarchypath) {
        this.hierarchypath = hierarchypath;
    }

    @Override
    public java.util.Map<String, Object> resolveReferences(java.util.Map<String, Object> map) {
        

        try {
            if (map.containsKey("intaccounttype")) {
                map.put("type", getTypeString(Integer.valueOf(map.get("intaccounttype").toString())));
                map.remove("intaccounttype");
            }
        } catch (NumberFormatException numberFormatException) {
            //already resolved?
            Log.Debug(numberFormatException);
        }

        if (map.containsKey("intparentaccount")) {
            try {
                try {
                    map.put("parentaccount", DatabaseObject.getObject(Context.getAccounts(), Integer.valueOf(map.get("intparentaccount").toString())).__getCname());
                    map.remove("intparentaccount");
                } catch (NodataFoundException ex) {
                    map.put("parentaccount", null);
                    Log.Debug(this, ex.getMessage());
                }
            } catch (NumberFormatException numberFormatException) {
                //already resolved?
            }
        }

        return super.resolveReferences(map);
    }

    /**
     * Safely import a database object from external sources (xml, csv etc)<br/>
     * Override this for ensuring the existance of DObject specific mandatory values.
     * @return
     */
    @Override
    public boolean saveImport() {
        Log.Debug(this, "Starting import..");
        Log.Debug(this, "Setting IDS to -1");
        ids = -1;
        Log.Debug(this, "Setting intaddedby to " + mpv5.db.objects.User.getCurrentUser().__getIDS());
        setIntaddedby(mpv5.db.objects.User.getCurrentUser().__getIDS());

        if (__getGroupsids() <= 0 || !DatabaseObject.exists(Context.getGroup(), __getGroupsids())) {
            Log.Debug(this, "Setting groups to users group.");
            setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
        }

        return save();
    }

    @Override
    public boolean hasView() {
        return true;
    }
}
