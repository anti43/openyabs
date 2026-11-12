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
package mpv5.utils.importers;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.ProductGroup;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Tax;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.handler.ItemService;
import mpv5.handler.ProductService;
import mpv5.logging.Log;

/**
 * Imports a Fakturama 2 database (HSQLDB: Database.script, .properties,
 * .lobs) into YaBS: tax rates, product categories, contacts with address and
 * bank account, products with block prices, documents with their positions
 * and expense vouchers.
 * <p>
 * The database is read from a temporary copy (the Fakturama files are never
 * changed). Columns are read by name, so different Fakturama 2 versions work.
 * Records already imported (same number and name) are not imported twice.
 */
public class Fakturama2Importer {

    /**
     * Progress of the import
     */
    public interface Progress {

        /**
         * @param message what is imported now
         * @param percent 0..100
         */
        void step(String message, int percent);
    }

    private boolean importContacts = true;
    private boolean importProducts = true;
    private boolean importDocuments = true;
    private boolean importVouchers = true;
    private boolean importTax = true;

    public void setImportContacts(boolean v) {
        this.importContacts = v;
    }

    public void setImportProducts(boolean v) {
        this.importProducts = v;
    }

    public void setImportDocuments(boolean v) {
        this.importDocuments = v;
    }

    public void setImportVouchers(boolean v) {
        this.importVouchers = v;
    }

    /**
     * The YaBS VAT rate for products, positions and expenses whose VAT rate
     * cannot be determined from Fakturama (no or an unknown VAT entry)
     *
     * @param taxids ids of a YaBS tax, null: 0 %
     */
    public void setDefaultTax(Integer taxids) {
        this.defaultTaxId = taxids;
        try {
            this.defaultPercent = taxids == null ? BigDecimal.ZERO : Tax.getTaxValue(taxids);
        } catch (Exception e) {
            this.defaultPercent = BigDecimal.ZERO;
        }
    }

    private Integer defaultTaxId;
    private BigDecimal defaultPercent = BigDecimal.ZERO;

    /**
     * @return the VAT rate (percent) of a Fakturama VAT id; the default rate
     * if it cannot be determined
     */
    private BigDecimal percentOf(long vatId) {
        BigDecimal p = taxPercent.get(vatId);
        if (p == null) {
            result.defaultTaxUsed++;
            return defaultPercent;
        }
        return p;
    }

