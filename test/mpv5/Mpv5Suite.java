
package mpv5;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author anti
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({mpv5.webshopinterface.WebshopinterfaceSuite.class,mpv5.server.ServerSuite.class,mpv5.db.DbSuite.class,mpv5.utils.UtilsSuite.class,mpv5.compiler.CompilerSuite.class,mpv5.resources.ResourcesSuite.class,mpv5.usermanagement.UsermanagementSuite.class,mpv5.MainTest.class,mpv5.data.DataSuite.class,mpv5.logging.LoggingSuite.class,mpv5.mail.MailSuite.class,mpv5.bugtracker.BugtrackerSuite.class,mpv5.pluginhandling.PluginhandlingSuite.class,mpv5.i18n.I18nSuite.class,mpv5.handler.HandlerSuite.class,mpv5.ui.UiSuite.class,mpv5.globals.GlobalsSuite.class})
public class Mpv5Suite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}