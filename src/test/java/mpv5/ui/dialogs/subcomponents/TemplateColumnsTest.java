package mpv5.ui.dialogs.subcomponents;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class TemplateColumnsTest {

    @Test
    void columnsOfAFormat() {
        assertEquals(Arrays.asList(4, 2, 9), ControlPanel_Templates.columns("4, 2,x,9,2,0"));
        assertEquals(Collections.emptyList(), ControlPanel_Templates.columns(""));
    }

    @Test
    void namesInTheirOrder() {
        String s = ControlPanel_Templates.columnNames("2,4,6");
        assertEquals(3, s.split(" · ").length, s);
    }
}
