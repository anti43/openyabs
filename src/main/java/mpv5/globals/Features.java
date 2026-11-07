/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.globals;

/**
 * Parts of YaBS which can be switched off in the user interface.
 */
public final class Features {

    private Features() {
    }

    /**
     * Activity lists (navigation, lists in contacts, questions when creating
     * invoices, delete confirmation). Hidden unless YaBS is started with
     * -Dyabs.activitylists=true; the data and the code stay.
     *
     * @return true if activity lists are shown
     */
    public static boolean activityLists() {
        return Boolean.getBoolean("yabs.activitylists");
    }
}
