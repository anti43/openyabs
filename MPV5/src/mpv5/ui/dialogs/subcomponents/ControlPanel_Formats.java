package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryData;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Item;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.i18n.LanguageManager;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;
import mpv5.ui.panels.DataPanel;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.db.objects.User;
import mpv5.handler.FormatHandler;
import mpv5.handler.MPEnum;
import mpv5.handler.VariablesHandler;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.date.DateConverter;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.tables.Selection;
import mpv5.utils.tables.TableFormat;
import mpv5.utils.text.MD5HashGenerator;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 * 
 */
public class ControlPanel_Formats extends javax.swing.JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "formats";
    private PropertyStore oldvalues;
    private User dataOwner;
    private static ControlPanel_Formats ident;
    private Integer currentUser;

    public ControlPanel_Formats() {
        if (MPSecurityManager.checkAdminAccess()) {
            initComponents();
            refresh();
            setTable();
            setVisible(true);
            setacL();

        }
    }

    public ControlPanel_Formats(User user) {
        if (MPSecurityManager.checkAdminAccess()) {
            initComponents();
            refresh();
            setdata(user);
            setVisible(true);
            setacL();
        }
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jComboBox1 = new javax.swing.JComboBox();
        jButton5 = new javax.swing.JButton();
        labeledCombobox1 = new mpv5.ui.beans.LabeledCombobox();
        labeledCombobox2 = new mpv5.ui.beans.LabeledCombobox();
        labeledTextField1 = new mpv5.ui.beans.LabeledTextField();
        jLabel1 = new javax.swing.JLabel();
        labeledCombobox3 = new mpv5.ui.beans.LabeledCombobox();
        labeledSpinner1 = new mpv5.ui.beans.LabeledSpinner();
        jLabel2 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Formats.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Formats.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTable1.setName("jTable1"); // NOI18N
        jScrollPane1.setViewportView(jTable1);

        jComboBox1.setName("jComboBox1"); // NOI18N
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox1ItemStateChanged(evt);
            }
        });
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/2leftarrow.png"))); // NOI18N
        jButton5.setText(bundle.getString("ControlPanel_Formats.jButton5.text")); // NOI18N
        jButton5.setToolTipText(bundle.getString("ControlPanel_Formats.jButton5.toolTipText")); // NOI18N
        jButton5.setName("jButton5"); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        labeledCombobox1.set_Label(bundle.getString("ControlPanel_Formats.labeledCombobox1._Label")); // NOI18N
        labeledCombobox1.setName("labeledCombobox1"); // NOI18N

        labeledCombobox2.set_Label(bundle.getString("ControlPanel_Formats.labeledCombobox2._Label")); // NOI18N
        labeledCombobox2.setName("labeledCombobox2"); // NOI18N

        labeledTextField1.set_Label(bundle.getString("ControlPanel_Formats.labeledTextField1._Label")); // NOI18N
        labeledTextField1.setName("labeledTextField1"); // NOI18N

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/1downarrow.png"))); // NOI18N
        jLabel1.setText(bundle.getString("ControlPanel_Formats.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        labeledCombobox3.set_Label(bundle.getString("ControlPanel_Formats.labeledCombobox3._Label")); // NOI18N
        labeledCombobox3.setName("labeledCombobox3"); // NOI18N

        labeledSpinner1.set_Label(bundle.getString("ControlPanel_Formats.labeledSpinner1._Label")); // NOI18N
        labeledSpinner1.setName("labeledSpinner1"); // NOI18N

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel2.setText(bundle.getString("ControlPanel_Formats.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                        .addGap(2, 2, 2))
                    .addComponent(jComboBox1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, 0)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(labeledTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(labeledCombobox2, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(labeledCombobox1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(96, 96, 96))
                            .addComponent(labeledSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labeledCombobox3, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(121, 121, 121)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(labeledCombobox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(labeledCombobox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(labeledCombobox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labeledSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE))))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(43, 43, 43))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton3.setText(bundle.getString("ControlPanel_Formats.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton3);

        jButton2.setText(bundle.getString("ControlPanel_Formats.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton2);

        add(jPanel6, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (test()) {
            save();
        }
}//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        test();
}//GEN-LAST:event_jButton3ActionPerformed

    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
    }//GEN-LAST:event_jComboBox1ItemStateChanged

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed

        currentUser = Integer.valueOf(((MPComboBoxModelItem) jComboBox1.getSelectedItem()).getId());
        setTable();
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        if (test()) {
            save();
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    public void setValues(PropertyStore values) {
    }

    public String getUname() {
        return UNAME;
    }

    public void reset() {
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox2;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox3;
    private mpv5.ui.beans.LabeledSpinner labeledSpinner1;
    private mpv5.ui.beans.LabeledTextField labeledTextField1;
    // End of variables declaration//GEN-END:variables
    public String laf_;
    public String language_ = "buildin_en";
    public String locale_ = "en_US";
    public String defcountry_ = "";
    public String fullname_;
    public String mail_;
    public String cname_;
    public String password_;
    public boolean isenabled_;
    public boolean isloggedin_;
    public boolean isrgrouped_;
    public int inthighestright_ = 9;
    public int intdefaultaccount_ = 9;
    public int intaddedby_ = 4343;
    public Date datelastlog_ = new Date();
    public int ids_;
    public int groupsids_ = 1;
    public java.util.Date dateadded_ = new java.util.Date();

    public void refresh() {

        labeledSpinner1.getSpinner().setModel(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
         labeledSpinner1.getSpinner().setEditor(new JSpinner.NumberEditor(labeledSpinner1.getSpinner()));
//        ((JFormattedTextField) labeledSpinner1.getSpinner().getEditor()).setEditable(false);
        labeledSpinner1.getSpinner().updateUI();

        try {
            jComboBox1.setModel(MPComboBoxModelItem.toModel(QueryHandler.instanceOf().clone(Context.getUser()).getColumns(new String[]{"ids", "cname"}, 0)));
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }

        labeledCombobox1.setModel(QueryHandler.instanceOf().clone(Context.getFormats()).select("inttype,cname", (String[]) null));
        labeledCombobox2.setModel(VariablesHandler.GENERIC_VARS.values());
        labeledCombobox3.setModel(MPComboBoxModelItem.toModel((MPEnum[]) FormatHandler.TYPES.values()));
        labeledTextField1.setText(FormatHandler.INTEGERPART_IDENTIFIER);
        labeledCombobox3.getComboBox().setSelectedIndex(0);

        setUser();
        setTable();
    }



    @Override
    public Component getAndRemoveActionPanel() {
         this.remove(jPanel6);validate();
        return jPanel6;
    }

    private void save() {
        //set start value
        if (labeledSpinner1.getSpinner().getValue() != null && Integer.valueOf(labeledSpinner1.getSpinner().getValue().toString()).intValue() > 0) {
            labeledTextField1.setText(FormatHandler.START_VALUE_IDENTIFIER + Integer.valueOf(labeledSpinner1.getSpinner().getValue().toString()) + FormatHandler.START_VALUE_IDENTIFIER + labeledTextField1.getText());
        }

        QueryCriteria c = new QueryCriteria();
        c.add("inttype", Integer.valueOf(labeledCombobox3.getSelectedItem().getId()));
        c.add("usersids", Integer.valueOf(((MPComboBoxModelItem) jComboBox1.getSelectedItem()).getId()));
        QueryHandler.instanceOf().clone(Context.getFormats()).delete(c);
        QueryData what = new QueryData();
        what.add("inttype", Integer.valueOf(labeledCombobox3.getSelectedItem().getId()));
        what.add("cname", labeledTextField1.getText());
        what.add("usersids", Integer.valueOf(((MPComboBoxModelItem) jComboBox1.getSelectedItem()).getId()));
        QueryHandler.instanceOf().clone(Context.getFormats()).insert(what, null);

        setTable();
        labeledTextField1.setText("");
        labeledSpinner1.getSpinner().setValue(0);
        labeledCombobox1.setModel(QueryHandler.instanceOf().clone(Context.getFormats()).select("inttype,cname", (String[]) null));
    }

    private void setTable() {
        try {
            Object[][] d = QueryHandler.instanceOf().clone(Context.getFormats()).select("cname, inttype", new QueryCriteria("usersids", currentUser));
            for (int i = 0; i < d.length; i++) {
                MPEnum[] t = FormatHandler.TYPES.values();
                for (int j = 0; j < t.length; j++) {
                    MPEnum mPEnum = t[j];
                    if (mPEnum.getId() == Integer.valueOf(d[i][1].toString()).intValue()) {
                        d[i][1] = mPEnum.getName();
                        break;
                    }
                }
            }
            jTable1.setModel(new MPTableModel(d));
            TableFormat.format(jTable1, 1, 100);
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }
    }

    private void setUser() {
        if (currentUser == null) {
            currentUser = MPView.getUser().getID();
            jComboBox1.setSelectedIndex(MPComboBoxModelItem.getItemID(currentUser, jComboBox1.getModel()));
        }

        currentUser = Integer.valueOf(((MPComboBoxModelItem) jComboBox1.getSelectedItem()).getId());

    }

    private void setacL() {

        labeledCombobox1.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                labeledTextField1.set_Text(labeledCombobox1.getSelectedItem());
            }
        });

        labeledCombobox2.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                labeledTextField1.set_Text(labeledCombobox2.getSelectedItem().getValue() + labeledTextField1.get_Text());
            }
        });
    }

    private void setdata(User user) {
        jComboBox1.setSelectedIndex(MPComboBoxModelItem.getItemID(user.getID(), jComboBox1.getModel()));

    }

    private boolean test() {
        if (!labeledTextField1.getText().contains(FormatHandler.INTEGERPART_IDENTIFIER)) {
            labeledTextField1.set_Text(labeledTextField1.getText() + FormatHandler.INTEGERPART_IDENTIFIER);
        }

        String str = "";
        Item b = (Item) DatabaseObject.getObject(Context.getItems());
        b.avoidNulls();

        FormatHandler fh = new FormatHandler(b);
        fh.setFormat(labeledTextField1.getText());
        str = fh.toString(1000);
        str = Messages.THE_RESULT + str;
        return Popup.OK_dialog(str, Messages.NOTICE.getValue());
    }
}
