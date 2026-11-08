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
package mpv5.i18n;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import mpv5.Main;
import mpv5.db.common.*;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.Headers;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.Popup;

import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.date.DateConverter;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileReaderWriter;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPComboboxModel;
import mpv5.utils.reflection.ClasspathTools;
import mpv5.utils.text.RandomText;
import mpv5.utils.xml.XMLReader;
import org.jdom.Document;

/**
 *
 * Administrator
 */
public class LanguageManager {

    static {
    }
    protected final static String defLanguageBundleName = "languages/Panels";
    protected static ResourceBundle defLanguageBundle = ResourceBundle.getBundle(defLanguageBundleName);
    private static String[][] defLanguage = new String[][]{{"buildin_en", "English"}};
    private static Hashtable<String, ResourceBundle> cachedLanguages = new Hashtable<String, ResourceBundle>();
    private static boolean cached = false;

    /**
     * Flushes the Language Cache so that it contains no Languages.
     */
    public static void flushLanguageCache() {
        cachedLanguages.clear();
    }

    /**
     * Get a country name by id
     *
     * @param id
     * @return
     */
    public static String getCountryName(int id) {
        for (int i = 0; i < COUNTRIES.size(); i++) {
            MPComboBoxModelItem mPComboBoxModelItem = COUNTRIES.get(i);
            if (mPComboBoxModelItem.getId().equals(String.valueOf(id))) {
                return mPComboBoxModelItem.getValue();
            }
        }
        return "---";
    }

    /**
     * Import & replace the country list
     *
     * @param file
     */
    public static void importCountries(File file) {
        XMLReader r = new XMLReader();
        try {
            Document doc = r.newDoc(file, false);
            //            if (MPSecurityManager.checkAdminAccess() && !mpv5.db.objects.User.getCurrentUser().isDefault()) {

            if (doc != null) {
                ArrayList<DatabaseObject> users = User.getObjects(Context.getUser(), true);
                try {

                    for (int i = 0; i < users.size(); i++) {
                        User u = (User) users.get(i);
                        QueryHandler.instanceOf().clone(Context.getCountries()).delete(new String[][]{{"groupsids", String.valueOf(u.__getGroupsids()), ""}});
                    }
                } catch (Exception ex) {
                    Log.Debug(ex);
                }

                String[][] countries1 = r.toArray(r.getSubRootElement("countries"));

                for (int k = 0; k < users.size(); k++) {
                    DatabaseObject databaseObject = users.get(k);
                    for (int i = 0; i < countries1.length; i++) {
                        try {
                            String[] country = countries1[i];
                            QueryData t = new QueryData();
                            t.add("cname", country[1]);
                            t.add("iso", Integer.valueOf(country[2]));
                            t.add("groupsids", databaseObject.__getGroupsids());
                            QueryHandler.instanceOf().clone(Context.getCountries()).insert(t, Messages.DONE.toString());
                        } catch (Exception exception) {
                            Log.Debug(LanguageManager.class, exception.getMessage());
                        }
                    }
                }

//                mpv5.YabsViewProxy.instance().addMessage(langname + Messages.ROW_UPDATED);
            }
//            } else {
//                Popup.notice(Messages.ADMIN_ACCESS + "\n" + Messages.DEFAULT_USER);
//            }
        } catch (Exception x) {
            Log.Debug(LanguageManager.class, x);
        }
    }

    private static boolean isCachedLanguage(String langid) {
        return cachedLanguages.containsKey(langid);
    }

    private static void cachelanguage(String langid, ResourceBundle bundle) {
        Log.Debug(LanguageManager.class, "Caching language with id " + langid);
        if (cachedLanguages.containsKey(langid)) {
            cachedLanguages.remove(langid);
        }
        cachedLanguages.put(langid, bundle);
        cached = true;
    }

    private static ResourceBundle getCachedLanguage(String langid) {
        return cachedLanguages.get(langid);
    }

    /**
     * Checks the existance of a language in DB
     *
     * @param languagename
     * @return
     */
    public static boolean checkLanguage(String languagename) {
        Object[][] data = QueryHandler.instanceOf().clone(Context.getLanguage()).select(Context.SEARCH_NAME, new String[]{Context.SEARCH_NAME, languagename, "'"});
        if (data != null && data.length > 0) {
            return true;
        } else {
            return false;
        }
    }
    private static volatile boolean failed = false;
    private static final List<String> cacheRunWatchdog = Collections.synchronizedList(new ArrayList<String>());

