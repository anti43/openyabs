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
package mpv5.server;

import java.util.ArrayList;
import mpv5.db.common.Context;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
import mpv5.usermanagement.MPSecurityManager;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfig;
import org.apache.xmlrpc.metadata.XmlRpcSystemImpl;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcErrorLogger;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.server.XmlRpcStreamServer;
import org.apache.xmlrpc.webserver.WebServer;

//Client-Side:
//XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
//config.setServerUrl("http://127.0.0.1:8080/xmlrpc");
//config.setBasicUserName("adst-test");
//config.setBasicPassword("adst-test#@!");
//XmlRpcClient client = new XmlRpcClient();
//client.setConfig(config);
/**
 * This class implements an XML-RPC server which is bound to all importable {@link Context}s
 */
public class XMLRPCServer {

    private static int port = 8484;
    private WebServer webServer;

    /**
     * @return the port
     */
    public static int getPort() {
        return port;
    }

    /**
     * @return the webServer
     */
    public WebServer getWebServer() {
        return webServer;
    }

    /**
     * Starts the server
     * @throws Exception
     */
    public XMLRPCServer() throws Exception {
        if (LocalSettings.hasProperty(LocalSettings.SERVER_PORT)) {
            port = Integer.valueOf(LocalSettings.getProperty(LocalSettings.SERVER_PORT));
        }
        webServer = new WebServer(getPort());
        XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
        ((XmlRpcStreamServer) xmlRpcServer).setErrorLogger(new XmlRpcErrorLogger() {

            @Override
            public void log(java.lang.String pMessage) {
                Log.Debug(this, pMessage);
            }

            @Override
            public void log(java.lang.String pMessage, java.lang.Throwable pThrowable) {
                Log.Debug(this, pMessage);
                Log.Debug(pThrowable);
            }
        });

        PropertyHandlerMapping phm = new XPropertyHandlerMapping();
//        ArrayList<Context> cx = Context.getImportableContexts();
//        for (int i = 0; i < cx.size(); i++) {
//            Context context = cx.get(i);
//        }
        phm.addHandler("remote", XMLRPCHandler.class);

        xmlRpcServer.setHandlerMapping(phm);

        XmlRpcServerConfigImpl serverConfig =
                (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
        serverConfig.setEnabledForExtensions(true);
        serverConfig.setContentLengthOptional(false);


        PropertyHandlerMapping mapping = (PropertyHandlerMapping)((XmlRpcStreamServer) xmlRpcServer).getHandlerMapping();
        XmlRpcSystemImpl.addSystemHandler(mapping);

        webServer.start();
        Log.Debug(this, "XML RPC Server started! Listening port: " + port);

        if (Log.getLoglevel() == Log.LOGLEVEL_DEBUG) {
            Log.Debug(this, "XML RPC Server handler: ");
            String[] lm = phm.getListMethods();
            for (int i = 0; i < lm.length; i++) {
                String string = lm[i];
                Log.Debug(this, string);
            }
        }
    }
}

class XPropertyHandlerMapping extends PropertyHandlerMapping {

    private boolean isAuthenticated(String pUserName, String pPassword) {
        Log.Debug(this, "XML RPC Server isAuthenticated?: " + pUserName);
        return MPSecurityManager.checkAuth(pUserName, pPassword) != null;
    }

    protected XmlRpcHandlerMapping newXmlRpcHandlerMapping() throws XmlRpcException {
        PropertyHandlerMapping mapping = (PropertyHandlerMapping) this;
        AbstractReflectiveHandlerMapping.AuthenticationHandler handler =
                new AbstractReflectiveHandlerMapping.AuthenticationHandler() {

                    @Override
                    public boolean isAuthorized(XmlRpcRequest pRequest) {
                        XmlRpcHttpRequestConfig config =
                                (XmlRpcHttpRequestConfig) pRequest.getConfig();
                        return isAuthenticated(config.getBasicUserName(),
                                config.getBasicPassword());
                    }
                };
        mapping.setAuthenticationHandler(handler);
        return mapping;
    }
}
