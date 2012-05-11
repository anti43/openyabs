
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

import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.WSContactsMapping;
import mpv5.db.objects.WebShop;

import mpv5.globals.Messages;

import mpv5.logging.Log;

import mpv5.ui.frames.MPView;

import mpv5.utils.arrays.ArrayUtilities;

import mpv5.webshopinterface.WSConnectionClient;
import mpv5.webshopinterface.WSDaemon;
import mpv5.webshopinterface.WSDaemonJob;
import mpv5.webshopinterface.WSIManager;

import org.apache.xmlrpc.XmlRpcException;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pushes contacts to a webshop
 */
public class addContactJob implements WSDaemonJob {
    private final WSDaemon daemon;
    private boolean        done;

    /**
     *  Create a new job
     * @param ddaemon
     */
    public addContactJob(WSDaemon ddaemon) {
        this.daemon = ddaemon;
    }

    public boolean isOneTimeJob() {
        return true;
    }

    public boolean isDone() {
        return done;
    }

    public void work(WSConnectionClient client) {
        List<Contact> data = WSContactsMapping.getUnmappedContacts(daemon.getWebShop());

        try {
            for (Contact c : data) {
                Object id = client.getClient().invokeSetCommand(WSConnectionClient.COMMANDS.ADD_NEW_CONTACT.toString(),
                                ArrayUtilities.inserValue(c.getValues2().toArray(new Object[0][]), c.__getIDS(), -1));
                WSContactsMapping ws = new WSContactsMapping();

                ws.setContactsids(c.__getIDS());
                ws.setCname(String.valueOf(id) + "@" + daemon.getWebShopID());
                ws.setWebshopsids(daemon.getWebShopID());
                ws.setWscontact(String.valueOf(id));
                ws.setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
                ws.save();
            }

            if (!data.isEmpty()) {
                mpv5.YabsViewProxy.instance().addMessage(data.size() + " " + Messages.CONTACTS_TRANSMITTED.toString() + " "
                                  + daemon.getWebShop());
            }
        } catch (XmlRpcException ex) {
            Log.Debug(this, ex.getMessage());
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
