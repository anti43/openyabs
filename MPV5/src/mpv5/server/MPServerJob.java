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
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.logging.Log;
import mpv5.utils.jobs.Waitable;
import mpv5.utils.jobs.Waiter;

/**
 *
 */
public abstract class MPServerJob implements Waitable, Waiter {

    private Socket sock;
    private PrintStream out;
    private String xmlData;

    /**
     * Creates a new job
     * @param data
     * @param socket
     */
    public MPServerJob(String data, Socket socket) {
        this.xmlData = data;
        this.sock = socket;
        try {
            this.out = new PrintStream(socket.getOutputStream());
        } catch (IOException ex) {
            Log.Debug(ex);
        }
    }

    public MPServerJob(){}

    /**
     * @return the sock
     */
    public Socket getSock() {
        return sock;
    }

    /**
     * @return the out
     */
    public PrintStream getOut() {
        return out;
    }

    /**
     * @return the xmlData
     */
    public String getXmlData() {
        return xmlData;
    }

    /**
     * Start the job
     */
    public abstract void start();

    /**
     * @param sock the sock to set
     */
    public void setSock(Socket sock) throws IOException {
        this.sock = sock;
        this.out = new PrintStream(sock.getOutputStream());
    }

    /**
     * @param out the out to set
     */
    public void setOut(PrintStream out) {
        this.out = out;
    }

    /**
     * @param xmlData the xmlData to set
     */
    public void setXmlData(String xmlData) {
        this.xmlData = xmlData;
    }
}
