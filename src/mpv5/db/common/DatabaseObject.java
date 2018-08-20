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

import groovy.lang.GroovyShell;
import groovy.lang.Binding;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.Messages;
import mpv5.db.objects.HistoryItem;
import mpv5.handler.TemplateHandler;
import mpv5.logging.Log;
import mpv5.ui.panels.DataPanel;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.date.DateConverter;
import javax.swing.JComponent;
import mpv5.YabsViewProxy;
import mpv5.compiler.LazyInvocable;
import mpv5.db.objects.Group;
import mpv5.db.objects.User;
import mpv5.globals.LocalSettings;
import mpv5.handler.SimpleDatabaseObject;
import mpv5.handler.VariablesHandler;
import mpv5.pluginhandling.YabsPluginLoader;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.panels.ChangeNotApprovedException;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.date.RandomDate;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.images.MPIcon;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.text.RandomText;
import static mpv5.db.common.Context.*;
import mpv5.utils.text.TypeConversion;
import mpv5.globals.Constants;
import mpv5.globals.GlobalSettings;
import mpv5.handler.FormFieldsHandler;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.utils.export.Export;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.jobs.Job;
import mpv5.utils.jobs.Waiter;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.MethodClosure;

/**
 * Database Objects reflect a row in a table, and can parse graphical and
 * non-graphical beans to update or create itself to the database
 *
 * @author
 */
public abstract class DatabaseObject implements Comparable<DatabaseObject>, Serializable, Cloneable, Constants {

    private Map<String, Map<String, Object>> cachedFormFieldsByKey = new HashMap<String, Map<String, Object>>();
    private static InlineObjectModifier inlineObjectModifier = new InlineObjectModifier();

    /**
     * @return the cname
     */
    public String getCname() {
        return cname;
    }

    /**
     * @param cname the cname to set
     */
    public void setCname(String cname) {
        this.cname = cname;
    }

    

    /**
     * Represents a Context-ID pair which uniquely identifies a DatabaseObject
     *
     * @param <T>
     * @param <V>
     */
    public static class Entity<T extends Context, V> implements Serializable {

        private static final long serialVersionUID = 12L;
        private final Context ownerContext;
        private Integer ownerId;

//        /**
//         * Create a new Entity, nulls not allowed
//         * @param owner
//         */
//        public Entity(DatabaseObject owner) {
//            this.ownerContext = owner.getContext();
//            this.ownerId = owner.__getIDS();
//        }
        /**
         * Create a new Entity, nulls not allowed
         *
         * @param context
         * @param ids
         */
        public Entity(T context, Integer ids) {
            if (context == null || ids == null) {
                throw new NullPointerException("ids");
            }
            this.ownerContext = context;
            this.ownerId = ids;
        }

        /**
         * Returns the Context corresponding to this entry, never null
         *
         * @return
         */
        public Context getKey() {
            return ownerContext;
        }

        /**
         * Returns the ID corresponding to this entry, never null
         *
         * @return
         */
        public Integer getValue() {
            return ownerId;
        }

