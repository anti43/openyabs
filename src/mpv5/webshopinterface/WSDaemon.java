
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
package mpv5.webshopinterface;

//~--- non-JDK imports --------------------------------------------------------

import mpv5.db.objects.WebShop;

import mpv5.logging.Log;

//~--- JDK imports ------------------------------------------------------------

import java.net.MalformedURLException;
import java.net.URL;

import java.util.List;
import java.util.Vector;

/**
 * This service runs in background and polls the registered web shops in a defined interval
 */
public class WSDaemon extends Thread {
    private long               waitTime = 300;
    private boolean            running  = false;
    private List<WSDaemonJob>  jobs     = new Vector<WSDaemonJob>();
    private WSConnectionClient client;
    private WebShop            wes;

    /**
     *
     * @param webShop
     * @throws NoCompatibleHostFoundException
     * @throws MalformedURLException
     */
    public WSDaemon(WebShop webShop) throws NoCompatibleHostFoundException, MalformedURLException {
        if (webShop.__getIsauthenticated()) {
            client = new WSConnectionClient(new URL(webShop.__getUrl()), webShop.__getIsrequestCompression(),
                                            webShop.__getUsername(), webShop.__getPassw());
        } else {
            client = new WSConnectionClient(new URL(webShop.__getUrl()), webShop.__getIsrequestCompression(), null,
                                            null);
        }

        setWaitTime(webShop.__getInterv());
        wes     = webShop;
        running = true;
    }

    /**
     * Create a new background service
     * @param url The web shop url
     * @param requCompression
     * @throws NoCompatibleHostFoundException
     */
    public WSDaemon(URL url, boolean requCompression, String user, String pw) throws NoCompatibleHostFoundException {
        client  = new WSConnectionClient(url, requCompression, user, pw);
        wes     = new WebShop();
        running = true;
    }

    /**
     * Returns the unique id of the web shop used
     * @return
     */
    public int getWebShopID() {
        return getWebShop().__getIDS();
    }

    /**
     * Adds a job
     * @param job
     */
    public void addJob(WSDaemonJob job) {
        jobs.add(job);
    }

    /**
     * Stop this deamon
     */
    public void kill() {
        running = false;
    }

    @Override
    public void run() {
        do {
            Log.Debug(this, "Polling WebShop: " + client);
            checkForWork();

            try {
                Thread.sleep(getWaitTime() * 1000);
            } catch (InterruptedException ex) {}
        } while (running);
    }

    /**
     * @return the waitTime
     */
    public long getWaitTime() {
        return waitTime;
    }

    /**
     * @param waitTime the waitTime to set
     */
    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    private void checkForWork() {
        for (int i = 0; i < jobs.size(); i++) {
            jobs.get(i).work(client);

            if (jobs.get(i).isOneTimeJob() || jobs.get(i).isDone()) {
                jobs.remove(jobs.get(i));
            }
        }
    }

    /**
     * Run the the {@link WSDaemon#run()} method only once
     * @param runOnce
     */
    public void start(boolean runOnce) {
        running = !runOnce;
        super.start();
    }

    /**
     * @return the wes
     */
    public WebShop getWebShop() {
        return wes;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
