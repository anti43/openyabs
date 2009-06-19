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
package mpv5.utils.jobs;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import mpv5.logging.Log;
import mpv5.ui.frames.MPV5View;

/**
 *
 *  
 */
public class Job extends SwingWorker<Object, Object> {

    private Waitable object;
    private Waiter recipient;
    private JProgressBar bar;
    private String message;
    private Exception code = null;


    /**
     * 
     * @param waitable
     * @param waiter
     */
    public Job(Waitable waitable, Waiter waiter) {
        this.object = waitable;
        this.recipient = waiter;
        this.bar = MPV5View.progressbar;
    }

    /**
     *
     * @param waitable
     * @param waiter 
     * @param message
     */
    public Job(Waitable waitable, Waiter waiter, String message) {
        this.object = waitable;
        this.recipient = waiter;
        this.message = message;
    }

    @Override
    public Object doInBackground() throws Exception {
        if (bar != null) {
            bar.setIndeterminate(true);
        }
        try {
           code = object.waitFor();
        } catch (Exception e) {
            if (bar != null) {
                bar.setIndeterminate(false);
            }
            throw e;
        } finally {
            if (bar != null) {
                bar.setIndeterminate(false);
            }
        }
        return object;
    }

    @Override
    public void done() {
        if (recipient != null) {
            try {
                recipient.set(object, code);
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        }
        if (bar != null) {
            bar.setIndeterminate(false);
        }
        if (message != null) {
            MPV5View.addMessage(message);
        }
    }
}
