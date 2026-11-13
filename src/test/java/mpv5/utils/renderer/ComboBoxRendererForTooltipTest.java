package mpv5.utils.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JList;
import org.junit.jupiter.api.Test;

class ComboBoxRendererForTooltipTest {

    /**
     * A dark list (dark desktop theme): text and background come from the list,
     * also after the renderer painted something else (white on white before)
     */
    @Test
    void usesTheColorsOfTheList() {
        JList<String> list = new JList<String>(new String[]{"A", "B"});
        list.setBackground(new Color(40, 40, 40));
        list.setForeground(Color.WHITE);
        list.setSelectionBackground(new Color(21, 83, 158));
        list.setSelectionForeground(Color.WHITE);
        ComboBoxRendererForTooltip r = new ComboBoxRendererForTooltip();
        r.setForeground(Color.WHITE);//left over from painting the combo box
        Component c = r.getListCellRendererComponent(list, "A", 0, false, false);
        assertEquals(new Color(40, 40, 40), c.getBackground());
        assertEquals(Color.WHITE, c.getForeground());
        c = r.getListCellRendererComponent(list, "B", 1, true, false);
        assertEquals(new Color(21, 83, 158), c.getBackground());
        assertEquals(Color.WHITE, c.getForeground());
        list.setBackground(Color.WHITE);
        list.setForeground(Color.BLACK);
        c = r.getListCellRendererComponent(list, "A", 0, false, false);
        assertEquals(Color.WHITE, c.getBackground());
        assertEquals(Color.BLACK, c.getForeground());
    }
}