    /**
     *
     * @param langid
     * @return
     */
    public static ResourceBundle getBundle(String langid) {
        synchronized (LanguageManager.class) {
            if (!langid.contentEquals("buildin_en")) {
//                Log.Debug(LanguageManager.class, "Checking language: " + langid);
                if (!failed) {
//                    Log.Debug(LanguageManager.class, "Language has not previously failed: " + langid);
                    // if not cached AND not tried to cache it before
                    if (!isCachedLanguage(langid) && !cacheRunWatchdog.contains(langid)) {
                        //cache it
                        Log.Debug(LanguageManager.class, "Language is not cached: " + langid);
                        cacheRunWatchdog.add(langid);

                        File bundlefile = null;
                        Object[] data;
                        URI newfileUri;
                        String tempname = langid;
                        try {
                            data = QueryHandler.instanceOf().clone(Context.getLanguage()).
                                    selectFirst("filename", new String[]{Context.SEARCH_NAME, langid, "'"});
                            if (data != null && data.length > 0) {
                                bundlefile = QueryHandler.instanceOf().clone(Context.getFiles()).retrieveFile(String.valueOf(
                                        data[0]));
                            } else {
                                fail(langid);
                                failed = true;
                                return ResourceBundle.getBundle(defLanguageBundleName);
                            }
                            if (bundlefile != null) {
                                newfileUri = FileDirectoryHandler.copyFile(bundlefile, new File(LocalSettings.getProperty(LocalSettings.CACHE_DIR)), tempname + ".properties", false, true);
                                ClasspathTools.addPath(new File(LocalSettings.getProperty(LocalSettings.CACHE_DIR)));
                                File newbundle = new File(newfileUri);
                                if (hasNeededKeys(newbundle, false) || addNeededKeys(newbundle)) {
                                    Log.Debug(LanguageManager.class, "File has needed keys for language: " + langid);
                                    Log.Debug(LanguageManager.class, "Created language file at: " + newfileUri);
                                    try {
                                        ResourceBundle bundle = ResourceBundleUtf8.getBundle(tempname);
                                        cachelanguage(langid, bundle);
                                        return bundle;
                                    } catch (Exception e) {
                                        fail(langid);
                                        failed = true;
                                        Log.Debug(LanguageManager.class, e);
                                        return defLanguageBundle;
                                    }
                                } else {
                                    fail(langid);
                                    failed = true;
                                    return defLanguageBundle;
                                }
                            } else {
                                fail(langid);
                                failed = true;
                                return defLanguageBundle;
                            }
                        } catch (NodataFoundException nd) {
                            //language has been deleted?
                            Log.Debug(LanguageManager.class, nd.getMessage());
                            return ResourceBundle.getBundle(defLanguageBundleName);
                        } catch (Exception e) {
                            failed = true;
                            fail(langid);
                            Log.Debug(LanguageManager.class, e);
                            return defLanguageBundle;
                        }

                    } else if (isCachedLanguage(langid)) {
                        return getCachedLanguage(langid);
                    } else if (cacheRunWatchdog.contains(langid)) {
                        failed = true;
                        Log.Debug(LanguageManager.class, "Already tried to cache language with id: " + langid);
                        fail(langid);
                        return defLanguageBundle;
                    } else {
                        return defLanguageBundle;
                    }
                } else {
                    return defLanguageBundle;
                }
            }
        }

        return defLanguageBundle;
    }

    /**
     * The users selected language bundle, or default if not defined by the user
     *
     * @return
     */
    public static ResourceBundle getBundle() {
        if (Main.INSTANTIATED) {
            return getBundle(mpv5.db.objects.User.getCurrentUser().__getLanguage());
        } else {
            return ResourceBundle.getBundle(defLanguageBundleName);
        }
    }

