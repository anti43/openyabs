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
import java.io.InputStream;
import java.util.Date;
import mpv5.Main;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryData;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.utils.files.FileDirectoryHandler;

/**
 * What a new database gets: the countries, the bundled languages, the
 * company info of the first start and the demo templates; the invoice
 * template assigned to the administrator is the small business one
 * (Kleinunternehmer, no VAT) or the regular one
 */
public final class FirstStartSetup {

    private FirstStartSetup() {
    }

    /**
     * Imports the bundled country list
     *
     * @throws Exception
     */
    public static void importCountries() throws Exception {
        InputStream s = Main.class.getResourceAsStream("/extra/countries.xml");
        File f = FileDirectoryHandler.getTempFile("countries", "xml");
        FileDirectoryHandler.copyFile(s, f);
        LanguageManager.importCountries(f);
    }

    /**
     * Imports a bundled language (e.g. "DE") without switching to it
     *
     * @param langid
     * @return the id of the imported language
     * @throws Exception
     */
    public static String importLanguage(String langid) throws Exception {
        InputStream s = Main.class.getResourceAsStream("/extra/" + langid + "-" + Constants.EXTRA_FILES_VERSION + ".properties");
        File f = FileDirectoryHandler.getTempFile(langid.toLowerCase() + "-" + Constants.VERSION, "properties");
        FileDirectoryHandler.copyFile(s, f);
        return LanguageManager.importLanguageQuietly(f.getName() + " (" + new Date() + ")", f);
    }

    /**
     * Imports the demo invoice template ("Demo - Rechnung", with the EPC QR
     * code) and assigns it to the user
     *
     * @param owner
     * @param wait how long to wait for the import, 0: do not wait
     * @return true if the template was imported (in time)
     * @throws Exception
     */
    public static boolean importTemplate(final User owner, long wait) throws Exception {
        if (isSmallBusiness(owner)) {
            return importTemplate(owner, SMALL_BUSINESS_INVOICE_TEMPLATE, SMALL_BUSINESS_INVOICE_NAME,
                    POSITIONS_FORMAT, Constants.TYPE_INVOICE, wait) != null;
        }
        return importTemplate(owner, DEMO_INVOICE_TEMPLATE, "Demo - Rechnung",
                ITEM_FORMAT, Constants.TYPE_INVOICE, wait) != null;
    }

