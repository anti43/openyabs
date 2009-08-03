/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.jobs;

import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.logging.Log;

/**
 *
 *  
 */
public class Watcher implements Runnable {

    private int interval = 1000;
    private Object watched;
    private Object caller;
    private Thread t = new Thread(this);

    public Watcher(Object parent, Object obj) {
        this.watched = obj;
        this.caller = parent;
        t.start();
    }

    public void run() {
        while (true) {
            Log.Debug(this, "Watching variable from " + getCaller().getClass() + " with type:" + getWatched().getClass() + " has value: " + getWatched());
            try {
                Thread.sleep(interval);
            } catch (InterruptedException ex) {
                mpv5.logging.Log.Debug(ex);//Logger.getLogger(Watcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * @return the interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * @param interval the interval to set
     */
    public void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * @return the watched
     */
    public Object getWatched() {
        return watched;
    }

    /**
     * @return the caller
     */
    public Object getCaller() {
        return caller;
    }
}
