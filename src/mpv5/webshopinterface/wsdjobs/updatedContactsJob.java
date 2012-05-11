
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
import mpv5.db.common.QueryCriteria;
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
 * This job tries to fetch updated contacts + adresses of them
 */
public class updatedContactsJob implements WSDaemonJob {

    private final WSDaemon daemon;

    /**
     *  Create a new job
     * @param ddaemon
     */
    public updatedContactsJob(WSDaemon ddaemon) {
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
        try {
            Object d = client.getClient().invokeGetCommand(WSConnectionClient.COMMANDS.GET_CHANGED_CONTACTS.toString(),
                    new Object[]{}, new Object());
            List<Contact> obs = WSIManager.createObjects(d, new Contact());

            for (int i = 0; i < obs.size(); i++) {
                Contact contact = obs.get(i);
                int id = contact.__getIDS();
                WSContactsMapping m = null;

                try {
                    m = WSContactsMapping.getMapping(daemon.getWebShopID(), id);
                    contact.setIDS(m.__getContactsids());
                    contact.save();
                } catch (NodataFoundException ex) {
                    throw new UnsupportedOperationException("Invalid contact mapping found: " + id);
                }

                Object da =
                        client.getClient().invokeGetCommand(WSConnectionClient.COMMANDS.GET_CHANGED_ADRESSES.toString(),
                        new Object[]{m.__getWscontact()}, new Object());
                List<Address> aobs = WSIManager.createObjects(da, new Address());

                for (Address address : aobs) {
                    try {
                        QueryCriteria qs = new QueryCriteria("cname", address.__getCname());
                        qs.addAndCondition("prename", address.__getPrename());
                        qs.addAndCondition("contactsids", address.__getContactsids());

                        List<DatabaseObject> old = DatabaseObject.getObjects(Context.getAddress(), qs);

                        for (int ix = 0; ix < old.size(); ix++) {
                            old.get(ix).delete();
                        }

                        address.setContactsids(m.__getContactsids());
                        address.saveImport();
                    } catch (NodataFoundException ex) {
                        Log.Debug(ex);
                    }
                }
            }
            if (Log.getLoglevel() == Log.LOGLEVEL_DEBUG) {
                Popup.notice(obs, "Updated contacts");
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

