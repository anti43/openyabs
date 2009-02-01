/*
 *  This file is part of MP by anti43 /GPL.
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
package mpv5.resources.languages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import mpv5.db.common.Context;
import mpv5.db.common.PrepareData;
import mpv5.db.common.QueryHandler;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.frames.MPV5View;
import mpv5.utils.arrays.ArrayUtils;
import mpv5.utils.arrays.ListenDataUtils;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileReaderWriter;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.reflection.ClasspathTools;
import mpv5.utils.text.RandomText;

/**
 *
 * @author Administrator
 */
public class LanguageManager {

    private static final String defLanguageBundle = "mpv5/resources/languages/Panels";
    private static String[][] defLanguage = new String[][]{{"buildin_en", "English"}};

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

    /**
     * 
     * @param langid
     * @return
     */
    public static ResourceBundle getBundle(String langid) {
        if (!langid.contentEquals("buildin_en")) {
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
                    newfile = FileDirectoryHandler.copyFile(bundlefile, new File("languages"), tempname + ".properties");
                    ClasspathTools.addPath(new File("languages"));//Add the files parent to classpath to be found
                    FileDirectoryHandler.deleteTreeOnExit(new File("languages"));
                    Log.Debug(LanguageManager.class, "Created language file at: " + newfile);
                    try {
                        ResourceBundle bundle = java.util.ResourceBundle.getBundle(tempname);
                        return bundle;
                    } catch (Exception e) {
                        Log.Debug(LanguageManager.class, e);
                    }
                }
            } catch (Exception e) {
                Log.Debug(LanguageManager.class, e);
            }
        }
        Log.Debug(LanguageManager.class, "Error loading additional languages. Using default now.");
        return java.util.ResourceBundle.getBundle(defLanguageBundle);
    }

    /**
     * The users selected language bundle, or default if not defined by the user
     * @return
     */
    public static ResourceBundle getBundle() {
        return getBundle(MPV5View.getUser().__getLanguage());
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

        return ListenDataUtils.listToStringArrayArray(list);
    }

    /**
     *
     * @return A ComboBoxModel reflecting the available Languages
     */
    public static ComboBoxModel getLanguagesAsComboBoxModel() {
        Object[][] data = QueryHandler.instanceOf().clone(Context.getLanguage()).select("cname, longname", null);
        MPComboBoxModelItem[] t = null;
        Object[][] ldata;
        ldata = ArrayUtils.merge(defLanguage, data);
        t = MPComboBoxModelItem.toItems(ldata);
        return new DefaultComboBoxModel(t);
    }

    /**
     * Imports a language file to DB
     * @param langname
     * @param file
     */
    public static void importLanguage(String langname, File file) {
        String langid = new RandomText().getString();
        if (hasNeededKeys(file)) {
            try {
                String dbname = QueryHandler.instanceOf().clone(Context.getFiles()).insertFile(file);
                QueryHandler.instanceOf().clone(Context.getLanguage()).
                        insert(new String[]{"cname, longname, filename",
                            PrepareData.finalize(PrepareData.prepareString(langid) +
                            PrepareData.prepareString(langname) +
                            PrepareData.prepareString(dbname)), null},
                        new int[]{0}, "Imported language: " + langname);
                MPV5View.addMessage(langname + Messages.ROW_UPDATED);
            } catch (FileNotFoundException ex) {
                Log.Debug(LanguageManager.class, ex);
            }
        }
    }

    private static boolean hasNeededKeys(File file) {
        Enumeration<String> keys = ResourceBundle.getBundle(defLanguageBundle).getKeys();
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
                Log.Debug(LanguageManager.class, "Key '" + string + "' not found in file " + file);
                MPV5View.addMessage(Messages.ERROR_OCCURED);
                return false;
            }
        }
        return true;
    }
}
