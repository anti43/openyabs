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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import mpv5.db.common.*;
import mpv5.db.objects.Product;
import mpv5.globals.Messages;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.ProductList;
import mpv5.db.objects.ProductlistSubItem;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.User;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.toolbars.DataPanelTB;
import mpv5.ui.beans.MPCBSelectionChangeReceiver;
import mpv5.ui.dialogs.Search2;
import mpv5.ui.dialogs.subcomponents.ProductSelectDialog;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.export.Export;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.renderer.ButtonEditor;
import mpv5.utils.renderer.ButtonRenderer;
import mpv5.utils.renderer.CellEditorWithMPComboBox;
import mpv5.utils.renderer.TableCellRendererForDezimal;
import mpv5.utils.tables.DynamicTableCalculator;
import mpv5.utils.tables.TableFormat;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 * 
 */
public class ProductListsPanel extends javax.swing.JPanel implements DataPanel, MPCBSelectionChangeReceiver {

    private static final long serialVersionUID = 1L;
    private ProductList dataOwner;
    private DataPanelTB tb;
    private DynamicTableCalculator itemMultiplier;
    private DynamicTableCalculator netCalculator;
    private DynamicTableCalculator netCalculator2;
    private final SearchPanel sp;
    private java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();

    /**
     *
     */
    public ProductListsPanel() {
        initComponents();
        setName("productlistpanel");
        itemtable.getTableHeader().setReorderingAllowed(false);

        sp = new SearchPanel(Context.getProductList(), this);
        sp.setVisible(true);
        tb = new mpv5.ui.toolbars.DataPanelTB(this);
//        tb.disableButton(1);
        tb.disableButton(8);
        tb.disableButton(9);
        toolbarpanetbp.add(tb, BorderLayout.CENTER);
        dataOwner = new ProductList();

        refresh();
        addedby.setText(mpv5.db.objects.User.getCurrentUser().getName());
        groupnameselect.setContext(Context.getGroup());
        groupnameselect.setSearchEnabled(true);
        itemtable.getTableHeader().addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    MPTableModel m = (MPTableModel) itemtable.getModel();
                    if (m.getRowCount() > 0) {
                        m.addRow(5);
                    } else {

                        itemtable.setModel(ProductlistSubItem.toModel(new ProductlistSubItem[]{
                                    ProductlistSubItem.getDefaultItem(), ProductlistSubItem.getDefaultItem(),
                                    ProductlistSubItem.getDefaultItem(), ProductlistSubItem.getDefaultItem(),
                                    ProductlistSubItem.getDefaultItem(), ProductlistSubItem.getDefaultItem()
                                }));


                        formatTable();

                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {

                    MPTableModel m = (MPTableModel) itemtable.getModel();
                    Product p = (Product) Popup.SelectValue(Context.getProduct());
                    if (p != null) {
                        int row = m.getLastValidRow(new int[]{4});
                        m.setRowAt(new ProductlistSubItem(p).getRowData(row), row + 1, 1);
                    }
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

        listname.setSearchOnEnterEnabled(true);
        listname.setParent(this);
        listname.setSearchField("cname");
        listname.setContext(Context.getProductList());

        ((SpinnerNumberModel) calculator.getSpinner().getModel()).setMinimum(-1000);
        ((SpinnerNumberModel) calculator.getSpinner().getModel()).setMaximum(1000);

        value.set_ValueClass(BigDecimal.class);
        netvalue.set_ValueClass(BigDecimal.class);

    }

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean populate) {
        dataOwner = (ProductList) object;
        if (populate) {
            dataOwner.setPanelData(this);
            this.exposeData();
            prinitingComboBox1.init(rightpane);
            tb.setFavourite(Favourite.isFavourite(object));
            tb.setEditable(!object.isReadOnly());
            try {

                itemtable.setModel(ProductlistSubItem.toModel(ProductlistSubItem.getList(dataOwner.__getIDS()).toArray(new ProductlistSubItem[0])));
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex.getMessage());
            } finally {
            }
            if (((MPTableModel) itemtable.getModel()).getEmptyRows(new int[]{4}) < 2) {
                ((MPTableModel) itemtable.getModel()).addRow(1);
            }
            omodel = (MPTableModel) itemtable.getModel();

            formatTable();
            ((MPTableModel) itemtable.getModel()).fireTableCellUpdated(0, 0);
            if (object.isReadOnly()) {
                Popup.notice(Messages.LOCKED_BY);
            }
            validate();
        }
    }

