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

import javax.swing.SwingWorker;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;

/**
 * This is a convenience class for using SwingWorker
 *
 * 
 */
public class BackGroundJob extends SwingWorker<Void, Void> {

    private Class clazz;

    /**
     * Creates a new background job which will invoke a new instance of the given class
     * @param clazz
     */
    public BackGroundJob(Class clazz) {
        this.clazz = clazz;
        this.execute();
    }

    @Override
    public Void doInBackground() {
        mpv5.YabsViewProxy.instance().setWaiting(true);
        try {
            clazz.getConstructors()[0].newInstance((Object[]) null);
        } catch (Exception e) {
            Log.Debug(this, e.getMessage());
        } finally {
            mpv5.YabsViewProxy.instance().setWaiting(false);
        }
        return null;
    }

    @Override
    public void done() {

        mpv5.YabsViewProxy.instance().setWaiting(false);
    }
}