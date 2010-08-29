/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ProductsOverview.java
 *
 * Created on Aug 27, 2010, 1:48:39 PM
 */
package mpv5.ui.panels;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import mpv5.ui.misc.MPTable;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryCriteria2;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.QueryParameter;
import mpv5.db.objects.Group;
import mpv5.db.objects.Product;
import mpv5.db.objects.ProductGroup;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.BigPopup;
import mpv5.ui.misc.DnDTree;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.trees.TreeFormat;

/**
 *
 * @author andreas.weber
 */
public class ProductsOverview extends javax.swing.JPanel {

    /** Creates new form ProductsOverview */
    public ProductsOverview() {
        initComponents();
        setName("productsoverview");
        both.setSelected(true);
        addedafter.setDate(DateConverter.addYears(new Date(), -10));
        fillTree();
        filltable(null, null);
        search.getTextField().addActionListener(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                search();
            }
        });
    }

    private void search() {
        List<ProductGroup> gs = new ArrayList<ProductGroup>();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) gtree.getLastSelectedPathComponent();
        if (node != null) {
            ProductGroup g = (ProductGroup) node.getUserObject();
            gs.add(g);
        }

        filltable(search.getText(), gs);
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
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        gtree = new DnDTree();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        listtable = new MPTable(this);
        jToolBar1 = new javax.swing.JToolBar();
        products = new javax.swing.JRadioButton();
        services = new javax.swing.JRadioButton();
        both = new javax.swing.JRadioButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        addedafter = new mpv5.ui.beans.LabeledDateChooser();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        search = new mpv5.ui.beans.LabeledTextField();
        jButton1 = new javax.swing.JButton();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        gtree.setLargeModel(true);
        gtree.setName("gtree"); // NOI18N
        gtree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gtreeMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(gtree);

        jSplitPane1.setLeftComponent(jScrollPane1);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        listtable.setAutoCreateRowSorter(true);
        listtable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        listtable.setDragEnabled(true);
        listtable.setName("listtable"); // NOI18N
        listtable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listtable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listtableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(listtable);

        jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        buttonGroup1.add(products);
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();
        products.setText(bundle.getString("ProductsOverview.products.text")); // NOI18N
        products.setFocusable(false);
        products.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        products.setName("products"); // NOI18N
        products.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(products);

        buttonGroup1.add(services);
        services.setText(bundle.getString("ProductsOverview.services.text")); // NOI18N
        services.setFocusable(false);
        services.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        services.setName("services"); // NOI18N
        services.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(services);

        buttonGroup1.add(both);
        both.setText(bundle.getString("ProductsOverview.both.text")); // NOI18N
        both.setFocusable(false);
        both.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        both.setName("both"); // NOI18N
        both.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(both);

        jSeparator2.setMaximumSize(new java.awt.Dimension(10, 0));
        jSeparator2.setMinimumSize(new java.awt.Dimension(10, 0));
        jSeparator2.setName("jSeparator2"); // NOI18N
        jSeparator2.setOpaque(true);
        jSeparator2.setPreferredSize(new java.awt.Dimension(10, 0));
        jToolBar1.add(jSeparator2);

        addedafter.set_Label(bundle.getString("ProductsOverview.addedafter._Label")); // NOI18N
        addedafter.setMaximumSize(new java.awt.Dimension(226, 21));
        addedafter.setMinimumSize(new java.awt.Dimension(226, 21));
        addedafter.setName("addedafter"); // NOI18N
        jToolBar1.add(addedafter);

        jSeparator1.setMaximumSize(new java.awt.Dimension(10, 0));
        jSeparator1.setMinimumSize(new java.awt.Dimension(10, 0));
        jSeparator1.setName("jSeparator1"); // NOI18N
        jSeparator1.setOpaque(true);
        jSeparator1.setPreferredSize(new java.awt.Dimension(10, 0));
        jToolBar1.add(jSeparator1);

        search.set_Label(bundle.getString("ProductsOverview.search._Label")); // NOI18N
        search.setName("search"); // NOI18N
        jToolBar1.add(search);

        jButton1.setText(bundle.getString("ProductsOverview.jButton1.text")); // NOI18N
        jButton1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(55, 19));
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        jPanel1.add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        jSplitPane1.setRightComponent(jPanel1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void listtableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listtableMouseClicked
        doPopup(evt);
    }//GEN-LAST:event_listtableMouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        search();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void gtreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gtreeMouseClicked
       search();
    }//GEN-LAST:event_gtreeMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.beans.LabeledDateChooser addedafter;
    private javax.swing.JRadioButton both;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTree gtree;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JTable listtable;
    private javax.swing.JRadioButton products;
    private mpv5.ui.beans.LabeledTextField search;
    private javax.swing.JRadioButton services;
    // End of variables declaration//GEN-END:variables

    private void fillTree() {
        ArrayList<ProductGroup> data = null;
        try {
            data = DatabaseObject.getObjects(Context.getProductGroup());
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }

        ProductGroup g;
        try {
            g = (ProductGroup) DatabaseObject.getObject(Context.getProductGroup(), 1);
        } catch (NodataFoundException ex) {
            g = new ProductGroup(Messages.GROUPNAMES.toString());
            g.setIDS(-1);
        }

        gtree.setModel(ProductGroup.toTreeModel(data, g));
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                TreeFormat.expandTree(gtree);
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    private void doPopup(MouseEvent evt) {

        if (evt.isPopupTrigger()) {
        } else {
            if (evt.getClickCount() > 1) {
                try {
                    Product p = (Product) DatabaseObject.getObject(Context.getProduct(), Integer.valueOf(listtable.getModel().getValueAt(listtable.getSelectedRow(), 0).toString()));
                    ProductPanel pan = new ProductPanel(p);
                    BigPopup.showPopup(this, pan, p.__getCName());
                } catch (NodataFoundException ex) {
                }
            }

        }
    }

    private void filltable(String search, List<ProductGroup> groups) {

        QueryCriteria2 qc = new QueryCriteria2();
        if (search != null) {
            List<QueryParameter> ps = new ArrayList<QueryParameter>();
            for (Map.Entry<String, Class<?>> en : new Product().getKeySet()) {
                if (en.getValue().isAssignableFrom(String.class)) {
                    ps.add(new QueryParameter(Context.getProduct(), en.getKey(), search, QueryParameter.LIKE));
                }
            }
            qc.or(ps.toArray(new QueryParameter[0]));
        }

        if (!both.isSelected()) {
            int type = Product.TYPE_PRODUCT;
            if (services.isSelected()) {
                type = Product.TYPE_SERVICE;
            }
            qc.and(new QueryParameter(Context.getProduct(), "inttype", type, QueryParameter.EQUALS));
        }

        if (groups != null) {
            List<QueryParameter> ps = new ArrayList<QueryParameter>();
            for (int i = 0; i < groups.size(); i++) {
                ProductGroup group = groups.get(i);
                ps.add(new QueryParameter(Context.getProduct(), "productgroupsids", group.__getIDS(), QueryParameter.EQUALS));
            }
            qc.or(ps.toArray(new QueryParameter[0]));
        }

        Context c = Context.getProduct();
        c.addReference(Context.getContact().getDbIdentity(), "ids", "suppliersids");
        c.addReference(Context.getGroup());

        try {
            listtable.setModel(new MPTableModel(QueryHandler.instanceOf().clone(c).select(Context.DETAILS_PRODUCTS, qc, new vTimeframe(this.addedafter.getDate(), new Date())).getData()));
        } catch (NodataFoundException ex) {
            listtable.setModel(new MPTableModel());
        }
    }
}
