/*
 *  This file is part of MP.
 *
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.server;


import java.io.IOException;
import java.net.ServerSocket;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
/**
 *This is a listening server which starts a new {@link MPServerThread} on each connection.
 */
public class MPServer {

    public MPServer() {
        Log.Debug(this, "Initialising MP Server..");
        ServerSocket serverSocket = null;
        boolean running = true;
        try {
            serverSocket = new ServerSocket(Integer.valueOf(LocalSettings.getProperty(LocalSettings.SERVER_PORT)));
            Log.Debug(this, "MP Server started!");
        } catch (IOException e) {
            running = false;
            Log.Debug(e);
        }
        while (running) {
            try {
                new MPServerThread(serverSocket.accept()).start();
            } catch (IOException ex) {
                Log.Debug(ex);
            }
        }
        try {
            serverSocket.close();
            Log.Debug(this, "MP Server closed!");
        } catch (IOException ex) {
            Log.Debug(ex);
        }
    }
}