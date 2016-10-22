
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
package mpv5.webshopinterface.wsdjobs;

//~--- non-JDK imports --------------------------------------------------------
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.WSContactsMapping;
import mpv5.db.objects.WSItemsMapping;

import mpv5.globals.Messages;

import mpv5.logging.Log;

import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;

import mpv5.webshopinterface.WSConnectionClient;
import mpv5.webshopinterface.WSDaemon;
import mpv5.webshopinterface.WSDaemonJob;
import mpv5.webshopinterface.WSIManager;

import org.apache.xmlrpc.XmlRpcException;

//~--- JDK imports ------------------------------------------------------------

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This job tries to fetch new orders
 */
public class newOrdersJob implements WSDaemonJob {

    private final WSDaemon daemon;

    /**
     *  Create a new job
     * @param ddaemon
     */
    public newOrdersJob(WSDaemon ddaemon) {
        this.daemon = ddaemon;
    }

    @Override
    public boolean isOneTimeJob() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public void work(WSConnectionClient client) {
        WSContactsMapping md;
        Contact c;
        List<Item> savedOrders = new Vector<Item>();

        try {
            Object itd = WSItemsMapping.getLastWsID(daemon.getWebShop());
            Object d = client.getClient().invokeGetCommand(WSConnectionClient.COMMANDS.GET_NEW_ORDERS.toString(),
                    new Object[]{itd}, new Object());
            List<Item> obs = WSIManager.createObjects(d, new Item());

            for (Item order : obs) {
                int id = order.__getContactsids();

                if (id == -1) {    // Guestaccount
                    Object d2 = client.getClient().invokeGetCommand(WSConnectionClient.COMMANDS.GET_CONTACT.toString(),
                            new Object[]{order.__getIDS(),
                                Boolean.FALSE}, new Object());
                    List<Contact> obs2 = WSIManager.createObjects(d2, new Contact());

                    c = obs2.get(0);
                    c.setNotes(c.__getNotes() + "\nAuto generated guest account for webshop: "
                            + daemon.getWebShop().__getCname());
                    c.setMailaddress("NO-MAILS " + c.__getMailaddress());
                    c.setGroupsids(daemon.getWebShop().__getGroupsids());
                    c.saveImport();
                } else {
                    try {    // Look the contact up
                        md = (WSContactsMapping) WSContactsMapping.getObject(Context.getWebShopContactMapping(),
                                String.valueOf(id) + "@" + daemon.getWebShopID());

                        try {
                            c = (Contact) DatabaseObject.getObject(Context.getContact(), md.__getContactsids());
                        } catch (NodataFoundException nodataFoundException) {
                            throw new UnsupportedOperationException("Invalid contact mapping found: " + md);
                        }
                    } catch (NodataFoundException ex) {

                        // Not yet created
                        Object d2 =
                                client.getClient().invokeGetCommand(WSConnectionClient.COMMANDS.GET_CONTACT.toString(),
                                new Object[]{id,
                                    Boolean.TRUE}, new Object());
                        List<Contact> obs2 = WSIManager.createObjects(d2, new Contact());

                        c = obs2.get(0);
                        c.setGroupsids(daemon.getWebShop().__getGroupsids());
                        c.saveImport();
                        md = new WSContactsMapping();
                        md.setContactsids(c.__getIDS());
                        md.setWscontact(String.valueOf(id));
                        md.setCname(String.valueOf(id) + "@" + daemon.getWebShopID());
                        md.setWebshopsids(daemon.getWebShopID());
                        md.setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
                        md.save();
                    }
                }

                int wsitemids = order.__getIDS();

                order.setContactsids(c.__getIDS());
                order.setDescription(order.__getDescription() + "\nImport: " + daemon.getWebShop());
                order.setGroupsids(daemon.getWebShop().__getGroupsids());
                order.save();
                savedOrders.add(order);

                WSItemsMapping m = new WSItemsMapping();

                m.setItemsids(order.__getIDS());
                m.setWsitem(String.valueOf(wsitemids));
                m.setCname(String.valueOf(order.__getIDS() + "@" + daemon.getWebShopID()));
                m.setWebshopsids(daemon.getWebShopID());
                m.setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
                m.saveImport();

                // Fetch the order details
                Object da = client.getClient().invokeGetCommand(WSConnectionClient.COMMANDS.GET_ORDER_ROWS.toString(),
                        new Object[]{String.valueOf(wsitemids)}, new Object());
                List<SubItem> aobs = WSIManager.createObjects(da, new SubItem());

                for (SubItem orderRow : aobs) {
                    try {
                        WSItemsMapping mk = (WSItemsMapping) DatabaseObject.getObject(Context.getWebShopItemMapping(),
                                String.valueOf(orderRow.__getItemsids()));

                        orderRow.setItemsids(mk.__getItemsids());
                        orderRow.setGroupsids(daemon.getWebShop().__getGroupsids());
                        orderRow.saveImport();
                    } catch (NodataFoundException ex) {
                        Log.Debug(ex);
                    }
                }
            }

            if (obs.size() > 0) {
                mpv5.YabsViewProxy.instance().addMessage(obs.size() + " " + Messages.ORDERS_RECEIVED + " " + daemon.getWebShop());

                if (Popup.Y_N_dialog(obs.size() + " " + Messages.ORDERS_RECEIVED + " " + daemon.getWebShop() + "\n"
                        + Messages.LOAD_NOW)) {
                    for (Item s : savedOrders) {
                        try {
                            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(DatabaseObject.getObject(Context.getOrder(),
                                    s.__getIDS()));
                        } catch (NodataFoundException ex) {
                            Log.Debug(ex);    // Something must have failed during the import process
                            Popup.error(ex);
                        }
                    }
                }
            }
        } catch (XmlRpcException ex) {
            Log.Debug(this, ex.getMessage());
            if (Log.getLoglevel() == Log.LOGLEVEL_DEBUG) {
                Popup.error(ex);
            }
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com

