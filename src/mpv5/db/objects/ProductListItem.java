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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.logging.Log;

public class ProductListItem extends DatabaseObject {

    private String description = "";
    private int productsids;

    public ProductListItem() {
        context.setDbIdentity(Context.IDENTITY_ITEMSLIST);
        context.setIdentityClass(ProductListItem.class);
    }

    @Override
    public JComponent getView() {
        throw new UnsupportedOperationException("Not supported yet.");
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

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
    }

    /**
     * @return the productsids
     */
    public int __getProductsids() {
        return productsids;
    }

    /**
     * @param productsids the productsids to set
     */
    public void setProductsids(int productsids) {
        this.productsids = productsids;
    }

    /**
     * Get the items of this list
     * @param listname
     * @return
     * @throws NodataFoundException
     */
    public static List<ProductListItem> getList(String listname) throws NodataFoundException {
        QueryCriteria c = new QueryCriteria("cname", listname);
        ArrayList<ProductListItem> data = getObjects(new ProductListItem(), c);
        return data;
    }

    /**
     * Delete a whole list (all its entries)
     * @param listname
     */
    public static void deleteList(String listname) {
        QueryCriteria c = new QueryCriteria("cname", listname);
        try {
            ArrayList<ProductListItem> data = getObjects(new ProductListItem(), c);
            for (int i = 0; i < data.size(); i++) {
                ProductListItem productListItem = data.get(i);
                productListItem.delete();
            }
        } catch (NodataFoundException ex) {
            Log.Debug(ProductListItem.class, ex.getMessage());
        }
    }
}
