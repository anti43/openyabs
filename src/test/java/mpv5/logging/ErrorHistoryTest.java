package mpv5.logging;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * The errors in the history: one entry per message, with count
 */
class ErrorHistoryTest {

    @Test
    void messageIsTypeAndTextOnOneLine() {
        assertEquals("IllegalStateException: no printer found",
                ErrorHistory.message(new IllegalStateException("  no printer\n   found ")));
        assertEquals("NullPointerException", ErrorHistory.message(new NullPointerException()));
        String longText = "x".repeat(400);
        assertEquals(250, ErrorHistory.message(new RuntimeException(longText)).length());
    }

    @Test
    void whereNamesTheSourceAndTheYabsCode() {
        Exception e = new Exception("x");
        e.setStackTrace(new StackTraceElement[]{
            new StackTraceElement("java.util.ArrayList", "get", "ArrayList.java", 427),
            new StackTraceElement("mpv5.utils.export.Export", "print", "Export.java", 380)});
        assertEquals("mpv5.utils.export.Export.print(Export.java:380)", ErrorHistory.where(Log.class, e));
        assertEquals("mpv5.handler.ContactService · mpv5.utils.export.Export.print(Export.java:380)",
                ErrorHistory.where(mpv5.handler.ContactService.class, e));
        assertEquals("Thread AWT-EventQueue-0 · mpv5.utils.export.Export.print(Export.java:380)",
                ErrorHistory.where("AWT-EventQueue-0", e));
    }

    @Test
    void normalFlowIsNoError() {
        assertTrue(ErrorHistory.ignored(new mpv5.db.common.NodataFoundException()));
        assertFalse(ErrorHistory.ignored(new NullPointerException()));
    }
}
