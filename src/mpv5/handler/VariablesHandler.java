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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.ActivityList;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Conversation;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.User;
import mpv5.globals.GlobalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.utils.date.DateConverter;
import mpv5.utils.numberformat.FormatNumber;

/**
 *
 */
public abstract class VariablesHandler {

    //generic
    /**
     * Contains generic variables for any
     * {@link DatabaseObject}
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
        CURRENT_USER("[CURRENT_USER]"),
        DATENUMERIC("[DATENUMERIC]"),
        YEAR_SHORT("[YEAR_SHORT]"),
        DAY("[DAY]");
        private String val;

        GENERIC_VARS(String value) {
            val = value;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    public static enum GENERIC_VARS_FORMAT {

        YEAR("[YEAR]"),
        MONTH("[MONTH]"),
        QUARTER("[QUARTER]"),
        GROUP("[GROUP_NAME]"),
        CURRENT_USER("[CURRENT_USER]"),
        YEAR_SHORT("[YEAR_SHORT]");
        private String val;

        GENERIC_VARS_FORMAT(String value) {
            val = value;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    //special
    /**
     * Determines the specific variables for the given
     * {@link DatabaseObject}
     *
     * @param target
     * @return
     * @deprecated
     */
    public static String[] getSpecialVarsOf(DatabaseObject target) {
        List<String[]> vars = target.getValues();
        String[] svars = new String[vars.size()];
        if (!(target instanceof SubItem)) {//No subitems, takes ages
            for (int i = 0; i < vars.size(); i++) {
                String[] method = vars.get(i);
                svars[i] = "[" + method[0] + "]";
            }
        } else {
            return new String[0];
        }
        return svars;
    }

    /**
     * Generates an array containing all available variables
     * and values for the specific {@link DatabaseObject}
     *
     * @param target
     * @return
     */
    public static synchronized List<String[]> resolveVarsFor(final DatabaseObject target) {
        return resolveVarsFor(target, target.getFormFields());
    }

