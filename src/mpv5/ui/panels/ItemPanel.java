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

import enoa.handler.TemplateHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import mpv5.YabsViewProxy;
import mpv5.db.common.*;
import mpv5.db.objects.ActivityList;
import mpv5.db.objects.Product;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.Item;
import mpv5.db.objects.MailMessage;
import mpv5.db.objects.ProductList;
import mpv5.db.objects.ProductlistSubItem;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Template;
import mpv5.logging.Log;
import mpv5.ui.dialogs.BigPopup;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Groups;
import mpv5.ui.dialogs.subcomponents.ProductSelectDialog3;
import mpv5.ui.popups.FileTablePopUp;
import mpv5.ui.toolbars.DataPanelTB;
import mpv5.db.objects.User;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.Constants;
import mpv5.handler.FormatHandler;
import mpv5.ui.beans.MPCBSelectionChangeReceiver;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.ScheduleDayEvent;
import mpv5.ui.dialogs.Search2;
import mpv5.ui.dialogs.subcomponents.*;
import mpv5.ui.misc.MPTable;
import mpv5.ui.misc.Position;
import mpv5.ui.popups.TablePopUp;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.date.DateConverter;
import mpv5.utils.export.Export;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.jobs.Job;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.renderer.ButtonEditor;
import mpv5.utils.renderer.ButtonRenderer;
import mpv5.utils.renderer.TableCellRendererForDezimal;
import mpv5.utils.renderer.TableTabAction;
import mpv5.utils.renderer.TextAreaCellEditor;
import mpv5.utils.renderer.TextAreaCellRenderer;
import mpv5.utils.tables.TableFormat;
import mpv5.ui.misc.TableViewPersistenceHandler;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.renderer.LazyCellEditor;
import mpv5.utils.renderer.LazyCellRenderer;
import mpv5.utils.renderer.TableCellRendererForProducts;
import mpv5.utils.tables.DynamicTableCalculator;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 *
 */
public class ItemPanel extends javax.swing.JPanel implements DataPanel, MPCBSelectionChangeReceiver, ExportablePanel {

    private static final long serialVersionUID = 1L;
    private Item dataOwner;
    private DataPanelTB tb;
    private SearchPanel sp;
    private DynamicTableCalculator itemMultiplier;
    private DynamicTableCalculator taxcalculator;
    private DynamicTableCalculator netCalculator;
    private DynamicTableCalculator disccalculator;
    private boolean loading = true;
    private java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();

    /**
     * Creates new form ContactPanel
     *
     * @param context
     * @param type
     */
    public ItemPanel(Context context, int type) {
        initComponents();
        jButton1.setEnabled(MPSecurityManager.checkAdminAccess());

        inttype_ = type;
        sp = new SearchPanel(context, this);
        sp.setVisible(true);
        tb = new mpv5.ui.toolbars.DataPanelTB(this);
        toolbarpane.add(tb, BorderLayout.CENTER);
        dataOwner = (Item) DatabaseObject.getObject(context);
        if (type >= 0) {
            dataOwner.setInttype(type);
            this.type.setText(Item.getTypeString(type));
        } else {
            this.type.setText("");
        }

        refresh();
//        shipping.set_ValueClass(Double.class);

//        checkb_pront_oc.setSelected(
//            mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(
//                "org.openyabs.uiproperty", 
//                "orderconfirmationalways"));
        addedby.setText(mpv5.db.objects.User.getCurrentUser().getName());
        contactname.setSearchEnabled(true);
        contactname.setContext(Context.getCustomer());
        contactname.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final MPComboBoxModelItem item = contactname.getSelectedItem();
                if (item != null && item.isValid()) {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Contact dbo = (Contact) DatabaseObject.getObject(Context.getContact(), Integer.valueOf(item.getId()));
                                contactcity.setText(dbo.__getCity());
                                contactcompany.setText(dbo.__getCompany());
                                contactid.setText(dbo.__getCNumber());
                            } catch (NodataFoundException ex) {
                            }
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                }
            }
        });

        accountselect.setContext(Context.getAccounts());
        accountselect.setSearchEnabled(true);
        groupnameselect.setContext(Context.getGroup());
        groupnameselect.setSearchEnabled(true);

        date1.setDate(new Date());
        try {
            date3.setDate(DateConverter.addDays(new Date(), Integer.valueOf(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("bills.warn.days"))));
            date2.setDate(new Date());
        } catch (Exception e) {
            date3.setDate(DateConverter.addDays(new Date(), 14));
            date2.setDate(new Date());
        }
//        itemtable.getTableHeader().addMouseListener(new MouseListener() {
//
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (e.getButton() == MouseEvent.BUTTON2) {
//                    MPTableModel m = (MPTableModel) itemtable.getModel();
//                    if (m.getRowCount() > 0) {
//                        m.addRow(5);
//                    } else {
//                        itemtable.setModel(SubItem.toModel(new SubItem[]{
//                                    SubItem.getDefaultItem(), SubItem.getDefaultItem(),
//                                    SubItem.getDefaultItem(), SubItem.getDefaultItem(),
//                                    SubItem.getDefaultItem(), SubItem.getDefaultItem()
//                                }));
//                        formatTable();
//                    }
//                } else if (e.getButton() == MouseEvent.BUTTON3) {
//                    MPTableModel m = (MPTableModel) itemtable.getModel();
//                    Product p = (Product) Popup.SelectValue(Context.getProduct());
//                    if (p != null) {
//                        int row = m.getLastValidRow(new int[]{4});
//                        m.setRowAt(new SubItem(p).getRowData(row), row + 1, 1);
//                    }
//                }
//            }
//
//            @Override
//            public void mousePressed(MouseEvent e) {
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//            }
//
//            @Override
//            public void mouseEntered(MouseEvent e) {
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//            }
//        });

        InputMap inputMap = itemtable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        Action oldAction = itemtable.getActionMap().get(inputMap.get(tab));
        itemtable.getActionMap().put(inputMap.get(tab), new TableTabAction(notes, oldAction, false));
        KeyStroke shifttab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);
        oldAction = itemtable.getActionMap().get(inputMap.get(shifttab));
        itemtable.getActionMap().put(inputMap.get(shifttab), new TableTabAction(date3, oldAction, true));

