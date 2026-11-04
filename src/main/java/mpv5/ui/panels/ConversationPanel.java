/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ConversationUI.java
 *
 * Created on 06.04.2011, 20:17:07
 */
package mpv5.ui.panels;

import mpv5.handler.TemplateHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.rtf.RTFEditorKit;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Address;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Conversation;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.Group;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.Search2;
import mpv5.ui.toolbars.DataPanelTB;
import mpv5.utils.export.Export;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 * @author Jan Hahnisch 
 */
public class ConversationPanel
        extends javax.swing.JPanel
        implements DataPanel,
        ExportablePanel {

    private static final long serialVersionUID = -7873021087487613697L;
    private static ConversationPanel me;

    /**
     * Singleton
     * @return
     */
    public static ConversationPanel instanceOf() {
        if (me == null) {
            me = new ConversationPanel();
        } else {
            me.refresh_internal();
        }
        return me;
    }
    private int Type = Constants.TYPE_CONVERSATION;
    private DataPanelTB tb;
    private SearchPanel sp;
    private Conversation dataOwner;
    public Group group_;
    public String cnumber_ = "";
    public String content_ = "";
    public String cname_ = "";
    public String adress_ = "";
    public Date date_ = null;
    public Date dateadded_;
    Contact contact = null;
    List data = null;
    Thread t = null;
    public Integer intaddedby_;
    public int contactsids_;

    /** Creates new form ConversationUI */
    public ConversationPanel() {
        initComponents();
        Log.Debug(ConversationPanel.class,
                "Erstellung gestartet ...");
        preloadTemplates();
        sp = new SearchPanel(Context.getConversation(), this);
        sp.setVisible(true);
        tb = new DataPanelTB(this);
        tb.getMailButton().setEnabled(false);
        SearchBarPane.add(sp,
                BorderLayout.CENTER);
        Log.Debug(ConversationPanel.class,
                "Suchbar eingebaut ...");
        ToolBarPane.add(tb,
                BorderLayout.CENTER);
        Log.Debug(ConversationPanel.class,
                "Toolbar eingebaut ...");
        dataOwner = new Conversation();
        RTFEditorKit kit = (RTFEditorKit) RTF_Text.getEditorKitForContentType(
                "text/rtf");
        DefaultStyledDocument doc = (DefaultStyledDocument) kit.createDefaultDocument();
        RTF_Text.setEditorKit(kit);
        RTF_Text.setDocument(doc);

        addedby.setText(mpv5.db.objects.User.getCurrentUser().getName());
        contactname.setSearchEnabled(true);
        contactname.setContext(Context.getCustomer());
        contactname.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Log.Debug(this, e.getActionCommand());
                final MPComboBoxModelItem item = contactname.getSelectedItem();
                if (item != null && item.isValid()) {
                    t = new Thread() {

                        @Override
                        public void run() {
                            Address adr;
                            try {
                                contact = (Contact) DatabaseObject.getObject(Context.getContact(), Integer.valueOf(item.getId()));

                                data = DatabaseObject.getReferencedObjects(contact,
                                        Context.getAddress());
                                if (data.isEmpty()) {
                                    contactcity.setText(contact.__getCity());
                                    contactcompany.setText(contact.__getCompany());
                                    contactzip.setText(contact.__getZip());
                                    contactstreet.setText(contact.__getStreet());
                                } else {
                                    contactcity.setText("");
                                    contactcompany.setText("");
                                    contactzip.setText("");
                                    contactstreet.setText("");
                                }
                                contactid.setText(contact.__getCNumber());
                                Object[][] data1 = new Object[data.size() + 1][2];
                                data1[0][0] = 0;
                                data1[0][1] = Messages.Conversation_MainAddress.toString();
                                for (int o = 0; o < data.size(); o++) {
                                    adr = (Address) data.get(o);
                                    data1[o + 1][0] = adr.__getIDS();
                                    data1[o + 1][1] = adr.__getCname();
                                }
                                adressList.setModel(data1);
                            } catch (NodataFoundException ex) {
                                Log.Debug(this, ex);
                            }
                        }
                    };
                    t.start();
                }
            }
        });

        adressList.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final MPComboBoxModelItem item = adressList.getSelectedItem();
                if (item != null) {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            Log.Debug(this, "AdressListe");
                            if (item.getId().equals("0")) {
                                contactcity.setText(contact.__getCity());
                                contactcompany.setText(contact.__getCompany());
                                contactzip.setText(contact.__getZip());
                                contactstreet.setText(contact.__getStreet());
                                contactid.setText(contact.__getCNumber());
                            } else {
                                for (int i = 0; i < data.size(); i++) {
                                    Address adr = (Address) data.get(i);
                                    if (item.getId() == null ? adr.__getIDS().toString() == null : item.getId().equals(adr.__getIDS().toString())) {
                                        contactcity.setText(adr.__getCity());
                                        contactcompany.setText(adr.__getCompany());
                                        contactzip.setText(adr.__getZip());
                                        contactstreet.setText(adr.__getStreet());
                                    }
                                }
                            }
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                }
            }
        });
    }

    private HashMap<String, Object> getAddData() {
        HashMap<String, Object> hm1 = null;
        HashMap<String, Object> hm2 = new HashMap<String, Object>();
        if (contact != null) {
            String AdressName = adressList.getSelectedItem().getId();
            int adressID = Integer.parseInt(AdressName);
            Log.Debug(this,
                    "Kontakt zum drucken geladen ...");
            if (adressID == 0) {
                hm1 = contact.getValues4();
                Log.Debug(this,
                        "Hauptkontakt ausgegeben ...");
            } else {
                for (int i = 0; i < data.size(); i++) {
                    Address adr = (Address) data.get(i);
                    if (adressID == adr.__getIDS()) {
                        hm1 = adr.getValues4();
                    }
                }
                Log.Debug(this,
                        "gezielte Addresse geladen ...");
            }
            Iterator<String> iterator = hm1.keySet().iterator();
            Object e;
            while (iterator.hasNext()) {
                e = iterator.next();
                hm2.put(dataOwner.getType() + "." + contact.getType() + "." + e.toString(),
                        hm1.get(e));
                if (e.equals("ismale")) {
                    if (hm1.get(e).
                            equals(true)) {
                        hm2.put(dataOwner.getType() + "." + contact.getType() + ".gender",
                                Messages.CONTACT_TYPE_MALE.toString());
                        hm2.put(dataOwner.getType() + ".intro",
                                Messages.CONTACT_INTRO_MALE.toString());
                    } else {
                        hm2.put(dataOwner.getType() + "." + contact.getType() + ".gender",
                                Messages.CONTACT_TYPE_FEMALE.toString());
                        hm2.put(dataOwner.getType() + ".intro",
                                Messages.CONTACT_INTRO_FEMALE.toString());
                    }
                }
            }
        }
        return hm2;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ToolBarPane = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        street = new javax.swing.JPanel();
        contactname = new mpv5.ui.beans.LabeledCombobox();
        contactcity = new javax.swing.JTextField();
        contactcompany = new javax.swing.JTextField();
        contactid = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        adressList = new mpv5.ui.beans.LabeledCombobox();
        contactzip = new javax.swing.JTextField();
        contactstreet = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jComboBox2 = new javax.swing.JComboBox();
        String fontSizes[] = {"8", "10", "11", "12", "14", "16", "18",
            "20", "24", "30", "36", "40", "48", "60", "72"};
        for (int index = 0; index < fontSizes.length; index++) {
            jComboBox2.addItem(fontSizes[index]);
        }
        jComboBox1 = new javax.swing.JComboBox();
        try {
            String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            Log.Debug(this, "Ermittelte Fonts: " + fonts.length);

            for (int index = 0; index < fonts.length; index++) {
                jComboBox1.addItem(fonts[index]);
            }
        } catch (Exception e) {

            Log.Debug(ConversationPanel.class, e);
        }
        jToggleButton3 = new javax.swing.JToggleButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton2 = new javax.swing.JToggleButton();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        RTF_Text = new javax.swing.JTextPane();
        jLabel7 = new javax.swing.JLabel();
        dateTo = new mpv5.ui.beans.DateChooser();
        cname = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        ids = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        addedby = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        dateadded = new mpv5.ui.beans.DateChooser();
        dateadded.setEnabled(false);
        SearchBarPane = new javax.swing.JPanel();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        ToolBarPane.setName("ToolBarPane"); // NOI18N
        ToolBarPane.setLayout(new java.awt.BorderLayout());
        add(ToolBarPane, java.awt.BorderLayout.NORTH);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setName("jPanel1"); // NOI18N

//$2java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        street.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ConversationPanel.street.border.title"))); // NOI18N
        street.setName("street"); // NOI18N
        street.setOpaque(false);

        contactname.set_Label(bundle.getString("ConversationPanel.contactname._Label")); // NOI18N
        contactname.setName("contactname"); // NOI18N

        contactcity.setEditable(false);
        contactcity.setText(bundle.getString("ConversationPanel.contactcity.text")); // NOI18N
        contactcity.setName("contactcity"); // NOI18N

        contactcompany.setEditable(false);
        contactcompany.setText(bundle.getString("ConversationPanel.contactcompany.text")); // NOI18N
        contactcompany.setName("contactcompany"); // NOI18N

        contactid.setEditable(false);
        contactid.setText(bundle.getString("ConversationPanel.contactid.text")); // NOI18N
        contactid.setName("contactid"); // NOI18N

        jButton3.setText(bundle.getString("ConversationPanel.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionShowContact(evt);
            }
        });

        jButton4.setText(bundle.getString("ConversationPanel.jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionLookupContact(evt);
            }
        });

        adressList.set_Label(bundle.getString("ConversationPanel.adressList._Label")); // NOI18N
        adressList.setName("adressList"); // NOI18N

        contactzip.setEditable(false);
        contactzip.setText(bundle.getString("ConversationPanel.contactzip.text")); // NOI18N
        contactzip.setName("contactzip"); // NOI18N

        contactstreet.setEditable(false);
        contactstreet.setText(bundle.getString("ConversationPanel.contactstreet.text")); // NOI18N
        contactstreet.setName("contactstreet"); // NOI18N

        javax.swing.GroupLayout streetLayout = new javax.swing.GroupLayout(street);
        street.setLayout(streetLayout);
        streetLayout.setHorizontalGroup(
            streetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(streetLayout.createSequentialGroup()
                .addGroup(streetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(streetLayout.createSequentialGroup()
                        .addComponent(contactcompany, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(contactcity, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(contactname, javax.swing.GroupLayout.PREFERRED_SIZE, 338, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(streetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(streetLayout.createSequentialGroup()
                        .addComponent(adressList, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4))
                    .addGroup(streetLayout.createSequentialGroup()
                        .addComponent(contactzip, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(contactstreet, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(contactid)))
                .addGap(28, 28, 28))
        );
        streetLayout.setVerticalGroup(
            streetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(streetLayout.createSequentialGroup()
                .addGroup(streetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(adressList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4)
                    .addComponent(contactname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(streetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(contactcompany, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contactcity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contactzip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contactstreet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contactid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ConversationPanel.jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jComboBox2.setName("jComboBox2"); // NOI18N
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });

        jComboBox1.setName("jComboBox1"); // NOI18N
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jToggleButton3.setFont(jToggleButton3.getFont());
        jToggleButton3.setText(bundle.getString("ConversationPanel.jToggleButton3.text")); // NOI18N
        jToggleButton3.setName("jToggleButton3"); // NOI18N
        jToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton3ActionPerformed(evt);
            }
        });

        jToggleButton1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jToggleButton1.setText(bundle.getString("ConversationPanel.jToggleButton1.text")); // NOI18N
        jToggleButton1.setName("jToggleButton1"); // NOI18N
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jToggleButton2.setFont(new java.awt.Font("Tahoma", 2, 11));
        jToggleButton2.setText(bundle.getString("ConversationPanel.jToggleButton2.text")); // NOI18N
        jToggleButton2.setName("jToggleButton2"); // NOI18N
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });

        jLabel3.setLabelFor(dateTo);
        jLabel3.setText(bundle.getString("ConversationPanel.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jSeparator1.setName("jSeparator1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        RTF_Text.setEditorKit(new javax.swing.text.rtf.RTFEditorKit());
        RTF_Text.setName("RTF_Text"); // NOI18N
        RTF_Text.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                RTF_TextCaretUpdate(evt);
            }
        });
        jScrollPane1.setViewportView(RTF_Text);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel7.setLabelFor(cname);
        jLabel7.setText(bundle.getString("ConversationPanel.jLabel7.text")); // NOI18N
        jLabel7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel7.setMaximumSize(new java.awt.Dimension(65, 20));
        jLabel7.setMinimumSize(new java.awt.Dimension(65, 20));
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(65, 20));

        dateTo.setMaximumSize(new java.awt.Dimension(32767, 20));
        dateTo.setMinimumSize(new java.awt.Dimension(120, 20));
        dateTo.setName("dateTo"); // NOI18N
        dateTo.setPreferredSize(new java.awt.Dimension(150, 20));

        cname.setText(bundle.getString("ConversationPanel.cname.text")); // NOI18N
        cname.setName("cname"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 910, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(jToggleButton2)
                        .addGap(5, 5, 5)
                        .addComponent(jToggleButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dateTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cname, javax.swing.GroupLayout.DEFAULT_SIZE, 831, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton1)
                    .addComponent(jToggleButton2)
                    .addComponent(jToggleButton3)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateTo, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ConversationPanel.jPanel4.border.title"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel6.setLabelFor(ids);
        jLabel6.setText(bundle.getString("ConversationPanel.jLabel6.text")); // NOI18N
        jLabel6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel6.setMaximumSize(new java.awt.Dimension(333, 20));
        jLabel6.setMinimumSize(new java.awt.Dimension(65, 20));
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setOpaque(true);
        jLabel6.setPreferredSize(new java.awt.Dimension(120, 20));
        jPanel4.add(jLabel6);

        ids.setMaximumSize(new java.awt.Dimension(333, 20));
        ids.setMinimumSize(new java.awt.Dimension(65, 20));
        ids.setName("ids"); // NOI18N
        ids.setPreferredSize(new java.awt.Dimension(120, 20));
        jPanel4.add(ids);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setLabelFor(addedby);
        jLabel4.setText(bundle.getString("ConversationPanel.jLabel4.text")); // NOI18N
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel4.setMaximumSize(new java.awt.Dimension(333, 20));
        jLabel4.setMinimumSize(new java.awt.Dimension(65, 20));
        jLabel4.setName("jLabel4"); // NOI18N
        jLabel4.setOpaque(true);
        jLabel4.setPreferredSize(new java.awt.Dimension(120, 20));
        jPanel4.add(jLabel4);

        addedby.setEditable(false);
        addedby.setMaximumSize(new java.awt.Dimension(333, 20));
        addedby.setMinimumSize(new java.awt.Dimension(65, 20));
        addedby.setName("addedby"); // NOI18N
        addedby.setPreferredSize(new java.awt.Dimension(120, 20));
        jPanel4.add(addedby);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setLabelFor(ids);
        jLabel5.setText(bundle.getString("ConversationPanel.jLabel5.text")); // NOI18N
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel5.setMaximumSize(new java.awt.Dimension(333, 20));
        jLabel5.setMinimumSize(new java.awt.Dimension(65, 20));
        jLabel5.setName("jLabel5"); // NOI18N
        jLabel5.setOpaque(true);
        jLabel5.setPreferredSize(new java.awt.Dimension(120, 20));
        jPanel4.add(jLabel5);

        dateadded.setMaximumSize(new java.awt.Dimension(333, 20));
        dateadded.setMinimumSize(new java.awt.Dimension(65, 20));
        dateadded.setName("dateadded"); // NOI18N
        dateadded.setOpaque(true);
        dateadded.setPreferredSize(new java.awt.Dimension(120, 20));
        jPanel4.add(dateadded);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(street, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(street, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);

        SearchBarPane.setName("SearchBarPane"); // NOI18N
        SearchBarPane.setLayout(new java.awt.BorderLayout());
        add(SearchBarPane, java.awt.BorderLayout.WEST);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        JComboBox comboBox = (JComboBox) evt.getSource();
        javax.swing.text.StyledEditorKit.FontFamilyAction action =
                new StyledEditorKit.FontFamilyAction("xxx",
                comboBox.getSelectedItem().
                toString());
        action.actionPerformed(new ActionEvent(comboBox,
                ActionEvent.ACTION_PERFORMED,
                "xxx"));
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        JToggleButton btn = (JToggleButton) evt.getSource();
        RTFEditorKit kit = (RTFEditorKit) RTF_Text.getEditorKit();
        MutableAttributeSet attr = kit.getInputAttributes();
        boolean bold =
                !(StyleConstants.isBold(attr));
        btn.setSelected(bold);
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setBold(sas,
                bold);
        RTF_Text.setCharacterAttributes(sas,
                false);
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
        JToggleButton btn = (JToggleButton) evt.getSource();
        RTFEditorKit kit = (RTFEditorKit) RTF_Text.getEditorKit();
        MutableAttributeSet attr = kit.getInputAttributes();
        boolean italic =
                !(StyleConstants.isItalic(attr));
        btn.setSelected(italic);
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setItalic(sas,
                italic);
        RTF_Text.setCharacterAttributes(sas,
                false);
    }//GEN-LAST:event_jToggleButton2ActionPerformed

    private void jToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton3ActionPerformed
        JToggleButton btn = (JToggleButton) evt.getSource();
        RTFEditorKit kit = (RTFEditorKit) RTF_Text.getEditorKit();
        MutableAttributeSet attr = kit.getInputAttributes();
        boolean under =
                !(StyleConstants.isUnderline(attr));
        btn.setSelected(under);
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setUnderline(sas,
                under);
        RTF_Text.setCharacterAttributes(sas,
                false);
    }//GEN-LAST:event_jToggleButton3ActionPerformed

    private void RTF_TextCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_RTF_TextCaretUpdate
        JTextPane text = (JTextPane) evt.getSource();
        StyledDocument doc = text.getStyledDocument();

        if (doc.getLength() > 0) {
            Boolean isBold = null;
            Boolean isItalic = null;
            Boolean isUnderline = null;
            Boolean tmp;
            String fontFamily = null;
            int fontSize = 8;

            for (int i = evt.getMark(); i < evt.getDot(); i++) {
                AttributeSet atts = doc.getCharacterElement(i).
                        getAttributes();

                tmp = StyleConstants.isBold(atts);
                if (isBold == null) {
                    isBold = tmp;
                } else if (tmp.equals(isBold) == false) {
                    isBold = false;
                }

                tmp = StyleConstants.isItalic(atts);
                if (isItalic == null) {
                    isItalic = tmp;
                } else if (tmp.equals(isItalic) == false) {
                    isItalic = false;
                }

                tmp = StyleConstants.isUnderline(atts);
                if (isUnderline == null) {
                    isUnderline = tmp;
                } else if (tmp.equals(isUnderline) == false) {
                    isUnderline = false;
                }

                fontFamily = StyleConstants.getFontFamily(atts);
                fontSize = StyleConstants.getFontSize(atts);
            }

            if (isBold == null && isItalic == null && isUnderline == null) {
                AttributeSet atts = doc.getCharacterElement(evt.getDot() - 1).
                        getAttributes();
                isBold = StyleConstants.isBold(atts);
                isItalic = StyleConstants.isItalic(atts);
                isUnderline = StyleConstants.isUnderline(atts);
            }

            if (fontFamily == null || fontSize == 8) {
                AttributeSet atts = doc.getCharacterElement(evt.getDot() - 1).
                        getAttributes();
                fontFamily = StyleConstants.getFontFamily(atts);
                fontSize = StyleConstants.getFontSize(atts);
            }

            jToggleButton1.setSelected(isBold);
            jToggleButton2.setSelected(isItalic);
            jToggleButton3.setSelected(isUnderline);
            jComboBox1.setSelectedItem(fontFamily);
            jComboBox2.setSelectedItem(String.valueOf(fontSize));
        }
}//GEN-LAST:event_RTF_TextCaretUpdate

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        JComboBox comboBox = (JComboBox) evt.getSource();
        javax.swing.text.StyledEditorKit.FontSizeAction action =
                new StyledEditorKit.FontSizeAction("xxx",
                Integer.parseInt(comboBox.getSelectedItem().
                toString()));
        action.actionPerformed(new ActionEvent(comboBox,
                ActionEvent.ACTION_PERFORMED,
                "xxx"));
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void actionShowContact(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionShowContact
        try {
            int cid = Integer.valueOf(contactname.getSelectedItem().getId());
            Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), cid);
            ContactPanel cp = new ContactPanel(Context.getContact());
            mpv5.YabsViewProxy.instance().getIdentifierView().addOrShowTab(cp, Messages.TYPE_CONTACT.toString());
            cp.setDataOwner(c, true);
        } catch (Exception e) {
            Log.Debug(this, e);
        }
    }//GEN-LAST:event_actionShowContact

    private void actionLookupContact(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionLookupContact
        Contact dbo = (Contact) Search2.showSearchFor(Context.getContact());
        if (dbo != null) {
            contact = dbo;
            contactname.setModel(dbo);
            contactcity.setText(dbo.__getCity());
            contactzip.setText(dbo.__getZip());
            contactstreet.setText(dbo.__getStreet());
            contactcompany.setText(dbo.__getCompany());
            contactid.setText(dbo.__getCNumber());
        }
    }//GEN-LAST:event_actionLookupContact
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane RTF_Text;
    private javax.swing.JPanel SearchBarPane;
    private javax.swing.JPanel ToolBarPane;
    private javax.swing.JTextField addedby;
    private mpv5.ui.beans.LabeledCombobox adressList;
    private javax.swing.JTextField cname;
    private javax.swing.JTextField contactcity;
    private javax.swing.JTextField contactcompany;
    private javax.swing.JTextField contactid;
    private mpv5.ui.beans.LabeledCombobox contactname;
    private javax.swing.JTextField contactstreet;
    private javax.swing.JTextField contactzip;
    private mpv5.ui.beans.DateChooser dateTo;
    private mpv5.ui.beans.DateChooser dateadded;
    private javax.swing.JTextField ids;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JPanel street;
    // End of variables declaration//GEN-END:variables

    /**
     *  Give the DatabaseObject back
     * @return The DatabaseObject
     */
    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    /**
     * Sets the DatabaseObject
     * @param populate 
     */
    @Override
    public void setDataOwner(DatabaseObject object,
            boolean populate) {
        dataOwner = (Conversation) object;
        if (populate) {
            this.preloadTemplates();
            dataOwner.setPanelData(this);
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    try {
                        me.exposeData();
                    } catch (Exception e) {
                        Log.Debug(this,
                                e);
                    }
                }
            };

            SwingUtilities.invokeLater(runnable);

            if (dataOwner.isExisting() && populate) {
                setTitle(dataOwner.__getCname());
            }
            tb.setFavourite(Favourite.isFavourite(object));
            tb.setEditable(!object.isReadOnly());

            if (object.isReadOnly()) {
                Popup.notice(Messages.LOCKED_BY);
            }

            validate();
        }
    }

    private void refresh_internal() {
        contact = null;
        sp.refresh();
        cname.setText("");
        contactcity.setText("");
        contactcompany.setText("");
        contactzip.setText("");
        contactstreet.setText("");
        contactid.setText("");
        dateTo.setDate(new Date());
        dateadded.setDate(new Date());
        addedby.setText(String.valueOf(User.getUserId(User.getCurrentUser().
                toString())));
        ids.setText("");
        try {
            RTF_Text.getStyledDocument().
                    remove(0,
                    RTF_Text.getStyledDocument().
                    getLength());
        } catch (BadLocationException ex) {
            Log.Debug(this,
                    "Fehler löschen Textfeld!!");
        }
        Log.Debug(this,
                "UI zurückgesetzt!!");
    }

    /**
     * Pastestub of the Copy&Paste-function 
     * @param arg0 
     */
    @Override
    public void paste(DatabaseObject... arg0) {
        mpv5.YabsViewProxy.instance().
                addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE.toString(), Color.RED);
    }

    /**
     * Checks the fullfillment of all requierements
     */
    @Override
    public void showRequiredFields() {
        TextFieldUtils.blink(contactid,
                Color.RED);
        TextFieldUtils.blink(adressList,
                Color.RED);
        TextFieldUtils.blink(contactname,
                Color.RED);
        TextFieldUtils.blinkerRed(cname);
        cname.requestFocus();
    }

    /**
     * enables / disables the Searchbar
     * @param show 
     */
    @Override
    public void showSearchBar(boolean show) {

        SearchBarPane.removeAll();

        if (show) {
            SearchBarPane.add(sp,
                    BorderLayout.CENTER);
            Log.Debug(ConversationPanel.class,
                    "Suchfenster eingeblendet");
            sp.search();
        } else {
            Log.Debug(ConversationPanel.class,
                    "Suchfenster ausgeblendet");
        }

        validate();
    }

    /**
     * empty Function
     */
    @Override
    public void actionAfterSave() {
    }

    /**
     * empty Function
     */
    @Override
    public void actionAfterCreate() {
        sp.refresh();
    }

    /**
     * empty Function
     */
    @Override
    public void actionBeforeCreate() {
    }

    /**
     * Do an action bevor the System saves
     * @throws ChangeNotApprovedException 
     */
    @Override
    public void actionBeforeSave() throws ChangeNotApprovedException {
        if (dataOwner.isExisting()) {
            if (!mpv5.db.objects.User.getCurrentUser().
                    getProperties().
                    getProperty("org.openyabs.uiproperty",
                    "nowarnings")) {

                if (!Popup.Y_N_dialog(Messages.REALLY_CHANGE)) {
                    throw new ChangeNotApprovedException(dataOwner);
                }
            }
        }
    }

    /**
     * Sends the DataObject as Mail 
     */
    @Override
    public void mail() {
//        if (dataOwner != null && dataOwner.isExisting()) {
//            if (TemplateHandler.isLoaded(dataOwner.templateGroupIds(), dataOwner.__getInttype())) {
//
//                try {
//                    Contact cont = (Contact) (Contact.getObject(Context.getContact(), dataOwner.__getContactsids()));
//                    Export.mail(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.__getInttype()), dataOwner, cont);
//                } catch (NodataFoundException ex) {
//                    Log.Debug(ex);
//                }
//            } else {
//                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
//            }
//        } else {
//            Popup.notice(Messages.NOT_POSSIBLE + "\n" + Messages.NOT_SAVED_YET);
//        }
    }

    /**
     * prints the DataObject
     */
    @Override
    public void print() {
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()),
                    Type)) {
                Export.print(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(),
                        Type),
                        dataOwner);
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
                Export.print(this);
            }
        } else {
            Popup.notice(Messages.FILE_NOT_SAVED);
        }
    }

    /**
     * Sends the DataObject as PDF
     */
    @Override
    public void pdf() {
        if (dataOwner != null && dataOwner.isExisting()) {
            dataOwner.toPdf(true);
        } else {
            Popup.notice(Messages.FILE_NOT_SAVED);
        }
    }

    /**
     * Sends the DataObject as odt 
     */
    @Override
    public void odt() {
        if (dataOwner != null && dataOwner.isExisting()) {
            dataOwner.toOdt(true);
        } else {
            Popup.notice(Messages.FILE_NOT_SAVED);
        }
    }

    /**
     * Collects the Data from the Userscreen 
     */
    @Override
    public boolean collectData() {
        if (contactname.getSelectedItem() == null) {
            showRequiredFields();
            return false;
        } else {
            group_ = Group.getDefault();
            cname_ = cname.getText();
            cnumber_ = contactid.getText();
            contactsids_ = Integer.parseInt(contactname.getSelectedItem().getId());
            adress_ = adressList.getSelectedItem().getId();
            date_ = dateTo.getDate();
            dateadded_ = dateadded.getDate();
            intaddedby_ = User.getUserId(addedby.getText());
            content_ = serialize();

            if (cname_.length() == 0
                    || cnumber_.length() == 0
                    || adress_.equals("choose")) {
                Popup.notice(Messages.CONTACT_COMPLETE.toString());
                return false;
            }

            Object[][] liste = new DatabaseSearch(dataOwner).getValuesFor(
                    Context.IDENTITY_CONVERSATION + "." + "CNAME",
                    "cname",
                    cname_);
            if (liste.length > 0 && !dataOwner.isExisting()) {
                Popup.notice(
                        Messages.CNAME_EXISTS.toString());
                return false;
            }
        }
        return true;
    }

    /**
     * Puts the Data to the Userscreen
     */
    @Override
    public void exposeData() {
        cname.setText(cname_);
        fillContactFields();
        dateTo.setDate(date_);
        dateadded.setDate(dateadded_);
        addedby.setText(User.getUsername(intaddedby_));
        ids.setText(dataOwner.__getIDS().
                toString());
        deserialize(content_);
    }

    private String serialize() {
        ByteArrayOutputStream out;
        RTFEditorKit kit = (RTFEditorKit) RTF_Text.getEditorKit();
        StyledDocument doc = RTF_Text.getStyledDocument();
        out = new ByteArrayOutputStream();
        try {
            kit.write(out,
                    doc,
                    0,
                    doc.getLength());
        } catch (BadLocationException ex) {
            Log.Debug(this,
                    ex.toString());
        } catch (IOException ex) {
            Log.Debug(this,
                    ex.toString());
        }
        String str;

        str = out.toString();
        str = str.replaceAll("[\r\n]+",
                " ");
        str = str.replaceAll("  ",
                " ");
        return str;
    }

    private void deserialize(String in) {
        ByteArrayInputStream stream;
        in = in.replaceAll("`",
                "'");

        RTFEditorKit kit = (RTFEditorKit) RTF_Text.getEditorKit();
        StyledDocument doc = RTF_Text.getStyledDocument();
        stream = new ByteArrayInputStream(in.getBytes());
        Log.Debug(this,
                in);
        try {
            doc.remove(0,
                    doc.getLength());
            kit.read(stream,
                    doc,
                    0);
        } catch (BadLocationException ex) {
            Log.Debug(this,
                    ex.toString());
        } catch (IOException ex) {
            Log.Debug(this,
                    ex.toString());
        }
    }

    private void setTitle(String title) {
        if (this.getParent() instanceof JViewport
                || this.getParent() instanceof JTabbedPane) {
            JTabbedPane jTabbedPane = null;
            String title1 = title;
            if (this.getParent().
                    getParent().
                    getParent() instanceof JTabbedPane) {
                jTabbedPane = (JTabbedPane) this.getParent().
                        getParent().
                        getParent();
            } else {
                try {
                    jTabbedPane = (JTabbedPane) this.getParent();
                } catch (Exception e) {
                    //Free floating window
                    ((JFrame) this.getRootPane().
                            getParent()).setTitle(title1);
                }
            }
            if (jTabbedPane != null) {
                jTabbedPane.setTitleAt(jTabbedPane.getSelectedIndex(),
                        title1);
            }
        }
    }

    /**
     * empty
     */
    @Override
    public void refresh() {
    }

    private void fillContactFields() {
        Log.Debug(this,
                "Felder füllen ...");
        try {
            Log.Debug(this,
                    "... mit ID: " + contactsids_);
            if (contactsids_ > 0) {
                contact = (Contact) DatabaseObject.getObject(Context.getContact(),
                        contactsids_,
                        false);
                contactid.setText(cnumber_);
                contactname.setModel(contact);
                try {
                    t.join();
                    adressList.setSelectedItem(Integer.parseInt(adress_));
                } catch (InterruptedException ex) {
                    Log.Debug(this, ex);
                }
                Log.Debug(this, "test..2");
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this,
                    ex);
        }
    }
    
    private void preloadTemplates() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.templateType());
                } catch (Exception e) {
                }
            }
        };
        new Thread(runnable).start();
    }
}
