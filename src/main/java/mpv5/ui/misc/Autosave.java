/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.misc;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.beans.DateChooser;
import mpv5.ui.frames.MPView;
import mpv5.ui.panels.DataPanel;
import mpv5.ui.panels.ModernDataPanel;

/**
 * Optional autosave for the main modern views ({@link mpv5.ui.panels.ModernItemPanel},
 * {@link mpv5.ui.panels.ModernContactPanel}, {@link mpv5.ui.panels.ModernProductPanel}).
 * <p>
 * When enabled (Extras &gt; Autosave), leaving an editable field whose value was
 * modified saves the dataset in place without popups or focus loss. Whenever a
 * {@link DatabaseObject} is newly opened, the current autosave status (ON/OFF)
 * is logged to the status bar only.
 */
public final class Autosave {

    public static final String PROPERTY = "org.openyabs.uiproperty.autosave";
    public static final String IGNORE_PROPERTY = "yabs.autosave.ignore";

    private static boolean installed;
    private static ModernDataPanel trackedPanel;
    private static Component trackedField;
    private static Object trackedInitialValue;
    private static long trackedSaveGeneration;
    private static JTable trackedTable;
    private static int trackedTableRow = -1;
    private static int trackedTableCol = -1;
    private static Object trackedTableInitialValue;

    private Autosave() {
    }

    /**
     * @return true if autosave is enabled for the current user
     */
    public static boolean isEnabled() {
        try {
            User user = User.getCurrentUser();
            if (user == null || user.getProperties() == null) {
                return false;
            }
            return Boolean.parseBoolean(user.getProperties().getProperty(PROPERTY));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Enables or disables autosave for the current user and logs the new status
     * to the status bar only.
     *
     * @param enabled true to enable autosave
     */
    public static void setEnabled(boolean enabled) {
        try {
            User user = User.getCurrentUser();
            if (user != null) {
                user.changeProperty(PROPERTY, String.valueOf(enabled));
                user.saveProperties();
            }
        } catch (Exception e) {
            Log.Debug(Autosave.class, e);
        }
        logStatus();
    }

    /**
     * Logs the current autosave status (ON/OFF) to the status bar only.
     */
    public static void logStatus() {
        MPView.setStatusMessage((isEnabled() ? Messages.AUTOSAVE_STATUS_ON : Messages.AUTOSAVE_STATUS_OFF).toString());
    }

    /**
     * Installs the global focus-leave tracker once.
     */
    public static synchronized void ensureInstalled() {
        if (installed) {
            return;
        }
        installed = true;
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(
                "permanentFocusOwner", new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        Object newVal = evt.getNewValue();
                        onFocusChanged(newVal instanceof Component ? (Component) newVal : null);
                    }
                });
        DatabaseObject.addSaveListener(o -> SwingUtilities.invokeLater(() -> onObjectSaved(o)));
    }

    /**
     * Schedules an in-place autosave for {@code panel} on the EDT if autosave
     * is enabled (used for edits that commit via a separate dialog or button,
     * such as the floating position text editor or moving/removing a position).
     *
     * @param panel the target panel
     */
    public static void triggerLater(final ModernDataPanel panel) {
        if (panel == null || !panel.isAutosaveEnabledPanel() || !isEnabled()) {
            return;
        }
        final long gen = panel.getSaveGeneration();
        SwingUtilities.invokeLater(() -> {
            if (isEnabled() && panel.isShowing() && !panel.isLoadingData() && !panel.isAutosaving()
                    && panel.getSaveGeneration() == gen) {
                panel.performAutosave();
            }
        });
    }

    private static void onObjectSaved(DatabaseObject saved) {
        if (saved == null || trackedPanel == null || trackedField == null) {
            return;
        }
        DatabaseObject owner = trackedPanel.getDataOwner();
        if (owner == saved || (owner != null && owner.getContext().equals(saved.getContext())
                && owner.__getIDS() > 0 && owner.__getIDS().equals(saved.__getIDS()))) {
            // Reset baseline for the currently focused field so leaving it after an explicit Ctrl+S doesn't re-save
            trackedInitialValue = captureValue(trackedField);
            trackedSaveGeneration = trackedPanel.getSaveGeneration();
            if (trackedTable != null && trackedTableRow >= 0 && trackedTableCol >= 0
                    && trackedTableRow < trackedTable.getRowCount() && trackedTableCol < trackedTable.getColumnCount()) {
                trackedTableInitialValue = trackedTable.getValueAt(trackedTableRow, trackedTableCol);
            }
        }
    }

    private static void onFocusChanged(Component newOwner) {
        // Still inside the exact same logical field unit (e.g., combo editor <-> combo, date spinner <-> calendar button)?
        if (trackedField != null && newOwner != null && isInsideField(newOwner, trackedField)) {
            return;
        }

        final ModernDataPanel prevPanel = trackedPanel;
        final Component prevField = trackedField;
        final Object prevInitial = trackedInitialValue;
        final long prevGen = trackedSaveGeneration;
        final JTable prevTable = trackedTable;
        final int prevTableRow = trackedTableRow;
        final int prevTableCol = trackedTableCol;
        final Object prevTableInitial = trackedTableInitialValue;

        // Start tracking the newly focused editable field (if any) immediately
        trackNewFocus(newOwner);

        if (prevPanel == null || prevField == null || !isEnabled()) {
            return;
        }
        if (!prevPanel.isShowing() || prevPanel.isLoadingData() || prevPanel.isAutosaving()) {
            return;
        }
        // Defer one EDT cycle so component FocusListeners (e.g. docked editor commit,
        // spinner commit, markup recalculation, cell editor commit) finish first
        SwingUtilities.invokeLater(() -> {
            if (!isEnabled() || !prevPanel.isShowing() || prevPanel.isLoadingData() || prevPanel.isAutosaving()) {
                return;
            }
            if (prevPanel.getSaveGeneration() != prevGen) {
                return;
            }
            if (prevField instanceof JComboBox && ((JComboBox<?>) prevField).isPopupVisible()) {
                return;
            }
            if (hasFieldChanged(prevField, prevInitial, prevTable, prevTableRow, prevTableCol, prevTableInitial)) {
                prevPanel.performAutosave();
            }
        });
    }

