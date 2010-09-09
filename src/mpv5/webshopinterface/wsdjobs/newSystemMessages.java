
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

import mpv5.logging.Log;

import mpv5.webshopinterface.WSConnectionClient;
import mpv5.webshopinterface.WSDaemon;
import mpv5.webshopinterface.WSDaemonJob;

import org.apache.xmlrpc.XmlRpcException;

/**
 * This job tries to fetch new msgs
 */
public class newSystemMessages implements WSDaemonJob {
    private final WSDaemon daemon;

    /**
     *  Create a new job
     * @param ddaemon
     */
    public newSystemMessages(WSDaemon ddaemon) {
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
            Object d =
                client.getClient().invokeGetCommand(WSConnectionClient.COMMANDS.GET_NEW_SYSTEM_MESSAGES.toString(),
                    new Object[] { itd }, new Object());

            Log.Debug(this, d);

            // List<ItemMessage> obs = WSIManager.createObjects(d, new ItemMessage());
//
//          for (ItemMessage itemMessage : obs) {
//              Popup.notice(itemMessage);
//          }
        } catch (XmlRpcException ex) {
            Log.Debug(this, ex.getMessage());
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