    /**
     *
     * @param langid
     * @return
     */
    public static Object[][] getEditorModel(String langid) {
        ResourceBundle bundle = getBundle(langid);
        Enumeration<String> set = bundle.getKeys();
        ArrayList<String[]> list = new ArrayList<String[]>();

        while (set.hasMoreElements()) {
            String string = set.nextElement();
            list.add(new String[]{string, bundle.getString(string), null});
        }

        return ArrayUtilities.listToStringArrayArray(list);
    }

    /**
     *
     * @return A ComboBoxModel reflecting the available Languages
     */
    public static ComboBoxModel getLanguagesAsComboBoxModel() {
        Object[][] data = QueryHandler.instanceOf().clone(Context.getLanguage()).select("cname, longname", (String[]) null);
        MPComboBoxModelItem[] t = null;
        Object[][] ldata;
        ldata = ArrayUtilities.merge(defLanguage, data);
        t = MPComboBoxModelItem.toItems(ldata);
        return new DefaultComboBoxModel(t);
    }
    public static List<MPComboBoxModelItem> COUNTRIES;

    /**
     *
     * @return A ComboBoxModel reflecting the available Countries
     */
    public static synchronized mpv5.utils.models.MPComboboxModel getCountriesAsComboBoxModel() {
        if (COUNTRIES == null) {
            COUNTRIES = new Vector<MPComboBoxModelItem>();
            try {
                QueryCriteria2 q = new QueryCriteria2();
                q.and(new QueryParameter(Context.getCountries(), "groupsids", User.getCurrentUser().__getGroupsids(), QueryParameter.EQUALS));
                Object[][] data = QueryHandler.instanceOf().clone(Context.getCountries()).getColumns(new String[]{"iso", "cname"}, 0, q);
                for (int i = 0; i < data.length; i++) {
                    Object[] objects = data[i];
                    COUNTRIES.add(new MPComboBoxModelItem(objects[0].toString(), objects[1].toString()));
                }
                Log.Debug(LanguageManager.class, "Cached countries: " + COUNTRIES.size());
                return MPComboBoxModelItem.toModel(COUNTRIES);
            } catch (NodataFoundException ex) {
                Log.Debug(LanguageManager.class, ex.getMessage());
                return new MPComboboxModel(new MPComboBoxModelItem[]{});
            }
        } else {
            return MPComboBoxModelItem.toModel(COUNTRIES);
        }
    }

    /**
     *
     * @return A comboBoxModel reflecting the available locales
     */
    public static DefaultComboBoxModel getLocalesAsComboBoxModel() {
        Locale[] o = Locale.getAvailableLocales();
        MPComboBoxModelItem[] items = new MPComboBoxModelItem[o.length];
        for (int i = 0; i < items.length; i++) {
            String language = o[i].getLanguage();
            String country = o[i].getCountry();
            String locale_name = o[i].getDisplayName();
//            Log.Debug(LanguageManager.class, locale_name);
            items[i] = new MPComboBoxModelItem(language + "_" + country,
                    locale_name + "  [" + language + "_" + country + "]");
//            items[i] = new MPComboBoxModelItem(o[i].toString(), o[i].getDisplayName());
        }

        return new DefaultComboBoxModel(ArrayUtilities.sort(items));
    }

    public static String importLanguage(String langname, File file) throws Exception {
        return importLanguage(langname, file, false);
    }

    /**
     * Imports a language file to DB
     *
     * @param langname
     * @param file If it is a .zip, will get extracted and then processed
     * @param ignoreErrors
     * @return
     * @throws java.lang.Exception
     * @throws UnsupportedOperationException
     */
    /**
     * Imports a language without any message box
     *
     * @param langname
     * @param file
     * @return the id of the language
     * @throws Exception
     */
    public static String importLanguageQuietly(String langname, File file) throws Exception {
        boolean before = quietImport;
        quietImport = true;
        try {
            return importLanguage(langname, file);
        } finally {
            quietImport = before;
        }
    }

