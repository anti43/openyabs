/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * ReceiptLookup.java
 *
 * Created on 07.11.2016, 08:56:18
 */
package mpv5.ui.panels;
  
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria2;
import mpv5.db.common.QueryParameter;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.handler.MPEnum;
import mpv5.ui.dialogs.Wizard;
import mpv5.ui.dialogs.subcomponents.wizard_MassExportPanel;
import mpv5.utils.models.MPComboBoxModelItem;

/**
 *
 * @author andreas
 */
public class ReceiptLookup extends javax.swing.JPanel {

    private static final long serialVersionUID = -8991791754204962670L;

    /**
     * Creates new form ReceiptLookup
     */
    public ReceiptLookup() {
        initComponents();

        generalListPanel1.setGroupBy(Context.getContact());
        generalListPanel1.setSortable(false);

        final Object[][] typeModel = new Object[][]{
            new Object[]{Context.getInvoice(), Item.getTypeString(Item.TYPE_INVOICE)},
            new Object[]{Context.getOffer(), Item.getTypeString(Item.TYPE_OFFER)},
            new Object[]{Context.getOrder(), Item.getTypeString(Item.TYPE_ORDER)},
            new Object[]{Context.getDelivery(), Item.getTypeString(Item.TYPE_DELIVERY_NOTE)},
            new Object[]{Context.getConfirmation(), Item.getTypeString(Item.TYPE_ORDER_CONFIRMATION)},
            new Object[]{Context.getDeposit(), Item.getTypeString(Item.TYPE_DEPOSIT)},
            new Object[]{Context.getPartPayment(), Item.getTypeString(Item.TYPE_PART_PAYMENT)},
            new Object[]{Context.getCredit(), Item.getTypeString(Item.TYPE_CREDIT)}};
        type.setModel(typeModel);
        type.setSearchEnabled(false);
        type.setSearchOnEnterEnabled(false);

        type.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //search.setText("");
                int index = type.getComboBox().getSelectedIndex();
                if (index < 0) {
                    index = 0;//type must be selected
                }
                generalListPanel1.setData((Context) typeModel[index][0], getStatusQuery());
            }
        });

        /*search.getTextField().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                generalListPanel1.setData((Context) typeModel[type.getComboBox().getSelectedIndex()][0], search.getText());
            }
        });*/
        MPEnum[] stati = new MPEnum[Item.getStatusStrings().length + 1];
        for (int i = 0; i < stati.length; i++) {
            if (i == 0) {
                stati[i] = new MPEnum() {

                    @Override
                    public Integer getId() {
                        return -2;
                    }

                    @Override
                    public String getName() {
                        return mpv5.globals.Messages.ALL.toString();
                    }
                };
            } else {
                stati[i] = Item.getStatusStrings()[i - 1];
            }
        }

        status.setModel(stati, MPComboBoxModelItem.COMPARE_BY_ID, Collections.EMPTY_LIST);
        status.setSearchEnabled(false);
        status.setSearchOnEnterEnabled(false);
        status.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //search.setText("");
                generalListPanel1.setData((Context) type.getSelectedItem().getIdObject(), getStatusQuery());
            }
        });
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                generalListPanel1.setData((Context) typeModel[0][0], getStatusQuery());
            }
        });

        modernLayout();
        generalListPanel1.getTable().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    try {
                        popup(e);
                    } catch (Exception ex) {
                        Logger.getLogger(ReceiptLookup.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    try {
                        popup(e);
                    } catch (Exception ex) {
                        Logger.getLogger(ReceiptLookup.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    private QueryCriteria2 getStatusQuery() {
        MPComboBoxModelItem s = status.getSelectedItem();
        if (s != null) {
            int id = (int) s.getIdObject();
            if (id >= 0) {
                QueryCriteria2 q = new QueryCriteria2();
                q.and(new QueryParameter(Context.getInvoice(), "intstatus", id, QueryParameter.EQUALS));
                return q;
            }
        }
        return null;
    }

    private void popup(MouseEvent e) throws Exception {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem x = new JMenuItem(mpv5.globals.Messages.RECEIPT_ADD_TO_NEW_INVOICE.toString());
        x.addActionListener(e2 -> mergeSelected());
        menu.add(x);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * The selected documents (of one or more customers) in a new invoice per
     * customer: the wizard of the mass export
     */
    private void mergeSelected() {
        {
            {
                int[] rows = generalListPanel1.getTable().getSelectedRows();
                if (rows.length == 0) {
                    return;
                }
                final Map<Contact, List<Item>> list = new HashMap<Contact, List<Item>>();
                for (int i = 0; i < rows.length; i++) {
                    int j = rows[i];
                    DatabaseObject obj = (DatabaseObject) generalListPanel1.getTable().getModel().getValueAt(generalListPanel1.getTable().convertRowIndexToModel(j), 1);
                    if (obj instanceof Item) {
                        try {
                            Contact dataOwner = ((Item) obj).getContact();
                            if (!list.containsKey(dataOwner)) {
                                list.put(dataOwner, new ArrayList<Item>());
                            }
                            list.get(dataOwner).add((Item) obj);
                        } catch (NodataFoundException ex) {
                            Logger.getLogger(ReceiptLookup.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

                Wizard w = new Wizard(false);
                w.addPanel(new wizard_MassExportPanel(w, list));
                w.showWiz();
            }
        }
    }

    private final javax.swing.JButton merge = new javax.swing.JButton(mpv5.globals.Messages.RECEIPT_ADD_TO_NEW_INVOICE.toString());

    /**
     * The modern layout: title and count, the filters in a card (kind and
     * status apply at once, period and group with "Filter", the blue main
     * action), the list with "Combine into new invoice" and a hint
     */
    private void modernLayout() {
        removeAll();
        setLayout(new java.awt.BorderLayout(mpv5.ui.modern.ModernTheme.px(10), mpv5.ui.modern.ModernTheme.px(10)));
        setBorder(mpv5.ui.modern.ModernTheme.padding(12, 14));
        setBackground(mpv5.ui.modern.ModernTheme.background());

        javax.swing.JLabel title = new javax.swing.JLabel(mpv5.i18n.LanguageManager.getBundle().getString("ReceiptLookup.jPanel1.border.title"));
        title.setFont(mpv5.ui.modern.ModernTheme.font(9, java.awt.Font.BOLD));
        javax.swing.JLabel count = generalListPanel1.getCountLabel();
        count.setForeground(mpv5.ui.modern.ModernTheme.muted());
        count.setFont(mpv5.ui.modern.ModernTheme.font(0, java.awt.Font.PLAIN));
        javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, mpv5.ui.modern.ModernTheme.px(10), 0));
        header.setOpaque(false);
        header.add(title);
        header.add(count);

        for (mpv5.ui.beans.LabeledCombobox c : new mpv5.ui.beans.LabeledCombobox[]{type, status, generalListPanel1.getGroupChoice()}) {
            mpv5.ui.modern.ModernTheme.roomyCombo(c);
        }
        javax.swing.JPanel filterRow = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        filterRow.setOpaque(false);
        filterRow.add(generalListPanel1.getFilterButton());
        mpv5.ui.modern.Card filters = new mpv5.ui.modern.Card(null)
                .row(mpv5.ui.modern.Card.field(mpv5.globals.Messages.RECEIPT_TYPE.toString(), type.getComboBox()),
                        mpv5.ui.modern.Card.field(mpv5.globals.Messages.RECEIPT_STATUS.toString(), status.getComboBox()),
                        mpv5.ui.modern.Card.field(mpv5.globals.Messages.RECEIPT_PERIOD.toString(), generalListPanel1.getTimeframeChooser(), 2),
                        mpv5.ui.modern.Card.field(mpv5.globals.Messages.RECEIPT_GROUP.toString(), generalListPanel1.getGroupChoice().getComboBox()),
                        mpv5.ui.modern.Card.field(" ", filterRow));

        javax.swing.JScrollPane sp = generalListPanel1.getListScroll();
        sp.setBorder(javax.swing.BorderFactory.createLineBorder(mpv5.ui.modern.ModernTheme.border()));
        //no table style: it would show the header row (0 1 2 ...) the grouped list hides
        merge.addActionListener(e -> mergeSelected());
        merge.setEnabled(false);
        generalListPanel1.getTable().getSelectionModel().addListSelectionListener(e ->
                merge.setEnabled(generalListPanel1.getTable().getSelectedRowCount() > 0));
        javax.swing.JPanel buttons = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        buttons.setOpaque(false);
        buttons.add(merge);
        mpv5.ui.modern.Card list = new mpv5.ui.modern.Card(null)
                .addFull(sp, 1)
                .addFull(buttons)
                .addFull(mpv5.ui.modern.Applets.hint(mpv5.globals.Messages.RECEIPT_HINT.toString()));

        javax.swing.JPanel top = new javax.swing.JPanel(new java.awt.BorderLayout(0, mpv5.ui.modern.ModernTheme.px(10)));
        top.setOpaque(false);
        top.add(header, java.awt.BorderLayout.NORTH);
        top.add(filters, java.awt.BorderLayout.CENTER);
        add(top, java.awt.BorderLayout.NORTH);
        add(list, java.awt.BorderLayout.CENTER);
        revalidate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        generalListPanel1 = new mpv5.ui.panels.GeneralListPanel();
        jPanel1 = new javax.swing.JPanel();
        type = new mpv5.ui.beans.LabeledCombobox();
        status = new mpv5.ui.beans.LabeledCombobox();

        setName("Form"); // NOI18N

        java.util.ResourceBundle resourceMap = mpv5.i18n.LanguageManager.getBundle();
        generalListPanel1.setName("generalListPanel1"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("ReceiptLookup.jPanel1.border.title")));
        jPanel1.setName("jPanel1"); // NOI18N

        type.set_Label(resourceMap.getString("ReceiptLookup.type._Label")); // NOI18N
        type.setName("type"); // NOI18N

        status.set_Label(resourceMap.getString("ReceiptLookup.status._Label")); // NOI18N
        status.setName("status"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(type, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(status, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(status, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(43, Short.MAX_VALUE))
            .addComponent(generalListPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 681, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(generalListPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.panels.GeneralListPanel generalListPanel1;
    private javax.swing.JPanel jPanel1;
    private mpv5.ui.beans.LabeledCombobox status;
    private mpv5.ui.beans.LabeledCombobox type;
    // End of variables declaration//GEN-END:variables
}
