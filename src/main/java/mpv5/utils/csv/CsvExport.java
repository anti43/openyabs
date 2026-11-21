/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.csv;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Group;
import mpv5.db.objects.Product;
import mpv5.db.objects.Tax;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.files.FileDirectoryHandler;

/**
 * Exports contacts and products to CSV files that can be imported back via
 * {@link CsvImport} without issues.
 */
public final class CsvExport {

    public static final String[] CONTACT_HEADERS_DE = {
        "Name", "Vorname", "Anrede", "Firma", "Abteilung", "Strasse", "PLZ", "Ort", "Land",
        "Telefon", "Telefon geschaeftlich", "Mobil", "Fax", "E-Mail", "Webseite",
        "Kundennummer", "USt-IdNr", "Bank", "BIC", "IBAN", "Notizen", "Angelegt", "Gruppe"
    };

    public static final String[] CONTACT_HEADERS_EN = {
        "Name", "First name", "Title", "Company", "Department", "Street", "Zip", "City", "Country",
        "Phone", "Work phone", "Mobile", "Fax", "Email", "Website",
        "Number", "Tax number", "Bank", "BIC", "IBAN", "Notes", "Date added", "Group"
    };

    public static final String[] PRODUCT_HEADERS_DE = {
        "Artikelname", "Artikelnummer", "Beschreibung", "Verkaufspreis", "Einkaufspreis",
        "MwSt", "Einheit", "EAN", "Referenz", "URL", "Bestand", "Mindestbestand",
        "Lieferant", "Hersteller", "Angelegt", "Gruppe"
    };

    public static final String[] PRODUCT_HEADERS_EN = {
        "Product name", "Product number", "Description", "Sale price", "Purchase price",
        "Tax rate", "Unit", "EAN", "Reference", "URL", "Stock", "Threshold",
        "Supplier", "Manufacturer", "Date added", "Group"
    };

    private CsvExport() {
    }

    /**
     * @return true if German locale or user language is active
     */
    public static boolean isGerman() {
        try {
            //the id of the language is a random text (it may contain "de"): the language pack decides
            if (mpv5.i18n.LanguageManager.isGerman()) {
                return true;
            }
        } catch (Exception e) {
            // ignore
        }
        Locale l = Locale.getDefault();
        return l != null && l.getLanguage().equalsIgnoreCase("de");
    }

