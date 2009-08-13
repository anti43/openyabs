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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import mpv5.db.objects.WebShop;
import mpv5.logging.Log;

/**
 * This service runs in background and polls the registered web shops in a defined interval
 */
public class WSDaemon extends Thread {

    private WSConnectionClient client;
    private long waitTime = 30;
    private List<WSDaemonJob> jobs = new Vector<WSDaemonJob>();
    private int wsid;
    private boolean running;

    /**
     *
     * @param webShop
     * @throws NoCompatibleHostFoundException
     * @throws MalformedURLException
     */
    public WSDaemon(WebShop webShop) throws NoCompatibleHostFoundException, MalformedURLException {
        this(new URL(webShop.__getUrl()));
        setWaitTime(webShop.__getInterval());
        wsid = webShop.__getIDS();
        running = true;
    }

    /**
     * Returns the unique id of the web shop used
     * @return
     */
    public int getWebShopID() {
        return wsid;
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

    /**
     * Create a new background service
     * @param url The web shop url
     * @throws NoCompatibleHostFoundException
     */
    public WSDaemon(URL url) throws NoCompatibleHostFoundException {
        client = new WSConnectionClient(url);
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (running) {
            Log.Debug(this, "Polling WebShop: " + client);
            checkForWork();
            try {
                Thread.sleep(getWaitTime() * 1000);
            } catch (InterruptedException ex) {
            }
        }
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
            try {
                jobs.get(i).work(client);
                if (jobs.get(i).isOneTimeJob() || jobs.get(i).isDone()) {
                    jobs.remove(jobs.get(i));
                }
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
    }
}
