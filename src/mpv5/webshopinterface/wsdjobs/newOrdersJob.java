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
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.WSItemsMapping;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
import mpv5.webshopinterface.WSConnectionClient;
import mpv5.webshopinterface.WSDaemon;
import mpv5.webshopinterface.WSDaemonJob;
import mpv5.webshopinterface.WSIManager;
import org.apache.xmlrpc.XmlRpcException;

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
        try {
            Object d = client.getClient().invokeGetCommand(WSConnectionClient.COMMANDS.GET_NEW_ORDERS.toString(), new Object[]{new Date(0l), new Date()}, new Object());
            List<Item> obs = WSIManager.createObjects(d, new Item());

            for (Item order : obs) {
                order.save();
                WSItemsMapping m = new WSItemsMapping();
                m.setItemsids(order.__getIDS());
                m.setWsitem(String.valueOf(order.__getIDS()));
                m.setCName(String.valueOf(order.__getIDS()));
                m.setWebshopsids(daemon.getWebShopID());
                m.setGroupsids(MPView.getUser().__getGroupsids());
                m.save();
            }

            Object da = client.getClient().invokeGetCommand(WSConnectionClient.COMMANDS.GET_NEW_ORDER_ROWS.toString(), new Object[]{new Date(0l), new Date()}, new Object());
            List<SubItem> aobs = WSIManager.createObjects(da, new SubItem());

            for (SubItem orderRow : aobs) {
                try {
                    WSItemsMapping m = (WSItemsMapping) DatabaseObject.getObject(Context.getWebShopItemMapping(), String.valueOf(orderRow.__getItemsids()));
                    orderRow.setItemsids(m.__getItemsids());
                    orderRow.save();
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            }
        } catch (XmlRpcException ex) {
           Log.Debug(this, ex.getMessage());
        }
    }
}