    /**
     * @param user
     * @return true if the company of the user uses the small business VAT
     * exemption (Kleinunternehmer)
     */
    public static boolean isSmallBusiness(User user) {
        try {
            String global = mpv5.globals.CompanyInfo.getProperty(mpv5.utils.export.SmallBusiness.KEY_ENABLED);
            if (!global.isEmpty()) {
                return "true".equalsIgnoreCase(global.trim());
            }
            return user != null && "true".equalsIgnoreCase(String.valueOf(
                    user.getProperty("companyinfo." + mpv5.utils.export.SmallBusiness.KEY_ENABLED)).trim());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Without an invoice template the user cannot print, create PDFs or send
     * invoices. At login: if no invoice template is assigned to the user, the
     * bundled demo template is imported and assigned to "All Groups" - once per
     * user, so a template deleted on purpose does not come back. Covers
     * databases not created by the first start (an existing folder, a server)
     * and new users.
     *
     * @param user the user who logs in
     */
    public static void ensureInvoiceTemplate(User user) {
        try {
            if (user == null || user.__getIDS() <= 0
                    || user.getProperties().getProperty(DEMO_TEMPLATE, "imported")) {
                return;
            }
            ReturnValue rv = QueryHandler.getConnection().freeQuery(
                    "SELECT templatesids FROM templatestousers LEFT OUTER JOIN templates AS templates0 ON "
                    + "templates0.ids = templatestousers.templatesids WHERE templatestousers.usersids = "
                    + user.__getIDS() + " AND templates0.mimetype = '" + Constants.TYPE_INVOICE + "'",
                    MPSecurityManager.VIEW, null);
            if (!rv.hasData()) {
                Log.Debug(FirstStartSetup.class, "No invoice template for " + user + ": importing the demo template");
                if (!importTemplate(user, 60000)) {
                    //not imported (in time): try again at the next login
                    return;
                }
            }
            user.getProperties().changeProperty(DEMO_TEMPLATE, "imported", true);
            user.saveProperties();
        } catch (Exception e) {
            Log.Debug(FirstStartSetup.class, e);
        }
    }

    private static final String DEMO_TEMPLATE = "org.openyabs.templateproperty";

    /**
     * Imports a bundled template and assigns it to the user
     *
     * @param owner
     * @param resource the template in the jar, e.g. /extra/Kassenbon.odt
     * @param name the name of the template
     * @param format the columns of the positions (e.g. "2,4,9")
     * @param type the kind of documents (Constants.TYPE_...)
     * @param wait how long to wait for the import
     * @return the template, null if it was not imported in time
     * @throws Exception
     */
    public static Template importTemplate(final User owner, String resource, final String name, final String format, final int type, long wait) throws Exception {
        return importTemplate(owner, resource, name, format, type, wait, true);
    }

    /**
     * Imports a template from the jar, optionally without assigning it
     *
     * @param assign assign it to the owner for all groups
     */
    public static Template importTemplate(final User owner, String resource, final String name, final String format, final int type, long wait, final boolean assign) throws Exception {
        final File f = FileDirectoryHandler.getTempFile(name, "odt");
        try {
            try (InputStream s = Main.class.getResourceAsStream(resource)) {
                if (s == null) {
                    Log.Debug(FirstStartSetup.class, "Template resource not found: " + resource);
                    return null;
                }
                FileDirectoryHandler.copyFile(s, f);
            }
            String stored = QueryHandler.instanceOf().clone(Context.getFiles()).insertFileNow(f);
            if (stored == null) {
                return null;
            }
            int addedBy = owner != null && owner.__getIDS() > 0
                    ? owner.__getIDS()
                    : User.getCurrentUser().__getIDS();
            QueryData x = new QueryData();
            x.add("cname", name);
            x.add("filename", stored);
            x.add("description", "Wizard insert");
            x.add("dateadded", mpv5.utils.date.DateConverter.getTodayDBDate());
            x.add("intaddedby", addedBy);
            x.add("intsize", f.length());
            x.add("mimetype", String.valueOf(type));
            x.add("format", format);
            x.add("groupsids", 1);
            int newid = QueryHandler.instanceOf().clone(Context.getTemplate()).insert(x, null);
            if (newid <= 0) {
                return null;
            }
            DatabaseObject.uncacheObject(Context.getTemplate(), newid);
            Template t = (Template) DatabaseObject.getObject(Context.getTemplate(), newid);
            if (assign && owner != null && owner.__getIDS() > 0) {
                QueryData c = new QueryData();
                c.add("usersids", owner.__getIDS());
                c.add("templatesids", t.__getIDS());
                c.add("groupsids", 1);
                c.add("cname", t.__getIDS() + "@" + owner.__getIDS() + "@" + 1);
                QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).insert(c, null);
            }
            TemplateHandler.clearCache();
            return t;
        } finally {
            f.delete();
        }
    }

    /**
     * Prepares a new database: countries, all bundled languages (the user
     * switches to German on a German system) and the invoice template. Every
     * step is tried, a failing step does not stop the others.
     *
     * @param admin the administrator of the new database
     * @param german switch the administrator to German
     * @param progress told before every step, may be null
     */
    public static void prepare(User admin, boolean german, Progress progress) {
        prepare(admin, german, progress, null);
    }

    /**
     * Prepares a new database (see {@link #prepare(User, boolean, Progress)})
     * with the company info of the first start
     *
     * @param admin the administrator of the new database
     * @param german switch the administrator to German
     * @param progress told before every step, may be null
     * @param company the company info (keys without "companyinfo."), e.g.
     * {@link mpv5.utils.export.SmallBusiness#KEY_ENABLED}; null or empty for
     * none. With the small business exemption the small business invoice
     * template is the assigned invoice template.
     */
    public static void prepare(User admin, boolean german, Progress progress, java.util.Map<String, String> company) {
        saveCompany(admin, company);
        //invoice numbers unique among invoices, credit notes, deposit and part payment invoices:
        //a prefix of their own (#212)
        try {
            for (java.util.Map.Entry<Integer, String> f : FormatHandler.defaultInvoiceFormats(german).entrySet()) {
                FormatHandler.store(f.getKey(), User.ADMIN_ID, f.getValue(), 0, null);
            }
        } catch (Exception e) {
            Log.Debug(FirstStartSetup.class, e);
        }
        step(progress, Step.COUNTRIES);
        try {
            importCountries();
        } catch (Exception e) {
            Log.Debug(FirstStartSetup.class, e);
        }
        step(progress, Step.LANGUAGES);
        try {
            String de = importLanguage("DE");
            if (german) {
                admin.setLanguage(de);
            }
            //decided here, no automatic import at the first login
            admin.getProperties().changeProperty("org.openyabs.languageproperty", "autoimportde", true);
            admin.saveProperties();
            admin.save(true);
        } catch (Exception e) {
            Log.Debug(FirstStartSetup.class, e);
        }
        //the rates of the installation: VAT category and country
        completeTaxRates();
        step(progress, Step.TEMPLATES);
        //the demo templates, the demo invoice is the assigned invoice template
        importDemoTemplates(admin);
    }

    /**
     * The country of the bundled tax rates (0 %, 7 %, 19 %: the German rates),
     * as in the country list (countries.xml)
     */
    public static final String TAX_COUNTRY = "Germany";

    /**
     * Completes the tax rates of older databases and of new ones (the rates
     * of the installation have no VAT category and no country): an empty VAT
     * category becomes S (rate above 0 %) or E (0 %), the meaning it had
     * anyway (#209); rates of 0, 7 and 19 % without a country get
     * {@link #TAX_COUNTRY}. Only empty fields are changed.
     */
    public static void completeTaxRates() {
        String[] sql = {
            "UPDATE %%tablename%% SET categorycode = 'S' WHERE (categorycode IS NULL OR categorycode = '') AND taxvalue > 0",
            "UPDATE %%tablename%% SET categorycode = 'E' WHERE (categorycode IS NULL OR categorycode = '') AND (taxvalue IS NULL OR taxvalue = 0)",
            "UPDATE %%tablename%% SET country = '" + TAX_COUNTRY + "' WHERE (country IS NULL OR country = '')"
            + " AND (taxvalue IS NULL OR taxvalue = 0 OR taxvalue = 7 OR taxvalue = 19)"};
        for (String q : sql) {
            try {
                QueryHandler.instanceOf().clone(Context.getTaxes()).freeUpdateQuery(q,
                        mpv5.usermanagement.MPSecurityManager.CREATE_OR_DELETE, null);
            } catch (Exception e) {
                Log.Debug(FirstStartSetup.class, e);
            }
        }
        //cached rates would still show the old values
        DatabaseObject.clearCache(Context.getTaxes());
    }

    /**
     * The bundled demo templates (type id, name): one per kind of document,
     * with a dummy logo and a page listing all fields of the kind
     */
    public static final Object[][] DEMO_TEMPLATES = {
        {Constants.TYPE_INVOICE, "Rechnung"}, {Constants.TYPE_ORDER, "Auftrag"}, {Constants.TYPE_OFFER, "Angebot"},
        {Constants.TYPE_DELIVERY_NOTE, "Lieferschein"}, {Constants.TYPE_ORDER_CONFIRMATION, "Auftragsbestätigung"},
        {Constants.TYPE_PRODUCT, "Artikeldatenblatt"}, {Constants.TYPE_SERVICE, "Leistungsbeschreibung"},
        {Constants.TYPE_REMINDER, "Mahnung"}, {Constants.TYPE_CONTACT, "Kontaktdatenblatt"},
        {Constants.TYPE_JOURNAL, "Journal"}, {Constants.TYPE_PRODUCT_ORDER, "Bestellung"},
        {Constants.TYPE_CONTRACT, "Vertrag"}, {Constants.TYPE_CONVERSATION, "Gesprächsnotiz"},
        {Constants.TYPE_MASSPRINT, "Serienbrief"}, {Constants.TYPE_ACTIVITY, "Tätigkeitsliste"},
        {Constants.TYPE_CREDIT, "Gutschrift"}, {Constants.TYPE_PART_PAYMENT, "Abschlagsrechnung"},
        {Constants.TYPE_DEPOSIT, "Anzahlungsrechnung"}
    };

    /**
     * Imports all demo templates and assigns them to the administrator for
     * all groups (the demo invoice is the administrator's invoice template)
     *
     * @param admin
     */
    public static void importDemoTemplates(User admin) {
        //small business: the invoice without VAT is the invoice template, the regular one is there
        //but not assigned (and the other way round)
        boolean small = isSmallBusiness(admin);
        for (Object[] d : DEMO_TEMPLATES) {
            int type = (Integer) d[0];
            try {
                importTemplate(admin, String.format("/extra/templates/demo-%02d.odt", type), "Demo - " + d[1],
                        demoFormat(type), type, 60000, !(small && type == Constants.TYPE_INVOICE));
            } catch (Exception e) {
                Log.Debug(FirstStartSetup.class, e);
            }
        }
        try {
            importTemplate(admin, SMALL_BUSINESS_INVOICE_TEMPLATE, SMALL_BUSINESS_INVOICE_NAME, POSITIONS_FORMAT,
                    Constants.TYPE_INVOICE, 60000, small);
        } catch (Exception e) {
            Log.Debug(FirstStartSetup.class, e);
        }
    }

    /**
     * Saves the company info of the first start
     *
     * @param admin
     * @param company keys without "companyinfo.", empty values are skipped;
     * null for none
     */
    public static void saveCompany(User admin, java.util.Map<String, String> company) {
        if (company == null || company.isEmpty()) {
            return;
        }
        try {
            for (java.util.Map.Entry<String, String> e : company.entrySet()) {
                String v = e.getValue() == null ? "" : e.getValue().trim();
                if (!v.isEmpty()) {
                    mpv5.globals.CompanyInfo.setProperty(e.getKey(), v);
                }
            }
            mpv5.globals.CompanyInfo.save();
        } catch (Exception e) {
            Log.Debug(FirstStartSetup.class, e);
        }
    }

    /**
     * The column format older versions imported every template with: count,
     * quantity, text, price, tax rate, gross - the 5-column table of the
     * templates showed the tax rate under "total"
     */
    public static final String OLD_FORMAT = "1,2,4,5,7,9";
    /**
     * The regular invoice template, imported on the first start
     */
    public static final String INVOICE_TEMPLATE = "/extra/templates/invoice.odt";
    /**
     * The demo invoice template (with logo placeholder and EPC QR code), the
     * invoice template assigned on the first start and at the first login of
     * users without one
     */
    public static final String DEMO_INVOICE_TEMPLATE = "/extra/templates/demo-00.odt";
    /**
     * The invoice template of a small business (Kleinunternehmer, § 19 UStG):
     * no VAT, the note of the exemption, the EPC QR code; the assigned invoice
     * template when the small business exemption is chosen at the first start
     */
    public static final String SMALL_BUSINESS_INVOICE_TEMPLATE = "/extra/templates/demo-00-ku.odt";
    public static final String SMALL_BUSINESS_INVOICE_NAME = "Demo - Rechnung Kleinunternehmer";
    /**
     * The table columns of the documents: position, quantity, text, price,
     * tax rate, net total (the 6-column table of the bundled templates; the
     * tax rate of each position belongs on the invoice, § 14 Abs. 4 UStG)
     */
    public static final String ITEM_FORMAT = "1,2,4,5,7,6";
    /**
     * The table columns of the purchase order demo: position, quantity, text,
     * price, net total (5 columns)
     */
    public static final String POSITIONS_FORMAT = "1,2,4,5,6";
    /**
     * The activity list: position, quantity, text, price, gross total
     */
    public static final String ACTIVITY_FORMAT = "1,2,4,5,9";
    /**
     * The table columns of the journal demo: date, number, type, name,
     * account, status, net, tax, gross, profit (see tools/templates/gen.py)
     */
    public static final String JOURNAL_FORMAT = "6,9,10,1,8,11,12,13,14,15";

    /**
     * @param type
     * @return the column format of the demo template of the type
     */
    public static String demoFormat(int type) {
        switch (type) {
            case Constants.TYPE_JOURNAL:
                return JOURNAL_FORMAT;
            case Constants.TYPE_ACTIVITY:
                return ACTIVITY_FORMAT;
            case Constants.TYPE_PRODUCT_ORDER:
                return POSITIONS_FORMAT;
            default:
                return ITEM_FORMAT;
        }
    }

    /**
     * SHA-256 of the demo templates of older versions, by type
     */
    private static final java.util.Map<Integer, String> OLD_DEMOS = new java.util.HashMap<>();
    /**
     * SHA-256 of the regular invoice templates of older versions
     */
    private static final java.util.Set<String> OLD_INVOICE_TEMPLATES = new java.util.HashSet<>(java.util.Arrays.asList(
            "797a9cc32dc0b66a38c04e4aa86700a66317d21a41adba9e66278765332e1ea2",
            "51a57b06204d9827b135e3d6c11b429f4458a473d08186eeaffda23783b1b508"));

    /**
     * SHA-256 of the bundled templates of previous releases -> the resource
     * that replaces them (the invoices got the service date, item.servicedate,
     * #208, then the VAT ID and the notes of the VAT categories,
     * companyinfo.vatid and item.taxnotes, #209); a template whose file was
     * changed is kept
     */
    private static final java.util.Map<String, String> PREVIOUS_RELEASE = new java.util.HashMap<>();

    static {
        PREVIOUS_RELEASE.put("622057ccf9692bd0285a178be7beb0d206c61b08ac967d80c2a78e8c1e9550d8", SMALL_BUSINESS_INVOICE_TEMPLATE);
        PREVIOUS_RELEASE.put("ddef24e033e53fcc9013ce093be3525e1e82b7f8b0d9010a137f6cd8ff4257f1", "/extra/templates/demo-00.odt");
        PREVIOUS_RELEASE.put("a4e934ca106c445a60b4e840ad6ea7edca10b727bc6b7e19a638a676b07d9b23", "/extra/templates/demo-01.odt");
        PREVIOUS_RELEASE.put("a541ce696ce70606d4bc33aea456213df342cfbf38cacd49e9b1ab0cd63b448c", "/extra/templates/demo-02.odt");
        PREVIOUS_RELEASE.put("a4211fc213295818e2a3f25ed81d85db2e0d8f6a0774b9896c52f41865921839", "/extra/templates/demo-03.odt");
        PREVIOUS_RELEASE.put("67e5aa91101a5a75ffe001341e09cecbe003d67c63bf19883bfe44abaa84c13d", "/extra/templates/demo-04.odt");
        PREVIOUS_RELEASE.put("981ea02f19f8ddb11c57a8b22a904ab623cc97dfdfc677bc456adccea63b7c6a", "/extra/templates/demo-07.odt");
        PREVIOUS_RELEASE.put("eb5639643aaf728b03fecc7c61cecf2604f251f2ce1694854eafa6537dd66046", "/extra/templates/demo-11.odt");
        PREVIOUS_RELEASE.put("6cb9f8d0a5933e3cf614cab8cf491176ce773ceb665a2c654868827597f2659b", "/extra/templates/demo-20.odt");
        PREVIOUS_RELEASE.put("04ece9267c08364f5fcfa80636c142eab2c5cbdfa652fe2f21932525065a11b5", "/extra/templates/demo-21.odt");
        PREVIOUS_RELEASE.put("c91c15c26612f8df1cb17706ec67ca0f658566df5c4deec9a05e5092dbe376ea", "/extra/templates/demo-22.odt");
        PREVIOUS_RELEASE.put("16d3cc0bf3872fe4efa550032dde9410bbde8b87bc13733646ee22529d629a53", INVOICE_TEMPLATE);
        //the templates with the service date (#208), before the VAT ID and the notes of the VAT categories (#209)
        PREVIOUS_RELEASE.put("6cc5913d02cbf9d16fb92133742f3ff542c9a58e417f9b92c41b46e2c0a25baf", SMALL_BUSINESS_INVOICE_TEMPLATE);
        PREVIOUS_RELEASE.put("1886d10d7fbd93f11e9821af902ad3bb266724fb945b8254a05feca189105ccc", "/extra/templates/demo-00.odt");
        PREVIOUS_RELEASE.put("526ce5d4b065567c5887a4197de4528b306e2d1d737d872146224e1c0e50b8a7", "/extra/templates/demo-01.odt");
        PREVIOUS_RELEASE.put("b3b495e072fea03e9f4120b73fad4519a919da1836c489c4f10f885180132e94", "/extra/templates/demo-02.odt");
        PREVIOUS_RELEASE.put("fc8637a4a059b3d683848150492848bc52254a1ec4d905bcf5876810a679bfd8", "/extra/templates/demo-03.odt");
        PREVIOUS_RELEASE.put("72b77d848568332240feff525e11cbbf75f59cf016321ab6efcfa8d84f7f89eb", "/extra/templates/demo-04.odt");
        PREVIOUS_RELEASE.put("20b9bbf5705fc3b7953effa78a1ba49bc374d56525918cbe56f65b30e0700024", "/extra/templates/demo-07.odt");
        PREVIOUS_RELEASE.put("96a2055b5a3da7365a9ffa7a1365330a64661d9fbd94d5914bcaeb55f723df1e", "/extra/templates/demo-11.odt");
        PREVIOUS_RELEASE.put("a76f80b5a627dbcc597cbb5cc2f69c0b31a398d889faaa265c77a67dabf834a5", "/extra/templates/demo-20.odt");
        PREVIOUS_RELEASE.put("c0b61bc6f77db59cc2b61e1c98c36a08be9653399f30d664cef4d6f9c190453b", "/extra/templates/demo-21.odt");
        PREVIOUS_RELEASE.put("6e09c0997ff6b75c22dd1555fee507c513d644c9fd8feaf72e9dc1779c85921c", "/extra/templates/demo-22.odt");
        PREVIOUS_RELEASE.put("b34ebf1bc103c0871f049feddbaf150868b8da062a156bf57d26969dd48e4716", INVOICE_TEMPLATE);
        //with the VAT ID and the notes of the VAT categories (#209), before the reference of credit notes (#211)
        PREVIOUS_RELEASE.put("2966c886f32735048952523a95200e899a018001179e55242d1c51aadc3e21ce", SMALL_BUSINESS_INVOICE_TEMPLATE);
        PREVIOUS_RELEASE.put("1f169b2817ccf906b70018f7cec207fc9bf943abede238a5c743dc012f80090a", "/extra/templates/demo-00.odt");
        PREVIOUS_RELEASE.put("87b3cc87e0cec0ef155f4b7ae367620a74fae2532ca29f68333431582af269c7", "/extra/templates/demo-01.odt");
        PREVIOUS_RELEASE.put("0e77a4df19a73068d9c89eac417cdcc55ae13380e7cc564854ac18a1b760753d", "/extra/templates/demo-02.odt");
        PREVIOUS_RELEASE.put("bebabcc37fdc64f5c8e37f38a8164b287de290eb7994edc24796d274b5567279", "/extra/templates/demo-03.odt");
        PREVIOUS_RELEASE.put("3cbf82aa48024c35f6ff82ef1b989f95378cc499102a3866c6eca4ce6fa02e9c", "/extra/templates/demo-04.odt");
        PREVIOUS_RELEASE.put("e44f064056031146b8cb7a3be438216df2dcbc632c858246d5ba8a52dd769302", "/extra/templates/demo-07.odt");
        PREVIOUS_RELEASE.put("003cc009e737cebd057be882be32e4aedb5ada9b2845194955fd196d00cd79c8", "/extra/templates/demo-11.odt");
        PREVIOUS_RELEASE.put("c759d467f9746cc5b72e752f2391ae818f3fb62d880c6012a746c2e60b25e9da", "/extra/templates/demo-20.odt");
        PREVIOUS_RELEASE.put("86255683f04e5b495f1458878da16c765c5141f6959daaca66af8cbd2aa8eae4", "/extra/templates/demo-21.odt");
        PREVIOUS_RELEASE.put("baac7a3abb29ef79ba0ffb71f52d26adbd70863956c354cf14b02ca8b0362286", "/extra/templates/demo-22.odt");
        //with the reference of credit notes (#211), before the QR code got room on the first page (no empty paragraphs around it)
        PREVIOUS_RELEASE.put("63aaa2f51de2cd680f7f1203512a1348ad25330d5d6d53d357c3ed58498274d5", SMALL_BUSINESS_INVOICE_TEMPLATE);
        PREVIOUS_RELEASE.put("411795a40e1ca7438298a025b5a7db6c21bf2339887ecc200501531d43537fd0", "/extra/templates/demo-00.odt");
        PREVIOUS_RELEASE.put("c1a38c429ac33e452a781d42ff6d86921ebf57985fae65bb5306096c960a009a", "/extra/templates/demo-01.odt");
        PREVIOUS_RELEASE.put("82a221927578668804813a0bfbbf00903f6d16961a6f74b27697da8466363472", "/extra/templates/demo-02.odt");
        PREVIOUS_RELEASE.put("2cf743fc0f6c8b44dfe41e30f1a126555d62649c3b3650d5954d5ebabf60efb1", "/extra/templates/demo-03.odt");
        PREVIOUS_RELEASE.put("00337a486ee7e91eea78dc6eda9d023ea59e98960f4498858f00498f40229563", "/extra/templates/demo-04.odt");
        PREVIOUS_RELEASE.put("16111dbc71ea51c1b647b32f14e33a68cd0921221ece766ca3dc3fe324b422db", "/extra/templates/demo-07.odt");
        PREVIOUS_RELEASE.put("7c25234deaa14ad1c0ad474e7b3929f4e57f566c0ba5396980087d57ae7f9f52", "/extra/templates/demo-10.odt");
        PREVIOUS_RELEASE.put("595a1b1746df6f2cf69098efe9a66d347ec91fa65c8e18163898f250b0a5c2cf", "/extra/templates/demo-11.odt");
        PREVIOUS_RELEASE.put("9098864cb6c4d4e788aeac3090aba5320c71e970b134a52060fb3554b5d0571e", "/extra/templates/demo-14.odt");
        PREVIOUS_RELEASE.put("8c675b7851f0eac594fc366fc243aadbaa6f5af373b3fbbdb9f1f8219e170a91", "/extra/templates/demo-20.odt");
        PREVIOUS_RELEASE.put("14f1d1c0c196d8971758e7314412f0e870b9912d7bb40b71ce894c754ee63357", "/extra/templates/demo-21.odt");
        PREVIOUS_RELEASE.put("84b4a680d6d519a3a7a9477874457d87b3b2ac33d9f3c3d9a18f59db535943af", "/extra/templates/demo-22.odt");
        PREVIOUS_RELEASE.put("c11b393baee5a6f03eb39ec9e1094be90ecfd97cb5e583a2f5734d27c690b7b0", INVOICE_TEMPLATE);
        //v2.0.13, before compact QR code beside totals
        PREVIOUS_RELEASE.put("182d55fd1b4465bb6e58cdae10554911bdc7fc8c461dbc603e63143afcc69eb2", SMALL_BUSINESS_INVOICE_TEMPLATE);
        PREVIOUS_RELEASE.put("a941eb984121ddb051c72a67aaed10374b323d557e5ae57b741c6281fe805f58", "/extra/templates/demo-00.odt");
        PREVIOUS_RELEASE.put("7c36dd297b31c0985ebbb6dd9819bfa5d6682865a7b3c5f2328e3921a1df5b67", "/extra/templates/demo-01.odt");
        PREVIOUS_RELEASE.put("0f64139cdd9193ce32721cd0c033d4609236f61f748c47df873e7d9d45fd7717", "/extra/templates/demo-02.odt");
        PREVIOUS_RELEASE.put("f32d2bc603e818fe00fdaf7d0d82b9f17205a6eb5e7f48bf345ab4f65d7fae4e", "/extra/templates/demo-03.odt");
        PREVIOUS_RELEASE.put("b686ab463232ab8e0bfecde452988afd971796989b19577d9d9937d83714e6f9", "/extra/templates/demo-04.odt");
        PREVIOUS_RELEASE.put("df8a4dd642cf992c9cda4abd6ef61b3d9dbfa683315c3ef9dec272aa3ba64ae8", "/extra/templates/demo-11.odt");
        PREVIOUS_RELEASE.put("8b6e6f0538db8f836ca457dbb1b76230c97c63b80073fc918ff3b7bc27d65bd0", "/extra/templates/demo-20.odt");
        PREVIOUS_RELEASE.put("b195f17680b5936e2716e6d10a2a2b8894da41908d969efd4ed431254fbe8d99", "/extra/templates/demo-21.odt");
        PREVIOUS_RELEASE.put("e7c2c98733c4d6a6b14d84325655193e664b86ad0d9293b63b69b76e4438c286", "/extra/templates/demo-22.odt");
        PREVIOUS_RELEASE.put("8e5e5e3fb681cd5ef2e5255e163f72091d1062314c50244741e0516c01c016f0", INVOICE_TEMPLATE);
        //before Code 39 cnumber barcode (Barcode.img) on demo templates
        PREVIOUS_RELEASE.put("3f0a98f8ca59f34fb7ab0f8a36676e3427a13f6d8890c2956e2635fed3879723", SMALL_BUSINESS_INVOICE_TEMPLATE);
        PREVIOUS_RELEASE.put("ecade8fa7d70e9275a3417b4be295ff6601db7fdbc878969788528e7e05c6f74", "/extra/templates/demo-00.odt");
        PREVIOUS_RELEASE.put("5dc70b8c75c7cdde9fece4e62094055cc4172c4196bf2caf11a90598dd6eafd1", "/extra/templates/demo-01.odt");
        PREVIOUS_RELEASE.put("cbb5efe183e9b56b395de7c3647564d46f17dc1aee96d26dddd40f436f9400cb", "/extra/templates/demo-02.odt");
        PREVIOUS_RELEASE.put("e1de01bba58c01b7cf0e6881e940e72553610af5cd5ea44c0960157ef4febf69", "/extra/templates/demo-03.odt");
        PREVIOUS_RELEASE.put("c6c49b5c37bf131547b7d7b9e59a5da20a3f5b6d5b499fc0e47cac02df7642ef", "/extra/templates/demo-04.odt");
        PREVIOUS_RELEASE.put("54739eaf363c8cd14afa91cef7b64cfa6b9d90312d9c6bf360566411979c0127", "/extra/templates/demo-05.odt");
        PREVIOUS_RELEASE.put("a040858cd143a392f12261e02458d59e4d53666f38875593bab4e534e2cd645c", "/extra/templates/demo-06.odt");
        PREVIOUS_RELEASE.put("4e924e7c7e9068bfa6ad35c6e6d97d900f0aa4bd3bdd78f1031ca5cd6e0ae94e", "/extra/templates/demo-07.odt");
        PREVIOUS_RELEASE.put("0b358dff2f87823a3029d86ae6ad922c4493c6fe39ab0c745d1678db6b450a1e", "/extra/templates/demo-08.odt");
        PREVIOUS_RELEASE.put("5937827ea1813fc0bd9bed9aef2dcad9b4237100b5fd977a365bd17fe176ee30", "/extra/templates/demo-10.odt");
        PREVIOUS_RELEASE.put("1ab97b52949ce543d5abc1a95e4c6315009eb6dd68d75e875ce0f7cb69d9cd57", "/extra/templates/demo-11.odt");
        PREVIOUS_RELEASE.put("822ce363b0f38722eebc450a372748905a059d1a3e16927cac60aaaee3adc252", "/extra/templates/demo-12.odt");
        PREVIOUS_RELEASE.put("056e6beb517d9477240c70e257e0e4aecdc50dbe61926f5a30946329be7c94d7", "/extra/templates/demo-13.odt");
        PREVIOUS_RELEASE.put("c41612852696197b61b53776c4829fafc7591025924ee36af7dde4d3fb29e4ec", "/extra/templates/demo-14.odt");
        PREVIOUS_RELEASE.put("3fcc08bd31af27bd1502e1211f5f0bd922367bfa71e0a1233003f14e2a37ff44", "/extra/templates/demo-20.odt");
        PREVIOUS_RELEASE.put("c9ce3d950c53948ccadccc627196af1ae8eff91d4a4a273b2b34b349eb0e5069", "/extra/templates/demo-21.odt");
        PREVIOUS_RELEASE.put("c294e39c0c1ebc45992e67651bea6e41a7081d7d7ceeb1b9316bbcd14ab4d5ec", "/extra/templates/demo-22.odt");
    }

    static {
        OLD_DEMOS.put(0, "653b59c8436659bcae793af1f06ab0c83257a7b4ddbd50144a6e6b558d1ac692");
        OLD_DEMOS.put(1, "3c7fc4e8bd12fef6c3d70d73a3c9ed952698d021ee0b66888e222c26a32eb198");
        OLD_DEMOS.put(2, "7366f380e3099f88bdb836504819d41c69d6be5ee8ba79f348266b602b6f2612");
        OLD_DEMOS.put(3, "e9d2577cc4f3e7832853f3897d4ce0291f7ed9c71b1ec7eab2259c790a2769d9");
        OLD_DEMOS.put(4, "6110ccf4984b0aac4eae7b2c03aa4b13f5a2f89b8e83aaae41eca4d417887321");
        OLD_DEMOS.put(5, "123b5316ae73e5f4600bb357d5520e9fc3ab7a864faf2c934aa1a000778a37eb");
        OLD_DEMOS.put(6, "d54686e37593e3ecc069f6325109c0655f9982ffa2d61df9393f9b6ea1cd7381");
        OLD_DEMOS.put(7, "587985b5de983c0379f478f4ce1fa2e6068e540f65e40c5171cdc66ac6735a4e");
        OLD_DEMOS.put(8, "1affeb8b75b3308efbd13f3d70216221ffc45939606a827e1c218226899196ca");
        OLD_DEMOS.put(9, "83c2d3bb927cb1452d6e58ae282da0343ae49326980dfb8d8c14501a46cb0d5a");
        OLD_DEMOS.put(10, "935398598f87c28cd14a55836d7c264915901e4fd6b764f05c8a67f775aeda1a");
        OLD_DEMOS.put(11, "c4fe7d042826eee233abc5f6abc9b3c789e29accb98a46f353c81c942beb2353");
        OLD_DEMOS.put(12, "32ee1d36b8bd4f2c48fa2b5835024e89da54c41326f143bd5ef9e2ed1106d84a");
        OLD_DEMOS.put(13, "19a9b20db80e50d44c0e8db1d3ef37adae4f371e96d3a9775e0529cf58683295");
        OLD_DEMOS.put(14, "41dffd1b9e6b213c7f333f8f853b6a38fa1af87a0e8e3b0b254f256c0bbe66d9");
        OLD_DEMOS.put(20, "cfdedec98d99d158bd2add46348b7446c32bd3f126577d5af63005c785c42113");
        OLD_DEMOS.put(21, "babef2c228a1782cda4f48d3865f1e6cb0ef12fe262bfb897abf6a071a9129be");
        OLD_DEMOS.put(22, "ac5882d94bc44f6c21d7d29b50a221e9fcca0f1eb353b893dbe4240bd0ddbc91");
    }

    /**
     * Templates imported by older versions: the demo templates are replaced by
     * the current ones (with content for every kind of document, the journal in
     * landscape, the service date on invoices) and the column format of very
     * old ones is corrected - but only templates whose file is still exactly
     * the one shipped; changed templates stay as they are.
     */
    public static void upgradeDemoTemplates() {
        repairUnassignedDemoTemplates();
        try {
            ReturnValue rv = QueryHandler.getConnection().freeQuery(
                    "SELECT ids FROM templates WHERE description = 'Wizard insert'",
                    MPSecurityManager.VIEW, null);
            if (!rv.hasData()) {
                return;
            }
            for (Object[] row : rv.getData()) {
                try {
                    Template t = (Template) DatabaseObject.getObject(Context.getTemplate(), Integer.valueOf(row[0].toString()));
                    int type = Integer.parseInt(t.__getMimetype());
                    boolean oldFormat = OLD_FORMAT.equals(t.__getFormat());
                    String hash = sha256(t.getFile());
                    String resource;
                    if (!oldFormat) {
                        //the previous release: the current file, the format stays
                        resource = PREVIOUS_RELEASE.get(hash);
                        if (resource == null) {
                            continue;
                        }
                    } else if (hash.equals(OLD_DEMOS.get(type))) {
                        resource = String.format("/extra/templates/demo-%02d.odt", type);
                    } else if (type == Constants.TYPE_INVOICE && OLD_INVOICE_TEMPLATES.contains(hash)) {
                        resource = INVOICE_TEMPLATE;
                    } else {
                        continue;
                    }
                    File f = FileDirectoryHandler.getTempFile("template-" + type, "odt");
                    try {
                        try (InputStream in = Main.class.getResourceAsStream(resource)) {
                            FileDirectoryHandler.copyFile(in, f);
                        }
                        QueryHandler.instanceOf().clone(Context.getFiles()).updateFileNow(f, t.__getFilename());
                    } finally {
                        f.delete();
                    }
                    if (oldFormat) {
                        t.setFormat(demoFormat(type));
                    }
                    t.save(true);
                    Log.Debug(FirstStartSetup.class, "Template updated: " + t + " (" + type + ")");
                } catch (Exception e) {
                    Log.Debug(FirstStartSetup.class, e);
                }
            }
            TemplateHandler.clearCache();
        } catch (Exception e) {
            Log.Debug(FirstStartSetup.class, e);
        }
    }

    private static final class DemoSpec {
        final int type;
        final String name;
        final String format;
        final boolean smallBusiness;

        DemoSpec(int type, String name, String format, boolean smallBusiness) {
            this.type = type;
            this.name = name;
            this.format = format;
            this.smallBusiness = smallBusiness;
        }
    }

    private static volatile java.util.Map<String, DemoSpec> demoByHash;
    private static volatile java.util.Map<String, DemoSpec> demoByFileName;

    private static void initDemoSpecs() {
        if (demoByHash != null) {
            return;
        }
        synchronized (FirstStartSetup.class) {
            if (demoByHash != null) {
                return;
            }
            java.util.Map<String, DemoSpec> byHash = new java.util.HashMap<>();
            java.util.Map<String, DemoSpec> byName = new java.util.HashMap<>();
            java.util.Map<String, DemoSpec> byResource = new java.util.HashMap<>();
            java.util.Map<Integer, DemoSpec> byType = new java.util.HashMap<>();

            for (Object[] d : DEMO_TEMPLATES) {
                int type = (Integer) d[0];
                String name = "Demo - " + d[1];
                String resource = String.format("/extra/templates/demo-%02d.odt", type);
                DemoSpec spec = new DemoSpec(type, name, demoFormat(type), false);
                byName.put(name + ".odt", spec);
                byResource.put(resource, spec);
                byType.put(type, spec);
                String hash = resourceSha256(resource);
                if (hash != null) {
                    byHash.put(hash, spec);
                }
            }
            DemoSpec smallSpec = new DemoSpec(Constants.TYPE_INVOICE, SMALL_BUSINESS_INVOICE_NAME, POSITIONS_FORMAT, true);
            byName.put(SMALL_BUSINESS_INVOICE_NAME + ".odt", smallSpec);
            byResource.put(SMALL_BUSINESS_INVOICE_TEMPLATE, smallSpec);
            String smallHash = resourceSha256(SMALL_BUSINESS_INVOICE_TEMPLATE);
            if (smallHash != null) {
                byHash.put(smallHash, smallSpec);
            }
            for (java.util.Map.Entry<String, String> e : PREVIOUS_RELEASE.entrySet()) {
                DemoSpec spec = byResource.get(e.getValue());
                if (spec != null) {
                    byHash.putIfAbsent(e.getKey(), spec);
                }
            }
            for (java.util.Map.Entry<Integer, String> e : OLD_DEMOS.entrySet()) {
                DemoSpec spec = byType.get(e.getKey());
                if (spec != null) {
                    byHash.putIfAbsent(e.getValue(), spec);
                }
            }
            demoByFileName = byName;
            demoByHash = byHash;
        }
    }

    private static String resourceSha256(String resource) {
        try (InputStream in = Main.class.getResourceAsStream(resource)) {
            return in == null ? null : sha256(in);
        } catch (Exception e) {
            Log.Debug(FirstStartSetup.class, e);
            return null;
        }
    }

    private static boolean isNumericType(String mimetype) {
        if (mimetype == null) {
            return false;
        }
        String s = mimetype.trim();
        if (s.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Repairs bundled demo templates that were left without a usage type
     * (e.g. {@code mimetype = "odt"}) in databases created before the cache
     * clear / synchronous import fix.
     */
    public static void repairUnassignedDemoTemplates() {
        try {
            ReturnValue rv = QueryHandler.getConnection().freeQuery(
                    "SELECT ids, mimetype FROM templates",
                    MPSecurityManager.VIEW, null);
            if (!rv.hasData()) {
                return;
            }
            boolean repaired = false;
            User current = User.getCurrentUser();
            boolean hasUser = current != null && current.__getIDS() > 0 && !current.isDefault();
            boolean small = hasUser && isSmallBusiness(current);
            for (Object[] row : rv.getData()) {
                String mime = row.length > 1 && row[1] != null ? String.valueOf(row[1]) : "";
                if (isNumericType(mime)) {
                    continue;
                }
                try {
                    initDemoSpecs();
                    int id = Integer.parseInt(String.valueOf(row[0]));
                    Template t = (Template) DatabaseObject.getObject(Context.getTemplate(), id);
                    DemoSpec spec = null;
                    try {
                        String hash = sha256(t.getFile());
                        spec = demoByHash.get(hash);
                    } catch (Exception e) {
                        Log.Debug(FirstStartSetup.class, e);
                    }
                    String oldCname = t.__getCname();
                    String oldDesc = t.__getDescription();
                    if (spec == null && oldCname != null
                            && (oldDesc == null || oldDesc.trim().isEmpty() || oldDesc.equals(oldCname))) {
                        spec = demoByFileName.get(oldCname);
                    }
                    if (spec == null) {
                        continue;
                    }
                    t.setMimetype(String.valueOf(spec.type));
                    if ((spec.name + ".odt").equals(oldCname)) {
                        t.setCname(spec.name);
                    }
                    String fmt = t.__getFormat();
                    if (fmt == null || fmt.trim().isEmpty() || Template.DEFAULT_FORMAT.equals(fmt)) {
                        t.setFormat(spec.format);
                    }
                    if (oldDesc == null || oldDesc.trim().isEmpty() || oldDesc.equals(oldCname)) {
                        t.setDescription("Wizard insert");
                    }
                    if (t.__getGroupsids() <= 0) {
                        t.setGroupsids(1);
                    }
                    t.save(true);
                    if (hasUser) {
                        reconcileTemplateAssignment(current, t, spec, small);
                    }
                    repaired = true;
                    Log.Debug(FirstStartSetup.class, "Template usage type repaired: " + t + " (" + spec.type + ")");
                } catch (Exception e) {
                    Log.Debug(FirstStartSetup.class, e);
                }
            }
            if (repaired) {
                TemplateHandler.clearCache();
            }
        } catch (Exception e) {
            Log.Debug(FirstStartSetup.class, e);
        }
    }

    private static void reconcileTemplateAssignment(User user, Template t, DemoSpec spec, boolean small) {
        int uid = user.__getIDS();
        int tid = t.__getIDS();
        ReturnValue existing = QueryHandler.getConnection().freeQuery(
                "SELECT ids FROM templatestousers WHERE usersids = " + uid + " AND templatesids = " + tid,
                MPSecurityManager.VIEW, null);
        boolean assigned = existing.hasData();
        ReturnValue other = QueryHandler.getConnection().freeQuery(
                "SELECT templatestousers.ids FROM templatestousers LEFT OUTER JOIN templates AS templates0 ON "
                + "templates0.ids = templatestousers.templatesids WHERE templatestousers.usersids = " + uid
                + " AND templates0.mimetype = '" + spec.type + "' AND templatestousers.templatesids <> " + tid,
                MPSecurityManager.VIEW, null);
        boolean hasOther = other.hasData();
        if (assigned && hasOther) {
            mpv5.db.common.QueryCriteria2 c = new mpv5.db.common.QueryCriteria2();
            c.and(new mpv5.db.common.QueryParameter(Context.getTemplatesToUsers(), "usersids", uid, mpv5.db.common.QueryParameter.EQUALS));
            c.and(new mpv5.db.common.QueryParameter(Context.getTemplatesToUsers(), "templatesids", tid, mpv5.db.common.QueryParameter.EQUALS));
            QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).delete(c);
        } else if (!assigned && !hasOther && (spec.type != Constants.TYPE_INVOICE || small == spec.smallBusiness)) {
            QueryData c = new QueryData();
            c.add("usersids", uid);
            c.add("templatesids", tid);
            c.add("groupsids", 1);
            c.add("cname", tid + "@" + uid + "@" + 1);
            QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).insert(c, null);
        }
    }

    static String sha256(File f) throws Exception {
        //java.io: the cached file has the template name, toPath() fails on umlauts without an UTF-8 file system encoding
        try (InputStream in = new java.io.FileInputStream(f)) {
            return sha256(in);
        }
    }

    private static String sha256(InputStream in) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] buf = new byte[8192];
        for (int n; (n = in.read(buf)) > 0;) {
            md.update(buf, 0, n);
        }
        StringBuilder b = new StringBuilder();
        for (byte x : md.digest()) {
            b.append(String.format("%02x", x));
        }
        return b.toString();
    }

    private static void step(Progress p, Step s) {
        if (p != null) {
            p.step(s);
        }
    }

    /**
     * The steps of {@link #prepare}
     */
    public enum Step {
        COUNTRIES, LANGUAGES, TEMPLATES
    }

    /**
     * Told before every step
     */
    public interface Progress {

        void step(Step s);
    }
}
