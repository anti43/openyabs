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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria2;
import mpv5.db.common.QueryParameter;
import mpv5.db.objects.Contact;
import mpv5.db.objects.FileToContact;
import mpv5.db.objects.FileToItem;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;

import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.panels.DataPanel;
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
//        System.err.println(childNode);
        if (childGroup.__getGroupsids() > 0 && !Objects.equals(childGroup.__getIDS(), Group.getDefault().__getIDS())) {
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
     *
     * @param rootNode
     * @param itemfilter
     */
    public MPTreeModel(Contact rootNode, Context ref, QueryCriteria2 itemfilter) {
        super(buildTreeFor(rootNode, ref, itemfilter));
    }

    private static MutableTreeNode buildTreeFor(Contact obj, Context ref, QueryCriteria2 itemfilter) {
        HashMap<Integer, DefaultMutableTreeNode> groups = new HashMap<Integer, DefaultMutableTreeNode>();
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(obj);

        //build group hierarchy

        // Add related items
        List<DatabaseObject> items = new ArrayList<>();
        try {
            if (itemfilter == null) {
                if (ref == null) {
                    try {
                        items.addAll(DatabaseObject.toObjectList(DatabaseObject.getReferencedObjects(obj, Context.getInvoice(), DatabaseObject.getObject(Context.getInvoice())), Context.getInvoice().getSampleObject()));
                    } catch (NodataFoundException nodataFoundException) {
                    }
                    try {
                        items.addAll(DatabaseObject.toObjectList(DatabaseObject.getReferencedObjects(obj, Context.getOrder(), DatabaseObject.getObject(Context.getOrder())), Context.getOrder().getSampleObject()));
                    } catch (NodataFoundException nodataFoundException) {
                    }
                    try {
                        items.addAll(DatabaseObject.toObjectList(DatabaseObject.getReferencedObjects(obj, Context.getOffer(), DatabaseObject.getObject(Context.getOffer())), Context.getOffer().getSampleObject()));
                    } catch (NodataFoundException nodataFoundException) {
                    }
                    try {
                        items.addAll(DatabaseObject.toObjectList(DatabaseObject.getReferencedObjects(obj, Context.getDelivery(), DatabaseObject.getObject(Context.getDelivery())), Context.getDelivery().getSampleObject()));
                    } catch (NodataFoundException nodataFoundException) {
                    }
                    try {
                        items.addAll(DatabaseObject.toObjectList(DatabaseObject.getReferencedObjects(obj, Context.getConfirmation(), DatabaseObject.getObject(Context.getConfirmation())), Context.getConfirmation().getSampleObject()));
                    } catch (NodataFoundException nodataFoundException) {
                    }
                } else {
                    items.addAll(DatabaseObject.toObjectList(DatabaseObject.getReferencedObjects(obj, ref, DatabaseObject.getObject(ref)), ref.getSampleObject()));
                }
            } else {
                List<QueryParameter> p = new ArrayList<QueryParameter>();
                p.add(new QueryParameter(ref, "invisible", 0, QueryParameter.EQUALS));
                p.add(new QueryParameter(ref, "contactsids", obj.__getIDS(), QueryParameter.EQUALS));
                itemfilter.setOrder("groupsids", true);
                itemfilter.and(p);
                items = DatabaseObject.getObjects(ref, itemfilter);
            }

            for (int i = 0; i < items.size(); i++) {
                DatabaseObject item = items.get(i);
                DefaultMutableTreeNode itemnode = new DefaultMutableTreeNode(item);

                if (!groups.containsKey(item.__getGroupsids())) {
                    DatabaseObject g = DatabaseObject.getObject(Context.getGroup(), item.__getGroupsids());
                    Log.Debug(MPTreeModel.class, g);
                    getGroupHierarchy((Group) g, new DefaultMutableTreeNode(g), groups, rootNode);
                }

                // Add files to the items
                List<DatabaseObject> itemfiles;
                try {
                    itemfiles = DatabaseObject.getReferencedObjects(item, Context.getFilesToItems(), DatabaseObject.getObject(Context.getFilesToItems()));
                    for (int j = 0; j < itemfiles.size(); j++) {
                        FileToItem ifile = (FileToItem) itemfiles.get(j);
                        itemnode.add(new DefaultMutableTreeNode(ifile));
                    }
                } catch (NodataFoundException ex) {
                    Log.Debug(MPTreeModel.class, ex.getMessage());
                }

                (groups.get(item.__getGroupsids())).add(itemnode);
            }
            if (groups.get(1) != null) {
                rootNode.add(groups.get(1));
            } else {
                Log.Debug(MPTreeModel.class, "No root group node found.");
            }
            // Add files to the contact
            List<DatabaseObject> contactFiles;
            try {
                contactFiles = DatabaseObject.getReferencedObjects(obj, Context.getFilesToContacts(), DatabaseObject.getObject(Context.getFilesToContacts()));

                DefaultMutableTreeNode ifil = new DefaultMutableTreeNode(Messages.FILES);
                for (int j = 0; j < contactFiles.size(); j++) {
                    FileToContact ifile = (FileToContact) contactFiles.get(j);
                    ifil.add(new DefaultMutableTreeNode(ifile));
                }
                rootNode.add(ifil);
            } catch (NodataFoundException ex) {
                Log.Debug(MPTreeModel.class, ex.getMessage());
            }
        } catch (NodataFoundException ex) {
            Log.Debug(MPTreeModel.class, ex.getMessage());
        }
        return rootNode;
    }

    /**
     * The default renderer for trees containing {@link DatabaseObject}s
     *
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
     * The default {@link MouseListener} for trees containing
     * {@link DatabaseObject}s
     *
     * @param tree
     * @return
     */
    public static MouseListener getDefaultTreeListener(final JTree tree, final Contact dataOwner) {
        return new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                evt.consume();
                if (evt.getClickCount() > 1) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (node != null) {
                        if (node.getUserObject() instanceof DatabaseObject) {
                            mpv5.YabsViewProxy.instance().getIdentifierView().addTab((DatabaseObject) node.getUserObject());
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup(e);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            private void popup(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem x = new JMenuItem("Add to new invoice");
                x.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        List<DatabaseObject> list = new ArrayList<>();
                        for (TreePath p : tree.getSelectionPaths()) {
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode) p.getLastPathComponent();
                            if (node != null && node.getUserObject() instanceof DatabaseObject) {
                                DatabaseObject dbo = (DatabaseObject) node.getUserObject();
                                list.add(dbo);
                            }
                        }
                        if (list.size() > 0) {
                            Item item = Context.getInvoice().getSampleObject();
                            item.setContact(dataOwner);
                            DataPanel tab = mpv5.YabsViewProxy.instance().getIdentifierView().addTab(item);
                            tab.paste(list.toArray(new DatabaseObject[0]));
                        }
                    }
                });
                menu.add(x);
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        };
    }

    /**
     * Reflects the given data in a tree model, builds grouping based on
     * {@link DatabaseObject#__getGroupsids()}
     *
     * @param <T>
     * @param rootNode
     * @return
     */
    public static <T extends DatabaseObject> DefaultTreeModel toTreeModel(T rootNode) {

        DefaultMutableTreeNode node1 = new DefaultMutableTreeNode(rootNode);
        try {
            mpv5.YabsViewProxy.instance().setWaiting(true);

            node1 = addChildren(node1);

        } catch (Exception e) {
            Log.Debug(e);
        } finally {
            mpv5.YabsViewProxy.instance().setWaiting(false);
        }

        DefaultTreeModel model = new DefaultTreeModel(node1);
        return model;
    }

    @SuppressWarnings("unchecked")
    private static <T extends DatabaseObject> DefaultMutableTreeNode addChildren(DefaultMutableTreeNode firstnode) {
        DatabaseObject dbo = (DatabaseObject) firstnode.getUserObject();
        QueryCriteria2 c = new QueryCriteria2();
        c.and(new QueryParameter(dbo.getContext(), "groupsids", dbo.__getIDS(), QueryParameter.EQUALS));
        try {
            ArrayList<DatabaseObject> l = DatabaseObject.getObjects(dbo.getContext(), c);
            for (DatabaseObject a : l) {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(a);
                firstnode.add(child);
                addChildren(child);
            }
        } catch (NodataFoundException ex) {
            //Logger.getLogger(MPTreeModel.class.getName()).log(Level.SEVERE, null, ex);
        }


        return firstnode;
    }
}
