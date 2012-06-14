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

import mpv5.YabsViewProxy;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Notificator;

/**
 *This is a listening server which starts a new {@link MPServerRunner} on each connection.
 */
public class MPServer extends Thread {

    private static MPServer serverInstance;
    private static XMLRPCServer xmlrpcs;

    /**
     *
     */
    public static void shutdown() {
        stopServer();
    }

    public static void runServer() {
        if (serverInstance == null) {
            Log.Debug(MPServer.class, "Initialising MP Server..");

            try {
                mpv5.YabsViewProxy.instance().setWaiting(true);
                mpv5.YabsViewProxy.instance().setProgressRunning(true);
                serverInstance = new MPServer();
                getServerInstance().start();
            } catch (Exception ex) {
                Log.Debug(ex);
            } finally {
                YabsViewProxy.instance().setWaiting(false);
                YabsViewProxy.instance().setProgressReset();
                YabsViewProxy.instance().showServerStatus(true);
            }
        }
    }

    /**
     * same as shutDown
     */
    public static void stopServer() {
        Log.Debug(MPServer.class, "Stopping MP Server..");
        xmlrpcs.getWebServer().shutdown();
        YabsViewProxy.instance().showServerStatus(false);
    }

    /**
     * @return the serverInstance
     */
    public static synchronized MPServer getServerInstance() {
        if (serverInstance == null) {
            runServer();
        }
        return serverInstance;
    }

    @Override
    public void run() {
        if (xmlrpcs == null) {
            try {
                xmlrpcs = new XMLRPCServer();
                Notificator.raiseNotification(Messages.DONE + "\n" + "Port: " + XMLRPCServer.getPort(), false);
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        }
    }
}
