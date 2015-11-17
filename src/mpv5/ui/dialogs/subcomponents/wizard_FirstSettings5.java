package mpv5.ui.dialogs.subcomponents;

import java.util.List;
import javax.swing.JToolBar;
import mpv5.data.PropertyStore;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;

/**
 *
 *
 */
public class wizard_FirstSettings5 extends javax.swing.JPanel implements Wizardable {

    private static final long serialVersionUID = -8347532498124147821L;
    private WizardMaster master;
    private PropertyStore nactions;
    private List<JToolBar> nbars;

    public wizard_FirstSettings5(WizardMaster w) {
        this.master = w;
        initComponents();
        setModels();

    }

   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jPanel1 = new javax.swing.JPanel();
      jPanel2 = new javax.swing.JPanel();
      jScrollPane1 = new javax.swing.JScrollPane();
      jTextPane1 = new javax.swing.JTextPane();

      //\$2java.awt.Color(255, 255, 255));
      setName("Form"); // NOI18N
      setLayout(new java.awt.BorderLayout());

//$2java.awt.Color(255, 255, 255));
      java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
      jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_FirstSettings5.jPanel1.border.title"))); // NOI18N
      jPanel1.setName("jPanel1"); // NOI18N

      jPanel2.setName("jPanel2"); // NOI18N

      jScrollPane1.setName("jScrollPane1"); // NOI18N

      jTextPane1.setText(bundle.getString("wizard_FirstSettings5.jTextPane1.text")); // NOI18N
      jTextPane1.setName("jTextPane1"); // NOI18N
      jScrollPane1.setViewportView(jTextPane1);

      javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
      jPanel2.setLayout(jPanel2Layout);
      jPanel2Layout.setHorizontalGroup(
         jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
      );
      jPanel2Layout.setVerticalGroup(
         jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
      );

      javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 3, Short.MAX_VALUE))
      );

      add(jPanel1, java.awt.BorderLayout.CENTER);
   }// </editor-fold>//GEN-END:initComponents

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JTextPane jTextPane1;
   // End of variables declaration//GEN-END:variables

    public boolean next() {
        master.dispose();
        return false;
    }

    public boolean back() {
        return true;
    }

    public void load() {
    }

    private void setModels() {
    }
}
