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
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import mpv5.YabsViewProxy;
import mpv5.globals.Messages;
import mpv5.i18n.LanguageManager;
import mpv5.ui.frames.MPView;
import mpv5.ui.modern.PrimaryButton;
import mpv5.ui.panels.DataPanel;

/**
 * Triggers a visible Save or Update button when pressing Ctrl+S on non-record
 * panels (such as settings applets in {@link mpv5.ui.panels.MPControlPanel} and
 * popup dialogs).
 */
public final class SaveShortcut {

    private static boolean installed;

    private SaveShortcut() {
    }

    /**
     * Installs a post-processor so Ctrl+S triggers a Save or Update button in
     * dialogs and non-record windows whenever no DataPanel or menu accelerator
     * already consumed the keystroke.
     */
    public static synchronized void install() {
        if (installed) {
            return;
        }
        installed = true;
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(e -> {
            if (e.isConsumed() || e.getID() != KeyEvent.KEY_PRESSED || !isCtrlS(e)) {
                return false;
            }
            Component focusOwner = e.getComponent();
            if (focusOwner == null) {
                focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            }
            // Do not interfere when focus is inside a DataPanel that owns a DatabaseObject
            DataPanel dp = focusOwner != null
                    ? (DataPanel) SwingUtilities.getAncestorOfClass(DataPanel.class, focusOwner)
                    : null;
            if (dp != null && dp.getDataOwner() != null) {
                return false;
            }
            Window win = focusOwner != null
                    ? SwingUtilities.getWindowAncestor(focusOwner)
                    : KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            if (win == null) {
                return false;
            }
            Container scope;
            if (win == YabsViewProxy.instance().getIdentifierFrame()) {
                Object tab = MPView.getShowingTab();
                scope = tab instanceof Container ? (Container) tab : null;
            } else {
                scope = win;
            }
            if (scope != null && triggerIn(scope, focusOwner)) {
                e.consume();
                return true;
            }
            return false;
        });
    }

