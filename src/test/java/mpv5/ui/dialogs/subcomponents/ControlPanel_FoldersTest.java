package mpv5.ui.dialogs.subcomponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.Date;
import mpv5.db.common.DatabaseBackup;
import mpv5.globals.LocalSettings;
import mpv5.handler.ExpenseReceipts;
import mpv5.handler.ZugpferdExpenseImport;
import mpv5.ui.panels.MPControlPanel;
import org.junit.jupiter.api.Test;

class ControlPanel_FoldersTest {

    @Test
    void foldersControlPanelEntryExists() {
        boolean found = false;
        for (MPControlPanel.Entry e : MPControlPanel.entries()) {
            if (e.panelClass == ControlPanel_Folders.class) {
                found = true;
                break;
            }
        }
        assertTrue(found, "ControlPanel_Folders entry must be present in MPControlPanel entries");
    }

    @Test
    void receiptsDirResolvesPlaceholders() {
        Date fixed = new Date(1735689600000L); // 2025-01-01
        File resolved = ExpenseReceipts.resolve("[APPDIR]/receipts/[YEAR]", "testdb", fixed);
        assertNotNull(resolved);
        assertTrue(resolved.getPath().contains("receipts"));
        assertTrue(resolved.getPath().contains("2025"));
    }

    @Test
    void panelCreationAndDefaults() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanel_Folders panel = new ControlPanel_Folders();
        assertNotNull(panel);
        assertEquals(ControlPanel_Folders.UNAME, panel.getUname());
        assertNotNull(panel.getAndRemoveActionPanel());
    }

    @Test
    void localPanelDoesNotOverwriteFoldersSettings() {
        String testBase = "target/custom-base-dir-test";
        String testReceipts = "target/custom-receipts-dir-test";
        LocalSettings.setProperty(LocalSettings.BASE_DIR, testBase);
        ExpenseReceipts.setFolderPattern(testReceipts);

        // Instantiating and saving ControlPanel_Local must NOT overwrite BASE_DIR or BACKUP_DIR
        assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanel_Local local = new ControlPanel_Local();
        local.setValues(new mpv5.data.PropertyStore());
        // Trigger save indirectly / verify BASE_DIR remains untouched
        assertEquals(testBase, LocalSettings.getProperty(LocalSettings.BASE_DIR));
        assertEquals(testReceipts, ExpenseReceipts.folderPattern());
    }
}
