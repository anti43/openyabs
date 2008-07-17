/*
 * customers.java
 *
 * Created on 28. Dezember 2007, 19:17
 */
package mp4.panels;

import mp4.klassen.objekte.RechnungPosten;
import mp4.klassen.objekte.Rechnung;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.CellEditor;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import mp3.classes.interfaces.Strings;
import handling.db.Query;



import mp3.classes.layer.visual.CustomerPicker;
import mp3.classes.layer.visual.DatePick;
import mp3.classes.utils.Formater;
import mp3.classes.utils.Log;
import mp3.classes.layer.Popup;
import mp3.classes.layer.PostenTableModel;
import mp3.classes.layer.visual.ProductPicker;
import mp3.classes.layer.QueryClass;


//import mp3.classes.objects.bill.*;

import mp4.klassen.objekte.Customer;
import mp4.klassen.objekte.History;
import mp4.klassen.objekte.Product;
import mp3.classes.visual.main.mainframe;

import handling.pdf.PDF_Rechnung;
import mp4.klassen.objekte.MyData;
import mp3.classes.visual.util.arrear;
import mp4.utils.datum.DateConverter;


/**
 *
 * @author  anti43
 */
public class billsView extends javax.swing.JPanel implements Runnable ,mp4.datenbank.struktur.Tabellen{

    private Rechnung current;
    private String[][] liste;
    private Customer customer;
    private mainframe mainframe;
   
    private TableCellEditor editor;
    private SimpleDateFormat df;
 
    private Thread t;
    private boolean nettoprices = true;
    private Customer oldcustomer;
    private double defTax = 0d;
    
    private MyData l;
    private boolean pdf = false;
    private int taxcount = 0;

 

    /** Creates new form customers
     * @param aThis 
     */
    @SuppressWarnings("unchecked")
    public billsView(mainframe aThis) {
        l = MyData.instanceOf();
        defTax = Double.valueOf(l.getGlobaltax());
 

        initComponents();
        

        current = new Rechnung(QueryClass.instanceOf());

        current.stripFirst(jTable1);

        updateListTable();
        billsOfTheMonth();
        Formater.format(jTable2, 1, 120);
        Formater.format(jTable2, 2, 120);
        Formater.format(jTable2, 3, 120);

        df = new SimpleDateFormat( "dd.MM.yyyy" );
        
//        df = new SimpleDateFormat( "dd.MM.yyyy" );

        jTextField7.setText(df.format(new Date()));

        this.customer = new Customer(QueryClass.instanceOf());
        this.mainframe = aThis;
        renewTableModel();
        t = new Thread(this);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

        jTextField6.setEnabled(false);   

        billsOfTheMonth();
        
        resizeFields();


    }

    public void addToBill(Product product, boolean ean, boolean name, boolean text) {



        TableModel m = getJTable1().getModel();

        addRow();

        int z = getJTable1().getSelectedRow();

        if (z == -1) {

            z = this.getLastRow();
        }
        int end = 0;

        try {

            try {

                if (product.getText().length() > 60) {
                    end = 60;
                } else {
                    end = product.getText().length();
                }

                String str = "";

                if (name) {
                    str = str + product.getName() + " ";
                }
                if (text) {
                    str = str + product.getText().substring(0, end);
                }
                if (ean) {
                    str = str + " EAN: " + product.getEan();
                }

                m.setValueAt(str, z, 2);

            } catch (Exception exception) {
                Log.Debug(exception);
            }



//            m.setValueAt(product.getHersteller() + " " + product.getName() + " " + product.getEan(), z, 2);//settings?
            m.setValueAt(new Double(1), z, 1);
            try {
                m.setValueAt(new Double(product.getTAX()), z, 3);

            } catch (NumberFormatException numberFormatException) {
                nachricht("Wert 'Steuer' unzulässig: Produkt " + product.getNummer());
                m.setValueAt(l.getGlobaltax(), z, 3);
            }

            try {
                m.setValueAt(new Double(product.getVK()), z, 4);

            } catch (NumberFormatException numberFormatException) {
                nachricht("Wert 'Preis' unzulässig: Produkt " + product.getNummer());
//                
                String p =product.getVK().toString();
                
                m.setValueAt(new Double(p), z, 4);
            }



        } catch (Exception exception) {
           Log.Debug(exception);
        }



    }

    public JTextField getJTextField5() {
        return this.jTextField5;
    }

    private void addRow() {
        Object[] o = new Object[]{null, null, null, null, null};
        DefaultTableModel m = (DefaultTableModel) getJTable1().getModel();
        m.addRow(o);
    }

    private void billsOfTheMonth() {
        
        jTextField2.setText(DateConverter.getTodayDefDate());
        
        String[][] list = getCurrent().select("id, rechnungnummer, datum ", "datum", DateConverter.getTodayDefDate(), "datum", true);
        String k = "id, " + "Nummer,Datum";

        this.jTable3.setModel(new DefaultTableModel(list, k.split(",")));
        getCurrent().stripFirst(jTable3);
    }

    private boolean hasCurrent() {
        if (getCurrent().hasId()) {
            return true;
        } else {
            return false;
        }
    }

    private void nachricht(String string) {
        mainframe.getNachricht().setText(string);
    }

