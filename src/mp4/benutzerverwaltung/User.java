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
package mp4.benutzerverwaltung;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import mp4.items.visual.Popup;
import mp4.items.Things;
import mp4.logs.*;
import mp4.utils.security.md5hash;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.datenbank.verbindung.PrepareData;
import mp4.utils.datum.DateConverter;
import mp4.utils.tabellen.DataModelUtils;

public class User extends Things {

    private String name = "Niemand";
    private boolean validUser = false;
    private String createdBy;
    private String passwordHash;
    private Date createdOn;
    private boolean isAdmin = false;
    private boolean useAuth = false;
    private boolean isEditor = false;
    private md5hash md;
    private Integer id = 0;
    public final static int EDITOR = 0;
    public final static int ADMIN = 1;

    public User() {
        super(ConnectionHandler.instanceOf().clone(TABLE_USER));

        try {
            md = md5hash.getInstance();
        } catch (NoSuchAlgorithmException ex) {
            Log.Debug(ex);
        }

    }

    public User(Integer id) {
        super(ConnectionHandler.instanceOf().clone(TABLE_USER));
        String[] t = null;
        try {
            t = this.selectLast("*", "id", id.toString(), true);
        } catch (Exception ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (t != null && t.length > 0) {

            this.setName(t[1]);
            this.setCreatedOn(DateConverter.getDate(t[3]));
            this.setCreatedBy(t[4]);
            this.setIsEditor(PrepareData.parseBoolean(t[5]));
            this.setIsAdmin(PrepareData.parseBoolean(t[6]));

        } else {
            Popup.notice("Benutzername existiert nicht!");
        }

    }

    public User(String username, String password, boolean isEditor, boolean isAdmin, User user) {
        super(ConnectionHandler.instanceOf().clone(TABLE_USER));

        try {
            md = md5hash.getInstance();
        } catch (NoSuchAlgorithmException ex) {
            Log.Debug(ex);
        }

        this.name = username;
        this.passwordHash = md.hashData(password.getBytes());
        this.isAdmin = isAdmin;
        this.isEditor = isEditor;
        this.createdBy = user.getName();

        this.save();
    }

    public boolean checkUser(String username, String password) {

        String[][] t = this.select("*", "username", username, false);

        if (t != null && t.length > 0) {
            for (int i = 0; i < t.length; i++) {
                String[] strings = t[i];
                if (md.hashData(password.getBytes()).matches(strings[2])) {
                    this.setValidUser(true);
                    this.setName(strings[1]);
                    this.setCreatedOn(DateConverter.getDate(strings[3]));
                    this.setCreatedBy(strings[4]);
                    this.setIsEditor(PrepareData.parseBoolean(strings[5]));
                    this.setIsAdmin(PrepareData.parseBoolean(strings[6]));
                }
            }
            if (!isValidUser()) {
                Popup.notice("Falsches Passwort!");
            }
        } else {
            Popup.notice("Benutzername existiert nicht!");
        }
        return isValidUser();
    }

    public void deactivate(Integer id) {
        this.update("deleted", "1", id.toString());
    }

    public Object[][] getAll() {
        return DataModelUtils.changeToClassValue(this.select("id, username, createdon, createdby, iseditor, isadmin", "deleted", "0", true),
                Boolean.class, new int[]{4, 5});

    }

    private String collect() {
        String str = PrepareData.prepareString(this.getName());
        str = str + PrepareData.prepareString(this.getPasswordHash());
        str = str + PrepareData.prepareString(DateConverter.getSQLDateString(new Date()));
        str = str + PrepareData.prepareString(this.getCreatedBy());
        str = str + PrepareData.prepareBoolean(isIsEditor());
        str = str + PrepareData.prepareBoolean(isIsAdmin());
        return PrepareData.finalize(str);
    }

    public boolean save() {
        int result = -1;
        result = this.insert(TABLE_USER_FIELDS, this.collect(),new int[]{0});
        if (result > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * return User
     */
    @Override
    public String toString() {
        return this.getName();
    }

    private void setValidUser(boolean value) {
        this.validUser = value;
    }

    public boolean isValidUser() {
        return validUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public boolean isIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean isIsEditor() {
        return isEditor;
    }

    public void setIsEditor(boolean isEditor) {
        this.isEditor = isEditor;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean doAction(int level) {
        if (!useAuth) {
            return true;
        }
        switch (level) {
            case User.EDITOR:
                if (this.isEditor) {
                    return true;
                }
            case User.ADMIN:
                if (this.isAdmin) {
                    return true;
                }
            default:
                Popup.notice("Sie haben keine Berechtigung,\ndiese Aktion auszufuehren.");
                return false;
        }
    }

    public void setUseAuth(boolean useAuth) {
        this.useAuth = useAuth;
    }
}
