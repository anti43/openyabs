/*
 * customers.java
 *
 * Created on 28. Dezember 2007, 19:17
 */
package mp4.panels.rechnungen;

import java.awt.Color;
import java.awt.event.MouseEvent;
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
import mp4.datenbank.verbindung.Query;



import mp3.classes.layer.visual.CustomerPicker;
import mp3.classes.layer.visual.DatePick;
import mp3.classes.utils.Formater;
import mp3.classes.utils.Log;
import mp3.classes.layer.Popup;
import mp4.utils.tabellen.models.PostenTableModel;
import mp3.classes.layer.visual.ProductPicker;
import mp4.datenbank.verbindung.ConnectionHandler;



import mp4.klassen.objekte.Rechnung;
import mp4.klassen.objekte.RechnungPosten;
import mp4.klassen.objekte.Customer;
import mp4.klassen.objekte.HistoryItem;
import mp4.einstellungen.Einstellungen;
import mp4.klassen.objekte.Angebot;
import handling.pdf.PDF_Angebot;
import mp4.klassen.objekte.AngebotPosten;
import mp4.klassen.objekte.Product;
import mp3.classes.visual.main.mainframe;
import mp4.utils.datum.DateConverter;
import mp4.utils.tabellen.SelectionCheck;

/**
 *
 * @author  anti43
 */
public class offersView extends javax.swing.JPanel implements Runnable, mp4.datenbank.struktur.Tabellen {

    private Angebot current;
    private String[][] liste;
    private Customer customer;
    private mainframe mainframe;
    private DefaultTableModel jtable1Model = new DefaultTableModel();
    private TableCellEditor editor;
    private SimpleDateFormat df;
    private String jtable3Header;
    private String[][] jtable3Data;
    private Thread t;
    private boolean nettoprices = true;
    private Customer oldcustomer;
    private double defTax = 19d;
    private Einstellungen l;

    @SuppressWarnings("unchecked")
    public offersView(mainframe aThis) {
        l = Einstellungen.instanceOf();
        defTax = l.getGlobaltax();

        initComponents();

        current = new Angebot();
        current.stripFirst(jTable1);
        this.updateListTable();

        Formater.format(jTable2, 1, 120);
        Formater.format(jTable2, 2, 120);
        Formater.format(jTable2, 3, 120);

        jTextField7.setText(DateConverter.getTodayDefDate());
        jTextField10.setText(DateConverter.getTodayDefDate());

        this.customer = new Customer();
        this.mainframe = aThis;

        t = new Thread(this);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

        renewTableModel();
        resizeFields();
        offersOfTheMonth();
    }

    public void addToOrder(Product product, boolean ean, boolean name, boolean text) {

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
                    str = str + product.getEan();
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
                m.setValueAt(new Double(0), z, 3);
            }

