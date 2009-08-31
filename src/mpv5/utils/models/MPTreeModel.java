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
package mpv5.utils.models;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Contact;
import mpv5.db.objects.FileToContact;
import mpv5.db.objects.FileToItem;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;

import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
import mpv5.utils.images.MPIcon;

/**
 *
 */
public class MPTreeModel extends DefaultTreeModel {

    private static final long serialVersionUID = 1L;

    private synchronized static DefaultMutableTreeNode getGroupHierarchy(
            Group childGroup, DefaultMutableTreeNode childNode,
            HashMap<Integer, DefaultMutableTreeNode> groups,
            DefaultMutableTreeNode rootNode) throws NodataFoundException {
        groups.put(childGroup.__getIDS(), childNode);
        if (childGroup.__getGroupsids() > 0) {
            Group parent = (Group) DatabaseObject.getObject(Context.getGroup(), childGroup.__getGroupsids());
            DefaultMutableTreeNode gnode = new DefaultMutableTreeNode(parent);
            gnode.add(childNode);
            return getGroupHierarchy(parent, gnode, groups, rootNode);
        } else {
            
            rootNode.add(childNode);
            return childNode;
        }
    }

    /**
     * Generates a tree view of the contact including related items and files
     * @param rootNode
     */
    public MPTreeModel(Contact rootNode) {
        super(buildTreeFor(rootNode));
    }

    private static MutableTreeNode buildTreeFor(Contact obj) {
        HashMap<Integer, DefaultMutableTreeNode> groups = new HashMap<Integer, DefaultMutableTreeNode>();
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(obj);

        //build group hierarchy

        // Add related items
        ArrayList<DatabaseObject> items = null;

        try {
            items = DatabaseObject.getReferencedObjects(obj, Context.getItems(), DatabaseObject.getObject(Context.getItems()));

            for (int i = 0; i < items.size(); i++) {
                Item item = (Item) items.get(i);
                DefaultMutableTreeNode itemnode = new DefaultMutableTreeNode(item);

                if(!groups.containsKey(new Integer(item.__getGroupsids()))){
                    DatabaseObject g = DatabaseObject.getObject(Context.getGroup(),item.__getGroupsids());
                    getGroupHierarchy((Group) g, new DefaultMutableTreeNode(g), groups, rootNode);
                }

                // Add files to the items
                ArrayList<DatabaseObject> itemfiles = null;
                try {
                    itemfiles = DatabaseObject.getReferencedObjects(item, Context.getFilesToItems(), DatabaseObject.getObject(Context.getFilesToItems()));
                    for (int j = 0; j < itemfiles.size(); j++) {
                        FileToItem ifile = (FileToItem) itemfiles.get(j);
                        itemnode.add(new DefaultMutableTreeNode(ifile));
                    }
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }

                (groups.get(item.__getGroupsids())).add(itemnode);
            }
            rootNode.add(groups.get(new Integer(1)));
            // Add files to the contact
            ArrayList<DatabaseObject> contactFiles = null;
            try {
                contactFiles = DatabaseObject.getReferencedObjects(obj, Context.getFilesToContacts(), DatabaseObject.getObject(Context.getFilesToContacts()));

                DefaultMutableTreeNode ifil = new DefaultMutableTreeNode(Messages.FILES);
                for (int j = 0; j < contactFiles.size(); j++) {
                    FileToContact ifile = (FileToContact) contactFiles.get(j);
                    ifil.add(new DefaultMutableTreeNode(ifile));
                }
                rootNode.add(ifil);
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }
        return rootNode;
    }

    /**
     * The default renderer for trees containing {@link DatabaseObject}s
     * @return
     */
    public static DefaultTreeCellRenderer getRenderer() {
        return new DefaultTreeCellRenderer() {

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                try {
                    DatabaseObject obj = (DatabaseObject) node.getUserObject();
                    this.setIcon(obj.getIcon());
                } catch (Exception e) {
                    try {
                        if (node.isLeaf()) {
                            setIcon(new MPIcon("/mpv5/resources/images/22/folder_grey.png"));
                        } else {
                            setIcon(new MPIcon("/mpv5/resources/images/22/folder_green.png"));
                        }
                    } catch (Exception ef) {
                        setIcon(leafIcon);
                        Log.Debug(ef);
                    }
                }
                return this;
            }
        };
    }

    /**
     * The default {@link MouseListener} for trees containing {@link DatabaseObject}s
     * @param tree 
     * @return
     */
    public static MouseListener getDefaultTreeListener(final JTree tree) {
        return new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                evt.consume();
                if (evt.getClickCount() > 1) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (node != null) {
                        if (node.getUserObject() instanceof DatabaseObject) {
                            MPView.identifierView.addTab((DatabaseObject) node.getUserObject());
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };
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
                MPView.setWaiting(true);
                node1 = addToParents(node1, data);

            } catch (Exception e) {
                Log.Debug(e);
            } finally {
                MPView.setWaiting(false);
            }
        }
        DefaultTreeModel model = new DefaultTreeModel(node1);
        return model;
    }

    @SuppressWarnings("unchecked")
    private static <T extends DatabaseObject> DefaultMutableTreeNode addToParents(DefaultMutableTreeNode firstnode, ArrayList<T> dobjlist) {

//        Log.Debug(ArrayUtilities.class, "Parent Node: " + firstnode);
        for (int i = 0; i < dobjlist.size(); i++) {
            T dobj = dobjlist.get(i);
//            Log.Debug(ArrayUtilities.class, "Node: " + dobj);

            if (dobj.__getGroupsids() <= 0 && firstnode.isRoot()) {
//                Log.Debug(ArrayUtilities.class, "Node is root child, adding it to root and removing it from the list.");
                firstnode.add(new DefaultMutableTreeNode(dobj));
                dobjlist.remove(dobj);//First level groups
                i--;
            } else {
                int parentid = dobj.__getGroupsids();
                if (((T) firstnode.getUserObject()).__getIDS().intValue() == parentid) {
//                    Log.Debug(ArrayUtilities.class, "Node is child of parentnode, adding and removing it from the list.");
                    firstnode.add(new DefaultMutableTreeNode(dobj));
                    dobjlist.remove(dobj);
                    i--;
                } else {
//                    Log.Debug(ArrayUtilities.class, "Node is no child of parentnode, iterating over the parent node..");
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
