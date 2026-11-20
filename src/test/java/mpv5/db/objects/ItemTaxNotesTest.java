package mpv5.db.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import mpv5.utils.export.VatCategory;
import org.junit.jupiter.api.Test;

/**
 * The notes of the VAT categories on invoices, item.taxnotes (#209)
 */
class ItemTaxNotesTest {

    private static SubItem line(String text, String rate) {
        SubItem s = new SubItem();
        s.setDescription(text);
        s.setQuantityvalue(BigDecimal.ONE);
        s.setExternalvalue(new BigDecimal("100"));
        s.setTaxpercentvalue(new BigDecimal(rate));
        return s;
    }

    @Test
    void notesOfThePositions() {
        Tax reverse = new Tax();
        reverse.setTaxvalue(BigDecimal.ZERO);
        reverse.setCategorycode("AE");
        SubItem[] subs = {line("Beratung", "0"), line("Lizenz", "19"), line("Support", "0"), line("", "0")};
        //all 0 % positions reverse charge: the note once
        assertEquals(VatCategory.AE.defaultNote(), Item.taxNotes(subs, s -> reverse, false, null));
        //no rate: exempt (E), with the reason of the company if set
        assertEquals(VatCategory.E.defaultNote(), Item.taxNotes(subs, s -> null, false, null));
        assertEquals("Steuerfrei nach X", Item.taxNotes(subs, s -> null, false, "Steuerfrei nach X"));
        //positions with VAT only: no note
        assertEquals("", Item.taxNotes(new SubItem[]{line("Lizenz", "19")}, s -> null, false, null));
        //small business: its own note (companyinfo.smallbusinessnote)
        assertEquals("", Item.taxNotes(subs, s -> reverse, true, null));
    }
}
