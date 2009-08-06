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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Groups;
import mpv5.ui.panels.MPControlPanel;

/**
 *
 *  
 */
public class ProductGroup extends DatabaseObject {

    private String description = "";
    private String defaults = "";

    public ProductGroup() {
        context.setDbIdentity(Context.IDENTITY_PGROUPS);
        context.setIdentityClass(this.getClass());
    }

    public ProductGroup(String name) {
        context.setDbIdentity(Context.IDENTITY_PGROUPS);
        context.setIdentityClass(this.getClass());
        cname = name;
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
     * @return the defaultvalue
     */
    public String __getDefaults() {
        return defaults;
    }

    /**
     * @param defaultvalue the defaultvalue to set
     */
    public void setDefaults(String defaultvalue) {
        this.defaults = defaultvalue;
    }

    @Override
    public String toString() {
        return __getCName();
    }

    @Override
    public boolean delete() {
        try {
            ArrayList<ProductGroup> childs = DatabaseObject.getReferencedObjects(this, Context.getProductGroup());
            for (int i = 0; i < childs.size(); i++) {
                DatabaseObject databaseObject = childs.get(i);
                if (!databaseObject.delete()) {
                    return false;
                }
            }
        } catch (NodataFoundException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(ProductGroup.class.getName()).log(Level.SEVERE, null, ex);
        }
        return super.delete();
    }

    @Override
    public JComponent getView() {
//       MPControlPanel p = (MPControlPanel) MPControlPanel.instanceOf();
//       p.openDetails(new ControlPanel_Groups(this));
//        return new ControlPanel_Groups(this);
        return null;
    }

     @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
    }
}
