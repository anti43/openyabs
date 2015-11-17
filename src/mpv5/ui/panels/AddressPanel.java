/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * AddressPanel.java
 *
 * Created on 26.02.2009, 11:33:08
 */
package mpv5.ui.panels;

import java.awt.Color;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.globals.Messages;
import mpv5.db.objects.Address;
import mpv5.db.objects.Contact;
import mpv5.logging.Log;
import mpv5.i18n.LanguageManager;
import mpv5.ui.frames.MPView;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 *  
 */
public class AddressPanel extends javax.swing.JPanel implements DataPanel {

    private static final long serialVersionUID = 8513278191283931211L;
    private Address dataOwner = new Address();
    private Contact dataParent = new Contact();
    private java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();

    /** Creates new form AddressPanel */
    public AddressPanel() {
        initComponents();
        dataOwner.setCountry(mpv5.db.objects.User.getCurrentUser().__getDefcountry());
        refresh();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel2 = new javax.swing.JPanel();
        mainaddress = new javax.swing.JPanel();
        title = new mpv5.ui.beans.LabeledTextField();
        street = new mpv5.ui.beans.LabeledTextField();
        cname = new mpv5.ui.beans.LabeledTextField();
        prename = new mpv5.ui.beans.LabeledTextField();
        zip = new mpv5.ui.beans.LabeledTextField();
        male = new javax.swing.JRadioButton();
        female = new javax.swing.JRadioButton();
        department = new mpv5.ui.beans.LabeledTextField();
        jLabel5 = new javax.swing.JLabel();
        countryselect = new javax.swing.JComboBox();
        city = new mpv5.ui.beans.LabeledTextField();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        badress = new javax.swing.JCheckBox();
        dadress = new javax.swing.JCheckBox();

        //\$2java.awt.Color(204, 204, 204));
        setName("Address#"); // NOI18N

//$2java.awt.Color(204, 204, 204));
        jPanel2.setBorder(null);
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.BorderLayout());

//$2java.awt.Color(204, 204, 204));
        mainaddress.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        mainaddress.setName("mainaddress"); // NOI18N

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();  // NOI18N
        title.set_Label(bundle.getString("AddressPanel.title._Label")); // NOI18N
        title.setMaximumSize(new java.awt.Dimension(120, 21));
        title.setMinimumSize(new java.awt.Dimension(120, 24));
        title.setName("title"); // NOI18N
        title.setPreferredSize(new java.awt.Dimension(120, 24));

        street.set_Label(bundle.getString("AddressPanel.street._Label")); // NOI18N
        street.setMaximumSize(new java.awt.Dimension(120, 21));
        street.setMinimumSize(new java.awt.Dimension(120, 24));
        street.setName("street"); // NOI18N
        street.setPreferredSize(new java.awt.Dimension(120, 24));

        cname.set_Label(bundle.getString("AddressPanel.cname._Label")); // NOI18N
        cname.setMaximumSize(new java.awt.Dimension(120, 21));
        cname.setMinimumSize(new java.awt.Dimension(120, 24));
        cname.setName("cname"); // NOI18N
        cname.setPreferredSize(new java.awt.Dimension(120, 24));

        prename.set_Label(bundle.getString("AddressPanel.prename._Label")); // NOI18N
        prename.setMaximumSize(new java.awt.Dimension(120, 21));
        prename.setMinimumSize(new java.awt.Dimension(120, 24));
        prename.setName("prename"); // NOI18N
        prename.setPreferredSize(new java.awt.Dimension(120, 24));

