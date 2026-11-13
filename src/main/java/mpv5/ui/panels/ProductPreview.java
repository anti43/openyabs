/*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.panels;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Product;
import mpv5.globals.Messages;
import mpv5.logging.Log;

/**
 * For a list of products: a preview of the selected product (picture, price,
 * stock, description) at the right, a hint below, and a new invoice with the
 * selected products (Enter or the button) with the cursor in its customer box.
 */
public final class ProductPreview extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private final JTable listtable;
    private final int idColumn;

    /**
     * @param table the list (multiple selection)
     * @param idColumn the model column with the ids of the products
     * @param host gets the preview (EAST) and the hint (SOUTH), a BorderLayout panel
     * @param buttons gets the "new invoice" button, e.g. the toolbar
     * @return the preview
     */
    public static ProductPreview attach(JTable table, int idColumn, javax.swing.JPanel host, java.awt.Container buttons) {
        ProductPreview p = new ProductPreview(table, idColumn);
        p.install(host, buttons);
        return p;
    }

    /**
     * @return the column names of Context.DETAILS_PRODUCTS (the first, the id, is hidden)
     */
    public static String[] productColumns() {
        return new String[]{"ids", Messages.PRODUCTS_COL_NUMBER.toString(), Messages.PRODUCTS_COL_NAME.toString(), "EAN",
            Messages.PRODUCTS_COL_REFERENCE.toString(), Messages.PRODUCTS_COL_SUPPLIER.toString(), Messages.PRODUCTS_COL_GROUP.toString(),
            Messages.PRODUCTS_COL_PURCHASE.toString(), Messages.PRODUCTS_COL_SALE.toString(), Messages.PRODUCTS_STOCK.toString()};
    }

    private ProductPreview(JTable table, int idColumn) {
        super(new java.awt.BorderLayout(0, mpv5.ui.modern.ModernTheme.px(8)));
        this.listtable = table;
        this.idColumn = idColumn;
    }
    private final javax.swing.JLabel previewImage = new javax.swing.JLabel("", javax.swing.SwingConstants.CENTER);
    private final javax.swing.JLabel previewTitle = new javax.swing.JLabel();
    private final javax.swing.JLabel previewFacts = new javax.swing.JLabel();
    private final javax.swing.JTextArea previewText = new javax.swing.JTextArea();
    private final javax.swing.JButton invoiceButton = new javax.swing.JButton();
    private final java.util.Map<String, javax.swing.ImageIcon> images = new java.util.LinkedHashMap<String, javax.swing.ImageIcon>(32, 0.75f, true) {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<String, javax.swing.ImageIcon> e) {
            return size() > 40;
        }
    };
    private int previewId = -1;

    private void install(javax.swing.JPanel jPanel1, java.awt.Container jToolBar1) {
        int px = mpv5.ui.modern.ModernTheme.px(1);
        javax.swing.JPanel preview = this;
        preview.setPreferredSize(new java.awt.Dimension(270 * px, 10));
        previewImage.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        //a thin line between the list and the preview
        preview.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 0, mpv5.ui.modern.ModernTheme.border()),
                javax.swing.BorderFactory.createEmptyBorder(10 * px, 12 * px, 10 * px, 10 * px)));
        previewImage.setForeground(mpv5.ui.modern.ModernTheme.muted());
        previewTitle.setFont(mpv5.ui.modern.ModernTheme.font(13f, java.awt.Font.BOLD));
        previewFacts.setForeground(mpv5.ui.modern.ModernTheme.muted());
        previewText.setEditable(false);
        previewText.setLineWrap(true);
        previewText.setWrapStyleWord(true);
        previewText.setOpaque(false);
        previewText.setFocusable(false);
        javax.swing.JPanel texts = new javax.swing.JPanel();
        texts.setLayout(new javax.swing.BoxLayout(texts, javax.swing.BoxLayout.Y_AXIS));
        texts.setOpaque(false);
        for (javax.swing.JComponent c : new javax.swing.JComponent[]{previewTitle, previewFacts}) {
            c.setAlignmentX(LEFT_ALIGNMENT);
            texts.add(c);
        }
        preview.add(previewImage, java.awt.BorderLayout.NORTH);
        javax.swing.JPanel middle = new javax.swing.JPanel(new java.awt.BorderLayout(0, 6 * px));
        middle.setOpaque(false);
        middle.add(texts, java.awt.BorderLayout.NORTH);
        middle.add(previewText, java.awt.BorderLayout.CENTER);
        preview.add(middle, java.awt.BorderLayout.CENTER);
        jPanel1.add(preview, java.awt.BorderLayout.EAST);

        javax.swing.JLabel hint = new javax.swing.JLabel(Messages.PRODUCTS_HINT.toString());
        hint.setForeground(mpv5.ui.modern.ModernTheme.muted());
        hint.setBorder(javax.swing.BorderFactory.createEmptyBorder(4 * px, 8 * px, 4 * px, 8 * px));
        jPanel1.add(hint, java.awt.BorderLayout.SOUTH);

        invoiceButton.setFocusable(false);
        invoiceButton.addActionListener(e -> newInvoice());
        if (jToolBar1 instanceof javax.swing.JToolBar) {
            ((javax.swing.JToolBar) jToolBar1).addSeparator();
        }
        jToolBar1.add(invoiceButton);
        //the list is filled again (search, filters): the selection is gone; prices and stock formatted
        listtable.addPropertyChangeListener("model", e -> {
            formatNumbers();
            updatePreview();
        });
        formatNumbers();

        listtable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updatePreview();
            }
        });
        //Enter: a new invoice with the selected products (instead of moving to the next row)
        javax.swing.KeyStroke enter = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0);
        listtable.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, "yabs.newInvoice");
        listtable.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(enter, "yabs.newInvoice");
        listtable.getActionMap().put("yabs.newInvoice", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                newInvoice();
            }
        });
        updatePreview();
    }

    /**
     * Purchase price, sales price and stock (the last three columns): local
     * number format, right aligned
     */
    private void formatNumbers() {
        javax.swing.table.DefaultTableCellRenderer r = new javax.swing.table.DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                if (value instanceof Number) {
                    setText(mpv5.utils.numberformat.FormatNumber.formatDezimal(new java.math.BigDecimal(value.toString())));
                } else {
                    super.setValue(value);
                }
            }
        };
        r.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        int n = listtable.getColumnModel().getColumnCount();
        for (int i = Math.max(0, n - 3); i < n; i++) {
            listtable.getColumnModel().getColumn(i).setCellRenderer(r);
        }
    }

    /**
     * @return the ids of the selected products, in the order shown
     */
    private List<Integer> selectedIds() {
        List<Integer> ids = new ArrayList<Integer>();
        for (int r : listtable.getSelectedRows()) {
            try {
                ids.add(Integer.valueOf(listtable.getModel().getValueAt(listtable.convertRowIndexToModel(r), idColumn).toString()));
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
        }
        return ids;
    }

    private void updatePreview() {
        List<Integer> ids = selectedIds();
        invoiceButton.setText(ids.isEmpty() ? Messages.PRODUCTS_NEW_INVOICE.toString()
                : String.format(Messages.PRODUCTS_NEW_INVOICE_N.toString(), ids.size()));
        invoiceButton.setEnabled(!ids.isEmpty());
        if (ids.isEmpty()) {
            previewId = -1;
            previewImage.setIcon(null);
            previewImage.setText(Messages.PRODUCTS_SELECT.toString());
            previewTitle.setText(" ");
            previewFacts.setText(" ");
            previewText.setText("");
            return;
        }
        final int id = ids.get(0);
        Product p;
        try {
            p = (Product) DatabaseObject.getObject(Context.getProduct(), id);
        } catch (NodataFoundException ex) {
            return;
        }
        previewId = id;
        previewTitle.setText(p.__getCname());
        java.math.BigDecimal net = p.__getExternalnetvalue();
        java.math.BigDecimal gross = net.multiply(mpv5.db.objects.Tax.getCalculationValue(p.__getTaxids()));
        String unit = p.__getMeasure() == null || p.__getMeasure().isEmpty() ? "" : " / " + p.__getMeasure();
        StringBuilder facts = new StringBuilder("<html>");
        facts.append(p.__getCnumber()).append("<br>")
                .append(mpv5.utils.numberformat.FormatNumber.formatLokalCurrency(net)).append(unit).append(" ").append(Messages.PRODUCTS_NET)
                .append(" · ").append(mpv5.utils.numberformat.FormatNumber.formatLokalCurrency(gross)).append(" ").append(Messages.PRODUCTS_GROSS);
        if (p.__getInttype() == Product.TYPE_PRODUCT && p.__getStockvalue() != null) {
            facts.append("<br>").append(Messages.PRODUCTS_STOCK).append(": ").append(mpv5.utils.numberformat.FormatNumber.formatDezimal(p.__getStockvalue()));
        }
        if (ids.size() > 1) {
            facts.append("<br><b>").append(String.format(Messages.PRODUCTS_SELECTED_N.toString(), ids.size())).append("</b>");
        }
        previewFacts.setText(facts.append("</html>").toString());
        previewText.setText(p.__getDescription() == null ? "" : p.__getDescription());
        final String image = p.__getDefaultimage();
        if (image == null || image.isEmpty()) {
            previewImage.setIcon(null);
            previewImage.setText(Messages.PRODUCT_NO_IMAGE.toString());
            return;
        }
        javax.swing.ImageIcon cached = images.get(image);
        if (cached != null) {
            previewImage.setText(null);
            previewImage.setIcon(cached);
            return;
        }
        previewImage.setIcon(null);
        previewImage.setText("…");
        //the picture at most 250 x 200 (scaled); the area takes its height
        final int w = mpv5.ui.modern.ModernTheme.px(250), h = mpv5.ui.modern.ModernTheme.px(200);
        new Thread(() -> {
            javax.swing.ImageIcon icon = null;
            try {
                java.io.File f = mpv5.handler.ProductService.retrieveImage(image);
                java.awt.image.BufferedImage img = f == null ? null : javax.imageio.ImageIO.read(f);
                if (img != null) {
                    double sc = Math.min(1d, Math.min((double) w / img.getWidth(), (double) h / img.getHeight()));
                    icon = new javax.swing.ImageIcon(img.getScaledInstance(Math.max(1, (int) (img.getWidth() * sc)),
                            Math.max(1, (int) (img.getHeight() * sc)), java.awt.Image.SCALE_SMOOTH));
                }
            } catch (Exception ex) {
                Log.Debug(ProductPreview.this, ex.getMessage());
            }
            final javax.swing.ImageIcon loaded = icon;
            SwingUtilities.invokeLater(() -> {
                if (loaded != null) {
                    images.put(image, loaded);
                }
                if (previewId == id) {
                    previewImage.setText(loaded == null ? Messages.PRODUCT_NO_IMAGE.toString() : null);
                    previewImage.setIcon(loaded);
                }
            });
        }, "product-preview").start();
    }

    /**
     * A new invoice with the selected products as positions; the cursor in the
     * customer box (with "+ New customer ...")
     */
    private void newInvoice() {
        List<Integer> ids = selectedIds();
        if (ids.isEmpty()) {
            return;
        }
        List<DatabaseObject> products = new ArrayList<DatabaseObject>();
        for (Integer id : ids) {
            try {
                products.add(DatabaseObject.getObject(Context.getProduct(), id));
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex.getMessage());
            }
        }
        javax.swing.JComponent view = mpv5.YabsViewProxy.instance().newItemView(mpv5.db.objects.Item.TYPE_INVOICE);
        mpv5.YabsViewProxy.instance().addTab(view, Messages.NEW_INVOICE.toString());
        if (view instanceof DataPanel) {
            ((DataPanel) view).paste(products.toArray(new DatabaseObject[0]));
        }
        if (view instanceof ModernItemPanel) {
            ((ModernItemPanel) view).focusCustomer();
        }
    }
}
