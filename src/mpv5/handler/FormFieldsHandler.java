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
package mpv5.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Group;
import mpv5.utils.date.DateConverter;
import mpv5.utils.numberformat.FormatNumber;

/**
 * This class holds all form field names and maps them to {@link DatabaseObject}s
 */
public abstract class FormFieldsHandler {

    /**
     * Creates a hash map view to all the object's form fields and their values (which may be other {@link DatabaseObject}s)
     * @param obj
     * @return A HashMap<key,value/>
     */
    public synchronized static HashMap<String, Object> getFormFieldsFor(DatabaseObject obj) {
        ArrayList<Object[]> m = obj.getValues2();
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < m.size(); i++) {
            Object[] vals = m.get(i);
            map.put(vals[0].toString().toLowerCase(), vals[1]);
        }

        obj.resolveReferences(map);

        return map;
    }

    /**
     * Creates a hash map view to all the object's form fields and their values, including their referenced objects resolved to keys and values.<br/>
     * This method is safe to never return null values.
     * @param obj
     * @return A HashMap<key,value/>
     */
    public synchronized static HashMap<String, String> getFormattedFormFieldsFor(DatabaseObject obj) {
        HashMap<String, Object> map = FormFieldsHandler.getFormFieldsFor(obj);
        HashMap<String, String> maps = new HashMap<String, String>();

        for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
            String key = it.next();
            Object o = map.get(key);

            if (o instanceof DatabaseObject && !(o instanceof Group)) {
                maps.putAll(getFormattedFormFieldsFor((DatabaseObject) o));
            } else if (o instanceof Double) {
                maps.put(obj.getType() + "." + key, FormatNumber.formatDezimal((Double) o));
            } else if (o instanceof Date) {
                maps.put(obj.getType() + "." + key, DateConverter.getDefDateString((Date) o));
            } else {
                maps.put(obj.getType() + "." + key, String.valueOf(o));
            }
        }

        return maps;
    }
}

