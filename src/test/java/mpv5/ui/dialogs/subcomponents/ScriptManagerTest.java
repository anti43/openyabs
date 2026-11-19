package mpv5.ui.dialogs.subcomponents;

import static org.junit.jupiter.api.Assertions.*;

import mpv5.db.common.Context;
import mpv5.db.common.ScriptScope;
import org.junit.jupiter.api.Test;

/**
 * The helpers of the script manager: kinds ("For"), names, the template
 * field shown, examples
 */
class ScriptManagerTest {

    @Test
    void allDocumentsComesFirstAndKindsAreFoundById() {
        assertEquals(ScriptScope.ALL_DOCUMENTS, ControlPanel_ValueProps.kinds().get(0).id);
        assertEquals("item", ControlPanel_ValueProps.kindOf(Context.getOffer().getId()).prefix);
        assertEquals("contact", ControlPanel_ValueProps.kindOf(Context.getCustomer().getId()).prefix);
        assertEquals("product", ControlPanel_ValueProps.kindOf(Context.getProduct().getId()).prefix);
        assertNull(ControlPanel_ValueProps.kindOf(-5));
    }

    @Test
    void namesAreCleanedButHooksStayAsTheyAre() {
        assertEquals("anrede", ControlPanel_ValueProps.cleanName(" an-rede "));
        assertEquals("modifyOnSave", ControlPanel_ValueProps.cleanName("modifyOnSave"));
        assertEquals("IntItemLabel", ControlPanel_ValueProps.cleanName("IntItemLabel"));
        assertTrue(ControlPanel_ValueProps.isSpecial("executeAfterSave"));
        assertFalse(ControlPanel_ValueProps.isSpecial("anrede"));
    }

    @Test
    void theFieldShownFitsTheKind() {
        assertTrue(ControlPanel_ValueProps.fieldText(ControlPanel_ValueProps.kindOf(ScriptScope.ALL_DOCUMENTS), "anrede")
                .contains("<item.property.anrede>"));
        String contact = ControlPanel_ValueProps.fieldText(ControlPanel_ValueProps.kindOf(Context.getContact().getId()), "anrede");
        assertTrue(contact.contains("<item.contact.property.anrede>"), contact);
        assertTrue(contact.contains("<contact.property.anrede>"), contact);
        assertTrue(ControlPanel_ValueProps.fieldText(ControlPanel_ValueProps.kindOf(Context.getProduct().getId()), "x")
                .contains("<product.property.x>"));
    }

    @Test
    void onlyTheOuterScriptSymbolsAreRemoved() {
        assertEquals("'a' + 'b'", ControlPanel_ValueProps.unwrap("#'a' + 'b'#"));
        assertEquals("plain", ControlPanel_ValueProps.unwrap("plain"));
        assertEquals("", ControlPanel_ValueProps.unwrap(null));
    }

    @Test
    void examplesForEveryKindHaveNameAndScript() {
        for (ControlPanel_ValueProps.Kind k : ControlPanel_ValueProps.kinds()) {
            for (String[] ex : ControlPanel_ValueProps.Examples.of(k.prefix)) {
                assertEquals(3, ex.length);
                assertFalse(ex[1].isEmpty(), k.label);
                assertFalse(ex[2].isEmpty(), k.label);
                assertFalse(ex[2].contains("#"), "the script symbol must not be in an example: " + ex[2]);
            }
        }
    }
}
