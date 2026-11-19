package mpv5.db.common;

import static org.junit.jupiter.api.Assertions.*;

import mpv5.db.objects.Contact;
import org.junit.jupiter.api.Test;

/**
 * Scripts (template fields, script manager) read the fields of the record by
 * their template names: contact.ismale, contact.cname
 */
class ScriptFieldsTest {

    @Test
    void contactFieldsByTemplateName() {
        Contact c = new Contact();
        c.setCname("Muster");
        c.setisMale(false);
        ScriptFields f = new ScriptFields(c);
        assertEquals("Muster", f.get("cname"));
        assertEquals(Boolean.FALSE, f.get("ismale"));
        assertNull(f.get("nosuchfield"));
        //no contact without a contactsids
        assertNull(f.get("contact"));
    }

    @Test
    void scriptUsesTheFieldsLikeTheTemplate() {
        Contact c = new Contact();
        c.setCname("Muster");
        c.setisMale(true);
        String script = "#contact.ismale ? 'Hello Mr ' + contact.cname + '!' : 'Hello Mrs ' + contact.cname + '!'#";
        assertEquals("Hello Mr Muster!", c.evaluate(script));
        c.setisMale(false);
        assertEquals("Hello Mrs Muster!", c.evaluate(script));
        //dbo still works
        assertEquals("Muster", c.evaluate("#dbo.__getCname()#"));
    }
}
