package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.WebShop;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.Popup;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileReaderWriter;
import mpv5.utils.files.Zip;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.text.Base64Encoder; 
import mpv5.utils.text.RandomText;
import mpv5.webshopinterface.NoCompatibleHostFoundException;
import mpv5.webshopinterface.WSConnectionClient;
import mpv5.webshopinterface.WSDaemon;
import mpv5.webshopinterface.WSIManager;
import mpv5.webshopinterface.wsdjobs.*;
import org.bouncycastle.util.encoders.Base64;


/**
 *
 * 
 */
public class ControlPanel_WebShopManager extends javax.swing.JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "webshops";
    private PropertyStore oldvalues;
    private static ControlPanel_WebShopManager ident;
    private JPopupMenu popup;

    public ControlPanel_WebShopManager() {
        initComponents();
        addPopupMenu();

        groupselect.setSearchEnabled(true);
        groupselect.setContext(Context.getGroup());
        refresh();

        setVisible(true);
    }

    private void addPopupMenu() {
        popup = new JPopupMenu();
        JMenuItem jmi1;
        popup.add(jmi1 = new JMenuItem(Messages.ACTION_DELETE.toString()));
        popup.add(new JPopupMenu.Separator());
        JMenuItem jmi2;
        popup.add(jmi2 = new JMenuItem(Messages.ACTION_TEST.toString()));
        popup.add(new JPopupMenu.Separator());

        jmi1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (MPSecurityManager.checkAdminAccess()) {
                    WebShop gin = (WebShop) list.getSelectedValue();
                    gin.delete();
                    WSIManager.instanceOf().reset();
                    WSIManager.instanceOf().start();
                    refresh();
                }
            }
        });

        jmi2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                WebShop gin = (WebShop) list.getSelectedValue();
                try {
                    if (jCheckBox1.isSelected()) {
                        mpv5.ui.dialogs.Popup.notice(new WSConnectionClient(new URL(gin.__getUrl()), isrequestCompression.isSelected(), jTextField1.getText(), new String(jPasswordField1.getPassword())).test());
                    } else {
                        mpv5.ui.dialogs.Popup.notice(new WSConnectionClient(new URL(gin.__getUrl()), isrequestCompression.isSelected(), null, null).test());
                    }

                } catch (Exception x) {
                    Log.Debug(x);
                    mpv5.ui.dialogs.Popup.error(x);
                }
            }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel5 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        urls = new mpv5.ui.beans.LabeledTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        descr = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        intervals = new mpv5.ui.beans.LabeledSpinner();
        groupselect = new mpv5.ui.beans.LabeledCombobox();
        isrequestCompression = new javax.swing.JCheckBox();
        jCheckBox1 = new javax.swing.JCheckBox();
        jTextField1 = new javax.swing.JTextField();
        jPasswordField1 = new javax.swing.JPasswordField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        //\$2java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_WebShopManager.border.title"))); // NOI18N
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(300, 300));
        setLayout(new java.awt.BorderLayout());

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel5.setMaximumSize(new java.awt.Dimension(32767, 250));
        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setPreferredSize(new java.awt.Dimension(453, 250));

        jButton3.setText(bundle.getString("ControlPanel_WebShopManager.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        urls.set_Label(bundle.getString("ControlPanel_WebShopManager.urls._Label")); // NOI18N
        urls.setName("urls"); // NOI18N

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        descr.setColumns(20);
        descr.setLineWrap(true);
        descr.setRows(5);
        descr.setWrapStyleWord(true);
        descr.setName("descr"); // NOI18N
        jScrollPane2.setViewportView(descr);

        jLabel1.setText(bundle.getString("ControlPanel_WebShopManager.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        intervals.set_Label(bundle.getString("ControlPanel_WebShopManager.intervals._Label")); // NOI18N
        intervals.setName("intervals"); // NOI18N

        groupselect.set_Label(bundle.getString("ControlPanel_WebShopManager.groupselect._Label")); // NOI18N
        groupselect.setName("groupselect"); // NOI18N

        isrequestCompression.setText(bundle.getString("ControlPanel_WebShopManager.isrequestCompression.text")); // NOI18N
        isrequestCompression.setName("isrequestCompression"); // NOI18N

        jCheckBox1.setText(bundle.getString("ControlPanel_WebShopManager.jCheckBox1.text")); // NOI18N
        jCheckBox1.setName("jCheckBox1"); // NOI18N
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox1ItemStateChanged(evt);
            }
        });

        jTextField1.setText(bundle.getString("ControlPanel_WebShopManager.jTextField1.text")); // NOI18N
        jTextField1.setEnabled(false);
        jTextField1.setName("jTextField1"); // NOI18N

        jPasswordField1.setText(bundle.getString("ControlPanel_WebShopManager.jPasswordField1.text")); // NOI18N
        jPasswordField1.setEnabled(false);
        jPasswordField1.setName("jPasswordField1"); // NOI18N

        jLabel4.setText(bundle.getString("ControlPanel_WebShopManager.jLabel4.text")); // NOI18N
        jLabel4.setEnabled(false);
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setText(bundle.getString("ControlPanel_WebShopManager.jLabel5.text")); // NOI18N
        jLabel5.setEnabled(false);
        jLabel5.setName("jLabel5"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(groupselect, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(urls, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(isrequestCompression)
                                .addGap(18, 18, 18)
                                .addComponent(intervals, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jCheckBox1))
                        .addGap(110, 110, 110))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(groupselect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jButton3, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addComponent(urls, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(isrequestCompression)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox1))
                    .addComponent(intervals, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(56, 56, 56))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        add(jPanel5, java.awt.BorderLayout.NORTH);

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setPreferredSize(new java.awt.Dimension(605, 400));

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel3.setText(bundle.getString("ControlPanel_WebShopManager.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        list.setToolTipText(bundle.getString("ControlPanel_WebShopManager.list.toolTipText")); // NOI18N
        list.setName("list"); // NOI18N
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(list);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                    .addComponent(jLabel3))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(jPanel3, java.awt.BorderLayout.CENTER);

//$2java.awt.Color(255, 255, 255));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton2.setText(bundle.getString("ControlPanel_WebShopManager.jButton2.text")); // NOI18N
        jButton2.setEnabled(false);
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton2);

        jButton5.setText(bundle.getString("ControlPanel_WebShopManager.jButton5.text")); // NOI18N
        jButton5.setName("jButton5"); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton5);

        jButton4.setText(bundle.getString("ControlPanel_WebShopManager.jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton4);

        jButton1.setText(bundle.getString("ControlPanel_WebShopManager.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1);

        add(jPanel2, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (list.getSelectedValue() != null) {
            try {
                save((WebShop) list.getSelectedValue());
                refresh();
                WSIManager.instanceOf().reset();
                WSIManager.instanceOf().start();
            } catch (Exception x) {
                Log.Debug(x);
                mpv5.ui.dialogs.Popup.error(x);
            }
        }

}//GEN-LAST:event_jButton1ActionPerformed

    private void listMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMouseClicked

        if (SwingUtilities.isRightMouseButton(evt) && !list.isSelectionEmpty() && list.locationToIndex(evt.getPoint()) == list.getSelectedIndex()) {
            popup.show(list, evt.getX(), evt.getY());
        } else if (evt.getClickCount() >= 1) {
            if (list.getSelectedValue() != null) {
                WebShop ws = (WebShop) list.getSelectedValue();
                intervals.set_Value(Integer.valueOf(ws.__getInterv()));
                descr.setText(ws.__getDescription());
                urls.setText(ws.__getUrl());
                groupselect.setSelectedIndex(MPComboBoxModelItem.getItemID(String.valueOf(ws.__getGroupsids()), groupselect.getModel()));
                isrequestCompression.setSelected(ws.__getIsrequestCompression());
                jCheckBox1.setSelected(ws.__getIsauthenticated());
                jTextField1.setText(ws.__getUsername());
                jPasswordField1.setText(ws.__getPassw());
            }
        }
}//GEN-LAST:event_listMouseClicked

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            if (jCheckBox1.isSelected()) {
                mpv5.ui.dialogs.Popup.notice(new WSConnectionClient(new URL(urls.getText()), isrequestCompression.isSelected(), jTextField1.getText(), new String(jPasswordField1.getPassword())).test());
            } else {
                mpv5.ui.dialogs.Popup.notice(new WSConnectionClient(new URL(urls.getText()), isrequestCompression.isSelected(), null, null).test());
            }
        } catch (Exception x) {
            Log.Debug(x);
            mpv5.ui.dialogs.Popup.error(x);
        }
}//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

        try {
            WebShop ws = new WebShop();
            save(ws);
            WSIManager.instanceOf().reset();
            WSIManager.instanceOf().start();
        } catch (Exception x) {
//            Log.Debug(x);
            mpv5.ui.dialogs.Popup.error(x);
        }
}//GEN-LAST:event_jButton4ActionPerformed

    private void jCheckBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged
        jPasswordField1.setEnabled(jCheckBox1.isSelected());
        jTextField1.setEnabled(jCheckBox1.isSelected());
        jLabel5.setEnabled(jCheckBox1.isSelected());
        jLabel4.setEnabled(jCheckBox1.isSelected());
        jButton2.setEnabled(jCheckBox1.isSelected());
    }//GEN-LAST:event_jCheckBox1ItemStateChanged

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
            generate();
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        try {
            WebShop ws = (WebShop) list.getSelectedValue();
            WSDaemon d = new WSDaemon(ws);
            d.addJob(new newContactsJob(d));
            d.addJob(new newOrdersJob(d));
            d.addJob(new newSystemMessages(d));
            d.addJob(new updatedContactsJob(d));
            d.addJob(new updatedOrdersJob(d));
            d.addJob(new addContactJob(d));
            d.start();
        } catch (Exception ex) {
            Notificator.raiseNotification(ex, true);
        }
    }//GEN-LAST:event_jButton5ActionPerformed

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
    private javax.swing.JTextArea descr;
    private mpv5.ui.beans.LabeledCombobox groupselect;
    private mpv5.ui.beans.LabeledSpinner intervals;
    private javax.swing.JCheckBox isrequestCompression;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JList list;
    private mpv5.ui.beans.LabeledTextField urls;
    // End of variables declaration//GEN-END:variables

    private void refresh() {
        groupselect.triggerSearch();
        intervals.setValue(300);
        list.setModel(new DefaultListModel());
        list.validate();
        try {
            ArrayList<DatabaseObject> data = DatabaseObject.getObjects(Context.getWebShop());
            DefaultListModel xl = new DefaultListModel();
            Log.Debug(this, "Shops found: " + data.size());
            for (int i = 0; i < data.size(); i++) {
                DatabaseObject databaseObject = data.get(i);
                xl.addElement((WebShop) databaseObject);
            }
            list.setModel(xl);
        } catch (NodataFoundException nodataFoundException) {
            Log.Debug(this, nodataFoundException.getMessage());
        }
    }

    @Override
    public Component getAndRemoveActionPanel() {
        this.remove(jPanel2);
        validate();
        return jPanel2;
    }

    private void save(WebShop ws) throws MalformedURLException {
        new URL(urls.getText());//test 
        ws.setUrl(urls.getText());
        ws.setDescription(descr.getText());
        ws.setInterv(Integer.valueOf(intervals.getSpinner().getValue().toString()));
        ws.setIsrequestCompression(isrequestCompression.isSelected());
        ws.setIsauthenticated(jCheckBox1.isSelected());
        ws.setUsername(jTextField1.getText());
        ws.setPassw(new String(jPasswordField1.getPassword()));
        if (groupselect.getSelectedItem() != null) {
            ws.setGroupsids(Integer.valueOf((groupselect.getSelectedItem()).getId()));
        } else {
            ws.setGroupsids(1);
        }
        ws.save();
        refresh();
    }

    @SuppressWarnings("Deprecated")
    private void generate() throws NoSuchAlgorithmException, IOException {
        try {
            String pwdir = RandomText.getText();
            String user = RandomText.getText();
            String password = RandomText.getString();
            Log.Debug(this, "The SUPER SECRET password for the generated .htaccess file is: " + password);
            //apache codec base64encoder produces wrong values here
            String crypt = "{SHA}" + Base64.encode(java.security.MessageDigest.getInstance("SHA1").digest(password.getBytes()));
            String content1 =
                    "AuthUserFile " + pwdir + "/.htpasswd\n" +
                    "AuthGroupFile /dev/null\n" +
                    "AuthName \"" + user + "\"\n" +
                    "AuthType Basic\n" +
                    "<Limit GET POST>\n" +
                    "require valid-user\n" +
                    "</Limit>\n" +
                    "<Files *.ini>\n" +
                    "Order Deny,Allow\n" +
                    "Deny from all\n" +
                    "</Files>\n";

            String content2 = user + ":" + crypt;
            String tmp = FileDirectoryHandler.getTempDir() + RandomText.getText();
            File f1 = new File(tmp + File.separator + ".htaccess");
            f1.getParentFile().mkdirs();
            f1.createNewFile();
            File f2 = new File(tmp + File.separator + pwdir + File.separator + ".htpasswd");
            f2.getParentFile().mkdirs();
            f2.createNewFile();
            FileReaderWriter htacc = new FileReaderWriter(f1);
            FileReaderWriter htpw = new FileReaderWriter(f2);

            if (htacc.write0(content1) && htpw.write0(content2)) {
                DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY);
                d.setSelectedFile(new File("generated.zip"));
                if (d.saveFile()) {
                    Zip.zip(tmp, d.getFile().getPath());
                }

                jTextField1.setText(user);
                jPasswordField1.setText(password);
            }
        } catch (Exception e) {
            Popup.error(e);
            Log.Debug(e);
        }
    }
}
