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
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Groups;

/**
 *
 *  
 */
public class Group extends DatabaseObject {

    private String description = "";
    private String defaults = "";
    private String hierarchypath = "";
    public static String GROUPSEPARATOR = " - ";

    public Group() {
        context = Context.getGroup();
    }

    public Group(String name) {
        context.setDbIdentity(Context.IDENTITY_GROUPS);
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
            List<Group> childs = DatabaseObject.getReferencedObjects(this, Context.getGroup());
            for (int i = 0; i < childs.size(); i++) {
                DatabaseObject databaseObject = childs.get(i);
                if (!databaseObject.delete()) {
                    return false;
                }
            }
        } catch (NodataFoundException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(Group.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            return super.delete();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public JComponent getView() {
//       MPControlPanel p = (MPControlPanel) MPControlPanel.instanceOf();
//       p.openDetails(new ControlPanel_Groups(this));
        return new ControlPanel_Groups(this);
    }

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
    }

    /**
     * @return the hierarchypath
     */
    public String __getHierarchypath() {
        if (hierarchypath == null || hierarchypath.equals("")) {
            int intp = __getIDS();
            do {
                try {
                    Group p = (Group) getObject(Context.getGroup(), intp);
                    hierarchypath = Group.GROUPSEPARATOR + p + hierarchypath;
                    intp = p.__getGroupsids();
                } catch (NodataFoundException ex) {
                    break;
                }
            } while (intp >= 1);
            return hierarchypath.replaceFirst(Group.GROUPSEPARATOR, "");
        }
        return hierarchypath;
    }

    /**
     * @param hierarchypath the hierarchypath to set
     */
    public void setHierarchypath(String hierarchypath) {
        this.hierarchypath = hierarchypath;
    }

    /**
     * Find the children and sub-children of this group
     * @return
     * @throws NodataFoundException 
     */
    public List<Group> getChildGroups() throws NodataFoundException {
        List<Group> childs = DatabaseObject.getReferencedObjects(this, Context.getGroup());
        for (int i = 0; i < childs.size(); i++) {
            Group databaseObject = childs.get(i);
            childs.addAll(databaseObject.getChildGroups());
        }
        return childs;
    }

    /**
     * 
     * @return True if the group has no parent group
     */
    public boolean isRoot() {
        return __getGroupsids() == 0;
    }
}
