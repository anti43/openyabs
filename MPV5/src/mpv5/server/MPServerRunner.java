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
import java.util.HashMap;
import mpv5.Main;
import mpv5.logging.Log;

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
    public static final String NO_COMMAND = "I am deaf to your words.. try again! [300]";
    public static final String CREATE_OBJECT_OK_LINES = "Ok, I will do. Please tell me the " +
            "number of lines i shall read before i save the object? [400]";
    public static final String GO = "Ok, go! [500]";
    public static final String ERROR_OCCOURED = "An error occoured processing your request [600] ";
    private static final String READ_LINES_ = "Lines to read: ";
    public static final String COMMAND_DONE = "Your command has been processed! [700] ";
    public static final String QUIT_OK = "Let’s face it, LeChuck. You are an evil, " +
            "foul-smelling, vile, codependent villain and that’s just not what I’m " +
            "looking for in a romantic relationship right now. [800]";
    //************ Commands from clients ***********************************
    public static final String HANDSHAKE_RESPONSE = "friend";
    public static final String RUN = "run";
    public static final String QUIT = "quit";
    //**** Jobs
    public static final String CREATE_OBJECT = "XMLToDatabaseObjects";
    public static HashMap<String, MPServerJob> JOBS = new HashMap<String, MPServerJob>();

    /**
     * Creates a new socket listener thread
     * @param socket
     */
    public MPServerRunner(Socket socket) {
        this.socket = socket;
        addJob(CREATE_OBJECT, new XMLToDatabaseObjectJob());
    }

    /**
     * Add a job to the runners job list.<br/>
     * After adding a job, you can run it remotely by calling "run [jobname]" to the server.
     * @param name
     * @param job
     */
    public static void addJob(String name, MPServerJob job) {
        JOBS.put(name, job);
    }

    @Override
    public void run() {
        Log.Debug(this, "New MP Server connection initialized.. ");
        if (getSocket().getInetAddress() != null) {
            Log.Debug(this, "Listening to: " + getSocket().getInetAddress());
            try {
                in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
                out = new PrintStream(getSocket().getOutputStream());
                out.println(HANDSHAKE);
                if (in.readLine().equals(HANDSHAKE_RESPONSE)) {
                    out.println(WAITING);
                    while ((line = in.readLine()) != null && !line.equalsIgnoreCase("quit")) {
                        try {
                            if (!line.equals(QUIT)) {
                                if (line.startsWith(RUN) && JOBS.containsKey(line.substring(RUN.length() + 1))) {
                                    try {
                                        start(JOBS.get(line.substring(RUN.length() + 1)));
                                    } catch (Exception ex) {
                                        out.println(ex.getMessage());
                                        Log.Debug(ex);
                                    }
                                } else {
                                    out.println(NO_COMMAND);
                                }
                            } else {
                                out.println(QUIT_OK);
                                getSocket().close();
                                break;
                            }
                        } catch (Exception e) {
                            out.println(e.getMessage());
                        }
                    }
                } else {
                    out.println("Mellon!");
                    out.println("blfft!");
                }
                getSocket().close();
            } catch (IOException ioe) {
                Log.Debug(ioe);
            }
        }
    }

    /**
     * @return the socket
     */
    public Socket getSocket() {
        return socket;
    }

    private void start(MPServerJob get) {
        try {
            out.println(CREATE_OBJECT_OK_LINES);
            int lines = Integer.valueOf(in.readLine());
            out.println(READ_LINES_ + lines);
            out.println(GO);
            String xml = "";
            for (int i = 0; i <= lines; i++) {
                xml += in.readLine() + "\n";
            }
            Log.Debug(this, "Data received:\n\n" + xml + "\n\n");
            get.setSock(socket);
            get.setXmlData(xml);
            get.start();
        } catch (IOException ex) {
            out.println(ex.getMessage());
        }
    }
}
