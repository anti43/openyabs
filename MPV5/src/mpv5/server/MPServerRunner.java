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
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.Main;
import mpv5.logging.Log;
import mpv5.utils.jobs.Job;
import mpv5.utils.jobs.Waitable;
import mpv5.utils.jobs.Waiter;
import mpv5.utils.text.RandomText;

/**
 * This server-thread handles connections to the {@link MPServer}
 */
public class MPServerRunner implements Runnable {

    private Socket socket = null;
    private String line;
    public static final String JOB_DONE = "MP server finished a job.";
    private BufferedReader in;
    private PrintStream out;

    //************************* Responses from us *************************
    public static final String HANDSHAKE = "Speak, friend, and enter [100]";
    public static final String WAITING = "Awaiting your advise.. [200]";
    public static final String NO_COMMAND = "I am deaf to your words.. try again! The sequenze is clear: <command> <lines> <data> [300]";
    public static final String CREATE_OBJECT_OK_LINES = "Ok, I will do. Please tell me the number of lines i shall read before i save the object? [400]";
    public static final String GO = "Ok, go! [500]";
    public static final String ERROR_OCCOURED = "An error occoured processing your request [600] ";
    private static final String READ_LINES_ = "Lines to read: ";
    public static final String COMMAND_DONE = "Your command has been processed! [700] ";
    //************ Responses from clients ***********************************
    public static final String HANDSHAKE_RESPONSE = "Friend";
    //**** Jobs
    public static final String CREATE_OBJECT = "Add DatabaseObjects";

    /**
     * Creates a new socket listener thread
     * @param socket
     */
    public MPServerRunner(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        Log.Debug(this, "New MP Server connection initialized.. ");
        if (socket.getInetAddress() != null) {
            Log.Debug(this, "Listening to: " + socket.getInetAddress());
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintStream(socket.getOutputStream());
                out.println(HANDSHAKE);
                if (in.readLine().equals(HANDSHAKE_RESPONSE)) {
                    out.println(WAITING);
                    while ((line = in.readLine()) != null && !line.equals(Main.GOODBYE_MESSAGE)) {
                        if (line.equalsIgnoreCase(CREATE_OBJECT)) {
                            try {
                                createDatabaseObject();
                            } catch (Exception ex) {
                                out.println(ex.getMessage());
                                Log.Debug(ex);
                            }
                        } else {
                            out.println(NO_COMMAND);
                        }
                    }
                } else {
                    out.println("Mellon!");
                    out.println(Main.GOODBYE_MESSAGE);
                }
                socket.close();
            } catch (IOException ioe) {
                Log.Debug(ioe);
            }
        }
    }

    private void createDatabaseObject() throws Exception {

        out.println(CREATE_OBJECT_OK_LINES);
        int lines = Integer.valueOf(in.readLine());
        out.println(READ_LINES_ + lines);
        out.println(GO);

        String xml = "";
        for (int i = 0; i <= lines; i++) {
            xml += in.readLine() + "\n";
        }
        Log.Debug(this, "Data received:\n\n" + xml + "\n\n");
        Waitable w = new XMLToDatabaseObjectJob(xml, socket);
        Waiter w2 = (Waiter) w;
        new Job(w, w2, JOB_DONE).execute();

    }
}