    public static String importLanguage(String langname, File file, Boolean ignoreErrors) throws Exception {
        try {
            String langid = RandomText.getText();

            file = FileDirectoryHandler.unzipFile(file);
            Log.Debug(LanguageManager.class, "Importing: " + file);

            if (ignoreErrors || hasNeededKeys(file, true)) {
                try {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            mpv5.YabsViewProxy.instance().setWaiting(true);
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                    String dbname = QueryHandler.instanceOf().clone(Context.getFiles()).insertFile(file);
                    try {
                        Thread.sleep(3333);
                    } catch (InterruptedException ex) {
                        Log.Debug(ex);
                    }
                    QueryData t = new QueryData();
                    t.add("cname", langid);
                    t.add("longname", langname);
                    t.add("filename", dbname);
                    t.add("dateadded", DateConverter.getTodayDBDate());
                    Log.Debug(LanguageManager.class, "Adding language: " + langname);
                    int id = QueryHandler.instanceOf().clone(Context.getLanguage()).insert(t, "Imported language: " + langname);
                    if (id > 0) {
                        mpv5.YabsViewProxy.instance().addMessage(langname + Messages.INSERTED.toString());
                        if (!quietImport) {
                            Popup.notice(langname + Messages.INSERTED.toString());
                        }
                        cachelanguage(langid, getBundle(langid));
                    } else {
                        mpv5.YabsViewProxy.instance().addMessage(Messages.ERROR_OCCURED.toString());
                        Popup.notice(Messages.ERROR_OCCURED.toString());
                    }
                } catch (FileNotFoundException ex) {
                    Log.Debug(LanguageManager.class, ex);
                } catch (Exception x) {
                    //insert was not possible
                    throw new UnsupportedOperationException("Language could not be inserted, file already exists!?");
                }
                return langid;
            } else {
                return null;
            }
        } catch (Exception exception) {
            Popup.error(exception);
            throw exception;
        } finally {
            mpv5.YabsViewProxy.instance().setWaiting(false);
        }
    }

    /**
     * Exports a language file to DB
     *
     * @param langid
     * @param file If it is a .zip, will get extracted and then processed
     * @return
     * @throws java.lang.Exception
     * @throws UnsupportedOperationException
     */
    public static boolean exportLanguage(String langid, File file) throws Exception {
        try {
            Log.Debug(LanguageManager.class, "Exporting: " + langid + " to " + file);
            Object[] data;
            File bundlefile = null;
            URI newfileUri;
            ResourceBundle bundle = null;
            String tempname = langid;
            try {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        mpv5.YabsViewProxy.instance().setWaiting(true);
                    }
                };
                SwingUtilities.invokeLater(runnable);
                data = QueryHandler.instanceOf().clone(Context.getLanguage()).
                        selectFirst("filename", new String[]{Context.SEARCH_NAME, langid, "'"});
                if (data != null && data.length > 0) {
                    bundlefile = QueryHandler.instanceOf().clone(Context.getFiles()).retrieveFile(String.valueOf(
                            data[0]), file);
                }

                if (bundlefile != null) {
                    DialogForFile df = new DialogForFile(JFileChooser.FILES_AND_DIRECTORIES);
                    file = df.saveFile(bundlefile);
                }

                List<String> failures = new ArrayList<String>();
                Enumeration<String> keys = ResourceBundle.getBundle(defLanguageBundleName).getKeys();
                File impFile = file;
                FileReaderWriter frw = new FileReaderWriter(impFile);
                String[] lines = frw.readLines();

                while (keys.hasMoreElements()) {
                    String string = keys.nextElement();
                    boolean found = false;
                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i];
                        if (line.startsWith(string)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        failed = true;
                        Log.Debug(LanguageManager.class, "Key '" + string + "' not found in file " + file);
                        try {
                            failures.add(string + "=" + ResourceBundle.getBundle(defLanguageBundleName).getString(string));
                        } catch (Exception e) {
                            failures.add(string + "=???");
                        }
                    }
                }