    private void prepareTable() {
//        TableModel m = jTable1.getModel();
//        for (int k = 0; k < m.getRowCount(); k++) {
//            //anzahl,bezeichnung,mehrwertsteuer,nettopreis
//
//            String z = (String) m.getValueAt(k, 4);
//            if (z != null && z.contains(",")) {
//                z = z.replaceAll(",", ".");
//                m.setValueAt(z, k, 4);
//            }
//        }
    }

    private void renewTableModel() {
        PostenTableModel f = 
                new PostenTableModel(new Object[][]{{null, 1, null, defTax, null, null}, 
                {null, 1, null, defTax, null, null}, {null, 1, null, defTax, null, null}, 
                {null, 1, null, defTax, null, null}, {null, 1, null, defTax, null, null}, 
                {null, 1, null, defTax, null, null}, {null, 1, null, defTax, null, null}, 
                {null, 1, null, defTax, null, null}, {null, 1, null, defTax, null, null}, 
                {null, 1, null, defTax, null, null}}, 
                new String[]{"id", "Anzahl", "Bezeichnung", "Mehrwertsteuer", "Nettopreis", "Bruttopreis"});
        
        getJTable1().setModel(f);
        
        Formater.stripFirst(jTable1);
        
        
    }

    public void resizeFields() {

        try {
            getJTable1().getColumn(getJTable1().getColumnName(2)).setPreferredWidth(350);
            getJTable1().getColumn(getJTable1().getColumnName(1)).setPreferredWidth(30);
            getJTable1().getColumn(getJTable1().getColumnName(3)).setPreferredWidth(30);

            getJTable1().getColumn(getJTable1().getColumnName(1)).setMinWidth(30);
            getJTable1().getColumn(getJTable1().getColumnName(3)).setMinWidth(30);

        } catch (Exception exception) {
        }

    }

    public void updateListTable() {


        try {
            liste = current.getWithDepencies("rechnungen.id,rechnungnummer,datum,kundennummer,firma, bezahlt, storno");

            Object[][] lister = Rechnung.formatTableArray(liste, new int[]{5, 6}, 7);

            String k = "id,Nummer,Datum,Kunde,Firma, Bezahlt, Storniert, Verzug";

            Formater.reverseArray(liste);


            this.jTable2.setModel(new DefaultTableModel(lister, k.split(",")));
            getCurrent().stripFirst(jTable2);

            Formater.format(jTable2, 1, 120);
            Formater.format(jTable2, 2, 120);
            Formater.format(jTable2, 3, 120);

        } catch (Exception exception) {
           
        }

    }

    public void setBill(Rechnung current) {

        this.current = current;
        this.setCustomer(new Customer(current.getKundenId()));


        this.jTextField6.setText(current.getRechnungnummer());
        jTextField6.setBackground(Color.WHITE);
        this.jTextField7.setText(DateConverter.getDefDateString(current.getDatum()));

//        if (current.isBezahlt()) {
//            this.jLabelbezahlt.setText("Bezahlt");
//             jTextField6.setBackground(Color.GREEN);
//        } else {
//            
//            this.jLabelbezahlt.setText("Unbezahlt");
//            
//             jTextField6.setBackground(Color.WHITE);
//        }
//
//        if (current.isStorno()) {
//            this.jlabelstorno.setText("Storniert");
//        } else {
//            
//            if(current.isVerzug()) {
//                this.jlabelstorno.setText("Verzug!");
//                jTextField6.setBackground(Color.RED);
//            }
//            else {
//                this.jlabelstorno.setText("");
//                
//            }
//        }

        
        
        
        this.getJTable1().setModel(current.getProductlistAsTableModel());
        current.stripFirst(getJTable1());
        resizeFields();

    }

    public void setCustomer(Customer c) {

        oldcustomer = this.customer;
        this.customer = c;

        getJTextField4().setText(c.getKundennummer());
        jTextField5.setText(c.getFirma());

//        mainframe.nachricht("Aktuell: Kunde Nummer " + c.getKundennummer());

//        if (getCustomer().isDeleted()) {
//            isdeleted.setText("Inaktiv");
//            jTextField5.setBackground(Color.RED);
//        } else {
//
//            isdeleted.setText("");
//            jTextField5.setBackground(Color.WHITE);
//
//        }

    }

