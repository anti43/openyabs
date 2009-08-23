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
import mpv5.db.common.QueryCriteria;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.WSItemsMapping;
import mpv5.logging.Log;
import mpv5.webshopinterface.WSConnectionClient;
import mpv5.webshopinterface.WSDaemon;
import mpv5.webshopinterface.WSDaemonJob;
import mpv5.webshopinterface.WSIManager;
import org.apache.xmlrpc.XmlRpcException;

/**
 * This job tries to fetch updated contacts + adresses of them
 */
public class updatedOrdersJob implements WSDaemonJob {

    private final WSDaemon daemon;

    /**
     *  Create a new job
     * @param ddaemon 
     */
    public updatedOrdersJob(WSDaemon ddaemon) {
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
        Object itd = "0";
        try {
            Object d = client.getClient().invokeGetCommand(WSConnectionClient.COMMANDS.GET_CHANGED_ORDERS.toString(),  new Object[]{itd}, new Object());
            List<Item> obs = WSIManager.createObjects(d, new Item());
            for (int i = 0; i < obs.size(); i++) {
                Item contact = obs.get(i);
                int id = contact.__getIDS();
                WSItemsMapping m;
                try {
                    m = WSItemsMapping.getMapping(daemon.getWebShopID(), id);
                    contact.setIDS(m.__getItemsids());
                    contact.save();
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
           
            }

            Object da = client.getClient().invokeGetCommand(WSConnectionClient.COMMANDS.GET_CHANGED_ORDER_ROWS.toString(),  new Object[]{itd}, new Object());
            List<SubItem> aobs = WSIManager.createObjects(da, new SubItem());

            for (SubItem orderRow : aobs) {
                try {
                    QueryCriteria qs = new QueryCriteria();
                    qs.add("itemsids", orderRow.__getItemsids());
                    List<DatabaseObject> old = DatabaseObject.getObjects(Context.getAddress(), qs);
                    for (int i = 0; i < old.size(); i++) {
                         old.get(i).delete();
                    }

                    WSItemsMapping m = WSItemsMapping.getMapping(daemon.getWebShopID(), orderRow.__getItemsids());
                    orderRow.setItemsids(m.__getItemsids());
                    orderRow.saveImport();
                } catch (NodataFoundException ex) {
                    Log.Debug(this, ex.getMessage());
                }
            }
        } catch (XmlRpcException ex) {
           Log.Debug(this, ex.getMessage());
        }
    }
}
