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

import enoa.handler.TableHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import mpv5.db.common.*;
import mpv5.db.objects.Account;
import mpv5.db.objects.Product;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.db.objects.Contact;
import mpv5.db.common.DataNotCachedException;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Template;
import mpv5.logging.Log;
import mpv5.ui.dialogs.BigPopup;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Groups;
import mpv5.ui.frames.MPView;
import mpv5.ui.popups.FileTablePopUp;
import mpv5.ui.toolbars.DataPanelTB;
import mpv5.db.objects.User;
import mpv5.handler.FormFieldsHandler;
import mpv5.ui.beans.MPCBSelectionChangeReceiver;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.ScheduleDayEvent;
import mpv5.ui.panels.calendar.JCalendar;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.date.DateConverter;
import mpv5.utils.export.Export;
import mpv5.utils.export.Exportable;
import mpv5.utils.export.ODTFile;
import mpv5.utils.export.PDFFile;
import mpv5.utils.export.PDFFile;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.jobs.Job;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.models.MPTableModelRow;
import mpv5.utils.models.NativeModeNotSupportedException;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.ooo.OOOPanel;
import mpv5.utils.tables.TableCalculator;
import mpv5.utils.renderer.CellRendererWithMPComboBox;
import mpv5.utils.renderer.TableCellRendererForDezimal;
import mpv5.utils.tables.TableFormat;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 * 
 */
public class ItemPanel extends javax.swing.JPanel implements DataPanel, MPCBSelectionChangeReceiver {

    private static final long serialVersionUID = 1L;
    private Item dataOwner;
    private DataPanelTB tb;
    private SearchPanel sp;
    private Integer dataTableContent = null;
    private TableCalculator itemMultiplier;
    private TableCalculator netCalculator;
    private TableCalculator netCalculator2;

