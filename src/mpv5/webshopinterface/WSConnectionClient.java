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
import mpv5.logging.Log;
import mpv5.utils.xml.XMLRpcClient;
import org.apache.xmlrpc.XmlRpcException;

/**
 *This class acts as connector to webshops using the WebShopAPI specified at
 * http://code.google.com/p/mp-rechnungs-und-kundenverwaltung/wiki/WebShopInterfaceSpecs
 */
public class WSConnectionClient {

    private XMLRpcClient client;

    /**
     * @return the client
     */
    public XMLRpcClient getClient() {
        return client;
    }

    public String test() throws XmlRpcException {
         Object[] params = new Object[0];
         Integer v = (Integer) getClient().execute(COMMANDS.GETVERSION.toString(), params);
         return "Server XML RPC Yabs Version : " + v;
    }

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
        GET_CHANGED_ORDER_ROWS("getUpdatedOrderRows"),
        ////////////////////////////////////////////////////////////////////////
        ADD_NEW_PRODUCTS("addNewProducts"),
        ADD_NEW_CONTACTS("addNewContacts"),
        SET_ORDER_STATUS("setOrderStatus"),
        SET_DISABLED_CONTACTS("setDisabledContacts"),
        SET_DISABLED_PRODUCTS("setDisabledProducts");

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

    private boolean connect(URL host) {
        client = new XMLRpcClient(host);
        try {
            Log.Debug(this, test());
            return true;
        } catch (XmlRpcException ex) {
            Log.Debug(this, ex.getMessage());
            return false;
        }
    }
}
