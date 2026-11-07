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
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * Lays out its children (usually {@link Card}s) in as many equal columns as
 * fit the width: one column on narrow windows, two or more on wide ones. Put
 * it into a JScrollPane; it follows the width of the viewport and scrolls
 * vertically.
 */
public class ResponsiveGrid extends JPanel implements Scrollable {

    private static final long serialVersionUID = 1L;

    /**
     * @param minColumnWidth minimal width of a column at 12pt default font
     * @param maxColumns
     */
    public ResponsiveGrid(int minColumnWidth, int maxColumns) {
        super(new Layout(ModernTheme.px(minColumnWidth), maxColumns, ModernTheme.px(12)));
        setOpaque(false);
        setBorder(ModernTheme.padding(12, 12));
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return ModernTheme.px(16);
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(visibleRect.height - ModernTheme.px(32), ModernTheme.px(16));
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return getParent() != null && getParent().getHeight() > getPreferredSize().height;
    }

    private static final class Layout implements LayoutManager {

        private final int minColumnWidth;
        private final int maxColumns;
        private final int gap;

        Layout(int minColumnWidth, int maxColumns, int gap) {
            this.minColumnWidth = minColumnWidth;
            this.maxColumns = maxColumns;
            this.gap = gap;
        }

        private int columns(Container parent, int width) {
            Insets in = parent.getInsets();
            int w = width - in.left - in.right;
            int cols = Math.max(1, (w + gap) / (minColumnWidth + gap));
            return Math.min(Math.min(cols, maxColumns), Math.max(1, visibleCount(parent)));
        }

        private static int visibleCount(Container parent) {
            int n = 0;
            for (Component c : parent.getComponents()) {
                if (c.isVisible()) {
                    n++;
                }
            }
            return n;
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        private int width(Container parent) {
            //the width of the viewport if the grid is in a scroll pane
            int w = parent.getWidth();
            if (w <= 0 && parent.getParent() != null) {
                w = parent.getParent().getWidth();
            }
            return w;
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Insets in = parent.getInsets();
            int w = width(parent);
            int cols = w > 0 ? columns(parent, w) : Math.min(2, maxColumns);
            int colWidth = w > 0 ? (w - in.left - in.right - gap * (cols - 1)) / cols : minColumnWidth;
            int height = 0;
            int rowHeight = 0;
            int i = 0;
            for (Component c : parent.getComponents()) {
                if (!c.isVisible()) {
                    continue;
                }
                rowHeight = Math.max(rowHeight, heightFor(c, colWidth));
                if (++i % cols == 0) {
                    height += rowHeight + gap;
                    rowHeight = 0;
                }
            }
            height += rowHeight;
            if (rowHeight == 0 && height > 0) {
                height -= gap;
            }
            int prefWidth = w > 0 ? w : cols * minColumnWidth + (cols - 1) * gap + in.left + in.right;
            return new Dimension(prefWidth, height + in.top + in.bottom);
        }

        private static int heightFor(Component c, int width) {
            //let wrapping components compute their height for the given width
            Dimension old = c.getSize();
            if (old.width != width) {
                c.setSize(width, Math.max(old.height, 1));
            }
            return c.getPreferredSize().height;
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            Insets in = parent.getInsets();
            return new Dimension(minColumnWidth + in.left + in.right, in.top + in.bottom);
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets in = parent.getInsets();
            int w = parent.getWidth();
            int cols = columns(parent, w);
            int colWidth = Math.max(1, (w - in.left - in.right - gap * (cols - 1)) / cols);
            java.util.List<Component> visible = new java.util.ArrayList<Component>();
            for (Component c : parent.getComponents()) {
                if (c.isVisible()) {
                    visible.add(c);
                }
            }
            int y = in.top;
            for (int start = 0; start < visible.size(); start += cols) {
                int rowHeight = 0;
                int end = Math.min(start + cols, visible.size());
                for (int i = start; i < end; i++) {
                    rowHeight = Math.max(rowHeight, heightFor(visible.get(i), colWidth));
                }
                for (int i = start; i < end; i++) {
                    int x = in.left + (i - start) * (colWidth + gap);
                    //cards of a row get the same height, so their edges line up
                    visible.get(i).setBounds(x, y, colWidth, rowHeight);
                }
                y += rowHeight + gap;
            }
        }
    }
}
