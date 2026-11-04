/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * ContactPanel.java
 *
 * Created on Nov 20, 2008, 8:17:28 AM
 */
package mpv5.ui.panels;

import mpv5.handler.TemplateHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import mpv5.db.common.*;
import mpv5.db.objects.Product;
import mpv5.db.objects.ProductList;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.Group;
import mpv5.db.objects.MailMessage;
import mpv5.db.objects.ProductGroup;
import mpv5.db.objects.ProductPrice;
import mpv5.db.objects.ProductsToSuppliers;
import mpv5.db.objects.Tax;
import mpv5.logging.Log;
import mpv5.ui.dialogs.BigPopup;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Groups;
import mpv5.ui.popups.FileTablePopUp;
import mpv5.ui.toolbars.DataPanelTB;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.ui.beans.MPCBSelectionChangeReceiver;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.misc.MPTable;
import mpv5.utils.export.Export;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.images.MPIcon;
import mpv5.utils.jobs.Job;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.tables.TableFormat;
import mpv5.ui.misc.TableViewPersistenceHandler;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 *
 */
public class ProductPanel extends javax.swing.JPanel implements DataPanel, MPCBSelectionChangeReceiver, ExportablePanel {

    private static final long serialVersionUID = 1L;
    private Product dataOwner;
    private DataPanelTB tb;
    private SearchPanel sp;
    private FileTablePopUp fil;
    private int type = 0;
    private String old_cnumber = "";
    private java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();
    private File currentImageFile;
    private String lastimage;

    /**
     * Creates new form ContactPanel
     *
     * @param context
     * @param type
     */
    public ProductPanel(Context context, int type) {
        initComponents();
        sp = new SearchPanel(context, this);
        sp.setVisible(true);
        tb = new mpv5.ui.toolbars.DataPanelTB(this);
        toolbarpane.add(tb, BorderLayout.CENTER);
        dataOwner = new Product();
        dataOwner.setInttype(type);
        this.type = type;
        refresh();

        addedby.setText(mpv5.db.objects.User.getCurrentUser().getName());

        contactname1.setSearchEnabled(true);
        contactname1.setContext(Context.getManufacturer());
//        contactname1.getComboBox().addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                final MPComboBoxModelItem item = contactname1.getSelectedItem();
//                if (item != null && item.isValid()) {
//                    Runnable runnable = new Runnable() {
//
//                        @Override
//                        public void run() {
//                            try {
//                                Contact dbo1 = (Contact) DatabaseObject.getObject(Context.getContact(), Integer.valueOf(item.getId()));
////                                contactcity1.setText(dbo1.__getCity());
////                                contactcompany1.setText(dbo1.__getCompany());
////                                contactid1.setText(dbo1.__getCNumber());
//                            } catch (NodataFoundException ex) {
//                            }
//                        }
//                    };
//                    SwingUtilities.invokeLater(runnable);
//                }
//            }
//        });

        familyselect.setContext(Context.getProductGroup());
        familyselect.setSearchEnabled(true);
        groupnameselect.setContext(Context.getGroup());
        groupnameselect.setSearchEnabled(true);
//
//        currencylabel1.setText(NumberFormat.getCurrencyInstance().getCurrency().getSymbol());
//        currencylabel2.setText(NumberFormat.getCurrencyInstance().getCurrency().getSymbol());

        netvalue.set_ValueClass(Double.class);
        netvalue.set_ValueClass(Double.class);


        JMenuItem x = new JMenuItem(Messages.SET_AS_DEFAULT.getValue());
        x.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String fname = dataTable.getModel().getValueAt(dataTable.getSelectedRow(), 0).toString();
                defaultimage_ = fname;
                setIcon();
                save();
            }
        });

        fil = new FileTablePopUp();
        fil.addDefaultPopupMenu(dataTable);
        fil.addSeparator();
        fil.add(x);

        cnumber.setSearchOnEnterEnabled(true);
        cnumber.setParent(this);
        cnumber.setSearchField("cnumber");
        cnumber.setContext(Context.getProduct());

        cname.setSearchOnEnterEnabled(true);
        cname.setParent(this);
        cname.setSearchField("cname");
        cname.setContext(Context.getProduct());

        selecttax.setSearchEnabled(true);
        selecttax.setContext(Context.getTaxes());
        selecttax.getComboBox().setEditable(false);

        contactname1.setEditable(true);
        supplierpanel.add(new ProductPanelContactSub(null, true));
        calcFactor.set_ValueClass(Double.class);
        calcResult.set_ValueClass(Double.class);
        extvalue.set_ValueClass(Double.class);
        netvalue.set_ValueClass(Double.class);

        selecttax.getComboBox().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                taxids_ = Integer.valueOf(selecttax.getSelectedItem().getIdObject().toString());
                double val = extvalue.getValue(0d);
                calcResult.setText(FormatNumber.formatDezimal(new BigDecimal(val).multiply(Tax.getCalculationValue(taxids_))));
