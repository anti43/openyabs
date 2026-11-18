package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;

import mpv5.db.objects.Group;
import mpv5.db.objects.MailMessage;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;
import mpv5.ui.panels.DataPanel;
import mpv5.db.objects.User;

import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 * 
 */
public class ControlPanel_MailTemplates extends javax.swing.JPanel implements ControlApplet, DataPanel {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "mailtemplates";
    private MailMessage dataOwner;
    private final JTabbedPane editorTabs = new JTabbedPane();
    private final JEditorPane previewPane = new JEditorPane();
    private final JCheckBox resolvePlaceholders = new JCheckBox("Sample placeholders", true);

    public ControlPanel_MailTemplates() {
        initComponents();
        modernLayout();
        try {
            if (mpv5.db.common.DatabaseConnection.getConnector() != null && mpv5.db.objects.User.getCurrentUser() != null) {
                refresh();
                groupname.setSearchEnabled(true);
                groupname.setContext(Context.getGroup());
                groupname.triggerSearch();
            }
        } catch (Throwable ignore) {
        }
        setVisible(true);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel6 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        fullname = new mpv5.ui.beans.LabeledTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        descr = new javax.swing.JTextArea();
        groupname = new mpv5.ui.beans.LabeledCombobox();
        jScrollPane4 = new javax.swing.JScrollPane();
        templates = new javax.swing.JTable();

        //\$2java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_MailTemplates.border.title_1"))); // NOI18N
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(495, 183));
        setLayout(new java.awt.BorderLayout());

//$2java.awt.Color(255, 255, 255));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton4.setText(bundle.getString("ControlPanel_MailTemplates.jButton4.text_1")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton4);

        jButton3.setText(bundle.getString("ControlPanel_MailTemplates.jButton3.text_1")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton3);

        jButton2.setText(bundle.getString("ControlPanel_MailTemplates.jButton2.text_1")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton2);

