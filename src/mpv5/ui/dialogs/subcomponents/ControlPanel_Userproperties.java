package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.beans.LightMPComboBox;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;
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

    public ControlPanel_Userproperties() {
        initComponents();

        defcount.set_ValueClass(Double.class);
        deftax.setSearchEnabled(true);
        deftax.setContext(Context.getTaxes());
        deftax.triggerSearch();

        shiptax.setSearchEnabled(true);
        shiptax.setContext(Context.getTaxes());
        shiptax.triggerSearch();

        loadSettings();
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
        autorev = new javax.swing.JCheckBox();
        format = new mpv5.ui.beans.LabeledTextField();
        jPanel3 = new javax.swing.JPanel();
        tabs = new javax.swing.JCheckBox();
        views = new javax.swing.JCheckBox();
        unpaidbills = new javax.swing.JCheckBox();
        columnquantity = new javax.swing.JCheckBox();
        columnmeasure = new javax.swing.JCheckBox();
        hideproductscolumn = new javax.swing.JCheckBox();
        hidetaxcolumn = new javax.swing.JCheckBox();
        showoptionalcolumn = new javax.swing.JCheckBox();
        supresscurrencysymbols = new javax.swing.JCheckBox();
        ordersoverproducts = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        labeledTextField1 = new mpv5.ui.beans.LabeledTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        labeledTextField2 = new mpv5.ui.beans.LabeledTextField();
        jPanel9 = new javax.swing.JPanel();
        smtphost = new mpv5.ui.beans.LabeledTextField();
        smtpuser = new mpv5.ui.beans.LabeledTextField();
        jLabel3 = new javax.swing.JLabel();
        smtppw = new javax.swing.JPasswordField();
        smtptls = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
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
        jButton3.setName("jButton3"); // NOI18N
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
                    .addComponent(cnumber, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                    .addComponent(description, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                    .addComponent(cname, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE))
                .addGap(8, 8, 8)
                .addGroup(productstobillspropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ean, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                    .addComponent(reference, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                    .addComponent(jButton3))
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
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        autorev.setText(bundle.getString("ControlPanel_Userproperties.autorev.text")); // NOI18N
        autorev.setEnabled(false);
        autorev.setName("autorev"); // NOI18N

        javax.swing.GroupLayout defsLayout = new javax.swing.GroupLayout(defs);
        defs.setLayout(defsLayout);
        defsLayout.setHorizontalGroup(
            defsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(defsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(defsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(defsLayout.createSequentialGroup()
                        .addComponent(defunit, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(defcount, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(autorev))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(defsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(shiptax, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(deftax, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)))
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
                .addGroup(defsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(autorev)
                    .addComponent(shiptax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        format.set_Label(bundle.getString("ControlPanel_Userproperties.format._Label")); // NOI18N
        format.setName("format"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(format, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(defs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(productstobillsproperties, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Userproperties.jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.PAGE_AXIS));

        tabs.setText(bundle.getString("ControlPanel_Userproperties.tabs.text")); // NOI18N
        tabs.setName("tabs"); // NOI18N
        jPanel3.add(tabs);

        views.setText(bundle.getString("ControlPanel_Userproperties.views.text")); // NOI18N
        views.setName("views"); // NOI18N
        jPanel3.add(views);

        unpaidbills.setText(bundle.getString("ControlPanel_Userproperties.unpaidbills.text")); // NOI18N
        unpaidbills.setName("unpaidbills"); // NOI18N
        jPanel3.add(unpaidbills);

        columnquantity.setText(bundle.getString("ControlPanel_Userproperties.columnquantity.text")); // NOI18N
        columnquantity.setName("columnquantity"); // NOI18N
        jPanel3.add(columnquantity);

        columnmeasure.setText(bundle.getString("ControlPanel_Userproperties.columnmeasure.text")); // NOI18N
        columnmeasure.setName("columnmeasure"); // NOI18N
        jPanel3.add(columnmeasure);

        hideproductscolumn.setText(bundle.getString("ControlPanel_Userproperties.hideproductscolumn.text")); // NOI18N
        hideproductscolumn.setName("hideproductscolumn"); // NOI18N
        jPanel3.add(hideproductscolumn);

        hidetaxcolumn.setText(bundle.getString("ControlPanel_Userproperties.hidetaxcolumn.text")); // NOI18N
        hidetaxcolumn.setName("hidetaxcolumn"); // NOI18N
        jPanel3.add(hidetaxcolumn);

        showoptionalcolumn.setText(bundle.getString("ControlPanel_Userproperties.showoptionalcolumn.text")); // NOI18N
        showoptionalcolumn.setName("showoptionalcolumn"); // NOI18N
        jPanel3.add(showoptionalcolumn);

        supresscurrencysymbols.setText(bundle.getString("ControlPanel_Userproperties.supresscurrencysymbols.text")); // NOI18N
        supresscurrencysymbols.setName("supresscurrencysymbols"); // NOI18N
        jPanel3.add(supresscurrencysymbols);

        ordersoverproducts.setText(bundle.getString("ControlPanel_Userproperties.ordersoverproducts.text")); // NOI18N
        ordersoverproducts.setName("ordersoverproducts"); // NOI18N
        jPanel3.add(ordersoverproducts);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Userproperties.jPanel8.border.title"))); // NOI18N
        jPanel8.setName("jPanel8"); // NOI18N

        labeledTextField1.set_Label(bundle.getString("ControlPanel_Userproperties.labeledTextField1._Label")); // NOI18N
        labeledTextField1.set_Text(bundle.getString("ControlPanel_Userproperties.labeledTextField1._Text")); // NOI18N
        labeledTextField1.setName("labeledTextField1"); // NOI18N

        jLabel1.setText(bundle.getString("ControlPanel_Userproperties.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(bundle.getString("ControlPanel_Userproperties.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        labeledTextField2.set_Label(bundle.getString("ControlPanel_Userproperties.labeledTextField2._Label")); // NOI18N
        labeledTextField2.set_Text(bundle.getString("ControlPanel_Userproperties.labeledTextField2._Text")); // NOI18N
        labeledTextField2.setName("labeledTextField2"); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labeledTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(93, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(labeledTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15))
        );

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

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(smtptls, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(smtphost, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(smtppw, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(smtpuser, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(45, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(smtpuser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(smtppw, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(smtptls)))
                    .addComponent(smtphost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton4.setText(bundle.getString("ControlPanel_Userproperties.jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton4);

        jButton2.setText(bundle.getString("ControlPanel_Userproperties.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton2);

        jButton1.setText(bundle.getString("ControlPanel_Userproperties.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton1);

        add(jPanel6, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        setSettings();

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setSettings();
        MPView.getUser().saveProperties();
        Popup.notice(Messages.RESTART_REQUIRED);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        generate();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        MPControlPanel.instanceOf().openDetails(new ControlPanel_MailTemplates());
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
    private javax.swing.JCheckBox autorev;
    private javax.swing.JCheckBox cname;
    private javax.swing.JCheckBox cnumber;
    private javax.swing.JCheckBox columnmeasure;
    private javax.swing.JCheckBox columnquantity;
    private mpv5.ui.beans.LabeledTextField defcount;
    private javax.swing.JPanel defs;
    private mpv5.ui.beans.LabeledCombobox deftax;
    private mpv5.ui.beans.LabeledTextField defunit;
    private javax.swing.JCheckBox description;
    private javax.swing.JCheckBox ean;
    private mpv5.ui.beans.LabeledTextField format;
    private javax.swing.JCheckBox hideproductscolumn;
    private javax.swing.JCheckBox hidetaxcolumn;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private mpv5.ui.beans.LabeledTextField labeledTextField1;
    private mpv5.ui.beans.LabeledTextField labeledTextField2;
    private javax.swing.JCheckBox ordersoverproducts;
    private javax.swing.JPanel productstobillsproperties;
    private javax.swing.JCheckBox reference;
    private mpv5.ui.beans.LabeledCombobox shiptax;
    private javax.swing.JCheckBox showoptionalcolumn;
    private mpv5.ui.beans.LabeledTextField smtphost;
    private javax.swing.JPasswordField smtppw;
    private javax.swing.JCheckBox smtptls;
    private mpv5.ui.beans.LabeledTextField smtpuser;
    private javax.swing.JCheckBox supresscurrencysymbols;
    private javax.swing.JCheckBox tabs;
    private javax.swing.JCheckBox unpaidbills;
    private javax.swing.JCheckBox views;
    // End of variables declaration//GEN-END:variables

    private void setSettings() {

        if (format.getText().length() == 0) {
            generate();
        }
        try {
            MPView.getUser().getProperties().changeProperty(Context.getProduct() + LightMPComboBox.VALUE_SEARCHFIELDS, format.getText());
            MPView.getUser().getProperties().changeProperty(defcount.getName(), defcount.getText());
            MPView.getUser().getProperties().changeProperty(defunit.getName(), defunit.getText());
            MPView.getUser().getProperties().changeProperty(MPView.getTabPane(), "norecycletabs", tabs.isSelected());
            MPView.getUser().getProperties().changeProperty(MPView.getTabPane(), "avoidmultipleviews", views.isSelected());
            MPView.getUser().getProperties().changeProperty(MPView.getTabPane(), "hideunpaidbills", unpaidbills.isSelected());
            MPView.getUser().getProperties().changeProperty(MPView.getTabPane(), "hidecolumnquantity", columnquantity.isSelected());
            MPView.getUser().getProperties().changeProperty(MPView.getTabPane(), "hideproductscolumn", hideproductscolumn.isSelected());
            MPView.getUser().getProperties().changeProperty(MPView.getTabPane(), "hidecolumnmeasure", columnmeasure.isSelected());
            MPView.getUser().getProperties().changeProperty(MPView.getTabPane(), "hidetaxcolumn", hidetaxcolumn.isSelected());
            MPView.getUser().getProperties().changeProperty(MPView.getTabPane(), "showoptionalcolumn", showoptionalcolumn.isSelected());
            MPView.getUser().getProperties().changeProperty(MPView.getTabPane(), "supresscurrencysymbols", supresscurrencysymbols.isSelected());
            MPView.getUser().getProperties().changeProperty(MPView.getTabPane(), "autocreaterevenue", autorev.isSelected());
            MPView.getUser().getProperties().changeProperty(MPView.getTabPane(), "ordersoverproducts", ordersoverproducts.isSelected());
            MPView.getUser().getProperties().changeProperty(MPView.getTabPane(), "propertiesdefined", true);

            if (deftax.getSelectedItem() != null) {
                MPView.getUser().getProperties().changeProperty(deftax.getName(), deftax.getSelectedItem().getId());
            }
            if (shiptax.getSelectedItem() != null) {
                MPView.getUser().getProperties().changeProperty(shiptax.getName(), shiptax.getSelectedItem().getId());
            }

            MPView.getUser().getProperties().changeProperty("bills.warn.days", labeledTextField1.getText());
            MPView.getUser().getProperties().changeProperty("bills.alert.days", labeledTextField2.getText());
            MPView.getUser().getProperties().changeProperty("smtp.host", smtphost.getText());
            MPView.getUser().getProperties().changeProperty("smtp.host.user", smtpuser.getText());
            MPView.getUser().getProperties().changeProperty("smtp.host.password", new String(smtppw.getPassword()));
            MPView.getUser().getProperties().changeProperty("smtp.host.usetls", Boolean.toString(smtptls.isSelected()));
            MPView.getUser().defineMailConfig();
        } catch (Exception e) {
            Log.Debug(e);
        }
        loadSettings();
    }

    @Override
    public Component getAndRemoveActionPanel() {
        this.remove(jPanel6);
        validate();
        return jPanel6;
    }

    private void loadSettings() {
        tabs.setSelected(MPView.getUser().getProperties().getProperty(MPView.getTabPane(), "norecycletabs"));
        views.setSelected(MPView.getUser().getProperties().getProperty(MPView.getTabPane(), "avoidmultipleviews"));
        unpaidbills.setSelected(MPView.getUser().getProperties().getProperty(MPView.getTabPane(), "hideunpaidbills"));
        columnquantity.setSelected(MPView.getUser().getProperties().getProperty(MPView.getTabPane(), "hidecolumnquantity"));
        columnmeasure.setSelected(MPView.getUser().getProperties().getProperty(MPView.getTabPane(), "hidecolumnmeasure"));
        hideproductscolumn.setSelected(MPView.getUser().getProperties().getProperty(MPView.getTabPane(), "hideproductscolumn"));
        showoptionalcolumn.setSelected(MPView.getUser().getProperties().getProperty(MPView.getTabPane(), "showoptionalcolumn"));
        supresscurrencysymbols.setSelected(MPView.getUser().getProperties().getProperty(MPView.getTabPane(), "supresscurrencysymbols"));
        autorev.setSelected(MPView.getUser().getProperties().getProperty(MPView.getTabPane(), "autocreaterevenue"));
        ordersoverproducts.setSelected(MPView.getUser().getProperties().getProperty(MPView.getTabPane(), "ordersoverproducts"));
        hidetaxcolumn.setSelected(MPView.getUser().getProperties().getProperty(MPView.getTabPane(), "hidetaxcolumn"));

        Component[] t = productstobillsproperties.getComponents();
        for (int i = 0; i < t.length; i++) {
            Component component = t[i];
            if (MPView.getUser().getProperties().hasProperty(Context.getProduct() + LightMPComboBox.VALUE_SEARCHFIELDS)) {
                if (component instanceof JCheckBox) {
                    try {
                        ((JCheckBox) component).setSelected(MPView.getUser().getProperties().getProperty(Context.getProduct() + LightMPComboBox.VALUE_SEARCHFIELDS).contains(component.getName()));
                    } catch (Exception e) {
                    }
                }
            }
        }
        String s = MPView.getUser().getProperties().getProperty(Context.getProduct() + LightMPComboBox.VALUE_SEARCHFIELDS);
        if (s != null) {
            format.setText(s);
        } else {
            format.setText("_$cname$_ ");
        }

        if (MPView.getUser().getProperties().hasProperty(defcount.getName())) {
            defcount.setText(MPView.getUser().getProperties().getProperty(defcount.getName()));
        } else {
            defcount.setText("1");
        }

        if (MPView.getUser().getProperties().hasProperty(defcount.getName())) {
            defcount.setText(MPView.getUser().getProperties().getProperty(defcount.getName()));
        } else {
            defcount.setText("1");
        }

        if (MPView.getUser().getProperties().hasProperty("bills.warn.days")) {
            labeledTextField1.setText(MPView.getUser().getProperties().getProperty("bills.warn.days"));
        } else {
            labeledTextField1.setText("14");
        }

        if (MPView.getUser().getProperties().hasProperty("bills.alert.days")) {
            labeledTextField2.setText(MPView.getUser().getProperties().getProperty("bills.alert.days"));
        } else {
            labeledTextField2.setText("30");
        }


        if (MPView.getUser().getProperties().hasProperty("smtp.host")) {
            smtphost.setText(MPView.getUser().getProperties().getProperty("smtp.host"));
        }

        if (MPView.getUser().getProperties().hasProperty("smtp.host.user")) {
            smtpuser.setText(MPView.getUser().getProperties().getProperty("smtp.host.user"));
        }

        if (MPView.getUser().getProperties().hasProperty("smtp.host.password")) {
            smtppw.setText(MPView.getUser().getProperties().getProperty("smtp.host.password"));
        }

        if (MPView.getUser().getProperties().hasProperty("smtp.host.usetls")) {
            smtptls.setSelected(MPView.getUser().getProperties().getProperty("smtp.host.usetls", true));
        }
        try {
            Runnable runnable = new Runnable() {

                public void run() {
                    try {
                        deftax.setSelectedItem(Integer.valueOf(MPView.getUser().getProperties().getProperty(deftax.getName())));
                    } catch (NumberFormatException numberFormatException) {
                    }
                    try {
                        shiptax.setSelectedItem(Integer.valueOf(MPView.getUser().getProperties().getProperty(shiptax.getName())));
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
