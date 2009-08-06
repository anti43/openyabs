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

import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.logging.Log;
import mpv5.utils.http.HttpClient;
import org.apache.http.*;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.omg.CORBA.DATA_CONVERSION;

/**
 *This class acts as connector to webshops using the WebShopAPI specified at
 * http://code.google.com/p/mp-rechnungs-und-kundenverwaltung/wiki/WebShopInterfaceSpecs
 */
public class WSConnectionClient {

    private XmlRpcClient client;

    /**
     * Contains all known xml-rpc commands
     */
    public static enum COMMANDS {

        GETVERSION("getYWSIVersion"),
        GET_NEW_CONTACTS("getNewContacts"),
        GET_NEW_ADRESSES("getNewAdresses"),
        GET_NEW_ORDERS("getNewOrders"),
        GET_NEW_ORDER_ROWS("getOrderRows"),
        GET_NEW_SYSTEM_MESSAGES("getNewSystemMessages"),
        GET_CHANGED_CONTACTS("getUpdatedContacts"),
        GET_CHANGED_ADRESSES("getUpdatedAdresses"),
        GET_CHANGED_ORDERS("getUpdatedOrders"),
        GET_CHANGED_ORDER_ROWS("getUpdatedOrderRows");

        private COMMANDS(String command) {
            this.command = command;
        }
        String command;

        /**
         * Overriden to return the actual command
         * @return The command specified in this enum
         */
        @Override
        public String toString() {
            return command;
        }
    }

    /**
     * Create a new connection to the specified url.<br/>
     * e.g. new URL("http://127.0.0.1:8080/xmlrpc")
     * @param host
     * @throws NoCompatibleHostFoundException 
     */
    public WSConnectionClient(final URL host) throws NoCompatibleHostFoundException {
        if (!connect(host)) {
            throw new NoCompatibleHostFoundException(host);
        }
    }

    /**
     * Invoke a get command
     * @param commandName
     * @return
     * @throws XmlRpcException
     */
    public Object[][] invokeGetCommand(String commandName) throws XmlRpcException {
        if (client != null) {
            Object[] params = new Object[]{3, 5};
            Vector result = (Vector) client.execute(commandName, params);

            Object[][] data = new Object[result.size()][2];
            for (int i = 0; i < result.size(); i++) {
                Log.Debug(this, result.get(i));
//                data[i] = (Object[]) result.elementAt(i);
            }
            return data;
        }
        return null;
    }

    private boolean connect(URL host) {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(host);
        client = new XmlRpcClient();
        client.setConfig(config);
        Object[] params = new Object[0];
        try {
            Boolean result = (Boolean) client.execute(COMMANDS.GETVERSION.toString(), params);
            return result;
        } catch (XmlRpcException ex) {
            Log.Debug(this, ex.getMessage());
            return true;
        }
    }
}
