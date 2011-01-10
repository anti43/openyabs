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
package enoa.connection;

import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.application.OfficeApplicationRuntime;
import ag.ion.bion.officelayer.desktop.IDesktopService;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.internal.document.DocumentService;
import ag.ion.noa.NOAException;
import com.mysql.jdbc.log.CommonsLogger;
import com.sun.star.auth.InvalidArgumentException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.Main;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;
import mpv5.utils.files.FileExecutor;
import ooo.connector.server.OOoServer;

/**
 *This class handles connections to remote and local OpenOffice installations
 */
public class NoaConnection {

    /**
     * Indicates a Local OO Installation.
     */
    public static final int TYPE_LOCAL = 0;
    /**
     * Indicates a remote OO installation.
     */
    public static final int TYPE_REMOTE = 1;
    private IOfficeApplication officeAplication;
    private int type = -1;
    private IDocumentService documentService;
    private IDesktopService desktopService;
    private static NoaConnection Connection;

    /**
     * Creates a connection, depending on the current local config.
     * @return
     */
    public synchronized static NoaConnection getConnection() {
        if (LocalSettings.hasProperty(LocalSettings.OFFICE_HOST)) {
            if (Connection == null) {
                try {
//                    if (LocalSettings.getBooleanProperty(LocalSettings.OFFICE_REMOTE)) {
                    Connection = new NoaConnection(LocalSettings.getProperty(LocalSettings.OFFICE_HOST), Integer.valueOf(LocalSettings.getProperty(LocalSettings.OFFICE_PORT)));
//                    } else {
//                        Connection = new NoaConnection(LocalSettings.getProperty(LocalSettings.OFFICE_HOST), 0);
//                    }
                } catch (Exception ex) {
                    mpv5.logging.Log.Debug(ex);//Logger.getLogger(NoaConnection.class.getName()).log(Level.SEVERE, null, ex);
                    Popup.error(mpv5.YabsViewProxy.instance().getIdentifierFrame(), Messages.OOCONNERROR + "\n" + ex);
                }
            }
            return Connection;
        } else {
            throw new UnsupportedOperationException("OpenOffice is not configured yet.");
        }
    }

    /**
     * New NoaConnection instance, connects to OO using the given parameters.
     * @param connectionString The connection String. Can be a <b>Path<b/> or an <b>IP<b/>
     * @param port The port to connect to. A port value of zero (0) indicates a <b>local<b/> connection
     * @throws Exception If any Exception is thrown during the connection attempt
     */
    public NoaConnection(String connectionString, int port) throws Exception {
        Log.Debug(this, connectionString);
        if (connectionString != null && connectionString.length() > 1 && port <= 0) {
            createLocalConnection(connectionString);
        } else if (connectionString != null && connectionString.length() > 1 && port > 0 && port < 9999) {
            createServerConnection(connectionString, port);
        } else {
            throw new Exception("Connection not possible with the given parameters: [" + connectionString + ":" + port + "]");
        }
    }

    /**
     * Creates a new connection
     * @param host
     * @param port
     * @return
     * @throws OfficeApplicationException
     * @throws NOAException
     * @throws InvalidArgumentException
     */
    private synchronized boolean createServerConnection(String host, int port) throws OfficeApplicationException, NOAException, InvalidArgumentException {
        if (host != null && port > 0) {
            Map<String, String> configuration = new HashMap<String, String>();
            configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY,
                    IOfficeApplication.REMOTE_APPLICATION);
            configuration.put(IOfficeApplication.APPLICATION_HOST_KEY, host.replace("http://", ""));
            configuration.put(IOfficeApplication.APPLICATION_PORT_KEY, String.valueOf(port));

            officeAplication =
                    OfficeApplicationRuntime.getApplication(configuration);
            officeAplication.setConfiguration(configuration);
            try {
                officeAplication.activate();
            } catch (Exception officeApplicationException) {
                try {
                    Thread.sleep(6666);
                } catch (InterruptedException ex) {
                }
                try {
                    officeAplication.activate();
                } catch (OfficeApplicationException officeApplicationException1) {
                    Log.Debug(officeApplicationException);
                    Popup.error(officeApplicationException);
                }
            }
            documentService =
                    officeAplication.getDocumentService();
            desktopService =
                    officeAplication.getDesktopService();
            setType(TYPE_REMOTE);
        } else {
            throw new InvalidArgumentException("Host cannot be null and port must be > 0: " + host + ":" + port);
        }