        jButton7.setText(mpv5.globals.Messages.ACTION_SAVE_AS_NEW.toString()); // NOI18N
        jButton7.setName("jButton7"); // NOI18N
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton7);

        add(jPanel6, java.awt.BorderLayout.PAGE_END);

        jPanel1.setName("jPanel1"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_MailTemplates.jPanel2.border.title_1"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(425, 100));

//$2java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_MailTemplates.jPanel4.border.title_1"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        fullname.set_Label(bundle.getString("ControlPanel_MailTemplates.fullname._Label_1")); // NOI18N
        fullname.setFont(fullname.getFont());
        fullname.setName("fullname"); // NOI18N

        jLabel1.setFont(jLabel1.getFont());
        jLabel1.setText(bundle.getString("ControlPanel_MailTemplates.jLabel1.text_1")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        descr.setColumns(20);
        descr.setRows(5);
        descr.setName("descr"); // NOI18N
        jScrollPane2.setViewportView(descr);

        groupname.set_Label(bundle.getString("ControlPanel_MailTemplates.groupname._Label")); // NOI18N
        groupname.setName("groupname"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(258, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(fullname, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(groupname, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(fullname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(groupname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_MailTemplates.jScrollPane4.border.title_1"))); // NOI18N
        jScrollPane4.setName("jScrollPane4"); // NOI18N

        templates.setAutoCreateRowSorter(true);
        templates.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        templates.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        templates.setDragEnabled(true);
        templates.setFillsViewportHeight(true);
        templates.setName("templates"); // NOI18N
        templates.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                templatesMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(templates);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        reset();
}//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        //nothing saved yet (nothing chosen, or a new one): Save creates it
        if (dataOwner == null || !dataOwner.isExisting()) {
            jButton7ActionPerformed(evt);
            return;
        }

        if (dataOwner != null) {
            DatabaseObject dato = dataOwner;
            dato.getPanelData(this);
            if (dato.save()) {
                actionAfterSave();
                refresh();
            } else {
                showRequiredFields();
            }
        }
}//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if (Popup.Y_N_dialog(Messages.REALLY_DELETE)) {
            if (dataOwner != null) {
                DatabaseObject dato = dataOwner;
                dato.getPanelData(this);
                dato.delete();
            }
            try {
                Thread.sleep(333);
            } catch (InterruptedException ex) {
            }
            refresh();
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed

        if (dataOwner == null) {
            dataOwner = new MailMessage();
        }

        dataOwner.getPanelData(this);
        dataOwner.setIDS(-1);
        dataOwner.save();

        refresh();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void templatesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_templatesMouseClicked
        try {
            setDataOwner((DatabaseObject) templates.getValueAt(templates.getSelectedRow(), 0), true);
        } catch (Exception e) {
            Log.Debug(e);
        }
    }//GEN-LAST:event_templatesMouseClicked

    public void setValues(PropertyStore values) {
    }

    public String getUname() {
        return UNAME;
    }

    public void reset() {
        if (dataOwner != null) {
            DatabaseObject dato = dataOwner;

            dato.getPanelData(this);
            dato.reset();
            setDataOwner(dato, true);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea descr;
    private mpv5.ui.beans.LabeledTextField fullname;
    private mpv5.ui.beans.LabeledCombobox groupname;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable templates;
    // End of variables declaration//GEN-END:variables
    public String description_ = "";
    public String cname_;
    public String format_;
    public int ids_;
    public int intaddedby_;
    public Group group_;
    public int usersids_;
    public java.util.Date dateadded_ = new java.util.Date();

    public boolean collectData() {
         if (groupname.getSelectedItem() != null) {
            int groupsids_ = Integer.valueOf(((MPComboBoxModelItem) groupname.getSelectedItem()).getId());
            try {
                group_ = (Group) DatabaseObject.getObject(Context.getGroup(), groupsids_);
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex);
                group_ = Group.getDefault();
            }
        }

        description_ = descr.getText();
        cname_ = fullname.getText();
        usersids_ = mpv5.db.objects.User.getCurrentUser().__getIDS();

        return true;
    }

    public void exposeData() {

        try {
            if(group_!=null)
                groupname.setSelectedIndex(MPComboBoxModelItem.getItemID(String.valueOf(group_.__getIDS()), groupname.getModel()));
            
            fullname.setText(cname_);
            descr.setText(description_);
            renderPreview();
        } catch (Exception e) {
            Log.Debug(this, e);
        }
    }

    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    public void setDataOwner(DatabaseObject object, boolean p) {
        dataOwner = (MailMessage) object;
        if (p) {
            dataOwner.setPanelData(this);
            this.exposeData();
        }
    }

    public void refresh() {
        ArrayList<DatabaseObject> temps;
        try {
            if (mpv5.db.common.DatabaseConnection.getConnector() == null || mpv5.db.objects.User.getCurrentUser() == null) {
                return;
            }
            QueryCriteria c = new QueryCriteria("usersids", mpv5.db.objects.User.getCurrentUser().__getIDS());
            temps = DatabaseObject.getObjects(Context.getMessage());
            Object[][] data = new Object[temps.size()][3];

            for (int i = 0; i < temps.size(); i++) {
                MailMessage t = (MailMessage) temps.get(i);
                data[i][0] = t;
                data[i][1] = t.__getDescription();
                data[i][2] = Group.getObject(Context.getGroup(), t.__getGroupsids());
            }

            templates.setModel(new MPTableModel(data, Headers.MAILTEMPLATES.getValue()));
        } catch (Throwable ex) {
            Log.Debug(this, ex.getMessage());
        }
    }

    public void paste(DatabaseObject... dbos) {
        for (DatabaseObject dbo : dbos) {
            if (dbo.getContext().equals(Context.getTemplate())) {
                setDataOwner(dbo, true);
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE, Color.RED);
            }
        }
    }

    public void showRequiredFields() {
        TextFieldUtils.blinkerRed(fullname);
    }

    public void showSearchBar(boolean show) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Component getAndRemoveActionPanel() {
        return actions;
    }

    private javax.swing.JPanel actions = new javax.swing.JPanel();

    /**
     * The parts of the form as cards, the buttons in the action bar
     */
    private void modernLayout() {
        java.util.ResourceBundle b = mpv5.i18n.LanguageManager.getBundle();
        removeAll();
        setBorder(null);
        setLayout(new java.awt.BorderLayout());
        setOpaque(false);
        mpv5.ui.modern.ModernTheme.style(templates);
        jScrollPane4.setBorder(javax.swing.BorderFactory.createLineBorder(mpv5.ui.modern.ModernTheme.border()));
        groupname.getComboBox().setPreferredSize(null);

        // Editor tab: text area with HTML formatting toolbar
        descr.setFont(new Font(Font.MONOSPACED, Font.PLAIN, mpv5.ui.modern.ModernTheme.font(0, Font.PLAIN).getSize()));
        descr.setLineWrap(true);
        descr.setWrapStyleWord(true);
        jScrollPane2.setBorder(javax.swing.BorderFactory.createLineBorder(mpv5.ui.modern.ModernTheme.border()));

        JPanel formatToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, mpv5.ui.modern.ModernTheme.px(4), mpv5.ui.modern.ModernTheme.px(2)));
        formatToolbar.setOpaque(false);

        JButton boldBtn = formatButton("B", "<b>", "</b>", "Bold");
        boldBtn.setFont(new Font(Font.DIALOG, Font.BOLD, mpv5.ui.modern.ModernTheme.font(0, Font.BOLD).getSize()));
        JButton italicBtn = formatButton("I", "<i>", "</i>", "Italic");
        italicBtn.setFont(new Font(Font.DIALOG, Font.ITALIC, mpv5.ui.modern.ModernTheme.font(0, Font.ITALIC).getSize()));
        JButton underlineBtn = formatButton("U", "<u>", "</u>", "Underline");
        JButton pBtn = formatButton("P", "<p>", "</p>", "Paragraph");
        JButton brBtn = formatButton("BR", "<br>\n", "", "Line break");
        JButton linkBtn = createLinkButton();
        JButton tagBtn = createPlaceholderButton();

        formatToolbar.add(boldBtn);
        formatToolbar.add(italicBtn);
        formatToolbar.add(underlineBtn);
        formatToolbar.add(pBtn);
        formatToolbar.add(brBtn);
        formatToolbar.add(linkBtn);
        formatToolbar.add(tagBtn);

        JPanel sourcePanel = new JPanel(new BorderLayout(0, mpv5.ui.modern.ModernTheme.px(2)));
        sourcePanel.setOpaque(false);
        sourcePanel.add(formatToolbar, BorderLayout.NORTH);
        sourcePanel.add(jScrollPane2, BorderLayout.CENTER);

        // Preview tab: HTML preview pane with controls
        previewPane.setEditable(false);
        previewPane.setContentType("text/html");
        previewPane.putClientProperty(javax.swing.JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        previewPane.setFont(mpv5.ui.modern.ModernTheme.font(1, Font.PLAIN));
        previewPane.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED && e.getURL() != null) {
                try {
                    java.awt.Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    Log.Debug(ControlPanel_MailTemplates.class, ex);
                }
            }
        });

        JPanel previewToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, mpv5.ui.modern.ModernTheme.px(6), mpv5.ui.modern.ModernTheme.px(2)));
        previewToolbar.setOpaque(false);
        resolvePlaceholders.setOpaque(false);
        resolvePlaceholders.addActionListener(e -> renderPreview());
        JButton refreshPreview = new JButton("↻");
        refreshPreview.setToolTipText("Refresh preview");
        refreshPreview.setFocusable(false);
        refreshPreview.setMargin(new java.awt.Insets(mpv5.ui.modern.ModernTheme.px(2), mpv5.ui.modern.ModernTheme.px(6), mpv5.ui.modern.ModernTheme.px(2), mpv5.ui.modern.ModernTheme.px(6)));
        refreshPreview.addActionListener(e -> renderPreview());
        previewToolbar.add(resolvePlaceholders);
        previewToolbar.add(refreshPreview);

        JScrollPane previewScroll = new JScrollPane(previewPane);
        previewScroll.setBorder(javax.swing.BorderFactory.createLineBorder(mpv5.ui.modern.ModernTheme.border()));

        JPanel previewPanel = new JPanel(new BorderLayout(0, mpv5.ui.modern.ModernTheme.px(2)));
        previewPanel.setOpaque(false);
        previewPanel.add(previewToolbar, BorderLayout.NORTH);
        previewPanel.add(previewScroll, BorderLayout.CENTER);

        editorTabs.removeAll();
        editorTabs.addTab(Messages.MAIL_PREVIEW_SOURCE.toString(), sourcePanel);
        editorTabs.addTab(Messages.MAIL_PREVIEW_RENDERED.toString(), previewPanel);
        editorTabs.setPreferredSize(new Dimension(mpv5.ui.modern.ModernTheme.px(420), mpv5.ui.modern.ModernTheme.px(300)));
        editorTabs.addChangeListener(e -> {
            if (editorTabs.getSelectedIndex() == 1) {
                renderPreview();
            }
        });

        mpv5.ui.modern.Card details = new mpv5.ui.modern.Card(b.getString("ControlPanel_MailTemplates.jPanel4.border.title_1"))
                .row(mpv5.ui.modern.Applets.field(fullname, 2), mpv5.ui.modern.Card.field(groupname.get_Label(), groupname.getComboBox(), 1))
                .row(mpv5.ui.modern.Card.field(jLabel1.getText(), editorTabs))
                .addFull(mpv5.ui.modern.Applets.hint(Messages.MAILTEMPLATES_HINT.toString()));
        add(mpv5.ui.modern.Applets.treePage(b.getString("ControlPanel_MailTemplates.jScrollPane4.border.title_1"), jScrollPane4, null, details), java.awt.BorderLayout.CENTER);
        mpv5.ui.modern.Applets.danger(jButton4);
        mpv5.ui.modern.PrimaryButton save = new mpv5.ui.modern.PrimaryButton(jButton2.getText(), "/images/16/filesave.png");
        save.addActionListener(e -> jButton2.doClick());
        actions = mpv5.ui.modern.Applets.actionBar(new javax.swing.JComponent[]{jButton4}, new javax.swing.JComponent[]{jButton3, jButton7, save});
    }

    private JButton formatButton(String label, String openTag, String closeTag, String tooltip) {
        JButton b = new JButton(label);
        b.setToolTipText(tooltip);
        b.setFocusable(false);
        b.setMargin(new java.awt.Insets(mpv5.ui.modern.ModernTheme.px(2), mpv5.ui.modern.ModernTheme.px(6), mpv5.ui.modern.ModernTheme.px(2), mpv5.ui.modern.ModernTheme.px(6)));
        b.addActionListener(e -> wrapSelection(openTag, closeTag));
        return b;
    }

    private void wrapSelection(String openTag, String closeTag) {
        int start = descr.getSelectionStart();
        int end = descr.getSelectionEnd();
        String selected = descr.getSelectedText();
        if (selected == null || selected.isEmpty()) {
            descr.insert(openTag + closeTag, start);
            descr.setCaretPosition(start + openTag.length());
        } else {
            descr.replaceRange(openTag + selected + closeTag, start, end);
            descr.setSelectionStart(start + openTag.length());
            descr.setSelectionEnd(start + openTag.length() + selected.length());
        }
        descr.requestFocusInWindow();
    }

    private JButton createLinkButton() {
        JButton b = new JButton("Link");
        b.setToolTipText("Insert link <a href=\"...\">");
        b.setFocusable(false);
        b.setMargin(new java.awt.Insets(mpv5.ui.modern.ModernTheme.px(2), mpv5.ui.modern.ModernTheme.px(6), mpv5.ui.modern.ModernTheme.px(2), mpv5.ui.modern.ModernTheme.px(6)));
        b.addActionListener(e -> {
            String selected = descr.getSelectedText();
            String url = "https://";
            wrapSelection("<a href=\"" + url + "\">", "</a>");
        });
        return b;
    }

    private JButton createPlaceholderButton() {
        JButton b = new JButton("{...}");
        b.setToolTipText("Insert placeholder tag");
        b.setFocusable(false);
        b.setMargin(new java.awt.Insets(mpv5.ui.modern.ModernTheme.px(2), mpv5.ui.modern.ModernTheme.px(6), mpv5.ui.modern.ModernTheme.px(2), mpv5.ui.modern.ModernTheme.px(6)));
        b.addActionListener(e -> {
            javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
            String[] tags = {
                "{item.cnumber}",
                "{item.cname}",
                "{contact.cname}",
                "{contact.mailaddress}",
                "{user.cname}",
                "{item.totalnet}",
                "{item.totaltax}",
                "{item.totalbrut}",
                "{date}"
            };
            for (String tag : tags) {
                javax.swing.JMenuItem item = new javax.swing.JMenuItem(tag);
                item.addActionListener(ev -> wrapSelection(tag, ""));
                menu.add(item);
            }
            menu.show(b, 0, b.getHeight());
        });
        return b;
    }

    public void renderPreview() {
        String text = descr.getText();
        if (text == null) {
            text = "";
        }
        if (resolvePlaceholders.isSelected()) {
            text = resolveSamplePlaceholders(text);
        }
        String html;
        if (mpv5.mail.SimpleMail.isHtml(text)) {
            html = text;
        } else {
            html = "<html><body style=\"font-family: sans-serif; font-size: 13px; line-height: 1.5; padding: 10px;\">"
                    + text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>")
                    + "</body></html>";
        }
        try {
            previewPane.setContentType("text/html");
            previewPane.setText(html);
        } catch (Exception e) {
            Log.Debug(this, e);
            previewPane.setContentType("text/plain");
            previewPane.setText(text);
        }
        previewPane.setCaretPosition(0);
    }

    public static String resolveSamplePlaceholders(String text) {
        if (text == null) {
            return "";
        }
        String s = text;
        String[][] samples = {
            {"{item.cnumber}", "RE-2026-0042"},
            {"[item.cnumber]", "RE-2026-0042"},
            {"<item.cnumber>", "RE-2026-0042"},
            {"{item.cname}", "Invoice / Rechnung"},
            {"[item.cname]", "Invoice / Rechnung"},
            {"<item.cname>", "Invoice / Rechnung"},
            {"{contact.cname}", "Musterfirma GmbH"},
            {"[contact.cname]", "Musterfirma GmbH"},
            {"<contact.cname>", "Musterfirma GmbH"},
            {"{contact.prename}", "Erika"},
            {"[contact.prename]", "Erika"},
            {"<contact.prename>", "Erika"},
            {"{contact.name}", "Mustermann"},
            {"[contact.name]", "Mustermann"},
            {"<contact.name>", "Mustermann"},
            {"{contact.mailaddress}", "erika@musterfirma.de"},
            {"[contact.mailaddress]", "erika@musterfirma.de"},
            {"<contact.mailaddress>", "erika@musterfirma.de"},
            {"{user.cname}", "Max Admin"},
            {"[user.cname]", "Max Admin"},
            {"<user.cname>", "Max Admin"},
            {"{item.totalnet}", "1.000,00 €"},
            {"[item.totalnet]", "1.000,00 €"},
            {"<item.totalnet>", "1.000,00 €"},
            {"{item.totaltax}", "190,00 €"},
            {"[item.totaltax]", "190,00 €"},
            {"<item.totaltax>", "190,00 €"},
            {"{item.totalbrut}", "1.190,00 €"},
            {"[item.totalbrut]", "1.190,00 €"},
            {"<item.totalbrut>", "1.190,00 €"},
            {"{date}", "17.11.2026"},
            {"[date]", "17.11.2026"},
            {"<date>", "17.11.2026"}
        };
        for (String[] pair : samples) {
            s = s.replace(pair[0], pair[1]);
        }
        return s;
    }

    public JTabbedPane getEditorTabs() {
        return editorTabs;
    }

    public JEditorPane getPreviewPane() {
        return previewPane;
    }

    public javax.swing.JTextArea getSourceArea() {
        return descr;
    }

    @Override
    public void actionAfterSave() {
    }

    @Override
    public void actionAfterCreate() {
    }

    public void actionBeforeCreate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void actionBeforeSave() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mail() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void print() {
        mpv5.utils.export.Export.print(this);
    }
}
