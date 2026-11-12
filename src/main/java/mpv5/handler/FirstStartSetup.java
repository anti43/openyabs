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
        return importTemplate(owner, "/extra/DE-" + Constants.EXTRA_FILES_VERSION + ".odt", "template-de",
                "1,2,4,5,7,9", Constants.TYPE_INVOICE, wait) != null;
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
                        "1,2,4,5,7,9", type, 60000, type != Constants.TYPE_INVOICE);
            } catch (Exception e) {
                Log.Debug(FirstStartSetup.class, e);
            }
        }
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
