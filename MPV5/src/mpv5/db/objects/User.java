/*
 *  This file is part of MP by anti43 /GPL.
 *
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.db.objects;

import mpv5.usermanagement.*;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import mpv5.Main;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryData;
import mpv5.db.common.QueryHandler;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.db.objects.Property;
import mpv5.logging.Log;
import mpv5.pluginhandling.MP5Plugin;
import mpv5.pluginhandling.MPPLuginLoader;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Fonts;
import mpv5.ui.frames.MPV5View;
import mpv5.utils.text.TypeConversion;

/**
 *
 * @author Andreas
 */
public class User extends DatabaseObject {

    private String password = "";
    private String fullname = "";
    private String laf = "";
    private String locale = "en_UK";
    private String defcountry = "";
    private String mail = "";
    private String language = "buildin_en";
    private int inthighestright = 4;
    private boolean isenabled = true;
    private boolean isloggedin = true;
    private boolean isrgrouped = false;
    private Date datelastlog = new Date();
    public static User DEFAULT = new User("Default User", "nobody", -1, 4343);
    public static HashMap<String, String> userCache = new HashMap<String, String>();
    private static PropertyStore properties = new PropertyStore();

    /**
     * Caches all available usernames and IDs
     */
    public static void cacheUser() {
        userCache.clear();
        userCache.put(Integer.toBinaryString(DEFAULT.__getIDS()), DEFAULT.__getCName());
        try {
            ArrayList<DatabaseObject> data = DatabaseObject.getObjects(Context.getUser());
            for (int i = 0; i < data.size(); i++) {
                DatabaseObject databaseObject = data.get(i);
                userCache.put(Integer.toBinaryString(databaseObject.__getIDS()), databaseObject.__getCName());
            }
        } catch (NodataFoundException ex) {
            Log.Debug(User.class, ex.getMessage());
        }
    }

    /**
     * Tries to find the given ID in the DB
     * @param forId
     * @return A username if existing, else "unknown"
     */
    public static String getUsername(int forId) {
        if (userCache.containsKey(Integer.toBinaryString(forId))) {
            return userCache.get(Integer.toBinaryString(forId));
        } else {
            cacheUser();
            if (userCache.containsKey(Integer.toBinaryString(forId))) {
                return userCache.get(Integer.toBinaryString(forId));
            }
        }
        return "unknown";
    }

    /**
     * Tries to find an User with the given name
     * @param username
     * @return
     */
    public static int getUserId(String username) {

        if (userCache.containsValue(username)) {
            Iterator<Entry<String, String>> data = userCache.entrySet().iterator();

            while (data.hasNext()) {
                Entry<String, String> entry = data.next();
                if (entry.getValue().equals(username)) {
                    Log.Debug(User.class, "Cached user id found: " + Integer.parseInt(entry.getKey(), 2));
                    return Integer.parseInt(entry.getKey(), 2);
                }
            }
        } else {
            cacheUser();
            if (userCache.containsValue(username)) {
                Iterator<Entry<String, String>> data = userCache.entrySet().iterator();

                while (data.hasNext()) {
                    Entry<String, String> entry = data.next();
                    if (entry.getValue().equals(username)) {
                        Log.Debug(User.class, "Cached user id found: " + Integer.parseInt(entry.getKey(), 2));
                        return Integer.parseInt(entry.getKey(), 2);
                    }
                }
            }
        }
        return DEFAULT.ids;
    }

    /**
     * Tries to find the given ID in the DB
     * @param userid
     * @throws mpv5.db.common.NodataFoundException
     */
    public User(int userid) throws NodataFoundException {
        context.setDbIdentity(Context.IDENTITY_USERS);
        this.fetchDataOf(userid);
    }

    public User() {
        context.setDbIdentity(Context.IDENTITY_USERS);
    }

    private User(String fullname, String userid, int highright, int IDS) {
        context.setDbIdentity(Context.IDENTITY_USERS);
        this.fullname = fullname;
        this.cname = userid;
        this.inthighestright = highright;
        this.setIDS(IDS);
    }

    public Integer getID() {
        return __getIDS();
    }

    public String getName() {
        return cname;
    }

    @Override
    public String __getCName() {
        return cname;
    }

    /**
     * Return this users plugins
     * @return 
     */
    public MP5Plugin[] getPlugins() {
        return new MPPLuginLoader().getPlugins();
    }

