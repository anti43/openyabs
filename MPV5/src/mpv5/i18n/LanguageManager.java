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
package mpv5.i18n;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import mpv5.Main;
import mpv5.db.common.Context;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryData;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPV5View;

import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.date.DateConverter;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileReaderWriter;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.reflection.ClasspathTools;
import mpv5.utils.text.RandomText;
import mpv5.utils.xml.XMLReader;
import org.jdom.Document;

/**
 *
 *  Administrator
 */
public class LanguageManager {

    private static final String defLanguageBundle = "mpv5/resources/languages/Panels";
    private static String[][] defLanguage = new String[][]{{"buildin_en", "English"}};
    private static Hashtable<String, ResourceBundle> cachedLanguages = new Hashtable<String, ResourceBundle>();

    /**
     * Flushes the Language Cache so that it contains no Languages.
     */
    public static void flushLanguageCache() {
        cachedLanguages.clear();
    }

    /**
     * Import & replace the country list
     * @param file
     */
    public static void importCountries(File file) {
        XMLReader r = new XMLReader();
        try {
            Document doc = r.newDoc(file, true);

            if (MPSecurityManager.checkAdminAccess() && !MPV5View.getUser().isDefault()) {
                if (doc != null) {
                    try {
                        QueryHandler.instanceOf().clone(Context.getCountries()).delete(new String[][]{{"usersids", MPV5View.getUser().getID().toString(), ""}});
                    } catch (Exception ex) {
                        Log.Debug(ex);
                    }
                    String[][] countries = r.toArray(r.getSubRootElement("countries"));
                    for (int i = 0; i < countries.length; i++) {
                        String[] country = countries[i];
                        QueryData t = new QueryData();
                        t.add("cname", country[1]);
                        t.add("iso", Integer.valueOf(country[2]));
                        t.add("groupsids", MPV5View.getUser().__getGroupsids());
                        QueryHandler.instanceOf().clone(Context.getCountries()).insert(t, Messages.DONE.toString());
                    }
//                MPV5View.addMessage(langname + Messages.ROW_UPDATED);
                }
            } else {
                Popup.notice(Messages.ADMIN_ACCESS + "\n" + Messages.DEFAULT_USER);
            }
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
    }

    private static ResourceBundle getCachedLanguage(String langid) {
        return cachedLanguages.get(langid);
    }

    /**
     * Checks the existance of a language in DB
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
    private static boolean failed = false;

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
                    if (!isCachedLanguage(langid)) {
                        Log.Debug(LanguageManager.class, "Language is not cached: " + langid);
                        File bundlefile = null;
                        Object[] data;
                        URI newfile;
                        String tempname = new RandomText(10).getString();
                        try {
                            data = QueryHandler.instanceOf().clone(Context.getLanguage()).
                                    selectFirst("filename", new String[]{Context.SEARCH_NAME, langid, "'"});
                            if (data != null && data.length > 0) {
                                bundlefile = QueryHandler.instanceOf().clone(Context.getFiles()).retrieveFile(String.valueOf(
                                        data[0]));
                            }
                            if (bundlefile != null) {
                                newfile = FileDirectoryHandler.copyFile(bundlefile, new File(LocalSettings.getProperty(LocalSettings.CACHE_DIR)), tempname + ".properties", true);
                                if (hasNeededKeys(bundlefile)) {
                                    Log.Debug(LanguageManager.class, "File has needed keys for language: " + langid);
                                    ClasspathTools.addPath(new File(LocalSettings.getProperty(LocalSettings.CACHE_DIR)));//Add the files parent to classpath to be found
//                            FileDirectoryHandler.deleteTreeOnExit(new File(LocalSettings.getProperty(LocalSettings.CACHE_DIR)));

                                    Log.Debug(LanguageManager.class, "Created language file at: " + newfile);
                                    try {
                                        ResourceBundle bundle = ResourceBundleUtf8.getBundle(tempname);
                                        cachelanguage(langid, bundle);
                                        return bundle;
                                    } catch (Exception e) {
                                        failed = true;
                                        Log.Debug(LanguageManager.class, e);
                                    }
                                } else {
                                    Log.Debug(LanguageManager.class, "Failed language: " + langid);
                                    failed = true;
                                }
                            }
                        } catch (Exception e) {
                            failed = true;
                            Log.Debug(LanguageManager.class, e);
                        }

                    } else {
                        return getCachedLanguage(langid);
                    }
                } else {
                    failed = true;
                    Log.Debug(LanguageManager.class, "Error loading additional languages. Using default now.");
                    return java.util.ResourceBundle.getBundle(defLanguageBundle);
                }
            }
        }

        return java.util.ResourceBundle.getBundle(defLanguageBundle);
    }

    /**
     * The users selected language bundle, or default if not defined by the user
     * @return
     */
    public static ResourceBundle getBundle() {
        if (Main.INSTANTIATED) {
            return getBundle(MPV5View.getUser().__getLanguage());
        } else {
            return java.util.ResourceBundle.getBundle(defLanguageBundle);
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
    private static PreparedStatement psc;

    /**
     *
     * @return A ComboBoxModel reflecting the available Countries
     */
    public static synchronized ComboBoxModel getCountriesAsComboBoxModel() {

        if (psc == null) {
            try {
                psc = QueryHandler.instanceOf().clone(Context.getCountries()).buildPreparedSelectStatement(new String[]{"iso", "cname"}, new String[]{"cname"}, null, true);
            } catch (SQLException ex) {
                Log.Debug(ex);
            }
        }
//        Object[][] data = QueryHandler.instanceOf().clone(Context.getCountries()).select("iso, cname", (String[]) null);
        Object[][] data = new Object[][]{};
        try {
            data = QueryHandler.instanceOf().clone(Context.getCountries()).executeStatement(psc, new Object[]{""}).getData();
        } catch (SQLException ex) {
            Logger.getLogger(LanguageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        MPComboBoxModelItem[] t = null;
        Object[][] ldata;
        ldata = ArrayUtilities.merge(new String[][]{{"", ""}}, data);
        t = MPComboBoxModelItem.toItems(ldata);
        return new DefaultComboBoxModel(t);
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
            Log.Debug(LanguageManager.class, locale_name);
            items[i] = new MPComboBoxModelItem(language + "_" + country,
                    locale_name + "  [" + language + "_" + country + "]");
//            items[i] = new MPComboBoxModelItem(o[i].toString(), o[i].getDisplayName());
        }

        return new DefaultComboBoxModel(ArrayUtilities.sort(items));
    }

    /**
     * Imports a language file to DB
     * @param langname
     * @param file
     */
    public static void importLanguage(String langname, File file) {
        String langid = new RandomText(10).getString();

        if (hasNeededKeys(file)) {
            try {
                String dbname = QueryHandler.instanceOf().clone(Context.getFiles()).insertFile(file);
                QueryData t = new QueryData();
                t.add("cname", langid);
                t.add("longname", langname);
                t.add("filename", dbname);
                t.add("dateadded", DateConverter.getTodayDBDate());
                Log.Debug(LanguageManager.class, "Adding language: " + langname);
                int id = QueryHandler.instanceOf().clone(Context.getLanguage()).insert(t, "Imported language: " + langname);
                if (id > 0) {
                    MPV5View.addMessage(langname + Messages.INSERTED.toString());
                    Popup.notice(langname + Messages.INSERTED.toString());
                } else {
                    MPV5View.addMessage(Messages.ERROR_OCCURED.toString());
                    Popup.notice(Messages.ERROR_OCCURED.toString());
                }
            } catch (FileNotFoundException ex) {
                Log.Debug(LanguageManager.class, ex);
            }

        }
    }

    /**
     * Deletes a language from db
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

    private static boolean hasNeededKeys(File file) {
        synchronized (new LanguageManager()) {
            Enumeration<String> keys = ResourceBundleUtf8.getBundle(defLanguageBundle).getKeys();
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
                    MPV5View.addMessage(Messages.ERROR_OCCURED.toString());

                    return false;
                }
            }
        }
        return true;
    }
}
