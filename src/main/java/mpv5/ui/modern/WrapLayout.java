/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.modern;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * A FlowLayout which wraps its components into further lines when the width
 * is not sufficient, and reports the height of all lines (FlowLayout itself
 * always asks for one line).
 */
public class WrapLayout extends FlowLayout {

    private static final long serialVersionUID = 1L;

    /**
     * @param align FlowLayout.LEFT ...
     * @param hgap
     * @param vgap
     */
    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension d = layoutSize(target, false);
        d.width -= getHgap() + 1;
        return d;
    }

    /**
     * The preferred size is computed with the width of the last layout; when
     * the width changes (e.g. the window is made smaller), the number of
     * lines changes too, so the container is laid out once more with the new
     * width.
     */
    @Override
    public void layoutContainer(Container target) {
        super.layoutContainer(target);
        if (!(target instanceof javax.swing.JComponent)) {
            return;
        }
        final Dimension preferred = preferredLayoutSize(target);
        Integer last = (Integer) ((javax.swing.JComponent) target).getClientProperty(LAST_HEIGHT);
        if (last == null || last != preferred.height) {
            ((javax.swing.JComponent) target).putClientProperty(LAST_HEIGHT, preferred.height);
            if (last != null) {
                SwingUtilities.invokeLater(() -> {
                    Container top = target;
                    //revalidate up to the next scroll pane or window, so the parents take the new height
                    while (top.getParent() != null && !(top.getParent() instanceof javax.swing.JViewport)
                            && !(top.getParent() instanceof java.awt.Window)) {
                        top = top.getParent();
                    }
                    top.invalidate();
                    target.invalidate();
                    top.validate();
                    top.repaint();
                });
            }
        }
    }
    private static final String LAST_HEIGHT = "WrapLayout.lastHeight";

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;
            Container c = target;
            while (c.getSize().width == 0 && c.getParent() != null) {
                c = c.getParent();
            }
            targetWidth = c.getSize().width;
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }
            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;
            for (Component m : target.getComponents()) {
                if (!m.isVisible()) {
                    continue;
                }
                Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                if (rowWidth + d.width > maxWidth) {
                    addRow(dim, rowWidth, rowHeight);
                    rowWidth = 0;
                    rowHeight = 0;
                }
                if (rowWidth != 0) {
                    rowWidth += hgap;
                }
                rowWidth += d.width;
                rowHeight = Math.max(rowHeight, d.height);
            }
            addRow(dim, rowWidth, rowHeight);
            dim.width += insets.left + insets.right + hgap * 2;
            dim.height += insets.top + insets.bottom + vgap * 2;
            Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
            if (scrollPane != null && target.isValid()) {
                dim.width -= hgap + 1;
            }
            return dim;
        }
    }

    private void addRow(Dimension dim, int rowWidth, int rowHeight) {
        dim.width = Math.max(dim.width, rowWidth);
        if (dim.height > 0) {
            dim.height += getVgap();
        }
        dim.height += rowHeight;
    }
}