            try {
                m.setValueAt(new Double(product.getVK()), z, 4);

            } catch (NumberFormatException numberFormatException) {
                nachricht("Wert 'Preis' unzulässig: Produkt " + product.getNummer());
                m.setValueAt(new Double(0), z, 4);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
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
                    {null, 1, null, defTax, null, null}
                },
                new String[]{"id", "Anzahl", "Bezeichnung", "Mehrwertsteuer", "Nettopreis", "Bruttopreis"});

        getJTable1().setModel(f);
        Formater.stripFirst(jTable1);
    }

    private void resizeFields() {
        try {
            getJTable1().getColumn(getJTable1().getColumnName(2)).setPreferredWidth(350);
            getJTable1().getColumn(getJTable1().getColumnName(1)).setPreferredWidth(30);
            getJTable1().getColumn(getJTable1().getColumnName(3)).setPreferredWidth(30);
            getJTable1().getColumn(getJTable1().getColumnName(1)).setMinWidth(30);
            getJTable1().getColumn(getJTable1().getColumnName(3)).setMinWidth(30);
        } catch (Exception exception) {
        }
    }

    private void setAuftrag(boolean b) {

        current.setAuftrag(b);

        if (current.isAuftrag()) {
            this.jCheckBox2.setSelected(true);
            this.jTextField6.setBackground(Color.GREEN);
        } else {
            this.jCheckBox2.setSelected(false);
            this.jTextField6.setBackground(Color.LIGHT_GRAY);
        }
    }

    private void updateListTable() {

        try {
            liste = getCurrent().getWithDepencies("angebote.id,angebotnummer,datum,kundennummer,firma,auftragdatum");
//            String[] label = getCurrent().getLabelsOfAllWithDepencies();
            String k = "id,Nummer,Datum,Kunde,Firma,Auftrag";

            Formater.formatTableArray(liste, new int[]{5});
            Formater.reverseArray(liste);
            this.jTable2.setModel(new DefaultTableModel(liste, k.split(",")));
            getCurrent().stripFirst(jTable2);

            Formater.format(jTable2, 1, 120);
            Formater.format(jTable2, 2, 120);
            Formater.format(jTable2, 3, 120);

        } catch (Exception exception) {
            Log.Debug(exception);
        }

    }

    public void setOrder(Angebot current) {

        this.current = current;
        this.setCustomer(new Customer(current.getKundenId()));

        this.jTextField6.setText(current.getAngebotnummer());
        this.jTextField7.setText(DateConverter.getDefDateString(current.getDatum()));
        this.jTextField10.setText(DateConverter.getDefDateString(current.getBisDatum()));

        this.getJTable1().setModel(current.getProductlistAsTableModel());
        current.stripFirst(getJTable1());
        resizeFields();

        this.setAuftrag(current.isAuftrag());
    }

    public void setCustomer(Customer c) {

        oldcustomer = this.customer;
        this.customer = c;
        getJTextField4().setText(c.getKundennummer());
        jTextField5.setText(c.getFirma());

//        mainframe.nachricht("Aktuell: Kunde Nummer " + c.getKundennummer());

        if (getCustomer().isDeleted()) {
            isdeleted.setText("Gelöscht.");
            jTextField5.setBackground(Color.RED);
        } else {
            isdeleted.setText("");
            jTextField5.setBackground(Color.WHITE);
        }
    }

    private boolean hasCustomer() {
        int i = JOptionPane.OK_OPTION;
        if (getCustomer() != null) {

//            if (oldcustomer != null && !oldcustomer.getid().equals("0") && !oldcustomer.getid().equals(getCustomer().getid())) {
//
//                i = JOptionPane.showConfirmDialog(null, "Wollen Sie wirklich dem Angebot einen neuen Kunden zuordnen?", "Konflikt", JOptionPane.WARNING_MESSAGE);
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
        isdeleted = new javax.swing.JLabel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        jTextField10 = new javax.swing.JTextField();
        jButton13 = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        jButton23 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable()
        {
            public boolean isCellEditable(int x, int y) {
                return false;
            }
        }

        ;
        jPanel1 = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        jButton6 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton18 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton22 = new javax.swing.JButton();
        jCheckBox4 = new javax.swing.JCheckBox();

        setBackground(new java.awt.Color(204, 204, 255));

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
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Suche"));

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
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addContainerGap(11, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Details"));

        jLabel5.setText("Kunde");

        jLabel6.setText("Nummer");

        jLabel7.setText("Firma");

        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jTextField5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField5ActionPerformed(evt);
            }
        });

        jLabel8.setText("Angebot");

        jLabel9.setText("Nummer");

        jLabel10.setText("Datum");

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setModel( jtable1Model
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

        isdeleted.setForeground(new java.awt.Color(153, 0, 0));

        jCheckBox2.setText("Auftrag");
        jCheckBox2.setEnabled(false);
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        jLabel13.setText("bis");

        jTextField10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField10ActionPerformed(evt);
            }
        });

        jButton13.setText("Kalender");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });
        jButton13.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jButton13KeyPressed(evt);
            }
        });

        jLabel14.setText("von");

        jTextField11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField11ActionPerformed(evt);
            }
        });

        jButton23.setText("Kalender");
        jButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton23ActionPerformed(evt);
            }
        });
        jButton23.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jButton23KeyPressed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("Gültig");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(isdeleted, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel13))
                                .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel14)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jTextField11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton23, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(14, 14, 14))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jCheckBox1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 215, Short.MAX_VALUE)
                                .addComponent(jLabel12))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jCheckBox2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 176, Short.MAX_VALUE)
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
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(isdeleted, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton23)
                    .addComponent(jLabel14)
                    .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton10)
                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton13)
                    .addComponent(jLabel13))
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jCheckBox1)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox2)))
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
        jToolBar1.add(jButton4);

        jButton3.setText("Als neues Angebot anlegen");
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

        jButton8.setText("Zeile hinzu");
        jButton8.setFocusable(false);
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton8MouseClicked(evt);
            }
        });
        jToolBar1.add(jButton8);

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

        jButton12.setText("Auftrag");
        jButton12.setFocusable(false);
        jButton12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton12.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton12);

        jButton15.setText("PDF");
        jButton15.setFocusable(false);
        jButton15.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton15.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton15);

        jButton14.setText("Rechnung anlegen");
        jButton14.setFocusable(false);
        jButton14.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton14.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton14);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
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
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
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
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Liste", jPanel3);

        jPanel1.setBackground(new java.awt.Color(255, 255, 204));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jToolBar2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/medium/undo.png"))); // NOI18N
        jButton6.setToolTipText("Rueckgaengig");
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(jButton6);

        jButton20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/medium/tab_remove.png"))); // NOI18N
        jButton20.setToolTipText("Tab schliessen");
        jButton20.setFocusable(false);
        jButton20.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton20.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(jButton20);

        jButton16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/medium/filesave.png"))); // NOI18N
        jButton16.setToolTipText("Speichern");
        jButton16.setFocusable(false);
        jButton16.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton16.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton16MouseClicked(evt);
            }
        });
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton16ActionPerformed(evt);
            }
        });
        jToolBar2.add(jButton16);

        jButton17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/medium/new_window.png"))); // NOI18N
        jButton17.setToolTipText("Als neue Rechnung anlegen");
        jButton17.setFocusable(false);
        jButton17.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton17.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton17MouseClicked(evt);
            }
        });
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });
        jToolBar2.add(jButton17);
        jToolBar2.add(jSeparator2);

        jButton18.setText("Storno");
        jButton18.setFocusable(false);
        jButton18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton18.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton18.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton18MouseClicked(evt);
            }
        });
        jToolBar2.add(jButton18);

        jButton19.setText("Bezahlt");
        jButton19.setFocusable(false);
        jButton19.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton19.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton19.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton19MouseClicked(evt);
            }
        });
        jToolBar2.add(jButton19);

        jButton21.setText("Mahnung");
        jButton21.setFocusable(false);
        jButton21.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton21.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton21ActionPerformed(evt);
            }
        });
        jToolBar2.add(jButton21);
        jToolBar2.add(jSeparator1);

        jButton22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/medium/print_printer.png"))); // NOI18N
        jButton22.setToolTipText("Drucken");
        jButton22.setFocusable(false);
        jButton22.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton22.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton22.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton22MouseClicked(evt);
            }
        });
        jToolBar2.add(jButton22);

        jCheckBox4.setText("mit Lieferschein");
        jCheckBox4.setFocusable(false);
        jCheckBox4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jCheckBox4.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jCheckBox4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBox4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox4ItemStateChanged(evt);
            }
        });
        jToolBar2.add(jCheckBox4);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, 645, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 649, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed

        String[][] list = getCurrent().select("id, auftragnummer, datum ", "auftragnummer", jTextField1.getText(), "auftragnummer", true);
        String k = "id, " + "Nummer,Datum";

        this.jTable3.setModel(new DefaultTableModel(list, k.split(",")));
        getCurrent().stripFirst(jTable3);
