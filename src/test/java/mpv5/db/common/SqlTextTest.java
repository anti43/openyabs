package mpv5.db.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SqlTextTest {

    @Test
    void escapesQuotes() {
        assertEquals("O''Brien", SqlText.literal("O'Brien"));
        assertEquals("", SqlText.literal(null));
        assertEquals("O`Brien", SqlText.legacy("O'Brien"));
        assertEquals("cname = 'Muster'", SqlText.equalsText("cname", "Muster"));
        assertEquals("(cname = 'O''Brien' OR cname = 'O`Brien')", SqlText.equalsText("cname", "O'Brien"));
        assertEquals("O_BRIEN", SqlText.likeText("O'BRIEN"));
        assertEquals("(p = 'a\\b' OR p = 'a<removedbackslash>b')", SqlText.equalsText("p", "a\\b"));
        assertEquals("a\\b", QueryHandler.rescapeBackslashes("a<removedbackslash>b"));
    }

    @Test
    void saveStringKeepsTheText() {
        SaveString s = new SaveString("href='x'", true);
        assertEquals("href=''x''", s.toString());
        assertEquals("href='x'", s.getRaw());
        assertEquals("'", s.getWrapper());
        //not a text (numbers ...): never a quote in the SQL
        assertEquals("1`", new SaveString("1'", false).toString());
    }
}
