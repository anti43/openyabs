package mpv5.ui.dialogs.subcomponents;

import java.io.File;
import java.sql.SQLException;
import java.util.Locale;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import javax.swing.JTextField;
import mpv5.data.PropertyStore;
import mpv5.db.common.*;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.misc.TextFieldUtils;

import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.text.TypeConversion;

/**
 *
 *
 */
public class ControlPanel_Locale extends javax.swing.JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "locale";
    private PropertyStore oldvalues;
    private static ControlPanel_Locale ident;

    public ControlPanel_Locale() {
        initComponents();
        locales.setModel(getLocales());
        locales.setSelectedIndex(MPComboBoxModelItem.getItemID(Locale.getDefault().toString(),
                locales.getModel()));
        setLanguageSelection();
        isocode.set_ValueClass(java.lang.Integer.class);
        countryselect.setModel(LanguageManager.getCountriesAsComboBoxModel());
        setVisible(true);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel5 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        labeledTextChooser2 = new mpv5.ui.beans.LabeledTextChooser();
        jButton4 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        isocode = new mpv5.ui.beans.LabeledTextField();
        countryname = new mpv5.ui.beans.LabeledTextField();
        jButton7 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jButton8 = new javax.swing.JButton();
        countryselect = new mpv5.ui.beans.LabeledCombobox();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        labeledTextChooser1 = new mpv5.ui.beans.LabeledTextChooser();
        jButton3 = new javax.swing.JButton();
        langName = new mpv5.ui.beans.LabeledTextField();
        jLabel7 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        languages = new javax.swing.JComboBox();
        Delete = new javax.swing.JButton();
        export = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        locales = new javax.swing.JComboBox();
        jPanel6 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Locale.border.title"))); // NOI18N
        setName("Form"); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel5.setName("jPanel5"); // NOI18N

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel8.setText(bundle.getString("ControlPanel_Locale.jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jLabel9.setText(bundle.getString("ControlPanel_Locale.jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        labeledTextChooser2.set_Label(bundle.getString("ControlPanel_Locale.labeledTextChooser2._Label")); // NOI18N
        labeledTextChooser2.set_LabelFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        labeledTextChooser2.setName("labeledTextChooser2"); // NOI18N

        jButton4.setText(bundle.getString("ControlPanel_Locale.jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Locale.jPanel1.border.title"))); // NOI18N
        jPanel1.setName(bundle.getString("ControlPanel_Locale.jPanel1.name")); // NOI18N

        isocode.set_Label(bundle.getString("ControlPanel_Locale.isocode._Label")); // NOI18N
        isocode.setName(bundle.getString("ControlPanel_Locale.isocode.name")); // NOI18N

        countryname.set_Label(bundle.getString("ControlPanel_Locale.countryname._Label")); // NOI18N
        countryname.setName(bundle.getString("ControlPanel_Locale.countryname.name")); // NOI18N

        jButton7.setText(bundle.getString("ControlPanel_Locale.jButton7.text")); // NOI18N
        jButton7.setName(bundle.getString("ControlPanel_Locale.jButton7.name")); // NOI18N
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(isocode, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(countryname, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton7, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(isocode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(countryname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Locale.jPanel7.border.title"))); // NOI18N
        jPanel7.setName(bundle.getString("ControlPanel_Locale.jPanel7.name")); // NOI18N

        jButton8.setText(bundle.getString("ControlPanel_Locale.jButton8.text")); // NOI18N
        jButton8.setName(bundle.getString("ControlPanel_Locale.jButton8.name")); // NOI18N
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        countryselect.set_Label(bundle.getString("ControlPanel_Locale.countryselect._Label")); // NOI18N
        countryselect.setName(bundle.getString("ControlPanel_Locale.countryselect.name")); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(countryselect, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton8)
                .addContainerGap(28, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jButton8)
                .addComponent(countryselect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(labeledTextChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton4))
                            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addGap(7, 7, 7)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labeledTextChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        add(jPanel5);

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setName("jPanel4"); // NOI18N

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setText(bundle.getString("ControlPanel_Locale.jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setText(bundle.getString("ControlPanel_Locale.jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        labeledTextChooser1.set_Label(bundle.getString("ControlPanel_Locale.labeledTextChooser1._Label")); // NOI18N
        labeledTextChooser1.set_LabelFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        labeledTextChooser1.setName("labeledTextChooser1"); // NOI18N

        jButton3.setText(bundle.getString("ControlPanel_Locale.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        langName.set_Label(bundle.getString("ControlPanel_Locale.langName._Label")); // NOI18N
        langName.set_LabelFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        langName.setName("langName"); // NOI18N

        jLabel7.setText(bundle.getString("ControlPanel_Locale.jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addComponent(labeledTextChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton3))
                            .addComponent(langName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 112, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(langName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton3)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(labeledTextChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        add(jPanel4);

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel3.setText(bundle.getString("ControlPanel_Locale.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setText(bundle.getString("ControlPanel_Locale.jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        languages.setName("languages"); // NOI18N

        Delete.setText(bundle.getString("ControlPanel_Locale.Delete.text")); // NOI18N
        Delete.setName("Delete"); // NOI18N
        Delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteActionPerformed(evt);
            }
        });

        export.setText(bundle.getString("ControlPanel_Locale.export.text")); // NOI18N
        export.setName("export"); // NOI18N
        export.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(languages, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(export))
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Delete)
                        .addGap(3, 3, 3))
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(99, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(languages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Delete)
                    .addComponent(export))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel3);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel2.setText(bundle.getString("ControlPanel_Locale.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel1.setText(bundle.getString("ControlPanel_Locale.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        locales.setName("locales"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                    .addComponent(jLabel2)
                    .addComponent(locales, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(locales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel2);

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton2.setText(bundle.getString("ControlPanel_Locale.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton2);

        jButton1.setText(bundle.getString("ControlPanel_Locale.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton1);

        add(jPanel6);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        setSettings();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setSettings();
        mpv5.db.objects.User.getCurrentUser().save();
        cleanup();
//        mpv5.YabsViewProxy.instance().resetTables();
        Popup.notice(Messages.RESTART_REQUIRED);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

        if (QueryHandler.instanceOf().clone(Context.getLanguage()).checkUniqueness("longname", new JTextField[]{langName.getTextField()})) {
            if (!langName.hasText()) {
                String fn = new File(labeledTextChooser1.get_Text(false)).getName();
                int end = fn.indexOf(".");
                langName.setText(fn.substring(0, end > 0 ? end : fn.length()));
            }
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    try {
                        mpv5.YabsViewProxy.instance().setWaiting(true);
                        jButton3.setEnabled(false);
                        LanguageManager.importLanguage(langName.get_Text(), new File(labeledTextChooser1.get_Text(true)));
                        User.getCurrentUser().setLanguage(langName.getText());
                        setLanguageSelection(); 
                        setSettings();
                        mpv5.db.objects.User.getCurrentUser().save();

                        Popup.notice(Messages.RESTART_REQUIRED);
                    } catch (Exception e) {
                        Log.Debug(e);
                        jButton3.setEnabled(true);
                    } finally {
                        cleanup();
                        mpv5.YabsViewProxy.instance().setWaiting(false);
                    }
                }
            };
            new Thread(runnable).start();
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if (labeledTextChooser2.hasText()) {
            LanguageManager.importCountries(new File(labeledTextChooser2.get_Text(true)));
            LanguageManager.COUNTRIES = null;
            countryselect.setModel(LanguageManager.getCountriesAsComboBoxModel());
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void DeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteActionPerformed
        if (languages.getSelectedIndex() == -1) {
            Popup.warn(Messages.LANG_EMPTY.toString());
        } else {
            String languagestring = ((MPComboBoxModelItem) languages.getSelectedItem()).getId();
            try {
                int rows = QueryHandler.instanceOf().
                        clone(Context.getUser()).
                        selectCount("LANGUAGE=",
                                "'" + languagestring + "'");
                if (rows != 0) {
                    Popup.notice(Messages.LANG_USED.toString());
                } else {
                    try {
                        LanguageManager.removeLanguage(languagestring);
                        setLanguageSelection();
                    } catch (NodataFoundException ex) {
                        Log.Debug(ex);
                    }

                }
            } catch (SQLException ex) {
                Log.Debug(ex);
            }
        }
    }//GEN-LAST:event_DeleteActionPerformed

    private void exportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportActionPerformed
        File f;
        String key;

        if (languages.getSelectedIndex() == -1) {
            Popup.warn(Messages.LANG_EMPTY.toString());
        } else {
            try {
                String lang_long = ((MPComboBoxModelItem) languages.getSelectedItem()).toString();
                String lang = ((MPComboBoxModelItem) languages.getSelectedItem()).getId();

                f = FileDirectoryHandler.getTempFile(lang_long + "_" + Constants.VERSION, "yabs");

                if (LanguageManager.exportLanguage(lang, f)) {
                    Popup.notice(Messages.LANG_EXPORTED.toString());
                }

            } catch (Exception ex) {
                Log.Debug(this, ex.getLocalizedMessage());
            }
        }
    }//GEN-LAST:event_exportActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        if (!isocode.hasText()) {
            TextFieldUtils.blinkerRed(isocode);
        }
        if (!countryname.hasText()) {
            TextFieldUtils.blinkerRed(countryname);
        } else {
            try {
                QueryData t = new QueryData();
                t.add("cname", countryname.getText());
                t.add("iso", isocode.getValue(0));
                t.add("groupsids", User.getCurrentUser().__getGroupsids());
                QueryHandler.instanceOf().clone(Context.getCountries()).insert(t, Messages.DONE.toString());
                Notificator.raiseNotification(Messages.DONE);
                LanguageManager.COUNTRIES = null;
                countryselect.setModel(LanguageManager.getCountriesAsComboBoxModel());
            } catch (Exception exception) {
                Log.Debug(LanguageManager.class, exception.getMessage());
                Notificator.raiseNotification("Isocode must be unique!");
            }
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        String country_ = countryselect.getSelectedItem().getId();
        QueryCriteria2 q = new QueryCriteria2();
        q.and(new QueryParameter(Context.getCountries(), "iso", Integer.valueOf(country_), QueryParameter.EQUALS));
        QueryHandler.instanceOf().clone(Context.getCountries()).delete(q);
        LanguageManager.COUNTRIES = null;
        countryselect.setModel(LanguageManager.getCountriesAsComboBoxModel());
        Notificator.raiseNotification(Messages.DONE);
    }//GEN-LAST:event_jButton8ActionPerformed

    @Override
    public void setValues(PropertyStore values) {
        oldvalues = values;
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
    private javax.swing.JButton Delete;
    private mpv5.ui.beans.LabeledTextField countryname;
    private mpv5.ui.beans.LabeledCombobox countryselect;
    private javax.swing.JButton export;
    private mpv5.ui.beans.LabeledTextField isocode;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private mpv5.ui.beans.LabeledTextChooser labeledTextChooser1;
    private mpv5.ui.beans.LabeledTextChooser labeledTextChooser2;
    private mpv5.ui.beans.LabeledTextField langName;
    private javax.swing.JComboBox languages;
    private javax.swing.JComboBox locales;
    // End of variables declaration//GEN-END:variables

    // End of variables declaration
    private void setLanguageSelection() {
        languages.setModel(getLanguages());
        languages.setSelectedIndex(MPComboBoxModelItem.getItemID(mpv5.db.objects.User.getCurrentUser().__getLanguage(),
                languages.getModel()));
    }
    // End of variables declaration

    private void setSettings() {

        String localestring = ((MPComboBoxModelItem) locales.getSelectedItem()).getId();
        String languagestring = ((MPComboBoxModelItem) languages.getSelectedItem()).getId();
        mpv5.db.objects.User.getCurrentUser().setLocale(localestring);
        mpv5.db.objects.User.getCurrentUser().setLanguage(languagestring);
        Locale.setDefault(TypeConversion.stringToLocale(localestring));

    }

    private DefaultComboBoxModel getLocales() {
        return LanguageManager.getLocalesAsComboBoxModel();
    }

    private ComboBoxModel getLanguages() {
        return LanguageManager.getLanguagesAsComboBoxModel();
    }

    private ComboBoxModel getCountries() {
        return LanguageManager.getCountriesAsComboBoxModel();
    }

    @Override
    public java.awt.Component getAndRemoveActionPanel() {
        this.remove(jPanel6);
        validate();
        return jPanel6;
    }

    private void cleanup() {
        for (File f : new File(FileDirectoryHandler.getTempDir2()).listFiles()) {
            f.deleteOnExit();
        }
    }
}
