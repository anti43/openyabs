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
        InputStream s = Main.class.getResourceAsStream("/extra/DE-" + Constants.EXTRA_FILES_VERSION + ".odt");
        final File f = FileDirectoryHandler.getTempFile("template-de", "odt");
        FileDirectoryHandler.copyFile(s, f);
        final CountDownLatch done = new CountDownLatch(1);
        TemplateHandler.importTemplateAndAssign(f, new FutureCallback<Template>() {
            @Override
            public void call(Template t) {
                try {
                    if (t != null) {
                        t.setGroupsids(1);
                        t.setCname(f.getName());
                        t.setMimetype(String.valueOf(Constants.TYPE_INVOICE));
                        t.setFormat("1,2,4,5,7,9");
                        t.setDescription("Wizard insert");
                        t.save(true);
                        QueryData c = new QueryData();
                        c.add("usersids", owner.__getIDS());
                        c.add("templatesids", t.__getIDS());
                        c.add("groupsids", 1);
                        c.add("cname", t.__getIDS() + "@" + owner.__getIDS() + "@" + 1);
                        QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).insert(c, null);
                        TemplateHandler.clearCache();
                    }
                } catch (Exception e) {
                    Log.Debug(FirstStartSetup.class, e);
                } finally {
                    done.countDown();
                }
            }
        });
        return wait <= 0 || done.await(wait, TimeUnit.MILLISECONDS);
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
