/*
 *      This is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License Version 2.1 as published by
 *      the Free Software Foundation, either version 2.1 of the License, or
 *      (at your option) any later version.
 *
 *      This is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *      GNU Lesser General Public License Version 2.1 for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License Version 2.1
 *      along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.dialogs.subcomponents;

import javax.swing.JDialog;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Item;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.panels.DataPanel;

/**
 *
 * @author andreas
 */
public class ItemNumberEditor extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private final Item item;
    private final JDialog dialog;
    private final DataPanel parent;

    /**
     * Creates new form ItemNumberEditor
     *
     * @param item
     * @param dialog
     * @param parent
     */
    public ItemNumberEditor(Item item, JDialog dialog, DataPanel parent) {
        initComponents();
        this.item = item;
        try {
            contact.setText(item.getContact().toString());
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }

        auto.setText(item.getFormatHandler().next());
        this.dialog = dialog;
        this.parent = parent;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        auto = new mpv5.ui.beans.LabeledTextField();
        manual = new mpv5.ui.beans.LabeledTextField();
        jButton1 = new javax.swing.JButton();
        contact = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ItemNumberEditor.border.title"))); // NOI18N
        setName(bundle.getString("ItemNumberEditor.name")); // NOI18N

        auto.set_Label(bundle.getString("ItemNumberEditor.auto._Label")); // NOI18N
        auto.setName(bundle.getString("ItemNumberEditor.auto.name")); // NOI18N

        manual.set_Label(bundle.getString("ItemNumberEditor.manual._Label")); // NOI18N
        manual.setName(bundle.getString("ItemNumberEditor.manual.name")); // NOI18N

        jButton1.setText(bundle.getString("ItemNumberEditor.jButton1.text")); // NOI18N
        jButton1.setName(bundle.getString("ItemNumberEditor.jButton1.name")); // NOI18N
        jButton1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jButton1ItemStateChanged(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        contact.setFont(contact.getFont().deriveFont(contact.getFont().getStyle() | java.awt.Font.BOLD));
        contact.setText(bundle.getString("ItemNumberEditor.contact.text")); // NOI18N
        contact.setName(bundle.getString("ItemNumberEditor.contact.name")); // NOI18N

        jButton2.setText(bundle.getString("ItemNumberEditor.jButton2.text")); // NOI18N
        jButton2.setName(bundle.getString("ItemNumberEditor.jButton2.name")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jScrollPane1.setName(bundle.getString("ItemNumberEditor.jScrollPane1.name")); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setName(bundle.getString("ItemNumberEditor.jTextArea1.name")); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1))
            .addComponent(auto, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
            .addComponent(manual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(contact, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(contact, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(auto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(manual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jButton2)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        test();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jButton1ItemStateChanged
    }//GEN-LAST:event_jButton1ItemStateChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (test()) {
            item.setCnumber(manual.getText());
            item.setCname(manual.getText());
            try {
                item.save();
            } catch (Exception e) {
                Log.Debug(e);
            } finally {
                parent.setDataOwner(item, true);
            }
            dialog.dispose();
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.beans.LabeledTextField auto;
    private javax.swing.JLabel contact;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private mpv5.ui.beans.LabeledTextField manual;
    // End of variables declaration//GEN-END:variables

    private boolean test() {
        String next = manual.getText();
        if (next == null || next.length() == 0) {
            next = auto.getText();
            manual.setText(next);
        }
        if (!QueryHandler.instanceOf().clone(item.getContext()).checkUniqueness("cnumber", next)) {
            jTextArea1.setText(Messages.VALUE_ALREADY_EXISTS + next);
        } else {
            jTextArea1.setText("OK!");
            return true;
        }

        return false;
    }
}
