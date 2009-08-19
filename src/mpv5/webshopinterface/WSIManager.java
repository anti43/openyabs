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
package mpv5.webshopinterface;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.WebShop;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.webshopinterface.wsdjobs.addContactJob;
import mpv5.webshopinterface.wsdjobs.newContactsJob;
import mpv5.webshopinterface.wsdjobs.newOrdersJob;
import mpv5.webshopinterface.wsdjobs.newSystemMessages;
import mpv5.webshopinterface.wsdjobs.updatedContactsJob;
import mpv5.webshopinterface.wsdjobs.updatedOrdersJob;

/**
 * Manages the web shops and related tasks
 */
public class WSIManager {

    private static WSIManager instance;

    /**
     * Get the one and only WSI Manager instance
     * @return 
     */
    public static WSIManager instanceOf() {
        return instance == null ? instance = new WSIManager() : instance;
    }
    private ArrayList<WebShop> shops = new ArrayList<WebShop>();

    private WSIManager() {
        reset();
    }

    /**
     * Starts the WSI Manager
     */
    public void start() {
        Log.Debug(this, "Initiating WebShop clients..");
        for (int i = 0; i < shops.size(); i++) {
            WebShop webShop = shops.get(i);
            try {
                WSDaemon d = new WSDaemon(webShop);
                d.addJob(new newContactsJob(d));
                d.addJob(new newOrdersJob(d));
                d.addJob(new newSystemMessages(d));
                d.addJob(new updatedContactsJob(d));
                d.addJob(new updatedOrdersJob(d));
                d.addJob(new addContactJob(d));
                d.start();
                shopDeamons.put(webShop, d);
                Log.Debug(this, "WebShop client: " + d + " started.");
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        }
        Log.Debug(this, "Done with initiating WebShop clients.");
    }
    private HashMap<WebShop, WSDaemon> shopDeamons = new HashMap<WebShop, WSDaemon>();


    /**
     * @return the shops
     */
    public ArrayList<WebShop> getShops() {
        return shops;
    }

    /**
     * Creates objects from the given map
     * @param <T>
     * @param data
     * @param template
     * @return
     */
    public static <T extends DatabaseObject> List<T> createObjects(Object data, T template) {
        Vector<T> list = new Vector<T>();
        if (data instanceof HashMap) {
            HashMap m = (HashMap) data;
            for (Iterator i = m.keySet().iterator(); i.hasNext();) {
                Log.Debug(WSDaemon.class, i.next());
            }
        } else if (data instanceof List) {
            for (int i = 0; i < ((List) data).size(); i++) {
                Log.Debug(WSDaemon.class, ((List) data).get(i));
            }
        } else {
            throw new IllegalArgumentException("Only List and HashMap are supported here! You provided: " + data.getClass());
        }
        return list;
    }

    /**
     * Reset. Call start() afterwards.
     */
    public void reset() {
        try {
            shops.clear();
            Set<WebShop> k = shopDeamons.keySet();
            for (Iterator i = k.iterator(); i.hasNext();) {
               shopDeamons.get((WebShop)i.next()).kill();
            }
            shops.addAll(DatabaseObject.getObjects(new WebShop(), null));
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
    }
}
