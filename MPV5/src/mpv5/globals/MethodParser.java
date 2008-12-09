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
package mpv5.globals;

import java.lang.reflect.Method;
import java.util.ArrayList;
import mpv5.utils.arrays.ListenDataUtils;


/**
 * This abstract class allows it to invoke every method of the inheriting class
 * based on the method's String name
 * 
 * @author Andreas
 */
public abstract class MethodParser {

    /**
     * This method invokes every method of the inheriting class which is named
     * using following convention:
     * 
     * get<keyname>[<key>_<type>]
     * 
     * @param typ <type>
     * @return Object[keyname][keyvalue]
     */
    @SuppressWarnings("unchecked")
    public Object[][] getDaten(String typ) {
        ArrayList values = null;
        try {
            values = new ArrayList();
            Method[] methods = this.getClass().getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().startsWith("get") && methods[i].getName().endsWith(typ) && !methods[i].isVarArgs()) {
                    values.add(new Object[]{methods[i].getName().substring(3, methods[i].getName().length()).replaceAll("_", " "),
                                methods[i].invoke(this, (Object[]) null)
                            });
                }
            }
        } catch (Exception ex) {
            return ListenDataUtils.listToTableArray(values);
        }
        return ListenDataUtils.listToTableArray(values);
    }

    /**
     * This method invokes every method of the inheriting class which is named
     * using following convention:
     * 
     * set<keyname>(String keyvalue)
     * @param daten Object[keyname][keyvalue]
     */
    public void setDaten(Object[][] daten) {

        try {
            Method[] methods = this.getClass().getMethods();

            for (int i = 0; i < methods.length; i++) {
                for (int j = 0; j < daten.length; j++) {
                    if (methods[i].getName().equals("set" + daten[j][0].toString().replace(" ", "_"))) {
                        methods[i].invoke(this, (String) daten[j][1]);
                    }
                }
            }
        } catch (Exception ex) {
        }
    }
}
