/*
 * SearchPanel.java
 *
 * Created on Nov 30, 2008, 6:16:09 PM
 */
package mpv5.ui.dialogs.subcomponents;

import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.DatabaseConnection;
import mpv5.globals.Messages;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;

/**
 *
 * @author Administrator
 */
public class wizard_DBSettings_2 extends javax.swing.JPanel implements Wizardable {

    private WizardMaster master;

    public wizard_DBSettings_2(WizardMaster w) {
        this.master = w;
        initComponents();
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 351, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 279, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    public boolean next() {

        if (master.getStore().getProperty("driver").equals("Local (Derby)")) {
            master.setMessage(Messages.CONNECTION_PROBE + "Local (Derby)");

            try {
                if (new DatabaseConnection().connect(ConnectionTypeHandler.DERBY,
                        master.getStore().getProperty("user"),
                        master.getStore().getProperty("password"),
                        master.getStore().getProperty("url"), true)) {
                    master.setMessage(Messages.CONNECTION_VERIFIED);
                } else {
                    master.setMessage(Messages.CONNECTION_FAILED);
                }
            } catch (Exception ex) {
                master.setMessage(Messages.CONNECTION_FAILED);
                return false;
            }
        }

        return false;

    }

    public boolean back() {
        return true;
    }
}
