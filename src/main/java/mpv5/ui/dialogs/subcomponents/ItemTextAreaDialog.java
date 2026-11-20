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
import mpv5.utils.models.MPTableModel;

public class ItemTextAreaDialog extends javax.swing.JDialog implements KeyListener, DatabaseObejctReceiver {

    private JTable parentTable;
    private mpv5.db.objects.Product product;

    /**
     * Creates new form ItemTextAreaDialog
     */
    public ItemTextAreaDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setResizable(false);
        setUndecorated(true);
        initComponents();
        textArea.addKeyListener(this);
        bindKeys();
        cancelButton.setToolTipText("Esc");
        labeledCombobox1.setContext(Context.getProduct());
        labeledCombobox1.setSearchOnEnterEnabled(true);
        labeledCombobox1.setReceiver(this);
        labeledCombobox1.setToolTipText(Messages.SEARCHABLE.toString());

        labeledSpinner1.setValue(1);
        makeMovable();
    }

    /**
     * Undecorated, so: a border and a strip at the top to drag the dialog
     * (e.g. when it opens at the bottom of the screen)
     */
    private void makeMovable() {
        final java.awt.Color line = mpv5.ui.modern.ModernTheme.border();
        javax.swing.JPanel handle = new javax.swing.JPanel() {
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
        handle.setPreferredSize(new java.awt.Dimension(10, mpv5.ui.modern.ModernTheme.px(12)));
        handle.setBackground(mpv5.ui.modern.ModernTheme.blend(mpv5.ui.modern.ModernTheme.background(), mpv5.ui.modern.ModernTheme.cardBackground(), 0.5f));
        handle.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, line));
        handle.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.MOVE_CURSOR));
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
                }
            }
        };
        handle.addMouseListener(drag);
        handle.addMouseMotionListener(drag);

        javax.swing.JPanel root = new javax.swing.JPanel(new java.awt.BorderLayout());
        root.setBorder(javax.swing.BorderFactory.createLineBorder(line));
        root.add(handle, java.awt.BorderLayout.NORTH);
        root.add(jPanel1, java.awt.BorderLayout.CENTER);
        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        setContentPane(root);
        pack();
    }

    /**
     * As in a spreadsheet: Enter (or Ctrl+Enter) applies the text and closes
     * the dialog, Shift+Enter is a new line in the text
     */
    private void bindKeys() {
        //the cell editor (TextAreaCellEditor) applies the text on the command "OK",
        //not on the label of the button (which is "OK " in German)
        okButton.setActionCommand("OK");
        okButton.setToolTipText(Messages.ITEM_TEXT_KEYS.toString());
        textArea.setToolTipText(Messages.ITEM_TEXT_KEYS.toString());
        bindEnter(textArea, this::apply);
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
        ActionEvent actionEvent = new ActionEvent(okButton, ActionEvent.ACTION_PERFORMED, okButton.getActionCommand());
        for (ActionListener l : okButton.getActionListeners()) {
            l.actionPerformed(actionEvent);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            textArea.grabFocus();
        }
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

        jScrollPane1.setName("jScrollPane1"); // NOI18N

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
        //Enter, Ctrl+Enter, Shift+Enter: see bindKeys()
        if (e.getKeyCode() == e.VK_ESCAPE) {
            ActionListener[] listeners = cancelButton.getActionListeners();
            ActionEvent actionEvent = new ActionEvent(cancelButton, ActionEvent.ACTION_PERFORMED, cancelButton.getActionCommand());
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].actionPerformed(actionEvent);
            }
        }
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
    }

    private synchronized void setRow() {
        SubItem p;
        MPTableModel m = (MPTableModel) getParentTable().getModel();
        if (product == null) {
            p = SubItem.getFromModel(m, getParentTable().getSelectedRow());
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

        m.setRowAt(p.getRowData(getParentTable().getSelectedRow()), getParentTable().getSelectedRow(), 1, 14);
        labeledCombobox1.setSelectedIndex(-1);
        product = null;
        setVisible(false);
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

    public void setQuantity(Double valueOf) {
        labeledSpinner1.setValue(valueOf);
    }
}
