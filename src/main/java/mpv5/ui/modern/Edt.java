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
package mpv5.ui.modern;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * Swing changes from background threads (database queries, jobs, mail):
 * done on the event dispatch thread. FlatLaf throws an
 * IllegalStateException (and may paint an NPE) if a progress bar changes
 * its indeterminate state on another thread.
 */
public final class Edt {

    private Edt() {
    }

    /**
     * Runs r now if this is the event dispatch thread, else later on it
     *
     * @param r
     */
    public static void run(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    /**
     * @param bar may be null
     * @param indeterminate
     */
    public static void setIndeterminate(final JProgressBar bar, final boolean indeterminate) {
        if (bar != null) {
            run(() -> bar.setIndeterminate(indeterminate));
        }
    }
}
