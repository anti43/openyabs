
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
package mpv5.utils.trees;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import java.awt.Font;
import java.awt.print.PrinterException;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 *
 *
 */
public class TreeFormat {

    /**
     * Expands a given node in a JTree.
     *
     * @param tree      The JTree to expand.
     * @param model     The TreeModel for tree.
     * @param node      The node within tree to expand.
     * @param row       The displayed row in tree that represents
     *                  node.
     * @param depth     The depth to which the tree should be expanded.
     *                  Zero will just expand node, a negative
     *                  value will fully expand the tree, and a positive
     *                  value will recursively expand the tree to that
     *                  depth relative to node.
     * @return
     */
    public static int expandJTreeNode(javax.swing.JTree tree, javax.swing.tree.TreeModel model, Object node, int row,
                                      int depth) {
        if ((node != null) &&!model.isLeaf(node)) {
            tree.expandRow(row);

            if (depth != 0) {
                for (int index = 0; (row + 1 < tree.getRowCount()) && (index < model.getChildCount(node)); index++) {
                    row++;

                    Object chil1 = model.getChild(node, index);

                    if (chil1 == null) {
                        break;
                    }

                    javax.swing.tree.TreePath path;

                    while ((path = tree.getPathForRow(row)) != null && (path.getLastPathComponent() != chil1)) {
                        row++;
                    }

                    if (path == null) {
                        break;
                    }

                    row = expandJTreeNode(tree, model, chil1, row, depth - 1);
                }
            }
        }

        return row;
    }    // expandJTreeNode()

    /**
     * Expand the tree
     * @param tree
     */
    public static void expandTree(JTree tree) {
        int rc = 0;

        do {
            rc = tree.getRowCount();

            for (int x = rc; x >= 0; x--) {
                tree.expandRow(x);
            }
        } while (rc != tree.getRowCount());
    }

    /**
     * Print the tree
     * @param treeWidth
     * @param tree
     */
    public static void print(final int treeWidth, final JTree tree) {
        final int MAX_HEIGHT = 20000;
        JTable    printTree  = new JTable(0, 1);

        printTree.setFont(Font.decode(Font.MONOSPACED));
        printTree.setSize(treeWidth, MAX_HEIGHT);
        printTree.getColumnModel().getColumn(0).setWidth(treeWidth);
        printTree.setGridColor(Color.WHITE);

        DefaultTableModel printModel = (DefaultTableModel) printTree.getModel();
        StringBuffer      rowElement = new StringBuffer();

        for (int i = 0; i < tree.getRowCount(); i++) {
            TreePath path  = tree.getPathForRow(i);
            int      level = path.getPathCount();

            rowElement.delete(0, rowElement.length());

            for (int j = 0; j < level; j++) {
                rowElement.append("      ");
            }

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

            if (!node.isLeaf()) {
                rowElement.append(tree.isCollapsed(i)
                                  ? "+ "
                                  : "-> ");
            }

            rowElement.append(node.toString());

            Object[] rowData = new Object[] { rowElement.toString() };

            printModel.addRow(rowData);
        }

        try {
            printTree.print();    // since 1.5
        } catch (PrinterException ex) {
            ex.printStackTrace();
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