    /** Creates new form ContactPanel
     * @param context
     * @param type
     */
    public ItemPanel(Context context, int type) {
        initComponents();
        sp = new SearchPanel(context, this);
        sp.setVisible(true);
        tb = new mpv5.ui.toolbars.DataPanelTB(this);
        toolbarpane.add(tb, BorderLayout.CENTER);
        dataOwner = new Item();
        if (type >= 0) {
            dataOwner.setInttype(type);
            this.type.setText(Item.getTypeString(type));
        } else {
            this.type.setText("");
        }

        refresh();
        shipping.set_ValueClass(Double.class);

        addedby.setText(MPView.getUser().getName());
        contactname.setSearchOnEnterEnabled(true);
        contactname.setContext(Context.getContact());
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
        accountselect.setSearchOnEnterEnabled(true);
        groupnameselect.setContext(Context.getGroup());
        groupnameselect.setSearchOnEnterEnabled(true);

        date1.setDate(new Date());
        date2.setDate(new Date());
        date3.setDate(new Date());

        itemtable.getTableHeader().addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                MPTableModel m = (MPTableModel) itemtable.getModel();
                if (m.getRowCount() > 0) {
                    m.addRow(5);
                } else {
                    itemtable.setModel(SubItem.toModel(new SubItem[]{
                                SubItem.getDefaultItem(), SubItem.getDefaultItem(),
                                SubItem.getDefaultItem(), SubItem.getDefaultItem(),
                                SubItem.getDefaultItem(), SubItem.getDefaultItem()
                            }));

                    formatTable();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        number.setSearchOnEnterEnabled(true);
        number.setParent(this);
        number.setSearchField("cname");
        number.setContext(Context.getItems());
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
        dataOwner = (Item) object;
        if (populate) {
            dataOwner.setPanelData(this);
            inttype_ = dataOwner.__getInttype();
            typelabel.setIcon(dataOwner.getIcon());
            this.exposeData();

            setTitle();
            type.setText(Item.getTypeString(dataOwner.__getInttype()));
            prinitingComboBox1.init(dataOwner);

            tb.setFavourite(Favourite.isFavourite(object));
            tb.setEditable(!object.isReadOnly());

            itemtable.setModel(SubItem.toModel(((Item) object).getSubitems()));
            formatTable();
            ((MPTableModel) itemtable.getModel()).fireTableCellUpdated(0, 0);
            if (object.isReadOnly()) {
                Popup.notice(Messages.LOCKED_BY);
            }
            preload = false;
            button_preview.setEnabled(preload);
            preloadTemplate();
            validate();
        }
    }
    Exportable preloadedExportFile;
    Template preloadedTemplate;

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
        TextFieldUtils.blink(contactname.getComboBox().getEditor().getEditorComponent(), Color.RED);
        contactname.requestFocus();
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
        Context c = Context.getFilesToItems();
        c.addReference(Context.getFiles().getDbIdentity(), "cname", "filename");
        Object[][] data = new DatabaseSearch(c).getValuesFor(Context.DETAILS_FILES_TO_ITEMS, "itemsids", dataOwner.__getIDS());

        dataTable.setModel(new MPTableModel(data, Headers.FILE_REFERENCES.getValue()));
        TableFormat.stripFirstColumn(dataTable);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        itemtable = new javax.swing.JTable();
        jToolBar1 = new javax.swing.JToolBar();
        type = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        typelabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        button_reminders = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        jButton1 = new javax.swing.JButton();
        button_preview = new javax.swing.JButton();
        prinitingComboBox1 = new mpv5.ui.beans.PrinitingComboBox();
        jPanel2 = new javax.swing.JPanel();
        contactname = new mpv5.ui.beans.LabeledCombobox();
        contactcity = new javax.swing.JTextField();
        contactcompany = new javax.swing.JTextField();
        contactid = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        notes = new javax.swing.JTextPane();
        jPanel5 = new javax.swing.JPanel();
        date1 = new mpv5.ui.beans.LabeledDateChooser();
        date2 = new mpv5.ui.beans.LabeledDateChooser();
        date3 = new mpv5.ui.beans.LabeledDateChooser();
        jScrollPane2 = new javax.swing.JScrollPane();
        dataTable = new JTable() {
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
        addfile = new javax.swing.JButton();
        removefile = new javax.swing.JButton();
        jToolBar2 = new javax.swing.JToolBar();
        shipping = new mpv5.ui.beans.LabeledTextField();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        discountpercent = new mpv5.ui.beans.LabeledSpinner();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        netvalue = new mpv5.ui.beans.LabeledTextField();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        taxvalue = new mpv5.ui.beans.LabeledTextField();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        value = new mpv5.ui.beans.LabeledTextField();
        toolbarpane = new javax.swing.JPanel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ItemPanel.border.title_1"))); // NOI18N
        setName("Form"); // NOI18N

        leftpane.setName("leftpane"); // NOI18N
        leftpane.setLayout(new java.awt.BorderLayout());

        rightpane.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ItemPanel.rightpane.border.title"))); // NOI18N
        rightpane.setName("rightpane"); // NOI18N

        jPanel1.setBackground(new java.awt.Color(227, 219, 202));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setName("jPanel1"); // NOI18N

        number.set_Label(bundle.getString("ItemPanel.number._Label")); // NOI18N
        number.setFocusable(false);
        number.setFont(number.getFont());
        number.setName("number"); // NOI18N

        addedby.setFont(addedby.getFont());
        addedby.setText(bundle.getString("ItemPanel.addedby.text")); // NOI18N
        addedby.setToolTipText(bundle.getString("ItemPanel.addedby.toolTipText")); // NOI18N
        addedby.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        addedby.setEnabled(false);
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(accountselect, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(status, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(number, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 81, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(groupnameselect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel4)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(button_order2)
                            .addGap(116, 116, 116)))
                    .addComponent(addedby, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(status, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(accountselect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(button_order2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(addedby, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(groupnameselect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(number, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

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
        itemtable.setCellSelectionEnabled(true);
        itemtable.setName("itemtable"); // NOI18N
        itemtable.setSurrendersFocusOnKeystroke(true);
        itemtable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                itemtableMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(itemtable);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 682, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
        );

        jToolBar1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        type.setBackground(new java.awt.Color(255, 255, 255));
        type.setFont(type.getFont().deriveFont(type.getFont().getStyle() | java.awt.Font.BOLD, type.getFont().getSize()+2));
        type.setForeground(new java.awt.Color(0, 51, 51));
        type.setText(bundle.getString("ItemPanel.type.text")); // NOI18N
        type.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(1, 3, 1, 5)));
        type.setMaximumSize(new java.awt.Dimension(100, 23));
        type.setName("type"); // NOI18N
        jToolBar1.add(type);

        jSeparator4.setName("jSeparator4"); // NOI18N
        jToolBar1.add(jSeparator4);

        typelabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/22/editcopy.png"))); // NOI18N
        typelabel.setText(bundle.getString("ItemPanel.typelabel.text")); // NOI18N
        typelabel.setName("typelabel"); // NOI18N
        jToolBar1.add(typelabel);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jToolBar1.add(jSeparator1);

        jSeparator2.setName("jSeparator2"); // NOI18N
        jToolBar1.add(jSeparator2);

        button_reminders.setText(bundle.getString("ItemPanel.button_reminders.text")); // NOI18N
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

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/kalarm.png"))); // NOI18N
        jButton1.setText(bundle.getString("ItemPanel.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

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

        prinitingComboBox1.setName("prinitingComboBox1"); // NOI18N

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
                .addGap(0, 12, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(contactname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contactcity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contactcompany, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contactid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jScrollPane1.setBorder(null);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        notes.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        notes.setText(bundle.getString("ItemPanel.notes.text")); // NOI18N
        notes.setDragEnabled(true);
        notes.setName("notes"); // NOI18N
        jScrollPane1.setViewportView(notes);

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
                .addComponent(date1, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(date2, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(date3, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(13, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(date1, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(date2, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(date3, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

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

        addfile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/add.png"))); // NOI18N
        addfile.setText(bundle.getString("ItemPanel.addfile.text")); // NOI18N
        addfile.setName("addfile"); // NOI18N
        addfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addfileActionPerformed(evt);
            }
        });

        removefile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/remove.png"))); // NOI18N
        removefile.setText(bundle.getString("ItemPanel.removefile.text")); // NOI18N
        removefile.setName("removefile"); // NOI18N
        removefile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removefileActionPerformed(evt);
            }
        });

        jToolBar2.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 1, true));
        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);
        jToolBar2.setName("jToolBar2"); // NOI18N

        shipping.set_Label(bundle.getString("ItemPanel.shipping._Label")); // NOI18N
        shipping.setName("shipping"); // NOI18N
        jToolBar2.add(shipping);

        jSeparator8.setName("jSeparator8"); // NOI18N
        jSeparator8.setSeparatorSize(new java.awt.Dimension(15, 10));
        jToolBar2.add(jSeparator8);

        discountpercent.set_Label(bundle.getString("ItemPanel.discountpercent._Label")); // NOI18N
        discountpercent.setName("discountpercent"); // NOI18N
        jToolBar2.add(discountpercent);

        jSeparator3.setName("jSeparator3"); // NOI18N
        jSeparator3.setSeparatorSize(new java.awt.Dimension(15, 10));
        jToolBar2.add(jSeparator3);

        netvalue.set_Label(bundle.getString("ItemPanel.netvalue._Label")); // NOI18N
        netvalue.setName("netvalue"); // NOI18N
        jToolBar2.add(netvalue);

        jSeparator6.setName("jSeparator6"); // NOI18N
        jSeparator6.setSeparatorSize(new java.awt.Dimension(15, 10));
        jToolBar2.add(jSeparator6);

        taxvalue.set_Label(bundle.getString("ItemPanel.taxvalue._Label")); // NOI18N
        taxvalue.setName("taxvalue"); // NOI18N
        jToolBar2.add(taxvalue);

        jSeparator5.setName("jSeparator5"); // NOI18N
        jSeparator5.setSeparatorSize(new java.awt.Dimension(15, 10));
        jToolBar2.add(jSeparator5);

        value.set_Label(bundle.getString("ItemPanel.value._Label")); // NOI18N
        value.setName("value"); // NOI18N
        jToolBar2.add(value);

        javax.swing.GroupLayout rightpaneLayout = new javax.swing.GroupLayout(rightpane);
        rightpane.setLayout(rightpaneLayout);
        rightpaneLayout.setHorizontalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rightpaneLayout.createSequentialGroup()
                .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, rightpaneLayout.createSequentialGroup()
                        .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 99, Short.MAX_VALUE)
                        .addComponent(prinitingComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(rightpaneLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(removefile, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addfile, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE))
                .addContainerGap())
        );
        rightpaneLayout.setVerticalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightpaneLayout.createSequentialGroup()
                .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(prinitingComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(rightpaneLayout.createSequentialGroup()
                        .addComponent(addfile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removefile))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(toolbarpane, javax.swing.GroupLayout.DEFAULT_SIZE, 696, Short.MAX_VALUE)
                        .addContainerGap())
                    .addComponent(rightpane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(leftpane, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolbarpane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(rightpane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        if (dataOwner.isExisting()) {
            try {
                MPView.identifierView.addTab(DatabaseObject.getObject(Context.getContact(), dataOwner.__getContactsids()));
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

        MPTableModel m = (MPTableModel) itemtable.getModel();
        if (!m.hasEmptyRows(new int[]{4})) {
            m.addRow(2);
        }
    }//GEN-LAST:event_itemtableMouseClicked

    private void button_previewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_previewActionPerformed
        preview();
    }//GEN-LAST:event_button_previewActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked

//        JCalendar.instanceOf(300, evt.getLocationOnScreen());
        if (dataOwner != null && dataOwner.isExisting()) {
            ScheduleDayEvent.instanceOf().setItem(dataOwner);
        }
    }//GEN-LAST:event_jButton1MouseClicked

    private void button_remindersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_remindersActionPerformed
         if (dataOwner != null && dataOwner.isExisting()) {
            BigPopup.showPopup(MPView.identifierFrame.getRootPane(), new RemindPanel(dataOwner), Messages.REMINDER.toString(), true);
        }
    }//GEN-LAST:event_button_remindersActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.beans.LabeledCombobox accountselect;
    private javax.swing.JLabel addedby;
    private javax.swing.JButton addfile;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton button_order2;
    private javax.swing.JButton button_preview;
    private javax.swing.JButton button_reminders;
    private javax.swing.JTextField contactcity;
    private javax.swing.JTextField contactcompany;
    private javax.swing.JTextField contactid;
    private mpv5.ui.beans.LabeledCombobox contactname;
    private javax.swing.JTable dataTable;
    private mpv5.ui.beans.LabeledDateChooser date1;
    private mpv5.ui.beans.LabeledDateChooser date2;
    private mpv5.ui.beans.LabeledDateChooser date3;
    private mpv5.ui.beans.LabeledSpinner discountpercent;
    private mpv5.ui.beans.MPCombobox groupnameselect;
    private javax.swing.JTable itemtable;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JPanel leftpane;
    private mpv5.ui.beans.LabeledTextField netvalue;
    private javax.swing.JTextPane notes;
    private mpv5.ui.beans.LabeledTextField number;
    private mpv5.ui.beans.PrinitingComboBox prinitingComboBox1;
    private javax.swing.JButton removefile;
    private javax.swing.JPanel rightpane;
    private mpv5.ui.beans.LabeledTextField shipping;
    private mpv5.ui.beans.LabeledCombobox status;
    private mpv5.ui.beans.LabeledTextField taxvalue;
    private javax.swing.JPanel toolbarpane;
    private javax.swing.JLabel type;
    private javax.swing.JLabel typelabel;
    private mpv5.ui.beans.LabeledTextField value;
    // End of variables declaration//GEN-END:variables
    public String cname_;
    public String cnumber_;
    public String description_;
    public int intaddedby_;
    public int ids_;
    public Date dateadded_;
    public int groupsids_ = 1;
    public int contactsids_;
    public int defaultaccountsids_;
    public double netvalue_;
    public double taxvalue_;
    public double shippingvalue_;
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
                defaultaccountsids_ = Integer.valueOf(accountselect.getSelectedItem().getId());
            } catch (Exception e) {
                Log.Debug(e);
                defaultaccountsids_ = 1;
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
            try {
                shippingvalue_ = FormatNumber.parseDezimal(shipping.getText());
            } catch (Exception e) {
                shippingvalue_ = 0;
            }

            datetodo_ = date2.getDate();
            dateend_ = date3.getDate();
            intstatus_ = Integer.valueOf(status.getSelectedItem().getId());

        } else {
            showRequiredFields();
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

        shipping.setText(FormatNumber.formatDezimal(shippingvalue_));
        button_reminders.setToolTipText(Messages.REMINDERS + String.valueOf(intreminders_));

        status.setSelectedIndex(intstatus_);
        try {
            accountselect.setModel(DatabaseObject.getObject(Context.getAccounts(), defaultaccountsids_));
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
            Contact owner = (Contact) DatabaseObject.getObject(Context.getContact(), contactsids_);
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
    public void refresh() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    groupnameselect.setModel(MPComboBoxModelItem.toModel(DatabaseObject.getObject(Context.getGroup(), MPView.getUser().__getGroupsids())));
                    groupnameselect.setSelectedIndex(0);
                    sp.refresh();

                    accountselect.setModel(DatabaseObject.getObject(Context.getAccounts(), MPView.getUser().__getIntdefaultaccount()));
                    fillFiles();

                    status.setModel(Item.getStatusStrings(), MPComboBoxModelItem.COMPARE_BY_ID);
                    status.setSelectedIndex(MPView.getUser().__getIntdefaultstatus());

                    itemtable.setModel(SubItem.toModel(new SubItem[]{
                                SubItem.getDefaultItem(), SubItem.getDefaultItem(),
                                SubItem.getDefaultItem(), SubItem.getDefaultItem(),
                                SubItem.getDefaultItem(), SubItem.getDefaultItem()
                            }));

                    formatTable();
                    shipping.setText(FormatNumber.formatDezimal(0d));
                } catch (Exception e) {
                    Log.Debug(this, e);
                }
            }
        };

        SwingUtilities.invokeLater(runnable);
    }

    private void formatTable() {
        TableCellRendererForDezimal t = new TableCellRendererForDezimal(itemtable);
        t.setRendererTo(6);
        t.setRendererTo(5);
        t.setRendererTo(2);
        TableCellRendererForDezimal tc = new TableCellRendererForDezimal(itemtable, Color.LIGHT_GRAY);
        tc.setRendererTo(7);

        CellRendererWithMPComboBox r = new CellRendererWithMPComboBox(Context.getProducts(), itemtable);
        r.setRendererTo(4, this);
        itemMultiplier = new TableCalculator(itemtable, new int[]{2, 5, 6}, new int[]{7}, new int[]{6}, TableCalculator.ACTION_MULTIPLY, new int[]{7});
        ((MPTableModel) itemtable.getModel()).addCalculator(itemMultiplier);
        itemMultiplier.addLabel(value, 7);

        netCalculator = new TableCalculator(itemtable, new int[]{7, 5}, new int[]{8}, new int[]{}, TableCalculator.ACTION_SUBSTRACT, new int[]{8});
        ((MPTableModel) itemtable.getModel()).addCalculator(netCalculator);
        netCalculator.addLabel(taxvalue, 8);

        netCalculator2 = new TableCalculator(itemtable, new int[]{5}, new int[]{9}, new int[]{}, TableCalculator.ACTION_SUM, new int[]{9});
        ((MPTableModel) itemtable.getModel()).addCalculator(netCalculator2);
        netCalculator2.addLabel(netvalue, 9);

        TableFormat.resizeCols(itemtable, new Integer[]{0, 23, 53, 63, 100, 83, 63, 63, 0, 0, 0}, new Boolean[]{true, true, true, true, false, true, true, true, true, true, true});
        TableFormat.changeBackground(itemtable, 1, Color.LIGHT_GRAY);
    }

    @Override
    public void paste(DatabaseObject dbo) {
        if (dbo.getDbIdentity().equals(Context.getItems().getDbIdentity())) {
            setDataOwner(dbo, true);
        } else {
            MPView.addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE.toString());
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
        saveSubItems();
    }

    @Override
    public void actionAfterCreate() {
        ArrayUtilities.replaceColumn(itemtable, 0, null);
        saveSubItems();
    }

    private void saveSubItems() {
        if (itemtable.getCellEditor() != null) {
            itemtable.getCellEditor().stopCellEditing();
        }
        SubItem.saveModel(dataOwner, (MPTableModel) itemtable.getModel());
    }

    @Override
    public void changeSelection(MPComboBoxModelItem to, Context c) {
        try {
            DatabaseObject o = DatabaseObject.getObject(c, Integer.valueOf(to.getId()));
            int i = itemtable.getSelectedRow();
            if (i >= 0) {
                ((MPTableModel) itemtable.getModel()).setRowAt(new SubItem((Product) o).getRowData(i), i, 4);
            }
        } catch (Exception ex) {
        }
    }

    private void preview() {
        PreviewPanel pr;
//        String[] arr;
        if (preloadedTemplate != null && preload) {
            if (dataOwner != null && dataOwner.isExisting()) {
                if (itemtable.getCellEditor() != null) {
                    itemtable.getCellEditor().stopCellEditing();
                }

                HashMap<String, String> hm1 = new FormFieldsHandler(dataOwner).getFormattedFormFields(null);
                File f2 = FileDirectoryHandler.getTempFile("pdf");
                Export ex = new Export();
                ex.putAll(hm1);

                Vector<String[]> l = SubItem.convertModel(dataOwner, (MPTableModel) itemtable.getModel(), preloadedTemplate);

                ex.put(TableHandler.KEY_TABLE + "1", l);
                ex.setTemplate(preloadedExportFile);
                ex.setTargetFile(f2);

                pr = new PreviewPanel();
                pr.setDataOwner(dataOwner);
                new Job(ex, pr).execute();
                saveSubItems();
//
//                arr=ex.keySet().toArray(new String[]{});
//                Arrays.sort(arr);
//                for (int i = 0; i < arr.length; i++) {
//                    String string = arr[i];
//                    System.err.println(string);
//                }

            }
        } else {
            Popup.notice(Messages.NO_TEMPLATE_LOADED);
        }
    }
    private boolean preload = false;

    private void preloadTemplate() {
        Runnable runnable = new Runnable() {

            public void run() {
                preloadedTemplate = Template.loadTemplate(dataOwner);
                if (preloadedTemplate != null) {
                    try {
                        preloadedExportFile = preloadedTemplate.getExFile();
                        preload = true;
                        button_preview.setEnabled(preload);
                    } catch (Exception e) {
                        Log.Debug(e);
                    }
                } else {
                    button_preview.setText(Messages.OO_NOT_CONFIGURED.getValue());
                    button_preview.setEnabled(false);
                }
            }
        };
        new Thread(runnable).start();
    }
}