//        KeyStroke right = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
//        oldAction = itemtable.getActionMap().get(inputMap.get(right));
//        itemtable.getActionMap().put(inputMap.get(right), new TableTabAction(notes, oldAction, false));
//        KeyStroke left = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
//        oldAction = itemtable.getActionMap().get(inputMap.get(left));
//        itemtable.getActionMap().put(inputMap.get(left), new TableTabAction(date3, oldAction, true));

        number.setSearchOnEnterEnabled(true);
        number.setParent(this);
        number.setSearchField("cname");
        number.setContext(Context.getItem());

        final DataPanel p = this;
        status.getComboBox().addActionListener(new ActionListener() {

            Item dato = (Item) getDataOwner();

            public void actionPerformed(ActionEvent e) {
                if (dato.__getInttype() == Item.TYPE_BILL && !loading && dataOwner.isExisting() && Integer.valueOf(status.getSelectedItem().getId()) == Item.STATUS_PAID && mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "autocreaterevenue")) {
                    if (Popup.Y_N_dialog(Messages.BOOK_NOW)) {

                        if (dato.getPanelData(p) && dato.save()) {
                            try {
                                actionAfterSave();
                                dato.createRevenue();
                            } catch (Exception ef) {
                                Log.Debug(this, ef);
                            }
                        } else {
                            showRequiredFields();
                        }
                    }
                }

                if (dato.__getInttype() == Item.TYPE_BILL && !loading && dataOwner.isExisting()
                        && Integer.valueOf(status.getSelectedItem().getId()) == Item.STATUS_PAID) {

                    //set dateend
                    date3.setDate(new Date());
                }
            }
        });

        labeledCombobox1.setContext(Context.getMessage());
        labeledCombobox1.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    MailMessage d = (MailMessage) DatabaseObject.getObject(Context.getMessage(), Integer.valueOf(labeledCombobox1.getSelectedItem().getId()));
                    notes.setText(d.__getDescription());
                } catch (Exception ex) {
                }
            }
        });
        labeledCombobox1.setSearchOnEnterEnabled(true);

        ((MPTable) dataTable).setPersistanceHandler(new TableViewPersistenceHandler((MPTable) dataTable, this));
        ((MPTable) proptable).setPersistanceHandler(new TableViewPersistenceHandler((MPTable) proptable, this));
        jSplitPane1.setDividerLocation((User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty.itempanel.divider1.dividerLocation", 150)));
        jSplitPane2.setDividerLocation((User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty.itempanel.divider2.dividerLocation", 0)));

    }

    /**
     *
     * @param items
     */
    public ItemPanel(Context items) {
        this(items, -1);
    }

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean populate) {
        loading = true;

        if (object instanceof Item) {
            dataOwner = (Item) object;
            if (populate) {
                dataOwner.setPanelData(this);
                inttype_ = dataOwner.__getInttype();
                button_reminders.setEnabled(inttype_ == Item.TYPE_BILL);
                button_schedule.setEnabled(inttype_ == Item.TYPE_BILL);
                toorder.setEnabled(inttype_ != Item.TYPE_ORDER && inttype_ != Item.TYPE_BILL);
                toinvoice.setEnabled(inttype_ != Item.TYPE_BILL);
                type.setText(Item.getTypeString(inttype_));
                //            typelabel.setIcon(dataOwner.getIcon());
                this.exposeData();

                setTitle();

                tb.setFavourite(Favourite.isFavourite(object));
                tb.setEditable(!object.isReadOnly());


                itemtable.setModel(SubItem.toModel(((Item) object).getSubitems()));
                if (((MPTableModel) itemtable.getModel()).getEmptyRows(new int[]{4}) < 2) {
                    ((MPTableModel) itemtable.getModel()).addRow(1);
                }

                omodel = (MPTableModel) itemtable.getModel();

                formatTable();
                try {
                    ((MPTableModel) itemtable.getModel()).fireTableCellUpdated(0, 0);
                } catch (Exception e) {
                }
                if (object.isReadOnly()) {
                    Popup.notice(Messages.LOCKED_BY);
                }
                button_preview.setEnabled(false);
                preloadTemplates();
                validate();
            }
        } else if (object instanceof SubItem) {
            Item i;
            try {
                i = (Item) DatabaseObject.getObject(Context.getItem(), ((SubItem) object).__getItemsids());
                setDataOwner(i, populate);
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        }

        properties();
        loading = false;

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
                    ((JFrame) this.getRootPane().getParent()).setTitle(title1);
                }
            }
            if (jTabbedPane != null) {
                jTabbedPane.setTitleAt(jTabbedPane.getSelectedIndex(), title1);
            }
        }
    }

    @Override
    public void showRequiredFields() {
        TextFieldUtils.blink(contactname.getComboBox(), Color.RED);
        contactname.requestFocus();
//        Popup.notice(Messages.SELECT_A_CONTACT);
    }

    private void addFile() {
        if (dataOwner.isExisting()) {
            DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY);
            if (d.chooseFile()) {
                String s = Popup.Enter_Value(Messages.ENTER_A_DESCRIPTION);
                if (s != null) {
                    QueryHandler.instanceOf().clone(Context.getFiles(), this).insertFile(d.getFile(), dataOwner, QueryCriteria.getSaveStringFor(s));
                }
            }
        }
    }

    private void deleteFile() {
        if (dataOwner.isExisting()) {
            try {
                QueryHandler.instanceOf().clone(Context.getFiles()).removeFile(dataTable.getModel().getValueAt(dataTable.getSelectedRow(), 0).toString());
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
            fillFiles();
        }
    }

    private void fileTableClicked(MouseEvent evt) {
        if (evt.getClickCount() > 1) {
            FileDirectoryHandler.open(QueryHandler.instanceOf().clone(Context.getFiles()).
                    retrieveFile(dataTable.getModel().getValueAt(dataTable.getSelectedRow(), 0).
                    toString(), new File(FileDirectoryHandler.getTempDir() + dataTable.getModel().
                    getValueAt(dataTable.getSelectedRow(), 1).toString())));
        } else if (evt.getClickCount() == 1 && evt.getButton() == MouseEvent.BUTTON3) {

            JTable source = (JTable) evt.getSource();
            int row = source.rowAtPoint(evt.getPoint());
            int column = source.columnAtPoint(evt.getPoint());

            if (!source.isRowSelected(row)) {
                source.changeSelection(row, column, false, false);
            }

            FileTablePopUp.instanceOf(dataTable).show(source, evt.getX(), evt.getY());
        }
    }

    private void fillFiles() {

        Runnable runnable = new Runnable() {

            public void run() {
                Context c = Context.getFilesToItems();
                c.addReference(Context.getFiles().getDbIdentity(), "cname", "filename");
                Object[][] data = new DatabaseSearch(c).getValuesFor(Context.DETAILS_FILES_TO_ITEMS, "itemsids", dataOwner.__getIDS());

                dataTable.setModel(new MPTableModel(data, Headers.FILE_REFERENCES.getValue()));
                TableFormat.stripFirstColumn(dataTable);
            }
        };
        new Thread(runnable).start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        leftpane = new javax.swing.JPanel();
        rightpane = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        number = new mpv5.ui.beans.LabeledTextField();
        addedby = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        button_order2 = new javax.swing.JButton();
        status = new mpv5.ui.beans.LabeledCombobox();
        accountselect = new mpv5.ui.beans.LabeledCombobox();
        groupnameselect = new mpv5.ui.beans.MPCombobox();
        staus_icon = new javax.swing.JLabel();
        type = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jToolBar1 = new javax.swing.JToolBar();
        toorder = new javax.swing.JButton();
        toinvoice = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        typelabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        button_reminders = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        button_schedule = new javax.swing.JButton();
        button_preview = new javax.swing.JButton();
        button_deliverynote = new javax.swing.JButton();
        button_orderconf = new javax.swing.JButton();
        checkb_pront_oc = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        contactname = new mpv5.ui.beans.LabeledCombobox();
        contactcity = new javax.swing.JTextField();
        contactcompany = new javax.swing.JTextField();
        contactid = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        date1 = new mpv5.ui.beans.LabeledDateChooser();
        date2 = new mpv5.ui.beans.LabeledDateChooser();
        date3 = new mpv5.ui.beans.LabeledDateChooser();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel6 = new javax.swing.JPanel();
        jToolBar2 = new alignRightToolbar();
        jLabel1 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        netvalue = new javax.swing.JLabel();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        jLabel2 = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        taxvalue = new javax.swing.JLabel();
        jSeparator10 = new javax.swing.JToolBar.Separator();
        jLabel3 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        value = new javax.swing.JLabel();
        jSeparator11 = new javax.swing.JToolBar.Separator();
        jLabel5 = new javax.swing.JLabel();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        discount = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        itemtable = new javax.swing.JTable();
        itemPanel = new javax.swing.JPanel();
        addItem = new javax.swing.JButton();
        delItem = new javax.swing.JButton();
        upItem = new javax.swing.JButton();
        upItem1 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        class NoTabTextArea extends JTextPane {
            private static final long serialVersionUID = 1L;
            protected void processComponentKeyEvent( KeyEvent e ) {
                if ( e.getID() == KeyEvent.KEY_PRESSED &&
                    e.getKeyCode() == KeyEvent.VK_TAB ) {
                    e.consume();
                    if (e.isShiftDown()) {
                        transferFocusBackward();
                    } else {
                        itemtable.requestFocusInWindow();
                    }
                } else {
                    super.processComponentKeyEvent( e );
                }
            }
        }
        notes = new NoTabTextArea();
        labeledCombobox1 = new mpv5.ui.beans.LabeledCombobox();
        jScrollPane4 = new javax.swing.JScrollPane();
        proptable = new  mpv5.ui.misc.MPTable(this) {
            private static final long serialVersionUID = 1L;
            public Component prepareRenderer(TableCellRenderer renderer,
                int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent)c;
                    jc.setToolTipText(String.valueOf(getValueAt(rowIndex, vColIndex)));
                }
                return c;
            }
        };
        jPanel8 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        dataTable = new  mpv5.ui.misc.MPTable(this) {
            private static final long serialVersionUID = 1L;
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

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ItemPanel.border.title_1"))); // NOI18N
        setName("ItemPanel"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        leftpane.setName("leftpane"); // NOI18N
        leftpane.setLayout(new java.awt.BorderLayout());
        add(leftpane, java.awt.BorderLayout.WEST);

        rightpane.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ItemPanel.rightpane.border.title"))); // NOI18N
        rightpane.setName("rightpane"); // NOI18N

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(843, 100));

        number.set_Label(bundle.getString("ItemPanel.number._Label")); // NOI18N
        number.setFocusable(false);
        number.setFont(number.getFont());
        number.setName("number"); // NOI18N

        addedby.setFont(addedby.getFont());
        addedby.setText(bundle.getString("ItemPanel.addedby.text")); // NOI18N
        addedby.setToolTipText(bundle.getString("ItemPanel.addedby.toolTipText")); // NOI18N
        addedby.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        addedby.setName("addedby"); // NOI18N

        jLabel4.setFont(jLabel4.getFont());
        jLabel4.setText(bundle.getString("ItemPanel.jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        button_order2.setFont(button_order2.getFont().deriveFont(button_order2.getFont().getStyle() & ~java.awt.Font.BOLD, button_order2.getFont().getSize()-2));
        button_order2.setText(bundle.getString("ItemPanel.button_order2.text")); // NOI18N
        button_order2.setFocusable(false);
        button_order2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_order2.setName("button_order2"); // NOI18N
        button_order2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_order2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_order2ActionPerformed(evt);
            }
        });

        status.set_Label(bundle.getString("ItemPanel.status._Label")); // NOI18N
        status.setName("status"); // NOI18N

        accountselect.set_Label(bundle.getString("ItemPanel.accountselect._Label")); // NOI18N
        accountselect.setName("accountselect"); // NOI18N
        accountselect.setSearchOnEnterEnabled(false);

        groupnameselect.setName("groupnameselect"); // NOI18N

        staus_icon.setText(bundle.getString("ItemPanel.staus_icon.text")); // NOI18N
        staus_icon.setName("staus_icon"); // NOI18N

        type.setBackground(new java.awt.Color(255, 255, 255));
        type.setFont(type.getFont().deriveFont(type.getFont().getStyle() | java.awt.Font.BOLD, type.getFont().getSize()+4));
        type.setForeground(new java.awt.Color(26, 34, 70));
        type.setText(bundle.getString("ItemPanel.type.text")); // NOI18N
        type.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        type.setMaximumSize(new java.awt.Dimension(250, 23));
        type.setName("type"); // NOI18N
        type.setPreferredSize(new java.awt.Dimension(200, 24));

        jButton1.setText(bundle.getString("ItemPanel.jButton1.text")); // NOI18N
        jButton1.setName(bundle.getString("ItemPanel.jButton1.name")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(accountselect, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                    .addComponent(type, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(number, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(status, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(57, 57, 57)
                        .addComponent(button_order2))
                    .addComponent(groupnameselect, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(staus_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(49, 49, 49)
                .addComponent(addedby, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(addedby, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(status, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(type, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(accountselect, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(button_order2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(number, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(groupnameselect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(61, 61, 61))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(staus_icon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(122, 122, 122))
        );

        jToolBar1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        toorder.setText(bundle.getString("ItemPanel.toorder.text")); // NOI18N
        toorder.setEnabled(false);
        toorder.setFocusable(false);
        toorder.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toorder.setName(bundle.getString("ItemPanel.toorder.name")); // NOI18N
        toorder.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toorderActionPerformed(evt);
            }
        });
        jToolBar1.add(toorder);

        toinvoice.setText(bundle.getString("ItemPanel.toinvoice.text")); // NOI18N
        toinvoice.setEnabled(false);
        toinvoice.setFocusable(false);
        toinvoice.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toinvoice.setName(bundle.getString("ItemPanel.toinvoice.name")); // NOI18N
        toinvoice.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toinvoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toinvoiceActionPerformed(evt);
            }
        });
        jToolBar1.add(toinvoice);

        jSeparator4.setName("jSeparator4"); // NOI18N
        jToolBar1.add(jSeparator4);

        typelabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/22/editcopy.png"))); // NOI18N
        typelabel.setText(bundle.getString("ItemPanel.typelabel.text")); // NOI18N
        typelabel.setName("typelabel"); // NOI18N
        jToolBar1.add(typelabel);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jToolBar1.add(jSeparator1);

        button_reminders.setText(bundle.getString("ItemPanel.button_reminders.text")); // NOI18N
        button_reminders.setEnabled(false);
        button_reminders.setFocusable(false);
        button_reminders.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_reminders.setName("button_reminders"); // NOI18N
        button_reminders.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_reminders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_remindersActionPerformed(evt);
            }
        });
        jToolBar1.add(button_reminders);

        jButton2.setText(bundle.getString("ItemPanel.jButton2.text")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        jSeparator7.setName("jSeparator7"); // NOI18N
        jToolBar1.add(jSeparator7);

        button_schedule.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/kalarm.png"))); // NOI18N
        button_schedule.setText(bundle.getString("ItemPanel.button_schedule.text")); // NOI18N
        button_schedule.setEnabled(false);
        button_schedule.setName("button_schedule"); // NOI18N
        button_schedule.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                button_scheduleMouseClicked(evt);
            }
        });
        button_schedule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_scheduleActionPerformed(evt);
            }
        });
        jToolBar1.add(button_schedule);

        button_preview.setText(bundle.getString("ItemPanel.button_preview.text")); // NOI18N
        button_preview.setEnabled(false);
        button_preview.setFocusable(false);
        button_preview.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_preview.setName("button_preview"); // NOI18N
        button_preview.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_preview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_previewActionPerformed(evt);
            }
        });
        jToolBar1.add(button_preview);

        button_deliverynote.setText(bundle.getString("ItemPanel.button_deliverynote.text")); // NOI18N
        button_deliverynote.setEnabled(false);
        button_deliverynote.setFocusable(false);
        button_deliverynote.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_deliverynote.setName("button_deliverynote"); // NOI18N
        button_deliverynote.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_deliverynote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_deliverynoteActionPerformed(evt);
            }
        });
        jToolBar1.add(button_deliverynote);

        button_orderconf.setText(bundle.getString("ItemPanel.button_orderconf.text")); // NOI18N
        button_orderconf.setEnabled(false);
        button_orderconf.setFocusable(false);
        button_orderconf.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_orderconf.setName("button_orderconf"); // NOI18N
        button_orderconf.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_orderconf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_orderconfActionPerformed(evt);
            }
        });
        jToolBar1.add(button_orderconf);

        checkb_pront_oc.setText(bundle.getString("ItemPanel.checkb_pront_oc.text")); // NOI18N
        checkb_pront_oc.setEnabled(false);
        checkb_pront_oc.setFocusable(false);
        checkb_pront_oc.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        checkb_pront_oc.setName("checkb_pront_oc"); // NOI18N
        checkb_pront_oc.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(checkb_pront_oc);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ItemPanel.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        contactname.set_Label(bundle.getString("ItemPanel.contactname._Label")); // NOI18N
        contactname.setName("contactname"); // NOI18N

        contactcity.setEditable(false);
        contactcity.setText(bundle.getString("ItemPanel.contactcity.text")); // NOI18N
        contactcity.setName("contactcity"); // NOI18N

        contactcompany.setEditable(false);
        contactcompany.setText(bundle.getString("ItemPanel.contactcompany.text")); // NOI18N
        contactcompany.setName("contactcompany"); // NOI18N

        contactid.setEditable(false);
        contactid.setText(bundle.getString("ItemPanel.contactid.text")); // NOI18N
        contactid.setName("contactid"); // NOI18N

        jButton3.setText(bundle.getString("ItemPanel.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText(bundle.getString("ItemPanel.jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(contactname, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contactcity, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contactcompany, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(contactid, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addContainerGap(54, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(contactcity)
                        .addComponent(contactcompany)
                        .addComponent(contactid)
                        .addComponent(contactname, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ItemPanel.jPanel5.border.title"))); // NOI18N
        jPanel5.setName("jPanel5"); // NOI18N

        date1.set_Label(bundle.getString("ItemPanel.date1._Label")); // NOI18N
        date1.setName("date1"); // NOI18N

        date2.set_Label(bundle.getString("ItemPanel.date2._Label")); // NOI18N
        date2.setName("date2"); // NOI18N

        date3.set_Label(bundle.getString("ItemPanel.date3._Label")); // NOI18N
        date3.setName("date3"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(date1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(date2, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(date3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(date1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(date2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(date3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(0.8);
        jSplitPane2.setName("jSplitPane2"); // NOI18N
        jSplitPane2.setOneTouchExpandable(true);
        jSplitPane2.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSplitPane2PropertyChange(evt);
            }
        });

        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setPreferredSize(new java.awt.Dimension(869, 250));
        jPanel6.setLayout(new java.awt.BorderLayout());

        jToolBar2.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 1, true));
        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);
        jToolBar2.setName("jToolBar2"); // NOI18N
        jToolBar2.setPreferredSize(new java.awt.Dimension(210, 26));

        jLabel1.setText(bundle.getString("ItemPanel.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        jToolBar2.add(jLabel1);

        jSeparator3.setName("jSeparator3"); // NOI18N
        jSeparator3.setSeparatorSize(new java.awt.Dimension(15, 10));
        jToolBar2.add(jSeparator3);

        netvalue.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        netvalue.setText(bundle.getString("ItemPanel.netvalue.text")); // NOI18N
        netvalue.setName("netvalue"); // NOI18N
        jToolBar2.add(netvalue);

        jSeparator9.setName("jSeparator9"); // NOI18N
        jToolBar2.add(jSeparator9);

        jLabel2.setText(bundle.getString("ItemPanel.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        jToolBar2.add(jLabel2);

        jSeparator6.setName("jSeparator6"); // NOI18N
        jSeparator6.setSeparatorSize(new java.awt.Dimension(15, 10));
        jToolBar2.add(jSeparator6);

        taxvalue.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        taxvalue.setText(bundle.getString("ItemPanel.taxvalue.text")); // NOI18N
        taxvalue.setName("taxvalue"); // NOI18N
        jToolBar2.add(taxvalue);

        jSeparator10.setName("jSeparator10"); // NOI18N
        jToolBar2.add(jSeparator10);

        jLabel3.setText(bundle.getString("ItemPanel.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N
        jToolBar2.add(jLabel3);

        jSeparator5.setName("jSeparator5"); // NOI18N
        jSeparator5.setSeparatorSize(new java.awt.Dimension(15, 10));
        jToolBar2.add(jSeparator5);

        value.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        value.setText(bundle.getString("ItemPanel.value.text")); // NOI18N
        value.setName("value"); // NOI18N
        jToolBar2.add(value);

        jSeparator11.setName("jSeparator11"); // NOI18N
        jToolBar2.add(jSeparator11);

        jLabel5.setText(bundle.getString("ItemPanel.jLabel5.text_1")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N
        jToolBar2.add(jLabel5);

        jSeparator8.setName("jSeparator8"); // NOI18N
        jSeparator8.setSeparatorSize(new java.awt.Dimension(15, 10));
        jToolBar2.add(jSeparator8);

        discount.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        discount.setText(bundle.getString("ItemPanel.discount.text")); // NOI18N
        discount.setName("discount"); // NOI18N
        jToolBar2.add(discount);

        jPanel6.add(jToolBar2, java.awt.BorderLayout.PAGE_END);

        jPanel4.setBackground(new java.awt.Color(204, 204, 204));
        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setName("jPanel4"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        itemtable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        itemtable.setName("itemtable"); // NOI18N
        itemtable.setSelectionBackground(new java.awt.Color(161, 176, 190));
        itemtable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        itemtable.setSurrendersFocusOnKeystroke(true);
        itemtable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                itemtableMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(itemtable);
        itemtable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        itemPanel.setName("itemPanel"); // NOI18N
        itemPanel.setOpaque(false);

        addItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/add.png"))); // NOI18N
        addItem.setName("addItem"); // NOI18N
        addItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addItemActionPerformed(evt);
            }
        });

        delItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/remove.png"))); // NOI18N
        delItem.setName("delItem"); // NOI18N
        delItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delItemActionPerformed(evt);
            }
        });

        upItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/arrow-up.png"))); // NOI18N
        upItem.setName("upItem"); // NOI18N
        upItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upItemActionPerformed(evt);
            }
        });

        upItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/arrow-down.png"))); // NOI18N
        upItem1.setName("upItem1"); // NOI18N
        upItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upItem1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout itemPanelLayout = new javax.swing.GroupLayout(itemPanel);
        itemPanel.setLayout(itemPanelLayout);
        itemPanelLayout.setHorizontalGroup(
            itemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addItem, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(delItem, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(upItem, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(upItem1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        itemPanelLayout.setVerticalGroup(
            itemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemPanelLayout.createSequentialGroup()
                .addComponent(addItem)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(delItem)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(upItem)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(upItem1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(itemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 830, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(itemPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        jPanel6.add(jPanel4, java.awt.BorderLayout.CENTER);

        jSplitPane2.setLeftComponent(jPanel6);

        jPanel7.setName("jPanel7"); // NOI18N
        jPanel7.setLayout(new java.awt.BorderLayout());

        jSplitPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 1));
        jSplitPane1.setLastDividerLocation(150);
        jSplitPane1.setName("jSplitPane1"); // NOI18N
        jSplitPane1.setOneTouchExpandable(true);
        jSplitPane1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSplitPane1PropertyChange(evt);
            }
        });

        jTabbedPane1.setMinimumSize(new java.awt.Dimension(400, 61));
        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        notes.setBackground(new java.awt.Color(254, 254, 254));
        notes.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        notes.setText(bundle.getString("ItemPanel.notes.text")); // NOI18N
        notes.setToolTipText(bundle.getString("ItemPanel.notes.toolTipText")); // NOI18N
        notes.setName("notes"); // NOI18N
        jScrollPane1.setViewportView(notes);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        labeledCombobox1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        labeledCombobox1.set_Label(bundle.getString("ItemPanel.labeledCombobox1._Label")); // NOI18N
        labeledCombobox1.setName("labeledCombobox1"); // NOI18N
        labeledCombobox1.setSearchEnabled(false);
        labeledCombobox1.setSearchOnEnterEnabled(false);
        jPanel3.add(labeledCombobox1, java.awt.BorderLayout.PAGE_START);

        jTabbedPane1.addTab(bundle.getString("ItemPanel.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        proptable.setAutoCreateRowSorter(true);
        proptable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "", ""
            }
        ));
        proptable.setName("proptable"); // NOI18N
        proptable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(proptable);

        jTabbedPane1.addTab(bundle.getString("ItemPanel.jScrollPane4.TabConstraints.tabTitle"), jScrollPane4); // NOI18N

        jSplitPane1.setLeftComponent(jTabbedPane1);

        jPanel8.setName("jPanel8"); // NOI18N

        jScrollPane2.setToolTipText(bundle.getString("ItemPanel.jScrollPane2.toolTipText")); // NOI18N
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        dataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        dataTable.setToolTipText(bundle.getString("ItemPanel.dataTable.toolTipText")); // NOI18N
        dataTable.setName("dataTable"); // NOI18N
        dataTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dataTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(dataTable);

        removefile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/remove.png"))); // NOI18N
        removefile.setText(bundle.getString("ItemPanel.removefile.text")); // NOI18N
        removefile.setToolTipText(bundle.getString("ItemPanel.removefile.toolTipText")); // NOI18N
        removefile.setName("removefile"); // NOI18N
        removefile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removefileActionPerformed(evt);
            }
        });

        addfile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/add.png"))); // NOI18N
        addfile.setText(bundle.getString("ItemPanel.addfile.text")); // NOI18N
        addfile.setToolTipText(bundle.getString("ItemPanel.addfile.toolTipText")); // NOI18N
        addfile.setName("addfile"); // NOI18N
        addfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addfileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(434, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addfile, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removefile, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                    .addGap(31, 31, 31)))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(addfile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removefile)
                .addContainerGap(12, Short.MAX_VALUE))
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(jPanel8);

        jPanel7.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jSplitPane2.setRightComponent(jPanel7);

        javax.swing.GroupLayout rightpaneLayout = new javax.swing.GroupLayout(rightpane);
        rightpane.setLayout(rightpaneLayout);
        rightpaneLayout.setHorizontalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(rightpaneLayout.createSequentialGroup()
                .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(rightpaneLayout.createSequentialGroup()
                        .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 866, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        rightpaneLayout.setVerticalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightpaneLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE))
        );

        add(rightpane, java.awt.BorderLayout.CENTER);

        toolbarpane.setName("toolbarpane"); // NOI18N
        toolbarpane.setLayout(new java.awt.BorderLayout());
        add(toolbarpane, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        if (dataOwner.isExisting()) {
            try {
                mpv5.YabsViewProxy.instance().getIdentifierView().addTab(DatabaseObject.getObject(Context.getContact(), dataOwner.__getContactsids()));
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        }
}//GEN-LAST:event_jButton2ActionPerformed

    private void button_order2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_order2ActionPerformed
        BigPopup.showPopup(this, new ControlPanel_Groups(), null);
}//GEN-LAST:event_button_order2ActionPerformed

    private void removefileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removefileActionPerformed
        deleteFile();
}//GEN-LAST:event_removefileActionPerformed

    private void addfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addfileActionPerformed
        if (dataOwner.isExisting()) {
            addFile();
        }
}//GEN-LAST:event_addfileActionPerformed

    private void dataTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dataTableMouseClicked
        fileTableClicked(evt);
    }//GEN-LAST:event_dataTableMouseClicked

    private void itemtableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_itemtableMouseClicked
        if (evt != null) {
            MPTableModel m = (MPTableModel) itemtable.getModel();
            if (evt.getButton() != MouseEvent.BUTTON1) {
                SubItem it = m.getRowAt(itemtable.getSelectedRow(), SubItem.getDefaultItem());

                if (it != null) {
                    mpv5.YabsViewProxy.instance().addToClipBoard(it);

                } else if (!m.hasEmptyRows(new int[]{4})) {
                    m.addRow(2);
                }
            }
        }
    }//GEN-LAST:event_itemtableMouseClicked

    private void button_previewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_previewActionPerformed
        preview();
    }//GEN-LAST:event_button_previewActionPerformed

    private void button_scheduleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_scheduleActionPerformed
    }//GEN-LAST:event_button_scheduleActionPerformed

    private void button_scheduleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_scheduleMouseClicked

