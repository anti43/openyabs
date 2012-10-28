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
import ag.ion.noa.NOAException;

import com.sun.star.auth.InvalidArgumentException;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.Main;
import mpv5.YabsViewProxy;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.files.FileExecutor;

/**
 * This class handles connections to remote and local OpenOffice installations
 */
public class NoaConnectionLocalServer extends NoaConnection {

    private static List<Process> ooProcesses = new ArrayList<Process>();

    /**
     * Creates a connection, depending on the current local config.
     *
     * @return
     */
    public synchronized static NoaConnection getConnection() {
        if (LocalSettings.hasProperty(LocalSettings.OFFICE_HOST)) {
            if (cachedConnection == null) {
                try {
                    cachedConnection = new NoaConnectionLocalServer(LocalSettings.getProperty(LocalSettings.OFFICE_HOST), Long.valueOf(LocalSettings.getProperty(LocalSettings.OFFICE_PORT)));
                } catch (Exception ex) {
                    mpv5.logging.Log.Debug(ex);//Logger.getLogger(NoaConnection.class.getName()).log(Level.SEVERE, null, ex);
                    YabsViewProxy.instance().addMessage(Messages.OOCONNERROR + "\n" + ex, Color.RED);
                }
                YabsViewProxy.instance().showOfficeStatus(cachedConnection != null, "Local server");
            }
            return cachedConnection;
        } else {
            throw new UnsupportedOperationException("OpenOffice is not configured yet.");
        }
    }

    /**
     * clears the onnnetion for testing puproses
     */
    public static void clearConnection() {
        cachedConnection = null;
    }

    /**
     * New NoaConnection instance, connects to OO using the given parameters.
     *
     * @param connectionString The connection String. Can be a <b>Path<b/> or an
     * <b>IP<b/>
     * @param port The port to connect to. A port value of zero (0) indicates a
     * <b>local<b/> connection
     * @throws Exception If any Exception is thrown during the connection
     * attempt
     */
    public NoaConnectionLocalServer(String connectionString, Long port) throws Exception {
        Log.Debug(this, connectionString);
        if (connectionString != null && connectionString.length() > 1 && port > 0 && port < 9999) {
            createServerConnection(connectionString, port.intValue());
        } else {
            throw new Exception("Connection not possible with the given parameters: [" + connectionString + ":" + port + "]");
        }
    }

    /**
     * Creates a new connection
     *
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
                officeAplication.activate(Notificator.getOfficeMonitor());
            } catch (Throwable officeApplicationException) {
                try {
                    Thread.sleep(6666);
                    Notificator.raiseNotification(Messages.OO_WAITING, false);
                } catch (InterruptedException ex) {
                }
                try {
                    officeAplication.activate(Notificator.getOfficeMonitor());
                } catch (Throwable officeApplicationException1) {
                    Log.Debug(officeApplicationException1);
                    Notificator.raiseNotification(officeApplicationException1, true);
                }
            }
            if (officeAplication.isActive()) {
                documentService = officeAplication.getDocumentService();
                desktopService = officeAplication.getDesktopService();
            } else {
                throw new RuntimeException("Office " + officeAplication + " cannot get activated .. " + new ArrayList(configuration.values()));
            }
            setType(TYPE_REMOTE);
        } else {
            throw new InvalidArgumentException("Host cannot be null and port must be > 0: " + host + ":" + port);
        }

        return true;
    }

    public static void killConnection() {
        try {
            YabsViewProxy.instance().setWaiting(true);
            YabsViewProxy.instance().setProgressRunning(true);
            try {
                if (cachedConnection != null) {
                    cachedConnection.getDesktopService().terminate();
                }
                if (officeAplication != null) {
                    officeAplication.deactivate();
                }
            } catch (Exception ex) {
                Log.Debug(NoaConnectionLocalServer.class, ex.getMessage());
            } finally {
                cachedConnection = null;
                officeAplication = null;
            }
        } catch (Exception ex) {
            Logger.getLogger(NoaConnectionLocalServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                for (int i = 0; i < ooProcesses.size(); i++) {
                    Process officeProcess = (Process) ooProcesses.get(i);
                    if (officeProcess != null) {
                        officeProcess.destroy();
                        officeProcess.waitFor();
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(NoaConnectionLocalServer.class.getName()).log(Level.SEVERE, null, e);
            }
            YabsViewProxy.instance().setWaiting(false);
            YabsViewProxy.instance().setProgressReset();
            YabsViewProxy.instance().showOfficeStatus(false, "");
        }
    }

    /**
     * -1 indicates no connection.
     *
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
     * Tries to start OO in headless server mode.
     * <code>Give it at least 3-4 seconds before attempting to use the server.</code>
     *
     * @param path The path where the OO installation resides
     * @param port The port the server shall listen to
     * @throws IOException
     */
    public synchronized static void startOOServerIfNotRunning(final String path, final int port) {

//        final String command = path.replace("\\", "\\\\") + File.separator + LocalSettings.getProperty(LocalSettings.OFFICE_BINARY_FOLDER) + File.separator + "soffice" + " "
//                + "-headless" + " "
//                + "-nofirststartwizard" + " "
//                + "-norestore" + " "
//                + "-nolockcheck" + " "
//                + "-nocrashreport" + " "
//                + "-nodefault" + " "
//                + (Main.osIsWindows ? "-accept=socket,host=0.0.0.0,port=" + port + ";urp;StarOffice.Service"
//                : "-accept='socket,host=0.0.0.0,port=" + port + ";urp;StarOffice.Service'");

        try {
            SocketAddress addr = new InetSocketAddress("127.0.0.1", port);
            Socket socket = new Socket();
            socket.connect(addr, 100);
            throw new UnsupportedOperationException("Port " + port + " is already in use :-/. Not going to start OO here.");
        } catch (IOException iOException) {
            //nothing is running here
        }

        Runnable runnable = new Runnable() {
            public void run() {
                if (LocalSettings.hasProperty(LocalSettings.OFFICE_COMMAND)) {
                    FileExecutor.run(LocalSettings.getProperty(LocalSettings.OFFICE_COMMAND));
                } else {
                    FileExecutor.run(getOOArgs(path, port));
                }
            }
        };

        Thread t = new Thread(runnable);
        t.setDaemon(true);
        t.start();

    }

    /**
     * Tries to start OO in headless server mode.
     * <code>Give it at least 3-4 seconds before attempting to use the server.</code>
     *
     * @param path The path where the OO installation resides
     * @param port The port the server shall listen to
     * @throws IOException
     */
    public synchronized static void startOOServer(String path, int port) throws IOException {
        FileExecutor.run(getOOArgs(path, port), ooProcesses);
    }
}