    /**
     *
     * @return True if this user IS User.DEFAULT
     */
    public boolean isDefault() {
        if (getName().equals(DEFAULT.__getCName()) && __getIDS().intValue() == DEFAULT.__getIDS().intValue()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAdmin() {
        if (__getIDS().intValue() == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Logs in this user into MP
     */
    public void login() {
        if (isenabled) {
            MPV5View.setUser(this);
            setProperties();
            try {
                Locale.setDefault(TypeConversion.stringToLocale(__getLocale()));
                ControlPanel_Fonts.applyFont(Font.decode(LocalSettings.getProperty(LocalSettings.DEFAULT_FONT)));
                Main.setLaF(__getLaf());
            } catch (Exception e) {
                Log.Debug(e);
            }
            Lock.unlock(MPV5View.identifierFrame);
            Runnable runnable = new Runnable() {

                public void run() {
                    setDatelastlog(new Date());
                    setIsloggedin(true);
                    save();
                }
            };

            SwingUtilities.invokeLater(runnable);
        } else Popup.warn(Messages.NOT_POSSIBLE + Messages.USER_DISABLED);
    }

    /**
     * Logs out this user
     */
    public void logout() {
        saveProperties();
        MPV5View.setUser(DEFAULT);
        if (!isDefault()) {
            setIsloggedin(false);
            save();
        }
    }

    @Override
    public void setCName(String name) {
        cname = name;
    }

    @Override
    public boolean save() {
        if (!isDefault()) {
            return super.save();
        } else {
            Popup.notice(Messages.DEFAULT_USER);
            return false;
        }
    }

    @Override
    public boolean delete() {
        if (!isAdmin()) {
            return super.delete();
        } else {
            Popup.notice(Messages.ADMIN_USER);
            return false;
        }
    }

    /**
     * @return the password
     */
    public String __getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the laf
     */
    public String __getLaf() {
        return laf;
    }

    /**
     * @param laf the laf to set
     */
    public void setLaf(String laf) {
        this.laf = laf;
    }

    /**
     * @return the locale
     */
    public String __getLocale() {
        return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * @return the mail
     */
    public String __getMail() {
        return mail;
    }

    /**
     * @param mail the mail to set
     */
    public void setMail(String mail) {
        this.mail = mail;
    }

    /**
     * @return the language
     */
    public String __getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return the highestright
     */
    public int __getINThighestright() {
        return inthighestright;
    }

    /**
     * @param highestright the highestright to set
     */
    public void setINThighestright(int highestright) {
        this.inthighestright = highestright;
    }

    /**
     * @return the isenabled
     */
    public boolean __getIsenabled() {
        return isenabled;
    }

    /**
     * @param isenabled the isenabled to set
     */
    public void setIsenabled(boolean isenabled) {
        this.isenabled = isenabled;
    }

    @Override
    public String toString() {
        return cname;
    }

    /**
     * @return the lastlogdate
     */
    public Date __getDatelastlog() {
        return datelastlog;
    }

    /**
     * @param lastlogdate the lastlogdate to set
     */
    public void setDatelastlog(Date lastlogdate) {
        this.datelastlog = lastlogdate;
    }

    /**
     * @return the isloggedin
     */
    public boolean __getIsloggedin() {
        return isloggedin;
    }

    /**
     * @param isloggedin the isloggedin to set
     */
    public void setIsloggedin(boolean isloggedin) {
        this.isloggedin = isloggedin;
    }

    /**
     * @return the fullname
     */
    public String __getFullname() {
        return fullname;
    }

    /**
     * @param fullname the fullname to set
     */
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    /**
     * @return the defcountry
     */
    public String __getDefcountry() {
        return defcountry;
    }

    /**
     * @param defcountry the defcountry to set
     */
    public void setDefcountry(String defcountry) {
        this.defcountry = defcountry;
    }

    /**
     * If the usernames & ids match, return true
     * @param n
     * @return
     */
    @Override
    public boolean equals(Object n) {
        if (((User) n).__getCName().equals(this.__getCName()) && ((User) n).__getIDS().intValue() == this.__getIDS().intValue()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public JComponent getView() {
        return new mpv5.ui.dialogs.subcomponents.ControlPanel_Users(this);
    }

    /**
     * Set a property for this user
     * @param key
     * @param value
     */
    public void changeProperty(String key, String value) {
        properties.addProperty(key, value);
    }

    /**
     * Retrieve all properties from this user
     * @return 
     */
    public PropertyStore getProperties() {
        return properties;
    }

    /**
     * Saves the user properties
     */
    public void saveProperties() {

        if (properties.isChanged()) {
            ArrayList<String[]> l = properties.getList();
            for (int i = 0; i < l.size(); i++) {
                String[] d = l.get(i);
                Property p = new Property();
                p.setValue(d[1]);
                p.setCName(d[0]);
                p.setUsersids(getID());
                p.save();
            }
        }
        properties.setChanged(false);

    }

    /**
     * Delete the users properties
     */
    public void deleteProperties() {
        QueryCriteria c = new QueryCriteria();
        c.add("usersids", __getIDS());
        QueryHandler.instanceOf().clone(Context.getProperties()).delete(c);
    }

    private void setProperties() {
        Runnable runnable = new Runnable() {

            public void run() {
                QueryCriteria criteria = new QueryCriteria();
                criteria.add("usersids", ids);
                properties = new PropertyStore();
                try {
                    properties.addAll(QueryHandler.instanceOf().clone(Context.getProperties()).select("cname, value", criteria));
//            if (Log.getLoglevel() == Log.LOGLEVEL_DEBUG) {
//                properties.print();
//            }
                } catch (NodataFoundException ex) {
                    Log.Debug(this, ex.getMessage());
                }
            }
        };

        SwingUtilities.invokeLater(runnable);
    }

    /**
     * @return the isrgrouped
     */
    public boolean __getIsrgrouped() {
        return isrgrouped;
    }

    /**
     * @param isrgrouped the isrgrouped to set
     */
    public void setIsrgrouped(boolean isrgrouped) {
        this.isrgrouped = isrgrouped;
    }
}
