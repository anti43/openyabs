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
package mpv5.usermanagement;

import java.util.Date;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.globals.Messages;
import mpv5.ui.dialogs.Popup;

/**
 *
 * @author Andreas
 */
public class User extends DatabaseObject {

    private String password = "";
    private String fullname = "";
    private String laf = "";
    private String locale = "en_UK";
    private String mail = "";
    private String language = "buildin_en";
    private int inthighestright = 4;
    private boolean isenabled = true;
    private boolean isloggedin = true;
    private Date datelastlog = new Date();
    public static User DEFAULT = new User("Default User", "nobody", -1, 4343);
    private int groupsids = 1;

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

    public boolean isDefault() {
        if (getName().equals("nobody") && __getFullname().equals("Default User")) {
            return true;
        } else {
            return false;
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
}
