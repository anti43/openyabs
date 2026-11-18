package mpv5.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JComponent;
import mpv5.YabsViewProxy;
import mpv5.db.objects.Item;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Company;
import mpv5.ui.panels.MPControlPanel;
import mpv5.ui.panels.ModernItemPanel;
import org.junit.jupiter.api.Test;

class MenuNavigationTest {

    @Test
    void simpleNavigationSettingToggle() {
        boolean original = LocalSettings.isSimpleNavigation();
        try {
            LocalSettings.setSimpleNavigation(true);
            assertTrue(LocalSettings.isSimpleNavigation());

            LocalSettings.setSimpleNavigation(false);
            assertFalse(LocalSettings.isSimpleNavigation());
        } finally {
            LocalSettings.setSimpleNavigation(original);
        }
    }

    @Test
    void companyInfoControlPanelEntryExists() {
        boolean found = false;
        for (MPControlPanel.Entry e : MPControlPanel.entries()) {
            if (e.panelClass == ControlPanel_Company.class) {
                found = true;
                break;
            }
        }
        assertTrue(found, "ControlPanel_Company entry must be present in MPControlPanel entries");
    }

    @Test
    void invoiceStatusInProgressConstant() {
        assertEquals(1, Item.STATUS_IN_PROGRESS);
        assertEquals(Messages.STATUS_IN_PROGRESS.toString(), Item.getStatusString(Item.STATUS_IN_PROGRESS));
    }
}
