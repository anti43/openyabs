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
package mpv5.bugtracker;

import java.util.List;
import java.util.Vector;
import mpv5.ui.frames.MPView;

/**
 * This class handles unexpected exceptions thrown during the use of Yabs
 */
public class ExceptionHandler {

    /**
     * Notify the ExceptionHandler about a new Exception
     * @param exception
     */
    public static void add(Exception exception) {
        exc.add(exception);
//        mpv5.YabsViewProxy.instance().showError();
//        mpv5.YabsViewProxy.instance().setWaiting(false);//clean up
    }
    static List<Exception> exc = new Vector<Exception>();

    /**
     * Returns the Excpetions and flushes the list
     * @return A list of Exceptions
     */
    public static List<Exception> getExceptions() {
        Vector<Exception> t = new Vector<Exception>(exc);
        exc.clear();
        return t;
    }
}
