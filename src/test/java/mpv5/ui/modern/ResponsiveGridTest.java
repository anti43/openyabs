package mpv5.ui.modern;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Dimension;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class ResponsiveGridTest {

    private static JPanel box(int h) {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(50, h));
        return p;
    }

    @Test
    void fullWidthCardTakesAWholeRow() {
        ResponsiveGrid g = new ResponsiveGrid(100, 2);
        JPanel a = box(40), b = box(60), c = box(30), wide = box(200);
        g.add(a);
        g.add(b);
        g.add(c);
        g.addFullWidth(wide);
        g.setSize(500, 1000);
        g.doLayout();
        //two columns as before
        assertEquals(a.getY(), b.getY());
        assertEquals(a.getHeight(), b.getHeight());
        assertEquals(a.getWidth(), c.getWidth());
        //the wide card: a row of its own, over both columns
        assertEquals(c.getX(), wide.getX());
        assertEquals(b.getX() + b.getWidth() - a.getX(), wide.getWidth());
        assertEquals(true, wide.getY() > c.getY());
        assertEquals(200, wide.getHeight());
    }
}
