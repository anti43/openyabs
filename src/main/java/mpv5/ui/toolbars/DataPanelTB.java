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
 * ContactsTB.java
 *
 * Created on 25.01.2009, 16:54:29
 */
package mpv5.ui.toolbars;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.globals.Messages;
import mpv5.db.objects.Favourite;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.panels.ChangeNotApprovedException;
import mpv5.ui.panels.DataPanel;
import mpv5.ui.panels.ExportablePanel;

/**
 *
 *
 */
public class DataPanelTB extends javax.swing.JPanel implements ActionListener, ItemListener {

    private static final long serialVersionUID = -8215471082724735228L;
    private DataPanel parents;
    private final ActionListener action1 = new java.awt.event.ActionListener() {

        public void actionPerformed(java.awt.event.ActionEvent evt) {
            favAdd(evt);
        }
    };
    private final ActionListener action2 = new java.awt.event.ActionListener() {

        public void actionPerformed(java.awt.event.ActionEvent evt) {
            favRemover(evt);
        }
    };

    /**
     *
     * @param panel
     */
    public DataPanelTB(DataPanel panel) {
        initComponents();
        parents = panel;
        parents.showSearchBar(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(this, "jToggleButton1"));
        but1.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(this, "jToggleButton1"));
        but6.setEnabled(!DatabaseObject.isAutoLockEnabled());