    private boolean hasCustomer() {
        int i = JOptionPane.OK_OPTION;
        if (getCustomer() != null) {

//            if (oldcustomer != null && !oldcustomer.getid().equals("0") && !oldcustomer.getid().equals(getCustomer().getid())) {
//
//                i = JOptionPane.showConfirmDialog(null, "Wollen Sie wirklich der Rechnung einen neuen Kunden zuordnen?", "Konflikt", JOptionPane.WARNING_MESSAGE);
//            }

            if (getCustomer().getId() != null && !customer.getId().equals("0") && !customer.isDeleted()) {

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel14 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable()

        {
            public boolean isCellEditable(int x, int y) {
                return false;
            }
        }
        ;
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTextField8 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton16 = new javax.swing.JButton();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jTextField10 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        jButton17 = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jTextField12 = new javax.swing.JTextField();
        jButton18 = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jTextField13 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jToolBar1 = new javax.swing.JToolBar();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton8 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jButton12 = new javax.swing.JButton();
        jCheckBox4 = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable()
        {
            public boolean isCellEditable(int x, int y) {
                return false;
            }
        }

        ;

        jLabel14.setText("jLabel14");

        setBackground(new java.awt.Color(204, 204, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 204));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 1, 14));
        jLabel1.setText("Rechnungen");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(733, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
        );

        jTabbedPane1.setBackground(new java.awt.Color(255, 255, 255));
        jTabbedPane1.setOpaque(true);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Datensätze"));

        jScrollPane5.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane5.setAutoscrolls(true);

        jTable3.setAutoCreateRowSorter(true);
        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null}
            },
            new String [] {
                "Nummer", "Datum"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable3.setRequestFocusEnabled(false);
        jTable3.setUpdateSelectionOnSort(false);
        jTable3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable3MouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(jTable3);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Suche"));
        jPanel4.setMaximumSize(new java.awt.Dimension(240, 32767));

        jLabel2.setText("Nummer:");

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jLabel3.setText("Datum:");

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        jLabel4.setText("Firma:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField3)
                    .addComponent(jTextField2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(46, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Details"));

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel5.setText("Kunde");

        jLabel6.setText("Nummer");

        jLabel7.setText("Firma");

        jTextField4.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextField4.setEnabled(false);
        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jTextField5.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextField5.setEnabled(false);
        jTextField5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField5ActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel8.setText("Rechnung");

        jLabel9.setText("Nummer");

        jLabel10.setText("Datum");

        jTextField6.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextField6.setEnabled(false);
        jTextField6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField6MouseClicked(evt);
            }
        });

        jTextField7.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextField7.setEnabled(false);

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setModel( new DefaultTableModel()
        );
        jTable1.setCellSelectionEnabled(true);
        jTable1.setDragEnabled(true);
        jTable1.setFillsViewportHeight(true);
        jTable1.setSurrendersFocusOnKeystroke(true);
        jScrollPane1.setViewportView(jTable1);

        jLabel11.setText("Gesamtbruttopreis");

        jLabel12.setText("Steuer");

        jButton9.setText("Wählen...");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setText("Kalender");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });
        jButton10.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jButton10KeyPressed(evt);
            }
        });

        jCheckBox1.setSelected(true);
        jCheckBox1.setText("Nettopreise");
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox1ItemStateChanged(evt);
            }
        });

        jButton16.setText("Bearbeiten");

        jCheckBox2.setText("Bezahlt");

        jCheckBox3.setText("Storniert");

        jTextField10.setText("0");

        jLabel13.setText("Mahnungen");

        jLabel15.setText("Ausführung");

        jTextField11.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextField11.setEnabled(false);

        jButton17.setText("Kalender");
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });
        jButton17.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jButton17KeyPressed(evt);
            }
        });

        jLabel16.setText("Betreff");

        jTextField12.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextField12.setEnabled(false);
        jTextField12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField12ActionPerformed(evt);
            }
        });

        jButton18.setText("Bearbeiten");

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setText("Angebot");

        jLabel18.setText("Datum");

        jTextField13.setDisabledTextColor(new java.awt.Color(0, 51, 51));
        jTextField13.setEnabled(false);

        jButton1.setText("Kalender");

        jButton2.setText("Verlauf");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel8)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel10))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField7, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                                    .addComponent(jTextField6)))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jTextField4)
                                    .addComponent(jTextField5, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel16)
                                    .addComponent(jLabel18))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jTextField13)
                                    .addComponent(jTextField12, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                                    .addComponent(jTextField11, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                                    .addComponent(jButton2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jCheckBox2)
                                .addGap(18, 18, 18)
                                .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 170, Short.MAX_VALUE)
                                .addComponent(jCheckBox1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel12))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jCheckBox3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 306, Short.MAX_VALUE)
                                .addComponent(jLabel11)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jTextField8)
                            .addComponent(jTextField9, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9)
                    .addComponent(jLabel18)
                    .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel9)
                        .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton16)
                        .addComponent(jLabel16)
                        .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton18)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton10)
                    .addComponent(jLabel15)
                    .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox1)
                    .addComponent(jCheckBox2)
                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel11)
                        .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jCheckBox3)
                        .addContainerGap())))
        );

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jButton4.setText("Speichern");
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton4MouseClicked(evt);
            }
        });
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton4);

        jButton3.setText("Als neue Rechnung anlegen");
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
        });
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton3);
        jToolBar1.add(jSeparator1);

        jButton8.setText("Zeile hinzu");
        jButton8.setFocusable(false);
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton8MouseClicked(evt);
            }
        });
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton8);

        jButton11.setText("Zeile löschen");
        jButton11.setFocusable(false);
        jButton11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton11.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton11MouseClicked(evt);
            }
        });
        jToolBar1.add(jButton11);

        jButton5.setText("Produkt hinzu");
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton5MouseClicked(evt);
            }
        });
        jToolBar1.add(jButton5);

        jButton7.setText("AC");
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton7);
        jToolBar1.add(jSeparator2);

        jButton13.setText("Storno");
        jButton13.setFocusable(false);
        jButton13.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton13.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton13MouseClicked(evt);
            }
        });
        jToolBar1.add(jButton13);

        jButton14.setText("Bezahlt");
        jButton14.setFocusable(false);
        jButton14.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton14.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton14.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton14MouseClicked(evt);
            }
        });
        jToolBar1.add(jButton14);

        jButton15.setText("Mahnung");
        jButton15.setFocusable(false);
        jButton15.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton15.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton15);
        jToolBar1.add(jSeparator3);

        jButton12.setText("Drucken");
        jButton12.setFocusable(false);
        jButton12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton12.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton12MouseClicked(evt);
            }
        });
        jToolBar1.add(jButton12);

        jCheckBox4.setText("mit Lieferschein");
        jCheckBox4.setFocusable(false);
        jCheckBox4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jCheckBox4.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jCheckBox4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jCheckBox4);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 855, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        jTabbedPane1.addTab("Allgemein", jPanel2);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPane4.setAutoscrolls(true);

        jTable2.setAutoCreateRowSorter(true);
        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nummer", "Datum", "Firma", "Ort"
            }
        ));
        jTable2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable2MouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(jTable2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 855, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 543, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Liste", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 860, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jTabbedPane1))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed

        String[][] list = getCurrent().select("id, rechnungnummer, datum ", "rechnungnummer", jTextField1.getText(), "rechnungnummer", true);
        String k = "id, " + "Nummer,Datum";

        this.jTable3.setModel(new DefaultTableModel(list, k.split(",")));
        getCurrent().stripFirst(jTable3);
//
//        this.jtable3Header = k;
//        this.jtable3Data = current.select("id, kundennummer, firma ", "kundennummer", jTextField1.getText(), "kundennummer", true);
//        

    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField2ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed


        String[][] list = getCurrent().select("id, rechnungnummer, datum ", "datum", jTextField2.getText(), "datum", true);
        String k = "id, " + "Nummer,Datum";

        this.jTable3.setModel(new DefaultTableModel(list, k.split(",")));
        getCurrent().stripFirst(jTable3);
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField3ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed

        String[] str = getCustomer().selectLast("id", "firma", jTextField3.getText(), false, false, true);

        if (str != null) {

            String[][] list = getCurrent().select("id, rechnungnummer, datum ", "kundenid", str[0], true);
            String k = "id, " + "Nummer,Datum";

            this.jTable3.setModel(new DefaultTableModel(list, k.split(",")));
            getCurrent().stripFirst(jTable3);
        }
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jButton3MouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked

        editor = getJTable1().getCellEditor();
        String rechnungnummer = "0";

        if (hasCustomer() && validDate()) {
            TableModel m = getJTable1().getModel();
            ListSelectionModel selectionModel = getJTable1().getSelectionModel();
            boolean valide = true;

            if (editor != null) {
                editor.stopCellEditing();
            }

            if (valide) {

                Query f = QueryClass.instanceOf().clone(TABLE_BILLS);
               
                if(jTextField6.getText().length()<1 || jCheckBox2.isSelected()) {
                    rechnungnummer = f.getNextIndexString("rechnungnummer");
                }else{
      
                    rechnungnummer = jTextField6.getText();
                }
              
                Rechnung bill = new Rechnung(QueryClass.instanceOf());

                bill.setRechnungnummer(rechnungnummer);

//                bill.setDatum(DateCongh));
                bill.setKundenId(getCustomer().getId());
                
                
   // **********************EUR**********************************************************
                Double betrag = 0d;
                Double allovertax = 0d;
                Double nettobetrag = 0d;
                
                for (int i = 0; i < m.getRowCount(); i++) {

                    //anzahl,bezeichnung,mehrwertsteuer,nettopreis
                    if (m.getValueAt(i, 4) != null) {

                        try {

                           
                            betrag = betrag + (Double.valueOf(m.getValueAt(i, 4).toString().replaceAll(",", ".")) * (Double.valueOf((Double.valueOf(m.getValueAt(i, 3).toString().replaceAll(",", "."))/100)+1)));
                            betrag = Formater.formatDecimalDouble(betrag);
                            
                            nettobetrag = nettobetrag + (Double.valueOf(m.getValueAt(i, 4).toString().replaceAll(",", "."))) ;
                            betrag = Formater.formatDecimalDouble(betrag);
                            
                            allovertax = allovertax + ((Double.valueOf(m.getValueAt(i, 3).toString().replaceAll(",", ".")))+100);
                         
                        } catch (Exception exception) {
                            Log.Debug(exception); 
                        }
                    }
                }
               
//                bill.setGesamtpreis(betrag.toString());
                  bill.setGesamtpreis(Double.valueOf(jTextField8.getText()));
                if(nettobetrag>0) {
                     bill.setGesamttax((allovertax / nettobetrag));
                }
                else{bill.setGesamttax(0d);
                }
  //***************************************************************************************
                
                
                bill.save();
               
                this.clear();

                for (int i = 0; i < m.getRowCount(); i++) {

                    //anzahl,bezeichnung,mehrwertsteuer,nettopreis
                    if (m.getValueAt(i, 4) != null) {

                        RechnungPosten b = new RechnungPosten(QueryClass.instanceOf());

                        b.setRechnungid(bill.getId());
                        b.setPosten((String) m.getValueAt(i, 2));

                        try {

                            b.setAnzahl(      (Double) m.getValueAt(i, 1));
                            b.setSteuersatz((Double) m.getValueAt(i, 3));
                            b.setPreis((Double) m.getValueAt(i, 4));
                      
                        } catch (Exception exception) {
//                            b.setAnzahl("0");
//                            b.setSteuersatz("0");
//                            b.setPreis("0");
                        }

                        b.save();

                    }
                }
          

                jTextField6.setText(String.valueOf(f.getNextIndex("rechnungnummer")));
                mainframe.nachricht("Rechnung Nummer: " + bill.getRechnungnummer() + " angelegt.");

                new History(QueryClass.instanceOf(),  Strings.BILL, "Rechnung Nummer: " + bill.getRechnungnummer() + " angelegt.");
                this.setBill(new Rechnung(bill.getId()));

                save();
            }

        } else {

            new Popup("Sie müssen einen (validen) Kunden auswählen.", Popup.ERROR);
        }
        updateListTable();
        billsOfTheMonth();
        resizeFields();
        
        jCheckBox2.setSelected(true);

       

    }//GEN-LAST:event_jButton3MouseClicked

    private void jTable2MouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked

        boolean idOk = true;
        Integer id = 0;


        try {
            id = Integer.valueOf((String) jTable2.getValueAt(jTable2.getSelectedRow(), 0));
        } catch (Exception numberFormatException) {
            idOk = false;
        }



        if (evt.getClickCount() >= 2 && idOk && evt.getButton() == MouseEvent.BUTTON1) {

            try {
                this.setBill(new Rechnung(id));
                jTabbedPane1.setSelectedIndex(0);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else if (idOk && (evt.getButton() == MouseEvent.BUTTON2 || evt.getButton() == MouseEvent.BUTTON3)) {


        }


        idOk = true;
        
        
    }//GEN-LAST:event_jTable2MouseClicked

    public void save() {
      

        if (hasCustomer() && validDate()) {

            if (hasCurrent()) {

                TableModel m = getJTable1().getModel();
                ListSelectionModel selectionModel = getJTable1().getSelectionModel();
                boolean valide = true;

                editor = getJTable1().getCellEditor();
                if (editor != null) {
                    editor.stopCellEditing();
                }


                if (valide) {

                    Query f = QueryClass.instanceOf().clone( TABLE_BILLS);

                    //Integer rechnungnummer = f.getNextIndex("rechnungnummer");

                    Rechnung bill = getCurrent();

//                    bill.setRechnungnummer(rechnungnummer.toString());

                    bill.setDatum(DateConverter.getDate(jTextField7.getText()));


                    bill.setKundenId(getCustomer().getId());
                    
                    bill.setGesamtpreis(Double.valueOf(jTextField8.getText()));
                    
                                    
   // **********************EUR**********************************************************
                Double betrag = 0d;
                Double allovertax = 0d;
                Double nettobetrag = 0d;
                
                for (int i = 0; i < m.getRowCount(); i++) {

                    //anzahl,bezeichnung,mehrwertsteuer,nettopreis
                    if (m.getValueAt(i, 4) != null) {

                     try {

                           
                            betrag = betrag + (Double.valueOf(m.getValueAt(i, 4).toString().replaceAll(",", ".")) * (Double.valueOf((Double.valueOf(m.getValueAt(i, 3).toString().replaceAll(",", "."))/100)+1)));
                            betrag = Formater.formatDecimalDouble(betrag);
                            
                            nettobetrag = nettobetrag + (Double.valueOf(m.getValueAt(i, 4).toString().replaceAll(",", "."))) ;
                            betrag = Formater.formatDecimalDouble(betrag);
                            
                            taxcount = taxcount +100;
                            
                            allovertax = allovertax + ((Double.valueOf(m.getValueAt(i, 3).toString().replaceAll(",", ".")))+100);
                        Log.Debug(allovertax);
                        } catch (Exception exception) {
                            Log.Debug(exception); 
                        }
                    }
                }
              Log.Debug("val = "+allovertax + " / " + taxcount);
//                bill.setGesamtpreis(betrag.toString());
                bill.setGesamttax((allovertax/taxcount));
                taxcount = 0;
  //***************************************************************************************

                    bill.save();
                    //  this.clear();



                    for (int i = 0; i < m.getRowCount(); i++) {

                        //delete first
                        if (m.getValueAt(i, 0) != null) {



                            try {
                                RechnungPosten b = new RechnungPosten(QueryClass.instanceOf(), m.getValueAt(i, 0).toString());

                                b.destroy();

                            } catch (Exception exception) {
                            }

                        }
                    }


                    for (int i = 0; i < m.getRowCount(); i++) {

                        //anzahl,bezeichnung,mehrwertsteuer,nettopreis
                        if (m.getValueAt(i, 4) != null) {



                            RechnungPosten b = new RechnungPosten(QueryClass.instanceOf());

                            b.setRechnungid(getCurrent().getId());

//                            b.setAnzahl((String) m.getValueAt(i, 1));
                            b.setPosten((String) m.getValueAt(i, 2));
                            try {

                                b.setAnzahl((Double) (m.getValueAt(i, 1)));
                                b.setSteuersatz((Double) (m.getValueAt(i, 3)));
                                b.setPreis((Double) (m.getValueAt(i, 4)));

                            } catch (Exception exception) {
//                                b.setAnzahl("0");
//                                b.setSteuersatz("0");
//                                b.setPreis("0");
                            }
                            b.save();

                        }
                    }

                    mainframe.getNachricht().setText("Rechnung Nummer " + bill.getRechnungnummer() + " gespeichert.");

                    new History(QueryClass.instanceOf(), Strings.BILL, "Rechnung Nummer: " + bill.getRechnungnummer() + " editiert.");

                    this.setBill(new Rechnung(bill.getid()));

                }
            } else {
                new Popup("Sie müssen die Rechnung erst anlegen.", Popup.ERROR);
//                jButton3MouseClicked(new MouseEvent(jTable1, WIDTH, WIDTH, WIDTH, WIDTH, WIDTH, WIDTH, nettoprices));

            }





        } else {

            new Popup("Sie müssen einen Kunden auswählen.", Popup.ERROR);
        }

        resizeFields();



    }

    private void jButton4MouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseClicked

        save();
        
    }//GEN-LAST:event_jButton4MouseClicked

    private void jTable3MouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable3MouseClicked

        boolean idOk = true;
        Integer id = 0;

        try {
            id = Integer.valueOf((String) jTable3.getValueAt(jTable3.getSelectedRow(), 0));
        } catch (Exception numberFormatException) {
            idOk = false;
        }


        if (evt.getClickCount() >= 2 && idOk) {

            try {
                this.setBill(new Rechnung(id));

            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        idOk = true;
        
    }//GEN-LAST:event_jTable3MouseClicked

    private void clear() {


//        jTextField6.setText(current.getNextBillNumber().toString());

        this.customer = new Customer(QueryClass.instanceOf());
        this.current = new Rechnung(QueryClass.instanceOf());

        jTextField4.setText("");
        jTextField5.setText("");
        jTextField6.setText("");
        jTextField6.setBackground(Color.WHITE);


        renewTableModel();
//        jlabelstorno.setText("");
//        jLabelbezahlt.setText("");

//        df = new SimpleDateFormat( "dd.MM.yyyy" );
        df = new SimpleDateFormat( "dd.MM.yyyy" );
        jTextField7.setText(df.format(new Date()));

        getCurrent().stripFirst(getJTable1());
        resizeFields();
    }

    private void jButton7ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed

        this.clear();

//        TableModel m = getJTable1().getModel();
//
//        for (int i = 0; i < getJTable1().getRowCount(); i++) {
//            if (m.getValueAt(i, 0) != null) {
//
//
//
//                BillProduct b = new BillProduct(QueryClass.instanceOf(), m.getValueAt(i, 0).toString());
//
//                b.destroy();
//
//
//
//            }
//        }
//
//
//        getJTable1().setModel(renewTableModel());
//        getCurrent().stripFirst(getJTable1());
//
//        resizeFields();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jTextField4ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed


        String[] st = getCustomer().selectLast( Strings.ALL, "kundennummer", jTextField4.getText(), false, false, false);

        this.clear();
        jTextField5.setText("");
        jTextField6.setText("");
        jTextField7.setText(df.format(new Date()));


        try {
            this.setCustomer(new Customer(Integer.valueOf(st[0])));


        } catch (Exception exception) {

            try {

                this.setCustomer(new Customer(QueryClass.instanceOf(), jTextField4.getText(), true));

            } catch (Exception exception1) {
                //    exception1.printStackTrace();
                mainframe.nachricht("Kein Datensatz gefunden!");
            }

        }

    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jTextField5ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed



        String[] st = getCustomer().selectLast( Strings.ALL, "firma", jTextField5.getText(), false, false, false);

        this.clear();
        jTextField4.setText("");
        jTextField6.setText("");
        jTextField7.setText(df.format(new Date()));
        String[][] sta;


        try {
            this.setCustomer(new Customer(Integer.valueOf(st[0])));

        } catch (Exception exception) {

            try {

                this.setCustomer(new Customer(QueryClass.instanceOf(), jTextField4.getText(), true));

            } catch (Exception exception1) {
                //    exception1.printStackTrace();
                mainframe.nachricht("Kein Datensatz gefunden k");
            }
        }

    }//GEN-LAST:event_jTextField5ActionPerformed

    private void jButton8MouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton8MouseClicked

        addRow();
    }//GEN-LAST:event_jButton8MouseClicked

    private void jButton9ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed


        new CustomerPicker(this);
    
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed


        new DatePick(jTextField7);
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jCheckBox1ItemStateChanged (java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged

        if (jCheckBox1.isSelected()) {

            nettoprices = true;

        } else {

            nettoprices = false;

        }
    }//GEN-LAST:event_jCheckBox1ItemStateChanged

    private void jButton3ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
