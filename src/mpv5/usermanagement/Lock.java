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
package mpv5.usermanagement;

import java.awt.Component;
import mpv5.logging.Log;
import mpv5.ui.dialogs.LoginScreen;
import mpv5.ui.dialogs.LoginToInstanceScreen;

/**
 *
 * 
 */
public class Lock {

    /**
     * Locks the given component and brings up a login window which could unlock it again
     * @param comp
     */
    public static void lock(Component comp) {
        comp.setEnabled(false);
        LoginScreen loginScreen = new mpv5.ui.dialogs.LoginScreen(comp);
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
