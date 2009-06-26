/*
 *  This file is part of MP.
 *
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.models;

import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import mpv5.db.common.DatabaseObject;
import mpv5.logging.Log;
import mpv5.ui.frames.MPV5View;
import mpv5.utils.arrays.ArrayUtilities;

/**
 *
 */
public class MPTreeModel extends DefaultTreeModel {

    private static final long serialVersionUID = 1L;

    public MPTreeModel(DatabaseObject rootNode) {
        super(new DefaultMutableTreeNode(rootNode));
        buildTree();
    }

    private void buildTree() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Reflects the given data in a tree model, builds grouping based on {@link DatabaseObject#__getGroupsids()}
     * @param <T>
     * @param data
     * @param rootNode
     * @return
     */
    public static <T extends DatabaseObject> DefaultTreeModel toTreeModel(ArrayList<T> data, T rootNode) {

        DefaultMutableTreeNode node1 = null;
        if (data.size() > 0) {
            node1 = new DefaultMutableTreeNode(rootNode);
            data.remove(rootNode);//remove root if in list
            try {
                MPV5View.setWaiting(true);
                node1 = addToParents(node1, data);

            } catch (Exception e) {
                Log.Debug(e);
            } finally {
                MPV5View.setWaiting(false);
            }
        }
        DefaultTreeModel model = new DefaultTreeModel(node1);
        return model;
    }

    @SuppressWarnings("unchecked")
    private static <T extends DatabaseObject> DefaultMutableTreeNode addToParents(DefaultMutableTreeNode firstnode, ArrayList<T> dobjlist) {

        Log.Debug(ArrayUtilities.class, "Parent Node: " + firstnode);
        for (int i = 0; i < dobjlist.size(); i++) {
            T dobj = dobjlist.get(i);
            Log.Debug(ArrayUtilities.class, "Node: " + dobj);

            if (dobj.__getGroupsids() <= 0 && firstnode.isRoot()) {
                Log.Debug(ArrayUtilities.class, "Node is root child, adding it to root and removing it from the list.");
                firstnode.add(new DefaultMutableTreeNode(dobj));
                dobjlist.remove(dobj);//First level groups
                i--;
            } else {
                int parentid = dobj.__getGroupsids();
                if (((T) firstnode.getUserObject()).__getIDS().intValue() == parentid) {
                    Log.Debug(ArrayUtilities.class, "Node is child of parentnode, adding and removing it from the list.");
                    firstnode.add(new DefaultMutableTreeNode(dobj));
                    dobjlist.remove(dobj);
                    i--;
                } else {
                    Log.Debug(ArrayUtilities.class, "Node is no child of parentnode, iterating over the parent node..");
                    @SuppressWarnings("unchecked")
                    Enumeration<DefaultMutableTreeNode> nodes = firstnode.children();
                    while (nodes.hasMoreElements()) {
                        addToParents(nodes.nextElement(), dobjlist);
                    }
                }
            }
        }
        return firstnode;
    }
}
