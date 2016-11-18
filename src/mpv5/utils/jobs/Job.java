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
package mpv5.utils.jobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;

/**
 *
 *  This class helps to manage background tasks which result e.g. in the creation of a file or similar
 */
public class Job extends SwingWorker<Object, Object> {

    private final List<Waitable> objects;
    private final Waiter recipient;
    private JProgressBar bar;
    private String message;
    private final List<Exception> code = new ArrayList<Exception>();

    /**
     * Creates a new job
     * @param waitable
     * @param waiter
     */
    public Job(Waitable waitable, Waiter waiter) {
        this.objects = Collections.singletonList(waitable);
        this.recipient = waiter;
        this.bar = mpv5.YabsViewProxy.instance().getProgressbar();
    }

    /**
     * Creates a new job
     * @param waitable
     * @param waiter 
     * @param message
     */
    public Job(Waitable waitable, Waiter waiter, String message) {
        this.objects = Collections.singletonList(waitable);
        this.recipient = waiter;
        this.message = message;
    }

    /**
     * Creates a new job
     * @param waitables
     * @param waiter
     * @param message
     */
    public Job(List<Waitable> waitables, Waiter waiter, String message) {
        this.objects = waitables;
        this.recipient = waiter;
        this.message = message;
        this.bar = mpv5.YabsViewProxy.instance().getProgressbar();
    }

    @Override
    public Object doInBackground() throws Exception {
        if (bar != null) {
            bar.setIndeterminate(true);
        }
        try {
            for (int i = 0; i < objects.size(); i++) {
                Waitable waitable = objects.get(i);
                code.add(waitable.waitFor());
            }
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
        return objects;
    }

    @Override
    public void done() {
        if (recipient != null) {
            try {
                if (code != null && code.size() > 0) {
                    if (objects.size() == 1) {
                        recipient.set(objects.get(0), code.get(0));
                    } else {
                        recipient.set(objects, code.get(0));
                    }
                } else {
                    if (objects.size() == 1) {
                        recipient.set(objects.get(0), null);
                    } else {
                        recipient.set(objects, null);
                    }
                }
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        }
        if (bar != null) {
            bar.setIndeterminate(false);
        }
        if (message != null) {
            mpv5.YabsViewProxy.instance().addMessage(message);
        }
    }

    public void executeSync() throws Exception { 
        Log.Debug(this, "executeSync");
        doInBackground();
        done();
    }
}