                if (!failures.isEmpty()) {
                    Popup.notice(failures, Messages.ADD_MISSING_KEYS.toString() + file + ":\n");
                }

            } catch (Exception x) {
                //insert was not possible
                throw new UnsupportedOperationException(Messages.LANG_NOT_EXPORTED.toString() + x.getLocalizedMessage());
            }
            return true;
        } catch (Exception exception) {
            Popup.error(exception);
            throw exception;
        } finally {
            mpv5.YabsViewProxy.instance().setWaiting(false);
        }
    }

    /**
     * Deletes a language from db
     *
     * @param langid
     * @throws mpv5.db.common.NodataFoundException
     */
    public static void removeLanguage(String langid) throws NodataFoundException {
        Object[] data;
        if (!langid.contentEquals("buildin_en")) {
            data = QueryHandler.instanceOf().clone(Context.getLanguage()).
                    selectFirst("filename", new String[]{Context.SEARCH_NAME, langid, "'"});
            if (data != null && data.length > 0) {
                try {
                    QueryHandler.instanceOf().clone(Context.getFiles()).removeFile(String.valueOf(data[0]));
                } catch (Exception ex) {
                    Log.Debug(ex);
                }
            }
        } else {
            Popup.notice(Messages.NOT_POSSIBLE);
        }
    }

    private static boolean hasNeededKeys(File file, boolean popupOnError) {
        synchronized (new LanguageManager()) {
            try {
                List<String> failures = new ArrayList<String>();
                Enumeration<String> keys = ResourceBundle.getBundle(defLanguageBundleName).getKeys();
                File impFile = file;
                FileReaderWriter frw = new FileReaderWriter(impFile);
                String[] lines = frw.readLines();

                while (keys.hasMoreElements()) {
                    String string = keys.nextElement();
                    boolean found = false;
                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i];
                        if (line.startsWith(string)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        //only a check: a file without the keys of a newer version (e.g. the
                        //cached copy after an update) is completed from the database copy;
                        //it must not disable all languages ("failed") for the session
                        Log.Debug(LanguageManager.class, "Key '" + string + "' not found in file " + file);
                        if (!popupOnError) {
                            return false;
                        } else {
                            try {
                                failures.add(string + "=" + ResourceBundle.getBundle(defLanguageBundleName).getString(string));
                            } catch (Exception e) {
                                failures.add(string + "=???");
                            }
                        }
                    }
                }
                if (popupOnError && !failures.isEmpty()) {
                    Popup.notice(failures, "Import not possible.\nMissing keys in " + file + ":\n");
                }
                return failures.isEmpty();
            } catch (Exception e) {
                Log.Debug(e);
                return false;
            }
        }
    }

    /**
     * Returns the status of the language manager
     *
     * @return true if at least one language is cached
     */
    public static boolean isReady() {
        return cached;
    }

    private static boolean addNeededKeys(File file) {
        Notificator.raiseNotification(Messages.LANGUAGE_FILE, false);
        synchronized (new LanguageManager()) {
            try {
                Enumeration<String> keys = ResourceBundle.getBundle(defLanguageBundleName).getKeys();
                File impFile = file;
                FileReaderWriter frw = new FileReaderWriter(impFile, "UTF8");
                String[] lines = frw.readLinesWCharset();

                while (keys.hasMoreElements()) {
                    String string = keys.nextElement();
                    boolean found = false;
                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i];
                        if (line.startsWith(string)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        Log.Debug(LanguageManager.class, "Key '" + string + "' added to file " + file);
                        frw.write(string + "=" + ResourceBundle.getBundle(defLanguageBundleName).getString(string));
                    }
                }
                failed = false;
                return !failed;
            } catch (Exception e) {
                Log.Debug(e);
                fail(e.getMessage());
                failed = true;
                return false;
            }
        }
    }

    private static void fail(String langid) {
        Log.Debug(LanguageManager.class, "Failed language: " + langid);
        Log.Debug(LanguageManager.class, "Error loading additional languages. Using default now.");
        mpv5.YabsViewProxy.instance().addMessage(Messages.ERROR_OCCURED.toString());
    }

    private static boolean exists(String langid) {
        Object[] data;
        if (!langid.contentEquals("buildin_en")) {
            try {
                data = QueryHandler.instanceOf().clone(Context.getLanguage()).selectFirst("filename", new String[]{Context.SEARCH_NAME, langid, "'"});
                if (data != null && data.length > 0) {
                    return true;
                } else {
                    return false;
                }
            } catch (NodataFoundException ex) {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * Disable all languages other than the build-in one
     */
    public static void disableLanguages() {
        failed = true;
    }

    /**
     * Checks the cache directory for cached languages and loads the last cached
     * one
     */
    public static void preLoadCachedLanguage() {
        File cache = FileDirectoryHandler.getTempDirAsFile();
        ClasspathTools.addPath(cache);
        Log.Debug(LanguageManager.class, "[preload-lang] Preloading languages from: " + cache);
        File[] candidates = cache.listFiles();
        List<File> languages = new ArrayList<File>();
        for (int i = 0; i < candidates.length; i++) {
            File file = candidates[i];
            if (file.getName().endsWith(".properties")) {
                languages.add(file);
            }
        }
        Log.Debug(LanguageManager.class, "[preload-lang] Found potential language files: " + languages.size());
        File lastfile = null;
        for (int i = 0; i < languages.size(); i++) {
            File file = languages.get(i);
            if (lastfile == null) {
                lastfile = file;
            }
            if (file.lastModified() > lastfile.lastModified()) {
                lastfile = file;
            } else {
                //file.deleteOnExit();
            }
        }

        if (lastfile != null && hasNeededKeys(lastfile, false)) {
            Log.Debug(LanguageManager.class, "[preload-lang] File has needed keys: " + lastfile);
            //Add the files parent to classpath to be found
            Log.Debug(LanguageManager.class, "[preload-lang] Using language file at: " + lastfile);
            try {
                ResourceBundle bundle = ResourceBundleUtf8.getBundle(lastfile.getName().substring(0, lastfile.getName().lastIndexOf(".")));
                if (lastfile.canRead()) {
                    //only cached for its user: the built-in English stays the default
                    //(it was replaced here, so English could not be chosen again)
                    cachelanguage(lastfile.getName().substring(0, lastfile.getName().lastIndexOf(".")), bundle);
                }
            } catch (Exception e) {
                Log.Debug(LanguageManager.class, e.getMessage());
            }
        }
    }

    public static ArrayList checkBundle(ResourceBundle bundle, boolean popup) {
        try {
            if (bundle == null) {
                bundle = defLanguageBundle;
            }
            ArrayList<String> missing = new ArrayList<String>();
            Headers[] h = Headers.values();
            for (int j = 0; j < h.length; j++) {
                String[] k = h[j].getKeys();
                for (int u = 0; u < k.length; u++) {
                    if (!bundle.containsKey(k[u])) {
                        missing.add(k[u] + "=" + h[j].getValue(u));
                    }
                }
            }

            Set<String> skip = new HashSet(Arrays.asList(new String[]{Messages.START_MESSAGE.name(), Messages.LANGUAGE_UPDATED.name()}));
            Messages[] m = Messages.values();
            for (int j = 0; j < m.length; j++) {
                if (!bundle.containsKey(m[j].name()) && !skip.contains(m[j].name())) {
                    missing.add(m[j].name() + "=" + m[j].rawMessage());
                }
            }

            if (!missing.isEmpty()) {
                if (popup) {
                    Popup.notice(missing, Messages.MISSING_KEYS);
                } else {
                    Notificator.raiseNotification(Messages.MISSING_KEYS);
                }
            }
            return missing;
        } catch (Exception ex) {
            Log.Debug(LanguageManager.class, ex.getLocalizedMessage());
            return null;
        }
    }

    public static boolean checkAndReplaceLangFile(String langid) {

        try {
            Object[] data = QueryHandler.instanceOf().clone(Context.getLanguage()).
                    selectFirst("longname", new String[]{Context.SEARCH_NAME, mpv5.db.objects.User.getCurrentUser().__getLanguage(), "'"});
            if (data != null && data.length > 0) {
                String previous = String.valueOf(data[0]).toLowerCase();
                Log.Debug(LanguageManager.class, "previous: " + previous);
                if (previous.startsWith(("language-" + langid).toLowerCase())
                        || previous.startsWith((langid + "-").toLowerCase())) {
                    importFromJar(langid.toUpperCase());
                    return true;
                }
            }
        } catch (NodataFoundException nodataFoundException) {
            Logger.getLogger(LanguageManager.class.getName()).log(Level.SEVERE, null, nodataFoundException);
        } catch (Exception ex) {
            Logger.getLogger(LanguageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static void importFromJar(String langid) throws Exception {
        InputStream s = Main.class.getResourceAsStream("/extra/" + langid + "-" + Constants.EXTRA_FILES_VERSION + ".properties");
        File f = FileDirectoryHandler.getTempFile(langid.toLowerCase() + "-" + Constants.VERSION, "properties");
        FileDirectoryHandler.copyFile(s, f);
        Log.Debug(Main.class, "Importing language from: " + f.getCanonicalPath());

        String ref = LanguageManager.importLanguage(f.getName() + " (" + new Date() + ")", f);
        User.getCurrentUser().setLanguage(ref);
        User.getCurrentUser().save(true);
        Log.Debug(LanguageManager.class, "\n\n\n#####################################\nLanguage updated: " + langid + "\n#####################################\n\n");
    }

    public static boolean checkAndReplaceLangFiles() {
        boolean result = false;
        for (String l : Constants.LANGUAGES) {
            if (!l.equals("EN"))//default
            {
                if (checkAndReplaceLangFile(l)) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * @return true if the system or the user's locale is German (de, de_DE,
     * de_AT, de_CH, ...)
     */
    public static boolean isGermanUser(User u) {
        String system = System.getProperty("user.language");
        String locale = u == null ? null : u.__getLocale();
        return (system != null && system.toLowerCase().startsWith("de"))
                || (locale != null && locale.toLowerCase().startsWith("de"));
    }

    /**
     * Imports the German language and switches the user to it, once, if the
     * system or the user's locale is German and the user still has the
     * built-in English. A user who switches back to English keeps it.
     *
     * @param u the user logging in
     * @return true if imported
     */
    private static volatile boolean quietImport;

    public static boolean importGermanIfGerman(User u) {
        try {
            if (System.getProperty("yabs_firststart") != null) {
                //the first-start wizard asks (preselected for German systems)
                return false;
            }
            if (u.getProperties().getProperty("org.openyabs.languageproperty", "autoimportde")) {
                return false;
            }
            String lang = u.__getLanguage();
            boolean builtin = lang == null || lang.trim().isEmpty() || lang.startsWith("buildin");
            if (!builtin) {
                //a language of the user's own (German or another one)
                u.getProperties().changeProperty("org.openyabs.languageproperty", "autoimportde", true);
                u.saveProperties();
                return false;
            }
            if (!isGermanUser(u)) {
                return false;
            }
            //at login: no confirmation dialog
            quietImport = true;
            try {
                importFromJar("DE");
            } finally {
                quietImport = false;
            }
            u.getProperties().changeProperty("org.openyabs.languageproperty", "autoimportde", true);
            u.saveProperties();
            Log.Debug(LanguageManager.class, "German language imported automatically for " + u);
            return true;
        } catch (Exception e) {
            Log.Debug(LanguageManager.class, e);
            return false;
        }
    }

    /**
     * Languages to choose from: built-in English, German (imported if needed)
     * and all other imported languages; the newest import of German only
     *
     * @return {id or "import:DE", name}; the id of German is null if it is not
     * imported yet
     */
    public static List<String[]> choices() {
        List<String[]> l = new ArrayList<String[]>();
        l.add(new String[]{"buildin_en", "English"});
        String german = null;
        List<String[]> others = new ArrayList<String[]>();
        try {
            Object[][] data = QueryHandler.instanceOf().clone(Context.getLanguage()).select("cname, longname", (String[]) null);
            for (Object[] d : data == null ? new Object[0][] : data) {
                String id = String.valueOf(d[0]);
                String name = String.valueOf(d[1]);
                if (name.toLowerCase().startsWith("de-") || name.toLowerCase().startsWith("language-de")) {
                    german = id;//the last one is the newest
                } else {
                    others.add(new String[]{id, name});
                }
            }
        } catch (Exception e) {
            Log.Debug(LanguageManager.class, e);
        }
        l.add(new String[]{german == null ? "import:DE" : german, "Deutsch"});
        l.addAll(others);
        return l;
    }

    /**
     * Switches the current user to the language (imports German first if
     * needed); takes effect at the next start
     *
     * @param id an id of {@link #choices()}
     */
    public static void switchTo(String id) throws Exception {
        User u = User.getCurrentUser();
        if ("import:DE".equals(id)) {
            quietImport = true;
            try {
                importFromJar("DE");
            } finally {
                quietImport = false;
            }
        } else {
            u.setLanguage(id);
            u.save(true);
        }
        //chosen by the user: no automatic switch to German any more
        u.getProperties().changeProperty("org.openyabs.languageproperty", "autoimportde", true);
        u.saveProperties();
    }
}
