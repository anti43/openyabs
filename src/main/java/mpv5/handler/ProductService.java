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
package mpv5.handler;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.ProductGroup;
import mpv5.db.objects.ProductPrice;
import mpv5.db.objects.ProductsToSuppliers;
import mpv5.db.objects.Tax;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.panels.DataPanel;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.models.MPTableModel;

/**
 * The non-visual logic of a product view: prices, suppliers, staggered
 * prices, files and the documents the product was used in.
 * <p>
 * Contains no Swing code, so every product view (classic or modern) can use
 * it and it can be tested without a GUI.
 */
public final class ProductService {

    private ProductService() {
    }

    // <editor-fold defaultstate="collapsed" desc="Prices">
    /**
     * @param cost purchase price (net)
     * @param markupPercent e.g. 25 for +25%
     * @return the sale price (net): cost + markup
     */
    public static BigDecimal salePrice(BigDecimal cost, BigDecimal markupPercent) {
        BigDecimal c = cost == null ? BigDecimal.ZERO : cost;
        BigDecimal m = markupPercent == null ? BigDecimal.ZERO : markupPercent;
        return c.multiply(BigDecimal.ONE.add(m.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)));
    }

    /**
     * @param cost purchase price (net)
     * @param sale sale price (net)
     * @return the markup in percent, null if there is no cost
     */
    public static BigDecimal markup(BigDecimal cost, BigDecimal sale) {
        if (cost == null || sale == null || cost.signum() == 0) {
            return null;
        }
        return sale.subtract(cost).multiply(BigDecimal.valueOf(100)).divide(cost, 2, RoundingMode.HALF_UP);
    }

    /**
     * @param net
     * @param taxid
     * @return the gross price with the tax rate of the given tax
     */
    public static BigDecimal gross(BigDecimal net, int taxid) {
        return gross(net, Tax.getCalculationValue(taxid));
    }

    /**
     * @param net
     * @param calculationValue the factor of the tax, e.g. 1.19
     * @return net * factor
     */
    public static BigDecimal gross(BigDecimal net, BigDecimal calculationValue) {
        BigDecimal n = net == null ? BigDecimal.ZERO : net;
        return calculationValue == null ? n : n.multiply(calculationValue);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Staggered prices">
    /**
     * A staggered price: the price from a minimum quantity on
     */
    public static final class Tier {

        public final BigDecimal minimum;
        public final BigDecimal price;

        public Tier(BigDecimal minimum, BigDecimal price) {
            this.minimum = minimum == null ? BigDecimal.ZERO : minimum;
            this.price = price == null ? BigDecimal.ZERO : price;
        }
    }

    /**
     * @param product
     * @return the staggered prices of the product
     */
    public static List<Tier> loadTiers(Product product) {
        List<Tier> tiers = new ArrayList<Tier>();
        if (product == null || !product.isExisting()) {
            return tiers;
        }
        for (ProductPrice p : product.findProductPrices()) {
            tiers.add(new Tier(p.getMincountvalue(), p.getExternalnetvalue()));
        }
        return tiers;
    }

    /**
     * Stores the staggered prices of a saved product: existing ones are
     * updated, new ones created, missing ones deleted. Tiers with minimum 0
     * are ignored.
     *
     * @param product
     * @param tiers
     */
    public static void saveTiers(Product product, List<Tier> tiers) {
        if (product == null || !product.isExisting()) {
            return;
        }
        try {
            Map<BigDecimal, ProductPrice> existing = new HashMap<BigDecimal, ProductPrice>();
            for (ProductPrice p : product.findProductPrices()) {
                existing.put(p.getMincountvalue(), p);
            }
            for (Tier t : tiers) {
                if (t.minimum.intValue() == 0) {
                    continue;
                }
                ProductPrice p = existing.remove(t.minimum);
                if (p == null) {
                    //the map is keyed by BigDecimal: 5 and 5.0 are different keys
                    for (BigDecimal k : new ArrayList<BigDecimal>(existing.keySet())) {
                        if (k.compareTo(t.minimum) == 0) {
                            p = existing.remove(k);
                            break;
                        }
                    }
                }
                if (p != null) {
                    p.setExternalnetvalue(t.price);
                    p.save(true);
                } else {
                    ProductPrice n = new ProductPrice();
                    n.setProduct(product);
                    n.setMincountvalue(t.minimum);
                    n.setExternalnetvalue(t.price);
                    n.setInternalnetvalue(t.price);
                    n.save(true);
                }
            }
            for (ProductPrice p : existing.values()) {
                p.delete();
            }
        } catch (Exception e) {
            Log.Debug(ProductService.class, e);
        }
    }

    /**
     * Deletes all staggered prices of the product
     *
     * @param product
     */
    public static void deleteTiers(Product product) {
        if (product == null || !product.isExisting()) {
            return;
        }
        for (ProductPrice p : product.findProductPrices()) {
            p.delete();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Suppliers, groups">
    /**
     * @param id
     * @return the contact, null if not found or id &lt;= 0
     */
    public static Contact getContact(int id) {
        if (id <= 0) {
            return null;
        }
        try {
            return (Contact) DatabaseObject.getObject(Context.getContact(), id);
        } catch (NodataFoundException e) {
            return null;
        }
    }

    /**
     * @param product
     * @return the further suppliers (not the default supplier, which is
     * stored in the product)
     */
    public static List<Contact> loadFurtherSuppliers(Product product) {
        List<Contact> list = new ArrayList<Contact>();
        if (product == null || !product.isExisting()) {
            return list;
        }
        try {
            for (ProductsToSuppliers p : DatabaseObject.getReferencedObjects(product, Context.getProductsToSuppliers(), new ProductsToSuppliers())) {
                Contact c = getContact(p.__getContactsids());
                if (c != null) {
                    list.add(c);
                }
            }
        } catch (NodataFoundException e) {
            Log.Debug(ProductService.class, e.getMessage());
        }
        return list;
    }

    /**
     * Replaces the further suppliers of a saved product
     *
     * @param product
     * @param suppliers without the default supplier
     */
    public static void saveFurtherSuppliers(Product product, List<Contact> suppliers) {
        if (product == null || !product.isExisting()) {
            return;
        }
        QueryHandler.instanceOf().clone(Context.getProductsToSuppliers()).delete(new QueryCriteria("productsids", product.__getIDS()));
        for (Contact c : suppliers) {
            if (c != null && c.__getIDS() != product.__getSuppliersids()) {
                ProductsToSuppliers p = new ProductsToSuppliers();
                p.setProductsids(product.__getIDS());
                p.setContactsids(c.__getIDS());
                p.save(true);
            }
        }
    }

    /**
     * @return all tax rates: id, name
     */
    public static Object[][] loadTaxes() {
        try {
            Object[][] data = new DatabaseSearch(Context.getTaxes(), 200).getValuesFor("ids, cname", "cname", "", true);
            return data == null ? new Object[0][] : data;
        } catch (Exception e) {
            Log.Debug(ProductService.class, e);
            return new Object[0][];
        }
    }

    /**
     * @param id
     * @return the product group, null if not found
     */
    public static ProductGroup getProductGroup(int id) {
        try {
            return (ProductGroup) DatabaseObject.getObject(Context.getProductGroup(), id);
        } catch (NodataFoundException e) {
            Log.Debug(ProductService.class, e.getMessage());
            return null;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Files">
    /**
     * @param product
     * @return the files: column 0 the file name in the database, then
     * {@link Headers#FILE_REFERENCES}
     */
    public static MPTableModel loadFiles(Product product) {
        Context c = Context.getFilesToProducts();
        c.addReference(Context.getFiles().getDbIdentity(), "cname", "filename");
        Object[][] data = new DatabaseSearch(c).getValuesFor(Context.DETAILS_FILES_TO_PRODUCTS, "productsids", product.__getIDS());
        return new MPTableModel(data, Headers.FILE_REFERENCES.getValue());
    }

    /**
     * @param product must be saved
     * @param file
     * @param description
     * @param viewToBeNotified may be null
     * @return true if stored
     */
    public static boolean addFile(Product product, File file, String description, DataPanel viewToBeNotified) {
        if (product == null || !product.isExisting() || file == null) {
            return false;
        }
        QueryHandler q = viewToBeNotified == null
                ? QueryHandler.instanceOf().clone(Context.getFiles())
                : QueryHandler.instanceOf().clone(Context.getFiles(), viewToBeNotified);
        return q.insertFile(file, product, QueryCriteria.getSaveStringFor(description == null ? "" : description));
    }

    /**
     * @param filename the file name in the database
     */
    public static void deleteFile(String filename) {
        try {
            DatabaseObject.getObject(Context.getFilesToProducts(), "filename", filename).delete();
        } catch (Exception e) {
            Log.Debug(ProductService.class, e.getMessage());
        }
    }

    /**
     * @param filename the file name in the database
     * @param target where to copy it
     * @return the copy or null
     */
    public static File retrieveFile(String filename, File target) {
        return QueryHandler.instanceOf().clone(Context.getFiles()).retrieveFile(filename, target);
    }

    /**
     * @param filename the file name in the database, e.g. the default image
     * @return a temporary copy, null if there is none
     */
    public static File retrieveImage(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        return retrieveFile(filename, FileDirectoryHandler.getTempFile("image"));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Documents with the product">
    /**
     * Documents in which a product can be used
     */
    public enum Usage {

        OFFERS(Item.TYPE_OFFER), ORDERS(Item.TYPE_ORDER), INVOICES(Item.TYPE_INVOICE), CREDITS(Item.TYPE_CREDIT);
        private final int itemType;

        Usage(int itemType) {
            this.itemType = itemType;
        }

        /**
         * @return Item.TYPE_*
         */
        public int getItemType() {
            return itemType;
        }
    }

    /**
     * Columns of quantity and net value in {@link #loadUsage(Product, Usage)}
     */
    public static final int USAGE_QUANTITY = 4;
    public static final int USAGE_NET = 5;

    /**
     * The positions with this product in offers, orders, invoices or credit
     * notes, newest first. Columns: item id (hidden), number, date, contact,
     * quantity, net value of the position, details (the text of the
     * position).
     *
     * @param product
     * @param usage
     * @return the model, empty for unsaved products
     */
    public static MPTableModel loadUsage(Product product, Usage usage) {
        List<Object[]> rows = new ArrayList<Object[]>();
        if (product != null && product.isExisting()) {
            try {
                Object[][] data = QueryHandler.instanceOf().clone(Context.getSubItem()).freeSelectQuery(
                        "SELECT items.ids, items.cnumber, items.dateadded, contacts.cname, contacts.company,"
                        + " subitems.quantityvalue, subitems.measure, subitems.totalnetvalue, subitems.description, subitems.cname,"
                        + " subitems.externalvalue, subitems.discount"
                        + " FROM subitems INNER JOIN items ON subitems.itemsids = items.ids"
                        + " LEFT OUTER JOIN contacts ON items.contactsids = contacts.ids"
                        + " WHERE subitems.originalproductsids = " + product.__getIDS()
                        + " AND items.inttype = " + usage.getItemType()
                        + " AND items.invisible = 0 AND subitems.invisible = 0"
                        + " ORDER BY items.dateadded DESC, items.ids DESC",
                        mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
                for (Object[] d : data == null ? new Object[0][] : data) {
                    rows.add(usageRow(d));
                }
            } catch (Exception e) {
                Log.Debug(ProductService.class, e);
            }
        }
        MPTableModel m = new MPTableModel(rows.toArray(new Object[0][]), new String[]{
            "Internal ID", Headers.ITEM_DEFAULT.getValue()[1], Headers.ITEM_DEFAULT.getValue()[2],
            Messages.PRODUCT_USAGE_CONTACT.toString(), Messages.PRODUCT_USAGE_QUANTITY.toString(),
            Headers.ITEM_DEFAULT.getValue()[3], Messages.CONTACT_DETAILS.toString()});
        m.setTypes(new Class[]{Integer.class, Object.class, Date.class, String.class, BigDecimal.class, BigDecimal.class, String.class});
        return m;
    }

    /**
     * @param d ids, cnumber, date, contact name, company, quantity, measure,
     * net, description, name[, price, discount %]
     * @return the table row; with price and discount the net value is that
     * after discount (as on the document)
     */
    static Object[] usageRow(Object[] d) {
        String name = ContactService.plain(d[3]);
        String company = ContactService.plain(d[4]);
        String contact = company.isEmpty() || company.equals(name) ? name
                : name.isEmpty() ? company : company + " (" + name + ")";
        String text = ContactService.plain(d[8]);
        if (text.isEmpty()) {
            text = ContactService.plain(d[9]);
        }
        BigDecimal quantity = toDecimal(d[5]);
        BigDecimal net = toDecimal(d[7]);
        if (d.length > 11 && d[10] != null) {
            net = mpv5.utils.numberformat.InvoiceMath.lineNet(quantity, toDecimal(d[10]), toDecimal(d[11]));
        }
        return new Object[]{toInt(d[0]), d[1], d[2], contact, quantity, net, text};
    }

    private static Integer toInt(Object o) {
        try {
            return Integer.valueOf(String.valueOf(o).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal toDecimal(Object o) {
        try {
            //the decimal as stored/shown (0.1), never the binary double
            return new BigDecimal(String.valueOf(o).trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * @param model from {@link #loadUsage(Product, Usage)}
     * @return {number of documents, total quantity, total net value}
     */
    public static BigDecimal[] usageTotals(javax.swing.table.TableModel model) {
        java.util.Set<Object> documents = new java.util.HashSet<Object>();
        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal net = BigDecimal.ZERO;
        for (int i = 0; i < model.getRowCount(); i++) {
            documents.add(model.getValueAt(i, 0));
            quantity = quantity.add(toDecimal(model.getValueAt(i, USAGE_QUANTITY)));
            net = net.add(toDecimal(model.getValueAt(i, USAGE_NET)));
        }
        return new BigDecimal[]{BigDecimal.valueOf(documents.size()), quantity, net};
    }
    // </editor-fold>
}