        /**
         * Compares the specified object with this entry for equality. Returns
         * <tt>true</tt> if the given object is also a map entry and Key and
         * Value.intValue are equal
         *
         * @param o
         */
        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof Entity)) {
                return false;
            }
            Entity e = (Entity) o;
            return e.hashCode() == hashCode();
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 11 * hash + (this.ownerContext != null ? this.ownerContext.hashCode() : 0);
            hash = 11 * hash + (this.ownerId != null ? this.ownerId.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return ownerContext + " [" + ownerId + "]";
        }
    }

    /**
     * Marks the value of the annotated getter to be persisted on {@link #save}
     * / annotated setter to be evaluated with a value from the database on
     * {@link #explode}. Default is true on methods with the signature start
     * '__get'.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Persistable {

        boolean value();
    }

    /**
     * Marks a relation getter
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Relation {

        boolean value();
    }

    /**
     * If set, the auto-database-schema creator will create a foreign key
     * reference to the {@link Context} with the given id. To be one of {@link Context#getId()
     * }.
     *
     * @deprecated not yet implemented
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface References {

        int value();
    }

    /**
     * If set, the auto-database-schema creator will create a ON DELETE CASCADE
     * reference to any {@link Context}s which have been annotated via the
     *
     * @References annotation on this method To be one of {@link Context#getId()
     * }.
     *
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
     * Cache all Objects which are within the {@link Context#getCacheableContexts()
     * }
     */
    public static void cacheObjects() {
        DatabaseObject.cacheObjects(Context.getCacheableContexts().toArray(new Context[]{}));
    }

    /**
     * Caches objects of this Context
     *
     * @param contextArray
     */
    public static void cacheObjects(final Context[] contextArray) {

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                Log.Debug(DatabaseObject.class, "Start caching objects..");
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
                            ReturnValue data = QueryHandler.instanceOf().clone(context, LocalSettings.getIntegerProperty(LocalSettings.CACHE_SIZE), true).select(false);
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

    public static synchronized void cacheObject(final DatabaseObject databaseObject) {
        if (databaseObject != null && databaseObject.__getIDS() > 0) {
            if (databaseObject.IDENTITY == null) {
                throw new IllegalStateException("IDENTITY must bes set!");
            }
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
//        if(context.equals(Context.getGroup()))Thread.dumpStack();
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
     * This method can be used to workaround the DatabaseObject#getObjects(...)
     * casting issues introduced by
     *
     * @me :-)
     *
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

    /**
     * This method can be used to workaround the DatabaseObject#getObjects(...)
     * casting issues introduced by
     *
     * @me :-)
     *
     * @param <T>
     * @param objects
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> List<DatabaseObject> toObjectList(List<T> objects) {
        List<DatabaseObject> l = new ArrayList<DatabaseObject>();
        for (int i = 0; i < objects.size(); i++) {
            DatabaseObject databaseObject = objects.get(i);
            l.add(databaseObject);
        }
        return l;
    }

    private static void invoke(Method method, Object argument, DatabaseObject dbo, Object valx) {
        synchronized (dbo) {
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
                        method.invoke(dbo, new Object[]{(byte[]) ((argument instanceof String) ? String.valueOf(argument).getBytes("UTF-8") : argument)});
                    } else if (DatabaseObject.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        if (argument instanceof DatabaseObject) {
                            DatabaseObject d = (DatabaseObject) argument;
                            method.invoke(dbo, new Object[]{d});
                        } else {
                            Context c = ((DatabaseObject) method.getParameterTypes()[0].newInstance()).getContext();
                            try {
                                int myId = Integer.valueOf(String.valueOf(argument));
                                if (myId > 0) {
                                    DatabaseObject d = getObject(c, myId);
                                    method.invoke(dbo, new Object[]{d});
                                }
                            } catch (NumberFormatException numberFormatException) {
                                try {
                                } catch (Exception e) {
                                    Log.Debug(dbo, "Cannot parse " + argument + " as Ids!" + numberFormatException.getMessage());
                                }
                            }
                        }
                    } else if (File.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        //a blob
                        File f = FileDirectoryHandler.getTempFile();
                        FileOutputStream stream = new FileOutputStream(f);
                        try {
                            stream.write((byte[]) argument);
                            stream.close();
                            method.invoke(dbo, new Object[]{f});
                        } finally {
                            stream.close();
                        }
                    } else {
                        try {
                            //defaults to java.lang.String, Object args are not supported.. possibly later via XMLEncoder?
                            method.invoke(dbo, new Object[]{(argument instanceof byte[])
                                ? new String((byte[]) argument)
                                : String.valueOf(argument)});
                        } catch (Exception uie) {
                            Log.Debug(dbo, new IllegalArgumentException(method + ": " + argument.getClass(), uie));
                        }
                    }
                } else {
                    if (int.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        method.invoke(dbo, new Object[]{Integer.valueOf(String.valueOf(argument))});
                    } else if (float.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        method.invoke(dbo, new Object[]{Float.valueOf(String.valueOf(argument))});
                    } else if (double.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        method.invoke(dbo, new Object[]{Double.valueOf(String.valueOf(argument))});
                    } else if (short.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        method.invoke(dbo, new Object[]{Short.valueOf(String.valueOf(argument))});
                    } else if (long.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        method.invoke(dbo, new Object[]{Long.valueOf(String.valueOf(argument))});
                    } else if (boolean.class.isAssignableFrom(method.getParameterTypes()[0])) {
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
    }
    /**
     * The unique id, or 0 if it is a new do
     */
    protected Integer ids = 0;
    private boolean readOnly = false;
    private boolean active = true;
    /**
     * The mandatory name
     */
    private String cname = "";
    private int groupsids = 1;
    private int intaddedby = 0;
    private Date dateadded = new Date(0);
    private transient DatabaseObjectLock LOCK = new DatabaseObjectLock(this);
    private Color color = Color.WHITE;
    /**
     *
     */
    public Entity<Context, Integer> IDENTITY;
    private static final Map<String, Map<String, Method>> setVars_cached = new HashMap<String, Map<String, Method>>();
    private static final Map<String, Map<String, Method>> getVars_cached = new HashMap<String, Map<String, Method>>();
    private static final Map<String, List<String>> getStringVars_cached = new HashMap<String, List<String>>();

    public String __getCname() {
        return getCname();
    }

    /**
     * Fills all {@link DatabaseObject#setVars()} with non-NULL default values
     */
    public void avoidNulls() {
        ArrayList<Method> vals = new ArrayList<Method>(setVars().values());
        for (int i = 0; i < vals.size(); i++) {
            Method method = vals.get(i);
            try {
                try {
                    if (!method.getParameterTypes()[0].isPrimitive()) {
                        if (method.getParameterTypes()[0].isInstance(new String())) {
                            Log.Debug(this, "Set : " + method + " with value: " + "<empty>");
                            method.invoke(this, "<empty>");
                        } else if (method.getParameterTypes()[0].isInstance(BigDecimal.ZERO)) {
                            Log.Debug(this, "Set : " + method + " with value: 0");
                            method.invoke(this, BigDecimal.ZERO);
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
     * Some DatabaseObjects have unique fields, and calling this method shall
     * ensure they are unique before saving. The native implementation actually
     * does nothing, you need to override the method if you define unique
     * columns for a DO.
     */
    public void ensureUniqueness() {
    }
    
    
    /**
     * Called before every @link save() invocation
     * The native implementation actually
     * does nothing, you need to override the method
     */
    public void onBeforeSave() { 
    }

    /**
     *
     * @return The preferred view for this do's data
     */
    public abstract JComponent getView();

    /**
     *
     * @return returns true if the do has a view
     */
    public boolean hasView() {
        return true;
    }

    /**
     * This can be used to graphically represent a do. The programmer has to
     * take care of the icon size! See
     * {@link MPIcon#getIcon(int width, int height)} It is recommended to use
     * 22*22 sized icons which do not need to get resized for performance
     * reasons.
     *
     * @return An Icon representing the type of this do
     */
    public abstract MPIcon getIcon();

    /**
     * Default Color is Color.WHITE
     *
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
        DatabaseObject obj = getObject(getContext());
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
     *
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
                Log.Print(valuespairs[0].toString(), valuespairs[1]);
                Log.Debug(ex);
            }
        }
         return obj;
    }

    /**
     * The type of a database object should equal the dbidentity in singular
     * form, but as the dbidentity can change over time, the type name must be
     * consistent
     *
     * @return The type of this do, an unique type identifier
     */
    public String getType() {
        return getDbIdentity().substring(0, getDbIdentity().length() - 1);
    }

    /**
     * The do's context
     *
     * @return A Context
     */
    public Context getContext() {
        return this.IDENTITY.getKey();
    }

    /**
     * The do's context
     *
     * @param c
     */
    protected void setContext(Context c) {
        this.IDENTITY = new Entity<Context, Integer>(c, ids);
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
     * Sets the unique id of this do, usually not called manually.
     *
     * @param ids
     */
    public void setIDS(int ids) {
        uncacheObject(this);
        this.ids = ids;
    }

    /**
     * Checks whether this do represents an existing row in the db
     *
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
     * @return A list of all <b>SETTERS</b> in this do child, except the native
     * methods will not recognize newly added methods (eg with groovy or
     * reflection) unless you clear {@code setVars_cached}
     */
    public synchronized Map<String, Method> setVars() {
        if (!setVars_cached.containsKey(getClass().getCanonicalName())) {
            Log.Debug(this, "Caching setVars of " + this.getClass().getCanonicalName());

            setVars_cached.put(this.getClass().getCanonicalName(), new HashMap<String, Method>());
            for (int i = 0; i < this.getClass().getMethods().length; i++) {
                Method m = this.getClass().getMethods()[i];
                if (m.getParameterTypes().length == 1
                        && m.getName().startsWith("set")
                        && !m.getName().startsWith("setVars")//annotation!!
                        && !m.getName().startsWith("setPanelData")
                        && !m.getName().startsWith("setAutoLock")) {
                    setVars_cached.get(this.getClass().getCanonicalName()).put(m.getName().substring(3).toLowerCase(), m);
                }
            }
        }
        return setVars_cached.get(getClass().getCanonicalName());
    }

    /**
     *
     * @return A list of all <b>getters with annotation Relation</b> in this do
     */
    public Map<String, Method> getAllRelationGetters() {
        String key = this.getClass().getCanonicalName() + "_Relation";
        if (!getVars_cached.containsKey(key)) {
            Log.Debug(this, "Caching " + key);
            getVars_cached.put(key, new HashMap<String, Method>());
            Method[] methods = this.getClass().getMethods();
            for (int i = 0; i < this.getClass().getMethods().length; i++) {
                if ((methods[i].isAnnotationPresent(Relation.class)
                        && methods[i].getAnnotation(Relation.class).value()
                        && methods[i].getParameterTypes().length == 0)) {
                    getVars_cached.get(key).put(methods[i].getName().substring(methods[i].getName().startsWith("__") ? 5 : 3).toLowerCase(), methods[i]);
                }
            }
        }
        return getVars_cached.get(key);
    }

    /**
     *
     * @return A list of all <b>getters</b> in this do child, which match the
     * naming convention as in <strong>__get*methodname*</strong> (methodname
     * starts with 2 (two) underscores) OR use the new annotations for
     * persistable DO fields
     */
    public Map<String, Method> getVars() {
        if (!getVars_cached.containsKey(getClass().getCanonicalName())) {
            Log.Debug(this, "Caching " + this.getClass().getCanonicalName());
            getVars_cached.put(this.getClass().getCanonicalName(), new HashMap<String, Method>());
            Method[] methods = this.getClass().getMethods();
            for (int i = 0; i < this.getClass().getMethods().length; i++) {
                if ((methods[i].isAnnotationPresent(Persistable.class)
                        && methods[i].getAnnotation(Persistable.class).value()
                        && methods[i].getParameterTypes().length == 0)
                        /*
                         * for backwards compatibility
                         */
                        || (methods[i].getName().startsWith("__get")
                        && !(methods[i].isAnnotationPresent(Persistable.class)
                        && !methods[i].getAnnotation(Persistable.class).value()))) {
                    getVars_cached.get(this.getClass().getCanonicalName()).put(methods[i].getName().substring(methods[i].getName().startsWith("__") ? 5 : 3).toLowerCase(), methods[i]);
                }
            }

        }
        return getVars_cached.get(this.getClass().getCanonicalName());
    }

    /**
     * Convenience method: Returns all DO properties (getters) which would
     * return plain String values
     *
     * @return A list of shortened method names (without "get")
     */
    public List<String> getStringVars() {
        if (!getStringVars_cached.containsKey(getClass().getCanonicalName())) {
            Log.Debug(this, "Caching " + this.getClass().getCanonicalName());
            getStringVars_cached.put(this.getClass().getCanonicalName(), new ArrayList<String>());
            String left = "";
            Method[] methods = this.getClass().getMethods();

            boolean annotated = false;
            for (int i = 0; i < this.getClass().getMethods().length; i++) {
                if ((methods[i].isAnnotationPresent(Persistable.class)
                        && methods[i].getAnnotation(Persistable.class).value()
                        && methods[i].getParameterTypes().length == 0)
                        /*
                         * for backwards compatibility
                         */
                        || (methods[i].getName().startsWith("__get")
                        && !(methods[i].isAnnotationPresent(Persistable.class)
                        && !methods[i].getAnnotation(Persistable.class).value()))) {
                    annotated = methods[i].isAnnotationPresent(Persistable.class);

                    left = annotated
                            ? methods[i].getName().toLowerCase().substring(3, methods[i].getName().length())
                            : methods[i].getName().toLowerCase().replace("__get", "");
                    if (methods[i].getReturnType().getName().equals(String.class.getName())) {
                        getStringVars_cached.get(this.getClass().getCanonicalName()).add(left);
                    }
                }
            }
        }
        return getStringVars_cached.get(this.getClass().getCanonicalName());
    }

    /**
     * Save this do to db, or update if it has a valid uid already
     *
     * @return
     */
    public boolean save() {
        return save(false);
    }

    /**
     * Save this do to db, or update if it has a valid uid already
     *
     * @param silent If true, no notifications are sent to the history handler
     * regarding this call to save
     * @return true if the save did not throw any errors
     */
    public boolean save(boolean silent) {
        cachedFormFieldsByKey.clear();
        if (!this.isReadOnly()) {
            String message = null;
            uncacheObject(this);

            onBeforeSave();
            
            List<DatabaseObjectModifier> mods = null;
            if (Context.getModifiableContexts().contains(getContext())) {
                mods = new ArrayList<DatabaseObjectModifier>(YabsPluginLoader.registeredModifiers);
                mods.add(inlineObjectModifier);
                for (int ik = 0; ik < mods.size(); ik++) {
                    DatabaseObjectModifier databaseObjectModifier = mods.get(ik);
                    try {
                        Log.Debug(this, "Passing to plugin: " + databaseObjectModifier);
                        databaseObjectModifier.modifyOnSave(this);
                    } catch (ChangeNotApprovedException e) {
                        Log.Debug(DatabaseObject.class, "Error while on-save modifying Object " + this + " within Modifier " + databaseObjectModifier);
                        Log.Debug(DatabaseObject.class, e.getMessage());
                        return false;
                    } catch (Throwable e) {
                        Log.Debug(DatabaseObject.class, "Error while on-save modifying Object " + this + " within Modifier " + databaseObjectModifier);
                        Log.Debug(e);
                    }
                }
            }
            try {
                if (ids <= 0) {
                    try {
                        ensureUniqueness();
                    } catch (Exception e) {
                        Notificator.raiseNotification(e, !silent, true, this);
                        return false;
                    }
                    if (__getCname() != null && __getCname().length() > 0) {
                        Log.Debug(this, "Inserting new dataset into: " + this.getContext());
                        dateadded = new Date();
                        intaddedby = mpv5.db.objects.User.getCurrentUser().__getIDS();
                        if (!silent && !this.getType().equals(new HistoryItem().getType())) {
                            message = this.__getCname() + Messages.INSERTED;
                        }
                        ids = QueryHandler.instanceOf().clone(getContext()).insert(collect(), message);
                        IDENTITY = new Entity<Context, Integer>(getContext(), ids);
                        Log.Debug(this, "The inserted row has id: " + IDENTITY);
                    } else {
                        Popup.notice(Messages.CNAME_CANNOT_BE_NULL + ": " + this.getType());
                        Log.Debug(this, new RuntimeException(Messages.CNAME_CANNOT_BE_NULL + " [" + getContext() + "]"));
                        return false;
                    }

                    if (this instanceof Triggerable) {
                        ((Triggerable) this).triggerOnCreate();
                    }

                    if (mods != null) {
                        for (int ik = 0; ik < mods.size(); ik++) {
                            DatabaseObjectModifier databaseObjectModifier = mods.get(ik);
                            try {
                                Log.Debug(this, "Passing to plugin: " + databaseObjectModifier);
                                databaseObjectModifier.executeAfterCreate(this);
                            } catch (Throwable e) {
                                Log.Debug(DatabaseObject.class, "Error while after-create executing Object " + this + " within Modifier " + databaseObjectModifier);
                                Log.Debug(e);
                            }
                        }
                    }
                } else {
                    Log.Debug(this, "Updating dataset: " + ids + " within context '" + getContext() + "'");
                    if (!silent) {
                        message = this.__getCname() + Messages.UPDATED;
                    }
                    QueryHandler.instanceOf().clone(getContext()).update(collect(), ids, message);

                    if (this instanceof Triggerable) {
                        ((Triggerable) this).triggerOnUpdate();
                    }
                }

                final String fmessage = message;
                final String fdbid = this.getDbIdentity();
                final int fids = this.ids;
                final int fgids = this.groupsids;
                //Ignore History and User events

                if (!silent && Context.getArchivableContexts().contains(getContext())) {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(fmessage, mpv5.db.objects.User.getCurrentUser().__getCname(), fdbid, fids, fgids);
                        }
                    };
                    new Thread(runnable).start();
                }
                if (mods != null) {
                    for (int ik = 0; ik < mods.size(); ik++) {
                        DatabaseObjectModifier databaseObjectModifier = mods.get(ik);
                        try {
                            Log.Debug(this, "Passing to plugin: " + databaseObjectModifier);
                            databaseObjectModifier.executeAfterSave(this);
                        } catch (Throwable e) {
                            Log.Debug(DatabaseObject.class, "Error while after-save executing Object " + this + " within Modifier " + databaseObjectModifier);
                            Log.Debug(e);
                        }
                    }
                }

                return true;
            } catch (Exception e) {
                Log.Debug(e);
                return false;
            }
        } else {
            mpv5.YabsViewProxy.instance().addMessage("You cannot alter the read only object: " + this, Color.RED);//TODO: l10n
            return true;//fake it for the ui
        }
    }

    /**
     * Safely import a database object from external sources (xml, csv etc)<br/>
     * Override and call this from the overriding method for ensuring the
     * existance of DObject specific mandatory values.
     *
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

        if (__getCname() == null || __getCname().length() == 0) {
            setCname(RandomText.getText());
        }

        return save();
    }

    /**
     * Reset the data of this do (reload from database and discard changes)
     *
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
     * Deletes this do from database, can possibly not get reverted! Note: You
     * can reuse this do then as it it would be a new one with prepopulated data
     *
     * @return
     */
    public boolean delete() {
        if (Context.getModifiableContexts().contains(getContext())) {
            List<DatabaseObjectModifier> mods = new ArrayList<DatabaseObjectModifier>(YabsPluginLoader.registeredModifiers);
            mods.add(inlineObjectModifier);
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
        }
        boolean result = false;
        String message = null;
        uncacheObject(this);
        if (!this.getContext().equals(getHistory())) {
            message = this.__getCname() + Messages.TRASHED;
        }
        if (ids > 0) {
            if (Context.getTrashableContexts().contains(getContext())) {
                Log.Debug(this, "Moving to trash:");
                QueryData d = new QueryData();
                d.add("invisible", 1);
                QueryHandler.instanceOf().clone(getContext()).update(d, new String[]{"ids", ids.toString(), ""}, message);
                result = true;
                Log.Debug(this, "The trashed row has id: " + ids);
            } else {
                Log.Debug(this, "Deleting dataset:");
                if (!this.getType().equals(new HistoryItem().getType())) {
                    message = this.__getCname() + Messages.DELETED;
                }
                result = QueryHandler.instanceOf().clone(getContext()).delete(new String[][]{{"ids", ids.toString(), ""}}, message);
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
                        QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(fmessage, mpv5.db.objects.User.getCurrentUser().__getCname(), fdbid, fids, fgids);
                    }
                };
                SwingUtilities.invokeLater(runnable);
            }
        }
        setIDS(-1);
        return result;
    }

    public boolean undelete() {

        boolean result = false;
        String message = null;
        message = this.__getCname() + " " + Messages.UNTRASHED;

        Log.Debug(this, "Removing from trash:");
        QueryData d = new QueryData();
        d.add("invisible", 0);
        QueryHandler.instanceOf().clone(getContext()).update(d, ids, message);
        QueryCriteria2 c2 = new QueryCriteria2();
        Context tr = new Context("trashbin", null);
        c2.and(new QueryParameter(tr, "rowid", ids, QueryParameter.EQUALS));
        c2.and(new QueryParameter(tr, "cname", getContext().getDbIdentity().toLowerCase(), QueryParameter.EQUALS));

        result = QueryHandler.instanceOf().clone(tr).delete(c2);

        Log.Debug(this, "The untrashed row has id: " + ids);

        final String fmessage = message;
        final String fdbid = this.getDbIdentity();
        final int fids = this.ids;
        final int fgids = this.groupsids;
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(fmessage, mpv5.db.objects.User.getCurrentUser().__getCname(), fdbid, fids, fgids);
            }
        };
        SwingUtilities.invokeLater(runnable);

        save(true);
        return result;
    }

    /**
     * Locks the dataset represented by this do. The dataset can not be edited
     * by someone else after locking it, until the locking user has logged-out.
     * This does not prevent the data to be viewed by anyone.
     *
     * @return
     */
    public boolean lock() {
        if (!this.isReadOnly()) {
            return LOCK.aquire();
        } else {
            Log.Debug(this, getCname() + " is readonly, lock not required.");
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
        return getContext().getDbIdentity();
    }

    /**
     * Collect the data to masked/valid DB String array
     *
     * @return
     */
    protected synchronized QueryData collect() {

        QueryData data = new QueryData();
        Map<String, Method> vars = getVars();

        for (String key : vars.keySet()) {
            Object tempval;
            String left = key.toLowerCase();
            try {
//                    Log.Debug(this, "Calling: " + methods[i]);
                tempval = vars.get(key).invoke(this, (Object[]) null);
                // Log.Debug(this, "On " + methods[i].getName());
                if (tempval != null) {
                    Log.Debug(this, "Collect: " + tempval.getClass().getName() + " : " + vars.get(key).getName() + " ? " + tempval);
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
                    } else if (tempval.getClass().isInstance(0l)) {
                        data.add(left, (Long) tempval);
                    } else if (tempval.getClass().isInstance(new BigDecimal(0))) {
                        data.add(left, (BigDecimal) tempval);
                    } else if (File.class.isAssignableFrom(tempval.getClass())) {
                        /*NOT supported by following INSERT! insert yourself..data.add(left, (File) tempval);*/
                        throw new IllegalArgumentException("File NOT supported by current INSERT implementation. Override save() method and insert blobs yourself.");
                    } else if (DatabaseObject.class.isAssignableFrom(tempval.getClass())) {
                        data.add(left + "sids", ((DatabaseObject) tempval).__getIDS());
                    } else {
                        throw new RuntimeException("Unsupported class found: " + tempval.getClass());
                    }
                } else {
                    Log.Debug(this, "NULL value on " + vars.get(key).getName());
                }
            } catch (Exception ex) {
                mpv5.logging.Log.Debug(this, vars.get(key).getName());
                mpv5.logging.Log.Debug(ex);//Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        return data;
    }

    /**
     * Parses the given DataPanel into this do. Each of the DataPanel's fields
     * which has a name ending with underscore must match one of the fields in
     * this do child (without underscore)
     *
     * @param source The DataPanel to parse.
     * @return false if the view parsing did not return successfully
     */
    public boolean getPanelData(DataPanel source) {

        if (!source.collectData()) {
            return false;
        }
        for (String key : setVars().keySet()) {
            String fieldname = key + "_";
            if (!fieldname.equals("ids_")) {
                try {
                    Log.Debug(this, "GetPanelData: " + fieldname + ": " + source.getClass().getField(fieldname).getType().getName() + " [" + source.getClass().getField(fieldname).get(source) + "]");
                    setVars().get(key).invoke(this, source.getClass().getField(fieldname).get(source));
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
     * Injects this do into a Datapanel Each of the DataPanel's fields wich has
     * a name ending with underscore must match one of the fields in this do
     * child (without underscore)
     *
     * FIXME remove dependency on __set names
     *
     * @param target
     */
    public void setPanelData(DataPanel target) {

        for (String key : getVars().keySet()) {
            try {
                Log.Debug(this, key + " [" + getVars().get(key).invoke(this, new Object[0]) + "]");
                Field fi = target.getClass().getField(key + "_");
                fi.set(target, getVars().get(key).invoke(this, new Object[0]));
                Log.Debug(target, fi.get(target));
            } catch (java.lang.NoSuchFieldException nf) {
                Log.Debug(this, "The view: " + target.getClass() + " is missing a field: " + nf.getMessage());
            } catch (Exception n) {
                Log.Debug(this, n.getMessage() + " in " + target.getClass());
                Log.Debug(n);
            }
        }
    }

    /**
     *
     * @return A list containing pairs of <b>VARNAME</b> and their <b>VALUE</b>
     * (as unformatted String) of this Databaseobject, those which return in
     * <code>getVars()</code>, as two-fields String-Array. Example: new
     * String[]{"CName", "Michael"}
     */
    public List<String[]> getValues() {
        Map<String, Method> vars = getVars();
        List<String[]> vals = new ArrayList<String[]>();

        for (String key : vars.keySet()) {
            try {
                if (Date.class.isAssignableFrom(vars.get(key).getReturnType())) {
                    String date = null;
                    try {
                        date = DateConverter.getDefDateString((Date) vars.get(key).invoke(this, new Object[0]));
                    } catch (Exception n) {
                        date = DateConverter.getDefDateString(new Date());
                    }
                    vals.add(new String[]{key, date});
                } else if (DatabaseObject.class.isAssignableFrom(vars.get(key).getReturnType())) {
                    try {
                        vals.add(new String[]{key,
                            String.valueOf(vars.get(key).invoke(this, new Object[0]))});
                    } catch (Exception ex) {
                        vals.add(new String[]{key, ""});
                    }
                } else {
                    vals.add(new String[]{key,
                        String.valueOf(vars.get(key).invoke(this, new Object[0]))});
                }
            } catch (Exception n) {
                Log.Debug(this, n);
                vals.add(new String[]{key, ""});
            }
        }
        return vals;
    }

    /**
     *
     * @return A list containing pairs of <b>VARNAME</b> and their <b>VALUE (as
     * formatted String, and variables resolved)</b> of this Databaseobject,
     * those which return in <code>getVars()</code>, as two-fields String-Array.
     * Example: new String[]{"dateadded", "24.22.2980"}
     */
    public List<String[]> getValues3() {
        Map<String, Method> vars = getVars();
        List<String[]> vals = new ArrayList<String[]>();

        for (String name : vars.keySet()) {
            try {
                Object value = vars.get(name).invoke(this, new Object[0]);

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
                } else if (name.toUpperCase().startsWith("CNUMBER")) {
                    vals.add(new String[]{name, String.valueOf(value)});
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
     * @return A list containing pairs of <b>VARNAME</b> and their <b>VALUE</b>
     * of this Databaseobject, those which return in <code>getVars()</code>, as
     * two-fields Object-Array. Example: new Object[]{"dateadded",
     * java.util.Date }
     */
    public List<Object[]> getValues2() {
        Map<String, Method> vars = getVars();
        List<Object[]> vals = new ArrayList<Object[]>();
        for (String name : vars.keySet()) {
            try {
                vals.add(new Object[]{name.toLowerCase(),
                    (vars.get(name).invoke(this, new Object[0]))});
            } catch (Exception n) {
                Log.Debug(this, "Failed to invoke " + vars.get(name) + " ( " + this + " ) ");
//                Log.Debug(this, n);
            }
        }

        return vals;
    }

    /**
     *
     * @return A list containing pairs of <b>VARNAME</b> and their <b>VALUE</b>
     * of this Databaseobject, those which return in
     * <code>getAllRelationGetters()</code>, as two-fields Object-Array.
     * Example: new Object[]{"dateadded", java.util.Date }
     */
    public List<Object[]> getValues5() {
        Map<String, Method> vars = getAllRelationGetters();
        List<Object[]> vals = new ArrayList<Object[]>();
        for (String name : vars.keySet()) {
            try {
                vals.add(new Object[]{name.toLowerCase(),
                    (vars.get(name).invoke(this, new Object[0]))});
            } catch (Exception n) {
                Log.Debug(this, "Failed to invoke " + vars.get(name) + " ( " + this + " ) ");
//                Log.Debug(this, n);
            }
        }

        return vals;
    }

    /**
     *
     * @return A list containing pairs of <b>VARNAME</b> and their <b>VALUE</b>
     * of this Databaseobject, those which return in <code>getVars()</code>, as
     * two-fields Object-Array. Referenced DatabaseObjects are resolved as well.
     * Flagged
     * @Displayable(false) ones are ignored. Example: new Object[]{"dateadded",
     * java.util.Date }
     */
    public HashMap<String, Object> getValues4() {
        Map<String, Method> vars = getVars();
        HashMap<String, Object> data = new HashMap<String, Object>();
        String left = "";
        Object tempval;

        for (String name : vars.keySet()) {
            try {
                Method m = vars.get(name);
                if (!(m.isAnnotationPresent(Displayable.class)
                        && !m.getAnnotation(Displayable.class).value())) {
                    left = name.toLowerCase();
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
     *
     * @param entity
     * @return A database object with data, or null if none found
     * @throws NodataFoundException
     */
    public static DatabaseObject getObject(Entity entity) throws NodataFoundException {
        return getObject(entity.getKey(), entity.getValue());
    }

    /**
     * Searches for a specific dataset, cached or non-cached
     *
     * @param entity
     * @return A database object with data, or null if none found
     * @throws NodataFoundException
     */
    public static DatabaseObject getObject(SerializableEntity entity) throws NodataFoundException {
        return getObject(Context.getMatchingContext(entity.getTablename()), entity.getId().intValue());
    }

    /**
     * Searches for a specific dataset, cached or non-cached, including deleted
     *
     * @param context The context to search under
     * @param id The id of the object
     * @return A database object with data, or null if none found
     * @throws NodataFoundException
     */
    public static DatabaseObject getObject(final Context context, final int id) throws NodataFoundException {
        return getObject(context, id, true);
    }

    /**
     * Searches for a specific dataset, cached or non-cached
     *
     * @param context The context to search under
     * @param id The id of the object
     * @param includeInvisible
     * @return A database object with data, or null if none found
     * @throws NodataFoundException
     */
    public static DatabaseObject getObject(final Context context, final int id, boolean includeInvisible) throws NodataFoundException {
        if (id > 0) {
            DatabaseObject cdo = null;
            if (context != null) {
                cdo = DatabaseObject.getCachedObject(context, id);
            }
            if (cdo == null) {
                try {
                    Object obj = context.getIdentityClass().newInstance();
                    ((DatabaseObject) obj).IDENTITY = new Entity<>(context, -1);
                    ((DatabaseObject) obj).fetchDataOf(id, includeInvisible);
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
     *
     * @param context The context to search under
     * @param cname
     * @return A database object with data, or null if none found
     * @throws NodataFoundException
     */
    public static DatabaseObject getObject(Context context, String cname) throws NodataFoundException {
        try {
            Object obj = context.getIdentityClass().newInstance();
            ((DatabaseObject) obj).IDENTITY = new Entity<>(context, -1);
            if (((DatabaseObject) obj).fetchDataOf(cname)) {
                cacheObject((DatabaseObject) obj);
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
     *
     * @param context The context to search under
     * @param column
     * @param value
     * @return A database object with data, or null if none found
     * @throws NodataFoundException
     */
    public static DatabaseObject getObject(Context context, String column, Object value) throws NodataFoundException {
        try {
            Object obj = context.getIdentityClass().newInstance();
            ((DatabaseObject) obj).IDENTITY = new Entity<>(context, -1);
            if (((DatabaseObject) obj).fetchDataOf(column, value)) {
                cacheObject((DatabaseObject) obj);
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
     * Returns an empty "sample" Object of the specified <code>Context</code>
     * type
     *
     * @param context
     * @return
     */
    public static DatabaseObject getObject(Context context) {

        if (context.getIdentityClass() != null) {
            try {
                Object obj = context.getIdentityClass().newInstance();
                ((DatabaseObject) obj).IDENTITY = new Entity<>(context, -1);
                ((DatabaseObject) obj).setIDS(0);
                return (DatabaseObject) obj;
            } catch (InstantiationException ex) {
                mpv5.logging.Log.Debug(ex);
            } catch (IllegalAccessException ex) {
                mpv5.logging.Log.Debug(ex);
            }
        } else {
            Log.Debug(DatabaseObject.class, "No identity class found for: " + context);
        }
        return null;
    }

    /**
     * Delegates to {@link getObjects(Context context, true)}
     *
     * @param <T>
     * @param context
     * @return A list of DBOs
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> ArrayList<T> getObjects(Context context) throws NodataFoundException {
        return (ArrayList<T>) getObjects(context, (QueryCriteria2) null, true);
    }

    /**
     * Delegates to {@link getObjects(Context context, true)}
     *
     * @param <T>
     * @param context
     * @param withCached
     * @return A list of DBOs
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> ArrayList<T> getObjects(Context context, boolean withCached) throws NodataFoundException {
        return (ArrayList<T>) getObjects(context, (QueryCriteria2) null, withCached);
    }

    /**
     * Returns all DBOs in the specific context
     *
     * @param <T>
     * @param context
     * @param criterias
     * @param withCached IGNORED
     * @return A list of DBOs
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> ArrayList<T> getObjects(Context context, QueryCriteria2 criterias, boolean withCached) throws NodataFoundException {

//        if (!withCached) { //quatsch
//            return (ArrayList<T>) getObjects(DatabaseObject.getObject(context), null);
//        } else {
        List<Integer> idlist;
        if (criterias != null) {
            idlist = QueryHandler.instanceOf().clone(context).selectIds(criterias);
        } else {
            idlist = QueryHandler.instanceOf().clone(context).selectIds(false);
        }
        return getObjects(context, idlist);
//        }
    }

    /**
     * Returns all DBOs in the specific context
     *
     * @param <T>
     * @param context
     * @param withCached IGNORED
     * @return A list of DBOs
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> ArrayList<T> getObjects(Context context, QueryCriteria criterias, boolean withCached) throws NodataFoundException {

//        if (!withCached) {
//            return (ArrayList<T>) getObjects(DatabaseObject.getObject(context), null);
//        } else {
        List<Integer> idlist;
        if (criterias != null) {
            idlist = QueryHandler.instanceOf().clone(context).selectIds(criterias);
        } else {
            idlist = QueryHandler.instanceOf().clone(context).selectIds(false);
        }
        return getObjects(context, idlist);
//        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> ArrayList<T> getObjects(Context context, Collection<Integer> listi) throws NodataFoundException {
        if (Log.LOGLEVEL_DEBUG == Log.getLoglevel()) {
            Log.Debug(DatabaseObject.class, "Fetching objects " + context + ": " + listi);
        }
        ArrayList<T> list = new ArrayList<T>();
        QueryCriteria2 criterias = new QueryCriteria2();
        List<QueryParameter> uncachedIds = new ArrayList<QueryParameter>();
        ArrayList<Integer> idlist = new ArrayList<Integer>(listi);
        for (int i = 0; i < idlist.size(); i++) {
            Integer id = idlist.get(i);
            DatabaseObject x = DatabaseObject.getCachedObject(context, id);
            if (x != null) {
                list.add((T) x);
            } else {
                uncachedIds.add(new QueryParameter(context, "ids", id, QueryParameter.EQUALS));
            }
        }
        if (uncachedIds.isEmpty()) {
            return list;//all from cache
        }
        criterias.list(uncachedIds);
        if (Context.getGroupableContexts().contains(context)) {
            criterias.setOrder("dateadded", false);
        }
        criterias.setIncludeInvisible(true);
//        Log.Debug(DatabaseObject.class, criterias.getQuery());
        ReturnValue data = QueryHandler.instanceOf().clone(context).select("*", criterias);
        if (data.hasData()) {
            DatabaseObject[] f = explode(data, getObject(context), false, true);
            for (int i = 0; i < f.length; i++) {
                DatabaseObject databaseObject = f[i];
                cacheObject(databaseObject);
                list.add((T) databaseObject);
            }
        }
        return list;
    }

    /**
     * Returns objects within the given context which match the criterias in the
     * given DataStringHandler
     *
     * @param <T>
     * @param context
     * @param criterias
     * @return
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> ArrayList< T> getObjects(Context context, QueryCriteria criterias) throws NodataFoundException {
        return (ArrayList<T>) getObjects(context, criterias, true);
    }

    /**
     * Returns objects within the given context which match the criterias in the
     * given DataStringHandler
     *
     * @param <T>
     * @param context
     * @param criterias
     * @return
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> ArrayList< T> getObjects(Context context, QueryCriteria2 criterias) throws NodataFoundException {
        return (ArrayList<T>) getObjects(context, criterias, true);
    }

    /**
     * Returns objects within the given context which match the criterias in the
     * given QueryCriteria object<br/>
     *
     * @param <T>
     * @param criterias If NULL returns ALL
     * @param template
     * @return
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> ArrayList< T> getObjects(T template, QueryCriteria criterias) throws NodataFoundException {
        return getObjects(template.getContext(), criterias, true);
    }

    /**
     * Returns objects within the given context which match the criterias in the
     * given QueryCriteria object
     *
     * @param <T>
     * @param criterias If NULL returns ALL
     * @param template
     * @return
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> ArrayList< T> getObjects(T template, QueryCriteria2 criterias) throws NodataFoundException {
        return getObjects(template.getContext(), criterias, true);
    }

    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> List< T> getReferencedObjects(DatabaseObject dataOwner, Context inReference, T targetType) throws NodataFoundException {
        return getReferencedObjects(dataOwner, inReference, targetType, false);
    }

    /**
     * Return objects which are referenced in the given Context@table As list of
     * getObject(inReference, (SELECT ids FROM Context@table WHERE dataOwnerIDS
     * = dataowner.ids))
     *
     * @param <T>
     * @param dataOwner
     * @param inReference
     * @param targetType The type you like to get back, most likely
     * {@link DatabaseObject.getObject(Context)}
     * @param includeInvisible
     * @return
     * @throws mpv5.db.common.NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> List< T> getReferencedObjects(DatabaseObject dataOwner, Context inReference, T targetType, boolean includeInvisible) throws NodataFoundException {

        String query = "select " + inReference.getDbIdentity() + ".ids from " + inReference.getDbIdentity();
        query += " " + inReference.getConditions(includeInvisible);
        query += " AND " + dataOwner.getDbIdentity() + "ids = " + dataOwner.__getIDS();

        Object[][] allIds = QueryHandler.instanceOf().clone(inReference).freeSelectQuery(query, MPSecurityManager.VIEW, null).getData();
        if (allIds.length == 0) {
            throw new NodataFoundException(inReference);
        }
        List<Integer> idlist = new ArrayList<Integer>();
        for (int i = 0; i < allIds.length; i++) {
            idlist.add(Integer.valueOf(String.valueOf(allIds[i][0])));
        }
        return getObjects(inReference, idlist);
    }

//    /**
//     *
//     * @param <T>
//     * @param dataOwner
//     * @param inReference
//     * @param targetType
//     * @return
//     * @throws NodataFoundException
//     */
//    public static <T extends DatabaseObject> List<T> getReferencedObjects(DatabaseObject dataOwner, Context inReference, T targetType) throws NodataFoundException {
//        List<DatabaseObject> vals = getReferencedObjects(dataOwner, inReference);
//        List<T> cvals = new ArrayList<T>();
//        for (int i = 0; i < vals.size(); i++) {
//            cvals.add( (T) vals.get(i));
//        }
//        return cvals;
//    }
    /**
     * Return objects which are referenced in the given Context@table As list of
     * getObject(inReference, (SELECT ids FROM Context@table WHERE dataOwnerIDS
     * = dataowner.ids))
     *
     * @param <T>
     * @param dataOwner
     * @param inReference
     * @return
     * @throws mpv5.db.common.NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> List< T> getReferencedObjects(T dataOwner, Context inReference) throws NodataFoundException {

        Object[][] allIds = QueryHandler.instanceOf().clone(inReference).select("ids", new String[]{dataOwner.getDbIdentity() + "ids", dataOwner.__getIDS().toString(), ""});
        LinkedList<T> list = new LinkedList<>();

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
     * Return objects which are referenced in the given Context@table <br/>As
     * list of getObject(inReference, (SELECT ids FROM Context@table WHERE
     * dataOwnerIDS = dataowner.ids))
     *
     * @param <T>
     * @param dataOwner
     * @param inReference
     * @param order
     * @param limit
     * @return
     * @throws mpv5.db.common.NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends DatabaseObject> List< T> getReferencedObjects(T dataOwner, Context inReference, String order, Integer limit) throws NodataFoundException {

        Object[][] allIds = QueryHandler.instanceOf().clone(inReference).select("ids", new String[]{dataOwner.getDbIdentity() + "ids", dataOwner.__getIDS().toString(), ""}, order, limit);
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
     * Returns objects which match the given Context and are created within the
     * given timeframe
     *
     * @param <T>
     * @param context
     * @param timeframe
     * @return
     * @throws NodataFoundException
     * @deprecated Not using caches, this may well get slow
     */
    @SuppressWarnings("unchecked")
    @Deprecated()
    public static <T extends DatabaseObject> ArrayList< T> getObjects(Context context, vTimeframe timeframe) throws NodataFoundException {

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
     *
     * @param id
     * @return
     * @throws NodataFoundException
     */
    public boolean fetchDataOf(int id) throws NodataFoundException {
        explode(QueryHandler.instanceOf().clone(getContext()).select(id), this, true, true);
        return true;
    }

    /**
     * Fills this do with the data of the given dataset id
     *
     * @param id
     * @param includeInvisible
     * @return
     * @throws NodataFoundException
     */
    public boolean fetchDataOf(int id, boolean includeInvisible) throws NodataFoundException {
        explode(QueryHandler.instanceOf().clone(getContext()).select(id, includeInvisible), this, true, true);
        return true;
    }

    /**
     * Fills this do with the data of the given dataset id
     *
     * @param column
     * @param value
     * @return
     * @throws NodataFoundException
     */
    public boolean fetchDataOf(String column, Object value) throws NodataFoundException {
        QueryCriteria c = new QueryCriteria(column, value);
        explode(QueryHandler.instanceOf().clone(getContext()).select(c), this, true, true);
        return true;
    }

    /**
     * Tries to get the id of the last do with the given cname
     *
     * @param cname
     * @return The id, or 0 if none found
     * @throws NodataFoundException
     */
    public Integer getIdOf(String cname) throws NodataFoundException {
        cname = new SaveString(cname, false).toString();
        Object[] data = QueryHandler.instanceOf().clone(getContext()).selectLast("ids", new String[]{"cname", cname, "'"});
        if (data != null && data.length > 0) {
            return Integer.valueOf(String.valueOf(data[0]));
        } else {
            return 0;
        }
    }

    /**
     * Fills this do with the data of the given dataset cname
     *
     * @param cname
     * @return True if data was found
     */
    public boolean fetchDataOf(String cname) {
        Integer id;
        ReturnValue data;
        try {
            id = this.getIdOf(cname);
            data = QueryHandler.instanceOf().clone(getContext()).select(id);
        } catch (NodataFoundException ex) {
            return false;
        }
        if (data.getData() != null && data.getData().length > 0) {
            try {
                explode(data, this, true, true);
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fills the return value's data (rows) into an array of dos if
     * singleExplode is false, if not fills target with the first row
     */
    public static synchronized DatabaseObject[] explode(ReturnValue select, DatabaseObject target, boolean singleExplode, boolean lock) throws NodataFoundException {
        String[] cols = select.getColumnnames();
        DatabaseObject[] dos = null;
        if (!singleExplode) {
            dos = new DatabaseObject[select.getData().length];
        } else {
            dos = new DatabaseObject[1];
        }

        Log.Debug(DatabaseObject.class, "Preparing to explode rows: " + dos.length);
        for (int i = 0; i < dos.length; i++) {

            DatabaseObject dbo = null;
            if (!singleExplode) {
                dbo = DatabaseObject.getObject(target.getContext());
            } else {
                dbo = target;
            }

            Map<String, Method> vars = dbo.setVars();
            dos[i] = dbo;
            if (select.hasData()) {
                for (int j = 0; j < select.getData()[i].length; j++) {
                    String name = cols[j].toLowerCase();
                    if (vars.containsKey(name)) {
                        Method m = vars.get(name);
                        String valx = "";
                        if (select.getData()[i][j] != null) {
                            valx = select.getData()[i][j].getClass().getName();
                        } else {
                            valx = "NULL VALUE!";
                        }
                        if (select.getData()[i][j] != null) {
                            invoke(m, select.getData()[i][j], dbo, valx);
                        }
                    } else if (name.endsWith("ids")
                            && (vars.containsKey(name + "ids") || vars.containsKey(name + "sids"))) {
                        Method m = vars.get(name + "ids");
                        if (m == null) {
                            m = vars.get(name + "sids");
                        }
                        if (Log.isDebugging()) {
                            Log.Debug(DatabaseObject.class, "Explode: " + m.toGenericString() + " with " + select.getData()[i][j] + "[DBO]");
                        }
                        if (select.getData()[i][j] != null) {
                            invoke(m, select.getData()[i][j], dbo, "DatabaseObject");
                        }
                    } else if (name.endsWith("ids")
                            && (vars.containsKey(name.substring(0, name.length() - 3)) || vars.containsKey(name.substring(0, name.length() - 4)))) {
                        Method m = vars.get(name.substring(0, name.length() - 3));
                        if (m == null) {
                            m = vars.get(name.substring(0, name.length() - 4));
                        }
                        if (Log.isDebugging()) {
                            Log.Debug(DatabaseObject.class, "Explode: " + m.toGenericString() + " with " + select.getData()[i][j] + "[DBO]");
                        }
                        if (select.getData()[i][j] != null) {
                            invoke(m, select.getData()[i][j], dbo, "DatabaseObject");
                        }
                    } 
                }
            }

            if (lock) {
                if (AUTO_LOCK && Context.getLockableContexts().contains(dbo.getContext())) {
                    Log.Debug(DatabaseObject.class, "Preparing to lock: " + dbo);
                    boolean lck = dbo.lock();
                    Log.Debug(DatabaseObject.class, "Locking was: " + lck);
                }
            }
            if (Context.getModifiableContexts().contains(target.getContext())) {
                List<DatabaseObjectModifier> mods = new ArrayList<DatabaseObjectModifier>(YabsPluginLoader.registeredModifiers);
                //mods.add(inlineObjectModifier);
                for (int ik = 0; ik < mods.size(); ik++) {
                    DatabaseObjectModifier databaseObjectModifier = mods.get(ik);
                    try {
                        dbo = databaseObjectModifier.modifyOnExplode(dbo);
                    } catch (Exception e) {
                        Log.Debug(DatabaseObject.class, "Error while on-explode modifying Object " + dbo + " within Modifier " + databaseObjectModifier);
                        Log.Debug(e);
                    }
                }
            }
            dbo.IDENTITY = new Entity<Context, Integer>(target.getContext(), dbo.__getIDS());
            if (dbo.__getGroupsids() == 0) {
                dbo.setGroupsids(1);//default do all group
            }
            if (Log.LOGLEVEL_DEBUG == Log.getLoglevel()) {
                Log.Debug(dbo.getClass(), "Exploded " + dbo.IDENTITY);
            }
        }

        return dos;
    }

    /**
     * Creates a Set of Entries on runtime which reflect the actual getters of
     * the databaseObject child
     *
     * @return A set Set: String (cname), Class (java.lang.String)
     */
    public Set<Map.Entry<String, Class<?>>> getKeySet() {
        Set<Map.Entry<String, Class<?>>> s = new HashSet<Map.Entry<String, Class<?>>>();
        Map<String, Method> vars = getVars();
        for (final String key : vars.keySet()) {
            final Method method = vars.get(key);
            s.add(new Map.Entry<String, Class<?>>() {

                public String getKey() {
                    return key;
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
     * Tries to reflect the {@link SimpleDatabaseObject} into a new do. The
     * simple objects name must match the DatabaseObjects's
     * {@link Context#getDbIdentity()}
     *
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
     * The simple objects name must match the DatabaseObjects's
     * {@link Context#getDbIdentity()}
     *
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
    @Persistable(false)
    public int __getGroupsids() {
        return groupsids;
    }

    /**
     * @param groupsids the groupsids to set
     */
    @Persistable(false)
    public void setGroupsids(int groupsids) {
        this.groupsids = groupsids;
    }

    @Override
    public String toString() {
        return getCname() + (readOnly ? " [Read-only]" : "");//TODO: l10n
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
        hash = 83 * hash + (this.getContext() != null ? this.getContext().hashCode() : 0);
        hash = 83 * hash + (this.ids != null ? this.ids.hashCode() : 0);
        return hash;
    }

    /**
     * The equals Method is implemented here to check if two DatabaseObjects
     * represent the same row of their table in the database.<br/> You should
     * not override this, and if, check for the Object to be an instanceOf your
     * own class.
     *
     * @param databaseObject
     */
    @Override
    public boolean equals(Object databaseObject) {
        if (databaseObject == null || !(databaseObject instanceof DatabaseObject)) {
            return false;
        } else {
            return (getContext().equals(((DatabaseObject) databaseObject).getContext()) && ids == ((DatabaseObject) databaseObject).__getIDS());
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
     *
     * @param cont
     * @param ids
     * @return
     */
    public static boolean exists(Context cont, Integer ids) {
        try {
            if (QueryHandler.instanceOf().clone(cont).selectCount("ids", ids.toString()) > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Return true if the given data exist in the given Context
     *
     * @param cont
     * @param ids
     * @return
     */
    public static boolean exists(Context cont, QueryCriteria2 c2) {
        try {
            if (QueryHandler.instanceOf().clone(cont).selectCount(null, c2.getQuery()) > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Return true if the given cname exist in the given Context
     *
     * @param cont
     * @param ids
     * @return
     */
    public static boolean exists(Context cont, String cname) {
        try {
            if (QueryHandler.instanceOf().clone(cont).selectCount("cname", "=" + cname) > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
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
     *
     * @param active
     */
    public static void setAutoLockEnabled(boolean active) {
        AUTO_LOCK = active;
    }

    /**
     * Resolves referencing ids in the map to their named values.<br/> Child
     * classes should override this and call
     * super.resolveReferences(HashMap<String, String> map) in the overriding
     * method
     *
     * @param map
     * @return
     */
    public Map<String, Object> resolveReferences(Map<String, Object> map) {
        map.put("dataOwner", this);
        resolveValueProperties(map);

        if (map.containsKey("groupsids")) {
            try {
                try {
                    Group g = (Group) DatabaseObject.getObject(Context.getGroup(), Integer.valueOf(map.get("groupsids").toString()));
                    map.put("group", g.__getCname());
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
                map.put("addedbyfull", User.getFullName(Integer.valueOf(map.get("intaddedby").toString())));
            }
        } catch (NumberFormatException numberFormatException) {
            //already resolved?
        }

        Locale l = Locale.getDefault();
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("item.date.locale")) {
            try {
                l = TypeConversion.stringToLocale(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("item.date.locale"));
            } catch (Exception e) {
            }
            if (l != null) {
            } else {
                Log.Debug(this, "Error while using item.date.locale");
            }
        }
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, l);
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("org.openyabs.exportproperty.dateformat")) {
            String dd = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.exportproperty.dateformat");
            try {
                df = new SimpleDateFormat(dd, l);
            } catch (Exception e) {
                Log.Debug(this, "Error while using default.date.format: " + e);
            }
        }

        map.put("dateadded", df.format(__getDateadded()));
        if (Context.getModifiableContexts().contains(getContext())) {
            List<DatabaseObjectModifier> mods = new ArrayList<DatabaseObjectModifier>(YabsPluginLoader.registeredModifiers);
            //not implemented here mods.add(inlineObjectModifier);
            for (int ik = 0; ik < mods.size(); ik++) {
                DatabaseObjectModifier databaseObjectModifier = mods.get(ik);
                try {
                    map = databaseObjectModifier.modifyOnResolve(map);
                } catch (Exception e) {
                    Log.Debug(DatabaseObject.class, "Error while modificationg object map if " + this + " within Modifier " + databaseObjectModifier);
                }
            }
        }

        return map;
    }

    /**
     * Tries to reflect the given key and value to a setter of this database
     * object at runtime. Does convert nulls to the String "null".
     *
     * @param key
     * @param value
     * @throws Exception
     */
//    
    public void parse(Map<String, Object> values) throws Exception {
        Object[][] data = ArrayUtilities.mapToArray(values);
        for (int row = 0; row < data.length; row++) {
            parse(String.valueOf(data[row][0]), data[row][1]);
        }
    }

    /**
     * Tries to reflect the hash table into this do. The hashtable's keys must
     * match the methods retrieved by do.setVars()
     *
     * @param values
     * @throws Exception
     */
    public void parse(String key, Object value) throws Exception {
        Map<String, Method> vars = setVars();
        String name = key.toLowerCase();
        Method m = vars.get(name);
        if (m == null) {
            Log.Debug(this, new Exception("Unknown field in " + this + ": " + name));
        } else {
// Log.Debug(this, name + " ?? : " + " = " + data[row][1]);
//                Log.Debug(this, name + " ?? : " + vars.get(k).getName() + " = " + data[row][1]);
//                    Log.Debug(this, name + " ?? : " + vars.get(k).getName() + " = " + data[row][1]);
//         Log.Debug(this, "Set: " + key + " to: " + value + " [" + value.getClass().getName() + "]");
            invoke(m, value, this, value);
        }
    }

    /**
     * Fill the do with (senseless) sample data
     */
//    @Deprecated
    public void fillSampleData() {
        List<Method> vars = new ArrayList<Method>(setVars().values());
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
                    vars.get(k).invoke(this, new Object[]{RandomText.getString()});
                }
            } catch (IllegalAccessException illegalAccessException) {
            } catch (IllegalArgumentException illegalArgumentException) {
            } catch (InvocationTargetException invocationTargetException) {
            }
        }
    }

    /**
     * Generate an array out of the getValues2()
     *
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
     *
     * @param key
     * @return
     * @throws Exception Thrown if the given key is invalid for this object
     * during runtime
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
     *
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
     * Creates an Object Array out of all getters of this DO, first column is a
     * DatabaseObject.Entity to identify this DO
     *
     * @param fieldCount Defines how many columns the resulting array shall
     * have. The ordering is somewhat unpredictable, however the array will
     * always start with [IDENTITY, ...]. A fieldcount below 2 is not allowed.
     * @param fields You can specify as many fields you like to force *some*
     * ordering of the resulting array. Specified fields will appear BEFORE
     * unspecified fields, however the ordering of the specified fields is not
     * guaranteed. Nonexisting fields will be ignored.
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
    private GroovyShell groovyShell;

    private GroovyShell getGroovyShell() {
        synchronized (this) {
            if (groovyShell == null) {
                Binding binding = new Binding();
                binding.setVariable("dbo", this);
                MethodClosure cl = new MethodClosure(this, "getObject");
                binding.setVariable("getObject", cl);
                MethodClosure ff = new MethodClosure(this, "getFormFields");
                binding.setVariable("getFormFields", ff);
                MethodClosure ff1 = new MethodClosure(this, "getFormField");
                binding.setVariable("getFormField", ff1);
                groovyShell = new GroovyShell(binding);
            }
            return groovyShell;
        }
    }

    public DatabaseObject getObject(String contextname, String cname) throws NodataFoundException {
        return getObject(Context.getMatchingContext(contextname), cname);
    }

    public DatabaseObject getObject(String contextname, int id) throws NodataFoundException {
        return getObject(Context.getMatchingContext(contextname), id);
    }

    public DatabaseObject getObject(String contextname, String field, Object value) throws NodataFoundException {
        return getObject(Context.getMatchingContext(contextname), field, value);
    }

    public synchronized Map<String, Object> getFormFields() {
        return getFormFields(null);
    }

    public Map<String, Object> getFormFields(String key) {
        if (!cachedFormFieldsByKey.containsKey(key)) {
            cachedFormFieldsByKey.put(key, new FormFieldsHandler(this).getFormattedFormFields(key));
        }
        //Log.Debug(this, ".........." + key + ":>\n\n" + cachedFormFieldsByKey);
        return cachedFormFieldsByKey.get(key);
    }

    public Object getFormField(String name) {
        return getFormFields().get(name);
    }

    /**
     * Evaluates a script in this and returns its result
     *
     * @param script
     * @return
     */
    public String evaluate(String script) {
        try {
            return evaluateAll(script);
        } catch (Exception compilationFailedException) {
            Log.Debug(compilationFailedException);
        }
        return script;
    }

    private String doEvaluate(String script) {
        try {
            return String.valueOf(getGroovyShell().evaluate(script));
        } catch (CompilationFailedException compilationFailedException) {
            Log.Debug(compilationFailedException);
        }
        return script;
    }

    public String evaluateAll(String t) {
        //String sm = GlobalSettings.getProperty("org.openyabs.config.scriptsymbol", "#");
        /*if (!t.contains(sm)) {
        t = sm + t + sm;
        }*/
        return evaluateAll(t, false);
    }

    public String evaluateAll(String text, boolean showError) {
        String sm = GlobalSettings.getProperty("org.openyabs.config.scriptsymbol", "#");
        Pattern SCRIPTPATTERN = Pattern.compile("\\" + sm + "(.*?)\\" + sm + "", Pattern.DOTALL);
        Matcher scriptmatcher = SCRIPTPATTERN.matcher(text);
        Log.Debug(this, "script text:" + text);
        while (scriptmatcher.find()) {
            try {
                String script = scriptmatcher.group(1);
                String orig = scriptmatcher.group(0);
                Log.Debug(this, "script:" + script);
                if (script.startsWith(sm) && script.endsWith(sm)) {
                    script = script.substring(1, script.length() - 1);
                }
                text = text.replace(orig, doEvaluate(script));
            } catch (Exception e) {
                Log.Debug(this, e);
                if (showError) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }

        return text;
    }

    /**
     *
     * @param map
     */
    public synchronized void resolveValueProperties(final Map<String, Object> map) {
        if (map.containsKey("vpresolved@" + this)) {
            return;//just resolve once
        }
        map.put("vpresolved@" + this, Boolean.TRUE);
        List<ValueProperty> props = new ArrayList<ValueProperty>(ValueProperty.getProperties(this));
        try {
            props.addAll(ValueProperty.getProperties(getContext(), getGroup()));
        } catch (Exception ex) {
            Log.Debug(this, ex.getMessage());
        }

        String sm = GlobalSettings.getProperty("org.openyabs.config.scriptsymbol", "#");
        for (ValueProperty p : props) {
            if (!p.getKey().startsWith("modify") && !p.getKey().startsWith("execute")) {
                String strVal = String.valueOf(p.getValue());
                if (p.getValue() instanceof LazyInvocable) {
                    try {
                        LazyInvocable lazy = (LazyInvocable) p.getValue();
                        lazy.doIt(this);
                        map.put("property." + p.getKey(), String.valueOf(lazy));
                    } catch (Exception e) {
                        Log.Debug(this, e.getMessage());
                    }
                } else if (strVal.startsWith(sm) && strVal.endsWith(sm)) {
                    try {
                        Object value = evaluateAll(strVal);
                        map.put("property." + p.getKey(), String.valueOf(value));
                    } catch (Exception e) {
                        Log.Debug(this, e.getMessage());
                        Notificator.raiseNotification(Messages.SCRIPT_ERROR + " " + p.getKey(), false);
                    }
                } else {
                    map.put("property." + p.getKey(), strVal);
                }
            } else {
                Log.Debug(this, "Skipping DatabaseObjectModifier key: " + p.getKey());
            }
        }
    }

    /**
     * @return the Group
     */
    @Persistable(true)
    @Relation(true)
    public Group getGroup() {
        try {
            return (Group) getObject(Context.getGroup(), __getGroupsids());
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        return Group.getDefault();
    }

    /**
     * @param g the Group to set
     */
    @Persistable(true)
    public void setGroup(Group g) {
        setGroupsids(g.__getIDS());
    }

    public void toPdf(boolean showDialog) {
        export(true, showDialog);
    }

    public void toOdt(boolean showDialog) {
        export(false, showDialog);
    }

    private void export(boolean convertToPdf, boolean showDialog) {
        if (this instanceof Templateable) {
            Templateable me = (Templateable) this;
            if (TemplateHandler.isLoaded(me)) {
                File d = User.getSaveDir(this);
                d.mkdirs();
                if(!d.exists()){
                    Notificator.raiseNotification(Messages.FILE_OPEN_FAILED + " " + d);
                    return;
                }
                File xf = new File(d, me.getFormatHandler().toUserString() + (convertToPdf ? ".pdf" : ".odt"));
                Log.Debug(this, "Export to: " + xf);
                Waiter x;
                if (showDialog) {
                    x = new DialogForFile(DialogForFile.FILES_ONLY, xf);
                } else {
                    x = Export.wait(xf);
                }
                try {
                    if (convertToPdf) {
                        new Job(Export.createFile(null, TemplateHandler.loadTemplate(me), this), x).executeSync();
                    } else {
                        new Job(Export.sourceFile(null, TemplateHandler.loadTemplate(me), this), x).executeSync();
                    }
                } catch (Exception exception) {
                    Log.Debug(exception);
                }

                final String fmessage = Messages.PDF + __getCname() + " " + xf;
                final String fdbid = getDbIdentity();
                final int fids = __getIDS();
                final int fgids = __getGroupsids();
                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(fmessage, mpv5.db.objects.User.getCurrentUser().__getCname(), fdbid, fids, fgids);
                    }
                };
                new Thread(runnable).start();
            } else {
                YabsViewProxy.instance().addMessage(Messages.NO_TEMPLATE_LOADED + ": " + getClass().getSimpleName() + " (" + mpv5.db.objects.User.getCurrentUser() + ")", Color.YELLOW);
            }
        }
    }

    /**
     * Returns the first found object with Context r within this object's
     * getters (all)
     *
     * @param r
     * @return R
     */
    public DatabaseObject getRelationObject(Context r) {
        for (int i = 0; i < getValues5().size(); i++) {
            Object object = getValues5().get(i)[1];
            if (object instanceof DatabaseObject && ((DatabaseObject) object).getContext().equals(r)) {
                return (DatabaseObject) object;
            }
        }
        return null;
    }

    static class InlineObjectModifier implements DatabaseObjectModifier {

        public DatabaseObject modifyOnExplode(DatabaseObject object) {
            return object;
        }

        public DatabaseObject executeAfterSave(DatabaseObject object) {
            return modify(object, "executeAfterSave");
        }

        public DatabaseObject modifyOnSave(DatabaseObject object) throws ChangeNotApprovedException {
            return modify(object, "modifyOnSave");
        }

        public DatabaseObject executeAfterCreate(DatabaseObject object) {
            return modify(object, "executeAfterCreate");
        }

        public Map<String, Object> modifyOnResolve(Map<String, Object> map) {
            return map;
        }

        public DatabaseObject modifyOnDelete(DatabaseObject object) throws ChangeNotApprovedException {
            return modify(object, "modifyOnDelete");
        }

        private DatabaseObject modify(DatabaseObject object, final String key) {
            try {
                if (ValueProperty.hasProperty(object.getContext(), key)) {
                    ValueProperty script = ValueProperty.getProperty(object.getContext(), key);
                    Object s = script.getValue();
                    if (s != null) {
                        object.evaluateAll(String.valueOf(s));
                    }
                }
                if (ValueProperty.hasProperty(object, key)) {
                    ValueProperty script = ValueProperty.getProperty(object, key);
                    Object s = script.getValue();
                    if (s != null) {
                        object.evaluateAll(String.valueOf(s));
                    }
                }
            } catch (Throwable ex) {
                Notificator.raiseNotification(ex, true, true, object);
            }

            return object;
        }
    }
}
