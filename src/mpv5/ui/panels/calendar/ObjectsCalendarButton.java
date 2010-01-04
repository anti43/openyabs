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
package mpv5.ui.panels.calendar;

import java.awt.Color;
import java.util.Date;
import javax.swing.JButton;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Item;


public class ObjectsCalendarButton extends JButton {

    private static final long serialVersionUID = 1L;
    private Date date;
    private DatabaseObject item;

    public ObjectsCalendarButton(Date d) {
        super("");
        this.date = d;

    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the schedule
     */
    public DatabaseObject getDatabaseObject() {
        return item;
    }

    /**
     * @param item
     */
    public void setDatabaseObject(DatabaseObject item) {
        this.item = item;
    }
}
