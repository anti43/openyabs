  /*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.db.common;

import java.util.Arrays;
import java.util.TreeMap;
import java.util.Collections;
import java.awt.Color;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.Messages;
import mpv5.db.objects.HistoryItem;
import mpv5.logging.Log;
import mpv5.ui.panels.DataPanel;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.date.DateConverter;
import javax.swing.JComponent;
import mpv5.db.objects.Group;
import mpv5.db.objects.User;
import mpv5.globals.LocalSettings;
import mpv5.handler.SimpleDatabaseObject;
import mpv5.handler.VariablesHandler;
import mpv5.pluginhandling.MPPLuginLoader;
import mpv5.ui.panels.ChangeNotApprovedException;
import mpv5.utils.date.RandomDate;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.images.MPIcon;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.text.RandomText;
import static mpv5.db.common.Context.*;

/**
 * Database Objects reflect a row in a table, and can parse graphical and
 * non-graphical beans to update or create itself to the database
 * @author
 */
public abstract class DatabaseObject implements Comparable<DatabaseObject>, Serializable, Cloneable {

    /**
     * Represents a Context-ID pair which uniquely identifies a DatabaseObject
     * @param <T>
     * @param <V>
     */
    public static class Entity<T extends Context, V> implements Serializable{

        private final DatabaseObject owner;

        /**
         * Create a new Entity, nulls not allowed
         * @param owner
         */
        public Entity(DatabaseObject owner) {
            this.owner = owner;
        }

        /**
         * Create a new Entity, nulls not allowed
         * @param context
         * @param ids
         */
        public Entity(T context, Integer ids) throws NodataFoundException {
            if (context == null || ids == null) {
                throw new NullPointerException("ids");
            }
            this.owner = getObject(context, ids);
        }

        /**
         * Returns the Context corresponding to this entry, never null
         * @return
         */
        public Context getKey() {
            return owner.context;
        }

        /**
         * Returns the ID corresponding to this entry, never null
         * @return
         */
        public Integer getValue() {
            return owner.ids;
        }

