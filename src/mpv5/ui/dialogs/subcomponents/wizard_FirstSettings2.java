package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JToolBar;
import mpv5.data.PropertyStore;
import mpv5.db.objects.User;
import mpv5.logging.Log;
import mpv5.ui.beans.LabeledTextField;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;
import mpv5.utils.ui.PanelUtils;

/**
 *
 *
 */
public class wizard_FirstSettings2 extends javax.swing.JPanel implements Wizardable {

   private static final long serialVersionUID = -8347532498124147821L;
   private WizardMaster master;
   private PropertyStore nactions;
   private List<JToolBar> nbars;

   public wizard_FirstSettings2(WizardMaster w) {
      this.master = w;
      initComponents();
      setModels();
   }

   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jPanel1 = new javax.swing.JPanel();
      jPanel2 = new javax.swing.JPanel();
      name = new mpv5.ui.beans.LabeledTextField();
      firstname = new mpv5.ui.beans.LabeledTextField();
      city = new mpv5.ui.beans.LabeledTextField();
      zip = new mpv5.ui.beans.LabeledTextField();
      street = new mpv5.ui.beans.LabeledTextField();
      business = new mpv5.ui.beans.LabeledTextField();
      email = new mpv5.ui.beans.LabeledTextField();
      phone = new mpv5.ui.beans.LabeledTextField();
      country = new mpv5.ui.beans.LabeledTextField();
      jScrollPane2 = new javax.swing.JScrollPane();
      jTextPane2 = new javax.swing.JTextPane();

      setBackground(new java.awt.Color(255, 255, 255));
      setName("Form"); // NOI18N
      setLayout(new java.awt.BorderLayout());

      jPanel1.setBackground(new java.awt.Color(255, 255, 255));
      java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();// NOI18N
      jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_FirstSettings2.jPanel1.border.title"))); // NOI18N
      jPanel1.setName("jPanel1"); // NOI18N

      jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_FirstSettings2.jPanel2.border.title"))); // NOI18N
      jPanel2.setName("jPanel2"); // NOI18N

      name.set_Label(bundle.getString("wizard_FirstSettings2.name._Label")); // NOI18N
      name.setName("name"); // NOI18N

      firstname.set_Label(bundle.getString("wizard_FirstSettings2.firstname._Label")); // NOI18N
      firstname.setName("firstname"); // NOI18N

      city.set_Label(bundle.getString("wizard_FirstSettings2.city._Label")); // NOI18N
      city.setName("city"); // NOI18N

      zip.set_Label(bundle.getString("wizard_FirstSettings2.zip._Label")); // NOI18N
      zip.setName("zip"); // NOI18N

      street.set_Label(bundle.getString("wizard_FirstSettings2.street._Label")); // NOI18N
      street.setName("street"); // NOI18N

      business.set_Label(bundle.getString("wizard_FirstSettings2.business._Label")); // NOI18N
      business.setName("business"); // NOI18N

      email.set_Label(bundle.getString("wizard_FirstSettings2.email._Label")); // NOI18N
      email.setName("email"); // NOI18N

      phone.set_Label(bundle.getString("wizard_FirstSettings2.phone._Label")); // NOI18N
      phone.setName("phone"); // NOI18N

      country.set_Label(bundle.getString("wizard_FirstSettings2.country._Label")); // NOI18N
      country.setName("country"); // NOI18N

      javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
      jPanel2.setLayout(jPanel2Layout);
      jPanel2Layout.setHorizontalGroup(
         jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
               .addComponent(business, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addComponent(firstname, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(5, 5, 5)
            .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))
         .addGroup(jPanel2Layout.createSequentialGroup()
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(jPanel2Layout.createSequentialGroup()
                  .addComponent(city, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(5, 5, 5)
                  .addComponent(zip, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addComponent(street, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(country, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addGroup(jPanel2Layout.createSequentialGroup()
                  .addComponent(phone, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(5, 5, 5)
                  .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(0, 0, Short.MAX_VALUE))
      );
      jPanel2Layout.setVerticalGroup(
         jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
            .addContainerGap(19, Short.MAX_VALUE)
            .addComponent(business, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
               .addComponent(name, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addComponent(firstname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(street, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(city, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(zip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(country, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(phone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap())
      );

      jScrollPane2.setName("jScrollPane2"); // NOI18N

      jTextPane2.setText(bundle.getString("wizard_FirstSettings2.jTextPane2.text")); // NOI18N
      jTextPane2.setName("jTextPane2"); // NOI18N
      jScrollPane2.setViewportView(jTextPane2);

      javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(5, 5, 5)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, 0))
      );

      add(jPanel1, java.awt.BorderLayout.CENTER);
   }// </editor-fold>//GEN-END:initComponents

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private mpv5.ui.beans.LabeledTextField business;
   private mpv5.ui.beans.LabeledTextField city;
   private mpv5.ui.beans.LabeledTextField country;
   private mpv5.ui.beans.LabeledTextField email;
   private mpv5.ui.beans.LabeledTextField firstname;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JScrollPane jScrollPane2;
   private javax.swing.JTextPane jTextPane2;
   private mpv5.ui.beans.LabeledTextField name;
   private mpv5.ui.beans.LabeledTextField phone;
   private mpv5.ui.beans.LabeledTextField street;
   private mpv5.ui.beans.LabeledTextField zip;
   // End of variables declaration//GEN-END:variables

   public boolean next() {
      setSettings();
      User.getCurrentUser().saveProperties();
      return true;
   }

   public boolean back() {
      return true;
   }

   public void load() {
   }

   private void setModels() {
      setValues(User.getCurrentUser().getProperties().getProperties("companyinfo."));
   }

   private void setSettings() {
      HashMap<String, String> m = PanelUtils.getSubComponentValues(jPanel2);
      Iterator<Map.Entry<String, String>> i = m.entrySet().iterator();
      while (i.hasNext()) {
         Map.Entry<String, String> it = i.next();
         User.getCurrentUser().setProperty("companyinfo." + it.getKey(), it.getValue());
      }
   }

   public void setValues(PropertyStore values) {

      try {
         for (Component comp : jPanel2.getComponents()) {
            for (String[] v : values.getList()) {
               if (comp.getName().equalsIgnoreCase(v[0])) {
                  ((LabeledTextField) comp).setText(v[1]);
               }
            }
         }
      } catch (Exception e) {
         Log.Debug(this, e);
      }
   }
}
