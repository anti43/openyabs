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
package mpv5.utils.trees;




import javax.swing.JTree;


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
    public static int expandJTreeNode(javax.swing.JTree tree,
            javax.swing.tree.TreeModel model,
            Object node, int row, int depth) {
        if (node != null && !model.isLeaf(node)) {
            tree.expandRow(row);
            if (depth != 0) {
                for (int index = 0;
                        row + 1 < tree.getRowCount() &&
                        index < model.getChildCount(node);
                        index++) {
                    row++;
                    Object chil1 = model.getChild(node, index);
                    if (chil1 == null) {
                        break;
                    }
                    javax.swing.tree.TreePath path;
                    while ((path = tree.getPathForRow(row)) != null &&
                            path.getLastPathComponent() != chil1) {
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
    } // expandJTreeNode()


    public static void expandTree(JTree tree) {
        int rc = 0;
        do {
            rc = tree.getRowCount();
            for (int x = rc; x >= 0; x--) {
                tree.expandRow(x);
            }
        } while (rc != tree.getRowCount());
    }
}
