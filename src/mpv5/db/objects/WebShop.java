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

import java.net.URL;
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
    private int interval;
    private String url = "";

    public WebShop() {
        context.setIdentityClass(this.getClass());
        context.setDbIdentity(Context.IDENTITY_WEBSHOPS);
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
    public int __getInterval() {
        return interval;
    }

    /**
     * @param interval the interval to set
     */
    public void setInterval(int interval) {
        this.interval = interval;
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
         setCName("-" + RandomText.getNumberText()  + "- (" + __getUrl() +")");
    }
}