//       
//        jButton3MouseClicked(new MouseEvent(jTable1, WIDTH, WIDTH, WIDTH, WIDTH, WIDTH, WIDTH, nettoprices));
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton11MouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton11MouseClicked



        TableModel m = getJTable1().getModel();
//        ListSelectionModel selectionModel = jTable1.getSelectionModel();

        try {
            if (m.getValueAt(getJTable1().getSelectedRow(), 0) != null) {



                RechnungPosten b = new RechnungPosten(QueryClass.instanceOf(), m.getValueAt(getJTable1().getSelectedRow(), 0).toString());

                b.destroy();


                for (int i = 0; i < getJTable1().getColumnCount(); i++) {
                    m.setValueAt(null, getJTable1().getSelectedRow(), i);
                }
            } else {


                for (int i = 0; i < getJTable1().getColumnCount(); i++) {
                    m.setValueAt(null, getJTable1().getSelectedRow(), i);
                }

            }

        } catch (Exception e) {
//            e.printStackTrace();
        }


    }//GEN-LAST:event_jButton11MouseClicked

    private void jButton10KeyPressed (java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jButton10KeyPressed

//        jButton1MouseClicked(new MouseEvent(jTable1, WIDTH, WIDTH, WIDTH, WIDTH, WIDTH, WIDTH, nettoprices));
    }//GEN-LAST:event_jButton10KeyPressed

    private void jButton5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton5MouseClicked
        new ProductPicker(this);
