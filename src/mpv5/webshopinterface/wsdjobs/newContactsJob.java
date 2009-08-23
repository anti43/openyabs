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

import java.util.Date;
import java.util.List;
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

/**
 * This job tries to fetch new contacts + adresses of them
 */
public class newContactsJob implements WSDaemonJob {

    private final WSDaemon daemon;

    /**
     *  Create a new job
     * @param ddaemon 
     */
    public newContactsJob(WSDaemon ddaemon) {
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
        Object itd = WSContactsMapping.getLastWsID(daemon.getWebShop());
        try {
            Object d = client.getClient().invokeGetCommand(WSConnectionClient.COMMANDS.GET_NEW_CONTACTS.toString(), 
                    new Object[]{itd}, new Object());
            List<Contact> obs = WSIManager.createObjects(d, new Contact());
            for (int i = 0; i < obs.size(); i++) {
                Contact contact = obs.get(i);
                int id = contact.__getIDS();
                contact.setIDS(-1);
                contact.save();
                WSContactsMapping m = new WSContactsMapping();
                m.setContactsids(contact.__getIDS());
                m.setWscontact(String.valueOf(id));
                m.setCName( String.valueOf(id) + "@" + daemon.getWebShopID());
                m.setWebshopsids(daemon.getWebShopID());
                m.setGroupsids(MPView.getUser().__getGroupsids());
                m.save();
            }

            Object da = client.getClient().invokeGetCommand(WSConnectionClient.COMMANDS.GET_NEW_ADRESSES.toString(),  new Object[]{itd}, new Object());
            List<Address> aobs = WSIManager.createObjects(da, new Address());

            for (Address address : aobs) {
                try {
                    WSContactsMapping m = WSContactsMapping.getMapping(daemon.getWebShopID(), address.__getContactsids());
                    address.setContactsids(m.__getContactsids());
                    address.save();
                } catch (NodataFoundException ex) {
                    Log.Debug(this, ex.getMessage());
                }
            }
        } catch (XmlRpcException ex) {
            Log.Debug(this, ex.getMessage());
        }
    }
}
