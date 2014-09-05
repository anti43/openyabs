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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Group;
import mpv5.db.objects.User;
import mpv5.logging.Log;
import mpv5.utils.date.DateConverter;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.text.TypeConversion;

/**
 * This class holds all form field names and maps them to {@link DatabaseObject}s
 */
public class FormFieldsHandler {

    private final DatabaseObject obj;

    /**
     * Create a new handler for the given object
     * @param obj
     */
    public FormFieldsHandler(DatabaseObject obj) {
        this.obj = obj;
    }

    /**
     * Creates a hash map view to all the object's form fields and their values (which may be other {@link DatabaseObject}s)
     * @return A HashMap<key,value/>
     */
    public synchronized Map<String, Object> getFormFields() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        
        if (obj != null) {
            obj.resolveValueProperties(map);
            List<Object[]> m = obj.getValues2();
            map = new HashMap<String, Object>();
            for (Object[] vals : m) {
                if (vals[1] == null) {
                    vals[1] = "";
                }
                map.put(vals[0].toString().toLowerCase(), vals[1]);
            }

            obj.resolveReferences(map);
        }
        
        return map;
    }

    /**
     * Creates a hash map view to all the object's form fields and their values, including their referenced objects resolved to keys and values.<br/>
     * This method is safe to never return null values.
     * @param identifier or NULL
     * @return A HashMap<key,value/>
     */
    public synchronized Map<String, Object> getFormattedFormFields(final String identifier) {
        Map<String, Object> map = getFormFields();
        TreeMap<String, Object> maps = new TreeMap<String, Object>();

        for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
            String key = it.next();
            Object o = map.get(key);

            String skey = null;
            if (identifier == null) {
                skey = obj.getType() + "." + key;
            } else {
                skey = identifier + "." + key;
            }

            if (o == null || String.valueOf(o).equals("null")) {
                maps.put(skey, "");
            } else if (o instanceof DatabaseObject && !(o instanceof Group)) {
                maps.putAll(new FormFieldsHandler((DatabaseObject) o).getFormattedFormFields(obj.getType() + "." + key));
            } else if (o instanceof Boolean) {
                maps.put(skey, TypeConversion.booleanToString((Boolean) o));
            } else if (o instanceof Double || o.getClass() == double.class) {
                maps.put(skey, FormatNumber.formatDezimal((Double) o));
            } else if (o instanceof Date) {
                maps.put(skey, DateConverter.getDefDateString((Date) o));
            } else {
                maps.put(skey, o);
            }
        }
        List<String[]> mapi = User.getCurrentUser().getProperties().getProperties("companyinfo.").getList();
        for (String[] strings : mapi) {
            if (strings[1] != null) {
                maps.put("companyinfo." + strings[0], strings[1].contains("[") ? VariablesHandler.parse(strings[1], obj) : strings[1]);
            }
        }
        
        mapi = User.getCurrentUser().getProperties().getProperties("template.").getList();
        for (String[] strings : mapi) {
            if (strings[1] != null) {
                maps.put("template." + strings[0], strings[1].contains("[") ? VariablesHandler.parse(strings[1], obj) : strings[1]);
            }
        }
        return maps;
    }
}