//        TableModel m;

//        if (getJTable1().getSelectedRow() == -1) {
//            m = getJTable1().getModel();
//            ListSelectionModel selectionModel = getJTable1().getSelectionModel();
//            selectionModel.setSelectionInterval(getLastRow(),
//                    getLastRow());
//        }
        
    }//GEN-LAST:event_jButton5MouseClicked

    private void jButton12MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton12MouseClicked
        jButton4MouseClicked(evt);

        if (current != null && current.hasId()) {
            new PDF_Rechnung(current);

            new History(QueryClass.instanceOf(),  Strings.BILL, "Rechnung Nummer: " + current.getRechnungnummer() + " als PDF erzeugt.");

        }
    }//GEN-LAST:event_jButton12MouseClicked

    private void jButton13MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton13MouseClicked

        this.current.setStorno(true);
//        this.jlabelstorno.setText("Storniert");

        this.jButton4MouseClicked(evt);
        
    }//GEN-LAST:event_jButton13MouseClicked

    private void jButton14MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton14MouseClicked

        this.current.setBezahlt(true);
//        this.jLabelbezahlt.setText("Bezahlt");

        this.jButton4MouseClicked(evt);
    }//GEN-LAST:event_jButton14MouseClicked

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        if(current.hasId()) {
            new arrear(current, customer);
        }
    }//GEN-LAST:event_jButton15ActionPerformed

    private void jTextField6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField6MouseClicked
       jCheckBox2.setSelected(false);
       jTextField6.setEnabled(true);        
    }//GEN-LAST:event_jTextField6MouseClicked

