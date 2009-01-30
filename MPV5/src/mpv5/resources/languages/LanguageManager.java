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
import java.util.Enumeration;
import java.util.ResourceBundle;
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
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileReaderWriter;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.text.RandomText;

/**
 *
 * @author Administrator
 */
public class LanguageManager {

    private static final String defLanguageBundle = "mpv5/resources/languages/Panels";
    private static MPComboBoxModelItem[][] defLanguage =new MPComboBoxModelItem[][]{{new MPComboBoxModelItem("en", "English")}};

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
     * The users selected language bundle, or default if not defined by the user
     * @return
     */
    public static String getBundle() {
        File bundlefile = null;
        Object[][] data;
        try {
            data = QueryHandler.instanceOf().clone(Context.getLanguage()).
                    select("filename", new String[]{Context.SEARCH_NAME, MPV5View.getUser().__getLanguage(), "'"});
            if (data !=null && data.length>0) {
                bundlefile = QueryHandler.instanceOf().clone(Context.getFiles()).retrieveFile(String.valueOf(
                        data));
            }
            if (bundlefile != null) {
                FileDirectoryHandler.copyFile(bundlefile, new File("/languages"), "user.properties");
                return "/languages/user.properties";
            } else {
                Log.Debug(LanguageManager.class, "Error loading additional languages. Using default now.");
            }
        } catch (Exception e) {
            Log.Debug(LanguageManager.class, e);
        }
        return defLanguageBundle;
    }

    /**
     *
     * @return A ComboBoxModel reflecting the available Languages
     */
    public static ComboBoxModel getLanguagesAsComboBoxModel() {
        Object[][] data = QueryHandler.instanceOf().clone(Context.getLanguage()).select("cname, longname", new String[]{Context.SEARCH_NAME, MPV5View.getUser().__getLanguage(), "'"});
        MPComboBoxModelItem[] t = null;
        Object[][] ldata;
            if (data !=null && data.length>0) {
               ldata = ArrayUtils.merge(defLanguage, data);
            } else {
            data = defLanguage;
        }
        t = MPComboBoxModelItem.toItems(data);
        return new DefaultComboBoxModel(t);
    }

    /**
     * Imports a language file to DB
     * @param langname
     * @param file
     */
    public static void importLanguage(String langname, String file) {
      String langid = new RandomText().getString();
        if (hasNeededKeys(file)) {
            try {
                String dbname = QueryHandler.instanceOf().clone(Context.getFiles()).insertFile(new File(file));
                QueryHandler.instanceOf().clone(Context.getLanguage()).
                        insert(new String[]{"cname, longname, filename",
                        PrepareData.finalize(PrepareData.prepareString(langid) +
                                PrepareData.prepareString(langname) +
                                PrepareData.prepareString(dbname)), null},
                                new int[]{0}, "Imported language: " + langname);
            } catch (FileNotFoundException ex) {
                Log.Debug(LanguageManager.class, ex);
            }
        }
    }

    private static boolean hasNeededKeys(String file) {
        Enumeration<String> keys = ResourceBundle.getBundle(defLanguageBundle).getKeys();
        File impFile = new File(file);
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