    private static boolean isCtrlS(KeyEvent e) {
        if (e.getKeyCode() != KeyEvent.VK_S) {
            return false;
        }
        int mods = e.getModifiersEx() & (InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK
                | InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        return mods == InputEvent.CTRL_DOWN_MASK || mods == shortcut;
    }

    /**
     * Finds and clicks the best visible Save or Update button inside {@code target}.
     *
     * @param target a container (e.g. the currently showing tab or dialog)
     * @return true if a button was found and clicked
     */
    public static boolean triggerIn(Object target) {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        return target instanceof Container && triggerIn((Container) target, focusOwner);
    }

    /**
     * Commits any active table cell / spinner edit at {@code focusOwner}, then
     * finds and clicks the best visible Save or Update button inside {@code scope}.
     *
     * @param scope container to search
     * @param focusOwner currently focused component, may be null
     * @return true if a button was found and clicked
     */
    public static boolean triggerIn(Container scope, Component focusOwner) {
        if (scope == null) {
            return false;
        }
        AbstractButton button = findSaveButton(scope);
        if (button == null) {
            return false;
        }
        commitActiveEditor(focusOwner);
        button.doClick();
        return true;
    }

    private static void commitActiveEditor(Component focusOwner) {
        if (focusOwner == null) {
            return;
        }
        JTable table = focusOwner instanceof JTable
                ? (JTable) focusOwner
                : (JTable) SwingUtilities.getAncestorOfClass(JTable.class, focusOwner);
        if (table != null && table.isEditing() && table.getCellEditor() != null) {
            try {
                table.getCellEditor().stopCellEditing();
            } catch (Exception ignored) {
            }
        }
        JSpinner spinner = focusOwner instanceof JSpinner
                ? (JSpinner) focusOwner
                : (JSpinner) SwingUtilities.getAncestorOfClass(JSpinner.class, focusOwner);
        if (spinner != null) {
            try {
                spinner.commitEdit();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Locates the best visible, enabled Save or Update button within {@code scope}.
     *
     * @param scope container to search
     * @return matching button, or null if none
     */
    public static AbstractButton findSaveButton(Container scope) {
        if (scope == null) {
            return null;
        }
        List<AbstractButton> candidates = new ArrayList<AbstractButton>();
        collectButtons(scope, scope.isShowing(), candidates);
        AbstractButton best = null;
        int bestScore = 0;
        for (AbstractButton b : candidates) {
            int score = scoreButton(b);
            if (score > bestScore) {
                bestScore = score;
                best = b;
            }
        }
        return best;
    }

    private static void collectButtons(Container container, boolean requireShowing, List<AbstractButton> out) {
        for (Component c : container.getComponents()) {
            if (!c.isVisible()) {
                continue;
            }
            if (c instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) c;
                if (!(b instanceof JToggleButton) && !(b instanceof JMenuItem)
                        && b.isEnabled() && (!requireShowing || b.isShowing())) {
                    out.add(b);
                }
            } else if (c instanceof Container) {
                collectButtons((Container) c, requireShowing, out);
            }
        }
    }

    static int scoreButton(AbstractButton b) {
        if (b instanceof PrimaryButton && ((PrimaryButton) b).getColor() != null) {
            // Red / custom-colored danger primary buttons (e.g. Delete) are never Save
            return 0;
        }
        String label = normalize(b.getText());
        String tip = normalize(b.getToolTipText());
        if (label.isEmpty() && tip.isEmpty()) {
            return 0;
        }
        String check = label.isEmpty() ? tip : label;
        if (isExcluded(check)) {
            return 0;
        }
        int bonus = b instanceof PrimaryButton ? 50 : 0;

        // Tier 1: exact Save / Speichern
        if (isExactSave(check)) {
            return 400 + bonus;
        }
        // Tier 2: Save variant (e.g. "Save and assign", "Save & Run", "Änderungen speichern")
        if (check.startsWith("save ") || check.startsWith("save&") || check.startsWith("speichern ")
                || check.startsWith("speichern&") || check.endsWith(" speichern") || check.endsWith(" save")) {
            return 300 + bonus;
        }
        if (b instanceof PrimaryButton && hasSaveIcon(b)
                && (check.contains("save") || check.contains("speicher") || check.contains("book") || check.contains("buchen"))) {
            return 300 + bonus;
        }
        // Tier 3: Update / Aktualisieren
        if (check.equals("update") || check.equals("aktualisieren")
                || check.startsWith("update ") || check.startsWith("aktualisieren ")
                || check.endsWith(" aktualisieren") || check.endsWith(" update")) {
            return 200 + bonus;
        }
        // Tier 4: Apply / Übernehmen (fallback when no Save/Update button is present)
        if (check.equals("apply") || check.equals("\u00fcbernehmen") || check.equals("uebernehmen")) {
            return 100 + bonus;
        }
        return 0;
    }

    private static boolean isExcluded(String s) {
        return s.contains("save as") || s.contains("save new") || s.contains("save file")
                || s.contains("als neu") || s.contains("neu speichern") || s.contains("speichern unter")
                || s.contains("datei speichern") || s.contains("save (pdf)") || s.contains("als pdf")
                || s.contains("check for update") || s.contains("nach update") || s.contains("update check")
                || s.contains("auto update");
    }

    private static boolean isExactSave(String s) {
        if (s.equals("save") || s.equals("speichern")) {
            return true;
        }
        for (Messages m : new Messages[]{
            Messages.USERS_SAVE,
            Messages.ZIPBACKUP_SAVE,
            Messages.TRANSLATE_SAVE,
            Messages.NUMFMT_SAVE,
            Messages.SCRIPTS_SAVE
        }) {
            if (s.equals(normalize(m.toString()))) {
                return true;
            }
        }
        try {
            String menuSave = normalize(LanguageManager.getBundle().getString("MPView.jMenuItem13.text"));
            if (!menuSave.isEmpty() && s.equals(menuSave)) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static boolean hasSaveIcon(AbstractButton b) {
        Icon icon = b.getIcon();
        if (icon instanceof ImageIcon) {
            String desc = ((ImageIcon) icon).getDescription();
            return desc != null && desc.toLowerCase(Locale.ROOT).contains("filesave.png");
        }
        return false;
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        // Strip simple HTML wrappers if any, collapse whitespace, trim trailing ellipsis
        String s = text.replaceAll("<[^>]*>", " ").trim().toLowerCase(Locale.ROOT);
        s = s.replaceAll("\\s+", " ");
        while (s.endsWith(".") || s.endsWith("\u2026")) {
            s = s.substring(0, s.length() - 1).trim();
        }
        return s;
    }
}
