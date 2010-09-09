
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

//~--- non-JDK imports --------------------------------------------------------

import mpv5.logging.Log;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

//~--- JDK imports ------------------------------------------------------------

import java.net.URL;

import java.util.HashMap;

/**
 * Our XML RPC Client implementation
 */
public class XMLRpcClient extends XmlRpcClient {

//  private final XmlRpcCommonsTransportFactory transport;

    /**
     * Generate a new client for the given host
     * @param host
     * @param requCompression
     */
    public XMLRpcClient(URL host, boolean requCompression, String basicAuthUsername, String basicAuthPaswword) {
        super();

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();

        config.setServerURL(host);
        config.setGzipRequesting(requCompression);
        config.setEnabledForExtensions(requCompression);

        if ((basicAuthUsername != null) && (basicAuthPaswword != null)) {
            Log.Debug(this, "Using basic authentication, with username: " + basicAuthUsername);
            config.setBasicUserName(basicAuthUsername);
            config.setBasicPassword(basicAuthPaswword);
        }

//      transport = new XmlRpcCommonsTransportFactory(this);
//      setTransportFactory(transport);
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
    public <T extends Object> T invokeGetCommand(String commandName, Object[] params, T expectedReturnType)
            throws XmlRpcException {
        Object data = execute(commandName, params);

        Log.Debug(this,
                  "RPC call to '" + ((XmlRpcClientConfigImpl) getClientConfig()).getServerURL() + "#" + commandName
                  + "' returned a: " + data.getClass().getSimpleName() + " [" + data + "]");

        return (T) data;
    }

    /**
     * Invoke a remote get command
     * @param commandName The name of the remote procedure
     * @param params The parameters to the remote procedure
     * @return The response
     * @throws XmlRpcException If any error occurs
     */
    @SuppressWarnings("unchecked")
    public HashMap<String, Object> invokeGetCommand(String commandName, Object[] params) throws XmlRpcException {
        HashMap<String, Object> data = (HashMap<String, Object>) execute(commandName, params);

        Log.Debug(this,
                  "RPC call to '" + ((XmlRpcClientConfigImpl) getClientConfig()).getServerURL() + "#" + commandName
                  + "' returned " + data.size() + " values.");

        return data;
    }

    /**
     * Invoke a remote set command
     * @param commandName
     * @param params
     * @return
     * @throws XmlRpcException
     */
    public Object invokeSetCommand(String commandName, Object[] params) throws XmlRpcException {
        Object result = execute(commandName, params);

        Log.Debug(this,
                  "RPC call to '" + ((XmlRpcClientConfigImpl) getClientConfig()).getServerURL() + "#" + commandName
                  + "' returned a: " + result.getClass().getSimpleName() + " [" + result + "]");

        return result;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