        if (!(parents instanceof ExportablePanel)) {
            commonActionsToolbar.remove(odtButton);
            commonActionsToolbar.remove(pdfButton);
            commonActionsToolbar.validate();
        }
    }

    /**
     * Disable the button with the given number (from left to right)
     *
     * @param button
     */
    public void disableButton(int button) {
        Component[] c = commonActionsToolbar.getComponents();
        for (int i = 0; i < c.length; i++) {
            Component component = c[i];
            if (component.getName().equals("but" + button)) {
                (component).setEnabled(false);
            }
        }
    }

    /**
     *
     * @param on
     */
    public void setEditable(boolean on) {
        but3.setEnabled(on);
        but5.setEnabled(on);
        but6.setEnabled(on);
    }

    /**
     *
     * @param favourite
     */
    public void setFavourite(boolean favourite) {
        if (!favourite) {
            but7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/bookmark_add.png"))); // NOI18N
            but7.setFocusable(false);
            but7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            but7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            but7.removeActionListener(action2);
            but7.removeActionListener(action1);
            but7.addActionListener(action1);
        } else {
            but7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/bookmark.png"))); // NOI18N
            but7.setFocusable(false);
            but7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            but7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            but7.removeActionListener(action1);
            but7.removeActionListener(action2);
            but7.addActionListener(action2);
        }
        this.validate();
    }

    public void save() {
        try {
            DatabaseObject dato = parents.getDataOwner();
            parents.actionBeforeSave();
            if (dato.getPanelData(parents) && dato.save()) {
                try {
                    parents.actionAfterSave();
                    parents.setDataOwner(dato, true);
                } catch (Exception e) {
                    Log.Debug(this, e);
                }
            } else {
                parents.showRequiredFields();
            }
        } catch (ChangeNotApprovedException ex) {
            Log.Debug(this, ex.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        commonActionsToolbar = new javax.swing.JToolBar();
        but1 = new javax.swing.JToggleButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        but2 = new javax.swing.JButton();
        but3 = new javax.swing.JButton();
        but4 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        but5 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        but6 = new javax.swing.JButton();
        but7 = new javax.swing.JButton();
        but10 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        printButton = new javax.swing.JButton();
        mailButton = new javax.swing.JButton();
        pdfButton = new javax.swing.JButton();
        odtButton = new javax.swing.JButton();

        setName("Form"); // NOI18N

        commonActionsToolbar.setFloatable(false);
        commonActionsToolbar.setRollover(true);
        commonActionsToolbar.setName("commonActionsToolbar"); // NOI18N
        commonActionsToolbar.setPreferredSize(new java.awt.Dimension(342, 41));

        but1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/viewmag.png"))); // NOI18N
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        but1.setText(bundle.getString("DataPanelTB.but1.text")); // NOI18N
        but1.setToolTipText(bundle.getString("DataPanelTB.but1.toolTipText")); // NOI18N
        but1.setContentAreaFilled(false);
        but1.setFocusable(false);
        but1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but1.setName("but1"); // NOI18N
        but1.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/bright_viewmag.png"))); // NOI18N
        but1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but1.addItemListener(this);
        but1.addActionListener(this);
        commonActionsToolbar.add(but1);

        jSeparator4.setName("jSeparator4"); // NOI18N
        commonActionsToolbar.add(jSeparator4);

        but2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/revert.png"))); // NOI18N
        but2.setText(bundle.getString("MPV5View.jButton22.text")); // NOI18N
        but2.setToolTipText(bundle.getString("MPV5View.jButton22.toolTipText")); // NOI18N
        but2.setContentAreaFilled(false);
        but2.setFocusable(false);
        but2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but2.setName("but2"); // NOI18N
        but2.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        but2.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/bright_revert.png"))); // NOI18N
        but2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but2.addActionListener(this);
        commonActionsToolbar.add(but2);

        but3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/3floppy_unmount.png"))); // NOI18N
        but3.setText(bundle.getString("MPV5View.jButton25.text")); // NOI18N
        but3.setToolTipText(bundle.getString("MPV5View.jButton25.toolTipText")); // NOI18N
        but3.setContentAreaFilled(false);
        but3.setFocusable(false);
        but3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but3.setName("but3"); // NOI18N
        but3.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        but3.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/bright_3floppy_unmount.png"))); // NOI18N
        but3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but3.addActionListener(this);
        commonActionsToolbar.add(but3);

        but4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/filenew.png"))); // NOI18N
        but4.setText(bundle.getString("MPV5View.jButton4.text")); // NOI18N
        but4.setToolTipText(bundle.getString("MPV5View.jButton4.toolTipText")); // NOI18N
        but4.setContentAreaFilled(false);
        but4.setFocusable(false);
        but4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but4.setName("but4"); // NOI18N
        but4.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        but4.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/bright_filenew.png"))); // NOI18N
        but4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but4.addActionListener(this);
        commonActionsToolbar.add(but4);

        jSeparator1.setName("jSeparator1"); // NOI18N
        commonActionsToolbar.add(jSeparator1);

        but5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/edittrash.png"))); // NOI18N
        but5.setText(bundle.getString("MPV5View.jButton26.text")); // NOI18N
        but5.setToolTipText(bundle.getString("MPV5View.jButton26.toolTipText")); // NOI18N
        but5.setContentAreaFilled(false);
        but5.setFocusable(false);
        but5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but5.setName("but5"); // NOI18N
        but5.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        but5.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/bright_edittrash.png"))); // NOI18N
        but5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but5.addActionListener(this);
        commonActionsToolbar.add(but5);

        jSeparator3.setName("jSeparator3"); // NOI18N
        commonActionsToolbar.add(jSeparator3);

        but6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/password.png"))); // NOI18N
        but6.setText(bundle.getString("MPV5View.jButton21.text")); // NOI18N
        but6.setToolTipText(bundle.getString("MPV5View.jButton21.toolTipText")); // NOI18N
        but6.setContentAreaFilled(false);
        but6.setFocusable(false);
        but6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but6.setName("but6"); // NOI18N
        but6.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        but6.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/bright_password.png"))); // NOI18N
        but6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but6.addActionListener(this);
        commonActionsToolbar.add(but6);

        but7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/bookmark_add.png"))); // NOI18N
        but7.setText(bundle.getString("MPV5View.jButton24.text")); // NOI18N
        but7.setToolTipText(bundle.getString("MPV5View.jButton24.toolTipText")); // NOI18N
        but7.setContentAreaFilled(false);
        but7.setFocusable(false);
        but7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but7.setName("but7"); // NOI18N
        but7.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        but7.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/bright_bookmark_add.png"))); // NOI18N
        but7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but7.addActionListener(this);
        commonActionsToolbar.add(but7);

        but10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/add.png"))); // NOI18N
        but10.setText(bundle.getString("DataPanelTB.but10.text")); // NOI18N
        but10.setToolTipText(bundle.getString("DataPanelTB.but10.toolTipText")); // NOI18N
        but10.setContentAreaFilled(false);
        but10.setFocusable(false);
        but10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but10.setName("but10"); // NOI18N
        but10.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        but10.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but10.addActionListener(this);
        commonActionsToolbar.add(but10);

        jSeparator2.setName("jSeparator2"); // NOI18N
        commonActionsToolbar.add(jSeparator2);

        printButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/printer.png"))); // NOI18N
        printButton.setText(bundle.getString("MPV5View.jButton23.text")); // NOI18N
        printButton.setToolTipText(bundle.getString("MPV5View.jButton23.toolTipText")); // NOI18N
        printButton.setContentAreaFilled(false);
        printButton.setFocusable(false);
        printButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        printButton.setName("printButton"); // NOI18N
        printButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        printButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/bright_printer.png"))); // NOI18N
        printButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        printButton.addActionListener(this);
        commonActionsToolbar.add(printButton);

        mailButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/mail_reply.png"))); // NOI18N
        mailButton.setText(bundle.getString("DataPanelTB.mailButton.text")); // NOI18N
        mailButton.setToolTipText(bundle.getString("DataPanelTB.mailButton.toolTipText")); // NOI18N
        mailButton.setContentAreaFilled(false);
        mailButton.setFocusable(false);
        mailButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        mailButton.setName("mailButton"); // NOI18N
        mailButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        mailButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/bright_mail_reply.png"))); // NOI18N
        mailButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mailButton.addActionListener(this);
        commonActionsToolbar.add(mailButton);

        pdfButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/pdf.png"))); // NOI18N
        pdfButton.setText(bundle.getString("DataPanelTB.pdfButton.text")); // NOI18N
        pdfButton.setToolTipText(bundle.getString("DataPanelTB.pdfButton.toolTipText")); // NOI18N
        pdfButton.setContentAreaFilled(false);
        pdfButton.setFocusable(false);
        pdfButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pdfButton.setName("pdfButton"); // NOI18N
        pdfButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        pdfButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        pdfButton.addActionListener(this);
        commonActionsToolbar.add(pdfButton);

        odtButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/openofficeorg-20-writer.png"))); // NOI18N
        odtButton.setText(bundle.getString("DataPanelTB.odtButton.text")); // NOI18N
        odtButton.setToolTipText(bundle.getString("DataPanelTB.odtButton.toolTipText")); // NOI18N
        odtButton.setContentAreaFilled(false);
        odtButton.setFocusable(false);
        odtButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        odtButton.setName("odtButton"); // NOI18N
        odtButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        odtButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        odtButton.addActionListener(this);
        commonActionsToolbar.add(odtButton);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(commonActionsToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(commonActionsToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == but1) {
            DataPanelTB.this.but1ActionPerformed(evt);
        }
        else if (evt.getSource() == but2) {
            DataPanelTB.this.but2ActionPerformed(evt);
        }
        else if (evt.getSource() == but3) {
            DataPanelTB.this.but3ActionPerformed(evt);
        }
        else if (evt.getSource() == but4) {
            DataPanelTB.this.but4ActionPerformed(evt);
        }
        else if (evt.getSource() == but5) {
            DataPanelTB.this.but5ActionPerformed(evt);
        }
        else if (evt.getSource() == but6) {
            DataPanelTB.this.but6ActionPerformed(evt);
        }
        else if (evt.getSource() == but7) {
            DataPanelTB.this.but7ActionPerformed(evt);
        }
        else if (evt.getSource() == but10) {
            DataPanelTB.this.but10ActionPerformed(evt);
        }
        else if (evt.getSource() == printButton) {
            DataPanelTB.this.printButtonActionPerformed(evt);
        }
        else if (evt.getSource() == mailButton) {
            DataPanelTB.this.mailButtonActionPerformed(evt);
        }
        else if (evt.getSource() == pdfButton) {
            DataPanelTB.this.pdfButtonActionPerformed(evt);
        }
        else if (evt.getSource() == odtButton) {
            DataPanelTB.this.odtButtonActionPerformed(evt);
        }
    }

    public void itemStateChanged(java.awt.event.ItemEvent evt) {
        if (evt.getSource() == but1) {
            DataPanelTB.this.but1ItemStateChanged(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void but2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but2ActionPerformed
        DatabaseObject dato = parents.getDataOwner();

        if (dato.isExisting()) {
            dato.getPanelData((parents));
            dato.reset();
            parents.setDataOwner(dato, true);
        }
}//GEN-LAST:event_but2ActionPerformed

    private void but3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but3ActionPerformed
        save();
    }//GEN-LAST:event_but3ActionPerformed

    private void but4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but4ActionPerformed
        DatabaseObject dato = parents.getDataOwner();

        boolean d = dato.getPanelData(parents);
        dato.setIDS(-1);
        parents.actionBeforeCreate();

        if (d && dato.save()) {
            try {
                parents.actionAfterCreate();
                parents.setDataOwner(dato, true);
            } catch (Exception e) {
                Log.Debug(this, e);
            }
        } else {
            parents.showRequiredFields();
        }
}//GEN-LAST:event_but4ActionPerformed

    private void but6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but6ActionPerformed
        DatabaseObject dato = parents.getDataOwner();
        if (mpv5.db.objects.User.getCurrentUser().isDefault()) {
            Popup.notice(Messages.DEFAULT_USER);
        } else if (dato.isExisting()) {
            if (dato.lock()) {
                Popup.notice(dato.toString() + Messages.LOCKED);
                but6.setEnabled(false);
            } else {
                Popup.notice(Messages.LOCK_FAILED);
            }
        } else {
            Popup.notice(Messages.NOT_SAVED_YET);
        }
}//GEN-LAST:event_but6ActionPerformed

    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed

        DatabaseObject dato = parents.getDataOwner();
        if (dato.isExisting()) {
            parents.print();

            final String fmessage = Messages.PRINTED + dato.__getCname();
            final String fdbid = dato.getDbIdentity();
            final int fids = dato.__getIDS();
            final int fgids = dato.__getGroupsids();
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(fmessage, mpv5.db.objects.User.getCurrentUser().__getCname(), fdbid, fids, fgids);
                }
            };
            new Thread(runnable).start();
        }
}//GEN-LAST:event_printButtonActionPerformed

    private void but5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but5ActionPerformed

        DatabaseObject dato = parents.getDataOwner();
        if (dato.isExisting() && Popup.Y_N_dialog(Messages.REALLY_DELETE)) {
            dato.getPanelData(parents);
            dato.delete();
            parents.refresh();
            parents.setDataOwner(dato.getObject(dato.getContext()), true);
        }
    }//GEN-LAST:event_but5ActionPerformed

    private void but7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but7ActionPerformed
    }//GEN-LAST:event_but7ActionPerformed

    private void but1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but1ActionPerformed

        parents.showSearchBar(but1.isSelected());
    }//GEN-LAST:event_but1ActionPerformed

    private void but1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_but1ItemStateChanged
        mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty(this, "jToggleButton1", but1.isSelected());
    }//GEN-LAST:event_but1ItemStateChanged

    private void mailButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mailButtonActionPerformed

        DatabaseObject dato = parents.getDataOwner();
        if (dato.isExisting()) {
            parents.mail();
        }
    }//GEN-LAST:event_mailButtonActionPerformed

    private void but10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but10ActionPerformed

        DatabaseObject dato = parents.getDataOwner();
        if (dato.isExisting()) {
            mpv5.YabsViewProxy.instance().addToClipBoard(dato);
//            mpv5.YabsViewProxy.instance().setClipBoardVisible(true);
        }
    }//GEN-LAST:event_but10ActionPerformed

    private void pdfButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfButtonActionPerformed

        DatabaseObject dato = parents.getDataOwner();
        if (dato.isExisting() && parents instanceof ExportablePanel) {
            ((ExportablePanel) parents).pdf();
        }

    }//GEN-LAST:event_pdfButtonActionPerformed

    private void odtButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_odtButtonActionPerformed
        DatabaseObject dato = parents.getDataOwner();
        if (dato.isExisting()) {
            ((ExportablePanel) parents).odt();
        }

    }//GEN-LAST:event_odtButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton but1;
    private javax.swing.JButton but10;
    private javax.swing.JButton but2;
    private javax.swing.JButton but3;
    private javax.swing.JButton but4;
    private javax.swing.JButton but5;
    private javax.swing.JButton but6;
    private javax.swing.JButton but7;
    private javax.swing.JToolBar commonActionsToolbar;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JButton mailButton;
    private javax.swing.JButton odtButton;
    private javax.swing.JButton pdfButton;
    private javax.swing.JButton printButton;
    // End of variables declaration//GEN-END:variables

    private void favRemover(java.awt.event.ActionEvent evt) {

        DatabaseObject dato = parents.getDataOwner();
        if (!mpv5.db.objects.User.getCurrentUser().isDefault() && dato.isExisting()) {
            dato.getPanelData(parents);
            if (dato.save()) {
                Favourite.removeFavourite(dato);
                mpv5.YabsViewProxy.instance().reloadFavorites();
                parents.setDataOwner(dato, true);
            } else {
                parents.showRequiredFields();
            }
        }
    }

    private void favAdd(java.awt.event.ActionEvent evt) {
        DatabaseObject dato = parents.getDataOwner();
        Favourite fav = null;
        if (!mpv5.db.objects.User.getCurrentUser().isDefault()) {
            if (dato.isExisting()) {
                dato.getPanelData(parents);
                if (dato.save()) {
                    fav = new Favourite(dato);
                    fav.save();
                    mpv5.YabsViewProxy.instance().reloadFavorites();
                    mpv5.YabsViewProxy.instance().addMessage(Messages.ADDED_TO_FAVOURITES + dato.__getCname(), Color.GREEN);
                    parents.setDataOwner(dato, true);
                } else {
                    parents.showRequiredFields();
                }
            }
        } else {
            Popup.notice(Messages.DEFAULT_USER);
        }
    }

    /**
     *
     * @param enable
     */
    public void setExportButtonsEnabled(boolean enable) {
        getMailButton().setEnabled(enable);
    }

    /**
     * @return the mailButton
     */
    public javax.swing.JButton getMailButton() {
        return mailButton;
    }

    /**
     * @return the odtButton
     */
    public javax.swing.JButton getOdtButton() {
        return odtButton;
    }

    /**
     * @return the pdfButton
     */
    public javax.swing.JButton getPdfButton() {
        return pdfButton;
    }

    /**
     * @return the printButton
     */
    public javax.swing.JButton getPrintButton() {
        return printButton;
    }

    /**
     * @return the pdfButton
     */
    public javax.swing.JButton getSaveButton() {
        return but3;
    }

    /**
     * @return the printButton
     */
    public javax.swing.JButton getCreateButton() {
        return but4;
    }
}