//                Popup.notice("Tax ID ist jetzt :" + taxids_);
            }
        });
 
 
    }

    private void recalc() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                double val = calcFactor.getValue(0d);
                BigDecimal b = BigDecimal.valueOf(netvalue.getValue(0d)).multiply(BigDecimal.valueOf(1 + (val / 100)));
                calcResult.setText(FormatNumber.formatDezimal(b.multiply(Tax.getCalculationValue(taxids_))));
                extvalue.setText(FormatNumber.formatDezimal(b));
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    /**
     *
     * @param c
     */
    public ProductPanel(Context c) {
        this(c, -1);
    }

    /**
     * Popuplates the Product p into $this
     *
     * @param p
     */
    public ProductPanel(Product p) {
        this(p.getContext(), p.__getInttype());
        setDataOwner(p, true);
        ((MPTable) dataTable).setPersistanceHandler(new TableViewPersistenceHandler((MPTable) dataTable, this));
    }

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean populate) {
        dataOwner = (Product) object;
        if (populate) {
            dataOwner.setPanelData(this);
            this.exposeData();

            setTitle();

            prinitingComboBox1.init(rightpane);

            tb.setFavourite(Favourite.isFavourite(object));
            tb.setEditable(!object.isReadOnly());

            if (object.isReadOnly()) {
                Popup.notice(Messages.LOCKED_BY);
            }

            button_preview.setEnabled(false);
            button_order.setEnabled(false);
            preloadTemplate();
        }
    }

    private void setTitle() {
        if (this.getParent() instanceof JViewport || this.getParent() instanceof JTabbedPane) {
            JTabbedPane jTabbedPane = null;
            String title1 = cname_;
            //this->viewport->scrollpane->tabbedpane
            if (this.getParent().getParent().getParent() instanceof JTabbedPane) {
                jTabbedPane = (JTabbedPane) this.getParent().getParent().getParent();
            } else {
                try {
                    jTabbedPane = (JTabbedPane) this.getParent();
                } catch (Exception e) {
                    //Free floating window
                    try {
                        ((JFrame) this.getRootPane().getParent()).setTitle(title1);
                    } catch (Exception ge) {
                    }
                }
            }
            if (jTabbedPane != null) {
                jTabbedPane.setTitleAt(jTabbedPane.getSelectedIndex(), title1);
            }
        }
    }

    @Override
    public void showRequiredFields() {
        TextFieldUtils.blinkerRed(cname);
    }

    private void addFile() {
        if (dataOwner.isExisting()) {
            DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY);
            if (d.chooseFile()) {
                String s = Popup.Enter_Value(Messages.ENTER_A_DESCRIPTION);
                if (s != null) {
                    if (QueryHandler.instanceOf().clone(Context.getFiles(), this).insertFile(d.getFile(), dataOwner, QueryCriteria.getSaveStringFor(s))) {
                        Log.Debug(this, "Insert file..done!");
                    }
                }
            }
        }
    }

    private void deleteFile() {
        if (dataOwner.isExisting()) {
            try {
                DatabaseObject.getObject(Context.getFilesToProducts(), "filename", (dataTable.getModel().getValueAt(dataTable.getSelectedRow(), 0).toString())).delete();
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
            fillFiles();
            setIcon();
        }
    }

    private void fileTableClicked(MouseEvent evt) {
        if (evt.getClickCount() > 1) {
            FileDirectoryHandler.open(QueryHandler.instanceOf().clone(Context.getFiles()).retrieveFile(dataTable.getModel().getValueAt(dataTable.getSelectedRow(), 0).toString(), new File(FileDirectoryHandler.getTempDir() + dataTable.getModel().getValueAt(dataTable.getSelectedRow(), 1).toString())));
        } else if (evt.getClickCount() == 1 && evt.getButton() == MouseEvent.BUTTON3) {

            JTable source = (JTable) evt.getSource();
            int row = source.rowAtPoint(evt.getPoint());
            int column = source.columnAtPoint(evt.getPoint());

            if (!source.isRowSelected(row)) {
                source.changeSelection(row, column, false, false);
            }

            fil.show(source, evt.getX(), evt.getY());
        }
    }

    private void fillFiles() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Context c = Context.getFilesToProducts();
                c.addReference(Context.getFiles().getDbIdentity(), "cname", "filename");
                Object[][] data = new DatabaseSearch(c).getValuesFor(Context.DETAILS_FILES_TO_PRODUCTS, "productsids", dataOwner.__getIDS());

                dataTable.setModel(new MPTableModel(data, Headers.FILE_REFERENCES.getValue()));
                TableFormat.stripFirstColumn(dataTable);

                if (dataTable.getModel().getRowCount() == 1 && defaultimage_ == null) {
                    final String fname = dataTable.getModel().getValueAt(0, 0).toString();
                    defaultimage_ = fname;
                    setIcon();
                }
            }
        };
        new Thread(runnable).start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings({"unchecked", "deprecation"})
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        leftpane = new javax.swing.JPanel();
        rightpane = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        cnumber = new mpv5.ui.beans.LabeledTextField();
        addedby = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        button_groupadd = new javax.swing.JButton();
        stype = new mpv5.ui.beans.LabeledCombobox();
        familyselect = new mpv5.ui.beans.LabeledCombobox();
        groupnameselect = new mpv5.ui.beans.MPCombobox();
        path = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        button_preview = new javax.swing.JButton();
        button_order = new javax.swing.JButton();
        button_listedit = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        prinitingComboBox1 = new mpv5.ui.beans.PrinitingComboBox();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        cname = new mpv5.ui.beans.LabeledTextField();
        ean = new mpv5.ui.beans.LabeledTextField();
        url = new mpv5.ui.beans.LabeledTextField();
        netvalue = new mpv5.ui.beans.LabeledTextField();
        unit = new mpv5.ui.beans.LabeledTextField();
        selecttax = new mpv5.ui.beans.LabeledCombobox();
        reference = new mpv5.ui.beans.LabeledTextField();
        extvalue = new mpv5.ui.beans.LabeledTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        class NoTabTextArea extends JTextArea {
            protected void processComponentKeyEvent( KeyEvent e ) {
                if ( e.getID() == KeyEvent.KEY_PRESSED &&
                    e.getKeyCode() == KeyEvent.VK_TAB ) {
                    e.consume();
                    if (e.isShiftDown()) {
                        transferFocusBackward();
                    } else {
                        transferFocus();
                    }
                } else {
                    super.processComponentKeyEvent( e );
                }
            }
        }
        description = new NoTabTextArea()
        ;
        contactname1 = new mpv5.ui.beans.LabeledCombobox();
        calcFactor = new mpv5.ui.beans.LabeledTextField();
        calcResult = new mpv5.ui.beans.LabeledTextField();
        jButton3 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        supplierpanel = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        stack = new mpv5.ui.beans.LabeledTextField();
        threshold = new mpv5.ui.beans.LabeledTextField();
        inventoryDisabled = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        prizes = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        imgpanel = new javax.swing.JPanel();
        imageprev = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        dataTable = new  mpv5.ui.misc.MPTable(this) {
            public Component prepareRenderer(TableCellRenderer renderer,
                int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent)c;
                    jc.setToolTipText(String.valueOf(getValueAt(rowIndex, vColIndex)));
                }
                return c;
            }
        }
        ;
        removefile = new javax.swing.JButton();
        addfile = new javax.swing.JButton();
        toolbarpane = new javax.swing.JPanel();

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ProductPanel.border.title_1"))); // NOI18N
        setName("Form"); // NOI18N

        leftpane.setName("leftpane"); // NOI18N
        leftpane.setLayout(new java.awt.BorderLayout());

        rightpane.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ProductPanel.rightpane.border.title"))); // NOI18N
        rightpane.setName("rightpane"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setName("jPanel1"); // NOI18N

        cnumber.set_Label(bundle.getString("ProductPanel.cnumber._Label")); // NOI18N
        cnumber.setFocusable(false);
        cnumber.setFont(cnumber.getFont());
        cnumber.setName("cnumber"); // NOI18N

        addedby.setFont(addedby.getFont());
        addedby.setText(bundle.getString("ProductPanel.addedby.text")); // NOI18N
        addedby.setToolTipText(bundle.getString("ProductPanel.addedby.toolTipText")); // NOI18N
        addedby.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        addedby.setName("addedby"); // NOI18N

        jLabel4.setFont(jLabel4.getFont());
        jLabel4.setText(bundle.getString("ProductPanel.jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        button_groupadd.setFont(button_groupadd.getFont().deriveFont(button_groupadd.getFont().getStyle() & ~java.awt.Font.BOLD, button_groupadd.getFont().getSize()-2));
        button_groupadd.setText(bundle.getString("ProductPanel.button_groupadd.text")); // NOI18N
        button_groupadd.setFocusable(false);
        button_groupadd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_groupadd.setName("button_groupadd"); // NOI18N
        button_groupadd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_groupadd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_groupaddActionPerformed(evt);
            }
        });

        stype.set_Label(bundle.getString("ProductPanel.stype._Label")); // NOI18N
        stype.setName("stype"); // NOI18N

        familyselect.set_Label(bundle.getString("ProductPanel.familyselect._Label")); // NOI18N
        familyselect.setName("familyselect"); // NOI18N
        familyselect.setSearchOnEnterEnabled(false);

        groupnameselect.setName("groupnameselect"); // NOI18N

        path.setFont(path.getFont().deriveFont(path.getFont().getStyle() | java.awt.Font.BOLD, path.getFont().getSize()+1));
        path.setText(bundle.getString("ProductPanel.path.text")); // NOI18N
        path.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.highlight")));
        path.setName("path"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(path, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cnumber, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                            .addComponent(stype, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(groupnameselect, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(button_groupadd, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                            .addComponent(familyselect, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addedby, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(stype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(familyselect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addedby, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cnumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(groupnameselect, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_groupadd, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(path, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(29, 29, 29))
        );

        jToolBar1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(366, 28));
        jToolBar1.setName("jToolBar1"); // NOI18N
        jToolBar1.setPreferredSize(new java.awt.Dimension(306, 28));

        button_preview.setText(bundle.getString("ProductPanel.button_preview.text")); // NOI18N
        button_preview.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        button_preview.setFocusable(false);
        button_preview.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_preview.setMaximumSize(new java.awt.Dimension(120, 20));
        button_preview.setMinimumSize(new java.awt.Dimension(80, 20));
        button_preview.setName("button_preview"); // NOI18N
        button_preview.setPreferredSize(new java.awt.Dimension(100, 20));
        button_preview.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_preview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_previewActionPerformed(evt);
            }
        });
        jToolBar1.add(button_preview);

        button_order.setText(bundle.getString("ProductPanel.button_order.text")); // NOI18N
        button_order.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        button_order.setFocusable(false);
        button_order.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_order.setMaximumSize(new java.awt.Dimension(120, 20));
        button_order.setMinimumSize(new java.awt.Dimension(80, 20));
        button_order.setName("button_order"); // NOI18N
        button_order.setPreferredSize(new java.awt.Dimension(100, 20));
        button_order.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_order.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_orderActionPerformed(evt);
            }
        });
        jToolBar1.add(button_order);

        button_listedit.setText(bundle.getString("ProductPanel.button_listedit.text")); // NOI18N
        button_listedit.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        button_listedit.setEnabled(false);
        button_listedit.setFocusable(false);
        button_listedit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_listedit.setMaximumSize(new java.awt.Dimension(120, 20));
        button_listedit.setMinimumSize(new java.awt.Dimension(80, 20));
        button_listedit.setName("button_listedit"); // NOI18N
        button_listedit.setPreferredSize(new java.awt.Dimension(100, 20));
        button_listedit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_listedit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_listeditActionPerformed(evt);
            }
        });
        jToolBar1.add(button_listedit);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jToolBar1.add(jSeparator1);

        prinitingComboBox1.setMaximumSize(new java.awt.Dimension(220, 27));
        prinitingComboBox1.setMinimumSize(new java.awt.Dimension(109, 20));
        prinitingComboBox1.setName("prinitingComboBox1"); // NOI18N
        prinitingComboBox1.setPreferredSize(new java.awt.Dimension(200, 20));
        jToolBar1.add(prinitingComboBox1);

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setFocusCycleRoot(true);
        jPanel4.setName("jPanel4"); // NOI18N

        cname.set_Label(bundle.getString("ProductPanel.cname._Label_1")); // NOI18N
        cname.setName("cname"); // NOI18N

        ean.set_Label(bundle.getString("ProductPanel.ean._Label_1")); // NOI18N
        ean.setName("ean"); // NOI18N

        url.set_Label(bundle.getString("ProductPanel.url._Label_1")); // NOI18N
        url.setName("url"); // NOI18N

        netvalue.set_Label(bundle.getString("ProductPanel.netvalue._Label_1")); // NOI18N
        netvalue.setName("netvalue"); // NOI18N
        netvalue.setNextFocusableComponent(ean);

        unit.set_Label(bundle.getString("ProductPanel.unit._Label")); // NOI18N
        unit.setName("unit"); // NOI18N
        unit.setNextFocusableComponent(reference);

        selecttax.set_Label(bundle.getString("ProductPanel.selecttax._Label_1")); // NOI18N
        selecttax.setName("selecttax"); // NOI18N

        reference.set_Label(bundle.getString("ProductPanel.reference._Label")); // NOI18N
        reference.setName("reference"); // NOI18N

        extvalue.set_Label(bundle.getString("ProductPanel.extvalue._Label")); // NOI18N
        extvalue.setName("extvalue"); // NOI18N
        extvalue.setNextFocusableComponent(url);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        description.setColumns(20);
        description.setLineWrap(true);
        description.setRows(5);
        description.setWrapStyleWord(true);
        description.setFocusTraversalPolicyProvider(true);
        description.setHighlighter(null);
        description.setName("description"); // NOI18N
        jScrollPane3.setViewportView(description);

        contactname1.set_Label(bundle.getString("ProductPanel.contactname1._Label")); // NOI18N
        contactname1.setName("contactname1"); // NOI18N

        calcFactor.set_Label(bundle.getString("ProductPanel.calcFactor._Label")); // NOI18N
        calcFactor.setName("calcFactor"); // NOI18N
        calcFactor.setNextFocusableComponent(ean);

        calcResult.set_Label(bundle.getString("ProductPanel.calcResult._Label")); // NOI18N
        calcResult.setEnabled(false);
        calcResult.setName("calcResult"); // NOI18N
        calcResult.setNextFocusableComponent(url);

        jButton3.setText(bundle.getString("ProductPanel.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cname, javax.swing.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE)
                    .addComponent(contactname1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ean, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(url, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(reference, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(calcFactor, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(unit, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                            .addComponent(netvalue, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(extvalue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(selecttax, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(calcResult, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(cname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(ean, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(url, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(reference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(contactname1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(netvalue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(extvalue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(unit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(selecttax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(calcFactor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(calcResult, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 16, Short.MAX_VALUE))
                    .addComponent(jScrollPane3))
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("ProductPanel.jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        jPanel6.setName(bundle.getString("ProductPanel.jPanel6.name")); // NOI18N
        jPanel6.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new java.awt.BorderLayout());

        supplierpanel.setBackground(new java.awt.Color(255, 255, 255));
        supplierpanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ProductPanel.supplierpanel.border.title"))); // NOI18N
        supplierpanel.setName("supplierpanel"); // NOI18N
        supplierpanel.setLayout(new javax.swing.BoxLayout(supplierpanel, javax.swing.BoxLayout.PAGE_AXIS));
        jPanel3.add(supplierpanel, java.awt.BorderLayout.CENTER);

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));
        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ProductPanel.jPanel9.border.title"))); // NOI18N
        jPanel9.setName("jPanel9"); // NOI18N

        stack.set_Label(bundle.getString("ProductPanel.stack._Label_1")); // NOI18N
        stack.set_ValueClass(java.math.BigDecimal.class);
        stack.setName("stack"); // NOI18N

        threshold.set_Label(bundle.getString("ProductPanel.threshold._Label_1")); // NOI18N
        threshold.set_ValueClass(java.math.BigDecimal.class);
        threshold.setName("threshold"); // NOI18N

        inventoryDisabled.setBackground(new java.awt.Color(255, 255, 255));
        inventoryDisabled.setText(bundle.getString("ProductPanel.inventoryDisabled.text")); // NOI18N
        inventoryDisabled.setName("inventoryDisabled"); // NOI18N
        inventoryDisabled.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                inventoryDisabledItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(stack, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(threshold, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(inventoryDisabled)
                .addContainerGap(395, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(inventoryDisabled, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                    .addComponent(stack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(threshold, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.add(jPanel9, java.awt.BorderLayout.NORTH);

        jScrollPane1.setViewportView(jPanel3);

        jPanel6.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab(bundle.getString("ProductPanel.jPanel6.TabConstraints.tabTitle"), jPanel6); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/22/adept_install.png"))); // NOI18N
        jButton1.setText(bundle.getString("ProductPanel.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        prizes.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        prizes.setName("prizes"); // NOI18N
        prizes.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 1, 5));

        jButton2.setText(bundle.getString("ProductPanel.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(prizes, javax.swing.GroupLayout.PREFERRED_SIZE, 515, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap(231, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addContainerGap(179, Short.MAX_VALUE))
            .addComponent(prizes, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(bundle.getString("ProductPanel.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel7.setName(bundle.getString("ProductPanel.jPanel7.name")); // NOI18N
        jPanel7.setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(500);
        jSplitPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSplitPane1.setName("jSplitPane1"); // NOI18N
        jSplitPane1.setOneTouchExpandable(true);

        imgpanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        imgpanel.setName("imgpanel"); // NOI18N
        imgpanel.setLayout(new java.awt.BorderLayout());

        imageprev.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageprev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xcf.png"))); // NOI18N
        imageprev.setText(bundle.getString("ProductPanel.imageprev.text")); // NOI18N
        imageprev.setMaximumSize(new java.awt.Dimension(3200, 3200));
        imageprev.setName(bundle.getString("ProductPanel.imageprev.name")); // NOI18N
        imageprev.setPreferredSize(new java.awt.Dimension(333, 333));
        imageprev.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imageprevMouseClicked(evt);
            }
        });
        imgpanel.add(imageprev, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(imgpanel);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        dataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        dataTable.setName("dataTable"); // NOI18N
        dataTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dataTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(dataTable);

        jSplitPane1.setRightComponent(jScrollPane2);

        jPanel7.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        removefile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/remove.png"))); // NOI18N
        removefile.setText(bundle.getString("ProductPanel.removefile.text")); // NOI18N
        removefile.setName("removefile"); // NOI18N
        removefile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removefileActionPerformed(evt);
            }
        });

        addfile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/add.png"))); // NOI18N
        addfile.setText(bundle.getString("ProductPanel.addfile.text")); // NOI18N
        addfile.setName("addfile"); // NOI18N
        addfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addfileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout rightpaneLayout = new javax.swing.GroupLayout(rightpane);
        rightpane.setLayout(rightpaneLayout);
        rightpaneLayout.setHorizontalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightpaneLayout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(removefile, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addfile, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        rightpaneLayout.setVerticalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightpaneLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(rightpaneLayout.createSequentialGroup()
                        .addComponent(addfile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removefile)
                        .addContainerGap(29, Short.MAX_VALUE))))
        );

        toolbarpane.setName("toolbarpane"); // NOI18N
        toolbarpane.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(leftpane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(rightpane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolbarpane, javax.swing.GroupLayout.DEFAULT_SIZE, 849, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolbarpane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rightpane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(leftpane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void button_groupaddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_groupaddActionPerformed
        BigPopup.showPopup(this, new ControlPanel_Groups(), null);
}//GEN-LAST:event_button_groupaddActionPerformed

    private void removefileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removefileActionPerformed
        deleteFile();
}//GEN-LAST:event_removefileActionPerformed

    private void addfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addfileActionPerformed
        if (dataOwner.getPanelData(this) && dataOwner.save()) {
            addFile();
        }
}//GEN-LAST:event_addfileActionPerformed

    private void dataTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dataTableMouseClicked
        fileTableClicked(evt);
    }//GEN-LAST:event_dataTableMouseClicked

    private void button_previewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_previewActionPerformed
        preview();
    }//GEN-LAST:event_button_previewActionPerformed

    private void inventoryDisabledItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_inventoryDisabledItemStateChanged

        stack.setEnabled(!inventoryDisabled.isSelected());
        threshold.setEnabled(!inventoryDisabled.isSelected());
    }//GEN-LAST:event_inventoryDisabledItemStateChanged

    private void button_orderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_orderActionPerformed

        preview_order();

    }//GEN-LAST:event_button_orderActionPerformed

    private void button_listeditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_listeditActionPerformed

        try {
            mpv5.db.objects.ProductList l = (ProductList) DatabaseObject.getObject(Context.getProductList(), productlistsids_);
            mpv5.YabsViewProxy.instance().addTab(l);
        } catch (NodataFoundException nodataFoundException) {
            Popup.error(nodataFoundException);
        }

    }//GEN-LAST:event_button_listeditActionPerformed

    private void imageprevMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageprevMouseClicked
        if (currentImageFile != null) {
            FileDirectoryHandler.open(currentImageFile);
        }
    }//GEN-LAST:event_imageprevMouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        prizes.add(new ProductPricePanel(BigDecimal.ZERO, BigDecimal.ZERO));
        prizes.validate();
        prizes.repaint();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        List<ProductPrice> pp = dataOwner.findProductPrices();
        for (int i = 0; i < pp.size(); i++) {
            ProductPrice productPrice = pp.get(i);
            productPrice.delete();
        }

        prizes.removeAll();
        setDataOwner(dataOwner, true);
        prizes.validate();
        prizes.repaint();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        recalc();
    }//GEN-LAST:event_jButton3ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addedby;
    private javax.swing.JButton addfile;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton button_groupadd;
    private javax.swing.JButton button_listedit;
    private javax.swing.JButton button_order;
    private javax.swing.JButton button_preview;
    private mpv5.ui.beans.LabeledTextField calcFactor;
    private mpv5.ui.beans.LabeledTextField calcResult;
    private mpv5.ui.beans.LabeledTextField cname;
    private mpv5.ui.beans.LabeledTextField cnumber;
    private mpv5.ui.beans.LabeledCombobox contactname1;
    private javax.swing.JTable dataTable;
    private javax.swing.JTextArea description;
    private mpv5.ui.beans.LabeledTextField ean;
    private mpv5.ui.beans.LabeledTextField extvalue;
    private mpv5.ui.beans.LabeledCombobox familyselect;
    private mpv5.ui.beans.MPCombobox groupnameselect;
    private javax.swing.JLabel imageprev;
    private javax.swing.JPanel imgpanel;
    private javax.swing.JCheckBox inventoryDisabled;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel leftpane;
    private mpv5.ui.beans.LabeledTextField netvalue;
    private javax.swing.JLabel path;
    private mpv5.ui.beans.PrinitingComboBox prinitingComboBox1;
    private javax.swing.JPanel prizes;
    private mpv5.ui.beans.LabeledTextField reference;
    private javax.swing.JButton removefile;
    private javax.swing.JPanel rightpane;
    private mpv5.ui.beans.LabeledCombobox selecttax;
    private mpv5.ui.beans.LabeledTextField stack;
    private mpv5.ui.beans.LabeledCombobox stype;
    private javax.swing.JPanel supplierpanel;
    private mpv5.ui.beans.LabeledTextField threshold;
    private javax.swing.JPanel toolbarpane;
    private mpv5.ui.beans.LabeledTextField unit;
    private mpv5.ui.beans.LabeledTextField url;
    // End of variables declaration//GEN-END:variables
    public String cname_;
    public String cnumber_;
    public String description_;
    public String hierarchiepath_;
    public int intaddedby_;
    public int ids_;
    public Date dateadded_;
    public Group group_;
    public int productgroupsids_ = 1;
    public int productlistsids_ = 0;
    public int taxids_;
    public int inttype_;
    public int manufacturersids_;
    public int suppliersids_;
    public BigDecimal externalnetvalue_;
    public BigDecimal internalnetvalue_;
    public String measure_ = "";
    public String url_ = "";
    public String ean_ = "";
    public String defaultimage_ = "";
    public String reference_ = "";//herstellernummer
    public BigDecimal stockvalue_;
    public BigDecimal thresholdvalue_;
    public int intinventorytype_;

    @Override
    public boolean collectData() {
        if (cname.getText() != null && cname.getText().length() > 0) {

            cnumber_ = cnumber.get_Text();
            Component[] supplierpanels = supplierpanel.getComponents();
            suppliersids_ = -1;
            for (int i = 0; i < supplierpanels.length; i++) {
                ProductPanelContactSub sub = (ProductPanelContactSub) supplierpanels[i];

                if (sub.getContact() != null) {
                    if (sub.isDefault()) {
                        suppliersids_ = sub.getContact().__getIDS();
                    }
                }
            }

//            try {
//                //main supplier here
//                suppliersids_ = Integer.valueOf(contactname.getSelectedItem().getId());
//            } catch (Exception numberFormatException) {
//                //Contact not set
////                Log.Debug(this, numberFormatException.getMessage());
//            }

            try {
                manufacturersids_ = Integer.valueOf(contactname1.getSelectedItem().getId());
            } catch (Exception numberFormatException) {
                //Contact not set
//                Log.Debug(this, numberFormatException.getMessage());
            }

            try {
                productgroupsids_ = Integer.valueOf(familyselect.getSelectedItem().getId());
            } catch (Exception e) {
                Log.Debug(e);
                productgroupsids_ = 1;
            }

             if (groupnameselect.getSelectedItem() != null) {
              try {
                  int group = Integer.valueOf(groupnameselect.getSelectedItem().getId());
                  group_ = (Group) DatabaseObject.getObject(Context.getGroup(), group);
                  Log.Debug(this, groupnameselect.getSelectedItem().getId());
              } catch (NodataFoundException ex) {
                  Log.Debug(this, ex);
              }
          }

            if (dateadded_ == null) {
                dateadded_ = new Date();
            }
            try {
                intaddedby_ = User.getUserId(addedby.getText());
                description_ = description.getText();
                cname_ = cname.getText();

                measure_ = unit.getText();
                url_ = url.getText();
                ean_ = ean.getText();
                reference_ = reference.getText();
            } catch (Exception e) {
                Log.Debug(e);
            }

            try {
                internalnetvalue_ = FormatNumber.parseDezimal(netvalue.getText());
            } catch (NumberFormatException numberFormatException) {
                internalnetvalue_ = BigDecimal.ZERO;
            }

            try {
                externalnetvalue_ = FormatNumber.parseDezimal(extvalue.getText());
            } catch (NumberFormatException numberFormatException) {
                externalnetvalue_ = BigDecimal.ZERO;
            }

            try {
                taxids_ = Integer.valueOf(selecttax.getSelectedItem().getId());
            } catch (Exception numberFormatException) {
                Log.Debug(numberFormatException);
                taxids_ = 1;
            }
            try {
                inttype_ = Integer.valueOf(stype.getSelectedItem().getId());
            } catch (NumberFormatException numberFormatException) {
            }

            stockvalue_ = stack.getValue(new BigDecimal(1d));
            thresholdvalue_ = threshold.getValue(new BigDecimal(1d));
            if (inventoryDisabled.isSelected()) {
                intinventorytype_ = 0;
            } else {
                intinventorytype_ = 1;
            }

            return true;
        } else {
            showRequiredFields();
            return false;
        }
    }

    @Override
    public void exposeData() {

        cname.setText(cname_);
        cnumber.setText(cnumber_);
        old_cnumber = cnumber_;
        description.setText(description_);
        stype.setSelectedItem(inttype_);
        try {
            ProductGroup g = (ProductGroup) DatabaseObject.getObject(Context.getProductGroup(), productgroupsids_);
            Log.Print(g);
            familyselect.setModel(g);
            path.setText(g.__getHierarchypath());
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        try {
            Group g = group_;
            groupnameselect.setModel(g);
        } catch (Exception ex) {
            Log.Debug(this, ex.getMessage());
        }

        addedby.setText(User.getUsername(intaddedby_));

        supplierpanel.removeAll();

        try {
            if (suppliersids_ > 0) {
                supplierpanel.add(new ProductPanelContactSub((Contact) Contact.getObject(Context.getSupplier(), suppliersids_), true));
            } else {
                supplierpanel.add(new ProductPanelContactSub(null, true));
            }
        } catch (NodataFoundException nodataFoundException) {
            supplierpanel.add(new ProductPanelContactSub(null, true));
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    List<ProductsToSuppliers> supps = Contact.getReferencedObjects(dataOwner, Context.getProductsToSuppliers(), new ProductsToSuppliers());

                    for (int i = 0; i < supps.size(); i++) {
                        Contact contact = (Contact) Contact.getObject(Context.getContact(), supps.get(i).__getContactsids());
                        supplierpanel.add(new ProductPanelContactSub(contact, false));
                    }

                    supplierpanel.validate();
                } catch (NodataFoundException nodataFoundException) {
                }
            }
        };
        SwingUtilities.invokeLater(runnable);

        if (manufacturersids_ > 0) {
            try {
                Contact owner1 = (Contact) DatabaseObject.getObject(Context.getContact(), manufacturersids_);
                contactname1.setModel(owner1);
//                contactcity1.setText(owner1.__getCity());
//                contactcompany1.setText(owner1.__getCompany());
//                contactid1.setText(String.valueOf(owner1.__getCNumber()));
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        } else {
            contactname1.setModel(new Object[0][0]);
//            contactcity1.setText("");
//            contactcompany1.setText("");
//            contactid1.setText(String.valueOf(""));
        }

        unit.setText(measure_);
        url.setText(url_);
        ean.setText(ean_);
        reference.setText(reference_);

        netvalue.setText(FormatNumber.formatDezimal(internalnetvalue_));
        extvalue.setText(FormatNumber.formatDezimal(externalnetvalue_));
        selecttax.setSelectedItem(taxids_);
        calcResult.setText(FormatNumber.formatDezimal(externalnetvalue_.multiply(Tax.getTaxValue(taxids_))));
        calcFactor.setText(0d);

        stack.setText(FormatNumber.formatDezimal(stockvalue_));
        threshold.setText(FormatNumber.formatDezimal(thresholdvalue_));
        inventoryDisabled.setSelected(intinventorytype_ == 0);

        fillFiles();
        setIcon();

        button_listedit.setEnabled(productlistsids_ > 0);
        final Product b = dataOwner;
        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                try {
                    prizes.removeAll();
                    List<ProductPrice> pp = b.findProductPrices();
                    for (int i = 0; i < pp.size(); i++) {
                        ProductPrice productPrice = pp.get(i);
                        prizes.add(new ProductPricePanel(productPrice.getExternalnetvalue(), productPrice.getMincountvalue()));
                    }
                    prizes.validate();
                    prizes.repaint();
                } catch (Exception e) {
                    Log.Debug(this, e.getMessage());
                }
            }
        };
        SwingUtilities.invokeLater(runnable2);
        double val = extvalue.getValue(0d);
        calcResult.setText(FormatNumber.formatDezimal(new BigDecimal(val).multiply(Tax.getCalculationValue(taxids_))));
    }

    @Override
    public void refresh() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    try {
                        groupnameselect.setModel(MPComboBoxModelItem.toModel(DatabaseObject.getObject(Context.getGroup(), mpv5.db.objects.User.getCurrentUser().__getGroupsids())));
                        groupnameselect.setSelectedIndex(0);
                    } catch (NodataFoundException nodataFoundException) {
                    }
                    sp.refresh();

                    try {
                        familyselect.setModel(DatabaseObject.getObject(Context.getProductGroup(), mpv5.db.objects.User.getCurrentUser().__getIntdefaultaccount()));
                    } catch (NodataFoundException nodataFoundException) {
                    }
                    fillFiles();

                    stype.setModel(Product.getTypes(), MPComboBoxModelItem.COMPARE_BY_ID, new java.util.Vector<Integer>());
                    stype.setSelectedItem(type);
                    selecttax.triggerSearch();
                    selecttax.setSelectedItem(taxids_);

                    if (dataOwner != null) {
                        setDataOwner(dataOwner, true);
                    }
                } catch (Exception e) {
                    Log.Debug(this, e);
                }
            }
        };

        SwingUtilities.invokeLater(runnable);
    }

    @Override
    public void paste(DatabaseObject... dbos) {
        for (DatabaseObject dbo : dbos) {
            if (dbo.getDbIdentity().equals(Context.getProduct().getDbIdentity())) {
                setDataOwner(dbo, true);
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE.toString(), Color.RED);
            }
        }
    }

    @Override
    public void showSearchBar(boolean show) {
        leftpane.removeAll();
        if (show) {
            leftpane.add(sp, BorderLayout.CENTER);
            sp.search();
        }
        validate();
    }

    @Override
    public void actionAfterSave() {
        getSuppliers();
        Component[] ps = prizes.getComponents();
        try {
            List<ProductPrice> pp = dataOwner.findProductPrices();
            HashMap<BigDecimal, ProductPrice> map = new HashMap<BigDecimal, ProductPrice>();
            for (int i = 0; i < pp.size(); i++) {
                ProductPrice productPrice = pp.get(i);
                map.put(productPrice.getMincountvalue(), productPrice);
            }
            for (int i = 0; i < ps.length; i++) {
                ProductPricePanel component = (ProductPricePanel) ps[i];
                if (component.getMin().intValue() != 0) {
                    if (map.containsKey(component.getMin())) {
                        ProductPrice pz = map.remove(component.getMin());
                        pz.setExternalnetvalue(component.getPrize());
                        pz.save(true);
                    } else {
                        ProductPrice pp3 = new ProductPrice();
                        pp3.setProduct(dataOwner);
                        pp3.setMincountvalue(component.getMin());
                        BigDecimal v3 = component.getPrize();
                        pp3.setExternalnetvalue(v3);
                        pp3.setInternalnetvalue(v3);
                        pp3.save(true);
                    }
                } else {
                    Log.Debug(this, "Ignoring min-0 Productprice");
                    prizes.remove(component);
                }
            }
            for (ProductPrice p : map.values()) {//remove unused
                Log.Debug(this, "Deleting obselete Productprice");
                p.delete();
            }

        } catch (Exception e) {
            Log.Debug(this, e.getMessage());
        }
        prizes.removeAll();
        try {
            List<ProductPrice> pp = dataOwner.findProductPrices();
            for (int i = 0; i < pp.size(); i++) {
                ProductPrice productPrice = pp.get(i);
                prizes.add(new ProductPricePanel(productPrice.getExternalnetvalue(), productPrice.getMincountvalue()));
            }

        } catch (Exception e) {
            Log.Debug(this, e.getMessage());
        }
        prizes.validate();
    }

    @Override
    public void actionAfterCreate() {
        getSuppliers();
        sp.refresh();
    }

    @Override
    public void changeSelection(MPComboBoxModelItem to, Context c) {
    }

    private void preview() {
        PreviewWaiter pr;
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
                pr = new PreviewWaiter();
                pr.setDataOwner(dataOwner);
                new Job(Export.createFile(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.__getInttype()), dataOwner), pr).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
            }
        }
    }

    private void preview_order() {
        PreviewWaiter pr;
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), Constants.TYPE_PRODUCT_ORDER)) {
                pr = new PreviewWaiter();
                pr.setDataOwner(dataOwner);
                new Job(Export.createFile(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), Constants.TYPE_PRODUCT_ORDER), dataOwner), pr).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
            }
        }
    }

    private void preloadTemplate() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (dataOwner.__getInttype() == Product.TYPE_PRODUCT) {
                    TemplateHandler.loadTemplateFor(button_preview, Long.valueOf(dataOwner.templateGroupIds()), Constants.TYPE_PRODUCT);
                    TemplateHandler.loadTemplateFor(button_order, Long.valueOf(dataOwner.templateGroupIds()), Constants.TYPE_PRODUCT_ORDER);
                } else {
                    TemplateHandler.loadTemplateFor(button_preview, Long.valueOf(dataOwner.templateGroupIds()), Constants.TYPE_SERVICE);
                }
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public void actionBeforeCreate() {
        if (old_cnumber.equals(cnumber_)) {
            cnumber_ = null;
        }
    }

    @Override
    public void actionBeforeSave() throws ChangeNotApprovedException {
        if (dataOwner.isExisting()) {
            if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "dowarnings")) {

                if (!Popup.Y_N_dialog(Messages.REALLY_CHANGE)) {
                    throw new ChangeNotApprovedException(dataOwner);
                }
            }
        }
    }

    @Override
    public void mail() {
        MailMessage m = null;
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {

                try {
                    Contact cont = (Contact) Popup.SelectValue(Context.getContact());
                    Export.mail(TemplateHandler.loadTemplate(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype()), dataOwner, cont, null);
                } catch (Exception ex) {
                    Log.Debug(ex);
                }
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ") : " + TemplateHandler.getName(Constants.TYPE_PRODUCT));
            }
        }
    }

    @Override
    public void print() {
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
                Export.print(TemplateHandler.loadTemplate(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype()), dataOwner);
            } else {
                Notificator.raiseNotification(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ") : " + TemplateHandler.getName(Constants.TYPE_PRODUCT), false, false, dataOwner);
                mpv5.utils.export.Export.print(this);
            }
        } else {
            mpv5.utils.export.Export.print(this);
        }
    }

    private void getSuppliers() {
        QueryCriteria c = new QueryCriteria("productsids", dataOwner.__getIDS());
        QueryHandler.instanceOf().clone(Context.getProductsToSuppliers()).delete(c);
        Component[] supplierpanels = supplierpanel.getComponents();
        for (int i = 0; i < supplierpanels.length; i++) {
            ProductPanelContactSub sub = (ProductPanelContactSub) supplierpanels[i];
            if (sub.getContact() != null) {
                if (sub.isDefault()) {
                } else {
                    ProductsToSuppliers p = new ProductsToSuppliers();
                    p.setProductsids(dataOwner.__getIDS());
                    p.setContactsids(sub.getContact().__getIDS());
                    p.save(true);
                }
            }
        }
    }

    private void setIcon() {
        if (defaultimage_ != null && defaultimage_.equals(lastimage)) {
            return;
        }
        lastimage = defaultimage_;
        if (defaultimage_ != null && !defaultimage_.equals("")) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    QueryHandler q = QueryHandler.instanceOf().clone(Context.getFiles());
                    currentImageFile = q.retrieveFile(defaultimage_, FileDirectoryHandler.getTempFile("image"));
                    if (currentImageFile != null) {
                        try {
                            MPIcon ic = new MPIcon(currentImageFile.toURI().toURL());
                            imageprev.setIcon(ic.getIcon(imgpanel.getHeight(), imgpanel.getHeight()));
                        } catch (Exception ef) {
                            imageprev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xcf.png")));
                            Log.Debug(this, ef.getMessage());
                        }
                    } else {
                        imageprev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xcf.png")));
                    }
                }
            };
            SwingUtilities.invokeLater(runnable);
        } else {
            imageprev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xcf.png")));
            currentImageFile = null;
        }
    }

    private void save() {
        if (dataOwner != null) {
            dataOwner.getPanelData(this);
            dataOwner.save(false);
        }
    }

    @Override
    public void pdf() {
        if (dataOwner != null && dataOwner.isExisting()) {
            dataOwner.toPdf(true);
        }
    }

    @Override
    public void odt() {
        if (dataOwner != null && dataOwner.isExisting()) {
            dataOwner.toOdt(true);
        }
    }
}