private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButton8ActionPerformed

private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButton4ActionPerformed

private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButton17ActionPerformed

private void jButton17KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jButton17KeyPressed
// TODO add your handling code here:
}//GEN-LAST:event_jButton17KeyPressed

private void jTextField12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField12ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jTextField12ActionPerformed

    private int getLastRow() {
        int potz = -1;
        int i;

        for (i = 0; i < getJTable1().getRowCount(); i++) {
            if (getJTable1().getValueAt(i, 2) == null || getJTable1().getValueAt(i, 2).equals("")) {
                potz = i;
                break;
            }

        }

        if (potz == -1) {

            Object[][] o = new Object[][]{null, null, null, null, null};
            DefaultTableModel m = (DefaultTableModel) getJTable1().getModel();
            m.addRow(o);

            potz = i + 1;
        }

        return potz;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton jButton1;
    public javax.swing.JButton jButton10;
    public javax.swing.JButton jButton11;
    public javax.swing.JButton jButton12;
    public javax.swing.JButton jButton13;
    public javax.swing.JButton jButton14;
    public javax.swing.JButton jButton15;
    public javax.swing.JButton jButton16;
    public javax.swing.JButton jButton17;
    public javax.swing.JButton jButton18;
    public javax.swing.JButton jButton2;
    public javax.swing.JButton jButton3;
    public javax.swing.JButton jButton4;
    public javax.swing.JButton jButton5;
    public javax.swing.JButton jButton7;
    public javax.swing.JButton jButton8;
    public javax.swing.JButton jButton9;
    public javax.swing.JCheckBox jCheckBox1;
    public javax.swing.JCheckBox jCheckBox2;
    public javax.swing.JCheckBox jCheckBox3;
    public javax.swing.JCheckBox jCheckBox4;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JLabel jLabel10;
    public javax.swing.JLabel jLabel11;
    public javax.swing.JLabel jLabel12;
    public javax.swing.JLabel jLabel13;
    public javax.swing.JLabel jLabel14;
    public javax.swing.JLabel jLabel15;
    public javax.swing.JLabel jLabel16;
    public javax.swing.JLabel jLabel17;
    public javax.swing.JLabel jLabel18;
    public javax.swing.JLabel jLabel2;
    public javax.swing.JLabel jLabel3;
    public javax.swing.JLabel jLabel4;
    public javax.swing.JLabel jLabel5;
    public javax.swing.JLabel jLabel6;
    public javax.swing.JLabel jLabel7;
    public javax.swing.JLabel jLabel8;
    public javax.swing.JLabel jLabel9;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JPanel jPanel2;
    public javax.swing.JPanel jPanel3;
    public javax.swing.JPanel jPanel4;
    public javax.swing.JPanel jPanel5;
    public javax.swing.JPanel jPanel6;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JScrollPane jScrollPane4;
    public javax.swing.JScrollPane jScrollPane5;
    public javax.swing.JToolBar.Separator jSeparator1;
    public javax.swing.JToolBar.Separator jSeparator2;
    public javax.swing.JToolBar.Separator jSeparator3;
    public javax.swing.JTabbedPane jTabbedPane1;
    public javax.swing.JTable jTable1;
    public javax.swing.JTable jTable2;
    public javax.swing.JTable jTable3;
    public javax.swing.JTextField jTextField1;
    public javax.swing.JTextField jTextField10;
    public javax.swing.JTextField jTextField11;
    public javax.swing.JTextField jTextField12;
    public javax.swing.JTextField jTextField13;
    public javax.swing.JTextField jTextField2;
    public javax.swing.JTextField jTextField3;
    public javax.swing.JTextField jTextField4;
    public javax.swing.JTextField jTextField5;
    public javax.swing.JTextField jTextField6;
    public javax.swing.JTextField jTextField7;
    public javax.swing.JTextField jTextField8;
    public javax.swing.JTextField jTextField9;
    public javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables
    private boolean validDate() {
        if (Formater.check(jTextField7.getText(), Formater.DATE)) {
            return true;
        } else {

            new Popup("Sie müssen ein Datum angeben (tt/mm/yyyy)", Popup.ERROR);
            return false;
        }
    }

    public Rechnung getCurrent() {
        if (current != null) {
            return current;
        } else {
            return new Rechnung(QueryClass.instanceOf());
        }
    }

    public Customer getCustomer() {
        return customer;
    }

    public javax.swing.JTextField getJTextField4() {
        return jTextField4;
    }

    public javax.swing.JTable getJTable1() {
        return jTable1;
    }

    /**
     * "id", "Anzahl", "Bezeichnung", "Mehrwertsteuer", "Nettopreis", "Bruttopreis"
     */
//        Class[] types = new Class [] {
//                java.lang.Integer.class,  java.lang.Double.class, 
//                java.lang.String.class, java.lang.Double.class, 
//                java.lang.Double.class,java.lang.Double.class
//            };
//
//        @Override
//            public Class getColumnClass(int columnIndex) {
//                return types [columnIndex];
//            }
    public void run() {

        TableColumn column;
        CellEditor cell;


        while (true) {  
            
            try{
                Thread.sleep(1000);
                }catch (Exception e){}
            while (mainframe.getShowingTab() == 1) {

             
                try{
                Thread.sleep(800);
                }catch (Exception e){}
                
                TableModel m = getJTable1().getModel();

//            Log.Debug(getJTable1().getModel().getColumnClass(1));
//            Log.Debug(getJTable1().getModel().getColumnClass(2));
//            Log.Debug(getJTable1().getModel().getColumnClass(3));

                Double tax = 0d;
                Double itax = 0d;

                Double netto = 0d;
                Double brutto = 0d;
                Double curnetto = 0d;
                Double curbrutto = 0d;
                Double curnettoe = 0d;
                Double curbruttoe = 0d;

                //anzahl,bezeichnung,mehrwertsteuer,nettopreis



                try {


                    if (!jTable1.isEditing()) {

                        for (int k = 0; k < m.getRowCount(); k++) {
                            //anzahl,bezeichnung,mehrwertsteuer,nettopreis

                            if ((m.getValueAt(k, 2)) != null) {
                                if ((m.getValueAt(k, 2)).toString().equals("null")) {
                                    m.setValueAt("", k, 2);
                                }
                            }

                            if ((m.getValueAt(k, 1)) == null) {

                                m.setValueAt(new Double(1), k, 1);

                            }

                            if ((m.getValueAt(k, 3)) == null) {

                                m.setValueAt(new Double(defTax), k, 3);

                            }


                            try {
                                if (m.getValueAt(k, 4) != null && nettoprices) {

                                    tax = Double.valueOf((m.getValueAt(k, 3)).toString());

                                    itax = (tax / 100) + 1;

                                    curnetto = Double.valueOf((m.getValueAt(k, 1)).toString().replaceFirst(",", ".")) *
                                            Double.valueOf((m.getValueAt(k, 4)).toString().replaceFirst(",", "."));
                                    curnettoe = Double.valueOf((m.getValueAt(k, 4)).toString().replaceFirst(",", "."));

                                    netto = netto + curnetto;
                                    curbruttoe = curnettoe * itax;

                                    curbrutto = curnetto * itax;
                                    brutto = brutto + curbrutto;

                                    m.setValueAt(curbruttoe, k, 5);

                                } else if (m.getValueAt(k, 5) != null && !nettoprices) {

                                    tax = Double.valueOf((m.getValueAt(k, 3)).toString().replaceFirst(",", "."));

                                    itax = (tax / 100) + 1;

                                    curbrutto = Double.valueOf((m.getValueAt(k, 5)).toString().replaceFirst(",", "."));

                                    curnetto = curbrutto / itax;

                                    netto = netto + Double.valueOf((m.getValueAt(k, 1)).toString().replaceFirst(",", ".")) * curnetto;

                                    brutto = brutto + Double.valueOf((m.getValueAt(k, 1)).toString().replaceFirst(",", ".")) * curbrutto;

                                    m.setValueAt(curnetto, k, 4);

                                }
                            } catch (Exception e) {
                            }
                        }

                        jTextField9.setText(Formater.formatDecimal(brutto - netto));//!tax
                        jTextField8.setText(Formater.formatDecimal(brutto));//!tax
                    }

                   
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
               
            }
        }
    }
    }




    



