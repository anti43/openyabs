/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PropertyDialog.java
 *
 * Created on Sep 2, 2010, 3:02:18 PM
 */
package mpv5.ui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import mpv5.db.objects.User;
import mpv5.globals.GlobalSettings;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;

/**
 *
 * @author andreas.weber
 */
public class PropertyDialog extends javax.swing.JDialog {

    /**
     * Change a Global property
     */
    public static final int GLOBAL = 0;
    /**
     * Change a Local property
     */
    public static final int LOCAL = 1;
    /**
     * Change a User property
     */
    public static final int USER = 2;
    private final int scope;

    /** Creates new form PropertyDialog
     * @param scope
     * @param property
     * @param value
     */
    public PropertyDialog(int scope, String property, Object value) {
        initComponents();
        setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
        labeledTextField1.getTextField().setEditable(false);
        labeledTextField1.setText(Messages.PROPERTY);
        labeledTextField2.setText(Messages.VALUE);
        labeledTextField2.getTextField().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        labeledTextField1.setText(property);
        labeledTextField2.setText(value);
        this.scope = scope;
        setVisible(true);
    }

    @Override
    public void dispose() {
        save();
        super.dispose();
    }

    private void save() {
        switch (scope) {
            case GLOBAL:
                GlobalSettings.setProperty(labeledTextField1.getText(), labeledTextField2.getText());
                break;
            case LOCAL:
                LocalSettings.setProperty(labeledTextField1.getText(), labeledTextField2.getText());
                break;
            case USER:
                User.getCurrentUser().getProperties().changeProperty(labeledTextField1.getText(), labeledTextField2.getText());
                break;
            default:;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labeledTextField1 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField2 = new mpv5.ui.beans.LabeledTextField();

        setName("Form"); // NOI18N

        labeledTextField1.setName("labeledTextField1"); // NOI18N

        labeledTextField2.setName("labeledTextField2"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(labeledTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labeledTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.beans.LabeledTextField labeledTextField1;
    private mpv5.ui.beans.LabeledTextField labeledTextField2;
    // End of variables declaration//GEN-END:variables
}
