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

import java.net.*;
import java.io.*;
import mpv5.logging.Log;
import mpv5.utils.text.RandomText;

/**
 * This server-thread handles connections to the {@link MPServer}
 */
public class MPServerThread extends Thread {

    private Socket socket = null;
    private String line;

    /**
     * Creates a new socket listener thread
     * @param socket
     */
    public MPServerThread(Socket socket) {
        super("MPV5ServerThread_" + RandomText.getText());
        this.socket = socket;
    }

    @Override
    public void run() {
        Log.Debug(this, "New MP Server connection initialized.. ");
        if (socket.getInetAddress() != null) {
            Log.Debug(this, "Listening to: " + socket.getInetAddress());

            String input = "";

            try {
                // Get input from the client
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintStream out = new PrintStream(socket.getOutputStream());

                while ((line = in.readLine()) != null && !line.equals(".")) {
                    input = input + line;
                    out.println("I got:" + line);
                }

                // Now write to the client

                System.out.println("Overall message is:" + input);
                out.println("Overall message is:" + input);

                socket.close();
            } catch (IOException ioe) {
                System.out.println("IOException on socket listen: " + ioe);
                ioe.printStackTrace();
            }
        }
    }
}