//        JCalendar.instanceOf(300, evt.getLocationOnScreen());
        if (dataOwner != null && dataOwner.isExisting()) {
            ScheduleDayEvent.instanceOf().setItem(dataOwner);
        }
    }//GEN-LAST:event_button_scheduleMouseClicked

    private void button_remindersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_remindersActionPerformed
        if (dataOwner != null && dataOwner.isExisting()) {
            BigPopup.showPopup(mpv5.YabsViewProxy.instance().getIdentifierFrame().getRootPane(), new RemindPanel(dataOwner), Messages.REMINDER.toString(), false);
        }
    }//GEN-LAST:event_button_remindersActionPerformed
    MPTableModel omodel = null;
    private void button_deliverynoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_deliverynoteActionPerformed

        delivery();
    }//GEN-LAST:event_button_deliverynoteActionPerformed

    private void addItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addItemActionPerformed
        ((MPTableModel) itemtable.getModel()).addRow(1);
    }//GEN-LAST:event_addItemActionPerformed

    private void delItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delItemActionPerformed
        try {
            int index = itemtable.getSelectedRow();
            if (index < 0) {
                return;
            }

            MPTableModel m = (MPTableModel) itemtable.getModel();
            SubItem.addToDeletionQueue(m.getValueAt(index, 0));
            ((MPTableModel) itemtable.getModel()).removeRow(index);
        } catch (Exception e) {
            Log.Debug(e);
        }
    }//GEN-LAST:event_delItemActionPerformed

    private void upItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upItemActionPerformed
        try {
            int index = itemtable.getSelectedRow();
            if (index <= 0) {
                return;
            }
            ((MPTableModel) itemtable.getModel()).moveRow(index, index, index - 1);
            itemtable.changeSelection(index - 1, itemtable.getSelectedColumn(), false, false);
        } catch (Exception e) {
            Log.Debug(e);
        }
    }//GEN-LAST:event_upItemActionPerformed

    private void upItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upItem1ActionPerformed
        try {
            int index = itemtable.getSelectedRow();
            if (index < 0 || index >= itemtable.getRowCount() - 1) {
                return;
            }
            ((MPTableModel) itemtable.getModel()).moveRow(index, index, index + 1);
            itemtable.changeSelection(index + 1, itemtable.getSelectedColumn(), false, false);
        } catch (Exception e) {
            Log.Debug(e);
        }
    }//GEN-LAST:event_upItem1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed


        try {
            int cid = Integer.valueOf(contactname.getSelectedItem().getId());
            Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), cid);
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(c);
        } catch (Exception e) {
            //Nothing to show
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void button_orderconfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_orderconfActionPerformed

        confirmation();
    }//GEN-LAST:event_button_orderconfActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

        Contact dbo = (Contact) Search2.showSearchFor(Context.getContact());
        if (dbo != null) {
            contactname.setModel(dbo);
            contactcity.setText(dbo.__getCity());
            contactcompany.setText(dbo.__getCompany());
            contactid.setText(dbo.__getCNumber());
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void toorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toorderActionPerformed
        toOrder();
    }//GEN-LAST:event_toorderActionPerformed

    private void toinvoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toinvoiceActionPerformed
        toInvoice();
    }//GEN-LAST:event_toinvoiceActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (MPSecurityManager.checkAdminAccess()) {
            JDialog d = new JDialog(YabsViewProxy.instance().getIdentifierFrame(), true);
            d.add(new ItemNumberEditor(dataOwner, d, this));
            d.pack();
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            Position p = new Position(d);
            p.center();
            d.setVisible(true);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jSplitPane2PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSplitPane2PropertyChange
        if (evt.getPropertyName().equals("dividerLocation")) {
            User.getCurrentUser().setProperty("org.openyabs.uiproperty.itempanel.divier2.dividerLocation", evt.getNewValue().toString());
        }
    }//GEN-LAST:event_jSplitPane2PropertyChange

    private void jSplitPane1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSplitPane1PropertyChange
        if (evt.getPropertyName().equals("dividerLocation")) {
            User.getCurrentUser().setProperty("org.openyabs.uiproperty.itempanel.divier1.dividerLocation", evt.getNewValue().toString());
        }
    }//GEN-LAST:event_jSplitPane1PropertyChange
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.beans.LabeledCombobox accountselect;
    private javax.swing.JButton addItem;
    private javax.swing.JLabel addedby;
    private javax.swing.JButton addfile;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton button_deliverynote;
    private javax.swing.JButton button_order2;
    private javax.swing.JButton button_orderconf;
    private javax.swing.JButton button_preview;
    private javax.swing.JButton button_reminders;
    private javax.swing.JButton button_schedule;
    private javax.swing.JCheckBox checkb_pront_oc;
    private javax.swing.JTextField contactcity;
    private javax.swing.JTextField contactcompany;
    private javax.swing.JTextField contactid;
    private mpv5.ui.beans.LabeledCombobox contactname;
    private javax.swing.JTable dataTable;
    private mpv5.ui.beans.LabeledDateChooser date1;
    private mpv5.ui.beans.LabeledDateChooser date2;
    private mpv5.ui.beans.LabeledDateChooser date3;
    private javax.swing.JButton delItem;
    private javax.swing.JLabel discount;
    private mpv5.ui.beans.MPCombobox groupnameselect;
    private javax.swing.JPanel itemPanel;
    private javax.swing.JTable itemtable;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator10;
    private javax.swing.JToolBar.Separator jSeparator11;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JToolBar.Separator jSeparator9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox1;
    private javax.swing.JPanel leftpane;
    private javax.swing.JLabel netvalue;
    private javax.swing.JTextPane notes;
    private mpv5.ui.beans.LabeledTextField number;
    private javax.swing.JTable proptable;
    private javax.swing.JButton removefile;
    private javax.swing.JPanel rightpane;
    private mpv5.ui.beans.LabeledCombobox status;
    private javax.swing.JLabel staus_icon;
    private javax.swing.JLabel taxvalue;
    private javax.swing.JButton toinvoice;
    private javax.swing.JPanel toolbarpane;
    private javax.swing.JButton toorder;
    private javax.swing.JLabel type;
    private javax.swing.JLabel typelabel;
    private javax.swing.JButton upItem;
    private javax.swing.JButton upItem1;
    private javax.swing.JLabel value;
    // End of variables declaration//GEN-END:variables
    public String cname_;
    public String cnumber_;
    public String description_;
    public int intaddedby_;
    public int ids_;
    public Date dateadded_;
    public int groupsids_ = 1;
    public int contactsids_;
    public int accountsids_;
    public BigDecimal netvalue_;
    public BigDecimal taxvalue_;
    public BigDecimal shippingvalue_;
    public BigDecimal discountvalue_;
    public Date datetodo_;
    public Date dateend_;
    public int intreminders_;
    public int intstatus_;
    public int inttype_;

    @Override
    public boolean collectData() {
        try {
            contactsids_ = Integer.valueOf(contactname.getSelectedItem().getId());
        } catch (Exception numberFormatException) {
            return false;
        }
        if (contactsids_ > 0) {
            try {
                accountsids_ = Integer.valueOf(accountselect.getSelectedItem().getId());
            } catch (Exception e) {
                accountsids_ = 1;
            }

            if (groupnameselect.getSelectedItem() != null) {
                groupsids_ = Integer.valueOf(groupnameselect.getSelectedItem().getId());
                Log.Debug(this, groupnameselect.getSelectedItem().getId());
            } else {
                groupsids_ = 1;
            }

            if (dateadded_ == null) {
                dateadded_ = new Date();
            }
            intaddedby_ = User.getUserId(addedby.getText());
            description_ = notes.getText();
            dateadded_ = date1.getDate();

            if (cnumber_ == null) {
                cname_ = "<not set>";
            } else {
                cname_ = cnumber_;
            }

            netvalue_ = FormatNumber.parseDezimal(netvalue.getText());
            taxvalue_ = FormatNumber.parseDezimal(taxvalue.getText());
            discountvalue_ = FormatNumber.parseDezimal(discount.getText());

//            try {
//                shippingvalue_ = FormatNumber.parseDezimal(shipping.getText());
//            } catch (Exception e) {
            shippingvalue_ = BigDecimal.ZERO;
//            }

            datetodo_ = date2.getDate();
            dateend_ = date3.getDate();
            intstatus_ = Integer.valueOf(status.getSelectedItem().getId());

        } else {
            showRequiredFields();
            return false;
        }

        return true;
    }

    @Override
    public void exposeData() {

        number.setText(cname_);
        date1.setDate(dateadded_);
        date2.setDate(datetodo_);
        date3.setDate(dateend_);
        notes.setText(description_);

//        shipping.setText(FormatNumber.formatDezimal(shippingvalue_));
        button_reminders.setToolTipText(Messages.REMINDERS + String.valueOf(intreminders_));
        //  discountpercent.setValue(discountvalue_);

        status.setSelectedItem(intstatus_);
        staus_icon.setIcon(dataOwner.getIcon());
        try {
            accountselect.setModel(DatabaseObject.getObject(Context.getAccounts(), accountsids_));
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        try {
            groupnameselect.setModel(DatabaseObject.getObject(Context.getGroup(), groupsids_));
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }

        addedby.setText(User.getUsername(intaddedby_));
        try {
            Contact owner = (Contact) DatabaseObject.getObject(Context.getContact(), contactsids_, true);
            contactname.setModel(owner);
            contactcity.setText(owner.__getCity());
            contactcompany.setText(owner.__getCompany());
            contactid.setText(String.valueOf(owner.__getCNumber()));
            contactsids_ = owner.__getIDS();
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }

        fillFiles();
    }

    @Override
    public final void refresh() {

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {

                    groupnameselect.setModel(MPComboBoxModelItem.toModel(DatabaseObject.getObject(Context.getGroup(), mpv5.db.objects.User.getCurrentUser().__getGroupsids())));
                    groupnameselect.setSelectedIndex(0);
                    sp.refresh();

                    try {
                        accountselect.setModel(DatabaseObject.getObject(Context.getAccounts(), mpv5.db.objects.User.getCurrentUser().__getIntdefaultaccount()));
                    } catch (NodataFoundException nodataFoundException) {
                        Log.Debug(this, nodataFoundException.getMessage());
                    }

                    List<Integer> skip = new ArrayList<Integer>();
                    if (inttype_ == Item.TYPE_BILL) {
                        skip.add(new Integer(0));
//                        skip.add(new Integer(1));
                        skip.add(new Integer(2));
//                        skip.add(new Integer(5));
                    } else {
                        skip.add(new Integer(3));
                        skip.add(new Integer(4));
                    }
                    status.setModel(Item.getStatusStrings(), MPComboBoxModelItem.COMPARE_BY_ID, skip);
                    try {
                        status.setSelectedIndex(mpv5.db.objects.User.getCurrentUser().__getIntdefaultstatus());
                    } catch (Exception e) {
                    }
                    itemtable.setModel(SubItem.toModel(new SubItem[]{
                                SubItem.getDefaultItem(), SubItem.getDefaultItem(),
                                SubItem.getDefaultItem(), SubItem.getDefaultItem(),
                                SubItem.getDefaultItem(), SubItem.getDefaultItem()
                            }));
                    formatTable();
//                    shipping.setText(FormatNumber.formatDezimal(0d));

                } catch (Exception e) {
                    Log.Debug(this, e);
                }

                if (dataOwner.isExisting()) {
                    setDataOwner(dataOwner, true);
                }
            }
        };

        SwingUtilities.invokeLater(runnable);

    }

    /**
     *
     */
    public void formatTable() {

        prepareTable();

        //"Internal ID", Position, "Count", "Measure", "Text", "Netto Price", "Tax Rate", "Total Price", "Tax value", "Net 2", "Product ID", "", "", "Link", "Optional"
        TableFormat.resizeCols(itemtable, new Integer[]{0, 23, 53, 63, 100, 63, 63, 63, 0, 0, 63, 20, 0, 0, 100, 63, 0},
                new Boolean[]{true, true, true, true, false, true, true, true, true, true, true, true, true, true, false, true, true});
        MPTableModel model = (MPTableModel) itemtable.getModel();
        model.setCanEdits(new boolean[]{false, false, true, true, true, true, true, true, false, false, true, true, false, false, true, true, false});
        TableFormat.changeBackground(itemtable, 1, Color.LIGHT_GRAY);
        if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidecolumnquantity")) {
            TableFormat.stripColumn(itemtable, 2);
            model.setCellEditable(0, 2, false);
        }
        if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidecolumnmeasure")) {
            TableFormat.stripColumn(itemtable, 3);
            model.setCellEditable(0, 3, false);
        }

        if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hideproductscolumn")) {
            TableFormat.stripColumn(itemtable, 10);
            model.setCellEditable(0, 10, false);
        }

        if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidetaxcolumn")) {
            TableFormat.stripColumn(itemtable, 6);
            model.setCellEditable(0, 6, false);
        }

        if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidediscountcolumn")) {
            TableFormat.stripColumn(itemtable, 15);
            model.setCellEditable(0, 15, false);
        }

        TextAreaCellEditor r = new TextAreaCellEditor(itemtable);
        ProductSelectDialog3 productSelectDialog = new ProductSelectDialog3(mpv5.YabsViewProxy.instance().getIdentifierFrame(), true, itemtable);
        productSelectDialog.okButton.addActionListener(r);
        productSelectDialog.cancelButton.addActionListener(r);
        r.setDialog(productSelectDialog, productSelectDialog.getIDTextField());
        r.setEditorTo(10);

        if (!mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "showoptionalcolumn")) {
            TableFormat.stripColumn(itemtable, 14);
            model.setCellEditable(0, 14, false);
        } else {
            int widthc = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty.optionalcolumn.width", 0);
            if (widthc <= 0) {
                widthc = 121;
            }
            TableFormat.resizeCol(itemtable, 14, widthc, widthc != 121);
        }

        //column move is the very last thing to do, in newcol.asc order
        itemtable.moveColumn(10, 3);
        itemtable.moveColumn(14, 5);
        itemtable.moveColumn(15, 7);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void paste(DatabaseObject... dbos) {

        if (itemtable.getCellEditor() != null) {
            try {
                itemtable.getCellEditor().stopCellEditing();
            } catch (Exception e) {
            }
        }

        try {
            ((MPTableModel) itemtable.getModel()).removeEmptyRows(new int[]{4});
        } catch (Exception e) {
        }

        BigDecimal tpvs = null;
        for (DatabaseObject dbo : dbos) {
            if (dbo.getContext().equals(Context.getItem())
                    || dbo.getContext().equals(Context.getInvoice())
                    || dbo.getContext().equals(Context.getOffer())
                    || dbo.getContext().equals(Context.getOrder())) {
                Item o = (Item) dbo.clone();

                if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "pasten")) {
                    SubItem s = new SubItem();
                    s.setQuantityvalue(BigDecimal.ONE);
//                    s.setItemsids(o.__getIDS());
                    s.setInternalvalue(((Item) dbo).__getNetvalue());
                    s.setExternalvalue(((Item) dbo).__getNetvalue());
                    s.setTotalnetvalue(((Item) dbo).__getNetvalue());
                    s.setTotalbrutvalue(((Item) dbo).__getNetvalue().add(((Item) dbo).__getTaxvalue()));
                    if (s.__getTotalnetvalue().doubleValue() > 0d) {
                        BigDecimal tp = s.__getTotalbrutvalue().subtract(s.__getTotalnetvalue()).multiply(Constants.BD100).divide(s.__getTotalnetvalue(), 9, RoundingMode.HALF_UP);
                        if (tpvs == null) {
                            tpvs = tp;
                        }
                        if (tpvs.equals(tpvs)) {
                            s.setTaxpercentvalue(tp);
                        } else {
                            Popup.warn(Messages.TAXES_NOT_EQUAL);
                            break;
                        }
                    }
                    s.setCName(((Item) dbo).__getCName());
                    s.setDescription(Messages.GOOSE1 + " " + ((Item) dbo).__getCnumber() + " " + Messages.GOOSE2 + " " + DateConverter.getDefDateString(o.__getDateadded()));
//                   if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("deftax")) {
//                        int taxid = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("deftax", new Integer(0));
//                        BigDecimal deftax = Tax.getTaxValue(taxid);
//                        s.setTaxpercentvalue(deftax);
//                    }

//                    Log.PrintArray(s.toStringArray());

                    ((MPTableModel) itemtable.getModel()).addRow(s.getRowData(((MPTableModel) itemtable.getModel()).getLastValidRow(new int[]{4}) + 1));
                    ((MPTableModel) itemtable.getModel()).fireTableCellUpdated(0, 0);
                } else {
                    o.setIntstatus(Item.STATUS_IN_PROGRESS);
                    o.setInttype(inttype_);
                    o.setCnumber("");
                    o.setCName("");
                    o.setDateadded(new Date());
                    o.setDatetodo(new Date());
                    o.setDateend(new Date());

                    SubItem[] subs = new SubItem[0];
                    subs = o.getSubitems();
                    o.setIDS(-1);
                    setDataOwner(o, true);

                    MPTableModel t = SubItem.toModel(subs, true);

                    itemtable.setModel(t);
                    omodel = (MPTableModel) itemtable.getModel();
                    formatTable();
                    ((MPTableModel) itemtable.getModel()).fireTableCellUpdated(0, 0);
                }
            } else if (dbo.getContext().equals(Context.getContact())) {
                dataOwner.setContactsids(((Contact) dbo).__getIDS());
                setDataOwner(dataOwner, true);
            } else if (dbo.getContext().equals(Context.getProduct())) {
                ((MPTableModel) itemtable.getModel()).addRow(
                        new SubItem((Product) dbo).getRowData(((MPTableModel) itemtable.getModel()).getRowCount() + 1));
                ((MPTableModel) itemtable.getModel()).fireTableCellUpdated(0, 0);
            } else if (dbo.getContext().equals(Context.getProductlist())) {
                try {
                    SubItem[] subs = new SubItem[0];
                    if (dataOwner != null) {
                        subs = dataOwner.getSubitems();
                    }
                    List<ProductlistSubItem> l = ProductList.getReferencedObjects(dbo, Context.getProductListItems(), new ProductlistSubItem());
                    MPTableModel t = SubItem.toModel(subs);
                    int count = t.getRowCount();
                    for (int i = 0; i < l.size(); i++) {
                        ProductlistSubItem productlistSubItem = l.get(i);
                        productlistSubItem.setIDS(-1);
                        t.addRow(productlistSubItem.getRowData(i + count + 1));
                    }
                    itemtable.setModel(t);
                    omodel = (MPTableModel) itemtable.getModel();
                    formatTable();
                    ((MPTableModel) itemtable.getModel()).fireTableCellUpdated(0, 0);
                } catch (NodataFoundException ex) {
                    Log.Debug(this, ex.getMessage());
                }
            } else if (dbo.getContext().equals(Context.getSubItem())) {
                try {
                    SubItem sub = (SubItem) dbo;

                    ((MPTableModel) itemtable.getModel()).addRow(
                            sub.getRowData(((MPTableModel) itemtable.getModel()).getRowCount() + 1));

                    ((MPTableModel) itemtable.getModel()).fireTableCellUpdated(0, 0);
                } catch (Exception ex) {
                    Log.Debug(this, ex.getMessage());
                }
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE.toString());
                Log.Debug(this, dbo.getContext() + " to " + Context.getItem());
            }
        }

        try {
            itemtable.changeSelection(0, 0, true, false);
        } catch (Exception e) {
            Log.Debug(e);
        }

        itemMultiplier.calculateOnce();
        netCalculator.calculateOnce();
        taxcalculator.calculateOnce();
        disccalculator.calculateOnce();


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
        saveSubItems(true);
        omodel = (MPTableModel) itemtable.getModel();
        setTitle();
    }

    @Override
    public void actionAfterCreate() {

        sp.refresh();
        ArrayUtilities.replaceColumn(itemtable, 0, null);
        saveSubItems(false);
        omodel = (MPTableModel) itemtable.getModel();
        setTitle();

    }

    private void saveSubItems(boolean deleteRemovedSubitems) {
        if (itemtable.getCellEditor() != null) {
            try {
                itemtable.getCellEditor().stopCellEditing();
            } catch (Exception e) {
            }
        }

        try {
            itemtable.changeSelection(0, 0, true, false);
        } catch (Exception e) {
            Log.Debug(e);
        }

        if (dataOwner.__getInttype() != Item.TYPE_BILL) {
            Product.createProducts(SubItem.saveModel(dataOwner, (MPTableModel) itemtable.getModel(), deleteRemovedSubitems), dataOwner);
        } else {
            SubItem.saveModel(dataOwner, (MPTableModel) itemtable.getModel(), deleteRemovedSubitems);
        }

        for (int i = 0; i < usedOrders.size(); i++) {
            Item o = usedOrders.get(i);
            o.setIntstatus(Item.STATUS_FINISHED);
            o.save(true);
        }
    }
    List<Item> usedOrders = new ArrayList<Item>();

    @Override
    public void changeSelection(MPComboBoxModelItem to, Context c) {
        try {
            DatabaseObject o = DatabaseObject.getObject(c, Integer.valueOf(to.getId()));
            int i = itemtable.getSelectedRow();
            if (i >= 0) {
                Object opt = itemtable.getModel().getValueAt(i, 14);
                ((MPTableModel) itemtable.getModel()).setRowAt(new SubItem((Product) o).getRowData(i), i, 4);
                itemtable.setValueAt(opt, i, 14);
            }
        } catch (Exception ex) {
        }
    }

    public void actionBeforeCreate() {
        status.setSelectedIndex(Item.STATUS_IN_PROGRESS);
        date1.setDate(new Date());
        try {
            date3.setDate(DateConverter.addDays(new Date(), Integer.valueOf(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("bills.warn.days"))));
            date2.setDate(new Date());
        } catch (Exception e) {
            date3.setDate(DateConverter.addDays(new Date(), 14));
            date2.setDate(new Date());
        }
    }

    public void actionBeforeSave() throws ChangeNotApprovedException {
        if (dataOwner.isExisting()) {
            if ((dataOwner.__getIntstatus() != Item.STATUS_PAID && dataOwner.__getIntstatus() != Item.STATUS_CANCELLED) || Popup.Y_N_dialog(Messages.REALLY_CHANGE_DONE_ITEM)) {

                if (!mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "nowarnings")) {

                    if (!Popup.Y_N_dialog(Messages.REALLY_CHANGE)) {
                        throw new ChangeNotApprovedException(dataOwner);
                    }
                }
            } else {
                throw new ChangeNotApprovedException(dataOwner);
            }
        }
    }

    private void prepareTable() {
        TableCellRendererForProducts tx = new TableCellRendererForProducts(itemtable);
        tx.setRendererTo(10);

        TableCellRendererForDezimal ti = new TableCellRendererForDezimal(itemtable, User.getCurrentUser().getProperty("org.openyabs.uiproperty$defquantityformat"));
        ti.setRendererTo(2);

        LazyCellRenderer lcr = new LazyCellRenderer(itemtable);
        lcr.setRendererTo(3);

        TableCellRendererForDezimal t = new TableCellRendererForDezimal(itemtable);
        t.setRendererTo(6);
        t.setRendererTo(5);
        t.setRendererTo(15);
        t.setRendererTo(16);
        TableCellRendererForDezimal tc = new TableCellRendererForDezimal(itemtable, new java.awt.Color(161, 176, 190));
        tc.setRendererTo(7);

//        CellEditorWithMPComboBox r = new CellEditorWithMPComboBox(Context.getProduct(), itemtable);
//        r.setEditorTo(4, this);
        TextAreaCellRenderer textAreaCellRenderer = new TextAreaCellRenderer(itemtable);
        textAreaCellRenderer.setRendererTo(4);
        TextAreaCellEditor r = new TextAreaCellEditor(itemtable);
        ItemTextAreaDialog itemTextAreaDialog = new ItemTextAreaDialog(mpv5.YabsViewProxy.instance().getIdentifierFrame(), true);
        itemTextAreaDialog.setParentTable(itemtable);
        itemTextAreaDialog.okButton.addActionListener(r);
        itemTextAreaDialog.cancelButton.addActionListener(r);
        r.setDialog(itemTextAreaDialog, itemTextAreaDialog.textArea);
        r.setEditorTo(4);

        itemtable.getColumnModel().getColumn(3).setCellEditor(new LazyCellEditor(new JTextField()));

        itemMultiplier = new DynamicTableCalculator(itemtable, "(([2]*[5])-([2]*[5]%[15]))+(([2]*[5])-([2]*[5]%[15]))%[6]", new int[]{7});
        ((MPTableModel) itemtable.getModel()).addCalculator(itemMultiplier);
        itemMultiplier.addLabel(value, 7);

        netCalculator = new DynamicTableCalculator(itemtable, "[2]*[5]", new int[]{9});
        ((MPTableModel) itemtable.getModel()).addCalculator(netCalculator);
        netCalculator.addLabel(netvalue, 9);

        taxcalculator = new DynamicTableCalculator(itemtable, "((([2]*[5])-([2]*[5]%[15]))+(([2]*[5])-([2]*[5]%[15]))%[6])-(([2]*[5])-([2]*[5]%[15]))", new int[]{8});
        ((MPTableModel) itemtable.getModel()).addCalculator(taxcalculator);
        taxcalculator.addLabel(taxvalue, 8);

        disccalculator = new DynamicTableCalculator(itemtable, "[2]*[5]%[15]", new int[]{16});
        ((MPTableModel) itemtable.getModel()).addCalculator(disccalculator);
        disccalculator.addLabel(discount, 16);

//        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("shiptax")) {
//            int taxid = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("shiptax", new Integer(0));
//            Double shiptax = Tax.getTaxValue(taxid).doubleValue();
//            Double shipt = (shiptax / 100);
//            itemMultiplier.addAsRow(1, new LabeledTextField(1d, Double.class), 2);
//            itemMultiplier.addAsRow(1, new LabeledTextField(shiptax, Double.class), 6);
//            itemMultiplier.addAsRow(1, shipping, 5);
//
//            netCalculator.addAsRow(1, new LabeledTextField(1d, Double.class), 2);
//            netCalculator.addAsRow(1, new LabeledTextField(shiptax, Double.class), 6);
//            netCalculator.addAsRow(1, shipping, 5);
//            taxcalculator.addAsRow(1, new LabeledTextField(1d, Double.class), 2);
//            taxcalculator.addAsRow(1, new LabeledTextField(shiptax, Double.class), 6);
//            taxcalculator.addAsRow(1, shipping, 5);
//        } else {
//            itemMultiplier.addToSum(shipping);
//            netCalculator.addToSum(shipping);
//        }

        JButton b1 = new JButton();
        b1.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
                if (!mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "ordersoverproducts")) {
                    ProductSelectDialog.instanceOf((MPTableModel) itemtable.getModel(), itemtable.getSelectedRow(), e, 0, null, null);
                } else {
                    SubItem s = new SubItem();
                    Item o = (Item) Popup.SelectValue(Context.getOrder());
                    if (o != null) {
                        s.setQuantityvalue(BigDecimal.ONE);
                        s.setItemsids(o.__getIDS());
                        s.setExternalvalue(o.__getNetvalue().add(o.__getTaxvalue()));
                        s.setTotalnetvalue(o.__getNetvalue());
                        s.setCName(o.__getCName());
                        s.setDescription(Messages.TYPE_ORDER + " " + o.__getCnumber() + " " + DateConverter.getDefDateString(o.__getDateadded()));

                        ((MPTableModel) itemtable.getModel()).setRowAt(s.getRowData(itemtable.getSelectedRow()), itemtable.getSelectedRow(), 1);

                        usedOrders.add(o);
                    }
                }

                if (((MPTableModel) itemtable.getModel()).getEmptyRows(new int[]{4}) < 2) {
                    ((MPTableModel) itemtable.getModel()).addRow(1);
                }
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });

        JButton b2 = new JButton();
        b2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MPTableModel m = (MPTableModel) itemtable.getModel();
                int row = itemtable.getSelectedRow();
                SubItem.addToDeletionQueue(m.getValueAt(row, 0));
                m.setRowAt(SubItem.getDefaultItem().getRowData(row), row, 1);
            }
        });

        itemtable.getColumnModel().getColumn(SubItem.COLUMNINDEX_ADD).setCellRenderer(new ButtonRenderer());
        itemtable.getColumnModel().getColumn(SubItem.COLUMNINDEX_ADD).setCellEditor(new ButtonEditor(b1));
        itemtable.getColumnModel().getColumn(SubItem.COLUMNINDEX_REMOVE).setCellRenderer(new ButtonRenderer());
        itemtable.getColumnModel().getColumn(SubItem.COLUMNINDEX_REMOVE).setCellEditor(new ButtonEditor(b2));

        TablePopUp tpu = new TablePopUp(itemtable, new String[]{
                    Messages.ACTION_COPY.getValue(),
                    Messages.ACTION_PASTE.getValue(),
                    null,
                    Messages.ACTION_ADD.getValue(),
                    Messages.ACTION_REMOVE.getValue()},
                new ActionListener[]{new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    MPTableModel m = (MPTableModel) itemtable.getModel();
                    SubItem it = m.getRowAt(itemtable.getSelectedRow(), SubItem.getDefaultItem());

                    if (it != null) {
                        mpv5.YabsViewProxy.instance().addToClipBoard(it);

                    }
                }
            }, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
