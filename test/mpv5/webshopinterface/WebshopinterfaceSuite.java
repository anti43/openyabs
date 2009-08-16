
package mpv5.webshopinterface;

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
@Suite.SuiteClasses({mpv5.webshopinterface.WSConnectionClientTest.class,mpv5.webshopinterface.WSDaemonTest.class,mpv5.webshopinterface.wsdjobs.WsdjobsSuite.class,mpv5.webshopinterface.WSIManagerTest.class,mpv5.webshopinterface.WSDaemonJobTest.class})
public class WebshopinterfaceSuite {

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