        /**
         * Compares the specified object with this entry for equality.
         * Returns <tt>true</tt> if the given object is also a map entry and Key and Value.intValue are equal
         * @param o
         */
        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof Entity)) {
                return false;
            }
            Entity e = (Entity) o;
            if (e.hashCode() == hashCode()) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 11 * hash + (this.owner.context != null ? this.owner.context.hashCode() : 0);
            hash = 11 * hash + (this.owner.ids != null ? this.owner.ids.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return owner.context + " [" + owner.ids + "]";
        }
    }

    /**
     * Marks the value of the annotated getter to be persisted on {@link #save}
     * / annotated setter to be evaluated with a value from the database on {@link #explode}.
     * Default is true on methods with the signature start '__get'.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Persistable {

        boolean value();
    }

    /**
     * If set, the auto-database-schema creator will create a foreign key reference to the {@link Context} with the given id.
     * To be one of {@link Context#getId() }.
     * @deprecated not yet implemented
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface References {

        int value();
    }

    /**
     * If set, the auto-database-schema creator will create a ON DELETE CASCADE 
     * reference to any {@link Context}s which have been annotated via the @References annotation on this method
     * To be one of {@link Context#getId() }.
     * @deprecated not yet implemented
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Cascade {

        boolean value();
    }

    /**
     * If set to false, this property will not show up in autogenerated tables.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Displayable {

        boolean value();
    }
    private static boolean AUTO_LOCK = false;
    private static Map<String, SoftReference<DatabaseObject>> cache = new ConcurrentHashMap<String, SoftReference<DatabaseObject>>(1000);

    /**
     * Cache all Objects which are within the {@link Context#getCacheableContexts() }
     */
    public static void cacheObjects() {
        DatabaseObject.cacheObjects(Context.getCacheableContexts().toArray(new Context[]{}));
    }

    /**
     * Caches objects of this Context
     * @param contextArray
     */
    public static void cacheObjects(final Context[] contextArray) {

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                int count = 0;
                mpv5.YabsViewProxy.instance().setProgressMaximumValue(contextArray.length - 1);
                for (int f = 0; f < contextArray.length; f++) {
                    try {
                        Context context = contextArray[f];
                        boolean cacheable = true;
                        if (AUTO_LOCK) {
                            if (Context.getLockableContexts().contains(context)) {
                                cacheable = false;
                                Log.Debug(DatabaseObject.class, "AUTO_LOCK is enabled, not going to cache " + context);
                            }
                        }
                        if (cacheable) {
                            ReturnValue data = QueryHandler.instanceOf().clone(context, LocalSettings.getIntegerProperty(LocalSettings.CACHE_SIZE), true).select();
                            DatabaseObject[] dos = explode(data, DatabaseObject.getObject(context), false, true);
                            for (int i = 0; i < dos.length; i++) {
                                DatabaseObject databaseObject = dos[i];
                                cacheObject(databaseObject);
                                count++;
                            }
                        }
                        mpv5.YabsViewProxy.instance().setProgressValue(f);
                    } catch (Exception nodataFoundException) {
                        Log.Debug(DatabaseObject.class, nodataFoundException.getMessage());
                    }
                }
                Log.Debug(DatabaseObject.class, "Cached objects: " + count);
                mpv5.YabsViewProxy.instance().setProgressReset();
            }
        };
        Thread t = new Thread(runnable);
        t.start();
    }

    private static synchronized void cacheObject(final DatabaseObject databaseObject) {
        if (databaseObject != null && databaseObject.__getIDS().intValue() > 0) {
            cache.put(databaseObject.getDbIdentity() + "@" + databaseObject.__getIDS(), new SoftReference<DatabaseObject>(databaseObject));
        }
    }

    private static synchronized void uncacheObject(final DatabaseObject databaseObject) {
        if (databaseObject != null) {
            if (cache.remove(databaseObject.getDbIdentity() + "@" + databaseObject.__getIDS()) != null) {
                Log.Debug(DatabaseObject.class, "Removed from cache: " + databaseObject.getDbIdentity() + "@" + databaseObject.__getIDS());
            }
        }
    }

    private synchronized static DatabaseObject getCachedObject(final Context context, final int id) {
        final String uid = context.getDbIdentity() + "@" + id;
        if (cache.containsKey(uid)) {
            DatabaseObject o = cache.get(uid).get();
            if (o == null) {
                //was already garbage collected
                cache.remove(uid);
            } else {
//                Log.Debug(DatabaseObject.class, "Using cached object " + context.getDbIdentity() + "@" + id + " [" + o + "]");
            }
            return o;
        } else {
            Log.Debug(DatabaseObject.class, "" + context.getDbIdentity() + "@" + id + " not found in cache.");
            return null;
        }
    }

    /**
     * This method can be used to workaround the DatabaseObject#getObjects(...) casting issues introduced by @me :-)
     * @param <T>
     * @param objects
     * @param template
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> List<T> toObjectList(List<DatabaseObject> objects, T template) {
        List<T> l = new ArrayList<T>();
        for (int i = 0; i < objects.size(); i++) {
            DatabaseObject databaseObject = objects.get(i);
            l.add((T) databaseObject);
        }
        return l;
    }

    private static synchronized void invoke(Method method, Object argument, DatabaseObject dbo, Object valx) {

        try {

            if (!method.getParameterTypes()[0].isPrimitive()) {
                if (method.getParameterTypes()[0].isInstance(true)) {
                    if (String.valueOf(argument).equals("1") || String.valueOf(argument).equalsIgnoreCase("true")) {
                        method.invoke(dbo, new Object[]{true});
                    } else {
                        method.invoke(dbo, new Object[]{false});
                    }
                } else if (method.getParameterTypes()[0].isInstance(0)) {
                    method.invoke(dbo, new Object[]{Integer.valueOf(String.valueOf(argument))});
                } else if (method.getParameterTypes()[0].isInstance(new Date())) {
                    method.invoke(dbo, new Object[]{DateConverter.getDate(argument)});
                } else if (method.getParameterTypes()[0].isInstance(BigDecimal.ONE)) {
                    method.invoke(dbo, new Object[]{new BigDecimal(String.valueOf(argument))});
                } else if (method.getParameterTypes()[0].isInstance(0d)) {
                    method.invoke(dbo, new Object[]{Double.valueOf(String.valueOf(argument))});
                } else if (method.getParameterTypes()[0].isInstance(0l)) {
                    method.invoke(dbo, new Object[]{Long.valueOf(String.valueOf(argument))});
                } else if (method.getParameterTypes()[0].isInstance(0f)) {
                    method.invoke(dbo, new Object[]{Float.valueOf(String.valueOf(argument))});
                } else if (method.getParameterTypes()[0].isInstance(01)) {
                    method.invoke(dbo, new Object[]{Short.valueOf(String.valueOf(argument))});
                } else if (method.getParameterTypes()[0].getCanonicalName().equals(new byte[0].getClass().getCanonicalName())) {//doitbetter
                    method.invoke(dbo, new Object[]{(byte[]) argument});
                } else {
                    //defaults to java.lang.String, Object args are not supported.. possibly later via XMLEncoder?
                    method.invoke(dbo, new Object[]{String.valueOf(argument)});
                }
            } else {
                if (method.getParameterTypes()[0].isAssignableFrom(int.class)) {
                    method.invoke(dbo, new Object[]{Integer.valueOf(String.valueOf(argument))});
                } else if (method.getParameterTypes()[0].isAssignableFrom(float.class)) {
                    method.invoke(dbo, new Object[]{Float.valueOf(String.valueOf(argument))});
                } else if (method.getParameterTypes()[0].isAssignableFrom(double.class)) {
                    method.invoke(dbo, new Object[]{Double.valueOf(String.valueOf(argument))});
                } else if (method.getParameterTypes()[0].isAssignableFrom(short.class)) {
                    method.invoke(dbo, new Object[]{Short.valueOf(String.valueOf(argument))});
                } else if (method.getParameterTypes()[0].isAssignableFrom(long.class)) {
                    method.invoke(dbo, new Object[]{Long.valueOf(String.valueOf(argument))});
                } else if (method.getParameterTypes()[0].isAssignableFrom(boolean.class)) {
                    if (String.valueOf(argument).equals("1") || String.valueOf(argument).equalsIgnoreCase("true")) {
                        method.invoke(dbo, new Object[]{true});
                    } else {
                        method.invoke(dbo, new Object[]{false});
                    }
                }
            }
        } catch (Exception ex) {
            Log.Debug(DatabaseObject.class, "Explode: " + method.toGenericString() + " with " + argument + "[" + valx + "]");
            Log.Debug(ex);
        }
    }
    /**
     * The db context of this do. To be defined by the child class!
     * 
     */
    protected Context context = Context.DEFAULT;
    /**
     * The unique id, or 0 if it is a new do
     */
    protected Integer ids = 0;
    private boolean readOnly = false;
    private boolean active = true;
    /**
     * The mandatory name
     */
    protected String cname = "";
    private int groupsids = 1;
    private int intaddedby = 0;
    private Date dateadded = new Date(0);
    private transient DatabaseObjectLock LOCK = new DatabaseObjectLock(this);
    private Color color = Color.WHITE;
    public Entity<Context, Integer> IDENTITY = new Entity<Context, Integer>(this);

    public String __getCName() {
        return cname;
    }

    /**
     * Fills all {@link DatabaseObject#setVars()} with non-NULL default values
     */
    public void avoidNulls() {
        List<Method> vals = setVars();
        for (int i = 0; i < vals.size(); i++) {
            Method method = vals.get(i);
            try {
                try {
                    if (!method.getParameterTypes()[0].isPrimitive()) {
                        if (method.getParameterTypes()[0].isInstance(new String())) {
                            Log.Debug(this, "Set : " + method + " with value: " + "<empty>");
                            method.invoke(this, "<empty>");
                        } else if (method.getParameterTypes()[0].isInstance(new BigDecimal("0"))) {
                            Log.Debug(this, "Set : " + method + " with value: 0");
                            method.invoke(this, new BigDecimal("0"));
                        } else {
                            Log.Debug(this, "Set : " + method + " with value: " + method.getParameterTypes()[0].newInstance());
                            method.invoke(this, method.getParameterTypes()[0].newInstance());
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    mpv5.logging.Log.Debug(ex);//Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    mpv5.logging.Log.Debug(ex);//Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (InstantiationException ex) {
                mpv5.logging.Log.Debug(ex);//Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                mpv5.logging.Log.Debug(ex);//Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
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

    /**
     * Default Color is Color.WHITE
     * @return
     */
    public Color getColor() {
        return color;
    }

    /**
     * 
     * @param color
     */
    public void defineColor(Color color) {
        this.color = color;
    }

    @Override
    public DatabaseObject clone() {
        DatabaseObject obj = getObject(context);
        obj.avoidNulls();
        List<Object[]> vals = this.getValues2();
        for (int i = 0; i < vals.size(); i++) {
            Object[] valuespairs = vals.get(i);
            try {
                if (valuespairs[1] != null) {
                    obj.parse(valuespairs[0].toString(), valuespairs[1]);
                }
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        }
        return obj;
    }

    /**
     * Clone this DO, but into a new {@link Context}
     * @param newContext
     * @return
     */
    public DatabaseObject clone(Context newContext) {
        DatabaseObject obj = getObject(newContext);
        obj.avoidNulls();
        List<Object[]> vals = this.getValues2();
        for (int i = 0; i < vals.size(); i++) {
            Object[] valuespairs = vals.get(i);
            try {
                if (valuespairs[1] != null) {
                    obj.parse(valuespairs[0].toString(), valuespairs[1]);
                }
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        }
        return obj;
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
    @Persistable(false)
    @Displayable(false)
    public Integer __getIDS() {
        return ids;
    }

    /**
     * Sets the unique id of this do,
     * usually not called manually.
     * @param ids
     */
    public void setIDS(int ids) {
        uncacheObject(this);
        this.ids = ids;
    }

    /**
     * Checks whether this do represents an existing row in the db
     * @return True if the do has been saved once
     */
    public boolean isExisting() {
        if (ids <= 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     *
     * @return A list of all <b>SETTERS</b> in this do child, except the native methods
     */
    public List<Method> setVars() {
        ArrayList<Method> list = new ArrayList<Method>();
        for (int i = 0; i < this.getClass().getMethods().length; i++) {
            if (this.getClass().getMethods()[i].getName().startsWith("set")
                    && !this.getClass().getMethods()[i].getName().startsWith("setVars")
                    && !this.getClass().getMethods()[i].getName().startsWith("setPanelData")
                    && !this.getClass().getMethods()[i].getName().startsWith("setAutoLock")) {
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
     * OR use the new annotations for persistable DO fields
     */
    public List<Method> getVars() {
        ArrayList<Method> list = new ArrayList<Method>();
        Method[] methods = this.getClass().getMethods();

        for (int i = 0; i < this.getClass().getMethods().length; i++) {
            if ((methods[i].isAnnotationPresent(Persistable.class)
                    && methods[i].getAnnotation(Persistable.class).value()
                    && methods[i].getParameterTypes().length == 0)
                    /*for backwards compatibility*/
                    || (methods[i].getName().startsWith("__get")
                    && !(methods[i].isAnnotationPresent(Persistable.class)
                    && !methods[i].getAnnotation(Persistable.class).value()))) {
                list.add(methods[i]);
            }
        }

        return list;
    }

    /**
     * Convenience method:
     * Returns all DO properties (getters) which would return plain String values
     * @return A list of shortened method names (without "get")
     */
    public List<String> getStringVars() {

        String left = "";
        Method[] methods = this.getClass().getMethods();
        ArrayList<String> list = new ArrayList<String>();
        boolean annotated = false;
        for (int i = 0; i < this.getClass().getMethods().length; i++) {
            if ((methods[i].isAnnotationPresent(Persistable.class)
                    && methods[i].getAnnotation(Persistable.class).value()
                    && methods[i].getParameterTypes().length == 0)
                    /*for backwards compatibility*/
                    || (methods[i].getName().startsWith("__get")
                    && !(methods[i].isAnnotationPresent(Persistable.class)
                    && !methods[i].getAnnotation(Persistable.class).value()))) {
                annotated = methods[i].isAnnotationPresent(Persistable.class);

                left = annotated
                        ? methods[i].getName().toLowerCase().substring(3, methods[i].getName().length())
                        : methods[i].getName().toLowerCase().replace("__get", "");
                if (methods[i].getReturnType().getName().equals(String.class.getName())) {
                    list.add(left);
                }
            }
        }
        return list;
    }

    /**
     * Save this do to db, or update if it has a valid uid already
     * @return
     */
    public boolean save() {
        return save(false);
    }

    /**
     * Save this do to db, or update if it has a valid uid already
     * @param silent If true, no notifications are sent to the history handler regarding this call to save
     * @return true if the save did not throw any errors
     */
    public boolean save(boolean silent) {
        if (!this.isReadOnly()) {
            String message = null;
            uncacheObject(this);

            List<DatabaseObjectModifier> mods = MPPLuginLoader.registeredModifiers;
            for (int ik = 0; ik < mods.size(); ik++) {
                DatabaseObjectModifier databaseObjectModifier = mods.get(ik);
                try {
                    Log.Debug(this, "Passing to plugin: " + databaseObjectModifier);
                    databaseObjectModifier.modifyOnSave(this);
                } catch (ChangeNotApprovedException e) {
                    Log.Debug(DatabaseObject.class, "Error while on-save modifying Object " + this + " within Modifier " + databaseObjectModifier);
                    Log.Debug(DatabaseObject.class, e.getMessage());
                    return false;
                } catch (Exception e) {
                    Log.Debug(DatabaseObject.class, "Error while on-save modifying Object " + this + " within Modifier " + databaseObjectModifier);
                    Log.Debug(e);
                }
            }

            try {
                if (ids <= 0) {
                    try {
                        ensureUniqueness();
                    } catch (Exception e) {
                        Popup.error(e);
                        return false;
                    }
                    if (__getCName() != null && __getCName().length() > 0) {
                        Log.Debug(this, "Inserting new dataset into: " + this.getContext());
                        dateadded = new Date();
                        intaddedby = mpv5.db.objects.User.getCurrentUser().__getIDS();
                        if (!silent && !this.getType().equals(new HistoryItem().getType())) {
                            message = this.__getCName() + Messages.INSERTED;
                        }
                        ids = QueryHandler.instanceOf().clone(context).insert(collect(), message);
                        Log.Debug(this, "The inserted row has id: " + IDENTITY);
                    } else {
                        Popup.notice(Messages.CNAME_CANNOT_BE_NULL);
                        Log.Debug(this, Messages.CNAME_CANNOT_BE_NULL + " [" + context + "]");
                        return false;
                    }

                    if (this instanceof Triggerable) {
                        ((Triggerable) this).triggerOnCreate();
                    }

                    for (int ik = 0; ik < mods.size(); ik++) {
                        DatabaseObjectModifier databaseObjectModifier = mods.get(ik);
                        try {
                            Log.Debug(this, "Passing to plugin: " + databaseObjectModifier);
                            databaseObjectModifier.modifyAfterCreate(this);
                        } catch (Exception e) {
                            Log.Debug(DatabaseObject.class, "Error while after-create modifying Object " + this + " within Modifier " + databaseObjectModifier);
                            Log.Debug(e);
                        }
                    }
                } else {
                    Log.Debug(this, "Updating dataset: " + ids + " within context '" + context + "'");
                    if (!silent) {
                        message = this.__getCName() + Messages.UPDATED;
                    }
                    QueryHandler.instanceOf().clone(context).update(collect(), ids, message);

                    if (this instanceof Triggerable) {
                        ((Triggerable) this).triggerOnUpdate();
                    }
                }

                final String fmessage = message;
                final String fdbid = this.getDbIdentity();
                final int fids = this.ids;
                final int fgids = this.groupsids;
                //Ignore History and User events

                if (!silent && Context.getArchivableContexts().contains(context)) {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(fmessage, mpv5.db.objects.User.getCurrentUser().__getCName(), fdbid, fids, fgids);
                        }
                    };
                    new Thread(runnable).start();
                }

                return true;
            } catch (Exception e) {
                Log.Debug(e);
                return false;
            }
        } else {
            mpv5.YabsViewProxy.instance().addMessage("You cannot alter the read only object: " + this);//TODO: l10n
            return true;//fake it for the ui
        }
    }

    /**
     * Safely import a database object from external sources (xml, csv etc)<br/>
     * Override and call this from the overriding method for ensuring the existance of DObject specific mandatory values.
     * @return
     */
    public boolean saveImport() {
        Log.Debug(this, "Starting import..");
        Log.Debug(this, "Setting IDS to -1");
        ids = -1;
        Log.Debug(this, "Setting intaddedby to " + mpv5.db.objects.User.getCurrentUser().__getIDS());
        intaddedby = mpv5.db.objects.User.getCurrentUser().__getIDS();

        if (groupsids <= 0 || !DatabaseObject.exists(Context.getGroup(), groupsids)) {
            Log.Debug(this, "Setting groups to users group.");
            groupsids = mpv5.db.objects.User.getCurrentUser().__getGroupsids();
        }

        if (__getCName() == null || __getCName().length() == 0) {
            setCName(RandomText.getText());
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
            try {
                return fetchDataOf(ids);
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        }
        return false;
    }

    /**
     * Deletes this do from database, can not be reverted!
     * Note: You can reuse this do then as it it would be a new one with prepopulated data
     * @return
     */
    public boolean delete() {

        List<DatabaseObjectModifier> mods = MPPLuginLoader.registeredModifiers;
        for (int ik = 0; ik < mods.size(); ik++) {
            DatabaseObjectModifier databaseObjectModifier = mods.get(ik);
            try {
                databaseObjectModifier.modifyOnDelete(this);
            } catch (ChangeNotApprovedException e) {
                Log.Debug(DatabaseObject.class, "Error while on-save modifying Object " + this + " within Modifier " + databaseObjectModifier);
                Log.Debug(e);
                return false;
            } catch (Exception e) {
                Log.Debug(DatabaseObject.class, "Error while on-delete modifying Object " + this + " within Modifier " + databaseObjectModifier);
                Log.Debug(e);
            }
        }

        boolean result = false;
        String message = null;
        uncacheObject(this);
        if (!this.getContext().equals(getHistory())) {
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

            if (this instanceof Triggerable) {
                ((Triggerable) this).triggerOnDelete();
            }

            ValueProperty.deleteProperty(this, null);

            final String fmessage = message;
            final String fdbid = this.getDbIdentity();
            final int fids = this.ids;
            final int fgids = this.groupsids;
            if (!this.getType().equals(new HistoryItem().getType())) {
                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(fmessage, mpv5.db.objects.User.getCurrentUser().__getCName(), fdbid, fids, fgids);
                    }
                };
                SwingUtilities.invokeLater(runnable);
            }
        }
        setIDS(-1);
        return result;
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
     * @return
     */
    protected QueryData collect() {

        QueryData data = new QueryData();
        String left = "";
        Object tempval;
        Method[] methods = this.getClass().getMethods();
        boolean annotated = false;

        for (int i = 0; i < this.getClass().getMethods().length; i++) {
            if ((methods[i].isAnnotationPresent(Persistable.class)
                    && methods[i].getAnnotation(Persistable.class).value()
                    && methods[i].getParameterTypes().length == 0)
                    /*for backwards compatibility*/
                    || (methods[i].getName().startsWith("__get")
                    && !(methods[i].isAnnotationPresent(Persistable.class)
                    && !methods[i].getAnnotation(Persistable.class).value()))) {
                annotated = methods[i].isAnnotationPresent(Persistable.class);
                try {
                    left = annotated
                            ? methods[i].getName().toLowerCase().substring(3, methods[i].getName().length())
                            : methods[i].getName().toLowerCase().replace("__get", "");
                    Log.Debug(this, "Calling: " + methods[i]);
                    tempval = methods[i].invoke(this, (Object[]) null);
                    Log.Debug(this, "Collect: " + tempval.getClass().getName() + " : " + methods[i].getName() + " ? " + tempval);
                    if (tempval.getClass().isInstance(new String())) {
                        data.add(left, String.valueOf(tempval));
                    } else if (tempval.getClass().isInstance(true)) {
                        boolean c = (Boolean) tempval;
                        data.add(left, c);
                    } else if (tempval.getClass().isInstance(new Date())) {
                        data.add(left, DateConverter.getSQLDateString((Date) tempval));
                    } else if (tempval.getClass().isInstance(new RandomDate(null))) {
                        data.add(left, DateConverter.getSQLDateString((Date) tempval));
                    } else if (tempval.getClass().isInstance(new java.sql.Date(0))) {
                        data.add(left, DateConverter.getSQLDateString((Date) tempval));
                    } else if (tempval.getClass().isInstance(0)) {
                        //if the field is an IDS field an below 0, set it to 0
                        //as integer columns may not allow signed integers (eg. -1)
                        if (left.toLowerCase().endsWith("ids")) {
                            if (Integer.valueOf(tempval.toString()).intValue() < 0) {
                                Log.Debug(this, "Correcting below-zero integer ids in " + left);
                                data.add(left, 0);
                            } else {
                                data.add(left, (Integer) tempval);
                            }
                        } else {
                            data.add(left, (Integer) tempval);
                        }
                    } else if (tempval.getClass().isInstance(0f)) {
                        data.add(left, (Float) tempval);
                    } else if (tempval.getClass().isInstance(0d)) {
                        data.add(left, (Double) tempval);
                    } else if (tempval.getClass().isInstance(01)) {
                        data.add(left, (Short) tempval);
                    } else if (tempval.getClass().isInstance(new BigDecimal(0))) {
                        data.add(left, (BigDecimal) tempval);
                    }
                } catch (Exception ex) {
                    mpv5.logging.Log.Debug(this, methods[i].getName());
                    mpv5.logging.Log.Debug(ex);//Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return data;
    }

    /**
     * Parses the given DataPanel into this do.
     * Each of the DataPanel's fields which has a name ending with underscore must match
     * one of the fields in this do child (without underscore)
     * @param source The DataPanel to parse.
     * @return false if the view parsing did not return successfully
     */
    public boolean getPanelData(DataPanel source) {

        if (!source.collectData()) {
            return false;
        }
        List<Method> vars = setVars();
        for (int i = 0; i < vars.size(); i++) {

            String fieldname = vars.get(i).getName().toLowerCase().substring(3, vars.get(i).getName().length()) + "_";
            if (!fieldname.equals("ids_")) {
                try {
                    Log.Debug(this, "GetPanelData: " + fieldname + "_ : " + source.getClass().getField(fieldname).
                            getType().getName() + " [" + source.getClass().getField(fieldname).get(source) + "]");
                    vars.get(i).invoke(this, source.getClass().getField(fieldname).get(source));
                } catch (java.lang.NoSuchFieldException nf) {
                    Log.Debug(this, "The view: " + source.getClass()
                            + " is missing a field: " + nf.getMessage());
                } catch (Exception n) {
                    Log.Debug(this, n);
                }
            }
        }

        return true;
    }

    /**
     * Injects this do into a Datapanel
     * Each of the DataPanel's fields wich has a name ending with underscore must match
     * one of the fields in this do child (without underscore)
     *
     * FIXME remove dependency on __set names
     * @param target
     */
    public void setPanelData(DataPanel target) {

        List<Method> vars = getVars();

        for (int i = 0; i < vars.size(); i++) {
            try {
//                Log.Debug(this, vars.get(i).getName());
                target.getClass().getField(vars.get(i).getName().toLowerCase().substring(5, vars.get(i).getName().length()) + "_").set(target, vars.get(i).invoke(this, new Object[0]));
            } catch (java.lang.NoSuchFieldException nf) {
                Log.Debug(this, "The view: " + target
                        + " is missing a field: " + nf.getMessage());
            } catch (Exception n) {
                Log.Debug(this, n.getMessage() + " in " + target);
                Log.Debug(n);
            }
        }
    }

    /**
     *
     * @return A list containing pairs of <b>VARNAME</b> and their  <b>VALUE</b> (as unformatted String) of this Databaseobject,
     * those which return in <code>getVars()</code>, as two-fields String-Array.
     * Example: new String[]{"CName", "Michael"}
     */
    public List<String[]> getValues() {
        List<Method> vars = getVars();
        List<String[]> vals = new ArrayList<String[]>();

        for (int i = 0; i < vars.size(); i++) {
            try {
                if (!vars.get(i).getName().substring(5, vars.get(i).getName().length()).toUpperCase().startsWith("DATE")) {
                    vals.add(new String[]{vars.get(i).getName().substring(5, vars.get(i).getName().length()),
                                String.valueOf(vars.get(i).invoke(this, new Object[0]))});
                } else {
                    String date = null;
                    try {
                        date = DateConverter.getDefDateString((Date) vars.get(i).invoke(this, new Object[0]));
                    } catch (Exception n) {
                        date = DateConverter.getDefDateString(new Date());
                    }
                    vals.add(new String[]{vars.get(i).getName().substring(5, vars.get(i).getName().length()),
                                date});
                }
            } catch (Exception n) {
                Log.Debug(this, n);
            }
        }
        return vals;
    }

    /**
     *
     * @return A list containing pairs of <b>VARNAME</b> and their  <b>VALUE (as formatted String, and variables resolved)</b>  of this Databaseobject,
     * those which return in <code>getVars()</code>, as two-fields String-Array.
     * Example: new String[]{"dateadded", "24.22.2980"}
     */
    public List<String[]> getValues3() {
        List<Method> vars = getVars();
        List<String[]> vals = new ArrayList<String[]>();

        for (int i = 0; i < vars.size(); i++) {
            try {
                String name = vars.get(i).getName().substring(5, vars.get(i).getName().length());
                Object value = vars.get(i).invoke(this, new Object[0]);

                if (name.startsWith("is") || name.toUpperCase().startsWith("BOOL")) {
                    if (String.valueOf(value).equals("1") || String.valueOf(value).toUpperCase().equals("TRUE")) {
                        vals.add(new String[]{name, "1"});
                    } else {
                        vals.add(new String[]{name, "0"});
                    }
                } else if (!name.toUpperCase().startsWith("INTERNAL") && (name.toUpperCase().startsWith("INT") || name.endsWith("uid") || name.endsWith("ids") || name.equals("ids"))) {
                    vals.add(new String[]{name, value.toString()});
                } else if (name.toUpperCase().startsWith("DATE") || name.toUpperCase().endsWith("DATE")) {
                    vals.add(new String[]{name, DateConverter.getDefDateString(DateConverter.getDate(value))});
                } else if (name.toUpperCase().startsWith("VALUE") || name.toUpperCase().endsWith("VALUE")) {
                    vals.add(new String[]{name, FormatNumber.formatDezimal(FormatNumber.parseDezimal(value.toString()))});
                } else {
                    vals.add(new String[]{name, VariablesHandler.parse(String.valueOf(value), this)});
                }
            } catch (Exception n) {
                Log.Debug(this, n);
            }
        }
        return vals;
    }

    /**
     *
     * @return A list containing pairs of <b>VARNAME</b> and their <b>VALUE</b> of this Databaseobject,
     * those which return in <code>getVars()</code>, as two-fields Object-Array.
     * Example: new Object[]{"dateadded", java.util.Date }
     */
    public List<Object[]> getValues2() {
        List<Method> vars = getVars();
        List<Object[]> vals = new ArrayList<Object[]>();

        for (int i = 0; i < vars.size(); i++) {
            try {
//                if (!vars.get(i).getName().substring(5, vars.get(i).getName().length()).toUpperCase().startsWith("DATE")) {
                vals.add(new Object[]{vars.get(i).getName().substring(5, vars.get(i).getName().length()),
                            (vars.get(i).invoke(this, new Object[0]))});
//                }
            } catch (Exception n) {
                Log.Debug(this, n);
            }
        }

        return vals;
    }

    /**
     *
     * @return A list containing pairs of <b>VARNAME</b> and their <b>VALUE</b> of this Databaseobject,
     * those which return in <code>getVars()</code>, as two-fields Object-Array.
     * Referenced DatabaseObjects are resolved as well. Flagged @Displayable(false) ones are ignored.
     * Example: new Object[]{"dateadded", java.util.Date }
     */
    public HashMap<String, Object> getValues4() {
        List<Method> vars = getVars();
        HashMap<String, Object> data = new HashMap<String, Object>();
        String left = "";
        Object tempval;

        for (int i = 0; i < vars.size(); i++) {
            try {
                Method m = vars.get(i);
                if (!(m.isAnnotationPresent(Displayable.class)
                        && !m.getAnnotation(Displayable.class).value())) {
                    left = m.getName().toLowerCase().substring(5, m.getName().length());
                    Log.Debug(this, "Calling: " + m);
                    tempval = m.invoke(this, (Object[]) null);
                    Log.Debug(this, "Collect: " + tempval.getClass().getName() + " : " + m.getName() + " ? " + tempval);
                    if (tempval.getClass().isInstance(new String())) {
                        data.put(left, String.valueOf(tempval));
                    } else if (tempval.getClass().isInstance(true)) {
                        boolean c = (Boolean) tempval;
                        data.put(left, c);
                    } else if (tempval.getClass().isInstance(new Date())) {
                        data.put(left, ((Date) tempval));
                    } else if (tempval.getClass().isInstance(new RandomDate(null))) {
                        data.put(left, ((Date) tempval));
                    } else if (tempval.getClass().isInstance(new java.sql.Date(0))) {
                        data.put(left, ((Date) tempval));
                    } else if (tempval.getClass().isInstance(0)) {
                        //if the field is an IDS field
                        if (left.toLowerCase().endsWith("ids")) {
                            if (Context.getMatchingContext(left) != null) {
                                Log.Debug(this, "Found Context for " + left);
                                try {
                                    data.put(left, getObject(Context.getMatchingContext(left), (Integer) tempval));
                                } catch (NodataFoundException nodataFoundException) {
                                    data.put(left, nodataFoundException);
                                }
                            } else {
                                data.put(left, (Integer) tempval);
                            }
                        } else if (left.toLowerCase().contains("group")) {
                            Log.Debug(this, "Found potential Group Context for " + left);
                            try {
                                data.put(left, getObject(Context.getGroup(), (Integer) tempval));
                            } catch (NodataFoundException nodataFoundException) {
                                data.put(left, (Integer) tempval);
                            }
                        } else {
                            data.put(left, (Integer) tempval);
                        }
                    } else if (tempval.getClass().isInstance(0f)) {
                        data.put(left, (Float) tempval);
                    } else if (tempval.getClass().isInstance(0d)) {
                        data.put(left, (Double) tempval);
                    } else if (tempval.getClass().isInstance(01)) {
                        data.put(left, (Short) tempval);
                    } else if (tempval.getClass().isInstance(new BigDecimal(0))) {
                        data.put(left, (BigDecimal) tempval);
                    }
                }
            } catch (Exception n) {
                Log.Debug(this, n);
            }
        }

        return data;
    }

    /**
     * Searches for a specific dataset, cached or non-cached
     * @param entity 
     * @return A database object with data, or null if none found
     * @throws NodataFoundException
     */
    public static DatabaseObject getObject(Entity entity) throws NodataFoundException {
        return getObject(entity.getKey(), entity.getValue());
    }

    /**
     * Searches for a specific dataset, cached or non-cached
     * @param context The context to search under
     * @param id The id of the object
     * @return A database object with data, or null if none found
     * @throws NodataFoundException
     */
    public static DatabaseObject getObject(final Context context, final int id) throws NodataFoundException {
        if (id > 0) {
            DatabaseObject cdo = DatabaseObject.getCachedObject(context, id);
            if (cdo == null) {
                try {
                    Object obj = context.getIdentityClass().newInstance();
                    ((DatabaseObject) obj).fetchDataOf(id);
                    ((DatabaseObject) obj).context = context;
                    cacheObject((DatabaseObject) obj);
                    return (DatabaseObject) obj;
                } catch (InstantiationException ex) {
                    mpv5.logging.Log.Debug(ex);//Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    mpv5.logging.Log.Debug(ex);//Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                return cdo;
            }
        } else {
            throw new NodataFoundException(context, id);
        }
        return null;
    }

    /**
     * Searches for a specific dataset by name
     * @param context The context to search under
     * @param cname
     * @return A database object with data, or null if none found
     * @throws NodataFoundException 
     */
    public static DatabaseObject getObject(Context context, String cname) throws NodataFoundException {
        try {
            Object obj = context.getIdentityClass().newInstance();
            if (((DatabaseObject) obj).fetchDataOf(cname)) {
                cacheObject((DatabaseObject) obj);
                ((DatabaseObject) obj).context = context;
                return (DatabaseObject) obj;
            } else {
                throw new NodataFoundException(context);
            }
        } catch (InstantiationException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Searches for a specific dataset by name
     * @param context The context to search under
     * @param column
     * @param value
     * @return A database object with data, or null if none found
     * @throws NodataFoundException
     */
    public static DatabaseObject getObject(Context context, String column, Object value) throws NodataFoundException {
        try {
            Object obj = context.getIdentityClass().newInstance();
            if (((DatabaseObject) obj).fetchDataOf(column, value)) {
                cacheObject((DatabaseObject) obj);
                ((DatabaseObject) obj).context = context;
                return (DatabaseObject) obj;
            } else {
                throw new NodataFoundException(context);
            }
        } catch (InstantiationException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
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
                ((DatabaseObject) obj).context = context;
                return (DatabaseObject) obj;
            } catch (InstantiationException ex) {
                mpv5.logging.Log.Debug(ex);//Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                mpv5.logging.Log.Debug(ex);//Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Log.Debug(DatabaseObject.class, "No identity class found for: " + context);
        }
        return null;
    }

    /**
     * Returns all DBOs in the specific context, VERY SLOW!
     * Use {@link getObjects(Context context, boolean withCached)} instead
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
     * Returns all DBOs in the specific context
     * @param <T>
     * @param context
     * @param withCached If true, checks the cache first for matching objects (faster)
     * @return A list of DBOs
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> ArrayList<T> getObjects(Context context, boolean withCached) throws NodataFoundException {
        ArrayList<T> list = new ArrayList<T>();
        if (!withCached) {
            return (ArrayList<T>) getObjects(DatabaseObject.getObject(context), null);
        } else {
            Object[] ids = QueryHandler.instanceOf().clone(context).getColumn("ids", 0);
            for (int i = 0; i < ids.length; i++) {
                Integer id = Integer.valueOf(ids[i].toString());
                DatabaseObject x = DatabaseObject.getCachedObject(context, id);
                if (x != null) {
                    list.add((T) x);
                } else {
                    list.add((T) getObject(context, id));
                }
            }
        }
        return list;
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
     *  Returns objects within the given context which match the criterias in the given QueryCriteria object<br/>
     *  May get very <b>slow</b> with some hundreds objects.
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
            DatabaseObject[] f = explode(data, template, false, true);
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
    public static <T extends DatabaseObject> List<T> getReferencedObjects(DatabaseObject dataOwner, Context inReference, T targetType) throws NodataFoundException {

        Object[][] allIds = QueryHandler.instanceOf().clone(inReference).select("ids", new String[]{dataOwner.getDbIdentity() + "ids", dataOwner.__getIDS().toString(), ""});
        if (allIds.length == 0) {
            throw new NodataFoundException(inReference);
        }
        LinkedList<T> list = new LinkedList<T>();

        for (int i = 0; i < allIds.length; i++) {
            int id = Integer.valueOf(allIds[i][0].toString());
            DatabaseObject x = DatabaseObject.getCachedObject(inReference, id);
            if (x != null) {
                list.add((T) x);
            } else {
                list.add((T) getObject(inReference, id));
            }
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
    public static <T extends DatabaseObject> List<T> getReferencedObjects(T dataOwner, Context inReference) throws NodataFoundException {

        Object[][] allIds = QueryHandler.instanceOf().clone(inReference).select("ids", new String[]{dataOwner.getDbIdentity() + "ids", dataOwner.__getIDS().toString(), ""});
        LinkedList<T> list = new LinkedList<T>();

        for (int i = 0; i < allIds.length; i++) {
            int id = Integer.valueOf(allIds[i][0].toString());
            DatabaseObject x = DatabaseObject.getCachedObject(inReference, id);
            if (x != null) {
                list.add((T) x);
            } else {
                list.add((T) getObject(inReference, id));
            }
        }
        return list;
    }

    /**
     * Returns objects which match the given Context and are created within the given timeframe
     * @param <T>
     * @param context
     * @param timeframe
     * @return
     * @throws NodataFoundException 
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> ArrayList<T> getObjects(Context context, vTimeframe timeframe) throws NodataFoundException {

        ReturnValue data = data = QueryHandler.instanceOf().clone(context).select("*", new QueryCriteria2(), timeframe);

        DatabaseObject template = getObject(context);
        ArrayList<T> list = new ArrayList<T>(0);

        if (data.hasData()) {
            DatabaseObject[] f = explode(data, template, false, true);
            for (int i = 0; i < f.length; i++) {
                DatabaseObject databaseObject = f[i];
                list.add((T) databaseObject);
            }
        }

        Log.Debug(DatabaseObject.class, "Rows found: " + data.getData().length);
        return list;
    }

    /**
     * Fills this do with the data of the given dataset id
     * @param id
     * @return
     * @throws NodataFoundException
     */
    public boolean fetchDataOf(int id) throws NodataFoundException {
        explode(QueryHandler.instanceOf().clone(context).select(id), this, true, true);
        return true;
    }

    /**
     * Fills this do with the data of the given dataset id
     * @param column
     * @param value
     * @return
     * @throws NodataFoundException
     */
    public boolean fetchDataOf(String column, Object value) throws NodataFoundException {
        QueryCriteria c = new QueryCriteria(column, value);
        explode(QueryHandler.instanceOf().clone(context).select(c), this, true, true);
        return true;
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
            explode(data, this, true, true);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fills the return value's data (rows) into an array of dos if singleExplode is false, if not fills target with the first row
     */
    public static synchronized DatabaseObject[] explode(ReturnValue select, DatabaseObject target, boolean singleExplode, boolean lock) {

        DatabaseObject[] dos = null;
        if (!singleExplode) {
            dos = new DatabaseObject[select.getData().length];
        } else {
            dos = new DatabaseObject[1];
        }

        Log.Debug(DatabaseObject.class, "Preparing to explode rows: " + dos.length);

        for (int i = 0; i < dos.length; i++) {

            DatabaseObject dbo = null;
            List<Method> methods = null;
            if (!singleExplode) {
                dbo = DatabaseObject.getObject(target.getContext());
            } else {
                dbo = target;
            }

            methods = dbo.setVars();
            dos[i] = dbo;

            if (select.hasData()) {
                for (int j = 0; j < select.getData()[i].length; j++) {
                    String name = select.getColumnnames()[j].toLowerCase();

                    for (int k = 0; k < methods.size(); k++) {
                        if (name.equals("ids") || !(methods.get(k).isAnnotationPresent(Persistable.class)
                                && !methods.get(k).getAnnotation(Persistable.class).value())) {
                            if (methods.get(k).getName().toLowerCase().substring(3).equals(name)) {

                                //Debug section
                                String valx = "";
                                if (select.getData()[i][j] != null) {
                                    valx = select.getData()[i][j].getClass().getName();
                                } else {
                                    valx = "NULL VALUE!";
                                }
//                                Log.Debug(DatabaseObject.class, "Explode: " + methods.get(k).toGenericString() + " with " + select.getData()[i][j] + "[" + valx + "]");
//                            //End Debug Section

                                if (select.getData()[i][j] != null) {
                                    invoke(methods.get(k), select.getData()[i][j], dbo, valx);
                                }
                            }
                        }
                    }
                }
            }

            if (lock) {
                if (AUTO_LOCK && Context.getLockableContexts().contains(dbo.getContext())) {
                    Log.Debug(DatabaseObject.class, "Preparing to lock: " + dbo);
                    boolean lck = dbo.lock();
                    dbo.ReadOnly(!lck);
                    Log.Debug(DatabaseObject.class, "Locking was: " + lck);
                }
            }

            List<DatabaseObjectModifier> mods = MPPLuginLoader.registeredModifiers;
            for (int ik = 0; ik < mods.size(); ik++) {
                DatabaseObjectModifier databaseObjectModifier = mods.get(ik);
                try {
                    dbo = databaseObjectModifier.modifyOnExplode(dbo);
                } catch (Exception e) {
                    Log.Debug(DatabaseObject.class, "Error while on-explode modifying Object " + dbo + " within Modifier " + databaseObjectModifier);
                    Log.Debug(e);
                }
            }

//            Log.Debug(dbo.getClass(), "Exploded " + dbo.IDENTITY);
        }

        return dos;
    }

    /**
     * Tries to reflect the hash table into this do.
     * The hashtable's keys must match the methods retrieved by do.setVars()
     * @param values
     * @throws Exception
     */
    public void parse(Hashtable<String, Object> values) throws Exception {
        List<Method> vars = setVars();
//        Log.Debug(this, " ?? : " +  toHashTable.size());
        Object[][] data = ArrayUtilities.hashTableToArray(values);
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

                        //fix the int/internal confusion :-(
                    } else if (!name.toUpperCase().startsWith("INTERNAL") && (name.toUpperCase().startsWith("INT") || name.endsWith("uid") || name.endsWith("ids") || name.equals("ids"))) {
                        vars.get(k).invoke(this, new Object[]{Integer.valueOf(String.valueOf(data[row][1]))});
                    } else if (name.toUpperCase().startsWith("DATE") || name.toUpperCase().endsWith("DATE")) {
                        vars.get(k).invoke(this, new Object[]{DateConverter.getDate(data[row][1])});
                    } else if (name.toUpperCase().startsWith("VALUE") || name.toUpperCase().endsWith("VALUE")) {
                        try {
                            vars.get(k).invoke(this, new Object[]{Double.valueOf(String.valueOf(data[row][1]))});
                        } catch (Exception illegalAccessException) {
                            try {
                                vars.get(k).invoke(this, new Object[]{new BigDecimal(String.valueOf(data[row][1]))});
                            } catch (NumberFormatException n) {
                                Log.Debug(this, "Unable to parse number: " + String.valueOf(data[row][1]));
                            }
                        }
                    } else {
                        vars.get(k).invoke(this, new Object[]{String.valueOf(data[row][1])});
                    }
                }
            }
        }
    }

    /**
     * Creates a Set of Entries on runtime which reflect the actual getters of the databaseObject child
     * @return A set Set: String (cname), Class (java.lang.String)
     */
    public Set<Map.Entry<String, Class<?>>> getKeySet() {
        Set<Map.Entry<String, Class<?>>> s = new HashSet<Map.Entry<String, Class<?>>>();
        List<Method> vars = getVars();
        for (int i = 0; i < vars.size(); i++) {
            final Method method = vars.get(i);
            s.add(new Map.Entry<String, Class<?>>() {

                public String getKey() {
                    return method.getName().substring(5);
                }

                public Class<?> getValue() {
                    return method.getReturnType();
                }

                public Class<?> setValue(Class<?> value) {
                    //value ignored. not going to set a class :-)
                    return method.getReturnType();
                }
            });
        }

        return s;
    }

    /**
     * Tries to reflect the hash table into this do.
     * The hashtable's keys must match the methods retrieved by do.setVars()
     * @param values
     * @throws Exception
     */
    public void parse(HashMap<String, Object> values) throws Exception {
        List<Method> vars = setVars();
//        Log.Debug(this, " ?? : " +  toHashTable.size());
        Object[][] data = ArrayUtilities.hashMapToArray(values);
//        Log.Debug(this, " ?? : " +  data[0]);
        for (int row = 0; row < data.length; row++) {
            String name = data[row][0].toString().toLowerCase();
// Log.Debug(this, name + " ?? : " + " = " + data[row][1]);
            for (int k = 0; k < vars.size(); k++) {
//                Log.Debug(this, name + " ?? : " + vars.get(k).getName() + " = " + data[row][1]);
                if (vars.get(k).getName().toLowerCase().substring(3).equals(name)) {
//                    Log.Debug(this, name + " ?? : " + vars.get(k).getName() + " = " + data[row][1]);
                    invoke(vars.get(k), data[row][1], this, name);
                }
            }
        }
    }

    /**
     * Tries to reflect the {@link SimpleDatabaseObject} into a new do.
     * The simple objects name must match the DatabaseObjects's {@link Context#getDbIdentity()}
     * @param sdo
     * @return
     * @throws Exception
     */
    public static DatabaseObject parse(SimpleDatabaseObject sdo) throws Exception {
        Log.Debug(DatabaseObject.class, "Parsing " + sdo);
        DatabaseObject newd = DatabaseObject.getObject(Context.getMatchingContext(sdo.getContext()));
        Hashtable<String, Object> data = new Hashtable<String, Object>();
        Method[] m = sdo.getClass().getMethods();

        for (int i = 0; i < m.length; i++) {
            Method method = m[i];
            if (method.getName().startsWith("get")) {
                Object o = method.invoke(sdo, new Object[]{});
                if (o != null) {
                    data.put(method.getName().substring(3), o);
                }
            }
        }
        newd.parse(data);
        return newd;
    }

    /**
     * Tries to inject the dos data into the given {@link SimpleDatabaseObject}.
     * The simple objects name must match the DatabaseObjects's {@link Context#getDbIdentity()}
     * @param sdo
     * @throws Exception
     */
    public void inject(SimpleDatabaseObject sdo) throws Exception {
        Log.Debug(DatabaseObject.class, "Injecting " + sdo);
        Method[] m = sdo.getClass().getMethods();

        for (int i = 0; i < m.length; i++) {
            Method method = m[i];
            if (method.getName().startsWith("set")) {
                Object o = method.invoke(sdo, new Object[]{this.getClass().getMethod(method.getName().replace("set", "get"),
                            (Class[]) null).invoke(this, new Object[0])});
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
        return cname + (readOnly ? " [Read-only]" : "");//TODO: l10n
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
    @Displayable(false)
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
            return (context.equals(((DatabaseObject) databaseObject).getContext()) && ids == ((DatabaseObject) databaseObject).__getIDS());
        }
    }

    @Override
    public int compareTo(DatabaseObject anotherObject) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (anotherObject == null) {
            return BEFORE;
        }

        if (this == anotherObject) {
            return EQUAL;
        }

        if (this.equals(anotherObject)) {
            return EQUAL;
        }

        // items grouped by context..
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
    public static void setAutoLockEnabled(boolean active) {
        AUTO_LOCK = active;
    }

    /**
     * Resolves referencing ids in the map to their named values.<br/>
     * Child classes should override this and call super.resolveReferences(HashMap<String, String> map) in the overriding method
     * @param map
     * @return
     */
    public HashMap<String, Object> resolveReferences(HashMap<String, Object> map) {
        List<ValueProperty> props = ValueProperty.getProperties(this);
        for (ValueProperty p : props) {
            map.put("property." + p.getKey(), String.valueOf(p.getValue()));
        }

        if (map.containsKey("groupsids")) {
            try {
                try {
                    Group g = (Group) DatabaseObject.getObject(Context.getGroup(), Integer.valueOf(map.get("groupsids").toString()));
                    map.put("group", g.__getCName());
                    map.put("grouppath", g.__getHierarchypath());
                    map.put("groupdescription", g.__getDescription());
                } catch (NodataFoundException ex) {
                    map.put("group", null);
                    map.put("grouppath", null);
                    map.put("groupdescription", null);
                    Log.Debug(this, ex.getMessage());
                }
            } catch (NumberFormatException numberFormatException) {
                //already resolved?
            }
        }
        try {
            if (map.containsKey("intaddedby")) {
                map.put("addedby", User.getUsername(Integer.valueOf(map.get("intaddedby").toString())));
            }
        } catch (NumberFormatException numberFormatException) {
            //already resolved?
        }
        List<DatabaseObjectModifier> mods = MPPLuginLoader.registeredModifiers;
        for (int ik = 0; ik < mods.size(); ik++) {
            DatabaseObjectModifier databaseObjectModifier = mods.get(ik);
            try {
                map = databaseObjectModifier.modifyOnResolve(map);
            } catch (Exception e) {
                Log.Debug(DatabaseObject.class, "Error while modificationg object map if " + this + " within Modifier " + databaseObjectModifier);
            }
        }

        return map;
    }

    /**
     * Tries to reflect the given key and value to a setter of this database object at runtime. Does convert nulls to the String "null".
     * @param key
     * @param value
     * @throws Exception
     */
    public void parse(String key, Object value) throws Exception {
        Hashtable<String, Object> map = new Hashtable<String, Object>();
        map.put(key, value);
//        Log.Debug(this, "Set: " + key + " to: " + value + " [" + value.getClass().getName() + "]");
        parse(map);
    }

    /**
     * Fill the do with (senseless) sample data
     */
//    @Deprecated
    public void fillSampleData() {
        List<Method> vars = setVars();
        for (int k = 0; k < vars.size(); k++) {

            try {
                String name = vars.get(k).getName().toLowerCase().substring(3);
                if (name.startsWith("is") || name.toUpperCase().startsWith("BOOL")) {
                    vars.get(k).invoke(this, new Object[]{new Random().nextBoolean()});
                } else if (!name.toUpperCase().startsWith("INTERNAL") && (name.toUpperCase().startsWith("INT") || name.endsWith("uid") || name.endsWith("ids") || name.equals("ids"))) {
                    vars.get(k).invoke(this, new Object[]{new Random().nextInt(100)});
                } else if (name.toUpperCase().startsWith("DATE") || name.toUpperCase().endsWith("DATE")) {
                    vars.get(k).invoke(this, new Object[]{DateConverter.getRandomDate()});
                } else if (name.toUpperCase().startsWith("VALUE") || name.toUpperCase().endsWith("VALUE")) {
                    vars.get(k).invoke(this, new Object[]{new Random().nextDouble()});
                } else {
                    vars.get(k).invoke(this, new Object[]{new RandomText().getString()});
                }
            } catch (IllegalAccessException illegalAccessException) {
            } catch (IllegalArgumentException illegalArgumentException) {
            } catch (InvocationTargetException invocationTargetException) {
            }
        }
    }

    /**
     * Generate an array out of the getValues2()
     * @return
     */
    public Object[] toArray() {
        List<Object[]> a = getValues2();
        Object[] b = new Object[a.size()];
        for (int i = 0; i < a.size(); i++) {
//            Object[] objects = a.get(i);
            b[i] = a.get(i)[1];
        }

        return b;
    }

    /**
     * Fetch the value of the given key
     * @param key
     * @return
     * @throws Exception Thrown if the given key is invalid for this object during runtime
     */
    public Object getValue(String key) throws Exception {
        List<Object[]> a = getValues2();
        for (int i = 0; i < a.size(); i++) {
            Object[] objects = a.get(i);
            if (String.valueOf(objects[0]).equalsIgnoreCase(key)) {
                return (String.valueOf(objects[1]).equals("null")) ? null : String.valueOf(objects[1]);
            }
        }
        throw new UnsupportedOperationException(key + " not known in " + this);
    }

    /**
     * Creates an Object Array out of all getters of this DO, no special order.
     * Mostly useless.
     * @return
     */
    public Object[] toResolvedArray() {
        HashMap<String, Object> data = getValues4();
        Object[] d = new Object[data.size()];
        int i = 0;
        for (Object object : data.values()) {
            d[i] = object;
            i++;
        }
        return d;
    }

    /**
     * Creates an Object Array out of all getters of this DO,
     * first column is a DatabaseObject.Entity to identify this DO
     * @param fieldCount Defines how many columns the resulting array shall have.
     *                   The ordering is somewhat unpredictable, however the array will always start with [IDENTITY, ...].
     *                   A fieldcount below 2 is not allowed.
     * @param fields     You can specify as many fields you like to force *some* ordering of the resulting array.
     *                   Specified fields will appear BEFORE unspecified fields, however the ordering of the specified fields is not guaranteed.
     *                   Nonexisting fields will be ignored.
     * @return An array representing this DOs values
     */
    public Object[] toResolvedArray(int fieldCount, final String... fields) {
        if (fieldCount < 2) {
            throw new UnsupportedOperationException("You must have more than 1 fields here.");
        }
        HashMap<String, Object> data = getValues4();
        Object[] d = new Object[data.size() + 1];
        final List<String> ff = Arrays.asList(fields);

        Map<String, Object> copy = new TreeMap<String, Object>(new Comparator<String>() {

            @Override
            public int compare(String a, String b) {
                if (a.equals(b)) {
                    return 0;
                }
                if (ff.contains(a)) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        copy.putAll(data);
        int i = 1;
        for (Object object : copy.values()) {
            d[i] = object;
            i++;
            if (i == fieldCount) {
            }
        }
        d[0] = IDENTITY;
        return d;
    }
}
