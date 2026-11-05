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
import mpv5.utils.export.ZugpferdFile;
import mpv5.utils.text.Iban;
import mpv5.utils.ui.FieldCheck;
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
        addPaytermField();
        markRequiredFields();
        //mark invalid values while typing
        FieldCheck.attach(account1, ZugpferdFile::isValidVatId, Messages.COMPANY_VATID_TOOLTIP.toString(),
                Messages.COMPANY_VATID_INVALID.toString());
        FieldCheck.attach(taxnumber, v -> !ZugpferdFile.isValidVatId(v), Messages.COMPANY_TAXNUMBER_TOOLTIP.toString(),
                Messages.COMPANY_TAXNUMBER_IS_VATID.toString());
        FieldCheck.attach(account, Iban::isValid, Messages.COMPANY_IBAN_TOOLTIP.toString(),
                Messages.COMPANY_IBAN_INVALID_HINT.toString());
        FieldCheck.attach(banknumber, Iban::isValidBic, Messages.COMPANY_BIC_TOOLTIP.toString(),
                Messages.COMPANY_BIC_INVALID_HINT.toString());
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

        String problems = checkSettings();
        if (!problems.isEmpty() && !Popup.Y_N_dialog(Messages.COMPANY_CHECK_DATA + "\n" + problems + "\n\n"
                + Messages.COMPANY_SAVE_ANYWAY)) {
            return;
        }
        setSettings();
        User.getCurrentUser().saveProperties();
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * Default payment term (days) of e-invoices, for contacts and templates
     * without their own; stored as companyinfo.payterm like the other fields
     */
    private final LabeledTextField payterm = new LabeledTextField();

    private final LabeledTextField skontopercent = new LabeledTextField();
    private final LabeledTextField skontodays = new LabeledTextField();

    /**
     * Defaults for the payment of invoices (contacts can override them in the
     * tab "E-Invoice"): payment term, Skonto and further bank accounts
     */
    private void addPaytermField() {
        java.awt.GridLayout grid = (java.awt.GridLayout) jPanel3.getLayout();
        grid.setRows(grid.getRows() + 2);
        payterm.set_Label(Messages.COMPANY_PAYTERM.toString());
        payterm.getTextField().setToolTipText(Messages.COMPANY_PAYTERM_TOOLTIP.toString());
        payterm.setName("payterm"); // NOI18N, = companyinfo.payterm
        jPanel3.add(payterm);
        javax.swing.JButton accounts = new javax.swing.JButton(Messages.COMPANY_BANKACCOUNTS_BUTTON.toString());
        accounts.setName("bankaccountsbutton"); // NOI18N, setValues() needs a name
        accounts.setToolTipText(Messages.COMPANY_BANKACCOUNTS_INFO.toString());
        accounts.addActionListener(e -> editBankAccounts());
        jPanel3.add(accounts);
        skontopercent.set_Label(Messages.COMPANY_SKONTO_PERCENT.toString());
        skontopercent.getTextField().setToolTipText(Messages.COMPANY_SKONTO_TOOLTIP.toString());
        skontopercent.setName(mpv5.utils.export.PaymentTerms.KEY_SKONTO_PERCENT); // NOI18N, = companyinfo.skontopercent
        jPanel3.add(skontopercent);
        skontodays.set_Label(Messages.COMPANY_SKONTO_DAYS.toString());
        skontodays.getTextField().setToolTipText(Messages.COMPANY_SKONTO_TOOLTIP.toString());
        skontodays.setName(mpv5.utils.export.PaymentTerms.KEY_SKONTO_DAYS); // NOI18N, = companyinfo.skontodays
        jPanel3.add(skontodays);
    }

    /**
     * Edit the further bank accounts (the first account is the one of the
     * fields IBAN / BIC / Bankname); stored as companyinfo.bankaccounts
     */
    private void editBankAccounts() {
        String key = "companyinfo." + mpv5.utils.export.BankAccount.KEY_ACCOUNTS;
        java.util.List<mpv5.utils.export.BankAccount> current
                = mpv5.utils.export.BankAccount.fromJson(User.getCurrentUser().getProperty(key));
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(new Object[]{
            Messages.BANKACCOUNT_NAME.toString(), "IBAN", "BIC", Messages.BANKACCOUNT_BANK.toString()}, 0);
        for (mpv5.utils.export.BankAccount a : current) {
            model.addRow(new Object[]{a.getName(), Iban.format(a.getIban()), a.getBic(), a.getBank()});
        }
        for (int i = 0; i < 5; i++) {
            model.addRow(new Object[]{"", "", "", ""});
        }
        javax.swing.JTable table = new javax.swing.JTable(model);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(table);
        scroll.setPreferredSize(new java.awt.Dimension(640, 180));
        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 6));
        javax.swing.JTextArea info = new javax.swing.JTextArea(Messages.COMPANY_BANKACCOUNTS_INFO.toString());
        info.setEditable(false);
        info.setOpaque(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setColumns(50);
        panel.add(info, java.awt.BorderLayout.NORTH);
        panel.add(scroll, java.awt.BorderLayout.CENTER);
        if (javax.swing.JOptionPane.showConfirmDialog(this, panel, Messages.COMPANY_BANKACCOUNTS_BUTTON.toString(),
                javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE) != javax.swing.JOptionPane.OK_OPTION) {
            return;
        }
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        java.util.List<mpv5.utils.export.BankAccount> list = new java.util.ArrayList<mpv5.utils.export.BankAccount>();
        StringBuilder invalid = new StringBuilder();
        for (int r = 0; r < model.getRowCount(); r++) {
            String iban = String.valueOf(model.getValueAt(r, 1)).trim();
            if (iban.isEmpty()) {
                continue;
            }
            String bic = String.valueOf(model.getValueAt(r, 2)).trim();
            if (!Iban.isValid(iban)) {
                invalid.append("\n - ").append(Messages.ZUGFERD_INVALID_IBAN).append(": ").append(iban);
            }
            if (!bic.isEmpty() && !Iban.isValidBic(bic)) {
                invalid.append("\n - ").append(Messages.ZUGFERD_INVALID_BIC).append(": ").append(bic);
            }
            list.add(new mpv5.utils.export.BankAccount(String.valueOf(model.getValueAt(r, 0)), iban, bic,
                    String.valueOf(model.getValueAt(r, 3))));
        }
        User.getCurrentUser().setProperty(key, mpv5.utils.export.BankAccount.toJson(list));
        User.getCurrentUser().saveProperties();
        if (invalid.length() > 0) {
            Popup.warn(Messages.COMPANY_CHECK_DATA + "\n" + invalid);
        }
    }

    /**
     * Mark the fields required for e-invoices with an asterisk and explain it
     * (done here and not in the language files, so it also works with
     * customized languages)
     */
    private void markRequiredFields() {
        for (LabeledTextField f : new LabeledTextField[]{business, street, zip, city, country, phone, email, account1,
            account}) {
            f.set_Label(f.get_Label() + " *");
        }
        jTextArea1.setText("* " + Messages.COMPANY_REQUIRED_LEGEND + "\n" + jTextArea1.getText());
        jTextArea1.setRows(jTextArea1.getRows() + 2);
    }

    /**
     * Check VAT ID, IBAN and BIC (they are used for e-invoices and QR codes)
     *
     * @return the problems, empty if everything is fine
     */
    private String checkSettings() {
        StringBuilder problems = new StringBuilder();
        //VAT ID: the most common mix-up is the VAT ID in the tax number field
        String vatValue = account1.getText() == null ? "" : account1.getText().trim();
        String taxValue = taxnumber.getText() == null ? "" : taxnumber.getText().trim();
        if (vatValue.isEmpty() && ZugpferdFile.isValidVatId(taxValue)) {
            problems.append("\n - ").append(Messages.COMPANY_VATID_IN_TAXNUMBER).append(" '").append(taxValue).append("'");
        } else if (!vatValue.isEmpty() && !ZugpferdFile.isValidVatId(vatValue)) {
            problems.append("\n - ").append(ZugpferdFile.isValidVatId(taxValue)
                    ? Messages.COMPANY_VATID_SWAPPED : Messages.COMPANY_VATID_INVALID).append(" '").append(vatValue).append("'");
        }
        String ibanValue = account.getText();
        if (ibanValue != null && !ibanValue.trim().isEmpty() && !Iban.isValid(ibanValue)) {
            problems.append("\n - ").append(Messages.ZUGFERD_INVALID_IBAN).append(": ").append(ibanValue);
        }
        String bicValue = banknumber.getText();
        if (bicValue != null && !bicValue.trim().isEmpty() && !Iban.isValidBic(bicValue)) {
            problems.append("\n - ").append(Messages.ZUGFERD_INVALID_BIC).append(": ").append(bicValue);
        }
        return problems.toString();
    }

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
