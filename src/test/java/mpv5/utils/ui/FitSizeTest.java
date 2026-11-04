package mpv5.utils.ui;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.UIManager;
import mpv5.ui.beans.DateChooser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FitSizeTest {

    @BeforeAll
    static void flatLaf() throws Exception {
        UIManager.setLookAndFeel(mpv5.ui.Appearance.LIGHT);
    }

    @Test
    void enlargesFixedLabelButNeverShrinks() {
        JPanel p = new JPanel();
        JLabel longLabel = new JLabel("Department of something long");
        longLabel.setPreferredSize(new Dimension(40, 20));
        JLabel shortLabel = new JLabel("ID");
        shortLabel.setPreferredSize(new Dimension(100, 20));
        p.add(longLabel);
        p.add(shortLabel);
        FitSize.fit(p);
        assertTrue(longLabel.getPreferredSize().width >= longLabel.getUI().getPreferredSize(longLabel).width);
        assertEquals(100, shortLabel.getPreferredSize().width, "never shrink");

        FitSize.fit(p);//repeatable
        assertEquals(100, shortLabel.getPreferredSize().width);
    }

    @Test
    void dateSpinnerFitsLongestMonth() {
        JSpinner s = new JSpinner(new SpinnerDateModel());
        s.setEditor(new JSpinner.DateEditor(s, "MMMM d, yyyy"));
        s.setPreferredSize(new Dimension(100, 20));
        JPanel p = new JPanel();
        p.add(s);
        FitSize.fit(p);
        int text = s.getFontMetrics(((JSpinner.DateEditor) s.getEditor()).getTextField().getFont()).stringWidth("September 28, 2028");
        assertTrue(s.getPreferredSize().width > text, s.getPreferredSize() + " vs text " + text);
    }

    @Test
    void dateChooserGrowsWithItsSpinner() {
        DateChooser chooser = new DateChooser();
        chooser.setPreferredSize(new Dimension(118, 30));
        JPanel p = new JPanel();
        p.add(chooser);
        FitSize.fit(p);
        int spinner = chooser.getSpinner().getPreferredSize().width;
        assertTrue(chooser.getPreferredSize().width >= spinner, chooser.getPreferredSize() + " spinner " + spinner);
    }
}
