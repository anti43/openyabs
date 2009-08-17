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

import java.util.List;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.utils.images.MPIcon;

/**
 * Maps Contacts to WebShop Contacts
 */
public class WSItemsMapping extends DatabaseObject {

     /**
     * Fetches a mapping from db
     * @param webShopID
      * @param itemsids
     * @return
     * @throws NodataFoundException
     */
    public static WSItemsMapping getMapping(int webShopID, int itemsids) throws NodataFoundException {
            QueryCriteria qs = new QueryCriteria();
            qs.add("webshopid", webShopID);
            qs.add("itemsids", itemsids);
            List old = DatabaseObject.getObjects(Context.getAddress(), qs);
            return (WSItemsMapping) old.get(0);
    }

    private int webshopsids;
    private int itemsids;
    private String wsitem = "";

    /**
     * Create a new mapping
     */
    public WSItemsMapping() {
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
     * @return the itemsids
     */
    public int __getItemsids() {
        return itemsids;
    }

    /**
     * @param itemsids the itemsids to set
     */
    public void setItemsids(int itemsids) {
        this.itemsids = itemsids;
    }

    /**
     * @return the wsitem
     */
    public String __getWsitem() {
        return wsitem;
    }

    /**
     * @param wsitem the wsitem to set
     */
    public void setWsitem(String wsitem) {
        this.wsitem = wsitem;
    }


}