        zip.set_Label(bundle.getString("AddressPanel.zip._Label")); // NOI18N
        zip.setMaximumSize(new java.awt.Dimension(120, 21));
        zip.setMinimumSize(new java.awt.Dimension(120, 24));
        zip.setName("zip"); // NOI18N
        zip.setPreferredSize(new java.awt.Dimension(120, 24));

//$2java.awt.Color(204, 204, 204));
        buttonGroup2.add(male);
        male.setFont(male.getFont().deriveFont(male.getFont().getStyle() & ~java.awt.Font.BOLD));
        male.setSelected(true);
        male.setText(bundle.getString("AddressPanel.male.text")); // NOI18N
        male.setName("male"); // NOI18N

//$2java.awt.Color(204, 204, 204));
        buttonGroup2.add(female);
        female.setFont(female.getFont().deriveFont(female.getFont().getStyle() & ~java.awt.Font.BOLD));
        female.setText(bundle.getString("AddressPanel.female.text")); // NOI18N
        female.setName("female"); // NOI18N

        department.set_Label(bundle.getString("AddressPanel.department._Label")); // NOI18N
        department.setFont(department.getFont().deriveFont(department.getFont().getStyle() | java.awt.Font.BOLD));
        department.setMaximumSize(new java.awt.Dimension(120, 21));
        department.setMinimumSize(new java.awt.Dimension(120, 24));
        department.setName("department"); // NOI18N
        department.setPreferredSize(new java.awt.Dimension(120, 24));

