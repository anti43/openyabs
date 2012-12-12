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

import ag.ion.bion.officelayer.application.*;
import ag.ion.bion.officelayer.desktop.IDesktopService;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.noa.NOAException;

import com.sun.star.auth.InvalidArgumentException;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import mpv5.Main;
import mpv5.YabsViewProxy;
import mpv5.globals.GlobalSettings;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.files.FileExecutor;
import mpv5.utils.reflection.ClasspathTools;

/**
 * This class handles connections to remote and local OpenOffice installations
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

   /*
    * Define the native dll /so path
    */
   public static void definePath() {
      if (System.getProperty(IOfficeApplication.NOA_NATIVE_LIB_PATH) == null) {
         System.setProperty(IOfficeApplication.NOA_NATIVE_LIB_PATH, ClasspathTools.findUserDir().getPath() + File.separator + "lib");
      }
   }

   /**
    * Terminates the cached connection (if any)
    */
   public static void killConnection() {
      try {
         YabsViewProxy.instance().setWaiting(true);
         YabsViewProxy.instance().setProgressRunning(true);
         Notificator.raiseNotification(Messages.OO_WAITING, false);
         if (LocalSettings.getBooleanProperty(LocalSettings.OFFICE_LOCALSERVER)) {
            NoaConnectionLocalServer.killConnection();
         } else {
            if (cachedConnection != null) {
               try {
                  cachedConnection.getDesktopService().terminate();
                  officeAplication.deactivate();

               } catch (NOAException ex) {
                  Log.Debug(NoaConnection.class, ex.getMessage());
               } finally {
                  cachedConnection = null;
                  officeAplication = null;
               }
            }
         }
      } catch (Exception e) {
         Log.Debug(NoaConnection.class, e.getMessage());
      } finally {
         YabsViewProxy.instance().setWaiting(false);
         YabsViewProxy.instance().setProgressReset();
         YabsViewProxy.instance().showOfficeStatus(false, "");

      }
   }
   protected static IOfficeApplication officeAplication;
   protected int type = -1;
   protected IDocumentService documentService;
   protected IDesktopService desktopService;
   protected static NoaConnection cachedConnection;

   /**
    * Creates a connection, depending on the current local config. Should not be
    * called from EDT
    *
    * @return
    * @throws Exception
    */
   public synchronized static NoaConnection getConnection() throws Exception {
      if (LocalSettings.getBooleanProperty(LocalSettings.OFFICE_LOCALSERVER)) {
         return NoaConnectionLocalServer.getConnection();
      }

      definePath();
      if (cachedConnection != null) {
         return cachedConnection;
      }
      Log.Debug(NoaConnection.class, "Connection not established yet, trying..");
      try {
         YabsViewProxy.instance().setWaiting(true);
         YabsViewProxy.instance().setProgressRunning(true);
         Notificator.raiseNotification(Messages.OO_WAITING, false);
         if (LocalSettings.getBooleanProperty(LocalSettings.OFFICE_USE) && (LocalSettings.hasProperty(LocalSettings.OFFICE_HOST) || LocalSettings.hasProperty(LocalSettings.OFFICE_HOME))) {
            cachedConnection = new NoaConnection(LocalSettings.getBooleanProperty(LocalSettings.OFFICE_REMOTE) ? LocalSettings.getProperty(LocalSettings.OFFICE_HOST) : LocalSettings.getProperty(LocalSettings.OFFICE_HOME), LocalSettings.getBooleanProperty(LocalSettings.OFFICE_REMOTE) ? Integer.valueOf(LocalSettings.getProperty(LocalSettings.OFFICE_PORT)) : 0);
            Notificator.raiseNotification(Messages.OO_DONE_LOADING, false);
         } else {
            throw new UnsupportedOperationException(Messages.OO_NOT_CONFIGURED.getValue());
         }
      } catch (Exception ex) {
         mpv5.logging.Log.Debug(ex);
         Notificator.raiseNotification(Messages.OOCONNERROR + " \n" + ex, true);
         throw ex;
      } finally {
         YabsViewProxy.instance().setWaiting(false);
         YabsViewProxy.instance().setProgressReset();
         YabsViewProxy.instance().showOfficeStatus(cachedConnection != null, cachedConnection.toString());
         return cachedConnection;
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
    * @throws Exception If any Exception is thrown during the connection attempt
    */
   private NoaConnection(String connectionString, int port) throws Exception {

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
    * dummy
    *
    * @throws Exception
    */
   public NoaConnection() throws Exception {
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
         configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY, IOfficeApplication.REMOTE_APPLICATION);
         configuration.put(IOfficeApplication.APPLICATION_HOST_KEY, host.replace("http://", ""));
         configuration.put(IOfficeApplication.APPLICATION_PORT_KEY, String.valueOf(port));
         configuration.put(IOfficeApplication.APPLICATION_ARGUMENTS_KEY, String.valueOf(port));

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

   /**
    *
    * @param OOOPath
    * @return
    * @throws OfficeApplicationException
    * @throws NOAException
    * @throws InvalidArgumentException
    */
   private synchronized boolean createLocalConnection(String OOOPath) throws OfficeApplicationException, NOAException, InvalidArgumentException {

//        IApplicationAssistant applicationAssistant = new ApplicationAssistant();
//        ILazyApplicationInfo[] appInfos = applicationAssistant.getLocalApplications();
//        if (appInfos.length < 1) {
////            throw new Exception("No LOOffice Application found.");
//        } else {
////            OOOPath = appInfos[0].getHome();
//        }
//        System.err.println(OOOPath);
//        System.err.println(appInfos[0].getHome());

      if (OOOPath != null) {
         HashMap<String, String> configuration = new HashMap<String, String>();
         configuration.put(IOfficeApplication.APPLICATION_HOME_KEY, OOOPath);
         configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY, IOfficeApplication.LOCAL_APPLICATION);
         officeAplication = OfficeApplicationRuntime.getApplication(configuration);

         officeAplication.setConfiguration(configuration);
         officeAplication.activate();

         documentService = officeAplication.getDocumentService();
         desktopService = officeAplication.getDesktopService();

         setType(TYPE_LOCAL);
      } else {
         throw new InvalidArgumentException("Path to LOO cannot be null: " + OOOPath);
      }

      return true;
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
   public synchronized static void startOffice(final String path, final int port) {
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

   protected static String[] getOOArgs(String path, int port) {
      String b = LocalSettings.getProperty(LocalSettings.OFFICE_BINARY_FOLDER);
      return new String[]{path.replace("\\", "\\\\") + File.separator + (b == null || b.equals("null") ? "program" : b) + File.separator + "soffice",
               "--headless",
               "--nofirststartwizard",
               (GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.invisibleofficeserver", true) ? "--invisible" : ""),
               "--norestore",
               "--nolockcheck",
               "--nocrashreport",
               "--nodefault",
               (Main.osIsWindows ? "--accept=socket,host=0.0.0.0,port=" + port + ";urp;StarOffice.Service"
               : "--accept='socket,host=0.0.0.0,port=" + port + ";urp;StarOffice.Service'")};
   }

   /**
    * @return the IOfficeApplication or null
    */
   public IOfficeApplication getApplication() {
      return officeAplication;
   }
}
