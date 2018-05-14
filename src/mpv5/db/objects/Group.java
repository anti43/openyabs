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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import static mpv5.db.common.DatabaseObject.getObject;
import mpv5.db.common.NodataFoundException;
import static mpv5.db.objects.ProductGroup.getDefault;
import mpv5.logging.Log;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Groups;

/**
 *
 *
 */
public class Group extends DatabaseObject {

    /**
     * Returns an empty "sample" Object of the specified <code>Context</code>
     * type
     *
     * @param context
     * @return
     */
    public static Group getGroupOrDefault(int id) {
        Context c = Context.getGroup();
        try {
            return (Group) getObject(c, id);
        } catch (NodataFoundException ex) {
            Logger.getLogger(Group.class.getName()).log(Level.SEVERE, null, ex);
            return getDefault();
        }
    }

    /**
     * Returns 'All Group' or the first group found if 'All group' is gone, and
     * if all are deleted, creates a default group
     *
     * @return A group, never NULL
     */
    public static Group getDefault() {
        try {
            return (Group) DatabaseObject.getObject(Context.getGroup(), 1);
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
            try {
                return (Group) DatabaseObject.getObjects(Context.getGroup()).get(0);
            } catch (NodataFoundException nodataFoundException) {
                Group g = new Group();
                g.setCname("All Group#");
                g.save();
                return g;
            }
        }
    }
    private String description = "";
    private String defaults = "";
    private String hierarchypath = "";
    public static String GROUPSEPARATOR = "/";

    public Group() {
        setContext(Context.getGroup());
    }

    public Group(String name) {
        setContext(Context.getGroup());
        setCname(name);
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
        return __getCname();
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
        Set<Integer> used = new HashSet<Integer>();
        hierarchypath = "";
        int intp = __getIDS();
        boolean notUsed = used.add(intp);
        while (intp > 0 && notUsed) {
            try {
                ProductGroup p = (ProductGroup) getObject(Context.getProductGroup(), intp);
                hierarchypath = p + Group.GROUPSEPARATOR + hierarchypath;
                intp = p.__getProductgroupsids();
            } catch (NodataFoundException ex) {
                break;
            }
        }
        return hierarchypath;
    }

    /**
     * @param hierarchypath the hierarchypath to set
     */
    public void setHierarchypath(String hierarchypath) {
        this.hierarchypath = hierarchypath;
    }

    @Override
    public void setGroupsids(int a) {
        super.setGroupsids(a);
        hierarchypath = null;
    }

    /**
     * Find the children and sub-children of this group
     *
     * @return
     * @throws NodataFoundException
     */
    public List<Group> getChildGroups() throws NodataFoundException {
        List<Group> childs = DatabaseObject.getReferencedObjects(this, Context.getGroup());
        for (int i = 0; i < childs.size(); i++) {
            Group databaseObject = childs.get(i);
            childs.addAll(databaseObject.getChildGroups());
        }
        if (Log.LOGLEVEL_DEBUG == Log.getLoglevel()) {
            Log.Debug(this, childs);
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

    @Override
    public void onBeforeSave() {
        if (isRoot() || Objects.equals(getDefault().ids, ids)) {
            return;
        }
        try {
            Group parent = (Group) getObject(Context.getGroup(), __getGroupsids());
            if (!parent.__getHierarchypath().contains(getDefault().toString())) {
                //must be root of all
                setGroup(getDefault());
            }
            //all good
        } catch (NodataFoundException ex) {
            Logger.getLogger(ProductGroup.class.getName()).log(Level.SEVERE, null, ex);
            setGroup(getDefault());
        }
    }
}
