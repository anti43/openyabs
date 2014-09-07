
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
import mpv5.db.objects.Address;
import mpv5.db.objects.Contact;
import mpv5.db.objects.WSContactsMapping;

import mpv5.logging.Log;

import mpv5.ui.frames.MPView; 
import mpv5.webshopinterface.WSConnectionClient;
import mpv5.webshopinterface.WSDaemon;
import mpv5.webshopinterface.WSDaemonJob;
import mpv5.webshopinterface.WSIManager;

import org.apache.xmlrpc.XmlRpcException;

//~--- JDK imports ------------------------------------------------------------

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.ui.dialogs.Popup;

/**
 * This job tries to fetch new contacts + adresses of them
 */
public class getContactJob implements WSDaemonJob {

    private final WSDaemon daemon;
    private final String wscontactid;

    /**
     *  Create a new job
     * @param ddaemon
     */
    public getContactJob(WSDaemon ddaemon, String wscontactid) {
        this.daemon = ddaemon;
        this.wscontactid = wscontactid;
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
        try {
            Object d = client.getClient().invokeGetCommand(WSConnectionClient.COMMANDS.GET_CONTACT.toString(),
                    new Object[]{wscontactid,
                        Boolean.TRUE}, new Object());
            List<Contact> obs = WSIManager.createObjects(d, new Contact());

            for (int i = 0; i < obs.size(); i++) {
                Contact contact = obs.get(i);
                int id = contact.__getIDS();
                WSContactsMapping m;

                try {    // Check if the mapping already exists
                    m = (WSContactsMapping) WSContactsMapping.getObject(Context.getWebShopContactMapping(),
                            String.valueOf(id) + "@" + daemon.getWebShopID());
                    Log.Debug(this,
                            "Using exiting mapping to: " + contact.__getIDS() + ". Not going to create " + contact);
                } catch (NodataFoundException ex) {
                    contact.setGroupsids(daemon.getWebShop().__getGroupsids());
                    contact.saveImport();

                    // If not, create one
                    m = new WSContactsMapping();
                    m.setContactsids(contact.__getIDS());
                    m.setWscontact(String.valueOf(id));
                    m.setCname(String.valueOf(id) + "@" + daemon.getWebShopID());
                    m.setWebshopsids(daemon.getWebShopID());
                    m.setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
                    m.save();
                }
            }

            Object da = client.getClient().invokeGetCommand(WSConnectionClient.COMMANDS.GET_ADDRESSES.toString(),
                    new Object[]{wscontactid}, new Object());
            List<Address> aobs = WSIManager.createObjects(da, new Address());

            for (Address address : aobs) {
                try {
                    WSContactsMapping m = WSContactsMapping.getMapping(daemon.getWebShopID(),
                            address.__getContactsids());

                    address.setContactsids(m.__getContactsids());
                    address.saveImport();
                } catch (NodataFoundException ex) {
                    Log.Debug(this, ex.getMessage());
                }
            }

            if (Log.getLoglevel() == Log.LOGLEVEL_DEBUG) {
                Popup.notice(obs, "Saved contacts");
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

