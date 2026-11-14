package mpv5.db.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Creates a YaBS 1.3.2 database (Apache Derby) as YaBS 1.3.2 did: the
 * statements of release-1.3.2 (src/test/resources/yabs132/yabs-1.3.2-derby.sql:
 * structure, initial data, then the updates, each update stopping at its first
 * failing statement like DatabaseConnection.runQueries of 1.3.2), then data in
 * every table, stored as 1.3.2 stored it: every ' as `, every \ as
 * "&lt;removedbackslash&gt;" (setting "escape backslashes", default on).
 * <p>
 * Plain JDBC, no YaBS classes: also runs with the Derby 10.8 driver of YaBS
 * 1.3.2. The texts as typed by the user are in {@link #TYPED}, the expected
 * row counts in {@link #ROWS}.
 */
public class Yabs132Fixture {

    public static final String SQL = "/yabs132/yabs-1.3.2-derby.sql";
    /**
     * As typed in YaBS 1.3.2 (stored escaped)
     */
    public static final String NAME = "O'Brien \\ Söhne & Töchter \"GmbH\"";
    public static final String STREET = "Hauptstraße 1 – Hinterhaus";
    public static final String NOTES;
    public static final String PATH = "C:\\Users\\hans\\Rechnungen";
    public static final String RTF = "{\\rtf1\\ansi{\\fonttbl\\f0\\fswiss Helvetica;}\\f0\\pard Kunde sagt: 'geht klar' – Grüße\\par}";
    public static final String MAIL = "<p>Hallo,</p><p><a href='https://example.org/rechnung'>Rechnung</a> – O'Brien</p>";
    public static final String POSITION = "Beratung \"Säule 1\" inkl. 'Fahrtkosten' \\ Pauschale";
    /**
     * A number format with ` (typed as `, kept: ' would be a quote character
     * of the pattern)
     */
    public static final String OFFER_FORMAT = "AN`{0,number,000}";
    public static final byte[] TEMPLATE_FILE;
    public static final byte[] ATTACHMENT;
    public static final byte[] LANGUAGE_FILE;
    public static final byte[] VALUE_BLOB;
    /**
     * table (upper case) - rows the fixture holds
     */
    public static final Map<String, Integer> ROWS = new LinkedHashMap<String, Integer>();

    static {
        StringBuilder b = new StringBuilder();
        while (b.length() < 7000) {
            b.append("Zeile ").append(b.length()).append(": Äpfel, Öl & Übermaß – 'zitiert' \\ Pfad\n");
        }
        NOTES = b.substring(0, 7000);
        Random r = new Random(132);
        byte[] template = new byte[200_000];
        r.nextBytes(template);
        if (System.getProperty("yabs132.template") != null) {
            //a real template, e.g. for printing in the application
            try {
                template = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(System.getProperty("yabs132.template")));
            } catch (java.io.IOException e) {
                throw new IllegalStateException(e);
            }
        }
        TEMPLATE_FILE = template;
        ATTACHMENT = new byte[256];
        for (int i = 0; i < 256; i++) {
            ATTACHMENT[i] = (byte) i;
        }
        LANGUAGE_FILE = "SOME_KEY=Some text\n".getBytes(StandardCharsets.UTF_8);
        //as ValueProperty stores it: java.beans.XMLEncoder, gzip
        java.io.ByteArrayOutputStream xml = new java.io.ByteArrayOutputStream();
        try (java.beans.XMLEncoder e = new java.beans.XMLEncoder(xml)) {
            e.writeObject("Wert 'mit' Zitat");
        }
        java.io.ByteArrayOutputStream gz = new java.io.ByteArrayOutputStream();
        try (java.util.zip.GZIPOutputStream z = new java.util.zip.GZIPOutputStream(gz)) {
            z.write(xml.toByteArray());
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
        VALUE_BLOB = gz.toByteArray();
    }

    /**
     * How YaBS 1.3.2 stored a typed text
     */
    public static String stored(String typed) {
        return typed == null ? null : typed.replace("'", "`").replace("\\", "<removedbackslash>");
    }

    private final Connection c;

    private Yabs132Fixture(Connection c) {
        this.c = c;
    }

    /**
     * Create the database and shut it down
     *
     * @param dir the database folder (must not exist)
     * @param user the Derby user (= schema of the tables), as in the old
     * settings
     * @param version the database version: 1.1984 (YaBS 1.3.2) or older
     * (the updates after it are not run)
     */
    public static void create(File dir, String user, double version) throws Exception {
        create(dir, user, version, null);
    }

    /**
     * @param password of the user, may be null
     */
    public static void create(File dir, String user, double version, String password) throws Exception {
        String url = "jdbc:derby:" + dir.getAbsolutePath();
        try (Connection c = DriverManager.getConnection(url + ";create=true", user, password == null ? "" : password)) {
            install(c, version);
            new Yabs132Fixture(c).fill(version);
        }
        try {
            DriverManager.getConnection(url + ";shutdown=true");
        } catch (SQLException e) {
            //08006: shut down
        }
    }

    private static void install(Connection c, double version) throws Exception {
        List<String> lines = new ArrayList<String>();
        InputStream in = Yabs132Fixture.class.getResourceAsStream(SQL);
        if (in == null) {
            in = new java.io.FileInputStream(System.getProperty("yabs132.sql"));
        }
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String l;
            while ((l = r.readLine()) != null) {
                lines.add(l);
            }
        }
        boolean skip = false;
        try (Statement s = c.createStatement()) {
            for (String l : lines) {
                if (l.startsWith("-- version ")) {
                    double v = Double.parseDouble(l.substring("-- version ".length()).trim());
                    skip = v > version;
                    if (!skip) {
                        s.execute("UPDATE globalsettings SET value = '" + v + "' WHERE cname = 'yabs_dbversion'");
                    }
                    continue;
                }
                if (l.startsWith("--") || l.isBlank() || skip) {
                    continue;
                }
                try {
                    s.execute(l);
                } catch (SQLException e) {
                    //1.3.2 stopped the update at its first failing statement and went on with the next one
                    skip = true;
                }
            }
            s.execute("UPDATE globalsettings SET value = '" + version + "' WHERE cname = 'yabs_dbversion'");
        }
    }

    private long insert(String table, Object... kv) throws SQLException {
        StringBuilder cols = new StringBuilder();
        StringBuilder marks = new StringBuilder();
        for (int i = 0; i < kv.length; i += 2) {
            cols.append(i == 0 ? "" : ", ").append(kv[i]);
            marks.append(i == 0 ? "?" : ", ?");
        }
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO " + table + " (" + cols + ") VALUES (" + marks + ")")) {
            for (int i = 1; i < kv.length; i += 2) {
                Object v = kv[i];
                if (v instanceof String) {
                    ps.setString(i / 2 + 1, stored((String) v));
                } else if (v instanceof Raw) {
                    ps.setString(i / 2 + 1, ((Raw) v).text);
                } else if (v instanceof byte[]) {
                    ps.setBytes(i / 2 + 1, (byte[]) v);
                } else if (v == null) {
                    ps.setNull(i / 2 + 1, java.sql.Types.VARCHAR);
                } else {
                    ps.setObject(i / 2 + 1, v);
                }
            }
            ps.executeUpdate();
        }
        ROWS.merge(table.toUpperCase(), 1, Integer::sum);
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("VALUES IDENTITY_VAL_LOCAL()")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    /**
     * A text stored exactly as given (not escaped)
     */
    private static class Raw {

        final String text;

        Raw(String text) {
            this.text = text;
        }
    }

    private void exec(String sql) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.execute(sql);
        }
    }

    private void count(String... tables) throws SQLException {
        for (String t : tables) {
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + t)) {
                rs.next();
                ROWS.put(t.toUpperCase(), rs.getInt(1));
            }
        }
    }

    private static Date d(String s) {
        return Date.valueOf(s);
    }

    private void fill(double version) throws Exception {
        ROWS.clear();
        Date added = d("2016-03-14");
        //groups, users with passwords (MD5) and rights
        long sales = insert("groups", "cname", "Vertrieb 'Süd'", "description", "Gruppe \\ Süd", "groupsids", 1, "dateadded", d("2015-01-02"), "hierarchypath", "All Groups/Vertrieb");
        long hans = insert("users", "cname", "hans", "groupsids", sales, "fullname", "Hans O'Brien", "password", "e10adc3949ba59abbe56e057f20f883e",
                "laf", "com.jtattoo.plaf.smart.SmartLookAndFeel", "locale", "de_DE", "mail", "hans@example.org", "language", "buildin_en",
                "inthighestright", 2, "isenabled", 1, "datelastlog", d("2018-09-01"), "dateadded", d("2015-01-03"), "intdefaultaccount", 1);
        insert("users", "cname", "aushilfe", "groupsids", 1, "fullname", "Aus Hilfe", "password", "202cb962ac59075b964b07152d234b70",
                "laf", "javax.swing.plaf.metal.MetalLookAndFeel", "locale", "de_DE", "mail", null, "language", "buildin_en",
                "inthighestright", 3, "isenabled", 0, "datelastlog", d("2017-05-05"), "dateadded", d("2016-01-03"));
        insert("userproperties", "cname", "defaultsavedir", "value", PATH, "usersids", hans, "dateadded", added, "groupsids", 1);
        insert("userproperties", "cname", "mpv5.ui.panels.ItemPanel$1", "value", "true", "usersids", 1, "dateadded", added, "groupsids", 1);
        insert("globalsettings", "cname", "mailsignature", "groupsids", 1, "value", "Grüße, O'Brien \\ Team");
        insert("globalsettings", "cname", "companytaxnumber", "groupsids", sales, "value", "12/345/67890");
        //company
        insert("comps", "cname", "Meine Firma", "name", "O'Brien", "firstname", "Hans", "street", STREET, "zipcode", "12345", "city", "Köln",
                "email", "info@example.org", "taxnumber", "12/345/67890", "dateadded", added, "ismale", 1, "isenabled", 1);
        //taxes, accounts, product groups, countries
        long tax16 = insert("tax", "cname", "MwSt 16% (2020)", "taxvalue", 16.0, "identifier", "16%", "groupsids", 1, "country", "DE", "dateadded", added);
        long account = insert("accounts", "cname", "Erlöse 19%", "description", "Konto 8400 'Erlöse'", "taxvalue", 19.0, "dateadded", added,
                "intparentaccount", 3, "groupsids", 1, "intaccounttype", 3, "intprofitfid", 0, "inttaxfid", 0, "inttaxuid", 0, "frame", "SKR03", "hierarchypath", "Income/8400");
        long pgroup = insert("productgroups", "cname", "Dienstleistungen", "description", "Beratung & Service", "groupsids", 1, "productgroupsids", 1, "dateadded", added, "hierarchypath", "All Products/Dienstleistungen");
        insert("countries", "cname", "Österreich", "iso", 40, "groupsids", 1);
        //contacts: customer with everything, one deleted (gap in the ids), supplier with NULLs, manufacturer in the trash
        long customer = insert("contacts", "cnumber", "K-00001", "taxnumber", "DE123456789", "title", "Dr.", "groupsids", sales, "country", "276",
                "prename", "Jürgen", "cname", NAME, "street", STREET, "zip", "50667", "city", "Köln", "mainphone", "+49 221 123'456",
                "mailaddress", "juergen@example.org", "company", "O'Brien & Söhne", "website", "https://example.org/a\\b", "notes", NOTES,
                "dateadded", d("2014-07-01"), "isactive", 1, "iscustomer", 1, "iscompany", 1, "ismale", 1, "isenabled", 1, "intaddedby", hans,
                "bankaccount", "DE89370400440532013000", "bankid", "COBADEFFXXX", "bankname", "Commerzbank", "bankcurrency", "EUR", "bankcountry", "DE", "payterm", 14);
        long gone = insert("contacts", "cnumber", "K-00002", "cname", "Gelöscht", "groupsids", 1, "dateadded", added, "iscustomer", 1);
        exec("DELETE FROM contacts WHERE ids = " + gone);
        ROWS.merge("CONTACTS", -1, Integer::sum);
        long supplier = insert("contacts", "cnumber", "L-00001", "cname", "Lieferant ohne Angaben", "groupsids", 1, "dateadded", added, "issupplier", 1,
                "prename", null, "street", null, "city", null, "notes", null);
        long maker = insert("contacts", "cnumber", "H-00001", "cname", "Hersteller", "company", "Hersteller AG", "groupsids", 1, "dateadded", added, "ismanufacturer", 1);
        exec("UPDATE contacts SET invisible = 1 WHERE ids = " + maker);
        insert("addresses", "groupsids", 1, "contactsids", customer, "prename", "Lieferung", "cname", "O'Brien", "street", "Lagerweg 2", "zip", "50668",
                "city", "Köln", "company", "O'Brien & Söhne", "country", "276", "dateadded", added, "inttype", 1);
        //products, prices, suppliers, lists
        long product = insert("products", "cname", "Beratung 'Premium'", "cnumber", "P-001", "description", "Stunde \\ Beratung – inkl. Bericht",
                "externalnetvalue", 120.5, "internalnetvalue", 60.25, "measure", "Std.", "taxids", 2, "manufacturersids", maker, "suppliersids", supplier,
                "groupsids", 1, "productgroupsids", pgroup, "url", "https://example.org/p", "ean", "4006381333931", "reference", "REF-1",
                "dateadded", added, "inttype", 1, "stockvalue", 3.5, "thresholdvalue", 1.0, "intinventorytype", 1);
        long product2 = insert("products", "cname", "Schraube M5", "cnumber", "P-002", "externalnetvalue", 0.1, "internalnetvalue", 0.02, "measure", "Stk",
                "taxids", tax16, "groupsids", 1, "productgroupsids", 1, "dateadded", added, "inttype", 0);
        insert("productprices", "cname", "Staffel 10", "productsids", product2, "externalnetvalue", 0.08, "internalnetvalue", 0.02, "mincountvalue", 10.0, "dateadded", added, "groupsids", 1);
        insert("productstosuppliers", "productsids", product2, "contactsids", supplier, "cname", "Lieferant", "groupsids", 1, "dateadded", added);
        long plist = insert("productlists", "cname", "Standardpaket", "groupsids", 1, "description", "Liste 'A'", "dateadded", added);
        insert("productlistitems", "cname", "Schraube M5", "groupsids", 1, "productlistsids", plist, "originalproductsids", product2, "countvalue", 1.0,
                "quantityvalue", 100.0, "measure", "Stk", "internalvalue", 0.02, "totalnetvalue", 10.0, "totalbrutvalue", 11.9, "externalvalue", 0.1, "taxpercentvalue", 19.0, "dateadded", added);
        //documents: invoices, offer, order (with reference), positions
        long invoice = insert("items", "cname", "Rechnung 'Projekt X'", "cnumber", "RE-00001", "description", "Projekt \\ Phase 1", "groupsids", 1,
                "accountsids", account, "contactsids", customer, "netvalue", 241.0, "taxvalue", 45.79, "discountvalue", 0.0, "shippingvalue", 4.9,
                "datetodo", d("2018-09-20"), "dateend", d("2018-10-04"), "intreminders", 0, "inttype", 0, "dateadded", d("2018-09-20"), "intaddedby", hans,
                "intstatus", 1, "discountgrosvalue", 0.0);
        long invoice2 = insert("items", "cname", "Rechnung Schrauben", "cnumber", "RE-00002", "groupsids", 1, "accountsids", account, "contactsids", customer,
                "netvalue", 10.0, "taxvalue", 1.9, "datetodo", d("2018-09-21"), "dateend", d("2018-10-05"), "intreminders", 1, "inttype", 0,
                "dateadded", d("2018-09-21"), "intstatus", 4);
        long offer = insert("items", "cname", "Angebot", "cnumber", "AN`001", "groupsids", 1, "accountsids", account, "contactsids", customer,
                "netvalue", 241.0, "taxvalue", 45.79, "datetodo", d("2018-09-01"), "dateend", d("2018-09-15"), "inttype", 1, "dateadded", d("2018-09-01"), "intstatus", 3);
        long order = insert("items", "cname", "Auftrag", "cnumber", "AU-00001", "groupsids", 1, "accountsids", account, "contactsids", customer,
                "netvalue", 241.0, "taxvalue", 45.79, "datetodo", d("2018-09-10"), "dateend", d("2018-09-10"), "inttype", 2, "dateadded", d("2018-09-10"),
                "intstatus", 3);
        if (version >= 1.1983) {
            exec("UPDATE items SET reforderids = " + offer + " WHERE ids = " + order);
        }
        insert("subitems", "cname", POSITION, "itemsids", invoice, "groupsids", 1, "originalproductsids", product, "countvalue", 1.0, "quantityvalue", 2.0,
                "measure", "Std.", "linkurl", null, "description", "Beschreibung mit 'Zitat' und C:\\Pfad", "internalvalue", 60.25, "totalnetvalue", 241.0,
                "totalbrutvalue", 286.79, "externalvalue", 120.5, "taxpercentvalue", 19.0, "dateadded", d("2018-09-20"), "ordernr", 1, "inttype", 0, "discount", 0.0);
        insert("subitems", "cname", "Schraube M5", "itemsids", invoice2, "groupsids", 1, "originalproductsids", product2, "countvalue", 2.0, "quantityvalue", 100.0,
                "measure", "Stk", "description", "", "internalvalue", 0.02, "totalnetvalue", 10.0, "totalbrutvalue", 11.9, "externalvalue", 0.1,
                "taxpercentvalue", 19.0, "dateadded", d("2018-09-21"), "ordernr", 1, "inttype", 0, "discount", 0.0);
        insert("subitems", "cname", "Text-Position", "itemsids", offer, "groupsids", 1, "countvalue", 1.0, "quantityvalue", 0.0,
                "measure", "", "description", NOTES.substring(0, 2500), "totalnetvalue", 0.0, "dateadded", d("2018-09-01"), "ordernr", 1, "inttype", 1, "discount", 0.0);
        insert("itemstoaccounts", "itemsids", invoice, "accountsids", account);
        insert("favourites", "cname", "items", "usersids", hans, "groupsids", 1, "itemsids", invoice, "dateadded", added);
        //reminders and stages
        long stage = insert("stages", "cname", "1. Mahnung", "description", "Bitte zahlen Sie 'umgehend'", "extravalue", 5.0, "dateadded", added, "groupsids", 1, "templategroup", 1);
        insert("reminders", "cname", "Mahnung RE-00002", "description", "erste", "stagesids", stage, "itemsids", invoice2, "extravalue", 5.0, "dateadded", added, "groupsids", 1);
        //schedule
        insert("schedule", "cname", "Rechnung 'monatlich'", "groupsids", 1, "usersids", hans, "itemsids", invoice, "stopdate", d("2019-12-31"),
                "startdate", d("2018-10-01"), "nextdate", d("2018-11-01"), "isdone", 0, "intervalmonth", 1, "dateadded", added, "contactsids", customer, "eventtype", 1);
        //files: template, attachments, language, plugin
        insert("files", "cname", "tplrechnung001", "groupsids", 1, "dateadded", added, "data", TEMPLATE_FILE, "filesize", (long) TEMPLATE_FILE.length);
        insert("files", "cname", "att0000000001", "groupsids", 1, "dateadded", added, "data", ATTACHMENT, "filesize", 256L);
        insert("files", "cname", "lang000000001", "groupsids", 1, "dateadded", added, "data", LANGUAGE_FILE, "filesize", (long) LANGUAGE_FILE.length);
        insert("files", "cname", "plugin0000001", "groupsids", 1, "dateadded", added, "data", ATTACHMENT, "filesize", 256L);
        long template = insert("templates", "cname", "Rechnung O'Brien.odt", "format", "1,2,4,5,6", "description", "Meine Vorlage \\ 2018", "intsize", (long) TEMPLATE_FILE.length,
                "mimetype", "0", "filename", "tplrechnung001", "dateadded", added, "groupsids", 1, "printer", "", "isupdateenabled", 1, "lastmodified", 1537000000000L,
                "pathtofile", PATH + "\\Rechnung.odt");
        insert("templatestousers", "cname", template + "@1@1", "usersids", 1, "templatesids", template, "groupsids", 1);
        insert("templatestousers", "cname", template + "@" + hans + "@1", "usersids", hans, "templatesids", template, "groupsids", 1);
        insert("filestocontacts", "cname", "Vertrag.pdf", "description", "Vertrag 'unterschrieben'", "contactsids", customer, "filename", "att0000000001",
                "intsize", 256L, "mimetype", "pdf", "dateadded", added, "groupsids", 1);
        insert("filestoitems", "cname", "Lieferschein.pdf", "description", "zur Rechnung", "itemsids", invoice, "filename", "att0000000001",
                "intsize", 256L, "mimetype", "pdf", "dateadded", added, "groupsids", 1);
        insert("filestoproducts", "cname", "Bild.png", "description", "Produktbild", "productsids", product, "filename", "att0000000001",
                "intsize", 256L, "mimetype", "png", "dateadded", added, "groupsids", 1);
        insert("languages", "cname", "de_DE_custom", "groupsids", 1, "longname", "Deutsch (eigene)", "filename", "lang000000001", "dateadded", added);
        long plugin = insert("plugins", "cname", "Mein Plugin", "description", "macht 'etwas'", "filename", "plugin0000001", "dateadded", added, "groupsids", 1);
        insert("pluginstousers", "cname", "Mein Plugin", "usersids", hans, "pluginsids", plugin, "dateadded", added, "groupsids", 1);
        //number formats: invoices, offers (with `)
        insert("formatstousers", "cname", "RE-{0,number,00000}", "usersids", 1, "inttype", 0);
        insert("formatstousers", "cname", new Raw(OFFER_FORMAT), "usersids", 1, "inttype", 1);
        //values, fonts, mails (mail templates), mass print, history
        insert("valueproperties", "cname", "einstellung", "classname", "mpv5.db.objects.Contact", "contextids", 1, "objectids", customer,
                "dateadded", added, "groupsids", 1, "value", VALUE_BLOB);
        insert("fontsforitext", "cname", "DejaVu Sans", "encoding", "Cp1252", "embedded", 1, "size", 11.0, "style", 0, "color", 0L, "filename", "DejaVuSans.ttf", "font", ATTACHMENT);
        insert("mails", "cname", "Rechnungsversand", "dateadded", added, "groupsids", 1, "usersids", hans, "description", MAIL);
        insert("massprintrules", "cname", "Kunden in Köln", "inttype", -1, "content", "Select * from contacts where city = 'Köln'", "dateadded", added, "groupsids", 1);
        insert("history", "cname", "Rechnung 'Projekt X' geändert", "username", "hans", "dbidentity", "items", "intitem", (int) invoice, "groupsids", 1, "dateadded", d("2018-09-20"), "intaddedby", hans);
        //conversations (RTF), activities, product orders
        insert("conversations", "cname", "Telefonat", "adress", "Jürgen O'Brien", "cnumber", "000001", "groupsids", 1, "date", d("2018-09-02"),
                "dateadded", d("2018-09-03"), "content", RTF, "contactsids", customer);
        long activity = insert("activitylists", "cname", "Stunden September", "cnumber", "TL-0001", "groupsids", 1, "contactsids", customer, "orderids", order,
                "totalamount", 241.0, "dateadded", added, "isbilled", 1);
        insert("activitylistitems", "cname", "Termin", "groupsids", 1, "activitylistsids", activity, "datedoing", d("2018-09-05"), "quantityvalue", 2.0,
                "measure", "Std.", "description", "vor Ort 'Köln'", "internalvalue", 60.25, "taxpercentvalue", 19.0, "totalbrutvalue", 286.79, "productsids", product, "dateadded", added);
        long porder = insert("productorders", "cname", "Bestellung Schrauben", "cnumber", "BE-00001", "description", "dringend 'bitte'", "groupsids", 1,
                "contactsids", supplier, "netvalue", 2.0, "taxvalue", 0.38, "datetodo", d("2018-09-05"), "dateend", d("2018-09-12"), "inttype", 13, "dateadded", added, "intstatus", 1);
        insert("productordersubitems", "cname", "Schraube M5", "productordersids", porder, "groupsids", 1, "originalproductsids", product2, "countvalue", 1.0,
                "quantityvalue", 100.0, "measure", "Stk", "description", "", "internalvalue", 0.02, "totalnetvalue", 2.0, "totalbrutvalue", 2.38,
                "externalvalue", 0.02, "taxpercentvalue", 19.0, "dateadded", added, "inttype", 0, "ordernr", 1, "discount", 0.0);
        //expenses and revenues
        insert("expenses", "groupsids", 1, "cnumber", "A-0001", "cname", "Büromaterial 'Papier'", "accountsids", 2, "netvalue", 10.0, "taxpercentvalue", 19.0,
                "brutvalue", 11.9, "description", "Quittung \\ 12", "dateadded", d("2018-08-01"), "dateend", d("2018-08-01"), "ispaid", 1);
        if (version >= 1.1982) {
            insert("revenues", "groupsids", 1, "cnumber", "E-0001", "cname", "Einnahme bar", "accountsids", 3, "netvalue", 50.0, "taxpercentvalue", 19.0,
                    "brutvalue", 59.5, "description", "Kasse", "dateadded", d("2018-08-02"), "dateend", d("2018-08-02"), "status", 4, "contactsids", customer);
        } else {
            //before 1.1982: ispaid (1 = paid), updated to status 4
            insert("revenues", "groupsids", 1, "cnumber", "E-0001", "cname", "Einnahme bar", "accountsids", 3, "netvalue", 50.0, "taxpercentvalue", 19.0,
                    "brutvalue", 59.5, "description", "Kasse", "dateadded", d("2018-08-02"), "dateend", d("2018-08-02"), "ispaid", 1);
        }
        //web shop
        long shop = insert("webshops", "cname", "Shop", "description", "Mein 'Shop'", "groupsids", 1, "dateadded", added, "isrequestcompression", 0,
                "isauthenticated", 1, "username", "shop", "passw", "geheim", "interv", 60L, "url", "https://shop.example.org");
        insert("wscontactsmapping", "webshopsids", shop, "cname", "ws1", "groupsids", 1, "contactsids", customer, "wscontact", "C-77", "dateadded", added);
        insert("wsitemsmapping", "webshopsids", shop, "cname", "ws1", "groupsids", 1, "itemsids", invoice, "wsitem", "O-77", "dateadded", added);
        //a lock of the old program (not imported), a trash bin entry made by hand
        insert("tablelock", "cname", "contacts", "rowid", customer, "usersids", hans);
        insert("trashbin", "cname", "items", "rowid", 999L, "description", "alter Eintrag 'x'", "deleteme", 1);
        //everything the installation and the triggers wrote
        count("accounts", "countries", "globalsettings", "groups", "massprintrules", "productgroups", "scheduletypes", "tax", "users",
                "searchindex", "trashbin", "contacts");
    }

    /**
     * Create a database: folder [user [version]]
     */
    public static void main(String[] args) throws Exception {
        create(new File(args[0]), args.length > 1 ? args[1] : null, args.length > 2 ? Double.parseDouble(args[2]) : 1.1984);
        System.out.println("created " + args[0] + ": " + ROWS);
    }
}
