package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
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
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.text.RandomStringUtils;
import mpv5.utils.text.RandomText;
import mpv5.webshopinterface.NoCompatibleHostFoundException;
import mpv5.webshopinterface.WSConnectionClient;
import mpv5.webshopinterface.WSDaemon;
import mpv5.webshopinterface.WSIManager;
import mpv5.webshopinterface.wsdjobs.addContactJob;
import mpv5.webshopinterface.wsdjobs.newContactsJob;
import mpv5.webshopinterface.wsdjobs.newOrdersJob;
import mpv5.webshopinterface.wsdjobs.newSystemMessages;
import mpv5.webshopinterface.wsdjobs.updatedContactsJob;
import mpv5.webshopinterface.wsdjobs.updatedOrdersJob;

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

        groupselect.setSearchOnEnterEnabled(true);
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
                    mpv5.ui.dialogs.Popup.notice(new WSConnectionClient(new URL(gin.__getUrl())).test());
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
        jLabel2 = new javax.swing.JLabel();
        groupselect = new mpv5.ui.beans.LabeledCombobox();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_WebShopManager.border.title"))); // NOI18N
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(300, 300));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel5.setMaximumSize(new java.awt.Dimension(32767, 250));
        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setPreferredSize(new java.awt.Dimension(453, 200));

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

        jLabel2.setText(bundle.getString("ControlPanel_WebShopManager.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        groupselect.set_Label(bundle.getString("ControlPanel_WebShopManager.groupselect._Label")); // NOI18N
        groupselect.setName("groupselect"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(groupselect, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                        .addComponent(intervals, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE))
                    .addComponent(urls, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE))
                .addGap(10, 10, 10)
                .addComponent(jButton3)
                .addGap(136, 136, 136))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(groupselect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel2)
                    .addComponent(intervals, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(urls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE))
                .addContainerGap())
        );

        add(jPanel5);

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
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                    .addComponent(jLabel3))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(jPanel3);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

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

        add(jPanel2);
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
        } else if (evt.getClickCount() > 1) {
            if (list.getSelectedValue() != null) {
                WebShop ws = (WebShop) list.getSelectedValue();
                intervals.set_Value(Integer.valueOf(ws.__getInterval()));
                descr.setText(ws.__getDescription());
                urls.setText(ws.__getUrl());
                groupselect.setSelectedIndex(MPComboBoxModelItem.getItemID(String.valueOf(ws.__getGroupsids()), groupselect.getModel()));

            }
        }

}//GEN-LAST:event_listMouseClicked

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            mpv5.ui.dialogs.Popup.notice(new WSConnectionClient(new URL(urls.getText())).test());
            WSDaemon d = new WSDaemon(new URL(urls.getText()));
            d.start(true);
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
            Log.Debug(x);
            mpv5.ui.dialogs.Popup.error(x);
        }
}//GEN-LAST:event_jButton4ActionPerformed

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
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList list;
    private mpv5.ui.beans.LabeledTextField urls;
    // End of variables declaration//GEN-END:variables

    private void refresh() {
        groupselect.triggerSearch();
        intervals.setValue(300);
        list.setModel(new DefaultListModel());
        list.validate();
        try {
            ArrayList<DatabaseObject> data = DatabaseObject.getObjects(Context.getWebShops());
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
        ws.setInterval(Integer.valueOf(intervals.getSpinner().getValue().toString()));
        if (groupselect.getSelectedItem() != null) {
            ws.setGroupsids(Integer.valueOf((groupselect.getSelectedItem()).getId()));
        } else {
            ws.setGroupsids(1);
        }
        ws.save();
        refresh();
    }
}
