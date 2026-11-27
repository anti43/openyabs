package mpv5.ui.modern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WheelScrollTest {

    @BeforeAll
    static void setupLafAndHook() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
        SwingUtilities.invokeAndWait(() -> {
            try {
                UIManager.setLookAndFeel(new YabsLookAndFeel());
            } catch (Exception ignored) {
            }
            WheelScroll.install();
        });
    }

    private static void wheel(Component target, int x, int y, int rotation) {
        Window w = SwingUtilities.getWindowAncestor(target);
        Point p = SwingUtilities.convertPoint(target, x, y, w);
        MouseWheelEvent ev = new MouseWheelEvent(
                w,
                MouseEvent.MOUSE_WHEEL,
                System.currentTimeMillis(),
                0,
                p.x,
                p.y,
                0,
                false,
                MouseWheelEvent.WHEEL_UNIT_SCROLL,
                3,
                rotation);
        w.dispatchEvent(ev);
    }

    @Test
    void nestedNonScrollableTextAreaAndTableForwardToOuterScrollPane() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame("wheel-test");
            try {
                frame.setSize(600, 400);
                JPanel content = new JPanel(null);
                content.setPreferredSize(new Dimension(560, 1200));

                JTextArea ta = new JTextArea("short note");
                JScrollPane innerTa = new JScrollPane(ta);
                innerTa.setBounds(10, 10, 200, 120);
                content.add(innerTa);

                JTable tbl = new JTable(new Object[][]{{"1", "2"}}, new Object[]{"A", "B"});
                JScrollPane innerTbl = new JScrollPane(tbl);
                innerTbl.setBounds(230, 10, 200, 120);
                content.add(innerTbl);

                JScrollPane outer = new JScrollPane(content);
                frame.add(outer);
                frame.setVisible(true);
                frame.validate();

                // Default 1px unit increment is bumped automatically and wheel bubbles from textarea
                wheel(ta, 20, 20, 1);
                int afterTa = outer.getVerticalScrollBar().getValue();
                assertTrue(afterTa >= 16, "expected outer scroll pane to scroll over inner textarea, got " + afterTa);

                // Wheel over non-scrollable table also bubbles to outer scroll pane
                wheel(tbl, 20, 20, 1);
                int afterTbl = outer.getVerticalScrollBar().getValue();
                assertTrue(afterTbl > afterTa, "expected outer scroll pane to scroll further over inner table");
            } finally {
                frame.dispose();
            }
        });
    }

    @Test
    void scrollableInnerPaneScrollsItselfThenBubblesAtLimits() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame("wheel-nested-limits");
            try {
                frame.setSize(600, 400);
                JPanel content = new JPanel(null);
                content.setPreferredSize(new Dimension(560, 1200));

                StringBuilder lines = new StringBuilder();
                for (int i = 0; i < 40; i++) {
                    lines.append("Line ").append(i).append('\n');
                }
                JTextArea ta = new JTextArea(lines.toString());
                JScrollPane inner = new JScrollPane(ta);
                inner.setBounds(10, 10, 240, 120);
                content.add(inner);

                JScrollPane outer = new JScrollPane(content);
                outer.getVerticalScrollBar().setUnitIncrement(16);
                frame.add(outer);
                frame.setVisible(true);
                frame.validate();

                // Scrolling down from top scrolls the inner text area, not outer
                wheel(ta, 20, 20, 1);
                assertTrue(inner.getVerticalScrollBar().getValue() > 0, "inner should scroll down first");
                assertEquals(0, outer.getVerticalScrollBar().getValue(), "outer should stay at 0 while inner scrolls");

                // Jump inner to its bottom limit; scrolling down now bubbles to outer
                JScrollBar iv = inner.getVerticalScrollBar();
                iv.setValue(iv.getMaximum() - iv.getModel().getExtent());
                wheel(ta, 20, 20, 1);
                int outerDown = outer.getVerticalScrollBar().getValue();
                assertTrue(outerDown > 0, "outer should scroll down once inner reached bottom");

                // Jump inner back to top; scrolling up now bubbles to outer
                iv.setValue(0);
                wheel(ta, 20, 20, -1);
                assertTrue(outer.getVerticalScrollBar().getValue() < outerDown,
                        "outer should scroll up once inner is at top");
            } finally {
                frame.dispose();
            }
        });
    }

    @Test
    void flatTabbedPaneContentAreaForwardsToOuterScrollPane() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame("wheel-tabbedpane");
            try {
                frame.setSize(600, 400);
                JPanel content = new JPanel(null);
                content.setPreferredSize(new Dimension(560, 1200));

                JTabbedPane tp = new JTabbedPane();
                tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
                JPanel tabBody = new JPanel();
                tp.addTab("Tab 1", tabBody);
                tp.setBounds(10, 10, 300, 160);
                content.add(tp);

                JScrollPane outer = new JScrollPane(content);
                outer.getVerticalScrollBar().setUnitIncrement(16);
                frame.add(outer);
                frame.setVisible(true);
                frame.validate();

                wheel(tabBody, 50, 50, 1);
                assertTrue(outer.getVerticalScrollBar().getValue() > 0,
                        "wheel over JTabbedPane content area should scroll outer pane");
            } finally {
                frame.dispose();
            }
        });
    }

    @Test
    void wideInnerScrollPanePrefersVerticalAncestorWhenScrollingWithoutShift() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame("wheel-wide-inner");
            try {
                frame.setSize(600, 400);
                JPanel content = new JPanel(null);
                content.setPreferredSize(new Dimension(560, 1200));

                JPanel wideChild = new JPanel();
                wideChild.setPreferredSize(new Dimension(800, 60));
                JScrollPane inner = new JScrollPane(wideChild);
                inner.setBounds(10, 10, 240, 100);
                content.add(inner);

                JScrollPane outer = new JScrollPane(content);
                outer.getVerticalScrollBar().setUnitIncrement(16);
                frame.add(outer);
                frame.setVisible(true);
                frame.validate();

                wheel(wideChild, 20, 20, 1);
                assertTrue(outer.getVerticalScrollBar().getValue() > 0,
                        "vertical wheel without Shift should scroll vertical ancestor");
                assertEquals(0, inner.getHorizontalScrollBar().getValue(),
                        "wide inner pane should not slide sideways when outer scrolls vertically");
            } finally {
                frame.dispose();
            }
        });
    }
}
