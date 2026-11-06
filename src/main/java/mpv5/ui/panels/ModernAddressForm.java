/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Address;
import mpv5.db.objects.Contact;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.ui.beans.LabeledTextField;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.ui.TextFieldUtils;

/**
 * An additional address of a contact (invoicing / delivery) in the modern
 * layout; same data and functions as {@link AddressPanel}. Saved and deleted
 * on its own with its buttons, independent of the contact.
 * <p>
 * The public fields ending with an underscore are filled from / copied to the
 * address by name.
 */
public class ModernAddressForm extends JPanel implements DataPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Notified when the address has been saved or deleted
     */
    public interface Listener {

        void saved(ModernAddressForm form);

        void deleted(ModernAddressForm form);
    }
    private Address dataOwner = new Address();
    private Contact dataParent = new Contact();
    private final Listener listener;
    private final LabeledTextField title = ModernTheme.plain(new LabeledTextField());
    private final LabeledTextField prename = ModernTheme.plain(new LabeledTextField());
    private final LabeledTextField cname = ModernTheme.plain(new LabeledTextField());
    private final LabeledTextField department = ModernTheme.plain(new LabeledTextField());
    private final LabeledTextField street = ModernTheme.plain(new LabeledTextField());
    private final LabeledTextField zip = ModernTheme.plain(new LabeledTextField());
    private final LabeledTextField city = ModernTheme.plain(new LabeledTextField());
    private final JComboBox countryselect = new JComboBox();
    private final JRadioButton male = new JRadioButton(text("AddressPanel.male.text"));
    private final JRadioButton female = new JRadioButton(text("AddressPanel.female.text"));
    private final JCheckBox badress = new JCheckBox(text("AddressPanel.badress.text"));
    private final JCheckBox dadress = new JCheckBox(text("AddressPanel.dadress.text"));
    // <editor-fold defaultstate="collapsed" desc="Data fields, filled by name from / copied to the address">
    public String city_;
    public String cname_;
    public String taxnumber_;
    public String department_;
    public boolean ismale_;
    public String prename_;
    public String street_;
    public String title_;
    public String zip_;
    public String company_;
    public String country_;
    public int ids_;
    public int contactsids_;
    public int groupsids_ = 1;
    public java.util.Date dateadded_ = new java.util.Date();
    public int intaddedby_ = 4343;
    public int inttype_;
    // </editor-fold>

    /**
     * @param listener may be null
     */
    public ModernAddressForm(Listener listener) {
        super(new BorderLayout());
        this.listener = listener;
        setOpaque(false);
        dataOwner.setCountry(User.getCurrentUser().__getDefcountry());
        countryselect.setModel(LanguageManager.getCountriesAsComboBoxModel());
        try {
            countryselect.setSelectedIndex(MPComboBoxModelItem.getItemID(User.getCurrentUser().__getDefcountry(), countryselect.getModel()));
        } catch (Exception e) {
            Log.Debug(this, e);
        }
        ButtonGroup g = new ButtonGroup();
        g.add(male);
        g.add(female);
        male.setSelected(true);
        for (javax.swing.AbstractButton b : new javax.swing.AbstractButton[]{male, female, badress, dadress}) {
            b.setOpaque(false);
        }
        JPanel gender = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), 0));
        gender.setOpaque(false);
        gender.add(male);
        gender.add(female);
        JPanel type = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), 0));
        type.setOpaque(false);
        type.add(badress);
        type.add(dadress);

        JButton save = new JButton(text("AddressPanel.jButton2.text"));
        save.addActionListener(e -> save());
        JButton delete = new JButton(text("AddressPanel.jButton1.text"));
        delete.addActionListener(e -> delete());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0));
        buttons.setOpaque(false);
        buttons.add(delete);
        buttons.add(save);

        add(new Card(null).flat()
                .row(Card.field(null, type))
                .row(Card.field(text("AddressPanel.title._Label"), title, 1),
                        Card.field(text("AddressPanel.prename._Label"), prename, 2))
                .row(Card.field(text("AddressPanel.cname._Label"), cname))
                .row(Card.field(text("AddressPanel.department._Label"), department, 2),
                        Card.field(null, gender, 1))
                .row(Card.field(text("AddressPanel.street._Label"), street))
                .row(Card.field(text("AddressPanel.zip._Label"), zip, 1),
                        Card.field(text("AddressPanel.city._Label"), city, 2))
                .row(Card.field(text("AddressPanel.jLabel5.text"), countryselect))
                .addFull(buttons), BorderLayout.CENTER);
    }

    private static String text(String key) {
        try {
            return LanguageManager.getBundle().getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    /**
     * @return a short name for the selector: type and name
     */
    public String getLabel() {
        String type = inttype_ == 2 ? Messages.ADDRESS_TYPE_BOTH.toString()
                : inttype_ == 1 ? Messages.ADDRESS_TYPE_DELIVERY.toString() : Messages.ADDRESS_TYPE_INVOICE.toString();
        String name = cname.getTextField().getText();
        if (department.getTextField().getText().length() > 0) {
            name = department.getTextField().getText() + (name.length() > 0 ? " - " + name : "");
        }
        return name.length() > 0 ? type + ": " + name : (dataOwner.isExisting() ? type : Messages.NEW_VALUE.toString());
    }

    /**
     * Saves the address for the contact (which must be saved already)
     */
    public void save() {
        if (!dataParent.isExisting()) {
            return;
        }
        dataOwner.setContactsids(dataParent.__getIDS());
        if (!dataOwner.getPanelData(this) || !dataOwner.save()) {
            showRequiredFields();
        } else if (listener != null) {
            listener.saved(this);
        }
    }

    /**
     * Deletes the address (after asking)
     */
    public void delete() {
        if (dataOwner.isExisting() && !Popup.Y_N_dialog(Messages.REALLY_DELETE)) {
            return;
        }
        if (dataOwner.isExisting()) {
            dataOwner.delete();
        }
        if (listener != null) {
            listener.deleted(this);
        }
    }

    /**
     * @param dataParent the contact of the address
     */
    public void setDataParent(Contact dataParent) {
        this.dataParent = dataParent;
    }

    @Override
    public boolean collectData() {
        if (cname.getText().length() == 0) {
            return false;
        }
        ids_ = dataOwner.__getIDS();
        city_ = city.get_Text();
        cname_ = cname.get_Text();
        taxnumber_ = dataParent.__getTaxnumber();
        company_ = dataParent.__getCompany();
        ismale_ = male.isSelected();
        prename_ = prename.get_Text();
        street_ = street.get_Text();
        title_ = title.get_Text();
        zip_ = zip.get_Text();
        department_ = department.get_Text();
        contactsids_ = dataParent.__getIDS();
        if (badress.isSelected() && dadress.isSelected()) {
            inttype_ = 2;
        } else if (badress.isSelected()) {
            inttype_ = 0;
        } else if (dadress.isSelected()) {
            inttype_ = 1;
        }
        if (countryselect.getSelectedItem() != null) {
            country_ = String.valueOf(((MPComboBoxModelItem) countryselect.getSelectedItem()).getValue());
        } else {
            country_ = "";
        }
        return true;
    }

    @Override
    public void exposeData() {
        city.set_Text(city_);
        male.setSelected(ismale_);
        female.setSelected(!ismale_);
        prename.set_Text(prename_);
        street.set_Text(street_);
        cname.setText(cname_);
        title.set_Text(title_);
        zip.set_Text(zip_);
        department.set_Text(department_);
        if (contactsids_ > 0) {
            try {
                dataParent = (Contact) DatabaseObject.getObject(Context.getContact(), contactsids_);
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex.getMessage());
            }
        }
        try {
            countryselect.setSelectedIndex(MPComboBoxModelItem.getItemIDfromValue(country_, countryselect.getModel()));
        } catch (Exception e) {
            try {
                countryselect.setSelectedIndex(MPComboBoxModelItem.getItemIDfromValue(dataParent.__getCountry(), countryselect.getModel()));
            } catch (Exception ex) {
                Log.Debug(this, ex.getMessage());
            }
        }
        badress.setSelected(inttype_ == 0 || inttype_ == 2);
        dadress.setSelected(inttype_ == 1 || inttype_ == 2);
    }

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean populate) {
        dataOwner = (Address) object;
        if (populate) {
            dataOwner.setPanelData(this);
            exposeData();
        }
    }

    @Override
    public void refresh() {
    }

    @Override
    public void paste(DatabaseObject... dbos) {
        for (DatabaseObject dbo : dbos) {
            if (dbo.getDbIdentity().equals(Context.getAddress().getDbIdentity())) {
                setDataOwner(dbo, true);
            }
        }
    }

    @Override
    public void showRequiredFields() {
        TextFieldUtils.blinkerRed(cname);
        cname.requestFocus();
    }

    @Override
    public void showSearchBar(boolean show) {
    }

    @Override
    public void actionAfterSave() {
    }

    @Override
    public void actionAfterCreate() {
    }

    @Override
    public void actionBeforeCreate() {
    }

    @Override
    public void actionBeforeSave() {
    }

    @Override
    public void mail() {
    }

    @Override
    public void print() {
        mpv5.utils.export.Export.print(this);
    }
}
