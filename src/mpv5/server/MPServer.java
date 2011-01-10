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

import java.util.Vector;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;

/**
 *This is a listening server which starts a new {@link MPServerRunner} on each connection.
 */
public class MPServer extends Thread {

    /**
     *
     */
    public static void shutdown() {
        xmlrpcs.getWebServer().shutdown();
    }
    private static XMLRPCServer xmlrpcs;

    public MPServer() {
        Log.Debug(this, "Initialising MP Server..");
    }

    @Override
    public void run() {
        mpv5.YabsViewProxy.instance().setWaiting(true);
        mpv5.YabsViewProxy.instance().setProgressRunning(true);
        if (xmlrpcs == null) {
            try {
                xmlrpcs = new XMLRPCServer();
                mpv5.YabsViewProxy.instance().setWaiting(false);
                mpv5.YabsViewProxy.instance().setProgressRunning(false);
                Popup.notice(Messages.DONE + "\n" + "Port: " + XMLRPCServer.getPort());
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        }
    }
}
