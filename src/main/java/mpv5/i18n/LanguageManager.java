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
    public static synchronized void flushLanguageCache() {
        cachedLanguages.clear();
        germanLanguages.clear();
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
                return localizedCountryName(mPComboBoxModelItem.getId(), mPComboBoxModelItem.getValue());
            }
        }
        return "---";
    }

    /**
     * The name of a country in the language of the user (Locale.getDefault(),
     * set at the login): "Deutschland", "Österreich" for German. The bundled
     * country list (countries.xml) has English names; English users see them
     * as stored.
     *
     * @param iso the ISO 3166 numeric code (276) or null
     * @param name the name as stored ("Germany")
     * @return the localized name, or the name as stored if the country is unknown
     */
    public static String localizedCountryName(Object iso, String name) {
        return localizedCountryName(iso, name, Locale.getDefault());
    }

    /**
     * @see #localizedCountryName(Object, String)
     */
    public static String localizedCountryName(Object iso, String name, Locale locale) {
        if (locale == null || locale.getLanguage().isEmpty() || "en".equals(locale.getLanguage())) {
            return name;
        }
        String code = "";
        if (iso != null && String.valueOf(iso).trim().matches("\\d{1,3}")) {
            code = mpv5.utils.export.ZugpferdFile.toCountryCode(String.valueOf(iso).trim());
        }
        if (code.isEmpty() && name != null && !name.trim().isEmpty()) {
            //toCountryCode gives "DE" for an empty value: only for a name
            code = mpv5.utils.export.ZugpferdFile.toCountryCode(name.trim());
        }
        if (code.isEmpty()) {
            return name;
        }
        String d = new Locale("", code).getDisplayCountry(locale);
        return d == null || d.isEmpty() || d.equals(code) ? name : d;
    }

    /**
     * The index of a stored country in a list of {@link #getCountriesAsComboBoxModel()}:
     * by name (also of another language: "Germany" in the German list), by code (276)
     *
     * @param stored the country as stored
     * @param model the countries
     * @return the index, 0 (the first country, as before) if the country is not in the list
     */
    public static int countryIndex(String stored, ComboBoxModel model) {
        int idx = findCountryIndex(stored, model);
        return idx >= 0 ? idx : 0;
    }

    /**
     * Finds the index of {@code stored} in {@code model}, or {@code -1} if not found.
     */
    public static int findCountryIndex(String stored, ComboBoxModel model) {
        if (stored == null || stored.trim().isEmpty() || model == null) {
            return -1;
        }
        String c = stored.trim();
        for (int i = 0; i < model.getSize(); i++) {
            if (c.equalsIgnoreCase(String.valueOf(model.getElementAt(i)).trim())) {
                return i;
            }
        }
        for (int i = 0; i < model.getSize(); i++) {
            Object o = model.getElementAt(i);
            if (o instanceof MPComboBoxModelItem && c.equalsIgnoreCase(((MPComboBoxModelItem) o).getId())) {
                return i;
            }
        }
        for (int i = 0; i < model.getSize(); i++) {
            Object o = model.getElementAt(i);
            if (sameCountry(String.valueOf(o), c)
                    || (o instanceof MPComboBoxModelItem && sameCountry(((MPComboBoxModelItem) o).getId(), c))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Resolves a country string (name in any language, alpha-2 code like "DE",
     * or numeric ISO code like "276") to the numeric ISO ID used in the
     * countries combo box model, or returns {@code stored.trim()} unchanged if
     * no matching country is loaded.
     */
    public static String resolveCountryId(String stored) {
        if (stored == null || stored.trim().isEmpty()) {
            return "";
        }
        String trimmed = stored.trim();
        try {
            ComboBoxModel model = getCountriesAsComboBoxModel();
            int idx = findCountryIndex(trimmed, model);
            if (idx >= 0) {
                Object o = model.getElementAt(idx);
                if (o instanceof MPComboBoxModelItem) {
                    return ((MPComboBoxModelItem) o).getId();
                }
            }
        } catch (Throwable t) {
            Log.Debug(LanguageManager.class, t);
        }
        return trimmed;
    }

    /**
     * @param a a country as stored (name in any language, ISO numeric or alpha-2 code)
     * @param b another one
     * @return true if both name the same country ("Germany", "Deutschland", "276", "DE")
     */
    public static boolean sameCountry(String a, String b) {
        if (a == null || b == null || a.trim().isEmpty() || b.trim().isEmpty()) {
            return false;
        }
        if (a.trim().equalsIgnoreCase(b.trim())) {
            return true;
        }
        String ca = mpv5.utils.export.ZugpferdFile.toCountryCode(a.trim());
        return !ca.isEmpty() && ca.equals(mpv5.utils.export.ZugpferdFile.toCountryCode(b.trim()));
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
                                    fixRedefinedKeys(newbundle);
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
        if (COUNTRIES == null || COUNTRIES.isEmpty()) {
            Vector<MPComboBoxModelItem> loaded = new Vector<MPComboBoxModelItem>();
            try {
                QueryCriteria2 q = new QueryCriteria2();
                q.and(new QueryParameter(Context.getCountries(), "groupsids", User.getCurrentUser().__getGroupsids(), QueryParameter.EQUALS));
                Object[][] data = QueryHandler.instanceOf().clone(Context.getCountries()).getColumns(new String[]{"iso", "cname"}, 0, q);
                for (int i = 0; i < data.length; i++) {
                    Object[] objects = data[i];
                    loaded.add(new MPComboBoxModelItem(objects[0].toString(), objects[1].toString()));
                }
            } catch (Exception ex) {
                // Fall back to default group 1 if the user's group has no own country rows
                try {
                    QueryCriteria2 q1 = new QueryCriteria2();
                    q1.and(new QueryParameter(Context.getCountries(), "groupsids", 1, QueryParameter.EQUALS));
                    Object[][] data = QueryHandler.instanceOf().clone(Context.getCountries()).getColumns(new String[]{"iso", "cname"}, 0, q1);
                    for (int i = 0; i < data.length; i++) {
                        Object[] objects = data[i];
                        loaded.add(new MPComboBoxModelItem(objects[0].toString(), objects[1].toString()));
                    }
                } catch (Exception ex2) {
                    Log.Debug(LanguageManager.class, ex2.getMessage());
                    return new MPComboboxModel(new MPComboBoxModelItem[]{});
                }
            }
            if (!loaded.isEmpty()) {
                COUNTRIES = loaded;
                Log.Debug(LanguageManager.class, "Cached countries: " + COUNTRIES.size());
            } else {
                return new MPComboboxModel(new MPComboBoxModelItem[]{});
            }
        }
        return MPComboBoxModelItem.toModel(localizedCountries());
    }

    /**
     * The cached countries with their names in the language of the user, sorted
     * by these names (the cache keeps the names as stored: the locale is set at
     * the login, after the cache may have been filled)
     */
    private static List<MPComboBoxModelItem> localizedCountries() {
        List<MPComboBoxModelItem> l = new ArrayList<MPComboBoxModelItem>();
        Locale locale = Locale.getDefault();
        for (MPComboBoxModelItem c : COUNTRIES) {
            l.add(new MPComboBoxModelItem(c.getId(), localizedCountryName(c.getId(), c.getValue(), locale)));
        }
        if (!"en".equals(locale.getLanguage())) {
            final java.text.Collator col = java.text.Collator.getInstance(locale);
            Collections.sort(l, (a, b) -> col.compare(a.getValue(), b.getValue()));
        }
        return l;
    }

    /**
     *
     * @return A comboBoxModel reflecting the available locales
     */
    public static DefaultComboBoxModel getLocalesAsComboBoxModel() {
        //only language + country: the country gives the currency (without it: the symbol \u00a4)
        java.util.Map<String, MPComboBoxModelItem> unique = new java.util.TreeMap<String, MPComboBoxModelItem>();
        for (Locale l : Locale.getAvailableLocales()) {
            String language = l.getLanguage();
            String country = l.getCountry();
            if (language.isEmpty() || country.length() != 2 || !l.getVariant().isEmpty() || !l.getScript().isEmpty()) {
                continue;
            }
            String id = language + "_" + country;
            unique.put(id, new MPComboBoxModelItem(id, l.getDisplayName() + "  [" + id + "]"));
        }
        return new DefaultComboBoxModel(ArrayUtilities.sort(unique.values().toArray(new MPComboBoxModelItem[0])));
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

    /**
     * Returns a language display name that does not collide with any existing
     * {@code languages.longname} entry and fits within the 250-character column limit.
     */
    public static String uniqueLanguageName(String baseName) {
        java.util.List<String> existing = new ArrayList<String>();
        try {
            Object[][] data = QueryHandler.instanceOf().clone(Context.getLanguage()).select("longname", (String[]) null);
            if (data != null) {
                for (Object[] row : data) {
                    if (row != null && row.length > 0 && row[0] != null) {
                        existing.add(String.valueOf(row[0]));
                    }
                }
            }
        } catch (Exception e) {
            Log.Debug(LanguageManager.class, e);
        }
        return uniqueLanguageName(baseName, existing);
    }

    static String uniqueLanguageName(String baseName, java.util.Collection<String> existing) {
        String base = baseName == null ? "" : baseName.trim();
        if (base.isEmpty()) {
            base = "Own translation";
        }
        if (base.length() > 240) {
            base = base.substring(0, 240).trim();
        }
        Set<String> used = new HashSet<String>();
        if (existing != null) {
            for (String s : existing) {
                if (s != null) {
                    used.add(s.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        if (!used.contains(base.toLowerCase(Locale.ROOT))) {
            return base;
        }
        for (int n = 2; n < 10000; n++) {
            String candidate = base + " (" + n + ")";
            if (!used.contains(candidate.toLowerCase(Locale.ROOT))) {
                return candidate;
            }
        }
        return base + " (" + RandomText.getText(6) + ")";
    }

    public static String importLanguage(String langname, File file, Boolean ignoreErrors) throws Exception {
        try {
            String langid = RandomText.getText();

            file = FileDirectoryHandler.unzipFile(file);
            Log.Debug(LanguageManager.class, "Importing: " + file);

            if (ignoreErrors || hasNeededKeys(file, true)) {
                String dbname = null;
                try {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            mpv5.YabsViewProxy.instance().setWaiting(true);
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                    dbname = QueryHandler.instanceOf().clone(Context.getFiles()).insertFile(file);
                    if (dbname == null) {
                        mpv5.YabsViewProxy.instance().addMessage(Messages.ERROR_OCCURED.toString());
                        if (!quietImport) {
                            Popup.notice(Messages.ERROR_OCCURED.toString());
                        }
                        return null;
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
                        failed = false;
                        cacheRunWatchdog.remove(langid);
                        cachelanguage(langid, getBundle(langid));
                        return langid;
                    } else {
                        try {
                            QueryHandler.instanceOf().clone(Context.getFiles()).removeFile(dbname);
                        } catch (Exception ex) {
                            Log.Debug(LanguageManager.class, ex);
                        }
                        mpv5.YabsViewProxy.instance().addMessage(Messages.ERROR_OCCURED.toString());
                        if (!quietImport) {
                            Popup.notice(Messages.ERROR_OCCURED.toString());
                        }
                        return null;
                    }
                } catch (FileNotFoundException ex) {
                    Log.Debug(LanguageManager.class, ex);
                    return null;
                } catch (Exception x) {
                    if (dbname != null) {
                        try {
                            QueryHandler.instanceOf().clone(Context.getFiles()).removeFile(dbname);
                        } catch (Exception ex) {
                            Log.Debug(LanguageManager.class, ex);
                        }
                    }
                    //insert was not possible
                    throw new UnsupportedOperationException("Language could not be inserted, file already exists!?", x);
                }
            } else {
                return null;
            }
        } catch (Exception exception) {
            Popup.error(exception);
            throw exception;
        } finally {
            SwingUtilities.invokeLater(() -> mpv5.YabsViewProxy.instance().setWaiting(false));
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
                for (java.util.Map.Entry<String, String> e : missingKeys(file).entrySet()) {
                    Log.Debug(LanguageManager.class, "Key '" + e.getKey() + "' not found in file " + file);
                    failures.add(e.getKey() + "=" + e.getValue());
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
        if (langid == null || langid.trim().isEmpty()) {
            return;
        }
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
            //the language itself too (before only its file: the list kept a broken entry)
            QueryHandler.instanceOf().clone(Context.getLanguage()).delete(new mpv5.db.common.QueryCriteria("cname", langid));
            cachedLanguages.remove(langid);
            cacheRunWatchdog.remove(langid);
            germanLanguages.remove(langid);
        } else {
            Popup.notice(Messages.NOT_POSSIBLE);
        }
    }

    /**
     * @param file a language file
     * @return the keys the built-in language has and the file has not, with
     * the English text; read as a properties file, so escaped keys (e.g.
     * "a\\ b") and \\uXXXX escapes are found
     * @throws java.io.IOException
     */
    static java.util.SortedMap<String, String> missingKeys(File file) throws java.io.IOException {
        java.util.Set<String> has = readLanguageFile(file).keySet();
        java.util.SortedMap<String, String> missing = new java.util.TreeMap<String, String>();
        for (java.util.Map.Entry<String, String> e : requiredKeys().entrySet()) {
            if (!has.contains(e.getKey())) {
                missing.put(e.getKey(), e.getValue());
            }
        }
        return missing;
    }

    private static boolean hasNeededKeys(File file, boolean popupOnError) {
        synchronized (LanguageManager.class) {
            try {
                java.util.SortedMap<String, String> missing = missingKeys(file);
                if (missing.isEmpty()) {
                    return true;
                }
                //only a check: a file without the keys of a newer version (e.g. the
                //cached copy after an update) is completed from the database copy;
                //it must not disable all languages ("failed") for the session
                Log.Debug(LanguageManager.class, "Keys " + missing.keySet() + " not found in file " + file);
                if (popupOnError) {
                    List<String> failures = new ArrayList<String>();
                    for (java.util.Map.Entry<String, String> e : missing.entrySet()) {
                        failures.add(e.getKey() + "=" + e.getValue());
                    }
                    Popup.notice(failures, "Import not possible.\nMissing keys in " + file + ":\n");
                }
                return false;
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
        synchronized (LanguageManager.class) {
            try {
                java.util.SortedMap<String, String> missing = missingKeys(file);
                //the cached file is named after the id of the language (a random text):
                //German by the name of the pack, not by letters in the file name
                String fname = file.getName();
                String langid = fname.endsWith(".properties") ? fname.substring(0, fname.length() - ".properties".length()) : fname;
                boolean isDe = isGermanLanguage(langid) || isGermanName(fname);
                StringBuilder add = new StringBuilder("\n");
                for (java.util.Map.Entry<String, String> e : missing.entrySet()) {
                    Log.Debug(LanguageManager.class, "Key '" + e.getKey() + "' added to file " + file);
                    String val = isDe ? bundledGerman().getProperty(e.getKey()) : null;
                    if (val == null) {
                        val = e.getValue();
                    }
                    add.append(escape(e.getKey(), true)).append('=').append(escape(val, false)).append('\n');
                }
                if (!missing.isEmpty()) {
                    //ASCII with \\u escapes: right in UTF-8 and ISO-8859-1 files
                    java.nio.file.Files.write(file.toPath(), add.toString().getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                            java.nio.file.StandardOpenOption.APPEND);
                }
                failed = false;
                return true;
            } catch (Exception e) {
                Log.Debug(e);
                fail(e.getMessage());
                failed = true;
                return false;
            }
        }
    }

    /**
     * Texts whose meaning changed since old language files (YaBS 1.2): there
     * the company fields IBAN/BIC were labelled "Konto"/"IBAN" and the VAT ID
     * "Etwas". Such a file shows a wrong label above the field, so these texts
     * always come from the bundled language (German) or English.
     */
    static final String[] REDEFINED_KEYS = {
        "ControlPanel_Company.account._Label",
        "ControlPanel_Company.banknumber._Label",
        "ControlPanel_Company.bankname._Label",
        "ControlPanel_Company.account1._Label",
        "ControlPanel_Company.taxadvjob._Label",
        "ControlPanel_Company.taxmandant._Label"
    };

    private static void fixRedefinedKeys(File file) {
        try {
            String fname = file.getName();
            String langid = fname.endsWith(".properties") ? fname.substring(0, fname.length() - ".properties".length()) : fname;
            boolean isDe = isGermanLanguage(langid) || isGermanName(fname);
            ResourceBundle en = ResourceBundle.getBundle(defLanguageBundleName);
            StringBuilder add = new StringBuilder("\n");
            for (String k : REDEFINED_KEYS) {
                String val = isDe ? bundledGerman().getProperty(k) : null;
                if (val == null && en.containsKey(k)) {
                    val = en.getString(k);
                }
                if (val != null) {
                    //a later entry replaces the earlier one when the file is read
                    add.append(escape(k, true)).append('=').append(escape(val, false)).append('\n');
                }
            }
            java.nio.file.Files.write(file.toPath(), add.toString().getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception e) {
            Log.Debug(LanguageManager.class, e);
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
                String lid = langid.toLowerCase();
                boolean matches = previous.startsWith(("language-" + lid).toLowerCase())
                        || previous.startsWith((lid + "-").toLowerCase())
                        || previous.startsWith((lid + "_").toLowerCase())
                        || previous.equals(lid)
                        || (lid.equals("de") && (previous.contains("deutsch") || previous.contains("german") || previous.contains("deu") || previous.startsWith("de")));
                if (matches) {
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

        //no message box: this runs at the start (language update) and on request
        String ref = LanguageManager.importLanguageQuietly(f.getName() + " (" + new Date() + ")", f);
        if (ref == null) {
            //not imported: the user keeps the language (was set to null = English)
            throw new IllegalStateException("Language " + langid + " could not be imported from " + f);
        }
        User.getCurrentUser().setLanguage(ref);
        User.getCurrentUser().save(true);
        Log.Debug(LanguageManager.class, "\n\n\n#####################################\nLanguage updated: " + langid + "\n#####################################\n\n");
    }

    private static java.util.Properties bundledGerman;
    private static final java.util.Map<String, Boolean> germanLanguages = new java.util.HashMap<String, Boolean>();

    /**
     * @return true if the active language is German: the language pack the
     * user has selected (not the locale or the system: a French pack stays
     * French on a German system). Before the main window exists (login, first
     * start) there is no pack yet: the system language decides.
     */
    public static synchronized boolean isGerman() {
        if (!Main.INSTANTIATED) {
            return isGermanUser(null);
        }
        try {
            User u = User.getCurrentUser();
            return u != null && isGermanLanguage(u.__getLanguage());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param langid the id of an imported language (a random text), or
     * "buildin_en"
     * @return true if the language pack is German, by its name (as the
     * language selection: "DE-...", "language-de...", "Deutsch", "German")
     */
    private static final ThreadLocal<Boolean> checkingGerman =
            ThreadLocal.withInitial(() -> false);
    public static synchronized boolean isGermanLanguage(String langid) {
        if (langid == null || langid.trim().isEmpty()
                || "null".equals(langid) || "buildin_en".equals(langid)) {
            return false;
        }

        Boolean known = germanLanguages.get(langid);
        if (known != null) {
            return known;
        }

        // Break the cycle if exception-message localization calls back here.
        if (checkingGerman.get()) {
            return false;
        }

        checkingGerman.set(true);
        try {
            Object[] data = QueryHandler.instanceOf().clone(Context.getLanguage())
                    .selectFirst("longname",
                            new String[]{Context.SEARCH_NAME, langid, "'"});

            boolean german = data != null && data.length > 0
                    && isGermanName(String.valueOf(data[0]));
            germanLanguages.put(langid, german);
            return german;
        } catch (Exception e) {
            return false;
        } finally {
            checkingGerman.remove();
        }
    }

    /**
     * @param name the name of a language pack, e.g. "de-2.0.10.properties
     * (...)" (imported from YaBS) or a name given at the import
     * @return true for a German pack: the name starts with "de" as the
     * language code, or with "language-de", "deutsch", "german", or contains
     * one of the words de, deu, ger, deutsch, german, sprachdatei (older packs:
     * "Yabs-1.2-beta7-Sprachdatei-DEU.txt", "1508740910373_Yabs-1.2.7.2_DEU")
     */
    static boolean isGermanName(String name) {
        if (name == null) {
            return false;
        }
        String n = name.trim().toLowerCase(java.util.Locale.ROOT);
        return n.equals("de") || n.matches("de[-_. (].*") || n.startsWith("language-de")
                || n.startsWith("deutsch") || n.startsWith("german")
                || n.matches(".*(^|[^a-z])(de|deu|ger|deutsch|german|sprachdatei)([^a-z]|$).*");
    }

    /**
     * A text missing in the language of the database (e.g. a German language
     * imported before the text existed): taken from the German file bundled
     * with YaBS when the language is German, so new texts are not English
     *
     * @param key
     * @return the German text or null
     */
    public static synchronized String fallback(String key) {
        try {
            return isGerman() ? bundledGerman().getProperty(key) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param key
     * @param value the text found in the language of the database
     * @return the text; the German one of the bundled file when the language
     * is German but the text is still the English default (the language was
     * imported before the text existed)
     */
    public static String localize(String key, String value) {
        try {
            String english = ResourceBundle.getBundle(defLanguageBundleName).getString(key);
            if (value != null && value.equals(english)) {
                String de = fallback(key);
                if (de != null && !de.isEmpty()) {
                    return de;
                }
            }
        } catch (Exception e) {
            //no default text
        }
        return value;
    }

    private static synchronized java.util.Properties bundledGerman() {
        if (bundledGerman == null) {
            bundledGerman = new java.util.Properties();
            try (java.io.InputStream in = Main.class.getResourceAsStream("/extra/DE-" + Constants.EXTRA_FILES_VERSION + ".properties")) {
                if (in != null) {
                    bundledGerman.load(in);
                }
            } catch (Exception e) {
                Log.Debug(LanguageManager.class, e);
            }
        }
        return bundledGerman;
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
    /**
     * @return all keys a language needs, with the English text, sorted
     */
    public static java.util.SortedMap<String, String> requiredKeys() {
        ResourceBundle b = ResourceBundle.getBundle(defLanguageBundleName);
        java.util.SortedMap<String, String> m = new java.util.TreeMap<String, String>();
        for (Enumeration<String> e = b.getKeys(); e.hasMoreElements();) {
            String k = e.nextElement();
            m.put(k, b.getString(k));
        }
        return m;
    }

    /**
     * @param langid
     * @return the texts the language really has (without the English
     * fallback)
     */
    public static java.util.Map<String, String> ownKeys(String langid) {
        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
        if ("buildin_en".equals(langid)) {
            return m;
        }
        ResourceBundle b = getBundle(langid);
        for (Enumeration<String> e = b.getKeys(); e.hasMoreElements();) {
            String k = e.nextElement();
            try {
                m.put(k, b.getString(k));
            } catch (Exception ex) {
                Log.Debug(LanguageManager.class, ex.getMessage());
            }
        }
        return m;
    }

    /**
     * Writes a language file (.properties, ASCII with \\u escapes: readable
     * as UTF-8 and ISO-8859-1)
     *
     * @param texts key - text
     * @param file
     * @throws java.io.IOException
     */
    public static void writeLanguageFile(java.util.Map<String, String> texts, File file) throws java.io.IOException {
        StringBuilder s = new StringBuilder("# YaBS language file\n");
        for (java.util.Map.Entry<String, String> e : new java.util.TreeMap<String, String>(texts).entrySet()) {
            s.append(escape(e.getKey(), true)).append('=').append(escape(e.getValue(), false)).append('\n');
        }
        java.nio.file.Files.write(file.toPath(), s.toString().getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }

    private static String escape(String v, boolean key) {
        StringBuilder s = new StringBuilder();
        String t = v == null ? "" : v;
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            switch (c) {
                case '\\':
                    s.append("\\\\");
                    break;
                case '\n':
                    s.append("\\n");
                    break;
                case '\r':
                    s.append("\\r");
                    break;
                case '\t':
                    s.append("\\t");
                    break;
                default:
                    if ((key && (c == '=' || c == ':' || c == ' ')) || (i == 0 && c == ' ') || c == '#' && i == 0 || c == '!' && i == 0) {
                        s.append('\\').append(c);
                    } else if (c < 0x20 || c > 0x7e) {
                        s.append(String.format("\\u%04x", (int) c));
                    } else {
                        s.append(c);
                    }
            }
        }
        return s.toString();
    }

    /**
     * Reads a language file (.properties; UTF-8 or ISO-8859-1)
     *
     * @param file
     * @return key - text
     * @throws java.io.IOException
     */
    public static java.util.Map<String, String> readLanguageFile(File file) throws java.io.IOException {
        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        String text;
        try {
            text = java.nio.charset.StandardCharsets.UTF_8.newDecoder()
                    .decode(java.nio.ByteBuffer.wrap(bytes)).toString();
        } catch (java.nio.charset.CharacterCodingException notUtf8) {
            text = new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);
        }
        java.util.Properties p = new java.util.Properties();
        p.load(new java.io.StringReader(text));
        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
        for (String k : p.stringPropertyNames()) {
            m.put(k, p.getProperty(k));
        }
        return m;
    }

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
                if (isGermanName(name)) {
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
