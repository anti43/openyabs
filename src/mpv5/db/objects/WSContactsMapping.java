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

/**
 * Maps Contacts to WebShop Contacts
 */
public class WSContactsMapping extends DatabaseObject {

    private int webshopsids;
    private int contactsids;
    private String wscontact = "";

    /**
     * Create a new mapping
     */
    public WSContactsMapping() {
        context.setIdentityClass(this.getClass());
        context.setDbIdentity(Context.IDENTITY_WSMAPPING);
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
     * @return the webshopsids
     */
    public int __getWebshopsids() {
        return webshopsids;
    }

    /**
     * @param webshopsids the webshopsids to set
     */
    public void setWebshopsids(int webshopsids) {
        this.webshopsids = webshopsids;
    }

    /**
     * @return the contactsids
     */
    public int __getContactsids() {
        return contactsids;
    }

    /**
     * @param contactsids the contactsids to set
     */
    public void setContactsids(int contactsids) {
        this.contactsids = contactsids;
    }

    /**
     * @return the wscontact
     */
    public String __getWscontact() {
        return wscontact;
    }

    /**
     * @param wscontact the wscontact to set
     */
    public void setWscontact(String wscontact) {
        this.wscontact = wscontact;
    }
}
