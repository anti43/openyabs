package mpv5.ui.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import mpv5.db.objects.SubItem;
import mpv5.handler.ContactService;
import mpv5.utils.models.MPTableModel;
import org.junit.jupiter.api.Test;

class ItemPositionsTableTaxOverrideTest {

    @Test
    void customerTaxRateNullHandling() {
        assertNull(ContactService.customerTaxRate((mpv5.db.objects.Contact) null));
        assertNull(ContactService.customerTaxRate(0));
        assertNull(ContactService.customerTaxRate(-1));
    }

    @Test
    void itemPositionsTableAppliesCustomerTaxRateOverride() {
        ItemPositionsTable table = new ItemPositionsTable();
        table.setDefaultPositions();
        MPTableModel model = table.getModel();

        // Populate a position with 19%
        model.setValueAt("Position 1", 0, 4);
        model.setValueAt(new BigDecimal("100.00"), 0, 5);
        model.setValueAt(new BigDecimal("19.00"), 0, 6);

        // Apply customer tax rate override of 0% (tax exempt customer)
        table.applyCustomerTaxRate(BigDecimal.ZERO);
        assertEquals(0, BigDecimal.ZERO.compareTo(new BigDecimal(model.getValueAt(0, 6).toString())));

        // Apply customer tax rate override of 7%
        table.applyCustomerTaxRate(new BigDecimal("7.00"));
        assertEquals(0, new BigDecimal("7.00").compareTo(new BigDecimal(model.getValueAt(0, 6).toString())));

        // Adding a position while customerTaxRate is 7%
        SubItem s = new SubItem();
        s.setDescription("New Position");
        s.setExternalvalue(new BigDecimal("50.00"));
        s.setTaxpercentvalue(new BigDecimal("19.00"));
        table.addPosition(s);

        int newRow = model.getRowCount() - 1;
        assertEquals(0, new BigDecimal("7.00").compareTo(new BigDecimal(model.getValueAt(newRow, 6).toString())));
    }
}
