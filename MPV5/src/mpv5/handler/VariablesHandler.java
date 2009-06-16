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
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Group;
import mpv5.db.objects.User;
import mpv5.logging.Log;
import mpv5.ui.frames.MPV5View;
import mpv5.utils.date.DateConverter;

/**
 *
 */
public abstract class VariablesHandler {

    //generic
    /**
     * Contains generic variables for any {@link DatabaseObject}
     */
    public static enum GENERIC_VARS {

        YEAR("[YEAR]"),
        MONTH("[MONTH]"),
        QUARTER("[QUARTER]"),
        MONTH_NAME("[MONTH_NAME]"),
        GROUP("[GROUP_NAME]"),
        CREATE_USER("[CREATE_USER]"),
        CURRENT_USER("[CURRENT_USER]");
        private String val;

        GENERIC_VARS(String value) {
            val = value;
        }

        @Override
        public String toString() {
            return val;
        }
    }


    //special
    /**
     * Determines the specific variables for the given {@link DatabaseObject}
     * @param target
     * @return
     */
    public static String[] getSpecialVarsOf(DatabaseObject target) {
        ArrayList<String[]> vars = target.getValues();
        String[] svars = new String[vars.size()];
        for (int i = 0; i < vars.size(); i++) {
            String[] method = vars.get(i);
            svars[i] = "[" + method[0].toUpperCase() + "]";
        }
        return svars;
    }

    /**
     * Generates an array containing all available variables and values for the specific {@link DatabaseObject}
     * @param target
     * @return
     */
    public static String[][] resolveVarsFor(DatabaseObject target) {
        String[][] g = new String[GENERIC_VARS.values().length + getSpecialVarsOf(target).length][2];
        GENERIC_VARS[] gens = GENERIC_VARS.values();
        int i;
        for (i = 0; i < gens.length; i++) {
            VariablesHandler.GENERIC_VARS generic_vars = gens[i];
            g[i][0] = generic_vars.toString();
            try {
                if (g[i][0].equals(GENERIC_VARS.CREATE_USER.toString())) {
                    g[i][1] = User.getUsername(target.__getIntaddedby());
                } else if (g[i][0].equals(GENERIC_VARS.CURRENT_USER.toString())) {
                    g[i][1] = User.getUsername(MPV5View.getUser().__getIDS());
                } else if (g[i][0].equals(GENERIC_VARS.GROUP.toString())) {
                    g[i][1] = Group.getObject(Context.getGroup(), target.__getGroupsids()).__getCName();
                } else if (g[i][0].equals(GENERIC_VARS.MONTH.toString())) {
                    g[i][1] = DateConverter.getMonth();
                } else if (g[i][0].equals(GENERIC_VARS.MONTH_NAME.toString())) {
                    g[i][1] = DateConverter.getMonthName();
                } else if (g[i][0].equals(GENERIC_VARS.QUARTER.toString())) {
                    g[i][1] = String.valueOf(DateConverter.getQuarter());
                } else if (g[i][0].equals(GENERIC_VARS.YEAR.toString())) {
                    g[i][1] = DateConverter.getYear();
                }
            } catch (NodataFoundException nodataFoundException) {
            }

            String[] specs = getSpecialVarsOf(target);
            for (int j = 0; j < specs.length; j++) {
                String string = specs[j];
                g[1 + j][0] = string;
                ArrayList<String[]> vals;
                vals = target.getValues();
                for (int k = 0; k < vals.size(); k++) {
                    String[] methods = vals.get(k);
                    if (methods[0].equalsIgnoreCase(string)) {
                        g[1 + j][1] = methods[1];
                    }
                }
            }
        }
        return g;
    }

    /**
     * Replaces each variable in the text with the according values from the {@link DatabaseObject}
     * @param text
     * @param source
     * @return
     */
    public static synchronized String parse(String text, DatabaseObject source){
        String[][] c = resolveVarsFor(source);
        for (int i = 0; i < c.length; i++) {
            String[] data = c[i];
            Log.Debug(VariablesHandler.class, source +": replacing key: " + data[0] + " with value: " + data[1]);
            text = text.replace(data[0], data[1]);
        }
        return text;
    }
}
