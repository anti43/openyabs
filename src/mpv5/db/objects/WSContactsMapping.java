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

import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.logging.Log;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.images.MPIcon;

/**
 * Maps Contacts to WebShop Contacts
 */
public class WSContactsMapping extends DatabaseObject {

    /**
     * Fetches a mapping from db
     * @param webShopID
     * @param contactsids
     * @return
     * @throws NodataFoundException 
     */
    public static WSContactsMapping getMapping(int webShopID, int contactsids) throws NodataFoundException {
        QueryCriteria qs = new QueryCriteria("webshopid", webShopID);
        qs.addAndCondition("contactsids", contactsids);
        List old = DatabaseObject.getObjects(Context.getWebShopContactMapping(), qs);
        return (WSContactsMapping) old.get(0);
    }

    /**
     * Fetches all contacts from the db which:
     * <li>Have no current mapping to the given webshop
     * <li>Are in the same group as the webshop (ignored if shop is in "All" group)
     * @param webShop
     * @return A list of Contacts
     */
    public static List<Contact> getUnmappedContacts(WebShop webShop) {
        String query = Context.getContact().prepareSQLString("SELECT contacts.ids FROM contacts WHERE contacts.ids NOT IN " +
                "(SELECT contactsids FROM wscontactsmapping WHERE webshopsids = " + webShop.__getIDS() + ") ");
        if (webShop.__getGroupsids() > 1) {
            query += " AND groupsids = " + webShop.__getGroupsids();
        }
        ReturnValue ads = QueryHandler.instanceOf().clone(Context.getContact()).freeSelectQuery(query, MPSecurityManager.VIEW, null);
        Object[][] data = ads.getData();
        List<Contact> l = new Vector<Contact>();
        for (int i = 0; i < data.length; i++) {
            int ig = Integer.valueOf(data[i][0].toString());
            try {
                l.add((Contact) DatabaseObject.getObject(Context.getContact(), ig));
            } catch (NodataFoundException ex) {
                Log.Debug(WSContactsMapping.class, ex.getMessage());
            }
        }

        Log.Debug(WSContactsMapping.class, "Found unmapped contacts: " + l);
        return l;
    }

    /**
     * Tries to find the highest WS id, in the following order:
     * <li>If the IDs are {@link Number} values, returns the highest number
     * <li>If the values are non-number values, returns the highest values such as Collections.sort(List<String>, String.CASE_INSENSITIVE_ORDER) would return as last value
     * @param webShop 
     * @return The String representation of the highest found number
     */
    public static String getLastWsID(WebShop webShop) {
        String query = Context.getWebShopContactMapping().prepareSQLString("SELECT wscontact FROM wscontactsmapping WHERE webshopsids = " + webShop.__getIDS());
        ReturnValue ads = QueryHandler.instanceOf().clone(Context.getWebShopContactMapping()).freeSelectQuery(query, MPSecurityManager.VIEW, null);

        Object[][] data = ads.getData();
        List<String> l = new Vector<String>();
        boolean stringvals = false;
        Integer hn = 0;
        for (int i = 0; i < data.length; i++) {
            try {
                Integer d = Integer.valueOf(data[i][0].toString());
                if (d > hn) {
                    hn = d;
                }
            } catch (NumberFormatException numberFormatException) {
                stringvals = true;
            }
            l.add(String.valueOf(data[i][0].toString()));
        }

        if (stringvals) {
            Collections.sort(l, String.CASE_INSENSITIVE_ORDER);
            Log.Debug(WSContactsMapping.class, "Last String id: " + l.get(l.size()));
            return String.valueOf(l.get(l.size()));
        } else {
            Log.Debug(WSContactsMapping.class, "Last Number id: " + hn);
            return String.valueOf(hn);
        }
    }
    private int webshopsids;
    private int contactsids;
    private String wscontact = "";

    /**
     * Create a new mapping
     */
    public WSContactsMapping() {
        setContext(Context.getWebShopContactMapping());
    }

    @Override
    public JComponent getView() {
        return null;
    }

    @Override
    public MPIcon getIcon() {
        return null;
    }

    /**
     * @return the webshopsids
     */
    public int __getWebshopsids() {
        return webshopsids;
    }

    /**
     * @param webshopsids the webshopsids to set
     */
    public void setWebshopsids(int webshopsids) {
        this.webshopsids = webshopsids;
    }

    /**
     * @return the contactsids
     */
    public int __getContactsids() {
        return contactsids;
    }

    /**
     * @param contactsids the contactsids to set
     */
    public void setContactsids(int contactsids) {
        this.contactsids = contactsids;
    }

    /**
     * @return the wscontact
     */
    public String __getWscontact() {
        return wscontact;
    }

    /**
     * @param wscontact the wscontact to set
     */
    public void setWscontact(String wscontact) {
        this.wscontact = wscontact;
    }

    @Override
    public boolean save() {
        return super.save(true);
    }
}
