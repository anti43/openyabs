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
package mpv5.utils.xml;

import java.net.URL;
import mpv5.logging.Log;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

/**
 * Our XML RPC Client implementation
 */
public class XMLRpcClient extends XmlRpcClient {
    private final XmlRpcCommonsTransportFactory transport;

    /**
     * Generate a new client for the given host
     * @param host
     */
    public XMLRpcClient(URL host) {
        super();
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(host);
        transport = new XmlRpcCommonsTransportFactory(this);
        setTransportFactory(transport);
        setConfig(config);

    }

    /**
     * Invoke a remote get command
     * @param <T>
     * @param commandName The name of the remote procedure
     * @param params The parameters to the remote procedure
     * @param expectedReturnType The type you expect to get back
     * @return The response
     * @throws XmlRpcException If any error occurs
     */
    @SuppressWarnings("unchecked")
    public <T extends Object> T invokeGetCommand(String commandName, Object[] params, T expectedReturnType) throws XmlRpcException {
        Object data = execute(commandName, params);
        Log.Debug(this, "RPC call to '" + ((XmlRpcClientConfigImpl) getClientConfig()).getServerURL() + "#" + commandName + "' returned a: " + data.getClass().getSimpleName());
        return (T) data;
    }

    /**
     * Invoke a remote set command
     * @param commandName
     * @param params
     * @return
     * @throws XmlRpcException
     */
    public boolean invokeSetCommand(String commandName, Object[] params) throws XmlRpcException {
            Boolean result = (Boolean) execute(commandName, params);
            Log.Debug(this, "RPC call to '" + ((XmlRpcClientConfigImpl) getClientConfig()).getServerURL() + "#" + commandName + "' returned a: " + result.getClass().getSimpleName());
            return result;
    }
}