        jLabel5.setFont(jLabel5.getFont());
        jLabel5.setText(bundle.getString("AddressPanel.jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        countryselect.setMaximumSize(new java.awt.Dimension(120, 21));
        countryselect.setMinimumSize(new java.awt.Dimension(120, 24));
        countryselect.setName("countryselect"); // NOI18N
        countryselect.setPreferredSize(new java.awt.Dimension(120, 24));
        countryselect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                countryselectActionPerformed(evt);
            }
        });

        city.set_Label(bundle.getString("AddressPanel.city._Label")); // NOI18N
        city.setMaximumSize(new java.awt.Dimension(120, 21));
        city.setMinimumSize(new java.awt.Dimension(120, 24));
        city.setName("city"); // NOI18N
        city.setPreferredSize(new java.awt.Dimension(120, 24));

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/filesave.png"))); // NOI18N
        jButton2.setText(bundle.getString("AddressPanel.jButton2.text")); // NOI18N
        jButton2.setToolTipText(bundle.getString("AddressPanel.jButton2.toolTipText")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/remove.png"))); // NOI18N
        jButton1.setText(bundle.getString("AddressPanel.jButton1.text")); // NOI18N
        jButton1.setToolTipText(bundle.getString("AddressPanel.jButton1.toolTipText")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

//$2java.awt.Color(204, 204, 204));
        badress.setFont(badress.getFont());
        badress.setText(bundle.getString("AddressPanel.badress.text")); // NOI18N
        badress.setName("badress"); // NOI18N

//$2java.awt.Color(204, 204, 204));
        dadress.setFont(dadress.getFont());
        dadress.setText(bundle.getString("AddressPanel.dadress.text")); // NOI18N
        dadress.setName("dadress"); // NOI18N

        javax.swing.GroupLayout mainaddressLayout = new javax.swing.GroupLayout(mainaddress);
        mainaddress.setLayout(mainaddressLayout);
        mainaddressLayout.setHorizontalGroup(
            mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainaddressLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(mainaddressLayout.createSequentialGroup()
                        .addComponent(badress, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dadress, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainaddressLayout.createSequentialGroup()
                        .addGroup(mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(department, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                            .addComponent(title, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(street, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainaddressLayout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(countryselect, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(zip, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(prename, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cname, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainaddressLayout.createSequentialGroup()
                                .addComponent(male, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(female, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(city, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        mainaddressLayout.setVerticalGroup(
            mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainaddressLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(department, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(countryselect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(male)
                    .addComponent(female)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(prename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(street, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(city, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(dadress)
                    .addComponent(badress))
                .addGap(29, 29, 29))
        );

        jPanel2.add(mainaddress, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        removeAddress();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        addAddress();

    }//GEN-LAST:event_jButton2ActionPerformed

    private void countryselectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_countryselectActionPerformed

    }//GEN-LAST:event_countryselectActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox badress;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private mpv5.ui.beans.LabeledTextField city;
    private mpv5.ui.beans.LabeledTextField cname;
    private javax.swing.JComboBox countryselect;
    private javax.swing.JCheckBox dadress;
    private mpv5.ui.beans.LabeledTextField department;
    private javax.swing.JRadioButton female;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel mainaddress;
    private javax.swing.JRadioButton male;
    private mpv5.ui.beans.LabeledTextField prename;
    private mpv5.ui.beans.LabeledTextField street;
    private mpv5.ui.beans.LabeledTextField title;
    private mpv5.ui.beans.LabeledTextField zip;
    // End of variables declaration//GEN-END:variables
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

    public boolean collectData() {
        if (cname.getText().length() == 0) {
            return false;
        }
        ids_ = dataOwner.__getIDS();
        city_ = city.get_Text();
        cname_ = cname.get_Text();
        taxnumber_ = dataParent.__getTaxnumber();
        company_ = dataParent.__getCompany();
        country_ = dataParent.__getCountry();
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
        try {
            dataParent = (Contact) DatabaseObject.getObject(Context.getContact(), contactsids_);
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        try {
            countryselect.setSelectedIndex(MPComboBoxModelItem.getItemIDfromValue(country_, countryselect.getModel()));
        } catch (Exception e) {
            try {
                countryselect.setSelectedIndex(MPComboBoxModelItem.getItemIDfromValue(dataParent.__getCountry(), countryselect.getModel()));
            } catch (Exception ex) {
            }
        }
        badress.setSelected(inttype_ == 0 || inttype_ == 2);
        dadress.setSelected(inttype_ == 1 || inttype_ == 2);
    }

    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    public void setDataOwner(DatabaseObject object, boolean populate) {
        dataOwner = (Address) object;
        if (populate) {
            dataOwner.setPanelData(this);
            this.exposeData();

            if (this.getParent() instanceof JTabbedPane) {
                JTabbedPane jTabbedPane = (JTabbedPane) this.getParent();
                jTabbedPane.setTitleAt(jTabbedPane.getSelectedIndex(), Messages.CONTACT + cname_);
            }
        }
    }

    public void refresh() {
        try {
            countryselect.setModel(LanguageManager.getCountriesAsComboBoxModel());
            Runnable runnable = new Runnable() {

                public void run() {
                    countryselect.setSelectedIndex(MPComboBoxModelItem.getItemID(mpv5.db.objects.User.getCurrentUser().__getDefcountry(), countryselect.getModel()));
                }
            };
            SwingUtilities.invokeLater(runnable);
        } catch (Exception e) {
        }
    }

    public void paste(DatabaseObject... dbos) {
        for (DatabaseObject dbo : dbos) {
            if (dbo.getDbIdentity().equals(Context.getAddress().getDbIdentity())) {
                setDataOwner(dbo, true);
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE.toString(), Color.RED);
            }
        }

    }

    public void showRequiredFields() {
        TextFieldUtils.blinkerRed(cname);
    }

    /**
     * Removes this adress from the current user
     */
    public void removeAddress() {
        this.getParent().remove(this);
        dataOwner.delete();
    }

    /**
     * Associates this adress with the current user
     */
    public void addAddress() {
        if (dataParent.isExisting()) {
            dataOwner.setContactsids(dataParent.__getIDS());
            dataOwner.getPanelData(this);
            if (!dataOwner.save()) {
                showRequiredFields();
            } else {
                ((JTabbedPane) getParent()).setTitleAt(
                        ((JTabbedPane) getParent()).getSelectedIndex(),
                        dataOwner.__getCname());
            }
        }
    }

    /**
     * @return the dataParent
     */
    public Contact getDataParent() {
        return dataParent;
    }

    /**
     * @param dataParent the dataParent to set
     */
    public void setDataParent(Contact dataParent) {
        this.dataParent = dataParent;

    }

    public void showSearchBar(boolean show) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void actionAfterSave() {
    }

    @Override
    public void actionAfterCreate() {
    }

    public void actionBeforeCreate() {
    }

    public void actionBeforeSave() {
    }

    public void mail() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void print() {
       mpv5.utils.export.Export.print(this);
    }
}
