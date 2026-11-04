package mpv5.utils.ui;

import static org.junit.jupiter.api.Assertions.*;

import mpv5.ui.beans.LabeledTextField;
import mpv5.utils.export.ZugpferdFile;
import org.junit.jupiter.api.Test;

class FieldCheckTest {

    @Test
    void marksInvalidValues() {
        LabeledTextField field = new LabeledTextField();
        FieldCheck.attach(field, ZugpferdFile::isValidVatId, "tip", "invalid");
        java.awt.Color normal = field.getTextField().getBackground();

        field.setText("345435");
        assertNotEquals(normal, field.getTextField().getBackground());
        assertEquals("invalid", field.getTextField().getToolTipText());

        field.setText("DE123456789");
        assertEquals(normal, field.getTextField().getBackground());
        assertEquals("tip", field.getTextField().getToolTipText());

        field.setText("");
        assertEquals(normal, field.getTextField().getBackground(), "empty is fine");
    }
}
