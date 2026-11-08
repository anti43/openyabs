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
/*
 * ContactPanel.java
 *
 * Created on Nov 20, 2008, 8:17:28 AM
 */
package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Tax;
import mpv5.globals.Messages;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.misc.RichList;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.ui.panels.DataPanel;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.ui.TextFieldUtils;

/**
 * The tax rates: a list on the left, the chosen rate on the right
 */
public class ControlPanel_Taxes extends JPanel implements ControlApplet, DataPanel {

    private static final long serialVersionUID = 1L;
    private ControlPanel_Taxes insta;
    private Tax dataOwner;
    private final DefaultListModel<Object> model = new DefaultListModel<Object>();
    private final JList<Object> list = new JList<Object>(model);
    private final JTextField cname = ModernTheme.roomy(new JTextField());
    private final JTextField value = ModernTheme.roomy(new JTextField());
    private final JComboBox<Object> countries = new JComboBox<Object>();
    private final JLabel title = new JLabel();
    private final JButton delete = Applets.danger(new JButton(Messages.TAXES_DELETE.toString()));
    private JPanel actions = new JPanel();
    private boolean listing;

    /**
     * Creates the tax rates panel (administrators only)
     */
    public ControlPanel_Taxes() {
        super(new BorderLayout(ModernTheme.px(12), 0));
        setOpaque(false);
        setBorder(ModernTheme.padding(12, 14));
        if (!MPSecurityManager.checkAdminAccess()) {
            return;
        }
        java.util.ResourceBundle bundle = LanguageManager.getBundle();

        JPanel left = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        left.setOpaque(false);
        JButton add = new JButton("+ " + Messages.TAXES_NEW);
        add.addActionListener(e -> newTax());
        left.add(add, BorderLayout.NORTH);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new RichList.Renderer());
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !listing && list.getSelectedValue() instanceof RichList.Row) {
                setDataOwner((Tax) ((RichList.Row) list.getSelectedValue()).data, true);
            }
        });
        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
        left.add(sp, BorderLayout.CENTER);
        left.setPreferredSize(new java.awt.Dimension(ModernTheme.px(280), ModernTheme.px(300)));
        add(left, BorderLayout.WEST);

        title.setFont(ModernTheme.font(4, Font.BOLD));
        Card card = new Card(null)
                .row(Card.field(bundle.getString("ControlPanel_Taxes.cname._Label"), cname, 2),
                        Card.field(Messages.TAXES_RATE.toString(), value, 1))
                .row(Card.field(bundle.getString("ControlPanel_Taxes.countries._Label"), countries))
                .addFull(Applets.hint(Messages.TAXES_HINT.toString()));
        JPanel right = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        right.setOpaque(false);
        right.add(title, BorderLayout.NORTH);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(card, BorderLayout.NORTH);
        right.add(top, BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);

        delete.addActionListener(e -> deleteTax());
        PrimaryButton save = new PrimaryButton(bundle.getString("ControlPanel_Taxes.jButton2.text"), "/images/16/filesave.png");
        save.addActionListener(e -> save());
        actions = Applets.actionBar(new JComponent[]{delete}, new JComponent[]{save});
        refresh();
        newTax();
    }

    private void newTax() {
        dataOwner = null;
        listing = true;
        list.clearSelection();
        listing = false;
        cname.setText("");
        value.setText(FormatNumber.formatDezimal(0d));
        title.setText(Messages.TAXES_NEW.toString());
        delete.setEnabled(false);
        cname.requestFocusInWindow();
    }

    private void save() {
        if (!collectData() || cname_ == null || cname_.trim().isEmpty()) {
            showRequiredFields();
            return;
        }
        Tax t = dataOwner != null ? dataOwner : new Tax();
        if (dataOwner == null) {
            t.setIdentifier(cname_);
        }
        t.setCname(cname_);
        t.setCountry(country_);
        t.setTaxvalue(taxvalue_);
        if (t.save()) {
            dataOwner = t;
            actionAfterSave();
        }
    }

    private void deleteTax() {
        if (dataOwner != null && dataOwner.isExisting()
                && Popup.Y_N_dialog(String.format(Messages.TAXES_REALLY_DELETE.toString(), dataOwner.__getCname()))) {
            if (!dataOwner.delete()) {
                Popup.notice(Messages.IN_USE);
            }
            refresh();
            newTax();
        }
    }

    @Override
    public void showRequiredFields() {
        TextFieldUtils.blinkerRed(cname);
    }
    public String cname_;
    public int intaddedby_;
    public int ids_;
    public Date dateadded_;
    public int groupsids_ = 1;
    public BigDecimal taxvalue_;
    public String identifier_ = "";
    public String country_ = "";

    @Override
    public void setValues(PropertyStore values) {
    }

    @Override
    public String getUname() {
        return "Taxes";
    }

    @Override
    public void reset() {
        if (dataOwner != null) {
            setDataOwner(dataOwner, true);
        } else {
            newTax();
        }
    }

    public ControlApplet instanceOf() {
        if (insta == null) {
            insta = new ControlPanel_Taxes();
        }
        return insta;
    }

    @Override
    public void refresh() {
        countries.setModel(new javax.swing.DefaultComboBoxModel<Object>(toArray(LanguageManager.getCountriesAsComboBoxModel())));
        Object sel = dataOwner;
        listing = true;
        try {
            model.clear();
            List<DatabaseObject> taxes = new ArrayList<DatabaseObject>();
            try {
                taxes = DatabaseObject.getObjects(Context.getTaxes());
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex.getMessage());
            }
            for (DatabaseObject o : taxes) {
                Tax t = (Tax) o;
                RichList.Row r = new RichList.Row();
                r.badge = "%";
                r.color = RichList.GREEN;
                r.title = t.__getCname();
                r.detail = t.__getCountry();
                r.right = FormatNumber.formatDezimal(t.__getTaxvalue()) + " %";
                r.strong = true;
                r.data = t;
                model.addElement(r);
                if (sel != null && ((Tax) sel).__getIDS().equals(t.__getIDS())) {
                    list.setSelectedIndex(model.size() - 1);
                }
            }
        } finally {
            listing = false;
        }
    }

    private static Object[] toArray(javax.swing.ComboBoxModel<?> m) {
        Object[] a = new Object[m.getSize()];
        for (int i = 0; i < a.length; i++) {
            a[i] = m.getElementAt(i);
        }
        return a;
    }

    @Override
    public Component getAndRemoveActionPanel() {
        return actions;
    }

    @Override
    public boolean collectData() {
        try {
            cname_ = cname.getText();
            taxvalue_ = FormatNumber.parseDezimal(value.getText().trim().isEmpty() ? "0" : value.getText());
            country_ = countries.getSelectedItem() == null ? "" : countries.getSelectedItem().toString();
            return true;
        } catch (Exception e) {
            Popup.notice(Messages.TAXES_RATE_INVALID.toString());
            return false;
        }
    }

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean populateData) {
        this.dataOwner = (Tax) object;
        if (populateData) {
            dataOwner.setPanelData(this);
            this.exposeData();
        }
        title.setText(dataOwner.__getCname());
        delete.setEnabled(dataOwner.isExisting());
    }

    @Override
    public void exposeData() {
        cname.setText(cname_);
        value.setText(taxvalue_ == null ? "0" : FormatNumber.formatDezimal(taxvalue_));
        for (int i = 0; i < countries.getItemCount(); i++) {
            if (String.valueOf(countries.getItemAt(i)).equals(country_)) {
                countries.setSelectedIndex(i);
            }
        }
    }

    @Override
    public void paste(DatabaseObject... dbos) {
        for (DatabaseObject dbo : dbos) {
            setDataOwner(dbo, true);
        }
    }

    @Override
    public void showSearchBar(boolean show) {
    }

    @Override
    public void actionAfterSave() {
        refresh();
        if (dataOwner != null) {
            title.setText(dataOwner.__getCname());
            delete.setEnabled(true);
        }
    }

    @Override
    public void actionAfterCreate() {
        refresh();
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