    private static void trackNewFocus(Component comp) {
        trackedPanel = null;
        trackedField = null;
        trackedInitialValue = null;
        trackedSaveGeneration = 0;
        trackedTable = null;
        trackedTableRow = -1;
        trackedTableCol = -1;
        trackedTableInitialValue = null;

        if (comp == null) {
            return;
        }
        ModernDataPanel panel = owningAutosavePanel(comp);
        if (panel == null || panel.isLoadingData()) {
            return;
        }
        Component field = resolveFieldUnit(comp, panel);
        if (field == null) {
            return;
        }
        trackedPanel = panel;
        trackedField = field;
        trackedInitialValue = captureValue(field);
        trackedSaveGeneration = panel.getSaveGeneration();

        // If this field is an active JTable cell editor, remember the cell coordinates & initial value too
        JTable table = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, comp);
        if (table != null && table.isEditing() && table.getEditingRow() >= 0 && table.getEditingColumn() >= 0) {
            trackedTable = table;
            trackedTableRow = table.getEditingRow();
            trackedTableCol = table.getEditingColumn();
            trackedTableInitialValue = table.getValueAt(trackedTableRow, trackedTableCol);
        }
    }

    private static ModernDataPanel owningAutosavePanel(Component comp) {
        for (Component k = comp; k != null; k = k.getParent()) {
            if (k instanceof JComponent && Boolean.TRUE.equals(((JComponent) k).getClientProperty(IGNORE_PROPERTY))) {
                return null;
            }
            if (k instanceof DataPanel) {
                if (k instanceof ModernDataPanel && ((ModernDataPanel) k).isAutosaveEnabledPanel()) {
                    return (ModernDataPanel) k;
                }
                return null;
            }
        }
        return null;
    }

    private static Component resolveFieldUnit(Component comp, ModernDataPanel panel) {
        for (Component k = comp; k != null && k != panel; k = k.getParent()) {
            if (k instanceof JComponent && Boolean.TRUE.equals(((JComponent) k).getClientProperty(IGNORE_PROPERTY))) {
                return null;
            }
            if (k instanceof DateChooser) {
                return k.isEnabled() ? k : null;
            }
            if (k instanceof JComboBox) {
                return k.isEnabled() ? k : null;
            }
            if (k instanceof JSpinner) {
                return k.isEnabled() ? k : null;
            }
        }
        if (comp instanceof JTextComponent) {
            JTextComponent tc = (JTextComponent) comp;
            return tc.isEditable() && tc.isEnabled() ? tc : null;
        }
        if (comp instanceof JToggleButton) {
            return comp.isEnabled() ? comp : null;
        }
        return null;
    }

    private static boolean isInsideField(Component candidate, Component field) {
        return candidate == field || SwingUtilities.isDescendingFrom(candidate, field);
    }

    private static Object captureValue(Component field) {
        if (field instanceof DateChooser) {
            return ((DateChooser) field).getDate();
        }
        if (field instanceof JComboBox) {
            JComboBox<?> cb = (JComboBox<?>) field;
            Object sel = cb.getSelectedItem();
            Object editorText = null;
            if (cb.isEditable() && cb.getEditor() != null && cb.getEditor().getEditorComponent() instanceof JTextComponent) {
                editorText = ((JTextComponent) cb.getEditor().getEditorComponent()).getText();
            }
            return new Object[]{sel, editorText};
        }
        if (field instanceof JSpinner) {
            return ((JSpinner) field).getValue();
        }
        if (field instanceof JTextComponent) {
            return ((JTextComponent) field).getText();
        }
        if (field instanceof JToggleButton) {
            return ((JToggleButton) field).isSelected();
        }
        return null;
    }

    private static boolean hasFieldChanged(Component field, Object initial,
            JTable table, int tableRow, int tableCol, Object tableInitial) {
        if (table != null && tableRow >= 0 && tableCol >= 0
                && tableRow < table.getRowCount() && tableCol < table.getColumnCount()) {
            Object currentCell = table.getValueAt(tableRow, tableCol);
            if (!Objects.equals(String.valueOf(tableInitial), String.valueOf(currentCell))) {
                return true;
            }
        }
        if (field instanceof JSpinner) {
            JSpinner spinner = (JSpinner) field;
            try {
                spinner.commitEdit();
            } catch (Exception ignored) {
                // keep current value
            }
            return !Objects.equals(initial, spinner.getValue());
        }
        if (field instanceof JComboBox) {
            Object[] prev = initial instanceof Object[] ? (Object[]) initial : new Object[]{null, null};
            Object[] cur = (Object[]) captureValue(field);
            return !Objects.equals(prev[0], cur[0]) || !Objects.equals(prev[1], cur[1]);
        }
        return !Objects.equals(initial, captureValue(field));
    }
}
