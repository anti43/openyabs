/*
 * This file is part of YaBS.
 * 
 *    YaBS is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 * 
 *    YaBS is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 * 
 *    You should have received a copy of the GNU General Public License
 *    along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.dialogs.subcomponents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import javax.swing.JTable;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Product;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPComboboxModel;
import mpv5.utils.models.MPTableModel;

public class ItemTextAreaDialog extends javax.swing.JDialog implements KeyListener, DatabaseObejctReceiver {

    private JTable parentTable;
    private mpv5.db.objects.Product product;
    private boolean userMoved;
    private int editedRow = -1;
    private boolean closing;
    private java.awt.event.AWTEventListener outsidePressListener;
    private boolean outsideListenerInstalled;
    private boolean docked;
    private boolean loadingState;
    private boolean dirty;
    private boolean quantityDirty;
    private boolean editorEditable = true;
    private Runnable pinToggleAction;
    private java.util.function.Consumer<Boolean> dockedApplyCallback;
    private Runnable dockedCancelCallback;
    private javax.swing.JPanel dialogRoot;
    private javax.swing.JPanel handlePanel;
    private javax.swing.JButton floatingPinButton;
    private static final String WIDTH_PROPERTY = "floatingwidth"; //NOI18N
    private static final String HEIGHT_PROPERTY = "floatingheight"; //NOI18N
    private javax.swing.JPanel searchesPanel;
    private javax.swing.JPanel footerPanel;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JLabel keyHintLabel;
    private java.awt.Dimension floatingSize;
    private java.awt.Dimension minFloatingSize;

    /**
     * Creates new form ItemTextAreaDialog
     */
    public ItemTextAreaDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setResizable(true);
        setUndecorated(true);
        initComponents();
        textArea.addKeyListener(this);
        cancelButton.setToolTipText("Esc");
        cancelButton.addActionListener(e -> {
            if (docked && !closing) {
                cancel();
            }
        });
        labeledCombobox1.setContext(Context.getProduct());
        labeledCombobox1.setSearchOnEnterEnabled(true);
        labeledCombobox1.setReceiver(this);
        labeledCombobox1.setToolTipText(Messages.SEARCHABLE.toString());
        setupProductGroups();

        labeledSpinner1.setValue(1);
        bindKeys();
        installDirtyTracking();
        installFocusLossHandler();
        makeMovable();
    }

    public boolean isUserMoved() {
        return userMoved;
    }

    public int getEditedRow() {
        return editedRow;
    }

    public void setEditedRow(int row) {
        this.editedRow = row;
    }

    public boolean isDocked() {
        return docked;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isComboPopupVisible() {
        return groupselect.getComboBox().isPopupVisible()
                || labeledCombobox1.getComboBox().isPopupVisible();
    }

    public void clearDirty() {
        dirty = false;
        quantityDirty = false;
    }

    public mpv5.db.objects.Product getSelectedProduct() {
        return product;
    }

    public Number getQuantityValue() {
        commitSpinnerEdit();
        Object val = labeledSpinner1.get_Value();
        if (val instanceof Number) {
            return (Number) val;
        }
        try {
            return new BigDecimal(String.valueOf(val));
        } catch (Exception e) {
            return Integer.valueOf(1);
        }
    }

    /**
     * @return the inner editor panel (shared between floating dialog and docked pane)
     */
    public javax.swing.JPanel getEditorPanel() {
        return jPanel1;
    }

    /**
     * Enables the pin toggle button in the floating dialog's drag header.
     *
     * @param action invoked when the user clicks the pin button, null to hide
     */
    public void setPinToggleAction(Runnable action) {
        this.pinToggleAction = action;
        if (handlePanel == null) {
            return;
        }
        if (floatingPinButton != null) {
            handlePanel.remove(floatingPinButton);
            floatingPinButton = null;
        }
        if (action != null) {
            floatingPinButton = createFlatPinButton(false, Messages.ITEM_TEXT_PIN.toString(), action);
            handlePanel.add(floatingPinButton, java.awt.BorderLayout.EAST);
            handlePanel.setPreferredSize(new java.awt.Dimension(10, mpv5.ui.modern.ModernTheme.px(22)));
        } else {
            handlePanel.setPreferredSize(new java.awt.Dimension(10, mpv5.ui.modern.ModernTheme.px(12)));
        }
        handlePanel.revalidate();
        handlePanel.repaint();
        if (!docked) {
            pack();
            applyFloatingSize();
        }
    }

    /**
     * Callbacks invoked on apply / cancel while docked.
     *
     * @param onApply receives {@code true} when table focus should be restored
     * @param onCancel invoked when editing is cancelled (Esc / Cancel button)
     */
    public void setDockedCallbacks(java.util.function.Consumer<Boolean> onApply, Runnable onCancel) {
        this.dockedApplyCallback = onApply;
        this.dockedCancelCallback = onCancel;
    }

    /**
     * Switches the editor between floating dialog mode and docked side-panel mode.
     *
     * @param docked true to detach editor content for docking, false to re-attach to this dialog
     */
    public void setDocked(boolean docked) {
        if (this.docked == docked) {
            return;
        }
        this.docked = docked;
        if (docked) {
            removeOutsidePressListener();
            if (isVisible()) {
                super.setVisible(false);
            }
            if (jPanel1.getParent() == dialogRoot) {
                dialogRoot.remove(jPanel1);
            }
            updateFooterLayout(true);
        } else {
            updateFooterLayout(false);
            java.awt.Container parent = jPanel1.getParent();
            if (parent != null && parent != dialogRoot) {
                parent.remove(jPanel1);
            }
            dialogRoot.add(jPanel1, java.awt.BorderLayout.CENTER);
            dialogRoot.revalidate();
            pack();
            applyFloatingSize();
        }
    }

    /**
     * Loads the given row's description and quantity into the editor without marking it dirty.
     *
     * @param row model row index
     * @param text current description text
     * @param quantity current quantity (null or 0 defaults to 1)
     */
    public void loadRowState(int row, String text, Number quantity) {
        loadingState = true;
        try {
            this.editedRow = row;
            this.product = null;
            labeledCombobox1.setSelectedIndex(-1);
            textArea.setText(text == null ? "" : text);
            textArea.setCaretPosition(textArea.getDocument().getLength());
            if (quantity != null && quantity.doubleValue() != 0d) {
                labeledSpinner1.setValue(quantity);
            } else {
                labeledSpinner1.setValue(1);
            }
            dirty = false;
            quantityDirty = false;
        } finally {
            loadingState = false;
        }
    }

    /**
     * Restores in-progress unsaved edits (e.g. when pinning while the floating dialog was open).
     */
    public void restoreInProgressState(int row, String text, Number quantity, mpv5.db.objects.Product selectedProduct,
            boolean wasDirty, boolean wasQuantityDirty) {
        loadingState = true;
        try {
            this.editedRow = row;
            this.product = selectedProduct;
            textArea.setText(text == null ? "" : text);
            textArea.setCaretPosition(textArea.getDocument().getLength());
            if (quantity != null && quantity.doubleValue() != 0d) {
                labeledSpinner1.setValue(quantity);
            } else {
                labeledSpinner1.setValue(1);
            }
            this.dirty = wasDirty;
            this.quantityDirty = wasQuantityDirty;
        } finally {
            loadingState = false;
        }
    }

    /**
     * Enables or disables editing in the editor controls (e.g. for locked/read-only documents).
     */
    public void setEditorEditable(boolean editable) {
        this.editorEditable = editable;
        textArea.setEditable(editable);
        groupselect.setEnabled(editable);
        groupselect.getComboBox().setEnabled(editable);
        labeledCombobox1.setEnabled(editable);
        labeledCombobox1.getComboBox().setEnabled(editable);
        labeledSpinner1.setEnabled(editable);
        okButton.setEnabled(editable);
        cancelButton.setEnabled(editable);
    }

    /**
     * Applies pending changes when docked and dirty.
     */
    public void applyIfDirty(boolean restoreTableFocus) {
        if (docked && dirty && editorEditable) {
            apply(restoreTableFocus);
        }
    }

    public void focusTextArea() {
        textArea.requestFocusInWindow();
    }

    public void focusProductSearch() {
        javax.swing.JComboBox<?> combo = labeledCombobox1.getComboBox();
        if (combo.getEditor() != null && combo.getEditor().getEditorComponent() != null) {
            combo.getEditor().getEditorComponent().requestFocusInWindow();
        } else {
            combo.requestFocusInWindow();
        }
    }

    /**
     * Creates a compact borderless pin/unpin button with a crisp vector icon.
     */
    public static javax.swing.JButton createFlatPinButton(boolean pinned, String tooltip, Runnable onClick) {
        javax.swing.JButton b = new javax.swing.JButton(createPinIcon(pinned));
        b.setToolTipText(tooltip);
        b.setFocusable(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        b.setMargin(new java.awt.Insets(0, mpv5.ui.modern.ModernTheme.px(4), 0, mpv5.ui.modern.ModernTheme.px(4)));
        b.addActionListener(e -> onClick.run());
        return b;
    }

    /**
     * Crisp HiDPI vector pushpin icon: tilted muted outline when unpinned,
     * upright accent-filled pin when pinned.
     */
    public static javax.swing.Icon createPinIcon(final boolean pinned) {
        final int size = mpv5.ui.modern.ModernTheme.px(14);
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
                double s = size / 14.0;
                g2.translate(x + size / 2.0, y + size / 2.0);
                if (!pinned) {
                    g2.rotate(Math.toRadians(35));
                }
                g2.scale(s, s);
                java.awt.Color color = pinned ? mpv5.ui.modern.ModernTheme.accent() : mpv5.ui.modern.ModernTheme.muted();
                g2.setColor(color);
                g2.setStroke(new java.awt.BasicStroke(1.4f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                // pin head (trapezoid)
                java.awt.geom.Path2D.Double head = new java.awt.geom.Path2D.Double();
                head.moveTo(-3.2, -5.5);
                head.lineTo(3.2, -5.5);
                head.lineTo(2.4, -1.2);
                head.lineTo(4.2, 0.8);
                head.lineTo(-4.2, 0.8);
                head.lineTo(-2.4, -1.2);
                head.closePath();
                if (pinned) {
                    g2.fill(head);
                } else {
                    g2.draw(head);
                }
                // needle
                g2.draw(new java.awt.geom.Line2D.Double(0, 0.8, 0, 5.8));
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * The property (per user) of the last selected product group
     */
    private static final String GROUP_PROPERTY = "productgroup"; //NOI18N
    private final mpv5.ui.beans.LabeledCombobox groupselect = new mpv5.ui.beans.LabeledCombobox();
    /**
     * The selected product group and its child groups, empty: all
     */
    private volatile java.util.Set<Integer> searchedGroups = java.util.Collections.emptySet();

    /**
     * A product group selection above the product search: the search finds
     * the products of the selected group and its child groups. The selection
     * is remembered per user.
     */
    private void setupProductGroups() {
        groupselect.set_Label(Messages.ITEM_TEXT_PRODUCTGROUP.toString());
        groupselect.setName("groupselect"); //NOI18N
        final java.util.Map<Integer, java.util.List<mpv5.db.objects.ProductGroup>> children = new java.util.HashMap<>();
        java.util.List<MPComboBoxModelItem> items = new java.util.ArrayList<>();
        items.add(new MPComboBoxModelItem(Integer.valueOf(0), Messages.ITEM_TEXT_PRODUCTGROUP_ALL.toString()));
        try {
            java.util.List<mpv5.db.objects.ProductGroup> groups = new java.util.ArrayList<>();
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getProductGroup())) {
                groups.add((mpv5.db.objects.ProductGroup) o);
            }
            java.util.Set<Integer> ids = new java.util.HashSet<>();
            for (mpv5.db.objects.ProductGroup g : groups) {
                ids.add(g.__getIDS());
            }
            java.util.List<mpv5.db.objects.ProductGroup> roots = new java.util.ArrayList<>();
            for (mpv5.db.objects.ProductGroup g : groups) {
                int parent = g.__getProductgroupsids();
                if (parent <= 0 || parent == g.__getIDS() || !ids.contains(parent)) {
                    roots.add(g);
                } else {
                    children.computeIfAbsent(parent, k -> new java.util.ArrayList<>()).add(g);
                }
            }
            java.util.Comparator<mpv5.db.objects.ProductGroup> byName = java.util.Comparator.comparing(
                    g -> String.valueOf(g.__getCname()), String.CASE_INSENSITIVE_ORDER);
            for (java.util.List<mpv5.db.objects.ProductGroup> l : children.values()) {
                l.sort(byName);
            }
            roots.sort(byName);
            for (mpv5.db.objects.ProductGroup g : roots) {
                addGroup(items, children, g, 0, new java.util.HashSet<>());
            }
        } catch (Exception ex) {
            Log.Debug(this, ex.getMessage());
        }
        groupselect.setModel(new MPComboboxModel(items.toArray(new MPComboBoxModelItem[0])));
        groupselect.getComboBox().setMaximumRowCount(20);

        int last = 0;
        try {
            String v = User.getCurrentUser().getProperties().getProperty(ItemTextAreaDialog.class.getSimpleName() + "$" + GROUP_PROPERTY);
            last = v == null || v.trim().isEmpty() || "null".equals(v) ? 0 : Integer.parseInt(v.trim());
        } catch (Exception ex) {
            last = 0;
        }
        groupselect.setSelectedIndex(0);
        if (last > 0 && MPComboBoxModelItem.getItemID(Integer.valueOf(last), groupselect.getModel()) >= 0) {
            groupselect.setSelectedItem(Integer.valueOf(last));
        }
        searchedGroups = withChildren(selectedGroup(), children);

        groupselect.getComboBox().addItemListener(e -> {
            if (e.getStateChange() != java.awt.event.ItemEvent.SELECTED) {
                return;
            }
            int id = selectedGroup();
            searchedGroups = withChildren(id, children);
            labeledCombobox1.resetSearch();
            try {
                User.getCurrentUser().getProperties().changeProperty(ItemTextAreaDialog.class.getSimpleName(), GROUP_PROPERTY, id);
            } catch (Exception ex) {
                Log.Debug(this, ex.getMessage());
            }
        });
        labeledCombobox1.setSearchRestriction("productgroupsids", () -> searchedGroups); //NOI18N

        //above the product search
        searchesPanel = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 0, mpv5.ui.modern.ModernTheme.px(4)));
        searchesPanel.setOpaque(false);
        searchesPanel.add(groupselect);
        searchesPanel.add(labeledCombobox1);
    }

    /**
     * Adds the group and its children (indented) to the items
     */
    private static void addGroup(java.util.List<MPComboBoxModelItem> items,
            java.util.Map<Integer, java.util.List<mpv5.db.objects.ProductGroup>> children,
            mpv5.db.objects.ProductGroup g, int depth, java.util.Set<Integer> seen) {
        if (!seen.add(g.__getIDS())) {
            return;
        }
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("    ");
        }
        items.add(new MPComboBoxModelItem(g.__getIDS(), indent + String.valueOf(g.__getCname())));
        for (mpv5.db.objects.ProductGroup c : children.getOrDefault(g.__getIDS(), java.util.Collections.emptyList())) {
            addGroup(items, children, c, depth + 1, seen);
        }
    }

    /**
     * @return the id of the selected product group, 0: all
     */
    private int selectedGroup() {
        MPComboBoxModelItem item = groupselect.getSelectedItem();
        if (item == null) {
            return 0;
        }
        try {
            return Integer.parseInt(item.getId());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /**
     * @param id a product group, 0: all
     * @return the group and all its child groups, empty for 0
     */
    static java.util.Set<Integer> withChildren(int id,
            java.util.Map<Integer, java.util.List<mpv5.db.objects.ProductGroup>> children) {
        if (id <= 0) {
            return java.util.Collections.emptySet();
        }
        java.util.Set<Integer> all = new java.util.LinkedHashSet<>();
        java.util.Deque<Integer> todo = new java.util.ArrayDeque<>();
        todo.add(id);
        while (!todo.isEmpty()) {
            Integer g = todo.poll();
            if (all.add(g)) {
                for (mpv5.db.objects.ProductGroup c : children.getOrDefault(g, java.util.Collections.emptyList())) {
                    todo.add(c.__getIDS());
                }
            }
        }
        return java.util.Collections.unmodifiableSet(all);
    }

    /**
     * Undecorated, so: a border and a strip at the top to drag the dialog
     * (e.g. when it opens at the bottom of the screen)
     */
    private void makeMovable() {
        final java.awt.Color line = mpv5.ui.modern.ModernTheme.border();
        handlePanel = new javax.swing.JPanel(new java.awt.BorderLayout()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                //a grip in the middle
                int d = Math.max(2, mpv5.ui.modern.ModernTheme.px(2));
                int gap = d * 2;
                int x = getWidth() / 2 - gap * 2;
                int y = getHeight() / 2 - gap / 2;
                g.setColor(mpv5.ui.modern.ModernTheme.muted());
                for (int i = 0; i < 5; i++) {
                    g.fillRect(x + i * gap, y, d, d);
                    g.fillRect(x + i * gap, y + gap, d, d);
                }
            }
        };
        handlePanel.setPreferredSize(new java.awt.Dimension(10, mpv5.ui.modern.ModernTheme.px(12)));
        handlePanel.setBackground(mpv5.ui.modern.ModernTheme.blend(mpv5.ui.modern.ModernTheme.background(), mpv5.ui.modern.ModernTheme.cardBackground(), 0.5f));
        handlePanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, line));
        handlePanel.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.MOVE_CURSOR));
        java.awt.event.MouseAdapter drag = new java.awt.event.MouseAdapter() {
            private java.awt.Point offset;

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                java.awt.Point p = e.getLocationOnScreen();
                offset = new java.awt.Point(p.x - getX(), p.y - getY());
            }

            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (offset != null) {
                    java.awt.Point p = e.getLocationOnScreen();
                    setLocation(p.x - offset.x, p.y - offset.y);
                    userMoved = true;
                }
            }
        };
        handlePanel.addMouseListener(drag);
        handlePanel.addMouseMotionListener(drag);

        textArea.setRows(6);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        mpv5.ui.modern.ModernTheme.roomy(textArea);

        keyHintLabel = new javax.swing.JLabel(Messages.ITEM_TEXT_KEYS.toString());
        keyHintLabel.setForeground(mpv5.ui.modern.ModernTheme.muted());
        java.awt.Font hintFont = keyHintLabel.getFont();
        if (hintFont != null) {
            keyHintLabel.setFont(hintFont.deriveFont(Math.max(10f, hintFont.getSize2D() - 1.5f)));
        }
        keyHintLabel.setToolTipText(Messages.ITEM_TEXT_KEYS.toString());
        keyHintLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(mpv5.ui.modern.ModernTheme.px(2), 0, 0, 0));

        javax.swing.JPanel textAreaPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        textAreaPanel.setOpaque(false);
        textAreaPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        textAreaPanel.add(keyHintLabel, java.awt.BorderLayout.SOUTH);

        buttonsPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, mpv5.ui.modern.ModernTheme.px(6), 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        footerPanel = new javax.swing.JPanel();
        footerPanel.setOpaque(false);

        javax.swing.JPanel controls = new javax.swing.JPanel(new java.awt.BorderLayout(0, mpv5.ui.modern.ModernTheme.px(6)));
        controls.setOpaque(false);
        controls.add(searchesPanel, java.awt.BorderLayout.NORTH);
        controls.add(footerPanel, java.awt.BorderLayout.SOUTH);

        jPanel1.removeAll();
        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.BorderLayout(0, mpv5.ui.modern.ModernTheme.px(6)));
        jPanel1.add(textAreaPanel, java.awt.BorderLayout.CENTER);
        jPanel1.add(controls, java.awt.BorderLayout.SOUTH);
        updateFooterLayout(false);

        dialogRoot = new javax.swing.JPanel(new java.awt.BorderLayout());
        dialogRoot.setBackground(mpv5.ui.modern.ModernTheme.cardBackground());
        dialogRoot.setBorder(javax.swing.BorderFactory.createLineBorder(line));
        dialogRoot.add(handlePanel, java.awt.BorderLayout.NORTH);
        dialogRoot.add(createResizeStrip(java.awt.Cursor.W_RESIZE_CURSOR, mpv5.ui.modern.ModernTheme.px(4), 0), java.awt.BorderLayout.WEST);
        dialogRoot.add(createResizeStrip(java.awt.Cursor.E_RESIZE_CURSOR, mpv5.ui.modern.ModernTheme.px(4), 0), java.awt.BorderLayout.EAST);
        dialogRoot.add(createBottomResizeBar(), java.awt.BorderLayout.SOUTH);
        dialogRoot.add(jPanel1, java.awt.BorderLayout.CENTER);
        setContentPane(dialogRoot);
        pack();
        java.awt.Dimension size = getSize();
        minFloatingSize = new java.awt.Dimension(
                Math.max(mpv5.ui.modern.ModernTheme.px(280), Math.min(size.width, mpv5.ui.modern.ModernTheme.px(320))),
                Math.max(mpv5.ui.modern.ModernTheme.px(200), size.height - mpv5.ui.modern.ModernTheme.px(48)));
        setMinimumSize(minFloatingSize);
        floatingSize = loadFloatingSize(new java.awt.Dimension(size.width + mpv5.ui.modern.ModernTheme.px(50), size.height));
        setSize(floatingSize);
    }

    private javax.swing.JPanel createResizeStrip(final int cursorType, int width, int height) {
        javax.swing.JPanel strip = new javax.swing.JPanel();
        strip.setOpaque(false);
        strip.setPreferredSize(new java.awt.Dimension(width, height));
        strip.setCursor(java.awt.Cursor.getPredefinedCursor(cursorType));
        installResizeHandler(strip, e -> cursorType);
        return strip;
    }

    private javax.swing.JPanel createBottomResizeBar() {
        final int corner = mpv5.ui.modern.ModernTheme.px(16);
        final int barHeight = mpv5.ui.modern.ModernTheme.px(12);
        javax.swing.JPanel bar = new javax.swing.JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mpv5.ui.modern.ModernTheme.muted());
                int w = getWidth();
                int h = getHeight();
                int step = Math.max(3, mpv5.ui.modern.ModernTheme.px(3));
                int pad = Math.max(2, mpv5.ui.modern.ModernTheme.px(2));
                for (int i = 1; i <= 3; i++) {
                    int len = i * step;
                    g2.drawLine(w - pad - len, h - pad, w - pad, h - pad - len);
                }
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new java.awt.Dimension(10, barHeight));
        installResizeHandler(bar, e -> {
            if (e.getX() >= bar.getWidth() - corner) {
                return java.awt.Cursor.SE_RESIZE_CURSOR;
            }
            if (e.getX() <= corner) {
                return java.awt.Cursor.SW_RESIZE_CURSOR;
            }
            return java.awt.Cursor.S_RESIZE_CURSOR;
        });
        return bar;
    }

    private void installResizeHandler(final javax.swing.JComponent target,
            final java.util.function.ToIntFunction<java.awt.event.MouseEvent> cursorForEvent) {
        java.awt.event.MouseAdapter adapter = new java.awt.event.MouseAdapter() {
            private java.awt.Point startScreen;
            private java.awt.Rectangle startBounds;
            private int activeCursor;

            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                target.setCursor(java.awt.Cursor.getPredefinedCursor(cursorForEvent.applyAsInt(e)));
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (docked) {
                    return;
                }
                activeCursor = cursorForEvent.applyAsInt(e);
                startScreen = e.getLocationOnScreen();
                startBounds = getBounds();
            }

            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (docked || startScreen == null || startBounds == null) {
                    return;
                }
                java.awt.Point current = e.getLocationOnScreen();
                int dx = current.x - startScreen.x;
                int dy = current.y - startScreen.y;
                int minW = minFloatingSize != null ? minFloatingSize.width : mpv5.ui.modern.ModernTheme.px(280);
                int minH = minFloatingSize != null ? minFloatingSize.height : mpv5.ui.modern.ModernTheme.px(200);

                int x = startBounds.x;
                int y = startBounds.y;
                int w = startBounds.width;
                int h = startBounds.height;

                if (activeCursor == java.awt.Cursor.E_RESIZE_CURSOR || activeCursor == java.awt.Cursor.SE_RESIZE_CURSOR) {
                    w = Math.max(minW, startBounds.width + dx);
                }
                if (activeCursor == java.awt.Cursor.W_RESIZE_CURSOR || activeCursor == java.awt.Cursor.SW_RESIZE_CURSOR) {
                    int newW = Math.max(minW, startBounds.width - dx);
                    x = startBounds.x + (startBounds.width - newW);
                    w = newW;
                }
                if (activeCursor == java.awt.Cursor.S_RESIZE_CURSOR
                        || activeCursor == java.awt.Cursor.SE_RESIZE_CURSOR
                        || activeCursor == java.awt.Cursor.SW_RESIZE_CURSOR) {
                    h = Math.max(minH, startBounds.height + dy);
                }

                setBounds(x, y, w, h);
                floatingSize = new java.awt.Dimension(w, h);
                validate();
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (startBounds != null && floatingSize != null) {
                    saveFloatingSize(floatingSize);
                }
                startScreen = null;
                startBounds = null;
            }
        };
        target.addMouseListener(adapter);
        target.addMouseMotionListener(adapter);
    }

    private void applyFloatingSize() {
        if (floatingSize == null) {
            return;
        }
        int minW = minFloatingSize != null ? minFloatingSize.width : getWidth();
        int minH = minFloatingSize != null ? minFloatingSize.height : getHeight();
        setSize(Math.max(minW, floatingSize.width), Math.max(minH, floatingSize.height));
    }

    private java.awt.Dimension loadFloatingSize(java.awt.Dimension fallback) {
        try {
            mpv5.data.PropertyStore props = User.getCurrentUser().getProperties();
            String ws = props.getProperty(ItemTextAreaDialog.class.getSimpleName() + "$" + WIDTH_PROPERTY);
            String hs = props.getProperty(ItemTextAreaDialog.class.getSimpleName() + "$" + HEIGHT_PROPERTY);
            if (ws != null && hs != null) {
                int w = Integer.parseInt(ws.trim());
                int h = Integer.parseInt(hs.trim());
                if (w >= minFloatingSize.width && h >= minFloatingSize.height) {
                    return new java.awt.Dimension(w, h);
                }
            }
        } catch (Exception ignored) {
            // use default size
        }
        return fallback;
    }

    private void saveFloatingSize(java.awt.Dimension size) {
        try {
            User u = User.getCurrentUser();
            if (u != null) {
                u.getProperties().changeProperty(ItemTextAreaDialog.class.getSimpleName(), WIDTH_PROPERTY, size.width);
                u.getProperties().changeProperty(ItemTextAreaDialog.class.getSimpleName(), HEIGHT_PROPERTY, size.height);
            }
        } catch (Exception ex) {
            Log.Debug(this, ex.getMessage());
        }
    }

    private void updateFooterLayout(boolean isDocked) {
        if (footerPanel == null) {
            return;
        }
        footerPanel.removeAll();
        int side = mpv5.ui.modern.ModernTheme.px(8);
        if (isDocked) {
            footerPanel.setLayout(new java.awt.BorderLayout(0, mpv5.ui.modern.ModernTheme.px(6)));
            footerPanel.add(labeledSpinner1, java.awt.BorderLayout.NORTH);
            footerPanel.add(buttonsPanel, java.awt.BorderLayout.SOUTH);
            jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(
                    mpv5.ui.modern.ModernTheme.px(6), side, mpv5.ui.modern.ModernTheme.px(8), side));
        } else {
            footerPanel.setLayout(new java.awt.BorderLayout(mpv5.ui.modern.ModernTheme.px(8), 0));
            footerPanel.add(labeledSpinner1, java.awt.BorderLayout.WEST);
            footerPanel.add(buttonsPanel, java.awt.BorderLayout.EAST);
            jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(
                    mpv5.ui.modern.ModernTheme.px(6), mpv5.ui.modern.ModernTheme.px(4),
                    mpv5.ui.modern.ModernTheme.px(2), mpv5.ui.modern.ModernTheme.px(4)));
        }
        footerPanel.revalidate();
        footerPanel.repaint();
    }

    private void installDirtyTracking() {
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                if (!loadingState) {
                    dirty = true;
                }
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                if (!loadingState) {
                    dirty = true;
                }
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                if (!loadingState) {
                    dirty = true;
                }
            }
        });
        labeledSpinner1.getSpinner().addChangeListener(e -> {
            if (!loadingState) {
                dirty = true;
                quantityDirty = true;
            }
        });
    }

    /**
     * As in a spreadsheet: Enter (or Ctrl+Enter) applies the text and closes
     * the dialog, Shift+Enter is a new line in the text, Tab moves focus out
     * of the text area to the dropdowns and controls, Esc cancels from any
     * field (including the quantity spinner and product search).
     */
    private void bindKeys() {
        //the cell editor (TextAreaCellEditor) applies the text on the command "OK",
        //not on the label of the button (which is "OK " in German)
        okButton.setActionCommand("OK");
        okButton.setToolTipText(Messages.ITEM_TEXT_KEYS.toString());
        textArea.setToolTipText(Messages.ITEM_TEXT_KEYS.toString());
        bindEnter(textArea, this::apply);
        bindTab(textArea);

        javax.swing.Action cancelAction = new javax.swing.AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (groupselect.getComboBox().isPopupVisible()) {
                    groupselect.getComboBox().hidePopup();
                } else if (labeledCombobox1.getComboBox().isPopupVisible()) {
                    labeledCombobox1.getComboBox().hidePopup();
                } else {
                    cancel();
                }
            }
        };
        javax.swing.Action applyAction = new javax.swing.AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                apply();
            }
        };

        javax.swing.InputMap rootIm = getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap rootAm = getRootPane().getActionMap();
        rootIm.put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "yabs.cancel");
        rootIm.put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.event.InputEvent.CTRL_DOWN_MASK), "yabs.apply");
        rootIm.put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.event.InputEvent.META_DOWN_MASK), "yabs.apply");
        rootAm.put("yabs.cancel", cancelAction);
        rootAm.put("yabs.apply", applyAction);

        // Also bind on jPanel1 (scoped to ancestor of focused component) so shortcuts work when docked in a parent window.
        javax.swing.InputMap panelIm = jPanel1.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        javax.swing.ActionMap panelAm = jPanel1.getActionMap();
        panelIm.put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "yabs.cancel");
        panelIm.put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.event.InputEvent.CTRL_DOWN_MASK), "yabs.apply");
        panelIm.put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.event.InputEvent.META_DOWN_MASK), "yabs.apply");
        panelAm.put("yabs.cancel", cancelAction);
        panelAm.put("yabs.apply", applyAction);

        // JFormattedTextField inside JSpinner consumes ESCAPE (reset-field-edit) in WHEN_FOCUSED,
        // and JComboBox editors may also intercept keys: bind directly on them so Esc and Ctrl+Enter always work.
        javax.swing.JComponent spinnerEditor = labeledSpinner1.getSpinner().getEditor();
        if (spinnerEditor instanceof javax.swing.JSpinner.DefaultEditor) {
            bindDialogShortcuts(((javax.swing.JSpinner.DefaultEditor) spinnerEditor).getTextField(), applyAction, cancelAction);
        }
        bindComboEditorShortcuts(labeledCombobox1.getComboBox(), applyAction, cancelAction);
        bindComboEditorShortcuts(groupselect.getComboBox(), applyAction, cancelAction);
    }

    static void bindDialogShortcuts(javax.swing.JComponent comp, javax.swing.Action applyAction, javax.swing.Action cancelAction) {
        if (comp == null) {
            return;
        }
        javax.swing.InputMap im = comp.getInputMap(javax.swing.JComponent.WHEN_FOCUSED);
        javax.swing.ActionMap am = comp.getActionMap();
        im.put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "yabs.cancel");
        im.put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.event.InputEvent.CTRL_DOWN_MASK), "yabs.apply");
        im.put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.event.InputEvent.META_DOWN_MASK), "yabs.apply");
        am.put("yabs.cancel", cancelAction);
        am.put("yabs.apply", applyAction);
    }

    private static void bindComboEditorShortcuts(javax.swing.JComboBox<?> combo, javax.swing.Action applyAction, javax.swing.Action cancelAction) {
        if (combo == null) {
            return;
        }
        bindDialogShortcuts(combo, applyAction, cancelAction);
        if (combo.getEditor() != null && combo.getEditor().getEditorComponent() instanceof javax.swing.JComponent) {
            bindDialogShortcuts((javax.swing.JComponent) combo.getEditor().getEditorComponent(), applyAction, cancelAction);
        }
    }

    /**
     * Dismiss the popup only when the user presses the mouse outside this
     * dialog and its owned windows. This avoids closing it just because focus
     * changes during popup initialization or when moving between fields.
     */
    private void installFocusLossHandler() {
        outsidePressListener = event -> {
            if (!(event instanceof java.awt.event.MouseEvent)) {
                return;
            }
            java.awt.event.MouseEvent mouseEvent = (java.awt.event.MouseEvent) event;
            if (mouseEvent.getID() != java.awt.event.MouseEvent.MOUSE_PRESSED
                    || docked || !isVisible() || closing) {
                return;
            }

            if (groupselect.getComboBox().isPopupVisible()
                    || labeledCombobox1.getComboBox().isPopupVisible()) {
                return;
            }

            java.awt.Component source = mouseEvent.getComponent();
            if (source == null) {
                return;
            }

            java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(source);
            if (isOwnedByThis(window) || javax.swing.SwingUtilities.isDescendingFrom(source, this)) {
                return;
            }

            javax.swing.SwingUtilities.invokeLater(() -> {
                if (!docked && isVisible() && !closing) {
                    apply(false);
                }
            });
        };
    }

    private void installOutsidePressListener() {
        if (outsideListenerInstalled) {
            return;
        }
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(
                outsidePressListener,
                java.awt.AWTEvent.MOUSE_EVENT_MASK);
        outsideListenerInstalled = true;
    }

    private void removeOutsidePressListener() {
        if (!outsideListenerInstalled) {
            return;
        }
        java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(outsidePressListener);
        outsideListenerInstalled = false;
    }

    private boolean isOwnedByThis(java.awt.Window window) {
        for (java.awt.Window current = window; current != null; current = current.getOwner()) {
            if (current == this) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tab and Shift+Tab move focus out of the text area so the product group
     * dropdown, product search, quantity spinner and buttons can be reached by
     * keyboard.
     *
     * @param area
     */
    static void bindTab(javax.swing.JTextArea area) {
        area.setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        area.setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
    }

    /**
     * Enter and Ctrl+Enter run apply, Shift+Enter inserts a new line
     *
     * @param area
     * @param apply
     */
    static void bindEnter(javax.swing.JTextArea area, final Runnable apply) {
        javax.swing.InputMap im = area.getInputMap(javax.swing.JComponent.WHEN_FOCUSED);
        im.put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "yabs.apply");
        im.put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.event.InputEvent.CTRL_DOWN_MASK), "yabs.apply");
        im.put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.event.InputEvent.META_DOWN_MASK), "yabs.apply");
        im.put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.event.InputEvent.SHIFT_DOWN_MASK), javax.swing.text.DefaultEditorKit.insertBreakAction);
        area.getActionMap().put("yabs.apply", new javax.swing.AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                apply.run();
            }
        });
    }

    /**
     * Applies the text to the position and closes the dialog (as the OK
     * button)
     */
    void apply() {
        apply(true);
    }

    void apply(boolean restoreTableFocus) {
        if (closing) {
            return;
        }
        if (docked) {
            if (!editorEditable) {
                return;
            }
            closing = true;
            try {
                setRow();
                clearDirty();
                if (dockedApplyCallback != null) {
                    dockedApplyCallback.accept(restoreTableFocus);
                }
            } finally {
                closing = false;
            }
            return;
        }
        closing = true;
        try {
            commitSpinnerEdit();
            String cmd = restoreTableFocus ? okButton.getActionCommand() : mpv5.utils.renderer.TextAreaCellEditor.OK_KEEP_FOCUS;
            ActionEvent actionEvent = new ActionEvent(okButton, ActionEvent.ACTION_PERFORMED, cmd);
            for (ActionListener l : okButton.getActionListeners()) {
                l.actionPerformed(actionEvent);
            }
        } finally {
            closing = false;
        }
    }

    private void commitSpinnerEdit() {
        try {
            labeledSpinner1.getSpinner().commitEdit();
        } catch (java.text.ParseException ignored) {
            // keep last valid spinner value
        }
    }

    /**
     * Cancels editing and closes the dialog (as the Cancel button)
     */
    void cancel() {
        if (closing) {
            return;
        }
        if (docked) {
            closing = true;
            try {
                product = null;
                labeledCombobox1.setSelectedIndex(-1);
                clearDirty();
                if (dockedCancelCallback != null) {
                    dockedCancelCallback.run();
                }
            } finally {
                closing = false;
            }
            return;
        }
        closing = true;
        try {
            ActionListener[] listeners = cancelButton.getActionListeners();
            ActionEvent actionEvent = new ActionEvent(cancelButton, ActionEvent.ACTION_PERFORMED, cancelButton.getActionCommand());
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].actionPerformed(actionEvent);
            }
        } finally {
            closing = false;
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (docked && visible) {
            return;
        }
        if (visible) {
            dirty = false;
            quantityDirty = false;
            installOutsidePressListener();
        } else {
            removeOutsidePressListener();
        }

        super.setVisible(visible);

        if (visible) {
            javax.swing.SwingUtilities.invokeLater(textArea::grabFocus);
        }
    }

    @Override
    public void dispose() {
        removeOutsidePressListener();
        super.dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        labeledCombobox1 = new mpv5.ui.beans.LabeledCombobox();
        labeledSpinner1 = new mpv5.ui.beans.LabeledSpinner();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N;

        textArea.setColumns(20);
        textArea.setRows(5);
        textArea.setName("textArea"); // NOI18N
        jScrollPane1.setViewportView(textArea);

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();// NOI18N
        okButton.setText(bundle.getString("okButton")); // NOI18N
        okButton.setName("okButton"); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(bundle.getString("cancelButton")); // NOI18N
        cancelButton.setActionCommand("CANCEL"); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N

        labeledCombobox1.set_Label(bundle.getString("ItemTextAreaDialog.labeledCombobox1._Label")); // NOI18N
        labeledCombobox1.setName("labeledCombobox1"); // NOI18N

        labeledSpinner1.set_Label(bundle.getString("ItemTextAreaDialog.labeledSpinner1._Label")); // NOI18N
        labeledSpinner1.setName("labeledSpinner1"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(labeledSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 62, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)
                        .addGap(11, 11, 11))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                    .addComponent(labeledCombobox1, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(labeledCombobox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(okButton)
                    .addComponent(cancelButton)
                    .addComponent(labeledSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (docked) {
            if (!closing) {
                apply(true);
            }
            return;
        }
        setRow();
//        Log.Debug(this, evt);
    }//GEN-LAST:event_okButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton cancelButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox1;
    private mpv5.ui.beans.LabeledSpinner labeledSpinner1;
    public javax.swing.JButton okButton;
    public javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        //Enter, Ctrl+Enter, Shift+Enter, Tab, Esc: see bindKeys()
    }

    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void receive(DatabaseObject obj) {
        if (obj.getContext().equals(Context.getProduct())) {
            this.product = (Product) obj;
            textArea.setText(new SubItem(product).__getDescription());
        } else {
            textArea.setText(obj.__getCname());
        }
        dirty = true;
    }

    private synchronized void setRow() {
        commitSpinnerEdit();
        JTable table = getParentTable();
        if (table == null) {
            if (!docked) {
                setVisible(false);
            }
            return;
        }
        int row = editedRow >= 0 && editedRow < table.getRowCount() ? editedRow : table.getSelectedRow();
        if (row < 0) {
            if (!docked) {
                setVisible(false);
            }
            return;
        }
        MPTableModel m = (MPTableModel) table.getModel();
        if (docked && product == null) {
            if (quantityDirty) {
                BigDecimal quantity = new BigDecimal(labeledSpinner1.get_Value().toString());
                m.setValueAt(quantity, row, 2);
            }
            m.setValueAt(textArea.getText(), row, 4);
            labeledCombobox1.setSelectedIndex(-1);
            return;
        }
        SubItem p;
        if (product == null) {
            p = SubItem.getFromModel(m, row);
        } else {
            p = new SubItem(product);
        }
        BigDecimal quantity = new BigDecimal(labeledSpinner1.get_Value().toString());
        if (product != null) {
            //staggered price for the quantity (not for the position number)
            p.setExternalvalue(product.findPriceFor(quantity));
        }
        p.setDescription(textArea.getText());
        p.setQuantityvalue(quantity);

        m.setRowAt(p.getRowData(row), row, 1, 14);
        labeledCombobox1.setSelectedIndex(-1);
        product = null;
        if (!docked) {
            setVisible(false);
        }
    }

    /**
     * @return the parentTable
     */
    public JTable getParentTable() {
        return parentTable;
    }

    /**
     * @param parentTable the parentTable to set
     */
    public void setParentTable(JTable parentTable) {
        this.parentTable = parentTable;
    }

    public boolean isQuantityDirty() {
        return quantityDirty;
    }

    public void setQuantity(Double valueOf) {
        boolean prev = loadingState;
        loadingState = true;
        try {
            labeledSpinner1.setValue(valueOf);
        } finally {
            loadingState = prev;
        }
    }
}
