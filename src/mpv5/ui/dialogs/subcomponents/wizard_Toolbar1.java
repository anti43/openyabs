package mpv5.ui.dialogs.subcomponents;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import mpv5.Main;
import mpv5.data.PropertyStore;
import mpv5.db.objects.User;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.GlobalSettings;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;
import mpv5.ui.frames.MPView;

/**
 *
 *
 */
public class wizard_Toolbar1 extends javax.swing.JPanel implements Wizardable {

    private static final long serialVersionUID = -8347532498124147821L;
    private WizardMaster master;
    private PropertyStore nactions;
    private List<JToolBar> nbars;

    public wizard_Toolbar1(WizardMaster w) {
        this.master = w;
        initComponents();
        setModels();

    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        actions = new javax.swing.JList();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        script = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        bars = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        name = new mpv5.ui.beans.LabeledTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_Toolbar1.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel2.setText(bundle.getString("wizard_Toolbar1.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jButton1.setText(bundle.getString("wizard_Toolbar1.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        actions.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        actions.setName("actions"); // NOI18N
        actions.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                actionsValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(actions);

        jButton2.setText(bundle.getString("wizard_Toolbar1.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        jButton3.setText(bundle.getString("wizard_Toolbar1.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        script.setColumns(20);
        script.setRows(5);
        script.setName("script"); // NOI18N
        jScrollPane3.setViewportView(script);

        jScrollPane4.setMinimumSize(new java.awt.Dimension(50, 226));
        jScrollPane4.setName("jScrollPane4"); // NOI18N

        bars.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        bars.setName("bars"); // NOI18N
        jScrollPane4.setViewportView(bars);

        jLabel1.setText(bundle.getString("wizard_Toolbar1.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jButton4.setText(bundle.getString("wizard_Toolbar1.jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        name.set_Label(bundle.getString("wizard_Toolbar1.name._Label")); // NOI18N
        name.setName("name"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane3)
                            .addComponent(name, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1)
                        .addComponent(jButton4)
                        .addComponent(jButton2))
                    .addComponent(jButton3))
                .addContainerGap())
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        Object tb = bars.getSelectedValue();
        if (tb == null) {
            Notificator.raiseNotification("Select a toolbar!", true);
            return;
        }
        String n = name.getText(true, "My Action");
        String sc = script.getText();

        GlobalSettings.setProperty("org.openyabs.uiproperty.toolbaraction." + tb + "." + n, sc);
        GlobalSettings.save();
        Notificator.raiseNotification("OK!", true);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            String sc = script.getText();
            Binding binding = new Binding();
            binding.setVariable("view", MPView.identifierView);
            GroovyShell groovyShell = new GroovyShell(binding);
            groovyShell.evaluate(sc);
        } catch (Exception exception) {
            Notificator.raiseNotification(exception, true);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void actionsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_actionsValueChanged
        String tb = String.valueOf(actions.getSelectedValue());
        if (tb == null) {
            return;
        }
        String val = nactions.getProperty(tb);

        tb = tb.substring(1);
        String[] x = tb.split("\\.");
        String tbx = x[0];
        String act = x[1];
        
        script.setText(val);
        name.setText(act);
        bars.setSelectedValue(tbx, true);
    }//GEN-LAST:event_actionsValueChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList actions;
    private javax.swing.JList bars;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private mpv5.ui.beans.LabeledTextField name;
    private javax.swing.JTextArea script;
    // End of variables declaration//GEN-END:variables

    public boolean next() {

        return true;

    }

    public boolean back() {
        return false;
    }

    public void load() {
    }

    private void setModels() {
        DefaultListModel m = new DefaultListModel();
        nbars = Main.getApplication().getMainView().getToolBars();
        for (JToolBar t : nbars) {
            m.addElement(t.getName());
        }
        bars.setModel(m);

        DefaultListModel m2 = new DefaultListModel();
        nactions = GlobalSettings.getProperties("org.openyabs.uiproperty.toolbaraction");
        for (String f : nactions.getMap().keySet()) {
            m2.addElement(f);
        }
        actions.setModel(m2);
    }
}
