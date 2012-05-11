/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.db.objects;

import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.utils.images.MPIcon;
import mpv5.utils.text.RandomText;

/**
 * Represents a WebShop
 */
public class WebShop extends DatabaseObject {

    private String description = "";
    private int interv;
    private String url = "";
    private boolean isrequestCompression;
    private boolean isauthenticated;
    private String username = "";
    private String passw = "";

    public WebShop() {
        setContext(Context.getWebShop());
    }

    @Override
    public JComponent getView() {
        return null;
    }

    @Override
    public MPIcon getIcon() {
        return null;
    }

    /**
     * @return the description
     */
    public String __getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the interval
     */
    public int __getInterv() {
        return interv;
    }

    /**
     * @param interval the interval to set
     */
    public void setInterv(int interval) {
        this.interv = interval;
    }

    /**
     * @return the url
     */
    public String __getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

        @Override
    public void ensureUniqueness() {
         setCname("-" + RandomText.getNumberText()  + "- (" + __getUrl() +")");
    }

    /**
     * @return the isrequestCompression
     */
    public boolean __getIsrequestCompression() {
        return isrequestCompression;
    }

    /**
     * @param isrequestCompression the isrequestCompression to set
     */
    public void setIsrequestCompression(boolean isrequestCompression) {
        this.isrequestCompression = isrequestCompression;
    }

    /**
     * @return the isauthenticated
     */
    public boolean __getIsauthenticated() {
        return isauthenticated;
    }

    /**
     * @param isauthenticated the isauthenticated to set
     */
    public void setIsauthenticated(boolean isauthenticated) {
        this.isauthenticated = isauthenticated;
    }

    /**
     * @return the username
     */
    public String __getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String __getPassw() {
        return passw;
    }

    /**
     * @param password the password to set
     */
    public void setPassw(String password) {
        this.passw = password;
    }



}
