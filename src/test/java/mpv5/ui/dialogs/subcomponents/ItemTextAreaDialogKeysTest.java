package mpv5.ui.dialogs.subcomponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

/**
 * The text of a position: Enter applies it (as in a spreadsheet), Shift+Enter
 * is a new line
 */
class ItemTextAreaDialogKeysTest {

    private static void press(JTextArea area, int modifiers) {
        Object name = area.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, modifiers));
        assertNotNull(name, "Enter with modifiers " + modifiers + " bound");
        Action a = area.getActionMap().get(name);
        assertNotNull(a, "action " + name);
        a.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, ""));
    }

    @Test
    void enterAppliesShiftEnterIsANewLine() {
        JTextArea area = new JTextArea("Beratung");
        area.setCaretPosition(area.getText().length());
        AtomicInteger applied = new AtomicInteger();
        ItemTextAreaDialog.bindEnter(area, applied::incrementAndGet);

        press(area, InputEvent.SHIFT_DOWN_MASK);
        assertEquals("Beratung\n", area.getText(), "Shift+Enter: new line");
        assertEquals(0, applied.get(), "Shift+Enter does not apply");

        press(area, 0);
        assertEquals(1, applied.get(), "Enter applies");
        assertEquals("Beratung\n", area.getText(), "Enter: no new line");

        press(area, InputEvent.CTRL_DOWN_MASK);
        assertEquals(2, applied.get(), "Ctrl+Enter applies (as before)");
    }

    @Test
    void withChildrenIncludesNestedGroupsAndHandlesAllAndCycles() {
        mpv5.db.objects.ProductGroup root = group(1);
        mpv5.db.objects.ProductGroup child = group(2);
        mpv5.db.objects.ProductGroup grandChild = group(3);
        mpv5.db.objects.ProductGroup sibling = group(4);

        java.util.Map<Integer, java.util.List<mpv5.db.objects.ProductGroup>> children = new java.util.HashMap<>();
        children.put(1, java.util.Arrays.asList(child, sibling));
        children.put(2, java.util.Arrays.asList(grandChild, root)); // includes cycle back to root

        assertEquals(java.util.Collections.emptySet(), ItemTextAreaDialog.withChildren(0, children));
        assertEquals(new java.util.LinkedHashSet<>(java.util.Arrays.asList(1, 2, 4, 3)),
                ItemTextAreaDialog.withChildren(1, children));
        assertEquals(java.util.Collections.singleton(4), ItemTextAreaDialog.withChildren(4, children));
    }

    @Test
    void tabMovesFocusOutOfTextAreaToDropdowns() throws Exception {
        JTextArea area = new JTextArea();
        ItemTextAreaDialog.bindTab(area);

        java.util.Set<java.awt.AWTKeyStroke> forward = area.getFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        java.util.Set<java.awt.AWTKeyStroke> backward = area.getFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        org.junit.jupiter.api.Assertions.assertTrue(
                forward.contains(java.awt.AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0)),
                "Tab must transfer focus forward out of textArea");
        org.junit.jupiter.api.Assertions.assertTrue(
                backward.contains(java.awt.AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK)),
                "Shift+Tab must transfer focus backward out of textArea");

        // Verify LayoutFocusTraversalPolicy visits both dropdowns after textArea
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                javax.swing.JDialog dlg = new javax.swing.JDialog();
                try {
                    javax.swing.JPanel container = new javax.swing.JPanel(new java.awt.GridLayout(3, 1));
                    javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(area);
                    mpv5.ui.beans.LabeledCombobox groupselect = new mpv5.ui.beans.LabeledCombobox();
                    mpv5.ui.beans.LabeledCombobox productsearch = new mpv5.ui.beans.LabeledCombobox();
                    productsearch.setSearchOnEnterEnabled(true);
                    container.add(scroll);
                    container.add(groupselect);
                    container.add(productsearch);
                    dlg.setContentPane(container);
                    dlg.setLocation(-10000, -10000);
                    dlg.pack();
                    dlg.setVisible(true);

                    java.awt.FocusTraversalPolicy policy = dlg.getFocusTraversalPolicy();
                    java.awt.Component firstAfterArea = policy.getComponentAfter(dlg, area);
                    assertEquals(groupselect.getComboBox(), firstAfterArea,
                            "First Tab after textArea reaches product group dropdown");
                    java.awt.Component secondAfterArea = policy.getComponentAfter(dlg, firstAfterArea);
                    assertEquals(productsearch.getComboBox().getEditor().getEditorComponent(), secondAfterArea,
                            "Second Tab reaches product search dropdown");
                } finally {
                    dlg.dispose();
                }
            });
        }
    }

    private static mpv5.db.objects.ProductGroup group(int id) {
        mpv5.db.objects.ProductGroup g = new mpv5.db.objects.ProductGroup();
        g.setIDS(id);
        return g;
    }
}
