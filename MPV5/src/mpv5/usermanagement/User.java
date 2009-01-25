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
import mpv5.db.common.DatabaseObject;

/**
 *
 * @author Andreas
 */
public class User extends DatabaseObject {

    private String password = "";
    private String laf = "";
    private String locale = "";
    private String mail = "";
    private String language = "";
    private int highestright = 4;
    private boolean isenabled = true;
    private Date lastlogdate = new Date();

    public User(int userid) {
    }

    public User() {
    }

    public int __getHighestRight() {
        return SecurityManager.RIGHT_TO_CREATE;
    }

    public Integer __getID() {
        return __getIDS();
    }

    public String __getName() {
        return cname;
    }

    @Override
    public String __getCName() {
        return cname;
    }

    @Override
    public void setCName(String name) {
        cname = name;
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
    public int __getHighestright() {
        return highestright;
    }

    /**
     * @param highestright the highestright to set
     */
    public void setHighestright(int highestright) {
        this.highestright = highestright;
    }

    /**
     * @return the isenabled
     */
    public boolean isIsenabled() {
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
    public Date __getLastlogdate() {
        return lastlogdate;
    }

    /**
     * @param lastlogdate the lastlogdate to set
     */
    public void setLastlogdate(Date lastlogdate) {
        this.lastlogdate = lastlogdate;
    }
}
