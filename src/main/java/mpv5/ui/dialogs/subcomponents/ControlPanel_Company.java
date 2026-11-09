package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
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

/**
 *
 * 
 */
public final class ControlPanel_Company extends JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "companyinfo";
    private PropertyStore oldvalues;
    private final java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();
    private final List<LabeledTextField> fields = new ArrayList<LabeledTextField>();
    private final LabeledTextField business = field("business", "ControlPanel_Company.business._Label", true);
    private final LabeledTextField name = field("name", "ControlPanel_Company.name._Label", false);
    private final LabeledTextField firstname = field("firstname", "ControlPanel_Company.firstname._Label", false);
    private final LabeledTextField street = field("street", "ControlPanel_Company.street._Label", true);
    private final LabeledTextField city = field("city", "ControlPanel_Company.city._Label", true);
    private final LabeledTextField zip = field("zip", "ControlPanel_Company.zip._Label", true);
    private final LabeledTextField country = field("country", "ControlPanel_Company.country._Label", true);
    private final LabeledTextField phoneprefix = field("phoneprefix", "ControlPanel_Company.phoneprefix._Label", false);
    private final LabeledTextField phone = field("phone", "ControlPanel_Company.phone._Label", true);
    private final LabeledTextField email = field("email", "ControlPanel_Company.email._Label", true);
    private final LabeledTextField fax = field("fax", "ControlPanel_Company.fax._Label", false);
    private final LabeledTextField taxauthority = field("taxauthority", "ControlPanel_Company.taxauthority._Label", false);
    private final LabeledTextField taxmandant = field("taxmandant", "ControlPanel_Company.taxmandant._Label", false);
    private final LabeledTextField taxadvsior = field("taxadvsior", "ControlPanel_Company.taxadvsior._Label", false);
    private final LabeledTextField taxadvjob = field("taxadvjob", "ControlPanel_Company.taxadvjob._Label", false);
    private final LabeledTextField taxnumber = field("taxnumber", "ControlPanel_Company.taxnumber._Label", false);
    private final LabeledTextField bankname = field("bankname", "ControlPanel_Company.bankname._Label", false);
    private final LabeledTextField banknumber = field("banknumber", "ControlPanel_Company.banknumber._Label", false);
    private final LabeledTextField account = field("account", "ControlPanel_Company.account._Label", true);
    private final LabeledTextField account1 = field("account1", "ControlPanel_Company.account1._Label", true);
    /**
     * Default payment term (days) of e-invoices, for contacts and templates
     * without their own; stored as companyinfo.payterm like the other fields
     */
    private final LabeledTextField payterm = new LabeledTextField();
    private final LabeledTextField skontopercent = new LabeledTextField();
    private final LabeledTextField skontodays = new LabeledTextField();
    private final JPanel actions;

    public ControlPanel_Company() {
        super(new BorderLayout());
        setName("Form");
        setOpaque(false);
        payterm.set_Label(Messages.COMPANY_PAYTERM.toString());
        payterm.getTextField().setToolTipText(Messages.COMPANY_PAYTERM_TOOLTIP.toString());
        payterm.setName("payterm"); // = companyinfo.payterm
        skontopercent.set_Label(Messages.COMPANY_SKONTO_PERCENT.toString());
        skontopercent.getTextField().setToolTipText(Messages.COMPANY_SKONTO_TOOLTIP.toString());
        skontopercent.setName(mpv5.utils.export.PaymentTerms.KEY_SKONTO_PERCENT);
        skontodays.set_Label(Messages.COMPANY_SKONTO_DAYS.toString());
        skontodays.getTextField().setToolTipText(Messages.COMPANY_SKONTO_TOOLTIP.toString());
        skontodays.setName(mpv5.utils.export.PaymentTerms.KEY_SKONTO_DAYS);
        fields.add(payterm);
        fields.add(skontopercent);
        fields.add(skontodays);

        Card company = new Card(Messages.COMPANY_CARD_COMPANY.toString())
                .row(Applets.field(business))
                .row(Applets.field(firstname), Applets.field(name))
                .row(Applets.field(street))
                .row(Applets.field(zip, 1), Applets.field(city, 2))
                .row(Applets.field(country));
        Card contact = new Card(Messages.COMPANY_CARD_CONTACT.toString())
                .row(Applets.field(phoneprefix, 1), Applets.field(phone, 2))
                .row(Applets.field(fax))
                .row(Applets.field(email));
        JButton accounts = new JButton(Messages.COMPANY_BANKACCOUNTS_BUTTON.toString());
        accounts.setToolTipText(Messages.COMPANY_BANKACCOUNTS_INFO.toString());
        accounts.addActionListener(e -> editBankAccounts());
        JPanel accountsRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        accountsRow.setOpaque(false);
        accountsRow.add(accounts);
        Card bank = new Card(Messages.COMPANY_CARD_BANK.toString())
                .row(Applets.field(account))
                .row(Applets.field(banknumber, 1), Applets.field(bankname, 2))
                .row(Applets.field(payterm), Applets.field(skontopercent), Applets.field(skontodays))
                .addFull(Applets.hint(Messages.COMPANY_BANK_HINT.toString()))
                .addFull(accountsRow);
        Card tax = new Card(Messages.COMPANY_CARD_TAX.toString())
                .row(Applets.field(account1))
                .row(Applets.field(taxnumber), Applets.field(taxauthority))
                .row(Applets.field(taxmandant))
                .row(Applets.field(taxadvsior), Applets.field(taxadvjob));
        String info = "* " + Messages.COMPANY_REQUIRED_LEGEND + "  \u00b7  " + bundle.getString("ControlPanel_Company.jTextArea1.text");
        add(Applets.page(info, 380, company, contact, bank, tax), BorderLayout.CENTER);

        //mark invalid values while typing
        FieldCheck.attach(account1, ZugpferdFile::isValidVatId, Messages.COMPANY_VATID_TOOLTIP.toString(),
                Messages.COMPANY_VATID_INVALID.toString());
        FieldCheck.attach(taxnumber, v -> !ZugpferdFile.isValidVatId(v), Messages.COMPANY_TAXNUMBER_TOOLTIP.toString(),
                Messages.COMPANY_TAXNUMBER_IS_VATID.toString());
        FieldCheck.attach(account, Iban::isValid, Messages.COMPANY_IBAN_TOOLTIP.toString(),
                Messages.COMPANY_IBAN_INVALID_HINT.toString());
        FieldCheck.attach(banknumber, Iban::isValidBic, Messages.COMPANY_BIC_TOOLTIP.toString(),
                Messages.COMPANY_BIC_INVALID_HINT.toString());

        JButton resetItems = Applets.danger(new JButton(bundle.getString("ControlPanel_Company.jButton2.text")));
        resetItems.addActionListener(e -> resetItems());
        JButton reset = new JButton(bundle.getString("ControlPanel_Company.jButton3.text"));
        reset.addActionListener(e -> reset());
        PrimaryButton save = new PrimaryButton(bundle.getString("ControlPanel_Company.jButton1.text"), "/images/16/filesave.png");
        save.addActionListener(e -> save());
        actions = Applets.actionBar(new javax.swing.JComponent[]{resetItems}, new javax.swing.JComponent[]{reset, save});
        add(actions, BorderLayout.SOUTH);

        setValues(User.getCurrentUser().getProperties().getProperties("companyinfo."));
    }

    private LabeledTextField field(String key, String label, boolean required) {
        LabeledTextField f = new LabeledTextField();
        f.setName(key);
        String l;
        try {
            l = bundle.getString(label);
        } catch (Exception e) {
            l = key;
        }
        //required for e-invoices
        f.set_Label(required ? l + " *" : l);
        fields.add(f);
        return f;
    }

    private void save() {
        String problems = checkSettings();
        if (!problems.isEmpty() && !Popup.Y_N_dialog(Messages.COMPANY_CHECK_DATA + "\n" + problems + "\n\n"
                + Messages.COMPANY_SAVE_ANYWAY)) {
            return;
        }
        setSettings();
        User.getCurrentUser().saveProperties();
        mpv5.YabsViewProxy.instance().addMessage(Messages.COMPANY_SAVED.toString());
    }

    private void resetItems() {
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

    private void setSettings() {
        for (LabeledTextField f : fields) {
            User.getCurrentUser().setProperty("companyinfo." + f.getName(), f.getText() == null ? "" : f.getText());
        }
    }

    @Override
    public Component getAndRemoveActionPanel() {
        remove(actions);
        validate();
        return actions;
    }

    @Override
    public void setValues(PropertyStore values) {
        oldvalues = values;
        try {
            for (LabeledTextField f : fields) {
                for (String[] v : values.getList()) {
                    if (f.getName().equalsIgnoreCase(v[0])) {
                        f.setText(v[1]);
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
        if (oldvalues != null) {
            setValues(oldvalues);
        }
    }
}
