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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
 * What a new database gets without asking: the countries, the bundled
 * languages and the invoice template, assigned to the administrator
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
     * Imports the bundled invoice template and assigns it to the user
     *
     * @param owner
     * @param wait how long to wait for the import, 0: do not wait
     * @return true if the template was imported (in time)
     * @throws Exception
     */
    public static boolean importTemplate(final User owner, long wait) throws Exception {
        return importTemplate(owner, INVOICE_TEMPLATE, "template-de",
                ITEM_FORMAT, Constants.TYPE_INVOICE, wait) != null;
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
        InputStream s = Main.class.getResourceAsStream(resource);
        final File f = FileDirectoryHandler.getTempFile(name, "odt");
        FileDirectoryHandler.copyFile(s, f);
        final CountDownLatch done = new CountDownLatch(1);
        final Template[] made = new Template[1];
        TemplateHandler.importTemplateAndAssign(f, new FutureCallback<Template>() {
            @Override
            public void call(Template t) {
                try {
                    if (t != null) {
                        t.setGroupsids(1);
                        t.setCname("template-de".equals(name) ? f.getName() : name);
                        t.setMimetype(String.valueOf(type));
                        t.setFormat(format);
                        t.setDescription("Wizard insert");
                        t.save(true);
                        if (!assign) {
                            made[0] = t;
                            return;
                        }
                        QueryData c = new QueryData();
                        c.add("usersids", owner.__getIDS());
                        c.add("templatesids", t.__getIDS());
                        c.add("groupsids", 1);
                        c.add("cname", t.__getIDS() + "@" + owner.__getIDS() + "@" + 1);
                        QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).insert(c, null);
                        TemplateHandler.clearCache();
                        made[0] = t;
                    }
                } catch (Exception e) {
                    Log.Debug(FirstStartSetup.class, e);
                } finally {
                    done.countDown();
                }
            }
        });
        if (wait > 0) {
            done.await(wait, TimeUnit.MILLISECONDS);
        }
        return made[0];
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
        step(progress, Step.TEMPLATES);
        try {
            importTemplate(admin, 60000);
        } catch (Exception e) {
            Log.Debug(FirstStartSetup.class, e);
        }
        importDemoTemplates(admin);
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
     * all groups (the invoice demo is imported, but the regular invoice
     * template stays the assigned one)
     *
     * @param admin
     */
    public static void importDemoTemplates(User admin) {
        for (Object[] d : DEMO_TEMPLATES) {
            int type = (Integer) d[0];
            try {
                importTemplate(admin, String.format("/extra/templates/demo-%02d.odt", type), "Demo - " + d[1],
                        demoFormat(type), type, 60000, type != Constants.TYPE_INVOICE);
            } catch (Exception e) {
                Log.Debug(FirstStartSetup.class, e);
            }
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
     * The table columns of the documents: position, quantity, text, price,
     * net total (the 5-column table of the bundled templates)
     */
    public static final String ITEM_FORMAT = "1,2,4,5,6";
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
     * landscape) and the column format is corrected - but only templates whose
     * file is still exactly the one shipped; changed templates stay as they are.
     */
    public static void upgradeDemoTemplates() {
        try {
            ReturnValue rv = QueryHandler.getConnection().freeQuery(
                    "SELECT ids FROM templates WHERE format = '" + OLD_FORMAT + "' AND description = 'Wizard insert'",
                    MPSecurityManager.VIEW, null);
            if (!rv.hasData()) {
                return;
            }
            for (Object[] row : rv.getData()) {
                try {
                    Template t = (Template) DatabaseObject.getObject(Context.getTemplate(), Integer.valueOf(row[0].toString()));
                    int type = Integer.parseInt(t.__getMimetype());
                    String hash = sha256(t.getFile());
                    String resource;
                    if (hash.equals(OLD_DEMOS.get(type))) {
                        resource = String.format("/extra/templates/demo-%02d.odt", type);
                    } else if (type == Constants.TYPE_INVOICE && OLD_INVOICE_TEMPLATES.contains(hash)) {
                        resource = INVOICE_TEMPLATE;
                    } else {
                        continue;
                    }
                    File f = FileDirectoryHandler.getTempFile("template-" + type, "odt");
                    FileDirectoryHandler.copyFile(Main.class.getResourceAsStream(resource), f);
                    QueryHandler.instanceOf().clone(Context.getFiles()).updateFileNow(f, t.__getFilename());
                    t.setFormat(demoFormat(type));
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

    static String sha256(File f) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        //java.io: the cached file has the template name, toPath() fails on umlauts without an UTF-8 file system encoding
        try (InputStream in = new java.io.FileInputStream(f)) {
            byte[] buf = new byte[8192];
            for (int n; (n = in.read(buf)) > 0;) {
                md.update(buf, 0, n);
            }
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
