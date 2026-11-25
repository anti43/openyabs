package mpv5.ui.misc;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import mpv5.ui.modern.PrimaryButton;
import org.junit.jupiter.api.Test;

class SaveShortcutTest {

    @Test
    void prefersExactSaveOverBackupAndExcludedButtons() {
        JPanel panel = new JPanel();
        JButton saveAsNew = new JButton("Save as new");
        JButton saveFileAs = new JButton("Save file as");
        JButton checkUpdates = new JButton("Check for updates");
        PrimaryButton deleteBtn = new PrimaryButton("Delete", null);
        deleteBtn.setColor(new Color(200, 30, 30));
        PrimaryButton backupNow = new PrimaryButton("Backup now", "/images/16/filesave.png");
        JButton saveSettings = new JButton("Save");

        panel.add(saveAsNew);
        panel.add(saveFileAs);
        panel.add(checkUpdates);
        panel.add(deleteBtn);
        panel.add(backupNow);
        panel.add(saveSettings);

        assertSame(saveSettings, SaveShortcut.findSaveButton(panel));
    }

    @Test
    void matchesLocalizedSaveUpdateAndApplyInPriorityOrder() {
        JPanel p1 = new JPanel();
        JButton apply = new JButton("\u00dcbernehmen");
        JButton update = new JButton("Aktualisieren");
        JButton save = new JButton("Speichern");
        p1.add(apply);
        p1.add(update);
        p1.add(save);
        assertSame(save, SaveShortcut.findSaveButton(p1));

        save.setEnabled(false);
        assertSame(update, SaveShortcut.findSaveButton(p1));

        update.setVisible(false);
        assertSame(apply, SaveShortcut.findSaveButton(p1));
    }

    @Test
    void ignoresToggleButtonsHiddenTabsAndRedPrimaryButtons() {
        JTabbedPane tabs = new JTabbedPane();
        JPanel visibleTab = new JPanel();
        JToggleButton toggle = new JToggleButton("Save");
        PrimaryButton dangerSave = new PrimaryButton("Save", null);
        dangerSave.setColor(Color.RED);
        visibleTab.add(toggle);
        visibleTab.add(dangerSave);

        JPanel hiddenTab = new JPanel();
        JButton hiddenSave = new JButton("Save");
        hiddenTab.add(hiddenSave);

        tabs.addTab("One", visibleTab);
        tabs.addTab("Two", hiddenTab);
        tabs.setSelectedIndex(0);
        hiddenTab.setVisible(false);

        assertNull(SaveShortcut.findSaveButton(tabs));
    }

    @Test
    void triggerInClicksSaveButtonAndCommitsSpinner() {
        JPanel panel = new JPanel();
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(10, 0, 100, 1));
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
        editor.getTextField().setText("42");

        AtomicInteger clicks = new AtomicInteger();
        AtomicInteger seenValue = new AtomicInteger();
        JButton update = new JButton("Update");
        update.addActionListener(e -> {
            clicks.incrementAndGet();
            seenValue.set(((Number) spinner.getValue()).intValue());
        });
        panel.add(spinner);
        panel.add(update);

        assertTrue(SaveShortcut.triggerIn(panel, editor.getTextField()));
        assertEquals(1, clicks.get());
        assertEquals(42, seenValue.get());
    }
}
