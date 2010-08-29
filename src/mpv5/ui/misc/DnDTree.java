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
package mpv5.ui.misc;

/*
Java Swing, 2nd Edition
By Marc Loy, Robert Eckstein, Dave Wood, James Elliott, Brian Cole
ISBN: 0-596-00408-7
Publisher: O'Reilly 
 */
// TreeDragTest.java
//
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.IOException;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author andreas
 */
public class DnDTree extends JTree {

    TreeDragSource ds;
    TreeDropTarget dt;

    public DnDTree() {
        ds = new TreeDragSource(this, DnDConstants.ACTION_COPY_OR_MOVE);
        dt = new TreeDropTarget(this);
        setDragEnabled(true);
//        setDropTarget(dt);
    }
}

class TreeDragSource implements DragSourceListener, DragGestureListener {

    DragSource source;
    DragGestureRecognizer recognizer;
    TransferableTreeNode transferable;
    DefaultMutableTreeNode oldNode;
    JTree sourceTree;

    public TreeDragSource(JTree tree, int actions) {
        sourceTree = tree;
        source = new DragSource();
        recognizer = source.createDefaultDragGestureRecognizer(sourceTree,
                actions, this);
    }

    /*
     * Drag Gesture Handler
     */
    public void dragGestureRecognized(DragGestureEvent dge) {
        TreePath path = sourceTree.getSelectionPath();
        if ((path == null) || (path.getPathCount() <= 1)) {
            // We can't move the root node or an empty selection
            return;
        }
        oldNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        transferable = new TransferableTreeNode(path);
        try {
            source.startDrag(dge, DragSource.DefaultMoveNoDrop, transferable, this);
        } catch (InvalidDnDOperationException invalidDnDOperationException) {
        }

        // If you support dropping the node anywhere, you should probably
        // start with a valid move cursor:
        //source.startDrag(dge, DragSource.DefaultMoveDrop, transferable,
        // this);
    }

    /*
     * Drag Event Handlers
     */
    public void dragEnter(DragSourceDragEvent dsde) {
    }

    public void dragExit(DragSourceEvent dse) {
    }

    public void dragOver(DragSourceDragEvent dsde) {
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
        System.out.println("Action: " + dsde.getDropAction());
        System.out.println("Target Action: " + dsde.getTargetActions());
        System.out.println("User Action: " + dsde.getUserAction());
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
        /*
         * to support move or copy, we have to check which occurred:
         */
        System.out.println("Drop Action: " + dsde.getDropAction());
        if (dsde.getDropSuccess()
                && (dsde.getDropAction() == DnDConstants.ACTION_MOVE)) {
            ((DefaultTreeModel) sourceTree.getModel()).removeNodeFromParent(oldNode);
        }

        /*
         * to support move only... if (dsde.getDropSuccess()) {
         * ((DefaultTreeModel)sourceTree.getModel()).removeNodeFromParent(oldNode); }
         */
    }
}

//TreeDropTarget.java
//A quick DropTarget that's looking for drops from draggable JTrees.
//
class TreeDropTarget implements DropTargetListener {

    DropTarget target;
    JTree targetTree;

    public TreeDropTarget(JTree tree) {
        targetTree = tree;
        target = new DropTarget(targetTree, this);
    }

    /*
     * Drop Event Handlers
     */
    private TreeNode getNodeForEvent(DropTargetDragEvent dtde) {
        Point p = dtde.getLocation();
        DropTargetContext dtc = dtde.getDropTargetContext();
        JTree tree = (JTree) dtc.getComponent();
        TreePath path = tree.getClosestPathForLocation(p.x, p.y);
        return (TreeNode) path.getLastPathComponent();
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        TreeNode node = getNodeForEvent(dtde);
        if (node.isLeaf()) {
            dtde.rejectDrag();
        } else {
            // start by supporting move operations
            //dtde.acceptDrag(DnDConstants.ACTION_MOVE);
            dtde.acceptDrag(dtde.getDropAction());
        }
    }

    public void dragOver(DropTargetDragEvent dtde) {
        TreeNode node = getNodeForEvent(dtde);
        if (node.isLeaf()) {
            dtde.rejectDrag();
        } else {
            // start by supporting move operations
            //dtde.acceptDrag(DnDConstants.ACTION_MOVE);
            dtde.acceptDrag(dtde.getDropAction());
        }
    }

    public void dragExit(DropTargetEvent dte) {
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public void drop(DropTargetDropEvent dtde) {
        Point pt = dtde.getLocation();
        DropTargetContext dtc = dtde.getDropTargetContext();
        JTree tree = (JTree) dtc.getComponent();
        TreePath parentpath = tree.getClosestPathForLocation(pt.x, pt.y);
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) parentpath.getLastPathComponent();
        if (parent.isLeaf()) {
            dtde.rejectDrop();
            return;
        }

        try {
            Transferable tr = dtde.getTransferable();
            DataFlavor[] flavors = tr.getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                if (tr.isDataFlavorSupported(flavors[i])) {
                    try {
                        dtde.acceptDrop(dtde.getDropAction());
                        TreePath p = (TreePath) tr.getTransferData(flavors[i]);
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) p.getLastPathComponent();
                        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                        model.insertNodeInto(node, parent, 0);
                        dtde.dropComplete(true);
                    } catch (UnsupportedFlavorException unsupportedFlavorException) {
                    } catch (IOException iOException) {
                    }
                    return;
                }
            }
            dtde.rejectDrop();
        } catch (Exception e) {
            e.printStackTrace();
            dtde.rejectDrop();
        }
    }
}

//TransferableTreeNode.java
//A Transferable TreePath to be used with Drag & Drop applications.
//
class TransferableTreeNode implements Transferable {

    public static DataFlavor TREE_PATH_FLAVOR = new DataFlavor(TreePath.class,
            "Tree Path");
    DataFlavor flavors[] = {TREE_PATH_FLAVOR};
    TreePath path;

    public TransferableTreeNode(TreePath tp) {
        path = tp;
    }

    public synchronized DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return (flavor.getRepresentationClass() == TreePath.class);
    }

    public synchronized Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            return (Object) path;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
