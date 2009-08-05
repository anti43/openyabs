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
package mpv5.ui.beans;

import mpv5.db.common.Context;
import mpv5.utils.models.MPComboBoxModelItem;

/**
 * This interface specifies Objects which are notified after a MPComboBox selection change
 */
public interface MPCBSelectionChangeReceiver {
    /**
     * Invoked after selection change
     * @param to
     * @param c
     */
    public void changeSelection(MPComboBoxModelItem to, Context c);
}
