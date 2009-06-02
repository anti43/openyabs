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
package mpv5.usermanagement;

import java.awt.Component;
import mpv5.logging.Log;

/**
 *
 *  anti43
 */
public class Lock {

    /**
     * Locks the given component and brings up a login window which could unlock it again
     * @param comp
     */
    public static void lock(Component comp) {
        comp.setEnabled(false);
        new mpv5.ui.dialogs.LoginScreen();
    }


    /**
     * Unlocks the given component
     * @param frame
     */
    public static void unlock(Component frame) {
        try {
            frame.setEnabled(true);
        } catch (Exception e) {
            Log.Debug(Lock.class, "Frame is not visible, can not be unlocked.");
        }
    }
}
