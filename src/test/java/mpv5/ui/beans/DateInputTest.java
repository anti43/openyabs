package mpv5.ui.beans;

import static org.junit.jupiter.api.Assertions.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.Test;

public class DateInputTest {

    private static String de(Date d) {
        return d == null ? null : new SimpleDateFormat("dd.MM.yyyy").format(d);
    }

    @Test
    public void germanInput() {
        String p = "dd.MM.yyyy";
        int y = Calendar.getInstance().get(Calendar.YEAR);
        assertEquals("01.02.2026", de(DateInput.parse("01.02.2026", p)));
        assertEquals("01.02.2026", de(DateInput.parse("1.2.2026", p)));
        assertEquals("01.02.2026", de(DateInput.parse("1.2.26", p)));
        assertEquals("01.02.2026", de(DateInput.parse("010226", p)));
        assertEquals("01.02.2026", de(DateInput.parse("01022026", p)));
        assertEquals("01.02." + y, de(DateInput.parse("1.2.", p)));
        assertEquals("01.02." + y, de(DateInput.parse("1.2", p)));
        assertEquals(de(new Date()), de(DateInput.parse("heute", p)));
        assertNull(DateInput.parse("32.1.2026", p));
        assertNull(DateInput.parse("abc", p));
    }

    @Test
    public void usInput() {
        String p = "MM/dd/yyyy";
        assertEquals("01.02.2026", de(DateInput.parse("02/01/2026", p)));
        assertEquals("01.02.2026", de(DateInput.parse("2/1/26", p)));
    }
}
