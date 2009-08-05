/*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.dialogs;

import java.awt.Component;
import mpv5.data.PropertyStore;

/**
 *
 * 
 */
public interface ControlApplet {

    /**
     * A panel containing the save, delete etc buttons
     * @return
     */
    public Component getAndRemoveActionPanel();

    /**
     * Fills the view with data
     * @param values
     */
    public void setValues(PropertyStore values);

    /**
     * Returns the unique name for this applet
     * @return
     */
    public String getUname();

    /**
     * Reset the view
     */
    public void reset();


//    /**
//     * The local type for properties - gets stored in a local cockie
//     */
//    public final static int LOCAL = 0;
//    /**
//     * The user defined type - gets stored in the user's profile in db
//     */
//    public final static int USERDEFINED = 1;
//    /**
//     * The global type - changes to all users
//     */
//    public final static int GLOBAL = 2;
}