    /**
     * Generates an array containing all available variables
     * and values for the specific {@link DatabaseObject}
     *
     * @param target
     * @param preResolvedVars
     * @return
     */
    public static synchronized List<String[]> resolveVarsFor(final DatabaseObject target, Map preResolvedVars) {
        Log.Debug(VariablesHandler.class, "..................>\n\n" + preResolvedVars);
        Log.Debug(VariablesHandler.class, "Resolving vars for " + target.getContext() + "#" + target.__getIDS() + ".." + target.getClass());

        List<String[]> vars = new ArrayList<String[]>();
        GENERIC_VARS[] gens = GENERIC_VARS.values();
        int i;
        for (i = 0; i < gens.length; i++) {
            VariablesHandler.GENERIC_VARS generic_vars = gens[i];
            String varName = generic_vars.toString();
            String varValue = "";
            try {
                if (varName.equals(GENERIC_VARS.CREATE_USER.toString())) {
                    varValue = User.getUsername(target.__getIntaddedby());
                } else if (varName.equals(GENERIC_VARS.CURRENT_USER.toString())) {
                    varValue = User.getUsername(mpv5.db.objects.User.getCurrentUser().__getIDS());
                } else if (varName.equals(GENERIC_VARS.GROUP.toString())) {
                    varValue = Group.getObject(Context.getGroup(), target.__getGroupsids()).__getCname();
                } else if (varName.equals(GENERIC_VARS.MONTH.toString())) {
                    varValue = DateConverter.getMonth();
                } else if (varName.equals(GENERIC_VARS.MONTH_NAME.toString())) {
                    varValue = DateConverter.getMonthName();
                } else if (varName.equals(GENERIC_VARS.QUARTER.toString())) {
                    varValue = String.valueOf(DateConverter.getQuarter());
                } else if (varName.equals(GENERIC_VARS.YEAR.toString())) {
                    varValue = DateConverter.getYear();
                } else if (varName.equals(GENERIC_VARS.YEAR_SHORT.toString())) {
                    varValue = String.valueOf(DateConverter.getYear()).substring(2);
                } else if (varName.equals(GENERIC_VARS.OBJECT_MONTH.toString())) {
                    varValue = DateConverter.getMonthName(target.__getDateadded());
                } else if (varName.equals(GENERIC_VARS.OBJECT_YEAR.toString())) {
                    varValue = DateConverter.getYearName(target.__getDateadded());
                } else if (varName.equals(GENERIC_VARS.DATENUMERIC.toString())) {
                    varValue = DateConverter.getDateNumeric();
                } else if (varName.equals(GENERIC_VARS.DAY.toString())) {
                    varValue = DateConverter.getDayOfMonth();
                }
            } catch (Exception ex) {
                Log.Debug(VariablesHandler.class, ex.getMessage());
            }
            vars.add(new String[]{varName, varValue});
        }
        String[] specs = getSpecialVarsOf(target);//fixme evt remove
        Log.Debug(VariablesHandler.class, "..xxxx>\n\n" + Arrays.asList(specs));

        Iterator iterator = preResolvedVars.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) iterator.next();
            vars.add(new String[]{"[" + String.valueOf(mapEntry.getKey()).toUpperCase() + "]", String.valueOf(mapEntry.getValue())});
        }
        int j;
        for (j = 0; j < specs.length; j++) {
            String varName = specs[j];
            String varValue = "";

            List<String[]> vals;
            vals = target.getValues();
            for (String[] value : vals) {
                if (value[0].equalsIgnoreCase(varName.substring(1, varName.length() - 1))) {
                    varValue = value[1];
                }
            }
            vars.add(new String[]{varName, varValue});
        }

        if (target instanceof Item) {
            try {
                Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), ((Item) target).__getContactsids());

                vars.add(new String[]{"[contact.cname]", c.__getCname()});
                vars.add(new String[]{"[contact.cnumber]", c.__getCNumber()});
                vars.add(new String[]{"[contact.company]", c.__getCompany()});
                vars.add(new String[]{"[contact.prename]", c.__getPrename()});
                vars.add(new String[]{"[contact.title]", c.__getTitle()});
                vars.add(new String[]{"[contact.country]", c.__getCountry()});
                vars.add(new String[]{"[grosvaluef]", FormatNumber.formatLokalCurrency(((Item) target).__getTaxvalue().doubleValue() + ((Item) target).__getNetvalue().doubleValue())});
                vars.add(new String[]{"[type]", Item.getTypeString(((Item) target).__getInttype())});

                if (c.__getisMale()) {
                    vars.add(new String[]{"[contact.gender]", Messages.CONTACT_TYPE_MALE.getValue()});
                    vars.add(new String[]{"[contact.intro]", Messages.CONTACT_INTRO_MALE.getValue()});
                } else {
                    vars.add(new String[]{"[contact.gender]", Messages.CONTACT_TYPE_FEMALE.getValue()});
                    vars.add(new String[]{"[contact.intro]", Messages.CONTACT_INTRO_FEMALE.getValue()});
                }

            } catch (NodataFoundException ex) {
                Log.Debug(VariablesHandler.class, ex.getMessage());
            }
        }

        if (target instanceof Conversation) {
            try {
                Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), ((Conversation) target).__getContactsids());
                vars.add(new String[]{"[contact.cname]", c.__getCname()});
                vars.add(new String[]{"[contact.company]", c.__getCompany()});
                vars.add(new String[]{"[contact.prename]", c.__getPrename()});
                vars.add(new String[]{"[contact.title]", c.__getTitle()});
                vars.add(new String[]{"[contact.country]", c.__getCountry()});
                vars.add(new String[]{"[type]", Messages.TYPE_CONVERSATION.toString()});

            } catch (NodataFoundException ex) {
                Log.Debug(VariablesHandler.class, ex.getMessage());
            }
        }

        if (target instanceof ActivityList) {
            try {
                Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), ((ActivityList) target).__getContactsids());

                vars.add(new String[]{"[contact.cname]", c.__getCname()});
                vars.add(new String[]{"[contact.company]", c.__getCompany()});
                vars.add(new String[]{"[contact.prename]", c.__getPrename()});
                vars.add(new String[]{"[contact.title]", c.__getTitle()});
                vars.add(new String[]{"[contact.country]", c.__getCountry()});
                vars.add(new String[]{"[type]", Messages.TYPE_ACTIVITY.toString()});

            } catch (NodataFoundException ex) {
                Log.Debug(VariablesHandler.class, ex.getMessage());
            }
        }
        if (Log.isDebugging()) {
            for (String[] strings : vars) {
                Log.Debug(target, Arrays.asList(strings));
            }
        }

        return vars;
    }

    /**
     * Replaces each variable in the text with the according
     * values from the {@link DatabaseObject} and evaluates
     * scripts
     *
     * @param text
     * @param source
     * @return
     */
    public static synchronized String parse(String text, final DatabaseObject source) {
        return parse(text, source, source.getFormFields());
    }

    /**
     * Replaces each variable in the text with the according
     * values from the {@link DatabaseObject} and evaluates
     * scripts
     *
     * @param text
     * @param source
     * @param preResolvedVars
     * @return
     */
    public static synchronized String parse(String text, final DatabaseObject source, Map preResolvedVars) {
        List<String[]> c = resolveVarsFor(source, preResolvedVars);
        if (c != null) {
            for (String[] data : c) {
                if (data != null) {
                    if (data[1] != null) {
                        if (Log.isDebugging()) {
                            Log.Debug(VariablesHandler.class, source + ": replacing key: " + data[0] + " with value: " + data[1]);
                        }
                        //text = text.replace(data[0], data[1]);
                        text = text.replaceAll("(?i)" + data[0].replace("[", "\\[").replace("]", "\\]"), data[1]);//.replace(".","\\.") regex escape..
                    }
                }
            }
        }
        String sm = GlobalSettings.getProperty("org.openyabs.config.scriptsymbol", "#");
        if (text.contains(sm)) {
            text = source.evaluateAll(text);
        }

        return text;
    }

    public static synchronized String parseNoScript(String text, DatabaseObject source) {
        List<String[]> c = resolveVarsFor(source, Collections.EMPTY_MAP);
        if (c != null) {
            for (String[] data : c) {
                if (data != null) {
                    if (data[1] != null) {
                        if (Log.isDebugging()) {
                            Log.Debug(VariablesHandler.class, source + ": replacing key: " + data[0] + " with value: " + data[1]);
                        }
                        //text = text.replace(data[0], data[1]);
                        text = text.replaceAll("(?i)" + data[0].replace("[", "\\[").replace("]", "\\]"), data[1]);//.replace(".","\\.") regex escape..
                    }
                }
            }
        }
        return text;
    }
}