//
//        this.jtable3Header = k;
//        this.jtable3Data = current.select("id, kundennummer, firma ", "kundennummer", jTextField1.getText(), "kundennummer", true);
//        

    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField2ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed

        String[][] list = getCurrent().select("id, auftragnummer, datum ", "datum", jTextField2.getText(), "datum", true);
        String k = "id, " + "Nummer,Datum";

        this.jTable3.setModel(new DefaultTableModel(list, k.split(",")));
        getCurrent().stripFirst(jTable3);
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField3ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed

        String[] str = getCustomer().selectLast("id", "firma", jTextField3.getText(), false, false, true);

        if (str != null) {

            String[][] list = getCurrent().select("id, auftragnummer, datum ", "kundenid", str[0], true);
            String k = "id, " + "Nummer,Datum";

            this.jTable3.setModel(new DefaultTableModel(list, k.split(",")));
            getCurrent().stripFirst(jTable3);
        }
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jButton3MouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked

        editor = getJTable1().getCellEditor();
        if (hasCustomer() && validDate()) {
            TableModel m = getJTable1().getModel();
            ListSelectionModel selectionModel = getJTable1().getSelectionModel();
            boolean valide = true;
            if (editor != null) {
                editor.stopCellEditing();
            }
            if (valide) {

                Query f = ConnectionHandler.instanceOf().clone(TABLE_OFFERS);
                Integer auftragnummer = f.getNextIndexOfIntCol("auftragnummer");
                Angebot order = new Angebot(ConnectionHandler.instanceOf());
                order.setAngebotnummer(auftragnummer.toString());
                order.setDatum(DateConverter.getDate(jTextField7.getText()));
                order.setBisDatum(DateConverter.getDate(jTextField10.getText()));
                order.setKundenId(getCustomer().getId());
                order.save();
                this.clear();

                for (int i = 0; i < m.getRowCount(); i++) {

                    //anzahl,bezeichnung,mehrwertsteuer,nettopreis
                    if (m.getValueAt(i, 4) != null) {
                        AngebotPosten b = new AngebotPosten(ConnectionHandler.instanceOf());
                        b.setauftragid(order.getId());
                        b.setPosten((String) m.getValueAt(i, 2));

                        try {
                            b.setAnzahl((Double) m.getValueAt(i, 1));
                            b.setSteuersatz((Double) m.getValueAt(i, 3));
                            b.setPreis((Double) m.getValueAt(i, 4));

                        } catch (Exception exception) {
                            Log.Debug(exception);
                        }
                        b.save();
                    }
                }

                jTextField6.setText(order.getAngebotnummer());
                mainframe.setMessage("Angebot Nummer: " + order.getAngebotnummer() + " angelegt.");
                new HistoryItem(ConnectionHandler.instanceOf(), Strings.ORDER, "Angebot Nummer: " + order.getAngebotnummer() + " angelegt.");
                this.setOrder(new Angebot(order.getId()));
            }
        } else {
            new Popup("Sie müssen einen (validen) Kunden auswählen.", Popup.ERROR);
        }
        updateListTable();
        resizeFields();
        offersOfTheMonth();
    }//GEN-LAST:event_jButton3MouseClicked

    private void jTable2MouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked

        SelectionCheck selection = new SelectionCheck(jTable2);

        if (selection.checkID()) {
            if (evt.getClickCount() >= 2 && evt.getButton() == MouseEvent.BUTTON1) {

                try {
                    this.setOrder(new Angebot(selection.getId()));
                    jTabbedPane1.setSelectedIndex(0);
                } catch (Exception exception) {
                    Log.Debug(exception);
                }
            }
        }
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
                    Query f = ConnectionHandler.instanceOf().clone(TABLE_OFFERS);
                    Angebot order = getCurrent();
                    order.setDatum(DateConverter.getDate(jTextField7.getText()));
                    order.setBisDatum(DateConverter.getDate(jTextField10.getText()));
                    order.setKundenId(getCustomer().getId());
                    order.save();

                    for (int i = 0; i < m.getRowCount(); i++) {
                        if (m.getValueAt(i, 0) != null) {
                            AngebotPosten b = new AngebotPosten(ConnectionHandler.instanceOf(), m.getValueAt(i, 0).toString());
                            b.destroy();
                        }
                    }

                    for (int i = 0; i < m.getRowCount(); i++) {
                        //anzahl,bezeichnung,mehrwertsteuer,nettopreis
                        if (m.getValueAt(i, 4) != null) {
                            AngebotPosten b = new AngebotPosten(ConnectionHandler.instanceOf());
                            b.setauftragid(getCurrent().getId());
                            b.setPosten((String) m.getValueAt(i, 2));
                            try {
                                b.setAnzahl((Double) (m.getValueAt(i, 1)));
                                b.setSteuersatz((Double) (m.getValueAt(i, 3)));
                                b.setPreis((Double) (m.getValueAt(i, 4)));
                            } catch (Exception exception) {
                                Log.Debug(exception);
                            }
                            b.save();
                        }
                    }

                    mainframe.getNachricht().setText("Angebot Nummer " + order.getAngebotnummer() + " gespeichert.");
                    new HistoryItem(ConnectionHandler.instanceOf(), Strings.ORDER, "Angebot Nummer: " + order.getAngebotnummer() + " editiert.");
                    this.setOrder(new Angebot(order.getId()));
                }
            } else {
                jButton3MouseClicked(new MouseEvent(jTable1, WIDTH, WIDTH, WIDTH, WIDTH, WIDTH, WIDTH, nettoprices));
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

        SelectionCheck selection = new SelectionCheck(jTable1);

        if (selection.checkID() && evt.getClickCount() >= 2) {

            try {
                this.setOrder(new Angebot(selection.getId()));
            } catch (Exception exception) {
                Log.Debug(exception);
            }
        }
    }//GEN-LAST:event_jTable3MouseClicked

    private void clear() {

        this.customer = new Customer(ConnectionHandler.instanceOf());
        this.current = new Angebot(ConnectionHandler.instanceOf());

        jTextField4.setText("");
        jTextField5.setText("");
        jTextField6.setText("");
        jTextField6.setBackground(Color.WHITE);
        jCheckBox2.setSelected(false);

        renewTableModel();

        df = new SimpleDateFormat("dd.MM.yyyy");
        jTextField7.setText(df.format(new Date()));

        getCurrent().stripFirst(getJTable1());
        resizeFields();
    }

    private void jButton7ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        this.clear();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jTextField4ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed

        String[] st = getCustomer().selectLast(Strings.ALL, "kundennummer", jTextField4.getText(), false, false, false);

        this.clear();
        jTextField5.setText("");
        jTextField6.setText("");
        jTextField7.setText(df.format(new Date()));

        try {
            this.setCustomer(new Customer(Integer.valueOf(st[0])));
        } catch (Exception exception) {
            try {
                this.setCustomer(new Customer(ConnectionHandler.instanceOf(), jTextField4.getText(), true));

            } catch (Exception exception1) {
                mainframe.setMessage("Kein Datensatz gefunden k");
            }
        }
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jTextField5ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed

        String[] st = getCustomer().selectLast(Strings.ALL, "firma", jTextField5.getText(), false, false, false);

        this.clear();
        jTextField4.setText("");
        jTextField6.setText("");
        jTextField7.setText(df.format(new Date()));
        String[][] sta;

        try {
            this.setCustomer(new Customer(Integer.valueOf(st[0])));
        } catch (Exception exception) {
            try {
                this.setCustomer(new Customer(ConnectionHandler.instanceOf(), jTextField4.getText(), true));
            } catch (Exception exception1) {
                mainframe.setMessage("Kein Datensatz gefunden k");
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
        try {
            if (m.getValueAt(getJTable1().getSelectedRow(), 0) != null) {
                AngebotPosten b = new AngebotPosten((Integer) m.getValueAt(getJTable1().getSelectedRow(), 0));
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
            Log.Debug(e);
        }
    }//GEN-LAST:event_jButton11MouseClicked

    private void jButton10KeyPressed (java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jButton10KeyPressed
        jButton1MouseClicked(new MouseEvent(jTable1, WIDTH, WIDTH, WIDTH, WIDTH, WIDTH, WIDTH, nettoprices));
    }//GEN-LAST:event_jButton10KeyPressed

    private void jButton5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton5MouseClicked
        new ProductPicker(this);
    }//GEN-LAST:event_jButton5MouseClicked

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        jButton12ActionPerformed(evt);
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void offersOfTheMonth() {

        jTextField2.setText(DateConverter.getTodayDefDate());

        String[][] list = getCurrent().select("id, angebotnummer, datum ", "datum", DateConverter.getTodayDefDate(), "datum", true);
        String k = "id, " + "Nummer,Datum";

        this.jTable3.setModel(new DefaultTableModel(list, k.split(",")));
        getCurrent().stripFirst(jTable3);
    }

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        if (current.hasId()) {
            if (!current.isAuftrag()) {
                this.current.setAuftrag(true);
                this.setAuftrag(true);
                this.current.save();
                mainframe.setMessage("Angebot Nummer " + current.getAngebotnummer() + " als 'Auftrag' markiert.");
            }
        }
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jTextField10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField10ActionPerformed
    }//GEN-LAST:event_jTextField10ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        new DatePick(jTextField10);
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton13KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jButton13KeyPressed
    }//GEN-LAST:event_jButton13KeyPressed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed

        jButton4MouseClicked(new MouseEvent(jTable1, WIDTH, WIDTH, WIDTH, WIDTH, WIDTH, WIDTH, nettoprices));
        jButton12ActionPerformed(evt);

        df = new SimpleDateFormat("dd.MM.yyyy");
        Rechnung bill = new Rechnung();
        if (!current.hasRechnung()) {
            current.setAuftrag(true);
            editor = getJTable1().getCellEditor();

            if (hasCustomer() && validDate()) {
                TableModel m = getJTable1().getModel();
                boolean valide = true;
                if (editor != null) {
                    editor.stopCellEditing();
                }
                if (valide) {
                    Query f = ConnectionHandler.instanceOf().clone(TABLE_BILLS);
                    Integer rechnungnummer = f.getNextIndexOfIntCol("rechnungnummer");

                    bill.setRechnungnummer(rechnungnummer.toString());
                    bill.setDatum(new Date());
                    bill.setKundenId(getCustomer().getId());
                    bill.save();

                    for (int i = 0; i < m.getRowCount(); i++) {

                        //anzahl,bezeichnung,mehrwertsteuer,nettopreis
                        if (m.getValueAt(i, 4) != null) {
                            RechnungPosten b = new RechnungPosten(ConnectionHandler.instanceOf());
                            b.setRechnungid(bill.getId());
                            b.setPosten((String) m.getValueAt(i, 2));

                            try {
                                b.setAnzahl((Double) m.getValueAt(i, 1));
                                b.setSteuersatz((Double) m.getValueAt(i, 3));
                                b.setPreis((Double) m.getValueAt(i, 4));
                            } catch (Exception exception) {
                                Log.Debug(exception);
                            }
                            b.save();
                        }
                    }
                    current.setRechnung(true);
                    mainframe.setMessage("Rechnung Nummer: " + bill.getRechnungnummer() + " angelegt.");
                }
            } else {
                new Popup("Sie müssen einen (validen) Kunden auswählen.", Popup.ERROR);
            }
//            mainframe.getBillPanel().updateListTable();
//            mainframe.getBillPanel().resizeFields();
//            mainframe.getBillPanel().setBill(new Rechnung(bill.getId()));
            mainframe.getJTabbedPane1().setSelectedIndex(1);
        } else {
            Popup.notice("Zu diesem Auftrag existiert bereits eine Rechnung");
        }
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        new PDF_Angebot(current);
    }//GEN-LAST:event_jButton15ActionPerformed

