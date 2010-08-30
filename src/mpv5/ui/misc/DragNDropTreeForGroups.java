package mpv5.ui.misc;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Enumeration;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Group;
import mpv5.db.objects.Product;
import mpv5.db.objects.ProductGroup;
import mpv5.logging.Log;
import mpv5.ui.panels.ListPanel;

////////////////////////////////////////////////////////////////////////////////
/**
 * Valid for {@link Group} and {@link ProductGroup} trees
 * @author andreas.weber
 */
public final class DragNDropTreeForGroups extends JTree {

    /**
     * If the tree nodes are ordinary {@link Group}s (any {@link DatabaseObject} can be dropped on).
     */
    public static final int TYPE_GROUP = 0;
    /**
     * If the tree nodes are {@link ProductGroup}s (only {@link Product}s can be dropped on).
     */
    public static final int TYPE_PRODUCT_GROUP = 1;
    private final int type;
    private ListPanel containerToNotify;

    /**
     * Creates anew tree for the given group type
     * @param type
     */
    public DragNDropTreeForGroups(int type) {
        super();
//        setDragEnabled(true);
        setDropMode(DropMode.ON_OR_INSERT);
//        setDropTarget(new DropTarget());
        setTransferHandler(new TreeTransferHandler(type));
        getSelectionModel().setSelectionMode(
                TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        expandTree();
        this.type = type;
    }

    /**
     * Expand the tree
     */
    public void expandTree() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();
        Enumeration e = root.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) e.nextElement();
            if (node.isLeaf()) {
                continue;
            }
            int row = getRowForPath(new TreePath(node.getPath()));
            expandRow(row);
        }
    }

    /**
     * @return the containerToNotify
     */
    public ListPanel getContainerToNotify() {
        return containerToNotify;
    }

    /**
     * @param containerToNotify the containerToNotify to set
     */
    public void setContainerToNotify(ListPanel containerToNotify) {
        this.containerToNotify = containerToNotify;
        ((TreeTransferHandler) getTransferHandler()).setContainerToNotify(containerToNotify);
    }
}

class TreeTransferHandler extends TransferHandler {

    private DataFlavor nodesFlavor;
    private DataFlavor[] flavors = new DataFlavor[2];
    private final int type;
    private ListPanel containerToNotify;

    public TreeTransferHandler(int type) {
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType
                    + ";class=\""
                    + javax.swing.tree.DefaultMutableTreeNode[].class.getName()
                    + "\"";
            nodesFlavor = new DataFlavor(mimeType);
            flavors[0] = nodesFlavor;
            flavors[1] = DatabaseObjectTransferable.FLAVOR;
        } catch (ClassNotFoundException e) {
        }
        this.type = type;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {

        if (!support.isDrop()) {
            return false;
        }
        support.setShowDropLocation(true);
        if (support.isDataFlavorSupported(flavors[0]) || support.isDataFlavorSupported(flavors[1])) {
            return true;
        }

        return false;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        return null;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        boolean result = false;
        if (!canImport(support)) {
            return result;
        }
        try {
            DatabaseObject[] movedObjs = new DatabaseObject[0];
            try {
                Object o = support.getTransferable().getTransferData(DatabaseObjectTransferable.FLAVOR);
                movedObjs = (DatabaseObject[]) o;
            } catch (Exception ex) {
                Log.Debug(ex);
            }

            JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
            TreePath dest = dl.getPath();
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) dest.getLastPathComponent();
            JTree tree = (JTree) support.getComponent();
            tree.setSelectionPath(dest);

            for (int i = 0; i < movedObjs.length; i++) {

                DatabaseObject obj = movedObjs[i];
                DatabaseObject par = (DatabaseObject) parentNode.getUserObject();

                if (type == DragNDropTreeForGroups.TYPE_PRODUCT_GROUP) {
                    if (dropPGroup(par, obj)) {
                        result = true;
                    } else {
                        result = false;
                    }
                } else {
                    if (dropGroup(par, obj)) {
                        result = true;
                    } else {
                        result = false;
                    }
                }
            }
        } catch (Exception e) {
            Log.Debug(e);
        }

        if (containerToNotify != null) {
            containerToNotify.refresh();
        }

        return result;
    }

    private boolean dropPGroup(DatabaseObject par, DatabaseObject obj) {

        if (par instanceof ProductGroup) {
            if (obj instanceof ProductGroup) {
                ((ProductGroup) obj).setProductgroupsids(par.__getIDS());
                return obj.save(true);
            } else if (obj instanceof Product) {
                ((Product) obj).setProductgroupsids(par.__getIDS());
                return obj.save(true);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean dropGroup(DatabaseObject par, DatabaseObject obj) {
        if (par instanceof Group) {
            if (obj instanceof Group) {
                ((Group) obj).setGroupsids(par.__getIDS());
                return obj.save(true);
            } else {
                (obj).setGroupsids(par.__getIDS());
                return obj.save(true);
            }
        } else {
            return false;
        }
    }

    protected void setContainerToNotify(ListPanel containerToNotify) {
        this.containerToNotify = containerToNotify;
    }
}
