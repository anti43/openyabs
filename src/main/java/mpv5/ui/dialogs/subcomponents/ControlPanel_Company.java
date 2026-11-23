package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import mpv5.Main;
import mpv5.i18n.LanguageManager;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.utils.models.MPComboBoxModelItem;
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
    private final JComboBox country = new JComboBox();
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
    //VAT ID or tax number: one of them is required for e-invoices (small businesses often have no VAT ID)
    private final LabeledTextField account1 = field("account1", "ControlPanel_Company.account1._Label", false);
    /**
     * Default payment term (days) of e-invoices, for contacts and templates
     * without their own; stored as companyinfo.payterm like the other fields
     */
    private final LabeledTextField payterm = new LabeledTextField();
    private final LabeledTextField skontopercent = new LabeledTextField();
    private final LabeledTextField skontodays = new LabeledTextField();
    /**
     * EU customs EORI number, stored as companyinfo.eori
     */
    private final LabeledTextField eori = new LabeledTextField();
    /**
     * Seller Peppol-ID (BT-34), stored as companyinfo.peppolid
     */
    private final LabeledTextField peppolid = new LabeledTextField();
    /**
     * SEPA creditor ID (for direct debits), stored as companyinfo.sepacreditorid
     */
    private final LabeledTextField creditorid = new LabeledTextField();
    /**
     * Small business VAT exemption (companyinfo.smallbusiness*), see
     * {@link mpv5.utils.export.SmallBusiness}
     */
    private final javax.swing.JCheckBox smallBusiness = new javax.swing.JCheckBox(Messages.COMPANY_SMALLBUSINESS.toString());
    private final LabeledTextField smallBusinessNote = new LabeledTextField();
    private final LabeledTextField smallBusinessId = new LabeledTextField();
    private final javax.swing.JCheckBox defaultZugpferd = new javax.swing.JCheckBox(Messages.COMPANY_DEFAULT_ZUGFERD.toString());
    /**
     * Invoice email archive (companyinfo.mailarchive.*), see
     * {@link mpv5.utils.export.InvoiceMailArchive}
     */
    private final LabeledTextField mailArchiveAddress = new LabeledTextField();
    private final LabeledTextField mailArchiveSubject = new LabeledTextField();
    private final javax.swing.JTextArea mailArchiveBody = new javax.swing.JTextArea(4, 30);
    private final JButton smtpSettingsBtn = new JButton(Messages.COMPANY_MAILARCHIVE_SMTP_BUTTON.toString() + " \u2026");
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
        creditorid.set_Label(Messages.COMPANY_CREDITOR_ID.toString());
        creditorid.setName(mpv5.utils.export.SepaDirectDebit.KEY_CREDITOR_ID);
        fields.add(creditorid);
        eori.set_Label(Messages.COMPANY_EORI.toString());
        eori.setName(ZugpferdFile.KEY_EORI);
        fields.add(eori);
        peppolid.set_Label(Messages.COMPANY_PEPPOL_ID.toString());
        peppolid.setName(ZugpferdFile.KEY_COMPANY_PEPPOL_ID);
        fields.add(peppolid);

        smallBusinessNote.set_Label(Messages.COMPANY_SMALLBUSINESS_NOTE.toString());
        smallBusinessNote.setName(mpv5.utils.export.SmallBusiness.KEY_NOTE);
        smallBusinessId.set_Label(Messages.COMPANY_SMALLBUSINESS_ID.toString());
        smallBusinessId.setName(mpv5.utils.export.SmallBusiness.KEY_KU_ID);
        fields.add(smallBusinessNote);
        fields.add(smallBusinessId);
        smallBusiness.setOpaque(false);
        smallBusiness.setToolTipText(Messages.COMPANY_SMALLBUSINESS_HINT.toString());
        smallBusiness.addActionListener(e -> showDefaultNote());

        mailArchiveAddress.set_Label(Messages.COMPANY_MAILARCHIVE_ADDRESS.toString());
        mailArchiveAddress.setName(mpv5.utils.export.InvoiceMailArchive.KEY_ADDRESS);
        mailArchiveAddress.getTextField().setToolTipText(Messages.COMPANY_MAILARCHIVE_ADDRESS_TOOLTIP.toString());
        mailArchiveSubject.set_Label(Messages.COMPANY_MAILARCHIVE_SUBJECT.toString());
        mailArchiveSubject.setName(mpv5.utils.export.InvoiceMailArchive.KEY_SUBJECT);
        mailArchiveSubject.getTextField().setToolTipText(mpv5.utils.export.InvoiceMailArchive.DEFAULT_SUBJECT);
        fields.add(mailArchiveAddress);
        fields.add(mailArchiveSubject);
        mailArchiveBody.setName(mpv5.utils.export.InvoiceMailArchive.KEY_BODY);
        mailArchiveBody.setLineWrap(true);
        mailArchiveBody.setWrapStyleWord(true);
        mailArchiveBody.setToolTipText(mpv5.utils.export.InvoiceMailArchive.DEFAULT_BODY);
        javax.swing.JScrollPane mailArchiveBodyScroll = new javax.swing.JScrollPane(mailArchiveBody);
        mailArchiveBodyScroll.setPreferredSize(new java.awt.Dimension(280, 76));

        ModernTheme.roomyCombo(country);
        loadCountries();

        String countryLabel;
        try {
            countryLabel = bundle.getString("ControlPanel_Company.country._Label");
        } catch (Exception e) {
            countryLabel = "Country";
        }
        countryLabel += " *";

        Card company = new Card(Messages.COMPANY_CARD_COMPANY.toString())
                .row(Applets.field(business))
                .row(Applets.field(firstname), Applets.field(name))
                .row(Applets.field(street))
                .row(Applets.field(zip, 1), Applets.field(city, 2))
                .row(Card.field(countryLabel, country));
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
                .row(Applets.field(creditorid))
                .addFull(Applets.hint(Messages.COMPANY_BANK_HINT.toString()))
                .addFull(accountsRow);
        Card tax = new Card(Messages.COMPANY_CARD_TAX.toString())
                .row(Applets.field(account1), Applets.field(eori))
                .row(Applets.field(taxnumber), Applets.field(taxauthority))
                .row(Applets.field(taxmandant))
                .row(Applets.field(taxadvsior), Applets.field(taxadvjob));
        Card smallBusinessCard = new Card(Messages.COMPANY_CARD_SMALLBUSINESS.toString())
                .addFull(smallBusiness)
                .row(Applets.field(smallBusinessNote))
                .row(Applets.field(smallBusinessId))
                .addFull(Applets.hint(Messages.COMPANY_SMALLBUSINESS_HINT.toString()));
        defaultZugpferd.setToolTipText(Messages.COMPANY_DEFAULT_ZUGFERD_HINT.toString());
        defaultZugpferd.setOpaque(false);
        JButton foldersBtn = new JButton(Messages.FOLDERS_TITLE.toString() + " \u2026");
        foldersBtn.setIcon(new mpv5.utils.images.MPIcon("/images/22/folder_yellow.png").getIcon(16));
        foldersBtn.addActionListener(e -> mpv5.ui.panels.MPControlPanel.show(ControlPanel_Folders.class));
        JPanel foldersRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        foldersRow.setOpaque(false);
        foldersRow.add(foldersBtn);
        Card einvoice = new Card(Messages.COMPANY_CARD_EINVOICE.toString())
                .row(Applets.field(peppolid))
                .addFull(defaultZugpferd)
                .addFull(Applets.hint(Messages.COMPANY_DEFAULT_ZUGFERD_HINT.toString()))
                .addFull(foldersRow);
        smtpSettingsBtn.setIcon(new mpv5.utils.images.MPIcon("/images/22/kontact_mail.png").getIcon(16));
        smtpSettingsBtn.addActionListener(e -> ControlPanel_Userproperties.openSmtpSettings());
        JPanel smtpRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        smtpRow.setOpaque(false);
        smtpRow.add(smtpSettingsBtn);
        Card mailArchiveCard = new Card(Messages.COMPANY_CARD_MAILARCHIVE.toString())
                .row(Applets.field(mailArchiveAddress))
                .row(Applets.field(mailArchiveSubject))
                .row(Card.field(Messages.COMPANY_MAILARCHIVE_BODY.toString(), mailArchiveBodyScroll))
                .addFull(Applets.hint(Messages.COMPANY_MAILARCHIVE_HINT.toString()))
                .addFull(smtpRow);
        String info = "* " + Messages.COMPANY_REQUIRED_LEGEND + "  \u00b7  " + bundle.getString("ControlPanel_Company.jTextArea1.text");
        add(Applets.page(info, 380, company, contact, bank, tax, smallBusinessCard, einvoice, mailArchiveCard), BorderLayout.CENTER);

        //mark invalid values while typing
        FieldCheck.attach(account1, ZugpferdFile::isValidVatId, Messages.COMPANY_VATID_TOOLTIP.toString(),
                Messages.COMPANY_VATID_INVALID.toString());
        FieldCheck.attach(eori, ZugpferdFile::isValidEori, Messages.COMPANY_EORI_TOOLTIP.toString(),
                Messages.COMPANY_EORI_INVALID.toString());
        FieldCheck.attach(peppolid, ZugpferdFile::isValidPeppolId, Messages.COMPANY_PEPPOL_ID_TOOLTIP.toString(),
                Messages.COMPANY_PEPPOL_ID_INVALID.toString());
        FieldCheck.attach(taxnumber, v -> !ZugpferdFile.isValidVatId(v), Messages.COMPANY_TAXNUMBER_TOOLTIP.toString(),
                Messages.COMPANY_TAXNUMBER_IS_VATID.toString());
        FieldCheck.attach(account, Iban::isValid, Messages.COMPANY_IBAN_TOOLTIP.toString(),
                Messages.COMPANY_IBAN_INVALID_HINT.toString());
        FieldCheck.attach(creditorid, mpv5.utils.export.SepaDirectDebit::isValidCreditorId,
                Messages.COMPANY_CREDITOR_ID_TOOLTIP.toString(), Messages.COMPANY_CREDITOR_ID_INVALID.toString());
        FieldCheck.attach(banknumber, Iban::isValidBic, Messages.COMPANY_BIC_TOOLTIP.toString(),
                Messages.COMPANY_BIC_INVALID_HINT.toString());
        FieldCheck.attach(mailArchiveAddress, mpv5.utils.text.TypeConversion::isMailList,
                Messages.COMPANY_MAILARCHIVE_ADDRESS_TOOLTIP.toString(), Messages.MAIL_COPY_INVALID.toString());

        JButton reset = new JButton(bundle.getString("ControlPanel_Company.jButton3.text"));
        reset.addActionListener(e -> reset());
        PrimaryButton save = new PrimaryButton(bundle.getString("ControlPanel_Company.jButton1.text"), "/images/16/filesave.png");
        save.addActionListener(e -> save());
        actions = Applets.actionBar(new javax.swing.JComponent[]{}, new javax.swing.JComponent[]{reset, save});
        add(actions, BorderLayout.SOUTH);

        setValues(mpv5.globals.CompanyInfo.getProperties());
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
        //e-invoices by default: the company info must be complete (it is the seller of every e-invoice)
        if (defaultZugpferd.isSelected()) {
            java.util.Map<String, String> values = new java.util.HashMap<String, String>();
            for (LabeledTextField f : fields) {
                values.put(f.getName(), f.getText() == null ? "" : f.getText());
            }
            values.put("country", getSelectedCountry());
            values.put(mpv5.utils.export.SmallBusiness.KEY_ENABLED, String.valueOf(smallBusiness.isSelected()));
            java.util.List<String> missing = ZugpferdFile.sellerProblems(ZugpferdFile.createSender(values::get));
            if (!missing.isEmpty()) {
                Popup.warn(Messages.EINVOICE_COMPANY_INCOMPLETE + "\n - " + String.join("\n - ", missing));
                return;
            }
        }
        String problems = checkSettings();
        if (!problems.isEmpty() && !Popup.Y_N_dialog(Messages.COMPANY_CHECK_DATA + "\n" + problems + "\n\n"
                + Messages.COMPANY_SAVE_ANYWAY)) {
            return;
        }
        setSettings();
        mpv5.globals.CompanyInfo.save();
        mpv5.YabsViewProxy.instance().addMessage(Messages.COMPANY_SAVED.toString());
    }

    /**
     * Edit the further bank accounts (the first account is the one of the
     * fields IBAN / BIC / Bankname); stored as companyinfo.bankaccounts
     */
    private void editBankAccounts() {
        String key = mpv5.utils.export.BankAccount.KEY_ACCOUNTS;
        java.util.List<mpv5.utils.export.BankAccount> current
                = mpv5.utils.export.BankAccount.fromJson(mpv5.globals.CompanyInfo.getProperty(key));
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
        mpv5.globals.CompanyInfo.setProperty(key, mpv5.utils.export.BankAccount.toJson(list));
        mpv5.globals.CompanyInfo.save();
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
        String eoriValue = eori.getText();
        if (eoriValue != null && !eoriValue.trim().isEmpty() && !ZugpferdFile.isValidEori(eoriValue)) {
            problems.append("\n - ").append(Messages.COMPANY_EORI_INVALID).append(": ").append(eoriValue.trim());
        }
        String peppolValue = peppolid.getText();
        if (peppolValue != null && !peppolValue.trim().isEmpty() && !ZugpferdFile.isValidPeppolId(peppolValue)) {
            problems.append("\n - ").append(Messages.COMPANY_PEPPOL_ID_INVALID).append(": ").append(peppolValue.trim());
        }
        String archiveMailValue = mailArchiveAddress.getText();
        if (archiveMailValue != null && !archiveMailValue.trim().isEmpty()
                && !mpv5.utils.text.TypeConversion.isMailList(archiveMailValue)) {
            problems.append("\n - ").append(Messages.MAIL_COPY_INVALID).append(": ").append(archiveMailValue.trim());
        }
        return problems.toString();
    }

    private void setSettings() {
        for (LabeledTextField f : fields) {
            mpv5.globals.CompanyInfo.setProperty(f.getName(), f.getText() == null ? "" : f.getText());
        }
        mpv5.globals.CompanyInfo.setProperty("country", getSelectedCountry());
        mpv5.globals.CompanyInfo.setProperty(mpv5.utils.export.SmallBusiness.KEY_ENABLED,
                String.valueOf(smallBusiness.isSelected()));
        mpv5.globals.CompanyInfo.setProperty(mpv5.utils.export.InvoiceMailArchive.KEY_BODY,
                mailArchiveBody.getText() == null ? "" : mailArchiveBody.getText());
        mpv5.utils.export.EInvoiceDelivery.setZugpferdDefault(defaultZugpferd.isSelected());
        mpv5.globals.LocalSettings.save();
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
                f.setText("");
                if (values != null && values.getList() != null) {
                    for (String[] v : values.getList()) {
                        if (f.getName().equalsIgnoreCase(v[0])) {
                            f.setText(v[1]);
                        }
                    }
                }
            }
            String bodyVal = "";
            String countryVal = null;
            if (values != null && values.getList() != null) {
                for (String[] v : values.getList()) {
                    if ("country".equalsIgnoreCase(v[0])) {
                        countryVal = v[1];
                    } else if (mpv5.utils.export.InvoiceMailArchive.KEY_BODY.equalsIgnoreCase(v[0])) {
                        bodyVal = v[1] == null ? "" : v[1];
                    }
                }
            }
            mailArchiveBody.setText(bodyVal);
            selectCountry(countryVal);
            boolean small = false;
            if (values != null && values.getList() != null) {
                for (String[] v : values.getList()) {
                    if (mpv5.utils.export.SmallBusiness.KEY_ENABLED.equalsIgnoreCase(v[0])) {
                        small = "true".equalsIgnoreCase(String.valueOf(v[1]).trim());
                    }
                }
            }
            smallBusiness.setSelected(small);
            showDefaultNote();
            defaultZugpferd.setSelected(mpv5.utils.export.EInvoiceDelivery.isZugpferdDefault());
        } catch (Exception e) {
            Log.Debug(this, e);
        }
    }

    /**
     * Show the legal wording of the selected country in the empty note field
     */
    private void showDefaultNote() {
        String code = mpv5.utils.export.ZugpferdFile.toCountryCode(getSelectedCountry());
        String def = mpv5.utils.export.SmallBusiness.defaultNote(code, smallBusinessId.getText());
        smallBusinessNote.getTextField().setToolTipText(def);
        smallBusinessNote.setEnabled(smallBusiness.isSelected());
        smallBusinessId.setEnabled(smallBusiness.isSelected());
    }

    private void loadCountries() {
        try {
            country.setModel(LanguageManager.getCountriesAsComboBoxModel());
        } catch (Exception e) {
            Log.Debug(this, e);
        }
        if (country.getItemCount() == 0) {
            try {
                java.io.InputStream in = Main.class.getResourceAsStream("/extra/countries.xml");
                if (in != null) {
                    org.jdom.Document doc = new org.jdom.input.SAXBuilder().build(in);
                    if (doc != null) {
                        String[][] list = new mpv5.utils.xml.XMLReader().toArray(doc.getRootElement().getChild("countries"));
                        for (String[] c : list) {
                            country.addItem(new MPComboBoxModelItem(c[2], c[1]));
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (country.getItemCount() == 0) {
            country.addItem(new MPComboBoxModelItem("276", "Germany"));
            country.addItem(new MPComboBoxModelItem("40", "Austria"));
            country.addItem(new MPComboBoxModelItem("756", "Switzerland"));
        }
    }

    private void selectCountry(String countryVal) {
        if (country.getItemCount() == 0) {
            loadCountries();
        }
        if (countryVal == null || countryVal.trim().isEmpty()) {
            try {
                countryVal = User.getCurrentUser().__getDefcountry();
            } catch (Exception ignored) {
            }
        }
        if (countryVal == null || countryVal.trim().isEmpty()) {
            countryVal = "276";
        }
        countryVal = countryVal.trim();

        // 1. Exact match on value/name
        for (int i = 0; i < country.getItemCount(); i++) {
            Object o = country.getItemAt(i);
            if (o instanceof MPComboBoxModelItem) {
                MPComboBoxModelItem it = (MPComboBoxModelItem) o;
                if (it.getValue().equalsIgnoreCase(countryVal)) {
                    country.setSelectedIndex(i);
                    return;
                }
            } else if (o != null && o.toString().equalsIgnoreCase(countryVal)) {
                country.setSelectedIndex(i);
                return;
            }
        }

        // 2. Exact match on ID / ISO numeric
        for (int i = 0; i < country.getItemCount(); i++) {
            Object o = country.getItemAt(i);
            if (o instanceof MPComboBoxModelItem) {
                MPComboBoxModelItem it = (MPComboBoxModelItem) o;
                if (String.valueOf(it.getId()).equalsIgnoreCase(countryVal)) {
                    country.setSelectedIndex(i);
                    return;
                }
            }
        }

        // 3. Match on ISO-2 code (e.g. "DE" vs "Germany" or "276")
        String targetCode = ZugpferdFile.toCountryCode(countryVal);
        if (!targetCode.isEmpty()) {
            for (int i = 0; i < country.getItemCount(); i++) {
                Object o = country.getItemAt(i);
                if (o instanceof MPComboBoxModelItem) {
                    MPComboBoxModelItem it = (MPComboBoxModelItem) o;
                    if (targetCode.equalsIgnoreCase(ZugpferdFile.toCountryCode(it.getValue()))
                            || targetCode.equalsIgnoreCase(ZugpferdFile.toCountryCode(String.valueOf(it.getId())))) {
                        country.setSelectedIndex(i);
                        return;
                    }
                }
            }
        }

        // 4. Custom / unknown country not in list: add it and select it
        MPComboBoxModelItem custom = new MPComboBoxModelItem(countryVal, countryVal);
        country.addItem(custom);
        country.setSelectedItem(custom);
    }

    public String getSelectedCountry() {
        Object sel = country.getSelectedItem();
        if (sel instanceof MPComboBoxModelItem) {
            return ((MPComboBoxModelItem) sel).getValue();
        } else if (sel != null) {
            return sel.toString().trim();
        }
        return "";
    }

    public JComboBox getCountryComboBox() {
        return country;
    }

    public LabeledTextField getEoriField() {
        return eori;
    }

    public LabeledTextField getPeppolIdField() {
        return peppolid;
    }

    public LabeledTextField getMailArchiveAddressField() {
        return mailArchiveAddress;
    }

    public LabeledTextField getMailArchiveSubjectField() {
        return mailArchiveSubject;
    }

    public javax.swing.JTextArea getMailArchiveBodyArea() {
        return mailArchiveBody;
    }

    public JButton getSmtpSettingsButton() {
        return smtpSettingsBtn;
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
