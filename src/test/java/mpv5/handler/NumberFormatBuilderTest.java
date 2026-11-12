package mpv5.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.GregorianCalendar;
import org.junit.jupiter.api.Test;

class NumberFormatBuilderTest {

    private static final Date NOV_2026 = new GregorianCalendar(2026, 10, 12).getTime();

    @Test
    void buildsPatternAndSamples() {
        NumberFormatBuilder b = new NumberFormatBuilder().setPrefix("RE-").setDate(NumberFormatBuilder.DatePart.YEAR)
                .setSeparator("-").setDigits(5);
        assertEquals("RE-[YEAR]-{0,number,00000}", b.pattern());
        assertEquals("RE-2026-00042", b.sample(42, NOV_2026));
        assertEquals("RE-2026-123456", b.sample(123456, NOV_2026));

        b.setDate(NumberFormatBuilder.DatePart.YEAR_MONTH).setSeparator("/").setPrefix("AN").setDigits(3).setSuffix(" (k)");
        assertEquals("AN[YEAR]/[MONTH]/{0,number,000} (k)", b.pattern());
        assertEquals("AN2026/11/007 (k)", b.sample(7, NOV_2026));

        b.setDate(NumberFormatBuilder.DatePart.NONE).setPrefix("K-").setSuffix("").setDigits(4);
        assertEquals("K-{0,number,0000}", b.pattern());
        assertEquals("K-0001", b.sample(1, NOV_2026));
    }

    @Test
    void quotesAndForbiddenCharacters() {
        NumberFormatBuilder b = new NumberFormatBuilder().setPrefix("Bob's [x]{y}-").setDigits(2);
        assertEquals("Bob''s xy-{0,number,00}", b.pattern());
        assertEquals("Bob's xy-03", b.sample(3, NOV_2026));
        NumberFormatBuilder back = NumberFormatBuilder.fromPattern(b.pattern(), 0);
        assertNotNull(back);
        assertEquals("Bob's xy-", back.getPrefix());
    }

    @Test
    void parsePositionRestartOrContinue() {
        NumberFormatBuilder b = new NumberFormatBuilder().setPrefix("RE-").setDate(NumberFormatBuilder.DatePart.YEAR).setSeparator("-");
        b.setRestart(true);
        assertEquals(0, b.parsePosition());
        b.setRestart(false);
        assertEquals("RE-2026-".length(), b.parsePosition());
        b.setDate(NumberFormatBuilder.DatePart.YEAR_MONTH);
        assertEquals("RE-2026-11-".length(), b.parsePosition());
        b.setDate(NumberFormatBuilder.DatePart.NONE);
        assertEquals(0, b.parsePosition(), "no date part: nothing to skip");
    }

    @Test
    void readsBuiltPatternsBack() {
        for (NumberFormatBuilder.DatePart d : NumberFormatBuilder.DatePart.values()) {
            NumberFormatBuilder b = new NumberFormatBuilder().setPrefix("RE-").setDate(d).setSeparator("-").setDigits(6).setSuffix("/X");
            b.setRestart(d == NumberFormatBuilder.DatePart.NONE || d == NumberFormatBuilder.DatePart.YEAR_SHORT);
            NumberFormatBuilder back = NumberFormatBuilder.fromPattern(b.pattern(), b.parsePosition());
            assertNotNull(back, d.toString());
            assertEquals(b.pattern(), back.pattern(), d.toString());
            assertEquals(b.isRestart() || d == NumberFormatBuilder.DatePart.NONE, back.isRestart(), d.toString());
        }
        //the old default and an expert pattern
        assertEquals("{0,number,000000}", NumberFormatBuilder.fromPattern("{0,number,000000}", 0).pattern());
        assertNull(NumberFormatBuilder.fromPattern("[GROUP_NAME]-{0,number,00}", 0));
        assertNull(NumberFormatBuilder.fromPattern("{0,number,#,##0}", 0));
    }

    @Test
    void formatsExpertPatterns() {
        assertTrue(NumberFormatBuilder.format("Q[QUARTER]/[YEAR_SHORT]-{0,number,00}", 5, NOV_2026).equals("Q4/26-05"));
    }
}