//                    mpv5.YabsViewProxy.instance().pasteClipboardItems();
                }
            },
                    null,
                    new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ((MPTableModel) itemtable.getModel()).addRow(1);
                        }
                    }, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = itemtable.getSelectedRow();
                    if (index < 0) {
                        return;
                    }

                    MPTableModel m = (MPTableModel) itemtable.getModel();
                    SubItem.addToDeletionQueue(m.getValueAt(index, 0));
                    ((MPTableModel) itemtable.getModel()).removeRow(index);
                }
            }});
    }

    private void delivery() {
        PreviewPanel pr;
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), Constants.TYPE_DELIVERY_NOTE)) {
                pr = new PreviewPanel();
                pr.setDataOwner(dataOwner);
                new Job(Export.createFile(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), Constants.TYPE_DELIVERY_NOTE), dataOwner), pr).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
            }
        }
    }

    private void confirmation() {
        PreviewPanel pr;
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), Constants.TYPE_ORDER_CONFIRMATION)) {

                pr = new PreviewPanel();
                pr.setDataOwner(dataOwner);
                new Job(Export.createFile(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), Constants.TYPE_ORDER_CONFIRMATION), dataOwner), pr).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
            }
        }
    }

    public void mail() {

        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {

                try {
                    Contact cont = (Contact) (Contact.getObject(Context.getContact(), dataOwner.__getContactsids()));
                    Export.mail(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.__getInttype()), dataOwner, cont);
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
            }
        } else {
            Popup.notice(Messages.NOT_POSSIBLE + "\n" + Messages.NOT_SAVED_YET);
        }
    }

    public void print() {
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
                if (!checkb_pront_oc.isSelected()) {
                    Export.print(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.__getInttype()), dataOwner);
                } else {
                    Export.print(new Template[]{TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.__getInttype()), TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), Constants.TYPE_ORDER_CONFIRMATION)}, dataOwner);
                }
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
                Export.print(this);
            }
        } else {
            Export.print(this);
        }
    }

    private void preview() {
        PreviewPanel pr;
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
                pr = new PreviewPanel();
                pr.setDataOwner(dataOwner);
                new Job(Export.createFile(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.__getInttype()), dataOwner), pr).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
            }
        }
    }

    private void preloadTemplates() {
        Runnable runnable = new Runnable() {

            public void run() {
                TemplateHandler.loadTemplateFor(button_preview, dataOwner.templateGroupIds(), dataOwner.__getInttype());
                TemplateHandler.loadTemplateFor(button_deliverynote, dataOwner.templateGroupIds(), Constants.TYPE_DELIVERY_NOTE);
                TemplateHandler.loadTemplateFor(new JComponent[]{button_orderconf, checkb_pront_oc}, dataOwner.templateGroupIds(), Constants.TYPE_ORDER_CONFIRMATION);
                TemplateHandler.loadTemplateFor(button_reminders, dataOwner.templateGroupIds(), Constants.TYPE_REMINDER);

                if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
                    button_preview.setText(Messages.ACTION_PREVIEW.getValue());
                } else {
                    button_preview.setText(Messages.OO_NO_TEMPLATE.getValue());
                }
            }
        };
        new Thread(runnable).start();
    }

    public void pdf() {
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
                new Job(Export.createFile(dataOwner.getFormatHandler().toUserString(), TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.__getInttype()), dataOwner), new DialogForFile(User.getSaveDir(dataOwner))).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
            }
        }
    }

    public void odt() {
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
                new Job(Export.sourceFile(dataOwner.getFormatHandler().toUserString(), TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.__getInttype()), dataOwner), new DialogForFile(User.getSaveDir(dataOwner))).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
            }
        }
    }

    private void properties() {
        final MPTableModel m = new MPTableModel(ValueProperty.getProperties(dataOwner));
        final MPTableModel mold = m.clone();

        if (m.getDataVector().isEmpty()) {
            proptable.setModel(new MPTableModel(
                    Arrays.asList(new ValueProperty[]{new ValueProperty("", "", dataOwner)})));
        } else {
            proptable.setModel(m);
        }

        m.addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent e) {
                if (dataOwner.isExisting()) {
                    if (e.getColumn() == 0 && e.getType() == TableModelEvent.DELETE) {
                        ValueProperty.deleteProperty(dataOwner, String.valueOf(mold.getData()[e.getLastRow()][0]));
                        m.removeTableModelListener(this);
                        properties();
                    } else if (e.getColumn() == 1 && m.getValueAt(e.getLastRow(), 0) != null && String.valueOf(m.getValueAt(e.getLastRow(), 0)).length() > 0) {
                        ValueProperty.addOrUpdateProperty(String.valueOf(m.getData()[e.getLastRow()][0]).replaceAll("[^\\w]", ""), String.valueOf(m.getData()[e.getLastRow()][1]), dataOwner);
                        m.removeTableModelListener(this);
                        properties();
                    }
                }
            }
        });
    }

    private void toOrder() {

        dataOwner.setIntstatus(Item.STATUS_FINISHED);
        dataOwner.save();

        Item i2 = (Item) dataOwner.clone(Context.getOrder());
        i2.setInttype(Item.TYPE_ORDER);
        i2.setIDS(-1);
        i2.defineFormatHandler(new FormatHandler(i2));
        i2.save();
        if (itemtable.getCellEditor() != null) {
            try {
                itemtable.getCellEditor().stopCellEditing();
            } catch (Exception e) {
            }
        }
        SubItem.saveModel(i2, (MPTableModel) itemtable.getModel(), true, true);
        setDataOwner(i2, true);
        Popup.notice(i2 + Messages.INSERTED.getValue());
    }

    private void toInvoice() {

        dataOwner.setIntstatus(Item.STATUS_FINISHED);
        dataOwner.save();
        ArrayList<ActivityList> data;
        Object[] row;

        Item i2 = (Item) dataOwner.clone(Context.getItem());
        i2.setInttype(Item.TYPE_BILL);
        i2.setIDS(-1);
        i2.defineFormatHandler(new FormatHandler(i2));
        i2.save();
        if (itemtable.getCellEditor() != null) {
            try {
                itemtable.getCellEditor().stopCellEditing();
            } catch (Exception e) {
            }
        }
        try {
            data = DatabaseObject.getObjects(Context.getActivityList(), new QueryCriteria("orderids", dataOwner.__getIDS()));
            if (Popup.Y_N_dialog(Messages.ActivityList_Existing.toString())) {
                MPTableModel model = (MPTableModel) itemtable.getModel();
                Iterator<ActivityList> it = data.iterator();
                while (it.hasNext()) {
                    row = it.next().getDataForInvoice();
                    row[1] = model.getRowCount();
                    model.insertRow(model.getRowCount(), row);
                }
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        SubItem.saveModel(i2, (MPTableModel) itemtable.getModel(), true, true);
        setDataOwner(i2, true);
        Popup.notice(i2 + Messages.INSERTED.getValue());
    }

    private class alignRightToolbar extends JToolBar {

        private static final long serialVersionUID = 1L;

        public alignRightToolbar() {
            add(Box.createHorizontalGlue());
        }
    }
}