private void jButton16MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton16MouseClicked
save();
}//GEN-LAST:event_jButton16MouseClicked

private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed

}//GEN-LAST:event_jButton16ActionPerformed

private void jButton17MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton17MouseClicked
createNew();
}//GEN-LAST:event_jButton17MouseClicked

private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
//       
//        jButton3MouseClicked(new MouseEvent(jTable1, WIDTH, WIDTH, WIDTH, WIDTH, WIDTH, WIDTH, nettoprices));
}//GEN-LAST:event_jButton17ActionPerformed

private void jButton18MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton18MouseClicked
this.currentBill.setStorno(true);
        this.jButton4MouseClicked(evt);
        setEdited(true);
}//GEN-LAST:event_jButton18MouseClicked

private void jButton19MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton19MouseClicked
this.currentBill.setBezahlt(true);
        this.jButton4MouseClicked(evt);
        setEdited(true);
}//GEN-LAST:event_jButton19MouseClicked

private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
if (currentBill.hasId()) {
            new MahnungView(currentBill, customer, this);
        }
        setEdited(true);
}//GEN-LAST:event_jButton21ActionPerformed

private void jButton22MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton22MouseClicked
jButton4MouseClicked(evt);

        if (currentBill != null && currentBill.hasId()) {
            new PDF_Rechnung(currentBill);
            new HistoryItem(ConnectionHandler.instanceOf(), Strings.BILL, "Rechnung Nummer: " + currentBill.getRechnungnummer() + " als PDF erzeugt.");
        }
}//GEN-LAST:event_jButton22MouseClicked

