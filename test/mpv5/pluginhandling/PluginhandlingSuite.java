
package mpv5.pluginhandling;

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
@Suite.SuiteClasses({mpv5.pluginhandling.MPPLuginLoaderTest.class,mpv5.pluginhandling.UserPluginTest.class,mpv5.pluginhandling.PluginTest.class,mpv5.pluginhandling.MP5PluginTest.class})
public class PluginhandlingSuite {

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