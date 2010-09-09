
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

//~--- non-JDK imports --------------------------------------------------------

import mpv5.globals.Constants;

import mpv5.logging.Log;

import mpv5.ui.dialogs.Popup;

import mpv5.utils.xml.XMLRpcClient;

import org.apache.xmlrpc.XmlRpcException;

//~--- JDK imports ------------------------------------------------------------

import java.net.URL;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * This class acts as connector to webshops using the WebShopAPI specified at
 * http://code.google.com/p/mp-rechnungs-und-kundenverwaltung/wiki/WebShopInterfaceSpecs
 */
public class WSConnectionClient {
    private XMLRpcClient client;

    /**
     * Create a new connection to the specified url.<br/>
     * e.g. new URL("http://127.0.0.1:8080/xmlrpc")
     * @param host
     * @param requCompression
     * @throws NoCompatibleHostFoundException
     */
    public WSConnectionClient(final URL host, boolean requCompression, String user, String pw)
            throws NoCompatibleHostFoundException {
        if (!connect(host, requCompression, user, pw)) {
            throw new NoCompatibleHostFoundException(host);
        }
    }

    /**
     * Contains all known xml-rpc commands
     */
    public static enum COMMANDS {
        GETVERSION("getYWSIVersion"), GET_NEW_CONTACTS("getNewContacts"), GET_CONTACT("getContact"),
        GET_NEW_ADDRESSES("getNewAddresses"), GET_ADDRESSES("getAddresses"), GET_NEW_ORDERS("getNewOrders"),
        GET_ORDER_ROWS("getOrderRows"),

        // //////////////////////////////////////////////////////////////////////
        GET_NEW_SYSTEM_MESSAGES("getNewSystemMessages"), GET_CHANGED_CONTACTS("getUpdatedContacts"),
        GET_CHANGED_ADRESSES("getUpdatedAdresses"), GET_CHANGED_ORDERS("getUpdatedOrders"),
        GET_CHANGED_ORDER_ROWS("getUpdatedOrderRows"),

        // //////////////////////////////////////////////////////////////////////
        ADD_NEW_PRODUCT("addNewProduct"), ADD_NEW_CONTACT("addNewContact"), SET_ORDER_STATUS("setOrderStatus"),
        SET_DISABLED_CONTACT("setDisabledContact"), SET_DISABLED_PRODUCT("setDisabledProduct");

        String command;

        private COMMANDS(String command) {
            this.command = command;
        }

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
     * @return the client
     */
    public XMLRpcClient getClient() {
        return client;
    }

    /**
     * Test the shop & runs system.listMethods
     * @return The shop impl version
     * @throws XmlRpcException If the test fails
     */
    @SuppressWarnings("unchecked")
    public String test() throws XmlRpcException {
        Object   o          = client.execute("system.listMethods", new Object[0]);
        Object[] methodList = (Object[]) o;

        Arrays.sort(methodList);
        Popup.notice(Arrays.asList(methodList), "Supported methods:\n");

        Object[] params = new Object[] { Constants.RELEASE_VERSION };
        Integer  v      = (Integer) getClient().execute(COMMANDS.GETVERSION.toString(), params);

        return "Server XML RPC Yabs Version : " + v;
    }

    private boolean connect(URL host, boolean requCompression, String user, String pw) {
        client = new XMLRpcClient(host, requCompression, user, pw);

        try {
            Log.Debug(this, test());

            return true;
        } catch (XmlRpcException ex) {
            Log.Debug(this, ex.getMessage());

            return false;
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
