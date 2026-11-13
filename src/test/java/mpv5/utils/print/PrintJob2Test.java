package mpv5.utils.print;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.print.Paper;
import org.junit.jupiter.api.Test;

/**
 * The component print changed the one shared A4 paper to a printable area of
 * about 15 x 21 metres: every later print rendered gigantic raster pages on
 * the Swing thread and YaBS hung.
 */
class PrintJob2Test {

    @Test
    void componentPaperHasMarginsInsideTheA4Page() {
        Paper p = PrintJob2.a4WithMargins();
        assertEquals(595, p.getWidth(), 0.01);
        assertEquals(842, p.getHeight(), 0.01);
        assertEquals(56.16, p.getImageableX(), 0.01);
        assertEquals(56.16, p.getImageableY(), 0.01);
        assertEquals(595 - 2 * 56.16, p.getImageableWidth(), 0.01);
        assertEquals(842 - 2 * 56.16, p.getImageableHeight(), 0.01);
    }

    @Test
    void everyPrintGetsItsOwnPaper() {
        Paper margins = PrintJob2.a4WithMargins();
        Paper plain = PrintJob2.a4();
        assertNotSame(margins, plain);
        assertNotSame(PrintJob2.a4(), PrintJob2.a4());
        //a component print does not change the paper of the next PDF print
        assertEquals(0, plain.getImageableX(), 0.01);
        assertEquals(595, plain.getImageableWidth(), 0.01);
        assertEquals(842, plain.getImageableHeight(), 0.01);
    }
}