    @Override
    public void showRequiredFields() {
        TextFieldUtils.blinkerRed(listname);
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
        listname = new mpv5.ui.beans.LabeledTextField();
        addedby = new javax.swing.JLabel();
        prinitingComboBox1 = new mpv5.ui.beans.PrinitingComboBox();
        groupnameselect = new mpv5.ui.beans.LabeledCombobox();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        itemtable = new  JTable() {
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
        jScrollPane1 = new javax.swing.JScrollPane();
        notes = new javax.swing.JTextPane();
        jToolBar2 = new javax.swing.JToolBar();
        calculator = new mpv5.ui.beans.LabeledSpinner();
        jButton1 = new javax.swing.JButton();
        jSeparator12 = new javax.swing.JToolBar.Separator();
        netvalue = new mpv5.ui.beans.LabeledTextField();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        taxvalue = new mpv5.ui.beans.LabeledTextField();
        jSeparator10 = new javax.swing.JToolBar.Separator();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        value = new mpv5.ui.beans.LabeledTextField();
        toolbarpanetbp = new javax.swing.JPanel();

        setBackground(javax.swing.UIManager.getDefaults().getColor("InternalFrame.inactiveTitleBackground"));
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ProductListsPanel.border.title_1"))); // NOI18N
        setName("Form"); // NOI18N
        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        leftpane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        leftpane.setName("leftpane"); // NOI18N
        leftpane.setLayout(new java.awt.BorderLayout());
        add(leftpane, java.awt.BorderLayout.WEST);

        rightpane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        rightpane.setName("rightpane"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setName("jPanel1"); // NOI18N

        listname.set_Label(bundle.getString("ProductListsPanel.listname._Label")); // NOI18N
        listname.setFocusable(false);
        listname.setFont(listname.getFont());
        listname.setName("listname"); // NOI18N

        addedby.setFont(addedby.getFont());
        addedby.setText(bundle.getString("ProductListsPanel.addedby.text")); // NOI18N
        addedby.setToolTipText(bundle.getString("ProductListsPanel.addedby.toolTipText")); // NOI18N
        addedby.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        addedby.setEnabled(false);
        addedby.setName("addedby"); // NOI18N

        prinitingComboBox1.setName("prinitingComboBox1"); // NOI18N

        groupnameselect.set_Label(bundle.getString("ProductListsPanel.groupnameselect._Label")); // NOI18N
        groupnameselect.setName("groupnameselect"); // NOI18N

        jButton2.setText(bundle.getString("ProductListsPanel.jButton2.text_1")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText(bundle.getString("ProductListsPanel.jButton3.text")); // NOI18N
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setName("jButton3"); // NOI18N
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(groupnameselect, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(listname, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 123, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(prinitingComboBox1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addedby, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(listname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(groupnameselect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(prinitingComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addedby, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

//$2java.awt.Color(204, 204, 204));
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
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 591, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
        );

        jScrollPane1.setBorder(null);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        notes.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        notes.setText(bundle.getString("ProductListsPanel.notes.text")); // NOI18N
        notes.setName("notes"); // NOI18N
        jScrollPane1.setViewportView(notes);

//$2java.awt.Color(255, 255, 255));
        jToolBar2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 1, true));
        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);
        jToolBar2.setName("jToolBar2"); // NOI18N

        calculator.set_Label(bundle.getString("ProductListsPanel.calculator._Label")); // NOI18N
        calculator.setMaximumSize(new java.awt.Dimension(200, 20));
        calculator.setName("calculator"); // NOI18N
        jToolBar2.add(calculator);

        jButton1.setText(bundle.getString("ProductListsPanel.jButton1.text")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar2.add(jButton1);

        jSeparator12.setName("jSeparator12"); // NOI18N
        jSeparator12.setSeparatorSize(new java.awt.Dimension(15, 10));
        jToolBar2.add(jSeparator12);

        netvalue.set_Label(bundle.getString("ProductListsPanel.netvalue._Label")); // NOI18N
        netvalue.setName("netvalue"); // NOI18N
        jToolBar2.add(netvalue);

        jSeparator9.setName("jSeparator9"); // NOI18N
        jToolBar2.add(jSeparator9);

        jSeparator6.setName("jSeparator6"); // NOI18N
        jSeparator6.setSeparatorSize(new java.awt.Dimension(15, 10));
        jToolBar2.add(jSeparator6);

        taxvalue.set_Label(bundle.getString("ProductListsPanel.taxvalue._Label")); // NOI18N
        taxvalue.setName("taxvalue"); // NOI18N
        jToolBar2.add(taxvalue);

        jSeparator10.setName("jSeparator10"); // NOI18N
        jToolBar2.add(jSeparator10);

        jSeparator5.setName("jSeparator5"); // NOI18N
        jSeparator5.setSeparatorSize(new java.awt.Dimension(15, 10));
        jToolBar2.add(jSeparator5);

        value.set_Label(bundle.getString("ProductListsPanel.value._Label")); // NOI18N
        value.setName("value"); // NOI18N
        jToolBar2.add(value);

        javax.swing.GroupLayout rightpaneLayout = new javax.swing.GroupLayout(rightpane);
        rightpane.setLayout(rightpaneLayout);
        rightpaneLayout.setHorizontalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
            .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        rightpaneLayout.setVerticalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightpaneLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        add(rightpane, java.awt.BorderLayout.CENTER);

        toolbarpanetbp.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        toolbarpanetbp.setName("toolbarpanetbp"); // NOI18N
        toolbarpanetbp.setLayout(new java.awt.BorderLayout());
        add(toolbarpanetbp, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void itemtableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_itemtableMouseClicked

        MPTableModel m = (MPTableModel) itemtable.getModel();
        if (!m.hasEmptyRows(new int[]{4})) {
            m.addRow(2);
        }
    }//GEN-LAST:event_itemtableMouseClicked
    MPTableModel omodel = null;
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (omodel == null) {
            omodel = (MPTableModel) itemtable.getModel();
        }
        if (omodel.getValidRows(new int[]{4}).size() > 0) {

            itemtable.setModel(omodel);
            ProductlistSubItem.changeValueFields(itemtable, Integer.valueOf(calculator.get_Value().toString()), this);
            ((MPTableModel) itemtable.getModel()).fireTableCellUpdated(0, 0);
            ((MPTableModel) itemtable.getModel()).addRow(1);

        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

  
            if (dataOwner == null) {
                dataOwner = new ProductList();
            }
            asproduct_ = true;
            actionBeforeSave();
            if (dataOwner.getPanelData(this) && dataOwner.save()) {
            } else {
                showRequiredFields();
            }

            Product p = new Product();
            p.setCname(dataOwner.__getCname());
            p.setProductlistsids(dataOwner.__getIDS());
            p.setExternalnetvalue(value.getValue(BigDecimal.ONE));
            p.setInternalnetvalue(netvalue.getValue(BigDecimal.ONE));
            p.setInttype(Product.TYPE_PRODUCT);
            p.setIntinventorytype(0);

            if (itemtable.getCellEditor() != null) {
                try {
                    itemtable.getCellEditor().stopCellEditing();
                } catch (Exception e) {
                }
            }
            List<Object[]> rowsl = ((MPTableModel) itemtable.getModel()).getValidRows(new int[]{4});
            Log.Debug(SubItem.class, "Rows found: " + rowsl.size());
            String descr = "";
            for (int i = 0; i < rowsl.size(); i++) {
                Object[] row = rowsl.get(i);
                for (int j = 0; j < row.length; j++) {
                    if (row[j] == null) {
                        row[j] = "";
                    }
                }
                descr += row[4].toString() + "\n";
            }
            p.setDescription(descr);

            if (p.save()) {
                Popup.notice(Messages.BOM_CREATED.toString() + p);
                mpv5.YabsViewProxy.instance().addTab(p);

            }
        
}//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        final Item i = (Item) Search2.showSearchFor(Context.getInvoice());
        if (i != null && dataOwner != null && dataOwner.isExisting()) {
            mpv5.YabsViewProxy.instance().addTab(i);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    
                    mpv5.YabsViewProxy.instance().getCurrentTab().paste(dataOwner);
                }
            };
            SwingUtilities.invokeLater(runnable);
        }
    }//GEN-LAST:event_jButton3ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addedby;
    private javax.swing.ButtonGroup buttonGroup1;
    private mpv5.ui.beans.LabeledSpinner calculator;
    private mpv5.ui.beans.LabeledCombobox groupnameselect;
    private javax.swing.JTable itemtable;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JToolBar.Separator jSeparator10;
    private javax.swing.JToolBar.Separator jSeparator12;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator9;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JPanel leftpane;
    private mpv5.ui.beans.LabeledTextField listname;
    private mpv5.ui.beans.LabeledTextField netvalue;
    private javax.swing.JTextPane notes;
    private mpv5.ui.beans.PrinitingComboBox prinitingComboBox1;
    private javax.swing.JPanel rightpane;
    private mpv5.ui.beans.LabeledTextField taxvalue;
    private javax.swing.JPanel toolbarpanetbp;
    private mpv5.ui.beans.LabeledTextField value;
    // End of variables declaration//GEN-END:variables
    public String cname_;
    public String description_;
    public int intaddedby_;
    public int ids_;
    public Date dateadded_;
    public Group group_;
    public boolean asproduct_;

    @Override
    public boolean collectData() {
        cname_ = listname.getText();
        if (cname_.length() > 0) {
            description_ = notes.getText();
             if (groupnameselect.getSelectedItem() != null) {
              try {
                  int group = Integer.valueOf(groupnameselect.getSelectedItem().getId());
                  group_ = (Group) DatabaseObject.getObject(Context.getGroup(), group);
                  Log.Debug(this, groupnameselect.getSelectedItem().getId());
              } catch (NodataFoundException ex) {
                  Log.Debug(this, ex);
              }
          }
            return true;
        } else {
            showRequiredFields();
            return false;
        }
    }

    @Override
    public void exposeData() {
        listname.setText(cname_);
        notes.setText(description_);
        try {
            groupnameselect.setModel( group_);
        } catch ( Exception ex) {
            Log.Debug(this, ex.getMessage());
        }

        addedby.setText(User.getUsername(intaddedby_));
    }

    @Override
    public final void refresh() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {

                    groupnameselect.setModel(MPComboBoxModelItem.toModel(DatabaseObject.getObject(Context.getGroup(), mpv5.db.objects.User.getCurrentUser().__getGroupsids())));
                    groupnameselect.setSelectedIndex(0);
                    itemtable.setModel(ProductlistSubItem.toModel(new ProductlistSubItem[]{
                                ProductlistSubItem.getDefaultItem(), ProductlistSubItem.getDefaultItem(),
                                ProductlistSubItem.getDefaultItem(), ProductlistSubItem.getDefaultItem(),
                                ProductlistSubItem.getDefaultItem(), ProductlistSubItem.getDefaultItem(),
                                ProductlistSubItem.getDefaultItem(), ProductlistSubItem.getDefaultItem(),
                                ProductlistSubItem.getDefaultItem(), ProductlistSubItem.getDefaultItem(),
                                ProductlistSubItem.getDefaultItem(), ProductlistSubItem.getDefaultItem()
                            }));
                    formatTable();

                } catch (Exception e) {
                    Log.Debug(this, e);
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
        TableFormat.resizeCols(itemtable, new Integer[]{0, 23, 53, 63, 100, 83, 63, 63, 0, 0, 0, 20, 20, 0, 0, 0, 0}, new Boolean[]{true, true, true, true, false, true, true, true, true, true, true, true, true, true, true, true, true, true});
        TableFormat.changeBackground(itemtable, 1, Color.LIGHT_GRAY);
        if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidecolumnquantity")) {
            TableFormat.stripColumn(itemtable, 2);
        }
        if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidecolumnmeasure")) {
            TableFormat.stripColumn(itemtable, 3);
        }


    }

    @Override
    public void paste(DatabaseObject... dbos) {
        for (DatabaseObject dbo : dbos) {
            if (dbo.getContext().equals(Context.getProduct())) {
                MPTableModel m = (MPTableModel) itemtable.getModel();
                m.addRow(ProductlistSubItem.toRow((Product) dbo).getRowData(m.getValidRows(new int[]{4}).size()));

                itemtable.setModel(m);
                omodel = m;

            } else if (dbo.getContext().equals(Context.getProductList())) {
                setDataOwner(dbo, true);
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE.toString(), Color.YELLOW);
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
        saveProductlistSubItems();
        omodel = (MPTableModel) itemtable.getModel();
        if (dataOwner.__isAsproduct()) {
            try {
                QueryCriteria c = new QueryCriteria("productlistsids", dataOwner.__getIDS());
                Product p = (Product) Product.getObjects(Context.getProduct(), c).get(0);
                p.setCname(dataOwner.__getCname());
                p.setProductlistsids(dataOwner.__getIDS());
                p.setExternalnetvalue(value.getValue(BigDecimal.ONE));
                p.setInternalnetvalue(netvalue.getValue(BigDecimal.ONE));
                p.setInttype(Product.TYPE_PRODUCT);
                p.setIntinventorytype(0);
                if (p.save()) {
                    Popup.notice(Messages.BOM_CREATED.toString() + p);
                    mpv5.YabsViewProxy.instance().addTab(p);
                }
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
    }

    @Override
    public void actionAfterCreate() {
        ArrayUtilities.replaceColumn(itemtable, 0, null);
        saveProductlistSubItems();
        omodel = (MPTableModel) itemtable.getModel();
    }

    private void saveProductlistSubItems() {
        if (itemtable.getCellEditor() != null) {
            try {
                itemtable.getCellEditor().stopCellEditing();
            } catch (Exception e) {
            }
        }
        ProductlistSubItem.saveModel((MPTableModel) itemtable.getModel(), dataOwner.__getIDS());
    }

    @Override
    public void changeSelection(MPComboBoxModelItem to, Context c) {
        try {
            DatabaseObject o = DatabaseObject.getObject(c, Integer.valueOf(to.getId()));
            int i = itemtable.getSelectedRow();
            if (i >= 0) {
                ((MPTableModel) itemtable.getModel()).setRowAt(new ProductlistSubItem((Product) o).getRowData(i), i, 4);
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public void actionBeforeCreate() {
    }

    @Override
    public void actionBeforeSave() {
    }

    @Override
    public void mail() {
    }

    @Override
    public void print() {
        Export.print(this);
    }

    private void prepareTable() {
        TableCellRendererForDezimal t = new TableCellRendererForDezimal(itemtable);
        t.setRendererTo(6);
        t.setRendererTo(5);
        t.setRendererTo(2);
        TableCellRendererForDezimal tc = new TableCellRendererForDezimal(itemtable, new java.awt.Color(161, 176, 190));
        tc.setRendererTo(7);

        CellEditorWithMPComboBox r = new CellEditorWithMPComboBox(Context.getProduct(), itemtable);
        r.setEditorTo(4, this, false);
        itemMultiplier =new DynamicTableCalculator(itemtable, "(([2]*[5])+([2]*[5]%[6]))", new int[]{7});
        ((MPTableModel) itemtable.getModel()).addCalculator(itemMultiplier);
        itemMultiplier.addLabel(value, 7);

        netCalculator = new DynamicTableCalculator(itemtable, "([2]*[5]%[6])", new int[]{8});
        ((MPTableModel) itemtable.getModel()).addCalculator(netCalculator);
        netCalculator.addLabel(taxvalue, 8);

        netCalculator2 = new DynamicTableCalculator(itemtable, "[2]*[5]", new int[]{9});
        ((MPTableModel) itemtable.getModel()).addCalculator(netCalculator2);
        netCalculator2.addLabel(netvalue, 9);

        JButton b1 = new JButton();
        b1.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                ProductSelectDialog.instanceOf((MPTableModel) itemtable.getModel(), itemtable.getSelectedRow(), e, 0, null, null);
                if (((MPTableModel) itemtable.getModel()).getEmptyRows(new int[]{4}) < 2) {
                    ((MPTableModel) itemtable.getModel()).addRow(1);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        JButton b2 = new JButton();
        b2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                MPTableModel m = (MPTableModel) itemtable.getModel();
                int row = itemtable.getSelectedRow();
                m.setRowAt(ProductlistSubItem.getDefaultItem().getRowData(row), row, 1);
            }
        });

        try {
            itemtable.getColumnModel().getColumn(itemtable.getColumnModel().getColumnIndex("A")).setCellRenderer(new ButtonRenderer());
        } catch (Exception e) {
        }
        try {
            itemtable.getColumnModel().getColumn(itemtable.getColumnModel().getColumnIndex("A")).setCellEditor(new ButtonEditor(b1));
        } catch (Exception e) {
        }
        try {
            itemtable.getColumnModel().getColumn(itemtable.getColumnModel().getColumnIndex("C")).setCellRenderer(new ButtonRenderer());
        } catch (Exception e) {
        }
        try {
            itemtable.getColumnModel().getColumn(itemtable.getColumnModel().getColumnIndex("C")).setCellEditor(new ButtonEditor(b2));
        } catch (Exception e) {
        }
 }
}