    /**
     * The rate of a VAT entry: its value (0.19 or 19), else a number in its
     * name ("MwSt. 19 %"); null if neither
     */
    static BigDecimal rateOf(BigDecimal value, String name) {
        if (value != null) {
            return (value.abs().compareTo(BigDecimal.ONE) < 0 ? value.movePointRight(2) : value).stripTrailingZeros();
        }
        if (name != null) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*%?").matcher(name);
            if (m.find()) {
                return new BigDecimal(m.group(1).replace(',', '.')).stripTrailingZeros();
            }
        }
        return null;
    }

    public void setImportTax(boolean v) {
        this.importTax = v;
    }

    /**
     * The result of an import
     */
    public static class ImportResult {

        public int contactsImported = 0;
        public int productsImported = 0;
        public int groupsImported = 0;
        public int itemsImported = 0;
        public int subItemsImported = 0;
        public int vouchersImported = 0;
        public int taxesImported = 0;
        public int alreadyThere = 0;
        public int imagesImported = 0;
        public int suppliers = 0;
        public int manufacturers = 0;
        public int productsLinked = 0;
        public int inventoryProducts = 0;
        public int deletedSkipped = 0;
        public int taxesExisting = 0;
        public int defaultTaxUsed = 0;
        public List<String> warnings = new ArrayList<>();

        /**
         * @return the counts, translated
         */
        public String getSummary() {
            StringBuilder b = new StringBuilder();
            b.append(Messages.FKT_RESULT_TAXES).append(": ").append(taxesImported);
            if (taxesExisting > 0) {
                b.append("  \u00b7  ").append(Messages.FKT_RESULT_TAXES_EXISTING).append(": ").append(taxesExisting);
            }
            b.append('\n');
            if (defaultTaxUsed > 0) {
                b.append(Messages.FKT_RESULT_DEFAULT_TAX).append(": ").append(defaultTaxUsed).append('\n');
            }
            b.append(Messages.FKT_RESULT_GROUPS).append(": ").append(groupsImported).append('\n');
            b.append(Messages.FKT_RESULT_CONTACTS).append(": ").append(contactsImported).append('\n');
            b.append(Messages.FKT_RESULT_PRODUCTS).append(": ").append(productsImported).append('\n');
            b.append(Messages.FKT_RESULT_DOCUMENTS).append(": ").append(itemsImported)
                    .append(" (").append(subItemsImported).append(' ').append(Messages.FKT_RESULT_POSITIONS).append(")\n");
            b.append(Messages.FKT_RESULT_VOUCHERS).append(": ").append(vouchersImported).append('\n');
            b.append(Messages.FKT_RESULT_IMAGES).append(": ").append(imagesImported).append('\n');
            b.append(Messages.FKT_RESULT_SUPPLIERS).append(": ").append(suppliers)
                    .append("  \u00b7  ").append(Messages.FKT_RESULT_MANUFACTURERS).append(": ").append(manufacturers).append('\n');
            if (productsLinked > 0) {
                b.append(Messages.FKT_RESULT_LINKED).append(": ").append(productsLinked).append('\n');
            }
            b.append(Messages.FKT_RESULT_INVENTORY).append(": ").append(inventoryProducts).append('\n');
            if (alreadyThere > 0) {
                b.append(Messages.FKT_RESULT_EXISTING).append(": ").append(alreadyThere).append('\n');
            }
            if (deletedSkipped > 0) {
                b.append(Messages.FKT_RESULT_DELETED).append(": ").append(deletedSkipped).append('\n');
            }
            return b.toString();
        }
    }

    //Fakturama id -> YaBS id
    private final Map<Long, Integer> taxIds = new HashMap<>();
    private final Map<Long, BigDecimal> taxPercent = new HashMap<>();
    private final Map<Long, Integer> groupIds = new HashMap<>();
    private final Map<Long, Integer> contactIds = new HashMap<>();
    private final Map<Long, Integer> productIds = new HashMap<>();
    private Connection con;
    private ImportResult result;
    //progress: all records to read, records done
    private Progress progress = (m, i) -> {
    };
    private int total = 1;
    private int done;
    private long lastTick;
    private long started;
    //records already in YaBS: context -> "number|name" -> ids
    private final Map<String, Map<String, Integer>> known = new HashMap<>();
    //products with a stock in Fakturama: managed in the inventory after the import
    private final Map<Integer, BigDecimal> stock = new LinkedHashMap<>();
    //contact categories of Fakturama: id -> name
    private final Map<Long, String> contactCategories = new HashMap<>();

    /**
     * Imports from the Fakturama database the file belongs to
     *
     * @param databaseFile Database.script (or any file of the database, or its
     * folder)
     * @param progress may be null
     * @return the result
     * @throws Exception if the database cannot be read
     */
    public ImportResult importFrom(File databaseFile, Progress progress) throws Exception {
        //one status message per record would flood the message list of the main window
        mpv5.YabsViewProxy.instance().pauseMessages();
        try {
            return importUnpaused(databaseFile, progress);
        } finally {
            mpv5.YabsViewProxy.instance().resumeMessages();
        }
    }

    private ImportResult importUnpaused(File databaseFile, Progress progress) throws Exception {
        Progress p = progress != null ? progress : (m, i) -> {
        };
        this.progress = p;
        result = new ImportResult();
        started = System.currentTimeMillis();
        File tmp = copyDatabase(databaseFile);
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
            p.step(Messages.FKT_STEP_OPEN.toString(), 2);
            con = DriverManager.getConnection("jdbc:hsqldb:file:" + tmp.getPath()
                    + ";readonly=true;ifexists=true;hsqldb.lock_file=false", "SA", "");
            if (!hasTable("FKT_CONTACT") && !hasTable("FKT_PRODUCT")) {
                throw new IllegalArgumentException(Messages.FKT_NOT_FAKTURAMA.toString());
            }
            countAll(p);
            if (importTax || importProducts || importDocuments || importVouchers) {
                p.step(Messages.FKT_RESULT_TAXES.toString(), percent());
                importTaxes();
            }
            if (importProducts) {
                p.step(Messages.FKT_RESULT_GROUPS.toString(), percent());
                importGroups();
            }
            if (importContacts || importDocuments) {
                p.step(Messages.FKT_RESULT_CONTACTS.toString(), percent());
                importContacts(importContacts);
            }
            if (importProducts || importDocuments) {
                p.step(Messages.FKT_RESULT_PRODUCTS.toString(), percent());
                importProducts(importProducts);
            }
            if (importDocuments) {
                p.step(Messages.FKT_RESULT_DOCUMENTS.toString(), percent());
                importDocuments(p);
            }
            if (importVouchers) {
                p.step(Messages.FKT_RESULT_VOUCHERS.toString(), percent());
                importVouchers();
            }
            //only now: imported old invoices must not reduce the stock
            p.step(Messages.FKT_STEP_INVENTORY.toString(), 99);
            applyInventory();
            p.step(Messages.FKT_STEP_DONE.toString(), 100);
        } finally {
            if (con != null) {
                try (Statement s = con.createStatement()) {
                    s.execute("SHUTDOWN");
                } catch (Exception e) {
                    //closed
                }
                try {
                    con.close();
                } catch (Exception e) {
                    //closed
                }
            }
            deleteDir(tmp.getParentFile());
        }
        return result;
    }

    // <editor-fold defaultstate="collapsed" desc="Progress">
    /**
     * Counts the records to import; tells the counts as a line (percent -1)
     */
    private void countAll(Progress p) {
        p.step(Messages.FKT_STEP_COUNT.toString(), 1);
        Map<String, String> tables = new LinkedHashMap<>();
        tables.put("FKT_VAT", Messages.FKT_RESULT_TAXES.toString());
        tables.put("FKT_CATEGORY", Messages.FKT_RESULT_GROUPS.toString());
        tables.put("FKT_CONTACT", Messages.FKT_RESULT_CONTACTS.toString());
        tables.put("FKT_PRODUCT", Messages.FKT_RESULT_PRODUCTS.toString());
        tables.put("FKT_DOCUMENT", Messages.FKT_RESULT_DOCUMENTS.toString());
        tables.put("FKT_DOCUMENTITEM", Messages.FKT_RESULT_POSITIONS.toString());
        tables.put("FKT_VOUCHERS", Messages.FKT_RESULT_VOUCHERS.toString());
        StringBuilder b = new StringBuilder(Messages.FKT_FOUND.toString()).append(' ');
        int sum = 0;
        for (Map.Entry<String, String> t : tables.entrySet()) {
            int n = count(t.getKey());
            if (!t.getKey().equals("FKT_DOCUMENTITEM")) {
                sum += n;
            }
            if (n > 0) {
                b.append(String.format(Locale.getDefault(), "%,d", n)).append(' ').append(t.getValue()).append(", ");
            }
        }
        total = Math.max(1, sum);
        p.step(b.toString().replaceAll(", $", ""), -1);
    }

    private int count(String table) {
        if (!hasTable(table)) {
            return 0;
        }
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM PUBLIC.\"" + table + "\"")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private int percent() {
        return Math.min(98, 2 + 96 * done / total);
    }

    /**
     * One record done: tells the progress (at most every 200 ms)
     *
     * @param label what is imported
     * @param i the number of the record
     * @param n the records of this kind
     */
    private void tick(String label, int i, int n) {
        done++;
        long now = System.currentTimeMillis();
        if (now - lastTick < 200 && i < n) {
            return;
        }
        lastTick = now;
        String eta = "";
        long spent = now - started;
        if (done > 20 && spent > 5000) {
            long left = spent * (total - done) / done / 1000;
            eta = "  \u00b7  " + String.format(Messages.FKT_ETA.toString(),
                    left >= 120 ? (left / 60) + " min" : Math.max(1, left) + " s");
        }
        progress.step(String.format(Locale.getDefault(), "%s %,d / %,d%s", label, i, n, eta), percent());
    }

    /**
     * @return the id of a record with this number and name in YaBS, else null
     * (all records of the context are read once)
     */
    private Integer existing(Context c, String cnumber, String cname) {
        if (cnumber == null || cname == null) {
            return null;
        }
        Map<String, Integer> m = known.get(c.toString());
        if (m == null) {
            m = new HashMap<>();
            try {
                Object[][] rows = new mpv5.db.common.DatabaseSearch(c, 0).getValuesFor("ids, cnumber, cname");
                for (Object[] r : rows == null ? new Object[0][] : rows) {
                    m.put(r[1] + "|" + r[2], Integer.valueOf(String.valueOf(r[0])));
                }
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
            known.put(c.toString(), m);
        }
        return m.get(cnumber + "|" + cname);
    }

    private void remember(Context c, String cnumber, String cname, int ids) {
        Map<String, Integer> m = known.get(c.toString());
        if (m != null && cnumber != null && cname != null) {
            m.put(cnumber + "|" + cname, ids);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Database access">
    /**
     * Copies the database files (name.script, .properties, .lobs, .data,
     * .backup) to a temporary folder
     *
     * @return the copy (without extension, as HSQLDB expects it)
     */
    static File copyDatabase(File f) throws Exception {
        File dir = f.isDirectory() ? f : f.getParentFile();
        String base;
        if (f.isDirectory()) {
            File[] scripts = dir.listFiles((d, n) -> n.toLowerCase(Locale.ROOT).endsWith(".script"));
            if (scripts == null || scripts.length == 0) {
                throw new IllegalArgumentException(Messages.FKT_NO_SCRIPT.toString());
            }
            base = scripts[0].getName().substring(0, scripts[0].getName().length() - ".script".length());
        } else {
            String n = f.getName();
            base = n.contains(".") ? n.substring(0, n.lastIndexOf('.')) : n;
        }
        if (!new File(dir, base + ".script").isFile()) {
            throw new IllegalArgumentException(Messages.FKT_NO_SCRIPT.toString());
        }
        File tmp = Files.createTempDirectory("yabs-fakturama").toFile();
        for (String ext : new String[]{".script", ".properties", ".lobs", ".data", ".backup"}) {
            File src = new File(dir, base + ext);
            if (src.isFile()) {
                Files.copy(src.toPath(), new File(tmp, "db" + ext).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        //a database left open by Fakturama: open the copy anyway
        File props = new File(tmp, "db.properties");
        if (props.isFile()) {
            String t = new String(Files.readAllBytes(props.toPath()), java.nio.charset.StandardCharsets.ISO_8859_1);
            Files.write(props.toPath(), t.replace("modified=yes", "modified=no").getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
        }
        return new File(tmp, "db");
    }

    private static void deleteDir(File d) {
        File[] fs = d == null ? null : d.listFiles();
        if (fs != null) {
            for (File f : fs) {
                if (f.isDirectory()) {
                    deleteDir(f);
                } else if (!f.delete()) {
                    f.deleteOnExit();
                }
            }
        }
        if (d != null && !d.delete()) {
            d.deleteOnExit();
        }
    }

    private boolean hasTable(String name) {
        try (ResultSet rs = con.getMetaData().getTables(null, "PUBLIC", name, null)) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * A row of a Fakturama table, columns by name (upper case)
     */
    static final class Row {

        final Map<String, Object> v = new LinkedHashMap<>();

        String s(String col) {
            Object o = v.get(col);
            if (o == null) {
                return null;
            }
            String t = String.valueOf(o).trim();
            return t.isEmpty() ? null : t;
        }

        String s(String col, String def) {
            String t = s(col);
            return t == null ? def : t;
        }

        long l(String col) {
            Object o = v.get(col);
            if (o instanceof Number) {
                return ((Number) o).longValue();
            }
            try {
                return o == null ? 0 : Long.parseLong(String.valueOf(o).trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        BigDecimal d(String col) {
            Object o = v.get(col);
            if (o == null) {
                return null;
            }
            try {
                //doubles of Fakturama: 7.170000076293945 -> 7.17000
                return new BigDecimal(String.valueOf(o).trim()).setScale(5, RoundingMode.HALF_UP).stripTrailingZeros();
            } catch (NumberFormatException e) {
                return null;
            }
        }

        boolean b(String col) {
            Object o = v.get(col);
            return o instanceof Boolean ? (Boolean) o : o != null && ("1".equals(String.valueOf(o)) || "true".equalsIgnoreCase(String.valueOf(o)));
        }

        Date date(String col) {
            Object o = v.get(col);
            return o instanceof Date ? new Date(((Date) o).getTime()) : null;
        }

        boolean deleted() {
            return b("DELETED");
        }
    }

    /**
     * @return all rows of a table (empty if the table does not exist)
     */
    List<Row> rows(String table) {
        List<Row> l = new ArrayList<>();
        if (!hasTable(table)) {
            return l;
        }
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM PUBLIC.\"" + table + "\" ORDER BY 1")) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                Row r = new Row();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    Object o;
                    try {
                        int type = md.getColumnType(i);
                        if (type == java.sql.Types.BLOB || type == java.sql.Types.LONGVARBINARY || type == java.sql.Types.VARBINARY) {
                            //pictures of products
                            o = rs.getBytes(i);
                        } else if (type == java.sql.Types.CLOB || type == java.sql.Types.NCLOB) {
                            o = rs.getString(i);
                            //Fakturama appends "null" to some texts (text + null)
                            if (o != null && ((String) o).length() > 4 && ((String) o).endsWith("null")) {
                                o = ((String) o).substring(0, ((String) o).length() - 4);
                            }
                        } else if (type == java.sql.Types.DATE || type == java.sql.Types.TIMESTAMP) {
                            o = rs.getTimestamp(i);
                        } else {
                            o = rs.getObject(i);
                        }
                    } catch (Exception e) {
                        //a text in a missing .lobs file
                        o = null;
                    }
                    r.v.put(md.getColumnName(i).toUpperCase(Locale.ROOT), o);
                }
                l.add(r);
            }
        } catch (Exception e) {
            Log.Debug(this, e);
            result.warnings.add(table + ": " + e.getMessage());
        }
        return l;
    }

    private Map<Long, Row> byId(String table) {
        Map<Long, Row> m = new HashMap<>();
        for (Row r : rows(table)) {
            m.put(r.l("ID"), r);
        }
        return m;
    }

    /**
     * @return the text, at most max characters; "" for null (YaBS columns are
     * not nullable)
     */
    private static String cut(String s, int max) {
        return s == null ? "" : s.length() <= max ? s : s.substring(0, max);
    }

    /**
     * New records get a YaBS number and today as date when saved: keep the
     * number and the date of Fakturama
     */
    private static void keepNumberAndDate(DatabaseObject o, String number, Date date) {
        boolean changed = false;
        if (number != null && !number.isEmpty()) {
            if (o instanceof Contact && !number.equals(((Contact) o).__getCNumber())) {
                ((Contact) o).setCNumber(number);
                changed = true;
            } else if (o instanceof Item && !number.equals(((Item) o).__getCnumber())) {
                ((Item) o).setCnumber(number);
                o.setCname(number);
                changed = true;
            } else if (o instanceof Expense && !number.equals(((Expense) o).__getCnumber())) {
                ((Expense) o).setCnumber(number);
                changed = true;
            }
        }
        if (date != null) {
            o.setDateadded(date);
            changed = true;
        }
        if (changed) {
            o.save(true);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Taxes and groups">
    private void importTaxes() {
        List<Tax> known = new ArrayList<>();
        try {
            known.addAll(DatabaseObject.getObjects(Context.getTaxes()));
        } catch (Exception e) {
            //none yet
        }
        List<Row> vats = rows("FKT_VAT");
        int k = 0;
        for (Row r : vats) {
            tick(Messages.FKT_RESULT_TAXES.toString(), ++k, vats.size());
            //Fakturama: 0.19 for 19 %; without a value: the rate in the name ("MwSt. 19 %")
            BigDecimal percent = rateOf(r.d("TAXVALUE"), r.s("NAME"));
            if (percent == null) {
                //unknown: products and positions with it get the default rate
                result.warnings.add("VAT " + r.s("NAME", String.valueOf(r.l("ID"))) + ": " + Messages.FKT_TAX_UNKNOWN);
                continue;
            }
            taxPercent.put(r.l("ID"), percent);
            //a rate YaBS has already (same value, else same name) is used, not imported again
            Tax match = null;
            for (Tax t : known) {
                if (t.__getTaxvalue() != null && t.__getTaxvalue().compareTo(percent) == 0) {
                    match = t;
                    break;
                }
            }
            for (Tax t : known) {
                if (match == null && r.s("NAME") != null && r.s("NAME").trim().equalsIgnoreCase(String.valueOf(t.__getCname()).trim())) {
                    match = t;
                    taxPercent.put(r.l("ID"), t.__getTaxvalue());
                }
            }
            if (match != null) {
                taxIds.put(r.l("ID"), match.__getIDS());
                result.taxesExisting++;
                continue;
            }
            if (!importTax || r.deleted()) {
                continue;
            }
            try {
                Tax t = (Tax) DatabaseObject.getObject(Context.getTaxes());
                t.setCname(cut(r.s("NAME", percent.toPlainString() + " %"), 250));
                t.setTaxvalue(percent);
                t.setGroupsids(1);
                if (t.save(true)) {
                    known.add(t);
                    taxIds.put(r.l("ID"), t.__getIDS());
                    result.taxesImported++;
                }
            } catch (Exception e) {
                result.warnings.add("VAT " + r.s("NAME") + ": " + e.getMessage());
            }
        }
    }

    private void importGroups() {
        List<Row> cats = new ArrayList<>();
        List<Row> all = rows("FKT_CATEGORY");
        int k = 0;
        for (Row r : all) {
            tick(Messages.FKT_RESULT_GROUPS.toString(), ++k, all.size());
            String type = r.s("CATEGORYNAME", r.s("DTYPE", ""));
            if (type.toLowerCase(Locale.ROOT).contains("product") && !r.deleted()) {
                cats.add(r);
            }
        }
        //parents first
        Set<Long> done = new HashSet<>();
        for (int pass = 0; pass < 10 && done.size() < cats.size(); pass++) {
            for (Row r : cats) {
                long id = r.l("ID");
                long parent = r.l("FK_PARENT_CATEGORY");
                if (done.contains(id) || (parent > 0 && !groupIds.containsKey(parent) && containsId(cats, parent))) {
                    continue;
                }
                done.add(id);
                int parentIds = parent > 0 && groupIds.containsKey(parent) ? groupIds.get(parent) : 1;
                String name = cut(r.s("NAME", "Fakturama " + id), 250);
                try {
                    ProductGroup g = findGroup(name, parentIds);
                    if (g == null) {
                        g = (ProductGroup) DatabaseObject.getObject(Context.getProductGroup());
                        g.setCname(name);
                        g.setProductgroupsids(parentIds);
                        g.setGroupsids(1);
                        g.setDescription("Fakturama");
                        g.save(true);
                        result.groupsImported++;
                    }
                    groupIds.put(id, g.__getIDS());
                } catch (Exception e) {
                    result.warnings.add(name + ": " + e.getMessage());
                }
            }
        }
    }

    private static boolean containsId(List<Row> rows, long id) {
        for (Row r : rows) {
            if (r.l("ID") == id) {
                return true;
            }
        }
        return false;
    }

    private static ProductGroup findGroup(String name, int parent) {
        try {
            QueryCriteria q = new QueryCriteria("cname", name);
            q.addAndCondition("productgroupsids", parent);
            List<DatabaseObject> l = DatabaseObject.getObjects(Context.getProductGroup(), q);
            return l.isEmpty() ? null : (ProductGroup) l.get(0);
        } catch (Exception e) {
            return null;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Contacts">
    private void importContacts(boolean create) {
        Map<Long, Row> banks = byId("FKT_BANKACCOUNT");
        Map<Long, Row> addressById = byId("FKT_ADDRESS");
        //addresses of a contact (newer Fakturama: CONTACT_ADDRESSES)
        Map<Long, Row> addressOf = new HashMap<>();
        for (Row a : addressById.values()) {
            long owner = a.l("CONTACT_ADDRESSES");
            if (owner > 0 && (!addressOf.containsKey(owner) || addressOf.get(owner).deleted())) {
                addressOf.put(owner, a);
            }
        }
        //contact categories: "Lieferanten", "Hersteller" ... tell the kind of contact
        for (Row c : rows("FKT_CATEGORY")) {
            if (c.s("CATEGORYNAME", "").toLowerCase(Locale.ROOT).contains("contact")) {
                contactCategories.put(c.l("ID"), c.s("NAME", ""));
            }
        }
        List<Row> contacts = rows("FKT_CONTACT");
        int k = 0;
        for (Row r : contacts) {
            tick(Messages.FKT_RESULT_CONTACTS.toString(), ++k, contacts.size());
            long id = r.l("ID");
            if (r.deleted()) {
                result.deletedSkipped += create ? 1 : 0;
                continue;
            }
            String category = contactCategories.getOrDefault(r.l("FK_CATEGORY"), "").toLowerCase(Locale.ROOT);
            boolean manufacturer = category.matches(".*(hersteller|manufactur|produzent|fabrikant|marke|brand).*");
            boolean supplier = r.s("DTYPE", "").toLowerCase(Locale.ROOT).startsWith("creditor")
                    || category.matches(".*(lieferant|supplier|kreditor|creditor|vendor|zulieferer).*");
            String number = supplier ? r.s("SUPPLIERNUMBER", r.s("CUSTOMERNUMBER")) : r.s("CUSTOMERNUMBER", r.s("SUPPLIERNUMBER"));
            String company = r.s("COMPANY");
            String name = r.s("NAME");
            String cname = cut(name != null ? name : company != null ? company : "Fakturama " + id, 250);
            Integer there = existing(Context.getContact(), number, cname);
            if (there != null) {
                contactIds.put(id, there);
                result.alreadyThere += create ? 1 : 0;
                continue;
            }
            if (!create) {
                continue;
            }
            try {
                Contact c = (Contact) DatabaseObject.getObject(Context.getContact());
                c.setCname(cname);
                if (number != null) {
                    c.setCNumber(cut(number, 250));
                }
                c.setCompany(cut(company, 250));
                c.setisCompany(name == null && company != null);
                c.setPrename(cut(r.s("FIRSTNAME"), 250));
                c.setTitle(cut(r.s("TITLE"), 250));
                c.setisMale(r.l("GENDER") == 1);
                c.setisSupplier(supplier);
                c.setisManufacturer(manufacturer);
                c.setisCustomer(!supplier && !manufacturer);
                c.settaxnumber(cut(r.s("VATNUMBER"), 250));
                c.setWebsite(cut(r.s("WEBSITE"), 250));
                c.setNotes(cut(r.s("NOTE"), 100000));
                c.setGroupsids(1);
                //address: own table (newer versions) or columns of the contact (older ones)
                Row a = addressOf.get(id);
                if (a == null && r.l("FK_ADDRESS") > 0) {
                    a = addressById.get(r.l("FK_ADDRESS"));
                }
                if (a == null) {
                    a = r;
                }
                c.setStreet(cut(a.s("STREET"), 250));
                c.setZip(cut(a.s("ZIP"), 50));
                c.setCity(cut(a.s("CITY"), 250));
                c.setCountry(cut(a.s("COUNTRYCODE", a.s("COUNTRY")), 250));
                c.setMainphone(cut(a.s("PHONE", r.s("PHONE")), 250));
                c.setMobilephone(cut(a.s("MOBILE", r.s("MOBILE")), 250));
                c.setFax(cut(a.s("FAX", r.s("FAX")), 250));
                c.setMailaddress(cut(a.s("EMAIL", r.s("EMAIL")), 250));
                Row bank = banks.get(r.l("FK_BANKACCOUNT"));
                if (bank != null) {
                    c.setBankaccount(cut(bank.s("IBAN"), 250));
                    c.setBankid(cut(bank.s("BIC"), 250));
                    c.setBankname(cut(bank.s("BANKNAME"), 250));
                }
                if (c.save(true)) {
                    keepNumberAndDate(c, number, r.date("DATEADDED"));
                    contactIds.put(id, c.__getIDS());
                    remember(Context.getContact(), number, cname, c.__getIDS());
                    result.contactsImported++;
                    result.suppliers += supplier ? 1 : 0;
                    result.manufacturers += manufacturer ? 1 : 0;
                }
            } catch (Exception e) {
                Log.Debug(this, e);
                result.warnings.add(cname + ": " + e.getMessage());
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Products">
    private void importProducts(boolean create) {
        List<Row> products = rows("FKT_PRODUCT");
        boolean linkColumns = false;
        int k = 0;
        for (Row r : products) {
            tick(Messages.FKT_RESULT_PRODUCTS.toString(), ++k, products.size());
            long id = r.l("ID");
            if (r.deleted()) {
                result.deletedSkipped += create ? 1 : 0;
                continue;
            }
            String number = r.s("ITEMNUMBER");
            String name = cut(r.s("NAME", number != null ? number : "Fakturama " + id), 250);
            Integer there = existing(Context.getProduct(), number, name);
            if (there != null) {
                productIds.put(id, there);
                result.alreadyThere += create ? 1 : 0;
                continue;
            }
            if (!create) {
                continue;
            }
            try {
                Product p = (Product) DatabaseObject.getObject(Context.getProduct());
                p.setCname(name);
                if (number != null) {
                    p.setCnumber(cut(number, 250));
                }
                String description = r.s("DESCRIPTION");
                String note = r.s("NOTE");
                p.setDescription(cut(note == null ? description : description == null ? note : description + "\n\n" + note, 100000));
                p.setInttype(Constants.TYPE_PRODUCT);
                p.setExternalnetvalue(nz(r.d("PRICE1")));
                p.setInternalnetvalue(nz(r.d("COSTPRICE")));
                if (r.d("QUANTITY") != null) {
                    p.setStockvalue(r.d("QUANTITY"));
                }
                p.setMeasure(cut(r.s("QUANTITYUNIT"), 250));
                p.setEan(cut(r.s("GTIN"), 250));
                p.setReference(cut(r.s("SUPPLIERITEMNUMBER"), 250));
                Integer tax = taxIds.get(r.l("FK_VAT"));
                if (tax == null) {
                    //VAT not determined: the default rate of the import
                    result.defaultTaxUsed++;
                    tax = defaultTaxId != null ? defaultTaxId : 1;
                }
                p.setTaxids(tax);
                Integer group = groupIds.get(r.l("FK_CATEGORY"));
                p.setProductgroupsids(group != null ? group : 1);
                p.setGroupsids(1);
                p.setUrl("");
                //supplier and manufacturer, if this Fakturama version has them per product
                Integer supplier = link(r, new String[]{"FK_SUPPLIER", "SUPPLIER_ID", "FK_CREDITOR", "FK_VENDOR"},
                        new String[]{"SUPPLIER", "SUPPLIERNAME", "VENDOR"}, false);
                Integer manufacturer = link(r, new String[]{"FK_MANUFACTURER", "MANUFACTURER_ID"},
                        new String[]{"MANUFACTURER", "MANUFACTURERNAME", "BRAND"}, true);
                linkColumns |= hasAny(r, "FK_SUPPLIER", "SUPPLIER_ID", "FK_CREDITOR", "FK_VENDOR", "SUPPLIER", "SUPPLIERNAME",
                        "VENDOR", "FK_MANUFACTURER", "MANUFACTURER_ID", "MANUFACTURER", "MANUFACTURERNAME", "BRAND");
                if (supplier != null) {
                    p.setSuppliersids(supplier);
                }
                if (manufacturer != null) {
                    p.setManufacturersids(manufacturer);
                }
                if (p.save(true)) {
                    keepNumberAndDate(p, null, r.date("DATEADDED"));
                    productIds.put(id, p.__getIDS());
                    remember(Context.getProduct(), number, name, p.__getIDS());
                    result.productsImported++;
                    result.productsLinked += supplier != null || manufacturer != null ? 1 : 0;
                    blockPrices(r, p);
                    if (r.d("QUANTITY") != null) {
                        stock.put(p.__getIDS(), r.d("QUANTITY"));
                    }
                    Object pic = r.v.get("PICTURE");
                    if (pic instanceof byte[] && ((byte[]) pic).length > 0) {
                        picture(p, (byte[]) pic, number != null ? number : name);
                    }
                }
            } catch (Exception e) {
                Log.Debug(this, e);
                result.warnings.add(name + ": " + e.getMessage());
            }
        }
        if (create && !linkColumns && result.suppliers > 0) {
            result.warnings.add(Messages.FKT_NO_PRODUCT_SUPPLIER.toString());
        }
    }

    private static boolean hasAny(Row r, String... cols) {
        for (String c : cols) {
            if (r.v.containsKey(c)) {
                return true;
            }
        }
        return false;
    }

    private final Map<String, Integer> linkedByName = new HashMap<>();

    /**
     * @return the YaBS contact of a supplier / manufacturer column of a
     * product (id of a Fakturama contact, or a name), else null
     */
    private Integer link(Row r, String[] idColumns, String[] nameColumns, boolean manufacturer) {
        for (String c : idColumns) {
            if (r.l(c) > 0 && contactIds.containsKey(r.l(c))) {
                return contactIds.get(r.l(c));
            }
        }
        for (String c : nameColumns) {
            String name = r.v.get(c) instanceof Number ? null : r.s(c);
            if (name == null) {
                continue;
            }
            String key = (manufacturer ? "m|" : "s|") + name.toLowerCase(Locale.ROOT);
            if (linkedByName.containsKey(key)) {
                return linkedByName.get(key);
            }
            try {
                Integer id = null;
                try {
                    List<DatabaseObject> l = DatabaseObject.getObjects(Context.getContact(), new QueryCriteria("cname", cut(name, 250)));
                    id = l.isEmpty() ? null : l.get(0).__getIDS();
                } catch (NodataFoundException e) {
                    //a new one
                }
                if (id == null) {
                    Contact k = (Contact) DatabaseObject.getObject(Context.getContact());
                    k.setCname(cut(name, 250));
                    k.setCompany(cut(name, 250));
                    k.setisCompany(true);
                    k.setisManufacturer(manufacturer);
                    k.setisSupplier(!manufacturer);
                    k.setisCustomer(false);
                    k.setGroupsids(1);
                    if (k.save(true)) {
                        id = k.__getIDS();
                        result.contactsImported++;
                        if (manufacturer) {
                            result.manufacturers++;
                        } else {
                            result.suppliers++;
                        }
                    }
                }
                linkedByName.put(key, id);
                return id;
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
        }
        return null;
    }

    /**
     * Fakturama keeps the picture of a product in the database: it becomes a
     * file of the product and its product image
     */
    private void picture(Product p, byte[] data, String name) {
        try {
            String ext = data.length > 3 && (data[0] & 0xff) == 0x89 && data[1] == 'P' ? "png"
                    : data.length > 2 && (data[0] & 0xff) == 0xFF && (data[1] & 0xff) == 0xD8 ? "jpg"
                    : data.length > 3 && data[0] == 'G' && data[1] == 'I' && data[2] == 'F' ? "gif"
                    : data.length > 2 && data[0] == 'B' && data[1] == 'M' ? "bmp" : "png";
            File dir = Files.createTempDirectory("yabs-fkt-img").toFile();
            File f = new File(dir, name.replaceAll("[^A-Za-z0-9_.-]", "_") + "." + ext);
            Files.write(f.toPath(), data);
            final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            final String[] stored = new String[1];
            mpv5.db.common.QueryHandler.instanceOf().clone(Context.getFiles()).insertFile(f, p,
                    mpv5.db.common.QueryCriteria.getSaveStringFor(Messages.FKT_PICTURE.toString()), o -> {
                        if (o instanceof mpv5.db.objects.FileToProduct) {
                            stored[0] = ((mpv5.db.objects.FileToProduct) o).__getFilename();
                        }
                        latch.countDown();
                    });
            latch.await(120, java.util.concurrent.TimeUnit.SECONDS);
            if (stored[0] != null) {
                p.setDefaultimage(stored[0]);
                p.save(true);
                result.imagesImported++;
            } else {
                result.warnings.add(name + ": " + Messages.FKT_PICTURE_NOT_IMPORTED);
            }
            if (!f.delete()) {
                f.deleteOnExit();
            }
            dir.delete();
        } catch (Exception e) {
            Log.Debug(this, e);
            result.warnings.add(name + ": " + Messages.FKT_PICTURE_NOT_IMPORTED + " " + e.getMessage());
        }
    }

    /**
     * Products with a stock in Fakturama are managed in the inventory; set
     * after the documents, so that old invoices do not reduce the stock
     */
    private void applyInventory() {
        for (Map.Entry<Integer, BigDecimal> e : stock.entrySet()) {
            try {
                Product p = (Product) DatabaseObject.getObject(Context.getProduct(), e.getKey());
                p.setIntinventorytype(1);
                p.setStockvalue(e.getValue());
                if (p.save(true)) {
                    result.inventoryProducts++;
                }
            } catch (Exception ex) {
                Log.Debug(this, ex.getMessage());
            }
        }
    }

    /**
     * Fakturama block prices (from BLOCKn pieces PRICEn) as staggered prices
     */
    private void blockPrices(Row r, Product p) {
        List<ProductService.Tier> tiers = new ArrayList<>();
        BigDecimal first = nz(r.d("PRICE1"));
        for (int i = 2; i <= 5; i++) {
            BigDecimal price = r.d("PRICE" + i);
            BigDecimal block = r.d("BLOCK" + i);
            if (price != null && price.signum() > 0 && block != null && block.signum() > 0 && price.compareTo(first) != 0) {
                tiers.add(new ProductService.Tier(block, price));
            }
        }
        if (!tiers.isEmpty()) {
            ProductService.saveTiers(p, tiers);
        }
    }

    private static BigDecimal nz(BigDecimal b) {
        return b == null ? BigDecimal.ZERO : b;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Documents">
    private void importDocuments(Progress progress) {
        Map<Long, List<Row>> positions = new HashMap<>();
        for (Row i : rows("FKT_DOCUMENTITEM")) {
            if (!i.deleted()) {
                positions.computeIfAbsent(i.l("FK_DOCUMENT"), k -> new ArrayList<>()).add(i);
            }
        }
        Map<Long, Row> receivers = new HashMap<>();
        for (Row r : rows("FKT_DOCUMENTRECEIVER")) {
            receivers.putIfAbsent(r.l("FK_DOCUMENT"), r);
        }
        Map<Long, Row> shippings = byId("FKT_SHIPPING");
        List<Row> docs = rows("FKT_DOCUMENT");
        int n = 0;
        for (Row d : docs) {
            n++;
            tick(Messages.FKT_RESULT_DOCUMENTS.toString(), n, docs.size());
            if (d.deleted()) {
                result.deletedSkipped++;
                continue;
            }
            String dtype = d.s("DTYPE", "").toLowerCase(Locale.ROOT);
            int type;
            Context ctx;
            if (dtype.startsWith("invoice")) {
                type = Constants.TYPE_INVOICE;
                ctx = Context.getInvoice();
            } else if (dtype.startsWith("offer")) {
                type = Constants.TYPE_OFFER;
                ctx = Context.getOffer();
            } else if (dtype.startsWith("order")) {
                type = Constants.TYPE_ORDER;
                ctx = Context.getOrder();
            } else if (dtype.startsWith("confirmation")) {
                type = Constants.TYPE_ORDER_CONFIRMATION;
                ctx = Context.getConfirmation();
            } else if (dtype.startsWith("delivery")) {
                type = Constants.TYPE_DELIVERY_NOTE;
                ctx = Context.getDelivery();
            } else if (dtype.startsWith("credit")) {
                type = Constants.TYPE_CREDIT;
                ctx = Context.getCredit();
            } else {
                //dunning letters, letters, pro forma invoices
                result.warnings.add(d.s("NAME", "") + ": " + Messages.FKT_TYPE_NOT_IMPORTED + " " + d.s("DTYPE", "?"));
                continue;
            }
            String number = cut(d.s("NAME"), 250);
            Integer existingItem = existing(ctx, number, number);
            if (existingItem != null) {
                result.alreadyThere++;
                continue;
            }
            Integer contact = contactIds.get(d.l("FK_CONTACT"));
            Row receiver = receivers.get(d.l("ID"));
            if (contact == null && receiver != null) {
                contact = contactIds.get(receiver.l("ORIGINCONTACTID"));
            }
            if (contact == null) {
                contact = receiverAsContact(receiver, d);
            }
            if (contact == null) {
                result.warnings.add(number + ": " + Messages.FKT_NO_CONTACT);
                continue;
            }
            try {
                Item item = (Item) DatabaseObject.getObject(ctx);
                item.setInttype(type);
                item.setCname(number);
                item.setCnumber(number);
                item.setContactsids(contact);
                item.setGroupsids(1);
                item.setAccountsids(1);
                Date date = d.date("DOCUMENTDATE") != null ? d.date("DOCUMENTDATE") : d.date("DATEADDED");
                if (date == null) {
                    date = new Date();
                }
                item.setDateadded(date);
                Calendar due = Calendar.getInstance();
                due.setTime(date);
                due.add(Calendar.DATE, (int) d.l("DUEDAYS"));
                item.setDatetodo(due.getTime());
                boolean paid = d.b("PAID");
                item.setIntstatus(paid ? Item.STATUS_PAID : Item.STATUS_FINISHED);
                item.setDateend(paid && d.date("PAYDATE") != null ? d.date("PAYDATE") : due.getTime());
                String ref = d.s("CUSTOMERREF");
                String message = d.s("MESSAGE");
                item.setDescription(cut(ref == null ? message : message == null ? ref : ref + "\n" + message, 100000));
                if (!item.save(true)) {
                    result.warnings.add(number + ": not saved");
                    continue;
                }
                keepNumberAndDate(item, number, date);
                remember(ctx, number, number, item.__getIDS());
                int pos = 1;
                List<Row> items = positions.getOrDefault(d.l("ID"), new ArrayList<>());
                items.sort((a, b) -> Long.compare(a.l("POSNR"), b.l("POSNR")));
                for (Row i : items) {
                    addPosition(item, i, pos++);
                }
                //shipping costs (Fakturama: gross value)
                BigDecimal shipping = d.d("SHIPPINGVALUE");
                if (shipping != null && shipping.signum() != 0) {
                    Row sh = shippings.get(d.l("FK_SHIPPING"));
                    BigDecimal rate = sh != null ? percentOf(sh.l("FK_VAT")) : defaultPercent;
                    BigDecimal net = shipping.divide(BigDecimal.ONE.add(rate.movePointLeft(2)), 5, RoundingMode.HALF_UP);
                    SubItem s = position(item, sh != null ? sh.s("NAME", Messages.FKT_SHIPPING.toString()) : Messages.FKT_SHIPPING.toString(),
                            BigDecimal.ONE, net, rate, null, pos++);
                    s.save();
                    result.subItemsImported++;
                }
                ItemService.updateTotals(item);
                BigDecimal rebate = d.d("ITEMSREBATE");
                if (rebate != null && rebate.signum() != 0) {
                    result.warnings.add(number + ": " + Messages.FKT_REBATE_NOT_IMPORTED + " " + rebate.abs().movePointRight(2).stripTrailingZeros().toPlainString() + " %");
                }
                result.itemsImported++;
            } catch (Exception e) {
                Log.Debug(this, e);
                result.warnings.add(number + ": " + e.getMessage());
            }
        }
    }

    private void addPosition(Item item, Row i, int pos) {
        String name = i.s("NAME", i.s("ITEMNUMBER", ""));
        String description = i.s("DESCRIPTION");
        BigDecimal rate = i.b("NOVAT") ? BigDecimal.ZERO : percentOf(i.l("FK_VAT"));
        if (i.b("NOVAT")) {
            rate = BigDecimal.ZERO;
        }
        SubItem s = position(item, name, i.d("QUANTITY") == null ? BigDecimal.ONE : i.d("QUANTITY"),
                nz(i.d("PRICE")), rate, i.s("QUANTITYUNIT"), pos);
        if (description != null && !description.equals(name)) {
            s.setDescription(cut(name + "\n" + description, 10000));
        }
        //Fakturama: -0.05 for 5 % discount
        BigDecimal rebate = i.d("ITEMREBATE");
        if (rebate != null && rebate.signum() != 0) {
            s.setDiscount((rebate.abs().compareTo(BigDecimal.ONE) <= 0 ? rebate.abs().movePointRight(2) : rebate.abs()).stripTrailingZeros());
        }
        Integer product = productIds.get(i.l("FK_PRODUCT"));
        if (product != null) {
            s.setOriginalproductsids(product);
        }
        s.save();
        result.subItemsImported++;
    }

    private static SubItem position(Item item, String name, BigDecimal quantity, BigDecimal net, BigDecimal rate, String measure, int pos) {
        SubItem s = new SubItem();
        s.setItemsids(item.__getIDS());
        s.setCname(cut(name, 250));
        s.setDescription(cut(name, 10000));
        s.setQuantityvalue(quantity);
        s.setCountvalue(BigDecimal.valueOf(pos));
        s.setExternalvalue(net);
        s.setInternalvalue(net);
        s.setTaxpercentvalue(rate);
        s.setMeasure(measure == null ? "" : cut(measure, 250));
        s.setOrdernr(pos);
        s.setDateadded(item.__getDateadded());
        s.setGroupsids(1);
        return s;
    }

    /**
     * A document without a known contact: the receiver of the document
     * becomes a contact
     */
    private Integer receiverAsContact(Row r, Row doc) {
        String name = r != null ? r.s("NAME", r.s("COMPANY")) : doc.s("ADDRESSFIRSTLINE");
        if (name == null) {
            return null;
        }
        try {
            Contact c = (Contact) DatabaseObject.getObject(Context.getContact());
            c.setCname(cut(name, 250));
            c.setisCustomer(true);
            c.setGroupsids(1);
            if (r != null) {
                c.setPrename(cut(r.s("FIRSTNAME"), 250));
                c.setCompany(cut(r.s("COMPANY"), 250));
                c.setStreet(cut(r.s("STREET"), 250));
                c.setZip(cut(r.s("ZIP"), 50));
                c.setCity(cut(r.s("CITY"), 250));
                c.setCountry(cut(r.s("COUNTRYCODE"), 250));
                c.setMailaddress(cut(r.s("EMAIL"), 250));
                c.setMainphone(cut(r.s("PHONE"), 250));
                c.setisMale(r.l("GENDER") == 1);
            }
            if (c.save(true)) {
                result.contactsImported++;
                if (r != null && r.l("ORIGINCONTACTID") > 0) {
                    contactIds.put(r.l("ORIGINCONTACTID"), c.__getIDS());
                }
                return c.__getIDS();
            }
        } catch (Exception e) {
            Log.Debug(this, e);
        }
        return null;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Vouchers">
    private void importVouchers() {
        Map<Long, List<Row>> items = new HashMap<>();
        for (Row i : rows("FKT_VOUCHERITEMS")) {
            if (!i.deleted()) {
                items.computeIfAbsent(i.l("FK_VOUCHER"), k -> new ArrayList<>()).add(i);
            }
        }
        List<Row> vouchers = rows("FKT_VOUCHERS");
        int k = 0;
        for (Row v : vouchers) {
            tick(Messages.FKT_RESULT_VOUCHERS.toString(), ++k, vouchers.size());
            if (v.deleted()) {
                result.deletedSkipped++;
                continue;
            }
            String type = v.s("VOUCHERTYPE", "").toUpperCase(Locale.ROOT);
            if (type.startsWith("R") || type.contains("RECEIPT")) {
                //received money (receipts): not an expense
                continue;
            }
            String number = cut(v.s("DOCUMENTNUMBER", v.s("VOUCHERNUMBER")), 250);
            String name = cut(v.s("NAME", number != null ? number : "Fakturama"), 250);
            if (existing(Context.getExpense(), number, name) != null) {
                result.alreadyThere++;
                continue;
            }
            try {
                BigDecimal gross = nz(v.d("TOTALVALUE"));
                BigDecimal net = BigDecimal.ZERO;
                BigDecimal rate = BigDecimal.ZERO;
                StringBuilder text = new StringBuilder();
                for (Row i : items.getOrDefault(v.l("ID"), new ArrayList<>())) {
                    BigDecimal r = percentOf(i.l("FK_VAT"));
                    BigDecimal price = nz(i.d("PRICE"));
                    net = net.add(price);
                    rate = r;
                    if (i.s("NAME") != null) {
                        text.append(i.s("NAME")).append('\n');
                    }
                }
                if (net.signum() == 0 && gross.signum() != 0) {
                    net = gross.divide(BigDecimal.ONE.add(rate.movePointLeft(2)), 2, RoundingMode.HALF_UP);
                }
                if (gross.signum() == 0) {
                    gross = net.multiply(BigDecimal.ONE.add(rate.movePointLeft(2))).setScale(2, RoundingMode.HALF_UP);
                }
                Expense e = (Expense) DatabaseObject.getObject(Context.getExpense());
                e.setCname(name);
                if (number != null) {
                    e.setCnumber(number);
                }
                e.setDescription(cut(text.toString().trim(), 100000));
                e.setNetvalue(net);
                e.setBrutvalue(gross);
                e.setTaxpercentvalue(rate);
                e.setAccountsids(1);
                e.setGroupsids(1);
                Date date = v.date("VOUCHERDATE") != null ? v.date("VOUCHERDATE") : v.date("DATEADDED");
                if (date != null) {
                    e.setDateadded(date);
                    e.setDateend(date);
                }
                BigDecimal paid = v.d("PAIDVALUE");
                e.setIspaid(paid != null && paid.compareTo(gross) >= 0);
                if (e.save(true)) {
                    keepNumberAndDate(e, number, date);
                    result.vouchersImported++;
                }
            } catch (Exception e) {
                Log.Debug(this, e);
                result.warnings.add(name + ": " + e.getMessage());
            }
        }
    }
    // </editor-fold>
}