    /**
     * Exports all records in the given context (Contact or Product) to a CSV file
     * and shows a file save dialog.
     *
     * @param context Context.getContact() or Context.getProduct()
     */
    public static void export(Context context) {
        if (!MPSecurityManager.check(context, MPSecurityManager.EXPORT)) {
            return;
        }
        try {
            ArrayList<DatabaseObject> objects = DatabaseObject.getObjects(context);
            if (objects == null || objects.isEmpty()) {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NO_DATA_FOUND.toString());
                return;
            }
            File file = createExportFile(context, objects);
            if (file != null) {
                DialogForFile d = mpv5.YabsViewProxy.instance().getFiledialog();
                d.resetChoosableFileFilters();
                d.addChoosableFileFilter(DialogForFile.CSV_FILES);
                d.setFileFilter(DialogForFile.CSV_FILES);
                d.setSelectedFile(new File(file.getName()));
                d.saveFile(file);
            }
        } catch (NodataFoundException ex) {
            mpv5.YabsViewProxy.instance().addMessage(Messages.NO_DATA_FOUND.toString());
        } catch (Exception ex) {
            Log.Debug(CsvExport.class, ex);
            mpv5.ui.dialogs.Popup.error(ex);
        }
    }

    /**
     * Creates a temporary CSV file with exported records.
     *
     * @param context
     * @param objects
     * @return the created file
     * @throws IOException
     */
    public static File createExportFile(Context context, List<? extends DatabaseObject> objects) throws IOException {
        String csv = toCsv(context, objects);
        String prefix = context.getDbIdentity() != null ? context.getDbIdentity() : "export";
        File file = FileDirectoryHandler.getTempFile(prefix + "_export", "csv");
        Files.write(file.toPath(), csv.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    /**
     * Serializes database objects of context to CSV string using current language.
     *
     * @param context
     * @param objects
     * @return CSV content
     */
    public static String toCsv(Context context, List<? extends DatabaseObject> objects) {
        return toCsv(context, objects, isGerman());
    }

    /**
     * Serializes database objects of context to CSV string.
     *
     * @param context
     * @param objects
     * @param german whether to use German headers and formatting
     * @return CSV content
     */
    public static String toCsv(Context context, List<? extends DatabaseObject> objects, boolean german) {
        if (context.equals(Context.getContact())) {
            return toContactsCsv(objects, german);
        } else if (context.equals(Context.getProduct())) {
            return toProductsCsv(objects, german);
        } else {
            throw new IllegalArgumentException("Unsupported context for CSV export: " + context);
        }
    }

    /**
     * Serializes contact objects to CSV string using current language.
     */
    public static String toContactsCsv(List<? extends DatabaseObject> contacts) {
        return toContactsCsv(contacts, isGerman());
    }

    /**
     * Serializes contact objects to CSV string.
     *
     * @param contacts
     * @param german whether to use German headers and formatting
     * @return CSV content
     */
    public static String toContactsCsv(List<? extends DatabaseObject> contacts, boolean german) {
        String[] headers = german ? CONTACT_HEADERS_DE : CONTACT_HEADERS_EN;
        StringBuilder sb = new StringBuilder();
        appendRow(sb, headers);

        if (contacts != null) {
            for (DatabaseObject obj : contacts) {
                if (obj instanceof Contact) {
                    Contact c = (Contact) obj;
                    String[] row = new String[]{
                        nullToEmpty(c.__getCname()),
                        nullToEmpty(c.__getPrename()),
                        nullToEmpty(c.__getTitle()),
                        nullToEmpty(c.__getCompany()),
                        nullToEmpty(c.__getDepartment()),
                        nullToEmpty(c.__getStreet()),
                        nullToEmpty(c.__getZip()),
                        nullToEmpty(c.__getCity()),
                        nullToEmpty(c.__getCountry()),
                        nullToEmpty(c.__getMainphone()),
                        nullToEmpty(c.__getWorkphone()),
                        nullToEmpty(c.__getMobilephone()),
                        nullToEmpty(c.__getFax()),
                        nullToEmpty(c.__getMailaddress()),
                        nullToEmpty(c.__getWebsite()),
                        nullToEmpty(c.__getCNumber()),
                        nullToEmpty(c.__getTaxnumber()),
                        nullToEmpty(c.__getBankname()),
                        nullToEmpty(c.__getBankid()),
                        nullToEmpty(c.__getBankaccount()),
                        nullToEmpty(c.__getNotes()),
                        formatDate(c.__getDateadded(), german),
                        resolveGroupName(c.__getGroupsids())
                    };
                    appendRow(sb, row);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Serializes product objects to CSV string using current language.
     */
    public static String toProductsCsv(List<? extends DatabaseObject> products) {
        return toProductsCsv(products, isGerman());
    }

    /**
     * Serializes product objects to CSV string.
     *
     * @param products
     * @param german whether to use German headers and formatting
     * @return CSV content
     */
    public static String toProductsCsv(List<? extends DatabaseObject> products, boolean german) {
        String[] headers = german ? PRODUCT_HEADERS_DE : PRODUCT_HEADERS_EN;
        StringBuilder sb = new StringBuilder();
        appendRow(sb, headers);

        if (products != null) {
            for (DatabaseObject obj : products) {
                if (obj instanceof Product) {
                    Product p = (Product) obj;
                    String[] row = new String[]{
                        nullToEmpty(p.__getCname()),
                        nullToEmpty(p.__getCnumber()),
                        nullToEmpty(p.__getDescription()),
                        formatNumber(p.__getExternalnetvalue(), german),
                        formatNumber(p.__getInternalnetvalue(), german),
                        resolveTaxValue(p.__getTaxids()),
                        nullToEmpty(p.__getMeasure()),
                        nullToEmpty(p.__getEan()),
                        nullToEmpty(p.__getReference()),
                        nullToEmpty(p.__getUrl()),
                        formatNumber(p.__getStockvalue(), german),
                        formatNumber(p.__getThresholdvalue(), german),
                        resolveContactName(p.__getSuppliersids()),
                        resolveContactName(p.__getManufacturersids()),
                        formatDate(p.__getDateadded(), german),
                        resolveGroupName(p.__getGroupsids(), p.__getProductgroupsids())
                    };
                    appendRow(sb, row);
                }
            }
        }
        return sb.toString();
    }

    private static void appendRow(StringBuilder sb, String[] values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append(escape(values[i]));
        }
        sb.append("\r\n");
    }

    /**
     * RFC 4180 CSV value escaping with semicolon delimiter support.
     */
    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(";") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    private static String formatDate(Date date, boolean german) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(german ? "dd.MM.yyyy" : "yyyy-MM-dd");
        return sdf.format(date);
    }

    private static String formatNumber(BigDecimal value, boolean german) {
        if (value == null) {
            return "";
        }
        BigDecimal stripped = value.stripTrailingZeros();
        String str = stripped.scale() < 0 ? stripped.setScale(0).toPlainString() : stripped.toPlainString();
        return german ? str.replace('.', ',') : str;
    }

    private static boolean hasDatabaseConnection() {
        try {
            return DatabaseConnection.getConnector() != null && DatabaseConnection.getConnector().getConnection() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private static String resolveTaxValue(int taxId) {
        if (taxId <= 0 || !hasDatabaseConnection()) {
            return "";
        }
        try {
            Tax t = (Tax) DatabaseObject.getObject(Context.getTaxes(), taxId);
            if (t != null && t.__getTaxvalue() != null) {
                return t.__getTaxvalue().stripTrailingZeros().toPlainString();
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    private static String resolveContactName(int contactId) {
        if (contactId <= 0 || !hasDatabaseConnection()) {
            return "";
        }
        try {
            Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), contactId);
            if (c != null) {
                if (c.__getCname() != null && !c.__getCname().trim().isEmpty()) {
                    return c.__getCname().trim();
                }
                if (c.__getCompany() != null && !c.__getCompany().trim().isEmpty()) {
                    return c.__getCompany().trim();
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    private static String resolveGroupName(int groupId) {
        if (groupId <= 0 || !hasDatabaseConnection()) {
            return "";
        }
        try {
            Group g = (Group) DatabaseObject.getObject(Context.getGroup(), groupId);
            if (g != null && g.__getCname() != null && !g.__getCname().trim().isEmpty()) {
                return g.__getCname().trim();
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    private static String resolveGroupName(int groupId, int productGroupId) {
        String name = resolveGroupName(groupId);
        if (!name.isEmpty()) {
            return name;
        }
        if (productGroupId > 0 && hasDatabaseConnection()) {
            try {
                DatabaseObject pg = DatabaseObject.getObject(Context.getProductGroup(), productGroupId);
                if (pg != null && pg.__getCname() != null && !pg.__getCname().trim().isEmpty()) {
                    return pg.__getCname().trim();
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return "";
    }
}
