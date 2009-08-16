
package mpv5.server;

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
@Suite.SuiteClasses({mpv5.server.XMLToDatabaseObjectJobTest.class,mpv5.server.MPServerTest.class,mpv5.server.MPServerJobTest.class,mpv5.server.XMLRPCServerTest.class,mpv5.server.XPropertyHandlerMappingTest.class,mpv5.server.MPServerRunnerTest.class})
public class ServerSuite {

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