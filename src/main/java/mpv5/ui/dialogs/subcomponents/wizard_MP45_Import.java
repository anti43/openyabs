package mpv5.ui.dialogs.subcomponents;

import java.awt.Cursor;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;

import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Product;
import mpv5.db.objects.ProductGroup;
import mpv5.db.objects.Tax;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;
import mpv5.utils.text.RandomText;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 * 
 */
public class wizard_MP45_Import extends javax.swing.JPanel implements Wizardable {

    private static final long serialVersionUID = -8347532498124147821L;
    private WizardMaster master;
    private DatabaseConnection conn;
    private QueryHandler qh;
    private int imports;

    public wizard_MP45_Import(WizardMaster w) {
        this.master = w;
        initComponents();
        path.setFilter(DialogForFile.DIRECTORIES);
        jComboBox1.setModel(new DefaultComboBoxModel(ConnectionTypeHandler.DRIVERS));
        path.setModalityParent(this);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        path = new mpv5.ui.beans.LabeledTextChooser();
        jLabel1 = new javax.swing.JLabel();
        products = new javax.swing.JCheckBox();
        customers = new javax.swing.JCheckBox();
        suppliers = new javax.swing.JCheckBox();
        manufacturers = new javax.swing.JCheckBox();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        dbname = new mpv5.ui.beans.LabeledTextField();
        dbuser = new mpv5.ui.beans.LabeledTextField();
        dbpassword = new mpv5.ui.beans.LabeledTextField();
        dbprefix = new mpv5.ui.beans.LabeledTextField();

        //\$2java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

//$2java.awt.Color(255, 255, 255));
         java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_MP45_Import.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        path.set_Label(bundle.getString("wizard_MP45_Import.path._Label")); // NOI18N
        path.setName("path"); // NOI18N

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() & ~java.awt.Font.BOLD));
        jLabel1.setText(bundle.getString("wizard_MP45_Import.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

//$2java.awt.Color(255, 255, 255));
        products.setSelected(true);
        products.setText(bundle.getString("wizard_MP45_Import.products.text")); // NOI18N
        products.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        products.setName("products"); // NOI18N

//$2java.awt.Color(255, 255, 255));
        customers.setSelected(true);
        customers.setText(bundle.getString("wizard_MP45_Import.customers.text")); // NOI18N
        customers.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        customers.setName("customers"); // NOI18N

//$2java.awt.Color(255, 255, 255));
        suppliers.setSelected(true);
        suppliers.setText(bundle.getString("wizard_MP45_Import.suppliers.text")); // NOI18N
        suppliers.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        suppliers.setName("suppliers"); // NOI18N

//$2java.awt.Color(255, 255, 255));
        manufacturers.setSelected(true);
        manufacturers.setText(bundle.getString("wizard_MP45_Import.manufacturers.text")); // NOI18N
        manufacturers.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        manufacturers.setName("manufacturers"); // NOI18N

        jComboBox1.setEditable(true);
        jComboBox1.setFont(new java.awt.Font("Dialog", 0, 11));
        jComboBox1.setName("jComboBox1"); // NOI18N
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel2.setText(bundle.getString("wizard_MP45_Import.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        dbname.set_Label(bundle.getString("wizard_MP45_Import.dbname._Label")); // NOI18N
        dbname.setName("dbname"); // NOI18N

        dbuser.set_Label(bundle.getString("wizard_MP45_Import.dbuser._Label")); // NOI18N
        dbuser.setName("dbuser"); // NOI18N

        dbpassword.set_Label(bundle.getString("wizard_MP45_Import.dbpassword._Label")); // NOI18N
        dbpassword.setName("dbpassword"); // NOI18N

        dbprefix.set_Label(bundle.getString("wizard_MP45_Import.dbprefix._Label")); // NOI18N
        dbprefix.setName("dbprefix"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(products)
                    .addComponent(customers)
                    .addComponent(suppliers)
                    .addComponent(manufacturers)
                    .addComponent(path, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 431, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(dbuser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 119, Short.MAX_VALUE)
                        .addComponent(dbname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(dbpassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 119, Short.MAX_VALUE)
                        .addComponent(dbprefix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(manufacturers)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(suppliers)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(customers)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(products)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 102, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(path, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(dbuser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbpassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(dbname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbprefix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
    }//GEN-LAST:event_jComboBox1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox customers;
    private mpv5.ui.beans.LabeledTextField dbname;
    private mpv5.ui.beans.LabeledTextField dbpassword;
    private mpv5.ui.beans.LabeledTextField dbprefix;
    private mpv5.ui.beans.LabeledTextField dbuser;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JCheckBox manufacturers;
    private mpv5.ui.beans.LabeledTextChooser path;
    private javax.swing.JCheckBox products;
    private javax.swing.JCheckBox suppliers;
    // End of variables declaration//GEN-END:variables

    public boolean next() {
        master.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        if (path.hasText()) {

            imports();

            return false;
        } else {
            return false;
        }
    }

    public boolean back() {
        return false;
    }

    public void load() {
    }

    private void imports() {
        Runnable runnable = new Runnable() {

            public void run() {
                if (dbname.getText().length() > 0) {
                    if (path.get_Text(false).endsWith(dbname.getText())) {
                        path.set_Text(path.get_Text(false).replace(dbname.getText(), ""));
                    }
                    try {
                        conn = new DatabaseConnection();

                        conn.connect(jComboBox1.getSelectedItem().toString(),
                                dbuser.getText(),
                                dbpassword.getText(),
                                path.get_Text(false),
                                dbname.getText(), dbprefix.getText(),
                                false);
                        conn.setProgressbar(master.getProgressbar());

                        if (conn.getConnection().isValid(1000)) {

                            qh = new QueryHandler(conn);

                            if (manufacturers.isSelected()) {
                                importm();
                            }

                            if (suppliers.isSelected()) {
                                importl();
                            }

                            if (customers.isSelected()) {
                                importc();
                            }

                            if (products.isSelected()) {
                                importp();
                            }
                        }
                        master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        master.isEnd(true);
                        Popup.notice(imports + " " + Messages.IMPORTED.toString());
                        if (!errors.isEmpty()) {
                            Popup.notice(errors, mpv5.globals.Messages.ERROR_OCCURED);
                        }
                    } catch (Exception exception) {
                        Popup.error(exception);
                        Log.Debug(exception);
                    }
                } else {
                    TextFieldUtils.blinkerRed(dbname);
                }
            }
        };
        new Thread(runnable).start();
    }
    List<DatabaseObject> errors = new Vector<DatabaseObject>();
    /**
     * Fields in manufacturer table
     */
    public final String TABLE_MANUFACTURER_FIELDS =
            "nummer" + "," + "Firma" + "," + "Anrede" + "," + "Vorname" +
            "," + "Name" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" + "," + "Fax" + "," + "Mobil" + "," + "Mail" + "," + "Webseite" + "," + "Notizen" + "," + "id";
    HashMap<Integer, Integer> manufacturer = new HashMap<Integer, Integer>();

    private void importm() {
        try {
            qh.setTable2("hersteller");
            Object[][] data = qh.freeSelect(TABLE_MANUFACTURER_FIELDS).getData();
            for (int i = 0; i < data.length; i++) {
                Contact c = new Contact();
                c.setCNumber(String.valueOf(data[i][0]));
                c.setCompany(String.valueOf(data[i][1]));
                c.setTitle(String.valueOf(data[i][2]));
                c.setPrename(String.valueOf(data[i][3]));
                c.setCname(String.valueOf(data[i][4]));
                c.setStreet(String.valueOf(data[i][5]));
                c.setZip(String.valueOf(data[i][6]));
                c.setCity(String.valueOf(data[i][7]));
                c.setMainphone(String.valueOf(data[i][8]));
                c.setFax(String.valueOf(data[i][9]));
                c.setMobilephone(String.valueOf(data[i][10]));
                c.setMailaddress(String.valueOf(data[i][11]));
                c.setWebsite(String.valueOf(data[i][12]));
                c.setNotes(String.valueOf(data[i][12 + 1]));
                c.setisManufacturer(true);
                c.setisMale(true);
                c.saveImport();
                imports++;
                manufacturer.put(Integer.valueOf(String.valueOf(data[i][14])), c.__getIDS());
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
    }
    /**
     * Fields in Kunde table
     */
    public final String TABLE_Kunde_FIELDS =
            "nummer" + "," + "Firma" + "," + "Anrede" + "," + "Vorname" +
            "," + "Name" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" + "," + "Fax" + "," + "Mobil" + "," + "Mail" + "," +
            "Webseite" + "," + "Notizen" + "," + "Steuernummer";

    private void importc() {
        try {
            qh.setTable2("kunden");
            Object[][] data = qh.freeSelect(TABLE_Kunde_FIELDS).getData();
            for (int i = 0; i < data.length; i++) {
                Contact c = new Contact();
                c.setCNumber(String.valueOf(data[i][0]));
                c.setCompany(String.valueOf(data[i][1]));
                c.setTitle(String.valueOf(data[i][2]));
                c.setPrename(String.valueOf(data[i][3]));
                c.setCname(String.valueOf(data[i][4]));
                c.setStreet(String.valueOf(data[i][5]));
                c.setZip(String.valueOf(data[i][6]));
                c.setCity(String.valueOf(data[i][7]));
                c.setMainphone(String.valueOf(data[i][8]));
                c.setFax(String.valueOf(data[i][9]));
                c.setMobilephone(String.valueOf(data[i][10]));
                c.setMailaddress(String.valueOf(data[i][11]));
                c.setWebsite(String.valueOf(data[i][12]));
                c.setNotes(String.valueOf(data[i][12 + 1]));
                c.setisCustomer(true);
                c.setisManufacturer(false);
                c.setisSupplier(false);
                c.setisCompany(false);
                c.setisMale(true);
                c.saveImport();
                imports++;
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
    }
    /**
     * Fields in supplier table
     */
    public final String TABLE_SUPPLIER_FIELDS =
            "nummer" + "," + "Firma" + "," + "Anrede" + "," + "Vorname" +
            "," + "Name" + "," + "Str" + "," + "PLZ" + "," + "Ort" + "," +
            "Tel" + "," + "Fax" + "," + "Mobil" + "," + "Mail" + "," + "Webseite" + "," + "Notizen" + "," + "id";
    HashMap<Integer, Integer> supplier = new HashMap<Integer, Integer>();

    private void importl() {
        try {
            qh.setTable2("lieferanten");
            Object[][] data = qh.freeSelect(TABLE_SUPPLIER_FIELDS).getData();
            for (int i = 0; i < data.length; i++) {
                Contact c = new Contact();
                c.setCNumber(String.valueOf(data[i][0]));
                c.setCompany(String.valueOf(data[i][1]));
                c.setTitle(String.valueOf(data[i][2]));
                c.setPrename(String.valueOf(data[i][3]));
                c.setCname(String.valueOf(data[i][4]));
                c.setStreet(String.valueOf(data[i][5]));
                c.setZip(String.valueOf(data[i][6]));
                c.setCity(String.valueOf(data[i][7]));
                c.setMainphone(String.valueOf(data[i][8]));
                c.setFax(String.valueOf(data[i][9]));
                c.setMobilephone(String.valueOf(data[i][10]));
                c.setMailaddress(String.valueOf(data[i][11]));
                c.setWebsite(String.valueOf(data[i][12]));
                c.setNotes(String.valueOf(data[i][12 + 1]));
                c.setisSupplier(true);
                c.setisMale(true);
                c.saveImport();
                imports++;
                supplier.put(Integer.valueOf(String.valueOf(data[i][14])), c.__getIDS());
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
    }
    /**
     * Fields in products table
     */
    public final String TABLE_PRODUCTS_FIELDS =
            "Produktnummer" + "," + "Name" + "," + "Text" + "," + "VK" + "," + "EK" +
            "," + "steuersatzid" + "," + "herstellerid" + "," + "lieferantenid" + "," +
            "warengruppenid" + "," + "Datum" + "," + "Url" + "," + "EAN" +
            "," + "bestellnr" + "," + "herstellernr" + "," + "lieferantennr" + "," +
            "bestelldatum" + "," + "bestellmenge" + "," + "lagermenge";

    private void importp() {
        try {
            qh.setTable2("steuersaetze");
            Object[][] txes = qh.freeSelect("id, name, wert").getData();
            HashMap<Integer, Integer> taxes = new HashMap<Integer, Integer>();
            for (int i = 0; i < txes.length; i++) {
                Tax t = new Tax();
                t.setCname(txes[i][1].toString());
                t.setTaxvalue(new BigDecimal(txes[i][2].toString()));
                t.save();
                taxes.put(Integer.valueOf(txes[i][0].toString()), t.__getIDS());
            }

            qh.setTable2("warengruppengruppen");
            Object[][] pg = qh.freeSelect("id, name").getData();
            HashMap<Integer, Integer> productg = new HashMap<Integer, Integer>();
            ProductGroup g = new ProductGroup("Import " + dbname.getText() + " " + RandomText.getNumberText());
            g.save();
            for (int i = 0; i < pg.length; i++) {
                ProductGroup h = new ProductGroup(pg[i][1].toString());
                h.setProductgroupsids(g.__getIDS());
                productg.put(Integer.valueOf(pg[i][0].toString()), h.__getIDS());
            }

            qh.setTable2("produkte");
            Object[][] data = qh.freeSelect(TABLE_PRODUCTS_FIELDS).getData();
            for (int i = 0; i < data.length; i++) {
                Product c = new Product();
                c.setCnumber(String.valueOf(data[i][0]));
                c.setCname(String.valueOf(data[i][1]));
                c.setDescription(String.valueOf(data[i][2]));
                c.setExternalnetvalue(new BigDecimal(data[i][3].toString()));
                c.setInternalnetvalue(new BigDecimal(data[i][4].toString()));
                c.setTaxids(taxes.get(Integer.valueOf(String.valueOf(data[i][5]))));
                if (manufacturer.containsKey(Integer.valueOf(String.valueOf(data[i][6])))) {
                    c.setManufacturersids(manufacturer.get(Integer.valueOf(String.valueOf(data[i][6]))));
                }
                if (supplier.containsKey(Integer.valueOf(String.valueOf(data[i][7])))) {
                    c.setSuppliersids(supplier.get(Integer.valueOf(String.valueOf(data[i][7]))));
                }
                if (productg.containsKey(Integer.valueOf(String.valueOf(data[i][8])))) {
                    c.setProductgroupsids(productg.get(Integer.valueOf(String.valueOf(data[i][8]))));
                }
                c.setUrl(String.valueOf(data[i][10]));
                c.setEan(String.valueOf(data[i][11]));
                c.setReference(String.valueOf(data[i][12]));
                c.saveImport();
                imports++;
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
    }
    /**
     * Fields in products table
     */
    public final String TABLE_SERVICES_FIELDS =
            "produktnummer,name ,beschreibung,einheit," +
            "preis , steuersatzid ," +
            "warengruppenid," + "datum";
//       "CREATE TABLE warengruppenkategorien (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), kategorienummer VARCHAR(120),name VARCHAR(500),deleted INTEGER DEFAULT 0,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        "CREATE TABLE warengruppenfamilien (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), familienummer VARCHAR(120), kategorieid BIGINT  REFERENCES warengruppenkategorien(id) ON DELETE CASCADE, name VARCHAR(500),deleted INTEGER DEFAULT 0,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
//        "CREATE TABLE warengruppengruppen (ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), gruppenummer VARCHAR(120),familienid BIGINT  REFERENCES warengruppenfamilien(id) ON DELETE CASCADE, name VARCHAR(500),deleted INTEGER DEFAULT 0,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (id))",
}
