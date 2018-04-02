package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.SwingUtilities;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.beans.LabeledTextField;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.ui.PanelUtils;

/**
 *
 * 
 */
public final class ControlPanel_Company extends javax.swing.JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "companyinfo";
    private PropertyStore oldvalues;
    private static ControlPanel_Company ident;

    public ControlPanel_Company() {
        initComponents();
        setValues(User.getCurrentUser().getProperties().getProperties("companyinfo."));
        setVisible(true);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        business = new mpv5.ui.beans.LabeledTextField();
        name = new mpv5.ui.beans.LabeledTextField();
        firstname = new mpv5.ui.beans.LabeledTextField();
        street = new mpv5.ui.beans.LabeledTextField();
        city = new mpv5.ui.beans.LabeledTextField();
        zip = new mpv5.ui.beans.LabeledTextField();
        country = new mpv5.ui.beans.LabeledTextField();
        phoneprefix = new mpv5.ui.beans.LabeledTextField();
        phone = new mpv5.ui.beans.LabeledTextField();
        email = new mpv5.ui.beans.LabeledTextField();
        fax = new mpv5.ui.beans.LabeledTextField();
        taxauthority = new mpv5.ui.beans.LabeledTextField();
        taxmandant = new mpv5.ui.beans.LabeledTextField();
        taxadvsior = new mpv5.ui.beans.LabeledTextField();
        taxadvjob = new mpv5.ui.beans.LabeledTextField();
        taxnumber = new mpv5.ui.beans.LabeledTextField();
        bankname = new mpv5.ui.beans.LabeledTextField();
        banknumber = new mpv5.ui.beans.LabeledTextField();
        account = new mpv5.ui.beans.LabeledTextField();
        account1 = new mpv5.ui.beans.LabeledTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        //\$2java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Company.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new java.awt.GridLayout(10, 2, 5, 1));

        business.set_Label(bundle.getString("ControlPanel_Company.business._Label")); // NOI18N
        business.setName("business"); // NOI18N
        jPanel3.add(business);

        name.set_Label(bundle.getString("ControlPanel_Company.name._Label")); // NOI18N
        name.setName("name"); // NOI18N
        jPanel3.add(name);

        firstname.set_Label(bundle.getString("ControlPanel_Company.firstname._Label")); // NOI18N
        firstname.setName("firstname"); // NOI18N
        jPanel3.add(firstname);

        street.set_Label(bundle.getString("ControlPanel_Company.street._Label")); // NOI18N
        street.setName("street"); // NOI18N
        jPanel3.add(street);

        city.set_Label(bundle.getString("ControlPanel_Company.city._Label")); // NOI18N
        city.setName("city"); // NOI18N
        jPanel3.add(city);

        zip.set_Label(bundle.getString("ControlPanel_Company.zip._Label")); // NOI18N
        zip.setName("zip"); // NOI18N
        jPanel3.add(zip);

        country.set_Label(bundle.getString("ControlPanel_Company.country._Label")); // NOI18N
        country.setName("country"); // NOI18N
        jPanel3.add(country);

        phoneprefix.set_Label(bundle.getString("ControlPanel_Company.phoneprefix._Label")); // NOI18N
        phoneprefix.setName("phoneprefix"); // NOI18N
        jPanel3.add(phoneprefix);

        phone.set_Label(bundle.getString("ControlPanel_Company.phone._Label")); // NOI18N
        phone.setName("phone"); // NOI18N
        jPanel3.add(phone);

        email.set_Label(bundle.getString("ControlPanel_Company.email._Label")); // NOI18N
        email.setName("email"); // NOI18N
        jPanel3.add(email);

        fax.set_Label(bundle.getString("ControlPanel_Company.fax._Label")); // NOI18N
        fax.setName("fax"); // NOI18N
        jPanel3.add(fax);

        taxauthority.set_Label(bundle.getString("ControlPanel_Company.taxauthority._Label")); // NOI18N
        taxauthority.setName("taxauthority"); // NOI18N
        jPanel3.add(taxauthority);

        taxmandant.set_Label(bundle.getString("ControlPanel_Company.taxmandant._Label")); // NOI18N
        taxmandant.setName("taxmandant"); // NOI18N
        jPanel3.add(taxmandant);

        taxadvsior.set_Label(bundle.getString("ControlPanel_Company.taxadvsior._Label")); // NOI18N
        taxadvsior.setName("taxadvsior"); // NOI18N
        jPanel3.add(taxadvsior);

        taxadvjob.set_Label(bundle.getString("ControlPanel_Company.taxadvjob._Label")); // NOI18N
        taxadvjob.setName("taxadvjob"); // NOI18N
        jPanel3.add(taxadvjob);

        taxnumber.set_Label(bundle.getString("ControlPanel_Company.taxnumber._Label")); // NOI18N
        taxnumber.setName("taxnumber"); // NOI18N
        jPanel3.add(taxnumber);

        bankname.set_Label(bundle.getString("ControlPanel_Company.bankname._Label")); // NOI18N
        bankname.setName("bankname"); // NOI18N
        jPanel3.add(bankname);

        banknumber.set_Label(bundle.getString("ControlPanel_Company.banknumber._Label")); // NOI18N
        banknumber.setName("banknumber"); // NOI18N
        jPanel3.add(banknumber);

        account.set_Label(bundle.getString("ControlPanel_Company.account._Label")); // NOI18N
        account.setName("account"); // NOI18N
        jPanel3.add(account);

        account1.set_Label(bundle.getString("ControlPanel_Company.account1._Label")); // NOI18N
        account1.setName("account1"); // NOI18N
        jPanel3.add(account1);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

//$2java.awt.Color(240, 240, 240));
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText(bundle.getString("ControlPanel_Company.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setName("jTextArea1"); // NOI18N
        jTextArea1.setOpaque(false);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 614, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 614, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(90, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(51, Short.MAX_VALUE))
        );

        add(jPanel2, java.awt.BorderLayout.CENTER);

//$2java.awt.Color(255, 255, 255));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

//$2java.awt.Color(255, 153, 153));
        jButton2.setText(bundle.getString("ControlPanel_Company.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2);

        jButton1.setText(bundle.getString("ControlPanel_Company.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);

        jButton3.setText(bundle.getString("ControlPanel_Company.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3);

        add(jPanel1, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        setSettings();
        User.getCurrentUser().saveProperties();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        reset();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (mpv5.ui.dialogs.Popup.Y_N_dialog("Really delete all invoices, orders and offers from the database? This is irreversible!!! Contacts and Products will not be affected.", Messages.ARE_YOU_SURE)) {
            if (Popup.Y_N_dialog(Messages.ARE_YOU_SURE)) {
                Popup.notice(QueryHandler.instanceOf().freeQuery("delete from items", MPSecurityManager.ADMINISTRATE, "Item reset done.").getMessage());
                final String fmessage = "ITEM RESET: removed all invoices, orders and offers from database";
                final String fdbid = "items";
                final int fids = 0;
                final int fgids = 1;

                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(fmessage, mpv5.db.objects.User.getCurrentUser().__getCname(), fdbid, fids, fgids);
                    }
                };
                SwingUtilities.invokeLater(runnable);
            }
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    @Override
    public void setValues(PropertyStore values) {
        oldvalues = values;
        try {
            for (Component comp : jPanel3.getComponents()) {
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

    @Override
    public String getUname() {
        return UNAME;
    }

    @Override
    public void reset() {
        setValues(oldvalues);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.beans.LabeledTextField account;
    private mpv5.ui.beans.LabeledTextField account1;
    private mpv5.ui.beans.LabeledTextField bankname;
    private mpv5.ui.beans.LabeledTextField banknumber;
    private mpv5.ui.beans.LabeledTextField business;
    private mpv5.ui.beans.LabeledTextField city;
    private mpv5.ui.beans.LabeledTextField country;
    private mpv5.ui.beans.LabeledTextField email;
    private mpv5.ui.beans.LabeledTextField fax;
    private mpv5.ui.beans.LabeledTextField firstname;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private mpv5.ui.beans.LabeledTextField name;
    private mpv5.ui.beans.LabeledTextField phone;
    private mpv5.ui.beans.LabeledTextField phoneprefix;
    private mpv5.ui.beans.LabeledTextField street;
    private mpv5.ui.beans.LabeledTextField taxadvjob;
    private mpv5.ui.beans.LabeledTextField taxadvsior;
    private mpv5.ui.beans.LabeledTextField taxauthority;
    private mpv5.ui.beans.LabeledTextField taxmandant;
    private mpv5.ui.beans.LabeledTextField taxnumber;
    private mpv5.ui.beans.LabeledTextField zip;
    // End of variables declaration//GEN-END:variables

    private void setSettings() {
        HashMap<String, String> m = PanelUtils.getSubComponentValues(jPanel3);
        Iterator<Entry<String, String>> i = m.entrySet().iterator();
        while (i.hasNext()) {
            Entry<String, String> it = i.next();
            User.getCurrentUser().setProperty("companyinfo." + it.getKey(), it.getValue());
        }
    }

    @Override
    public Component getAndRemoveActionPanel() {
        this.remove(jPanel1);
        validate();
        return jPanel1;
    }
}
