package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import mpv5.db.objects.Tax;
import org.junit.jupiter.api.Test;

/**
 * VAT categories of tax rates (#209)
 */
class VatCategoryTest {

    @Test
    void withoutCodeAsBefore() {
        assertSame(VatCategory.S, VatCategory.of(null, new BigDecimal("19")));
        assertSame(VatCategory.S, VatCategory.of("", new BigDecimal("7")));
        assertSame(VatCategory.E, VatCategory.of(null, BigDecimal.ZERO));
        assertSame(VatCategory.E, VatCategory.of(" ", null));
    }

    @Test
    void storedCodeIfItFitsTheRate() {
        assertSame(VatCategory.AE, VatCategory.of("AE", BigDecimal.ZERO));
        assertSame(VatCategory.K, VatCategory.of("k", BigDecimal.ZERO));
        assertSame(VatCategory.G, VatCategory.of("G", new BigDecimal("0.00")));
        assertSame(VatCategory.L, VatCategory.of("L", new BigDecimal("7")));
        //does not fit: the default of the rate
        assertSame(VatCategory.S, VatCategory.of("AE", new BigDecimal("19")));
        assertSame(VatCategory.E, VatCategory.of("S", BigDecimal.ZERO));
        assertSame(VatCategory.E, VatCategory.of("XX", BigDecimal.ZERO));
    }

    @Test
    void defaults() {
        assertEquals("VATEX-EU-AE", VatCategory.AE.defaultReasonCode());
        assertEquals("VATEX-EU-IC", VatCategory.K.defaultReasonCode());
        assertEquals("VATEX-EU-G", VatCategory.G.defaultReasonCode());
        assertNull(VatCategory.E.defaultReasonCode());
        assertFalse(VatCategory.AE.defaultNote().isEmpty());
        assertFalse(VatCategory.E.defaultNote().isEmpty());
        assertEquals("", VatCategory.S.defaultNote());
        assertTrue(VatCategory.AE.needsReason());
        assertFalse(VatCategory.Z.needsReason());
        assertFalse(VatCategory.S.needsReason());
    }

    private static Tax tax(int id, String rate, String category, String note) {
        Tax t = new Tax();
        t.setIDS(id);
        t.setTaxvalue(new BigDecimal(rate));
        t.setCategorycode(category);
        t.setExemptionnote(note);
        return t;
    }

    @Test
    void taxOfAPosition() {
        Tax regular = tax(1, "19", "", "");
        Tax exempt = tax(2, "0", "", "");
        Tax reverse = tax(3, "0", "AE", "");
        Tax export = tax(4, "0", "G", "Ausfuhr");
        List<Tax> all = Arrays.asList(regular, exempt, reverse, export);
        //the customer's rate first, then the product's
        assertSame(reverse, Tax.forRate(BigDecimal.ZERO, all, 3, 4));
        assertSame(export, Tax.forRate(BigDecimal.ZERO, all, 4, 3));
        //a preferred rate with another value is skipped (customer 19 %, product exempt)
        assertSame(exempt, Tax.forRate(BigDecimal.ZERO, all, 1, 2));
        //none preferred: a rate with a category before one without
        assertSame(reverse, Tax.forRate(new BigDecimal("0.00"), all));
        assertSame(regular, Tax.forRate(new BigDecimal("19.00"), all, (Integer) null));
        assertNull(Tax.forRate(new BigDecimal("7"), all));

        assertSame(VatCategory.AE, reverse.category());
        assertEquals("VATEX-EU-AE", reverse.reasonCode());
        assertEquals(VatCategory.AE.defaultNote(), reverse.note());
        assertEquals("Ausfuhr", export.note());
        assertSame(VatCategory.S, regular.category());
        assertNull(regular.reasonCode());
        assertEquals("", regular.note());
    }
}
