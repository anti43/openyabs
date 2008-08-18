/*
 *  This file is part of MP by anti43 /GPL.
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
package mp4.utils.tasks;

import javax.swing.SwingWorker;
import mp3.classes.interfaces.Waitable;
import mp3.classes.interfaces.Waiter;

/**
 *
 * @author Andreas
 */
public class Job extends SwingWorker<Object, Object> {

    private Waitable object;
    private Waiter recipient;

    public Job(Waitable object, Waiter rec) {
        this.object = object;
        this.recipient = rec;
    }

    public Object doInBackground() {
        object.waitFor();
        return object;
    }

    @Override
    public void done() {
       recipient.set(object);
    }
}
