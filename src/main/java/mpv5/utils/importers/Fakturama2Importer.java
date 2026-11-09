package mpv5.utils.importers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.ProductGroup;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Tax;
import mpv5.db.objects.User;
import mpv5.logging.Log;

/**
 * Backend Parser & Entity Importer for Fakturama 2 into OpenYABS
 */
public class Fakturama2Importer {

    private boolean importContacts = true;
    private boolean importProducts = true;
    private boolean importDocuments = true;
    private boolean importVouchers = true;
    private boolean importTax = true;

    public void setImportContacts(boolean v) { this.importContacts = v; }
    public void setImportProducts(boolean v) { this.importProducts = v; }
    public void setImportDocuments(boolean v) { this.importDocuments = v; }
    public void setImportVouchers(boolean v) { this.importVouchers = v; }
    public void setImportTax(boolean v) { this.importTax = v; }

    public static class ImportResult {
        public int contactsImported = 0;
        public int productsImported = 0;
        public int itemsImported = 0;
        public int subItemsImported = 0;
        public int vouchersImported = 0;
        public int taxesImported = 0;
        public List<String> warnings = new ArrayList<>();

        public String getSummary() {
            return String.format(
                "Imported: %d Contacts, %d Products, %d Invoices/Items, %d Line Positions, %d Vouchers, %d Taxes.",
                contactsImported, productsImported, itemsImported, subItemsImported, vouchersImported, taxesImported
            );
        }
    }

    public ImportResult importFromScript(File scriptFile) throws Exception {
        ImportResult result = new ImportResult();
        Map<Integer, Integer> contactIdMap = new HashMap<>();
        Map<Integer, Integer> taxIdMap = new HashMap<>();
        Map<Integer, Integer> categoryIdMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(scriptFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("INSERT INTO")) continue;

                // Match table and values
                if (importTax && line.contains("FKT_VAT")) {
                    List<String> vals = extractValues(line);
                    if (vals.size() >= 4) {
                        try {
                            int fktId = Integer.parseInt(vals.get(0));
                            String name = vals.get(1);
                            double taxVal = Double.parseDouble(vals.get(2));
                            Tax t = new Tax();
                            t.setCname(name);
                            t.setTaxvalue(new BigDecimal(taxVal <= 1 ? taxVal * 100 : taxVal));
                            if (vals.size() > 3) t.setDescription(vals.get(3));
                            t.save();
                            taxIdMap.put(fktId, t.__getIDS());
                            result.taxesImported++;
                        } catch (Exception e) {
                            Log.Debug(this, "Failed importing Tax: " + e.getMessage());
                        }
                    }
                } else if (importProducts && line.contains("FKT_CATEGORY")) {
                    List<String> vals = extractValues(line);
                    if (vals.size() >= 2) {
                        try {
                            int fktId = Integer.parseInt(vals.get(0));
                            ProductGroup pg = new ProductGroup();
                            pg.setCname(vals.get(1));
                            pg.setDescription("Imported from Fakturama 2");
                            pg.save();
                            categoryIdMap.put(fktId, pg.__getIDS());
                        } catch (Exception e) {
                            Log.Debug(this, "Failed importing Category: " + e.getMessage());
                        }
                    }
                } else if (importContacts && line.contains("FKT_CONTACT")) {
                    List<String> vals = extractValues(line);
                    if (vals.size() >= 15) {
                        try {
                            int fktId = Integer.parseInt(vals.get(0));
                            Contact c = new Contact();
                            String surname = vals.get(1);
                            String prename = vals.get(2);
                            String company = vals.get(3);
                            String cnumber = vals.get(4);
                            boolean isSupplier = "1".equals(vals.get(5)) || "true".equalsIgnoreCase(vals.get(5));

                            c.setCNumber(cnumber != null && !cnumber.isEmpty() ? cnumber : "KD-" + fktId);
                            c.setCname(company != null && !company.isEmpty() ? company : (prename + " " + surname).trim());
                            c.setCompany(company);
                            c.setPrename(prename);
                            c.setisSupplier(isSupplier);
                            c.setisCustomer(!isSupplier);

                            if (vals.size() > 6) c.settaxnumber(vals.get(6));
                            if (vals.size() > 8) c.setNotes(vals.get(8));
                            if (vals.size() > 10) c.setTitle(vals.get(10));
                            if (vals.size() > 11) c.setStreet(vals.get(11));
                            if (vals.size() > 12) c.setZip(vals.get(12));
                            if (vals.size() > 13) c.setCity(vals.get(13));
                            if (vals.size() > 14) c.setCountry(vals.get(14));
                            if (vals.size() > 15) c.setMainphone(vals.get(15));
                            if (vals.size() > 16) c.setMobilephone(vals.get(16));
                            if (vals.size() > 17) c.setMailaddress(vals.get(17));
                            if (vals.size() > 18) c.setWebsite(vals.get(18));
                            if (vals.size() > 19) c.setBankaccount(vals.get(19));
                            if (vals.size() > 21) c.setBankname(vals.get(21));

                            c.saveImport();
                            contactIdMap.put(fktId, c.__getIDS());
                            result.contactsImported++;
                        } catch (Exception e) {
                            Log.Debug(this, "Failed importing Contact: " + e.getMessage());
                        }
                    }
                } else if (importProducts && line.contains("FKT_PRODUCT")) {
                    List<String> vals = extractValues(line);
                    if (vals.size() >= 6) {
                        try {
                            Product p = new Product();
                            p.setCNumber(vals.get(1));
                            p.setCname(vals.get(2));
                            p.setDescription(vals.get(3));
                            p.setNetprice(new BigDecimal(vals.get(4)));
                            if (vals.size() > 5) p.setCostprice(new BigDecimal(vals.get(5)));
                            if (vals.size() > 6) p.setStockquantity(new BigDecimal(vals.get(6)));
                            if (vals.size() > 7) p.setMeasureunit(vals.get(7));
                            if (vals.size() > 9) p.setEan(vals.get(9));

                            if (vals.size() > 10) {
                                int catId = Integer.parseInt(vals.get(10));
                                p.setProductgroupsids(categoryIdMap.getOrDefault(catId, 1));
                            }
                            if (vals.size() > 11) {
                                int vatId = Integer.parseInt(vals.get(11));
                                p.setTaxids(taxIdMap.getOrDefault(vatId, 1));
                            }
                            p.saveImport();
                            result.productsImported++;
                        } catch (Exception e) {
                            Log.Debug(this, "Failed importing Product: " + e.getMessage());
                        }
                    }
                }
            }
        }
        return result;
    }

    private List<String> extractValues(String insertLine) {
        List<String> values = new ArrayList<>();
        int start = insertLine.indexOf("VALUES");
        if (start < 0) return values;
        String valPart = insertLine.substring(start + 6).trim();
        if (valPart.startsWith("(") && valPart.endsWith(")")) {
            valPart = valPart.substring(1, valPart.length() - 1);
        }
        // Tokenizer for SQL values
        boolean inQuote = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valPart.length(); i++) {
            char c = valPart.charAt(i);
            if (c == '\'') {
                if (inQuote && i + 1 < valPart.length() && valPart.charAt(i + 1) == '\'') {
                    sb.append('\'');
                    i++;
                } else {
                    inQuote = !inQuote;
                }
            } else if (c == ',' && !inQuote) {
                values.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        values.add(sb.toString().trim());
        return values;
    }
}
