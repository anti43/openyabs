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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;

/**
 *This is a listening server which starts a new {@link MPServerRunner} on each connection.
 */
public class MPServer extends Thread {

    /**
     *
     */
    public static void stopAllInstances() {
        for (int i = 0; i < SOCKETS.size(); i++) {
            try {
                SOCKETS.get(i).close();
            } catch (Exception ex) {
            }
        }
        for (int i = 0; i < INSTANCES.size(); i++) {
            INSTANCES.get(i).interrupt();
        }
    }
    /**
     * If set to false, the server will stop after next connection attempt
     */
    public boolean ALLOWED_TO_RUN = true;
    private Thread t;
    private static Vector<Thread> INSTANCES = new Vector<Thread>();
    private ServerSocket serverSocket;
    private static Vector<ServerSocket> SOCKETS = new Vector<ServerSocket>();

    public MPServer() {
        Log.Debug(this, "Initialising MP Server..");
        INSTANCES.add(this);
    }

    @Override
    public void run() {
        ALLOWED_TO_RUN = true;
        serverSocket = null;
        
        boolean running = true;
        try {
            serverSocket = new ServerSocket(Integer.valueOf(LocalSettings.getProperty(LocalSettings.SERVER_PORT)));
            SOCKETS.add(serverSocket);
            Log.Debug(this, "MP Server started!");
            MPView.addMessage("MP Server started!");
        } catch (IOException e) {
            running = false;
            Log.Debug(e);
        }
        while (running & ALLOWED_TO_RUN) {
            try {
                t = new Thread(new MPServerRunner(serverSocket.accept()));
                t.start();
            } catch (IOException ex) {
                Log.Debug(this, ex.getMessage());
                running = false;
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