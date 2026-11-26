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
package mpv5.ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Konsole;
import mpv5.ui.modern.ModernTheme;

/**
 * Standalone tab wrapper for the unified SQL Console &amp; Query workbench
 * ({@link ControlPanel_Konsole}). Implements {@link Scrollable} so the tab
 * follows the viewport size and internal scroll panes handle table/editor
 * scrolling while the bottom action bar (including Run) stays pinned in view.
 */
public class QueryPanel extends JPanel implements Scrollable {

    private static final long serialVersionUID = 2L;
    private static final int MIN_WIDTH = 520;
    private static final int MIN_HEIGHT = 360;

    public QueryPanel() {
        setLayout(new BorderLayout());
        add(new ControlPanel_Konsole(), BorderLayout.CENTER);
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
        return ModernTheme.px(160);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getParent() instanceof JViewport && getParent().getWidth() >= ModernTheme.px(MIN_WIDTH);
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return getParent() instanceof JViewport && getParent().getHeight() >= ModernTheme.px(MIN_HEIGHT);
    }
}
