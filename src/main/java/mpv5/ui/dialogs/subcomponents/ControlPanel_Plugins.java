package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
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
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.pluginhandling.MP5Plugin;
import mpv5.pluginhandling.YabsPluginLoader;
import mpv5.pluginhandling.Plugin;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.pluginhandling.UserPlugin;
import mpv5.pluginhandling.YabsPlugin;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.UnZip;

/**
 *
 * 
 */
public class ControlPanel_Plugins extends javax.swing.JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "plugins";
    private PropertyStore oldvalues;
    private static ControlPanel_Plugins ident;
    private JPopupMenu popup;

    public ControlPanel_Plugins() {
        initComponents();
        addPopupMenu();
        refresh();
        setVisible(true);
    }

    /**
     * Import a plugin
     * @param file
     * @throws FileNotFoundException
     */
    public void importf(File file) throws FileNotFoundException {
        String s = file.getName();
        if(s.endsWith(".zip")){
            file = FileDirectoryHandler.unzipFile(file);
        }
        YabsPluginLoader.importPlugin(s, file);
        refresh();
    }

    private void addPopupMenu() {
        popup = new JPopupMenu();
        JMenuItem jmi1;
        popup.add(jmi1 = new JMenuItem(Messages.ACTION_DELETE.toString()));
        popup.add(new JPopupMenu.Separator());
        JMenuItem jmi2;
        popup.add(jmi2 = new JMenuItem(Messages.ACTION_OPEN.toString()));
        popup.add(new JPopupMenu.Separator());

        jmi1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (MPSecurityManager.checkAdminAccess()) {
                    Plugin gin = (Plugin) list.getSelectedValue();
                    gin.delete();
                    refresh();
                }
            }
        });

        jmi2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Plugin gin = (Plugin) list.getSelectedValue();
                try {
                    mpv5.YabsViewProxy.instance().getPluginLoader().loadPlugin(gin);
                } catch (Exception ex) {
                    Popup.error(ex);
                } catch (Throwable ex) {
                    Popup.error(new Exception(ex));
                }
            }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        jPanel5 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        labeledTextChooser2 = new mpv5.ui.beans.LabeledTextChooser();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        //\$2java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Plugins.border.title"))); // NOI18N
        setName("Form"); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel3.setText(bundle.getString("ControlPanel_Plugins.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 11));
        jLabel4.setText(bundle.getString("ControlPanel_Plugins.jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        list.setToolTipText(bundle.getString("ControlPanel_Plugins.list.toolTipText")); // NOI18N
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
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel3)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(jPanel3);

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel5.setMaximumSize(new java.awt.Dimension(32767, 150));
        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setPreferredSize(new java.awt.Dimension(453, 100));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel8.setText(bundle.getString("ControlPanel_Plugins.jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jLabel9.setFont(new java.awt.Font("Dialog", 0, 11));
        jLabel9.setText(bundle.getString("ControlPanel_Plugins.jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        labeledTextChooser2.set_Label(bundle.getString("ControlPanel_Plugins.labeledTextChooser2._Label")); // NOI18N
        labeledTextChooser2.set_LabelFont(new java.awt.Font("Tahoma", 0, 11));
        labeledTextChooser2.setName("labeledTextChooser2"); // NOI18N

        jButton4.setText(bundle.getString("ControlPanel_Plugins.jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton3.setText(bundle.getString("ControlPanel_Plugins.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(labeledTextChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3))
                    .addComponent(jLabel9))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton4)
                        .addComponent(jButton3))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel9)
                        .addGap(9, 9, 9)
                        .addComponent(labeledTextChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(276, 276, 276))
        );

        add(jPanel5);

//$2java.awt.Color(255, 255, 255));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton2.setText(bundle.getString("ControlPanel_Plugins.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton2);

        jButton1.setText(bundle.getString("ControlPanel_Plugins.jButton1.text")); // NOI18N
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

        try {
            List data = DatabaseObject.getReferencedObjects(mpv5.db.objects.User.getCurrentUser(), Context.getPluginsToUsers());
            for (int i = 0; i < data.size(); i++) {
                ((UserPlugin) data.get(i)).delete();
            }
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }

        Object[] plugs = list.getSelectedValues();

        for (int i = 0; i < plugs.length; i++) {
            Plugin gin = (Plugin) plugs[i];

            UserPlugin up = new UserPlugin();
            up.setUsersids(mpv5.db.objects.User.getCurrentUser().__getIDS());
            up.setPluginsids(gin.__getIDS());
            up.setCname(gin.__getCname());
            up.save();
        }
}//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        Object[] plugs = list.getSelectedValues();
        for (int i = 0; i < plugs.length; i++) {
            Plugin gin = (Plugin) plugs[i];
            try {
                mpv5.YabsViewProxy.instance().getPluginLoader().loadPlugin(gin);
            } catch (Exception ex) {
                Popup.error(ex);
            } catch (Throwable ex) {
                Popup.error(new Exception(ex));
            }
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        YabsPlugin p = null;
        try {
            if (YabsPluginLoader.checkPlugin(new File(labeledTextChooser2.get_Text(true))) != null) {
                p = YabsPluginLoader.checkPlugin(new File(labeledTextChooser2.get_Text(true)));
                try {
                    YabsPluginLoader.loadPlugin(p);
                } catch (Exception ex) {
                    Popup.error(ex);
                } catch (Throwable ex) {
                    Popup.error(new Exception(ex));
                }
            } else {
                Popup.notice(Messages.ERROR_OCCURED);
            }
        } catch (Exception e) {
            Popup.notice(Messages.ERROR_OCCURED);
        }
}//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if (labeledTextChooser2.hasText()) {
            try {
                importf(new File(labeledTextChooser2.get_Text(true)));
            } catch (FileNotFoundException ex) {
                Popup.error(ex);
            }
        }
}//GEN-LAST:event_jButton4ActionPerformed

    private void listMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMouseClicked

        if (SwingUtilities.isRightMouseButton(evt) && !list.isSelectionEmpty() && list.locationToIndex(evt.getPoint()) == list.getSelectedIndex()) {
            popup.show(list, evt.getX(), evt.getY());
        }
}//GEN-LAST:event_listMouseClicked

    public void setValues(PropertyStore values) {
        oldvalues = values;
    }

    public String getUname() {
        return UNAME;
    }

    public void reset() {
        setValues(oldvalues);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private mpv5.ui.beans.LabeledTextChooser labeledTextChooser2;
    private javax.swing.JList list;
    // End of variables declaration//GEN-END:variables

    private void refresh() {
        DefaultListModel xl = new DefaultListModel();

        ArrayList<Plugin> data = new ArrayList<Plugin>();
        try {
            data = DatabaseObject.getObjects(Context.getPlugins());
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        ArrayList<UserPlugin> udata = new ArrayList<UserPlugin>();
        try {
            udata = DatabaseObject.getObjects(Context.getPluginsToUsers());
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        ArrayList<Integer> selected = new ArrayList<Integer>();

        Log.Debug(this, "Plugins found: " + data.size());

        for (int i = 0; i < data.size(); i++) {
            Plugin lugin = data.get(i);
            xl.addElement(lugin);

            for (int ij = 0; ij < udata.size(); ij++) {
                UserPlugin userPlugin = udata.get(ij);
                if (lugin.__getIDS().intValue() == userPlugin.__getPluginsids()) {
                    selected.add(ij);
                }
            }
        }

        int[] sel = new int[selected.size()];

        for (int i = 0; i < selected.size(); i++) {
            Integer integer = selected.get(i);
            sel[i] = integer.intValue();
        }

        list.setModel(xl);
        list.setSelectedIndices(sel);
    }

    public Component getAndRemoveActionPanel() {
        this.remove(jPanel2);
        validate();
        return jPanel2;
    }
}
