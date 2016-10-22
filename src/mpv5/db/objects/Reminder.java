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
package mpv5.db.objects;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.Templateable;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.handler.FormatHandler;
import mpv5.logging.Log;
import mpv5.utils.images.MPIcon;

/**
 *
 *  
 */
public class Reminder extends DatabaseObject implements Templateable {

    static String getTypeString(int typ) {
        return Messages.TYPE_REMINDER.toString();
    }
    private String description = "";
    private int itemsids;
    private double extravalue;
    private int stagesids;

    public Reminder() {
        setContext(Context.getReminder());
    }

    @Override
    public JComponent getView() {
        return null;
    }

    /**
     * @return the description
     */
    public String __getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    MPIcon icon;

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
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
     * @return the extravalue
     */
    public double __getExtravalue() {
        return extravalue;
    }

    /**
     * @param extravalue the extravalue to set
     */
    public void setExtravalue(double extravalue) {
        this.extravalue = extravalue;
    }

    /**
     * Fetches all the reminders for the given item
     * @param bill
     * @return
     */
    public static List<Reminder> getRemindersOf(Item bill) {
        ArrayList<Reminder> data = new ArrayList<Reminder>();
        try {
            data.addAll(DatabaseObject.getReferencedObjects(bill, Context.getReminder(), new Reminder()));
        } catch (NodataFoundException ex) {
            Log.Debug(Reminder.class, ex.getMessage());
        }
        return data;
    }

    @Override
    public java.util.Map<String, Object> resolveReferences(java.util.Map<String, Object> map) {
        
        if (map.containsKey("itemsids")) {
            try {
                try {
                    map.put("item", DatabaseObject.getObject(Context.getInvoice(), Integer.valueOf(map.get("itemsids").toString())));
                    map.remove("itemsids");
                } catch (NodataFoundException ex) {
                    map.put("item", null);
                    Log.Debug(this, ex.getMessage());
                }
            } catch (NumberFormatException numberFormatException) {
                //already resolved?
            }
        }
        if (map.containsKey("stagesids")) {
            try {
                try {
                    map.put("stage", DatabaseObject.getObject(Context.getStage(), Integer.valueOf(map.get("stagesids").toString())));
                    map.remove("stagesids");
                } catch (NodataFoundException ex) {
                    map.put("stage", null);
                    Log.Debug(this, ex.getMessage());
                }
            } catch (NumberFormatException numberFormatException) {
                //already resolved?
            }
        }

        return super.resolveReferences(map);
    }

    /**
     * @return the stagesids
     */
    public int __getStagesids() {
        return stagesids;
    }

    /**
     * @param stagesids the stagesids to set
     */
    public void setStagesids(int stagesids) {
        this.stagesids = stagesids;
    }


    @Override
    public int templateType() {
        return Constants.TYPE_REMINDER;
    }

    /**
     *
     * @return
     */
    @Override
    public int templateGroupIds() {
        return __getGroupsids();
    }

    @Override
    public FormatHandler getFormatHandler() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
