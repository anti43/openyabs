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
import mpv5.logging.Log;

/**
 *This is a listening server which starts a new {@link MPServerRunner} on each connection.
 */
public class MPServer extends Thread {

    /**
     *
     */
    public static void stopAllInstances() {
        xmlrpcs.getWebServer().shutdown();
    }
    private static Vector<Thread> INSTANCES = new Vector<Thread>();
    private static XMLRPCServer xmlrpcs;

    public MPServer() {
        Log.Debug(this, "Initialising MP Server..");
        INSTANCES.add(this);
    }

    @Override
    public void run() {
       if (xmlrpcs == null) {
            try {
                xmlrpcs = new XMLRPCServer();//Conveniently start the XML RPC server along the native MP server
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        }
    }
}
