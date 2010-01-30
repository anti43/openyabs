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
package mpv5.handler;

import java.util.ArrayList;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Group;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.User;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
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
        OBJECT_MONTH("[OBJECT_MONTH]"),
        OBJECT_YEAR("[OBJECT_YEAR]"),
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
        if (!(target instanceof SubItem)) {//No subitems, takes ages
            for (int i = 0; i < vars.size(); i++) {
                String[] method = vars.get(i);
                svars[i] = "[" + method[0].toUpperCase() + "]";
            }
        } else {
            return new String[0];
        }
        return svars;
    }
    /**
     * Generates an array containing all available variables and values for the specific {@link DatabaseObject}
     * @param target
     * @return
     */
//    private static DatabaseObject old;
    public static synchronized String[][] resolveVarsFor(final DatabaseObject target) {
//
//        if (target.equals(old)) {
//            return null;
//        }
//        old = target;

        Log.Debug(VariablesHandler.class, "Resolving vars for " + target.getContext() + "#" + target.ids);
        String[][] vars = new String[GENERIC_VARS.values().length + getSpecialVarsOf(target).length][2];
        GENERIC_VARS[] gens = GENERIC_VARS.values();
        int i;
        for (i = 0; i < gens.length; i++) {
            VariablesHandler.GENERIC_VARS generic_vars = gens[i];
            vars[i][0] = generic_vars.toString();
            try {
                if (vars[i][0].equals(GENERIC_VARS.CREATE_USER.toString())) {
                    vars[i][1] = User.getUsername(target.__getIntaddedby());
                } else if (vars[i][0].equals(GENERIC_VARS.CURRENT_USER.toString())) {
                    vars[i][1] = User.getUsername(mpv5.db.objects.User.getCurrentUser().__getIDS());
                } else if (vars[i][0].equals(GENERIC_VARS.GROUP.toString())) {
                    vars[i][1] = Group.getObject(Context.getGroup(), target.__getGroupsids()).__getCName();
                } else if (vars[i][0].equals(GENERIC_VARS.MONTH.toString())) {
                    vars[i][1] = DateConverter.getMonth();
                } else if (vars[i][0].equals(GENERIC_VARS.MONTH_NAME.toString())) {
                    vars[i][1] = DateConverter.getMonthName();
                } else if (vars[i][0].equals(GENERIC_VARS.QUARTER.toString())) {
                    vars[i][1] = String.valueOf(DateConverter.getQuarter());
                } else if (vars[i][0].equals(GENERIC_VARS.YEAR.toString())) {
                    vars[i][1] = DateConverter.getYear();
                } else if (vars[i][0].equals(GENERIC_VARS.OBJECT_MONTH.toString())) {
                    vars[i][1] = DateConverter.getMonthName(target.__getDateadded());
                } else if (vars[i][0].equals(GENERIC_VARS.OBJECT_YEAR.toString())) {
                    vars[i][1] = DateConverter.getYearName(target.__getDateadded());
                }
            } catch (Exception nodataFoundException) {
            }
        }
        String[] specs = getSpecialVarsOf(target);
        for (int j = 0; j < specs.length; j++) {
            String variable = specs[j];
            vars[i + j][0] = variable;

            ArrayList<String[]> vals;
            vals = target.getValues();
            for (int k = 0; k < vals.size(); k++) {
                String[] value = vals.get(k);
                if (value[0].equalsIgnoreCase(variable.substring(1, variable.length() - 1))) {
                    vars[i + j][1] = value[1];
                }
            }
        }

        return vars;
    }

    /**
     * Replaces each variable in the text with the according values from the {@link DatabaseObject}
     * @param text
     * @param source
     * @return
     */
    public static synchronized String parse(String text, final DatabaseObject source) {
        String[][] c = resolveVarsFor(source);
        if (c != null) {
            for (int i = 0; i < c.length; i++) {
                String[] data = c[i];
//            Log.Debug(VariablesHandler.class, source + ": replacing key: " + data[0] + " with value: " + data[1]);
                if (data[1] != null) {
                    text = text.replace(data[0], data[1]);
                }
            }
        }
        return text;
    }
}
