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

/**
 *
 * 
 */
public class ProductList extends DatabaseObject {
    private String description;

    public ProductList() {
        context.setDbIdentity(Context.IDENTITY_PRODUCTSLIST);
        context.setIdentityClass(this.getClass());
    }

    @Override
    public JComponent getView() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
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
}
