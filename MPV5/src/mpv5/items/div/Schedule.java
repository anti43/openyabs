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
package mpv5.items.div;

import java.util.Date;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.ui.frames.MPV5View;

/**
 *
 * @author anti
 */
public class Schedule extends DatabaseObject{

    private int usersids = MPV5View.getUser().__getIDS();
    private int itemsids;
    private Date nextdate = new Date();
    private int intervalmonth = 1;

    public Schedule(){
        context.setDbIdentity(Context.IDENTITY_SCHEDULE);
        context.setIdentityClass(this.getClass());
    }

    @Override
    public JComponent getView() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the usersids
     */
    public int __getUsersids() {
        return usersids;
    }

    /**
     * @param usersids the usersids to set
     */
    public void setUsersids(int usersids) {
        this.usersids = usersids;
    }

    /**
     * @return the itemsids
     */
    public int __getItemsids() {
        return itemsids;
    }

    /**
     * @param itemsids the itemsids to set
     */
    public void setItemsids(int itemsids) {
        this.itemsids = itemsids;
    }

    /**
     * @return the nextdate
     */
    public Date __getNextdate() {
        return nextdate;
    }

    /**
     * @param nextdate the nextdate to set
     */
    public void setNextdate(Date nextdate) {
        this.nextdate = nextdate;
    }

    /**
     * @return the intervalmonth
     */
    public int __getIntervalmonth() {
        return intervalmonth;
    }

    /**
     * @param intervalmonth the intervalmonth to set
     */
    public void setIntervalmonth(int intervalmonth) {
        this.intervalmonth = intervalmonth;
    }

}
