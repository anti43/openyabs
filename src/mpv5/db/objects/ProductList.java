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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;

import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.logging.Log;
import mpv5.ui.panels.ProductListsPanel;
import mpv5.utils.images.MPIcon;

/**
 *
 * 
 */
public class ProductList extends DatabaseObject {

    private String description;
    private boolean asproduct = false;

    public ProductList() {
        setContext(Context.getProductlist());
    }

    @Override
    public JComponent getView() {
        return new ProductListsPanel();
    }

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return new MPIcon("/mpv5/resources/images/22/playlist.png");
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
     * @return the asproduct
     */
    public boolean __isAsproduct() {
        return asproduct;
    }

    /**
     * @param asproduct the asproduct to set
     */
    public void setAsproduct(boolean asproduct) {
        this.asproduct = asproduct;
    }

    @Override
    public boolean delete() {
        QueryCriteria c = new QueryCriteria("productlistsids", __getIDS());
        if (asproduct) {
            try {
                QueryHandler.instanceOf().clone(Context.getProduct()).delete(c);
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        }
        return super.delete();
    }

    @Override
    public boolean save(boolean b) {
        if (cname.length() == 0) {
            cname = "<unnamed>";
        }
        return super.save(b);
    }

}
