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
package plugins.database;

import mpv5.pluginhandling.MP5Plugin;
import mpv5.ui.frames.MPV5View;

/**
 *
 * @author anti
 */
public class DataLock implements MP5Plugin {

    public MP5Plugin load(MPV5View frame) {
        return this;
    }

    public void unload() {
    }

    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getVendor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Long getUID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isReal() {
        return false;
    }

    public boolean isVisual() {
        return false;
    }
}
