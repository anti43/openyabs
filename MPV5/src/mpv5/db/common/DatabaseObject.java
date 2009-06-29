  /*
 *  This file is part of MP.
 *
 *  MP is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.db.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import mpv5.globals.Messages;
import mpv5.db.objects.HistoryItem;
import mpv5.logging.Log;
import mpv5.ui.panels.DataPanel;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPV5View;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.date.DateConverter;
import javax.swing.JComponent;
import mpv5.pluginhandling.MPPLuginLoader;
import mpv5.utils.date.RandomDate;
import mpv5.utils.images.MPIcon;

/**
 * Database Objects reflect a row in a table, and can parse graphical and
 * non-graphical beans to update or create itself to the database
 * @author
 */
public abstract class DatabaseObject implements Comparable<DatabaseObject> {

    private static boolean AUTO_LOCK = false;
    private static HashMap<String, DatabaseObject> cache = new HashMap<String, DatabaseObject>();

    /**
     * Cache all Objects which are within the {@link Context#getCacheableContexts() }
     */
    public static void cacheObjects() {
            DatabaseObject.cacheObjects(Context.getCacheableContexts().toArray(new Context[]{}));
    }

    /**
     * Caches the last 100 objects of this Context
     * @param contextArray
     */
    public static void cacheObjects(final Context[] contextArray) {

        MPV5View.addMessage(Messages.CACHE);
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                int count = 0;
                MPV5View.setProgressMaximumValue(contextArray.length-1);
                for (int f = 0; f < contextArray.length; f++) {
                    try {
                        Context context = contextArray[f];
                        ReturnValue data = QueryHandler.instanceOf().clone(context, 100, true).select();
                        DatabaseObject[] dos = explode(data, DatabaseObject.getObject(context), false);
                        for (int i = 0; i < dos.length; i++) {
                            DatabaseObject databaseObject = dos[i];
                            cacheObject(databaseObject);
                            count++;
                        }
                        MPV5View.setProgressValue(f);
                    } catch (Exception nodataFoundException) {
                        Log.Debug(DatabaseObject.class, nodataFoundException.getMessage());
                    }
                }
                Log.Debug(DatabaseObject.class, "Cached objects: " + count);
                MPV5View.addMessage(Messages.CACHED_OBJECTS + ": " + count);
                MPV5View.setProgressReset();
            }
        };
        Thread t = new Thread(runnable);
        t.start();
    }

    private static void cacheObject(DatabaseObject databaseObject) {
        if (databaseObject != null) {
            cache.put(databaseObject.getDbIdentity() + "@" + databaseObject.__getIDS(), databaseObject);
        }
    }

    private synchronized static DatabaseObject getCachedObject(Context context, int id) {
        if (cache.containsKey(context.getDbIdentity() + "@" + id)) {
            Log.Debug(DatabaseObject.class, "Using cached object " + context + "@" + id);
            return cache.get(context.getDbIdentity() + "@" + id);
        } else {
            return null;
        }
    }
    /**
     * The db context of this do
     */
    public Context context = new Context(this) {
    };
    /**
     * The unique id, or 0 if it is a new do
     */
    public Integer ids = 0;
    /**
     * Is this do active or not?
     */
    private boolean isSaved = false;
    private boolean readOnly = false;
    private boolean active = true;
    /**
     * The mandatory name
     */
    public String cname = "";
    private int groupsids = 1;
    private int intaddedby = 0;
    private Date dateadded = new Date(0);
    private DatabaseObjectLock LOCK = new DatabaseObjectLock(this);

    public String __getCName() {
        return cname;
    }

    /**
     * Fills all {@link DatabaseObject#setVars()} with non-NULL default values
     */
    public void avoidNulls() {
        ArrayList<Method> vals = setVars();
        for (int i = 0; i < vals.size(); i++) {
            Method method = vals.get(i);
            try {
                try {
                    if (!method.getParameterTypes()[0].isPrimitive()) {
                        if (!method.getParameterTypes()[0].isInstance(new String())) {
                            Log.Debug(this, "Set : " + method + " with value: " + method.getParameterTypes()[0].newInstance());
                            method.invoke(this, method.getParameterTypes()[0].newInstance());
                        } else {
                            Log.Debug(this, "Set : " + method + " with value: " + "<empty>");
                            method.invoke(this, "<empty>");
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (InstantiationException ex) {
                Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Some DatabaseObjects have unique fields, and calling this method shall ensure they are unique before saving.<br/>
     * The native implementation actually does nothing, you need to override the method if you define unique columns for a DO.
     */
    public void ensureUniqueness() {
    }

    public void setCName(String name) {
        cname = name;
    }

    /**
     *
     * @return The preferred view for this do's data
     */
    public abstract JComponent getView();

    /**
     * This can be used to graphically represent a do.<br/>
     * The programmer has to take care of the icon size!
     * See {@link MPIcon#getIcon(int width, int height)}<br/>
     * It is recommended to use 22*22 sized icons which do not need to get resized for performance reasons.
     * @return An Icon representing the type of this do
     */
    public abstract MPIcon getIcon();

    @Override
    public DatabaseObject clone() {
        try {
            Object obj = context.getIdentityClass().newInstance();
            return (DatabaseObject) obj;
        } catch (InstantiationException ex) {
            Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * The type of a database object should equal the dbidentity in singular form,
     * but as the dbidentity can change over time, the type name must be consistent
     * @return The type of this do, an unique type identifier
     */
    public String getType() {
        return getDbIdentity().substring(0, getDbIdentity().length() - 1);
    }

    /**
     * The do's context
     * @return A Context
     */
    public Context getContext() {
        return context;
    }

    /**
     *
     * @return The unique id of this do
     */
    public Integer __getIDS() {
        return ids;
    }

    /**
     * Sets the unique id of this do,
     * should never be called manually
     * @param ids
     */
    public void setIDS(int ids) {
        this.ids = ids;
    }

    /**
     * Checks whether this do represents an existing row in the db
     * @return True if the do has been saved once
     */
    public boolean isExisting() {
        if (ids <= 0) {
            Popup.notice(Messages.NOT_POSSIBLE + "\n" + Messages.NOT_SAVED_YET);
            Log.Debug(this, __getCName());
            return false;
        } else {
            return true;
        }
    }

    /**
     *
     * @return A list of all <b>SETTERS</b> in this do child, except the native methods
     */
    public ArrayList<Method> setVars() {
        ArrayList<Method> list = new ArrayList<Method>();
        for (int i = 0; i < this.getClass().getMethods().length; i++) {
            if (this.getClass().getMethods()[i].getName().startsWith("set") &&
                    !this.getClass().getMethods()[i].getName().startsWith("setVars") &&
                    !this.getClass().getMethods()[i].getName().startsWith("setPanelData") &&
                    !this.getClass().getMethods()[i].getName().startsWith("getDbID") &&
                    !this.getClass().getMethods()[i].getName().startsWith("getObject")) {
                list.add(this.getClass().getMethods()[i]);
            }
        }
        return list;
    }

    /**
     *
     * @return A list of all <b>getters</b> in this do child,
     * which match the naming convetion as in
     * <strong>__get*methodname*</strong> (methodname starts with 2 (two) underscores)
     */
    public ArrayList<Method> getVars() {
        ArrayList<Method> list = new ArrayList<Method>();
        for (int i = 0; i < this.getClass().getMethods().length; i++) {
            if (this.getClass().getMethods()[i].getName().startsWith("__get")) {
                list.add(this.getClass().getMethods()[i]);
            }
        }
        return list;
    }

    /**
     * Save this do to db, or update if it has a valid uid already
     * @return
     */
    public boolean save() {
        String message = null;

        if (__getCName() != null && __getCName().length() > 0) {
            try {
                if (ids <= 0) {
                    Log.Debug(this, "Inserting new dataset into: " + this.getContext());
                    dateadded = new Date();
                    if (!this.getType().equals(new HistoryItem().getType())) {
                        message = this.__getCName() + Messages.INSERTED;
                    }
                    ids = QueryHandler.instanceOf().clone(context).insert(collect(), message);
                    Log.Debug(this, "The inserted row has id: " + ids);

                } else {
                    Log.Debug(this, "Updating dataset: " + ids + " within context '" + context + "'");
                    message = this.__getCName() + Messages.UPDATED;
                    QueryHandler.instanceOf().clone(context).update(collect(), ids, this.__getCName() + Messages.UPDATED);
                }

                final String fmessage = message;
                final String fdbid = this.getDbIdentity();
                final int fids = this.ids;
                final int fgids = this.groupsids;
                //Ignore History and User events

                if (Context.getArchivableContexts().contains(context)) {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(fmessage, MPV5View.getUser().__getCName(), fdbid, fids, fgids);
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                }
                this.Saved(true);
                return true;
            } catch (Exception e) {
                Log.Debug(e);
                return false;
            }
        } else {
            Popup.notice(Messages.CNAME_CANNOT_BE_NULL);
            return false;
        }
    }

    /**
     * Safely import a database object from external sources (xml, csv etc)<br/>
     * Override this for ensuring the existance of DObject specific mandatory values.
     * @return
     */
    public boolean saveImport() {
        Log.Debug(this, "Starting import..");
        Log.Debug(this, "Setting IDS to -1");
        ids = -1;
        Log.Debug(this, "Setting intaddedby to " + MPV5View.getUser().__getIDS());
        intaddedby = MPV5View.getUser().__getIDS();

        if (groupsids <= 0 || !DatabaseObject.exists(Context.getGroup(), groupsids)) {
            Log.Debug(this, "Setting groups to 'ungrouped'");
            groupsids = 1;
        }



        return save();
    }

    /**
     * Reset the data of this do (reload from database and discard changes)
     * @return
     */
    public boolean reset() {
        if (ids > 0) {
            Log.Debug(this, "Reloading dataset: " + ids);
            return fetchDataOf(ids);
        }
        return false;
    }

    /**
     * Deletes this do from database, can not be reverted!
     * Note: You can reuse this do then as it it would be a new one with prepopulated data
     * @return
     */
    public boolean delete() {
        boolean result = false;
        String message = null;
        if (!this.getType().equals(new HistoryItem().getType())) {
            message = this.__getCName() + Messages.TRASHED;
        }
        if (ids > 0) {
            if (Context.getTrashableContexts().contains(context)) {
                Log.Debug(this, "Moving to trash:");
                QueryData d = new QueryData();
                d.add("invisible", 1);
                QueryHandler.instanceOf().clone(context).update(d, new String[]{"ids", ids.toString(), ""}, message);
                result = true;
                Log.Debug(this, "The trashed row has id: " + ids);
            } else {
                Log.Debug(this, "Deleting dataset:");
                if (!this.getType().equals(new HistoryItem().getType())) {
                    message = this.__getCName() + Messages.DELETED;
                }
                result = QueryHandler.instanceOf().clone(context).delete(new String[][]{{"ids", ids.toString(), ""}}, message);
                Log.Debug(this, "The deleted row had id: " + ids);
            }

            final String fmessage = message;
            final String fdbid = this.getDbIdentity();
            final int fids = this.ids;
            final int fgids = this.groupsids;
            if (!this.getType().equals(new HistoryItem().getType())) {
                Runnable runnable = new Runnable() {

                    public void run() {
                        QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(fmessage, MPV5View.getUser().__getCName(), fdbid, fids, fgids);
                    }
                };
                SwingUtilities.invokeLater(runnable);
            }
        }
        setIDS(-1);
        return result;
    }

    private static synchronized DatabaseObject checkModification(DatabaseObject dbo) {
        List<DatabaseObjectModifier> mods = MPPLuginLoader.registeredModifiers;
        for (int i = 0; i < mods.size(); i++) {
            DatabaseObjectModifier databaseObjectModifier = mods.get(i);
            try {
                dbo = databaseObjectModifier.modify(dbo);
            } catch (Exception e) {
                Log.Debug(DatabaseObject.class, "Error while modificationg Object " + dbo + " within Modifier " + databaseObjectModifier);
            }
        }
        return dbo;
    }

    /**
     * Locks the dataset represented by this do. The dataset can not be edited by someone else
     * after locking it, until the locking user has logged-out.
     * This does not prevent the data to be viewed by anyone.
     * @return
     */
    public boolean lock() {
        if (!this.isReadOnly()) {
            return LOCK.aquire();
        } else {
            return false;
        }
    }

    /**
     * Releases this object, if any locks are present for the current user
     */
    public void release() {
        LOCK.release();
    }

    /**
     *
     * @return The tablename/view of this do
     */
    public String getDbIdentity() {
        return context.getDbIdentity();
    }

    /**
     * Collect the data to masked/valid DB String array
     */
    private QueryData collect() {

        QueryData t = new QueryData();
        String left = "";
        Object tempval;


        for (int i = 0; i < this.getClass().getMethods().length; i++) {
            if (this.getClass().getMethods()[i].getName().startsWith("__get") && !this.getClass().getMethods()[i].getName().endsWith("IDS")) {
                try {
                    left = this.getClass().getMethods()[i].getName().toLowerCase().substring(5, this.getClass().getMethods()[i].getName().length());
                    Log.Debug(this, "Calling: " + this.getClass().getMethods()[i]);
                    tempval = this.getClass().getMethods()[i].invoke(this, (Object[]) null);
                    Log.Debug(this, "Collect: " + tempval.getClass().getName() + " : " + this.getClass().getMethods()[i].getName() + " ? " + tempval);
                    if (tempval.getClass().isInstance(new String())) {
                        t.add(left, String.valueOf(tempval));
                    } else if (tempval.getClass().isInstance(true)) {
                        boolean c = (Boolean) tempval;
                        t.add(left, c);
                    } else if (tempval.getClass().isInstance(new Date())) {
                        t.add(left, DateConverter.getSQLDateString((Date) tempval));
                    } else if (tempval.getClass().isInstance(new RandomDate(null))) {
                        t.add(left, DateConverter.getSQLDateString((Date) tempval));
                    } else if (tempval.getClass().isInstance(0)) {
                        t.add(left, (Integer) tempval);
                    } else if (tempval.getClass().isInstance(0f)) {
                        t.add(left, (Float) tempval);
                    } else if (tempval.getClass().isInstance(0d)) {
                        t.add(left, (Double) tempval);
                    }
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
//            else if (this.getClass().getMethods()[i].getName().startsWith("is")) {
//                try {
//                    left += this.getClass().getMethods()[i].getName().substring(2, this.getClass().getMethods()[i].getName().length()) + ",";
//                    tempval = this.getClass().getMethods()[i].invoke(this, (Object[]) null);
//                    if (tempval.getClass().isInstance(boolean.class)) {
//                        if (((Boolean) tempval)) {
//                            intval = 1;
//                        }
//                    }
//                    right += "(;;2#4#1#1#8#0#;;)" + intval + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
//                } catch (IllegalAccessException ex) {
//                    Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (IllegalArgumentException ex) {
//                    Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (InvocationTargetException ex) {
//                    Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
        }

        return t;
    }

    /**
     * Parses the given DataPanel into this do.
     * Each of the DataPanel's fields wich has a name ending with underscore must match
     * one of the fields in this do child (without underscore)
     * @param source The DataPanel to parse.
     */
    public void getPanelData(DataPanel source) {
        source.collectData();
        ArrayList<Method> vars = setVars();
        for (int i = 0; i < vars.size(); i++) {
            try {
                Log.Debug(this, "GetPanelData: " + vars.get(i).getName().toLowerCase().substring(3, vars.get(i).getName().length()) + "_ : " +
                        source.getClass().getField(vars.get(i).getName().toLowerCase().substring(3, vars.get(i).getName().length()) + "_").
                        getType().getName() + " [" +
                        source.getClass().getField(vars.get(i).getName().toLowerCase().substring(3, vars.get(i).getName().length()) + "_").get(source) + "]");
                vars.get(i).invoke(this, source.getClass().getField(vars.get(i).getName().toLowerCase().substring(3, vars.get(i).getName().length()) + "_").get(source));
            } catch (Exception n) {
                Log.Debug(this, n.getCause());
                n.printStackTrace();

            }
        }
    }

    /**
     * Injects this do into a Datapanel
     * Each of the DataPanel's fields wich has a name ending with underscore must match
     * one of the fields in this do child (without underscore)
     * @param target
     */
    public void setPanelData(DataPanel target) {

        ArrayList<Method> vars = getVars();

        for (int i = 0; i < vars.size(); i++) {
            try {
//                Log.Debug(this, vars.get(i).getName());
                target.getClass().getField(vars.get(i).getName().toLowerCase().substring(5, vars.get(i).getName().length()) + "_").set(target, vars.get(i).invoke(this, new Object[0]));
            } catch (Exception n) {
                Log.Debug(this, n.getMessage() + " in " + target);
                n.printStackTrace();
            }
        }
    }

    /**
     *
     * @return A list containing pairs of <b>VARNAME</b> and their <b>VALUE</b> of this Databaseobject,
     * those which return in <code>getVars()</code>, as two-fields String-Array.
     * Example: new String[]{CName, Michael}
     */
    public ArrayList<String[]> getValues() {
        ArrayList<Method> vars = getVars();
        ArrayList<String[]> vals = new ArrayList<String[]>();

        for (int i = 0; i < vars.size(); i++) {
            try {
                if (!vars.get(i).getName().substring(5, vars.get(i).getName().length()).toUpperCase().startsWith("DATE")) {
                    vals.add(new String[]{vars.get(i).getName().substring(5, vars.get(i).getName().length()),
                                String.valueOf(vars.get(i).invoke(this, new Object[0]))});
                } else {
                    vals.add(new String[]{vars.get(i).getName().substring(5, vars.get(i).getName().length()),
                                DateConverter.getDefDateString((Date) vars.get(i).invoke(this, new Object[0]))});
                }
            } catch (Exception n) {
                Log.Debug(this, n.getCause());
                n.printStackTrace();
            }
        }

        return vals;
    }

    /**
     * Searches for a specific dataset, cached or non-cached
     * @param context The context to search under
     * @param id The id of the object
     * @return A database object with data, or null if none found
     * @throws NodataFoundException 
     */
    public static DatabaseObject getObject(Context context, int id) throws NodataFoundException {
        DatabaseObject cdo = DatabaseObject.getCachedObject(context, id);
        if (cdo == null) {
            try {
                Object obj = context.getIdentityClass().newInstance();
                ((DatabaseObject) obj).fetchDataOf(id);
                cacheObject((DatabaseObject) obj);
                return (DatabaseObject) obj;
            } catch (InstantiationException ex) {
                Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            return cdo;
        }
        return null;
    }

    /**
     * Returns an empty "sample" Object of the specified <code>Context</code> type
     * @param context
     * @return
     */
    public static DatabaseObject getObject(Context context) {

        if (context.getIdentityClass() != null) {
            try {
                Object obj = context.getIdentityClass().newInstance();
                return (DatabaseObject) obj;
            } catch (InstantiationException ex) {
                Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Log.Debug(DatabaseObject.class, "No identity class found for: " + context);
        }
        return null;
    }

    /**
     * Returns all DBOs in the specific context,
     * @param <T>
     * @param context
     * @return A list of DBOs
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> ArrayList<T> getObjects(Context context) throws NodataFoundException {
        return (ArrayList<T>) getObjects(DatabaseObject.getObject(context), null);
    }

    /**
     *  Returns objects within the given context which match the criterias in the given DataStringHandler
     * @param <T>
     * @param context
     * @param criterias
     * @return
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> ArrayList<T> getObjects(Context context, QueryCriteria criterias) throws NodataFoundException {
        return (ArrayList<T>) getObjects(DatabaseObject.getObject(context), criterias);
    }

    /**
     *  Returns objects within the given context which match the criterias in the given QueryCriteria object
     * @param <T>
     * @param criterias If NULL returns ALL
     * @param template
     * @return
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> ArrayList<T> getObjects(T template, QueryCriteria criterias) throws NodataFoundException {
        ReturnValue data = null;
        if (criterias != null) {
            data = QueryHandler.instanceOf().clone(template.getContext()).select(criterias);
        } else {
            data = QueryHandler.instanceOf().clone(template.getContext()).select();
        }
        ArrayList<T> list = new ArrayList<T>(0);

        if (data.hasData()) {
            DatabaseObject[] f = explode(data, template, false);
            for (int i = 0; i < f.length; i++) {
                DatabaseObject databaseObject = f[i];
                list.add((T) databaseObject);
            }
        }

        Log.Debug(DatabaseObject.class, "Rows found: " + data.getData().length);
        return list;
    }

    /**
     * Return objects which are referenced in the given Context@table
     * <br/>As list of getObject(inReference, (SELECT ids FROM Context@table WHERE dataOwnerIDS = dataowner.ids))
     * @param <T>
     * @param dataOwner
     * @param inReference
     * @param targetType The type you like to get back, most likely {@link DatabaseObject.getObject(Context)}
     * @return
     * @throws mpv5.db.common.NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> ArrayList<T> getReferencedObjects(DatabaseObject dataOwner, Context inReference, T targetType) throws NodataFoundException {

        Object[][] allIds = QueryHandler.instanceOf().clone(inReference).select("ids", new String[]{dataOwner.getDbIdentity() + "ids", dataOwner.__getIDS().toString(), ""});
        ArrayList<T> list = new ArrayList<T>();

        for (int i = 0; i < allIds.length; i++) {
            int id = Integer.valueOf(allIds[i][0].toString());
            list.add((T) DatabaseObject.getObject(inReference, id));
        }
        return list;
    }

    /**
     * Return objects which are referenced in the given Context@table
     * <br/>As list of getObject(inReference, (SELECT ids FROM Context@table WHERE dataOwnerIDS = dataowner.ids))
     * @param <T>
     * @param dataOwner
     * @param inReference
     * @return
     * @throws mpv5.db.common.NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> ArrayList<T> getReferencedObjects(T dataOwner, Context inReference) throws NodataFoundException {

        Object[][] allIds = QueryHandler.instanceOf().clone(inReference).select("ids", new String[]{dataOwner.getDbIdentity() + "ids", dataOwner.__getIDS().toString(), ""});
        ArrayList<T> list = new ArrayList<T>();

        for (int i = 0; i < allIds.length; i++) {
            int id = Integer.valueOf(allIds[i][0].toString());
            list.add((T) DatabaseObject.getObject(inReference, id));
        }
        return list;
    }

    /**
     * Fills this do with the data of the given dataset id
     * @param id
     * @return 
     */
    public boolean fetchDataOf(int id) {
        try {
            explode(QueryHandler.instanceOf().clone(context).select(id), this, true);
            return true;
        } catch (NodataFoundException ex) {
            return false;
        }
    }

    /**
     * Tries to get the id of the last do with the given cname
     * @param cname
     * @return The id, or 0 if none found
     * @throws NodataFoundException
     */
    public Integer getIdOf(String cname) throws NodataFoundException {
        cname = cname.replaceAll("'", "`");
        Object[] data = QueryHandler.instanceOf().clone(context).selectLast("ids", new String[]{"cname", cname, "'"});
        if (data != null && data.length > 0) {
            return Integer.valueOf(String.valueOf(data[0]));
        } else {
            return 0;
        }
    }

    /**
     * Fills this do with the data of the given dataset cname
     * @param cname
     * @return True if data was found
     */
    public boolean fetchDataOf(String cname) {
        cname = cname.replaceAll("'", "`");
        Integer id;
        ReturnValue data;
        try {
            id = this.getIdOf(cname);
            data = QueryHandler.instanceOf().clone(context).select(id);
        } catch (NodataFoundException ex) {
            return false;
        }
        if (data.getData() != null && data.getData().length > 0) {
            explode(data, this, true);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fills the return value's data (rows) into an array of dos if singleExplode is false, if not fills target with the first row
     */
    private static DatabaseObject[] explode(ReturnValue select, DatabaseObject target, boolean singleExplode) {

        DatabaseObject[] dos = null;
        if (!singleExplode) {
            dos = new DatabaseObject[select.getData().length];
        } else {
            dos = new DatabaseObject[1];
        }
        String valx = "";
        Log.Debug(DatabaseObject.class, "Preparing to explode rows: " + dos.length);

        for (int i = 0; i < dos.length; i++) {

            DatabaseObject dbo = null;
            ArrayList<Method> vars = null;
            if (!singleExplode) {
                dbo = DatabaseObject.getObject(target.getContext());
            } else {
                dbo = target;
            }

            vars = dbo.setVars();
            dos[i] = dbo;

            if (select.hasData()) {
                for (int j = 0; j < select.getData()[i].length; j++) {
                    String name = select.getColumnnames()[j].toLowerCase();

                    for (int k = 0; k < vars.size(); k++) {
                        if (vars.get(k).getName().toLowerCase().substring(3).equals(name)) {

                            //Debug section
                            if (select.getData()[i][j] != null) {
                                valx = select.getData()[i][j].getClass().getName();
                            } else {
                                valx = "NULL VALUE!";
                            }
//                        Log.Debug(DatabaseObject.class, "Explode: " + vars.get(k).toGenericString() + " with " + select.getData()[i][j] + "[" + valx + "]");
                            //End Debug Section

                            try {
                                if (name.startsWith("is") || name.toUpperCase().startsWith("BOOL") || name.toUpperCase().endsWith("BOOL")) {
                                    if (String.valueOf(select.getData()[i][j]).equals("1")) {
                                        vars.get(k).invoke(dbo, new Object[]{true});
                                    } else {
                                        vars.get(k).invoke(dbo, new Object[]{false});
                                    }
                                } else if (name.toUpperCase().startsWith("INT") || name.endsWith("uid") || name.endsWith("ids") || name.equals("ids")) {
                                    vars.get(k).invoke(dbo, new Object[]{Integer.valueOf(String.valueOf(select.getData()[i][j]))});
                                } else if (name.toUpperCase().startsWith("DATE") || name.toUpperCase().endsWith("DATE")) {
                                    vars.get(k).invoke(dbo, new Object[]{DateConverter.getDate(String.valueOf(select.getData()[i][j]))});
                                } else if (name.toUpperCase().startsWith("VALUE") || name.toUpperCase().endsWith("VALUE")) {
                                    vars.get(k).invoke(dbo, new Object[]{Double.valueOf(String.valueOf(select.getData()[i][j]))});
                                } else {
                                    vars.get(k).invoke(dbo, new Object[]{String.valueOf(select.getData()[i][j])});
                                }
                            } catch (IllegalAccessException ex) {

                                Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IllegalArgumentException ex) {
                                Log.Debug(dbo, name + " " + String.valueOf(select.getData()[i][j]));
                                Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (InvocationTargetException ex) {

                                Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }

            if (AUTO_LOCK && Context.getLockableContexts().contains(dbo.getContext())) {
                Log.Debug(DatabaseObject.class, "Preparing to lock: " + dbo);
                boolean lck = dbo.lock();
                dbo.ReadOnly(!lck);
                Log.Debug(DatabaseObject.class, "Locking was: " + lck);
            }

            dbo = checkModification(dbo);
        }

        return dos;
    }

    /**
     * Tries to reflect the hash table into this do.
     * The hashtable's keys must match the methods retrieved by do.setVars()
     * @param toHashTable
     * @throws Exception
     */
    public void parse(Hashtable<String, Object> toHashTable) throws Exception {
        ArrayList<Method> vars = setVars();
//        Log.Debug(this, " ?? : " +  toHashTable.size());
        Object[][] data = ArrayUtilities.hashTableToArray(toHashTable);
//        Log.Debug(this, " ?? : " +  data[0]);
        for (int row = 0; row < data.length; row++) {
            String name = data[row][0].toString().toLowerCase();
// Log.Debug(this, name + " ?? : " + " = " + data[row][1]);
            for (int k = 0; k < vars.size(); k++) {
//                Log.Debug(this, name + " ?? : " + vars.get(k).getName() + " = " + data[row][1]);
                if (vars.get(k).getName().toLowerCase().substring(3).equals(name)) {
//                    Log.Debug(this, name + " ?? : " + vars.get(k).getName() + " = " + data[row][1]);

                    if (name.startsWith("is") || name.toUpperCase().startsWith("BOOL")) {
                        if (String.valueOf(data[row][1]).equals("1") || String.valueOf(data[row][1]).toUpperCase().equals("TRUE")) {
                            vars.get(k).invoke(this, new Object[]{true});
                        } else {
                            vars.get(k).invoke(this, new Object[]{false});
                        }
                    } else if (name.toUpperCase().startsWith("INT") || name.endsWith("uid") || name.endsWith("ids") || name.equals("ids")) {
                        vars.get(k).invoke(this, new Object[]{Integer.valueOf(String.valueOf(data[row][1]))});
                    } else if (name.toUpperCase().startsWith("DATE") || name.toUpperCase().endsWith("DATE")) {
                        vars.get(k).invoke(this, new Object[]{DateConverter.getDate(String.valueOf(data[row][1]))});
                    } else if (name.toUpperCase().startsWith("VALUE") || name.toUpperCase().endsWith("VALUE")) {
                        vars.get(k).invoke(this, new Object[]{Double.valueOf(String.valueOf(data[row][1]))});
                    } else {
                        vars.get(k).invoke(this, new Object[]{String.valueOf(data[row][1])});
                    }


                }
            }
        }
    }

    /**
     * @return the groupsids
     */
    public int __getGroupsids() {
        return groupsids;
    }

    /**
     * @param groupsids the groupsids to set
     */
    public void setGroupsids(int groupsids) {
        this.groupsids = groupsids;
    }

    @Override
    public String toString() {
        return cname;
    }

    /**
     * @return the dateadded
     */
    public Date __getDateadded() {
        return dateadded;
    }

    /**
     * @param dateadded the dateadded to set
     */
    public void setDateadded(Date dateadded) {
        this.dateadded = dateadded;
    }

    /**
     * @return the intaddedby
     */
    public int __getIntaddedby() {
        return intaddedby;
    }

    /**
     * @param intaddedby the intaddedby to set
     */
    public void setIntaddedby(int intaddedby) {
        this.intaddedby = intaddedby;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.context != null ? this.context.hashCode() : 0);
        hash = 83 * hash + (this.ids != null ? this.ids.hashCode() : 0);
        return hash;
    }

    /**
     * The equals Method is implemented here to check if two DatabaseObjects represent the same row of their table in the database.<br/>
     * You should not override this, and if, check for the Object to be an instanceOf your own class.
     * @param databaseObject
     */
    @Override
    public boolean equals(Object databaseObject) {
        if (databaseObject == null || !(databaseObject instanceof DatabaseObject)) {
            return false;
        } else {
            return hashCode() == databaseObject.hashCode();
        }
    }

    @Override
    public int compareTo(DatabaseObject anotherObject) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (this == anotherObject) {
            return EQUAL;
        }

        if (this.equals(anotherObject)) {
            return EQUAL;
        }

        //This should yield to items grouped by context..
        if (this.getContext().getId() < anotherObject.getContext().getId()) {
            return BEFORE;
        }
        if (this.getContext().getId() > anotherObject.getContext().getId()) {
            return AFTER;
        }

        if (this.ids < anotherObject.ids) {
            return BEFORE;
        }
        if (this.ids > anotherObject.ids) {
            return AFTER;
        }

        return EQUAL;
    }

    /**
     * Return true if the given id exist in the given Context
     * @param cont
     * @param ids
     * @return
     */
    public static boolean exists(Context cont, Integer ids) {
        try {
            if (QueryHandler.instanceOf().clone(cont).select(ids).getData().length > 0) {
                return true;
            } else {
                return false;
            }
        } catch (NodataFoundException ex) {
            return false;
        }
    }

    /**
     * @return the isSaved
     */
    public boolean isSaved() {
        return isSaved;
    }

    /**
     * @param isSaved the isSaved to set
     */
    public void Saved(boolean isSaved) {
        this.isSaved = isSaved;
    }

    /**
     * @return the readOnly
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * @param readOnly the readOnly to set
     */
    public void ReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void Active(boolean active) {
        this.active = active;
    }

    /**
     * @return AutoLockEnabled
     */
    public static boolean isAutoLockEnabled() {
        return AUTO_LOCK;
    }

    /**
     * AutoLockEnabled
     * @param active
     */
    public static void AutoLockEnabled(boolean active) {
        AUTO_LOCK = active;
    }
}
