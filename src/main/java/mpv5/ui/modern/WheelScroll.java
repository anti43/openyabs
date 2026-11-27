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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseWheelEvent;
import javax.swing.BoundedRangeModel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * Makes mouse-wheel scrolling work smoothly across the whole application:
 * <ul>
 *   <li>Nested {@link JScrollPane}s (tables, text areas, lists, inner panels)
 *       forward wheel events to the enclosing {@link JScrollPane} whenever they
 *       have no scrollbar or have already reached the top/bottom limit.</li>
 *   <li>{@link JTabbedPane}s whose Look&amp;Feel registers a tab-strip wheel
 *       listener (e.g. FlatLaf's {@code FlatWheelTabScroller}) forward wheel
 *       events from their content area to the enclosing {@link JScrollPane}
 *       instead of swallowing them.</li>
 *   <li>Non-{@link javax.swing.Scrollable} {@link JScrollPane}s left at Swing's
 *       1-pixel default unit increment get a comfortable step size.</li>
 * </ul>
 */
public final class WheelScroll {

    private static boolean installed;
    private static boolean forwarding;

    private WheelScroll() {
    }

    /**
     * Installs the global AWT mouse-wheel listener once per JVM.
     */
    public static synchronized void install() {
        if (installed) {
            return;
        }
        installed = true;
        Toolkit.getDefaultToolkit().addAWTEventListener(WheelScroll::onEvent, AWTEvent.MOUSE_WHEEL_EVENT_MASK);
    }

    static void onEvent(AWTEvent event) {
        if (forwarding || !(event instanceof MouseWheelEvent)) {
            return;
        }
        MouseWheelEvent e = (MouseWheelEvent) event;
        if (e.isConsumed() || !(e.getSource() instanceof Component)) {
            return;
        }
        double rot = rotation(e);
        if (rot == 0.0) {
            return;
        }
        Component src = (Component) e.getSource();
        // Find nearest JScrollPane ancestor (including source if it is one)
        JScrollPane sp = (src instanceof JScrollPane) ? (JScrollPane) src : (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, src);
        if (sp != null) {
            ensureUnitIncrement(sp);
            JScrollPane target = findForwardTarget(sp, e, rot);
            if (target != null) {
                forward(sp, target, e);
            }
            return;
        }
        // Find nearest JTabbedPane ancestor (including source)
        JTabbedPane tp = (src instanceof JTabbedPane) ? (JTabbedPane) src : (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, src);
        if (tp != null) {
            Component selected = tp.getSelectedComponent();
            if (selected != null && selected.getBounds().contains(e.getPoint())) {
                JScrollPane target = findScrollableAncestor(tp, e, rot);
                if (target != null) {
                    forward(tp, target, e);
                }
            }
        }
    }

    private static double rotation(MouseWheelEvent e) {
        double precise = e.getPreciseWheelRotation();
        return precise != 0.0 ? precise : e.getWheelRotation();
    }

    private static JScrollPane findForwardTarget(JScrollPane sp, MouseWheelEvent e, double rot) {
        if (e.isShiftDown()) {
            if (sp.isWheelScrollingEnabled() && canScrollHorizontal(sp, rot)) {
                return null;
            }
            return findAncestor(sp, true, rot);
        }
        if (sp.isWheelScrollingEnabled() && canScrollVertical(sp, rot)) {
            return null;
        }
        // Prefer scrolling an enclosing pane vertically over sliding a wide inner table sideways
        // when the user rolls the vertical wheel without Shift.
        JScrollPane verticalAncestor = findAncestor(sp, false, rot);
        if (verticalAncestor != null) {
            return verticalAncestor;
        }
        if (sp.isWheelScrollingEnabled() && canScrollHorizontal(sp, rot)) {
            return null;
        }
        return findAncestor(sp, true, rot);
    }

    private static JScrollPane findScrollableAncestor(Component start, MouseWheelEvent e, double rot) {
        if (e.isShiftDown()) {
            return findAncestor(start, true, rot);
        }
        JScrollPane vertical = findAncestor(start, false, rot);
        return vertical != null ? vertical : findAncestor(start, true, rot);
    }

    private static JScrollPane findAncestor(Component start, boolean horizontal, double rot) {
        for (Container p = start.getParent(); p != null && !(p instanceof Window); p = p.getParent()) {
            if (p instanceof JScrollPane) {
                JScrollPane sp = (JScrollPane) p;
                if (sp.isWheelScrollingEnabled()
                        && (horizontal ? canScrollHorizontal(sp, rot) : canScrollVertical(sp, rot))) {
                    return sp;
                }
            }
        }
        return null;
    }

    static boolean canScrollVertical(JScrollPane sp, double rot) {
        JScrollBar bar = sp.getVerticalScrollBar();
        return bar != null && bar.isVisible() && bar.isEnabled() && canMove(bar, rot);
    }

    static boolean canScrollHorizontal(JScrollPane sp, double rot) {
        JScrollBar bar = sp.getHorizontalScrollBar();
        return bar != null && bar.isVisible() && bar.isEnabled() && canMove(bar, rot);
    }

    private static boolean canMove(JScrollBar bar, double rot) {
        BoundedRangeModel m = bar.getModel();
        int min = m.getMinimum();
        int max = m.getMaximum() - m.getExtent();
        if (max <= min) {
            return false;
        }
        int val = m.getValue();
        return rot < 0 ? val > min : val < max;
    }

    private static void ensureUnitIncrement(JScrollPane sp) {
        int step = ModernTheme.px(16);
        JScrollBar vsb = sp.getVerticalScrollBar();
        if (vsb != null && vsb.getUnitIncrement() <= 1) {
            vsb.setUnitIncrement(step);
        }
        JScrollBar hsb = sp.getHorizontalScrollBar();
        if (hsb != null && hsb.getUnitIncrement() <= 1) {
            hsb.setUnitIncrement(step);
        }
    }

    private static void forward(Component src, JScrollPane target, MouseWheelEvent e) {
        ensureUnitIncrement(target);
        e.consume();
        Point p = SwingUtilities.convertPoint(src, e.getX(), e.getY(), target);
        MouseWheelEvent forwarded = new MouseWheelEvent(
                target,
                e.getID(),
                e.getWhen(),
                e.getModifiersEx(),
                p.x,
                p.y,
                e.getXOnScreen(),
                e.getYOnScreen(),
                e.getClickCount(),
                e.isPopupTrigger(),
                e.getScrollType(),
                e.getScrollAmount(),
                e.getWheelRotation(),
                e.getPreciseWheelRotation());
        forwarding = true;
        try {
            target.dispatchEvent(forwarded);
        } finally {
            forwarding = false;
        }
    }
}