private void jCheckBox4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox4ItemStateChanged
Programmdaten.instanceOf().setBILLPANEL_CHECKBOX_MITLIEFERSCHEIN(jCheckBox4.isSelected());
}//GEN-LAST:event_jCheckBox4ItemStateChanged

private void jTextField11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField11ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jTextField11ActionPerformed

private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButton23ActionPerformed

private void jButton23KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jButton23KeyPressed
// TODO add your handling code here:
}//GEN-LAST:event_jButton23KeyPressed

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
    public javax.swing.JLabel isdeleted;
    public javax.swing.JButton jButton10;
    public javax.swing.JButton jButton11;
    public javax.swing.JButton jButton12;
    public javax.swing.JButton jButton13;
    public javax.swing.JButton jButton14;
    public javax.swing.JButton jButton15;
    public javax.swing.JButton jButton16;
    public javax.swing.JButton jButton17;
    public javax.swing.JButton jButton18;
    public javax.swing.JButton jButton19;
    public javax.swing.JButton jButton20;
    public javax.swing.JButton jButton21;
    public javax.swing.JButton jButton22;
    public javax.swing.JButton jButton23;
    public javax.swing.JButton jButton3;
    public javax.swing.JButton jButton4;
    public javax.swing.JButton jButton5;
    public javax.swing.JButton jButton6;
    public javax.swing.JButton jButton7;
    public javax.swing.JButton jButton8;
    public javax.swing.JButton jButton9;
    public javax.swing.JCheckBox jCheckBox1;
    public javax.swing.JCheckBox jCheckBox2;
    public javax.swing.JCheckBox jCheckBox4;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JLabel jLabel10;
    public javax.swing.JLabel jLabel11;
    public javax.swing.JLabel jLabel12;
    public javax.swing.JLabel jLabel13;
    public javax.swing.JLabel jLabel14;
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
    public javax.swing.JTabbedPane jTabbedPane1;
    public javax.swing.JTable jTable1;
    public javax.swing.JTable jTable2;
    public javax.swing.JTable jTable3;
    public javax.swing.JTextField jTextField1;
    public javax.swing.JTextField jTextField10;
    public javax.swing.JTextField jTextField11;
    public javax.swing.JTextField jTextField2;
    public javax.swing.JTextField jTextField3;
    public javax.swing.JTextField jTextField4;
    public javax.swing.JTextField jTextField5;
    public javax.swing.JTextField jTextField6;
    public javax.swing.JTextField jTextField7;
    public javax.swing.JTextField jTextField8;
    public javax.swing.JTextField jTextField9;
    public javax.swing.JToolBar jToolBar1;
    public javax.swing.JToolBar jToolBar2;
    // End of variables declaration//GEN-END:variables

    private boolean validDate() {
        if (DateConverter.getDate(jTextField7.getText()) != null) {
            return true;
        } else {
            new Popup("Sie müssen ein Datum angeben (tt.mm.yyyy)", Popup.ERROR);
            return false;
        }
    }

    public Angebot getCurrent() {
        if (current != null) {
            return current;
        } else {
            return new Angebot(ConnectionHandler.instanceOf());
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

    public void run() {

        TableColumn column;
        CellEditor cell;

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
            while (mainframe.getShowingTab() == 3) {
                try {
                    Thread.sleep(800);
                } catch (Exception e) {
                }

                TableModel m = getJTable1().getModel();

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
                                Log.Debug(e);
                            }
                        }
                        jTextField9.setText(Formater.formatDecimal(brutto - netto));//!tax
                        jTextField8.setText(Formater.formatDecimal(brutto));//!tax
                    }
                } catch (Exception ex) {
                    Log.Debug(ex);
                    Log.Debug(ex);
                }
            }
        }
    }
}




    



