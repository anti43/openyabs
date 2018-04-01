package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import javax.mail.MessagingException;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.objects.User;
import mpv5.globals.GlobalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.mail.SimpleMail;
import mpv5.ui.beans.LightMPComboBox;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.panels.MPControlPanel;

/**
 *
 *
 */
public class ControlPanel_Userproperties extends javax.swing.JPanel implements ControlApplet {

    private static final long serialVersionUID = -8347532498124147821L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "userproperties";
    private PropertyStore oldvalues;
    private static ControlPanel_Userproperties ident;
    private java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();

    public ControlPanel_Userproperties() {
        initComponents();

        defcount.set_ValueClass(Double.class);
        deftax.setSearchEnabled(true);
        deftax.setContext(Context.getTaxes());
        deftax.triggerSearch();

        shiptax.setSearchEnabled(true);
        shiptax.setContext(Context.getTaxes());
        shiptax.triggerSearch();

//        savedir.setFilter(DialogForFile.DIRECTORIES);
        loadSettings();
        try {
            groupl.setText(User.getCurrentUser().getGroup().__getCname());
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        productstobillsproperties = new javax.swing.JPanel();
        cname = new javax.swing.JCheckBox();
        description = new javax.swing.JCheckBox();
        cnumber = new javax.swing.JCheckBox();
        reference = new javax.swing.JCheckBox();
        ean = new javax.swing.JCheckBox();
        jButton3 = new javax.swing.JButton();
        defs = new javax.swing.JPanel();
        defcount = new mpv5.ui.beans.LabeledTextField();
        defunit = new mpv5.ui.beans.LabeledTextField();
        deftax = new mpv5.ui.beans.LabeledCombobox();
        shiptax = new mpv5.ui.beans.LabeledCombobox();
        format = new mpv5.ui.beans.LabeledTextField();
        jPanel3 = new javax.swing.JPanel();
        columnquantity = new javax.swing.JCheckBox();
        columnmeasure = new javax.swing.JCheckBox();
        hidetaxcolumn = new javax.swing.JCheckBox();
        hidediscountcolumn = new javax.swing.JCheckBox();
        hideproductscolumn = new javax.swing.JCheckBox();
        showoptionalcolumn = new javax.swing.JCheckBox();
        jPanel9 = new javax.swing.JPanel();
        smtphost = new mpv5.ui.beans.LabeledTextField();
        smtpuser = new mpv5.ui.beans.LabeledTextField();
        jLabel3 = new javax.swing.JLabel();
        smtppw = new javax.swing.JPasswordField();
        smtptls = new javax.swing.JCheckBox();
        smtps = new javax.swing.JCheckBox();
        jButton4 = new javax.swing.JButton();
        smtpport = new mpv5.ui.beans.LabeledTextField();
        TestButton = new javax.swing.JButton();
        smtpssl = new javax.swing.JCheckBox();
        testadress = new mpv5.ui.beans.LabeledTextField();
        jPanel4 = new javax.swing.JPanel();
        saveformat = new mpv5.ui.beans.LabeledTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jPanel10 = new javax.swing.JPanel();
        dtabankid = new mpv5.ui.beans.LabeledTextField();
        dtabankaccount = new mpv5.ui.beans.LabeledTextField();
        dtabankname = new mpv5.ui.beans.LabeledTextField();
        dtabankcountry = new mpv5.ui.beans.LabeledTextField();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        dtausage0 = new javax.swing.JTextPane();
        jPanel5 = new javax.swing.JPanel();
        tabs = new javax.swing.JCheckBox();
        supresscurrencysymbols = new javax.swing.JCheckBox();
        ordersoverproducts = new javax.swing.JCheckBox();
        companiesovernames = new javax.swing.JCheckBox();
        pasten = new javax.swing.JCheckBox();
        nowarnings = new javax.swing.JCheckBox();
        unpaidbills = new javax.swing.JCheckBox();
        views = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        groupl = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        nocount = new javax.swing.JCheckBox();
        resolv = new javax.swing.JCheckBox();
        remoteurl = new javax.swing.JCheckBox();
        noblank = new javax.swing.JCheckBox();
        showItemAsDesc = new javax.swing.JCheckBox();
        jPanel11 = new javax.swing.JPanel();
        transdateX = new javax.swing.JCheckBox();
        enddateX = new javax.swing.JCheckBox();
        calcEnddateX = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Userproperties.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Userproperties.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        productstobillsproperties.setName("productstobillsproperties"); // NOI18N

        cname.setText(bundle.getString("ControlPanel_Userproperties.cname.text")); // NOI18N
        cname.setName("cname"); // NOI18N

        description.setText(bundle.getString("ControlPanel_Userproperties.description.text")); // NOI18N
        description.setName("description"); // NOI18N

        cnumber.setText(bundle.getString("ControlPanel_Userproperties.cnumber.text")); // NOI18N
        cnumber.setName("cnumber"); // NOI18N

        reference.setText(bundle.getString("ControlPanel_Userproperties.reference.text")); // NOI18N
        reference.setName("reference"); // NOI18N

        ean.setText(bundle.getString("ControlPanel_Userproperties.ean.text")); // NOI18N
        ean.setName("ean"); // NOI18N

        jButton3.setText(bundle.getString("ControlPanel_Userproperties.jButton3.text")); // NOI18N
        jButton3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton3.setName("jButton3"); // NOI18N
        jButton3.setPreferredSize(new java.awt.Dimension(100, 24));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout productstobillspropertiesLayout = new javax.swing.GroupLayout(productstobillsproperties);
        productstobillsproperties.setLayout(productstobillspropertiesLayout);
        productstobillspropertiesLayout.setHorizontalGroup(
            productstobillspropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(productstobillspropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(productstobillspropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cnumber, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(description, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cname, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(productstobillspropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ean, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reference, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        productstobillspropertiesLayout.setVerticalGroup(
            productstobillspropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(productstobillspropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(productstobillspropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cname)
                    .addComponent(ean))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(productstobillspropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(description)
                    .addComponent(reference))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(productstobillspropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cnumber)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        defs.setName("defs"); // NOI18N

        defcount.set_Label(bundle.getString("ControlPanel_Userproperties.defcount._Label")); // NOI18N
        defcount.set_Text(bundle.getString("ControlPanel_Userproperties.defcount._Text")); // NOI18N
        defcount.setName("defcount"); // NOI18N

        defunit.set_Label(bundle.getString("ControlPanel_Userproperties.defunit._Label")); // NOI18N
        defunit.set_Text(bundle.getString("ControlPanel_Userproperties.defunit._Text")); // NOI18N
        defunit.setName("defunit"); // NOI18N

        deftax.set_Label(bundle.getString("ControlPanel_Userproperties.deftax._Label")); // NOI18N
        deftax.setName("deftax"); // NOI18N

        shiptax.set_Label(bundle.getString("ControlPanel_Userproperties.shiptax._Label")); // NOI18N
        shiptax.setName("shiptax"); // NOI18N

        javax.swing.GroupLayout defsLayout = new javax.swing.GroupLayout(defs);
        defs.setLayout(defsLayout);
        defsLayout.setHorizontalGroup(
            defsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(defsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(defunit, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(defcount, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(defsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(shiptax, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deftax, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        defsLayout.setVerticalGroup(
            defsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(defsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(defsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(defsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(deftax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(defcount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(defunit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shiptax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        format.set_Label(bundle.getString("ControlPanel_Userproperties.format._Label")); // NOI18N
        format.setName("format"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(productstobillsproperties, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(format, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(defs, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(productstobillsproperties, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(format, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(defs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Userproperties.jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.PAGE_AXIS));

        columnquantity.setText(bundle.getString("ControlPanel_Userproperties.columnquantity.text")); // NOI18N
        columnquantity.setName("columnquantity"); // NOI18N
        jPanel3.add(columnquantity);

        columnmeasure.setText(bundle.getString("ControlPanel_Userproperties.columnmeasure.text")); // NOI18N
        columnmeasure.setName("columnmeasure"); // NOI18N
        jPanel3.add(columnmeasure);

        hidetaxcolumn.setText(bundle.getString("ControlPanel_Userproperties.hidetaxcolumn.text")); // NOI18N
        hidetaxcolumn.setName("hidetaxcolumn"); // NOI18N
        jPanel3.add(hidetaxcolumn);

        hidediscountcolumn.setText(bundle.getString("ControlPanel_Userproperties.hidediscountcolumn.text")); // NOI18N
        hidediscountcolumn.setName("hidediscountcolumn"); // NOI18N
        jPanel3.add(hidediscountcolumn);

        hideproductscolumn.setText(bundle.getString("ControlPanel_Userproperties.hideproductscolumn.text")); // NOI18N
        hideproductscolumn.setName("hideproductscolumn"); // NOI18N
        jPanel3.add(hideproductscolumn);

        showoptionalcolumn.setText(bundle.getString("ControlPanel_Userproperties.showoptionalcolumn.text")); // NOI18N
        showoptionalcolumn.setName("showoptionalcolumn"); // NOI18N
        jPanel3.add(showoptionalcolumn);

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Userproperties.jPanel9.border.title"))); // NOI18N
        jPanel9.setName("jPanel9"); // NOI18N

        smtphost.set_Label(bundle.getString("ControlPanel_Userproperties.smtphost._Label")); // NOI18N
        smtphost.setName("smtphost"); // NOI18N

        smtpuser.set_Label(bundle.getString("ControlPanel_Userproperties.smtpuser._Label")); // NOI18N
        smtpuser.setName("smtpuser"); // NOI18N

        jLabel3.setText(bundle.getString("ControlPanel_Userproperties.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        smtppw.setText(bundle.getString("ControlPanel_Userproperties.smtppw.text")); // NOI18N
        smtppw.setName("smtppw"); // NOI18N

        smtptls.setText(bundle.getString("ControlPanel_Userproperties.smtptls.text")); // NOI18N
        smtptls.setName("smtptls"); // NOI18N

        smtps.setText(bundle.getString("ControlPanel_Userproperties.smtps.text")); // NOI18N
        smtps.setName("smtps"); // NOI18N

        jButton4.setText(bundle.getString("ControlPanel_Userproperties.jButton4.text")); // NOI18N
        jButton4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton4.setName("jButton4"); // NOI18N
        jButton4.setPreferredSize(new java.awt.Dimension(140, 24));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        smtpport.set_Label(bundle.getString("ControlPanel_Userproperties.smtpport._Label")); // NOI18N
        smtpport.setName("smtpport"); // NOI18N

        TestButton.setText(bundle.getString("ControlPanel_Userproperties.TestButton.text")); // NOI18N
        TestButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        TestButton.setName("TestButton"); // NOI18N
        TestButton.setPreferredSize(new java.awt.Dimension(120, 24));
        TestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TestButtonActionPerformed(evt);
            }
        });

        smtpssl.setText(bundle.getString("ControlPanel_Userproperties.smtpssl.text")); // NOI18N
        smtpssl.setName("smtpssl"); // NOI18N

        testadress.set_Label(bundle.getString("ControlPanel_Userproperties.testadress._Label")); // NOI18N
        testadress.setName("testadress"); // NOI18N

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addComponent(smtptls, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(smtps, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(smtpssl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(smtphost, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                            .addComponent(smtpport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4)
                                .addComponent(smtppw))
                            .addComponent(smtpuser, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(testadress, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TestButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(smtpuser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(smtphost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(smtpport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(smtppw, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(smtptls)
                            .addComponent(smtps)
                            .addComponent(smtpssl)))
                    .addComponent(testadress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TestButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Userproperties.jPanel4.border.title"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        saveformat.set_Label(bundle.getString("ControlPanel_Userproperties.saveformat._Label")); // NOI18N
        saveformat.setName("saveformat"); // NOI18N

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 3, 1));
        jTextPane1.setText(bundle.getString("ControlPanel_Userproperties.jTextPane1.text")); // NOI18N
        jTextPane1.setName("jTextPane1"); // NOI18N
        jTextPane1.setOpaque(false);
        jScrollPane1.setViewportView(jTextPane1);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 673, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(saveformat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(saveformat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Userproperties.jPanel10.border.title"))); // NOI18N
        jPanel10.setName("jPanel10"); // NOI18N

        dtabankid.set_Label(bundle.getString("ControlPanel_Userproperties.dtabankid._Label")); // NOI18N
        dtabankid.setName("dtabankid"); // NOI18N

        dtabankaccount.set_Label(bundle.getString("ControlPanel_Userproperties.dtabankaccount._Label")); // NOI18N
        dtabankaccount.setName("dtabankaccount"); // NOI18N

        dtabankname.set_Label(bundle.getString("ControlPanel_Userproperties.dtabankname._Label")); // NOI18N
        dtabankname.setName("dtabankname"); // NOI18N

        dtabankcountry.set_Label(bundle.getString("ControlPanel_Userproperties.dtabankcountry._Label")); // NOI18N
        dtabankcountry.setName("dtabankcountry"); // NOI18N

        jLabel4.setText(bundle.getString("ControlPanel_Userproperties.jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        dtausage0.setText("Invoice from {item.dateadded} ref {item.cnumber}"); // NOI18N
        dtausage0.setName("dtausage0"); // NOI18N
        jScrollPane2.setViewportView(dtausage0);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(dtabankid, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                            .addComponent(dtabankaccount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(dtabankname, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                            .addComponent(dtabankcountry, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dtabankid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dtabankname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dtabankaccount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dtabankcountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Userproperties.jPanel5.border.title"))); // NOI18N
        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.Y_AXIS));

        tabs.setText(bundle.getString("ControlPanel_Userproperties.tabs.text")); // NOI18N
        tabs.setName("tabs"); // NOI18N
        jPanel5.add(tabs);

        supresscurrencysymbols.setText(bundle.getString("ControlPanel_Userproperties.supresscurrencysymbols.text")); // NOI18N
        supresscurrencysymbols.setName("supresscurrencysymbols"); // NOI18N
        jPanel5.add(supresscurrencysymbols);

        ordersoverproducts.setText(bundle.getString("ControlPanel_Userproperties.ordersoverproducts.text")); // NOI18N
        ordersoverproducts.setName("ordersoverproducts"); // NOI18N
        jPanel5.add(ordersoverproducts);

        companiesovernames.setText(bundle.getString("ControlPanel_Userproperties.companiesovernames.text")); // NOI18N
        companiesovernames.setName("companiesovernames"); // NOI18N
        jPanel5.add(companiesovernames);

        pasten.setText(bundle.getString("ControlPanel_Userproperties.pasten.text")); // NOI18N
        pasten.setName("pasten"); // NOI18N
        jPanel5.add(pasten);

        nowarnings.setText(bundle.getString("ControlPanel_Userproperties.nowarnings.text")); // NOI18N
        nowarnings.setName("nowarnings"); // NOI18N
        jPanel5.add(nowarnings);

        unpaidbills.setText(bundle.getString("ControlPanel_Userproperties.unpaidbills.text")); // NOI18N
        unpaidbills.setName("unpaidbills"); // NOI18N
        jPanel5.add(unpaidbills);

        views.setText(bundle.getString("ControlPanel_Userproperties.views.text")); // NOI18N
        views.setName("views"); // NOI18N
        jPanel5.add(views);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Userproperties.jPanel7.border.title"))); // NOI18N
        jPanel7.setName("jPanel7"); // NOI18N
        jPanel7.setLayout(new java.awt.BorderLayout());

        groupl.setFont(groupl.getFont().deriveFont(groupl.getFont().getStyle() | java.awt.Font.BOLD, groupl.getFont().getSize()+1));
        groupl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        groupl.setText(bundle.getString("ControlPanel_Userproperties.groupl.text")); // NOI18N
        groupl.setName("groupl"); // NOI18N
        jPanel7.add(groupl, java.awt.BorderLayout.CENTER);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Userproperties.jPanel8.border.title"))); // NOI18N
        jPanel8.setName("jPanel8"); // NOI18N

        nocount.setText(bundle.getString("ControlPanel_Userproperties.nocount.text")); // NOI18N
        nocount.setName("nocount"); // NOI18N

        resolv.setText(bundle.getString("ControlPanel_Userproperties.resolv.text")); // NOI18N
        resolv.setName("resolv"); // NOI18N

        remoteurl.setText(bundle.getString("ControlPanel_Userproperties.remoteurl.text")); // NOI18N
        remoteurl.setName("remoteurl"); // NOI18N

        noblank.setText(bundle.getString("ControlPanel_Userproperties.noblank.text")); // NOI18N
        noblank.setName("noblank"); // NOI18N

        showItemAsDesc.setText(bundle.getString("ControlPanel_Userproperties.showItemAsDesc.text")); // NOI18N
        showItemAsDesc.setName("showItemAsDesc"); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(noblank, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(remoteurl, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                    .addComponent(resolv, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nocount, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(showItemAsDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(noblank)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(remoteurl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resolv)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nocount)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, Short.MAX_VALUE)
                .addComponent(showItemAsDesc)
                .addContainerGap())
        );

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Userproperties.jPanel11.border.title"))); // NOI18N
        jPanel11.setName("jPanel11"); // NOI18N
        jPanel11.setLayout(new javax.swing.BoxLayout(jPanel11, javax.swing.BoxLayout.PAGE_AXIS));

        transdateX.setText(bundle.getString("ControlPanel_Userproperties.transdateX.text")); // NOI18N
        transdateX.setName("transdateX"); // NOI18N
        jPanel11.add(transdateX);

        enddateX.setText(bundle.getString("ControlPanel_Userproperties.enddateX.text")); // NOI18N
        enddateX.setName("enddateX"); // NOI18N
        jPanel11.add(enddateX);

        calcEnddateX.setText(bundle.getString("ControlPanel_Userproperties.calcEnddateX.text")); // NOI18N
        calcEnddateX.setName("calcEnddateX"); // NOI18N
        calcEnddateX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calcEnddateXActionPerformed(evt);
            }
        });
        jPanel11.add(calcEnddateX);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 699, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 889, Short.MAX_VALUE)))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton2.setText(bundle.getString("ControlPanel_Userproperties.jButton2.text")); // NOI18N
        jButton2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setPreferredSize(new java.awt.Dimension(120, 24));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton2);

        jButton1.setText(bundle.getString("ControlPanel_Userproperties.jButton1.text")); // NOI18N
        jButton1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(120, 24));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton1);

        jButton5.setText(bundle.getString("ControlPanel_Userproperties.jButton5.text")); // NOI18N
        jButton5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton5.setName("jButton5"); // NOI18N
        jButton5.setPreferredSize(new java.awt.Dimension(120, 24));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton5);

        add(jPanel6, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        setSettings();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setSettings();
        mpv5.db.objects.User.getCurrentUser().saveProperties();
        GlobalSettings.save();
        Popup.notice(Messages.RESTART_REQUIRED);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        generate();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        MPControlPanel.instanceOf().openDetails(new ControlPanel_MailTemplates());
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        ControlPanel_AdvancedUserProperties c = new ControlPanel_AdvancedUserProperties();
        MPControlPanel.instanceOf().openDetails(c);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void calcEnddateXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calcEnddateXActionPerformed

    }//GEN-LAST:event_calcEnddateXActionPerformed

    private void TestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TestButtonActionPerformed
        if (this.testadress.getText().isEmpty()) {
            Popup.error(this, Messages.NO_MAIL_DEFINED);
            return;
        }

        if (mpv5.db.objects.User.getCurrentUser().__getMail() == null || 
                mpv5.db.objects.User.getCurrentUser().__getMail().trim().length()==0) {
            mpv5.db.objects.User.getCurrentUser().setMail(this.testadress.getText());
            mpv5.db.objects.User.getCurrentUser().save(true);
            mpv5.db.objects.User.getCurrentUser().reset();
        }
        
        try {
            new SimpleMail(
                    this.smtphost.getText(),
                    this.smtpport.getText(),
                    this.smtpuser.getText(),
                    this.smtppw.getText(),
                    this.smtptls.isSelected(),
                    this.smtpssl.isSelected(),
                    this.smtps.isSelected(),
                    User.getCurrentUser().__getMail(),
                    this.testadress.getText(),
                    "Mail-Test",
                    "Mail from YABS"
            );
        } catch (MessagingException ex) {
            Log.Debug(this, ex.getLocalizedMessage());
        }
    }//GEN-LAST:event_TestButtonActionPerformed

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
    private javax.swing.JButton TestButton;
    private javax.swing.JCheckBox calcEnddateX;
    private javax.swing.JCheckBox cname;
    private javax.swing.JCheckBox cnumber;
    private javax.swing.JCheckBox columnmeasure;
    private javax.swing.JCheckBox columnquantity;
    private javax.swing.JCheckBox companiesovernames;
    private mpv5.ui.beans.LabeledTextField defcount;
    private javax.swing.JPanel defs;
    private mpv5.ui.beans.LabeledCombobox deftax;
    private mpv5.ui.beans.LabeledTextField defunit;
    private javax.swing.JCheckBox description;
    private mpv5.ui.beans.LabeledTextField dtabankaccount;
    private mpv5.ui.beans.LabeledTextField dtabankcountry;
    private mpv5.ui.beans.LabeledTextField dtabankid;
    private mpv5.ui.beans.LabeledTextField dtabankname;
    private javax.swing.JTextPane dtausage0;
    private javax.swing.JCheckBox ean;
    private javax.swing.JCheckBox enddateX;
    private mpv5.ui.beans.LabeledTextField format;
    private javax.swing.JLabel groupl;
    private javax.swing.JCheckBox hidediscountcolumn;
    private javax.swing.JCheckBox hideproductscolumn;
    private javax.swing.JCheckBox hidetaxcolumn;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JCheckBox noblank;
    private javax.swing.JCheckBox nocount;
    private javax.swing.JCheckBox nowarnings;
    private javax.swing.JCheckBox ordersoverproducts;
    private javax.swing.JCheckBox pasten;
    private javax.swing.JPanel productstobillsproperties;
    private javax.swing.JCheckBox reference;
    private javax.swing.JCheckBox remoteurl;
    private javax.swing.JCheckBox resolv;
    private mpv5.ui.beans.LabeledTextField saveformat;
    private mpv5.ui.beans.LabeledCombobox shiptax;
    private javax.swing.JCheckBox showItemAsDesc;
    private javax.swing.JCheckBox showoptionalcolumn;
    private mpv5.ui.beans.LabeledTextField smtphost;
    private mpv5.ui.beans.LabeledTextField smtpport;
    private javax.swing.JPasswordField smtppw;
    private javax.swing.JCheckBox smtps;
    private javax.swing.JCheckBox smtpssl;
    private javax.swing.JCheckBox smtptls;
    private mpv5.ui.beans.LabeledTextField smtpuser;
    private javax.swing.JCheckBox supresscurrencysymbols;
    private javax.swing.JCheckBox tabs;
    private mpv5.ui.beans.LabeledTextField testadress;
    private javax.swing.JCheckBox transdateX;
    private javax.swing.JCheckBox unpaidbills;
    private javax.swing.JCheckBox views;
    // End of variables declaration//GEN-END:variables

    private void setSettings() {

        if (format.getText().length() == 0) {
            generate();
        }
        try {
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty(Context.getProduct() + LightMPComboBox.VALUE_SEARCHFIELDS, format.getText());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty(defcount.getName(), defcount.getText());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty(defunit.getName(), defunit.getText());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty(saveformat.getName(), saveformat.getText().replace("\\", "/"));
//            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty(savedir.getName(), savedir.get_Text(false));
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.uiproperty", "norecycletabs", tabs.isSelected());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.uiproperty", "avoidmultipleviews", views.isSelected());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.uiproperty", "hidecolumnquantity", columnquantity.isSelected());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.uiproperty", "hideproductscolumn", hideproductscolumn.isSelected());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.uiproperty", "hidecolumnmeasure", columnmeasure.isSelected());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.uiproperty", "hidetaxcolumn", hidetaxcolumn.isSelected());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.uiproperty", "hidediscountcolumn", hidediscountcolumn.isSelected());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.uiproperty", "showoptionalcolumn", showoptionalcolumn.isSelected());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.uiproperty", "supresscurrencysymbols", supresscurrencysymbols.isSelected());

            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.uiproperty", "ordersoverproducts", ordersoverproducts.isSelected());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.uiproperty", "companiesovernames", companiesovernames.isSelected());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.uiproperty", "pasten", pasten.isSelected());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.uiproperty", "nowarnings", nowarnings.isSelected());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.uiproperty", "propertiesdefined", true);

            //Settings for Item Date Defaults
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.itemproperty", "keepmodifiedtransdate", transdateX.isSelected());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.itemproperty", "keepmodifiedenddate", enddateX.isSelected());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.itemproperty", "calcenddate", calcEnddateX.isSelected());

            if (deftax.getSelectedItem() != null) {
                mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty(deftax.getName(), deftax.getSelectedItem().getId());
            }
            if (shiptax.getSelectedItem() != null) {
                mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty(shiptax.getName(), shiptax.getSelectedItem().getId());
            }
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("smtp.host", smtphost.getText());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("smtp.port", smtpport.getText());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("smtp.host.user", smtpuser.getText());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("smtp.host.password", new String(smtppw.getPassword()));
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("smtp.host.usetls", Boolean.toString(smtptls.isSelected()));
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("smtp.host.usessl", Boolean.toString(smtpssl.isSelected()));
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("smtp.host.usesmpts", Boolean.toString(smtps.isSelected()));

            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("dtabankaccount", dtabankaccount.getText());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("dtabankid", dtabankid.getText());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("dtabankname", dtabankname.getText());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("dtabankcountry", dtabankcountry.getText());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("dtausage0", dtausage0.getText());

            mpv5.db.objects.User.getCurrentUser().defineMailConfig();
            mpv5.db.objects.User.getCurrentUser().defineDTAConfig();
        } catch (Exception e) {
            Log.Debug(e);
            Notificator.raiseNotification(e);
        }
        GlobalSettings.setProperty("org.openyabs.exportproperty.hidecountfortext", String.valueOf(nocount.isSelected()));
        GlobalSettings.setProperty("org.openyabs.exportproperty.showitemasdesc", String.valueOf(showItemAsDesc.isSelected()));
        GlobalSettings.setProperty("org.openyabs.exportproperty.productsresolved", String.valueOf(resolv.isSelected()));
        GlobalSettings.setProperty("org.openyabs.exportproperty.blankunusedfields.disable", String.valueOf(noblank.isSelected()));
        GlobalSettings.setProperty("org.openyabs.exportproperty.loadremoteoodocbyurl", String.valueOf(remoteurl.isSelected()));

        loadSettings();
    }

    @Override
    public Component getAndRemoveActionPanel() {
        this.remove(jPanel6);
        validate();
        return jPanel6;
    }

    private void loadSettings() {
        tabs.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "norecycletabs"));
        nowarnings.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "nowarnings"));
        views.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "avoidmultipleviews"));
        unpaidbills.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hideunpaidbills"));
        columnquantity.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidecolumnquantity"));
        columnmeasure.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidecolumnmeasure"));
        hideproductscolumn.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hideproductscolumn"));
        showoptionalcolumn.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "showoptionalcolumn"));
        supresscurrencysymbols.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "supresscurrencysymbols"));
        ordersoverproducts.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "ordersoverproducts"));
        companiesovernames.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "companiesovernames"));
        hidetaxcolumn.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidetaxcolumn"));
        pasten.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "pasten"));
        dtabankaccount.setText(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("dtabankaccount"));
        dtabankid.setText(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("dtabankid"));
        dtabankname.setText(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("dtabankname"));
        dtabankcountry.setText(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("dtabankcountry"));
        dtausage0.setText(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("dtausage0"));
        hidediscountcolumn.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidediscountcolumn"));

        //Settings for Item Date Defaults
        transdateX.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.itemproperty", "keepmodifiedtransdate"));
        enddateX.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.itemproperty", "keepmodifiedenddate"));
        calcEnddateX.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.itemproperty", "calcenddate"));

        Component[] t = productstobillsproperties.getComponents();
        for (int i = 0; i < t.length; i++) {
            Component component = t[i];
            if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty(Context.getProduct() + LightMPComboBox.VALUE_SEARCHFIELDS)) {
                if (component instanceof JCheckBox) {
                    try {
                        ((JCheckBox) component).setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(Context.getProduct() + LightMPComboBox.VALUE_SEARCHFIELDS).contains(component.getName()));
                    } catch (Exception e) {
                    }
                }
            }
        }
        String s = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(Context.getProduct() + LightMPComboBox.VALUE_SEARCHFIELDS);
        if (s != null) {
            format.setText(s);
        } else {
            format.setText("_$cname$_ ");
        }

        String s1 = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("saveformat");
        if (s1 != null) {
            saveformat.setText(s1);
        } else {
            format.setText("");
        }

//        String s2 = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("savedir");
//        if (s2 != null) {
//            savedir.set_Text(s2);
//        } else {
//            savedir.set_Text("");
//        }
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty(defcount.getName())) {
            defcount.setText(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(defcount.getName()));
        } else {
            defcount.setText("1");
        }

        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty(defcount.getName())) {
            defcount.setText(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(defcount.getName()));
        } else {
            defcount.setText("1");
        }

        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("smtp.host")) {
            smtphost.setText(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("smtp.host"));
        }

        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("smtp.port")) {
            smtpport.setText(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("smtp.port"));
        }

        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("smtp.host.user")) {
            smtpuser.setText(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("smtp.host.user"));
        }

        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("smtp.host.password")) {
            smtppw.setText(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("smtp.host.password"));
        }

        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("smtp.host.usetls")) {
            smtptls.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("smtp.host.usetls", false));
        }

        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("smtp.host.usessl")) {
            smtpssl.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("smtp.host.usessl", false));
        }

        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("smtp.host.usesmpts")) {
            smtps.setSelected(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("smtp.host.usesmpts", false));
        }

        testadress.setText(User.getCurrentUser().__getMail());

        nocount.setSelected(GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.hidecountfortext"));
        showItemAsDesc.setSelected(GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.showitemasdesc"));
        resolv.setSelected(GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.productsresolved"));
        noblank.setSelected(GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.blankunusedfields.disable"));
        remoteurl.setSelected(GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.loadremoteoodocbyurl"));

        try {
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    try {
                        deftax.setSelectedItem(Integer.valueOf(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(deftax.getName())));
                    } catch (NumberFormatException numberFormatException) {
                    }
                    try {
                        shiptax.setSelectedItem(Integer.valueOf(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(shiptax.getName())));
                    } catch (NumberFormatException numberFormatException) {
                    }
                }
            };
            SwingUtilities.invokeLater(runnable);
        } catch (Exception e) {
        }

    }

    private void generate() {
        Component[] t = productstobillsproperties.getComponents();
        String h = "";
        for (int i = 0; i < t.length; i++) {
            Component component = t[i];
            if (component instanceof JCheckBox) {
                if (((JCheckBox) component).isSelected()) {
                    h += "_$" + component.getName() + "$_ ";
                }
            }
        }
        format.setText(h);
    }
}