        return true;
    }

    /**
     *
     * @param OOOPath
     * @return
     * @throws OfficeApplicationException
     * @throws NOAException
     * @throws InvalidArgumentException
     */
    private synchronized boolean createLocalConnection(String OOOPath) throws OfficeApplicationException, NOAException, InvalidArgumentException {
        if (OOOPath != null) {
            Map<String, String> configuration = new HashMap<String, String>();
            configuration.put(IOfficeApplication.APPLICATION_HOME_KEY, OOOPath);
            configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY,
                    IOfficeApplication.LOCAL_APPLICATION);

            officeAplication =
                    OfficeApplicationRuntime.getApplication(configuration);
            officeAplication.setConfiguration(configuration);
            officeAplication.activate();
            documentService =
                    officeAplication.getDocumentService();
            desktopService =
                    officeAplication.getDesktopService();

            setType(TYPE_LOCAL);
        } else {
            throw new InvalidArgumentException("Path to OO cannot be null: " + OOOPath);
        }

        return true;
    }

    /**
     *  -1 indicates no connection.
     * @return the type
     */
    public synchronized int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    private synchronized void setType(int type) {
        this.type = type;
    }

    /**
     * @return the documentService
     */
    public synchronized IDocumentService getDocumentService() {
        return documentService;
    }

    /**
     * @return the desktopService
     */
    public synchronized IDesktopService getDesktopService() {
        return desktopService;
    }

      /**
     * Tries to start OO in headless server mode. <code>Give it at least 3-4 seconds before attempting to use the server.</code>
     * @param path The path where the OO installation resides
     * @param port The port the server shall listen to
     * @throws IOException
     */
    public synchronized static void startOOServerIfNotRunning(String path, int port) {

        final String command = path.replace("\\", "\\\\") + File.separator + LocalSettings.getProperty(LocalSettings.OFFICE_BINARY_FOLDER) + File.separator + "soffice" + " "
                + "-headless" + " "
                + "-nofirststartwizard" + " "
                + "-norestore" + " "
                + "-nolockcheck" + " "
                + "-nocrashreport" + " "
                + "-nodefault" + " "
                + "-accept='socket,host=0.0.0.0,port=" + port + ";urp;StarOffice.Service'";

        try {
            SocketAddress addr = new InetSocketAddress("127.0.0.1", port);
            Socket socket = new Socket();
            socket.connect(addr, 100);
            throw new UnsupportedOperationException("Port " + port + " is already in use! Not going to start OO here.");
        } catch (IOException iOException) {
            //nothing is running here
        }

        Runnable runnable = new Runnable() {
            public void run() {
                FileExecutor.run(command);
            }
        };

        Thread t = new Thread(runnable);
        t.setDaemon(true);
        t.start();

    }

    /**
     * Tries to start OO in headless server mode. <code>Give it at least 3-4 seconds before attempting to use the server.</code>
     * @param path The path where the OO installation resides
     * @param port The port the server shall listen to
     * @throws IOException
     */
    public synchronized static void startOOServer(String path, int port) throws IOException {
        final ProcessBuilder builder = new ProcessBuilder(
                path.replace("\\", "\\\\") + File.separator + LocalSettings.getProperty(LocalSettings.OFFICE_BINARY_FOLDER) + File.separator + "soffice",
                "-headless",
                "-nofirststartwizard",
                //                "-invisible",
                "-norestore",
                "-nolockcheck",
                "-nocrashreport",
                "-nodefault",
                "-accept=socket,host=0.0.0.0,port=" + port + ";urp;StarOffice.Service");

        Map<String, String> environment = builder.environment();
        environment.put("path", ";"); // Clearing the path variable;
        environment.put("path", path.replace("\\", "\\\\") + File.pathSeparator);

        String command = "";
        for (int i = 0; i
                < builder.command().size(); i++) {
            Object object = builder.command().get(i);
            command += object + " ";
        }

        Log.Debug(NoaConnection.class, command);
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    Process oos = builder.start();
                    OOOServers.add(oos);
                    InputStream is = oos.getErrorStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String line;

                    while ((line = br.readLine()) != null) {
                        mpv5.logging.Log.Print(line);
                    }
                } catch (IOException ex) {
                    stopOOOServer();
                    mpv5.logging.Log.Debug(ex);
                }
            }
        };
        new Thread(runnable).start();
    }
    private static List<Process> OOOServers = new Vector<Process>();

    /**
     * Stops all OOO servers instances started by this instance
     */
    public synchronized static void stopOOOServer() {
        for (int i = 0; i < OOOServers.size(); i++) {
            Process process = OOOServers.get(i);
            process.destroy();
        }
    }
}
